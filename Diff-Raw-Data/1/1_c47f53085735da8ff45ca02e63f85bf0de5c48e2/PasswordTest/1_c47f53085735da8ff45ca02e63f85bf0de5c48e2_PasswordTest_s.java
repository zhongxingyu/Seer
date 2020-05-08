 package no.steria.swhrs;
 
 import org.junit.Test;
 
 import static org.fest.assertions.Assertions.assertThat;
 
 public class PasswordTest {
     @Test
     public void testTwoIdenticalEquals() throws Exception {
         Password first = Password.fromPlaintext("salt", "password");
         Password second = Password.fromPlaintext("salt", "password");
         assertThat(first).isEqualTo(second);
     }
 
     @Test
     public void testTwoDifferentPasswordNotEquals() throws Exception {
         Password first = Password.fromPlaintext("salt", "password");
         Password second = Password.fromPlaintext("salt", "passwort");
         assertThat(first).isNotEqualTo(second);
     }
 
     @Test
     public void testTwoDifferentSaltNotEquals() throws Exception {
         Password first = Password.fromPlaintext("salt", "password");
         Password second = Password.fromPlaintext("zalt", "password");
         assertThat(first).isNotEqualTo(second);
     }
 
     @Test
     public void testTwoIdenticalButCreatedDifferentlyNotEquals() throws Exception {
         Password first = Password.fromPlaintext("salt", "password");
         Password second = Password.fromHashed(first.getSalt() + Password.SALT_SEPARATOR + first.getDigest());
         assertThat(first).isEqualTo(second);
     }
 }
