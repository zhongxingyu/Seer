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
 import java.util.Date;
 import java.util.logging.Level;
 import java.util.logging.Logger;
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
 class XsltEdictConverter implements Converter {
 
     private File file;
     private Writer writer;
     private String lang;
     private XStream xstream;
     private Transformer transformer;
 
     @Override
     public void init(File file, Writer writer, String lang) throws TransformerConfigurationException {
         this.file = file;
         this.writer = writer;
         this.lang = lang;
 //        XmlFriendlyNameCoder coder = new XmlFriendlyNameCoder("dollar", "_");
 //        xstream = new XStream(new DomDriver("UTF-8", coder));
         xstream = new XStream(new DomDriver());
         xstream.autodetectAnnotations(true);
 
         TransformerFactory factory = TransformerFactory.newInstance();
         String fileName = file.getParent() + File.separator + "edict.xsl";
         StreamSource template;
         if (new File(fileName).exists())
             template = new StreamSource(new File(fileName));
         else
             template = new StreamSource(getClass().getResource("edict.xsl").toString());
         transformer = factory.newTransformer(template);
     }
 
     @Override
     public void doit(XmlEntry entry) throws TransformerException {
         StreamResult result = new StreamResult(writer);
         TraxSource in = new TraxSource(entry, xstream);
         transformer.transform(in, result);
     }
 
     @Override
     public void finish() throws IOException {
         if (writer != null)
             writer.close();
     }
 
     @Override
     public void writeHeader() {
         try {
            writer.append("　？？？ /EDICT, EDICT_SUB(P), EDICT2 Japanese-English Electronic Dictionary Files/Copyright Electronic Dictionary Research & Development Group - 2011/Created: " + new Date().toString() + "\n");
         } catch (IOException ex) {
             Logger.getLogger(XsltEdictConverter.class.getName()).log(Level.SEVERE, null, ex);
         }
 
     }
 }
