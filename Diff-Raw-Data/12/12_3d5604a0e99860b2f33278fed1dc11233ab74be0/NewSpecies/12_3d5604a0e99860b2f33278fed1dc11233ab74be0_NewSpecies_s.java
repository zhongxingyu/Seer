 package org.openforis.specieseditor;
 
 import java.util.List;
 
 import javax.servlet.ServletContext;
 
 import org.openforis.collect.manager.SpeciesManager;
 import org.openforis.idm.model.TaxonOccurrence;
 import org.springframework.context.ApplicationContext;
 import org.zkoss.zk.ui.Component;
 import org.zkoss.zk.ui.select.SelectorComposer;
 import org.zkoss.zk.ui.select.annotation.Listen;
 import org.zkoss.zk.ui.select.annotation.Wire;
 import org.zkoss.zul.Button;
 import org.zkoss.zul.Checkbox;
 import org.zkoss.zul.Messagebox;
 import org.zkoss.zul.Textbox;
 
 
 public class NewSpecies extends SelectorComposer<Component> {
 	@Wire
     private Button submitButton;    
    
 	@Wire
 	private Textbox taxonomyName;
 	
 	@Wire
 	private Textbox speciesName;
 	
 	@Wire
 	private Textbox vernacularName;
 	
 	@Wire
 	private Textbox qualifier1Name;
 	
	@Wire
	private SpeciesManager speciesManager;
     
     @Listen("onClick = #okButton")
     public void okButton(){
     	String taxonomy = taxonomyName.getText();
     	String species = speciesName.getText();
     	String vernacular = vernacularName.getText();
     	String qualifier1 = qualifier1Name.getText();
    	Messagebox.show("Saving of " + taxonomy + "," + speciesName + ", " + vernacular + "," + qualifier1);
     	
     	List<TaxonOccurrence> speciesList = speciesManager.findByScientificName(taxonomy, species, 1);
     	Messagebox.show(speciesList.size()+"");
     }
 }
