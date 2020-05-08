 /*
  * Copyright (C) 2011-2012 Oleg Tolmatcev <oleg_tolmatcev@yahoo.de>
  */
 package jmdicttodsl;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.stringtemplate.v4.ST;
 import org.stringtemplate.v4.STGroup;
 import org.stringtemplate.v4.STGroupFile;
 
 /**
  *
  * @author Oleg Tolmatcev
  */
 class StlEdictConverter implements Converter {
 
     private Writer writer;
     private STGroup group;
     private String lang;
 
     public StlEdictConverter() {
     }
 
     @Override
     public void init(File file, Writer writer, String lang) {
         this.writer = writer;
         this.lang = lang;
         String fileName = file.getParent() + File.separator + "edict.stg";
         if (new File(fileName).exists())
             group = new STGroupFile(fileName);
         else
            group = new STGroupFile(getClass().getResource("edict.stg"),
                     "UTF-8", '<', '>');
     }
 
     @Override
     public void doit(XmlEntry xmlEntry) throws IOException {
         for (Reading r_ele : xmlEntry.r_ele)
             if (!xmlEntry.k_ele.isEmpty())
                 for (Kanji k_ele : xmlEntry.k_ele)
                     processEntry(xmlEntry, k_ele, r_ele);
             else
                 processEntry(xmlEntry, new Kanji(), r_ele);
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
 
     private void processEntry(XmlEntry xmlEntry, Kanji k_ele, Reading r_ele) throws IOException {
         StringBuilder result = new StringBuilder();
         if (r_ele.re_restr.contains(k_ele.keb) || r_ele.re_restr.isEmpty()) {
             List<Sense> senses = new ArrayList<>();
             for (Sense s : xmlEntry.sense)
                 if ((s.stagr.isEmpty() && s.stagk.isEmpty())
                         || s.stagr.contains(r_ele.reb)
                         || s.stagk.contains(k_ele.keb)) {
                     senses.add(s);
                 }
             if (!senses.isEmpty()) {
                 if (k_ele.keb != null)
                     result.append(k_ele.keb).append(" [").append(r_ele.reb).append("] /");
                 else
                     result.append(r_ele.reb).append(" /");
 
                 ST st;
                 if (k_ele.keb != null) {
                     st = group.getInstanceOf("infos");
                     st.add("xs", k_ele.ke_inf);
                     result.append(st.render());
                 }
 
                 st = group.getInstanceOf("infos");
                 st.add("xs", r_ele.re_inf);
                 result.append(st.render());
 
                 st = group.getInstanceOf("sense");
                 st.add("xs", senses);
                 st.add("count", senses.size() > 1);
                 result.append(st.render());
 
                 result.append("\n");
                 writer.append(result.toString());
             }
         }
     }
 }
