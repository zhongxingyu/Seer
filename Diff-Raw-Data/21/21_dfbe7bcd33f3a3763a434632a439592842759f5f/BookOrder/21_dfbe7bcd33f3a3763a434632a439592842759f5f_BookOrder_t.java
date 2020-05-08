 /**
  * 
  */
 package org.idch.bible.ref;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Pattern;
 
 /**
  * Represents a particularly ordering of the books of the Bible. For now, this is a 
  * temporary holding class until I do something with it that makes sense. 
  * 
  * Note the work done by John Hunt at https://github.com/openscriptures/BibleOrgSys
  * 
  * @author Neal Audenaert
  */
 public class BookOrder {
 	
 	public static final BookOrder KJV = new BookOrder();
 
 	@SuppressWarnings("unused")
     private SystemDescription information;
 	
 	public List<String> bookIds = new ArrayList<String>();
 	public Map<String, BookName> bks = new HashMap<String, BookName>();
 	
 	
 	/**
 	 * Temporary constructor that builds a book order list using the KJV sequence.
 	 */
 	public BookOrder() {
 	    // OLD TESTAMENT
 		define("Gen", "Gensis"); 
 		define("Exod", "Exodus");
 		define("Lev", "Leviticus"); 
 		define("Num", "Numbers");
 		define("Deut", "Deuteronomy"); 
 		define("Josh", "Joshua");
 		define("Judg", "Judges");
 		define("Ruth", "Ruth");
 		define("1Sam", "1 Samuel"); 
 		define("2Sam", "2 Samuel");
 		define("1Kgs", "1 Kings");
 		define("2Kgs", "2 Kings");
 		define("1Chr", "1 Chronicles"); 
 		define("2Chr", "2 Chronicles"); 
 		define("Ezra", "Ezra"); 
 		define("Neh", "Nehemiah"); 
 		define("Esth", "Esther"); 
 		define("Job", "Job"); 
 		define("Ps", "Psalms"); 
 		define("Prov", "Proverbs"); 
 		define("Eccl", "Ecclesiastes"); 
 		define("Song", "Song of Songs"); 
 		define("Isa", "Isaiah"); 
 		define("Jer", "Jeremiah"); 
 		define("Lam", "Lamentations"); 
 		define("Ezek", "Ezekiel"); 
 		define("Dan", "Daniel"); 
 		define("Hos", "Hosea"); 
 		define("Joel", "Joel"); 
 		define("Amos", "Amos"); 
 		define("Obad", "Obadiah"); 
 		define("Jonah", "Jonah"); 
 		define("Mic", "Micah"); 
 		define("Nah", "Nahum"); 
 		define("Hab", "Habakkuk"); 
 		define("Zeph", "Zephaniah"); 
 		define("Hag", "Haggai"); 
 		define("Zech", "Zechariah"); 
 		define("Mal", "Malachi");
 		
 		// NEW TESTAMENT
 		define("Matt", "Matthew"); 
 		define("Mark", "Mark"); 
 		define("Luke", "Luke"); 
 		define("John", "John"); 
 		define("Acts", "Acts"); 
 		define("Rom", "Romans"); 
 		define("1Cor", "1 Corinthians"); 
 		define("2Cor", "2 Corinthians"); 
 		define("Gal", "Galations"); 
 		define("Eph", "Ephesians"); 
 		define("Phil", "Philippians"); 
 		define("Col", "Colossians"); 
 		define("1Thess", "1 Thessalonians"); 
 		define("2Thess", "2 Thessalonians"); 
 		define("1Tim", "1 Timothy"); 
 		define("2Tim", "2 Timothy"); 
 		define("Titus", "Titus"); 
 		define("Phlm", "Philemon"); 
 		define("Heb", "Hebrews"); 
 		define("Jas", "Jamess"); 
 		define("1Pet", "1 Peter"); 
 		define("2Pet", "2 Peter"); 
 		define("1John", "1 John"); 
 		define("2John", "2 John"); 
 		define("3John", "3 John"); 
 		define("Jude", "Jude"); 
 		define("Rev", "Revelation");
 		
 		// DEUTEROCANONICAL
 		define("Bar", "Baruch"); 
 		define("AddDan", "Additions to Daniel"); 
 		define("PrAzar", "Prayer of Azariah"); 
 		define("Bel", "Bel and the Dragon"); 
 		define("SgThree", "Song of the Three Holy Children"); 
 		define("Sus", "Susanna");
 		define("1Esd", "1 Esdras"); 
 		define("2Esd", "2 Esdras"); 
 		define("AddEsth", "Additions to Esther"); 
 		define("EpJer", "Epistle of Jeremiah"); 
 		define("Jdt", "Judith");
 		define("1Macc", "1 Maccabees"); 
 		define("2Macc", "2 Maccabees"); 
 		define("3Macc", "3 Maccabees"); 
 		define("4Macc", "4 Maccabees"); 
 		define("PrMan", "Prayer of Manasseh"); 
 		define("Sir", "Sirach"); 
 		define("Tob", "Tobit"); 
 		define("Wis", "Wisdom");
 	}
 	
 	void define(String id, String name) {
 	    BookName bk = new BookName(id, name);
 	    bookIds.add(bk.getId());
 	    bks.put(id, bk);
 	}
 	
	@SuppressWarnings("unused")
    private void generateRegEx() {
 	    Map<String, Pattern> patterns = new HashMap<String, Pattern>();
 	    
 //	    patterns.put("1Chr",    Pattern.compile("1 Chronicles",             Pattern.CASE_INSENSITIVE));  
 //	    patterns.put("1Kgs",    Pattern.compile("1 Kings",                  Pattern.CASE_INSENSITIVE)); 
 //	    patterns.put("1Sam",    Pattern.compile("1 Samuel",                 Pattern.CASE_INSENSITIVE));  
 //	    patterns.put("2Chr",    Pattern.compile("2 Chronicles",             Pattern.CASE_INSENSITIVE));  
 //	    patterns.put("2Kgs",    Pattern.compile("2 Kings",                  Pattern.CASE_INSENSITIVE)); 
 //	    patterns.put("2Sam",    Pattern.compile("2 Samuel",                 Pattern.CASE_INSENSITIVE));
 //	    
 //	    patterns.put("Amos",    Pattern.compile("Amos",                     Pattern.CASE_INSENSITIVE));  
 //	    patterns.put("Dan",     Pattern.compile("Daniel",                   Pattern.CASE_INSENSITIVE));  
 //	    patterns.put("Deut",    Pattern.compile("Deuteronomy",              Pattern.CASE_INSENSITIVE));  
 //	    patterns.put("Eccl",    Pattern.compile("Ecclesiastes",             Pattern.CASE_INSENSITIVE));  
 //	    patterns.put("Esth",    Pattern.compile("Esther",                   Pattern.CASE_INSENSITIVE));  
 //	    patterns.put("Exod",    Pattern.compile("Exodus",                   Pattern.CASE_INSENSITIVE)); 
 //	    patterns.put("Ezek",    Pattern.compile("Ezekiel",                  Pattern.CASE_INSENSITIVE));  
 //	    patterns.put("Ezra",    Pattern.compile("Ezra",                     Pattern.CASE_INSENSITIVE));  
 //	    patterns.put("Gen",     Pattern.compile("Gensis",                   Pattern.CASE_INSENSITIVE));  
 //	    patterns.put("Hab",     Pattern.compile("Habakkuk",                 Pattern.CASE_INSENSITIVE));  
 //	    patterns.put("Hag",     Pattern.compile("Haggai",                   Pattern.CASE_INSENSITIVE));  
 //	    patterns.put("Hos",     Pattern.compile("Hosea",                    Pattern.CASE_INSENSITIVE));  
 //	    patterns.put("Isa",     Pattern.compile("Isaiah",                   Pattern.CASE_INSENSITIVE));  
 //      patterns.put("Josh",    Pattern.compile("Joshua",                   Pattern.CASE_INSENSITIVE)); 
 //      patterns.put("Job",     Pattern.compile("Job",                      Pattern.CASE_INSENSITIVE));  
 //      patterns.put("Joel",    Pattern.compile("Joel",                     Pattern.CASE_INSENSITIVE));  
 //      patterns.put("Jonah",   Pattern.compile("Jonah",                    Pattern.CASE_INSENSITIVE));  
 //      patterns.put("Judg",    Pattern.compile("Judges",                   Pattern.CASE_INSENSITIVE)); 
 //      patterns.put("Jer",     Pattern.compile("Jeremiah",                 Pattern.CASE_INSENSITIVE));  
 //      patterns.put("Lam",     Pattern.compile("Lamentations",             Pattern.CASE_INSENSITIVE));  
 //      patterns.put("Lev",     Pattern.compile("Leviticus",                Pattern.CASE_INSENSITIVE));  
 //      patterns.put("Mal",     Pattern.compile("Malachi",                  Pattern.CASE_INSENSITIVE)); 
 //      patterns.put("Mic",     Pattern.compile("Micah",                    Pattern.CASE_INSENSITIVE));  
 //      patterns.put("Nah",     Pattern.compile("Nahum",                    Pattern.CASE_INSENSITIVE));  
 //      patterns.put("Neh",     Pattern.compile("Nehemiah",                 Pattern.CASE_INSENSITIVE));  
 //      patterns.put("Num",     Pattern.compile("Numbers",                  Pattern.CASE_INSENSITIVE)); 
 //      patterns.put("Obad",    Pattern.compile("Obadiah",                  Pattern.CASE_INSENSITIVE));  
 //      patterns.put("Prov",    Pattern.compile("Proverbs",                 Pattern.CASE_INSENSITIVE));  
 //      patterns.put("Ruth",    Pattern.compile("Ruth",                     Pattern.CASE_INSENSITIVE)); 
 //      patterns.put("Ps",      Pattern.compile("Psalms",                   Pattern.CASE_INSENSITIVE));  
 //      patterns.put("Song",    Pattern.compile("Song of Songs",            Pattern.CASE_INSENSITIVE));  
 //      patterns.put("Zeph",    Pattern.compile("Zephaniah",                Pattern.CASE_INSENSITIVE));  
 //      patterns.put("Zech",    Pattern.compile("Zechariah",                Pattern.CASE_INSENSITIVE));  
         
         patterns.put("1Cor",    Pattern.compile("^[1I]\\s*C\\D*",           Pattern.CASE_INSENSITIVE)); 
         patterns.put("1John",   Pattern.compile("^[1I]\\s*J\\D*",           Pattern.CASE_INSENSITIVE));
         patterns.put("1Pet",    Pattern.compile("^[1I]\\s*P\\D*",           Pattern.CASE_INSENSITIVE));
         patterns.put("1Thess",  Pattern.compile("^[1I]\\s*Th\\D*",          Pattern.CASE_INSENSITIVE));
         patterns.put("1Tim",    Pattern.compile("^[1I]\\s*T[im]\\D*",       Pattern.CASE_INSENSITIVE));
         patterns.put("2Cor",    Pattern.compile("^(?:2|II)\\s*C\\D*",       Pattern.CASE_INSENSITIVE));
         patterns.put("2John",   Pattern.compile("^(?:2|II)\\s*J\\D*",       Pattern.CASE_INSENSITIVE));
         patterns.put("2Pet",    Pattern.compile("^(?:2|II)\\s*P\\D*",       Pattern.CASE_INSENSITIVE)); 
         patterns.put("2Thess",  Pattern.compile("^(?:2|II)\\s*Th\\D*",      Pattern.CASE_INSENSITIVE)); 
         patterns.put("2Tim",    Pattern.compile("^(?:2|II)\\s*T[im]\\D*",   Pattern.CASE_INSENSITIVE));
         patterns.put("3John",   Pattern.compile("^(?:3|III)\\s*J\\D*",      Pattern.CASE_INSENSITIVE)); 
         patterns.put("Acts",    Pattern.compile("^A\\D*",                   Pattern.CASE_INSENSITIVE)); 
         patterns.put("Col",     Pattern.compile("^C\\D*",                   Pattern.CASE_INSENSITIVE)); 
         patterns.put("Eph",     Pattern.compile("^E\\D*",                   Pattern.CASE_INSENSITIVE)); 
         patterns.put("Gal",     Pattern.compile("^G\\D*",                   Pattern.CASE_INSENSITIVE)); 
         patterns.put("Heb",     Pattern.compile("^H\\D*",                   Pattern.CASE_INSENSITIVE)); 
         patterns.put("Jas",     Pattern.compile("^J[mas]\\D*",              Pattern.CASE_INSENSITIVE));
         patterns.put("John",    Pattern.compile("^Jo?[hn]\\D*",             Pattern.CASE_INSENSITIVE));
         patterns.put("Jude",    Pattern.compile("^Ju\\D*",                  Pattern.CASE_INSENSITIVE)); 
         patterns.put("Luke",    Pattern.compile("^L\\D*",                   Pattern.CASE_INSENSITIVE)); 
         patterns.put("Mark",    Pattern.compile("^Ma?[rk]\\D*",             Pattern.CASE_INSENSITIVE));
         patterns.put("Matt",    Pattern.compile("^Ma?t?t\\D*",              Pattern.CASE_INSENSITIVE));
         patterns.put("Phlm",    Pattern.compile("^Ph\\w*m\\D*",             Pattern.CASE_INSENSITIVE));
         patterns.put("Phil",    Pattern.compile("^Ph(il|\\w*p)\\D*",        Pattern.CASE_INSENSITIVE));
         patterns.put("Phil",    Pattern.compile("^Ph\\D*",                  Pattern.CASE_INSENSITIVE));
         patterns.put("Rev",     Pattern.compile("^R[ev]\\D*",               Pattern.CASE_INSENSITIVE));
         patterns.put("Rom",     Pattern.compile("^R[om]\\D*",               Pattern.CASE_INSENSITIVE));
         patterns.put("Titus",   Pattern.compile("^T\\D*",                   Pattern.CASE_INSENSITIVE));
         
 //        Genesis Gen, Ge, Gn
 //        Exodus  Exo, Ex, Exod
 //        Leviticus   Lev, Le, Lv
 //        Numbers Num, Nu, Nm, Nb
 //        Deuteronomy Deut, Dt
 //        Joshua  Josh, Jos, Jsh
 //        Judges  Judg, Jdg, Jg, Jdgs
 //        Ruth    Rth, Ru
 //        1 Samuel    1 Sam, 1 Sa, 1Samuel, 1S, I Sa, 1 Sm, 1Sa, I Sam, 1Sam, I Samuel, 1st Samuel, First Samuel
 //        2 Samuel    2 Sam, 2 Sa, 2S, II Sa, 2 Sm, 2Sa, II Sam, 2Sam, II Samuel, 2Samuel, 2nd Samuel, Second Samuel
 //        1 Kings 1 Kgs, 1 Ki, 1K, I Kgs, 1Kgs, I Ki, 1Ki, I Kings, 1Kings, 1st Kgs, 1st Kings, First Kings, First Kgs, 1Kin
 //        2 Kings 2 Kgs, 2 Ki, 2K, II Kgs, 2Kgs, II Ki, 2Ki, II Kings, 2Kings, 2nd Kgs, 2nd Kings, Second Kings, Second Kgs, 2Kin
 //        1 Chronicles    1 Chron, 1 Ch, I Ch, 1Ch, 1 Chr, I Chr, 1Chr, I Chron, 1Chron, I Chronicles, 1Chronicles, 1st Chronicles, First Chronicles
 //        2 Chronicles    2 Chron, 2 Ch, II Ch, 2Ch, II Chr, 2Chr, II Chron, 2Chron, II Chronicles, 2Chronicles, 2nd Chronicles, Second Chronicles
 //        Ezra    Ezra, Ezr
 //        Nehemiah    Neh, Ne
 //        Esther  Esth, Es
 //        Job Job, Job, Jb
 //        Psalm   Pslm, Ps, Psalms, Psa, Psm, Pss
 //        Proverbs    Prov, Pr, Prv
 //        Ecclesiastes    Eccles, Ec, Qoh, Qoheleth
 //        Song of Solomon Song, So, Canticle of Canticles, Canticles, Song of Songs, SOS
 //        Isaiah  Isa, Is
 //        Jeremiah    Jer, Je, Jr
 //        Lamentations    Lam, La
 //        Ezekiel Ezek, Eze, Ezk
 //        Daniel  Dan, Da, Dn
 //        Hosea   Hos, Ho
 //        Joel    Joel, Joe, Jl
 //        Amos    Amos, Am
 //        Obadiah Obad, Ob
 //        Jonah   Jnh, Jon
 //        Micah   Micah, Mic
 //        Nahum   Nah, Na
 //        Habakkuk    Hab, Hab
 //        Zephaniah   Zeph, Zep, Zp
 //        Haggai  Haggai, Hag, Hg
 //        Zechariah   Zech, Zec, Zc
 //        Malachi Mal, Mal, Ml
 //        Tobit   Tobit, Tob, Tb
 //        Judith  Jdth, Jdt, Jth
 //        Additions to Esther Add Esth, Add Es, Rest of Esther, The Rest of Esther, AEs, AddEsth
 //        Wisdom of Solomon   Wisd of Sol, Wis, Ws, Wisdom
 //        Sirach  Sirach, Sir, Ecclesiasticus, Ecclus
 //        Baruch  Baruch, Bar
 //        Letter of Jeremiah  Let Jer, Let Jer, LJe, Ltr Jer
 //        Song of Three Youths    Song of Three, Song Thr, The Song of Three Youths, Pr Az, Prayer of Azariah, Azariah, The Song of the Three Holy Children, The Song of Three Jews, Song of the Three Holy Children, Song of Thr, Song of Three Children, Song of Three Jews
 //        Susanna Susanna, Sus
 //        Bel and the Dragon  Bel, Bel
 //        1 Maccabees 1 Macc, 1 Mac, 1M, I Ma, 1Ma, I Mac, 1Mac, I Macc, 1Macc, I Maccabees, 1Maccabees, 1st Maccabees, First Maccabees
 //        2 Maccabees 2 Macc, 2 Mac, 2M, II Ma, 2Ma, II Mac, 2Mac, II Macc, 2Macc, II Maccabees, 2Maccabees, 2nd Maccabees, Second Maccabees
 //        1 Esdras    1 Esdr, 1 Esd, I Es, 1Es, I Esd, 1Esd, I Esdr, 1Esdr, I Esdras, 1Esdras, 1st Esdras, First Esdras
 //        Prayer of Manasseh  Pr of Man, Pr Man, PMa, Prayer of Manasses
 //        Additional Psalm    Add Psalm, Add Ps
 //        3 Maccabees 3 Macc, 3 Mac, III Ma, 3Ma, III Mac, 3Mac, III Macc, 3Macc, III Maccabees, 3rd Maccabees, Third Maccabees
 //        2 Esdras    2 Esdr, 2 Esd, II Es, 2Es, II Esd, 2Esd, II Esdr, 2Esdr, II Esdras, 2Esdras, 2nd Esdras, Second Esdras
 //        4 Maccabees 4 Macc, 4 Mac, IV Ma, 4Ma, IV Mac, 4Mac, IV Macc, 4Macc, IV Maccabees, IIII Maccabees, 4Maccabees, 4th Maccabees, Fourth Maccabees
 //        Ode Ode, Ode
 //        Psalms of Solomon   Ps Solomon, Ps Sol, Psalms Solomon, PsSol
 //        Epistle to the Laodiceans   Laodiceans, Laod, Ep Laod, Epist Laodiceans, Epistle Laodiceans, Epistle to Laodiceans
 //        Matthew Matt, Mt
 //        Mark    Mrk, Mk, Mr
 //        Luke    Luk, Lk
 //        John    John, Jn, Jhn
 //        Acts    Acts, Ac
 //        Romans  Rom, Ro, Rm
 //        1 Corinthians   1 Cor, 1 Co, I Co, 1Co, I Cor, 1Cor, I Corinthians, 1Corinthians, 1st Corinthians, First Corinthians
 //        2 Corinthians   2 Cor, 2 Co, II Co, 2Co, II Cor, 2Cor, II Corinthians, 2Corinthians, 2nd Corinthians, Second Corinthians
 //        Galatians   Gal, Ga
 //        Ephesians   Ephes, Eph
 //        Philippians Phil, Php
 //        Colossians  Col, Col
 //        1 Thessalonians 1 Thess, 1 Th, I Th, 1Th, I Thes, 1Thes, I Thess, 1Thess, I Thessalonians, 1Thessalonians, 1st Thessalonians, First Thessalonians
 //        2 Thessalonians 2 Thess, 2 Th, II Th, 2Th, II Thes, 2Thes, II Thess, 2Thess, II Thessalonians, 2Thessalonians, 2nd Thessalonians, Second Thessalonians
 //        1 Timothy   1 Tim, 1 Ti, I Ti, 1Ti, I Tim, 1Tim, I Timothy, 1Timothy, 1st Timothy, First Timothy
 //        2 Timothy   2 Tim, 2 Ti, II Ti, 2Ti, II Tim, 2Tim, II Timothy, 2Timothy, 2nd Timothy, Second Timothy
 //        Titus   Titus, Tit
 //        Philemon    Philem, Phm
 //        Hebrews Hebrews, Heb
 //        James   James, Jas, Jm
 //        1 Peter 1 Pet, 1 Pe, I Pe, 1Pe, I Pet, 1Pet, I Pt, 1 Pt, 1Pt, I Peter, 1Peter, 1st Peter, First Peter
 //        2 Peter 2 Pet, 2 Pe, II Pe, 2Pe, II Pet, 2Pet, II Pt, 2 Pt, 2Pt, II Peter, 2Peter, 2nd Peter, Second Peter
 //        1 John  1 John, 1 Jn, I Jn, 1Jn, I Jo, 1Jo, I Joh, 1Joh, I Jhn, 1 Jhn, 1Jhn, I John, 1John, 1st John, First John
 //        2 John  2 John, 2 Jn, II Jn, 2Jn, II Jo, 2Jo, II Joh, 2Joh, II Jhn, 2 Jhn, 2Jhn, II John, 2John, 2nd John, Second John
 //        3 John  3 John, 3 Jn, III Jn, 3Jn, III Jo, 3Jo, III Joh, 3Joh, III Jhn, 3 Jhn, 3Jhn, III John, 3John, 3rd John, Third John
 //        Jude    Jude, Jud
 //        Revelation  Rev, Re, The Revelation
         
         
 	}
 	
 	public BookOrder(File config) {
 		
 	}
 	
 	
 	public void parseReference() {
 	    
 	}
 	/** 
 	 * Returns the index of the supplied book name.
 	 * 
 	 * @param id The book name to look for.
 	 * @return The index of the indicated book or -1 if there is no such book.
 	 */
 	public int indexOf(String id) {
 	    return bookIds.indexOf(id);
 	}
 	
 	private void checkIx(int ix) {
 	    assert (ix < bks.size()) && ix >= 0;
         
         if ((ix >= bks.size()) && (ix < 0)) {
             // In case we want to offer more information later. In contrast to returning 
             // null, this enforces a fail fast policy to support easier error finding.
             throw new ArrayIndexOutOfBoundsException();
         }
 	}
 	
 	public String getId(int ix) {
 	    checkIx(ix);
 	    return bks.get(bookIds.get(ix)).getId();
 	}
 	
 	public String getName(int ix) {
 	    checkIx(ix);
 	    return bks.get(bookIds.get(ix)).getName();
 	}
 	
 	public boolean contains(String id) {
 		return bks.containsKey(id);
 	}
 	
 	public int size() {
 		return this.bks.size();
 	}
 	
 	public List<String> listBooks() {
 		return Collections.unmodifiableList(this.bookIds);
 	}
 	
 	
 	public class Book {
 	    
 	}
 }
