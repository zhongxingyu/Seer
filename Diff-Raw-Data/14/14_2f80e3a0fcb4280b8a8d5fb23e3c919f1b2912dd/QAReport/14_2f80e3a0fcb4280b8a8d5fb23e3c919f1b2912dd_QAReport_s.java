 
 package wsc_application;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import javax.swing.JOptionPane;
 
 public class QAReport {
     
     private Order order;
     private int QAID;
     private Employee inspectedBy;
     private int contentCheck;
     private int mediaCheck;
     private int mediaFinishCheck;
     private int workmanshipCheck;
     private String contentFailComment;
     private String mediaFailComment;
     private String mediaFinishFailComment;
     private String workmanshipFailComment;
     private String correctiveActionComment;
     
     public QAReport(){
         
     }
     
     public QAReport(int qaid, Order order, Employee inspectedBy, Boolean contentCheck,
         Boolean mediaCheck, Boolean mediaFinish, Boolean workmanship, String contentFail, String mediaFail,
         String medFinFail, String workmanshipFail, String corrActComment){
         setQAID(qaid); 
         setOrder(order); 
         setInspectedBy(inspectedBy); 
         setContentCheck(contentCheck);
         setMediaCheck(mediaCheck); 
         setMediaFinishCheck(mediaFinish); 
         setWorkmanshipCheck(workmanship);
         setContentFailComment(contentFail);
         setMediaFailComment(mediaFail);
         setMediaFinishFailComment(medFinFail);
         setWorkmanshipFailComment(workmanshipFail); 
         setCorrectiveActionComment(corrActComment);
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
     public static QAReport getQAby(String column, int id){
         QAReport qa = null;
          ResultSet rs;
         MysqlConn mysql = new MysqlConn();
         String query = "Select * from cis470.QUALITYASSURANCE WHERE "
                     + column + " = " + id;
                     
         rs = mysql.doQuery(query);
         try {
             if (rs.next()) {
                 qa = new QAReport(
                         rs.getInt("QAID"),
                         Order.getOrder(rs.getInt("ORDERID")),
                         Employee.searchBy(rs.getInt("EMPID")),  
                         rs.getBoolean("contentCheck"),
                         rs.getBoolean("mediaCheck"),
                         rs.getBoolean("mediaFinish"),
                         rs.getBoolean("workmanship"),
                         rs.getString("contentFailComment") != null ? rs.getString("contentFailComment") : new String(),
                         rs.getString("mediaFailComment") != null ? rs.getString("mediaFailComment") : new String(),                        
                         rs.getString("mediaFinishFailComment") != null ? rs.getString("mediaFinishFailComment") : new String(),
                         rs.getString("workmanshipFailComment") != null ? rs.getString("workmanshipFailComment") : new String(),
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
              return qa;
          }
         
     }
    public static QAReport insertOrUpdateOV(QAReport qa){
         QAReport thisQA = null;
         ResultSet rs;
         MysqlConn mysql = new MysqlConn();
         try {
             mysql.stmt = mysql.conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
            String query = "INSERT INTO QUALITYASSURANCE (QAID,EMPID,ORDID,contentCheck,mediaCheck,"
                    + "workmanshipCheck,contentFailComment,mediaFailComment,mediaFinishFailComment,"
                     + "workmanshipFailComment,correctiveActionComment) values ("
                     + qa.QAID + ", "
                     + qa.inspectedBy.getEmpId() + ", "
                     + qa.order.getORDID() + ", "
                     + qa.contentCheck + ", "
                     + qa.mediaCheck + ", "
                     + qa.mediaFinishCheck + ", "
                     + qa.workmanshipCheck + ", '"
                     + qa.contentFailComment + "', '"
                     + qa.mediaFailComment + "', '"
                     + qa.mediaFinishFailComment + "', '"
                     + qa.workmanshipFailComment + "', '"
                     + qa.correctiveActionComment + "')"
                     + "ON DUPLICATE KEY UPDATE " 
                     + "EMPID = " + qa.inspectedBy.getEmpId() + ", "
                     + "ORDERID = "+ qa.order.getORDID() + ", "
                     + "contentCheck = " + qa.contentCheck + ", "
                     + "mediaCheck = " + qa.mediaCheck + ", "
                     + "mediaFinish = " + qa.mediaFinishCheck + ", "
                     + "workmanship = " + qa.workmanshipCheck + ", "
                     + "contentFailComment = '" + qa.contentFailComment + "', "
                     + "mediaFailComment = '" + qa.mediaFailComment + "', "
                     + "mediaFinishFailComment = '" + qa.mediaFinishFailComment + "', "
                     + "workmanshipFailComment = '" + qa.workmanshipFailComment + "', "
                     + "correctiveActionComment = '" + qa.correctiveActionComment
                     + "';";
             System.out.println(query);
             mysql.stmt.executeUpdate(query, java.sql.Statement.RETURN_GENERATED_KEYS);
             int key = -1;
             rs = mysql.stmt.getGeneratedKeys();
             if (rs.next()) {
                 key = rs.getInt(1);
             }
             if (key > 0) {
                thisQA = QAReport.getQAby("VERID", key);
             }
             //else{
             //    thisOV = OrderVerify.getOVby("VERID", qa.VERID);
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
             return thisQA;
         }
     }
     
     public void setQAID(int qaid){
        this.QAID = qaid;
     } 
     public void setOrder(Order order){
        this.order = order;
     } 
     public void setInspectedBy(Employee inspect){
        this.inspectedBy = inspect;
     }
     public void setContentCheck(Boolean contentCheck){
        if(contentCheck){
            this.contentCheck = 1;
        }
        else{
            this.contentCheck = 0;
        }
     }
     public void setMediaCheck(Boolean mediaCheck){
              if(mediaCheck){
            this.mediaCheck = 1;
        }
        else{
            this.mediaCheck = 0;
        }
     }
     public void setMediaFinishCheck(Boolean mediaFinishCheck){
        if(mediaFinishCheck){
            this.mediaFinishCheck = 1;
        }
        else{
            this.mediaFinishCheck = 0;
        }
     }
     public void setWorkmanshipCheck(Boolean workmanshipCheck){
        if(workmanshipCheck){
            this.workmanshipCheck = 1;
        }
        else{
            this.workmanshipCheck = 0;
        }
     }
     public void setContentFailComment(String contFail){
         this.contentFailComment = contFail;
     } 
     public void setMediaFailComment(String medFail){
         this.mediaFailComment = medFail;
     }
     public void setMediaFinishFailComment(String medFinFail){
         this.mediaFinishFailComment = medFinFail;
     }
     public void setWorkmanshipFailComment(String workFail){
         this.workmanshipFailComment = workFail;
     }
     public void setCorrectiveActionComment(String corrAct){
         this.correctiveActionComment = corrAct;
     }
     public int getQAID(){
         return QAID;
     }
     public Employee getInspectedBy(){
         return inspectedBy;
     }
     public Boolean getContentCheck(){
         if(contentCheck == 1){
             return true;
         }
         else{
             return false;
         }                
     }
     public Boolean getMediaCheck(){
         if(mediaCheck == 1){
             return true;
         }
         else{
             return false;
         }
     }
     public Boolean getMediaFinishCheck(){
         if(mediaFinishCheck == 1){
             return true;
         }
         else{
             return false;
         }
     }
     public Boolean getWorkmanshipCheck(){
         if(workmanshipCheck == 1){
             return true;
         }
         else{
             return false;
         }
     }
     public String getContentFailComment(){
         return contentFailComment;
     }
     public String getMediaFailComment(){
         return mediaFailComment;
     }
     public String getMediaFinishFailComment(){
         return mediaFinishFailComment;
     }
     public String getWorkmanshipFailComment(){
         return workmanshipFailComment;
     }
     public String getCorrectiveActionComment(){
         return correctiveActionComment;
     }
 }    
