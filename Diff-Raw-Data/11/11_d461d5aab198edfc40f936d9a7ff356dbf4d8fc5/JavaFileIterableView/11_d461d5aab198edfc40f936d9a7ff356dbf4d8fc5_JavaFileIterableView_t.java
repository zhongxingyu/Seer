 /* Copyright (c) 2007, http://www.codeviation.org project 
  * This program is made available under the terms of the MIT License. 
  */
 
 package hudson.plugins.codeviation;
 
 import hudson.model.ModelObject;
 import hudson.util.ChartUtil;
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.List;
 import java.util.logging.Logger;
 import javax.servlet.http.Cookie;
 import org.codeviation.model.JavaFile;
 import org.codeviation.statistics.ChartConf;
 import org.codeviation.statistics.ChartConfProvider;
 import org.codeviation.statistics.CountsStatHandler;
 import org.codeviation.statistics.Graph;
 import org.codeviation.statistics.JavaFileHandler;
 import org.codeviation.statistics.Statistics;
 import org.jfree.chart.JFreeChart;
 import org.jfree.chart.plot.XYPlot;
 import org.jfree.chart.renderer.xy.XYItemRenderer;
 import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
 import org.kohsuke.stapler.StaplerRequest;
 import org.kohsuke.stapler.StaplerResponse;
 import org.openide.util.Lookup;
 
 /**
  *
  * @author pzajac
  */
 public abstract class JavaFileIterableView implements ModelObject {
     static String chartType;
     static final String CHART_TYPE_PARAM = "ChartType";
     public JavaFileIterableView() {
     }
 
     private ChartConf selectedChartConf ;
     
     protected abstract Date getMinDate();
     protected abstract Date getMaxDate();
     protected abstract Iterable<JavaFile> getJavaFiles();
     
     public static List<ChartConf> getChartConfs() {
         Lookup lookup = Lookup.getDefault();
         
         List<ChartConf> confs = new ArrayList<ChartConf>();
             for (ChartConfProvider prov : lookup.lookupAll(ChartConfProvider.class)) {
                 for (ChartConf c : prov.getChartConfs()) {
                     confs.add(c);
                 }
             }
         if (confs.size() == 0) {
             ChartConfProvider provider = new CountsStatHandler();
             for (ChartConf conf :provider.getChartConfs()) {
                 confs.add(conf);
             }
         }
         return confs;
     }
     
     public  List<String> getChartConfOptions() {
         List<String> values = new ArrayList<String>();
         for (ChartConf conf : getChartConfs()) {
             values.add("<option VALUE=\""+ conf.getName() + getSelectedOption(conf) + "\">" + conf.getTitle() + "</option>");
         }
         System.out.println("values:" + values);
         return values;
     }
     private String getSelectedOption(ChartConf conf) {
         return (conf.equals(selectedChartConf)) ?
             " selected = \"true\" " : "";
             
         
     }
     
     public static void updateGraphType(StaplerRequest req,StaplerResponse rsp) {
         chartType = req.getParameter(CHART_TYPE_PARAM);
         Enumeration en = req.getParameterNames();
         while(en.hasMoreElements()) {
             System.out.println(en.nextElement());
         }
         if (chartType != null) {
             rsp.addCookie(new Cookie(CHART_TYPE_PARAM,chartType));
         }
         
     }
     public  ChartConf getChartConf(StaplerRequest req) {
         String chartTypePar = req.getParameter(CHART_TYPE_PARAM);
         
         if (chartTypePar == null) {
             Cookie cookies[] = req.getCookies() ;
             // cookies are null
             if (cookies != null) {
                 for (Cookie cookie : cookies) {
                     if (cookie.getName().equals(CHART_TYPE_PARAM)) {
                         chartTypePar = cookie.getValue();
                         break;
                     }
                 }
             }
         }
         for (ChartConf conf :  getChartConfs()) {
             if (conf.getName().equals(chartTypePar)) {
                 selectedChartConf = conf;
                 return conf;
             }
         }
         selectedChartConf = null;
         return null;
     }
     public void doGraph(StaplerRequest req, StaplerResponse rsp) throws IOException {
         Lookup lookup = Lookup.getDefault();
         ChartConfProvider provider = null;
         ChartConf conf = getChartConf(req);
         if (lookup != null) {
             for (ChartConfProvider prov : lookup.lookupAll(ChartConfProvider.class)) {
                 for (ChartConf c : prov.getChartConfs()) {
                     if (c.equals(conf)) {
                         provider = prov;
                     }
                 }
             } 
         } 
         if (provider == null) {
             provider = new CountsStatHandler();
            conf = provider.getChartConfs()[0];
         }
 
         Graph graph = conf.createGraph();
        if (graph == null) {
            getLogger().info("Date:" + getMinDate() + "," + getMaxDate());
            graph = new Statistics(getMinDate(),getMaxDate());
        } 
        JavaFileHandler handler = conf.getStatHandler();
        graph.setJavaFileHandler(handler);
        graph.setItemsCount(100);
        handler.init(graph);
        graph.addJavaFiles(getJavaFiles());
 
         handler.initGraphPaint(conf);
         JFreeChart chart = graph.getChart(conf, true);
        
         chart.setBackgroundPaint(Color.WHITE);
         chart.setTitle((String)null);
         XYPlot plot = (XYPlot) chart.getPlot();
 //        plot.setDomainGridlinePaint(Color.BLACK);
 //        plot.setRangeGridlinePaint(Color.BLACK);
 //        
 //        plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
 //        
 //        plot.setDomainCrosshairVisible(true);
 //        plot.setRangeCrosshairVisible(true);       
 //        
         XYItemRenderer r = plot.getRenderer();
         if (r instanceof XYLineAndShapeRenderer) {
             XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
 //            renderer.setBaseShapesVisible(true);
 ////            renderer.setBaseShapesFilled(true);
 //            renderer.setUseFillPaint(true);
 //            
 //  //          renderer.setSeriesItemLabelsVisible(1, true);
 //            renderer.setUseOutlinePaint(true);
             renderer.setStroke(new BasicStroke(2.0f));
 ////            renderer.getPlot().setRenderer(1, r)getRenderer(1).setStroke();
 ////            renderer.setStroke();
 //            
 //  //          renderer.setS
         }
 //        
         ChartUtil.generateGraph(req,rsp,chart,400,400);
 
     }
     static Logger getLogger() {
         return Logger.getLogger(SourceRootView.class.getName());
     }
 
 }
