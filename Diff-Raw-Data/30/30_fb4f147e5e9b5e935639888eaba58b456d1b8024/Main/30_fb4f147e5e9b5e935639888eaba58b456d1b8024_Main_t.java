 package com;
 
import java.io.IOException;

 import db.ComDb;
 
 public class Main
 {
 	public static ComDb db;
	public static void main(String[] args)
 	{
		System.out.println("Scm2Pgsql tool developed by eggnet at UVic.");
		try {
			if (args.length < 1)
			{
				throw new ArrayIndexOutOfBoundsException();
			}
			else
			{
				// Create the DB
				db = new ComDb();
				db.connect("");
				db.createDb(args[0]);
				
				// Run Bugzilla importer
				
			}
		}
		catch (ArrayIndexOutOfBoundsException e)
		{
			System.out.println("Usage scm2pgsql <DB Name>");
		}
 	}
 }
