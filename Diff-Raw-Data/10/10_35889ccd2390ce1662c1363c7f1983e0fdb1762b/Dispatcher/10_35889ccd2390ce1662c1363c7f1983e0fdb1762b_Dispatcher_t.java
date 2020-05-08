 /**
  * This file is part of libRibbonIO library (check README).
  * Copyright (C) 2012-2013 Stanislav Nepochatov
  * 
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 **/
 
 package Export;
 
 import Utils.IOControl;
 
 /**
  * Export dispatcher (exporter factory) class.
  * @author Stanislav Nepochatov <spoilt.exile@gmail.com>
  */
 public class Dispatcher {
     
     /**
      * Path to export schemas.
      */
     public String exportDirPath;
     
     /**
      * Path to export modules.
      */
     public String exportModulePath;
     
     /**
      * List of export schemas.
      */
     private java.util.ArrayList<Utils.ModuleContainer> moduleList = new java.util.ArrayList<>();
     
     /**
      * List of export schemas.
      */
     private java.util.ArrayList<Schema> schemaList = new java.util.ArrayList<>();
     
     /**
      * System directory export subscribes.<br>
      * <br>
      * Subsribe hash map has next structure:<br>
      * <b>DIR_NAME, ArrayList(SCHEME_NAME)</b>
      */
     private java.util.HashMap<String, java.util.ArrayList<String>> subscribes = new java.util.HashMap();
     
     /**
      * Default constructor.
      * @param givenDirPath path to directory with export schemas;
      * @param givenModulePath path to modules to load;
      */
     public Dispatcher(String givenModulePath, String givenDirPath) {
         exportDirPath = givenDirPath;
         exportModulePath = givenModulePath;
        Utils.IOControl.registerExport(this);
         moduleList = Utils.IOControl.loadModules(givenModulePath);
         java.io.File exportPropsDir = new java.io.File(exportDirPath);
         if (!exportPropsDir.exists()) {
             exportPropsDir.mkdirs();
             Utils.IOControl.serverWrapper.log(Utils.IOControl.EXPORT_LOGID, 2, "Створюю теку експорту...");
         }
         java.io.File[] exportsProps = exportPropsDir.listFiles(new java.io.FilenameFilter() {
 
             @Override
             public boolean accept(java.io.File dir, String name) {
                 if (name.endsWith(".export")) {
                     return true;
                 } else {
                     return false;
                 }
             }
         });
         for (java.io.File exportFile : exportsProps) {
             java.util.Properties exportConfig = new java.util.Properties();
             try {
                 exportConfig.load(new java.io.FileReader(exportFile));
             } catch (java.io.FileNotFoundException ex) {
                 Utils.IOControl.serverWrapper.log(Utils.IOControl.EXPORT_LOGID, 1, "неможливо знайти файл " + exportFile.getName());
             } catch (java.io.IOException ex) {
                 Utils.IOControl.serverWrapper.log(Utils.IOControl.EXPORT_LOGID, 1, "помилка при зчитуванні файлу " + exportFile.getName());
             }
             Schema newExport = getNewSchema(exportConfig);
             if (newExport != null) {
                 this.schemaList.add(newExport);
             } else {
                 Utils.IOControl.serverWrapper.log(Utils.IOControl.EXPORT_LOGID, 2, "заванатження модулю для " + exportFile.getName() + " завершилось з помилкою.");
             }
         }
         if (this.schemaList.isEmpty()) {
             Utils.IOControl.serverWrapper.log(Utils.IOControl.EXPORT_LOGID, 2, "система не знайшла жодної схеми экспорту!");
         }
     }
     
     /**
      * Subscribe directory to export.
      * @param givenSchemas array with schemas names;
      * @param givenDirName name of directory;
      */
     public void subscribeDir(String[] givenSchemas, String givenDirName) {
 	if (givenSchemas == null) {
 	    return;
 	}
         java.util.ArrayList<String> putList = new java.util.ArrayList();
         for (String currScheme : givenSchemas) {
             if (currScheme.isEmpty()) {
                 continue;
             }
             if (isSchemaExists(currScheme)) {
                 putList.add(currScheme);
             } else {
                 IOControl.serverWrapper.log(IOControl.EXPORT_LOGID, 2, "схему експорту " + currScheme + " не існує (" + givenDirName + ")");
             }
         }
         if (!putList.isEmpty()) {
             this.subscribes.put(givenDirName, putList);
         }
     }
     
     /**
      * Find out if schema existed.
      * @param givenSchema schema to search;
      * @return true if existed / false if not.
      */
     private Boolean isSchemaExists(String givenSchema) {
         java.util.ListIterator<Schema> shIter = this.schemaList.listIterator();
         while (shIter.hasNext()) {
             if (shIter.next().name.equals(givenSchema)) {
                 return true;
             }
         }
         return false;
     }
     
     /**
      * Return new export schema.
      * @param givenConfig schema's config;
      * @return new reference of Scheme or null if type is uknown;
      */
     private Schema getNewSchema(java.util.Properties givenConfig) {
         java.util.ListIterator<Utils.ModuleContainer> modIter = this.moduleList.listIterator();
         Utils.ModuleContainer findedMod = null;
         while (modIter.hasNext()) {
             Utils.ModuleContainer currMod = modIter.next();
             if (currMod.moduleType.equals(givenConfig.getProperty("export_type"))) {
                 findedMod = currMod;
                 break;
             }
         }
         if (findedMod != null) {
             return new Schema(givenConfig, findedMod.moduleClass);
         } else {
             Utils.IOControl.serverWrapper.log(Utils.IOControl.EXPORT_LOGID, 2, "неможливо знайти модуль для типу " + givenConfig.getProperty("export_type"));
         }
         return null;
     }
     
     /**
      * Find out if there is subscriptions for given directories.
      * @param givenDirs array with directory names;
      * @return true if export needed / false if no subscriptions.
      */
     public Boolean checkExport(String[] givenDirs) {
         for (String currDir : givenDirs) {
             if (this.subscribes.containsKey(currDir)) {
                 return true;
             }
         }
         return false;
     }
     
     /**
      * Init export sequence.
      * @param exportedMessage message to export;
      */
     public void initExport(MessageClasses.Message exportedMessage) {
         ReleaseSwitch newSwitch = new ReleaseSwitch(exportedMessage.INDEX);
         for (String currDir : exportedMessage.DIRS) {
             if (this.subscribes.containsKey(currDir)) {
                 newSwitch.addSchemas(subscribes.get(currDir));
                 java.util.ListIterator<String> schemeIter = subscribes.get(currDir).listIterator();
                 while (schemeIter.hasNext()) {
                     Schema currSchema = this.getScheme(schemeIter.next());
                     Exporter newExport = currSchema.getNewExportTask(exportedMessage, newSwitch, currDir);
                     newExport.start();
                 }
             }
         }
     }
     
     /**
      * Get scheme by name.
      * @param givenName scheme's name;
      * @return scheme or null.
      */
     private Schema getScheme(String givenName) {
         java.util.ListIterator<Schema> schemeIter = this.schemaList.listIterator();
         while (schemeIter.hasNext()) {
             Schema currScheme = schemeIter.next();
             if (currScheme.name.equals(givenName)) {
                 return currScheme;
             }
         }
         return null;
     }
 }
