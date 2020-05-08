 package synthseq.playables.readables.waveforms;
 
 import synthseq.playables.readables.ReadableSound;
 import synthseq.playables.readables.Variable;
 
 
 
 public class TriangleWave extends ReadableSound{
 	private double time = 0;
 	private Variable freq;
 	private double direction = 1;
 	private boolean running = false;
 
 	public TriangleWave(double freq){
 		this(new Variable(freq));
 	}
 	public TriangleWave(Variable freq) {
 		this.freq =  freq;
 	}
 
 	@Override
 	public double read() {
 		if (!running)
 			return 0;
 		time++;
 		double val = freq.read()*2;
 		if(time>=44100/val){
 			time-=44100/val;
 			direction*=-1;
 		}
		return (direction-direction*2*time/(44100/freq.read()))*2-1;
 	}
 
 	@Override
 	public void start() {
 		running = true;
 		freq.start();
 	}
 
 	@Override
 	public void stop() {
 		running = false;
 		freq.stop();
 	}
 
 }
