 /*
  *  StrangeFind, an anomaly detection system for the OpenDiamond Platform
  *
  *  Copyright (c) 2007-2008 Carnegie Mellon University
  *  All rights reserved.
  *
  *  StrangeFind is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, version 2.
  *
  *  StrangeFind is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with StrangeFind. If not, see <http://www.gnu.org/licenses/>.
  *
  *  Linking StrangeFind statically or dynamically with other modules is
  *  making a combined work based on StrangeFind. Thus, the terms and
  *  conditions of the GNU General Public License cover the whole
  *  combination.
  * 
  *  In addition, as a special exception, the copyright holders of
  *  StrangeFind give you permission to combine StrangeFind with free software
  *  programs or libraries that are released under the GNU LGPL or the
  *  Eclipse Public License 1.0. You may copy and distribute such a system
  *  following the terms of the GNU GPL for StrangeFind and the licenses of
  *  the other code concerned, provided that you include the source code of
  *  that other code when and as the GNU GPL requires distribution of source
  *  code.
  *
  *  Note that people who make modified versions of StrangeFind are not
  *  obligated to grant this special exception for their modified versions;
  *  it is their choice whether to do so. The GNU General Public License
  *  gives permission to release a modified version without this exception;
  *  this exception also makes it possible to release a modified version
  *  which carries forward this exception.
  */
 
 package edu.cmu.cs.diamond.strangefind.search;
 
 import java.awt.*;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.Arc2D;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.text.DecimalFormat;
 import java.util.*;
 import java.util.List;
 
 import javax.swing.*;
 
 import edu.cmu.cs.diamond.opendiamond.*;
 import edu.cmu.cs.diamond.strangefind.*;
 
 public class CircleAnomalyFilter implements StrangeFindSearch {
     public enum CircleFill {
         CIRCLE_FILL_DASHED, CIRCLE_FILL_SOLID, CIRCLE_FILL_HAIRLINE
     }
 
     public static class Circle {
         public float x;
 
         public float y;
 
         public float a;
 
         public float b;
 
         public float t;
 
         public boolean inResult;
 
         @Override
         public String toString() {
             return "(" + x + "," + y + "), [" + a + " x " + b + "] t: " + t
                     + ", inResult: " + inResult;
         }
     }
 
     private static final String[] LABELS = { "circle-count",
             "circle-area-fraction", "circle-area-m1", "circle-area-cm2",
             "circle-area-cm3", "circle-area-cm4", "circle-eccentricity-m1",
             "circle-eccentricity-cm2", "circle-eccentricity-cm3",
             "circle-eccentricity-cm4" };
 
     private static final String[] NICE_LABELS = { "Count", "Area fraction",
             "Area mean", "Area variance", "Area skewness", "Area kurtosis",
             "Eccentricity mean", "Eccentricity variance",
             "Eccentricity skewness", "Eccentricity kurtosis" };
 
     public CircleAnomalyFilter() {
         // init GUI elements
         for (int i = 0; i < NICE_LABELS.length; i++) {
             checkboxes[i] = new JCheckBox(NICE_LABELS[i]);
 
             JSpinner s = new JSpinner(
                     new SpinnerNumberModel(3.0, 1.0, 7.0, 0.5));
             stddevs[i] = s;
         }
 
         checkboxes[0].setSelected(true);
         checkboxes[1].setSelected(true);
         checkboxes[6].setSelected(true);
     }
 
     public String[] getApplicationDependencies() {
         return new String[] { "rgb" };
     }
 
     public Filter[] getFilters() {
         Filter rgb = null;
         Filter thumb = null;
         Filter circles = null;
         Filter anom = null;
         try {
             FilterCode c;
             c = new FilterCode(new FileInputStream("/tmp/fil_rgb.so"));
             rgb = new Filter("rgb", c, "f_eval_img2rgb", "f_init_img2rgb",
                     "f_fini_img2rgb", 1, Arrays.asList(new String[0]), Arrays
                             .asList(new String[0]));
 
             c = new FilterCode(new FileInputStream("/tmp/fil_thumb.so"));
             thumb = new Filter("thumb", c, "f_eval_thumbnailer",
                     "f_init_thumbnailer", "f_fini_thumbnailer", 1, Arrays
                             .asList(new String[] { "rgb" }), Arrays
                             .asList(new String[] {
                                     Integer.toString(ResultViewer
                                             .getPreferredWidth()),
                                     Integer.toString(ResultViewer
                                             .getPreferredHeight()) }));
 
             c = new FilterCode(new FileInputStream("/tmp/fil_circle.so"));
             circles = new Filter("circles", c, "f_eval_circles",
                     "f_init_circles", "f_fini_circles", 0, Arrays
                             .asList(new String[] { "rgb" }), Arrays
                             .asList(new String[] { "-1", "-1", "0.4", "1" }));
             System.out.println(circles);
 
             List<String> paramsList = new ArrayList<String>();
             for (int i = 0; i < checkboxes.length; i++) {
                 paramsList.add(LABELS[i]);
                 paramsList.add(stddevs[i].getValue().toString());
             }
 
             StringBuilder logicalExpression = new StringBuilder();
             boolean anySelected = false;
             boolean exactlyOneSelected = false;
             String lastSelected = null;
             logicalExpression.append("OR(");
             for (int i = 0; i < checkboxes.length; i++) {
                 if (checkboxes[i].isSelected()) {
                     if (anySelected) {
                         exactlyOneSelected = false;
                         logicalExpression.append(",");
                     } else {
                         exactlyOneSelected = true;
                     }
                     lastSelected = "$" + (i + 1);
                     logicalExpression.append(lastSelected);
                     anySelected = true;
                 }
             }
             logicalExpression.append(")");
 
             if (!anySelected) {
                 logicalExpression = new StringBuilder(); // clear
             } else if (exactlyOneSelected) {
                 logicalExpression = new StringBuilder(lastSelected);
             }
 
             String anomArgs[] = new String[paramsList.size() + 3];
             anomArgs[0] = ignoreSpinner.getValue().toString(); // skip
             anomArgs[1] = UUID.randomUUID().toString(); // random value
             anomArgs[2] = LogicEngine
                     .getMachineCodeForExpression(logicalExpression.toString());
             System.arraycopy(paramsList.toArray(), 0, anomArgs, 3, paramsList
                     .size());
             c = new FilterCode(new FileInputStream("/tmp/fil_anomaly.so"));
             anom = new Filter("anomaly", c, "f_eval_afilter", "f_init_afilter",
                     "f_fini_afilter", 1, Arrays
                             .asList(new String[] { "circles" }), Arrays
                             .asList(anomArgs));
             System.out.println(anom);
 
         } catch (FileNotFoundException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
 
         return new Filter[] { rgb, thumb, circles, anom };
     }
 
     final private JCheckBox[] checkboxes = new JCheckBox[LABELS.length];
 
     final private JSpinner[] stddevs = new JSpinner[LABELS.length];
 
     final private JSpinner ignoreSpinner = new JSpinner(new SpinnerNumberModel(
             5, 0, 100, 1));
 
     public JPanel getInterface() {
         // XXX do this another way
         JPanel result = new JPanel();
         result.setBorder(BorderFactory
                 .createTitledBorder("Circle Anomaly Detector"));
         result.setLayout(new SpringLayout());
 
         result.add(new JLabel("Priming count"));
         result.add(ignoreSpinner);
 
         result.add(new JLabel(" "));
         result.add(new JLabel(" "));
 
         result.add(new JLabel("Descriptor"));
         result.add(new JLabel("Std. dev."));
         for (int i = 0; i < LABELS.length; i++) {
             result.add(checkboxes[i]);
             result.add(stddevs[i]);
         }
 
         Util.makeCompactGrid(result, LABELS.length + 3, 2, 5, 5, 2, 2);
 
         return result;
     }
 
     public Decorator getDecorator() {
         return new Decorator() {
             public void decorate(AnnotatedResult r, Graphics2D g, double scale) {
                 byte data[] = r.getValue("circle-data");
                 if (data == null) {
                     return;
                 }
 
                 List<Circle> circles = extractCircles(data);
                 for (Circle circle : circles) {
                     drawCircle(g, circle, scale,
                             circle.inResult ? CircleFill.CIRCLE_FILL_SOLID
                                     : CircleFill.CIRCLE_FILL_DASHED);
                 }
             }
         };
     }
 
     private static final DoubleComposer composer = new DoubleComposer() {
         public double compose(String key, double a, double b) {
             return a + b;
         }
     };
 
     public DoubleComposer getDoubleComposer() {
         return composer;
     }
 
     protected void drawCircle(Graphics2D g, Circle circle, double scale,
             CircleFill fill) {
         float x = circle.x;
         float y = circle.y;
         float a = circle.a;
         float b = circle.b;
         float t = circle.t;
 
         g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                 RenderingHints.VALUE_ANTIALIAS_ON);
 
         // draw
         Arc2D arc = new Arc2D.Double(-1, -1, 2, 2, 0, 360, Arc2D.CHORD);
 
         AffineTransform at = new AffineTransform();
         at.scale(scale, scale);
 
         at.translate(x, y);
         at.rotate(t);
         at.scale(a, b);
 
         Shape s = at.createTransformedShape(arc);
         switch (fill) {
         case CIRCLE_FILL_DASHED:
             g.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
                     BasicStroke.JOIN_BEVEL, 1f, new float[] { 5.0f }, 0));
             g.setPaint(Color.RED);
             g.draw(s);
             break;
 
         case CIRCLE_FILL_SOLID:
             g.setStroke(new BasicStroke(2.0f));
             g.setPaint(new Color(1.0f, 0.0f, 0.0f, 0.2f));
             g.fill(s);
             g.setPaint(Color.RED);
             g.draw(s);
             break;
 
         case CIRCLE_FILL_HAIRLINE:
             g.setStroke(new BasicStroke(1.0f));
             g.setPaint(Color.RED);
             g.draw(s);
             break;
         }
     }
 
     public Annotator getAnnotator() {
         final List<String> selectedLabels = new ArrayList<String>();
         final List<String> niceSelectedLabels = new ArrayList<String>();
         for (int i = 0; i < checkboxes.length; i++) {
             JCheckBox c = checkboxes[i];
             if (c.isSelected()) {
                 selectedLabels.add(LABELS[i]);
                 niceSelectedLabels.add(NICE_LABELS[i]);
             }
         }
 
         return new Annotator() {
             public String annotate(Result r) {
 
                 boolean useHTML = true;
 
                 DecimalFormat df = new DecimalFormat("0.###");
                 StringBuilder sb = new StringBuilder();
 
                 if (useHTML) {
                     sb.append("<html>");
                 }
 
                 String server = r.getServerName();
                 String name = getName(r);
                 int samples = getSamples(r, 0);
 
                 for (int i = 0; i < LABELS.length; i++) {
                     boolean isA = getIsAnomalous(r, i);
 
                     if (useHTML) {
                         sb.append("<p>");
                     } else {
                         sb.append("\n\n");
                     }
                     if (isA) {
                         if (useHTML) {
                             sb.append("<b>");
                         }
                         sb.append("*");
                     }
 
                     String descriptor = NICE_LABELS[i];
                     double stddev = getStddev(r, i);
                     double value = getValue(r, i);
                     double mean = getMean(r, i);
                     double stddevDiff = getStddevDiff(stddev, value, mean);
                     String aboveOrBelow = getAboveOrBelow(stddevDiff, "+", "−");
 
                     sb.append(descriptor);
                     if (isA) {
                         if (useHTML) {
                             sb.append("</b>");
                         }
                     }
                     sb.append(" = " + format(mean, df) + " " + aboveOrBelow
                             + " " + format(Math.abs(stddevDiff), df) + "σ ("
                             + format(value, df) + ")");
                 }
 
                 if (useHTML) {
                     sb.append("<hr><p>" + name + "<p>" + server + " ["
                             + samples + "]</html>");
                 } else {
                     sb.append("\n---\n\n" + name + "\n\n" + server + " ["
                             + samples + "]");
                 }
 
                 return sb.toString();
             }
 
             private double getValue(Result r, int descriptor) {
                 double value = Util.extractDouble(r
                         .getValue("anomaly-descriptor-value-" + descriptor
                                 + ".double"));
                 return value;
             }
 
             private double getStddevDiff(double stddev, double value,
                     double mean) {
                 double stddevDiff = (value - mean) / stddev;
                 return stddevDiff;
             }
 
             private double getMean(Result r, int descriptor) {
                 double mean = Util.extractDouble(r
                         .getValue("anomaly-descriptor-mean-" + descriptor
                                 + ".double"));
                 return mean;
             }
 
             private double getStddev(Result r, int descriptor) {
                 double stddev = Util.extractDouble(r
                         .getValue("anomaly-descriptor-stddev-" + descriptor
                                 + ".double"));
                 return stddev;
             }
 
             private boolean getIsAnomalous(Result r, int descriptor) {
                 int isAnomalous = Util.extractInt(r
                         .getValue("anomaly-descriptor-is_anomalous-"
                                 + descriptor + ".int"));
                 return isAnomalous == 1 ? true : false;
             }
 
             private String getName(Result r) {
                 String name = r.getName();
                 name = name.substring(name.lastIndexOf('/') + 1);
                 return name;
             }
 
             private String getAboveOrBelow(double stddevDiff, String above,
                     String below) {
                 String aboveOrBelow = Math.signum(stddevDiff) >= 0.0 ? above
                         : below;
                 return aboveOrBelow;
             }
 
             private int getSamples(Result r, int descriptor) {
                 int samples = Util.extractInt(r
                         .getValue("anomaly-descriptor-count-" + descriptor
                                 + ".int"));
                 return samples;
             }
 
             public String annotateTooltip(Result r) {
                 return annotate(r);
             }
 
             @Override
             public String annotateOneLine(Result r) {
                 return null;
             }
 
             @Override
             public String annotateVerbose(Result r) {
                 return null;
             }
 
             @Override
             public String annotateNonHTML(Result r) {
                 return "TODO: not implemented";
             }
         };
     }
 
     protected String format(double d, DecimalFormat df) {
         if (Double.isNaN(d)) {
             return "NaN";
         } else {
             return df.format(d);
         }
     }
 
     static protected List<Circle> extractCircles(byte[] data) {
         List<Circle> circles = new ArrayList<Circle>();
        final int sizeOfFloatPart = 4 * 5;
 
        for (int i = 0; i < data.length; i += sizeOfFloatPart + 1) {
            Circle c = new Circle();
 
            ByteBuffer b = ByteBuffer.wrap(data, i, sizeOfFloatPart + 1);
            b.order(ByteOrder.LITTLE_ENDIAN);
 
             c.x = b.getFloat();
             c.y = b.getFloat();
             c.a = b.getFloat();
             c.b = b.getFloat();
             c.t = b.getFloat();
            c.inResult = b.get() != 0;
 
             circles.add(c);
         }
 
         return circles;
     }
 
     @Override
     public Set<String> getPushAttributes() {
         Set<String> set = new HashSet<String>();
         addAnomalyAttributesToSet(set);
         set.add("thumbnail.jpeg");
         set.add("circle-data");
         set.add("Device-Name");
         set.add("Display-Name");
         set.add("_cols.int");
         set.add("_rows.int");
 
         return set;
     }
 
     private void addAnomalyAttributesToSet(Set<String> set) {
         String a = "anomaly-descriptor-";
         for (int i = 0; i < LABELS.length; i++) {
             set.add(a + "value-" + i + ".double");
             set.add(a + "mean-" + i + ".double");
             set.add(a + "stddev-" + i + ".double");
             set.add(a + "count-" + i + ".int");
             set.add(a + "is_anomalous-" + i + ".int");
         }
     }
 }
