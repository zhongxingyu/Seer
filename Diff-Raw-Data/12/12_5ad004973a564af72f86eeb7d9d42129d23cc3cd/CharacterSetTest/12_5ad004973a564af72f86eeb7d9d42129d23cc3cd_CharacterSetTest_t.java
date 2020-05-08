 package com.wesabe.servlet.normalizers.util.tests;
 
 import static org.hamcrest.CoreMatchers.*;
 import static org.junit.Assert.*;
 
 import org.junit.Test;
 import org.junit.experimental.runners.Enclosed;
 
 import org.junit.runner.RunWith;
 
 import com.wesabe.servlet.normalizers.util.CharacterSet;
 
 @RunWith(Enclosed.class)
 public class CharacterSetTest {
 	public static class A_Set_From_An_Array_Of_Chars {
 		private final CharacterSet chars = CharacterSet.of('a', 'b', 'c', 'd');
 		
 		@Test
 		public void itContainsSomeCharacters() throws Exception {
 			assertThat(chars.contains(Character.valueOf('c')), is(true));
 		}
 		
 		@Test
 		public void itDoesntContainSomeCharacters() throws Exception {
 			assertThat(chars.contains(Character.valueOf('z')), is(false));
 		}
 		
 		@Test
 		public void itCanHandlePrimitives() throws Exception {
 			assertThat(chars.contains('c'), is(true));
 			assertThat(chars.contains('D'), is(false));
 		}
 		
 		@Test
 		public void itTestsForComposition() throws Exception {
 			assertThat(chars.composes("dddbbccdd"), is(true));
 			assertThat(chars.composes("dddbb1ccdd"), is(false));
			assertThat(chars.composes(null), is(false));
 		}
 	}
 	
 	public static class A_Set_From_A_String {
 		private final CharacterSet chars = CharacterSet.of("acbd");
 		
 		@Test
 		public void itContainsSomeCharacters() throws Exception {
 			assertThat(chars.contains(Character.valueOf('c')), is(true));
 		}
 		
 		@Test
 		public void itDoesntContainSomeCharacters() throws Exception {
 			assertThat(chars.contains(Character.valueOf('z')), is(false));
 		}
 		
 		@Test
 		public void itCanHandlePrimitives() throws Exception {
 			assertThat(chars.contains('c'), is(true));
 			assertThat(chars.contains('D'), is(false));
			assertThat(chars.composes(null), is(false));
 		}
 	}
 }
