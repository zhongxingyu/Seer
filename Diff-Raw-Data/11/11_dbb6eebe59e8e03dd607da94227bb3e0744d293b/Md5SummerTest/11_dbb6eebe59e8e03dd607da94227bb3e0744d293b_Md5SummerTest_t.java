 package com.mathieubolla.io;
 
 import static org.fest.assertions.Assertions.assertThat;
 
 import java.io.File;
 
 import org.junit.Test;
 
 public class Md5SummerTest {
 	@Test
 	public void shouldHashSampleCorrectly() {
 		Md5Summer summer = new Md5Summer();
 		assertThat(summer.hash(new File("src/test/resources/sample2.data"))).isEqualTo("b728e867fdb6569881121243598f6d42");
 		assertThat(summer.hash(new File("src/test/resources/sample1.data"))).isEqualTo("001bca3226b6671bbaf9d8a9699b629c");
		assertThat(summer.hash(new File("src/test/resources/sample3.data"))).isEqualTo("0000a5a8d69d59a796ea98df33393785");
 	}
 }
