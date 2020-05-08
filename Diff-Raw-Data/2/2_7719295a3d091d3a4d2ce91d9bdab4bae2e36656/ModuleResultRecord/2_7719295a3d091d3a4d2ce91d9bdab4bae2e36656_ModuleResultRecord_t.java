 package org.kalibro.core.persistence.record;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import javax.persistence.*;
 
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
 @Table(name = "\"module_result\"")
 public class ModuleResultRecord extends ModuleResultDto {
 
 	private static final String TOKEN = "###";
 
 	public static String persistedName(String[] moduleName) {
 		String name = "";
 		for (String namePart : moduleName)
 			name += TOKEN + namePart;
 		if (moduleName.length > 0)
 			name = name.substring(TOKEN.length());
 		return name;
 	}
 
 	@Id
 	@GeneratedValue(strategy = GenerationType.SEQUENCE)
 	@Column(name = "\"id\"", nullable = false, unique = true)
 	private Long id;
 
 	@Column(name = "\"processing\"", nullable = false)
 	private Long processing;
 
 	@Column(name = "\"module_name\"", nullable = false)
 	private String moduleName;
 
 	@Column(name = "\"module_granularity\"", nullable = false)
 	private String moduleGranularity;
 
 	@Column(name = "\"grade\"")
 	private Long grade;
 
 	@Column(name = "\"parent\"")
 	private Long parent;
 
 	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "moduleResult")
 	private Collection<MetricResultRecord> metricResults;
 
 	public ModuleResultRecord() {
 		super();
 	}
 
 	public ModuleResultRecord(ModuleResult moduleResult) {
 		this(moduleResult, null);
 	}
 
 	public ModuleResultRecord(ModuleResult moduleResult, Long processingId) {
 		this(moduleResult, null, processingId);
		if (moduleResult.hasParent())
			parent = moduleResult.getParent().getId();
 	}
 
 	public ModuleResultRecord(ModuleResult moduleResult, Long parentId, Long processingId) {
 		this(moduleResult.getModule(), parentId, processingId);
 		id = moduleResult.getId();
 		grade = Double.doubleToLongBits(moduleResult.getGrade());
 		setMetricResults(moduleResult.getMetricResults());
 	}
 
 	public ModuleResultRecord(Module module, Long parentId, Long processingId) {
 		processing = processingId;
 		moduleName = persistedName(module.getName());
 		moduleGranularity = module.getGranularity().name();
 		parent = parentId;
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
 		return new Module(Granularity.valueOf(moduleGranularity), moduleName.split(TOKEN));
 	}
 
 	@Override
 	public Double grade() {
 		return grade == null ? Double.NaN : Double.longBitsToDouble(grade);
 	}
 
 	@Override
 	public Long parentId() {
 		return parent;
 	}
 }
