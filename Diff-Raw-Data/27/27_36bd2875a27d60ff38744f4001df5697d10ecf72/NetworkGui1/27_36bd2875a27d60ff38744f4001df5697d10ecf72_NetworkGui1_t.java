 /* AWE - Amanzi Wireless Explorer
  * http://awe.amanzi.org
  * (C) 2008-2009, AmanziTel AB
  *
  * This library is provided under the terms of the Eclipse Public License
  * as described at http://www.eclipse.org/legal/epl-v10.html. Any use,
  * reproduction or distribution of the library constitutes recipient's
  * acceptance of this agreement.
  *
  * This library is distributed WITHOUT ANY WARRANTY; without even the
  * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  */
 
 package org.amanzi.neo.loader.ui.wizards;
 
 import java.io.File;
 import java.util.Arrays;
 import java.util.Collection;
 
 import org.amanzi.neo.loader.core.CommonConfigData;
 import org.amanzi.neo.loader.core.IValidateResult;
 import org.amanzi.neo.loader.core.IValidateResult.Result;
 import org.amanzi.neo.loader.ui.NeoLoaderPluginMessages;
 import org.amanzi.neo.loader.ui.utils.LoaderUiUtils;
 import org.amanzi.neo.services.INeoConstants;
 import org.amanzi.neo.services.enums.NodeTypes;
 import org.amanzi.neo.services.ui.NeoServiceProviderUi;
 import org.amanzi.neo.services.utils.Utils;
 import org.apache.commons.lang.StringUtils;
 import org.eclipse.jface.dialogs.DialogPage;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.traversal.TraversalDescription;
 
 /**
  * <p>
  *Main GUI for load network (TODO maybe merge with LoadNetworkMainPage.class)
  * </p>
  * @author tsinkel_a
  * @since 1.0.0
  */
 public class NetworkGui1 extends AbstractMainPage<CommonConfigData> {
 
     
     /**
      * Instantiates a new ericsson network gui.
      */
     public NetworkGui1() {
         super("configMainPage", false);
        setTitle(NeoLoaderPluginMessages.NetworkConfigurationImportWizard_PAGE_DESCR);
         
     }
 
     @Override
     protected String getRootLabel() {
         return "Network:";
     }
 
     @Override
     protected String getLoaderLabel() {
         return "Loader:";
     }
 
     @Override
     protected String[] getRootItems() {
         final String  projectName = LoaderUiUtils.getAweProjectName();
         TraversalDescription td = Utils.getTDRootNodesOfProject(projectName, null);
         Node refNode = NeoServiceProviderUi.getProvider().getService().getReferenceNode();
         restrictedNames.clear();
         rootList.clear();
         for (Node node : td.traverse(refNode).nodes()) {
             String id = node.getProperty(INeoConstants.PROPERTY_NAME_NAME).toString();
             if (NodeTypes.NETWORK.checkNode(node)) { //$NON-NLS-1$
                 rootList.put(id, node);
             } else {
                 restrictedNames.add(id);
             }
         }
 
         String[] result = rootList.keySet().toArray(new String[] {});
         Arrays.sort(result);
         return result;
     }
 
     @Override
     protected boolean validateConfigData(CommonConfigData configurationData) {
 
 //        File file = new File(fileName);
 //        if (!(file.isAbsolute() && file.exists())){
 //            setMessage(NeoLoaderPluginMessages.NetworkSiteImportWizardPage_NO_FILE,DialogPage.ERROR); 
 //            return false;         
 //        }
         if ( StringUtils.isEmpty(rootName)){
             setMessage(NeoLoaderPluginMessages.NetworkSiteImportWizardPage_NO_NETWORK,DialogPage.ERROR); 
             return false;
         }
         if (restrictedNames.contains(rootName)){
             setMessage(NeoLoaderPluginMessages.NetworkSiteImportWizardPage_RESTRICTED_NETWORK_NAME,DialogPage.ERROR);  
             return false;
         }
         if (getSelectedLoader() == null){
             setMessage(NeoLoaderPluginMessages.NetworkSiteImportWizardPage_NO_TYPE,DialogPage.ERROR); 
             return false;           
         }
         //TODO must be refactoring after change loaders
         Collection<File> files = viewer.getSelectedFiles(null);
         configurationData.setProjectName(LoaderUiUtils.getAweProjectName());
         configurationData.setCrs(getSelectedCRS());
         configurationData.setDbRootName(rootName);
         configurationData.setFileToLoad(files);     
         if (files.isEmpty()) {
             configurationData.setRoot(null);
             try {
                 if (getSelectedLoader().getValidator().validate(configurationData).getResult() == Result.SUCCESS) {
                     setMessage(NeoLoaderPluginMessages.NetworkSiteImportWizardPage_NO_FILE, DialogPage.WARNING);
                     return true;
                 }
             } catch (Exception e) {
                 //not handled
             }
             setMessage(NeoLoaderPluginMessages.NetworkSiteImportWizardPage_NO_FILE, DialogPage.ERROR);
             return false;
         }
         viewer.storeDefSelection(null);
 
         configurationData.setRoot(files.iterator().next());  
         getSelectedLoader().getValidator().filter(configurationData);
         if (files.size()!=configurationData.getAllLoadedFiles().size()){
             viewer.getTreeViewer().setSelection(new StructuredSelection(configurationData.getAllLoadedFiles().toArray()), false);
             return validateConfigData(configurationData);
         }
         if (configurationData.getAllLoadedFiles().isEmpty()){
             setMessage(NeoLoaderPluginMessages.NetworkSiteImportWizardPage_NO_FILE,DialogPage.ERROR); 
             return false;            
         }
         configurationData.setRoot(configurationData.getAllLoadedFiles().iterator().next());
         IValidateResult result = getSelectedLoader().getValidator().validate(configurationData);
         if (result.getResult()==Result.FAIL){
             setMessage(String.format(result.getMessages(), getSelectedLoader().getDescription()),DialogPage.ERROR); 
             return false;          
         }else if (result.getResult()==Result.UNKNOWN){
             setMessage(String.format(result.getMessages(), getSelectedLoader().getDescription()),DialogPage.WARNING); 
         }else{
             setMessage(""); //$NON-NLS-1$
         }
         return true;
     }
 
 }
