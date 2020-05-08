 package com.nclodger.dao;
 
 import com.nclodger.additional.BookingViewing;
 import com.nclodger.domain.Order;
 import com.nclodger.domain.User;
 import com.nclodger.myexception.MyException;
 import com.nclodger.publicdao.UserDaoInterface;
 import org.springframework.stereotype.Component;
 
 import java.sql.*;
 import java.sql.Date;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: antoshka
  * Date: 08.10.13
  * Time: 21:24
  * To change this template use File | Settings | File Templates.
  */
 @Component("userDAO")
 public class UserDao extends AbstractRepository implements UserDaoInterface {
 
 
     public boolean insert(String _email, String _pswd, String _name, int register_confirm) throws MyException {
         return dbOperation(new WrapperDBOperation<Boolean>() {
             @Override
             public Boolean doMethod(Connection dataBase) {
                 return true;
                 /*String sql = "INSERT INTO User(id,email,pswd,name,register_confirmed)" +
                         "values" +
                         "(" + _id + "," + _email + "," + _pswd + "," + _name + ",1);";*/
             }
         });
 
     }
 
 
 
     public boolean insert(final String email, final String name) throws MyException {
         return dbOperation(new WrapperDBOperation<Boolean>() {
             @Override
             public Boolean doMethod(Connection dataBase) throws SQLException {
 
                 String sql = "INSERT INTO Users(email,username,confirm_register, id_ut, is_blocked)" +
                         "values" +
                         "(?,?,1,1,0)";
 
 
                 PreparedStatement prep = dataBase.prepareStatement(sql);
 
                 prep.setString(1, email);
                 prep.setString(2, name);
 
                 java.sql.ResultSet res = prep.executeQuery();
                 res.next();
 
 
                 return true;
             }
         });
 
     }
 
 
 
 
     public boolean confirm_register(final User _user) throws MyException {
         return dbOperation(new WrapperDBOperation<Boolean>() {
             @Override
             public Boolean doMethod(Connection dataBase) throws MyException, SQLException {
                 //dataBase.
                 Statement st = dataBase.createStatement();
                 java.sql.ResultSet res = st.executeQuery("SELECT ID_USER FROM Users WHERE " +
                         "ID_USER=" + _user.getId() + ";");
                 res.next();
                 int _id = res.getInt(1);
                 res = st.executeQuery("UPDATE User" +
                         "SET confirm_register = 1 " +
                         "WHERE ID_USER=" + _id + ";");
                 return true;
             }
         });
 
     }
 
     public boolean confirmRegisterByUserID(final int userID) throws MyException {
         return dbOperation(new WrapperDBOperation<Boolean>() {
             @Override
             public Boolean doMethod(Connection dataBase) throws MyException, SQLException {
                 Statement st = dataBase.createStatement();
                 PreparedStatement prepGetUserID = dataBase.prepareStatement(
                         "SELECT ID_USER FROM Users WHERE ID_USER=?"
                 );
                 prepGetUserID.setInt(1, userID);
                 java.sql.ResultSet res = prepGetUserID.executeQuery();
                 try {
                     res.next();
                     int id = res.getInt(1);
                     PreparedStatement prepSetUserConfirmStatus = dataBase.prepareStatement(
                             "UPDATE Users SET CONFIRM_REGISTER =1 WHERE ID_USER=?"
                     );
                     prepSetUserConfirmStatus.setInt(1, userID);
                     java.sql.ResultSet execUpdation = prepSetUserConfirmStatus.executeQuery();
                     execUpdation.next();
                 } catch (Exception ex) {
                     throw new MyException(ex.getMessage());
                 }
                 return true;
             }
         });
 
     }
 
 
     @Override
     public boolean insert(final User user) throws MyException {
         //Tested valid sql
         //INSERT INTO "User" (id_user,username,email,pswd,user_type,is_blocked) values (0,'reshet','reshet.ukr@gmail.com','tratata','customer',0);
         return dbOperation(new WrapperDBOperation<Boolean>() {
             @Override
             public Boolean doMethod(Connection dataBase) throws SQLException, MyException {
                 PreparedStatement prep = dataBase.prepareStatement(
                         "INSERT INTO Users (USERNAME,EMAIL,PSWD,CONFIRM_REGISTER,ID_UT,IS_BLOCKED) values (?,?,?,0,1,0)"
                 );
                 prep.setString(1, user.getName());
                 prep.setString(2, user.getEmail());
                 prep.setString(3, user.getPswd());
 
                 java.sql.ResultSet res = prep.executeQuery();
                 res.next();
                 return true;
             }
         });
     }
 
 
 
 
     @Override
     public boolean updateForSM(final int userID) throws MyException {
        return dbOperation(new WrapperDBOperation<Boolean>() {
            @Override
            public Boolean doMethod(Connection dataBase) throws SQLException, MyException {
                PreparedStatement prep = dataBase.prepareStatement(
                        "UPDATE Users SET VIP=? WHERE ID_USER=?"
                );
 
                prep.setInt(1, 1);
                prep.setInt(2, userID);
 
                java.sql.ResultSet res = prep.executeQuery();
                res.next();
                return true;  //To change body of implemented methods use File | Settings | File Templates.
            }
        });
     }
 
 
 
     @Override
     public boolean updateMakeUnvip(final int userID) throws MyException {
         return dbOperation(new WrapperDBOperation<Boolean>() {
             @Override
             public Boolean doMethod(Connection dataBase) throws SQLException, MyException {
                 PreparedStatement prep = dataBase.prepareStatement(
                         "UPDATE Users SET VIP=? WHERE ID_USER=?"
                 );
 
                 prep.setInt(1, 0);
                 prep.setInt(2, userID);
 
                 java.sql.ResultSet res = prep.executeQuery();
                 res.next();
                 return true;  //To change body of implemented methods use File | Settings | File Templates.
             }
         });
     }
 
 
     @Override
     public boolean makeBlock(final int userID) throws MyException {
         return dbOperation(new WrapperDBOperation<Boolean>() {
             @Override
             public Boolean doMethod(Connection dataBase) throws SQLException, MyException {
                 PreparedStatement prep = dataBase.prepareStatement(
                         "UPDATE Users SET IS_BLOCKED=? WHERE ID_USER=?"
                 );
 
                 prep.setInt(1, 1);
                 prep.setInt(2, userID);
 
                 java.sql.ResultSet res = prep.executeQuery();
                 res.next();
                 return true;  //To change body of implemented methods use File | Settings | File Templates.
             }
         });
     }
 
 
 
     @Override
     public boolean makeUnBlock(final int userID) throws MyException {
         return dbOperation(new WrapperDBOperation<Boolean>() {
             @Override
             public Boolean doMethod(Connection dataBase) throws SQLException, MyException {
                 PreparedStatement prep = dataBase.prepareStatement(
                         "UPDATE Users SET IS_BLOCKED=? WHERE ID_USER=?"
                 );
 
                 prep.setInt(1, 0);
                 prep.setInt(2, userID);
 
                 java.sql.ResultSet res = prep.executeQuery();
                 res.next();
                 return true;  //To change body of implemented methods use File | Settings | File Templates.
             }
         });
     }
 
     @Override
     public boolean update(User _user) {
         //To change body of implemented methods use File | Settings | File Templates.
         //TODO method
         return false;
     }
 
     @Override
     public boolean delete(String email) throws MyException {
         return false;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public boolean getUser(final String email, final String password) throws MyException {
         return dbOperation(new WrapperDBOperation<Boolean>() {
 
             @Override
             public Boolean doMethod(Connection dataBase) throws MyException, SQLException {
                 PreparedStatement prep = dataBase.prepareStatement(
                         "SELECT ID_USER FROM Users WHERE email=? AND pswd= ?"
                 );
                 prep.setString(1, email);
                 prep.setString(2, password);
 
                 java.sql.ResultSet res = prep.executeQuery();
 //                res.next();
 //                int exist = res.getInt(1);
 
               //  boolean answer = false;
                 boolean answer = true;
 //                if (exist > 0) {
 //                    answer = true;
 //                }
 
                 // A correct way to check if there is any data in the ResultSet
                 if (!res.isBeforeFirst() ) {
                    answer = false;
                 }
 
                 return answer;
                 //return null;  //To change body of implemented methods use File | Settings | File Templates.
             }
         });
 
     }
 
     @Override
     public boolean checkPswd(int userID, String password) throws MyException {
         return false;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public List<User> getAllUsers() throws MyException {
 
         return dbOperation(new WrapperDBOperation<List<User>>() {
 
             @Override
             public List<User> doMethod(Connection dataBase) throws MyException, SQLException {
                 PreparedStatement prep = dataBase.prepareStatement(
                         "SELECT ID_USER,USERNAME,ID_UT,EMAIL,IS_BLOCKED,BONUS,VIP FROM Users"
                 );
 
                 java.sql.ResultSet results = prep.executeQuery();
                 List<User> uList = new ArrayList<User>();
                 while (results.next()) {
 
                     Integer id = results.getInt(1);
                     String uname = results.getString(2);
                     Integer utype = results.getInt(3);
                     String email = results.getString(4);
                     Integer is_blocked = results.getInt(5);
                     Integer bonus = results.getInt(6);
                     Integer vip = results.getInt(7);
 
 
                     User user = new User(id, uname);
                     user.setId_ut(utype);
                     user.setEmail(email);
                     user.setIs_blocked(is_blocked);
                     user.setBonus(bonus);
                     user.setVip(vip);
 
                     uList.add(user);
                 }
                 return uList;
             }
         });
     }
 
 
 
     public User getUserObj(final String email, final String password) throws MyException {
                 return dbOperation(new WrapperDBOperation<User>() {
 
                     @Override
                     public User doMethod(Connection dataBase) throws MyException, SQLException {
                         PreparedStatement prep = dataBase.prepareStatement(
                                 "SELECT ID_USER,USERNAME,ID_UT,EMAIL, CONFIRM_REGISTER, IS_BLOCKED, VIP FROM Users WHERE email=? AND pswd= ?"
                         );
                         prep.setString(1, email);
                         prep.setString(2, password);
 
                         java.sql.ResultSet res = prep.executeQuery();
                         res.next();
                         int id = res.getInt(1);
                         boolean answer = false;
                         if (id > 0) {
                             //answer = true;
                             String uname = res.getString(2);
                             Integer utype = res.getInt(3);
                             String email = res.getString(4);
                             Integer confirmed = res.getInt(5);
                             Integer isBlocked = res.getInt(6);
                             Integer vip = res.getInt(7);
 
                             //String pswd = res.getString(5);
                             User user = new User(id, uname);
                             user.setId_ut(utype);
 
                             user.setEmail(email);
                             user.set_confirm_register(confirmed);
                             user.setIs_blocked(isBlocked);
                             user.setVip(vip);
                             //user.setPswd();
                             return user;
                         }
 
                         return null;
                         //return null;  //To change body of implemented methods use File | Settings | File Templates.
                     }
                 });
 
             }
 
 
 
 
 
 
     public User getUserObj2(final String email) throws MyException {
         return dbOperation(new WrapperDBOperation<User>() {
 
             @Override
             public User doMethod(Connection dataBase) throws MyException, SQLException {
                 PreparedStatement prep = dataBase.prepareStatement(
                         "SELECT ID_USER,USERNAME,ID_UT,EMAIL, CONFIRM_REGISTER, IS_BLOCKED, VIP FROM Users WHERE email=?"
                 );
                 prep.setString(1, email);
        //         prep.setString(2, password);
 
                 java.sql.ResultSet res = prep.executeQuery();
                 res.next();
                 int id = res.getInt(1);
                 boolean answer = false;
                 if (id > 0) {
                     //answer = true;
                     String uname = res.getString(2);
                     Integer utype = res.getInt(3);
                     String email = res.getString(4);
                     Integer confirmed = res.getInt(5);
                     Integer isBlocked = res.getInt(6);
                     Integer vip = res.getInt(7);
 
                     //String pswd = res.getString(5);
                     User user = new User(id, uname);
                     user.setId_ut(utype);
 
                     user.setEmail(email);
                     user.set_confirm_register(confirmed);
                     user.setIs_blocked(isBlocked);
                     user.setVip(vip);
                     //user.setPswd();
                     return user;
                 }
 
                 return null;
                 //return null;  //To change body of implemented methods use File | Settings | File Templates.
             }
         });
 
     }
 
             @Override
             public User find(final int id) throws MyException {
                 //return null;  //To change body of implemented methods use File | Settings | File Templates.
                 return dbOperation(new WrapperDBOperation<User>() {
 
                     @Override
                     public User doMethod(Connection dataBase) throws MyException, SQLException {
                         PreparedStatement prep = dataBase.prepareStatement(
                                 "SELECT * FROM Users WHERE ID_USER=?"
                         );
                         prep.setInt(1, id);
                         //prep.setString(2,password);
 
                         java.sql.ResultSet res = prep.executeQuery();
                         res.next();
                         int exist = res.getInt(1);
                         User user = new User(res.getInt(1), res.getString(2), res.getString(3), res.getString(4), res.getInt(5));
                         return user;
                         //return null;  //To change body of implemented methods use File | Settings | File Templates.
                     }
                 });
             }
 
         @Override
         public int getUserId(final String email) throws MyException {
             /**
              * Working sql query example:
              * SELECT ID_SM FROM MANAGER WHERE MANAGER.ID_USER IN
              *      (SELECT ID_USER FROM User WHERE User.EMAIL = 'reshet.ukr@gmail.com')
              */
             return dbOperation(new WrapperDBOperation<Integer>() {
                 @Override
                 public Integer doMethod(Connection dataBase) throws SQLException, MyException {
                     PreparedStatement prep = dataBase.prepareStatement(
                             "SELECT ID_USER FROM Users WHERE Users.EMAIL=?"
                     );
                     prep.setString(1, email);
                     java.sql.ResultSet res = prep.executeQuery();
                     res.next();
                     int idSm = res.getInt(1);
                     return idSm;
                 }
             });
         }
 
     @Override
     public boolean updatePswd(final User u) throws MyException {
         return dbOperation(new WrapperDBOperation<Boolean>() {
             @Override
             public Boolean doMethod(Connection dataBase) throws SQLException, MyException {
                 PreparedStatement prep = dataBase.prepareStatement(
                         "UPDATE Users SET PSWD=? WHERE ID_USER=?"
                 );
 
                 prep.setString(1, u.getPswd());
                 prep.setInt(2, u.getId());
 
                 java.sql.ResultSet res = prep.executeQuery();
                 res.next();
                 return true;
             }
         });
     }
 
     //check if such email is exist in databases
     @Override
     public boolean isExistEmail(final String email) throws MyException {
         return dbOperation(new WrapperDBOperation<Boolean>() {
 
             @Override
             public Boolean doMethod(Connection dataBase) throws MyException, SQLException {
                 PreparedStatement prep = dataBase.prepareStatement(
                         "SELECT ID_USER FROM Users WHERE EMAIL=?"
                 );
                 prep.setString(1, email);
                 java.sql.ResultSet res = prep.executeQuery();
                /* res.next();
                 int check = res.getInt(1);
                 boolean answer = false;
                 if (check == 0) {
                     answer = false;
                 } else {
                     answer = true;
                 }
                 return answer;*/
 
                 // Correct way
                 boolean answer = true;
 
                 if(!res.isBeforeFirst()){
                     answer = false;
                 }
 
                 return answer;
             }
         });
 
     }
 
 
     @Override
     public boolean delete(final int userId) throws MyException {
         return dbOperation(new WrapperDBOperation<Boolean>() {
 
             @Override
             public Boolean doMethod(Connection dataBase) throws MyException, SQLException {
 
                 PreparedStatement prep5 = dataBase.prepareStatement(
                         "SELECT ID_SM FROM MANAGER WHERE ID_USER=?"
                 );
                 prep5.setInt(1,userId);
                 java.sql.ResultSet res = prep5.executeQuery();
 
                 if(res.next()){
                     int smId = res.getInt(1);
 
                     PreparedStatement prep7 = dataBase.prepareStatement(
                             "DELETE FROM PROMOCODE WHERE ID_SM=?"
                     );
                     prep7.setInt(1,smId);
                     prep7.executeUpdate();
 
                     PreparedStatement prep8 = dataBase.prepareStatement(
                             "DELETE FROM HOTEL_MANAGER WHERE ID_SM=?"
                     );
                     prep8.setInt(1,smId);
                     prep8.executeUpdate();
 
                     PreparedStatement prep9 = dataBase.prepareStatement(
                             "DELETE FROM MANAGER WHERE ID_SM=?"
                     );
                     prep9.setInt(1,smId);
                     prep9.executeUpdate();
 
                     PreparedStatement prep11 = dataBase.prepareStatement(
                             "DELETE FROM ORDERS WHERE ID_SM=?"
                     );
                     prep11.setInt(1, smId);
                     prep11.executeUpdate();
 
                 }
 
 
                 PreparedStatement prep10 = dataBase.prepareStatement(
                         "DELETE FROM ORDERS WHERE ID_USER=?"
                 );
                 prep10.setInt(1, userId);
                 prep10.executeUpdate();
 
                 PreparedStatement prep1 = dataBase.prepareStatement(
                         "DELETE FROM CONFIRM WHERE ID_USER=?"
                 );
                 prep1.setInt(1, userId);
                 prep1.executeUpdate();
 
 
                 PreparedStatement prep2 = dataBase.prepareStatement(
                         "DELETE FROM USER_TYPE WHERE ID_USER=?"
                 );
                 prep2.setInt(1, userId);
                 prep2.executeUpdate();
 
 
 
                 PreparedStatement prep3 = dataBase.prepareStatement(
                         "DELETE FROM USERS WHERE ID_USER=?"
                 );
                 prep3.setInt(1,userId);
                 prep3.executeUpdate();
 
 
                 return true;
             }
         });
 
 
     }
 
     @Override
     public String getPasswordbyEmail(final String email) throws MyException {
         return dbOperation(new WrapperDBOperation<String >() {
 
             @Override
             public String  doMethod(Connection dataBase) throws MyException, SQLException {
                 PreparedStatement prep = dataBase.prepareStatement(
                         "SELECT  PSWD FROM Users WHERE EMAIL=?"
                 );
                 prep.setString(1, email);
                 java.sql.ResultSet res = prep.executeQuery();
                 res.next();
                 String pswd = res.getString(1);
                 return pswd;
 
             }
         });
     }
 
     @Override
     public ArrayList<BookingViewing> getPastOrder(final int userID) throws MyException {
         return dbOperation(new WrapperDBOperation<ArrayList<BookingViewing> >() {
 
             @Override
             public ArrayList<BookingViewing>   doMethod(Connection dataBase) throws MyException, SQLException {
                PreparedStatement prep = dataBase.prepareStatement(
                         "SELECT T,HOTEL.NAME_H,HOTEL.CITY, DO,SD,ED,P FROM( " +
                                 "        SELECT  ORDERS.ID_ORDER,ORDERS.PRICE AS P,ORDERS.DATE_ORDER AS DO," +
                                 " ORDERS.START_DATE AS SD,ORDERS.end_date AS ED,ACCOMMODATION.ID_ACC,ACCOMMODATION.TYPE AS T,HOTEL.ID_HOTEL AS IDHOT FROM ORDERS " +
                                 "          LEFT JOIN ACCOMMODATION " +
                                 "            ON ORDERS.ID_ACC=ACCOMMODATION.ID_ACC " +
                                 "          LEFT JOIN HOTEL " +
                                 "            ON ACCOMMODATION.ID_HOTEL = HOTEL.ID_HOTEL " +
                                 "        WHERE ORDERS.ID_USER=?" +
                                 "      ),HOTEL " +
                                 " WHERE HOTEL.ID_HOTEL=IDHOT " +
                                 " ORDER BY DO ASC"
                 );
                 prep.setInt(1,userID);
                 java.sql.ResultSet results = prep.executeQuery();
 /*                res.next();*/
                 ArrayList<BookingViewing> uList = new ArrayList<BookingViewing>();
                 while (results.next()) {
 
                     String type = results.getString(1);
                     String hotelName = results.getString(2);
                     String hotelCity = results.getString(3);
                     String dateOrder = results.getDate(4).toString();
                     String startOrder = results.getDate(5).toString();
                     String endOrder = results.getDate(6).toString();
                     double price = results.getDouble(7);
 
 
                     uList.add(new BookingViewing(type,hotelName,hotelCity,dateOrder,startOrder,endOrder,price));
                 }
                 return uList;
 
             }
         });
     }
 
     @Override
     public boolean saveOrder(final Order ord) throws MyException {
         return dbOperation(new WrapperDBOperation<Boolean>() {
             @Override
             public Boolean doMethod(Connection dataBase) throws MyException, SQLException {
                 PreparedStatement prep = dataBase.prepareStatement(
                         "INSERT INTO ORDERS (ID_USER,DATE_ORDER,ID_ACC,PRICE,START_DATE,END_DATE,ID_PC,DISCOUNT,ID_SM) values (?,?,?,?,?,?,?,?,?)"
                 );
 
                 prep.setInt(1, ord.getUserid());
                 prep.setDate(2, new Date(new java.util.Date().getTime()));
                 //TODO real acc id
                prep.setInt(3, 2);
                 //TODO change price to double, think on currencies
                 prep.setInt(4, (int) Math.round(ord.getFinal_price()));
 
 
                 DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
                 java.util.Date date1 = null;
                 java.util.Date date2 = null;
                 try {
                     if(ord.getStart_date()!=null)
                         date1 = formatter.parse(ord.getStart_date());
                     if(ord.getEnd_date()!=null)
                         date2 = formatter.parse(ord.getEnd_date());
                 } catch (ParseException e) {
                     e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                 }
                 if(date1!=null){
                     java.sql.Date sqlDate1 = new java.sql.Date(date1.getTime());
                     prep.setDate(5, sqlDate1);
 
                 }
                 if(date2!=null){
                     java.sql.Date sqlDate2 = new java.sql.Date(date2.getTime());
                     prep.setDate(6, sqlDate2);
                 }
 
                  if(ord.getPromo()!=null)
                  {
                      prep.setInt(7,ord.getPromo().getId_pc());
                  }else{
                      prep.setNull(7,7);
                  }
                 //TODO real user discount
                  prep.setInt(8,(int)Math.round(ord.getDiscount()));
                  prep.setInt(9,ord.getPrice_list_choice());
 
 
                 java.sql.ResultSet res = prep.executeQuery();
                 res.next();
                 return true;
 
             }
         });
     }
 }
