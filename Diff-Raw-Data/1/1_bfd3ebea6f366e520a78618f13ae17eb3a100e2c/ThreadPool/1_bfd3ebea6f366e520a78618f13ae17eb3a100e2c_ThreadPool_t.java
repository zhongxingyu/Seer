 package per.andy.test;
 
 public class ThreadPool {
         static Thread[] t = new Thread[3];
 	static int count = 0;
         public void emptyit(int kind){
             switch(kind){
                case 0:
                  for(int = 0;i < t.length;i++;){
                     t[i] = null;
                  }
                  System.gc();
                  Runtime.getRuntime().gc();
                  break;
                case 1:
                  for(int = 0;i < t.length;i++;){
                     t[i] = null;
                  }
                  System.gc();
                  Runtime.getRuntime().gc();
                  count = 0;
                  break;
            }
         }
 }
