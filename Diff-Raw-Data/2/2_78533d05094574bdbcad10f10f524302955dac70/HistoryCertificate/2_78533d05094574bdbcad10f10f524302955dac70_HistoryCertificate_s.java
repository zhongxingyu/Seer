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
 import java.sql.Date;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.LinkedList;
 import java.util.List;
 
 public class HistoryCertificate {
     private Statement mStmt;
     
     public HistoryCertificate(Connection connection) throws SQLException{
         mStmt = connection.createStatement();
     }
     
     public void addHistory(Certificate certificate) throws SQLException{
         PreparedStatement pstmt = mStmt.getConnection().prepareStatement(
         		"INSERT INTO tbl_company VALUES(?,?);");
         pstmt.setString(1, certificate.getCompanyName());
         pstmt.setString(2, certificate.getCompanyAddress());
         pstmt.addBatch();
         
         PreparedStatement pstmt2 = mStmt.getConnection().prepareStatement(
         		"INSERT INTO tbl_cert_hist values(?,?,?,?,?,?,?);");
         pstmt2.setInt(1, certificate.getCertificateNumber());
         pstmt2.setString(2, certificate.getProductType());
         pstmt2.setString(3,certificate.getProductName());
         pstmt2.setDate(4, new Date(certificate.getIssuedDate().getTime()));
         pstmt2.setDate(5, new Date(certificate.getValidDate().getTime()));
         pstmt2.setString(6, certificate.getCertificateURL().toString());
         pstmt2.setString(7, certificate.getCompanyName());
         pstmt2.addBatch();
         mStmt.getConnection().setAutoCommit(false);
         pstmt.executeBatch();
         pstmt2.executeBatch();
         mStmt.getConnection().setAutoCommit(true);
     }
     
     
     public Certificate getCertificate(Integer certNumber) throws Exception{
         ResultSet rs = mStmt.executeQuery("SELECT " +
         		"tbl_cert_hist.product_type, " +
         		"tbl_cert_hist.product_name, " +
         		"tbl_cert_hist.issued, " +
         		"tbl_cert_hist.valid_until, " +
         		"tbl_cert_hist.url, " +
         		"tbl_company.name, " +
         		"tbl_company.address " +
         		"FROM tbl_cert_hist " +
         		"INNER JOIN tbl_company " +
         		"ON tbl_company.name = tbl_cert_hist.company_name " +
         		"WHERE tbl_cert_hist.number = " + certNumber.toString() + ";");
 
         Certificate retval = null;
         if(rs.next()){
             retval = new Certificate();
             retval.setCertificateNumber(certNumber);
             retval.setProductType(rs.getString(1));
             retval.setProductName(rs.getString(2));
             retval.setIssuedDate(rs.getDate(3));
             retval.setValidDate(rs.getDate(4));
             retval.setCertificateURL(new URL(rs.getString(5)));
             retval.setCompanyName(rs.getString(6));
             retval.setCompanyAddress(rs.getString(7));
         }
         return retval;
     }
     
     
     public List<Certificate> getCertificateList() throws Exception{
         ResultSet rs = mStmt.executeQuery("SELECT " +
         		"tbl_cert_hist.number, " + 
         		"tbl_cert_hist.product_type, " +
         		"tbl_cert_hist.product_name, " +
         		"tbl_cert_hist.issued, " +
         		"tbl_cert_hist.valid_until, " +
         		"tbl_cert_hist.url, " + 
         		"tbl_company.name, " +
         		"tbl_company.address " +
         		"FROM tbl_cert_hist " +
         		"INNER JOIN tbl_company " +
         		"ON tbl_company.name = tbl_cert_hist.company_name;");
         List<Certificate> retval = new LinkedList<Certificate>();
         while(rs.next()){
         	Certificate cert = new Certificate();
         	cert.setCertificateNumber(rs.getInt(1));
         	cert.setProductType(rs.getString(2));
         	cert.setProductName(rs.getString(3));
         	cert.setIssuedDate(rs.getDate(4));
         	cert.setValidDate(rs.getDate(5));
         	cert.setCertificateURL(new URL(rs.getString(6)));
         	cert.setCompanyName(rs.getString(7));
         	cert.setCompanyAddress(rs.getString(8));
         	retval.add(cert);
         }
         return retval;
     }
     
     public List<String> getCompaniesList() throws Exception{
     	ResultSet rs = mStmt.executeQuery("SELECT tbl_company.name FROM tbl_company;");
     	List<String> retval = new LinkedList<String>();
     	while(rs.next()){
     		retval.add(rs.getString(1));
     	}
         return retval;
     }
     
     
     public String getCompanyAddress(String company)throws SQLException{
     	ResultSet rs = mStmt.executeQuery("SELECT tbl_company.address " +
    			"FROM tbl_company WHERE tbl_company.name="+company+";");
     	if(rs.next()) return rs.getString(1);
     	else return null;
     }
     
 }
