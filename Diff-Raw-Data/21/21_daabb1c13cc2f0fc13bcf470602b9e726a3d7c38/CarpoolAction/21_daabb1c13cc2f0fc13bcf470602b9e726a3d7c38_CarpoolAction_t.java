 package com.crs.action;
 
 import com.opensymphony.xwork2.ActionSupport;
 
 public class CarpoolAction extends ActionSupport {
 
 	public String execute() {
 		return SUCCESS;
 	}
 	
 	public String confirm() {
		Boolean removed = true;
 		//remove member from carpool
		
		if(removed)
			return SUCCESS;
		else
			return ERROR;
 	}
 }
