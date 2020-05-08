 package dit126.group4.group4shop.core;
 
 import java.io.File;
 import java.util.List;
 import org.junit.Test;
 import static org.junit.Assert.*;
 import org.junit.BeforeClass;
 
 /**
  *
  * @author Group4
  */
 public class TestDatabase {
 
         final static String PU = "group4_test_shop";
         static IGroup4Shop shop;
         
         @BeforeClass
         public static void before(){
               shop = Group4ShopFactory.getShop(PU);
         }
         
         @Test
         public void pointLess(){
             assertTrue(true);
         }
         
         @Test
         public void testAddProduct(){
             Product p = new Product(new Long(1),"Banan", 22.33,"Frukt", "En gul banan");
             
             shop.getProductCatalogue().add(p);
             Product found = shop.getProductCatalogue().find(new Long(1));
 
             assertTrue(found!=null);
             
             shop.getProductCatalogue().remove(p.getId());
             
             Product removedProduct = shop.getProductCatalogue().find(new Long(1));
             assertTrue(removedProduct==null);
             
         }
         
         
         
         @Test
         public void testAssignRolesToUsers(){
             Users user = new Users("emil@test.se", "Emil", "B", "qwerty");
             Roles role = new Roles("Admin", "En admin");
             
            shop.getUserregister().add(user);
             System.out.println("  TEST:  " + shop.getRolesRegister()  );
             shop.getRolesRegister().add(role);
             
             UserRoles ur = new UserRoles(user, role);
             System.out.println("Added user roles");
             shop.getUserRolesRegister().add(ur);
             assertTrue(true);
         }
         
        @Test
         public void testAddProductImage(){
             Product p = new Product(new Long(2), "Banana", 22.22,"Frukt", "En gul banan");
             shop.getProductCatalogue().add(p);
             
             File image = new File("/Users/Christian/Documents/school/webbapplikationer/WebApplikationer/Group4Shop/src/resources/banan.png");
             
             ProductImage i = new ProductImage("Test-image", p.getId(),image);
             
             shop.getProductImageConatiner().add(i);
             
             List<ProductImage> list = shop.getProductImageConatiner().find(p.getId());
             assertTrue(list.size() == 1);
             assertTrue(list.get(0).getName().equals("Test-image"));
           
         }
         
         @Test
         public void testAddAddress(){
             Address a = new Address("SWE", "GBG", "fdjbn", "dskgndgs", 2, new Long(2));
             
             shop.getAddressCatalogue().add(a);
             
             List<Address> list = shop.getAddressCatalogue().find(new Long(2));
             
             assertTrue(list.size() == 1);
             assertTrue(list.get(0).getCountry().equals("SWE"));
         }
 
 
         /*@Test 
         public void testAddAdmin(){
             Admin a = new Admin("emil@group.se", "Emil", "B", "password");
             
             shop.getAdminRegister().add(a);
             System.out.println("Test Admin");
             
             Admin a1 = shop.getAdminRegister().find("emil@group.se");
             assertTrue(true); //see if equals
             assertTrue(1 != 2 );
         }*/
         
         
 
 }
