 package org.kalibro.core.persistence.record;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import javax.persistence.*;
 
 import org.eclipse.persistence.annotations.CascadeOnDelete;
 import org.kalibro.MetricConfiguration;
 import org.kalibro.MetricResult;
 import org.kalibro.dto.MetricResultDto;
 
 /**
  * Java Persistence API entity for {@link MetricResult}.
  * 
  * @author Carlos Morais
  */
 @Entity(name = "MetricResult")
 @Table(name = "\"METRIC_RESULT\"")
 public class MetricResultRecord extends MetricResultDto {
 
 	@SuppressWarnings("unused" /* used by JPA */)
 	@ManyToOne(fetch = FetchType.LAZY, optional = false)
 	@JoinColumn(name = "\"module_result\"", nullable = false, referencedColumnName = "\"id\"")
 	private ModuleResultRecord moduleResult;
 
 	@ManyToOne(fetch = FetchType.LAZY, optional = false)
 	@JoinColumn(name = "\"configuration\"", nullable = false, referencedColumnName = "\"id\"")
 	private MetricConfigurationSnapshotRecord configuration;
 
 	@Id
 	@GeneratedValue
 	@Column(name = "\"id\"", nullable = false)
 	private Long id;
 
 	@Column(name = "\"value\"", nullable = false)
 	private Long value;
 
 	@CascadeOnDelete
 	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
 	@JoinColumn(name = "\"error\"", referencedColumnName = "\"id\"")
 	private ThrowableRecord error;
 
 	@CascadeOnDelete
 	@OneToMany(cascade = CascadeType.ALL, mappedBy = "metricResult", orphanRemoval = true)
 	private Collection<DescendantResultRecord> descendantResults;
 
 	public MetricResultRecord() {
 		super();
 	}
 
 	public MetricResultRecord(MetricResult metricResult) {
 		this(metricResult, null);
 	}
 
 	public MetricResultRecord(MetricResult metricResult, ModuleResultRecord moduleResult) {
 		this.moduleResult = moduleResult;
 		configuration = new MetricConfigurationSnapshotRecord(metricResult.getConfiguration().getId());
 		id = metricResult.getId();
 		value = Double.doubleToLongBits(metricResult.getValue());
 		error = metricResult.hasError() ? new ThrowableRecord(metricResult.getError()) : null;
 		setDescendantResults(metricResult.getDescendantResults());
 	}
 
 	private void setDescendantResults(List<Double> descendantResults) {
 		this.descendantResults = new ArrayList<DescendantResultRecord>();
 		for (Double descendantResult : descendantResults)
			this.descendantResults.add(new DescendantResultRecord(descendantResult, this));
 	}
 
 	@Override
 	public Long id() {
 		return id;
 	}
 
 	@Override
 	public MetricConfiguration configuration() {
 		return configuration.convert();
 	}
 
 	@Override
 	public Double value() {
 		return Double.longBitsToDouble(value);
 	}
 
 	@Override
 	public Throwable error() {
 		return error == null ? null : error.convert();
 	}
 }
