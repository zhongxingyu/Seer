 package de.fnordeingang.fnordapp.test;
 
 import android.test.AndroidTestCase;
 import de.fnordeingang.fnordapp.util.ServiceClient;
 import de.fnordeingang.fnordapp.util.dto.Cart;
 import de.fnordeingang.fnordapp.util.dto.EanArticle;
 import de.fnordeingang.fnordapp.util.dto.UserProfile;
 import junit.framework.Assert;
 
 import java.math.BigDecimal;
 import java.util.List;
 
 /**
  * User: vileda
  * Date: 22.10.11
  * Time: 23:10
  */
 public class ServiceClientTest extends AndroidTestCase {
   //private static final String HOST = "172.29.1.47:8080";
   private static final String HOST = "services.fnordeingang.de";
 
   public void testGetArticleInfo() throws Exception {
     ServiceClient client = new ServiceClient(HOST);
     EanArticle eanArticle = client.getArticleInfo("30058569");
     Assert.assertTrue("returned article is the one we searched for","Zigarettendrehpapier".equals(eanArticle.getDescription()));
   }
 
   public void testEmptyCart() throws Exception {
     ServiceClient client = new ServiceClient(HOST);
     EanArticle eanArticle = client.getArticleInfo("30058569");
     client.emptyCart();
     client.addArticleToCart(eanArticle);
     Cart cart = client.getCurrentCart();
     Assert.assertTrue("cart size is equals one", cart.getArticles().size() == 1);
     Assert.assertTrue("first cart item is the one we searched for","Zigarettendrehpapier".equals(cart.getArticle(0).getDescription()));
     client.emptyCart();
     cart = client.getCurrentCart();
     Assert.assertTrue("cart size is zero", cart.getArticles().size() == 0);
   }
 
   public void testGetProfile() throws Exception {
     ServiceClient client = new ServiceClient(HOST);
     UserProfile userProfile = client.getProfile("viledatest","1234","ffff-ffff-ffff-ffff");
     Assert.assertTrue("balance is equals 10 on test account", userProfile.getBalance().equals(BigDecimal.valueOf(10.0)));
     Assert.assertTrue("user is the one we provided", "viledatest".equals(userProfile.getName()));
   }
 
   public void testAddArticleToCart() throws Exception {
     ServiceClient client = new ServiceClient(HOST);
     EanArticle eanArticle = client.getArticleInfo("30058569");
     client.emptyCart();
     client.addArticleToCart(eanArticle);
     Cart cart = client.getCurrentCart();
     Assert.assertTrue("cart size is equals one", cart.getArticles().size() == 1);
     Assert.assertTrue("first cart item is the one we searched for","Zigarettendrehpapier".equals(cart.getArticle(0).getDescription()));
   }
 
   public void testRemoveArticleFromCart() throws Exception {
     ServiceClient client = new ServiceClient(HOST);
     client.emptyCart();
     EanArticle eanArticle = client.getArticleInfo("30058569");
     client.addArticleToCart(eanArticle);
     Cart cart = client.getCurrentCart();
     Assert.assertTrue("cart size is equals 1", cart.getArticles().size() == 1);
     client.removeArticleFromCart("30058569");
     cart = client.getCurrentCart();
     Assert.assertTrue("cart size is equals 0", cart.getArticles().size() == 0);
   }
 
   public void testGetAllArticles() throws Exception {
     ServiceClient client = new ServiceClient(HOST);
     List<EanArticle> articles = client.getAllArticles();
     Assert.assertTrue("there are more than zero articles in the list", articles.size() > 0);
   }
 
   public void testToggleStatus() throws Exception {
     ServiceClient client = new ServiceClient(HOST);
     int status = client.toggleStatus("viledatest","1234");
     Assert.assertTrue("status is equals one", status == 1);
   }
 }
