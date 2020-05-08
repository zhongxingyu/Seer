 package org.jboss.community.pv243.controller;
 
 import java.io.Serializable;
 
 import javax.annotation.PostConstruct;
 import javax.enterprise.context.SessionScoped;
 import javax.enterprise.inject.Produces;
 import javax.faces.application.FacesMessage;
 import javax.faces.context.FacesContext;
 import javax.inject.Inject;
 import javax.inject.Named;
 
 import org.jboss.community.pv243.model.MenuItem;
 import org.jboss.community.pv243.model.Restaurant;
 import org.jboss.community.pv243.service.MenuItemManager;
 import org.jboss.community.pv243.service.RestaurantManager;
 
 @Named
 @SessionScoped
 public class MenuController implements Serializable{
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -554980009537138357L;
 
 	@Inject
 	FacesContext facesContext;
 	
 	@Inject 
 	MenuItemManager menuItemManager;
 	
 	@Inject
 	RestaurantManager restaurantManager;
 
 	private Restaurant restaurant;
 	
 	private MenuItem menuItem;
 	
 	private boolean edit = false;
 	
 	public Restaurant getLoggedRestaurant(){
 		if (!facesContext.getExternalContext().isUserInRole("MANAGER")){
 			throw new IllegalStateException("Uzivatel neni v roli MANAGER");
 		}
 		//if (restaurant== null){
 		restaurant = restaurantManager.getRestaurantByEmail(facesContext.getExternalContext().getUserPrincipal().getName());
 		//}
 		return restaurant;
 	}
 	
 	@Produces
 	@Named
 	public MenuItem getMenuItem(){
 		return menuItem;
 	}
 	
 	public void createMenuItem(){
 		menuItem.setRestaurant(getLoggedRestaurant());
 		menuItemManager.createMenuItem(menuItem);
 		facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, 
 				"Creation successful", "Menu Item was successfuly created"));
 		menuItem = new MenuItem();
 		
 	}
 	
 	public void deleteMenuItem(MenuItem menuItem){
 		menuItemManager.removeMenuItem(menuItem);
 		initMenuItem();
 	}
 	
 	public void editMenuItem(MenuItem menuItem){
 		this.menuItem = menuItemManager.getMenuItem(menuItem.getId());
 		edit = true;
 	}
 	
 	public void save() {
 		menuItemManager.updateMenuItem(menuItem);
 		edit = false;
 		initMenuItem();
 	}
 	
 	public void clear() {
 		edit = false;
 		initMenuItem();
 	}
 	
 	public boolean isEdit() {
 		return edit;
 	}
 	
 	
 	@PostConstruct
 	public void initMenuItem(){
 		menuItem = new MenuItem();
 		menuItem.setRestaurant(getLoggedRestaurant());
 	}
 }
