 package com.caa.bspace;
 
 import java.util.*;
 
 import org.htmlcleaner.*;
 
 public class BSpaceClass {
     
     private static final String BSPACE_CLASS_BASE_URL = "https://bspace.berkeley.edu/portal/site/";
     private static final String XPATH_SIDEBAR = "//div[@id='toolMenu']//span";
     
     private static final String[] supportedTools = { "Syllabus", "Resources", "Gradebook" };
     
     private BSpaceUser user;
     public String uuid, name;
     private LinkedList<String> tools;
     
     public BSpaceClass(BSpaceUser user, String uuid, String name) {
         this.user = user;
         this.uuid = uuid;
         this.name = name;
         this.tools = new LinkedList<String>();
     }
     
     public void parseSidebar() {
         String url = BSPACE_CLASS_BASE_URL + uuid;
         String html = user.openPage(url);
         
         HtmlCleaner cleaner = new HtmlCleaner();
         TagNode root = cleaner.clean(html);
         
         try {
             Object[] sidebarEls = root.evaluateXPath(XPATH_SIDEBAR);
             HashSet<String> supported = new HashSet<String>(Arrays.asList(supportedTools));
             HashSet<String> actual = new HashSet<String>();
             for (Object obj : sidebarEls) {
                 actual.add(((TagNode) obj).getText().toString());
             }
             supported.retainAll(actual); // Intersect support and actual tool list; result stored in supported
             tools.addAll(supported);
         } catch (Exception e) {
             System.out.println("I think I will print a stacktrace.");
             e.printStackTrace();
         }
     }
 
 }
