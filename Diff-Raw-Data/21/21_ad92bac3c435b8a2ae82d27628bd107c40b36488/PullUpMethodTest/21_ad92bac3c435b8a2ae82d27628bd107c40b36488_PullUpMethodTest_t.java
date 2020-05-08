 	package tests.jdolly;
 
 
 import jdolly.JDolly;
 import jdolly.JDollyImp;
 import AST.MethodDecl;
 import AST.Program;
 import AST.RefactoringException;
 import AST.TypeDecl;
 
 public class PullUpMethodTest extends Test {
 
 	private static final String methodTarget = "m_0";
	private static final String classTarget = "C1_0";
 
 	
 
 	@Override
 	protected Program refactoring(Program program) throws RefactoringException {
 
 		TypeDecl td = program.findType(classTarget);
 
 		MethodDecl md = td.findMethod(methodTarget);
 
 		md.doPullUp();
 
 		return program;
 	}
 
 	
 	@Override
 	protected JDolly getCUGen() {
 		 
 		JDolly jdolly = new JDollyImp("jdolly-0.4/alloyTheory/pullupmethod_final.als",2, 3, 4);
 		jdolly.setExactMaxPackages(true);
 		jdolly.setMaxMethodNames(3);
 		jdolly.setExactMaxMethodNames(true);
 		
 //		jdolly.setOptimized(isOptimized());
 //		jdolly.setJump(getJumpInterval());
 		return jdolly; 
 	}
 }
