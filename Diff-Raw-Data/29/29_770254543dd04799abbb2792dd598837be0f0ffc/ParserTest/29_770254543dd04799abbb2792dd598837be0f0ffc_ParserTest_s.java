 package de.kroesch.clt;
 
 import static org.junit.Assert.*;
 
 import org.junit.Test;
 
 
 public class ParserTest {
 
 	@Test
 	public void quoted() throws Exception {
 		Environment env = new TestEnvironment();
 		Parser parser = new Parser(env);
 	}
 }
