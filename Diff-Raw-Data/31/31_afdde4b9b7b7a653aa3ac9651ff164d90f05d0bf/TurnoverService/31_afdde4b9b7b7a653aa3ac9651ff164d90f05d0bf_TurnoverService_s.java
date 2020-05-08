 package com.octo.red.newsql.services;
 
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.octo.red.newsql.dao.SaleOperationRepository;
import com.octo.red.newsql.model.TurnoverVo;

 @Service
 public class TurnoverService {
 	
 	@Autowired
 	private SaleOperationRepository saleOperationRepository;
 	
 	//For test purpose only
 	private Date actualTestDate;
 	
 	void setActualTestDate(Date date) {
 		actualTestDate = date;
 	}
 	
 	/**
 	 * Compute the turnover of the group for the previous month
 	 * @param groupId
 	 * @return
 	 */
 	public List<TurnoverVo> computeTurnover(int groupId) {
 		Date actualDate = actualTestDate == null ? new Date() : actualTestDate;
 		Calendar calendar = Calendar.getInstance();
 		calendar.setTime(actualDate);
 		calendar.add(Calendar.HOUR, -1);
 		Date minDate = calendar.getTime();
		List<TurnoverVo> result = new ArrayList<TurnoverVo>();
 		List<Object[]> lines = saleOperationRepository.aggregateAmount(groupId, minDate);
		if(lines != null) {
			for(Object[] l : lines) {
				result.add(new TurnoverVo(groupId, (BigDecimal)l[0], (String)l[1]));
			}
		}
 		return result;
 	}
 }
