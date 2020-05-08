 package net.pixelcop.monstrous.load;
 
 import net.pixelcop.monstrous.Job;
 
 public class Main {
    
     public static void main(String[] args) throws Exception {
        
         Job job = new Job();
        job.setNumThreads(getThreadCount(args));
        
         LoadTester tester = new LoadTester(job);
         tester.start();
     }
    
    public static int getThreadCount(String[] args) {
        
        if (args.length == 0) {
            System.out.println("usage: test.sh <thread count>");
            System.exit(0);
        }
        
        return Integer.parseInt(args[0]);
        
    }
 
 }
