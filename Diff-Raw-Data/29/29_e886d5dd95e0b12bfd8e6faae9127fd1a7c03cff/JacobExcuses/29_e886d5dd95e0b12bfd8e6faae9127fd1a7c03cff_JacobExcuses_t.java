 package us.praefectus.scorebored;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 
 /**
  *
  * @author ken
  */
 public class JacobExcuses {
    private static final String textFile = "/JacobExcusesText.txt";
     public File current;
     private ArrayList<String> jacobExcuseList;
     private static final String[] jacobList = new String[] {
         "I am over trained.",
         "Did that nick?",
         "Your shirt is white or orange, cant see the ball when its in front of your shirt.",
         "My back hurts.",
         "I let him win",
         "The lighting was bad",
         "I was practicing my swing and was not playing serious.",
         "It is Tuesday.",
         "I was blowing my thing.",
         "These balls are too big.",
         "I thought he was Jeff.",
         "My wrist hurts.",
         "Was that interference?"
     };
     
     public JacobExcuses() {
        String userHome = System.getProperty("user.home");
        current = new File(userHome + "/.pong-scorebored", textFile);
        try {      
            if(!current.isFile()) {          
                current.getParentFile().mkdirs();
                current.createNewFile();
                PrintWriter pw = new PrintWriter(new FileWriter(current, true));
                for(String s : jacobList) {
                    pw.println(s);
                }
                pw.close();                     
            }
            jacobExcuseList = new ArrayList<String>();

            BufferedReader in = new BufferedReader(new FileReader(current));
             while(in.ready()) {
                 jacobExcuseList.add(in.readLine());
             }
             in.close();
         }
         catch(Exception e) {
            System.err.println("Jacob Excuses Constructor error: " + e.toString());
         }       
     }
     
     public String getJacobExcuse() {
         int randomNumber = (int)(Math.random() * (jacobExcuseList.size()));
         return jacobExcuseList.get(randomNumber);
     }
     
     public ArrayList<String> getJacobExcuseList() {
         return jacobExcuseList;
     }
     
     public void removeExcuse(String removeExcuse, int index) {       
         try {
             File tmp = File.createTempFile("tmp","");            
             BufferedReader in  = new BufferedReader(new FileReader(current));
             PrintWriter outTmp = new PrintWriter(new FileWriter(tmp, true));
 
             while(in.ready()) {
                 String currentExcuse = in.readLine();              
                 if(removeExcuse.equals(currentExcuse)) {
                     continue;
                 }
                 else {
                     outTmp.println(currentExcuse);
                 }
             }           
             in.close();
             outTmp.close();            
             jacobExcuseList.remove(index);                     
             tmp.renameTo(current);       
         }
         catch(Exception e) {
             System.err.println("Jacob Excuses removeExcuse error:" + e.toString());
         } 
     }
     
     public void addExcuse(String newExcuse) {
         try {
             PrintWriter pw = new PrintWriter(new FileWriter(current, true));
             pw.println(newExcuse);
             jacobExcuseList.add(newExcuse);
             pw.close();
         }
         catch(Exception e) {
             System.err.print("Error writing to file: " + e.toString());          
         }
     }
 }
