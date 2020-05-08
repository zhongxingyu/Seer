 package searchBasis.portlet.component;
 
 import org.exoplatform.webui.config.annotation.ComponentConfig;
 import org.exoplatform.webui.config.annotation.EventConfig;
 import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
 import org.exoplatform.webui.core.model.SelectItemOption;
 import org.exoplatform.webui.event.Event;
 import org.exoplatform.webui.event.EventListener;
 import org.exoplatform.webui.form.*;
 import org.exoplatform.webui.form.input.UICheckBoxInput;
 import org.exoplatform.webui.form.wysiwyg.*;
 import org.exoplatform.webui.form.wysiwyg.UIFormWYSIWYGInput;
 
 import javax.jcr.nodetype.PropertyDefinition;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Kevin
  * Date: 7/12/12
  * Time: 13:23
  * To change this template use File | Settings | File Templates.
  */
 @ComponentConfig(
         lifecycle = UIFormLifecycle.class,
         template =  "app:/groovy/SearchBasis/portlet/UIPropertyInputForm.gtmpl"
 )
 public class UIPropertyInputForm extends UIForm {
 
     public UIPropertyInputForm() throws Exception {
     }
 
     public void load(PropertyDefinition property, String typeNode) throws Exception{
 
         String labelProperty;
         if(property != null){
             if(!property.getName().contains("Comments")) {
                 labelProperty = typeNode+".label." + property.getName().split("basis:")[1];
             }
             else {
                 labelProperty = typeNode+"_basis.label.comments";
             }
         }
         else {
             labelProperty = typeNode;
         }
         UICheckBoxInput uiCheckBoxInput = new UICheckBoxInput(labelProperty+"_checkBox",null,null);
         addUIFormInput(uiCheckBoxInput);
 
         List<SelectItemOption<String>> lsSearch = new ArrayList<SelectItemOption<String>>() ;
         addUIFormInput(new UIFormSelectBox(labelProperty+"_searchType", null, lsSearch));
         if(property != null){
             if(property.getRequiredType() != 5){
                 addUIFormInput(new UIFormStringInput(labelProperty, null, null));
             }
             else {
                 addUIFormInput(new UIFormDateTimeInput(labelProperty, null, new Date(), false));
             }
         }
         else{
             addUIFormInput(new UIFormStringInput(labelProperty, null, null));
         }
 
 
     }
 
 
 }
