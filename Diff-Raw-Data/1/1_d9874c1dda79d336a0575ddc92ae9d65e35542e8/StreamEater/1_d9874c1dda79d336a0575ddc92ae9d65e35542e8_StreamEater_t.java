 /*
  * This file is part of Bytecast.
  *
  * Bytecast is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Bytecast is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Bytecast.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 
 package edu.syr.bytecast.util;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 import org.apache.commons.io.IOUtils;
 
 public class StreamEater implements Runnable {
    
   private InputStream m_inputStream;
   private String m_string;
   private volatile boolean m_done;
   private boolean m_print;
 
   public StreamEater(InputStream stream) {
     m_inputStream = stream;
     m_done = false;
     m_print = false;
     Thread thread = new Thread(this);
     thread.setDaemon(true);
     thread.start();
   }
   
   public StreamEater(InputStream stream, boolean print) {
     m_inputStream = stream;
     m_done = false;
     m_print = print;
     Thread thread = new Thread(this);
     thread.setDaemon(true);
     thread.start();
   }
 
   @Override
   public void run(){
     try {
       m_string = IOUtils.toString(m_inputStream, "UTF-8");
      m_inputStream.close();
     } catch(Exception ex){
       ex.printStackTrace();
     }
     m_done = true;
   }
 
   public String getString(){
     while(m_done == false){
       try {
         Thread.sleep(100);
       } catch(Exception ex){
         ex.printStackTrace();
       }
     }
     return m_string;
   }
 }
