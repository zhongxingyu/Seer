 package org.devemu.sql.entity.manager;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import org.devemu.sql.dao.DAO;
 import org.devemu.sql.entity.Account;
 
 import com.google.common.base.Splitter;
 import com.google.common.collect.Iterables;
 
 public class AccountManager {
 	public static Account findByName(String arg0) {
 		Account loc3 = DAO.getAccountDAO().find(arg0);
 		return loc3;
 	}
 	
 	public static long getAboTime(Account arg0) {
 		if(arg0.getAboTime() == 0)
 			return 0;
 		return (arg0.getAboTime() - System.currentTimeMillis());
 	}
 	
 	public static Account create(ResultSet set) {
         try {
             Account acc = new Account();
             acc.setGuid(set.getInt("id"));
             acc.setName(set.getString("name"));
             acc.setPassword(set.getString("password"));
             acc.setPseudo(set.getString("pseudo"));
             acc.setQuestion(set.getString("question"));
             acc.setAboTime(set.getLong("aboTime"));
             String s = set.getString("players");
            String[] s1 = Iterables.toArray(Splitter.on(";").split(s), String.class);
             for (String s2 : s1) {
                String[] s3 = Iterables.toArray(Splitter.on(":").split(s2), String.class);
                acc.addPlayer(Integer.parseInt(s3[1]));
             }
             return acc;
         } catch (SQLException ex) {
             ex.printStackTrace();
         }
         return null;
     }
     
     public static Account create(String[] set) {
         Account acc = new Account();
         acc.setGuid(Integer.parseInt(set[0]));
         acc.setName(set[1]);
         acc.setPassword(set[2]);
         acc.setPseudo(set[3]);
         acc.setQuestion(set[4]);
         acc.setAboTime(Long.parseLong(set[5]));
         /*String s = set[6];
         String[] s1 = StringUtils.split(s, '&');
         for (String s2 : s1) {
             String[] s3 = StringUtils.split(s2, ':');
             if (s3.length < 2) {
                 continue;
             }
             acc.setNbrCharacter(Integer.parseInt(s3[0]), Integer.parseInt(s3[1]));
         }*/
         //TODO: Character load
         return acc;
     }
 }
