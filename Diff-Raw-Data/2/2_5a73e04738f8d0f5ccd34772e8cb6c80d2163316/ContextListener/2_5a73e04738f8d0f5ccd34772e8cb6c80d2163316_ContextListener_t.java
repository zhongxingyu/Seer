 package com.appspot.accent.controller;
 
 import java.util.ArrayList;
 
 import javax.servlet.ServletContextEvent;
 import javax.servlet.ServletContextListener;
 
 import com.appspot.accent.model.Administrator;
 import com.appspot.accent.model.Categorie;
 import com.appspot.accent.model.CompetentieItem;
 import com.appspot.accent.model.Docent;
 import com.appspot.accent.model.Leerling;
 import com.appspot.accent.model.User;
 
 
 
 public class ContextListener implements ServletContextListener {
 
 	
 	private ArrayList<User> users = new ArrayList<User>();
 	private ArrayList<Categorie> cat;
     public void contextInitialized(ServletContextEvent sce) {
     	
     	Administrator admin = new Administrator("admin","admin");
     	users.add(admin);
     	ArrayList<CompetentieItem> items = admin.getAlleCompetentieItems();
     	
     	User l1 = new Leerling("leerling","leerling");
     	users.add(l1);
     	User l2 = new Leerling("leerling2","leerling2");
     	users.add(l2);
     	User d1 = new Docent("docent","docent");
     	users.add(d1);
    	User testleerling = new Leerling("leerling123,.","leerling123,.");
    	users.add(testleerling);
     	
     	//Voorbeeld collectie 1
     	Categorie cat1 = new Categorie("Samenwerken");
     	CompetentieItem c1 = new CompetentieItem("eerste competentie");
     	CompetentieItem c2 = new CompetentieItem("tweede competentie");
     	CompetentieItem c3 = new CompetentieItem("derde competentie");
     	admin.createCompetentie(c1);
     	admin.createCompetentie(c2);
     	admin.createCompetentie(c3);
     	cat1.voegItemToe(c1);
     	cat1.voegItemToe(c2);
     	cat1.voegItemToe(c3);
     	
     	
     	//Context attributen
     	sce.getServletContext().setAttribute("cat",cat);
     	sce.getServletContext().setAttribute("admin", admin);
     	sce.getServletContext().setAttribute("itemList", items);
     	sce.getServletContext().setAttribute("userList", users);
     }
 
 	
     public void contextDestroyed(ServletContextEvent sce) {
         // TODO Auto-generated method stub
     }
 	
 }
