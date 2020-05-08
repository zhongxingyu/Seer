 package fr.emse.clientadmin;
 
 import java.awt.Color;
 import java.awt.Point;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JTextPane;
 import javax.swing.ListModel;
 import javax.swing.event.MouseInputListener;
 
 import org.openstreetmap.gui.jmapviewer.Coordinate;
 import org.openstreetmap.gui.jmapviewer.JMapViewer;
 import org.openstreetmap.gui.jmapviewer.MapMarkerDot;
 import org.openstreetmap.gui.jmapviewer.MapPolygonImpl;
 import org.openstreetmap.gui.jmapviewer.interfaces.MapMarker;
 
 import com.cloudgarden.layout.AnchorConstraint;
 import com.cloudgarden.layout.AnchorLayout;
 
 import fr.emse.server.Itinerary;
 import fr.emse.server.Note;
 import fr.emse.server.SCoordinate;
 
 /**
  * This code was edited or generated using CloudGarden's Jigloo
  * SWT/Swing GUI Builder, which is free for non-commercial
  * use. If Jigloo is being used commercially (ie, by a corporation,
  * company or business for any purpose whatever) then you
  * should purchase a license for each developer using Jigloo.
  * Please visit www.cloudgarden.com for details.
  * Use of Jigloo implies acceptance of these licensing terms.
  * A COMMERCIAL LICENSE HAS NOT BEEN PURCHASED FOR
  * THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED
  * LEGALLY FOR ANY CORPORATE OR COMMERCIAL PURPOSE.
  */
 /**
  * Classe principale de l'interface graphique qui a pour rôle d'afficher la
  * carte et de répondre aux interactions de l'utilisateur
  * 
  * @author Antoine, Julien
  */
 public class MainSwingApp extends JFrame implements ActionListener,
 		MouseInputListener {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	// bouton pour créer une note
 	private JButton jButtonCreateNote;
 	// bonton pour créer un itinéraire
 	private JButton jButtonItinerary;
 	// bouton pour supprimer une note
 	private JButton jButtonRemove;
 	// objet liste graphique qui permet d'afficher les itinéraires en cours
 	private JList<String> jListItineraries;
 	// objet qui gère la carte géographique
 	private JMapViewer map;
 	// marqueur courant dans l'exécution de la classe
 	private MapMarker tmpMapmarker;
 	// champ de texte pour afficher les informations détaillées des notes et /
 	// ou itinéraires
 	private JTextPane jTextPaneInfo;
 
 	// itinéraire courant à la classe
 	private Itinerary currentItinerary;
 	// note courante à la classe
 	private Note currentNote;
 	// label 'liste des itinéraires'
 	private JLabel jLabel1;
 
 	/**
 	 * Auto-generated main method to display this JFrame
 	 */
 
 	public MainSwingApp() {
 		super();
 		Context.setState(State.NORMAL);
 
 		this.map = new JMapViewer();
 		map.addMouseListener(this);
 		initiateMainView();
 		initGUI();
 		if (map.getMapMarkerList().isEmpty()) {
 			map.setDisplayPositionByLatLon(45.430262484682125, 4.3890380859375,
 					11);
 		} else {
 			map.setDisplayToFitMapMarkers();
 			map.setZoom(9);
 		}
 
 		// Sign in
 		// new SignInDialog(this);
 	}
 
 	@SuppressWarnings("unchecked")
 	private void initGUI() {
 		try {
 			AnchorLayout thisLayout = new AnchorLayout();
 			getContentPane().setLayout(thisLayout);
 			this.setTitle("GeoNotes");
 			{
 				jTextPaneInfo = new JTextPane();
 				getContentPane().add(
 						jTextPaneInfo,
 						new AnchorConstraint(42, 302, 283, 49,
 								AnchorConstraint.ANCHOR_REL,
 								AnchorConstraint.ANCHOR_REL,
 								AnchorConstraint.ANCHOR_REL,
 								AnchorConstraint.ANCHOR_REL));
 				jTextPaneInfo.setText("");
 				jTextPaneInfo.setPreferredSize(new java.awt.Dimension(199, 87));
 			}
 			{
 				jButtonRemove = new JButton();
 				getContentPane().add(
 						jButtonRemove,
 						new AnchorConstraint(810, 961, 929, 902,
 								AnchorConstraint.ANCHOR_REL,
 								AnchorConstraint.ANCHOR_REL,
 								AnchorConstraint.ANCHOR_REL,
 								AnchorConstraint.ANCHOR_REL));
 				jButtonRemove.setText("X");
 				jButtonRemove.setPreferredSize(new java.awt.Dimension(46, 43));
 				jButtonRemove.addActionListener(this);
 				jButtonRemove.setVisible(false);
 			}
 			{
 				jLabel1 = new JLabel();
 				getContentPane().add(
 						jLabel1,
 						new AnchorConstraint(317, 301, 358, 48,
 								AnchorConstraint.ANCHOR_REL,
 								AnchorConstraint.ANCHOR_REL,
 								AnchorConstraint.ANCHOR_REL,
 								AnchorConstraint.ANCHOR_REL));
 				jLabel1.setText("Liste des itinéraires");
 				jLabel1.setPreferredSize(new java.awt.Dimension(200, 15));
 			}
 			{
 
 				List<Itinerary> itineraries = ClientAdmin.dataModel
 						.getItineraries();
 				ArrayList<String> itinerariesName = new ArrayList<String>();
 				if (itineraries != null) {
 					for (Itinerary itinerary : itineraries) {
 						itinerariesName.add(itinerary.getTitle());
 					}
 				}
 
 				@SuppressWarnings({ "rawtypes" })
 				ListModel jListItinerariesModel = new DefaultComboBoxModel(
 						itinerariesName.toArray());
 
 				jListItineraries = new JList<String>();
 				getContentPane().add(
 						jListItineraries,
 						new AnchorConstraint(388, 215, 767, 49,
 								AnchorConstraint.ANCHOR_REL,
 								AnchorConstraint.ANCHOR_REL,
 								AnchorConstraint.ANCHOR_REL,
 								AnchorConstraint.ANCHOR_REL));
 				jListItineraries.setModel(jListItinerariesModel);
 				jListItineraries.setPreferredSize(new java.awt.Dimension(116,
 						138));
 				// insertion d'un gestionnaire de click souris pour la liste
 				// graphique
 				MouseListener mouseListener = new MouseAdapter() {
 					public void mousePressed(MouseEvent mouseEvent) {
 						// lorsque l'on clique sur la liste, on ve récupérer
 						// l'élément cliqué
 						int index = jListItineraries.locationToIndex(mouseEvent
 								.getPoint());
 						if (index >= 0) {
 							// on obtient les informations de cet élément
 							// et on les affiche dans la bulle prévue à cet
 							// effet
 							Itinerary itinerary = ClientAdmin.adminBeanRemote
 									.getItineraries().get(index);
 							jTextPaneInfo.setText("Itinéraire: "
 									+ itinerary.getTitle() + "\n"
 									+ "Commentaires: "
 									+ itinerary.getComments() + "\n"
 									+ "Distance: "
 									+ itinerary.getDistanceString() + "\n"
 									+ "Dénivelé: "
 									+ itinerary.getDeniveleString() + "\n"
 									+ "Date: " + itinerary.getDateCreation());
 
 							String element = jListItineraries.getModel()
 									.getElementAt(index);
 							System.out.println("clicked on: "
 									+ element.toString());

							map.removeAllMapPolygons();
							drawMapItinerary(itinerary);
 						}
 					}
 				};
 				jListItineraries.addMouseListener(mouseListener);
 
 			}
 			{
 				jButtonItinerary = new JButton();
 				getContentPane().add(
 						jButtonItinerary,
 						new AnchorConstraint(831, 489, 913, 49,
 								AnchorConstraint.ANCHOR_REL,
 								AnchorConstraint.ANCHOR_REL,
 								AnchorConstraint.ANCHOR_REL,
 								AnchorConstraint.ANCHOR_REL));
 				jButtonItinerary.setText("Créer un itinéraire");
 				jButtonItinerary.setPreferredSize(new java.awt.Dimension(308,
 						30));
 				jButtonItinerary.addActionListener(this);
 			}
 			{
 
 				final JCheckBox scrollWrapEnabled = new JCheckBox(
 						"Scrollwrap enabled");
 				scrollWrapEnabled.addActionListener(new ActionListener() {
 					public void actionPerformed(ActionEvent e) {
 						map.setScrollWrapEnabled(scrollWrapEnabled.isSelected());
 					}
 				});
 
 				final JCheckBox showMapMarker = new JCheckBox(
 						"Map markers visible");
 				showMapMarker.setSelected(map.getMapMarkersVisible());
 				showMapMarker.addActionListener(new ActionListener() {
 
 					public void actionPerformed(ActionEvent e) {
 						map.setMapMarkerVisible(showMapMarker.isSelected());
 					}
 				});
 
 				getContentPane().add(
 						map,
 						new AnchorConstraint(42, 970, 767, 339,
 								AnchorConstraint.ANCHOR_REL,
 								AnchorConstraint.ANCHOR_REL,
 								AnchorConstraint.ANCHOR_REL,
 								AnchorConstraint.ANCHOR_REL));
 				map.setPreferredSize(new java.awt.Dimension(440, 300));
 
 			}
 			{
 				jButtonCreateNote = new JButton();
 				getContentPane().add(
 						jButtonCreateNote,
 						new AnchorConstraint(830, 870, 912, 515,
 								AnchorConstraint.ANCHOR_REL,
 								AnchorConstraint.ANCHOR_REL,
 								AnchorConstraint.ANCHOR_REL,
 								AnchorConstraint.ANCHOR_REL));
 				jButtonCreateNote.setText("Create note");
 				jButtonCreateNote.setPreferredSize(new java.awt.Dimension(278,
 						30));
 				jButtonCreateNote.addActionListener(this);
 			}
 
 			this.setSize(800, 400);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void initiateMainView() {
 		for (Note note : ClientAdmin.dataModel.getNotes()) {
 			System.out.println(note.getCoordinate().getLat() + " "
 					+ note.getCoordinate().getLon());
 			map.addMapMarker(new MapMarkerDot(note.getCoordinate().getLat(),
 					note.getCoordinate().getLon()));
 		}
 	}
 
 	// gestionnaire d'évenements de type 'bouton'
 	@Override
 	public void actionPerformed(ActionEvent ae) {
 		// si le bouton itinéraire est appuyé
 		if (ae.getSource() == jButtonItinerary) {
 			// si on est dans le contexte normal
 			if (Context.getState() == State.NORMAL) {
 				// alors on initialise la variable globale de l'itinéraire
 				// courant
 				currentItinerary = new Itinerary();
 				// on change le contexte pour le mode 'création d'itinéraire'
 				Context.setState(State.CREATE_ITINERARY);
 				// on change le texte du bouton par 'Finish itinerary'
 				jButtonItinerary.setText("Finish itinerary");
 				// si on est dans le mode édition de note
 			} else if (Context.getState() == State.EDIT_NOTE) {
 				System.out
 						.println("On ne peut pas crér un itineraire lors de l'édition d'un point");
 			} else {
 				// sinon ie si on est déjà dans le mode création d'itinéraire
 				// alors ouverture de la fenêtre pour renseigner les
 				// informations textuelles
 				new CreateItineraryDialog(currentItinerary, this);
 			}
 			// si on appuye sur le bouton 'Create Note'
 		} else if (ae.getSource() == jButtonCreateNote) {
 			// si on est dans le contexte normal
 			if (Context.getState() == State.NORMAL) {
 				// on se place dans le contexte de création d'une note
 				Context.setState(State.CREATE_NOTE);
 				// on modifie le nom du bouton en 'Put note on map'
 				jButtonCreateNote.setText("Put note on map");
 			}
 
 			// si on est déjà dans le mode d'édition
 			else if (Context.getState() == State.EDIT_NOTE) {
 				// on récupérer les coordonnées de la note qu'on veut éditer
 				SCoordinate coor = new SCoordinate(Context
 						.getCurrentMapMarker().getLat(), Context
 						.getCurrentMapMarker().getLon());
 				System.out.println(coor.toString());
 				// on récupère toutes les informations sur cette note depuis la
 				// base de données
 				Note note = ClientAdmin.dataModel.getNote(coor);
 				// on ouvre une nouvelle fenêtre pour modifier ses informations
 				new CreateNoteDialog(note, this);
 			}
 			// si on est déjà dans le mode de création de note
 			else if (Context.getState() == State.CREATE_NOTE) {
 				// on modifie le texte du boutton en 'Create Note'
 				jButtonCreateNote.setText("Create note");
 				// on retourne dans le contexte normal
 				Context.setState(State.NORMAL);
 			}
 			// si on appuye sur le bouton remove
 		} else if (ae.getSource() == jButtonRemove) {
 			// si on est bien dans le mode édition
 			if (Context.getState() == State.EDIT_NOTE) {
 				// on retire le point de la carte
 				map.removeMapMarker(tmpMapmarker);
 				// on récupère les coordonnées du points
 				SCoordinate coor = new SCoordinate(Context
 						.getCurrentMapMarker().getLat(), Context
 						.getCurrentMapMarker().getLon());
 				// on retire la note de la base de données
 				ClientAdmin.dataModel.removeNote(coor);
 				Context.setCurrentMapMarker(null);
 				// on retourne au contexte normal
 				deselectNote();
 				System.out.println("Note supprimée");
 			}
 		}
 	}
 
 	/**
 	 * méthode qui gère les clics de la souris sur la carte
 	 */
 	@Override
 	public void mousePressed(MouseEvent e) {
 		// on récupère l'endroit où a eu lieu le clic souris
 		Point mousePoint = e.getPoint();
 		// s'il y a eu un click gauche
 		if (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON1) {
 			// on récupère le marqueur sur lequel on a cliqué (null) si on a
 			// cliqué dans le vide de la carte
 			MapMarker mapMarker = getMapMarker(mousePoint);
 			// si le contexte est normal et qu'un marqueur a été sélectionné
 			if (Context.getState() == State.NORMAL && mapMarker != null) {
 				// on change l'indice du marqueur courant dans le contexte
 				Context.setCurrentIndex(map.getMapMarkerList().indexOf(
 						mapMarker));
 				// on change le du marqueur courant en celui que l'on vient de
 				// sélectionner
 				Context.setCurrentMapMarker(mapMarker);
 				// on modifie la couleur de ce marqueur en rouge
 				tmpMapmarker = new MapMarkerDot(Color.RED, mapMarker.getLat(),
 						mapMarker.getLon());
 				map.getMapMarkerList().set(Context.getCurrentIndex(),
 						tmpMapmarker);
 				System.out.println(mapMarker.toString() + " is clicked");
 
 				// on récupère les coordonnées de ce marqueur
 				SCoordinate coor = new SCoordinate(Context
 						.getCurrentMapMarker().getLat(), Context
 						.getCurrentMapMarker().getLon());
 				// on change la note courante par les valeurs récupérées dans la
 				// base de données grâce aux coordonnées du marqueur
 				currentNote = ClientAdmin.dataModel.getNote(coor);
 				// on change le texte du bouton de création par 'Edit Note'
 				jButtonCreateNote.setText("Edit note");
 				// on rend le bouton 'remove' visible
 				jButtonRemove.setVisible(true);
 
 				// on met à jour la partie descriptive de note
 				jTextPaneInfo.setText("Note: " + currentNote.getId() + "\n"
 						+ currentNote.getCoordinate().toString() + "\n"
 						+ "Category: " + currentNote.getCategory() + "\n"
 						+ "Comments: " + currentNote.getComments() + "\n"
 						+ "Created: " + currentNote.getDateCreation());
 
 				// on change le contexte pour passer en mode édition
 				Context.setState(State.EDIT_NOTE);
 			}
 			// si le contexte est en mode édition
 			else if (Context.getState() == State.EDIT_NOTE) {
 				// on récupère les coordonnés du clic et les anciennes
 				// coordonnées du marqueur sélectionné
 				SCoordinate oldCoor = new SCoordinate(Context
 						.getCurrentMapMarker().getLat(), Context
 						.getCurrentMapMarker().getLon());
 				SCoordinate newCoor = new SCoordinate(map.getPosition(
 						mousePoint).getLat(), map.getPosition(mousePoint)
 						.getLon());
 				MapMarker tmpMapmarker = new MapMarkerDot(newCoor.getLat(),
 						newCoor.getLon());
 				// on met à jour le marqueur sélectionné avec les coordonées du
 				// nouveau marqueur
 				map.getMapMarkerList().set(Context.getCurrentIndex(),
 						tmpMapmarker);
 				// on récupère la note correspondante au marqueur
 				Note note = ClientAdmin.dataModel.getNote(oldCoor);
 				// on la modifie pour changer les coordonnées
 				note.setCoordinate(newCoor);
 				// on met à jour la note dans la base de données et dans la Map
 				// de DataModel
 				ClientAdmin.dataModel.updateNote(oldCoor, note);
 				// on repasse en contexte normal
 				deselectNote();
 			}
 			// si le contexte est en mode de création
 			else if (Context.getState() == State.CREATE_NOTE) {
 				// si on clique sur le vide de la carte
 				if (mapMarker == null) {
 					// on récupère la position du clic sur la carte
 					SCoordinate coor = new SCoordinate(map.getPosition(
 							mousePoint).getLat(), map.getPosition(mousePoint)
 							.getLon());
 					Context.setCurrentMapMarker(new MapMarkerDot(coor.getLat(),
 							coor.getLon()));
 					// on ajoute un marqueur à l'endroid du clic
 					map.addMapMarker(Context.getCurrentMapMarker());
 					// on ouvre une fenêtre pour renseigner des informations
 					// textuelles de la note
 					new CreateNoteDialog(coor, 0, this);
 				} else {
 					// si cherche à cliquer sur un marqueur existant
 					System.out.println("Note existante à cet emplacement");
 					// on revient au contexte normal
 					Context.setState(State.NORMAL);
 					jButtonCreateNote.setText("Create note");
 				}
 			}
 
 			// si on est en mode création d'itinéraire
 			else if (Context.getState() == State.CREATE_ITINERARY) {
 				// si on a bien cliqué sur un marqueur
 				if (mapMarker != null) {
 					System.out.println("Note : " + mapMarker.toString());
 					// on récupère la note correspondante
 					Note noteToAdd = ClientAdmin.dataModel.getNearestNodeFrom(
 							mapMarker.getLat(), mapMarker.getLon());
 					// on ajoute cette note à l'itinéraire courant
 					currentItinerary.appendNote(noteToAdd);
 					// on met à jour les traits entre les marqueurs rattachés à
 					// un itinéraire
 					updateMap();
 				}
 			}
 			// dans tous les cas, on update la carte à la fin
 			map.repaint();
 		}
 	}
 
 	/**
 	 * méthode qui récupère un marqueur lorsque l'on clique dessus avec la
 	 * souris
 	 * 
 	 * @param mousePoint
 	 * @return
 	 */
 	private MapMarker getMapMarker(Point mousePoint) {
 		int X = mousePoint.x + 3;
 		int Y = mousePoint.y + 3;
 
 		// on récupère la liste des marqueurs de la carte
 		List<MapMarker> ar = map.getMapMarkerList();
 		Iterator<MapMarker> i = ar.iterator();
 
 		double radCircle = 10;
 		MapMarker mapMarker = null;
 		// tant que la distance au marqueur courant est supérieure à la valeur 8
 		while (i.hasNext() && radCircle >= 8) {
 
 			mapMarker = (MapMarker) i.next();
 
 			Point MarkerPosition = map.getMapPosition(mapMarker.getLat(),
 					mapMarker.getLon());
 			if (MarkerPosition != null) {
 
 				int centerX = MarkerPosition.x;
 				int centerY = MarkerPosition.y;
 
 				// System.out.println(map.getPosition(p).getLat()+":"+map.getPosition(p).getLon());
 				// calculate the radius from the touch to the center of the dot
 				radCircle = Math
 						.sqrt((((centerX - X) * (centerX - X)) + (centerY - Y)
 								* (centerY - Y)));
 
 			}
 		}
 		// if the radius is smaller then 23 (radius of a ball is 5), then it
 		// must be on the dot
 		if (radCircle < 8) {
 			return mapMarker;
 		}
 		return null;
 	}
 
 	/**
 	 * méthode qui se charge de repasser en contexte normal
 	 */
 	public void deselectNote() {
 		// on change le tete du bouton création en 'Create note'
 		jButtonCreateNote.setText("Create note");
 		// on efface le bouton de suppression
 		jButtonRemove.setVisible(false);
 		// on efface les informations dans le texte descriptif
 		jTextPaneInfo.setText("");
 		// on se place dans le contexte en status normal
 		Context.setState(State.NORMAL);
 		// on refresh la carte
 		updateMap();
 	}
 
 	/**
 	 * méthode qui se charge de supprimer le marqueur courant lorsqu'on annule
 	 * la création d'une note
 	 */
 	public void cancelCreateNote() {
 		if (Context.getCurrentMapMarker() != null) {
 			map.removeMapMarker(Context.getCurrentMapMarker());
 			Context.setCurrentMapMarker(null);
 		}
 		deselectNote();
 	}
 
 	/**
 	 * méthode qui se charge de rétablir le marqueur courant lorsque l'on annule
 	 * l'édition de celui-ci
 	 */
 	public void cancelUpdate() {
 		map.getMapMarkerList().set(
 				Context.getCurrentIndex(),
 				new MapMarkerDot(Context.getCurrentMapMarker().getLat(),
 						Context.getCurrentMapMarker().getLon()));
 		deselectNote();
 	}
 
 	/**
 	 * méthode qui est appelée par la fenêtre de création lorsque la mise à jour
 	 * d'une note est finie afin de mettre à jour la carte et de se replacer en
 	 * contexte normal
 	 */
 	public void updateNotefinished() {
 		map.getMapMarkerList().set(Context.getCurrentIndex(),
 				Context.getCurrentMapMarker());
 		deselectNote();
 		map.getMapMarkerList().set(Context.getCurrentIndex(),
 				Context.getCurrentMapMarker());
 		jButtonCreateNote.setText("Create note");
 		jButtonRemove.setVisible(false);
 		Context.setState(State.NORMAL);
 		map.repaint();
 	}
 
 	/**
 	 * méthode qui est appelée par la fenêtre de création de note lorsque la
 	 * création d'une note est finie afin de de se replacer en contexte normal
 	 */
 	public void createNotefinished() {
 		jButtonCreateNote.setText("Create note");
 		Context.setState(State.NORMAL);
 	}
 
 	/**
 	 * méthode qui est appelée par la fenêtre de création d'un itinéraire
 	 * lorsque ça création est terminée afin de rebasculer en contexte normal
 	 */
 	public void createItineraryFinished() {
 		ClientAdmin.dataModel.addItinerary(currentItinerary);
 		System.out.println("Itinéraire ajouté !");
 		updateListItineraries();
 		currentItinerary = null;
 		Context.setState(State.NORMAL);
 		jButtonItinerary.setText("Create itinerary");
 	}
 
 	/**
 	 * méthode qui met à jour les lignes entre les marqueurs pour tous les
 	 * itinéraires
 	 */
 	private void updateMap() {
 		map.removeAllMapPolygons();
 
 		if (currentItinerary != null) {
			drawMapItinerary(currentItinerary);
 		}
 		List<Itinerary> itineraries = ClientAdmin.dataModel.getItineraries();
 		for (Itinerary itinerary : itineraries) {
			drawMapItinerary(itinerary);
 		}
 	}
 
 	/**
 	 * méthode qui met à jour les lignes entre les marqueurs pour l'itinéraire
	 * indiqué
 	 * 
 	 * @param itinerary
 	 */
	private void drawMapItinerary(Itinerary itinerary) {
 		Coordinate coord1 = null;
 		Coordinate coord2 = null;
 
 		for (Note note : itinerary.getNotes()) {
 			coord1 = coord2;
 			coord2 = new Coordinate(note.getCoordinate().getLat(), note
 					.getCoordinate().getLon());
 
 			if (coord1 != null) {
 				// petite subtilité, il faut donner 3 points pour dessiner une
 				// arête, en redoublant le deuxième
 				List<Coordinate> route = new ArrayList<Coordinate>(
 						Arrays.asList(coord1, coord2, coord2));
 				map.addMapPolygon(new MapPolygonImpl(route));
 			}
 		}
 	}
 
 	/**
 	 * méthode qui met à jour la liste des itinéraires
 	 */
 	@SuppressWarnings("unchecked")
 	private void updateListItineraries() {
 		List<Itinerary> itineraries = ClientAdmin.dataModel.getItineraries();
 		ArrayList<String> itinerariesName = new ArrayList<String>();
 		for (Itinerary itinerary : itineraries) {
 			itinerariesName.add(itinerary.getTitle());
 		}
 
 		@SuppressWarnings({ "rawtypes" })
 		ListModel jListItinerariesModel = new DefaultComboBoxModel(
 				itinerariesName.toArray());
 		jListItineraries.setModel(jListItinerariesModel);
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent e) {
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent e) {
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent e) {
 	}
 
 	@Override
 	public void mouseExited(MouseEvent e) {
 	}
 
 	@Override
 	public void mouseDragged(MouseEvent e) {
 	}
 
 	@Override
 	public void mouseMoved(MouseEvent e) {
 	}
 
 }
