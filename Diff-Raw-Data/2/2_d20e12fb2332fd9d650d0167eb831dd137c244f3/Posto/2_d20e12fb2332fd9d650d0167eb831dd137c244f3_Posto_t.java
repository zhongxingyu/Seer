 package modelloTreni;
 
 import java.io.Serializable;
 
 import javax.persistence.*;
 
 import org.hibernate.annotations.OnDelete;
 import org.hibernate.annotations.OnDeleteAction;
 
 
 @Entity
 @Table(name = "posti")
 public class Posto implements Serializable{
 	
     /**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	@Id
     @GeneratedValue(strategy = GenerationType.AUTO)
     private int id;
     
     @Enumerated(EnumType.STRING)
 	private ClassePosto classe;
     
     @ManyToOne
 	private Prenotazione prenotazione;
     
 	private boolean prenotato;
 	
     @ManyToOne //per ader faccio cosi
     @OnDelete(action = OnDeleteAction.CASCADE)
 	private IstanzaTreno istanzaTreno;
     
     Posto(){
     	
     }
 
 	public Posto(ClassePosto classe, IstanzaTreno i) {
 		super();
 		this.classe = classe;
 //		this.id=id;
 		this.istanzaTreno = i;
 		this.prenotato = false;
 	}
 	
 	public Integer getId() {
 		return id;
 	}
 
 	void setId(int id) {
 		this.id = id;
 	}
 
 	public Prenotazione getPrenotazione() {
 		return prenotazione;
 	}
 
	public void setPrenotazione(Prenotazione prenotazione) {
 		this.prenotazione = prenotazione;
 	}
 	
 	public ClassePosto getClasse(){
 		return classe;
 	}
 
 
 
 	public IstanzaTreno getIstanzaTreno() {
 		return istanzaTreno;
 	}
 
 	boolean isPrenotato() {
 		return prenotato;
 	}
 	
 	void prenota() {
 		prenotato=true;
 	}
 
 	@Override
 	public String toString() {
 		String s = "Posto: " + String.valueOf(id);
 		s += "\tClasse: " + classe + " \tPrenotato: " + prenotato;
 		if (prenotazione != null) {
 			s += "\tda: " + prenotazione.getUtente().getNome();
 		}
 		s += "\n";
 		return s;
 	}
 
 }
