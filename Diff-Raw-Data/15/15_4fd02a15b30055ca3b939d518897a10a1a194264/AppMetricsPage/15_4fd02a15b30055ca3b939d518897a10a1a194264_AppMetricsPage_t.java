 /*
  * Copyright (c) 2000-2002 Netspective Corporation -- all rights reserved
  *
  * Netspective Corporation permits redistribution, modification and use
  * of this file in source and binary form ("The Software") under the
  * Netspective Source License ("NSL" or "The License"). The following 
  * conditions are provided as a summary of the NSL but the NSL remains the 
  * canonical license and must be accepted before using The Software. Any use of
  * The Software indicates agreement with the NSL. 
  *
  * 1. Each copy or derived work of The Software must preserve the copyright
  *    notice and this notice unmodified.
  *
  * 2. Redistribution of The Software is allowed in object code form only 
  *    (as Java .class files or a .jar file containing the .class files) and only 
  *    as part of an application that uses The Software as part of its primary 
  *    functionality. No distribution of the package is allowed as part of a software 
  *    development kit, other library, or development tool without written consent of 
  *    Netspective Corporation. Any modified form of The Software is bound by 
  *    these same restrictions.
  * 
  * 3. Redistributions of The Software in any form must include an unmodified copy of 
  *    The License, normally in a plain ASCII text file unless otherwise agreed to,
  *    in writing, by Netspective Corporation.
  *
  * 4. The names "Sparx" and "Netspective" are trademarks of Netspective 
  *    Corporation and may not be used to endorse products derived from The 
  *    Software without without written consent of Netspective Corporation. "Sparx" 
  *    and "Netspective" may not appear in the names of products derived from The 
  *    Software without written consent of Netspective Corporation.
  *
  * 5. Please attribute functionality to Sparx where possible. We suggest using the 
  *    "powered by Sparx" button or creating a "powered by Sparx(tm)" link to
  *    http://www.netspective.com for each application using Sparx.
  *
  * The Software is provided "AS IS," without a warranty of any kind. 
  * ALL EXPRESS OR IMPLIED REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
  * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
  * OR NON-INFRINGEMENT, ARE HEREBY DISCLAIMED.
  *
  * NETSPECTIVE CORPORATION AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES
  * SUFFERED BY LICENSEE OR ANY THIRD PARTY AS A RESULT OF USING OR DISTRIBUTING 
  * THE SOFTWARE. IN NO EVENT WILL NETSPECTIVE OR ITS LICENSORS BE LIABLE
  * FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL,
  * CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND
  * REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR
  * INABILITY TO USE THE SOFTWARE, EVEN IF HE HAS BEEN ADVISED OF THE POSSIBILITY
  * OF SUCH DAMAGES.      
  *
  * @author Shahid N. Shah
  */
  
 /**
 * $Id: AppMetricsPage.java,v 1.3 2002-08-09 12:56:52 shahid.shah Exp $
  */
 
 package com.netspective.sparx.ace.page;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import com.netspective.sparx.ace.AceServletPage;
 import com.netspective.sparx.util.metric.FileTypeMetric;
 import com.netspective.sparx.util.metric.Metric;
 import com.netspective.sparx.util.config.ConfigurationManager;
 import com.netspective.sparx.util.config.ConfigurationManagerFactory;
 import com.netspective.sparx.xaf.form.DialogManager;
 import com.netspective.sparx.xaf.form.DialogManagerFactory;
 import com.netspective.sparx.xaf.page.PageContext;
 import com.netspective.sparx.xaf.sql.StatementManager;
 import com.netspective.sparx.xaf.sql.StatementManagerFactory;
 
 public class AppMetricsPage extends AceServletPage
 {
     public static final String METRICS_CFG_PREFIX = com.netspective.sparx.Globals.DEFAULT_CONFIGITEM_PREFIX + "ace.metrics";
     public static final String FILESYS_CFG_PREFIX = METRICS_CFG_PREFIX + ".filesys";
 
     private Set countLinesInFileExtn = new HashSet();
     private boolean ignoreCaseInFileExtn;
 
     public final String getName()
     {
         return "metrics";
     }
 
     public final String getPageIcon()
     {
         return "metrics.gif";
     }
 
     public final String getCaption(PageContext pc)
     {
         return "Metrics";
     }
 
     public final String getHeading(PageContext pc)
     {
         return "Application Metrics";
     }
 
     public void calcFileSystemMetrics(File path, int depth, Metric dirMetrics, Metric allFileMetrics, FileTypeMetric codeFileMetrics, FileTypeMetric appFileMetrics)
     {
         Metric totalDirsMetric = dirMetrics.createChildMetricSimple("Total folders");
         Metric avgEntriesMetric = dirMetrics.createChildMetricAverage("Average entries per folder");
         Metric avgDepthMetric = dirMetrics.createChildMetricAverage("Average Depth");
         avgDepthMetric.incrementAverage(depth);
 
         File[] entries = path.listFiles();
         for(int i = 0; i < entries.length; i++)
         {
             File entry = entries[i];
             if(entry.isDirectory())
             {
                 totalDirsMetric.incrementCount();
                 File[] childEntries = entry.listFiles();
                 avgEntriesMetric.incrementAverage(childEntries.length);
                 calcFileSystemMetrics(entry, depth + 1, dirMetrics, allFileMetrics, codeFileMetrics, appFileMetrics);
             }
             else
             {
                 String entryCaption = entry.getName();
                 String entryExtension = "(no extension)";
                 int extnIndex = entryCaption.lastIndexOf('.');
                 if(extnIndex > -1)
                     entryExtension = entryCaption.substring(extnIndex);
                 if(ignoreCaseInFileExtn)
                     entryExtension = entryExtension.toLowerCase();
 
                 Metric fileMetric = allFileMetrics.createChildMetricSimple(entryExtension);
                 fileMetric.setFlag(Metric.METRICFLAG_SHOW_PCT_OF_PARENT);
                 fileMetric.incrementCount();
 
                 if(countLinesInFileExtn.contains(entryExtension))
                 {
                     FileTypeMetric ftMetric = (FileTypeMetric) codeFileMetrics.getChild(entryExtension);
                     if(ftMetric == null)
                         ftMetric = codeFileMetrics.createChildMetricFileType(entryExtension, true);
                     ftMetric.incrementCount(entry);
                 }
                 else
                 {
                     FileTypeMetric ftMetric = (FileTypeMetric) appFileMetrics.getChild(entryExtension);
                     if(ftMetric == null)
                         ftMetric = appFileMetrics.createChildMetricFileType(entryExtension, false);
                     ftMetric.incrementCount(entry);
                 }
             }
         }
     }
 
     public void createFileSystemMetrics(Metric parentMetric, String pathStr)
     {
         Metric fsMetrics = parentMetric.createChildMetricGroup("Application Files");
         Metric dirMetrics = fsMetrics.createChildMetricGroup("Folders");
         Metric allFileMetrics = fsMetrics.createChildMetricSimple("Files");
         allFileMetrics.setFlag(Metric.METRICFLAG_SUM_CHILDREN);
         allFileMetrics.setFlag(Metric.METRICFLAG_SORT_CHILDREN);
 
         FileTypeMetric codeFileMetrics = parentMetric.createChildMetricFileType("Code Files", false);
         codeFileMetrics.setFlag(Metric.METRICFLAG_SUM_CHILDREN);
         codeFileMetrics.setFlag(Metric.METRICFLAG_SORT_CHILDREN);
 
         FileTypeMetric appFileMetrics = parentMetric.createChildMetricFileType("App Files", false);
         appFileMetrics.setFlag(Metric.METRICFLAG_SUM_CHILDREN);
         appFileMetrics.setFlag(Metric.METRICFLAG_SORT_CHILDREN);
 
         File path = new File(pathStr);
         calcFileSystemMetrics(path, 1, dirMetrics, allFileMetrics, codeFileMetrics, appFileMetrics);
     }
 
     public void handlePageBody(PageContext pc) throws ServletException, IOException
     {
         ServletContext context = pc.getServletContext();
         ConfigurationManager config = ConfigurationManagerFactory.getManager(context);
 
         ignoreCaseInFileExtn = config.getBooleanValue(pc, FILESYS_CFG_PREFIX + ".ignore-case", true);
         String[] codeExtns = config.getDelimitedValues(pc, FILESYS_CFG_PREFIX + ".code-extensions", null, ",");
         if(codeExtns != null)
         {
             countLinesInFileExtn.clear();
             for(int i = 0; i < codeExtns.length; i++)
                 countLinesInFileExtn.add("." + codeExtns[i]);
         }
 
         Metric metrics = new Metric(null, "Application Metrics", Metric.METRIC_TYPE_GROUP);
 
         DialogManager dmanager = DialogManagerFactory.getManager(context);
         dmanager.getMetrics(metrics);
 
         StatementManager smanager = StatementManagerFactory.getManager(context);
         smanager.getMetrics(metrics);
 
        createFileSystemMetrics(metrics, config.getTextValue(pc, "app.site-root-path"));
 
         try
         {
             DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
             DocumentBuilder parser = factory.newDocumentBuilder();
             Document xmlDoc = parser.newDocument();
 
             Element rootElem = xmlDoc.createElement("xaf");
             xmlDoc.appendChild(rootElem);
 
             metrics.createElement(rootElem);
             transform(pc, xmlDoc, com.netspective.sparx.Globals.ACE_CONFIG_ITEMS_PREFIX + "metrics-browser-xsl");
         }
         catch(Exception e)
         {
             throw new ServletException(e);
         }
     }
 }
