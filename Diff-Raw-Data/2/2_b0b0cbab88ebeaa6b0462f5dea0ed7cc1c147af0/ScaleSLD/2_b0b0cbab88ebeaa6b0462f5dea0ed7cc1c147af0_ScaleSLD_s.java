 /*
     Open Aviation Map
     Copyright (C) 2012-2013 Ákos Maróy
 
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU Affero General Public License as
     published by the Free Software Foundation, either version 3 of the
     License, or (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU Affero General Public License for more details.
 
     You should have received a copy of the GNU Affero General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.openaviationmap.rendering;
 
 import gnu.getopt.Getopt;
 import gnu.getopt.LongOpt;
 
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Reader;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.StringTokenizer;
 
 import javax.xml.namespace.NamespaceContext;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpressionException;
 import javax.xml.xpath.XPathFactory;
 
 import org.apache.ws.commons.util.NamespaceContextImpl;
 import org.geotools.referencing.CRS;
 import org.opengis.referencing.crs.CoordinateReferenceSystem;
 import org.w3c.dom.Document;
 import org.w3c.dom.DocumentFragment;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.w3c.dom.Text;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 /**
  * Utility program to pre-process SLD files, so that they are scaled
  * for a particular DPI value, and a number of map scales.
  */
 public final class ScaleSLD {
 
     /** The default DPI value. */
     public static final double DEFAULT_DPI = 25.4d / .28d;
 
     /** The default CRS value. */
     public static final String DEFAULT_CRS = "EPSG:900913";
 
     /** The default reference point in the default CRS. */
     public static final double[] DEFAULT_REF_XY = {2094309, 5873561};
 
     /** Namespace prefix for the SLD namespace. */
     private static final String SLD_NS_PREFIX = "sld";
 
     /** Namespace URI for the SLD namespace. */
     private static final String SLD_NS_URI    = "http://www.opengis.net/sld";
 
     /** Namespace prefix for the OGC namespace. */
     private static final String OGC_NS_PREFIX = "ogc";
 
     /** Namespace URI for the OGC namespace. */
     private static final String OGC_NS_URI    = "http://www.opengis.net/ogc";
 
     /** Namespace prefix for the GML namespace. */
     private static final String GML_NS_PREFIX = "gml";
 
     /** Namespace URI for the GML namespace. */
     private static final String GML_NS_URI    = "http://www.opengis.net/gml";
 
     /** Namespace prefix for the XLink namespace. */
     private static final String XLINK_NS_PREFIX = "xlink";
 
     /** Namespace URI for the XLink namespace. */
     private static final String XLINK_NS_URI = "http://www.w3.org/1999/xlink";
 
     /**
      * Private default constructor.
      */
     private ScaleSLD() {
     }
 
     /**
      * Print a help message.
      */
     private static void printHelpMessage() {
         System.out.println(
         "Open Aviation Map SLD scaling utility");
         System.out.println();
         System.out.println(
         "usage:");
         System.out.println();
         System.out.println(
         "  -c | --crs <value>           the reference CRS, e.g.");
         System.out.println(
         "                               EPSG:900913");
         System.out.println(
         "  -d | --dpi <value>           the target device dpi");
         System.out.println(
         "                               optional, defaults to "
                                       + DEFAULT_DPI);
         System.out.println(
         "  -i | --input <input.file>    specify the input file, required");
         System.out.println(
         "  -o | --output <output.file>  the output file");
         System.out.println(
         "  -r | --refpoint x,y          a coordiante point as defined by");
         System.out.println(
         "                               the supplied CRS, used to calculate");
         System.out.println(
         "                               distances");
         System.out.println(
         "  -s | --scales N,M,...        a comma-separated list of scales");
         System.out.println(
         "                               to generate rule segments in the");
         System.out.println(
         "                               output file, e.g. 50000,100000");
         System.out.println(
         "                               optional");
         System.out.println(
         "                               must be in increasing order!");
         System.out.println(
         "  -h | --help                  show this usage page");
         System.out.println();
     }
 
     /**
      * Program entry point.
      *
      * @param args command line arguments.
      */
     public static void main(String[] args) {
 
         LongOpt[] longopts = new LongOpt[7];
 
         longopts[0] = new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h');
         longopts[1] = new LongOpt("crs", LongOpt.OPTIONAL_ARGUMENT,
                 null, 'c');
         longopts[2] = new LongOpt("dpi", LongOpt.OPTIONAL_ARGUMENT,
                 null, 'd');
         longopts[3] = new LongOpt("input", LongOpt.REQUIRED_ARGUMENT,
                 null, 'i');
         longopts[4] = new LongOpt("output", LongOpt.REQUIRED_ARGUMENT,
                 null, 'o');
         longopts[5] = new LongOpt("refpoint", LongOpt.OPTIONAL_ARGUMENT,
                 null, 'r');
         longopts[6] = new LongOpt("scales", LongOpt.OPTIONAL_ARGUMENT,
                 null, 's');
 
         Getopt g = new Getopt("ScaleSLD", args, "c:d:hi:o:r:s:", longopts);
 
         int c;
 
         String      inputFile   = null;
         String      outputFile  = null;
         String      strScales   = null;
         String      strDpi      = null;
         String      crsStr      = DEFAULT_CRS;
         String      strRefPoint = null;
 
         while ((c = g.getopt()) != -1) {
             switch (c) {
             case 'c':
                 crsStr = g.getOptarg();
                 break;
 
             case 'd':
                 strDpi = g.getOptarg();
                 break;
 
             case 'i':
                 inputFile = g.getOptarg();
                 break;
 
             case 'o':
                 outputFile = g.getOptarg();
                 break;
 
             case 'r':
                 strRefPoint = g.getOptarg();
                 break;
 
             case 's':
                 strScales = g.getOptarg();
                 break;
 
             default:
             case 'h':
                 printHelpMessage();
                 return;
 
             case '?':
                 System.out.println("Invalid option '" + g.getOptopt()
                                    + "' specified");
                 return;
             }
         }
 
         if (inputFile == null) {
             System.out.println("Required option input not specified");
             System.out.println();
             printHelpMessage();
             return;
         }
         if (outputFile == null) {
             System.out.println("Required option output not specified");
             System.out.println();
             printHelpMessage();
             return;
         }
 
         // parse the DPI value, if supplied
         double dpi = DEFAULT_DPI;
         if (strDpi != null) {
             try {
                 dpi = Double.parseDouble(strDpi);
             } catch (Exception e) {
                 System.out.println("Error parsing dpi value.");
                 System.out.println();
                 e.printStackTrace(System.out);
                 return;
             }
         }
 
         // parse the scales, if provided
         List<Double>   scales = null;
         if (strScales != null) {
             if ("EPSG:900913".equals(strScales)) {
                 scales = new ArrayList<Double>(
                                 KnownScaleList.epsg900913ScaleList(dpi, 31));
                 Collections.reverse(scales);
             } else {
                 StringTokenizer tok = new StringTokenizer(strScales, ",");
                 scales              = new ArrayList<Double>(tok.countTokens());
                 try {
                     while (tok.hasMoreTokens()) {
                         scales.add(Double.parseDouble(tok.nextToken()));
                     }
                 } catch (Exception e) {
                     System.out.println("Error parsing supplied scale values.");
                     System.out.println();
                     e.printStackTrace(System.out);
                     return;
                 }
             }
         }
 
         // parse the reference point
         double[] refXY = DEFAULT_REF_XY;
         if (strRefPoint != null) {
             StringTokenizer tok = new StringTokenizer(strScales, ",");
             if (tok.countTokens() == 2) {
                 try {
                     refXY = new double[2];
                     refXY[0] = Double.parseDouble(tok.nextToken());
                     refXY[1] = Double.parseDouble(tok.nextToken());
                 } catch (Exception e) {
                     System.out.println("Error parsing supplied scale values.");
                     System.out.println();
                     e.printStackTrace(System.out);
                     return;
                 }
             }
         }
 
         try {
             FileReader   reader = new FileReader(inputFile);
             Writer       writer = new FileWriter(outputFile);
             CoordinateReferenceSystem crs = CRS.decode(crsStr);
 
             scaleSld(reader, scales, dpi, crs, refXY, writer);
 
         } catch (Exception e) {
             System.out.println("Scaling failed.");
             System.out.println();
             e.printStackTrace(System.out);
             return;
         }
 
         System.out.println("Scaling successful.");
     }
 
     /**
      * Return a namespace context with the used namespaces and their
      * preferred prefixes.
      *
      * @return a namespace context with namespaces used by SLD documents.
      */
     public static NamespaceContext getNsCtx() {
         NamespaceContextImpl nsCtx = new NamespaceContextImpl();
 
         nsCtx.startPrefixMapping(SLD_NS_PREFIX, SLD_NS_URI);
         nsCtx.startPrefixMapping(OGC_NS_PREFIX, OGC_NS_URI);
         nsCtx.startPrefixMapping(GML_NS_PREFIX, GML_NS_URI);
         nsCtx.startPrefixMapping(XLINK_NS_PREFIX, XLINK_NS_URI);
 
         return nsCtx;
     }
 
     /**
      * Scale an SLD file, using the default DPI value.
      *
      * @param input the input file to scale
      * @param scales a list of scales, interpreted as 1:N, for which to generate
      *        different segments in the generated SLD output
      * @param crs the CRS reference to use
      * @param refXY the x and y coordinates of a reference point in CRS space
      * @param output generate the scaled result into this output file
      * @throws ParserConfigurationException on XML parser configuration errors
      * @throws SAXException on XML parser errors
      * @throws IOException on I/O errors
      * @throws TransformerException on XML transformer errors
      * @throws XPathExpressionException on XPath errors
      * @throws RenderException on SLD scaling, rendering issues
      */
     static void
     scaleSld(Reader                     input,
              List<Double>               scales,
              CoordinateReferenceSystem  crs,
              double[]                   refXY,
              Writer                     output)
                                         throws ParserConfigurationException,
                                                SAXException,
                                                IOException,
                                                TransformerException,
                                                XPathExpressionException,
                                                RenderException {
 
         scaleSld(input, scales, DEFAULT_DPI, crs, refXY, output);
     }
 
     /**
      * Scale an SLD file.
      *
      * @param input the input file to scale
      * @param scales a list of scales, interpreted as 1:N, for which to generate
      *        different segments in the generated SLD output
      * @param dpi dots per inch on the target rendering device
      * @param crs the CRS reference to use
      * @param refXY the x and y coordinates of a reference point in CRS space
      * @param output generate the scaled result into this output file
      * @throws ParserConfigurationException on XML parser configuration errors
      * @throws SAXException on XML parser errors
      * @throws IOException on I/O errors
      * @throws TransformerException on XML transformer errors
      * @throws XPathExpressionException on XPath errors
      * @throws RenderException on SLD scaling, rendering issues
      */
     static void
     scaleSld(Reader                     input,
              List<Double>               scales,
              double                     dpi,
              CoordinateReferenceSystem  crs,
              double[]                   refXY,
              Writer                     output)
                                         throws ParserConfigurationException,
                                                SAXException,
                                                IOException,
                                                TransformerException,
                                                XPathExpressionException,
                                                RenderException {
         // read the input file
         DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
         dbf.setNamespaceAware(true);
         DocumentBuilder        db  = dbf.newDocumentBuilder();
 
         InputSource  fSource = new InputSource(input);
         Document     d = db.parse(fSource);
 
         // scale the loaded SLD
         Document dd = scaleSld(d, scales, dpi, crs, refXY);
 
         // write the XML document into a file
         TransformerFactory tFactory = TransformerFactory.newInstance();
         Transformer transformer = tFactory.newTransformer();
         transformer.setOutputProperty(OutputKeys.INDENT, "yes");
 
         DOMSource source = new DOMSource(dd);
         StreamResult result = new StreamResult(output);
         transformer.transform(source, result);
     }
 
     /**
      * Scale an SLD document.
      *
      * @param input the input document to scale
      * @param scales a list of scales, interpreted as 1:N, for which to generate
      *        different segments in the generated SLD output
      * @param dpi the dpi value of the target device
      * @param crs the CRS reference to use
      * @param refXY the x and y coordinates of a reference point in CRS space
      * @return the scaled result as an XML document
      * @throws XPathExpressionException on XPath errors
      * @throws RenderException on SLD parsing, scaling errors
      * @throws ParserConfigurationException on XML parser config errors
      */
     static Document scaleSld(Document                   input,
                              List<Double>               scales,
                              double                     dpi,
                              CoordinateReferenceSystem  crs,
                              double[]                   refXY)
                                       throws XPathExpressionException,
                                              RenderException,
                                              ParserConfigurationException {
 
         // check if this is really an SLD document
         Node root = input.getDocumentElement();
         if (!"StyledLayerDescriptor".equals(root.getLocalName())
          || !"http://www.opengis.net/sld".equals(root.getNamespaceURI())) {
 
             throw new RenderException("input document is not an SLD document");
         }
 
         XPath xpath = XPathFactory.newInstance().newXPath();
         xpath.setNamespaceContext(getNsCtx());
 
         // see if this document already contains scaling information
         Double d = (Double) xpath.evaluate(
                 "count(//" + SLD_NS_PREFIX + ":MinScaleDenominator"
                    + "|//" + SLD_NS_PREFIX + ":MaxScaleDenominator)",
                 input, XPathConstants.NUMBER);
         if (d != 0) {
             throw new RenderException("document is already scaled, "
                                     + "rescaling not supported");
         }
 
         // duplicate the input at first
         DocumentBuilderFactory dbf    = DocumentBuilderFactory.newInstance();
         dbf.setNamespaceAware(true);
         DocumentBuilder        db     = dbf.newDocumentBuilder();
         Document               output = db.newDocument();
 
         Node r = output.importNode(root, true);
         output.appendChild(r);
 
         // now add scales, if any
         if (scales == null || scales.isEmpty()) {
             scaleSldDpi(output, dpi, crs, refXY);
         } else if (scales.size() == 1) {
             scaleSldSingle(output, scales.get(0), dpi, crs, refXY);
         } else if (scales.size() > 1) {
             scaleSldMultiple(output, scales, dpi, crs, refXY);
         }
 
         return output;
     }
 
     /**
      * Scale only taking the DPI value into account.
      *
      * @param document the SLD document to scale.
      * @param dpi the DPI value of the target device
      * @param crs the CRS reference to use
      * @param refXY the x and y coordinates of a reference point in CRS space
      * @throws XPathExpressionException on XPath errors
      */
     private static void
     scaleSldDpi(Document                    document,
                 double                      dpi,
                 CoordinateReferenceSystem   crs,
                 double[]                    refXY)
                                               throws XPathExpressionException {
 
         XPath xpath = XPathFactory.newInstance().newXPath();
         xpath.setNamespaceContext(getNsCtx());
 
         NodeList rules = (NodeList) xpath.evaluate(
                                                 "//" + SLD_NS_PREFIX + ":Rule",
                                                 document,
                                                 XPathConstants.NODESET);
 
         for (int i = 0; i < rules.getLength(); ++i) {
             Element rule = (Element) rules.item(i);
 
             // scale both old and new rule elements
             scaleValues(rule, 0, dpi, crs, refXY);
         }
     }
 
     /**
      * Scale with a single scaling value, that is, just re-scale all the values
      * for this single scale value.
      *
      * @param document the SLD document to scale.
      * @param scale the single scale point to split rendering by
      * @param dpi the DPI value of the target device
      * @param crs the CRS reference to use
      * @param refXY the x and y coordinates of a reference point in CRS space
      * @throws XPathExpressionException on XPath errors
      */
     private static void
     scaleSldSingle(Document                    document,
                    double                      scale,
                    double                      dpi,
                    CoordinateReferenceSystem   crs,
                    double[]                    refXY)
                                                throws XPathExpressionException {
 
         XPath xpath = XPathFactory.newInstance().newXPath();
         xpath.setNamespaceContext(getNsCtx());
 
         NodeList rules = (NodeList) xpath.evaluate(
                                                 "//" + SLD_NS_PREFIX + ":Rule",
                                                 document,
                                                 XPathConstants.NODESET);
 
         for (int i = 0; i < rules.getLength(); ++i) {
             Element rule = (Element) rules.item(i);
 
             // scale both old and new rule elements
             scaleValues(rule, scale, dpi, crs, refXY);
         }
     }
 
     /**
      * Scale with multiple scaling values.
      * The result will be scales.size() + 1 sld:Rule elements created
      * for each sld:Rule, with proper scale denominators added.
      *
      * @param document the SLD document to scale.
      * @param scales the scaling points
      * @param dpi the dpi value of the target device
      * @param crs the CRS reference to use
      * @param refXY the x and y coordinates of a reference point in CRS space
      * @throws XPathExpressionException on XPath errors
      */
     private static void
     scaleSldMultiple(Document                   document,
                      List<Double>               scales,
                      double                     dpi,
                      CoordinateReferenceSystem  crs,
                      double[]                   refXY)
                                              throws XPathExpressionException {
 
         // don't solve this here if there are less than 2 scaling values
         if (scales.isEmpty()) {
             return;
         } else if (scales.size() == 1) {
             scaleSldSingle(document, scales.get(0), dpi, crs, refXY);
             return;
         }
 
         // convert the scale values into scaling intervals, that are suitable
         // as min and max denominators in the SLD
         List<Double> scaleIntervals = generateIntervals(scales);
 
         XPath xpath = XPathFactory.newInstance().newXPath();
         xpath.setNamespaceContext(getNsCtx());
 
         NodeList rules = (NodeList) xpath.evaluate(
                                                 "//" + SLD_NS_PREFIX + ":Rule",
                                                 document,
                                                 XPathConstants.NODESET);
 
         for (int i = 0; i < rules.getLength(); ++i) {
             Element          rule = (Element) rules.item(i);
             DocumentFragment df   = document.createDocumentFragment();
 
             // the first duplicate will have a single max scale denominator
             Element ruleCopy = (Element) rule.cloneNode(true);
             double  sMax     = scaleIntervals.get(0);
             int     nScales1 = scaleIntervals.size() - 1;
 
             // insert the max scale denominator
             insertMaxScaleDenominator(ruleCopy, sMax);
             // scale the values
             scaleValues(ruleCopy, sMax, dpi, crs, refXY);
             // store this node, will be added later
             df.appendChild(ruleCopy);
 
             // process the intermediate rules, that have box max and min scale
             // denominators
             for (int j = 1; j <= nScales1; ++j) {
                 double sMin = sMax;
                 sMax        = scaleIntervals.get(j);
                 ruleCopy    = (Element) rule.cloneNode(true);
 
                 // insert the min and max scale denominators
                 insertMaxScaleDenominator(ruleCopy, sMax);
                 insertMinScaleDenominator(ruleCopy, sMin);
 
                 // scale the values inside this rule
                 scaleValues(ruleCopy, (sMax + sMin) / 2, dpi, crs, refXY);
 
                 df.appendChild(ruleCopy);
             }
 
             // update the original rule with a min scale denominator
             double sMin = scaleIntervals.get(nScales1);
             insertMinScaleDenominator(rule, sMin);
             scaleValues(rule, sMin, dpi, crs, refXY);
 
             // insert the new rules before the original one
             rule.getParentNode().insertBefore(df, rule);
         }
     }
 
     /**
      * Create scale intervals based on the supplied scale values. The number of
      * scale values is at least two. The intervals generated will be such that
      * the lowest will be a 75% below the lowest scale value, the values in
      * between will be halfway between values, and the highest value will be
      * at 150% of the highest value.
      *
      * @param scales the scales to generate intervals for, in highest scale
      *        first order (that is, lowest scale value first, e.g 1:100 first,
      *        then 1:200 second, etc)
      * @return scale intervals generated from the supplied scales
      */
     static List<Double> generateIntervals(List<Double> scales) {
         List<Double> scaleIntervals = new ArrayList<Double>(scales.size() + 1);
 
         // add the lowest scale
         scaleIntervals.add(scales.get(0) * 0.75d);
 
         // add intermediate scales
         double p = scales.get(0);
         for (int i = 1; i < scales.size(); ++i) {
             double s = scales.get(i);
             scaleIntervals.add((p + s) / 2.0d);
             p = s;
         }
 
         // add the highest scale
         scaleIntervals.add(scales.get(scales.size() - 1) * 1.5d);
 
         return scaleIntervals;
     }
 
     /**
      * Insert an sdl:MinScaleDenominator element into an sld:Rule element.
      *
      * @param rule the sld:Rule element to insert into
      * @param scale the scale value of the scale denominator element.
      * @throws XPathExpressionException on XPath errors
      */
     private static void
     insertMinScaleDenominator(Element rule, double scale)
                                             throws XPathExpressionException {
 
         insertScaleDenominator(rule, scale,
                                SLD_NS_PREFIX + ":MinScaleDenominator");
     }
 
     /**
      * Insert an sdl:MaxScaleDenominator element into an sld:Rule element.
      *
      * @param rule the sld:Rule element to insert into
      * @param scale the scale value of the scale denominator element.
      * @throws XPathExpressionException on XPath errors
      */
     private static void
     insertMaxScaleDenominator(Element rule, double scale)
                                             throws XPathExpressionException {
 
         insertScaleDenominator(rule, scale,
                                SLD_NS_PREFIX + ":MaxScaleDenominator");
     }
 
     /**
      * Insert a scale denominator element of the specified name.
      *
      * @param rule the sld:Rule element to insert into
      * @param scale the scale value of the scale denominator element.
      * @param elementName the name of the scale denominator element
      * @throws XPathExpressionException on XPath errors
      */
     private static void
     insertScaleDenominator(Element rule, double scale, String elementName)
                                             throws XPathExpressionException {
 
         XPath xpath = XPathFactory.newInstance().newXPath();
         xpath.setNamespaceContext(getNsCtx());
 
         // create the scale denominator element
         Document d   = rule.getOwnerDocument();
         Element  msd = d.createElementNS(SLD_NS_URI, elementName);
         msd.setTextContent(Double.toString(scale));
 
         // insert the scale denominator into its proper place
         Node n = (Node) xpath.evaluate(SLD_NS_PREFIX + ":Name", rule,
                                        XPathConstants.NODE);
         if (n != null) {
             rule.insertBefore(msd, n.getNextSibling());
         } else {
             rule.insertBefore(msd, rule.getFirstChild());
         }
     }
 
     /**
      * Scale the numeric values inside an element.
      * Scaling is done according to the scaling factor and the target device
      * DPI, and taking units of measurement into account.
      *
      * @param node the node in which to scale all values
      * @param scale the scaling factor used
      * @param dpi the DPI value of the target device
      * @param crs the CRS reference to use
      * @param refXY the x and y coordinates of a reference point in CRS space
      * @throws XPathExpressionException on XPath errors
      */
     private static void
     scaleValues(Node                      node,
                 double                    scale,
                 double                    dpi,
                 CoordinateReferenceSystem crs,
                 double[]                  refXY)
                                             throws XPathExpressionException {
 
         XPath xpath = XPathFactory.newInstance().newXPath();
         xpath.setNamespaceContext(getNsCtx());
 
         NodeList nodes = (NodeList) xpath.evaluate(".//text()", node,
                                                        XPathConstants.NODESET);
 
         for (int i = 0; i < nodes.getLength(); ++i) {
             Text         n  = (Text) nodes.item(i);
             String       s  = n.getNodeValue().trim();
 
             if (s.isEmpty()) {
                 continue;
             }
 
             StringBuffer sb = new StringBuffer();
 
             xpath.reset();
             boolean inFunction = (Boolean) xpath.evaluate(
                         "count(ancestor::" + OGC_NS_PREFIX + ":Function) > 0",
                         n, XPathConstants.BOOLEAN);
 
             StringTokenizer tok = new StringTokenizer(s, " ");
             while (tok.hasMoreTokens()) {
                 String t = tok.nextToken();
 
                 if (t.isEmpty() || UOM.uomPostfix(t) == null) {
                     sb.append(t);
                     sb.append(' ');
                     continue;
                 }
 
                 try {
                     if (inFunction) {
                         double d = UOM.scaleValueCrs(t, scale, crs, refXY);
                         sb.append(Double.toString(d));
                         sb.append(' ');
                     } else {
                         double d = UOM.scaleValue(t, scale, dpi);
                        sb.append(Double.toString(d));
                         sb.append(' ');
                     }
                 } catch (RenderException e) {
                     sb.append(t);
                     sb.append(' ');
                 }
             }
 
             n.setTextContent(sb.toString().trim());
         }
     }
 
 }
