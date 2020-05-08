 package org.chai.kevin.dashboard;
 
 /* 
  * Copyright (c) 2011, Clinton Health Access Initiative.
  *
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *     * Redistributions of source code must retain the above copyright
  *       notice, this list of conditions and the following disclaimer.
  *     * Redistributions in binary form must reproduce the above copyright
  *       notice, this list of conditions and the following disclaimer in the
  *       documentation and/or other materials provided with the distribution.
  *     * Neither the name of the <organization> nor the
  *       names of its contributors may be used to endorse or promote products
  *       derived from this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
  * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.chai.kevin.LanguageService;
 import org.chai.kevin.Period;
 import org.chai.kevin.location.CalculationLocation;
 import org.chai.kevin.location.DataLocationType;
 import org.chai.kevin.location.Location;
 import org.chai.kevin.location.LocationLevel;
 import org.chai.kevin.reports.ReportProgram;
 import org.chai.kevin.reports.ReportService;
 import org.chai.kevin.value.Value;
 import org.hibernate.SessionFactory;
 import org.hibernate.criterion.Restrictions;
 import org.springframework.transaction.annotation.Transactional;
 
 public class DashboardService {
 
 //	private Log log = LogFactory.getLog(DashboardService.class);
 	
 	private ReportService reportService;
 	private LanguageService languageService;
 	private SessionFactory sessionFactory;
 	private Set<String> locationSkipLevels;
 	private DashboardValueService dashboardPercentageService;	
 	
 	@Transactional(readOnly = true)
 	public Dashboard getProgramDashboard(Location location, ReportProgram program, Period period, Set<DataLocationType> types){
 
 		List<CalculationLocation> locations = new ArrayList<CalculationLocation>();		
 		locations.add(location);
 
 		List<DashboardEntity> dashboardEntities = collectDashboardEntitiesWithTargets(program);
 		
 		List<Location> locationPath = new ArrayList<Location>();
 		Map<CalculationLocation, Map<DashboardEntity, Value>> valueMap = 
 				new HashMap<CalculationLocation, Map<DashboardEntity, Value>>();
 		
 		if(dashboardEntities.isEmpty())
 			return new Dashboard(locations, dashboardEntities, locationPath, valueMap);
 		Collections.sort(dashboardEntities, DashboardEntitySorter.BY_ENTITY());
 		
 		locationPath = calculateLocationPath(location);
 		
 		valueMap = getValues(locations, dashboardEntities, period, types);
 		
 		return new Dashboard(locations, dashboardEntities, locationPath, valueMap);
 	}
 			
 	@Transactional(readOnly = true)
 	public Dashboard getLocationDashboard(Location location, ReportProgram program, Period period, Set<DataLocationType> types, boolean compare) {
 		
 		List<CalculationLocation> calculationLocations = new ArrayList<CalculationLocation>();
 		if(compare) 
 			calculationLocations.add(location);
 		else {
 			Set<LocationLevel> skipLevels = getSkipLocationLevels();
 			calculationLocations.addAll(location.getChildrenWithData(skipLevels, types, true));			
 		}
 		
 		List<DashboardEntity> dashboardEntities = new ArrayList<DashboardEntity>();		
 		dashboardEntities.add(getDashboardProgram(program));		
 		
 		List<Location> locationPath = new ArrayList<Location>();
 		Map<CalculationLocation, Map<DashboardEntity, Value>> valueMap = 
 				new HashMap<CalculationLocation, Map<DashboardEntity, Value>>();
 		
 		if(calculationLocations.isEmpty())
 			return new Dashboard(calculationLocations, dashboardEntities, locationPath, valueMap);
 		
 		locationPath = calculateLocationPath(location);
 		valueMap = getValues(calculationLocations, dashboardEntities, period, types);
 		
 		return new Dashboard(calculationLocations, dashboardEntities, locationPath, valueMap);
 	}
 	
 	private Map<CalculationLocation, Map<DashboardEntity, Value>> getValues(List<CalculationLocation> locations, List<DashboardEntity> dashboardEntities, Period period, Set<DataLocationType> types) {
 		Map<CalculationLocation, Map<DashboardEntity, Value>> valueMap = new HashMap<CalculationLocation, Map<DashboardEntity, Value>>();
 
 		for (CalculationLocation location : locations) {
 			Map<DashboardEntity, Value> locationMap = getValues(dashboardEntities, period, location, types);
 			valueMap.put(location, locationMap);
 		}
 		return valueMap;
 	}
 
 	private Map<DashboardEntity, Value> getValues(List<DashboardEntity> dashboardEntities, Period period, CalculationLocation location, Set<DataLocationType> types) {
 		Map<DashboardEntity, Value> entityMap = new HashMap<DashboardEntity, Value>();
 		for (DashboardEntity dashboardEntity : dashboardEntities) {
 			Value percentage = dashboardPercentageService.getDashboardValue(period, location, types, dashboardEntity);
 			entityMap.put(dashboardEntity, percentage);
 		}
 		return entityMap;
 	}
 
 	private List<Location> calculateLocationPath(Location location) {
 		List<Location> locationPath = new ArrayList<Location>();
 		Location parent = location;
 		while ((parent = parent.getParent()) != null) {
 			locationPath.add(parent);
 		}
 		Collections.reverse(locationPath);
 		return locationPath;
 	}
 	
 	//gets all dashboard program children and dashboard program targets (that have dashboard targets)
 	private List<DashboardEntity> collectDashboardEntitiesWithTargets(ReportProgram program) {
 		List<DashboardEntity> allEntities = getDashboardEntities(program);
 		
 		List<ReportProgram> programTreeWithTargets = new ArrayList<ReportProgram>();
 		List<DashboardTarget> collectedTargets = new ArrayList<DashboardTarget>();
 		reportService.collectReportTree(DashboardTarget.class, program, programTreeWithTargets, collectedTargets);
 		
 		List<DashboardEntity> entityTreeWithTargets = new ArrayList<DashboardEntity>();
 		entityTreeWithTargets.addAll(collectedTargets);
 		for (ReportProgram reportProgram : programTreeWithTargets) {
 			entityTreeWithTargets.add(getDashboardProgram(reportProgram));
 		}
 		
 		allEntities.retainAll(entityTreeWithTargets);
 		return allEntities;
 	}
 	
 	//gets all dashboard program children and dashboard target children
 	protected List<DashboardEntity> getDashboardEntities(ReportProgram program) {		
 		List<DashboardEntity> entities = new ArrayList<DashboardEntity>();		
 		List<DashboardProgram> dashboardChildren = getDashboardProgramChildren(program);
 		entities.addAll(dashboardChildren);
 		List<DashboardTarget> dashboardTargets = reportService.getReportTargets(DashboardTarget.class, program);
 		entities.addAll(dashboardTargets);
 		return entities;
 	}
		
 	//gets all dashboard program children
 	private List<DashboardProgram> getDashboardProgramChildren(ReportProgram program){
 		List<DashboardProgram> result = new ArrayList<DashboardProgram>();
 		List<ReportProgram> children = program.getChildren();
 		for (ReportProgram child : children) {
 			DashboardProgram dashboardProgram = getDashboardProgram(child);
 			if(dashboardProgram != null)	
 				result.add(dashboardProgram);
 		}
 		return result;
 	}
 	
 	public DashboardProgram getDashboardProgram(ReportProgram program) {
 		DashboardProgram dashboardProgram = (DashboardProgram) sessionFactory.getCurrentSession()
 				.createCriteria(DashboardProgram.class)
 				.add(Restrictions.eq("program", program))
 				.setCacheable(true)
 				.uniqueResult();
 		return dashboardProgram;
 	}
 	
 	public Set<LocationLevel> getSkipLocationLevels(){
 		return reportService.getSkipReportLevels(locationSkipLevels);
 	}
 	
 	public void setSessionFactory(SessionFactory sessionFactory) {
 		this.sessionFactory = sessionFactory;
 	}
 	
 	public void setLocationSkipLevels(Set<String> locationSkipLevels) {
 		this.locationSkipLevels = locationSkipLevels;
 	}
 	
 	public void setDashboardPercentageService(DashboardValueService dashboardPercentageService) {
 		this.dashboardPercentageService = dashboardPercentageService;
 	}
 	
 	public void setReportService(ReportService reportService) {
 		this.reportService = reportService;
 	}
 	
 	public void setLanguageService(LanguageService languageService) {
 		this.languageService = languageService;
 	}
 }
