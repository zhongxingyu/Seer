 /*
  * Copyright (C) 2011-2012 Oleg Tolmatcev <oleg_tolmatcev@yahoo.de>
  */
 package jmdicttodsl;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.Writer;
import java.net.URL;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import org.stringtemplate.v4.ST;
 import org.stringtemplate.v4.STGroup;
 import org.stringtemplate.v4.STGroupFile;
 
 /**
  *
  * @author Oleg Tolmatcev
  */
 class StlDslConverter implements Converter {
 
     private Writer writer;
     private STGroup group;
     private DslProcessor processor = new DslProcessor();
     private String lang;
 
     @Override
     public void init(File file, Writer writer, String lang) {
         this.writer = writer;
         this.lang = lang;
         String fileName = file.getParent() + File.separator + "dsl.stg";
        URL resource = getClass().getResource("/dsl.stg");
         if (new File(fileName).exists())
             group = new STGroupFile(fileName);
         else
            group = new STGroupFile(resource, "UTF-8", '<', '>');
     }
 
     @Override
     public void doit(XmlEntry xmlEntry) throws IOException {
         List<DslEntry> dslEntries = processor.process(xmlEntry);
         List<String> names = Arrays.asList("pos", "field", "misc",
                 "dial", "lsource", "gloss", "xref");
 
         for (DslEntry dslEntry : dslEntries) {
             List<String> senses = new ArrayList<>();
             for (Sense sense : dslEntry.sense) {
                 // STL evaluates an attribute to false only if it is "null" or
                 // "false".
                 for (LSource lsource : sense.lsource)
                     if (lsource.text != null && lsource.text.isEmpty())
                         lsource.text = null;
 
                 List<String> attributes = new ArrayList<>();
                 for (String name : names) {
                     ST st = group.getInstanceOf(name);
                     st.add("sense", sense);
                     String result = st.render();
                     if (!result.equals(""))
                         attributes.add(result);
                 }
 
                 if (!attributes.isEmpty()) {
                     ST st = group.getInstanceOf("join");
                     st.add("list", attributes);
                     senses.add(st.render());
                 }
             }
             ST st = group.getInstanceOf("senses");
             st.add("index", dslEntry.index);
             st.add("entries", dslEntry.entry);
             st.add("senses", senses);
             st.add("count", senses.size() > 1);
             writer.append(st.render());
             writer.append("\r\n\r\n");
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
