 package game.model;
 
 import game.model.Field.AllowedContent;
 
 import java.awt.Dimension;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.HashSet;
 
 /**
  * Model/Logik des Starfields
  * 
  * @author Alexander Arians
  * 
  */
 
 public class Starfield implements Serializable {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	ArrayList<ArrayList<Field>> listcontainer;
 	Boolean playable;
 	HashSet<Field> arrows;
 	HashSet<Field> stars;
 	HashSet<Field> allSolutionStars; // alle Sterne zur KI-berprfung
 	HashSet<Field> grayed;
 	Dimension size;
 	
 
 	public Starfield(int xNumber, int yNumber) {
 		size = new Dimension();
 		size.setSize(xNumber, yNumber);
 		listcontainer = new ArrayList<ArrayList<Field>>();
 		createStarfield(xNumber, yNumber);
 	}
 
 	public void createStarfield(int xNumber, int yNumber) {
 		for (int x = 0; x < xNumber; x++) {
 			ArrayList<Field> yList = new ArrayList<Field>();
 			for (int y = 0; y < yNumber; y++) {
 				yList.add(new Field(x, y));
 			}
 			listcontainer.add(yList);
 		}
 
 	}
 
 	public Starfield setPlayable(Boolean playable) {
 		this.playable = playable;
 		return this;
 	}
 
 	public boolean isPlayable() {
 		return playable;
 	}
 
 	public Field getField(int xCoord, int yCoord) {
 
 		return listcontainer.get(xCoord).get(yCoord);
 	}
 
 	/**
 	 * Diese Methode ndert die Gre des Starfields auf die bergebenen
 	 * Paramatergren
 	 * 
 	 * @param newXSize
 	 *            neue Anzahl an Spalten
 	 * @param newYSize
 	 *            neue Anzahl an Zeilen
 	 */
 	public Starfield changeSize(int newXSize, int newYSize) {
 
 		// berprfen ob sich die Anzahl an Spalten gendert hat.
 		if (newXSize != size.width) {
 			// Hat sich die Anzahl gendert wird berprft ob der Container
 			// breiter oder kleiner gemacht werden muss
 			if (newXSize > size.width) {
 				// Soll der Container breiter werden werden nun Spalten
 				// angehngt und mit leeren Feldern gefllt.
 				for (int i = size.width; i < newXSize; i++) {
 					ArrayList<Field> spalte = new ArrayList<Field>();
 					for (int row = 0; row < size.height; row++)
 						spalte.add(new Field(i, row));
 					listcontainer.add(spalte);
 				}
 			} else {
 				// Soll der Container schmaler werden, werden nun berschssige
 				// Spalten gelscht
 				for (int i = size.width; i > newXSize; i--) {
 					listcontainer.remove(i - 1);
 				}
 
 			}
 			// Nachdem der Container angepasst wurde, wird die neue size gesetzt
 			size.width = newXSize;
 		}
 		// berprfen ob sich die Anzahl an Zeilen gendert hat
 		if (newYSize != size.height) {
 			// Hat sich die Anzahl gendert wird berprft ob der Container
 			// lnger oder krzer gemacht werden soll
 			if (newYSize > size.height) {
 				// Soll der Container lnger werden, werden nun an jeden
 				// Spaltenvektor neue leere Field gehangen
 				for (int i = 0; i < size.width; i++) {
 					for (int column = size.height; column < newYSize; column++)
 						listcontainer.get(i).add(new Field(i, column));
 				}
 			} else {
 				// Soll der Container krzer werden, wird von jeder Spalte die
 				// Differenz an Zeilen gelscht
 				for (int i = 0; i < size.width; i++) {
 					for (int column = size.height; column > newYSize; column--)
 						listcontainer.get(i).remove(column - 1);
 				}
 			}
 			// Nachdem der Container angepasst wurde, wird die neue size gesetzt
 			size.height = newYSize;
 		}
 		return this;
 	}
 
 	// gibt die Größe des Spieldfeldes zurück
 	public Dimension getSize() {
 
 		return size;
 	}
 
 	/**
 	 * Zhlt die Sterne in einer Spalte
 	 */
 	public int getStarCountX(int column) {
 		int starcounter = 0;
 		for (int y = 0; y < size.height; y++) {
 			if (getField(column, y).getSolutionContent() == AllowedContent.CONTENT_STAR)
 				starcounter++;
 		}
 		return starcounter;
 	}
 
 	/**
 	 * Zhlt die Sterne in einer Reihe
 	 */
 	public int getStarCountY(int row) {
 		int starcounter = 0;
 		for (int x = 0; x < size.width; x++) {
 			if (getField(x, row).getSolutionContent() == AllowedContent.CONTENT_STAR)
 				starcounter++;
 		}
 		return starcounter;
 	}
 
 	/**
 	 * berprft die Eingaben des Users whrend des Spielens.
 	 */
 	public boolean checkSolution() {
 		boolean rightorwrong = true;
 		for (ArrayList<Field> list : listcontainer) {
 			for (Field field : list) {
 				if (field.getSolutionContent()!=AllowedContent.CONTENT_EMPTY) {
 					 if(field.getUserContent()==AllowedContent.CONTENT_GRAYED){
 						rightorwrong = false;
 					 }}
 					if( field.getUserContent() != (field.getSolutionContent())&&field.userContent!=AllowedContent.CONTENT_GRAYED){
 						rightorwrong = false;
 					}
 				
 
 			}
 		}
 
 		return rightorwrong;
 	}
 	
 	
 
 	public ArrayList<Field> getWrongFields() {
 		ArrayList<Field> wrongFields = new ArrayList<Field>();
 		for (ArrayList<Field> list : this.listcontainer) {
 			for (int i = 0; i < list.size(); i++) {
 				if (list.get(i).IsCurrentContentRight()) {
 					wrongFields.add(list.get(i));
 				}
 
 			}
 		}
 		return wrongFields;
 	}
 
 	/**
 	 * Diese Methode dient dazu, ein bereits gespeichertes Starfield erneut im
 	 * Editor zu bearbeiten. Damit der User die Lsung bearbeiten kann, werden
 	 * die Daten in den UserContent geladen. <br>
 	 * Diese Methode sollte nur direkt nach dem Laden eines Starfields aus einer
 	 * serialisierten Datei aufgerufen werden. Wenn das Starfield im Anschluss
 	 * im Editor bearbeitet werden soll.
 	 * 
 	 * @return - Das Starfield nach Bearbeitung
 	 */
 	public Starfield prepareUserContent(boolean forPlay) {
 		for (int y = 0; y < size.getHeight(); y++) {
 			for (int x = 0; x < size.getWidth(); x++) {
 				AllowedContent solutionContent = getField(x, y)
 						.getSolutionContent();
 				if (forPlay) {
 					if (solutionContent != AllowedContent.CONTENT_STAR)
 						getField(x, y).setUserContent(solutionContent);
 				} else
 					getField(x, y).setUserContent(solutionContent);
 			}
 		}
 		return this;
 	}
 
 	/**
 	 * Diese Methode dient dazu, vor der Speicherung eines neu Bearbeiteten
 	 * Puzzles den Inhalt, den der User eingegeben hat, als Lsung fr
 	 * zuknftige Spielsessions vorzugeben. <br>
 	 * Diese Methode sollte nur vor der Speicherung des Puzzles aufgerufen
 	 * werden, sofern die Korrektheit des Puzzles berprft worden ist.
 	 * 
 	 * @return - Das Starfield nach Bearbeitung
 	 */
 	public Starfield copyUserToSolutionContent() {
 		for (int y = 0; y < size.getHeight(); y++) {
 			for (int x = 0; x < size.getWidth(); x++) {
 				getField(x, y).setSolutionContent(
 						getField(x, y).getUserContent());
 			}
 		}
 		return this;
 	}
 
 	/**
 	 * Diese Methode dient dazu, nach dem Laden eines Starfields den UserContent
 	 * zu leeren, damit ein neues Spiel begonnen werden kann. <br>
 	 * Die Methode sollte nur direkt nach dem Laden eines Starfield aus der
 	 * serialisierten Datei aufgerufen werden.
 	 */
 	public Starfield clearUserContent() {
 		for (int y = 0; y < size.getHeight(); y++) {
 			for (int x = 0; x < size.getWidth(); x++) {
 				getField(x, y).setUserContent(AllowedContent.CONTENT_EMPTY);
 
 			}
 		}
 		return this;
 	}
 
 	/**
 	 * Diese Methode gibt eine ArrayList mit Argumenten zurck: An Pos. 0 Liste
 	 * mit Felder, auf die ein Pfeil zeigt oder an Pos 1-8 ArrayListen mit
 	 * Feldern um ein Stern rum in der jeweiligen Richtung.
 	 * (UL,U,UR,R,DR,D,DL,L)
 	 */
 	private ArrayList<ArrayList<Field>> getFieldsInDirectionFromStarOrArrow(
 			Field field) {
 		ArrayList<ArrayList<Field>> args = new ArrayList<ArrayList<Field>>();
 		ArrayList<Field> pointedFieldsFromArrow = new ArrayList<Field>();
 		args.add(pointedFieldsFromArrow);
 		ArrayList<Field> pointedFieldsFromStarUL = new ArrayList<Field>();
 		ArrayList<Field> pointedFieldsFromStarU = new ArrayList<Field>();
 		ArrayList<Field> pointedFieldsFromStarUR = new ArrayList<Field>();
 		ArrayList<Field> pointedFieldsFromStarR = new ArrayList<Field>();
 		ArrayList<Field> pointedFieldsFromStarDR = new ArrayList<Field>();
 		ArrayList<Field> pointedFieldsFromStarD = new ArrayList<Field>();
 		ArrayList<Field> pointedFieldsFromStarDL = new ArrayList<Field>();
 		ArrayList<Field> pointedFieldsFromStarL = new ArrayList<Field>();
 		args.add(pointedFieldsFromStarUL);
 		args.add(pointedFieldsFromStarU);
 		args.add(pointedFieldsFromStarUR);
 		args.add(pointedFieldsFromStarR);
 		args.add(pointedFieldsFromStarDR);
 		args.add(pointedFieldsFromStarD);
 		args.add(pointedFieldsFromStarDL);
 		args.add(pointedFieldsFromStarL);
 		switch (field.getUserContent()) {
 		case CONTENT_STAR:
 			Field field2 = field;
 			while (!(getField_UL(field2) == null)) {
 				field2 = getField_UL(field2);
 				pointedFieldsFromStarUL.add(field2);
 			}
 			field2 = field;
 			while (!(getField_U(field2) == (null))) {
 				field2 = getField_U(field2);
 				pointedFieldsFromStarU.add(field2);
 			}
 			field2 = field;
 			while (!(getField_UR(field2) == (null))) {
 				field2 = getField_UR(field2);
 				pointedFieldsFromStarUR.add(field2);
 			}
 			field2 = field;
 			while (!(getField_L(field2) == (null))) {
 				field2 = getField_L(field2);
 				pointedFieldsFromStarL.add(field2);
 			}
 			field2 = field;
 			while (!(getField_R(field2) == (null))) {
 				field2 = getField_R(field2);
 				pointedFieldsFromStarR.add(field2);
 			}
 			field2 = field;
 			while (!(getField_DL(field2) == (null))) {
 				field2 = getField_DL(field2);
 				pointedFieldsFromStarDL.add(field2);
 			}
 			field2 = field;
 			while (!(getField_D(field2) == (null))) {
 				field2 = getField_D(field2);
 				pointedFieldsFromStarD.add(field2);
 			}
 			field2 = field;
 			while (!(getField_DR(field2) == (null))) {
 				field2 = getField_DR(field2);
 				pointedFieldsFromStarDR.add(field2);
 			}
 			break;
 		case CONTENT_EMPTY:
 			break;
 		case CONTENT_GRAYED:
 			break;
 		case CONTENT_ARROW_UL:
 			while (!(getField_UL(field) == (null))) {
 				field = getField_UL(field);
 				pointedFieldsFromArrow.add(field);
 			}
 			break;
 		case CONTENT_ARROW_U:
 			while (!(getField_U(field) == (null))) {
 				field = getField_U(field);
 				pointedFieldsFromArrow.add(field);
 			}
 			break;
 		case CONTENT_ARROW_UR:
 			while (!(getField_UR(field) == (null))) {
 				field = getField_UR(field);
 				pointedFieldsFromArrow.add(field);
 			}
 			break;
 		case CONTENT_ARROW_L:
 			while (!(getField_L(field) == (null))) {
 				field = getField_L(field);
 				pointedFieldsFromArrow.add(field);
 			}
 			break;
 		case CONTENT_ARROW_R:
 			while (!(getField_R(field) == (null))) {
 				field = getField_R(field);
 				pointedFieldsFromArrow.add(field);
 			}
 			break;
 		case CONTENT_ARROW_DL:
 			while (!(getField_DL(field) == (null))) {
 				field = getField_DL(field);
 				pointedFieldsFromArrow.add(field);
 			}
 			break;
 		case CONTENT_ARROW_D:
 			while (!(getField_D(field) == (null))) {
 				field = getField_D(field);
 				pointedFieldsFromArrow.add(field);
 			}
 			break;
 		case CONTENT_ARROW_DR:
 			while (!(getField_DR(field) == (null))) {
 				field = getField_DR(field);
 				pointedFieldsFromArrow.add(field);
 			}
 			break;
 		}
 
 		return args;
 	}
 
 	/**
 	 * Gibt das Feld linksoben vom bergebenen Feld aus.
 	 * 
 	 * @return
 	 */
 	public Field getField_UL(Field field) {
 		if (field.getxPos() > 0 && field.getyPos() > 0) {
 			return listcontainer.get(field.getxPos() - 1).get(
 					field.getyPos() - 1);
 		}
 		return null;
 	}
 
 	/**
 	 * Gibt das Feld oben vom bergebenen Feld aus.
 	 * 
 	 * @return
 	 */
 	public Field getField_U(Field field) {
 		if (field.getyPos() > 0) {
 			return listcontainer.get(field.getxPos()).get(field.getyPos() - 1);
 		}
 		return null;
 	}
 
 	/**
 	 * Gibt das Feld rechtsoben vom bergebenen Feld aus.
 	 * 
 	 * @return
 	 */
 	public Field getField_UR(Field field) {
 		if (field.getxPos() < (int) size.getSize().getWidth() - 1
 				&& field.getyPos() > 0) {
 			return listcontainer.get(field.getxPos() + 1).get(
 					field.getyPos() - 1);
 		}
 		return null;
 	}
 
 	/**
 	 * Gibt das Feld rechts vom bergebenen Feld aus.
 	 * 
 	 * @return
 	 */
 	public Field getField_R(Field field) {
 		if (field.getxPos() < (int) size.getSize().getWidth() - 1) {
 			return listcontainer.get(field.getxPos() + 1).get(field.getyPos());
 		}
 		return null;
 	}
 
 	/**
 	 * Gibt das Feld rechtsunten vom bergebenen Feld aus.
 	 * 
 	 * @return
 	 */
 	public Field getField_DR(Field field) {
 		if (field.getxPos() < (int) size.getSize().getWidth() - 1
 				&& field.getyPos() < (int) size.getSize().getHeight() - 1) {
 			return listcontainer.get(field.getxPos() + 1).get(
 					field.getyPos() + 1);
 		}
 		return null;
 	}
 
 	/**
 	 * Gibt das Feld unten vom bergebenen Feld aus.
 	 * 
 	 * @return
 	 */
 	public Field getField_D(Field field) {
 		if (field.getyPos() < (int) size.getSize().getHeight() - 1) {
 			return listcontainer.get(field.getxPos()).get(field.getyPos() + 1);
 		}
 		return null;
 	}
 
 	/**
 	 * Gibt das Feld linksunten vom bergebenen Feld aus.
 	 * 
 	 * @return
 	 */
 	public Field getField_DL(Field field) {
 		if (field.getxPos() > 0
 				&& field.getyPos() < (int) size.getSize().getHeight() - 1) {
 			return listcontainer.get(field.getxPos() - 1).get(
 					field.getyPos() + 1);
 		}
 		return null;
 	}
 
 	/**
 	 * Gibt das Feld links vom bergebenen Feld aus.
 	 * 
 	 * @return
 	 */
 	public Field getField_L(Field field) {
 		if (field.getxPos() > 0) {
 			return listcontainer.get(field.getxPos() - 1).get(field.getyPos());
 		}
 		return null;
 	}
 
 	/**
 	 * Testet ob ein Stern in einer Reine/ Arrayliste liegt.
 	 * 
 	 * @return
 	 */
 	private Boolean ContainsStar(ArrayList<Field> directionFieldList) {
 		for (Field f : directionFieldList) {
 			if (f.getUserContent().equals(AllowedContent.CONTENT_STAR)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Testet ob der Stern von einem entsprechenden Arrow getroffen wird.
 	 * 
 	 * @return
 	 */
 	private Boolean IsHitByArrow(ArrayList<Field> FieldList,
 			AllowedContent content) {
 		switch (content) {
 		case CONTENT_ARROW_UL:
 			for (Field f : FieldList) {
 				if (f.getUserContent().equals(AllowedContent.CONTENT_ARROW_UL)) {
 					return true;
 				}
 			}
 			break;
 		case CONTENT_ARROW_U:
 			for (Field f : FieldList) {
 				if (f.getUserContent().equals(AllowedContent.CONTENT_ARROW_U)) {
 					return true;
 				}
 			}
 			break;
 		case CONTENT_ARROW_UR:
 			for (Field f : FieldList) {
 				if (f.getUserContent().equals(AllowedContent.CONTENT_ARROW_UR)) {
 					return true;
 				}
 			}
 			break;
 		case CONTENT_ARROW_L:
 			for (Field f : FieldList) {
 				if (f.getUserContent().equals(AllowedContent.CONTENT_ARROW_L)) {
 					return true;
 				}
 			}
 			break;
 		case CONTENT_ARROW_R:
 			for (Field f : FieldList) {
 				if (f.getUserContent().equals(AllowedContent.CONTENT_ARROW_R)) {
 					return true;
 				}
 			}
 			break;
 		case CONTENT_ARROW_DL:
 			for (Field f : FieldList) {
 				if (f.getUserContent().equals(AllowedContent.CONTENT_ARROW_DL)) {
 					return true;
 				}
 			}
 			break;
 		case CONTENT_ARROW_D:
 			for (Field f : FieldList) {
 				if (f.getUserContent().equals(AllowedContent.CONTENT_ARROW_D)) {
 					return true;
 				}
 			}
 			break;
 		case CONTENT_ARROW_DR:
 			for (Field f : FieldList) {
 				if (f.getUserContent().equals(AllowedContent.CONTENT_ARROW_DR)) {
 					return true;
 				}
 			}
 			break;
 
 		}
 		return false;
 	}
 
 	public HashSet<Field> checkPlayable() {
 		HashSet<Field> wrongFields = new HashSet<Field>();
 		ArrayList<ArrayList<Field>> args = new ArrayList<ArrayList<Field>>();
 		for (int x = 0; x < size.getWidth(); x++) {
 			for (int y = 0; y < size.getHeight(); y++) {
 				args = getFieldsInDirectionFromStarOrArrow(listcontainer.get(x)
 						.get(y)); // Rckgabe der Argument ArrayListen
 				if (!(args.get(0).isEmpty() && args.get(3).isEmpty() && args
 						.get(6).isEmpty())) { // leeres Feld !!!
 					if (args.get(3).isEmpty() && args.get(6).isEmpty()) {// Pfeil
 						if (!ContainsStar(args.get(0))) { // Checkt ob der Pfeil
 															// auf einen Stern
 															// zeigt
							wrongFields.add(getField(x, y));							
 						}
 					} else {
 						if (!(IsHitByArrow(args.get(1),
 								AllowedContent.CONTENT_ARROW_DR))
 								&& !(IsHitByArrow(args.get(2),
 										AllowedContent.CONTENT_ARROW_D))
 								&& !(IsHitByArrow(args.get(3),
 										AllowedContent.CONTENT_ARROW_DL))
 								&& !(IsHitByArrow(args.get(4),
 										AllowedContent.CONTENT_ARROW_L))
 								&& !(IsHitByArrow(args.get(5),
 										AllowedContent.CONTENT_ARROW_UL))
 								&& !(IsHitByArrow(args.get(6),
 										AllowedContent.CONTENT_ARROW_U))
 								&& !(IsHitByArrow(args.get(7),
 										AllowedContent.CONTENT_ARROW_UR))
 								&& !(IsHitByArrow(args.get(8),
 										AllowedContent.CONTENT_ARROW_R))) {
 							wrongFields.add(getField(x, y));
 						}
 					}
 
 				}
 			}
 		}
 
 		return wrongFields;
 
 	}
 
 	/**
 	 * Falls die Reihe keine Sterne besitzt, setze sie in das HashSet grayed.
 	 */
 	private void setFieldsGrayInRowIfNoStars(int row) {
 		if (getStarCountY(row) == 0) {
 			for (int x = 0; x < listcontainer.size(); x++)
 				if (listcontainer.get(x).get(row).solutionContent == AllowedContent.CONTENT_EMPTY) {
 					grayed.add(listcontainer.get(x).get(row));
 				}
 
 		}
 	}
 
 	/**
 	 * Falls die Spalte keine Sterne besitzt, setze sie in das HashSet grayed.
 	 */
 	private void setFieldsGrayInColumnIfNoStars(int column) {
 		if (getStarCountX(column) == 0) {
 			for (int y = 0; y < listcontainer.get(column).size(); y++)
 				if (listcontainer.get(column).get(y).solutionContent == AllowedContent.CONTENT_EMPTY) {
 					grayed.add(listcontainer.get(column).get(y));
 				}
 		}
 	}
 
 	/**
 	 * Speichert alle Felder, auf die kein Arrow zeigt grau. Auerdem werden
 	 * alle Sterne in allSoulitionStars gespeichert und alle Arrows im Hashset
 	 * arrows.
 	 */
 	private void setAllFieldsGrayNoArrowPointed() {
 		for (ArrayList<Field> list : listcontainer) {
 			for (Field field : list) {
 				setAllSolutionStars(field);
 				setArrowInHashSet(field);
 				setFieldGrayNoArrowPointed(field);
 			}
 		}
 	}
 
 	/**
 	 * Setzt falls ein Stern vorhanden ist ins allSolutionStars Hashset.
 	 */
 	private void setAllSolutionStars(Field field) {
 		if (field.solutionContent == AllowedContent.CONTENT_STAR) {
 			allSolutionStars.add(field);
 		}
 
 	}
 
 	/**
 	 * Guckt ob auf ein "nicht graues" Feld, Arrows zeigen.
 	 */
 	private void setFieldGrayNoArrowPointed(Field field) {
 		if (!grayed.contains(field) && !arrows.contains(field)) { // Feld
 																	// bereits
 																	// ausgegraut
 
 			if (!isFieldHitByArrow(field)) { // teste ob ein Arrow aufs Feld
 												// zeigt
 				grayed.add(field); // auf das Feld zeigt kein Arrow > grau flag
 			}
 		}
 	}
 
 	/**
 	 * berprft ob das Feld von einem passenden Pfeil getroffen wird
 	 * 
 	 * @return boolean isHit
 	 */
 	private boolean isFieldHitByArrow(Field field) {
 		Field outputField = field;
 		boolean isHit = false;
 		while (!(getField_UL(field) == (null))) { // linksoben checken ob ein
 													// passender Pfeil vorhanden
 													// ist
 			field = getField_UL(field);
 			if (field.getSolutionContent() == AllowedContent.CONTENT_ARROW_DR) {
 				isHit = true;
 			}
 		}
 		field = outputField;
 		while (!(getField_U(field) == (null))) { // oben checken ob ein
 													// passender Pfeil vorhanden
 													// ist
 			field = getField_U(field);
 			if (field.getSolutionContent() == AllowedContent.CONTENT_ARROW_D) {
 				isHit = true;
 			}
 		}
 		field = outputField;
 		while (!(getField_UR(field) == (null))) { // rechtsoben checken ob ein
 													// passender Pfeil vorhanden
 													// ist
 			field = getField_UR(field);
 			if (field.getSolutionContent() == AllowedContent.CONTENT_ARROW_DL) {
 				isHit = true;
 			}
 		}
 		field = outputField;
 		while (!(getField_R(field) == (null))) { // rechts checken ob ein
 													// passender Pfeil vorhanden
 													// ist
 			field = getField_R(field);
 			if (field.getSolutionContent() == AllowedContent.CONTENT_ARROW_L) {
 				isHit = true;
 			}
 		}
 		field = outputField;
 		while (!(getField_DR(field) == (null))) { // rechtsunten checken ob ein
 													// passender Pfeil vorhanden
 													// ist
 			field = getField_DR(field);
 			if (field.getSolutionContent() == AllowedContent.CONTENT_ARROW_UL) {
 				isHit = true;
 			}
 		}
 		field = outputField;
 		while (!(getField_D(field) == (null))) { // unten checken ob ein
 													// passender Pfeil vorhanden
 													// ist
 			field = getField_D(field);
 			if (field.getSolutionContent() == AllowedContent.CONTENT_ARROW_U) {
 				isHit = true;
 			}
 		}
 		field = outputField;
 		while (!(getField_DL(field) == (null))) { // linksunten checken ob ein
 													// passender Pfeil vorhanden
 													// ist
 			field = getField_DL(field);
 			if (field.getSolutionContent() == AllowedContent.CONTENT_ARROW_UR) {
 				isHit = true;
 			}
 		}
 		field = outputField;
 		while (!(getField_L(field) == (null))) { // links checken ob ein
 													// passender Pfeil vorhanden
 													// ist
 			field = getField_L(field);
 			if (field.getSolutionContent() == AllowedContent.CONTENT_ARROW_R) {
 				isHit = true;
 			}
 
 		}
 
 		return isHit;
 	}
 
 	/**
 	 * Gibt leere Felder und Sterne in einer Reihe zurck und speichert .
 	 */
 	private HashSet<Field> getEmptyFieldsInRow(int row) {
 		HashSet<Field> fields = new HashSet<Field>();
 		for (int x = 0; x < size.width; x++) {
 			Field currentField = getField(x, row);
 			if (!(grayed.contains(currentField) || arrows
 					.contains(currentField))) {
 				if (currentField.getSolutionContent() == AllowedContent.CONTENT_EMPTY
 						|| currentField.getSolutionContent() == AllowedContent.CONTENT_STAR) { // Empty
 					fields.add(currentField);
 				}
 			}
 		}
 		return fields;
 	}
 
 	/**
 	 * Gibt leere und Sternen Felder in einer Spalte zurck.
 	 */
 	private HashSet<Field> getEmptyFieldsInColumn(int column) {
 		HashSet<Field> fields = new HashSet<Field>();
 		for (int y = 0; y < size.height; y++) {
 			Field currentField = getField(column, y);
 			if (!(grayed.contains(currentField) && !(arrows
 					.contains(currentField)))) { // Feldinhalt weder grau noch
 													// Arrow
 				if (currentField.getSolutionContent() == AllowedContent.CONTENT_EMPTY
 						|| currentField.getSolutionContent() == AllowedContent.CONTENT_STAR) { // Empty
 					fields.add(currentField);
 				}
 			}
 		}
 
 		return fields;
 	}
 
 	/**
 	 * Falls in einer Reihe oder Spalte die Anzahl der Soll-Sterne den freien
 	 * Felder(inkl. vorhandene Sterne) gleicht setze dortin Sterne.
 	 */
 	private void setStarCountEqualEmptyFields() {
 		for (int x = 0; x < size.width; x++) { // Spalte
 			if (getEmptyFieldsInColumn(x).size() == getStarCountX(x)) { // freie
 																		// Felder
 																		// =
 																		// Anzahl
 																		// Soll-Sterne
 				for (Field field : getEmptyFieldsInColumn(x)) { // setze Sterne
 					if (field.getSolutionContent() == AllowedContent.CONTENT_EMPTY
 							|| field.getSolutionContent() == AllowedContent.CONTENT_STAR) {
 						if (!stars.contains(field)) { // falls noch kein Stern
 														// vorhanden ist
 							stars.add(field); // fgt Stern der Hashmap zu
 						}
 					}
 				}
 			}
 		}
 		for (int y = 0; y < size.height; y++) { // Reihe
 			if (getEmptyFieldsInRow(y).size() == getStarCountY(y)) { // freie
 																		// Felder
 																		// =
 																		// Anzahl
 																		// Soll-Sterne
 				for (Field field : getEmptyFieldsInRow(y)) { // setze Sterne
 					if (field.getSolutionContent() == AllowedContent.CONTENT_EMPTY
 							|| field.getSolutionContent() == AllowedContent.CONTENT_STAR) {
 						if (!stars.contains(field)) { // falls noch kein Stern
 														// vorhanden ist
 							stars.add(field); // fgt Stern der Hashmap zu
 						}
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Falls in einer Reihe alle Sterne gesetzt sind. Setze restlichen Felder
 	 * grau.
 	 */
 	private void fillFullRowGray(int row) {
 		int rowStarCount = 0; // Anzahl gesetzte Sterne
 		for (int x = 0; x < size.width; x++) {
 			if (stars.contains(listcontainer.get(x).get(row))) { // zhle
 																	// gesetzte
 																	// Sterne
 				rowStarCount = rowStarCount + 1;
 			}
 
 		}
 		if (getStarCountY(row) == rowStarCount) { // Sternanzahl == gesetzte
 													// Sterne : Rest kann grau
 			for (int x = 0; x < size.width; x++) {
 				if (!(stars.contains(listcontainer.get(x).get(row)))
 						&& !(arrows.contains(listcontainer.get(x).get(row)))
 						&& !(grayed.contains(listcontainer.get(x).get(row)))) { // Feld
 																				// weder
 																				// Stern,Arrow
 																				// noch
 																				// grau.
 					grayed.add(listcontainer.get(x).get(row)); // setze grau
 				}
 			}
 		}
 	}
 
 	/**
 	 * Falls in einer Spalte alle Sterne gesetzt sind. Setze restlichen Felder
 	 * grau.
 	 */
 	private void fillFullColumnGray(int column) {
 		int rowStarCount = 0; // Anzahl gesetzte Sterne
 		for (int y = 0; y < size.height; y++) {
 			if (stars.contains(listcontainer.get(column).get(y))) { // zhle
 																	// gesetzte
 																	// Sterne
 				rowStarCount = rowStarCount + 1; //
 			}
 
 		}
 		if (getStarCountX(column) == rowStarCount) { // Sternanzahl == gesetzte
 														// Sterne : Rest kann
 														// grau
 			for (int y = 0; y < size.height; y++) {
 				if (!(stars.contains(listcontainer.get(column).get(y)))
 						&& !(arrows.contains(listcontainer.get(column).get(y)))
 						&& !(grayed.contains(listcontainer.get(column).get(y)))) { // //
 																					// Feld
 																					// weder
 																					// Stern,Arrow
 																					// noch
 																					// grau.
 					grayed.add(listcontainer.get(column).get(y)); // setze grau
 				}
 
 			}
 		}
 	}
 
 	/**
 	 * berprft ob die AI das Rtsel fertig gelst hat.
 	 */
 	private boolean isAIFinished() {
 		return stars.containsAll(allSolutionStars);
 	}
 
 	/**
 	 * berprfe ob Arrow, falls ja speichere in Arrowliste.
 	 */
 	private void setArrowInHashSet(Field field) {
 		if (!(field.getSolutionContent() == AllowedContent.CONTENT_STAR)
 				&& !(field.getSolutionContent() == AllowedContent.CONTENT_EMPTY)) {
 			arrows.add(field);
 		}
 	}
 
 	/**
 	 * berprft die Schwierigkeit und gibt sie aus.
 	 */
 	public String checkDifficulty() {
 		if (playable) {
			grayed = new HashSet<Field>();
			stars = new HashSet<Field>();
			arrows = new HashSet<Field>();
			allSolutionStars = new HashSet<Field>();
 			int aiCount = 0;
 			copyUserToSolutionContent();
 			setFieldsGrayIfNoStars(); // alles grau, wo 0 drber
 			setAllFieldsGrayNoArrowPointed(); // alles grau, wo kein Pfeil drauf
 												// & Pfeile+LsungsSterne setzen
 			System.out.println("Anzahl Pfeile: " + arrows.size()
 					+ " Anzahl Sterne: " + stars.size() + "Anzahl Grayed: "
 					+ grayed.size());
 
 			// Schleife
 			while ((!isAIFinished()) && aiCount < 100) {
 				setStarCountEqualEmptyFields(); // freie Pltze = Anzahl Sterne
 												// > setze Sterne
 				fillFullRowColumnGray(); // setzt falls alle Sterne gesetzt
 											// wurden Restfelder Grau
 				checkStarfieldforDiagonalsStars(); // Richtung vom Pfeil nur 1
 													// brig und bisher kein
 													// Stern
 				checkObviousFieldsWhereNoArrowsInRowColumn();// Checkt ob hinter
 																// einem geraden
 																// Pfeil(u,d,l,r)
 																// kein Stern
 																// mehr soll.
 				System.out.println(aiCount + " Anzahl Pfeile: " + arrows.size()
 						+ " Anzahl Sterne: " + stars.size() + "Anzahl Grayed: "
 						+ grayed.size());
 				aiCount = aiCount + 1;
 
 			}
 			if (aiCount < 1) {
 				return "Easy";
 			} else if (aiCount < 99) {
 				return "Hard";
 			} else {
 				setPlayable(false); // darf nicht speicher bzw. spielbar sein!
 				return "Nicht eindeutig";
 			}
 		}
 		return " ";
 	}
 
 	private void fillFullRowColumnGray() {
 		for (int x = 0; x < size.getWidth(); x++) {
 			fillFullColumnGray(x);
 		}
 		for (int y = 0; y < size.getHeight(); y++) {
 			fillFullRowGray(y);
 		}
 	}
 
 	/**
 	 * Setzt Reihen+Spalten mit einer 0 komplett grau.
 	 */
 	private void setFieldsGrayIfNoStars() {
 		for (int x = 0; x < size.getWidth(); x++) {
 			setFieldsGrayInColumnIfNoStars(x);
 		}
 		for (int y = 0; y < size.getHeight(); y++) {
 			setFieldsGrayInRowIfNoStars(y);
 		}
 
 	}
 
 	/**
 	 * berprft das Starfield ob in der Diagonalen von den Pfeilen aus sich nur
 	 * noch ein freies Feld befindet und noch kein Stern gesetzt wurde.
 	 */
 	private void checkStarfieldforDiagonalsStars() {
 		for (ArrayList<Field> list : listcontainer) {
 			for (Field field : list) {
 				if (!(field.getSolutionContent() == AllowedContent.CONTENT_EMPTY)
 						&& !(field.getSolutionContent() == AllowedContent.CONTENT_STAR)) {
 					checkDiagonalForStars(field);
 				}
 			}
 		}
 	}
 
 	/**
 	 * berprft ob in der Diagonalen vom Pfeil aus sich nur noch ein freies
 	 * Feld befindet und noch kein Stern gesetzt wurde.
 	 */
 	private void checkDiagonalForStars(Field field) {
 		int emptyCount = 0;
 		Boolean starFound = false;
 		Field fieldwithstar = new Field(0, 0);
 
 		for (Field f : getEmptyFieldsInDiagonalDirection(field)) {
 			if (stars.contains(f)) { // Falls Stern schon vorhanden
 				starFound = true;
 			}
 			if (!grayed.contains(f)) { // Falls Feld leer (bzw kein graues)
 				emptyCount = emptyCount + 1;
 				fieldwithstar = f;
 
 			}
 		}
 
 		if (emptyCount == 1 && !starFound) { // Falls nur 1 freies Feld
 			stars.add(fieldwithstar);
 		}
 	}
 
 	private HashSet<Field> getEmptyFieldsInDiagonalDirection(Field field) {
 		switch (field.getSolutionContent()) {
 		case CONTENT_ARROW_UL:
 			return getEmtpyFieldsInADiagonalUL(field);
 		case CONTENT_ARROW_U:
 			return getEmtpyFieldsInADiagonalU(field);
 		case CONTENT_ARROW_UR:
 			return getEmtpyFieldsInADiagonalUR(field);
 		case CONTENT_ARROW_R:
 			return getEmtpyFieldsInADiagonalR(field);
 		case CONTENT_ARROW_DR:
 			return getEmtpyFieldsInADiagonalDR(field);
 		case CONTENT_ARROW_D:
 			return getEmtpyFieldsInADiagonalD(field);
 		case CONTENT_ARROW_DL:
 			return getEmtpyFieldsInADiagonalDL(field);
 		case CONTENT_ARROW_L:
 			return getEmtpyFieldsInADiagonalL(field);
 		default:
 			return new HashSet<Field>();
 		}
 	}
 
 	/**
 	 * Gibt die freien Felder(Star+Empty) in der Diagonalen aus.
 	 */
 	private HashSet<Field> getEmtpyFieldsInADiagonalUL(Field field) {
 		HashSet<Field> fields = new HashSet<Field>();
 		while (!(getField_UL(field) == (null))) {
 			field = getField_UL(field);
 			if (field.getSolutionContent() == AllowedContent.CONTENT_EMPTY
 					&& field.getSolutionContent() == AllowedContent.CONTENT_STAR) {
 				fields.add(field);
 			}
 		}
 		return fields;
 	}
 
 	private HashSet<Field> getEmtpyFieldsInADiagonalU(Field field) {
 		HashSet<Field> fields = new HashSet<Field>();
 		while (!(getField_U(field) == (null))) {
 			field = getField_U(field);
 			if (field.getSolutionContent() == AllowedContent.CONTENT_EMPTY
 					&& field.getSolutionContent() == AllowedContent.CONTENT_STAR) {
 				fields.add(field);
 			}
 		}
 		return fields;
 	}
 
 	private HashSet<Field> getEmtpyFieldsInADiagonalUR(Field field) {
 		HashSet<Field> fields = new HashSet<Field>();
 		while (!(getField_UR(field) == (null))) {
 			field = getField_UR(field);
 			if (field.getSolutionContent() == AllowedContent.CONTENT_EMPTY
 					&& field.getSolutionContent() == AllowedContent.CONTENT_STAR) {
 				fields.add(field);
 			}
 		}
 		return fields;
 	}
 
 	private HashSet<Field> getEmtpyFieldsInADiagonalR(Field field) {
 		HashSet<Field> fields = new HashSet<Field>();
 		while (!(getField_R(field) == (null))) {
 			field = getField_R(field);
 			if (field.getSolutionContent() == AllowedContent.CONTENT_EMPTY
 					|| field.getSolutionContent() == AllowedContent.CONTENT_STAR) {
 				fields.add(field);
 			}
 		}
 		return fields;
 	}
 
 	private HashSet<Field> getEmtpyFieldsInADiagonalDR(Field field) {
 		HashSet<Field> fields = new HashSet<Field>();
 		while (!(getField_DR(field) == (null))) {
 			field = getField_DR(field);
 			if (field.getSolutionContent() == AllowedContent.CONTENT_EMPTY
 					&& field.getSolutionContent() == AllowedContent.CONTENT_STAR) {
 				fields.add(field);
 			}
 		}
 		return fields;
 	}
 
 	private HashSet<Field> getEmtpyFieldsInADiagonalD(Field field) {
 		HashSet<Field> fields = new HashSet<Field>();
 		while (!(getField_D(field) == (null))) {
 			field = getField_D(field);
 			if (field.getSolutionContent() == AllowedContent.CONTENT_EMPTY
 					&& field.getSolutionContent() == AllowedContent.CONTENT_STAR) {
 				fields.add(field);
 			}
 		}
 		return fields;
 	}
 
 	private HashSet<Field> getEmtpyFieldsInADiagonalDL(Field field) {
 		HashSet<Field> fields = new HashSet<Field>();
 		while (!(getField_DL(field) == (null))) {
 			field = getField_DL(field);
 			if (field.getSolutionContent() == AllowedContent.CONTENT_EMPTY
 					&& field.getSolutionContent() == AllowedContent.CONTENT_STAR) {
 				fields.add(field);
 			}
 		}
 		return fields;
 	}
 
 	private HashSet<Field> getEmtpyFieldsInADiagonalL(Field field) {
 		HashSet<Field> fields = new HashSet<Field>();
 		while (!(getField_L(field) == (null))) {
 			field = getField_L(field);
 			if (field.getSolutionContent() == AllowedContent.CONTENT_EMPTY
 					&& field.getSolutionContent() == AllowedContent.CONTENT_STAR) {
 				fields.add(field);
 			}
 		}
 		return fields;
 	}
 
 	/**
 	 * Guckt ob in einer Reihe nur noch 1 Stern hinkommt und ein Pfeil gerade in
 	 * der Reihe positioniert ist. Graut die sich dahinter befindenen aus.
 	 */
 	private void checkObviousFieldsWhereNoArrowsInRowColumn() {
 		for (ArrayList<Field> list : listcontainer) {
 			for (Field field : list) {
 				switch (field.getSolutionContent()) {
 				case CONTENT_ARROW_U:
 					setFieldsGrayDownToTheArrow(field);
 					break;
 				case CONTENT_ARROW_R:
 					setFieldsGrayLeftToTheArrow(field);
 					break;
 				case CONTENT_ARROW_D:
 					setFieldsGrayUpToTheArrow(field);
 					break;
 				case CONTENT_ARROW_L:
 					setFieldsGrayRightToTheArrow(field);
 					break;
 
 				}
 			}
 
 		}
 	}
 
 	/**
 	 * Setzt Felder unterhalb eines nach obenzeigenden Pfeiles grau, wenn er
 	 * Gesamtzahl - Darunterpfeile=1.
 	 */
 	private void setFieldsGrayDownToTheArrow(Field field) {
 		int starCount = 0;
 		for (Field f : getEmtpyFieldsInADiagonalD(field)) { // holt
 															// leere+sternenfelder
 															// unter
 															// dem
 															// Pfeil
 			if (stars.contains(f)) {
 				starCount = starCount + 1; // Sterne unter dem Pfeil
 			}
 		}
 		if (getStarCountX(field.getxPos()) - starCount == 1) {
 			// Gesamtzahl
 			// -
 			// drunter
 			// =
 			// 1
 			// setzte
 			// alle
 			// darunter
 			// grau.
 			for (Field f : getEmtpyFieldsInADiagonalD(field)) { // holt
 																// leere+sternenfelder
 																// unter
 																// dem
 																// Pfeil
 				if (!arrows.contains(f) && !stars.contains(f)) {
 					grayed.add(f);
 				}
 			}
 		}
 
 	}
 
 	/**
 	 * Setzt Felder oberhalb eines nach untenzeigenden Pfeiles grau, wenn er
 	 * Gesamtzahl - Darunterpfeile=1.
 	 */
 	private void setFieldsGrayUpToTheArrow(Field field) {
 		int starCount = 0;
 		for (Field f : getEmtpyFieldsInADiagonalU(field)) { // holt
 			// leere+sternenfelder
 			// ber
 			// dem
 			// Pfeil
 			if (stars.contains(f)) {
 				starCount = starCount + 1; // Sterne ber dem Pfeil
 			}
 		}
 		if (getStarCountX(field.getxPos()) - starCount == 1) {
 
 			// Gesamtzahl
 			// -
 			// drber
 			// =
 			// 1
 			// setzte
 			// alle
 			// drber
 			// grau.
 			for (Field f : getEmtpyFieldsInADiagonalU(field)) { // holt
 				// leere+sternenfelder
 				// ber
 				// dem
 				// Pfeil
 				if (!arrows.contains(f) && !stars.contains(f)) {
 					grayed.add(f);
 				}
 			}
 		}
 
 	}
 
 	/**
 	 * Setzt Felder links eines nach rechtszeigenden Pfeiles grau, wenn er
 	 * Gesamtzahl - Darunterpfeile=1.
 	 */
 	private void setFieldsGrayLeftToTheArrow(Field field) {
 		int starCount = 0;
 		for (Field f : getEmtpyFieldsInADiagonalL(field)) { // holt
 			// leere+sternenfelder
 			// links
 			// vom
 			// Pfeil
 			if (stars.contains(f)) {
 				starCount = starCount + 1; // Sterne links vom Pfeil
 			}
 		}
 		if (getStarCountY(field.getyPos()) - starCount == 1) {
 			// Gesamtzahl
 			// -
 			// links
 			// =
 			// 1
 			// setze
 			// alle
 			// links
 			// grau.
 			for (Field f : getEmtpyFieldsInADiagonalL(field)) { // holt
 				// leere+sternenfelder
 				// links
 				// vom
 				// Pfeil
 				if (!arrows.contains(f) && !stars.contains(f)) {
 					grayed.add(f);
 				}
 			}
 		}
 
 	}
 
 	/**
 	 * Setzt Felder rechts eines nach linkszeigenden Pfeiles grau, wenn er
 	 * Gesamtzahl - Darunterpfeile=1.
 	 */
 	private void setFieldsGrayRightToTheArrow(Field field) {
 		int starCount = 0;
 		for (Field f : getEmtpyFieldsInADiagonalR(field)) { // holt
 			// leere+sternenfelder
 			// rechts
 			// vom
 			// Pfeil
 			if (stars.contains(f)) {
 				starCount = starCount + 1; // Sterne rechts vom Pfeil
 			}
 		}
 		if (getStarCountY(field.getyPos()) - starCount == 1) {
 			// Gesamtzahl
 			// -
 			// rechts
 			// =
 			// 1
 			// setze
 			// alle
 			// rechts
 			// grau.
 			for (Field f : getEmtpyFieldsInADiagonalR(field)) { // holt
 				// leere+sternenfelder
 				// rechts
 				// vom
 				// Pfeil
 				if (!arrows.contains(f) && !stars.contains(f)) {
 					grayed.add(f);
 				}
 			}
 		}
 
 	}
 	/**
 	 * Gibt die mglichen alternativ Felder in einem Hashset zurck.
 	 * Muss nach der KI berprfung checkDifficulty aufgerufen werden.
 	 * 
 	 */
 	public HashSet<Field> getPossibleOtherSolutionFields() {
 		HashSet<Field> possibleSolutionFields = new HashSet<Field>();
 		for (ArrayList<Field> list : listcontainer) {
 			for (Field field : list) {
 				if (!stars.contains(field) && !arrows.contains(field)
 						&& !grayed.contains(field)&&!(field.solutionContent==AllowedContent.CONTENT_STAR)) {
 					possibleSolutionFields.add(field);
 				}
 			}
 		}
 		return possibleSolutionFields;
 	}
 }
