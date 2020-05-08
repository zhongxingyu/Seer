 package com.errclipse;
 
import com.errclipse.orm.connector.ConnectToORM;
 import com.errclipse.rmi.connectionManagement.RegisterServer;
 
 
 public class runner {
     public static void main(String args[]) throws Exception{
     	
 		RegisterServer.initiateServer();
 		
 		System.out.println("rmi server start");
		
		ConnectToORM.recommendSolution(24);
     }
 }
