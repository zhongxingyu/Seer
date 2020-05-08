 package com.orbious.util;
 
 import com.orbious.util.Command;
 import static org.hamcrest.CoreMatchers.*;
 import static org.junit.Assert.*;
 import org.junit.Test;
 
 public class CommandTest {
 
 	@Test
 	public void type() throws Exception {
 		assertThat(Command.class, notNullValue());
 	}
 
 	@Test
 	public void instance_A$() throws Exception {
 		Object actual = Command.instance();
 		Object expected = Command.instance();
 		assertThat(actual, is(equalTo(expected)));
 	}
 
 	@Test
 	public void shutdown_A$() throws Exception {
 		Command target = Command.instance();
 		assertThat(target.shutdown(), is(equalTo(false)));
 	}
 
 	@Test
 	public void shutdown_A$boolean() throws Exception {
     Command target = Command.instance();
     target.shutdown(true);
     assertThat(target.shutdown(), is(equalTo(true)));
 
     // reset
     target.shutdown(false);
 	}
 
 	@Test
 	public void canExit_A$() throws Exception {
 		Command target = Command.instance();
    assertThat(target.canExit(), is(equalTo(true)));
 	}
 
 	@Test
 	public void canExit_A$boolean() throws Exception {
     Command target = Command.instance();
    target.canExit(false);
    assertThat(target.canExit(), is(equalTo(false)));
 
     // reset
    target.canExit(true);
 	}
 
 }
