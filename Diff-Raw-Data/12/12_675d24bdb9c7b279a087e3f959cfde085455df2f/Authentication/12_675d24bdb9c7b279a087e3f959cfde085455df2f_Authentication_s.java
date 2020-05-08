 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package stockexserver;
 
 import java.io.Serializable;
 import java.rmi.RemoteException;
 import java.rmi.server.UnicastRemoteObject;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import stockEx.Client;
 import stockEx.ConfigManager;
 import stockEx.IAuthentication;
 import stockexserver.dataAccess.DataAccess;
 
 /**
  *
  * @author harsimran.maan
  */
 public class Authentication extends UnicastRemoteObject implements IAuthentication, Serializable
 {
 
     private static final long serialVersionUID = 2222L;
 
     public Authentication() throws RemoteException
     {
         super();
     }
 
     @Override
     public Client init(String userName, boolean isAdmin) throws RemoteException
     {
         Client client;
         if (userName.matches("[a-zA-Z0-9]+"))
         {
             try
             {
                ResultSet set = DataAccess.getResultSet("SELECT * FROM Client where clientName = " + userName);
                if (set != null || set.next())
                 {
                     client = new Client(userName);
                     client.setIsAdmin(set.getBoolean("isAdmin"));
                     client.setBalance(set.getInt("balance"));
                 }
                 else
                 {
                     int balance = Integer.valueOf(ConfigManager.getInstance().getPropertyValue("initialBalance"));
                     DataAccess.updateOrInsertSingle("INSERT INTO Client VALUES('" + userName + "'," + balance + "," + isAdmin + " )");
                     client = new Client(userName);
                     client.setBalance(balance);
                     client.setIsAdmin(isAdmin);
                 }
                 return client;
             }
             catch (SQLException ex)
             {
                 Logger.getLogger(Authentication.class.getName()).log(Level.SEVERE, null, ex);
                 throw new RemoteException("Something went worng while fetching your details. Write to the admin about this issue.");
             }
         }
         else
         {
             throw new RemoteException("Username should be aplhanumeric only.");
         }
     }
 }
