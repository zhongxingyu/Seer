package omella;
 
 import java.util.ArrayList;
 
 /**
  *
  * @author Rafael - aka Juvigar
  */
 public class EnemyCreation 
 {
     
     public static String setName()
     {
         String name;
         
         ArrayList nameList = new ArrayList ();
         nameList.add("Pedrão");
         nameList.add("XxHansxX");
         nameList.add("Fuentes");
         nameList.add("Runo");
         nameList.add("John");
         nameList.add("Carlos");
         nameList.add("Marcos"); 
         nameList.add("Ribeiro");
         
         int i = (int)(Math.random()*nameList.size());
         name = nameList.get(i).toString();       
         return name;
     }
    
     
     public static String setGender()
     {
         int i = (int)(Math.random()*2);
         
         if (i == 1)       
             return "male";
         else
             return "female";        
     }
     
     
     public static String setClass() // Warrior, Ranger, Mage, Thief
     {
         int i = (int)(Math.random()*4);
         
         switch (i)
         {
             case 0:
                 return "Warrior";
             case 1:
                 return "Ranger";
             case 2:
                 return "Mage";
             case 3:
                 return "Thief";
             default:
                 return "PROBLEM!!";               
         }
     }
     
     
     public static String setRace() //Human, Orc, Undead, Goblin
     {
         int i = (int)(Math.random()*4);
         
         switch (i)
         {
             case 0:
                 return "Human";
             case 1:
                 return "Orc";
             case 2:
                 return "Undead";
             case 3:
                 return "Goblin";
             default:
                 return "PROBLEM!!";
         }        
     }
     
     
     public static int setLvl() // Default
     {
         return 1;
     }
     
     public static int[] setAtt(String enemyClass)
     {
          int[] att = new int[5];
          
         for(int i=0; i<att.length; i++)
         {
             do
             {
             att[i] = (int)(Math.random()*20);
             }while(att[i]<6);
         }
         switch(enemyClass)
         {
             case "Warrior":
                 att[0] += 4;
                 att[1] += 0;
                 att[2] += 2;
                 att[3] -= 2;
                 att[4] -= 4; 
                 break;
             case "Ranger":
                 att[0] -= 4;
                 att[1] += 4;
                 att[2] -= 2;
                 att[3] += 2;
                 att[4] += 0;
                 break;
             case "Mage":
                 att[0] -= 4;
                 att[1] += 1;
                 att[2] -= 2;
                 att[3] += 1;
                 att[4] += 4;
                 break;
             case "Thief":
                 att[0] -= 4;
                 att[1] += 3;
                 att[2] -= 2;
                 att[3] += 3;
                 att[4] += 0;
                 break;
         }
         for(int i=0; i<5; i++){
             if(att[i]>20)
                 att[i]=20;
             if(att[i]<1)
                 att[i]=1;
         }
         return att;
     }
     
     
 }
