 /**
  * TODO
  * Parseaza fisierul de layout XML si reprezinta datele in memorie.
  * 
  * 1) Metode pentru a naviga prin fisierul de layout XML.
  *      - nextElement()
  *      - prevElement()
  * 
  * 2) Metode pentru a uni 2 elemente.
  *      - mergeElement()
  * 
  * 3) Medoda pentru a salva datele intr-un XML:
  * 	    - saveLayout()
  * 
  * @author Unknown-Revengers
  */
 
 import static org.joox.JOOX.$;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.nio.MappedByteBuffer;
 import java.nio.channels.FileChannel;
 import java.nio.charset.Charset;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.joox.Match;
 import org.w3c.dom.Attr;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 import tree.GenericTree;
 import tree.GenericTreeNode;
 
 public class LayoutParser {
 	
     // Pagina va fi tinuta intr-un arbore
     GenericTree<LayoutParserTreeElement> XMLTree;
     String xmlPath;
     String imagePath;
     String direction;
 
     /**
      * Constructorul clasei
      * 
      * Reprezinta datele din fisierul de layout in memorie
      * 
      * @param String
      *            xmlPath Aici va fi retinuta calea catre XML. Calea este
      *            absoluta.
      */
     public LayoutParser(String xmlPath) {
     	String xmlExample = "";
     	this.xmlPath = xmlPath;
     
     	// Citeste continutul XML-ului
     	try {
     	    xmlExample = this.readFile(xmlPath);
     	} catch (IOException e) {
     		ErrorMessage.show("EROARE: XML-ul nu a fost citit cum trebuie");
     	    e.printStackTrace();
     	}
     
     	// Creaza arborele care va tine minte structura paginii
     	this.XMLTree = this.parseXML(xmlExample);
     
     	// System.out.println(this.XMLTree.getRoot().getChildren());
     }
 
     /**
      * Parseaza arborele dat ca parametru si construieste XML-ul de layout din
      * acesta
      * 
      * @return string Returneaza XML-ul rezultat din parsarea arborelui ce va
      *         contine informatii despre pagina
      * 
      * @throws TransformerException
      */
     public String constructXml()
 	    throws TransformerException {
     	String result_xml = null;
     	DocumentBuilderFactory docFactory = DocumentBuilderFactory
     		.newInstance();
     	DocumentBuilder docBuilder = null;
     	try {
     	    docBuilder = docFactory.newDocumentBuilder();
     	} catch (ParserConfigurationException e) {
     	    System.out
     		    .println("EROARE: A fost o eroare cand a fost creat documentul");
     	    e.printStackTrace();
     	}
     
     	// Creaza noul document
     	Document doc = docBuilder.newDocument();
     	Element rootElement = doc.createElement(XMLTree.getRoot().toString());
     
     	// Adauga radacina arborelui
     	doc.appendChild(rootElement);
     
     	// Parseaza si creaza tot arborele
     	doc = addElements(doc, rootElement, XMLTree.getRoot());
     
     	// Returneaza XML-ul sub forma de String din obiectul de tip DOM
     	TransformerFactory transformerFactory = TransformerFactory
     		.newInstance();
     	Transformer transformer = transformerFactory.newTransformer();
     	DOMSource source = new DOMSource(doc);
     	StringWriter sw = new StringWriter();
     	StreamResult result = new StreamResult(sw);
     	transformer.transform(source, result);
     	result_xml = sw.toString();
     
     	return result_xml;
     }
 
     /**
      * In aceasta metoda a fost implementata logica de parsare a arborelui
      * pentru a construi XML-ul
      * 
      * @param doc
      *            Arborele care este construit pentru a crea din el XML-ul
      * 
      * @param currentElement
      *            Elementul curent din arborele din care se va crea XML-ul in
      *            care ne aflam
      * 
      * @param Node
      *            Nodul curent din arborele in care e retinuta logica paginii
      * 
      * @return Document Intoarce arborele din care se va contstrui XML-ul
      * 
      * @throws TransformerException
      */
     private Document addElements(Document doc, Element currentElement,
 	    GenericTreeNode<LayoutParserTreeElement> Node)
 	    throws TransformerException {
     	Element child;
     
     	// Gaseste copiii nodului curent
     	List<GenericTreeNode<LayoutParserTreeElement>> children = Node
     		.getChildren();
     	Iterator<GenericTreeNode<LayoutParserTreeElement>> it = children
     		.iterator();
     
     	// Itereaza prin fiecare copil
     	while (it.hasNext()) {
     	    GenericTreeNode<LayoutParserTreeElement> childNode = it.next();
     	    LayoutParserTreeElement childElement = childNode.getData();
    	    
     	    if (childElement.text.isEmpty() == false &&
     		    childElement.toString().compareTo("String") == 0) {
     		// Este frunza
     		child = doc.createElement(childElement.toString());
     		child.appendChild(doc.createTextNode(childElement.text
     			.toString()));
     		currentElement.appendChild(child);
     	    } else {
     		// Creaza elementul
     		child = doc.createElement(childElement.toString());
     
     		// Adauga atributele
     		Attr bottom = doc.createAttribute("bottom");
     		bottom.setValue(Integer.toString(childElement.bottom));
     		child.setAttributeNode(bottom);
     		Attr top = doc.createAttribute("top");
     		top.setValue(Integer.toString(childElement.top));
     		child.setAttributeNode(top);
     		Attr right = doc.createAttribute("right");
     		right.setValue(Integer.toString(childElement.right));
     		child.setAttributeNode(right);
     		Attr left = doc.createAttribute("left");
     		left.setValue(Integer.toString(childElement.left));
     		child.setAttributeNode(left);
     
     		// Adauga elementul la arbore
     		currentElement.appendChild(child);
     	    }
     
     	    // Merge mai jos in arbore
     	    addElements(doc, child, childNode);
     	}
     
     	return doc;
     }
 
     /**
      * Citeste XML-ul dintr-un fisier primit ca parametru
      * 
      * @param path
      *            Calea catre fisier
      * 
      * @return Returneaza XML-ul intr-un String
      * @throws IOException
      */
     private String readFile(String path) throws IOException {
     	FileInputStream stream = new FileInputStream(new File(path));
     	try {
     	    FileChannel fc = stream.getChannel();
     	    MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0,
     		    fc.size());
     
     	    return Charset.defaultCharset().decode(bb).toString();
     	} finally {
     	    stream.close();
     	}
     }
 
     /**
      * Parseaza XML-ul primit ca String si returneaza un arbore de tip
      * GenericTree
      * 
      * @param string
      *            layoutXML XML-ul ce contine informatii despre pagina
      * 
      * @return GenericTree<LayoutParserTreeElement> Arborele ce va contine
      *         informatii despre pagina dupa ce a parsat XML-ul
      */
     private GenericTree<LayoutParserTreeElement> parseXML(String layoutXML) {
     	GenericTree<LayoutParserTreeElement> newTree = new GenericTree<LayoutParserTreeElement>();
     
     	Document result = null;
     	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
     	InputSource source = new InputSource(new StringReader(layoutXML));
     
     	try {
     	    result = factory.newDocumentBuilder().parse(source);
     	} catch (SAXException e) {
     		ErrorMessage.show("Eroare SAX");
     	    e.printStackTrace();
     	} catch (IOException e) {
     		ErrorMessage.show("Eroare IOException");
     	    e.printStackTrace();
     	} catch (ParserConfigurationException e) {
     		ErrorMessage.show("Eroare ParserConfigurationException");
     	    e.printStackTrace();
     	}
     
     	// Parsare XML
     	Match documentRoot = $(result).first();
     
     	// Salveaza imaginea si directia din tag-ul Documentului
     	saveImageFromXML(documentRoot);
     
     	// Parseaza XML-ul si intoarce
     	GenericTreeNode<LayoutParserTreeElement> rootDocument = parseXMLRow(documentRoot);
     
     	// Creaza arbore din structura de noduri
     	newTree.setRoot(rootDocument);
     
     	return newTree;
     }
 
     /**
      * Parseaza XML-ul folosing DFS si in acelasi timp creaza arborele
      * 
      * @param currentMatch
      *            Reprezinta elementul curent
      * 
      * @return GenericTreeNode<LayoutParserTreeElement> Returneaza nodul curent
      */
     private GenericTreeNode<LayoutParserTreeElement> parseXMLRow(
 	    Match currentMatch) {
     	int i;
     	int top = -1;
     	int bottom;
     	int right;
     	int left;
     
     	// Parsam atributele ca sa nu dea eroare
     	if (currentMatch.attr("top") != null) {
     	    top = Integer.parseInt(currentMatch.attr("top"));
     	} else {
     	    top = -1;
     	}
     
     	if (currentMatch.attr("bottom") != null) {
     	    bottom = Integer.parseInt(currentMatch.attr("bottom"));
     	} else {
     	    bottom = -1;
     	}
     
     	if (currentMatch.attr("left") != null) {
     	    left = Integer.parseInt(currentMatch.attr("left"));
     	} else {
     	    left = -1;
     	}
     
     	if (currentMatch.attr("right") != null) {
     	    right = Integer.parseInt(currentMatch.attr("right"));
     	} else {
     	    right = -1;
     	}
     
     	// Suntem in frunza
     	if (currentMatch.children().size() == 0) {
     	    LayoutParserTreeElement new_element = new LayoutParserTreeElement(
     		    currentMatch.tag(),
     		    currentMatch.content(), top, bottom, right, left,
     		    currentMatch.attr("image"));
     	    return new GenericTreeNode<LayoutParserTreeElement>(new_element);
     	}
     
     	// Cream nod parinte
     	LayoutParserTreeElement rootElement = new LayoutParserTreeElement(
     		currentMatch.tag(),
     		currentMatch.content(), top, bottom, left, right,
     		currentMatch.attr("image"));
     	GenericTreeNode<LayoutParserTreeElement> parentTreeNode = new GenericTreeNode<LayoutParserTreeElement>(
     		rootElement);
     
     	// Parsam copiii
     	for (i = 0; i < currentMatch.children().size(); i++) {
     	    Match textLineElement = currentMatch.child(i);
     	    GenericTreeNode<LayoutParserTreeElement> newTreeNode = parseXMLRow(textLineElement);
     	    parentTreeNode.addChild(newTreeNode);
     	}
     
     	// Intoarcem nodul parinte
     	return parentTreeNode;
     }
 
     /**
      * 
      * @return String Returneaza calea catre imagine
      */
     private String getImagePath() {
     	return this.imagePath;
     }
 
     /**
      * Muta un nod de la un parinte la altul intr-un arbore
      * 
      * @param movingNode
      *            Nodul mutat
      * @param toParentNode
      *            Nodul parinte destinatie
      * 
      * @return boolen True daca operatia a fost indeplinita cu succes, sau false
      *         altfel
      */
     public boolean moveChildToParent(
 	    GenericTreeNode<LayoutParserTreeElement> movingNode,
 	    GenericTreeNode<LayoutParserTreeElement> toParentNode) {
     	int i;
     
     	// Gaseste parintele nodului care va fi mutat
     	GenericTreeNode<LayoutParserTreeElement> parent = movingNode
     		.getParent();
     
     	// Adauga nodul mutat la noul nod
     	toParentNode.addChild(movingNode);
     
     	// Sterge nodul mutat de la vechiul parinte
     	List<GenericTreeNode<LayoutParserTreeElement>> parentChildrenList = parent
     		.getChildren();
     	for (i = 0; i < parentChildrenList.size(); i++) {
     	    if (parentChildrenList.get(i) == movingNode) {
     		// Found child
     		parent.removeChildAt(i);
     		break;
     	    }
     	}
     
     	return true;
     }
 
     /**
      * Salveaza calea catre imagine din radacina XML-ului. Functia va functiona
      * cum trebuie chiar daca calea XML-ului este relativa sau absoluta
      * 
      * @param documentRoot
      *            Radacina documentului
      * 
      */
     private void saveImageFromXML(Match documentRoot) {
     	if (documentRoot.attr("image") != null) {
     	    if (xmlPath.contains("\\")) {
     		String auxImagePath = documentRoot.attr("image");
     		int subpathIndex = this.xmlPath.lastIndexOf("\\") + 1;
     		this.imagePath = this.xmlPath.substring(0, subpathIndex)
     			+ auxImagePath;
     	    } else {
     		this.imagePath = documentRoot.attr("image");
     	    }
     	}
     
     	if (documentRoot.attr("direction") != null) {
     	    this.direction = documentRoot.attr("direction");
     	}
     }
 }
