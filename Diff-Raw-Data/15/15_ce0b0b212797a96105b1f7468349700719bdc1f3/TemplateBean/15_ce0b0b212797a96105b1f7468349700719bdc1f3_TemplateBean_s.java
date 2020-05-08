 package server;
 
 import java.util.List;
 import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
 import javax.faces.event.AbortProcessingException;
 import javax.faces.event.ActionEvent;
 import javax.faces.event.ActionListener;
 
import org.primefaces.component.behavior.ajax.AjaxBehavior;
import org.primefaces.component.menu.Menu;
 import org.primefaces.component.menuitem.MenuItem;
 import org.primefaces.component.submenu.Submenu;
 import org.primefaces.model.DefaultMenuModel;
 import org.primefaces.model.MenuModel;
 
 import ctrl.DBExRules;
 import ctrl.DBModManual;
 
 import data.ExRules;
 import data.ModManual;
 import data.Module;
 
 @ManagedBean(name="TemplateBean")
@RequestScoped
 public class TemplateBean implements ActionListener {
 	
 	private String exRules, modMan, module;
 	private List<ExRules> exRulesList;
 	private List<ModManual> modManList;
 	private List<Module> moduleList;
 	private MenuModel model;
 	
 	public TemplateBean() {
 		
 		model = new DefaultMenuModel();
 		Submenu submenu = new Submenu();
 		submenu.setLabel("Pruefungsordnung");
 		
 		exRulesList = DBExRules.loadExRules();
 		
 		for(int i = 0; i < exRulesList.size(); i++) {
 			MenuItem m = new MenuItem();
 			m.setValue(exRulesList.get(i).getExRulesTitle());
 			m.setAjax(true);
			
 			m.addActionListener(this);
 			submenu.getChildren().add(m);
 		}
 		
 		model.addSubmenu(submenu);
 		
 		
 	}
 	
 	public void loadModMan(String exRulesTitle) {
 		exRules = exRulesTitle;
 		
 		modManList = DBModManual.loadAllModManuals(exRulesTitle);
 		
 		Submenu submenu = new Submenu();
 		submenu.setLabel(exRulesTitle);
 		
 		for(int i = 0; i < modManList.size(); i++) {
 			MenuItem m = new MenuItem();
 			m.setValue(modManList.get(i).getModManTitle());
 			m.addActionListener(this);
 			submenu.getChildren().add(m);
 		}
 		
 		model = new DefaultMenuModel();
 		model.addSubmenu(submenu);
 	}
 	
 	@Override
 	public void processAction(ActionEvent arg0) throws AbortProcessingException {
 		// TODO Auto-generated method stub
 		System.out.println((String)((MenuItem)arg0.getSource()).getValue());
 		loadModMan((String)((MenuItem)arg0.getSource()).getValue());
 		
 	}
 
 	/**
 	 * @return the exRules
 	 */
 	public String getExRules() {
 		return exRules;
 	}
 
 	/**
 	 * @param exRules the exRules to set
 	 */
 	public void setExRules(String exRules) {
 		this.exRules = exRules;
 	}
 
 	/**
 	 * @return the modMan
 	 */
 	public String getModMan() {
 		return modMan;
 	}
 
 	/**
 	 * @param modMan the modMan to set
 	 */
 	public void setModMan(String modMan) {
 		this.modMan = modMan;
 	}
 
 	/**
 	 * @return the module
 	 */
 	public String getModule() {
 		return module;
 	}
 
 	/**
 	 * @param module the module to set
 	 */
 	public void setModule(String module) {
 		this.module = module;
 	}
 
 	/**
 	 * @return the exRulesList
 	 */
 	public List<ExRules> getExRulesList() {
 		return exRulesList;
 	}
 
 	/**
 	 * @param exRulesList the exRulesList to set
 	 */
 	public void setExRulesList(List<ExRules> exRulesList) {
 		this.exRulesList = exRulesList;
 	}
 
 	/**
 	 * @return the modManList
 	 */
 	public List<ModManual> getModManList() {
 		return modManList;
 	}
 
 	/**
 	 * @param modManList the modManList to set
 	 */
 	public void setModManList(List<ModManual> modManList) {
 		this.modManList = modManList;
 	}
 
 	/**
 	 * @return the moduleList
 	 */
 	public List<Module> getModuleList() {
 		return moduleList;
 	}
 
 	/**
 	 * @param moduleList the moduleList to set
 	 */
 	public void setModuleList(List<Module> moduleList) {
 		this.moduleList = moduleList;
 	}
 
 	/**
 	 * @return the model
 	 */
 	public MenuModel getModel() {
 		return model;
 	}
 
 	/**
 	 * @param model the model to set
 	 */
 	public void setModel(MenuModel model) {
 		this.model = model;
 	}
 	
 }
