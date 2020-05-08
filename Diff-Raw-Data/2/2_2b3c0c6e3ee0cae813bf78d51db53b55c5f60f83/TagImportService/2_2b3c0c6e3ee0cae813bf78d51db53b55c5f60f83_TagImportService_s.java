 package org.opentaps.dataimport.domain;
 
 import java.util.List;
 import java.util.Locale;
 
 import org.apache.log4j.Logger;
 import org.ofbiz.base.util.Debug;
 import org.ofbiz.base.util.UtilDateTime;
 import org.opentaps.base.constants.StatusItemConstants;
 import org.opentaps.base.entities.DataImportTag;
 import org.opentaps.base.entities.Enumeration;
 import org.opentaps.base.entities.EnumerationType;
 import org.opentaps.dataimport.UtilImport;
 import org.opentaps.domain.DomainService;
 import org.opentaps.domain.dataimport.TagDataImportRepositoryInterface;
 import org.opentaps.domain.dataimport.TagImportServiceInterface;
 import org.opentaps.domain.ledger.LedgerRepositoryInterface;
 import org.opentaps.foundation.entity.hibernate.Session;
 import org.opentaps.foundation.entity.hibernate.Transaction;
 import org.opentaps.foundation.infrastructure.Infrastructure;
 import org.opentaps.foundation.infrastructure.InfrastructureException;
 import org.opentaps.foundation.infrastructure.User;
 import org.opentaps.foundation.repository.RepositoryException;
 import org.opentaps.foundation.service.ServiceException;
 
 public class TagImportService extends DomainService implements
 		TagImportServiceInterface {
 
 	private static final String MODULE = TagImportService.class.getName();
 	// session object, using to store/search pojos.
 	private Session session;
 	public int importedRecords;
 	static Logger logger = Logger.getLogger(TagImportService.class);
 
 	public TagImportService() {
 		super();
 	}
 
 	public TagImportService(Infrastructure infrastructure, User user,
 			Locale locale) throws ServiceException {
 		super(infrastructure, user, locale);
 	}
 
 	/** {@inheritDoc} */
 	public int getImportedRecords() {
 		return importedRecords;
 	}
 
 	/** {@inheritDoc} */
 	public void importTag() throws ServiceException {
 		try {
 
 			this.session = this.getInfrastructure().getSession();
 
 			TagDataImportRepositoryInterface imp_repo = this
 					.getDomainsDirectory().getDataImportDomain()
 					.getTagDataImportRepository();
 			LedgerRepositoryInterface ledger_repo = this.getDomainsDirectory()
 					.getLedgerDomain().getLedgerRepository();
 
 			List<DataImportTag> dataforimp = imp_repo
 					.findNotProcessesDataImportTags();
 
 			int imported = 0;
 			Transaction imp_tx1 = null;
 
 			for (int i = 0; i < dataforimp.size(); i++) {
 				DataImportTag rawdata = dataforimp.get(i);
 				// import accounts as many as possible
 				try {
 					imp_tx1 = null;
 					String id = ledger_repo.getNextSeqId("Enumeration");
 
 					// begin importing raw data item
 					Enumeration enumeration = new Enumeration();
 
 					if (rawdata.getSequenceNum() != null) {
 
 						List<EnumerationType> types = ledger_repo.findList(
 								EnumerationType.class, ledger_repo.map(
 										EnumerationType.Fields.enumTypeId,
 										rawdata.getType()));
 
 						// Buscar Parent Id del tag
 						List<Enumeration> enums1 = ledger_repo.findList(
 								Enumeration.class, ledger_repo.map(
 										Enumeration.Fields.sequenceId, rawdata
 												.getSequenceNum(),
 										Enumeration.Fields.enumTypeId, types
 												.get(0).getEnumTypeId()));
 
 						if (enums1.isEmpty()) {
 							Debug.log("Registro Nuevo");
 							enumeration.setEnumId(id);
 						} else {
 							enumeration.setEnumId(enums1.get(0).getEnumId());
 						}
 
 						enumeration.setEnumTypeId(types.get(0).getEnumTypeId());
 						enumeration.setSequenceId(rawdata.getSequenceNum());
 						enumeration.setEnumCode(rawdata.getName());
 						enumeration.setFechaInicio(rawdata.getFechaInicio());
 						enumeration.setFechaFin(rawdata.getFechaFin());
 						enumeration.setDescription(rawdata.getDescription());
 						enumeration.setNivelId(rawdata.getNivel());
 						enumeration.setNode(rawdata.getNode());
 
 						imp_tx1 = this.session.beginTransaction();
 						ledger_repo.createOrUpdate(enumeration);
 						imp_tx1.commit();
 
 					}
 
 					String message = "Successfully imported Tag ["
 							+ rawdata.getId() + "].";
 					this.storeImportTagSuccess(rawdata, imp_repo);
 					Debug.logInfo(message, MODULE);
 
 					imported = imported + 1;
 
 				} catch (Exception ex) {
 					String message = "Failed to import Tag [" + rawdata.getId()
 							+ "], Error message : " + ex.getMessage();
 					storeImportTagError(rawdata, message, imp_repo);
 
 					// rollback all if there was an error when importing item
 					if (imp_tx1 != null) {
 						imp_tx1.rollback();
 					}
 
 					Debug.logError(ex, message, MODULE);
 					throw new ServiceException(ex.getMessage());
 				}
 			}
 
 			// / relacionar ParentId a enum
			getParentEnumeration(ledger_repo, dataforimp);
 			this.importedRecords = imported;
 
 		} catch (InfrastructureException ex) {
 			Debug.logError(ex, MODULE);
 			throw new ServiceException(ex.getMessage());
 		} catch (RepositoryException ex) {
 			Debug.logError(ex, MODULE);
 			throw new ServiceException(ex.getMessage());
 		} finally {
 			if (session != null) {
 				session.close();
 			}
 		}
 	}
 
 	private void getParentEnumeration(LedgerRepositoryInterface ledger_repo,
 			List<DataImportTag> dataforimp,
 			TagDataImportRepositoryInterface imp_repo)
 			throws RepositoryException {
 
 		Transaction imp_tx1 = null;
 
 		try {
 
 			for (int i = 0; i < dataforimp.size(); i++) {
 
 				DataImportTag rowdata = dataforimp.get(i);
 
 				Enumeration enumeration = new Enumeration();
 
 				if (rowdata.getParentId() != null
 						&& rowdata.getSequenceNum() != null) {
 					List<Enumeration> enumId = getSequenceEnumeration(
 							ledger_repo, rowdata);
 
 					if (!enumId.isEmpty()) {
 						// Buscar Parent Id del tag
 						List<Enumeration> enums = ledger_repo.findList(
 								Enumeration.class, ledger_repo.map(
 										Enumeration.Fields.sequenceId,
 										rowdata.getParentId(),
 										Enumeration.Fields.enumTypeId,
 										rowdata.getType()));
 
 						// enumeration.setSequenceId();
 						enumeration.setEnumId(enumId.get(0).getEnumId());
 						if (UtilImport.validaPadreEnum(ledger_repo,
 								rowdata.getNivel(), rowdata.getParentId(),
 								rowdata.getType())) {
 							Debug.log("Padre valido");
 							enumeration.setParentEnumId(enums.get(0)
 									.getEnumId());
 						} else {
 							Debug.log("Padre no valido");
 							String message = "Failed to import Tag ["
 									+ rowdata.getSequenceNum()
 									+ "], Error message : " + "Padre no valido";
 							storeImportTagError(rowdata, message, imp_repo);
 
 							// rollback all if there was an error when importing
 							// item
 							if (imp_tx1 != null) {
 								imp_tx1.rollback();
 							}
 							// Debug.logError(ex, message, MODULE);
 							throw new ServiceException(message);
 						}
 
 						enumeration.setEnumTypeId(rowdata.getType());
 						enumeration.setSequenceId(rowdata.getSequenceNum());
 						enumeration.setEnumCode(rowdata.getName());
 						enumeration.setFechaInicio(rowdata.getFechaInicio());
 						enumeration.setFechaFin(rowdata.getFechaFin());
 						enumeration.setDescription(rowdata.getDescription());
 						enumeration.setNivelId(rowdata.getNivel());
 
 					}
 
 					imp_tx1 = this.session.beginTransaction();
 					ledger_repo.createOrUpdate(enumeration);
 					imp_tx1.commit();
 
 				}
 
 			}
 
 		} catch (Exception e) {
 			logger.debug("No se logro impactar parent " + e);
 		}
 	}
 
 	/**
 	 * @param ledger_repo
 	 * @param rowdata
 	 * @return
 	 * @throws RepositoryException
 	 */
 	private List<Enumeration> getSequenceEnumeration(
 			LedgerRepositoryInterface ledger_repo, DataImportTag rowdata)
 			throws RepositoryException {
 
 		logger.debug("entro a la funcion  getSequenceEnumeration "
 				+ rowdata.getSequenceNum() + ", " + rowdata.getType());
 		// Buscar si existe el registro para hacer update
 		List<Enumeration> sequence = ledger_repo.findList(Enumeration.class,
 				ledger_repo.map(Enumeration.Fields.sequenceId,
 						rowdata.getSequenceNum(),
 						Enumeration.Fields.enumTypeId, rowdata.getType()));
 
 		logger.debug("lista sequence " + sequence.size());
 		return sequence;
 	}
 
 	/**
 	 * Helper method to store GL account import succes into
 	 * <code>DataImportGlAccount</code> entity row.
 	 * 
 	 * @param rawdata
 	 *            item of <code>DataImportGlAccount</code> entity that was
 	 *            successfully imported
 	 * @param imp_repo
 	 *            repository of accounting
 	 * @throws org.opentaps.foundation.repository.RepositoryException
 	 */
 	private void storeImportTagSuccess(DataImportTag rawdata,
 			TagDataImportRepositoryInterface imp_repo)
 			throws RepositoryException {
 		// mark as success
 		rawdata.setImportStatusId(StatusItemConstants.Dataimport.DATAIMP_IMPORTED);
 		rawdata.setImportError(null);
 		rawdata.setProcessedTimestamp(UtilDateTime.nowTimestamp());
 		imp_repo.createOrUpdate(rawdata);
 	}
 
 	/**
 	 * Helper method to store GL account import error into
 	 * <code>DataImportGlAccount</code> entity row.
 	 * 
 	 * @param rawdata
 	 *            item of <code>DataImportGlAccount</code> entity that was
 	 *            unsuccessfully imported
 	 * @param message
 	 *            error message
 	 * @param imp_repo
 	 *            repository of accounting
 	 * @throws org.opentaps.foundation.repository.RepositoryException
 	 */
 	private void storeImportTagError(DataImportTag rawdata, String message,
 			TagDataImportRepositoryInterface imp_repo)
 			throws RepositoryException {
 		// store the exception and mark as failed
 		rawdata.setImportStatusId(StatusItemConstants.Dataimport.DATAIMP_FAILED);
 		rawdata.setImportError(message);
 		rawdata.setProcessedTimestamp(UtilDateTime.nowTimestamp());
 		imp_repo.createOrUpdate(rawdata);
 	}
 
 }
