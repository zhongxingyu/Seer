 /*
  * Copyright 2010, 2011 Institut Pasteur.
  * 
  * This file is part of Mask Editor, which is an ICY plugin.
  * 
  * Mask Editor is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * Mask Editor is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with Mask Editor. If not, see <http://www.gnu.org/licenses/>.
  */
 package plugins.nherve.maskeditor;
 
 import icy.file.Saver;
 import icy.gui.component.ComponentUtil;
 import icy.gui.main.MainEvent;
 import icy.gui.util.GuiUtil;
 import icy.image.IcyBufferedImage;
 import icy.main.Icy;
 import icy.plugin.PluginDescriptor;
 import icy.plugin.PluginLauncher;
 import icy.plugin.PluginLoader;
 import icy.preferences.XMLPreferences;
 import icy.roi.ROI;
 import icy.roi.ROI2D;
 import icy.sequence.Sequence;
 import icy.swimmingPool.SwimmingObject;
 import icy.swimmingPool.SwimmingPool;
 import icy.swimmingPool.SwimmingPoolEvent;
 import icy.swimmingPool.SwimmingPoolListener;
 import icy.system.thread.ThreadUtil;
 
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Graphics2D;
 import java.awt.Shape;
 import java.awt.datatransfer.DataFlavor;
 import java.awt.datatransfer.Transferable;
 import java.awt.datatransfer.UnsupportedFlavorException;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.awt.geom.Ellipse2D;
 import java.awt.geom.Point2D;
 import java.awt.geom.Rectangle2D;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.ButtonGroup;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSlider;
 import javax.swing.JTextField;
 import javax.swing.TransferHandler;
 import javax.swing.border.TitledBorder;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import loci.formats.FormatException;
 import plugins.nherve.toolbox.NherveToolbox;
 import plugins.nherve.toolbox.PersistenceException;
 import plugins.nherve.toolbox.image.DifferentColorsMap;
 import plugins.nherve.toolbox.image.mask.Mask;
 import plugins.nherve.toolbox.image.mask.MaskException;
 import plugins.nherve.toolbox.image.mask.MaskListener;
 import plugins.nherve.toolbox.image.mask.MaskPersistence;
 import plugins.nherve.toolbox.image.mask.MaskStack;
 import plugins.nherve.toolbox.image.mask.OptimizedMaskPersistenceImpl;
 import plugins.nherve.toolbox.plugin.BackupAndPainterManagerSingletonPlugin;
 import plugins.nherve.toolbox.plugin.HelpWindow;
 import plugins.nherve.toolbox.plugin.PluginHelper;
 
 /**
  * The Class MaskEditor.
  * 
  * @author Nicolas HERVE - nicolas.herve@pasteur.fr
  */
 public class MaskEditor extends BackupAndPainterManagerSingletonPlugin<MaskStack, MaskEditorPainter> implements ItemListener, ActionListener, ChangeListener, SwimmingPoolListener, MaskListener, Iterable<MaskLayer> {
 
 	private class BackgroundPanelTransferHandler extends TransferHandler {
 		private static final long serialVersionUID = -307871237679751409L;
 
 		@Override
 		public boolean canImport(TransferSupport support) {
 			if (!support.isDataFlavorSupported(localFlavor)) {
 				return false;
 			}
 			return true;
 		}
 
 		@Override
 		public boolean importData(TransferSupport support) {
 			if (!canImport(support)) {
 				return false;
 			}
 
 			try {
 				Transferable tf = support.getTransferable();
 				String s = (String) tf.getTransferData(localFlavor);
 				MaskLayer toMove = getLayerById(Integer.parseInt(s));
 				getStack().moveBottom(toMove.getMask());
 
 				return true;
 			} catch (UnsupportedFlavorException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 
 			return false;
 		}
 	}
 
 	private static DataFlavor localFlavor = null;
 
 	/** The Constant SHAPE_SQUARE. */
 	private final static String SHAPE_SQUARE = "Square";
 
 	/** The Constant SHAPE_CIRCLE. */
 	private final static String SHAPE_CIRCLE = "Circle";
 
 	/** The Constant SHAPE_VERTICAL_LINE. */
 	private final static String SHAPE_VERTICAL_LINE = "Vertical line";
 
 	/** The Constant SHAPE_HORIZONTAL_LINE. */
 	private final static String SHAPE_HORIZONTAL_LINE = "Horizontal line";
 
 	/** The Constant NB_COLOR_CYCLE. */
 	public final static int NB_COLOR_CYCLE = 3;
 
	private static String HELP = "<html>" + "<p align=\"center\"><b>" + HelpWindow.TAG_FULL_PLUGIN_NAME + "</b></p>" + "<p align=\"center\"><b>" + NherveToolbox.DEV_NAME_HTML + "</b></p>" + "<p align=\"center\"><a href=\"http://www.herve.name/pmwiki.php/Main/MaskEditor\">Online help is available</a></p>" + "<p align=\"center\"><b>" + NherveToolbox.COPYRIGHT_HTML + "</b></p>" + "<hr/>" + "<p>On each opened sequence, you can use the following keys : </p>" + "<table>" + "<tr><td align=\"center\"><b>D</b></td><td>activate / deactivate the drawing tool</td></tr>" + "<tr><td align=\"center\"><b>Click</b></td><td>add to current mask</td></tr>" + "<tr><td align=\"center\"><b>SHIFT + Click</b></td><td>substract from current mask</td></tr>" + "<tr><td align=\"center\"><b>CTRL + Click</b></td><td>fill hole in current mask</td></tr>" + "<tr><td></td></tr>" + "<tr><td align=\"center\"><b>W</b></td><td>show / hide the active mask</td></tr>" + "<tr><td align=\"center\"><b>CTRL + [0..9]</b></td><td>show / hide the corresponding mask</td></tr>" + "<tr><td align=\"center\"><b>X</b></td><td>show / hide all masks</i> tool</td></tr>" + "<tr><td align=\"center\"><b>C</b></td><td>decrease the draw tool size</td></tr>" + "<tr><td align=\"center\"><b>CTRL + C</b></td><td>slightly decrease the draw tool size</td></tr>" + "<tr><td align=\"center\"><b>V</b></td><td>increase the draw tool size</td></tr>" + "<tr><td align=\"center\"><b>CTRL + V</b></td><td>slightly increase the draw tool size</td></tr>" + "</table>" + "<hr/>" + "<p>" + HelpWindow.TAG_PLUGIN_NAME + NherveToolbox.LICENCE_HTML + "</p>" + "<p>" + NherveToolbox.LICENCE_HTMLLINK + "</p>" + "</html>";
 
 	/**
 	 * Gets the running instance.
 	 * 
 	 * @param forceStart
 	 *            the force start
 	 * @return the running instance
 	 */
 	public static MaskEditor getRunningInstance(boolean forceStart) {
 		MaskEditor singleton = (MaskEditor) getInstance(MaskEditor.class);
 		if (forceStart && (singleton == null)) {
 			PluginDescriptor pd = PluginLoader.getPlugin(MaskEditor.class.getName());
 			PluginLauncher.launch(pd);
 			singleton = (MaskEditor) getInstance(MaskEditor.class);
 		}
 		return singleton;
 	}
 
 	/**
 	 * Sets the segmentation.
 	 * 
 	 * @param seq
 	 *            the seq
 	 * @param ms
 	 *            the ms
 	 */
 	public static void setSegmentation(final Sequence seq, final MaskStack ms) {
 		final MaskEditor me = getRunningInstance(true);
 		ThreadUtil.invokeLater(new Runnable() {
 			@Override
 			public void run() {
 				me.setSegmentationForSequence(seq, ms);
 			}
 		});
 	}
 
 	/** The cbx cursor shape. */
 	private JComboBox cbxCursorShape;
 
 	/** The sl cursor size. */
 	private JSlider slCursorSize;
 
 	/** The sl opacity. */
 	private JSlider slOpacity;
 
 	/** The backup opacity. */
 	private int backupOpacity;
 
 	/** The bt load. */
 	private JButton btLoad;
 
 	/** The bt save. */
 	private JButton btSave;
 
 	/** The bt send sp global. */
 	private JButton btSendSPGlobal;
 
 	/** The bt get sp global. */
 	private JButton btGetSPGlobal;
 
 	/** The bt real colors. */
 	private JButton btRealColors;
 
 	/** The bt artificial colors. */
 	private JButton btArtificialColors;
 
 	/** The bt save full image. */
 	private JButton btSaveFullImage;
 
 	/** The bt help. */
 	private JButton btHelp;
 
 	/** The view bgd box. */
 	private JCheckBox cbViewBgdBox;
 
 	/** The black white. */
 	private JCheckBox cbBlackWhite;
 
 	/** The cb only contours. */
 	private JCheckBox cbOnlyContours;
 
 	/** The cb draw enabled. */
 	private JCheckBox cbDrawEnabled;
 
 	/** The mll. */
 	private JPanel mll;
 
 	/** The color map. */
 	private DifferentColorsMap colorMap;
 
 	/** The stack. */
 	private MaskStack stack;
 
 	/** The bt duplicate mask. */
 	private JButton btDuplicateMask;
 
 	/** The bt add mask. */
 	private JButton btAddMask;
 
 	/** The bt add mask sp. */
 	private JButton btAddMaskSP;
 
 	/** The bt group. */
 	private ButtonGroup btGroup;
 
 	/** The bt erode. */
 	private JButton btErode;
 
 	/** The bt dilate. */
 	private JButton btDilate;
 
 	/** The bt invert. */
 	private JButton btInvert;
 
 	/** The bt fill holes. */
 	private JButton btFillHoles;
 
 	/** The bt filter size. */
 	private JButton btFilterSize;
 
 	private JButton btFromROI;
 
 	/** The tf filter size. */
 	private JTextField tfFilterSize;
 
 	/** The bt compare. */
 	private JButton btCompare;
 
 	private JScrollPane scroll;
 
 	private JPanel mlp;
 
 	/** The layers. */
 	private ArrayList<MaskLayer> layers;
 
 	private final static int COLORMAP_NBCOLORS = 10;
 
 	/**
 	 * Instantiates a new mask editor.
 	 */
 	public MaskEditor() {
 		super();
 
 		if (localFlavor == null) {
 			ClassLoader backup = Thread.currentThread().getContextClassLoader();
 			Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
 			try {
 				localFlavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + ";class=" + MaskLayer.class.getName());
 			} catch (ClassNotFoundException e) {
 				e.printStackTrace();
 			}
 			Thread.currentThread().setContextClassLoader(backup);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
 	 */
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		Object o = e.getSource();
 
 		if (o == null) {
 			return;
 		}
 
 		if (o instanceof JButton) {
 			JButton b = (JButton) e.getSource();
 
 			if (b == btAddMask) {
 				try {
 					reInitColorMap();
 					stack.createNewMask(MaskStack.MASK_DEFAULT_LABEL, true, colorMap, getGlobalOpacity());
 				} catch (MaskException e1) {
 					logError(e1.getClass().getName() + " : " + e1.getMessage());
 				}
 			}
 
 			if (b == btCompare) {
 				new SegmentCompareWindow(stack);
 			}
 
 			if (b == btDuplicateMask) {
 				try {
 					reInitColorMap();
 					stack.copyCurrentMask(colorMap);
 				} catch (MaskException e1) {
 					logError(e1.getClass().getName() + " : " + e1.getMessage());
 				}
 			}
 
 			if (b == btFromROI) {
 				try {
 					reInitColorMap();
 					Mask m = stack.createNewMask(MaskStack.MASK_DEFAULT_LABEL, true, colorMap, getGlobalOpacity());
 					for (ROI2D roi : getCurrentSequence().getROI2Ds()) {
 						m.add(roi);
 					}
 				} catch (MaskException e1) {
 					logError(e1.getClass().getName() + " : " + e1.getMessage());
 				}
 			}
 
 			if (b == btAddMaskSP) {
 				try {
 					Mask m = null;
 					do {
 						m = getMaskFromPool();
 						if (m != null) {
 							reInitColorMap();
 							stack.addExternalMask(m, colorMap);
 							m.setOpacity(getGlobalOpacity());
 						}
 					} while (m != null);
 				} catch (MaskException e1) {
 					logError(e1.getClass().getName() + " : " + e1.getMessage());
 				}
 			}
 
 			if (b == btErode) {
 				try {
 					Mask m = stack.getActiveMask();
 					m.erode();
 				} catch (MaskException e1) {
 					logError(e1.getClass().getName() + " : " + e1.getMessage());
 				}
 				getCurrentSequence().painterChanged(null);
 			}
 
 			if (b == btDilate) {
 				try {
 					Mask m = stack.getActiveMask();
 					m.dilate();
 				} catch (MaskException e1) {
 					logError(e1.getClass().getName() + " : " + e1.getMessage());
 				}
 				getCurrentSequence().painterChanged(null);
 			}
 
 			if (b == btInvert) {
 				try {
 					Mask m = stack.getActiveMask();
 					m.invert();
 				} catch (MaskException e1) {
 					logError(e1.getClass().getName() + " : " + e1.getMessage());
 				}
 				getCurrentSequence().painterChanged(null);
 			}
 
 			if (b == btFillHoles) {
 				try {
 					Mask m = stack.getActiveMask();
 					m.fillHoles();
 				} catch (MaskException e1) {
 					logError(e1.getClass().getName() + " : " + e1.getMessage());
 				}
 				getCurrentSequence().painterChanged(null);
 			}
 
 			if (b == btFilterSize) {
 				try {
 					Mask m = stack.getActiveMask();
 					m.filterSize(Integer.parseInt(tfFilterSize.getText()));
 				} catch (MaskException e1) {
 					logError(e1.getClass().getName() + " : " + e1.getMessage());
 				} catch (NumberFormatException e2) {
 					logError(e2.getClass().getName() + " : " + e2.getMessage());
 				}
 				getCurrentSequence().painterChanged(null);
 			}
 
 			if (b == btHelp) {
 				openHelpWindow(HELP, 400, 600);
 				return;
 			}
 
 			if (b == btRealColors) {
 				getStack().reInitColors(getCurrentSequence().getFirstImage());
 				return;
 			}
 
 			if (b == btArtificialColors) {
 				getStack().reInitColors(colorMap);
 				return;
 			}
 
 			if (b == btSaveFullImage) {
 				File f = displayTiffExport();
 				if (f != null) {
 					try {
 						Sequence s = getCurrentSequence();
 						BufferedImage localCache = new BufferedImage(s.getWidth(), s.getHeight(), BufferedImage.TYPE_INT_ARGB);
 						Graphics2D g2 = localCache.createGraphics();
 						g2.drawImage(s.getFirstImage(), null, 0, 0);
 						getCurrentSequencePainter().paint(g2, s, null);
 						Saver.saveImage(IcyBufferedImage.createFrom(localCache), f, true);
 					} catch (IOException e1) {
 						e1.printStackTrace();
 					} catch (FormatException e1) {
 						e1.printStackTrace();
 					}
 				}
 			}
 
 			if (b == btGetSPGlobal) {
 				MaskStack s = null;
 				s = getStackFromPool();
 				setSegmentationForSequence(getCurrentSequence(), s);
 				return;
 			}
 
 			if (b == btSendSPGlobal) {
 				MaskStack s = getStack();
 				SwimmingObject result = new SwimmingObject(s);
 				Icy.getMainInterface().getSwimmingPool().add(result);
 				return;
 			}
 
 			if (b == btLoad) {
 				try {
 					MaskPersistence rep = new OptimizedMaskPersistenceImpl();
 					File f = displaySegmentationExport(rep, null);
 					if (f != null) {
 						loadForCurrentSequence(f, rep);
 					}
 
 				} catch (PersistenceException e1) {
 					logError(e1.getClass().getName() + " : " + e1.getMessage());
 				}
 				return;
 			}
 
 			if (b == btSave) {
 				try {
 					MaskPersistence rep = new OptimizedMaskPersistenceImpl();
 					String d = getCurrentSequence().getFilename();
 					File df = rep.getMaskFileFor(new File(d));
 					File f = displaySegmentationExport(rep, df);
 					if (f != null) {
 						rep.save(getStack(), f);
 					}
 				} catch (PersistenceException e1) {
 					logError(e1.getClass().getName() + " : " + e1.getMessage());
 				}
 				return;
 			}
 		}
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * plugins.nherve.toolbox.plugin.BackupSingletonPlugin#backupCurrentSequence
 	 * ()
 	 */
 	@Override
 	public void backupCurrentSequence() {
 		if (!hasBackupObject()) {
 			Sequence s = getCurrentSequence();
 			MaskStack stack = new MaskStack(s.getWidth(), s.getHeight());
 			try {
 				reInitColorMap();
 				stack.createNewMask(MaskStack.MASK_DEFAULT_LABEL, true, colorMap.get(0), getGlobalOpacity());
 			} catch (MaskException e) {
 				logError(e.getClass().getName() + " : " + e.getMessage());
 			}
 			addBackupObject(stack);
 		}
 	}
 
 	/**
 	 * Builds the cursor shape.
 	 * 
 	 * @param p
 	 *            the p
 	 * @return the shape
 	 */
 	public Shape buildCursorShape(Point2D p) {
 		return buildCursorShape(p, 1);
 	}
 
 	/**
 	 * Builds the cursor shape.
 	 * 
 	 * @param p
 	 *            the p
 	 * @param mult
 	 *            the mult
 	 * @return the shape
 	 */
 	public Shape buildCursorShape(Point2D p, float mult) {
 		Shape shape = null;
 		int s = (int) (slCursorSize.getValue() * mult);
 		int s1 = s / 2;
 		if (cbxCursorShape.getSelectedItem().equals(SHAPE_CIRCLE)) {
 			shape = new Ellipse2D.Double(p.getX() - s1, p.getY() - s1, s, s);
 		} else if (cbxCursorShape.getSelectedItem().equals(SHAPE_SQUARE)) {
 			shape = new Rectangle2D.Double(p.getX() - s1, p.getY() - s1, s, s);
 		} else if (cbxCursorShape.getSelectedItem().equals(SHAPE_HORIZONTAL_LINE)) {
 			shape = new Rectangle2D.Double(p.getX() - s1, p.getY(), s, 1);
 		} else if (cbxCursorShape.getSelectedItem().equals(SHAPE_VERTICAL_LINE)) {
 			shape = new Rectangle2D.Double(p.getX(), p.getY() - s1, 1, s);
 		}
 
 		return shape;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see plugins.nherve.toolbox.plugin.PainterFactory#createNewPainter()
 	 */
 	@Override
 	public MaskEditorPainter createNewPainter() {
 		MaskEditorPainter painter = new MaskEditorPainter();
 		painter.setEditor(this);
 		Sequence currentSequence = getCurrentSequence();
 		painter.setSequence(currentSequence);
 		return painter;
 	}
 
 	private File displaySegmentationExport(MaskPersistence rep, File df) {
 		XMLPreferences preferences = getPreferences().node("loadsave");
 		File f = PluginHelper.fileChooser(preferences, rep, df);
 		return f;
 	}
 
 	/**
 	 * Display tiff export.
 	 * 
 	 * @return the file
 	 */
 	File displayTiffExport() {
 		return PluginHelper.fileChooser(".tif", "TIFF files (*.tif)", getPreferences().node("tiffexport"), "Choose TIFF file");
 	}
 
 	int getBackupOpacity() {
 		return backupOpacity;
 	}
 
 	JCheckBox getCbBlackWhite() {
 		return cbBlackWhite;
 	}
 
 	JCheckBox getCbDrawEnabled() {
 		return cbDrawEnabled;
 	}
 
 	JCheckBox getCbViewBgdBox() {
 		return cbViewBgdBox;
 	}
 
 //	public Container getFrame() {
 //		return frame.getFrame();
 //	}
 
 	public float getGlobalOpacity() {
 		return slOpacity.getValue() / 100f;
 	}
 
 	/**
 	 * Gets the layer by id.
 	 * 
 	 * @param id
 	 *            the id
 	 * @return the layer by id
 	 */
 	public MaskLayer getLayerById(int id) {
 		for (MaskLayer l : layers) {
 			if (l.getMask().getId() == id) {
 				return l;
 			}
 		}
 		return null;
 	}
 
 	DataFlavor getLocalFlavor() {
 		return localFlavor;
 	}
 
 	/**
 	 * Gets the from pool.
 	 * 
 	 * @return the from pool
 	 */
 	private Mask getMaskFromPool() {
 		SwimmingPool sp = Icy.getMainInterface().getSwimmingPool();
 		SwimmingObject res = null;
 		for (SwimmingObject r : sp.getObjects()) {
 			if (r.getObject() instanceof Mask) {
 				res = r;
 				break;
 			}
 		}
 
 		Mask m = null;
 		if (res != null) {
 			m = (Mask) res.getObject();
 			sp.remove(res);
 		}
 		return m;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see plugins.nherve.toolbox.plugin.PainterManager#getPainterName()
 	 */
 	@Override
 	public String getPainterName() {
 		return MaskEditorPainter.class.getName();
 	}
 
 	JSlider getSlCursorSize() {
 		return slCursorSize;
 	}
 
 	JSlider getSlOpacity() {
 		return slOpacity;
 	}
 
 	/**
 	 * Gets the stack.
 	 * 
 	 * @return the stack
 	 */
 	public MaskStack getStack() {
 		return stack;
 	}
 
 	/**
 	 * Gets the from pool.
 	 * 
 	 * @return the from pool
 	 */
 	private MaskStack getStackFromPool() {
 		SwimmingPool sp = Icy.getMainInterface().getSwimmingPool();
 		SwimmingObject res = null;
 		for (SwimmingObject r : sp.getObjects()) {
 			if (r.getObject() instanceof MaskStack) {
 				res = r;
 				break;
 			}
 		}
 
 		MaskStack m = null;
 		if (res != null) {
 			m = (MaskStack) res.getObject();
 			sp.remove(res);
 		}
 		return m;
 	}
 
 	/**
 	 * Checks if is mask in pool.
 	 * 
 	 * @return true, if is mask in pool
 	 */
 	private boolean isMaskInPool() {
 		SwimmingPool sp = Icy.getMainInterface().getSwimmingPool();
 		for (SwimmingObject r : sp.getObjects()) {
 			if (r.getObject() instanceof Mask) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Checks if is segmentation in pool.
 	 * 
 	 * @return true, if is segmentation in pool
 	 */
 	private boolean isSegmentationInPool() {
 		SwimmingPool sp = Icy.getMainInterface().getSwimmingPool();
 		for (SwimmingObject r : sp.getObjects()) {
 			if (r.getObject() instanceof MaskStack) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
 	 */
 	@Override
 	public void itemStateChanged(ItemEvent e) {
 		Object o = e.getSource();
 
 		if (o == null) {
 			return;
 		}
 
 		if (o instanceof JCheckBox) {
 			JCheckBox c = (JCheckBox) e.getSource();
 
 			if (c == cbDrawEnabled) {
 				getCurrentSequence().painterChanged(null);
 			} else if (c == cbOnlyContours) {
 				for (MaskLayer ml : this) {
 					ml.getMask().setDrawOnlyContours(cbOnlyContours.isSelected());
 				}
 				getCurrentSequence().painterChanged(null);
 			} else if (c == cbViewBgdBox) {
 				getCurrentSequence().painterChanged(null);
 			} else if (c == cbBlackWhite) {
 				getCurrentSequence().painterChanged(null);
 			}
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Iterable#iterator()
 	 */
 	@Override
 	public Iterator<MaskLayer> iterator() {
 		return layers.iterator();
 	}
 
 	public void loadForCurrentSequence(File f, MaskPersistence rep) throws PersistenceException {
 		MaskStack s = rep.loadMaskStack(f);
 		s.checkAfterLoad((float) slOpacity.getValue() / 100f, getCurrentSequence().getFirstImage());
 		removeBackupObject(getCurrentSequence());
 		addBackupObject(s);
 		setStack(s);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * plugins.nherve.toolbox.plugin.SingletonPlugin#pluginClosed(icy.gui.main
 	 * .MainEvent)
 	 */
 	@Override
 	public void pluginClosed(MainEvent arg0) {
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * plugins.nherve.toolbox.plugin.SingletonPlugin#pluginOpened(icy.gui.main
 	 * .MainEvent)
 	 */
 	@Override
 	public void pluginOpened(MainEvent arg0) {
 
 	}
 
 	/**
 	 * Refresh interface.
 	 */
 	public void refreshInterface() {
 		reInitColorMap();
 		refreshMLLInterface();
 		Sequence s = getCurrentSequence();
 		if (s != null) {
 			s.painterChanged(null);
 		}
 	}
 
 	/**
 	 * Refresh interface.
 	 */
 	public void refreshMLLInterface() {
 		// for (MaskLayer ml : layers) {
 		// mlp.remove(ml);
 		// }
 		mlp.removeAll();
 		layers.clear();
 		btGroup = new ButtonGroup();
 
 		if (stack == null) {
 			// need stack
 			btAddMask.setEnabled(false);
 			btLoad.setEnabled(false);
 		}
 
 		if ((stack == null) || (stack.size() < 1)) {
 			// need 1 mask
 			btSave.setEnabled(false);
 			btSaveFullImage.setEnabled(false);
 			btSendSPGlobal.setEnabled(false);
 			btDuplicateMask.setEnabled(false);
 			cbxCursorShape.setEnabled(false);
 			slCursorSize.setEnabled(false);
 			slOpacity.setEnabled(false);
 			cbDrawEnabled.setEnabled(false);
 			cbOnlyContours.setEnabled(false);
 			cbViewBgdBox.setEnabled(false);
 			cbBlackWhite.setEnabled(false);
 			btRealColors.setEnabled(false);
 			btArtificialColors.setEnabled(false);
 			btErode.setEnabled(false);
 			btDilate.setEnabled(false);
 			btInvert.setEnabled(false);
 			btFillHoles.setEnabled(false);
 			btFilterSize.setEnabled(false);
 		}
 
 		if ((stack == null) || (stack.size() < 2)) {
 			// need 2 masks
 			btCompare.setEnabled(false);
 		}
 
 		if (stack != null) {
 			btAddMask.setEnabled(true);
 			btLoad.setEnabled(true);
 
 			if (stack.size() > 0) {
 				btSave.setEnabled(true);
 				btSaveFullImage.setEnabled(true);
 				btSendSPGlobal.setEnabled(true);
 				btDuplicateMask.setEnabled(true);
 				cbxCursorShape.setEnabled(true);
 				slCursorSize.setEnabled(true);
 				slOpacity.setEnabled(true);
 				cbDrawEnabled.setEnabled(true);
 				cbOnlyContours.setEnabled(true);
 				cbViewBgdBox.setEnabled(true);
 				cbBlackWhite.setEnabled(true);
 				btRealColors.setEnabled(true);
 				btArtificialColors.setEnabled(true);
 				btErode.setEnabled(true);
 				btDilate.setEnabled(true);
 				btInvert.setEnabled(true);
 				btFillHoles.setEnabled(true);
 				btFilterSize.setEnabled(true);
 			}
 
 			if (stack.size() > 1) {
 				btCompare.setEnabled(true);
 			}
 
 			MaskLayer al = null;
 			for (int i = stack.size() - 1; i >= 0; i--) {
 				Mask m = stack.getByIndex(i);
 				MaskLayer ml = new MaskLayer(m);
 				ml.setEditor(this);
 				ml.startInterface(btGroup);
 				mlp.add(ml);
 				layers.add(ml);
 				if (stack.getActiveMask() == m) {
 					al = ml;
 				}
 			}
 			if (al != null) {
 				al.setCurrentlyActive();
 			}
 		}
 
 		mlp.revalidate();
 		scroll.revalidate();
 		scroll.repaint();
 
 	}
 
 	private void reInitColorMap() {
 		if (hasCurrentSequence() && (getStack() != null) && (colorMap != null)) {
 			if (getStack().getMaxId() >= (colorMap.getNbColors() - 1)) {
 				int n = 2 + getStack().getMaxId() / COLORMAP_NBCOLORS;
 				colorMap = new DifferentColorsMap(n * COLORMAP_NBCOLORS, n);
 			}
 		}
 
 		if (colorMap == null) {
 			colorMap = new DifferentColorsMap(COLORMAP_NBCOLORS, 1);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * plugins.nherve.toolbox.plugin.BackupSingletonPlugin#restoreCurrentSequence
 	 * (boolean)
 	 */
 	@Override
 	public void restoreCurrentSequence(boolean refresh) {
 	}
 
 	@Override
 	public void roiAdded(MainEvent event) {
 		ROI roi = (ROI) event.getSource();
 		if (roi.getFirstSequence() == getCurrentSequence()) {
 			if (getCurrentSequence().getROI2Ds().size() > 0) {
 				setBtFromROIEnabled(true);
 			}
 		}
 	}
 
 	@Override
 	public void roiRemoved(MainEvent event) {
 		if (getCurrentSequence().getROI2Ds().size() == 0) {
 			setBtFromROIEnabled(false);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * plugins.nherve.toolbox.plugin.BackupAndPainterManagerSingletonPlugin#
 	 * sequenceHasChangedAfterSettingPainter()
 	 */
 	@Override
 	public void sequenceHasChangedAfterSettingPainter() {
 		if (hasCurrentSequence()) {
 			Sequence currentSequence = getCurrentSequence();
 			setTitle(getName() + " - " + currentSequence.getName());
 			setStack(getBackupObject());
 		} else {
 			setTitle(getName());
 			setStack(null);
 		}
 		refreshInterface();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * plugins.nherve.toolbox.plugin.BackupAndPainterManagerSingletonPlugin#
 	 * sequenceHasChangedBeforeSettingPainter()
 	 */
 	@Override
 	public void sequenceHasChangedBeforeSettingPainter() {
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see plugins.nherve.toolbox.plugin.SingletonPlugin#sequenceWillChange()
 	 */
 	@Override
 	public void sequenceWillChange() {
 		colorMap = null;
 	}
 
 	void setBackupOpacity(int backupOpacity) {
 		this.backupOpacity = backupOpacity;
 	}
 
 	public void setBtFromROIEnabled(boolean arg0) {
 		btFromROI.setEnabled(arg0);
 	}
 
 	/**
 	 * Sets the segmentation for sequence.
 	 * 
 	 * @param seq
 	 *            the seq
 	 * @param ms
 	 *            the ms
 	 */
 	public void setSegmentationForSequence(Sequence seq, MaskStack ms) {
 		if (ms != null) {
 			ms.checkAfterLoad((float) slOpacity.getValue() / 100f, seq.getFirstImage());
 			removeBackupObject(seq);
 			addBackupObject(seq, ms);
 			if (getCurrentSequence() == seq) {
 				setStack(ms);
 			}
 		}
 	}
 
 	/**
 	 * Sets the stack.
 	 * 
 	 * @param stack
 	 *            the new stack
 	 */
 	public void setStack(MaskStack stack) {
 		this.stack = stack;
 		if (stack != null) {
 			stack.addListener(getRunningInstance(false));
 		}
 		refreshInterface();
 	}
 
 	@Override
 	public void stackChanged(MaskStack s) {
 		if (getStack() == s) {
 			ThreadUtil.invokeLater(new Runnable() {
 
 				@Override
 				public void run() {
 					refreshInterface();
 				}
 			});
 		}
 	}
 
 	@Override
 	public Dimension getDefaultFrameDimension() {
 		return new Dimension(580, 800);
 	}
 	
 	@Override
 	public void fillInterface(JPanel mainPanel) {
 		SwimmingPool sp = Icy.getMainInterface().getSwimmingPool();
 		sp.addListener(this);
 
 		// LOAD / SAVE TOOLS
 		btGetSPGlobal = new JButton(NherveToolbox.fromSwimingPoolIcon);
 		btGetSPGlobal.setEnabled(isSegmentationInPool());
 		btGetSPGlobal.setToolTipText("Get the full stack from the swimming pool");
 		btGetSPGlobal.addActionListener(this);
 		btLoad = new JButton("Load");
 		btLoad.addActionListener(this);
 		btSave = new JButton("Save");
 		btSave.addActionListener(this);
 		btLoad.setEnabled(false);
 		btSave.setEnabled(false);
 		btSaveFullImage = new JButton("Export");
 		btSaveFullImage.setToolTipText("Export current display");
 		btSaveFullImage.addActionListener(this);
 		btSendSPGlobal = new JButton(NherveToolbox.toSwimingPoolIcon);
 		btSendSPGlobal.setToolTipText("Send the full stack to the swimming pool");
 		btSendSPGlobal.addActionListener(this);
 		JPanel lsTool = GuiUtil.createLineBoxPanel(new Component[] { Box.createHorizontalGlue(), btGetSPGlobal, Box.createHorizontalGlue(), btLoad, Box.createHorizontalGlue(), btSave, Box.createHorizontalGlue(), btSaveFullImage, Box.createHorizontalGlue(), btSendSPGlobal, Box.createHorizontalGlue() });
 		lsTool.setBorder(new TitledBorder("Load & Save tools"));
 		mainPanel.add(lsTool);
 
 		// DRAWING TOOLS
 		cbxCursorShape = new JComboBox();
 		cbxCursorShape.addItem(SHAPE_CIRCLE);
 		cbxCursorShape.addItem(SHAPE_SQUARE);
 		cbxCursorShape.addItem(SHAPE_HORIZONTAL_LINE);
 		cbxCursorShape.addItem(SHAPE_VERTICAL_LINE);
 		ComponentUtil.setFixedSize(cbxCursorShape, new Dimension(125, 25));
 		cbDrawEnabled = new JCheckBox("Draw enabled");
 		cbDrawEnabled.addItemListener(this);
 		btHelp = new JButton(NherveToolbox.questionIcon);
 		btHelp.addActionListener(this);
 		JPanel p1 = GuiUtil.createLineBoxPanel(new Component[] { new JLabel("Shape  "), cbxCursorShape, Box.createHorizontalGlue(), cbDrawEnabled, Box.createHorizontalGlue(), btHelp });
 
 		slCursorSize = new JSlider(JSlider.HORIZONTAL, 1, 100, 11);
 		slCursorSize.addChangeListener(this);
 		slCursorSize.setMajorTickSpacing(10);
 		slCursorSize.setMinorTickSpacing(2);
 		slCursorSize.setPaintTicks(true);
 		cbxCursorShape.setEnabled(false);
 		slCursorSize.setEnabled(false);
 		JPanel p2 = GuiUtil.createLineBoxPanel(new JLabel("Size  "), slCursorSize);
 
 		JPanel tool = GuiUtil.createPageBoxPanel(p1, p2);
 		tool.setBorder(new TitledBorder("Drawing tools"));
 		mainPanel.add(tool);
 
 		// DISPLAY TOOLS
 		cbOnlyContours = new JCheckBox("Contours");
 		cbOnlyContours.setSelected(false);
 		cbOnlyContours.addItemListener(this);
 		cbViewBgdBox = new JCheckBox("Image");
 		cbViewBgdBox.setSelected(true);
 		cbViewBgdBox.addItemListener(this);
 		cbBlackWhite = new JCheckBox("B / W");
 		cbBlackWhite.setSelected(true);
 		cbBlackWhite.addItemListener(this);
 		btRealColors = new JButton("Real colors");
 		btRealColors.addActionListener(this);
 		btArtificialColors = new JButton("Artificial colors");
 		btArtificialColors.addActionListener(this);
 		JPanel p3 = GuiUtil.createLineBoxPanel(new Component[] { cbOnlyContours, Box.createHorizontalGlue(), cbViewBgdBox, Box.createHorizontalGlue(), cbBlackWhite, Box.createHorizontalGlue(), btRealColors, Box.createHorizontalGlue(), btArtificialColors });
 
 		backupOpacity = 50;
 		slOpacity = new JSlider(JSlider.HORIZONTAL, 0, 100, backupOpacity);
 		slOpacity.addChangeListener(this);
 		slOpacity.setMajorTickSpacing(10);
 		slOpacity.setMinorTickSpacing(2);
 		slOpacity.setPaintTicks(true);
 		slOpacity.setEnabled(false);
 		JPanel p4 = GuiUtil.createLineBoxPanel(new JLabel("Opacity  "), slOpacity);
 
 		JPanel dspTool = GuiUtil.createPageBoxPanel(p3, p4);
 		dspTool.setBorder(new TitledBorder("Display tools"));
 		mainPanel.add(dspTool);
 
 		// MASKS LIST
 		mll = new JPanel();
 
 		stack = null;
 		layers = new ArrayList<MaskLayer>();
 
 		mll.setOpaque(false);
 		mll.setLayout(new BoxLayout(mll, BoxLayout.PAGE_AXIS));
 		mll.setBorder(new TitledBorder("Masks list"));
 
 		btAddMaskSP = new JButton("From SP");
 		btAddMaskSP.addActionListener(this);
 		btAddMaskSP.setEnabled(isMaskInPool());
 
 		btFromROI = new JButton("From ROI");
 		btFromROI.addActionListener(this);
 		btFromROI.setEnabled(false);
 
 		btDuplicateMask = new JButton("Copy mask");
 		btDuplicateMask.addActionListener(this);
 		btDuplicateMask.setEnabled(false);
 
 		btAddMask = new JButton("New mask");
 		btAddMask.addActionListener(this);
 		btAddMask.setEnabled(false);
 
 		btCompare = new JButton("Compare");
 		btCompare.addActionListener(this);
 
 		JPanel btp = GuiUtil.createLineBoxPanel(new Component[] { Box.createHorizontalGlue(), btAddMaskSP, Box.createHorizontalGlue(), btFromROI, Box.createHorizontalGlue(), btDuplicateMask, Box.createHorizontalGlue(), btAddMask, Box.createHorizontalGlue(), btCompare, Box.createHorizontalGlue() });
 		mll.add(btp);
 
 		btErode = new JButton("Erode");
 		btErode.addActionListener(this);
 
 		btDilate = new JButton("Dilate");
 		btDilate.addActionListener(this);
 
 		btInvert = new JButton("Invert");
 		btInvert.addActionListener(this);
 
 		btFillHoles = new JButton("Fill holes");
 		btFillHoles.addActionListener(this);
 
 		btFilterSize = new JButton("Filter");
 		btFilterSize.addActionListener(this);
 
 		tfFilterSize = new JTextField("1");
 		ComponentUtil.setFixedSize(tfFilterSize, new Dimension(50, 25));
 
 		JPanel morpho = GuiUtil.createLineBoxPanel(new Component[] { btErode, Box.createHorizontalGlue(), btDilate, Box.createHorizontalGlue(), btInvert, Box.createHorizontalGlue(), btFillHoles, Box.createHorizontalGlue(), btFilterSize, Box.createHorizontalGlue(), tfFilterSize });
 		mll.add(morpho);
 
 		mlp = new JPanel();
 		mlp.setLayout(new BoxLayout(mlp, BoxLayout.PAGE_AXIS));
 		// mlp.setMinimumSize(new Dimension(0, 300));
 
 		scroll = new JScrollPane(mlp);
 		mll.add(scroll);
 
 		mll.setTransferHandler(new BackgroundPanelTransferHandler());
 
 		mainPanel.add(mll);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent
 	 * )
 	 */
 	@Override
 	public void stateChanged(ChangeEvent e) {
 		Object o = e.getSource();
 
 		if (o == null) {
 			return;
 		}
 
 		if (o instanceof JSlider) {
 			JSlider s = (JSlider) e.getSource();
 
 			if (s == slOpacity) {
 				float globalOpacity = getGlobalOpacity();
 				for (MaskLayer ml : this) {
 					ml.getMask().setOpacity(globalOpacity);
 				}
 				getCurrentSequence().painterChanged(null);
 			}
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see plugins.nherve.toolbox.plugin.SingletonPlugin#stopInterface()
 	 */
 	@Override
 	public void stopInterface() {
 		SwimmingPool sp = Icy.getMainInterface().getSwimmingPool();
 		sp.removeListener(this);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see icy.swimmingPool.SwimmingPoolListener#swimmingPoolChangeEvent(icy.
 	 * swimmingPool.SwimmingPoolEvent)
 	 */
 	@Override
 	public void swimmingPoolChangeEvent(SwimmingPoolEvent swimmingPoolEvent) {
 		Thread ct = Thread.currentThread();
 
 		if (ct.getClass().getName().equals("java.awt.EventDispatchThread")) {
 			btGetSPGlobal.setEnabled(isSegmentationInPool());
 			btAddMaskSP.setEnabled(isMaskInPool());
 		} else {
 			ThreadUtil.invokeLater(new Runnable() {
 				@Override
 				public void run() {
 					btGetSPGlobal.setEnabled(isSegmentationInPool());
 					btAddMaskSP.setEnabled(isMaskInPool());
 				}
 			});
 		}
 	}
 
 	/**
 	 * Switch active layer display.
 	 */
 	public void switchActiveLayerDisplay() {
 		for (MaskLayer l : layers) {
 			if (l.isCurrentlyActive()) {
 				switchLayerDisplay(l);
 				break;
 			}
 		}
 	}
 
 	/**
 	 * Switch layer display.
 	 * 
 	 * @param id
 	 *            the id
 	 */
 	public void switchLayerDisplay(int id) {
 		MaskLayer l = getLayerById(id);
 		switchLayerDisplay(l);
 	}
 
 	/**
 	 * Switch layer display.
 	 * 
 	 * @param l
 	 *            the l
 	 */
 	public void switchLayerDisplay(MaskLayer l) {
 		if (l != null) {
 			if (l.getCbView().isSelected()) {
 				l.getCbView().setSelected(false);
 				l.getMask().setVisibleLayer(false);
 			} else {
 				l.getCbView().setSelected(true);
 				l.getMask().setVisibleLayer(true);
 			}
 		}
 	}
 
 	/**
 	 * Switch opacity off.
 	 * 
 	 * @return the int
 	 */
 	public int switchOpacityOff() {
 		int bck = slOpacity.getValue();
 		slOpacity.setValue(slOpacity.getMinimum());
 		return bck;
 	}
 
 	/**
 	 * Switch opacity on.
 	 */
 	public void switchOpacityOn() {
 		switchOpacityOn(slOpacity.getMaximum());
 	}
 
 	/**
 	 * Switch opacity on.
 	 * 
 	 * @param bck
 	 *            the bck
 	 */
 	public void switchOpacityOn(int bck) {
 		slOpacity.setValue(bck);
 	}
 
 }
