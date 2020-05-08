 package twofishes.pipedream.engine;
 
import java.util.ArrayList;
 import java.util.Random;
 
 import twofishes.pipedream.pipe.AbsPipe;
 import twofishes.pipedream.pipe.MovablePipes;
 
 public class PipeGenerator {
 
	protected ArrayList <Class<? extends AbsPipe>> pipeClassList;
 
 	protected Random random = null;
 
	public PipeGenerator(ArrayList <Class<? extends AbsPipe>> pipeClassList) {
		this.pipeClassList = pipeClassList;
 		random = new Random(System.currentTimeMillis());
 	}
 
 	public AbsPipe getNewPipe() {
 		
 		int positiveNextInt = Math.abs(random.nextInt()); 
		int index = positiveNextInt % (pipeClassList.size());
		Class<? extends AbsPipe> pipeClass = pipeClassList.get(index);
 		try {
 			return pipeClass.newInstance();
 		} catch (Exception e) {
 			System.out.println("Exception Thrown from PipeGenerator: " + e);
 			e.printStackTrace();
 		}
 		return null;
 
 	}
 
 }
