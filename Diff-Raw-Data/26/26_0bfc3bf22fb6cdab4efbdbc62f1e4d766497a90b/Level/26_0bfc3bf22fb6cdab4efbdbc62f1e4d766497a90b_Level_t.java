 
 package com.ninjacannon.quantum.level;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Random;
 
 /**
  * @author Dan Cannon
  */
 public class Level 
 {   
    private static String string;
     private static ArrayList<String> easy;
     private static ArrayList<String> med;
     private static ArrayList<String> hard;
     
     private ArrayList<String> level;
     private int nextWave;
     
     public Level(int difficulty, int length)
     {
         if(easy == null){
             init();
         }
         nextWave = 0;
         level = new ArrayList<String>();
         switch(difficulty){
             case 1: GenerateEasyLevel(difficulty * length * 25);
                 break;
             case 2: GenerateMedLevel(difficulty * length * 25);
                 break;
             case 3: GenerateHardLevel(difficulty * length * 25);
                 break;
         }
     }
     
     public String nextWave(){
         String wave;
         if(nextWave < level.size()){
             wave = level.get(nextWave++);
         } else {
             wave = null;
         }
         return wave;
     }
     
     private void GenerateEasyLevel(int rating)
     {
         Random random = new Random();
         while(rating > 0){
             if(Math.random() > .50){
                level.add(string + "/med/"+ med.get(random.nextInt(med.size())));
                 rating -= 2;
             } else {
                level.add(string + "/easy/"+ easy.get(random.nextInt(easy.size())));
                 rating -= 1;
             }
         }
     }
     
     private void GenerateMedLevel(int rating)
     {
         Random random = new Random();
         while(rating > 0){
             double num = Math.random();
             if(num > .85){
                level.add(string + "/hard/"+ hard.get(random.nextInt(hard.size()-1)));
                 rating -= 3;
             } else if (num < .15){
                level.add(string + "/easy/"+ easy.get(random.nextInt(easy.size()-1)));
                 rating -= 1;
             } else {
                level.add(string + "/med/"+ med.get(random.nextInt(med.size()-1)));
                 rating -=2;
             }
         }
     }
     
     private void GenerateHardLevel(int rating)
     {
         Random random = new Random();
         while(rating > 0){
             if(Math.random() > .75){
                level.add(string + "/med/" + med.get(random.nextInt(med.size()-1)));
                 rating -= 2;
             } else {
                level.add(string + "/hard/"+hard.get(random.nextInt(hard.size()-1)));
                 rating -= 3;
             }
         }
     }
     
     public final void init()
     {
         
         string = new File("res/waves").getAbsolutePath();
         System.out.println(string);
         easy = new ArrayList<String>(Arrays.asList(new File(string + "/easy").list()));
         med = new ArrayList<String>(Arrays.asList(new File(string + "/med").list()));
         hard = new ArrayList<String>(Arrays.asList(new File(string + "/hard").list()));
     }
 }
