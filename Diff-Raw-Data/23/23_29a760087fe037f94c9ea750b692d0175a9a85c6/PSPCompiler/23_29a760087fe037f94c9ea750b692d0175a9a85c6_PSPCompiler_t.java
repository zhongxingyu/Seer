 package org.eclipse.imp.presentation.compiler;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.regex.Pattern;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceProxy;
 import org.eclipse.core.resources.IResourceProxyVisitor;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.imp.builder.MarkerCreator;
 import org.eclipse.imp.language.Language;
 import org.eclipse.imp.language.LanguageRegistry;
 import org.eclipse.imp.model.ISourceProject;
 import org.eclipse.imp.model.ModelFactory;
 import org.eclipse.imp.model.ModelFactory.ModelException;
 import org.eclipse.imp.parser.IParseController;
 import org.eclipse.imp.presentation.PSPActivator;
 import org.eclipse.imp.presentation.builders.PSPBuilder;
 import org.eclipse.imp.presentation.parser.PSPParseController;
 import org.eclipse.imp.presentation.parser.Ast.ASTNode;
 import org.eclipse.imp.presentation.parser.Ast.Iexpression;
 import org.eclipse.imp.presentation.parser.Ast.IlanguageMember;
 import org.eclipse.imp.presentation.parser.Ast.InodePresentationAttribute;
 import org.eclipse.imp.presentation.parser.Ast.IpresentationMember;
 import org.eclipse.imp.presentation.parser.Ast.ItextAttributeDecl;
 import org.eclipse.imp.presentation.parser.Ast.ItextColoringMember;
 import org.eclipse.imp.presentation.parser.Ast.colorAttributeDecl;
 import org.eclipse.imp.presentation.parser.Ast.compilationUnit;
 import org.eclipse.imp.presentation.parser.Ast.expression0;
 import org.eclipse.imp.presentation.parser.Ast.expression1;
 import org.eclipse.imp.presentation.parser.Ast.foldableNode;
 import org.eclipse.imp.presentation.parser.Ast.foldableNodeList;
 import org.eclipse.imp.presentation.parser.Ast.foldingSpec;
 import org.eclipse.imp.presentation.parser.Ast.fontAttributeDecl;
 import org.eclipse.imp.presentation.parser.Ast.iconPresentation;
 import org.eclipse.imp.presentation.parser.Ast.importSpecList;
 import org.eclipse.imp.presentation.parser.Ast.labelPresentation;
 import org.eclipse.imp.presentation.parser.Ast.languageBody;
 import org.eclipse.imp.presentation.parser.Ast.languageMemberList;
 import org.eclipse.imp.presentation.parser.Ast.languageSpec;
 import org.eclipse.imp.presentation.parser.Ast.literal0;
 import org.eclipse.imp.presentation.parser.Ast.literal1;
 import org.eclipse.imp.presentation.parser.Ast.nodePresentation;
 import org.eclipse.imp.presentation.parser.Ast.nodePresentationAttributeList;
 import org.eclipse.imp.presentation.parser.Ast.nodeSpec;
 import org.eclipse.imp.presentation.parser.Ast.outlineSpec;
 import org.eclipse.imp.presentation.parser.Ast.outlineSpecMemberList;
 import org.eclipse.imp.presentation.parser.Ast.presentationMemberList;
 import org.eclipse.imp.presentation.parser.Ast.presentationSpec;
 import org.eclipse.imp.presentation.parser.Ast.resourceDecl;
 import org.eclipse.imp.presentation.parser.Ast.styleAttributesDecl;
 import org.eclipse.imp.presentation.parser.Ast.styleKeyList;
 import org.eclipse.imp.presentation.parser.Ast.textAttributeDeclList;
 import org.eclipse.imp.presentation.parser.Ast.textColoringMemberList;
 import org.eclipse.imp.presentation.parser.Ast.textColoringSpec;
 import org.eclipse.imp.presentation.parser.Ast.tokenDecl;
 import org.eclipse.imp.runtime.RuntimePlugin;
 import org.eclipse.imp.utils.StreamUtils;
