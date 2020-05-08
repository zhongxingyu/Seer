 package vysichart;
 
 import java.util.Calendar;
 
 /**
  *
  * @author UP612136, UP619902, [paddy's jupiter]
  */
 public class Vysichart {
 
     /**
      * @param args Command-line arguments.
      */
     public static void test1() {
         // debug stuff in here!
         Project myProject = new Project("Toast Maker", "C:/System32/VysiChart");
         Task task1 = new Task();
         Task task2 = new Task();
         Task task3 = new Task();
         Task task4 = new Task();
         Task task5 = new Task();
 
         task1.setTaskName("Make Toast");
         task2.setTaskName("Get Bread");
         task3.setTaskName("Toast Bread");
         task4.setTaskName("Butter Toast");
         task5.setTaskName("Eat The Toast");
 
         task2.setTaskParent(task1);
         task3.setTaskParent(task1);
         task4.setTaskParent(task1);
         task5.setTaskParent(task1);
 
         task3.addDependantNode(task2);
         task4.addDependantNode(task3);
         task5.addDependantNode(task4);
 
         myProject.addTask(task1);
 
         myProject.printOut();
 
 
     }
 
     public static void test2() {
         //make breakfast!
         Project makeBreakfast = new Project("Breakfast Maker", "D:/Documents/MRC/Vysichart/Breakfast");
 
         Task makeBreak = new Task("Make Breakfast");
         Calendar startCal = Calendar.getInstance();
         startCal.set(2011, 5, 12, 5, 25);
         Calendar endCal = Calendar.getInstance();
         endCal.set(2011, 5, 12, 5, 30);
         makeBreak.setStartCalendar(startCal);
         makeBreak.setEndCalendar(endCal);
 
         Task makeCerial = new Task("Make Cerial", makeBreak);
         Task makeJuice = new Task("Make Juice", makeBreak);
         Task eatBreakfast = new Task("Eat The Breakfast", makeBreak);
 
         Task getBowl = new Task("Get Bowl", makeCerial);
         Task pourCerial = new Task("Pour Cerial Into Bowl", makeCerial);
         Task addMilk = new Task("Add Milk To Cerial", makeCerial);
 
         Task getGlass = new Task("Get Glass", makeJuice);
         Task addJuice = new Task("Pour Juice", makeJuice);
 
         eatBreakfast.addDependantNode(makeCerial);
         eatBreakfast.addDependantNode(makeJuice);
 
         pourCerial.addDependantNode(getBowl);
         addMilk.addDependantNode(pourCerial);
 
         addJuice.addDependantNode(getGlass);
 
 
         makeBreakfast.addTask(makeBreak);
 
         //makeBreakfast.printOut();
 
         //System.out.println(makeBreakfast.getString());
 
         GraphicalUserInterface gui = new GraphicalUserInterface(makeBreakfast);
         
         
         // no default contructor
         gui.main(null);
 
     }
 
     public static void main(String[] args) {
 
         long startTime = System.currentTimeMillis();
 
         //test1();
         test2();
 
         // node, testN classes are debugging classes, they have no real implication
         
         //Gantt g = new Gantt();
         //GanttRender gr = new GanttRender(g);
         //gr.run(); //debug
 
 
 
         //GraphicalUserInterface.main(null); // run GUI
 
         long endTime = System.currentTimeMillis();
         System.out.println("Computation Time: " // check how fast it is
                 + (endTime - startTime) + " ms");
     }
 }
