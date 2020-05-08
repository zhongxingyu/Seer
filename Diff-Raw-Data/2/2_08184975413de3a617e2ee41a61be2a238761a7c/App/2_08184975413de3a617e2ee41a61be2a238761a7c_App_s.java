 package com.blogpost.maven.maven_spring;
 
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 public class App {
 	public static void main(String[] args) {
 		@SuppressWarnings("resource")
 		ApplicationContext context = new ClassPathXmlApplicationContext(
 				"Beans.xml");
 
 		MaintainVehicle maintain = new MaintainVehicle();
 
 		// vehicle1 bean
 		Vehicle obj1 = (Vehicle) context.getBean("vehicle1");
 		System.out.printf("I drive a %s.\n", obj1.getLongDescription());
 		System.out.printf("Is my %s tuned up? %s\n",
 				obj1.getShortDescription(), obj1.getServiced());
 		maintain.serviceVehicle(obj1);
 		System.out.printf("Is my %s tuned up, yet? %s\n\n", obj1.getMake(),
 				obj1.getServiced());
 
 		// vehicle2 bean
 		Vehicle obj2 = (Vehicle) context.getBean("vehicle2");
 		System.out.printf("My wife drives a %s.\n", obj2.getLongDescription());
 		System.out.printf("Is her %s clean? %s\n", obj2.getShortDescription(),
 				obj2.getWashed());
 		maintain.washVehicle(obj2);
 		System.out.printf("Is her %s clean, now? %s\n\n", obj2.getMake(),
 				obj2.getWashed());
 
 		// vehicle3 bean
 		Vehicle obj3 = (Vehicle) context.getBean("vehicle3");
		System.out.printf("Our son drives his %s to fast!\n", obj3.getType()
 				.toLowerCase());
 
 	}
 }
