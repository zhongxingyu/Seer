 package org.sonar.ide.shared;
 
 import org.junit.Test;
 
import static org.hamcrest.CoreMatchers.equalTo;
 import static org.junit.Assert.assertThat;
 
 /**
  * @author Evgeny Mandrikov
  */
 public class SourceCodeMatcherTest {
 
   @Test
   public void testGetHashCode() {
     int hash1 = SourceCodeMatcher.getHashCode("int i;");
     int hash2 = SourceCodeMatcher.getHashCode("int\ti;");
     int hash3 = SourceCodeMatcher.getHashCode("int i;\n");
     int hash4 = SourceCodeMatcher.getHashCode("int i;\r\n");
     int hash5 = SourceCodeMatcher.getHashCode("int i;\r");
 
     assertThat(hash2, equalTo(hash1));
     assertThat(hash3, equalTo(hash1));
     assertThat(hash4, equalTo(hash1));
     assertThat(hash5, equalTo(hash1));
   }
 
 }
