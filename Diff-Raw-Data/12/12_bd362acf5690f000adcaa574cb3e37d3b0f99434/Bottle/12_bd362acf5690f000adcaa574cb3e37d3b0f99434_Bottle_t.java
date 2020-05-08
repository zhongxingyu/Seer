 package de.atomfrede.mate.domain.entities.bottle;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.Table;
 
 import org.hibernate.annotations.GenericGenerator;
 import org.hibernate.annotations.Parameter;
 
 import de.atomfrede.mate.domain.entities.AbstractEntity;
 
 @Entity
 @Table(name = "bottle")
 public class Bottle extends AbstractEntity {
 
 	@GenericGenerator(name = "BottleIdGenerator", strategy = "org.hibernate.id.MultipleHiLoPerTableGenerator", parameters = {
 			@Parameter(name = "table", value = "IdentityGenerator"),
 			@Parameter(name = "primary_key_column", value = "sequence_name"),
 			@Parameter(name = "primary_key_value", value = "Bottle"),
 			@Parameter(name = "value_column", value = "next_hi_value"),
 			@Parameter(name = "primary_key_length", value = "100"),
 			@Parameter(name = "max_lo", value = "1000") })
 	@Id
 	@GeneratedValue(strategy = GenerationType.TABLE, generator = "BottleIdGenerator")
 	protected Long id;
 
 	@Override
 	public Long getId() {
 		return id;
 	}
 
	@Column(name = "consumed", columnDefinition = "BIT", length = 1)
 	protected boolean consumed;
 
 	public boolean isConsumed() {
 		return consumed;
 	}
 
 	public void consume() {
 		this.consumed = true;
 	}
 
 }
