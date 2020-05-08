 package com.parsek.test;
 
 import com.parsek.test.model.Member;
 import com.parsek.test.seam.Messages;
 
 import javax.annotation.PostConstruct;
 import javax.ejb.Stateful;
 import javax.enterprise.event.Event;
 import javax.enterprise.inject.Model;
 import javax.enterprise.inject.Produces;
 import javax.inject.Inject;
 import javax.inject.Named;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 
 @Stateful @Model
 public class MemberRegistration {
     @PersistenceContext private EntityManager em;
     @Inject private Event<Member> memberEventSrc;
     @Inject private Messages messages;
 
     private Member newMember;
 
     @PostConstruct
     public void initNewMember() {
         newMember = new Member();
     }
 
     @Produces @Named
     public Member getNewMember() {
         return newMember;
     }
 
     public void register() throws Exception {
         em.persist(newMember);
         memberEventSrc.fire(newMember);
         messages.addInfo("member.created", newMember.getName());
         initNewMember();
     }
 
     public void delete(Member m) {
         em.remove(em.getReference(Member.class, m.getId()));
        messages.addInfo("member.deleted", m.getName());
         memberEventSrc.fire(m);
     }
 }
