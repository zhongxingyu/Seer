 package com.fieldexpert.fbapi4j;
 
 public class PriorityDemo {
 
 	public static void main(String[] args) {
 		String email = args[0];
 		String password = args[1];
 
 		Configuration conf = new Configuration();
 		conf.setProperty("endpoint", "https://fieldexpert.fogbugz.com/");
 		conf.setProperty("email", email);
 		conf.setProperty("password", password);
 
 		Session session = conf.getSession();
 		for (Priority priority : session.findAll(Priority.class)) {
 			System.out.println(priority.getId() + " -> " + priority.getName());
 		}
 
		Priority priority = session.get(Priority.class, 1);
 		System.out.println(priority.getName());
 		session.close();
 	}
 
 }
