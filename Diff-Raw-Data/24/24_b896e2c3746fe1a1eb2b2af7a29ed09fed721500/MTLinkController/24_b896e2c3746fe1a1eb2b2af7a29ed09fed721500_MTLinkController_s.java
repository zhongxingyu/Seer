 package ch.mitoco.components.visibleComponents.objectlink;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.mt4j.AbstractMTApplication;
 import org.mt4j.components.MTCanvas;
 
 import org.mt4j.input.IMTInputEventListener;
 import org.mt4j.input.gestureAction.DefaultLassoAction;
 import org.mt4j.input.gestureAction.TapAndHoldVisualizer;
 import org.mt4j.input.inputData.AbstractCursorInputEvt;
 import org.mt4j.input.inputData.MTInputEvent;
 import org.mt4j.input.inputProcessors.IGestureEventListener;
 import org.mt4j.input.inputProcessors.MTGestureEvent;
 import org.mt4j.input.inputProcessors.componentProcessors.lassoProcessor.LassoEvent;
 import org.mt4j.input.inputProcessors.componentProcessors.lassoProcessor.LassoProcessor;
 import org.mt4j.input.inputProcessors.componentProcessors.tapAndHoldProcessor.TapAndHoldEvent;
 import org.mt4j.input.inputProcessors.componentProcessors.tapAndHoldProcessor.TapAndHoldProcessor;
 import org.mt4j.util.MTColor;
 import org.mt4j.util.math.Vector3D;
 import org.mt4j.util.math.Vertex;
 
 import ch.mitoco.components.visibleComponents.MyMTObject;
 import ch.mitoco.model.ModelLink;
 import ch.mitoco.model.ModelLinkDescription;
 import ch.mitoco.model.ModelObjectTyps;
 import ch.mitoco.model.ModelTypDescription;
 import ch.mitoco.webserver.FileServer;
 import processing.core.PApplet;
 
 /** the MTLinkController Class control the links between the MyMTObjects and the eventHandling. */
 
 public class MTLinkController {
 
 	/** MTApplication. */
 	private PApplet pApplet;
 	
 	/** AbstractMTApplication. */
 	private AbstractMTApplication app;
 	/** MTApplication canvas. */
 	private MTCanvas canvas;
 	
 	/** List with all MyMTObject. */
 	private List<MyMTObject> myobjectList;
 		
 	/** Datamodel with all Link verbindungen. */
 	private List<ModelLink> modellinkList;
 	
 	/** Datamodel with Denny List. */
 	private List<ModelTypDescription> modelObjectTypDesc;
 		
 	/** Link Liste. */
 	private List<MTObjectLink> linkHeadlist;
 	
 	/** List with MTSelectStoreObjects ID. */
 	private List<MTSelectStoreObject> selectedObjectID;
 	
 	/** List with valid pairs for link building. */
 	private List<MTSelectStoreObject> validLinkPair;
 	
 	/** Lasso Processor.*/
 	private LassoProcessor lassoProcessor;
 	
 	/** Liste mit Links.*/
 	private List<MTLink> linkliste;
 	
 	
 	
 	/** MTLinkController. 
 	 * 
 	 * @param pApplet 
 	 * @param canvas
 	 * @param 
 	 * */
 	public MTLinkController(AbstractMTApplication pApplet, MTCanvas canvas) {
 		this.pApplet = pApplet;
 		this.app = pApplet;
 		this.canvas = canvas;
 		
 		System.out.println("MTLinkController: Konstruktor: ObjektListe " + this.myobjectList);
 		linkHeadlist = new ArrayList<MTObjectLink>();
 		
 		selectedObjectID = new ArrayList<MTSelectStoreObject>(); // Alle Markierten Object IDs werden als Paar gespeichert
 		selectedObjectID.add(new MTSelectStoreObject()); // Erste Object fr Dupleten Speicher. Die nchsten Objekte werden in der Methode storeSelectObject erstellt
 		
 		validLinkPair = new ArrayList<MTSelectStoreObject>();
 		
 		linkliste = Collections.synchronizedList(new ArrayList<MTLink>()); // Liste fr alle Objekte LINK, Thread Safe TODO: Nutzen muss noch abgeklrt werden
 	
 		init();
 	}
 
 	/** Init Method.*/
 	private void init() {
 		System.out.println("MTLinkController: init()    CanvasID: " + canvas.getID());
 		//link = new MTObjectLink(pApplet, new Vertex(new Vector3D(0, 0, 0)), new Vertex(new Vector3D(0, 0, 0)),);
 		//canvas.addChild(link);
 		lassoProcessor = new LassoProcessor(app, canvas, canvas.getViewingCamera());
 		canvas.registerInputProcessor(lassoProcessor);
 		canvas.addGestureListener(LassoProcessor.class, new DefaultLassoAction(app, canvas.getClusterManager(), canvas));
 		
 		selectedLasso();
 	
 	
 	}
 
 	/**
 	 * Objectliste und das Data Model der Links wird gesetzt. Aufgrufen durch den Datacontroller nach dem erstellen eines Objekt oder mehrern.
 	 * @param myobjectList
 	 */
 	public synchronized void setObjectList(List<MyMTObject> myobjectList , List<ModelLink> modellink, ModelObjectTyps modelObjectTyps){
 		this.myobjectList = null;
 		this.myobjectList = myobjectList;
 		System.out.println("MTLinkController: setObjectList: ObjektListe " + this.myobjectList + " IM bergabeParameter " + myobjectList);
 		this.modellinkList = null;
 		this.modellinkList = modellink;
 		
 
 		this.modelObjectTypDesc = null;
 		this.modelObjectTypDesc = new ArrayList<ModelTypDescription>();
 		for (ModelTypDescription it : modelObjectTyps.getObjectdescription()) {
 			//ArrayList<Integer> inn = it.getObjectdenylink();
 			this.modelObjectTypDesc.add(it);
 			/*
 			for (Integer itt : it.getObjectdenylink()) {
 				System.out.println("MTLinkController: Denny Object ID: " + itt);
 			}
 			*/
 		}
 		
 		// Liste ist leer
 		// TODO: Problem im Datacontroller lsen
 		System.out.println("MTLinkController: LinkList Model: " + modellinkList + " Grsse des Array: " + modellink.size());
 		for (ModelLink it : modellinkList) {
 			System.out.println("MTLinkController: setObjectList: Linklist: First " + it.getFirstObject() + " Second Objekt : " + it.getSecondObject());
 		}
 		
 	}
 	
 	/**
 	 * Lesen der ModelDaten fr die Links und Setzten.
 	 */
 	public void readDataAll() {
 		if(!(modellinkList.isEmpty())){
 			for (ModelLink it : modellinkList) {
 				System.out.println("MTLinkController: readDataAll: Links aus XML werden erstellt: 1: " + it.getFirstObject() + "  2: " + it.getSecondObject());
 				
 				Vertex vNode1 = new Vertex(new Vector3D(myobjectList.get(it.getFirstObject()).getCenterPointGlobal()));
 				Vertex vNode2 = new Vertex(new Vector3D(myobjectList.get(it.getSecondObject()).getCenterPointGlobal()));
 				
 				MTLink link = new MTLink(pApplet, vNode1, vNode2, it.getFirstObject(), it.getSecondObject());
 				linkliste.add(link);
 				addMTLinktoCanvas(link);
 			}
 		} else {
 			System.out.println("MTLinkController: readDataAll: Liste ist leer ");
 			
 		}
 		
 	}
 	
 	
 	public void showLinkListe() {
 		for (MTLink it : linkliste) {
 			System.out.println("MTLinkController: showLinkListe: EndObjekt " + it.getEndObjectID() + " StartObj:" +it.getStartObjectID());
 			}
 	}
 	
 	/**
 	 * DIe ganze linkliste wird aus dem Gui in das Datamodel geschrieben.
 	 */
 	public void writeDataAll() {
 		
 		modellinkList.clear();
 		for (MTLink it : linkliste) {
 		writeDataOne(it);
 		}
 	}
 	
 	/**
 	 * Schreibt die Daten eines Link Objekt in das Datamodel.
 	 * 
 	 * @param obj MTLink
 	 */
 	public void writeDataOne(final MTLink obj) {
 		System.out.println("MTLinkController: writeDataOne: Link wird geschrieben: " + obj);
 		
 		ModelLink m1 = new ModelLink();
 		m1.setFirstObject(obj.getStartObjectID());
 		m1.setSecondObject(obj.getEndObjectID());
 	
 		List<ModelLinkDescription> d1  = new ArrayList<ModelLinkDescription>();
 		ModelLinkDescription el1 = new ModelLinkDescription();
 		el1.setLinkcolor(obj.getStrokeColor());
 		el1.setLinkdescription(obj.getName());
 		d1.add(el1);
 		
 		m1.setObjectlinkdescription(d1);
 		
 		modellinkList.add(m1);	
 	}
 	
 	
 	/** Position Dedection for MTObjects.*/
 	// TODO Postition Dedection
 	private void eventObjectHandling() {
 		
 		for (MyMTObject it : myobjectList){
 			it.addInputListener(new IMTInputEventListener() {
 				
 				public boolean processInputEvent(MTInputEvent inEvt) {
 				
 					if (inEvt instanceof AbstractCursorInputEvt) {
 						AbstractCursorInputEvt cursorInputEvt = (AbstractCursorInputEvt) inEvt;
 						if (!(linkliste.isEmpty())) {
 							switch (cursorInputEvt.getId()) {
 							case AbstractCursorInputEvt.INPUT_STARTED:
 								drawLinie();	
 								break;
 							case AbstractCursorInputEvt.INPUT_UPDATED:
 								drawLinie();
 								break;
 							case AbstractCursorInputEvt.INPUT_ENDED:
 								drawLinie();
 								break;
 							default:
 								break;
 							}
 						} else {
 							//System.out.println("MTLinkController: eventObjectHandling: Keine Link Objekte vorhanden");	
 						}
 						
 					} else {
 						// handle other input events stuff
 					}
 					return false;
 				}
 			}
 			);
 		}
 	}
 	
 	/** 
 	 * Zeichnet alle Linien die sich in der ArrayListe linkliste befinden.
 	 * */
 	public void drawLinie() {
 		
 		for (MTLink it : linkliste) {
 			it.setColorPickerVisible(false);
 			it.setVertices(new Vertex[]{
 					new Vertex(myobjectList.get(it.getStartObjectID()).getCenterPointGlobal()),
 					new Vertex(myobjectList.get(it.getEndObjectID()).getCenterPointGlobal())});	
 		}	
 	}
 	
 	
 	
 	/**
 	 * 
 	 * Add Gesture Listener (TapAndHoldProcessor) for all MyMTObjects.
 	 * 
 	 */
 	//TODO Insert this in Model Controller when object Createt
 	@Deprecated
 	public final void setTapAndHoldListener() {
 		System.out.println("MTLinkController: setTapAndHoldListener: fr ListenFeld");
 		for (MyMTObject it : myobjectList) {
 			detectionObSelection(it);
 			detectionObSelectionLasso(it);			
 		}
 	}
 	
 	/**
 	 * 
 	 * Add Gesture Listener (TapAndHoldProcessor) for one selected Object.
 	 * 
 	 * Erstellt den Linkhead (Target Finder) um ein Link zwischen zwei Objekte zu machen.
 	 * 
 	 * @param obj MyMTObject
 	 */
 	public final void setTapAndHoldListener(final MyMTObject obj) {
 		System.out.println("MTLinkController: setTapAndHoldListener: fr Objekt: " + obj);
 		
 		Vector3D startPointLH = new Vector3D(260, 10); // 10,10
 		Vector3D endPointLH = new Vector3D(260, -40); // -40 , 10
 		
 		detectionObSelection(obj);
 		detectionObSelectionLasso(obj);	
 		linkHeadlist.add(obj.getID(), new MTObjectLink(pApplet, new Vertex(startPointLH), new Vertex(endPointLH), obj.getID()));
 		obj.addChild(linkHeadlist.get(obj.getID()));
 		
 	}
 	
 	/** Object detection/selection with TapAndHold Processor.
 	 * 
 	 * Each param MyMTObject become a TapAndHoldProcessor and run by Gesture_Ended the methode setSelectedObjectColor.
 	 * 
 	 * @param obj MyMTObject 
 	 */
 	private void detectionObSelection(final MyMTObject obj) {
 		obj.registerInputProcessor(new TapAndHoldProcessor(app, 1000));
 		obj.addGestureListener(TapAndHoldProcessor.class, new TapAndHoldVisualizer(app, canvas));
 		obj.addGestureListener(TapAndHoldProcessor.class, new IGestureEventListener() {
 			public boolean processGestureEvent(MTGestureEvent ge) {
 				TapAndHoldEvent th = (TapAndHoldEvent) ge;
 				switch (th.getId()) {
 				case TapAndHoldEvent.GESTURE_STARTED:
 					break;
 				case TapAndHoldEvent.GESTURE_UPDATED:
 					break;
 				case TapAndHoldEvent.GESTURE_ENDED:
 					if (th.isHoldComplete()) {
 						setSelectedObjectColor(obj);
 					}
 					break;
 				default:
 					break;
 				}
 				return false;
 			}
 		});
 	}
 	
 	/** Object detection/selection with LassoProcessor
 	 * 
 	 * Jedem Object wird dem LassoProcessor hinzugefgt, damit es auch lassoable wird.
 	 * 
 	 * @param obj MyMTObject
 	 */
 	private void detectionObSelectionLasso(final MyMTObject obj) {
 		lassoProcessor.addLassoable(obj);
 	}
 	
 	/**
 	 *  Set the StrokeColor from the selected Object.
 	 *  
 	 *  @param obj MyMTObject
 	 *   
 	 * */	
 	private void setSelectedObjectColor(final MyMTObject obj) {
 		if (obj.getTagFlag()) { // == true
 			obj.setNormalColor();
 			obj.setTagFlag(false);
 			removeSelectObject(obj);
 		} else {
 			obj.setTaggedColor(MTColor.RED);
 			obj.setTagFlag(true);
 			storeSelectObject(obj);
 		}		
 	}
 	
 	/**
 	 *  Set the StrokeColor from the selected Object with Interger ID.
 	 *  
 	 *  @param objID int
 	 *  @param setting boolean 
 	 *  				1 = markieren
 	 *  				0 = nicht markiert
 
 	 * */	
 	public void setSelectedObjectColor(final int objID, boolean setting, MTColor color) {
 		
 		System.out.println("MTLinkController: setSelectedObjectColor: Object ID " + objID);
 		System.out.println("MTLinkController: setSelectedObjectColor: Object ID " + myobjectList.get(objID));
 	
 		
 		if (myobjectList.get(objID).getTagFlag() == true && !setting) {
 			myobjectList.get(objID).setNormalColor();
 			myobjectList.get(objID).setTagFlag(false);
 			//removeSelectObject(myobjectList.get(objID));
 		} else if (setting) {
 			myobjectList.get(objID).setTaggedColor(color);
 			myobjectList.get(objID).setTagFlag(true);
 			//storeSelectObject(myobjectList.get(objID));
 		}	
 		
 	}
 
 	/**
 	 * 
 	 * Gibt zurck ob eine Linkverbindung erlaubt ist oder nicht. Dies anhand der in der ModelTypDescription festgelegten Denny List.
 	 * 
 	 * @param startObj int 
 	 * 			ID des start Objekts
 	 * @param endObj int
 	 * 			ID des end Objekts
 	 * @return valid booelan
 	 */
 	public boolean isValidLinkRequest(int startObj, int endObj) {
 		boolean valid = true;
 				
 		if (myobjectList.get(startObj).getObjecttyp() == -1) { // Ist dem MyMtObjekt kein Objekttyp zugeordnet (Festgelegt duch -1) kann immer eine Beziehung errichtet werden. 
 			System.out.println("MTLinkController: isValidLinkRequest: Objekttyp ist nicht definiert - ID: " + myobjectList.get(startObj).getObjecttyp());
 			valid = true;
 		} else {
 			
 			// Dem Objekt wurde ein Objekttyp zugordnet und kann nun nach einer Denny Liste gesucht werden
 			for (ModelTypDescription it : modelObjectTypDesc) {
 				
 				// In der Denny Liste nach der Richtigen Description fr den richtige Objekttyp suchen
 				if (myobjectList.get(startObj).getObjecttyp() == it.getObjectypeid()) {
 					
 					// Objekttyp ist in der ModelTypDescription vorhanden. Jetzt muss nur noch die Denny Liste durchsuchen werden
 					for (Integer itt : it.getObjectdenylink()) {
						System.out.println("MTLinkController: isValidLinkRequest: LINK NICHT ERLAUBT - End Objekt : " + itt);	
 						//Liste mit allen Objekten in dem
 						if (myobjectList.get(endObj).getObjecttyp() == itt) {
 							valid = false; // EndObjekt ist in der Liste vorhanden, Link darf nicht erstellt werden		
 							System.out.println("MTLinkController: isValidLinkRequest: LINK NICHT ERLAUBT - End Objekt : " + myobjectList.get(endObj).getObjecttyp() + " ist gleich StartObj: " + itt);
 						break; // Denny gefunden aus der Schlaufe 
 						} else {
							valid = true; // EnbObjekt ist in der Liste nicht vorhanden, Link darf erstellt werden
							System.out.println("MTLinkController: isValidLinkRequest: LINK erlaubt");
 						}
 					}
 					break;
 				} else {
 					System.err.println("MTLinkController: isValidLinkRequest: Object ID: " + it.getObjectypeid() + " gibt es in der ModelTypDescription Liste nicht!");
 					valid = true;
 				}
 			}
 		}
 		return valid;
 	}
 	
 	/** Zhler fr markiertes Objekt.*/
 	private int i = 0;
 	
 	/** Zhler fr Objektpaar von je zwei markierte Objekten.*/
 	private int j = 0; // Objecte 
 	/** Save the selected MyMtObject.
 	 * 
 	 * @param obj MyMTObject
 	 * */
 	private void storeSelectObject(final MyMTObject obj) {
 		
 		if(i%2==0){
 			selectedObjectID.get(j).setEndObjectID(obj.getID());
 			System.out.println("Object erstellt" +j +":"+i );
 			i++;
 		} else {
 		
 			selectedObjectID.get(j).setStartObjectID(obj.getID());
 			selectedObjectID.get(j).setValid(true);
 			System.out.println("Object erstellt" +j +":"+i );
 			i++;
 			
 		}
 		if(i%2==0){
 			System.out.println("Dupplete Vorhanden" +j );
 			j++;
 			selectedObjectID.add(new MTSelectStoreObject());
 			System.out.println("Wurde erstellt: " +j );
 			
 		}
 		
 		
 	}
 	
 	/** Save the selected MyMtObject.
 	 * 
 	 * @param obj MyMTObject
 	 * */
 	private void removeSelectObject(final MyMTObject obj) {
 		if(i%2==0){
 			System.out.println("Dupplete Vorhanden" +j );
 			System.out.println("Wurde gelscht: " +j );
 			selectedObjectID.remove(j);
 			j--;
 
 		}
 		
 		if(i%2==0){
 			i--;
 			//selectObjectID.get(0).setEndObjectID(obj.getID());
 			System.out.println("Gelscht Zahl ist gerade: " +j + ":"+i );
 			
 		} else {
 			i--;
 			//selectObjectID.get(0).setStartObjectID(obj.getID());
 			System.out.println("Gelscht Zahl ist ungerade: " +j + ":"+i );
 		}
 	
 		for(MTSelectStoreObject it: selectedObjectID) {
 			if (it.getEndObjectID() == obj.getID()){
 				it.setValid(false);
 				it.setEndObjectID(-1);
 			}
 			if (it.getStartObjectID() == obj.getID()){
 				it.setValid(false);
 				it.setStartObjectID(-1);
 			}
 		}
 
 		
 		
 	}
 	/** 
 	 * Print selected Object in the list.
 	 * @return list List<Integer> 
 	 */
 	public final List<Integer> getSelectedList() {
 		List<Integer> list = new ArrayList<Integer>();
 		
 //		for (MTSelectStoreObject it : selectedObjectID) {
 //			System.out.println("Opject Paar gepeichert " + it.getEndObjectID() + ":" + it.getStartObjectID() + " PAAR ist Valid; " + it.isValid());
 //		}
 		for (MyMTObject it : myobjectList) {
 			if(it.getTagFlag() == true) {
 				list.add(it.getID());
 			}
 		}
 		
 		return list; 
 	}
 	
 	/**
 	 * 
 	 * Store only Valid Link pair in validLinkPair.
 	 * 
 	 * */
 	public final void storeValidPair() {
 		for (MTSelectStoreObject it : selectedObjectID) {
 			if (it.isValid() ){
 				validLinkPair.add(new MTSelectStoreObject(it.getStartObjectID(), it.getEndObjectID(), it.isValid()));		
 			}
 		}
 		
 		for (MTSelectStoreObject itt : validLinkPair) {
 			System.out.println("VALID Opject Paar gepeichert " + itt.getEndObjectID() + ":" + itt.getStartObjectID() + " PAAR ist Valid; " + itt.isValid());
 		}		
 	}
 		
 	/**
 	 * 
 	 * Gibt eine Liste von Objekt IDs mit allen verlinkungen eines gewnschten Objekts zurck.
 	 * 
 	 * @param searchObj int
 	 * @return list List<Integer>
 	 */ 
 	public final List<Integer> getLinkListObject(final int searchObj) {
 		List<Integer> list = new ArrayList<Integer>();
 	
 		for (MTLink it : linkliste) {
 			if (it.getEndObjectID() == searchObj) {
 				list.add(it.getStartObjectID());
 			}
 			if (it.getStartObjectID() == searchObj) {
 				list.add(it.getEndObjectID());
 			}		
 		}
 		return list; 
 	}
 	
 	/**
 	 * Anzeigen oder verstecken aller Links.
 	 * 
 	 * visible = false = Link wird nicht angezeigt
 	 * visible = true = Link wird angezeigt
 	 * 
 	 * @param visible boolean
 	 */
 	public void setVisibleAllLink(final boolean visible) {
 		for (MTLink it : linkliste) {
 			it.setVisible(visible);
 		}
 	}
 	
 	/**
 	 * 
 	 * Anzeigen oder verstecken aller Links die dem mitgegebenen Objekt angehngt sind.
 	 *  
 	 * true = sichtbar
 	 * false = unsichtbar
 	 * 
 	 * @param visible boolean
 	 *  			true = sichtbar, false = unsichtbar
 	 *  
 	 * @param objID int
 	 */
 	public void setVisibleOne(boolean visible, int objID){
 		for (MTLink it : linkliste) {
 			if (it.getEndObjectID() == objID) {
 				it.setVisible(visible);
 			}
 			if (it.getStartObjectID() == objID) {
 				it.setVisible(visible);
 			}
 		}
 	}
 
 	/**
 	 * 
 	 * Lscht die Links eines gewnschten Objekts.
 	 * 
 	 * @param objID
 	 */
 	public void removeLinkOne(int objID) {
 		List<Integer> listLink = new ArrayList<Integer>();
 
 		// Suchen nach den passenden Links
 		for(int i = 0; i<linkliste.size(); i++){
 			MTLink it = linkliste.get(i);
 			if (it.getEndObjectID() == objID) {
 				listLink.add(i);
 			} else if (it.getStartObjectID() == objID) {
 				listLink.add(i);
 			}	
 			
 		}
 		
 		for(Integer it : listLink) {
 			System.out.println("MTLinkController: removeLinkOne: Link ID; " + it + " Gibt" + linkliste.get(it).getDescription());
 			
 			linkliste.remove(it);
 		}
 		writeDataAll();	
 	}
 	
 	/**
 	 * 
 	 * Gibt eine Liste alle MTLink Objekte zurck.
 	 * 
 	 * @return List<MTLink>
 	 */
 	public final List<MTLink> getMTLinkList() {
 		return linkliste;
 	}
 	
 	
 	/**
 	 * -> Selector
 	 * Gibt alle Object ID der ber den ArcBall getaggten Objekte zurck.
 	 * 
 	 * @return list List<Integer>
 	 */
 	public final List<Integer> showTaggedObject() {
 		List<Integer> list = new ArrayList<Integer>();
 		for (MyMTObject itt : myobjectList) {
 			list.add(itt.getID());
 		}
 		return list;
 	}
 	
 	/**
 	 * Erstellt Link zwischen zwei Objekte. Soll ein Link erstellt werden der schon vorhanden ist wird dieser gelscht. Zwischen zwei gleiche Objekte (ID Paar) kann nur ein Link bestehen.
 	 * 
 	 * @param node1 int
 	 * @param node2 int
 	 */
 	public void createLink(final int node1, final int node2) {
 		
 		System.out.println("MTLinkController: createLink: Link for Objekt ID; " + node1 + " und " + node2);
 		/*
 		 * 1. Link erstellen 
 		 *    - a. Abfrage: Link schon in der Liste 
 		 *    - b. Wenn ja Link lschen, ansonsten erstellen
 		 * 2. Beim erstelln berprfen ob Link zwischen den zwei gewnschten Objekten erlaubt ist
 		 */
 
 		Vertex vNode1 = new Vertex(new Vector3D(myobjectList.get(node1).getCenterPointGlobal()));
 		Vertex vNode2 = new Vertex(new Vector3D(myobjectList.get(node2).getCenterPointGlobal()));
 		
 		boolean found = false;
 		if (!(linkliste.isEmpty())) {
 			
 			
 				for (int t = 0; t < linkliste.size(); t++) {		
 					MTLink it = linkliste.get(t);
 					//if ((it.getStartObjectID() == node1 && it.getEndObjectID() == node2) || (it.getStartObjectID() == node2 && it.getEndObjectID() == node1)) {
 		
 					if ((it.getStartObjectID() == node1 && it.getEndObjectID() == node2) || (it.getStartObjectID() == node2 && it.getEndObjectID() == node1)) {
 						// Lschen eines vorhanden Links
 						System.out.println("MTLinkController: createLink: Link SCHON in der Liste vorhanden; " + node1 + " und " + node2 + "\n \t \t Vorhanderner LINK: " + it.getStartObjectID() + " und " + it.getEndObjectID());
 						canvas.removeChild(it);
 						
 						linkliste.remove(t);
 						it.destroy();
 						writeDataAll();
 						found = true;
 						
 					} else {
 						System.out.println("MTLinkController: createLink: NICHT GEFUNDEN");
 					}
 						
 				}
 				if (!found) {
 				//Testen ist das Linksetzen ber die Typen der zwei Objekte erlaubt (ber eine Deny Liste)
 					createLinkObject(vNode1, vNode2, node1, node2);
 					}	
 		} else {
			// Noch kein Link vorhanden
 			createLinkObject(vNode1, vNode2, node1, node2);
 		}
 		
 	}
 	
 	/**
 	 * Erstell eine neues Link Objekt.
 	 * 
 	 * @param vNode1 vertex
 	 * @param vNode2 vertex
 	 * @param node1 int
 	 * @param node2 int
 	 */
 	private void createLinkObject(final Vertex vNode1, final Vertex vNode2, final int node1, final int node2) {
 		
 		if (isValidLinkRequest(node1, node2)) {
 			System.out.println("MTLinkController: createLink: Link noch NICHT in der Liste vorhanden; " + node1 + " und " + node2);
 			MTLink link = new MTLink(pApplet, vNode1, vNode2, node1, node2);
 			linkliste.add(link);
 			addMTLinktoCanvas(link);
 			writeDataAll();
 		} else { 
 			System.out.println("MTLinkController: createLink: Link ist nicht erlaubt; " + node1 + " und " + node2);
 		}
 	}
 	
 	
 	/**
 	 * MTLink auf dem Canvas Zeichnen.
 	 * 
 	 * @param obj MTLink
 	 */
 	private void addMTLinktoCanvas(final MTLink obj) {	
 		System.out.println("MTLinkController: addMTLinktoCanvas: Link wird dem Canavas zugeordnet; ");
 		canvas.addChild(obj);
 		eventObjectHandling();
 	}
 	
 	
 	
 	
 	/**
 	 * Which Object ist lassobaled.
 	 */
 	public final void selectedLasso() {
 		
 		
 		lassoProcessor.addGestureListener(new IGestureEventListener() {
 			
 			@Override
 			public boolean processGestureEvent(MTGestureEvent ge) {
 				// TODO Auto-generated method stub
 
 				LassoEvent th = (LassoEvent) ge;
 				switch (th.getId()) {
 				case LassoEvent.GESTURE_STARTED:
 					break;
 				case LassoEvent.GESTURE_UPDATED:
 					break;
 				case LassoEvent.GESTURE_ENDED:
 					System.out.println("Gesture wurde ausgefhrt");
 					
 					
 						if (!(myobjectList == null)) {
 							for (MyMTObject it : myobjectList) {
 								if (it.isSelected() == true) {
 									System.out.println("MTLinkController: @@@@@ Object ist gespeichert NR:" + it.getID());
 									
 								}
 							}
 						}
 						
 					
 					break;
 				default:
 					break;
 				}
 			return false;
 			}
 		});		
 	}
 	
 	
 	
 	
 	
 }
