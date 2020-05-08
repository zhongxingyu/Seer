 package com.crs.action;
 
 import com.opensymphony.xwork2.ActionSupport;
 
 public class CrpAction extends ActionSupport {
 
 	public String execute() {
 		return SUCCESS;
 	}
 	
 	public String confirm() {
		Boolean removed = true;
 		//remove member from crp
		
		if (removed)
			return SUCCESS;
		else
			return ERROR;
 	}
 }
