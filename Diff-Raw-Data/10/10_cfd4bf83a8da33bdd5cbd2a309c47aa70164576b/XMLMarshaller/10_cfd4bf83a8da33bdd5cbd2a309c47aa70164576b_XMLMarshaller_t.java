 package webApplication.xml;
 
 import java.awt.Image;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.math.BigDecimal;
 import java.math.BigInteger;
 import java.util.List;
 
 import javax.swing.ImageIcon;
 import javax.swing.JTree;
 import javax.swing.SwingWorker;
 import javax.xml.XMLConstants;
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.Marshaller;
 import javax.xml.validation.Schema;
 import javax.xml.validation.SchemaFactory;
 
 import webApplication.business.Componente;
 import webApplication.business.ComponenteAlternative;
 import webApplication.business.ComponenteMolteplice;
 import webApplication.business.ComponenteSemplice;
 import webApplication.business.Immagine;
 import webApplication.business.Link;
 import webApplication.business.Testo;
 import webApplication.grafica.DisabledNode;
 
 public class XMLMarshaller extends SwingWorker<Void, Void> {
 	
 	private static final String SCHEMABASEPATH = "xml/";
 	private static final String SCHEMANAME = "CATree.xsd";
 	
 	private JAXBContext content;
 	private ObjectFactory factory;
 	private JTree tree;
 	private File file;
 	
 	public XMLMarshaller(JTree tree, File file) {
 		this.tree = tree;
 		this.file = file;
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	protected Void doInBackground() throws Exception {
 		content = JAXBContext.newInstance("webApplication.xml");
 		factory = new ObjectFactory();
 		CARoot root = factory.createCARoot();
 		List<Object> list = root.getECIOrCINode();
 		//leggere l'albero e creare gli elementi appositi
 		DisabledNode rootNode = (DisabledNode) tree.getModel().getRoot();
 		for (int i=0; i<rootNode.getChildCount(); i++) {
 			Componente componente = (Componente)((DisabledNode) rootNode.getChildAt(i)).getUserObject(); 
 			Object element = createElement(componente);
 			list.add(element);
 		}
 		Marshaller marshaller = content.createMarshaller();
 		SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Schema schema = schemaFactory.newSchema(this.getClass().getClassLoader().getResource(SCHEMABASEPATH+SCHEMANAME));
 		marshaller.setSchema(schema);
 		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
 		FileOutputStream output = new FileOutputStream(file);
 		marshaller.marshal(root, output);
 		output.close();
 		return null;
 	}
 	
 	/**
 	 * Create an element to add at the root
 	 * @param comp	The node to convert
 	 * @return		The node converted 
 	 */
 	private Object createElement(Componente comp) {
 		Object element;
 		if (comp.isSimple()) {
 			element = createSimpleElement((ComponenteSemplice) comp);
 		} else {
 			element = createComplexElement((ComponenteMolteplice)comp);
 		}
 		return element;
 	}
 	
 	/**
 	 * Create an element from an advanced type node
 	 * @param comp	The node to convert
 	 * @return		The node converted
 	 */
 	private Object createComplexElement(ComponenteMolteplice comp) {
 		CINodeType node = factory.createCINodeType();
 		node.setID(comp.getNome());
 		List<Object> list = node.getECIOrCINode();
 		if (comp.getType().equals(ComponenteAlternative.ALTERNATIVETYPE)) {
 			node.setType(CAandxor.XOR);
 			for (int i=0; i<comp.getOpzioni().size(); i++) {
 				list.add(createElementInAlternative(comp.getOpzione(i), i, comp.getOpzioni().size()));
 			}
 		} else {
 			node.setType(CAandxor.AND);
 			for (int i=0; i<comp.getOpzioni().size(); i++) {
 				list.add(createSimpleElement(comp.getOpzione(i)));
 			}
 		}
 		node.setCategory(comp.getCategoria());
 		node.setNecessity(convertRepresentationValue(comp.getVisibilita()));
 		node.setRelevance(convertRepresentationValue(comp.getEnfasi()));
 		return node;
 	}
 	
 	/**
 	 * Create an element from a simple type node
 	 * @param comp	The node to convert
 	 * @return		The node converted
 	 */
 	private Object createSimpleElement(ComponenteSemplice comp) {
 		ECIType node = factory.createECIType();
 		node.setID(comp.getNome());
 		node.setCategory(comp.getCategoria());
 		node.setNecessity(convertRepresentationValue(comp.getVisibilita()));
 		node.setRelevance(convertRepresentationValue(comp.getEnfasi()));
 		if (comp.getType().equals(Testo.TEXTTYPE)) {
 			node.setType(TypeOfECI.TEXT);
 			node.setCharCount(new BigInteger(Integer.toString(((Testo)comp).getTesto().length())));
 			node.setValue(((Testo)comp).getTesto());
 		} else if (comp.getType().equals(Immagine.IMAGETYPE)) {
 			node.setType(TypeOfECI.IMAGE);
 			ImageIcon icon = new ImageIcon(((Immagine)comp).getPath());
 			Image img = icon.getImage();
 			node.setWidth(new BigInteger(Integer.toString(img.getWidth(null))));
 			node.setHeight(new BigInteger(Integer.toString(img.getHeight(null))));
 			node.setValue(((Immagine)comp).getPath()); //spostare l'immagine in un path relativo???
 		} else {
 			node.setType(TypeOfECI.LINK);
 			node.setCharCount(new BigInteger(Integer.toString(((Link)comp).getUri().length())));
 			node.setValue(((Link)comp).getUri());
 			//Link ha solo un campo value che salva il link...e il testo???
 		}
 		return node;
 	}
 	
 	/**
 	 * Create an element from a simple type node included in an Alternative type node 
 	 * @param comp	The node to convert
 	 * @param pos	The position of the node inside the Alternative node list
 	 * @param size	The length of the Alternative node list
 	 * @return		The node converted
 	 */
 	private Object createElementInAlternative(ComponenteSemplice comp, int pos, int size) {
 		ECIType node = (ECIType) createSimpleElement(comp);
 		node.setCoverage(calculateCoverageValue(pos, size));
 		return node;
 	}
 	
 	/**
 	 * Convert the value of the node representation parameter (emphasis or necessity)
 	 * @param i	The value to convert
 	 * @return	The value converted
 	 */
 	private BigDecimal convertRepresentationValue(int i) {
 		String value = "0."+(int)((1 - (i / 3 + 0.2)) * 100);
 		return new BigDecimal(value);
 	}
 	
 	/**
 	 * Calculate the coverage value for a node inside an Alternative type node list
 	 * @param j		The position of the node in the list
 	 * @param size	The size of the list
 	 * @return		The corrisponding value
 	 */
 	private BigDecimal calculateCoverageValue(int j, int size) {
 		double passo = 0.8 / size;
 		String value = "0."+(int) ((0.9 - (passo * j)) * 100);
 		return new BigDecimal(value);
 	}
 
 }
