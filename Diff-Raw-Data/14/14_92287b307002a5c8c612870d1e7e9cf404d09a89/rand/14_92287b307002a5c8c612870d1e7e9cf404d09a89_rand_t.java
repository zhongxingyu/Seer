 import java.util.Random;
 public class rand 
 {
    public static void main (String args[])
    {
       Random r = new Random();
       try
       {
         int number = Integer.parseInt(args[0]);
         for(int i=0;i<number;i++)
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
      catch (NumberFormatException ex)
      {
         System.err.println("Error parsing your value.");
         System.err.println(ex.getMessage());
         System.exit(0);
       }      
    }
}
