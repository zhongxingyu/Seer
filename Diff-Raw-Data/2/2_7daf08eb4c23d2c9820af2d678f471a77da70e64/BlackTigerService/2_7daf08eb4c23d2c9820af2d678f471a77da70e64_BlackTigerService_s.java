 package dk.drb.blacktiger.service;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.*;
 import java.util.regex.Pattern;
 
 import javax.sql.DataSource;
 
 import dk.drb.blacktiger.model.CallInformation;
 import dk.drb.blacktiger.model.Participant;
 import dk.drb.blacktiger.model.User;
 import org.asteriskjava.live.AsteriskServer;
 import org.asteriskjava.live.MeetMeRoom;
 import org.asteriskjava.live.MeetMeUser;
 import org.asteriskjava.manager.ManagerEventListener;
 import org.asteriskjava.manager.event.ManagerEvent;
 import org.asteriskjava.manager.event.MeetMeJoinEvent;
 import org.asteriskjava.manager.event.MeetMeLeaveEvent;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.dao.EmptyResultDataAccessException;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.jdbc.core.RowMapper;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.GrantedAuthority;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.transaction.annotation.Transactional;
 
 /**
  * An implementation of the <code>IBlackTigerService</code> which communicates with an asterisk server and mysql databases.<br>
  * This implementation is customized to the setup already setup before this system was developed.<br>
  * It expects 2 mysql databases - The first one being an original asterisk database and the second one being a database with phonebook entries
 * and statistical information. The latter has traditionally been called astersiskcdrdb but that name is optional to this implementation.
  */
 public class BlackTigerService implements IBlackTigerService {
 
     private static final Logger LOG = LoggerFactory.getLogger(BlackTigerService.class);
     private static final Pattern PHONE_NUMBER_PATTERN = Pattern.compile("\\d{9,12}");
     private AsteriskServer asteriskServer;
     private JdbcTemplate asteriskJdbcTemplate;
     private JdbcTemplate callInfoJdbcTemplate;
     private UserMapper userMapper = new UserMapper();
     private PhoneBookEntryMapper phoneBookEntryMapper = new PhoneBookEntryMapper();
     private CallInformationMapper callInformationMapper = new CallInformationMapper();
     
     private List<BlackTigerEventListener> eventListeners = new ArrayList<BlackTigerEventListener>();
     private ManagerEventListener managerEventListener = new ManagerEventListener() {
         @Override
         public void onManagerEvent(ManagerEvent event) {
             if (event instanceof MeetMeJoinEvent) {
                 String roomNo = ((MeetMeJoinEvent) event).getMeetMe();
                 Integer index = ((MeetMeJoinEvent) event).getUserNum();
                 fireEvent(new ParticipantJoinEvent(roomNo, index.toString()));
             }
 
             if (event instanceof MeetMeLeaveEvent) {
                 String roomNo = ((MeetMeLeaveEvent) event).getMeetMe();
                 Integer index = ((MeetMeLeaveEvent) event).getUserNum();
                 fireEvent(new ParticipantLeaveEvent(roomNo, index.toString()));
             }
         }
     };
 
     private class UserMapper implements RowMapper {
 
         @Override
         public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
             return new User(rs.getString("id"), rs.getString("data"));
         }
     }
     
     private class PhoneBookEntryMapper implements RowMapper<String> {
 
         @Override
         public String mapRow(ResultSet rs, int rowNum) throws SQLException {
             return rs.getString("name");
         }
         
     }
     
     private class CallInformationMapper implements RowMapper<CallInformation> {
 
         @Override
         public CallInformation mapRow(ResultSet rs, int rowNum) throws SQLException {
             String phoneNumber = rs.getString("phoneNumber");
             String name = getPhonebookEntry(phoneNumber);
             phoneNumber = fromDbPhoneNumber(phoneNumber);
             
             return new CallInformation(phoneNumber, name, rs.getInt("numberOfCalls"), rs.getInt("totalDuration"), rs.getTimestamp("firstCallTimestamp"));
         }
         
     }
 
     private void fireEvent(ParticipantEvent event) {
         for (BlackTigerEventListener listener : eventListeners) {
             listener.onParticipantEvent(event);
         }
     }
 
     public void setAsteriskServer(AsteriskServer asteriskServer) {
         if (this.asteriskServer != null) {
             this.asteriskServer.getManagerConnection().removeEventListener(managerEventListener);
         }
         this.asteriskServer = asteriskServer;
         this.asteriskServer.getManagerConnection().addEventListener(managerEventListener);
     }
 
     public void setAsteriskDataSource(DataSource dataSource) {
         this.asteriskJdbcTemplate = new JdbcTemplate(dataSource);
     }
     
     public void setCallInfoDataSource(DataSource dataSource) {
         this.callInfoJdbcTemplate = new JdbcTemplate(dataSource);
     }
 
     @Override
     public User getUser(String username) {
         return (User) this.asteriskJdbcTemplate.queryForObject("select * from sip where id=? and keyword like 'secret'",
                 new Object[]{username}, userMapper);
     }
 
     @Override
     public List<Participant> listParticipants(String roomNo) {
         LOG.debug("Listing participants. [room={}]", roomNo);
         checkRoomAccess(roomNo);
         MeetMeRoom room = asteriskServer.getMeetMeRoom(roomNo);
         List<Participant> result = new ArrayList<Participant>();
 
         for (MeetMeUser mmu : room.getUsers()) {
             result.add(participantFromMeetMeUser(mmu));
         }
         return result;
     }
 
     @Override
     public Participant getParticipant(String roomNo, String participantId) {
         LOG.debug("Retrieving participant. [room={};participant={}]", roomNo, participantId);
         checkRoomAccess(roomNo);
         MeetMeUser mmu = getMeetMeUser(roomNo, participantId);
         return mmu == null ? null : participantFromMeetMeUser(mmu);
     }
 
     @Override
     public void kickParticipant(String roomNo, String participantId) {
         checkRoomAccess(roomNo);
         MeetMeUser mmu = getMeetMeUser(roomNo, participantId);
         if (mmu != null) {
             mmu.kick();
         }
     }
 
     @Override
     public void muteParticipant(String roomNo, String participantId) {
         checkRoomAccess(roomNo);
         MeetMeUser mmu = getMeetMeUser(roomNo, participantId);
         if (mmu != null) {
             mmu.mute();
         }
     }
 
     @Override
     public void unmuteParticipant(String roomNo, String participantId) {
         checkRoomAccess(roomNo);
         MeetMeUser mmu = getMeetMeUser(roomNo, participantId);
         if (mmu != null) {
             mmu.unmute();
         }
     }
 
     private MeetMeUser getMeetMeUser(String roomNo, String participantId) {
         MeetMeRoom room = asteriskServer.getMeetMeRoom(roomNo);
         Integer id = Integer.parseInt(participantId);
         for (MeetMeUser mmu : room.getUsers()) {
             if (mmu.getUserNumber().equals(id)) {
                 return mmu;
             }
         }
         return null;
     }
 
     private Participant participantFromMeetMeUser(MeetMeUser user) {
         Authentication auth = SecurityContextHolder.getContext().getAuthentication();
         String number = user.getChannel().getCallerId().getNumber();
         boolean host = false; 
         
         //'number' is of syntax 'IP-<username>'. We have to take it into consideration when matching the number.
         if(auth!=null && number.equalsIgnoreCase(auth.getName())) {
             host = true;
         }
         
         String phoneNumber = user.getChannel().getCallerId().getNumber();
         String name = user.getChannel().getCallerId().getName();
         
         if(isNumber(name)) {
             phoneNumber = name;
             
             //Ensure that phonenumber is international, eg. +4512341234
             if(!phoneNumber.startsWith("+")) {
                 phoneNumber = "+" + phoneNumber;
             }
             name = getPhonebookEntry(phoneNumber);
         }
         
         return new Participant(user.getUserNumber().toString(),
                 name,
                 phoneNumber,
                 user.isMuted(),
                 host,
                 user.getDateJoined());
     }
 
     @Override
     public List<CallInformation> getReport(Date start, Date end, int minimumDuration) {
         String sql = "SELECT count(*) as numberOfCalls,src as phoneNumber, min(calldate) as firstCallTimeStamp,dcontext,sum(duration) as totalDuration "
                     + "FROM cdr where calldate > ? and calldate < ?  group by src having totalDuration > ? and length(src) >=9";
         return this.callInfoJdbcTemplate.query(sql, new Object[]{start, end, minimumDuration}, callInformationMapper);
     }
 
     @Override
     public String getPhonebookEntry(String phoneNumber) {
         try {
             phoneNumber = toDbPhoneNumber(phoneNumber);
             return this.callInfoJdbcTemplate.queryForObject("select * from ConfNames where phonenumber=?",
                 new Object[]{phoneNumber}, phoneBookEntryMapper);
         
         } catch(EmptyResultDataAccessException ex) {
             return null;
         }
     }
 
     @Override
     @Transactional
     public void updatePhonebookEntry(String phoneNumber, String name) {
         phoneNumber = toDbPhoneNumber(phoneNumber);
         
         boolean newEntry = getPhonebookEntry(phoneNumber) == null;
         
         String sql;
         if(newEntry) {
             sql = "insert into ConfNames (name, phonenumber) values (?,?)";
         } else {
             sql = "update ConfNames set name=? where phonenumber = ?";
         }
         
         this.callInfoJdbcTemplate.update(sql, new Object[]{name, phoneNumber});
     }
 
     @Override
     public void removePhonebookEntry(String phoneNumber) {
         String sql = "delete from ConfNames where phonenumber = ?";
         this.callInfoJdbcTemplate.update(sql, new Object[]{phoneNumber});
     }
     
     @Override
     public void addEventListener(BlackTigerEventListener listener) {
         if (listener != null) {
             eventListeners.add(listener);
         }
     }
 
     @Override
     public void removeEventListener(BlackTigerEventListener listener) {
         if (listener != null) {
             eventListeners.remove(listener);
         }
     }
     
     /**
      * Checks whether current user has access to a given room number.
      */
     private void checkRoomAccess(String roomNo) {
         if(!hasRole("ROOMACCESS_" + roomNo)) {
             throw new SecurityException("Not authorized to access room " + roomNo);
         }
     }
     
     /**
      * Checks whether current user hols a specific role.
      */
     private boolean hasRole(String role) {
         LOG.debug("Checking if current user has role '{}'", role);
         
         Authentication auth = SecurityContextHolder.getContext().getAuthentication();
         if(auth!=null && auth.isAuthenticated()) {
             for(GrantedAuthority ga : auth.getAuthorities()) {
                 if(ga.getAuthority().startsWith("ROLE_") && ga.getAuthority().substring(5).equals(role)) {
                     return true;
                 }
             }
         }
         LOG.debug("User does not have role. [auth={}]", auth);
         return false;
     }
     
     /**
      * Checks whether the string is a valid phone number.
      * @param text
      * @return 
      */
     private boolean isNumber(String text) {
         return PHONE_NUMBER_PATTERN.matcher(text).matches();
     }
     
     /**
      * Converts a string to a phone number in the format expected in database.<br>
      * The format expected in database is international number without the plus sign fx. 4512345678 instead of +4512345678.
      */
     private String toDbPhoneNumber(String number) {
         if(number.startsWith("+")) {
             number = number.substring(1);
         }
         return number;
     }
     
     /**
      * Converts the phonenumber from database to a proper international format.
      * In the database the format is international number without the plus sign fx. 4512345678 instead of +4512345678.<br>
      * This method ensures that the phonenumber becomes normal international format.
      */
     private String fromDbPhoneNumber(String number) {
         if(!number.startsWith("+")) {
              number = "+" + number;   
         }
         return number;
             
     }
 }
