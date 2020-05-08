 package com.winvector;
 
 import java.io.IOException;
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.Set;
 import java.util.regex.Pattern;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipOutputStream;
 
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 
 
 /**
  * State machine to extract program listings and callouts.
  * Users must call startElement(), endElement() and characters()
  * @author johnmount
  *
  */
 public final class ExampleClipper extends DefaultHandler {
 
 	
 	public final class Clip {
 		public final Date lastModifiedTime;
 		public final String chapterSymbol;
 		public String chapter;
 		public String positionCode;
 		public String positionDescription;
 		public String foundSuffix;
 		public String clipTitle;
 		public String progText;
 		public ArrayList<String> calloutText;
 		
 		public Clip(final String chapterSymbol, final Date lastModifiedTime) {
 			this.chapterSymbol = chapterSymbol;
 			this.lastModifiedTime = lastModifiedTime;
 		}
 		
 		@Override
 		public String toString() {
 			final StringBuilder b = new StringBuilder();
 			//b.append(chapter + lineBreak);
 			b.append(openComment + " " + positionCode + " " + closeComment + lineBreak);
 			b.append(openComment + " " + positionDescription + " " + closeComment + lineBreak);
 			if((null!=clipTitle)&&(clipTitle.trim().length()>0)) {
 				b.append(openComment + " Title: " + clipTitle  + " " + closeComment + lineBreak);
 			}
 			b.append(lineBreak);
 			b.append(progText + lineBreak);
 			if(null!=calloutText) {
 				for(int i=0;i<calloutText.size();++i) {
 					b.append(lineBreak);
 					b.append(openComment + " Note " + (i+1) + ": " + closeComment + lineBreak + openComment + "   ");
 					final String ci = calloutText.get(i);
 					b.append(ci.replaceAll("[ \t]+"," ").replaceAll("[\n\r]+"," " + closeComment + lineBreak + openComment + "  "));
 					b.append(" " + closeComment + lineBreak);
 				}
 			}
 			b.append(lineBreak);
 			return b.toString();
 		}
 	}
 	
 	public static interface ClipConsumer {
 		void takeClip(Clip clip) throws IOException;
 	}
 	
 	public static String safeFileName(final String o) {
 		String s = o.replaceAll("\\s+"," ").trim();
 		s = s.replaceAll("[^\\w/ .]+","_");
 		return s;
 	}
 	
 	private static String cleanDirComponent(final String o) {
 		String s = o.replaceAll("\\s+"," ").trim().replace(' ','_');
 		s = s.replaceAll("[^\\w.]+","_");
 		return s;
 	}
 	
 	private static String zPad(final String s, final int k) {
 		final StringBuffer b = new StringBuffer();
 		final Pattern p = Pattern.compile("\\d+");
 		if(p.matcher(s).matches()) {
 			b.append("c");
 		} else {
 			b.append("x");
 		}
 		int n = k - s.length();
 		while(n>0) {
 			b.append('0');
 			--n;
 		}
 		b.append(s);
 		return b.toString();
 	}
 	
 	public static final class ClipZipper implements ClipConsumer {
 		private final ZipOutputStream o;
 		private final String dirName;
 		private final NumberFormat clipNF = new DecimalFormat("00000");
 		private final String defaultFileSuffix;
 		private int clipNumber = 0;
 		
		public ClipZipper(final ZipOutputStream o, final String dirName, final String readmeStr, final String defaultFileSuffix, final long time) throws IOException {
 			this.o = o;
 			this.dirName = dirName;
 			this.defaultFileSuffix = defaultFileSuffix;
 			final String safeFileName = safeFileName(dirName + "/" + "README.txt");
 			final ZipEntry e = new ZipEntry(safeFileName);
			e.setTime(time);
 			o.putNextEntry(e);
 			final byte[] data = readmeStr.getBytes("UTF-8");
 		    o.write(data, 0, data.length);
 		    o.closeEntry();
 		}
 		
 		@Override
 		public void takeClip(final Clip clip) throws IOException {
 			++clipNumber;
 			String fileSuffix = defaultFileSuffix;
 			if((clip.foundSuffix!=null)&&(clip.foundSuffix.trim().length()>0)) {
 				fileSuffix = "." + clip.foundSuffix.trim();
 			}
 			final String safeFileName = safeFileName(dirName 
 					+ "/" + cleanDirComponent(zPad(clip.chapterSymbol,2) + "_" + clip.chapter) 
 					+ "/" + cleanDirComponent(clipNF.format(clipNumber) + "_" + clip.positionCode) 
 					+ fileSuffix);
 			final ZipEntry e = new ZipEntry(safeFileName);
			e.setTime(clip.lastModifiedTime.getTime());
 			o.putNextEntry(e);
 			if(clip.progText.indexOf("&amp;")>=0) {
 				System.out.println("Warning clip has '&amp;'s: " + safeFileName);
 			}
 			final byte[] data = clip.toString().getBytes("UTF-8");
 		    o.write(data, 0, data.length);
 		    o.closeEntry();
 		}
 	}
 
 	// config
 	public boolean takeCallouts = true;
 	public String openComment = "#";
 	public String closeComment = "";
 	public String lineBreak = "\n";
 	private static final String PROGRAMLISTING = "programlisting";
 	private static final String CALLOUT = "callout";
 	private static final String TITLE = "title";
 	private static final String EXAMPLE = "example";
 	private final Set<String> blocks = new HashSet<String>(Arrays.asList(new String[] { EXAMPLE, "informalexample" }));
 	private final Set<String> trackedTextContexts = new HashSet<String>(Arrays.asList(new String[] { TITLE, PROGRAMLISTING, CALLOUT }));
 	private final ClipConsumer clipConsumer;
 	private final ErrorCollector ec;
 	// state
 	public final Date lastModifiedTime;
 	public final ItemLabeler itemLabeler = new ItemLabeler();
 	private LinkedList<String> curContext = new LinkedList<String>();
 	private Map<String,StringBuilder> charCollectors = new HashMap<String,StringBuilder>();
 	private int nCallouts = 0;
 	private String foundSuffix = null;
 	private String progText = null;
 	private ArrayList<String> calloutText = null;
 	private String progTitle = null;
 
 	
 	public ExampleClipper(final ErrorCollector ec, final ClipConsumer clipConsumer, final Date lastModifiedTime) {
 		this.lastModifiedTime = lastModifiedTime;
 		this.clipConsumer = clipConsumer;
 		this.ec = ec;
 	}
 	
 	
 	@Override
 	public void startElement(final String uri, 
 			final String localName, final String qName, 
 			final Attributes attributes) throws SAXException {
 		itemLabeler.startElement(uri, localName, qName, attributes);
 		if(qName.equals(PROGRAMLISTING)) {
 			foundSuffix = attributes.getValue("lang");
 		} else if(qName.equals("calloutlist")) {
 			calloutText = new ArrayList<String>();
 		}
 		if(blocks.contains(qName)) {
 			progText = null;
 			progTitle = null;
 			calloutText = null;
 			nCallouts = 0;
 			charCollectors.put(PROGRAMLISTING,new StringBuilder());
 			charCollectors.put(TITLE,new StringBuilder());
 		}
 		if(takeCallouts) {
 			if(qName.equals("co")) {
 				final StringBuilder chars = charCollectors.get(PROGRAMLISTING);
 				if(null!=chars) {
 					++nCallouts;
 					chars.append("\t" + openComment + " Note: " + nCallouts + " " + closeComment);
 				}
 			} else if(qName.equals(CALLOUT)) {
 				charCollectors.put(CALLOUT,new StringBuilder());
 			}
 		}
 		if(trackedTextContexts.contains(qName)) {
 			curContext.addLast(qName);
 		}
 	}
 	
 	@Override
 	public void characters(final char[] ch,
             final int start,
             final int length)
             throws SAXException {
 		itemLabeler.characters(ch, start, length);
 		if(!curContext.isEmpty()) {
 			final StringBuilder chars = charCollectors.get(curContext.getLast());
 			if(null!=chars) {
 				for(int i=start;i<start+length;++i) {
 					chars.append(ch[i]);
 				}
 			}
 		}
 	}
 	
 	@Override
 	public void endElement(final String uri,
             final String localName,
             final String qName)
             throws SAXException {
 		itemLabeler.endElement(uri, localName, qName);
 		if(qName.equals(PROGRAMLISTING)) {
 			progText = charCollectors.remove(PROGRAMLISTING).toString().trim();
 		} else if(qName.equals(CALLOUT)) {
 			calloutText.add(charCollectors.remove(CALLOUT).toString().trim());
 		} else if(qName.equals(TITLE)) {
 			progTitle = null;
 			final StringBuilder foundTitle = charCollectors.remove(TITLE);
 			if(null!=foundTitle) {
 				progTitle = foundTitle.toString().replaceAll("\\s+"," ").trim();
 			}
 		}
 		if(blocks.contains(qName)) {
 			if((null!=progText)&&(progText.trim().length()>0)) {
 				final Clip clip = new Clip(itemLabeler.chapterSymbol(),lastModifiedTime);
 				clip.chapter = itemLabeler.chapterName();
 				clip.positionCode = itemLabeler.curPositionCode(qName);
 				clip.positionDescription = itemLabeler.curPositionDescription(qName);
 				clip.clipTitle = progTitle;
 				if(EXAMPLE.equals(qName)&&((null==progTitle)||(progTitle.trim().length()<=0))) {
 					ec.mkError("no title", "no title: " + itemLabeler.curPositionCode(qName));
 				}
 				clip.foundSuffix = foundSuffix;
 				clip.progText = progText;
 				clip.calloutText = calloutText;
 				if(null!=clipConsumer) {
 					try {
 						clipConsumer.takeClip(clip);
 					} catch (IOException e) {
 						throw new SAXException("caught: " + e);
 					}
 				}
 			} else {
 				ec.mkError("no prog", "no prog: " + itemLabeler.curPositionCode(qName));
 			}
 			progTitle = null;
 		}
 		if((!curContext.isEmpty())&&(trackedTextContexts.contains(qName))) {
 			curContext.removeLast();
 		}
 	}
 }
