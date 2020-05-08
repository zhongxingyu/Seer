 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package de.cismet.cids.custom.tostringconverter.wunda_blau;
 
 import de.cismet.cids.tools.CustomToStringConverter;
 
 /**
  * DOCUMENT ME!
  *
  * @author   srichter
  * @version  $Revision$, $Date$
  */
 public class Alkis_landparcelToStringConverter extends CustomToStringConverter {
 
     //~ Methods ----------------------------------------------------------------
 
     @Override
     public String createString() {
        final Object bezeichnung = cidsBean.getProperty("alkis_id");
         if (bezeichnung != null) {
             return bezeichnung.toString();
         }
         return "-";
     }
 }
