 package com.eventinvitation.domain.dto;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import com.eventinvitation.domain.Event;
 import com.eventinvitation.domain.EventMailingList;
 
 public class EventDTOMapper {
 
 	public static EventDTO mapEventToEventDTO(Event event){
 		if(event == null){
 			return null;
 		}
 		EventDTO eventDTO = new EventDTO();
 		eventDTO.setAcceptListDTOs(mapMaillingListToAcceptListDTO(event.getMaillingList(),null));
 		eventDTO.setAddress(AddressDTOMapper.mapFromAddressToAddressDTO(event.getAddress()));
 		eventDTO.setDescription(event.getDescription());
 		eventDTO.setId(event.getId());
 		eventDTO.setName(event.getName());
 		eventDTO.setOwner(UserDTOMapper.mapUserDetailsToUserDTO(event.getOwner()));
 		eventDTO.setTime(event.getTime());
 		return eventDTO;
 	}
 	
 	public static List<AcceptListDTO> mapMaillingListToAcceptListDTO(List<EventMailingList> eventMailingList,String onlineFlag){
 		List<AcceptListDTO> acceptListDTOs = new ArrayList<AcceptListDTO>();
 		Date currentDate = new Date();
 		
 		if(eventMailingList == null)
 			return acceptListDTOs;
 		
 		for(EventMailingList mailingList : eventMailingList){
 			AcceptListDTO acceptListDTO = new AcceptListDTO();
 			acceptListDTO.setEmail(mailingList.getEmail());
 			acceptListDTO.setStatus(mailingList.getStatus());
 			if(mailingList.getUserDetailsEntity() != null){
 				acceptListDTO.setName(mailingList.getUserDetailsEntity().getName());
 				acceptListDTO.setLastOnlineDateTime(mailingList.getUserDetailsEntity().getAudit().getUpdatedOn().toString());
 				if(onlineFlag != null){
					if(((currentDate.getTime() - mailingList.getUserDetailsEntity().getAudit().getUpdatedOn().getTime())/60) > Integer.parseInt(onlineFlag)){
 						acceptListDTO.setOnline(false);
 					}					
 					else{
 						acceptListDTO.setOnline(true);
 					}
 				}
 			}
 			acceptListDTOs.add(acceptListDTO);
 		}
 		
 		return acceptListDTOs;
 	}
 	
 }
