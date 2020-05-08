 /* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.vfny.geoserver.wms.responses.featureInfo;
 
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 import java.nio.charset.Charset;
 import java.util.Collections;
 
 import org.geotools.data.FeatureReader;
 import org.geotools.data.FeatureResults;
 import org.geotools.feature.AttributeType;
 import org.geotools.feature.Feature;
 import org.geotools.feature.FeatureType;
 import org.geotools.feature.IllegalAttributeException;
 
 import com.vividsolutions.jts.geom.Geometry;
 
 
 /**
  * Produces a FeatureInfo response in HTML.  Relies on abstractfeatureinfo and
  * the feature delegate to do most of the work, just implements an html based
  * writeTo method.
  * 
  * <p>
  * In the future James suggested that we allow some sort of template system, so
  * that one can control the formatting of the html output, since now we just
  * hard code some minimal header stuff. See
  * http://jira.codehaus.org/browse/GEOS-196
  * </p>
  *
  * @author James Macgill, PSU
  * @version $Id: HTMLTableFeatureInfoResponse.java,v 1.1 2004/07/19 22:32:22 jmacgill Exp $
  */
 public class HTMLTableFeatureInfoResponse extends AbstractFeatureInfoResponse {
     /**
      *
      */
     public HTMLTableFeatureInfoResponse() {
         format = "text/html";
         supportedFormats = Collections.singletonList(format);
     }
 
     /**
      * Writes the image to the client.
      *
      * @param out The output stream to write to.
      *
      * @throws org.vfny.geoserver.ServiceException For problems with geoserver
      * @throws java.io.IOException For problems writing the output.
      */
     public void writeTo(OutputStream out)
         throws org.vfny.geoserver.ServiceException, java.io.IOException {
         Charset charSet = getRequest().getGeoServer().getCharSet();
         OutputStreamWriter osw = new OutputStreamWriter(out, charSet);
         PrintWriter writer = new PrintWriter(osw);
         writer.println("<html><body>");
 
         FeatureReader reader = null;
         try {
             for (int i = 0; i < results.size(); i++) {
                 FeatureResults fr = (FeatureResults) results.get(i);
                 FeatureType schema = fr.getSchema();
 
                 writer.println("<table border='1'>");
                 writer.println("<tr><th colspan=" + schema.getAttributeCount()
                     + " scope='col'>" + schema.getTypeName() + " </th></tr>");
                 writer.println("<tr>");
 
                 for (int j = 0; j < schema.getAttributeCount(); j++) {
                     writer.println("<td>"
                         + schema.getAttributeType(j).getName() + "</td>");
                 }
 
                 writer.println("</tr>");
 
                 //writer.println("Found " + fr.getCount() + " in " + schema.getTypeName());
                 reader = fr.reader();
 
                 while (reader.hasNext()) {
                     Feature f = reader.next();
                     AttributeType[] types = schema.getAttributeTypes();
                     writer.println("<tr>");
 
                     for (int j = 0; j < types.length; j++) {
                         if (Geometry.class.isAssignableFrom(types[j].getType())) {
                             writer.println("<td>");
                             writer.println("[GEOMETRY]");
                             writer.println("</td>");
                         } else {
                             writer.println("<td>");
                             writer.print(f.getAttribute(types[j].getName()));
                             writer.println("</td>");
                         }
                     }
 
                     writer.println("</tr>");
                 }
 
                 writer.println("</table>");
                 writer.println("<p>");
             }
         } catch (IllegalAttributeException ife) {
             writer.println("Unable to generate information " + ife);
         }
         finally
 		{
         	if (reader != null)
         		reader.close();
 		}
         
         writer.flush();
     }
 }
