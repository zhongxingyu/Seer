 package dit126.group4.group4shop.core;
 
 import org.junit.Test;
 import static org.junit.Assert.*;
 import org.junit.BeforeClass;
 
 /**
  *
  * @author Group4
  */
 public class TestDatabase {
     
     final static String PU = "group4_shop";
     static IGroup4Shop shop;
     
     @BeforeClass
     public static void before(){
         shop = Group4ShopFactory.getShop(PU);
     }
     
     
     
     @Test
     public void testAddProduct(){
         Product p = new Product(new Long(454),"Banan", 22.33,"Frukt", "En gul banan");
         
         shop.getProductCatalogue().add(p);
         Product found = shop.getProductCatalogue().find(new Long(454));
         
         assertTrue(found!=null);
         
         shop.getProductCatalogue().remove(p.getId());
         
         Product removedProduct = shop.getProductCatalogue().find(new Long(1));
         assertTrue(removedProduct==null);
         
     }
     
     
     
     @Test
     public void testAssignRolesToUsers(){
         Users user1 = new Users("emil@tes.se", "Emil", "B", "qwerty");
         Users user2 = new Users("emilb@tes.se", "Bo", "Botest", "qwerty");
         Roles role1 = new Roles("admin", "En admin");
         Roles role2 = new Roles("user", "En user");
         
         shop.getUserRegister().add(user1);
         shop.getUserRegister().add(user2);
         System.out.println("  TEST:  " + shop.getRolesRegister()  );
         shop.getRolesRegister().add(role1);
         shop.getRolesRegister().add(role2);
         
         UserRoles ur1 = new UserRoles(user1, role1);
         UserRoles ur2 = new UserRoles(user2, role2);
         shop.getUserRolesRegister().add(ur1);
         shop.getUserRolesRegister().add(ur2);
         
         //Now find user!
         Users found = shop.getUserRegister().find("emil@tes.se");
         assertTrue(found.getFirstName().equals("Emil"));
     }
     
     @Test
     public void testAddUserAndAddress(){
         //Create user
         Users user = new Users("ad@e.se", "EttLångtNamn", "EttFintEfternamn", "qwerty");
         Users user1 = new Users("ads@e.se", "Emil", "Bogren", "qwerty");
         Users user2 = new Users("asd@e.se", "Erik", "Forsberg", "qwerty");
         Users user3 = new Users("asdfg@e.se", "Christian", "Svensson", "qwerty");
         Users user4 = new Users("asdfgh@e.se", "David", "Oksarsson", "qwerty");
         Users user5 = new Users("asdfghj@e.se", "Någonannan", "FreeInternet", "qwerty");
         
         shop.getUserRegister().add(user);
         shop.getUserRegister().add(user1);
         shop.getUserRegister().add(user2);
         shop.getUserRegister().add(user3);
         shop.getUserRegister().add(user4);
         shop.getUserRegister().add(user5);
         
         Roles role = shop.getRolesRegister().get("user");
         
         Address address = new Address("Sweden", "Göteborg", "41250", "Åvägen", "15", "ad@e.se");
         Address address1 = new Address("Sweden", "Göteborg", "41260", "Skånevägen", "16", "ads@e.se");
         Address address2 = new Address("Sweden", "Göteborg", "41270", "Ullevigatan", "17", "asd@e.se");
         Address address3 = new Address("Sweden", "Göteborg", "41280", "Gibraltargatan", "18", "addfg@e.se");
         Address address4 = new Address("Sweden", "Göteborg", "41290", "Engata", "19", "adfgh@e.se");
         Address address5 = new Address("Sweden", "Göteborg", "41340", "Ekgatan", "10", "adfghj@e.se");
         
         
         
         UserRoles ur1 = new UserRoles(user, role);
         UserRoles ur2 = new UserRoles(user1, role);
         UserRoles ur3 = new UserRoles(user2, role);
         UserRoles ur4 = new UserRoles(user3, role);
         UserRoles ur5 = new UserRoles(user4, role);
         UserRoles ur6 = new UserRoles(user5, role);
         
         shop.getAddressCatalogue().add(address);
         shop.getAddressCatalogue().add(address1);
         shop.getAddressCatalogue().add(address2);
         shop.getAddressCatalogue().add(address3);
         shop.getAddressCatalogue().add(address4);
         shop.getAddressCatalogue().add(address5);
         
         shop.getUserRolesRegister().add(ur1);
         shop.getUserRolesRegister().add(ur2);
         shop.getUserRolesRegister().add(ur3);
         shop.getUserRolesRegister().add(ur4);
         shop.getUserRolesRegister().add(ur5);
         shop.getUserRolesRegister().add(ur6);
         
         assertTrue(true);
     }
     
 }
