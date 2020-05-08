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
 
 import org.chai.kevin.location.CalculationEntity;
 import org.chai.kevin.location.DataEntityType;
 import org.chai.kevin.location.LocationEntity;
 import org.chai.kevin.location.LocationLevel;
 import org.chai.kevin.reports.ReportProgram;
 import org.chai.kevin.reports.ReportService;
 import org.hibernate.SessionFactory;
 import org.hibernate.criterion.Restrictions;
 import org.hisp.dhis.period.Period;
 import org.springframework.transaction.annotation.Transactional;
 
 public class DashboardService {
 
 //	private Log log = LogFactory.getLog(DashboardService.class);
 	
 	private ReportService reportService;
 	private SessionFactory sessionFactory;
 	private Set<String> skipLevels;
 	private DashboardPercentageService dashboardPercentageService;	
 	
 	@Transactional(readOnly = true)
 	public Dashboard getProgramDashboard(LocationEntity location, ReportProgram program, Period period, Set<DataEntityType> types){
 
 		List<CalculationEntity> locationEntities = new ArrayList<CalculationEntity>();		
 		locationEntities.add(location);
 
 		List<DashboardEntity> dashboardEntities = getDashboardEntitiesWithTargets(program);				
 		
 		List<LocationEntity> locationPath = new ArrayList<LocationEntity>();
 		Map<CalculationEntity, Map<DashboardEntity, DashboardPercentage>> valueMap = 
 				new HashMap<CalculationEntity, Map<DashboardEntity, DashboardPercentage>>();
 		
 		if(dashboardEntities.isEmpty())
 			return new Dashboard(locationEntities, dashboardEntities, locationPath, valueMap);
 		
 		locationPath = calculateLocationPath(location);
 		valueMap = getValues(locationEntities, dashboardEntities, period, types);
 		
 		return new Dashboard(locationEntities, dashboardEntities, locationPath, valueMap);
 	}
 			
 	@Transactional(readOnly = true)
 	public Dashboard getLocationDashboard(LocationEntity location, ReportProgram program, Period period, Set<DataEntityType> types, boolean compare) {
 		
 		List<CalculationEntity> locationEntities = new ArrayList<CalculationEntity>();
 		if(compare) 
 			locationEntities.add(location);
 		else {
 			Set<LocationLevel> skipLevels = getSkipLocationLevels();
 			locationEntities.addAll(location.getChildrenEntitiesWithDataEntities(skipLevels, types));			
 		}
 		
 		List<DashboardEntity> dashboardEntities = new ArrayList<DashboardEntity>();		
 		dashboardEntities.add(getDashboardProgram(program));		
 		
 		List<LocationEntity> locationPath = new ArrayList<LocationEntity>();
 		Map<CalculationEntity, Map<DashboardEntity, DashboardPercentage>> valueMap = 
 				new HashMap<CalculationEntity, Map<DashboardEntity, DashboardPercentage>>();
 		
 		if(locationEntities.isEmpty())
 			return new Dashboard(locationEntities, dashboardEntities, locationPath, valueMap);
 		
 		locationPath = calculateLocationPath(location);
 		valueMap = getValues(locationEntities, dashboardEntities, period, types);
 		
 		return new Dashboard(locationEntities, dashboardEntities, locationPath, valueMap);
 	}
 	
 	private Map<CalculationEntity, Map<DashboardEntity, DashboardPercentage>> getValues(List<CalculationEntity> locations, List<DashboardEntity> dashboardEntities, Period period, Set<DataEntityType> types) {
 		Map<CalculationEntity, Map<DashboardEntity, DashboardPercentage>> valueMap = new HashMap<CalculationEntity, Map<DashboardEntity, DashboardPercentage>>();
 
 		for (CalculationEntity location : locations) {
 			Map<DashboardEntity, DashboardPercentage> locationMap = getValues(dashboardEntities, period, location, types);
 			valueMap.put(location, locationMap);
 		}
 		return valueMap;
 	}
 
 	private Map<DashboardEntity, DashboardPercentage> getValues(List<DashboardEntity> dashboardEntities, Period period, CalculationEntity location, Set<DataEntityType> types) {
 		Map<DashboardEntity, DashboardPercentage> entityMap = new HashMap<DashboardEntity, DashboardPercentage>();
 		for (DashboardEntity dashboardEntity : dashboardEntities) {
 			DashboardPercentage percentage = dashboardPercentageService.getDashboardValue(period, location, types, dashboardEntity);
 			entityMap.put(dashboardEntity, percentage);
 		}
 		return entityMap;
 	}
 
 	private List<LocationEntity> calculateLocationPath(LocationEntity entity) {
 		List<LocationEntity> locationPath = new ArrayList<LocationEntity>();
 		LocationEntity parent = entity;
 		while ((parent = parent.getParent()) != null) {
 			locationPath.add(parent);
 		}
 		Collections.reverse(locationPath);
 		return locationPath;
 	}
 	
 //	private List<DashboardProgram> getBreadcrumb(ReportProgram program) {
 //		List<DashboardProgram> programPath = new ArrayList<DashboardProgram>();
 //		while (program != null) {
 //			ReportProgram parent = program.getParent();
 //			if(parent != null) {
 //				DashboardProgram dashboardParent = (DashboardProgram) getDashboardProgram(parent);
 //				if(dashboardParent != null)	programPath.add(dashboardParent);
 //			}
 //			program = parent;
 //		}
 //		Collections.reverse(programPath);
 //		return programPath;
 //	}
 
 	//gets all dashboard program children and dashboard program targets
 	public List<DashboardEntity> getDashboardEntities(ReportProgram program) {		
		List<DashboardEntity> entities = new ArrayList<DashboardEntity>();				
		
 		List<DashboardEntity> dashboardChildren = getDashboardChildren(program);
 		entities.addAll(dashboardChildren);
		List<DashboardEntity> dashboardProgramTree = getDashboardProgramTree();
 		List<DashboardEntity> dashboardTargets = getDashboardTargets(program);
 		entities.addAll(dashboardTargets);
 		return entities;
 	}
 	
 	//gets all dashboard program children and dashboard program targets (that have dashboard targets)
 	public List<DashboardEntity> getDashboardEntitiesWithTargets(ReportProgram program) {
		List<DashboardEntity> dashboardEntities = getDashboardEntities(program);
		
 		List<DashboardEntity> dashboardProgramTree = getDashboardProgramTree();
 		List<DashboardEntity> dashboardTargets = getDashboardTargets(program);
		for(DashboardEntity dashboardEntity : dashboardEntities)
			if(!dashboardProgramTree.contains(dashboardEntity) && 
					!dashboardTargets.contains(dashboardEntity))
				dashboardEntities.remove(dashboardEntity);
				
		return dashboardEntities;
 	}
 	
 	//gets all dashboard program children
 	public List<DashboardEntity> getDashboardChildren(ReportProgram program){
 		List<DashboardEntity> result = new ArrayList<DashboardEntity>();
 		List<ReportProgram> children = program.getChildren();
 		if(children != null) {
 			for (ReportProgram child : children) {
 				DashboardEntity dashboardProgram = getDashboardProgram(child);
 				if(dashboardProgram != null)	
 					result.add(dashboardProgram);
 			}
 		}
 		return result;
 	}
 	
 	//gets all dashboard program targets
 	public List<DashboardEntity> getDashboardTargets(ReportProgram program){
 		List<DashboardEntity> entities = new ArrayList<DashboardEntity>();		
 		List<DashboardTarget> dashboardTargets = reportService.getReportTargets(DashboardTarget.class, program);		
 		entities.addAll(dashboardTargets);
 		return entities;
 	}
 	
 	//gets all dashboard program children, grandchildren, etc (that have dashboard targets)
 	public List<DashboardEntity> getDashboardProgramTree(){
 		List<ReportProgram> programTree = reportService.getProgramTree(DashboardTarget.class);
 		List<DashboardEntity> dashboardProgramTree = new ArrayList<DashboardEntity>();
 		for(ReportProgram program : programTree){
 			DashboardEntity dashboardProgram = getDashboardProgram(program);
 			if(dashboardProgram != null)
 				dashboardProgramTree.add(getDashboardProgram(program));
 		}		
 		return dashboardProgramTree;
 	}
 	
 	public DashboardEntity getDashboardProgram(ReportProgram program) {
 		DashboardProgram dashboardProgram = (DashboardProgram) sessionFactory.getCurrentSession()
 				.createCriteria(DashboardProgram.class)
 				.add(Restrictions.eq("program", program))
 				.uniqueResult();
 		
 		return dashboardProgram;
 	}
 	
 	public Set<LocationLevel> getSkipLocationLevels(){
 		return reportService.getSkipLocationLevels(skipLevels);
 	}
 	
 	public void setSessionFactory(SessionFactory sessionFactory) {
 		this.sessionFactory = sessionFactory;
 	}
 	
 	public void setSkipLevels(Set<String> skipLevels) {
 		this.skipLevels = skipLevels;
 	}
 	
 	public void setDashboardPercentageService(DashboardPercentageService dashboardPercentageService) {
 		this.dashboardPercentageService = dashboardPercentageService;
 	}
 	
 	public void setReportService(ReportService reportService) {
 		this.reportService = reportService;
 	}
 }
