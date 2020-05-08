 package com.piotrnowicki.exam.simulator.control;
 
 import java.util.List;
 import java.util.NavigableMap;
 import java.util.TreeMap;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.annotation.PostConstruct;
 import javax.ejb.DependsOn;
 import javax.ejb.Singleton;
 import javax.ejb.Startup;
 import javax.enterprise.event.Observes;
 import javax.inject.Inject;
 import javax.persistence.EntityManager;
 
 import com.piotrnowicki.exam.simulator.entity.Question;
 
 @Singleton
 @Startup
 @DependsOn("initializer")
 public class Cache {
 
 	@Inject
 	EntityManager em;
 
 	@Inject
 	Logger log;
 
 	NavigableMap<String, Question> questions;
 
 	@PostConstruct
 	private void init() {
 		questions = new TreeMap<>();
 
 		populate();
 	}
 
 	private void populate() {
 		List<Question> questionsFromDB = em.createNamedQuery(Question.READ_ALL,
 				Question.class).getResultList();
 
 		for (Question question : questionsFromDB) {
 			questions.put(question.getNumber(), question);
 		}
 
 		log.info("Cache populated successfully.");
 	}
 
 	// TODO: READ Lock?
 	public NavigableMap<String, Question> getQuestions() {
 		return questions;
 	}
 
 	public void dataModified(@Observes DataModifiedEvent event) {
		log.log(Level.INFO, "Data modified. Event related with question: {}.",
 				event.getQuestion());
 
 		questions.clear();
 
 		// TODO: populate only part of the cache.
 		populate();
 	}
 }
