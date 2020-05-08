 package refactoring;
 
 import java.io.IOException;
 import java.math.BigInteger;
 import java.util.List;
 
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.emftext.language.java.containers.CompilationUnit;
 import org.emftext.language.java.expressions.ExpressionsFactory;
 import org.emftext.language.java.expressions.UnaryExpression;
 import org.emftext.language.java.expressions.impl.ExpressionsFactoryImpl;
 import org.emftext.language.java.literals.BooleanLiteral;
 import org.emftext.language.java.literals.DecimalIntegerLiteral;
 import org.emftext.language.java.literals.LiteralsFactory;
 import org.emftext.language.java.literals.impl.LiteralsFactoryImpl;
 import org.emftext.language.java.members.Method;
 import org.emftext.language.java.resource.JaMoPPUtil;
 import org.emftext.language.java.statements.Block;
 import org.emftext.language.java.statements.Condition;
 import org.emftext.language.java.statements.LocalVariableStatement;
 import org.emftext.language.java.statements.StatementsFactory;
 import org.emftext.language.java.statements.impl.StatementsFactoryImpl;
 import org.emftext.language.java.types.PrimitiveType;
 import org.emftext.language.java.types.TypesFactory;
 import org.emftext.language.java.types.impl.TypesFactoryImpl;
 import org.emftext.language.java.variables.LocalVariable;
 import org.emftext.language.java.variables.VariablesFactory;
 import org.emftext.language.java.variables.impl.VariablesFactoryImpl;
 
 /* 
  * projects needed in the build path:
  * 
 * org.emftext.language.java
  * org.emftext.language.java.resource
  * 
  * Reason: JaMoPP maven repo seems broken right now.
  * 
  * TODO: use maven dependency instead as soon as possible
  */
 
 /**
  * A simple demonstration of using JaMoPP to modify existing .java files.
  * 
  * This Class will read a file and insert following code directly under the first method header:
  * 
  * <pre>if (true) {
  *   int answer = 23;
  *} else {
  *}</pre>
  * 
  * @author Christian Busch
  */
 public class Refactoring {
 
     private static final String location = "src/test/java/input/CalculatorPow.java";
 
 
     public static void main(String[] args) {
 
 	JaMoPPUtil.initialize(); // initialize everything (has to be done once.)
 
 	Resource resource = readJavaFile(location);
 	LocalVariableStatement content = getFirstVariableStatementOfFirstMethod(resource);
 	modifyCodeBefore(content);
 	saveModifications(resource);
     }
 
 
     private static Resource readJavaFile(String fileLocation) {
 
 	ResourceSet resSet = new ResourceSetImpl();
 	Resource resource = resSet.getResource(URI.createURI(fileLocation), true);
 	return resource;
     }
 
 
     private static LocalVariableStatement getFirstVariableStatementOfFirstMethod(Resource resource) {
 
 	CompilationUnit cu = (CompilationUnit) resource.getContents().get(0);
 	List<Method> methods = cu.getContainedClass().getMethods();
 	LocalVariableStatement content = methods.get(0).getFirstChildByType(LocalVariableStatement.class);
 	return content;
     }
 
 
     private static void modifyCodeBefore(LocalVariableStatement content) {
 
 	StatementsFactory statFac = new StatementsFactoryImpl();
 	ExpressionsFactory expFac = new ExpressionsFactoryImpl();
 	LiteralsFactory litFac = new LiteralsFactoryImpl();
 	VariablesFactory varFac = new VariablesFactoryImpl();
 	TypesFactory typeFac = new TypesFactoryImpl();
 
 	Block ifBlock = statFac.createBlock();
 	Block elseBlock = statFac.createBlock();
 
 	LocalVariableStatement initializedVariable = createLocalVariableStatement(statFac, litFac, varFac,
 		typeFac);
 	ifBlock.getStatements().add(initializedVariable);
 
 	Condition ifElseBlock = statFac.createCondition();
 	UnaryExpression falseBool = buildFalseBoolean(expFac, litFac);
 	ifElseBlock.setCondition(falseBool);
 	ifElseBlock.setStatement(ifBlock);
 	ifElseBlock.setElseStatement(elseBlock);
 
 	content.addBeforeContainingStatement(ifElseBlock);
     }
 
 
     private static UnaryExpression buildFalseBoolean(ExpressionsFactory expFac, LiteralsFactory litFac) {
 
 	UnaryExpression exp = expFac.createUnaryExpression();
 	BooleanLiteral boo = litFac.createBooleanLiteral();
 	boo.setValue(false);
 	exp.setChild(boo);
 	return exp;
     }
 
 
     private static LocalVariableStatement createLocalVariableStatement(StatementsFactory statFac,
 	    LiteralsFactory litFac, VariablesFactory varFac, TypesFactory typeFac) {
 
 	LocalVariable answerVariable = varFac.createLocalVariable();
 	PrimitiveType intType = typeFac.createInt();
 	answerVariable.setTypeReference(intType);
 	answerVariable.setName("answer");
 	DecimalIntegerLiteral intLit = litFac.createDecimalIntegerLiteral();
 	intLit.setDecimalValue(new BigInteger("23"));
 	answerVariable.setInitialValue(intLit);
 
 	LocalVariableStatement locVarStat = statFac.createLocalVariableStatement();
 	locVarStat.setVariable(answerVariable);
 	return locVarStat;
     }
 
 
     private static void saveModifications(Resource resource) {
 
 	try {
 	    resource.save(null);
 	} catch (IOException e) {
 	    System.out.println("ERROR when saving modifications!");
 	    e.printStackTrace();
 	}
     }
 
 }
