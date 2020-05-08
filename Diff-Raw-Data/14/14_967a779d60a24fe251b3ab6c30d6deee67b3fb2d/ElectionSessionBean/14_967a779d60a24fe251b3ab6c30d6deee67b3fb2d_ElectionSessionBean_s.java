 package evoting.controller.bean.stateless;
 
 import evoting.counter.bean.stateless.CounterRemote;
 import evoting.validator.bean.stateless.ValidatorSessionRemote;
 import evoting.controller.entity.Commissioner;
 import evoting.controller.entity.Election;
 import evoting.controller.entity.ElectionEvent;
 import evoting.controller.entity.Person;
 import evoting.controller.entity.Voter;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.ejb.EJB;
 import javax.ejb.Stateless;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import evoting.controller.pojo.ControllerException;
 import evoting.counter.pojo.CounterException;
 import evoting.validator.pojo.ValidatorException;
 import javax.persistence.TypedQuery;
 
 @Stateless
 public class ElectionSessionBean implements ElectionSessionRemote {
 
     @EJB
     private ValidatorSessionRemote validatorBean;
 
     @EJB
     private CounterRemote counterBean;
 
     @PersistenceContext
     private EntityManager em;
 
     /**
      * Creates the new election with the given name and type.
      * @param electionName
      * @param electionType
      * @throws ControllerException //TODO description
      */
     @Override
     public void createElection(final String electionName, final String electionType) throws ControllerException {
         Election el = new Election();
         el.setName(electionName);
         el.setType(electionType);
         em.persist(el);
         em.flush();
         byte[] key = null;
         try {            
             key = counterBean.createNewElection(el.getId());
         } catch (CounterException ex) {
             // TODO delete el from db
             Logger.getLogger(ElectionSessionBean.class.getName()).log(Level.SEVERE, "Error during election creation.", ex);
             throw new ControllerException(ex.getMessage());
         }
         //el.setPublicKey(key);
         //em.persist(el);
     }
 
     /**
      *
      * @return all persons in database
      */
     @Override
     public Collection<Person> getAllPerson() {
         TypedQuery<Person> query = em.createNamedQuery(Person.FIND_ALL, Person.class);
         return query.getResultList();
     }
 
     /**
      *
      * @param commissionerLogin login of the given commissioner
      * @return elections where is the given commissioner
      * @throws ControllerException if commissioner not found
      */
     @Override
     public Collection<Election> getCommissionerElection(final String commissionerLogin) throws ControllerException {        
         Commissioner com = em.find(Commissioner.class, commissionerLogin);
         if (com == null) {
             throw new ControllerException("Commissioner not found.");
         }
         Collection<Election> elections = com.getElections();
         elections.size(); // hack ! Pokud se to nezavola, objevi se nejak lazy exception
         return elections;
     }
 
     /**
      * Return collection of unfinished events in given election.
      * @param election A given election.
      * @return Collection of unfinished events. 
      */
     @Override
     public Collection<ElectionEvent> getUnfinishedElectionEvents(Election election) {
         TypedQuery<ElectionEvent> query = em.createNamedQuery(ElectionEvent.FIND_UNFINISHED_BY_ELECTION_ID, ElectionEvent.class).setParameter("election", election);
         return query.getResultList();
     }
 
     /**
      * Adds the given voter to the given election event.
      * @param voterLogin login of the given voter
      * @param eventId Id of the given election event
      * @throws ControllerException if election event not found
      */
     @Override
     public void addVoter(final String voterLogin, final Integer eventId) throws ControllerException {
         ElectionEvent electionEvent = em.find(ElectionEvent.class, eventId);
         if (electionEvent == null) {
             throw new ControllerException("Election event not found.");
         }
         Voter voter = em.find(Voter.class, voterLogin);
         if (voter == null) {
             voter = new Voter();
             voter.setLogin(voterLogin);
             Collection<ElectionEvent> electionEvents = new ArrayList<ElectionEvent>();
             voter.setElectionEvents(electionEvents);
         }
         voter.getElectionEvents().add(electionEvent);
         electionEvent.getVoters().add(voter);
         em.persist(voter);
         em.persist(electionEvent);
         try {
             validatorBean.addVoter(voterLogin, eventId);
         } catch (ValidatorException ex) {
             //TODO db synchronizovano?
             throw new ControllerException(ex.getMessage());
         }
 
     }
 
     /**
      * Deletes voter
      * @param Voter - voter you want to remove
      */
     /**
      * Adds the given commissioner to the given election.
      * @param commissioner the given Commissioner
      * @param electionId Id of the given election
      * @throws ControllerException if election not found
      */
     @Override
     public void addCommissioner(final Person person, final Integer electionId) throws ControllerException {
         Election election = em.find(Election.class, electionId);
         if (election == null) {
             throw new ControllerException("Election not found.");
         }
         Commissioner commissioner = em.find(Commissioner.class, person.getLogin());
         if (commissioner == null) {
             commissioner = new Commissioner();
             commissioner.setFirstName(person.getFirstname());
             commissioner.setLastName(person.getLastname());
             commissioner.setLogin(person.getLogin());
             Collection<Election> elections = new ArrayList<Election>();
             commissioner.setElections(elections);
         }
         commissioner.getElections().add(election);
         election.getCommissioners().add(commissioner);
         em.persist(commissioner);
         em.persist(election);
     }
 
     /**
      *
      * @return all elections in database
      */
     @Override
     public Collection<Election> getAllElection() {
         TypedQuery<Election> query = em.createNamedQuery(Election.FIND_ALL, Election.class);
         return query.getResultList();
     }
 
     /**
      *
      * @param electionId Id of the given election
      * @return commissioners of the given election
      * @throws ControllerException if election not found.
      */
     @Override
     public Collection<Commissioner> getElectionCommissioners(final Integer electionId) throws ControllerException {
         if (electionId == null) {
             return null;
         }
         Election election = em.find(Election.class, electionId);
         if (election == null) {
             throw new ControllerException("Election not found.");
         }
         Collection<Commissioner> commisioners = election.getCommissioners();
         commisioners.size(); // hack on LAZY relationship
         return election.getCommissioners();
     }
 
     /**
      *
      * @param eventId
      * @return election event of the given Id
      * @throws ControllerException if election event not found
      */
     @Override
     public ElectionEvent getElectionEvent(final Integer eventId) throws ControllerException {
         ElectionEvent electionEvent = em.find(ElectionEvent.class, eventId);
         if (electionEvent == null) {
             throw new ControllerException("Election event not found.");
         }
         return electionEvent;
     }
 
     /**
      * Persists the given election event.
      * @param electionEvent
      */
     @Override
     public void changeEvent(ElectionEvent electionEvent) {
         em.merge(electionEvent);
     }
 
     /**
      * Creates the new election event with the given election, name nad info.
      * @param electionId election where will the new election event stand
      * @param name name of the new election event
      * @param info info ot the new election event
      * @throws ControllerException if election not found and .. //TODO description
      */
     @Override
     public void createElectionEvent(final Integer electionId, final String name, final String info) throws ControllerException {
         Election election = em.find(Election.class, electionId);
         if (election == null) {
             throw new ControllerException("Election not found.");
         }
         ElectionEvent electionEvent = new ElectionEvent();
         electionEvent.setName(name);
         electionEvent.setInfo(info);
         electionEvent.setNominatingStarted(Boolean.FALSE);
         electionEvent.setVotingStarted(Boolean.FALSE);
         electionEvent.setFinished(Boolean.FALSE);
         electionEvent.setElectionId(election);
         em.persist(electionEvent);
         em.flush();
         election.getElectionEvents().add(electionEvent);
         em.persist(election);
         em.flush();
         try {
             validatorBean.createNewElectionEvent(electionEvent.getId());
             counterBean.createNewElectionEvent(electionId, electionEvent.getId());
         } catch (CounterException ex) {
             Logger.getLogger(ElectionSessionBean.class.getName()).log(Level.SEVERE, null, ex);
         }
             
         
     }
 
     /**
      *
      * @param eventId Id of the given election event
      * @return voters who vote in the given election event
      * @throws ControllerException if election event not found.
      */
     @Override
     public Collection<Voter> getEventVoters(final Integer eventId) throws ControllerException {
         ElectionEvent event = em.find(ElectionEvent.class, eventId);
         if (event == null) {
             throw new ControllerException("Election event not found.");
         }
         Collection<Voter> voters = event.getVoters();
         voters.size(); // hack for LAZY relationship
         return voters;
     }
 
     /**
      *
      * @param voter - voter, that you want to delete
      * @param eventId - id of event
      * @return void
      * @throws ControllerException if voter not found.
      */
     @Override
     public void deleteVoterFromEvent(Voter voter, Integer eventId) throws ControllerException {
         ElectionEvent event = em.find(ElectionEvent.class, eventId);
         Collection<Voter> voters = event.getVoters();
         voters.size();
         Voter vot = em.find(Voter.class, voter.getLogin());
         Collection<ElectionEvent> events = vot.getElectionEvents();
         events.size();
         events.remove(event);
        voters.remove(vot);
 
         try {
             validatorBean.deleteVoterFromEvent(voter.getLogin(), eventId);

         } catch (Exception ex) {
             //TODO db synchronizovano?
             throw new ControllerException(ex.getMessage());
         }
     }
 
     /**
      *
      * @param login login of the given voter
      * @return ended election events of the given voter
      * @throws ControllerException if voter not found.
      */
     @Override
     public Collection<ElectionEvent> getEndedEvents(final String login) throws ControllerException {
         Voter voter = em.find(Voter.class, login);
         if (voter == null) {
             throw new ControllerException("Voter not found.");
         }
         Collection<ElectionEvent> eventsOut = new ArrayList<ElectionEvent>();
         for (ElectionEvent event : voter.getElectionEvents()) {
             if (event.getFinished()) {
                 eventsOut.add(event);
             }
         }
         return eventsOut;
     }
 
     /**
      *
      * @param electionId
      * @return election of the given election Id
      * @throws ControllerException if election not found.
      */
     @Override
     public Election getElection(final Integer electionId) throws ControllerException {
         if (electionId == null) {
             return null;
         }
         Election election = em.find(Election.class, electionId);
         if (election == null) {
             throw new ControllerException("Election not found.");
         }
         return election;
 
     }
 }
