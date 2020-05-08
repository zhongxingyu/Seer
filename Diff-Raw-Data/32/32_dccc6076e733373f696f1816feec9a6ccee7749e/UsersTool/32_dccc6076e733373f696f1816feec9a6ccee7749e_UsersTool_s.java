 package twitterexample;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.hbase.client.HTable;
 import org.apache.hadoop.hbase.client.HTableInterface;
 import org.apache.log4j.Logger;
 import twitterexample.dao.UsersDAO;
 import twitterexample.model.User;
 
 import java.io.IOException;
 import java.util.List;
 
 /**
  * AbstractUser: ChenLong
  * Created Date: 9/22/13 6:38 下午
  * Description:
  */
 public class UsersTool {
 
     private static final Logger log = Logger.getLogger(UsersTool.class);
 
     public static void main(String[] args) throws IOException {
         Configuration myConf = new Configuration();
         myConf.set("hbase.master","101.227.253.67:44801");
         myConf.set("hbase.zookeeper.property.clientPort", "2181");
         myConf.set("hbase.zookeeper.quorum", "101.227.253.67");
         HTableInterface usersTable = new HTable(myConf, "users");
 
        String userName = "ambo2";
 
         UsersDAO dao = new UsersDAO(usersTable);
 
         List<User> users = dao.getUsers();
         log.info(String.format("Found %s users.", users.size()));
         for(User u1 : users) {
             System.out.println(u1);
         }
 
         User u = dao.getUser(userName);
 
 
         if (u == null)
         {
             System.out.println(String.format("Not found user %s", userName));
            dao.addUser(userName, userName,"hbase@ambo.com", "ambo_pwd");
         }
         else
         {
             System.out.println(String.format("Found user %s", userName));
         }
 
         users = dao.getUsers();
         log.info(String.format("Found %s users.", users.size()));
         for(User u1 : users) {
             System.out.println(u1);
         }
 
         usersTable.close();
 
     }
 
 }
