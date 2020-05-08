 package Helpers;
 
 import java.util.regex.Pattern;
 
 public class Vector3f {
     public Vector3f()
     {
         this.x = 0;
         this.y = 0;
         this.z = 0;
     }
     public Vector3f(float x, float y, float z)
     {
         this.x = x;
         this.y = y;
         this.z = z;
     }
     public Vector3f(Vector3f copy){
         this.x = copy.x;
         this.y = copy.y;
         this.z = copy.z;
     }
     
     public Vector3f(String input) throws Exception{
        if(input == null){throw new Exception("Error:\nInput can't be null");}
        if(input.length() % 3 != 0){throw new Exception("Error:\nInput has to be an x times 3 value ");}
        if(Pattern.matches("[0-9]+", input) == false){throw new Exception("Error:\nInput may only container numbers");}
        
        char[] inputC = input.toCharArray();
        int counter = 0;
        String x = "0";
        String y = "0";
        String z = "0";
        for (int i = 0; i < 3; i++) {
            switch(i){
                    case 0:
                    for (int j = counter; j < counter+input.length()/3; j++)
                        x += Character.toString(inputC[j]);
                    break;
                    case 1:
                    for (int j = counter; j < counter+input.length()/3; j++)
                        y += Character.toString(inputC[j]);
                    break;
                    case 2:
                    for (int j = counter; j < counter+input.length()/3; j++)
                        z += Character.toString(inputC[j]);
                    break;
            }
            counter += (input.length() / 3);
        }
        this.x = Integer.parseInt(x);
        this.y = Integer.parseInt(y);
        this.z = Integer.parseInt(z);
     }
     
     public Vector3f(String input, String format) throws Exception{
         if(input == null){throw new Exception("Error:\nInput can't be null");}
         if(input.length() % 3 != 0){throw new Exception("Error:\nInput has to be an x times 3 value ");}
         if(Pattern.matches("[0-9]+", input) == false){throw new Exception("Error:\nInput may only container numbers");}
         if(format == null){throw new Exception("Error:\\nFormat can't be null");}
         if(format.length() != 3){throw new Exception("Error:\nFormat has to be 3 chars");}
         if(Pattern.matches("[XYZ]+", format.toUpperCase()) == false){throw new Exception("Error:\nFormat may only container X,Y or Z");}
         
         char[] formatC = format.toLowerCase().toCharArray();
         char[] inputC = input.toCharArray();
         int counter = 0;
         String x = "0";
         String y = "0";
         String z = "0";
         for (int i = 0; i < 3; i++) {
             switch(formatC[i]){
                     case 'x':
                     for (int j = counter; j < counter+input.length()/3; j++)
                         x += Character.toString(inputC[j]);
                     break;
                     case 'y':
                     for (int j = counter; j < counter+input.length()/3; j++)
                         y += Character.toString(inputC[j]);
                     break;
                     case 'z':
                     for (int j = counter; j < counter+input.length()/3; j++)
                         z += Character.toString(inputC[j]);
                     break;
             }
             counter += (input.length() / 3);
         }
         this.x = Integer.parseInt(x);
         this.y = Integer.parseInt(y);
         this.z = Integer.parseInt(z);
     }
     
     public float x;
     public float y;
     public float z;
     
     @Override
     public String toString()
     {
         return "[X:" + (int)x + ",Y:" + (int)y + ",Z:" + (int)z + "]";
     }
     
     public String toString(String format) throws Exception{
         if(format == null){throw new Exception("Error:\\nFormat can't be null");}
         if(format.length() != 3){throw new Exception("Error:\nFormat has to be 3 chars");}
         if(Pattern.matches("[XYZ]+", format.toUpperCase()) == false){throw new Exception("Error:\nFormat may only container X,Y or Z");}
         String returnString = "";
         
         char[] formatC = format.toLowerCase().toCharArray();
         for (int i = 0; i < 3; i++) {
             switch(formatC[i]){
                     case 'x':
                     returnString += (int)this.x;
                     break;
                     case 'y':
                     returnString += (int)this.y;
                     break;
                     case 'z':
                     returnString += (int)this.z;
                     break;
             }
         }
         return returnString;
     }
     public void normalize(){
         float length = length();
         x/=length;
         y/=length;
         z/=length;
     }
     
     public float length(){
         float tx = x;
         float ty = y;
         float tz = z;
         if(tx<0)tx=-tx;
         if(ty<0)ty=-ty;
         if(tz<0)tz=-tz;
         
         return (float)Math.sqrt( Math.pow(tx, 2) + Math.pow(ty, 2) + Math.pow(tz, 2) );
     }
     
     public static float distance(Vector3f v1, Vector3f v2){
         float distX = v1.x - v2.x;
         float distY = v1.y - v2.y;
         float distZ = v1.z - v2.z;
         if(distX<0)distX*=-1;
         if(distY<0)distY*=-1;
         if(distZ<0)distZ*=-1;
         
         return (float)Math.sqrt( Math.pow(distX, 2) + Math.pow(distY, 2) + Math.pow(distZ, 2)  );  
     }
     
     public void CopyVector3f(Vector3f copy){
         this.x = copy.x;
         this.y = copy.y;
         this.z = copy.z;
     }
     
     public Vector3f AddVector3f(Vector3f add){
         this.x += add.x;
         this.y += add.y;
         this.z += add.z;
         return new Vector3f(x, y, z);
     }
     
     public static Vector3f GetCenter(Vector3f pointA, Vector3f pointB){
         float x = (pointA.x - pointB.x) / 2;
         float y = (pointA.y - pointB.y) / 2;
         float z = (pointA.z - pointB.z) / 2;
         
         return new Vector3f(pointA.x-x, pointA.y-y, pointA.z-z);
     }
 }
