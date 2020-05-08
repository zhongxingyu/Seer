 package eu.margiel.repositories;
 
 import static org.fest.assertions.Assertions.*;
 import static org.junit.Assert.*;
 
 import java.util.List;
 
import org.junit.Ignore;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.transaction.annotation.Transactional;
 
 import eu.margiel.domain.Presentation;
 import eu.margiel.domain.Speaker;
 import eu.margiel.domain.User;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration("/test-spring.xml")
 @Transactional
 public class SpeakerRepositoryShould {
 	@Autowired
 	private SpeakerRepository repository;
 
 	@Test
 	public void findSpeakerByMailCaseInsensitive() {
 		String mail = "jan.kowalski@domena.pl";
 		Speaker expectedSpeaker = new Speaker().mail(mail);
 		repository.saveAndFlush(expectedSpeaker);
 		repository.saveAndFlush(new Speaker().mail("michal.nowak@domena.pl"));
 
 		User foundSpeaker = repository.readByMail(mail.toUpperCase());
 
 		assertEquals(expectedSpeaker, foundSpeaker);
 	}
 
 	@Test
	@Ignore
 	public void findOnlySpeakersWithAttLeastOneAcceptedPresentations() {
 		Speaker speakerA = createSpeaker("a")
 				.addPresentation(new Presentation().setTitle("a").toggleAccepted());
 		Speaker speakerB = createSpeaker("b")
 				.addPresentation(new Presentation().setTitle("b1").toggleAccepted())
 				.addPresentation(new Presentation().setTitle("b2").toggleAccepted());
 		Speaker speakerC = createSpeaker("c")
 				.addPresentation(new Presentation().setTitle("c"));
 		repository.saveAndFlush(speakerA);
 		repository.saveAndFlush(speakerB);
 		repository.saveAndFlush(speakerC);
 
 		List<Speaker> speakers = repository.readWithAcceptedPresentations();
 
 		assertThat(speakers)
 				.hasSize(2)
 				.onProperty("firstName").containsOnly("a", "b");
 	}
 
 	private Speaker createSpeaker(String firstName) {
 		return new Speaker().firstName(firstName);
 	}
 }
