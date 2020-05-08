 import java.util.*;
 
 public class DynamicProgrammingSolver {
   public static int INF = Integer.MAX_VALUE;
 
   public RandomProblemInstance problemInstance;
   public int[][] tmpMatrix;
 
   public DynamicProgrammingSolver(RandomProblemInstance problemInstance) {
     this.problemInstance = problemInstance;
     tmpMatrix = new int[problemInstance.numberOfJobs + 1][problemInstance.maximumProcessingTime + 1];
   }
 
   public List<Job> solve() {
     calculateTmpMatrix();
     return calculateSolutionJobs();
   }
 
  public int getMinimalObjectiveFunctionValue(){
    return tmpMatrix[tmpMatrix.length - 1][tmpMatrix[0].length - 1];
  }

   private List<Job> calculateSolutionJobs() {
     int t = problemInstance.jobs[problemInstance.numberOfJobs - 1].dueDate;
     List<Job> resultSet = new LinkedList<Job>();
 
     Job[] jobs = problemInstance.jobs;
 
     for(int j = problemInstance.numberOfJobs; j > 0; j--) {
       Job tmpJob = jobs[j-1];
       t = Math.min(t, tmpJob.dueDate);
       if(costFunction(j, t) == costFunction(j - 1, t) + tmpJob.weight) {
         resultSet.add(tmpJob);
       } else {
         t = t - tmpJob.processingTime;
       }
     }
 
     return resultSet;
   }
 
   private void calculateTmpMatrix() {
     Arrays.sort(problemInstance.jobs);
     Job[] jobs = problemInstance.jobs;
 
     for(int j = 1; j <= problemInstance.numberOfJobs; j++) {
       Job tmpJob = jobs[j-1];
 
       for(int t = 0; t <= tmpJob.dueDate; t++) {
         if(costFunction(j - 1, t) + tmpJob.weight < costFunction(j - 1, t - tmpJob.processingTime)) {
           tmpMatrix[j][t] = costFunction(j - 1, t) + tmpJob.weight;
         } else {
           tmpMatrix[j][t] = costFunction(j - 1, t - tmpJob.processingTime);
         }
       }
 
       for(int t = tmpJob.dueDate + 1; t <= problemInstance.maximumProcessingTime; t++) {
         tmpMatrix[j][t] = costFunction(j, tmpJob.dueDate);
       }
 
     }
   }
 
   private int costFunction(int j, int t) {
     // do not forget to shift j by -1 later
 
     if(t < 0) {
       return INF;
     } else if(j == 0) {
       return 0;
     } else {
       return tmpMatrix[j][t];
     }
   }
 
   public static void main(String[] args) {
     int problemSize = Integer.parseInt(args[0]);
     long start = System.nanoTime();
 
     RandomProblemInstance instance = new RandomProblemInstance(problemSize);
     System.out.println("Job list:");
     System.out.println("----------------------------------------------------");
     for(Job j: instance.jobs) {
       System.out.println(j);
     }
    System.out.println("----------------------------------------------------");
     System.out.println();
     System.out.println();
 
     DynamicProgrammingSolver solver = new DynamicProgrammingSolver(instance);
     List<Job> result = solver.solve();
 
     long end = System.nanoTime();
 
    System.out.println("Late Jobs:");
     System.out.println("----------------------------------------------------");
     for(Job j: result) {
       System.out.println(j);
     }
     System.out.println("----------------------------------------------------");
    System.out.println();
    System.out.println();

    System.out.println("Stats: ");
    System.out.println("----------------------------------------------------");
     System.out.println("The operation took: " + (double)((end - start) * 1000000.0) + " seconds");
     System.out.println("The problem instance size was: " + instance.numberOfJobs);
    System.out.println("The number of late jobs: " + result.size());
    System.out.println("The objective function value of the optimal solution is: " + solver.getMinimalObjectiveFunctionValue());
     System.out.println("----------------------------------------------------");
   }
 }
