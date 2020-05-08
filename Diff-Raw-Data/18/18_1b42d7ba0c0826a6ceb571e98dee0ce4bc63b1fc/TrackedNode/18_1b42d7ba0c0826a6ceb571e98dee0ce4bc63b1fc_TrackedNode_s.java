 package org.knime.knip.tracking.data.graph;
 
 import java.awt.geom.Rectangle2D;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import net.imglib2.Localizable;
 import net.imglib2.RealPoint;
 import net.imglib2.type.logic.BitType;
 import net.imglib2.type.numeric.RealType;
 
 import org.knime.core.data.filestore.FileStoreFactory;
 import org.knime.knip.base.data.img.ImgPlusValue;
 import org.knime.knip.tracking.util.OffsetHandling;
 import org.knime.knip.tracking.util.Permutation;
 import org.knime.knip.tracking.util.TrackingConstants;
 import org.knime.network.core.api.Feature;
 import org.knime.network.core.api.KPartiteGraphView;
 import org.knime.network.core.api.Partition;
 import org.knime.network.core.api.PersistentObject;
 import org.knime.network.core.core.exception.InvalidFeatureException;
 import org.knime.network.core.core.exception.PersistenceException;
 
 import fiji.plugin.trackmate.tracking.TrackableObject;
 import fiji.plugin.trackmate.tracking.TrackingUtils;
 
 public class TrackedNode extends GraphObject implements
 		Comparable<TrackedNode>, TrackableObject {
 
 	private final static String CENTROID_X = "Centroid X";
 	private final static String CENTROID_Y = "Centroid Y";
 	private final static String CENTROID_Z = "Centroid Z";
 
 	private final static String CENTROID_TIME = "Centroid Time";
 
 	private final List<Edge> outgoing = new LinkedList<Edge>();
 	private final List<Edge> incoming = new LinkedList<Edge>();
 
 	// this
 	private RealPoint point;
 	private double frame;
 	private HashMap<String, Double> m_features;
 
 	public TrackedNode(KPartiteGraphView<PersistentObject, Partition> net,
 			PersistentObject pObj) {
 		super(net, pObj);
 
 		m_features = new HashMap<String, Double>();
 
 		double x = getDoubleFeature(CENTROID_X);
 		double y = getDoubleFeature(CENTROID_Y);
 		double z = getDoubleFeature(CENTROID_Z);
 		double time = getDoubleFeature(CENTROID_TIME);
 
 		if (Double.isNaN(z) && !Double.isNaN(time))
 			point = new RealPoint(x, y, time);
 		else if (!Double.isNaN(z) && Double.isNaN(time))
 			point = new RealPoint(x, y, z);
 		else if (!Double.isNaN(z) && !Double.isNaN(time))
 			point = new RealPoint(x, y, z, time);
 		
 		frame = point.getDoublePosition(point.numDimensions() - 1);
 	}
 
 	public void addEdge(Edge edge) {
 		if (edge.getStartNode() == this) {
 			outgoing.add(edge);
 		} else if (edge.getEndNode() == this) {
 			incoming.add(edge);
 		} else {
 			throw new IllegalArgumentException("Edge " + edge
 					+ " does not contain node " + this);
 		}
 	}
 
 	/**
 	 * Last position of this {@link Localizable} is Time
 	 * 
 	 * @return
 	 */
 	public String getPartition() {
 		return "t" + frame();
 	}
 
 	/**
 	 * Creates a copy of this node in the {@link TransitionGraph}. The copy is
 	 * returned.
 	 * 
 	 * @param target
 	 *            the {@link TransitionGraph}
 	 * @return the copy of target.
 	 */
 	public TrackedNode createCopyIn(TransitionGraph target) {
		System.out.println("Copying node " + getID());
 		try {
 			// create node in net, but do not add to graph yet
 			PersistentObject targetnode = target.getNet().createNode(this.getID());
 			for (Feature feature : this.getNetwork().getFeatures()) {
 				Object value = this.getNetwork().getFeatureValue(
 						this.getPersistentObject(), feature);
 				// System.out.println(feature + " = " + value);
 				if (value != null) {
 					target.getNet().defineFeature(feature);
 					target.getNet().addFeature(targetnode, feature.getName(),
 							value);
 					//System.out.println("adding feature " + feature.getName() + " with value " + value + " to " + targetnode.getId());
 				}
 			}
 			// now with all features tracked node could finally be created and added
 			TrackedNode node = target.createNode(this.getID(), true);
			target.addNode(node);
 			return node;
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	public List<Edge> getOutgoingEdges() {
 		return outgoing;
 	}
 
 	@Override
 	public int compareTo(TrackedNode o) {
 		return this.getID().compareTo(o.getID());
 	}
 
 	public double distanceTo(TrackedNode otherNode) {
 		double minDist = Double.MAX_VALUE;
 
 		// check if # followers is equal
 		if (getOutgoingEdges().size() != otherNode.getOutgoingEdges().size()) {
 			return Double.NaN;
 		}
 
 		// no further edges -> no further distance
 		if (getOutgoingEdges().size() == 0)
 			minDist = 0;
 
 		// 1. feature dist (euclidean)
 		double featureDist = featureDistance(otherNode);
 		// 2. permutate otherEdges for all combinations
 		List<Edge[]> otherEdgesPermutations = Permutation
 				.getAllPermutations(otherNode.getOutgoingEdges());
 		for (Edge[] permutation : otherEdgesPermutations) {
 			int index = 0;
 			double dist = 0.0;
 			for (Edge edge : getOutgoingEdges()) {
 				Edge otherEdge = permutation[index++];
 				dist += edge.distanceTo(otherEdge);
 			}
 			if (dist < minDist) {
 				minDist = dist;
 			}
 		}
 		return featureDist + minDist;
 	}
 
 	public long[] getOffset() {
 		return OffsetHandling
 				.decode(getStringFeature(TrackingConstants.FEATURE_BITMASK_OFFSET));
 	}
 
 	public Rectangle2D getImageRectangle() {
 		try {
 			@SuppressWarnings({ "rawtypes", "unchecked" })
 			ImgPlusValue<? extends RealType<?>> ipv = (ImgPlusValue) getNetwork()
 					.getFeatureCell(
 							FileStoreFactory
 									.createNotInWorkflowFileStoreFactory(),
 							getPersistentObject(),
 							TrackingConstants.FEATURE_BITMASK);
 			long[] offset = getOffset();
 			return new Rectangle2D.Double(ipv.getMinimum()[0] + offset[0],
 					ipv.getMinimum()[1] + offset[1], ipv.getDimensions()[0],
 					ipv.getDimensions()[1]);
 		} catch (PersistenceException e) {
 			e.printStackTrace();
 		} catch (InvalidFeatureException e) {
 			e.printStackTrace();
 		}
 		return new Rectangle2D.Double();
 	}
 
 	@SuppressWarnings("unchecked")
 	public ImgPlusValue<BitType> getBitmask() {
 		try {
 			return (ImgPlusValue<BitType>) getNetwork().getFeatureCell(
 					FileStoreFactory.createNotInWorkflowFileStoreFactory(),
 					getPersistentObject(), TrackingConstants.FEATURE_BITMASK);
 		} catch (PersistenceException e) {
 			e.printStackTrace();
 		} catch (InvalidFeatureException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	@SuppressWarnings({ "unchecked" })
 	public ImgPlusValue<? extends RealType<?>> getSegmentImage() {
 		try {
 			return (ImgPlusValue<? extends RealType<?>>) getNetwork()
 					.getFeatureCell(
 							FileStoreFactory
 									.createNotInWorkflowFileStoreFactory(),
 							getPersistentObject(),
 							TrackingConstants.FEATURE_SEGMENT_IMG);
 		} catch (PersistenceException e) {
 			e.printStackTrace();
 		} catch (InvalidFeatureException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	public double euclideanDistanceTo(TrackedNode targetNode) {
 		return Math.sqrt(TrackingUtils.squareDistanceTo(this, targetNode));
 	}
 
 	public double[] getDistanceVector() {
 		double[] vec = new double[getDoubleFeatures().size()];
 		Iterator<String> it = getDoubleFeatures().iterator();
 		for (int i = 0; i < vec.length; i++) {
 			String featName = it.next();
 			vec[i] = getDoubleFeature(featName);
 			for (Edge edge : outgoing) {
 				TrackedNode endNode = edge.getEndNode();
 				vec[i] -= endNode.getDoubleFeature(featName);
 			}
 		}
 		return vec;
 	}
 
 	// Nodes might exist as copy in different transition graphs, make them
 	// comparable
 	@Override
 	public boolean equals(Object obj) {
 		if (obj instanceof TrackedNode) {
 			return getID().equals(((TrackedNode) obj).getID());
 		}
 		return super.equals(obj);
 	}
 
 	// same as above
 	@Override
 	public int hashCode() {
 		return getID().hashCode();
 	}
 
 	public List<Edge> getIncomingEdges() {
 		return incoming;
 	}
 
 	@Override
 	public void localize(float[] position) {
 		point.localize(position);
 	}
 
 	@Override
 	public void localize(double[] position) {
 		point.localize(position);
 	}
 
 	@Override
 	public float getFloatPosition(int d) {
 		return point.getFloatPosition(d);
 	}
 
 	@Override
 	public double getDoublePosition(int d) {
 		return point.getDoublePosition(d);
 	}
 
 	@Override
 	public int numDimensions() {
 		return point.numDimensions();
 	}
 
 	@Override
 	public Double getFeature(String feature) {
 		if (m_features.containsKey(feature)) {
 			return m_features.get(feature);
 		}
 
 		try {
 			return getNetwork()
 					.getDoubleFeature(getPersistentObject(), feature);
 		} catch (InvalidFeatureException e) {
 			throw new RuntimeException(e);
 		} catch (PersistenceException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	@Override
 	public void putFeature(String feature, Double value) {
 		m_features.put(feature, value);
 	}
 
 	@Override
 	public int frame() {
 		return (int) frame;
 	}
 
 	@Override
 	public int ID() {
 		return getID().hashCode();
 	}
 }
