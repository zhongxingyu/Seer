 package com.gooddata.processor.parser;
 
 import org.gooddata.processor.Command;
 import junit.framework.TestCase;
 import org.apache.log4j.Logger;
 
 import java.io.FileReader;
 import java.io.Reader;
 import java.util.List;
 
 /**
  * GoodData
  *
  * @author zd <zd@gooddata.com>
  * @version 1.0
  */
 public class TestDIScriptParser extends TestCase {
 
     private static Logger l = Logger.getLogger(TestDIScriptParser.class);
 
     public void testParseCmd() throws Exception {
         try {
            Reader r = new FileReader("../distro/examples/ga/ga.cmd");
             DIScriptParser parser = new DIScriptParser(r);
             List<Command> commands = parser.parse();
             assertEquals(commands.size(),8);
         }
         catch(Exception e) {
            e.printStackTrace();
         }
     }
 
 
 }
