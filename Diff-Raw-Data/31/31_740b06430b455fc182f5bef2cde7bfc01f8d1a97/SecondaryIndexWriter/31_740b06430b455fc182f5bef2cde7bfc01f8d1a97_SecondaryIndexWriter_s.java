 package com.xingcloud.xa.secondaryindex;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 /**
  * Created with IntelliJ IDEA.
  * User: wangchangli
  * Date: 5/16/13
  * Time: 9:20 AM
  * To change this template use File | Settings | File Templates.
  */
 public class SecondaryIndexWriter {
   private static final Log LOG = LogFactory.getLog(SecondaryIndexWriter.class);
   
   public static void main(String args[]){
    new Thread(new IndexTailer("/data/log/secondaryindexconfig/")).start();    
   }
 }
