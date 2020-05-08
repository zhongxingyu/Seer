 package generator;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import models.Commit;
 import models.CommitPattern;
 import models.Network;
 import models.Person;
 import models.STPattern;
 import models.STPattern.patternTypes;
 import models.UnorderedPair;
 import models.WeightCriteria;
 import db.Resources;
 import db.SocialAnalyzerDb;
 import db.TechnicalAnalyzerDb;
 
 /**
  * <code>Generator</code will go through each commit
  * and create relationships
  * @author braden
  *
  */
 public class Generator
 {
 	private SocialAnalyzerDb stcaDb;
 	private TechnicalAnalyzerDb techDb;
 	private WeightCriteria weightCriteria;
 
 	public Generator(SocialAnalyzerDb stcaDb, TechnicalAnalyzerDb db, float techlower, float techUpper, 
 								float socialLower, float socialUpper,float fuzzylower, float fuzzyUpper) {
 		this.stcaDb = stcaDb;
 		this.techDb = db;
 		this.weightCriteria = new WeightCriteria(techlower, techUpper, socialLower, socialUpper, fuzzylower, fuzzyUpper);
 	}
 	
 	/**
 	 * Iterates over all the {@link models.Commit} in a repo, and builds up a list of 
 	 * <code>Patterns</code> 
 	 */
 	public void generate() { 
 		int pagingOffset = 0;
 		List<Commit> commits;
 		int countCommits = techDb.getCommitCount();
 		int doneCommits = 1;
 		while (pagingOffset != -1)
 		{
 			commits = techDb.getCommits(Resources.DB_LIMIT, pagingOffset);
 			if (commits.size() < Resources.DB_LIMIT) pagingOffset = -1;
 			for (Commit currentCommit : commits)
 			{
 				Resources.log("Commit (%d/%d) : %s (%s)", doneCommits, countCommits, currentCommit.getCommit_id(), currentCommit.getCommit_date().toString());
 				
 				// Get all the related items and their threads.
 				Network commitNetwork = stcaDb.getNetwork(currentCommit.getCommit_id());
 				buildCommitPatterns(commitNetwork);
 				
 				// get pass/fail from technical db
 				commitNetwork.setPass(techDb.getCommitStatus(currentCommit.getCommit_id()));
 				
 				// Insert into graph tables.
 				stcaDb.insertNetwork(commitNetwork, weightCriteria);
 				doneCommits++;
 				if (doneCommits >= countCommits) return;
 			}
 			pagingOffset += Resources.DB_LIMIT;
 		}
 	}
 	
 	public void buildCommitPatterns(Network network)
 	{
 		STPattern newSTPattern = null;
 		CommitPattern technicalCommitPattern = techDb.getTechnicalNetworkForCommit(network.getCommitId());
 
 		for (Integer threadId : network.getThreadPersonMap().keySet())
 		{						
 			// for each thread, construct links between the people involved.
 			List<Person> personList = new LinkedList<Person>(network.getThreadPersonMap().get(threadId));
 			
 			for (int currentPersonPos = 0;currentPersonPos < personList.size()-1;currentPersonPos++)
 			{
 				List<Person> innerPersonsList = new LinkedList<Person>(personList);
 				
 				// create the connected set.
 				Person currentPerson = personList.get(currentPersonPos);
 				innerPersonsList.remove(0);
 				for (Person p : innerPersonsList)
 				{
 					newSTPattern = new STPattern();
 					newSTPattern.setPatternType(patternTypes.SOCIAL_ONLY);
 					newSTPattern.setPerson1Id(currentPerson.getEmail());
 					newSTPattern.setPerson2Id(p.getEmail());
					newSTPattern.setCommitId(network.getCommitId());
 					// Construct the key 
 					UnorderedPair<String, String> pair = new UnorderedPair<String, String>(newSTPattern.getPerson1Id(), newSTPattern.getPerson2Id());
 					
 					// combine our pattern into the technical one.
 					if (technicalCommitPattern.getStPatterns().containsKey(pair))
 					{
 						// its in our technical pattern already, combine the patterns
 						STPattern techPattern = technicalCommitPattern.getStPatterns().get(pair);
 						techPattern.setPatternType(patternTypes.SOCIAL_TECHNICAL);
 						technicalCommitPattern.getStPatterns().put(pair, techPattern);
 					}
 					else
 					{
 						//add a new pattern in
 						technicalCommitPattern.getStPatterns().put(pair, newSTPattern);
 					}
 				}
 			}
 		}
 		network.setNetworkCommitPattern(technicalCommitPattern);
 	}
 }
