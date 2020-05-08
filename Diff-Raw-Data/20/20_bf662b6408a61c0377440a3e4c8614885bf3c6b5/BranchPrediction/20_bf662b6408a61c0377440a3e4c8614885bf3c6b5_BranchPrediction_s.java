 import java.util.LinkedList;
 
 public class BranchPrediction {
 
 	public class twoBit {
 		int state;
 		
 		public twoBit()	{
 			state = 2;
 		}
 		
 		public twoBit(int i)	{
			state = i;		// TODO performs 0.6% better when gshare is initialized at 0
 		}
 
 		public boolean predict() {
 			return state > 1;
 		}
 
 		public void train(boolean outcome) {
 			if (outcome && state < 3)
 				state++;
 			else if (!outcome && state > 0)
 				state--;
 		}
 	}
 
 	public class bimodal {
 		final static int n = 10;
 		static final int size = 1 << n;
 		private twoBit arr[];
 
 		public bimodal() {
 			arr = new twoBit[size];
 			for (int i = 0; i < size; ++i)
 				arr[i] = new twoBit();
 		}
 
 		public boolean predict(long pc, boolean outcome) {
 			pc %= size;
 			boolean prediction = arr[(int)pc].predict();
 			return prediction;
 		}
 
 		public void train(long pc, boolean outcome) {
 			pc %= size;
 			arr[(int)pc].train(outcome);
 		}
 	}
 
 	public class gShare {
 		final static int k = 12;
 		static final int size = 1 << k;
 		int bhr;
 		private twoBit arr[];
 
 		public gShare() {
 			arr = new twoBit[size];
 			for (int i = 0; i < size; ++i)
				arr[i] = new twoBit();
 		}
 
 		public boolean predict(long pc, boolean outcome) {
 			pc %= size;
 			int r = (int)pc ^ bhr;
 			boolean prediction = arr[r].predict();
 			return prediction;
 		}
 
 		public void train(long pc, boolean outcome) {
 			pc %= size;
 			int r = (int)pc ^ bhr;
 			if (outcome)
 				bhr += (1 << 6);
 			bhr >>= 1;
 			arr[r].train(outcome);
 		}
 	}
 
 	/** accuracy meter **/
 	public long correct, total;
 	public void update(boolean precision) {
 		if (precision)
 			correct++;
 		total++;
 	}
 	public double givAcc() {
 		return ((double)(100 * correct) / (double)total);
 	}
 
 	/*********** tournament predictor ***********/
 	final static int m = 9;
 	static final int size = 1 << m;
 
 	public gShare g = new gShare();
 	public bimodal b = new bimodal();
 	private twoBit c[];
 	
 	/** constructor **/
 	public BranchPrediction() {
 		c = new twoBit[size];
 		for (int i = 0; i < size; ++i)
 			c[i] = new twoBit();
 	}
 
 	private LinkedList<Boolean> gResultQ = new LinkedList<Boolean>(),
 								bResultQ = new LinkedList<Boolean>();
 
 	public boolean predict(long pc, boolean outcome) {
		
 		int intpc = (int) pc % size;
 		
 		boolean gpred = g.predict(pc, outcome), // predictor 1
 				bpred = b.predict(pc, outcome); // predictor 2
 		
 		gResultQ.add(gpred);
 		bResultQ.add(bpred);
 		
 		boolean prediction = c[intpc].predict() ? bpred : gpred;
 		update(prediction == outcome);
 		return prediction;
 	}
 
 	public void train(long pc, boolean outcome) {
 		int intpc = (int)pc % size;
 		
 		g.train(pc, outcome);
 		b.train(pc, outcome);
 
 		boolean bpred = bResultQ.pop();
 		if(bpred ^ gResultQ.pop())
			c[intpc].train(bpred); 
 	}
 
 }                     
