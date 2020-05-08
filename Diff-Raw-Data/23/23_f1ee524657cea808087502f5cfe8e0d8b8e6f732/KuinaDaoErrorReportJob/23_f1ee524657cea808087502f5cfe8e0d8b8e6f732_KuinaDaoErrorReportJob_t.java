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
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.regex.Pattern;
 
 import jp.aonir.fuzzyxml.FuzzyXMLNode;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.resources.WorkspaceJob;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jdt.core.ICompilationUnit;
 import org.eclipse.jdt.core.IField;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.IType;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jdt.core.dom.ASTNode;
 import org.eclipse.jdt.core.dom.ASTVisitor;
 import org.eclipse.jdt.core.dom.MethodDeclaration;
 import org.eclipse.jdt.core.dom.PrimitiveType;
 import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
 import org.eclipse.jdt.core.dom.Type;
 import org.seasar.dolteng.core.kuina.KuinaEmulator;
 import org.seasar.dolteng.eclipse.Constants;
 import org.seasar.dolteng.eclipse.DoltengCore;
 import org.seasar.dolteng.eclipse.nls.Messages;
 import org.seasar.dolteng.eclipse.preferences.DoltengPreferences;
 import org.seasar.dolteng.eclipse.util.FuzzyXMLUtil;
 import org.seasar.dolteng.eclipse.util.JDTDomUtil;
 import org.seasar.dolteng.eclipse.util.JavaElementUtil;
 import org.seasar.dolteng.eclipse.util.ResourcesUtil;
 import org.seasar.dolteng.eclipse.util.TypeUtil;
 import org.seasar.dolteng.eclipse.util.ResourcesUtil.FindingHandler;
 import org.seasar.framework.convention.NamingConvention;
 import org.seasar.framework.util.StringUtil;
