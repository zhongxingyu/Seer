 /*
  * Created by Andrey Markelov 11-11-2012.
  * Copyright Mail.Ru Group 2012. All rights reserved.
  */
 package ru.mail.jira.plugins.up;
 
 import java.util.Comparator;
 import java.util.Map;
 
 class ValueComparator
     implements Comparator<String>
 {
     Map<String, String> base;
 
     public ValueComparator(Map<String, String> base)
     {
         this.base = base;
     }
 
     public int compare(String a, String b)
     {
        if (base.get(a).compareTo(base.get(b)) <= 0)
         {
             return -1;
         }
         else
         {
             return 1;
         }
     }
 }
