 package org.basex.fs.fsml.build;
 
 import static org.basex.fs.fsml.FSMLTokens.CHOPPING;
 import static org.basex.fs.fsml.FSMLTokens.CONTENT;
 import static org.basex.fs.fsml.FSMLTokens.DOC_ENCODING;
 import static org.basex.fs.fsml.FSMLTokens.DOC_SIZE;
 import static org.basex.fs.fsml.FSMLTokens.DBID;
 import static org.basex.fs.fsml.FSMLTokens.METADATA;
 import static org.basex.fs.fsml.FSMLTokens.NAME;
 import static org.basex.fs.fsml.FSMLTokens.WELLFORMED;
 import static org.basex.fs.fsml.FSMLTokens.XML_DBNODES;
 import static org.basex.fs.fsml.FSMLTokens.XML_DBSIZE;
 import static org.basex.fs.fsml.FSMLTokens.XML_DBTIME;
 import static org.basex.fs.fsml.FSMLTokens.XML_XDCR;
 
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import org.basex.build.Builder;
 import org.basex.build.Parser;
 import org.basex.build.xml.XMLParser;
 import org.basex.core.Context;
 import org.basex.core.Prop;
import org.basex.core.cmd.Close;
 import org.basex.core.cmd.Create;
 import org.basex.data.MetaData;
 import org.basex.fs.fsml.FSMLUtils;
 import org.basex.io.IO;
 import org.basex.util.Performance;
 import org.basex.util.Token;
 
 /**
  * Create an XML database from XML input file and add statistics to FSML DB.
  *
  * @author BaseX Team 2005-11, BSD License
  * @author Alexander Holupirek
  */
 public class XMLTransducer extends Parser {
 
   /** Date format. */
   private static final SimpleDateFormat DATE =
       new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
 
   /** Reference to Builder instance. */
   private Builder builder;
 
   /** Name of transducer that invoked XML parsing. */
   private byte[] xdcr;
   /** Flag if db will contain metadata about file or native XML content. */
   private boolean metadata;
 
   /** Constructor. */
   public XMLTransducer(final IO path)
       throws IOException {
     super(path);
   }
 
   /** Constructor. */
   public XMLTransducer(final IO path, final byte[] name, final boolean meta)
       throws IOException {
     super(path);
     xdcr = name;
     metadata = meta;
   }
 
   /**
    * Parses XML document and adds it to FSML database instance.
    *
    * @param FSML builder
    */
   @Override
   public void parse(final Builder build) throws IOException {
     builder = build;
     XMLParser xp = new XMLParser(src, "", new Prop());
     Context ctx = new Context();
     String dbname = FSMLUtils.uuidString();
     boolean ok = new Create(xp, dbname).run(ctx);
     if (!ok) {
       atts.reset();
       atts.add(NAME, XML_XDCR);
       atts.add(WELLFORMED, Token.FALSE);
       builder.emptyElem(METADATA, atts);
       return;
     }
 
     MetaData meta = ctx.data().meta;
 
     atts.reset();
     if (xdcr != null)
       atts.add(XML_XDCR, xdcr);
     atts.add(DBID, Token.token(meta.name));
 
     atts.add(XML_DBSIZE, Token.token(meta.dbsize()));
     atts.add(XML_DBNODES, Token.token(meta.size));
     atts.add(XML_DBTIME, Token.token(DATE.format(new Date(meta.dbtime()))));
 
     if(meta.filesize != 0)
       atts.add(DOC_SIZE, Token.token(Performance.format(meta.filesize)));
 
     atts.add(DOC_ENCODING, Token.token(meta.encoding));
     atts.add(CHOPPING, Token.token(meta.chop));
 
     builder.emptyElem(metadata ? METADATA : CONTENT, atts);

    Close.close(ctx.data(), ctx);
   }
 }
