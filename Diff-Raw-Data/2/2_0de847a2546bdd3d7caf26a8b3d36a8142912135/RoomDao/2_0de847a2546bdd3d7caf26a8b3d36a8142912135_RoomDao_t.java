 package com.xlthotel.core.admin.orm.dao;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import org.hibernate.HibernateException;
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.springframework.orm.hibernate3.HibernateCallback;
 import org.springframework.stereotype.Repository;
 
 import com.google.common.collect.Lists;
 import com.google.common.collect.Sets;
 import com.xlthotel.foundation.common.Condition;
 import com.xlthotel.foundation.common.PageOrder;
 import com.xlthotel.foundation.common.Page;
 import com.xlthotel.foundation.common.Condition.ConditionEntry;
 import com.xlthotel.foundation.common.SimpleConditionImpl.BetweenEntry;
 import com.xlthotel.foundation.orm.dao.BaseDao;
 import com.xlthotel.foundation.orm.entity.Room;
 
 @Repository
 public class RoomDao extends BaseDao {
 	public List<Room> getRoomByConditions(Page page, Condition condition,
 			PageOrder order) {
 		final Page queryPage = page;
 		final Condition queryConditions = condition;
 		final PageOrder queryOrder = order;
 		final String hql = condition != null ? condition.getQL()
 				: " from Room ";
 
 		Map<Integer, List<Room>> result = getHibernateTemplate().execute(
 				new HibernateCallback<Map<Integer, List<Room>>>() {
 					@Override
 					public Map<Integer, List<Room>> doInHibernate(
 							Session session) throws HibernateException,
 							SQLException {
 						Map<Integer, List<Room>> resultList = new HashMap<Integer, List<Room>>();
 						// search list
 						Query query = session.createQuery(hql
 								+ (queryOrder != null ? queryOrder.getQL() : ""));
 						if (queryConditions != null) {
 							for (Entry<String, ConditionEntry> entry : queryConditions
 									.getConditionMap().entrySet()) {
 								ConditionEntry conditionEntry = entry
 										.getValue();
 								if (conditionEntry instanceof BetweenEntry) {
 									Object[] paramValue = (Object[]) conditionEntry
 											.getValue();
 									query.setParameter(
 											"min" + conditionEntry.getKey(),
 											paramValue[0]);
 									query.setParameter(
 											"max" + conditionEntry.getKey(),
 											paramValue[1]);
 								} else {
 									query.setParameter(conditionEntry.getKey(),
 											conditionEntry.getValue());
 								}
 							}
 						}
 						if (queryPage != null) {
 							query.setMaxResults(queryPage.getCount());
 							query.setFirstResult(queryPage.getIndex()
 									* queryPage.getCount());
 						}
 
 						// calculate size
 						Query sizeQuery = session
 								.createQuery("select count(id) " + hql);
 						if (queryConditions != null) {
 							for (Entry<String, ConditionEntry> entry : queryConditions
 									.getConditionMap().entrySet()) {
 								ConditionEntry conditionEntry = entry
 										.getValue();
 								if (conditionEntry instanceof BetweenEntry) {
 									Object[] paramValue = (Object[]) conditionEntry
 											.getValue();
 									sizeQuery.setParameter("min"
 											+ conditionEntry.getKey(),
 											paramValue[0]);
 									sizeQuery.setParameter("max"
 											+ conditionEntry.getKey(),
 											paramValue[1]);
 								} else {
 									sizeQuery.setParameter(
 											conditionEntry.getKey(),
 											conditionEntry.getValue());
 								}
 							}
 						}
 						Long size = (Long) sizeQuery.uniqueResult();
 						resultList.put(Integer.valueOf(String.valueOf(size)),
 								(List<Room>) query.list());
 						return resultList;
 					}
 				});
 		List<Room> forReturn = new ArrayList<Room>();
 		for (Entry<Integer, List<Room>> entry : result.entrySet()) {
 			if (page != null) {
 				page.setTotalCount(entry.getKey());
 			}
 			forReturn = entry.getValue();
 		}
 		return forReturn;
 	}
 
 	public void save(Room room) {
 		getHibernateTemplate().save(room);
 	}
 
 	public Room find(String id) {
 		return (Room) getHibernateTemplate().find(
 				"from Room where id = '" + id + "'").get(0);
 	}
 
 	public void update(Room room) {
 		getHibernateTemplate().update(room);
 	}
 
 	public void delete(String id) {
 		Room room = find(id);
 		getHibernateTemplate().delete(room);
 	}
 
 	public List<Room> findAvailableRooms(final String hotelId, final String roomTypeId) {
 		return getHibernateTemplate().executeFind(new HibernateCallback<List<Room>>() {
 			@Override
 			public List<Room> doInHibernate(Session session)
 					throws HibernateException, SQLException {
 				Query query = session.createQuery("select distinct room from Room as room " +
 						" left join room.ordered as ordRoom " +
 						" where room.hotel.id = :hotelId " +
 						"  and room.roomType.id = :roomTypeId" +
 						"  and (ordRoom is null or ordRoom.order.status = 3)");
 				query.setParameter("hotelId", hotelId);
 				query.setParameter("roomTypeId", roomTypeId);
 				return query.list();
 			}
 		});
 	}
 	
 	public void updateRoomTypeStatus(final List<String> targetIds, final int status) {
 		getHibernateTemplate().execute(new HibernateCallback<Object>() {
 			@Override
 			public Object doInHibernate(Session session)
 					throws HibernateException, SQLException {
 				Query query = session.createSQLQuery("update xlt_room set status = :status where id in (:id)");
 				query.setParameter("status", status);
 				query.setParameterList("id", targetIds);
 				query.executeUpdate();
 				return null;
 			}
 		});
 	}
 	
 	@SuppressWarnings("unchecked")
 	public List<Room> getAvailableRoom(final List<Date> orderDateList, final String hotelId, final String roomTypeId) {
 		List<Room> rooms = getHibernateTemplate().executeFind(new HibernateCallback<List<Room>>() {
 			@Override
 			public List<Room> doInHibernate(Session session)
 					throws HibernateException, SQLException {
 				Query query = session.createQuery(
 						" select distinct r from Room as r where " +
 						" r.hotel.id = :hotelId " +
 						" and r.roomType.id = :roomTypeId" +
 						" and r.id not in " +
 						" (select oroom.room.id from OrderRoom oroom " +
 						" where oroom.room.hotel.id = :hotelId " +
 						" and oroom.room.roomType.id = :roomTypeId " +
 						" and oroom.orderDate in (:orderDateList)" +
						" and oroom.order.status != 3) and r.status !=0 " +
 						" order by number ");
 				query.setParameter("hotelId", hotelId);
 				query.setParameter("roomTypeId", roomTypeId);
 				query.setParameterList("orderDateList", orderDateList);
 				return query.list();
 			}
 		});
 		return rooms;
 	}
 	
 	public long getAvailableRoomCount(final List<Date> orderDateList, final String hotelId, final String roomTypeId) {
 		return getHibernateTemplate().execute(new HibernateCallback<Long>() {
 			@Override
 			public Long doInHibernate(Session session)
 					throws HibernateException, SQLException {
 				Query query = session.createQuery(
 						" select count(*) from Room where id not in " +
 						" (select oroom.room.id from OrderRoom oroom " +
 						" where oroom.room.hotel.id = :hotelId " +
 						" and oroom.room.roomType.id = :roomTypeId " +
 						" and oroom.orderDate in (:orderDateList)) ");
 				query.setParameter("hotelId", hotelId);
 				query.setParameter("roomTypeId", roomTypeId);
 				query.setParameterList("orderDateList", orderDateList);
 				return (Long) query.uniqueResult();
 			}
 		});
 	}
 	
 	public List<Room> findRoomByIds(final List<String> roomIds) {
 		return getHibernateTemplate().executeFind(new HibernateCallback<List<Room>>() {
 			@Override
 			public List<Room> doInHibernate(Session session) throws HibernateException,
 					SQLException {
 				Query query = session.createQuery("from Room where id in (:roomIds)");
 				query.setParameterList("roomIds", roomIds);
 				return query.list();
 			}
 		});
 	}
 }
