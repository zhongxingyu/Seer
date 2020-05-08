 package persistence;
 
 import java.awt.geom.Point2D;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import petrinet.INode;
 import petrinet.Petrinet;
 import petrinet.Renews;
 import transformation.ITransformation;
 import transformation.Rule;
 import transformation.TransformationComponent;
 import engine.attribute.NodeLayoutAttribute;
 import engine.handler.RuleNet;
 import engine.ihandler.IPetrinetPersistence;
 import engine.ihandler.IRulePersistence;
 import exceptions.EngineException;
 import exceptions.ShowAsWarningException;
 import gui.PopUp;
 
 /**
  * This utility class provides converting methods which are used by persistence
  * class to convert from and to JAXB-classes.
  * 
  * */
 public class Converter {
 
 	private Converter() {
 		// utilitiy class
 	}
 
 	/**
 	 * Value for attribute {@link Net#getType() type} of {@link Net xmlNet}
 	 */
 	private static final String PETRINET_IDENT = "petrinet";
 	/**
 	 * Value for attribute {@link Net#getType() type} of {@link Net xmlNet}
 	 */
 	private static final String RULE_IDENT = "rule";
 
 	/**
 	 * Converts a logical {@link Petrinet petrinet} object into the equivalent
 	 * Pnml object tree, which can the be marshalled into a file.
 	 * 
 	 * @param petrinet
 	 *            logical petrinet
 	 * @param layout
 	 *            layout information in the following format:
 	 *            <ul>
 	 *            <li>key: id of node as String</li>
 	 *            <li>value: a list of Strings consistent of: [x, y, red, green,
 	 *            blue] all as Strings. Where x and y are position in pixels and
 	 *            red, green, blue are between 0 and 255
 	 *            </ul>
 	 * @param nodeSize
 	 *            the size of the nodes in pixels. This is important due to
 	 *            collision detection when loading the petrinet.
 	 * @return
 	 */
 	static public Pnml convertPetrinetToPnml(Petrinet petrinet,
 			Map<String, String[]> layout, double nodeSize) {
 		// PNML node
 		Pnml pnml = new Pnml();
 		pnml.setNodeSize(nodeSize);
 		pnml.setType(PETRINET_IDENT);
 		pnml.net = new ArrayList<Net>();
 
 		// Net node
 		Net xmlnet = new Net();
 		xmlnet.setId(String.valueOf(petrinet.getId()));
 		pnml.net.add(xmlnet);
 
 		// Page node
 		Page page = new Page();
 		xmlnet.setPage(page);
 
 		// Places
 		Set<petrinet.Place> set = petrinet.getAllPlaces();
 		List<Place> places = new ArrayList<Place>();
 		page.setPlace(places);
 		for (petrinet.Place place : set) {
 			addPlaceToPnml(layout, places, place);
 		}
 
 		// Transitions
 		Set<petrinet.Transition> transis = petrinet.getAllTransitions();
 		List<Transition> Tlist = new ArrayList<Transition>();
 		for (petrinet.Transition t : transis) {
 			addTransitionToPnml(layout, Tlist, t);
 		}
 		page.setTransition(Tlist);
 
 		// Arcs
 		Set<petrinet.Arc> arcs = petrinet.getAllArcs();
 		List<Arc> newArcs = new ArrayList<Arc>();
 		for (petrinet.Arc arc : arcs) {
 			addArcToPnml(places, Tlist, newArcs, arc);
 		}
 		page.setArc(newArcs);
 		return pnml;
 	}
 
 	/**
 	 * Similar to {@link Converter#addPlaceToPnml(Map, List, petrinet.Place)}
 	 * 
 	 * @param places
 	 * @param transitions
 	 * @param pnmlArcs
 	 * @param logicalArc
 	 */
 	private static void addArcToPnml(List<Place> places,
 			List<Transition> transitions, List<Arc> pnmlArcs,
 			petrinet.Arc logicalArc) {
 		// Arc and ID
 		Arc pnmlArc = new Arc();
 		pnmlArc.setId(String.valueOf(logicalArc.getId()));
 
 		// End of Arc -- Place
 		String arcEnd = null;
 		for (Place place : places) {
 			if (place.getId().equals(
 					String.valueOf(logicalArc.getEnd().getId()))) {
 				arcEnd = place.getId();
 				break;
 			}
 		}
 
 		// End of Arc -- Transition
 		if (arcEnd == null) {
 			for (Transition t : transitions) {
 				if (t.getId().equals(
 						String.valueOf(logicalArc.getEnd().getId()))) {
 					arcEnd = t.getId();
 					break;
 				}
 			}
 		}
 		pnmlArc.setTarget(arcEnd);
 
 		// Start of Arc -- Transition
 		String arcStart = null;
 		for (Place p : places) {
 			if (p.getId().equals(String.valueOf(logicalArc.getStart().getId()))) {
 				arcStart = p.getId();
 				break;
 			}
 		}
 
 		// Start of Arc -- Place
 		if (arcStart == null) {
 			for (Transition t : transitions) {
 				if (t.getId().equals(
 						String.valueOf(logicalArc.getStart().getId()))) {
 					arcStart = t.getId();
 					break;
 				}
 			}
 		}
 		pnmlArc.setSource(arcStart);
 
 		// Text
 		Inscription inscription = new Inscription();
 		inscription.setText(logicalArc.getName());
 		pnmlArc.setInscription(inscription);
 
 		// Add to List
 		pnmlArcs.add(pnmlArc);
 	}
 
 	/**
 	 * Similar to Converter#addPlaceToPnml(Map, List, petrinet.Place)
 	 * 
 	 * @param layout
 	 * @param transitionsList
 	 * @param logicalTransition
 	 */
 	private static void addTransitionToPnml(Map<String, String[]> layout,
 			List<Transition> transitionsList,
 			petrinet.Transition logicalTransition) {
 		// Transition and ID
 		Transition pnmlTransition = new Transition();
 		pnmlTransition.setId(String.valueOf(logicalTransition.getId()));
 
 		// Name
 		TransitionName name = new TransitionName();
 		name.setText(logicalTransition.getName());
 		pnmlTransition.setTransitionName(name);
 
 		// Graphics -- Position
 		Graphics graphics = new Graphics();
 		Position position = new Position();
 		position.setX(layout.get(pnmlTransition.getId())[0]);
 		position.setY(layout.get(pnmlTransition.getId())[1]);
 		List<Position> positionsList = new ArrayList<Position>();
 		positionsList.add(position);
 		graphics.setPosition(positionsList);
 
 		pnmlTransition.setGraphics(graphics);
 
 		// Label
 		TransitionLabel label = new TransitionLabel();
 		label.setText(logicalTransition.getTlb());
 		pnmlTransition.setTransitionLabel(label);
 
 		// Renew
 		TransitionRenew rnw = new TransitionRenew();
 		rnw.setText(logicalTransition.getRnw().toGUIString());
 		pnmlTransition.setTransitionRenew(rnw);
 
 		// Add to list
 		transitionsList.add(pnmlTransition);
 	}
 
 	/**
 	 * Adds a {@link petrinet.Place} into the <code>places</code> List using the
 	 * given <code>layout</code>
 	 * 
 	 * @param layout
 	 * @param places
 	 * @param place
 	 */
 	private static void addPlaceToPnml(Map<String, String[]> layout,
 			List<Place> places, petrinet.Place place) {
 		// Place and ID
 		Place newPlace = new Place();
 		newPlace.setId(String.valueOf(place.getId()));
 
 		// Name
 		PlaceName placeName = new PlaceName();
 		placeName.setText(place.getName());
 		newPlace.setPlaceName(placeName);
 
 		// Marking
 		InitialMarking initm = new InitialMarking();
 		initm.setText(String.valueOf(place.getMark()));
 		newPlace.setInitialMarking(initm);
 
 		// Graphics
 		Graphics graphics = new Graphics();
 
 		// Graphics -- Color
 		Color c = new Color();
 		c.setR(layout.get(newPlace.getId())[2]);
 		c.setG(layout.get(newPlace.getId())[3]);
 		c.setB(layout.get(newPlace.getId())[4]);
 		graphics.setColor(c);
 
 		// Graphics -- Position
 		List<Position> positionList = new ArrayList<Position>();
 		Position position = new Position();
 		position.setX(layout.get(newPlace.getId())[0]);
 		position.setY(layout.get(newPlace.getId())[1]);
 		positionList.add(position);
 
 		graphics.setPosition(positionList);
 		newPlace.setGraphics(graphics);
 
 		// Add to List
 		places.add(newPlace);
 	}
 
 	/**
 	 * Converts the position as a List of Strings into a {@link Point2D.Double}
 	 * object like specified in
 	 * {@link Converter#convertPetrinetToPnml(Petrinet, Map, double)}
 	 * 
 	 * @param pos
 	 * @throws NullPointerException
 	 *             if the any string is null
 	 * @throws NumberFormatException
 	 *             if any string does not contain a parsable double.
 	 * @return
 	 */
 	static private Point2D positionToPoint2D(List<Position> pos) {
 		return new Point2D.Double(Double.parseDouble(pos.get(0).x),
 				Double.parseDouble(pos.get(0).y));
 	}
 
 	/**
 	 * Converts a {@link Pnml} object that was unmarshalled from an XMLFile into
 	 * a {@link Petrinet}
 	 * 
 	 * @param pnml
 	 *            The object tree, representing the XML file
 	 * @param handler
 	 *            The engine handler to create and modify the petrinet.
 	 * @return id of petrinet Id of the created petrinet
 	 */
 	static public int convertPnmlToPetrinet(Pnml pnml,
 			IPetrinetPersistence handler) {
 		// In each XML file there is the type attribute for the pnml node to
 		// quick-check if its a rule or a petrinet
		if (pnml.getType().equals(RULE_IDENT)) {
 			throw new ShowAsWarningException(
 					"Die ausgewählte Datei enthält eine Regel, kein Petrinetz");
 		}
 		int petrinetID = -1;
 		try {
 			// create petrinet
 			petrinetID = handler.createPetrinet();
 
 			handler.setNodeSize(petrinetID, pnml.getNodeSize());
 
 			/**
 			 * Maps XML id to logical object
 			 */
 			Map<String, petrinet.INode> placesAndTransis = new HashMap<String, petrinet.INode>();
 
 			// create places
 			for (Place place : pnml.getNet().get(0).page.place) {
 				INode realPlace = handler.createPlace(petrinetID,
 						positionToPoint2D(place.getGraphics().getPosition()));
 				handler.setPlaceColor(petrinetID, realPlace, place
 						.getGraphics().getColor().toAWTColor());
 				handler.setPname(petrinetID, realPlace, place.getPlaceName()
 						.getText());
 				handler.setMarking(petrinetID, realPlace,
 						Integer.parseInt(place.getInitialMarking().getText()));
 
 				placesAndTransis.put(place.getId(), realPlace);
 			}
 
 			// create transitions
 			for (Transition pnmlTransition : pnml.getNet().get(0).getPage()
 					.getTransition()) {
 				INode realTransition = handler.createTransition(petrinetID,
 						positionToPoint2D(pnmlTransition.getGraphics()
 								.getPosition()));
 
 				handler.setTname(petrinetID, realTransition, pnmlTransition
 						.getTransitionName().getText());
 				handler.setTlb(petrinetID, realTransition, pnmlTransition
 						.getTransitionLabel().getText());
 				handler.setRnw(petrinetID, realTransition, Renews
 						.fromString(pnmlTransition.getTransitionRenew()
 								.getText()));
 
 				placesAndTransis.put(pnmlTransition.getId(), realTransition);
 
 			}
 
 			// create arcs
 			for (Arc arc : pnml.getNet().get(0).getPage().getArc()) {
 				handler.createArc(petrinetID, placesAndTransis.get(arc.source),
 						placesAndTransis.get(arc.target));
 
 			}
 			return petrinetID;
 		} catch (EngineException e) {
 			e.printStackTrace();
 			return -1;
 		}
 	}
 
 	/**
 	 * Converts a {@link Pnml} object that was unmarshalled from an XMLFile into
 	 * a {@link Rule}
 	 * 
 	 * @param pnml
 	 *            The object tree, representing the XML file
 	 * @param handler
 	 *            The engine handler to create and modify the rule.
 	 * @return id of petrinet Id of the created petrinet
 	 */
 	public static int convertPnmlToRule(Pnml pnml, IRulePersistence handler) {
 		// In each XML file there is the type attribute for the pnml node to
 		// quick-check if its a rule or a petrinet
 		if (!pnml.getType().equals(RULE_IDENT)) {
 			throw new ShowAsWarningException(
 					"Die ausgewählte Datei enthält ein Petrinetz, keine Regel");
 		}
 		int id = handler.createRule();
 		handler.setNodeSize(id, pnml.getNodeSize());
 
 		Net lNet = pnml.getNet().get(0);
 		Net kNet = pnml.getNet().get(1);
 		Net rNet = pnml.getNet().get(2);
 
 		// Elements of L
 		List<Place> lPlaces = lNet.getPage().getPlace();
 		List<Transition> lTransis = lNet.getPage().getTransition();
 		List<Arc> lArcs = lNet.getPage().getArc();
 
 		// Elements of K
 		List<Place> kPlaces = kNet.getPage().getPlace();
 		List<Transition> kTransis = kNet.getPage().getTransition();
 		List<Arc> kArcs = kNet.getPage().getArc();
 
 		// Elements of R
 		List<Place> rPlaces = rNet.getPage().getPlace();
 		List<Transition> rTransis = rNet.getPage().getTransition();
 		List<Arc> rArcs = rNet.getPage().getArc();
 
 		/** Contains the created INode object for each XML-id */
 		Map<String, INode> idToINodeInL = new HashMap<String, INode>();
 		/** Contains the created INode object for each XML-id */
 		Map<String, INode> idToINodeInK = new HashMap<String, INode>();
 		/** Contains the created INode object for each XML-id */
 		Map<String, INode> idToINodeInR = new HashMap<String, INode>();
 		try {
 			addPlacesToRule(id, lPlaces, kPlaces, rPlaces, handler,
 					idToINodeInL, idToINodeInK, idToINodeInR);
 			addTransitionsToRule(id, lTransis, kTransis, rTransis, handler,
 					idToINodeInL, idToINodeInK, idToINodeInR);
 			fillMapsWithMissingMappings(id, idToINodeInL, idToINodeInK,
 					idToINodeInR);
 			addArcsToTule(id, lArcs, kArcs, rArcs, handler, idToINodeInL,
 					idToINodeInK, idToINodeInR);
 		} catch (EngineException e) {
 			PopUp.popError(e);
 			e.printStackTrace();
 		}
 		return id;
 	}
 
 	/**
 	 * After Places and Rules have been added, there are only
 	 * String-id-to-INode-mappings for the original inserted nodes, but not for
 	 * the ones that were inserted automatically For beeing able to properly add
 	 * arcs to the rule, we need those mappings
 	 */
 	private static void fillMapsWithMissingMappings(int ruleId,
 			Map<String, INode> idToINodeInL, Map<String, INode> idToINodeInK,
 			Map<String, INode> idToINodeInR) {
 		ITransformation transformation = TransformationComponent
 				.getTransformation();
 		// fill with mappings of Ls nodes
 		for (String xmlId : idToINodeInL.keySet()) {
 			INode node = idToINodeInL.get(xmlId);
 			List<INode> mappings = transformation.getMappings(ruleId, node);
 			for (int i = 0; i < mappings.size(); i++) {
 				INode respectiveNode = mappings.get(i);
 				if (respectiveNode != null) {
 					if (i == 0) {
 						idToINodeInL.put(xmlId, respectiveNode);
 					} else if (i == 1) {
 						idToINodeInK.put(xmlId, respectiveNode);
 					} else if (i == 2) {
 						idToINodeInR.put(xmlId, respectiveNode);
 					}
 				}
 			}
 		}
 		// fill with mappings of Ks nodes
 		for (String xmlId : idToINodeInK.keySet()) {
 			INode node = idToINodeInK.get(xmlId);
 			List<INode> mappings = transformation.getMappings(ruleId, node);
 			for (int i = 0; i < mappings.size(); i++) {
 				INode respectiveNode = mappings.get(i);
 				if (respectiveNode != null) {
 					if (i == 0) {
 						idToINodeInL.put(xmlId, respectiveNode);
 					} else if (i == 1) {
 						idToINodeInK.put(xmlId, respectiveNode);
 					} else if (i == 2) {
 						idToINodeInR.put(xmlId, respectiveNode);
 					}
 				}
 			}
 		}
 		// fill with mappings of Rs nodes
 		for (String xmlId : idToINodeInR.keySet()) {
 			INode node = idToINodeInR.get(xmlId);
 			List<INode> mappings = transformation.getMappings(ruleId, node);
 			for (int i = 0; i < mappings.size(); i++) {
 				INode respectiveNode = mappings.get(i);
 				if (respectiveNode != null) {
 					if (i == 0) {
 						idToINodeInL.put(xmlId, respectiveNode);
 					} else if (i == 1) {
 						idToINodeInK.put(xmlId, respectiveNode);
 					} else if (i == 2) {
 						idToINodeInR.put(xmlId, respectiveNode);
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Adds all arcs from the XML to the rule object, using the engine handler
 	 * 
 	 * @param idToINodeInR
 	 * @param idToINodeInK
 	 */
 	private static void addArcsToTule(int id, List<Arc> lArcs, List<Arc> kArcs,
 			List<Arc> rArcs, IRulePersistence handler,
 			Map<String, INode> idToINodeInL, Map<String, INode> idToINodeInK,
 			Map<String, INode> idToINodeInR) throws EngineException {
 		/*
 		 * All arcs must be in K. To determine where the arcs must be added, we
 		 * have to find out: Are they also in L AND in K or just in one of them?
 		 * And if they are only in one of them - in which?
 		 */
 		for (Arc arc : kArcs) {
 			RuleNet toAddto;
 			INode source;
 			INode target;
 			if (getIdsOfArcsList(lArcs).contains(arc.getId())) {
 				if (getIdsOfArcsList(rArcs).contains(arc.getId())) {
 					toAddto = RuleNet.K;
 					source = idToINodeInK.get(arc.getSource());
 					target = idToINodeInK.get(arc.getTarget());
 				} else {
 					toAddto = RuleNet.L;
 					source = idToINodeInL.get(arc.getSource());
 					target = idToINodeInL.get(arc.getTarget());
 				}
 			} else {
 				toAddto = RuleNet.R;
 				source = idToINodeInR.get(arc.getSource());
 				target = idToINodeInR.get(arc.getTarget());
 			}
 			handler.createArc(id, toAddto, source, target);
 		}
 
 	}
 
 	/**
 	 * Adds places to the rule, writing the mappings of created places into the
 	 * resprective maps (last 3 parameters)
 	 * 
 	 * @param id
 	 * @param lPlaces
 	 * @param kPlaces
 	 * @param rPlaces
 	 * @param handler
 	 * @param idToINodeInL
 	 * @param idToINodeInK
 	 * @param idToINodeInR
 	 * @return
 	 * @throws EngineException
 	 */
 	private static Map<String, INode> addPlacesToRule(int id,
 			List<Place> lPlaces, List<Place> kPlaces, List<Place> rPlaces,
 			IRulePersistence handler, Map<String, INode> idToINodeInL,
 			Map<String, INode> idToINodeInK, Map<String, INode> idToINodeInR)
 			throws EngineException {
 		Map<String, INode> result = new HashMap<String, INode>();
 		/*
 		 * All places must be in K. To determine where the places must be added,
 		 * we have to find out: Are they also in L AND in K or just in one of
 		 * them? And if they are only in one of them - in which?
 		 */
 		for (Place place : kPlaces) {
 			RuleNet toAddto;
 			if (getIdsOfPlaceList(lPlaces).contains(place.getId())) {
 				if (getIdsOfPlaceList(rPlaces).contains(place.getId())) {
 					toAddto = RuleNet.K;
 				} else {
 					toAddto = RuleNet.L;
 				}
 			} else {
 				toAddto = RuleNet.R;
 			}
 			INode createdPlace = handler.createPlace(id, toAddto,
 					positionToPoint2D(place.getGraphics().getPosition()));
 			handler.setPlaceColor(id, createdPlace, place.getGraphics()
 					.getColor().toAWTColor());
 			handler.setPname(id, createdPlace, place.getPlaceName().getText());
 			handler.setMarking(id, createdPlace,
 					Integer.valueOf(place.getInitialMarking().getText()));
 			if (toAddto == RuleNet.L) {
 				idToINodeInL.put(place.id, createdPlace);
 			} else if (toAddto == RuleNet.K) {
 				idToINodeInK.put(place.id, createdPlace);
 			} else {
 				idToINodeInR.put(place.id, createdPlace);
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Adds transitions to the rule, writing the mappings of created places into
 	 * the resprective maps (last 3 parameters)
 	 * 
 	 * @param id
 	 * @param lPlaces
 	 * @param kPlaces
 	 * @param rPlaces
 	 * @param handler
 	 * @param idToINodeInL
 	 * @param idToINodeInK
 	 * @param idToINodeInR
 	 * @return
 	 * @throws EngineException
 	 */
 	private static Map<String, INode> addTransitionsToRule(int id,
 			List<Transition> lTransitions, List<Transition> kTransition,
 			List<Transition> rTransition, IRulePersistence handler,
 			Map<String, INode> idToINodeInL, Map<String, INode> idToINodeInK,
 			Map<String, INode> idToINodeInR) throws EngineException {
 		Map<String, INode> result = new HashMap<String, INode>();
 		/*
 		 * All Transitions must be in K. To determine where the transitions must
 		 * be added, we have to find out: Are they also in L AND in K or just in
 		 * one of them? And if they are only in one of them - in which?
 		 */
 		for (Transition transition : kTransition) {
 			RuleNet toAddto;
 			if (getIdsOfTransitionList(lTransitions).contains(
 					transition.getId())) {
 				if (getIdsOfTransitionList(rTransition).contains(
 						transition.getId())) {
 					toAddto = RuleNet.K;
 				} else {
 					toAddto = RuleNet.L;
 				}
 			} else {
 				toAddto = RuleNet.R;
 			}
 			INode createdTransition = handler.createTransition(id, toAddto,
 					positionToPoint2D(transition.getGraphics().getPosition()));
 			handler.setTlb(id, createdTransition, transition
 					.getTransitionLabel().getText());
 			handler.setTname(id, createdTransition, transition
 					.getTransitionName().getText());
 			handler.setRnw(id, createdTransition, Renews.fromString(transition
 					.getTransitionRenew().getText()));
 			if (toAddto == RuleNet.L) {
 				idToINodeInL.put(transition.id, createdTransition);
 			} else if (toAddto == RuleNet.K) {
 				idToINodeInK.put(transition.id, createdTransition);
 			} else {
 				idToINodeInR.put(transition.id, createdTransition);
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Extracts the XML ids of {@link Place places} into a List of ids as String
 	 * 
 	 * @param placeList
 	 * @return
 	 */
 	private static List<String> getIdsOfPlaceList(List<Place> placeList) {
 		List<String> ids = new LinkedList<String>();
 		for (Place place : placeList) {
 			ids.add(place.getId());
 		}
 		return ids;
 	}
 
 	/**
 	 * Extracts the XML ids of {@link Transition transitions} into a List of ids
 	 * as String
 	 * 
 	 * @param placeList
 	 * @return
 	 */
 	private static List<String> getIdsOfTransitionList(
 			List<Transition> transitionList) {
 		List<String> ids = new LinkedList<String>();
 		for (Transition place : transitionList) {
 			ids.add(place.getId());
 		}
 		return ids;
 	}
 
 	/**
 	 * Extracts the XML ids of {@link Arc arc} into a List of ids as String
 	 * 
 	 * @param placeList
 	 * @return
 	 */
 	private static List<String> getIdsOfArcsList(List<Arc> arcList) {
 		List<String> ids = new LinkedList<String>();
 		for (Arc arc : arcList) {
 			ids.add(arc.getId());
 		}
 		return ids;
 	}
 
 	/**
 	 * Converts a logical {@link Rule rule} object into the equivalent Pnml
 	 * object tree, which can the be marshalled into a file.
 	 * 
 	 * @param rule
 	 *            logical rule
 	 * @param layout
 	 *            layout information in the following format:
 	 *            <ul>
 	 *            <li>key: id of node as String</li>
 	 *            <li>value: {@link NodeLayoutAttribute layout}
 	 *            </ul>
 	 * @param nodeSize
 	 *            the size of the nodes in pixels. This is important due to
 	 *            collision detection when loading the petrinet.
 	 * @return
 	 */
 	public static Pnml convertRuleToPnml(Rule rule,
 			Map<INode, NodeLayoutAttribute> map, double nodeSize) {
 		Pnml pnml = new Pnml();
 
 		pnml.setType(RULE_IDENT);
 		pnml.setNodeSize(nodeSize);
 
 		pnml.net = new ArrayList<Net>();
 		final Net lNet = createNet(rule.getL(), map, RuleNet.L, rule);
 		final Net kNet = createNet(rule.getK(), map, RuleNet.K, rule);
 		final Net rNet = createNet(rule.getR(), map, RuleNet.R, rule);
 
 		pnml.setNet(new ArrayList<Net>() {
 			private static final long serialVersionUID = 8434245017694015611L;
 
 			{
 				add(lNet);
 				add(kNet);
 				add(rNet);
 			}
 		});
 
 		return pnml;
 	}
 
 	/**
 	 * Creates an {@link Net xml petrinet} as part of a rule. You can view this
 	 * as generating a sub tree
 	 * 
 	 * @param petrinet
 	 * @param map
 	 * @param type
 	 * @param rule
 	 * @return
 	 */
 	private static Net createNet(Petrinet petrinet,
 			Map<INode, NodeLayoutAttribute> map, RuleNet type, Rule rule) {
 		// Net, Page, ID and Type
 		Net net = new Net();
 		Page page = new Page();
 		net.setId(String.valueOf(petrinet.getId()));
 		net.setNettype(type.name());
 		net.setPage(page);
 
 		Set<petrinet.Arc> arcs = petrinet.getAllArcs();
 		Set<petrinet.Place> places = petrinet.getAllPlaces();
 		Set<petrinet.Transition> transis = petrinet.getAllTransitions();
 
 		List<Arc> listArcs = new ArrayList<Arc>();
 		List<Place> listPlace = new ArrayList<Place>();
 		List<Transition> listTrans = new ArrayList<Transition>();
 
 		try {
 			// inserting places
 			for (petrinet.Place place : places) {
 				Place newPlace = new Place();
 				
 				// This "redirects" the variable place to the node in K in case
 				// it is not already a node in K
 				if (type != RuleNet.K) {
 					INode correspondingNode = type == RuleNet.L ? rule
 							.fromLtoK(place) : rule.fromRtoK(place);
 					if (correspondingNode != null) {
 						place = (petrinet.Place) correspondingNode;
 					}
 				}
 				// id
 				newPlace.setId(String.valueOf(place.getId()));
 
 				// Name
 				PlaceName name = new PlaceName();
 				name.setText(place.getName());
 				newPlace.setPlaceName(name);
 
 
 				//Graphics -- Color
 				Graphics graphics = new Graphics();
 				Color color = new Color();
 				color.setR(String.valueOf(map.get(place).getColor().getRed()));
 				color.setG(String.valueOf(map.get(place).getColor().getGreen()));
 				color.setB(String.valueOf(map.get(place).getColor().getBlue()));
 				graphics.setColor(color);
 
 				//Graphics -- Position
 				Position position = new Position();
 				position.setX(String.valueOf(map.get(place).getCoordinate().getX()));
 				position.setY(String.valueOf(map.get(place).getCoordinate().getY()));
 				List<Position> positions = new ArrayList<Position>();
 				positions.add(position);
 				graphics.setPosition(positions);
 
 				newPlace.setGraphics(graphics);
 
 				// Marking
 				InitialMarking initM = new InitialMarking();
 				initM.setText(String.valueOf(place.getMark()));
 
 				newPlace.setInitialMarking(initM);
 
 				// Add to List
 				listPlace.add(newPlace);
 
 			}
 
 			// inserting Transitions
 			for (petrinet.Transition logicalTransition : transis) {
 				Transition xmlTransition = new Transition();
 
 				// This "redirects" the variable place to the node in K in case
 				// it is not already a node in K
 				if (type != RuleNet.K) {
 					INode correspondingNode = type == RuleNet.L ? rule
 							.fromLtoK(logicalTransition) : rule.fromRtoK(logicalTransition);
 					if (correspondingNode != null) {
 						logicalTransition = (petrinet.Transition) correspondingNode;
 					}
 				}
 
 				// ID
 				xmlTransition.setId(String.valueOf(logicalTransition.getId()));
 
 				// Name
 				TransitionName name = new TransitionName();
 				name.setText(logicalTransition.getName());
 				xmlTransition.setTransitionName(name);
 
 				// Graphics -- Position
 				Graphics graphics = new Graphics();
 				List<Position> positions = new ArrayList<Position>();
 				Position position = new Position();
 				position.setX(String.valueOf(map.get(logicalTransition).getCoordinate().getX()));
 				position.setY(String.valueOf(map.get(logicalTransition).getCoordinate().getY()));
 				positions.add(position);
 				graphics.setPosition(positions);
 				xmlTransition.setGraphics(graphics);
 
 				// Label
 				TransitionLabel label = new TransitionLabel();
 				label.setText(logicalTransition.getTlb());
 				xmlTransition.setTransitionLabel(label);
 
 				// Renew
 				TransitionRenew rnw = new TransitionRenew();
 				rnw.setText(logicalTransition.getRnw().toGUIString());
 				xmlTransition.setTransitionRenew(rnw);
 
 				// Add to List
 				listTrans.add(xmlTransition);
 			}
 
 			// inserting arcs
 			for (petrinet.Arc a : arcs) {
 				Arc arc = new Arc();
 
 				// This "redirects" the variable place to the node in K in case
 				// it is not already a node in K
 				if (type != RuleNet.K) {
 					INode correspondingNode = type == RuleNet.L ? rule
 							.fromLtoK(a) : rule.fromRtoK(a);
 					if (correspondingNode != null) {
 						a = (petrinet.Arc) correspondingNode;
 					}
 				}
 
 				// ID
 				arc.setId(String.valueOf(a.getId()));
 
 				// Graphics -- Position
 				Graphics graphics = new Graphics();
 				Position position = new Position();
 
 				List<Position> positions = new ArrayList<Position>();
 				positions.add(position);
 				graphics.setPosition(positions);
 
 				arc.setGraphics(graphics);
 
 				// Text
 				Inscription inscription = new Inscription();
 				inscription.setText(a.getName());
 				arc.setInscription(inscription);
 
 				// Source and Target
 				arc.setSource(String.valueOf(a.getStart().getId()));
 				arc.setTarget(String.valueOf(a.getEnd().getId()));
 
 				// Add to List
 				listArcs.add(arc);
 			}
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		page.setArc(listArcs);
 		page.setPlace(listPlace);
 		page.setTransition(listTrans);
 
 		return net;
 	}
 
 }
