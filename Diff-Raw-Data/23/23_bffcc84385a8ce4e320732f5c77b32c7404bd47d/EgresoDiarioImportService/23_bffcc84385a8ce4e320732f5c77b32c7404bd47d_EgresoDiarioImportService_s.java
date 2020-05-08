 package org.opentaps.dataimport.domain;
 
 import java.sql.Timestamp;
 import java.util.Date;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 import org.ofbiz.base.util.Debug;
 import org.ofbiz.base.util.UtilDateTime;
 import org.opentaps.base.constants.StatusItemConstants;
 import org.opentaps.base.entities.AcctgTrans;
 import org.opentaps.base.entities.AcctgTransEntry;
 import org.opentaps.base.entities.AcctgTransPresupuestal;
 import org.opentaps.base.entities.AcctgTransType;
 import org.opentaps.base.entities.DataImportEgresoDiario;
 import org.opentaps.base.entities.Enumeration;
 import org.opentaps.base.entities.Geo;
 import org.opentaps.base.entities.GlAccountOrganization;
 import org.opentaps.base.entities.Party;
 import org.opentaps.base.entities.PartyGroup;
 import org.opentaps.base.entities.PaymentMethod;
 import org.opentaps.base.entities.ProductCategory;
 import org.opentaps.base.entities.TipoDocumento;
 import org.opentaps.base.entities.WorkEffort;
 import org.opentaps.domain.DomainService;
 import org.opentaps.domain.dataimport.EgresoDiarioDataImportRepositoryInterface;
 import org.opentaps.domain.dataimport.EgresoDiarioImportServiceInterface;
 import org.opentaps.domain.ledger.LedgerRepositoryInterface;
 import org.opentaps.foundation.entity.hibernate.Session;
 import org.opentaps.foundation.entity.hibernate.Transaction;
 import org.opentaps.foundation.infrastructure.Infrastructure;
 import org.opentaps.foundation.infrastructure.InfrastructureException;
 import org.opentaps.foundation.infrastructure.User;
 import org.opentaps.foundation.repository.RepositoryException;
 import org.opentaps.foundation.service.ServiceException;
 
 import com.ibm.icu.util.Calendar;
 
 public class EgresoDiarioImportService extends DomainService implements
 		EgresoDiarioImportServiceInterface {
 	private static final String MODULE = EgresoDiarioImportService.class
 			.getName();
 	// session object, using to store/search pojos.
 	private Session session;
 	public int importedRecords;
 
 	public EgresoDiarioImportService() {
 		super();
 	}
 
 	public EgresoDiarioImportService(Infrastructure infrastructure, User user,
 			Locale locale) throws ServiceException {
 		super(infrastructure, user, locale);
 	}
 
 	/** {@inheritDoc} */
 	public int getImportedRecords() {
 		return importedRecords;
 	}
 
 	public Party validaParty(
 			EgresoDiarioDataImportRepositoryInterface imp_repo,
 			LedgerRepositoryInterface ledger_repo,
 			DataImportEgresoDiario rowdata, String id, String campo)
 			throws RepositoryException {
 		List<Party> parties = ledger_repo.findList(Party.class,
 				ledger_repo.map(Party.Fields.externalId, id));
 		if (parties.isEmpty()) {
 			Debug.log("Error, " + campo + " no existe");
 			String message = "Failed to import Egreso Diario ["
 					+ rowdata.getClavePres() + "], Error message : " + campo
 					+ " no existe";
 			Debug.log(message);
 			Debug.log("despues de message");
 			storeImportEgresoDiarioError(rowdata, message, imp_repo);
 			throw new RepositoryException("Error, " + campo + " no existe");
 		}
 		PartyGroup pg = ledger_repo.findOne(PartyGroup.class, ledger_repo.map(
 				PartyGroup.Fields.partyId, parties.get(0).getPartyId()));
 
 		parties.get(0).setDescription(pg.getGroupName());
 		return parties.get(0);
 	}
 
 	public WorkEffort validaWorkEffort(
 			EgresoDiarioDataImportRepositoryInterface imp_repo,
 			LedgerRepositoryInterface ledger_repo,
 			DataImportEgresoDiario rowdata, String id, String campo)
 			throws RepositoryException {
 		WorkEffort act = ledger_repo.findOne(WorkEffort.class,
 				ledger_repo.map(WorkEffort.Fields.workEffortId, id));
 		if (act == null) {
 			Debug.log("Error, " + campo + " no existe");
 			String message = "Failed to import Egreso Diario ["
 					+ rowdata.getClavePres() + "], Error message : " + campo
 					+ " no existe";
 			storeImportEgresoDiarioError(rowdata, message, imp_repo);
 			throw new RepositoryException("Error, " + campo + " no existe");
 		}
 		return act;
 	}
 
 	private ProductCategory validaProduct(
 			EgresoDiarioDataImportRepositoryInterface imp_repo,
 			LedgerRepositoryInterface ledger_repo,
 			DataImportEgresoDiario rowdata, String id, String tipo, String campo)
 			throws RepositoryException {
 		List<ProductCategory> products = ledger_repo.findList(
 				ProductCategory.class, ledger_repo.map(
 						ProductCategory.Fields.categoryName, id,
 						ProductCategory.Fields.productCategoryTypeId, tipo));
 		if (products.isEmpty()) {
 			Debug.log("Error, " + campo + " no existe");
 			String message = "Failed to import Egreso Diario ["
 					+ rowdata.getClavePres() + "], Error message : " + campo
 					+ " no existe";
 			Debug.log(message);
 			Debug.log("despues de message");
 			storeImportEgresoDiarioError(rowdata, message, imp_repo);
 			throw new RepositoryException("Error, " + campo + " no existe");
 		}
 
 		return products.get(0);
 	}
 
 	public Geo validaGeo(EgresoDiarioDataImportRepositoryInterface imp_repo,
 			LedgerRepositoryInterface ledger_repo,
 			DataImportEgresoDiario rowdata, String id, String campo)
 			throws RepositoryException {
 		Geo loc = ledger_repo.findOne(Geo.class,
				ledger_repo.map(Geo.Fields.geoId, rowdata.getLoc()));
 		if (loc == null) {
 			Debug.log("Error, " + campo + " no existe");
 			String message = "Failed to import Egreso Diario ["
 					+ rowdata.getClavePres() + "], Error message : " + campo
 					+ " no existe";
 			storeImportEgresoDiarioError(rowdata, message, imp_repo);
 			throw new RepositoryException("Error, " + campo + " no existe");
 		}
 		return loc;
 	}
 
 	private Enumeration validaEnumeration(
 			EgresoDiarioDataImportRepositoryInterface imp_repo,
 			LedgerRepositoryInterface ledger_repo,
 			DataImportEgresoDiario rowdata, String id, String tipo, String campo)
 			throws RepositoryException {
 		List<Enumeration> enums = ledger_repo.findList(Enumeration.class,
 				ledger_repo.map(Enumeration.Fields.sequenceId, id,
 						Enumeration.Fields.enumTypeId, tipo));
 
 		if (enums.isEmpty()) {
 			Debug.log("Error, " + campo + " no existe");
 			String message = "Failed to import Egreso Diario ["
 					+ rowdata.getClavePres() + "], Error message : " + campo
 					+ " no existe";
 			storeImportEgresoDiarioError(rowdata, message, imp_repo);
 			throw new RepositoryException("Error, " + campo + " no existe");
 		}
 		return enums.get(0);
 	}
 
 	private void validaVigencia(
 			EgresoDiarioDataImportRepositoryInterface imp_repo,
 			DataImportEgresoDiario rowdata, String campo,
 			Enumeration enumeration, Date fechaTrans)
 			throws RepositoryException {
 
 		if (!enumeration.getFechaInicio().before(fechaTrans)
 				|| !enumeration.getFechaFin().after(fechaTrans)) {
 			Debug.log("Error, " + campo + " no vigente");
 			String message = "Failed to import Egreso Diario ["
 					+ rowdata.getClavePres() + "], Error message : " + campo
 					+ " no vigente";
 			storeImportEgresoDiarioError(rowdata, message, imp_repo);
 			throw new RepositoryException("Error, " + campo + " no vigente");
 		}
 	}
 
 	// private void validaTipoTrans(
 	// EgresoDiarioDataImportRepositoryInterface imp_repo,
 	// LedgerRepositoryInterface ledger_repo,
 	// DataImportEgresoDiario rowdata, String tipo)
 	// throws RepositoryException {
 	// AcctgTransType type = ledger_repo.findOne(AcctgTransType.class,
 	// ledger_repo.map(AcctgTransType.Fields.acctgTransTypeId, tipo));
 	// if (type == null) {
 	// Debug.log("Error, tipoTrans no existe");
 	// String message = "Failed to import Egreso Diario ["
 	// + rowdata.getClavePres() + "], Error message : "
 	// + "tipoTrans no existe";
 	// storeImportEgresoDiarioError(rowdata, message, imp_repo);
 	// throw new RepositoryException("Error, tipoTrans no existe");
 	// }
 	// }
 
 	private TipoDocumento validaTipoDoc(
 			EgresoDiarioDataImportRepositoryInterface imp_repo,
 			LedgerRepositoryInterface ledger_repo,
 			DataImportEgresoDiario rowdata, String tipo)
 			throws RepositoryException {
 		TipoDocumento type = ledger_repo.findOne(TipoDocumento.class,
 				ledger_repo.map(TipoDocumento.Fields.idTipoDoc, tipo));
 		if (type == null) {
 			Debug.log("Error, tipoDoc no existe");
 			String message = "Failed to import Egreso Diario ["
 					+ rowdata.getClavePres() + "], Error message : "
 					+ "tipoDoc no existe";
 			storeImportEgresoDiarioError(rowdata, message, imp_repo);
 			throw new RepositoryException("Error, tipoDoc no existe");
 		}
 		return type;
 	}
 
 	private void validaPago(EgresoDiarioDataImportRepositoryInterface imp_repo,
 			LedgerRepositoryInterface ledger_repo,
 			DataImportEgresoDiario rowdata, String tipo)
 			throws RepositoryException {
 		PaymentMethod payment = ledger_repo.findOne(PaymentMethod.class,
 				ledger_repo.map(PaymentMethod.Fields.paymentMethodId, tipo));
 		if (payment == null) {
 			Debug.log("Error, idPago no existe");
 			String message = "Failed to import Egreso Diario ["
 					+ rowdata.getClavePres() + "], Error message : "
 					+ "idPago no existe";
 			storeImportEgresoDiarioError(rowdata, message, imp_repo);
 			throw new RepositoryException("Error, idPago no existe");
 		}
 	}
 
 	private AcctgTransEntry generaAcctgTransEntry(AcctgTrans transaccion,
 			DataImportEgresoDiario rowdata, String seqId, String flag,
 			String cuenta, String sfeId) {
 		// C/D
 		Debug.log("Empieza AcctgTransEntry " + cuenta);
 
 		AcctgTransEntry acctgentry = new AcctgTransEntry();
 		acctgentry.setAcctgTransId(transaccion.getAcctgTransId());
 		acctgentry.setAcctgTransEntrySeqId(seqId);
 		acctgentry.setAcctgTransEntryTypeId("_NA_");
 		acctgentry.setDescription(acctgentry.getDescription());
 		acctgentry.setGlAccountId(cuenta);
 		acctgentry.setOrganizationPartyId(rowdata.getOrganizationPartyId());
 		acctgentry.setAmount(transaccion.getPostedAmount());
 		acctgentry.setCurrencyUomId("MXN");
 		acctgentry.setDebitCreditFlag(flag);
 		acctgentry.setReconcileStatusId("AES_NOT_RECONCILED");
 		// Tags seteados.
 		acctgentry.setAcctgTagEnumId3(sfeId);
 		return acctgentry;
 	}
 
 	private GlAccountOrganization actualizaGlAccountOrganization(
 			LedgerRepositoryInterface ledger_repo,
 			DataImportEgresoDiario rowdata, String cuenta)
 			throws RepositoryException {
 
 		// GlAccountOrganization
 		Debug.log("Empieza GlAccountOrganization " + cuenta);
 		GlAccountOrganization glAccountOrganization = ledger_repo.findOne(
 				GlAccountOrganization.class, ledger_repo.map(
 						GlAccountOrganization.Fields.glAccountId, cuenta,
 						GlAccountOrganization.Fields.organizationPartyId,
 						rowdata.getOrganizationPartyId()));
 
 		if (glAccountOrganization.getPostedBalance() == null) {
 			glAccountOrganization.setPostedBalance(rowdata.getMonto());
 		} else {
 			glAccountOrganization.setPostedBalance(glAccountOrganization
 					.getPostedBalance().add(rowdata.getMonto()));
 		}
 		return glAccountOrganization;
 	}
 
 	private void storeImportEgresoDiarioSuccess(DataImportEgresoDiario rowdata,
 			EgresoDiarioDataImportRepositoryInterface imp_repo)
 			throws RepositoryException {
 		// mark as success
 		rowdata.setImportStatusId(StatusItemConstants.Dataimport.DATAIMP_IMPORTED);
 		rowdata.setImportError(null);
 		rowdata.setProcessedTimestamp(UtilDateTime.nowTimestamp());
 		imp_repo.createOrUpdate(rowdata);
 	}
 
 	private void storeImportEgresoDiarioError(DataImportEgresoDiario rowdata,
 			String message, EgresoDiarioDataImportRepositoryInterface imp_repo)
 			throws RepositoryException {
 		// store the exception and mark as failed
 		rowdata.setImportStatusId(StatusItemConstants.Dataimport.DATAIMP_FAILED);
 		rowdata.setImportError(message);
 		rowdata.setProcessedTimestamp(UtilDateTime.nowTimestamp());
 		imp_repo.createOrUpdate(rowdata);
 	}
 
 	/** {@inheritDoc} */
 	public void importEgresoDiario() throws ServiceException {
 		try {
 			this.session = this.getInfrastructure().getSession();
 			EgresoDiarioDataImportRepositoryInterface imp_repo = this
 					.getDomainsDirectory().getDataImportDomain()
 					.getEgresoDiarioDataImportRepository();
 			LedgerRepositoryInterface ledger_repo = this.getDomainsDirectory()
 					.getLedgerDomain().getLedgerRepository();
 
 			List<DataImportEgresoDiario> dataforimp = imp_repo
 					.findNotProcessesDataImportEgresoDiarioEntries();
 
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
 
 			for (DataImportEgresoDiario rowdata : dataforimp) {
 				// Empieza bloque de validaciones
 				Debug.log("Empieza bloque de validaciones");
 				Debug.log("Empieza bloque de validaciones");
 				Party ur = validaParty(imp_repo, ledger_repo, rowdata,
 						rowdata.getUr(), "ur");
 				Party uo = validaParty(imp_repo, ledger_repo, rowdata,
 						rowdata.getUo(), "uo");
 				Party ue = validaParty(imp_repo, ledger_repo, rowdata,
 						rowdata.getUe(), "ue");
 				Enumeration fin = validaEnumeration(imp_repo, ledger_repo,
 						rowdata, rowdata.getFin(), "CLAS_FUN", "fin");
 				Enumeration fun = validaEnumeration(imp_repo, ledger_repo,
 						rowdata, rowdata.getFun(), "CLAS_FUN", "fun");
 				Enumeration subf = validaEnumeration(imp_repo, ledger_repo,
 						rowdata, rowdata.getSubf(), "CLAS_FUN", "subf");
 				WorkEffort eje = validaWorkEffort(imp_repo, ledger_repo,
 						rowdata, rowdata.getEje(), "eje");
 				WorkEffort pp = validaWorkEffort(imp_repo, ledger_repo,
 						rowdata, rowdata.getPp(), "pp");
 				WorkEffort spp = validaWorkEffort(imp_repo, ledger_repo,
 						rowdata, rowdata.getSpp(), "spp");
 				WorkEffort act = validaWorkEffort(imp_repo, ledger_repo,
 						rowdata, rowdata.getAct(), "act");
 				Enumeration tg = validaEnumeration(imp_repo, ledger_repo,
 						rowdata, rowdata.getTg(), "TIPO_GASTO", "tg");
 				ProductCategory cap = validaProduct(imp_repo, ledger_repo,
 						rowdata, rowdata.getCap(), "CA", "cap");
 				ProductCategory con = validaProduct(imp_repo, ledger_repo,
 						rowdata, rowdata.getCon(), "CON", "con");
 				ProductCategory pg = validaProduct(imp_repo, ledger_repo,
 						rowdata, rowdata.getPg(), "PG", "pg");
 				ProductCategory pe = validaProduct(imp_repo, ledger_repo,
 						rowdata, rowdata.getPe(), "PE", "pe");
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
 				Enumeration sec = validaEnumeration(imp_repo, ledger_repo,
 						rowdata, rowdata.getSec(), "CLAS_SECT", "sec");
 				Enumeration subsec = validaEnumeration(imp_repo, ledger_repo,
 						rowdata, rowdata.getSubsec(), "CLAS_SECT", "subsec");
 				Enumeration area = validaEnumeration(imp_repo, ledger_repo,
 						rowdata, rowdata.getArea(), "CLAS_SECT", "area");
 				TipoDocumento tipodoc = new TipoDocumento();
 				if (!rowdata.getIdTipoDoc().equalsIgnoreCase("0")) {
 					tipodoc = validaTipoDoc(imp_repo, ledger_repo, rowdata,
 							rowdata.getIdTipoDoc());
 				}
 				if (rowdata.getIdPago() != null
 						&& !rowdata.getIdPago().equalsIgnoreCase("0")) {
 					validaPago(imp_repo, ledger_repo, rowdata,
 							rowdata.getIdPago());
 				}
 				Debug.log("Motor Contable");
 				MotorContable motor = new MotorContable(ledger_repo);
 				Map<String, String> cuentas = motor.cuentasDiarias(
 						tipodoc.getAcctgTransTypeId(), rowdata.getPg(),
 						pe.getProductCategoryId(),
 						rowdata.getOrganizationPartyId(), rowdata.getTg(),
 						null, rowdata.getIdTipoCatalogo(), rowdata.getIdPago(),
 						null, null, null, true, null, null,
 						rowdata.getIdProducto());
 
 				try {
 
 					// id maximo
 					Debug.log("Busqueda idMax");
 					String id = ledger_repo.getNextSeqId("AcctgTrans");
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
 
 					AcctgTrans EgresoDiario = new AcctgTrans();
 
 					// Vigencias
 					validaVigencia(imp_repo, rowdata, "sfe", sfe,
 							rowdata.getFechaRegistro());
 
 					Calendar cal = Calendar.getInstance();
 					cal.setTime(rowdata.getFechaRegistro());
 					EgresoDiario.setTransactionDate(new Timestamp(cal
 							.getTimeInMillis()));
 					EgresoDiario.setIsPosted("Y");
 					cal.setTime(rowdata.getFechaContable());
 					EgresoDiario.setPostedDate(new Timestamp(cal
 							.getTimeInMillis()));
 					EgresoDiario.setAcctgTransTypeId(tipodoc
 							.getAcctgTransTypeId());
 					EgresoDiario.setLastModifiedByUserLogin(rowdata
 							.getUsuario());
 					EgresoDiario.setPartyId(ue.getPartyId());
 					EgresoDiario.setPostedAmount(rowdata.getMonto());
 
 					// ACCTG_TRANS_PRESUPUESTAL
 					Debug.log("ACCTG_TRANS_PRESUPUESTAL");
 					AcctgTransPresupuestal aux = new AcctgTransPresupuestal();
 					aux.setCiclo(rowdata.getCiclo());
 					aux.setUnidadResponsable(ur.getPartyId());
 					aux.setDescripcionUr(ur.getDescription());
 					aux.setUnidadOrganizacional(uo.getPartyId());
 					aux.setDescripcionUo(uo.getDescription());
 					aux.setUnidadEjecutora(ue.getPartyId());
 					aux.setDescripcionUe(ue.getDescription());
 					aux.setFinalidad(fin.getEnumId());
 					aux.setDescripcionFinalidad(fin.getDescription());
 					aux.setFuncion(fun.getEnumId());
 					aux.setDescripcionFuncion(fun.getDescription());
 					aux.setSubFuncion(subf.getEnumId());
 					aux.setDescripcionSubFuncion(subf.getDescription());
 					aux.setProgramaPlan(rowdata.getEje());
 					aux.setDescripcionProgramaPlan(eje.getDescription());
 					aux.setProgramaPresupuestario(rowdata.getPp());
 					aux.setDescripcionProgramaPres(pp.getDescription());
 					aux.setSubProgramaPresupuestario(rowdata.getSpp());
 					aux.setDescripcionSubProgramaPres(spp.getDescription());
 					aux.setActividad(rowdata.getAct());
 					aux.setDescripcionActividad(act.getDescription());
 					aux.setTipoGasto(tg.getEnumId());
 					aux.setDescripcionTg(tg.getDescription());
 					aux.setCapitulo(cap.getProductCategoryId());
 					aux.setDescripcionCapitulo(cap.getDescription());
 					aux.setConcepto(con.getProductCategoryId());
 					aux.setDescripcionConcepto(con.getDescription());
 					aux.setPartidaGenerica(pg.getProductCategoryId());
 					aux.setDescripcionPg(pg.getDescription());
 					aux.setPartidaEspecifica(pe.getProductCategoryId());
 					aux.setDescripcionPe(pe.getDescription());
 					aux.setFuente(f.getEnumId());
 					aux.setDescripcionFuente(f.getDescription());
 					aux.setSubFuente(sf.getEnumId());
 					aux.setDescripcionSubfuente(sf.getDescription());
 					aux.setSubFuenteEspecifica(sfe.getEnumId());
 					aux.setDescripcionSubfuenteEspecifica(sfe.getDescription());
 					aux.setEntidadFederativa(rowdata.getEf());
 					aux.setDescripcionEntFed(ef.getGeoName());
 					aux.setRegion(rowdata.getReg());
 					aux.setDescripcionRegion(reg.getGeoName());
 					aux.setMunicipio(rowdata.getMun());
 					aux.setDescripcionMunicipio(mun.getGeoName());
 					aux.setLocalidad(rowdata.getLoc());
 					aux.setDescripcionLocalidad(loc.getGeoName());
 					aux.setSector(sec.getEnumId());
 					aux.setDescripcionSector(sec.getDescription());
 					aux.setSubSector(subsec.getEnumId());
 					aux.setDescripcionSubsector(subsec.getDescription());
 					aux.setArea(area.getEnumId());
 					aux.setDescripcionArea(area.getDescription());
 					aux.setAgrupador(rowdata.getRefDoc());
 					aux.setIdTipoDoc(rowdata.getIdTipoDoc());
 					aux.setDescripcionTipoDoc(tipodoc.getDescripcion());
 					aux.setSecuencia(rowdata.getSecuencia());
 					aux.setLote(rowdata.getLote());
 					aux.setClavePres(rowdata.getClavePres());
 
 					if (cuentas.get("Cuenta Cargo Presupuesto") != null) {
 						Debug.log("Cuenta Presupuestal");
 						EgresoDiario.setDescription(tipodoc.getDescripcion()
 								+ "-" + rowdata.getRefDoc() + "-P");
 
 						// id Transaccion
 						List<AcctgTrans> trans = ledger_repo.findList(
 								AcctgTrans.class, ledger_repo.map(
 										AcctgTrans.Fields.description,
 										EgresoDiario.getDescription()));
 
 						if (trans.isEmpty()) {
 							Debug.log("Trans Nueva");
 							EgresoDiario.setAcctgTransId(id + "-P");
 							EgresoDiario.setCreatedByUserLogin(rowdata
 									.getUsuario());
 						} else {
 							Debug.log("Trans Modif");
 							EgresoDiario.setAcctgTransId(trans.get(0)
 									.getAcctgTransId());
 							EgresoDiario.setCreatedByUserLogin(trans.get(0)
 									.getCreatedByUserLogin());
 						}
 						Debug.log("commit Trans");
 						EgresoDiario.setGlFiscalTypeId("BUDGET");
 						imp_tx1 = this.session.beginTransaction();
 						ledger_repo.createOrUpdate(EgresoDiario);
 						imp_tx1.commit();
 
 						Debug.log("commit Aux");
 						aux.setAcctgTransId(EgresoDiario.getAcctgTransId());
 						imp_tx3 = this.session.beginTransaction();
 						ledger_repo.createOrUpdate(aux);
 						imp_tx3.commit();
 
 						Debug.log("commit CCP");
 						AcctgTransEntry acctgentry = generaAcctgTransEntry(
 								EgresoDiario, rowdata, "00001", "D",
 								cuentas.get("Cuenta Cargo Presupuesto"),
 								sfe.getEnumId());
 						imp_tx5 = this.session.beginTransaction();
 						ledger_repo.createOrUpdate(acctgentry);
 						imp_tx5.commit();
 
 						Debug.log("commit GlAO");
 						GlAccountOrganization glAccountOrganization = actualizaGlAccountOrganization(
 								ledger_repo, rowdata,
 								cuentas.get("Cuenta Cargo Presupuesto"));
 						imp_tx7 = this.session.beginTransaction();
 						ledger_repo.createOrUpdate(glAccountOrganization);
 						imp_tx7.commit();
 
 						Debug.log("commit CAP");
 						acctgentry = generaAcctgTransEntry(EgresoDiario,
 								rowdata, "00002", "C",
 								cuentas.get("Cuenta Abono Presupuesto"),
 								sfe.getEnumId());
 						imp_tx9 = this.session.beginTransaction();
 						ledger_repo.createOrUpdate(acctgentry);
 						imp_tx9.commit();
 
 						Debug.log("commit GlAO");
 						glAccountOrganization = actualizaGlAccountOrganization(
 								ledger_repo, rowdata,
 								cuentas.get("Cuenta Abono Presupuesto"));
 						imp_tx11 = this.session.beginTransaction();
 						ledger_repo.createOrUpdate(glAccountOrganization);
 						imp_tx11.commit();
 
 					}
 
 					if (cuentas.get("Cuenta Cargo Contable") != null) {
 						Debug.log("Cuenta Contable");
 						EgresoDiario.setDescription(tipodoc.getDescripcion()
 								+ "-" + rowdata.getRefDoc() + "-C");
 
 						// id Transaccion
 						List<AcctgTrans> trans = ledger_repo.findList(
 								AcctgTrans.class, ledger_repo.map(
 										AcctgTrans.Fields.description,
 										EgresoDiario.getDescription()));
 
 						if (trans.isEmpty()) {
 							Debug.log("Trans Nueva");
 							EgresoDiario.setAcctgTransId(id + "-C");
 							EgresoDiario.setCreatedByUserLogin(rowdata
 									.getUsuario());
 						} else {
 							Debug.log("Trans Modif");
 							EgresoDiario.setAcctgTransId(trans.get(0)
 									.getAcctgTransId());
 							EgresoDiario.setCreatedByUserLogin(trans.get(0)
 									.getCreatedByUserLogin());
 						}
 
 						EgresoDiario.setGlFiscalTypeId(cuentas
 								.get("GlFiscalType"));
 						imp_tx2 = this.session.beginTransaction();
 						ledger_repo.createOrUpdate(EgresoDiario);
 						imp_tx2.commit();
 
 						aux.setAcctgTransId(EgresoDiario.getAcctgTransId());
 						imp_tx4 = this.session.beginTransaction();
 						ledger_repo.createOrUpdate(aux);
 						imp_tx4.commit();
 
 						AcctgTransEntry acctgentry = generaAcctgTransEntry(
 								EgresoDiario, rowdata, "00001", "D",
 								cuentas.get("Cuenta Cargo Contable"),
 								sfe.getEnumId());
 						imp_tx6 = this.session.beginTransaction();
 						ledger_repo.createOrUpdate(acctgentry);
 						imp_tx6.commit();
 
 						GlAccountOrganization glAccountOrganization = actualizaGlAccountOrganization(
 								ledger_repo, rowdata,
 								cuentas.get("Cuenta Cargo Contable"));
 						imp_tx8 = this.session.beginTransaction();
 						ledger_repo.createOrUpdate(glAccountOrganization);
 						imp_tx8.commit();
 
 						acctgentry = generaAcctgTransEntry(EgresoDiario,
 								rowdata, "00002", "C",
 								cuentas.get("Cuenta Abono Contable"),
 								sfe.getEnumId());
 						imp_tx10 = this.session.beginTransaction();
 						ledger_repo.createOrUpdate(acctgentry);
 						imp_tx10.commit();
 
 						glAccountOrganization = actualizaGlAccountOrganization(
 								ledger_repo, rowdata,
 								cuentas.get("Cuenta Abono Contable"));
 						imp_tx12 = this.session.beginTransaction();
 						ledger_repo.createOrUpdate(glAccountOrganization);
 						imp_tx12.commit();
 					}
 
 					String message = "Successfully imported Egreso Diario ["
 							+ rowdata.getClavePres() + "].";
 					this.storeImportEgresoDiarioSuccess(rowdata, imp_repo);
 					Debug.logInfo(message, MODULE);
 					imported = imported + 1;
 
 				} catch (Exception ex) {
 					String message = "Failed to import Egreso Diario ["
 							+ rowdata.getClavePres() + "], Error message : "
 							+ ex.getMessage();
 					storeImportEgresoDiarioError(rowdata, message, imp_repo);
 
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
