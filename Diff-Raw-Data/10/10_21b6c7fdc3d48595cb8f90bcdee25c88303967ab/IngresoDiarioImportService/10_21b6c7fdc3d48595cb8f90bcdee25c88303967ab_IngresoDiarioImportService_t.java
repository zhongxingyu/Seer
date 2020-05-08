 package org.opentaps.dataimport.domain;
 
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 import org.apache.poi.hssf.util.HSSFColor.ROSE;
 import org.ofbiz.base.util.Debug;
 import org.ofbiz.base.util.UtilDateTime;
 import org.opentaps.base.constants.StatusItemConstants;
 import org.opentaps.base.entities.AcctgTrans;
 import org.opentaps.base.entities.AcctgTransEntry;
 import org.opentaps.base.entities.AcctgTransPresupuestal;
 import org.opentaps.base.entities.ClasifPresupuestal;
 import org.opentaps.base.entities.CustomTimePeriod;
 import org.opentaps.base.entities.DataImportIngresoDiario;
 import org.opentaps.base.entities.Enumeration;
 import org.opentaps.base.entities.EstructuraClave;
 import org.opentaps.base.entities.Geo;
 import org.opentaps.base.entities.GlAccountHistory;
 import org.opentaps.base.entities.GlAccountOrganization;
 import org.opentaps.base.entities.LoteTransaccion;
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
 
 			if (UtilImport.validaLote(ledger_repo, lote, "IngresoDiario")) {
 				boolean loteValido=true;
 				for (DataImportIngresoDiario rowdata : dataforimp) {
 					// Empieza bloque de validaciones
 					String mensaje = "";
 					Debug.log("Empieza bloque de validaciones");
 					ContenedorContable contenedor = new ContenedorContable();
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
 					// mensaje = UtilImport.validaEnumeration(mensaje,
 					// ledger_repo,
 					// rowdata.getF(), "CLAS_FR", "F");
 					// mensaje = UtilImport.validaEnumeration(mensaje,
 					// ledger_repo,
 					// rowdata.getSf(), "CLAS_FR", "SF");
 					// mensaje = UtilImport.validaEnumeration(mensaje,
 					// ledger_repo,
 					// rowdata.getSfe(), "CLAS_FR", "SFE");
 					
 					/*mensaje = UtilImport.validaCiclo(mensaje,
 							rowdata.getCiclo(), rowdata.getFechaContable());
 
 					mensaje = UtilImport.validaParty(mensaje, ledger_repo,
 							rowdata.getUe(), "ADMINISTRATIVA");
 					mensaje = UtilImport.validaProductCategory(mensaje,
 							ledger_repo, rowdata.getN5(), "NIVEL_5_ING",
 							"RUBRO DEL INGRESO");
 					mensaje = UtilImport.validaEnumeration(mensaje,
 							ledger_repo, rowdata.getSfe(),
 							"CL_FUENTE_RECURSOS", "FUENTE DE LOS RECURSOS");
 					mensaje = UtilImport.validaGeo(mensaje, ledger_repo,
 							rowdata.getLoc(), "GEOGRAFICA");
 					mensaje = UtilImport.validaMonto(rowdata.getMonto(),
 							mensaje);
 					*/
 					//Se obtiene la estructura de la clave valida para el ciclo
 					EstructuraClave estructura = ledger_repo.findList(EstructuraClave.class,
 							ledger_repo.map(EstructuraClave.Fields.ciclo, 
 									UtilImport.obtenerCiclo(rowdata.getFechaContable()),
 									EstructuraClave.Fields.acctgTagUsageTypeId,"Ingreso")).get(0);
 					//Se obtiene el tipo de clasficacion
 					List<Clasificacion> listaClasif = new ArrayList<Clasificacion>();
 					if(estructura.getClasificacion1()!=null){
 					Clasificacion c = new Clasificacion();
 					String tipoClasif1 = ledger_repo.findList(ClasifPresupuestal.class, 
 							ledger_repo.map(ClasifPresupuestal.Fields.clasificacionId,
 									estructura.getClasificacion1())).get(0).getTablaRelacion();
 					c.setTipoObjeto(tipoClasif1);
 					c.setValor(rowdata.getClasificacion1());
 					c.setTipoEnum(ledger_repo.findList(ClasifPresupuestal.class, 
 							ledger_repo.map(ClasifPresupuestal.Fields.clasificacionId,
 									estructura.getClasificacion1())).get(0).getClasificacionId());
 					listaClasif.add(c);
 					}
 					if(estructura.getClasificacion2()!=null){
 					Clasificacion c = new Clasificacion();
 					String tipoClasif2 = ledger_repo.findList(ClasifPresupuestal.class, 
 							ledger_repo.map(ClasifPresupuestal.Fields.clasificacionId,
 									estructura.getClasificacion2())).get(0).getTablaRelacion();
 					c.setTipoObjeto(tipoClasif2);
 					c.setValor(rowdata.getClasificacion2());
 					c.setTipoEnum(ledger_repo.findList(ClasifPresupuestal.class, 
 							ledger_repo.map(ClasifPresupuestal.Fields.clasificacionId,
 									estructura.getClasificacion2())).get(0).getClasificacionId());
 					listaClasif.add(c);
 					}
 					if(estructura.getClasificacion3()!=null){
 					Clasificacion c = new Clasificacion();
 					String tipoClasif3 = ledger_repo.findList(ClasifPresupuestal.class, 
 							ledger_repo.map(ClasifPresupuestal.Fields.clasificacionId,
 									estructura.getClasificacion3())).get(0).getTablaRelacion();
 					c.setTipoObjeto(tipoClasif3);
 					c.setValor(rowdata.getClasificacion3());
 					c.setTipoEnum(ledger_repo.findList(ClasifPresupuestal.class, 
 							ledger_repo.map(ClasifPresupuestal.Fields.clasificacionId,
 									estructura.getClasificacion3())).get(0).getClasificacionId());
 					listaClasif.add(c);
 					}
 					if(estructura.getClasificacion4()!=null){
 						Clasificacion c = new Clasificacion();
 					String tipoClasif4 = ledger_repo.findList(ClasifPresupuestal.class, 
 							ledger_repo.map(ClasifPresupuestal.Fields.clasificacionId,
 									estructura.getClasificacion4())).get(0).getTablaRelacion();
 					c.setTipoObjeto(tipoClasif4);
 					c.setValor(rowdata.getClasificacion4());
 					c.setTipoEnum(ledger_repo.findList(ClasifPresupuestal.class, 
 							ledger_repo.map(ClasifPresupuestal.Fields.clasificacionId,
 									estructura.getClasificacion4())).get(0).getClasificacionId());
 					listaClasif.add(c);
 					}
 					if(estructura.getClasificacion5()!=null){
 						Clasificacion c = new Clasificacion();
 					String tipoClasif5 = ledger_repo.findList(ClasifPresupuestal.class, 
 							ledger_repo.map(ClasifPresupuestal.Fields.clasificacionId,
 									estructura.getClasificacion5())).get(0).getTablaRelacion();
 					c.setTipoObjeto(tipoClasif5);
 					c.setValor(rowdata.getClasificacion5());
 					c.setTipoEnum(ledger_repo.findList(ClasifPresupuestal.class, 
 							ledger_repo.map(ClasifPresupuestal.Fields.clasificacionId,
 									estructura.getClasificacion5())).get(0).getClasificacionId());
 					listaClasif.add(c);
 					}
 					if(estructura.getClasificacion6()!=null){
 						Clasificacion c = new Clasificacion();
 					String tipoClasif6 = ledger_repo.findList(ClasifPresupuestal.class, 
 							ledger_repo.map(ClasifPresupuestal.Fields.clasificacionId,
 									estructura.getClasificacion6())).get(0).getTablaRelacion();
 					c.setTipoObjeto(tipoClasif6);
 					c.setValor(rowdata.getClasificacion6());
 					c.setTipoEnum(ledger_repo.findList(ClasifPresupuestal.class, 
 							ledger_repo.map(ClasifPresupuestal.Fields.clasificacionId,
 									estructura.getClasificacion6())).get(0).getClasificacionId());
 					listaClasif.add(c);
 					}
 					if(estructura.getClasificacion7()!=null){
 						Clasificacion c = new Clasificacion();
 					String tipoClasif7 = ledger_repo.findList(ClasifPresupuestal.class, 
 							ledger_repo.map(ClasifPresupuestal.Fields.clasificacionId,
 									estructura.getClasificacion7())).get(0).getTablaRelacion();
 					c.setTipoObjeto(tipoClasif7);
 					c.setValor(rowdata.getClasificacion7());
 					c.setTipoEnum(ledger_repo.findList(ClasifPresupuestal.class, 
 							ledger_repo.map(ClasifPresupuestal.Fields.clasificacionId,
 									estructura.getClasificacion7())).get(0).getClasificacionId());
 					listaClasif.add(c);
 					}
 					if(estructura.getClasificacion8()!=null){
 						Clasificacion c = new Clasificacion();
 					String tipoClasif8 = ledger_repo.findList(ClasifPresupuestal.class, 
 							ledger_repo.map(ClasifPresupuestal.Fields.clasificacionId,
 									estructura.getClasificacion8())).get(0).getTablaRelacion();
 					c.setTipoObjeto(tipoClasif8);
 					c.setValor(rowdata.getClasificacion8());
 					c.setTipoEnum(ledger_repo.findList(ClasifPresupuestal.class, 
 							ledger_repo.map(ClasifPresupuestal.Fields.clasificacionId,
 									estructura.getClasificacion8())).get(0).getClasificacionId());
 					listaClasif.add(c);
 					}
 					if(estructura.getClasificacion9()!=null){
 						Clasificacion c = new Clasificacion();
 					String tipoClasif9 = ledger_repo.findList(ClasifPresupuestal.class, 
 							ledger_repo.map(ClasifPresupuestal.Fields.clasificacionId,
 									estructura.getClasificacion9())).get(0).getTablaRelacion();
 					c.setTipoObjeto(tipoClasif9);
 					c.setValor(rowdata.getClasificacion9());
 					c.setTipoEnum(ledger_repo.findList(ClasifPresupuestal.class, 
 							ledger_repo.map(ClasifPresupuestal.Fields.clasificacionId,
 									estructura.getClasificacion9())).get(0).getClasificacionId());
 					listaClasif.add(c);
 					}
 					if(estructura.getClasificacion10()!=null){
 						Clasificacion c = new Clasificacion();
 					String tipoClasif10 = ledger_repo.findList(ClasifPresupuestal.class, 
 							ledger_repo.map(ClasifPresupuestal.Fields.clasificacionId,
 									estructura.getClasificacion10())).get(0).getTablaRelacion();
 					c.setTipoObjeto(tipoClasif10);
 					c.setValor(rowdata.getClasificacion10());
 					c.setTipoEnum(ledger_repo.findList(ClasifPresupuestal.class, 
 							ledger_repo.map(ClasifPresupuestal.Fields.clasificacionId,
 									estructura.getClasificacion10())).get(0).getClasificacionId());
 					listaClasif.add(c);
 					}
 					if(estructura.getClasificacion11()!=null){
 						Clasificacion c = new Clasificacion();
 					String tipoClasif11 = ledger_repo.findList(ClasifPresupuestal.class, 
 							ledger_repo.map(ClasifPresupuestal.Fields.clasificacionId,
 									estructura.getClasificacion11())).get(0).getTablaRelacion();
 					c.setTipoObjeto(tipoClasif11);
 					c.setValor(rowdata.getClasificacion11());
 					c.setTipoEnum(ledger_repo.findList(ClasifPresupuestal.class, 
 							ledger_repo.map(ClasifPresupuestal.Fields.clasificacionId,
 									estructura.getClasificacion11())).get(0).getClasificacionId());
 					listaClasif.add(c);
 					}
 					if(estructura.getClasificacion12()!=null){
 						Clasificacion c = new Clasificacion();
 					String tipoClasif12 = ledger_repo.findList(ClasifPresupuestal.class, 
 							ledger_repo.map(ClasifPresupuestal.Fields.clasificacionId,
 									estructura.getClasificacion12())).get(0).getTablaRelacion();
 					c.setTipoObjeto(tipoClasif12);
 					c.setValor(rowdata.getClasificacion12());
 					c.setTipoEnum(ledger_repo.findList(ClasifPresupuestal.class, 
 							ledger_repo.map(ClasifPresupuestal.Fields.clasificacionId,
 									estructura.getClasificacion12())).get(0).getClasificacionId());
 					listaClasif.add(c);
 					}
 					if(estructura.getClasificacion13()!=null){
 						Clasificacion c = new Clasificacion();
 					String tipoClasif13 = ledger_repo.findList(ClasifPresupuestal.class, 
 							ledger_repo.map(ClasifPresupuestal.Fields.clasificacionId,
 									estructura.getClasificacion13())).get(0).getTablaRelacion();
 					c.setTipoObjeto(tipoClasif13);
 					c.setValor(rowdata.getClasificacion13());
 					c.setTipoEnum(ledger_repo.findList(ClasifPresupuestal.class, 
 							ledger_repo.map(ClasifPresupuestal.Fields.clasificacionId,
 									estructura.getClasificacion13())).get(0).getClasificacionId());
 					listaClasif.add(c);
 					}
 					if(estructura.getClasificacion14()!=null){
 						Clasificacion c = new Clasificacion();
 					String tipoClasif14 = ledger_repo.findList(ClasifPresupuestal.class, 
 							ledger_repo.map(ClasifPresupuestal.Fields.clasificacionId,
 									estructura.getClasificacion14())).get(0).getTablaRelacion();
 					c.setTipoObjeto(tipoClasif14);
 					c.setValor(rowdata.getClasificacion14());
 					c.setTipoEnum(ledger_repo.findList(ClasifPresupuestal.class, 
 							ledger_repo.map(ClasifPresupuestal.Fields.clasificacionId,
 									estructura.getClasificacion14())).get(0).getClasificacionId());
 					listaClasif.add(c);
 					}
 					if(estructura.getClasificacion15()!=null){
 						Clasificacion c = new Clasificacion();
 					String tipoClasif15 = ledger_repo.findList(ClasifPresupuestal.class, 
 							ledger_repo.map(ClasifPresupuestal.Fields.clasificacionId,
 									estructura.getClasificacion15())).get(0).getTablaRelacion();
 					c.setTipoObjeto(tipoClasif15);
 					c.setValor(rowdata.getClasificacion15());
 					c.setTipoEnum(ledger_repo.findList(ClasifPresupuestal.class, 
 							ledger_repo.map(ClasifPresupuestal.Fields.clasificacionId,
 									estructura.getClasificacion15())).get(0).getClasificacionId());
 					listaClasif.add(c);
 					}
 					
 					//Bloque de Validacion de Clasificaciones
 					
 					contenedor = UtilImport.validaClasificaciones(listaClasif,ledger_repo,"I",rowdata.getFechaContable());
 					Debug.log("Clasificaciones validadas");
 					mensaje = UtilImport.validaTipoDoc(mensaje, ledger_repo,
 							rowdata.getIdTipoDoc());
 					//----------------------------------------
 					if (contenedor.getMensaje()!= "" || !mensaje.isEmpty()) {
 						loteValido=false;
 						storeImportIngresoDiarioError(rowdata, contenedor.getMensaje(),
 								imp_repo);
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
 					// Geo ef = UtilImport.obtenGeo(ledger_repo,
 					// rowdata.getEf());
 					// Geo reg = UtilImport.obtenGeo(ledger_repo,
 					// rowdata.getReg());
 					// Geo mun = UtilImport.obtenGeo(ledger_repo,
 					// rowdata.getMun());
 					// Geo loc = UtilImport.obtenGeo(ledger_repo,
 					// rowdata.getLoc());
 					// Enumeration f = UtilImport.obtenEnumeration(ledger_repo,
 					// rowdata.getF(), "CLAS_FR");
 					// Enumeration sf = UtilImport.obtenEnumeration(ledger_repo,
 					// rowdata.getSf(), "CLAS_FR");
 					// Enumeration sfe =
 					// UtilImport.obtenEnumeration(ledger_repo,
 					// rowdata.getSfe(), "CLAS_FR");
 					TipoDocumento tipoDoc = UtilImport.obtenTipoDocumento(
 							ledger_repo, rowdata.getIdTipoDoc());
 
 					/*Party ue = UtilImport.obtenParty(ledger_repo,
 							rowdata.getUe());
 					ProductCategory n5 = UtilImport.obtenProductCategory(
 							ledger_repo, rowdata.getN5(), "NIVEL_5_ING");
 					Enumeration sfe = UtilImport.obtenEnumeration(ledger_repo,
 							rowdata.getSfe(), "CL_FUENTE_RECURSOS");
 					Geo loc = UtilImport
 							.obtenGeo(ledger_repo, rowdata.getLoc());*/
 
 					// Empieza bloque de vigencias
 					/*Debug.log("Empieza bloque de vigencias");
 					mensaje = UtilImport.validaVigencia(mensaje, "SFE", sfe,
 							rowdata.getFechaContable());
 
 					if (!mensaje.isEmpty()) {
 						loteValido=false;
 						
 						storeImportIngresoDiarioError(rowdata, mensaje,
 								imp_repo);
 						continue;
 					}*/
 
 					// Obtenemos los padres de cada nivel.
 					/*String uo = UtilImport.obtenPadreParty(ledger_repo,
 							ue.getPartyId());
 					String ur = UtilImport.obtenPadreParty(ledger_repo, uo);
 					String con = UtilImport.obtenPadreProductCategory(
 							ledger_repo, n5.getProductCategoryId());
 					String cla = UtilImport.obtenPadreProductCategory(
 							ledger_repo, con);
 					String tip = UtilImport.obtenPadreProductCategory(
 							ledger_repo, cla);
 					String rub = UtilImport.obtenPadreProductCategory(
 							ledger_repo, tip);
 					String sf = UtilImport.obtenPadreEnumeration(ledger_repo,
 							sfe.getEnumId());
 					String f = UtilImport
 							.obtenPadreEnumeration(ledger_repo, sf);
 					String mun = UtilImport.obtenPadreGeo(ledger_repo,
 							loc.getGeoId());
 					String reg = UtilImport.obtenPadreGeo(ledger_repo, mun);
 					String ef = UtilImport.obtenPadreGeo(ledger_repo, reg);*/
 					
 
 					Debug.log("Motor Contable");
 					MotorContable motor = new MotorContable(ledger_repo);
 					// Map<String, String> cuentas = motor.cuentasDiarias(
 					// tipoDoc.getAcctgTransTypeId(), null, null,
 					// rowdata.getOrganizationPartyId(), null,
 					// n5.getProductCategoryId(), rowdata.getIdTipoCatalogo(),
 					// rowdata.getIdPago(), null, null, tip,
 					// false, null, null, rowdata.getIdProducto());
 
 					// Map<String, String> cuentas = motor
 					// .cuentasIngresoDiario(tipoDoc.getAcctgTransTypeId(),
 					// rowdata.getOrganizationPartyId(),
 					// rowdata.getIdPago(), tip,
 					// rowdata.getIdProductoD(),
 					// rowdata.getIdProductoH());
 					Debug.log("Tipo de Transaccion: "+tipoDoc.getAcctgTransTypeId());
 					Debug.log("Organizacion: "+rowdata.getOrganizationPartyId());
 					Debug.log("IDPago: "+rowdata.getIdPago());
 					Debug.log("Cri: "+contenedor.getProduct().getCategoryName());
 					Debug.log("IDProductoD: "+rowdata.getIdProductoD());
 					Debug.log("IDProductoH: "+rowdata.getIdProductoH());
 					Map<String, String> cuentas = motor.cuentasIngresoDiario(
 							tipoDoc.getAcctgTransTypeId(),
 							rowdata.getOrganizationPartyId(),
 							rowdata.getIdPago(), contenedor.getProduct().getCategoryName(),
 							rowdata.getIdProductoD(), rowdata.getIdProductoH());
 					
 					Debug.log("A huevo");
 					if (cuentas.get("Mensaje") != null) {
 						loteValido=false;
 						String message = cuentas.get("Mensaje");
 						storeImportIngresoDiarioError(rowdata, message,
 								imp_repo);
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
 						ingresoDiario.setPartyId(contenedor.getParty().getPartyId());
 						ingresoDiario.setPostedAmount(rowdata.getMonto());
 						ingresoDiario.setDescription(tipoDoc.getDescripcion()
 								+ "-" + rowdata.getRefDoc() + "-P");
						ingresoDiario = UtilImport.setPartyWorkEffortEnAcctTrans(ingresoDiario, contenedor);
 						Debug.log("Antes de ACCTG_TRANS_PRESUPUESTAL");
 						// ACCTG_TRANS_PRESUPUESTAL
 						AcctgTransPresupuestal aux = new AcctgTransPresupuestal();
 						
 						aux.setAgrupador(rowdata.getRefDoc());
 						aux.setIdPago(rowdata.getIdPago());
 						aux.setIdProductoD(rowdata.getIdProductoD());
 						aux.setIdProductoH(rowdata.getIdProductoH());
 						aux.setIdTipoDoc(rowdata.getIdTipoDoc());
 						aux.setSecuencia(rowdata.getSecuencia());
 						aux.setLote(lote);
 						aux.setClasificacion1(rowdata.getClasificacion1());
 						aux.setClasificacion2(rowdata.getClasificacion2());
 						aux.setClasificacion3(rowdata.getClasificacion3());
 						aux.setClasificacion4(rowdata.getClasificacion4());
 						aux.setClasificacion5(rowdata.getClasificacion5());
 						aux.setClasificacion6(rowdata.getClasificacion5());
 						aux.setClasificacion7(rowdata.getClasificacion6());
 						aux.setClasificacion8(rowdata.getClasificacion7());
 						aux.setClasificacion9(rowdata.getClasificacion8());
 						aux.setClasificacion10(rowdata.getClasificacion9());
 						aux.setClasificacion11(rowdata.getClasificacion10());
 						aux.setClasificacion12(rowdata.getClasificacion11());
 						aux.setClasificacion13(rowdata.getClasificacion12());
 						aux.setClasificacion14(rowdata.getClasificacion13());
 						aux.setClasificacion15(rowdata.getClasificacion14());
 						aux.setClavePres(contenedor.getClavePresupuestal());
 						
 						// History
 						Debug.log("Busca periodos");
 						List<CustomTimePeriod> periodos = UtilImport
 								.obtenPeriodos(ledger_repo,
 										rowdata.getOrganizationPartyId(),
 										ingresoDiario.getPostedDate());
 
 						if (cuentas.get("Cuenta Cargo Presupuesto") != null) {
 							Debug.log("Cuenta Presupuestal");
 							ingresoDiario.setDescription(tipoDoc
 									.getDescripcion()
 									+ "-"
 									+ rowdata.getRefDoc() + "-P");
 
 							// id Transaccion
 							ingresoDiario.setAcctgTransId(UtilImport
 									.getAcctgTransIdDiario(rowdata.getRefDoc(),
 											rowdata.getSecuencia(), "P"));
 
 							AcctgTrans trans = ledger_repo.findOne(
 									AcctgTrans.class, ledger_repo.map(
 											AcctgTrans.Fields.acctgTransId,
 											ingresoDiario.getAcctgTransId()));
 
 							if (trans != null) {
 								loteValido=false;
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
 											null);
 							for(int i = 0; i<contenedor.getEnumeration().size(); i++)
 							{
 								String indice = new Integer(i+1).toString();
 								String campo = "acctgTagEnumId" + indice;
 								acctgentry.set(campo, contenedor.getEnumeration().get(i).getEnumId());
 							}
 							imp_tx5 = this.session.beginTransaction();
							
 							ledger_repo.createOrUpdate(acctgentry);
 							imp_tx5.commit();
 
 							GlAccountOrganization glAccountOrganization = UtilImport
 									.actualizaGlAccountOrganization(
 											ledger_repo,
 											rowdata.getMonto(),
 											cuentas.get("Cuenta Cargo Presupuesto"),
 											rowdata.getOrganizationPartyId(),
 											"D");
 							imp_tx7 = this.session.beginTransaction();
 							ledger_repo.createOrUpdate(glAccountOrganization);
 							imp_tx7.commit();
 							
 							// GlAccountHistory
 							Debug.log("Busca histories");
 							List<GlAccountHistory> histories = UtilImport
 									.actualizaGlAccountHistories(
 											ledger_repo,
 											periodos,
 											cuentas.get("Cuenta Cargo Presupuesto"),
 											rowdata.getMonto(), "Debit");
 							
 							Debug.log("Se impactan las histories regresadas");
 							for (GlAccountHistory history : histories) {
 								Transaction txHistory = null;
 								txHistory = this.session.beginTransaction();
 								ledger_repo.createOrUpdate(history);
 								txHistory.commit();
 							}
 
 							acctgentry = UtilImport.generaAcctgTransEntry(
 									ingresoDiario,
 									rowdata.getOrganizationPartyId(), "00002",
 									"C",
 									cuentas.get("Cuenta Abono Presupuesto"),
 									null);
 							for(int i = 0; i<contenedor.getEnumeration().size(); i++)
 							{
 								String indice = new Integer(i+1).toString();
 								String campo = "acctgTagEnumId" + indice;
 								acctgentry.set(campo, contenedor.getEnumeration().get(i).getEnumId());
 							}
 							imp_tx9 = this.session.beginTransaction();
 							ledger_repo.createOrUpdate(acctgentry);
 							imp_tx9.commit();
 
 							glAccountOrganization = UtilImport
 									.actualizaGlAccountOrganization(
 											ledger_repo,
 											rowdata.getMonto(),
 											cuentas.get("Cuenta Abono Presupuesto"),
 											rowdata.getOrganizationPartyId(),
 											"A");
 							imp_tx11 = this.session.beginTransaction();
 							ledger_repo.createOrUpdate(glAccountOrganization);
 							imp_tx11.commit();
 							
 							// GlAccountHistory
 							Debug.log("Busca histories");
 							histories = UtilImport
 									.actualizaGlAccountHistories(
 											ledger_repo,
 											periodos,
 											cuentas.get("Cuenta Abono Presupuesto"),
 											rowdata.getMonto(), "Credit");
 							
 							Debug.log("Se impactan las histories regresadas");
 							for (GlAccountHistory history : histories) {
 								Transaction txHistory = null;
 								txHistory = this.session.beginTransaction();
 								ledger_repo.createOrUpdate(history);
 								txHistory.commit();
 							}
 
 						}
 
 						if (cuentas.get("Cuenta Cargo Contable") != null) {
 							Debug.log("Cuenta Contable");
 							ingresoDiario.setDescription(tipoDoc
 									.getDescripcion()
 									+ "-"
 									+ rowdata.getRefDoc() + "-C");
 
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
 									.generaAcctgTransEntry(
 											ingresoDiario,
 											rowdata.getOrganizationPartyId(),
 											"00001",
 											"D",
 											cuentas.get("Cuenta Cargo Contable"),
 											null);
 							for(int i = 0; i<contenedor.getEnumeration().size(); i++)
 							{
 								String indice = new Integer(i+1).toString();
 								String campo = "acctgTagEnumId" + indice;
 								acctgentry.set(campo, contenedor.getEnumeration().get(i).getEnumId());
 							}
 							imp_tx6 = this.session.beginTransaction();
 							ledger_repo.createOrUpdate(acctgentry);
 							imp_tx6.commit();
 
 							GlAccountOrganization glAccountOrganization = UtilImport
 									.actualizaGlAccountOrganization(
 											ledger_repo,
 											rowdata.getMonto(),
 											cuentas.get("Cuenta Cargo Contable"),
 											rowdata.getOrganizationPartyId(),
 											"D");
 							imp_tx8 = this.session.beginTransaction();
 							ledger_repo.createOrUpdate(glAccountOrganization);
 							imp_tx8.commit();
 							
 							// GlAccountHistory
 							Debug.log("Busca histories");
 							List<GlAccountHistory> histories = UtilImport
 									.actualizaGlAccountHistories(
 											ledger_repo,
 											periodos,
 											cuentas.get("Cuenta Cargo Contable"),
 											rowdata.getMonto(), "Debit");
 
 							Debug.log("Se impactan las histories regresadas");
 							for (GlAccountHistory history : histories) {
 								Transaction txHistory = null;
 								txHistory = this.session.beginTransaction();
 								ledger_repo.createOrUpdate(history);
 								txHistory.commit();
 							}
 
 							acctgentry = UtilImport.generaAcctgTransEntry(
 									ingresoDiario,
 									rowdata.getOrganizationPartyId(), "00002",
 									"C", cuentas.get("Cuenta Abono Contable"),
 									null);
 							for(int i = 0; i<contenedor.getEnumeration().size(); i++)
 							{
 								String indice = new Integer(i+1).toString();
 								String campo = "acctgTagEnumId" + indice;
 								acctgentry.set(campo, contenedor.getEnumeration().get(i).getEnumId());
 							}
 							imp_tx10 = this.session.beginTransaction();
 							ledger_repo.createOrUpdate(acctgentry);
 							imp_tx10.commit();
 
 							glAccountOrganization = UtilImport
 									.actualizaGlAccountOrganization(
 											ledger_repo,
 											rowdata.getMonto(),
 											cuentas.get("Cuenta Abono Contable"),
 											rowdata.getOrganizationPartyId(),
 											"A");
 							imp_tx12 = this.session.beginTransaction();
 							ledger_repo.createOrUpdate(glAccountOrganization);
 							imp_tx12.commit();
 							
 							// GlAccountHistory
 							Debug.log("Busca histories");
 							histories = UtilImport
 									.actualizaGlAccountHistories(
 											ledger_repo,
 											periodos,
 											cuentas.get("Cuenta Abono Contable"),
 											rowdata.getMonto(), "Credit");
 
 							Debug.log("Se impactan las histories regresadas");
 							for (GlAccountHistory history : histories) {
 								Transaction txHistory = null;
 								txHistory = this.session.beginTransaction();
 								ledger_repo.createOrUpdate(history);
 								txHistory.commit();
 							}
 						}
 
 						if (mensaje.isEmpty()) {
 							String message = "Se importo correctamente Ingreso Diario ["
 									+ contenedor.getClavePresupuestal() + "].";
 							this.storeImportIngresoDiarioSuccess(rowdata,
 									imp_repo);
 							Debug.logInfo(message, MODULE);
 							imported = imported + 1;
 						}
 
 					} catch (Exception ex) {
 						String message =  ex.getMessage();
 						storeImportIngresoDiarioError(rowdata, message,
 								imp_repo);
 
 						// rollback all if there was an error when importing
 						// item
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
 
 				// Se inserta el Lote.
 				if (!lote.equalsIgnoreCase("X")&&loteValido) {
 					LoteTransaccion loteTrans = new LoteTransaccion();
 					loteTrans.setIdLote(lote);
 					loteTrans.setTipoTransaccion("IngresoDiario");
 					Transaction transLote = null;
 					transLote = this.session.beginTransaction();
 					ledger_repo.createOrUpdate(loteTrans);
 					transLote.commit();
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
