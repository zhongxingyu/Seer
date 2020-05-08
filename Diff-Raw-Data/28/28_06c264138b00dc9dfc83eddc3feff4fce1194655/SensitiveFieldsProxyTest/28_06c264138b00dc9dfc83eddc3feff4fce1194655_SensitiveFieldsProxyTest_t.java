 package org.n3r.sensitive.proxy;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.Calendar;
 
 import com.google.common.collect.Lists;
 import junit.framework.Assert;
 
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.n3r.core.security.AesCryptor;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertSame;
 
 public class SensitiveFieldsProxyTest {
     private final static String DRIVER_NAME = "oracle.jdbc.OracleDriver";
     private final static String URL = "jdbc:oracle:thin:@10.142.195.62:1521:EcsMall";
     private final static String USER = "eop4malltest";
     private final static String PASSWORD = "eop4mall";
     /*private final static String DRIVER_NAME = "com.mysql.jdbc.Driver";
     private final static String URL = "jdbc:mysql://192.168.1.114:3306/kinglast";
     private final static String USER = "mysql";
     private final static String PASSWORD = "mysql";*/
     private final static String PSPT_TYPE_CODE = "01";
     private final static AesCryptor cryptor = new AesCryptor("rocket");
     private static Connection CONNECTION;
 
     @BeforeClass
     public static void beforeClass() throws SQLException {
         try {
             Class.forName(DRIVER_NAME);
         } catch (ClassNotFoundException e) {
             throw new RuntimeException("Where is your Oracle JDBC Driver?", e);
         }
 
         try {
             CONNECTION = DriverManager.getConnection(URL, USER, PASSWORD);
         } catch (SQLException e) {
             throw new RuntimeException(
                     "Connection Failed! Check output console.", e);
         }
 
         if (CONNECTION == null)
             throw new RuntimeException(
                     "Failed to make connection! connection is null.");
 
         // create tables
         Statement create = null;
         try{
             create = CONNECTION.createStatement();
             create.execute("CREATE TABLE TF_B_ORDER_NETIN (PSPT_NO VARCHAR2(128), PSPT_TYPE_CODE VARCHAR2(2), UPDATETIME DATE)");
             create.execute("CREATE TABLE TF_B_ORDER (ORDER_ID VARCHAR2(128), PSPT_NO VARCHAR2(128))");
         } finally {
             if (create != null)
                 create.close();
         }
 
     }
 
     @AfterClass
     public static void afterClass() throws SQLException {
         if (CONNECTION == null)
             return;
 
         Statement create = null;
         try{
             create = CONNECTION.createStatement();
             create.execute("DROP TABLE TF_B_ORDER_NETIN");
             create.execute("DROP TABLE TF_B_ORDER");
         } finally {
             if (create != null)
                 create.close();
             CONNECTION.close();
         }
 
     }
 
     @Test
     public void testInsert() throws SQLException {
         String psptNo = insertOrderNetIn();
 
         String sql = "SELECT PSPT_NO FROM TF_B_ORDER_NETIN " +
                      "WHERE PSPT_NO = ?";
         PreparedStatement pstmt = null;
         ResultSet resultSet = null;
 
         try {
             // select (use encrypted psptNo) and compare
             String encryptPsptNo = cryptor.encrypt(psptNo);
             pstmt = CONNECTION.prepareStatement(sql);
             pstmt.setString(1, encryptPsptNo);
             resultSet = pstmt.executeQuery();
             if (resultSet.next())
                 Assert.assertEquals(encryptPsptNo, resultSet.getString(1));
             else
                 Assert.fail("ResultSet is empty！");
         } finally {
             if (pstmt != null)
                 pstmt.close();
             if (resultSet != null)
                 resultSet.close();
         }
     }
 
     @Test
     public void testSelect() throws SQLException {
         String psptNo = insertOrderNetIn();
 
         String sql = "SELECT PSPT_NO FROM TF_B_ORDER_NETIN " +
                      "WHERE PSPT_NO = ?";
         PreparedStatement pstmt = null;
         ResultSet resultSet = null;
 
         try {
             // select (use original psptNo)
             pstmt = new PreparedStatementHandler(CONNECTION, sql, cryptor).getPreparedStatement();
             pstmt.setString(1, psptNo);
             resultSet = pstmt.executeQuery();
             if (resultSet.next())
                 // resultSet.getString(1)
                 Assert.assertEquals(psptNo, resultSet.getString("PSPT_NO"));
             else
                 Assert.fail("ResultSet is empty！");
         } finally {
             if (pstmt != null)
                 pstmt.close();
             if (resultSet != null)
                 resultSet.close();
         }
     }
 
     @Test
     public void testUpdate() throws SQLException {
         String psptNo = insertOrderNetIn();
 
         String sql = "UPDATE TF_B_ORDER_NETIN N " +
                      "SET N.PSPT_TYPE_CODE = ? " +
                      "WHERE N.PSPT_NO = ?";
         PreparedStatement pstmt = null;
         ResultSet resultSet = null;
         String psptTypeCode = "00";
 
         try {
             // update
             pstmt = new PreparedStatementHandler(CONNECTION, sql, cryptor).getPreparedStatement();
             pstmt.setString(1, psptTypeCode);
             pstmt.setString(2, psptNo);
             pstmt.execute();
 
             // select and compare
             pstmt = new PreparedStatementHandler(CONNECTION,
                     "SELECT PSPT_TYPE_CODE FROM TF_B_ORDER_NETIN WHERE PSPT_NO = ?", cryptor).getPreparedStatement();
             pstmt.setString(1, psptNo);
             resultSet = pstmt.executeQuery();
 
             if (resultSet.next())
                 Assert.assertEquals(psptTypeCode, resultSet.getString(1));
             else
                 Assert.fail("ResultSet is empty！");
         } finally {
             if (pstmt != null)
                 pstmt.close();
             if (resultSet != null)
                 resultSet.close();
         }
     }
 
     @Test
     public void testMerge() throws SQLException {
         String sql = "MERGE INTO TF_B_ORDER_NETIN N " +
                      "USING TF_B_ORDER R " +
                      "ON (R.PSPT_NO = N.PSPT_NO) " +
                      "WHEN MATCHED THEN " +
                      "UPDATE SET " +
                      "N.PSPT_TYPE_CODE = ? " +
                      "WHEN NOT MATCHED THEN " +
                      "INSERT(PSPT_NO, PSPT_TYPE_CODE, UPDATETIME) " +
                      "VALUES(?, ?, sysdate)";
 
         PreparedStatement pstmt = null;
         ResultSet resultSet = null;
         String psptTypeCodeUpdated = "ox";
         try {
             // not matched
             String psptNo = insertOrder(generateId());
 
             pstmt = new PreparedStatementHandler(CONNECTION, sql, cryptor).getPreparedStatement();
             pstmt.setString(1, psptTypeCodeUpdated);
             pstmt.setString(2, psptNo);
             pstmt.setString(3, PSPT_TYPE_CODE);
             pstmt.execute();
 
             pstmt = new PreparedStatementHandler(CONNECTION,
                     "SELECT PSPT_NO FROM TF_B_ORDER_NETIN WHERE PSPT_NO = ? AND PSPT_TYPE_CODE = ?", cryptor).getPreparedStatement();
             pstmt.setString(1, psptNo);
             pstmt.setString(2, PSPT_TYPE_CODE);
             resultSet = pstmt.executeQuery();
             if (resultSet.next())
                 Assert.assertEquals(psptNo, resultSet.getString(1));
             else
                 Assert.fail("ResultSet is empty！");
 
             // matched
             pstmt = new PreparedStatementHandler(CONNECTION, sql, cryptor).getPreparedStatement();
             pstmt.setString(1, psptTypeCodeUpdated);
             pstmt.setString(2, psptNo);
             pstmt.setString(3, PSPT_TYPE_CODE);
             pstmt.execute();
 
             pstmt = new PreparedStatementHandler(CONNECTION,
                     "SELECT PSPT_NO FROM TF_B_ORDER_NETIN WHERE PSPT_NO = ? AND PSPT_TYPE_CODE = ?", cryptor).getPreparedStatement();
             pstmt.setString(1, psptNo);
             pstmt.setString(2, psptTypeCodeUpdated);
             resultSet = pstmt.executeQuery();
             if (resultSet.next())
                 Assert.assertEquals(psptNo, resultSet.getString(1));
             else
                 Assert.fail("ResultSet is empty！");
 
         } finally {
             if (pstmt != null)
                 pstmt.close();
             if (resultSet != null)
                 resultSet.close();
         }
     }
 
     @Test
     public void testDelete() throws SQLException {
         String psptNo = insertOrderNetIn();
         String sql = "DELETE FROM TF_B_ORDER_NETIN " +
                      "WHERE PSPT_NO = ?";
         PreparedStatement pstmt = null;
         ResultSet resultSet = null;
         try {
             pstmt = new PreparedStatementHandler(CONNECTION, sql, cryptor).getPreparedStatement();
             pstmt.setString(1, psptNo);
             pstmt.execute();
 
             // select and compare
             pstmt = new PreparedStatementHandler(CONNECTION,
                     "SELECT PSPT_NO FROM TF_B_ORDER_NETIN WHERE PSPT_NO = ?", cryptor).getPreparedStatement();
             pstmt.setString(1, psptNo);
             resultSet = pstmt.executeQuery();
 
             if (resultSet.next())
                 Assert.fail("Delete is not work");
         } finally {
             if (pstmt != null)
                 pstmt.close();
             if (resultSet != null)
                 resultSet.close();
         }
     }
 
     /**
      * 1W  -- 1.227217 1.1489345 1.1820456 : 1
      * 10W -- 1.1413171 : 1
      * @throws SQLException
      */
 
     @Test
     public void testCache() throws SQLException {
         String psptNo = insertOrderNetIn();
         String sql = "DELETE FROM TF_B_ORDER_NETIN " +
                 "WHERE PSPT_NO = ?";
         PreparedStatement pstmt = null;
         ResultSet resultSet = null;
         try {
             PreparedStatementHandler preparedStatementHandler = new PreparedStatementHandler(CONNECTION, sql, cryptor);
             pstmt = preparedStatementHandler.getPreparedStatement();
 
             PreparedStatementHandler preparedStatementHandler1 = new PreparedStatementHandler(CONNECTION, sql, cryptor);
 
             assertSame(preparedStatementHandler.getSensitiveFieldsParser(), preparedStatementHandler1.getSensitiveFieldsParser());
 
        } finally {
            if (pstmt != null)
                pstmt.close();
            if (resultSet != null)
                resultSet.close();
        }
    }
    @Test
    public void testSQLErrorInSQLCache() throws SQLException {
        String psptNo = insertOrderNetIn();
        String sql = "DELETE FROMM TF_B_ORDER_NETIN " +
                "WHERE PSPT_NO = ?";
        PreparedStatement pstmt = null;
        ResultSet resultSet = null;
        try {
            PreparedStatementHandler preparedStatementHandler = new PreparedStatementHandler(CONNECTION, sql, cryptor);
            pstmt = preparedStatementHandler.getPreparedStatement();
             pstmt.setString(1, psptNo);
             pstmt.execute();
 
 
         } finally {
             if (pstmt != null)
                 pstmt.close();
             if (resultSet != null)
                 resultSet.close();
         }
     }

     @Ignore("true")
     public void testEncryptEfficiency() throws SQLException {
         int loop = 100000;
         PreparedStatement pstmt = null;
         String sql = "INSERT INTO TF_B_ORDER_NETIN (PSPT_NO, PSPT_TYPE_CODE, UPDATETIME) VALUES (?, ?, sysdate)";
 
         long encryptStart = Calendar.getInstance().getTimeInMillis();
         try {
             for (int i = 0; i < loop; i++) {
                 pstmt = new PreparedStatementHandler(CONNECTION, sql, cryptor).getPreparedStatement();
                 pstmt.setString(1, generateId());
                 pstmt.setString(2, PSPT_TYPE_CODE);
                 pstmt.execute();
             }
         } finally {
             if (pstmt != null)
                 pstmt.close();
         }
         long encryptEnd = Calendar.getInstance().getTimeInMillis();
         long encryptDuration = encryptEnd - encryptStart;
 
         long start = Calendar.getInstance().getTimeInMillis();
         try {
             for (int i = 0; i < loop; i++) {
                 pstmt = CONNECTION.prepareStatement(sql);
                 pstmt.setString(1, generateId());
                 pstmt.setString(2, PSPT_TYPE_CODE);
                 pstmt.execute();
             }
         } finally {
             if (pstmt != null)
                 pstmt.close();
         }
         long end = Calendar.getInstance().getTimeInMillis();
         long duration = end - start;
 
         System.out.println(((float) encryptDuration) / duration);
     }
 
     private String generateId() {
         return Calendar.getInstance().getTimeInMillis() + "";
     }
 
     private String insertOrderNetIn() throws SQLException {
         return insertOrderNetIn(generateId());
     }
 
     private String insertOrderNetIn(String psptNo) throws SQLException {
         String sql = "INSERT INTO TF_B_ORDER_NETIN (PSPT_NO, PSPT_TYPE_CODE, UPDATETIME) VALUES (?, ?, sysdate)";
         PreparedStatement pstmt = null;
         try {
             pstmt = new PreparedStatementHandler(CONNECTION, sql, cryptor).getPreparedStatement();
             pstmt.setString(1, psptNo);
             pstmt.setString(2, PSPT_TYPE_CODE);
             pstmt.execute();
         } finally {
             if (pstmt != null)
                 pstmt.close();
         }
         return psptNo;
     }
 
     private String insertOrder(String psptNo) throws SQLException {
         String sql = "INSERT INTO TF_B_ORDER (ORDER_ID, PSPT_NO) VALUES (?, ?)";
         PreparedStatement pstmt = null;
         try {
             pstmt = new PreparedStatementHandler(CONNECTION, sql, cryptor).getPreparedStatement();
             pstmt.setString(1, generateId());
             pstmt.setString(2, psptNo);
             pstmt.execute();
         } finally {
             if (pstmt != null)
                 pstmt.close();
         }
         return psptNo;
     }
 
 }
