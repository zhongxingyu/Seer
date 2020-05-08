 package net.sf.okapi.tm.pensieve.tmx;
 
 import java.io.StringWriter;
 import net.sf.okapi.common.XMLWriter;
 import net.sf.okapi.filters.tmx.TmxFilter;
 import net.sf.okapi.tm.pensieve.seeker.PensieveSeeker;
import net.sf.okapi.tm.pensieve.seeker.ITmSeeker;
 import net.sf.okapi.tm.pensieve.writer.PensieveWriter;
import net.sf.okapi.tm.pensieve.writer.ITmWriter;
 import org.apache.lucene.store.Directory;
 import org.apache.lucene.store.RAMDirectory;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 import net.sf.okapi.common.filterwriter.TMXWriter;
 
 public class TmxHandlerExportTest {
 
     @Test
     public void exportTmx_sample_metadata() throws Exception {
         Directory ramDir = new RAMDirectory();
        ITmWriter tmWriter = new PensieveWriter(ramDir);
         OkapiTmxImporter tmxImporter = new OkapiTmxImporter("EN", new TmxFilter());
         OkapiTmxExporter tmxExporter = new OkapiTmxExporter();
         tmxImporter.importTmx(this.getClass().getResource("/sample_tmx.xml").toURI(), "IT", tmWriter);
 
        ITmSeeker seeker = new PensieveSeeker(ramDir);
         StringWriter sWriter = new StringWriter();
         tmxExporter.exportTmx("EN", "IT", seeker, new TMXWriter(new XMLWriter(sWriter)));
 
         String expectedTmx = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                 "<tmx version=\"1.4\">" +
                 "<header creationtool=\"pensieve\" creationtoolversion=\"0.0.1\" " +
                 "segtype=\"sentence\" o-tmf=\"pensieve\" adminlang=\"en\" " +
                 "srclang=\"EN\" datatype=\"unknown\"></header>" +
                 "<body>" +
                 "<tu tuid=\"hello123\">" +
                 "<prop type=\"Txt::GroupName\">ImAGroupie</prop>" +
                 "<prop type=\"Txt::FileName\">GeorgeInTheJungle.hdf</prop>" +
                 "<tuv xml:lang=\"EN\"><seg>hello</seg></tuv>" +
                 "<tuv xml:lang=\"IT\"><seg>ciao</seg></tuv></tu>" + 
                 "<tu tuid=\"world\">" +
                 "<tuv xml:lang=\"EN\"><seg>world</seg></tuv>" +
                 "<tuv xml:lang=\"IT\"><seg>mondo</seg></tuv>" + 
                 "</tu></body></tmx>";
         assertEquals("tmx content", expectedTmx.replaceAll("[\\n\\r]+", ""), sWriter.toString().replaceAll("[\\n\\r]+", ""));
     }
 }
