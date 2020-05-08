 package com.tvo.service;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.tvo.dao.StrandsScheduleDaoImpl;
 import com.tvo.entity.StrandsScheduleView;
 
 @Service
 @Transactional
 public class StrandsScheduleViewServiceImpl implements StrandsScheduleViewService {
 	
 	@Autowired
 	private StrandsScheduleDaoImpl strandScheduleDao;
 	
 	public StrandsScheduleView[] getSchedule() {
 		return strandScheduleDao.getStrandsScheduleView();
 	}
 	
 	public void update(StrandsScheduleView[] view) {
 		strandScheduleDao.update(view);
 	}
}
