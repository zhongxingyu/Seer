 package sg.edu.nus.iss.yunakti.engine.parser;
 
 import static sg.edu.nus.iss.yunakti.engine.util.YConstants.ANNOTATION_PROPERTY_CLASS_UNDER_TEST;
 import static sg.edu.nus.iss.yunakti.engine.util.YConstants.TEST_CASE_ANNOTATION;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import org.apache.commons.lang.StringUtils;
 import org.eclipse.jdt.core.ICompilationUnit;
 import org.eclipse.jdt.core.dom.ASTNode;
 import org.eclipse.jdt.core.dom.ASTVisitor;
 import org.eclipse.jdt.core.dom.FieldDeclaration;
 import org.eclipse.jdt.core.dom.IMethodBinding;
 import org.eclipse.jdt.core.dom.ITypeBinding;
 import org.eclipse.jdt.core.dom.MemberValuePair;
 import org.eclipse.jdt.core.dom.MethodDeclaration;
 import org.eclipse.jdt.core.dom.MethodInvocation;
 import org.eclipse.jdt.core.dom.NormalAnnotation;
 import org.eclipse.jdt.core.dom.SimpleName;
 import org.eclipse.jdt.core.dom.SimpleType;
 import org.eclipse.jdt.core.dom.TypeDeclaration;
 
 import sg.edu.nus.iss.yunakti.engine.util.ConsoleStreamUtil;
 import sg.edu.nus.iss.yunakti.model.YClass;
 import sg.edu.nus.iss.yunakti.model.YMethod;
 import sg.edu.nus.iss.yunakti.model.YModel;
 import sg.edu.nus.iss.yunakti.model.YTYPE;
 
 public class YModelVisitor extends ASTVisitor implements YModelSource{
 
 	private static Logger logger=Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
 	ConsoleStreamUtil streamUtil=ConsoleStreamUtil.getInstance();
 	
 	private YModel model;
 	private List<MethodDeclaration> methods = new ArrayList<MethodDeclaration>();
 	private List<FieldDeclaration> fields = new ArrayList<FieldDeclaration>();
 	private List<NormalAnnotation> annotations = new ArrayList<NormalAnnotation>();
 	private boolean testCaseConstructed=false;
 	private String currentClassName;
 	private YClass testCase;
 	private YClass classUnderTest=null;
 	private ICompilationUnit testCaseCompilationUnit;
 
 	private Set<String> allTestCaseMethods=new HashSet<String>();
 	private List<String> allClassNames;
 
 	public YModelVisitor(YModel model, ICompilationUnit testCaseElementCompilationUnit, List<String> allClassNames) {
 		this.model=model;
 		this.testCaseCompilationUnit=testCaseElementCompilationUnit;
 		this.allClassNames=allClassNames;
 	}
 	
 
 	@Override
 	public boolean visit(MethodDeclaration node) {
 		methods.add(node);
 		
 		return super.visit(node);
 	}
 	
 	@Override
 	public boolean visit(FieldDeclaration node) {
 		
 		if (node.getType().isSimpleType()){
 			SimpleType simpleFieldType=(SimpleType) node.getType();
 			addToModel(simpleFieldType.resolveBinding().getQualifiedName());
 		}
 		
 		return super.visit(node); 
 	}
 
 	private void addToModel(String qualifiedName) {
 		
 		logger.fine("Current qualifed nme : "+qualifiedName);
 		logger.fine("Current class name   : "+currentClassName);
 		if (!testCaseConstructed) return;
		else if (StringUtils.equals(qualifiedName, currentClassName)) return;
 		YClass member=null;
 		
 		if (allClassNames.contains(qualifiedName)){
 			member=new YClass(qualifiedName);
 			member.setyClassType(YTYPE.TEST_HELPER);
 			//model.getTestCases().get(0).addMember(member);
 			testCase.addMember(member);
 		}
 	}
 
 	@Override
 	public boolean visit(NormalAnnotation node) {
 		
 		annotations.add(node);
 		resolveClassUnderTest(node);
 		
 		return super.visit(node);
 	}
 	
 	@Override
 	public boolean visit(TypeDeclaration node) {
 		
 		ConsoleStreamUtil.println("Field Class name : "+node.getName().toString());
 		testCase=new YClass(node.resolveBinding().getQualifiedName());
 		testCase.setyClassType(YTYPE.TEST_CASE);
 		testCase.setPath(testCaseCompilationUnit.getResource().getLocation().toOSString());
 		model.addTestCase(testCase);
 		testCaseConstructed=true;
 		currentClassName=testCase.getFullyQualifiedName();
 		//model.addTestCase(new YClass(ParserUtils.getClassName(node.getName().toString())));
 		return super.visit(node);
 		
 	}
 	
 	
 	@Override
 	public boolean visit(SimpleName simpleName){
 		
 		if(simpleName.resolveTypeBinding() != null && simpleName.resolveTypeBinding().isClass()){
 			addToModel(simpleName.resolveTypeBinding().getQualifiedName());	
 		}
 		  
 		return super.visit(simpleName); 
 	}
 
 	//this method will be invoked for all the method invocations by the testcase
 	@Override
 	public boolean visit(MethodInvocation node) {
 		
 		ConsoleStreamUtil.println("Method invocation : "+node);
 		logger.fine("Method invocation :"+node);
 		
 		//get the name of the testcase
 		String testCaseMethod=getCallerMethod(node);
 		
 		IMethodBinding methodBinding = node.resolveMethodBinding();
 		ITypeBinding declaringClass = methodBinding.getDeclaringClass();
 		IMethodBinding methodDeclaration = methodBinding.getMethodDeclaration();
 
 		String cutClassName=declaringClass.getQualifiedName();
 		String cutMethodName=methodDeclaration.getName();
 		
 		
 		YMethod testMethod=getTestClassMethodIfAvailable(testCaseMethod, cutClassName, cutMethodName);
 		
 		if (testMethod!=null){
 			testCase.addMethod(testMethod);
 		}
 		
 		
 		if (StringUtils.equalsIgnoreCase(cutClassName, model.getClassUnderTest().getFullyQualifiedName())){
 			ConsoleStreamUtil.println("Adding method to be annotated : "+testMethod);
 			testCase.addMethodToBeAnnotated(testMethod);
 		}
 		
 		ConsoleStreamUtil.println("Classname and method name : "+cutClassName +"::::"+cutMethodName);
 		return super.visit(node);
 	}
 	
 
 	private YMethod getTestClassMethodIfAvailable(String testCaseMethod, String cutClassName, String cutMethodName) {
 
 		
 		/*if (allClassNames.contains(methodName)){
 			return null;
 		}*/
 		
 		YMethod returnYMethod=new YMethod();
 		
 		returnYMethod.setParentClass(new YClass(testCase.getFullyQualifiedName()));
 		
 		YMethod calleeMethod=new YMethod(cutMethodName);
 		
 		if (allTestCaseMethods.contains(testCaseMethod)){
 			
 			for (YMethod eachTestCaseMethod : testCase.getMethods()) {
 				if (StringUtils.equalsIgnoreCase(eachTestCaseMethod.getMethodName(), testCaseMethod)){
 					returnYMethod=eachTestCaseMethod;
 					break;
 				}
 			}
 		}
 		else{
 			allTestCaseMethods.add(testCaseMethod);
 			returnYMethod=new YMethod(testCaseMethod);
 		}
 		
 		//add Parent class and callee information
 		calleeMethod.setParentClass(new YClass(cutClassName));
 		
 		if (classUnderTest!=null && StringUtils.equals(classUnderTest.getFullyQualifiedName(), cutClassName)){
 			classUnderTest.addMethod(calleeMethod);
 		}
 		
 		returnYMethod.addCallee(calleeMethod);
 		
 		return returnYMethod;
 	}
 
 
 	private String getCallerMethod(MethodInvocation node) {
 
 		MethodDeclaration callerMethodNode=null;
 		ASTNode currentNode=node;
 		while (currentNode.getNodeType()!=ASTNode.METHOD_DECLARATION){
 			currentNode=currentNode.getParent();
 		}
 		callerMethodNode=(MethodDeclaration) currentNode;
 		
 		return callerMethodNode.resolveBinding().getMethodDeclaration().getName();
 
 	}
 
 
 	private void resolveClassUnderTest(NormalAnnotation node) {
 		if (StringUtils.equals(node.getTypeName().getFullyQualifiedName(),TEST_CASE_ANNOTATION)){
 			List<MemberValuePair> members = node.values();
 			for (MemberValuePair memberValuePair : members) {
 				if (StringUtils.equals(memberValuePair.getName().toString(),ANNOTATION_PROPERTY_CLASS_UNDER_TEST)){
 					
 					String classUnderTestString="";
 					
 					classUnderTestString=memberValuePair.getValue().toString();
 					if (StringUtils.isNotBlank(classUnderTestString)){
 						classUnderTestString=StringUtils.replace(classUnderTestString, "\"", "");
 					}
 					
 					logger.fine("Class Under Test String : "+classUnderTestString);
 					classUnderTest=new YClass(classUnderTestString);
 					classUnderTest.setyClassType(YTYPE.CLASS_UNDER_TEST);
 					logger.fine("Annotation Root : "+node.getRoot());
 					
 					model.setClassUnderTest(classUnderTest);
 					
 				}
 			}
 			
 		}
 	}
 	
 	
 	public List<MethodDeclaration> getMethods() {
 		return methods;
 	}
 	
 	public List<NormalAnnotation> getAnnotations() {
 		return annotations;
 	}
 
 	public List<FieldDeclaration> getFields() {
 		return fields;
 		
 	}
 	
 }
