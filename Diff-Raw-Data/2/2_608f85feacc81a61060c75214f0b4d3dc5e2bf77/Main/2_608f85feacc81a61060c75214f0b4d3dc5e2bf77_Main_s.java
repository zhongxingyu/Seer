 public class Main {
    private static boolean test;
 
   public static void main(String[] args) throws Exception {
     test = getTest();
     if (!getTest()) {
       ProcessImage main = new ProcessImage(getInfile(), getOutfile(), getThreads(), getPhases());
       main.processImage();
       main.saveImage();
     } else {
       int i = Runtime.getRuntime().availableProcessors();
       long[][] data = new long[i][10];
       for (int k=0; k < i; k++ ) {
         for (int j=0; j< 10; j++) {
           ProcessImage main = new ProcessImage(getInfile(), null, (k+1), getPhases(), false);
           main.processImage();
           data[k][j] = main.getTime();
         }
       }
       outputJson(processData(data));
     }
   }
 
   private static boolean getTest() {
     return System.getProperty("test") != null;
   }
 
   private static String getInfile() {
     String infile = System.getProperty("infile");
     if(!test)
       System.out.println("infile: " + infile);
     return infile;
   }
 
   private static String getOutfile() {
     String outfile = System.getProperty("outfile");
     if(!test)
       System.out.println("outfile: " + outfile);
     return outfile;
   }
 
   private static int getThreads() {
     int threads = 0;
     try {
       threads = Integer.parseInt(System.getProperty("threads"));
     } catch (NumberFormatException e) {
       System.out.println("threads needs to be a number");
       System.exit(0);
     }
     if (threads < 0)
       threads = Runtime.getRuntime().availableProcessors();
     if(!test)
       System.out.println("threads: " + threads);
     return threads;
   }
 
   private static int getPhases() {
     int phases = 0;
     try {
       phases = Integer.parseInt(System.getProperty("phases"));
     } catch (NumberFormatException e) {
       System.out.println("phases needs to be a number");
       System.exit(0);
     }
     if(!test)
       System.out.println("phases: " + phases);
     return phases;
   }
 
   private static long[] processData(long[][] data) {
     long[] paldata = new long[data.length];
     for (int i=0; i<data.length;i++) {
       for (int j=0; j<data[0].length; j++) {
         paldata[i] += data[i][j];
       }
       paldata[i] /= data[0].length;
     }
     return paldata;
   }
 
   private static void outputJson(long[] data) {
     System.out.print("[");
     for (int i=0; i<data.length;i++) {
      System.out.print("["+i+","+data[i]+"]");
       if (i+1 != data.length)
         System.out.print(",");
     }
     System.out.print("]");
   }
 }
