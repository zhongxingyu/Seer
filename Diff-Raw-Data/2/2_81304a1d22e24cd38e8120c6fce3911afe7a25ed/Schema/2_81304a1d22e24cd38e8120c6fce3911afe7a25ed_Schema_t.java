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
  * Export schema class.
  * @author Stanislav Nepochatov <spoilt.exile@gmail.com>
  */
 public class Schema {
     
     /**
      * Type of exporter.
      */
     public String type;
     
     /**
      * Name of export schema.
      */
     public String name;
     
     /**
      * Exporter class link.
      */
     private Class<Exporter> exporterModule;
     
     /**
      * Export formater.
      */
     public Formater currFormater;
     
     /**
      * Current export config.
      */
     public java.util.Properties currConfig;
     
     public static enum EM_ACTION {
         PLACE_ERRQ_DIRTY,
         DROP_WARN,
         DROP_WARN_DIRTY
     }
     
     EM_ACTION currAction = EM_ACTION.PLACE_ERRQ_DIRTY;
     
     /**
      * Default constructor.
      * @param givenConfig config for export;
      * @param givenModule export module class link.
      */
     public Schema(java.util.Properties givenConfig, Class<Exporter> givenModule) {
         currConfig = givenConfig;
         exporterModule = givenModule;
         type = currConfig.getProperty("export_type");
         name = currConfig.getProperty("export_name");
         if (currConfig.containsKey("export_template")) {
             try {
                currFormater = new Formater(currConfig, new String(java.nio.file.Files.readAllBytes(new java.io.File(currConfig.getProperty("export_template")).toPath())));
             } catch (java.io.IOException ex) {
                 IOControl.serverWrapper.log(IOControl.EXPORT_LOGID + ":" + name, 1, "помилка завантаження шаблону " + IOControl.dispathcer.exportDirPath + "/" + currConfig.getProperty("export_template"));
             }
         }
         if (currConfig.containsKey("opt_em_action")) {
             try {
                 currAction = EM_ACTION.valueOf(currConfig.getProperty("opt_em_action"));
             } catch (IllegalArgumentException ex) {
                 IOControl.serverWrapper.log(IOControl.EXPORT_LOGID + ":" + name, 1, "тип аварійної дії '" + currConfig.getProperty("opt_em_action") + "' не підтримується системою.");
             }
         }
         IOControl.serverWrapper.log(IOControl.EXPORT_LOGID, 3, "завантажено схему експорту '" + this.name + "'");
     }
     
     /**
      * Get timeout befor export.
      * @return integer value for time waiting before export;
      */
     public Integer getTimeout() {
         return Integer.parseInt(currConfig.getProperty("export_timeout")) * 60 * 1000;
     }
     
     /**
      * Get new export task thread.
      * @param givenMessage message to export;
      * @param givenSwitch switch;
      * @param givenDir called dir;
      * @return new export task;
      */
     public Exporter getNewExportTask(MessageClasses.Message givenMessage, ReleaseSwitch givenSwitch, String givenDir) {
         Exporter newExport = null;
         try {
             newExport = (Exporter) this.exporterModule.getConstructor(MessageClasses.Message.class, Schema.class, ReleaseSwitch.class, String.class).newInstance(givenMessage, this, givenSwitch, givenDir);
         } catch (java.lang.reflect.InvocationTargetException ex) {
             ex.getTargetException().printStackTrace();
         } catch (Exception ex) {
             IOControl.serverWrapper.log(IOControl.EXPORT_LOGID, 1, "неможливо опрацювати класс " + this.exporterModule.getName());
             IOControl.serverWrapper.enableDirtyState(type, name, this.currConfig.getProperty("export_print"));
             ex.printStackTrace();
         }
         return newExport;
     }
 }
