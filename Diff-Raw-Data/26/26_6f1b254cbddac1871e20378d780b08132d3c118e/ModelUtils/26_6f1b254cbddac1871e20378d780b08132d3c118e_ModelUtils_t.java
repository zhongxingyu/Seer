 package com.netxforge.netxstudio.common.model;
 
 import java.io.File;
 import java.math.BigDecimal;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.NoSuchElementException;
 
 import javax.xml.datatype.DatatypeFactory;
 import javax.xml.datatype.XMLGregorianCalendar;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.emf.cdo.CDOObject;
 import org.eclipse.emf.cdo.CDOObjectReference;
 import org.eclipse.emf.cdo.common.branch.CDOBranchVersion;
 import org.eclipse.emf.cdo.common.id.CDOID;
 import org.eclipse.emf.cdo.common.revision.CDORevision;
 import org.eclipse.emf.cdo.common.revision.delta.CDOAddFeatureDelta;
 import org.eclipse.emf.cdo.common.revision.delta.CDOContainerFeatureDelta;
 import org.eclipse.emf.cdo.common.revision.delta.CDOFeatureDelta;
 import org.eclipse.emf.cdo.common.revision.delta.CDOListFeatureDelta;
 import org.eclipse.emf.cdo.common.revision.delta.CDOMoveFeatureDelta;
 import org.eclipse.emf.cdo.common.revision.delta.CDORemoveFeatureDelta;
 import org.eclipse.emf.cdo.common.revision.delta.CDORevisionDelta;
 import org.eclipse.emf.cdo.common.revision.delta.CDOSetFeatureDelta;
 import org.eclipse.emf.cdo.eresource.CDOResource;
 import org.eclipse.emf.cdo.spi.common.id.AbstractCDOIDLong;
 import org.eclipse.emf.cdo.spi.common.revision.InternalCDORevision;
 import org.eclipse.emf.cdo.util.CDOUtil;
 import org.eclipse.emf.cdo.view.CDOView;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.TreeIterator;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 
 import com.google.common.base.Function;
 import com.google.common.base.Predicate;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Iterators;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Multimap;
 import com.google.common.collect.Multimaps;
 import com.google.common.collect.Ordering;
 import com.google.inject.Inject;
 import com.netxforge.netxstudio.ServerSettings;
 import com.netxforge.netxstudio.common.internal.CommonActivator;
 import com.netxforge.netxstudio.generics.DateTimeRange;
 import com.netxforge.netxstudio.generics.GenericsFactory;
 import com.netxforge.netxstudio.generics.GenericsPackage;
 import com.netxforge.netxstudio.generics.Lifecycle;
 import com.netxforge.netxstudio.generics.Person;
 import com.netxforge.netxstudio.generics.Role;
 import com.netxforge.netxstudio.generics.Value;
 import com.netxforge.netxstudio.library.Component;
 import com.netxforge.netxstudio.library.Equipment;
 import com.netxforge.netxstudio.library.LevelKind;
 import com.netxforge.netxstudio.library.LibraryPackage;
 import com.netxforge.netxstudio.library.NetXResource;
 import com.netxforge.netxstudio.library.NodeType;
 import com.netxforge.netxstudio.metrics.DataKind;
 import com.netxforge.netxstudio.metrics.IdentifierDataKind;
 import com.netxforge.netxstudio.metrics.KindHintType;
 import com.netxforge.netxstudio.metrics.Mapping;
 import com.netxforge.netxstudio.metrics.MappingColumn;
 import com.netxforge.netxstudio.metrics.Metric;
 import com.netxforge.netxstudio.metrics.MetricRetentionPeriod;
 import com.netxforge.netxstudio.metrics.MetricRetentionRule;
 import com.netxforge.netxstudio.metrics.MetricSource;
 import com.netxforge.netxstudio.metrics.MetricValueRange;
 import com.netxforge.netxstudio.metrics.MetricsFactory;
 import com.netxforge.netxstudio.metrics.MetricsPackage;
 import com.netxforge.netxstudio.metrics.ValueDataKind;
 import com.netxforge.netxstudio.operators.Marker;
 import com.netxforge.netxstudio.operators.Network;
 import com.netxforge.netxstudio.operators.Node;
 import com.netxforge.netxstudio.operators.Operator;
 import com.netxforge.netxstudio.operators.OperatorsPackage;
 import com.netxforge.netxstudio.operators.Relationship;
 import com.netxforge.netxstudio.operators.ResourceMonitor;
 import com.netxforge.netxstudio.operators.ToleranceMarker;
 import com.netxforge.netxstudio.operators.ToleranceMarkerDirectionKind;
 import com.netxforge.netxstudio.scheduling.Job;
 import com.netxforge.netxstudio.scheduling.SchedulingPackage;
 import com.netxforge.netxstudio.services.DerivedResource;
 import com.netxforge.netxstudio.services.RFSService;
 import com.netxforge.netxstudio.services.Service;
 import com.netxforge.netxstudio.services.ServiceDistribution;
 import com.netxforge.netxstudio.services.ServiceMonitor;
 import com.netxforge.netxstudio.services.ServiceUser;
 import com.netxforge.netxstudio.services.ServicesPackage;
 
 public class ModelUtils {
 
 	public static final String DATE_PATTERN_1 = "MM/dd/yyyy";
 	public static final String DATE_PATTERN_2 = "dd-MM-yyyy";
 	public static final String DATE_PATTERN_3 = "dd.MM.yyyy";
 
 	public static final String TIME_PATTERN_1 = "HH:mm:ss"; // 24 hour.
 	public static final String TIME_PATTERN_2 = "HH:mm"; // 24 hour
 	public static final String TIME_PATTERN_3 = "hh:mm:ss"; // AM PM
 	public static final String TIME_PATTERN_4 = "hh:mm"; // AM PM
 
 	public static final String TIME_PATTERN_5 = "a"; // AM PM marker.
 	public static final String DEFAULT_DATE_TIME_PATTERN = "MM/dd/yyyy HH:mm:ss";
 
 	public static final int SECONDS_IN_A_MINUTE = 60;
 	public static final int SECONDS_IN_15MIN = SECONDS_IN_A_MINUTE * 15;
 	public static final int SECONDS_IN_AN_HOUR = SECONDS_IN_A_MINUTE * 60;
 	public static final int SECONDS_IN_A_DAY = SECONDS_IN_AN_HOUR * 24;
 	public static final int SECONDS_IN_A_WEEK = SECONDS_IN_A_DAY * 7;
 
 	/**
 	 * Lifecycle state Planned.
 	 */
 	public static final int LIFECYCLE_PROPOSED = 4;
 	public static final int LIFECYCLE_PLANNED = 3;
 	public static final int LIFECYCLE_CONSTRUCTED = 2;
 	public static final int LIFECYCLE_INSERVICE = 1;
 	public static final int LIFECYCLE_OUTOFSERVICE = 0;
 	public static final int LIFECYCLE_NOTSET = -1;
 
 	/**
 	 * this is seconds in 4 weeks. Should be use only as an interval rule.
 	 */
 	public static final int SECONDS_IN_A_MONTH = SECONDS_IN_A_DAY * 30;
 
 	public static final int MINUTES_IN_AN_HOUR = 60;
 	public static final int MINUTES_IN_A_DAY = 60 * 24;
 	public static final int MINUTES_IN_A_WEEK = MINUTES_IN_A_DAY * 4;
 
 	// Note! For months, we better use a calendar function.
 	public static final int MINUTES_IN_A_MONTH = MINUTES_IN_A_DAY * 30;
 
 	public static final String EXTENSION_DONE = ".done";
 	public static final String EXTENSION_DONE_WITH_FAILURES = ".done_with_failures";
 
 	// Required to translate.
 	public static final String NETWORK_ELEMENT_ID = "Network Element ID";
 	public static final String NETWORK_ELEMENT = "Network Element";
 	public static final String NODE_ID = "NodeID";
 	public static final String NODE = "NODE";
 
 	private static final int MAX_CHANGE_LENGTH = 2000;
 
 	public static final Iterable<String> MAPPING_NODE_ATTRIBUTES = ImmutableList
 			.of(NETWORK_ELEMENT_ID);
 	public static final Iterable<String> MAPPING_REL_ATTRIBUTES = ImmutableList
 			.of("Name", "Protocol");
 	public static final Iterable<String> MAPPING_FUNCTION_ATTRIBUTES = ImmutableList
 			.of("Name");
 	public static final Iterable<String> MAPPING_EQUIPMENT_ATTRIBUTES = ImmutableList
 			.of("Name", "EquipmentCode", "Position");
 
 	/**
 	 * Compare two dates.
 	 */
 	public class ValueTimeStampComparator implements Comparator<Value> {
 		public int compare(final Value v1, final Value v2) {
 
 			// check if set.
 			if (v1 != null
 					&& v1.eIsSet(GenericsPackage.Literals.VALUE__TIME_STAMP)
 					&& v2 != null
 					&& v2.eIsSet(GenericsPackage.Literals.VALUE__TIME_STAMP)) {
 				return v1.getTimeStamp().compare(v2.getTimeStamp());
 			}
 			return -1;
 		}
 	};
 
 	public ValueTimeStampComparator valueTimeStampCompare() {
 		return new ValueTimeStampComparator();
 	}
 
 	/**
 	 * Compare two values
 	 */
 	public class ValueValueComparator implements Comparator<Value> {
 		public int compare(final Value v1, final Value v2) {
 			// check if set.
 			if (v1.eIsSet(GenericsPackage.Literals.VALUE__VALUE)
 					&& v2.eIsSet(GenericsPackage.Literals.VALUE__VALUE)) {
 				return Double.compare(v1.getValue(), v2.getValue());
 			}
 			return 0;
 		}
 	};
 
 	public ValueValueComparator valueValueCompare() {
 		return new ValueValueComparator();
 	}
 
 	public String value(Value v) {
 		StringBuffer sb = new StringBuffer();
 		if (v == null)
 			return "";
 		sb.append("v=" + v.getValue() + ", ");
 		sb.append("ts=" + dateAndTime(v.getTimeStamp()));
 		return sb.toString();
 	}
 
 	/**
 	 * Compare two values
 	 */
 	public class DateComparator implements Comparator<Date> {
 		public int compare(final Date v1, final Date v2) {
 			return v1.compareTo(v2);
 		}
 	};
 
 	public DateComparator dateComparator() {
 		return new DateComparator();
 	}
 
 	/**
 	 * Compare two values
 	 */
 	public class DoubleComparator implements Comparator<Double> {
 		public int compare(final Double v1, final Double v2) {
 			return Double.compare(v1, v2);
 		}
 	};
 
 	public DoubleComparator doubleCompare() {
 		return new DoubleComparator();
 	}
 
 	/**
 	 * Compare two value ranges, on the interval in minutes.
 	 */
 	public class MvrComparator implements Comparator<MetricValueRange> {
 		public int compare(final MetricValueRange mvr1,
 				final MetricValueRange mvr2) {
 			return new Integer(mvr1.getIntervalHint()).compareTo(mvr2
 					.getIntervalHint());
 		}
 	};
 
 	public MvrComparator mvrCompare() {
 		return new MvrComparator();
 	}
 
 	/**
 	 * Compare two markers on the time stamps.
 	 */
 	public class MarkerTimeStampComparator implements Comparator<Marker> {
 		public int compare(final Marker m1, final Marker m2) {
 
 			CDOUtil.cleanStaleReference(m1,
 					OperatorsPackage.Literals.MARKER__VALUE_REF);
 			CDOUtil.cleanStaleReference(m2,
 					OperatorsPackage.Literals.MARKER__VALUE_REF);
 
 			return new ValueTimeStampComparator().compare(m1.getValueRef(),
 					m2.getValueRef());
 		}
 	};
 
 	public MarkerTimeStampComparator markerTimeStampCompare() {
 		return new MarkerTimeStampComparator();
 	}
 
 	/**
 	 * Simply compare the begin of the period, we do not check for potential
 	 * overlap with the end of the period.
 	 */
 	public class ServiceMonitorComparator implements Comparator<ServiceMonitor> {
 		public int compare(final ServiceMonitor sm1, ServiceMonitor sm2) {
 			return sm1.getPeriod().getBegin()
 					.compare(sm2.getPeriod().getBegin());
 		}
 	};
 
 	public ServerSettings serverSettings(Resource serverSettingsResource) {
 
 		if (serverSettingsResource != null
 				&& serverSettingsResource.getContents().size() == 1) {
 			ServerSettings settings = (ServerSettings) serverSettingsResource
 					.getContents().get(0);
 			return settings;
 		}
 
 		return null;
 
 	}
 
 	public FileLastModifiedComparator fileLastModifiedComparator() {
 		return new FileLastModifiedComparator();
 	}
 
 	public class FileLastModifiedComparator implements Comparator<File> {
 		public int compare(final File f1, File f2) {
 			return new Long(f2.lastModified()).compareTo(f1.lastModified());
 		}
 	};
 
 	public ServiceMonitorComparator serviceMonitorCompare() {
 		return new ServiceMonitorComparator();
 	}
 
 	public class NodeTypeIsLeafComparator implements Comparator<NodeType> {
 		public int compare(final NodeType nt1, NodeType nt2) {
 			if (nt1.isLeafNode() && nt2.isLeafNode()) {
 				return 0;
 			}
 			if (nt1.isLeafNode() && !nt2.isLeafNode()) {
 				return 1;
 			}
 			if (!nt1.isLeafNode() && nt2.isLeafNode()) {
 				return -1;
 			}
 			return 0;
 		}
 	};
 
 	public NodeTypeIsLeafComparator nodeTypeIsLeafComparator() {
 		return new NodeTypeIsLeafComparator();
 	}
 
 	/**
 	 * Explicitly evaluates to the timestamp being within the period. If the
 	 * timestamp is equal to the period, it will not be included.
 	 * 
 	 * @author Christophe
 	 */
 	public class ValueInsideRange implements Predicate<Value> {
 
 		private long from;
 		private long to;
 
 		public ValueInsideRange(final DateTimeRange dtr) {
 			from = dtr.getBegin().toGregorianCalendar().getTimeInMillis();
 			to = dtr.getEnd().toGregorianCalendar().getTimeInMillis();
 		}
 
 		public ValueInsideRange(Date from, Date to) {
 			this.from = from.getTime();
 			this.to = to.getTime();
 		}
 
 		public ValueInsideRange(long from, long to) {
 			this.from = from;
 			this.to = to;
 		}
 
 		public boolean apply(final Value v) {
 
 			long target = v.getTimeStamp().toGregorianCalendar()
 					.getTimeInMillis();
 			return from <= target && to >= target;
 		}
 	}
 
 	public ValueInsideRange valueInsideRange(DateTimeRange dtr) {
 		return new ValueInsideRange(dtr);
 	}
 
 	public ValueInsideRange valueInsideRange(Date from, Date to) {
 		return new ValueInsideRange(from, to);
 	}
 
 	public ValueInsideRange valueInsideRange(long from, long to) {
 		return new ValueInsideRange(from, to);
 	}
 
 	public List<Value> valuesInsideRange(Iterable<Value> unfiltered,
 			DateTimeRange dtr) {
 
 		Iterable<Value> filterValues = Iterables.filter(unfiltered,
 				valueInsideRange(dtr));
 		return Lists.newArrayList(filterValues);
 	}
 
 	public List<Value> valuesInsideRange(Iterable<Value> unfiltered, Date from,
 			Date to) {
 
 		Iterable<Value> filterValues = Iterables.filter(unfiltered,
 				valueInsideRange(from, to));
 		return Lists.newArrayList(filterValues);
 	}
 
 	public List<Value> valuesInsideRange(Iterable<Value> unfiltered, long from,
 			long to) {
 
 		Iterable<Value> filterValues = Iterables.filter(unfiltered,
 				valueInsideRange(from, to));
 		return Lists.newArrayList(filterValues);
 	}
 
 	public class ValueForValues implements Predicate<Value> {
 
 		List<Value> referenceValues;
 
 		public ValueForValues(final List<Value> referenceValues) {
 			this.referenceValues = referenceValues;
 		}
 
 		public boolean apply(final Value unfilteredValue) {
 
 			Predicate<Value> predicate = new Predicate<Value>() {
 				public boolean apply(final Value v) {
 					return valueTimeStampCompare().compare(unfilteredValue, v) == 0;
 				}
 			};
 			try {
 				Iterables.find(referenceValues, predicate);
 				return true;
 			} catch (NoSuchElementException nsee) {
 				return false;
 			}
 		}
 	}
 
 	public ValueForValues valueForValues(List<Value> referenceValues) {
 		return new ValueForValues(referenceValues);
 	}
 
 	public List<Value> valuesForValues(Iterable<Value> unfiltered,
 			List<Value> referenceValues) {
 		Iterable<Value> filterValues = Iterables.filter(unfiltered,
 				valueForValues(referenceValues));
 		return Lists.newArrayList(filterValues);
 	}
 
 	/**
 	 * Gets a range from a bunch of values. The values are sorted first. The
 	 * values should not be empty.
 	 * 
 	 * @param values
 	 * @return
 	 */
 	public DateTimeRange range(List<Value> values) {
 
 		if (values.isEmpty()) {
 			return null;
 
 		}
 		List<Value> sortValuesByTimeStamp = sortValuesByTimeStamp(values);
 		DateTimeRange createDateTimeRange = GenericsFactory.eINSTANCE
 				.createDateTimeRange();
 		createDateTimeRange.setBegin(sortValuesByTimeStamp.get(0)
 				.getTimeStamp());
 		createDateTimeRange.setEnd(sortValuesByTimeStamp.get(
 				sortValuesByTimeStamp.size() - 1).getTimeStamp());
 
 		return createDateTimeRange;
 	}
 
 	public List<List<Value>> splitValueRange(List<Value> values, int srcInterval) {
 		return this.splitValueRange(values, srcInterval, -1);
 	}
 
 	/**
 	 * Split the value range, in subranges for the provided interval boundaries.
 	 * So a Day interval will split the value range containing hourly values in
 	 * sub ranges containing a maximum quantity of values which is lesser or
 	 * equal of a day. (23)
 	 * 
 	 * @param values
 	 * @param srcInterval
 	 *            in minutes.
 	 * @return
 	 */
 	public List<List<Value>> splitValueRange(List<Value> values,
 			int srcInterval, int targetInterval) {
 
 		List<List<Value>> valueMatrix = Lists.newArrayList();
 
 		int field = fieldForInterval(srcInterval, targetInterval);
 
 		if (field == -1) {
 			// can't split, bale out.
 			valueMatrix.add(values);
 			return valueMatrix;
 		}
 
 		List<Value> nextSequence = Lists.newArrayList();
 		// we expect at least one value, so we can safely add the first
 		// sequence.
 		if (!values.isEmpty()) {
 			valueMatrix.add(nextSequence);
 		}
 		Iterator<Value> iterator = values.iterator();
 
 		GregorianCalendar cal = null;
 		int actualMaximum = -1;
 		int actualMinimum = -1;
 		int lastVal = -1;
 		while (iterator.hasNext()) {
 			Value v = iterator.next();
 			cal = v.getTimeStamp().toGregorianCalendar();
 			if (actualMaximum == -1) {
 				actualMaximum = cal.getActualMaximum(field);
 				actualMinimum = cal.getActualMinimum(field);
 			}
 			int currentVal = cal.get(field);
 
 			// Get the relevant field for this timestamp. the value for the
 			// corresponding field
 			// is pushed until the max value.
 			if (currentVal == actualMinimum && lastVal == actualMaximum) {
 				nextSequence = Lists.newArrayList();
 				valueMatrix.add(nextSequence);
 			}
 			// else {
 			// // it should nog get here.
 			// throw new IllegalStateException(
 			// "interval out of bounds, for value=" + this.value(v));
 			// }
 			nextSequence.add(v);
 			lastVal = currentVal;
 		}
 
 		return valueMatrix;
 	}
 
 	/**
 	 * The IntervalHint is required if to compare the difference is less than
 	 * the interval.
 	 * 
 	 * @param intervalHint
 	 *            in Minutes
 	 * @param time1
 	 * @param time2
 	 * @return
 	 * @deprecated DO NOT USE, WORK IN PROGRESS.
 	 */
 	public boolean isSameTime(int intervalHint, Date d1, Date d2) {
 
 		Calendar instance = Calendar.getInstance();
 		instance.setTime(d1);
 
 		// Get the next timestamp for this interval,
 
 		@SuppressWarnings("unused")
 		int fieldForInterval = this.fieldForInterval(intervalHint, -1);
 
 		return false;
 	}
 
 	/**
 	 * Return a Calendar field value which corresponds to the source interval
 	 * provided in minutes. The target Interval is used for some source interval
 	 * only. (Like Day, could be day of the week or day of the month), if the
 	 * target Interval is not specified (-1), day of the month is the default.
 	 * 
 	 * @param srcInterval
 	 * @param targetInterval
 	 * @return
 	 */
 	public int fieldForInterval(int srcInterval, int targetInterval) {
 
 		switch (srcInterval) {
 		case 15: {
 			switch (targetInterval) {
 			case MINUTES_IN_A_DAY:
 			case -1:
 				return Calendar.HOUR_OF_DAY;
 			case MINUTES_IN_AN_HOUR: {
 				// we can't split using a field.
 				// return -1;
 			}
 			}
 		}
 		case MINUTES_IN_AN_HOUR: // one hour interval.
 			return Calendar.HOUR_OF_DAY;
 		case MINUTES_IN_A_DAY: { // one day interval
 			switch (targetInterval) {
 			case MINUTES_IN_A_MONTH:
 			case -1:
 				return Calendar.DAY_OF_MONTH;
 			case MINUTES_IN_A_WEEK:
 				return Calendar.DAY_OF_WEEK;
 			}
 		}
 		case MINUTES_IN_A_WEEK:
 			return Calendar.WEEK_OF_YEAR;
 		case MINUTES_IN_A_MONTH: {
 			return Calendar.MONTH;
 		}
 		default:
 			return -1;
 		}
 	}
 
 	public class NonHiddenFile implements Predicate<File> {
 		public boolean apply(final File f) {
 			return !f.isHidden();
 		}
 	}
 
 	public NonHiddenFile nonHiddenFile() {
 		return new NonHiddenFile();
 	}
 
 	/**
 	 * A Predicate which can filter files based on one or more file extensions
 	 * including the '.' separator, when the negate paramter is provided the
 	 * reverse predicate is applied.
 	 * 
 	 * @author Christophe
 	 * 
 	 */
 	public class ExtensionFile implements Predicate<File> {
 
 		private String[] extensions;
 		private boolean negate = false;
 
 		public ExtensionFile(String... extensions) {
 			this.extensions = extensions;
 		}
 
 		public ExtensionFile(boolean negate, String... extensions) {
 			this.extensions = extensions;
 			this.negate = negate;
 		}
 
 		public boolean apply(final File f) {
 			String fileName = f.getName();
 			if (f.isDirectory())
 				return false;
 
 			int dotIndex = fileName.lastIndexOf('.');
 
 			if (dotIndex == -1) {
 				return false;
 			}
 			String extension = fileName.substring(dotIndex, fileName.length());
 
 			for (String ext : this.extensions) {
 				if (ext.equals(extension)) {
 					return negate ? true : !true;
 				}
 			}
 			return false;
 		}
 	}
 
 	public ExtensionFile extensionFile(String... extension) {
 		return new ExtensionFile(extension);
 	}
 
 	public ExtensionFile extensionFile(boolean negate, String... extensions) {
 		return new ExtensionFile(negate, extensions);
 	}
 
 	public class NodeOfType implements Predicate<Node> {
 		private final NodeType nt;
 
 		public NodeOfType(final NodeType nt) {
 			this.nt = nt;
 		}
 
 		public boolean apply(final Node node) {
 			if (node.eIsSet(OperatorsPackage.Literals.NODE__NODE_TYPE)) {
 				if (node.getNodeType().eIsSet(
 						LibraryPackage.Literals.NODE_TYPE__NAME)) {
 					return node.getNodeType().getName().equals(nt.getName());
 				}
 			}
 			return false;
 		}
 	}
 
 	public NodeOfType nodeOfType(NodeType nodeType) {
 		return new NodeOfType(nodeType);
 	}
 
 	public class NodeInRelationship implements Predicate<Node> {
 		private final Relationship r;
 
 		public NodeInRelationship(final Relationship r) {
 			this.r = r;
 		}
 
 		public boolean apply(final Node n) {
 			return r.getNodeID1Ref() == n;
 		}
 	}
 
 	public class SourceRelationshipForNode implements Predicate<Relationship> {
 		private final Node n;
 
 		public SourceRelationshipForNode(final Node n) {
 			this.n = n;
 		}
 
 		public boolean apply(final Relationship r) {
 			return r.getNodeID1Ref() == n;
 		}
 	}
 
 	public NodeInRelationship nodeInRelationship(Relationship r) {
 		return new NodeInRelationship(r);
 	}
 
 	public SourceRelationshipForNode sourceRelationshipInNode(Node n) {
 		return new SourceRelationshipForNode(n);
 	}
 
 	/*
 	 * Note will not provide the ServiceMonitors for which the period is partly
 	 * in range. targetBegin <= begin && targetEnd <= end. (Example of an
 	 * overlapping).
 	 */
 	public class ServiceMonitorInsideRange implements Predicate<ServiceMonitor> {
 		private final DateTimeRange dtr;
 
 		public ServiceMonitorInsideRange(final DateTimeRange dtr) {
 			this.dtr = dtr;
 		}
 
 		public boolean apply(final ServiceMonitor s) {
 
 			long begin = dtr.getBegin().toGregorianCalendar().getTimeInMillis();
 			long end = dtr.getEnd().toGregorianCalendar().getTimeInMillis();
 
 			long targetBegin = s.getPeriod().getBegin().toGregorianCalendar()
 					.getTimeInMillis();
 			long targetEnd = s.getPeriod().getEnd().toGregorianCalendar()
 					.getTimeInMillis();
 
 			return targetBegin >= begin && targetEnd <= end;
 
 		}
 	}
 
 	public ServiceMonitorInsideRange serviceMonitorinsideRange(DateTimeRange dtr) {
 		return new ServiceMonitorInsideRange(dtr);
 	}
 
 	public class IsRelationship implements Predicate<EObject> {
 		public boolean apply(final EObject eo) {
 			return eo instanceof Relationship;
 		}
 	}
 
 	public IsRelationship isRelationship() {
 		return new IsRelationship();
 	}
 
 	public class MarkerForValue implements Predicate<Marker> {
 		private final Value value;
 
 		public MarkerForValue(final Value value) {
 			this.value = value;
 		}
 
 		public boolean apply(final Marker m) {
 			Value mValue = m.getValueRef();
 			if (mValue != null) {
 				return EcoreUtil.equals(mValue, value);
 			}
 			return false;
 		}
 	}
 
 	public MarkerForValue markerForValue(Value v) {
 		return new MarkerForValue(v);
 	}
 
 	public class MarkerForDate implements Predicate<Marker> {
 		private final Date checkDate;
 
 		public MarkerForDate(final Date value) {
 			this.checkDate = value;
 		}
 
 		public boolean apply(final Marker m) {
 			if (m.eIsSet(OperatorsPackage.Literals.MARKER__VALUE_REF)) {
 				Value markerValue = m.getValueRef();
 				if (markerValue
 						.eIsSet(GenericsPackage.Literals.VALUE__TIME_STAMP)) {
 					Date markerDate = fromXMLDate(markerValue.getTimeStamp());
 					return markerDate.equals(checkDate);
 				}
 			}
 			return false;
 		}
 	}
 
 	public MarkerForDate markerForDate(Date d) {
 		return new MarkerForDate(d);
 	}
 
 	public class ToleranceMarkerPredicate implements Predicate<Marker> {
 		public boolean apply(final Marker m) {
 			return m instanceof ToleranceMarker;
 		}
 	}
 
 	public ToleranceMarkerPredicate toleranceMarkers() {
 		return new ToleranceMarkerPredicate();
 	}
 
 	/**
 	 * 
 	 * @param node
 	 * @return
 	 */
 	public boolean isInService(Node node) {
 		if (!node.eIsSet(OperatorsPackage.Literals.NODE__LIFECYCLE)) {
 			return true;
 		} else
 			return isInService(node.getLifecycle());
 	}
 
 	/**
 	 * 
 	 * @param c
 	 * @return
 	 */
 	public boolean isInService(Component c) {
 		if (!c.eIsSet(LibraryPackage.Literals.COMPONENT__LIFECYCLE)) {
 			return true;
 		} else
 			return isInService(c.getLifecycle());
 	}
 
 	/**
 	 * 
 	 * @param lc
 	 * @return
 	 */
 	public boolean isInService(Lifecycle lc) {
 		final long time = System.currentTimeMillis();
 		if (lc.getInServiceDate() != null
 				&& lc.getInServiceDate().toGregorianCalendar()
 						.getTimeInMillis() > time) {
 			return false;
 		}
 		if (lc.getOutOfServiceDate() != null
 				&& lc.getOutOfServiceDate().toGregorianCalendar()
 						.getTimeInMillis() < time) {
 			return false;
 		}
 		return true;
 	}
 
 	@Inject
 	private DatatypeFactory dataTypeFactory;
 
 	/**
 	 * Compute a resource path on the basis of an instance. Components generate
 	 * a specific path based on their location in the node/nodetype tree.
 	 * 
 	 * Note that Components with an empty name are not allowed. Also, the depth
 	 * is limited to 5 as CDO folders, so we create a resource separated by an
 	 * underscore instead of a forward slash.
 	 * 
 	 * Note: if the name changes, we won't be able to retrieve the resource.
 	 * 
 	 * @deprecated
 	 * 
 	 */
 	public String cdoCalculateResourcePath(EObject eObject) {
 		if (eObject instanceof Component) {
 
 			final Component component = (Component) eObject;
 			if (!component.eIsSet(LibraryPackage.Literals.COMPONENT__NAME)
 					|| component.getName().length() == 0) {
 				return null;
 			}
 
 			return cdoCalculateResourcePath(component.eContainer()) + "_"
 					+ component.getName();
 		} else if (eObject instanceof Node) {
 			return "/Node_/" + ((Node) eObject).getNodeID();
 		} else if (eObject instanceof NodeType) {
 			final NodeType nodeType = (NodeType) eObject;
 			if (nodeType.eContainer() instanceof Node) {
 				return cdoCalculateResourcePath(nodeType.eContainer());
 			}
 			return "/NodeType_/" + ((NodeType) eObject).getName();
 		} else {
 			return eObject.eClass().getName();
 		}
 	}
 
 	/*
 	 * Construct a path name specific to holde NetXResource objects.
 	 */
 	public String cdoCalculateResourcePathII(EObject eObject) {
 		if (eObject instanceof Component) {
 			final Component component = (Component) eObject;
 			// if (!component.eIsSet(LibraryPackage.Literals.COMPONENT__NAME)
 			// || component.getName().length() == 0) {
 			// return null;
 			// }
 			return cdoCalculateResourcePathII(component.eContainer());
 		} else if (eObject instanceof Node) {
 			Node n = (Node) eObject;
 			if (n.eIsSet(OperatorsPackage.Literals.NODE__NODE_ID)) {
 				return "/Node_/"
 						+ LibraryPackage.Literals.NET_XRESOURCE.getName() + "_"
 						+ ((Node) eObject).getNodeID();
 			} else {
 				return "/Node_/"
 						+ LibraryPackage.Literals.NET_XRESOURCE.getName();
 			}
 
 		} else if (eObject instanceof NodeType) {
 			final NodeType nodeType = (NodeType) eObject;
 			if (nodeType.eContainer() instanceof Node) {
 				return cdoCalculateResourcePathII(nodeType.eContainer());
 			} else {
 				if (nodeType.eIsSet(LibraryPackage.Literals.NODE_TYPE__NAME)) {
 					return "/NodeType_/"
 							+ LibraryPackage.Literals.NET_XRESOURCE.getName()
 							+ "_" + ((NodeType) eObject).getName();
 				} else {
 					return "/NodeType_/"
 							+ LibraryPackage.Literals.NET_XRESOURCE.getName();
 				}
 			}
 		} else {
 			return LibraryPackage.Literals.NET_XRESOURCE.getName();
 		}
 	}
 
 	/*
 	 * Construct a name specific to hold NetXResource objects.
 	 */
 	public String cdoCalculateResourceName(EObject eObject)
 			throws IllegalAccessException {
 		if (eObject instanceof Component) {
 			final Component component = (Component) eObject;
 			// }
 			return cdoCalculateResourceName(component.eContainer());
 		} else if (eObject instanceof Node) {
 			Node n = (Node) eObject;
 			if (n.eIsSet(OperatorsPackage.Literals.NODE__NODE_ID)) {
 				return LibraryPackage.Literals.NET_XRESOURCE.getName() + "_"
 						+ ((Node) eObject).getNodeID();
 			} else {
 				throw new IllegalAccessException("The node ID should be set");
 			}
 
 		} else if (eObject instanceof NodeType) {
 			final NodeType nodeType = (NodeType) eObject;
 			if (nodeType.eContainer() instanceof Node) {
 				return cdoCalculateResourceName(nodeType.eContainer());
 			} else {
 				// throw an exception, we shouldn't call this method and expect
 				// a resource, for a NodeType instad of Node.
 				throw new IllegalAccessException(
 						"The root parent should always ne a Node");
 			}
 		} else {
 			// throw an exception, we shouldn't call this method and expect an
 			// invalid EObject.
 			throw new IllegalAccessException(
 					"Invalid argument EObject, shoud be Component, or parent");
 		}
 	}
 
 	public List<com.netxforge.netxstudio.library.Function> functionsWithName(
 			List<com.netxforge.netxstudio.library.Function> functions,
 			String name) {
 		final List<com.netxforge.netxstudio.library.Function> fl = Lists
 				.newArrayList();
 		for (final com.netxforge.netxstudio.library.Function f : functions) {
 			if (f.getName().equals(name)) {
 				fl.add(f);
 			}
 			fl.addAll(this.functionsWithName(f.getFunctions(), name));
 		}
 		return fl;
 	}
 
 	/**
 	 * Return the closure of equipments matching the code and name.
 	 * 
 	 * @param equips
 	 * @param code
 	 * @return
 	 */
 	public List<Equipment> equimentsWithCodeAndName(List<Equipment> equips,
 			String code, String name) {
 		final List<Equipment> el = Lists.newArrayList();
 		for (final Equipment e : equips) {
 			if (e.getEquipmentCode().equals(code) && e.getName().equals(name)) {
 				el.add(e);
 			}
 			el.addAll(this.equimentsWithCodeAndName(e.getEquipments(), code,
 					name));
 		}
 		return el;
 	}
 
 	/**
 	 * Return the closure of equipments matching the code.
 	 * 
 	 * @param equips
 	 * @param code
 	 * @return
 	 */
 	public List<Equipment> equimentsWithCode(List<Equipment> equips, String code) {
 		final List<Equipment> el = Lists.newArrayList();
 		for (final Equipment e : equips) {
 			if (e.getEquipmentCode().equals(code)) {
 				el.add(e);
 			}
 			el.addAll(this.equimentsWithCode(e.getEquipments(), code));
 		}
 		return el;
 	}
 
 	public Collection<String> expressionLines(String Expression) {
 		final String[] splitByNewLine = Expression.split("\n");
 		final Collection<String> collection = Lists
 				.newArrayList(splitByNewLine);
 		return collection;
 	}
 
 	public List<NetXResource> resourcesFor(Node node) {
 		List<NetXResource> resources = Lists.newArrayList();
 		TreeIterator<EObject> iterator = node.eAllContents();
 		while (iterator.hasNext()) {
 			EObject eo = iterator.next();
 			if (eo instanceof NetXResource) {
 				resources.add((NetXResource) eo);
 			}
 		}
 		return resources;
 	}
 
 	public List<NetXResource> resourcesForComponent(Component component) {
 		List<NetXResource> resources = Lists.newArrayList();
 		List<Component> componentsForComponent = this
 				.componentsForComponent(component);
 		for (Component c : componentsForComponent) {
 			resources.addAll(c.getResourceRefs());
 		}
 		return resources;
 	}
 
 	public List<DerivedResource> derivedResourcesWithName(Service s,
 			String expressionName) {
 
 		final List<DerivedResource> drL = Lists.newArrayList();
 
 		for (ServiceUser su : s.getServiceUserRefs()) {
 			if (su.eIsSet(ServicesPackage.Literals.SERVICE_USER__SERVICE_PROFILE)) {
 				for (DerivedResource dr : su.getServiceProfile()
 						.getProfileResources()) {
 					if (dr.getExpressionName().equals(expressionName)) {
 						drL.add(dr);
 					}
 				}
 			}
 		}
 		return drL;
 	}
 
 	public List<NetXResource> resourcesWithExpressionName(NodeType nt,
 			String expressionName) {
 		final List<Component> cl = Lists.newArrayList();
 		cl.addAll(nt.getEquipments());
 		cl.addAll(nt.getFunctions());
 		return this.resourcesWithExpressionName(cl, expressionName, true);
 	}
 
 	public List<NetXResource> resourcesWithExpressionName(Node n,
 			String expressionName) {
 		final List<Component> cl = Lists.newArrayList();
 		cl.addAll(n.getNodeType().getEquipments());
 		cl.addAll(n.getNodeType().getFunctions());
 		return this.resourcesWithExpressionName(cl, expressionName, true);
 	}
 
 	public List<Value> sortValuesByTimeStampAndReverse(List<Value> values) {
 		List<Value> sortedCopy = Ordering.from(valueTimeStampCompare())
 				.reverse().sortedCopy(values);
 		return sortedCopy;
 
 	}
 
 	/**
 	 * Sorts a list of value in chronological order. (oldest first).
 	 * 
 	 * @param values
 	 * @return
 	 */
 	public List<Value> sortValuesByTimeStamp(List<Value> values) {
 		List<Value> sortedCopy = Ordering.from(valueTimeStampCompare())
 				.sortedCopy(values);
 		return sortedCopy;
 
 	}
 
 	public List<Marker> sortMarkersByTimeStamp(List<Marker> markers) {
 		List<Marker> sortedCopy = Ordering.from(markerTimeStampCompare())
 				.sortedCopy(markers);
 		return sortedCopy;
 	}
 
 	public List<ServiceMonitor> filterSerciceMonitorInRange(
 			List<ServiceMonitor> unfiltered, DateTimeRange dtr) {
 		Iterable<ServiceMonitor> filterValues = Iterables.filter(unfiltered,
 				this.serviceMonitorinsideRange(dtr));
 		return (Lists.newArrayList(filterValues));
 	}
 
 	/**
 	 * Return the Node or null if the target object, has a Node somewhere along
 	 * the parent hiearchy.
 	 * 
 	 * @param target
 	 * @return
 	 */
 	public Node nodeFor(EObject target) {
 		if (target instanceof Node) {
 			return (Node) target;
 		}
 		if (target != null && target.eContainer() != null) {
 			if (target.eContainer() instanceof Node) {
 				return (Node) target.eContainer();
 			} else {
 				return nodeFor(target.eContainer());
 			}
 		} else {
 			return null;
 		}
 	}
 
 	public int depthToResource(int initialDepth, EObject eObject) {
 		if (eObject.eContainer() != null) {
 			return depthToResource(++initialDepth, eObject.eContainer());
 		}
 		return initialDepth;
 	}
 
 	/**
 	 * Return the Node or null if the target object, has a NodeType somewhere
 	 * along the parent hiearchy.
 	 * 
 	 * @param target
 	 * @return
 	 */
 	public NodeType resolveParentNodeType(EObject target) {
 
 		if (target instanceof NodeType) {
 			return (NodeType) target;
 		} else if (target != null && target.eContainer() != null) {
 			if (target.eContainer() instanceof NodeType) {
 				return (NodeType) target.eContainer();
 			} else {
 				return resolveParentNodeType(target.eContainer());
 			}
 		} else {
 			return null;
 		}
 	}
 
 	public Service resolveRootService(EObject target) {
 		if (target instanceof Service) {
 			if (target.eContainer() instanceof Service) {
 				return resolveRootService(target.eContainer());
 			} else {
 				return (Service) target;
 			}
 		}
 		return null;
 	}
 
 	public ServiceMonitor lastServiceMonitor(Service service) {
 		if (service.getServiceMonitors().isEmpty()) {
 			return null;
 		}
 		int size = service.getServiceMonitors().size();
 		ServiceMonitor sm = service.getServiceMonitors().get(size - 1);
 		return sm;
 	}
 
 	/**
 	 * Business Rule:
 	 * 
 	 * A Lifecycle is valid when the Lifecycle sequence are in chronological
 	 * order.
 	 * 
 	 * @param lf
 	 * @return
 	 */
 	public boolean lifeCycleValid(Lifecycle lf) {
 
 		long proposed = lf.getProposed().toGregorianCalendar()
 				.getTimeInMillis();
 		long planned = lf.getPlannedDate().toGregorianCalendar()
 				.getTimeInMillis();
 		long construction = lf.getConstructionDate().toGregorianCalendar()
 				.getTimeInMillis();
 		long inService = lf.getInServiceDate().toGregorianCalendar()
 				.getTimeInMillis();
 		long outOfService = lf.getOutOfServiceDate().toGregorianCalendar()
 				.getTimeInMillis();
 
 		boolean proposed_planned = lf
 				.eIsSet(GenericsPackage.Literals.LIFECYCLE__PLANNED_DATE) ? proposed <= planned
 				: true;
 		boolean planned_construction = lf
 				.eIsSet(GenericsPackage.Literals.LIFECYCLE__CONSTRUCTION_DATE) ? planned <= construction
 				: true;
 		boolean construcion_inService = lf
 				.eIsSet(GenericsPackage.Literals.LIFECYCLE__IN_SERVICE_DATE) ? construction <= inService
 				: true;
 		boolean inService_outOfService = lf
 				.eIsSet(GenericsPackage.Literals.LIFECYCLE__OUT_OF_SERVICE_DATE) ? inService <= outOfService
 				: true;
 
 		return proposed_planned && planned_construction
 				&& construcion_inService && inService_outOfService;
 	}
 
 	/**
 	 * Get a String representation of the Lifeycycle State.
 	 * 
 	 * @param state
 	 * @return
 	 */
 	public String lifecycleText(Lifecycle lc) {
 		return lifecycleText(lifecycleState(lc));
 	}
 
 	/**
 	 * Get a String representation of the Lifeycycle State.
 	 * 
 	 * @param state
 	 * @return
 	 */
 	public String lifecycleText(int state) {
 
 		switch (state) {
 		case LIFECYCLE_PROPOSED:
 			return "Proposed";
 		case LIFECYCLE_CONSTRUCTED:
 			return "Constructed";
 		case LIFECYCLE_PLANNED:
 			return "Planned";
 		case LIFECYCLE_INSERVICE:
 			return "In Service";
 		case LIFECYCLE_OUTOFSERVICE:
 			return "Out of Service";
 		case LIFECYCLE_NOTSET:
 		default:
 			return "NotSet";
 		}
 
 	}
 
 	/**
 	 * Get the lifecycle state. Each date of a Lifecycle is compared with it's
 	 * predecessor. From this the state is determined.
 	 * 
 	 * @param lc
 	 * @return
 	 */
 	public int lifecycleState(Lifecycle lc) {
		
		if(lc == null){
			return LIFECYCLE_NOTSET;
		}
 		EAttribute[] states = new EAttribute[]{
 				
 				GenericsPackage.Literals.LIFECYCLE__OUT_OF_SERVICE_DATE,
 				GenericsPackage.Literals.LIFECYCLE__IN_SERVICE_DATE,
 				GenericsPackage.Literals.LIFECYCLE__CONSTRUCTION_DATE, 
 				GenericsPackage.Literals.LIFECYCLE__PLANNED_DATE, 
 				GenericsPackage.Literals.LIFECYCLE__PROPOSED
 		};
 		
 		long latestDate = -1;
 		int latestIndex = -1;
 		for( int i = 0; i < states.length ; i++){
 			EAttribute state = states[i];
 			if(lc.eIsSet(state)){
 				long currentDate = ((XMLGregorianCalendar)lc.eGet(state)).toGregorianCalendar().getTimeInMillis();
 				if(latestDate != -1) {
 					if ( latestDate >= currentDate ){
 						break;
 					}else{
 						latestDate = currentDate;
 						latestIndex = i;
 						if( CommonActivator.DEBUG){
 							CommonActivator.TRACE.trace(CommonActivator.TRACE_UTILS_OPTION, "-- update index to: " + i);
 						}
 						// set the index, we are later then predecessor. 
 
 					}
 				}else {
 					latestDate = currentDate;
 					latestIndex = i;
 				}
 			}
 		}
 		return latestIndex;
 	}
 
 	// public class HasNodeType implements Predicate<NodeType> {
 	//
 	// private List<NodeType> baseList;
 	//
 	// public HasNodeType(List<NodeType> baseList) {
 	// this.baseList = baseList;
 	// }
 	// public boolean apply(final NodeType toApply) {
 	// for(NodeType nt : baseList){
 	// if(nt.getName().equals(toApply)){
 	// return true;
 	// }
 	// }
 	// return false;
 	// }
 	// }
 	//
 	// public Predicate<NodeType> hasNodeType(List<NodeType> nodeTypes){
 	// return new HasNodeType(nodeTypes);
 	// }
 
 	public List<NodeType> uniqueNodeTypes(List<NodeType> unfiltered) {
 		List<NodeType> uniques = Lists.newArrayList();
 		for (NodeType nt : unfiltered) {
 			ImmutableList<NodeType> uniquesCopy = ImmutableList.copyOf(uniques);
 			boolean found = false;
 			for (NodeType u : uniquesCopy) {
 				if (nt.eIsSet(LibraryPackage.Literals.NODE_TYPE__NAME)
 						&& u.eIsSet(LibraryPackage.Literals.NODE_TYPE__NAME)) {
 					if (u.getName().equals(nt.getName())) {
 						found = true;
 					}
 				} else {
 					continue;
 				}
 			}
 			if (!found) {
 				uniques.add(nt);
 			}
 		}
 		return uniques;
 	}
 
 	/**
 	 * Replaces all white spaces with an underscore
 	 * 
 	 * @param inString
 	 * @return
 	 */
 	public String underscopeWhiteSpaces(String inString) {
 		return inString.replaceAll("\\s", "_");
 	}
 
 	public List<Node> nodesForNodeType(List<Node> nodes, NodeType targetNodeType) {
 		Iterable<Node> filtered = Iterables.filter(nodes,
 				this.nodeOfType(targetNodeType));
 		return Lists.newArrayList(filtered);
 	}
 
 	public List<Node> nodesForNodeType(RFSService service,
 			NodeType targetNodeType) {
 		Iterable<Node> filtered = Iterables.filter(service.getNodes(),
 				this.nodeOfType(targetNodeType));
 		return Lists.newArrayList(filtered);
 	}
 
 	public RFSServiceSummary serviceSummaryForService(Service service,
 			DateTimeRange dtr, IProgressMonitor monitor) {
 		RFSServiceSummary serviceSummary = new RFSServiceSummary(
 				(RFSService) service);
 
 		if (service instanceof RFSService) {
 			int[] ragTotalResources = new int[] { 0, 0, 0 };
 			int[] ragTotalNodes = new int[] { 0, 0, 0 };
 			for (Node n : ((RFSService) service).getNodes()) {
 				if (monitor != null && monitor.isCanceled()) {
 					return serviceSummary;
 				}
 				int[] ragResources = ragCountResourcesForNode(service, n, dtr,
 						monitor);
 				for (int i = 0; i < ragTotalResources.length; i++) {
 					ragTotalResources[i] += ragResources[i];
 				}
 				// Any of the levels > 0, we increase the total node count.
 				ragTotalNodes[0] += ragResources[0] > 0 ? 1 : 0;
 				ragTotalNodes[1] += ragResources[1] > 0 ? 1 : 0;
 				ragTotalNodes[2] += ragResources[2] > 0 ? 1 : 0;
 			}
 			serviceSummary.setRagCountResources(ragTotalResources);
 			serviceSummary.setRagCountNodes(ragTotalNodes);
 		}
 		serviceSummary.setPeriodFormattedString(formatPeriod(dtr));
 		return serviceSummary;
 	}
 
 	/**
 	 * Overall RAG Status.
 	 * 
 	 * @param sm
 	 * @return
 	 * @deprecated DO NOT USE, no distinction per NetXResource.
 	 */
 	public int[] ragCountResources(ServiceMonitor sm) {
 
 		int red = 0, amber = 0, green = 0;
 
 		for (ResourceMonitor rm : sm.getResourceMonitors()) {
 			int[] rag = ragForMarkers(rm.getMarkers());
 			red += rag[0];
 			amber += rag[1];
 			green += rag[2];
 		}
 		return new int[] { red, amber, green };
 
 	}
 
 	public boolean ragShouldReport(int[] ragStatus) {
 		if (ragStatus.length != 3) {
 			return false;
 		}
 
 		if (ragStatus[0] > 0) {
 			return true;
 		}
 
 		if (ragStatus[1] > 0) {
 			return true;
 		}
 
 		return false;
 	}
 
 	/**
 	 * Get the first marker with this value otherwise null.
 	 * 
 	 * @param markers
 	 * @param v
 	 * @return
 	 */
 	public Marker markerForValue(List<Marker> markers, Value v) {
 
 		Iterable<Marker> filtered = Iterables.filter(markers,
 				this.markerForValue(v));
 		if (Iterables.size(filtered) > 0) {
 			return filtered.iterator().next();
 		}
 		return null;
 	}
 
 	/**
 	 * returns a Map of markers for each of the NetXResources in the specified
 	 * node.
 	 * 
 	 * @param service
 	 * @param n
 	 * @param dtr
 	 * @return
 	 */
 	public Map<NetXResource, List<Marker>> toleranceMarkerMapPerResourceForServiceAndNodeAndPeriod(
 			Service service, Node n, DateTimeRange dtr, IProgressMonitor monitor) {
 
 		// Sort by begin date and reverse the Service Monitors.
 		List<ServiceMonitor> sortedCopy = Ordering
 				.from(this.serviceMonitorCompare()).reverse()
 				.sortedCopy(service.getServiceMonitors());
 
 		// Filter ServiceMonitors on the time range.
 		// List<ServiceMonitor> filtered = this.filterSerciceMonitorInRange(
 		// sortedCopy, dtr);
 
 		Map<NetXResource, List<Marker>> markersPerResource = toleranceMarkerMapPerResourceForServiceMonitorsAndNode(
 				sortedCopy, n, monitor);
 		return markersPerResource;
 
 	}
 
 	/**
 	 * Provides a total list of markers for the Service Monitor, Node and Date
 	 * Time Range. ,indiscreet of the NetXResource.
 	 * 
 	 * @param sm
 	 * @param n
 	 * @param dtr
 	 * @return
 	 */
 	public List<Marker> toleranceMarkersForServiceMonitor(ServiceMonitor sm,
 			Node n) {
 		// Process a ServiceMonitor for which the period is somehow within the
 		// range.
 		List<Marker> filtered = Lists.newArrayList();
 
 		// Each RM represents a Resource.
 		for (ResourceMonitor rm : sm.getResourceMonitors()) {
 			if (rm.getNodeRef().getNodeID().equals(n.getNodeID())) {
 				List<Marker> toleranceMarkers = toleranceMarkersForResourceMonitor(rm);
 				filtered.addAll(toleranceMarkers);
 			}
 		}
 
 		return filtered;
 	}
 
 	// public int[] ragCount(ServiceMonitor sm, Node n,
 	// DateTimeRange dtr) {
 	//
 	// int red = 0, amber = 0, green = 0;
 	// //Each RM represents a Resource.
 	// for (ResourceMonitor rm : sm.getResourceMonitors()) {
 	// if (rm.getNodeRef().getNodeID().equals(n.getNodeID())) {
 	// Marker[] markerArray = new Marker[rm.getMarkers().size()];
 	// rm.getMarkers().toArray(markerArray);
 	// List<Marker> markersForNodeList = this
 	// .toleranceMarkers(markerArray);
 	// int[] rag = this.ragForMarkers(markersForNodeList);
 	// red += rag[0];
 	// amber += rag[1];
 	// green += rag[2];
 	// }
 	//
 	// }
 	//
 	// return new int[]{red, amber, green};
 	// }
 
 	/**
 	 * A two pass rag analyzer. First go through all ResourceMonitor and bundle
 	 * the markers per NetXResource. Then count the RAG per NetXResource's
 	 * markers. Note: Resources which are not referenced by a Resource Monitor
 	 * from the specified node, are ignored.
 	 * 
 	 * @param sm
 	 * @param n
 	 * @return
 	 */
 	public int[] ragCountResourcesForNode(Service service, Node n,
 			DateTimeRange dtr, IProgressMonitor monitor) {
 
 		int red = 0, amber = 0, green = 0;
 
 		// Sort and reverse the Service Monitors.
 		List<ServiceMonitor> sortedCopy = Ordering
 				.from(this.serviceMonitorCompare()).reverse()
 				.sortedCopy(service.getServiceMonitors());
 
 		// Filter ServiceMonitors on the time range.
 		List<ServiceMonitor> filtered = this.filterSerciceMonitorInRange(
 				sortedCopy, dtr);
 
 		Map<NetXResource, List<Marker>> markersPerResource = toleranceMarkerMapPerResourceForServiceMonitorsAndNode(
 				filtered, n, monitor);
 
 		for (NetXResource res : markersPerResource.keySet()) {
 
 			if (monitor != null && monitor.isCanceled()) {
 				return new int[] { red, amber, green };
 			}
 			List<Marker> markers = markersPerResource.get(res);
 			int[] rag = ragForMarkers(markers);
 			red += rag[0];
 			amber += rag[1];
 			green += rag[2];
 
 		}
 
 		return new int[] { red, amber, green };
 	}
 
 	/**
 	 * @param serviceMonitors
 	 * @param n
 	 * @return
 	 */
 	public Map<NetXResource, List<Marker>> toleranceMarkerMapPerResourceForServiceMonitorsAndNode(
 			List<ServiceMonitor> serviceMonitors, Node n,
 			IProgressMonitor monitor) {
 		Map<NetXResource, List<Marker>> markersPerResource = Maps.newHashMap();
 
 		for (ServiceMonitor sm : serviceMonitors) {
 			for (ResourceMonitor rm : sm.getResourceMonitors()) {
 
 				// Abort the task if we are cancelled.
 				if (monitor != null && monitor.isCanceled()) {
 					return markersPerResource;
 				}
 
 				if (rm.getNodeRef().getNodeID().equals(n.getNodeID())) {
 
 					// Analyze per resource, why would a resource monitor
 					// contain markers for a nother resource?
 					List<Marker> markers;
 					NetXResource res = rm.getResourceRef();
 					if (!markersPerResource.containsKey(res)) {
 						markers = Lists.newArrayList();
 						markersPerResource.put(res, markers);
 					} else {
 						markers = markersPerResource.get(res);
 					}
 					List<Marker> toleranceMarkers = toleranceMarkersForResourceMonitor(rm);
 					markers.addAll(toleranceMarkers);
 				}
 			}
 
 		}
 		return markersPerResource;
 	}
 
 	public List<Marker> toleranceMarkersForResourceMonitor(ResourceMonitor rm) {
 		List<Marker> toleranceMarkers = Lists.newArrayList(Iterables.filter(
 				rm.getMarkers(), toleranceMarkers()));
 		return toleranceMarkers;
 	}
 
 	/**
 	 * For a collection of marker determine the rag status. Higher levels take
 	 * precedence over lower levels. Lower levels are in case preceded by a
 	 * Higher level, cleared.
 	 * 
 	 * @param markersForNodeList
 	 * @return
 	 */
 	public int[] ragForMarkers(List<Marker> markersForNodeList) {
 
 		int red = 0, amber = 0, green = 0;
 		Marker[] markerForNodeArray = new Marker[markersForNodeList.size()];
 		markersForNodeList.toArray(markerForNodeArray);
 		// Iterate markers per level.
 		for (LevelKind lk : LevelKind.VALUES) {
 
 			ToleranceMarker tm = lastToleranceMarker(lk, markerForNodeArray);
 			if (tm != null) {
 				switch (tm.getLevel().getValue()) {
 				case LevelKind.RED_VALUE: {
 					if (isStartOrUp(tm)) {
 						red++;
 					}
 				}
 					break;
 				case LevelKind.AMBER_VALUE: {
 					if (isStartOrUp(tm)) {
 						amber++;
 					}
 				}
 					break;
 				case LevelKind.GREEN_VALUE: {
 					if (isStartOrUp(tm)) {
 						green++;
 					}
 				}
 				case LevelKind.YELLOW_VALUE: {
 					// what to do with yellow??
 				}
 					break;
 				}
 			}
 			// else {
 			// green++;
 			// }
 		}
 
 		// Clear the lower levels.
 		if (red > 0) {
 			amber = 0;
 			green = 0;
 		}
 		if (amber > 0) {
 			green = 0;
 		}
 
 		return new int[] { red, amber, green };
 	}
 
 	public boolean isStartOrUp(ToleranceMarker tm) {
 		return tm.getDirection() == ToleranceMarkerDirectionKind.UP
 				|| tm.getDirection() == ToleranceMarkerDirectionKind.START;
 	}
 
 	// CB, replaced by predicate.
 	// public List<Marker> toleranceMarkers(Marker... unfiltered) {
 	// List<Marker> resultList = Lists.newArrayList();
 	// List<Marker> markerList = Lists.newArrayList(unfiltered);
 	// for (Marker m : markerList) {
 	// if (m instanceof ToleranceMarker) {
 	// ToleranceMarker tempMarker = (ToleranceMarker) m;
 	// resultList.add(tempMarker);
 	// }
 	// }
 	// return resultList;
 	// }
 
 	/**
 	 * Return the last marker which is either START or UP. The lists is sorted
 	 * and analyzed from the tail. (Newest first). Return null, if we can't find
 	 * a marker matching the Level Kind.
 	 * 
 	 * @param lk
 	 * @param markers
 	 * @return
 	 */
 	public ToleranceMarker lastToleranceMarker(LevelKind lk, Marker... markers) {
 		ToleranceMarker tm = null;
 		List<Marker> markerList = Lists.newArrayList(markers);
 		markerList = sortMarkersByTimeStamp(markerList);
 		Collections.reverse(markerList);
 		for (Marker m : markerList) {
 			if (m instanceof ToleranceMarker
 					&& ((ToleranceMarker) m).getLevel() == lk) {
 				tm = (ToleranceMarker) m;
 				break;
 			}
 		}
 		return tm;
 	}
 
 	/**
 	 * 
 	 * Get the Job of a certain type with a target value for the target feature.
 	 * 
 	 * @param jobResource
 	 * @param jobClass
 	 * @param feature
 	 * @param value
 	 * @return
 	 */
 	public Job jobForSingleObject(Resource jobResource, EClass jobClass,
 			EStructuralFeature feature, EObject value) {
 
 		// The job Class should extend the Job EClass.
 		if (!jobClass.getESuperTypes().contains(SchedulingPackage.Literals.JOB)) {
 			return null;
 		}
 
 		for (EObject eo : jobResource.getContents()) {
 			if (eo.eClass() == jobClass) {
 				if (eo.eIsSet(feature)) {
 					Object v = eo.eGet(feature);
 					if (v == value) {
 						return (Job) eo;
 					}
 				}
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * 
 	 * Get the Job of a certain type with a target collection contained in the
 	 * collection of the target feature.
 	 * 
 	 * @param jobResource
 	 * @param jobClass
 	 * @param feature
 	 * @param targetValues
 	 * @return
 	 */
 	public Job jobForMultipleObjects(Resource jobResource, EClass jobClass,
 			EStructuralFeature feature, Collection<?> targetValues) {
 
 		assert feature.isMany();
 
 		int shouldMatch = targetValues.size();
 
 		// The job Class should extend the Job EClass.
 		if (!jobClass.getESuperTypes().contains(SchedulingPackage.Literals.JOB)) {
 			return null;
 		}
 
 		for (EObject eo : jobResource.getContents()) {
 			int actuallyMatches = 0;
 			if (eo.eClass() == jobClass) {
 				if (eo.eIsSet(feature)) {
 					Object v = eo.eGet(feature);
 					if (v instanceof List<?>) {
 						for (Object listItem : (List<?>) v) {
 							// Do we contain any of our objects?
 							for (Object target : targetValues) {
 								if (listItem == target) {
 									actuallyMatches++;
 								}
 							}
 						}
 					}
 				}
 			}
 			// Check if the number of entries are actually in the target job.
 			if (actuallyMatches == shouldMatch) {
 				return (Job) eo;
 			}
 		}
 		return null;
 	}
 
 	public DateTimeRange lastMonthPeriod() {
 		DateTimeRange dtr = GenericsFactory.eINSTANCE.createDateTimeRange();
 		dtr.setBegin(this.toXMLDate(oneMonthAgo()));
 		dtr.setEnd(this.toXMLDate(todayAndNow()));
 		return dtr;
 	}
 
 	public Date begin(DateTimeRange dtr) {
 		return this.fromXMLDate(dtr.getBegin());
 	}
 
 	public Date end(DateTimeRange dtr) {
 		return this.fromXMLDate(dtr.getEnd());
 	}
 
 	public DateTimeRange period(Date start, Date end) {
 		DateTimeRange dtr = GenericsFactory.eINSTANCE.createDateTimeRange();
 		dtr.setBegin(this.toXMLDate(start));
 		dtr.setEnd(this.toXMLDate(end));
 		return dtr;
 	}
 
 	public String formatLastMonitorDate(ServiceMonitor sm) {
 		DateTimeRange dtr = sm.getPeriod();
 		return formatPeriod(dtr);
 	}
 
 	public String formatPeriod(DateTimeRange dtr) {
 		StringBuilder sb = new StringBuilder();
 		Date begin = fromXMLDate(dtr.getBegin());
 		Date end = fromXMLDate(dtr.getEnd());
 		sb.append("From: ");
 		sb.append(date(begin));
 		sb.append(" @ ");
 		sb.append(time(begin));
 		sb.append(" To: ");
 		sb.append(date(end));
 		sb.append(" @ ");
 		sb.append(time(end));
 		return sb.toString();
 	}
 
 	public List<NetXResource> resourcesInMetricSource(
 			EList<EObject> allMetrics, MetricSource ms) {
 
 		List<Metric> targetListInMetricSource = Lists.newArrayList();
 
 		// Cross reference the metrics.
 		for (EObject o : allMetrics) {
 			if (o instanceof CDOObject) {
 				CDOView cdoView = ((CDOObject) o).cdoView();
 				try {
 					List<CDOObjectReference> queryXRefs = cdoView
 							.queryXRefs(
 									(CDOObject) o,
 									new EReference[] { MetricsPackage.Literals.VALUE_DATA_KIND__METRIC_REF });
 
 					if (queryXRefs != null) {
 						for (CDOObjectReference xref : queryXRefs) {
 
 							EObject referencingValueDataKind = xref
 									.getSourceObject();
 							EObject targetMetric = xref.getTargetObject();
 							for (MappingColumn mc : ms.getMetricMapping()
 									.getDataMappingColumns()) {
 								DataKind dk = mc.getDataType();
 								// auch, that hurts....
 								if (dk instanceof ValueDataKind
 										&& (dk.cdoID() == ((CDOObject) referencingValueDataKind)
 												.cdoID())) {
 									// Yes, this is the one, add the metric.
 									targetListInMetricSource
 											.add((Metric) targetMetric);
 								}
 							}
 						}
 					}
 
 				} catch (Exception e) {
 					e.printStackTrace();
 					// The query sometimes throws exeception, if i.e an entity
 					// can't be found..
 					// EClass ExpressionResult does not have an entity name, has
 					// it been mapped to Hibernate?
 				}
 			}
 		}
 
 		return resourcesForMetrics(targetListInMetricSource);
 	}
 
 	public List<NetXResource> resourcesForMetrics(
 			List<Metric> targetListInMetricSource) {
 		List<NetXResource> targetListNetXResources = Lists.newArrayList();
 
 		// Cross reference the metrics from the target MetricSource.
 		for (EObject o : targetListInMetricSource) {
 
 			System.out.println("Look for NetXResource referencing metric: "
 					+ ((Metric) o).getName());
 
 			if (o instanceof CDOObject) {
 				CDOView cdoView = ((CDOObject) o).cdoView();
 				try {
 					List<CDOObjectReference> queryXRefs = cdoView
 							.queryXRefs(
 									(CDOObject) o,
 									new EReference[] { LibraryPackage.Literals.NET_XRESOURCE__METRIC_REF });
 
 					if (queryXRefs != null) {
 						for (CDOObjectReference xref : queryXRefs) {
 
 							EObject referencingEObject = xref.getSourceObject();
 							// Gather all metrics from the target source.
 							if (referencingEObject instanceof NetXResource) {
 								NetXResource res = (NetXResource) referencingEObject;
 								Node n = this.nodeFor(res.getComponentRef());
 								if (n != null) {
 									targetListNetXResources
 											.add((NetXResource) referencingEObject);
 								}
 							}
 
 						}
 					}
 
 				} catch (Exception e) {
 					e.printStackTrace();
 					// The query sometimes throws exeception, if i.e an entity
 					// can't be found..
 					// EClass ExpressionResult does not have an entity name, has
 					// it been mapped to Hibernate?
 				}
 			}
 		}
 
 		return targetListNetXResources;
 	}
 
 	/**
 	 * This User's role.
 	 * 
 	 * @param users
 	 * @return
 	 */
 	public Role roleForUserWithName(String loginName, List<Person> users) {
 		Person result = null;
 		for (Person p : users) {
 			if (p.eIsSet(GenericsPackage.Literals.PERSON__LOGIN)) {
 				if (p.getLogin().equals(loginName)) {
 					result = p;
 					break;
 
 				}
 			}
 		}
 		if (result != null) {
 			return result.getRoles();
 		}
 		return null;
 	}
 
 	public Value mostRecentValue(List<Value> rawListOfValues) {
 		List<Value> values = this
 				.sortValuesByTimeStampAndReverse(rawListOfValues);
 		if (values.size() > 0) {
 			return values.get(0);
 		}
 		return null;
 	}
 
 	public Value oldestValue(List<Value> rawListOfValues) {
 		List<Value> values = this.sortValuesByTimeStamp(rawListOfValues);
 		if (values.size() > 0) {
 			return values.get(0);
 		}
 		return null;
 	}
 
 	/**
 	 * Iterate through the ranges, and find for this interval.
 	 * 
 	 * @param resource
 	 * @param targetInterval
 	 * @return
 	 */
 	public Value mostRecentCapacityValue(NetXResource resource) {
 		return mostRecentValue(resource.getCapacityValues());
 	}
 
 	/**
 	 * Will return an empty list, if no range is found with the provided
 	 * parameters.
 	 * 
 	 * @param res
 	 * @param intervalHint
 	 * @param kh
 	 * @param dtr
 	 * @return
 	 */
 	public List<Value> valueRangeForIntervalKindAndPeriod(NetXResource res,
 			int intervalHint, KindHintType kh, DateTimeRange dtr) {
 
 		MetricValueRange mvr;
 		if (kh == null) {
 			mvr = valueRangeForInterval(res, intervalHint);
 		} else {
 			mvr = valueRangeForIntervalAndKind(res, kh, intervalHint);
 		}
 
 		if (mvr != null) {
 			Iterable<Value> filterValues = Iterables.filter(
 					mvr.getMetricValues(), valueInsideRange(dtr));
 			return Lists.newArrayList(filterValues);
 		}
 		return Lists.newArrayList();
 	}
 
 	/**
 	 * Iterate through the ranges, and find for this interval.
 	 * 
 	 * @param resource
 	 * @param targetInterval
 	 * @return
 	 */
 	public MetricValueRange valueRangeForInterval(NetXResource resource,
 			int targetInterval) {
 		for (MetricValueRange mvr : resource.getMetricValueRanges()) {
 			if (mvr.getIntervalHint() == targetInterval) {
 				return mvr;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Note, side effect of creating the value range if the range doesn't exist.
 	 */
 	public MetricValueRange valueRangeForIntervalAndKind(
 			NetXResource foundNetXResource, KindHintType kindHintType,
 			int intervalHint) {
 		MetricValueRange foundMvr = null;
 		for (final MetricValueRange mvr : foundNetXResource
 				.getMetricValueRanges()) {
 
 			// A succesfull match on Kind and Interval.
 			if (mvr.getKindHint() == kindHintType
 					&& mvr.getIntervalHint() == intervalHint) {
 				foundMvr = mvr;
 				break;
 			}
 		}
 		return foundMvr;
 	}
 
 	/**
 	 * Note, side effect of creating the value range if the range doesn't exist.
 	 */
 	public MetricValueRange valueRangeForIntervalAndKindGetOrCreate(
 			NetXResource foundNetXResource, KindHintType kindHintType,
 			int intervalHint) {
 		MetricValueRange foundMvr = null;
 		for (final MetricValueRange mvr : foundNetXResource
 				.getMetricValueRanges()) {
 
 			// A succesfull match on Kind and Interval.
 			if (mvr.getKindHint() == kindHintType
 					&& mvr.getIntervalHint() == intervalHint) {
 				foundMvr = mvr;
 				break;
 			}
 		}
 
 		if (foundMvr == null) {
 			foundMvr = MetricsFactory.eINSTANCE.createMetricValueRange();
 			foundMvr.setKindHint(kindHintType);
 			foundMvr.setIntervalHint(intervalHint);
 			foundNetXResource.getMetricValueRanges().add(foundMvr);
 		}
 		return foundMvr;
 	}
 
 	public List<NetXResource> resourcesFromNodeTypes(List<NodeType> nodeTypes) {
 
 		List<NetXResource> allResources = Lists.newArrayList();
 		for (NodeType nt : nodeTypes) {
 			TreeIterator<EObject> eAllContents = nt.eAllContents();
 			while (eAllContents.hasNext()) {
 				EObject next = eAllContents.next();
 				if (next instanceof NetXResource) {
 					allResources.add((NetXResource) next);
 				}
 			}
 		}
 		return allResources;
 	}
 
 	public List<NetXResource> resourcesWithExpressionNameFromNodeTypes(
 			List<NodeType> nodeTypes, NetXResource resource) {
 
 		List<NetXResource> allResources = Lists.newArrayList();
 		for (NodeType nt : nodeTypes) {
 			List<NetXResource> resources = resourcesWithExpressionName(nt,
 					resource.getExpressionName());
 			allResources.addAll(resources);
 		}
 		return allResources;
 	}
 
 	public void deriveValues(ServiceDistribution distribution, List<Node> nodes) {
 
 		// Sequence of the nodes is by Leaf first, and then follow the
 		// relationships.
 		// Need an algo, to build a matrix of sorted nodes.
 
 	}
 
 	public void printMatrix(Node[][] matrix) {
 		for (int i = 0; i < matrix.length; i++) {
 			for (int j = 0; j < matrix[0].length; j++) {
 				Node n = matrix[i][j];
 				if (n != null) {
 					System.out.print(n.getNodeID() + ",");
 				}
 			}
 			System.out.println("\n");
 		}
 	}
 
 	public String printNodeStructure(Node node) {
 		StringBuilder result = new StringBuilder();
 		result.append("-" + printModelObject(node) + "\n");
 		if (node.eIsSet(OperatorsPackage.Literals.NODE__NODE_TYPE)) {
 			NodeType nt = node.getNodeType();
 			result.append("-" + printModelObject(nt) + "\n");
 			result.append(this.printComponents("--",
 					transformToComponents(nt.getFunctions())));
 			result.append(this.printComponents("--",
 					transformToComponents(nt.getEquipments())));
 		}
 		return result.toString();
 	}
 
 	public String printComponents(String prefix, List<Component> components) {
 		StringBuilder result = new StringBuilder();
 		for (Component c : components) {
 			result.append(prefix + printModelObject(c) + "\n");
 
 			if (c instanceof Equipment) {
 				result.append(printComponents("--" + prefix,
 						transformToComponents(((Equipment) c).getEquipments())));
 			} else if (c instanceof com.netxforge.netxstudio.library.Function) {
 				result.append(printComponents(
 						"--" + prefix,
 						transformToComponents(((com.netxforge.netxstudio.library.Function) c)
 								.getFunctions())));
 			}
 
 		}
 		return result.toString();
 	}
 
 	public String printModelObject(EObject o) {
 		StringBuilder result = new StringBuilder();
 
 		if (o instanceof Network) {
 			Network net = (Network) o;
 			result.append("Network: name=" + net.getName());
 		}
 
 		if (o instanceof Node) {
 			Node n = (Node) o;
 			result.append("Node: name=" + n.getNodeID());
 		}
 
 		if (o instanceof Equipment) {
 			result.append("Equipment: code="
 					+ ((Equipment) o).getEquipmentCode());
 		}
 		if (o instanceof com.netxforge.netxstudio.library.Function) {
 			result.append("Function: name="
 					+ ((com.netxforge.netxstudio.library.Function) o).getName());
 		}
 		if (o instanceof NodeType) {
 			NodeType nt = (NodeType) o;
 			result.append("NodeType: name=" + nt.getName());
 		}
 		if (o instanceof NetXResource) {
 			NetXResource nt = (NetXResource) o;
 			result.append("NetXResource: short name=" + nt.getShortName());
 		}
 
 		if (o instanceof Service) {
 			Service nt = (Service) o;
 			result.append("Service: name=" + nt.getServiceName());
 		}
 
 		if (o instanceof MetricSource) {
 			MetricSource ms = (MetricSource) o;
 			result.append("Metric Source: name=" + ms.getName());
 		}
 		if (o instanceof Mapping) {
 			Mapping mapping = (Mapping) o;
 			result.append("Mapping: datarow=" + mapping.getFirstDataRow()
 					+ "interval=" + mapping.getIntervalHint() + ",colums="
 					+ mapping.getDataMappingColumns().size());
 		}
 		if (o instanceof MappingColumn) {
 			MappingColumn mc = (MappingColumn) o;
 			result.append("mapping column: " + mc.getColumn());
 		}
 		if (o instanceof DataKind) {
 			DataKind dk = (DataKind) o;
 			if (dk instanceof IdentifierDataKind) {
 				result.append("Identifier Datakind: "
 						+ ((IdentifierDataKind) dk).getObjectKind());
 			}
 			if (dk instanceof ValueDataKind) {
 				result.append("Value Datakind: "
 						+ ((ValueDataKind) dk).getValueKind());
 			}
 		}
 
 		// if( ECoreUtil.geto.getClass() != null){
 		// result.append(" class=" + o.eClass().getName());
 		// }else if(o.eResource() != null){
 		// result.append(" resource=" + o.eResource().getURI().toString());
 		// }
 		// result.append(" ( CDO Info object=" + ((CDOObject) o).cdoRevision()
 		// + " )");
 
 		return result.toString();
 	}
 
 	public Node[][] matrix(List<Node> nodes) {
 
 		// Node[][] emptyMatrix = new Node[0][0];
 
 		List<NodeType> nts = this.transformNodeToNodeType(nodes);
 		List<NodeType> unique = this.uniqueNodeTypes(nts);
 		List<NodeType> sortedByIsLeafCopy = Ordering
 				.from(this.nodeTypeIsLeafComparator()).reverse()
 				.sortedCopy(unique);
 
 		int ntCount = sortedByIsLeafCopy.size();
 		int nodeDepth = 0;
 
 		// We need a two pass, to determine the array size first.
 		// Is there another trick?
 
 		for (NodeType nt : sortedByIsLeafCopy) {
 			Iterable<Node> filtered = Iterables.filter(nodes,
 					this.nodeOfType(nt));
 			if (Iterables.size(filtered) > nodeDepth) {
 				nodeDepth = Iterables.size(filtered);
 			}
 		}
 
 		Node[][] matrix = new Node[ntCount][nodeDepth];
 
 		for (int i = 0; i < ntCount; i++) {
 			NodeType nt = sortedByIsLeafCopy.get(i);
 			Iterable<Node> filtered = Iterables.filter(nodes,
 					this.nodeOfType(nt));
 			for (int j = 0; j < Iterables.size(filtered); j++) {
 				Node n = Iterables.get(filtered, j);
 				matrix[i][j] = n;
 			}
 		}
 
 		return matrix;
 	}
 
 	public List<Relationship> connections(RFSService service, Node n) {
 
 		if (service.eContainer() instanceof Operator) {
 			Operator op = (Operator) service.eContainer();
 
 			List<Relationship> relationships = Lists.newArrayList();
 			TreeIterator<EObject> eAllContents = op.eAllContents();
 			while (eAllContents.hasNext()) {
 				EObject eo = eAllContents.next();
 				if (eo instanceof Relationship) {
 					relationships.add((Relationship) eo);
 				}
 			}
 
 			List<Relationship> filteredRelationships = Lists.newArrayList();
 			Iterable<Relationship> filtered = Iterables.filter(relationships,
 					this.sourceRelationshipInNode(n));
 			if (Iterables.size(filtered) > 0) {
 				filteredRelationships.addAll(Lists.newArrayList(filtered));
 			}
 			return filteredRelationships;
 		}
 		return null;
 	};
 
 	public List<Node> connectedNodes(RFSService service) {
 
 		if (service.eContainer() instanceof Operator) {
 			Operator op = (Operator) service.eContainer();
 
 			List<Relationship> relationships = Lists.newArrayList();
 			TreeIterator<EObject> eAllContents = op.eAllContents();
 			while (eAllContents.hasNext()) {
 				EObject eo = eAllContents.next();
 				if (eo instanceof Relationship) {
 					relationships.add((Relationship) eo);
 				}
 			}
 
 			List<Relationship> filteredRelationships = Lists.newArrayList();
 
 			for (Node n : service.getNodes()) {
 				Iterable<Relationship> filtered = Iterables.filter(
 						relationships, this.sourceRelationshipInNode(n));
 				if (Iterables.size(filtered) > 0) {
 					filteredRelationships.addAll(Lists.newArrayList(filtered));
 				}
 			}
 		}
 		return null;
 
 	};
 
 	/**
 	 * Resources with this name. Notice: Matching is on regular expression, i.e.
 	 * name = .* is all resources.
 	 * 
 	 * Very slow approach.
 	 * 
 	 * @param components
 	 * @param name
 	 * @param closure
 	 *            decend the child hierarchy and look for resources when
 	 *            <code>true</code>
 	 * @return
 	 */
 	public List<NetXResource> resourcesWithExpressionName(
 			List<Component> components, String name, boolean closure) {
 		final List<NetXResource> rl = Lists.newArrayList();
 
 		for (final Component c : components) {
 			for (final NetXResource r : c.getResourceRefs()) {
 				String expName = r.getExpressionName();
 				if (expName.matches(name)) {
 					rl.add(r);
 				}
 			}
 
 			if (closure) {
 				final List<Component> cl = Lists.newArrayList();
 				if (c instanceof Equipment) {
 					cl.addAll(((Equipment) c).getEquipments());
 				}
 				if (c instanceof com.netxforge.netxstudio.library.Function) {
 					cl.addAll(((com.netxforge.netxstudio.library.Function) c)
 							.getFunctions());
 				}
 				rl.addAll(this.resourcesWithExpressionName(cl, name, closure));
 			}
 		}
 		return rl;
 	}
 
 	/**
 	 * Merge the time from a date into a given base date and return the result.
 	 * 
 	 * @param baseDate
 	 * @param dateWithTime
 	 * @return
 	 */
 	public Date mergeTimeIntoDate(Date baseDate, Date dateWithTime) {
 		final Calendar baseCalendar = GregorianCalendar.getInstance();
 		baseCalendar.setTime(baseDate);
 
 		final Calendar dateWithTimeCalendar = GregorianCalendar.getInstance();
 		dateWithTimeCalendar.setTime(dateWithTime);
 
 		baseCalendar.set(Calendar.HOUR_OF_DAY,
 				dateWithTimeCalendar.get(Calendar.HOUR_OF_DAY));
 		baseCalendar.set(Calendar.MINUTE,
 				dateWithTimeCalendar.get(Calendar.MINUTE));
 		return baseCalendar.getTime();
 
 	}
 
 	public List<Integer> weekDaysAsInteger() {
 		final List<Integer> week = ImmutableList.of(Calendar.MONDAY,
 				Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY,
 				Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY);
 		return week;
 	}
 
 	public int weekDay(Date date) {
 
 		final Function<Date, Integer> getDayString = new Function<Date, Integer>() {
 			public Integer apply(Date from) {
 				final Calendar c = GregorianCalendar.getInstance();
 				c.setTime(from);
 				return new Integer(c.get(Calendar.DAY_OF_WEEK));
 			}
 		};
 		return getDayString.apply(date);
 	}
 
 	public String weekDay(Integer weekDay) {
 		final Function<Integer, String> getDayString = new Function<Integer, String>() {
 			public String apply(Integer from) {
 				final Calendar c = GregorianCalendar.getInstance();
 				c.set(Calendar.DAY_OF_WEEK, from.intValue());
 				final Date date = c.getTime();
 				final SimpleDateFormat df = new SimpleDateFormat("EEEE");
 				return df.format(date);
 			}
 		};
 		return getDayString.apply(weekDay);
 	}
 
 	public String date(Date d) {
 		final Function<Date, String> getDateString = new Function<Date, String>() {
 			public String apply(Date from) {
 				final SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
 				return df.format(from);
 			}
 		};
 		return getDateString.apply(d);
 	}
 
 	public String folderDate(Date d) {
 		final Function<Date, String> getDateString = new Function<Date, String>() {
 			public String apply(Date from) {
 				final SimpleDateFormat df = new SimpleDateFormat("ddMMyyyy");
 				return df.format(from);
 			}
 		};
 		return getDateString.apply(d);
 	}
 
 	public String time(Date d) {
 		final Function<Date, String> getDateString = new Function<Date, String>() {
 			public String apply(Date from) {
 				final SimpleDateFormat df = new SimpleDateFormat("HH:mm");
 				return df.format(from);
 			}
 		};
 		return getDateString.apply(d);
 	}
 
 	public String timeAndSeconds(Date d) {
 		final Function<Date, String> getDateString = new Function<Date, String>() {
 			public String apply(Date from) {
 				final SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
 				return df.format(from);
 			}
 		};
 		return getDateString.apply(d);
 	}
 
 	public String currentTimeAndSeconds() {
 		return timeAndSeconds(new Date());
 	}
 
 	public String dateAndTime(XMLGregorianCalendar d) {
 		Date date = fromXMLDate(d);
 		return dateAndTime(date);
 	}
 
 	public String dateAndTime(Date d) {
 
 		StringBuilder sb = new StringBuilder();
 
 		final Function<Date, String> getDateString = new Function<Date, String>() {
 			public String apply(Date from) {
 				final SimpleDateFormat df = new SimpleDateFormat("HHmm");
 				return df.format(from);
 			}
 		};
 		sb.append(folderDate(d) + "_");
 		sb.append(getDateString.apply(d));
 		return sb.toString();
 	}
 
 	/**
 	 * Get the days of the week, in a long textual format i.e. "Monday". The
 	 * days of the week, adapts to the current Locale.
 	 * 
 	 * @return
 	 */
 	public List<String> weekDays() {
 		final Function<Integer, String> getDayString = new Function<Integer, String>() {
 			public String apply(Integer from) {
 				final Calendar c = GregorianCalendar.getInstance();
 				c.set(Calendar.DAY_OF_WEEK, from.intValue());
 				final Date date = c.getTime();
 				final SimpleDateFormat df = new SimpleDateFormat("EEEE");
 				return df.format(date);
 			}
 		};
 
 		return Lists.transform(weekDaysAsInteger(), getDayString);
 	}
 
 	public int weekDay(String day) {
 		final Function<String, Integer> getDayFromString = new Function<String, Integer>() {
 			public Integer apply(String from) {
 				try {
 					final Date d = DateFormat.getDateInstance().parse(from);
 					final Calendar c = GregorianCalendar.getInstance();
 					c.setTime(d);
 					return c.get(Calendar.DAY_OF_WEEK);
 
 				} catch (final ParseException e) {
 					e.printStackTrace();
 				}
 				return -1;
 			}
 		};
 		return getDayFromString.apply(day).intValue();
 	}
 
 	public Date mergeDateIntoTime(Date baseTime, Date targetDate) {
 
 		final Calendar baseCalendar = GregorianCalendar.getInstance();
 		baseCalendar.setTime(baseTime);
 
 		final Calendar targetCalendar = GregorianCalendar.getInstance();
 		targetCalendar.setTime(targetDate);
 
 		// CB 06-09-2011, removed date has to be later requirement.
 		// if (targetCalendar.compareTo(GregorianCalendar.getInstance()) > 0) {
 		baseCalendar.set(Calendar.YEAR, targetCalendar.get(Calendar.YEAR));
 		baseCalendar.set(Calendar.MONTH, targetCalendar.get(Calendar.MONTH));
 		baseCalendar.set(Calendar.WEEK_OF_YEAR,
 				targetCalendar.get(Calendar.WEEK_OF_YEAR));
 
 		// We need to roll the week, if the target day
 		// is after the current day in that same week
 		if (targetCalendar.get(Calendar.WEEK_OF_YEAR) == baseCalendar
 				.get(Calendar.WEEK_OF_YEAR)
 				&& targetCalendar.get(Calendar.DAY_OF_WEEK) > baseCalendar
 						.get(Calendar.DAY_OF_WEEK)) {
 			baseCalendar.add(Calendar.WEEK_OF_YEAR, 1);
 		}
 		// baseCalendar.set(Calendar.DAY_OF_WEEK,
 		// targetCalendar.get(Calendar.DAY_OF_WEEK));
 		// }
 		return baseCalendar.getTime();
 	}
 
 	/**
 	 * Calculate a new date for a certain day of week and hour of day. If the
 	 * startdate is not provided or earlier than today, the current date (today)
 	 * is used.
 	 * 
 	 * @param baseDate
 	 * @param dayOfWeek
 	 * @return
 	 */
 	public Date mergeDayIntoDate(Date baseDate, int dayOfWeek) {
 
 		final Calendar c = GregorianCalendar.getInstance();
 		c.setTime(baseDate);
 		if (dayOfWeek != -1) {
 			c.set(Calendar.DAY_OF_WEEK, dayOfWeek);
 		}
 		return c.getTime();
 	}
 
 	public XMLGregorianCalendar toXMLDate(Date date) {
 		final XMLGregorianCalendar gregCalendar = dataTypeFactory
 				.newXMLGregorianCalendar();
 		final Calendar calendar = GregorianCalendar.getInstance();
 		calendar.setTime(date);
 
 		gregCalendar.setYear(calendar.get(Calendar.YEAR));
 		gregCalendar.setMonth(calendar.get(Calendar.MONTH) + 1); // correct with
 																	// 1 on
 																	// purpose
 		gregCalendar.setDay(calendar.get(Calendar.DAY_OF_MONTH));
 
 		gregCalendar.setHour(calendar.get(Calendar.HOUR_OF_DAY));
 		gregCalendar.setMinute(calendar.get(Calendar.MINUTE));
 		gregCalendar.setSecond(calendar.get(Calendar.SECOND));
 		gregCalendar.setMillisecond(calendar.get(Calendar.MILLISECOND));
 		// gregCalendar.setTimezone(calendar.get(Calendar.ZONE_OFFSET));
 
 		return gregCalendar;
 	}
 
 	public Date fromXMLDate(XMLGregorianCalendar date) {
 		return date.toGregorianCalendar().getTime();
 	}
 
 	public int daysInJanuary(int year) {
 		return daysInMonth(year, Calendar.JANUARY);
 	}
 
 	public int daysInFebruari(int year) {
 		return daysInMonth(year, Calendar.FEBRUARY);
 	}
 
 	public int daysInMarch(int year) {
 		return daysInMonth(year, Calendar.MARCH);
 	}
 
 	public int daysInApril(int year) {
 		return daysInMonth(year, Calendar.APRIL);
 	}
 
 	// .... etc...
 
 	public int daysInMonth(int year, int month) {
 		final Calendar cal = new GregorianCalendar(year, month, 1);
 		return cal.getActualMaximum(Calendar.DAY_OF_MONTH);
 	}
 
 	public Date lastWeek() {
 		final Calendar cal = GregorianCalendar.getInstance();
 		cal.setTime(new Date(System.currentTimeMillis()));
 		cal.add(Calendar.WEEK_OF_YEAR, -1);
 		return cal.getTime();
 	}
 
 	public Date yesterday() {
 		final Calendar cal = GregorianCalendar.getInstance();
 		cal.setTime(new Date(System.currentTimeMillis()));
 		cal.add(Calendar.DAY_OF_WEEK, -1);
 		return cal.getTime();
 	}
 
 	public Date tomorrow() {
 		final Calendar cal = GregorianCalendar.getInstance();
 		cal.setTime(new Date(System.currentTimeMillis()));
 		cal.add(Calendar.DAY_OF_WEEK, 1);
 		return cal.getTime();
 	}
 
 	public Date twoDaysAgo() {
 		final Calendar cal = GregorianCalendar.getInstance();
 		cal.setTime(new Date(System.currentTimeMillis()));
 		cal.add(Calendar.DAY_OF_MONTH, -2);
 		return cal.getTime();
 	}
 
 	public Date threeDaysAgo() {
 		final Calendar cal = Calendar.getInstance();
 		cal.setTime(new Date(System.currentTimeMillis()));
 		cal.add(Calendar.DAY_OF_MONTH, -3);
 		return cal.getTime();
 	}
 
 	public Date fourDaysAgo() {
 		final Calendar cal = Calendar.getInstance();
 		cal.setTime(new Date(System.currentTimeMillis()));
 		cal.add(Calendar.DAY_OF_MONTH, -4);
 		return cal.getTime();
 	}
 
 	public Date daysAgo(int days) {
 		final Calendar cal = Calendar.getInstance();
 		cal.setTime(new Date(System.currentTimeMillis()));
 		cal.add(Calendar.DAY_OF_YEAR, -days);
 		return cal.getTime();
 
 	}
 
 	public Date oneWeekAgo() {
 		final Calendar cal = GregorianCalendar.getInstance();
 		cal.setTime(new Date(System.currentTimeMillis()));
 		cal.add(Calendar.WEEK_OF_YEAR, -1);
 		return cal.getTime();
 	}
 
 	public Date oneMonthAgo() {
 		final Calendar cal = GregorianCalendar.getInstance();
 		cal.setTime(new Date(System.currentTimeMillis()));
 		cal.add(Calendar.MONTH, -1);
 		return cal.getTime();
 	}
 
 	public Date twoMonthsAgo() {
 		final Calendar cal = GregorianCalendar.getInstance();
 		cal.setTime(new Date(System.currentTimeMillis()));
 		cal.add(Calendar.MONTH, -2);
 		return cal.getTime();
 	}
 
 	public Date threeMonthsAgo() {
 		final Calendar cal = GregorianCalendar.getInstance();
 		cal.setTime(new Date(System.currentTimeMillis()));
 		cal.add(Calendar.MONTH, -3);
 		return cal.getTime();
 	}
 
 	public Date sixMonthsAgo() {
 		final Calendar cal = GregorianCalendar.getInstance();
 		cal.setTime(new Date(System.currentTimeMillis()));
 		cal.add(Calendar.MONTH, -6);
 		return cal.getTime();
 	}
 
 	public Date todayAndNow() {
 		final Calendar cal = Calendar.getInstance();
 		cal.setTime(new Date(System.currentTimeMillis()));
 		return cal.getTime();
 	}
 
 	public Date todayAtDayEnd() {
 		final Calendar cal = Calendar.getInstance();
 		cal.setTime(new Date(System.currentTimeMillis()));
 		adjustToDayEnd(cal);
 		return cal.getTime();
 	}
 
 	/**
 	 * Set a period to day start and end.
 	 * 
 	 * @param from
 	 * @param to
 	 */
 	public void adjustToDayStartAndEnd(Date from, Date to) {
 		this.adjustToDayStart(from);
 		this.adjustToDayEnd(to);
 	}
 
 	/**
 	 * Set the hour, minutes, seconds and milliseconds so the calendar
 	 * represents midnight, which is the start of the day.
 	 * 
 	 * @param cal
 	 */
 	public void adjustToDayStart(Calendar cal) {
 		// When doing this, we push it forward one day, so if the day is 7 Jan
 		// at 11:50:27h,
 		// it will become 8 Jan at 00:00:00h, so we substract one day.
 		cal.add(Calendar.DAY_OF_MONTH, -1);
 		cal.set(Calendar.HOUR_OF_DAY, 24);
 		cal.set(Calendar.MINUTE, 0);
 		cal.set(Calendar.SECOND, 0);
 		cal.set(Calendar.MILLISECOND, 0);
 
 	}
 
 	public Date adjustToDayStart(Date d) {
 		final Calendar cal = Calendar.getInstance();
 		cal.setTime(d);
 		this.adjustToDayStart(cal);
 		return cal.getTime();
 	}
 
 	/**
 	 * Set the hours, minutes, seconds and milliseconds so the calendar
 	 * represents midnight minus one milli-second.
 	 * 
 	 * @param cal
 	 */
 	public void adjustToDayEnd(Calendar cal) {
 		cal.set(Calendar.HOUR_OF_DAY, 23);
 		cal.set(Calendar.MINUTE, 59);
 		cal.set(Calendar.SECOND, 59);
 		cal.set(Calendar.MILLISECOND, 999);
 
 	}
 
 	public Date adjustToDayEnd(Date d) {
 		final Calendar cal = Calendar.getInstance();
 		cal.setTime(d);
 		this.adjustToDayEnd(cal);
 		return cal.getTime();
 	}
 
 	public void setToFullHour(Calendar cal) {
 		cal.set(Calendar.MINUTE, 00);
 		cal.set(Calendar.SECOND, 00);
 		cal.set(Calendar.MILLISECOND, 000);
 	}
 
 	public int inSeconds(String field) {
 		final Function<String, Integer> getFieldInSeconds = new Function<String, Integer>() {
 			public Integer apply(String from) {
 				if (from.equals("Week")) {
 					return ModelUtils.SECONDS_IN_A_WEEK;
 				}
 				if (from.equals("Day")) {
 					return ModelUtils.SECONDS_IN_A_DAY;
 				}
 				if (from.equals("Hour")) {
 					return ModelUtils.SECONDS_IN_AN_HOUR;
 				}
 				if (from.equals("Quarter")) {
 					return ModelUtils.SECONDS_IN_15MIN;
 				}
 
 				if (from.endsWith("min")) {
 					// Strip the minutes
 					int indexOfMin = from.indexOf("min");
 					from = from.substring(0, indexOfMin).trim();
 					try {
 						return new Integer(from) * 60;
 					} catch (final NumberFormatException nfe) {
 						nfe.printStackTrace();
 					}
 				}
 
 				try {
 					return new Integer(from);
 				} catch (final NumberFormatException nfe) {
 					nfe.printStackTrace();
 				}
 				return -1;
 			}
 		};
 		return getFieldInSeconds.apply(field);
 	}
 
 	public String fromMinutes(int minutes) {
 
 		switch (minutes) {
 		case MINUTES_IN_A_MONTH: {
 			return "Month";
 		}
 		case MINUTES_IN_A_WEEK: {
 			return "Week";
 		}
 		}
 		return this.fromSeconds(minutes * 60);
 	}
 
 	/**
 	 * Convert in an interval in seconds to a String value. The Week, Day and
 	 * Hour values in seconds are converted to the respective screen. Any other
 	 * value is converted to the number of minutes with a "min" prefix.
 	 * 
 	 * @param secs
 	 * @return
 	 */
 	public String fromSeconds(int secs) {
 		final Function<Integer, String> getFieldInSeconds = new Function<Integer, String>() {
 			public String apply(Integer from) {
 
 				if (from.equals(ModelUtils.SECONDS_IN_A_MONTH)) {
 					return "Month";
 				}
 				if (from.equals(ModelUtils.SECONDS_IN_A_WEEK)) {
 					return "Week";
 				}
 				if (from.equals(ModelUtils.SECONDS_IN_A_DAY)) {
 					return "Day";
 				}
 				if (from.equals(ModelUtils.SECONDS_IN_AN_HOUR)) {
 					return "Hour";
 				}
 
 				// if (from.equals(ModelUtils.SECONDS_IN_A_QUARTER)) {
 				// return "Quarter";
 				// }
 
 				// Do also multiples intepretation in minutes.
 				if (from.intValue() % 60 == 0) {
 					int minutes = from.intValue() / 60;
 					return new Integer(minutes).toString() + " min";
 				}
 
 				return new Integer(from).toString();
 			}
 		};
 		return getFieldInSeconds.apply(secs);
 	}
 
 	public int inWeeks(String field) {
 		final Function<String, Integer> getFieldInSeconds = new Function<String, Integer>() {
 			public Integer apply(String from) {
 				if (from.equals("Week")) {
 					return 1;
 				}
 				return null;
 			}
 		};
 		return getFieldInSeconds.apply(field);
 	}
 
 	public String toString(Date date) {
 		return DateFormat.getDateInstance().format(date);
 	}
 
 	/**
 	 * limits occurences to 52.
 	 * 
 	 * @param start
 	 * @param end
 	 * @param interval
 	 * @param repeat
 	 * @return
 	 */
 	public List<Date> occurences(Date start, Date end, int interval, int repeat) {
 		return this.occurences(start, end, interval, repeat, 52);
 	}
 
 	public List<Date> occurences(Date start, Date end, int interval,
 			int repeat, int maxEntries) {
 
 		final List<Date> occurences = Lists.newArrayList();
 		Date occurenceDate = start;
 		occurences.add(start);
 
 		if (repeat > 0 && interval > 1) {
 			// We roll on the interval from the start date until repeat is
 			// reached.
 			for (int i = 0; i < repeat; i++) {
 				occurenceDate = rollSeconds(occurenceDate, interval);
 				occurences.add(occurenceDate);
 			}
 			return occurences;
 		}
 		if (end != null && interval > 1) {
 			// We roll on the interval from the start date until the end date.
 			int i = 0;
 			while (i < maxEntries) {
 				occurenceDate = rollSeconds(occurenceDate, interval);
 				if (!crossedDate(end, occurenceDate)) {
 					occurences.add(occurenceDate);
 				} else {
 					break;
 				}
 				i++;
 			}
 			return occurences;
 		}
 		if (repeat == 0 && interval > 1) {
 			int i = 0;
 			while (i < maxEntries) {
 				occurenceDate = rollSeconds(occurenceDate, interval);
 				occurences.add(occurenceDate);
 				i++;
 			}
 			return occurences;
 		}
 
 		return occurences;
 	}
 
 	public Date rollSeconds(Date baseDate, int seconds) {
 		final Calendar c = GregorianCalendar.getInstance();
 		c.setTime(baseDate);
 
 		// We can't roll large numbers.
 		if (seconds / SECONDS_IN_A_DAY > 0) {
 			final int days = new Double(seconds / SECONDS_IN_A_DAY).intValue();
 			c.add(Calendar.DAY_OF_YEAR, days);
 			return c.getTime();
 		}
 		if (seconds / SECONDS_IN_AN_HOUR > 0) {
 			final int hours = new Double(seconds / SECONDS_IN_AN_HOUR)
 					.intValue();
 			c.add(Calendar.HOUR_OF_DAY, hours);
 			return c.getTime();
 		}
 
 		if (seconds / SECONDS_IN_A_MINUTE > 0) {
 			final int minutes = new Double(seconds / SECONDS_IN_A_MINUTE)
 					.intValue();
 			c.add(Calendar.MINUTE, minutes);
 			return c.getTime();
 		}
 
 		c.add(Calendar.SECOND, seconds);
 		return c.getTime();
 
 	}
 
 	public boolean crossedDate(Date refDate, Date variantDate) {
 		final Calendar refCal = GregorianCalendar.getInstance();
 		refCal.setTime(refDate);
 
 		final Calendar variantCal = GregorianCalendar.getInstance();
 		variantCal.setTime(variantDate);
 
 		return refCal.compareTo(variantCal) < 0;
 
 	}
 
 	/**
 	 * Casts to AbstractCDOIDLong and returns the long as value.
 	 * 
 	 * @param cdoObject
 	 * @return
 	 */
 	public String cdoLongIDAsString(CDOObject cdoObject) {
 		long lValue = ((AbstractCDOIDLong) cdoObject.cdoID()).getLongValue();
 		return new Long(lValue).toString();
 	}
 
 	public String cdoResourcePath(CDOObject cdoObject) {
 		if (cdoObject.eResource() != null) {
 			Resource eResource = cdoObject.eResource();
 			if (eResource instanceof CDOResource) {
 				CDOResource cdoR = (CDOResource) eResource;
 				return cdoR.getPath();
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Get all revisions from this object.
 	 * 
 	 * @param cdoObject
 	 * @return
 	 */
 	public Iterator<CDORevision> cdoRevisions(CDOObject cdoObject) {
 
 		List<CDORevision> revisions = Lists.newArrayList();
 
 		CDORevision cdoRevision = cdoObject.cdoRevision();
 		// get the previous.
 		for (int version = cdoRevision.getVersion(); version > 0; version--) {
 
 			CDOBranchVersion branchVersion = cdoRevision.getBranch()
 					.getVersion(version);
 
 			CDORevision revision = cdoObject
 					.cdoView()
 					.getSession()
 					.getRevisionManager()
 					.getRevisionByVersion(cdoObject.cdoID(), branchVersion, 0,
 							true);
 			revisions.add(revision);
 		}
 		return revisions.iterator();
 	}
 
 	public String cdoDumpNewObject(InternalCDORevision revision) {
 		final StringBuilder sb = new StringBuilder();
 		for (final EStructuralFeature feature : revision.getClassInfo()
 				.getAllPersistentFeatures()) {
 			final Object value = revision.getValue(feature);
 			cdoDumpFeature(sb, feature, value);
 		}
 		return truncate(sb.toString());
 	}
 
 	public void cdoDumpFeatureDeltas(StringBuilder sb,
 			List<CDOFeatureDelta> featureDeltas) {
 		for (final CDOFeatureDelta featureDelta : featureDeltas) {
 			if (featureDelta instanceof CDOListFeatureDelta) {
 				final CDOListFeatureDelta list = (CDOListFeatureDelta) featureDelta;
 				cdoDumpFeatureDeltas(sb, list.getListChanges());
 			} else {
 				cdoDumpFeature(sb, featureDelta.getFeature(), featureDelta);
 			}
 		}
 	}
 
 	public void cdoDumpFeature(StringBuilder sb, EStructuralFeature feature,
 			Object value) {
 		addNewLine(sb);
 		sb.append(feature.getName() + " = " + value);
 	}
 
 	public void cdoDumpFeature(StringBuilder sb, EStructuralFeature feature,
 			CDOFeatureDelta value) {
 		addNewLine(sb);
 		sb.append(feature.getName() + " = " + cdoPrintFeatureDelta(value));
 	}
 
 	public String cdoPrintFeatureDelta(CDOFeatureDelta delta) {
 		String str = delta.toString();
 		if (str.indexOf(",") != -1) {
 			// do + 2 to get of one space
 			str = str.substring(str.indexOf(",") + 2);
 		}
 		// and get rid of the ] at the end
 		return str.substring(0, str.length() - 1);
 	}
 
 	public void addNewLine(StringBuilder sb) {
 		if (sb.length() > 0) {
 			sb.append("\n");
 		}
 	}
 
 	public String truncate(String value) {
 		if (value.length() >= MAX_CHANGE_LENGTH) {
 			return value.substring(0, MAX_CHANGE_LENGTH);
 		}
 		return value;
 	}
 
 	public CDOObject cdoObject(CDOObject currentObject, CDORevision cdoRevision) {
 		CDOView revView = currentObject.cdoView().getSession().openView();
 		boolean revViewOk = revView.setTimeStamp(cdoRevision.getTimeStamp());
 		if (revViewOk) {
 			CDOObject object = revView.getObject(cdoRevision.getID());
 			return object;
 		}
 		return null;
 	}
 
 	public void cdoPrintRevisionDelta(CDORevisionDelta delta) {
 		for (CDOFeatureDelta fd : delta.getFeatureDeltas()) {
 			System.out.println("-- delta=" + fd);
 		}
 	}
 
 	/**
 	 * Extract the EReference's with referen type is Expression from a target
 	 * object.
 	 */
 	public List<EReference> expressionEReferences(EObject target) {
 		final List<EReference> expRefs = Lists.newArrayList();
 		for (EReference eref : target.eClass().getEAllReferences()) {
 			if (eref.getEReferenceType() == LibraryPackage.Literals.EXPRESSION) {
 				expRefs.add(eref);
 			}
 		}
 		return expRefs;
 	}
 
 	/**
 	 * Intended for use together with an ITableLabelProvider
 	 * <ul>
 	 * <li>Index = 0, returns a literal string of the feature for this delta.</li>
 	 * <li>Index = 1, returns a literal string of the delta type. (Add, Remove
 	 * etc..). {@link CDOFeatureDelta.Type}</li>
 	 * <li>Index = 2, returns the new value if any for this type, if an object
 	 * {@link #printModelObject(EObject)}</li>
 	 * <li>Index = 3, returns the old value if any for this type, if an object
 	 * {@link #printModelObject(EObject)}</li>
 	 * </ul>
 	 * 
 	 * @see CDOFeatureDelta
 	 * @param cdoFeatureDelta
 	 */
 	public String cdoFeatureDeltaIndex(CDOFeatureDelta cdoFeatureDelta,
 			int index) {
 
 		// Only support index in a range.
 		assert index >= 0 && index <= 3;
 
 		Object newValue = null;
 		Object oldValue = null;
 
 		// if index = 0, we simp
 		if (index == 0) {
 			return cdoFeatureDelta.getFeature().getName();
 		} else if (index == 1) {
 			return cdoFeatureDelta.getType().name();
 		} else if (index == 2 || index == 3) {
 			switch (cdoFeatureDelta.getType()) {
 			case ADD: {
 				CDOAddFeatureDelta fdType = (CDOAddFeatureDelta) cdoFeatureDelta;
 				newValue = fdType.getValue();
 			}
 				break;
 			case REMOVE: {
 				CDORemoveFeatureDelta fdType = (CDORemoveFeatureDelta) cdoFeatureDelta;
 				newValue = fdType.getValue();
 			}
 				break;
 			case CLEAR: {
 				// CDOClearFeatureDelta fdType = (CDOClearFeatureDelta) delta;
 				// has no value.
 			}
 				break;
 			case MOVE: {
 				CDOMoveFeatureDelta fdType = (CDOMoveFeatureDelta) cdoFeatureDelta;
 				newValue = fdType.getValue();
 
 				// A list position move.
 				fdType.getNewPosition();
 				fdType.getOldPosition();
 			}
 				break;
 			case SET: {
 				CDOSetFeatureDelta fdType = (CDOSetFeatureDelta) cdoFeatureDelta;
 				newValue = fdType.getValue();
 				oldValue = fdType.getOldValue();
 
 			}
 				break;
 			case UNSET: {
 				// CDOUnsetFeatureDelta fdType = (CDOUnsetFeatureDelta) delta;
 				// has no value.
 			}
 				break;
 			case LIST: {
 				CDOListFeatureDelta fdType = (CDOListFeatureDelta) cdoFeatureDelta;
 
 				@SuppressWarnings("unused")
 				List<CDOFeatureDelta> listChanges = fdType.getListChanges();
 				// What to do with this???
 			}
 				break;
 			case CONTAINER: {
 				CDOContainerFeatureDelta fdType = (CDOContainerFeatureDelta) cdoFeatureDelta;
 
 				// Assume one of the two...
 				fdType.getContainerID(); // The container ID.
 				fdType.getResourceID(); // The resource ID.
 			}
 				break;
 			}
 
 			if (index == 2 && newValue != null) {
 				if (newValue instanceof String) {
 					return (String) newValue;
 				} else if (newValue instanceof EObject) {
 					printModelObject((EObject) newValue).toString();
 				} else if (newValue instanceof CDOID) {
 					// It would be nice for references, to get the mutated CDOID
 					// and present it as a link
 					// to the object.
 					CDOID cdoID = (CDOID) newValue;
 					return "Object ID =" + cdoID.toString();
 				}
 			} else if (index == 3 && oldValue != null) {
 				return oldValue instanceof String ? (String) oldValue
 						: printModelObject((EObject) oldValue).toString();
 			}
 		}
 		return "";
 	}
 
 	/**
 	 * Appends the cdo Object ID to the actual object resource name.
 	 * 
 	 * @param object
 	 * @return
 	 */
 	public String resolveHistoricalResourceName(Object object) {
 
 		if (!(object instanceof CDOObject)) {
 			return null;
 		}
 
 		// TODO, keep a cache of CDOObject ID, and resource path.
 		String affectedPath = this.cdoResourcePath((CDOObject) object);
 
 		// The object needs to be in the correct state, if not persisted (CLEAN,
 		// DIRTY etc..),
 		// no cdoID will be present.
 		CDOID id = ((CDOObject) object).cdoID();
 		if (id != null) {
 			URI idURI = URI.createURI(id.toURIFragment());
 			String fragment = idURI.fragment();
 			if (fragment != null) {
 				String[] fragments = fragment.split("#");
 				affectedPath = affectedPath + "_"
 						+ fragments[fragments.length - 1];
 			}
 			return affectedPath;
 		} else
 			return null;
 	}
 
 	/*
 	 * Historical components can exist in a Node or NodeType. when checking the
 	 * path, we check both the Node and NodeType. (Both could be historical
 	 * elements).
 	 */
 	public boolean isHistoricalComponent(Component c) {
 
 		if (c instanceof CDOObject) {
 			String path = this.cdoResourcePath(c);
 
 			// Check for Node first.
 			Node node = this.nodeFor(c);
 			if (node != null) {
 				String nodeHistoricalPath = this
 						.resolveHistoricalResourceName(node);
 				if (path.equals(nodeHistoricalPath)) {
 					return true;
 				}
 				return false;
 			}
 
 			// Check for Node type.
 			NodeType nt = this.resolveParentNodeType(c);
 			if (nt != null) {
 				String nodeTypeHistoricalPath = this
 						.resolveHistoricalResourceName(nt);
 				if (path.equals(nodeTypeHistoricalPath)) {
 					return true;
 				}
 
 			}
 
 		}
 
 		return false;
 	}
 
 	/**
 	 * Transform a list of resources to a list of URI for the resource.
 	 * 
 	 * @param resources
 	 * @return
 	 */
 	public List<URI> transformResourceToURI(List<Resource> resources) {
 		final Function<Resource, URI> resourceToURI = new Function<Resource, URI>() {
 			public URI apply(Resource from) {
 				return from.getURI();
 			}
 		};
 		return Lists.transform(resources, resourceToURI);
 	}
 
 	public List<NodeType> transformNodeToNodeType(List<Node> nodes) {
 		final Function<Node, NodeType> nodeTypeFromNode = new Function<Node, NodeType>() {
 			public NodeType apply(Node from) {
 				return from.getNodeType();
 			}
 		};
 		return Lists.transform(nodes, nodeTypeFromNode);
 	}
 
 	public Iterator<CDOObject> transformEObjectToCDOObjects(
 			Iterator<EObject> eObjects) {
 		final Function<EObject, CDOObject> cdoObjectFromEObject = new Function<EObject, CDOObject>() {
 			public CDOObject apply(EObject from) {
 				return (CDOObject) from;
 			}
 		};
 		return Iterators.transform(eObjects, cdoObjectFromEObject);
 	}
 
 	/**
 	 * Transform a list of Value object, to only the value part of the Value
 	 * Object.
 	 * 
 	 * @param values
 	 * @return
 	 */
 	public List<BigDecimal> transformValueToBigDecimal(List<Value> values) {
 		final Function<Value, BigDecimal> valueToBigDecimal = new Function<Value, BigDecimal>() {
 			public BigDecimal apply(Value from) {
 				return new BigDecimal(from.getValue());
 			}
 		};
 		return Lists.transform(values, valueToBigDecimal);
 	}
 
 	public List<Double> transformBigDecimalToDouble(List<BigDecimal> values) {
 		final Function<BigDecimal, Double> valueToBigDecimal = new Function<BigDecimal, Double>() {
 			public Double apply(BigDecimal from) {
 				return from.doubleValue();
 			}
 		};
 		return Lists.transform(values, valueToBigDecimal);
 	}
 
 	public double[] transformValueToDoubleArray(List<Value> values) {
 		final Function<Value, Double> valueToDouble = new Function<Value, Double>() {
 			public Double apply(Value from) {
 				return from.getValue();
 			}
 		};
 		List<Double> doubles = Lists.transform(values, valueToDouble);
 		double[] doubleArray = new double[doubles.size()];
 		for (int i = 0; i < doubles.size(); i++) {
 			doubleArray[i] = doubles.get(i).doubleValue();
 		}
 		return doubleArray;
 	}
 
 	public List<Component> transformToComponents(
 			List<? extends EObject> components) {
 		final Function<EObject, Component> valueToDouble = new Function<EObject, Component>() {
 			public Component apply(EObject from) {
 				if (from instanceof Component) {
 					return (Component) from;
 				}
 				return null;
 			}
 		};
 		List<Component> result = Lists.transform(components, valueToDouble);
 		return result;
 	}
 
 	/**
 	 * Get all the day timestamps in the period.
 	 * 
 	 * @param dtr
 	 * @return
 	 */
 	public List<XMLGregorianCalendar> transformPeriodToDailyTimestamps(
 			DateTimeRange dtr) {
 
 		List<XMLGregorianCalendar> timeStamps = Lists.newArrayList();
 
 		final Calendar cal = GregorianCalendar.getInstance();
 		// Set the end time and count backwards, make the hour is the end hour.
 		// Optional set to day end, the UI should have done this already.
 		// this.setToDayEnd();
 
 		// BACKWARD, WILL USE THE END TIME STAMP WHICH IS 23:59:999h
 		// cal.setTime(dtr.getEnd().toGregorianCalendar().getTime());
 		// Date begin = dtr.getBegin().toGregorianCalendar().getTime();
 		// while (cal.getTime().after(begin)) {
 		// timeStamps.add(this.toXMLDate(cal.getTime()));
 		// cal.add(Calendar.DAY_OF_YEAR, -1);
 		// }
 
 		// FORWARD, WILL USE THE BEGIN TIME STAMP WHICH IS 00:00:000h
 		cal.setTime(dtr.getBegin().toGregorianCalendar().getTime());
 		Date end = dtr.getEnd().toGregorianCalendar().getTime();
 		while (cal.getTime().before(end)) {
 			timeStamps.add(this.toXMLDate(cal.getTime()));
 			cal.add(Calendar.DAY_OF_YEAR, 1);
 		}
 
 		return timeStamps;
 	}
 
 	/**
 	 * Get all the hourly timestamps in the period.
 	 * 
 	 * @param dtr
 	 * @return
 	 */
 	public List<XMLGregorianCalendar> transformPeriodToHourlyTimestamps(
 			DateTimeRange dtr) {
 
 		List<XMLGregorianCalendar> timeStamps = Lists.newArrayList();
 
 		final Calendar cal = GregorianCalendar.getInstance();
 		// Set the end time and count backwards, make the hour is the end hour.
 		// Optional set to day end, the UI should have done this already.
 		// this.setToDayEnd();
 
 		Date endTime = dtr.getEnd().toGregorianCalendar().getTime();
 
 		Date beginTime = dtr.getBegin().toGregorianCalendar().getTime();
 
 		cal.setTime(endTime);
 		setToFullHour(cal);
 
 		while (cal.getTime().compareTo(beginTime) >= 0) {
 			cal.add(Calendar.HOUR_OF_DAY, -1);
 			Date runTime = cal.getTime();
 			timeStamps.add(this.toXMLDate(runTime));
 		}
 
 		return timeStamps;
 	}
 
 	public double[] multiplyByHundredAndToArray(List<Double> values) {
 		final Function<Double, Double> valueToDouble = new Function<Double, Double>() {
 			public Double apply(Double from) {
 				return from * 100;
 			}
 		};
 		List<Double> doubles = Lists.transform(values, valueToDouble);
 		double[] doubleArray = new double[doubles.size()];
 		for (int i = 0; i < doubles.size(); i++) {
 			doubleArray[i] = doubles.get(i).doubleValue();
 		}
 		return doubleArray;
 	}
 
 	public List<Double> transformValueToDouble(List<Value> values) {
 		final Function<Value, Double> valueToDouble = new Function<Value, Double>() {
 			public Double apply(Value from) {
 				return from.getValue();
 			}
 		};
 		return Lists.transform(values, valueToDouble);
 	}
 
 	public List<Date> transformValueToDate(List<Value> values) {
 		final Function<Value, Date> valueToDouble = new Function<Value, Date>() {
 			public Date apply(Value from) {
 				return fromXMLDate(from.getTimeStamp());
 			}
 		};
 		return Lists.transform(values, valueToDouble);
 	}
 
 	public List<Date> transformXMLDateToDate(
 			Collection<XMLGregorianCalendar> dates) {
 		final Function<XMLGregorianCalendar, Date> valueToDouble = new Function<XMLGregorianCalendar, Date>() {
 			public Date apply(XMLGregorianCalendar from) {
 				return fromXMLDate(from);
 			}
 		};
 		return Lists.newArrayList(Iterables.transform(dates, valueToDouble));
 	}
 
 	public Date[] transformValueToDateArray(List<Value> values) {
 		final Function<Value, Date> valueToDouble = new Function<Value, Date>() {
 			public Date apply(Value from) {
 				return fromXMLDate(from.getTimeStamp());
 			}
 		};
 		List<Date> transform = Lists.transform(values, valueToDouble);
 		return transform.toArray(new Date[transform.size()]);
 	}
 
 	/**
 	 * Separate and Merge the date and value from a value collection into two
 	 * separate collections. if the Date is already in the date collection, we
 	 * re-use that index.
 	 * 
 	 * @param dates
 	 */
 	public List<Double> merge(List<Date> dates, List<Value> valuesToMerge) {
 
 		// should from with the dates list.
 		List<Double> doubles = Lists.newArrayListWithCapacity(dates.size());
 		for (int i = 0; i < dates.size(); i++) {
 			doubles.add(new Double(-1));
 		}
 
 		for (Value v : valuesToMerge) {
 			Date dateToMergeOrAdd = fromXMLDate(v.getTimeStamp());
 			int positionOf = positionOf(dates, dateToMergeOrAdd);
 			if (positionOf != -1) {
 				// store in the same position, the initial size should allow
 				// this.
 				doubles.add(positionOf, v.getValue());
 			} else {
 				dates.add(dateToMergeOrAdd);
 				double value = v.getValue();
 				doubles.add(value);
 			}
 		}
 		return doubles;
 	}
 
 	/**
 	 * Converts the detention period for {@link MetricRetentionRule}.
 	 * 
 	 * @param rule
 	 * @return
 	 */
 	public DateTimeRange getDTRForRetentionRule(MetricRetentionRule rule) {
 		DateTimeRange dtr = null;
 		switch (rule.getPeriod().getValue()) {
 		case MetricRetentionPeriod.ALWAYS_VALUE: {
 			// DTR is not set.
 		}
 			break;
 		case MetricRetentionPeriod.ONE_MONTH_VALUE: {
 			dtr = GenericsFactory.eINSTANCE.createDateTimeRange();
 			dtr.setBegin(toXMLDate(oneWeekAgo()));
 			dtr.setEnd(toXMLDate(todayAndNow()));
 		}
 			break;
 		case MetricRetentionPeriod.ONE_WEEK_VALUE: {
 		}
 			dtr = GenericsFactory.eINSTANCE.createDateTimeRange();
 			dtr.setBegin(toXMLDate(oneMonthAgo()));
 			dtr.setEnd(toXMLDate(todayAndNow()));
 
 			break;
 
 		}
 		return dtr;
 	}
 
 	/**
 	 * Get hourly timestamps in weekly chunks.
 	 * 
 	 * @param dtr
 	 * @return
 	 */
 	public Multimap<Integer, XMLGregorianCalendar> hourlyTimeStampsByWeekFor(
 			DateTimeRange dtr) {
 
 		List<XMLGregorianCalendar> tses = this
 				.transformPeriodToHourlyTimestamps(dtr);
 
 		Function<XMLGregorianCalendar, Integer> weekNumFunction = new Function<XMLGregorianCalendar, Integer>() {
 			Calendar cal = Calendar.getInstance();
 
 			public Integer apply(XMLGregorianCalendar from) {
 				// convert to a regular calendar to get the time.
 				cal.setTime(from.toGregorianCalendar().getTime());
 				return cal.get(Calendar.WEEK_OF_YEAR);
 			}
 		};
 		return Multimaps.index(tses, weekNumFunction);
 	}
 
 	public int positionOf(List<Date> dates, Date toCheckDate) {
 		int indexOf = dates.indexOf(toCheckDate);
 		return indexOf;
 	}
 
 	/**
 	 * Transform from a Double list to a double array.
 	 * 
 	 * @param values
 	 * @return
 	 */
 	public double[] transformToDoublePrimitiveArray(List<Double> values) {
 		final double[] doubles = new double[values.size()];
 		int i = 0;
 		for (final Double d : values) {
 			doubles[i] = d.doubleValue();
 			i++;
 		}
 		return doubles;
 	}
 
 	/**
 	 * look down the containment tree, and find the most recenrt date.
 	 * 
 	 * @param object
 	 * @return
 	 */
 	public long mostRecentContainedDated(CDOObject object) {
 
 		long ts = object.cdoRevision().getTimeStamp();
 
 		TreeIterator<EObject> eAllContents = object.eAllContents();
 		while (eAllContents.hasNext()) {
 			EObject eo = eAllContents.next();
 			if (eo.eContainer() != null) {
 				// We are contained, so we might have been updated.
 				if (eo instanceof CDOObject) {
 					long leafTS = ((CDOObject) eo).cdoRevision().getTimeStamp();
 					if (leafTS > ts) {
 						ts = leafTS;
 					}
 				}
 
 			}
 		}
 		return ts;
 	}
 
 	/**
 	 * All closure networks.
 	 * 
 	 * @param network
 	 * @return
 	 */
 	public List<Network> networksForOperator(Operator operator) {
 		final List<Network> networks = new ArrayList<Network>();
 
 		for (Network n : operator.getNetworks()) {
 			networks.addAll(networksForNetwork(n));
 		}
 		return networks;
 	}
 
 	public List<Network> networksForNetwork(Network network) {
 		final List<Network> networks = new ArrayList<Network>();
 		networks.add(network);
 		for (Network child : network.getNetworks()) {
 			networks.addAll(networksForNetwork(child));
 		}
 		return networks;
 	}
 
 	/**
 	 * All closure nodes.
 	 * 
 	 * @param network
 	 * @return
 	 */
 	public List<Node> nodesForNetwork(Network network) {
 
 		List<Node> nodes = Lists.newArrayList();
 		nodes.addAll(network.getNodes());
 		for (Network n : network.getNetworks()) {
 			nodes.addAll(nodesForNetwork(n));
 		}
 		return nodes;
 	}
 
 	public List<Component> componentsForOperator(Operator op) {
 
 		List<Component> components = Lists.newArrayList();
 		for (Network net : op.getNetworks()) {
 			List<Node> nodesForNetwork = this.nodesForNetwork(net);
 			for (Node n : nodesForNetwork) {
 				List<Component> componentsForNode = this.componentsForNode(n);
 				components.addAll(componentsForNode);
 			}
 		}
 		return components;
 	}
 
 	/**
 	 * All closure components.
 	 * 
 	 * @param n
 	 * @return
 	 */
 	public List<Component> componentsForNode(Node n) {
 		final List<Component> components = new ArrayList<Component>();
 
 		if (n.eIsSet(OperatorsPackage.Literals.NODE__NODE_TYPE)) {
 			for (Component c : n.getNodeType().getFunctions()) {
 				components.addAll(componentsForComponent(c));
 			}
 			for (Component c : n.getNodeType().getEquipments()) {
 				components.addAll(componentsForComponent(c));
 			}
 		}
 		return components;
 	}
 
 	public List<Component> componentsForComponent(Component c) {
 		final List<Component> components = new ArrayList<Component>();
 		components.add(c);
 		if (c instanceof com.netxforge.netxstudio.library.Function) {
 			com.netxforge.netxstudio.library.Function f = (com.netxforge.netxstudio.library.Function) c;
 			for (Component child : f.getFunctions()) {
 				components.addAll(componentsForComponent(child));
 			}
 		}
 
 		if (c instanceof Equipment) {
 			Equipment eq = (Equipment) c;
 			for (Component child : eq.getEquipments()) {
 				components.addAll(componentsForComponent(child));
 			}
 		}
 
 		return components;
 	}
 
 	/**
 	 * The component name. If the component is a Function, the name will be
 	 * returned. If the component is an Equipment, the code could be returned or
 	 * the name.
 	 * 
 	 * @param fromObject
 	 * @return
 	 */
 	public String componentName(Object fromObject) {
 		if (fromObject instanceof Equipment) {
 			String code = ((Equipment) fromObject).getEquipmentCode();
 			String name = ((Equipment) fromObject).getName();
 			StringBuilder sb = new StringBuilder();
 			if (code != null && code.length() > 0) {
 				sb.append(code + " ");
 			}
 			if (name != null && name.length() > 0) {
 				sb.append(name);
 			}
 			return sb.toString();
 
 		} else if (fromObject instanceof com.netxforge.netxstudio.library.Function) {
 			return ((com.netxforge.netxstudio.library.Function) fromObject)
 					.getName();
 		}
 		return null;
 	}
 
 	public List<NodeType> nodeTypesForResource(Resource operatorsResource) {
 		final List<NodeType> nodeTypes = new ArrayList<NodeType>();
 		for (EObject eo : operatorsResource.getContents()) {
 			if (eo instanceof Operator) {
 				Operator op = (Operator) eo;
 				for (Service service : op.getServices()) {
 					nodeTypes.addAll(nodeTypeForService(service));
 				}
 			}
 		}
 		return nodeTypes;
 	}
 
 	public List<NodeType> nodeTypeForService(Service service) {
 		final List<NodeType> nodeTypes = new ArrayList<NodeType>();
 		if (service instanceof RFSService) {
 			for (Node n : ((RFSService) service).getNodes()) {
 				nodeTypes.add(n.getNodeType());
 			}
 			for (Service subService : service.getServices()) {
 				nodeTypes.addAll(nodeTypeForService(subService));
 			}
 		}
 		return nodeTypes;
 	}
 
 	public static class CollectionForObjects<T> {
 
 		public List<T> collectionForObjects(List<EObject> objects) {
 
 			List<T> typedList = Lists.transform(objects,
 					new Function<EObject, T>() {
 
 						@SuppressWarnings("unchecked")
 						public T apply(EObject from) {
 							return (T) from;
 						}
 					});
 
 			return typedList;
 		}
 
 	}
 }
