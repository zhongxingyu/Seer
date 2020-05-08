 package storage.entities;
 
 
import javax.persistence.Entity;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
@Entity
public class StundenplanStunde extends DefaultEntity{
 
     @ManyToOne
     @JoinColumn(name="klasseId", 
                 insertable=false, updatable=false, 
                 nullable=false)
 	private Klasse klasse;
 
     @ManyToOne
     @JoinColumn(name="tagId", 
                 insertable=false, updatable=false, 
                 nullable=false)
 	private Tag tag;
     
     @ManyToOne
     @JoinColumn(name="stundeId", 
                 insertable=false, updatable=false, 
                 nullable=false)
 	private Stunde stunde;
     
     @ManyToOne
     @JoinColumn(name="lehrerId", 
                 insertable=false, updatable=false, 
                 nullable=false)
 	private Lehrer lehrer;
     
     @ManyToOne
     @JoinColumn(name="fachId", 
                 insertable=false, updatable=false, 
                 nullable=false)
 	private Unterrichtsfach unterrichtsfach;
     
     @ManyToOne
     @JoinColumn(name="raumId", 
                 insertable=false, updatable=false, 
                 nullable=false)
 	private Raum raum;
     
     @ManyToOne
     @JoinColumn(name="vertretungId", 
                 insertable=false, updatable=false, 
                 nullable=false)
 	private Vertretung vertretung;
 
 }
