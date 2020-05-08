 /*
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License version 2
  * as published by the Free Software Foundation.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License along
  * with this program; if not, see <http://www.gnu.org/licenses/>
  *
  */
 /**
  *
  */
 package org.Transformer;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Map;
 import java.util.Vector;
 
 import org.Transformer.dataset.DataFilter;
 import org.Transformer.exporter.ExportStyle;
 import org.Transformer.exporter.Exporter;
 import org.Transformer.importer.ImportSelector;
 import org.Transformer.importer.Importer;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 /** Representation of the Job to do.
  * Brings together all information needed to execute the task.
  * @author Lars P&ouml;tter
  * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
  */
 public class Job
 {
     public static final int MAX_DATA_FILTER_NUMBER = 100;
 
     private static final String IMPORTER_LINE = "Importer";
     private static final String IMPORT_SELECTOR_LINE = "Import Selector";
     private static final String DATA_FILTER_LINE = "Data Filter";
     private static final String EXPORTER_LINE = "Exporter";
     private static final String EXPORT_STYLE_LINE = "Export Style";
     private static final String CLASS_TYPE_NAME = "type";
 
     private Importer theImporter;
     private ImportSelector theImportSelector;
     private Vector<DataFilter> theDataFilters;
     private Exporter theExporter;
     private ExportStyle theExportStyle;
 
     /**
      *
      */
     public Job()
     {
     }
 
     public static void writeJobToFile(final File cfgFile, final Job job)
     {
         FileWriter fw = null;
         try
         {
             fw = new FileWriter(cfgFile);
 
             final Importer imp = job.getImporter();
             if(null != imp)
             {
                 fw.write("[" + IMPORTER_LINE + "]\n");
                 fw.write(CLASS_TYPE_NAME + " = " + imp.getName() + "\n");
                 fw.write(imp.getConfig() + "\n");
             }
 
             final ImportSelector impsel = job.getImportSelector();
             if(null != impsel)
             {
                 fw.write("[" + IMPORT_SELECTOR_LINE + "]\n");
                 fw.write(CLASS_TYPE_NAME + " = " + impsel.getName() + "\n");
                 fw.write(impsel.getConfig() + "\n");
             }
 
             for(int i = 0; i < job.getNumberOfDataFilters(); i++)
             {
                 final DataFilter df = job.getDataFilter(i);
                 if(null != df)
                 {
                     fw.write("[" + DATA_FILTER_LINE + "_" + i + "]\n");
                     fw.write(CLASS_TYPE_NAME + " = " + df.getName() + "\n");
                     fw.write(df.getConfig() + "\n");
                 }
             }
 
             final Exporter exp = job.getExporter();
             if(null != exp)
             {
                 fw.write("[" + EXPORTER_LINE + "]\n");
                 fw.write(CLASS_TYPE_NAME + " = " + exp.getName() + "\n");
                 fw.write(exp.getConfig() + "\n");
             }
 
             final ExportStyle expsty = job.getExportStyle();
             if(null != expsty)
             {
                 fw.write("[" + EXPORT_STYLE_LINE + "]\n");
                 fw.write(CLASS_TYPE_NAME + " = " + expsty.getName() + "\n");
                 fw.write(expsty.getConfig() + "\n");
             }
         }
         catch(final IOException e)
         {
             e.printStackTrace();
         }
         finally
         {
             if(null != fw)
             {
                 try
                 {
                     fw.close();
                 }
                 catch(final IOException e)
                 {
                     e.printStackTrace();
                 }
             }
         }
     }
 
     private static Importer readImporter(final ConfigParser cfgp)
     {
         final Map<String, String> settings = cfgp.getSettingsOfSection(IMPORTER_LINE);
         if(null != settings)
         {
             final String classType = settings.get(CLASS_TYPE_NAME);
             final Importer imp = Factory.createImporterFor(classType);
             if(null != imp)
             {
                 imp.setConfig(settings);
                 return imp;
             }
             else
             {
                 final Logger log = LoggerFactory.getLogger(Job.class);
                 log.error("Could not create the Importer : " + classType);
             }
         }
         else
         {
             final Logger log = LoggerFactory.getLogger(Job.class);
             log.error("File did not contain an Importer !");
         }
         return null;
     }
 
     private static ImportSelector readImportSelector(final ConfigParser cfgp)
     {
         final Map<String, String> settings = cfgp.getSettingsOfSection(IMPORT_SELECTOR_LINE);
         if(null != settings)
         {
             final String classType = settings.get(CLASS_TYPE_NAME);
             final ImportSelector impsel = Factory.createImportSelectorFor(classType);
             if(null != impsel)
             {
                 impsel.setConfig(settings);
                 return impsel;
             }
             else
             {
                 final Logger log = LoggerFactory.getLogger(Job.class);
                 log.error("Could not create the Import Selector : " + classType);
             }
         }
         else
         {
             final Logger log = LoggerFactory.getLogger(Job.class);
             log.error("File did not contain an Import Selector !");
         }
         return null;
     }
 
     private static void addSingleDataFilter(final Map<String, String> settings, final Job job)
     {
         if(null != settings)
         {
             final String classType = settings.get(CLASS_TYPE_NAME);
             final DataFilter df = Factory.createDataFilterFor(classType);
             if(null != df)
             {
                 df.setConfig(settings);
                 job.addDataFilter(df);
             }
             else
             {
                 final Logger log = LoggerFactory.getLogger(Job.class);
                 log.error("Could not create the Data Filter : " + classType);
             }
         }
         // else nothing to do
     }
 
     private static void readDataFilter(final ConfigParser cfgp, final Job job)
     {
         addSingleDataFilter(cfgp.getSettingsOfSection(DATA_FILTER_LINE), job);
         for(int i = 0; i < MAX_DATA_FILTER_NUMBER; i++)
         {
             final Map<String, String> hlp = cfgp.getSettingsOfSection(DATA_FILTER_LINE + "_" + i);
             if(null == hlp)
             {
                 break;
             }
             else
             {
                 addSingleDataFilter(hlp, job);
             }
         }
     }
 
     private static Exporter readExporter(final ConfigParser cfgp)
     {
         final Map<String, String> settings = cfgp.getSettingsOfSection(EXPORTER_LINE);
         if(null != settings)
         {
             final String classType = settings.get(CLASS_TYPE_NAME);
             final Exporter exp = Factory.createExporterFor(classType);
             if(null != exp)
             {
                 exp.setConfig(settings);
                 return exp;
             }
             else
             {
                 final Logger log = LoggerFactory.getLogger(Job.class);
                 log.error("Could not create the Exporter : " + classType);
             }
         }
         else
         {
             final Logger log = LoggerFactory.getLogger(Job.class);
             log.error("File did not contain an Exporter !");
         }
         return null;
     }
 
     private static ExportStyle readExportStyle(final ConfigParser cfgp)
     {
         final Map<String, String> settings = cfgp.getSettingsOfSection(EXPORT_STYLE_LINE);
         if(null != settings)
         {
             final String classType = settings.get(CLASS_TYPE_NAME);
             final ExportStyle expSty = Factory.createExportStyleFor(classType);
             if(null != expSty)
             {
                 expSty.setConfig(settings);
                 return expSty;
             }
             else
             {
                 final Logger log = LoggerFactory.getLogger(Job.class);
                 log.error("Could not create the Export Style : " + classType);
             }
         }
         else
         {
             final Logger log = LoggerFactory.getLogger(Job.class);
             log.error("File did not contain an Export Style !");
         }
         return null;
     }
 
     /** creates a Job from the provided File.
      * If the File does not exist a new Job is returned.
      * @param cfgFile File that contains Job Description
      * @return Job created from cfgFile or new Job
      */
     public static Job readFromFile(final File cfgFile)
     {
         final Job result = new Job();
         try
         {
             final FileReader fr = new FileReader(cfgFile);
             final ConfigParser cfgp = new ConfigParser(fr);
 
             result.setImporter(readImporter(cfgp));
             result.setImportSelector(readImportSelector(cfgp));
             readDataFilter(cfgp, result);
             result.setExporter(readExporter(cfgp));
             result.setExportStyle(readExportStyle(cfgp));
 
             fr.close();
         }
         catch(final FileNotFoundException e)
         {
             e.printStackTrace();
         }
         catch(final IOException e)
         {
             e.printStackTrace();
         }
         return result;
     }
 
     public final void setImporter(final Importer aImporter)
     {
         theImporter = aImporter;
     }
 
     public final Importer getImporter()
     {
         return theImporter;
     }
 
     public final void setImportSelector(final ImportSelector ainselect)
     {
         theImportSelector = ainselect;
     }
 
     public final ImportSelector getImportSelector()
     {
         return theImportSelector;
     }
 
     public final void setExporter(final Exporter exp)
     {
         theExporter = exp;
     }
 
     public final Exporter getExporter()
     {
         return theExporter;
     }
 
     public final void setExportStyle(final ExportStyle expStyle)
     {
         theExportStyle = expStyle;
     }
 
     public final ExportStyle getExportStyle()
     {
         return theExportStyle;
     }
 
     public final void addDataFilter(final DataFilter filter)
     {
         theDataFilters.add(filter);
     }
 
     public final DataFilter getDataFilter(final int index)
     {
        return theDataFilters.get(index);
     }
 
     public final int getNumberOfDataFilters()
     {
        return theDataFilters.size();
     }
 
     public final boolean isExecuteable()
     {
         if(null == theImporter)
         {
             return false;
         }
         if(null == theImportSelector)
         {
             return false;
         }
         if(null == theExporter)
         {
             return false;
         }
         if(null == theExportStyle)
         {
             return false;
         }
         return true;
     }
 
 }
