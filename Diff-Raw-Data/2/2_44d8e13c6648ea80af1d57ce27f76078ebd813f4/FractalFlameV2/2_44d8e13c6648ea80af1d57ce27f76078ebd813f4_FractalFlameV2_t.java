 import java.util.concurrent.ThreadLocalRandom;
 
 import processing.core.PApplet;
 
 public final class FractalFlameV2 extends PApplet {
 	ThreadLocalRandom	r		= ThreadLocalRandom.current();
 	
 	int					iters	= 0;
 	
 	public static final void main(final String args[]) {
 		System.out.println(java.lang.Runtime.getRuntime().maxMemory());
 		PApplet.main(new String[] { "--present", "FractalFlameV2" });
 	}
 	
 	public final void setup() {
 		size(GLB.swid, GLB.shei);
 		background(0);
 		fill(0);
 		stroke(0);
 		GLB.newHistogram();
 		GLB.reset();
 	}
 	
 	public final void keyPressed() {
 		if ((key == 'r') || (key == 'R')) {
 			GLB.reset();
 			return;
 		}
 		
 		if ((key == 'f') || (key == 'F')) {
 			GLB.enableVariations = !GLB.enableVariations;
 			GLB.resetHistogram();
 		}
 		
 		GLB.stopThreads();
 		
 		if ((key == 'h') || (key == 'H')) {
 			GLB.ss = GLB.ss != GLB.ssMAX ? GLB.ssMAX : GLB.ssMIN;
 			GLB.hwid = GLB.swid * GLB.ss;
 			GLB.hhei = GLB.shei * GLB.ss;
 			GLB.newHistogram();
 			GLB.resetHistogram();
 		}
 		
 		if ((key == 'c') || (key == 'C')) {
 			GLB.resetAffineColorMap();
 			GLB.resetHistogram();
 		}
 		
 		if ((key == 'g') || (key == 'G')) {
 			GLB.resetGamma();
 		}
 		
 		if ((key == 't') || (key == 'T')) {
 			GLB.nThreads = (GLB.nThreads == GLB.maxThreads) ? 1 : GLB.maxThreads;
 		}
 		
 		if ((key == '-') || (key == '_')) {
 			GLB.cameraXShrink *= 1.01;
 			GLB.cameraYShrink *= 1.01;
 			GLB.resetHistogram();
 		}
 		
 		if ((key == '+') || (key == '=')) {
 			GLB.cameraXShrink *= .99;
 			GLB.cameraYShrink *= .99;
 			GLB.resetHistogram();
 		}
 		
 		if (keyCode == UP) {
 			GLB.cameraYOffset += .01 * GLB.cameraYShrink;
 			GLB.resetHistogram();
 		}
 		if (keyCode == DOWN) {
 			GLB.cameraYOffset -= .01 * GLB.cameraYShrink;
 			GLB.resetHistogram();
 		}
 		
 		if (keyCode == LEFT) {
 			GLB.cameraXOffset += .01 * GLB.cameraXShrink;
 			GLB.resetHistogram();
 		}
 		
 		if (keyCode == RIGHT) {
 			GLB.cameraXOffset -= .01 * GLB.cameraXShrink;
 			GLB.resetHistogram();
 		}
 		GLB.startThreads();
 		
 	}
 	
 	public final void draw() {
 		if (frameCount <= 5) {
 			loadPixels();
 		}
 		
 		double maxA = 0;
 		
 		for (int y = 0; y < GLB.hhei; y++) {
 			for (int x = 0; x < GLB.hwid; x++) {
 				final int px = x / GLB.ss;
 				final int py = y / GLB.ss;
 				
 				final double r = GLB.h.histo[(4 * x) + (4 * y * GLB.hwid) + 0];
 				final double g = GLB.h.histo[(4 * x) + (4 * y * GLB.hwid) + 1];
 				final double b = GLB.h.histo[(4 * x) + (4 * y * GLB.hwid) + 2];
 				final double a = GLB.h.histo[(4 * x) + (4 * y * GLB.hwid) + 3];
 				
 				GLB.image[(4 * px) + (4 * py * GLB.swid) + 0] += r;
 				GLB.image[(4 * px) + (4 * py * GLB.swid) + 1] += g;
 				GLB.image[(4 * px) + (4 * py * GLB.swid) + 2] += b;
 				GLB.image[(4 * px) + (4 * py * GLB.swid) + 3] += a;
 				
 				// grab the alpha of the current image pixel to see if it's larger than any other pixel
 				final double imga = GLB.image[(4 * px) + (4 * py * GLB.swid) + 3];
				maxA = (maxA >= imga) ? maxA : imga;
 			}
 		}
 		// maxA holds the sum of each histogram-block per pixel, so we divide the sum by the number of bins per pixel
 		// (supersamples squared) to get the average
 		maxA /= (GLB.ss * GLB.ss);
 		
 		final double logMaxA = Math.log(maxA);
 		
 		for (int y = 0; y < GLB.shei; y++) {
 			for (int x = 0; x < GLB.swid; x++) {
 				
 				double a_avg = GLB.image[(4 * x) + (4 * y * GLB.swid) + 3] / (GLB.ss * GLB.ss);
 				
 				a_avg = ((a_avg != 0) && (a_avg <= 1.0)) ? 1 : a_avg;
 				
 				if (a_avg != 0) {
 					final double r_avg = GLB.image[(4 * x) + (4 * y * GLB.swid) + 0] / (GLB.ss * GLB.ss);
 					final double g_avg = GLB.image[(4 * x) + (4 * y * GLB.swid) + 1] / (GLB.ss * GLB.ss);
 					final double b_avg = GLB.image[(4 * x) + (4 * y * GLB.swid) + 2] / (GLB.ss * GLB.ss);
 					double color_scale_factor = Math.log(a_avg) / logMaxA;
 					if (GLB.gamma != 1) {
 						color_scale_factor = Math.pow(color_scale_factor, 1 / GLB.gamma);
 					}
 					
 					final int a = 0xFF;
 					final int r = (int) ((r_avg * color_scale_factor) * 0xFF) & 0xFF;
 					final int g = (int) ((g_avg * color_scale_factor) * 0xFF) & 0xFF;
 					final int b = (int) ((b_avg * color_scale_factor) * 0xFF) & 0xFF;
 					
 					pixels[x + (y * GLB.swid)] = (a << 24) | (r << 16) | (g << 8) | (b << 0);
 				} else {
 					pixels[x + (y * GLB.swid)] = 0xFF << 24;
 				}
 				GLB.image[(4 * x) + (4 * y * GLB.swid) + 0] = 0;
 				GLB.image[(4 * x) + (4 * y * GLB.swid) + 1] = 0;
 				GLB.image[(4 * x) + (4 * y * GLB.swid) + 2] = 0;
 				GLB.image[(4 * x) + (4 * y * GLB.swid) + 3] = 0;
 			}
 		}
 		
 		updatePixels();
 		if (((frameCount % 3) == 0) && (GLB.ss == GLB.ssMAX)) {
 			saveFrame(GLB.uFlameID + ".bmp");
 		}
 	}
 }
