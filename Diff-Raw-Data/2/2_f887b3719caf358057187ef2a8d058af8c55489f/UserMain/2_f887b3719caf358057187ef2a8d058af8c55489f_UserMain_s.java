 package com.aci;
 
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.FileSystemXmlApplicationContext;
 
 public class UserMain {
 	public static void main(String[] args) {
		ApplicationContext context = new FileSystemXmlApplicationContext("bin/beans.xml");
 		User user = context.getBean("user", User.class);
 		user.printSessions();
 	}
 }
