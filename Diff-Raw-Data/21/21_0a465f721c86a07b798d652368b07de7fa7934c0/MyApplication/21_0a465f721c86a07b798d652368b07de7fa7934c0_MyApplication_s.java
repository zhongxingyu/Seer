 package com.example.restfulclient.helpers;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Application;
 
 public class MyApplication extends Application
 {
     public List<Category> categories;
     
     public Category c;
     public Book b;
     
     public boolean offline = false;
     
     public MyApplication()
     {
 	categories = new ArrayList<Category>();
     }
     
     public int getById(String id)
     {
 	for(int i= 0; i < categories.size(); i++)
 	{
 	    if(categories.get(i).getCategoryId().equals(id))
 		return i;
 	}
 	
 	return -1;
     }
 }
