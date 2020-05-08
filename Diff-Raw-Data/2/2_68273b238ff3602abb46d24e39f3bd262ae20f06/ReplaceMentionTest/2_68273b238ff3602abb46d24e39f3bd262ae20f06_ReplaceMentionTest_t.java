 package sage;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 import sage.domain.repository.UserRepository;
 import sage.domain.service.TweetPostService;
 import sage.entity.User;
 
 public class ReplaceMentionTest {
     @Test
     public void test() {
         String content = "@Admin @Bethia XXX@Admin@Bethia@CentOS社区 ";
         content = "@Admin @Admin@Admin@Admin@Admin@Admin @Admin@Admin @Admin@Admin@Admin @Admi";
         UserRepository ur = new UserRepository() {
             @Override
             public User findByName(String name) {
                 User user = new User();
                user.setId(1000L);
                 user.setName(name);
                 return user;
             }
         };
         String output = TweetPostService.replaceMention(content, 0, new StringBuilder(), ur);
         Assert.assertEquals(output,
                 "@Admin#1000 @Admin@Admin@Admin@Admin@Admin#1000 @Admin@Admin#1000 @Admin@Admin@Admin#1000 @Admi");
     }
 }
