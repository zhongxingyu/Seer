 package org.mashti.sight;
 
 import java.awt.Rectangle;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectOutputStream;
 import java.io.OutputStreamWriter;
 import java.io.Writer;
 import org.apache.batik.dom.GenericDOMImplementation;
 import org.apache.batik.svggen.SVGGraphics2D;
 import org.apache.commons.io.IOUtils;
 import org.jfree.chart.JFreeChart;
 import org.w3c.dom.DOMImplementation;
 import org.w3c.dom.Document;
 
 /** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
 public final class ChartExportUtils {
 
     private static final String SVG_CHARSET = "UTF-8";
     private static final boolean USE_CSS_IN_SVG_EXPORT = true;
     private static final DOMImplementation DOM = GenericDOMImplementation.getDOMImplementation();
 
     public static void saveAsSVG(JFreeChart chart, int width, int height, File destination) throws IOException {
 
         final Document document = DOM.createDocument(null, "svg", null);
         final SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
         Writer out = null;
         try {
             out = new OutputStreamWriter(new FileOutputStream(destination), SVG_CHARSET);
             chart.draw(svgGenerator, new Rectangle(width, height));
             svgGenerator.stream(out, USE_CSS_IN_SVG_EXPORT);
             out.flush();
         }
         finally {
             IOUtils.closeQuietly(out);
         }
     }
 
     public static void saveAsSerializedObject(JFreeChart chart, File destination) throws IOException {
 
         ObjectOutputStream out = null;
         try {
             out = new ObjectOutputStream(new FileOutputStream(destination));
             out.writeObject(chart);
             out.flush();
         }
         finally {
             IOUtils.closeQuietly(out);
         }
     }
 
 }
