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
 
 /**
  * @author alexandru-m-g
  *
  */
 @Entity
 @Table(name = "resource_config_entry")
 @SequenceGenerator(name = "resource_config_entry_seq", sequenceName = "resource_config_entry_seq")
 public class ResourceConfigEntry extends AbstractConfigEntry {
 
 	@Id
 	@GeneratedValue(strategy = GenerationType.AUTO, generator = "resource_config_entry_seq")
 	@Column(name = "id", nullable = false)
 	private long id;
 
 	@ManyToOne
 	@JoinColumn(name = "resource_configuration_id", nullable=false)
 	@ForeignKey(name = "fk_resource_config_map_to_source")
 	private ResourceConfiguration  parentConfiguration;
 
 	public ResourceConfigEntry(){}
 
 	public ResourceConfigEntry(final String key, final String value) {
 		super(key, value);
 	}
 
 	public ResourceConfiguration getParentConfiguration() {
 		return this.parentConfiguration;
 	}
 
 	public void setParentConfiguration(final ResourceConfiguration parentConfiguration) {
 		this.parentConfiguration = parentConfiguration;
 	}
 
 	public long getId() {
 		return this.id;
 	}
 
 	public void setId(final long id) {
 		this.id = id;
 	}
 
 
 
 }