import org.seasar.kuina.dao.OrderingSpec;
 
 /**
  * @author taichi
  * 
  */
 public class KuinaDaoErrorReportJob extends WorkspaceJob {
 
     private ICompilationUnit unit;
 
     public KuinaDaoErrorReportJob(ICompilationUnit target) {
         super(Messages.PROCESS_VALIDATE);
         this.unit = target;
         setRule(this.unit.getSchedulingRule());
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.eclipse.core.runtime.jobs.Job#belongsTo(java.lang.Object)
      */
     @Override
     public boolean belongsTo(Object family) {
         return family == ResourcesPlugin.FAMILY_AUTO_BUILD
                 || family == ResourcesPlugin.FAMILY_MANUAL_BUILD;
 
     }
 
     /*
      * (non-Javadoc)
      * 
     * @see
     * org.eclipse.core.resources.WorkspaceJob#runInWorkspace(org.eclipse.core
     * .runtime.IProgressMonitor)
      */
     @Override
     public IStatus runInWorkspace(IProgressMonitor monitor)
             throws CoreException {
         IResource r = unit.getResource();
         r.deleteMarkers(Constants.ID_KUINA_ERROR, false, 0);
         ASTNode node = JavaElementUtil.parse(r);
         if (node != null) {
             node.accept(new Visitor(unit.getJavaProject(), unit
                     .findPrimaryType()));
         }
         return Status.OK_STATUS;
     }
 
     private class Visitor extends ASTVisitor {
         IJavaProject project = null;
 
         IType primary = null;
 
         Set<String> queryPatterns;
 
         public Visitor(IJavaProject project, IType type) {
             this.project = project;
             this.primary = type;
             this.queryPatterns = new HashSet<String>();
             this.queryPatterns.add(int.class.getName());
             this.queryPatterns.add(Integer.class.getName());
         }
 
         @Override
         public boolean visit(MethodDeclaration method) {
             Type t = method.getReturnType2();
             IType returnType = resolve(t);
             String methodName = method.getName().getIdentifier();
             List params = method.parameters();
             if (params.size() == 1) {
                 SingleVariableDeclaration param = (SingleVariableDeclaration) params
                         .get(0);
                 IType type = resolve(param.getType());
                 if (type != null
                         && type.exists()
                         && returnType != null
                         && returnType.getFullyQualifiedName().equals(
                                 type.getFullyQualifiedName())) {
                     return false;
                 }
             }
             for (Iterator i = params.iterator(); i.hasNext();) {
                 SingleVariableDeclaration param = (SingleVariableDeclaration) i
                         .next();
                 process(params, param, returnType, methodName);
             }
             return false;
         }
 
         public IType resolve(Type t) {
             return JDTDomUtil.resolve(t, primary, project);
         }
 
         public void process(List params, SingleVariableDeclaration param,
                 IType returnType, String methodName) {
             try {
                 // Irenka があれば、もっとスッキリ実装出来そうなもんだけどなぁ…
                 DoltengPreferences pref = DoltengCore.getPreferences(project);
                 NamingConvention nc = pref.getNamingConvention();
                 if (returnType == null || StringUtil.isEmpty(methodName)) {
                     return;
                 }
                 String paramType = toParamType(param);
                 String name = param.getName().getIdentifier();
                 if (checkDto(params, param, methodName, nc, paramType, name)) {
                     // DTO っぽい時は、チェックの対象外にするが、引数が複数あれば、エラー
                     return;
                 }
 
                 String pkg = primary.getPackageFragment().getElementName();
                 IPath resourcePath = new Path(pkg.replace('.', '/'));
                 if (checkSqlFile(methodName, resourcePath)) {
                     // SQL ファイルが存在するなら、オッケー。
                     return;
                 }
 
                 if (checkOrmXml(methodName, nc, pkg, resourcePath)) {
                     // NamedQueryが存在するなら、オッケー
                     return;
                 }
 
                 if (KuinaEmulator.isOrderbyPatterns(name)) {
                    if (OrderingSpec.class.getName().equals(paramType) == false
                            && String.class.getName().equals(paramType) == false) {
                         String msg = Messages.bind(
                                 Messages.ILLEGAL_KEYWORD_TYPE, new String[] {
                                        name,
                                                "String, OrderingSpec or OrderingSpec[]" });
                         report(param.getName(),
                                 Constants.ERROR_TYPE_KUINA_TYPE, methodName,
                                 name, msg);
                     }
                     return;
                 }
 
                 if (KuinaEmulator.isQueryPatterns(name)) {
                     if (this.queryPatterns.contains(paramType) == false) {
                         String msg = Messages.bind(
                                 Messages.ILLEGAL_KEYWORD_TYPE, new String[] {
                                         name, "int" });
                         report(param.getName(),
                                 Constants.ERROR_TYPE_KUINA_TYPE, methodName,
                                 name, msg);
                     }
                     return;
                 }
                 String[] names = KuinaEmulator.splitPropertyName(name);
                 IType type = returnType;
                 for (int i = 0; i < names.length; i++) {
                     String fieldName = names[i];
                     IField f = type.getField(fieldName);
                     if (f == null || f.exists() == false) {
                         String msg = Messages.bind(
                                 Messages.ILLEGAL_PARAMETER_NAME, new String[] {
                                         name, type.getFullyQualifiedName() });
                         report(param.getName(),
                                 Constants.ERROR_TYPE_KUINA_NAME, methodName,
                                 name, msg);
                         break;
                     }
                     String fieldType = TypeUtil.getResolvedTypeName(f
                             .getTypeSignature(), type);
                     if (names.length <= i + 1) {
                         // $区切りの最後の名前では、型の整合性チェックをする。
                         if (fieldType.equals(paramType) == false) {
                             String msg = Messages.bind(
                                     Messages.ILLEGAL_PARAMETER_TYPE, name);
                             report(param.getType(),
                                     Constants.ERROR_TYPE_KUINA_TYPE,
                                     methodName, name, msg);
                         }
                     } else {
                         IType t = this.project.findType(fieldType);
 
                         // 引数名から型を辿る時、GenericsなCollectionなら、そのパラメタライズド型を
                         // 妥当性チェックする対象の型として使う。
                         Set<String> infs = new HashSet<String>(Arrays.asList(t
                                 .getSuperInterfaceNames()));
                         infs.add(t.getFullyQualifiedName());
                         if (infs.contains(Collection.class.getName())) {
                             fieldType = TypeUtil.getParameterizedTypeName(f
                                     .getTypeSignature(), 0, type);
                             type = this.project.findType(fieldType);
                         } else if (infs.contains(Map.class.getName())) {
                             fieldType = TypeUtil.getParameterizedTypeName(f
                                     .getTypeSignature(), 1, type);
                             type = this.project.findType(fieldType);
                         } else {
                             type = t;
                         }
                     }
                 }
             } catch (Exception e) {
                 DoltengCore.log(e);
             }
         }
 
         private boolean checkDto(List params, SingleVariableDeclaration param,
                 String methodName, NamingConvention nc, String paramType,
                 String name) {
             if (nc.isTargetClassName(paramType, nc.getDtoSuffix())) {
                 if (1 < params.size()) {
                     String msg = Messages.bind(
                             Messages.ILLEGAL_PARAMETER_COUNT,
                             new String[] { methodName });
                     report(param.getName(), Constants.ERROR_TYPE_KUINA_NAME,
                             methodName, name, msg);
                     return false;
                 }
                 return true;
             }
             return false;
         }
 
         private boolean checkSqlFile(String methodName, IPath resourcePath)
                 throws JavaModelException, CoreException {
             String sql = primary.getElementName() + "_" + methodName;
             Pattern sqlPtn = Pattern.compile(sql + ".*\\.sql",
                     Pattern.CASE_INSENSITIVE);
             if (ResourcesUtil.findDir(project.getProject(), resourcePath,
                     sqlPtn, FindingHandler.NULL)) {
                 return true;
             }
             return false;
         }
 
         private boolean checkOrmXml(String methodName, NamingConvention nc,
                 String pkg, IPath resourcePath) throws JavaModelException,
                 CoreException {
             String entityName = "";
             String en = primary.getElementName();
             if (nc.isTargetClassName(primary.getFullyQualifiedName(), nc
                     .getDaoSuffix())) {
                 entityName = en.substring(0, en.length() - 3);
             } else {
                 if (pkg.endsWith(nc.getEntityPackageName())) {
                     entityName = en;
                 }
             }
             Pattern ormXml = Pattern.compile(entityName + "Orm\\.xml",
                     Pattern.CASE_INSENSITIVE);
             XmlFindingHandler handler = new XmlFindingHandler(entityName,
                     methodName);
             if ((ResourcesUtil.findDir(project.getProject(), new Path(
                     "META-INF"), ormXml, handler) && handler.hasNamedQuery)
                     || (ResourcesUtil.findDir(project.getProject(),
                             resourcePath, ormXml, handler) && handler.hasNamedQuery)
                     || (ResourcesUtil
                             .findDir(project.getProject(), resourcePath
                                     .removeLastSegments(1).append(
                                             nc.getEntityPackageName()), ormXml,
                                     handler) && handler.hasNamedQuery)) {
                 return true;
             }
             return false;
         }
 
         private String toParamType(SingleVariableDeclaration param) {
             Type t = param.getType();
             String paramType = "";
             if (t.isPrimitiveType()) {
                 PrimitiveType pt = (PrimitiveType) t;
                 paramType = pt.getPrimitiveTypeCode().toString();
             } else {
                 paramType = resolve(t).getFullyQualifiedName();
             }
             return paramType;
         }
 
         @SuppressWarnings("unchecked")
         public void report(ASTNode node, String errorType, String methodName,
                 String paramName, String msg) {
             try {
                 Map m = new HashMap();
                 m.put(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
                 m.put(IMarker.CHAR_START, new Integer(node.getStartPosition()));
                 m.put(IMarker.CHAR_END, new Integer(node.getStartPosition()
                         + node.getLength()));
                 m.put(IMarker.MESSAGE, msg);
                 m.put(Constants.MARKER_ATTR_ERROR_TYPE_KUINA, errorType);
                 m.put(Constants.MARKER_ATTR_METHOD_NAME, methodName);
                 m.put(Constants.MARKER_ATTR_PARAMETER_NAME, paramName);
                 IResource r = unit.getResource();
                 IMarker marker = r.createMarker(Constants.ID_KUINA_ERROR);
                 marker.setAttributes(m);
             } catch (Exception e) {
                 DoltengCore.log(e);
             }
         }
     }
 
     private static class XmlFindingHandler implements FindingHandler {
         public boolean hasNamedQuery = false;
 
         private String entityName;
 
         private String methodName;
 
         public XmlFindingHandler(String entityName, String methodName) {
             this.entityName = entityName;
             this.methodName = methodName;
         }
 
         public void handle(IFile file) {
             try {
                 String query = "//named-query[@name=\"" + entityName + "."
                         + methodName + "\"]";
                 FuzzyXMLNode[] list = FuzzyXMLUtil.selectNodes(file, query);
                 hasNamedQuery = list != null && 0 < list.length;
             } catch (Exception e) {
                 DoltengCore.log(e);
             }
         }
     }
 }
