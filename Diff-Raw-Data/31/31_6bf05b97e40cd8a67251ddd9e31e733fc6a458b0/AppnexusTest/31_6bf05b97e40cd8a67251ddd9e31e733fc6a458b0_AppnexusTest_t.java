 package appnexus;
 
 import static org.apache.commons.lang.StringUtils.isBlank;
 import static org.junit.Assert.assertNotNull;
 
 import org.junit.BeforeClass;
 import org.junit.Test;
 
import appnexus.api.Member;

 public class AppnexusTest {
   
   private static final String USERNAME = System.getProperty("appnexus.username");
   
   private static final String PASSWORD = System.getProperty("appnexus.password");
   
   @BeforeClass
   public static void setup() {
     if (isBlank(USERNAME) || isBlank(PASSWORD)) {
       throw new IllegalArgumentException("Please set the appnexus.username and appnexus.password system environment variables");
     }
     Appnexus.initSandbox(USERNAME, PASSWORD);
   }
   
   @Test
   public void testAccessTokenHasBeenSet() {
     assertNotNull(Appnexus.accountDetails.getAccessToken());
   }
  
  @Test
  public void testGetMember() {
    Member member = Appnexus.api.getMember();
    assertNotNull(member.getId());
    assertNotNull(member.getName());
  }
 
 }