import org.eclipse.imp.wizards.ExtensionEnabler;
 import org.eclipse.imp.wizards.WizardUtilities;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jdt.core.formatter.CodeFormatter;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.Document;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.pde.core.plugin.IExtensions;
 import org.eclipse.pde.core.plugin.IPluginAttribute;
 import org.eclipse.pde.core.plugin.IPluginElement;
 import org.eclipse.pde.core.plugin.IPluginExtension;
 import org.eclipse.pde.core.plugin.IPluginModel;
 import org.eclipse.pde.internal.core.bundle.BundlePluginModel;
 import org.eclipse.pde.internal.core.bundle.WorkspaceBundleModel;
 import org.eclipse.pde.internal.core.ibundle.IBundleModel;
 import org.eclipse.text.edits.MalformedTreeException;
 import org.eclipse.text.edits.TextEdit;
 
 /**
  * This compiler translates a presentation specification file into a set of Java classes that 
  * implement the interfaces associated with the various IMP extension points that govern IDE
  * presentation appearance (e.g., token coloring, source folding, outlining).
  * @author rfuhrer@watson.ibm.com
  */
 public class PSPCompiler {
 	private static class Pair<T1,T2> {
 		public T1 fst;
 		public T2 snd;
 		public Pair(T1 v1, T2 v2) {
 			fst= v1;
 			snd= v2;
 		}
 	}
 
 	private static final String KW_ICON= "icon";
 
     private static final String KW_STYLE= "style";
 
     private static final String KW_COLOR= "color";
 
     private static final String KW_FONT= "font";
 
     // public static final String PROBLEM_MARKER_ID= Activator.kPluginID + ".$PROBLEM_ID$";
     public final String PROBLEM_MARKER_ID;
 
     private final PSPBuilder fBuilder;
 
     private IProject fProject;
 
     private ISourceProject fSrcProject;
 
     private IFile fSpecFile;
 
     Map<ASTNode, String> fTranslationMap= new HashMap<ASTNode, String>();
 
     private Language fLanguage;
 
     private String fPackageName;
 
     private ArrayList<String> fImports;
 
     private textColoringSpec fColoringSpec;
 
     private presentationSpec fPresentationSpec;
 
     private outlineSpec fOutlineSpec;
 
     private foldingSpec fFoldingSpec;
 
     private Map<String, String> fManifestMap;
 
     public PSPCompiler(PSPBuilder builder, String problem_marker_id) {
         fBuilder= builder;
         PROBLEM_MARKER_ID= problem_marker_id;
     }
 
     public String getFileContents(IFile file) {
         char[] buf= null;
         try {
             File javaFile= new File(file.getLocation().toOSString());
             FileReader fileReader= new FileReader(javaFile);
             int len= (int) javaFile.length();
 
             buf= new char[len];
             fileReader.read(buf, 0, len);
             return new String(buf);
         } catch (FileNotFoundException fnf) {
             System.err.println(fnf.getMessage());
             return "";
         } catch (IOException io) {
             System.err.println(io.getMessage());
             return "";
         }
     }
 
     private static String formatJavaCode(String contents) {
         CodeFormatter formatter= org.eclipse.jdt.core.ToolFactory.createCodeFormatter(JavaCore.getOptions());
         TextEdit te= formatter.format(CodeFormatter.K_COMPILATION_UNIT, contents, 0, contents.length(), 0, null);
 
         if (te == null)
             return contents;
 
         IDocument l_doc= new Document(contents);
         try {
             te.apply(l_doc);
         } catch (MalformedTreeException e) {
             e.printStackTrace();
         } catch (BadLocationException e) {
             e.printStackTrace();
         } catch (NullPointerException e) {
             // Can happen that te is null
             e.printStackTrace();
         }
         contents= l_doc.get();
         return contents;
     }
 
     public void compile(IFile specFile, IProgressMonitor mon) {
         if (specFile == null) {
             PSPActivator.getInstance().writeErrorMsg("PSPCompiler.compile(..): source file is null???");
             return;
         }
 
         fSpecFile= specFile;
         compilationUnit astRoot= parseSpecification(specFile, mon);
 
         if (astRoot == null) {
             PSPActivator.getInstance().writeErrorMsg("PSPCompiler.compile(..): parsing produced a null AST; aborting compile.");
             return;
         }
 
         if (!specOk(astRoot)) {
             return;
         }
 
         IFolder srcFolder= getProjectSourceFolder(fProject, specFile);
 
         collectCodeParams(astRoot);
         createExtensions(fProject, astRoot, mon);
         createServiceImplementations(fProject, astRoot, srcFolder.getFullPath().removeFirstSegments(1), fPackageName, mon);
     }
 
     private boolean specOk(compilationUnit astRoot) {
         boolean result= true;
 
         if (astRoot.getlanguageSpecList().size() > 1) {
             languageSpec secondLang= astRoot.getlanguageSpecList().getlanguageSpecAt(1);
             createErrorMarker(secondLang, "Restriction: only 1 language specification per file is allowed.");
             result= false;
         }
         languageSpec langSpec= astRoot.getlanguageSpecList().getlanguageSpecAt(0);
         String langName= langSpec.getlangName().toString();
 
         fLanguage= LanguageRegistry.findLanguage(langName);
 
         if (fLanguage == null) {
             // TODO Look in other projects in the workspace
             String projLangName= WizardUtilities.discoverLanguageForProject(fProject);
 
             if (projLangName != null && projLangName.equals(langName)) {
                 fLanguage= new Language(projLangName, "", "", "", "", "", "", "", "", null);
             } else {
                 createErrorMarker(langSpec.getlangName(), "Non-existent language: " + langName);
                 result= false;
             }
         }
 
         languageBody langBody= langSpec.getlanguageBody();
         languageMemberList langMembers= langBody.getlanguageMembers();
 
         List<IlanguageMember> coloringMembers= membersOf(textColoringSpec.class, langMembers);
         List<IlanguageMember> presMembers= membersOf(presentationSpec.class, langMembers);
         List<IlanguageMember> outlineMembers= membersOf(outlineSpec.class, langMembers);
         List<IlanguageMember> foldingMembers= membersOf(foldingSpec.class, langMembers);
 
         if (coloringMembers.size() > 1) {
             createErrorMarker((ASTNode) coloringMembers.get(1), "Only one text coloring specification is allowed per language");
             result= false;
         } else {
             if (!coloringMembers.isEmpty()) {
                 fColoringSpec= (textColoringSpec) coloringMembers.get(0);
             }
         }
         if (presMembers.size() > 1) {
             createErrorMarker((ASTNode) presMembers.get(1), "Only one presentation specification is allowed per language");
             result= false;
         } else {
             if (!presMembers.isEmpty()) {
                 fPresentationSpec= (presentationSpec) presMembers.get(0);
             }
         }
         if (outlineMembers.size() > 1) {
             createErrorMarker((ASTNode) outlineMembers.get(1), "Only one outline specification is allowed per language");
             result= false;
         } else {
             if (!outlineMembers.isEmpty()) {
                 fOutlineSpec= (outlineSpec) outlineMembers.get(0);
             }
         }
         if (foldingMembers.size() > 1) {
             createErrorMarker((ASTNode) foldingMembers.get(1), "Only one folding specification is allowed per language");
             result= false;
         } else {
             if (!foldingMembers.isEmpty()) {
                 fFoldingSpec= (foldingSpec) foldingMembers.get(0);
             }
         }
         return result;
     }
 
     private List<IlanguageMember> membersOf(Class<?> whatClass, languageMemberList memberList) {
         List<IlanguageMember> members= new ArrayList<IlanguageMember>();
         for(int i=0; i < memberList.size(); i++) {
             IlanguageMember langMember= memberList.getlanguageMemberAt(i);
             if (whatClass.isInstance(langMember)) {
                 members.add(langMember);
             }
         }
         return members;
     }
     private void createErrorMarker(ASTNode markedNode, String msg) {
         int startLine= markedNode.getLeftIToken().getLine();
         int charStart= markedNode.getLeftIToken().getStartOffset();
         int charEnd= markedNode.getRightIToken().getEndOffset()+1;
 
         fBuilder.createMarker(fSpecFile, startLine, charStart, charEnd, msg, IMarker.SEVERITY_ERROR);
     }
 
     private void createServiceImplementations(IProject project, compilationUnit astRoot, IPath srcFolderPath, String pkgName, IProgressMonitor mon) {
         createLabelProvider(astRoot, srcFolderPath, pkgName, mon);
         createTokenColorer(astRoot, srcFolderPath, pkgName, mon);
         createModelBuilder(astRoot, srcFolderPath, pkgName, mon);
         createFoldingUpdater(astRoot, srcFolderPath, pkgName, mon);
     }
 
     private void createLabelProvider(compilationUnit astRoot, IPath srcFolderPath, String basePkgName, IProgressMonitor mon) {
         if (fPresentationSpec == null)
             return;
         Map<String,String> subs= defineStandardSubstitutions(astRoot);
         String pkgName= basePkgName.concat(".labelProvider");
         String lpClassName= fLanguage.getName().concat("LabelProvider");
         String liClassName= fLanguage.getName().concat("LabelImages");
         String resClassName= fLanguage.getName().concat("Resources");
         String qualActivator= determineBundleActivator();
         String unqualActivator= qualActivator.substring(qualActivator.lastIndexOf('.')+1);
 
         subs.put("$PACKAGE_NAME$", pkgName);
         subs.put("$LABEL_PROVIDER_CLASS_NAME$", lpClassName);
         subs.put("$LABEL_IMAGES_CLASS_NAME$", liClassName);
         subs.put("$RESOURCES_CLASS_NAME$", resClassName);
         subs.put("$BUNDLE_ID$", determineBundleID());
         subs.put("$BUNDLE_ACTIVATOR_QUAL_CLASS$", qualActivator);
         subs.put("$BUNDLE_ACTIVATOR_CLASS$", unqualActivator);
 
         Map<String,String> resNameToIconPath= new HashMap<String,String>();
         Map<String,String> imageCases= new HashMap<String, String>();
         Map<String,Pair> labelCases= new HashMap<String, Pair>();
         presentationMemberList presMembers= fPresentationSpec.getmembers();
 
         collectPresentationInfo(presMembers, resNameToIconPath, imageCases, labelCases);
 
         StringBuilder labelsSB= new StringBuilder();
         for(String nodeType: labelCases.keySet()) {
             Pair<String,String> nodeInfo= labelCases.get(nodeType);
             labelsSB.append("if (n instanceof " + nodeType + ") {\n");
             labelsSB.append("   " + nodeType + " " + nodeInfo.fst + " = (" + nodeType + ") n;\n");
             labelsSB.append("   return " + nodeInfo.snd + ";\n");
             labelsSB.append("}\n");
         }
         StringBuilder imagesSB= new StringBuilder();
         for(String nodeName: imageCases.keySet()) {
             imagesSB.append("if (n instanceof " + nodeName + ") {\n");
             imagesSB.append("   return " + liClassName + "." + imageCases.get(nodeName) + ";\n");
             imagesSB.append("}\n");
         }
         StringBuilder declsSB= new StringBuilder();
         for(String nodeName: resNameToIconPath.keySet()) {
             String iconPath= resNameToIconPath.get(nodeName);
             declsSB.append("public static Image " + nodeName + " = sImageRegistry.get(\"" + iconPath + "\");\n");
         }
         
         subs.put("$NODE_LABEL_CASES$", labelsSB.toString());
         subs.put("$NODE_IMAGE_CASES$", imagesSB.toString());
         subs.put("$EXTRA_IMAGE_DECLS$", declsSB.toString());
 
         String javaSource= "";
 
         javaSource= WizardUtilities.createFileContentsFromTemplate("labelProvider.java", PSPActivator.kPluginID, subs, mon);
         createFileWithText(javaSource, fSrcProject, srcFolderPath.toPortableString(), pkgName, lpClassName, mon);
 
         javaSource= WizardUtilities.createFileContentsFromTemplate("labelImages.java", PSPActivator.kPluginID, subs, mon);
         createFileWithText(javaSource, fSrcProject, srcFolderPath.toPortableString(), pkgName, liClassName, mon);
 
         javaSource= WizardUtilities.createFileContentsFromTemplate("resources.java", PSPActivator.kPluginID, subs, mon);
         createFileWithText(javaSource, fSrcProject, srcFolderPath.toPortableString(), pkgName, resClassName, mon);
     }
 
     private void collectPresentationInfo(presentationMemberList presMembers, Map<String, String> resNameToIconPath, Map<String, String> imageCases,
             Map<String, Pair> labelCases) {
         for(int i=0; i < presMembers.size(); i++) {
             IpresentationMember presMember= presMembers.getpresentationMemberAt(i);
             if (presMember instanceof resourceDecl) {
                 resourceDecl rd= (resourceDecl) presMember;
                 
                 String resType= rd.getprimitiveType().toString();
                 String resName= rd.getident().toString();
                 if (resType.equals(KW_ICON)) {
                     String iconPath= stripQuotes(exprToString(rd.getinitializer()));
                     resNameToIconPath.put(resName, iconPath);
                     if (fProject.findMember(iconPath) == null) {
                         createErrorMarker(rd, "Icon '" + iconPath + "' does not exist in project " + fProject.getName() + ".");
                     }
                 } else {
                     createErrorMarker(rd, "Invalid resource type for coloring spec: " + resType);
                 }
             } else if (presMember instanceof nodePresentation) {
                 nodePresentation nodePres= (nodePresentation) presMember;
                 String nodeType= nodePres.gettype().toString();
                 String nodeName= nodePres.getname().toString();
                 nodePresentationAttributeList nodeAttrs= nodePres.getattrs();
 
                 for(int j=0; j < nodeAttrs.size(); j++) {
                     InodePresentationAttribute nodePresAttr= nodeAttrs.getnodePresentationAttributeAt(j);
                     if (nodePresAttr instanceof labelPresentation) {
                         labelPresentation labelPres= (labelPresentation) nodePresAttr;
                         String labelExpr= labelPres.getexpression().toString();
 
                         if (labelExpr.startsWith("{:")) {
                             labelExpr= labelExpr.substring(2);
                         }
                         if (labelExpr.endsWith(":}")) {
                             labelExpr= labelExpr.substring(0, labelExpr.length() - 2);
                         }
                         labelCases.put(nodeType, new Pair<String,String>(nodeName, labelExpr));
                     } else if (nodePresAttr instanceof iconPresentation) {
                         iconPresentation iconPres= (iconPresentation) nodePresAttr;
 
                         imageCases.put(nodeType, iconPres.getexpression().toString());
                     }
                 }
             }
         }
     }
 
     private Map<String, String> getManifestProps() {
         if (fManifestMap == null) {
             IFile manifestFile= fProject.getFile("META-INF/MANIFEST.MF");
             try {
                 String manifest= StreamUtils.readStreamContents(manifestFile.getContents());
                 manifest= manifest.replaceAll(",\\n", ",");
                 List<String> lines= Arrays.asList(manifest.split("\\n"));
                 Map<String,String> manifestMap= new HashMap<String, String>();
                 for(String line: lines) {
                     int colonIdx= line.indexOf(':');
                     String key= line.substring(0, colonIdx);
                     String value= line.substring(colonIdx+1).trim();
     
                     manifestMap.put(key, value);
                 }
                 fManifestMap= manifestMap;
             } catch (CoreException e) {
                 PSPActivator.getInstance().logException("Unable to read plugin project's bundle manifest", e);
                 fManifestMap= Collections.emptyMap();
             }
         }
         return fManifestMap;
     }
 
     private String determineBundleID() {
         String symName= getManifestProps().get("Bundle-SymbolicName");
 
         return (symName.indexOf(';') > 0) ? symName.substring(0, symName.indexOf(';')) : symName;
     }
 
     private String determineBundleActivator() {
         return getManifestProps().get("Bundle-Activator");
     }
 
     private static final Map<String,String> STYLEMAP= new HashMap<String, String>();
 
     static {
         STYLEMAP.put("bold", "SWT.BOLD");
         STYLEMAP.put("italic", "SWT.ITALIC");
         STYLEMAP.put("regular", "SWT.NORMAL");
         STYLEMAP.put("underline", "SWT.ITALIC");
     }
 
     private String styleNamesToSWTStyle(Set<String> styleNames) {
         StringBuilder sb= new StringBuilder();
         int i=0;
         for(String style: styleNames) {
             if (i++ > 0) { sb.append(" | "); }
             sb.append(STYLEMAP.get(style));
         }
         return sb.toString();
     }
 
     private void createTokenColorer(compilationUnit astRoot, IPath srcFolderPath, String basePkgName, IProgressMonitor mon) {
         if (fColoringSpec == null)
             return;
         Map<String,String> subs= defineStandardSubstitutions(astRoot);
         String pkgName= basePkgName.concat(".tokenColorer");
         String className= fLanguage.getName().concat("TokenColorer");
 
         Map<String,Map> tokenAttrs= new HashMap<String,Map>();
         Map<String,String> resNameToFontName= new HashMap<String, String>();
 
         collectTokenAttrs(tokenAttrs, resNameToFontName);
 
         subs.put("$PACKAGE_NAME$", pkgName);
         subs.put("$COLORER_CLASS_NAME$", className);
 
         StringBuilder attrFields= new StringBuilder();
         StringBuilder attrInits= new StringBuilder();
         StringBuilder attrCases= new StringBuilder();
         int i=0;
         for(String tokenKind: tokenAttrs.keySet()) {
             String attrField= tokenKind + "Attribute";
             if (i++ > 0) { attrFields.append(", "); }
             attrFields.append(attrField);
             Map<String,Object> attrs= tokenAttrs.get(tokenKind);
             String tokenFont= (String) attrs.get(KW_FONT); // unsupported
             String tokenColor= (String) attrs.get(KW_COLOR);
             Set<String> tokenStyle= (Set<String>) attrs.get(KW_STYLE);
 
             attrInits.append(attrField + " = new TextAttribute(display.getSystemColor(SWT.COLOR_" + stripQuotes(tokenColor) + "), null, " + styleNamesToSWTStyle(tokenStyle) + ");\n");
             attrCases.append("case TK_" + tokenKind + ":\n" + "return " + attrField + ";\n");
         }
         subs.put("$ATTRIBUTE_FIELD_NAMES$", attrFields.toString());
         subs.put("$ATTRIBUTE_INITIALIZERS$", attrInits.toString());
         subs.put("$TOKEN_CASES$", attrCases.toString());
 
         String javaSource= WizardUtilities.createFileContentsFromTemplate("tokenColorer.java", PSPActivator.kPluginID, subs, mon);
 
         createFileWithText(javaSource, fSrcProject, srcFolderPath.toPortableString(), pkgName, className, mon);
     }
 
     private String stripQuotes(String s) {
         int from= 0, to= s.length()-1;
         if (s.charAt(from) == '"') { from++; }
         if (s.charAt(to) == '"') { to--; }
         return s.substring(from, to - from + 2);
     }
 
     private void collectTokenAttrs(Map<String/*tokenKind*/, Map> tokenAttrs, Map<String, String> resNameToFontName) {
         textColoringMemberList coloringMembers= fColoringSpec.gettextColoringMembers();
         for(int i=0; i < coloringMembers.size(); i++) {
             ItextColoringMember member= coloringMembers.gettextColoringMemberAt(i);
             if (member instanceof resourceDecl) {
                 resourceDecl rd= (resourceDecl) member;
 
                 String resType= rd.getprimitiveType().toString();
                 String resName= rd.getident().toString();
                 if (resType.equals(KW_FONT)) {
                     String fontName= exprToString(rd.getinitializer());
                     resNameToFontName.put(resName, fontName);
                 } else {
                     createErrorMarker(rd, "Invalid resource type for coloring spec: " + resType);
                 }
             } else if (member instanceof tokenDecl) {
                 tokenDecl td= (tokenDecl) member;
                 String tokenName= td.getname().toString();
                 textAttributeDeclList attrs= td.getattrs();
                 Map<String,Object> attrMap= new HashMap<String, Object>();
 
                 for(int j=0; j < attrs.size(); j++) {
                     ItextAttributeDecl attr= attrs.gettextAttributeDeclAt(j);
                     if (attr instanceof colorAttributeDecl) {
                         colorAttributeDecl cad= (colorAttributeDecl) attr;
                         attrMap.put(KW_COLOR, cad.getresourceValue().toString());
                     } else if (attr instanceof fontAttributeDecl) {
                         fontAttributeDecl fad= (fontAttributeDecl) attr;
                         attrMap.put(KW_FONT, fad.getresourceValue().toString());
                     } else if (attr instanceof styleAttributesDecl) {
                         styleAttributesDecl sad= (styleAttributesDecl) attr;
                         styleKeyList styleList= sad.getstyleSet().getstyleList();
                         Set<String> styleSet= new HashSet<String>();
                         for(int s=0; s < styleList.size(); s++) {
                             styleSet.add(styleList.getstyleKeyAt(s).toString());
                         }
                         attrMap.put(KW_STYLE, styleSet);
                     }
                 }
                 tokenAttrs.put(tokenName, attrMap);
             }
         }
     }
 
     private String exprToString(Iexpression expr) {
         if (expr instanceof expression0) {
             expression0 e= (expression0) expr;
             return e.getIDENTIFIER().toString();
         } else if (expr instanceof expression1) {
             expression1 e= (expression1) expr;
             return e.getJAVA_EXPR().toString();
         } else if (expr instanceof literal0) {
             literal0 lit= (literal0) expr;
             return lit.getSTRING_LITERAL().toString();
         } else if (expr instanceof literal1) {
             literal1 lit= (literal1) expr;
             return lit.getINTEGER().toString();
         }
         return "";
     }
 
     private static final String sModelBuilderVisitorMethodTemplate=
         "        @Override\n" +
         "        public boolean visit($NODE_TYPE$ n) {\n" + 
         "            pushSubItem(n);\n" + 
         "            return true;\n" + 
         "        }\n" + 
         "\n" + 
         "        @Override\n" +
         "        public void endVisit($NODE_TYPE$ n) {\n" + 
         "            popSubItem();\n" + 
         "        }\n";
 
     private void createModelBuilder(compilationUnit astRoot, IPath srcFolderPath, String basePkgName, IProgressMonitor mon) {
         if (fOutlineSpec == null)
             return;
         String pkgName= basePkgName.concat(".treeModelBuilder");
         String className= fLanguage.getName().concat("ModelBuilder");
         Map<String,String> subs= defineStandardSubstitutions(astRoot);
 
         StringBuilder sb= new StringBuilder();
         outlineSpecMemberList outlineNodes= fOutlineSpec.getmembers();
         for(int i=0; i < outlineNodes.size(); i++) {
             nodeSpec node= outlineNodes.getoutlineSpecMemberAt(i);
             sb.append(sModelBuilderVisitorMethodTemplate.replace("$NODE_TYPE$", node.getid().toString()));
         }
 
         subs.put("$PACKAGE_NAME$", pkgName);
         subs.put("$TREE_MODEL_BUILDER_CLASS_NAME$", className);
         subs.put("$VISITOR_METHODS$", sb.toString());
 
         String javaSource= WizardUtilities.createFileContentsFromTemplate("treeModelBuilder.java", PSPActivator.kPluginID, subs, mon);
 
         createFileWithText(javaSource, fSrcProject, srcFolderPath.toPortableString(), pkgName, className, mon);
     }
 
     private static final String sFolderVisitorMethodTemplate=
         "       @Override\n" +
         "       public boolean visit($NODE_TYPE$ n) {\n" + 
         "           makeFoldable(n);\n" + 
         "           return true;\n" + 
         "       }\n";
 
     private IPluginModel fPluginModel;
 
     private String fParseCtlrPkg;
 
     private String fParseCtlrName;
 
     private void createFoldingUpdater(compilationUnit astRoot, IPath srcFolderPath, String basePkgName, IProgressMonitor mon) {
         if (fFoldingSpec == null)
             return;
         String pkgName= basePkgName.concat(".foldingUpdater");
         String className= fLanguage.getName().concat("FoldingUpdater");
         Map<String,String> subs= defineStandardSubstitutions(astRoot);
 
         StringBuilder sb= new StringBuilder();
         foldableNodeList foldableNodes= fFoldingSpec.getfoldableNodes();
         for(int i=0; i < foldableNodes.size(); i++) {
             foldableNode node= foldableNodes.getfoldableNodeAt(i);
             sb.append(sFolderVisitorMethodTemplate.replace("$NODE_TYPE$", node.getnodeName().toString()));
         }
 
         subs.put("$PACKAGE_NAME$", pkgName);
         subs.put("$FOLDER_CLASS_NAME$", className);
         subs.put("$VISITOR_METHODS$", sb.toString());
 
         String javaSource= WizardUtilities.createFileContentsFromTemplate("foldingUpdater.java", PSPActivator.kPluginID, subs, mon);
 
         createFileWithText(javaSource, fSrcProject, srcFolderPath.toPortableString(), pkgName, className, mon);
     }
 
     private Map<String,String> defineStandardSubstitutions(compilationUnit astRoot) {
         Map<String,String> subs= new HashMap<String, String>();
         String langName= fLanguage.getName();
 
         subs.put("$PLUGIN_ID$", determineBundleID());
         subs.put("$EXTRA_IMPORTS$", buildImports(astRoot));
         subs.put("$LANG_NAME$", langName);
         subs.put("$SPEC_NAME$", fSpecFile.getName());
         subs.put("$DATE_TIME$", SimpleDateFormat.getDateTimeInstance().format(new Date()));
 
         determineParserProps(subs, langName);
 
         return subs;
     }
 
     private void determineParserProps(Map<String,String> subs, String langName) {
         if (fParseCtlrPkg == null) {
             IExtensions pmExtensions= fPluginModel.getExtensions();
             IPluginExtension[] pluginExtensions = pmExtensions.getExtensions();
     
             for (int i = 0; i < pluginExtensions.length; i++) {
                 IPluginExtension pluginExtension = pluginExtensions[i];
     
                 if (pluginExtension == null) continue;
                 if (pluginExtension.getPoint() == null) continue;
     
                 if (pluginExtension.getPoint().equals(RuntimePlugin.IMP_RUNTIME + ".parser")) { // TODO would use global exten pt ID, but def in ServiceFactory not public
                     IPluginElement parseCtrlrExten= (IPluginElement) pluginExtension.getChildren()[0];
                     IPluginAttribute attr= parseCtrlrExten.getAttribute("class");
                     String ctrlrQualClass= attr.getValue();
                     int pkgEnd= ctrlrQualClass.lastIndexOf('.');
                     fParseCtlrPkg= ctrlrQualClass.substring(0, pkgEnd);
                     fParseCtlrName= ctrlrQualClass.substring(pkgEnd+1);
                     break;
                 }
             }
             if (fParseCtlrPkg == null)
                 return;
         }
         subs.put("$PARSER_PKG$", fParseCtlrPkg);
         subs.put("$PARSE_CONTROLLER_CLASS_NAME$", fParseCtlrName);
 
         IFile grammarFile= findSourceFile(fParseCtlrPkg, ".*\\.g$");
         Map<String,String> grammarOptions= readOptionsFromGrammar(grammarFile);
         String grammarFileName= grammarFile.getName();
         String filePrefix= grammarOptions.containsKey("fp") ? grammarOptions.get("fp") : (grammarFileName.substring(0, grammarFileName.length() - grammarFile.getFileExtension().length() - 1));
 
         subs.put("$PARSER_SYMBOL_CLASS_NAME$", filePrefix + "sym");
         subs.put("$AST_NODE$", grammarOptions.containsKey("ast_type") ? grammarOptions.get("ast_type") : "ASTNode");
     }
 
     private Map<String, String> readOptionsFromGrammar(IFile grammarFile) {
         Map<String,String> result= new HashMap<String, String>();
         try {
             String contents= StreamUtils.readStreamContents(grammarFile.getContents());
             String[] lines= contents.split(System.getProperty("line.separator"));
             for(int i= 0; i < lines.length; i++) {
                 String line= lines[i];
                 if (line.trim().startsWith("%options ")) {
                     String[] options= line.substring(9).split(",");
                     for(int j= 0; j < options.length; j++) {
                         String option= options[j];
                         String name, value;
                         int eqIdx= option.indexOf('=');
                         if (eqIdx > 0) {
                             name= option.substring(0, eqIdx);
                             value= option.substring(eqIdx+1);
                         } else {
                             name= option;
                             value= "";
                         }
                         result.put(name, value);
                     }
                 }
             }
         } catch (CoreException e) {
             e.printStackTrace();
         }
         return result;
     }
 
     private IFile findSourceFile(String parseCtlrPkg, String regex) {
         final IFile[] result= new IFile[1];
         try {
             IJavaProject javaProject= JavaCore.create(fProject);
             IClasspathEntry[] cpEntries= javaProject.getResolvedClasspath(true);
             final Pattern pat= Pattern.compile(regex);
             IPath parseCtlrFolder= new Path(parseCtlrPkg.replace('.', File.separatorChar));
             IWorkspace ws= fProject.getWorkspace();
             for(int i= 0; i < cpEntries.length; i++) {
                 IClasspathEntry entry= cpEntries[i];
                 if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
                     IPath entryPath= entry.getPath();
                     IResource pkgMember= ws.getRoot().findMember(entryPath.append(parseCtlrFolder));
                     if (pkgMember != null && pkgMember.exists()) {
                         pkgMember.accept(new IResourceProxyVisitor() {
                             public boolean visit(IResourceProxy proxy) throws CoreException {
                                 if (proxy.getType() == IResource.FILE &&  pat.matcher(proxy.getName()).matches()) {
                                     result[0]= (IFile) proxy.requestResource();
                                 }
                                 return true;
                             }
                         },
                         0);
                     }
                 }
             }
         } catch (CoreException e) {
             // ...
         }
         return result[0];
     }
 
     private String buildImports(compilationUnit astRoot) {
         importSpecList importSpecs= astRoot.getimportSpecs();
         StringBuilder sb= new StringBuilder();
         for(int i=0; i < importSpecs.size(); i++) {
             sb.append(importSpecs.getimportSpecAt(i));
         }
         return sb.toString();
     }
 
     /**
      * @param project
      * @param srcFile
      * @return the source folder on the given project's classpath that contains the given source file
      */
     private IFolder getProjectSourceFolder(IProject project, IFile srcFile) {
         try {
             if (project == null)
                 return null;
 
             IJavaProject javaProj= JavaCore.create(project);
 
             if (javaProj == null)
                 return null;
 
             IClasspathEntry[] cpEntries= javaProj.getResolvedClasspath(true);
 
             for(IClasspathEntry entry: cpEntries) {
                 if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
                     IPath entryPath= entry.getPath();
 
                     if (entryPath.matchingFirstSegments(srcFile.getFullPath()) > 0) {
                         return project.getWorkspace().getRoot().getFolder(entryPath);
                     }
                 }
             }
         } catch (JavaModelException e) {
         }
         return null;
     }
 
     private void collectCodeParams(compilationUnit unit) {
         fPackageName= unit.getpackageSpec().getqualifiedName().toString();
         fImports= new ArrayList<String>();
         importSpecList imports= unit.getimportSpecs();
         for(int i=0; i < imports.size(); i++) {
             fImports.add(imports.getimportSpecAt(i).getqualifiedName().toString());
         }
     }
 
     protected static IFile createFileWithText(String srcText, ISourceProject srcProject, String projectSourceLoc,
             String packageName, String className, IProgressMonitor mon)
     {
         // Find or create the folder to contain the file
         IFolder packageFolder= createFolderIfNeeded(packageName, projectSourceLoc, srcProject, mon);
         
         // Find or create the file to contain the text, and put the text into it
         String formattedSource= formatJavaCode(srcText);
         String fileName= className;
 
         if (!fileName.endsWith(".java"))
             fileName += ".java";
 
         IFile file= packageFolder.getFile(fileName);
 
         try {
             ByteArrayInputStream inpStream= new ByteArrayInputStream(formattedSource.getBytes());
 
             if (file.exists()) {
                 file.setContents(inpStream, true, true, mon);
             } else {
                 file.create(inpStream, true, mon);
             }
         } catch (CoreException e) {
             PSPActivator.getInstance().logException("PSPCompiler.createFileWithText(): CoreException creating file; returning null", e);
             return null;
         }
         
         return file;
     }
 
     private static IFolder createFolderIfNeeded(String pkgName, String projectSrcLoc, ISourceProject srcProject, IProgressMonitor mon) {
         IFolder pkgFolder = null;
         String pkgFolderName = pkgName.replace(".", "/");
         String createdPath = null;
         String[] pathSegs = (projectSrcLoc + "/" + pkgFolderName).split("/");
 
         for (int i = 0; i < pathSegs.length; i++) {
             if (createdPath == null)
                 createdPath = pathSegs[i];
             else
                 createdPath = createdPath + "/" + pathSegs[i];
             pkgFolder = srcProject.getRawProject().getFolder(createdPath);
             try {
                 if (!pkgFolder.exists()) {
                     pkgFolder.create(true, true, mon);
                     if (!pkgFolder.exists()) {
                         PSPActivator.getInstance().writeErrorMsg("PSPCompiler.createFileWithText(): cannot find or create package folder '" +
                                 pkgFolder.getLocation().toString() + "'");
                         return null;
                     }
                 }
             } catch (CoreException e) {
                 PSPActivator.getInstance().logException("PSPCompiler.createFileWithText(): CoreException finding or creating package folder '" +
                         pkgFolder.getLocation().toString() + "'", e);
                 return null;
             }
         }
         return pkgFolder;
     }
 
     private compilationUnit parseSpecification(IFile file, IProgressMonitor mon) {
         fProject= file.getProject();
         if (fProject == null) {
             System.err.println("PSPCompiler.compile(..):  Project is null; returning without parsing");
             return null;
         }
         try {
             fSrcProject= ModelFactory.open(fProject);
         } catch (ModelException me) {
             PSPActivator.getInstance().logException("PSPCompiler.compile(..): Model exception opening project: " + me.getMessage() + "; returning without parsing", me);
             return null;
         }
         IParseController parseController= new PSPParseController();
 
         // Marker creator handles error messages from the parse controller
         MarkerCreator markerCreator= new MarkerCreator(file, parseController, PROBLEM_MARKER_ID);
 
         // If we have a kind of parser that might be receptive, tell it
         // what types of problem marker the builder will create
         parseController.getAnnotationTypeInfo().addProblemMarkerType(PROBLEM_MARKER_ID);
 
         parseController.initialize(file.getProjectRelativePath(), fSrcProject, markerCreator);
 
         parseController.parse(getFileContents(file), mon);
 
         compilationUnit astRoot= (compilationUnit) parseController.getCurrentAst();
 
         return astRoot;
     }
 
     private void createExtensions(IProject project, compilationUnit astRoot, IProgressMonitor mon) {
        fPluginModel= ExtensionEnabler.getPluginModel(project);
 
         if (fPluginModel == null) {
             PSPActivator.getInstance().writeErrorMsg("Error locating plugin model for project '" + fProject.getName() + "'");
             return;
         }
 
         // SMS 30 Jul 2008
         if (fPluginModel instanceof BundlePluginModel) {
             BundlePluginModel bpm = (BundlePluginModel) fPluginModel;
             IBundleModel bm = bpm.getBundleModel();
             if (bm instanceof WorkspaceBundleModel) {
                 ((WorkspaceBundleModel)bm).setEditable(true);
             }
         }
 
         try {
             // Load the IMP way to get the complete model
            ExtensionEnabler.loadImpExtensionsModel(fPluginModel, project);
 
             if (fPresentationSpec != null) {
                 removeOldExtensionsFor(fPluginModel, "org.eclipse.imp.labelProvider");
             }
             if (fOutlineSpec != null) {
                 removeOldExtensionsFor(fPluginModel, "org.eclipse.imp.modelTreeBuilder");
             }
             if (fFoldingSpec != null) {
                 removeOldExtensionsFor(fPluginModel, "org.eclipse.imp.foldingUpdater");
             }
             if (fColoringSpec != null) {
                 removeOldExtensionsFor(fPluginModel, "org.eclipse.imp.tokenColorer");
             }
 
            ExtensionEnabler.saveAndRefresh(fPluginModel);
         } catch (CoreException e) {
             PSPActivator.getInstance().logException("Error adding extensions to project '" + fProject.getName() + "'", e);
         }
 
         if (fPresentationSpec != null) {
             createLabelExtension(project, mon);
         }
         if (fOutlineSpec != null) {
             createOutlineExtension(project, mon);
         }
         if (fFoldingSpec != null) {
             createFoldingExtension(project, mon);
         }
         if (fColoringSpec != null) {
             createColorerExtension(project, mon);
         }
     }
 
     private void createLabelExtension(IProject project, IProgressMonitor mon) {
        ExtensionEnabler.enable(
                 project, "org.eclipse.imp.runtime", "labelProvider",
                 new String[][] {
                         { "labelProvider:id", "labelProvider" },
                         { "labelProvider:name", fLanguage.getName() + " Label Provider" },
                         { "labelProvider:class", fPackageName + "." + fLanguage.getName() + "LabelProvider" },
                         { "labelProvider:language", fLanguage.getName() },
                 },
                 true, // replace any existing extension (just to be sure; explicitly deleted above)
                 getPluginDependencies(),
                 mon);
     }
 
     private void createColorerExtension(IProject project, IProgressMonitor mon) {
        ExtensionEnabler.enable(
                 project, "org.eclipse.imp.runtime", "tokenColorer",
                 new String[][] {
                         { "tokenColorer:id", "tokenColorer" },
                         { "tokenColorer:name", fLanguage.getName() + " Token Colorer" },
                         { "tokenColorer:class", fPackageName + "." + fLanguage.getName() + "TokenColorer" },
                         { "tokenColorer:language", fLanguage.getName() },
                 },
                 true, // replace any existing extension (just to be sure; explicitly deleted above)
                 getPluginDependencies(),
                 mon);
     }
 
     private void createOutlineExtension(IProject project, IProgressMonitor mon) {
        ExtensionEnabler.enable(
                 project, "org.eclipse.imp.runtime", "modelTreeBuilder",
                 new String[][] {
                         { "treeBuilder:id", "treeBuilder" },
                         { "treeBuilder:name", fLanguage.getName() + " Tree Model Builder" },
                         { "treeBuilder:class", fPackageName + "." + fLanguage.getName() + "TreeModelBuilder" },
                         { "treeBuilder:language", fLanguage.getName() },
                 },
                 true, // replace any existing extension (just to be sure; explicitly deleted above)
                 getPluginDependencies(),
                 mon);
     }
 
     private void createFoldingExtension(IProject project, IProgressMonitor mon) {
        ExtensionEnabler.enable(
                 project, "org.eclipse.imp.runtime", "foldingUpdater",
                 new String[][] {
                         { "foldingUpdater:id", "foldingUpdater" },
                         { "foldingUpdater:name", fLanguage.getName() + " Folding Updater" },
                         { "foldingUpdater:class", fPackageName + "." + fLanguage.getName() + "FoldingUpdater" },
                         { "foldingUpdater:language", fLanguage.getName() },
                 },
                 true, // replace any existing extension (just to be sure; explicitly deleted above)
                 getPluginDependencies(),
                 mon);
     }
 
     private void removeOldExtensionsFor(IPluginModel pluginModel, String extensionPointID) throws CoreException {
         IExtensions pmExtensions = pluginModel.getExtensions();
         IPluginExtension[] pluginExtensions = pmExtensions.getExtensions();
 
         for (int i = 0; i < pluginExtensions.length; i++) {
             IPluginExtension pluginExtension = pluginExtensions[i];
 
             if (pluginExtension == null) continue;
             if (pluginExtension.getPoint() == null) continue;
 
             if (pluginExtension.getPoint().equals(extensionPointID)) {
                 pmExtensions.remove(pluginExtension);
             }
         }
     }
 
     protected List<String> getPluginDependencies() {
         return Arrays.asList(new String[] { "org.eclipse.core.runtime", "org.eclipse.core.resources",
             "org.eclipse.imp.runtime" });
     }
 }
