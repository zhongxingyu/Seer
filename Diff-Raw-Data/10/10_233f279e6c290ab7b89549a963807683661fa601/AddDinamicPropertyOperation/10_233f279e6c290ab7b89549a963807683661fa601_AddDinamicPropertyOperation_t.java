 /*
  * Copyright 2004-2010 the Seasar Foundation and the Others.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
  * either express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 package org.seasar.dolteng.eclipse.operation;
 
 import jp.aonir.fuzzyxml.FuzzyXMLAttribute;
 
 import org.eclipse.core.resources.IWorkspaceRunnable;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jdt.core.IJavaElement;
 import org.eclipse.jdt.core.IMethod;
 import org.eclipse.jdt.core.IType;
 import org.eclipse.jdt.ui.CodeGeneration;
 import org.eclipse.jdt.ui.JavaUI;
 import org.eclipse.ui.IEditorPart;
 import org.seasar.dolteng.eclipse.util.ProgressMonitorUtil;
 import org.seasar.dolteng.eclipse.util.ProjectUtil;
 import org.seasar.framework.util.StringUtil;
 
 /**
  * @author taichi
  * 
  */
 public class AddDinamicPropertyOperation implements IWorkspaceRunnable {
 
     private IType type;
 
     private String elementId;
 
     private FuzzyXMLAttribute[] attrs;
 
     public AddDinamicPropertyOperation(IType type, String id,
             FuzzyXMLAttribute[] attrs) {
         super();
         this.type = type;
         this.elementId = StringUtil.capitalize(id);
         this.attrs = attrs;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.eclipse.core.resources.IWorkspaceRunnable#run(org.eclipse.core.runtime.IProgressMonitor)
      */
     public void run(IProgressMonitor monitor) throws CoreException {
         monitor = ProgressMonitorUtil.care(monitor);
         IJavaElement created = null;
         String lineDelimiter = ProjectUtil.getLineDelimiterPreference(type
                 .getJavaProject().getProject());
         for (FuzzyXMLAttribute attr : attrs) {
             if (attr.getName().equalsIgnoreCase("id") == false) {
                 created = createMethod(lineDelimiter, attr);
             }
         }
 
         IEditorPart part = JavaUI.openInEditor(type);
         JavaUI.revealInEditor(part, created == null ? type : created);
     }
 
     private IJavaElement createMethod(String lineDelimiter,
             FuzzyXMLAttribute attr) throws CoreException {
         StringBuffer stb = new StringBuffer();
        String attrName = attr.getName();
        String methodName;
        if (attrName.equals("class")) {
            methodName = "get" + this.elementId + "StyleClass";
        } else {
            methodName = "get" + this.elementId
                    + StringUtil.capitalize(attr.getName());
        }
         IMethod mtd = type.getMethod(methodName, StringUtil.EMPTY_STRINGS);
         if (mtd != null && mtd.exists()) {
             return mtd;
         }
 
         String comment = CodeGeneration.getMethodComment(type
                 .getCompilationUnit(), type.getElementName(), methodName,
                 StringUtil.EMPTY_STRINGS, StringUtil.EMPTY_STRINGS, "QString;",
                 StringUtil.EMPTY_STRINGS, null, lineDelimiter);
         stb.append(comment);
         stb.append(lineDelimiter);
         stb.append("public String ");
         stb.append(methodName);
         stb.append("() {");
         stb.append(lineDelimiter);
         stb.append(ProjectUtil.createIndentString(1, type.getJavaProject()));
         stb.append("return ");
         String value = attr.getValue();
         if (StringUtil.isEmpty(value)) {
             stb.append("null");
         } else {
             stb.append('"');
             stb.append(value);
             stb.append('"');
         }
         stb.append(';');
         stb.append(lineDelimiter);
         stb.append('}');
         stb.append(lineDelimiter);
         return type.createMethod(stb.toString(), null, false, null);
     }
 
 }
