 package org.vme.service.search.vme;
 
 import java.util.Calendar;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.inject.Inject;
 import javax.persistence.EntityManager;
 import javax.persistence.Query;
 
 import org.apache.commons.lang.StringUtils;
 import org.fao.fi.figis.dao.FigisDao;
 import org.fao.fi.figis.domain.VmeObservation;
 import org.fao.fi.vme.dao.config.VmeDB;
 import org.fao.fi.vme.domain.GeneralMeasure;
 import org.fao.fi.vme.domain.GeoRef;
 import org.fao.fi.vme.domain.InformationSource;
 import org.fao.fi.vme.domain.Profile;
 import org.fao.fi.vme.domain.SpecificMeasure;
 import org.fao.fi.vme.domain.ValidityPeriod;
 import org.fao.fi.vme.domain.Vme;
 import org.fao.fi.vme.domain.util.MultiLingualStringUtil;
 import org.vme.service.dto.VmeGetRequestDto;
 import org.vme.service.dto.VmeRequestDto;
 import org.vme.service.dto.VmeSearchDto;
 import org.vme.service.dto.VmeSearchRequestDto;
 import org.vme.service.dto.VmeSearchResult;
 import org.vme.service.reference.ReferenceServiceException;
 import org.vme.service.reference.ReferenceServiceFactory;
 import org.vme.service.reference.domain.Authority;
 import org.vme.service.reference.domain.VmeCriteria;
 import org.vme.service.reference.domain.VmeType;
 
 
 public class VmeSearchService implements SearchService {
 
 
 	@Inject
 	@VmeDB
 	private EntityManager entityManager;
 
 	protected FigisDao dao;
 
 	private MultiLingualStringUtil u = new MultiLingualStringUtil();
 
 
 	public VmeSearchService() {
 		System.out.println("VME search engine 1.0");
 
 	}
 
 
 	@SuppressWarnings("unchecked")
 	public VmeSearchResult search(VmeSearchRequestDto request) throws Exception  {
 		if (request.hasYear()){
 		} else {
 			request.setYear( Calendar.getInstance().get(Calendar.YEAR));
 		}
 		Query query = entityManager.createQuery(createHibernateSearchTextualQuery(request));
 		List<Vme> result =  (List<Vme>)query.getResultList();
 		List<Vme> toRemove =  postPurgeResult(request, result);
 		VmeSearchResult res = convertPersistenceResult(request, (List<Vme>) result, toRemove);
 		return res;
 	}
 
 
 	public VmeSearchResult get(VmeGetRequestDto request)  {
 		if (request.hasYear()){
 		} else {
 			request.setYear( Calendar.getInstance().get(Calendar.YEAR));
 		}		
 		String text_query;
 
 		if (request.getId()>0){
 			text_query = "from Vme vme where vme.id = " + request.getId();
 		} else if (request.hasInventoryIdentifier()) {
 			text_query = "from Vme vme where vme.inventoryIdentifier = '" + request.getInventoryIdentifier() + "'";
 		} else if (request.hasGeographicFeatureId()) {
 			text_query = "SELECT vme from Vme vme, GEO_REF gfl WHERE vme = gfl.vme and gfl IN (SELECT gfl from GEO_REF WHERE gfl.geographicFeatureID = '" + request.getGeographicFeatureId() + "')";
 		} else text_query = "";
 		Query query = entityManager.createQuery(text_query);
 		List<?> result =   query.getResultList();
 		@SuppressWarnings("unchecked")
 		VmeSearchResult res = convertPersistenceResult(request, (List<Vme>) result, null);
 		return res;
 	}
 
 
 
 	private String createHibernateSearchTextualQuery(VmeSearchRequestDto request) throws Exception {
 		StringBuffer txtQuery = new StringBuffer(200);
 		String conjunction;
 		txtQuery.append("Select vme from Vme vme");
 		if (request.hasAtLeastOneParameterButText()){
 			txtQuery.append(" where");
 			conjunction = "";
 		} else {
 			return txtQuery.toString();
 		}
 
 		if (request.hasAuthority()){
 			Authority vmeAuthority = (Authority) ReferenceServiceFactory.getService().getReference(Authority.class, (long) request.getAuthority());
 			String authority = vmeAuthority.getAcronym();
 			txtQuery.append(conjunction);
 			txtQuery.append(" vme.rfmo.id = '");
 			txtQuery.append(authority);
 			txtQuery.append("'");
 			conjunction = " AND";
 		}
 
 		if (request.hasCriteria()){
 			VmeCriteria vmeCriteria = (VmeCriteria) ReferenceServiceFactory.getService().getReference(VmeCriteria.class, (long) request.getCriteria());
 			String criteria = vmeCriteria.getName();
 			txtQuery.append(conjunction);
 			txtQuery.append(" vme.criteria = '");
 			txtQuery.append(criteria);
 			txtQuery.append("'");
 			conjunction = " AND";
 		}
 
 		if (request.hasType()){
 			VmeType vmeType = (VmeType) ReferenceServiceFactory.getService().getReference(VmeType.class, (long) request.getType());
 			String areaType = vmeType.getName();
 			txtQuery.append(conjunction);
 			txtQuery.append("  vme.areaType = '");
 			txtQuery.append(areaType);
 			txtQuery.append("'");
 			conjunction = " AND";
 		}
 
 		txtQuery.append(" AND vme.validityPeriod.beginYear <= ");
 		txtQuery.append(request.getYear());
 		txtQuery.append(" AND vme.validityPeriod.endYear >= ");
 		txtQuery.append(request.getYear());
 		
 		String res = txtQuery.toString();
 		System.out.println("FAB:" + res);
 		return res;
 	}
 
 
 
 
 
 
 	private List<Vme> postPurgeResult (VmeSearchRequestDto request,  List<Vme> result){
 		int requested_year = request.getYear();
 		List<Vme> res = new LinkedList<Vme>();
 		// Patch placed to solve VME-10 JIRA issue.
 		for (Vme vme : result) {
 			if (vme.getRfmo().getId().trim().equals("SIODFA")){
 				res.add(vme);
 			}
 		}
 		if (requested_year>0) {
 			for (Vme vme : result) {
 				boolean is_good = false;
 				List<GeoRef> georef = vme.getGeoRefList();
 				for (GeoRef profile : georef) {
 					if (profile.getYear()==requested_year) {
 						is_good = true;
 						break;
 					}
 				}
 				if (!is_good){
 					ValidityPeriod validityPeriod =  vme.getValidityPeriod();
 					if (validityPeriod.getBeginYear()<= requested_year && validityPeriod.getEndYear()>= requested_year){
 						is_good = true;
 					}
 				}
 				
 				if (is_good && request.hasText()){
 					is_good = containRelevantText(vme, request.getText());
 				}
 				
 				if (!is_good){
 					res.add(vme);
 				}
 			}
 		}
 		return res;
 	}
 
 
 
 
 
 
 	private boolean containRelevantText(Vme vme, String text) {
 		if (StringUtils.containsIgnoreCase(vme.getAreaType(), text)) return true;
 		if (StringUtils.containsIgnoreCase(vme.getCriteria(), text)) return true;
 		for (String element : vme.getGeoArea().getStringMap().values()) {
 			if (StringUtils.containsIgnoreCase(element, text)) return true;
 		} 
 		if (StringUtils.containsIgnoreCase(vme.getGeoform(), text)) return true;
 		for (GeoRef geoRef : vme.getGeoRefList()) {
 			if (StringUtils.containsIgnoreCase(geoRef.getGeographicFeatureID(), text)) return true;
 		}
 		if (StringUtils.containsIgnoreCase(vme.getInventoryIdentifier(), text)) return true;
 		for (String element : vme.getName().getStringMap().values()) {
 			if (StringUtils.containsIgnoreCase(element, text)) return true;
 		} 
 		for (Profile profile : vme.getProfileList()) {
 			for (String element : profile.getDescriptionBiological().getStringMap().values()) {
 				if (StringUtils.containsIgnoreCase(element, text)) return true;
 			} 
 			for (String element : profile.getDescriptionImpact().getStringMap().values()) {
 				if (StringUtils.containsIgnoreCase(element, text)) return true;
 			} 
 			for (String element : profile.getDescriptionPhisical().getStringMap().values()) {
 				if (StringUtils.containsIgnoreCase(element, text)) return true;
 			} 
 		}
 		
 		for (GeneralMeasure generalMeasure : vme.getRfmo().getGeneralMeasureList()) {
 			if (StringUtils.containsIgnoreCase(generalMeasure.getFishingAreas(), text)) return true;
 			if (generalMeasure.getExplorataryFishingProtocols()!=null){
 				for (String element : generalMeasure.getExplorataryFishingProtocols().getStringMap().values()) {
 					if (StringUtils.containsIgnoreCase(element, text)) return true;
 				} 
 			}
 			if (generalMeasure.getVmeEncounterProtocols()!=null){
 				for (String element : generalMeasure.getVmeEncounterProtocols().getStringMap().values()) {
 					if (StringUtils.containsIgnoreCase(element, text)) return true;
 				} 
 			}
 			if (generalMeasure.getVmeIndicatorSpecies()!=null){
 				for (String element : generalMeasure.getVmeIndicatorSpecies().getStringMap().values()) {
 					if (StringUtils.containsIgnoreCase(element, text)) return true;
 				} 
 			}
 
 			if (generalMeasure.getVmeThreshold()!=null){
 				for (String element : generalMeasure.getVmeThreshold().getStringMap().values()) {
 					if (StringUtils.containsIgnoreCase(element, text)) return true;
 				} 
 			}
 			
 			if (generalMeasure.getInformationSourceList()!=null){
 				for (InformationSource informationSource : generalMeasure.getInformationSourceList()) {
 					
 					if (informationSource.getCitation()!=null){
 						for (String element : informationSource.getCitation().getStringMap().values()) {
 							if (StringUtils.containsIgnoreCase(element, text)) return true;
 						} 
 					}
 					if (informationSource.getCommittee()!=null){
 						for (String element : informationSource.getCommittee().getStringMap().values()) {
 							if (StringUtils.containsIgnoreCase(element, text)) return true;
 						} 
 					}
 
 					if (informationSource.getReportSummary()!=null){
 						for (String element : informationSource.getReportSummary().getStringMap().values()) {
 							if (StringUtils.containsIgnoreCase(element, text)) return true;
 						} 
 					}
 					if (StringUtils.containsIgnoreCase(	Integer.toString(informationSource.getPublicationYear()), text)) return true;
 					if (StringUtils.containsIgnoreCase(informationSource.getUrl()!=null?informationSource.getUrl().toExternalForm():"", text)) return true;
 				}
 			}
 			
 
 		}
 		
 		
 		for (SpecificMeasure specificMeasure : vme.getSpecificMeasureList()) {
 			if (specificMeasure.getVmeSpecificMeasure()!=null){
 				for (String element : specificMeasure.getVmeSpecificMeasure().getStringMap().values()) {
 					if (StringUtils.containsIgnoreCase(element, text)) return true;
 				} 
 			}
 			if (specificMeasure.getInformationSource()!=null){
 				if(specificMeasure.getInformationSource().getCitation()!=null){
 					for (String element : specificMeasure.getInformationSource().getCitation().getStringMap().values()) {
 						if (StringUtils.containsIgnoreCase(element, text)) return true;
 					} 
 				}
 				if (StringUtils.containsIgnoreCase(	Integer.toString(specificMeasure.getInformationSource().getPublicationYear()), text)) return true;
 				if (StringUtils.containsIgnoreCase(specificMeasure.getInformationSource().getUrl()!=null?specificMeasure.getInformationSource().getUrl().toExternalForm():"", text)) return true;
 			}
			if (specificMeasure.getVmeSpecificMeasure()!=null && specificMeasure.getInformationSource().getCommittee()!=null){
 				for (String element : specificMeasure.getInformationSource().getCommittee().getStringMap().values()) {
 					if (StringUtils.containsIgnoreCase(element, text)) return true;
 				} 
 			}
 
 			if (specificMeasure.getInformationSource()!=null && specificMeasure.getInformationSource().getReportSummary()!=null){
 				for (String element : specificMeasure.getInformationSource().getReportSummary().getStringMap().values()) {
 					if (StringUtils.containsIgnoreCase(element, text)) return true;
 				} 
 			}
 
 		}
 		return false;
 	}
 
 
 	private VmeSearchResult convertPersistenceResult(VmeRequestDto request,  List<Vme> result, List<Vme> toRemove){
 		VmeSearchResult res = new VmeSearchResult(request);
 		for (Vme vme : result) {
 			if (toRemove==null || (toRemove!=null  && !toRemove.contains(vme))){
 				res.addElement(getVmeSearchDto(vme,request.getYear()));
 			}
 		}
 		return res;
 	}
 
 
 
 
 	private VmeSearchDto getVmeSearchDto(Vme vme, int year) {
 		VmeSearchDto res = new VmeSearchDto();
 		res.setVmeId(vme.getId());
 		res.setInventoryIdentifier(vme.getInventoryIdentifier());
 		res.setLocalName(u.getEnglish(vme.getName()));
 		res.setEnvelope("");
 		String authority_acronym = vme.getRfmo().getId();
 		try {
 			Authority authority = (Authority)ReferenceServiceFactory.getService().getReferencebyAcronym(Authority.class, authority_acronym);
 			res.setOwner(authority.getName() + " (" + authority.getAcronym() + ")");
 		} catch (ReferenceServiceException e) {
 			res.setOwner(authority_acronym);
 			e.printStackTrace();
 		}
 		
 		VmeObservation vo = dao.findFirstVmeObservation(vme.getId(), Integer.toString(year));
 		if (vo!=null){
 			res.setFactsheetUrl("fishery/vme/"+ vo.getId().getVmeId() + "/" + vo.getId().getObservationId() +"/en");
 		} else {
 			res.setFactsheetUrl("");
 		}
 
 		res.setGeoArea(u.getEnglish(vme.getGeoArea()));
 		
 		res.setValidityPeriodFrom(vme.getValidityPeriod().getBeginYear());
 		res.setValidityPeriodTo(vme.getValidityPeriod().getEndYear());
 		res.setVmeType(vme.getAreaType());
 		res.setYear(year);
 		res.setGeographicFeatureId(vme.getGeoRefList().size()>0?vme.getGeoRefList().get(0).getGeographicFeatureID():"");
 		return res;
 	}
 
 
 	/**
 	 * @param dao the dao to set
 	 */
 	@Inject
 	public void setDao(FigisDao dao) {
 		this.dao = dao;
 	}
 
 
 
 
 
 }
