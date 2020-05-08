 package library.seat.manage.service.impl;
 
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.google.inject.Inject;
 
 import library.seat.manage.dao.IOrderDao;
 import library.seat.manage.dto.OrdersInfo;
 import library.seat.manage.dto.PageInfo;
 import library.seat.manage.exception.BusinessException;
 import library.seat.manage.exception.DataAccessException;
 import library.seat.manage.service.IOrderService;
 import library.seat.manage.util.FieldValueCriteria;
 import library.seat.manage.util.Operator;
 
 /**
  * 
  * @author mahs
  * @version 1.0
  * @created 1-1-2013 22:50:00
  */
 public class OrderServiceImpl implements IOrderService {
 	
 	@Inject
 	private IOrderDao orderDao;
 
 	@Override
 	public PageInfo<OrdersInfo> queryByConditon(int deskId, int userId,
 			Timestamp reserveBeginTime, Timestamp reserveEndTime,
 			PageInfo pageInfo) {
 		try {
 			List<FieldValueCriteria> criteria = new ArrayList<FieldValueCriteria>();
 			
 			if(deskId != 0) {
 				criteria.add(new FieldValueCriteria("DESK_ID", deskId));
 			}
 			if(userId != 0) {
 				criteria.add(new FieldValueCriteria("USER_ID", userId));
 			}
 			if(reserveBeginTime != null) {
 				criteria.add(new FieldValueCriteria("RESERVE_BEGIN_TIME",Operator.GE, reserveBeginTime));
 			}
 			if(reserveEndTime != null) {
 				criteria.add(new FieldValueCriteria("RESERVE_END_TIME",Operator.GE, reserveEndTime));
 			}
 			
 			pageInfo.setTotalCount(orderDao.getCount(criteria));
 			pageInfo = orderDao.findByCriteria(criteria, pageInfo);
 			return pageInfo;
 		} catch (DataAccessException e) {
             throw new BusinessException("error_query_rec_fail", e);
         }
 	}
 	
 	@Override
 	public List<OrdersInfo> queryOrders(int deskId, int userId,
 			Timestamp reserveBeginTime, Timestamp reserveEndTime) {
 		try {
 			List<FieldValueCriteria> criteria = new ArrayList<FieldValueCriteria>();
 			
 			if(deskId != 0) {
 				criteria.add(new FieldValueCriteria("DESK_ID", deskId));
 			}
 			if(userId != 0) {
 				criteria.add(new FieldValueCriteria("USER_ID", userId));
 			}
 			if(reserveBeginTime != null) {
 				criteria.add(new FieldValueCriteria("RESERVE_BEGIN_TIME",Operator.GE, reserveBeginTime));
 			}
 			if(reserveEndTime != null) {
 				criteria.add(new FieldValueCriteria("RESERVE_END_TIME",Operator.LE, reserveEndTime));
 			}
 			
 			return orderDao.findByCriteria(criteria);
 		} catch (DataAccessException e) {
             throw new BusinessException("error_query_rec_fail", e);
         }
 	}
 
 	@Override
 	public OrdersInfo getOrderById(int orderId) {
		return null;
		//return orderDao.;
 	}
 
 	public IOrderDao getOrderDao() {
 		return orderDao;
 	}
 
 	public void setOrderDao(IOrderDao orderDao) {
 		this.orderDao = orderDao;
 	}
 
 	
 
 }
