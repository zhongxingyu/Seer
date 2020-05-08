 package com.winvector;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.HashSet;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Properties;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.TreeSet;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.util.zip.ZipOutputStream;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.xml.sax.Attributes;
 import org.xml.sax.Locator;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 
 import com.winvector.ExampleClipper.ClipConsumer;
 import com.winvector.ExampleClipper.ClipZipper;
 
 /**
  * For each file name in book.xml create file with all of the cross-references defined from other files (including those included through parts), append with _external_links.xml
  * the strategy is to get all possible external refs by add a line of XML like: 	<xi:include href="X_external_links.xml"/>
  * 
  * Also check for a number of errors:
  *   1) Illegal id tags (not stating with alpha or containing whitespace)
  *   2) linkend references to non-existent tags
  *   3) Duplicate tags
  *   4) Case confusion between tags
  *   5) Use of <co id=X> and <callout arrearefs=X> in non-example context ( <example> or <informalexample> )
  *   6) Non-parallel structure between call-outs <co id=X> and <callout arrearefs=X>
  *   7) items that must have ids (and these ids must be referred to): <example> and <figure>
  *   9) Dangling filerefs.
  *   9) Unused file assets (warn)
  *  10) resource directories used by more than one XML file (warn)
  *  11) referring to un-numbered structures (informalexample, formalpara, sect3)
  *  
  *  And generate a directory tree of code extracts.
  *  
  *  Note: all of the extract process and results assume correctly formatted XML for proper operations.
  *  
  *  Requires a Java XML properties file named CodeConfig.xml in working directory with the following fields set:
  *  
  *  <!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd">
  *  <properties>
  *      <comment>Config example extrator from https://github.com/WinVector/JXREF</comment>
  *      <entry key="TakeCallouts">True</entry>
  *      <entry key="OpenComment">#</entry>
  *      <entry key="CloseComment"></entry>
  *      <entry key="ReadMe">README text</entry>
  *  </properties>
  * 
  *   
  * @author johnmount
  *
  */
 public final class ScanIDs {
 	public static final String IDREGEXP = "^[a-zA-Z][a-zA-Z0-9_:\\-/]*$";
 	public static final Pattern IDPATTERN = Pattern.compile(IDREGEXP);
 
 
 	public static final Set<String> callOutContexts = new HashSet<String>(Arrays.asList(new String[] {
 			"informalexample",
 			"example"
 	}));
 	
 	public static final Set<String> mustHaveId = new HashSet<String>(Arrays.asList(new String[] {
 			"co", "figure"	
 		}));
 	
 
 	public static final Set<String> cantReferToId = new HashSet<String>(Arrays.asList(new String[] {
 			"sect3", "informalexample", "formalpara", "simplesect", "title", "para", "programlisting", "sect4",
 			"mediaobject", "imageobject", "imagedata"
 		}));
 
 	
 	private static final class FileRec {
 		public final String id;
 		public final File f;
 		
 		public FileRec(final String id, final File f) {
 			this.id = id;
 			this.f = f;
 		}
 		
 		@Override
 		public String toString() {
 			return "(" + id + "," + f + ")";
 		}
 	}
 
 	private static final Comparator<String> compareIgnoreCase = new Comparator<String>() {
 		@Override
 		public int compare(final String o1, final String o2) {
 			return o1.compareToIgnoreCase(o2);
 		}
 	};
 	
 
 	public final String ourSuffix = "_external_links.xml";
 	public final File workingDir;
 	public final File destDir;
 	public final Properties props;
 
 	public ScanIDs(final File workingDir, final Properties props) {
 		this.workingDir = workingDir;
 		this.props = props;
 		destDir = new File(workingDir,"generated");
 		destDir.mkdirs();
 	}
 	
 	
 	private final class OutlineHandler extends DefaultHandler {
 		private final Set<String> targets = new TreeSet<String>(Arrays.asList(new String[] {"chapter", "part", "appendix", "sect1"}));
 		private final ExampleClipper exampleClipper;
 		private LinkedList<String> tagStack = new LinkedList<String>();
 		private final ErrorCollector ec;
 		private StringBuilder titleBuf = null;
 		
 		
 		public OutlineHandler(final ClipConsumer clipConsumer, final ErrorCollector ec) {
 			this.ec = ec;
 			exampleClipper = new ExampleClipper(ec,clipConsumer);
 			exampleClipper.takeCallouts = Boolean.parseBoolean(props.getProperty("TakeCallouts"));
 			exampleClipper.openComment = props.getProperty("OpenComment");
 			exampleClipper.closeComment = props.getProperty("CloseComment");
 		}
 
 		@Override
 		public void startElement(final String uri, 
 				final String localName, final String qName, 
 				final Attributes attributes) throws SAXException {
 			if(qName.equals("title")) {
 				titleBuf = new StringBuilder();
 			}
 			tagStack.addLast(qName);
 			exampleClipper.startElement(uri, localName, qName, attributes);
 		}
 		
 		@Override
 		public void characters(final char[] ch,
                 final int start,
                 final int length)
                 throws SAXException {
 			exampleClipper.characters(ch, start, length);
 			if(null!=titleBuf) {
 				for(int i=start;i<start+length;++i) {
 					titleBuf.append(ch[i]);
 				}
 			}
 		}
 		
 		@Override
 		public void endElement(final String uri,
                 final String localName,
                 final String qName)
                 throws SAXException {
 			final String here = tagStack.removeLast();
 			if(null!=titleBuf) {
 				if(titleBuf.toString().trim().length()<=0) {
 					ec.mkError("Empty title", "Empty title: " + exampleClipper.itemLabeler.curPositionCode(here));
 				}
 				if((!tagStack.isEmpty())) {
 					final String prevElt = tagStack.getLast();
 					if(targets.contains(prevElt)) {
 						for(int i=0;i<tagStack.size();++i) {
 							System.out.print("\t");
 						}
 						System.out.println(exampleClipper.itemLabeler.curPositionCode(tagStack.getLast()) + "\t" + titleBuf.toString());
 					}
 				}
 				titleBuf = null;
 			}
 			exampleClipper.endElement(uri, localName, qName);
 		}
 	}
 	
 	private final class CheckHandler extends DefaultHandler {
 		public String fi = null;
 		private final ErrorCollector ec;
 		public Locator locator = null;
 		public ItemLabeler itemLabeler = null;
 		
 		public CheckHandler(final ErrorCollector ec) {
 			this.ec = ec;
 		}
 		
 		public final class TagRec implements Comparable<TagRec> {
 			public final String fileName;
 			public final String tagType;
 			public final String fieldType;
 			public final String id;
 			public final String label;
 			public final int lineNum;
 			public final int colNum;
 			
 			public TagRec(final String fileName, final String tagType, final String fieldType, final String id) {
 				this.fileName = fileName;
 				this.tagType = tagType;
 				this.fieldType = fieldType;
 				this.id = id;
 				label = itemLabeler.curPositionCode(tagType);
 				lineNum = locator.getLineNumber();
 				colNum = locator.getColumnNumber();
 			}
 			
 			@Override
 			public String toString() {
 				return  fileName + " (line: " + lineNum + ", col: " + colNum + "): <" + tagType + " " + fieldType +"=\"" + id + "\" />";
 			}
 
 			@Override
 			public int compareTo(final TagRec o) {
 				{
 					final int cmp = fileName.compareTo(o.fileName);
 					if(cmp!=0) {
 						return cmp;
 					}
 				}
 				if(lineNum!=o.lineNum) {
 					if(lineNum>=o.lineNum) {
 						return 1;
 					} else {
 						return -1;
 					}
 				}
 				if(colNum!=o.colNum) {
 					if(colNum>=o.colNum) {
 						return 1;
 					} else {
 						return -1;
 					}
 				}
 				{
 					final int cmp = tagType.compareTo(o.tagType);
 					if(cmp!=0) {
 						return cmp;
 					}
 				}
 				{
 					final int cmp = fieldType.compareTo(o.fieldType);
 					if(cmp!=0) {
 						return cmp;
 					}
 				}
 				{
 					final int cmp = id.compareTo(o.id);
 					if(cmp!=0) {
 						return cmp;
 					}
 				}
 				return 0;
 			}
 			
 			@Override
 			public boolean equals(final Object o) {
 				return compareTo((TagRec)o)==0;
 			}
 			
 			@Override
 			public int hashCode() {
 				return fileName.hashCode() + 3*lineNum + 7*colNum + 13*tagType.hashCode() + 29*fieldType.hashCode() + 37*id.hashCode();
 			}
 		}
 		
 		public final Map<String,TagRec> idToRec = new TreeMap<String,TagRec>(compareIgnoreCase); // map ids to record of XML element
 		public final Map<String,TagRec> cantGloballyRef = new TreeMap<String,TagRec>(compareIgnoreCase); // ids that must be used (co callouts at this point)
 		public final Map<String,TagRec> mustReferTo = new TreeMap<String,TagRec>(compareIgnoreCase); // ids must be used somewhere
 		public final Map<String,TagRec> idRefToFirstIdRef = new TreeMap<String,TagRec>(compareIgnoreCase); // uses of ids to reference  (maps to exact casing of first use)
 		public final Map<String,FileRec> fileRefToExamplePerXML = new TreeMap<String,FileRec>(compareIgnoreCase); // file refs in XML
 		public final Map<String,FileRec> fileResfToExample = new TreeMap<String,FileRec>(compareIgnoreCase); // file refs overall
 		// callout declaration to use matching
 		private Set<String> knownCallOuts = null;
 		private ArrayList<TagRec> callOutsMarks = null;
 		private ArrayList<TagRec> callOutsTexts = null;
 		private boolean sawAnnotSet = false;
 		private final Set<String> perXMLResourceDirs = new TreeSet<String>();
 
 		public void startXMLFile(final String fi) {
 			this.fi = fi;
 			knownCallOuts = null;
 			perXMLResourceDirs.clear();
 			itemLabeler = new ItemLabeler();
 		}
 		
 		@Override
 		public void setDocumentLocator(final Locator locator) {
 		    this.locator = locator;
 		}
 
 		private boolean goodID(final String s) {
 			if(null==s) {
 				return false;
 			}
 			final Matcher matcher = IDPATTERN.matcher(s);
 			if(!matcher.find()) {
 				return false;
 			}
 			return true;
 		}
 		
 		private boolean interestingString(final String s) {
 			if(null==s) {
 				return false;
 			}
 			return s.trim().length()>0;
 		}
 		
 		@Override
 		public void startElement(final String uri, 
 				final String localName, final String qName, 
 				final Attributes attributes) throws SAXException {
 			itemLabeler.startElement(uri, localName, qName, attributes);
 			if(callOutContexts.contains(qName)) {
 				knownCallOuts = new TreeSet<String>(compareIgnoreCase);
 				callOutsMarks = new ArrayList<TagRec>();
 				callOutsTexts = new ArrayList<TagRec>();
 				sawAnnotSet = interestingString(attributes.getValue("annotations"));
 			}
 			{
 				final String IDFIELD = "id";
 				final String id = attributes.getValue(IDFIELD);
 				if(null!=id) {
 					final TagRec idRec = new TagRec(fi,qName,IDFIELD,id);
 					if(!goodID(id)) {
 						ec.mkError(" bad id (must match regexp: " +  IDREGEXP + ")", "Error: " + fi + " tag " + idRec + " bad id (must match regexp: " +  IDREGEXP + ")");
 					} else {
 						if(cantReferToId.contains(qName)) {
 							ec.mkError("Warning unrefferable ","Warning unrefferable " + fi + " tag " + idRec + " has an ID");
 						}
 						if(idToRec.containsKey(id)) {
 							ec.mkError(" duplicates tag ","Error: " + fi + " tag " + idRec + " duplicates tag " + idToRec.get(id));
 						} else {
 							idToRec.put(id,idRec);
 							if("co".equalsIgnoreCase(qName)) {
 								if(null==callOutsMarks) {
 									ec.mkError(" when not in a callout environment (example/informalexample)","Error: " + fi + " co " + idRec + " when not in a callout environment (example/informalexample)");
 								} else {
 									if(knownCallOuts.contains(id)) {
 										// not reached as error is currently handled elsewhere in the flow
 										ec.mkError(" duplicate callout tag","Error: " + fi + " co " + idRec + " duplicate callout tag");																		
 									} else {
 										callOutsMarks.add(idRec);
 										knownCallOuts.add(id);
 									}
 								}
 								cantGloballyRef.put(id,idRec);
 							} else {
 								if(mustHaveId.contains(qName)) {
 									mustReferTo.put(id, idRec);
 								}
 							}
 						}
 					}
 				} else {
 					if(mustHaveId.contains(qName)) {
 						final TagRec idRec = new TagRec(fi,qName,IDFIELD,id);
 						ec.mkError(" must have an ID","Error: " + fi + " " + idRec + " must have an ID");
 					}
 				}
 			}
 			final String AREAREFKEY = "arearefs";
 			for(final String field: new String[] {"linkend",AREAREFKEY }){
 				final String linkEnd = attributes.getValue(field);
 				if(null!=linkEnd) {
 					final TagRec ourExample = new TagRec(fi,qName,field,linkEnd);
 					if(!goodID(linkEnd)) {
 						ec.mkError(" bad id (must start with a letter and have no whitespace)","Error: " + ourExample + " bad id (must start with a letter and have no whitespace)");
 					} else {
 						final TagRec prevExample = idRefToFirstIdRef.get(linkEnd);
 						if(null!=prevExample) {
 							if(prevExample.id.compareTo(linkEnd)!=0) {
 								ec.mkError(" confusing casing with ","Error: " + ourExample + " linkend " + linkEnd + " confusing casing with " + prevExample);					
 							}
 						} else {
 							if(field.equals(AREAREFKEY)) {
 								if(null==callOutsMarks) {
 									ec.mkError(" when not in a callout environment (example/informalexample)","Error: " + fi + " arearef " + ourExample + " when not in a callout environment (example/informalexample)");								
 								} else {
 									if(!knownCallOuts.contains(linkEnd)) {
 										ec.mkError(" unknown callout tag","Error: " + fi + " co " + ourExample + " unknown callout tag");
 									} else {
 										callOutsTexts.add(ourExample);
 									}
 								}
 							} else {
 								idRefToFirstIdRef.put(linkEnd,ourExample);
 							}
 						}
 					}
 				}
 			}
 			{
 				final String FILEREFFIELD = "fileref";
 				final String fileRef = attributes.getValue(FILEREFFIELD);
 				if(null!=fileRef) {
 					final TagRec here = new TagRec(fi,qName,FILEREFFIELD,fileRef);
 					{ // global issues
 						final FileRec prevGlobalExample = fileResfToExample.get(fileRef);
 						if(null!=prevGlobalExample) {
 							if(prevGlobalExample.id.compareTo(fileRef)!=0) {
 								ec.mkError(" confusing casing with ","Error: " + here + " fileref " + fileRef + " confusing casing with " + prevGlobalExample);				
 							}
 						} else {
 							final File ref = new File(workingDir,fileRef);
 							fileResfToExample.put(fileRef,new FileRec(fileRef,ref));
 							if((!ref.exists())||(!ref.canRead())) {
 								ec.mkError(" missing referred file: ","Error: " + here + " missing referred file: " + fileRef);
 							}
 						}
 					} 
 					{ // per XML file issue
 						final FileRec prevExample = fileRefToExamplePerXML.get(fileRef);
 						if(null!=prevExample) {
 						} else {
 							final File ref = new File(workingDir,fileRef);
 							fileRefToExamplePerXML.put(fileRef,new FileRec(fileRef,ref));
 							final File resourceDir = ref.getParentFile();
 							try {
 								perXMLResourceDirs.add(resourceDir.getCanonicalPath().toString());
 							} catch (IOException e) {
 								ec.mkError(" threw on getCanonicalPath(): ","Error: " + here + " threw on getCanonicalPath(): " + fileRef);
 							}
 						}
 					}
 				}
 			}
 		}
 		
 		@Override
 		public void characters(final char[] ch,
 	            final int start,
 	            final int length)
 	            throws SAXException {
 			itemLabeler.characters(ch, start, length);
 		}
 		
 		@Override
 		public void endElement(final String uri, final String localName, final String qName) throws SAXException {
 			itemLabeler.endElement(uri, localName, qName);
 			if(callOutContexts.contains(qName)) {
 				// confirm parallel structure
 				final int n = callOutsMarks.size();
 				boolean disordered = n!=callOutsTexts.size();
 				for(int i=0;(i<n)&&(!disordered);++i) {
 					if(callOutsMarks.get(i).id.compareTo(callOutsTexts.get(i).id)!=0) {
 						disordered = true;
 					}
 				}
 				if(disordered) {
 					final TagRec here = new TagRec(fi,qName,"","");
 					ec.mkError(" callouts not in matching order","Error: " + here + " callouts not in matching order");
 				}
 				if((!disordered)&&(n>0)) {
 					if(!sawAnnotSet) {
 						final TagRec here = new TagRec(fi,qName,"","");
 						ec.mkError(" didn't set annotations in example or informalexample","Warn: " + here + " didn't set annotations in example or informalexample");					
 					}
 				}
 				// prepare for next pass
 				knownCallOuts = null;
 				callOutsMarks = null;
 				callOutsTexts = null;
 			}
 		}
 	}
 	
 	private static void scanForContent(final File dir, final Map<String,File> nameToPath) {
 		final File[] files =  dir.listFiles();
 		for(final File fi: files) {
 			final String name = fi.getName();
 			if((name.length()>0)&&(name.charAt(0)!='.')) {
 				if(fi.isDirectory()) {
 					scanForContent(fi,nameToPath);
 				} else {
 					final String nm = fi.getName();
 					final File opath = nameToPath.get(nm);
 					if(null!=opath) {
 						System.out.println("WARN: paths " + opath + " and " + fi + " are confusing");
 					} else {
 						nameToPath.put(nm,fi);
 					}
 				}
 			}
 		}
 	}
 	
 	private ArrayList<String> getXMLIncludes(final SAXParser saxParser, final String xmlFileName) throws SAXException, IOException {
 		final ArrayList<String> fileNameList = new ArrayList<String>();
 		final File xmlFile = new File(workingDir,xmlFileName);
 		if(xmlFile.exists()) {
 			System.out.println("reading:\t" + xmlFile.getAbsolutePath());
 			saxParser.parse(xmlFile, new DefaultHandler() {
 				@Override
 				public void startElement(final String uri, 
 						final String localName, final String qName, 
 						final Attributes attributes) throws SAXException {
 					if("xi:include".compareTo(qName)==0) {
 						final String href = attributes.getValue("href");
 						if((null!=href)&&(href.length()>0)&&
 								(href.endsWith(".xml"))&&(!href.endsWith(ourSuffix))&&(!xmlFileName.equalsIgnoreCase(href))) {
 							fileNameList.add(href);
 						}
 					}
 				}			
 			});
 		}
 		return fileNameList;
 	}
 	
 	private void getXMLIncludesAndCounts(final SAXParser saxParser, final String xmlFileName, final LinkedHashMap<String,Integer> counts) throws SAXException, IOException {
 		if(!counts.containsKey(xmlFileName)) {
 			final ArrayList<String> incs = getXMLIncludes(saxParser,xmlFileName);
 			counts.put(xmlFileName,incs.size());
 			for(final String fi: incs) {
 				if(!counts.containsKey(fi)) {
 					getXMLIncludesAndCounts(saxParser,fi,counts);
 				}
 			}
 		}
 	}
 	
 	public int doWork(final String zipName) throws IOException, ParserConfigurationException, SAXException {
 		System.out.println("working in: " + workingDir.getAbsolutePath());
 		final SAXParserFactory saxFactory = SAXParserFactory.newInstance();
 		final SAXParser saxParser = saxFactory.newSAXParser();
 		final String bookFileName = "book.xml";
 		final LinkedHashMap<String,Integer> xmlIncCounts = new LinkedHashMap<String,Integer>();
 		getXMLIncludesAndCounts(saxParser,bookFileName,xmlIncCounts);
 		final ErrorCollector ec = new ErrorCollector();
 		{ // scan for chapter and sect 1 structure, and zip up examples
 			final String readMeStr = props.getProperty("ReadMe");
 			final String defaultFileSuffix = props.getProperty("FileSuffix");
 			final File of = new File(zipName + ".zip");
 			System.out.println("writing: " + of.getAbsolutePath());
 			final ZipOutputStream o = new ZipOutputStream(new FileOutputStream(of));
 			final ClipConsumer clipConsumer = new ClipZipper(o,zipName,readMeStr,defaultFileSuffix);
 			for(final Entry<String, Integer> me: xmlIncCounts.entrySet()) {
 				final String fi = me.getKey();
 				final int count = me.getValue();
 				if(count<=0) {
 					final File f = new File(workingDir,fi);
 					//System.out.println("\treading: " + fi + "\t" + f);
 					final OutlineHandler dataHandler = new OutlineHandler(clipConsumer,ec);
 					saxParser.parse(f,dataHandler);
 				}
 			}
 			o.close();
 		}
 		final CheckHandler checkHandler = new CheckHandler(ec);
 		final Map<String,String> resourceDirToXML = new TreeMap<String,String>();
 		{ // scan all files for tags
 			for(final String fi: xmlIncCounts.keySet()) {
 				final File f = new File(workingDir,fi);
 				//System.out.println("\treading: " + fi + "\t" + f);
 				checkHandler.startXMLFile(fi);
 				saxParser.parse(f,checkHandler);
 				if(!checkHandler.perXMLResourceDirs.isEmpty()) {
 					for(final String di: checkHandler.perXMLResourceDirs) {
 						final String otherXML = resourceDirToXML.get(di);
 						if(null!=otherXML) {
 							System.out.println("WARN: resource directory " + di + " used by " + fi + " and " + otherXML);
 						}
 						resourceDirToXML.put(di,fi);
 					}					
 				}
 			}
 		}
 		// check for broken/dangling links
 		for(final Entry<String, com.winvector.ScanIDs.CheckHandler.TagRec> me: checkHandler.idRefToFirstIdRef.entrySet()) {
 			final String headId = me.getKey();
 			final com.winvector.ScanIDs.CheckHandler.TagRec linkend = me.getValue();
 			final com.winvector.ScanIDs.CheckHandler.TagRec forbidden = checkHandler.cantGloballyRef.get(linkend.id);
 			if(null!=forbidden) {
 				ec.mkError("Error: illegal global ref from ","Error: illegal global ref from " + linkend + " to " + forbidden);
 			} else {
 				final com.winvector.ScanIDs.CheckHandler.TagRec rec = checkHandler.idToRec.get(linkend.id);
 				if(null==rec) {
 					ec.mkError("file link broken","Error: link " + linkend + " broken");
 				} else {
 					final com.winvector.ScanIDs.CheckHandler.TagRec linkhead = checkHandler.idToRec.get(headId);
 					if(cantReferToId.contains(linkhead.tagType)) {
 						ec.mkError("file ref problem","Error: linkend " + linkend + " references a " + linkhead.tagType);						
 					}
 					if(rec.id.compareTo(linkend.id)!=0) {
 						ec.mkError("file casing","Error: linkend " + linkend + " confusing casing with " + rec.id);
 					}
 				}
 			}
 		}
 		for(final com.winvector.ScanIDs.CheckHandler.TagRec dest: checkHandler.mustReferTo.values()) {
 			final com.winvector.ScanIDs.CheckHandler.TagRec use = checkHandler.idRefToFirstIdRef.get(dest.id);
 			if(null==use) {
				ec.mkError("linkend never referred to","Error: linkend " + dest + " never referred to");
 			}
 		}
 		ec.printReport(System.out);
 		// check content
 		final Map<String,File> nameToPath = new TreeMap<String,File>(compareIgnoreCase);
 		scanForContent(workingDir,nameToPath);
 		final Set<File> filesSeen = new TreeSet<File>();
 		filesSeen.addAll(nameToPath.values());
 		for(final FileRec f: checkHandler.fileResfToExample.values()) {
 			if(!filesSeen.contains(f.f)) {
 				System.out.println("WARN: file " + f.f + " not found");
 			} else {
 				filesSeen.remove(f.f);
 			}
 		}
 		for(final String fi: xmlIncCounts.keySet()) {
 			final File f = new File(workingDir,fi);
 			filesSeen.remove(f);
 		}
 		for(final File f: filesSeen) {
 			if((f.getParentFile().compareTo(workingDir)!=0)&&
 					(!f.getName().endsWith(ourSuffix))&&(!f.getName().endsWith("~"))&&(!f.getName().endsWith(".xsd"))) {
 				System.out.println("WARN: file " + f + " not used");
 			}
 		}
 		// write external links
 		final String[] header = {
 				   "<simplesect xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"",
 				   "	xsi:schemaLocation=\"http://www.manning.com/schemas/book manning-book.xsd\"",
 				   "	xmlns=\"http://www.manning.com/schemas/book\" xmlns:ns=\"http://www.manning.com/schemas/book\"",
 				   "	xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">",
 				   "	<title>.</title>",
 		};
 		final String[] footer = {
 				   "</simplesect>",
 		};
 		System.out.println("\twriting links");
 		for(final String fi: xmlIncCounts.keySet()) {
 			if(fi.equalsIgnoreCase(bookFileName)) {
 				continue;
 			}
 			final File resFile = new File(destDir,fi + ourSuffix);
 			//System.out.println("\twriting: " + resFile);
 			final PrintStream p = new PrintStream(resFile);
 			for(final String line: header) {
 				p.println(line);
 			}
 			for(final com.winvector.ScanIDs.CheckHandler.TagRec idRec: checkHandler.idToRec.values()) {
 				if(idRec.fileName.compareToIgnoreCase(fi)!=0) {
 					p.println("	<para id=\"" + idRec.id + "\" xreflabel=\"XRF:" + idRec.label + ":" + idRec.id + "\" />");
 				}
 			}
 			for(final String line: footer) {
 				p.println(line);
 			}
 			p.close();			
 		}
 		System.out.println("total Errors: " + ec.nErrors);
 		System.out.println("done");
 		return ec.nErrors;
 	}
 	
 	/**
 	 * @param args
 	 */
 	public static void main(final String[] args) throws Exception {
 		final File workingDir = new File(".");
 		final Properties prop = new Properties();
 		prop.loadFromXML(new FileInputStream("CodeConfig.xml"));
 		final ScanIDs scanner = new ScanIDs(workingDir,prop);
 		String zipName = "CodeExamples";
 		final int totErrors = scanner.doWork(zipName);
 		if(totErrors>0) {
 			System.exit(1);
 		}
 	}
 
 }
