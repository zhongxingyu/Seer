 package com.seitenbau.testing.dbunit.model;
 
 import com.seitenbau.testing.dbunit.generator.DataType;
 import com.seitenbau.testing.dbunit.generator.DatabaseModel;
 import com.seitenbau.testing.dbunit.generator.Table;
 
 public class Model extends DatabaseModel
 {
   public Model()
   {
     database("PersonDatabase");
     packageName("com.seitenbau.testing.dbunit.model");
 
     Table jobs = table("jobs") //
         .description("The table containing the jobs of a great company")
         .column("id", DataType.BIGINT) //
           .autoIdHandling() //
           .identifierColumn() //
         .column("title", DataType.VARCHAR) //
         .column("description", DataType.VARCHAR) //
       .build();
 
     Table teams = table("teams") //
         .description("The table containing the teams of a great company")
         .column("id", DataType.BIGINT) //
           .autoIdHandling() //
           .identifierColumn() //
         .column("title", DataType.VARCHAR) //
         .column("description", DataType.VARCHAR) //
         .column("membersize", DataType.BIGINT) //
       .build();
 
     Table persons = table("persons") //
         .description("The table containing the staff of a great company")
         .column("id", DataType.BIGINT) //
           .autoIdHandling() //
           .identifierColumn() //
         .column("first_name", DataType.VARCHAR) //
         .column("name", DataType.VARCHAR) //
          .description("Actually this column represents the last name of a person")
         .column("job_id", DataType.BIGINT) //
           .references(jobs.ref("id")) //
             .description("Assigns a job to the person")
         .column("team_id", DataType.BIGINT) //
           .references(teams.ref("id")) //
             .description("Assigns a team to the person")
       .build(); //
   }
 
 }
