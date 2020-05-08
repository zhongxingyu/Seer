 package org.demond.java.dereference;
 
 
 import org.apache.log4j.Logger;
 
 /**
  * Created by IntelliJ IDEA.
  * User: degtyarev.dm
  */
 public class Start
 {
     private static Logger logger = Logger.getLogger(Start.class);
 
     public static void main(String [] args)
     {
         logger.info("[start]");
         //http://bit.ly/xZ8zCZ  ->  http://autokadabra.ru/shouts/47417
         //http://clck.ru/0q     ->  http://google.com
         //http://is.gd/qQ9jHv   ->  http://yandex.ru
         //http://goo.gl/K2zBM   ->  http://habrahabr.ru/
         //http://t.co/TENldP40  ->  http://moto.ly/rls2
         //http://t.co/OH0GTAUS  ->  https://docs.google.com/file/d/0B7_JZGebl8cKdzNDQlNPZUVRcXlObWhZcGVqMkR2Zw/edit?pli=1
         //http://is.gd/OREr5X   ->  http://ianfette.org malware
 
        String url = "http://clckru/0q";
        try{

         Dereference dereference = new Dereference();
         System.out.println(dereference.dereferenceUrl(url));
//        for(String result: dereference.dereferenceAndCheckUrl(url))

//        System.out.print(result+" ");
 
        }
        catch(Exception e)
        {
            System.out.print("Trobls");
        }
     }
 
 }
