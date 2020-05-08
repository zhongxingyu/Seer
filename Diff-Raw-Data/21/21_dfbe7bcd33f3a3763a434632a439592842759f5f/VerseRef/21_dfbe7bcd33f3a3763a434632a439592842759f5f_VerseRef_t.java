 /**
  * 
  */
 package org.idch.bible.ref;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 
 /**
  * Maintains a reference to a single verse or to a point within a verse. 
  *  
  * The OSIS specification treats reference identifiers as a segment of '<tt>.</tt>' separated 
  * identifiers of increasingly narrowing scope. This allows the OSIS scheme to represent
  * not only Bibles but a variety different types of works. As a result, however, the semantics 
  * of different segments are not well defined. <tt>VerseRef</tt>, in contrast, emphasizes 
  * access by traditional verse semantics (book of the Bible, chapter, verse, and extension). 
  * Thus, <tt>VerseRef</tt> provides a partial implementation of the OSIS specification 
  * designed to simplify the common task of working with scripture references.
  * 
  * TODO This needs to be brought up to speed to parse full blown OSIS identifiers.
  *      For some detail, see: http://groups.google.com/group/openscriptures/msg/4fb744efb27c1a41?pli=1
  *      
  *      The current implementation works on verses represented in the form of Matt.3.4, where
  *      the names are book names that can be resolved using the supplied BookOrder. Chapters
  *      and verses are assumed to be numeric.
  *      
  * TODO Implement support for grains (e.g., Gen.1.1@cp[8] or Gen.1.1@s[beginning]).
  *  
  * 
  * @author Neal Audenaert
  */
 public class VerseRef extends Passage {
     private static final Logger LOGGER = Logger.getLogger(VerseRef.class);
     
 	private Integer book = null;
 	private Integer chapter = null;
 	private Integer verse = null;
 	
 	/** The sub-verse level extension, for example, in <tt>Matt.2.10!a</tt>, the 
 	 *  extension is <tt>a</tt>. 
 	 */
 	private String ext = null;
 	
 	// TODO next/prev VERSE, CHAPTER, BOOK
 	// TODO increment/decrement VERSE, CHAPTER, BOOK by XX
 	// TODO go up (verse to chapter, chapter to book, book to testament)
 	// TODO get path (NT > Matt > Chapter 3 > Verse 6 > a)
 	// TODO Iterator<VerseRef> (passage)
 	
 //======================================================================================
 // CONSTUCTORS
 //======================================================================================	
 	public VerseRef(String ref) throws InvalidReferenceException {
 		this(BookOrder.KJV, ref);
 	}
 	
 	public VerseRef(BookOrder order, String ref) throws InvalidReferenceException {
 		super(order);
 		this.parseCanonicalReference(ref, false /* defualtToChapter */);
 	}
 	
 	/**
 	 * Used to resolve a partial reference within the context of another verse reference.
 	 * If, for example, when parsing a verse range like <tt>Matt.3.3-4</tt>, the first 
 	 * verse in the range parses to <tt>Matt.3.3</tt>, while the second verse, 
 	 * <tt>4</tt> should be interpreted relative to the first <tt>Matt.3.4</tt>.
 	 *  
 	 * @param order
 	 * @param context
 	 * @param ref
 	 */
 	public VerseRef(VerseRef context, String ref) {
 		super(context.getBookOrder());
 		this.parseCanonicalReference(ref, !context.isVerseSpecified() /* defualtToChapter */);
 		
 		// We'll attempt to inherit book and verse references, 
 		if (this.book == null) 
 			this.book = context.getBookIndex();
 		else return;				// short ciruit once a higher order field is specified
 		
 		if (this.chapter == null)
 			this.chapter = context.getChapter();
 		else return;
 		
 		// but not verse and extension for now
 	}
 	
 	public VerseRef(BookOrder order, Integer book, Integer chapter, Integer vs, String ext) {
 		super(order);
 		this.book = book;
 		this.chapter = chapter;
 		this.verse = vs;
 		this.ext = ext;
 	}
 	
 	//======================================================================================
 	// PARSING METHODS
 	//======================================================================================
 	private String parseGrain(String ref) {
 		String remainder = ref, grain = null;
 		int ix = ref.lastIndexOf('@');
 		if (ix >= 0) {
 			assert ix < ref.length() : "Bad Refernce Format: trailing '@'";
 			
 			grain = ref.substring(ix + 1);
 			remainder = ref.substring(0, ix);
 			
 			LOGGER.debug("Extracted grain '" + grain + "' from referecne '" + ref + "'.");
 		}
 		
 		return remainder;
 	}
 	
 	private String parseExtension(String ref) {
 		String remainder = ref;
 		int ix = ref.lastIndexOf('!');
 		if (ix >= 0) {
 			assert ix < ref.length() : "Bad Refernce Format: trailing '!'";
 			
 			this.ext = ref.substring(ix + 1);
 			remainder = ref.substring(0, ix);
 			LOGGER.debug("Extracted extension '" + this.ext + "' from referecne '" + ref + "'.");
 		}
 		
 		return remainder;
 		
 	}
 	
 
 	/**
 	 * Parses a canonical verse reference. A canonical reference is one with a book name,
 	 * chapter and verse, separated by periods Matt.3.2!a@w[3]
 	 * 
 	 * DESCRIBE THIS HERE
 	 * 
 	 * @param ref The string-valued verse reference to parse.
 	 * @param defaultToChapter if only a single numeric value is provided, indicates whether
 	 * 		that value should be interpreted as a chapter or as a verse.
 	 * @throws InvalidReferenceException
 	 */
 	private void parseCanonicalReference(String ref, boolean defaultToChapter) 
 	throws InvalidReferenceException {
 		// NOTE Consider using RegEx based parsing. 
 		if (StringUtils.isBlank(ref)) {
 			throw new InvalidReferenceException("Blank reference", ref);
 		}
 		
 		ref = this.parseGrain(ref);
 		ref = this.parseExtension(ref);
 		
 		String[] segments = ref.split("\\.");
 		int len = segments.length;
 		
 		if (len == 0) 
 			return;		// No segments to process
 		
 		if (!StringUtils.isNumeric(segments[0])) {
 			// first segment is a book identifier, process left to right
 			this.book = order.indexOf(segments[0]);
 			this.chapter = (len > 1) ? Integer.parseInt(segments[1]) : null;
 			this.verse = (len > 2) ? Integer.parseInt(segments[2]) : null;
 			
 			if (len > 3) {
 			    LOGGER.warn("Bad verse reference (" + ref + "): trailing segments.");
 			}
 			
 		} else {
 			// first segment is not a book, process right to left
 			if (len == 1) {		// just a verse
 				if (defaultToChapter)
 					this.chapter = Integer.parseInt(segments[0]);
 				else
 					this.verse = Integer.parseInt(segments[0]);
 			} else {			// chapter.verse
 				this.chapter = Integer.parseInt(segments[0]);
 				this.verse = Integer.parseInt(segments[1]);
 			}
 			
 			if (len > 2) {
 			    LOGGER.warn("Bad verse reference (" + ref + "): too many numeric " +
                       "identifiers. Expected reference in the form of 'ch.vs'.");
 			}
 		}
 		
 		LOGGER.debug("Book Index: " + this.book);
 		LOGGER.debug("Chapter:    " + this.chapter);
 		LOGGER.debug("Verse:      " + this.verse);
 		
 	}
 	
 	//======================================================================================
 	// GETTERS AND SETTERS
 	//======================================================================================
 	
 	public VerseRef getFirst() {
 		return this;
 	}
 	
 	public VerseRef getLast() {
 		return this;
 	}
 	
 	/**
 	 * Returns the index of the book of the Bible relative to the <tt>BookOrder</tt> scheme 
 	 * used by this reference.
 	 *  
 	 * @return the index of this book or <tt>null</tt> if no book is specified for this 
 	 * 		reference
 	 */
 	public Integer getBookIndex() {
 		return this.book;
 	}
 	
 	
 	/** 
 	 * Returns the short or canonical name used to identify this book in the given 
 	 * <tt>BookOrder</tt>. This is typically the osisId of the book.
 	 * 
 	 * @return
 	 */
 	public String getBookIdentifier() {
	    return this.isBookSpecified() ? order.getId(book) : null;
 	}
 	
 	/**
 	 * Returns the common name of the book as defined by the associated book order.
 	 * @return
 	 */
 	public String getBookName() {
		return this.isBookSpecified() ? order.getName(book) : null;
 	}
 	
 	public Integer getChapter() {
 		return this.chapter;
 	}
 	
 	public Integer getVerse() {
 		return this.verse;
 	}
 	
 	public String getExtension() {
 		return this.ext;
 	}
 	
 	/**
 	 * Indicates whether the book is specified for this reference.
 	 * @return <tt>true</tt> if the book is specified.
 	 */
 	public boolean isBookSpecified() {
		return (this.book != null) && (this.book >= 0) && (this.book < this.order.size());
 	}
 	
 	/**
 	 * Indicates whether the chapter is specified for this reference.
 	 * @return <tt>true</tt> if the chapter is specified.
 	 */
 	public boolean isChapterSpecified() {
 		return this.chapter != null;
 	}
 	
 	/**
 	 * Indicates whether the verse is specified for this reference.
 	 * @return <tt>true</tt> if the verse is specified.
 	 */
 	public boolean isVerseSpecified() {
 		return this.verse != null;
 	}
 	
 	//======================================================================================
 	// UTILITY METHODS
 	//======================================================================================
 	
 	/**
 	 * Checks to ensure that this object refers to a valid verse.
 	 * @return
 	 */
 	public boolean isValid() {
 		// TODO right now, this just checks to see if we know about the verse. It 
 		//      needs to be extended to see if the chapter and verse are valid for the 
 		//		give book according to the specified reference system. 
 		
 		// make sure that we have a valid book, given our reference system
		if (this.isBookSpecified()) {
 			return false;
 		}
 		
 		return true;
 	}
 	
 	private int compareParts(Integer a, Integer b) {
         if ((a != null) && (b != null)) {
             int ch = a - b;
             return ch;
         } else {
             return (a == b) ? 0 
                     : (a == null) ? -1 : 1;
         }
     }
     
     public int compareTo(Passage passage) {
         // TODO need to make sure that we're using the same book order.
         //      figure out how to order them if we aren't.
         // TODO possible null pointer errors
         
         VerseRef a = this;
         VerseRef b = passage.getFirst();
         
         int result = compareParts(a.getBookIndex(), b.getBookIndex());
         if (result != 0) return result;
             
         result = compareParts(a.getChapter(), b.getChapter());
         if (result != 0) return result;
         
         result = compareParts(a.getVerse(), b.getVerse());
         if (result != 0) return result;
         
         
         String extA = a.getExtension();
         String extB = b.getExtension();
         
         extA = (extA == null) ? "" : extA.toLowerCase();
         extB = (extB == null) ? "" : extB.toLowerCase();
         
         return extA.compareTo(extB);
     }
     
     public boolean equals(Object obj) {
         return this.compareTo((VerseRef)obj) == 0;
     }
 
 	/**
 	 * Returns this verse reference as a canonically formatted string.
 	 */
 	public String toString() {
 		StringBuilder sb = new StringBuilder();
 		boolean hasContent = false;
 		
 		if (this.book != null && (this.book >= 0)) {
 			hasContent = true;
 			sb.append(order.getId(this.book));
 		}
 		
 		if (this.chapter != null) {
 			if (hasContent) sb.append(".");
 			sb.append(this.chapter);
 			hasContent = true;
 		}
 		
 		if (this.verse != null) {
 			if (hasContent) sb.append(".");
 			sb.append(this.verse);
 			hasContent = true;
 		}
 		
 		if (this.ext != null)
 			sb.append("!").append(this.ext);
 		
 		return sb.toString();
 	}
 }
