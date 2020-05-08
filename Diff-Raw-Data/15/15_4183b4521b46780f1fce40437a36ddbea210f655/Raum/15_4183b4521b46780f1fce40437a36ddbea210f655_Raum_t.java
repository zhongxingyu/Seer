 package storage.entities;
 
 import java.util.List;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.OneToMany;
 
 import org.hibernate.annotations.IndexColumn;
 
 @Entity
 public class Raum extends DefaultEntity {
 
 	private String bezeichnung;
 
 	public String getBezeichnung() {
 		return this.bezeichnung;
 	}
 
 	public void setBezeichnung(final String bezeichnung) {
 		this.bezeichnung = bezeichnung;
 	}
 
     @OneToMany(cascade={CascadeType.ALL})
     @JoinColumn(name="raumId")
     @IndexColumn(name="indx")
     private List<StundenplanStunde> stundenPlanStunde;
 
 }
