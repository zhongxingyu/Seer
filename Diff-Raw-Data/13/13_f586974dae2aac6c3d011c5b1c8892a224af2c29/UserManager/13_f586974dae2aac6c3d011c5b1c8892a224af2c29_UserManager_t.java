 package com.cs301w01.meatload.authentication.Controllers;
 
 import android.content.Context;
 import android.database.Cursor;
 import com.cs301w01.meatload.authentication.Model.Patient;
 import com.cs301w01.meatload.authentication.Model.Specialist;
 import com.cs301w01.meatload.authentication.Model.User;
 import com.cs301w01.meatload.authentication.querygenerator.UserQueryGenerator;
 import com.cs301w01.meatload.controllers.FController;
 import com.cs301w01.meatload.model.Album;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 /**
  * This class handles all the intermediate steps between the db and the GUI/activity.
  */
 public class UserManager implements FController{
 
     private Context context;
     
     public UserManager(Context c) {
         
         this.context = context;
         
     }
     
     public User authenticateUser(String userName, String password) {
 
         String encryptedPwd = PasswordManager.generatePassword(password);
         
         Cursor c = new UserQueryGenerator(context).logUserIn(userName, encryptedPwd);
         
         if(c.getCount() == 0) {
 
             return null;
 
         } else {
 
             //c has the following: COL_ID, COL_NAME, COL_USERNAME, COL_EMAIL, COL_ROLE
             String id = c.getString(c.getColumnIndex(UserQueryGenerator.COL_ID));
             String name = c.getString(c.getColumnIndex(UserQueryGenerator.COL_NAME));
             String email = c.getString(c.getColumnIndex(UserQueryGenerator.COL_EMAIL));
             String role = c.getString(c.getColumnIndex(UserQueryGenerator.COL_ROLE));
             
             User u;
             
             if(role.equals(UserQueryGenerator.PATIENT_ROLE)) {
 
                 Collection<Album> albums = new ArrayList<Album>();
                 //TODO: get albums
 
                int specialistID = 0;
                //TODO: get specialist ID
                
                u = new Patient(name, email, albums, specialistID);
 
                 return u;
                 
             } else {
 
                 Collection<Patient> patients = new ArrayList<Patient>();
 
                 //TODO: get patients
 
 
                 u = new Specialist(name, email, patients);
                         
                 return u;
 
             }
         }
     }
 
 }
