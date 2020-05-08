 package com.aman.dao;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import com.aman.Jdbc.ConnectionUtils;
 import com.aman.domain.Book;
 import com.aman.domain.Record;
 import com.aman.domain.Student;
 
 public class IssueDao {
 
 	Book book1 = new Book();
 
 	List<Student> studentList = new ArrayList<Student>();
 
 	public void searchStudent(Student student) {
 		// TODO Auto-generated method stub
 		String fName = student.getFirstName();
 		String lName = student.getLastName();
 		Connection con = ConnectionUtils.getConnection();
 		String query = "SELECT * from Student where firstName=? and lastName=? ";
 		try {
 			PreparedStatement statement = con.prepareStatement(query);
 			statement.setString(1, student.getFirstName());
 			statement.setString(2, student.getLastName());
 			ResultSet resultSet = statement.executeQuery();
 			while (resultSet.next()) {
 				Student student1 = new Student();
 				student1.setFirstName(resultSet.getString("firstName"));
 				student1.setLastName(resultSet.getString("lastName"));
 				student1.setStudentId(resultSet.getString("studentId"));
 				student1.setAddress(resultSet.getString("address"));
 				student1.setEmailId(resultSet.getString("email"));
 				student1.setPhoneNumber(resultSet.getInt("phoneNumber"));
 				studentList.add(student1);
 			}
 		} catch (Exception ex) {
 			throw new IllegalStateException(ex);
 		} finally {
 			try {
 				con.close();
 			} catch (Exception ex1) {
 				throw new IllegalStateException(ex1);
 			}
			
 
 		}
 	}
 
	
 	String bookRecordId;
 
 	List<Record> recordList = new ArrayList<Record>();
 
 	public void createRecord(Record record) {
 		// TODO Auto-generated method stub
 		Connection con = ConnectionUtils.getConnection();
 		String query = "SELECT * from BookRecord where bookRecordId =?";
 		try {
 			PreparedStatement statement = con.prepareStatement(query);
 			statement.setString(1, record.getBookRecord());
 			ResultSet resultSet = statement.executeQuery();
 			while (resultSet.next()) {
 				Record record1 = new Record();
 				record1.setBookRecord(resultSet.getString("bookRecordId"));
 				record1.setBookId(resultSet.getString("bookId"));
				record1.setStatus(resultSet.getString("status "));
 				record1.setStudentId(resultSet.getString("studentId"));
 				recordList.add(record1);
 			}
 		} catch (Exception ex) {
 			throw new IllegalStateException(ex);
 		} finally {
 			try {
 				con.close();
 			} catch (Exception ex1) {
 				throw new IllegalStateException(ex1);
 			}
 		}
 
 	}
 
 	Student student2;
 	Record record2;
 
 	public void updateRecord() {
 
 		String studentid;
 		Iterator<Student> itr = studentList.iterator();
 		while (itr.hasNext()) {
 			student2 = (Student) itr.next();
 		}
 		studentid = student2.getStudentId();
 		String bookRecordId;
 		Iterator<Record> itr1 = recordList.iterator();
 		while (itr1.hasNext()) {
 			record2 = (Record) itr1.next();
 		}
 		bookRecordId = record2.getBookRecord();
 		Connection con = ConnectionUtils.getConnection();
		String query = "UPDATE BookRecord set status=? and studentId=? where bookRecordId=? ";
 		try {
 			PreparedStatement statement = con.prepareStatement(query);
			statement.setString(1, "issued");
			statement.setString(2, student2.getStudentId());
			statement.setString(3, record2.getBookRecord());
			ResultSet result = statement.executeQuery();
 		} catch (Exception ex) {
 			throw new IllegalStateException(ex);
 		} finally {
 			try {
 				con.close();
 			} catch (Exception ex1) {
 				throw new IllegalStateException(ex1);
 			}
 		}
 	}
 
 	List<Record> recList = new ArrayList<Record>();
 
 	public List<Record> getRecord() {
 		Connection con = ConnectionUtils.getConnection();
 		String qury = "SELECT * from BookRecord";
 		try {
 			PreparedStatement statement = con.prepareStatement(qury);
 			ResultSet resultSet = statement.executeQuery();
 			while (resultSet.next()) {
 				Record rec = new Record();
 				rec.setBookRecord(resultSet.getString("bookRecordId"));
 				rec.setBookId(resultSet.getString("bookId"));
 				rec.setStatus(resultSet.getString("status"));
 				rec.setStudentId(resultSet.getString("studentId"));
 				recList.add(rec);
 			}
 		} catch (Exception ec) {
 			throw new IllegalStateException(ec);
 		}
 		return recList;
 	}
 }

