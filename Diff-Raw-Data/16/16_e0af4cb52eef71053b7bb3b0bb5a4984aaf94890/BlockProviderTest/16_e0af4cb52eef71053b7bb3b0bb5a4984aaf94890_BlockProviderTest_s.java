 package org.sonar.duplications.java;
 
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.List;
 
 import junit.framework.Assert;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.sonar.duplications.api.codeunit.block.Block;
 import org.sonar.duplications.api.codeunit.block.BlockProvider;
 import org.sonar.duplications.api.codeunit.statement.StatementProvider;
 import org.sonar.duplications.api.codeunit.token.TokenProvider;
 import org.sonar.duplications.api.lexer.ELanguage;
 import org.sonar.duplications.api.lexer.Lexer;
 import org.sonar.duplications.api.lexer.LexterFactory;
 import org.sonar.duplications.api.sourcecode.SourceCodeElement;
 
 public class BlockProviderTest {
 	
 	BlockProvider blockProvider;
 	
	String filename = this.getClass().getResource("../testdata/Test1.jav").getPath();
 	
 	@Before
 	public void initTest(){
 		
 		SourceCodeElement rootSourceElement = new SourceCodeElement(filename , Charset.defaultCharset(), ELanguage.JAVA);
 		Lexer lexer = LexterFactory.getJavaLexer();
 		TokenProvider tokenProvider = new TokenProvider(lexer);
 		StatementProvider statementProvider = new StatementProvider(tokenProvider, true);
 		blockProvider = new BlockProvider(statementProvider, 3);
 		blockProvider.init(rootSourceElement);
 	}
 	
 	@Test
 	public void shouldTokenizeSource(){
 		List <Block> blockList = new ArrayList<Block>();
 		Block block;
 		while( (block = blockProvider.getNext() ) != null){
 			blockList.add((Block) block);
 			//System.out.println(block.getChunkHash().toString());
 		}
 		
 		Assert.assertEquals(0, blockList.get(0).getFirstUnitIndex());
 		Assert.assertEquals(1, blockList.get(0).getFirstLineNumber());
 		Assert.assertEquals(4, blockList.get(0).getLastLineNumber());
 
 		Assert.assertEquals(7, blockList.get(blockList.size()-1).getFirstUnitIndex());
 		Assert.assertEquals(9, blockList.get(blockList.size()-1).getFirstLineNumber());
 		Assert.assertEquals(11, blockList.get(blockList.size()-1).getLastLineNumber());
 		
 //		assertThat(blockList, hasItems(
 //				new Block(filename, null, 0 , 1 , 4), 
 //				new Block(filename, null, 7 , 9 , 11)));
 		
 	}
 	
 }
