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
 import org.opentaps.base.entities.DataImportEgresoDiario;
 import org.opentaps.base.entities.Enumeration;
 import org.opentaps.base.entities.Geo;
 import org.opentaps.base.entities.GlAccountOrganization;
 import org.opentaps.base.entities.Party;
 import org.opentaps.base.entities.ProductCategory;
 import org.opentaps.base.entities.TipoDocumento;
 import org.opentaps.base.entities.WorkEffort;
 import org.opentaps.dataimport.UtilImport;
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
 	private String lote;
 	public int importedRecords;
 
 	public EgresoDiarioImportService() {
 		super();
 	}
 
 	public EgresoDiarioImportService(Infrastructure infrastructure, User user,
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
 				String mensaje = "";
 				Debug.log("Empieza bloque de validaciones");
 				// mensaje = UtilImport.validaParty(mensaje, ledger_repo,
 				// rowdata.getUr(), "UR");
 				// mensaje = UtilImport.validaParty(mensaje, ledger_repo,
 				// rowdata.getUo(), "UO");
 				// mensaje = UtilImport.validaParty(mensaje, ledger_repo,
 				// rowdata.getUe(), "UE");
 				// mensaje = UtilImport.validaEnumeration(mensaje, ledger_repo,
 				// rowdata.getFin(), "CLAS_FUN", "FIN");
 				// mensaje = UtilImport.validaEnumeration(mensaje, ledger_repo,
 				// rowdata.getFun(), "CLAS_FUN", "FUN");
 				// mensaje = UtilImport.validaEnumeration(mensaje, ledger_repo,
 				// rowdata.getSubf(), "CLAS_FUN", "SUBF");
 				// mensaje = UtilImport.validaWorkEffort(mensaje, ledger_repo,
 				// rowdata.getEje(), "EJE");
 				// mensaje = UtilImport.validaWorkEffort(mensaje, ledger_repo,
 				// rowdata.getPp(), "PP");
 				// mensaje = UtilImport.validaWorkEffort(mensaje, ledger_repo,
 				// rowdata.getSpp(), "SPP");
 				// mensaje = UtilImport.validaWorkEffort(mensaje, ledger_repo,
 				// rowdata.getAct(), "ACT");
 				// mensaje = UtilImport.validaEnumeration(mensaje, ledger_repo,
 				// rowdata.getTg(), "TIPO_GASTO", "tg");
 				// mensaje = UtilImport.validaProductCategory(mensaje,
 				// ledger_repo, rowdata.getCap(), "CA", "CAP");
 				// mensaje = UtilImport.validaProductCategory(mensaje,
 				// ledger_repo, rowdata.getCon(), "CON", "CON");
 				// mensaje = UtilImport.validaProductCategory(mensaje,
 				// ledger_repo, rowdata.getPg(), "PG", "PG");
 				// mensaje = UtilImport.validaProductCategory(mensaje,
 				// ledger_repo, rowdata.getPe(), "PE", "PE");
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
 				// mensaje = UtilImport.validaEnumeration(mensaje, ledger_repo,
 				// rowdata.getSec(), "CLAS_SECT", "SEC");
 				// mensaje = UtilImport.validaEnumeration(mensaje, ledger_repo,
 				// rowdata.getSubsec(), "CLAS_SECT", "SUBSEC");
 				// mensaje = UtilImport.validaEnumeration(mensaje, ledger_repo,
 				// rowdata.getArea(), "CLAS_SECT", "AREA");
 				mensaje = UtilImport.validaTipoDoc(mensaje, ledger_repo,
 						rowdata.getIdTipoDoc());
 				mensaje = UtilImport.validaCiclo(mensaje, rowdata.getCiclo(),
 						rowdata.getFechaContable());
 
 				mensaje = UtilImport.validaParty(mensaje, ledger_repo,
 						rowdata.getUe(), "ADMINISTRATIVA");
 				mensaje = UtilImport.validaEnumeration(mensaje, ledger_repo,
 						rowdata.getSubf(), "CL_FUNCIONAL", "FUNCIONAL");
 				mensaje = UtilImport.validaWorkEffort(mensaje, ledger_repo,
 						rowdata.getAct(), "ACTIVIDAD");
 				mensaje = UtilImport.validaEnumeration(mensaje, ledger_repo,
 						rowdata.getTg(), "TIPO_GASTO", "TIPO GASTO");
 				mensaje = UtilImport.validaProductCategory(mensaje,
 						ledger_repo, rowdata.getPe(), "PARTIDA ESPECIFICA",
 						"PRODUCTO ESPECIFICO");
 				mensaje = UtilImport.validaEnumeration(mensaje, ledger_repo,
 						rowdata.getSfe(), "CL_FUENTE_RECURSOS", "FUENTE DE LOS RECURSOS");
 				mensaje = UtilImport.validaGeo(mensaje, ledger_repo,
 						rowdata.getLoc(), "GEOGRAFICA");
 				mensaje = UtilImport.validaEnumeration(mensaje, ledger_repo,
 						rowdata.getArea(), "CL_SECTORIAL", "SECTORIAL");
 
 				if (!mensaje.isEmpty()) {
 					String message = "Failed to import Egreso Diario ["
 							+ rowdata.getClavePres() + "], Error message : "
 							+ mensaje;
 					storeImportEgresoDiarioError(rowdata, message, imp_repo);
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
 				// Enumeration fin = UtilImport.obtenEnumeration(ledger_repo,
 				// rowdata.getFin(), "CLAS_FUN");
 				// Enumeration fun = UtilImport.obtenEnumeration(ledger_repo,
 				// rowdata.getFun(), "CLAS_FUN");
 				// Enumeration subf = UtilImport.obtenEnumeration(ledger_repo,
 				// rowdata.getSubf(), "CLAS_FUN");
 				// WorkEffort eje = UtilImport.obtenWorkEffort(ledger_repo,
 				// rowdata.getEje());
 				// WorkEffort pp = UtilImport.obtenWorkEffort(ledger_repo,
 				// rowdata.getPp());
 				// WorkEffort spp = UtilImport.obtenWorkEffort(ledger_repo,
 				// rowdata.getSpp());
 				// WorkEffort act = UtilImport.obtenWorkEffort(ledger_repo,
 				// rowdata.getAct());
 				// Enumeration tg = UtilImport.obtenEnumeration(ledger_repo,
 				// rowdata.getFin(), "TIPO_GASTO");
 				// ProductCategory cap = UtilImport.obtenProductCategory(
 				// ledger_repo, rowdata.getCap(), "CA");
 				// ProductCategory con = UtilImport.obtenProductCategory(
 				// ledger_repo, rowdata.getCon(), "CON");
 				// ProductCategory pg = UtilImport.obtenProductCategory(
 				// ledger_repo, rowdata.getPg(), "PG");
 				// ProductCategory pe = UtilImport.obtenProductCategory(
 				// ledger_repo, rowdata.getPe(), "PE");
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
 				// Enumeration sec = UtilImport.obtenEnumeration(ledger_repo,
 				// rowdata.getSec(), "CLAS_SECT");
 				// Enumeration subsec = UtilImport.obtenEnumeration(ledger_repo,
 				// rowdata.getSubsec(), "CLAS_SECT");
 				// Enumeration area = UtilImport.obtenEnumeration(ledger_repo,
 				// rowdata.getArea(), "CLAS_SECT");
 				TipoDocumento tipoDoc = UtilImport.obtenTipoDocumento(
 						ledger_repo, rowdata.getIdTipoDoc());
 
 				Party ue = UtilImport.obtenParty(ledger_repo, rowdata.getUe());
 				Enumeration subf = UtilImport.obtenEnumeration(ledger_repo,
 						rowdata.getSubf(), "CL_FUNCIONAL");
 				WorkEffort act = UtilImport.obtenWorkEffort(ledger_repo,
 						rowdata.getAct());
 				Enumeration tg = UtilImport.obtenEnumeration(ledger_repo,
 						rowdata.getTg(), "TIPO_GASTO");
 				ProductCategory pe = UtilImport.obtenProductCategory(
 						ledger_repo, rowdata.getPe(), "PARTIDA ESPECIFICA");
 				Geo loc = UtilImport.obtenGeo(ledger_repo, rowdata.getLoc());
 				Enumeration sfe = UtilImport.obtenEnumeration(ledger_repo,
 						rowdata.getSfe(), "CL_FUENTE_RECURSOS");
 				Enumeration area = UtilImport.obtenEnumeration(ledger_repo,
 						rowdata.getArea(), "CL_SECTORIAL");
 
 				// Empieza bloque de vigencias
 				Debug.log("Empieza bloque de vigencias");
 				// Vigencias
 				mensaje = UtilImport.validaVigencia(mensaje, "FUNCIONAL", subf,
 						rowdata.getFechaContable());
 				mensaje = UtilImport.validaVigencia(mensaje, "TIPO GASTO", tg,
 						rowdata.getFechaContable());
 				mensaje = UtilImport.validaVigencia(mensaje,
 						"FUENTE DE LOS RECURSOS", sfe,
 						rowdata.getFechaContable());
 				mensaje = UtilImport.validaVigencia(mensaje, "SECTORIAL", area,
 						rowdata.getFechaContable());
 
 				if (!mensaje.isEmpty()) {
 					String message = "Failed to import Egreso Diario ["
 							+ rowdata.getClavePres() + "], Error message : "
 							+ mensaje;
 					storeImportEgresoDiarioError(rowdata, message, imp_repo);
 					continue;
 				}
 
 				// Obtenemos los padres de cada nivel.
 				String uo = UtilImport.obtenPadreParty(ledger_repo,
 						ue.getPartyId());
 				String ur = UtilImport.obtenPadreParty(ledger_repo, uo);
 				String fun = UtilImport.obtenPadreEnumeration(ledger_repo,
 						subf.getEnumId());
 				String fin = UtilImport.obtenPadreEnumeration(ledger_repo, fun);
 				String spp = UtilImport.obtenPadreWorkEffort(ledger_repo,
 						act.getWorkEffortId());
 				String pp = UtilImport.obtenPadreWorkEffort(ledger_repo, spp);
 				String eje = UtilImport.obtenPadreWorkEffort(ledger_repo, pp);
 				String pg = UtilImport.obtenPadreProductCategory(ledger_repo,
 						pe.getProductCategoryId());
 				String con = UtilImport.obtenPadreProductCategory(ledger_repo,
 						pg);
 				String cap = UtilImport.obtenPadreProductCategory(ledger_repo,
 						con);
 				String sf = UtilImport.obtenPadreEnumeration(ledger_repo,
 						sfe.getEnumId());
 				String f = UtilImport.obtenPadreEnumeration(ledger_repo, sf);
 				String mun = UtilImport.obtenPadreGeo(ledger_repo,
 						loc.getGeoId());
 				String reg = UtilImport.obtenPadreGeo(ledger_repo, mun);
 				String ef = UtilImport.obtenPadreGeo(ledger_repo, reg);
 				String subsec = UtilImport.obtenPadreEnumeration(ledger_repo,
 						area.getEnumId());
 				String sec = UtilImport.obtenPadreEnumeration(ledger_repo,
 						subsec);
 
 				Debug.log("Motor Contable");
 				MotorContable motor = new MotorContable(ledger_repo);
 				// Map<String, String> cuentas = motor.cuentasDiarias(
 				// tipoDoc.getAcctgTransTypeId(), pg,
 				// pe.getProductCategoryId(),
 				// rowdata.getOrganizationPartyId(), rowdata.getTg(),
 				// null, rowdata.getIdTipoCatalogo(), rowdata.getIdPago(),
 				// null, null, null, true, null, null,
 				// rowdata.getIdProducto());
 
 //				Map<String, String> cuentas = motor.cuentasEgresoDiario(
 //						tipoDoc.getAcctgTransTypeId(), pg,
 //						rowdata.getOrganizationPartyId(), rowdata.getTg(),
 //						rowdata.getIdPago(), rowdata.getIdProductoD(),
 //						rowdata.getIdProductoH());
 
 				Map<String, String> cuentas = motor.cuentasEgresoDiario(
 						tipoDoc.getAcctgTransTypeId(), pe.getProductCategoryId(),
 						rowdata.getOrganizationPartyId(), rowdata.getTg(),
 						rowdata.getIdPago(), rowdata.getIdProductoD(),
 						rowdata.getIdProductoH());
 				
 
 				if (cuentas.get("Mensaje") != null) {
 					String message = "Failed to import Egreso Diario ["
 							+ rowdata.getClavePres() + "], Error message : "
 							+ cuentas.get("Mensaje");
 					storeImportEgresoDiarioError(rowdata, message, imp_repo);
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
 
 					AcctgTrans egresoDiario = new AcctgTrans();
 
 					Calendar cal = Calendar.getInstance();
 					cal.setTime(rowdata.getFechaRegistro());
 					egresoDiario.setTransactionDate(new Timestamp(cal
 							.getTimeInMillis()));
 					egresoDiario.setIsPosted("Y");
 					cal.setTime(rowdata.getFechaContable());
 					egresoDiario.setPostedDate(new Timestamp(cal
 							.getTimeInMillis()));
 					egresoDiario.setAcctgTransTypeId(tipoDoc
 							.getAcctgTransTypeId());
 					egresoDiario.setLastModifiedByUserLogin(rowdata
 							.getUsuario());
 					egresoDiario.setPartyId(ue.getPartyId());
 					egresoDiario.setPostedAmount(rowdata.getMonto());
 					egresoDiario.setWorkEffortId(act.getWorkEffortId());
 
 					// ACCTG_TRANS_PRESUPUESTAL
 					Debug.log("ACCTG_TRANS_PRESUPUESTAL");
 					AcctgTransPresupuestal aux = new AcctgTransPresupuestal();
 					aux.setCiclo(rowdata.getCiclo());
 					aux.setUnidadResponsable(ur);
 					aux.setUnidadOrganizacional(uo);
 					aux.setUnidadEjecutora(ue.getPartyId());
 					aux.setFinalidad(fin);
 					aux.setFuncion(fun);
 					aux.setSubFuncion(subf.getEnumId());
 					aux.setProgramaPlan(eje);
 					aux.setProgramaPresupuestario(pp);
 					aux.setSubProgramaPresupuestario(spp);
 					aux.setActividad(act.getWorkEffortId());
 					aux.setTipoGasto(tg.getEnumId());
 					aux.setCapitulo(cap);
 					aux.setConcepto(con);
 					aux.setPartidaGenerica(pg);
 					aux.setPartidaEspecifica(pe.getProductCategoryId());
 					aux.setFuente(f);
 					aux.setSubFuente(sf);
 					aux.setSubFuenteEspecifica(sfe.getEnumId());
 					aux.setEntidadFederativa(ef);
 					aux.setRegion(reg);
 					aux.setMunicipio(mun);
 					aux.setLocalidad(loc.getGeoId());
 					aux.setSector(sec);
 					aux.setSubSector(subsec);
 					aux.setArea(area.getEnumId());
 					aux.setAgrupador(rowdata.getRefDoc());
					aux.setIdPago(rowdata.getIdPago());
					aux.setIdProductoD(rowdata.getIdProductoD());
					aux.setIdProductoH(rowdata.getIdProductoH());
 					aux.setIdTipoDoc(rowdata.getIdTipoDoc());
 					aux.setSecuencia(rowdata.getSecuencia());
 					aux.setLote(lote);
 					aux.setClavePres(rowdata.getClavePres());
 
 					if (cuentas.get("Cuenta Cargo Presupuesto") != null) {
 						Debug.log("Cuenta Presupuestal");
 
 						egresoDiario.setDescription(tipoDoc.getDescripcion()
 								+ "-" + rowdata.getRefDoc() + "-P");
 
 						// id Transaccion
 						egresoDiario.setAcctgTransId(tipoDoc.getDescripcion()
 								+ "-" + rowdata.getRefDoc() + "-P");
 
 						AcctgTrans trans = ledger_repo.findOne(
 								AcctgTrans.class, ledger_repo.map(
 										AcctgTrans.Fields.acctgTransId,
 										egresoDiario.getAcctgTransId()));
 
 						if (trans != null) {
 							Debug.log("Trans Modif");
 							String message = "La transaccion con id: "
 									+ egresoDiario.getAcctgTransId()
 									+ "ya existe.";
 							Debug.log(message);
 							storeImportEgresoDiarioError(rowdata, message,
 									imp_repo);
 							continue;
 						}
 
 						Debug.log("Trans Nueva");
 						egresoDiario
 								.setCreatedByUserLogin(rowdata.getUsuario());
 
 						egresoDiario.setGlFiscalTypeId(cuentas
 								.get("GlFiscalTypePresupuesto"));
 						imp_tx1 = this.session.beginTransaction();
 						ledger_repo.createOrUpdate(egresoDiario);
 						imp_tx1.commit();
 
 						Debug.log("commit Aux");
 						aux.setAcctgTransId(egresoDiario.getAcctgTransId());
 						imp_tx3 = this.session.beginTransaction();
 						ledger_repo.createOrUpdate(aux);
 						imp_tx3.commit();
 
 						Debug.log("commit CCP");
 						AcctgTransEntry acctgentry = UtilImport
 								.generaAcctgTransEntry(
 										egresoDiario,
 										rowdata.getOrganizationPartyId(),
 										"00001",
 										"D",
 										cuentas.get("Cuenta Cargo Presupuesto"),
 										sfe.getEnumId());
 						// Tags seteados.
 						acctgentry.setAcctgTagEnumId1(subf.getEnumId());
 						acctgentry.setAcctgTagEnumId2(tg.getEnumId());
 						acctgentry.setAcctgTagEnumId3(sfe.getEnumId());
 						acctgentry.setAcctgTagEnumId4(area.getEnumId());
 						imp_tx5 = this.session.beginTransaction();
 						ledger_repo.createOrUpdate(acctgentry);
 						imp_tx5.commit();
 
 						Debug.log("commit GlAO");
 						GlAccountOrganization glAccountOrganization = UtilImport
 								.actualizaGlAccountOrganization(
 										ledger_repo,
 										rowdata.getMonto(),
 										cuentas.get("Cuenta Cargo Presupuesto"),
 										rowdata.getOrganizationPartyId());
 						imp_tx7 = this.session.beginTransaction();
 						ledger_repo.createOrUpdate(glAccountOrganization);
 						imp_tx7.commit();
 
 						Debug.log("commit CAP");
 						acctgentry = UtilImport.generaAcctgTransEntry(
 								egresoDiario, rowdata.getOrganizationPartyId(),
 								"00002", "C",
 								cuentas.get("Cuenta Abono Presupuesto"),
 								sfe.getEnumId());
 						// Tags seteados.
 						acctgentry.setAcctgTagEnumId1(subf.getEnumId());
 						acctgentry.setAcctgTagEnumId2(tg.getEnumId());
 						acctgentry.setAcctgTagEnumId3(sfe.getEnumId());
 						acctgentry.setAcctgTagEnumId4(area.getEnumId());
 						imp_tx9 = this.session.beginTransaction();
 						ledger_repo.createOrUpdate(acctgentry);
 						imp_tx9.commit();
 
 						Debug.log("commit GlAO");
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
 						egresoDiario.setDescription(tipoDoc.getDescripcion()
 								+ "-" + rowdata.getRefDoc() + "-C");
 
 						// id Transaccion
 						egresoDiario.setAcctgTransId(UtilImport
 								.getAcctgTransIdDiario(rowdata.getRefDoc(),
 										rowdata.getSecuencia(), "C"));
 
 						AcctgTrans trans = ledger_repo.findOne(
 								AcctgTrans.class, ledger_repo.map(
 										AcctgTrans.Fields.acctgTransId,
 										egresoDiario.getAcctgTransId()));
 
 						if (trans != null) {
 							Debug.log("Trans Modif");
 							String message = "La transaccion con id: "
 									+ egresoDiario.getAcctgTransId()
 									+ "ya existe.";
 							Debug.log(message);
 							storeImportEgresoDiarioError(rowdata, message,
 									imp_repo);
 							continue;
 						}
 
 						egresoDiario.setGlFiscalTypeId(cuentas
 								.get("GlFiscalTypeContable"));
 						imp_tx2 = this.session.beginTransaction();
 						ledger_repo.createOrUpdate(egresoDiario);
 						imp_tx2.commit();
 
 						aux.setAcctgTransId(egresoDiario.getAcctgTransId());
 						imp_tx4 = this.session.beginTransaction();
 						ledger_repo.createOrUpdate(aux);
 						imp_tx4.commit();
 
 						AcctgTransEntry acctgentry = UtilImport
 								.generaAcctgTransEntry(egresoDiario,
 										rowdata.getOrganizationPartyId(),
 										"00001", "D",
 										cuentas.get("Cuenta Cargo Contable"),
 										sfe.getEnumId());
 						// Tags seteados.
 						acctgentry.setAcctgTagEnumId1(subf.getEnumId());
 						acctgentry.setAcctgTagEnumId2(tg.getEnumId());
 						acctgentry.setAcctgTagEnumId3(sfe.getEnumId());
 						acctgentry.setAcctgTagEnumId4(area.getEnumId());
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
 								egresoDiario, rowdata.getOrganizationPartyId(),
 								"00002", "C",
 								cuentas.get("Cuenta Abono Contable"),
 								sfe.getEnumId());
 						// Tags seteados.
 						acctgentry.setAcctgTagEnumId1(subf.getEnumId());
 						acctgentry.setAcctgTagEnumId2(tg.getEnumId());
 						acctgentry.setAcctgTagEnumId3(sfe.getEnumId());
 						acctgentry.setAcctgTagEnumId4(area.getEnumId());
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
 						String message = "Successfully imported Egreso Diario ["
 								+ rowdata.getClavePres() + "].";
 						this.storeImportEgresoDiarioSuccess(rowdata, imp_repo);
 						Debug.logInfo(message, MODULE);
 						imported = imported + 1;
 					}
 
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
