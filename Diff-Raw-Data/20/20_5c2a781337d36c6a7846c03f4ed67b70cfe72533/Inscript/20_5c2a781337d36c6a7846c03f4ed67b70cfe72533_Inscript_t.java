 package org.abratuhi.snippettool.model;
 
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Observable;
 import java.util.Set;
 
 import org.abratuhi.snippettool.gui._panel_Options;
 import org.abratuhi.snippettool.util.ErrorUtil;
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.input.SAXBuilder;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.xml.sax.InputSource;
 
 /**
  * Inscript class. Stores all inscript relevant attributes from inscript's .xml
  * description and inscript's text structured due reading variants.
  * 
  * @author Alexei Bratuhin
  * 
  */
 public class Inscript extends Observable {
 
 	private static final Logger logger = LoggerFactory.getLogger(Inscript.class);
 
 	/** Inscript's id, e.g., HDS_1. **/
 	private String id = "";
 
 	/** Absolute database server path to inscript's image. **/
 	// FIXME: Inscript shouldn't know full path
 	private String absoluteRubbingPath = "";
 
 	/** Absolute database server path to inscript's .xml description. **/
 	private String path = "";
 
 	/** Inscript's pyramidal image **/
 	private PyramidalImage pyramidalImage = null;
 
 	/**
 	 * Inscript's text. Structure: 1st index: continuous character numbering;
 	 * 2nd index: continuous character's variant numbering; 3rd index:
 	 * supplementary index for the case, when not preferred reading contains
 	 * more characters, than preferred one
 	 **/
 	private ArrayList<ArrayList<ArrayList<InscriptCharacter>>> text = new ArrayList<ArrayList<ArrayList<InscriptCharacter>>>();
 
 	/** Whether text is read left-to-right **/
 	private boolean leftToRight = false;
 
 	/** Whether character should be drawn **/
 	private boolean characterVisible = true;
 
 	/** Whether character's number should be drawn **/
 	private boolean numberVisible = false;
 
 	/** Whether character's row, column must be drawn **/
 	private boolean rowColumnVisible = false;
 
 	/** Currently selected character index **/
 	private InscriptCharacter activeCharacter = null;
 
 	/** Font used to draw characters **/
 	private Font f;
 
 	/** Marking values **/
 
 	/** X Offset **/
 	private int oa;
 
 	/** Y Offset **/
 	private int ob;
 
 	/** Snippet width **/
 	private int a;
 
 	/** Snippet height **/
 	private int b;
 
 	/** X distance between snippets **/
 	private int da;
 
 	/** Y distance between snippets **/
 	private int db;
 
 	public Inscript() {
 	}
 
 	public void setFont(String localFont) {
 		try {
 			FileInputStream fontStream = new FileInputStream(localFont);
 			Font basisfont = Font.createFont(Font.TRUETYPE_FONT, fontStream);
 			f = basisfont.deriveFont(14.0f);
 			this.setChanged();
 			this.notifyObservers();
 		} catch (Exception e) {
 			logger.error("Could not load font", e);
 		}
 	}
 
 	public Font getFont() {
 		return this.f;
 	}
 
 	/**
 	 * Load inscript image from local file
 	 * 
 	 * @param img
 	 *            absolute path to image
 	 */
 	public void loadLocalImage(File img) {
 		try {
 			this.setPyramidalImage(PyramidalImage.loadImage(img));
 		} catch (IOException e) {
 			logger.error("IOException occurred in loadLocalImage", e);
 			ErrorUtil.showError(null, "Error loading local image: ", e);
 		}
 	}
 
 	public String getPlainText() {
 		String out = "";
 		int row = 1;
 		int crow = 1;
 		for (InscriptCharacter csign : getPreferredReadingText()) {
 			crow = csign.getRow();
 
 			if (crow != row) { // add breakline
 				out += "\n";
 				row = crow;
 			}
 
 			out += csign.characterStandard;
 		}
 		if (out.startsWith("\n")) {
 			out = out.substring(1);
 		}
 		return out;
 	}
 
 	/**
 	 * Load text from inscript's .xml description
 	 * 
 	 * @param xml
 	 *            content of inscript's .xml description
 	 */
 	@SuppressWarnings("unchecked")
 	public void setTextFromXML(String xml) {
 		// clear old text
 		getText().clear();
 
 		// prepare index variables
 		int current_number = 1;
 		int current_row = 1;
 		int current_column = 1;
 
 		// start
 		try {
 			// parse XML document
 			SAXBuilder builder = new SAXBuilder();
 			Document doc = builder.build(new InputSource(new StringReader(xml)));
 			List<Element> l = doc.getRootElement().getChildren();
 			for (int i = 0; i < l.size(); i++) {
 				// line break
 				if (l.get(i).getName().equals("br")) {
 					// remove starting newline if any
 					if (i != 0)
 						current_row++; // some texts start with leading
 					// linebreak, which needs to be
 					// eliminated explicitly
 					current_column = 1; // line break -> start numbering of
 					// columns from beginning
 				}
 
 				// choice, which may possibly mean no choice, but just a sign
 				// formatted to choice
 				// to achieve compatibility
 				if (l.get(i).getName().equals("choice")) {
 					// List<org.jdom.Element> lvariants =
 					// l.get(i).getChildren("variant");
 					List<Element> lvariants = l.get(i).getChildren();
 
 					// set the length of preferred reading
 					int basicvariant = 0;
 					int basiclength = 0;
 					float basiccert = 0.0f;
 					for (int v = 0; v < lvariants.size(); v++) {
 						float cert = Float.parseFloat(lvariants.get(v).getAttributeValue("cert"));
 						if (cert > basiccert) {
 							basicvariant = v;
 							basiclength = lvariants.get(v).getChildren().size();
 							basiccert = cert;
 						}
 					}
 
 					// preferred reading should be the first variant
 					if (basicvariant != 0) {
 						Element tvariant = lvariants.get(0);
 						lvariants.set(0, lvariants.remove(basicvariant));
 						lvariants.add(tvariant);
 					}
 
 					// if preferred reading is empty, dismiss the whole choice
 					if (lvariants.get(0).getChildren().size() == 0) {
 						continue;
 					}
 
 					// search for variant with maximum stringlength
 					int maxlength = 0;
 					for (int v = 0; v < lvariants.size(); v++) {
 						if (lvariants.get(v).getChildren().size() > maxlength) {
 							maxlength = lvariants.get(v).getChildren().size();
 						}
 					}
 
 					// proceed basic length
 					for (int j = 0; j < basiclength; j++) {
 						ArrayList<ArrayList<InscriptCharacter>> signVariants = new ArrayList<ArrayList<InscriptCharacter>>();
 						ArrayList<InscriptCharacter> signs = new ArrayList<InscriptCharacter>();
 						InscriptCharacter csign = null;
 						boolean supplied = false;
 
 						for (int v = 0; v < lvariants.size(); v++) {
 
 							if (lvariants.get(v).getChildren().size() == 0) {
 								continue;
 							}
 
 							signs.clear();
 
 							// the first reading in choice schema is preferred
 							boolean preferred = (v == 0) ? true : false;
 							// load cert form parent tag choice
 							float cert = Float.parseFloat(lvariants.get(v).getAttributeValue("cert"));
 							// create variant number
 							int variantnumber = v;
 
 							if (j < lvariants.get(variantnumber).getChildren().size()) { // if
 								// there
 								// is
 								// a
 								// sign
 								// with
 								// indexed j in this variant
 
 								Element cspan = (Element) lvariants.get(v).getChildren().get(j);
 
 								if (cspan == null)
 									System.out.println("NULL Element cspan while proceeding.");
 								if (cspan.getAttribute("class") == null)
 									System.out.println("Element doesn't have 'class' attribute;\n" + cspan.toString());
 
 								if (cspan.getAttributeValue("class").equals("supplied")) {
 									supplied = true;
 								} else {
 									String ch = cspan.getText();
 									String chOriginal = (cspan.getAttribute("original") != null) ? cspan
 											.getAttributeValue("original") : ch;
 
 											csign = new InscriptCharacter(this, ch, chOriginal, cert, preferred, variantnumber,
 													current_row, current_column, current_number, new SnippetShape(
 															new Rectangle(0, 0, 0, 0)));
 
 											signs.add(csign);
 											signVariants.add((ArrayList<InscriptCharacter>) signs.clone());
 								}
 							}
 						}
 
 						if (!supplied) {
 							// add variants arraylist to sutra text
 							getText().add((ArrayList<ArrayList<InscriptCharacter>>) signVariants.clone());
 							//
 							current_column++;
 							current_number++;
 						} else {
 							current_column++;
 						}
 					}
 
 					// proceed extra length
 					for (int j = basiclength; j < maxlength; j++) {
 						InscriptCharacter csign = null;
 						int current_number_for_extra = current_number - 1; // current_number
 						// was
 						// already
 						// incremented
 						int current_column_for_extra = current_column - 1;
 
 						for (int v = 0; v < lvariants.size(); v++) {
 
 							if (lvariants.get(v).getChildren().size() < basiclength) {
 								continue;
 							}
 
 							// the first reading in choice schema is preferred
 							boolean preferred = (v == 0) ? true : false;
 							// load cert form parent tag choice
 							float cert = Float.parseFloat(lvariants.get(v).getAttributeValue("cert"));
 							// create variant number
 							int variantnumber = v;
 
 							if (j < lvariants.get(variantnumber).getChildren().size()) { // if
 								// there
 								// is
 								// a
 								// sign
 								// with
 								// indexed j in this variant
 
 								Element cspan = (Element) lvariants.get(v).getChildren().get(j);
 
 								if (cspan == null)
 									System.out.println("NULL Element cspan while proceeding.");
 								if (cspan.getAttribute("class") == null)
 									System.out.println("Element doesn't have 'class' attribute;\n" + cspan.toString());
 
 								if (cspan.getAttributeValue("class").equals("supplied")) {
 
 								} else {
 									String ch = cspan.getText();
 									String chOriginal = (cspan.getAttribute("original") != null) ? cspan
 											.getAttributeValue("original") : ch;
 
 											csign = new InscriptCharacter(this, ch, chOriginal, cert, preferred, variantnumber,
 													current_row, current_column_for_extra, current_number_for_extra,
 													new SnippetShape(new Rectangle(0, 0, 0, 0)));
 
 											// no imagesign -> attach it to last placed
 											// sign
 											// -1, current_number starts with 1, not 0
 											// and in arraylist numbering starts with 0
 											getText().get(current_number_for_extra - 1).get(variantnumber).add(csign);
 								}
 							}
 						}
 					}
 				}
 			}
 			this.setChanged();
 			this.notifyObservers();
 		} catch (Exception e) {
 			logger.error("Could not setTextFromXML()", e);
 		}
 	}
 
 	/**
 	 * Update characters' snippet marking coordinates and dimension using
 	 * results from querying the inscript database
 	 * 
 	 * @param appearances
 	 *            <appearance>s as result of querying database
 	 */
 	public void updateCoordinates(Element[] appearances) {
 		// transform to list of Characters
 		ArrayList<InscriptCharacter> tarrayOfSigns = new ArrayList<InscriptCharacter>();
 		for (Element appearance : appearances)
 			tarrayOfSigns.add(InscriptCharacter.fromAppearance(this, appearance));
 
 		// use each appearance's number to update coordinates
 		for (int i = 0; i < tarrayOfSigns.size(); i++) {
 			InscriptCharacter csign = tarrayOfSigns.get(i);
 			updateSnippet(csign.getShape(), csign.getRow(), csign.getColumn());
 		}
 
 		// check, whether all signs have become coordinates assigned, if not,
 		// mark those as missing
 		for (int i = 0; i < getText().size(); i++) {
 			for (int j = 0; j < getText().get(i).size(); j++) {
 				for (int k = 0; k < getText().get(i).get(j).size(); k++) {
 					InscriptCharacter csign = getText().get(i).get(j).get(k);
 					csign.setModified(false);
 					if (csign.getShape().isEmpty()) {
 						csign.missing = true;
 					}
 				}
 			}
 		}
 		this.setChanged();
 		this.notifyObservers();
 	}
 
 	public InscriptCharacter getCharacterNV(int n, int v) {
 		return getText().get(n).get(v).get(0);
 	}
 
 	public InscriptCharacter getCharacterRC(int r, int c) {
 		for (int i = 0; i < getText().size(); i++) {
 			InscriptCharacter ch = getCharacterNV(i, 0);
 			if (ch.getRow() == r && ch.getColumn() == c) {
 				return ch;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Resize character's snippet's marking. Notice: all corresponding
 	 * characters (those with the same continuous number) will be automatically
 	 * resized too. Notice: generally applies - all variants marking are
 	 * adjusted using preferred reading's marking.
 	 * 
 	 * @param sn
 	 *            character
 	 * @param dir
 	 *            resize direction
 	 * @param dx
 	 *            x resize
 	 * @param dy
 	 *            y resize
 	 */
 	public void resizeSnippet(InscriptCharacter sn, String dir, int dx, int dy) {
 		int index = sn.getNumber() - 1; // all variants must be resized
 		for (int j = 0; j < this.getText().get(index).size(); j++) {
 			for (int k = 0; k < this.getText().get(index).get(j).size(); k++) {
 				this.getText().get(index).get(j).get(k).resizeSnippet(dir, dx, dy);
 			}
 		}
 		this.setChanged();
 		this.notifyObservers();
 	}
 
 	/**
 	 * Move character's snippet's marking. Notice: all corresponding characters
 	 * (those with the same continuous number) will be automatically resized
 	 * too. Notice: generally applies - all variants marking are adjusted using
 	 * preferred reading's marking.
 	 * 
 	 * @param sn
 	 *            character
 	 * @param dx
 	 *            x shift
 	 * @param dy
 	 *            y shift
 	 */
 	public void moveSnippet(InscriptCharacter sn, int dx, int dy) {
 		int index = sn.getNumber() - 1; // all variants must be moved
 		for (int j = 0; j < this.getText().get(index).size(); j++) {
 			for (int k = 0; k < this.getText().get(index).get(j).size(); k++) {
 				this.getText().get(index).get(j).get(k).moveSnippet(dx, dy);
 			}
 		}
 		this.setChanged();
 		this.notifyObservers();
 	}
 
	public void clearShape(InscriptCharacter character) {
		int index = character.getNumber() - 1; // all variants must be moved
		for (int j = 0; j < this.getText().get(index).size(); j++) {
			for (int k = 0; k < this.getText().get(index).get(j).size(); k++) {
				this.getText().get(index).get(j).get(k).clearShape();
			}
		}
		this.setChanged();
		this.notifyObservers();

	}

 	public void rotateSnippet(InscriptCharacter sn, double phi) {
 		int index = sn.getNumber() - 1; // all variants must be moved
 		for (int j = 0; j < this.getText().get(index).size(); j++) {
 			for (int k = 0; k < this.getText().get(index).get(j).size(); k++) {
 				this.getText().get(index).get(j).get(k).rotateSnippet(phi);
 			}
 		}
 		this.setChanged();
 		this.notifyObservers();
 	}
 
 	public void updateSnippet(SnippetShape shape, int r, int c) {
 		int indexTarget = -1;
 		for (int i = 0; i < getText().size(); i++) {
 			if (getText().get(i).get(0).get(0).getRow() == r
 					&& getText().get(i).get(0).get(0).getColumn() == c) {
 				indexTarget = i;
 				break;
 			}
 		}
 
 		// check whether indexTarget found
 		if (indexTarget != -1) {
 			for (int j = 0; j < getText().get(indexTarget).size(); j++) {
 				for (int k = 0; k < getText().get(indexTarget).get(j).size(); k++) {
 					getText().get(indexTarget).get(j).get(k).updateSnippet(shape);
 				}
 			}
 		}
 		this.setChanged();
 		this.notifyObservers();
 	}
 
 	public void updatePathToSnippet(String pathToSnippet, int indexTarget) {
 		for (int j = 0; j < this.getText().get(indexTarget).size(); j++) {
 			for (int k = 0; k < this.getText().get(indexTarget).get(j).size(); k++) {
 				getText().get(indexTarget).get(j).get(k).path_to_snippet = pathToSnippet;
 			}
 		}
 		this.setChanged();
 		this.notifyObservers();
 	}
 
 	/**
 	 * Load values for autoguided marking from Snippet-tool's Options component.
 	 * 
 	 * @param options
 	 *            Snippet-Tool options component
 	 * @param missingOnly
 	 *            whether marking schema should be applied for characters not
 	 *            already in database only
 	 */
 	public void loadMarkingSchema(_panel_Options options, boolean missingOnly) {
 		// load markup parameters
 		oa = Integer.valueOf(options.jtf_oa.getText());
 		ob = Integer.valueOf(options.jtf_ob.getText());
 		a = Integer.valueOf(options.jtf_a.getText());
 		b = Integer.valueOf(options.jtf_b.getText());
 		da = Integer.valueOf(options.jtf_da.getText());
 		db = Integer.valueOf(options.jtf_db.getText());
 
 		// check whether all markup snippets are seen
 		// use preferred reading's signs
 		int dim_x;
 		int dim_y;
 		// try {
 		dim_x = getPyramidalImage().getWidth();
 		dim_y = getPyramidalImage().getHeight();
 		// } catch (IOException e) {
 		// logger.error("IOException occurred in loadMarkingSchema", e);
 		// ErrorUtil.showError(null, "Could not access image file", e);
 		// return;
 		// }
 		int x_width = 0;
 		int y_height = 0;
 		int max_row = 0;
 		int max_column = 0;
 		for (InscriptCharacter inscriptCharacter : getPreferredReadingText()) {
 			if (inscriptCharacter.getColumn() > max_column)
 				max_column = inscriptCharacter.getColumn();
 			if (inscriptCharacter.getRow() > max_row)
 				max_row = inscriptCharacter.getRow();
 		}
 		x_width = oa + (max_row - 1) * (a + da);
 		y_height = ob + (max_column - 1) * (b + db);
 		if (x_width > dim_x || y_height > dim_y) {
 			// TODO: What's going on here? Why we need to warn?
 			logger.warn("loadMarkingSchema failed");
 			ErrorUtil.showWarning(null, "loadMarkingSchema failed");
 			return;
 		}
 
 		// apply parameters if check passed
 		for (int i = 0; i < getText().size(); i++) {
 			ArrayList<ArrayList<InscriptCharacter>> signvariants = getText().get(i);
 			for (int j = 0; j < signvariants.size(); j++) {
 				for (int k = 0; k < signvariants.get(j).size(); k++) {
 					InscriptCharacter csign = signvariants.get(j).get(k);
 
 					if (missingOnly && csign.missing) {
 						continue;
 					} else {
 						int r = csign.getRow() - 1;
 						int c = csign.getColumn() - 1;
 						if (isLeftToRight()) {
 							csign.setShape(new SnippetShape(new Rectangle(
 									new Point(oa + (a + da) * r, ob + (b + db)
 											* c), new Dimension(a, b))));
 						} else {
 							csign.setShape(new SnippetShape(new Rectangle(
 									new Point(dim_x - oa - a - (a + da) * r, ob
 											+ (b + db) * c),
 											new Dimension(a, b))));
 						}
 					}
 				}
 			}
 		}
 		this.setChanged();
 		this.notifyObservers();
 	}
 
 	public String getXUpdate(String collection) {
 		StringBuffer xupdate = new StringBuffer();
 
 		Set<String> ids = new HashSet<String>();
 		StringBuffer additions = new StringBuffer();
 		for (int i = 0; i < getText().size(); i++) {
 			for (int j = 0; j < getText().get(i).size(); j++) {
 				for (int k = 0; k < getText().get(i).get(j).size(); k++) {
 					InscriptCharacter character = getText().get(i).get(j).get(k);
 					if (character.isModified()) {
 						ids.add(character.getId());
 						additions.append(character.getXUpdate(collection));
 						character.setModified(false);
 					}
 				}
 			}
 
 		}
 
 		xupdate.append("<xu:modifications version=\'1.0\' xmlns:xu=\'http://www.xmldb.org/xupdate\'>\n");
 
 		for (String id : ids) {
 			xupdate.append("    <xu:remove select=\"//appearance[@id='" + id + "']\" />\n");
 		}
 
 		xupdate.append(additions);
 
 		xupdate.append("</xu:modifications>");
 
 		return xupdate.toString();
 	}
 
 	/**
 	 * clear inscript's information
 	 */
 	public void clear() {
 		getText().clear();
 		setId("");
 		setPath("");
 		setAbsoluteRubbingPath("");
 		setActiveCharacter(null);
 		this.setChanged();
 		this.notifyObservers();
 	}
 
 	/**
 	 * @param absoluteRubbingPath
 	 *            the absoluteRubbingPath to set
 	 */
 	public void setAbsoluteRubbingPath(String absoluteRubbingPath) {
 		this.absoluteRubbingPath = absoluteRubbingPath;
 		this.setChanged();
 		this.notifyObservers();
 	}
 
 	/**
 	 * Get the absolute rubbing path.
 	 * 
 	 * @return the absoluteRubbingPath
 	 */
 	public String getAbsoluteRubbingPath() {
 		return absoluteRubbingPath;
 	}
 
 	public String getRelativeRubbingPath() {
 		return absoluteRubbingPath.replaceFirst("xmldb:.*?/db/", "");
 	}
 
 	/**
 	 * @param id
 	 *            the id to set
 	 */
 	public void setId(String id) {
 		this.id = id;
 		this.setChanged();
 		this.notifyObservers();
 	}
 
 	/**
 	 * @return the id
 	 */
 	public String getId() {
 		return id;
 	}
 
 	/**
 	 * @param path_file
 	 *            the path_file to set
 	 */
 	public void setPath(String path_file) {
 		this.path = path_file;
 		this.setChanged();
 		this.notifyObservers();
 	}
 
 	/**
 	 * @return the path_file
 	 */
 	public String getPath() {
 		return path;
 	}
 
 	/**
 	 * @param text
 	 *            the text to set
 	 */
 	public void setText(ArrayList<ArrayList<ArrayList<InscriptCharacter>>> text) {
 		this.text = text;
 		this.setChanged();
 		this.notifyObservers();
 	}
 
 	/**
 	 * @return the text
 	 */
 	public ArrayList<ArrayList<ArrayList<InscriptCharacter>>> getText() {
 		return text;
 	}
 
 	public List<InscriptCharacter> getPreferredReadingText() {
 		List<InscriptCharacter> result = new ArrayList<InscriptCharacter>(text.size());
 		for (ArrayList<ArrayList<InscriptCharacter>> inscriptCharacter : text) {
 			result.add(inscriptCharacter.get(0).get(0));
 		}
 		return result;
 	}
 
 	/**
 	 * @param leftToRight
 	 *            the leftToRight to set
 	 */
 	public void setLeftToRight(boolean leftToRight) {
 		this.leftToRight = leftToRight;
 	}
 
 	/**
 	 * @return the leftToRight
 	 */
 	public boolean isLeftToRight() {
 		return leftToRight;
 	}
 
 	/**
 	 * @param characterVisible
 	 *            the characterVisible to set
 	 */
 	public void setCharacterVisible(boolean characterVisible) {
 		this.characterVisible = characterVisible;
 		this.setChanged();
 		this.notifyObservers();
 	}
 
 	/**
 	 * @return the characterVisible
 	 */
 	public boolean isCharacterVisible() {
 		return characterVisible;
 	}
 
 	/**
 	 * @param numberVisible
 	 *            the numberVisible to set
 	 */
 	public void setNumberVisible(boolean numberVisible) {
 		this.numberVisible = numberVisible;
 		this.setChanged();
 		this.notifyObservers();
 	}
 
 	/**
 	 * @return the numberVisible
 	 */
 	public boolean isNumberVisible() {
 		return numberVisible;
 	}
 
 	/**
 	 * @param rowColumnVisible
 	 *            the rowColumnVisible to set
 	 */
 	public void setRowColumnVisible(boolean rowColumnVisible) {
 		this.rowColumnVisible = rowColumnVisible;
 		this.setChanged();
 		this.notifyObservers();
 	}
 
 	/**
 	 * @return the rowColumnVisible
 	 */
 	public boolean isRowColumnVisible() {
 		return rowColumnVisible;
 	}
 
 	/**
 	 * @param activeCharacter
 	 *            the activeCharacter to set
 	 */
 	public void setActiveCharacter(InscriptCharacter activeCharacter) {
 		this.activeCharacter = activeCharacter;
 		this.setChanged();
 		this.notifyObservers();
 	}
 
 	/**
 	 * @return the activeCharacter
 	 */
 	public InscriptCharacter getActiveCharacter() {
 		return activeCharacter;
 	}
 
 	/**
 	 * @return the pyramidalImage
 	 */
 	public PyramidalImage getPyramidalImage() {
 		return pyramidalImage;
 	}
 
 	/**
 	 * @param pyramidalImage
 	 *            the pyramidalImage to set
 	 */
 	private void setPyramidalImage(PyramidalImage pyramidalImage) {
 		this.pyramidalImage = pyramidalImage;
 		setChanged();
 		notifyObservers();
 		System.gc();
 	}
 
 }
