 package per.andy.test;
 
 /**
    ATest ThreadPool Class
    Test threads will temporary store here.
 
    If end,invoke emptyit(int) method.
 
    @author Andy Cheung
    @since 1
 **/
 
 public class ThreadPool {
         static Thread[] t = new Thread[3];  //Static value of Threads. And threads will be store here.
 	static int count = 0;       //Static value of count. (Counter)
 
         /**
             This method will clean the ThreadPool's temporary store threads.
             Invoke it when test end.
 
             !!!!!! WARNING! !!!!!!
             If invoke it,all the threads will gone & death,and this not going back.
 
             @author Andy Cheung
             @since 2
         **/
         public static boolean testNull(Object o){   //Method to test null.
             try{
               o.toString();
               return false;
             }
             catch(Exception ex){
               return true;
             }
         }
         public static void emptyit(int kind){   //Empty Pool Method
             /*
                Or I can add interrupt statements.
 
                Interrupt statements:
 
                for(int i = 0;i < t.length;i++){  //Repeat statement.
                 if(ThreadPool.testNull(t[i]){ //Test null.
                    t[i].interrupt();  //If not,interrupt it.
                  }
                }
             */
             switch(kind){        //Go to statement.
                case 0:  //Clear pool only
                  for(int i = 0;i < t.length;i++){
                     t[i] = null;  //Set null to Collect.
                  }
                  System.gc();  //Request GC.
                  Runtime.getRuntime().gc();
                  break;
                case 1:  //Clear all
                  for(int = 0;i < t.length;i++;){
                     t[i] = null;  //Set null to collect
                  }
                  System.gc();  //Request GC
                  Runtime.getRuntime().gc();
                  count = 0;  //Zero Count
                  break;
             }
         }
 }
