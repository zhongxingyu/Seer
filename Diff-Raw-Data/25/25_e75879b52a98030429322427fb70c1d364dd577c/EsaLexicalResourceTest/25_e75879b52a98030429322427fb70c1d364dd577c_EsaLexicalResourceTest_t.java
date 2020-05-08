 package test.lexent.esa;
 
 import lexent.resource.esa.EsaLexicalResource;
 
 import org.junit.Test;
 
 public class EsaLexicalResourceTest {
 	
 	@Test
 	public void test_probEntails() {
 		EsaLexicalResource esa = new EsaLexicalResource();
		System.out.println(esa.probEntails("dog", "animal", null));
		System.out.println(esa.probEntails("animal", "dog", null));
		System.out.println(esa.probEntails("kidney", "organ", null));
		System.out.println(esa.probEntails("organ", "kidney", null));
		System.out.println(esa.probEntails("buy", "acquire", null));
		System.out.println(esa.probEntails("acquire", "buy", null));
		System.out.println(esa.probEntails("synthesis", "production", null));
		System.out.println(esa.probEntails("production", "synthesis", null));
 	}
 	
 }
