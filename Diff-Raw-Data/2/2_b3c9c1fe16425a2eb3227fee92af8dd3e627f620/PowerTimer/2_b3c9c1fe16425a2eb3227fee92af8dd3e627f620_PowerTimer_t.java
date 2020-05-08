 package com.github.inside;
 
 import java.util.Map;
 import java.util.Iterator;
 import java.util.concurrent.ConcurrentHashMap;
 import com.github.inside.PaddlePower;
 
 public class PowerTimer
 {
     // The parameters were set on the advice of:
     // http://ria101.wordpress.com/2011/12/12/concurrenthashmap-avoid-a-common-misuse/
     public static ConcurrentHashMap<String, PaddlePower> leftPaddlePowers = new ConcurrentHashMap<String, PaddlePower>(8, 0.9f, 1);
     public static ConcurrentHashMap<String, PaddlePower> rightPaddlePowers = new ConcurrentHashMap<String, PaddlePower>(8, 0.9f, 1);
 
     public static void handlePowerTimer()
     {
         if (PowerTimer.leftPaddlePowers.size() > 0)
         {
             PowerTimer.iterateOverPowers(leftPaddlePowers);
         }
         if (PowerTimer.rightPaddlePowers.size() > 0)
         {
             PowerTimer.iterateOverPowers(rightPaddlePowers);
         }
     }
 
    public static void iterateOverPowers(ConcurrentHashMap<String, PaddlePower> map)
     {
         Iterator<Map.Entry<String, PaddlePower>> entries = map.entrySet().iterator();
 
         while (entries.hasNext())
         {
             Map.Entry<String, PaddlePower> entry = entries.next();
             PaddlePower power = entry.getValue();
 
             if (power.getInitTime() + power.getLifeTime() <= Board.currentTime)
             {
                 power.action();
                 map.remove(entry.getKey());
             }
         }
     }
 }
