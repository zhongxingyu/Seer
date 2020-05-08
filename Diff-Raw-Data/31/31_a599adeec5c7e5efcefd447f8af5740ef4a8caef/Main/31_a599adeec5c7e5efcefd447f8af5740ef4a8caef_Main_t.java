 package net.pixelcop.monstrous.load;
 
 import net.pixelcop.monstrous.Job;
 
 public class Main {

     public static void main(String[] args) throws Exception {

        if (args.length < 3) {
            System.out.println("usage: test.sh <thread count> <test length in sec> URL");
            System.exit(1);
        }

         Job job = new Job();

        job.setType(Job.T_TIME_LIMIT);
        job.setNumThreads(Integer.parseInt(args[0]));
        job.setNumSeconds(Integer.parseInt(args[1]));
        job.setUrl(args[2]);

         LoadTester tester = new LoadTester(job);
         tester.start();
     }
 
 }
