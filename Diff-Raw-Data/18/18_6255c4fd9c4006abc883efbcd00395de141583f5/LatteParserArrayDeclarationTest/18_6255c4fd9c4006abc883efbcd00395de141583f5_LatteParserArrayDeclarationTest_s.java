 /**
  * 
  */
 package latte.lexparse;
 
 import junit.framework.TestCase;
 
 import org.antlr.runtime.RecognitionException;
 import org.junit.Test;
 
 
 /**
  * @author guydmann
  *
  */
 public class LatteParserArrayDeclarationTest extends TestCase {
 	public void testArrayDeclaration(String input) throws RecognitionException {
 		System.out.println(input + "\n");
 		System.out.println(LatteParser.makeParser(input).array_declaration().tree.toString() + "\n-----------\n");
 		
 	}
 	
 	
 	@Test
 	public void testArrayDeclaration01() throws Exception
 	{
 		testArrayDeclaration("a[1]");
 	}
 	
 	@Test
 	public void testArrayDeclaration02() throws Exception
 	{
 		testArrayDeclaration("a[0]");
 	}
 	
 	@Test
 	public void testArrayDeclaration03() throws Exception
 	{
 		testArrayDeclaration("a23asd[161]");
 	}
 	
 	@Test
 	public void testArrayDeclaration04() throws Exception
 	{
 		testArrayDeclaration("afsaf[0]");
 	}
 	
 }
