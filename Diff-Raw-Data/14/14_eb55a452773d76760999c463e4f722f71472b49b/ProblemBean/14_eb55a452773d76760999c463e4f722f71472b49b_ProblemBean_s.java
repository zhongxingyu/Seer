 package com.cvparse.beans;
 
import javax.ejb.EJB;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.RequestScoped;
 
 @RequestScoped
 @ManagedBean
 public class ProblemBean {
 
 	private int id;
 
 	public int getId() {
 		return id;
 	}
 
 	public void setId(int id) {
 		this.id = id;
 	}
 	
 }
