 /**
  * This file is part of ImportPlain library (check README).
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
 
 package ExportModules;
 
 import Utils.IOControl;
 
 /**
  * Plain export class.
  * @author Stanislav Nepochatov <spoilt.exile@gmail.com>
  */
 @Utils.RibbonIOModule(type="PLAIN", property="EXPORT_PLAIN", api_version=1)
 public class Plain extends Export.Exporter {
     
     /**
      * Constructor redirect.
      * @param givenMessage message to export;
      * @param givenSchema export scheme reference;
      * @param givenSwitch message index updater switch;
      * @param givenDir dir which message came from;
      */
     public Plain(MessageClasses.Message givenMessage, Export.Schema givenSchema, Export.ReleaseSwitch givenSwitch, String givenDir) {
         super(givenMessage, givenSchema, givenSwitch, givenDir);
     }
 
     @Override
     protected void doExport() throws Exception {
         String fileName;
         //TODO: make this part according to specs.
         switch (this.currSchema.currConfig.getProperty("plain_naming")) {
             case "HEADER":
                 fileName = this.exportedMessage.HEADER;
                 break;
             case "INDEX":
                 fileName = this.exportedMessage.INDEX;
                 break;
             default:
                 fileName = this.exportedMessage.INDEX;
         }
         java.io.File exportFile = new java.io.File(this.currSchema.currConfig.getProperty("plain_path") + "/" + fileName);
         java.io.FileWriter exportWriter = new java.io.FileWriter(exportFile);
         exportWriter.write(this.exportedContent);
         exportWriter.close();
     }
 }
