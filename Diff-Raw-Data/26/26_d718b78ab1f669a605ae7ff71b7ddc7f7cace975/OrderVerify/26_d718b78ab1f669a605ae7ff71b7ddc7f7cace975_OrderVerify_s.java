 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package wsc_application;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import javax.swing.JOptionPane;
 
 /**
  *
  * @author Bradley Clawson
  */
 public class OrderVerify {
     private int VERID;
     private Order order;
     private Employee verifiedBy;
     private int nameCheck;
     private int accountCheck;
     private int mediaCheck;
     private int contentCheck;
     private int paymentCheck;
     private int depositCheck;
     private String nameFailComment;
     private String accountFailComment;
     private String mediaFailComment;
     private String contentFailComment;
     private String paymentFailComment;
     private String depositFailComment;
     private String correctiveActionComment;
     
     public OrderVerify(){
         
     }
     public OrderVerify(int verID, Employee verBy, Order ord, Boolean nameChk,
             Boolean acctChk, Boolean medChk, Boolean contChk, Boolean payChk,
             Boolean depChk, String nameFail, String acctFail, String medFail,
             String contFail, String payFail, String depFail,
             String corrActComm){
         setVerID(verID);
         setOrder(ord);
         setVerifiedBy(verBy);
         setNameCheck(nameChk);
         setAccountCheck(acctChk);
         setMediaCheck(medChk);
         setContentCheck(contChk);
         setPaymentCheck(payChk);
         setDepositCheck(depChk);
         setNameFailComment(nameFail);
         setAccountFailComment(acctFail);
         setMediaFailComment(medFail);
         setContentFailComment(contFail);
         setPaymentFailComment(payFail);
         setDepositFailComment(depFail);
         setCorrectiveActionComment(corrActComm);        
         }
     
     public Boolean checkExistBy(String column, int id){
         Boolean ovExist = false;
         OrderVerify ov = null;
          ResultSet rs;
         MysqlConn mysql = new MysqlConn();
         String query = "Select * from cis470.ORDERVERIFY WHERE "
                     + column + " = " + id;
                     
         rs = mysql.doQuery(query);
         try {
             if (rs.next()){
                 ovExist = true;
             }
         }
         catch (SQLException ex) {
             JOptionPane.showMessageDialog(null, ex.getMessage(), "MySQL Error", JOptionPane.ERROR_MESSAGE);
             System.out.println("SQLException: " + ex.getMessage());
             System.out.println("SQLState: " + ex.getSQLState());
             System.out.println("VendorError: " + ex.getErrorCode());
          }
          finally {
              mysql.closeAll();
        }return ovExist;
     }
     public static OrderVerify getOVby(String column, int id){
         OrderVerify ov = null;
          ResultSet rs;
         MysqlConn mysql = new MysqlConn();
         String query = "Select * from cis470.ORDERVERIFY WHERE "
                     + column + " = " + id;
                     
         rs = mysql.doQuery(query);
         try {
             if (rs.next()) {
                 ov = new OrderVerify(
                         rs.getInt("VERID"),
                         Employee.searchBy(rs.getInt("EMPID")),  
                         Order.getOrder(rs.getInt("ORDERID")),
                         rs.getBoolean("nameCheck"),
                         rs.getBoolean("accountCheck"),
                         rs.getBoolean("mediaCheck"),
                         rs.getBoolean("contentCheck"),
                         rs.getBoolean("paymentCheck"),
                         rs.getBoolean("depositCheck"),
                         rs.getString("nameFailComment") != null ? rs.getString("nameFailComment") : new String(),
                         rs.getString("accountFailComment") != null ? rs.getString("accountFailComment") : new String(),                        
                         rs.getString("mediaFailComment") != null ? rs.getString("mediaFailComment") : new String(),
                         rs.getString("contentFailComment") != null ? rs.getString("contentFailComment") : new String(),
                         rs.getString("paymentFailComment") != null ? rs.getString("paymentFailComment") : new String(),
                         rs.getString("depositFailComment") != null ? rs.getString("depositFailComment") : new String(),
                         rs.getString("correctiveActionComment") != null ? rs.getString("correctiveActionComment") : new String());
             }
         }
         catch (SQLException ex) {
             JOptionPane.showMessageDialog(null, ex.getMessage(), "MySQL Error", JOptionPane.ERROR_MESSAGE);
             System.out.println("SQLException: " + ex.getMessage());
             System.out.println("SQLState: " + ex.getSQLState());
             System.out.println("VendorError: " + ex.getErrorCode());
          }
          finally {
              mysql.closeAll();
              return ov;
          }
         
     }
     public static OrderVerify insertOrUpdateOV(OrderVerify ov){
         OrderVerify thisOV = null;
         ResultSet rs;
         MysqlConn mysql = new MysqlConn();
         try {
             mysql.stmt = mysql.conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
             String query = "INSERT INTO ORDERVERIFY (VERID,EMPID,ORDERID,nameCheck,"
                     + "accountCheck,mediaCheck,contentCheck,paymentCheck,depositCheck,nameFailComment,"
                     + "accountFailComment,mediaFailComment,contentFailComment,paymentFailComment,"
                     + "depositFailComment,correctiveActionComment)"
                    + " values (0, "
                     + ov.verifiedBy.getEmpId() + ", "
                     + ov.order.getORDID() + ", "
                     + ov.nameCheck + ", "
                     + ov.accountCheck + ", "
                     + ov.mediaCheck + ", "
                     + ov.contentCheck + ", "
                     + ov.paymentCheck + ", "
                     + ov.depositCheck + ", '"
                     + ov.nameFailComment + "', '"
                     + ov.accountFailComment + "', '"
                     + ov.mediaFailComment + "', '"
                     + ov.contentFailComment + "', '"
                     + ov.paymentFailComment + "', '"
                     + ov.depositFailComment + "', '"
                    + ov.correctiveActionComment + "');"
                   /* + "ON DUPLICATE KEY UPDATE cis470.ORDERVERIFY " 
                    + "SET verifiedBy = " + ov.verifiedBy.getEmpId() + ", "
                     + "ORDERID = "+ ov.order.getORDID() + ", "
                    + "nameCheck = " + ov.nameCheck + ", "
                    + "accountCheck = " + ov.accountCheck + ", "
                    + "mediaCheck = " + ov.mediaCheck + ", "
                    + "contentCheck = " + ov.contentCheck + ", "
                    + "paymentCheck = " + ov.paymentCheck + ", "
                    + "depositCheck = " +ov.depositCheck + ", "
                     + "nameFailComment = '" + ov.nameFailComment + "', "
                     + "accountFailComment = '" + ov.accountFailComment + "', "
                     + "mediaFailComment = '" + ov.mediaFailComment + "', "
                     + "contentFailComment = '" + ov.contentFailComment + "', "
                     + "paymentFailComment = '" + ov.paymentFailComment + "', "
                     + "depositFailComment = '" + ov.depositFailComment + "', "
                     + "correctiveActionComment = '" + ov.correctiveActionComment
                    + "';"*/;
             System.out.println(query);
             mysql.stmt.executeUpdate(query, java.sql.Statement.RETURN_GENERATED_KEYS);
             int key = -1;
             rs = mysql.stmt.getGeneratedKeys();
             if (rs.next()) {
                 key = rs.getInt(1);
             }
             if (key > 0) {
                 thisOV = OrderVerify.getOVby("VERID", key);
             }
             //else{
             //    thisOV = OrderVerify.getOVby("VERID", ov.VERID);
             //}
         }   
         catch (SQLException ex) {
             JOptionPane.showMessageDialog(null, ex.getMessage(), "MySQL Error", JOptionPane.ERROR_MESSAGE);
             System.out.println("SQLException: " + ex.getMessage());
             System.out.println("SQLState: " + ex.getSQLState());
             System.out.println("VendorError: " + ex.getErrorCode());
         }
         finally {
             mysql.closeAll();
             return thisOV;
         }
     }
     public int getVerID(){
         return VERID;
     }
     public void setVerID(int verID){
         VERID = verID;
     }
     public Order getOrder(){
         return order;
     }
     public void setOrder(Order ord){
         order = ord;
     }
     public Employee getVerifiedBy(){
         return verifiedBy;
     }
     public void setVerifiedBy(Employee verBy){
         verifiedBy = verBy;
     }
     public Boolean getNameCheck(){
         if (nameCheck == 1){
             return Boolean.TRUE;}
         else{
             return Boolean.FALSE;
         }
     }
     public void setNameCheck(Boolean nameChk){
         if (nameChk){
             nameCheck = 1;
         }
         else{
             nameCheck = 0;
         }
     }
     public Boolean getAccountCheck(){
         if (accountCheck == 1){
             return Boolean.TRUE;}
         else{
             return Boolean.FALSE;
         }
     }
     public void setAccountCheck(Boolean acctChk){
         if (acctChk){
             accountCheck = 1;
         }
         else{
             accountCheck = 0;
         }
     }
     public Boolean getMediaCheck(){
         if (mediaCheck == 1){
             return Boolean.TRUE;}
         else{
             return Boolean.FALSE;
         }
     }
     public void setMediaCheck(Boolean medChk){
         if (medChk){
             mediaCheck = 1;
         }
         else{
             mediaCheck = 0;
         }
     }
     public Boolean getContentCheck(){
         if (contentCheck == 1){
             return Boolean.TRUE;}
         else{
             return Boolean.FALSE;
         }
     }
     public void setContentCheck(Boolean contChk){
         if (contChk){
             contentCheck = 1;
         }
         else{
             contentCheck = 0;
         }
     }
     public Boolean getPaymentCheck(){
         if (paymentCheck == 1){
             return Boolean.TRUE;}
         else{
             return Boolean.FALSE;
         }
     }
     public void setPaymentCheck(Boolean payChk){
         if (payChk){
             paymentCheck = 1;
         }
         else{
             paymentCheck = 0;
         }
     }
     public Boolean getDepositCheck(){
         if (depositCheck == 1){
             return Boolean.TRUE;}
         else{
             return Boolean.FALSE;
         }
     }
     public void setDepositCheck(Boolean depChk){
         if (depChk){
             depositCheck = 1;
         }
         else{
             depositCheck = 0;
         }
     }
     public String getNameFailComment(){
         return nameFailComment;
     }
     public void setNameFailComment(String nameFail){
         nameFailComment = nameFail;
     }
     public String getAccountFailComment(){
         return accountFailComment;
     }
     public void setAccountFailComment(String acctFail){
         accountFailComment = acctFail;
     }
     public String getMediaFailComment(){
         return mediaFailComment;
     }
     public void setMediaFailComment(String medFail){
         mediaFailComment = medFail;
     }
     public String getContentFailComment(){
         return contentFailComment;
     }
     public void setContentFailComment(String contFail){
         contentFailComment = contFail;
     }
     public String getPaymentFailComment(){
         return paymentFailComment;
     }
     public void setPaymentFailComment(String payFail){
         paymentFailComment = payFail;
     }
     public String getDepositFailComment(){
         return depositFailComment;
     }
     public void setDepositFailComment(String depFail){
         depositFailComment = depFail;
     }
     public String getCorrectiveActionComment(){
         return correctiveActionComment;
     }
     public void setCorrectiveActionComment(String corrActComm){
         correctiveActionComment = corrActComm;
     }
 }
