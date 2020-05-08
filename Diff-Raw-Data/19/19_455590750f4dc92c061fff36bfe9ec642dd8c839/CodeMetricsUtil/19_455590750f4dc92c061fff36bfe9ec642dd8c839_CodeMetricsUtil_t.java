 package org.jenkinsci.plugins.vs_code_metrics.util;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.text.NumberFormat;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map.Entry;
 
 import hudson.FilePath;
 import hudson.model.AbstractBuild;
 
 import org.apache.commons.digester.Digester;
 import org.xml.sax.SAXException;
 
 import org.jenkinsci.plugins.vs_code_metrics.Messages;
 import org.jenkinsci.plugins.vs_code_metrics.bean.*;
 
 public abstract class  CodeMetricsUtil {
 
    private static final int BUILD_ACTION_TOKEN_POS = 4;
 
     private CodeMetricsUtil() {}
 
     /**
      *
      * @param  path
      * @return
      * @throws VsCodeMetricsException
      */
     public static CodeMetrics createCodeMetrics(FilePath path) throws IOException, InterruptedException {
         InputStream stream = null;
         try {
             stream = path.read();
 
             Digester digester = new Digester();
             digester.setClassLoader(CodeMetrics.class.getClassLoader());
 
             digester.addObjectCreate("*/CodeMetricsReport", CodeMetrics.class);
 
             digester.addObjectCreate("*/Module", Module.class);
             digester.addSetNext("*/Module", "addChild");
             digester.addSetProperties("*/Module", "Name", "name");
             digester.addObjectCreate("*/Module/Metrics/Metric", Metric.class);
             digester.addSetNext("*/Module/Metrics/Metric", "addMetric");
             digester.addSetProperties("*/Module/Metrics/Metric", "Name", "name");
             digester.addSetProperties("*/Module/Metrics/Metric", "Value", "value");
 
             digester.addObjectCreate("*/Namespace", Namespace.class);
             digester.addSetNext("*/Namespace", "addChild");
             digester.addSetProperties("*/Namespace", "Name", "name");
             digester.addObjectCreate("*/Namespace/Metrics/Metric", Metric.class);
             digester.addSetNext("*/Namespace/Metrics/Metric", "addMetric");
             digester.addSetProperties("*/Namespace/Metrics/Metric", "Name", "name");
             digester.addSetProperties("*/Namespace/Metrics/Metric", "Value", "value");
 
             digester.addObjectCreate("*/Type", Type.class);
             digester.addSetNext("*/Type", "addChild");
             digester.addSetProperties("*/Type", "Name", "name");
             digester.addObjectCreate("*/Type/Metrics/Metric", Metric.class);
             digester.addSetNext("*/Type/Metrics/Metric", "addMetric");
             digester.addSetProperties("*/Type/Metrics/Metric", "Name", "name");
             digester.addSetProperties("*/Type/Metrics/Metric", "Value", "value");
 
             digester.addObjectCreate("*/Member", Member.class);
             digester.addSetNext("*/Member", "addChild");
             digester.addSetProperties("*/Member", "Name", "name");
             digester.addObjectCreate("*/Member/Metrics/Metric", Metric.class);
             digester.addSetNext("*/Member/Metrics/Metric", "addMetric");
             digester.addSetProperties("*/Member/Metrics/Metric", "Name", "name");
             digester.addSetProperties("*/Member/Metrics/Metric", "Value", "value");
 
             CodeMetrics bean = (CodeMetrics)digester.parse(stream);
 
             return bean;
         } catch (SAXException e) {
             return null;
         } catch (IOException e) {
             return null;
         } finally {
             try { if (stream != null) stream.close(); } catch (Exception e) {}
         }
     }
 
     /**
      *
      * @param  build
      * @return
      */
     public static File getReportDir(AbstractBuild<?, ?> build) {
         return new File(build.getRootDir(), Constants.REPORT_DIR);
     }
 
     /**
      *
      * @param  file
      * @return
      * @throws VsCodeMetricsException
      */
     public static FilePath[] getReports(File file) throws IOException, InterruptedException {
         FilePath path = new FilePath(file);
         if (path.isDirectory()) {
             return path.list("*xml");
         } else {
             FilePath report = new FilePath(new File(path.getName() + ".xml"));
             return report.exists() ? new FilePath[]{report} : new FilePath[0];
         }
     }
 
     /**
      *
      * @param  build
      * @return
      */
     public static CodeMetrics getCodeMetrics(AbstractBuild<?, ?> build) throws IOException, InterruptedException {
         File reportFolder = getReportDir(build);
 
         FilePath[] reports = getReports(reportFolder);
         CodeMetrics total = null;
 
         for (FilePath report : reports) {
             CodeMetrics bean = createCodeMetrics(report);
             if (bean == null) continue;
 
             if (total != null) {
                 for (Entry<String, Module> entry : bean.getChildren().entrySet()) {
                     if (!total.getChildren().containsKey(entry.getKey())) {
                         total.addChild(entry.getValue());
                     }
                 }
             } else {
                 total = bean;
             }
         }
 
         // set total bean.
         total.setName(Messages.Summart_AllClasses());
 
         if (total.getChildren().size() > 0) {
             int sumMaintainabilityIndex = 0;
             int sumCyclomaticComplexity = 0;
             int sumClassCoupling        = 0;
             int sumDepthOfInheritance   = 0;
             int sumtLinesOfCode         = 0;
 
             for (Module module : total.getChildren().values()) {
                 sumMaintainabilityIndex += parseLong(module.getMaintainabilityIndex());
                 sumCyclomaticComplexity += parseLong(module.getCyclomaticComplexity());
                 sumClassCoupling        += parseLong(module.getClassCoupling());
                 sumDepthOfInheritance   += parseLong(module.getDepthOfInheritance());
                 sumtLinesOfCode         += parseLong(module.getLinesOfCode());
             }
 
             total.setMaintainabilityIndex(String.valueOf(sumMaintainabilityIndex / total.getChildren().size()));
             total.setCyclomaticComplexity(String.valueOf(sumCyclomaticComplexity));
             total.setClassCoupling(String.valueOf(sumClassCoupling));
             total.setDepthOfInheritance(String.valueOf(sumDepthOfInheritance));
             total.setLinesOfCode(String.valueOf(sumtLinesOfCode));
         }
 
         return total;
     }
 
     public static long parseLong(String value) {
         try {
             return (Long)NumberFormat.getNumberInstance().parse(value);
         } catch (ParseException e) {
             return 0;
         }
     }
 
     public static FilePath[] locateReports(FilePath workspace, String includes) throws IOException, InterruptedException {
 
         try {
             FilePath[] ret = workspace.list(includes);
             if (ret.length > 0) {
                 return ret;
             }
         } catch (Exception e) {
         }
 
         ArrayList<FilePath> files = new ArrayList<FilePath>();
         String parts[] = includes.split("\\s*[;:,]+\\s*");
         for (String path : parts) {
             FilePath src = workspace.child(path);
             if (src.exists()) {
                 if (src.isDirectory()) {
                     files.addAll(Arrays.asList(src.list("**/metrics*.xml")));
                 } else {
                     files.add(src);
                 }
             }
         }
         return files.toArray(new FilePath[files.size()]);
     }
 
     /**
      *
      * @param folder
      * @param files
      * @throws IOException
      * @throws InterruptedException
      */
     public static void saveReports(FilePath folder, FilePath[] files) throws IOException, InterruptedException {
         folder.mkdirs();
         for (int i = 0; i < files.length; i++) {
             String name = "metrics" + (i > 0 ? i : "") + ".xml";
             FilePath src = files[i];
             FilePath dst = folder.child(name);
             src.copyTo(dst);
         }
     }
 
     public static String[] getBuildActionTokens(String requestURI, String contextPath) {
         List<String> tokens = new ArrayList<String>();
 
         String path = requestURI;
         if (!StringUtil.isNullOrSpace(contextPath)) {
             if (!requestURI.startsWith(contextPath)) return tokens.toArray(new String[0]);
             path = requestURI.substring(contextPath.length());
         }
 
        int indexJob = path.indexOf("/job");
        if (indexJob < 0) return tokens.toArray(new String[0]);
 
        String[] parts = path.substring(indexJob + "/job".length()).split("/");
 
         for (int i = BUILD_ACTION_TOKEN_POS; i < parts.length - 1; i++) {
             tokens.add(parts[i]);
         }
 
         return tokens.toArray(new String[0]);
     }
 
     public static AbstractBean<?> searchBean(AbstractBean<?> bean, String[] tokens) {
         return searchBean(bean, tokens, 0);
     }
 
     public static AbstractBean<?> searchBean(AbstractBean<?> bean, String[] tokens, int index) {
         if (tokens.length <= index)
             return bean;
 
         if (!bean.getChildren().containsKey(tokens[index]))
             return null;
         else
             return searchBean((AbstractBean<?>)bean.getChildren().get(tokens[index]), tokens, index + 1);
     }
 }
