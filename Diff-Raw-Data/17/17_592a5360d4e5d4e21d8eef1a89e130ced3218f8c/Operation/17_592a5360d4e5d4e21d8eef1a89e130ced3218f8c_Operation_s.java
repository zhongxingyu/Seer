 package models;
 
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.EnumType;
 import javax.persistence.Enumerated;
 import javax.persistence.JoinColumn;
 import javax.persistence.JoinTable;
 import javax.persistence.ManyToMany;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 
 import play.data.validation.Required;
 import play.db.jpa.JPA;
 import play.db.jpa.Model;
 
 @Entity
 @Table(name = "OPERATION")
 public class Operation extends Model {
 
 	@Required
 	public Date date;
 
 	@Required
 	public String libelle;
 
 	@Required
 	@Enumerated(EnumType.STRING)
 	public ETypeOperation type;
 
 	@Required
 	@Column(columnDefinition = "Decimal(10,2)")
 	public Float montant;
 
 	public String numero;
 
 	public String detail;
 
 	@Required
 	@Enumerated(EnumType.STRING)
 	public EEtatOperation etat = EEtatOperation.NONPOINTEE;
 
 	@ManyToMany
 	@JoinTable(name = "OPERATION_TAGS", joinColumns = @JoinColumn(name = "OPERATION_ID"), inverseJoinColumns = @JoinColumn(name = "TAG_ID"))
 	public List<Tag> tags = new ArrayList<Tag>();
 
 	@Required
 	@ManyToOne
 	public Compte compte;
 
 	@ManyToOne
 	public Tiers tiers;
 
 	public Float getMontantFromDatabase() {
		return ((BigDecimal) JPA.newEntityManager().createNativeQuery("SELECT montant FROM Operation WHERE id=?").setParameter(1, this.id).getSingleResult()).floatValue();
 	}
 }
