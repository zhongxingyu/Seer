 package nl.waisda.services;
 
 import java.util.Date;
 import java.util.List;
 
 import nl.waisda.domain.Game;
 import nl.waisda.domain.Mission;
 import nl.waisda.domain.MissionGame;
 import nl.waisda.domain.MissionParticipant;
 import nl.waisda.domain.Participant;
 import nl.waisda.domain.User;
 import nl.waisda.domain.Video;
 import nl.waisda.exceptions.NotFoundException;
 import nl.waisda.repositories.MissionGameRepository;
 import nl.waisda.repositories.MissionParticipantRepository;
 import nl.waisda.repositories.MissionRepository;
 import nl.waisda.repositories.MissionVideoRepository;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 @Service
 public class MissionService {
 
     private final Logger log = Logger.getLogger(MissionService.class);
 
     @Autowired
     private MissionRepository missionRepo;
 
     @Autowired
     private MissionVideoRepository missionVideoRepo;
 
     @Autowired
     private GameService gameService;
 
     @Autowired
     private MissionParticipantRepository missionParticipantRepo;
 
     @Autowired
     private MissionGameRepository missionGameRepo;
 
     public Mission getMissionById(final int missionId) {
         return missionRepo.getById(missionId);
     }
 
     public List<Video> getVideosForMission(final int missionId) {
         return missionVideoRepo.getVideosForMission(missionId);
     }
 
    public void joinMission(final Mission mission, final User user, final Video video) throws NotFoundException {
         final Game game = gameService.createGame(user, video);
 
         final MissionParticipant missionParticipant = new MissionParticipant();
         final MissionGame missionGame = new MissionGame();
         final Participant participant = new Participant();
 
         participant.setGame(game);
         participant.setUser(user);
         participant.setJoinedOn(new Date());
 
         missionGame.setGame(game);
         missionGame.setMission(mission);
 
         missionParticipant.setMission(mission);
         missionParticipant.setParticipant(participant);
 
         missionGameRepo.store(missionGame);
         missionParticipantRepo.store(missionParticipant);
     }
     
     public List<Mission> getMissions() {
     	return missionRepo.getMissions();    	
     }
 }
