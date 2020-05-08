 package org.bonitasoft.demo.callforpaper.db;
 
 import java.util.Date;
 
 import javax.annotation.PostConstruct;
 import javax.ejb.Singleton;
 import javax.ejb.Startup;
 import javax.inject.Inject;
 
 import org.bonitasoft.demo.callforpaper.model.Paper;
 import org.bonitasoft.demo.callforpaper.model.PaperState;
 import org.bonitasoft.demo.callforpaper.model.Vote;
 import org.bonitasoft.demo.callforpaper.service.PaperService;
 import org.bonitasoft.demo.callforpaper.service.VoteService;
 
 @Singleton
 @Startup
 public class DatabasePopulator {
 
 	private Paper aPaper;
 	private Paper anotherPaper;
 
 	@Inject
 	private PaperService paperService;
 	@Inject
 	private VoteService voteService;
 
 	@PostConstruct
 	private void populateDB() {
 		// A Paper
 		aPaper = new Paper();
 		aPaper.setCreationDate(new Date());
 		aPaper.setSubmitterEmail("sebastien.prunier(at)gmail(dot)com");
 		aPaper.setSessionType("Quickie");
 		aPaper.setSessionTitle("How Javascript will save the world !");
 		aPaper.setSessionSummary("15 minutes to explain how Javascript will save the world.");
 		aPaper.setSpeakers("Sebastien Prunier");
 		aPaper.setSpeakersBios("Software engineer @MosicaFR. Follow @sebprunier on Twitter !");
 
 		// Another Paper
 		anotherPaper = new Paper();
 		anotherPaper.setCreationDate(new Date());
 		anotherPaper
 				.setSubmitterEmail("frederic.bouquet(at)bonitasoft(dot)com");
 		anotherPaper.setSessionType("Conference");
 		anotherPaper.setSessionTitle("Add BPM to your Business Applications");
 		anotherPaper
 				.setSessionSummary("Demo-oriented conference to show the power of Bonita Open Solution !");
 		anotherPaper.setSpeakers("Frederic Bouquet");
 		anotherPaper
 				.setSpeakersBios("Bonita evangelist. Follow @bouquetf on Twitter !");
 
 		// Save papers !
		paperService.createPaperWithoutStartingProcess(aPaper);
		paperService.createPaperWithoutStartingProcess(anotherPaper);
 
 		// Add votes
 		Vote v1 = new Vote();
 		v1.setJuryMember("jury1");
 		v1.setVote(false);
 		v1.setPaper(aPaper);
 		Vote v2 = new Vote();
 		v2.setJuryMember("jury2");
 		v2.setVote(false);
 		v2.setPaper(aPaper);
 		Vote v3 = new Vote();
 		v3.setJuryMember("jury3");
 		v3.setVote(false);
 		v3.setPaper(aPaper);
 		Vote v4 = new Vote();
 		v4.setJuryMember("jury1");
 		v4.setVote(true);
 		v4.setPaper(anotherPaper);
 		Vote v5 = new Vote();
 		v5.setJuryMember("jury2");
 		v5.setVote(false);
 		v5.setPaper(anotherPaper);
 		Vote v6 = new Vote();
 		v6.setJuryMember("jury3");
 		v6.setVote(true);
 		v6.setPaper(anotherPaper);
 		voteService.createVote(v1);
 		voteService.createVote(v2);
 		voteService.createVote(v3);
 		voteService.createVote(v4);
 		voteService.createVote(v5);
 		voteService.createVote(v6);
 
 		// Update Papers states
 		aPaper.setState(PaperState.REFUSED);
 		anotherPaper.setState(PaperState.ACCEPTED);
 		paperService.updatePaper(aPaper);
 		paperService.updatePaper(anotherPaper);
 	}
 }
