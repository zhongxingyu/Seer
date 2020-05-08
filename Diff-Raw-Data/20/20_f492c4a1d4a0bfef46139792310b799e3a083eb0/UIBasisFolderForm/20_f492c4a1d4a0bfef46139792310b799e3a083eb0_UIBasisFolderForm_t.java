 package searchBasis.portlet.component;
 
 import org.exoplatform.container.ExoContainer;
 import org.exoplatform.container.ExoContainerContext;
 import org.exoplatform.services.jcr.RepositoryService;
 import org.exoplatform.webui.config.annotation.ComponentConfig;
 import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
 import org.exoplatform.webui.form.UIForm;
 import org.exoplatform.webui.form.input.UICheckBoxInput;
 import sun.rmi.runtime.NewThreadAction;
 
 import javax.jcr.nodetype.NodeType;
 import javax.jcr.nodetype.PropertyDefinition;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Kevin
  * Date: 6/12/12
  * Time: 15:35
  * To change this template use File | Settings | File Templates.
  */
 @ComponentConfig(
         lifecycle = UIFormLifecycle.class,
         template =  "app:/groovy/SearchBasis/portlet/UIBasisFolderForm.gtmpl"
 )
 public class UIBasisFolderForm extends UIForm {
 
     public static final String FIELD_FOLDER_ID = "exo:title" ;
     private PropertyDefinition[] basisFolderNodetypeProperties;
 
     public UIBasisFolderForm() throws Exception {
 
     }
     public void  load(String parent) throws Exception {
         ExoContainer exoContainer = ExoContainerContext.getCurrentContainer();
         RepositoryService rs = (RepositoryService) exoContainer.getComponentInstanceOfType(RepositoryService.class);
         NodeType basisFolderNodetype = rs.getRepository("repository").getNodeTypeManager().getNodeType("basis:basisFolder");
         basisFolderNodetypeProperties = basisFolderNodetype.getPropertyDefinitions();
         UIPropertyInputForm uiPropertyInputForm = addChild(UIPropertyInputForm.class,null, FIELD_FOLDER_ID);
         uiPropertyInputForm.load(null, "basisFolder.label.folderNumber");
         for (PropertyDefinition property : basisFolderNodetypeProperties) {
             if(property.getName().contains("basis")) {
                 if(!property.getName().contains("folderLanguage")) {
                     uiPropertyInputForm = addChild(UIPropertyInputForm.class,null, parent+"_"+property.getName());
                     uiPropertyInputForm.load(property,"basisFolder");
                 }
             }
         }
     }
 
     public PropertyDefinition[] getBasisFolderNodetypeProperties() {
         return basisFolderNodetypeProperties;
     }
 
     public void setBasisFolderNodetypeProperties(PropertyDefinition[] basisFolderNodetypeProperties) {
         this.basisFolderNodetypeProperties = basisFolderNodetypeProperties;
     }
 }
