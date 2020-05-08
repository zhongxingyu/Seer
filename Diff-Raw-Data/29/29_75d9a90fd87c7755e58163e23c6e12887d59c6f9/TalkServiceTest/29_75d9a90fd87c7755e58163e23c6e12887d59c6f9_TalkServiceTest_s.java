 package com.prodyna.pac.conference.service.test;
 
 import java.util.Date;
 import java.util.List;
 
 import javax.inject.Inject;
 
 import org.jboss.arquillian.container.test.api.Deployment;
 import org.jboss.arquillian.junit.Arquillian;
 import org.jboss.arquillian.junit.InSequence;
 import org.jboss.shrinkwrap.api.Archive;
 import org.jboss.shrinkwrap.api.ShrinkWrap;
 import org.jboss.shrinkwrap.api.spec.WebArchive;
 import org.junit.Assert;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.slf4j.Logger;
 
 import com.prodyna.pac.conference.client.api.ConferenceService;
 import com.prodyna.pac.conference.client.api.RoomService;
 import com.prodyna.pac.conference.client.api.TalkService;
 import com.prodyna.pac.conference.client.model.Conference;
 import com.prodyna.pac.conference.client.model.Room;
 import com.prodyna.pac.conference.client.model.Talk;
 
 @RunWith(Arquillian.class)
 public class TalkServiceTest {
 
 	@Inject
 	Logger logger;
 
 	@Inject
 	private RoomService roomService;
 
 	@Inject
 	private ConferenceService conferenceService;
 
 	@Inject
 	private TalkService talkService;
 	
 	@Inject
 	private Date dateInFuture;
 	
 	@Deployment
 	public static Archive<?> createTestArchive() {
 
 		WebArchive war = ShrinkWrap.create(WebArchive.class,
 				"TalkrServiceTest.war");
 		war.addPackages(true, "com.prodyna.pac.conference");
 		war.addAsResource("META-INF/test-beans.xml", "META-INF/beans.xml");
 		war.addAsResource("META-INF/test-persistence.xml",
 				"META-INF/persistence.xml");
 		return war;
 	}
 
 	@Test
 	@InSequence(1)
 	public void createTalkTest() throws Exception {
 		Talk talk = new Talk();
 		talk.setDescription("Description");
 		talk.setDuration(60);
 		talk.setName("Talk");
 		talk.setStart(dateInFuture);
 
 		talkService.createTalk(talk);
 
 		Assert.assertTrue(talk.getId() > 0);
 	}
 
 	@Test
 	@InSequence(2)
 	public void updateTalkTest() throws Exception {
 		Talk talk = new Talk();
 		talk.setDescription("Description");
 		talk.setDuration(60);
 		talk.setName("Talk");
 		talk.setStart(dateInFuture);
 
 		talkService.createTalk(talk);
 
 		Assert.assertTrue(talk.getId() > 0);
 
 		talk.setName("Updated Talk");
 
 		Talk updatedTalk = talkService.updateTalk(talk);
 
 		Assert.assertEquals("Updated Talk", updatedTalk.getName());
 	}
 
 	@Test
 	@InSequence(3)
 	public void deleteTalkTest() throws Exception {
 		Talk talk = new Talk();
 		talk.setDescription("Description");
 		talk.setDuration(60);
 		talk.setName("Talk");
 		talk.setStart(dateInFuture);
 
 		talkService.createTalk(talk);
 
 		Assert.assertTrue(talk.getId() > 0);
 
 		talkService.deleteTalk(talk);
 
 		Talk foundTalk = talkService.findTalkById(talk.getId());
 
 		Assert.assertNull(foundTalk);
 
 	}
 
 	@Test
 	@InSequence(4)
 	public void findTalkByIdTest() throws Exception {
 		Talk talk = new Talk();
 		talk.setDescription("Description");
 		talk.setDuration(60);
 		talk.setName("Talk");
 		talk.setStart(dateInFuture);
 
 		talkService.createTalk(talk);
 
 		Assert.assertTrue(talk.getId() > 0);
 
 		Talk foundTalk = talkService.findTalkById(talk.getId());
 
 		Assert.assertEquals(talk.getId(), foundTalk.getId());
 	}
 
 	@Test
 	@InSequence(5)
 	public void findTalksByNameTest() throws Exception {
 		Talk t1 = new Talk();
 		t1.setDescription("Description");
 		t1.setDuration(60);
 		t1.setName("Talk");
 		t1.setStart(dateInFuture);
 
 		talkService.createTalk(t1);
 
 		Talk t2 = new Talk();
 		t2.setDescription("Description");
 		t2.setDuration(60);
 		t2.setName("Talk");
 		t2.setStart(dateInFuture);
 
 		talkService.createTalk(t2);
 
 		List<Talk> foundTalks = talkService.findTalksByName("Talk");
 
 		Assert.assertTrue(foundTalks.size() > 0);
 
 		Assert.assertEquals("Talk", foundTalks.get(0).getName());
 	}
 
 	@Test
 	@InSequence(6)
 	public void findTalksByConferenceIdTest() throws Exception {
 		Conference conference = new Conference();
 		conference.setDescription("Desription");
 		conference.setName("Conference");
 		conference.setStart(dateInFuture);
 		conference.setEnd(dateInFuture);
 		
 		conferenceService.createConference(conference);
 		
 		Talk talk = new Talk();
 		talk.setDescription("Description");
 		talk.setDuration(60);
 		talk.setName("Talk");
 		talk.setConference(conference);
 		talk.setStart(dateInFuture);
 		
 		talkService.createTalk(talk);
 		
 		List<Talk> foundTalks = talkService.findTalksByConferenceId(conference.getId());
 		
 		Assert.assertTrue(foundTalks.size() > 0 );
 		
 		Assert.assertEquals(talk.getId(), foundTalks.get(0).getId());
 	}
 
 	@Test
 	@InSequence(7)
 	public void findTalksByRoomIdTest() throws Exception {
 		Room room = new Room();
 		room.setCapacity(10);
 		room.setName("Room");
 
 		
 		roomService.createRoom(room);
 		
 		
 		Talk talk = new Talk();
 		talk.setDescription("Description");
 		talk.setDuration(60);
 		talk.setName("Talk");
 		talk.setRoom(room);
 		talk.setStart(dateInFuture);
 		
 		talkService.createTalk(talk);
 		
 		List<Talk> foundTalks = talkService.findTalksByRoomId(room.getId());
 		
 		Assert.assertTrue(foundTalks.size() > 0 );
 		
 		Assert.assertEquals(talk.getId(), foundTalks.get(0).getId());
 	}
 
 	@Test
 	@InSequence(8)
 	public void findAllTest() throws Exception {
 		Talk t1 = new Talk();
 		t1.setDescription("Description");
 		t1.setDuration(60);
 		t1.setName("Talk");
 		t1.setStart(dateInFuture);
 		talkService.createTalk(t1);
 
 		Talk t2 = new Talk();
 		t2.setDescription("Description");
 		t2.setDuration(60);
 		t2.setName("Talk");
 		t2.setStart(dateInFuture);
 
 		talkService.createTalk(t2);
 
 		List<Talk> foundTalks = talkService.findAll();
 
 		Assert.assertTrue(foundTalks.size() > 0);
 
 	}
 
 }
