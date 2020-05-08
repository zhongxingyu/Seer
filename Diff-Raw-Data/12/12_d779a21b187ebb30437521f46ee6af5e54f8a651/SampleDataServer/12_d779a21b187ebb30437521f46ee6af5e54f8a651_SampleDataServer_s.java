 /**
  * 
  * 
  * Copyright 2002 NCHELP
  * 
  * Author:		Tim Bornholtz,  Priority Technologies, Inc.
  * 
  * 
  * This code is part of the Meteor system as defined and specified 
  * by the National Council of Higher Education Loan Programs, Inc. 
  * (NCHELP) and the Meteor Sponsors, and developed by Priority 
  * Technologies, Inc. (PTI). 
  *
  * 
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *	
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *	
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  ********************************************************************************/
 
 package com.prioritytech.meteor;
 
 import java.math.BigDecimal;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import org.nchelp.meteor.logging.Logger;
 import org.nchelp.meteor.message.MeteorDataResponse;
 import org.nchelp.meteor.message.response.AddressInfo;
 import org.nchelp.meteor.message.response.Award;
 import org.nchelp.meteor.message.response.Borrower;
 import org.nchelp.meteor.message.response.ConsolLender;
 import org.nchelp.meteor.message.response.Contacts;
 import org.nchelp.meteor.message.response.DataProviderAggregateTotal;
 import org.nchelp.meteor.message.response.DataProviderData;
 import org.nchelp.meteor.message.response.Default;
 import org.nchelp.meteor.message.response.Disbursement;
 import org.nchelp.meteor.message.response.DisbursingAgent;
 import org.nchelp.meteor.message.response.Employer;
 import org.nchelp.meteor.message.response.Guarantor;
 import org.nchelp.meteor.message.response.Lender;
 import org.nchelp.meteor.message.response.MeteorDataProviderAwardDetails;
 import org.nchelp.meteor.message.response.MeteorDataProviderDetailInfo;
 import org.nchelp.meteor.message.response.MeteorDataProviderInfo;
 import org.nchelp.meteor.message.response.MeteorDataProviderMsg;
 import org.nchelp.meteor.message.response.MeteorRsMsg;
 import org.nchelp.meteor.message.response.OrgType;
 import org.nchelp.meteor.message.response.PersonType;
 import org.nchelp.meteor.message.response.Phone;
 import org.nchelp.meteor.message.response.Reference;
 import org.nchelp.meteor.message.response.Repayment;
 import org.nchelp.meteor.message.response.School;
 import org.nchelp.meteor.message.response.Servicer;
 import org.nchelp.meteor.message.response.Student;
 import org.nchelp.meteor.provider.MeteorContext;
 import org.nchelp.meteor.provider.data.DataServerAbstraction;
 import org.nchelp.meteor.security.SecurityToken;
 import org.nchelp.meteor.util.Resource;
 import org.nchelp.meteor.util.ResourceFactory;
 
 /**
 * This is a sample implementation of how to tie the Index Provider
 * Meteor code with your proprietary backend system.  All code in 
 * the classes that implement IndexServerAbstraction are excluded 
 * from the normal licensing terms of Meteor.  That means you can
 * put proprietary code here and not have to release it to anyone.
 * 
 * @version   $Revision$ $Date$
 * @since     Meteor1.0
 * 
 */
 public class SampleDataServer implements DataServerAbstraction {
 
 	private final Logger log = Logger.create(this.getClass());
 
 	private static Connection conn = null;
 	/*
	 * @see DataServerAbstraction#getData(String, Date)
 	 */
 	public MeteorDataResponse getData(MeteorContext context, String ssn) {
 		/* 
 		 * Set up the bare minimum stuff that will need to be
 		 * part of the response object regardless of whether
 		 * an error occurs or not
 		 */
 		MeteorDataResponse dataResp = new MeteorDataResponse();
 		MeteorRsMsg msg = dataResp.getRsMsg();
 
 		MeteorDataProviderInfo mdpi = new MeteorDataProviderInfo();
 		msg.addMeteorDataProviderInfo(mdpi);
 		
 		MeteorDataProviderDetailInfo mdpdi = this.getMeteorDataProviderDetailInfo();
 		mdpi.setMeteorDataProviderDetailInfo(mdpdi);
 
 		/*  
 		 * Make sure that the SecurityToken isn't null
 		 */
 		SecurityToken security = context.getSecurityToken();
 		if(security == null){
 			/*
 			 * ACK!! Bad Bad Bad
 			 */
 			this.setError(mdpi, "No Security Token passed to Data Provider", "E");
 			dataResp.createMinimalResponse();
 			return dataResp;
 		}
 		
 		String ssnType = null;
 		if( SecurityToken.roleFAA.equals(security.getRole())){
 			ssnType = "S";
 		} else if(SecurityToken.roleSTUDENT.equals(security.getRole())){
 			ssnType = "B";	
 		} else {
 			log.error("Invalid Security Role passed to Data Provider: '" + security.getRole() + "'");
 			
 			this.setError(mdpi, "Invalid Security Role passed to Data Provider", "E");
 			dataResp.createMinimalResponse();
 			return dataResp;
 		}
 				
 		conn = this.getConnection();
 		
 		if(conn == null){
 			// something went wrong.  
 			// So there isn't much else I can do here
 			this.setError(mdpi, "Error Connecting to Database", "E");
 			dataResp.createMinimalResponse();
 			return dataResp;
 		}		
 		
 		this.addAwards(mdpi, ssn, ssnType);
 		
 		dataResp.createMinimalResponse();
 		
 		return dataResp;
 	}
 	
 	private Connection getConnection(){
 		if(conn != null){
 			// If there's already one there then use that
 			return conn;
 		}
 		
 		Resource res = ResourceFactory.createResource("dataprovider.properties");
 	    String jdbcClass = res.getProperty("com.prioritytech.sample.JDBCClass");
 	    String jdbcURL = res.getProperty("com.prioritytech.sample.JDBCURL");
 	    String jdbcUserID = res.getProperty("com.prioritytech.sample.JDBCUserID");
 	    String jdbcPassword = res.getProperty("com.prioritytech.sample.JDBCPassword");
 
 		Connection con = null;
 		
 		try{
 		    Class.forName(jdbcClass).newInstance();
 	    	con = DriverManager.getConnection(jdbcURL, jdbcUserID, jdbcPassword);
 		} catch(ClassNotFoundException e){
 			log.error("Class not found: '" + jdbcClass + "'", e);
 		} catch(SQLException e){
 			log.error("Error connecting to the database: '" + jdbcURL + "'", e);
 		} catch(Exception e){
 			log.error("Error creating connection object: ", e);
 		}
 
 		try{
 			con.setReadOnly(true);
 			con.setAutoCommit(true);
 		} catch(NullPointerException e){
 			// hapens if the previous code threw an exception
 		} catch(SQLException e){
 			log.error("Error setting database options", e);
 		}
 
 		return con;	
 	}	
 
 	/**
 	 * Method addAwards.
 	 * @param mdpi
 	 * @param ssn
 	 * @param ssnType  Type of SSN to look for.  'B' - Borrower, 'S' - Student
 	 */
 	private void addAwards(MeteorDataProviderInfo mdpi, String ssn, String ssnType){
 		ResultSet rs = null;
 
 		MeteorDataProviderAwardDetails mdpad = new MeteorDataProviderAwardDetails();
 		mdpi.setMeteorDataProviderAwardDetails(mdpad);
 
 		String ssnTypeJoinColumn = null;
 		if("B".equals(ssnType)){
 			ssnTypeJoinColumn = "a.borrower_person_id";
 		} else if("S".equals(ssnType)){
 			ssnTypeJoinColumn = "a.student_person_id";
 		} else {
 			log.error("Invalid ssnType passed to addAwards(). Expected 'S' or 'B' but got '" + ssnType + "'");
 		}
 		
 		try{
 			Statement s = conn.createStatement();
 			String sql = "select a.* from awarddata a, person p where " + ssnTypeJoinColumn + " = p.person_id and p.ssn = '" + ssn + "'"; 
 
 			rs = s.executeQuery(sql);
 			conn.commit();
 
 			while(rs.next() ){
 
 				Award awd = new Award();
 				mdpad.addAward(awd);
 				
 				int    awardID = 0;
 				
 				String  strColumn = null;
 				double dblColumn = 0;
 				int    iColumn = 0;
 				
 				awardID = rs.getInt("award_data_id");
 				awd.setDataProviderType(rs.getString("data_provider_type").trim());
 				
 				strColumn = rs.getString("award_type");
 				if(strColumn != null){ awd.setAwardType(strColumn.trim()); }
 				
 				awd.setAwardAmt(new BigDecimal(rs.getDouble("award_amt")).setScale(2, BigDecimal.ROUND_DOWN));
 				
 				awd.setAwardBeginDt(new org.exolab.castor.types.Date(rs.getDate("award_begin_dt")));
 				awd.setAwardEndDt(new org.exolab.castor.types.Date(rs.getDate("award_end_dt")));
 				
 				if(strColumn != null) { awd.setGradeLevelInd(rs.getString("grade_level_ind")); }
 				
 				if(strColumn != null) { awd.setLoanStat(rs.getString("loan_stat")); }
 				
 				awd.setLoanStatDt(new org.exolab.castor.types.Date(rs.getDate("loan_stat_dt")));
 				
 				if(strColumn != null){ awd.setMPNInd(rs.getString("mpn_ind")); }
 				
 				strColumn = rs.getString("esign");
 				if(strColumn != null){ awd.setEsign(strColumn.equalsIgnoreCase("y") ? true : false); }
 				
 				awd.setCommonlineError(rs.getString("commonline_error"));
 				
				awd.setGuarDt(new org.exolab.castor.types.Date(rs.getDate("guar_dt")));
 				
 				iColumn = rs.getInt("student_person_id");
 				if(iColumn != 0) awd.setStudent((Student)this.getPersonType(iColumn, new Student()));
 				
 				iColumn = rs.getInt("borrower_person_id");
 				if(iColumn != 0) awd.setBorrower((Borrower)this.getPersonType(iColumn, new Borrower()));
 				
 				iColumn = rs.getInt("reference_1_person_id");
 				if(iColumn != 0) awd.addReference((Reference)this.getPersonType(iColumn, new Reference()));
 				iColumn = rs.getInt("reference_2_person_id");
 				if(iColumn != 0) awd.addReference((Reference)this.getPersonType(iColumn, new Reference()));
 				
 				iColumn = rs.getInt("disbursing_agent_org_id");
 				if(iColumn != 0) awd.setDisbursingAgent((DisbursingAgent)this.getOrgType(iColumn, new DisbursingAgent()));
 				
 				iColumn = rs.getInt("lender_org_id");
 				if(iColumn != 0) awd.setLender((Lender)this.getOrgType(iColumn, new Lender()));
 				
 				iColumn = rs.getInt("servicer_org_id");
 				if(iColumn != 0) awd.setServicer((Servicer)this.getOrgType(iColumn, new Servicer()));
 				
 				iColumn = rs.getInt("consol_lender_org_id");
 				if(iColumn != 0) awd.setConsolLender((ConsolLender)this.getOrgType(iColumn, new ConsolLender()));
 				
 				iColumn = rs.getInt("school_org_id");
 				if(iColumn != 0) awd.setSchool((School)this.getOrgType(iColumn, new School()));
 				
 				iColumn = rs.getInt("guarantor_org_id");
 				if(iColumn != 0) awd.setGuarantor((Guarantor)this.getOrgType(iColumn, new Guarantor()));
 				
 				awd.setAwardId(rs.getString("award_id"));
 				
 				iColumn = rs.getInt("repayment_id");
 				if(iColumn != 0) awd.setRepayment(this.getRepayment(iColumn));
 				
 				iColumn = rs.getInt("default_id");
 				if(iColumn != 0) awd.addDefault(this.getDefault(iColumn));
 
 				this.addDisbursements(awd, awardID);
 
 
 			}
 			
 		} catch(SQLException e) {
 			log.warn("SQL Exception: ", e);
 		}
 		
 		
 	}
 	
 	private MeteorDataProviderDetailInfo getMeteorDataProviderDetailInfo(){
 		MeteorDataProviderDetailInfo mdpdi = new MeteorDataProviderDetailInfo();
 		
 		DataProviderData dpd = new DataProviderData();
 		mdpdi.setDataProviderData(dpd);
 
 		Contacts cont = new Contacts();
 		dpd.setContacts(cont);
 		
 
 		Resource res = ResourceFactory.createResource("dataprovider.properties");
 
 		dpd.setEntityName(res.getProperty("DataProvider.Data.Name"));
 		String id = res.getProperty("DataProvider.Data.ID");
 		if(id != null && ! id.equals("")) dpd.setEntityID(id);
 		dpd.setEntityURL(res.getProperty("DataProvider.Data.URL"));
 		mdpdi.setDataProviderType(res.getProperty("DataProvider.Data.Type"));
 		
 		Phone phone = new Phone();
 		phone.setPhoneNum(res.getProperty("DataProvider.Data.Contacts.PhoneNum"));
 		phone.setPhoneNumType("P");
 		
 		cont.addPhone(phone);
 		
 		cont.setEmail(res.getProperty("DataProvider.Data.Contacts.Email"));
 		
 		AddressInfo addr = new AddressInfo();
 		String val = res.getProperty("DataProvider.Data.Contacts.Addr");
 		if(val != null) addr.addAddr(val);
 		
 		val = res.getProperty("DataProvider.Data.Contacts.Addr2");
 		if(val != null) addr.addAddr(val);
 		val = res.getProperty("DataProvider.Data.Contacts.Addr3");
 		if(val != null) addr.addAddr(val);
 		val = res.getProperty("DataProvider.Data.Contacts.City");
 		if(val != null) addr.setCity(val);
 		val = res.getProperty("DataProvider.Data.Contacts.StateProv");
 		if(val != null) addr.setStateProv(val);
 		val = res.getProperty("DataProvider.Data.Contacts.PostalCd");
 		if(val != null) addr.setPostalCd(val);
 
 		cont.setAddressInfo(addr);
 		DataProviderAggregateTotal dpat = new DataProviderAggregateTotal();
 		mdpdi.setDataProviderAggregateTotal(dpat);
 		
 		return mdpdi;
 	}
 	
 	private PersonType getPersonType(int personID, PersonType person){
 		ResultSet rs = null;
 		try{
 			Statement s = conn.createStatement();
 			String sql = "select * from person where person_id = " + personID; 
 
 			rs = s.executeQuery(sql);
 			conn.commit();
 
 			while(rs.next() ){
 				String strColumn = null;
 				int iColumn = 0;
 				java.sql.Date dtColumn = null;
 				
 				
 				
 				strColumn = rs.getString("last_name");
 				if(strColumn != null) person.setLastName(strColumn.trim());
 				
 				strColumn = rs.getString("first_name");
 				if(strColumn != null) person.setFirstName(strColumn.trim());
 				
 				strColumn = rs.getString("middle_initial");
 				if(strColumn != null) person.setMiddleInitial(strColumn.trim());
 				
 				person.setSSNum(rs.getString("ssn"));
 				
 				dtColumn = rs.getDate("birth_dt");
 				if(dtColumn != null) person.setDtOfBirth(new org.exolab.castor.types.Date(dtColumn));
 			
 				iColumn = rs.getInt("contact_id");
 				if(iColumn != 0){ person.setContacts(this.getContacts(iColumn)); }
 				
 				iColumn = rs.getInt("employer_org_id");
 				if(iColumn != 0){ person.setEmployer((Employer)this.getOrgType(iColumn, new Employer())); }
 
 
 				// Probably not the most elegant way to handle this
 				if(person instanceof Borrower){
 					Borrower borrower = (Borrower)person;
 					borrower.setDriversLicense(rs.getString("drivers_license"));
 					strColumn = rs.getString("drivers_license_state");
 					if(strColumn != null) borrower.setDriversLicenseState(strColumn);
 				}
 				
 				if(person instanceof Student){
 					Student student = (Student) person;
 					dtColumn = rs.getDate("grad_dt");
 					if(dtColumn != null) student.setGradDt(new org.exolab.castor.types.Date(dtColumn));
 				}
 			}
 
 		}catch(SQLException e){
 			log.warn("Error retrieveing data from Person table", e);
 		}
 			
 		
 		return person;	
 	}
 	
 	private OrgType getOrgType(int orgID, OrgType org){
 		ResultSet rs = null;
 		try{
 			Statement s = conn.createStatement();
 			String sql = "select * from organization where org_id = " + orgID; 
 	
 			rs = s.executeQuery(sql);
 			conn.commit();
 
 			while(rs.next() ){
 		
 				org.setEntityName(rs.getString("entity_name"));
 				org.setEntityURL(rs.getString("entity_url"));
 				org.setEntityID(rs.getString("entity_id"));
 				//org.setDataProviderType(rs.getString("data_provider_type").trim());
 				
 				int iColumn = rs.getInt("contact_id");
 				if(iColumn != 0) {
 					org.setContacts(this.getContacts(iColumn));
 				} else {
 					org.setContacts(new Contacts());
 				}
 				
 
 			}
 		}catch(SQLException e){
 			log.warn("Error retrieveing data from organization table", e);
 		}
 		
 		return org;
 	}
 	
 	private Repayment getRepayment(int repaymentID){
 		Repayment pay = new Repayment();
 		ResultSet rs = null;
 
 		
 		try{
 			Statement s = conn.createStatement();
 			String sql = "select * from repayment where repayment_id = " + repaymentID; 
 			
 			rs = s.executeQuery(sql);
 			conn.commit();
 
 			while(rs.next()){
 				String strColumn = null;
 				java.sql.Date dtColumn = null;
 				
 				pay.setNextPmtAmt(new BigDecimal(rs.getDouble("next_payment_amt")).setScale(2, BigDecimal.ROUND_DOWN));
 				
 				dtColumn = rs.getDate("next_due_dt");
 				if(dtColumn != null) pay.setNextDueDt(new org.exolab.castor.types.Date(dtColumn));
 
 				pay.setAcctBal(new BigDecimal(rs.getDouble("account_balance")).setScale(2, BigDecimal.ROUND_DOWN));
 				
 				dtColumn = rs.getDate("account_balance_dt");
 				if(dtColumn != null) pay.setAcctBalDt(new org.exolab.castor.types.Date(dtColumn));
 
 				dtColumn = rs.getDate("payment_begin_dt");
 				if(dtColumn != null) pay.setPmtBeginDt(new org.exolab.castor.types.Date(dtColumn));
 				
 				pay.setCurrIntRate(new BigDecimal(rs.getDouble("current_int_rate")).setScale(3, BigDecimal.ROUND_DOWN));
 
 				pay.setRepaidPrincipalAmt(new BigDecimal(rs.getDouble("repaid_principal_amt")).setScale(2, BigDecimal.ROUND_DOWN));
 
 				pay.setCapitalizedIntAmt(new BigDecimal(rs.getDouble("capitalized_int_amt")).setScale(2, BigDecimal.ROUND_DOWN));
 
 			}
 		} catch(SQLException e){
 			log.warn("Error retrieveing data from repayment table", e);
 		}
 		
 		return pay;
 	}
 	
 	private Default getDefault(int defaultID){
 		return new Default();
 	}
 	
 	private void addDisbursements(Award awd, int awardID){
 		
 		ResultSet rs = null;
 		try{
 			Statement s = conn.createStatement();
 			String sql = "select * from disbursement where award_id = " + awardID; 
 
 			rs = s.executeQuery(sql);
 			conn.commit();
 
 			while(rs.next()){
 				String strColumn = null;
 				int   iColumn = 0;
 				Disbursement disb = new Disbursement();
 				
 				iColumn = rs.getInt("disb_seq_num");
 				if(iColumn != 0) disb.setDisbSeqNum(BigDecimal.valueOf(iColumn));
 
 				java.sql.Date dtColumn = rs.getDate("disb_dt");
 				if(dtColumn != null) {
 					disb.setSchedDisbDt(new org.exolab.castor.types.Date(dtColumn));
 					disb.setActualDisbDt(new org.exolab.castor.types.Date(dtColumn));
 				}
 				
 				disb.setDisbNetAmt(new BigDecimal(rs.getDouble("disb_net_amt")).setScale(2, BigDecimal.ROUND_DOWN));
 				
 				strColumn = rs.getString("disb_stat_code");
 				if(strColumn != null) disb.setDisbStatCd(strColumn);
 				
 				dtColumn = rs.getDate("disb_stat_dt");
 				if(dtColumn != null) disb.setDisbStatDt(new org.exolab.castor.types.Date(dtColumn));
 				
 				strColumn = rs.getString("disb_hold");
 				if(strColumn != null) disb.setDisbHold(strColumn);
 				
 				
 				awd.addDisbursement(disb);
 			}
 		} catch(SQLException e){
 			log.warn("Error retrieveing data from disbursement table", e);
 		}
 		
 		return;	
 	}
 	private Contacts getContacts(int contactID){
 		Contacts contacts = new Contacts();
 		ResultSet rs = null;
 		try{
 			Statement s = conn.createStatement();
 			String sql = "select * from contact where contact_id = " + contactID; 
 
 			rs = s.executeQuery(sql);
 			conn.commit();
 			
 			while(rs.next() ){
 				String strColumn = null;
 				
 				for(int i = 1; i <= 4; i++){
 					strColumn = rs.getString("phone_" + i);
 					if(strColumn != null) {
 						Phone phone = new Phone();
 						phone.setPhoneNum(strColumn.trim());
 						strColumn = rs.getString("phone_type_" + i);
 						if(strColumn == null) { strColumn = "O";}
 						phone.setPhoneNumType(strColumn.trim());
 						
 						contacts.addPhone(phone);
 					}
 				}				
 
 				AddressInfo address = new AddressInfo();
 				contacts.setAddressInfo(address);
 				
 				strColumn = rs.getString("address_type");
 				if(strColumn != null) address.setAddressType(strColumn);
 				
 				strColumn = rs.getString("addr_valid_ind");
 				if(strColumn != null) address.setAddrValidInd(strColumn.equalsIgnoreCase("y") ? true : false);
 
 				java.sql.Date dtColumn = rs.getDate("addr_valid_dt");
 				if(dtColumn != null) address.setAddrValidDt(new org.exolab.castor.types.Date(dtColumn));
 				
 				strColumn = rs.getString("address_1");
 				if(strColumn != null) address.addAddr(strColumn.trim());
 				
 				strColumn = rs.getString("address_2");
 				if(strColumn != null) address.addAddr(strColumn.trim());
 				
 				strColumn = rs.getString("address_3");
 				if(strColumn != null) address.addAddr(strColumn.trim());
 				
 				strColumn = rs.getString("city");
 				if(strColumn != null) address.setCity(strColumn.trim());
 				
 				strColumn = rs.getString("state");
 				if(strColumn != null) address.setStateProv(strColumn);
 				
 				strColumn = rs.getString("postal_code");
 				if(strColumn != null) address.setPostalCd(strColumn.trim());
 				
 				strColumn = rs.getString("email");
 				if(strColumn != null) contacts.setEmail(strColumn);
 				
 
 			}
 		}catch(SQLException e){
 			log.warn("Error retrieveing data from contact table", e);
 		}
 		
 		return contacts;
 	}
 	
 	private void setError(MeteorDataProviderInfo mdpi, String errorMessage, String errorLevel){
 		MeteorDataProviderMsg errs = new MeteorDataProviderMsg();
 		mdpi.addMeteorDataProviderMsg(errs);
 		errs.setRsMsg(errorMessage);
 		errs.setRsMsgLevel(errorLevel);
 		
 		return;
 	}		
 	
 }
 
