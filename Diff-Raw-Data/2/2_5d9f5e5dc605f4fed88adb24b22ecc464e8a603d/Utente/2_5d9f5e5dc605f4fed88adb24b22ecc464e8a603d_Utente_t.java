 package modelloUtenti;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.persistence.UniqueConstraint;
 
 import javax.persistence.*;
 
 import modelloTreni.Prenotazione;
 	
 	@Entity
 	@Table(name = "utenti",uniqueConstraints = @UniqueConstraint(columnNames = "email"))
 	public class Utente implements Serializable {
 
 		private static final long serialVersionUID = 1L;
 
 		@Id
 	    @GeneratedValue(strategy = GenerationType.AUTO)
 	    private int id;
 
 		private String nome;
 		
 		private String cognome;
 
 		private String codiceFiscale;
 		
 		@Enumerated(value = EnumType.STRING) 
 		private TipoUtente tipo;
 		
 		
 		private String email;
 		
 		@OneToMany
 		private List<Prenotazione> prenotazioni=new ArrayList<Prenotazione>();
 		
 		Utente(){
 			
 		}
 
 		Utente(String nome, String cognome, String codiceFiscale,TipoUtente tipo,String email) {
 			super();
 			this.nome = nome;
 			this.cognome = cognome;
 			this.codiceFiscale = codiceFiscale;
 			this.tipo = tipo;
 			this.email = email;
 		}
 		
 		
 		Utente(int id,String nome, String cognome, String codiceFiscale,TipoUtente tipo,String email) {
 			super();
 			this.nome = nome;
 			this.cognome = cognome;
 			this.codiceFiscale = codiceFiscale;
 			this.tipo = tipo;
 			this.email = email;
 			this.id = id;
 		}
 		
 		String getEmail() {
 			return email;
 		}
 
 		void setEmail(String email) {
 			this.email = email;
 
 		}
 			
 		
 		 public TipoUtente getTipo() {
 			return tipo;
 		}
 
 		void setTipo(TipoUtente tipo) {
 			this.tipo = tipo;
 		}
 
		public int getId() {
 			return id;
 		}
 
 
 		 void setId(int id) {
 			this.id = id;
 		}
 
 
 
 		public void addPrenotazione(Prenotazione p) {
 			prenotazioni.add(p);
 		}
 
 		void removePrenotazione(Prenotazione p) {
 			prenotazioni.remove(p);
 		}
 
 
 		public String getNome() {
 			return nome;
 		}
 
 		void setNome(String nome) {
 			this.nome = nome;
 		}
 
 		public String getCognome() {
 			return cognome;
 		}
 
 		void setCognome(String cognome) {
 			this.cognome = cognome;
 		}
 
 		public String getCodiceFiscale() {
 			return codiceFiscale;
 		}
 
 		void setCodiceFiscale(String codiceFiscale) {
 			this.codiceFiscale = codiceFiscale;
 		}
 
 		public ArrayList<Prenotazione> getPrenotazioni() {
 			return new ArrayList<Prenotazione>(prenotazioni);
 		}
 
 		@Override
 		public String toString() {
 			return "Utente [id=" + id + ", nome=" + nome + ", cognome="
 					+ cognome + ", codiceFiscale=" + codiceFiscale + ", tipo="
 					+ tipo + ", email=" + email + ", prenotazioni="
 					+ prenotazioni + "]";
 		}
 		
 }
