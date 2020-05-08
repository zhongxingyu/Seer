 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package project_hibernate;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 
 /**
  *
  * @author sean
  */
 public class Driver {
     
     private static final String HELP_MSG = 
             "* ---------------- Commands ---------------- *\n"
             + "* create, load, find <congressman_id>        *\n"
             + "* congressmen, districts, committees, states *\n"
            + "* congressmanInDistrict <district_name>      *\n"
             + "* congressmanInState <state_code>            *\n"
             + "* congressmanInCommittee <committee_name>    *\n"
             + "* quit                                       *\n"
             + "* ------------------------------------------ *";
     
     public static void main(String[] args) {
         
         BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
         String command = "";
         
         Class[] clases = {Congressman.class, Committee.class, State.class, District.class};
         
         HibernateContext.addClasses(clases);
         
         do {
             System.out.println();
             System.out.println(HELP_MSG);
             System.out.print("Command? ");
             
             try {
                 command = stdin.readLine();
             }
             catch(java.io.IOException ex) {
                 command = "?";
             }
             
             String parts[] = command.split(" ");
             
             if(command.equalsIgnoreCase("create")) {
                 HibernateContext.createSchema();
             }
             
             else if(command.equalsIgnoreCase("load")) {
                 State.load();
                 District.load();
                 Congressman.load();
                 Committee.load();
             }
             
             else if(command.equalsIgnoreCase("congressmen")) {
                 Congressman.list();
             }
             
             else if(command.equalsIgnoreCase("districts")) {
                 District.list();
             }
             
             else if(command.equalsIgnoreCase("committees")) {
                 Committee.list();
             }
             
             else if(command.equalsIgnoreCase("states")) {
                 State.list();
             }
             
             else if(parts[0].equalsIgnoreCase("find") && parts.length >= 2) {
                 try {
                     int id = Integer.parseInt(parts[1]);
                     Congressman congressman = Congressman.find(id);
                     
                     if(congressman != null)
                         congressman.printInSession();
                     else
                         System.out.printf("*** No congressman with id %d\n", id);
                 } catch(NumberFormatException ex) {
                     System.out.println(ex.getMessage());
                 }
             }
             
             else if(parts[0].equalsIgnoreCase("congressmanInDistrict") 
                     && parts.length >= 2) {
             	String districtName = "";
             	for (int i = 1; i < parts.length; i++)
             	{
             		districtName += parts[i];
             		if (i != parts.length - 1)
             			districtName += " ";
             	}
                 Congressman.congressmanInDistrict(districtName);
             }
             
             else if(parts[0].equalsIgnoreCase("congressmanInState") 
                     && parts.length >= 2) {
                 Congressman.congressmanInState(parts[1]);
             }
             
             else if(parts[0].equalsIgnoreCase("congressmanInCommittee") 
                     && parts.length >= 2) {
                 Congressman.congressmanInCommittee(parts[1]);
             }
             
         } while(!command.equalsIgnoreCase("quit"));
         
     }
     
 }
