 package org.sonar.duplications.java;
 
 import static org.hamcrest.Matchers.hasItems;
 import static org.junit.Assert.assertThat;
 
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.sonar.duplications.api.codeunit.token.IToken;
 import org.sonar.duplications.api.codeunit.token.Token;
 import org.sonar.duplications.api.codeunit.token.TokenProvider;
 import org.sonar.duplications.api.lexer.ELanguage;
 import org.sonar.duplications.api.lexer.Lexer;
 import org.sonar.duplications.api.lexer.LexterFactory;
 import org.sonar.duplications.api.sourcecode.SourceCodeElement;
 
 public class TokenProviderTest {
 
 	TokenProvider tokenProvider;
 	Lexer lexer;
 	
 	@Before
 	public void initTest(){
 		
		String filename = this.getClass().getResource("/org/sonar/duplications/java/TestFile.jav").getPath();
 		
 		SourceCodeElement rootSourceElement = new SourceCodeElement(filename , Charset
 				.defaultCharset(), ELanguage.JAVA);
 		//ISourceCodeElementProvider<ISourceCodeElement> elementProvider = new SourceCodeElementProvider<ISourceCodeElement>();
 		lexer = LexterFactory.getJavaLexer();
 		tokenProvider = new TokenProvider(lexer);
 		tokenProvider.init(rootSourceElement);		
 	}
 	
 	@Test
 	public void shouldTokenizeSource(){
 		List <Token> tokenList = new ArrayList<Token>();
 		IToken token;
 		while( (token = tokenProvider.getNext() ) != null){
 			tokenList.add((Token) token);
 		}
 		assertThat(tokenList, hasItems(new Token("DirListing", 3 , 14), new Token("LITERAL", 6 , 30), new Token("INTEGER" , 9 , 17)));
 	}
 	
 }
