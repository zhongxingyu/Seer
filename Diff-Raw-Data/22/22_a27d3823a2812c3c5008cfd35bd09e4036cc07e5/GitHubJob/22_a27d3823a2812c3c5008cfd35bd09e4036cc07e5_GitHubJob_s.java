 package org.tothought.spring.jobs;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.scheduling.annotation.Scheduled;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 import org.tothought.entities.Commit;
 import org.tothought.entities.DataLoadLogEntry;
 import org.tothought.entities.Skill;
 import org.tothought.github.GitHubService;
 import org.tothought.repositories.CommitRepository;
 import org.tothought.repositories.DataLoadLogEntryRepository;
 import org.tothought.repositories.RecentDataLoadLogEntryRepository;
 import org.tothought.repositories.SkillRepository;
 import org.tothought.views.RecentDataLoadLogEntry;
 
 @Service
 @Transactional
 public class GitHubJob {
 
 	private static final String JOB_NAME = "GITHUB";
 
 	@Value("${github.username}")
 	private String username;
 	
 	@Value("${github.password}")
 	private String password;
 	
 	private SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
 
 	Logger logger = LoggerFactory.getLogger(GitHubJob.class);
 
 	@Autowired
 	DataLoadLogEntryRepository dataLoadLogEntryRepository;
 
 	@Autowired
 	SkillRepository skillRepository;
 
 	@Autowired
 	RecentDataLoadLogEntryRepository recentDataLoadLogEntryRepository;
 
 	@Autowired
 	CommitRepository repository;
 
	@Scheduled(fixedDelay = 600000)
 	public void perform() {
 		try {
 			logger.info("Starting GitHub Data Load..");
 			Date mostRecentCommitDate = null;
 			Date dataCurrentDate = this.getDataCurrentDate();
 
 			logger.info((dataCurrentDate != null) ? "GitHub data load is processing commits made after "
 					+ dateFormat.format(dataCurrentDate) : "GitHub data load is processing all commits");
 
 			GitHubService service = new GitHubService(this.username, this.password);
 
 			// Commits to save
 			List<Commit> unsavedCommits = new ArrayList<Commit>();
 
 			for (Skill skill : this.skillRepository.findAll()) {
 				List<Commit> commits = service.getCommitsByTag(skill.getTag());
 
 				logger.info("GitHub data load is processing " + commits.size() + " commits for skill: " + skill.getTag().getName() + ".");
 
 				for (Commit commit : commits) {
 					Date commitDt = commit.getCommitDt();
 					if ((dataCurrentDate == null || commitDt.after(dataCurrentDate) && !unsavedCommits.contains(commit))) {
 						unsavedCommits.add(commit);
 
 						if (mostRecentCommitDate == null || commitDt.after(mostRecentCommitDate)) {
 							mostRecentCommitDate = commitDt;
 						}
 					}
 				}
 			}
 
 			int unsavedCommitsCount = unsavedCommits.size();
 			if (unsavedCommitsCount > 0) {
 				
 				logger.info("GitHub data load is saving " + unsavedCommitsCount + " commits.");
 				this.log(unsavedCommits);
 				repository.save(unsavedCommits);
 				dataLoadLogEntryRepository.save(new DataLoadLogEntry(JOB_NAME, unsavedCommitsCount,
 						mostRecentCommitDate));
 			}
 			
 			logger.info("GitHub data load has completed.");
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 			logger.error("Error performing GitHub data load.");
 		}
 	}
 
 	private void log(List<Commit> unsavedCommits) {
 		int x = 0;
 		for(Commit commit: unsavedCommits){
			System.out.println("--- Commit ---" + ++x);
			System.out.println(commit.getApiUrl());
			System.out.println(commit.getHtmlUrl());
			System.out.println(commit.getMessage());
			System.out.println(commit.getSha());
			System.out.println(commit.getTitle());
			System.out.println(commit.getCommitDt());
			System.out.println(commit.getTags().size());
			System.out.println("-- End Commit --");
 		}
 		
 	}
 
 	private Date getDataCurrentDate() {
 		RecentDataLoadLogEntry logEntry = recentDataLoadLogEntryRepository.findByLoadName(JOB_NAME);
 		
 		if (logEntry != null) {
 			Calendar calendar = Calendar.getInstance();
 			//Add 1 day to date to prevent processing the same records, depending on run/commit time records could be lost.
 			calendar.setTime(logEntry.getDataCurrentDt());
 			calendar.add(Calendar.DATE, 1);
 			return calendar.getTime();
 		}
 		
 		return null;
 	}
 }
