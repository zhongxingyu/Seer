 package scanner;
 
 import htmlscanner.Token;
 import htmlscanner.TokenType;
 
 import java.io.File;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.math.RandomUtils;
 import org.ho.yaml.Yaml;
 
 import com.google.common.collect.Iterators;
 import com.google.common.collect.Maps;
 
 public class AllTokensJavaTest extends AutomatonTests {
 	public void testAllTokens() throws Exception {
 		String input = "iuo123\n" + "123\n" + "0.23\n" + "'s'\n"
 				+ "\"ajhskjhd\"\n" + "public\n" + "class\n" + ",\n" + ";\n"
 				+ "int\n" + "char\n" + "float\n" + "double\n" + "[\n" + "]\n"
 				+ "{\n" + "}\n" + "if\n" + "(\n" + ")\n" + "else\n" + "while\n"
 				+ "return\n" + "+\n" + "=\n" + "&&\n" + "==\n" + "<\n" + ">\n"
 				+ "-\n" + "∗\n" + "/\n" + "++\n" + "new\n";
 
 		Iterator<Token> tokens = scan(input).tokens.iterator();
 		Map<String, Token> toTest = Maps.newHashMap();
 		for (String s : input.split("\n")) {
 			Token t = null;
 			if (tokens.hasNext()) {
 				t = tokens.next();
 			}
 			toTest.put(s, t);
 		}
 		System.out.println(Yaml.dump(toTest));
 		// System.out.println(StringUtils.join(scan(input).tokens,"\n"));
 		//
 		// System.out.println(StringUtils.join(scan(input).errors,"\n"));
 		//
 		// System.out.println("INPUT:\n"+input);
 	}
 
 	public void testIntReal() throws Exception {
 		setLog(true);
 		assertTokens(
 				token(TokenType.ENTERO, "123").and(TokenType.REAL, "0.23"),
 				"123\n0.23");
 	}
 
 	public void testAllCombinations() throws Exception{
 		setLog(true);
		@SuppressWarnings("unchecked")
		Map<String,Object> conf = Yaml.loadType(new File("resources/tokens-java.yaml"), HashMap.class);
		@SuppressWarnings("unchecked")
		Map<String,Token> allTokens = (Map<String, Token>) conf.get("tokens");
 		@SuppressWarnings("unchecked")
 		List<String> seps = (List<String>) conf.get("separators");
 		
 		for(Entry<String, Token> entryFirst : allTokens.entrySet()){
 			String sep = seps.get(RandomUtils.nextInt(seps.size()));
 			for(Entry<String, Token> entrySecond : allTokens.entrySet()){
 				Token first = entryFirst.getValue(), second = entrySecond.getValue();
 				String input = StringUtils.join(Arrays.asList(entryFirst.getKey(), sep, entrySecond.getKey()), "");
 				System.out.println(String.format("TESTING cadena \"%s\" y tokens %s",input,Arrays.asList(first,second)));
 				assertTokens(token(first).and(second), input);
 			}
 		}
 		
 	}
 }
