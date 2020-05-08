 
 import java.awt.TextArea;
 import java.util.ArrayList;
 import java.util.Scanner;
 
 /* OpSystem Class
  * 
  */
 
 /**
  *
  * @author amabeli
  */
 public class OpSystem {
     
     private TextArea output;
     private int numResourceTypes=0, availableVector=0;
     private ArrayList<Process> processList = new ArrayList();
     private ArrayList<String> processLabels = new   ArrayList();
     
     public OpSystem(TextArea output){
         this.output = output;
     }
     
     public void parseAndStoreData(String inputBlock){
         System.out.println("attempting to parse the input data");
         output.append(inputBlock+"\n\n\n");
         Scanner scanner = new Scanner(inputBlock);
         String tempProcessLabels = scanner.nextLine();
         availableVector = Integer.parseInt(scanner.nextLine());
        output.append("Available in the system:\n");
        output.append(tempProcessLabels+"\n");
        output.append(availableVector+"\n");
        output.append("Processes\n"
                + "        Allocation     Max     Need\n");
         while(scanner.hasNext()){
             String tempLine = scanner.nextLine();
             int tempAllocation = Integer.parseInt(""+tempLine.charAt(0)+tempLine.charAt(1)+tempLine.charAt(2));
             int tempMax = Integer.parseInt(""+tempLine.charAt(3)+tempLine.charAt(4)+tempLine.charAt(5));
 //            System.out.println("allocation-"+tempAllocation+" | max-"+tempMax+" | need-"+(tempMax-tempAllocation));
             processList.add(new Process(tempAllocation,tempMax, tempMax-tempAllocation));
         }
        for(int i=0;i<processList.size();i++){
            output.append("** "+i+":"+processList.get(i).toString());
        }
     }
     
 }
