 
 import java.io.*;
 
 public class ChromiumPostProcessor {
     
     //These variables are user-editable
     static File chromium_log=new File("/home/devasia/Desktop/chromium.txt");
     static File output_file=new File("/home/devasia/Desktop/chromium_report.txt");
     static int stallThreshold=60; // in milliseconds
     static int linkSpeed=10000; //in kbps
     
     static BufferedWriter wt;
     
 
     public static void main(String args[]) {
         try{
         
         BufferedReader rd =new BufferedReader(new FileReader(chromium_log));
         wt=new BufferedWriter(new FileWriter(output_file));
         
         String line=null, log="";
         while((line=rd.readLine())!=null){
             if(line.startsWith("#")){
                 log=log+line+"\n";
             }
         }
         
         String arr[]=log.split("\n");
        for(int i=0;i<arr.length-2;i++){
             if(arr[i].contains("Loading")){
                 double time=processLine(arr[i], arr[i+1]);
                 log("Load Time: "+time+"ms");
             }
             
             else{
                 double time=processLine(arr[i], arr[i+1]);
                 if(time>stallThreshold){
                     log("Detected Stall of "+time+"ms between Frame "+i+" and Frame "+(i+1));
                 }
                 else if(time<20){
                     log("Error: Detect quicken of "+time+" between Frame "+i+" and Frame "+(i+1));
                 }
             }
         }
         
         wt.close();
         }
         catch (Exception e){
             e.printStackTrace();
         }
     }
     
     public static void log(String message) throws Exception{
         System.out.println(message);
         wt.write(message+"\n");
         wt.flush();
     }
     
     //Wondershaper doesn't work! We need to use something else to throttle bandwidth
     public static void setLinkSpeed(int speed) throws Exception{
         Runtime.getRuntime().exec("sudo wondershaper eth0 "+speed+" 10000");
     }
     
     public static int processLine(String line1, String line2){
         line1=line1.replaceAll("#FrameReady at ", "");
         line1=line1.replace("#Loading at ", "");
         line2=line2.replaceAll("#FrameReady at ", "");
         line2=line2.replace("#Loading at ", "");
         double d1=Double.parseDouble(line1);
         double d2=Double.parseDouble(line2);
         double time=d2-d1;
         int ret=(int) time;
         return ret;
     }
 }
