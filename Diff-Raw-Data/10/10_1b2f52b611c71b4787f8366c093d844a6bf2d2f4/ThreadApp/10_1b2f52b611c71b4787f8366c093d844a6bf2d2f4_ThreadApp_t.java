 package org.zoneproject.extractor;
 
 /*
  * #%L
  * ZONE-extractor-start
  * %%
  * Copyright (C) 2012 ZONE-project
  * %%
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  * #L%
  */
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 
 /**
  *
  * @author Desclaux Christophe <christophe@zouig.org>
  */
 public class ThreadApp extends Thread{
     private static final org.apache.log4j.Logger  logger = org.apache.log4j.Logger.getLogger(ThreadApp.class);
     private String app;
     public ThreadApp(String app) {
         this.app = app;
     }
     public void run(){
         logger.info("Starting annotation process for "+app);
         try {
             Class.forName(app).newInstance();
         } catch (InstantiationException ex) {
            Logger.getLogger(ThreadApp.class.getName()).log(Level.WARNING, null, ex);
         } catch (IllegalAccessException ex) {
            Logger.getLogger(ThreadApp.class.getName()).log(Level.WARNING, null, ex);
         } catch (ClassNotFoundException ex) {
            Logger.getLogger(ThreadApp.class.getName()).log(Level.WARNING, null, ex);
        } catch (Exception ex){
            Logger.getLogger(ThreadApp.class.getName()).log(Level.WARNING, null, ex);
         }
         
         try{  Thread.currentThread().sleep(30000);}catch(InterruptedException ie){}
         this.run();
    }
 }
