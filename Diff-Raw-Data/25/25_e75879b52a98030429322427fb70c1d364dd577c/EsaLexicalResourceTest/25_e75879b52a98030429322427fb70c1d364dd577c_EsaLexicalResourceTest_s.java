 package test.lexent.esa;
 
 import lexent.resource.esa.EsaLexicalResource;
 
 import org.junit.Test;
 
 public class EsaLexicalResourceTest {
 	
 	@Test
 	public void test_probEntails() {
 		EsaLexicalResource esa = new EsaLexicalResource();
		System.out.println(esa.probEntails("dog", "animal"));
		System.out.println(esa.probEntails("animal", "dog"));
		System.out.println(esa.probEntails("kidney", "organ"));
		System.out.println(esa.probEntails("organ", "kidney"));
		System.out.println(esa.probEntails("buy", "acquire"));
		System.out.println(esa.probEntails("acquire", "buy"));
		System.out.println(esa.probEntails("synthesis", "production"));
		System.out.println(esa.probEntails("production", "synthesis"));
 	}
 	
 }
