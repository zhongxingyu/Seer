 package org.opentaps.dataimport.domain;
 
 import java.sql.Timestamp;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import java.util.Locale;
 import org.ofbiz.base.util.Debug;
 import org.ofbiz.base.util.UtilDateTime;
 import org.opentaps.base.constants.StatusItemConstants;
 import org.opentaps.base.entities.AcctgTrans;
 import org.opentaps.base.entities.AcctgTransEntry;
 import org.opentaps.base.entities.AcctgTransPresupuestal;
 import org.opentaps.base.entities.DataImportPresupuestoIngreso;
 import org.opentaps.base.entities.Enumeration;
 import org.opentaps.base.entities.Geo;
 import org.opentaps.base.entities.GlAccountOrganization;
 import org.opentaps.base.entities.Party;
 import org.opentaps.base.entities.PartyGroup;
 import org.opentaps.base.entities.ProductCategory;
 import org.opentaps.domain.DomainService;
 import org.opentaps.domain.dataimport.PresupuestoIngresoDataImportRepositoryInterface;
 import org.opentaps.domain.dataimport.PresupuestoIngresoImportServiceInterface;
 import org.opentaps.domain.ledger.LedgerRepositoryInterface;
 import org.opentaps.foundation.entity.hibernate.Session;
 import org.opentaps.foundation.entity.hibernate.Transaction;
 import org.opentaps.foundation.infrastructure.Infrastructure;
 import org.opentaps.foundation.infrastructure.InfrastructureException;
 import org.opentaps.foundation.infrastructure.User;
 import org.opentaps.foundation.repository.RepositoryException;
 import org.opentaps.foundation.service.ServiceException;
 
 public class PresupuestoIngresoImportService extends DomainService implements
 		PresupuestoIngresoImportServiceInterface {
 	private static final String MODULE = PresupuestoIngresoImportService.class
 			.getName();
 	// session object, using to store/search pojos.
 	private Session session;
 	public String organizationPartyId;
 	public int importedRecords;
 
 	public PresupuestoIngresoImportService() {
 		super();
 	}
 
 	public PresupuestoIngresoImportService(Infrastructure infrastructure,
 			User user, Locale locale) throws ServiceException {
 		super(infrastructure, user, locale);
 	}
 
 	/** {@inheritDoc} */
 	public void setOrganizationPartyId(String organizationPartyId) {
 		this.organizationPartyId = organizationPartyId;
 	}
 
 	/** {@inheritDoc} */
 	public int getImportedRecords() {
 		return importedRecords;
 	}
 
 	public Party validaParty(
 			PresupuestoIngresoDataImportRepositoryInterface imp_repo,
 			LedgerRepositoryInterface ledger_repo,
 			DataImportPresupuestoIngreso rowdata, String id, String campo)
 			throws RepositoryException {
 		List<Party> parties = ledger_repo.findList(Party.class,
 				ledger_repo.map(Party.Fields.externalId, id));
 		if (parties.isEmpty()) {
 			Debug.log("Error, " + campo + " no existe");
 			String message = "Failed to import Presupuesto Ingreso ["
 					+ rowdata.getClavePres() + "], Error message : " + campo
 					+ " no existe";
 			Debug.log(message);
 			Debug.log("despues de message");
 			storeImportPresupuestoIngresoError(rowdata, message, imp_repo);
 			throw new RepositoryException("Error, " + campo + " no existe");
 		}
 		PartyGroup pg = ledger_repo.findOne(PartyGroup.class, ledger_repo.map(
 				PartyGroup.Fields.partyId, parties.get(0).getPartyId()));
 
 		parties.get(0).setDescription(pg.getGroupName());
 		return parties.get(0);
 	}
 
 	public ProductCategory validaProduct(
 			PresupuestoIngresoDataImportRepositoryInterface imp_repo,
 			LedgerRepositoryInterface ledger_repo,
 			DataImportPresupuestoIngreso rowdata, String id, String tipo,
 			String campo) throws RepositoryException {
 		List<ProductCategory> products = ledger_repo.findList(
 				ProductCategory.class, ledger_repo.map(
 						ProductCategory.Fields.categoryName, id,
 						ProductCategory.Fields.productCategoryTypeId, tipo));
 		if (products.isEmpty()) {
 			Debug.log("Error, " + campo + " no existe");
 			String message = "Failed to import Presupuesto Ingreso ["
 					+ rowdata.getClavePres() + "], Error message : " + campo
 					+ " no existe";
 			Debug.log(message);
 			Debug.log("despues de message");
 			storeImportPresupuestoIngresoError(rowdata, message, imp_repo);
 			throw new RepositoryException("Error, " + campo + " no existe");
 		}
 
 		return products.get(0);
 	}
 
 	public Geo validaGeo(
 			PresupuestoIngresoDataImportRepositoryInterface imp_repo,
 			LedgerRepositoryInterface ledger_repo,
 			DataImportPresupuestoIngreso rowdata, String id, String campo)
 			throws RepositoryException {
 		Geo loc = ledger_repo.findOne(Geo.class,
				ledger_repo.map(Geo.Fields.geoId, rowdata.getLoc()));
 		if (loc == null) {
 			Debug.log("Error, " + campo + " no existe");
 			String message = "Failed to import Presupuesto Ingreso ["
 					+ rowdata.getClavePres() + "], Error message : " + campo
 					+ " no existe";
 			storeImportPresupuestoIngresoError(rowdata, message, imp_repo);
 			throw new RepositoryException("Error, " + campo + " no existe");
 		}
 		return loc;
 	}
 
 	public Enumeration validaEnumeration(
 			PresupuestoIngresoDataImportRepositoryInterface imp_repo,
 			LedgerRepositoryInterface ledger_repo,
 			DataImportPresupuestoIngreso rowdata, String id, String tipo,
 			String campo) throws RepositoryException {
 		List<Enumeration> enums = ledger_repo.findList(Enumeration.class,
 				ledger_repo.map(Enumeration.Fields.sequenceId, id,
 						Enumeration.Fields.enumTypeId, tipo));
 
 		if (enums.isEmpty()) {
 			Debug.log("Error, " + campo + " no existe");
 			String message = "Failed to import Presupuesto Ingreso ["
 					+ rowdata.getClavePres() + "], Error message : " + campo
 					+ " no existe";
 			storeImportPresupuestoIngresoError(rowdata, message, imp_repo);
 			throw new RepositoryException("Error, " + campo + " no existe");
 		}
 		return enums.get(0);
 	}
 
 	public void validaVigencia(
 			PresupuestoIngresoDataImportRepositoryInterface imp_repo,
 			DataImportPresupuestoIngreso rowdata, String campo,
 			Enumeration enumeration, Date fechaTrans)
 			throws RepositoryException {
 
 		if (!enumeration.getFechaInicio().before(fechaTrans)
 				|| !enumeration.getFechaFin().after(fechaTrans)) {
 			Debug.log("Error, " + campo + " no vigente");
 			String message = "Failed to import Presupuesto Ingreso ["
 					+ rowdata.getClavePres() + "], Error message : " + campo
 					+ " no vigente";
 			storeImportPresupuestoIngresoError(rowdata, message, imp_repo);
 			throw new RepositoryException("Error, " + campo + " no vigente");
 		}
 	}
 
 	/** {@inheritDoc} */
 	public void importPresupuestoIngreso() throws ServiceException {
 
 		try {
 			this.session = this.getInfrastructure().getSession();
 
 			PresupuestoIngresoDataImportRepositoryInterface imp_repo = this
 					.getDomainsDirectory().getDataImportDomain()
 					.getPresupuestoIngresoDataImportRepository();
 			LedgerRepositoryInterface ledger_repo = this.getDomainsDirectory()
 					.getLedgerDomain().getLedgerRepository();
 
 			List<DataImportPresupuestoIngreso> dataforimp = imp_repo
 					.findNotProcessesDataImportPresupuestoIngresoEntries();
 
 			int imported = 0;
 			Transaction imp_tx1 = null;
 			Transaction imp_tx2 = null;
 			Transaction imp_tx3 = null;
 			Transaction imp_tx4 = null;
 			for (DataImportPresupuestoIngreso rowdata : dataforimp) {
 				// Empieza bloque de validaciones
 				Debug.log("Empieza bloque de validaciones");
 
 				Party ur = validaParty(imp_repo, ledger_repo, rowdata,
 						rowdata.getUr(), "ur");
 				Party uo = validaParty(imp_repo, ledger_repo, rowdata,
 						rowdata.getUo(), "uo");
 				Party ue = validaParty(imp_repo, ledger_repo, rowdata,
 						rowdata.getUe(), "ue");
 				ProductCategory rub = validaProduct(imp_repo, ledger_repo,
 						rowdata, rowdata.getRub(), "RU", "rub");
 				ProductCategory tip = validaProduct(imp_repo, ledger_repo,
 						rowdata, rowdata.getTip(), "TI", "tip");
 				ProductCategory cla = validaProduct(imp_repo, ledger_repo,
 						rowdata, rowdata.getCla(), "CL", "cla");
 				ProductCategory con = validaProduct(imp_repo, ledger_repo,
 						rowdata, rowdata.getCon(), "CO", "con");
 				ProductCategory n5 = validaProduct(imp_repo, ledger_repo,
 						rowdata, rowdata.getN5(), "N5", "n5");
 				Geo ef = validaGeo(imp_repo, ledger_repo, rowdata,
 						rowdata.getEf(), "ef");
 				Geo reg = validaGeo(imp_repo, ledger_repo, rowdata,
 						rowdata.getReg(), "reg");
 				Geo mun = validaGeo(imp_repo, ledger_repo, rowdata,
 						rowdata.getMun(), "mun");
 				Geo loc = validaGeo(imp_repo, ledger_repo, rowdata,
 						rowdata.getLoc(), "loc");
 				Enumeration f = validaEnumeration(imp_repo, ledger_repo,
 						rowdata, rowdata.getF(), "CLAS_FR", "f");
 				Enumeration sf = validaEnumeration(imp_repo, ledger_repo,
 						rowdata, rowdata.getSf(), "CLAS_FR", "sf");
 				Enumeration sfe = validaEnumeration(imp_repo, ledger_repo,
 						rowdata, rowdata.getSfe(), "CLAS_FR", "sfe");
 
 				// import Presupuestos Ingreso as many as possible
 				try {
 					// id maximo
 					Debug.log("Busqueda idMax");
 					String id = ledger_repo.getNextSeqId("AcctgTrans");
 
 					for (int mes = 1; mes < 13; mes++) {
 						imp_tx1 = null;
 						imp_tx2 = null;
 						imp_tx3 = null;
 						imp_tx4 = null;
 						AcctgTrans presupuestoIngreso = new AcctgTrans();
 						presupuestoIngreso.setDescription(rowdata
 								.getClavePres() + "-" + mes);
 
 						// id Transaccion
 						List<AcctgTrans> trans = ledger_repo.findList(
 								AcctgTrans.class, ledger_repo.map(
 										AcctgTrans.Fields.description,
 										presupuestoIngreso.getDescription()));
 
 						if (trans.isEmpty()) {
 							Debug.log("Trans Nueva");
 							presupuestoIngreso.setAcctgTransId(id + " I"
 									+ rowdata.getCiclo() + "-" + mes);
 							presupuestoIngreso.setCreatedByUserLogin(rowdata
 									.getUsuario());
 						} else {
 							Debug.log("Trans Modif");
 							presupuestoIngreso.setAcctgTransId(trans.get(0)
 									.getAcctgTransId());
 							presupuestoIngreso.setCreatedByUserLogin(trans.get(
 									0).getCreatedByUserLogin());
 
 						}
 
 						Calendar cal = Calendar.getInstance();
 						cal.set(Calendar.MONTH, mes - 1);
 						cal.set(Calendar.DAY_OF_MONTH, 1);
 
 						// Vigencias
 						validaVigencia(imp_repo, rowdata, "sfe", sfe,
 								cal.getTime());
 
 						presupuestoIngreso.setTransactionDate(new Timestamp(cal
 								.getTimeInMillis()));
 						presupuestoIngreso.setIsPosted("Y");
 						presupuestoIngreso.setPostedDate(new Timestamp(cal
 								.getTimeInMillis()));
 						presupuestoIngreso.setGlFiscalTypeId("BUDGET");
 						presupuestoIngreso
 								.setAcctgTransTypeId("TINGRESOESTIMADO");
 						presupuestoIngreso.setLastModifiedByUserLogin(rowdata
 								.getUsuario());
 						presupuestoIngreso.setPartyId(ue.getPartyId());
 						switch (mes) {
 						case 1:
 							presupuestoIngreso.setPostedAmount(rowdata
 									.getEnero());
 							break;
 						case 2:
 							presupuestoIngreso.setPostedAmount(rowdata
 									.getFebrero());
 							break;
 						case 3:
 							presupuestoIngreso.setPostedAmount(rowdata
 									.getMarzo());
 							break;
 						case 4:
 							presupuestoIngreso.setPostedAmount(rowdata
 									.getAbril());
 							break;
 						case 5:
 							presupuestoIngreso.setPostedAmount(rowdata
 									.getMayo());
 							break;
 						case 6:
 							presupuestoIngreso.setPostedAmount(rowdata
 									.getJunio());
 							break;
 						case 7:
 							presupuestoIngreso.setPostedAmount(rowdata
 									.getJulio());
 							break;
 						case 8:
 							presupuestoIngreso.setPostedAmount(rowdata
 									.getAgosto());
 							break;
 						case 9:
 							presupuestoIngreso.setPostedAmount(rowdata
 									.getSeptiembre());
 							break;
 						case 10:
 							presupuestoIngreso.setPostedAmount(rowdata
 									.getOctubre());
 							break;
 						case 11:
 							presupuestoIngreso.setPostedAmount(rowdata
 									.getNoviembre());
 							break;
 						case 12:
 							presupuestoIngreso.setPostedAmount(rowdata
 									.getDiciembre());
 							break;
 						}
 						imp_tx1 = this.session.beginTransaction();
 						ledger_repo.createOrUpdate(presupuestoIngreso);
 						imp_tx1.commit();
 
 						// ACCTG_TRANS_PRESUPUESTAL
 						AcctgTransPresupuestal aux = new AcctgTransPresupuestal();
 						aux.setAcctgTransId(presupuestoIngreso
 								.getAcctgTransId());
 						aux.setCiclo(rowdata.getCiclo());
 						aux.setUnidadResponsable(ur.getPartyId());
 						aux.setDescripcionUr(ur.getDescription());
 						aux.setUnidadOrganizacional(uo.getPartyId());
 						aux.setDescripcionUo(uo.getDescription());
 						aux.setUnidadEjecutora(ue.getPartyId());
 						aux.setDescripcionUe(ue.getDescription());
 						aux.setRubro(rub.getProductCategoryId());
 						aux.setDescripcionRubro(rub.getDescription());
 						aux.setTipo(tip.getProductCategoryId());
 						aux.setDescripcionTipo(tip.getDescription());
 						aux.setClase(cla.getProductCategoryId());
 						aux.setDescripcionClase(cla.getDescription());
 						aux.setConceptoRub(con.getProductCategoryId());
 						aux.setDescripcionConceptoRubro(con.getDescription());
 						aux.setNivel5(n5.getProductCategoryId());
 						aux.setDescripcionN5(n5.getDescription());
 						aux.setFuente(f.getEnumId());
 						aux.setDescripcionFuente(f.getDescription());
 						aux.setSubFuente(sf.getEnumId());
 						aux.setDescripcionSubfuente(sf.getDescription());
 						aux.setSubFuenteEspecifica(sfe.getEnumId());
 						aux.setDescripcionSubfuenteEspecifica(sfe
 								.getDescription());
 						aux.setEntidadFederativa(rowdata.getEf());
 						aux.setDescripcionEntFed(ef.getGeoName());
 						aux.setRegion(rowdata.getReg());
 						aux.setDescripcionRegion(reg.getGeoName());
 						aux.setMunicipio(rowdata.getMun());
 						aux.setDescripcionMunicipio(mun.getGeoName());
 						aux.setLocalidad(rowdata.getLoc());
 						aux.setDescripcionLocalidad(loc.getGeoName());
 						aux.setAgrupador(rowdata.getAgrupador());
 						aux.setClavePres(rowdata.getClavePres());
 						imp_tx2 = this.session.beginTransaction();
 						ledger_repo.createOrUpdate(aux);
 						imp_tx2.commit();
 
 						// C/D
 						String seqId = "00001", flag = "D", cuenta = "8.1.1";
 
 						for (int j = 0; j < 2; j++) {
 							if (j != 0) {
 								seqId = "00002";
 								flag = "C";
 								cuenta = "8.1.2";
 							}
 							AcctgTransEntry acctgentry = new AcctgTransEntry();
 							acctgentry.setAcctgTransId(presupuestoIngreso
 									.getAcctgTransId());
 							acctgentry.setAcctgTransEntrySeqId(seqId);
 							acctgentry.setAcctgTransEntryTypeId("_NA_");
 							acctgentry.setDescription(presupuestoIngreso
 									.getDescription());
 							acctgentry.setGlAccountId(cuenta);
 							acctgentry
 									.setOrganizationPartyId(organizationPartyId);
 							acctgentry.setAmount(presupuestoIngreso
 									.getPostedAmount());
 							acctgentry.setCurrencyUomId("MXN");
 							acctgentry.setDebitCreditFlag(flag);
 							acctgentry
 									.setReconcileStatusId("AES_NOT_RECONCILED");
 							// Tags seteados.
 							acctgentry.setAcctgTagEnumId3(sfe.getEnumId());
 							imp_tx3 = this.session.beginTransaction();
 							ledger_repo.createOrUpdate(acctgentry);
 							imp_tx3.commit();
 
 							// GlAccountOrganization
 							Debug.log("Empieza GlAccountOrganization");
 							GlAccountOrganization glAccountOrganization = ledger_repo
 									.findOne(
 											GlAccountOrganization.class,
 											ledger_repo
 													.map(GlAccountOrganization.Fields.glAccountId,
 															cuenta,
 															GlAccountOrganization.Fields.organizationPartyId,
 															organizationPartyId));
 
 							if (glAccountOrganization.getPostedBalance() == null) {
 								glAccountOrganization
 										.setPostedBalance(presupuestoIngreso
 												.getPostedAmount());
 							} else {
 								glAccountOrganization
 										.setPostedBalance(glAccountOrganization
 												.getPostedBalance()
 												.add(presupuestoIngreso
 														.getPostedAmount()));
 							}
 							imp_tx4 = this.session.beginTransaction();
 							ledger_repo.createOrUpdate(glAccountOrganization);
 							imp_tx4.commit();
 						}
 					}
 
 					String message = "Successfully imported Presupuesto Ingreso ["
 							+ rowdata.getClavePres() + "].";
 					this.storeImportPresupuestoIngresoSuccess(rowdata, imp_repo);
 					Debug.logInfo(message, MODULE);
 					imported = imported + 1;
 				} catch (Exception ex) {
 					String message = "Failed to import Presupuesto Ingreso ["
 							+ rowdata.getClavePres() + "], Error message : "
 							+ ex.getMessage();
 					storeImportPresupuestoIngresoError(rowdata, message,
 							imp_repo);
 
 					// rollback all if there was an error when importing item
 					if (imp_tx1 != null) {
 						imp_tx1.rollback();
 					}
 					if (imp_tx2 != null) {
 						imp_tx2.rollback();
 					}
 					if (imp_tx3 != null) {
 						imp_tx3.rollback();
 					}
 					if (imp_tx4 != null) {
 						imp_tx4.rollback();
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
 	 * Helper method to store Presupuesto Ingreso import success into
 	 * <code>DataImportPresupuestoIngreso</code> entity row.
 	 * 
 	 * @param rowdata
 	 *            item of <code>DataImportPresupuestoIngreso</code> entity that
 	 *            was successfully imported
 	 * @param imp_repo
 	 *            repository of accounting
 	 * @throws org.opentaps.foundation.repository.RepositoryException
 	 */
 	private void storeImportPresupuestoIngresoSuccess(
 			DataImportPresupuestoIngreso rowdata,
 			PresupuestoIngresoDataImportRepositoryInterface imp_repo)
 			throws RepositoryException {
 		// mark as success
 		rowdata.setImportStatusId(StatusItemConstants.Dataimport.DATAIMP_IMPORTED);
 		rowdata.setImportError(null);
 		rowdata.setProcessedTimestamp(UtilDateTime.nowTimestamp());
 		imp_repo.createOrUpdate(rowdata);
 	}
 
 	/**
 	 * Helper method to store Presupuesto Ingreso import error into
 	 * <code>DataImportPresupuestoIngreso</code> entity row.
 	 * 
 	 * @param rowdata
 	 *            item of <code>DataImportPresupuestoIngreso</code> entity that
 	 *            was unsuccessfully imported
 	 * @param message
 	 *            error message
 	 * @param imp_repo
 	 *            repository of accounting
 	 * @throws org.opentaps.foundation.repository.RepositoryException
 	 */
 	private void storeImportPresupuestoIngresoError(
 			DataImportPresupuestoIngreso rowdata, String message,
 			PresupuestoIngresoDataImportRepositoryInterface imp_repo)
 			throws RepositoryException {
 		// store the exception and mark as failed
 		rowdata.setImportStatusId(StatusItemConstants.Dataimport.DATAIMP_FAILED);
 		Debug.log("message: " + message);
 		rowdata.setImportError(message);
 		Debug.log("ImportError:" + rowdata.getImportError());
 		rowdata.setProcessedTimestamp(UtilDateTime.nowTimestamp());
 		imp_repo.createOrUpdate(rowdata);
 	}
 
 }
