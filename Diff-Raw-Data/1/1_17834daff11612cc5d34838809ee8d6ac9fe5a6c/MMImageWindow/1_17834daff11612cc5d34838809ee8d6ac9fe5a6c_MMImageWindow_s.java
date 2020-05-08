 ///////////////////////////////////////////////////////////////////////////////
 //FILE:          MMImageWindow.java
 //PROJECT:       Micro-Manager
 //SUBSYSTEM:     mmstudio
 //-----------------------------------------------------------------------------
 //
 // AUTHOR:       Nenad Amodaj, nenad@amodaj.com, October 1, 2006
 //
 // COPYRIGHT:    University of California, San Francisco, 2006
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
 //
 // CVS:          $Id$
 //
 package org.micromanager.utils;
 
 import java.awt.Color;
 import java.awt.Panel;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.awt.image.ColorModel;
 import java.util.concurrent.locks.Lock;
 import java.util.prefs.Preferences;
 
 import javax.swing.AbstractButton;
 import javax.swing.JButton;
 import javax.swing.JOptionPane;
 
 import org.micromanager.MMStudioMainFrame;
 
 import com.swtdesigner.SwingResourceManager;
 
 import ij.ImagePlus;
 import ij.WindowManager;
 import ij.gui.ImageWindow;
 import ij.io.FileSaver;
 import ij.measure.Calibration;
 import ij.process.ByteProcessor;
 import ij.process.ColorProcessor;
 import ij.process.ImageProcessor;
 import ij.process.ShortProcessor;
 import mmcorej.CMMCore;
 
 /**
  * ImageJ compatible image window. Derived from the original ImageJ class.
  */
 public class MMImageWindow extends ImageWindow {
 	private static final long serialVersionUID = 1L;
 	private static MMImageWindow imageWin_ = null;
 	private static final String WINDOW_X = "mmimg_y";
 	private static final String WINDOW_Y = "mmimg_x";
 	private static final String WINDOW_WIDTH = "mmimg_width";
 	private static final String WINDOW_HEIGHT = "mmimg_height";
 	private static CMMCore core_ = null;
 	private static String title_ = "Live";
 	private static ColorModel currentColorModel_ = null;
 	private static Lock winAccesslock_;
 	private static Preferences prefs_ = null;
 	private static MMStudioMainFrame gui_ = null;
 	
 	private Panel buttonPanel_;
 	private static ContrastSettings contrastSettings8_ = new ContrastSettings();
 	private static ContrastSettings contrastSettings16_ = new ContrastSettings();;
 	private LUTDialog contrastDlg_;
 	private ImageController contrastPanel_ = null;
 
 	public MMImageWindow(ImagePlus imp, CMMCore core) throws Exception {
 		super(imp);
 		core_ = core;
 		Initialize();
 	}
 
 	public MMImageWindow(CMMCore core, ImageController contrastPanel)
 			throws Exception {
 		super(createImagePlus(core_ = core, title_));
 		contrastPanel_ = contrastPanel;
 		core_ = core;
 		Initialize();
 	}
 
 	public MMImageWindow(CMMCore core, MMStudioMainFrame gui, ImageController contrastPanel)
 			throws Exception {
 		super(createImagePlus(core_ = core, title_));
 		contrastPanel_ = contrastPanel;
 		gui_ = gui;
 		core_ = core;
 		Initialize();
 	}
 	
 	public MMImageWindow(CMMCore core, ImageController contrastPanel,
 			String wndTitle) throws Exception {
 		super(createImagePlus(core_ = core, title_ = wndTitle));
 		contrastPanel_ = contrastPanel;
 		core_ = core;
 		Initialize();
 	}
 
 	public static void setContrastSettings(ContrastSettings s8,
 			ContrastSettings s16) {
 		contrastSettings8_ = s8;
 		contrastSettings16_ = s16;
 	}
 
 	public static void setContrastSettings8(ContrastSettings s8) {
 		contrastSettings16_ = s8;
 	}
 
 	public static void setContrastSetting16(ContrastSettings s16) {
 		contrastSettings16_ = s16;
 	}
 
 	public ContrastSettings getCurrentContrastSettings() {
 		if (getImagePlus().getBitDepth() == 8)
 			return contrastSettings8_;
 		else
 			return contrastSettings16_;
 	}
 
 	public static ContrastSettings getContrastSettings8() {
 		return contrastSettings8_;
 	}
 
 	public static ContrastSettings getContrastSettings16() {
 		return contrastSettings16_;
 	}
 
 	public void loadPosition(int x, int y) {
 		if (prefs_ != null)
 			setLocation(prefs_.getInt(WINDOW_X, x), prefs_.getInt(WINDOW_Y, y));
 	}
 
 	public void savePosition() {
 		if (prefs_ == null)
 			loadPreferences();
 		Rectangle r = getBounds();
 		// save window position
 		prefs_.putInt(WINDOW_X, r.x);
 		prefs_.putInt(WINDOW_Y, r.y);
 		prefs_.putInt(WINDOW_WIDTH, r.width);
 		prefs_.putInt(WINDOW_HEIGHT, r.height);
 	}
 
 	private static ImagePlus createImagePlus(CMMCore core, String wndTitle)
 			throws Exception {
 		ImageProcessor ip;
 		long byteDepth = core_.getBytesPerPixel();
 		long channels = core_.getNumberOfChannels();
 		int width = (int) core_.getImageWidth();
 		int height = (int) core_.getImageHeight();
 		if (byteDepth == 0) {
 			throw (new Exception(logError("Imaging device not initialized")));
 		}
 		if (byteDepth == 1 && channels == 1) {
 			ip = new ByteProcessor(width, height);
 			if (contrastSettings8_.getRange() == 0.0)
 				ip.setMinAndMax(0, 255);
 			else
 				ip.setMinAndMax(contrastSettings8_.min, contrastSettings8_.max);
 		} else if (byteDepth == 2 && channels == 1) {
 			ip = new ShortProcessor(width, height);
 			if (contrastSettings16_.getRange() == 0.0)
 				ip.setMinAndMax(0, 65535);
 			else
 				ip.setMinAndMax(contrastSettings16_.min,
 						contrastSettings16_.max);
 		} else if (byteDepth == 4 && channels == 1) {
 			// assuming RGB32 format
 			ip = new ColorProcessor(width, height);
 			if (contrastSettings8_.getRange() == 0.0)
 				ip.setMinAndMax(0, 255);
 			else
 				ip.setMinAndMax(contrastSettings8_.min, contrastSettings8_.max);
 		} else if (byteDepth == 1 && channels == 4) {
 			// assuming RGB32 format
 			ip = new ColorProcessor(width, height);
 			if (contrastSettings8_.getRange() == 0.0)
 				ip.setMinAndMax(0, 255);
 			else
 				ip.setMinAndMax(contrastSettings8_.min, contrastSettings8_.max);
 		} else {
 			String message = "Unsupported pixel depth: "
 					+ core_.getBytesPerPixel() + " byte(s) and " + channels
 					+ " channel(s).";
 			throw (new Exception(logError(message)));
 		}
 		ip.setColor(Color.black);
 		if (currentColorModel_ != null) {
 			ip.setColorModel(currentColorModel_);
 			logError("Restoring color model:" + currentColorModel_.toString());
 		}
 		ip.fill();
 		return new ImagePlus(title_ = wndTitle, ip);
 	}
 
 	public void Initialize() {
 
 		setIJCal();
 		setPreferredLocation();
 
 		buttonPanel_ = new Panel();
 
 		AbstractButton saveButton = new JButton("Save");
 		saveButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				new FileSaver(getImagePlus()).save();
 			}
 		});
 		buttonPanel_.add(saveButton);
 
 		AbstractButton saveAsButton = new JButton("Save As...");
 		saveAsButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				new FileSaver(getImagePlus()).saveAsTiff();
 			}
 		});
 		buttonPanel_.add(saveAsButton);
 
 		
 
 		
 		AbstractButton addToSeriesButton = new JButton("Add to Series");
 		addToSeriesButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				try {
 					gui_.addToSnapSeries(getImagePlus().getProcessor().getPixels());
 			
 				} catch (Exception e2) {
 					e2.printStackTrace(System.err);
 				}
 			}
 		});
 		buttonPanel_.add(addToSeriesButton);
 		
 		
 		add(buttonPanel_);
 		pack();
 
 		// add window listeners
 		addWindowListener(new WindowAdapter() {
 			public void windowClosing(WindowEvent e) {
 				saveAttributes();
				WindowManager.removeWindow(getImagePlus().getWindow());
 			}
 		});
 		addWindowListener(new WindowAdapter() {
 			public void windowClosed(WindowEvent e) {
 			}
 		});
 
 		addWindowListener(new WindowAdapter() {
 			public void windowOpened(WindowEvent e) {
 				getCanvas().requestFocus();
 			}
 		});
 
 		addWindowListener(new WindowAdapter() {
 			public void windowGainedFocus(WindowEvent e) {
 				updateHistogram();
 			}
 		});
 
 		addWindowListener(new WindowAdapter() {
 			public void windowActivated(WindowEvent e) {
 				updateHistogram();
 			}
 		});
 		setIconImage(SwingResourceManager.getImage(MMStudioMainFrame.class,
 				"/org/micromanager/icons/camera_go.png"));
 
 		setIJCal();
 	}
 
 	public void saveAttributes()
 	{
 		if (contrastDlg_ != null)
 			contrastDlg_.dispose();
 		savePosition();
 		// ToDo: implement winAccesslock_;
 		// remember LUT so that a new window can be opened with the
 		// same LUT
 		if (getImagePlus().getProcessor().isPseudoColorLut()) {
 			currentColorModel_ = getImagePlus().getProcessor()
 					.getColorModel();
 			logError("Storing color model:"
 					+ currentColorModel_.toString());
 
 		}
 
 		if (contrastPanel_ != null)
 			contrastPanel_.setImagePlus(null);
 		
 	}
 	public void windowOpened(WindowEvent e) {
 		getCanvas().requestFocus();
 	}
 
 	private void loadPreferences() {
 		prefs_ = Preferences.userNodeForPackage(this.getClass());
 	}
 
 	public void setFirstInstanceLocation() {
 		setLocationRelativeTo(getParent());
 	}
 
 	public void setPreferredLocation() {
 		loadPreferences();
 		Point p = getLocation();
 		loadPosition(p.x, p.y);
 	}
 
 	private static String logError(String message) {
 		MMLogger.getLogger().info("MMImageWindow:" + message);
 		return message;
 	}
 
 
 	protected void updateHistogram() {
 		if (contrastPanel_ != null) {
 			contrastPanel_.setImagePlus(getImagePlus());
 			contrastPanel_.setContrastSettings(contrastSettings8_,
 					contrastSettings16_);
 			contrastPanel_.update();
 		}
 	}
 
 	public long getImageWindowByteLength() {
 		long imgWinByteLength;
 		ImageProcessor ip = getImagePlus().getProcessor();
 		int w = ip.getWidth();
 		int h = ip.getHeight();
 		int bitDepth = getImagePlus().getBitDepth();
 		// ImageWindow returns bitdepth 24 when Image processor type is Color
 		bitDepth = bitDepth == 24 ? 32 : bitDepth;
 		return imgWinByteLength = w * h * bitDepth / 8;
 	}
 
 	public long imageByteLenth(Object pixels) throws IllegalArgumentException {
 		int byteLength = 0;
 		if (pixels instanceof byte[]) {
 			byte bytePixels[] = (byte[]) pixels;
 			byteLength = bytePixels.length;
 		} else if (pixels instanceof short[]) {
 			short bytePixels[] = (short[]) pixels;
 			byteLength = bytePixels.length * 2;
 		} else if (pixels instanceof int[]) {
 			int bytePixels[] = (int[]) pixels;
 			byteLength = bytePixels.length * 4;
 		} else
 			throw (new IllegalArgumentException("Image bytelenth does not much"));
 		return byteLength;
 	}
 
 	public boolean windowNeedsResizing() {
 		long channels = core_.getNumberOfChannels();
 		long bpp = core_.getBytesPerPixel();
 		ImageProcessor ip = getImagePlus().getProcessor();
 		int w = ip.getWidth();
 		int h = ip.getHeight();
 		int bitDepth = getImagePlus().getBitDepth();
 		// ImageWindow returns bitdepth 24 when Image processor type is Color
 		bitDepth = bitDepth == 24 ? 32 : bitDepth;
 		long imgWinByteLength = w * h * bitDepth / 8;
 
 		// warn the user if image dimensions do not match the current window
 		boolean ret = w != core_.getImageWidth() || h != core_.getImageHeight()
 				|| bitDepth != core_.getBytesPerPixel() * 8 * channels;
 		return ret;
 	}
 
 	// public
 	public void newImage(Object img) {
 
 		if (getImageWindowByteLength() != imageByteLenth(img)) {
 			throw (new RuntimeException("Image bytelenth does not much"));
 		}
 		getImagePlus().getProcessor().setPixels(img);
 		getImagePlus().updateAndDraw();
 		// Graphics
 		// getCanvas().paint(getCanvas().getGraphics());
 		updateHistogram();
 		// update coordinate and pixel info in imageJ by simulating mouse
 		// move
 		Point pt = getCanvas().getCursorLoc();
 		getImagePlus().mouseMoved(pt.x, pt.y);
 	}
 
 	// Set ImageJ pixel calibration
 	public void setIJCal() {
 		double pixSizeUm = core_.getPixelSizeUm();
 		Calibration cal = new Calibration();
 		if (pixSizeUm > 0) {
 			cal.setUnit("um");
 			cal.pixelWidth = pixSizeUm;
 			cal.pixelHeight = pixSizeUm;
 		}
 		getImagePlus().setCalibration(cal);
 	}
 
 	public long getRawHistogramSize() {
 		long ret = 0;
 		int rawHistogram[] = getImagePlus().getProcessor().getHistogram();
 		if (rawHistogram != null) {
 			ret = rawHistogram.length;
 		}
 		return ret;
 	}
 
 }
