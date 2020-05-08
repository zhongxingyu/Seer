 package cluster;
 
 import gui.CheckBoxSelectDialog;
 import gui.View;
 import gui.Zoomable;
 import io.SDFUtil;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.BitSet;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Vector;
 
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.vecmath.Vector3f;
 
 import main.Settings;
 import main.TaskProvider;
 import util.ArrayUtil;
 import util.FileUtil;
 import util.SelectionModel;
 import util.Vector3fUtil;
 import util.VectorUtil;
 import data.ClusteringData;
 import data.DatasetFile;
 import dataInterface.ClusterData;
 import dataInterface.MoleculeProperty;
 import dataInterface.SubstructureSmartsType;
 
 public class Clustering implements Zoomable
 {
 	private Vector<Cluster> clusters;
 	private ClusteringData clusteringData;
 
 	SelectionModel clusterActive;
 	SelectionModel clusterWatched;
 	SelectionModel modelActive;
 	SelectionModel modelWatched;
 
 	boolean suppresAddEvent = false;
 	Vector<PropertyChangeListener> listeners;
 
 	public static String CLUSTER_ADDED = "cluster_added";
 	public static String CLUSTER_REMOVED = "cluster_removed";
 	public static String CLUSTER_MODIFIED = "cluster_modified";
 
 	boolean dirty = true;
 	int numModels = -1;
 	private boolean superimposed = false;
 	float superimposedDiameter;
 	float nonSuperimposedDiameter;
 
 	BitSet bitSetAll;
 	HashMap<Integer, Model> modelIndexToModel;
 	HashMap<Integer, Cluster> modelIndexToCluster;
 
 	Vector3f superimposedCenter;
 	Vector3f nonSuperimposedCenter;
 
 	public Clustering()
 	{
 		listeners = new Vector<PropertyChangeListener>();
 		init();
 	}
 
 	public void init()
 	{
 		clusterActive = new SelectionModel();
 		clusterWatched = new SelectionModel();
 		modelActive = new SelectionModel(true);
 		modelWatched = new SelectionModel();
 		clusters = new Vector<Cluster>();
 	}
 
 	public void addListener(PropertyChangeListener l)
 	{
 		listeners.add(l);
 	}
 
 	public void fire(String event, Object oldValue, Object newValue)
 	{
 		if (!suppresAddEvent)
 			for (PropertyChangeListener l : listeners)
 				l.propertyChange(new PropertyChangeEvent(this, event, oldValue, newValue));
 	}
 
 	private Cluster addSingleCluster(ClusterData clusterData)
 	{
 		Cluster c = new Cluster(clusterData, clusters.size() == 0);
 		@SuppressWarnings("unchecked")
 		Vector<Cluster> old = (Vector<Cluster>) VectorUtil.clone(Clustering.this.clusters);
 		clusters.add(c);
 		dirty = true;
 		fire(CLUSTER_ADDED, old, clusters);
 		return c;
 	}
 
 	public void update()
 	{
 		if (!dirty)
 			return;
 
 		numModels = 0;
 		for (Cluster c : clusters)
 			numModels += c.size();
 
 		bitSetAll = new BitSet();
 		for (Cluster c : clusters)
 			bitSetAll.or(c.getBitSet());
 
 		modelIndexToCluster = new HashMap<Integer, Cluster>();
 		modelIndexToModel = new HashMap<Integer, Model>();
 		for (Cluster c : clusters)
 		{
 			for (Model m : c.getModels())
 			{
 				modelIndexToModel.put(m.getModelIndex(), m);
 				modelIndexToCluster.put(m.getModelIndex(), c);
 			}
 		}
 
 		dirty = false;
 	}
 
 	public boolean isClusterActive()
 	{
 		return clusterActive.getSelected() != -1;
 	}
 
 	public boolean isClusterWatched()
 	{
 		return clusterWatched.getSelected() != -1;
 	}
 
 	public boolean isModelActive()
 	{
 		return modelActive.getSelected() != -1;
 	}
 
 	public boolean isModelWatched()
 	{
 		return modelWatched.getSelected() != -1;
 	}
 
 	public SelectionModel getClusterActive()
 	{
 		return clusterActive;
 	}
 
 	public SelectionModel getClusterWatched()
 	{
 		return clusterWatched;
 	}
 
 	public SelectionModel getModelActive()
 	{
 		return modelActive;
 	}
 
 	public SelectionModel getModelWatched()
 	{
 		return modelWatched;
 	}
 
 	public Cluster getClusterForModel(Model model)
 	{
 		update();
 		return modelIndexToCluster.get(model.getModelIndex());
 	}
 
 	public Cluster getClusterForModelIndex(int modelIndex)
 	{
 		update();
 		return modelIndexToCluster.get(modelIndex);
 	}
 
 	public int indexOf(Cluster cluster)
 	{
 		return clusters.indexOf(cluster);
 	}
 
 	public int getClusterIndexForModel(Model model)
 	{
 		return indexOf(getClusterForModel(model));
 	}
 
 	public int getClusterIndexForModelIndex(int modelIndex)
 	{
 		return indexOf(getClusterForModelIndex(modelIndex));
 	}
 
 	public Model getModelWithModelIndex(int modelIndex)
 	{
 		update();
 		return modelIndexToModel.get(modelIndex);
 	}
 
 	public int numModels()
 	{
 		update();
 		return numModels;
 	}
 
 	public int numClusters()
 	{
 		return clusters.size();
 	}
 
 	public BitSet getBitSetAll()
 	{
 		update();
 		return bitSetAll;
 	}
 
 	private void clearSelection()
 	{
 		modelActive.clearSelection();
 		modelWatched.clearSelection();
 		clusterActive.clearSelection();
 		clusterWatched.clearSelection();
 	}
 
 	public void clear()
 	{
 		@SuppressWarnings("unchecked")
 		Vector<Cluster> old = (Vector<Cluster>) VectorUtil.clone(Clustering.this.clusters);
 		clearSelection();
 		clusters.removeAllElements();
 		View.instance.zap(true, true, true);
 		dirty = true;
 
 		fire(CLUSTER_REMOVED, old, clusters);
 	}
 
 	private void removeCluster(final Cluster... clusters)
 	{
 		clearSelection();
 
 		@SuppressWarnings("unchecked")
 		Vector<Cluster> old = (Vector<Cluster>) VectorUtil.clone(Clustering.this.clusters);
 		for (Cluster c : clusters)
 		{
 			View.instance.hide(c.getBitSet());
 			Clustering.this.clusters.remove(c);
 		}
 		dirty = true;
 		updatePositions();
 		if (getNumClusters() == 1)
 			getClusterActive().setSelected(0);
 		fire(CLUSTER_REMOVED, old, clusters);
 	}
 
 	public Cluster getCluster(int clusterIndex)
 	{
 		if (clusterIndex < 0)
 			return null;
 		return clusters.get(clusterIndex);
 	}
 
 	public Iterable<Cluster> getClusters()
 	{
 		return clusters;
 	}
 
 	public void newClustering(ClusteringData d)
 	{
 		@SuppressWarnings("unchecked")
 		Vector<Cluster> old = (Vector<Cluster>) VectorUtil.clone(Clustering.this.clusters);
 		suppresAddEvent = true;
 
 		clusteringData = d;
 
 		for (int i = 0; i < d.getSize(); i++)
 		{
 			//String substructure = null;
 			//			if (d.getClusterSubstructureSmarts() != null)
 			//				substructure = d.getClusterSubstructureSmarts()[i];
 
 			addSingleCluster(d.getCluster(i));
 			TaskProvider.task().update("Loading cluster dataset " + (i + 1) + "/" + d.getSize());
 		}
 		TaskProvider.task().update(90, "Loading graphics");
 
 		updatePositions();
 
 		suppresAddEvent = false;
 
 		fire(CLUSTER_ADDED, old, clusters);
 
 		View.instance.scriptWait("hover off");
 	}
 
 	public void updatePositions()
 	{
 		ClusteringUtil.updateScaleFactor(this);
 
 		getClusterWatched().clearSelection();
 		getModelWatched().clearSelection();
 
 		Vector3f[] positions = ClusteringUtil.getClusterPositions(this);
 
 		View.instance.suspendAnimation("updating clustering positions");
 		for (int i = 0; i < positions.length; i++)
 		{
 			Cluster c = clusters.get(i);
 			if (!superimposed)
 				setClusterOverlap(c, true, null);
 			c.updatePositions();
 			if (!superimposed)
 				setClusterOverlap(c, false, null);
 		}
 		View.instance.proceedAnimation("updating clustering positions");
 
 		positions = ClusteringUtil.getClusterPositions(this);
		superimposedCenter = Vector3fUtil.centerBoundboxConvexHull(positions);
 
 		// take only cluster points into account (ignore cluster sizes)
 		superimposedDiameter = Vector3fUtil.maxDist(positions);
 		// needed for very small distances / only one cluster : diameter should be at least as big as cluster diameter
 		for (Cluster c : clusters)
 			superimposedDiameter = Math.max(superimposedDiameter, c.getDiameter(true));
 
 		// take only model positions into account (ignore model sizes)
 		positions = ClusteringUtil.getModelPositions(this);
		nonSuperimposedCenter = Vector3fUtil.centerBoundboxConvexHull(positions);
 		nonSuperimposedDiameter = Vector3fUtil.maxDist(positions);
 	}
 
 	/**
 	 * toggles model positions between model position (overlap=false) and cluster position (overlap=true) 
 	 * 
 	 * @param clusters
 	 * @param overlap
 	 * @param anim
 	 */
 	public void setClusterOverlap(List<Cluster> clusters, boolean overlap, View.AnimationSpeed anim)
 	{
 		List<Vector3f> modelPositions = new ArrayList<Vector3f>();
 		List<BitSet> bitsets = new ArrayList<BitSet>();
 
 		for (Cluster cluster : clusters)
 		{
 			if (cluster.isSuperimposed() != overlap)
 			{
 				for (int i = 0; i < cluster.size(); i++)
 				{
 					bitsets.add(cluster.getModel(i).getBitSet());
 
 					// destination is model position
 					Vector3f pos = cluster.getModel(i).getPosition();
 					// model is already at cluster position, sub to get relative vector
 					pos.sub(cluster.getCenter(true));
 					if (overlap)
 						pos.scale(-1);
 					modelPositions.add(pos);
 				}
 			}
 			cluster.setSuperimposed(overlap);
 		}
 		View.instance.setAtomCoordRelative(modelPositions, bitsets, anim);
 	}
 
 	public void setClusterOverlap(Cluster cluster, boolean overlap, View.AnimationSpeed anim)
 	{
 		List<Cluster> l = new ArrayList<Cluster>();
 		l.add(cluster);
 		setClusterOverlap(l, overlap, anim);
 	}
 
 	private int[] clusterChooser(String title, String description)
 	{
 		int clusterIndex = getClusterActive().getSelected();
 		if (clusterIndex == -1)
 			clusterIndex = getClusterWatched().getSelected();
 
 		Cluster c[] = new Cluster[numClusters()];
 		for (int i = 0; i < c.length; i++)
 			c[i] = getCluster(i);
 		boolean b[] = new boolean[numClusters()];
 		if (clusterIndex != -1)
 			b[clusterIndex] = true;
 
 		return CheckBoxSelectDialog.select((JFrame) Settings.TOP_LEVEL_COMPONENT, title, description, c, b);
 	}
 
 	/**
 	 * returns jmol model index (not orig sdf model index)  
 	 */
 	private int[] compoundChooser(String title, String description)
 	{
 		int clusterIndex = getClusterActive().getSelected();
 		if (clusterIndex == -1)
 			clusterIndex = getClusterWatched().getSelected();
 
 		List<Model> l = new ArrayList<Model>();
 		List<Boolean> lb = new ArrayList<Boolean>();
 
 		for (int i = 0; i < numClusters(); i++)
 		{
 			Cluster c = getCluster(i);
 			for (int j = 0; j < c.size(); j++)
 			{
 				l.add(c.getModel(j));
 				lb.add(clusterIndex == -1 || clusterIndex == i);
 			}
 		}
 		Model m[] = new Model[l.size()];
 		int selectedIndices[] = CheckBoxSelectDialog.select((JFrame) Settings.TOP_LEVEL_COMPONENT, title, description,
 				l.toArray(m), ArrayUtil.toPrimitiveBooleanArray(lb));
 		return selectedIndices;
 	}
 
 	public void chooseClustersToRemove()
 	{
 		int[] indices = clusterChooser("Remove Cluster/s",
 				"Select the clusters you want to remove (the original dataset is not modified).");
 		if (indices != null)
 		{
 			Cluster c2[] = new Cluster[indices.length];
 			for (int i = 0; i < indices.length; i++)
 				c2[i] = getCluster(indices[i]);
 			removeCluster(c2);
 		}
 	}
 
 	public void chooseClustersToExport()
 	{
 		int[] indices = clusterChooser("Export Cluster/s",
 				"Select the clusters you want to export. The compounds will be stored in a single SDF file.");
 		if (indices != null)
 			exportClusters(indices);
 	}
 
 	public void chooseModelsToRemove()
 	{
 		int[] indices = compoundChooser("Remove Compounds/s",
 				"Select the compounds you want to remove from the dataset (the original dataset is not modified).");
 		if (indices == null)
 			return;
 		removeModels(indices);
 	}
 
 	public void chooseModelsToExport()
 	{
 		int indices[] = compoundChooser("Export Compounds/s",
 				"Select the compounds you want to export. The compounds will be stored in a single SDF file.");
 		if (indices == null)
 			return;
 		List<Integer> l = new ArrayList<Integer>();
 		for (int i = 0; i < indices.length; i++)
 			l.add(getModelWithModelIndex(indices[i]).getModelOrigIndex());
 		exportModels(l);
 	}
 
 	public void exportClusters(int clusterIndices[])
 	{
 		List<Integer> l = new ArrayList<Integer>();
 		for (int i = 0; i < clusterIndices.length; i++)
 			for (Model m : getCluster(clusterIndices[i]).getModels())
 				l.add(m.getModelOrigIndex());
 		exportModels(l);
 	}
 
 	public void exportModels(List<Integer> modelIndices)
 	{
 		exportModels(ArrayUtil.toPrimitiveIntArray(modelIndices));
 	}
 
 	public void exportModels(int modelOrigIndices[])
 	{
 		JFileChooser f = new JFileChooser(clusteringData.getSDFFilename());//origSDFFile);
 		int i = f.showSaveDialog(Settings.TOP_LEVEL_COMPONENT);
 		if (i == JFileChooser.APPROVE_OPTION)
 		{
 			String dest = f.getSelectedFile().getAbsolutePath();
 			if (!f.getSelectedFile().exists() && !FileUtil.getFilenamExtension(dest).matches("(?i)sdf"))
 				dest += ".sdf";
 			if (new File(dest).exists())
 			{
 				if (JOptionPane.showConfirmDialog(Settings.TOP_LEVEL_COMPONENT, "File '" + dest
 						+ "' already exists, overwrite?", "Warning", JOptionPane.YES_NO_OPTION,
 						JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION)
 					return;
 			}
 			// file may be overwritten, and then reloaded -> clear
 			DatasetFile.clearFilesWith3DSDF(dest);
 			SDFUtil.filter(clusteringData.getSDFFilename(), dest, modelOrigIndices);
 		}
 	}
 
 	public String getName()
 	{
 		return clusteringData.getName();
 	}
 
 	public String getOrigSdfFile()
 	{
 		return clusteringData.getSDFFilename();
 	}
 
 	public void removeSelectedCluster()
 	{
 		if (getClusterActive().getSelected() != -1)
 			removeCluster(getClusterActive().getSelected());
 		else
 			removeCluster(getClusterWatched().getSelected());
 	}
 
 	public void removeCluster(int clusterIndex)
 	{
 		removeCluster(getCluster(clusterIndex));
 	}
 
 	public void removeSelectedModel()
 	{
 		if (getClusterWatched().getSelected() != -1)
 		{
 			removeModels(getModelWatched().getSelectedIndices());
 		}
 	}
 
 	public void removeModels(int modelIndices[])
 	{
 		LinkedHashMap<Cluster, List<Integer>> toDel = new LinkedHashMap<Cluster, List<Integer>>();
 
 		// assign indices to clusters
 		for (int i = 0; i < modelIndices.length; i++)
 		{
 			Cluster c = getCluster(getClusterIndexForModelIndex(modelIndices[i]));
 			List<Integer> l = toDel.get(c);
 			if (l == null)
 			{
 				l = new ArrayList<Integer>();
 				toDel.put(c, l);
 			}
 			l.add(modelIndices[i]);
 		}
 
 		// delete clusterwise
 		for (Cluster c : toDel.keySet())
 		{
 			int indices[] = ArrayUtil.toPrimitiveIntArray(toDel.get(c));
 			if (indices.length == c.size())
 				removeCluster(c);
 			else
 			{
 				modelActive.clearSelection();
 				modelWatched.clearSelection();
 				c.remove(indices);
 				dirty = true;
 				fire(CLUSTER_MODIFIED, null, null);
 			}
 		}
 	}
 
 	public List<SubstructureSmartsType> getSubstructures()
 	{
 		return clusteringData.getSubstructureSmartsTypes();
 	}
 
 	public List<MoleculeProperty> getFeatures()
 	{
 		return clusteringData.getFeatures();
 	}
 
 	public List<MoleculeProperty> getProperties()
 	{
 		return clusteringData.getProperties();
 	}
 
 	public int getNumClusters()
 	{
 		return clusters.size();
 	}
 
 	public int getNumCompounds()
 	{
 		int sum = 0;
 		for (Cluster c : clusters)
 			sum += c.size();
 		return sum;
 	}
 
 	public Iterable<Model> getModels()
 	{
 		List<Model> m = new ArrayList<Model>();
 		for (Cluster c : clusters)
 			for (Model mm : c.getModels())
 				m.add(mm);
 		return m;
 	}
 
 	public String getClusterAlgorithm()
 	{
 		return clusteringData.getClusterAlgorithm();
 	}
 
 	public String getEmbedAlgorithm()
 	{
 		return clusteringData.getEmbedAlgorithm();
 	}
 
 	@Override
 	public Vector3f getCenter(boolean superimposed)
 	{
 		if (superimposed)
 			return superimposedCenter;
 		else
 			return nonSuperimposedCenter;
 	}
 
 	@Override
 	public float getDiameter(boolean superimposed)
 	{
 		if (superimposed)
 			return superimposedDiameter;
 		else
 			return nonSuperimposedDiameter;
 	}
 
 	@Override
 	public boolean isSuperimposed()
 	{
 		return superimposed;
 	}
 
 	public void setSuperimposed(boolean superimposed)
 	{
 		this.superimposed = superimposed;
 	}
 }
