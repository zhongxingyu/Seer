 package org.softwaresynthesis.mytalk.server.abook;
 
 import java.util.HashSet;
 import java.util.Set;
 
 /**
  * Rappresentazione dell'utente del sistema mytalk
  *
  * @author 	Andrea Meneghinello
  * @version	1.0
  */
 public class UserData implements IUserData 
 {
 	private Long id;
 	private Set<IAddressBookEntry> addressBook;
 	private String mail;
 	private String password;
 	private String question;
 	private String answer;
 	private String name;
 	private String surname;
 	private String path;
 	
 	/**
 	 * Crea un utente privo di dati
 	 */
 	public UserData()
 	{
 		this.addressBook = new HashSet<IAddressBookEntry>();
 	}
 	
 	/**
 	 * Crea un utente con un particolare
 	 * identificativo, che però verrà ignorato
 	 * nelle operazioni di insert nel database
 	 * 
 	 * @param 	identifier	{@link Long} che identifica
 	 * 						un utente
 	 */
 	public UserData(Long identifier)
 	{
 		this();
 		this.setId(identifier);
 	}
 	
 	/**
 	 * Restituisce l'istanza sottoforma di stringa
 	 * JSON in modo che possa essere utilizzata
 	 * nella parte client
 	 * 
 	 * @return	{@link String} in formato JSON
 	 * 			dell'istanza
 	 */
 	@Override
 	public String toJson() 
 	{
 		String result = "{\"name\":\"" + this.getName() + "\"";
 		result += ", \"surname\":\"" + this.getSurname() + "\"";
 		result += ", \"email\":\"" + this.getMail() + "\"";
 		result += ", \"picturePath\":\"" + this.getPath() + "\"}";
 		return result;
 	}
 
 	/**
 	 * Restituisce l'identificativo univoco
 	 * dell'utente del sistema mytalk
 	 * 
 	 * @return	identificativo dell'utente di
 	 * 			tipo {@link Long}
 	 */
 	@Override
 	public Long getId() 
 	{
 		return this.id;
 	}
 	
 	/**
 	 * Imposta l'identificativo univoco di un
 	 * utente
 	 * 
 	 * @param 	identifier	{@link Long} che identifica
 	 * 						un utente
 	 */
 	protected void setId(Long identifier)
 	{
 		this.id = identifier;
 	}
 
 	/**
 	 * Restituisce l'indirizzo e-mail con cui
 	 * si è registrato un utente nel sistema
 	 * mytalk
 	 * 
 	 * @return	{@link String} rappresentante
 	 * 			l'indirizzo e-mail dell'utente
 	 */
 	@Override
 	public String getMail() 
 	{
 		return this.mail;
 	}
 
 	/**
 	 * Imposta l'indirizzo e-mail con cui
 	 * l'utente si vuole registrare nel 
 	 * sistema mytalk
 	 * 
 	 * @param 	eMail	{@link String} con
 	 * 					l'e-mail con cui vuole
 	 * 					registrarsi l'utente
 	 */
 	@Override
 	public void setMail(String eMail) 
 	{
 		this.mail = eMail;
 	}
 
 	/**
 	 * Restituisce la password, crittografata
 	 * secondo la strategia {@link org.softwaresynthesis.mytalk.server.authentication.ISecurityStrategy},
 	 * con l'utente accede al sistema
 	 * 
 	 * @return	{@link String} con la password
 	 * 			dell'utente
 	 */
 	@Override
 	public String getPassword() 
 	{
 		return password;
 	}
 
 	/**
 	 * Imposta la password che l'utente utilizzerà
 	 * per accedere al sistema mytalk
 	 * 
 	 * @param 	password	{@link String} con la
 	 * 						password scelta dall'utente
 	 */
 	@Override
 	public void setPassword(String password)
 	{
 		this.password = password;
 	}
 
 	/**
 	 * Restituisce la domanda segreta che l'utente
 	 * ha impostato per il recupero della password
 	 * 
 	 * @return	{@link String} con la domanda segreta
 	 */
 	@Override
 	public String getQuestion() 
 	{
 		return this.question;
 	}
 
 	/**
 	 * Imposta la domanda segreta utilizzata dall'utente
 	 * per il recupero della propria password
 	 * 
 	 * @param 	question	{@link String} con la domanda
 	 * 						segreta scelta dall'utente
 	 */
 	@Override
 	public void setQuestion(String question) 
 	{
 		this.question = question;
 	}
 
 	/**
 	 * Restituisce la risposta alla domanda segreta
 	 * per il recupero della password di accesso
 	 * al sistema mytalk
 	 * 
 	 * @return	{@link String} con la risposta alla
 	 * 			domanda segreta
 	 */
 	@Override
 	public String getAnswer() 
 	{
 		return this.answer;
 	}
 
 	/**
 	 * Imposta la risposta alla domanda segreta
 	 * per il recupero della password di accesso
 	 * al sistema mytalk
 	 * 
 	 * @param 	answer	{@link String} con la
 	 * 					risposta alla domanda segreta
 	 * 					per il recupero della password
 	 */
 	@Override
 	public void setAnswer(String answer) 
 	{
 		this.answer = answer;
 	}
 
 	/**
 	 * Restituisce il nome dell'utente
 	 * 
 	 * @return	{@link String} con il nome dell'utente
 	 */
 	@Override
 	public String getName()
 	{
 		return this.name;
 	}
 
 	/**
 	 * Imposta il nome dell'utente
 	 * 
 	 * @param 	name {@link String} con il nome dell'utente
 	 */
 	@Override
 	public void setName(String name)
 	{
 		this.name = name;
 	}
 
 	/**
 	 * Restituisce il cognome dell'utente
 	 * 
 	 * @return	{@link String} con il cognome dell'utente
 	 */
 	@Override
 	public String getSurname() 
 	{
 		return this.surname;
 	}
 
 	/**
 	 * Imposta il cognome dell'utente
 	 * 
 	 * @param 	surname	{@link String} con il cognome
 	 * 					dell'utnete
 	 */
 	@Override
 	public void setSurname(String surname)
 	{
 		this.surname = surname;
 	}
 
 	/**
 	 * Restituisce il link all'immagine profilo scelta
 	 * dall'utente
 	 * 
 	 * @return	{@link String} con il percorso realativo
 	 * 			all'immagine profilo dell'utente
 	 */
 	@Override
 	public String getPath() 
 	{
 		return this.path;
 	}
 
 	/**
 	 * Imposta il percorso dell'immagine profilo scelta
 	 * dall'utente
 	 * 
 	 * @param 	path	{@link String} con il percorso
 	 * 					dove sarà salvata nel server
 	 * 					l'immagine profilo
 	 */
 	@Override
 	public void setPath(String path)
 	{
 		this.path = path;
 	}
 	
 	/**
 	 * Restituisce la rubrica dell'utente
 	 * 
 	 * @return	{@link Set} con la rubrica
 	 * 			dell'utente
 	 */
 	@Override
 	public Set<IAddressBookEntry> getAddressBook()
 	{
 		return this.addressBook;
 	}
 	
 	/**
 	 * Inserisce la rubrica di un contatto
 	 * 
 	 * param	addressBook	{@link Set} con la rubrica
 	 * 						utente
 	 */
 	@Override
 	public void setAddressBook(Set<IAddressBookEntry> addressBook)
 	{
 		this.addressBook = addressBook;
 	}
 	
 	/**
 	 * Inserisce un nuovo contatto nella rubrica
 	 * dell'utente
 	 * 
 	 * @param 	entry	{@link AddressBookEntry}
 	 * 					con il nuovo contatto da
 	 * 					aggiungere
 	 */
 	@Override
 	public void addAddressBookEntry(IAddressBookEntry entry)
 	{
 		this.addressBook.add(entry);
 		entry.setOwner(this);
 	}
 	
 	/**
 	 * Rimove un contatto dalla rubrica
 	 * 
 	 * @param	entry	{@link AddressBookEntry} da rimuovere
 	 */
 	public void removeAddressBookEntry(IAddressBookEntry entry)
 	{
 		this.addressBook.remove(entry);
		entry.setOwner(null);
 	}
 	
 	/**
 	 * Verifica se due istanze sono uguali
 	 * 
 	 * @param	obj	{@link Object} con cui effettuare
 	 * 				confronto
 	 * @return 	true se le due istanze sono uguali,
 	 * 			false altrimenti
 	 */
 	@Override
 	public boolean equals(Object obj)
 	{
 		boolean result = false;
 		UserData user = null;
 		if (obj instanceof UserData)
 		{
 			user = (UserData)obj;
 			result = this.mail.equals(user.mail);
 		}
 		return result;
 	}
 	
 	/**
 	 * Restituisce l'istanza nel formato {@link String}
 	 * 
 	 * @return	{@link String} rappresentante l'istanza
 	 */
 	@Override
 	public String toString()
 	{
 		return String.format("UserData[id: %s, mail: %s", this.id.toString(), this.mail);
 	}
 }
