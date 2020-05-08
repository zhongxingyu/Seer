 package map;
 
 import java.io.IOException;
 import java.util.List;
 
 import log.LogManager;
 import log.Logger;
 import processing.core.PApplet;
 import rdf.RDFModel;
 import ui.marker.NamedMarker;
 import util.StringUtil;
 import util.location.LocationCache;
 import util.location.OrganizationLocationCache;
 
 import com.hp.hpl.jena.query.QuerySolution;
 import com.hp.hpl.jena.query.ResultSet;
 import com.hp.hpl.jena.rdf.model.RDFNode;
 import com.hp.hpl.jena.sparql.vocabulary.FOAF;
 
 import controlP5.ControlEvent;
 import controlP5.ControlListener;
 import controlP5.ControlP5;
 import controlP5.ListBox;
 import de.fhpotsdam.unfolding.UnfoldingMap;
 import de.fhpotsdam.unfolding.geo.Location;
 import de.fhpotsdam.unfolding.marker.Marker;
 import de.fhpotsdam.unfolding.marker.MarkerManager;
 import de.fhpotsdam.unfolding.utils.MapUtils;
 import de.looksgood.ani.Ani;
 
 public class KeywordMap extends PApplet {
 
 	private static final Logger logger = LogManager.getLogger(KeywordMap.class);
 
 	private static final int WIDTH = 1040;
 	private static final int HEIGHT = 720;
 
 	private static final int CONFLIST_X = 50, CONFLIST_Y = 50, CONFLIST_W = 120, CONFLIST_H = 120, CONFLIST_ITEMH = 15;
 
 	private static final int KEYWLIST_W = 140, KEYWLIST_H = HEIGHT - 2 * CONFLIST_Y, KEYWLIST_Y = CONFLIST_Y,
 			KEYWLIST_X = WIDTH - KEYWLIST_W, KEYWLIST_ITEMH = 20;
 
 	private static final int DEFAULT_MARKER_COLOR = 0x50505050, HIGHLIGHTED_MARKER_COLOR = 0xFFFF5050;
 
 	/**
 	 * Generated serial version ID
 	 */
 	private static final long serialVersionUID = -2929486399441127311L;
 
 	private UnfoldingMap map;
 	private ListBox conflist;
 	private ListBox keywordList;
 
 	private MarkerManager<NamedMarker> orgMarkMan;
 
 	private LocationCache locationCache;
 
 	private String currConf;
 
 	public static void main(String[] args) {
 		PApplet.main(new String[] { "map.KeywordMap" });
 	}
 
 	public void setup() {
 		size(WIDTH, HEIGHT);
 		frameRate(30);
 
 		smooth();
 		Ani.init(this);
 
 		// create location cache
 		try {
 			locationCache = new OrganizationLocationCache("data/organisations.csv");
 		} catch (IOException e) {
 			logger.fatal("Location cache file data/organisations.csv gave an IO Error");
 			logger.catching(e);
 
 			exit();
 		}
 
 		map = new UnfoldingMap(this) {
 			@Override
 			public boolean isHit(float checkX, float checkY) {
 				return !isInsideRect(CONFLIST_X, CONFLIST_Y, CONFLIST_W, CONFLIST_H, checkX, checkY)
 						&& !isInsideRect(KEYWLIST_X, KEYWLIST_Y, KEYWLIST_W, KEYWLIST_H, checkX, checkY);
 			}
 
 			private boolean isInsideRect(float x, float y, float w, float h, float checkX, float checkY) {
 				return (checkX >= x) && (checkX <= x + w) && (checkY >= y) && (checkY <= y + h);
 			}
 		}; // , 0, 200, this.width, this.height-200);
 		map.setTweening(true); // (doesn't work). it does now, what changed?
 								// smooth()?
 		map.zoomAndPanTo(new Location(20, 0), 3);
 		MapUtils.createDefaultEventDispatcher(this, map);
 
 		orgMarkMan = new MarkerManager<>();
 		addAllOrgMarkers();
 		map.addMarkerManager(orgMarkMan);
 
 		setupGUI();
 		populateGUI();
 
 		currConf = null;
 	}
 
 	/**
 	 * Populate the extra GUI elements with the needed data.
 	 */
 	private void populateGUI() {
 		List<RDFNode> confs = RDFModel.getConferences();
 		for (int i = 0; i < confs.size(); i++) {
 			String acronym = StringUtil.getString(confs
 					.get(i)
 					.asResource()
 					.getProperty(
 							RDFModel.getModel().getProperty("http://data.semanticweb.org/ns/swc/ontology#hasAcronym")));
 			conflist.addItem(acronym, i);
 		}
 		List<String> keywords = RDFModel.getResultsAsStrings(RDFModel.getAllKeywords(), "keyword");
 		keywordList.addItems(keywords);
 	}
 
 	/**
 	 * Add all the extra GUI elements (apart from the map) to the PApplet.
 	 */
 	private void setupGUI() {
 		ControlP5 cp5 = new ControlP5(this);
 		conflist = cp5.addListBox("Conferences").setPosition(CONFLIST_X, CONFLIST_Y).setSize(CONFLIST_W, CONFLIST_H)
 				.setBarHeight(CONFLIST_ITEMH).setItemHeight(CONFLIST_ITEMH).setColorActive(0x50505050).setValue(0f);
 
 		cp5.addButton("ShowAllButton").setCaptionLabel("Show all").setValue(0).setPosition(200, 35).setSize(120, 19);
 
 		keywordList = cp5.addListBox("Keywords").setPosition(KEYWLIST_X, KEYWLIST_Y).setSize(KEYWLIST_W, KEYWLIST_H)
 				.setBarHeight(KEYWLIST_ITEMH).setItemHeight(KEYWLIST_ITEMH);
 
 		// Listener to control selection events.
 		cp5.addListener(new ControlListener() {
 			@Override
 			public void controlEvent(ControlEvent e) {
 				if (e.isGroup() && e.getGroup().getName().equals("Conferences")) {
 					int idx = (int) e.getGroup().getValue();
 					String acro = conflist.getItem(idx).getName();
 					currConf = acro;
 					showOnlyConf(acro);
 				} else if (e.isGroup() && e.getGroup().getName().equals("Keywords")) {
 					int idx = (int) e.getGroup().getValue();
 					String keyword = keywordList.getItem(idx).getName();
 					logger.info("Marking keyword: " + keyword + " from acro:" + currConf);
 					markMarkers(currConf, keyword);
 				} else if (!e.isGroup() && e.getController().getName().equals("ShowAllButton")) {
 					showAll();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Called when showAll button is clicked
 	 */
 	private void showAll() {
 		showAllOrgMarkers();
 		keywordList.clear();
 		List<String> keywords = RDFModel.getResultsAsStrings(RDFModel.getAllKeywords(), "keyword");
 		keywordList.addItems(keywords);
 		currConf = null;
 	}
 
 	/**
 	 * Shows only the markers from the given conference name on the map.
 	 * 
 	 * @param confAcronym
 	 */
 	private void showOnlyConf(String confAcronym) {
 		hideAllOrgMarkers();
 		showOrgMarkersOf(confAcronym);
 		keywordList.clear();
 		List<String> keywords = RDFModel.getResultsAsStrings(RDFModel.getAllKeywordsFromConference(confAcronym),
 				"keyword");
 		keywordList.addItems(keywords);
 	}
 
 	private void clearMarkedMarkers() {
 		for (Marker m : orgMarkMan.getMarkers()) {
 			m.setColor(DEFAULT_MARKER_COLOR);
 		}
 	}
 
 	private void markMarkers(String confAcronym, String keyword) {
 		clearMarkedMarkers();
 		List<String> orgnames = RDFModel.getResultsAsStrings(RDFModel.getOrgsWithKeyword(confAcronym, keyword),
 				"orgname");
 		for (String orgName : orgnames) {
 			NamedMarker nm = getMarkerWithName(orgName);
			nm.setColor(HIGHLIGHTED_MARKER_COLOR);
 		}
 	}
 
 	/**
 	 * Adds all the organization markers to the map.
 	 */
 	private void addAllOrgMarkers() {
 		ResultSet rs = RDFModel.getAllOrganisations();
 
 		while (rs.hasNext()) {
 			QuerySolution sol = rs.next();
 			String orgname = StringUtil.getString(sol.getLiteral("orgname"));
 			Location loc = locationCache.get(orgname);
 			if (loc != null) {
 				NamedMarker m = new NamedMarker(orgname, loc);
 				orgMarkMan.addMarker(m);
 				m.setColor(DEFAULT_MARKER_COLOR);
 			}
 		}
 	}
 
 	/**
 	 * Hides all the organizations markers from the map.
 	 */
 	private void hideAllOrgMarkers() {
 		for (NamedMarker m : orgMarkMan.getMarkers()) {
 			m.setHidden(true);
 		}
 	}
 
 	/**
 	 * Shows all the organizations markers from the map.
 	 */
 	private void showAllOrgMarkers() {
 		for (NamedMarker m : orgMarkMan.getMarkers()) {
 			m.setHidden(false);
 		}
 	}
 
 	/**
 	 * Shows only the organisation markers of the given organisation name on the
 	 * map.
 	 * 
 	 * @param confAcronym
 	 */
 	private void showOrgMarkersOf(String confAcronym) {
 		for (RDFNode node : RDFModel.getOrganisationsOfConference(confAcronym)) {
 			String orgName = StringUtil.getString(node.asResource().getProperty(FOAF.name));
 
 			for (NamedMarker m : orgMarkMan.getMarkers()) {
 				if (m.getName().equals(orgName))
 					m.setHidden(false);
 			}
 		}
 	}
 
 	@Override
 	public void draw() {
 		background(245);
 		map.draw();
 	}
 
 	// /**
 	// * Takes care of the hovering feature.
 	// * When you hover over a marker, the marker is set to selected and the
 	// marker handles it change in look itself.
 	// */
 	// public void mouseMoved(){
 	// List<? extends Marker> hitMarkers = orgMarkMan.getHitMarkers(mouseX,
 	// mouseY);
 	// for (Marker m : orgMarkMan.getMarkers()) {
 	// m.setSelected(hitMarkers.contains(m));
 	// }
 	// }
 
 	public void mouseClicked() {
 		if (!map.isHit(mouseX, mouseY))
 			return;
 		NamedMarker hitMarker = orgMarkMan.getFirstHitMarker(mouseX, mouseY);
 		if (hitMarker == null)
 			return;
 		hitMarker.setSelected(!hitMarker.isSelected());
 		if (hitMarker.isSelected()) {
 			keywordList.clear();
 			keywordList
 					.addItems(RDFModel.getResultsAsStrings(RDFModel.getKeywordsOfOrg(hitMarker.getName()), "keyword"));
 		} else {
 			keywordList.clear();
 			List<String> keywords = RDFModel.getResultsAsStrings(RDFModel.getAllKeywordsFromConference(currConf),
 					"keyword");
 			keywordList.addItems(keywords);
 		}
 	}
 
 	/**
 	 * Find the marker with a given name
 	 */
 	public NamedMarker getMarkerWithName(String name) {
 		for (NamedMarker nm : orgMarkMan.getMarkers()) {
 			if (nm.getName().equals(name))
 				return nm;
 		}
 		return null;
 	}
 }
