 package lt.nfq.conference.service;
 
 import java.util.Date;
 import java.util.List;
 
 import lt.nfq.conference.domain.Conference;
 import lt.nfq.conference.domain.ConferenceType;
 import lt.nfq.conference.service.dao.ConferenceMapper;
 import lt.nfq.conference.service.dao.ConferenceTypeMapper;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 @Service
 public class ConferenceService {
 	
    @Autowired
     private ConferenceMapper conferenceMapper;
     
     @Autowired
     private ConferenceTypeMapper conferenceTypeMapper;
 
     /**
      * Get conferences by date interval
      * 
      * @param start Date
      * @param end Date
      * @return List<Conference>
      */
     public List<Conference> getConferencesByDates(Date start, Date end) {
        return conferenceMapper.getConferencesByDates(start, end);
     }
     
     /**
      * Get all available conference types
      * 
      * @return List<ConferenceType>
      */
     public List<ConferenceType> getConferenceTypes() {
     	return conferenceTypeMapper.getConferenceTypes();
     }
 
     /**
      * Get one conference by id
      * 
      * @param id Integer
      * @return Conference
      */
     public Conference getConference(int id) {
         return conferenceMapper.getConference(id);
     }
 
     /**
      * Update existing conference.
      * If update successful returns true. Otherwise false.
      * 
      * @param conference Conference
      * @return boolean
      */
     public boolean updateConference(Conference conference) {
         return conferenceMapper.updateConference(conference) > 0;
     }
 
     /**
      * If passed conference object has conferenceId value - executes update process.
      * Otherwise insert new conference record.
      * Returns new created or updated record id.
      * 
      * @param conference Conference
      * @return Integer
      */
     public int saveConference(Conference conference) {
     	int result = 0;
     	if (conference.getConferenceId() != null) {
    		result = conferenceMapper.updateConference(conference);
     	} else {
     		result = conferenceMapper.insertConference(conference);
     	}
     	
     	return result;
     }
     
     /**
      * Delete conference
      * 
      * @param conferenceId
      * @return
      */
     public int deleteConference(Integer conferenceId) {
     	return conferenceMapper.deleteConference(conferenceId);
     }
 }
