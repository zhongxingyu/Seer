 package twofishes.pipedream.engine;
 
 import java.util.Random;
 
 import twofishes.pipedream.pipe.AbsPipe;
 import twofishes.pipedream.pipe.MovablePipes;
 
 public class PipeGenerator {
 
	protected MovablePipes pipeMap;
 
 	protected Random random = null;
 
	public PipeGenerator(MovablePipes pipeMap) {
		this.pipeMap = pipeMap;
 		random = new Random(System.currentTimeMillis());
 	}
 
 	public AbsPipe getNewPipe() {
 		
 		int positiveNextInt = Math.abs(random.nextInt()); 
		int index = positiveNextInt % (pipeMap.size());
		Class<? extends AbsPipe> pipeClass = pipeMap.get(index);
 		try {
 			return pipeClass.newInstance();
 		} catch (Exception e) {
 			System.out.println("Exception Thrown from PipeGenerator: " + e);
 			e.printStackTrace();
 		}
 		return null;
 
 	}
 
 }
