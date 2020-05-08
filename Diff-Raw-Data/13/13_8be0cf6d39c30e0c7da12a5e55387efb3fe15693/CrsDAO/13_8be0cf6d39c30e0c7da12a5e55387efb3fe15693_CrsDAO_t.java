 package com.crs.dao;
 
 /**
  * Ibatis persistence framework imports
  */
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.ibatis.session.SqlSession;
 import org.apache.ibatis.session.SqlSessionFactory;
 
 
 import com.crs.dao.MyBatisConnectionFactory;
 import com.crs.model.CarPoolForm;
 import com.crs.model.CarPoolMemberForm;
 import com.crs.model.EmployeeForm;
 /*
  * Employee Bean import
  */
 
 /**
  * This class is the Data Access Object class
  * for which will be used to make all the direct database 
  * transactions using ibatis/mybatis
  * @author Subbu
  *
  */
 public class CrsDAO {
 	
 	//The database session
 	private SqlSessionFactory sqlSessionFactory; 
 	
 	public CrsDAO(){
 		/*
 		 * making a connection instance with the database using ibatis/mybatis
 		 */		
 		sqlSessionFactory = MyBatisConnectionFactory.getSqlSessionFactory();
 	}
 	
 	/**
 	 * This method is used to obtain a employee record using his/her
 	 * EmployeeID.
 	 * @param employee
 	 * @return EmployeeForm
 	 */
 	public EmployeeForm getLoginRecord(EmployeeForm employee){
 		//opens the database connection instance
 		SqlSession session = sqlSessionFactory.openSession();
 		try{
 			EmployeeForm employeeDetail = (EmployeeForm) session.selectOne("Employee.getRecordWithEmail",employee);
 			return employeeDetail;
 		}finally {
 			session.close();
 		}
 	}
 	
 	public EmployeeForm getLoginRecordWithEmpID(Integer empID){
 		//opens the database connection instance
 		SqlSession session = sqlSessionFactory.openSession();
 		try{
 			EmployeeForm employeeDetail = (EmployeeForm) session.selectOne("Employee.getRecordWithEmpID",empID);
 			return employeeDetail;
 		}finally {
 			session.close();
 		}
 	}
 	
 	/**
 	 * This method is used to insert a record into the 
 	 * Employee table as a result of new user registration
 	 * @param employee
 	 */
 	public void insertEmployeeRecord(EmployeeForm employee){
 		SqlSession session = sqlSessionFactory.openSession();
 		try{
 			int a = session.insert("Employee.insertRecord",employee);
 			System.out.println("a:" + a);
 			session.commit();
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 		finally {
 			session.close();
 		}
 	}
 	
 	public CarPoolForm getCarPoolGroup(){
 		SqlSession session = sqlSessionFactory.openSession();
 		try{
 			CarPoolForm carPoolGroup = session.selectOne("Employee.getCarPoolGroup");
 			return carPoolGroup;
 		}finally {
 			session.close();
 		}
 		 
 	}
 	
 	public CarPoolForm getCarPoolGroupDetails(Integer carpoolID){
 		SqlSession session = sqlSessionFactory.openSession();
 		try{
 			CarPoolForm carPoolGroup = session.selectOne("Carpool.getCarPoolDetails",carpoolID);
 			return carPoolGroup;
 		}finally {
 			session.close();
 		}
 		 
 	}
 	
 	public void processEmergencyRequest(CarPoolMemberForm carPoolMember){
 		SqlSession session = sqlSessionFactory.openSession();
 		try{
 			int carpoolID = session.selectOne("CarpoolMember.getFreeCarpoolGroups");
 			carPoolMember.setCarpoolID(carpoolID);
 			session.insert("CarpoolMember.insertRecord", carPoolMember);
 			session.update("Employee.updatePointsForEmergencyUsage", carPoolMember.getEmployeeID());
 			session.commit();
 		}finally{
 			session.close();
 		}
 	}
 	
 	public List fetchMembersEmailID(Integer carpoolID){
 		SqlSession session = sqlSessionFactory.openSession();
 		List<Object> tempList;
 		try{
 			tempList = session.selectList("CarpoolMember.getEmailID", carpoolID);
 			return tempList;
 		}finally{
 			session.close();
 		}
 	}
 	
 	public void checkOut(Integer carpoolID, Integer empID){
 		SqlSession session = sqlSessionFactory.openSession();
 		try{
 			session.update("Carpool.checkout",carpoolID);
 			session.update("CarpoolMember.resetTemporaryDrivers", carpoolID);
			session.update("CarpoolMember.resetTemporaryDrivers2", carpoolID);
 			session.delete("CarpoolMember.removeTemporaryMembers", carpoolID);
 			session.update("Employee.updatePointsForDrive", empID);
 			session.update("CarpoolMember.updatePickUpFlag", carpoolID);
 			session.commit();
 		}finally {
 			session.close();
 		}
 	}
 	
 	public void checkIn(Integer carpoolID){
 		SqlSession session = sqlSessionFactory.openSession();
 		try{
 			session.update("Carpool.checkin",carpoolID);
 			session.commit();
 		}finally {
 			session.close();
 		}
 	}
 	
 	public void createNewMember(CarPoolMemberForm carPoolMember){
 		SqlSession session = sqlSessionFactory.openSession();
 		try{
 			 session.insert("Employee.createNewMember",carPoolMember);
 			 session.commit();
 		}finally {
 			session.close();
 		}
 	}
 	
 	public void createNewCarPoolGroup(){
 		SqlSession session = sqlSessionFactory.openSession();
 		try{
 			session.insert("Employee.createNewCarPoolGroup");
 			session.commit();
 		}finally {
 			session.close();
 		}
 	}
 	
 	public ArrayList<CarPoolMemberForm> retrieveDrivers(){
 		SqlSession session = sqlSessionFactory.openSession();
 		ArrayList<CarPoolMemberForm> carpoolDriversList  = new ArrayList<CarPoolMemberForm>();
 		List<Object> tempList = new ArrayList<Object>();
 		Iterator<Object> it;
 	
 		
 		try{
 			//tempList retrieves the result set from the database
 			tempList = session.selectList("CarpoolMember.getCarpoolMemberDrivers");
 			it  = tempList.iterator();	
 			while(it.hasNext()){
 				carpoolDriversList.add((CarPoolMemberForm) it.next());
 			}			
 			return carpoolDriversList;
 		}finally {
 			session.close();
 		}
 	}
 	
 	public void updateCurrentDriver(int currentDriver){
 		SqlSession session = sqlSessionFactory.openSession();		
 		
 		try{
 			session.update("CarPoolMember.updateCurrentDriver",currentDriver);
 			session.commit();
 		}finally {
 			session.close();
 		}
 	}
 	
 	public void updateNextDriver(int nextDriver){
 		SqlSession session = sqlSessionFactory.openSession();		
 		
 		try{
 			session.update("CarPoolMember.updateNextDriver",nextDriver);	
 			session.commit();
 		}finally {
 			session.close();
 		}
 	}
 	
 	public CarPoolMemberForm getNextDriver(int currentDriver, int flag){
 		SqlSession session = sqlSessionFactory.openSession();		
 		CarPoolMemberForm nextDriver;
 		
 		try{
 			if(flag == 0)
 				nextDriver = session.selectOne("CarPoolMember.getNextDriver",currentDriver);
 			else
 				nextDriver = session.selectOne("CarPoolMember.getFirstDriver");
 			return nextDriver;
 		}finally {
 			session.close();
 		}
 	}
 	
 	public void updateTemporaryDriver(Integer empID){
 		SqlSession session = sqlSessionFactory.openSession();
 		try{
 			session.update("CarpoolMember.updateTemporaryDriver", empID);
 			session.commit();
 		}finally{
 			session.close();
 		}
 	}
 	
 	@SuppressWarnings("rawtypes")
 	public List retrievePassengers(Integer carpoolID){
 		SqlSession session = sqlSessionFactory.openSession();
 		List<Object> tempList = new ArrayList<Object>();
 
 		try{
 			//tempList retrieves the result set from the database
 			tempList = session.selectList("CarpoolMember.getCarpoolMemberPassengers",carpoolID);
 			return tempList;
 			
 		}finally {
 			session.close();
 		}
 	}
 	
 	public List retrieveMembers(CarPoolMemberForm carPoolMember){
 		SqlSession session = sqlSessionFactory.openSession();
 		List<Object> tempList = new ArrayList<Object>();
 
 		try{
 			//tempList retrieves the result set from the database
 			tempList = session.selectList("CarpoolMember.getCarpoolMembers",carPoolMember);
 			return tempList;
 			
 		}finally {
 			session.close();
 		}
 	}
 	
 	@SuppressWarnings("rawtypes")
 	public CarPoolForm getLatestCarpoolGroup(){
 		SqlSession session = sqlSessionFactory.openSession();
 		
 		try{
 			CarPoolForm objCarPoolForm = session.selectOne("Carpool.getLatestCarpool");
 			if(objCarPoolForm != null){
 				System.out.println("retrieved latest carpool id");
 			}
 			return objCarPoolForm;
 			
 		}finally {
 			session.close();
 		}
 	}
 	
 	/**
 	 * This method will cancel car pool pick up 
 	 * @param employee
 	 * @return
 	 */
 	
 	public void cancelCarpoolPickUp(CarPoolMemberForm carPoolMember){
 		SqlSession session = sqlSessionFactory.openSession();
 		try{
 			session.selectOne("CarpoolMember.cancelPickup",carPoolMember);
 			session.update("Employee.updatePointsForCancelPickUp", carPoolMember.getEmployeeID());
 			session.commit();
 		}finally {
 			session.close();
 		}
 	}
 	
 	public void optOutCrp(Integer empID){
 		SqlSession session = sqlSessionFactory.openSession();
 		try{
 			session.delete("CarpoolMember.removeMember", empID);
 			session.delete("Employee.removeMember", empID);
 			session.commit();
 		}finally{
 			
 		}
 	}
 	
 	/**
 	 * This method will cancel car pool pick up 
 	 * @param employee
 	 * @return
 	 */
 	
 	public void cancelCarpoolDrive(CarPoolMemberForm carPoolMember){
 		SqlSession session = sqlSessionFactory.openSession();
 		try{
			session.update("CarpoolMember.cancelDrive",carPoolMember);
			session.update("CarpoolMember.cancelDrive2",carPoolMember);
			session.commit();
 		}finally {
 			session.close();
 		}
 	}
 	
 	
 	/**
 	 * This method will return an employee's details given his ID
 	 * @param employee
 	 * @return
 	 */
 	public EmployeeForm getEmployeeRecord(Integer empID){
 		//opens the database connection instance
 		SqlSession session = sqlSessionFactory.openSession();
 		try{
 			EmployeeForm employeeDetail = (EmployeeForm) session.selectOne("Employee.getRecord",empID);
 			return employeeDetail;
 		}finally {
 			session.close();
 		}
 	}
 	
 	public void optOutCarpool(Integer empID){
 		//opens the database connection instance
 		SqlSession session = sqlSessionFactory.openSession();
 		try{
 			 session.delete("CarpoolMember.optOutCarpool",empID);
 			 session.update("Employee.updatePointsForOptingOut", empID);
 			 session.commit();
 		}finally {
 			session.close();
 		}
 	}
 	
 	/**
 	 * This method will return an employee's details from car pool member given his ID
 	 * @param employee
 	 * @return
 	 */
 	public CarPoolMemberForm getMemberInfo(int employeeID){
 		//opens the database connection instance
 		SqlSession session = sqlSessionFactory.openSession();
 		try{
 			CarPoolMemberForm carPoolMember = (CarPoolMemberForm) session.selectOne("CarpoolMember.getMemberDetails",employeeID);
 			System.out.println("Details : "+carPoolMember.getCarpoolID());
 			return carPoolMember;
 		}finally {
 			session.close();
 		}
 	}
 
 }
