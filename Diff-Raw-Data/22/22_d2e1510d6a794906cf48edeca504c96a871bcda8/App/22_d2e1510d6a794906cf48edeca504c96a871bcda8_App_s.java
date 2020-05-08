 package im.yuri.jcom;
 
 /**
  * Hello world!
  *
  */
 public class App
 {
     public static Channel[][] channels;
     public static Process[] processes;
     public static void main( String[] args )
     {
         channels = new Channel[5][5];
         processes = new Process[5];
         for (int i = 0; i < 5; i++) {
             for (int j = 0; j < 5; j++) {
                 channels[i][j] = new Channel();
             }
         }
         for (int i = 0; i<2; i++) {
             processes[i] = new Process(i, channels);
             (new Thread(processes[i])).start();
         }
     }
 }
