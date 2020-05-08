 package org.kalibro.core.persistence.record;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.List;
 
 import javax.persistence.*;
 
 import org.eclipse.persistence.annotations.CascadeOnDelete;
 import org.kalibro.Granularity;
 import org.kalibro.MetricResult;
 import org.kalibro.Module;
 import org.kalibro.ModuleResult;
 import org.kalibro.dto.ModuleResultDto;
 
 /**
  * Java Persistence API entity for {@link ModuleResult}.
  * 
  * @author Carlos Morais
  */
 @Entity(name = "ModuleResult")
 @Table(name = "\"MODULE_RESULT\"")
 public class ModuleResultRecord extends ModuleResultDto {
 
 	private static ModuleResultRecord parentRecord(ModuleResult moduleResult) {
 		if (!moduleResult.hasParent())
 			return null;
 		return new ModuleResultRecord(moduleResult.getParent().getId());
 	}
 
 	@SuppressWarnings("unused" /* used by JPA */)
 	@ManyToOne(fetch = FetchType.LAZY, optional = false)
 	@JoinColumn(name = "\"processing\"", nullable = false, referencedColumnName = "\"id\"")
 	private ProcessingRecord processing;
 
 	@Id
 	@GeneratedValue
 	@Column(name = "\"id\"", nullable = false)
 	private Long id;
 
 	@ElementCollection(fetch = FetchType.EAGER)
 	@OrderColumn(name = "\"index\"", nullable = false)
 	private List<String> moduleName;
 
 	@Column(name = "\"module_granularity\"", nullable = false)
 	private String moduleGranularity;
 
 	@Column(name = "\"grade\"")
 	private Long grade;
 
 	@ManyToOne(fetch = FetchType.LAZY)
 	@SuppressWarnings("unused" /* used by JPA */)
 	@JoinColumn(name = "\"parent\"", referencedColumnName = "\"id\"")
 	private ModuleResultRecord parent;
 
 	@CascadeOnDelete
 	@SuppressWarnings("unused" /* used by JPA */)
 	@OneToMany(mappedBy = "parent", orphanRemoval = true)
 	private Collection<ModuleResultRecord> children;
 
 	@CascadeOnDelete
 	@OneToMany(cascade = CascadeType.ALL, mappedBy = "moduleResult", orphanRemoval = true)
 	private Collection<MetricResultRecord> metricResults;
 
 	public ModuleResultRecord() {
 		super();
 	}
 
 	public ModuleResultRecord(Long id) {
 		this.id = id;
 	}
 
 	public ModuleResultRecord(ModuleResult moduleResult) {
 		this(moduleResult, null);
 	}
 
 	public ModuleResultRecord(ModuleResult moduleResult, Long processingId) {
 		this(moduleResult, parentRecord(moduleResult), processingId);
 	}
 
 	public ModuleResultRecord(ModuleResult moduleResult, ModuleResultRecord parent, Long processingId) {
 		this(moduleResult.getModule(), parent, processingId);
 		id = moduleResult.getId();
 		grade = Double.doubleToLongBits(moduleResult.getGrade());
 		setMetricResults(moduleResult.getMetricResults());
 	}
 
 	public ModuleResultRecord(Module module, ModuleResultRecord parent, Long processingId) {
 		this.parent = parent;
 		processing = new ProcessingRecord(processingId);
 		moduleName = Arrays.asList(module.getName());
 		moduleGranularity = module.getGranularity().name();
 	}
 
 	private void setMetricResults(Collection<MetricResult> results) {
 		metricResults = new ArrayList<MetricResultRecord>();
 		for (MetricResult metricResult : results)
 			metricResults.add(new MetricResultRecord(metricResult, this));
 	}
 
 	@Override
 	public Long id() {
 		return id;
 	}
 
 	@Override
 	public Module module() {
 		return new Module(Granularity.valueOf(moduleGranularity), moduleName.toArray(new String[0]));
 	}
 
 	@Override
 	public Double grade() {
		return Double.longBitsToDouble(grade);
 	}
 }
