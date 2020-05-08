 /*
    Copyright 2011 Frode Carlsen
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
  */
 package archie.rule;
 
 import java.util.List;
 import java.util.regex.Pattern;
 
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.jdt.core.IJavaElement;
 import org.eclipse.jdt.core.dom.ASTVisitor;
 import org.eclipse.jdt.core.dom.IBinding;
 import org.eclipse.jdt.core.dom.IMethodBinding;
 import org.eclipse.jdt.core.dom.ITypeBinding;
 import org.eclipse.jdt.core.dom.ImportDeclaration;
 import org.eclipse.jdt.core.dom.MethodInvocation;
 
 import archie.builder.ArchieCompilationUnit;
 
 public class DenyDependencyRule implements ArchieRule {
 
     private final class CheckMethodInvocationsVisitor extends ASTVisitor {
         private final ArchieCompilationUnit acu;
 
         private CheckMethodInvocationsVisitor(ArchieCompilationUnit acu) {
             this.acu = acu;
         }
 
         @Override
         public boolean visit(MethodInvocation node) {
             IMethodBinding methodBinding = node.resolveMethodBinding();
             ITypeBinding declaringClass = methodBinding.getDeclaringClass();
             IMethodBinding methodDeclaration = methodBinding.getMethodDeclaration();
 
             String denyToSrcLocation = getSourceLocation(methodBinding, declaringClass);
 
             if (denyToSrc != null && denyToSrcLocation != null && !denyToSrc.matcher(denyToSrcLocation).find()) {
                 return true;
             }
 
             String declaringClassName = declaringClass.getName();
             String declaringClassPackage = declaringClass.getPackage() == null ? "" : declaringClass.getPackage().getName() + ".";
             String methodDeclarationName = methodDeclaration.getName();
 
             String methodCall = declaringClassPackage + declaringClassName + "#" + methodDeclarationName;
 
             if (denyTo.matcher(methodCall).matches()) {
                 String txt = createWarningText(this.acu.getFullyQualifiedClassName(), methodCall, this.acu.getSourceLocation(),
                         denyToSrcLocation);
                 this.acu.addMarker("Method dependency " + txt, this.acu.getCompilationUnit().getLineNumber(node.getStartPosition()),
                         IMarker.SEVERITY_WARNING);
             }
 
             return true;
         }
     }
 
     private Pattern denyFrom;
     private Pattern denyTo;
     private Pattern denyFromSrc;
     private Pattern denyToSrc;
     private boolean enabled;
 
     public DenyDependencyRule(Pattern denyFromSrc, Pattern denyFrom, Pattern denyToSrc, Pattern denyTo, Boolean enabled) {
         this.denyFromSrc = denyFromSrc;
         this.denyFrom = denyFrom;
         this.denyToSrc = denyToSrc;
         this.denyTo = denyTo;
         this.enabled = enabled == null ? false : enabled;
     }
 
     public DenyDependencyRule(String denyFromSrc, String denyFrom, String denyToSrc, String denyTo, Boolean enabled) {
         this(denyFromSrc == null ? null : Pattern.compile(denyFromSrc)
                 , Pattern.compile(denyFrom)
                 , denyToSrc == null ? null : Pattern.compile(denyToSrc)
                 , Pattern.compile(denyTo)
                 , enabled);
     }
 
     public DenyDependencyRule() {
         this("src/main/java", ".*", ".*", ".*", true);
     }
 
     @Override
     public void check(final ArchieCompilationUnit acu) {
         if (shouldCheckRule(acu)) {
             if (isMatchMethodPattern()) {
                 acu.accept(new CheckMethodInvocationsVisitor(acu));
             } else {
                 checkImportDeclarations(acu);
             }
         }
     }
 
     String getSourceLocation(IBinding methodBinding, ITypeBinding declaringClass) {
         if (methodBinding == null) {
             throw new IllegalStateException("Cannot find methodbinding, running without workspace?");
         }
 
         IJavaElement javaElement = methodBinding.getJavaElement();
         if (javaElement == null) {
             return null;
         }
         IPath path = javaElement.getPath();
         if ("jar".equals(path.getFileExtension())) {
             return path.lastSegment();
         } else if (declaringClass != null) {
             return declaringClass.getJavaElement().getResource().getProjectRelativePath().toPortableString();
         } else {
             return javaElement.getPath().toPortableString();
         }
     }
 
     private boolean shouldCheckRule(final ArchieCompilationUnit acu) {
         return isEnabled()
                 && matchPackageName(acu)
                 && matchSourceLocation(acu);
     }
 
     private void checkImportDeclarations(final ArchieCompilationUnit acu) {
         @SuppressWarnings("unchecked")
         List<ImportDeclaration> imports = acu.getCompilationUnit().imports();
         for (ImportDeclaration i : imports) {
 
             String denyToSrcLocation = getSourceLocation(i.resolveBinding(), null);
             if (denyToSrc != null && denyToSrcLocation != null && !denyToSrc.matcher(denyToSrcLocation).find()) {
                 continue;
             }
 
             String importName = i.getName().getFullyQualifiedName();
 
             if (denyTo.matcher(importName).lookingAt()) {
                 acu.addMarker(
                         "Import dependency "
                                 + createWarningText(acu.getPackageName(), importName, acu.getSourceLocation(), denyToSrcLocation)
                         , acu.getCompilationUnit().getLineNumber(i.getStartPosition()),
                         IMarker.SEVERITY_WARNING);
             }
         }
     }
 
     private boolean isMatchMethodPattern() {
         return denyTo.pattern().contains("#");
     }
 
     private boolean matchSourceLocation(final ArchieCompilationUnit acu) {
         return denyFromSrc != null && denyFromSrc.matcher(acu.getSourceLocation()).find();
     }
 
     private boolean matchPackageName(final ArchieCompilationUnit acu) {
        return denyFrom.matcher(acu.getFullyQualifiedClassName()).matches();
     }
 
     private String createWarningText(String from, String to, final String denyFromSrcLocation, String denyToSrcLocation) {
         return String.format(
                 "[%s] -> [%s]\n(rule = [%s] !> [%s] ; locationrule = (%s:[%s]) !> (%s:[%s]))"
                 , from, to, denyFrom, denyTo, denyFromSrcLocation, denyFromSrc, denyToSrcLocation, denyToSrc);
     }
 
     public Pattern getDenyFrom() {
         return this.denyFrom;
     }
 
     public Pattern getDenyTo() {
         return this.denyTo;
     }
 
     public Pattern getDenyFromSrc() {
         return this.denyFromSrc;
     }
 
     public Pattern getDenyToSrc() {
         return this.denyToSrc;
     }
 
     public void setDenyFrom(Pattern denyFrom) {
         this.denyFrom = denyFrom;
     }
 
     public void setDenyTo(Pattern denyTo) {
         this.denyTo = denyTo;
     }
 
     public void setDenyFromSrc(Pattern denyFromSrc) {
         this.denyFromSrc = denyFromSrc;
     }
 
     public void setDenyToSrc(Pattern denyToSrc) {
         this.denyToSrc = denyToSrc;
     }
 
     public boolean isEnabled() {
         return enabled;
     }
 
     public void setEnabled(boolean enabled) {
         this.enabled = enabled;
     }
 }
