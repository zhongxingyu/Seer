 /*
  * Copyright (c) 2009. Orange Leap Inc. Active Constituent
  * Relationship Management Platform.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.orangeleap.tangerine.service.impl;
 
 import javax.annotation.Resource;
 
 import org.apache.commons.logging.Log;
import com.orangeleap.tangerine.util.OLLogger;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Propagation;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.orangeleap.tangerine.dao.ErrorLogDao;
 import com.orangeleap.tangerine.service.ErrorLogService;
 import com.orangeleap.tangerine.util.TangerineUserHelper;
import com.orangeleap.tangerine.domain.ErrorLog;
 import com.orangeleap.tangerine.web.common.PaginatedResult;
 import com.orangeleap.tangerine.web.common.SortInfo;
 
import java.util.List;

 @Service("errorLogService")
 @Transactional(propagation = Propagation.REQUIRED)
 public class ErrorLogServiceImpl extends AbstractTangerineService implements ErrorLogService {
 
     /** Logger for this class and subclasses */
    protected final Log logger = OLLogger.getLog(getClass());
 
     @Resource(name = "errorLogDAO")
     private ErrorLogDao errorLogDao;
     
     @Resource(name="tangerineUserHelper")
     protected TangerineUserHelper tangerineUserHelper;
 
     @Override
     @Transactional(propagation = Propagation.REQUIRES_NEW)
 	public void addErrorMessage(String message, String context) {
     	try {
         	Long constituentId = tangerineUserHelper.lookupUserId();
     		errorLogDao.addErrorMessage(message, context, constituentId);
     	} catch (Exception e) {
    		logger.error("Unable to log error message to ERROR_LOG: "+message, e);
     	}
     }
 
     @Override
     public PaginatedResult readErrorMessages(SortInfo sortInfo) {
     	PaginatedResult result = errorLogDao.readErrorMessages(sortInfo.getSort(), sortInfo.getDir(),
                 sortInfo.getStart(), sortInfo.getLimit());
         return result;
     }
 
     
 }
