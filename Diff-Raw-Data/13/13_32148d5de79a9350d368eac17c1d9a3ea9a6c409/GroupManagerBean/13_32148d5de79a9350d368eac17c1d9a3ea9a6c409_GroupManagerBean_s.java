 /*
  * Project :iCardea
  * File : GroupManagerBean.java
  * Date : Dec 15, 2010
  * User : mradules
  */


 package at.srfg.kmt.ehealth.phrs.security.impl;
 
 
 import at.srfg.kmt.ehealth.phrs.security.api.GroupManager;
 import at.srfg.kmt.ehealth.phrs.security.model.Fetcher;
 import at.srfg.kmt.ehealth.phrs.security.model.PhrActor;
 import at.srfg.kmt.ehealth.phrs.security.model.PhrGroup;
 import at.srfg.kmt.ehealth.phrs.util.Util;
 import java.util.HashSet;
 import java.util.List;
 
 import java.util.Set;
 import javax.ejb.Local;
 import javax.ejb.Stateless;
 import javax.persistence.EntityManager;
 import javax.persistence.NoResultException;
 import javax.persistence.PersistenceContext;
 import javax.persistence.Query;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Stateless bean local scoped used to manage and manipulate
  * <code>PhrGroup</code> and related information.
  *
  * @author Mihai
  */
 @Stateless
 @Local(GroupManager.class)
 public class GroupManagerBean implements GroupManager {
 
     /**
      * The Logger instance. All log messages from this class
      * are routed through this member. The Logger name space
      * is <code>at.srfg.kmt.ehealth.phrs.security.impl.GroupManager</code>.
      */
     private static final Logger LOGGER =
             LoggerFactory.getLogger(GroupManagerBean.class);
 
     /**
      * Used to communicate with the underlying persistence layer.
      */
     @PersistenceContext(unitName = "phrs_storage")
     private EntityManager entityManager;
 
     /**
      * Registers a given <code>PhrGroup</code>, if the group already exists
      * then its content will be updated according with the new specified group.
      * 
      * @param group the group to register, it can not be null.
      * @return true if the group is added false if the group is updated.
      * @throws NullPointerException if the <code>group</code> argument is null.
      */
     @Override
     public boolean addGroup(PhrGroup group) {
         if (group == null) {
             final NullPointerException nullException =
                     new NullPointerException("The group argument can not be null.");
             LOGGER.error(nullException.getMessage(), nullException);
             throw nullException;
         }
 
         final Long id = group.getId();
         if (id == null) {
             entityManager.persist(group);
             return true;
         }
        
        if (!entityManager.contains(group)) {
            // for removed
             entityManager.persist(group);
             return true;
         }
        
         entityManager.merge(group);
         return false;
     }
 
     /**
      * Returns true if the specified group exist (was created and registered).
      * 
      * @param group the that which existence is to be tested, 
      * it can not be null.
      * @return true if the specified group exist.
      * @throws NullPointerException if the <code>group</code> argument is null.
      */
     @Override
     public boolean groupExist(PhrGroup group) {
 
         if (group == null) {
             final NullPointerException nullException =
                     new NullPointerException("The group argument can not be null.");
             LOGGER.error(nullException.getMessage(), nullException);
             throw nullException;
         }
 
         final Long id = group.getId();
         if (id == null) {
             return false;
         }
 
         final PhrGroup result = entityManager.find(PhrGroup.class, id);
         return result != null;
     }
 
     /**
      * Removes the specified group.
      * 
      * @param group the group to remove, it can not be null.
      * @return the removed group.
      * @throws NullPointerException if the <code>group</code> argument is null.
      */
     @Override
     public PhrGroup removeGroup(PhrGroup group) {
         if (group == null) {
             final NullPointerException nullException =
                     new NullPointerException("The group argument can not be null.");
             LOGGER.error(nullException.getMessage(), nullException);
             throw nullException;
         }
 
         final PhrGroup managedGroup = entityManager.merge(group);
         entityManager.remove(managedGroup);
 
         return managedGroup;
     }
 
     /**
      * Removes all the registered groups. After this the \
      * <code>getAllGroups</code> method call will return an empty set.
      */
     @Override
     public void removeAllGroups() {
         final Query allGroupsQuery =
                 entityManager.createNamedQuery("selectAllGroups");
 
         final List<PhrGroup> groups = allGroupsQuery.getResultList();
         // mihai :
         // this may tahe while for a big number of groups.
         // I try to do it on one fire using a bulk operation but
         // the bulk delete have problems with the OneToMany
         // the bulk operation doen not support cascade :(.
 
         for (PhrGroup group : groups) {
             entityManager.remove(group);
         }
     }
 
     /**
      * Return all the registered group. <br/>
      * <b>Note :</b> The set of groups contains groups with
      * all the lazy initialized relation <b>loaded</b> 
      * according with the specified fetcher.
      * If the fetcher is null then this method behavior is the same with the
      * <code>getAllRoles</code> method.
      *
      * @return all the registered groups.
      * @see #getAllGroups(at.srfg.kmt.ehealth.phrs.security.model.Fetcher)
      * @see FetcherFactory#GROUP_FETCHER
      */
     @Override
     public Set<PhrGroup> getAllGroups() {
         return getAllGroups(null);
     }
 
     /**
      * Return all the registered groups. <br/>
      * <b>Note :</b> The set of groups contains groups with
      * all the lazy initialized relation <b>loaded</b>, 
      * the group members are available according with the specified fetcher.
      * If the fetcher is null then this method behavior is the same with the
      * <code>getAllGroups</code> method.
      *
      * @return all the registered groups.
      * @see #getAllGroups()
      * @see FetcherFactory#GROUP_FETCHER
      */
     @Override
     public final Set<PhrGroup> getAllGroups(Fetcher<PhrGroup> fetcher) {
         // the reason why this method is final is becuase the
         // getAllGroups is uses this method and I wnat to
         // avoid commplication by overwriting
 
 
         final Query query = entityManager.createNamedQuery("selectAllGroups");
         final List resultList = query.getResultList();
         // TODO : if the groups count goes to big do paging here.
         final Set<PhrGroup> groups = new HashSet<PhrGroup>(resultList);
 
         if (fetcher != null) {
             for (PhrGroup group : groups) {
                 fetcher.fetch(null);
             }
         }
 
         return groups;
     }
 
     /**
      * Returns a <code>PhrGroup</code> where the name attribute exactly match
      * (case sensitive) the given <code>name</code>. If there is no matching
      * group this method returns null. </br>
      * <b>Note :</b> the result group contains has all the lazy
      * initialized relation <b>not loaded</b>, 
      * any attempt to use/refer the group members (which are 
      * lazy initialized) will ends in to an exception.
      *
      * @param name the name for the group to search, it can not be null.
      * @param fetcher the fetcher used to load the lazy initialized relations.
      * @return a <code>PhrGroup</code> where the name attribute exactly match
      * the given <code>name</code> or null for no match.
      * @throws NullPointerException if the <code>name</code> argument is null.     */
     @Override
     public PhrGroup getGroupForName(String name) {
         final PhrGroup result = getGroupForName(name, null);
         return result;
     }
 
     @Override
     public PhrGroup getGroupForName(String name, Fetcher<PhrGroup> fetcher) {
         if (name == null) {
             final NullPointerException nullException =
                     new NullPointerException("The name argument can not be null.");
             LOGGER.error(nullException.getMessage(), nullException);
             throw nullException;
         }
 
         final Query query = entityManager.createNamedQuery("selectGroupByName");
         query.setParameter("name", name);
         final PhrGroup result;
         try {
             result = (PhrGroup) query.getSingleResult();
             // mihai : 
             // I call 'result.getMembers().size();' to ensure that
             // that all the actors are waking up when the group is retuned.
             // the reason for this is : the OneToMany lazy initialisation works
             // only in the same transtion/session - if the session is done
             // then the lazy initalisayion will fail.
             // result.getMembers().size();
         } catch (NoResultException exception) {
             LOGGER.debug("No PHRS Group with name [{}] was found.", name);
             return null;
         }
 
         if (fetcher != null) {
             fetcher.fetch(result);
         }
 
         return result;
 
     }
 
     /**
      * Returns a set that contains all the PHRS groups with the name matching
      * a given JPSQ like patten.<br>
      * The namePattern is a string literal or a string-valued
      * input parameter in which an underscore (_) stands for any single
      * character, a percent (%) character stands for any sequence of
      * characters (including the empty sequence), and all other characters
      * stand for themselves.
      *
      * @param namePattern the name pattern to search, it can not be null.
      * @return a set that contains all the PHRS groups with the name matching
      * a given JPSQ like patten.
      */
     @Override
     public Set<PhrGroup> getGroupsForNamePattern(String namePattern) {
 
         if (namePattern == null) {
             final NullPointerException nullException =
                     new NullPointerException("The namePattern argument can not be null.");
             LOGGER.error(nullException.getMessage(), nullException);
             throw nullException;
         }
 
         final Query query = entityManager.createNamedQuery("selectGroupsByNamePattern");
         final Query queryResult = query.setParameter("name_pattern", namePattern);
         final List resultList = queryResult.getResultList();
 
         final Set<PhrGroup> result = new HashSet<PhrGroup>(resultList);
         return result;
     }
 
     @Override
     public Set<PhrGroup> getGroupsForNamePattern(String namePattern, Fetcher<PhrGroup> fetcher) {
         if (namePattern == null) {
             final NullPointerException nullException =
                     new NullPointerException("The namePattern argument can not be null.");
             LOGGER.error(nullException.getMessage(), nullException);
             throw nullException;
         }
 
         final Query query = entityManager.createNamedQuery("selectGroupsByNamePattern");
         final Query queryResult = query.setParameter("name_pattern", namePattern);
         final List resultList = queryResult.getResultList();
 
         final Set<PhrGroup> groups = new HashSet<PhrGroup>(resultList);
         if (fetcher != null) {
             for (PhrGroup group : groups) {
                 fetcher.fetch(group);
             }
         }
 
         return groups;
     }
 
     @Override
     public final void addActorToGroup(PhrGroup group, PhrActor actor) {
         // the reason why this method is final is becuase the
         // addActorsToGroup is uses this method and I wnat to
         // avoid commplication by overwriting
 
         if (group == null) {
             final NullPointerException nullException =
                     new NullPointerException("The group argument can not be null.");
             LOGGER.error(nullException.getMessage(), nullException);
             throw nullException;
         }
 
 
         if (actor == null) {
             final NullPointerException nullException =
                     new NullPointerException("The actor argument can not be null.");
             LOGGER.error(nullException.getMessage(), nullException);
             throw nullException;
         }
 
 
         final Object[] toLog = Util.forLog(actor, group);
         LOGGER.debug("Tries to assign actor [{}] to group [{}].", toLog);
 
         final PhrGroup managedGroup = entityManager.merge(group);
         final PhrActor managedActor = entityManager.merge(actor);
 
         Set<PhrActor> members = managedGroup.getMembers();
         if (members == null) {
             members = new HashSet<PhrActor>();
         }
 
         members.add(managedActor);
         // I am not sure if I need to call the set.
         managedGroup.setMembers(members);
 
 
         LOGGER.debug("Actor [{}] was assined to group [{}].", toLog);
     }
 
     @Override
     public void addActorsToGroup(PhrGroup group, Set<PhrActor> actors) {
 
         if (actors == null) {
             final NullPointerException nullException =
                     new NullPointerException("The actors argument can not be null.");
             LOGGER.error(nullException.getMessage(), nullException);
             throw nullException;
         }
 
         if (actors.isEmpty()) {
             final IllegalArgumentException argumentException =
                     new IllegalArgumentException("The actors can not be an empty.");
             LOGGER.error(argumentException.getMessage(), argumentException);
             throw argumentException;
         }
 
         if (group == null) {
             final NullPointerException nullException =
                     new NullPointerException("The group argument can not be null.");
             LOGGER.error(nullException.getMessage(), nullException);
             throw nullException;
         }
 
         final Object[] toLog = Util.forLog(actors, group);
         LOGGER.debug("Tries to assign actors [{}] to group [{}].", toLog);
 
         for (PhrActor actor : actors) {
             addActorToGroup(group, actor);
         }
 
         LOGGER.debug("Actors [{}] was assined to group [{}].", toLog);
     }
 
     @Override
     public final void removeActorFromGroup(PhrGroup group, PhrActor actor) {
 
         // the reason why this method is final is becuase the
         // removeActorsFromGroup is uses this method and I wnat to
         // avoid commplication by overwriting
         if (actor == null) {
             final NullPointerException nullException =
                     new NullPointerException("The actor argument can not be null.");
             LOGGER.error(nullException.getMessage(), nullException);
             throw nullException;
         }
 
         if (group == null) {
             final NullPointerException nullException =
                     new NullPointerException("The group argument can not be null.");
             LOGGER.error(nullException.getMessage(), nullException);
             throw nullException;
         }
 
         final Object[] toLog = Util.forLog(actor, group);
         LOGGER.debug("Tries to remove actor [{}] from group [{}].", toLog);
 
         final PhrGroup managedGroup = entityManager.merge(group);
         final Set<PhrActor> members = managedGroup.getMembers();
         if (members == null) {
             return;
         }
         members.remove(actor);
 
         // I am not sure if I need to call the set.
         managedGroup.setMembers(members);
     }
 
     @Override
     public void removeActorsFromGroup(PhrGroup group, Set<PhrActor> actors) {
         if (actors == null) {
             final NullPointerException nullException =
                     new NullPointerException("The actors argument can not be null.");
             LOGGER.error(nullException.getMessage(), nullException);
             throw nullException;
         }
 
         if (actors.isEmpty()) {
             final IllegalArgumentException argumentException =
                     new IllegalArgumentException("The actors can not be an empty.");
             LOGGER.error(argumentException.getMessage(), argumentException);
             throw argumentException;
         }
 
         if (group == null) {
             final NullPointerException nullException =
                     new NullPointerException("The group argument can not be null.");
             LOGGER.error(nullException.getMessage(), nullException);
             throw nullException;
         }
 
         final Object[] toLog = Util.forLog(actors, group);
         LOGGER.debug("Tries to remove actors [{}] from the group [{}].", toLog);
 
         for (PhrActor actor : actors) {
             removeActorFromGroup(group, actor);
         }
 
         entityManager.merge(group);
         LOGGER.debug("Actors [{}] was removed from the group [{}].", toLog);
     }
 }
