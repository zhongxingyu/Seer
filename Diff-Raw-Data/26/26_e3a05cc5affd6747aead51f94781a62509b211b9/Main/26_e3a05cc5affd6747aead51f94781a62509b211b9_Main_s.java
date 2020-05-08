 package fractalFlameV3;
 
 import fractalFlameV3.fractalGenome.FractalGenome;
 import fractalFlameV3.fractalThread.FractalThread;
 import fractalFlameV3.fractalThread.ThreadSignal;
 import processing.core.PApplet;
 import processing.core.PConstants;
 
 public class Main extends PApplet {
	static boolean	fullscreen	= true;
 
	int	            swid	   = 1920;
	int	            shei	   = 1080;
	int	            ss	       = 1;
 
	int	            fr	       = 60;
 
 	Histogram	    h;
 	FractalGenome	genome;
 	FractalThread[]	threads;
 	ThreadSignal	threadSignal;
 
 	double	        seed;
 
 	public static final void main(final String args[]) {
 		if (Main.fullscreen) {
 			PApplet.main(new String[] { "--present", "fractalFlameV3.Main" });
 		} else {
			PApplet.main(new String[] { "main.Main" });
 		}
 	}
 
 	@Override
 	public void setup() {
 		this.size(swid, shei);
 		frameRate(fr);
 
 		h = newHistogram();
 
 		seed = this.random(0, 1);
 		genome = newGenome();
 		threads = new FractalThread[1];
 		threadSignal = new ThreadSignal();
 		startThreads();
 
 	}
 
 	private FractalGenome newGenome() {
 		final FractalGenome fg = new FractalGenome(3, 10);
 		fg.variationToggle = true;
 		fg.finalTransformToggle = true;
 		fg.setLogScale();
 		FractalGenome.center = true;
 		return fg;
 	}
 
 	private Histogram newHistogram() {
 		final Histogram h = new Histogram(swid, shei, ss);
 		return h;
 	}
 
 	@Override
 	public void keyPressed() {
 		stopThreads();
 
 		if ('h' == Character.toLowerCase(key)) {
 			ss = (ss == 1) ? 15 : 1;
 			h = null;
 			System.gc();
 			h = newHistogram();
 			System.out.println("# SS\t|\t " + ss);
 		}
 
 		if ('r' == Character.toLowerCase(key)) {
 			genome = newGenome();
 			h.reset();
 		}
 
 		if ('t' == Character.toLowerCase(key)) {
 			threads = new FractalThread[(threads.length == 8) ? 1 : 8];
 			System.out.println("# TH\t|\t " + threads.length);
 		}
 
 		if ('f' == Character.toLowerCase(key)) {
 			genome.finalTransformToggle = !genome.finalTransformToggle;
 			System.out.println("# FT\t|\t " + genome.finalTransformToggle);
 			h.reset();
 		}
 
 		if ('v' == Character.toLowerCase(key)) {
 			genome.variationToggle = !genome.variationToggle;
 			System.out.println("# VT\t|\t " + genome.variationToggle);
 			h.reset();
 		}
 
 		if (('+' == key) || ('=' == key)) {
 			FractalGenome.cameraXShrink /= 1.01;
 			FractalGenome.cameraYShrink /= 1.01;
 			h.reset();
 		}
 
 		if (('-' == key) || ('_' == key)) {
 			FractalGenome.cameraXShrink *= 1.01;
 			FractalGenome.cameraYShrink *= 1.01;
 			h.reset();
 		}
 		if (keyCode == PConstants.UP) {
 			FractalGenome.cameraYOffset += .01 * FractalGenome.cameraYShrink;
 			h.reset();
 		}
 		if (keyCode == PConstants.DOWN) {
 			FractalGenome.cameraYOffset -= .01 * FractalGenome.cameraYShrink;
 			h.reset();
 		}
 
 		if (keyCode == PConstants.LEFT) {
 			FractalGenome.cameraXOffset += .01 * FractalGenome.cameraXShrink;
 			h.reset();
 		}
 
 		if (keyCode == PConstants.RIGHT) {
 			FractalGenome.cameraXOffset -= .01 * FractalGenome.cameraXShrink;
 			h.reset();
 		}
 		startThreads();
 	}
 
 	private void stopThreads() {
 		threadSignal.running = false;
 		for (final Thread t : threads) {
 			try {
 				t.join();
 			} catch (final InterruptedException e) {
 				System.out.println(e.getLocalizedMessage());
 			}
 		}
 	}
 
 	private void startThreads() {
 		threadSignal.running = true;
 		for (final int i : Utils.range(threads.length)) {
 			threads[i] = new FractalThread(genome, threadSignal, h);
 		}
 		for (final Thread t : threads) {
 			t.start();
 		}
 	}
 
 	@Override
 	public void draw() {
 		if (frameCount == 1) {
 			loadPixels();
 		} else {
 			h.updatePixels(pixels, genome);
 			this.updatePixels();
 		}
 	}
 }
