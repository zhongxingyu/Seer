 /**
  * $Id: OZIM-Java-CodeTemplates.xml,v 1.2 2010/06/03 14:15:22 ozim Exp $
  *
  * Copyright (c) 2011 IBM Czech Republic, spol. s r.o.
  */
 package net.kelt.android.translationdiff;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.io.PrintStream;
 import java.io.UnsupportedEncodingException;
 import java.util.List;
 import java.util.Vector;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import net.kelt.android.translationdiff.items.ArrayItem;
 import net.kelt.android.translationdiff.items.IntegerArrayItem;
 import net.kelt.android.translationdiff.items.ItemType;
 import net.kelt.android.translationdiff.items.PluralsItem;
 import net.kelt.android.translationdiff.items.ResourceItem;
 import net.kelt.android.translationdiff.items.StringArrayItem;
 import net.kelt.android.translationdiff.items.StringItem;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 /**
  * @author Ondra
  * @version $Revision: 1.2 $ 7.10.2011
  */
 public class TranslationDiff {
 	private static PrintStream out = null;
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) throws UnsupportedEncodingException {
 		// Set out
 		out = new PrintStream(System.out, true, "UTF-8");
 		// Parse input
 		if (args.length == 0 || args.length > 2) {
 			printHelp();
 		} else {
 			try {
 				List<ResourceItem> srcEl = null;
 				List<ResourceItem> dstEl = null;
 				// If arg is only one, check it for directory
 				if (args.length == 1) {
 					// Assuming first arg as directory
 					String dir = args[0];
 					File fdir = new File(dir);
 					if (!fdir.isDirectory()) {
 						// One and only argument is file - assuming the translation file
 						String tran_fname = args[0];
 						File temp_tran = new File(tran_fname);
 						if (!temp_tran.exists()) {
 							outln("Translation file \"" + tran_fname + "\" did not exists!");
 							System.exit(0);
 						}
 						StringBuffer tdir = new StringBuffer();
						tdir.append(temp_tran.getParent().substring(0, temp_tran.getParent().lastIndexOf('/')));
						tdir.append("/values");
 						String orig_fname = getOrigFileFromTranslation(temp_tran);
 						if (!new File(orig_fname).exists()) {
 							outln("Original file \"" + orig_fname + "\" did not exists!");
 							System.exit(0);
 						}
 						srcEl = loadOrigFile(orig_fname);
 						dstEl = loadTranFile(tran_fname);
 					} else {
 						// One and only argument is directory - assusming comparing whole directory
 						// Create FileNameFilter - get the content filtered by "xml" pattern
 						FilenameFilter fnf = new FilenameFilter() {
 							@Override
 							public boolean accept(File dir, String name) {
 								return name.endsWith(".xml");
 							}
 						};
 						//
 						outln("### Performing translation comparation of whole directory. In many changes the output can be very long. #######");
 						//
 						String[] flist = fdir.list(fnf);
 						for (String tran_fname : flist) {
 							// For every xml filename in this directory preform a parsing and checking
 							File tranFile = new File(fdir.getPath() + "/" + tran_fname);
 							String orig_fname = getOrigFileFromTranslation(tranFile);
 							// outln("Original file: " + orig_fname);
 							// outln("Translation file: " + tranFile.getPath());
 							srcEl = loadOrigFile(orig_fname);
 							dstEl = loadTranFile(tranFile.getPath());
 							compareLoadedFiles(srcEl, dstEl);
 							outln("###############################################################################################################");
 						}
 						System.exit(0);
 					}
 				} else {
 					// Get args as input files
 					srcEl = loadOrigFile(args[0]);
 					dstEl = loadTranFile(args[1]);
 				}
 				compareLoadedFiles(srcEl, dstEl);
 			} catch (Exception e) {
 				outln("Error: " + e);
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private static void printHelp() {
 		outln("Translation Diff:");
 		outln("Usage: TranslationDiff [translation directory] | [translation file] | [original file] [translation file]");
 	}
 
 	private static void outln(String output) {
 		out.println(output);
 	}
 
 	private static List<ResourceItem> loadOrigFile(String F_SRC) throws Exception {
 		File file = new File(F_SRC);
 		if (file.exists() == false) {
 			outln("Original file \"" + F_SRC + "\" not exists.");
 			System.exit(-1);
 		} else if (!file.isFile()) {
 			outln("Original file \"" + F_SRC + "\" is not a file.");
 			System.exit(-1);
 		} else {
 			outln("Original file: " + F_SRC);
 		}
 		List<ResourceItem> srcEl = new Vector<ResourceItem>();
 		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 		DocumentBuilder db = dbf.newDocumentBuilder();
 		Document doc = db.parse(file);
 		doc.getDocumentElement().normalize();
 		// Get all source elements
 		Node root = doc.getDocumentElement();
 		NodeList nList = root.getChildNodes();
 		for (int i = 0; i < nList.getLength(); i++) {
 			// Iterate through all nodes
 			Node n = nList.item(i);
 			if (n.getNodeName().equalsIgnoreCase("string")) {
 				if (n.getNodeType() == Node.ELEMENT_NODE) {
 					Element e = (Element) n;
 					if (e.getAttribute("translatable") != null && !e.getAttribute("translatable").equalsIgnoreCase("false")) {
 						String elName = e.getAttribute("name");
 						String elValue = e.getTextContent();
 						srcEl.add(new StringItem(elName, elValue));
 						// outln("ORIGINAL: found translatable string: " + elName + "=" + elValue);
 					}
 				}
 			} else if (n.getNodeName().equalsIgnoreCase("plurals")) {
 				if (n.getNodeType() == Node.ELEMENT_NODE) {
 					Element e = (Element) n;
 					if (e.getAttribute("translatable") != null && !e.getAttribute("translatable").equalsIgnoreCase("false")) {
 						String elName = e.getAttribute("name");
 						NodeList plList = e.getElementsByTagName("item");
 						if (plList != null && plList.getLength() > 0) {
 							srcEl.add(new PluralsItem(elName, plList.getLength()));
 							// outln("ORIGINAL: found plurals: " + elName + ", #=" + plList.getLength());
 						} else {
 							outln("ORIGINAL: empty plurals: " + elName);
 						}
 					}
 				}
 			} else if (n.getNodeName().equalsIgnoreCase("string-array")) {
 				if (n.getNodeType() == Node.ELEMENT_NODE) {
 					Element e = (Element) n;
 					if (e.getAttribute("translatable") != null && !e.getAttribute("translatable").equalsIgnoreCase("false")) {
 						String elName = e.getAttribute("name");
 						NodeList plList = e.getElementsByTagName("item");
 						if (plList != null && plList.getLength() > 0) {
 							srcEl.add(new StringArrayItem(elName, plList.getLength()));
 							// outln("ORIGINAL: found string-array: " + elName + ", #=" + plList.getLength());
 						} else {
 							outln("ORIGINAL: empty string-array: " + elName);
 						}
 					}
 				}
 			} else if (n.getNodeName().equalsIgnoreCase("array")) {
 				if (n.getNodeType() == Node.ELEMENT_NODE) {
 					Element e = (Element) n;
 					if (e.getAttribute("translatable") != null && !e.getAttribute("translatable").equalsIgnoreCase("false")) {
 						String elName = e.getAttribute("name");
 						NodeList plList = e.getElementsByTagName("item");
 						if (plList != null && plList.getLength() > 0) {
 							srcEl.add(new ArrayItem(elName, plList.getLength()));
 							// outln("ORIGINAL: found array: " + elName + ", #=" + plList.getLength());
 						} else {
 							outln("ORIGINAL: empty array: " + elName);
 						}
 					}
 				}
 			} else if (n.getNodeName().equalsIgnoreCase("integer-array")) {
 				if (n.getNodeType() == Node.ELEMENT_NODE) {
 					Element e = (Element) n;
 					if (e.getAttribute("translatable") != null && !e.getAttribute("translatable").equalsIgnoreCase("false")) {
 						String elName = e.getAttribute("name");
 						NodeList plList = e.getElementsByTagName("item");
 						if (plList != null && plList.getLength() > 0) {
 							srcEl.add(new ArrayItem(elName, plList.getLength()));
 							// outln("ORIGINAL: found integer-array: " + elName + ", #=" + plList.getLength());
 						} else {
 							outln("ORIGINAL: empty integer-array: " + elName);
 						}
 					}
 				}
 			} else if (n.getNodeName().equalsIgnoreCase("#comment")) {
 				// skip
 			} else if (n.getNodeName().equalsIgnoreCase("#text")) {
 				// skip
 			} else if (n.getNodeName().equalsIgnoreCase("skip")) {
 				// skip
 			} else {
 				outln("ORIGINAL: unknown node: " + n.getNodeName() + "::" + n.getNodeValue());
 			}
 		}
 		return srcEl;
 	}
 
 	private static List<ResourceItem> loadTranFile(String F_DST) throws Exception {
 		File file = new File(F_DST);
 		if (file.exists() == false) {
 			outln("Translation file \"" + F_DST + "\" not exists.");
 			System.exit(-1);
 		} else if (!file.isFile()) {
 			outln("Translation file \"" + F_DST + "\" is not a file.");
 			System.exit(-1);
 		} else {
 			outln("Translation file: " + F_DST);
 		}
 		List<ResourceItem> dstEl = new Vector<ResourceItem>();
 		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 		DocumentBuilder db = dbf.newDocumentBuilder();
 		Document doc = db.parse(file);
 		doc = db.parse(file);
 		doc.getDocumentElement().normalize();
 		Node root = doc.getDocumentElement();
 		NodeList nList = root.getChildNodes();
 		for (int i = 0; i < nList.getLength(); i++) {
 			// Iterate through all nodes
 			Node n = nList.item(i);
 			if (n.getNodeName().equalsIgnoreCase("string")) {
 				if (n.getNodeType() == Node.ELEMENT_NODE) {
 					Element e = (Element) n;
 					if (e.getAttribute("translatable") != null && !e.getAttribute("translatable").equalsIgnoreCase("false")) {
 						String elName = e.getAttribute("name");
 						String elValue = e.getTextContent();
 						dstEl.add(new StringItem(elName, elValue));
 						// outln("TRANSLATION: found translatable string: " + elName + "=" + elValue);
 					}
 				}
 			} else if (n.getNodeName().equalsIgnoreCase("plurals")) {
 				if (n.getNodeType() == Node.ELEMENT_NODE) {
 					Element e = (Element) n;
 					if (e.getAttribute("translatable") != null && !e.getAttribute("translatable").equalsIgnoreCase("false")) {
 						String elName = e.getAttribute("name");
 						NodeList plList = e.getElementsByTagName("item");
 						if (plList != null && plList.getLength() > 0) {
 							dstEl.add(new PluralsItem(elName, plList.getLength()));
 							// outln("TRANSLATION: found plurals: " + elName + ", #=" + plList.getLength());
 						} else {
 							outln("TRANSLATION: empty plurals: " + elName);
 						}
 					}
 				}
 			} else if (n.getNodeName().equalsIgnoreCase("string-array")) {
 				if (n.getNodeType() == Node.ELEMENT_NODE) {
 					Element e = (Element) n;
 					if (e.getAttribute("translatable") != null && !e.getAttribute("translatable").equalsIgnoreCase("false")) {
 						String elName = e.getAttribute("name");
 						NodeList plList = e.getElementsByTagName("item");
 						if (plList != null && plList.getLength() > 0) {
 							dstEl.add(new StringArrayItem(elName, plList.getLength()));
 							// outln("TRANSLATION: found string-array: " + elName + ", #=" + plList.getLength());
 						} else {
 							outln("TRANSLATION: empty string-array: " + elName);
 						}
 					}
 				}
 			} else if (n.getNodeName().equalsIgnoreCase("array")) {
 				if (n.getNodeType() == Node.ELEMENT_NODE) {
 					Element e = (Element) n;
 					if (e.getAttribute("translatable") != null && !e.getAttribute("translatable").equalsIgnoreCase("false")) {
 						String elName = e.getAttribute("name");
 						NodeList plList = e.getElementsByTagName("item");
 						if (plList != null && plList.getLength() > 0) {
 							dstEl.add(new ArrayItem(elName, plList.getLength()));
 							// outln("TRANSLATION: found array: " + elName + ", #=" + plList.getLength());
 						} else {
 							outln("TRANSLATION: empty array: " + elName);
 						}
 					}
 				}
 			} else if (n.getNodeName().equalsIgnoreCase("integer-array")) {
 				if (n.getNodeType() == Node.ELEMENT_NODE) {
 					Element e = (Element) n;
 					if (e.getAttribute("translatable") != null && !e.getAttribute("translatable").equalsIgnoreCase("false")) {
 						String elName = e.getAttribute("name");
 						NodeList plList = e.getElementsByTagName("item");
 						if (plList != null && plList.getLength() > 0) {
 							dstEl.add(new IntegerArrayItem(elName, plList.getLength()));
 							// outln("TRANSLATION: found integer-array: " + elName + ", #=" + plList.getLength());
 						} else {
 							outln("TRANSLATION: empty integer-array: " + elName);
 						}
 					}
 				}
 			} else if (n.getNodeName().equalsIgnoreCase("#comment")) {
 				// skip
 			} else if (n.getNodeName().equalsIgnoreCase("#text")) {
 				// skip
 			} else if (n.getNodeName().equalsIgnoreCase("skip")) {
 				// skip
 			} else {
 				outln("TRANSLATION: unknown node: " + n.getNodeName() + "::" + n.getNodeValue());
 			}
 		}
 		return dstEl;
 	}
 
 	private static void compareLoadedFiles(List<ResourceItem> srcEl, List<ResourceItem> dstEl) throws Exception {
 		outln("... Comparing ...");
 		// Find all in source but not in destination
 		outln("### changes from ORIGINAL >>> to >>> TRANSLATION ###");
 		for (ResourceItem si : srcEl) {
 			String sname = si.getName();
 			int diItems = 0;
 			boolean found = false;
 			for (ResourceItem di : dstEl) {
 				if (si.getItemType() == ItemType.ITEM_STRING) {
 					if (sname.equalsIgnoreCase(di.getName())) {
 						found = true;
 						break;
 					}
 				} else if (si.getItemType() == ItemType.ITEM_PLURALS || si.getItemType() == ItemType.ITEM_STRING_ARRAY
 						|| si.getItemType() == ItemType.ITEM_ARRAY || si.getItemType() == ItemType.ITEM_INTEGER_ARRAY) {
 					if (sname.equalsIgnoreCase(di.getName())) {
 						diItems = di.getChildItems();
 						if (sname.equalsIgnoreCase(di.getName()) && si.getChildItems() == di.getChildItems()) {
 							found = true;
 							break;
 						}
 					}
 				}
 			}
 			if (!found) {
 				if (si.getItemType() == ItemType.ITEM_STRING) {
 					outln("STRING name=\"" + sname + "\"==\"" + si.getValue() + "\" not in TRANSLATION file");
 				} else {
 					if (si.getItemType() == ItemType.ITEM_PLURALS) {
 						outln("PLURALS name=\"" + sname + "\":\"" + si.getChildItems() + "!=" + diItems
 								+ "\" not in TRANSLATION file or different item count");
 					} else {
 						outln("ARRAY[" + si.getItemType() + "] name=\"" + sname + "\":\"" + si.getChildItems() + "!=" + diItems
 								+ "\" not in TRANSLATION file or different item count");
 					}
 				}
 			}
 		}
 		// Find all in dst but not in src
 		outln("### changes to ORIGINAL <<< from <<< TRANSLATION ###");
 		for (ResourceItem di : dstEl) {
 			String dname = di.getName();
 			boolean found = false;
 			for (ResourceItem si : srcEl) {
 				if (di.getItemType() == ItemType.ITEM_STRING) {
 					if (dname.equalsIgnoreCase(si.getName())) {
 						found = true;
 						break;
 					}
 				} else if (di.getItemType() == ItemType.ITEM_PLURALS || di.getItemType() == ItemType.ITEM_STRING_ARRAY
 						|| di.getItemType() == ItemType.ITEM_ARRAY || di.getItemType() == ItemType.ITEM_INTEGER_ARRAY) {
 					if (dname.equalsIgnoreCase(si.getName()) && di.getChildItems() == si.getChildItems()) {
 						found = true;
 						break;
 					}
 				}
 			}
 			if (!found) {
 				if (di.getItemType() == ItemType.ITEM_STRING) {
 					outln("STRING name=\"" + dname + "\"==\"" + di.getValue() + "\" not in ORIGINAL file");
 				} else {
 					if (di.getItemType() == ItemType.ITEM_PLURALS) {
 						outln("PLURALS name=\"" + dname + "\":\"" + di.getChildItems()
 								+ "\" not in ORIGINAL file or different item count");
 					} else {
 						outln("ARRAY[" + di.getItemType() + "] name=\"" + dname + "\":\"" + di.getChildItems()
 								+ "\" not in ORIGINAL file or different item count");
 					}
 				}
 			}
 		}
 	}
 
 	private static String getOrigFileFromTranslation(File tranFileName) {
 		StringBuffer tdir = new StringBuffer();
		tdir.append(tranFileName.getParent().substring(0, tranFileName.getParent().lastIndexOf('/')));
		tdir.append("/values");
 		return tdir + "/" + tranFileName.getName();
 	}
 }
