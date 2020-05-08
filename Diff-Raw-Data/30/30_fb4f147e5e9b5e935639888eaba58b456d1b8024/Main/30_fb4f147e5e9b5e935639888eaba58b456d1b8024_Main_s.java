 package com;
 
 import db.ComDb;
 
 public class Main
 {
 	public static ComDb db;
	public static void Main(String[] args)
 	{
		db = new ComDb();
		db.connect("");
		db.createDb(args[1]);
 	}
 }
