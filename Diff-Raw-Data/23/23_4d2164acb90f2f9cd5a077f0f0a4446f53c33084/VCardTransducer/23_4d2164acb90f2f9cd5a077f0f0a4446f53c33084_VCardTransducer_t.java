 package org.deepfs.fsml.xdcr;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Iterator;
 
 import org.deepfs.fsml.FSML;
 
 import info.ineighborhood.cardme.engine.VCardEngine;
 import info.ineighborhood.cardme.vcard.VCard;
 import info.ineighborhood.cardme.vcard.features.EmailFeature;
import info.ineighborhood.cardme.vcard.features.NameFeature;
 
 /**
  * This class extracts Information from VCards.
  *
  * @author Copyright (c) 2011, Michael Seiferle, ISC license
  * @author Copyright (c) 2011, Alexander Holupirek, ISC license
  *
  */
 public class VCardTransducer extends Transducer {
 
     /** The VCard Reader engine. */
     private static final VCardEngine VCE = new VCardEngine();
 
     /**
      * Constructs a VCardTransducer to extract metadata from VCards.
      */
     public VCardTransducer() {
         super("text/x-vcard");
         // TODO Auto-generated constructor stub
     }
 
     @Override
     public String read(final String path) {
         VCard vc = null;
         try {
             vc = VCE.parse(new File(path));
         } catch (IOException e) {
             e.printStackTrace();
         }
         if (null != vc) {
             StringBuilder s = new StringBuilder(256);
             s.append("<metadata>\n");
            final NameFeature name = vc.getName();
            if (name != null) {
             s.append(keyValue("given_name",
                    FSML.xmlifyTextContent(name.getGivenName())));
             s.append(keyValue("family_name",
                    FSML.xmlifyTextContent(name.getFamilyName())));
            }
             Iterator<EmailFeature> emlitr = vc.getEmails();
             while (emlitr.hasNext()) {
                 s.append(keyValue("emails", emlitr.next().getEmail()));
 
             }
 
             s.append("</metadata>");
             return s.toString();
             // String f = vc.getName().getGivenName();
             // String l = vc.getName().getFamilyName();
             // System.err.println("<fname>" + f + "</fname><lname>" + l
             // + "</lname>");
         }
         return "";
 
     }
 
     @Override
     public boolean write(final String path, final String xml) {
         // TODO Auto-generated method stub
         return false;
     }
 
     @Override
     public String template() {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public String rng() {
         // TODO Auto-generated method stub
         return null;
     }
 
 }
