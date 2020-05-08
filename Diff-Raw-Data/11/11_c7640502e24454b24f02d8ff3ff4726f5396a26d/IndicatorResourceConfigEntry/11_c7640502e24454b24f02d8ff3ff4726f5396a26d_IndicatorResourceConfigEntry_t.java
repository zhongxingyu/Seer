 /**
  *
  */
 package org.ocha.hdx.persistence.entity.configs;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.SequenceGenerator;
 import javax.persistence.Table;
 
 import org.hibernate.annotations.ForeignKey;
 import org.ocha.hdx.persistence.entity.curateddata.IndicatorType;
 import org.ocha.hdx.persistence.entity.curateddata.Source;
 
 /**
  * @author alexandru-m-g
  *
  */
 @Entity
 @Table(name = "indicator_resource_config_entry")
 @SequenceGenerator(name = "indicator_resource_config_entry_seq", sequenceName = "indicator_resource_config_entry_seq")
 public class IndicatorResourceConfigEntry extends AbstractConfigEntry{
 
 	@Id
 	@GeneratedValue(strategy = GenerationType.AUTO, generator = "indicator_resource_config_entry_seq")
 	@Column(name = "id", nullable = false)
 	private long id;
 
 	@ManyToOne
 	@JoinColumn(name = "resource_configuration_id", nullable=false)
	@ForeignKey(name = "fk_resource_config_map_to_parent")
 	private ResourceConfiguration  parentConfiguration;
 
 	@ManyToOne
 	@JoinColumn(name = "source_id", nullable=false)
 	@ForeignKey(name = "fk_resource_config_map_to_source")
 	private Source source;
 
 	@ManyToOne
 	@JoinColumn(name = "indicator_type_id", nullable=false)
 	@ForeignKey(name = "fk_resource_config_map_to_indicator_type")
 	private IndicatorType indicatorType;
 
 
 	public IndicatorResourceConfigEntry() {}
 
 	public IndicatorResourceConfigEntry(final String key, final String value,
 			final Source source, final IndicatorType indicatorType) {
 		super(key, value);
 		this.source = source;
 		this.indicatorType = indicatorType;
 	}
 
 
 	public long getId() {
 		return this.id;
 	}
 
 	public void setId(final long id) {
 		this.id = id;
 	}
 
 	public Source getSource() {
 		return this.source;
 	}
 
 	public void setSource(final Source source) {
 		this.source = source;
 	}
 
 	public IndicatorType getIndicatorType() {
 		return this.indicatorType;
 	}
 
 	public void setIndicatorType(final IndicatorType indicatorType) {
 		this.indicatorType = indicatorType;
 	}
 
 	public ResourceConfiguration getParentConfiguration() {
 		return this.parentConfiguration;
 	}
 
 	public void setParentConfiguration(final ResourceConfiguration parentConfiguration) {
 		this.parentConfiguration = parentConfiguration;
 	}
 
 
 
 }
