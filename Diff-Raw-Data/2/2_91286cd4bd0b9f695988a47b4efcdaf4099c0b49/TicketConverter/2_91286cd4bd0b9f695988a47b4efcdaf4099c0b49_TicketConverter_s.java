 package com.supinfo.ticketmanager.web.converter;
 
 import javax.ejb.EJB;
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.convert.Converter;
 import javax.inject.Named;
 
 import com.supinfo.ticketmanager.entity.Ticket;
 import com.supinfo.ticketmanager.service.TicketService;
 
 @Named
 public class TicketConverter implements Converter {
 
 	@EJB
 	private TicketService ticketService;
 	
 	
 	@Override
 	public Object getAsObject(FacesContext context, UIComponent component, String value) {
 		return ticketService.findTicketById(Long.valueOf(value));
 	}
 
 	@Override
 	public String getAsString(FacesContext context, UIComponent component, Object value) {
		return String.valueOf(((Ticket) value).getId());
 	}
 
 }
