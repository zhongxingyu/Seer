 package org.springframework.social.appdotnet.api.impl;
 
 import org.junit.Test;
 import org.springframework.core.io.ClassPathResource;
 import org.springframework.http.MediaType;
 import org.springframework.social.appdotnet.api.data.user.ADNUser;
 import org.springframework.social.appdotnet.api.data.user.ADNUsers;
 
 import java.util.Arrays;
 
 import static junit.framework.Assert.assertEquals;
 import static org.springframework.http.HttpMethod.GET;
 import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
 import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
 import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
 
 /**
  * @author Arik Galansky
  */
 public class UsersTemplateTest extends AbstractAppdotnetApiTest{
 
     @Test
     public void getUser() {
         mockServer.expect(requestTo(AppdotnetTemplate.BASE_URL + "0/users/me?access_token=ACCESS_TOKEN")).andExpect(method(GET))
                 .andRespond(withSuccess(new ClassPathResource("/testdata/user.json", getClass()), MediaType.APPLICATION_JSON));
         ADNUser user = appdotnet.usersOperations().getUserProfile();
         assertBasicUser(user, "1", "mthurman", "Mark Thurman");
 
         assertEquals("Hi, I'm Mark Thurman and I'm teaching you about the @appdotnet Stream #API.", user.getDescription().getText());
         assertEquals("Hi, I'm Mark Thurman and I'm <a href=\"https://github.com/appdotnet/api_spec\" rel=\"nofollow\">teaching you</a> about the <span itemprop=\"mention\" data-mention-name=\"appdotnet\" data-mention-id=\"3\">@appdotnet</span> Stream #<span itemprop=\"hashtag\" data-hashtag-name=\"api\">API</span>.", user.getDescription().getHtml());
         assertEquals("appdotnet", user.getDescription().getEntities().getMentions().get(0).getName());
         assertEquals("3", user.getDescription().getEntities().getMentions().get(0).getId());
         assertEquals(52, user.getDescription().getEntities().getMentions().get(0).getPosition());
         assertEquals(10, user.getDescription().getEntities().getMentions().get(0).getLength());
         assertEquals("api", user.getDescription().getEntities().getHashtags().get(0).getName());
         assertEquals(70, user.getDescription().getEntities().getHashtags().get(0).getPosition());
         assertEquals(4, user.getDescription().getEntities().getHashtags().get(0).getLength());
         assertEquals("teaching you", user.getDescription().getEntities().getLinks().get(0).getText());
         assertEquals("https://github.com/appdotnet/api-spec", user.getDescription().getEntities().getLinks().get(0).getUrl());
         assertEquals(29, user.getDescription().getEntities().getLinks().get(0).getPosition());
         assertEquals(12, user.getDescription().getEntities().getLinks().get(0).getLength());
 
         assertEquals("US/Pacific", user.getTimezone());
         assertEquals("en_US", user.getLocale());
 
         assertEqualsImage(user.getAvatarImage(), 200, 200, "https://example.com/avatar_image.jpg", false);
         assertEqualsImage(user.getCoverImage(), 264, 960, "https://example.com/cover_image.jpg", false);
 
         assertEquals(ADNUser.ADNUserType.HUMAN, user.getType());
         assertEqualsDate(user.getCreatedAt(), "2012-07-16 17:23:34");
 
         assertEquals(100, user.getFollowingCount());
         assertEquals(200, user.getFollowersCount());
         assertEquals(24, user.getPostsCount());
         assertEquals(76, user.getStarsCount());
 
         assertEquals(false, user.isFollowsYou());
         assertEquals(false, user.isYouBlocked());
         assertEquals(false, user.isYouFollow());
         assertEquals(false, user.isYouMuted());
         assertEquals(true, user.isYouCanSubscribe());
         assertEquals(true, user.isYouCanFollow());
 
         assertEquals("example.com", user.getVerifiedDomain());
         assertEquals("https://alpha.app.net/mthurman", user.getCanonicalUrl());
 
         assertEqualsAnnotation(user, 0, "net.app.core.directory.blog", "url", "http://myawesomeblog.com");
     }
 
     @Test
     public void getUsers() {
        mockServer.expect(requestTo(AppdotnetTemplate.BASE_URL + "0/users/?access_token=ACCESS_TOKEN&ids=1%252C2")).andExpect(method(GET))
                 .andRespond(withSuccess(new ClassPathResource("/testdata/users.json", getClass()), MediaType.APPLICATION_JSON));
         ADNUsers users = appdotnet.usersOperations().get(Arrays.asList("1", "2"));
         assertEquals(2, users.getUsers().size());
         assertBasicUser(users.getUsers().get(0), "1", "dalton", "Dalton Caldwell");
         assertBasicUser(users.getUsers().get(1), "2", "berg", "Bryan Berg");
     }
 
     private void assertEqualsAnnotation(ADNUser user, int expectedIndex, String expectedType, String expectedValueKey, String expectedValueValue) {
         assertEquals(expectedType, user.getAnnotations().get(expectedIndex).getType());
         assertEquals(expectedValueValue, user.getAnnotations().get(expectedIndex).getValue().get(expectedValueKey));
     }
 }
