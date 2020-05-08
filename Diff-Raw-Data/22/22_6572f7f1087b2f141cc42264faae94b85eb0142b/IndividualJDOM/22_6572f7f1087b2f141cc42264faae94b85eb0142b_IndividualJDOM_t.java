 package com.redbugz.macpaf.jdom;
 
 import java.io.File;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.jdom.Element;
 import org.jdom.JDOMException;
 import org.jdom.Text;
 import org.jdom.filter.ContentFilter;
 import org.jdom.filter.Filter;
 import org.jdom.output.Format;
 import org.jdom.output.XMLOutputter;
 import org.jdom.xpath.XPath;
 
 import com.apple.cocoa.foundation.NSBundle;
 import com.redbugz.macpaf.Event;
 import com.redbugz.macpaf.Family;
 import com.redbugz.macpaf.Gender;
 import com.redbugz.macpaf.Individual;
 import com.redbugz.macpaf.Multimedia;
 import com.redbugz.macpaf.Note;
 import com.redbugz.macpaf.Ordinance;
 import com.redbugz.macpaf.util.CocoaUtils;
 import com.redbugz.macpaf.util.JDOMUtils;
 
 /**
  * This class is a wrapper around a JDOM Element that represents an Individual.
  * Most of the data is stored in the Element and children Elements, as the JDOM
  * document is the official record of the data.
  * 
  * Created by IntelliJ IDEA.
  * User: logan
  * Date: Mar 16, 2003
  * Time: 3:29:46 PM
  */
 public class IndividualJDOM implements Individual, Cloneable {
  private static final ContentFilter CONTENT_FILTER_TEXT = new ContentFilter(ContentFilter.TEXT);
private static final Logger log = Logger.getLogger(IndividualJDOM.class);
   private static final String REF = "REF";
   public static final String INDIVIDUAL = "INDI";
   public static final String RESTRICTION = "RESN";
   public static final String GENDER = "SEX";
   public static final String SUBMISSION = "SUBM";
   public static final String RIN = "RIN";
   public static final String AFN = "AFN";
   public static final String RFN = "RFN";
 
   public static final String NAME = "NAME";
   public static final String NAME_PREFIX = "NPFX";
   public static final String NAME_GIVEN = "GIVN";
   public static final String NAME_NICKNAME = "NICK";
   public static final String NAME_SURNAME_PREFIX = "SPFX";
   public static final String NAME_SURNAME = "SURN";
   public static final String NAME_SUFFIX = "NSFX";
 
   private static final String ID = "ID";
   private static final String FAMILY_CHILD_LINK = "FAMC";
   private static final String FAMILY_SPOUSE_LINK = "FAMS";
   public static final String LOCKED = "locked";
   public static final String PRIVACY = "privacy";
 
   private Element element = null;//new Element(INDIVIDUAL);
 //  private List events = new ArrayList();
 
   private MacPAFDocumentJDOM document = null;
 //  private Family familyAsChild;
 //  private Family familyAsSpouse;
 private static final Collection commonNamePrefixes = Arrays.asList(new String[] {
 		"Dr.", "Prof.", "Gen.", "Sir", "Adm.", "Lt.", "Lt. Cmmdr.", "Lord", "Pvt.",
 		"Mr.", "Mrs.", "Miss", "Ms."
 });
 
 //  public IndividualJDOM(MacPAFDocumentJDOM parentDocument) {
 //	document = parentDocument;
 //	element = new Element(INDIVIDUAL);
 //  }
 
   public IndividualJDOM(Element newElement, MacPAFDocumentJDOM parentDocument) {
   	if (parentDocument == null) {
   		throw new IllegalArgumentException("Cannot create IndividualJDOM with null parentDocument");
   	}
 	if (newElement == null) {
 		throw new IllegalArgumentException("newElement is null: IndividualJDOM must be bound to a valid Element.");
 //	  element = new Element(INDIVIDUAL);
 	}
 	document = parentDocument;
 	element = newElement;
 	setFullName(getNameString());
 //		String iName = element.getChildText(NAME);
 //		int surnameStart = iName.indexOf("/");
 //		int surnameEnd = iName.lastIndexOf("/");
 //		givens = iName.substring(0, surnameStart);
 //		surname = iName.substring(surnameStart + 1, surnameEnd);
 //		suffix = iName.substring(surnameEnd);
 //		this.element = element;
 //		log.debug("ident:" + getId());
 //		if (element.getChild("BIRT") != null) {
 //			setBirthEvent(new EventJDOM(element.getChild("BIRT")));
 //		}
 //		if (element.getChild("BAPL") != null) {
 //			baptism = new OrdinanceJDOM(element.getChild("BAPL"));
 //		}
 //		if (element.getChild("CONL") != null) {
 //			confirmation = new OrdinanceJDOM(element.getChild("CONL"));
 //		}
 //		if (element.getChild("ENDL") != null) {
 //			endowment = new OrdinanceJDOM(element.getChild("ENDL"));
 //		}
 //		if (element.getChild("SLGC") != null) {
 //			sealingToParents = new OrdinanceJDOM(element.getChild("SLGC"));
 //		}
 	// some debugging of multimedia
 	// todo: clean this up	
 	final Element obje = newElement.getChild("OBJE");
 	if (obje != null) {
 	  log.debug("Person has multimedia object:" + obje);
 	  log.debug("ref=" + obje.getAttributeValue(REF));
 	  log.debug("form=" + obje.getChildText("FORM"));
 	  log.debug("title=" + obje.getChildText("TITL"));
 	  log.debug("file=" + obje.getChildText("FILE"));
 	  log.debug("content=" + obje.getText());
 	}
   }
 
 //  public IndividualJDOM(Individual newIndividual) {
 //	if (newIndividual instanceof IndividualJDOM) {
 //	  element = ( (IndividualJDOM) newIndividual).getElement();
 //	}
 //	else {
 //	  setAFN(newIndividual.getAFN());
 //	  setBirthEvent(newIndividual.getBirthEvent());
 //	  setBurialEvent(newIndividual.getBurialEvent());
 //	  setChristeningEvent(newIndividual.getChristeningEvent());
 //	  setDeathEvent(newIndividual.getDeathEvent());
 //	  setFamilyAsChild(newIndividual.getFamilyAsChild());
 //	  setFamilyAsSpouse(newIndividual.getFamilyAsSpouse());
 //	  setFather(newIndividual.getFather());
 //	  setFullName(newIndividual.getNamePrefix(), newIndividual.getGivenNames(), newIndividual.getSurname(),
 //				  newIndividual.getNameSuffix());
 //	  setGender(newIndividual.getGender());
 //	  setId(newIndividual.getId());
 //	  setImagePath(newIndividual.getImagePath());
 //	  setLDSBaptism(newIndividual.getLDSBaptism());
 //	  setLDSConfirmation(newIndividual.getLDSConfirmation());
 //	  setLDSEndowment(newIndividual.getLDSEndowment());
 //	  setLDSSealingToParent(newIndividual.getLDSSealingToParents());
 //	  setLocked(newIndividual.isLocked());
 //	  setMother(newIndividual.getMother());
 //	  setNotes(newIndividual.getNotes());
 //	  setPrimarySpouse(newIndividual.getPrimarySpouse());
 //	  setPrivate(newIndividual.isPrivate());
 //	  setRin(newIndividual.getRin());
 //	  setSpouseList(newIndividual.getSpouseList());
 //	}
 //  }
 
   public void setGivenNames(String givenNames) {
 	if (givenNames == null) {
 	  givenNames = "";
 	}
 	Element name = JDOMUtils.findOrMakeChildElement(NAME_GIVEN, JDOMUtils.findOrMakeChildElement(NAME, element));
	name.removeContent(CONTENT_FILTER_TEXT);
 	name.addContent(0, new Text(givenNames.trim()));
 	saveName();
   }
 
   public void setGender(Gender gender) {
 	if (gender == null) {
 	  gender = Gender.UNKNOWN;
 	}
 	element.removeChildren(GENDER);
 	if (Gender.UNKNOWN != gender) {
 	  element.addContent(new Element(GENDER).setText(gender.getAbbreviation()));
 	}
   }
 
   public void setSurname(String surname) {
 	if (surname == null) {
 	  surname = "";
 	}
 	Element name = JDOMUtils.findOrMakeChildElement(NAME_SURNAME, JDOMUtils.findOrMakeChildElement(NAME, element));
	name.removeContent(CONTENT_FILTER_TEXT);
 	name.addContent(0, new Text(surname));
 	saveName();
   }
 
   private String getNameString() {
 	String nameString = element.getChildTextTrim(NAME);
 	if (nameString == null) {
 	  nameString = "";
 	}
 	return nameString;
   }
 
   private void setName(String name) {
 	Element nameElement = JDOMUtils.findOrMakeChildElement(NAME, element);
	nameElement.removeContent(CONTENT_FILTER_TEXT);
 	nameElement.addContent(0, new Text(name));
   }
 
   public String getFullName() {
 //		String name = givens;
 //		if (name.length() > 0) {
 //			name += " ";
 //		}
 //		name += surname;
 	String name = element.getChildTextTrim(NAME); //name;
 	if (name == null) {
 	  name = "";
 	}
 	// remove the slashes around the surname
 	// todo: use string replacement
 //	log.debug("name before slashes removed:|" + name + "|");
 	StringBuffer nameBuf = new StringBuffer(name);
 	while (nameBuf.toString().indexOf('/') >= 0) {
 	  nameBuf.deleteCharAt(nameBuf.toString().indexOf('/'));
 	}
 	name = nameBuf.toString();
 //	log.debug("name  after slashes removed:|" + name + "|");
 	return name.trim();
 //	return getId()+":"+name.trim();
   }
 
   /* (non-Javadoc)
    * @see Individual#setFullName(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
    */
   public void setFullName(
 	  String prefix,
 	  String givenNames,
 	  String surname,
 	  String suffix) {
 //			this.prefix = prefix;
 //			this.givens = givenNames;
 //			this.surname = surname;
 //			this.suffix = suffix;
 //			saveName();
 	setNamePrefix(prefix);
 	setGivenNames(givenNames);
 	setSurname(surname);
 	setNameSuffix(suffix);
 	saveName();
   }
 
   public void setFullName(String name) {
 	log.debug("--------------------------IndividualJDOM.setFullName(name):" + name);
 	if (name == null) {
 	  name = "";
 	}
 	if (name.equals(getFullName())) {
 	  log.debug("IndividualJDOM.setFullName(name) name is already same as full name, returning...");
 	  return;
 	}
 	int firstSlashIndex = name.indexOf("/");
 	int lastSlashIndex = name.lastIndexOf("/"); //, firstSlashIndex + 1);
 	log.error("name:" + name);
 	log.error("surname:" + firstSlashIndex + "-" + lastSlashIndex);
 	if (firstSlashIndex >= 0) {
 	  setBestGuessPrefixAndGivenNames(name.substring(0, firstSlashIndex));
 	  if (lastSlashIndex >= 0 && lastSlashIndex > firstSlashIndex) {
 		setSurname(name.substring(firstSlashIndex + 1, lastSlashIndex));
 		setNameSuffix(name.substring(lastSlashIndex + 1).trim());
 	  }
 	  else {
 		setSurname(name.substring(firstSlashIndex + 1));
 	  }
 	}
 	else {
 //            element.removeChildren(NAME);
 	  setName(name);
 	}
   }
 
   /**
  * @param string
  */
 private void setBestGuessPrefixAndGivenNames(String string) {
 	int givenIndex = 0;
 	Iterator iter = commonNamePrefixes.iterator();
 	while (iter.hasNext() && givenIndex == 0) {
 		String prefix = (String) iter.next();
 		if (string.trim().startsWith(prefix)) {
 			givenIndex = string.indexOf(prefix)+prefix.length();
 		}
 	}
 	setNamePrefix(string.substring(0, givenIndex).trim());
 	setGivenNames(string.substring(givenIndex).trim());
 }
 
 private void saveName() {
 	String name = getNamePrefix();
 	if (getNamePrefix().length() > 0) {
 	  name += " ";
 	}
 	name += getGivenNames();
 	if (getGivenNames().length() > 0) {
 	  name += " ";
 	}
 	if (getSurname().length() > 0) {
 	  name += "/" + getSurname() + "/ ";
 	}
 	name += getNameSuffix();
 	log.debug("saveName name=" + name.trim());
 	setName(name.trim());
   }
 
   public String getGivenNames() {
 	String givens = ""; //getNameString();
 	Element name = element.getChild(NAME);
 	if (name != null) {
 	  Element givenNames = name.getChild(NAME_GIVEN);
 	  if (givenNames != null) {
 		givens = givenNames.getTextTrim(); //givens;
 	  }
 	  if (givens.indexOf("/") >= 0) {
 		givens = givens.substring(0, givens.indexOf("/"));
 	  }
 	}
 	return givens;
   }
 
   public String getSurname() {
 	String surname = "";
 	Element name = element.getChild(NAME);
 	if (name != null) {
 	  surname = name.getChildTextTrim(NAME_SURNAME);
 	  if (surname == null) {
 		surname = "";
 	  }
 	}
 	return surname;
   }
 
   public String getNamePrefix() {
 	String prefix = "";
 	Element name = element.getChild(NAME);
 	if (name != null) {
 	  prefix = name.getChildTextTrim(NAME_PREFIX);
 	  if (prefix == null) {
 		prefix = "";
 	  }
 	}
 	return prefix;
   }
 
   /* (non-Javadoc)
    * @see Individual#getNameSuffix()
    */
   public String getNameSuffix() {
 	String suffix = "";
 	Element name = element.getChild(NAME);
 	if (name != null) {
 	  suffix = name.getChildTextTrim(NAME_SUFFIX);
 	  if (suffix == null) {
 		suffix = "";
 	  }
 	}
 	return suffix;
   }
 
   /* (non-Javadoc)
    * @see Individual#setNamePrefix(java.lang.String)
    */
   public void setNamePrefix(String prefix) {
 	if (prefix == null) {
 	  prefix = "";
 	}
 	Element name = JDOMUtils.findOrMakeChildElement(NAME_PREFIX, JDOMUtils.findOrMakeChildElement(NAME, element));
	name.removeContent(CONTENT_FILTER_TEXT);
 	name.addContent(0, new Text(prefix));
 	saveName();
   }
 
   /* (non-Javadoc)
    * @see Individual#setNameSuffix(java.lang.String)
    */
   public void setNameSuffix(String suffix) {
 	if (suffix == null) {
 	  suffix = "";
 	}
 	Element name = JDOMUtils.findOrMakeChildElement(NAME_SUFFIX, JDOMUtils.findOrMakeChildElement(NAME, element));
	name.removeContent(CONTENT_FILTER_TEXT);
 	name.addContent(0, new Text(suffix));
 	saveName();
   }
 
   /* (non-Javadoc)
    * @see Individual#addSpouse(Individual)
    */
   public void addSpouse(Individual newSpouse) {
 	throw new UnsupportedOperationException("addSpouse not implemented in IndividualJDOM");
   }
 
   public void setId(String id) {
 	if (id == null) {
 	  id = "0";
 	}
 	element.setAttribute(ID, id);
   }
 
   public void setFamilyAsChild(Family family) {
 //		this.familyAsChild = family;
 	element.removeChildren(FAMILY_CHILD_LINK);
 	element.addContent(new Element(FAMILY_CHILD_LINK).setAttribute(REF, family.getId()));
   }
 
   public void addFamilyAsSpouse(Family family) {
 //		this.familyAsSpouse = family;
 //		familiesAsSpouse.add(family);
 	element.addContent(new Element(FAMILY_SPOUSE_LINK).setAttribute(REF, family.getId()));
 	//      addSpouse(family.getMother());
   }
 
   public Gender getGender() {
 	log.debug("IndividualJDOM.getGender() returns:" + element.getChildTextTrim(GENDER));
 	return Gender.genderWithCode(element.getChildTextTrim(GENDER));
   }
 
   public Event getBirthEvent() {
 	return new EventJDOM(JDOMUtils.findOrMakeChildElement(EventJDOM.BIRTH, element));
   }
 
   public void setBirthEvent(Event event) {
 	  if (event instanceof Event.UnknownEvent) {
 		  // do nothing
 		  return;
 	  } else {
 		  JDOMUtils.replaceFirstChildElement(element, new EventJDOM(event).getElement());
 	  }
   }
 
   public void setDeathEvent(Event event) {
 	  if (event instanceof Event.UnknownEvent) {
 		  // do nothing
 		  return;
 	  } else {
 		  JDOMUtils.replaceFirstChildElement(element, new EventJDOM(event).getElement());
 	  }
   }
 
   public void setBurialEvent(Event event) {
 	  if (event instanceof Event.UnknownEvent) {
 		  // do nothing
 		  return;
 	  } else {
 		  JDOMUtils.replaceFirstChildElement(element, new EventJDOM(event).getElement());
 	  }
   }
 
   public void setChristeningEvent(Event event) {
 	  if (event instanceof Event.UnknownEvent) {
 		  // do nothing
 		  return;
 	  } else {
 		  JDOMUtils.replaceFirstChildElement(element, new EventJDOM(event).getElement());
 	  }
   }
 
   public Event getChristeningEvent() {
 	return new EventJDOM(JDOMUtils.findOrMakeChildElement(EventJDOM.CHRISTENING, element));
   }
 
   public Event getDeathEvent() {
 	return new EventJDOM(JDOMUtils.findOrMakeChildElement(EventJDOM.DEATH, element));
   }
 
   public Event getBurialEvent() {
 	return new EventJDOM(JDOMUtils.findOrMakeChildElement(EventJDOM.BURIAL, element));
   }
 
   public Ordinance getLDSBaptism() {
 	return new OrdinanceJDOM(JDOMUtils.findOrMakeChildElement(OrdinanceJDOM.LDS_BAPTISM, element));
   }
 
   public Ordinance getLDSConfirmation() {
 	return new OrdinanceJDOM(JDOMUtils.findOrMakeChildElement(OrdinanceJDOM.LDS_CONFIRMATION, element));
   }
 
   public Ordinance getLDSEndowment() {
 	return new OrdinanceJDOM(JDOMUtils.findOrMakeChildElement(OrdinanceJDOM.LDS_ENDOWMENT, element));
   }
 
   public Ordinance getLDSSealingToParents() {
 	return new OrdinanceJDOM(JDOMUtils.findOrMakeChildElement(OrdinanceJDOM.LDS_SEALING_PARENTS, element));
   }
 
   public boolean childrensOrdinancesAreCompleted() {
 	if (getFamilyAsSpouse().getChildren().size() == 0) {
 	  // todo: determine if no children == true
 	  return false;
 	}
 	boolean complete = true;
 	for (int i = 0; i < getFamilyAsSpouse().getChildren().size(); i++) {
 	  complete = true;
 	  Individual child = (Individual) getFamilyAsSpouse().getChildren().get(i);
 	  if (! (child.getLDSBaptism().isCompleted()
 			 && child.getLDSEndowment().isCompleted()
 			 && child.getLDSSealingToParents().isCompleted()
 			 && child
 			 .getFamilyAsSpouse()
 			 .getSealingToSpouse()
 			 .isCompleted())) {
 		return false;
 	  }
 	}
 	return complete;
   }
 
   public String getId() {
 	String id = element.getAttributeValue(ID, "");
 	if (id == null) {
 		id = "";
 	}
 	return id;
   }
 
   public int getRin() {
 	try {
 	  return Integer.parseInt(element.getChildTextTrim(RIN));
 	}
 	catch (NumberFormatException e) {
 	  return 0;
 	}
   }
 
   public boolean isLocked() {
 	return LOCKED.equalsIgnoreCase(element.getChildTextTrim(RESTRICTION));
   }
 
   public boolean isPrivate() {
 	return PRIVACY.equalsIgnoreCase(element.getChildTextTrim(RESTRICTION));
   }
 
   public URL getImagePath() {
 	try {
 	  //		if (imageURL != null) {
 //			return imageURL;
 //		}
 //		try {
 //			return new File(
 //				NSBundle.mainBundle().pathForResource("tree", "gif"))
 //				.toURL();
 //		} catch (MalformedURLException e) {
 //			log.error("Exception: ", e);
 //			//To change body of catch statement use Options | File Templates.
 //		}
 // todo implement
 //        return null;
 //		return getMultimediaLink().getURL();
 	  return new File(
 		  NSBundle.mainBundle().pathForResource("tree", "gif"))
 		  .toURL();
 	}
 	catch (MalformedURLException e) {
 	  // TODO Auto-generated catch block
 	  log.error("Exception: ", e);
 	}
 	return null;
   }
   
   private Multimedia getMultimediaLink() {
   	return new MultimediaJDOM(element.getChild("OBJE"), null);
   }
 
   public void setImagePath(URL path) {
 //		imageURL = path;
 	  throw new UnsupportedOperationException("setImagePath not implemented in IndividualJDOM");
   }
 
   public Individual getPrimarySpouse() {
 	if (getGender().equals(Gender.MALE)) {
 	  return getFamilyAsSpouse().getMother();
 	}
 	else if (getGender().equals(Gender.FEMALE)) {
 	  return getFamilyAsSpouse().getFather();
 	}
 	// todo determine primary spouse
 	return new Individual.UnknownIndividual();
   }
 
   public Individual getFather() {
 	return getFamilyAsChild().getFather();
   }
 
   public Individual getMother() {
 	return getFamilyAsChild().getMother();
   }
 
   public Family getFamilyAsChild() {
 //	if (familyAsChild == null) {
 	  Family familyAsChild = Family.UNKNOWN_FAMILY;//JDOM(document);
 	  try {
 		Element famLink = element.getChild(FAMILY_CHILD_LINK);
 		if (famLink != null) {
 //		  XPath xpath = XPath.newInstance("//FAM[@ID='" + famLink.getAttributeValue(REF) + "']");
 //		  log.debug("child xpath:" + xpath.getXPath());
 //		  Element famNode = (Element) xpath.selectSingleNode(element);
 //		  log.debug("famNode: " + famNode);
 //		  familyAsChild = new FamilyJDOM(famNode, document);
 //		  try {
 //			new XMLOutputter(Format.getPrettyFormat()).output(familyAsChild.getElement(), System.out);
 //		  }
 //		  catch (IOException e1) {
 //			e1.printStackTrace();
 //		  }
 			familyAsChild = new FamilyLink(famLink.getAttributeValue(REF), document);
 		}
 	  }
 	  catch (Exception e) {
 		log.error("Exception: ", e); //To change body of catch statement use Options | File Templates.
 	  }
 //	}
 	return familyAsChild;
   }
 
   public Family getFamilyAsSpouse() {
 //	if (familyAsSpouse == null) {
 	  Family familyAsSpouse = Family.UNKNOWN_FAMILY;
 	  try {
 		familyAsSpouse = (Family) CocoaUtils.firstObjectFromList(getFamiliesAsSpouse());
		if (familyAsSpouse == null) {
			familyAsSpouse = Family.UNKNOWN_FAMILY;
		}
 	  }
 	  catch (Exception e) {
 		log.error("Exception: ", e); //To change body of catch statement use Options | File Templates.
 	  }
 //	}
 	return familyAsSpouse;
   }
 
 public List getFamiliesAsSpouse() {
 	List families = new ArrayList();
 	List famLinks = element.getChildren(FAMILY_SPOUSE_LINK);
 	for (Iterator iter = famLinks.iterator(); iter.hasNext();) {
 		Element famLink = (Element) iter.next();		
 		System.out.println("famLink="+famLink);
 		if (famLink != null) {
 			families.add(new FamilyLink(famLink.getAttributeValue(REF), document));
 		}
 	}
 	return families;
 }
 
   public void setNotes(List newNotes) {
 //		if (note == null) {
 //			this.note = new NoteJDOM();
 //		} else {
 //			this.note = note;
 //		}
 // todo implement
 	  throw new UnsupportedOperationException("setNotes not implemented in IndividualJDOM");
   }
 
   public void setFamilyAsSpouse(Family fam) {
 //		familyAsSpouse = fam;
 //		addFamilyAsSpouse(fam);
 		element.removeChildren(FAMILY_SPOUSE_LINK);
 		element.addContent(new Element(FAMILY_SPOUSE_LINK).setAttribute(REF, fam.getId()));
   }
 
   public List getSpouseList() {
 	List spouseList = new ArrayList();
 	List families = getFamiliesAsSpouse();
 	for (Iterator iter = families.iterator(); iter.hasNext();) {
 		Family family = (Family) iter.next();		
 		if (getGender().equals(Gender.FEMALE)) {
 			spouseList.add(family.getFather());
 		}
 		else {
 			spouseList.add(family.getMother());
 		}
 	}
 	return spouseList;
   }
 
   public void setSpouseList(List spouses) {
 //		for (Iterator iter = spouses.iterator(); iter.hasNext();) {
 //			Individual element = (Individual) iter.next();
 //
 //		}
   }
 
   public List getAssociations() {
 // todo implement
 	return new ArrayList();
   }
 
   public List getNotes() {
 //		if (note == null) {
 	List notes = new ArrayList();
 	List children = element.getChildren(NoteJDOM.NOTE);
 	Iterator iter = children.iterator();
 	while (iter.hasNext()) {
 	  Element item = (Element) iter.next();
 	  notes.add(new NoteJDOM(item, document));
 	}
 	return notes;
 //		}
 //		return note;
   }
 
   public String getNoteText() {
 	String noteString = "";
 	Iterator iter = getNotes().iterator();
 	while (iter.hasNext()) {
 	  Note item = (Note) iter.next();
 	  noteString += item.getText() + System.getProperty("line.separator");
 	}
 	return noteString;
   }
 
   //	 public void addSpouse(Individual newSpouse) {
   //		spouseList.add(newSpouse);
   //	 }
 
   /* (non-Javadoc)
    * @see Individual#removeSpouse(Individual)
    */
   public void removeSpouse(Individual removedSpouse) {
 	  for (Iterator iter = element.getChildren(IndividualJDOM.FAMILY_SPOUSE_LINK).iterator(); iter.hasNext();) {
 		Element spouseElement = (Element) iter.next();
 		if (spouseElement.getAttributeValue(IndividualJDOM.REF).equals(removedSpouse.getId())) {
 			log.warn("removing spouse with id="+removedSpouse.getId()+" from individual:"+getId());
 			iter.remove();
 		}
 	}
   }
 
 //  /* (non-Javadoc)
 //   * @see Individual#setFather(Individual)
 //   */
 //  public void setFather(Individual father) {
 //	// TODO Auto-generated method stub
 //
 //  }
 
   /* (non-Javadoc)
    * @see Individual#setLDSBaptism(Ordinance)
    */
   public void setLDSBaptism(Ordinance ldsBaptism) {
 	  if (ldsBaptism instanceof Ordinance.UnknownOrdinance) {
 		  // do nothing
 		  return;
 	  } else {
 		  JDOMUtils.replaceFirstChildElement(element, new OrdinanceJDOM(ldsBaptism).getElement());
 	  }
   }
 
   /* (non-Javadoc)
    * @see Individual#setLDSConfirmation(Ordinance)
    */
   public void setLDSConfirmation(Ordinance ldsConfirmation) {
 	  if (ldsConfirmation instanceof Ordinance.UnknownOrdinance) {
 		  // do nothing
 		  return;
 	  } else {
 		  JDOMUtils.replaceFirstChildElement(element, new OrdinanceJDOM(ldsConfirmation).getElement());
 	  }
   }
 
   /* (non-Javadoc)
    * @see Individual#setLDSEndowment(Ordinance)
    */
   public void setLDSEndowment(Ordinance ldsEndowment) {
 	  if (ldsEndowment instanceof Ordinance.UnknownOrdinance) {
 		  // do nothing
 		  return;
 	  } else {
 		  JDOMUtils.replaceFirstChildElement(element, new OrdinanceJDOM(ldsEndowment).getElement());
 	  }
   }
 
   /* (non-Javadoc)
    * @see Individual#setLDSSealingToParent(Ordinance)
    */
   public void setLDSSealingToParent(Ordinance sealingToParent) {
 	  if (sealingToParent instanceof Ordinance.UnknownOrdinance) {
 		  // do nothing
 		  return;
 	  } else {
 		  JDOMUtils.replaceFirstChildElement(element, new OrdinanceJDOM(sealingToParent).getElement());
 	  }
   }
 
   /* (non-Javadoc)
    * @see Individual#setLocked(boolean)
    */
   public void setLocked(boolean lockedFlag) {
 	JDOMUtils.findOrMakeChildElement(RESTRICTION, element).setText(LOCKED);
   }
 
 //  /* (non-Javadoc)
 //   * @see Individual#setMother(Individual)
 //   */
 //  public void setMother(Individual mother) {
 //	// TODO Auto-generated method stub
 //
 //  }
 
   /* (non-Javadoc)
    * @see Individual#setPrimarySpouse(Individual)
    */
   public void setPrimarySpouse(Individual primarySpouse) {
 	// TODO Auto-generated method stub
 
   }
 
   /* (non-Javadoc)
    * @see Individual#setPrivate(boolean)
    */
   public void setPrivate(boolean privateFlag) {
 	JDOMUtils.findOrMakeChildElement(RESTRICTION, element).setText(PRIVACY);
   }
 
   /* (non-Javadoc)
    * @see Individual#setRin(int)
    */
   public void setRin(int rin) {
 	JDOMUtils.findOrMakeChildElement(RIN, element).setText("" + rin);
   }
 
   /* (non-Javadoc)
    * @see Individual#getAFN()
    */
   public String getAFN() {
 	String afn = element.getChildTextTrim(AFN);
 	if (afn == null) {
 	  afn = "";
 	}
 	return afn;
   }
 
   /* (non-Javadoc)
    * @see Individual#setAFN(java.lang.String)
    */
   public void setAFN(String afn) {
 	if (afn == null) {
 	  afn = "";
 	}
 	JDOMUtils.findOrMakeChildElement(AFN, element).setText(afn);
   }
 
   public Element getElement() {
 	return element;
   }
 
   /* (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   public String toString() {
 	return "IndividualJDOM element:" + new XMLOutputter(Format.getPrettyFormat()).outputString(element); //super.toString();
   }
 
   /**
    * getEvents
    *
    * @return List
    */
   public List getEvents() {
 //    log.debug("IndividualJDOM.getEvents()");
 //	if (events == null) {
 	  log.debug("IndividualJDOM.getEvents() creating event list");
 	  List events = new ArrayList();
 	  try {
 		XPath xpath = XPath.newInstance(eventNodeNames);
 		log.debug("xpath = " + xpath);
 		List eventNodes = xpath.selectNodes(element); //, eventNodeNames);
 		log.debug("XPath events: " + eventNodes.size());
 		Iterator iter = eventNodes.iterator();
 		while (iter.hasNext()) {
 		  EventJDOM item = new EventJDOM( (Element) iter.next());
 		  events.add(item);
 		}
 	  }
 	  catch (JDOMException ex) {
 		ex.printStackTrace();
 	  }
 	  log.debug("IndividualJDOM.getEvents() creating event list2");
 	  List events2 = new ArrayList();
 	  try {
 		
 		List eventNodes = element.getChildren();//xpath.selectNodes(element); //, eventNodeNames);
 		log.debug("child events: " + eventNodes.size());
 		Iterator iter = eventNodes.iterator();
 		while (iter.hasNext()) {
 			
 		  Element next = (Element) iter.next();
 		  if (eventNodeNames.indexOf(next.getName()) >= 0) {
 		EventJDOM item = new EventJDOM( next);
 		  events2.add(item);
 		  }
 		}
 	  }
 	  catch (Exception ex) {
 		ex.printStackTrace();
 	  }
 	  
 //	}
 log.debug("returning events: "+events.size());
 log.debug("not returning events2: "+events2.size());
 	return events;
   }
 
   public static final String eventNodeNames = "BIRT | CHR | DEAT | BURI | CREM | ADOP | BAPM | BARM | BASM | BLES | CHRA | CONF | FCOM | ORDN | NATU | EMIG | IMMI | CENS | PROB | WILL | GRAD | RETI | BAPL | CONL | ENDL | SLGC | EVEN";
 
 /* (non-Javadoc)
  * @see com.redbugz.macpaf.Individual#getPreferredImage()
  */
 public Multimedia getPreferredImage() {
 	Multimedia image = Multimedia.UNKNOWN_MULTIMEDIA;
 	Iterator iter = getAllMultimedia().iterator();
 	while (iter.hasNext()) {
 		Multimedia multimedia = (Multimedia) iter.next();
 		if (multimedia.isImage()) {
 			image = multimedia;
 			break;
 		}
 	}
 	return image;
 }
 
 /* (non-Javadoc)
  * @see com.redbugz.macpaf.Individual#getMultimediaLinks()
  */
 public List getAllMultimedia() {
 	List list = new ArrayList();
 	for (Iterator iter = element.getChildren("OBJE").iterator(); iter.hasNext();) {
 		Element theElement = (Element) iter.next();
 		if (theElement.getAttributeValue("REF") != null) {
 			list.add(new MultimediaLink(theElement.getAttributeValue("REF"), document));
 		} else {
 			list.add(new MultimediaJDOM(theElement, document));
 		}
 	}
 	return list;
 }
 
 //  public static final String eventNodeNames = "/BIRT | /CHR | /DEAT | /BURI | /CREM | /ADOP | /BAPM | /BARM | /BASM | /BLES | /CHRA | /CONF | /FCOM | /ORDN | /NATU | /EMIG | /IMMI | /CENS | /PROB | /WILL | /GRAD | /RETI | /EVEN";
 //  public static final String eventNodeNames = "//BIRT | //CHR | //DEAT | //BURI | //CREM | //ADOP | //BAPM | //BARM | //BASM | //BLES | //CHRA | //CONF | //FCOM | //ORDN | //NATU | //EMIG | //IMMI | //CENS | //PROB | //WILL | //GRAD | //RETI | //EVEN";
 }
