 package net.guipsp.gindex;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.JUnit4;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 
 @RunWith(JUnit4.class)
 public class IndexTest {
 
 	@Test
 	public void count() {
		assertThat("Unexpected number of classes indexed", index.INDEX.length, is(2));
 	}
 }
