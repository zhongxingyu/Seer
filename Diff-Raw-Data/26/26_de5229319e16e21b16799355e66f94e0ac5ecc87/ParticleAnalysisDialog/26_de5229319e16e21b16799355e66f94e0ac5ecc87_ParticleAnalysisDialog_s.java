 //FILE:          ParticleAnalysisDialog.java
 //PROJECT:       Octane
 //-----------------------------------------------------------------------------
 //
 // AUTHOR:       Ji Yu, jyu@uchc.edu, 3/30/13
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
 //               INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES./**
 //
 
 
 package edu.uchc.octane;
 
 import ij.IJ;
 import ij.ImageListener;
 import ij.ImagePlus;
 import ij.ImageStack;
 import ij.gui.DialogListener;
 import ij.gui.GenericDialog;
 import ij.gui.NonBlockingGenericDialog;
 import ij.gui.PointRoi;
 import ij.process.ImageProcessor;
 
 import java.awt.AWTEvent;
 import java.awt.Rectangle;
 
 /**
  * Base class for particle analysis dialogs that specify analysis parameters
  * @author Ji-Yu
  *
  */
 public abstract class ParticleAnalysisDialog extends NonBlockingGenericDialog {
 	ImagePlus imp_;
 	Rectangle rect_;
 	
 	ImageListener imageListener_;
 	DialogListener dialogListener_;
 
 	private SmNode[][] nodes_ = null;  
 	
 	Integer lastFrame_;
 	Integer nFound_;
 	
 	private volatile Thread prevProcess_ = null;
 	
 	/**
 	 * Constructor that creates the dialog.
 	 * The dialog is non-modal. The analysis result of the current frame will be displayed in the form
 	 * of a PointRoi 
 	 * Parameter changes will trigger update of the analysis of current frame.
 	 * Changes in the image window (e.g., change frame) will trigger update of the analysis. 
 	 * @param imp The image data to be analyzed 
 	 * @param title The title of the dialog
 	 */
 	public ParticleAnalysisDialog(ImagePlus imp, String title) {
 		super(title);
 
 		imp_ = imp;
 		rect_ = imp.getProcessor().getRoi();
 
 		imageListener_ = new ImageListener() {
 
 			@Override
 			public void imageClosed(ImagePlus imp) {
 				if (imp == imp_) {
 					dispose();
 				}
 			}
 
 			@Override
 			public void imageOpened(ImagePlus imp) {
 			}
 
 			@Override
 			public void imageUpdated(ImagePlus imp) {
 				if (imp == imp_) {
 					updateResults();
 				}
 			}
 		};
 
 		ImagePlus.addImageListener(imageListener_);
 		
 		dialogListener_ = new DialogListener() {
 			@Override
 			public boolean dialogItemChanged(GenericDialog dlg, AWTEvent evt) {
 				if (dlg == null) {
 					return true;
 				}
 				
 				if (updateParameters() == true) {
 					updateResults();
 					return true;
 				} else {
 					return false;
 				}
 			}
 		};
 
 		addDialogListener(dialogListener_);
 	}
 
 	/**
 	 * Usually called when the dialog is closed.
 	 * Analyze all frames to detect all particles.
 	 * @return An array of array of SmNode representing all particles. Each SmNode[] represents a frame. 
 	 */
 	public SmNode[][] processAllFrames() {
 		imp_.killRoi();
 		IJ.log("Particle Analysis -- Searching for particles:");
 
 		final ImageStack stack = imp_.getImageStack();
 		
 		nodes_ = new SmNode[stack.getSize()][];
 		
 		lastFrame_ = 0;
 		nFound_ = 0;
 
 		class ProcessThread extends Thread {
 
 			public void run() {
 				int curFrame = 1;
 				do {
 					synchronized(lastFrame_) {
 						if (lastFrame_ < stack.getSize()) {
 							lastFrame_ ++;
 							curFrame = lastFrame_;
 						} else {
 							return;
 						}
 					}
 					if ((curFrame % 50) == 0) {
 						IJ.log("Processed: "+ curFrame + "frames.");
 					}
 					
 					IJ.showProgress(curFrame, stack.getSize());
 					
 					ImageProcessor ip = stack.getProcessor(curFrame);
 					
 					ParticleAnalysis module;
 					
 					try {
 						module = processCurrentFrame(ip);
 					} catch (InterruptedException e) {
 						return;
 					}
 					
 					nodes_[curFrame - 1] = module.createSmNodes(curFrame);
 
 					int nParticles = module.reportNumParticles();
 					
 					synchronized(nFound_) {
 						nFound_ += nParticles;
 					}
 
 				} while(curFrame < stack.getSize());
 			}
 		}
 		
 		ProcessThread [] threads = new ProcessThread[GlobalPrefs.nThread_];
 		
 		for (int i = 0; i < GlobalPrefs.nThread_; i++ ) {
 			threads[i] =  new ProcessThread();
 			threads[i].start();
 		}
 		
 		for (int i = 0; i < GlobalPrefs.nThread_; i++ ) {
 			try {
 				threads[i].join();
 			} catch (InterruptedException e) {
 			}
 		}
 		
 		IJ.log(imp_.getTitle() + "- Found " + nFound_ + " particles.");
 		
 		return nodes_;
 	}
 
 	/**
 	 * Update the analysis of the current frame to display the PointRoi.
 	 * The method starts a new analysis thread and returns immediately. Any new calls to the method 
 	 * cancels previous analysis thread.  
 	 */
 	void updateResults() {
 		if (imp_ == null) {
 			return;
 		}
 
 		class CurrentProcessThread extends Thread {
 			@Override
 			public void run() {
 				ParticleAnalysis module;
 				try {
 					module = processCurrentFrame(imp_.getProcessor());
 				} catch (InterruptedException e) {
 					return;
 				}
 
 				int nParticles = module.reportNumParticles();
 				if (nParticles <= 0) {
 					imp_.killRoi();
 					return;
 				}
 				
 				PointRoi roi;
 
 				if (nParticles > 0) {
 					double [] x = module.reportX();
 					double [] y = module.reportY();
 					int [] xi = new int[nParticles];
 					int [] yi = new int[nParticles];
 					for (int i = 0; i < nParticles; i ++ ) {
 						xi[i] = (int) x[i];
 						yi[i] = (int) y[i];
 					}
 					roi = new PointRoi(xi, yi, nParticles);
 					
 					imp_.setRoi(roi);
 				} 
 			}
 		}
 		
 		synchronized(this) {
 			if (prevProcess_ != null && prevProcess_.isAlive()) {
 				prevProcess_.interrupt();
 			}
 
 			prevProcess_ = new CurrentProcessThread();
 		}
 		
 		prevProcess_.start();
 	}
 
 	/* (non-Javadoc)
 	 * @see java.awt.Window#dispose()
 	 */
 	@Override 
 	public void dispose() {
 		if (imageListener_ != null) {
 			ImagePlus.removeImageListener(imageListener_);
 			imageListener_ = null;
 		}	
 		super.dispose();
 	}
 	
 	/**
 	 * Get the analysis result
 	 * @return All particles detected. This method must be called after processAllFrames()
 	 */
 	public SmNode[][] getSmNodes() {
 		return nodes_;
 	}	
 	
 	/**
 	 * Analyze current image frame
 	 * @param ip Current image frame
 	 * @return The analysis module used.
 	 * @throws InterruptedException
 	 */
 	abstract public ParticleAnalysis processCurrentFrame(ImageProcessor ip) throws InterruptedException;
 	/**
 	 * Update parameters to reflect changes in dialog input fields
 	 * @return True if the parameters are valid.
 	 */
 	abstract public boolean updateParameters();
 }
