 /**
  * 
  */
 package openscriptures.text;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import openscriptures.utils.Language;
 
 /**
  * @author Neal Audenaert
  */
 public class WorkId {
 	// Most of these TODO's are not properly a part of the OSIS framework.
 	// TODO Need to implement lookup services that that will search for a work ID based on
 	//		its title, language or type. For example, return all works of type 'Bible'
 	// TODO Need to implement more full-featured descriptions. A work should have 
 	//		the title, copyright information, description, versification strategy, and 
 	//      perhaps other information about how to resolve references to it.
 	// TODO cross walk with FRBR.
 	//		I think we have work = type, expression = <lg, slug>, manifestation = <publisher, edition> 
 	
 	public static enum Type {
 		// XXX This seems like the wrong approach. If we need multiple types, we'll
 		//     need some serious refactoring of the methods for representing books, 
 		//	   book orders etc. 
 		
 		// TODO Should think about implementing something more akin to FRBR. 
 		//      Work corresponds to Bible , 
 		//		Expression is the translation/edition 
 		//		Manifestation is a particular edition/printing
 		//		Item matters for MSS and/or specific imports
 		
 		BIBLE ("Bible"),
 	    QURAN ("Quran"),
 	    MISHNAH ("Mishnah"),
 	    TALMUD ("Talmud"),
 	    BOOK_OF_MORMON ("BookOfMormon");
 		
 		public final String value;
 		
 		Type(String v) {
 			this.value = v;
 		}
 		
 		/**
 		 * Returns the appropriate enum instance for a string-valued type. Currently, this 
 		 * matches the lower-case value of the name.
 		 * 
 		 * @param value
 		 * @return
 		 */
 		public static Type find(String value) {
 			value = value.toLowerCase();
 			
 			for (Type t : Type.values()) {
 				if (t.value.toLowerCase().equals(value))
 					return t;
 			}
 			
 			return null;
 		}
 	}
 	
 //============================================================================================
 // MEMBER VARIABLES
 //============================================================================================	
 
	private Type   type = Type.BIBLE;
 	private Language language;
 	private String publisher;
 	private String name;
 	private String publicationDate;
	
 	/**
 	 * 
 	 * @param name
 	 * @param date
 	 */
 	public WorkId(String name, String date) {
 		this.name = name;
 		this.publicationDate = date;
 	}
 	
 	/**
 	 * 
 	 * @param type
 	 * @param lg
 	 * @param publisher
 	 * @param name
 	 * @param pubDate
 	 */
 	public WorkId(Type type, Language lg, String publisher, String name, String pubDate) {
 		this.type = type;
 		this.language = lg;
 		this.publisher = publisher;
 		this.name = name;
 		this.publicationDate = pubDate;
 	}
 	
 	/**
 	 * 
 	 * @param work
 	 */
 	public WorkId(String work) {
 		// parse
		String[] segments = work.split(".");
 		int ix = 0,			            // the index for the current segment 
 		    len = segments.length;		// the number of segments
 		
 		if (ix >= len) {
 			// BAD THINGS - this is probably an error. Should throw something.
 			return;
 		}
 		
		
 		// check to see if the current element is a work type
 		Type t = Type.find(segments[ix]); 
 		if (t != null) {
 			this.type = t;
 			ix++;
 			if (ix >= len) return;	// just a type identifier 		
 		}
 		
		
 		// check to see if the current element is a language type
 		if (segments[ix].matches("^[a-z]{2,3}$")) {
 			this.language = Language.lookup(segments[ix++]);
 			
 			if (ix >= len) return;
 		}
 		
 		List<String> slugs = new ArrayList<String>();
 		for (; ix < len; ix++) {
 			String segment = segments[ix];
 			
 			if (segment.matches("\\d{4}")) {
 				// TODO For now, we'll just use YYYY style dates. Need to extend this to
 				//		make it more robust.
 				this.publicationDate = segment;
 			} else {
 				slugs.add(segment);
 			}
 		}
 		
 		if (slugs.size() == 1) {
 			this.name = slugs.get(0);
 		} else if (slugs.size() == 2) {
 			this.publisher = slugs.get(0);
 			this.name = slugs.get(1);
 		} else if (slugs.size() != 0) {
 			// XXX bad news. Invalid OSIS Work ID. Unexpected fields.
 		}
 		
 		// TODO handle revision number, version number, and edition number?
 	}
 	
 	/** Returns the type of this work. */
 	public Type getType() {
 		return this.type;
 	}
 	
 	/** Returns the language of this work */
 	public Language getLanguage() {
 		return this.language;
 	}
 	
 	/** Returns the publisher of this work. */
 	public String getPublisher() {
 		return this.publisher;
 	}
 	
 	/** Returns the name of this work. */
 	public String getName() {
 		return this.name;
 	}
 
 	/** Returns the date this work was published. */
 	public String getPublicationDate() {
 		return this.publicationDate;
 	}
 	
 	/** Returns the properly formatted OSIS identifier for this work. */
 	public String toString() {
 		StringBuilder builder = new StringBuilder();
 
 		if (this.type != null) builder.append(type.value);
 		if (this.language != null) {
 			if (builder.length() > 0) builder.append(".");
 			builder.append(this.language.getCode());
 		}
 		
 		if (this.publisher != null) {
 			if (builder.length() > 0) builder.append(".");
 			builder.append(this.publisher);
 		}
 		
 		if (this.name != null) {
 			if (builder.length() > 0) builder.append(".");
 			builder.append(this.name);
 		}
 		
 		if (this.publicationDate != null) {
 			if (builder.length() > 0) builder.append(".");
 			builder.append(this.publicationDate);
 		}
 		
 		return builder.toString();
 	}
 }
