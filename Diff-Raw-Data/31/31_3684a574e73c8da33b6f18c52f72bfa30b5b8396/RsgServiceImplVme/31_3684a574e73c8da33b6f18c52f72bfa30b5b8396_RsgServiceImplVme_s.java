 package org.fao.fi.vme.rsg.service;
 
 import java.lang.annotation.Annotation;
 import java.util.List;
 
 import javax.inject.Inject;
 
 import org.gcube.application.rsg.service.RsgService;
 import org.gcube.application.rsg.service.dto.NameValue;
 import org.gcube.application.rsg.service.dto.ReportEntry;
 import org.gcube.application.rsg.service.dto.ReportType;
 import org.gcube.application.rsg.service.dto.response.Response;
 import org.gcube.application.rsg.service.util.RsgServiceUtil;
 import org.gcube.application.rsg.support.compiler.ReportCompiler;
 import org.gcube.application.rsg.support.compiler.annotations.Compiler;
 import org.gcube.application.rsg.support.compiler.annotations.Evaluator;
 import org.gcube.application.rsg.support.compiler.bridge.annotations.RSGReferenceReport;
 import org.gcube.application.rsg.support.compiler.bridge.annotations.RSGReport;
 import org.gcube.application.rsg.support.evaluator.ReportEvaluator;
 import org.gcube.application.rsg.support.model.components.impl.CompiledReport;
 import org.reflections.Reflections;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.vme.service.dao.sources.vme.VmeDao;
 
 /**
  * 
  * @author Erik van Ingen
  * 
  */

 public class RsgServiceImplVme implements RsgService {
 	final static private Logger LOG = LoggerFactory.getLogger(RsgServiceImplVme.class);
 	
 	private Reflections _reflections = new Reflections("org.fao.fi.vme.domain");
 	
 	@Inject @Compiler private ReportCompiler _compiler;
 	@Inject @Evaluator private ReportEvaluator _evaluator;
 	
 	@Inject VmeDao vmeDao;
 
	RsgServiceUtil u = new RsgServiceUtil();
 
 	private Class<?> findReport(Class<? extends Annotation> marker, ReportType reportType) {
 		for(Class<?> report : this._reflections.getTypesAnnotatedWith(marker))
 			if(report.getSimpleName().equals(reportType.getTypeIdentifier()))
 				return report;
 		
 		return null;
 	}
 	
 	@Override
 	public List<ReportType> getReportTypes() {
 		u.create();
 		
 		for(Class<?> report : this._reflections.getTypesAnnotatedWith(RSGReport.class))
 			u.add(report.getSimpleName());
 		
 		return u.getReportTypes();
 	}
 
 	@Override
 	public List<ReportType> getRefReportTypes() {
 		u.create();
 		
 		for(Class<?> report : this._reflections.getTypesAnnotatedWith(RSGReferenceReport.class))
 			u.add(report.getSimpleName());
 		
 		return u.getReportTypes();
 	}
 
 	@Override
 	public List<ReportEntry> listReports(ReportType reportType, NameValue... filters) {
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.gcube.application.rsg.service.RsgService#getEmptyReport(org.gcube.application.rsg.service.dto.ReportType)
 	 */
 	@Override
 	public CompiledReport getTemplate(ReportType reportType) {
 		Class<?> identifiedReport = this.findReport(RSGReport.class, reportType);
 			
 		try {
 			return this._compiler.compile(identifiedReport);
 		} catch (Throwable t) {
 			return null;
 		}
 	}
 	
 	@Override
 	public CompiledReport getReport(ReportType reportType, int reportId) {
 		Class<?> identifiedReport = this.findReport(RSGReport.class, reportType);
 			
		Object identified = this.vmeDao.getByID(this.vmeDao.getEm(), identifiedReport, reportId);
 
 		if(identified == null) {
 			LOG.warn("Unable to identify report of type {} with id {}", reportType.getTypeIdentifier(), reportId);
 			
 			return null;
 		}
 		
 		try {
 			return this._evaluator.evaluate(this._compiler.compile(identifiedReport), identified);
 		} catch (Throwable t) {
 			LOG.info("Unable to compile report of type {} with id {}: {} [ {} ]", new Object[] { reportType.getTypeIdentifier(), reportId, t.getClass().getSimpleName(), t.getMessage() });
 
 			return null;
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.gcube.application.rsg.service.RsgService#getRefReport(org.gcube.application.rsg.service.dto.ReportType, int)
 	 */
 	@Override
 	public CompiledReport getRefReport(ReportType refReportType, int refReportId) {
 		Class<?> identifiedReport = this.findReport(RSGReferenceReport.class, refReportType);
 			
		Object identified = this.vmeDao.getByID(this.vmeDao.getEm(), identifiedReport, refReportId);
 		
 		if(identified == null) {
 			LOG.warn("Unable to identify ref report of type {} with id {}", refReportType.getTypeIdentifier(), refReportId);
 			
 			return null;
 		}
 		
 		try {
 			return this._evaluator.evaluate(this._compiler.compile(identifiedReport), identified);
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
 		Class<?> identifiedReport = this.findReport(RSGReferenceReport.class, refReportType);
 			
 		try {
 			return this._compiler.compile(identifiedReport);
 		} catch (Throwable t) {
 			return null;
 		}
 	}
 	
 	@Override
 	public Response publishDelta(List<CompiledReport> modelList) {
 		return null;
 	}
 
 	@Override
 	public Response validateDelta(List<CompiledReport> modelList) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Response publishRef(CompiledReport refModel) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Response validateRef(CompiledReport refModel) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 }
