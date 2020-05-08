 package ch.mitoco.components.visibleComponents;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.mt4j.MTApplication;
 import org.mt4j.components.MTComponent;
 import org.mt4j.components.TransformSpace;
 import org.mt4j.components.visibleComponents.shapes.MTRoundRectangle;
 import org.mt4j.components.visibleComponents.widgets.MTTextArea;
 import org.mt4j.components.visibleComponents.widgets.buttons.MTImageButton;
 import org.mt4j.input.IMTInputEventListener;
 import org.mt4j.input.gestureAction.DefaultDragAction;
 import org.mt4j.input.gestureAction.DefaultRotateAction;
 import org.mt4j.input.gestureAction.DefaultScaleAction;
 import org.mt4j.input.gestureAction.InertiaDragAction;
 import org.mt4j.input.inputData.AbstractCursorInputEvt;
 import org.mt4j.input.inputData.MTInputEvent;
 import org.mt4j.input.inputProcessors.IGestureEventListener;
 import org.mt4j.input.inputProcessors.MTGestureEvent;
 import org.mt4j.input.inputProcessors.componentProcessors.dragProcessor.DragEvent;
 import org.mt4j.input.inputProcessors.componentProcessors.dragProcessor.DragProcessor;
 import org.mt4j.input.inputProcessors.componentProcessors.lassoProcessor.ILassoable;
 import org.mt4j.input.inputProcessors.componentProcessors.rotateProcessor.RotateProcessor;
 import org.mt4j.input.inputProcessors.componentProcessors.scaleProcessor.ScaleProcessor;
 import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapEvent;
 import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapProcessor;
 import org.mt4j.util.MTColor;
 import org.mt4j.util.font.FontManager;
 import org.mt4j.util.font.IFont;
 import org.mt4j.util.math.ToolsMath;
 import org.mt4j.util.math.Vector3D;
 import org.mt4jx.input.gestureAction.dnd.DragAndDropTarget;
 
 import processing.core.PImage;
 import ch.mitoco.components.visibleComponents.objectlink.MTLinkController;
 import ch.mitoco.components.visibleComponents.widgets.Attributes;
 import ch.mitoco.components.visibleComponents.widgets.MTColorPickerGroup;
 import ch.mitoco.components.visibleComponents.widgets.MTDrawingBox;
 import ch.mitoco.components.visibleComponents.widgets.MTDropDownList;
 import ch.mitoco.components.visibleComponents.widgets.MTListAttribut;
 import ch.mitoco.components.visibleComponents.widgets.MTNumField;
 import ch.mitoco.components.visibleComponents.widgets.MTPictureBox;
 import ch.mitoco.components.visibleComponents.widgets.MTTextAttribut;
 import ch.mitoco.model.ModelAttributContent;
 import ch.mitoco.model.ModelMtAttributs;
 import ch.mitoco.model.ModelMtObjects;
 import ch.mitoco.webserver.FileServer;
 
 /**
  * 
  * The Class MyMTObject. Standard Class for all Object on the Table. 
  * 
  * @author tandrich
  */
 
 public class MyMTObject extends MTRoundRectangle implements ILassoable, DragAndDropTarget {
 
 	
 	/** MTApplication. */
 	private MTApplication pApplet;
 	
 	/** Baserect is the base structur for all Attributs. */
 	private MTRoundRectangle baserect;
 
 	/** Object Label. */
 	private MTTextArea textField;
 
 	/** Standard Object Width in Min Modus.*/
 	private int obSizeMinWidth;
 	
 	/** Standard Object Height in Min Modus. */
 	private int obSizeMinHeight;
 
 	/** Object Width in Max Modus. */
 	private int obSizeMaxWidth;
 	
 	/** Object Height in Max Modus. */
 	private int obSizeMaxHeight;
 	
 	/** Object Size dif max to min. */
 	private int obDifSizeHeight;
 	
 	/** MTImageButton for Max/Min Modus. */
 	private MTImageButton buttonMaxMin;
 	
 	/** MTImageButton for Rotate Modus.*/
 	private MTImageButton buttonRotate;
 
 	/** Int for minmaxModus. */
 	private int minmaxModus;
 	
 	/** Boolean for Rotation. */
 	private boolean updownrotate;
 	
 	/** Default Color for Border. */
 	private MTColor greypez = new MTColor(12, 12, 12, 34);
 
 	/** Default Color for stroke. */
 	private MTColor speblue = new MTColor(51, 102, 255);
 	
 	/** Object ID for identification and Objectlabel. */
 	private int id;
 	
 	/** Font for Content. */
 	private IFont fontArialMini;
 	
 	/** Font for Label. */
 	private IFont labelfont;
 	
 	/** List with all Attributes.*/
 	private List<Attributes>myAttributs;
 		
 	/** List for Attributes. */	
 	private List<ModelMtAttributs> attributesmodel;
 	
 	/** Data Model for this object.*/
 	private ModelMtObjects objectmodel;
 	
 	/** Standard Font. */
 	private IFont fontArial;
 	
 	/** Color Picker. */
 	private MTColorPickerGroup colorpicker;
 
 	/** Tagged Boolean.*/
 	private boolean tagged;
 	
 	/** before Tagged Stroke Color. */
 	private MTColor befortaggedColor;
 	
 	/** Clip selected. */
 	private boolean selected;
 	
 	/** Test: Linker Controller. */
 	private MTLinkController linker; //TODO: Test
 	
 	/** ID in Interger. */
 	private Integer idInt;
 	
 	/** DragAndDrop Target Objekt Speicher. */
 	private ArrayList<MTComponent> droppedComponents = new ArrayList<MTComponent>();
 	
 	/** Arraylist for attribut hight.*/
 	private ArrayList<Integer> attributHight = new  ArrayList<Integer>();
 	
 	/** Objekt Typ.*/
 	private int objecttyp;
 	
 	/**
 	 * Default Hhe der Attribute, wenn keinen Wert ber das Datamodel mitgegeben wurde .
 	 */
 	private int defaultHeightTextAttribut = 30;
 	private int defaultHeightNumField = 30;
 	private int defaultHeightDropDown = 30;
 	private int defaultHeightMTPictureBox = 120;
 	private int defaultHeightListAttribut = 200;
 	private int defaultHeightMTDrawingBox = 200;
 	
 	
 	/** Constructor MyMTObject.
 	 * 
 	 * Im Konstruktor wird das Grundobjekt mit dem Bilderrahmen und den Grundfunktionen aufgebaut. 
 	 * 
 	 * 
 	 * nderungen die Model noch ntig sind:
 	 * - Labelfont
 	 * - Normalerfont (vorhanden)
 	 * - Objekt grsse (Breite und Hhe)
 	 * - Konstruktion des Objekt in eine init Methode auslagern
 	 * 
 	 * 
 	 * @param pApplet2 MTApplication
 	 * @param model ModelMTObjects
 	 * @param objectID INT ObjectID  
 	 * 
 	 */
 	public MyMTObject(final MTApplication pApplet2, final ModelMtObjects model, final int objectID, MTLinkController linkerorg) {
 		super(pApplet2, 0, 0, 0, 0, 0, 5, 5);
 
 		this.pApplet = pApplet2;
 		this.id = objectID;
 		this.selected = false;
 		this.linker = linkerorg;
 		
 		// Data Model 
 		this.objectmodel = model;
 		attributesmodel = model.getObjectattributs();
 		
 //		obSizeMinWidth = 300; // Grsse Min
 //		obSizeMinHeight = 300; // Grsse Min
 //		obSizeMaxWidth = 300;
 //		obSizeMaxHeight = 400;
 		minmaxModus = 1; // Objekt wird im MinModus gestartet
 			
 		fontArial = FontManager.getInstance().createFont(pApplet, "arial.ttf", 
 				15, 	//Font size
 				MTColor.BLACK);
 		
 		//this.setSizeLocal(obSizeMinWidth, obSizeMinHeight);
 		this.setStrokeWeight(1);
 		Integer idInt = new Integer(id);
 		this.setName(idInt.toString());
 		
 		// Font for Textfield
 		fontArialMini = FontManager.getInstance().createFont(pApplet, "arial.ttf", 
 				14, 	//Font size
 				MTColor.BLACK);
 		// Font for Label
 		labelfont = FontManager.getInstance().createFont(pApplet, "arial.ttf", 
 				14, 	//Font size
 				MTColor.WHITE);
 		
 		// Crate ArrayList for Attributs
 		myAttributs = new ArrayList<Attributes>();
 		
 		
 		//Create a textfield
 		textField = new MTTextArea(pApplet, fontArial); 
 		textField.setNoStroke(true);
 		textField.setNoFill(true);
 		textField.setPickable(false);
 		textField.translate(new Vector3D(5, 5));
 		
 		// Base Rect 
 		baserect = new MTRoundRectangle(pApplet, 10, 10, 0, obSizeMinWidth - 20, obSizeMinHeight - 20, 5, 5);
 		baserect.setNoStroke(true);
 		baserect.setPickable(false);
 		
 		this.setSizeLocal(300, 100);
 		baserect.setSizeLocal(300, 100);
 		
 		// ColorPicker
 		colorpicker = new MTColorPickerGroup(pApplet, this, baserect);
 		colorpicker.translate(new Vector3D(0, -35, 0));
 		colorpicker.setVisible(false);
 	
 		readData();
 		
 		// Button for Rotate
 		PImage buttonImage = pApplet.loadImage("ch" + MTApplication.separator + "mitoco" + MTApplication.separator + "data" + MTApplication.separator +  "buttonRotate.png");
 		buttonRotate = new MTImageButton(pApplet, buttonImage);
 		buttonRotate.setSizeLocal(30, 30);
 		buttonRotate.setFillColor(new MTColor(255, 255, 255, 200));
 		buttonRotate.setName("KeyboardButton");
 		buttonRotate.setNoStroke(true);
 		//keyboardButton.translateGlobal(new Vector3D(-2,mtApplication.height-keyboardButton.getWidthXY(TransformSpace.GLOBAL)+2,0));
 		//buttonRotate.setPositionGlobal(new Vector3D(20, obSizeMinHeight - 20));
 		
 		buttonRotate.addGestureListener(TapProcessor.class, new IGestureEventListener() {
 			
 			@Override
 			public boolean processGestureEvent(final MTGestureEvent ge) {
 				TapEvent te = (TapEvent) ge;
 				switch(te.getTapID()) {
 				case TapEvent.TAPPED:
 					baserect.rotateZ(new Vector3D((baserect.getWidthXY(TransformSpace.LOCAL) / 2) + 10, (baserect.getHeightXY(TransformSpace.LOCAL) / 2) + 10), 180);
 					updownrotate = !updownrotate;
 					printMTObjectModel(objectmodel); //TODO: Eintrag frs Testen: gibt DataModel inhalt aus
 					dataWrite(); 
 					linker.getSelectedList();
 					linker.showTaggedObject();
 					linker.getLinkListObject(0);
 					linker.showLinkListe();
 					break;	
 				default:
 					break;
 				}
 			return false;
 			}
 		});
 		
 		//Button Max/Min Modus
 		PImage buttonMaxMinImage = pApplet.loadImage("ch" + MTApplication.separator + "mitoco" + MTApplication.separator + "data" + MTApplication.separator +  "buttonMaxMin.png");
 		buttonMaxMin = new MTImageButton(pApplet, buttonMaxMinImage);
 		buttonMaxMin.setSizeLocal(30, 30);
 		buttonMaxMin.setFillColor(new MTColor(255, 255, 255, 200));
 		buttonMaxMin.setName("KeyboardImage");
 		buttonMaxMin.setNoStroke(true);
 		//buttonMaxMin.setPositionGlobal(new Vector3D(obSizeMinWidth - 20, 20));
 		
 		buttonMaxMin.addGestureListener(TapProcessor.class, new IGestureEventListener() {
 			
 			//private float h = getHeightXY(TransformSpace.RELATIVE_TO_PARENT);
 			//private float w = getWidthXY(TransformSpace.RELATIVE_TO_PARENT);
 			
 			@Override
 			public boolean processGestureEvent(final MTGestureEvent ge) {
 				TapEvent ke = (TapEvent) ge;	
 				switch (ke.getTapID()) {
 				case TapEvent.TAPPED:
 					dataWrite();
 					if (minmaxModus == 0) { //TODO: IF Lschen!!!!
 						setAttributForMinModus();
 //						setMax(h, w);
 //						minmaxModus = 1;
 						
 					} else {
 						setAttributForMinModus();
 //						setMin(h, w);
 //						minmaxModus = 0;
 					}
 					break;
 
 				default:
 					break;
 				}
 				
 				return false;
 			}
 		});
 
 		
 		// Add MTComponets to Canvas
 		// --------------------------------
 
 		baserect.addChild(textField); //Titeltext
 
 		createAttributes();
 		setAttributForMinModus();		
 		addToBaserect(); // Add all Attributes to the base Rect Object
 		setPickableAttributes();
 		
 		saveOnEvent();		
 		this.addChild(baserect);
 		this.addChild(buttonRotate);
 		this.addChild(buttonMaxMin);
 		this.addChild(colorpicker);
 	}
 	
 
 	/** Create attributes.
 	 * 
 	 * Anhanden der IDs die im attributesmodel gespeichert sind, wird das Objekt aufgebaut.
 	 * 
 	 * Gibt es weiter Attribute, muss die Case Anweisung um die gewnschte ID erweitern werden. 
 	 * Welche ID welches Attribut ist muss definiert werden und kann nicht einfach so gendert werden.
 	 * ID 0: 	MTTextAttribut
 	 * ID 1: 	MTNumField
 	 * ID 2: 	MTDropDown
 	 * ID 3: 	MTPictureBox
 	 * ID 4:    MTListAttribut
 	 * ID 5:	futur MTPainter
 	 * ID 6:	futur MTBrowser
 	 * ID 7:	futur MTPDFReader
 	 * ID 8:	futur MTTextBox
 	 * 
 	 * TODO: Interface bauen.
 	 * 
 	 * Fr Jedes Attribut muss ein Defaulthhen angegeben werden. Dies wird verwendet, wenn kein Wert im Datamodel (xml) vorhanden ist.
 	 * Jedes Attribut muss der Anchor in der mitte haben (Center) damit das aufstellen klappt.
 	 */
 	private void createAttributes() {
 
 	int i = 0;
 		for (ModelMtAttributs it : attributesmodel) {
 			switch(it.getId()) {
 			case(0):
 				System.out.println("MyMTObject: " + i + "Attributs MTTextAttribut " + it);
 				myAttributs.add(new MTTextAttribut(pApplet, attributesmodel.get(i), fontArialMini, 250, readAttributHeight(i, defaultHeightTextAttribut), "Test", "TestFeld Text", labelfont));
 				break;
 			
 			case(1):	
 				System.out.println("MyMTObject: " + i + "Attributs MTNumField " + it);
 				myAttributs.add(new MTNumField(pApplet, attributesmodel.get(i), fontArialMini, 250, readAttributHeight(i, defaultHeightNumField), true, "1111134.34", "TestFeld", labelfont));
 				break;
 			
 			case(2):
 				System.out.println("MyMTObject: " + i + "Attributs MTDropDown " + it);
 				myAttributs.add(new MTDropDownList(pApplet, attributesmodel.get(i), fontArialMini, 250, readAttributHeight(i, defaultHeightDropDown), "Projekt Wichtigkeit", labelfont));
 				break;
 				
 			case(3):
 				System.out.println("MyMTObject: PB " + i + "Attributs MTPictureBox " + it);
 				myAttributs.add(new MTPictureBox(pApplet, attributesmodel.get(i), 250, readAttributHeight(i, defaultHeightMTPictureBox), "Picuture", labelfont));
 				break;
 				
 			case(4): 
 				System.out.println("MyMTObject: " + i + "Attributs MTListAttribut " + it);
 				myAttributs.add(new MTListAttribut(pApplet, attributesmodel.get(i), fontArialMini, 250, readAttributHeight(i, defaultHeightListAttribut), "Lcher", labelfont));
 				break;
 				
 			case(5):
 				System.out.println("MyMTObject: " + i + "Attributs MTDrawingBox " + it);
 				myAttributs.add(new MTDrawingBox(pApplet, 250, readAttributHeight(i,defaultHeightMTDrawingBox), "Zeichnen", labelfont));
 				break;
 			default:
 				break;
 			}
 		i++;
 		}
 		
 	}
 	
 	/**
 	 * 
 	 * Methode um die hhe im Max Modus zu setzten
 	 * 
 	 *  Attribute die im Min-Modus versteckt sind werden ausgeblendet. 
 	 */
 	private void setAttributForMinModus() {
 		/*
 		 * Zwei Methoden 
 		 *  1. Postitionierung fr den Max-Modus
 		 *  2. Positioinierung fr den Min-Modus
 		 *  
 		 * Selbe Spiel wir beim erstellen der Attribute createAttributs einfach der min/Max Status des Attributs muss ausgelesen werden Auslesen 
 		 * 
 		 * Ablauf:
 		 * 1. Objekte werden instanziert und erstell (createObject) und dem canvas zugeordnet
 		 * 2. Positioniert wird fr den MinModus berechnet
 		 * 
 		 * 3. Positionierung wird fr den MaxModus berechnet (wenn Max gedrckt wird)
 		 * 
 		 * setVisible
 		 * 
 		 */
 		int i = 0; // Anzahl Attribute im MaxModus
 		int j = 0; // Anzahl Attribute im MinModus
 		int difx = 0; // Zhler der die Attribut Postition aufsummiert von Attribut zu Attribut. Max Modus
 		int difxHiddenMinModus = 0; // Zhler der die Attribut Postition aufsummiert von Attribut zu Attribut. Max Modus
 		
 		for (ModelMtAttributs it : attributesmodel) {
 			switch(it.getId()) {
 			case(0):
 				System.out.println("MyMTObject: Position setzten: " + i + "Attributs MTTextAttribut: " + it + " Hhe des Attr:" + difx);
 			
 				if (minmaxModus == 0) {
 					//Objekt ist im MaxModus
 					myAttributs.get(i).setVisible(true);
 					difx = setAttributPosition(difx, readAttributHeight(i, defaultHeightTextAttribut), i, myAttributs.get(i), -1);			
 				} else if (minmaxModus == 1) {
 					//Objekt ist im MinModus
 					if (!(attributesmodel.get(i).isMinMax())) {
 						
 						difx = setAttributPosition(difx, readAttributHeight(i, defaultHeightTextAttribut), i, myAttributs.get(i), j);
 						j++;
 					} else {
 						 myAttributs.get(i).setVisible(false);
 					}
 				}
 				break;
 			
 			case(1):
 				System.out.println("MyMTObject: Position setzten: " + i + "Attributs MTNumField " + it + " " + difx);
 				
 				if (minmaxModus == 0) {
 					myAttributs.get(i).setVisible(true);
 					difx = setAttributPosition(difx, readAttributHeight(i, defaultHeightNumField), i, myAttributs.get(i), -1);
 				} else if (minmaxModus == 1) {
 					if (!(attributesmodel.get(i).isMinMax())) {
 						difx = setAttributPosition(difx, readAttributHeight(i, defaultHeightNumField), i, myAttributs.get(i), j);
 						j++;
 					} else  {
 						myAttributs.get(i).setVisible(false);
 					}
 				}
 				
 
 				break;
 			
 			case(2):
 				System.out.println("MyMTObject: Position setzten: " + i + "Attributs MTDropDown " + it + " " + difx);
 				
 				if (minmaxModus == 0) {
 					myAttributs.get(i).setVisible(true);
 					difx = setAttributPosition(difx, readAttributHeight(i, defaultHeightDropDown), i, myAttributs.get(i), -1);
 				} else if (minmaxModus == 1) {
 					if (!(attributesmodel.get(i).isMinMax())) {
 						difx = setAttributPosition(difx, readAttributHeight(i, defaultHeightDropDown), i, myAttributs.get(i), j);
 						j++;
 					} else  {
 						myAttributs.get(i).setVisible(false);
 					}
 				}
 				break;
 			
 			case(3):
 				System.out.println("MyMTObject: Position setzten: " + i + "Attributs MTPictureBox " + it  + " " + difx);
 				
 				if (minmaxModus == 0) {
 					myAttributs.get(i).setVisible(true);
 					difx = setAttributPosition(difx, readAttributHeight(i, defaultHeightMTPictureBox), i, myAttributs.get(i), -1);
 				} else if (minmaxModus == 1) {
 					//MinModus
 					if (!(attributesmodel.get(i).isMinMax())) {
 						difx = setAttributPosition(difx, readAttributHeight(i, defaultHeightMTPictureBox), i, myAttributs.get(i), j);
 						j++;
 					} else  {
 						myAttributs.get(i).setVisible(false);
 					}
 				}
 				break;
 			
 			case(4):
 				System.out.println("MyMTObject: Position setzten: " + i + "Attributs MTListAttribut " + it  + " " + difx);
 				
 				if (minmaxModus == 0) {
 					myAttributs.get(i).setVisible(true);
 					difx = setAttributPosition(difx, readAttributHeight(i, defaultHeightListAttribut), i, myAttributs.get(i), -1);
 				} else if (minmaxModus == 1) {
 					if (!(attributesmodel.get(i).isMinMax())) {
 						difx = setAttributPosition(difx, readAttributHeight(i, defaultHeightListAttribut), i, myAttributs.get(i), j);
 						j++;
 					} else  {
 						myAttributs.get(i).setVisible(false);
 					}
 				}
 				break;
 			
 			case(5):
 				System.out.println("MyMTObject: Position setzten: " + i + "Attributs MTDrawingBox " + it  + " " + difx);
 				
 				if (minmaxModus == 0) {
 					myAttributs.get(i).setVisible(true);
 					difx = setAttributPosition(difx, readAttributHeight(i, defaultHeightMTDrawingBox), i, myAttributs.get(i), -1);
 				} else if (minmaxModus == 1) {
 					if (!(attributesmodel.get(i).isMinMax())) {
 						difx = setAttributPosition(difx, readAttributHeight(i, defaultHeightMTDrawingBox), i, myAttributs.get(i), j);
 						j++;
 					} else  {
 						myAttributs.get(i).setVisible(false);
 					}
 				}
 				break;
 			default:
 				break;
 			
 			}
 			i++;
 		}	
 		setMyMTObjectNewSize(difx, j); // Grsse des Objekts festlegen
 	}
 	
 	/**
 	 * Liest aus dem Datenmodel oder setzten des Default Wertes fr die Attributhhe.
 	 * 
 	 * Zudem wird die Hhe der einzlen Attribute in einem Array gespeichert.
 	 * 
 	 * @param attributID int
 	 * 			ID Nummer des gerade aufgerufen Attributs (nicht ID des Typs)
 	 * @param defaultHight int 
 	 * 			Hier muss die default Hhe angegeben werden. Dieser wird verwendetet, wenn keine Hhe im Datenmodel festgelegt worden ist.
 	 * @return attributHight.get(attributID) int
 	 */
 	private int readAttributHeight(final int attributID, final int defaultHight) {
 		if (attributesmodel.get(attributID).getAttributHight() == 0) {
 				System.out.println("MyMTObject: readAttributHight: Default " + defaultHight);
 				attributHight.add(attributID, defaultHight);
 			} else {
 				System.out.println("MyMTObject: readAttributHight: aus XML " + attributesmodel.get(attributID).getAttributHight());
 				attributHight.add(attributID, attributesmodel.get(attributID).getAttributHight());
 			}
 		return attributHight.get(attributID);
 	}
 	
 	
 	
 	/**
 	 * Festlegen der neuen Grsse des MyMTObject.
 	 * 
 	 * @param size int
 	 */
 	private void setMyMTObjectNewSize(final int sizeMax, final int countforMinAttr) {
 	
 	//float h = getHeightXY(TransformSpace.RELATIVE_TO_PARENT);
 	//float w = getWidthXY(TransformSpace.RELATIVE_TO_PARENT);
 	
 	//setMax(h, w);
 		
 	
 	obSizeMinWidth = 300;
 	obSizeMaxWidth = 300;
 	
 	obDifSizeHeight = obSizeMaxHeight - obSizeMinHeight;
 	
 	if (minmaxModus ==0){
 		// Im Maxmodus
 		System.out.println("MyMTObject: setMyMTObjectNewSize: Platzbedarf (Hhe) der Attribute MaxModus: " + sizeMax);
 		
 		
 		obSizeMaxHeight = sizeMax + 40; // 40 + Offset fr Abstand zum Objektrand unten
 		this.setSizeLocal(obSizeMaxWidth, obSizeMaxHeight);
 		baserect.setSizeLocal(obSizeMaxWidth - 20, obSizeMaxHeight - 20);
 		
 		for (Attributes it : myAttributs) {
 			it.setMax();
 		}
 		colorpicker.setVisible(true);
 		
 //		if (updownrotate) {
 //			baserect.translate(new Vector3D(0, obDifSizeHeight));
 //		}
 		
 		buttonMaxMin.setPositionRelativeToParent(new Vector3D(obSizeMaxWidth - 20, 20));
 		//buttonMaxMin.translate(new Vector3D(obSizeMaxWidth - obSizeMinWidth, 0));
 //		buttonRotate.translate(new Vector3D(0, obSizeMaxHeight - obSizeMinHeight));
 		buttonRotate.setPositionRelativeToParent(new Vector3D(20, obSizeMaxHeight - 20));
 		minmaxModus = 1;
 		
 	} else if (minmaxModus == 1) {
 		// IM MinModus
 		System.out.println("MyMTObject: setMyMTObjectNewSize: Platzbedarf (Hhe) der Attribute MinModus: " + sizeMax);
 		
 		obSizeMinHeight = sizeMax + 40;
 		this.setSizeLocal(obSizeMinWidth, obSizeMinHeight);
 		baserect.setSizeLocal(obSizeMinWidth - 20, obSizeMinHeight - 20);
 		
 		for (Attributes it : myAttributs) {
 			it.setMin();
 		}
 		colorpicker.setVisible(false);
 		
 //		if (updownrotate) {
 //			baserect.translate(new Vector3D(0, -obDifSizeHeight));
 //		}	
 		
 		buttonMaxMin.setPositionRelativeToParent(new Vector3D(obSizeMinWidth - 20, 20));
 //		buttonMaxMin.translate(new Vector3D(obSizeMinWidth - obSizeMaxWidth, 0));
 //		buttonRotate.translate(new Vector3D(0, obSizeMinHeight - obSizeMaxHeight));
 		buttonRotate.setPositionRelativeToParent(new Vector3D(20, obSizeMinHeight - 20));
 		minmaxModus = 0;
 	}
 		
 	}
 	
 	/**
 	 *  Set Pickable False for all Attributes.
 	 */
 	private void setPickableAttributes() {
 		for (Attributes it : myAttributs) {
 			it.setPickable(false);
 		}
 	}
 	
 	
 	/**
 	 * Add Attributs to baserect.
 	 */
 	private void addToBaserect() {
 		
 		for (int j = myAttributs.size() - 1; j >= 0; j--) {
 			  System.out.println("MyMTObject: addChild " + j);
 			  baserect.addChild(myAttributs.get(j));
 			}		
 	}
 		
 	/**
 	 * 
 	 * Mit dieser Metode wird das Attribut auf dem Objekt platziert und wenn ntig den Abstand gegen Oben definiert.
 	 * 
 	 * @param difx
 	 * @param defaultHeight
 	 * @param i int Anzahl Attribute im Max-Modus
 	 * @param it Attributes
 	 * @param j int Anzahl Attribute im Min-Modus
 	 * @return
 	 */
 	private int setAttributPosition(int difx, int defaultHeight, int i, Attributes it, final int j) {
 		int captop = 20; // Zustzlicher Abstand zum ersten Attribut
 		int cap = 20; // Abstand zwischen den Zeilen
 		
 		difx = difx + cap + readAttributHeight(i, defaultHeight) / 2;
 		if (i == 0 || j == 0) difx += captop; // Erste Attribut im Objekt bekommt ein grsser Abstand von oben
 		it.setPositionRelativeToParent(new Vector3D(this.getWidthXY(TransformSpace.LOCAL) / 2, difx /*+ readAttributHeight(i,defaultHeightTextAttribut)/2*/));
 		difx = difx + readAttributHeight(i, defaultHeight) / 2;
 		
 		/*
 		if (attributesmodel.size() == 1) { // Befinden sich im Objekt nur ein Attribut muss die grsse nach unten nachgerechnet werden!! 
 			System.out.println("MyMTObject: setAttributPosition: sNur ein Attribut im Objekt vorhanden !!!!!!");
 			difx  = difx + readAttributHeight(i, defaultHeight) / 2;
 		}
 		*/
 		
 		return difx;
 	}
 	
 
 	
 	
 	
 	/**
 	 * Read data from object Model.
 	 * 
 	 * 1. Setzten der Farbe fr Rahmenlinie, Rahmen und Hintergrund
 	 * 2. Setzten der Objektposition
 	 * 3. Setzen des Objekt Titels
 	 * 4. Setzten Schriftsatzt
 	 * 5. Setzten des Objekttyp
 	 * 
 	 */
 	private void readData() {		
 		
 		// Model filled color Border parameters
 		if (this.objectmodel.getObjectcolor() == null) {
 			this.setFillColor(greypez);
 		} else {
 		this.setFillColor(this.objectmodel.getObjectcolor());
 		}
 		
 		// Object linie color
 		if (this.objectmodel.getObjectlinecolor() == null) {
 			this.setStrokeColor(speblue);
 		} else {
 		this.setStrokeColor(this.objectmodel.getObjectlinecolor());
 		}
 		
 		// Base Backround Color
 		System.out.println("MyMTObject: Fill Color");
 		if (this.objectmodel.getObjectFillcolor() == null) {
 			System.out.println("MyMTObject: No Fill Color found");
 			baserect.setFillColor(MTColor.GRAY);
 			baserect.setStrokeColor(MTColor.GRAY);
 		} else {
 			System.out.println("MyMTObject: Fill Color found");
 		baserect.setStrokeColor(this.objectmodel.getObjectFillcolor());
 		baserect.setFillColor(this.objectmodel.getObjectFillcolor());
 		}
 		
 		// Object Position with random when no position is stored
 		System.out.println("MyMTObject: Position");
 		if (this.objectmodel.getObjectposition() == null) {
 			this.setPositionGlobal(new Vector3D(ToolsMath.nextRandomInt(140, 800), ToolsMath.nextRandomInt(140, 700)));
 		} else {
 			this.setPositionGlobal(this.objectmodel.getObjectposition());
 		}
 		
 		// Object Font
 		System.out.println("MyMTObject: Font");
 		if (this.objectmodel.getObjectfont() == null) {
 			fontArial = FontManager.getInstance().createFont(pApplet, "arial.ttf", 
 					15, 	//Font size
 					MTColor.BLACK);
 		} else {
 			fontArial = this.objectmodel.getObjectfont();
 		}
 		
 		// Object Labeltext
 		if (this.objectmodel.getObjectlable() == null || this.objectmodel.getObjectlable().isEmpty()) {
 			textField.setText("Object" + " ID:" + id);
 		} else {
 			textField.setText(this.objectmodel.getObjectlable() + " " +id);
 		}
 		
 		// Object Typ wird gesetzt
 		if (this.objectmodel.getObjecttyp() == 0 ) {
 			setObjecttyp(-1);
 		} else {
 			setObjecttyp(this.objectmodel.getObjecttyp());
 		}
 		
 	}
 	
 	/** 
 	 * Write data in Datamodel.
 	 */
 	private void dataWrite() {
 		// Save Position
 		this.objectmodel.setObjectposition(this.getCenterPointGlobal());
 		// Save Color
 		this.objectmodel.setObjectcolor(this.getFillColor());
 		this.objectmodel.setObjectFillcolor(baserect.getFillColor());
 		this.objectmodel.setObjectlinecolor(this.getStrokeColor());
 		// Save Font (Not in use)
 	
 		// Save Directions
 		this.objectmodel.setDirection(updownrotate);
 		for (Attributes it : myAttributs ) {
 			it.dataWrite();
 		}
 		
 	}
 	
 	/** Position Dedection for MTObjects.*/
 	// TODO Postition Dedection
 	private void saveOnEvent() {
 		
 		this.addInputListener(new IMTInputEventListener() {
 			
 			public boolean processInputEvent(MTInputEvent inEvt) {
 			
 				if (inEvt instanceof AbstractCursorInputEvt) {
 					AbstractCursorInputEvt cursorInputEvt = (AbstractCursorInputEvt) inEvt;
 					
 						switch (cursorInputEvt.getId()) {
 						case AbstractCursorInputEvt.INPUT_STARTED:
 							
 							break;
 						case AbstractCursorInputEvt.INPUT_UPDATED:
 			
 							break;
 						case AbstractCursorInputEvt.INPUT_ENDED:
 							//System.out.println("Write Data after change the position");
 							dataWrite();
 							
 								for (Attributes it : myAttributs) {
 								it.dataWrite(); // daten der Attribute werden geschrieben.
 								}
 							
 							
 							break;
 						default:
 							break;
 						}
 					
 					
 				} else {
 					// handle other input events stuff
 				}
 				return false;
 			}
 		}
 		);
 		
 	}
 	
 	/**
 	 * Setzen des Object Namens. String als bergabe-Parameters.
 	 * @param titelname String
 	 */
 	public final void setTitelName(final String titelname) {
 	textField.setText(titelname);	
 	}
 	
 	/**
 	 * Zerstrt das Objekt.
 	 * 
 	 */
 	public final void destroyMyObject() {
 	this.destroy();
 	}
 	
 	/**
 	 * Get Object ID back.
 	 * @return id int
 	 */
 	public final int getID() {
 	return id;
 	}
 
 	/** 
 	 * Print the content from selected datamodel. 
 	 * 
 	 * @param obmodel ModelMtAttributs
 	 */
 	private void printMTObjectModel(final ModelMtObjects obmodel) {
 		
 		System.out.println("\n");
 		System.out.println("MyMTObject: \n");
 		System.out.println("Object ID:       " + obmodel.getId());
 		System.out.println("Object Color:    " + obmodel.getObjectcolor());
 		System.out.println("Object Font:     " + obmodel.getObjectfont());
 		System.out.println("Object Position: " + obmodel.getObjectposition());
 		System.out.println("Object Label     " + obmodel.getObjectlable());
 		System.out.println("Object Typ       " + obmodel.getObjecttyp());
 		//System.out.println("   Zoom:         " + attribut.getZoom());
 		//System.out.println("   Aussrichtung: " + attribut.isDirection());
 		
 		for (ModelMtAttributs attribut : obmodel.getObjectattributs()) {
 			System.out.println("   Attribut ID:  " + attribut.getId());
 			System.out.println("   Color:        " + attribut.getAttcolor());
 			System.out.println("   Label:        " + attribut.getLable());
 			
 			System.out.println("   Hight:        " + attribut.getAttributHight());
 			System.out.println("   MinMax:       " + attribut.isMinMax());
 			
 			int i = 0;
 			for (ModelAttributContent it : attribut.getAttributcontent()) {
 				System.out.println("              " + i + " Attr. Typ: " + it.getType());
 				System.out.println("              " + i + " Attr. Value: " + it.getValue());
 				i++;	
 			}
 			
 			/*
 			it j = 0;
 			for (ModelAttributDefinition at : attribut.getAttributdefinition()) {
 				System.out.println("              " + j + " Attr. Definiton: " + at.getDefinition());
 				System.out.println("              " + j + " Attr. Value: " + at.getValue());
 				j++;	
 			}
 			*/
 			
 		}
 		System.out.println("\n");
 	}
 	
 	/** Testmethode fr den LinkController. 
 	 * Set the Stroke Color for MyMTObject.
 	 * 
 	 * @param strokecolor MTColor
 	 * */ 
 	public void setTaggedColor(final MTColor strokecolor) {
 		System.out.println("MyMTObject: SET Tagged COLOR STROKE!!!!!!!");
 		befortaggedColor = this.getStrokeColor();
 		this.setStrokeColor(strokecolor);
 		this.setStrokeWeight(3);
 	}
 	
 	/** Testmethode fr den LinkController. 
 	 * Set the Stroke Color for MyMTObject.
 	 * 
 	 * */ 
 	public final void setNormalColor() {
 		System.out.println("MyMTObject: SET Normal COLOR STROKE!!!!!!!");
 		this.setStrokeColor(befortaggedColor);
 		this.setStrokeWeight(1);
 	}
 	
 	/** 
 	 * Get the flag if the object tagged.
 	 * 
 	 * @return tagged boolean
 	 * */
 	public final boolean getTagFlag() {
 		return tagged;
 	}
 	
 	/** Testmethode fr den LinkController. 
 	 * 
 	 * Set the Flag tag.
 	 * @param tag boolean
 	 * */
 	public final void setTagFlag(final boolean tag) {
 		tagged = tag;
 	}
 	
 	/* (non-Javadoc)
 	 * @see com.jMT.input.inputAnalyzers.clusterInputAnalyzer.IdragClusterable#isSelected()
 	 */
 	public boolean isSelected() {
 		return selected;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.jMT.input.inputAnalyzers.clusterInputAnalyzer.IdragClusterable#setSelected(boolean)
 	 */
 	public void setSelected(boolean selected) {
 		linker.setVisibleOne(!selected, id); // Verstecken der Linien, wenn Objekt im Lasso ist.
 		linker.drawLinie();
 		this.selected = selected;
 	}
 	
 	
 	/**
 	 * Setzt die DefaultGesture Action fr das Objekt. Dies durch die nderung in den Attributen.
 	 */
 	@Override
 	protected void setDefaultGestureActions() {
 //		super.setDefaultGestureActions();
 		
 		this.addGestureListener(DragProcessor.class, new InertiaDragAction());
 		
 		DragProcessor dp = new DragProcessor(getRenderer());
 //		dp.setLockPriority(0.5f);
 		registerInputProcessor(dp);
 		addGestureListener(DragProcessor.class, new DefaultDragAction());
 		dp.setBubbledEventsEnabled(true); 
 		
 		RotateProcessor rp = new RotateProcessor(getRenderer());
 //		rp.setLockPriority(0.8f);
 		registerInputProcessor(rp);
 		addGestureListener(RotateProcessor.class, new DefaultRotateAction());
 		rp.setBubbledEventsEnabled(true);  
 		
 		ScaleProcessor sp = new ScaleProcessor(getRenderer());
 //		sp.setLockPriority(0.8f);
 		registerInputProcessor(sp);
 		addGestureListener(ScaleProcessor.class, new DefaultScaleAction());
 		sp.setBubbledEventsEnabled(true);  
 	}
 
 	/**
 	 * 
 	 * Write Methode.
 	 * 
 	 * Wurde eine Linkparnter gefunden wird von der Methode componentDropped() 
 	 * 
 	 */
 	public void writeOutput() {
 		
 		System.out.println("MyMTObject: writeOutput:  Objekt gefunden ");
 		int node1 = new Integer(0);
 		int node2 = new Integer(0);
 	
 		for (int i = 0; i < this.droppedComponents.size(); i++) {
 	
 			try {
 				node1 = new Integer(Integer.valueOf((droppedComponents.get(i).getName())).intValue()) ; // Name der Linie die selbe ist 
 				node2 = new Integer(Integer.valueOf(this.getName()).intValue());
 				
 				linker.setSelectedObjectColor(node2, false, MTColor.RED);
 				linker.setSelectedObjectColor(node1, false, MTColor.RED);
 				
 					if ( !(node1 == node2)) { //Der Link wird nicht auf sich selbst ausgefhrt und zeigt auf ein anderes Objekt
 						linker.createLink(node1, node2);
 						droppedComponents.remove(i);
 					} else {
 						System.out.println("MyMTObject: Rekursive Link nicht mglich. ");
 					}
 				
 				} catch (NumberFormatException nfe) {
 				System.err.println("MyMTObject: Kann String nicht umwandeln. ");
 				}
 		}
 		
 		
 	}
 		
 	@Override
 	public void componentDropped(MTComponent droppedComponent, DragEvent de) {
 		if(!droppedComponents.contains(droppedComponent)){
 			this.droppedComponents.add(droppedComponent);
 		
 			if( !(this.getName().equalsIgnoreCase(droppedComponent.getName()))){
 				marker(this.getName() , droppedComponent.getName(), false); // Entfernen Markierung
 			}
 	
 		}
 		System.out.println("MyMTObject: componentDropped: " + this.getName() + ": " + droppedComponent.getName() + " dropped.");
 		
 		this.writeOutput();
 		
 		
 	}
 
 	/**
 	 * Bewegt sich das MTObjectLink auf ein anderes Objekt wird diese Methode aufgrufen.
 	 */
 	@Override
 	public void componentEntered(MTComponent enteredComponent) {
 		System.out.println("MyMTObject: componentEntered: " + this.getName() + ": " + enteredComponent.getName() + " entered.");
 		
 		if( !(this.getName().equalsIgnoreCase(enteredComponent.getName()))){
 			marker(this.getName() , enteredComponent.getName(), true);
 		}
 		
 		
 		//this.writeOutput();
 		
 	}
 
 	@Override
 	public void componentExited(MTComponent exitedComponent) {
 		
 		
 		if( !(this.getName().equalsIgnoreCase(exitedComponent.getName()))){
 			marker(this.getName(), exitedComponent.getName(), false);
 		}
 		this.droppedComponents.remove(exitedComponent);
 		
 		
 		System.out.println("MyMTObject: componentExited: " + this.getName() + ": " + exitedComponent.getName() + " exited.");
 		//this.writeOutput();
 		
 	}
 	
 	@Override
 	public boolean dndAccept(MTComponent component) {
 		// TODO Auto-generated method stub
 		return true;
 	}
 
 	/**
 	 * Markiert das Ausgewhlte Objekt ber den Linker Controller.
 	 * 
 	 * @param targetObj String
 	 * @param setting boolean 
 	 *  			1 = markieren
 	 *  			0 = nicht markiert
 	 */
 	private void marker(String targetObj, String startObj, boolean setting) {
 		
 		Integer idObj = new Integer(0);
 		Integer idSObj = new Integer(0);
 		try {
 			 idObj = Integer.valueOf(targetObj).intValue(); // Target Objekt
 			 idSObj = Integer.valueOf(startObj).intValue(); //  Start Objekts
 			
 				 if (!(idObj == idSObj)) {
 					 // Testen ob der Link erlaubt ist oder nicht, Farbliches Markieren
					 if(linker.isValidLinkRequest(idObj, idSObj)) {
 						 linker.setSelectedObjectColor(idObj, setting, MTColor.GREEN);
 					 } else {
 						 linker.setSelectedObjectColor(idObj, setting, MTColor.RED);
 					 }
 					
 					 System.out.println("MyMTObject: marker: des zu markierenden Objekts " + idObj + " StartObjekt ID:" + idSObj);
 				 } else {
 					 System.out.println("MyMTObject: marker: Markieren des eigenen  Objekts nicht mglich: " + idObj + " StartObjekt ID:" + idSObj);
 				 } 
 			} catch (NumberFormatException nfe) {
 			System.err.println("MyMTObject: marker: Kann Zahl nicht umwandeln. ");
 			}
 		
 	}
 
 	/**
 	 * 
 	 * Gibt des Objekttyp als Int Wert zurck.
 	 * 
 	 * @return int
 	 */
 	public final int getObjecttyp() {
 		return objecttyp;
 	}
 
 	/**
 	 * 
 	 * Setzen des Objekttyps.
 	 * 
 	 * @param objecttyp int
 	 */
 	private void setObjecttyp(final int objecttyp) {
 		this.objecttyp = objecttyp;
 	}
 
 	@Override
 	public void destroy() {
 		System.err.println("MyMTObject: destroy: Objekt wird gelscht.");
 	
 		
 		
 	}
 	
 }
 
