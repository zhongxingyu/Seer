 package org.kalibro.core.persistence.database.entities;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import javax.persistence.*;
 
 import org.eclipse.persistence.annotations.PrimaryKey;
 import org.kalibro.core.model.*;
 import org.kalibro.core.model.enums.Statistic;
 import org.kalibro.core.util.DataTransferObject;
 
 @Entity(name = "MetricConfiguration")
 @PrimaryKey(columns = {@Column(name = "configurationName"), @Column(name = "metricName")})
 public class MetricConfigurationRecord implements DataTransferObject<MetricConfiguration> {
 
	@ManyToOne(optional = false)
 	@JoinColumn(name = "configurationName", nullable = false, referencedColumnName = "name")
 	@SuppressWarnings("unused" /* used by JPA */)
 	private ConfigurationRecord configuration;
 
 	@Column(name = "metricName", nullable = false)
 	@SuppressWarnings("unused" /* used by JPA */)
 	private String metricName;
 
 	@ManyToOne
 	@JoinColumns({
 		@JoinColumn(insertable = false, name = "metricName", referencedColumnName = "name", updatable = false),
 		@JoinColumn(name = "metricOrigin", referencedColumnName = "origin")})
 	private NativeMetricRecord nativeMetric;
 
 	@OneToOne(cascade = CascadeType.ALL, mappedBy = "metricConfiguration", orphanRemoval = true)
 	@JoinColumns({
 		@JoinColumn(insertable = false, name = "metricName", referencedColumnName = "name", updatable = false),
 		@JoinColumn(name = "configurationName", referencedColumnName = "configurationName")})
 	private CompoundMetricRecord compoundMetric;
 
 	@Column(nullable = false)
 	private String code;
 
 	@Column(nullable = false)
 	private Long weight;
 
 	@Column(nullable = false)
 	private String aggregationForm;
 
 	@OneToMany(cascade = CascadeType.ALL, mappedBy = "metricConfiguration", orphanRemoval = true)
 	private Collection<RangeRecord> ranges;
 
 	public MetricConfigurationRecord() {
 		super();
 	}
 
 	public MetricConfigurationRecord(MetricConfiguration metricConfiguration, ConfigurationRecord configuration) {
 		this.configuration = configuration;
 		initializeMetric(metricConfiguration);
 		code = metricConfiguration.getCode();
 		weight = Double.doubleToLongBits(metricConfiguration.getWeight());
 		aggregationForm = metricConfiguration.getAggregationForm().name();
 		initializeRanges(metricConfiguration);
 	}
 
 	private void initializeMetric(MetricConfiguration metricConfiguration) {
 		Metric metric = metricConfiguration.getMetric();
 		metricName = metric.getName();
 		if (metric.isCompound())
 			compoundMetric = new CompoundMetricRecord((CompoundMetric) metric, this);
 		else
 			nativeMetric = new NativeMetricRecord((NativeMetric) metric);
 	}
 
 	private void initializeRanges(MetricConfiguration metricConfiguration) {
 		ranges = new ArrayList<RangeRecord>();
 		for (Range range : metricConfiguration.getRanges())
 			ranges.add(new RangeRecord(range, this));
 	}
 
 	@Override
 	public MetricConfiguration convert() {
 		MetricConfiguration metricConfiguration = new MetricConfiguration(convertMetric());
 		metricConfiguration.setCode(code);
 		metricConfiguration.setWeight(Double.longBitsToDouble(weight));
 		metricConfiguration.setAggregationForm(Statistic.valueOf(aggregationForm));
 		convertRanges(metricConfiguration);
 		return metricConfiguration;
 	}
 
 	private Metric convertMetric() {
 		return (nativeMetric == null) ? compoundMetric.convert() : nativeMetric.convert();
 	}
 
 	private void convertRanges(MetricConfiguration metricConfiguration) {
 		for (RangeRecord range : ranges)
 			metricConfiguration.addRange(range.convert());
 	}
 }
