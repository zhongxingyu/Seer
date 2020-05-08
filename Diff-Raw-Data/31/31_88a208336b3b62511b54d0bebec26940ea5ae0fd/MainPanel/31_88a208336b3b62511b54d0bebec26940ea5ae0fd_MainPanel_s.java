 package gui;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.Graphics;
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
 
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.SwingUtilities;
 import javax.vecmath.Vector3f;
 
 import main.Settings;
 
 import org.jmol.adapter.smarter.SmarterJmolAdapter;
 import org.jmol.api.JmolAdapter;
 import org.jmol.api.JmolSimpleViewer;
 
 import util.ArrayUtil;
 import util.ColorUtil;
 import util.DoubleUtil;
 import util.StringUtil;
 import cluster.Cluster;
 import cluster.ClusterUtil;
 import cluster.Clustering;
 import cluster.Model;
 import data.ClusteringData;
 import dataInterface.MoleculeProperty;
 import dataInterface.SubstructureSmartsType;
 
 public class MainPanel extends JPanel implements ViewControler
 {
 	GUIControler guiControler;
 	JmolPanel jmolPanel;
 	View view;
 	private Clustering clustering;
 	private boolean spinEnabled = false;
 	private boolean hideHydrogens = false;
 
 	private static final String DEFAULT_COLOR = "color cpk";
 
 	String style = STYLE_WIREFRAME;
 	String color = DEFAULT_COLOR;
 
 	private String getModelInactiveSuffix()
 	{
 		if (style.equals(STYLE_WIREFRAME))
 			return "; color translucent 0.8";
 		else if (style.equals(STYLE_BALLS_AND_STICKS))
 			return "; color translucent 0.9";
 		else
 			throw new Error();
 	}
 
 	//	String modelInactiveSuffix = "; color translucent 0.9";
 	String modelActiveSuffix = "";
 
 	public static final Highlighter DEFAULT_HIGHLIGHTER = new SimpleHighlighter("None (show atom types)");
 
 	HashMap<String, Highlighter[]> highlighters;
 	Highlighter selectedHighlighter = DEFAULT_HIGHLIGHTER;
 	MoleculeProperty selectedHighlightMoleculeProperty = null;
 	boolean highlighterLabelsVisible = false;
 	HighlightSorting highlightSorting = HighlightSorting.Med;
 
 	public Clustering getClustering()
 	{
 		return clustering;
 	}
 
 	public boolean isSpinEnabled()
 	{
 		return spinEnabled;
 	}
 
 	public void setSpinEnabled(boolean spin)
 	{
 		setSpinEnabled(spin, false);
 	}
 
 	private void setSpinEnabled(boolean spinEnabled, boolean force)
 	{
 		if (this.spinEnabled != spinEnabled || force)
 		{
 			this.spinEnabled = spinEnabled;
 			view.setSpinEnabled(spinEnabled);
 		}
 	}
 
 	public MainPanel(GUIControler guiControler)
 	{
 		this.guiControler = guiControler;
 		jmolPanel = new JmolPanel();
 
 		setLayout(new BorderLayout());
 		add(jmolPanel);
 
 		clustering = new Clustering();
 		View.init(jmolPanel, guiControler, hideHydrogens);
 		view = View.instance;
 
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
 						if (clustering.getClusterActive().getSelected() == -1)
 						{
 							// no cluster active at the moment
 							// set a cluster to active (zooming will be done in listener)
 							clustering.getClusterActive().setSelected(
 									clustering.getClusterIndexForModelIndex(view.getAtomModelIndex(atomIndex)));
 							clustering.getClusterWatched().clearSelection();
 						}
 						else
 						{
 							// already a cluster active, zooming into model
 							//						final Model m = clustering.getModelWithModelIndex(view.getAtomModelIndex(atomIndex));
 							clustering.getModelWatched().clearSelection();
 							if (e.isControlDown())
 								clustering.getModelActive().setSelectedInverted(view.getAtomModelIndex(atomIndex));
 							else
 								clustering.getModelActive().setSelected(view.getAtomModelIndex(atomIndex));
 						}
 					}
 					else
 					{
 						clustering.getModelWatched().clearSelection();
 						clustering.getClusterWatched().clearSelection();
 					}
 				}
 				else if (SwingUtilities.isRightMouseButton(e))
 				{
 					if (popup == null)
 						popup = MainPanel.this.guiControler.getPopup();
 					popup.show(e.getComponent(), e.getX(), e.getY());
 				}
 			}
 		});
 
 		jmolPanel.addMouseMotionListener(new MouseAdapter()
 		{
 
 			public void mouseMoved(MouseEvent e)
 			{
 				int atomIndex = view.findNearestAtomIndex(e.getX(), e.getY());
 
 				if (clustering.getClusterActive().getSelected() == -1)
 				{
 					if (atomIndex == -1)
 					{
 						// // do not clear cluster selection
 						clustering.getClusterWatched().clearSelection();
 					}
 					else
 						clustering.getClusterWatched().setSelected(
 								(clustering.getClusterIndexForModelIndex(view.getAtomModelIndex(atomIndex))));
 				}
 				else
 				{
 					if (atomIndex == -1)
 					{
 						// // do not clear model selection
 						clustering.getModelWatched().clearSelection();
 					}
 					else
 						clustering.getModelWatched().setSelected(view.getAtomModelIndex(atomIndex));
 				}
 			}
 		});
 	}
 
 	public String getStyle()
 	{
 		return style;
 	}
 
 	public void setStyle(String style)
 	{
 		if (!this.style.equals(style))
 		{
 			this.style = style;
 			updateAllClustersAndModels(false);
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
 	public void setHighlighter(final Highlighter highlighter)
 	{
 		if (this.selectedHighlighter != highlighter)
 		{
 			selectedHighlighter = highlighter;
 			selectedHighlightMoleculeProperty = null;
 
 			if (highlighter == DEFAULT_HIGHLIGHTER)
 				color = DEFAULT_COLOR;
 			else if (highlighter instanceof SubstructureHighlighter)
 				color = DEFAULT_COLOR;
 			else if (highlighter instanceof MoleculePropertyHighlighter)
 			{
 				color = "color FIXEDTEMPERATURE";
 				selectedHighlightMoleculeProperty = ((MoleculePropertyHighlighter) highlighter).getProperty();
 			}
 
 			updateAllClustersAndModels(false);
 			fireViewChange(PROPERTY_HIGHLIGHT_CHANGED);
 		}
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
 			updateAllClustersAndModels(false);
 			fireViewChange(PROPERTY_HIGHLIGHT_CHANGED);
 		}
 	}
 
 	@Override
 	public void setHighlightSorting(HighlightSorting sorting)
 	{
 		if (this.highlightSorting != sorting)
 		{
 			highlightSorting = sorting;
 			updateAllClustersAndModels(false);
 			fireViewChange(PROPERTY_HIGHLIGHT_CHANGED);
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
 	 * hides models
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
 				view.scriptWait("draw ID bb" + clusterIndex + " BOUNDBOX color "
 						+ ColorUtil.toJMolString(Settings.LIST_WATCH_BACKGROUND) + " translucent " + boxTranslucency
 						+ " MESH NOFILL \"" + c + "\"");
 
 				//				jmolPanel.repaint(); // HACK to avoid label display errors
 			}
 			else
 				view.scriptWait("draw bb" + clusterIndex + " off");
 		}
 
 		boolean visible = active == -1 || clusterIndex == active;
 		boolean someModelsHidden = visible && active == -1;
 
 		if (forceUpdate
 				|| visible != c.isVisible()
 				|| (visible && (someModelsHidden != c.someModelsHidden()
 						|| selectedHighlightMoleculeProperty != c.getHighlightProperty() || highlightSorting != c
 						.getHighlightSorting())))
 		{
 			c.setVisible(visible);
 			c.setSomeModelsHidden(someModelsHidden);
 			c.setHighlighProperty(selectedHighlightMoleculeProperty);
 			c.setHighlightSorting(highlightSorting);
 			hideOrDisplayCluster(c, visible, someModelsHidden);
 		}
 	}
 
 	private void hideOrDisplayCluster(Cluster c, boolean clusterVisible, boolean hideSomeModels)
 	{
 		BitSet toDisplay = new BitSet();
 		BitSet toHide = new BitSet();
 
 		if (!clusterVisible)
 			toHide.or(c.getBitSet());
 		else
 		{
 			if (!hideSomeModels)
 				toDisplay.or(c.getBitSet());
 			else
 			{
 				int numModels = c.getModels().size();
 				int max;
 				if (numModels <= 10)
 					max = 10;
 				else
 					max = (numModels - 10) / 3 + 10;
 				max = Math.min(25, max);
 				//			System.err.println("hiding: ''" + hide + "'', num '" + numModels + "', max '" + max + "'");
 
 				if (numModels < max)
 					toDisplay.or(c.getBitSet());
 				else
 				{
 					List<Model> models;
 					if (selectedHighlighter instanceof MoleculePropertyHighlighter)
 						models = c.getModelsInOrder(((MoleculePropertyHighlighter) selectedHighlighter).getProperty(),
 								highlightSorting);
 					else
 						models = c.getModels();
 					int count = 0;
 					for (Model m : models)
 					{
 						if (count++ >= max)
 							toHide.or(m.getBitSet());
 						else
 							toDisplay.or(m.getBitSet());
 					}
 				}
 			}
 		}
 		view.hide(toHide);
 		view.display(toDisplay);
 	}
 
 	/**
 	 * udpates all clusters
 	 */
 	private void updateAllClustersAndModels(boolean forceUpdate)
 	{
 		int a = getClustering().getClusterActive().getSelected();
 		for (int j = 0; j < clustering.numClusters(); j++)
 		{
 			updateCluster(j, forceUpdate);
 			if (a == -1 || a == j)
 				for (Model m : clustering.getCluster(j).getModels())
 					updateModel(m.getModelIndex(), forceUpdate);
 		}
 	}
 
 	/**
 	 * udpates single model
 	 * forceUpdate = true -> everything is reset (independent of model is part of active cluster or if single props habve changed)
 	 * 
 	 * shows/hides box around model
 	 * show/hides model label
 	 * set model translucent/opaque
 	 * highlight substructure in model 
 	 */
 	private void updateModel(int i, boolean forceUpdate)
 	{
 		int clus = clustering.getClusterIndexForModelIndex(i);
 		Cluster c = clustering.getCluster(clus);
 		Model m = clustering.getModelWithModelIndex(i);
 		if (m == null)
 		{
 			System.err.println("model is null!");
 			return;
 		}
 		int activeCluster = clustering.getClusterActive().getSelected();
 
 		boolean showHoverBox = false;
 		boolean showActiveBox = false;
 		boolean showLabel = false;
 		boolean translucent = true;
 
 		// inside the active cluster
 		if (clus == activeCluster)
 		{
 			translucent = false;
 			boolean hideUnselect = (c.isSuperimposed() && superimposeHideUnselected)
 					|| (!c.isSuperimposed() && hideUnselected);
 			if (hideUnselect && !clustering.getModelWatched().isSelected(i)
 					&& !clustering.getModelActive().isSelected(i))
 				translucent = true;
 
 			if (selectedHighlighter instanceof MoleculePropertyHighlighter)
 				showLabel = true;
 
 			if (clustering.getModelWatched().isSelected(i) && !c.isOverlap())
 				showHoverBox = true;
 			if (clustering.getModelActive().isSelected(i) && !c.isOverlap())
 				showActiveBox = true;
 		}
 		else
 		{
 			List<Model> models;
 			if (selectedHighlighter instanceof MoleculePropertyHighlighter)
 				models = c.getModelsInOrder(((MoleculePropertyHighlighter) selectedHighlighter).getProperty(),
 						highlightSorting);
 			else
 				models = c.getModels();
 
 			if (selectedHighlightMoleculeProperty != null && models.indexOf(m) == 0)
 				showLabel = true;
 			translucent = (models.indexOf(m) > 0);
 		}
 
 		String smarts = null;
 		if (selectedHighlighter instanceof SubstructureHighlighter)
 			smarts = c.getSubstructureSmarts(((SubstructureHighlighter) selectedHighlighter).getType());
 		else if (selectedHighlighter instanceof MoleculePropertyHighlighter
 				&& ((MoleculePropertyHighlighter) selectedHighlighter).getProperty().isSmartsProperty())
 			smarts = ((MoleculePropertyHighlighter) selectedHighlighter).getProperty().getSmarts();
 
 		if (!highlighterLabelsVisible)
 			showLabel = false;
 
 		BitSet bs = view.getModelUndeletedAtomsBitSet(i);
 		view.select(bs);
 
		boolean translucentUpdate = false;
		if (forceUpdate || !style.equals(m.getStyle()))
		{
			translucentUpdate = true;
			m.setStyle(style);
			view.scriptWait(style);
		}

 		boolean labelUpdate = showLabel
 				&& (!color.equals(m.getColor()) || selectedHighlightMoleculeProperty != m
 						.getHighlightMoleculeProperty());
 
		if (forceUpdate || translucentUpdate || translucent != m.isTranslucent() || !color.equals(m.getColor())
				|| selectedHighlightMoleculeProperty != m.getHighlightMoleculeProperty())
 		{
 			m.setTranslucent(translucent);
 			m.setColor(color);
 			m.setHighlightMoleculeProperty(selectedHighlightMoleculeProperty);
 
 			if (translucent)
 				view.scriptWait(color + getModelInactiveSuffix());
 			else
 				view.scriptWait(color + modelActiveSuffix);
 		}
 
 		if (forceUpdate || showHoverBox != m.isShowHoverBox())
 		{
 			m.setShowHoverBox(showHoverBox);
 			if (showHoverBox)
 			{
 				view.scriptWait("boundbox { selected }");
 				view.scriptWait("boundbox off");
 				view.scriptWait("draw ID bb" + m.getModelIndex() + "h BOUNDBOX color "
 						+ ColorUtil.toJMolString(Settings.LIST_WATCH_BACKGROUND) + " translucent " + boxTranslucency
 						+ " MESH NOFILL \"" + m.toString() + "\"");
 				//				jmolPanel.repaint(); // HACK to avoid label display errors
 			}
 			else
 			{
 				view.scriptWait("draw bb" + m.getModelIndex() + "h OFF");
 			}
 		}
 
 		if (forceUpdate || showActiveBox != m.isShowActiveBox())
 		{
 			m.setShowActiveBox(showActiveBox);
 			if (showActiveBox)
 			{
 				view.scriptWait("boundbox { selected }");
 				view.scriptWait("boundbox off");
 				view.scriptWait("draw ID bb" + m.getModelIndex() + "a BOUNDBOX color "
 						+ ColorUtil.toJMolString(Settings.LIST_ACTIVE_BACKGROUND) + " translucent " + boxTranslucency
 						+ " MESH NOFILL");
 				//				jmolPanel.repaint(); // HACK to avoid label display errors
 			}
 			else
 			{
 				view.scriptWait("draw bb" + m.getModelIndex() + "a OFF");
 			}
 		}
 
 		// CHANGES JMOL SELECTION !!!
		if (smarts != m.getHighlightedSmarts())
 		{
 			boolean match = false;
 			if (smarts != null && smarts.length() > 0)
 			{
 				BitSet matchBitSet = m.getSmartsMatch(smarts);
 				if (matchBitSet.cardinality() > 0)
 				{
 					match = true;
 					view.select(matchBitSet);
 					if (m.isTranslucent())
 						view.scriptWait("color orange" + getModelInactiveSuffix());
 					else
 						view.scriptWait("color orange" + modelActiveSuffix);
 				}
 			}
			if (!match && !forceUpdate)
 			{
 				if (m.isTranslucent())
 					view.scriptWait(color + getModelInactiveSuffix());
 				else
 					view.scriptWait(color + modelActiveSuffix);
 			}
 		}
 
 		// CHANGES JMOL SELECTION !!!
 		if (forceUpdate || labelUpdate || showLabel != m.isShowLabel())
 		{
 			m.setShowLabel(showLabel);
 			BitSet empty = new BitSet(bs.length());
 			empty.set(bs.nextSetBit(0));
 			view.select(empty);
 			if (showLabel)
 			{
 
 				Object val = clustering.getModelWithModelIndex(i).getTemperature(
 						((MoleculePropertyHighlighter) selectedHighlighter).getProperty());
 				Double d = DoubleUtil.parseDouble(val + "");
 				if (d != null)
 					val = StringUtil.formatDouble(d);
 				//				System.err.println("label : " + i + " : " + c + " : " + val);
 				String l = ((MoleculePropertyHighlighter) selectedHighlighter).getProperty() + ": " + val;
 				view.scriptWait("set fontSize " + View.FONT_SIZE);
 				view.scriptWait("label \"" + l + "\"");
 			}
 			else
 			{
 				//				System.err.println("label : " + i + " : " + c + " : off");
 				view.scriptWait("label OFF");
 			}
 		}
 	}
 
 	public void init(ClusteringData clusteredDataset)
 	{
 		clustering.newClustering(clusteredDataset);
 
 		clustering.getClusterActive().addListener(new PropertyChangeListener()
 		{
 			@Override
 			public void propertyChange(PropertyChangeEvent e)
 			{
 				int cIndexOldArray[] = ((int[]) e.getOldValue());
 				int cIndexOld = cIndexOldArray.length == 0 ? -1 : cIndexOldArray[0];
 				updateClusterSelection(clustering.getClusterActive().getSelected(), cIndexOld, true);
 			}
 		});
 		clustering.getClusterWatched().addListener(new PropertyChangeListener()
 		{
 			@Override
 			public void propertyChange(PropertyChangeEvent e)
 			{
 				int cIndexOldArray[] = ((int[]) e.getOldValue());
 				int cIndexOld = cIndexOldArray.length == 0 ? -1 : cIndexOldArray[0];
 				updateClusterSelection(clustering.getClusterWatched().getSelected(), cIndexOld, false);
 			}
 		});
 		clustering.getModelActive().addListener(new PropertyChangeListener()
 		{
 			@Override
 			public void propertyChange(PropertyChangeEvent e)
 			{
 				updateModelActiveSelection(((int[]) e.getNewValue()), ((int[]) e.getOldValue()), clustering
 						.getModelActive().isExclusiveSelection());
 			}
 		});
 		clustering.getModelWatched().addListener(new PropertyChangeListener()
 		{
 			@Override
 			public void propertyChange(PropertyChangeEvent e)
 			{
 				int mIndexOldArray[] = ((int[]) e.getOldValue());
 				int mIndexOld = mIndexOldArray.length == 0 ? -1 : mIndexOldArray[0];
 				updateModelWatchedSelection(clustering.getModelWatched().getSelected(), mIndexOld);
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
 
 		view.setBackground(Settings.BACKGROUND);
 		updateClusteringNew();
 	}
 
 	private void updateClusteringNew()
 	{
 		Highlighter[] h = new Highlighter[] { DEFAULT_HIGHLIGHTER };
 		if (clustering.getSubstructures().size() > 0)
 			for (SubstructureSmartsType type : clustering.getSubstructures())
 				h = ArrayUtil.concat(Highlighter.class, h, new Highlighter[] { new SubstructureHighlighter(type) });
 		highlighters = new LinkedHashMap<String, ViewControler.Highlighter[]>();
 		highlighters.put("", h);
 
 		List<MoleculeProperty> props = clustering.getFeatures();
 		MoleculePropertyHighlighter featureHighlighters[] = new MoleculePropertyHighlighter[props.size()];
 		int fCount = 0;
 		for (MoleculeProperty p : props)
 			featureHighlighters[fCount++] = new MoleculePropertyHighlighter(p);
 		highlighters.put("Features used for mapping", featureHighlighters);
 
 		props = clustering.getProperties();
 		featureHighlighters = new MoleculePropertyHighlighter[props.size()];
 		fCount = 0;
 		for (MoleculeProperty p : props)
 			featureHighlighters[fCount++] = new MoleculePropertyHighlighter(p);
 		highlighters.put("Features NOT used for mapping", featureHighlighters);
 
 		fireViewChange(PROPERTY_NEW_HIGHLIGHTERS);
 
 		updateAllClustersAndModels(true);
 
 		view.evalString("frame " + view.getModelNumberDotted(0) + " "
 				+ view.getModelNumberDotted(clustering.numModels() - 1));
 
 		view.zoomOut(clustering.getCenter(), 0, clustering.getRadius());//, clustering.getBitSetAll());
 		setSpinEnabled(isSpinEnabled(), true);
 
 		if (clustering.getNumClusters() == 1)
 		{
 			clustering.getClusterActive().setSelected(0);
 		}
 	}
 
 	private void updateClusterRemoved()
 	{
 		setHighlighter(DEFAULT_HIGHLIGHTER);
 	}
 
 	private void updateClusterModified()
 	{
 		updateAllClustersAndModels(true);
 	}
 
 	private void updateModelWatchedSelection(int mIndex, int mIndexOld)
 	{
 		//		System.out.println("update model active: " + active + " " + ArrayUtil.toString(mIndex));
 		int activeCluster = clustering.getClusterActive().getSelected();
 		if (activeCluster == -1 || activeCluster == clustering.getClusterIndexForModelIndex(mIndexOld))
 			updateModel(mIndexOld, false);
 		if (activeCluster == -1 || activeCluster == clustering.getClusterIndexForModelIndex(mIndex))
 			updateModel(mIndex, false);
 	}
 
 	private void updateModelActiveSelection(int mIndex[], int mIndexOld[], boolean zoomIntoModel)
 	{
 		//		System.out.println("update model active: " + active + " " + ArrayUtil.toString(mIndex));
 		int activeCluster = clustering.getClusterActive().getSelected();
 		for (int i : mIndexOld)
 			if (activeCluster == -1 || activeCluster == clustering.getClusterIndexForModelIndex(i))
 				updateModel(i, false);
 		for (int i : mIndex)
 			if (activeCluster == -1 || activeCluster == clustering.getClusterIndexForModelIndex(i))
 				updateModel(i, false);
 
 		if (activeCluster != -1 && !clustering.getCluster(activeCluster).isSuperimposed())
 		{
 			if (mIndex.length != 0 && zoomIntoModel)
 			{
 				if (mIndex.length != 1)
 					throw new IllegalStateException();
 				final Model m = clustering.getModelWithModelIndex(mIndex[0]);
 				view.zoomIn(new Vector3f(view.getAtomSetCenter(m.getBitSet())), 0.5f);
 			}
 			else if (mIndex.length == 0 && mIndexOld.length > 0)
 			{
 				final Cluster c = getClustering().getClusterForModelIndex(mIndexOld[0]);
 				view.zoomOut(c.getNonOverlapCenter(), 0.5f, c.getRadius());
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
 	 * @param active
 	 */
 	private void updateClusterSelection(final int cIndex, final int cIndexOld, boolean active)
 	{
 		// ignore watch updates when a cluster is selected
 		if (!active && clustering.getClusterActive().getSelected() != -1)
 			return;
 
 		System.out.println("updating cluster selection: " + cIndex + " " + cIndexOld + " " + active);
 
 		if (!active)
 		{
 			updateAllClustersAndModels(false);
 		}
 		else
 		{
 			if (cIndex != -1)
 			{
 				// zoom into cluster
 				final Cluster oldC = clustering.getCluster(cIndexOld);
 				final Cluster c = clustering.getCluster(cIndex);
 
 				if (oldC != null)
 					oldC.setOverlap(true, View.MoveAnimation.NONE, false);
 
 				updateAllClustersAndModels(false);
 
 				if (c.getModels().size() > 1)
 					view.zoomOut(c.getNonOverlapCenter(), 0.5f, c.getRadius());//, c.getBitSet());
 				else
 				{
 					//twice zoom time as zoom into cluster with more that one compund, because of model translation
 					view.zoomIn(c.getCenter(), 1f);
 				}
 				c.setOverlap(false, View.MoveAnimation.SLOW, false);
 			}
 			else
 			{
 				// zoom to home
 				final Cluster c = clustering.getCluster(cIndexOld);
 
 				for (Model m : c.getModels())
 					updateModel(m.getModelIndex(), true);
 
 				c.setOverlap(true, View.MoveAnimation.FAST, false);
 				//viewerUtils.zoomOut(true, new Vector3f(0, 0, 0), 0.5);
 				view.zoomOut(clustering.getCenter(), 0.5f, clustering.getRadius()); //, clustering.getBitSetAll());
 
 				view.afterAnimation(new Runnable()
 				{
 					@Override
 					public void run()
 					{
 						updateAllClustersAndModels(false);
 					}
 				}, "after zoom out");
 			}
 		}
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
 
 		public void paint(Graphics g)
 		{
 			getSize(currentSize);
 			g.getClipBounds(rectClip);
 			if (g != null && currentSize != null && rectClip != null)
 				viewer.renderScreenImage(g, currentSize, rectClip);
 		}
 	}
 
 	@Override
 	public void setSuperimpose(boolean superimpose)
 	{
 		int cIndex = clustering.getClusterActive().getSelected();
 		if (cIndex == -1)
 			return;
 		final Cluster c = clustering.getCluster(cIndex);
 		if (c.isOverlap() == superimpose)
 			return;
 
 		clustering.getModelWatched().clearSelection();
 
 		if (c.isOverlap())
 		{
 			view.zoomOut(c.getNonOverlapCenter(), 0.33f, c.getRadius()); //, c.getBitSet());
 			c.setOverlap(false, View.MoveAnimation.SLOW, false);
 		}
 		else
 		{
 			c.setOverlap(true, View.MoveAnimation.SLOW, true);
 			// the center is changed in the animation superimposition
 			view.afterAnimation(new Runnable()
 			{
 				@Override
 				public void run()
 				{
 					view.zoomIn(c.getCenter(), 0.33f);
 				}
 			}, "zoom to new(!) center");
 		}
 
 		view.afterAnimation(new Runnable()
 		{
 			@Override
 			public void run()
 			{
 				for (Model m : c.getModels())
 					updateModel(m.getModelIndex(), false);
 			}
 		}, "set models translucent");
 
 		fireViewChange(PROPERTY_DENSITY_CHANGED);
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
 	public boolean canChangeDensitiy(boolean higher)
 	{
 		if (higher && ClusterUtil.DENSITY <= 0.1f)
 			return false;
 		Cluster activeCluster = clustering.getCluster(clustering.getClusterActive().getSelected());
 		if (activeCluster == null)
 			return true;
 		else
 			return !activeCluster.isSuperimposed();
 	}
 
 	@Override
 	public void setDensitiyHigher(boolean higher)
 	{
 		Cluster activeCluster = clustering.getCluster(clustering.getClusterActive().getSelected());
 		if (activeCluster != null && activeCluster.isSuperimposed())
 			throw new IllegalStateException("does not make sense, because superimposed!");
 
 		if (activeCluster != null)
 		{
 			if (clustering.getModelActive().getSelected() != -1)
 				clustering.getModelActive().clearSelection();
 		}
 
 		if (higher)
 			ClusterUtil.DENSITY = ClusterUtil.DENSITY - 0.1f;
 		else
 			ClusterUtil.DENSITY = ClusterUtil.DENSITY + 0.1f;
 
 		view.setAnimated(false);
 		clustering.updatePositions();
 		if (activeCluster != null)
 		{
 			System.out.println("zooming out - cluster");
 			view.zoomOut(activeCluster.getNonOverlapCenter(), 0f, activeCluster.getRadius());
 		}
 		else
 		{
 			System.out.println("zooming out - home");
 			view.zoomOut(clustering.getCenter(), 0f, clustering.getRadius());
 		}
 		view.setAnimated(true);
 
 		fireViewChange(PROPERTY_DENSITY_CHANGED);
 	}
 
 	boolean superimposeHideUnselected = true;
 	boolean hideUnselected = false;
 
 	@Override
 	public boolean isHideUnselected()
 	{
 		Cluster activeCluster = clustering.getCluster(clustering.getClusterActive().getSelected());
 		if (activeCluster == null)
 			return false;
 		if (activeCluster.isSuperimposed())
 			return superimposeHideUnselected;
 		else
 			return hideUnselected;
 	}
 
 	@Override
 	public void setHideUnselected(boolean hide)
 	{
 		Cluster activeCluster = clustering.getCluster(clustering.getClusterActive().getSelected());
 		if (activeCluster == null)
 			throw new IllegalStateException();
 		if (activeCluster.isSuperimposed())
 			superimposeHideUnselected = hide;
 		else
 			hideUnselected = hide;
 		for (Model m : activeCluster.getModels())
 			updateModel(m.getModelIndex(), true);
 
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
 	}
 }
