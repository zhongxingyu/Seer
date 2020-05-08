 /*
  * Copyright (C) 2000 - 2013 Silverpeas
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  *
  * As a special exception to the terms and conditions of version 3.0 of
  * the GPL, you may redistribute this Program in connection withWriter Free/Libre
  * Open Source Software ("FLOSS") applications as described in Silverpeas's
  * FLOSS exception.  You should have recieved a copy of the text describing
  * the FLOSS exception, and it is also available here:
  * "http://www.silverpeas.org/legal/licensing"
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.silverpeas.migration.jcr.wysiwyg;
 
 import org.silverpeas.dbbuilder.dbbuilder_dl.DbBuilderDynamicPart;
 import org.silverpeas.migration.jcr.service.SimpleDocumentService;
 import org.silverpeas.util.StringUtil;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 
 public class WysiwygCorrector extends DbBuilderDynamicPart {
 
   private static final ExecutorService executor = Executors.newFixedThreadPool(10);
   private final SimpleDocumentService service;
 
   public WysiwygCorrector() {
     this.service = new SimpleDocumentService();
   }
 
  public void migrateDocuments() throws Exception {
     long totalNumberOfMigratedFiles = 0L;
     List<WysiwygDocumentMerger> mergers = buildWysiwygDocumentMergers();
     List<Future<Long>> result = executor.invokeAll(mergers);
     try {
       for (Future<Long> nbOfMigratedDocuments : result) {
         totalNumberOfMigratedFiles += nbOfMigratedDocuments.get();
       }
     } catch (InterruptedException ex) {
       throw ex;
     } catch (Exception ex) {
       getConsole().printError("Error during adjustment of wysiwyg attachments " + ex, ex);
       throw ex;
     } finally {
       executor.shutdown();
     }
     getConsole().printMessage("Nb of adjusted wysiwyg documents : " + totalNumberOfMigratedFiles);
     this.service.shutdown();
   }
 
   private List<WysiwygDocumentMerger> buildWysiwygDocumentMergers() {
     getConsole().printMessage("All components to be adjusted : ");
     List<String> componentIds = service.listComponentIdsWithWysiwyg();
     List<WysiwygDocumentMerger> result = new ArrayList<WysiwygDocumentMerger>(componentIds.size());
     for (String componentId : componentIds) {
       if (StringUtil.isDefined(componentId)) {
         result.add(new WysiwygDocumentMerger(componentId, service, getConsole()));
         getConsole().printMessage(componentId + ", ");
       }
     }
     getConsole().printMessage("*************************************************************");
     return result;
   }
 }
