 /* vim: set ts=2 et sw=2 cindent fo=qroca: */
 
 package com.globant.katari.gadgetcontainer.domain;
 
 import java.util.List;
 
 import org.apache.commons.lang.Validate;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
 
 /** Repository for the gadget groups.
  *
  * @author waabox(emiliano[dot]arango[at]globant[dot]com)
  */
 public class GadgetGroupRepository extends HibernateDaoSupport {
 
   /** The class logger.
    */
   private static Logger log = LoggerFactory.getLogger(
       GadgetGroupRepository.class);
 
   /** Find the requested gadget group by name and user.
    *
    * If the name of the group corresponds to a 'static' group, then the userId
    * is ignored, and returns that group. The userId must still not be null.
    *
    * @param userId the user that owns the group. It can not be null.
    *
    * @param name the gadget group name. It can not be empty.
    *
    * @return the gadget group found or null.
    */
   @SuppressWarnings("unchecked")
   public GadgetGroup findGadgetGroup(final long userId, final String name) {
     Validate.notEmpty(name, "the gadget group cannot be empty");
     Validate.notNull(userId, "the user cannot be null");
 
     log.trace("Entering findGadgetGroup('{}', '{}')", userId, name);
 
     List<GadgetGroup> groups = getHibernateTemplate().find("from"
         + " GadgetGroup where name = ?"
         + " and (type = ? or (type = ? and owner.id = ?))",
         new Object[]{name, GadgetGroup.Type.SHARED,
             GadgetGroup.Type.CUSTOMIZABLE, userId});
 
     if(groups.isEmpty()) {
       log.trace("Leaving findGadgetGroup, no group found");
       return null;
     }
 
     GadgetGroup group = groups.get(0);
 
     log.trace("Leaving findGadgetGroup with a group");
     return group;
   }
 
   /** Find the requested gadget group template by name.
    *
    * @param name the gadget group name. It can not be empty.
    *
    * @return the gadget group template or null if not found.
    */
   @SuppressWarnings("unchecked")
   public GadgetGroup findGadgetGroupTemplate(final String name) {
     Validate.notEmpty(name, "the gadget group cannot be empty");
 
     log.trace("Entering findGadgetGroupTemplate('{}')", name);
 
     List<GadgetGroup> groups = getHibernateTemplate().find("from"
         + " GadgetGroup where name = ? and type = ?",
         new Object[]{name, GadgetGroup.Type.TEMPLATE});
 
     if(groups.isEmpty()) {
       log.trace("Leaving findGadgetGroupTemplate, no group found");
       return null;
     }
 
     GadgetGroup group = groups.get(0);
 
     log.trace("Leaving findGadgetGroupTemplate with a group");
     return group;
   }
 
   /** Saves the given group in the db.
    *
    * @param gadgetGroup {@link GadgetGroup} the group to store. Can not be
    * null.
    */
   public void save(final GadgetGroup gadgetGroup) {
     log.trace("Entering save");
     Validate.notNull(gadgetGroup, "the group can not be null");
     getHibernateTemplate().saveOrUpdate(gadgetGroup);
     log.trace("Leaving save");
   }
 }
 
