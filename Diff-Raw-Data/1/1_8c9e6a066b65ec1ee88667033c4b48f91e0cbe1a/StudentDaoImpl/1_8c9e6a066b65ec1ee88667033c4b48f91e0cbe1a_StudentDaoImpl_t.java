 package com.frollz.dao.impl;
 
 import java.util.List;
 
 import org.hibernate.SessionFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Repository;
 
 import com.frollz.dao.StudentDAO;
 import com.frollz.model.Student;
 @Repository
 public class StudentDaoImpl implements StudentDAO {
 	@Autowired
 	private SessionFactory session;
 
 	@Override
 	public void add(Student student) {
 		session.getCurrentSession().save(student);
 	}
 
 	@Override
 	public void edit(Student student) {
 		session.getCurrentSession().update(student);
 	}
 
 	@Override
 	public void delete(int studentID) {
 		session.getCurrentSession().delete(getStudent(studentID));
 	}
 
 	@Override
 	public Student getStudent(int studentID) {
 		return (Student)session.getCurrentSession().get(Student.class, studentID);
 	}
 
 	@Override
 	public List getAllStudents() {
 		return session.getCurrentSession().createQuery("from Student").list();
 	}
 
 }
