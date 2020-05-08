 package gui;
 
 import gui.View.AnimationSpeed;
 import gui.property.ColorGradient;
 import gui.swing.ComponentFactory;
 import gui.util.CompoundPropertyHighlighter;
 import gui.util.HighlightAutomatic;
 import gui.util.Highlighter;
 import gui.util.SubstructureHighlighter;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.ArrayList;
 import java.util.BitSet;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 
 import javax.swing.JComponent;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.SwingUtilities;
 
 import main.ScreenSetup;
 import main.Settings;
 
 import org.jmol.adapter.smarter.SmarterJmolAdapter;
 import org.jmol.api.JmolAdapter;
 import org.jmol.api.JmolSimpleViewer;
 
 import util.ArrayUtil;
 import util.ColorUtil;
 import util.ObjectUtil;
 import util.StringUtil;
 import util.ThreadUtil;
 import cluster.Cluster;
 import cluster.Clustering;
 import cluster.ClusteringUtil;
 import cluster.Compound;
 import data.ClusteringData;
 import dataInterface.CompoundProperty;
 import dataInterface.CompoundProperty.Type;
 import dataInterface.CompoundPropertyOwner;
 import dataInterface.CompoundPropertyUtil;
 import dataInterface.SubstructureSmartsType;
 
 public class MainPanel extends JPanel implements ViewControler
 {
 	GUIControler guiControler;
 	JmolPanel jmolPanel;
 	View view;
 	private Clustering clustering;
 	private boolean spinEnabled = false;
 	private boolean hideHydrogens = true;
 	private Style style = Style.wireframe;
 
 	HideCompounds hideCompounds = HideCompounds.nonActive;
 	Color matchColor = Color.ORANGE;
 	CompoundProperty compoundDescriptorProperty = null;
 	List<JComponent> ignoreMouseMovementPanels = new ArrayList<JComponent>();
 
 	private static final ColorGradient DEFAULT_COLOR_GRADIENT = new ColorGradient(
 			CompoundPropertyUtil.getHighValueColor(), Color.WHITE, CompoundPropertyUtil.getLowValueColor());
 
 	private String getStyleString()
 	{
 		if (ScreenSetup.INSTANCE.isFontSizeLarge())
 		{
 			switch (style)
 			{
 				case wireframe:
 					return "spacefill 0; wireframe 0.08";
 				case ballsAndSticks:
 					return "wireframe 35; spacefill 21%";
 				case dots:
 					return "spacefill 65%";
 			}
 			throw new IllegalStateException("WTF");
 		}
 		else
 		{
 			switch (style)
 			{
 				case wireframe:
 					return "spacefill 0; wireframe 0.02";
 				case ballsAndSticks:
 					return "wireframe 25; spacefill 15%";
 				case dots:
 					return "spacefill 55%";
 			}
 			throw new IllegalStateException("WTF");
 		}
 	}
 
 	public Color getHighlightColor(CompoundPropertyOwner m, Highlighter h, CompoundProperty p)
 	{
 		return getHighlightColor(clustering, m, h, p);
 	}
 
 	public static Color getHighlightColor(Clustering clustering, CompoundPropertyOwner m, Highlighter h,
 			CompoundProperty p)
 	{
 		if (h == Highlighter.CLUSTER_HIGHLIGHTER)
 			if (m instanceof Compound)
 				return CompoundPropertyUtil.getClusterColor(clustering.getClusterIndexForCompound((Compound) m));
 			else
 				return null;
 		else
 			return getHighlightColor(clustering, m, p);
 	}
 
 	public static Color getHighlightColor(Clustering clustering, CompoundPropertyOwner m, CompoundProperty p)
 	{
 		return getHighlightColor(clustering, m, p, false);
 	}
 
 	public static Color getHighlightColor(Clustering clustering, CompoundPropertyOwner m, CompoundProperty p,
 			boolean whiteBackground)
 	{
 		if (p == null)
 			return null;
 		else if (p.getType() == Type.NOMINAL)
 		{
 			if (m.getStringValue(p) == null)
 				return CompoundPropertyUtil.getNullValueColor();
 			else
 				return CompoundPropertyUtil.getNominalColor(p, m.getStringValue(p));
 		}
 		else if (p.getType() == Type.NUMERIC)
 		{
 			if (m.getDoubleValue(p) == null)
 				return CompoundPropertyUtil.getNullValueColor();
 			double val;
 			if (p.getType() == Type.NUMERIC && p.isLogHighlightingEnabled())
 				val = clustering.getNormalizedLogDoubleValue(m, p);
 			else
 				val = clustering.getNormalizedDoubleValue(m, p);
 			ColorGradient grad = DEFAULT_COLOR_GRADIENT;
 			if (whiteBackground && grad.med == Color.WHITE)
 				grad = new ColorGradient(grad.high, Color.BLACK, grad.low);
 			if (p.getType() == Type.NUMERIC && p.getHighlightColorGradient() != null)
 				grad = p.getHighlightColorGradient();
 			return grad.getColor(val);
 		}
 		else
 			return null;
 	}
 
 	public static enum Translucency
 	{
 		None, ModerateWeak, ModerateStrong, Strong;
 	}
 
 	private String getColorSuffixTranslucent(Translucency t)
 	{
 		if (t == Translucency.None)
 			return "";
 		double translucency[] = null;
 		if (style == Style.wireframe)
 			translucency = new double[] { -1, 0.4, 0.6, 0.8 };
 		else if (style == Style.ballsAndSticks)
 			translucency = new double[] { -1, 0.5, 0.7, 0.9 };
 		else if (style == Style.dots)
 			translucency = new double[] { -1, 0.5, 0.7, 0.9 };
 		else
 			throw new IllegalStateException("WTF");
 		double trans = translucency[ArrayUtil.indexOf(Translucency.values(), t)];
 		//		if (Settings.SCREENSHOT_SETUP)
 		//			trans = 0.99;// Math.min(0.95, trans + 0.15);
 		//		Settings.LOGGER.warn(trans);
 		return "; color translucent " + trans;
 	}
 
 	HashMap<String, Highlighter[]> highlighters;
 	Highlighter selectedHighlighter = Highlighter.CLUSTER_HIGHLIGHTER;
 	Highlighter lastSelectedHighlighter = Highlighter.DEFAULT_HIGHLIGHTER;
 	CompoundProperty selectedHighlightCompoundProperty = null;
 	boolean highlighterLabelsVisible = false;
 	HighlightSorting highlightSorting = HighlightSorting.Median;
 	HighlightAutomatic highlightAutomatic;
 	HighlightMode highlightMode = HighlightMode.ColorCompounds;
 	boolean antialiasEnabled = ScreenSetup.INSTANCE.isAntialiasOn();
 	boolean highlightLastFeatureEnabled = false;
 
 	public Clustering getClustering()
 	{
 		return clustering;
 	}
 
 	public boolean isSpinEnabled()
 	{
 		return spinEnabled;
 	}
 
 	private int spinSpeed = 3;
 
 	public void setSpinEnabled(boolean spin)
 	{
 		setSpinEnabled(spin, false);
 	}
 
 	private void setSpinEnabled(boolean spinEnabled, boolean force)
 	{
 		if (this.spinEnabled != spinEnabled || force)
 		{
 			this.spinEnabled = spinEnabled;
 			view.setSpinEnabled(spinEnabled, spinSpeed);
 			fireViewChange(PROPERTY_SPIN_CHANGED);
 			guiControler.showMessage((spinEnabled ? "Enable" : "Disable") + " spinning.");
 		}
 	}
 
 	@Override
 	public void increaseSpinSpeed(boolean increase)
 	{
 		if (spinEnabled && (increase || spinSpeed > 2))
 		{
 			if (increase)
 				spinSpeed++;
 			else
 				spinSpeed--;
 			view.setSpinEnabled(spinEnabled, spinSpeed);
 			guiControler.showMessage((increase ? "Increase" : "Decrease") + " spin speed to " + spinSpeed + ".");
 		}
 	}
 
 	public MainPanel(GUIControler guiControler)
 	{
 		this.guiControler = guiControler;
 		jmolPanel = new JmolPanel();
 
 		setLayout(new BorderLayout());
 		add(jmolPanel);
 
 		clustering = new Clustering();
 		clustering.addListener(new PropertyChangeListener()
 		{
 			@Override
 			public void propertyChange(PropertyChangeEvent evt)
 			{
 				if (evt.getPropertyName().equals(Clustering.CLUSTER_NEW)
 						|| evt.getPropertyName().equals(Clustering.CLUSTER_CLEAR))
 					MainPanel.this.guiControler.updateTitle(clustering);
 			}
 		});
 		View.init(jmolPanel, guiControler, this, clustering);
 		view = View.instance;
 		highlightAutomatic = new HighlightAutomatic(this, clustering);
 
 		setBackgroundBlack(ComponentFactory.isBackgroundBlack(), true);
 
 		// mouse listener to click atoms or clusters (zoom in)
 		jmolPanel.addMouseListener(new MouseAdapter()
 		{
 			JPopupMenu popup;
 
 			public void mouseClicked(MouseEvent e)
 			{
 				if (SwingUtilities.isLeftMouseButton(e))
 				{
 					int atomIndex = view.findNearestAtomIndex(e.getX(), e.getY());
 					if (atomIndex != -1)
 					{
 						boolean selectCluster = !clustering.isClusterActive() && !e.isControlDown();
 						boolean selectCompound = clustering.isClusterActive() || e.isShiftDown();
 
 						if (selectCluster)
 						{
 							// set a cluster to active (zooming will be done in listener)
 							clustering.getClusterActive().setSelected(
 									clustering.getClusterIndexForCompoundIndex(view.getAtomCompoundIndex(atomIndex)));
 							clustering.getClusterWatched().clearSelection();
 						}
 						if (selectCompound)
 						{
 							// already a cluster active, zooming into compound
 							// final Compound m = clustering.getCompoundWithCompoundIndex(view.getAtomCompoundIndex(atomIndex));
 							clustering.getCompoundWatched().clearSelection();
 							if (e.isControlDown())
 								clustering.getCompoundActive()
 										.setSelectedInverted(view.getAtomCompoundIndex(atomIndex));
 							else
 							{
 								if (clustering.getCompoundActive().isSelected(view.getAtomCompoundIndex(atomIndex)))
 									clustering.getCompoundActive().clearSelection();
 								else
 									clustering.getCompoundActive().setSelected(view.getAtomCompoundIndex(atomIndex));
 							}
 						}
 					}
 					else
 					{
 						clustering.getCompoundWatched().clearSelection();
 						clustering.getClusterWatched().clearSelection();
 						clustering.getCompoundActive().clearSelection();
 					}
 				}
 				else if (SwingUtilities.isRightMouseButton(e))
 				{
 					if (popup == null)
 						popup = MainPanel.this.guiControler.getPopup();
 					else
 						popup.updateUI();
 					popup.show(e.getComponent(), e.getX(), e.getY());
 				}
 			}
 		});
 
 		jmolPanel.addMouseMotionListener(new MouseAdapter()
 		{
 
 			public void mouseMoved(MouseEvent e)
 			{
 				updateMouse(e.getPoint(), e.isShiftDown());
 			}
 		});
 	}
 
 	Point mousePos;
 	boolean shiftDown;
 
 	@Override
 	public void updateMouseSelection(boolean shiftDown)
 	{
 		updateMouse(mousePos, shiftDown);
 	}
 
 	private void updateMouse(Point mousePos, boolean shiftDown)
 	{
 		this.mousePos = mousePos;
 		this.shiftDown = shiftDown;
 
 		//		System.err.println("mouse udpate " + mousePos + " " + shiftDown);
 
 		for (JComponent c : ignoreMouseMovementPanels)
 		{
 			if (c.isVisible())
 			{
 				Point p = SwingUtilities.convertPoint(MainPanel.this, mousePos, c);
 				if (p.x >= 0 && p.y >= 0 && p.x <= c.getWidth() && p.y <= c.getHeight())
 					return;
 			}
 		}
 
 		int atomIndex = view.findNearestAtomIndex(mousePos.x, mousePos.y);
 
 		if (!clustering.isClusterActive() && !shiftDown)
 		{
 			if (atomIndex == -1)
 			{
 				// // do not clear cluster selection
 				clustering.getClusterWatched().clearSelection();
 			}
 			else
 			{
 				clustering.getCompoundWatched().clearSelection();
 				//clustering.getCompoundActive().clearSelection();
 				clustering.getClusterWatched().setSelected(
 						(clustering.getClusterIndexForCompoundIndex(view.getAtomCompoundIndex(atomIndex))));
 			}
 		}
 		else
 		{
 			if (atomIndex == -1)
 			{
 				// // do not clear compound selection
 				clustering.getCompoundWatched().clearSelection();
 			}
 			else
 			{
 				clustering.getClusterWatched().clearSelection();
 				clustering.getCompoundWatched().setSelected(view.getAtomCompoundIndex(atomIndex));
 			}
 		}
 	}
 
 	@Override
 	public void addIgnoreMouseMovementComponents(JComponent c)
 	{
 		this.ignoreMouseMovementPanels.add(c);
 	}
 
 	public Style getStyle()
 	{
 		return style;
 	}
 
 	private boolean putSpheresBackOn = false;
 
 	public void setStyle(Style style)
 	{
 		if (this.style != style)
 		{
 			if (style == Style.dots && highlightMode == HighlightMode.Spheres)
 			{
 				setHighlightMode(HighlightMode.ColorCompounds);
 				putSpheresBackOn = true;
 			}
 			else if ((style == Style.ballsAndSticks || style == Style.wireframe) && putSpheresBackOn)
 			{
 				setHighlightMode(HighlightMode.Spheres);
 				putSpheresBackOn = false;
 			}
 			guiControler.block("changing style");
 			this.style = style;
 			updateAllClustersAndCompounds(false);
 			fireViewChange(PROPERTY_STYLE_CHANGED);
 			if (style == Style.ballsAndSticks)
 				guiControler.showMessage("Draw compounds with balls (atoms) and sticks (bonds).");
 			else if (style == Style.wireframe)
 				guiControler.showMessage("Draw compounds with wireframes (shows only bonds).");
 			else if (style == Style.dots)
 				guiControler.showMessage("Draw compounds as dots.");
 			guiControler.unblock("changing style");
 		}
 	}
 
 	@Override
 	public HashMap<String, Highlighter[]> getHighlighters()
 	{
 		return highlighters;
 	}
 
 	@Override
 	public Highlighter getHighlighter()
 	{
 		return selectedHighlighter;
 	}
 
 	@Override
 	public void setHighlighter(Highlighter highlighter)
 	{
 		setHighlighter(highlighter, true);
 	}
 
 	@Override
 	public void setHighlighter(Highlighter highlighter, boolean showMessage)
 	{
 		if (this.selectedHighlighter != highlighter)
 		{
 			guiControler.block("set highlighter");
 			lastSelectedHighlighter = selectedHighlighter;
 			selectedHighlighter = highlighter;
 			if (selectedHighlighter instanceof CompoundPropertyHighlighter)
 				selectedHighlightCompoundProperty = ((CompoundPropertyHighlighter) highlighter).getProperty();
 			else
 				selectedHighlightCompoundProperty = null;
 
 			updateAllClustersAndCompounds(false);
 			fireViewChange(PROPERTY_HIGHLIGHT_CHANGED);
 			if (showMessage)
 			{
 				String lastMsg = ".";
 				if (highlightLastFeatureEnabled && lastSelectedHighlighter != null
 						&& lastSelectedHighlighter != Highlighter.DEFAULT_HIGHLIGHTER)
 				{
 					if (lastSelectedHighlighter == Highlighter.CLUSTER_HIGHLIGHTER)
 						lastMsg = " (flat sphere highlights cluster assignement).";
 					else if (lastSelectedHighlighter instanceof CompoundPropertyHighlighter)
 						lastMsg = " (flat sphere highlights '" + lastSelectedHighlighter + "').";
 				}
 				if (highlighter == Highlighter.DEFAULT_HIGHLIGHTER)
 					guiControler.showMessage("Disable highlighting" + lastMsg);
 				else if (highlighter == Highlighter.CLUSTER_HIGHLIGHTER)
 					guiControler.showMessage("Highlight cluster assignement" + lastMsg);
 				else if (highlighter instanceof CompoundPropertyHighlighter)
 					guiControler.showMessage("Highlight feature values of '" + highlighter + "'" + lastMsg);
 			}
 			guiControler.unblock("set highlighter");
 		}
 	}
 
 	@Override
 	public void setHighlighter(CompoundProperty prop)
 	{
 		Highlighter high = null;
 		for (Highlighter hs[] : highlighters.values())
 		{
 			for (Highlighter h : hs)
 			{
 				if (h instanceof CompoundPropertyHighlighter)
 				{
 					CompoundPropertyHighlighter m = (CompoundPropertyHighlighter) h;
 					if (m.getProperty() == prop)
 					{
 						high = h;
 						break;
 					}
 				}
 			}
 		}
 		if (high != null)
 			setHighlighter(high);
 	}
 
 	@Override
 	public void setHighlighter(SubstructureSmartsType type)
 	{
 		Highlighter high = null;
 		for (Highlighter hs[] : highlighters.values())
 		{
 			for (Highlighter h : hs)
 			{
 				if (h instanceof SubstructureHighlighter)
 				{
 					SubstructureHighlighter m = (SubstructureHighlighter) h;
 					if (m.getType() == type)
 					{
 						high = h;
 						break;
 					}
 				}
 			}
 		}
 		if (high != null)
 			setHighlighter(high);
 	}
 
 	@Override
 	public void setSelectLastSelectedHighlighter()
 	{
 		setHighlighter(lastSelectedHighlighter);
 	}
 
 	@Override
 	public boolean isHighlighterLabelsVisible()
 	{
 		return highlighterLabelsVisible;
 	}
 
 	@Override
 	public void setHighlighterLabelsVisible(boolean selected)
 	{
 		if (this.highlighterLabelsVisible != selected)
 		{
 			highlighterLabelsVisible = selected;
 			updateAllClustersAndCompounds(false);
 			fireViewChange(PROPERTY_HIGHLIGHT_CHANGED);
 			if (selected)
 				guiControler.showMessage("Show label for each compound feature value.");
 			else
 				guiControler.showMessage("Do not show label for each compound feature value.");
 		}
 	}
 
 	@Override
 	public void setHighlightSorting(HighlightSorting sorting)
 	{
 		if (this.highlightSorting != sorting)
 		{
 			highlightSorting = sorting;
 			updateAllClustersAndCompounds(false);
 			fireViewChange(PROPERTY_HIGHLIGHT_CHANGED);
 			if (sorting == HighlightSorting.Max)
 				guiControler
 						.showMessage("Highlight superimposed cluster using the compound with the maximum feature value.");
 			else if (sorting == HighlightSorting.Median)
 				guiControler
 						.showMessage("Highlight superimposed cluster using the compound with the median feature value.");
 			else if (sorting == HighlightSorting.Min)
 				guiControler
 						.showMessage("Highlight superimposed cluster using the compound with the minimum feature value.");
 		}
 	}
 
 	@Override
 	public HighlightSorting getHighlightSorting()
 	{
 		return highlightSorting;
 	}
 
 	private static double boxTranslucency = 0.05;
 
 	/**
 	 * updates cluster with index i
 	 * update is done if i != old index or forceupdate is true
 	 * 
 	 * paints/removes box around cluster
 	 * hides compounds
 	 * 
 	 * @param clusterIndex
 	 * @param forceUpdate
 	 */
 	public void updateCluster(int clusterIndex, boolean forceUpdate)
 	{
 		Cluster c = clustering.getCluster(clusterIndex);
 		int active = clustering.getClusterActive().getSelected();
 		int watched = clustering.getClusterWatched().getSelected();
 
 		boolean watch = active == -1 && clusterIndex == watched;
 
 		if (forceUpdate || watch != c.isWatched())
 		{
 			c.setWatched(watch);
 			if (watch)
 			{
 				view.select(c.getBitSet());
 				view.scriptWait("boundbox { selected }");
 				view.scriptWait("boundbox off");
 				//				view.scriptWait("font bb" + clusterIndex + " " + View.FONT_SIZE);
 				view.scriptWait("draw ID bb" + clusterIndex + " BOUNDBOX color "
 						+ ColorUtil.toJMolString(ComponentFactory.LIST_WATCH_BACKGROUND) + " translucent "
 						+ boxTranslucency + " MESH NOFILL \"" + c.toStringWithValue() + "\"");
 
 				//				jmolPanel.repaint(); // HACK to avoid label display errors
 			}
 			else
 				view.scriptWait("draw bb" + clusterIndex + " off");
 		}
 
 		boolean visible = active == -1 || clusterIndex == active;
 		boolean someCompoundsHidden = visible && active == -1 && clustering.isSuperimposed();
 
 		if (forceUpdate
 				|| visible != c.isVisible()
 				|| selectedHighlightCompoundProperty != c.getHighlightProperty()
 				|| (visible && (someCompoundsHidden != c.someCompoundsHidden() || highlightSorting != c
 						.getHighlightSorting())))
 		{
 			c.setVisible(visible);
 			c.setSomeCompoundsHidden(someCompoundsHidden);
 			c.setHighlighProperty(selectedHighlightCompoundProperty,
 					MainPanel.getHighlightColor(clustering, c, selectedHighlightCompoundProperty));
 			c.setHighlightSorting(highlightSorting);
 			hideOrDisplayCluster(c, visible, someCompoundsHidden);
 		}
 	}
 
 	private void hideOrDisplayCluster(Cluster c, boolean clusterVisible, boolean hideSomeCompounds)
 	{
 		BitSet toDisplay = new BitSet();
 		BitSet toHide = new BitSet();
 		List<Compound> toDisplayCompound = new ArrayList<Compound>();
 		List<Compound> toHideCompound = new ArrayList<Compound>();
 
 		if (!clusterVisible)
 		{
 			toHide.or(c.getBitSet());
 			for (Compound compound : c.getCompounds())
 				toHideCompound.add(compound);
 		}
 		else
 		{
 			if (!hideSomeCompounds)
 			{
 				toDisplay.or(style == Style.dots ? c.getDotModeDisplayBitSet() : c.getBitSet());
 				for (Compound compound : c.getCompounds())
 					toDisplayCompound.add(compound);
 			}
 			else
 			{
 				int numCompounds = c.getCompounds().size();
 				int max;
 				if (numCompounds <= 10)
 					max = 10;
 				else
 					max = (numCompounds - 10) / 3 + 10;
 				max = Math.min(25, max);
 				//			Settings.LOGGER.warn("hiding: ''" + hide + "'', num '" + numCompounds + "', max '" + max + "'");
 
 				if (numCompounds < max)
 					toDisplay.or(style == Style.dots ? c.getDotModeDisplayBitSet() : c.getBitSet());
 				else
 				{
 					List<Compound> compounds;
 					if (selectedHighlighter instanceof CompoundPropertyHighlighter)
 						compounds = c.getCompoundsInOrder(
 								((CompoundPropertyHighlighter) selectedHighlighter).getProperty(), highlightSorting);
 					else
 						compounds = c.getCompounds();
 					int count = 0;
 					for (Compound m : compounds)
 					{
 						if (count++ >= max)
 							toHide.or(style == Style.dots ? m.getDotModeDisplayBitSet() : m.getBitSet());
 						else
 							toDisplay.or(style == Style.dots ? m.getDotModeDisplayBitSet() : m.getBitSet());
 					}
 				}
 			}
 		}
 
 		for (Compound compound : toHideCompound)
 		{
 			if (compound.isShowActiveBox())
 				view.scriptWait("draw bb" + compound.getCompoundIndex() + "a OFF");
 			if (compound.isSphereVisible())
 				view.hideSphere(compound);
 		}
 		view.hide(toHide);
 
 		for (Compound compound : toDisplayCompound)
 		{
 			if (compound.isShowActiveBox())
 				view.scriptWait("draw bb" + compound.getCompoundIndex() + "a ON");
 			if (compound.isSphereVisible())
 				view.showSphere(compound, compound.isLastFeatureSphereVisible(), false);
 		}
 		view.display(toDisplay);
 	}
 
 	/**
 	 * udpates all clusters
 	 */
 	private void updateAllClustersAndCompounds(boolean forceUpdate)
 	{
 		int a = getClustering().getClusterActive().getSelected();
 		for (int j = 0; j < clustering.numClusters(); j++)
 		{
 			updateCluster(j, forceUpdate);
 			if (a == -1 || a == j)
 				for (Compound m : clustering.getCluster(j).getCompounds())
 					updateCompound(m.getCompoundIndex(), forceUpdate);
 		}
 	}
 
 	/**
 	 * udpates single compound
 	 * forceUpdate = true -> everything is reset (independent of compound is part of active cluster or if single props have changed)
 	 * 
 	 * shows/hides box around compound
 	 * show/hides compound label
 	 * set compound translucent/opaque
 	 * highlight substructure in compound 
 	 */
 	private void updateCompound(int compoundIndex, boolean forceUpdate)
 	{
 		int clus = clustering.getClusterIndexForCompoundIndex(compoundIndex);
 		Cluster c = clustering.getCluster(clus);
 		Compound m = clustering.getCompoundWithCompoundIndex(compoundIndex);
 		if (m == null)
 		{
 			Settings.LOGGER.warn("compound is null!");
 			return;
 		}
 		int activeCluster = clustering.getClusterActive().getSelected();
 		int watchedCluster = clustering.getClusterWatched().getSelected();
 
 		boolean showHoverBox = false;
 		boolean showActiveBox = false;
 		boolean showLabel = false;
 		boolean translucent = false;
 
 		// inside the active cluster
 		if (clus == activeCluster)
 		{
 			if (hideCompounds == HideCompounds.nonActive)
 			{
 				if (clustering.isCompoundActiveFromCluster(clus)
 						&& !clustering.getCompoundWatched().isSelected(compoundIndex)
 						&& !clustering.getCompoundActive().isSelected(compoundIndex))
 					translucent = true;
 			}
 			else if (hideCompounds == HideCompounds.nonWatched)
 			{
 				if ((clustering.isCompoundWatchedFromCluster(clus) || clustering.isCompoundActiveFromCluster(clus))
 						&& !clustering.getCompoundWatched().isSelected(compoundIndex)
 						&& !clustering.getCompoundActive().isSelected(compoundIndex))
 					translucent = true;
 			}
 
 			if (selectedHighlighter instanceof CompoundPropertyHighlighter)
 				showLabel = true;
 
 			if (clustering.getCompoundWatched().isSelected(compoundIndex) && !c.isSuperimposed())
 				showHoverBox = true;
 			if (clustering.getCompoundActive().isSelected(compoundIndex) && !c.isSuperimposed())
 				showActiveBox = true;
 		}
 		else
 		{
 			List<Compound> compounds;
 			if (selectedHighlighter instanceof CompoundPropertyHighlighter)
 				compounds = c.getCompoundsInOrder(((CompoundPropertyHighlighter) selectedHighlighter).getProperty(),
 						highlightSorting);
 			else
 				compounds = c.getCompounds();
 
 			if (selectedHighlightCompoundProperty != null
 					&& (compounds.indexOf(m) == 0 || !clustering.isSuperimposed()))
 				showLabel = true;
 
 			if (clustering.isSuperimposed())
 				translucent = (compounds.indexOf(m) > 0);
 			else if (hideCompounds != HideCompounds.none)
 			{
 				if (clustering.isCompoundWatched() || clustering.isCompoundActive())
 				{
 					translucent = !(clustering.getCompoundWatched().isSelected(compoundIndex) || clustering
 							.getCompoundActive().isSelected(compoundIndex));
 				}
 				else if (watchedCluster == -1 || selectedHighlighter == Highlighter.CLUSTER_HIGHLIGHTER)
 					translucent = false;
 				else
 					translucent = (clus != watchedCluster);
 			}
 
 			if (clustering.getCompoundWatched().isSelected(compoundIndex) && !c.isSuperimposed())
 				showHoverBox = true;
 			if (clustering.getCompoundActive().isSelected(compoundIndex) && !c.isSuperimposed())
 				showActiveBox = true;
 		}
 
 		String smarts = null;
 		if (selectedHighlighter instanceof SubstructureHighlighter)
 			smarts = c.getSubstructureSmarts(((SubstructureHighlighter) selectedHighlighter).getType());
 		else if (selectedHighlighter instanceof CompoundPropertyHighlighter
 				&& ((CompoundPropertyHighlighter) selectedHighlighter).getProperty().isSmartsProperty())
 			smarts = ((CompoundPropertyHighlighter) selectedHighlighter).getProperty().getSmarts();
 
 		if (!highlighterLabelsVisible)
 			showLabel = false;
 
 		Color highlightColor = getHighlightColor(m, selectedHighlighter, selectedHighlightCompoundProperty);
 		String highlightColorString = highlightColor == null ? null : "color " + ColorUtil.toJMolString(highlightColor);
 		String compoundColor;
 		if (highlightMode == HighlightMode.Spheres)
 			compoundColor = "color cpk";
 		else
 			compoundColor = highlightColor == null ? "color cpk" : "color " + ColorUtil.toJMolString(highlightColor);
 		if (compoundColor.equals("color cpk") && style == Style.dots)
 			compoundColor = "color "
 					+ ColorUtil.toJMolString(isBlackgroundBlack() ? Color.LIGHT_GRAY.brighter() : Color.GRAY);
 
 		boolean sphereVisible = (highlightMode == HighlightMode.Spheres && highlightColorString != null);
 		boolean lastFeatureSphereVisible = sphereVisible && highlightLastFeatureEnabled;
 
 		Translucency translucency;
 		if (translucent)
 		{
 			if (clustering.isSuperimposed())
 			{
 				translucency = Translucency.Strong;
 				if (highlightMode == HighlightMode.Spheres)
 					sphereVisible = false;
 			}
 			//			else if (hideCompounds != HideCompounds.none && ())
 			//			else if (clus  hideCompounds (hideCompounds == HideCompounds.nonActive && clustering.getCompoundActive().getSelected() != -1)
 			//					|| (hideCompounds == HideCompounds.nonWatched && clustering.getCompoundWatched().getSelected() != -1))
 			else
 			{
 				if (clus == activeCluster)
 				{
 					if (c.size() <= 5)
 						translucency = Translucency.ModerateWeak;
 					else if (c.size() <= 15)
 						translucency = Translucency.ModerateStrong;
 					else
 						translucency = Translucency.Strong;
 				}
 				else
 					translucency = Translucency.ModerateStrong;
 			}
 			//			else
 			//				translucency = Translucency.None;
 		}
 		else
 			translucency = Translucency.None;
 
 		boolean styleUpdate = style != m.getStyle();
 		boolean styleDotUpdate = (style == Style.dots && m.getStyle() != Style.dots)
 				|| (style != Style.dots && m.getStyle() == Style.dots);
 		boolean compoundUpdate = styleUpdate || translucency != m.getTranslucency()
 				|| !ObjectUtil.equals(compoundColor, m.getCompoundColor());
 		//showLabel is enough because superimpose<->not-superimpose is not tracked in compound
 		boolean checkLabelUpdate = showLabel || (!showLabel && m.getLabel() != null)
 				|| !ObjectUtil.equals(compoundColor, m.getCompoundColor())
 				|| selectedHighlightCompoundProperty != m.getHighlightCompoundProperty();
 		boolean sphereUpdate = sphereVisible != m.isSphereVisible()
 				|| (lastFeatureSphereVisible != m.isLastFeatureSphereVisible())
 				|| (sphereVisible && (translucency != m.getTranslucency() || !ObjectUtil.equals(highlightColorString,
 						m.getHighlightColorString())));
 		boolean spherePositionUpdate = sphereVisible && !ObjectUtil.equals(m.getPosition(), m.getSpherePosition());
 		boolean hoverBoxUpdate = showHoverBox != m.isShowHoverBox();
 		boolean activeBoxUpdate = showActiveBox != m.isShowActiveBox();
 		boolean smartsUpdate = compoundUpdate || smarts != m.getHighlightedSmarts();
 
 		m.setCompoundColor(compoundColor);
 		m.setStyle(style);
 		m.setTranslucency(translucency);
 		m.setHighlightCompoundProperty(selectedHighlightCompoundProperty);
 		m.setHighlightColor(highlightColorString, highlightColor);
 		m.setSpherePosition(m.getPosition());
 		m.setSphereVisible(sphereVisible);
 		m.setLastFeatureSphereVisible(lastFeatureSphereVisible);
 		m.setShowHoverBox(showHoverBox);
 		m.setShowActiveBox(showActiveBox);
 
 		if (styleDotUpdate)
 		{
 			if (style == Style.dots)
 			{
 				view.hide(m.getDotModeHideBitSet());
 				clustering.moveForDotMode(m, true);
 			}
 			else
 			{
 				clustering.moveForDotMode(m, false);
 				view.display(m.getDotModeHideBitSet());
 			}
 		}
 
 		// SET SELECTION
 		if (style == Style.dots)
 			view.select(m.getDotModeDisplayBitSet());
 		else
 			view.select(m.getBitSet());
 
 		if (forceUpdate || styleUpdate)
 			view.scriptWait(getStyleString());
 
 		if (forceUpdate || compoundUpdate)
 			view.scriptWait(compoundColor + getColorSuffixTranslucent(translucency));
 
 		if (forceUpdate || sphereUpdate || spherePositionUpdate)
 		{
 			if (sphereVisible)
 				view.showSphere(m, lastFeatureSphereVisible, forceUpdate || spherePositionUpdate);
 			else
 				view.hideSphere(m);
 		}
 
 		if (forceUpdate || hoverBoxUpdate)
 		{
 			if (showHoverBox)
 			{
 				if (style == Style.dots)
 					view.scriptWait("boundbox { selected } { 2 2 2 }");
 				else
 					view.scriptWait("boundbox { selected }");
 				view.scriptWait("boundbox off");
 				//				view.scriptWait("font bb" + m.getCompoundIndex() + "h " + View.FONT_SIZE);
 				view.scriptWait("draw ID bb" + m.getCompoundIndex() + "h BOUNDBOX color "
 						+ ColorUtil.toJMolString(ComponentFactory.LIST_WATCH_BACKGROUND) + " translucent "
 						+ boxTranslucency + " MESH NOFILL \"" + m.toStringWithValue() + "\"");
 
 				//				jmolPanel.repaint(); // HACK to avoid label display errors
 			}
 			else
 				view.scriptWait("draw bb" + m.getCompoundIndex() + "h OFF");
 		}
 
 		if (forceUpdate || activeBoxUpdate)
 		{
 			if (showActiveBox)
 			{
 				if (style == Style.dots)
 					view.scriptWait("boundbox { selected } { 2 2 2 }");
 				else
 					view.scriptWait("boundbox { selected }");
 				view.scriptWait("boundbox off");
 				//				view.scriptWait("font bb" + m.getCompoundIndex() + "a " + View.FONT_SIZE);
 				view.scriptWait("draw ID bb" + m.getCompoundIndex() + "a BOUNDBOX color "
 						+ ColorUtil.toJMolString(ComponentFactory.LIST_ACTIVE_BACKGROUND) + " translucent "
 						+ boxTranslucency + " MESH NOFILL");
 
 				//				jmolPanel.repaint(); // HACK to avoid label display errors
 			}
 			else
 			{
 				view.scriptWait("draw bb" + m.getCompoundIndex() + "a OFF");
 			}
 		}
 
 		// CHANGES JMOL SELECTION !!!
 		if (forceUpdate || smartsUpdate)
 		{
 			boolean match = false;
 			if (smarts != null && smarts.length() > 0)
 			{
 				BitSet matchBitSet = m.getSmartsMatch(smarts);
 				if (matchBitSet.cardinality() > 0)
 				{
 					match = true;
 					m.setHighlightedSmarts(smarts);
 					view.select(matchBitSet);
 					view.scriptWait("color " + View.convertColor(matchColor) + getColorSuffixTranslucent(translucency));
 				}
 			}
 			if (!match)// || forceUpdate || translucentUpdate)
 			{
 				m.setHighlightedSmarts(null);
 				if (highlightMode == HighlightMode.Spheres)
 					view.scriptWait("color cpk" + getColorSuffixTranslucent(translucency));
 				else
 					view.scriptWait(compoundColor + getColorSuffixTranslucent(translucency));
 			}
 		}
 
 		// MAY CHANGE JMOL SELECTION !!!
 		if (forceUpdate || checkLabelUpdate)
 		{
 			String labelString = null;
 			if (showLabel)
 			{
 				if (activeCluster == -1 && clustering.isSuperimposed())
 				{
 					labelString = c.getName() + " - "
 							+ ((CompoundPropertyHighlighter) selectedHighlighter).getProperty() + ": "
 							+ c.getSummaryStringValue(selectedHighlightCompoundProperty, false);
 				}
 				else
 				{
 					CompoundProperty p = ((CompoundPropertyHighlighter) selectedHighlighter).getProperty();
 					Object val = clustering.getCompoundWithCompoundIndex(compoundIndex).getFormattedValue(p);
 					//				Settings.LOGGER.warn("label : " + i + " : " + c + " : " + val);
 					labelString = ((CompoundPropertyHighlighter) selectedHighlighter).getProperty() + ": " + val;
 				}
 			}
 
 			// CHANGES JMOL SELECTION!!
 			if (forceUpdate || !ObjectUtil.equals(labelString, m.getLabel()))
 			{
 				m.setLabel(labelString);
 
 				view.selectFirstCarbonAtom(m.getBitSet());
 				//				BitSet empty = new BitSet(bs.length());
 				//				empty.set(bs.nextSetBit(0));
 				//				view.select(empty);
 
 				if (showLabel)
 				{
 					view.scriptWait("set fontSize " + ScreenSetup.INSTANCE.getFontSize());
 					view.scriptWait("label \"" + labelString + "\"");
 				}
 				else
 				{
 					//				Settings.LOGGER.warn("label : " + i + " : " + c + " : off");
 					view.scriptWait("label OFF");
 				}
 			}
 		}
 	}
 
 	public void init(ClusteringData clusteredDataset)
 	{
 		if (clustering.numClusters() > 0)
 		{
 			View.instance.suspendAnimation("clearing");
 			clustering.clear();
 			View.instance.proceedAnimation("clearing");
 		}
 		clustering.newClustering(clusteredDataset);
 		clustering.computeSpecifities();
 
 		clustering.getClusterActive().addListenerFirst(new PropertyChangeListener()
 		{
 			@Override
 			public void propertyChange(PropertyChangeEvent e)
 			{
 				int cIndexOldArray[] = ((int[]) e.getOldValue());
 				int cIndexOld = cIndexOldArray.length == 0 ? -1 : cIndexOldArray[0];
 				updateClusterSelection(clustering.getClusterActive().getSelected(), cIndexOld, true);
 			}
 		});
 		clustering.getClusterWatched().addListenerFirst(new PropertyChangeListener()
 		{
 			@Override
 			public void propertyChange(PropertyChangeEvent e)
 			{
 				int cIndexOldArray[] = ((int[]) e.getOldValue());
 				int cIndexOld = cIndexOldArray.length == 0 ? -1 : cIndexOldArray[0];
 				updateClusterSelection(clustering.getClusterWatched().getSelected(), cIndexOld, false);
 			}
 		});
 		clustering.getCompoundActive().addListenerFirst(new PropertyChangeListener()
 		{
 			@Override
 			public void propertyChange(PropertyChangeEvent e)
 			{
 				updateCompoundActiveSelection(((int[]) e.getNewValue()), ((int[]) e.getOldValue()), clustering
 						.getCompoundActive().isExclusiveSelection());
 			}
 		});
 		clustering.getCompoundWatched().addListenerFirst(new PropertyChangeListener()
 		{
 			@Override
 			public void propertyChange(PropertyChangeEvent e)
 			{
 				//				int mIndexOldArray[] = ((int[]) e.getOldValue());
 				//				int mIndexOld = mIndexOldArray.length == 0 ? -1 : mIndexOldArray[0];
 				updateCompoundWatchedSelection();//clustering.getCompoundWatched().getSelected(), mIndexOld);
 			}
 		});
 		clustering.addListener(new PropertyChangeListener()
 		{
 			@Override
 			public void propertyChange(PropertyChangeEvent evt)
 			{
 				if (evt.getPropertyName().equals(Clustering.CLUSTER_ADDED))
 					updateClusteringNew();
 				else if (evt.getPropertyName().equals(Clustering.CLUSTER_REMOVED))
 					updateClusterRemoved();
 				else if (evt.getPropertyName().equals(Clustering.CLUSTER_MODIFIED))
 					updateClusterModified();
 			}
 		});
 		updateClusteringNew();
 
 	}
 
 	private void updateClusteringNew()
 	{
 		Highlighter[] h = new Highlighter[] { Highlighter.DEFAULT_HIGHLIGHTER };
 		if (clustering.getNumClusters() > 1)
 			h = ArrayUtil.concat(Highlighter.class, h, new Highlighter[] { Highlighter.CLUSTER_HIGHLIGHTER });
 		if (clustering.getEmbeddingQualityProperty() != null)
 			h = ArrayUtil.concat(Highlighter.class, h,
 					new Highlighter[] { new CompoundPropertyHighlighter(clustering.getEmbeddingQualityProperty()) });
 
 		if (clustering.getDistanceToProperties() != null)
 			for (CompoundProperty p : clustering.getDistanceToProperties())
 				h = ArrayUtil.concat(Highlighter.class, h, new Highlighter[] { new CompoundPropertyHighlighter(p) });
 
 		if (clustering.getSubstructures().size() > 0)
 			for (SubstructureSmartsType type : clustering.getSubstructures())
 				h = ArrayUtil.concat(Highlighter.class, h, new Highlighter[] { new SubstructureHighlighter(type) });
 		highlighters = new LinkedHashMap<String, Highlighter[]>();
 		highlighters.put("", h);
 
 		List<CompoundProperty> props = clustering.getProperties();
 		CompoundPropertyHighlighter[] featureHighlighters = new CompoundPropertyHighlighter[props.size()];
 		int fCount = 0;
 		for (CompoundProperty p : props)
 			featureHighlighters[fCount++] = new CompoundPropertyHighlighter(p);
 		highlighters.put("Features NOT used for mapping", featureHighlighters);
 
 		props = clustering.getFeatures();
 		featureHighlighters = new CompoundPropertyHighlighter[props.size()];
 		fCount = 0;
 		for (CompoundProperty p : props)
 			featureHighlighters[fCount++] = new CompoundPropertyHighlighter(p);
 		highlighters.put("Features used for mapping", featureHighlighters);
 
 		fireViewChange(PROPERTY_NEW_HIGHLIGHTERS);
 
 		updateAllClustersAndCompounds(true);
 
 		view.evalString("frame " + view.getCompoundNumberDotted(0) + " "
 				+ view.getCompoundNumberDotted(clustering.numCompounds() - 1));
 
 		setSpinEnabled(isSpinEnabled(), true);
 
 		initHighlighter();
 		initCompoundDescriptor();
 
 		view.suspendAnimation("new clustering");
 		if (!isAllClustersSpreadable())
 			setSuperimpose(true);
 		if (clustering.getNumClusters() == 1)
 			clustering.getClusterActive().setSelected(0);
 		else
 			view.zoomTo(clustering, null);
 		view.proceedAnimation("new clustering");
 	}
 
 	private void initHighlighter()
 	{
 		if (clustering.getNumClusters() == 1)
 			setHighlighter(Highlighter.DEFAULT_HIGHLIGHTER, false);
 		else
 			setHighlighter(Highlighter.CLUSTER_HIGHLIGHTER, false);
 		highlightAutomatic.init();
 	}
 
 	private void updateClusterRemoved()
 	{
 		updateAllClustersAndCompounds(true);
 		if (clustering.getNumClusters() == 0 && selectedHighlightCompoundProperty != null)
 			initHighlighter();
 		if (clustering.getNumClusters() == 1)
 			clustering.getClusterActive().setSelected(0);
 	}
 
 	private void updateClusterModified()
 	{
 		updateAllClustersAndCompounds(true);
 	}
 
 	private void updateCompoundWatchedSelection()//int mIndex, int mIndexOld)
 	{
 		//		Settings.LOGGER.println("update compound active: " + active + " " + ArrayUtil.toString(mIndex));
 		int activeCluster = clustering.getClusterActive().getSelected();
 		Iterable<Compound> compounds;
 		if (activeCluster != -1)
 			compounds = clustering.getCluster(activeCluster).getCompounds();
 		else
 			compounds = clustering.getCompounds(true);
 		for (Compound m : compounds)
 			updateCompound(m.getCompoundIndex(), false);
 	}
 
 	private void updateCompoundActiveSelection(int mIndex[], int mIndexOld[], boolean zoomIntoCompound)
 	{
 		//		Settings.LOGGER.println("update compound active: " + active + " " + ArrayUtil.toString(mIndex));
 		int activeCluster = clustering.getClusterActive().getSelected();
 		Iterable<Compound> compounds;
 		if (activeCluster != -1)
 			compounds = clustering.getCluster(activeCluster).getCompounds();
 		else
 			compounds = clustering.getCompounds(true);
 		for (Compound m : compounds)
 			updateCompound(m.getCompoundIndex(), false);
 
 		if (/*activeCluster != -1 &&*/!clustering.isSuperimposed())
 		{
 			// zoom because compound selection has changed
 			if (mIndex.length != 0 && zoomIntoCompound)
 			{
 				// a compound selected
 				if (mIndex.length == 1)
 				{
 					final Compound m = clustering.getCompoundWithCompoundIndex(mIndex[0]);
 					view.zoomTo(m, AnimationSpeed.SLOW);
 					guiControler.showMessage("Zoom to compound '" + m + "'.");
 				}
 			}
 			else if (mIndex.length == 0 && mIndexOld.length > 0 && activeCluster != -1)
 			{
 				final Cluster c = getClustering().getClusterForCompoundIndex(mIndexOld[0]);
 				view.zoomTo(c, AnimationSpeed.SLOW);
 				if (clustering.getNumClusters() == 1)
 					guiControler.showMessage("Zoom out to show all compounds.");
 				else
 					guiControler.showMessage("Zoom to cluster '" + c + "'.");
 			}
 		}
 	}
 
 	/**
 	 * the cluster selection has changed
 	 * active=true -> the selection change is about active/inactive
 	 * active=false -> the selection change is about watched/not-watched
 	 * 
 	 * @param cIndex
 	 * @param cIndexOld
 	 * @param activeClusterChanged
 	 */
 	private void updateClusterSelection(int cIndex, int cIndexOld, final boolean activeClusterChanged)
 	{
 		// ignore watch updates when a cluster is active
 		if (!activeClusterChanged && clustering.isClusterActive())
 			return;
 
 		//		Settings.LOGGER.info("updating cluster selection: " + cIndex + " " + cIndexOld + " " + activeClusterChanged);
 
 		highlightAutomatic.resetClusterHighlighter(activeClusterChanged);
 
 		updateAllClustersAndCompounds(false);
 		if (activeClusterChanged)
 		{
 			updateSuperimpose(false);
 			if (cIndex == -1)
 				guiControler.showMessage("Zoom out to show all clusters.");
 			else if (clustering.getNumClusters() == 1)
 				guiControler.showMessage("Zoom out to show all compounds.");
 			else
 				guiControler.showMessage("Zoom to cluster '" + clustering.getCluster(cIndex) + "'.");
 		}
 
 		view.afterAnimation(new Runnable()
 		{
 			@Override
 			public void run()
 			{
 				highlightAutomatic.resetDefaultHighlighter(activeClusterChanged);
 			}
 		}, "highlight automatic after zooming and superimposing");
 	}
 
 	static class JmolPanel extends JPanel
 	{
 		JmolSimpleViewer viewer;
 		JmolAdapter adapter;
 
 		JmolPanel()
 		{
 			adapter = new SmarterJmolAdapter();
 			viewer = JmolSimpleViewer.allocateSimpleViewer(this, adapter);
 		}
 
 		public JmolSimpleViewer getViewer()
 		{
 			return viewer;
 		}
 
 		final Dimension currentSize = new Dimension();
 		final Rectangle rectClip = new Rectangle();
 
 		final Dimension dimSize = new Dimension();
 
 		public void paint(Graphics g)
 		{
 			////code for old version
 			//			getSize(currentSize);
 			//			g.getClipBounds(rectClip);
 			//			if (g != null && currentSize != null && rectClip != null)
 			//				viewer.renderScreenImage(g, currentSize, rectClip);
 
 			getSize(dimSize);
 			if (dimSize.width == 0)
 				return;
 			viewer.renderScreenImage(g, dimSize.width, dimSize.height);
 		}
 	}
 
 	@Override
 	public boolean isSuperimpose()
 	{
 		return clustering.isSuperimposed();
 	}
 
 	@Override
 	public void setSuperimpose(boolean superimpose)
 	{
 		if (clustering.isSuperimposed() != superimpose)
 		{
 			clustering.setSuperimposed(superimpose);
 			updateSuperimpose(true);
 			if (superimpose)
 				guiControler.showMessage("Move compounds to cluster center.");
 			else
 				guiControler.showMessage("Move compounds to compound positions.");
 		}
 	}
 
 	@Override
 	public boolean isAllClustersSpreadable()
 	{
 		for (Cluster c : clustering.getClusters())
 			if (c.isSpreadable())
 				return true;
 		return false;
 	}
 
 	@Override
 	public boolean isSingleClusterSpreadable()
 	{
 		if (!clustering.isClusterActive())
 			throw new IllegalStateException();
 		return clustering.getCluster(clustering.getClusterActive().getSelected()).isSpreadable();
 	}
 
 	@Override
 	public boolean isAntialiasEnabled()
 	{
 		return antialiasEnabled;
 	}
 
 	@Override
 	public void setAntialiasEnabled(boolean b)
 	{
 		if (this.antialiasEnabled != b)
 		{
 			this.antialiasEnabled = b;
 			View.instance.setAntialiasOn(antialiasEnabled);
 			fireViewChange(PROPERTY_ANTIALIAS_CHANGED);
 			guiControler.showMessage((b ? "Enable" : "Disable") + " antialiasing.");
 		}
 	}
 
 	private void updateSuperimpose(boolean animateSuperimpose)
 	{
 		guiControler.block("superimposing and zooming");
 
 		final List<Cluster> c = new ArrayList<Cluster>();
 		final Cluster activeCluster;
 		boolean superimpose = clustering.isSuperimposed();
 
 		final boolean setAntialiasBackOn;
 		if (antialiasEnabled && view.isAntialiasOn())
 		{
 			setAntialiasBackOn = true;
 			view.setAntialiasOn(false);
 		}
 		else
 			setAntialiasBackOn = false;
 
 		if (clustering.isClusterActive())
 		{
 			activeCluster = clustering.getCluster(clustering.getClusterActive().getSelected());
 			if (activeCluster.isSuperimposed() != superimpose)
 				c.add(activeCluster);
 		}
 		else
 		{
 			for (Cluster cluster : clustering.getClusters())
 				if (cluster.isSuperimposed() != superimpose)
 					c.add(cluster);
 			activeCluster = null;
 		}
 
 		clustering.getCompoundWatched().clearSelection();
 
 		if (!superimpose && animateSuperimpose) // zoom out before spreading (explicitly fetch non-superimpose diameter)
 		{
 			if (activeCluster == null)
 			{
 				for (Cluster cluster : c) // paint solid before spreading out
 				{
 					updateCluster(clustering.indexOf(cluster), false);
 					for (Compound m : cluster.getCompounds())
 						updateCompound(m.getCompoundIndex(), false);
 				}
 				view.zoomTo(clustering, AnimationSpeed.SLOW, false);
 			}
 			else
 				view.zoomTo(activeCluster, AnimationSpeed.SLOW, false);
 		}
 
		if (animateSuperimpose) // for superimposition or un-superimposition, hide shperes manually before moving compounds
			for (Compound compound : clustering.getCompounds(true))
				if (compound.isSphereVisible())
				{
					compound.setSphereVisible(false);
					view.hideSphere(compound);
				}
 
 		if (c.size() > 0)
 		{
 			if (!animateSuperimpose)
 				view.suspendAnimation("superimpose");
 			clustering.setClusterOverlap(c, superimpose, View.AnimationSpeed.SLOW);
 			if (!animateSuperimpose)
 				view.proceedAnimation("superimpose");
 		}
 
 		if (superimpose || !animateSuperimpose) // zoom in after superimposing
 		{
 			final Zoomable zoom = activeCluster == null ? clustering : activeCluster;
 			view.afterAnimation(new Runnable()
 			{
 				@Override
 				public void run()
 				{
 					view.zoomTo(zoom, AnimationSpeed.SLOW);
 				}
 			}, "zoom to " + zoom);
 		}
 
 		view.afterAnimation(new Runnable()
 		{
 			@Override
 			public void run()
 			{
 				for (final Cluster cluster : c)
 				{
 					updateCluster(clustering.indexOf(cluster), false);
 
 					for (Compound m : cluster.getCompounds())
 						updateCompound(m.getCompoundIndex(), false);
 				}
 				if (setAntialiasBackOn && !view.isAntialiasOn())
 				{
 					ThreadUtil.sleep(200);
 					view.setAntialiasOn(true);
 				}
 			}
 		}, "set compounds translucent in clusters");
 
 		view.afterAnimation(new Runnable()
 		{
 			@Override
 			public void run()
 			{
 				guiControler.unblock("superimposing and zooming");
 				fireViewChange(PROPERTY_SUPERIMPOSE_CHANGED);
 				fireViewChange(PROPERTY_DENSITY_CHANGED);
 			}
 		}, "do unblock wenn all done");
 	}
 
 	List<PropertyChangeListener> viewListeners = new ArrayList<PropertyChangeListener>();
 
 	@Override
 	public void addViewListener(PropertyChangeListener l)
 	{
 		viewListeners.add(l);
 	}
 
 	private void fireViewChange(String prop)
 	{
 		for (PropertyChangeListener l : viewListeners)
 			l.propertyChange(new PropertyChangeEvent(this, prop, "old", "new"));
 	}
 
 	@Override
 	public boolean canChangeCompoundSize(boolean larger)
 	{
 		if (larger && ClusteringUtil.COMPOUND_SIZE == ClusteringUtil.COMPOUND_SIZE_MAX)
 			return false;
 		if (!larger && ClusteringUtil.COMPOUND_SIZE == 0)
 			return false;
 		Cluster activeCluster = clustering.getCluster(clustering.getClusterActive().getSelected());
 		if (activeCluster == null)
 			return true;
 		else
 			return !clustering.isSuperimposed();
 	}
 
 	@Override
 	public void changeCompoundSize(boolean larger)
 	{
 		if (larger && ClusteringUtil.COMPOUND_SIZE < ClusteringUtil.COMPOUND_SIZE_MAX)
 		{
 			ClusteringUtil.COMPOUND_SIZE++;
 			updateDensitiy(true);
 		}
 		else if (!larger && ClusteringUtil.COMPOUND_SIZE > 0)
 		{
 			ClusteringUtil.COMPOUND_SIZE--;
 			updateDensitiy(false);
 		}
 	}
 
 	@Override
 	public void setCompoundSize(int compoundSize)
 	{
 		if (ClusteringUtil.COMPOUND_SIZE != compoundSize)
 		{
 			boolean increased = ClusteringUtil.COMPOUND_SIZE < compoundSize;
 			ClusteringUtil.COMPOUND_SIZE = compoundSize;
 			updateDensitiy(increased);
 		}
 	}
 
 	@Override
 	public HighlightMode getHighlightMode()
 	{
 		return highlightMode;
 	}
 
 	@Override
 	public void setHighlightMode(HighlightMode mode)
 	{
 		if (highlightMode != mode)
 		{
 			highlightMode = mode;
 			if (selectedHighlighter != Highlighter.DEFAULT_HIGHLIGHTER)
 				updateAllClustersAndCompounds(true);
 			fireViewChange(PROPERTY_HIGHLIGHT_MODE_CHANGED);
 			if (mode == HighlightMode.Spheres)
 				guiControler.showMessage("Highlight compound feature values with spheres.");
 			else if (mode == HighlightMode.ColorCompounds)
 				guiControler.showMessage("Highlight compound feature values by changing atom and bond colors.");
 		}
 	}
 
 	@Override
 	public boolean isHighlightLastFeatureEnabled()
 	{
 		return highlightLastFeatureEnabled;
 	}
 
 	@Override
 	public void setHighlightLastFeatureEnabled(boolean b)
 	{
 		if (highlightLastFeatureEnabled != b)
 		{
 			highlightLastFeatureEnabled = b;
 			if (highlightMode == HighlightMode.Spheres && selectedHighlighter != Highlighter.DEFAULT_HIGHLIGHTER)
 				updateAllClustersAndCompounds(true);
 			fireViewChange(PROPERTY_HIGHLIGHT_LAST_FEATURE);
 			String lastMsg = ".";
 			if (highlightLastFeatureEnabled && lastSelectedHighlighter != null
 					&& lastSelectedHighlighter != Highlighter.DEFAULT_HIGHLIGHTER)
 			{
 				if (lastSelectedHighlighter == Highlighter.CLUSTER_HIGHLIGHTER)
 					lastMsg = " (flat sphere highlights cluster assignement).";
 				else if (lastSelectedHighlighter instanceof CompoundPropertyHighlighter)
 					lastMsg = " (flat sphere highlights '" + lastSelectedHighlighter + "').";
 			}
 			guiControler.showMessage((b ? "Enable" : "Disable") + " highlighting of last selected feature" + lastMsg);
 		}
 	}
 
 	@Override
 	public void setSphereSize(double size)
 	{
 		if (view.sphereSize != size)
 		{
 			boolean increase = view.sphereSize < size;
 			view.sphereSize = size;
 			if (highlightMode == HighlightMode.Spheres && selectedHighlighter != Highlighter.DEFAULT_HIGHLIGHTER)
 				updateAllClustersAndCompounds(true);
 			guiControler.showMessage((increase ? "Increase" : "Descrease") + " sphere size to "
 					+ StringUtil.formatDouble(size) + ".");
 		}
 	}
 
 	@Override
 	public void setSphereTranslucency(double translucency)
 	{
 		if (view.sphereTranslucency != translucency)
 		{
 			boolean increase = view.sphereTranslucency < translucency;
 			view.sphereTranslucency = translucency;
 			if (highlightMode == HighlightMode.Spheres && selectedHighlighter != Highlighter.DEFAULT_HIGHLIGHTER)
 				updateAllClustersAndCompounds(true);
 			guiControler.showMessage((increase ? "Increase" : "Descrease") + " sphere translucency to "
 					+ StringUtil.formatDouble(translucency) + ".");
 		}
 	}
 
 	@Override
 	public int getCompoundSizeMax()
 	{
 		return ClusteringUtil.COMPOUND_SIZE_MAX;
 	}
 
 	@Override
 	public int getCompoundSize()
 	{
 		return ClusteringUtil.COMPOUND_SIZE;
 	}
 
 	private void updateDensitiy(boolean increased)
 	{
 		Cluster activeCluster = clustering.getCluster(clustering.getClusterActive().getSelected());
 		if (activeCluster != null && clustering.isSuperimposed())
 			throw new IllegalStateException("does not make sense, because superimposed!");
 
 		if (activeCluster != null)
 		{
 			if (clustering.isCompoundActive())
 				clustering.getCompoundActive().clearSelection();
 		}
 
 		//		if (higher)
 		//			ClusteringUtil.DENSITY = ClusteringUtil.DENSITY - 0.1f;
 		//		else
 		//			ClusteringUtil.DENSITY = ClusteringUtil.DENSITY + 0.1f;
 
 		view.suspendAnimation("change density");
 		clustering.updatePositions();
 		if (activeCluster != null)
 		{
 			Settings.LOGGER.info("zooming out - cluster");
 			view.zoomTo(activeCluster, null);
 		}
 		else
 		{
 			Settings.LOGGER.info("zooming out - home");
 			view.zoomTo(clustering, null);
 		}
 		for (Compound compound : clustering.getCompounds(true))
 			if (compound.isSphereVisible())
 				view.showSphere(compound, compound.isLastFeatureSphereVisible(), true);
 		view.proceedAnimation("change density");
 
 		fireViewChange(PROPERTY_DENSITY_CHANGED);
 		guiControler.showMessage((increased ? "Increase" : "Descrease") + " compound size to "
 				+ ClusteringUtil.COMPOUND_SIZE + ".");
 	}
 
 	@Override
 	public HideCompounds getHideCompounds()
 	{
 		return hideCompounds;
 	}
 
 	@Override
 	public void setHideCompounds(HideCompounds hide)
 	{
 		if (this.hideCompounds != hide)
 		{
 			hideCompounds = hide;
 			updateAllClustersAndCompounds(false);
 			fireViewChange(PROPERTY_HIDE_UNSELECT_CHANGED);
 			if (hide == HideCompounds.none)
 				guiControler.showMessage("Never draw un-selected compounds translucent.");
 			else if (hide == HideCompounds.nonActive)
 				guiControler
 						.showMessage("Draw un-selected compounds translucent when another compound is selected (with mouse click).");
 			else if (hide == HideCompounds.nonWatched)
 				guiControler
 						.showMessage("Draw un-selected compounds translucent when another compound is focused (with mouse over).");
 		}
 	}
 
 	@Override
 	public boolean isHideHydrogens()
 	{
 		return hideHydrogens;
 	}
 
 	@Override
 	public void setHideHydrogens(boolean b)
 	{
 		this.hideHydrogens = b;
 		view.hideHydrogens(b);
 		if (b)
 			guiControler.showMessage("Hide hydrogens.");
 		else
 			guiControler.showMessage("Draw hydrogens (if available).");
 	}
 
 	@Override
 	public boolean isBlackgroundBlack()
 	{
 		return ComponentFactory.isBackgroundBlack();
 	}
 
 	@Override
 	public void setBackgroundBlack(boolean backgroundBlack)
 	{
 		setBackgroundBlack(backgroundBlack, false);
 	}
 
 	public void setBackgroundBlack(boolean backgroundBlack, boolean forceUpdate)
 	{
 		if (backgroundBlack != ComponentFactory.isBackgroundBlack() || forceUpdate)
 		{
 			ComponentFactory.setBackgroundBlack(backgroundBlack);
 			view.setBackground(ComponentFactory.BACKGROUND);
 			updateAllClustersAndCompounds(false);
 			fireViewChange(PROPERTY_BACKGROUND_CHANGED);
 			guiControler.showMessage("Background color set to " + (backgroundBlack ? "black." : "white."));
 		}
 	}
 
 	@Override
 	public void setFontSize(int font)
 	{
 		if (font != ScreenSetup.INSTANCE.getFontSize())
 		{
 			boolean wasLarge = ScreenSetup.INSTANCE.isFontSizeLarge();
 			boolean increase = ScreenSetup.INSTANCE.getFontSize() < font;
 			ScreenSetup.INSTANCE.setFontSize(font);
 			if (wasLarge != ScreenSetup.INSTANCE.isFontSizeLarge())
 				updateAllClustersAndCompounds(true);
 			ComponentFactory.updateComponents();
 			fireViewChange(PROPERTY_FONT_SIZE_CHANGED);
 			guiControler.showMessage((increase ? "Increase" : "Descrease") + " font size to "
 					+ ScreenSetup.INSTANCE.getFontSize() + ".");
 
 			//			System.out.println(ArrayUtil.toCSVString(new String[] { "name", "description1/smarts", "description2",
 			//					"link" }));
 			//			for (CompoundProperty p : clustering.getFeatures())
 			//			{
 			//				String desc[] = ArrayUtil.trim(p.getCompoundPropertySet().getDescription().trim().split("\n"));
 			//				String link = "";
 			//				if (desc[desc.length - 1].startsWith("API: http"))
 			//				{
 			//					link = desc[desc.length - 1].replaceAll("API: ", "");
 			//					desc = Arrays.copyOfRange(desc, 0, desc.length - 1);
 			//				}
 			//				System.out.println(ArrayUtil.toCSVString(new String[] { ExportData.propToExportString(p),
 			//						p.isSmartsProperty() ? p.getSmarts() : p.getDescription(),
 			//						ArrayUtil.toString(desc, ",", "", "", " "), link }));
 			//			}
 		}
 	}
 
 	@Override
 	public void increaseFontSize(boolean increase)
 	{
 		setFontSize(ScreenSetup.INSTANCE.getFontSize() + (increase ? 1 : -1));
 	}
 
 	@Override
 	public void setMatchColor(Color color)
 	{
 		if (!matchColor.equals(color))
 		{
 			this.matchColor = color;
 			updateAllClustersAndCompounds(true);
 			fireViewChange(PROPERTY_MATCH_COLOR_CHANGED);
 			guiControler.showMessage("Change color to highlight substructure matches.");
 		}
 	}
 
 	@Override
 	public Color getMatchColor()
 	{
 		return matchColor;
 	}
 
 	@SuppressWarnings("unchecked")
 	private void initCompoundDescriptor()
 	{
 		if (compoundDescriptorProperty != COMPOUND_INDEX_PROPERTY
 				&& compoundDescriptorProperty != COMPOUND_SMILES_PROPERTY
 				&& !clustering.getFeatures().contains(compoundDescriptorProperty)
 				&& !clustering.getProperties().contains(compoundDescriptorProperty))
 			compoundDescriptorProperty = null;
 		if (compoundDescriptorProperty == null)
 		{
 			for (String names : new String[] { "(?i)^name$", "(?i).*name.*", "(?i)^id$", "(?i).*id.*", "(?i)^cas$",
 					"(?i).*cas.*", "(?i).*title.*" })
 			{
 				for (List<CompoundProperty> props : new List[] { clustering.getProperties(), clustering.getFeatures() })
 				{
 					for (CompoundProperty p : props)
 					{
 						// cond 1 : prop-name has to match
 						// cond 2 : distinct values are > 75% of the dataset size
 						// cond 3 : if its the id column, it has to either non-numeric or numeric & integer
 						if (p.toString().matches(names)
 								&& clustering.numDistinctValues(p) > (clustering.numCompounds() * 3 / 4.0)
 								&& (!names.equals("(?i)^id$") || !names.equals("(?i).*id.*")
 										|| p.getType() != Type.NUMERIC || p.isIntegerInMappedDataset()))
 						{
 							compoundDescriptorProperty = p;
 							break;
 						}
 					}
 					if (compoundDescriptorProperty != null)
 						break;
 				}
 				if (compoundDescriptorProperty != null)
 					break;
 			}
 		}
 		if (compoundDescriptorProperty == null)
 			compoundDescriptorProperty = COMPOUND_INDEX_PROPERTY;
 		for (Compound m : clustering.getCompounds(true))
 			m.setDescriptor(compoundDescriptorProperty);
 	}
 
 	@Override
 	public void setCompoundDescriptor(CompoundProperty prop)
 	{
 		if (compoundDescriptorProperty != prop)
 		{
 			compoundDescriptorProperty = prop;
 			for (Compound m : clustering.getCompounds(true))
 				m.setDescriptor(compoundDescriptorProperty);
 			fireViewChange(PROPERTY_COMPOUND_DESCRIPTOR_CHANGED);
 			guiControler.showMessage("Set compound identifier to feature value of '" + prop + "'.");
 		}
 	}
 
 	@Override
 	public CompoundProperty getCompoundDescriptor()
 	{
 		return compoundDescriptorProperty;
 	}
 
 	@Override
 	public Boolean isHighlightLogEnabled()
 	{
 		if (selectedHighlightCompoundProperty != null && selectedHighlightCompoundProperty.getType() == Type.NUMERIC)
 			return selectedHighlightCompoundProperty.isLogHighlightingEnabled();
 		else
 			return null;
 	}
 
 	@Override
 	public ColorGradient getHighlightGradient()
 	{
 		if (selectedHighlightCompoundProperty != null && selectedHighlightCompoundProperty.getType() == Type.NUMERIC)
 		{
 			if (selectedHighlightCompoundProperty.getHighlightColorGradient() == null)
 				return DEFAULT_COLOR_GRADIENT;
 			else
 				return selectedHighlightCompoundProperty.getHighlightColorGradient();
 		}
 		else
 			return null;
 	}
 
 	@Override
 	public void setHighlightColors(ColorGradient g, boolean log, CompoundProperty props[])
 	{
 		boolean fire = false;
 		for (CompoundProperty p : props)
 		{
 			if (p.getType() != Type.NUMERIC)
 				throw new IllegalStateException();
 			if (p == selectedHighlightCompoundProperty
 					&& (!g.equals(p.getHighlightColorGradient()) || p.isLogHighlightingEnabled() != log))
 				fire = true;
 			p.setHighlightColorGradient(g);
 			p.setLogHighlightingEnabled(log);
 		}
 		if (fire)
 		{
 			updateAllClustersAndCompounds(true);
 			fireViewChange(PROPERTY_HIGHLIGHT_COLORS_CHANGED);
 			guiControler.showMessage("Change color gradient or log transformation for highlighting.");
 		}
 	}
 }
