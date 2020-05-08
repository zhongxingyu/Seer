 package org.opentaps.dataimport.domain;
 
 import java.sql.Timestamp;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 import org.ofbiz.base.util.Debug;
 import org.ofbiz.base.util.UtilDateTime;
 import org.opentaps.base.constants.StatusItemConstants;
 import org.opentaps.base.entities.AcctgTrans;
 import org.opentaps.base.entities.AcctgTransEntry;
 import org.opentaps.base.entities.AcctgTransPresupuestal;
 import org.opentaps.base.entities.DataImportIngresoDiario;
 import org.opentaps.base.entities.Enumeration;
 import org.opentaps.base.entities.Geo;
 import org.opentaps.base.entities.GlAccountOrganization;
 import org.opentaps.base.entities.Party;
 import org.opentaps.base.entities.ProductCategory;
 import org.opentaps.base.entities.TipoDocumento;
 import org.opentaps.dataimport.UtilImport;
 import org.opentaps.domain.DomainService;
 import org.opentaps.domain.dataimport.IngresoDiarioDataImportRepositoryInterface;
 import org.opentaps.domain.dataimport.IngresoDiarioImportServiceInterface;
 import org.opentaps.domain.ledger.LedgerRepositoryInterface;
 import org.opentaps.foundation.entity.hibernate.Session;
 import org.opentaps.foundation.entity.hibernate.Transaction;
 import org.opentaps.foundation.infrastructure.Infrastructure;
 import org.opentaps.foundation.infrastructure.InfrastructureException;
 import org.opentaps.foundation.infrastructure.User;
 import org.opentaps.foundation.repository.RepositoryException;
 import org.opentaps.foundation.service.ServiceException;
 
 import com.ibm.icu.util.Calendar;
 
 public class IngresoDiarioImportService extends DomainService implements
 		IngresoDiarioImportServiceInterface {
 	private static final String MODULE = IngresoDiarioImportService.class
 			.getName();
 	// session object, using to store/search pojos.
 	private Session session;
 	private String lote;
 	public int importedRecords;
 
 	public IngresoDiarioImportService() {
 		super();
 	}
 
 	public IngresoDiarioImportService(Infrastructure infrastructure, User user,
 			Locale locale) throws ServiceException {
 		super(infrastructure, user, locale);
 	}
 
 	/** {@inheritDoc} */
 	public void setLote(String lote) {
 		this.lote = lote;
 	}
 
 	/** {@inheritDoc} */
 	public int getImportedRecords() {
 		return importedRecords;
 	}
 
 	private void storeImportIngresoDiarioSuccess(
 			DataImportIngresoDiario rowdata,
 			IngresoDiarioDataImportRepositoryInterface imp_repo)
 			throws RepositoryException {
 		// mark as success
 		rowdata.setImportStatusId(StatusItemConstants.Dataimport.DATAIMP_IMPORTED);
 		rowdata.setImportError(null);
 		rowdata.setProcessedTimestamp(UtilDateTime.nowTimestamp());
 		imp_repo.createOrUpdate(rowdata);
 	}
 
 	private void storeImportIngresoDiarioError(DataImportIngresoDiario rowdata,
 			String message, IngresoDiarioDataImportRepositoryInterface imp_repo)
 			throws RepositoryException {
 		// store the exception and mark as failed
 		rowdata.setImportStatusId(StatusItemConstants.Dataimport.DATAIMP_FAILED);
 		rowdata.setImportError(message);
 		rowdata.setProcessedTimestamp(UtilDateTime.nowTimestamp());
 		imp_repo.createOrUpdate(rowdata);
 	}
 
 	/** {@inheritDoc} */
 	public void importIngresoDiario() throws ServiceException {
 		try {
 			this.session = this.getInfrastructure().getSession();
 			IngresoDiarioDataImportRepositoryInterface imp_repo = this
 					.getDomainsDirectory().getDataImportDomain()
 					.getIngresoDiarioDataImportRepository();
 			LedgerRepositoryInterface ledger_repo = this.getDomainsDirectory()
 					.getLedgerDomain().getLedgerRepository();
 
 			List<DataImportIngresoDiario> dataforimp = imp_repo
 					.findNotProcessesDataImportIngresoDiarioEntries();
 
 			int imported = 0;
 			Transaction imp_tx1 = null;
 			Transaction imp_tx2 = null;
 			Transaction imp_tx3 = null;
 			Transaction imp_tx4 = null;
 			Transaction imp_tx5 = null;
 			Transaction imp_tx6 = null;
 			Transaction imp_tx7 = null;
 			Transaction imp_tx8 = null;
 			Transaction imp_tx9 = null;
 			Transaction imp_tx10 = null;
 			Transaction imp_tx11 = null;
 			Transaction imp_tx12 = null;
 
 			for (DataImportIngresoDiario rowdata : dataforimp) {
 				// Empieza bloque de validaciones
 				String mensaje = "";
 				Debug.log("Empieza bloque de validaciones");
 				// mensaje = UtilImport.validaParty(mensaje, ledger_repo,
 				// rowdata.getUr(), "UR");
 				// mensaje = UtilImport.validaParty(mensaje, ledger_repo,
 				// rowdata.getUo(), "UO");
 				// mensaje = UtilImport.validaParty(mensaje, ledger_repo,
 				// rowdata.getUe(), "UE");
 				// mensaje = UtilImport.validaProductCategory(mensaje,
 				// ledger_repo, rowdata.getRub(), "RU", "RUB");
 				// mensaje = UtilImport.validaProductCategory(mensaje,
 				// ledger_repo, rowdata.getTip(), "TI", "TIP");
 				// mensaje = UtilImport.validaProductCategory(mensaje,
 				// ledger_repo, rowdata.getCla(), "CL", "CLA");
 				// mensaje = UtilImport.validaProductCategory(mensaje,
 				// ledger_repo, rowdata.getCon(), "CO", "CON");
 				// mensaje = UtilImport.validaProductCategory(mensaje,
 				// ledger_repo, rowdata.getN5(), "N5", "N5");
 				// mensaje = UtilImport.validaGeo(mensaje, ledger_repo,
 				// rowdata.getEf(), "EF");
 				// mensaje = UtilImport.validaGeo(mensaje, ledger_repo,
 				// rowdata.getReg(), "REG");
 				// mensaje = UtilImport.validaGeo(mensaje, ledger_repo,
 				// rowdata.getMun(), "MUN");
 				// mensaje = UtilImport.validaGeo(mensaje, ledger_repo,
 				// rowdata.getLoc(), "LOC");
 				// mensaje = UtilImport.validaEnumeration(mensaje, ledger_repo,
 				// rowdata.getF(), "CLAS_FR", "F");
 				// mensaje = UtilImport.validaEnumeration(mensaje, ledger_repo,
 				// rowdata.getSf(), "CLAS_FR", "SF");
 				// mensaje = UtilImport.validaEnumeration(mensaje, ledger_repo,
 				// rowdata.getSfe(), "CLAS_FR", "SFE");
 				mensaje = UtilImport.validaTipoDoc(mensaje, ledger_repo,
 						rowdata.getIdTipoDoc());
 				mensaje = UtilImport.validaCiclo(mensaje, rowdata.getCiclo(),
 						rowdata.getFechaContable());
 
 				mensaje = UtilImport.validaParty(mensaje, ledger_repo,
 						rowdata.getUe(), "ADMINISTRATIVA");
 				mensaje = UtilImport
 						.validaProductCategory(mensaje, ledger_repo,
 								rowdata.getN5(), "NIVEL_5_ING", "RUBRO DEL INGRESO");
 				mensaje = UtilImport.validaEnumeration(mensaje, ledger_repo,
 						rowdata.getSfe(), "CL_FUENTE_RECURSOS", "FUENTE DE LOS RECURSOS");
 				mensaje = UtilImport.validaGeo(mensaje, ledger_repo,
 						rowdata.getLoc(), "GEOGRAFICA");
 
 				if (!mensaje.isEmpty()) {
 					String message = "Failed to import Ingreso Diario ["
 							+ rowdata.getClavePres() + "], Error message : "
 							+ mensaje;
 					storeImportIngresoDiarioError(rowdata, message, imp_repo);
 					continue;
 				}
 
 				// Creacion de objetos
 				Debug.log("Empieza creacion de objetos");
 				// Party ur = UtilImport.obtenParty(ledger_repo,
 				// rowdata.getUr());
 				// Party uo = UtilImport.obtenParty(ledger_repo,
 				// rowdata.getUo());
 				// Party ue = UtilImport.obtenParty(ledger_repo,
 				// rowdata.getUe());
 				// ProductCategory rub = UtilImport.obtenProductCategory(
 				// ledger_repo, rowdata.getRub(), "RU");
 				// ProductCategory tip = UtilImport.obtenProductCategory(
 				// ledger_repo, rowdata.getTip(), "TI");
 				// ProductCategory cla = UtilImport.obtenProductCategory(
 				// ledger_repo, rowdata.getCla(), "CL");
 				// ProductCategory con = UtilImport.obtenProductCategory(
 				// ledger_repo, rowdata.getCon(), "CO");
 				// ProductCategory n5 = UtilImport.obtenProductCategory(
 				// ledger_repo, rowdata.getN5(), "N5");
 				// Geo ef = UtilImport.obtenGeo(ledger_repo, rowdata.getEf());
 				// Geo reg = UtilImport.obtenGeo(ledger_repo, rowdata.getReg());
 				// Geo mun = UtilImport.obtenGeo(ledger_repo, rowdata.getMun());
 				// Geo loc = UtilImport.obtenGeo(ledger_repo, rowdata.getLoc());
 				// Enumeration f = UtilImport.obtenEnumeration(ledger_repo,
 				// rowdata.getF(), "CLAS_FR");
 				// Enumeration sf = UtilImport.obtenEnumeration(ledger_repo,
 				// rowdata.getSf(), "CLAS_FR");
 				// Enumeration sfe = UtilImport.obtenEnumeration(ledger_repo,
 				// rowdata.getSfe(), "CLAS_FR");
 				TipoDocumento tipoDoc = UtilImport.obtenTipoDocumento(
 						ledger_repo, rowdata.getIdTipoDoc());
 
 				Party ue = UtilImport.obtenParty(ledger_repo, rowdata.getUe());
 				ProductCategory n5 = UtilImport.obtenProductCategory(
 						ledger_repo, rowdata.getN5(), "NIVEL_5_ING");
 				Enumeration sfe = UtilImport.obtenEnumeration(ledger_repo,
 						rowdata.getSfe(), "CL_FUENTE_RECURSOS");
 				Geo loc = UtilImport.obtenGeo(ledger_repo, rowdata.getLoc());
 
 				// Empieza bloque de vigencias
 				Debug.log("Empieza bloque de vigencias");
 				mensaje = UtilImport.validaVigencia(mensaje, "SFE", sfe,
 						rowdata.getFechaContable());
 
 				if (!mensaje.isEmpty()) {
 					String message = "Failed to import Ingreso Diario ["
 							+ rowdata.getClavePres() + "], Error message : "
 							+ mensaje;
 					storeImportIngresoDiarioError(rowdata, message, imp_repo);
 					continue;
 				}
 
 				// Obtenemos los padres de cada nivel.
 				String uo = UtilImport.obtenPadreParty(ledger_repo,
 						ue.getPartyId());
 				String ur = UtilImport.obtenPadreParty(ledger_repo, uo);
 				String con = UtilImport.obtenPadreProductCategory(ledger_repo,
 						n5.getProductCategoryId());
 				String cla = UtilImport.obtenPadreProductCategory(ledger_repo,
 						con);
 				String tip = UtilImport.obtenPadreProductCategory(ledger_repo,
 						cla);
 				String rub = UtilImport.obtenPadreProductCategory(ledger_repo,
 						tip);
 				String sf = UtilImport.obtenPadreEnumeration(ledger_repo,
 						sfe.getEnumId());
 				String f = UtilImport.obtenPadreEnumeration(ledger_repo, sf);
 				String mun = UtilImport.obtenPadreGeo(ledger_repo,
 						loc.getGeoId());
 				String reg = UtilImport.obtenPadreGeo(ledger_repo, mun);
 				String ef = UtilImport.obtenPadreGeo(ledger_repo, reg);
 
 				Debug.log("Motor Contable");
 				MotorContable motor = new MotorContable(ledger_repo);
 				// Map<String, String> cuentas = motor.cuentasDiarias(
 				// tipoDoc.getAcctgTransTypeId(), null, null,
 				// rowdata.getOrganizationPartyId(), null,
 				// n5.getProductCategoryId(), rowdata.getIdTipoCatalogo(),
 				// rowdata.getIdPago(), null, null, tip,
 				// false, null, null, rowdata.getIdProducto());
 				
 //				Map<String, String> cuentas = motor
 //						.cuentasIngresoDiario(tipoDoc.getAcctgTransTypeId(),
 //								rowdata.getOrganizationPartyId(),
 //								rowdata.getIdPago(), tip,
 //								rowdata.getIdProductoD(),
 //								rowdata.getIdProductoH());
 				
 				Map<String, String> cuentas = motor
 						.cuentasIngresoDiario(tipoDoc.getAcctgTransTypeId(),
 								rowdata.getOrganizationPartyId(),
 								rowdata.getIdPago(), n5.getProductCategoryId(),
 								rowdata.getIdProductoD(),
 								rowdata.getIdProductoH());
 				
 				
 				if (cuentas.get("Mensaje") != null) {
 					String message = "Failed to import Ingreso Diario ["
 							+ rowdata.getClavePres() + "], Error message : "
 							+ cuentas.get("Mensaje");
 					storeImportIngresoDiarioError(rowdata, message, imp_repo);
 					continue;
 				}
 
 				try {
 
 					imp_tx1 = null;
 					imp_tx2 = null;
 					imp_tx3 = null;
 					imp_tx4 = null;
 					imp_tx5 = null;
 					imp_tx6 = null;
 					imp_tx7 = null;
 					imp_tx8 = null;
 					imp_tx9 = null;
 					imp_tx10 = null;
 					imp_tx11 = null;
 					imp_tx12 = null;
 
 					AcctgTrans ingresoDiario = new AcctgTrans();
 					Calendar cal = Calendar.getInstance();
 					cal.setTime(rowdata.getFechaRegistro());
 					ingresoDiario.setTransactionDate(new Timestamp(cal
 							.getTimeInMillis()));
 					ingresoDiario.setIsPosted("Y");
 					cal.setTime(rowdata.getFechaContable());
 					ingresoDiario.setPostedDate(new Timestamp(cal
 							.getTimeInMillis()));
 					ingresoDiario.setAcctgTransTypeId(tipoDoc
 							.getAcctgTransTypeId());
 					ingresoDiario.setLastModifiedByUserLogin(rowdata
 							.getUsuario());
 					ingresoDiario.setPartyId(ue.getPartyId());
 					ingresoDiario.setPostedAmount(rowdata.getMonto());
 					ingresoDiario.setDescription(tipoDoc.getDescripcion() + "-"
 							+ rowdata.getRefDoc() + "-P");
 
 					// ACCTG_TRANS_PRESUPUESTAL
 					AcctgTransPresupuestal aux = new AcctgTransPresupuestal();
 					aux.setCiclo(rowdata.getCiclo());
 					aux.setUnidadResponsable(ur);
 					aux.setUnidadOrganizacional(uo);
 					aux.setUnidadEjecutora(ue.getPartyId());
 					aux.setRubro(rub);
 					aux.setTipo(tip);
 					aux.setClase(cla);
 					aux.setConceptoRub(con);
 					aux.setNivel5(n5.getProductCategoryId());
 					aux.setFuente(f);
 					aux.setSubFuente(sf);
 					aux.setSubFuenteEspecifica(sfe.getEnumId());
 					aux.setEntidadFederativa(ef);
 					aux.setRegion(reg);
 					aux.setMunicipio(mun);
 					aux.setLocalidad(loc.getGeoId());
 					aux.setAgrupador(rowdata.getRefDoc());
 					aux.setIdTipoDoc(rowdata.getIdTipoDoc());
 					aux.setSecuencia(rowdata.getSecuencia());
 					aux.setLote(lote);
 					aux.setClavePres(rowdata.getClavePres());
 
 					if (cuentas.get("Cuenta Cargo Presupuesto") != null) {
 						Debug.log("Cuenta Presupuestal");
 						ingresoDiario.setDescription(tipoDoc.getDescripcion()
 								+ "-" + rowdata.getRefDoc() + "-P");
 
 						// id Transaccion
 						ingresoDiario.setAcctgTransId(UtilImport
 								.getAcctgTransIdDiario(rowdata.getRefDoc(),
 										rowdata.getSecuencia(), "P"));
 
 						AcctgTrans trans = ledger_repo.findOne(
 								AcctgTrans.class, ledger_repo.map(
 										AcctgTrans.Fields.acctgTransId,
 										ingresoDiario.getAcctgTransId()));
 
 						if (trans != null) {
 							Debug.log("Trans Modif");
 							String message = "La transaccion con id: "
 									+ ingresoDiario.getAcctgTransId()
 									+ "ya existe.";
 							Debug.log(message);
 							storeImportIngresoDiarioError(rowdata, message,
 									imp_repo);
 							continue;
 						}
 
 						Debug.log("Trans Nueva");
 						ingresoDiario.setCreatedByUserLogin(rowdata
 								.getUsuario());
 
 						ingresoDiario.setGlFiscalTypeId(cuentas
 								.get("GlFiscalTypePresupuesto"));
 						imp_tx1 = this.session.beginTransaction();
 						ledger_repo.createOrUpdate(ingresoDiario);
 						imp_tx1.commit();
 
 						aux.setAcctgTransId(ingresoDiario.getAcctgTransId());
 						imp_tx3 = this.session.beginTransaction();
 						ledger_repo.createOrUpdate(aux);
 						imp_tx3.commit();
 
 						AcctgTransEntry acctgentry = UtilImport
 								.generaAcctgTransEntry(
 										ingresoDiario,
 										rowdata.getOrganizationPartyId(),
 										"00001",
 										"D",
 										cuentas.get("Cuenta Cargo Presupuesto"),
 										sfe.getEnumId());
 						imp_tx5 = this.session.beginTransaction();
 						ledger_repo.createOrUpdate(acctgentry);
 						imp_tx5.commit();
 
 						GlAccountOrganization glAccountOrganization = UtilImport
 								.actualizaGlAccountOrganization(
 										ledger_repo,
 										rowdata.getMonto(),
 										cuentas.get("Cuenta Cargo Presupuesto"),
 										rowdata.getOrganizationPartyId());
 						imp_tx7 = this.session.beginTransaction();
 						ledger_repo.createOrUpdate(glAccountOrganization);
 						imp_tx7.commit();
 
 						acctgentry = UtilImport.generaAcctgTransEntry(
 								ingresoDiario,
 								rowdata.getOrganizationPartyId(), "00002", "C",
 								cuentas.get("Cuenta Abono Presupuesto"),
 								sfe.getEnumId());
 						imp_tx9 = this.session.beginTransaction();
 						ledger_repo.createOrUpdate(acctgentry);
 						imp_tx9.commit();
 
 						glAccountOrganization = UtilImport
 								.actualizaGlAccountOrganization(
 										ledger_repo,
 										rowdata.getMonto(),
 										cuentas.get("Cuenta Abono Presupuesto"),
 										rowdata.getOrganizationPartyId());
 						imp_tx11 = this.session.beginTransaction();
 						ledger_repo.createOrUpdate(glAccountOrganization);
 						imp_tx11.commit();
 
 					}
 
 					if (cuentas.get("Cuenta Cargo Contable") != null) {
 						Debug.log("Cuenta Contable");
 						ingresoDiario.setDescription(tipoDoc.getDescripcion()
 								+ "-" + rowdata.getRefDoc() + "-C");
 
 						// id Transaccion
 						ingresoDiario.setAcctgTransId(UtilImport
 								.getAcctgTransIdDiario(rowdata.getRefDoc(),
 										rowdata.getSecuencia(), "C"));
 
 						AcctgTrans trans = ledger_repo.findOne(
 								AcctgTrans.class, ledger_repo.map(
 										AcctgTrans.Fields.acctgTransId,
 										ingresoDiario.getAcctgTransId()));
 
 						if (trans != null) {
 							Debug.log("Trans Modif");
 							String message = "La transaccion con id: "
 									+ ingresoDiario.getAcctgTransId()
 									+ "ya existe.";
 							Debug.log(message);
 							storeImportIngresoDiarioError(rowdata, message,
 									imp_repo);
 							continue;
 						}
 
 						Debug.log("Trans Nueva");
 						ingresoDiario.setCreatedByUserLogin(rowdata
 								.getUsuario());
 
 						ingresoDiario.setGlFiscalTypeId(cuentas
 								.get("GlFiscalTypeContable"));
 						imp_tx2 = this.session.beginTransaction();
 						ledger_repo.createOrUpdate(ingresoDiario);
 						imp_tx2.commit();
 
 						aux.setAcctgTransId(ingresoDiario.getAcctgTransId());
 						imp_tx4 = this.session.beginTransaction();
 						ledger_repo.createOrUpdate(aux);
 						imp_tx4.commit();
 
 						AcctgTransEntry acctgentry = UtilImport
 								.generaAcctgTransEntry(ingresoDiario,
 										rowdata.getOrganizationPartyId(),
 										"00001", "D",
 										cuentas.get("Cuenta Cargo Contable"),
 										sfe.getEnumId());
 						imp_tx6 = this.session.beginTransaction();
 						ledger_repo.createOrUpdate(acctgentry);
 						imp_tx6.commit();
 
 						GlAccountOrganization glAccountOrganization = UtilImport
 								.actualizaGlAccountOrganization(ledger_repo,
 										rowdata.getMonto(),
 										cuentas.get("Cuenta Cargo Contable"),
 										rowdata.getOrganizationPartyId());
 						imp_tx8 = this.session.beginTransaction();
 						ledger_repo.createOrUpdate(glAccountOrganization);
 						imp_tx8.commit();
 
 						acctgentry = UtilImport.generaAcctgTransEntry(
 								ingresoDiario,
 								rowdata.getOrganizationPartyId(), "00002", "C",
 								cuentas.get("Cuenta Abono Contable"),
 								sfe.getEnumId());
 						imp_tx10 = this.session.beginTransaction();
 						ledger_repo.createOrUpdate(acctgentry);
 						imp_tx10.commit();
 
 						glAccountOrganization = UtilImport
 								.actualizaGlAccountOrganization(ledger_repo,
 										rowdata.getMonto(),
 										cuentas.get("Cuenta Abono Contable"),
 										rowdata.getOrganizationPartyId());
 						imp_tx12 = this.session.beginTransaction();
 						ledger_repo.createOrUpdate(glAccountOrganization);
 						imp_tx12.commit();
 					}
 
 					if (mensaje.isEmpty()) {
 						String message = "Successfully imported Ingreso Diario ["
 								+ rowdata.getClavePres() + "].";
 						this.storeImportIngresoDiarioSuccess(rowdata, imp_repo);
 						Debug.logInfo(message, MODULE);
 						imported = imported + 1;
 					}
 
 				} catch (Exception ex) {
 					String message = "Failed to import Ingreso Diario ["
 							+ rowdata.getClavePres() + "], Error message : "
 							+ ex.getMessage();
 					storeImportIngresoDiarioError(rowdata, message, imp_repo);
 
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
 					if (imp_tx5 != null) {
 						imp_tx5.rollback();
 					}
 					if (imp_tx6 != null) {
 						imp_tx6.rollback();
 					}
 					if (imp_tx7 != null) {
 						imp_tx7.rollback();
 					}
 					if (imp_tx8 != null) {
 						imp_tx8.rollback();
 					}
 					if (imp_tx9 != null) {
 						imp_tx9.rollback();
 					}
 					if (imp_tx10 != null) {
 						imp_tx10.rollback();
 					}
 					if (imp_tx11 != null) {
 						imp_tx11.rollback();
 					}
 					if (imp_tx12 != null) {
 						imp_tx12.rollback();
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
 }
