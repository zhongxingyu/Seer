 /*
  * Problemin çözüleceği sınıf burası. CSP algoritmaları burada implemente edilecek
  */
 package mapcofx2;
 
 import java.util.*;
 import javafx.scene.paint.Color;
 import javafx.scene.paint.Paint;
 import mapcofx2.Graph.Vertex;
 
 /**
  *
  * @author Bahadir
  */
 public class CSP {
 
     // Enums
     public enum SUVType {
 
         SIMPLE, MRV, DEGREE
     }
 
     public enum ODVType {
 
         SIMPLE, LCV
     }
     private List<Graph.Vertex> unassignedVariables;
     private Graph graph;
     private int vertexCount;
     private int colorCount;
     private List<Paint> standartDomain;
     private SUVType suvType;
     private ODVType odvType;
     private boolean forwardChecking;
     final public Stats stats;
     private static int checkIsConsistentCount = 0;
     private static int selectUnassigneCallCount = 0;
     private Map<Vertex, List<Paint>> domains;
 
     public class Stats {
 
         public int checkIsCompleteCount = 0;
         public int successfulAssignmentCount = 0;
         public int failedAssignmentCount = 0;
         public int orderDomainCallCount = 0;
         public int revertCount = 0;
         public int forwardCheckFailure = 0;
 
         public Stats() {
             checkIsConsistentCount = 0;
             selectUnassigneCallCount = 0;
         }
 
         @Override
         public String toString() {
 
             return "STATS:\r\n"
                     + "Is Soluble:" + (unassignedVariables.isEmpty()) + "\r\n"
                     + "VARIABLE SELECT METHOD:" + suvType.name() + "\r\n"
                     + "VALUE ORDER METHOD:" + odvType.name() + "\r\n"
                     + "FORWARD CHECKING:" + forwardChecking + "\r\n"
                     + "Forward Check Failure Count:" + forwardCheckFailure + "\r\n"
                     + "Check is complete count:" + checkIsCompleteCount + "\r\n"
                     + "Successfull assignment count:" + successfulAssignmentCount + "\r\n"
                     + "Failed Assignment Count:" + failedAssignmentCount + "\r\n"
                     + "Consistency check count:" + checkIsConsistentCount + "\r\n"
                     + "Order-Domain Call Count:" + orderDomainCallCount + "\r\n"
                     + "Select-Unassigned Call Count:" + selectUnassigneCallCount + "\r\n"
                     + "Backtrack count:" + revertCount;
         }
     }
 
     public class Assignment {
 
         final public Graph.Vertex variable;
         final public Paint color;
 
         public Assignment(Vertex variable, Paint color) {
             this.variable = variable;
             this.color = color;
         }
     }
 
     public class AssignmentsState {
 
         private List<Assignment> assignments;
         private List<Assignment> snapShot;
 
         public AssignmentsState() {
             this.assignments = new LinkedList<>();
         }
 
         public AssignmentsState(AssignmentsState cloneFrom) {
             this.assignments = new LinkedList<>(cloneFrom.getAssignments());
         }
 
         public void takeSnapshot() {
             this.snapShot = new LinkedList<>(this.assignments);
 
         }
 
         public void revert() {
             stats.revertCount++;
             this.assignments = new LinkedList<>(this.snapShot);
 
 
 
         }
 
         public List<Assignment> getAssignments() {
             return assignments;
         }
 
         public boolean addAssignment(Assignment assignment) {
 
             if (this.assignments.contains(assignment)) {
                 stats.failedAssignmentCount++;
                 return false;
             }
 
             if (CSP.isConsistent(assignment, assignments)) {
                 this.assignments.add(assignment);
                 stats.successfulAssignmentCount++;
                 return true;
             } else {
                 stats.failedAssignmentCount++;
                 return false;
             }
 
         }
 
         public boolean removeAssignment(Assignment assignment) {
             if (assignments.contains(assignment)) {
                 assignments.remove(assignment);
                 return true;
             }
             return false;
         }
 
         @Override
         public String toString() {
 
             String outStr = "";
 
             int i = 0;
             for (Assignment asg : this.assignments) {
                 i++;
                 Colorizer.Node element = (Colorizer.Node) asg.variable.getElement();
                 outStr += (i + ". " + element.toString() + " " + asg.color + "\r\n");
             }
 
             return outStr;
         }
     }
 
     public CSP(Graph graph, int colorCount, SUVType suvType, ODVType odvType, boolean forwardChecking) {
         this.suvType = suvType;
         this.odvType = odvType;
         this.stats = new Stats();
         this.forwardChecking = forwardChecking;
 
         Queue<Color> colors = new LinkedList<>();
         colors.add(Color.RED);
         colors.add(Color.BLUE);
         colors.add(Color.YELLOW);
         colors.add(Color.FUCHSIA);
         colors.add(Color.CYAN);
         /*
          * *
          * Random rndm = new Random(); Queue<Color> colors = new LinkedList<>();
          * for (int i=0; i<colorCount;i++) { Color clr = new
          * Color(rndm.nextDouble(),rndm.nextDouble(),rndm.nextDouble(),1.0);
          * //red,green,blue,opacity while
          * (clr.getRed()+clr.getGreen()+clr.getBlue()<1.2) clr = clr.brighter();
          * clr = clr.saturate(); while (colors.contains(clr)) clr = new
          * Color(rndm.nextDouble(),rndm.nextDouble(),rndm.nextDouble(),1.0); for
          * (Color c : colors) { if
          * (((c.getRed()+c.getGreen()+c.getBlue())-(clr.getRed()+clr.getGreen()+clr.getBlue()))<0.3)
          * clr = new
          * Color(rndm.nextDouble(),rndm.nextDouble(),rndm.nextDouble(),1.0); }
          * colors.add(clr); }
          */
         this.standartDomain = new LinkedList<>();
 
         for (int i = 0; i < colorCount; i++) {
             standartDomain.add(colors.poll());
         }
 
         this.graph = graph;
         this.vertexCount = graph.getVertices().size();
         this.colorCount = colorCount;
 
         this.unassignedVariables = new LinkedList<>();
         unassignedVariables.addAll(graph.getVertices());
 
         domains = new HashMap<>();
         for (Vertex v : unassignedVariables) {
             domains.put(v, new LinkedList<>(standartDomain));
         }
 
     }
 
     public static boolean isConsistent(Assignment assignment, List<Assignment> assignments) {
         checkIsConsistentCount++;
         List<Vertex> neighbours = assignment.variable.getNeighbours();
         for (Assignment asg : assignments) {
             // Yapılmış olan bütün atamaları gez
             for (Graph.Vertex neighbour : neighbours) {
                 // Verilen atamada kullanılan köşenin tüm komşularını gez
                 // Komşuya yapılmış bir atama var ve bu atama verilen atamadaki vertex ile aynı ise constraint sağlanmaz.
                 if (asg.variable.equals(neighbour) && asg.color.equals(assignment.color)) {
                     return false;
                 }
             }
         }
         return true;
     }
 
     public boolean checkComplete(List<Assignment> assignments) {
         stats.checkIsCompleteCount++;
         // Bütün atamalar yapılmış mı?
         if (unassignedVariables.size() > 0) {

             return false;
         }
 
         // Consistency check:
         for (Assignment asg : assignments) {
             // Bütün atamaların komşuları
             List<Vertex> neighbours = asg.variable.getNeighbours();
             for (Assignment asg2 : assignments) {
                 // Bütün atamalarda
                 for (Vertex neighbour : neighbours) {
                     // Komşular
                     if (asg2.variable.equals(neighbour) && asg2.color.equals(asg.color)) {
                         return false;
                     }
                 }
             }
         }
 
         return true;
     }
 
     public Vertex selectUnassigned() {
         selectUnassigneCallCount++;
         switch (suvType) {
             case SIMPLE:
                 return unassignedVariables.size() > 0
                         ? unassignedVariables.remove(0)
                         : null;
         }
        return null;
     }
 
     public List<Paint> orderDomain(Graph.Vertex variable) {
         stats.orderDomainCallCount++;
         switch (odvType) {
             case SIMPLE:
                 return domains.get(variable);
         }
        return null;
     }
 
     public CSP.AssignmentsState backTrack() {
         return backTrack(new AssignmentsState());
     }
 
     public AssignmentsState backTrack(AssignmentsState state) {
 
         if (this.checkComplete(state.getAssignments())) {
             return state;
         }
         boolean getNewVariable;
         do {
             getNewVariable = false;
             Graph.Vertex variable = this.selectUnassigned();
             List<Paint> domain = this.orderDomain(variable);
             if (domain.isEmpty()) {
 
                 System.out.println("Empty domain");
                 return state;
             }
 
             Iterator<Paint> it = domain.iterator();
             while (it.hasNext()) {
                 Paint value = it.next();
 
                 Assignment assignment = new Assignment(variable, value);
 
                 state.takeSnapshot();
                 if (state.addAssignment(assignment)) {
                     it.remove(); // Ekleme yapildiysa domainden çıkar.
                     //Inference
                     if (forwardChecking) {
                         if (forwardCheck(assignment)) {
                             return backTrack(state);
                         } else {
                             stats.forwardCheckFailure++;
                             //Komşuların domainlerine silinen rengi geri ekle
                             // Silinenler burada eklendiği için state snaplshot'ından domains'i kaldırdım.
                             List<Vertex> neighbours = assignment.variable.getNeighbours();
 
                             for (Vertex neighbour : neighbours) {
                                 List<Paint> neighbourDomain = domains.get(neighbour);
                                 neighbourDomain.add(assignment.color);
                             }
                             state.removeAssignment(assignment);
                             getNewVariable = true;
                             //unassigned'lara geri ekle?
                             //unassignedVariables.add(assignment.variable); 
                             // bence geri eklemicez çünkü zaten şu anda o değişkenin domaini içersindeyiz.
                             // yani en içteki döngü o değişkenin domainini çeviriyor zaten
                             
                         }
                     } else {
                         return backTrack(state);
                     }
                 }
                 state.revert();
             }
         } while (getNewVariable);
 
         System.out.println("Olmuyordu, zorlamadim..");
         return state;
     }
 
     /**
      * Aslında forwardCheck orijanl domainler üzerinde değilde kopyaları
      * üzerinde mi çalışsa dedim ama böylesi daha verimli olacak sanırım
      *
      * @param assignment
      * @return
      */
     public boolean forwardCheck(Assignment assignment) {
 
         //komşuların domainlerini revize et, boşalan varsa false dön
         List<Vertex> neighbours = assignment.variable.getNeighbours();
 
         for (Vertex neighbour : neighbours) {
 
             List<Paint> neighbourDomain = domains.get(neighbour);
             neighbourDomain.remove(assignment.color);
             if (neighbourDomain.isEmpty()) {
                 return false;
             }
         }
 
         return true;
     }
 
     private boolean domainRevise(Graph.Edge arc) {
         boolean revised = false;
         List<Paint> startDomain = domains.get(arc.getStart());
         List<Paint> finishDomain = domains.get(arc.getFinish());
 
         Iterator<Paint> it = startDomain.iterator();
         while (it.hasNext()) {
             Paint value = it.next();
             if (finishDomain.contains(value)) {
                 it.remove();
                 revised = true;
             }
         }
         return revised;
     }
 }
