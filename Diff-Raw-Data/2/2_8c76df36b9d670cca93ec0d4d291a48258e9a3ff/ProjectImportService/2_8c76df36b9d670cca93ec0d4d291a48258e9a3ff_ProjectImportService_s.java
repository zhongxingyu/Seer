 /*
  * Copyright (c) Open Source Strategies, Inc.
  *
  * Opentaps is free software: you can redistribute it and/or modify it
  * under the terms of the GNU Affero General Public License as published
  * by the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Opentaps is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with Opentaps.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.opentaps.dataimport.domain;
 
 import java.util.List;
 import org.ofbiz.base.util.Debug;
 import java.util.Locale;
 import org.ofbiz.base.util.UtilDateTime;
 import org.opentaps.base.constants.StatusItemConstants;
 import org.opentaps.base.entities.DataImportProject;
 import org.opentaps.base.entities.PartyGroup;
 import org.opentaps.base.entities.WorkEffort;
 import org.opentaps.base.entities.WorkEffortPartyAssignment;
 import org.opentaps.domain.DomainService;
 import org.opentaps.domain.dataimport.ProjectDataImportRepositoryInterface;
 import org.opentaps.domain.dataimport.ProjectImportServiceInterface;
 import org.opentaps.domain.ledger.LedgerRepositoryInterface;
 import org.opentaps.foundation.entity.hibernate.Session;
 import org.opentaps.foundation.entity.hibernate.Transaction;
 import org.opentaps.foundation.infrastructure.Infrastructure;
 import org.opentaps.foundation.infrastructure.InfrastructureException;
 import org.opentaps.foundation.infrastructure.User;
 import org.opentaps.foundation.repository.RepositoryException;
 import org.opentaps.foundation.service.ServiceException;
 
 /**
  * Import Projects via intermediate DataImportProject entity. Author: Jesus
  * Rodrigo Ruiz Merlin
  */
 public class ProjectImportService extends DomainService implements
 		ProjectImportServiceInterface {
 
 	private static final String MODULE = ProjectImportService.class.getName();
 	// session object, using to store/search pojos.
 	private Session session;
 	public String organizationPartyId;
 	public int importedRecords;
 
 	public ProjectImportService() {
 		super();
 	}
 
 	public ProjectImportService(Infrastructure infrastructure, User user,
 			Locale locale) throws ServiceException {
 		super(infrastructure, user, locale);
 	}
 
 	/** {@inheritDoc} */
 	public int getImportedRecords() {
 		return importedRecords;
 	}
 
 	/** {@inheritDoc} */
 	public void importProject() throws ServiceException {
 		try {
 			this.session = this.getInfrastructure().getSession();
 
 			ProjectDataImportRepositoryInterface imp_repo = this
 					.getDomainsDirectory().getDataImportDomain()
 					.getProjectDataImportRepository();
 			LedgerRepositoryInterface ledger_repo = this.getDomainsDirectory()
 					.getLedgerDomain().getLedgerRepository();
 
 			List<DataImportProject> dataforimp = imp_repo
 					.findNotProcessesDataImportProjectEntries();
 
 			int imported = 0;
 			Transaction imp_tx1 = null;
 			Transaction imp_tx2 = null;
 			for (DataImportProject rowdata : dataforimp) {
 				// import projects as many as possible
 				try {
 					imp_tx1 = null;
 					imp_tx2 = null;
 
 					// begin importing row data item
 					WorkEffort project = new WorkEffort();
 					project.setWorkEffortTypeId(rowdata.getWorkEffortTypeId());
 					project.setWorkEffortId(rowdata.getWorkEffortName());
 					project.setWorkEffortName(rowdata.getWorkEffortName());
 					project.setDescription(rowdata.getDescription());
 					project.setNivelId(rowdata.getNivelId());
 					project.setExternalId(rowdata.getExternalId());
 					project.setWorkEffortParentId(rowdata.getWorkEffortParentId());
 					project.setNode(rowdata.getNode());
 					
 					imp_tx1 = this.session.beginTransaction();
 					ledger_repo.createOrUpdate(project);
 					imp_tx1.commit();
 
 					// begin partyID
 					if(rowdata.getGroupName()!=null || rowdata.getGroupName().equalsIgnoreCase("0")){
 						List<PartyGroup> parties = ledger_repo.findList(
 								PartyGroup.class, ledger_repo.map(
 										PartyGroup.Fields.groupName,
 										rowdata.getGroupName()));
 						WorkEffortPartyAssignment partyAssignment = new WorkEffortPartyAssignment();
 						partyAssignment.setWorkEffortId(rowdata
 								.getWorkEffortName());
 						partyAssignment.setPartyId(parties.get(0).getPartyId());
 						partyAssignment.setRoleTypeId("INTERNAL_ORGANIZATIO");
 						partyAssignment
 								.setFromDate(UtilDateTime.nowTimestamp());
 						imp_tx2 = this.session.beginTransaction();
 						ledger_repo.createOrUpdate(partyAssignment);
 						imp_tx2.commit();
 					}
 
 					String message = "Successfully imported General Ledger account ["
 							+ rowdata.getWorkEffortName() + "].";
 					this.storeImportProjectSuccess(rowdata, imp_repo);
 					Debug.logInfo(message, MODULE);
 
 					imported = imported + 1;
 
 				} catch (Exception ex) {
 					String message = "Failed to import General Ledger account ["
 							+ rowdata.getWorkEffortName()
 							+ "], Error message : " + ex.getMessage();
 					storeImportProjectError(rowdata, message, imp_repo);
 
 					// rollback all if there was an error when importing item
 					if (imp_tx1 != null) {
 						imp_tx1.rollback();
 					}
 					
 					if (imp_tx2 != null) {
 						imp_tx2.rollback();
 					}
 
 					Debug.logError(ex, message, MODULE);
 					throw new ServiceException(ex.getMessage());
 				}
 			}
 
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
 
 	/**
 	 * Helper method to store Project import success into
 	 * <code>DataImportProject</code> entity row.
 	 * 
 	 * @param rowdata
 	 *            item of <code>DataImportProject</code> entity that was
 	 *            successfully imported
 	 * @param imp_repo
 	 *            repository of accounting
 	 * @throws org.opentaps.foundation.repository.RepositoryException
 	 */
 	private void storeImportProjectSuccess(DataImportProject rowdata,
 			ProjectDataImportRepositoryInterface imp_repo)
 			throws RepositoryException {
 		// mark as success
 		rowdata.setImportStatusId(StatusItemConstants.Dataimport.DATAIMP_IMPORTED);
 		rowdata.setImportError(null);
 		rowdata.setProcessedTimestamp(UtilDateTime.nowTimestamp());
 		imp_repo.createOrUpdate(rowdata);
 	}
 
 	/**
 	 * Helper method to store GL account import error into
 	 * <code>DataImportGlAccount</code> entity row.
 	 * 
 	 * @param rowdata
 	 *            item of <code>DataImportProject</code> entity that was
 	 *            unsuccessfully imported
 	 * @param message
 	 *            error message
 	 * @param imp_repo
 	 *            repository of accounting
 	 * @throws org.opentaps.foundation.repository.RepositoryException
 	 */
 	private void storeImportProjectError(DataImportProject rowdata,
 			String message, ProjectDataImportRepositoryInterface imp_repo)
 			throws RepositoryException {
 		// store the exception and mark as failed
 		rowdata.setImportStatusId(StatusItemConstants.Dataimport.DATAIMP_FAILED);
 		rowdata.setImportError(message);
 		rowdata.setProcessedTimestamp(UtilDateTime.nowTimestamp());
 		imp_repo.createOrUpdate(rowdata);
 	}
 
 }
