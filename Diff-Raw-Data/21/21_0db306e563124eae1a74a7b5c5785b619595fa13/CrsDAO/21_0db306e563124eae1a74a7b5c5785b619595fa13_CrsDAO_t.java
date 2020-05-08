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
 			EmployeeForm employeeDetail = (EmployeeForm) session.selectOne("Employee.getRecord",employee);
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
 			session.insert("Employee.insertRecord",employee);
 		}finally {
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
 	
 	public void createNewMember(CarPoolMemberForm carPoolMember){
 		SqlSession session = sqlSessionFactory.openSession();
 		try{
 			 session.insert("Employee.createNewMember",carPoolMember);
 		}finally {
 			session.close();
 		}
 	}
 	
 	public CarPoolForm createNewCarPoolGroup(){
 		SqlSession session = sqlSessionFactory.openSession();
 		try{
 			CarPoolForm carPoolGroup = session.selectOne("Employee.getCarPoolGroup");
 			return carPoolGroup;
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
 	
 	/**
 	 * This method will cancel car pool request
 	 * @param employee
 	 * @return
 	 */
 	
 	public void cancelCarpoolPickUp(CarPoolMemberForm carPoolMember){
 		SqlSession session = sqlSessionFactory.openSession();
 		try{
			EmployeeForm employeeDetail = (EmployeeForm) session.selectOne("CarpoolMember.cancelPickup",carPoolMember.getEmployeeID());

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
 
 }
