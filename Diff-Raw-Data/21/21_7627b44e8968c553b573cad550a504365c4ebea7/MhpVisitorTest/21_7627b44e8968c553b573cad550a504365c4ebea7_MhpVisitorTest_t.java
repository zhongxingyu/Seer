 import junit.framework.TestCase;
 import set.*;
 import visitor.*;
 import syntaxtree.*;
 
 public class MhpVisitorTest extends TestCase {
     MiniX10Parser _parser;
     public File Parse(String file) {
 	try {	    
 	    java.io.InputStream stream = new java.io.FileInputStream(file);
 	    _parser = new MiniX10Parser(stream);
 	    File f = _parser.File();
 	    return f;
 	}
 	catch (Exception e) {
 	    System.err.println(e);
 	    return null;
 	}
     }
 
     public void testMhpSimple() {
 	File f = Parse("examples/Simple.x10");
 	MhpVisitor v = new MhpVisitor();
 	v.visit(f);
 	assertEquals(v.M.size(), 8);
	assertTrue(v.M.contains(new StringPair("L5:i=i+2;","L3:i=i+3;")));
	assertTrue(v.M.contains(new StringPair("L2:i=i+1;","L4:i=i-1;")));			     
     }
 
     public void ftestMhpFinish() {	
 	File f = Parse("examples/Finish.x10");
 	MhpVisitor v = new MhpVisitor();
 	v.visit(f);
 	assertEquals(v.M.size(), 4);
 	assertTrue(v.M.contains(new StringPair("i = i + 1 ;","i = i + 9 ;")));
 	assertTrue(v.M.contains(new StringPair("i = i + 9 ;","i = i + 3 ;")));		
     }
 }
