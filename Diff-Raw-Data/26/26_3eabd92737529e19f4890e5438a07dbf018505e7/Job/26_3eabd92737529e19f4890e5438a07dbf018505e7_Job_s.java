 public class Job implements Comparable<Job> {
   public int weight;
   public int processingTime;
   public int dueDate;
 
   public Job(int weight, int processingTime, int dueDate) {
     this.weight         = weight;
     this.processingTime = processingTime;
     this.dueDate        = dueDate;
   }
 
   public int compareTo(Job other) {
     return this.dueDate - other.dueDate;
   }
 
   public String toString() {
     return "w: " + weight + ", p: " + processingTime + ", d: " + dueDate;
   }
 
   public static Job getRandomJob() {
     int weight         = 1 + (int)(Math.random() * ((100 - 1) + 1));
     int processingTime = 1 + (int)(Math.random() * ((200 - 1) + 1));
    int dueDate        = (1 + processingTime) + (int)(Math.random() * ((2*processingTime - (1 + processingTime)) + 1));
 
     return new Job(weight, processingTime, dueDate);
   }
 }
