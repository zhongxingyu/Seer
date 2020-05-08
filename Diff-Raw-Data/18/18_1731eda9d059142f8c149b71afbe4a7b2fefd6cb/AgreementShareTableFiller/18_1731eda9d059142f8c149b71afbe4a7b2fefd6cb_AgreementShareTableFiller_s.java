  package businessLayer;
 
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.Inheritance;
 import javax.persistence.InheritanceType;
 import javax.persistence.NamedQuery;
 
 
 @NamedQuery(name = "AgreementShareTableFiller.findAll", query = "SELECT f FROM AgreementShareTableFiller f ") 
 @Entity
 @Inheritance(strategy=InheritanceType.TABLE_PER_CLASS)
 
 public abstract class AgreementShareTableFiller {
 	public abstract void fill(AgreementShareTable table);
 	@Override
 	public abstract int hashCode();
 
 	@Override
 	public abstract boolean equals(Object obj);
 	
 	@Id private int id;
 	
 }
