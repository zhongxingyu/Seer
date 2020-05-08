 package org.basex.fs.fsml.build;
 
 import static org.basex.fs.fsml.FSMLTokens.EXIF_XDCR;
 
 import java.io.IOException;
 
 import org.basex.build.Builder;
 import org.basex.build.Parser;
 import org.basex.fs.fsml.build.exiftool.ExifToolFactory;
 import org.basex.fs.fsml.build.exiftool.ExifToolProcess;
 import org.basex.io.IO;
 import org.basex.io.IOContent;
 import org.basex.util.Util;
 
 public class ExifToolTransducer extends Parser {
 
   /** Prefix for debugging messages. */
  private String debugName = "[ExifToolTransducer] ";
   /** Reference to Builder instance. */
   private Builder builder;
   /** Reference ExifTool process. */
  private ExifToolProcess exiftool = ExifToolFactory.getInstance();
 
   public ExifToolTransducer(final IO source) {
     super(source);
   }
 
   @Override
   public void parse(final Builder build) throws IOException {
     if (!exiftool.isAlive()) {
       Util.debug(debugName + "Missing exiftool to extract from " + src.name());
       return;
     }
     builder = build;
     IOContent metadataXML = exiftool.extractMetadataAsXML(src);
     XMLTransducer xmlParser = new XMLTransducer(metadataXML, EXIF_XDCR, true);
     xmlParser.parse(builder);
   }
 
 }
