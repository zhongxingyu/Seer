 import inc.*;
 import java.io.*;
 
 public class Inc
 {
 	public static String readLine()
 	{
 		String s = "";
 		try {
 			InputStreamReader converter = new InputStreamReader(System.in);
 			BufferedReader in = new BufferedReader(converter);
 			s = in.readLine();
 		} catch (Exception e) {
 			System.out.println("Error! Exception: "+e); 
 		}
 		return s;
 	}
         public static void main(String[] argv)
         {
                 if(argv.length < 3)
                 {
                         System.err.println("<inc> num_threads num_iter worker_kind(0,1,2)");
                         return;
                 }
                 int num_threads = Integer.parseInt(argv[0]);
                 int num_iter = Integer.parseInt(argv[1]);
                 int worker_kind = Integer.parseInt(argv[2]);
                 System.out.println("Hey there me monkey robot!");
 
                 //readLine();
 
                num_iter = 10 ;
 
                 Cell cell = new Cell();
             //  Cell cell2 = new Cell();
 
            //   if( worker_kind == 0 )
            //           for (int i = 0; i < num_threads; i++)
            //                   new Worker(i,cell, num_iter).start();
            //   else if( worker_kind == 1 )
            //           for (int i = 0; i < num_threads; i++)
            //                   new SynchWorker(i,cell, num_iter).start();
            //   else
            //           for (int i = 0; i < num_threads; i++)
            //                   new MethodSynchWorker(i,cell, num_iter).start();
                 
                Worker w1 = new Worker( 0, cell, num_iter ); 
                Worker w2 = new Worker( 1, cell, num_iter ); 
                Worker w3 = new SynchWorker( 2, cell, num_iter ); 
                Worker w4 = new SynchWorker( 3, cell, num_iter ); 
             // w1.start();
             // w2.start();
                w3.start();
                w4.start();
 
                 //System.out.println("Result: " + cell.data + " " + cell.id);
                //System.out.println("Result: " + cell.data );
                 
               try{ Thread.sleep(10000); }catch(java.lang.InterruptedException e) { System.out.println("INterrupted!!"); }
         }
 } 
