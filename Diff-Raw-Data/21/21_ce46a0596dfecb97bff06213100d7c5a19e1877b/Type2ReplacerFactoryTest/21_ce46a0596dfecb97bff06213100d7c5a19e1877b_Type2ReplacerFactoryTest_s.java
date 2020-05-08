 package ch.unibe.scg.cc;
 
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 import ch.unibe.jexample.Given;
 import ch.unibe.jexample.JExample;
 import ch.unibe.scg.cc.regex.Replace;
 
 @RunWith(JExample.class)
 public class Type2ReplacerFactoryTest {
 
 	String sampleString() {
 		return "\t\tfish.stink.Rod.doIt(new int[] { 1, 2 ,3 });\n" + "	}\n";
 	}
 
 	@Test
 	public PhaseFrontend testFactory() {
 		final Type2ReplacerFactory factory = new Type2ReplacerFactory();
 		final Replace[] replaces = factory.get();
		assertThat(replaces, is(arrayWithSize(3)));
 		final Normalizer normalizer = new Normalizer(replaces);
 		return normalizer;
 	}
 
 	@Given("testFactory")
 	public PhaseFrontend testNormalize(PhaseFrontend phase) {
 		final StringBuilder sb = new StringBuilder(sampleString());
 		phase.normalize(sb);
 		assertThat(sb.toString(), is("\t\tt. t. t. t(t t[] { 1, 1 ,1 });\n\t}\n"));
 		return phase;
 	}
 }
