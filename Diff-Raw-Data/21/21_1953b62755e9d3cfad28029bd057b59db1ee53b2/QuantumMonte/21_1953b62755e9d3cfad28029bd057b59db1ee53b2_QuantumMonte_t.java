 import java.io.IOException;
 import java.util.Random;
 
 public class QuantumMonte {
     
     private Path path;
     private Action action;
 
     private double deltaTau;
     private int sliceCount;
 
     private double delta;
     private Random rand;    
     
     
     QuantumMonte(Path path, Action action, Random rand) {
         this.path = path;
         this.action = action;
         this.rand = rand;
 
         sliceCount = path.getSliceCount();
         deltaTau = path.getDeltaTau();
 
         delta = 0.5;
     }
 
 
     private void run(DataManager dataManager) {
         try {
             dataManager.openOutputFile("something.dat");			
             
            for (int j = 0; j < 5000; ++j) {               
                doMeasurement(dataManager, j);               
                 advance(100);	
             }
             dataManager.close();
         }
         catch (Exception e) {
             System.err.println("You broke something: " + e.getMessage());
         }
     }
 
 
     private void advance(int nstep) {
         for (int step = 0; step < nstep; step++){
             int z = rand.nextInt(sliceCount);
             int u = ((z-1)+sliceCount) % sliceCount;
             int w = (z+1) % sliceCount;
             double xold = path.getPosition(z);
             double xnew = xold + delta * (2*rand.nextDouble() - 1);
//          double xnew = xold + delta*rand.nextGaussian();
             double xnext = path.getPosition(w);
             double xprev = path.getPosition(u);
             double deltaS = 0;
             deltaS += action.calculateKinetic(xnew, xprev);
             deltaS += action.calculateKinetic(xnew, xnext);
             deltaS += action.calculatePotential(xnew);
             deltaS -= action.calculateKinetic(xold, xprev); 
             deltaS -= action.calculateKinetic(xold, xnext);
             deltaS -= action.calculatePotential(xold);
             if (rand.nextDouble() < Math.exp(-deltaS)) {
                 path.setPosition(z, xnew);
             }		
         }
     }
 
 
    private void doMeasurement(DataManager dataManager, int j) throws IOException {
         double sum = 0;
         double sumxsquared = 0;
         
         for (int i = 0; i < sliceCount; i++) {
             double x = path.getPosition(i);
             sum += x;
             double x2 = path.getPosition((i+1)%sliceCount);
             sumxsquared += (x2-x)*(x2-x);
         }
         double avgx = sum/sliceCount;
         double x = path.getPosition(0);
         double xsquared = x*x;
         double V = 0.5 * avgx * avgx;
         double T = 0.5 * (sumxsquared/sliceCount)/(deltaTau *deltaTau);
         double E = 1/(2*deltaTau) - T + V;
         dataManager.writeData(j,avgx,xsquared,E);
     }
 
     public static void main(String[] args){
 		
 		double kT = 0.5;
         int sliceCount = 10;
         Path path = new Path(sliceCount, kT);
         
         double deltaTau = path.getDeltaTau();
         Action action = new Action(deltaTau);
         
         DataManager dataManager = new DataManager();
 
         Random rand = new Random();
    	
 		QuantumMonte qmc = new QuantumMonte(path, action, rand);
 	
 		qmc.run(dataManager);
 	}
 
 }
