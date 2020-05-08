 package com.ffe.estimate.service.impl;
 
 import java.math.BigDecimal;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.drools.KnowledgeBase;
 import org.drools.runtime.StatefulKnowledgeSession;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.core.context.SecurityContextHolder;
 
 import com.ffe.common.exception.GTSException;
 import com.ffe.common.framework.model.UserProfile;
 import com.ffe.common.framework.util.ConstantUtil;
 import com.ffe.common.framework.util.StringUtil;
 import com.ffe.estimate.dao.FilmEstimateDAO;
 import com.ffe.estimate.model.CostCategoryEstimate;
 import com.ffe.estimate.model.EstimateByTerritory;
 import com.ffe.estimate.model.EstimateCosting;
 import com.ffe.estimate.model.FilmEstimate;
 import com.ffe.estimate.model.PrintCostEstimate;
 import com.ffe.estimate.model.ServiceTrailerWrapper;
 import com.ffe.estimate.model.TrailerSTWDisplay;
 import com.ffe.estimate.service.EstimateService;
 import com.ffe.estimate.service.FilmEstimateService;
 import com.ffe.process.task.model.FilmEstimateWorkFlow;
 import com.ffe.process.task.service.FFEHumanTaskService;
 import com.ffe.title.dao.TitleDAO;
 import com.ffe.title.message.SearchCriteria;
 import com.ffe.title.model.Title;
 import com.ffe.estimate.model.EstimateCommonTrailerInfo;
 import com.ffe.estimate.model.RegionInitiateEstimate;
 
 public class FilmEstimateServiceImpl implements FilmEstimateService {
 	
 
 
 	
 
 
 	private Logger logger= LoggerFactory.getLogger(FilmEstimateServiceImpl.class);
 
 
 	private FilmEstimateDAO filmEstimateDAO;
 	private TitleDAO titleDAO;
 	private StatefulKnowledgeSession statfulknowsession;
 	private FFEHumanTaskService ffeHumanTaskService;
 	private KnowledgeBase knowBase;
 	
 	public TitleDAO getTitleDAO() {
 		return titleDAO;
 	}
 
 
 	public void setTitleDAO(TitleDAO titleDAO) {
 		this.titleDAO = titleDAO;
 	}
 
 	public StatefulKnowledgeSession getStatfulknowsession() {
 		return statfulknowsession;
 	}
 
 
 	public void setStatfulknowsession(StatefulKnowledgeSession statfulknowsession) {
 		this.statfulknowsession = statfulknowsession;
 	}
 
 
 	public FFEHumanTaskService getFfeHumanTaskService() {
 		return ffeHumanTaskService;
 	}
 
 
 	public void setFfeHumanTaskService(FFEHumanTaskService ffeHumanTaskService) {
 		this.ffeHumanTaskService = ffeHumanTaskService;
 	}
 
 	
 	@Autowired
 	private EstimateService estimateService ;	
 	
 	public EstimateService getEstimateService() {
 		return estimateService;
 	}
 
 	public KnowledgeBase getKnowBase() {
 		return knowBase;
 	}
 
 
 	public void setKnowBase(KnowledgeBase knowBase) {
 		this.knowBase = knowBase;
 	}
 	
 	@Override
 	public FilmEstimate getFilmEstimate(long filmEstimateID)throws GTSException {
 		logger.debug("Entering the FilmEstimateServiceImpl.getFilmEstimate"+filmEstimateID);
 		FilmEstimate estimate = null;
 		try {
 			estimate = filmEstimateDAO.findById(filmEstimateID, false, null);
 		}catch (Exception ex) {
 			ex.printStackTrace();
 			logger.error(
 					" Exception occured in FilmEstimateServiceImpl.getFilmEstimate :",
 					ex.getMessage());
 			throw new GTSException(ex.getMessage(), ex.getCause());
 		}
 		logger.debug("Exiting the FilmEstimateServiceImpl.getFilmEstimate");
 		return estimate;
 	}
 	
 	
 	@Override
 	public FilmEstimate getFilmEstimateByRelease(long releaseId,long territoryId) throws GTSException {
 		logger.debug("Entering the FilmEstimateServiceImpl.getFilmEstimateByRelease"+releaseId);
 		FilmEstimate estimate = null;
 		try {
 			estimate=filmEstimateDAO.findByReleaseId(releaseId, false, null,territoryId);
 		}catch (Exception ex) {
 			logger.error(
 					" Exception occured in FilmEstimateServiceImpl.getFilmEstimateByRelease :",
 					ex.getMessage());
 			throw new GTSException(ex.getMessage(), ex.getCause());
 		}
 		logger.debug("Exiting the FilmEstimateServiceImpl.getFilmEstimateByRelease");
 		return estimate;
 	}
 	public FilmEstimateDAO getFilmEstimateDAO() {
 		return filmEstimateDAO;
 	}
 	public void setFilmEstimateDAO(FilmEstimateDAO filmEstimateDAO) {
 		this.filmEstimateDAO = filmEstimateDAO;
 	}
 
 
 
 	@Override
 	public FilmEstimate saveFilmEstimate(FilmEstimate estimate) throws GTSException {
 		logger.debug("Entering into FilmEstimateServiceImpl.saveEstimate"+estimate);
 		FilmEstimate estimateResult = null;
 		try {
 			
 			estimate = populateFilmEstimate(estimate);
 			logger.debug("Entering into FilmEstimateServiceImpl.saveEstimate"+estimate);
 			if(!(null!=estimate.getFilmEstimateId() && estimate.getFilmEstimateId().longValue()>0 )){
 				logger.debug("IF Condition satisfied------> FilmEstimateServiceImpl.saveEstimate"+estimate);
 				
 				estimate=populateSaveVersionDetails(estimate);
 				estimate = flushIds(estimate);
 			
 
 			}
 			List<CostCategoryEstimate> lstEstCostng = estimate.getLstPrintCostCategories();
 			logger.debug("lstEstCostng size"+lstEstCostng.size());
 			if (null!=lstEstCostng){
 				for (CostCategoryEstimate estCost:lstEstCostng){
 					logger.debug("CostCategoryEstimate"+estCost);
 					estCost.setFilmEstimate(estimate);
 					logger.debug("Afetr Seeting CostCategoryEstimate"+estCost);
 				}
 			}
 			
 			estimateResult = this.filmEstimateDAO.update(estimate,null);
 
 		}catch (Exception ex) {
 			logger.error(" Exception occured in FilmEstimateServiceImpl.saveEstimate :",
 					ex.getMessage());
 			ex.printStackTrace();
 			throw new GTSException(ex.getMessage(), ex.getCause());
 		}
 		logger.debug("Exiting into FilmEstimateServiceImpl.saveEstimate");
 		return estimateResult;
 	}
 
 
 
 	@Override
 	public FilmEstimate createNewVersion(FilmEstimate filmEstimate)
 			throws GTSException {
 		// TODO Auto-generated method stub
 		logger.debug("Entering into FilmEstimateServiceImpl.createNewVersion"+filmEstimate);
 		FilmEstimate estimateResult = null;
 		try {
 			
 			logger.debug("old Object------------------------------------------------>"+filmEstimate);
 			
 			filmEstimate = populateFilmEstimate(filmEstimate);	
 			Long old_filmEstimate_id = null;
 			if(null != filmEstimate){
 				old_filmEstimate_id =  filmEstimate.getFilmEstimateId();
 			}
 			
 			//New Logic Change
 			
 			if(filmEstimate.getEstimateType().equalsIgnoreCase(ConstantUtil.VERSION_TYPE_CURRENT)){
 				logger.debug("NEW LOGIC------------------------------------------------>VERSION IS CURRENT"+filmEstimate.getFilmEstimateId());
 				filmEstimate.setEstimateType(ConstantUtil.VERSION_TYPE_INTERIM);
 				List<CostCategoryEstimate> lstEstCostng = filmEstimate.getLstPrintCostCategories();
 				if (null!=lstEstCostng){
 					for (CostCategoryEstimate estCost:lstEstCostng){
 						estCost.setFilmEstimate(filmEstimate);
 					}
 				}
 				
 				filmEstimate = this.filmEstimateDAO.update(filmEstimate,null);
 				filmEstimate.setFilmEstimateId(null);
 				logger.debug("FILM ESTIMATE ID SET AS NULL------------------------------------------------>"+filmEstimate.getFilmEstimateId());
 				
 			}
 			
 			filmEstimate = populateCreateVersionDetails(filmEstimate);
 			
 			List<CostCategoryEstimate> lstEstCostng = filmEstimate.getLstPrintCostCategories();
 			if (null!=lstEstCostng && lstEstCostng.size()>0 ){
 				for (CostCategoryEstimate estCost:lstEstCostng){
 					estCost.setFilmEstimate(filmEstimate);
 				}
 			}
 			
 			logger.debug("new Object------------------------------------------------>"+filmEstimate);
 			
 			estimateResult = this.filmEstimateDAO.update(filmEstimate,null);
 			
 			
 			
 			logger.debug("saved new Object------------------------------------------------>"+estimateResult);
 			 
 			logger.debug("saved new Object------------------------------------------------>"+estimateResult.getFilmEstimateId());
 			logger.debug("old_filmEstimate_id------------------------------------------------>"+old_filmEstimate_id);
 			
 			if(null !=  estimateResult.getFilmEstimateId() && null != old_filmEstimate_id){								  	
 				PrintCostEstimate printCostEstimate=estimateService.getEstimateByFilmEstimateID(old_filmEstimate_id);
 				logger.debug(" old PrintCostEstimate Object------------------------------------------------>"+printCostEstimate);
 				if(null !=  printCostEstimate){
 					printCostEstimate = populateCreateVersionPrintCostEstimateDetails(printCostEstimate);
 					if(null != printCostEstimate && null !=  estimateResult.getFilmEstimateId()){
 						printCostEstimate.setFilmEstimateId(estimateResult.getFilmEstimateId());
 					}
 					
 					PrintCostEstimate savedPrintCostEstimate = estimateService.saveEstimate(printCostEstimate);					
 					logger.debug("saved new PrintCostEstimate Object------------------------------------------------>"+savedPrintCostEstimate);
 				}
 			}
 			
 			
 
 		}catch (Exception ex) {
 			logger.error(" Exception occured in FilmEstimateServiceImpl.createNewVersion :",
 					ex.getMessage());
 			ex.printStackTrace();
 			throw new GTSException(ex.getMessage(), ex.getCause());
 		}
 		logger.debug("Exiting into FilmEstimateServiceImpl.createNewVersion");
 		return estimateResult;
 	}
 
 
 	private FilmEstimate populateFilmEstimate(FilmEstimate estimate) {
 		if (!( null!=estimate && null!=estimate.getTerritoryId()
 				&& null!=estimate.getTerritoryId()
 				&& estimate.getTerritoryId().longValue()>0 )){
 			UserProfile profile =(UserProfile)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
 			estimate.setTerritoryId(profile.getTerritoryId());
 		}
 		return estimate;
 	}
 
 
 	private FilmEstimate populateSaveVersionDetails(FilmEstimate estimate) throws GTSException {
 	logger.info("Entering in FilmEstiamteServiceImpl.populateSaveVersionDetails");
 	
 	if(null!=estimate){
 		if (null==estimate.getFilmEstimateId() || estimate.getFilmEstimateId() == 0){
 			long territoryId = ((UserProfile)SecurityContextHolder.getContext().getAuthentication().getPrincipal()).territoryId;
 			estimate.setVersion(1+this.filmEstimateDAO.getNewVersion(estimate.getTitleId(),territoryId));	
 		}
 		
 		if(StringUtil.isNullorEmpty(estimate.getEstimateType()))//Initial , In progress ,//Final 
 		{
 			logger.debug("If in populateSaveVersionDetails");
 			logger.debug("IF 1"+estimate.getEstimateStatus());
 			logger.debug("IF 1"+estimate.getEstimateType());
 			estimate.setEstimateType(ConstantUtil.VERSION_TYPE_INITIAL);
 		}else{
 			logger.debug("Else Part Save");
 		//	estimate.setEstimateType(ConstantUtil.VERSION_TYPE_INPROGRESS);
 		}	
 		
 		if(StringUtil.isNullorEmpty(estimate.getEstimateStatus()) || estimate.getEstimateStatus().equalsIgnoreCase(ConstantUtil.VERSION_STATUS_NEW)){
 			logger.debug("If in populateSaveVersionDetails");
 			logger.debug("IF 2"+estimate.getEstimateStatus());
 			logger.debug("IF 2"+estimate.getEstimateType());
 			estimate.setEstimateStatus(ConstantUtil.VERSION_STATUS_NEW);
 		}
 		else{
 			logger.debug("Throw Excep");
 			logger.debug("Throw Excep"+estimate.getEstimateStatus());
 			logger.debug("Throw Excep"+estimate.getEstimateType());
 			throw new GTSException("Invalid Save States");
 		}
 		
 	}
 		
 		logger.info("Exiting in FilmEstiamteServiceImpl.populateSaveVersionDetails");
 		return estimate;
 	}
 
 	private FilmEstimate populateSubmitVersionDetails(FilmEstimate estimate) throws GTSException {
 		logger.info("Entering in FilmEstiamteServiceImpl.populateSubmitVersionDetails");
 		if(null!=estimate){
 			if (null==estimate.getFilmEstimateId() || estimate.getFilmEstimateId() == 0){
 				long territoryId = ((UserProfile)SecurityContextHolder.getContext().getAuthentication().getPrincipal()).territoryId;
 				estimate.setVersion(1+this.filmEstimateDAO.getNewVersion(estimate.getTitleId(),territoryId));	
 			}
 			
 			
 			if(StringUtil.isNullorEmpty(estimate.getEstimateType()))//Initial , In progress ,//Final 
 			{
 				logger.debug("If in populateSubmitVersionDetails");
 				logger.debug("IF 1"+estimate.getEstimateStatus());
 				logger.debug("IF 1"+estimate.getEstimateType());
 				estimate.setEstimateType(ConstantUtil.VERSION_TYPE_INITIAL);
 			}else{
 				//do nothing
 				logger.debug("Else in populateSubmitVersionDetails");
 				logger.debug("Else"+estimate.getEstimateStatus());
 				logger.debug("Else"+estimate.getEstimateType());
 			}		
 			//logic for Submit for Approval
 
 			if(!StringUtil.isNullorEmpty(estimate.getEstimateType()) && (estimate.getEstimateType().equalsIgnoreCase(ConstantUtil.VERSION_TYPE_INITIAL)) && (StringUtil.isNullorEmpty(estimate.getEstimateStatus()) || estimate.getEstimateStatus().equalsIgnoreCase(ConstantUtil.VERSION_STATUS_NEW) ) )
 			{
 				logger.debug("If in populateSubmitVersionDetails");
 				logger.debug("IF 2"+estimate.getEstimateStatus());
 				logger.debug("IF 2"+estimate.getEstimateType());
 				estimate.setEstimateStatus(ConstantUtil.VERSION_STATUS_APPROVED);
 			}
 			else if(!StringUtil.isNullorEmpty(estimate.getEstimateType()) && (estimate.getEstimateType().equalsIgnoreCase(ConstantUtil.VERSION_TYPE_CURRENT)) && (StringUtil.isNullorEmpty(estimate.getEstimateStatus()) || estimate.getEstimateStatus().equalsIgnoreCase(ConstantUtil.VERSION_STATUS_NEW) ) ){
 				logger.debug("If in populateSubmitVersionDetails");
 				logger.debug("IF 3"+estimate.getEstimateStatus());
 				logger.debug("IF 3"+estimate.getEstimateType());
 				estimate.setEstimateStatus(ConstantUtil.VERSION_STATUS_SUBMITTED);
 			}
 			else if(!StringUtil.isNullorEmpty(estimate.getEstimateType()) && (estimate.getEstimateType().equalsIgnoreCase(ConstantUtil.VERSION_TYPE_INTERIM)) && (StringUtil.isNullorEmpty(estimate.getEstimateStatus()) || estimate.getEstimateStatus().equalsIgnoreCase(ConstantUtil.VERSION_STATUS_NEW) ) ){
 				logger.debug("If in populateSubmitVersionDetails");
 				logger.debug("IF 3"+estimate.getEstimateStatus());
 				logger.debug("IF 3"+estimate.getEstimateType());
 				estimate.setEstimateStatus(ConstantUtil.VERSION_STATUS_SUBMITTED);
 			}
 			
 			else if( estimate.getEstimateStatus().equalsIgnoreCase(ConstantUtil.VERSION_STATUS_APPROVED) || estimate.getEstimateStatus().equalsIgnoreCase(ConstantUtil.VERSION_STATUS_REJECTED) || estimate.getEstimateStatus().equalsIgnoreCase(ConstantUtil.VERSION_STATUS_SUBMITTED))
 				{
 				logger.debug("If in populateSubmitVersionDetails");
 				logger.debug("IF 4"+estimate.getEstimateStatus());
 				logger.debug("IF 4"+estimate.getEstimateType());
 				throw new GTSException("Submit for Approval is not applicable for Approved,Rejected,Submitted Estimates");
 			}else {
 				logger.debug("Else Condition");
 				logger.debug("Else Condition"+estimate.getEstimateStatus());
 				logger.debug("Else Condition"+estimate.getEstimateType());
 			}
 
 			
 
 		}
 
 		logger.info("Exiting in FilmEstiamteServiceImpl.populateVersionDetails");
 		return estimate;
 	}
 	
 
 	private FilmEstimate populateCreateVersionDetails(FilmEstimate estimate) throws GTSException {
 		logger.info("Entering in FilmEstiamteServiceImpl.populateCreateVersionDetails");
 		
 		
 		if(null!=estimate){		
 			 estimate = flushIds(estimate);
 			 long territoryId = ((UserProfile)SecurityContextHolder.getContext().getAuthentication().getPrincipal()).territoryId;
 			 logger.debug("New Version in Create New Version---------------------------->"+this.filmEstimateDAO.getNewVersion(estimate.getTitleId(),territoryId));
 			 estimate.setVersion(1+ this.filmEstimateDAO.getNewVersion(estimate.getTitleId(),territoryId));
 			
 			
 			//logic forCreate Initial Estimate
 
 			if(StringUtil.isNullorEmpty(estimate.getEstimateType()) && StringUtil.isNullorEmpty(estimate.getEstimateStatus()) )
 			{
 				logger.debug("If in Create New Version Estimate");
 				logger.debug("IF 1"+estimate.getEstimateStatus());
 				logger.debug("IF 1"+estimate.getEstimateType());
 				estimate.setEstimateType(ConstantUtil.VERSION_TYPE_INITIAL);
 				estimate.setEstimateStatus(ConstantUtil.VERSION_STATUS_NEW);
 			}
 			else if( estimate.getEstimateType().equalsIgnoreCase(ConstantUtil.VERSION_TYPE_INITIAL) && estimate.getEstimateStatus().equalsIgnoreCase(ConstantUtil.VERSION_STATUS_NEW)){
 				logger.debug("If in Create New Version Estimate");
 				logger.debug("IF 2"+estimate.getEstimateStatus());
 				logger.debug("IF 2"+estimate.getEstimateType());
 				
 				//logic change
 				
 				//estimate.setEstimateType(ConstantUtil.VERSION_TYPE_INITIAL);
 				//estimate.setEstimateStatus(ConstantUtil.VERSION_STATUS_NEW);
 				
 				estimate.setEstimateType(ConstantUtil.VERSION_TYPE_CURRENT);
 				estimate.setEstimateStatus(ConstantUtil.VERSION_STATUS_NEW);
 			}
 			else if( estimate.getEstimateType().equalsIgnoreCase(ConstantUtil.VERSION_TYPE_INITIAL) && estimate.getEstimateStatus().equalsIgnoreCase(ConstantUtil.VERSION_STATUS_APPROVED)){
 				logger.debug("If in Create New Version Estimate");
 				logger.debug("IF 3"+estimate.getEstimateStatus());
 				logger.debug("IF 3"+estimate.getEstimateType());
 				
 				//logic change
 				
 				//estimate.setEstimateType(ConstantUtil.VERSION_TYPE_INPROGRESS);
 				//estimate.setEstimateStatus(ConstantUtil.VERSION_STATUS_NEW);
 				
 				estimate.setEstimateType(ConstantUtil.VERSION_TYPE_CURRENT);
 				estimate.setEstimateStatus(ConstantUtil.VERSION_STATUS_NEW);
 				
 			}
 			else if(estimate.getEstimateType().equalsIgnoreCase(ConstantUtil.VERSION_TYPE_INTERIM) && estimate.getEstimateStatus().equalsIgnoreCase(ConstantUtil.VERSION_STATUS_NEW)){
 				logger.debug("If in Create New Version Estimate");
 				logger.debug("IF 4"+estimate.getEstimateStatus());
 				logger.debug("IF 4"+estimate.getEstimateType());
 				
 				//logic change
 				
 				//estimate.setEstimateType(ConstantUtil.VERSION_TYPE_INPROGRESS);
 				//estimate.setEstimateStatus(ConstantUtil.VERSION_STATUS_NEW);
 				
 				estimate.setEstimateType(ConstantUtil.VERSION_TYPE_CURRENT);
 				estimate.setEstimateStatus(ConstantUtil.VERSION_STATUS_NEW);
 				
 			}
 			else if( estimate.getEstimateType().equalsIgnoreCase(ConstantUtil.VERSION_TYPE_INTERIM) && estimate.getEstimateStatus().equalsIgnoreCase(ConstantUtil.VERSION_STATUS_SUBMITTED)){
 				logger.debug("If in Create New Version Estimate");
 				logger.debug("IF 5"+estimate.getEstimateStatus());
 				logger.debug("IF 5"+estimate.getEstimateType());
 				estimate.setEstimateType(ConstantUtil.VERSION_TYPE_CURRENT);
 				estimate.setEstimateStatus(ConstantUtil.VERSION_STATUS_NEW);
 			}
 			else if( estimate.getEstimateType().equalsIgnoreCase(ConstantUtil.VERSION_TYPE_INTERIM) && estimate.getEstimateStatus().equalsIgnoreCase(ConstantUtil.VERSION_STATUS_APPROVED)){
 				logger.debug("If in Create New Version Estimate");
 				logger.debug("IF 6"+estimate.getEstimateStatus());
 				logger.debug("IF 6"+estimate.getEstimateType());
 				estimate.setEstimateType(ConstantUtil.VERSION_TYPE_CURRENT);
 				estimate.setEstimateStatus(ConstantUtil.VERSION_STATUS_NEW);
 			}
 			else if( estimate.getEstimateType().equalsIgnoreCase(ConstantUtil.VERSION_TYPE_INTERIM) && estimate.getEstimateStatus().equalsIgnoreCase(ConstantUtil.VERSION_STATUS_REJECTED)){
 				logger.debug("If in Create New Version Estimate");
 				logger.debug("IF 7"+estimate.getEstimateStatus());
 				logger.debug("IF 7"+estimate.getEstimateType());
 				estimate.setEstimateType(ConstantUtil.VERSION_TYPE_CURRENT);
 				estimate.setEstimateStatus(ConstantUtil.VERSION_STATUS_NEW);
 			}
 			else {
 				logger.debug("Else Condition in Create New Version Estimate");
 				logger.debug("Else Condition"+estimate.getEstimateStatus());
 				logger.debug("Else Condition"+estimate.getEstimateType());
 			}
 
 		}
 
 		logger.info("Exiting in FilmEstiamteServiceImpl.populateCreateVersionDetails");
 		return estimate;
 	}
 	
 	
 	private FilmEstimate flushIds(FilmEstimate estimate) {
 		logger.info("Entering in FilmEstiamteServiceImpl.flushIds");
 		estimate.setFilmEstimateId(null);
 		if(null!=estimate.getLstPrintCostCategories() && estimate.getLstPrintCostCategories().size()>0){
 			for (CostCategoryEstimate elements : estimate.getLstPrintCostCategories()) {
 				elements.setCostCategoryEstimateID(null);
 			}
 		}
 		logger.info("Entering in FilmEstiamteServiceImpl.flushIds");
 		return estimate;
 	}
 
 	
 	private PrintCostEstimate populateCreateVersionPrintCostEstimateDetails(PrintCostEstimate printCostEstimate) {
 		logger.info("Entering in FilmEstiamteServiceImpl.populateCreateVersionPrintCostEstimateDetails");
 		if(null !=  printCostEstimate){
 			
 			//flush Ids start
 			
 			printCostEstimate.setPrintCostEstimateID(null);
 			printCostEstimate.setFilmEstimateId(null);
 			
 			if(null != printCostEstimate.getEstimateCostHeader()){
 				printCostEstimate.getEstimateCostHeader().setEstCostHeadId(null);
 			}
 			
 			if(null != printCostEstimate.getCommonTrailerSection() && printCostEstimate.getCommonTrailerSection().size() > 0 ){
 				for(EstimateCommonTrailerInfo ecti : printCostEstimate.getCommonTrailerSection()){
 					if(null != ecti){
 						ecti.setEstimateCommonTrailerInfoId(null);
 					}				
 				}				
 			}
 			
 			if(null != printCostEstimate.getLstEstimateCostingDigitalFeature() && printCostEstimate.getLstEstimateCostingDigitalFeature().size() > 0 ){
 				for(EstimateCosting ec : printCostEstimate.getLstEstimateCostingDigitalFeature()){
 					if(null != ec){
 						ec.setEstimateCostingId(null);
 					}				
 				}				
 			}
 			
 			if(null != printCostEstimate.getLstEstimateCostingDubbing() && printCostEstimate.getLstEstimateCostingDubbing().size() > 0 ){
 				for(EstimateCosting ec : printCostEstimate.getLstEstimateCostingDubbing()){
 					if(null != ec){
 						ec.setEstimateCostingId(null);
 					}				
 				}				
 			}
 			
 			if(null != printCostEstimate.getLstEstimateCostingOthers() && printCostEstimate.getLstEstimateCostingOthers().size() > 0 ){
 				for(EstimateCosting ec : printCostEstimate.getLstEstimateCostingOthers()){
 					if(null != ec){
 						ec.setEstimateCostingId(null);
 					}				
 				}				
 			}
 			
 			if(null != printCostEstimate.getLstEstimateCostingTrailering() && printCostEstimate.getLstEstimateCostingTrailering().size() > 0 ){
 				for(ServiceTrailerWrapper stw : printCostEstimate.getLstEstimateCostingTrailering()){
 					if(null != stw){
						stw.setEstimateCostingId(null);	
						stw.setServiceTrailerWrapperId(null);
 						if(null != stw.getTrailerCosts() && stw.getTrailerCosts().size() > 0 ){
 							for(TrailerSTWDisplay tsd : stw.getTrailerCosts()){
 								if(null != tsd){
 									tsd.setTrailerId(null);
 								}				
 							}				
 						}						
 					}				
 				}				
 			}			
 			//flush Ids end
 			
 			//setting the parent Objects start			
 	
 			if(null != printCostEstimate.getEstimateCostHeader()){
 				printCostEstimate.getEstimateCostHeader().setprintCostEstimate(printCostEstimate);
 			}
 			
 			if(null != printCostEstimate.getCommonTrailerSection() && printCostEstimate.getCommonTrailerSection().size() > 0 ){
 				for(EstimateCommonTrailerInfo ecti : printCostEstimate.getCommonTrailerSection()){
 					if(null != ecti){
 						ecti.setPrintCostEstimate(printCostEstimate);
 					}				
 				}				
 			}
 			
 			if(null != printCostEstimate.getLstEstimateCostingDigitalFeature() && printCostEstimate.getLstEstimateCostingDigitalFeature().size() > 0 ){
 				for(EstimateCosting ec : printCostEstimate.getLstEstimateCostingDigitalFeature()){
 					if(null != ec){
 						ec.setPrintCostEstimate(printCostEstimate);
 					}				
 				}				
 			}
 			
 			if(null != printCostEstimate.getLstEstimateCostingDubbing() && printCostEstimate.getLstEstimateCostingDubbing().size() > 0 ){
 				for(EstimateCosting ec : printCostEstimate.getLstEstimateCostingDubbing()){
 					if(null != ec){
 						ec.setPrintCostEstimate(printCostEstimate);
 					}				
 				}				
 			}
 			
 			if(null != printCostEstimate.getLstEstimateCostingOthers() && printCostEstimate.getLstEstimateCostingOthers().size() > 0 ){
 				for(EstimateCosting ec : printCostEstimate.getLstEstimateCostingOthers()){
 					if(null != ec){
 						ec.setPrintCostEstimate(printCostEstimate);
 					}				
 				}				
 			}
 			
 			if(null != printCostEstimate.getLstEstimateCostingTrailering() && printCostEstimate.getLstEstimateCostingTrailering().size() > 0 ){
 				for(ServiceTrailerWrapper stw : printCostEstimate.getLstEstimateCostingTrailering()){
 					if(null != stw){
 						stw.setPrintCostEstimate(printCostEstimate);						
 						if(null != stw.getTrailerCosts() && stw.getTrailerCosts().size() > 0 ){
 							for(TrailerSTWDisplay tsd : stw.getTrailerCosts()){
 								if(null != tsd){
 									tsd.setServiceTrailerWrapper(stw);
 								}				
 							}				
 						}						
 					}				
 				}				
 			}
 			
 			//setting the parent Objects end
 		}	
 	
 		logger.info("Entering in FilmEstiamteServiceImpl.populateCreateVersionPrintCostEstimateDetails");
 		return printCostEstimate;
 	}
 
 
 	@Override
 	public FilmEstimate submitEstimate(FilmEstimate filmEstimate) throws GTSException {
 		// TODO Auto-generated method stub
 		logger.debug("Entering into EstimateServiceImpl.submitEstimate");
 		FilmEstimate filmEstimateResult = null;
 		try {
 			filmEstimate = populateFilmEstimate(filmEstimate);
 			filmEstimate = populateSubmitVersionDetails(filmEstimate);
 			
 			logger.debug("Control Coming Here");
 			
 			/*if(!(null!=filmEstimate.getFilmEstimateId() && filmEstimate.getFilmEstimateId().longValue()>0 )){
 				filmEstimate= populateSaveVersionDetails(filmEstimate);
 				filmEstimate = flushIds(filmEstimate);
 			
 
 			}*/
 			
 			
 			List<CostCategoryEstimate> lstEstCostng = filmEstimate.getLstPrintCostCategories();
 			if (null!=lstEstCostng){
 				for (CostCategoryEstimate estCost:lstEstCostng){
 					estCost.setFilmEstimate(filmEstimate);
 				}
 			}
 			
 			
 			filmEstimateResult = filmEstimateDAO.update(filmEstimate,null);
 			
 			//Condtion to trigger the workflow
 			if(filmEstimateResult.getEstimateStatus().equalsIgnoreCase(ConstantUtil.VERSION_STATUS_SUBMITTED))
 			{
 			logger.debug("WorkFlow Part.........."+filmEstimateResult);
 			FilmEstimateWorkFlow filmEstimateWorkFlow = new FilmEstimateWorkFlow();
 			logger.debug("Film Estimate Object in the EstimateServiceImpl.submitEstimate"+filmEstimateResult);
 			filmEstimateWorkFlow.setFilmEstimateId(filmEstimateResult.getFilmEstimateId());
 			filmEstimateWorkFlow.setFilmEstimateStatus(filmEstimateResult.getEstimateStatus());
 			logger.debug("After Setting the Estimate Id and Estimate Status values....");
 			List<CostCategoryEstimate> lstCostCategoryEstimate = filmEstimateResult.getLstPrintCostCategories();
 			logger.debug("CostCategory List SIze----------------->"+lstCostCategoryEstimate.size());
 			BigDecimal totalCost = new BigDecimal(0l);
 			for (CostCategoryEstimate element : lstCostCategoryEstimate) {
 				if( null != element){
 					logger.debug("Element is Not Null----------------->");
 				if( null != element.getEstimateCostTypeId() && element.getEstimateCostTypeId()== 1){
 					logger.debug("Adpub Cost----------------->"+element.getTotalCost());
 					filmEstimateWorkFlow.setAdpubEstimateCost(element.getTotalCost());
 					totalCost = totalCost.add(element.getTotalCost());
 				}
 				if( null != element.getEstimateCostTypeId() && element.getEstimateCostTypeId() == 2){
 					logger.debug("Print Total Cost----------------->"+element.getTotalCost());
 					filmEstimateWorkFlow.setPrintEstimateCost(element.getTotalCost());
 					totalCost = totalCost.add(element.getTotalCost());
 				}
 				if( null != element.getEstimateCostTypeId() && element.getEstimateCostTypeId()== 3){
 					logger.debug("Other DDE COst----------------->"+element.getTotalCost());
 					filmEstimateWorkFlow.setOtherDDECost(element.getTotalCost());
 					totalCost = totalCost.add(element.getTotalCost());
 				}
 				if(null != element.getEstimateCostTypeId() && element.getEstimateCostTypeId()== 4){
 					logger.debug("Estimate GBO Cost----------------->"+element.getTotalCost());
 					filmEstimateWorkFlow.setEstimateGBOCost(element.getTotalCost());
 					totalCost = totalCost.add(element.getTotalCost());
 				}
 				}
 				else{
 					logger.debug("Element is Null----------------->");
 				}
 				
 			}
 			filmEstimateWorkFlow.setTotalCost(totalCost.longValue());
 			logger.debug("Setting the User Profile----------------->");
 			//user , Title , Territory 
 			UserProfile profile =(UserProfile)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
 			filmEstimateWorkFlow.setRequesterId(profile.getUserid());
 			filmEstimateWorkFlow.setRequesterName(profile.getUsername());
 			filmEstimateWorkFlow.setTerritoryId(profile.getTerritoryId());
 			if (null != profile.getTerritory()){
 				logger.debug("Territory Name\t"+profile.getTerritory().getName());
 				filmEstimateWorkFlow.setTerritoryName(profile.getTerritory().getName());
 			}
 			else
 			{
 				logger.debug("Territory Object is Null.......");
 			}
 			
 			
 			logger.debug("Setting the User Profile Complated----------------->");
 			
 			logger.debug("Setting the Title Information----------------->");
 			Title title = titleDAO.findById(filmEstimate.getTitleId(), false, "");
 			if(title != null){
 				logger.debug("Title Object is Not Null");
 				filmEstimateWorkFlow.setTitleId(title.getTitleId());
 				filmEstimateWorkFlow.setTitleName(title.getTitleName());
 				filmEstimateWorkFlow.setVersion(filmEstimate.getVersion());
 			}
 			else{
 				logger.debug("Title Object is Null");
 			}
 			logger.debug("Setting the Title Information Completed----------------->");
 			logger.debug("Film Estimate Workflow Object ----------------->"+filmEstimateWorkFlow);
 			triggerRegionalApprovalProcess(filmEstimateWorkFlow);
 			}
 			else{
 				logger.debug("Not WorkFlow Part..........Auto Approve------------------------------>"+filmEstimateResult);
 			}
 			
 		}catch (Exception ex) {
 			logger.error(
 					" Exception occured in EstimateServiceImpl.submitEstimate :",
 					ex.getMessage());
 			throw new GTSException(ex.getMessage(), ex.getCause());
 		}
 		logger.debug("Exiting into EstimateServiceImpl.submitEstimate");
 		return filmEstimateResult;
 	}
 
 
 	@Override
 	public void triggerRegionalApprovalProcess(FilmEstimateWorkFlow filmEstimateWorkflow)
 			throws GTSException {
 		logger.debug("Entering into EstimateServiceImpl.triggerRegionalApprovalProcess");
 		try
 		{
 		
 		Map<String,Object> processInputMap = new HashMap<String, Object>();
 		logger.debug("Film Estimate Object before triggering the workflow"+filmEstimateWorkflow);
 		processInputMap.put("filmestimateworkflow", filmEstimateWorkflow);
 		processInputMap.put("logger", logger);
 		//JPAWorkingMemoryDbLogger auditlogger = new JPAWorkingMemoryDbLogger(statfulknowsession);
 		logger.debug("1111111111111111111111111111");
 		/*if (null == statfulknowsession || auditlogger == null ){
 			System.out.println("Null...");
 		}*/
 		
 		logger.debug("2222222222222222222222222222222");
 		statfulknowsession.startProcess(ConstantUtil.REGIONAL_APPROVAL_PROCESS_NAME,processInputMap);
 		logger.debug("333333333333333333333333333333333");
 		}
 		catch(Exception ex){
 			logger.error(
 					" Exception occured in FilmEstimateServiceImpl.triggerRegionalApprovalProcess :",
 					ex.getMessage());
 			ex.printStackTrace();
 			throw new GTSException(ex.getMessage(), ex.getCause());
 		}
 		logger.debug("Exiting into FilmEstimateServiceImpl.triggerRegionalApprovalProcess");
 	}
 
 	@Override
 	public List<EstimateByTerritory> lstEstimatesSubmmitedByTerritories(
 			Long titldId, String role,Long regionId,SearchCriteria sc) throws GTSException {
 		// TODO Auto-generated method stub
 		List<EstimateByTerritory> lstEstimateByTerritory = null;
 		try{
 			logger.debug("Entering into FilmEstimateServiceImpl.lstEstimatesSubmmitedByTerritories");
 			lstEstimateByTerritory = filmEstimateDAO.lstEstimatesSubmmitedByTerritories(titldId, role,regionId,sc);
 			
 		}
 		catch(GTSException gts){
 			logger.debug("Exception Happened in FilmEstimateServiceImpl.lstEstimatesSubmmitedByTerritories");
 			throw gts;
 		}
 		return lstEstimateByTerritory;
 	}
 
 
 	@Override
 	public List<RegionInitiateEstimate> lstTerritoriesForInitiateWorkflow(
 			Long titldId)throws GTSException {
 		// TODO Auto-generated method stub
 		List<RegionInitiateEstimate> lstRegionIntiateEstimate = null;
 		try{
 			logger.debug("Entering into FilmEstimateServiceImpl.lstTerritoriesForInitiateWorkflow");
 			UserProfile profile =(UserProfile)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
 			if (profile.getRegion() != null && profile.getRegionId() > 0){
 				lstRegionIntiateEstimate = filmEstimateDAO.lstTerritoriesForInitiateWorkflow(titldId, profile.getRegionId());
 			}
 			logger.debug("Exiting into FilmEstimateServiceImpl.lstTerritoriesForInitiateWorkflow");
 		}
 		catch(GTSException gts){
 			logger.debug("Exception Happened in FilmEstimateServiceImpl.lstEstimatesSubmmitedByTerritories");
 			throw gts;
 		}
 		return lstRegionIntiateEstimate;
 	}
 	
 }
