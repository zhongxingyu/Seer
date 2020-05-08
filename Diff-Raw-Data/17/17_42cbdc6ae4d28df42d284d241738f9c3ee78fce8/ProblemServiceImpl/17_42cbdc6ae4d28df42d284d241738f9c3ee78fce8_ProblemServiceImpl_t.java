 package com.crowdgame.service;
 
 import java.util.List;
 import java.util.Random;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 import com.crowdgame.model.Problem;
 import com.crowdgame.model.ProblemCollection;
 import com.crowdgame.util.RemoteCommunicationService;
 import com.google.common.collect.Lists;
 
 @Service
 public class ProblemServiceImpl implements ProblemService {
 
 	@Autowired
 	private ProblemCollectionService collectionService;
 
 	@Autowired
 	private RemoteCommunicationService remoteService;
 	
 	public Problem getProblem() {
 		ProblemCollection collection = collectionService.getCollection();
 		if (collection.getProblems().size() < 3) {
 			retrieveMoreProblems();
 		}
 		if (collection.getProblems().size() == 0) {
			System.out.println("Returning backup problem");
 			return getBackupProblem();
 		}
 		
 		Random random = new Random();
 		List<Problem> problems = Lists.newArrayList(collection.getProblems());
 		int index = random.nextInt(problems.size());
 		Problem problem = problems.get(index);
 		collectionService.removeProblemFromCollection(collection, problem);
 		return problem;
 	}
 	
 	private Problem getBackupProblem() {
 		ProblemCollection collection = collectionService.getBackupCollection();
 		Random random = new Random();
 		List<Problem> problems = Lists.newArrayList(collection.getProblems());
 		int index = random.nextInt(problems.size());
 		Problem problem = problems.get(index);
 		return problem;
 	}
 	
 	private void retrieveMoreProblems() {
 		remoteService.addTasksToProblemCollection();
 	}
 	
 }
