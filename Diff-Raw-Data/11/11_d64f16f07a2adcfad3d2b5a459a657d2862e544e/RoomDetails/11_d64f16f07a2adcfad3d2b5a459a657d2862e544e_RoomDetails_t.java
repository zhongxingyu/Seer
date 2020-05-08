 package com.prodyna.pac.conference.web.beans;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.faces.application.FacesMessage;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ViewScoped;
 import javax.faces.context.FacesContext;
 import javax.inject.Inject;
 
 import com.prodyna.pac.conference.room.api.model.Room;
 import com.prodyna.pac.conference.room.api.service.RoomReferencedException;
 import com.prodyna.pac.conference.room.api.service.RoomService;
 import com.prodyna.pac.conference.talk.api.model.Talk;
 import com.prodyna.pac.conference.talk.api.service.TalkService;
 import com.prodyna.pac.conference.web.constants.ViewConstants;
 
 @ManagedBean(name = RoomDetails.BEAN_NAME)
 @ViewScoped
 public class RoomDetails extends AbstractEditEntityMaskBean {
 
 	static final String BEAN_NAME = "roomDetails";
 
 	@Inject
 	RoomService roomService;
 
 	@Inject
 	TalkService talkService;
 
 	@Inject
 	private FacesContext facesContext;
 
 	private Room room;
 
 	private static final long serialVersionUID = 1L;
 
 	public Room getRoom() throws Exception
 	{
 		if (room == null) {
 			if (isNewMode()) {
 				initNewRoom();
 			} else {
 				room = roomService.getRoomById(getId());
 			}
 		}
 		return room;
 	}
 
 	public void setRoom(Room room)
 	{
 		this.room = room;
 	}
 
 	public List<Talk> getTalks() throws Exception
 	{
 
 		List<Talk> talksByRoom = new ArrayList<Talk>();
 		if (!getRoom().isNew()) {
 			talksByRoom = talkService.getTalksByRoom(getRoom().getId());
 		}
 		return talksByRoom;
 	}
 
 	public String createRoom() throws Exception
 	{
 
		String outcome = null;
 		try {
 			roomService.createRoom(room);
 			facesContext.addMessage(null, new FacesMessage(
 					FacesMessage.SEVERITY_INFO, "Saved!", "Room saved"));
 			room = null;
			outcome = ViewConstants.VIEW_ROOM_LIST;
 		} catch (Exception e) {
 			String errorMessage = getRootErrorMessage(e);
 			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,
 					errorMessage, "Saving unsuccessful");
 			facesContext.addMessage(null, m);
 		}
 
 		return outcome;
 	}
 
 	public String updateRoom() throws Exception
 	{
		String outcome = null;
 		try {
 			roomService.updateRoom(room);
 			facesContext.addMessage(null, new FacesMessage(
 					FacesMessage.SEVERITY_INFO, "Updated!", "Room updated"));
			outcome = ViewConstants.VIEW_ROOM_LIST;
 		} catch (Exception e) {
 			String errorMessage = getRootErrorMessage(e);
 			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,
 					errorMessage, "Updating room unsuccessful");
 			facesContext.addMessage(null, m);
 		}
 		return outcome;
 	}
 
 	public void initNewRoom()
 	{
 		room = new Room();
 	}
 
 	@Override
 	public String cancelEditing()
 	{
 		initNewRoom();
 
 		return ViewConstants.VIEW_ROOM_LIST;
 	}
 
 	public void deleteRoom(Room room) throws Exception
 	{
 		try {
 			roomService.deleteRoom(room);
 		} catch (RoomReferencedException e) {
 			facesContext
 					.addMessage(
 							null,
 							new FacesMessage(
 									FacesMessage.SEVERITY_ERROR,
 									"Room '"
 											+ room.getName()
 											+ "' cannot be deleted because it is still in use.",
 									null));
 
 		}
 	}
 
 }
