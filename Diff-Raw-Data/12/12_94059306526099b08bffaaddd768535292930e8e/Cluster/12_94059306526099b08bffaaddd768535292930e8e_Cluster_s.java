 package cluster;
 
 import gui.View;
 import gui.ViewControler.HighlightSorting;
 import gui.Zoomable;
 
 import java.util.ArrayList;
 import java.util.BitSet;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Vector;
 
 import javax.vecmath.Vector3f;
 
 import util.ArrayUtil;
 import util.Vector3fUtil;
 import dataInterface.ClusterData;
 import dataInterface.MoleculeProperty;
 import dataInterface.MoleculeProperty.Type;
 import dataInterface.SubstructureSmartsType;
 
 public class Cluster implements Zoomable
 {
 	private Vector<Model> models;
 	private ClusterData clusterData;
 
 	private BitSet bitSet;
 
 	private boolean superimposed = true;
 	private float superimposeDiameter;
 	private float nonSuperimposeDiameter;
 	private Vector3f superimposeCenter;
 	private Vector3f nonSuperimposeCenter;
 	private boolean allCompoundsHaveSamePosition = false;
 
 	HashMap<String, List<Model>> modelsOrderedByPropterty = new HashMap<String, List<Model>>();
 
 	private boolean watched;
 	private boolean visible;
 	private MoleculeProperty highlightProp;
 	private HighlightSorting highlightSorting;
 	private boolean someModelsHidden;
 	private boolean showLabel = false;
 
 	public Cluster(dataInterface.ClusterData clusterData, boolean firstCluster)
 	{
 		this.clusterData = clusterData;
 
 		int before = firstCluster ? 0 : View.instance.getModelCount();
 		View.instance.loadModelFromFile(null, clusterData.getFilename(), null, null, !firstCluster, null, null, 0);
 		int after = View.instance.getModelCount();
 
 		if ((after - before) != clusterData.getSize())
 			throw new IllegalStateException("models in file: " + (after - before) + " != model props passed: "
 					+ clusterData.getSize());
 
 		models = new Vector<Model>();
 		int mCount = 0;
 		for (int i = before; i < after; i++)
 			models.add(new Model(i, clusterData.getCompounds().get(mCount++)));
 
 		update();
 	}
 
 	boolean alignedCompoundsCalibrated = false;
 
 	public void updatePositions()
 	{
 		// the actual model position that is stored in model is never changed (depends only on scaling)
 		// this method moves all models to the cluster position
 
 		// recalculate non-superimposed diameter
 		update();
 
 		if (!clusterData.isAligned())
 		{
 			// the compounds have not been aligned
 			// the compounds may have a center != 0, calibrate to 0
 			for (Model m : models)
 				m.moveTo(new Vector3f(0f, 0f, 0f));
 		}
 		else
 		{
 			// the compounds are aligned, cannot calibrate to 0, this would brake the alignment
 			// however, the compound center may have an offset, calculate and remove
 			if (!alignedCompoundsCalibrated)
 			{
 				Vector3f[] origCenters = new Vector3f[models.size()];
 				for (int i = 0; i < origCenters.length; i++)
 					origCenters[i] = models.get(i).origCenter;
 				Vector3f center = Vector3fUtil.center(origCenters);
 				for (int i = 0; i < origCenters.length; i++)
 					models.get(i).origCenter.sub(center);
 				alignedCompoundsCalibrated = true;
 			}
 			for (Model m : models)
 				m.moveTo(m.origCenter);
 		}
 
 		// translate compounds to the cluster position
 		View.instance.setAtomCoordRelative(getCenter(true), getBitSet());
 	}
 
 	public String getSummaryStringValue(MoleculeProperty property)
 	{
 		return clusterData.getSummaryStringValue(property);
 	}
 
 	public String toString()
 	{
 		return getName() + " (#" + size() + ")";
 	}
 
 	public String getName()
 	{
 		return clusterData.getName();
 	}
 
 	public String getAlignAlgorithm()
 	{
 		return clusterData.getAlignAlgorithm();
 	}
 
 	private void update()
 	{
 		bitSet = new BitSet();
 		for (Model m : models)
 			bitSet.or(m.getBitSet());
 
 		// updating (unscaled!) cluster position
 		// this is only needed in case a compound was removed
 		Vector3f[] positions = ClusteringUtil.getModelPositions(this);
 		superimposeCenter = Vector3fUtil.center(positions);
		nonSuperimposeCenter = Vector3fUtil.centerBoundboxConvexHull(positions);
 
 		superimposeDiameter = -1;
 		for (Model m : models)
 			superimposeDiameter = Math.max(superimposeDiameter, m.getDiameter());
 
 		// recompute diameter, depends on the scaling
 		positions = ClusteringUtil.getModelPositions(this);
 		float maxCompoundDist = Vector3fUtil.maxDist(positions);
 		allCompoundsHaveSamePosition = maxCompoundDist == 0;
 
 		// nonSuperimposeDiameter ignores size of compounds, just uses the compound positions
 		// for very small diameter: should be at least as big as superimposed one
 		nonSuperimposeDiameter = Math.max(superimposeDiameter, maxCompoundDist);
 	}
 
 	public Model getModelWithModelIndex(int modelIndex)
 	{
 		for (Model m : models)
 			if (m.getModelIndex() == modelIndex)
 				return m;
 		return null;
 	}
 
 	public int getIndex(Model model)
 	{
 		return models.indexOf(model);
 	}
 
 	public Model getModel(int index)
 	{
 		return models.get(index);
 	}
 
 	public int size()
 	{
 		return models.size();
 	}
 
 	public boolean contains(Model model)
 	{
 		return models.contains(model);
 	}
 
 	public BitSet getBitSet()
 	{
 		return bitSet;
 	}
 
 	public boolean containsModelIndex(int modelIndex)
 	{
 		for (Model m : models)
 			if (m.getModelIndex() == modelIndex)
 				return true;
 		return false;
 	}
 
 	public List<Model> getModels()
 	{
 		return models;
 	}
 
 	public List<Model> getModelsInOrder(final MoleculeProperty property, HighlightSorting sorting)
 	{
 		String key = property + "_" + sorting;
 		if (!modelsOrderedByPropterty.containsKey(key))
 		{
 			List<Model> m = new ArrayList<Model>();
 			for (Model model : models)
 				m.add(model);
 			final HighlightSorting finalSorting;
 			if (sorting == HighlightSorting.Med)
 				finalSorting = HighlightSorting.Max;
 			else
 				finalSorting = sorting;
 			Collections.sort(m, new Comparator<Model>()
 			{
 				@Override
 				public int compare(Model o1, Model o2)
 				{
 					int res;
 					if (o1 == null)
 					{
 						if (o2 == null)
 							res = 0;
 						else
 							res = 1;
 					}
 					else if (o2 == null)
 						res = -1;
 					else if (property.getType() == Type.NUMERIC)
 					{
 						Double d1 = o1.getDoubleValue(property);
 						Double d2 = o2.getDoubleValue(property);
 						if (d1 == null)
 						{
 							if (d2 == null)
 								res = 0;
 							else
 								res = 1;
 						}
 						else if (d2 == null)
 							res = -1;
 						else
 							res = d1.compareTo(d2);
 					}
 					else
 						res = (o1.getStringValue(property) + "").compareTo(o2.getStringValue(property) + "");
 					return (finalSorting == HighlightSorting.Max ? -1 : 1) * res;
 				}
 			});
 			if (sorting == HighlightSorting.Med)
 			{
 				//				System.err.println("max order: ");
 				//				for (Model mm : m)
 				//					System.err.print(mm.getStringValue(property) + " ");
 				//				System.err.println();
 
 				/**
 				 * median sorting:
 				 * - first order by max to compute median
 				 * - create a dist-to-median array, sort models according to that array
 				 */
 				Model medianModel = m.get(m.size() / 2);
 				//				System.err.println(medianModel.getStringValue(property));
 				double distToMedian[] = new double[m.size()];
 				if (property.getType() == Type.NUMERIC)
 				{
 					Double med = medianModel.getDoubleValue(property);
 					for (int i = 0; i < distToMedian.length; i++)
 					{
 						Double d = m.get(i).getDoubleValue(property);
 						if (med == null)
 						{
 							if (d == null)
 								distToMedian[i] = 0;
 							else
 								distToMedian[i] = Double.MAX_VALUE;
 						}
 						else if (d == null)
 							distToMedian[i] = Double.MAX_VALUE;
 						else
 							distToMedian[i] = Math.abs(med - d);
 					}
 				}
 				else
 				{
 					String medStr = medianModel.getStringValue(property);
 					for (int i = 0; i < distToMedian.length; i++)
 						distToMedian[i] = Math.abs((m.get(i).getStringValue(property) + "").compareTo(medStr + ""));
 				}
 				int order[] = ArrayUtil.getOrdering(distToMedian, true);
 				Model a[] = new Model[m.size()];
 				Model s[] = ArrayUtil.sortAccordingToOrdering(order, m.toArray(a));
 				m = ArrayUtil.toList(s);
 
 				//				System.err.println("med order: ");
 				//				for (Model mm : m)
 				//					System.err.print(mm.getStringValue(property) + " ");
 				//				System.err.println();
 			}
 			modelsOrderedByPropterty.put(key, m);
 		}
 		//		System.err.println("in order: ");
 		//		for (Model m : order.get(key))
 		//			System.err.print(m.getModelOrigIndex() + " ");
 		//		System.err.println("");
 		return modelsOrderedByPropterty.get(key);
 	}
 
 	public String getSubstructureSmarts(SubstructureSmartsType type)
 	{
 		return clusterData.getSubstructureSmarts(type);
 	}
 
 	public void remove(int[] modelIndices)
 	{
 		List<Model> toDel = new ArrayList<Model>();
 		for (int i : modelIndices)
 			toDel.add(getModelWithModelIndex(i));
 		BitSet bs = new BitSet();
 		for (Model m : toDel)
 		{
 			bs.or(m.getBitSet());
 			models.remove(m);
 		}
 		View.instance.hide(bs);
 
 		modelsOrderedByPropterty.clear();
 
 		update();
 	}
 
 	public boolean isWatched()
 	{
 		return watched;
 	}
 
 	public void setWatched(boolean watched)
 	{
 		this.watched = watched;
 	}
 
 	public boolean isVisible()
 	{
 		return visible;
 	}
 
 	public void setVisible(boolean visible)
 	{
 		this.visible = visible;
 	}
 
 	public void setHighlighProperty(MoleculeProperty highlightProp)
 	{
 		this.highlightProp = highlightProp;
 	}
 
 	public MoleculeProperty getHighlightProperty()
 	{
 		return highlightProp;
 	}
 
 	public void setHighlightSorting(HighlightSorting highlightSorting)
 	{
 		this.highlightSorting = highlightSorting;
 	}
 
 	public HighlightSorting getHighlightSorting()
 	{
 		return highlightSorting;
 	}
 
 	public void setSomeModelsHidden(boolean someModelsHidden)
 	{
 		this.someModelsHidden = someModelsHidden;
 	}
 
 	public boolean someModelsHidden()
 	{
 		return someModelsHidden;
 	}
 
 	public String[] getStringValues(MoleculeProperty property, Model excludeModel)
 	{
 		List<String> l = new ArrayList<String>();
 		for (int i = 0; i < models.size(); i++)
 		{
 			Model m = models.get(i);
 			if (m != excludeModel)
 				l.add(m.getStringValue(property));
 		}
 		String v[] = new String[l.size()];
 		return l.toArray(v);
 	}
 
 	public Double[] getDoubleValues(MoleculeProperty property)
 	{
 		Double v[] = new Double[models.size()];
 		for (int i = 0; i < v.length; i++)
 			v[i] = models.get(i).getDoubleValue(property);
 		return v;
 	}
 
 	public void setShowLabel(boolean showLabel)
 	{
 		this.showLabel = showLabel;
 	}
 
 	public boolean isShowLabel()
 	{
 		return showLabel;
 	}
 
 	public boolean isSuperimposed()
 	{
 		return superimposed;
 	}
 
 	public boolean isSpreadable()
 	{
 		return !allCompoundsHaveSamePosition;
 	}
 
 	@Override
 	public Vector3f getCenter(boolean superimposed)
 	{
 		if (superimposed)
 			return superimposeCenter;
 		else
 			return nonSuperimposeCenter;
 	}
 
 	@Override
 	public float getDiameter(boolean superimposed)
 	{
 		if (superimposed)
 			return superimposeDiameter;
 		else
 			return nonSuperimposeDiameter;
 	}
 
 	public void setSuperimposed(boolean superimposed)
 	{
 		this.superimposed = superimposed;
 	}
 }
