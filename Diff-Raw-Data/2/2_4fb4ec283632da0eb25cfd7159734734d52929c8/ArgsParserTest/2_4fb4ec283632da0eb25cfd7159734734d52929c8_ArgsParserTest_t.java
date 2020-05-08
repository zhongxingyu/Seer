 package it.nuccioservizi.as400querier;
 
 import static junit.framework.Assert.assertEquals;
 
 import java.io.IOException;
 
 import org.testng.annotations.Test;
 
 import com.google.common.collect.ImmutableMap;
 
 public class ArgsParserTest {
 	@Test
 	public static void itShouldAcceptASingleArgumentAsTheQueryName() throws IOException {
		final ArgsParser parser = new ArgsParser(new String[] { "modelli" });
 		parser.getQuery();
 	}
 
 	@Test
 	public static void itShouldParsArgumentsAsVars() {
 		final ArgsParser parser = new ArgsParser(new String[] { "scalarini", "a=b", "c=d" });
 		assertEquals(ImmutableMap.of("a", "b", "c", "d"), parser.getVars());
 	}
 
 	@SuppressWarnings("unused")
 	@Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "^No default action\\. Tell me what you want to query\\.\\.\\.$")
 	public static void itShouldRequireAtLeastAnArgument() {
 		new ArgsParser(new String[] {});
 	}
 }
