 /**
  * 
  */
 package ecologylab.tests;
 
 import java.util.ArrayList;
 
 import ecologylab.net.ParsedURL;
 import ecologylab.serialization.ElementState;
 import ecologylab.serialization.SIMPLTranslationException;
 import ecologylab.serialization.TranslationScope;
 
 /**
  *
  * @author andruid
  */
 public class TestScalarCollection extends ElementState
 {
	@simpl_collection("person") @simpl_nowrap	ArrayList<String>	stuff;
 	
	@simpl_collection("link") @simpl_nowrap		ArrayList<ParsedURL> purls;
 	
 	static final TranslationScope TS	= TranslationScope.get("test_scalar", null, TestScalarCollection.class);
 	
 	static final String	xml	= "<test_scalar_collection><person>fred</person><person>wilma</person><link>http://www.google.com</link><link>http://ecologylab.cs.tamu.edu</link></test_scalar_collection>";
 	
 	public static void main(String[] a)
 	{
 		try
 		{
 			ElementState es	= TS.deserializeCharSequence(xml);
 			
 			es.serialize(System.out);
 			
 		} catch (SIMPLTranslationException e)
 		{
 			e.printStackTrace();
 		}
 	}
 }
