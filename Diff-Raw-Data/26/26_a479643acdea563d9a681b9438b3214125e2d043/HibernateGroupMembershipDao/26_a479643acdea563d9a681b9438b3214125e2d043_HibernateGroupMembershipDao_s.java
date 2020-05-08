 /**
  * 
  */
 package net.frontlinesms.data.repository.hibernate;
 
 import java.sql.SQLException;
 import java.util.List;
 
 import org.hibernate.HibernateException;
 import org.hibernate.Session;
 import org.hibernate.criterion.DetachedCriteria;
 import org.hibernate.criterion.Projections;
 import org.hibernate.criterion.Restrictions;
 import org.springframework.dao.DataAccessException;
 import org.springframework.dao.support.DataAccessUtils;
 import org.springframework.orm.hibernate3.HibernateCallback;
 import org.springframework.transaction.annotation.Transactional;
 
 import net.frontlinesms.data.DuplicateKeyException;
 import net.frontlinesms.data.domain.Contact;
 import net.frontlinesms.data.domain.Group;
 import net.frontlinesms.data.domain.GroupMembership;
 import net.frontlinesms.data.repository.GroupMembershipDao;
 
 /**
  * @author aga
  *
  */
 public class HibernateGroupMembershipDao extends BaseHibernateDao<GroupMembership> implements GroupMembershipDao {
 
 //> CONSTRUCTORS
 	HibernateGroupMembershipDao() {
 		super(GroupMembership.class);
 	}
 	
 //> DAO METHODS
 	/** @see GroupMembershipDao#addMember(Group, Contact) */
 	public boolean addMember(Group g, Contact contact) {
 		GroupMembership membership = new GroupMembership(g, contact);
 		try {
 			super.save(membership);
 			return true;
 		} catch (DuplicateKeyException e) {
 			return false;
 		}
 	}
 
 	/** @see GroupMembershipDao#getActiveMembers(Group) */
 	@SuppressWarnings("unchecked")
 	public List<Contact> getActiveMembers(Group group) {
 		return this.getHibernateTemplate().find("SELECT mem.contact FROM GroupMembership AS mem, Contact AS c WHERE mem.group='" + group.getPath() + "' AND c.active=TRUE");
 	}
 
 	/** @see GroupMembershipDao#getGroups(Contact) */
 	@SuppressWarnings("unchecked")
 	public List<Group> getGroups(Contact contact) {
 		return this.getHibernateTemplate().find("SELECT mem.group FROM GroupMembership AS mem WHERE mem.contact='" + contact.getId() + "'");
 	}
 
 	/** @see GroupMembershipDao#getMemberCount(Group) */
 	public int getMemberCount(Group group) {
 		if(group.isRoot()) {
 			DetachedCriteria crit = DetachedCriteria.forClass(Contact.class);
 			crit.setProjection(Projections.rowCount());
 			return DataAccessUtils.intResult(this.getHibernateTemplate().findByCriteria(crit));
 		} else {
 			DetachedCriteria crit = super.getCriterion();
 			crit.add(Restrictions.eq("group", group));
 			return super.getCount(crit);
 		}
 	}
 
 	/** @see GroupMembershipDao#getMembers(Group) */
 	@SuppressWarnings("unchecked")
 	public List<Contact> getMembers(Group group) {
 		if(group.isRoot()) {
 			return this.getHibernateTemplate().findByCriteria(DetachedCriteria.forClass(Contact.class));
 		} else {
			return this.getHibernateTemplate().find("SELECT mem.contact FROM GroupMembership AS mem WHERE " +
					"mem.group='" + group.getPath() + "' OR " +
					"mem.group LIKE '" + group.getPath() + Group.PATH_SEPARATOR + "%'");
 		}
 	}
 
 	/** @see GroupMembershipDao#getMembers(Group, int, int) */
 	public List<Contact> getMembers(Group group, int startIndex, int limit) {
 		// TODO Auto-generated method stub
 		List<Contact> allMembers = getMembers(group);
 		return allMembers.subList(startIndex, Math.min(allMembers.size(), startIndex + limit));
 	}
 
 	/** @see GroupMembershipDao#isMember(Group, Contact) */
 	public boolean isMember(Group group, Contact contact) {
 		DetachedCriteria crit = getMembershipCriteria(group, contact);
 		return super.getCount(crit) == 1;
 	}
 
 	/** @see GroupMembershipDao#removeMember(Group, Contact) */
 	@Transactional
 	public boolean removeMember(Group group, Contact contact) {
 		try {
 			DetachedCriteria crit = getMembershipCriteria(group, contact);
 			this.getHibernateTemplate().delete(DataAccessUtils.uniqueResult(this.getList(crit)));
 			return true;
 		} catch(DataAccessException ex) {
 			return false;
 		}
 	}
 
 //> PRIVATE HELPER METHODS
 	/** @return criteria for fetching a {@link GroupMembership} entity */
 	private DetachedCriteria getMembershipCriteria(Group group, Contact contact) {
 		DetachedCriteria crit = super.getCriterion();
 		crit.add(Restrictions.eq("contact", contact));
 		crit.add(Restrictions.eq("group", group));
 		return crit;
 	}
 }
