 package se.enbohms.hhcib.entity;
 
 import static org.fest.assertions.Assertions.assertThat;
 
 import org.junit.Test;
 
 /**
  * Test client for the {@link Password} class
  */
 public class PasswordTest {
 
 	@Test(expected = IllegalArgumentException.class)
 	public void should_throw_exception_when_password_is_null() throws Exception {
 		Password.of(null);
 	}
 
 	@Test(expected = IllegalArgumentException.class)
 	public void should_throw_exception_when_password_is_less_than_three_charactes()
 			throws Exception {
 		Password.of("abc");
 	}
 
 	@Test(expected = IllegalArgumentException.class)
 	public void should_throw_exception_when_password_is_more_than_fifthy_charactes()
 			throws Exception {
 		Password.of("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa "
 				+ "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa "
 				+ "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
 	}
 
	@Test(expected = IllegalArgumentException.class)
 	public void should_create_valid_password() throws Exception {
 		Password pwd = Password.of("abcd");
 		assertThat(pwd.getPassword()).isEqualTo("abcd");
 	}
 }
