 package hu.kazocsaba.memento;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.util.ArrayList;
 import java.util.ConcurrentModificationException;
 import java.util.Iterator;
 
 import java.util.List;
 import java.util.Objects;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import net.iharder.Base64;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.w3c.dom.Text;
 import org.xml.sax.SAXException;
 
 /**
  * Static functions for saving and loading mementos.
  * 
  * @author Kazó Csaba
  */
 public class MementoStore {
 	private MementoStore() {}
 
 	/**
 	 * Writes a memento to a file in binary format. Mementos written with this function can be read using
 	 * {@link #binaryFileToMemento(Path)}.
 	 * 
 	 * @param memento a memento
 	 * @param file the file in which to write the memento
 	 * @throws IOException if an I/O error occurs
 	 */
 	public static void mementoToBinaryFile(Memento memento, Path file) throws IOException {
 		Objects.requireNonNull(memento, "null memento");
 		Objects.requireNonNull(file, "null file");
 		try (OutputStream out=new BufferedOutputStream(Files.newOutputStream(file))) {
 			mementoToBinary(memento, out);
 		}
 	}
 	
 	/**
 	 * Reads a memento from a binary file. This function expects a file created using
 	 * {@link #mementoToBinaryFile(Memento, Path)}.
 	 * 
 	 * @param file the file to read from
 	 * @return the memento
 	 * @throws IOException if an I/O error occurs
 	 * @throws MementoFormatException if the format of the file is incorrect
 	 */
 	public static Memento binaryFileToMemento(Path file) throws IOException, MementoFormatException {
 		Objects.requireNonNull(file, "null file");
 		try (InputStream in=new BufferedInputStream(Files.newInputStream(file))) {
 			return binaryToMemento(in);
 		}
 	}
 	
 	/**
 	 * Writes a memento to a stream in binary format. The data written this way can be safely read using
	 * {@link #binaryToMemento(InputStream), even if additional data is appended to the stream after the
 	 * memento.
 	 * 
 	 * @param memento the memento
 	 * @param out the stream to write to
 	 * @throws IOException if an I/O error occurs
 	 */
 	public static void mementoToBinary(Memento memento, OutputStream out) throws IOException {
 		Objects.requireNonNull(memento, "null memento");
 		Objects.requireNonNull(out, "null stream");
 		DataOutputStream dos=out instanceof DataOutputStream ? (DataOutputStream)out : new DataOutputStream(out);
 		saveMementoBinary(memento, dos);
 	}
 	/**
 	 * Reads a memento to a stream in binary format. This function expects data written using
 	 * {@link #mementoToBinary(Memento, OutputStream)} and reads the exact same number of bytes as
 	 * {@code mementoToBinary} wrote.
 	 * 
 	 * @param in the input stream to read from
 	 * @return the memento
 	 * @throws IOException if an I/O error occurs
 	 * @throws MementoFormatException if the format of the data is incorrect
 	 */
 	public static Memento binaryToMemento(InputStream in) throws IOException, MementoFormatException {
 		DataInputStream dis=in instanceof DataInputStream ? (DataInputStream)in : new DataInputStream(in);
 		boolean hasType=dis.readBoolean();
 		String type=hasType ? dis.readUTF() : null;
 		return loadMementoBinary(dis,new Memento(type));
 	}
 	private static Memento loadMementoBinary(DataInputStream dis,Memento memento) throws IOException, MementoFormatException {
 		int count=dis.readInt();
 		while (count-->0) {
 			String key=dis.readUTF();
 			byte type=dis.readByte();
 			switch (type) {
 				case 0:
 					memento.putString(key,dis.readUTF());
 					break;
 				case 1:
 					memento.putInt(key,dis.readInt());
 					break;
 				case 2:
 					memento.putFloat(key,dis.readFloat());
 					break;
 				case 3:
 					memento.putBoolean(key,dis.readBoolean());
 					break;
 				case 4:
 					memento.putChar(key,dis.readChar());
 					break;
 				case 5:
 					String[] sa=new String[dis.readInt()];
 					for (int i=0; i<sa.length; i++)
 						sa[i]=dis.readUTF();
 					memento.putStringArray(key,sa);
 					break;
 				case 6:
 					int[] ia=new int[dis.readInt()];
 					for (int i=0; i<ia.length; i++)
 						ia[i]=dis.readInt();
 					memento.putIntArray(key,ia);
 					break;
 				case 7:
 					byte[] ba=new byte[dis.readInt()];
 					dis.readFully(ba);
 					memento.putByteArray(key,ba);
 					break;
 				case 8:
 					memento.putDouble(key, dis.readDouble());
 					break;
 				case 9:
 					memento.putLong(key, dis.readLong());
 					break;
 				case 10:
 					double[] da=new double[dis.readInt()];
 					for (int i=0; i<da.length; i++)
 						da[i]=dis.readDouble();
 					memento.putDoubleArray(key, da);
 					break;
 				default:
 					throw new MementoFormatException("Unknown type: "+type);
 			}
 		}
 		count=dis.readInt();
 		while (count-->0) {
 			boolean hasType=dis.readBoolean();
 			String type=hasType ? dis.readUTF() : null;
 			loadMementoBinary(dis,memento.createChild(type));
 		}
 		return memento;
 	}
 	private static void saveMementoBinary(Memento memento,DataOutputStream dos) throws IOException {
 		if (memento.getType()==null)
 			dos.writeBoolean(false);
 		else {
 			dos.writeBoolean(true);
 			dos.writeUTF(memento.getType());
 		}
 		int propertiesToWrite=memento.getPropertyCount();
 		dos.writeInt(propertiesToWrite);
 		try {
 			for (Iterator<String> i=memento.iterateProperties(); i.hasNext();propertiesToWrite--) {
 				String key=i.next();
 				dos.writeUTF(key);
 				Class<?> type=memento.getPropertyType(key);
 				if (type==String.class) {
 					dos.writeByte(0);
 					dos.writeUTF(memento.getString(key));
 				} else if (type==Integer.class) {
 					dos.writeByte(1);
 					dos.writeInt(memento.getInt(key));
 				} else if (type==Float.class) {
 					dos.writeByte(2);
 					dos.writeFloat(memento.getFloat(key));
 				} else if (type==Boolean.class) {
 					dos.writeByte(3);
 					dos.writeBoolean(memento.getBoolean(key));
 				} else if (type==Character.class) {
 					dos.writeByte(4);
 					dos.writeChar(memento.getChar(key));
 				} else if (type==String[].class) {
 					dos.writeByte(5);
 					String[] value=memento.getStringArray(key);
 					dos.writeInt(value.length);
 					for (String s:value) dos.writeUTF(s);
 				} else if (type==Integer[].class) {
 					dos.writeByte(6);
 					int[] value=memento.getIntArray(key);
 					dos.writeInt(value.length);
 					for (int in:value) dos.writeInt(in);
 				} else if (type==Byte[].class) {
 					dos.writeByte(7);
 					byte[] value=memento.getByteArray(key);
 					dos.writeInt(value.length);
 					dos.write(value);
 				} else if (type==Double.class) {
 					dos.writeByte(8);
 					dos.writeDouble(memento.getDouble(key));
 				} else if (type==Long.class) {
 					dos.writeByte(9);
 					dos.writeLong(memento.getLong(key));
 				} else if (type==Double[].class) {
 					dos.writeByte(10);
 					double[] value=memento.getDoubleArray(key);
 					dos.writeInt(value.length);
 					for (double d:value) dos.writeDouble(d);
 				} else
 					throw new IllegalStateException("Unknown type: "+type);
 			}
 		} catch (MementoFormatException e) {
 			throw new Error(e);
 		}
 		if (propertiesToWrite!=0)
 			throw new ConcurrentModificationException("Didn't write as many properties as there were");
 		int childrenToWrite=memento.getChildCount();
 		dos.writeInt(childrenToWrite);
 		for (Iterator<Memento> i=memento.iterateChildren(); i.hasNext(); childrenToWrite--)
 			saveMementoBinary(i.next(),dos);
 		if (childrenToWrite!=0)
 			throw new ConcurrentModificationException("Didn't write as many children as there were");
 	}
 	
 	/**
 	 * Creates an XML document defining a memento.
 	 * 
 	 * @param memento the memento to write
 	 * @return the XML document storing the memento
 	 */
 	public static Document mementoToXml(Memento memento) {
 		Objects.requireNonNull(memento, "null memento");
 		Document doc;
 		try {
 			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
 		} catch (ParserConfigurationException e) {
 			throw new AssertionError(e);
 		}
 		doc.appendChild(createMementoElement(memento, doc));
 		return doc;
 	}
 	
 	/**
 	 * Writes a memento to a stream in XML format.
 	 * 
 	 * @param memento the memento to write
 	 * @param out the stream to write to
 	 * @throws IOException if an I/O error occurs
 	 */
 	public static void mementoToXmlStream(Memento memento, OutputStream out) throws IOException {
 		Objects.requireNonNull(memento, "null memento");
 		Objects.requireNonNull(out, "null stream");
 		try {
 			Transformer transformer=TransformerFactory.newInstance().newTransformer();
 			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
 			transformer.transform(new DOMSource(mementoToXml(memento)), new StreamResult(out));
 		} catch (TransformerException e) {
 			throw new AssertionError(e);
 		}
 	}
 	
 	/**
 	 * Writes a memento to a file in XML format.
 	 * 
 	 * @param memento the memento to write
 	 * @param file the file to write to
 	 * @throws IOException if an I/O error occurs
 	 */
 	public static void mementoToXmlFile(Memento memento, Path file) throws IOException {
 		try {
 			Transformer transformer=TransformerFactory.newInstance().newTransformer();
 			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
 			transformer.transform(new DOMSource(mementoToXml(memento)), new StreamResult(file.toFile()));
 		} catch (TransformerException e) {
 			throw new AssertionError(e);
 		}
 	}
 	
 	private static Element createMementoElement(Memento memento, Document doc) {
 		Element mementoElement = doc.createElement("memento");
 		if (memento.getType()!=null)
 			mementoElement.setAttribute("type",memento.getType());
 		try {
 			for (Iterator<String> keys = memento.iterateProperties(); keys
 					.hasNext();) {
 				String key = keys.next();
 				Element property = doc.createElement("property");
 				mementoElement.appendChild(property);
 				Element keyElem = doc.createElement("key");
 				property.appendChild(keyElem);
 				keyElem.appendChild(doc.createTextNode(key));
 				Element valueElem = doc.createElement("value");
 				property.appendChild(valueElem);
 				Class<?> valueType = memento.getPropertyType(key);
 				if (valueType == String.class) {
 					valueElem.setAttribute("type", "string");
 					valueElem.appendChild(doc.createTextNode(memento
 							.getString(key)));
 				} else if (valueType == Integer.class) {
 					valueElem.setAttribute("type", "integer");
 					valueElem.appendChild(doc.createTextNode(Integer
 							.toString(memento.getInt(key))));
 				} else if (valueType == Long.class) {
 					valueElem.setAttribute("type", "long");
 					valueElem.appendChild(doc.createTextNode(Long
 							.toString(memento.getLong(key))));
 				} else if (valueType == Float.class) {
 					valueElem.setAttribute("type", "float");
 					valueElem.appendChild(doc.createTextNode(Float
 							.toString(memento.getFloat(key))));
 				} else if (valueType == Boolean.class) {
 					valueElem.setAttribute("type", "boolean");
 					valueElem.appendChild(doc.createTextNode(Boolean.toString(memento.getBoolean(key))));
 				} else if (valueType == Character.class) {
 					valueElem.setAttribute("type", "character");
 					valueElem.appendChild(doc.createTextNode(Character.toString(memento.getChar(key))));
 				} else if (valueType == String[].class) {
 					valueElem.setAttribute("type", "string[]");
 					for (String s:memento.getStringArray(key)) {
 						Element itemElement=doc.createElement("item");
 						valueElem.appendChild(itemElement);
 						itemElement.appendChild(doc.createTextNode(s));
 					}
 				} else if (valueType == Integer[].class) {
 					valueElem.setAttribute("type","integer[]");
 					for (int i:memento.getIntArray(key)) {
 						Element itemElement=doc.createElement("item");
 						valueElem.appendChild(itemElement);
 						itemElement.appendChild(doc.createTextNode(Integer.toString(i)));
 					}
 				} else if (valueType == Byte[].class) {
 					valueElem.setAttribute("type","byte[]");
 					valueElem.appendChild(doc.createTextNode(encodeByteArray(memento.getByteArray(key))));
 				} else if (valueType == Double.class) {
 					valueElem.setAttribute("type", "double");
 					valueElem.appendChild(doc.createTextNode(Double.toString(memento.getDouble(key))));
 				} else if (valueType == Double[].class) {
 					valueElem.setAttribute("type", "double[]");
 					for (double d:memento.getDoubleArray(key)) {
 						Element itemElement=doc.createElement("item");
 						valueElem.appendChild(itemElement);
 						itemElement.appendChild(doc.createTextNode(Double.toString(d)));
 					}
 				} else
 					throw new IllegalStateException("Unknown value type: " + valueType);
 			}
 		} catch (NoSuchPropertyException | TypeMismatchException e) {
 			throw new AssertionError(e);
 		}
 		for (Iterator<Memento> children = memento.iterateChildren(); children.hasNext();) {
 			mementoElement.appendChild(createMementoElement(children.next(), doc));
 		}
 		return mementoElement;
 	}
 	
 	/**
 	 * Reads a memento from an XML document.
 	 * 
 	 * @param doc the document storing the memento
 	 * @return the memento
 	 * @throws MementoFormatException if the format of the document is incorrect
 	 */
 	public static Memento xmlToMemento(Document doc) throws MementoFormatException {
 		Element mementoElement=doc.getDocumentElement();
 		if (mementoElement==null || !"memento".equals(mementoElement.getNodeName()))
 			throw new MementoFormatException("Expected 'memento' root element");
 		Memento root=new Memento();
 		fillMementoFromElement(root,mementoElement);
 		return root;
 	}
 	
 	/**
 	 * Reads a memento from a stream in XML format.
 	 * 
 	 * @param in the stream to read from
 	 * @return the memento
 	 * @throws IOException if an I/O error occurs
 	 * @throws MementoFormatException if the format of the data is incorrect
 	 */
 	public static Memento xmlStreamToMemento(InputStream in) throws IOException, MementoFormatException {
 		try {
 			return xmlToMemento(DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in));
 		} catch (SAXException e) {
 			throw new MementoFormatException(e);
 		} catch (ParserConfigurationException e) {
 			throw new AssertionError(e);
 		}
 	}
 	
 	/**
 	 * Reads a memento from a file in XML format.
 	 * 
 	 * @param file the file to read from
 	 * @return the memento
 	 * @throws IOException if an I/O error occurs
 	 * @throws MementoFormatException if the format of the data is incorrect
 	 */
 	public static Memento xmlFileToMemento(Path file) throws IOException, MementoFormatException {
 		try {
 			return xmlToMemento(DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file.toFile()));
 		} catch (SAXException e) {
 			throw new MementoFormatException(e);
 		} catch (ParserConfigurationException e) {
 			throw new AssertionError(e);
 		}
 	}
 	private static void fillMementoFromElement(Memento memento, Node mementoElement) throws MementoFormatException {
 		NodeList children=mementoElement.getChildNodes();
 		for (int i=0; i<children.getLength(); i++) {
 			Node child=children.item(i);
 			if (child.getNodeType()!=Node.ELEMENT_NODE) continue;
 			switch (child.getNodeName()) {
 				case "memento":
 					Element childMementoElement=(Element)child;
 					String type=childMementoElement.hasAttribute("type") ? childMementoElement.getAttribute("type") : null;
 					fillMementoFromElement(memento.createChild(type),child);
 					break;
 				case "property":
 					fillMementoPropertyFromElement(memento, child);
 					break;
 				default:
 					throw new MementoFormatException("Unexpected element: "+child.getNodeName());
 			}
 		}
 	}
 	private static String getStringValue(Element valueElement) throws MementoFormatException {
 		NodeList valueChildren=valueElement.getChildNodes();
 		if (valueChildren.getLength()==0) return "";
 		if (valueChildren.getLength()==1 && valueChildren.item(0).getNodeType()==Node.TEXT_NODE)
 			return valueChildren.item(0).getNodeValue();
 		throw new MementoFormatException("A single text child of '"+valueElement.getNodeName()+"' element expected");
 	}
 	private static void fillMementoPropertyFromElement(Memento memento,Node propertyElement) throws MementoFormatException {
 		String key=null;
 		String value=null;
 		Element valueElement=null;
 		String type=null;
 		NodeList children=propertyElement.getChildNodes();
 		for (int i=0; i<children.getLength(); i++) {
 			Node child=children.item(i);
 			if (child.getNodeType()!=Node.ELEMENT_NODE) continue;
 			if (key==null) {
 				if (!"key".equals(child.getNodeName()))
 					throw new MementoFormatException("Expected 'key' element, found '"+child.getNodeName()+"'");
 				NodeList keyChildren=child.getChildNodes();
 				if (keyChildren.getLength()!=1 || keyChildren.item(0).getNodeType()!=Node.TEXT_NODE)
 					throw new MementoFormatException("'key' element should only have a single text child");
 				key=keyChildren.item(0).getNodeValue();
 			} else if (value==null) {
 				if (!"value".equals(child.getNodeName()))
 					throw new MementoFormatException("Expected 'value' element, found '"+child.getNodeName()+"'");
 				valueElement=(Element)child;
 				if (child.getAttributes().getLength()!=1)
 					throw new MementoFormatException("'value' element should have a single 'type' attribute");
 				if (!valueElement.hasAttribute("type"))
 					throw new MementoFormatException("'value' element should have a 'type' attribute");
 				type=valueElement.getAttribute("type");
 				if (type.length()==0) throw new MementoFormatException("Property type missing");
 				if (!type.endsWith("[]") || type.equals("byte[]"))
 					value=getStringValue(valueElement);
 			} else throw new MementoFormatException("Unexpected element: "+child.getNodeName());
 		}
 		if (key==null)
 			throw new MementoFormatException("Missing 'key' element in property declaration");
 		if (valueElement==null)
 			throw new MementoFormatException("Missing 'value' element in property declaration");
 		if (memento.getPropertyType(key)!=null)
 			throw new MementoFormatException("Found memento with duplicate properties");
 		switch (type) {
 			case "string":
 				memento.putString(key, value);
 				break;
 			case "integer":
 				try {
 					memento.putInt(key, Integer.parseInt(value));
 				} catch (NumberFormatException e) {
 					throw new MementoFormatException("Incorrect value format: expected integer, found "+value);
 				}
 				break;
 			case "long":
 				try {
 					memento.putLong(key, Long.parseLong(value));
 				} catch (NumberFormatException e) {
 					throw new MementoFormatException("Incorrect value format: expected long, found "+value);
 				}
 				break;
 			case "float":
 				try {
 					memento.putFloat(key, Float.parseFloat(value));
 				} catch (NumberFormatException e) {
 					throw new MementoFormatException("Incorrect value format: expected float, found "+value);
 				}
 				break;
 			case "double":
 				try {
 					memento.putDouble(key, Double.parseDouble(value));
 				} catch (NumberFormatException e) {
 					throw new MementoFormatException("Incorrect value format: expected double, found "+value);
 				}
 				break;
 			case "boolean":
 				switch (value) {
 					case "true":
 						memento.putBoolean(key, true);
 						break;
 					case "false":
 						memento.putBoolean(key,false);
 						break;
 					default:
 						throw new MementoFormatException("Incorrect value format: expected boolean, found "+value);
 				}
 				break;
 			case "character":
 				if (value.length()!=1)
 					throw new MementoFormatException("Incorrect value format: expected character, found "+value);
 				memento.putChar(key,value.charAt(0));
 				break;
 			case "byte[]":
 				try {
 					memento.putByteArray(key, decodeByteArray(value));
 				} catch (IOException e) {
 					throw new MementoFormatException("Invalid base64 binary data", e);
 				}
 				break;
 			case "string[]":
 				{
 					NodeList items=valueElement.getChildNodes();
 					List<String> strs=new ArrayList<>();
 					for (int i=0; i<items.getLength(); i++) {
 						if (items.item(i).getNodeType()==Node.TEXT_NODE && ((Text)items.item(i)).getData().matches("\\s*"))
 							continue;
 						if (items.item(i).getNodeType()!=Node.ELEMENT_NODE)
 							throw new MementoFormatException("Item element expected");
 						Element itemElement=(Element)items.item(i);
 						if (!"item".equals(itemElement.getNodeName()))
 							throw new MementoFormatException("Item element expected");
 						strs.add(getStringValue(itemElement));
 					}
 					memento.putStringArray(key,strs.toArray(new String[strs.size()]));
 					break;
 				}
 			case "integer[]":
 				{
 					NodeList items=valueElement.getChildNodes();
 					List<Integer> ints=new ArrayList<>();
 					for (int i=0; i<items.getLength(); i++) {
 						if (items.item(i).getNodeType()==Node.TEXT_NODE && ((Text)items.item(i)).getData().matches("\\s*"))
 							continue;
 						if (items.item(i).getNodeType()!=Node.ELEMENT_NODE)
 							throw new MementoFormatException("Item element expected");
 						Element itemElement=(Element)items.item(i);
 						if (!"item".equals(itemElement.getNodeName()))
 							throw new MementoFormatException("Item element expected");
 						try {
 							ints.add(Integer.valueOf(getStringValue(itemElement)));
 						} catch (NumberFormatException e) {
 							throw new MementoFormatException("Incorrect integer");
 						}
 					}
 					int[] sa=new int[ints.size()];
 					for (int i=0; i<sa.length; i++)
 						sa[i]=ints.get(i);
 					memento.putIntArray(key,sa);
 					break;
 				}
 			case "double[]":
 				{
 					NodeList items=valueElement.getChildNodes();
 					List<Double> ds=new ArrayList<>();
 					for (int i=0; i<items.getLength(); i++) {
 						if (items.item(i).getNodeType()==Node.TEXT_NODE && ((Text)items.item(i)).getData().matches("\\s*"))
 							continue;
 						if (items.item(i).getNodeType()!=Node.ELEMENT_NODE)
 							throw new MementoFormatException("Item element expected");
 						Element itemElement=(Element)items.item(i);
 						if (!"item".equals(itemElement.getNodeName()))
 							throw new MementoFormatException("Item element expected");
 						try {
 							ds.add(Double.valueOf(getStringValue(itemElement)));
 						} catch (NumberFormatException e) {
 							throw new MementoFormatException("Incorrect integer");
 						}
 					}
 					double[] da=new double[ds.size()];
 					for (int i=0; i<da.length; i++)
 						da[i]=ds.get(i);
 					memento.putDoubleArray(key,da);
 					break;
 				}
 			default:
 				throw new MementoFormatException("Incorrect type: "+type);
 		}
 	}
 
 	private static byte[] decodeByteArray(String value) throws IOException {
 		return Base64.decode(value);
 	}
 	private static String encodeByteArray(byte[] source) {
 		return Base64.encodeBytes(source);
 	}
 }
