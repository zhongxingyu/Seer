 package nl.waisda.controllers;
 
 import java.util.List;
 
 import javax.servlet.http.HttpSession;
 
 import nl.waisda.domain.Mission;
 import nl.waisda.domain.MissionState;
 import nl.waisda.domain.User;
 import nl.waisda.domain.Video;
 import nl.waisda.exceptions.NotFoundException;
 import nl.waisda.services.MissionService;
 import nl.waisda.services.UserSessionService;
 import nl.waisda.services.VideoService;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.SessionAttributes;
 
 @Controller
 @SessionAttributes("userSession")
 public class MissionController {
 
     private final Logger log = Logger.getLogger(MissionController.class);
 
     @Autowired
     private MissionService missionService;
 
     @Autowired
     private VideoService videoService;
 
     @Autowired
     private UserSessionService userSessionService;
 
     @RequestMapping("/mission/{missionId}")
     public String mision(@PathVariable final int missionId, final ModelMap model, final HttpSession session)
             throws NotFoundException {
         final Mission mission = missionService.getMissionById(missionId);
         final List<Video> videos = missionService.getVideosForMission(missionId);
 
         if (mission == null) {
             throw new NotFoundException("Unknown mission " + missionId);
         } else if (mission.getMissionState().equals(MissionState.ENDED)) {
             log.info(String.format("Redirecting request for old mission %d to /", missionId));
             return "redirect:/";
         }
 
         model.addAttribute("mission", mission);
         model.addAttribute("videos", videos);
 
         model.addAttribute("cssClass", "game");
 
         return "mission";
     }
 
     @RequestMapping("/mission/join/{missionId}/{videoId}")
     public String joinMission(@PathVariable final int missionId, @PathVariable final int videoId, final ModelMap model, final HttpSession session) throws NotFoundException {
         final Mission mission = missionService.getMissionById(missionId);
         final Video video = videoService.getVideoById(videoId);
         final User user = userSessionService.requireCurrentUser(session);
        missionService.joinMission(mission, user, video);
        return "mission";
     }
 
     public void setMissionService(final MissionService missionService) {
         this.missionService = missionService;
     }
 
     public void setVideoService(final VideoService videoService) {
         this.videoService = videoService;
     }
 
 }
