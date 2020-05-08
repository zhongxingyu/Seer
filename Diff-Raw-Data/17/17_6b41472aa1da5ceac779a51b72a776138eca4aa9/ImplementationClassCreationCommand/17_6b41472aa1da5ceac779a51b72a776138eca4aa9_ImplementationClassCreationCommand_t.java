 package org.jboss.tools.ws.creation.core.commands;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jdt.core.ICompilationUnit;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.IPackageFragment;
 import org.eclipse.jdt.core.IPackageFragmentRoot;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jdt.core.dom.AST;
 import org.eclipse.jdt.core.dom.ASTParser;
 import org.eclipse.jdt.core.dom.ArrayType;
 import org.eclipse.jdt.core.dom.Block;
 import org.eclipse.jdt.core.dom.BooleanLiteral;
 import org.eclipse.jdt.core.dom.CompilationUnit;
 import org.eclipse.jdt.core.dom.ImportDeclaration;
 import org.eclipse.jdt.core.dom.MemberValuePair;
 import org.eclipse.jdt.core.dom.MethodDeclaration;
 import org.eclipse.jdt.core.dom.Modifier;
 import org.eclipse.jdt.core.dom.NormalAnnotation;
 import org.eclipse.jdt.core.dom.NumberLiteral;
 import org.eclipse.jdt.core.dom.PackageDeclaration;
 import org.eclipse.jdt.core.dom.ParameterizedType;
 import org.eclipse.jdt.core.dom.PrimitiveType;
 import org.eclipse.jdt.core.dom.QualifiedName;
 import org.eclipse.jdt.core.dom.ReturnStatement;
 import org.eclipse.jdt.core.dom.SimpleType;
 import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
 import org.eclipse.jdt.core.dom.StringLiteral;
 import org.eclipse.jdt.core.dom.Type;
 import org.eclipse.jdt.core.dom.TypeDeclaration;
 import org.eclipse.jdt.core.dom.WildcardType;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.Document;
 import org.eclipse.text.edits.TextEdit;
 import org.eclipse.wst.common.frameworks.datamodel.AbstractDataModelOperation;
 import org.jboss.tools.ws.core.utils.StatusUtils;
 import org.jboss.tools.ws.creation.core.JBossWSCreationCore;
 import org.jboss.tools.ws.creation.core.data.ServiceModel;
 import org.jboss.tools.ws.creation.core.messages.JBossWSCreationCoreMessages;
 import org.jboss.tools.ws.creation.core.utils.JBossWSCreationUtils;
 
 public class ImplementationClassCreationCommand extends
 		AbstractDataModelOperation {
 
 	private static final String RESOURCE_FOLDER = "src";
 
 	private static final String PREFIX_JAXWS_ANNOTATION_CLASS = "javax.jws";
 	private static final String DEFAULT_CU_SUFFIX = ".java";
 
 	private static final String ANNOTATION_WEB_SERVICE_FULLNAME = "javax.jws.WebService";
 	private static final String ANNOTATION_TYPE_NAME_WEBSERVICE = "WebService";;
 	private static final String ANNOTATION_PROPERTY_NAME = "name";
 	private static final String ANNOTATION_PROPERTY_SERVICE_NAME = "serviceName";
 	private static final String ANNOTATION_PROPERTY_ENDPOINT_INTERFACE = "endpointInterface";
 	
 
 	private ServiceModel model;
 	private IWorkspaceRoot fWorkspaceRoot;
 
 	public ImplementationClassCreationCommand(ServiceModel model) {
 		this.model = model;
 		fWorkspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
 
 	}
 
 	@Override
 	public IStatus execute(IProgressMonitor monitor, IAdaptable info)
 			throws ExecutionException {
 		
 		// if the user does not check the generate implementation class button, do nothing
 		if(!model.isGenImplementation()){
 			return Status.OK_STATUS;
 		}
 		
 		IStatus status = Status.OK_STATUS;
 		
 		try {
 			List<String> portTypes = model.getPortTypes();
 			for (String portTypeName : portTypes) {
 				generateImplClass(portTypeName);
 				String implClsName = getImplPackageName() + "."
 						+ getImplClassName(portTypeName);
 				model.addServiceClasses(implClsName);
 			}
 
 		} catch (CoreException e) {
 			status = StatusUtils
 					.errorStatus(
 							JBossWSCreationCoreMessages.Error_Message_Failed_to_Generate_Implementation,
 							e);
 			JBossWSCreationCore.getDefault().logError(e);
 		} catch (BadLocationException e) {
 			status = StatusUtils
 					.errorStatus(
 							JBossWSCreationCoreMessages.Error_Message_Failed_to_Generate_Implementation,
 							e);
 			JBossWSCreationCore.getDefault().logError(e);
 		}
 		return status;
 	}
 
 	protected void generateImplClass(String portTypeName/* , IFile implJavaFile */)
 			throws CoreException, BadLocationException {
 
 		CompilationUnit portTypeCU = getCompilationUnitForInterface(portTypeName);
 		List<ImportDeclaration> imports = getImportsWithoutJaxwsAnnotation(portTypeCU);
 
 		IPackageFragment pack = getImplPakcage();
 
 		String implFileName = getJavaFileName(portTypeName);
 		ICompilationUnit icu = pack.createCompilationUnit(implFileName,
 				"", true, null); //$NON-NLS-1$
 		// create a working copy with a new owner
 
 		icu.becomeWorkingCopy(null);
 
 		ASTParser parser = ASTParser.newParser(AST.JLS3);
 		parser.setSource(icu);
 		parser.setResolveBindings(false);
 		parser.setFocalPosition(0);
 
 		CompilationUnit implCu = (CompilationUnit) parser.createAST(null);
 		AST ast = implCu.getAST();
 
 		// creation of a Document and ASTRewrite
 		String source = icu.getBuffer().getContents();
 		Document document = new Document(source);
 
 		implCu.recordModifications();
 
 		// start to add content into implementation class
 
 		// add package declaration for impl class:
 		PackageDeclaration implPackage = ast.newPackageDeclaration();
 		implPackage.setName(ast.newName(pack.getElementName()));
 		implCu.setPackage(implPackage);
 
 		// add imports for implementation class
 		addImportsToImplementationClass(implCu, portTypeCU, portTypeName);
 
 		// add class declaration
 		TypeDeclaration type = ast.newTypeDeclaration();
 		type.setInterface(false);
 		// add WebService annotation
 		String endpoint = getPortTypeInterfaceFullName(portTypeName);
 		NormalAnnotation ann = createAnnotation(ast,
 				getImplClassName(portTypeName), portTypeName, endpoint);
 		type.modifiers().add(ann);
 		type.modifiers().add(
 				ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD));
 		type.setName(ast.newSimpleName(getImplClassName(portTypeName)));
 		type.superInterfaceTypes().add(
 				ast.newSimpleType(ast.newName(portTypeName)));
 
 		// add Logger variable declaration
 		//createLoggerField(ast, type, portTypeName);
 
 		// add method implementation
 		TypeDeclaration inTD = (TypeDeclaration) portTypeCU.types().get(0);
 		// firstly, get all methods that declared in Interface class and then
 		// add corresponding methods to
 		// the impl class
 		MethodDeclaration[] methods = inTD.getMethods();
 		for (int i = 0; i < methods.length; i++) {
 			MethodDeclaration newMethod = createMethodForImplClass(ast,
 					methods[i]);
 			type.bodyDeclarations().add(newMethod);
 		}
 
 		implCu.types().add(type);
 		
 
 		// try to save the Java file
 		TextEdit edits = implCu.rewrite(document, icu.getJavaProject()
 				.getOptions(true));
 		edits.apply(document);
 		String newSource = document.get();
 		icu.getBuffer().setContents(newSource);
 		icu.reconcile(ICompilationUnit.NO_AST, false, null, null);
 
 		icu.commitWorkingCopy(true, null);
 		icu.discardWorkingCopy();
 
 	}
 
 	private String getImplPackageName() {
 		return model.getCustomPackage() /* + ".impl" */;
 	}
 
 	private IPackageFragmentRoot getPackageFragmentRoot() {
 		String str = model.getWebProjectName() + File.separator
 				+ RESOURCE_FOLDER;
 		IPath path = new Path(str);
 		IResource res = fWorkspaceRoot.findMember(path);
 		IProject prj = res.getProject();
 		IJavaProject javaPrj = JavaCore.create(prj);
 		return javaPrj.getPackageFragmentRoot(res);
 
 	}
 
 	private String getJavaFileName(String portTypeName) {
 
 		return getImplClassName(portTypeName) + DEFAULT_CU_SUFFIX;
 	}
 
 	private String getImplClassName(String portTypeName) {
 		String firstLetter = portTypeName.substring(0, 1);
 		String implClsName = firstLetter.toUpperCase()
 				+ portTypeName.substring(1);
 		implClsName = implClsName + "Impl";
 		return implClsName;
 	}
 
 	private IPackageFragment getImplPakcage() throws JavaModelException {
 		IPackageFragmentRoot root = getPackageFragmentRoot();
 		String implPackageName = getImplPackageName();
 		IPackageFragment pack = root.getPackageFragment(implPackageName);
 
 		if (!pack.exists()) {
 			String packName = pack.getElementName();
 			pack = root.createPackageFragment(packName, true, null);
 		}
 
 		return pack;
 	}
 
 	private String getPortTypeInterfaceFullName(String portTypeName) {
 		return model.getCustomPackage() + "." + portTypeName;
 	}
 
 	private void addImportsToImplementationClass(CompilationUnit implCU,
 			CompilationUnit portTypeCU, String portTypeName) {
 		List<ImportDeclaration> imports = getImportsWithoutJaxwsAnnotation(portTypeCU);
 		AST implAST = implCU.getAST();
 
 		// add imports for implementation class
 		for (ImportDeclaration id : imports) {
 			ImportDeclaration newId = implAST.newImportDeclaration();
 			newId
 					.setName(implAST.newName(id.getName()
 							.getFullyQualifiedName()));
 			implCU.imports().add(newId);
 		}
 
 		// import port type interface 
 		ImportDeclaration importDec = implAST.newImportDeclaration();
 		QualifiedName portTypeImport = implAST.newQualifiedName(implAST
 				.newName(portTypeCU.getPackage().getName()
 						.getFullyQualifiedName()), implAST
 				.newSimpleName(portTypeName));
 		importDec.setName(portTypeImport);
 		implCU.imports().add(importDec);
 		//importDec = implAST.newImportDeclaration();
 		//importDec.setName(implAST.newName(LOGGER_CLASS_FULLNAME));
 		//implCU.imports().add(importDec);
 
 		// import jaxws WebService
 		importDec = implAST.newImportDeclaration();
 		// hardcode here?
 		importDec.setName(implAST.newName(ANNOTATION_WEB_SERVICE_FULLNAME));
 		implCU.imports().add(importDec);
 	}
 
 	/*
 	 * create web service annotation
 	 */
 	protected NormalAnnotation createAnnotation(AST ast, String name,
 			String serviceName, String endpoint) {
 		NormalAnnotation ann = ast.newNormalAnnotation();
 		ann.setTypeName(ast.newSimpleName(ANNOTATION_TYPE_NAME_WEBSERVICE));
 
 		MemberValuePair member = createMemberValuePair(ast,
 				ANNOTATION_PROPERTY_NAME, name);
 		ann.values().add(member);
 		member = createMemberValuePair(ast, ANNOTATION_PROPERTY_SERVICE_NAME,
 				serviceName);
 		ann.values().add(member);
 		member = createMemberValuePair(ast,
 				ANNOTATION_PROPERTY_ENDPOINT_INTERFACE, endpoint);
 		ann.values().add(member);
 		return ann;
 	}
 
 	private MemberValuePair createMemberValuePair(AST ast, String propertyName,
 			String Value) {
 		MemberValuePair member = ast.newMemberValuePair();
 		member.setName(ast.newSimpleName(propertyName));
 		StringLiteral value = ast.newStringLiteral();
 		value.setLiteralValue(Value);
 		member.setValue(value);
 
 		return member;
 	}
 
 	
 	protected MethodDeclaration createMethodForImplClass(AST ast,
 			MethodDeclaration inMethod) {
 
 		MethodDeclaration md = ast.newMethodDeclaration();
 		md.setConstructor(false);
 		List modifiers = md.modifiers();
 		modifiers.add(ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD));
 		md.setName(ast
 				.newSimpleName(inMethod.getName().getFullyQualifiedName()));
 
 		Type sType = copyTypeFromOtherASTNode(ast, inMethod.getReturnType2());
 		md.setReturnType2(sType);
 
 		List parameters = inMethod.parameters();
 		
 		for (Object obj : parameters) {
 			SingleVariableDeclaration implSvd = ast
 					.newSingleVariableDeclaration();
 			SingleVariableDeclaration svd = (SingleVariableDeclaration) obj;
 			implSvd.setName(ast.newSimpleName(svd.getName()
 					.getFullyQualifiedName()));
 			implSvd.setType(copyTypeFromOtherASTNode(ast, svd.getType()));
 			md.parameters().add(implSvd);
 		}
 
 		// create method body
 		Block block = ast.newBlock();
 		// add log info statement
 		//block.statements().add(createLoggerInvokeStatement(ast, md.getName().getFullyQualifiedName()));
 		
 		Type returnType = inMethod.getReturnType2();
 		ReturnStatement rs = ast.newReturnStatement();
 
 		if (returnType.isPrimitiveType()) {
 			if (((PrimitiveType) returnType).getPrimitiveTypeCode().equals(
 					PrimitiveType.BOOLEAN)) {
 				BooleanLiteral bl = ast.newBooleanLiteral(false);
 				rs.setExpression(bl);
 			}else if(((PrimitiveType) returnType).getPrimitiveTypeCode().equals(
 					PrimitiveType.VOID)) {
 				// do nothing
 			}else {
 				NumberLiteral nl = ast.newNumberLiteral();
 				nl.setToken("0");
 				rs.setExpression(nl);
 			}
 
 		} else if (returnType.isSimpleType()) {
 			String typeName = ((SimpleType) returnType).getName()
 					.getFullyQualifiedName();
 			if ("String".equals(typeName)) {
 
 				StringLiteral sl = ast.newStringLiteral();
 				sl.setLiteralValue("");
 				rs.setExpression(sl);
 			} else{
 				rs.setExpression(ast.newNullLiteral());
 			}
 				
 			
 		} else {
 			rs.setExpression(ast.newNullLiteral());
 		}
 
 		block.statements().add(rs);
 
 		md.setBody(block);
 
 		return md;
 	}
 
 	
 	private Type copyTypeFromOtherASTNode(AST ast, Type type) {
 		if (type instanceof PrimitiveType) {
 			return ast.newPrimitiveType(((PrimitiveType) type)
 					.getPrimitiveTypeCode());
 		} else if (type instanceof SimpleType) {
 			SimpleType simpleType = (SimpleType) type;
 
 			return ast.newSimpleType(ast.newName(simpleType.getName()
 					.getFullyQualifiedName()));
 		} else if (type instanceof ArrayType) {
 			ArrayType atype = (ArrayType) type;
 			return ast.newArrayType(copyTypeFromOtherASTNode(ast, atype
 					.getComponentType()));
 		} else if (type instanceof ParameterizedType) {
 			ParameterizedType ptype = (ParameterizedType) type;
			ParameterizedType newParaType = ast.newParameterizedType(copyTypeFromOtherASTNode(ast, ptype
 					.getType()));
			for(Object arg : ptype.typeArguments()){
				if(arg instanceof Type){
					Type newArg = copyTypeFromOtherASTNode(ast, (Type)arg);
					newParaType.typeArguments().add(newArg);
				}
			}
			return newParaType;
 		} else if (type instanceof WildcardType) {
 			WildcardType sourcetype = (WildcardType) type;
 			WildcardType wtype = ast.newWildcardType();
 			wtype.setBound(sourcetype.getBound());
 			return wtype;
 		}
 
 		return null;
 	}
 
 	protected List<ImportDeclaration> getImportsWithoutJaxwsAnnotation(
 			CompilationUnit cu) {
 		List<ImportDeclaration> importList = new ArrayList<ImportDeclaration>();
 		List imports = cu.imports();
 		for (Object obj : imports) {
 			ImportDeclaration id = (ImportDeclaration) obj;
 			String imClsName = id.getName().getFullyQualifiedName();
 			if (!imClsName.startsWith(PREFIX_JAXWS_ANNOTATION_CLASS)) {
 				importList.add(id);
 			}
 		}
 
 		return importList;
 	}
 
 	private CompilationUnit getCompilationUnitForInterface(String portTypeName) throws CoreException {
 		IFile inFile = getServiceInterfaceFile(portTypeName);
 		if(!inFile.exists()){
 			throw new CoreException(StatusUtils.errorStatus(JBossWSCreationCoreMessages.Error_Message_Failed_To_Generate_Code));
 		}
 		ICompilationUnit icu = JBossWSCreationUtils.getJavaUnitFromFile(inFile);
 		ASTParser astp = ASTParser.newParser(AST.JLS3);
 		astp.setSource(icu);
 
 		CompilationUnit cu = (CompilationUnit) astp.createAST(null);
 
 		return cu;
 	}
 
 	private IFile getServiceInterfaceFile(String portTypeName) {
 		IFolder pkgFolder = getPackageFolder();
 		IFile inFile = pkgFolder.getFile(portTypeName + DEFAULT_CU_SUFFIX);
 		return inFile;
 	}
 
 	private IFolder getPackageFolder() {
 		IProject project = JBossWSCreationUtils.getProjectByName(model
 				.getWebProjectName());
 		IFolder srcFolder = project.getFolder(RESOURCE_FOLDER);
 		String pkgFolderName = model.getCustomPackage().replace(".",
 				File.separator);
 		return srcFolder.getFolder(pkgFolderName);
 
 	}
 
 }
