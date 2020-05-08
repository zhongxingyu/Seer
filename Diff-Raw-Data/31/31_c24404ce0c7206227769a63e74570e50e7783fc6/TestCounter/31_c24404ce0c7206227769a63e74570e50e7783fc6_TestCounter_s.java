 package org.roger.study.ExClient.controller;
 
 import org.jboss.netty.util.internal.ConcurrentHashMap;
 import org.roger.study.ExClient.configuration.Configs;
 
 import static org.roger.study.ExClient.test.Report.report;
 
 /**
  * Created with IntelliJ IDEA.
  * User: next
  * Date: 13-8-22
  * Time: 下午5:34
  * To change this template use File | Settings | File Templates.
  */
 public class TestCounter {
     private static ConcurrentHashMap<Integer, Integer> counter = new ConcurrentHashMap<Integer, Integer>();  //Channel Id, test times
     private static Integer sent = 0x0, sentOk = 0x0, res=0x0;
 
     public static synchronized boolean  testOne(Integer key)  {
         if (!counter.containsKey(key))  {
             counter.put(key, 1);
             sent ++;
             return true;
         }
 
         Integer value = counter.get(key);
         if (value < Configs.getRunTimes())  {
             counter.replace(key, value+1);
             sent ++;
             return true;
         }
 
         return false;
     }
 
     public static boolean TestOver()  {
        Integer b = Configs.getRunTimes() * counter.size();
         return res == (Configs.getRunTimes() * counter.size());
     }
 
     public static void clearTestCounter()  {
         counter.clear();
        setSent(0);
        setSentOk(0);
     }
 
 
     public static Integer getSent() {
         return sent;
     }
 
    private static void setSent(Integer sent) {
        TestCounter.sent = sent;
    }

     public static Integer getSentOk() {
         return sentOk;
     }
 
     public static void setSentOk(Integer channelId) {
         synchronized(sentOk)  {
             if (counter.containsKey(channelId) && counter.get(channelId) > 0)
                 sentOk++;
         }
     }
 
     public static synchronized void setRes(Integer channelId) {
         if (counter.containsKey(channelId) && counter.get(channelId) > 0)
             res++;
 
         if (TestOver())
             report();
     }
 }
