 /*
  * Thibaut Colar Feb 5, 2010
  */
 package net.colar.netbeans.fan.test;
 
 import java.io.DataInputStream;
 import java.io.FileInputStream;
 import net.colar.netbeans.fan.parboiled.FantomParser;
 import net.jot.testing.JOTTestable;
 import net.jot.testing.JOTTester;
 import org.parboiled.Parboiled;
 import org.parboiled.common.StringUtils;
 import org.parboiled.support.ParseTreeUtils;
 import org.parboiled.support.ParsingResult;
 
 /**
  * Test new parboiled based parser
  * @author thibautc
  */
 public class FantomParserTest implements JOTTestable
 {
 
 	public void jotTest() throws Throwable
 	{
 		FantomParser parser = Parboiled.createParser(FantomParser.class);
 
 		DataInputStream dis = new DataInputStream(new FileInputStream("/home/thibautc/NetBeansProjects/Fan/test/parser_test/test.fan"));
 		byte[] buffer = new byte[dis.available()];
 		dis.readFully(buffer);
 		String testInput = new String(buffer);
 
 		ParsingResult<Object> result = parser.parse(parser.compilationUnit(), testInput);
 
 		System.out.println(testInput + " = " + result.parseTreeRoot.getValue() + '\n');
 		System.out.println("Parse Tree:\n" + ParseTreeUtils.printNodeTree(result) + '\n');
 
 		if (result.hasErrors())
 		{
 			System.out.println(StringUtils.join(result.parseErrors, "---\n"));
 		}
 		JOTTester.checkIf("Errors? ", ! result.hasErrors());
 	}
 }
