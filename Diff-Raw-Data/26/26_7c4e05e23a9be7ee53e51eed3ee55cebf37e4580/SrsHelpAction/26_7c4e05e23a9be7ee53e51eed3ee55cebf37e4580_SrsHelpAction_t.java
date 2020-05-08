 /* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.vfny.geoserver.action;
 
 import org.apache.struts.action.Action;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 import org.apache.struts.action.DynaActionForm;
 import org.geotools.referencing.CRS;
 import org.opengis.referencing.crs.CoordinateReferenceSystem;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
import java.util.HashSet;
 import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 
 /**
  *
  * @author Simone Giannecchini, GeoSolutions
  *
  */
 public class SrsHelpAction extends Action {
     private final static Logger LOGGER = Logger.getLogger(SrsHelpAction.class.toString());
 
     /**
      *  This is a simple action - it reads in the GT2 supported EPSG codes.
      *
      * DONE: once geosever support EPSG thats not in the properties file, this should
      *       be a bit more abstract and get a list of all EPSG defs from the
      *       GDSFactory (if possible).  Use toWKT() as its nicer to read.
      *
      *   Form has two properies - ids  (list of String - the epsg #)
      *                            defs (list of String - the epsg WKT definitions)
      */
     public ActionForward execute(ActionMapping mapping, ActionForm form,
         HttpServletRequest request, HttpServletResponse response)
         throws IOException, ServletException {
         ArrayList defs = new ArrayList();
         ArrayList ids_string = new ArrayList();
 
         Set codes = CRS.getSupportedCodes("EPSG");
 
         // make an array of each code (as an int)
        HashSet idSet = new HashSet();
         Iterator codeIt = codes.iterator();
 
         while (codeIt.hasNext()) {
             String code = (String) codeIt.next();
             String id = code.substring(code.indexOf(':') + 1); //just the number
 
            idSet.add(Integer.valueOf(id));
         }
 
        List ids = new ArrayList(idSet);
         Collections.sort(ids); //sort to get them in order
 
         CoordinateReferenceSystem crs;
         String def;
         codeIt = ids.iterator();
 
         Integer id;
 
         while (codeIt.hasNext()) //for each id (in sorted order)
          {
             id = (Integer) codeIt.next();
 
             try { //get its definition
                crs = CRS.decode("EPSG:"+ id);
                 def = crs.toWKT();
                 defs.add(def);
                 ids_string.add(id.toString());
             } catch (Exception e) {
                if(LOGGER.isLoggable(Level.FINER))
                    LOGGER.log(Level.FINER, "Issues converting EPSG:" + id, e);
                else
                    LOGGER.log(Level.WARNING, "Issues converting EPSG:" + id + ". " 
                        + e.getLocalizedMessage() + " Stack trace included at FINER logging level");
                
             }
         }
 
         //send off to the .jsp
         DynaActionForm myForm = (DynaActionForm) form;
 
         myForm.set("srsDefinitionList", defs.toArray(new String[1]));
         myForm.set("srsIDList", ids_string.toArray(new String[1]));
 
         // return back to the admin demo
         //
         return mapping.findForward("success");
     }
 }
