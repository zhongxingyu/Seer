 package cai.peter.xml;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.List;
 import java.util.Stack;
 
 import org.apache.log4j.Logger;
 import org.exolab.castor.xml.schema.AttributeDecl;
 import org.exolab.castor.xml.schema.ComplexType;
 import org.exolab.castor.xml.schema.ElementDecl;
 import org.exolab.castor.xml.schema.Group;
 import org.exolab.castor.xml.schema.Schema;
 import org.exolab.castor.xml.schema.XMLType;
 import org.exolab.castor.xml.schema.reader.SchemaReader;
 import org.xml.sax.InputSource;
 
 public class SchemaLiner {
 	/**
 	 * Logger for this class
 	 */
 	private static final Logger logger = Logger
 			.getLogger(XpathFromSchema.class);
 
 	private static Stack visitedTypes = new Stack();
 
 	private File outputFile;
 
 	private BufferedWriter bufferedWriter;
 
 	/** provide a simple method to start the dump. */
 	private void walkThroughElTree(final ElementDecl elementDecl) {
 		walkThroughElTree("", elementDecl);
 	}
 
 	private void walkThroughElTree(final String xpath, final ElementDecl elementDecl){
 		if (elementDecl == null) {
 			return;
 		}
		String elName = elementDecl.getName();
		int maxOccurs = elementDecl.getMaxOccurs();
		int minOccurs = elementDecl.getMinOccurs();
 		List forcedXpaths = new ArrayList();
 		XMLType typeReference = elementDecl.getType();
		String cardinality = ""; // Mandatory
		if( minOccurs==0 && maxOccurs ==1 ) cardinality = "?"; // optional
		if( minOccurs==0 && maxOccurs == -1 ) cardinality = "*";
		if(minOccurs==1 && maxOccurs == -1) cardinality = "+";
		String name = typeReference.getName();
		if (name != null && visitedTypes.contains(name)) {
 			// The type is already in the stack, therefore if we were to continue we would infinitely recurse.
 		} else {
			if (name != null) {
				visitedTypes.push(name);
 			}
 

			String newXpath = xpath + "/" + elName ;
 
 			System.out.println(newXpath);
			writeToFile(newXpath+cardinality);
 
 			if (typeReference.isComplexType()) {
 				ComplexType ct = (ComplexType)typeReference;
 				Enumeration attributes = ct.getAttributeDecls();
 				while (attributes.hasMoreElements()) {
 					AttributeDecl attributeDecl = (AttributeDecl)attributes.nextElement();
 					System.out.println(newXpath + "/@" + attributeDecl.getName());
 					writeToFile(newXpath + "/@" + attributeDecl.getName());
 				}
 				Enumeration particles = ct.enumerate();
 				while (particles.hasMoreElements()) {
 					Object o = particles.nextElement();
					// TODO: bug when there is only a sequence rather than an element
 					if (o instanceof Group) {
 						processGroup(newXpath, (Group)o);
 					} else {
 						System.out.println(" [dump] ***** Unknown particle type: " + o.getClass().getName());
 					}
 				}
 			}
 		}
 
		if (name != null && !visitedTypes.empty()) {
 			visitedTypes.pop();
 		}
 	}
 
 	/** I have no idea what a group is, but a little experimentation
 	 * showed the follow method to work.
 	 */
 	public void processGroup(String xpath, final Group group) {
 		Enumeration particles = group.enumerate();
 		while (particles.hasMoreElements()) {
 			Object o = particles.nextElement();
 			if (o instanceof Group) {
 				processGroup(xpath, (Group)o);
 			} else if (o instanceof ElementDecl) {
 				walkThroughElTree(xpath, (ElementDecl)o);
 			} else {
 				System.out.println("[dumpGroup] ***** Unknown particle type: " + o.getClass().getName());
 			}
 		}
 	}
 
 	public static void main(String[] args) {
 		File file = new File(args[0]);
 		new SchemaLiner().parseSchemaFile(file);
 	}
 
 	void writeToFile(String line)
 	{
 		try {
 			bufferedWriter.append(line);
 			bufferedWriter.append(System.getProperty("line.separator"));
 
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	void parseSchemaFile(File inputFile) {
 		try {
 			outputFile = new File(inputFile.toString()+".txt");
 			bufferedWriter = new BufferedWriter(new FileWriter(outputFile));
 
 			String fileURI = inputFile.toURI().toString();
 			InputSource inputSource = new InputSource(fileURI);
 			SchemaReader a = new SchemaReader(inputSource);
 			Schema s = a.read();
 
			// we handle element only
 			Enumeration elementDecls = s.getElementDecls();
 			while( elementDecls.hasMoreElements())
 			{
 				ElementDecl nextElement = (ElementDecl)elementDecls.nextElement();
 				walkThroughElTree(nextElement);
 			}
 
 			bufferedWriter.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			System.out.println("Done.");
 		}
 	}
 }
