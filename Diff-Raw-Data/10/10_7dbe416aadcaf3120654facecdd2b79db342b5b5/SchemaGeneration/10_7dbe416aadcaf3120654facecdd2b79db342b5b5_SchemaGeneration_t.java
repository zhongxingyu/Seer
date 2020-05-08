 package com.eyecall.database;
 
 import org.hibernate.cfg.Configuration;
 import org.hibernate.tool.hbm2ddl.SchemaExport;
 
 public class SchemaGeneration {
 	public static void main(String[] args) {
 		Configuration cfg = new Configuration()
 		.addAnnotatedClass(Volunteer.class)
		.addAnnotatedClass(Location.class)
 		.setProperty("hibernate.hbmddl2.auto", "create")
 		.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
 		new SchemaExport(cfg).setDelimiter(";").setOutputFile("gen/schema.sql").setFormat(true).execute(true, false, false, false);
 	}
 }
