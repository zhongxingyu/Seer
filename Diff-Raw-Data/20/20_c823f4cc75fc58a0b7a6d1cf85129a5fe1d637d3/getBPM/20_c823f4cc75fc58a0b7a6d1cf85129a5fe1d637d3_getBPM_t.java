 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package ecgjava2;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 /**
  *
  * @author francispapineau
  */
 public class getBPM{
 
     static double [][] array;
     static double [] datacopy;
    static boolean guardlow = false, guardhigh = false, GH1 = false, GH2 = false, GL1 = false, GL2 = false, BPMguard = false;
    static int count = 0, k = 0, totalcount = 0, averageBPM = 0, countGetBPM = 0;
     static getBPM BPM;
     static double previoustime = 0.00;
 
     static void createNewArrays(){
         //Create and print a 2D double array
         array = new double[40][2];
         datacopy = new double[5];
     }
     public static void getPeaks() throws InterruptedException{
         for (int i = 0; i < 40; i++){
             if (i < 35){
                 System.out.println(i);
                 k = 0;
                 for(int j = i; j < i + 5; j++){
                     datacopy[k] = array[j][1];
                     System.out.println(datacopy[k]);
                     k ++;
                 }
                 System.out.println();
                 k = 0;
                     if(datacopy[0] < datacopy[2]) GL1 = true;
                     else GL1 = false;
                     if(datacopy[1] < datacopy[2]) GL2 = true;
                     else GL2 = false;
                     if (GL2 && GL1) guardlow = true;
                     else guardlow = false;
                     System.out.println(datacopy[2] + "     " + datacopy[0] + "    " + GL1 + "    " + GL2 + "    " + guardlow);
                     System.out.println(datacopy[2] + "     " + datacopy[1] + "    " + GL1 + "    " + GL2 + "    " + guardlow);
 
 
                     if(datacopy[3] < datacopy[2]) GH1 = true;
                     else GH1 = false;
                     if(datacopy[4] < datacopy[2]) GH2 = true;
                     else GH2 = false;
                     if (GH2 && GH1) guardhigh = true;
                     else guardhigh = false;
                     System.out.println(datacopy[2] + "     " + datacopy[3] + "    " + GH1 + "    " + GH2 + "    " + guardhigh);
                     System.out.println(datacopy[2] + "     " + datacopy[4] + "    " + GL1 + "    " + GL2 + "    " + guardlow);
 
 
                 if (guardlow && guardhigh && GL2 && GL1 && GH1 && GH2){
                     count++;
                     totalcount += count;
                 }
                 guardhigh = false;
                 guardlow = false;
             }
         }
         System.out.println(count);
         previoustime = array[39][0];
        if (BPMguard){
            getBPM();
        }
        count = 0;
     }
     public static void reset(){
         array = null;
         datacopy = null;
         k = 0;
         count = 0;
         guardlow = guardhigh = false;
     }
     public static void buildArray() throws InterruptedException{
         array = SplitParse.getArray();
         for(int i = 0; i < 40; i++){
             System.out.println(array[i][0] + "   " + array[i][1]);
         }
     }
     public static void getBPM(){
         double BPM = 0.00;
         double time = (System.currentTimeMillis() - SplitParse.InitialTime);
        BPM = ((double) count*1.0 / (time / (1000*60))*1.0);
        averageBPM += BPM;
        averageBPM = (int) (averageBPM / (2.0));
         System.out.println(Integer.toString((int)BPM));
        ECGJAVa2View.PulseValue.setText(Double.toString(averageBPM));
     }
     public void run() {
         createNewArrays();
         try {
             buildArray();
         } catch (InterruptedException ex) {
             Logger.getLogger(getBPM.class.getName()).log(Level.SEVERE, null, ex);
         }
         if (SplitParse.full){
             try {
                 getPeaks();
             } catch (InterruptedException ex) {
                 Logger.getLogger(getBPM.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
     }
 }
