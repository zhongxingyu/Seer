 package org.jboss.perfrunner;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.junit.runner.Description;
 import org.junit.runner.Result;
 import org.junit.runner.notification.RunListener;
 
 /**
  * Listens to tests execution events from {@link PerfRunner}, generating an HTML
  * file that contains charts of the performance data.
  * <p>
  * The HTML files are created in the current directory, and have names of the form
  * <code>perfrunner-<i>fully-qualified-class-name</i>.html</code>.
  * <p>
  * TODO: use system properties to control where the output file will be created
  *
  * @author Jonathan Fuerth <jfuerth@gmail.com>
  */
 public class PerformanceReportBuilder extends RunListener {
 
   /**
    * A method parameter name and the value that was given for it during a
    * particular method invocation.
    */
   public static class ParamValue {
     private final String paramName;
     private final double paramValue;
 
     public ParamValue(String paramName, double paramValue) {
       this.paramName = paramName;
       this.paramValue = paramValue;
     }
 
     public String getParamName() {
       return paramName;
     }
 
     public double getParamValue() {
       return paramValue;
     }
 
     @Override
     public int hashCode() {
       final int prime = 31;
       int result = 1;
       result = prime * result
           + ((paramName == null) ? 0 : paramName.hashCode());
       long temp;
       temp = Double.doubleToLongBits(paramValue);
       result = prime * result + (int) (temp ^ (temp >>> 32));
       return result;
     }
 
     @Override
     public boolean equals(Object obj) {
       if (this == obj)
         return true;
       if (obj == null)
         return false;
       if (getClass() != obj.getClass())
         return false;
       ParamValue other = (ParamValue) obj;
       if (paramName == null) {
         if (other.paramName != null)
           return false;
       }
       else if (!paramName.equals(other.paramName))
         return false;
       if (Double.doubleToLongBits(paramValue) != Double
           .doubleToLongBits(other.paramValue))
         return false;
       return true;
     }
 
     @Override
     public String toString() {
       return paramName + "=" + paramValue;
     }
   }
 
   /**
    * The unique identifier for a Series. Instances are immutable. Suitable for
    * use as a key in a hash collection.
    */
   private static class Key {
 
     /**
      * Creates the Key for a page or series from the given run description.
      * <p>
      * Note that this could be refactored in the future to return a list (or
      * set) of keys based on the axis annotations present in the run
      * description. This would be better because it could support an arbitrary
      * number of axes.
      *
      * @param desc
      *          The descriptor for a particular invocation of a test method.
      * @param paramIndexes
      *          To create a page key, pass in the list of parameter indexes that
      *          were annotated with axis=PAGE; to create the key for a series,
      *          pass in the list of parameter indexes that were annotated with
      *          axis=SERIES.
      * @return a Key value representing the page or series key this method
      *         invocation belongs to.
      */
     public static Key create(PerfRunDescription desc, List<Integer> paramIndexes) {
       List<ParamValue> paramValues = new ArrayList<ParamValue>();
       for (int i : paramIndexes) {
         paramValues.add(new ParamValue(
             desc.getParamAnnotations().get(i).name(),
             desc.getParamValues().get(i)));
       }
       Key key = new Key(paramValues);
       return key;
     }
 
     private final List<ParamValue> paramValues;
 
     /**
      * Creates a new key (for a page or a series) based on the given param values.
      *
      * @param paramValues
      *          the collection of parameter values that define this series. A
      *          copy is made of this list, so you are free to modify the one
      *          that you passed in.
      */
     public Key(List<ParamValue> paramValues) {
       this.paramValues = new ArrayList<ParamValue>(paramValues);
     }
     @Override
     public int hashCode() {
       final int prime = 31;
       int result = 1;
       result = prime * result
           + ((paramValues == null) ? 0 : paramValues.hashCode());
       return result;
     }
     @Override
     public boolean equals(Object obj) {
       if (this == obj)
         return true;
       if (obj == null)
         return false;
       if (getClass() != obj.getClass())
         return false;
       Key other = (Key) obj;
       if (paramValues == null) {
         if (other.paramValues != null)
           return false;
       }
       else if (!paramValues.equals(other.paramValues))
         return false;
       return true;
     }
     @Override
     public String toString() {
       return paramValues.toString();
     }
   }
 
   /**
    * A point on the performance chart. Instances of this class are immutable.
    */
   private static class Point {
     private final double x;
     private final double y;
     public Point(double x, double y) {
       this.x = x;
       this.y = y;
     }
 
     /**
      * Appends a JavaScript representation of this point to {@code sb}.
      *
      * @param sb target for the generated JavaScript
      */
     public void appendTo(Appendable sb) throws IOException {
       sb.append("[").append(String.valueOf(x)).append(",").append(String.valueOf(y)).append("]");
     }
   }
 
   /**
    * Represents a series of observations that should be expressed as a single
    * line on the performance chart.
    */
   private static class Series {
     private final Key key;
     private final List<Point> points = new ArrayList<Point>();
 
     public Series(Key key) {
       this.key = key;
     }
 
     public void addPoint(double x, double y) {
       points.add(new Point(x, y));
     }
 
     /**
      * Appends a JavaScript representation of this series to {@code sb}.
      *
      * @param sb target for the generated JavaScript
      */
     public void appendTo(Appendable sb) throws IOException {
       sb.append("\n {");
 
       // only label the series if we have something to call it. :)
       if (key.paramValues.size() > 0) {
         sb.append("label: \"").append(key.toString()).append("\", ");
       }
 
       sb.append("data: [");
       boolean first = true;
       for (Point p : points) {
         if (!first) {
           sb.append(",");
         }
         p.appendTo(sb);
         first = false;
       }
       sb.append("]}");
     }
   }
 
   /**
    * Represents the data accumulated from all invocations of a particular test method.
    * This will correspond to one or more charts in the output file.
    *
    * @author Jonathan Fuerth <jfuerth@gmail.com>
    */
   private static class MethodRunData {
     private final Map<Key, Map<Key, Series>> pageSeriesMap = new LinkedHashMap<Key, Map<Key, Series>>();
     private final String className;
     private final String methodName;
 
     private int xAxisParam = -1;
     private List<Integer> pageAxisParams;
     private List<Integer> seriesParams;
 
     public MethodRunData(PerfRunDescription desc) {
       this.className = desc.getClassName();
       this.methodName = desc.getMethodName();
 
       List<Integer> pageAxisParams = new ArrayList<Integer>();
       List<Integer> seriesParams = new ArrayList<Integer>();
 
       for (int i = 0; i < desc.getParamAnnotations().size(); i++) {
         Varying v = desc.getParamAnnotations().get(i);
         if (v.axis() == Axis.X) {
           if (xAxisParam != -1) {
             throw new IllegalStateException(
                 "Found more than one x-axis parameter for test " + desc.getClassName() + "." + desc.getMethodName());
           }
           xAxisParam = i;
         }
         else if (v.axis() == Axis.SERIES) {
           seriesParams.add(i);
         }
         else if (v.axis() == Axis.PAGE) {
           pageAxisParams.add(i);
         }
         else {
           throw new AssertionError("Oops, unknown Axis type " + v.axis());
         }
       }
 
       if (xAxisParam == -1) {
         throw new IllegalStateException(
             "No x-axis parameter was specified for test " + desc.getClassName() + "." + desc.getMethodName());
       }
 
       this.pageAxisParams = Collections.unmodifiableList(pageAxisParams);
       this.seriesParams = Collections.unmodifiableList(seriesParams);
     }
 
     public boolean isSameChart(PerfRunDescription desc) {
       return desc.getClassName().equals(className) && desc.getMethodName().equals(methodName);
     }
 
     /**
      * Adds a data point for a single invocation of the test method.
      *
      * @param desc
      *          Description of this method invocation. Must correspond with the
      *          same descrption given to this MethodRunData's constructor.
      * @param yValue
      *          The Y-axis value to plot for this invocation. Initially, the
      *          only supported y-axis value is time. In the future, we'll
      *          probably introduce annotations for requesting other metrics.
      */
     public void addTestRunData(PerfRunDescription desc, double yValue) {
       if (!isSameChart(desc)) throw new IllegalArgumentException("The given description is for data that doesn't belong on this chart");
 
       Key pageKey = Key.create(desc, pageAxisParams);
       Map<Key, Series> seriesMap = pageSeriesMap.get(pageKey);
       if (seriesMap == null) {
         seriesMap = new LinkedHashMap<Key, Series>();
         pageSeriesMap.put(pageKey, seriesMap);
       }
 
       Key seriesKey = Key.create(desc, seriesParams);
       Series s = seriesMap.get(seriesKey);
       if (s == null) {
         s = new Series(seriesKey);
         seriesMap.put(seriesKey, s);
       }
 
       s.addPoint(desc.getParamValues().get(xAxisParam), yValue);
     }
 
     /**
      * Appends a chart of this method's accumulated run data to {@code sb}.
      *
      * @param sb
      *          The target for the HTML + JavaScript code.
      * @param chartNum
      *          The unique identifier (within the document being appended to by
      *          {@code sb}) for the chart.
      * @throws IOException
      *           if appending to {@code sb} fails.
      */
     public void appendJavascriptTo(Appendable sb, int chartNum) throws IOException {
       sb.append("\n<h2><span class=packageName>" + className + ".</span>" + methodName + "</h2>\n");
 
       for (Map.Entry<Key, Map<Key, Series>> e : pageSeriesMap.entrySet()) {
         Key pageKey = e.getKey();
         Map<Key, Series> series = e.getValue();
 
         if (pageKey.paramValues.size() > 0) {
           sb.append("<h3>" + pageKey.toString() + "</h3>");
         }
         sb.append("<div class=chart id=chart" + chartNum + "></div>\n");
         sb.append("<div class=legend id=legend" + chartNum + "></div>\n");
         sb.append("<script type='text/javascript'>\n");
         sb.append("$(function() {\n");
         sb.append(" $.plot($('#chart" + chartNum + "'), ");
 
         // chart data series
         sb.append("[");
         boolean first = true;
         for (Series s : series.values()) {
           if (!first) {
             sb.append(",");
           }
           s.appendTo(sb);
           first = false;
         }
         sb.append("\n]\n");
 
         // chart options
         sb.append(", {\n");
         sb.append("    series: { points: {show: true}, lines: {show: true} },\n");
         sb.append("    legend: { hideable: true, container: '#legend" + chartNum + "', noColumns: 2 }\n");
 
         sb.append("  });\n");
         sb.append("});\n");
         sb.append("</script>\n");
         chartNum++;
       }
     }
   }
 
   /**
    * Output stream for the HTML file we're creating.
    */
   private PrintWriter out;
 
   /**
    * Time the most recent test started.
    */
   private long startTime;
 
   /**
    * Object that is accumulating run data for the current method.
    */
   private MethodRunData runData;
 
   /**
    * Keeps track of how many charts we've made in the output file. Needed for
    * creating unique div identifiers.
    */
   private int chartNum;
 
   @Override
   public void testRunStarted(Description description) throws FileNotFoundException {
     out = new PrintWriter("perfrunner-" + description.getClassName() + ".html");
     out.println("<!DOCTYPE html>");
     out.println("<html>");
     out.println("<head>");
     out.println(" <title>" + description.getDisplayName() + "</title>");
     out.println(" <link rel=stylesheet type='text/css' href='perfrunner-style.css'></script>");
     out.println(" <script type='text/javascript' src='jquery.js'></script>");
     out.println(" <script type='text/javascript' src='jquery.flot.js'></script>");
     out.println(" <script type='text/javascript' src='jquery.flot.hiddengraphs.js'></script>");
     out.println("</head>");
     out.println("<body>");
     out.println(" <h1>" + description.getDisplayName() + "</h1>");
    out.println(" <p class=generatedBy>Generated by <a href='https://github.com/jfuerth/junit-4-perfrunner'>PerfRunner</a></p>");
     out.println(" <p class=generatedOn>On " + new Date() + "</p>");
   }
 
   @Override
   public void testStarted(Description description) {
     startTime = System.nanoTime();
   }
 
   @Override
   public void testFinished(Description description) {
     // compute the test duration in milliseconds
     double testDuration = (System.nanoTime() - startTime) / 1000000.0;
 
     try {
       PerfRunDescription desc = new PerfRunDescription(description);
 
       // check if run data needs to be dumped or (re)created
       if (runData == null) {
         runData = new MethodRunData(desc);
       }
       else if (!runData.isSameChart(desc)) {
         // done with this one. dump the chart data!
         runData.appendJavascriptTo(out, chartNum++);
         runData = new MethodRunData(desc);
       }
 
       // in all cases, we add this observation
       runData.addTestRunData(desc, testDuration);
 
     } catch (IOException e) {
       throw new RuntimeException(e);
     }
   }
 
   @Override
   public void testRunFinished(Result result) {
     if (runData != null) {
       try {
         runData.appendJavascriptTo(out, chartNum++);
       } catch (IOException e) {
         throw new RuntimeException(e);
       }
     }
     out.println("</body>");
     out.flush();
     out.close();
   }
 }
