 package at.ac.tuwien.server.service.impl;
 
 import java.util.Date;
 import java.util.List;
 import java.util.Set;
 import java.util.TreeSet;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import at.ac.tuwien.server.Constants;
 import at.ac.tuwien.server.dao.interfaces.ILocationDao;
 import at.ac.tuwien.server.dao.interfaces.IMessageDao;
 import at.ac.tuwien.server.dao.interfaces.IRaceDao;
 import at.ac.tuwien.server.dao.interfaces.IRaceStatisticsDao;
 import at.ac.tuwien.server.domain.Location;
 import at.ac.tuwien.server.domain.Message;
 import at.ac.tuwien.server.domain.MessageType;
 import at.ac.tuwien.server.domain.Race;
 import at.ac.tuwien.server.domain.RaceStatistics;
 import at.ac.tuwien.server.domain.User;
 import at.ac.tuwien.server.service.interfaces.IRaceService;
 import at.ac.tuwien.server.service.interfaces.IUserService;
 import at.ac.tuwien.server.service.stats.StatisticsHelper;
 
 @Service("raceService")
 public class RaceService implements IRaceService {
 
 	private static final int NOIDSET = -1;
 	@Autowired
 	IRaceDao raceDao;
 	@Autowired
 	ILocationDao locationDao;
 	@Autowired
 	IUserService userService;
 	@Autowired
 	IMessageDao messageDao;
 	@Autowired
 	IRaceStatisticsDao raceStatisticsDao;
 	
 	@Override
 	@Transactional
 	public Double getAvgSpeedInRaces(User u) {
 		
 		Double sumDistance = raceDao.getDistanceInRaces(u);
 		Long sumTime = new Long(0);
 
 		for(Race r : u.getRaces()){
 			if(!r.getRaceName().equals(Constants.defaultRace)){
 				Location first = locationDao.getFirstLocation(r);
 				Location last = locationDao.getLastLocation(r);
 				sumTime += last.getTimestamp().getTime() - first.getTimestamp().getTime();
 			}
 		}
 		// km/h
		Double temp = new Double(sumTime)/new Double(1000*60*60);
		Double avgSpeed = new Double(sumDistance/temp);
 		return avgSpeed;
 	}
 
 	@Override
 	@Transactional
 	public Set<Race> retrieveAllRacesForUser(User u) {
 		return u.getRaces();
 	}
 
 	@Override
 	@Transactional
 	public Set<Race> retrieveAllJoinableRacesForUser(User u) {
 		List<Message> messages =  userService.retrieveAllMessagesForUser(u);
 		Set<Race> races = new TreeSet<Race>();
 		for(Message m : messages){
 			if(m.getMsgType().equals(MessageType.RACEINVITATION)){
 				races.add(this.getRaceById(Integer.parseInt(m.getMsgText())));
 			}
 		}
 		return races;
 	}
 
 	@Transactional
 	public Race getRaceById(Integer id) {
 		return raceDao.getRaceById(id);
 	}
 
 	@Override
 	@Transactional
 	public Integer sendRaceInvitation(User u, List<String> userids, String raceName) {
 		
 		Race race = raceDao.saveNewRace(raceName);
 		race.addParticipant(u);
 		if(userids != null){
 			for(String idString : userids){
 				Integer id = Integer.parseInt(idString);
 				User receiver =  userService.getUserById(id);
 				//TODO
 				race.addParticipant(receiver);
 
 				Message m = new Message();
 				m.setMsgType(MessageType.RACEINVITATION);
 				m.setSentDate(new Date());
 				m.setSender(u);
 				m.setReceiver(receiver);
 				m.setMsgText(""+race.getId());
 				messageDao.saveMsg(m);
 			}
 		}
 		//update
 		raceDao.saveRace(race);
 		return race.getId();
 	}
 	
 	@Override
 	@Transactional
 	public void setRaceHistoricalLocation(int id, Location loc) {
 		Race race;
 		race = this.getRaceById(new Integer(id));
 		loc.setRace(race);
 
 
 		
 		locationDao.saveLocation(loc);
 		race.addLocation(loc);
 		raceDao.saveRace(race);
 		
 		List<Location> raceLocations = raceDao.getRaceLocationsForUser(race, loc.getUser()); 
 		if(raceLocations.size() >= 2){
 			
 		double distance = StatisticsHelper.calculateDistanceBetweenListOfPoints(raceLocations);
 		double timeInMS = raceLocations.get(0).getTimestamp().getTime() - raceLocations.get(raceLocations.size()-1).getTimestamp().getTime();
 		double timeInHour = timeInMS / (1000*60*60);
 		double avgSpeed = distance/timeInHour;
 		
 		
 		RaceStatistics stat = raceStatisticsDao.retrieveRaceStatisticsForRaceAndUser(race, loc.getUser());
 		stat.setDistance(distance);
 		stat.setAvgSpeed(avgSpeed);
 		stat.setRace(race);
 		stat.setUser(loc.getUser());
 		raceStatisticsDao.saveRaceStats(stat);
 		} else {
 			RaceStatistics raceStat = new RaceStatistics();
 			raceStat.setRace(race);
 			raceStat.setUser(loc.getUser());
 			raceStat.setDistance(new Double(0));
 			raceStat.setAvgSpeed(new Double(0));
 			raceStatisticsDao.saveRaceStats(raceStat);
 		}
 				
 		
 	}
 
 	@Override
 	@Transactional
 	public void setRaceLocation(int id, Location loc) {
 		Race race;
 		race = this.getRaceById(new Integer(id));
 		loc.setRace(race);
 
 		//get last point to race and user
 		Location lastSavedLoc = raceDao.getLastLocationForRaceAndUser(race, loc.getUser());
 		
 		locationDao.saveLocation(loc);
 		race.addLocation(loc);
 		//TODO update race distance, avg speed
 //		Location firstSavedLoc = raceDao.getFirstLocationForRaceAndUser(race,loc.getUser());
 		//calculate distance between the points
 		double distance = StatisticsHelper.calculateDistanceBetweenPoints(lastSavedLoc, loc);
 		
 		//update score
 		loc.getUser().setScore(loc.getUser().getScore()+ ((int) distance * Constants.scoreForRaceingDistanceMultiplyer));
 		
 		//get raceStatistics -object to raceid and user IF ANY
 		RaceStatistics stat = raceStatisticsDao.retrieveRaceStatisticsForRaceAndUser(race, loc.getUser());
 		//calculate avg speed
 		if(stat == null){
 			//create new RaceStatistics
 			RaceStatistics raceStat = new RaceStatistics();
 			raceStat.setRace(race);
 			raceStat.setUser(loc.getUser());
 			raceStat.setDistance(new Double(0));
 			raceStat.setAvgSpeed(new Double(0));
 			raceStatisticsDao.saveRaceStats(raceStat);
 		}else{
 			if(lastSavedLoc == null){
 				//should not be possible to reach this part
 				stat.setDistance(distance);
 				stat.setAvgSpeed(new Double(0));
 				raceStatisticsDao.saveRaceStats(stat);
 			}else{
 				double distanceBefore = stat.getDistance();
 				double avgSpeedBefore = stat.getAvgSpeed();
 				double timeBefore = 0; 							//in hours
 				if(avgSpeedBefore != 0) {
 					timeBefore = distanceBefore/avgSpeedBefore;
 				}
 
 				stat.setDistance(stat.getDistance()+distance);
 				double temp = (new Date().getTime() - lastSavedLoc.getTimestamp().getTime());
 				double time = temp / (1000*60*60); //hours
 				Double avgSpeed = (distanceBefore+distance) / (timeBefore + time);
 				stat.setAvgSpeed(avgSpeed);
 				raceStatisticsDao.saveRaceStats(stat);
 			}
 		}
 		
 		raceDao.saveRace(race);
 		
 	}
 
 //	@Override
 //	@Transactional
 //	public void setRaceHistoricalLocation(int id, Location loc, Date date) {
 //		Race race;
 //		race = this.getRaceById(new Integer(id));
 //		loc.setRace(race);
 //
 //		//get last point to race and user
 //		Location lastSavedLoc = raceDao.getLastLocationForRaceAndUser(race, loc.getUser());
 //		
 //		locationDao.saveLocation(loc);
 //		race.addLocation(loc);
 //		//TODO update race distance, avg speed
 ////		Location firstSavedLoc = raceDao.getFirstLocationForRaceAndUser(race,loc.getUser());
 //		//calculate distance between the points
 //		double distance = StatisticsHelper.calculateDistanceBetweenPoints(lastSavedLoc, loc);
 //		//get raceStatistics -object to raceid and user IF ANY
 //		RaceStatistics stat = raceStatisticsDao.retrieveRaceStatisticsForRaceAndUser(race, loc.getUser());
 //		//calculate avg speed
 //		if(stat == null){
 //			//create new RaceStatistics
 //			RaceStatistics raceStat = new RaceStatistics();
 //			raceStat.setRace(race);
 //			raceStat.setUser(loc.getUser());
 //			raceStat.setDistance(new Double(0));
 //			raceStat.setAvgSpeed(new Double(0));
 //			raceStatisticsDao.saveRaceStats(raceStat);
 //		}else{
 //			if(lastSavedLoc == null){
 //				//should not be possible to reach this part
 //				stat.setDistance(distance);
 //				stat.setAvgSpeed(new Double(0));
 //				raceStatisticsDao.saveRaceStats(stat);
 //			}else{
 //				double distanceBefore = stat.getDistance();
 //				double avgSpeedBefore = stat.getAvgSpeed();
 //				double timeBefore = 0; 							//in hours
 //				if(avgSpeedBefore != 0) {
 //					timeBefore = distanceBefore/avgSpeedBefore;
 //				}
 //
 //				stat.setDistance(stat.getDistance()+distance);
 //				double temp = (date.getTime() - lastSavedLoc.getTimestamp().getTime());
 //				double time = temp / (1000*60*60); //hours
 //				Double avgSpeed = (distanceBefore+distance) / (timeBefore + time);
 //				stat.setAvgSpeed(avgSpeed);
 //				raceStatisticsDao.saveRaceStats(stat);
 //			}
 //		}
 //		
 //		raceDao.saveRace(race);
 //		
 //	}
 //	
 }
