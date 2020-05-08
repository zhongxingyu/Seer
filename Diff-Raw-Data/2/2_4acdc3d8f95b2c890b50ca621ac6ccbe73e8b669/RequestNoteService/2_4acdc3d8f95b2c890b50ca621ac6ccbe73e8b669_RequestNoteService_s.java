 package fr.cg95.cvq.service.request.impl;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.springframework.beans.BeansException;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.ApplicationContextAware;
 
 import fr.cg95.cvq.business.request.Request;
 import fr.cg95.cvq.business.request.RequestEvent;
 import fr.cg95.cvq.business.request.RequestNote;
 import fr.cg95.cvq.business.request.RequestNoteType;
 import fr.cg95.cvq.business.request.RequestEvent.COMP_DATA;
 import fr.cg95.cvq.business.request.RequestEvent.EVENT_TYPE;
 import fr.cg95.cvq.dao.request.IRequestDAO;
 import fr.cg95.cvq.dao.request.IRequestNoteDAO;
 import fr.cg95.cvq.exception.CvqException;
 import fr.cg95.cvq.security.SecurityContext;
 import fr.cg95.cvq.security.annotation.Context;
 import fr.cg95.cvq.security.annotation.ContextPrivilege;
 import fr.cg95.cvq.security.annotation.ContextType;
 import fr.cg95.cvq.service.authority.IAgentService;
 import fr.cg95.cvq.service.request.IRequestNoteService;
 
 public class RequestNoteService implements IRequestNoteService, ApplicationContextAware {
 
     private IRequestNoteDAO requestNoteDAO;
     private IRequestDAO requestDAO;
     
     private IAgentService agentService;
     
     private ApplicationContext applicationContext;
     
     @Override
     @Context(types = {ContextType.ECITIZEN, ContextType.AGENT}, privilege = ContextPrivilege.READ)
     public List<RequestNote> getNotes(final Long requestId, RequestNoteType type)
         throws CvqException {
 
         // filter private notes one is not allowed to see
         // (agent private notes when ecitizen, and vice-versa)
         // TODO refactor this security filtering which doesn't look very robust
         List<RequestNote> result = new ArrayList<RequestNote>();
         List<RequestNote> notes = requestNoteDAO.listByRequestAndType(requestId, type);
         boolean isAgentNote;
         for (RequestNote note : notes) {
             isAgentNote = agentService.exists(note.getUserId());
             if (!note.getType().equals(RequestNoteType.INTERNAL)
                 || (isAgentNote
                     && SecurityContext.BACK_OFFICE_CONTEXT.equals(SecurityContext.getCurrentContext()))
                 || (!isAgentNote
                     && SecurityContext.FRONT_OFFICE_CONTEXT.equals(SecurityContext.getCurrentContext()))) {
                     result.add(note);
             }
         }
         return result;
     }
 
     public RequestNote getLastNote(final Long requestId, RequestNoteType type)
         throws CvqException {
         List<RequestNote> notes = getNotes(requestId, type);
         if (notes == null || notes.isEmpty()) return null;
         return notes.get(notes.size() -1);
     }
 
     public RequestNote getLastAgentNote(final Long requestId, RequestNoteType type)
         throws CvqException {
         List<RequestNote> notes = getNotes(requestId, type);
         if (notes == null || notes.isEmpty()) return null;
         Collections.reverse(notes);
         for (RequestNote note : notes) {
             if (agentService.exists(note.getUserId())) {
                 return note;
             }
         }
         return null;
     }
 
     @Override
     @Context(types = {ContextType.ECITIZEN, ContextType.AGENT}, privilege = ContextPrivilege.WRITE)
     public void addNote(final Long requestId, final RequestNoteType rtn, final String note) {
 
 
         Long userId = SecurityContext.getCurrentUserId();
 
         RequestNote requestNote = new RequestNote();
         requestNote.setType(rtn);
         requestNote.setNote(note);
         requestNote.setUserId(userId);
         requestNote.setDate(new Date());
 
         Request request = requestDAO.findById(requestId);
         if (request.getNotes() == null) {
             Set<RequestNote> notes = new HashSet<RequestNote>();
             notes.add(requestNote);
             request.setNotes(notes);
         } else {
             request.getNotes().add(requestNote);
         }
 
         updateLastModificationInformation(request);
         if (agentService.exists(userId)) {
             RequestEvent requestEvent =
                 new RequestEvent(this, EVENT_TYPE.NOTE_ADDED, request);
             requestEvent.addComplementaryData(COMP_DATA.REQUEST_NOTE, requestNote);
             applicationContext.publishEvent(requestEvent);
         }
     }
 
     private void updateLastModificationInformation(Request request) {
 
         request.setLastModificationDate(new Date());
         request.setLastInterveningUserId(SecurityContext.getCurrentUserId());
 
         requestDAO.update(request);
     }
 
     public void setRequestDAO(IRequestDAO requestDAO) {
         this.requestDAO = requestDAO;
     }
 
     public void setRequestNoteDAO(IRequestNoteDAO requestNoteDAO) {
         this.requestNoteDAO = requestNoteDAO;
     }
 
     public void setAgentService(IAgentService agentService) {
         this.agentService = agentService;
     }
 
     @Override
     public void setApplicationContext(ApplicationContext arg0) throws BeansException {
         this.applicationContext = arg0;
     }
 }
