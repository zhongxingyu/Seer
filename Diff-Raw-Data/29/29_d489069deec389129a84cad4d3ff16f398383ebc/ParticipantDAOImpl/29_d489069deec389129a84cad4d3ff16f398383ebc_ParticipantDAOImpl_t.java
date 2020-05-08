 /*
     Copyright (c) 2012-2015 Yoga Vidya Pranic Healing Foundation of Karnataka.
     All rights reserved. Patents pending.
  
     Responsible: byummadisingh
 */
 package com.yvphk.model.dao;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Repository;
 import org.hibernate.SessionFactory;
 import org.hibernate.Criteria;
 import org.hibernate.Session;
 import org.hibernate.FetchMode;
 import org.hibernate.criterion.Restrictions;
 import org.hibernate.criterion.Projection;
 import org.hibernate.criterion.Projections;
 import com.yvphk.model.form.*;
 import com.yvphk.common.Util;
 
 import java.util.List;
 import java.util.Date;
 import java.util.ArrayList;
 
 @Repository
 public class ParticipantDAOImpl implements ParticipantDAO
 {
     @Autowired
     private SessionFactory sessionFactory;
 
     public Participant addParticipant (RegisteredParticipant registeredParticipant)
     {
         Participant participant = registeredParticipant.getParticipant();
         Session session = sessionFactory.openSession();
 
         if (RegisteredParticipant.ActionRegister.equals(registeredParticipant.getAction())) {
             participant.setActive(true);
 
             String level = registeredParticipant.getParticipant().getLevel();
             if (Util.nullOrEmptyOrBlank(registeredParticipant.getParticipant().getLevel())) {
                 return null;
             }
             participant.setTimecreated(new Date());
             session.save(participant);
 
             List<ParticipantSeat> seats = registeredParticipant.getSeats();
             if (seats ==  null) {
                 seats = new ArrayList<ParticipantSeat>();
                 seats.add(new ParticipantSeat());
             }
             for (ParticipantSeat participantSeat : seats) {
                 if (participantSeat.getSeat() == null) {
                     Integer greatestSeat = getGreatestSeat(level);
                     Integer greatestSeatNo = new Integer(1);
                     if (greatestSeat != null) {
                         greatestSeatNo = greatestSeat.intValue() + 1;
                     }
                     participantSeat.setSeat(greatestSeatNo);
                 }
                 participantSeat.setLevel(level);
                 participantSeat.setParticipant(participant);
                 session.save(participantSeat);
             }
         }
         else if (RegisteredParticipant.ActionUpdate.equals(registeredParticipant.getAction())) {
             participant.setTimeupdated(new Date());
             session.update(participant);
         }
 
         for (Comment comment: registeredParticipant.getComments()) {
             comment.setParticipant(participant);
             comment.setTimecreated(new Date());
             if (!Util.nullOrEmptyOrBlank(comment.getComments())) {
                 session.save(comment);
             }
         }
         session.flush();
         session.close();
         return participant;
 
     }
 
     public void addComments (Participant participant, Comment comment)
     {
         
     }
 
     public Participant getParticipant (Integer participantId)
     {
         Session session = sessionFactory.openSession();
         Criteria criteria = session.createCriteria(Participant.class);
         criteria.setFetchMode("comments",FetchMode.EAGER);
         criteria.setFetchMode("seats", FetchMode.EAGER);
         criteria.add(Restrictions.eq("participantId", participantId));
         List results = criteria.list();
         Participant participant = null;
 
         if (results != null || !results.isEmpty()) {
             participant = (Participant) results.get(0);
         }
         session.close();
         return participant;
     }
 
     public List<Participant> listParticipants(ParticipantCriteria participantCriteria)
     {
         Session session = sessionFactory.openSession();
         Criteria criteria = session.createCriteria(Participant.class);
        criteria.setFetchMode("comments",FetchMode.JOIN);
        criteria.createAlias("seats", "seats");
 
         if (participantCriteria.getSeat() != null) {
             criteria.add(Restrictions.eq("seats.seat", participantCriteria.getSeat()));
         }
 
         if (!Util.nullOrEmptyOrBlank(participantCriteria.getName())) {
             criteria.add(Restrictions.like("name","%"+participantCriteria.getName()+"%"));
         }
 
         if (!Util.nullOrEmptyOrBlank(participantCriteria.getEmail())) {
             criteria.add(Restrictions.like("email","%"+participantCriteria.getEmail()+"%"));
         }
 
         if (!Util.nullOrEmptyOrBlank(participantCriteria.getFoundation())) {
             criteria.add(Restrictions.like("foundation","%"+participantCriteria.getFoundation()+"%"));
         }
 
         if (!Util.nullOrEmptyOrBlank(participantCriteria.getMobile())) {
             criteria.add(Restrictions.like("mobile","%"+participantCriteria.getMobile()+"%"));
         }
 
         if (!Util.nullOrEmptyOrBlank(participantCriteria.getLevel())) {
             criteria.add(Restrictions.eq("level",participantCriteria.getLevel()));
         }
        List<Participant> results = criteria.list();

         session.close();
         return results;
     }
 
     public Integer getGreatestSeat (String level)
     {
         Session session = sessionFactory.openSession();
         Criteria criteria = session.createCriteria(ParticipantSeat.class);
         criteria.setProjection(Projections.max("seat"));
 
         if (!Util.nullOrEmptyOrBlank(level)) {
             criteria.add(Restrictions.like("level",level));
         }
 
         List seats = criteria.list();
         session.close();
         if (!seats.isEmpty()) {
                 return (Integer)seats.get(0);
         }
         else {
             return null;
         }
     }
 
     public void processBatchEntry (List<RegisteredParticipant> participants)
     {
         for(RegisteredParticipant participant : participants)
             addParticipant(participant);
     }
 }
