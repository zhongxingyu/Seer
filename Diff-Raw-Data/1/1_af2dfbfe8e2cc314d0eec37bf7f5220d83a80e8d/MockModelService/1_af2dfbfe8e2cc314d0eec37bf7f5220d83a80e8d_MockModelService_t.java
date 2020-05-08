 package com.tradespeople.service;
 
 import java.util.List;
 
 import com.tradespeople.common.exception.TradesPeopleDaoException;
 import com.tradespeople.common.exception.TradesPeopleServiceException;
 import com.tradespeople.dao.IMockModelHibernateDao;
 import com.tradespeople.dao.MockMoelDao;
 import com.tradespeople.model.MockModel;
 
 public class MockModelService implements IMockModelService {
 
 	IMockModelHibernateDao mockModelHibernateDao;
 	
 	public MockModelService(){
		mockModelHibernateDao=new MockMoelDao();
 	}
 	
 	
 	public List<MockModel> getMockModels() throws TradesPeopleServiceException{
 		try {
 			return mockModelHibernateDao.getMockModels();
 		} catch (TradesPeopleDaoException e) {
 			throw new TradesPeopleServiceException(e);
 		}
 	}
 
 	
 	
 }
