 package com.xingcloud.xa.secondaryindex;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
import java.util.HashMap;
import java.util.Map;

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
    
//    Index index1 = new Index("sof-dsk","1","grade","1","put");
//    Index index2 = new Index("sof-dsk","1","grade","1","put");
//    Map<Index, Integer> map = new HashMap<Index, Integer>();
//    map.put(index1,1);
//    System.out.println(index1.hashCode());
//    System.out.println(index2.hashCode());
//    System.out.println(map.get(index2));
//    System.out.println(map.containsKey(index2));
    
   }
 }
