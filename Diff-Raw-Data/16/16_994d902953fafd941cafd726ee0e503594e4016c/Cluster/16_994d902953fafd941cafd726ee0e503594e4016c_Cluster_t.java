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
 import dataInterface.CompoundProperty;
 import dataInterface.CompoundProperty.Type;
 import dataInterface.SubstructureSmartsType;
 
 public class Cluster implements Zoomable
 {
 	private Vector<Compound> compounds;
 	private ClusterData clusterData;
 
 	private BitSet bitSet;
 	private BitSet dotModeDisplayBitSet;
 
 	private boolean superimposed = true;
 	private float superimposeDiameter;
 	private float nonSuperimposeDiameter;
 	private Vector3f superimposeCenter;
 	private Vector3f nonSuperimposeCenter;
 	private boolean allCompoundsHaveSamePosition = false;
 
 	HashMap<String, List<Compound>> compoundsOrderedByPropterty = new HashMap<String, List<Compound>>();
 
 	private boolean watched;
 	private boolean visible;
 	private CompoundProperty highlightProp;
 	private HighlightSorting highlightSorting;
 	private boolean someCompoundsHidden;
 	private boolean showLabel = false;
 
 	public Cluster(dataInterface.ClusterData clusterData, int begin, int endExcl) //boolean firstCluster, 
 	{
 		this.clusterData = clusterData;
 
 		//		int before = firstCluster ? 0 : View.instance.getCompoundCount();
 		//		View.instance.loadCompoundFromFile(null, clusterData.getFilename(), null, null, !firstCluster, null, null, 0);
 		//		int after = View.instance.getCompoundCount();
 
 		//		if ((after - before) != clusterData.getSize())
 		//			throw new IllegalStateException("compounds in file: " + (after - before) + " != compound props passed: "
 		//					+ clusterData.getSize());
 
 		if ((endExcl - begin) != clusterData.getSize())
 			throw new IllegalStateException("should be: " + (endExcl - begin) + " != compound props passed: "
 					+ clusterData.getSize());
 
 		compounds = new Vector<Compound>();
 		int mCount = 0;
 		//for (int i = before; i < after; i++)
 		for (int i = begin; i < endExcl; i++)
 			compounds.add(new Compound(i, clusterData.getCompounds().get(mCount++)));
 
 		if (View.instance != null) // for export without graphics
 			update();
 	}
 
 	boolean alignedCompoundsCalibrated = false;
 
 	public void updatePositions()
 	{
 		// the actual compound position that is stored in compound is never changed (depends only on scaling)
 		// this method moves all compounds to the cluster position
 
 		// recalculate non-superimposed diameter
 		update();
 
 		if (!clusterData.isAligned())
 		{
 			// the compounds have not been aligned
 			// the compounds may have a center != 0, calibrate to 0
 			for (Compound m : compounds)
 				m.moveTo(new Vector3f(0f, 0f, 0f));
 		}
 		else
 		{
 			// the compounds are aligned, cannot calibrate to 0, this would brake the alignment
 			// however, the compound center may have an offset, calculate and remove
 			if (!alignedCompoundsCalibrated)
 			{
 				Vector3f[] origCenters = new Vector3f[compounds.size()];
 				for (int i = 0; i < origCenters.length; i++)
 					origCenters[i] = compounds.get(i).origCenter;
 				Vector3f center = Vector3fUtil.center(origCenters);
 				for (int i = 0; i < origCenters.length; i++)
 					compounds.get(i).origCenter.sub(center);
 				alignedCompoundsCalibrated = true;
 			}
 			for (Compound m : compounds)
 				m.moveTo(m.origCenter);
 		}
 
 		// translate compounds to the cluster position
 		View.instance.setAtomCoordRelative(getCenter(true), getBitSet());
 	}
 
 	public String getSummaryStringValue(CompoundProperty property)
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
 		for (Compound m : compounds)
 			bitSet.or(m.getBitSet());
 
 		dotModeDisplayBitSet = new BitSet();
 		for (Compound m : compounds)
 			dotModeDisplayBitSet.or(m.getDotModeDisplayBitSet());
 
 		// updating (unscaled!) cluster position
 		// this is only needed in case a compound was removed
 		Vector3f[] positions = ClusteringUtil.getCompoundPositions(this);
 		superimposeCenter = Vector3fUtil.center(positions);
 		nonSuperimposeCenter = Vector3fUtil.centerConvexHull(positions);
 
 		superimposeDiameter = -1;
 		for (Compound m : compounds)
 			superimposeDiameter = Math.max(superimposeDiameter, m.getDiameter());
 
 		// recompute diameter, depends on the scaling
 		positions = ClusteringUtil.getCompoundPositions(this);
 		float maxCompoundDist = Vector3fUtil.maxDist(positions);
 		allCompoundsHaveSamePosition = maxCompoundDist == 0;
 
 		// nonSuperimposeDiameter ignores size of compounds, just uses the compound positions
 		// for very small diameter: should be at least as big as superimposed one
 		nonSuperimposeDiameter = Math.max(superimposeDiameter, maxCompoundDist);
 	}
 
 	public Compound getCompoundWithCompoundIndex(int compoundIndex)
 	{
 		for (Compound m : compounds)
 			if (m.getCompoundIndex() == compoundIndex)
 				return m;
 		return null;
 	}
 
 	public int getIndex(Compound compound)
 	{
 		return compounds.indexOf(compound);
 	}
 
 	public Compound getCompound(int index)
 	{
 		return compounds.get(index);
 	}
 
 	public int size()
 	{
 		return compounds.size();
 	}
 
 	public boolean contains(Compound compound)
 	{
 		return compounds.contains(compound);
 	}
 
 	public BitSet getBitSet()
 	{
 		return bitSet;
 	}
 
 	public BitSet getDotModeDisplayBitSet()
 	{
 		return dotModeDisplayBitSet;
 	}
 
 	public boolean containsCompoundIndex(int compoundIndex)
 	{
 		for (Compound m : compounds)
 			if (m.getCompoundIndex() == compoundIndex)
 				return true;
 		return false;
 	}
 
 	public List<Compound> getCompounds()
 	{
 		return compounds;
 	}
 
 	public List<Compound> getCompoundsInOrder(final CompoundProperty property, HighlightSorting sorting)
 	{
 		String key = property + "_" + sorting;
 		if (!compoundsOrderedByPropterty.containsKey(key))
 		{
 			List<Compound> c = new ArrayList<Compound>();
 			for (Compound compound : compounds)
 				c.add(compound);
 			final HighlightSorting finalSorting;
 			if (sorting == HighlightSorting.Median)
 				finalSorting = HighlightSorting.Max;
 			else
 				finalSorting = sorting;
 			Collections.sort(c, new Comparator<Compound>()
 			{
 				@Override
 				public int compare(Compound o1, Compound o2)
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
 			if (sorting == HighlightSorting.Median)
 			{
 				//				Settings.LOGGER.warn("max order: ");
 				//				for (Compound mm : m)
 				//					Settings.LOGGER.warn(mm.getStringValue(property) + " ");
 				//				Settings.LOGGER.warn();
 
 				/**
 				 * median sorting:
 				 * - first order by max to compute median
 				 * - create a dist-to-median array, sort compounds according to that array
 				 */
 				Compound medianCompound = c.get(c.size() / 2);
 				//				Settings.LOGGER.warn(medianCompound.getStringValue(property));
 				double distToMedian[] = new double[c.size()];
 				if (property.getType() == Type.NUMERIC)
 				{
 					Double med = medianCompound.getDoubleValue(property);
 					for (int i = 0; i < distToMedian.length; i++)
 					{
 						Double d = c.get(i).getDoubleValue(property);
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
 					String medStr = medianCompound.getStringValue(property);
 					for (int i = 0; i < distToMedian.length; i++)
 						distToMedian[i] = Math.abs((c.get(i).getStringValue(property) + "").compareTo(medStr + ""));
 				}
 				int order[] = ArrayUtil.getOrdering(distToMedian, true);
 				Compound a[] = new Compound[c.size()];
 				Compound s[] = ArrayUtil.sortAccordingToOrdering(order, c.toArray(a));
 				c = ArrayUtil.toList(s);
 
 				//				Settings.LOGGER.warn("med order: ");
 				//				for (Compound mm : m)
 				//					Settings.LOGGER.warn(mm.getStringValue(property) + " ");
 				//				Settings.LOGGER.warn();
 			}
 			compoundsOrderedByPropterty.put(key, c);
 		}
 		//		Settings.LOGGER.warn("in order: ");
 		//		for (Compound m : order.get(key))
 		//			Settings.LOGGER.warn(m.getCompoundOrigIndex() + " ");
 		//		Settings.LOGGER.warn("");
 		return compoundsOrderedByPropterty.get(key);
 	}
 
 	public String getSubstructureSmarts(SubstructureSmartsType type)
 	{
 		return clusterData.getSubstructureSmarts(type);
 	}
 
 	public void remove(int[] compoundIndices)
 	{
		System.out.println("to remove from cluster: " + ArrayUtil.toString(compoundIndices));
 		List<Compound> toDel = new ArrayList<Compound>();
		int[] toDelIndex = new int[compoundIndices.length];

		int count = 0;
 		for (int i : compoundIndices)
		{
			Compound c = getCompoundWithCompoundIndex(i);
			toDel.add(c);
			toDelIndex[count++] = getIndex(c);
		}
 		BitSet bs = new BitSet();
 		for (Compound m : toDel)
 		{
 			bs.or(m.getBitSet());
 			compounds.remove(m);
 		}
 		View.instance.hide(bs);
 
 		compoundsOrderedByPropterty.clear();
		clusterData.remove(toDelIndex);
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
 
 	public void setHighlighProperty(CompoundProperty highlightProp)
 	{
 		this.highlightProp = highlightProp;
 	}
 
 	public CompoundProperty getHighlightProperty()
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
 
 	public void setSomeCompoundsHidden(boolean someCompoundsHidden)
 	{
 		this.someCompoundsHidden = someCompoundsHidden;
 	}
 
 	public boolean someCompoundsHidden()
 	{
 		return someCompoundsHidden;
 	}
 
 	public String[] getStringValues(CompoundProperty property, Compound excludeCompound)
 	{
 		List<String> l = new ArrayList<String>();
 		for (Compound c : compounds)
 			if (c != excludeCompound && c.getStringValue(property) != null)
 				l.add(c.getStringValue(property));
 		String v[] = new String[l.size()];
 		return l.toArray(v);
 	}
 
 	public Double[] getDoubleValues(CompoundProperty property)
 	{
 		Double v[] = new Double[compounds.size()];
 		for (int i = 0; i < v.length; i++)
 			v[i] = compounds.get(i).getDoubleValue(property);
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
 
 	public int numMissingValues(CompoundProperty p)
 	{
 		return clusterData.numMissingValues(p);
 	}
 
 	public boolean containsNotClusteredCompounds()
 	{
 		return clusterData.containsNotClusteredCompounds();
 	}
 }
