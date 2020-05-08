 package net.sf.okapi.apptest.dummyfilter;
 
 import java.io.InputStream;
 import java.net.URL;
 import java.util.ArrayList;
 
 import net.sf.okapi.apptest.annotation.TargetsAnnotation;
 import net.sf.okapi.apptest.common.IEncoder;
 import net.sf.okapi.apptest.common.IParameters;
 import net.sf.okapi.apptest.common.IResource;
 import net.sf.okapi.apptest.filters.FilterEvent;
 import net.sf.okapi.apptest.filters.IFilter;
 import net.sf.okapi.apptest.filters.FilterEvent.FilterEventType;
 import net.sf.okapi.apptest.resource.Code;
 import net.sf.okapi.apptest.resource.Property;
 import net.sf.okapi.apptest.resource.StartDocument;
 import net.sf.okapi.apptest.resource.DocumentPart;
 import net.sf.okapi.apptest.resource.Ending;
 import net.sf.okapi.apptest.resource.StartGroup;
 import net.sf.okapi.apptest.resource.TextContainer;
 import net.sf.okapi.apptest.resource.TextFragment;
 import net.sf.okapi.apptest.resource.TextUnit;
 import net.sf.okapi.apptest.resource.TextFragment.TagType;
 import net.sf.okapi.apptest.skeleton.GenericSkeleton;
 
 public class DummyFilter implements IFilter {
 
 	ArrayList<FilterEvent> list;
 	private int current;
 	private boolean canceled;
 	private String language;
 	private IEncoder encoder;
 	
 	public DummyFilter () {
 		encoder = new DummyEncoder();
 	}
 	
 	public String getName () {
 		return "DummyFilter";
 	}
 	
 	public void setOptions (String language,
 		String defaultEncoding,
 		boolean generateSkeleton)
 	{
 		System.out.println("DummyParser: setOptions() called");
 		this.language = language;
 		System.out.println(" -output language = " + language);
 		System.out.println(" -output default encoding = " + defaultEncoding);
 	}
 	
 	public void open (InputStream input) {
 		System.out.println("DummyParser: open(stream) called");
 		resetResources();
 	}
 	
 	public void open (CharSequence inputText) {
 		System.out.println("DummyParser: open(charseq) called");
 		resetResources();
 	}
 
 	public void open (URL inputURL) {
 		System.out.println("DummyParser: open(URL) called");
 		resetResources();
 	}
 
 	public void close () {
 		System.out.println("DummyParser: close() called");
 		list.clear();
 		list = null;
 	}
 	
 	public boolean hasNext () {
 		if ( list == null ) return false;
 		return (current<list.size()-1);
 	}
 	
 	public FilterEvent next () {
 		if ( canceled ) {
 			if ( current < list.size()-1 ) {
 				current = list.size()-2;
 			}
 		}
 		current++;
 		return list.get(current);
 	}
 	
 	public IResource getResource () {
 		return list.get(current).getResource();
 	}
 
 	private void makeCase001 () {
 		// <p>Before <img href='img.png' alt='text'/> after.</p>
 
 		DocumentPart dp = new DocumentPart("dp1", true);
 		dp.setSourceProperty(new Property("href", "img.png", false));
 		list.add(new FilterEvent(FilterEventType.DOCUMENT_PART, dp));
 
 		list.add(new FilterEvent(FilterEventType.TEXT_UNIT,
 			new TextUnit("t1", "text", true, encoder)));
 
 		TextUnit tu = new TextUnit("t2", "Before ", false, encoder);
 		TextFragment tf = tu.getSourceContent();
 		Code code = tf.append(TagType.PLACEHOLDER, "image",
 			"<img href='" + TextFragment.makeRefMarker("dp1", "href") +
 			"' alt='" + TextFragment.makeRefMarker("t1") + "'/>");
 		code.setHasReference(true);
 		tf.append(" after.");
 		
 		GenericSkeleton skel = new GenericSkeleton();
 		skel.add("<p>");
 		skel.addRef(tu, null);
 		skel.add("</p>");
 		list.add(new FilterEvent(FilterEventType.TEXT_UNIT, tu, skel));
 
 	}
 	
 	private void makeCase002 () {
 		// <p>Before <a href='link.htm'/> after.</p>
 		
 		DocumentPart dp = new DocumentPart("dp1", true);
 		dp.setSourceProperty(new Property("href", "link.htm", false));
 		list.add(new FilterEvent(FilterEventType.DOCUMENT_PART, dp));
 
 		TextUnit tu = new TextUnit("t1", "Before ", false, encoder);
 		TextFragment tf = tu.getSourceContent();
 		Code code = tf.append(TagType.PLACEHOLDER, "link",
 			"<a href='" + TextFragment.makeRefMarker("dp1", "href") + "'/>");
 		code.setHasReference(true);
 		tf.append(" after.");
 
 		GenericSkeleton skel = new GenericSkeleton("<p>");
 		skel.addRef(tu, null);
 		skel.add("</p>");
 		list.add(new FilterEvent(FilterEventType.TEXT_UNIT, tu, skel));
 	}
 	
 	private void makeCase003 () {
 		// <table id=100> <tr><td>text</td></tr><table>
 		
 		StartGroup grp1 = new StartGroup("d1", "g1");
 		grp1.setSourceProperty(new Property("id", "100", true));
 		list.add(new FilterEvent(FilterEventType.START_GROUP, grp1,
 			new GenericSkeleton("<table id=100>\n ")));
 
 		StartGroup grp2 = new StartGroup("g1", "g2");
 		list.add(new FilterEvent(FilterEventType.START_GROUP, grp2,
 			new GenericSkeleton("<tr>")));
 		
 		StartGroup grp3 = new StartGroup("g2", "g3");
 		list.add(new FilterEvent(FilterEventType.START_GROUP, grp3,
 			new GenericSkeleton("<td>")));
 		
 		list.add(new FilterEvent(FilterEventType.TEXT_UNIT,
 			new TextUnit("t1", "text", false, encoder)));
 		
 		list.add(new FilterEvent(FilterEventType.END_GROUP,
 			new Ending("e3"),
 			new GenericSkeleton("</td>")));
 
 		list.add(new FilterEvent(FilterEventType.END_GROUP,
 			new Ending("e2"),
 			new GenericSkeleton("</tr>\n")));
 		
 		list.add(new FilterEvent(FilterEventType.END_GROUP,
 			new Ending("e1"),
 			new GenericSkeleton("</table>\n")));
 	}
 
 	private void makeCase004 () {
 		StartGroup grp1 = new StartGroup("d1", "g1", true);
 		list.add(new FilterEvent(FilterEventType.START_GROUP, grp1,
 			new GenericSkeleton("<ul>")));
 		
 		StartGroup grp2 = new StartGroup("g1", "g2", false);
 		list.add(new FilterEvent(FilterEventType.START_GROUP, grp2,
 			new GenericSkeleton("\n  <li>")));
 		
 		list.add(new FilterEvent(FilterEventType.TEXT_UNIT,
 			new TextUnit("t1", "Text of item 1 with special char < and &.", false, encoder)));
 		
 		list.add(new FilterEvent(FilterEventType.END_GROUP,
 			new Ending("e2"),
 			new GenericSkeleton("</li>")));
 		
 		StartGroup grp3 = new StartGroup("g1", "g3", false);
 		list.add(new FilterEvent(FilterEventType.START_GROUP, grp3,
 			new GenericSkeleton("\n  <li>")));
 		
 		list.add(new FilterEvent(FilterEventType.TEXT_UNIT,
 			new TextUnit("t2", "Text of item 2", false, encoder)));
 		
 		list.add(new FilterEvent(FilterEventType.END_GROUP,
 			new Ending("e3"),
 			new GenericSkeleton("</li>")));
 		
 		list.add(new FilterEvent(FilterEventType.END_GROUP,
 			new Ending("e1"),
 			new GenericSkeleton("\n </ul>")));
 
 		TextUnit tu = new TextUnit("t3");
 		tu.setEncoder(encoder);
 		TextFragment tf = new TextFragment();
 		tf.append("Text before list: \n ");
 		Code code = tf.append(TagType.PLACEHOLDER, "list",
 			TextFragment.makeRefMarker("g1"));
 		code.setHasReference(true);
 		tf.append("\n and text after the list.");
 		tu.setSourceContent(tf);
 		GenericSkeleton skel = new GenericSkeleton("<p>");
 		skel.addRef(tu, null);
 		skel.add("</p>");
 		list.add(new FilterEvent(FilterEventType.TEXT_UNIT, tu, skel));
 	}
 	
 	private void makeCase008 () {
 		//<tu tuid="1" datatype="Text">
 		// <note>TU level note</note>
 		// <prop type="x-Domain">TU level prop</prop>
 		// <tuv xml:lang="EN" creationid="Okapi">
 		//  <seg>Hello &lt; World</seg>
 		// </tuv>
 		// <tuv xml:lang="FR-CA" creationid="Okapi" changeid="Olifant">
 		//  <prop type="Origin">MT</prop>
 		//  <seg>Bonjour tout le monde</seg>
 		// </tuv>
 		//</tu>		
 
 		// Update the document info: TMX is multilingual input
 		((StartDocument)list.get(list.size()-1).getResource()).setIsMultilingual(true);
 		
 		TextUnit tu = new TextUnit("t1", "Hello < and { World", false, encoder);
 
 		String trgLang = "FR-CA";
 		TargetsAnnotation ta = new TargetsAnnotation();
 		TextContainer trgCont = new TextContainer("Bonjour tout le monde");
 		ta.set(trgLang, trgCont);
 		tu.setAnnotation(ta);
 		
 		GenericSkeleton skel = new GenericSkeleton("<tu tuid=\"1\" datatype=\"Text\">\n");
 		tu.setProperty(new Property("datatype", "Text", true));
 		tu.setProperty(new Property("tuid", "1", true));
 		skel.append(" <note>");
 		skel.addRef(tu, "note-1", "");
 		skel.append("</note>\n");
 		tu.setProperty(new Property("note-1", "TU level note", false));
 		skel.append(" <prop type=\"x-Domain\">TU level prop</prop>\n");
 		tu.setProperty(new Property("x-Domain", "TU level prop", true));
 		skel.append(" <tuv xml:lang=\"EN\" creationid=\"Okapi\">\n");
 		tu.setSourceProperty(new Property("creationid", "Okapi", true));
 		skel.append("  <seg>");
 		skel.addRef(tu, null);
 		skel.add("<seg>\n </tuv>\n");
 		skel.append(" <tuv xml:lang=\"FR-CA\" creationid=\"Okapi\" ");
 		tu.setTargetProperty(trgLang, new Property("creationid", "Okapi", true));
 		skel.append("changeid=\"");
 		trgCont.setProperty(new Property("changeid", "Olifant", false));
 		
 		skel.addRef(tu, "changeid", trgLang);
 		skel.add("\">\n");
 		skel.append("  <prop type=\"Origin\">");
 		trgCont.setProperty(new Property("Origin", "MT", false));
 		skel.addRef(tu, "Origin", trgLang);
 		skel.add("</prop>\n");
 		skel.append("  <seg>");
 		skel.addRef(tu, trgLang); 
 		skel.add("</seg>\n");
 		skel.append(" </tuv>\n</tu>");
 		
 		list.add(new FilterEvent(FilterEventType.TEXT_UNIT, tu, skel));
 	}
 	
 	private void resetResources () {
 		canceled = false;
 		current = -1;
 		list = new ArrayList<FilterEvent>();
 		
 		list.add(new FilterEvent(FilterEventType.START, null));
 
 		StartDocument docRes = new StartDocument();
 		docRes.setId("d1");
 		docRes.setEncoding("UTF-8"); // Always
 		docRes.setLanguage(language);
 		docRes.setName("myDocument");
 		list.add(new FilterEvent(FilterEventType.START_DOCUMENT, docRes));
 
		makeCase001();
 		//makeCase002();
 		//makeCase003();
 		//makeCase004();
		//makeCase008();
 	
 		list.add(new FilterEvent(FilterEventType.END_DOCUMENT,
 			new Ending("ed1")));
 		
 		list.add(new FilterEvent(FilterEventType.FINISHED, null));
 	}
 
 	public void cancel () {
 		canceled = true;
 	}
 
 	public IParameters getParameters () {
 		return null;
 	}
 
 	public void setParameters (IParameters params) {
 		// No parameters used for this filter.
 	}
 
 }
