 package org.calculator.models;
 
 import javax.persistence.Column;
 import javax.persistence.DiscriminatorColumn;
 import javax.persistence.DiscriminatorType;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.Inheritance;
 import javax.persistence.InheritanceType;
 import javax.persistence.Table;
 
 /**
  * The super class of Scenario JPA entity which can be a JarScenario,
  * WebScenario, ...
  * 
  * @author khalil.amdouni
  * 
  */
 @Entity
 @Table(name = "CORE_SCENARIOS")
 @Inheritance(strategy = InheritanceType.SINGLE_TABLE)
 @DiscriminatorColumn(name = "SCENARIO_CATEGORY", discriminatorType = DiscriminatorType.STRING)
 public class Scenario extends AbstractModel {
 
 	@Id
 	@Column(name = "ID")
 	private long id;
 
 	@Column(name = "NAME")
 	private String name;
 
 	@Column(name = "DESCRIPTION")
 	private String description;
 
 	@Column(name = "SEQUENCE")
 	private String sequence;
 
 	public long getId() {
 		return id;
 	}
 
 	public void setId(long id) {
 		this.id = id;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	public String getSequence() {
 		return sequence;
 	}
 
 	public void setSequence(String sequence) {
 		this.sequence = sequence;
 	}
 
 }
