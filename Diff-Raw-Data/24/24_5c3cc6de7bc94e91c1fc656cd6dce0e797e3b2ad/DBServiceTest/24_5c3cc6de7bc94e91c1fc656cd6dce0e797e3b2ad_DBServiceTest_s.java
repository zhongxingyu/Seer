 import Global.DBService;
 import Global.Imps.DBServiceImp;
 import Global.MessageSystem;
 import Global.MsgSystem.MessageSystemImp;
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 
 import java.sql.SQLException;
 
 /**
  * Created with IntelliJ IDEA.
  * User: MiST
  * Date: 30.11.13
  * Time: 10:21
  * To change this template use File | Settings | File Templates.
  */
 public class DBServiceTest {
     private DBService dbSerivce;
 
     final String userName = "TstUsrName123";
     final String userPassword = "VeryStrongPassword";
 
     @Before
     public void setUp() {
         MessageSystem ms = new MessageSystemImp();
         try {
             dbSerivce = new DBServiceImp(ms, "TestPlaneDB");
         } catch (SQLException e) {
             e.printStackTrace();
         }
     }
 
     @After
     public void tearDown() {
         if (dbSerivce.checkIsUserExist(userName))
             dbSerivce.deleteUser(dbSerivce.getUserIdByUserName(userName, userPassword));
     }
 
     @Test
     public void testUser() {
         //проверяем, что такого пользователя нет
         final String errUserExistText = "Test user already exists";
         Assert.assertFalse(errUserExistText, dbSerivce.checkIsUserExist(userName));
 
         //создаем нового пользователя
         final String errCreateUserText = "Create new test user error";
        Assert.assertTrue(errCreateUserText, dbSerivce.createUser(userName, userPassword));
 
         //проверяем, что пользователь создан
         final String errUserNotCreated = "No test user in database";
         Long userId = dbSerivce.getUserIdByUserName(userName, userPassword);
         Assert.assertTrue(errUserNotCreated, userId != dbSerivce.USER_NOT_EXIST);
 
         //удаляем пользователя
         final String errDeleteUser = "Delete user error";
         Assert.assertTrue(errDeleteUser, dbSerivce.deleteUser(userId));
 
         //проверяем, что пользователь удален
         final String errUserNotDeleteText = "Test user exist in database";
         Assert.assertFalse(errUserNotDeleteText, dbSerivce.checkIsUserExist(userName));
     }
 }
