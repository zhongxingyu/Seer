 /*
  * Copyright 2010, 2011 Institut Pasteur.
  * 
  * This file is part of ${CurrentProject}, which is an ICY plugin.
  * 
  * ${CurrentProject} is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * ${CurrentProject} is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with ${CurrentProject}. If not, see <http://www.gnu.org/licenses/>.
  */
 package plugins.nherve.colorquantization;
 
 import icy.gui.component.ComponentUtil;
 import icy.gui.util.GuiUtil;
 import icy.main.Icy;
 import icy.preferences.XMLPreferences;
 import icy.sequence.Sequence;
 import icy.swimmingPool.SwimmingObject;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.lang.reflect.InvocationTargetException;
 import java.text.DecimalFormat;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.swing.Box;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.SwingUtilities;
 import javax.swing.border.TitledBorder;
 
 import plugins.nherve.maskeditor.MaskEditor;
 import plugins.nherve.toolbox.NherveToolbox;
 import plugins.nherve.toolbox.image.feature.DefaultClusteringAlgorithmImpl;
 import plugins.nherve.toolbox.image.feature.SegmentableBufferedImage;
 import plugins.nherve.toolbox.image.feature.Signature;
 import plugins.nherve.toolbox.image.feature.SupportRegion;
 import plugins.nherve.toolbox.image.feature.clustering.KMeans;
 import plugins.nherve.toolbox.image.feature.descriptor.ColorPixel;
 import plugins.nherve.toolbox.image.feature.descriptor.DefaultDescriptorImpl;
 import plugins.nherve.toolbox.image.feature.region.GridFactory;
 import plugins.nherve.toolbox.image.feature.region.SupportRegionException;
 import plugins.nherve.toolbox.image.feature.signature.SignatureException;
 import plugins.nherve.toolbox.image.feature.signature.VectorSignature;
 import plugins.nherve.toolbox.image.mask.Mask;
 import plugins.nherve.toolbox.image.mask.MaskException;
 import plugins.nherve.toolbox.image.segmentation.DefaultSegmentationAlgorithm;
 import plugins.nherve.toolbox.image.segmentation.Segmentation;
 import plugins.nherve.toolbox.image.segmentation.SegmentationException;
 import plugins.nherve.toolbox.image.toolboxes.ColorSpaceTools;
 import plugins.nherve.toolbox.plugin.HelpWindow;
 import plugins.nherve.toolbox.plugin.SingletonPlugin;
 
 /**
  * The Class KMeansColorQuantization.
  * 
  * The pixel colors extraction is not optimized as it uses a more generic
  * framework. If you need a quicker version, you'll have to change this part.
  * However, the KMeans implementation is optimized and multithreaded.
  * 
  * @author Nicolas HERVE - nicolas.herve@pasteur.fr
  */
 public class KMeansColorQuantization extends SingletonPlugin implements ActionListener {
	private static String HELP = "<html>" + "<p align=\"center\"><b>" + HelpWindow.getTagFullPluginName() + "</b></p>" + "<p align=\"center\"><b>" + NherveToolbox.getDevNameHtml() + "</b></p>" + "<p align=\"center\"><b>" + NherveToolbox.getCopyrightHtml() + "</b></p>" + "<hr/>" + "<p>" + HelpWindow.getTagPluginName() + NherveToolbox.getLicenceHtml() + "</p>" + "<p>" + NherveToolbox.getLicenceHtmllink() + "</p>" + "</html>";
 
 	private Map<Integer, Integer> indexToColorspace;
 
 	/** The cb color space. */
 	private JComboBox cbColorSpace;
 
 	/** The bt start. */
 	private JButton btStart;
 
 	/** The tf nb cluster2. */
 	private JTextField tfNbCluster2;
 
 	/** The tf nb iteration2. */
 	private JTextField tfNbIteration2;
 
 	/** The tf stab crit2. */
 	private JTextField tfStabCrit2;
 
 	/** The cb display. */
 	private JCheckBox cbDisplay;
 
 	/** The cb send mask directly. */
 	private JCheckBox cbSendMaskDirectly;
 
 	private JButton btHelp;
 
 	/** The currently running. */
 	private Thread currentlyRunning;
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see plugins.nherve.toolbox.AbleToLogMessages#isDisplayEnabled()
 	 */
 	@Override
 	public boolean isLogEnabled() {
 		return cbDisplay.isSelected();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see plugins.nherve.toolbox.plugin.SingletonPlugin#sequenceHasChanged()
 	 */
 	@Override
 	public void sequenceHasChanged() {
 		btStart.setEnabled(hasCurrentSequence());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see plugins.nherve.toolbox.plugin.SingletonPlugin#sequenceWillChange()
 	 */
 	@Override
 	public void sequenceWillChange() {
 	}
 	
 	@Override
 	public void fillInterface(JPanel mainPanel) {
 		currentlyRunning = null;
 
 		XMLPreferences preferences = getPreferences();
 		int nbc2 = preferences.getInt("nbc2", 10);
 		int nbi2 = preferences.getInt("nbi2", 100);
 		double stab2 = preferences.getDouble("stab2", 0.001);
 
 		boolean dsp = preferences.getBoolean("dsp", false);
 
 		indexToColorspace = new HashMap<Integer, Integer>();
 		cbColorSpace = new JComboBox();
 		cbColorSpace.addItem(ColorSpaceTools.COLOR_SPACES[ColorSpaceTools.RGB]);
 		indexToColorspace.put(0, ColorSpaceTools.RGB);
 		cbColorSpace.addItem(ColorSpaceTools.COLOR_SPACES[ColorSpaceTools.RGB_TO_HSV]);
 		indexToColorspace.put(1, ColorSpaceTools.RGB_TO_HSV);
 		cbColorSpace.addItem(ColorSpaceTools.COLOR_SPACES[ColorSpaceTools.RGB_TO_H1H2H3]);
 		indexToColorspace.put(2, ColorSpaceTools.RGB_TO_H1H2H3);
 		ComponentUtil.setFixedSize(cbColorSpace, new Dimension(100, 25));
 
 		JPanel cs = GuiUtil.createLineBoxPanel(new JLabel("Color space"), Box.createHorizontalGlue(), cbColorSpace);
 		cs.setBorder(new TitledBorder("Color description"));
 		mainPanel.add(cs, BorderLayout.NORTH);
 
 		tfNbCluster2 = new JTextField(Integer.toString(nbc2));
 		ComponentUtil.setFixedSize(tfNbCluster2, new Dimension(100, 25));
 		JPanel p1 = GuiUtil.createLineBoxPanel(new JLabel("Nb. clusters"), Box.createHorizontalGlue(), tfNbCluster2);
 
 		tfNbIteration2 = new JTextField(Integer.toString(nbi2));
 		ComponentUtil.setFixedSize(tfNbIteration2, new Dimension(100, 25));
 		JPanel p2 = GuiUtil.createLineBoxPanel(new JLabel("Nb. max iterations"), Box.createHorizontalGlue(), tfNbIteration2);
 
 		tfStabCrit2 = new JTextField(Double.toString(stab2));
 		ComponentUtil.setFixedSize(tfStabCrit2, new Dimension(100, 25));
 		JPanel p3 = GuiUtil.createLineBoxPanel(new JLabel("Stabilization criterion"), Box.createHorizontalGlue(), tfStabCrit2);
 
 		JPanel algo = GuiUtil.createPageBoxPanel(p1, p2, p3);
 		algo.setBorder(new TitledBorder("KMeans clustering algorithm"));
 		mainPanel.add(algo, BorderLayout.CENTER);
 
 		btHelp = new JButton(NherveToolbox.questionIcon);
 		btHelp.setToolTipText("Get some basic informations");
 		btHelp.addActionListener(this);
 		cbDisplay = new JCheckBox("Log");
 		cbDisplay.setSelected(dsp);
 		cbSendMaskDirectly = new JCheckBox("Send to editor");
 		cbSendMaskDirectly.setSelected(true);
 		btStart = new JButton("Start");
 		btStart.addActionListener(this);
 		btStart.setEnabled(hasCurrentSequence());
 
 		JPanel bottom = GuiUtil.createLineBoxPanel(new Component[] { btHelp, Box.createHorizontalGlue(), cbDisplay, cbSendMaskDirectly, Box.createHorizontalGlue(), btStart });
 		mainPanel.add(bottom, BorderLayout.SOUTH);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see plugins.nherve.toolbox.plugin.SingletonPlugin#stopInterface()
 	 */
 	@Override
 	public void stopInterface() {
 		try {
 			XMLPreferences preferences = getPreferences();
 
 			preferences.putInt("nbc2", Integer.parseInt(tfNbCluster2.getText()));
 			preferences.putInt("nbi2", Integer.parseInt(tfNbIteration2.getText()));
 			preferences.putDouble("stab2", Double.parseDouble(tfStabCrit2.getText()));
 			preferences.putInt("cs", cbColorSpace.getSelectedIndex());
 			preferences.putBoolean("dsp", cbDisplay.isSelected());
 		} catch (NumberFormatException e) {
 			e.printStackTrace();
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
 		JButton b = (JButton) e.getSource();
 		if (b == null) {
 			return;
 		}
 
 		if (b == btHelp) {
 			openHelpWindow(HELP, 400, 300);
 			return;
 		}
 
 		if (b == btStart) {
 			if (hasCurrentSequence() && (currentlyRunning == null)) {
 				btStart.setEnabled(false);
 				currentlyRunning = new Thread() {
 					@Override
 					public void run() {
 						try {
 							final Sequence cs = getCurrentSequence();
 							final Segmentation seg = doClustering(cs);
 							if (cbSendMaskDirectly.isSelected()) {
 								final MaskEditor me = MaskEditor.getRunningInstance(true);
 								currentlyRunning = null;
 								Runnable r = new Runnable() {
 									@Override
 									public void run() {
 										me.setSegmentationForSequence(cs, seg);
 										me.switchOpacityOn();
 										btStart.setEnabled(true);
 									}
 								};
 								SwingUtilities.invokeAndWait(r);
 							} else {
 								SwimmingObject result = new SwimmingObject(seg);
 								Icy.getMainInterface().getSwimmingPool().add(result);
 								currentlyRunning = null;
 								Runnable r = new Runnable() {
 									@Override
 									public void run() {
 										btStart.setEnabled(true);
 									}
 								};
 								SwingUtilities.invokeAndWait(r);
 							}
 						} catch (SupportRegionException e1) {
 							logError(e1.getClass().getName() + " : " + e1.getMessage());
 						} catch (SegmentationException e1) {
 							logError(e1.getClass().getName() + " : " + e1.getMessage());
 						} catch (InterruptedException e1) {
 							logError(e1.getClass().getName() + " : " + e1.getMessage());
 						} catch (InvocationTargetException e1) {
 							logError(e1.getClass().getName() + " : " + e1.getMessage());
 						} catch (MaskException e1) {
 							logError(e1.getClass().getName() + " : " + e1.getMessage());
 						} catch (NumberFormatException e) {
 							logError(e.getClass().getName() + " : " + e.getMessage());
 						} catch (SignatureException e) {
 							logError(e.getClass().getName() + " : " + e.getMessage());
 						}
 					}
 				};
 				currentlyRunning.start();
 			}
 			return;
 		}
 
 	}
 
 	/**
 	 * Do single clustering.
 	 * 
 	 * @param img
 	 *            the img
 	 * @param regions
 	 *            the regions
 	 * @param descriptor
 	 *            the descriptor
 	 * @param algo
 	 *            the algo
 	 * @return the segmentation
 	 * @throws SupportRegionException
 	 *             the support region exception
 	 * @throws SegmentationException
 	 *             the segmentation exception
 	 */
 	private Segmentation doSingleClustering(SegmentableBufferedImage img, SupportRegion[] regions, DefaultDescriptorImpl<SegmentableBufferedImage, ? extends Signature> descriptor, DefaultClusteringAlgorithmImpl<VectorSignature> algo) throws SupportRegionException, SegmentationException {
 		DefaultSegmentationAlgorithm<SegmentableBufferedImage> segAlgo = new DefaultSegmentationAlgorithm<SegmentableBufferedImage>(descriptor, algo);
 		segAlgo.setLogEnabled(isLogEnabled());
 
 		Segmentation seg = segAlgo.segment(img, regions);
 
 		return seg;
 	}
 
 	/**
 	 * Gets the some stats.
 	 * 
 	 * @param seg
 	 *            the seg
 	 * @return the some stats
 	 */
 	private void getSomeStats(Segmentation seg) {
 		DecimalFormat df = new DecimalFormat("0");
 		System.out.println("id;label;h1;h2;h3");
 		for (Mask m : seg) {
 			Color rgb = m.getColor();
 			double[] h1h2h3 = ColorSpaceTools.getColorComponentsD_0_255(ColorSpaceTools.RGB_TO_I1H2H3, rgb.getRed(), rgb.getGreen(), rgb.getBlue());
 			log(m.getId() + ";" + m.getLabel() + ";" + df.format(h1h2h3[0]) + ";" + df.format(h1h2h3[1]) + ";" + df.format(h1h2h3[2]));
 		}
 	}
 
 	/**
 	 * Do clustering.
 	 * 
 	 * @param currentSequence
 	 *            the current sequence
 	 * @return the segmentation
 	 * @throws SupportRegionException
 	 *             the support region exception
 	 * @throws SegmentationException
 	 *             the segmentation exception
 	 * @throws MaskException
 	 *             the mask exception
 	 * @throws NumberFormatException
 	 *             the number format exception
 	 * @throws SignatureException
 	 *             the signature exception
 	 */
 	private Segmentation doClustering(Sequence currentSequence) throws SupportRegionException, SegmentationException, MaskException, NumberFormatException, SignatureException {
 		Segmentation seg = null;
 
 		seg = doClusteringKM(currentSequence);
 		seg.reInitColors(currentSequence.getImage(0, 0));
 		if (isLogEnabled()) {
 			getSomeStats(seg);
 		}
 
 		return seg;
 	}
 
 	/**
 	 * Do clustering km.
 	 * 
 	 * @param currentSequence
 	 *            the current sequence
 	 * @return the segmentation
 	 * @throws SupportRegionException
 	 *             the support region exception
 	 * @throws SegmentationException
 	 *             the segmentation exception
 	 * @throws MaskException
 	 *             the mask exception
 	 * @throws NumberFormatException
 	 *             the number format exception
 	 * @throws SignatureException
 	 *             the signature exception
 	 */
 	private Segmentation doClusteringKM(Sequence currentSequence) throws SupportRegionException, SegmentationException, MaskException, NumberFormatException, SignatureException {
 		int nbc2 = Integer.parseInt(tfNbCluster2.getText());
 		int nbi2 = Integer.parseInt(tfNbIteration2.getText());
 		double stab2 = Double.parseDouble(tfStabCrit2.getText());
 		int cs = indexToColorspace.get(cbColorSpace.getSelectedIndex());
 
 		log("Working on " + ColorSpaceTools.COLOR_SPACES[cs]);
 
 		SegmentableBufferedImage img = new SegmentableBufferedImage(currentSequence.getFirstImage());
 
 		KMeans km2 = new KMeans(nbc2, nbi2, stab2);
 		km2.setLogEnabled(isLogEnabled());
 
 		Segmentation seg = null;
 
 		DefaultDescriptorImpl<SegmentableBufferedImage, ? extends Signature> col = null;
 
 		ColorPixel cd = new ColorPixel(isLogEnabled());
 		cd.setColorSpace(cs);
 		col = cd;
 
 		col.setLogEnabled(isLogEnabled());
 
 		GridFactory factory = new GridFactory(GridFactory.ALGO_ONLY_PIXELS);
 		factory.setLogEnabled(isLogEnabled());
 		List<SupportRegion> lRegions = factory.extractRegions(img);
 		SupportRegion[] regions = new SupportRegion[lRegions.size()];
 		int r = 0;
 		for (SupportRegion sr : lRegions) {
 			regions[r++] = sr;
 		}
 
 		seg = doSingleClustering(img, regions, col, km2);
 
 		return seg;
 	}
 	
 	@Override
 	public Dimension getDefaultFrameDimension() {
 		return null;
 	}
 }
