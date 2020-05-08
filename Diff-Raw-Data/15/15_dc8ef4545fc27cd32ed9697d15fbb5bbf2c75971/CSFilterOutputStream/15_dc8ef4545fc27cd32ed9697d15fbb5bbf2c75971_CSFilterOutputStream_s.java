 /*
  * Created on Feb 11, 2009
  */
 package com.delcyon.updater.client;
 
 import java.io.ByteArrayOutputStream;
 import java.io.FilterOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
import java.util.logging.Level;

import org.GNOME.Accessibility.Application;
 
 /**
  * @author jeremiah
  * @version $Id: $
  */
 public class CSFilterOutputStream extends FilterOutputStream
 {
     
     public static final int MAX_BUFFER_SIZE = 50;
     public static final int SYMBOL = '@';
     private ByteArrayOutputStream buffer = new ByteArrayOutputStream();   
     private CSFilter filter;
     private int symbolCount = 0;
     private CentralServicesRequest centralServicesRequest = null;
     
     
     /**
      * @param centralServicesRequest 
      * 
      */
     public CSFilterOutputStream(CSFilter filter, OutputStream outputStream, CentralServicesRequest centralServicesRequest)
     {
         super(outputStream);
         this.filter = filter;
         this.centralServicesRequest  = centralServicesRequest;
     }
 
     /**
      * override write method  
      */
     @Override
     public void write(int b) throws IOException {
         
         
         if (b == SYMBOL) //this could be a variable declaration, so start counting
         {
             if (symbolCount == 0)//gotten our first @ symbol, so increment the symbolCount se we start counting @'s
             {                
                 symbolCount++;
             }
             else if (symbolCount == 1) //got another @ symbol, so increment the symbolCount, and prep the buffer 
             {                
                 buffer.reset(); 
                 symbolCount++;                
             }
             else if (symbolCount == 2) //got another @ symbol, so increment the symbolCount, we are on the other side of the trigger now
             {                                
                 symbolCount++;                
             }
             else if (symbolCount == 3) //we've gotten a full variable declaration now, so see if it's something we know about
             {                
                 //trigger matches so output the new value, and then reset
                 if (filter.getTrigger().equals(buffer.toString()))
                 {
                     byte[] replacement =  filter.getReplacement(centralServicesRequest);
                     //Application.logger.log(Level.FINER, "Replacing '"+(char)SYMBOL+(char)SYMBOL+filter.getTrigger()+(char)SYMBOL+(char)SYMBOL+"' with '"+new String(replacement)+"'");
                     out.write(replacement);                   
                     symbolCount = 0;
                 }
                 else //trigger doesn't match, so reset
                 {                    
                     out.write(SYMBOL);
                     out.write(SYMBOL);
                     out.write(buffer.toByteArray());
                     out.write(SYMBOL);
                     out.write(b);
                     symbolCount = 0;
                 }
             }
         }
         else if (symbolCount == 1) //should have gotten another @ symbol, so reset
         {              
             out.write(SYMBOL);
             out.write(b);
             symbolCount = 0;
         }
         else if (symbolCount == 2) //start buffering things
         {         
             buffer.write(b);
             if (b == '\n') //hit a new line, so reset
             {
                 out.write(SYMBOL);
                 out.write(SYMBOL);
                 out.write(buffer.toByteArray());
                 symbolCount = 0;
                 //Application.logger.log(Level.FINER, "Reached newline flushing buffer");
             }
             else if (buffer.size() >= MAX_BUFFER_SIZE)
             {
                 out.write(SYMBOL);
                 out.write(SYMBOL);
                 out.write(buffer.toByteArray());
                 symbolCount = 0;
                 //Application.logger.log(Level.FINER, "Reached max buffer size :"+MAX_BUFFER_SIZE+" flushing buffer");
             }
         }
         else if (symbolCount == 3) //should have gotten another @ symbol, so reset
         {           
             out.write(SYMBOL);
             out.write(SYMBOL);
             out.write(buffer.toByteArray());
             out.write(SYMBOL);
             out.write(b);
             symbolCount = 0;
         }
         else //normal char, just write it out
         {         
             out.write(b);  
         }
           
         
       }
     
     /**
      * override write method  
      */
     @Override
     public void write(byte[] data, int offset, int length) throws IOException
     {
         for (int i = offset; i < offset + length; i++)
         {
             this.write(data[i]);
         }
     }
     
     /**
      * override write method  
      */
     @Override
     public void write(byte[] b) throws IOException
     {
         write(b, 0, b.length);
     }
 }
