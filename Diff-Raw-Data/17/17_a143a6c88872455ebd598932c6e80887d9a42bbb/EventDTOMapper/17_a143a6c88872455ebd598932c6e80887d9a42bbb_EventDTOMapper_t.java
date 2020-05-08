 package com.eventinvitation.domain.dto;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.eventinvitation.domain.Event;
 import com.eventinvitation.domain.EventMailingList;
 
 public class EventDTOMapper {
 
 	public static EventDTO mapEventToEventDTO(Event event){
 		if(event == null){
 			return null;
 		}
 		EventDTO eventDTO = new EventDTO();
 		eventDTO.setAcceptListDTOs(mapMaillingListToAcceptListDTO(event.getMaillingList()));
 		eventDTO.setAddress(AddressDTOMapper.mapFromAddressToAddressDTO(event.getAddress()));
 		eventDTO.setDescription(event.getDescription());
 		eventDTO.setId(event.getId());
 		eventDTO.setName(event.getName());
 		eventDTO.setOwner(UserDTOMapper.mapUserDetailsToUserDTO(event.getOwner()));
 		eventDTO.setTime(event.getTime());
 		return eventDTO;
 	}
 	
 	public static List<AcceptListDTO> mapMaillingListToAcceptListDTO(List<EventMailingList> eventMailingList){
 		List<AcceptListDTO> acceptListDTOs = new ArrayList<AcceptListDTO>();
 		
 		if(eventMailingList == null)
 			return acceptListDTOs;
 		
 		for(EventMailingList mailingList : eventMailingList){
			AcceptListDTO acceptListDTO = new AcceptListDTO();
			acceptListDTO.setEmail(mailingList.getEmail());
			acceptListDTO.setStatus(mailingList.getStatus());
			if(mailingList.getUserDetailsEntity() != null){
				acceptListDTO.setName(mailingList.getUserDetailsEntity().getName());
				acceptListDTO.setLastOnlineDateTime(mailingList.getUserDetailsEntity().getAudit().getUpdatedOn().toString());
 			}
			acceptListDTOs.add(acceptListDTO);
 		}
 		
 		return acceptListDTOs;
 	}
 	
 }
