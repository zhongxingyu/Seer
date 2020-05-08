 package synthseq.playables.readables.filters;
 
 import synthseq.playables.readables.ReadableSound;
 import synthseq.playables.readables.Variable;
 
 public class LowPass extends ReadableSound {
 	private ReadableSound r;
 	private Variable alpha;
 	private double old = 0;
 	private double sameCount = 0;
 	private boolean running = false;
 
 	public LowPass(ReadableSound r, Variable v) {
 		this.r = r;
 		old = r.read();
 		this.alpha = v;
 	}
 	
 	public LowPass(ReadableSound r, double v){
 		this(r,new Variable(v));
 	}
 
 	@Override
 	public double read() {
 		// see
 		// http://en.wikipedia.org/wiki/Low-pass_filter#Algorithmic_implementation
 		if(!running)
 			return 0;
 		double out = alpha.read() * r.read() + (1 - alpha.read()) * old;
		old = out;
 		if(old==out)
 			sameCount++;
 		if(sameCount > 1000){
 			stop();
 			sameCount = 0;
 		}
 		return out;
 	}
 
 	@Override
 	public void start() {
 		running  = true;
 		r.start();
 		alpha.start();
 	}
 
 	@Override
 	public void stop() {
 		running = false;
 		r.stop();
 		alpha.stop();
 	}
 }
