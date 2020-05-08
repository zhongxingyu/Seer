 package com.prodyna.pac.conference.service.bean;
 
 import java.util.List;
 import java.util.logging.Logger;
 
 import javax.ejb.Local;
 import javax.ejb.Stateless;
 import javax.enterprise.event.Event;
 import javax.inject.Inject;
 import javax.persistence.EntityManager;
 import javax.persistence.TypedQuery;
 
 import com.prodyna.pac.conference.interceptor.Logged;
 import com.prodyna.pac.conference.interceptor.Performance;
 import com.prodyna.pac.conference.model.Speaker;
 import com.prodyna.pac.conference.model.Talk;
 import com.prodyna.pac.conference.model.TalkSpeaker;
 import com.prodyna.pac.conference.service.TalkService;
 
 /**
  * @author Martin Schwietzke, PRODYNA AG
  * 
  */
 @Stateless
 @Local(TalkService.class)
 @Logged
 @Performance
 public class TalkServiceBean implements TalkService {
 
 	@Inject
 	private Logger log;
 
 	@Inject
 	private EntityManager em;
 
 	@Inject
 	private Event<Talk> roomEventSrc;
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.prodyna.pac.conference.service.TalkService#createTalk(com.prodyna
 	 * .pac.conference.model.Talk)
 	 */
 	@Override
 	public void createTalk(Talk talk) throws Exception
 	{
 
 		log.info("Creating Talk [" + talk.getName() + "]");
 		em.persist(talk);
 		roomEventSrc.fire(talk);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.prodyna.pac.conference.service.TalkService#deleteTalk(com.prodyna
 	 * .pac.conference.model.Talk)
 	 */
 	@Override
 	public void deleteTalk(Talk talk) throws Exception
 	{
 
 		log.info("Deleting Talk [" + talk.getName() + "]");
 		em.remove(talk);
 		roomEventSrc.fire(talk);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.prodyna.pac.conference.service.TalkService#updateTalk(com.prodyna
 	 * .pac.conference.model.Talk)
 	 */
 	@Override
 	public void updateTalk(Talk talk) throws Exception
 	{
 
 		log.info("Updating Talk [" + talk.getName() + "]");
 		em.merge(talk);
 		roomEventSrc.fire(talk);
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.prodyna.pac.conference.service.TalkService#getTalkById(long)
 	 */
 	@Override
 	public Talk getTalkById(long talkId) throws Exception
 	{
 
 		return em.find(Talk.class, talkId);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.prodyna.pac.conference.service.TalkService#getAllTalks()
 	 */
 	@Override
 	public List<Talk> getAllTalks() throws Exception
 	{
 
 		return em.createNamedQuery(Talk.FIND_ALL, Talk.class).getResultList();
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.prodyna.pac.conference.service.TalkService#getTalksByConference(long)
 	 */
 	@Override
 	public List<Talk> getTalksByConference(long conferenceId) throws Exception
 	{
 
 		TypedQuery<Talk> q = em.createNamedQuery(Talk.FIND_BY_CONFERENCE_ID,
 				Talk.class);
 		q.setParameter("conferenceId", conferenceId);
 
 		return q.getResultList();
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.prodyna.pac.conference.service.TalkService#getTalksByRoom(long)
 	 */
 	@Override
 	public List<Talk> getTalksByRoom(long roomId) throws Exception
 	{
 
 		TypedQuery<Talk> q = em.createNamedQuery(Talk.FIND_BY_ROOM_ID,
 				Talk.class);
 		q.setParameter("roomId", roomId);
 
 		return q.getResultList();
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.prodyna.pac.conference.service.TalkService#getTalksBySpeaker(com.
 	 * prodyna.pac.conference.model.Speaker)
 	 */
 	@Override
 	public List<Talk> getTalksBySpeaker(Speaker speaker) throws Exception
 	{
 
 		TypedQuery<Talk> q = em.createNamedQuery(
 				TalkSpeaker.FIND_TALK_BY_SPEAKER_ID, Talk.class);
		q.setParameter("speakerId", speaker.getId());
 
 		return q.getResultList();
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.prodyna.pac.conference.service.TalkService#getSpeakersByTalk(long)
 	 */
 	@Override
 	public List<Speaker> getSpeakersByTalk(long talk) throws Exception
 	{
 
 		TypedQuery<Speaker> q = em.createNamedQuery(
 				Talk.FIND_SPEAKERS_BY_TALK_ID, Speaker.class);
 		q.setParameter("talkId", talk);
 
 		return q.getResultList();
 
 	}
 }
