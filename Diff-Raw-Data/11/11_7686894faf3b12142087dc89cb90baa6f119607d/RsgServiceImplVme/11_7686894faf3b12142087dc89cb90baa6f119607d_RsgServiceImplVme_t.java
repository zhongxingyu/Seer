 package org.fao.fi.vme.rsg.service;
 
 import java.io.File;
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.annotation.PostConstruct;
 import javax.enterprise.inject.Alternative;
 import javax.enterprise.inject.Any;
 import javax.enterprise.inject.Instance;
 import javax.inject.Inject;
 import javax.persistence.EntityManager;
 import javax.persistence.EntityTransaction;
 
 import org.fao.fi.vme.domain.model.GeneralMeasure;
 import org.fao.fi.vme.domain.model.InformationSource;
 import org.fao.fi.vme.domain.model.MultiLingualString;
 import org.fao.fi.vme.domain.model.Rfmo;
 import org.fao.fi.vme.domain.model.Vme;
 import org.fao.fi.vme.domain.model.extended.FisheryAreasHistory;
 import org.fao.fi.vme.domain.model.extended.VMEsHistory;
 import org.fao.fi.vme.domain.util.MultiLingualStringUtil;
 import org.fao.fi.vme.msaccess.VmeAccessDbImport;
 import org.gcube.application.reporting.persistence.PersistenceManager;
 import org.gcube.application.reporting.reader.ModelReader;
 import org.gcube.application.rsg.service.RsgService;
 import org.gcube.application.rsg.service.dto.NameValue;
 import org.gcube.application.rsg.service.dto.ReportEntry;
 import org.gcube.application.rsg.service.dto.ReportType;
 import org.gcube.application.rsg.service.dto.response.Response;
 import org.gcube.application.rsg.service.dto.response.ResponseEntry;
 import org.gcube.application.rsg.service.dto.response.ResponseEntryCode;
 import org.gcube.application.rsg.service.util.RsgServiceUtil;
 import org.gcube.application.rsg.support.builder.ReportBuilder;
 import org.gcube.application.rsg.support.builder.annotations.Builder;
 import org.gcube.application.rsg.support.builder.exceptions.ReportBuilderException;
 import org.gcube.application.rsg.support.compiler.ReportCompiler;
 import org.gcube.application.rsg.support.compiler.annotations.Compiler;
 import org.gcube.application.rsg.support.compiler.annotations.Evaluator;
 import org.gcube.application.rsg.support.compiler.bridge.annotations.RSGReferenceReport;
 import org.gcube.application.rsg.support.compiler.bridge.annotations.RSGReport;
 import org.gcube.application.rsg.support.compiler.bridge.annotations.fields.RSGConverter;
 import org.gcube.application.rsg.support.compiler.bridge.converters.DataConverter;
 import org.gcube.application.rsg.support.compiler.bridge.interfaces.ReferenceReport;
 import org.gcube.application.rsg.support.compiler.bridge.interfaces.Report;
 import org.gcube.application.rsg.support.compiler.bridge.utilities.ScanningUtils;
 import org.gcube.application.rsg.support.compiler.bridge.utilities.Utils;
 import org.gcube.application.rsg.support.evaluator.ReportEvaluator;
 import org.gcube.application.rsg.support.model.components.impl.CompiledReport;
 import org.gcube.application.rsg.support.model.utils.CompiledReportUtils;
 import org.gcube.portlets.d4sreporting.common.shared.Model;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.vme.service.dao.sources.vme.VmeDao;
 
 /**
  * Place your class / interface description here.
  *
  * History:
  *
  * ------------- --------------- -----------------------
  * Date			 Author			 Comment
  * ------------- --------------- -----------------------
  * 28/nov/2013   EVanIngen, FFiorellato     Creation.
  *
  * @version 1.0
  * @since 28/nov/2013
  */
 @Alternative
 public class RsgServiceImplVme implements RsgService {
 	final static private Logger LOG = LoggerFactory.getLogger(RsgServiceImplVme.class);
 
 	final protected RsgServiceUtil u = new RsgServiceUtil();
 	final protected MultiLingualStringUtil MLSu = new MultiLingualStringUtil();
 
 	@Inject @Compiler private ReportCompiler _compiler;
 	@Inject @Builder private ReportBuilder<Model> _builder;
 	@Inject @Evaluator private ReportEvaluator _evaluator;
 
 	@Inject @Any private Instance<Report> _reports;
 	@Inject @Any private Instance<ReferenceReport> _refReports;
 
 	@Inject VmeAccessDbImport importer;
 	@Inject VmeDao vmeDao;
 
 	@PostConstruct
 	private void postConstruct() {
 		this._compiler.registerPrimitiveType(MultiLingualString.class);
 
 		LOG.info("Available report types:");
 		for(Object report : this._reports) {
 			LOG.info("{}", report.getClass().getName());
 		}
 
 		LOG.info("Available reference report types:");
 		for(Object refReport : this._refReports) {
 			LOG.info("{}", refReport.getClass().getName());
 		}
 
 		//Using an in-memory database requires that data are 
 		//transferred from the original M$ Access DB into H2...
 		this.importer.importMsAccessData();
 	}
 	
 	final private String getReportDumpPath() {
 		String path = Utils.coalesce(System.getProperty("rsg.reports.dump.path"), System.getProperty("java.io.tmpdir"));
 
 		if(path != null && !path.endsWith(File.separator))
 			path += File.separator;
 
 		return path;
 	}
 	
 	@SuppressWarnings("unused")
 	final private void dumpModel(CompiledReport report) throws ReportBuilderException {
 		String type = report.getType();
 		String reportId = report.getId();
 		
 		if(reportId == null)
 			reportId = "new" + System.currentTimeMillis();
 		
 		ReportType reportType = new ReportType(type.substring(type.lastIndexOf(".")));
 		
 		Model model = this._builder.buildReport(report);
 	
 		File folder = new File(this.getReportDumpPath() + reportType.getTypeIdentifier());
 	
 		folder.mkdir();
 	
 		folder = new File(folder.getAbsolutePath() + File.separator + reportType.getTypeIdentifier().toUpperCase() + "_" + reportId);
 	
 		folder.mkdir();
 	
 		File file = new File(folder, reportType.getTypeIdentifier().toUpperCase() + "_" + reportId + ".d4st");
 	
 		new ModelReader(model);
 	
 		PersistenceManager.writeModel(model, file);
 		PersistenceManager.readModel(file.getAbsolutePath());
 	}
 	
 	final private Response handleRollback(Response current, EntityTransaction tx) {
 		try {
 			LOG.info("Rolling back transaction...");
 			
 			if(current != null) {
 				if(current.getResponseMessageList() != null && !current.getResponseMessageList().isEmpty()) {
 					LOG.info("Current response info:");
 					for(ResponseEntry entry : current.getResponseMessageList()) {
 						LOG.info("{} : {}", entry.getResponseCode(), entry.getResponseMessage());
 					}
 				} else {
 					LOG.warn("No previous response info!");
 				}
 			}
 			
 			tx.rollback();
 		} catch(Throwable t) {
 			String message = "Unable to rollback transaction: " + t.getClass().getSimpleName() + " [ " + t.getMessage() + " ]";
 			
 			LOG.error(message);
 			
 			current.addEntry(new ResponseEntry(ResponseEntryCode.NOT_SUCCEEDED, message));
 		}
 		
 		return current;
 	}
 
 	private Class<?> getTemplateOfType(Class<? extends Annotation> marker, ReportType reportType) {
 		if(marker.equals(RSGReport.class)) {
 			for(Object report : this._reports)
 				if(report.getClass().getSimpleName().equals(reportType.getTypeIdentifier()))
 					return report.getClass();
 		} else if(marker.equals(RSGReferenceReport.class)) {
 			for(Object refReport : this._refReports)
 				if(refReport.getClass().getSimpleName().equals(reportType.getTypeIdentifier()))
 					return refReport.getClass();
 		} 
 
 		return null;
 	}
 
 	@Override
 	public ReportType[] getReportTypes() {
 		u.create();
 
 		for(Object report : this._reports)
 			u.add(report.getClass().getSimpleName());
 
 //		for(Object report : this._refReports)
 //			u.add(report.getClass().getSimpleName());
 
 		return u.getReportTypes().toArray(new ReportType[0]);
 	}
 
 	@Override
 	public ReportType[] getRefReportTypes() {
 		u.create();
 
 		for(Object report : this._refReports)
 			u.add(report.getClass().getSimpleName());
 
 		return u.getReportTypes().toArray(new ReportType[0]);
 	}
 
 	@Override
 	public ReportEntry[] listReports(ReportType reportType, NameValue... filters) {
 		final String typeId = reportType.getTypeIdentifier();
 
 		Map<String, Object> criteria = new HashMap<String, Object>();
 
 		if(filters != null)
 			for(NameValue filter : filters) {
 				criteria.put(filter.getName(), filter.getValue());
 			}
 
 		if("Vme".equals(typeId))
 			return this.listVMEs(criteria);
 		else if("Rfmo".equals(typeId))
 			return this.listRFMOs(criteria);
 		else if("InformationSource".equals(typeId))
 			return this.listInformationSources(criteria);
 		else if("GeneralMeasure".equals(typeId))
 			return this.listGeneralMeasure(criteria);
 		else if("VMEsHistory".equals(typeId))
 			return this.listVMEsHistory(criteria);
 		else if("FisheryAreasHistory".equals(typeId))
 			return this.listFisheryAreasHistory(criteria);
 		return null;
 	}
 
 	private ReportEntry[] listVMEs(Map<String, Object> criteria) {
 		Collection<Vme> vmes = this.vmeDao.filterEntities(this.vmeDao.getEm(), Vme.class, criteria);
 
 		List<ReportEntry> results = new ArrayList<ReportEntry>();
 
 		ReportEntry entry;
 		Rfmo owner;
 		for(Vme vme : vmes) {
 			owner = vme.getRfmo();
 			entry = new ReportEntry();
 			entry.setNameValueList(new ArrayList<NameValue>());
 
 			entry.setId(vme.getId());
 			entry.setReportType(new ReportType("Vme"));
 			entry.setOwned(owner != null);
 			entry.setOwner(owner == null ? "[ NOT SET]" : owner.getId());
 
 			entry.setIdentifier(MLSu.getEnglish(vme.getName()));
 
 			entry.getNameValueList().add(new NameValue("InventoryIdentifier", vme.getInventoryIdentifier()));
 			entry.getNameValueList().add(new NameValue("Rfmo", owner == null ? "[ NOT SET]" : owner.getId()));
 			entry.getNameValueList().add(new NameValue("Name", MLSu.getEnglish(vme.getName())));
 
 			results.add(entry);
 		}
 
 		return results.toArray(new ReportEntry[0]);
 	}
 
 	private ReportEntry[] listRFMOs(Map<String, Object> criteria) {
 		Collection<Rfmo> rfmos = this.vmeDao.filterEntities(this.vmeDao.getEm(), Rfmo.class, criteria);
 
 		List<ReportEntry> results = new ArrayList<ReportEntry>();
 
 		ReportEntry entry;
 		for(Rfmo rfmo : rfmos) {
 			entry = new ReportEntry();
 			entry.setNameValueList(new ArrayList<NameValue>());
 
 			entry.setId(rfmo.getId());
 			entry.setReportType(new ReportType("Rfmo"));
 			entry.setIdentifier(rfmo.getId());
 
 			entry.getNameValueList().add(new NameValue("Name", rfmo.getId()));
 			entry.getNameValueList().add(new NameValue("Number of VMEs", String.valueOf(rfmo.getListOfManagedVmes().size())));
 
 			results.add(entry);
 		}
 
 		return results.toArray(new ReportEntry[0]);
 	}
 
 	private ReportEntry[] listInformationSources(Map<String, Object> criteria) {
 		Collection<InformationSource> informationSources = this.vmeDao.filterEntities(this.vmeDao.getEm(), InformationSource.class, criteria);
 
 		List<ReportEntry> results = new ArrayList<ReportEntry>();
 
 		ReportEntry entry;
 		Rfmo owner;
 		for(InformationSource informationSource : informationSources) {
 			owner = informationSource.getRfmo();
 			entry = new ReportEntry();
 			entry.setNameValueList(new ArrayList<NameValue>());
 
 			entry.setId(informationSource.getId());
 			entry.setReportType(new ReportType("InformationSource"));
 			entry.setOwned(owner != null);
 			entry.setOwner(owner == null ? "[ NOT SET]" : owner.getId());
 			entry.setIdentifier(entry.getOwner() + " - " + ( informationSource.getPublicationYear() == null ? "[ YEAR UNKNOWN ]" : informationSource.getPublicationYear().toString() ) + " - " + MLSu.getEnglish(informationSource.getCitation()));
 
 			entry.getNameValueList().add(new NameValue("Committee", MLSu.getEnglish(informationSource.getCommittee())));
 			entry.getNameValueList().add(new NameValue("Citation", MLSu.getEnglish(informationSource.getCitation())));
 
 			results.add(entry);
 		}
 
 		return results.toArray(new ReportEntry[0]);
 	}
 
 	private ReportEntry[] listGeneralMeasure(Map<String, Object> criteria) {
 		Collection<GeneralMeasure> generalMeasures = this.vmeDao.filterEntities(this.vmeDao.getEm(), GeneralMeasure.class, criteria);
 
 		List<ReportEntry> results = new ArrayList<ReportEntry>();
 
 		ReportEntry entry;
 		Rfmo owner;
 		for(GeneralMeasure generalMeasure : generalMeasures) {
 			owner = generalMeasure.getRfmo();
 			
 			entry = new ReportEntry();
 			entry.setNameValueList(new ArrayList<NameValue>());
 
 			entry.setId(generalMeasure.getId());
 			entry.setReportType(new ReportType("GeneralMeasure"));
 			entry.setOwned(owner != null);
 			entry.setOwner(owner == null ? "[ NOT SET]" : owner.getId());
 			entry.setIdentifier(entry.getOwner() + " - " + generalMeasure.getYear());
 
 			entry.getNameValueList().add(new NameValue("Rfmo", owner == null ? "[ NOT SET]" : owner.getId()));
 			entry.getNameValueList().add(new NameValue("Year", generalMeasure.getYear() == null ? null : generalMeasure.getYear().toString()));
 			entry.getNameValueList().add(new NameValue("Validity start", generalMeasure.getValidityPeriod() == null || generalMeasure.getValidityPeriod().getBeginYear() == null ? null : generalMeasure.getValidityPeriod().getBeginYear().toString()));
 			entry.getNameValueList().add(new NameValue("Validity end", generalMeasure.getValidityPeriod() == null || generalMeasure.getValidityPeriod().getEndYear() == null ? null : generalMeasure.getValidityPeriod().getEndYear().toString()));
 
 			results.add(entry);
 		}
 
 		return results.toArray(new ReportEntry[0]);
 	}
 
 	private ReportEntry[] listFisheryAreasHistory(Map<String, Object> criteria) {
 		Collection<FisheryAreasHistory> fisheryAreasHistory = this.vmeDao.filterEntities(this.vmeDao.getEm(), FisheryAreasHistory.class, criteria);
 
 		List<ReportEntry> results = new ArrayList<ReportEntry>();
 
 		ReportEntry entry;
 		Rfmo owner;
 		for(FisheryAreasHistory fisheryAreaHistory : fisheryAreasHistory) {
 			owner = fisheryAreaHistory.getRfmo();
 			
 			entry = new ReportEntry();
 			entry.setNameValueList(new ArrayList<NameValue>());
 
 			entry.setId(fisheryAreaHistory.getId());
 			entry.setReportType(new ReportType("FisheryAreasHistory"));
 			entry.setOwned(owner != null);
 			entry.setOwner(owner == null ? "[ NOT SET]" : owner.getId());
 			entry.setIdentifier(entry.getOwner() + " - " + fisheryAreaHistory.getYear());
 
 			entry.getNameValueList().add(new NameValue("Year", fisheryAreaHistory.getYear() == null ? null : fisheryAreaHistory.getYear().toString()));
 			entry.getNameValueList().add(new NameValue("History", MLSu.getEnglish(fisheryAreaHistory.getHistory())));
 
 			results.add(entry);
 		}
 
 		return results.toArray(new ReportEntry[0]);
 	}
 
 	private ReportEntry[] listVMEsHistory(Map<String, Object> criteria) {
 		Collection<VMEsHistory> vmesHistory = this.vmeDao.filterEntities(this.vmeDao.getEm(), VMEsHistory.class, criteria);
 
 		List<ReportEntry> results = new ArrayList<ReportEntry>();
 
 		ReportEntry entry;
 		Rfmo owner;
 		for(VMEsHistory vmeHistory : vmesHistory) {
 			owner = vmeHistory.getRfmo();
 			
 			entry = new ReportEntry();
 			entry.setNameValueList(new ArrayList<NameValue>());
 
 			entry.setId(vmeHistory.getId());
 			entry.setReportType(new ReportType("VMEsHistory"));
 			entry.setOwned(owner != null);
 			entry.setOwner(owner == null ? "[ NOT SET]" : owner.getId());
 			entry.setIdentifier(entry.getOwner() + " - " + vmeHistory.getYear());
 
 			entry.getNameValueList().add(new NameValue("Year", vmeHistory.getYear() == null ? null : vmeHistory.getYear().toString()));
 			entry.getNameValueList().add(new NameValue("History", MLSu.getEnglish(vmeHistory.getHistory())));
 
 			results.add(entry);
 		}
 
 		return results.toArray(new ReportEntry[0]);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.gcube.application.rsg.service.RsgService#getEmptyReport(org.gcube.application.rsg.service.dto.ReportType)
 	 */
 	@Override
 	public CompiledReport getTemplate(ReportType reportType) {
 		Class<?> identifiedReport = this.getTemplateOfType(RSGReport.class, reportType);
 
 		try {
 			CompiledReport template = this._compiler.compile(identifiedReport);
 
 			template.setCreatedBy("[ NOT SET ]");
 			template.setCreationDate(new Date());
 			template.setLastEditedBy("[ NOT SET ]");
 			template.setLastEditingDate(new Date());
 
 //			this.dumpModel(template);
 
 			return template;
 		} catch (Throwable t) {
 			return null;
 		}
 	}
 
 	@Override
 	public CompiledReport getReportById(ReportType reportType, Object reportId) {
 		Class<?> identifiedReport = this.getTemplateOfType(RSGReport.class, reportType);
 
 		Object identified = null;
 
 		try {
 			Field id = ScanningUtils.getUniqueIdentifier(identifiedReport);
 			Class<?> idType = id.getType();
 
 			@SuppressWarnings("unchecked")
 			DataConverter<?> converter = ScanningUtils.isAnnotatedWith(id, RSGConverter.class) ? ScanningUtils.getAnnotation(id, RSGConverter.class).value().newInstance() : null;
 
 			if(converter != null)
 				reportId = converter.fromString((String)reportId);
 			//Fallback case
 			else if(Integer.class.equals(idType) || Long.class.equals(idType))
 				reportId = Long.valueOf(reportId.toString());
 
 			identified = this.vmeDao.getEntityById(this.vmeDao.getEm(), identifiedReport, reportId);
 		} catch(Throwable t) {
 			LOG.error("Unable to get entity of type {} with id {}", reportType.getTypeIdentifier(), reportId, t);
 		}
 
 		if(identified == null) {
 			LOG.warn("Unable to identify report of type {} with id {}", reportType.getTypeIdentifier(), reportId);
 
 			return null;
 		}
 
 		try {
 			CompiledReport report = this._evaluator.evaluate(this._compiler.compile(identifiedReport), identified);
 			report.setCreatedBy("[ NOT SET ]");
 			report.setCreationDate(new Date());
 			report.setLastEditedBy("[ NOT SET ]");
 			report.setLastEditingDate(new Date());
 
 //			this.dumpModel(report);
 
 			return report;
 		} catch (Throwable t) {
 			LOG.info("Unable to compile report of type {} with id {}: {} [ {} ]", new Object[] { reportType.getTypeIdentifier(), reportId, t.getClass().getSimpleName(), t.getMessage() });
 
 			return null;
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.gcube.application.rsg.service.RsgService#getRefReport(org.gcube.application.rsg.service.dto.ReportType, int)
 	 */
 	@Override
 	public CompiledReport getReferenceReportById(ReportType refReportType, Object refReportId) {
 		Class<?> identifiedReport = this.getTemplateOfType(RSGReferenceReport.class, refReportType);
 
 		Object identified = null;
 
 		try {
 			Field id = ScanningUtils.getUniqueIdentifier(identifiedReport);
 			Class<?> idType = id.getType();
 
 			@SuppressWarnings("unchecked")
 			DataConverter<?> converter = ScanningUtils.isAnnotatedWith(id, RSGConverter.class) ? ScanningUtils.getAnnotation(id, RSGConverter.class).value().newInstance() : null;
 
 			if(converter != null)
 				refReportId = converter.fromString((String)refReportId);
 			//Fallback case
 			else if(Integer.class.equals(idType) || Long.class.equals(idType))
 				refReportId = Long.valueOf(refReportId.toString());
 
 			identified = this.vmeDao.getEntityById(this.vmeDao.getEm(), identifiedReport, refReportId);
 		} catch(Throwable t) {
 			LOG.error("Unable to get entity of type {} with id {}", refReportType.getTypeIdentifier(), refReportId);
 		}
 
 		if(identified == null) {
 			LOG.warn("Unable to identify ref report of type {} with id {}", refReportType.getTypeIdentifier(), refReportId);
 
 			return null;
 		}
 
 		try {
 			CompiledReport report = this._compiler.compile(identifiedReport);
 			report.setIsAReference(true);
 
 			report = this._evaluator.evaluate(report, identified);
 			report.setCreatedBy("[ NOT SET ]");
 			report.setCreationDate(new Date());
 			report.setLastEditedBy("[ NOT SET ]");
 			report.setLastEditingDate(new Date());
 
 //			this.dumpModel(report);
 
 			return report;
 		} catch (Throwable t) {
 			LOG.info("Unable to compile ref report of type {} with id {}: {} [ {} ]", new Object[] { refReportType.getTypeIdentifier(), refReportId, t.getClass().getSimpleName(), t.getMessage() });
 
 			return null;
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.gcube.application.rsg.service.RsgService#getRefTemplate(org.gcube.application.rsg.service.dto.ReportType)
 	 */
 	@Override
 	public CompiledReport getRefTemplate(ReportType refReportType) {
 		Class<?> identifiedReport = this.getTemplateOfType(RSGReferenceReport.class, refReportType);
 
 		try {
 			return this._compiler.compile(identifiedReport);
 		} catch (Throwable t) {
 			return null;
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.gcube.application.rsg.service.RsgService#deleteById(org.gcube.application.rsg.service.dto.ReportType, java.lang.Object)
 	 */
 	@Override
 	public Response deleteById(ReportType reportType, Object reportId) {
 		LOG.info("Requesting deletion of {} report #{}", reportType.getTypeIdentifier(), reportId);
 
 		Response response = new Response();
 
 		Class<?> entity = this.getEntityByReportType(reportType);
 
 		EntityManager em = this.vmeDao.getEm();
 		EntityTransaction tx = em.getTransaction();
 
 		try {
 			tx.begin();
 
 			Object toDelete = this.vmeDao.getEntityById(this.vmeDao.getEm(), entity, reportId);
 
 			if(toDelete != null) {
 				this.vmeDao.delete(toDelete);
 
 				response.succeeded(entity.getName() + " report #" + reportId + " has been deleted");
 
 				LOG.info("{} report #{} has been deleted", entity.getName(), reportId);
 			} else {
 				LOG.warn("{} report #{} cannot be deleted as it doesn't exist", entity.getName(), reportId);
 
 				response.notSucceeded(entity.getName() + " report #" + reportId + " cannot be deleted as it doesn't exist");
 			}
 
 			tx.commit();
 		} catch (Throwable t) {
 			LOG.error("Unable to delete {} report #{}: {} [ {} ]", entity.getName(), reportId, t.getClass().getSimpleName(), t.getMessage());
 
			response.notSucceeded(t.getMessage()); //entity.getName() + " report #" + reportId + " cannot be deleted: " + t.getClass().getSimpleName() + " [ " + t.getMessage() + " ]");
 
 			response = this.handleRollback(response, tx);
 		}
 
 		return response.undeterminedIfNotSet();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.gcube.application.rsg.service.RsgService#deleteReferenceById(org.gcube.application.rsg.service.dto.ReportType, java.lang.Object)
 	 */
 	@Override
 	public Response deleteReferenceById(ReportType referenceReportType, Object refReportId) {
 		LOG.info("Requesting deletion of {} reference report #{}", referenceReportType.getTypeIdentifier(), refReportId);
 
 		Response response = new Response();
 
 		Class<?> entity = this.getEntityByReferenceReportType(referenceReportType);
 
 		EntityManager em = this.vmeDao.getEm();
 		EntityTransaction tx = em.getTransaction();
 
 		try {
 			tx.begin();
 
 			Object toDelete = this.vmeDao.getEntityById(this.vmeDao.getEm(), entity, refReportId);
 
 			if(toDelete != null) {
 				this.vmeDao.delete(toDelete);
 
 				response.succeeded(entity.getName() + " reference report #" + refReportId + " has been deleted");
 
 				LOG.info("{} reference report #{} has been deleted", entity.getName(), refReportId);
 			} else {
 				LOG.warn("{} reference report #{} cannot be deleted as it doesn't exist", entity.getName(), refReportId);
 
 				response.notSucceeded(entity.getName() + " report #" + refReportId + " cannot be deleted as it doesn't exist");
 			}
 
 			tx.commit();
 		} catch (Throwable t) {
 			LOG.error("Unable to delete {} report #{}: {} [ {} ]", entity.getName(), refReportId, t.getClass().getSimpleName(), t.getMessage());
 
			response.notSucceeded(t.getMessage());//entity.getName() + " report #" + refReportId + " cannot be deleted: " + t.getClass().getSimpleName() + " [ " + t.getMessage() + " ]");
 
 			response = this.handleRollback(response, tx);
 		}
 
 		return response.undeterminedIfNotSet();
 	}
 
 	private Class<?> getEntityByReportType(ReportType reportType) {
 		Class<?>[] availableReportTypes = new Class<?>[] {
 			Vme.class
 		};
 
 		return this.getEntityByType(availableReportTypes, reportType);
 	}
 
 	private Class<?> getEntityByReferenceReportType(ReportType reportType) {
 		Class<?>[] availableReferenceReportTypes = new Class<?>[] {
 			Rfmo.class,
 			GeneralMeasure.class,
 			InformationSource.class,
 			FisheryAreasHistory.class,
 			VMEsHistory.class
 		};
 
 		return this.getEntityByType(availableReferenceReportTypes, reportType);
 	}
 
 	private Class<?> getEntityByType(Class<?>[] availableTypes, ReportType reportType) {
 		if(availableTypes == null || availableTypes.length == 0)
 			return null;
 
 		final String typeName = reportType.getTypeIdentifier();
 
 		for(Class<?> type : availableTypes)
 			if(type.getSimpleName().equals(typeName))
 				return type;
 
 		LOG.warn("Unknown type {}", typeName);
 
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.gcube.application.rsg.service.RsgService#update(org.gcube.application.rsg.support.model.components.impl.CompiledReport)
 	 */
 	@Override
 	public Response update(CompiledReport report) {
 		this.vmeDao.getEm().clear();
 		
 		boolean isNew = report.getId() == null;
 
 		String id = isNew ? "#NEW#" : "#" + report.getId();
 
 		LOG.info("Requesting update of {} report {}", report.getType(), id);
 		
 		LOG.info("Report details:");
 		
 		try {
 			LOG.info(CompiledReportUtils.toXML(report));
 		} catch(Throwable t) {
 			LOG.warn("Unable to dump report to its XML format", t);
 		}
 
 		Response response = new Response();
 
 		EntityManager em = this.vmeDao.getEm();
 		EntityTransaction tx = em.getTransaction();
 
 		try {
 			tx.begin();
 
 			report.setEvaluated(true);
 			
 			Object toUpdate = this._evaluator.extract(report);
 						
 			Object updated = isNew ? this.doCreate(toUpdate) : this.doUpdate(toUpdate);
 
 			if(updated != null) {
 				response.succeeded(report.getType() + " report " + id + " has been updated");
 
 				LOG.info("{} report {} has been updated", report.getType(), id);
 			} else {
 				response.notSucceeded(report.getType() + " report " + id + " cannot be updated");
 
 				LOG.error("{} report {} cannot be updated", report.getType(), id);
 			}
 
 			tx.commit();
 		} catch (Throwable t) {
 			LOG.error("Unable to update {} report {}: {} [ {} ]", report.getType(), id, t.getClass().getSimpleName(), t.getMessage());
 
			response.notSucceeded(t.getMessage()); //report.getType() + " report " + id + " cannot be updated: " + t.getClass().getSimpleName() + " [ " + t.getMessage() + " ]");
 
 			response = this.handleRollback(response, tx);
 		}
 
 		return response.undeterminedIfNotSet();
 	}
 
 	private Object doCreate(Object holder) throws Throwable {
 		if(holder == null)
 			throw new IllegalArgumentException("Object to update cannot be NULL");
 
 		if(holder instanceof Vme) {
 			return this.vmeDao.create((Vme)holder);
 		} else
 			throw new IllegalArgumentException(holder.getClass() + " is not a valid model for a report");
 	}
 
 	private Object doUpdate(Object holder) throws Throwable {
 		if(holder == null)
 			throw new IllegalArgumentException("Object to update cannot be NULL");
 
 		if(holder instanceof Vme) {
 			return this.vmeDao.update((Vme)holder);
 		} else
 			throw new IllegalArgumentException(holder.getClass() + " is not a valid model for a report");
 	}
 
 	/* (non-Javadoc)
 	 * @see org.gcube.application.rsg.service.RsgService#updateRef(org.gcube.application.rsg.support.model.components.impl.CompiledReport)
 	 */
 	@Override
 	public Response updateRef(CompiledReport referenceReport) {
 		boolean isNew = referenceReport.getId() == null;
 
 		String id = isNew ? "#NEW#" : "#" + referenceReport.getId();
 
 		LOG.info("Requesting update of {} reference report {}", referenceReport.getType(), id);
 
 		LOG.info("Reference report details:");
 		
 		try {
 			LOG.info(CompiledReportUtils.toXML(referenceReport));
 		} catch(Throwable t) {
 			LOG.warn("Unable to dump reference report to its XML format", t);
 		}
 		
 		Response response = new Response();
 
 		EntityManager em = this.vmeDao.getEm();
 		EntityTransaction tx = em.getTransaction();
 
 		try {
 			tx.begin();
 
 			referenceReport.setEvaluated(true);
 
 			Object toUpdate = this._evaluator.extract(referenceReport);
 			Object updated = isNew ? this.doCreateReference(toUpdate) : this.doUpdateReference(toUpdate);
 
 			if(updated != null) {
 				response.succeeded(referenceReport.getType() + " reference report " + id + " has been updated");
 
 				LOG.info("{} reference report {} has been updated", referenceReport.getType(), id);
 			} else {
 				response.notSucceeded(referenceReport.getType() + " reference report " + id + " cannot be updated");
 
 				LOG.error("{} reference report {} cannot be updated", referenceReport.getType(), id);
 			}
 
 			tx.commit();
 		} catch (Throwable t) {
 			LOG.error("Unable to update {} reference report {}: {} [ {} ]", referenceReport.getType(), id, t.getClass().getSimpleName(), t.getMessage());
 
			response.notSucceeded(t.getMessage()); //referenceReport.getType() + " reference report " + id + " cannot be updated: " + t.getClass().getSimpleName() + " [ " + t.getMessage() + " ]");
 			
 			response = this.handleRollback(response, tx);
 		}
 
 		return response.undeterminedIfNotSet();
 	}
 
 	private Object doCreateReference(Object holder) throws Throwable {
 		if(holder == null)
 			throw new IllegalArgumentException("Object to create cannot be NULL");
 
 		if(holder instanceof GeneralMeasure)  
 			return this.vmeDao.create((GeneralMeasure)holder);
 		else if(holder instanceof InformationSource) 
 			return this.vmeDao.create((InformationSource)holder);
 		else if(holder instanceof FisheryAreasHistory) 
 			return this.vmeDao.create((FisheryAreasHistory)holder);
 		else if(holder instanceof VMEsHistory) 
 			return this.vmeDao.create((VMEsHistory)holder);
 		else
 			throw new IllegalArgumentException(holder.getClass() + " is not a valid model for a reference report");
 	}
 
 	private Object doUpdateReference(Object holder) throws Throwable {
 		if(holder == null)
 			throw new IllegalArgumentException("Object to update cannot be NULL");
 
 		if(holder instanceof GeneralMeasure) 
 			return this.vmeDao.update((GeneralMeasure)holder);
 		else if(holder instanceof InformationSource) 
 			return this.vmeDao.update((InformationSource)holder);
 		else if(holder instanceof FisheryAreasHistory) 
 			return this.vmeDao.update((FisheryAreasHistory)holder);
 		else if(holder instanceof VMEsHistory)
 			return this.vmeDao.update((VMEsHistory)holder);
 		else
 			throw new IllegalArgumentException(holder.getClass() + " is not a valid model for a reference report");
 	}
 
 	/* (non-Javadoc)
 	 * @see org.gcube.application.rsg.service.RsgService#validate(org.gcube.application.rsg.support.model.components.impl.CompiledReport)
 	 */
 	@Override
 	public Response validate(CompiledReport report) {
 		String id = report.getId() == null ? "#NEW#" : "#" + report.getId();
 
 		LOG.info("Requesting validation of {} report {}", report.getType(), id);
 
 		return new Response().valid("Report validation performed OK for " + report.getType() + " " + report.getId());
 	}
 
 	/* (non-Javadoc)
 	 * @see org.gcube.application.rsg.service.RsgService#validateRef(org.gcube.application.rsg.support.model.components.impl.CompiledReport)
 	 */
 	@Override
 	public Response validateRef(CompiledReport report) {
 		String id = report.getId() == null ? "#NEW#" : "#" + report.getId();
 
 		LOG.info("Requesting validation of {} reference report {}", report.getType(), id);
 
 		return new Response().valid("Reference report validation performed OK for " + report.getType() + " " + report.getId());
 	}
 }
