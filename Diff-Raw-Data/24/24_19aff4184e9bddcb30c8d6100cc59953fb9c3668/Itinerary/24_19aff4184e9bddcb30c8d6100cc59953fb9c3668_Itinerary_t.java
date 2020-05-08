 package fr.emse.server;
 
 import java.io.Serializable;
 import java.text.DecimalFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.ManyToMany;
 
 @Entity
 public class Itinerary implements Serializable {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 8148007240281677876L;
 	@Id
 	@GeneratedValue(strategy = GenerationType.IDENTITY)
 	Integer id;
 	String title;
	@ManyToMany(mappedBy = "itineraries", cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
 	List<Note> notes;
 	double distance;
 	int deniveleTotal;
 	String comments;
 	int nbUsed;
 	String dateCreation;
 
 	/**
 	 * Constructeur vides
 	 */
 	public Itinerary() {
 		this.id = -1;
 		this.notes = new ArrayList<Note>();
 		this.title = "";
 		this.comments = "";
 		this.nbUsed = 0;
 		this.deniveleTotal = 0;
 		this.distance = 0;
 
 		// Date
 		Calendar currentDate = Calendar.getInstance();
 		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
 		this.dateCreation = formatter.format(currentDate.getTime());
 	}
 
 	/**
 	 * Constructeur
 	 * 
 	 * @param notes
 	 *            Liste de notes représentant le parcours
 	 * @param title
 	 *            Titre du parcours
 	 * @param comments
 	 *            Commentaires sur le parcours
 	 */
 	public Itinerary(List<Note> notes, String title, String comments) {
 		super();
 		this.notes = notes;
 		this.title = title;
 		this.comments = comments;
 		this.nbUsed = 0;
 
 		// Computes denivele and distance
 		this.deniveleTotal = 0;
 		this.distance = 0;
 		Note previousNote = notes.get(0);
 
 		boolean isFirst = true;
 		for (Note note : notes) {
 			if (isFirst) {
 				isFirst = false;
 			} else {
 				this.deniveleTotal += Math.abs(note.getHeight()
 						- previousNote.getHeight());
 				this.distance += distance(note.getCoordinate(),
 						previousNote.getCoordinate());
 			}
 
 			previousNote = note;
 			note.addItinerary(this);
 		}
 
 		// Date
 		Calendar currentDate = Calendar.getInstance();
 		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
 		this.dateCreation = formatter.format(currentDate.getTime());
 	}
 
 	/**
 	 * Calcule la distance entre deux coordonnées
 	 * 
 	 * @param A
 	 *            Coordonnées du premier point
 	 * @param B
 	 *            Coordonnées du deuxième point
 	 * @return Distance entre les deux coordonnées
 	 */
 	private double distance(SCoordinate A, SCoordinate B) {
 		double R = 6371;
 
 		// Convert to radiant
 		double latA = A.getLat() * Math.PI / 180;
 		double longA = A.getLon() * Math.PI / 180;
 		double latB = B.getLat() * Math.PI / 180;
 		double longB = B.getLon() * Math.PI / 180;
 
 		return R
 				* Math.acos(Math.sin(latA) * Math.sin(latB) + Math.cos(latA)
 						* Math.cos(latB) * Math.cos(longA - longB));
 	}
 
 	/**
 	 * Renvoie la liste des notes représentant le parcours
 	 * 
 	 * @return
 	 */
 	public List<Note> getNotes() {
 		return notes;
 	}
 
 	/**
 	 * Modifie la liste des notes représentant le parcours
 	 * 
 	 * @param notes
 	 *            Nouvelle liste de notes
 	 */
 	public void setNotes(List<Note> notes) {
 		this.notes = notes;
 	}
 
 	/**
 	 * Renvoie le titre du parcours
 	 * 
 	 * @return titre du parcours
 	 */
 	public String getTitle() {
 		return title;
 	}
 
 	public void setTitle(String title) {
 		this.title = title;
 	}
 
 	public String getComments() {
 		return comments;
 	}
 
 	public void setComments(String comments) {
 		this.comments = comments;
 	}
 
 	/**
 	 * Renvoie l'id du parcours
 	 * 
 	 * @return
 	 */
 	public int getId() {
 		return id;
 	}
 
 	/**
 	 * Renvoie la distance du parcours
 	 * 
 	 * @return Distance du parcours
 	 */
 	public double getDistance() {
 		return distance;
 	}
 
 	/**
 	 * Renvoie l'information de la distance sous forme de chaîne de caractères
 	 * 
 	 * @return
 	 */
 	public String getDistanceString() {
 		DecimalFormat dFormat = new DecimalFormat("0.00");
 		return dFormat.format(distance) + " km";
 	}
 
 	/**
 	 * Renvoie le dénivelé total du parcours
 	 * 
 	 * @return Dénivelé du parcours
 	 */
 	public int getDeniveleTotal() {
 		return deniveleTotal;
 	}
 
 	/**
 	 * Renvoie l'information du dénivelé sous forme de chaîne de caractères
 	 * 
 	 * @return
 	 */
 	public String getDeniveleString() {
 		return deniveleTotal + " m";
 	}
 
 	/**
 	 * Renvoie le nombre de fois que le parcours a été utilisé
 	 * 
 	 * @return
 	 */
 	public int getNbUsed() {
 		return nbUsed;
 	}
 
 	/**
 	 * Renvoie la date de création du parcours
 	 * 
 	 * @return
 	 */
 	public String getDateCreation() {
 		return dateCreation;
 	}
 
 	/**
 	 * Ajout d'une note en fin de parcours
 	 * 
 	 * @param newNote
 	 *            Nouvelle note à ajouter
 	 */
 	public void appendNote(Note newNote) {
 		if (notes.size() > 0) {
 			Note lastNote = notes.get(notes.size() - 1);
 
 			distance += distance(newNote.getCoordinate(),
 					lastNote.getCoordinate());
 			deniveleTotal += Math.abs(newNote.getHeight()
 					- lastNote.getHeight());
 		}
 
 		newNote.addItinerary(this);
 		notes.add(newNote);
 	}
 
 	/**
 	 * Mets à jour
 	 * 
 	 * @param coor
 	 * @param newNote
 	 */
 	public void updateNote(SCoordinate coor, Note newNote) {
 		int i = 0;
 		for (Note note : notes) {
 			if (note.getCoordinate().getLat() == coor.getLat()
 					&& note.getCoordinate().getLon() == coor.getLon()) {
 				notes.set(i, newNote);
 			}
 			i++;
 		}
 	}
 
 	/**
 	 * Supprime la note de l'itinéraire
 	 * 
 	 * @param coor
 	 *            Coordonnées de la note à supprimer
 	 */
 	public void removeNote(SCoordinate coor) {
 		int i = 0;
 		for (Note note : notes) {
 			if (note.getCoordinate().getLat() == coor.getLat()
 					&& note.getCoordinate().getLon() == coor.getLon()) {
 				notes.remove(i);
 			}
 			i++;
 		}
 	}
 
 	/**
 	 * Mets à jour les valeurs de distance et de dénivelé
 	 */
 	public void updateGeometry() {
 		Note previousNote = notes.get(0);
 		this.distance = 0;
 		this.deniveleTotal = 0;
 
 		boolean isFirst = true;
 		for (Note note : notes) {
 			if (isFirst) {
 				isFirst = false;
 			} else {
 				this.deniveleTotal += Math.abs(note.getHeight()
 						- previousNote.getHeight());
 				this.distance += distance(note.getCoordinate(),
 						previousNote.getCoordinate());
 			}
 		}
 	}
 
 	/**
 	 * Augmente le nombre de fois que le parcours a été sélectionné
 	 */
 	public void isUsed() {
 		nbUsed++;
 	}
 }
