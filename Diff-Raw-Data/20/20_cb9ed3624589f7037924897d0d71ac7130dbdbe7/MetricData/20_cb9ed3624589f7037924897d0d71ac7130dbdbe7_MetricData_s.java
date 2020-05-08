 package dna.series.data;
 
 import java.io.IOException;
 
 import dna.io.filesystem.Files;
 import dna.metrics.Metric.MetricType;
 import dna.series.lists.DistributionList;
 import dna.series.lists.ListItem;
 import dna.series.lists.NodeValueListList;
 import dna.series.lists.ValueList;
 import dna.util.ArrayUtils;
 import dna.util.Config;
 import dna.util.Log;
 
 public class MetricData implements ListItem {
 
 	public MetricData(String name) {
 		this.name = name;
 		this.values = new ValueList();
 		this.distributions = new DistributionList();
 		this.nodevalues = new NodeValueListList();
 	}
 
 	public MetricData(String name, MetricType type) {
 		this.name = name;
 		this.type = type;
 		this.values = new ValueList();
 		this.distributions = new DistributionList();
 		this.nodevalues = new NodeValueListList();
 	}
 
 	public MetricData(String name, MetricType type, int sizeValues,
 			int sizeDistributions) {
 		this.name = name;
 		this.type = type;
 		this.values = new ValueList(sizeValues);
 		this.distributions = new DistributionList(sizeDistributions);
 		this.nodevalues = new NodeValueListList();
 	}
 
 	public MetricData(String name, MetricType type, int sizeValues,
 			int sizeDistributions, int sizeNodeValueList) {
 		this.name = name;
 		this.type = type;
 		this.values = new ValueList(sizeValues);
 		this.distributions = new DistributionList(sizeDistributions);
 		this.nodevalues = new NodeValueListList(sizeNodeValueList);
 	}
 
 	public MetricData(String name, MetricType type, Value[] values,
 			Distribution[] distributions) {
 		this(name, type, values.length, distributions.length);
 		for (Value v : values) {
 			this.values.add(v);
 		}
 		for (Distribution d : distributions) {
 			this.distributions.add(d);
 		}
 		this.nodevalues = new NodeValueListList();
 	}
 
 	public MetricData(String name, MetricType type, Value[] values,
 			Distribution[] distributions, NodeValueList[] nodevalues) {
 		this(name, type, values.length, distributions.length, nodevalues.length);
 		for (Value v : values) {
 			this.values.add(v);
 		}
 		for (Distribution d : distributions) {
 			this.distributions.add(d);
 		}
 		for (NodeValueList n : nodevalues) {
 			this.nodevalues.add(n);
 		}
 	}
 
 	public MetricData(String name, MetricType type, ValueList values,
 			DistributionList distributions) {
 		this.name = name;
 		this.type = type;
 		this.values = values;
 		this.distributions = distributions;
 		this.nodevalues = new NodeValueListList();
 	}
 
 	public MetricData(String name, MetricType type, ValueList values,
 			DistributionList distributions, NodeValueListList nodevalues) {
 		this.name = name;
 		this.type = type;
 		this.values = values;
 		this.distributions = distributions;
 		this.nodevalues = nodevalues;
 	}
 
 	private NodeValueListList nodevalues;
 
 	public NodeValueListList getNodeValues() {
 		return this.nodevalues;
 	}
 
 	String name;
 
 	public String getName() {
 		return this.name;
 	}
 
 	private MetricType type;
 
 	public MetricType getType() {
 		return this.type;
 	}
 
 	public void setType(MetricType type) {
 		this.type = type;
 	}
 
 	private ValueList values;
 
 	public ValueList getValues() {
 		return this.values;
 	}
 
 	private DistributionList distributions;
 
 	public DistributionList getDistributions() {
 		return this.distributions;
 	}
 
 	public void write(String dir) throws IOException {
 		this.values.write(dir,
 				Files.getValuesFilename(Config.get("METRIC_DATA_VALUES")));
 		this.distributions.write(dir);
 		this.nodevalues.write(dir);
 	}
 
 	public static MetricData read(String dir, String name, boolean readValues)
 			throws IOException {
 		String[] temp = dir.split(Config.get("PLOT_DELIMITER"));
 		MetricType tempType = MetricType.unknown;
 		try {
 			if (temp[temp.length - 1] == MetricType.exact.name())
 				tempType = MetricType.exact;
 			if (temp[temp.length - 1] == MetricType.heuristic.name())
 				tempType = MetricType.heuristic;
 			if (temp[temp.length - 1] == MetricType.quality.name())
 				tempType = MetricType.quality;
 		} catch (IndexOutOfBoundsException e) {
 			// Log.warn("No metrictype detected for metric " + name + " at " +
 			// dir);
 		}
 		ValueList values = ValueList.read(dir,
 				Files.getValuesFilename(Config.get("METRIC_DATA_VALUES")));
 		DistributionList distributions = DistributionList.read(dir, readValues);
 		NodeValueListList nodevalues = NodeValueListList.read(dir, readValues);
 		return new MetricData(name, tempType, values, distributions, nodevalues);
 	}
 
 	/**
 	 * Tests if two MetricData objects are of the same type. Therefore it checks
 	 * if they got the same distributions and metrics.
 	 * 
 	 * @returns true - when they are of the same type, else false
 	 * 
 	 * @author Rwilmes
 	 * @date 24.06.2013
 	 */
 	public static boolean isSameType(MetricData m1, MetricData m2) {
 		// if(m1.getName().equals(m2.getName()))
 		// return false;
 
 		ValueList list1 = m1.getValues();
 		ValueList list2 = m2.getValues();
 
 		DistributionList dlist1 = m1.getDistributions();
 		DistributionList dlist2 = m2.getDistributions();
 
 		if (list1.size() != list2.size()) {
 			Log.warn("different amount of values on metric " + m1.getName()
 					+ " and metric " + m2.getName());
 			return false;
 		}
 
 		if (dlist1.size() != dlist2.size()) {
 			Log.warn("different amount of distributions on metric "
 					+ m1.getName() + " and metric " + m2.getName());
 			return false;
 		}
 
 		for (String k : list1.getNames()) {
 			if (!list1.get(k).getName().equals(list2.get(k).getName())) {
 				Log.warn("different values on metric " + m1.getName()
 						+ " and metric " + m2.getName());
 				return false;
 			}
 		}
 
 		for (String k : dlist1.getNames()) {
 			if (!dlist1.get(k).getName().equals(dlist2.get(k).getName())) {
 				Log.warn("different distributions on metric " + m1.getName()
 						+ " and metric " + m2.getName());
 				return false;
 			}
 		}
 
 		return true;
 	}
 
 	/**
 	 * Tests if two MetricData objects can be compared, which means one is
 	 * 'exact' and the other one is 'heuristic'. In addition there has to be at
 	 * least on similarity.
 	 * 
 	 * @returns true - when they are of the same type, else false
 	 * 
 	 * @author Rwilmes
 	 * @date 03.08.2013
 	 */
 	public static boolean isComparable(MetricData m1, MetricData m2) {
 		if (m1.getType().equals("exact") && !m2.getType().equals("heuristic")) {
 			Log.warn("Metrics " + m1.getName() + " & " + m2.getName()
 					+ " can't be compared. Type failure");
 			return false;
 		}
 
 		if (m2.getType().equals("exact") && !m1.getType().equals("heuristic")) {
 			Log.warn("Metrics " + m1.getName() + " & " + m2.getName()
 					+ " can't be compared. Type failure");
 			return false;
 		}
 
 		int similarities = 0;
 
 		// check for same values
 		for (String value : m1.getValues().getNames()) {
 			if (m2.getValues().get(value) != null) {
 				similarities++;
 			}
 		}
 		// check for same distributions
 		for (String distribution : m1.getDistributions().getNames()) {
 			if (m2.getDistributions().get(distribution) != null) {
 				similarities++;
 			}
 		}
 		// check for same nodevaluelists
 		for (String nodevalue : m1.getNodeValues().getNames()) {
 			if (m2.getNodeValues().get(nodevalue) != null) {
 				similarities++;
 			}
 		}
 
 		if (similarities > 0)
 			return true;
 		else
 			return false;
 	}
 
 	/**
 	 * Compares an 'exact' and a 'heuristic' MetricData object. Returns a new
 	 * MetricData object containing the quality of the heuristic in every
 	 * comparable value, distribution or nodevaluelist.
 	 * 
 	 * @param m1
 	 *            First MetricData object for comparison
 	 * 
 	 * @param m2
 	 *            Second MetricData object for comparison
 	 * 
 	 * @returns MetricData object containing the heuristics quality
 	 * 
 	 * @author Rwilmes
 	 * @date 08.08.2013
 	 */
 	public static MetricData compare(MetricData m1, MetricData m2) {
 		// check if comparable
 		if (!isComparable(m1, m2)) {
 			Log.warn("Failed attempt to compare metrics " + m1.getName()
 					+ " & " + m2.getName()
 					+ "! Returning empty MetricData object");
 			return new MetricData(null);
 		}
 		// let m1 be the 'exact' metric
 		if (m1.getType() != MetricType.exact) {
 			MetricData temp = m1;
 			m1 = m2;
 			m2 = temp;
 		}
 
 		// check similarities
 		ValueList similarValues = new ValueList();
 		for (String value : m1.getValues().getNames()) {
 			if (m2.getValues().get(value) != null) {
 				similarValues.add(m2.getValues().get(value));
 			}
 		}
 		DistributionList similarDistributions = new DistributionList();
 		for (String distribution : m1.getDistributions().getNames()) {
 			if (m2.getDistributions().get(distribution) != null) {
 				similarDistributions.add(m2.getDistributions()
 						.get(distribution));
 			}
 		}
 		NodeValueListList similarNodeValues = new NodeValueListList();
 		for (String nodevalue : m1.getNodeValues().getNames()) {
 			if (m2.getNodeValues().get(nodevalue) != null) {
 				similarNodeValues.add(m2.getNodeValues().get(nodevalue));
 			}
 		}
 
 		// compare values
 		ValueList comparedValues = new ValueList();
 		for (String value : similarValues.getNames()) {
 			double v1 = m1.getValues().get(value).getValue();
 			double v2 = m2.getValues().get(value).getValue();
 			double quality = 0;
 			double delta = Math.abs(v1 - v2);
 
 			if (v1 == 0 || v2 == 0) {
 				quality = delta;
 			} else {
 				if (v1 > v2)
 					quality = v2 / v1;
 				else
 					quality = v1 / v2;
 			}
 			comparedValues.add(new Value(value
 					+ Config.get("SUFFIX_METRIC_QUALITY"), quality));
 		}
 
 		// compare distributions
 		DistributionList comparedDistributions = new DistributionList();
 		for (String distribution : similarDistributions.getNames()) {
 			boolean compared = false;
 			if (!compared
 					&& m1.getDistributions().get(distribution) instanceof DistributionInt
 					&& m2.getDistributions().get(distribution) instanceof DistributionInt) {
 				// compare DistributionInt objects
 				int[] values1 = ((DistributionInt) m1.getDistributions().get(
 						distribution)).getIntValues();
 				int[] values2 = ((DistributionInt) m2.getDistributions().get(
 						distribution)).getIntValues();
 				int[] diffAbs = new int[Math
 						.max(values1.length, values2.length)];
 				double[] diffRel = new double[diffAbs.length];
 
 				int denom1 = ((DistributionInt) m1.getDistributions().get(
 						distribution)).getDenominator();
 				int denom2 = ((DistributionInt) m2.getDistributions().get(
 						distribution)).getDenominator();
 
 				for (int i = 0; i < diffAbs.length; i++) {
 					int v1 = 0;
 					int v2 = 0;
 
 					try {
 						v1 = values1[i] * denom2;
 					} catch (ArrayIndexOutOfBoundsException e) {
 					}
 					try {
 						v2 = values2[i] * denom1;
 					} catch (ArrayIndexOutOfBoundsException e) {
 					}
 
 					diffAbs[i] = v1 - v2;
 
 					if (v2 == 0) {
 						diffRel[i] = Double.MAX_VALUE;
 					} else {
 						diffRel[i] = v1 / v2;
 					}
 				}
 				// add absolute comparison
 				comparedDistributions.add(new DistributionInt(distribution
 						+ "_abs", diffAbs, denom1 * denom2, ArrayUtils
 						.sum(diffAbs), ArrayUtils.min(diffAbs), ArrayUtils
 						.max(diffAbs), ArrayUtils.med(diffAbs), ArrayUtils
 						.avg(diffAbs)));
 				// add relative comparison
 				comparedDistributions.add(new DistributionDouble(distribution
 						+ "_rel", diffRel, ArrayUtils.sum(diffRel), ArrayUtils
 						.min(diffRel), ArrayUtils.max(diffRel), ArrayUtils
 						.med(diffRel), ArrayUtils.avg(diffRel)));
 				compared = true;
 			}
 			if (!compared
 					&& m1.getDistributions().get(distribution) instanceof DistributionLong
 					&& m2.getDistributions().get(distribution) instanceof DistributionLong) {
 				// compare DistributionLong objects
 				long[] values1 = ((DistributionLong) m1.getDistributions().get(
 						distribution)).getLongValues();
 				long[] values2 = ((DistributionLong) m2.getDistributions().get(
 						distribution)).getLongValues();
 				long[] diffAbs = new long[Math.max(values1.length,
 						values2.length)];
 				double[] diffRel = new double[diffAbs.length];
 
 				long denom1 = ((DistributionLong) m1.getDistributions().get(
 						distribution)).getDenominator();
 				long denom2 = ((DistributionLong) m2.getDistributions().get(
 						distribution)).getDenominator();
 
 				for (int i = 0; i < diffAbs.length; i++) {
 					long v1 = 0;
 					long v2 = 0;
 
 					try {
 						v1 = values1[i] * denom2;
 					} catch (ArrayIndexOutOfBoundsException e) {
 					}
 					try {
 						v2 = values2[i] * denom1;
 					} catch (ArrayIndexOutOfBoundsException e) {
 					}
 
 					diffAbs[i] = v1 - v2;
 
 					if (v2 == 0) {
 						diffRel[i] = Double.MAX_VALUE;
 					} else {
 						diffRel[i] = v1 / v2;
 					}
 				}
 				// add absolute comparison
 				comparedDistributions.add(new DistributionLong(distribution
 						+ "_abs", diffAbs, denom1 * denom2, ArrayUtils
 						.sum(diffAbs), ArrayUtils.min(diffAbs), ArrayUtils
 						.max(diffAbs), ArrayUtils.med(diffAbs), ArrayUtils
 						.avg(diffAbs)));
 				// add relative comparison
 				comparedDistributions.add(new DistributionDouble(distribution
 						+ "_rel", diffRel, ArrayUtils.sum(diffRel), ArrayUtils
 						.min(diffRel), ArrayUtils.max(diffRel), ArrayUtils
 						.med(diffRel), ArrayUtils.avg(diffRel)));
 				compared = true;
 			}
 			if (!compared
 					&& m1.getDistributions().get(distribution) instanceof DistributionDouble
 					&& m2.getDistributions().get(distribution) instanceof DistributionDouble) {
 				// compare DistributionDouble objects
 				double[] values1 = ((DistributionDouble) m1.getDistributions()
 						.get(distribution)).getDoubleValues();
 				double[] values2 = ((DistributionDouble) m2.getDistributions()
 						.get(distribution)).getDoubleValues();
 
 				double[] diffAbs = new double[Math.max(values1.length,
 						values2.length)];
 				double[] diffRel = new double[diffAbs.length];
 
 				for (int i = 0; i < diffAbs.length; i++) {
 					double v1 = 0;
 					double v2 = 0;
 					try {
 						v1 = values1[i];
 					} catch (ArrayIndexOutOfBoundsException e) {
 					}
 					try {
 						v2 = values2[i];
 					} catch (ArrayIndexOutOfBoundsException e) {
 					}
 					diffAbs[i] = v1 - v2;
 
 					if (v2 == 0)
 						diffRel[i] = Double.MAX_VALUE;
 					else
 						diffRel[i] = v1 / v2;
 				}
 				// add absolute comparison
 				comparedDistributions.add(new DistributionDouble(distribution
 						+ "_abs", diffAbs, ArrayUtils.sum(diffAbs), ArrayUtils
 						.min(diffAbs), ArrayUtils.max(diffAbs), ArrayUtils
 						.med(diffAbs), ArrayUtils.avg(diffAbs)));
 				// add relative comparison
 				comparedDistributions.add(new DistributionDouble(distribution
 						+ "_rel", diffRel, ArrayUtils.sum(diffRel), ArrayUtils
 						.min(diffRel), ArrayUtils.max(diffRel), ArrayUtils
 						.med(diffRel), ArrayUtils.avg(diffRel)));
 				compared = true;
 			}
 			if (!compared) {
 				// compare Distribution objects that are neither
 				// DistributionInt/Long nor DistributionDouble
 				double[] values1 = (m1.getDistributions().get(distribution))
 						.getValues();
 				double[] values2 = (m2.getDistributions().get(distribution))
 						.getValues();
 
 				double[] diffAbs = new double[Math.max(values1.length,
 						values2.length)];
 				double[] diffRel = new double[diffAbs.length];
 
 				for (int i = 0; i < diffAbs.length; i++) {
 					double v1 = 0;
 					double v2 = 0;
 					try {
 						v1 = values1[i];
 					} catch (ArrayIndexOutOfBoundsException e) {
 					}
 					try {
 						v2 = values2[i];
 					} catch (ArrayIndexOutOfBoundsException e) {
 					}
 					diffAbs[i] = v1 - v2;
 
 					if (v2 == 0)
 						diffRel[i] = Double.MAX_VALUE;
 					else
 						diffRel[i] = v1 / v2;
 				}
 				// add absolute comparison
 				comparedDistributions.add(new DistributionDouble(distribution
 						+ "_abs", diffAbs, ArrayUtils.sum(diffAbs), ArrayUtils
 						.min(diffAbs), ArrayUtils.max(diffAbs), ArrayUtils
 						.med(diffAbs), ArrayUtils.avg(diffAbs)));
 				// add relative comparison
 				comparedDistributions.add(new DistributionDouble(distribution
 						+ "_rel", diffRel, ArrayUtils.sum(diffRel), ArrayUtils
 						.min(diffRel), ArrayUtils.max(diffRel), ArrayUtils
 						.med(diffRel), ArrayUtils.avg(diffRel)));
 				compared = true;
 			}
 		}
 
 		// compare nodevaluelists
 		NodeValueListList comparedNodeValues = new NodeValueListList();
 		for (String nodevalue : similarNodeValues.getNames()) {
 			double[] values1 = m1.getNodeValues().get(nodevalue).getValues();
 			double[] values2 = m2.getNodeValues().get(nodevalue).getValues();
 			double[] qualities = new double[values1.length];
 
 			for (int i = 0; i < values1.length; i++) {
 				double v1 = values1[i];
 				double v2 = values2[i];
 				double quality = 0;
 				double delta = Math.abs(v1 - v2);
 
 				if (v1 == 0 || v2 == 0) {
 					quality = delta;
 				} else {
 					if (v1 > v2)
 						quality = v2 / v1;
 					else
 						quality = v1 / v2;
 				}
 				qualities[i] = quality;
 			}
 			comparedNodeValues.add(new NodeValueList(nodevalue
 					+ Config.get("SUFFIX_METRIC_QUALITY"), qualities));
 		}
		return new MetricData(m2.getName(), MetricType.quality, comparedValues,
				comparedDistributions, comparedNodeValues);
 	}
 
 	/**
 	 * Counts the similarities between two MetricData objects. Every Value,
 	 * Distribution or NodeValueList of the same name counts as one similarity.
 	 * 
 	 * @param m1
 	 *            First MetricData object
 	 * @param m2
 	 *            Second MetricData object
 	 * @return Amount of Similarities.
 	 */
 	public static int countSimilarities(MetricData m1, MetricData m2) {
 		int similarities = 0;
 		// check if comparable
 		if (!isComparable(m1, m2)) {
 			return similarities;
 		}
 		// count similarities
 		for (String value : m1.getValues().getNames()) {
 			if (m2.getValues().get(value) != null) {
 				similarities++;
 			}
 		}
 		for (String distribution : m1.getDistributions().getNames()) {
 			if (m2.getDistributions().get(distribution) != null) {
 				similarities++;
 			}
 		}
 		for (String nodevalue : m1.getNodeValues().getNames()) {
 			if (m2.getNodeValues().get(nodevalue) != null) {
 				similarities++;
 			}
 		}
 		return similarities;
 	}
 }
