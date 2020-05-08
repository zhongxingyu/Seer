 package ch20.ex05;
 
 import java.io.IOException;
 
 import org.junit.Test;
 
 public class FindWordTest {
 
 	@Test
 	public void test() throws IOException {
 		String[] args = {"nishide", "JPL/ch20/ex05/findWord.txt"};
 		FindWord.main(args);
 	}

 }
