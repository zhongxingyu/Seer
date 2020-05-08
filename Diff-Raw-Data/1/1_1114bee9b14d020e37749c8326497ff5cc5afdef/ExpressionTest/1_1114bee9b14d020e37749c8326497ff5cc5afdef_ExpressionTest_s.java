 package chameleon.support.test;
 
 import static org.junit.Assert.assertTrue;
 
 import java.io.IOException;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.junit.Test;
 
 import chameleon.core.expression.Expression;
 import chameleon.input.ParseException;
 import chameleon.oo.type.Type;
 import chameleon.output.Syntax;
 import chameleon.test.ModelTest;
 import chameleon.test.provider.ElementProvider;
 import chameleon.test.provider.ModelProvider;
 
 /**
  * @author Marko van Dooren
  */
 public class ExpressionTest extends ModelTest {
   
 	/**
 	 * Create a new expression tester
 	 * @param provider
 	 * @throws IOException 
 	 * @throws ParseException 
 	 */
  /*@
    @ public behavior
    @
    @ post modelProvider() == modelProvider;
    @ post typeProvider() == typeProvider;
    @ post baseRecursive();
    @ post customRecursive();
    @*/
 	public ExpressionTest(ModelProvider provider, ElementProvider<Type> typeProvider) throws ParseException, IOException {
 		super(provider);
 		_typeProvider = typeProvider;
 	}
 	
   private ElementProvider<Type> _typeProvider;
   
   public ElementProvider<Type> typeProvider() {
   	return _typeProvider;
   }
   
   private static Logger _expressionLogger = Logger.getLogger("chameleon.test.expression");
   
   public static Logger getExpressionLogger() {
   	return _expressionLogger;
   }
 
   public void setLogLevels() {
   	//Logger.getRootLogger().setLevel(Level.FATAL);
   	getLogger().setLevel(Level.INFO);
 		//Logger.getLogger("chameleon.test.expression").setLevel(Level.FATAL);
   }
 
   @Test
   public void testExpressionTypes() throws Exception {
     Collection<Type> types = typeProvider().elements(language());
     getLogger().info("Starting to test "+types.size() + " types.");
     Iterator<Type> iter = types.iterator();
     long startTime = System.nanoTime();
     int count = 1;
     while (iter.hasNext()) {
       Type type = iter.next();
       getLogger().info(count+" Testing "+type.getFullyQualifiedName());
       processType(type);
       count++;
     }
     long endTime = System.nanoTime();
     System.out.println("Testing took "+(endTime-startTime)/1000000+" milliseconds.");
   }
 
   private int _count = 0;
   
   public void processType(Type type) throws Exception {
     _count++;
     Expression expr = null;
     Object o = null;
     try {
       List<Expression> exprs = type.descendants(Expression.class);
       for(Expression expression : exprs) {
       	Syntax syntax = language().connector(Syntax.class);
 //      	if(syntax != null) {
 //          getExpressionLogger().info(_count + " Testing: "+syntax.toCode(expression));
 //      	} else {
 //      		getExpressionLogger().info(_count + " Add a Syntax connector to the language for printing the code of the expression being tested.");
 //      	}
         Type expressionType = expression.getType();
 				assertTrue(expressionType != null);
 //        getExpressionLogger().info(_count + "        : "+expressionType.getFullyQualifiedName());
       }
     }
     catch (Exception e) {
       throw e; 
     }
     
   }
   
 }
