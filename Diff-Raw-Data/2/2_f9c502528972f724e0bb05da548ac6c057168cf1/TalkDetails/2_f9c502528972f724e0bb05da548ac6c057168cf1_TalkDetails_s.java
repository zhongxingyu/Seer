 package com.prodyna.pac.conference.web.beans;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.faces.application.FacesMessage;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ViewScoped;
 import javax.inject.Inject;
 
 import com.prodyna.pac.conference.conference.api.model.Conference;
 import com.prodyna.pac.conference.conference.api.service.ConferenceService;
 import com.prodyna.pac.conference.room.api.model.Room;
 import com.prodyna.pac.conference.room.api.service.RoomService;
 import com.prodyna.pac.conference.speaker.api.model.Speaker;
 import com.prodyna.pac.conference.talk.api.model.Talk;
 import com.prodyna.pac.conference.talk.api.service.OutOfConferenceDateRangeException;
 import com.prodyna.pac.conference.talk.api.service.RoomNotAvailableException;
 import com.prodyna.pac.conference.talk.api.service.TalkService;
 import com.prodyna.pac.conference.web.constants.ViewConstants;
 
 @ManagedBean(name = "talkDetails")
 @ViewScoped
 public class TalkDetails extends AbstractEditEntityMaskBean {
 
 	private static final long serialVersionUID = 1L;
 
 	@Inject
 	TalkService talkService;
 
 	@Inject()
 	ConferenceService conferenceService;
 
 	@Inject()
 	RoomService roomService;
 
 	List<Speaker> speakers;
 
 	private Talk talk;
 
 	public Talk getTalk() throws Exception
 	{
 		if (talk == null) {
 			if (isNewMode()) {
 				initNewTalk();
 			} else {
 				talk = talkService.getTalkById(getId());
 			}
 		}
 		return talk;
 	}
 
 	public void setTalk(Talk talk)
 	{
 		this.talk = talk;
 	}
 
 	public void setConferenceId(Long conferenceId) throws Exception
 	{
 
 		Conference conferenceById = conferenceService
 				.getConferenceById(conferenceId);
 
 		getTalk().setConference(conferenceById);
 	}
 
 	public Long getConferenceId() throws Exception
 	{
 		if (getTalk().getConference() == null) {
 			return null;
 		} else {
 			return getTalk().getConference().getId();
 		}
 	}
 
 	public void setRoomId(Long roomId) throws Exception
 	{
 
 		Room room = roomService.getRoomById(roomId);
 
 		getTalk().setRoom(room);
 	}
 
 	public Long getRoomId() throws Exception
 	{
 		if (getTalk().getRoom() == null) {
 			return null;
 		} else {
 			return getTalk().getId();
 		}
 	}
 
 	public List<Speaker> getSpeakers() throws Exception
 	{
 		if (speakers == null) {
 			if (isNewMode()) {
 				speakers = new ArrayList<Speaker>();
 			} else {
 				speakers = talkService.getSpeakersByTalk(getId());
 			}
 		}
 
 		return speakers;
 	}
 
 	public String createTalk() throws Exception
 	{
 
		String outcome = ViewConstants.VIEW_TALK_EDIT;
 		try {
 			talkService.createTalk(talk);
 			talkService.updateTalkSpeakers(talk, speakers);
 			facesContext.addMessage(null, new FacesMessage(
 					FacesMessage.SEVERITY_INFO, "Saved!", "Talk saved"));
 
 			outcome = ViewConstants.VIEW_TALK_LIST;
 		} catch (RoomNotAvailableException e) {
 			FacesMessage m = new FacesMessage(
 					FacesMessage.SEVERITY_ERROR,
 					"Room is not available. Please choose another one or update times",
 					null);
 			facesContext.addMessage(null, m);
 		} catch (OutOfConferenceDateRangeException e) {
 			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,
 					"Talk start and end must be in conference timespan.", null);
 			facesContext.addMessage(null, m);
 		}
 
 		catch (Exception e) {
 			String errorMessage = getRootErrorMessage(e);
 			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,
 					errorMessage, "Saving unsuccessful");
 			facesContext.addMessage(null, m);
 		}
 
 		return outcome;
 	}
 
 	public void updateTalk() throws Exception
 	{
 
 		try {
 			Talk talkUpdate = talkService.updateTalk(talk);
 			talkService.updateTalkSpeakers(talkUpdate, speakers);
 			facesContext.addMessage(null, new FacesMessage(
 					FacesMessage.SEVERITY_INFO, "Update!", "Talk updated"));
 
 		} catch (RoomNotAvailableException e) {
 			FacesMessage m = new FacesMessage(
 					FacesMessage.SEVERITY_ERROR,
 					"Room is not available. Please choose another one or update times",
 					null);
 			facesContext.addMessage(null, m);
 		} catch (OutOfConferenceDateRangeException e) {
 			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,
 					"Talk start and end must be in conference timespan.", null);
 			facesContext.addMessage(null, m);
 		}
 
 		catch (Exception e) {
 			String errorMessage = getRootErrorMessage(e);
 			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,
 					errorMessage, "Updating talk unsuccessful");
 			facesContext.addMessage(null, m);
 		}
 
 	}
 
 	public void assignSpeaker(Speaker speaker)
 	{
 		speakers.add(speaker);
 	}
 
 	public void unassignSpeaker(Speaker speaker)
 	{
 		speakers.remove(speaker);
 	}
 
 	public void initNewTalk()
 	{
 		talk = new Talk();
 	}
 
 	@Override
 	public String cancelEditing()
 	{
 		initNewTalk();
 
 		return ViewConstants.VIEW_TALK_LIST;
 	}
 
 	public void deleteTalk(Talk talk) throws Exception
 	{
 		talkService.deleteTalk(talk);
 	}
 
 }
