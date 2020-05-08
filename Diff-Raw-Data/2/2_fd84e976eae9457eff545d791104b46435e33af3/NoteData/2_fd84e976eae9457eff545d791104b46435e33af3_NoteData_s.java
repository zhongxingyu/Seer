 package data;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.HashMap;
 
 import org.apache.commons.lang3.StringUtils;
 import org.codehaus.jackson.annotate.JsonIgnore;
 import org.neo4j.graphdb.Direction;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.Relationship;
 
 import play.Logger;
 import play.data.format.Formats;
 import play.data.validation.Constraints;
 
 import data.Database.Rels;
 
 
 public final class NoteData {
 
 	public static class StaType {
 		public static final int AXIOM = 0;
 		public static final int THEOREM = 1;
 		public static final int DEFINITION = 2;
 		public static final int GROUP = 3;
 	}
 	
 	public static final String[] AXIOMS = {"axiom"};
 	public static final String[] THEOREMS = {"theorem", "proposition", "lemma", "corollary"
 		, "converse", "identity", "rule", "law", "principle"};
 
 	public int type = StaType.AXIOM;
 	
 	public String name;
 
 	public long cid = -1;
 	
     @Constraints.Required
     @Formats.NonEmpty
     @JsonIgnore
 	public String text;
 	@Constraints.Required
     @Formats.NonEmpty
     @JsonIgnore
     public String html;
 
     @JsonIgnore
 	public long addTime;
 
     private static SimpleDateFormat dt = new SimpleDateFormat("yy/MM/dd"); 
 
     @JsonIgnore
     public String getTimestamp() {
     	return dt.format(new Date(addTime));
     }
 
     @JsonIgnore
 	public long updateTime;
 
 	public long id = -1;
 
 	public static class Pro {
 		public static final String TITLE = "title";
 		public static final String TEXT = "text";
 		public static final String ADDTIME = "addtime";
 		public static final String UPDATETIME = "updatetime";
 		public static final String HTML = "html";
 		public static final String TYPE = "type";
 		public static final String X = "x";
 		public static final String Y = "y";
 		
 	}
 	// should never be used!! used only by form!!!
 	public NoteData() {
 	}
 	
 	public NoteData(boolean hack) {
 		this();
 		name = text = html = "";
 	}
 	
 	public NoteData(Node node) {
 		addTime = (long) node.getProperty(Pro.ADDTIME);
 		updateTime = (long) node.getProperty(Pro.UPDATETIME);
 		name = (String) node.getProperty(Pro.TITLE);
 		text = (String) node.getProperty(Pro.TEXT);
 		html = (String) node.getProperty(Pro.HTML);
 		type = (int) node.getProperty(Pro.TYPE);
 		id = node.getId();
 		if (node.hasRelationship(Rels.INCLUDE, Direction.INCOMING)) {
 			cid = node.getSingleRelationship(Rels.INCLUDE, Direction.INCOMING).getStartNode().getId();
 		} else if (node.hasRelationship(Rels.FRONTPAGE_IS, Direction.INCOMING)) {
 			cid = node.getSingleRelationship(Rels.FRONTPAGE_IS, Direction.INCOMING).getStartNode().getId();
 		}
 	}
 
 	public boolean write(Node n) {
 		addTime = updateTime = System.currentTimeMillis();
 		int i = 0;
 loop:	for (; i < text.length(); i++) {
 			switch (text.charAt(i)) {
 			case ' ':case '\n':case '\t':case '#':
 				continue;
 			default:
 				break loop;
 			}
 		}
 		int end = text.indexOf('\n', i);
 		if (end == -1) {
 			end = text.length();
 		}
 		if (i > end) {
 			i = end;
 		}
 		name = text.substring(i, end);
 		n.setProperty(Pro.TITLE, name);
 		n.setProperty(Pro.TEXT, text);
 		if (!(id > 0)) {
 			n.setProperty(Pro.ADDTIME, addTime);
 			id = n.getId();
 		}
 		n.setProperty(Pro.UPDATETIME, updateTime);
 		n.setProperty(Pro.HTML, html);
 		n.setProperty(Pro.TYPE, StaType.AXIOM);
 		return true;
 	}
 	
 	public boolean writeMath(Node n) {
 		deduceType(n);
 		n.setProperty(Pro.TYPE, type);
 		ArrayList<Long> axiomids = new ArrayList<Long>();
 		for (int i = text.indexOf("::"); i > 0; i = text.indexOf("::", i+2)) {
 			int j = i+2;
 			while (j < text.length() && text.charAt(j) >= '0' && text.charAt(j) <= '9') j++;
 			long axiomid;
 			try {
 				axiomid = Integer.parseInt(text.substring(i+2, j));
 			} catch (NumberFormatException e){
 				continue;
 			}
 			if (!axiomids.contains(axiomid))
 				axiomids.add(axiomid);
 		}
 		if (type != StaType.AXIOM && axiomids.size() == 0) {
 			return false;
 		}
 		if (type == StaType.AXIOM 
 				&& n.getSingleRelationship(Rels.AXIOM_IS, Direction.INCOMING) == null) {
 			n.getSingleRelationship(Rels.INCLUDE, Direction.INCOMING).getStartNode()
 				.createRelationshipTo(n, Rels.AXIOM_IS);
 		}
 		if (type != StaType.AXIOM || axiomids.size() > 0) {
 			Relationship r = n.getSingleRelationship(Rels.AXIOM_IS, Direction.INCOMING);
 			if (r != null)
 				r.delete();
 		}
 
 		for (int i = 0; i < axiomids.size();) {
 			NoteData ax = get(axiomids.get(i));
			html = html.replaceAll("::"+ax.id, "<a href=\""+ ax.cid + "/" + ax.id + "\">"+ax.name+"</a>");
 			if (ax.cid != cid) {
 				axiomids.remove(i);
 			} else {
 				i++;
 			}
 		}
 		n.setProperty(Pro.HTML, html);
 		if (type == StaType.GROUP) {
 			for (Relationship r : n.getRelationships(Rels.GROUP_HAS, Direction.OUTGOING)) {
 				Node s = r.getEndNode();
 				if (axiomids.contains(s.getId())) {
 					axiomids.remove(s.getId());
 				} else {
 					r.delete();
 				}
 			}
 			// only create for same category notes
 			for (Long id : axiomids) {
 				Node s = Database.get(id);
 				if (s != null && s.hasRelationship(Rels.INCLUDE, Direction.INCOMING)) {
 					n.createRelationshipTo(s, Rels.GROUP_HAS);
 				}
 			}
 		} else {
 			for (Relationship r : n.getRelationships(Rels.PROOF, Direction.INCOMING)) {
 				Node s = r.getStartNode();
 				if (axiomids.contains(s.getId())) {
 					axiomids.remove(s.getId());
 				} else {
 					r.delete();
 				}
 			}
 			for (Long id : axiomids) {
 				Node s = Database.get(id);
 				if (s != null && s.hasRelationship(Rels.INCLUDE, Direction.INCOMING)) {
 					s.createRelationshipTo(n, Rels.PROOF);
 				}
 			}
 			for (Relationship r : n.getRelationships(Rels.GROUP_HAS, Direction.OUTGOING)) {
 				r.delete();
 			}
 		}
 		return true;
 	}
 
 	private void deduceType(Node n) {
 		if (name.startsWith("group. ")) {
 			type = StaType.GROUP;
 			name = name.substring(7, name.length()).trim();
 			html = html.replaceFirst("group\\. ", "");
 			n.setProperty(Pro.TITLE, name);
 			n.setProperty(Pro.HTML, html);
 			return;
 		}
 		if (name.startsWith("def. ")) {
 			type = StaType.DEFINITION;
 			name = name.substring(5, name.length()).trim();
 			html = html.replaceFirst("def\\. ", "");
 			n.setProperty(Pro.TITLE, name);
 			n.setProperty(Pro.HTML, html);
 			return;
 		}
 		if (name.startsWith("axiom. ")) {
 			type = StaType.AXIOM;
 			name = name.substring(7, name.length()).trim();
 			html = html.replaceFirst("axiom\\. ", "");
 			n.setProperty(Pro.TITLE, name);
 			n.setProperty(Pro.HTML, html);
 			return;
 		}
 		for (String s : AXIOMS) {
 			if (StringUtils.containsIgnoreCase(name, s)) {
 				type = StaType.AXIOM;
 				return;
 			}
 		}
 		for (String s : THEOREMS) {
 			if (StringUtils.containsIgnoreCase(name, s)) {
 				type = StaType.THEOREM;
 				return;
 			}
 		}
 		type = StaType.DEFINITION;
 	}
 	
 	@JsonIgnore
 	public boolean hasTitle() {
 		return html.startsWith("<h");
 	}
 	
 	public static final long MILLIS = 60 * 1000;
 	public static HashMap<String, Comparator<NoteData>> comparators = new HashMap<String, Comparator<NoteData>>();
 	static {
 		comparators.put("addtime desc", new Comparator<NoteData>() {
 			@Override
 			public int compare(NoteData o1, NoteData o2) {
 				return (int) ((o2.addTime - o1.addTime)/MILLIS);
 			}});
 		comparators.put("addtime asc", new Comparator<NoteData>() {
 			@Override
 			public int compare(NoteData o1, NoteData o2) {
 				return (int) ((o1.addTime - o2.addTime)/MILLIS);
 			}});
 		comparators.put("updatetime asc", new Comparator<NoteData>() {
 			@Override
 			public int compare(NoteData o1, NoteData o2) {
 				return (int) ((o1.updateTime - o2.updateTime)/MILLIS);
 			}});
 		comparators.put("updatetime asc", new Comparator<NoteData>() {
 			@Override
 			public int compare(NoteData o1, NoteData o2) {
 				return (int) ((o1.updateTime - o2.updateTime)/MILLIS);
 			}});
 		comparators.put("", new Comparator<NoteData>() {
 			@Override
 			public int compare(NoteData o1, NoteData o2) {
 				return (int) ((o1.id - o2.id)/MILLIS);
 			}});
 	}
 	
 	public static Comparator<NoteData> getComparator(String itemorder) {
 		Comparator<NoteData> c = comparators.get(itemorder.trim());
 		if (c == null)
 			c = comparators.get("");
 		return c;
 	}
 	
 	public static NoteData get(long id) {
 		Node n = Database.get(id);
 		if (n == null)
 			return null;
 		if (n.hasRelationship(Rels.INCLUDE, Direction.INCOMING)
 				|| n.hasRelationship(Rels.FRONTPAGE_IS, Direction.INCOMING)) {
 			return new NoteData(n);
 		}
 		return null;
 	}
 	
 	
 	
 	// bug? why need setters??
 	
 
 	public void setText(String text) {
 		this.text = text;
 	}
 	public void setHtml(String html) {
 		this.html = html;
 	}
 	
 }
