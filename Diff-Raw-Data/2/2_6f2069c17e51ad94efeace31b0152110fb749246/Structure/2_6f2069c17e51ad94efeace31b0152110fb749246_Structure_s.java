 /*
  * 
  */
 package openscriptures.text;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.UUID;
 
 import javax.persistence.Basic;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 
 
 /**
  * @author Neal Audenaert
  */
 @Entity
 @Table(name="OpenS_Structures")
 public class Structure extends AbstractTokenSequence {
 	
 	/** Maps input tag and/or structure names to output structures. This may be used to 
 	 * transform input structures from one schema to another as needed. */
 	public static Map<String, String> ELEMENT_CHOICES = new HashMap<String, String>();
 	
 	/* Static initialization block. */
 	static {
 		// TODO these should be made more robust and drawn from an XML file or other 
 		//		extensible representation.
 		
 		// Element list
         ELEMENT_CHOICES.put("a", "a");
         ELEMENT_CHOICES.put("abbr", "abbr");
         ELEMENT_CHOICES.put("actor", "actor");
         ELEMENT_CHOICES.put("caption", "caption");
         ELEMENT_CHOICES.put("castGroup", "castGroup");
         ELEMENT_CHOICES.put("castItem", "castItem");
         ELEMENT_CHOICES.put("castList", "castList");
         ELEMENT_CHOICES.put("catchWord", "catchWord");
         ELEMENT_CHOICES.put("cell", "cell");
         ELEMENT_CHOICES.put("chapter", "chapter");
         ELEMENT_CHOICES.put("closer", "closer");
         ELEMENT_CHOICES.put("contributor", "contributor");
         ELEMENT_CHOICES.put("coverage", "coverage");
         ELEMENT_CHOICES.put("creator", "creator");
         ELEMENT_CHOICES.put("date", "date");
         ELEMENT_CHOICES.put("description", "description");
         //"div", (no need for this since promoting div[type] to full element named `type`)
         ELEMENT_CHOICES.put("divineName", "divineName");
         ELEMENT_CHOICES.put("figure", "figure");
         ELEMENT_CHOICES.put("foreign", "foreign");
         ELEMENT_CHOICES.put("format", "format");
         ELEMENT_CHOICES.put("head", "head");
         ELEMENT_CHOICES.put("header", "header");
         ELEMENT_CHOICES.put("hi", "hi");
         ELEMENT_CHOICES.put("identifier", "identifier");
         ELEMENT_CHOICES.put("index", "index");
         ELEMENT_CHOICES.put("inscription", "inscription");
         ELEMENT_CHOICES.put("item", "item");
         ELEMENT_CHOICES.put("l", "l");
         ELEMENT_CHOICES.put("label", "label");
         ELEMENT_CHOICES.put("language", "language");
         ELEMENT_CHOICES.put("lb", "lb");
         ELEMENT_CHOICES.put("lg", "lg");
         ELEMENT_CHOICES.put("list", "list");
         ELEMENT_CHOICES.put("mentioned", "mentioned");
         //"milestone",      (n/a since all strucutres milestoned)
         //"milestoneEnd",   (n/a since all strucutres milestoned)
         //"milestoneStart", (n/a since all strucutres milestoned)
         ELEMENT_CHOICES.put("name", "name");
         ELEMENT_CHOICES.put("note", "note");
         ELEMENT_CHOICES.put("osis", "osis");
         ELEMENT_CHOICES.put("osisCorpus", "osisCorpus");
         ELEMENT_CHOICES.put("osisText", "osisText");
         ELEMENT_CHOICES.put("p", "p");
         ELEMENT_CHOICES.put("publisher", "publisher");
         ELEMENT_CHOICES.put("q", "q");
         ELEMENT_CHOICES.put("rdg", "rdg");
         ELEMENT_CHOICES.put("rdgGrp", "rdgGrp");
         ELEMENT_CHOICES.put("refSystem", "refSystem");
         ELEMENT_CHOICES.put("reference", "reference");
         ELEMENT_CHOICES.put("relation", "relation");
         ELEMENT_CHOICES.put("revisionDesc", "revisionDesc");
         ELEMENT_CHOICES.put("rights", "rights");
         ELEMENT_CHOICES.put("role", "role");
         ELEMENT_CHOICES.put("roleDesc", "roleDesc");
         ELEMENT_CHOICES.put("row", "row");
         ELEMENT_CHOICES.put("salute", "salute");
         ELEMENT_CHOICES.put("scope", "scope");
         ELEMENT_CHOICES.put("seg", "seg");
         ELEMENT_CHOICES.put("seq", "seq");
         ELEMENT_CHOICES.put("signed", "signed");
         ELEMENT_CHOICES.put("source", "source");
         ELEMENT_CHOICES.put("speaker", "speaker");
         ELEMENT_CHOICES.put("speech", "speech");
         ELEMENT_CHOICES.put("subject", "subject");
         ELEMENT_CHOICES.put("table", "table");
         ELEMENT_CHOICES.put("teiHeader", "teiHeader");
         ELEMENT_CHOICES.put("title", "title");
         ELEMENT_CHOICES.put("titlePage", "titlePage");
         ELEMENT_CHOICES.put("transChange", "transChange");
         ELEMENT_CHOICES.put("type", "type");
         ELEMENT_CHOICES.put("verse", "verse");
         ELEMENT_CHOICES.put("w", "w");
         ELEMENT_CHOICES.put("work", "work");
 
         // Promoting div[type] elements to elements of the name [type]
         ELEMENT_CHOICES.put("acknowledgement", "acknowledgement");
         ELEMENT_CHOICES.put("afterword", "afterword");
         ELEMENT_CHOICES.put("annotant", "annotant");
         ELEMENT_CHOICES.put("appendix", "appendix");
         ELEMENT_CHOICES.put("article", "article");
         ELEMENT_CHOICES.put("back", "back");
         ELEMENT_CHOICES.put("body", "body");
         ELEMENT_CHOICES.put("book", "book");
         ELEMENT_CHOICES.put("bookGroup", "bookGroup");
         //"chapter", (use existing element above)
         ELEMENT_CHOICES.put("colophon", "colophon");
         ELEMENT_CHOICES.put("commentary", "commentary");
         ELEMENT_CHOICES.put("concordance", "concordance");
         ELEMENT_CHOICES.put("coverPage", "coverPage");
         ELEMENT_CHOICES.put("dedication", "dedication");
         ELEMENT_CHOICES.put("devotional", "devotional");
         ELEMENT_CHOICES.put("entry", "entry");
         ELEMENT_CHOICES.put("front", "front");
         ELEMENT_CHOICES.put("gazetteer", "gazetteer");
         ELEMENT_CHOICES.put("glossary", "glossary");
         ELEMENT_CHOICES.put("imprimatur", "imprimatur");
         //"index", (use existing element above)
         ELEMENT_CHOICES.put("introduction", "introduction");
         ELEMENT_CHOICES.put("majorSection", "majorSection");
         ELEMENT_CHOICES.put("map", "map");
         //"paragraph", (use existing element above)
         ELEMENT_CHOICES.put("part", "part");
         ELEMENT_CHOICES.put("preface", "preface");
         ELEMENT_CHOICES.put("section", "section");
         ELEMENT_CHOICES.put("subSection", "subSection");
         ELEMENT_CHOICES.put("summary", "summary");
         //"titlePage", (use existing element above)
         
         // New elements
         ELEMENT_CHOICES.put("page", "page"); // used to preserve page boundaries; TEI?
         
         // Proposed
         //ELEMENT_CHOICES.put("doubted", "doubted"); //level1 and level2? rdg
 	}
 
 //========================================================================================
 // MEMBER VARIABLES
 //========================================================================================
 	// TODO comments are currently derived from Python reference implementation. need to 
 	//      be updated
 	
 	private Long id;
 	
 	protected UUID uuid;
 	
 	/**
 	 * The name of the structure. This roughly corresponds to the XML element name.
 	 */
 	protected String name;
 	
 	/**
 	 * A locally unique identifier for this structure. This is (optionally) used to provides 
 	 * access to domain specific identifiers (e.g., OSIS ids for books, chapters and verses).
 	 */
 	protected String localId;
 	
 	/** The work this structure pertains to. This must be same as the start and end tokens. */
 	protected UUID workUUID;
 	
 	/** 
      * The position of the token that starts the structure's content; this may or may not 
      * include the startMarker, such as a quotation mark. 
      * 
      * NOTE we are currently evaluating the relevance of 'startMarker' and support for this 
      *      feature is not currently implemented. It will likely be included in a 
      *      future version.
      */
 	protected Integer startTokenPosition = null;
 	
 	/** Same as startTokenPosition, but for the end. */
 	protected Integer endTokenPosition = null;
 	
 	/** The token that starts the structure's content. This is used for caching purposes. */
 	private Token startToken = null;
 	
 	/** Same as startToken, but for the end. */
 	private Token endToken = null;
 	
     protected Structure parent = null;
     protected List<Structure> children = new ArrayList<Structure>();
     
     protected String perspective;
 
     protected Map<String, StructureAttribute> attributes = 
         new HashMap<String, StructureAttribute>();
     
 //========================================================================================
 // MISC PROPERTIES, CURRENTLY UNUSED
 //========================================================================================
      
 	/**
 	 * The optional token that marks the start of the structure. This marker may be included
 	 * (inside) in the startToken/endToken range as in the example of quotation marks, or 
 	 * it may excluded (outside) as in the case of paragraph markers which are double 
 	 * linebreaks. Outside markers may overlap (be shared) among multiple paragraphs' 
 	 * start/end_markers, whereas inside markers may not.
 	 * 
 	 * TODO this is really unclear to me. I think the quotation marks make sense, but the
 	 *      paragraph seems like that should be a display issue. Assuming that we are 
 	 *      normalizing spaces on import, we shouldn't end up with multiple line breaks in the 
 	 *      token sequence. 
 	 */
 	protected Token startMarker;
 	
 	/** Same as start_marker, but for the end. */
 	protected Token endMarker;
 
 	/** URL for where this structure came from; used for base to Token.relative_source_url */
     protected String sourceUrl;
     
 	
 //========================================================================================
 // CONSTRUCTORS
 //========================================================================================
 	
 	/**
 	 * 
 	 */
 	protected Structure() {
 	}
 	
 	public Structure(Long id) {
 	    this.id = id;
 	}
 	
 	public Structure(UUID id) {
 	    this.uuid = id;
 	}
 	
 	public Structure(Work work, String name) {
 	    this.uuid = UUID.randomUUID();
         
         this.name = name;
         this.workUUID = work.getUUID();
 	}
 	
 	public Structure(Work work, String name, Token start, Token end) {
 	    this(work, name);
 	    this.setTokens(start, end);
 	}
 	
 	protected void checkWork(Token t) throws InvalidTokenException {
	    if (!t.getWork().equals(this.workUUID))
 	        throw new InvalidTokenException(
 	                "The token's work does not match this structure.", t);
 	}
 	
 	protected void checkOrder(Token s, Token e) throws InvalidTokenException {
 	    if (e != null && e.getPosition() < s.getPosition()) {
             throw new InvalidTokenException(
                     "The start token cannot come after the end token");
         }
 	}
 	
 //========================================================================================
 // ACCESSORS
 //========================================================================================
 	 /** Returns the unique DB identifier. To be used by the persistence layer. */
     @Id @GeneratedValue
     public Long getId() { return id; }
     /** Used by persistence layer to set the UUID from a string. */
     public void setId(Long id) { this.id = id; }
 	
 	/** 
 	 *  Returns the unique identifier for this <tt>Structure</tt> 
      *  @see openscriptures.text.Structure#getUUID()
      */
     @Transient public UUID getUUID() { return uuid; }
     
     /** Returns a string version of this structures UUID for use by the persistence layer. */
     @Basic String getUUIDString() { return uuid.toString(); }
     /** Used by persistence layer to set the UUID from a string. */
     public void setUUIDString(String uuid) { this.uuid = UUID.fromString(uuid); }
     
     /** Returns the <tt>Work</tt> that this structure is found in. */
     // TODO make persistent
     @Transient public UUID getWorkUUID() { return this.workUUID; }
     public void setWork(UUID uuid) { this.workUUID = uuid; }
 
     /** Return the name of this structure. This corresponds to an element 
      * name in an XML document. */
     @Basic public String getName() { return this.name; }
     /** Sets the name of this structure. */
     public void setName(String value) { this.name = value; }
 
     // TODO perhaps replace getStart/getEnd
     @Basic
     public Integer getStartTokenPosition() { return this.startTokenPosition; }
     public void setStartTokenPosition(Integer pos) { this.startTokenPosition = pos; }
     
     @Basic public Integer getEndTokenPosition() { return this.endTokenPosition; }
     public void setEndTokenPosition(Integer pos) { this.endTokenPosition = pos; }
 
     
     
     /** 
      * Returns the interpretive or analytical perspective of this structure (if defined). 
      * If desired, structures may organized into perspectives such as the Book/Chapter/Verse 
      * hierarchy for scripture passages or narrative structures such as chapters, sections,
      * paragraphs, etc. in general text.
      */
     @Basic public String getPerspective() { return this.perspective; }
     /** Sets the interpretive perspective of this structure. */
     public void setPerspective(String perspective) { this.perspective = perspective; }
     
     //========================================================================================
     // MUTATORS
     //========================================================================================
 
     @Transient
     public int getStart() {
         // TODO Test start and end behavior extensively. These have lots of failure points.
         return (startTokenPosition != null)  
                 ? startTokenPosition
                 : -1;
     }
    
     @Transient
     public int getEnd() {
         return endTokenPosition != null  
                 ? endTokenPosition + 1
                 : this.getStart();
     }
     
     private Token getToken(int pos) {
         ApplicationContext ac = ApplicationContext.getApplicationContext();
         return ac.getToken(workUUID, pos);
     }
     
     /** Returns the token at which this structure starts. */
     @Transient
     public Token getStartToken() {
         if (this.startTokenPosition == null) 
             return null;
         
         int pos = startTokenPosition.intValue();
         if ((startToken == null) || (startToken.getPosition() != pos))
             startToken = getToken(pos);
     
         return startToken;
     }
     
     /**
      * Sets the start token for this structure.
      * 
      * @param token The token to set
      * 
      * @throws UnsupportedOperationException If this operation is not implemented for a 
      *         particular structure (e.g. for a structure accessed via a REST API that 
      *         doesn't support updates).
      * @throws InvalidTokenException If the supplied token is invalid. This might be because
      *         the token's work does not match this structure's work or because the token 
      *         does not occur before the end token.
      */
     public void setStartToken(Token token) 
     throws UnsupportedOperationException, InvalidTokenException {
         this.startToken = null;
         
         Token end = this.getEndToken();
         if (end != null) 
             checkOrder(token, end);
         checkWork(token); 
         
         this.startTokenPosition = (token != null) ? token.getPosition() : null;
     }
     
     /**
      * Returns the end token for this structure 
      * @see openscriptures.text.Structure#getEndToken()
      */
     @Transient
     public Token getEndToken() {
         if (this.endTokenPosition == null) 
             return null;
         
         int pos = endTokenPosition.intValue();
         if ((endToken == null) || (endToken.getPosition() != pos))
                 endToken = getToken(pos);
         
         return endToken;
     }
     
     /**
      * Sets the end token for this structure.
      * 
      * @param token The token to set
      * 
      * @throws UnsupportedOperationException If this operation is not implemented for a 
      *         particular structure (e.g. for a structure accessed via a REST API that 
      *         doesn't support updates).
      * @throws InvalidTokenException If the supplied token is invalid. This might be because
      *         the token's work does not match this structure's work or because the token 
      *         does not occur after the start token.
      *         
      */
     public void setEndToken(Token token) 
     throws UnsupportedOperationException, InvalidTokenException {
         this.endToken = null;
         
         checkOrder(this.getStartToken(), token);
         if (token != null) 
             checkWork(token);
                 
         this.endTokenPosition = (token != null) ? token.getPosition() : null;
     }
     
     /**
      * Sets the start and end tokens for this structure.
      * 
      * @param start The start token to set
      * @param end The end token to set
      * 
      * @throws UnsupportedOperationException If this operation is not implemented for a 
      *         particular structure (e.g. for a structure accessed via a REST API that 
      *         doesn't support updates).
      * @throws InvalidTokenException If the supplied token is invalid. This might be because
      *         the token's work does not match this structure's work or because the start 
      *         token does not occur before the end token.
      *         
      */
     public void setTokens(Token start, Token end) 
     throws UnsupportedOperationException, InvalidTokenException {
         this.startToken = null;
         this.endToken = null;
         
         if (start == null) {
             this.startTokenPosition = null;
             this.endTokenPosition = null;
         }
         
         checkOrder(start, end);
         checkWork(start); 
         if (end != null) 
             checkWork(end);
         
         this.startTokenPosition = (start != null) ? start.getPosition() : null;
         this.endTokenPosition = (end != null) ? end.getPosition() : null;
     }
     
    
     
 //========================================================================================
 // METHODS FOR REPRESENTING ATTRIBUTES, CONTENT, AND HIERARCHICAL STRUCTURES
 //========================================================================================
     /*
      * 
      */
 	public String getAttribute(String name) {
 		StructureAttribute attr = attributes.get(name);
 		
 		return (attr == null) ? null : attr.getValue();
 	}
 
     /* (non-Javadoc)
      * @see openscriptures.text.Structure#listAttributes()
      */
     public Set<String> listAttributes() {
         return this.attributes.keySet();
     }
     
     /**
      * 
      * @param name
      * @param value
      * @return
      */
     public String setAttribute(String name, String value) {
         StructureAttribute previous = 
             attributes.put(name, new StructureAttribute(name, value));
         
         return (previous != null) ? previous.getValue() : null;
     }
     
     /**
      * Retrieves the parent of this structure. This along with {@see #getChildren()} 
      * allows for the hierarchical nesting of structures. While the structures within
      * a text are not strictly or exclusively hierarcical, hierarchies do represent an 
      * important set of relationships between structures. For example, verses are not 
      * simply structures that happen to be found only within chapters, verses are 
      * specifically a sub-division
      *   
      * @return
      */
     @Transient
     public Structure getParent() { 
         return this.parent;
     }
     
     public void setParent(Structure parent) {
         // TODO find a way to check to see if structures are properly nested
         if (this.parent != null) {
             this.parent.children.remove(this);
         }
         
         this.parent = parent;
         if (parent != null)
             parent.children.add(this);
     }
     
     /**
      * 
      */
     public List<Structure> listChildren() {
         return Collections.unmodifiableList((List<? extends Structure>)children);
     }
     
    
     //========================================================================================
     // UTILITY METHODS 
     //========================================================================================
    
     public boolean equals(Object obj) {
         Structure s = (Structure)obj;
         return (s.getUUID().equals(this.getUUID()));
     }
     
     /** 
      * Two structures are equivalent if they mark the same same span of tokens, 
      * in the same text and have the same name. Equivalent structures may exist, for 
      * example, because two different repositories have marked the same structure in a
      * text (possibly with different attributes or other metadata). These structures
      * should be understood as being equivalent even though they have distinct propreties.
      *  
      * @param s
      * @return
      */
     public boolean equivalent(Structure s) {
         if (equals(s))
             return true;
         
         if (!s.getWorkUUID().equals(this.getWorkUUID()))
             return false;
         
         return (s.getStart() == this.getStart()) &&         
                (s.getEnd() == this.getEnd()) &&             
                s.getName().equals(this.getName());
     }
 }
