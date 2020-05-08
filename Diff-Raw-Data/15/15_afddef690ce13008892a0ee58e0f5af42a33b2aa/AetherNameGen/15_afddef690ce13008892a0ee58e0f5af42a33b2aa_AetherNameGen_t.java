 package net.aetherteam.aether;
 
 import java.io.*;
 import java.util.ArrayList;
import java.util.List;
 import java.util.Random;
 
 public class AetherNameGen
 {
     public static String[] valkNamePrefix = {"Har", "Her", "Gon", "Sko", "Hil"};
     public static String[] valkNameMiddix = {""};
     public static String[] valkNameSuffix = {"tur", "pul", "dul", "gul", "or"};
 
     public static String[] name1 = {"毁灭的","狂暴的","毒辣的","邪恶的","诡异的","超能的","狂拽的","浪气的","黑暗的","暗黑的","狂怒的","贪婪的","傲慢的","虚荣的","饕餮的","嫉妒的","色欲的","暴怒的","懒惰的","黑化的","无头骑士的"};
 
     public static String[] name2 = {"CaptainSparklez","zero","ztcjohn","某猫","蓝色铁卷门","大作之月","disco","黑桐谷歌","敖厂长","CovertJaguar","SirSengir","eloraam"};
 
     static Random rand = new Random();
 
     static {
         String[] temp;
         temp = loadNames("/names/perfix.txt");
         if (temp != null) name1 = temp;
         temp = loadNames("/names/suffix.txt");
         if (temp != null) name2 = temp;
         temp = loadNames("/names/valkperfix.txt");
         if (temp != null) valkNamePrefix = temp;
         temp = loadNames("/names/valkmiddix.txt");
         if (temp != null) valkNameMiddix = temp;
         temp = loadNames("/names/valksuffix.txt");
         if (temp != null) valkNameSuffix = temp;
     }
     private static String[] loadNames(String resname){
        List<String> result = new ArrayList();
         InputStreamReader reader;
         try
         {
             reader = new InputStreamReader(AetherNameGen.class.getResourceAsStream(resname));
             BufferedReader buf = new BufferedReader(reader);
             String line;
             while((line = buf.readLine()) != null) {
                 result.add(line);
             }
             buf.close();
             reader.close();
         } catch (IOException e)
         {
             e.printStackTrace();
            return null;
         }
        return result.toArray(new String[0]);
     }
     public static String gen()
     {
         String name = name1[rand.nextInt(name1.length)];
         return name + name2[rand.nextInt(name2.length)];
     }
 
     public static String valkGen()
     {
         String result;
         result = valkNamePrefix[rand.nextInt(valkNamePrefix.length)];
         result += valkNameSuffix[rand.nextInt(valkNameMiddix.length)];
         result += valkNameSuffix[rand.nextInt(valkNameSuffix.length)];
         return result;
     }
 }
