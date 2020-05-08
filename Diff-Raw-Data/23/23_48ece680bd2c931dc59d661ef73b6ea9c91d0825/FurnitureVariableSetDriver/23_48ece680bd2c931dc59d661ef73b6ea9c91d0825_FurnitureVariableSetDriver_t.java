 
 package interiores.business.models.backtracking;
 
import drivers.AbstractDriver;
 import interiores.core.presentation.terminal.IOStream;
import interiores.shared.backtracking.NoSolutionException;
 
 /**
  *
  * @author nil.mamano
  */
public class FurnitureVariableSetDriver extends AbstractDriver {
 
     private static IOStream iostream = new IOStream(System.in, System.out);
     String NEW_LINE = System.getProperty("line.separator");
     
     FurnitureVariableSet variableSet;
     
     public void main() {  
         introduction();
         boolean exit = false;
         while (!exit) {
             menu();
             int option = iostream.readInt();
             switch (option) {
                case 0:  iostream.println(variableSet.toString());
                          break;
                 case 1:  variableSet = readFurnitureVariableSet();
                          break;
                case 2: 
                    try{
                        solveTester();
                    }
                    catch (NoSolutionException nse) {
                        System.out.println("No solution Found");
                    }
                    break;
                 case 3: exit = true;
                          break;
                 default: System.out.println("Invalid option");
                          break;
             }
         }
     }
 
     
     private void introduction() {
         System.out.println("FurnitureVariableSet Driver" + NEW_LINE + NEW_LINE);
         System.out.println("Define the VariableSet object:" + NEW_LINE);
         variableSet = readFurnitureVariableSet();
     }
     
     private void menu() {
         System.out.println("Options:" + NEW_LINE);
         System.out.println("0) See the VariableSet" + NEW_LINE);
         System.out.println("1) Define a new VariableSet" + NEW_LINE);
         System.out.println("2) solve()" + NEW_LINE);
         System.out.println("3) exit" + NEW_LINE);
         System.out.println(">> ");
     }
     
    private void solveTester() throws NoSolutionException {
         System.out.println("Calling solve() method..." + NEW_LINE);
         variableSet.solve();
         System.out.println("Done. You can check if a solution was found, and ");
         System.out.println("which, with the appropiate menu option (0)" + NEW_LINE);
     }
 
 }
 
 
