 package org.jboss.community.pv243.controller;
 
 import java.io.Serializable;
 
 import javax.annotation.PostConstruct;
 import javax.enterprise.context.SessionScoped;
 import javax.enterprise.inject.Produces;
 import javax.faces.application.FacesMessage;
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.convert.Converter;
 import javax.inject.Inject;
 import javax.inject.Named;
 
 import org.jboss.community.pv243.model.Reservation;
 import org.jboss.community.pv243.model.Restaurant;
 import org.jboss.community.pv243.model.User;
 import org.jboss.community.pv243.service.ReservationManager;
 import org.jboss.community.pv243.service.RestaurantManager;
 import org.jboss.community.pv243.service.UserManager;
 
 @Named
 @SessionScoped
 public class UserReservationsController implements Serializable, Converter{
 
 	private static final long serialVersionUID = -2801739162098295137L;
 
 	@Inject
 	FacesContext facesContext;
 
 	@Inject
 	ReservationManager reservationManager;
 	
 	@Inject
 	RestaurantManager restaurantManager;
 
 	private Reservation newReservation;
 	
 	private User user;
 
 	private boolean edit = false;
 	
 	@Inject
 	UserManager userManager;
 	
 	@Override
 	public Object getAsObject(FacesContext context, UIComponent component,
 			String value) {
 		int id = Integer.valueOf(value);
 		return restaurantManager.getRestaurant(id);
 	}
 
 	@Override
 	public String getAsString(FacesContext context, UIComponent component,
 			Object value) {
 		return String.valueOf(((Restaurant)value).getId());
 	}
 
 	public User getLoggedUser(){
 		if (!facesContext.getExternalContext().isUserInRole("USER")){
 			throw new IllegalStateException("Uzivatel neni v roli USER");
 		}
		user = userManager.getUserByEmail(facesContext.getExternalContext().getUserPrincipal().getName());		
 		return user;
 	}
 	
 	@Produces
 	@Named
 	public Reservation getUserReservation() {
 		return newReservation;
 	}
 
 	public void clear() {
 		edit = false;
 		initReservation();
 	}
 
 	public boolean isEdit() {
 		return edit;
 	}
 
 	public void registerReservation() {
 		newReservation.setUser(getLoggedUser());
 		reservationManager.createReservation(newReservation);
 		facesContext.addMessage(null, new FacesMessage(
 				FacesMessage.SEVERITY_INFO, "Registration successful",
 				"Reservation was successfuly registered"));
 		initReservation();
 	}
 	
 	public void deleteReservation(Reservation reservation){
 		reservationManager.removeReservation(reservation);
 		edit = false;
 		initReservation();
 	}
 
 	public void editReservation(Reservation newReservation) {
 		this.newReservation = reservationManager.getReservation((newReservation.getId()));
 		edit = true;
 	}
 
 	public void save() {
 		reservationManager.updateReservation(newReservation);
 		edit = false;
 		initReservation();
 	}
 
 	@PostConstruct
 	public void initReservation() {
 		newReservation = new Reservation();
 	}
 
 	public void updateReservation(Reservation r) {
 		reservationManager.updateReservation(r);
 	}
 
 }
