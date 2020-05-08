 import java.util.*;
 public class DiGraphTest
 {
  public static void main(String[] args)
    {
 
       boolean repeat = true;
       String input;
       char decision;
       int size, temp, temp2;
       Scanner scan = new Scanner(System.in);
       int[] arr;
       
       System.out.println("Please enter number of verticies");
       do
          if(scan.hasNextInt())
          {
             size=scan.nextInt();
             scan.nextLine();
             break;
          }
          else
          {
             System.out.println("Please enter an int");
             scan.nextLine();
          }
          
       while(true); 
       DiGraphAM q = new DiGraphAM(size);      
       
       System.out.println("Choose one of the following operations:");    
       System.out.print("a(add edge), d(delete),e(edge count), ");
       System.out.print("v (vertex count), p(print), t (topological sort),");
       System.out.println(" or q(quit)");
       
       
       while( repeat )
       {
          
          input = scan.nextLine();
          decision  = input.charAt(0);
          
          
          switch( decision ) 
          {
 
             case 'a':
             case 'A':
                if(scan.hasNextInt())
                {
                   temp = scan.nextInt();
                   if(scan.hasNextInt())
                   { 
                      temp2 = scan.nextInt();
                      scan.nextLine();
                      q.addEdge(temp, temp2);
                      System.out.println(temp + ", " + temp2 + " added");
                   }
                }
                else
                {
                   System.out.println("Please enter a vertex (x  y)");
                   scan.nextLine();
                }
                break;
             case 'v':
             case 'V':
                System.out.println("Vertex count = " + q.vertexCount());
                break;
             case 'd':
             case 'D':
                if(scan.hasNextInt())
                {
                   temp = scan.nextInt();
                   if(scan.hasNextInt())
                   { 
                      temp2 = scan.nextInt();
                      scan.nextLine();
                      q.deleteEdge(temp, temp2);
                      System.out.println(temp + ", " + temp2 + " deleted");
                   }
                }
                else
                {
                   System.out.println("Please enter a vertex (x  y)");
                   scan.nextLine();
                }
                   break;
             case 'e':
             case 'E':
                   System.out.println("Edge count = " +q.edgeCount());
                   break;
             case 'p':
             case 'P':
                   q.print();
                   break;
             case 't':
             case 'T':
                  try
                   {
                     arr=q.topSort();
                     for(int i=0; i<arr.length; i++)
                     {
                        System.out.print(arr[i]+" ");
                     }
                     System.out.println();
                   }
                  catch (LQueue.MyException e)
                  {
                     System.out.println("Error: Cyclic graph cannot sort");
                  }

                   break;
             case 'q':
             case 'Q':
                  repeat = false;
                  System.out.println("Quiting...");
                  break;
             default:
                  System.out.println("Invalid Choice"); 
          }  
       }
       System.out.println();
    }
 }
