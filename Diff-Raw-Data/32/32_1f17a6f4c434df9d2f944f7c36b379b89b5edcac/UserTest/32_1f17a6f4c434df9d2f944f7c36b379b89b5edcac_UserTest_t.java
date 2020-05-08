 package org.triple_brain.module.model;
 
 import org.apache.commons.codec.binary.Base64;
 import org.apache.commons.codec.digest.DigestUtils;
 import org.codehaus.jettison.json.JSONObject;
 import org.hamcrest.MatcherAssert;
 import org.hamcrest.Matchers;
 import org.junit.Test;
 import org.triple_brain.module.model.json.UserJsonFields;
 
 import static org.hamcrest.Matchers.*;
 import static org.hamcrest.core.Is.is;
 import static org.hamcrest.core.IsNull.notNullValue;
 import static org.junit.Assert.assertThat;
 import static org.triple_brain.module.model.json.UserJsonFields.*;
 
 /**
  * Copyright Mozilla Public License 1.1
  */
 public class UserTest {
 
     @Test
     public void can_create_user() {
         User user = User.withUsernameEmailAndLocales(
                 "roger_lamothe",
                 "roger.lamothe@example.org",
                "[fr]"
         );
 
         assertThat(user.id(), is(notNullValue()));
         assertThat(user.username(), is("roger_lamothe"));
         assertThat(user.email(), is("roger.lamothe@example.org"));
     }
 
     @Test
     public void can_modify_user() {
         User user = User.withUsernameEmailAndLocales(
                 "a_user_name",
                 "email@mail.org",
                "[fr]"
         );
         user.password("secret");
         MatcherAssert.assertThat(user.hasPassword("secret"), Matchers.is(true));
     }
 
     @Test
     public void equality_is_based_on_email() {
         User user_1 = User.withUsernameEmailAndLocales(
                 "user1",
                 "email_1@mail.org",
                "[fr]"
         );
         User user_2 = User.withUsernameEmailAndLocales(
                 "user2",
                 "email_2@mail.org",
                "[fr]"
         );
         User user_3 = User.withUsernameEmailAndLocales(
                 "user3",
                 "email_1@mail.org",
                "[fr]"
         );
 
         MatcherAssert.assertThat(user_1, Matchers.is(not(equalTo(user_2))));
         MatcherAssert.assertThat(user_1, Matchers.is(equalTo(user_3)));
     }
 
     @Test
     public void email_is_always_saved_in_lower_case_and_trimed() {
         User user = User.withUsernameEmailAndLocales(
                 "a_user_name",
                 "  Email@mail.org  ",
                "[fr]"
         );
         MatcherAssert.assertThat(
                 user.email(),
                 Matchers.is("email@mail.org")
         );
     }
 
     @Test
     public void password_is_encoded_with_salt() {
         String password = "Potatoe";
 
         User user = User.withUsernameEmailAndLocales(
                 "roger_lamothe",
                 "roger@lamothe.org",
                "[fr]"
         ).password(password);
         String expectedPasswordHash = Base64.encodeBase64URLSafeString(DigestUtils.sha512(password + user.salt()));
 
         MatcherAssert.assertThat(user.passwordHash(), Matchers.is(expectedPasswordHash));
     }
 
     @Test
     public void can_clean_password() {
         User user = User.withUsernameEmailAndLocales(
                 "roger_lamothe",
                 "roger@lamothe.org",
                "[fr]"
         ).password("potatoe");
         MatcherAssert.assertThat(user.hasPassword("potatoe"), Matchers.is(true));
 
         user.cleanPassword();
 
         MatcherAssert.assertThat(user.hasPassword("potatoe"), Matchers.is(false));
         MatcherAssert.assertThat(user.passwordHash(), Matchers.is(nullValue()));
     }
 
     @Test
     public void hashCode_is_based_on_email() {
         User member = User.withUsernameEmailAndLocales(
                 "roger_lamothe",
                 "roger.lamothe@example.org",
                "[fr]"
         );
         MatcherAssert.assertThat(member.hashCode(), Matchers.is("roger.lamothe@example.org".hashCode()));
     }
 
     @Test
     public void toString_give_user_email() {
         User user = User.withUsernameEmailAndLocales(
                 "roger_lamothe",
                 "roger.lamothe@example.org",
                "[fr]"
         );
         MatcherAssert.assertThat(user.toString(), Matchers.is("roger.lamothe@example.org"));
     }
 
     @Test
     public void can_get_json_representation_of_user() throws Exception {
         User user = User.withUsernameEmailAndLocales(
                 "roger_lamothe",
                 "roger.lamothe@example.org",
                "[fr]"
         );
         JSONObject jsonUser = UserJsonFields.toJson(user);
         assertThat(jsonUser.getString(USER_NAME), is("roger_lamothe"));
         assertThat(jsonUser.getString(EMAIL), is("roger.lamothe@example.org"));
     }
 }
