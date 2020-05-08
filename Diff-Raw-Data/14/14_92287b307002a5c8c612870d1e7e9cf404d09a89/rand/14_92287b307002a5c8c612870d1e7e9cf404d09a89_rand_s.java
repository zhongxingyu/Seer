 import java.util.Random;
 public class rand 
 {
    public static void main (String args[])
    {
       Random r = new Random();
       try
       {
         for i=0;i<args[0];x++)
          {
             System.out.print("Your random number is:");
             System.out.println(r.nextInt(10));
          }
       }
       catch (ArrayIndexOutOfBoundsException ex)
       {
          for(int i=0;i<r.nextInt(10);i++)
          {
             System.out.print("Your random number is:");
             System.out.println(r.nextInt(10));
             
          }
       }      
    }
}
