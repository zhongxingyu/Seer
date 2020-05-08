 /*
  * Copyright (C) 2010 The Halal Certification Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at 
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software 
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and 
  * limitations under the License.
  */
 
 package com.mui.certificate.core.db;
 
 import com.mui.certificate.core.Certificate;
 
 import java.net.URL;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 public class ULStatusOperation {
     private Statement mStmt;
     
     public ULStatusOperation(Connection connection) throws SQLException{
         mStmt = connection.createStatement();
     }
     
     public Certificate getUploadedCandidate()throws Exception{
         ResultSet rs = mStmt.executeQuery(" SELECT " +
         		"tbl_cert_hist.number," +
         		"tbl_cert_hist.product_type," +
         		"tbl_cert_hist.product_name," +
         		"tbl_cert_hist.issued," +
         		"tbl_cert_hist.valid_until," +
         		"tbl_cert_hist.url," +
         		"tbl_company.name," +
         		"tbl_company.address " +
         		"FROM tbl_cert_hist " +
         		"INNER JOIN company " +
         		"INNER JOIN tbl_ul_status " +
         		"WHERE tbl_ul_status.status = 0;");
         Certificate retval = null;
         if(rs.next()){
             retval = new Certificate();
             retval.setCertificateNumber(rs.getInt(1));
             retval.setProductType(rs.getString(2));
             retval.setProductName(rs.getString(3));
             retval.setIssuedDate(rs.getDate(4));
             retval.setValidDate(rs.getDate(5));
             retval.setCertificateURL(new URL(rs.getString(6)));
             retval.setCompanyName(rs.getString(7));
             retval.setCompanyAddress(rs.getString(8));
         }
         
         return retval;
     }
     
    public void addUploadCandidate(Integer cert_number)throws Exception{
     	PreparedStatement pstmt = mStmt.getConnection().prepareStatement(
    			"INSERT INTO tbl_ul_status VALUES(?,?,?,?,?,?,?,?);");
    	pstmt.setInt(1, cert_number);
     	pstmt.setInt(2,0);
     	pstmt.setInt(3,0);
     	pstmt.setInt(4,0);
     	pstmt.setInt(5,0);
     	pstmt.setInt(6,0);
     	pstmt.setInt(7,0);
        pstmt.setInt(8,0);
     	pstmt.addBatch();
     	mStmt.getConnection().setAutoCommit(false);
     	pstmt.executeBatch();
     	mStmt.getConnection().setAutoCommit(true);
     	
     }
     
     public void updateUploadStatusProductType(Integer certnumber)throws SQLException{
     	mStmt.executeUpdate("UPDATE tbl_ul_status set product_type=1 WHERE cert_number = "+
     			certnumber.toString()+ ";");
     }
     
     public void updateUploadStatusProductName(Integer certnumber)throws SQLException{
     	mStmt.executeUpdate("UPDATE tbl_ul_status set product_name=1 WHERE cert_number = "+
     			certnumber.toString()+ ";");
     }
     
     public void updateUploadStatusIssued(Integer certnumber)throws SQLException{
     	mStmt.executeUpdate("UPDATE tbl_ul_status set issued=1 WHERE cert_number = "+
     			certnumber.toString()+ ";");    	
     }
     
     public void updateUploadStatusValidUntil(Integer certnumber)throws SQLException{
     	mStmt.executeUpdate("UPDATE tbl_ul_status set valid_until=1 WHERE cert_number = "+
     			certnumber.toString()+ ";");    	
     }
     
     public void updateUploadStatusUrl(Integer certnumber)throws SQLException{
     	mStmt.executeUpdate("UPDATE tbl_ul_status set url=1 WHERE cert_number = "+
     			certnumber.toString()+ ";");    	
     }
     
     public void updateUploadCompanyName(Integer certnumber)throws SQLException{
     	mStmt.executeUpdate("UPDATE tbl_ul_status set company_name=1 WHERE cert_number = "+
     			certnumber.toString()+ ";");    	   	
     }
     
    public void updateUploadCompanyAddress(Integer certnumber)throws SQLException{
        mStmt.executeUpdate("UPDATE tbl_ul_status SET company_address=1 WHERE cert_number = "+
                certnumber.toString()+ ";");            
    }    
    
    public ULStatusData getUploadStatus(Integer number)throws SQLException{
        ResultSet rs = mStmt.executeQuery("SELECT * FROM tbl_ul_status WHERE cert_number = " +
        		number.toString()+";");
    	ULStatusData retval = null;
    	if(rs.next()){
    	    retval = new ULStatusData();
    	    retval.certificate_number = rs.getInt(1);
    	    retval.product_type = rs.getInt(2);
    	    retval.product_name = rs.getInt(3);
    	    retval.issued = rs.getInt(4);
    	    retval.valid_until = rs.getInt(5);
    	    retval.url = rs.getInt(6);
    	    retval.company_name = rs.getInt(7);
    	    retval.company_address = rs.getInt(8);
    	}
     	return retval;
     }
 
 }
