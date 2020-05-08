 /*
  * Copyright (C) 2011-2012 Oleg Tolmatcev <oleg_tolmatcev@yahoo.de>
  */
 package jmdicttodsl;
 
 import com.thoughtworks.xstream.XStream;
 import com.thoughtworks.xstream.io.xml.DomDriver;
 import com.thoughtworks.xstream.io.xml.TraxSource;
 import java.io.File;
 import java.io.IOException;
 import java.io.Writer;
 import java.util.List;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.transform.stream.StreamSource;
 
 /**
  *
  * @author Oleg Tolmatcev
  */
 class XsltDslConverter implements Converter {
     private XStream xstream;
     private Transformer transformer;
     private Writer writer;
     private DslProcessor processor = new DslProcessor();
     private String lang;
 
     @Override
     public void init(File file, Writer writer, String lang) throws TransformerConfigurationException {
         this.lang = lang;
         this.writer = writer;
         xstream = new XStream(new DomDriver());
         xstream.autodetectAnnotations(true);
         xstream.alias("entry", DslEntry.class);
         xstream.alias("word", Entry.class);
 //        xstream.alias("sense", Sense.class);
 //        xstream.alias("lsource", LSource.class);
 //        xstream.aliasField("lsources", Sense.class, "lsource");
         xstream.omitField(Entry.class, "senses");
         xstream.omitField(Sense.class, "stagk");
         xstream.omitField(Sense.class, "stagr");
 
         TransformerFactory factory = TransformerFactory.newInstance();
         String fileName = file.getParent() + File.separator + "dsl.xsl";
         StreamSource template;
         if (new File(fileName).exists())
             template = new StreamSource(new File(fileName));
         else
            template = new StreamSource(getClass().getResource("/dsl.xsl").toString());
         transformer = factory.newTransformer(template);
     }
 
     @Override
     public void doit(XmlEntry entry) throws TransformerException {
         List<DslEntry> entries = processor.process(entry);
         StreamResult result = new StreamResult(writer);
         for (DslEntry e : entries) {
             TraxSource in = new TraxSource(e, xstream);
             transformer.transform(in, result);
         }
     }
 
     @Override
     public void finish() throws IOException {
         if (writer != null)
             writer.close();
     }
 
     @Override
     public void writeHeader() throws IOException {
         String lng;
         switch (lang) {
             case "German":
                 lng = "De";
                 break;
             case "French":
                 lng = "Fr";
                 break;
             case "Russian":
                 lng = "Ru";
                 break;
             default:
                 lng = "En";
                 break;
         }
         writer.append("#NAME	\"JMdict (Ja-" + lng + ")\"\r\n");
         writer.append("#INDEX_LANGUAGE	\"Japanese\"\r\n");
         writer.append("#CONTENTS_LANGUAGE	\"" + lang + "\"\r\n\r\n");
     }
 }
