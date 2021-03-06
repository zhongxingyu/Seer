 /**
  * Copyright (C) 2010 BonitaSoft S.A.
  * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 2.0 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.bonitasoft.studio.commands.test;
 
 import java.util.List;
 
 import org.bonitasoft.studio.common.emf.tools.ModelHelper;
 import org.bonitasoft.studio.common.repository.RepositoryManager;
 import org.bonitasoft.studio.diagram.custom.repository.DiagramRepositoryStore;
 import org.bonitasoft.studio.diagram.custom.repository.ProcessConfigurationFileStore;
 import org.bonitasoft.studio.diagram.custom.repository.ProcessConfigurationRepositoryStore;
 import org.bonitasoft.studio.model.configuration.Configuration;
 import org.bonitasoft.studio.model.process.MainProcess;
 import org.bonitasoft.studio.model.process.Pool;
 import org.bonitasoft.studio.model.process.ProcessPackage;
 import org.bonitasoft.studio.test.swtbot.util.SWTBotTestUtil;
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
 import org.eclipse.swtbot.eclipse.gef.finder.SWTBotGefTestCase;
 import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefEditor;
 import org.eclipse.swtbot.swt.finder.SWTBot;
 import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
 import org.eclipse.swtbot.swt.finder.waits.ICondition;
 import org.eclipse.ui.PartInitException;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 
 /**
  * @author Mickael Istria
  *
  */
 @RunWith(SWTBotJunit4ClassRunner.class)
 public class DuplicateCommandTest extends SWTBotGefTestCase {
 
     @Test
     public void testDuplicationSourceClean() throws PartInitException {
         baseTest(false);
     }
 
     @Test
     public void testDuplicationSourceDirty() throws PartInitException {
         baseTest(true);
     }
 
 
     private void baseTest(boolean sourceDirty) throws PartInitException {
         DiagramRepositoryStore drs = (DiagramRepositoryStore)RepositoryManager.getInstance().getRepositoryStore(DiagramRepositoryStore.class);
 
 
         SWTBotTestUtil.createNewDiagram(bot);
         SWTBotEditor botEditor = bot.activeEditor();
 
 
         final int nbEditorsBefore = bot.editors().size();
         int nbProcessesBefore = drs.getAllProcesses().size();
 
         String processName = botEditor.getTitle().substring(0, botEditor.getTitle().lastIndexOf("(")-1);
         String processVersion = botEditor.getTitle().substring(botEditor.getTitle().lastIndexOf("(")+1,botEditor.getTitle().lastIndexOf(")")) +" duplicated";
 
         if (sourceDirty) {
             alterDiagram(botEditor);
         }
 

         bot.menu("Diagram").menu("Duplicate...").click();
         bot.text(0).setText(processName);
         bot.text(1).setText(processVersion);
         bot.button(IDialogConstants.OK_LABEL).click();
         bot.waitUntil(new ICondition() {
 
             public boolean test() throws Exception {
                 return nbEditorsBefore + 1 ==  bot.editors().size();
             }
 
             public void init(SWTBot bot) {
 
             }
 
             public String getFailureMessage() {
                 return "There should be only one more editor open";
             }
         });
 
         int nbProcessAfte = drs.getAllProcesses().size();
         assertEquals("There should be only one more process defined", nbProcessesBefore + 1, nbProcessAfte);
 
         if (sourceDirty) {
             botEditor.save();
         }
 
 
         MainProcess newDiagram = drs.getDiagram(processName, processVersion).getContent();
         List<Pool> pools = ModelHelper.getAllItemsOfType(newDiagram, ProcessPackage.Literals.POOL);
         final ProcessConfigurationRepositoryStore store = (ProcessConfigurationRepositoryStore) RepositoryManager.getInstance().getRepositoryStore(ProcessConfigurationRepositoryStore.class);
         for(Pool p : pools){
             String id = ModelHelper.getEObjectID(p);
             ProcessConfigurationFileStore file = store.getChild(id+"."+ProcessConfigurationRepositoryStore.CONF_EXT);
             assertNotNull("Process configuration is missing after duplicate for "+p.getName()+" ("+p.getVersion()+")",file);
             final Configuration conf = file.getContent();
             assertTrue("Actor mapping is missing in process configuraiton after duplicate for "+p.getName()+" ("+p.getVersion()+")",!conf.getActorMappings().getActorMapping().isEmpty() );
         }
     }
 
     private void alterDiagram(SWTBotEditor botEditor) {
         SWTBotGefEditor gmfEditor = bot.gefEditor(botEditor.getTitle());
         gmfEditor.activateTool("Human");
         gmfEditor.click(200, 200);
     }
 
     @Before
     public void cleanEditors(){
         bot.saveAllEditors();
         bot.closeAllEditors();
     }
 }
