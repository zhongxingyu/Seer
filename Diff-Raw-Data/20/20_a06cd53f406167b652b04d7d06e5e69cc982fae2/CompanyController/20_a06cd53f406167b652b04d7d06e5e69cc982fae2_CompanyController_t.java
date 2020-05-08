 package com.iknition.firstzk.view.controller;
 
 
 import com.iknition.firstzk.beans.Company;
 import com.iknition.firstzk.service.CompanyManager;
 import com.iknition.firstzk.service.ServiceLocator;
 import com.iknition.firstzk.view.controller.renderer.CompanyListRenderer;
 import java.util.List;
 import org.zkoss.zk.ui.Component;
 import org.zkoss.zk.ui.event.Event;
 import org.zkoss.zk.ui.event.EventListener;
 import org.zkoss.zk.ui.event.EventQueues;
 import org.zkoss.zk.ui.select.annotation.Listen;
 import org.zkoss.zk.ui.util.GenericForwardComposer;
 import org.zkoss.zkplus.databind.AnnotateDataBinder;
 import org.zkoss.zul.Button;
 import org.zkoss.zul.Grid;
 import org.zkoss.zul.ListModelList;
 import org.zkoss.zul.Listbox;
 import org.zkoss.zul.Listitem;
 import org.zkoss.zul.Messagebox;
 
 public class CompanyController extends GenericForwardComposer {
 
 	private static final long serialVersionUID = 20111130143824L;
 	private AnnotateDataBinder binder;
 	private Listbox companyList;
 	private Grid editCompanyGrid;
 	private Button createCompany;
 	private Button updateCompany;
 	private Button deleteCompany;
 	
 	private CompanyManager manager = ServiceLocator.getCompanyManager();
 	private Company _company;
 
 	@Override
 	public void doAfterCompose(Component comp) throws Exception {
 		super.doAfterCompose(comp);
 		binder = (AnnotateDataBinder) page.getAttribute("binder");
 		_company = new Company();
                List<Company> companies = manager.getCompanyList();
 		companyList.setModel(new ListModelList(companies));
 		companyList.setItemRenderer(new CompanyListRenderer());
 	}
 	
 	private ListModelList getModel() {
 		return (ListModelList) companyList.getModel();
 	}
 	
 	public void onClick$resetCompany() {
 		companyList.clearSelection();
 		_company = new Company();
 		binder.loadComponent(editCompanyGrid);
 		createCompany.setDisabled(false);
 		updateCompany.setDisabled(true);
 		deleteCompany.setDisabled(true);
 		page.getFellow("contactDiv").setVisible(false);
 	}
 	
 	//set selection to edit data
 	public void onSelect$companyList() {
 		_company = (Company) companyList.getSelectedItem().getValue();
 		Company companyl = manager.getCompany(_company.getIdcompany());
                 binder.loadComponent(editCompanyGrid);
 		createCompany.setDisabled(true);
 		updateCompany.setDisabled(false);
 		deleteCompany.setDisabled(false);
 		// used for Hibernate lazy-loading
                 System.out.println("id es: "+_company.getIdcompany());
 		//_company = (Company) ServiceLocator.getHibernateSession().merge(_company);
 		Event event = new Event("onLoad", page.getFellow("contactDiv"), companyl);
 		EventQueues.lookup("loadContact", EventQueues.DESKTOP, true).publish(event);
 	}
 	
 	//register onClick event for creating new object into list model
 	public void onClick$createCompany() throws InterruptedException {
 		if (_company.getName() == null && _company.getCountry() == null) {
 			Messagebox.show("no new content to add");
 		} else {
 			manager.save(_company);
 			getModel().add(_company);
 		}
 	}
 	
 	//register onClick event for updating edited data in list model
 	//public void onClick$updateCompany() throws InterruptedException {
         @Listen("onClick=#updateCompany")
             public void caca(Event event)  throws InterruptedException {    
 		Listitem listItem = companyList.getSelectedItem();
 		manager.save(_company);
 		listItem.setValue(_company);
 		int index = companyList.getSelectedIndex();
 		getModel().set(index, _company);
 	}
 	
 	//register onClick event for removing data in list model
 	public void onClick$deleteCompany() throws InterruptedException {
 		Messagebox.show("Are you sure to delete?", null, Messagebox.YES | Messagebox.NO, Messagebox.QUESTION, new EventListener() {
 			public void onEvent(Event event) throws Exception {
 				if(event.getName().equals("onYes")) {
 					getModel().remove(_company);
 					manager.delete(_company);
 				}
 			}
 		});
 	}
 	
 	public Company getCompany() {
 		return _company;
 	}
 	
 	public void setCompany(Company company) {
 		_company = company;
 	}
 }
