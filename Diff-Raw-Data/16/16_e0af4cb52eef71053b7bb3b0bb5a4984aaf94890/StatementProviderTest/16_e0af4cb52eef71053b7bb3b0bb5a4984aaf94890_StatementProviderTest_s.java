 package org.sonar.duplications.java;
 
 import static org.hamcrest.Matchers.hasItems;
 import static org.junit.Assert.assertThat;
 
 import java.io.File;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.sonar.duplications.api.codeunit.statement.Statement;
 import org.sonar.duplications.api.codeunit.statement.StatementProvider;
 import org.sonar.duplications.api.codeunit.token.TokenProvider;
 import org.sonar.duplications.api.lexer.ELanguage;
 import org.sonar.duplications.api.lexer.Lexer;
 import org.sonar.duplications.api.lexer.LexterFactory;
 import org.sonar.duplications.api.sourcecode.SourceCodeElement;
 
 public class StatementProviderTest {
 
 	StatementProvider statementProvider;
	String filename = this.getClass().getResource("../testdata/Test1.jav").getPath();
 	
 	@Before
 	public void initTest(){
 		
 		SourceCodeElement rootSourceElement = new SourceCodeElement(filename , Charset.defaultCharset(), ELanguage.JAVA);
 		Lexer lexer = LexterFactory.getJavaLexer();
 		TokenProvider tokenProvider = new TokenProvider(lexer);
 
 		statementProvider = new StatementProvider(tokenProvider, true);
 		statementProvider.init(rootSourceElement);
 	}
 	
 	@Test
 	public void shouldTokenizeSource(){
 		List <Statement> statementList = new ArrayList<Statement>();
 		Statement statement;
 		while( (statement = statementProvider.getNext() ) != null){
 			statementList.add((Statement) statement);
 		}
 		
 		assertThat(statementList, hasItems(
 				new Statement(new File(filename), 4, 4, "publicstaticvoidmain(String[]args)", "publicstaticvoidmain(String[]args)", 2), 
 				new Statement(new File(filename), 8, 8, "caseINTEGER:dirlist(args[INTEGER])", "case1:dirlist(args[0])", 6), 
 				new Statement(new File(filename), 10, 10, "default:System.out.println(LITERAL)", "default:System.out.println(\"Multiple files are not allow.\")",8),
 				new Statement(new File(filename), 11, 11, "System.exit(INTEGER)", "System.exit(0)", 9)));
 
 	}
 	
 }
