 package org.basex.fs.fsml.build;
 
 import static org.basex.fs.fsml.FSMLTokens.BASEXFS;
 import static org.basex.fs.fsml.FSMLTokens.DIR;
 import static org.basex.fs.fsml.FSMLTokens.EXCLUDE_PATTERN;
 import static org.basex.fs.fsml.FSMLTokens.FILE;
 import static org.basex.fs.fsml.FSMLTokens.FSML;
 import static org.basex.fs.fsml.FSMLTokens.FSML_VERSION;
 import static org.basex.fs.fsml.FSMLTokens.LINK;
 import static org.basex.fs.fsml.FSMLTokens.NAME;
 import static org.basex.fs.fsml.FSMLTokens.SOURCE;
 import static org.basex.fs.fsml.FSMLTokens.ST_ATIME;
 import static org.basex.fs.fsml.FSMLTokens.ST_CTIME;
 import static org.basex.fs.fsml.FSMLTokens.ST_GID;
 import static org.basex.fs.fsml.FSMLTokens.ST_MODE;
 import static org.basex.fs.fsml.FSMLTokens.ST_MTIME;
 import static org.basex.fs.fsml.FSMLTokens.ST_NLINK;
 import static org.basex.fs.fsml.FSMLTokens.ST_SIZE;
 import static org.basex.fs.fsml.FSMLTokens.ST_UID;
 import static org.basex.fs.fsml.FSMLTokens.SUFFIX;
 import static org.basex.fs.fsml.FSMLTokens.VERSION;
 import static org.basex.util.Token.token;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.regex.PatternSyntaxException;
 
 import org.basex.build.Builder;
 import org.basex.build.Parser;
 import org.basex.fs.fsml.FSMLUtils;
 import org.basex.io.IOFile;
 import org.basex.util.Atts;
 import org.basex.util.Util;
 
 /**
  * Traverse a file hierarchy from given entry point and XML representation using
  * FSML (Filesystem Markup Language) dialect.
  *
  * @author BaseX Team 2005-11, BSD License
  * @author Alexander Holupirek
  */
 public class FileHierarchyParser extends Parser {
 
  /** Prefix for debugging messages. */
  private String debugName = "[FileHierarchyParser] ";
   /** Reference to Builder instance. */
   private Builder builder;
   /** Builder properties. */
   private HashMap<String, String> props;
 
   /**
    * Constructor.
    *
    * @param String path parsing process shall start from
    * @param Parsing user properties
    */
   public FileHierarchyParser(final IOFile path, final HashMap<String, String> p)
       throws IOException {
     super(path);
     props = p;
   }
 
   @Override
   public void parse(final Builder b) throws IOException {
     builder = b;
     IOFile f = (IOFile) src;
 
     builder.startDoc(BASEXFS);
     builder.startElem(FSML, attsFSML());
     if (!excludePath(f)) {
       if (f.isDir())
         parseDirectory(f);
       else
         parseFile(f);
     }
     builder.endElem();
     builder.endDoc();
   }
 
   /**
    * Parses directory and builds FSML directory entry.
    *
    * @param io the directory to parse
    * @throws IOException
    */
   private void parseDirectory(final IOFile io) throws IOException {
    Util.debug(debugName + "Parsing Directory: " + io.path());
     Stat s = new Stat(io);
     attsDir(s);
     builder.startElem(DIR, atts);
     for (final IOFile f : io.children()) {
       Stat stat = new Stat(f);
       if (excludePath(f))
         continue;
       if (stat.isLink())
         parseLink(f);
       else if (f.isDir())
         parseDirectory(f);
       else
         parseFile(f);
     }
     builder.endElem();
   }
 
   /**
    * Parses file and builds FSML file entry.
    *
    * @param f regular file to parse
    * @throws IOException
    */
   private void parseFile(final IOFile f) throws IOException {
     Stat s = new Stat(f);
     byte[] sfx = attsFile(s);
     builder.startElem(FILE, atts);
     // get file-specific parsers based on suffix
     if (sfx != null) {
       Parser[] parsers = FSMLUtils.getTransducers(sfx, f);
       for (Parser parser : parsers)
         if (parser != null)
           parser.parse(builder);
     }
     builder.endElem();
   }
 
   /**
    * Parses a symbolic link and builds FSML link entry.
    *
    * @param f link to parse
    * @throw IOException
    */
   private void parseLink(final IOFile f) throws IOException {
     Stat s = new Stat(f);
     attsLink(s);
     builder.startElem(LINK, atts);
     builder.text(s.getTarget());
     builder.endElem();
   }
 
   /**
    * Fills global attribute store with attributes for root fsml element.
    *
    * @return augmented attribute store
    */
   private Atts attsFSML() {
     atts.reset();
     atts.add(VERSION, FSML_VERSION);
     atts.add(SOURCE, token(src.path()));
     final String excludePattern = props.get("exclude-path");
     if (excludePattern.length() != 0)
       atts.add(EXCLUDE_PATTERN, token(excludePattern));
     return atts;
   }
 
   /**
    * Fills global attribute store with directory attributes.
    *
    * @param dir directory to process
    */
   private void attsDir(final Stat s) {
     atts.reset();
     String name = s.file.getName();
     atts.add(NAME, token(name));
     attsStat(s);
   }
 
   /**
    * Fills global attribute store with file attributes.
    *
    * @param file to process
    * @return suffix of file
    */
   private byte[] attsFile(final Stat s) {
     atts.reset();
     String name = s.file.getName();
     atts.add(NAME, token(name));
     byte[] sfx = FSMLUtils.getSuffix(name);
     if (sfx != null)
       atts.add(SUFFIX, sfx);
     attsStat(s);
     return sfx;
   }
 
   /**
    * Fills global attribute store with file attributes.
    *
    * @param file to process
    */
   private void attsLink(final Stat s) {
     atts.reset();
     String name = s.file.getName();
     atts.add(NAME, token(name));
     attsStat(s);
   }
 
   /**
    * Adds file attributes to FSML entry.
    *
    * XML attributes are supposed to match:
    * $ stat -f "%z %m %c %a %#p %u %g %l"
    *
    * @param dir directory to process
    */
   private void attsStat(final Stat s) {
     atts.add(ST_SIZE, token(s.getSize()));
     atts.add(ST_MTIME, token(s.getMTime()));
     if (s.hasPosixStat()) {
       atts.add(ST_CTIME, token(s.getCTime()));
       atts.add(ST_ATIME, token(s.getATime()));
       atts.add(ST_MODE, token(Integer.toOctalString(s.getMode())));
       atts.add(ST_UID, token(s.getUid()));
       atts.add(ST_GID, token(s.getGid()));
       atts.add(ST_NLINK, token(s.getNLink()));
     }
   }
 
   /**
    * Tests if path should be processed.
    *
    * @param pathname to test
    * @return whether path shall be excluded or not
    */
   private boolean excludePath(final IOFile f) {
     try {
       final String path = f.path();
       final String excludePattern = props.get("exclude-path");
       return path.matches(excludePattern);
     } catch (PatternSyntaxException e) {
       Util.debug(e.getDescription());
       return false;
     }
   }
 
 }
