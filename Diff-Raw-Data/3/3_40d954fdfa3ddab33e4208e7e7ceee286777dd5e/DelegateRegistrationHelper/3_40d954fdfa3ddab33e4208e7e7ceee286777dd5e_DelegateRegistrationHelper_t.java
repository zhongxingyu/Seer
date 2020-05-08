 /**
  * Licensed to Jasig under one or more contributor license
  * agreements. See the NOTICE file distributed with this work
  * for additional information regarding copyright ownership.
  * Jasig licenses this file to you under the Apache License,
  * Version 2.0 (the "License"); you may not use this file
  * except in compliance with the License. You may obtain a
  * copy of the License at:
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on
  * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 
 
 package org.jasig.schedassist.web.register.delegate;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Set;
 
 import org.apache.commons.lang.StringUtils;
 import org.jasig.schedassist.impl.AvailableScheduleReflectionService;
 import org.jasig.schedassist.impl.owner.AvailableScheduleDao;
 import org.jasig.schedassist.impl.owner.IneligibleException;
 import org.jasig.schedassist.impl.owner.OwnerDao;
 import org.jasig.schedassist.model.AvailableBlock;
 import org.jasig.schedassist.model.AvailableBlockBuilder;
 import org.jasig.schedassist.model.IDelegateCalendarAccount;
 import org.jasig.schedassist.model.IScheduleOwner;
 import org.jasig.schedassist.model.InputFormatException;
 import org.jasig.schedassist.model.Preferences;
 import org.jasig.schedassist.web.register.Registration;
 import org.jasig.schedassist.web.security.DelegateCalendarAccountUserDetailsImpl;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.context.SecurityContext;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.stereotype.Service;
 
 /**
  * Helper class used in the delegate-register webflow.
  *  
  * @author Nicholas Blair, nblair@doit.wisc.edu
  * @version $Id: DelegateRegistrationHelper.java 2695 2010-09-24 13:20:05Z npblair $
  */
 @Service("delegateRegistrationHelper")
 public class DelegateRegistrationHelper {
 
 	private AvailableScheduleDao availableScheduleDao;
 	private OwnerDao ownerDao;
 	private AvailableScheduleReflectionService reflectionService;
 	
 	/**
 	 * @param availableScheduleDao the availableScheduleDao to set
 	 */
 	@Autowired
 	public void setAvailableScheduleDao(AvailableScheduleDao availableScheduleDao) {
 		this.availableScheduleDao = availableScheduleDao;
 	}
 	/**
 	 * @param ownerDao the ownerDao to set
 	 */
 	@Autowired
 	public void setOwnerDao(OwnerDao ownerDao) {
 		this.ownerDao = ownerDao;
 	}
 	/**
 	 * @param reflectionService the reflectionService to set
 	 */
 	@Autowired
 	public void setReflectionService(
 			AvailableScheduleReflectionService reflectionService) {
 		this.reflectionService = reflectionService;
 	}
 	/**
 	 * 
 	 * @param registration
 	 * @throws IneligibleException
 	 * @throws InputFormatException
 	 * @throws ParseException
 	 */
 	public void executeDelegateRegistration(final Registration registration) throws IneligibleException, InputFormatException, ParseException {
 		SecurityContext context = SecurityContextHolder.getContext();
 		Authentication authentication = context.getAuthentication();
 		DelegateCalendarAccountUserDetailsImpl currentUser = (DelegateCalendarAccountUserDetailsImpl) authentication.getPrincipal();
 		IScheduleOwner delegateOwner = ownerDao.register(currentUser.getCalendarAccount());
 		delegateOwner = ownerDao.updatePreference(delegateOwner, Preferences.LOCATION, registration.getLocation());
 		
 		delegateOwner = ownerDao.updatePreference(delegateOwner, Preferences.DURATIONS, registration.durationPreferenceValue());
 		delegateOwner = ownerDao.updatePreference(delegateOwner, Preferences.MEETING_PREFIX, registration.getTitlePrefix());
 		delegateOwner = ownerDao.updatePreference(delegateOwner, Preferences.NOTEBOARD, registration.getNoteboard());
 		delegateOwner = ownerDao.updatePreference(delegateOwner, Preferences.VISIBLE_WINDOW, registration.visibleWindowPreferenceKey());
 		delegateOwner = ownerDao.updatePreference(delegateOwner, Preferences.DEFAULT_VISITOR_LIMIT, Integer.toString(registration.getDefaultVisitorsPerAppointment()));
 		delegateOwner = ownerDao.updatePreference(delegateOwner, Preferences.MEETING_LIMIT, Integer.toString(registration.getMeetingLimitValue()));
 		delegateOwner = ownerDao.updatePreference(delegateOwner, Preferences.REFLECT_SCHEDULE, Boolean.toString(registration.isReflectSchedule()));
 		delegateOwner = ownerDao.updatePreference(delegateOwner, Preferences.REMINDERS, registration.emailReminderPreferenceKey());
 		
 		if(registration.isScheduleSet()) {
 			SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
 			Set<AvailableBlock> blocks = AvailableBlockBuilder.createBlocks(registration.getStartTimePhrase(), 
 					registration.getEndTimePhrase(),
 					registration.getDaysOfWeekPhrase(),
 					dateFormat.parse(registration.getStartDatePhrase()),
 					dateFormat.parse(registration.getEndDatePhrase()),
 					registration.getDefaultVisitorsPerAppointment());
 			availableScheduleDao.addToSchedule(delegateOwner, blocks);
 		}
 		
 		if(registration.isReflectSchedule()) {
 			reflectionService.reflectAvailableSchedule(delegateOwner);
 		}
 	}
 
 	/**
 	 * 
 	 * @return
 	 */
 	public String currentDelegateUsername() {
 		SecurityContext context = SecurityContextHolder.getContext();
 		Authentication authentication = context.getAuthentication();
 		DelegateCalendarAccountUserDetailsImpl currentUser = (DelegateCalendarAccountUserDetailsImpl) authentication.getPrincipal();
 		return currentUser.getUsername();
 	}
 	
 	/**
 	 * Returns the value of the name field if {@link IDelegateCalendarAccount#getLocation()} does not return null; otherwise
 	 * returns the default value of {@link Preferences#LOCATION}.
 	 * 
 	 * @return a {@link String} containing the current authenticated {@link IDelegateCalendarAccount} location name
 	 */
 	public String currentDelegateLocation() {
 		SecurityContext context = SecurityContextHolder.getContext();
 		Authentication authentication = context.getAuthentication();
 		DelegateCalendarAccountUserDetailsImpl currentUser = (DelegateCalendarAccountUserDetailsImpl) authentication.getPrincipal();
 		String accountLocation = currentUser.getDelegateCalendarAccount().getLocation();
 		if(StringUtils.isNotBlank(accountLocation)) {
 			return accountLocation;
 		} else {
 			return Preferences.LOCATION.getDefaultValue();
 		}
 	}
 	
 	/**
 	 * 
 	 * @return true if the current authenticated delegate has ineligible for service
 	 */
 	public boolean currentDelegateIsIneligible() {
 		SecurityContext context = SecurityContextHolder.getContext();
 		Authentication authentication = context.getAuthentication();
 		DelegateCalendarAccountUserDetailsImpl currentUser = (DelegateCalendarAccountUserDetailsImpl) authentication.getPrincipal();
		boolean result = !currentUser.isEnabled();
		return result;
 	}
 }
