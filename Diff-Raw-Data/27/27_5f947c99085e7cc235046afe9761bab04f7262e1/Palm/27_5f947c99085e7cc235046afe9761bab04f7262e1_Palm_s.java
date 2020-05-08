 //FILE:          Palm.java
 //PROJECT:       Octane
 //-----------------------------------------------------------------------------
 //
 // AUTHOR:       Ji Yu, jyu@uchc.edu, 11/14/12
 //
 // LICENSE:      This file is distributed under the BSD license.
 //               License text is included with the source distribution.
 //
 //               This file is distributed in the hope that it will be useful,
 //               but WITHOUT ANY WARRANTY; without even the implied warranty
 //               of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 //
 //               IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 //               CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 //               INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES.
 
 package edu.uchc.octane;
 
 import ij.IJ;
 import ij.ImagePlus;
 import ij.ImageStack;
 import ij.gui.Roi;
 import ij.process.ColorProcessor;
 import ij.process.FloatProcessor;
 import ij.process.ImageProcessor;
 
 import java.awt.Color;
 import java.awt.Rectangle;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.concurrent.ExecutionException;
 
 import javax.swing.SwingWorker;
 
 import org.apache.commons.math3.util.FastMath;
 
 /**
  *  PALM plotting module
  */
 public class Palm {
 	int nPlotted_;
 	int nSkipped_;
 
 	public enum PalmType {AVERAGE, HEAD, TAIL, ALLPOINTS, TIMELAPSE};
 	//public enum ZRenderMode {NONE, COLOR, STACK}; 
 
 	private boolean correctDrift_;
 
 	private TrajDataset dataset_;
 	private ImagePlus imp_;
 
 	private Rectangle rect_;
 
 	private double palmScaleFactor_;
 
 	private final double palmThreshold_ = 1e6;
 
 	int lut_[] = null;
 
 	boolean bRenderStack_;
 	boolean bRenderInColor_;
 	int nSlices_;
 	int width_, height_;
 	double sigma_, sigmaZ_;
 	double zMin_, zMax_;
 	double zBottom_, zTop_;
 
 
 	private FloatProcessor [] ips_;
 	private ImageStack stack_;
 
 	/**
 	 * Constructor
 	 * @param dataset The dataset
 	 */
 	public Palm (TrajDataset dataset) {
 		dataset_ = dataset;
 		lut_ = new int[256];
 		for (int i = 0; i < 256; i++) {
 			lut_[i] = Color.getHSBColor(i/255f, 1f, 1f).getRGB();
 		}
 	}
 
 	private Rectangle getCurrentROI(ImagePlus imp) {
 		Rectangle rect;
 		Roi roi = imp.getRoi();
 		if (roi!=null && roi.isArea()) {
 			rect = roi.getBounds();
 		} else {
 			imp.killRoi();
 			rect = imp.getProcessor().getRoi();
 			imp.setRoi(roi);
 		}
 		return rect;
 	}
 
 	/**
 	 * Get number of particles plotted
 	 * @return Number of particles plotted
 	 */
 	public int getNPlotted() {
 		return nPlotted_;
 	}
 
 	/**
 	 * Get number of particle skipped
 	 * @return Number of particles skipped
 	 */
 	public int getNSkipped() {
 		return nSkipped_;
 	}
 
 	private SmNode getCorrectedNode(SmNode node) {
 		if (correctDrift_) {
 			SmNode n = node;
 			try {
 				node = dataset_.correctDrift(node);
 			} catch (OctaneException e) {
 				IJ.log("Cannot compensate drift. No data available.");
 				correctDrift_ = false;
 				return n;
 			}
 		}
 		return node;
 	}
 
 	private int getColor(double z) {
 		int zIndex;
 		if (z < zMin_) {
 			zIndex = 0;
 		} else if (z > zMax_) {
 			zIndex = lut_.length - 1;
 		} else {
 			zIndex = (int)((z - zMin_) / (zMax_ - zMin_) * lut_.length);
 		}
 
 		int rgb = lut_[zIndex];
 
 		return rgb;
 	}
 
 	private void increasePixelValue(int idx, int x, int y, double v) {
 		ImageProcessor ip = ips_[idx];
 		ip.setf(x, y, ip.getf(x,y) + (float)v);
 	}
 
 	private void increaseColorPixelValue(int idx, int x, int y, double v, int r, int g, int b) {
 		increasePixelValue(idx * 3, x, y, v * r);
 		increasePixelValue(idx * 3 + 1, x, y, v * g);
 		increasePixelValue(idx * 3 + 2, x, y, v * b);
 	}
 
 	private void renderGaussianSpot(double xs, double ys, double z) {
 		int r = 0, g = 0, b = 0;
 		double zs = 0;
 
 		if (bRenderInColor_) {
 			int rgb = getColor(z);
 			r = (rgb&0xff0000)>>16;
 		g = (rgb&0xff00)>>8;
 		b = rgb&0xff;
 		}
 
 		if (bRenderStack_) {
 			zs = (z - zBottom_) * palmScaleFactor_;
 		}
 
 		double sigma2 = 2 * sigma_ * sigma_;
 		double sigmaz2 = 2 * sigmaZ_ * sigmaZ_;
 
 		for (int x = FastMath.max(0, (int)(xs - 3 * sigma_)); x < FastMath.min(width_, (int)(xs + 3 * sigma_)); x ++) {
 			for (int y = FastMath.max(0, (int)(ys - 3 * sigma_)); y < FastMath.min(height_, (int)(ys + 3 * sigma_)); y++) {
 
 				double v = FastMath.exp( - ((x-xs) * (x-xs) + (y-ys)*(y-ys)) / sigma2 );
 
 				if (bRenderStack_) {
 
 					for (int zi = FastMath.max(0, (int)(zs - 3 * sigmaZ_)); zi < FastMath.min(nSlices_, zs + 3 * sigmaZ_); zi++) {
 
 						double intensity = FastMath.exp(-(zi - zs) * (zi-zs) / sigmaz2);
 
 						if (bRenderInColor_) {
 
 							increaseColorPixelValue(zi, x, y, intensity * v, r, g, b);
 
 						} else {
 
 							increasePixelValue(zi, x, y, intensity * v * 255);
 						}
 					}
 				} else { // not render to stack
 
 					if (bRenderInColor_) {
 
 						increaseColorPixelValue(0, x, y, v , r, g, b);
 
 					} else {
 
 						increasePixelValue(0, x, y, v * 255);
 					}
 				}
 			}
 		}
 	}
 
 
 	private void renderGaussianSpot(SmNode node) {
 		
 		if (OctaneWindowControl.compensateDrift_) {
 		
 			node = getCorrectedNode(node);
 		
 		}
 
 		double xs = (node.x - rect_.x) * palmScaleFactor_;
 		double ys = (node.y - rect_.y) * palmScaleFactor_;
 
 		renderGaussianSpot(xs, ys, node.z);
 	}
 
 	private void renderGaussianSpotInMovie(SmNode node) {
 		
 		int r = 0, g = 0, b = 0;
 
 		if (OctaneWindowControl.compensateDrift_) {
 		
 			node = getCorrectedNode(node);
 			
 		}
 
 		if (bRenderInColor_) {
 			
 			int rgb = getColor(node.z);
 			
 			r = (rgb&0xff0000)>>16;
 			g = (rgb&0xff00)>>8;
 			b = rgb&0xff;
 		
 		}
 
 		double xs = (node.x - rect_.x) * palmScaleFactor_;
 		double ys = (node.y - rect_.y) * palmScaleFactor_;
 		double sigma2 = 2 * sigma_ * sigma_;
 
 
 		for (int x = FastMath.max(0, (int)(xs - 3 * sigma_)); x < FastMath.min(width_, (int)(xs + 3 * sigma_)); x ++) {
 			for (int y = FastMath.max(0, (int)(ys - 3 * sigma_)); y < FastMath.min(height_, (int)(ys + 3 * sigma_)); y++) {
 
 				double v = FastMath.exp( - ((x-xs) * (x-xs) + (y-ys)*(y-ys)) / sigma2 );
 
 				if (bRenderInColor_) {
 
 					increaseColorPixelValue(node.frame - 1, x, y, v , r, g, b);
 
 				} else {
 
 					increasePixelValue(node.frame - 1, x, y, v * 255);
 				}
 			}
 		}
 	}
 
 	private void renderAverage(Trajectory traj) {
 		if (traj == null) {
 			return;
 		}
 
 		SmNode node;
 
 		node = traj.get(0);
 		
 		if (OctaneWindowControl.compensateDrift_) {
 
 			node = getCorrectedNode(node);
 		
 		}
 
 		double xx= node.x;
 		double yy= node.y;
 		double zz = node.z;
 		double xx2 = xx*xx;
 		double yy2 = yy*yy;
 		double zz2 = zz*zz;
 
 		for (int j = 1; j < traj.size(); j++ ) {
 
 			node = traj.get(j);
 			node = getCorrectedNode(node);
 			xx += node.x;
 			yy += node.y;
 			zz += node.z;
 			xx2 += node.x * node.x;
 			yy2 += node.y * node.y;
 			zz2 += node.z * node.z;
 
 		}
 
 		xx /= traj.size();
 		yy /= traj.size();
 		zz /= traj.size();
 		xx2 /= traj.size();
 		yy2 /= traj.size();
 		zz2 /= traj.size();
 
 		if (xx2 - xx * xx < palmThreshold_ && yy2 - yy * yy < palmThreshold_) {
 			
 			double xs = (xx - rect_.x)* palmScaleFactor_;
 			double ys = (yy - rect_.y)* palmScaleFactor_;
 			double zs = zz;
 
 			renderGaussianSpot(xs, ys, zs);
 
 			nPlotted_ ++;
 
 		} else {
 
 			nSkipped_ ++;
 		}
 	}
 
 	private void renderAllPoints(Trajectory traj) {
 
 		if (traj == null ) {
 			return;
 		}
 
 		for (int j = 0; j < traj.size(); j++ ) {
 		
 			renderGaussianSpot(traj.get(j));
 			nPlotted_ ++;
 		
 		}
 	}
 
 	private void renderMovie(Trajectory traj) {
 		
 		if (traj == null ) {
 		
 			return;
 		
 		}
 
 		for (int i = 0; i < traj.size(); i++ ) {
 		
 			renderGaussianSpotInMovie(traj.get(i));
 		
 		}
 	}
 
 	private void processColor() {
 
 		if (! bRenderInColor_) {
 		
 			return;
 		
 		}
 
 		double max = 0 ;
 		for (int i = 0; i < ips_.length; i++) {
 		
 			FloatProcessor ip = ips_[i];
 			float [] pixels = (float [])ip.getPixels();
 
 			for (int j = 0; j < pixels.length; j++) {
 				max = FastMath.max(max, pixels[j]);
 			
 			}
 		}
 
 		for (int i = 0; i < ips_.length; i += 3) {
 			
 			ColorProcessor cp = new ColorProcessor(width_, height_);
 
 			for (int j = 0; j < width_ * height_; j++) {
 				int r = (int)(ips_[i].getf(j) * 255 / max);
 				int g = (int)(ips_[i + 1].getf(j) * 255 / max);
 				int b = (int)(ips_[i + 2].getf(j) * 255 / max);
 				int rgb = r << 16 | g << 8 | b ;
 				cp.set(j, rgb);
 			}
 
 			stack_.addSlice(cp);
 		}
 	}
 
 	/**
 	 * Construct PALM image / image stack
 	 * @param imp The original image data
 	 * @param selected Trajectories to be included in the PALM plot
 	 */
 	public void constructPalm(final ImagePlus imp, final int [] selected) {
 		
 		nPlotted_ = 0;
 		nSkipped_ = 0;
 
 		final PalmType palmType = PalmParameters.getPalmType();
 
 		correctDrift_ = OctaneWindowControl.compensateDrift_;
 
 		imp_ = imp;
 		
		palmScaleFactor_ = dataset_.pixelSize_ / PalmParameters.palmPixelSize_;
 		rect_ = getCurrentROI(imp);
 		width_ = (int) (rect_.width * palmScaleFactor_);
 		height_ = (int) (rect_.height * palmScaleFactor_);
 		sigma_ = PalmParameters.palmResolution_ / PalmParameters.palmPixelSize_ / 2.355;
 		sigmaZ_ = PalmParameters.zResolution_ / PalmParameters.palmPixelSize_ / 2.355; 
 
 		bRenderInColor_ = PalmParameters.isRenderInColor(); 
 		bRenderStack_ = PalmParameters.isRenderStack();
 
		zMin_ = PalmParameters.lutMin_ / dataset_.pixelSize_;
		zMax_ = PalmParameters.lutMax_ / dataset_.pixelSize_;
 
 		if (bRenderStack_) {
			zBottom_ = PalmParameters.palmZMin_ / dataset_.pixelSize_ ;
			zTop_ = PalmParameters.palZMax_/ dataset_.pixelSize_  ;
 			nSlices_ = (int) ((zTop_ - zBottom_) * palmScaleFactor_);
 
 		} else if (palmType == PalmType.TIMELAPSE) {
 
 			nSlices_ = imp.getStackSize();
 
 		} else {
 
 			nSlices_ = 1;
 		}
 
 		stack_ = new ImageStack(width_, height_);
 		
 		int nImages = nSlices_ * (bRenderInColor_ ? 3 : 1);
 		ips_ = new FloatProcessor[nImages];
 
 		for (int i = 0; i < nImages; i ++ ) {
 
 			ips_[i]= new FloatProcessor(width_, height_);
 
 		}
 
 		doConstructPALM(palmType, selected);
 
 	}
 
 	/**
 	 * Does most of the plotting work
 	 * @param type The type of PALM image
 	 * @param selected Trajectories to be included in the PALM plot
 	 */
 	void doConstructPALM(final PalmType type, final int [] selected) {
 
 		class MySwingWorker extends SwingWorker<ImagePlus, Void> {
 
 			@Override
 			public ImagePlus doInBackground() {
 				ImagePlus imp = null;
 
 				for ( int i = 0; i < selected.length; i ++) {					
 					Trajectory traj = dataset_.getTrajectoryByIndex(selected[i]);
 
 					switch (type) {
 					case HEAD:
 						renderGaussianSpot(traj.get(0));
 						nPlotted_ ++;
 						break;
 					case TAIL:
 						renderGaussianSpot(traj.get(traj.size()-1));
 						nPlotted_ ++;
 						break;
 					case AVERAGE:
 						renderAverage(traj);
 						break;
 					case ALLPOINTS:
 						renderAllPoints(traj);
 						break;
 					case TIMELAPSE:
 						renderMovie(traj);
 						break;
 					}
 
 					firePropertyChange("Progress", (double)i / selected.length, (double)(i + 1)/selected.length);
 				}
 
 				if (bRenderInColor_) {
 					processColor();
 				} else {
 					for (int i = 0 ; i < ips_.length; i++) {
 						stack_.addSlice(ips_[i]);
 					}
 				}
 
 				if (stack_.getSize() > 1) {
 					imp = new ImagePlus("PALM-" + imp_.getTitle(), stack_);
 				} else {
 					imp = new ImagePlus("PALM-" + imp_.getTitle(), stack_.getProcessor(1));
 				}
 				
 				return imp;
 
 			}
 			
 			@Override
 			public void done() {
 				ImagePlus imp = null;
 				try {
 					imp = get();
 				} catch (InterruptedException e) {
 					System.err.println("PALM thread interrupted");
 				} catch (ExecutionException e) {
 					IJ.log("PALM rendering error:" + e.getCause().getMessage());
 					e.printStackTrace();
 				}
 				if (imp != null) {
 					imp.show();
 				}
 				
 			}
 		}
 
 		MySwingWorker task = new MySwingWorker();
 
 		task.addPropertyChangeListener(new PropertyChangeListener() {
 			public void propertyChange(PropertyChangeEvent evt) {
 				if (evt.getPropertyName() == "Progress") {
 					IJ.showProgress((Double)evt.getNewValue());
 				}
 			}
 		});
 
 		task.execute();
 
 	}
 }
