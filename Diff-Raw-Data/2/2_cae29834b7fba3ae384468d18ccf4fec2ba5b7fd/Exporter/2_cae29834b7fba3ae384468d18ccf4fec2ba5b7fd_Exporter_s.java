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
  * Export single operation thread class.
  * @author Stanislav Nepochatov <spoilt.exile@gmail.com>
  */
 public abstract class Exporter extends Thread {
     
     /**
      * Current export scheme.
      */
     protected Schema currSchema;
     
     /**
      * Release switch for index updating.
      */
     protected ReleaseSwitch currSwitch;
     
     /**
      * Dir which call this export task.
      */
     protected String calledDir;
     
     /**
      * Exported message itself. You should use 
      * this variable instead of <code>CONTENT</code> 
      * field in message.
      */
     protected String exportedContent;
     
     /**
      * Current exporting message.
      */
     protected MessageClasses.Message exportedMessage;
     
     /**
      * Charset for export.
      */
     protected String exportedCharset = "UTF-8";
     
     /**
      * Default constructor.
      * @param givenMessage message to export;
      * @param givenSchema export scheme reference;
      * @param givenSwitch message index updater switch;
      * @param givenDir dir which message came from;
      */
     public Exporter(MessageClasses.Message givenMessage, Schema givenSchema, ReleaseSwitch givenSwitch, String givenDir) {
         currSchema = givenSchema;
         currSwitch = givenSwitch;
         exportedMessage = givenMessage;
         calledDir = givenDir;
         if (currSchema.currFormater !=null) {
             exportedContent = currSchema.currFormater.format(exportedMessage, calledDir);
         } else {
             exportedContent = exportedMessage.CONTENT;
         }
         if (currSchema.currConfig.getProperty("opt_charset") != null) {
             exportedCharset = currSchema.currConfig.getProperty("opt_charset");
         }
     }
     
     @Override
     public void run() {
         try {
             doExport();
             if ("1".equals(this.currSchema.currConfig.getProperty("opt_log"))) {
                 IOControl.serverWrapper.log(IOControl.EXPORT_LOGID + ":" + this.currSchema.name, 3, "прозведено експорт повідомлення " + this.exportedMessage.INDEX);
             }
            exportedMessage.PROPERTIES.add(new MessageClasses.MessageProperty(this.currSchema.currConfig.getProperty("export_type"), "root", this.currSchema.currConfig.getProperty("export_print"), IOControl.serverWrapper.getDate()));
         } catch (Exception ex) {
             IOControl.serverWrapper.log(IOControl.EXPORT_LOGID + ":" + this.currSchema.name, 1, "експорт повідомлення '" + this.exportedMessage.HEADER + "' завершився помилкою.");
             IOControl.serverWrapper.enableDirtyState(this.currSchema.type, this.currSchema.name, this.currSchema.currConfig.getProperty("export_print"));
             IOControl.serverWrapper.postException("Помилка експорту: схема " + this.currSchema.name
                     + " тип " + this.currSchema.type
                     + "\nПовідомлення " + this.exportedMessage.HEADER + " за індексом " + this.exportedMessage.INDEX, ex);
         }
         this.currSwitch.markSchema(this.currSchema.name);
     }
     
     /**
      * Body of export method.
      */
     protected abstract void doExport() throws Exception;
     
     /**
      * Try to recover current export task.
      * @return result of recovery operation;
      */
     public abstract Boolean tryRecovery();
 }
