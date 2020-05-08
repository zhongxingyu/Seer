 package nl.kennisnet.arena.setup;
 
 import java.util.Map;
 import java.util.Map.Entry;
 
 import nl.kennisnet.arena.services.ParticipantService;
 
 import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
 
@Service
 public class Bootstrap {
 	private final Logger log = Logger.getLogger(Bootstrap.class);
 
	@Autowired
	private ParticipantService participantService;
 	private Map<String, String> teams;
 
 
 	public void bootstrap() {
 		log.info("==> bootstrapping");
 		setupTeams();
 	}
 
 	private void setupTeams() {
 		log.info("==> setup up teams");
 		for (Entry<String, String> entry : teams.entrySet()) {
 			String name = entry.getKey();
 			String color = entry.getValue();
 			log.info("==> creating team: " + name + " with color 0x" + color);
 			participantService.createParticipantIfNotPresent(name, entry.getValue());
 		}
 	}
 
 	public void setTeams(Map<String, String> teams) {
 		this.teams = teams;
 	}
 }
