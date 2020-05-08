 /*
  * Copyrighted 2012-2013 Netherlands eScience Center.
  *
  * Licensed under the Apache License, Version 2.0 (the "License").  
  * You may not use this file except in compliance with the License. 
  * For details, see the LICENCE.txt file location in the root directory of this 
  * distribution or obtain the Apache License at the following location: 
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software 
  * distributed under the License is distributed on an "AS IS" BASIS, 
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
  * See the License for the specific language governing permissions and 
  * limitations under the License.
  * 
  * For the full license, see: LICENCE.txt (located in the root folder of this distribution). 
  * ---
  */
 // source: 
 
 package nl.nlesc.vlet.gui.vbrowser;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import nl.esciencecenter.ptk.GlobalProperties;
 import nl.esciencecenter.ptk.ui.widgets.NavigationBar;
 import nl.esciencecenter.ptk.util.logging.ClassLogger;
 import nl.nlesc.vlet.gui.actions.ActionCommand;
 import nl.nlesc.vlet.gui.actions.ActionCommandType;
 
 
 /**
  * BrowserController Action Listener listens for Menu Actions
  * and ToolBar (Button) Actions.
  * 
  * @author ptdeboer
  */
 public class BrowserControllerActionListener implements ActionListener
 {
     /** VBrowser to controller */
     BrowserController browserController = null;
 
    /** Constructor */ 
     BrowserControllerActionListener(BrowserController browserController)
     {
         this.browserController = browserController;
     }
 
     /** 
      * MenuItem action listener.
      * Delegates Actions to the BrowserController 
      */
     
     public void actionPerformed(ActionEvent e)
     {
         String cmdstr = e.getActionCommand();
         
         // check for NavigationBar 
        NavigationBar.NavigationAction nav=NavigationBar.NavigationAction.valueOf(cmdstr); 
         
         ActionCommand cmd=null; 
         
         if (nav!=null)
         {
             getLogger().debugPrintf("NavBarEvent:%s\n",e);
             
             switch(nav)
             {
                 case BROWSE_BACK:
                     cmd=new ActionCommand(ActionCommandType.BROWSEBACK); 
                     break; 
                 case BROWSE_UP:
                     cmd=new ActionCommand(ActionCommandType.BROWSEUP); 
                     break; 
                 case BROWSE_FORWARD:
                     cmd=new ActionCommand(ActionCommandType.BROWSEFORWARD); 
                     break; 
                 case REFRESH:
                     cmd=new ActionCommand(ActionCommandType.REFRESHALL); // master refresh (not node refresh) 
                     break; 
                 case LOCATION_EDITED:
                     cmd=null;// filter edit events 
                     break; 
                 case LOCATION_CHANGED:
                     cmd=new ActionCommand(ActionCommandType.LOCATIONBAR_CHANGED); 
                     break; 
                 default:
                     getLogger().warnPrintf("*** Oopsy: Unknown NavigationBar Event:"+nav); 
             }
         }
         else
         {
             cmd=ActionCommand.createFrom(cmdstr);
         }
         
         // forward ALL Actions to browserController: 
         if (cmd!=null)
             browserController.performGlobalMenuAction(cmd);
         
     }
 
     private ClassLogger getLogger()
     {
         return this.browserController.getLogger(); 
     }
 }
