 package com.digt.jpa.spi;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.concurrent.Future;
 import java.util.logging.Logger;
 
 import javax.persistence.EntityManager;
 import javax.persistence.NoResultException;
 import javax.persistence.Query;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.shindig.auth.SecurityToken;
 import org.apache.shindig.common.util.ImmediateFuture;
 import org.apache.shindig.protocol.ProtocolException;
 import org.apache.shindig.protocol.RestfulCollection;
 import org.apache.shindig.social.opensocial.jpa.GroupDb;
 import org.apache.shindig.social.opensocial.jpa.PersonDb;
 import org.apache.shindig.social.opensocial.jpa.api.FilterCapability;
 import org.apache.shindig.social.opensocial.jpa.api.FilterSpecification;
 import org.apache.shindig.social.opensocial.jpa.spi.JPQLUtils;
 import org.apache.shindig.social.opensocial.jpa.spi.SPIUtils;
 import org.apache.shindig.social.opensocial.model.Group;
 import org.apache.shindig.social.opensocial.model.Person;
 import org.apache.shindig.social.opensocial.spi.CollectionOptions;
 import org.apache.shindig.social.opensocial.spi.GroupId;
 import org.apache.shindig.social.opensocial.spi.GroupService;
 import org.apache.shindig.social.opensocial.spi.UserId;
 
 import com.digt.common.utils.LogUtils;
 import com.digt.jpa.MembershipDb;
 import com.digt.model.GroupInvite;
 import com.digt.model.Membership;
 import com.digt.model.Membership.Type;
 import com.google.caja.util.Lists;
 import com.google.common.collect.ImmutableSet;
 
 public class GroupServiceDb extends EntityServiceDb 
 	implements GroupService {
 
 	private static final Logger LOG = Logger.getLogger(
 			GroupServiceDb.class.getName());
 
 
 	public Future<Void> createGroup(UserId userId, Group group, SecurityToken token) {
 
 		EntityManager entityManager = getEntityManager();
 		try {
 			Query q = entityManager.createNamedQuery(PersonDb.FINDBY_PERSONID);
 			q.setParameter("id", userId.getUserId(token));
 			Person owner = (Person) q.getSingleResult();
 			
 			GroupDb groupDb = (GroupDb) group;
 			groupDb.setOwner(owner);
 			
 			if (group.getId() == null) {
 				GroupId groupId = GroupId.fromJson("user_group" + SPIUtils.generateId(owner.getId()+System.currentTimeMillis())); 
 				groupDb.setId(groupId);
 			}
 
 			if (!entityManager.getTransaction().isActive()) {
 				entityManager.getTransaction().begin();
 			}
 			
 			entityManager.persist(groupDb);
 			entityManager.getTransaction().commit();
 		} catch (NoResultException e) {
 			LOG.severe(LogUtils.getTrace(e));
 			throw new ProtocolException(HttpServletResponse.SC_BAD_REQUEST, "Некорректно указан владелец группы.");
 		} catch (Exception e) {
 			LOG.severe(LogUtils.getTrace(e));
 			throw new ProtocolException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
 		} finally {
 			entityManager.close();
 		}
 		return ImmediateFuture.newInstance(null);
 	}
 
 	public Future<Group> getGroup(UserId userId, String gid, SecurityToken token) {
 	
 			String uid = userId.getUserId(token);
 			EntityManager entityManager = getEntityManager();
 			Query q = entityManager.createQuery(GroupDb.JPQL_FINDGROUPBYID + " and g.owner.id=?2");
 			q.setParameter(1, gid);
 			q.setParameter(2, uid);
 			Group group = null;
 			try {
 				group = (Group) q.getSingleResult();
 			} catch (NoResultException e) {}
 			
 			return ImmediateFuture.newInstance(group);
 	}
 	
 	@Override
 	public Future<RestfulCollection<Group>> getGroups(UserId userId,
 			CollectionOptions options, Set<String> fields, SecurityToken token) {
 
 		StringBuilder sb = new StringBuilder();
 		sb.append(GroupDb.JPQL_FINDGROUP);
 		List<Object> paramList = new ArrayList<Object>();
 		paramList.add(SPIUtils.getUserList(ImmutableSet.of(userId), token));
 		addFilterClause(sb, GroupDb.FILTER_CAPABILITY, options, paramList);
 
 		EntityManager entityManager = getEntityManager();
 		long totalResults = JPQLUtils.getTotalResults(entityManager, sb.toString(), paramList);
 		List<Group> glist = null;
 		// Execute ordered and paginated query
 		if (totalResults > 0) {
 			JPQLUtils.addOrderClause(sb, options, "g");
 			glist = JPQLUtils.getListQuery(entityManager, sb.toString(), paramList, options);
 		}
 		entityManager.close();
 		if (glist == null) {
 			glist = Lists.newArrayList();
 		}
 		
 		if (options == null)
 			options = new CollectionOptions();
 		
 		RestfulCollection<Group> restCollection = new RestfulCollection<Group>(
 				glist, options.getFirst(), (int) totalResults, options.getMax());
 
 		return ImmediateFuture.newInstance(restCollection);
 	}
 
 	public Future<Group> acceptGroupInvite(UserId userId, String gid, SecurityToken token) {
 		
 		EntityManager entityManager = getEntityManager();
 		try {
 			Query q = entityManager.createQuery(GroupDb.JPQL_FINDGROUPBYID);
 			q.setParameter(1, gid);
 			GroupDb group = (GroupDb) q.getSingleResult();
 			String uid = userId.getUserId(token);
 			Person p = null;
 			for (GroupInvite invite : group.getInvites()) {
 				if (invite.getPerson().getId().equals(uid)) {
 					p = invite.getPerson();
 					break;
 				}
 			}
 			if (p == null) throw new ProtocolException(HttpServletResponse.SC_BAD_REQUEST, "Ошибка.");
			
			MembershipDb m = new MembershipDb();
			m.setGroup(group);
			m.setPerson(p);
			m.setType(Membership.Type.MEMBER);
			
			group.getMembers().add(m);
			if (!entityManager.getTransaction().isActive())
				entityManager.getTransaction().begin();
			entityManager.merge(group);
			entityManager.getTransaction().commit();
 			
 			return ImmediateFuture.newInstance((Group)group);
 		} catch (NoResultException e) {
 			LOG.fine(LogUtils.getTrace(e));
 			throw new ProtocolException(HttpServletResponse.SC_BAD_REQUEST, "Некорректно указан идентификатор пользователя или группы.");
 		} catch (Exception e) {
 			LOG.severe(LogUtils.getTrace(e));
 			throw new ProtocolException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
 		} finally {
 			entityManager.close();
 		}
 	}
 	
 	public Future<Void> addToGroup(UserId owner, Set<UserId> members, Type type, GroupId groupId,
 			SecurityToken token) {
 
 		String uid = owner.getUserId(token);
 		String gid = groupId.getGroupId();
 
 		EntityManager entityManager = getEntityManager();
 		try {
 			Query q = entityManager.createNamedQuery(GroupDb.QUERY_FINDBYIDMSHIP);
 			q.setParameter(1, gid);
 			q.setParameter(2, uid);
 			q.setParameter(3, Type.ADMIN);
 			GroupDb	group = (GroupDb) q.getSingleResult();
 
 			List<String> params = SPIUtils.getUserList(members);
 			StringBuilder sb = new StringBuilder(PersonDb.JPQL_FINDPERSON);
 			JPQLUtils.addInClause(sb, "p", "id", 1, params.size());
 			List<Person> pList = JPQLUtils.getListQuery(entityManager,sb.toString(), params, null);
 			Set<Membership> newMembers = new HashSet<Membership>();
 			for (Person p : pList) {
 				boolean contains = false;
 				for (Membership om: group.getMembers()) {
 					if (om.getUserId().equals(p.getId())) {
 						contains = true;
 						break;
 					}
 				}
 				
 				if (!contains) {
 					MembershipDb m = new MembershipDb();
 					m.setGroup(group);
 					m.setPerson(p);
 					m.setType(type);
 					newMembers.add(m);
 				}
 			}
 			
 			if (!newMembers.isEmpty())
 			{
 				if (!entityManager.getTransaction().isActive())
 					entityManager.getTransaction().begin();
 				group.getMembers().addAll(newMembers);
 				//entityManager.merge(group);
 				entityManager.getTransaction().commit();
 			}
 		} catch (NoResultException e) {
 			LOG.fine(LogUtils.getTrace(e));
 			throw new ProtocolException(HttpServletResponse.SC_BAD_REQUEST, "Некорректно указан id или администратор группы.");
 		} catch (Exception e) {
 			LOG.severe(LogUtils.getTrace(e));
 			throw new ProtocolException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
 		} finally {
 			entityManager.close();
 		}
 		return null;
 	}
 	
 	public Future<Void> removeGroup(UserId owner, GroupId groupId, SecurityToken token) {
 		
 		EntityManager entityManager = getEntityManager();
 		try {
 			String uid = owner.getUserId(token);
 			String gid = groupId.getGroupId();
 			Query q = entityManager.createQuery(GroupDb.JPQL_FINDGROUPBYID + " and g.owner.id=?2");
 			
 			q.setParameter(1, gid);
 			q.setParameter(2, uid);
 			GroupDb group = (GroupDb) q.getSingleResult();
 
 			if (!entityManager.getTransaction().isActive())
 				entityManager.getTransaction().begin();
 			entityManager.remove(group);
 			entityManager.getTransaction().commit();
 		} catch (NoResultException e) {
 			throw new ProtocolException(HttpServletResponse.SC_BAD_REQUEST, "Группа не найдена или вы не являетесь владельцем данной группы.");
 		} catch (Exception e) {
 			LOG.severe(LogUtils.getTrace(e));
 			throw new ProtocolException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
 		} finally {
 			entityManager.close();
 		}
 		
 		return ImmediateFuture.newInstance(null);
 	}
 	
 	public Future<Void> removeFromGroup(UserId owner, String memberId, GroupId groupId, SecurityToken token) {
 		
 		String uid = owner.getUserId(token);
 		String gid = groupId.getGroupId();
 		EntityManager entityManager = getEntityManager();
 		try {
 			Query q = entityManager.createNamedQuery(GroupDb.QUERY_FINDBYIDMSHIP);
 			q.setParameter(1, gid);
 			q.setParameter(2, uid);
 			q.setParameter(3, Type.ADMIN);
 			GroupDb	group = (GroupDb) q.getSingleResult();
 			
 			if (!entityManager.getTransaction().isActive())
 				entityManager.getTransaction().begin();
 
 			for (Membership m: group.getMembers())
 			{
 				if (m.getUserId().equals(memberId))
 				{
 					group.getMembers().remove(m);
 					break;
 				}
 			}
 			
 			//entityManager.merge(group);
 			entityManager.getTransaction().commit();
 			
 		} catch (NoResultException e) {
 			LOG.severe(LogUtils.getTrace(e));
 			throw new ProtocolException(HttpServletResponse.SC_BAD_REQUEST, "Некорректно указан id или администратор группы.");
 		} catch (Exception e) {
 			LOG.severe(LogUtils.getTrace(e));
 			throw new ProtocolException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
 					"Error deleting member from group " + groupId.getGroupId() + " owner " + uid);
 		} finally {
 			entityManager.close();
 		}
 		return null;
 	}
 	
 	public Future<RestfulCollection<Membership>> getMembers(UserId userId, String gid, CollectionOptions options, SecurityToken token) {
 		
 		String uid = userId.getUserId(token);
 		List<String> params = new ArrayList<String>();
 		params.add(uid);
 		params.add(gid);
 		
 		StringBuilder sb = new StringBuilder(MembershipDb.JPQL_FIND);
 		addFilterClause(sb, MembershipDb.FILTER_CAPABILITY, options, params);
 		
 		EntityManager entityManager = getEntityManager();
 		
 		Long totalResults = JPQLUtils.getTotalResults(entityManager, sb.toString(), params);
 		List<Membership> mList;
 		if (totalResults > 0) {
 			JPQLUtils.addOrderClause(sb, options, "m");
 			mList = JPQLUtils.getListQuery(entityManager, sb.toString(), params, options);
 		} else {
 			mList = new ArrayList<Membership>();
 		}
 		
 		entityManager.close();
 		
 		RestfulCollection<Membership> res = new RestfulCollection<Membership>(
 				mList, options.getFirst(), totalResults.intValue(), options.getMax());
 		
 		return ImmediateFuture.newInstance(res);
 	}
 	
 	protected void addFilterClause(StringBuilder sb, FilterCapability filterable, 
 			CollectionOptions options,  @SuppressWarnings("rawtypes") List params) {
 		
 		if (options == null) return;
 		
 		String filter = filterable.findFilterableProperty(options.getFilter(), options.getFilterOperation());
 		if (FilterSpecification.isValid(filter)) {
 			if (FilterSpecification.isSpecial(filter)) {
 				String filterValue = JPQLUtils.saferFilterValue(options.getFilterValue());
 				if (Membership.TYPE_FILTER.equals(options.getFilter())) {
 					Type t = Type.valueOf(filterValue);
 					sb.append(" and m.type=")
 					  .append(t.getClass().getCanonicalName())
 					  .append(".")
 					  .append(t.name());
 				}
 			} else {
 				JPQLUtils.addFilterClause(sb, filterable, options, params);
 			}
 		}
 	}
 }
