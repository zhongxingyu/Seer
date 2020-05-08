 package scanner;
 
 import htmlscanner.Token;
 import htmlscanner.TokenType;
 
 import java.util.Arrays;
 import java.util.List;
 
 public class MiscAutomationTests extends AutomatonTests {
 	public void testEmtpy() throws Exception {
 		ScanResult result = scan("");
 		assertTrue(result.getTokens().isEmpty());
 		assertTrue(result.getErrors().isEmpty());
 	}
 	
 	public void testIdent() throws Exception {
 		assertTokens(
 				token(TokenType.ID, "a10").and(TokenType.ID, "bbb")
 						.and(TokenType.ID, "c001"), "a10 bbb c001");
 	}
 	
 	public void testInt() throws Exception {
 		ScanResult result = scan("10 00 101");
 		List<Token> tokens = Arrays.asList(new Token(TokenType.ENTERO,"10"),
 				new Token(TokenType.ENTERO,"00"), new Token(TokenType.ENTERO,"101"));
 		assertTrue(String.format("tokens esperado: %s\ntokens resultado: %s",tokens,result.tokens),isEqual(result.getTokens(),tokens));
 		System.out.println(result.tokens);
 	}
 	
 	public void testReal() throws Exception {
 		ScanResult result = scan("10 0.01 101");
 		List<Token> tokens = Arrays.asList(new Token(TokenType.ENTERO,"10"),
 				new Token(TokenType.REAL,"0.01"),
 				new Token(TokenType.ENTERO,"101"));
 		assertTrue(String.format("tokens esperado: %s\ntokens resultado: %s",tokens,result.tokens),isEqual(result.getTokens(),tokens));
 		System.out.println(result.tokens);
 	}
 	
 	public void testIntAndIdent() throws Exception {
 		String input = "10a";
 		ScanResult result = scan(input);
 		printResult(result);
 		List<Token> tokens = Arrays.asList(new Token(TokenType.ENTERO,"10"), new Token(TokenType.ID,"a"));
 		assertTrue(isEqual(tokens,result.tokens));
 		assertTrue(result.errors.isEmpty());
 		
 	}
 	public void testAllLetters() throws Exception {
 		ScanResult result = scan(allLetters);
 		printResult(result);
 		List<Token> expected =Arrays.asList(new Token(TokenType.ID,"abcdefghijklmnop"));
 		assertTrue(isEqual(expected ,result.tokens));
 	}
 	public void testMiscLettersNumbers() throws Exception {
 		ScanResult result = scan("apo9231 1231assqe 2.3 FUUUj1223lAq1j2zx31");
 		printResult(result);
 		List<Token> expected =Arrays.asList(
 					new Token(TokenType.ID,"apo9231"),
 					new Token(TokenType.ENTERO,"1231"),
 					new Token(TokenType.ID,"assqe"),
 					new Token(TokenType.REAL,"2.3"),
 					new Token(TokenType.ID,"FUUUj1223lAq1j2z")
 				);
 		assertTrue(isEqual(expected ,result.tokens));
 	}
 	
 	public void testIdentLimit() throws Exception {
		assertTokensAndError(token(TokenType.ID,"FUUUj1223lAq1j2z"), "FUUUj1223lAq1j2zFUUUj1223lAq1j2zFUUUj1223lAq1j2zFUUUj1223lAq1j2z");
 	}
 }
