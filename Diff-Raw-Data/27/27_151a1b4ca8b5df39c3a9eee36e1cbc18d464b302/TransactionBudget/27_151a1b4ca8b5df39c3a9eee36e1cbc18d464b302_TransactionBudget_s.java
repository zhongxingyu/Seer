 /*
 
  */
 package com.opensourcestrategies.financials.transactions;
 
 import java.math.BigDecimal;
 import java.sql.Timestamp;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 import javolution.util.FastList;
 
 import org.ofbiz.base.util.Debug;
 import org.ofbiz.base.util.GeneralException;
 import org.ofbiz.base.util.UtilMisc;
 import org.ofbiz.base.util.UtilValidate;
 import org.ofbiz.entity.Delegator;
 import org.ofbiz.entity.GenericValue;
 import org.ofbiz.entity.condition.EntityCondition;
 import org.ofbiz.entity.condition.EntityOperator;
 import org.ofbiz.service.DispatchContext;
 import org.ofbiz.service.GenericServiceException;
 import org.ofbiz.service.LocalDispatcher;
 import org.ofbiz.service.ModelService;
 import org.ofbiz.service.ServiceUtil;
 import org.opentaps.base.entities.AcctgTrans;
 import org.opentaps.base.entities.AcctgTransPresupuestal;
 import org.opentaps.base.entities.CustomTimePeriod;
 import org.opentaps.base.entities.Enumeration;
 import org.opentaps.base.entities.GlAccountHistory;
 import org.opentaps.base.entities.GlAccountOrganization;
 import org.opentaps.base.entities.NivelPresupuestal;
 import org.opentaps.base.entities.Party;
 import org.opentaps.base.entities.PartyAcctgPreference;
 import org.opentaps.base.entities.PartyGroup;
 import org.opentaps.base.entities.ProductCategory;
 import org.opentaps.base.entities.WorkEffort;
 import org.opentaps.common.util.UtilAccountingTags;
 import org.opentaps.common.util.UtilCommon;
 import org.opentaps.common.util.UtilMessage;
 import org.opentaps.domain.DomainsDirectory;
 import org.opentaps.domain.DomainsLoader;
 import org.opentaps.domain.ledger.LedgerRepositoryInterface;
 import org.opentaps.domain.organization.Organization;
 import org.opentaps.domain.organization.OrganizationRepositoryInterface;
 import org.opentaps.foundation.action.ActionContext;
 import org.opentaps.foundation.infrastructure.Infrastructure;
 import org.opentaps.foundation.infrastructure.User;
 import org.opentaps.foundation.repository.RepositoryException;
 import org.opentaps.foundation.service.ServiceException;
 
 import com.opensourcestrategies.financials.util.UtilBudget;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 
 
 /**
  * TransactionActions - Java Actions for Transactions.
  */
 public class TransactionBudget {
 
 	private static final String MODULE = TransactionBudget.class.getName();
 	private static final Double ZERO = Double.valueOf(0);
 
 	/**
 	 * Action for the find / list transactions screen.
 	 * 
 	 * @param context
 	 *            the screen context
 	 * @throws GeneralException
 	 *             if an error occurs
 	 * @throws ParseException
 	 *             if an error occurs
 	 */
 	
 	/**
 	 * Create a Quick <code>AcctgTrans</code> record. IsPosted is forced to "N".
 	 * Creates an Quick AcctgTrans and two offsetting AcctgTransEntry records.
 	 * 
 	 * @param dctx
 	 *            a <code>DispatchContext</code> value
 	 * @param context
 	 *            a <code>Map</code> value
 	 * @return a <code>Map</code> value
 	 */
 	@SuppressWarnings("unchecked")
 	public static Map createAcctgTransPresupuestoIngreso(DispatchContext dctx,
 			Map context) {
 		Debug.log("Si entro al servicio createAcctgTransPresupuestoIngreso");
 
 		LocalDispatcher dispatcher = dctx.getDispatcher();
 		GenericValue userLogin = (GenericValue) context.get("userLogin");
 
 		try {
 			DomainsLoader dl = new DomainsLoader(
 					new Infrastructure(dispatcher), new User(userLogin));
 			OrganizationRepositoryInterface organizationRepository = dl
 					.loadDomainsDirectory().getOrganizationDomain()
 					.getOrganizationRepository();
 
 			String organizationPartyId = (String) context
 					.get("organizationPartyId");
 
 			String glFiscalTypeId = (String) context.get("glFiscalTypeId");
 			String acctgTransTypeId = (String) context.get("acctgTransTypeId");
 			String idRubro = (String) context.get("idRubro");
 			String idTipo = (String) context.get("idTipo");
 			String idClase = (String) context.get("idClase");
 			String idConcepto = (String) context.get("idConcepto");
 			String idN5 = (String) context.get("idN5");
 			String EntidadFederativa = (String) context
 					.get("EntidadFederativa");
 			String Region = (String) context.get("Region");
 			String Municipio = (String) context.get("Municipio");
 			String Localidad = (String) context.get("Localidad");
 			String fechaTransaccion = (String) context.get("fechaTransaccion");
 			String fechaContable = (String) context.get("fechaContable");
 			String clave = UtilBudget.getClavePresupuestal(context, dispatcher); 
 			String monto = (String) context.get("monto");
 			String referencia = (String) context.get("referencia");
 			String descripcion = (String) context.get("description");
 			String unidadEjecutora = (String) context.get("unidadEjecutora");
 			Double amount = (Double) context.get("amount");
 			Debug.log("organizacion" + organizationPartyId);
 			Debug.log("glFiscalTypeId" + glFiscalTypeId);
 			Debug.log("acctgTransTypeId" + acctgTransTypeId);
 			Debug.log("idRubro" + idRubro);
 			Debug.log("idTipo" + idTipo);
 			Debug.log("idClase" + idClase);
 			Debug.log("idConcepto" + idConcepto);
 			Debug.log("idN5" + idN5);
 			Debug.log("EntidadFederativa" + EntidadFederativa);
 			Debug.log("Region" + Region);
 			Debug.log("Municipio" + Municipio);
 			Debug.log("Localidad" + Localidad);
 			Debug.log("fechaTransaccion" + fechaTransaccion);
 			Debug.log("fechaContable" + fechaContable);
 			Debug.log("clave" + clave);
 			Debug.log("monto" + monto);
 			Debug.log("referencia" + referencia);
 			Debug.log("descripcion" + descripcion);
 			Debug.log("amount" + amount);
 			
     		if(amount.compareTo(ZERO) <= 0){
 				Debug.logError("El monto debe ser MAYOR A CERO",MODULE);
 				throw new ServiceException(String.format("El monto debe ser MAYOR A CERO"));
     			
     		}
 
 			Organization organization = organizationRepository
 					.getOrganizationById(organizationPartyId);
 
 			Date fechaTrasac = UtilBudget.getDateTransaction(fechaTransaccion);
 			Date fechaConta = UtilBudget.getDateTransaction(fechaContable);
 			
 			// create the accounting transaction
 			String glFiscalType = getAcctgTransTypeId("TINGRESOESTIMADO", dispatcher);
 			
 			Debug.log("Tipo glFiscalType " + glFiscalType);
 			
 
 			Map createAcctgTransCtx = dctx.getModelService("createAcctgTransBugetManual")
 					.makeValid(context, ModelService.IN_PARAM);
 			if (UtilValidate.isEmpty(createAcctgTransCtx
 					.get("fechaTransaccion"))) {
 				
 				createAcctgTransCtx.put("transactionDate", fechaTransaccion);
 				createAcctgTransCtx.put("description", clave + "-" +  String.format("%02d",fechaConta.getMonth()+1));
 				createAcctgTransCtx.put("acctgTransTypeId", "TINGRESOESTIMADO");
 				createAcctgTransCtx.put("glFiscalTypeId" ,glFiscalType);
 				createAcctgTransCtx.put("partyId" ,organizationPartyId);
 				//createAcctgTransCtx.put("createdByUserLogin" ,"admin");
 				createAcctgTransCtx.put("postedAmount", amount);
 				//createdByUserLogin
 				//lastModifiedByUserLogin
 				//postedAmount
 				
 
 			}

			String clasif = getClasifNull(dispatcher,
					organizationPartyId, context, "INGRESO");
			
			if(clasif.equals("Nok"))
				throw new ServiceException(String.format("Deben de llenarse todas las clasificaciones"));
 				
 			Map results = dispatcher.runSync("createAcctgTransBugetManual",
 					createAcctgTransCtx);
 
 			if (!UtilCommon.isSuccess(results)) {
 				Debug.log("results" + results.toString());
 				return UtilMessage.createAndLogServiceError(results, MODULE);
 
 			}
 			String acctgTransId = (String) results.get("acctgTransId");
 			Debug.log("acctgTransId" + acctgTransId);
 
 			UtilCommon.isSuccess(createAcctgPresupuestal(context, acctgTransId,
 					dctx));
 
 			// create both debit and credit entries
 			String currencyUomId = (String) context.get("currencyUomId");
 			if (UtilValidate.isEmpty(currencyUomId)) {
 				PartyAcctgPreference partyAcctgPref = organization
 						.getPartyAcctgPreference();
 				if (partyAcctgPref != null) {
 					currencyUomId = partyAcctgPref.getBaseCurrencyUomId();
 				} else {
 					Debug.logWarning(
 							"No accounting preference found for organization: "
 									+ organizationPartyId, MODULE);
 				}
 			}
 
 			EntityCondition condicion = EntityCondition.makeCondition(
 					"acctgTransTypeId", acctgTransTypeId);
 			List<GenericValue> cuentas = dispatcher
 					.getDelegator()
 					.findByCondition("MiniGuiaContable", condicion,
 							UtilMisc.toList("cuentaCargo", "cuentaAbono"), null);
 			//AcctgTransEntry.Fields.acctgTagEnumId3
 			
 			String debitGlAccountId = "";
 			String creditGlAccountId = "";
 
 			for (GenericValue genericValue : cuentas) {
 				Debug.log("cuentaCargo"
 						+ genericValue.get("cuentaCargo").toString());
 				Debug.log("cuentaAbono"
 						+ genericValue.get("cuentaAbono").toString());
 				debitGlAccountId = genericValue.get("cuentaCargo").toString();
 				creditGlAccountId = genericValue.get("cuentaAbono").toString();
 			}
 
 			String rubro = (String) context.get("idRubro");
 			Debug.log("idRubro" + rubro);
 			// debit entry, using createAcctgTransEntryManual which validate the
 			// accounting tags, the tags for are prefixed by "debitTagEnumId"
 			Map createAcctgTransEntryCtx = dctx.getModelService(
 					"createAcctgTransEntryManual").makeValid(context,
 					ModelService.IN_PARAM);
 
 			Map debitCtx = new HashMap(createAcctgTransEntryCtx);
 			UtilAccountingTags.addTagParameters(context, debitCtx,
 					"debitTagEnumId", UtilAccountingTags.ENTITY_TAG_PREFIX);
 			debitCtx.put("acctgTransId", acctgTransId);
 			debitCtx.put("glAccountId", debitGlAccountId);
 			debitCtx.put("debitCreditFlag", "D");
 			debitCtx.put("acctgTransEntryTypeId", "_NA_");
 			debitCtx.put("currencyUomId", currencyUomId);
 			debitCtx.put("description", clave + "-" +  String.format("%02d",fechaConta.getMonth()+1));
 			debitCtx.put("acctgTagEnumId3", (String) context.get("subFuenteEsp"));
 			results = dispatcher.runSync("createAcctgTransEntryManual",
 					debitCtx);
 			
 			// credit entry, the tags for are prefixed by "creditTagEnumId"
 			Map creditCtx = new HashMap(createAcctgTransEntryCtx);
 			UtilAccountingTags.addTagParameters(context, creditCtx,
 					"creditTagEnumId", UtilAccountingTags.ENTITY_TAG_PREFIX);
 			creditCtx.put("acctgTransId", acctgTransId);
 			creditCtx.put("glAccountId", creditGlAccountId);
 			creditCtx.put("debitCreditFlag", "C");
 			creditCtx.put("acctgTransEntryTypeId", "_NA_");
 			creditCtx.put("currencyUomId", currencyUomId);
 			creditCtx.put("description", clave + "-" +  String.format("%02d",fechaConta.getMonth()+1));
 			creditCtx.put("acctgTagEnumId3", (String) context.get("subFuenteEsp"));
 			results = dispatcher.runSync("createAcctgTransEntryManual",
 					creditCtx);
 			
 			
 
 			results = ServiceUtil.returnSuccess();
 			results.put("acctgTransId", acctgTransId);
 
 			postedTransaccion(acctgTransId, fechaConta, dctx, userLogin);
 			glOrganizationHistory(debitGlAccountId, creditGlAccountId, dctx,
 					organizationPartyId, userLogin, amount, fechaConta);
 			return results;
 
 		} catch (GeneralException e) {
 			return UtilMessage.createAndLogServiceError(e, MODULE);
 		}
 	}
 
 	
 
 	private static void glOrganizationHistory(String debitGlAccountId,
 			String creditGlAccountId, DispatchContext dctx,
 			String organizationPartyId, GenericValue userLogin, double amount, Date fechaConta)
 			throws RepositoryException {
 
 		Delegator delegator = dctx.getDelegator();
 		LocalDispatcher dispatcher = dctx.getDispatcher();
 		LedgerRepositoryInterface ledgerRepository = new DomainsLoader(
 				new Infrastructure(dispatcher), new User(userLogin))
 				.loadDomainsDirectory().getLedgerDomain().getLedgerRepository();
 
 		BigDecimal monto = new BigDecimal(amount);
 		String cuenta = debitGlAccountId;
 		String tipo = "D";
 		try {
 			
 			List<CustomTimePeriod> periodos = getPeriodos(organizationPartyId,
 					fechaConta, ledgerRepository);
 			
 			for (int i = 0; i < 2; i++) {
 
 				if (i != 0)
 				{
 					cuenta = creditGlAccountId;
 					tipo = "C";
 				}
 					
 
 				GlAccountOrganization glorganization = ledgerRepository
 						.findOne(
 								GlAccountOrganization.class,
 								ledgerRepository
 										.map(GlAccountOrganization.Fields.glAccountId,
 												cuenta,
 												GlAccountOrganization.Fields.organizationPartyId,
 												organizationPartyId));
 
 				if (glorganization.getPostedBalance() == null) {
 					glorganization.setPostedBalance(monto);
 				} else {
 					glorganization.setPostedBalance(glorganization
 							.getPostedBalance().add(monto));
 				}
 
 				ledgerRepository.createOrUpdate(glorganization);
 				
 				
 				
 				for (CustomTimePeriod customTimePeriod : periodos) {
 					
 					GlAccountHistory glhistory = ledgerRepository
 							.findOne(
 									GlAccountHistory.class,
 									ledgerRepository
 											.map(GlAccountHistory.Fields.glAccountId,
 													cuenta,
 													GlAccountHistory.Fields.organizationPartyId,
 													organizationPartyId,
 													GlAccountHistory.Fields.customTimePeriodId,
 													customTimePeriod
 															.getCustomTimePeriodId()));
 					BigDecimal saldoD = BigDecimal.ZERO;
 					BigDecimal saldoC = BigDecimal.ZERO;
 					BigDecimal amouts = BigDecimal.valueOf(amount);
 					if(glhistory!= null)
 					{
 						if(tipo == "D")
 						{
 							if(glhistory.getPostedDebits()==null)
 							{
 								saldoD =  amouts;
 							}
 							else
 							{
 								saldoD = glhistory.getPostedDebits().add(amouts);
 							}
 						}
 						else
 						{
 							if(glhistory.getPostedCredits()==null)
 							{
 								saldoC =  amouts;
 							}
 							else
 							{
 								saldoC = glhistory.getPostedCredits().add(amouts);
 							}
 						}
 						glhistory.setPostedDebits(saldoD);
 						glhistory.setPostedCredits(saldoC);
 						ledgerRepository.createOrUpdate(glhistory);
 						
 					}
 					else
 					{
 						GlAccountHistory glaccounthistory = new GlAccountHistory();
 						glaccounthistory.setGlAccountId(cuenta);
 						glaccounthistory.setOrganizationPartyId(organizationPartyId);
 						glaccounthistory.setCustomTimePeriodId(customTimePeriod.getCustomTimePeriodId());
 						
 						if(tipo == "D")
 						{
 							
 							glaccounthistory.setPostedDebits(amouts);
 							
 						}
 						else
 						{
 							glaccounthistory.setPostedCredits(amouts);
 						}
 						
 						ledgerRepository.createOrUpdate(glaccounthistory);
 						
 					}
 					
 				}
 
 			}
 			
 			
 
 		} catch (Exception e) {
 
 		}
 
 	}
 
 	/**
 	 * @param organizationPartyId
 	 * @param fechaConta
 	 * @param ledgerRepository
 	 * @return
 	 * @throws RepositoryException
 	 */
 	private static List<CustomTimePeriod> getPeriodos(
 			String organizationPartyId, Date fechaConta,
 			LedgerRepositoryInterface ledgerRepository)
 			throws RepositoryException {
 		EntityCondition condicion = EntityCondition.makeCondition(EntityOperator.AND,
 		        EntityCondition.makeCondition("organizationPartyId", EntityOperator.EQUALS,organizationPartyId),
 		        EntityCondition.makeCondition("isClosed", EntityOperator.EQUALS,"N"),
 		        EntityCondition.makeCondition("fromDate", EntityOperator.LESS_THAN_EQUAL_TO,fechaConta),
 		        EntityCondition.makeCondition("thruDate", EntityOperator.GREATER_THAN_EQUAL_TO,fechaConta));
 
 		List<CustomTimePeriod> periodos = ledgerRepository.findList(CustomTimePeriod.class, condicion);
 		return periodos;
 	}
 
 	private static void getPeriodos() {
 		try {
 			
 			
 			
 		} catch (Exception e) {
 			// TODO: handle exception
 		}
 		
 	}
 
 	private static void postedTransaccion(String acctgTransId, Date fechaConta,
 			DispatchContext dctx, GenericValue userLogin)
 			throws RepositoryException {
 
 		Delegator delegator = dctx.getDelegator();
 		LocalDispatcher dispatcher = dctx.getDispatcher();
 		LedgerRepositoryInterface ledgerRepository = new DomainsLoader(
 				new Infrastructure(dispatcher), new User(userLogin))
 				.loadDomainsDirectory().getLedgerDomain().getLedgerRepository();
 		try {
 
 			// Fin
 			AcctgTrans acctgtrans = ledgerRepository.findOne(AcctgTrans.class,
 					ledgerRepository.map(AcctgTrans.Fields.acctgTransId,
 							acctgTransId));
 
 			if (acctgtrans != null) {
 				Timestamp timestamp = new Timestamp(fechaConta.getTime());
 				acctgtrans.setIsPosted("Y");
 				acctgtrans.setPostedDate(timestamp);
 				
 			
 
 				ledgerRepository.createOrUpdate(acctgtrans);
 			}
 
 		} catch (Exception e) {
 			Debug.log("Error al publicar Transaccion: acctgTransId[" + acctgTransId + "] " + e);
 		}
 
 	}
 
 	
 
 	private static Map<String, Object> createAcctgPresupuestal(Map context,
 			String acctgTransId, DispatchContext dctx) {
 
 		Delegator delegator = dctx.getDelegator();
 		LocalDispatcher dispatcher = dctx.getDispatcher();
 		Locale locale = UtilCommon.getLocale(context);
 		GenericValue userLogin = (GenericValue) context.get("userLogin");
 		Debug.log("Si entro al createAcctgTransPresupuestalManual, acctgTransId "
 				+ context.get("acctgTransId"));
 
 		try {
 			LedgerRepositoryInterface ledgerRepository = new DomainsLoader(
 					new Infrastructure(dispatcher), new User(userLogin))
 					.loadDomainsDirectory().getLedgerDomain()
 					.getLedgerRepository();
 
 			String fechaContable = (String) context.get("fechaContable");
 			String UE = (String) context.get("unidadEjecutora");
 			String UO = UtilBudget.getParentParty(UE, dispatcher);
 			String UR = UtilBudget.getParentParty(UO, dispatcher);
 			
 			String nivel5 = (String) context.get("idN5");
 			String conceptoRUB = UtilBudget.getParentProductCategory(nivel5, dispatcher);
 			String clase = UtilBudget.getParentProductCategory(conceptoRUB, dispatcher);
 			String tipo = UtilBudget.getParentProductCategory(clase, dispatcher);
 			String rubro = UtilBudget.getParentProductCategory(tipo, dispatcher);
 
 			String SubFuenteEspecifica = (String) context.get("subFuenteEsp");
 			String SubFuente = UtilBudget.getParentEnumeration(SubFuenteEspecifica,
 					dispatcher);
 			String Fuente = UtilBudget.getParentEnumeration(SubFuente, dispatcher);
 			
 			String subFuncion = (String) context.get("subfuncion");
 			String funcion = UtilBudget.getParentEnumeration(subFuncion, dispatcher);
 			String finalidad = UtilBudget.getParentEnumeration(funcion, dispatcher);
 			
 			String actividad = (String) context.get("actividad");
 			String subprogramap = UtilBudget.getParentWorkEffort(actividad, dispatcher);
 			String programa = UtilBudget.getParentWorkEffort(subprogramap, dispatcher);
 			String plan = UtilBudget.getParentWorkEffort(programa, dispatcher);
 			
 			String PE = (String) context.get("partidaEspecifica");
 			String PG = UtilBudget.getParentProductCategory(PE, dispatcher);
 			String conceptoPG = UtilBudget.getParentProductCategory(PG, dispatcher);
 			String capitulo = UtilBudget.getParentProductCategory(conceptoPG, dispatcher);
 			
 			String area = ((String) context.get("area"));
 			String subSector = UtilBudget.getParentEnumeration(area, dispatcher);
 			String sector = UtilBudget.getParentEnumeration(subSector, dispatcher);
 			
 
 			AcctgTransPresupuestal presupuestal = new AcctgTransPresupuestal();
 			presupuestal.initRepository(ledgerRepository);
 
 			presupuestal.setAcctgTransId(acctgTransId);
 			presupuestal.setClavePres(UtilBudget.getClavePresupuestal(context, dispatcher));
 			
 			for (int i = 0; i < 15; i++) {
 				presupuestal.set("clasificacion" + i, ((String) context.get("clasificacion" + i)));
 			}
 			//presupuestal.setClasificacion1(clasificacion1);
 			
 //			clasificadores
 //			presupuestal.setCiclo(String.valueOf((UtilBudget.getDateTransaction(
 //					fechaContable).getYear() + 1900)));
 //			presupuestal.setUnidadResponsable(UR);
 //			presupuestal.setUnidadOrganizacional(UO);
 //			presupuestal.setUnidadEjecutora(UE);
 //			
 //			presupuestal.setRubro(rubro);
 //			presupuestal.setTipo(tipo);
 //			presupuestal.setClase(clase);
 //			presupuestal.setConceptoRub(conceptoRUB);
 //			presupuestal.setNivel5(nivel5);
 //			
 //			presupuestal.setFuente(Fuente);
 //			presupuestal.setSubFuente(SubFuente);
 //			presupuestal.setSubFuenteEspecifica(SubFuenteEspecifica);
 //			
 //			presupuestal.setEntidadFederativa((String) context
 //					.get("EntidadFederativa"));
 //			presupuestal.setRegion((String) context.get("Region"));
 //			presupuestal.setMunicipio((String) context.get("Municipio"));
 //			presupuestal.setLocalidad((String) context.get("Localidad"));
 //			presupuestal.setAgrupador((String) context.get("referencia"));
 //			
 //			presupuestal.setFinalidad(finalidad);
 //			presupuestal.setFuncion(funcion);
 //			presupuestal.setSubFuncion(subFuncion);
 //			
 //			presupuestal.setProgramaPlan(plan);
 //			presupuestal.setProgramaPresupuestario(programa);
 //			presupuestal.setSubProgramaPresupuestario(subprogramap);			
 //			presupuestal.setActividad(actividad);	
 //			
 //			presupuestal.setTipoGasto((String) context.get("tipoGasto"));
 //			
 //			presupuestal.setCapitulo(capitulo);
 //			presupuestal.setConcepto(conceptoPG);
 //			presupuestal.setPartidaGenerica(PG);
 //			presupuestal.setPartidaEspecifica((String) context.get("partidaEspecifica"));
 //			
 //			presupuestal.setSector(sector);
 //			presupuestal.setSubSector(subSector);
 //			presupuestal.setArea(area);
 			
 			presupuestal.setAgrupador((String) context.get("referencia"));
 			// create
 			// presupuestal.setNextSubSeqId(AcctgTransEntry.Fields.acctgTransEntrySeqId.name());
 			ledgerRepository.createOrUpdate(presupuestal);
 
 			Map results = ServiceUtil.returnSuccess();
 			results.put("acctgTransId", presupuestal.getAcctgTransId());
 			return results;
 		} catch (GeneralException e) {
 			return UtilMessage.createAndLogServiceError(e, MODULE);
 		}
 	}
 
 	
 	
 	
 	public static Map createAcctgTransBugetManual(DispatchContext dctx,
 			Map context) throws GenericServiceException {
 
 		Delegator delegator = dctx.getDelegator();
 		LocalDispatcher dispatcher = dctx.getDispatcher();
 		Locale locale = UtilCommon.getLocale(context);
 		GenericValue userLogin = (GenericValue) context.get("userLogin");
 		
 		Debug.log("Si entro al createAcctgTransBugetManual");
 
 		try {
 			LedgerRepositoryInterface ledgerRepository = new DomainsLoader(
 					new Infrastructure(dispatcher), new User(userLogin))
 					.loadDomainsDirectory().getLedgerDomain()
 					.getLedgerRepository();
 			String acctgTransId = ledgerRepository.getNextSeqId("AcctgTrans");
 			Debug.log("acctgTransId " + acctgTransId);
 			
 			AcctgTrans acctgTrans = new AcctgTrans();
 			acctgTrans.initRepository(ledgerRepository);
 			
 			Date fecha = UtilBudget.getDateTransaction((String) context.get("transactionDate"));
 			Timestamp timestamp = new Timestamp(fecha.getTime());
 			String acctgTransTypeId = (String) context.get("acctgTransTypeId");
 			BigDecimal postedAmount = new BigDecimal((Double) context.get("postedAmount"));
 			
 			acctgTrans.setPartyId((String) context.get("partyId"));			
 			acctgTrans.setDescription((String) context.get("description"));			
 			acctgTrans.setAcctgTransTypeId(acctgTransTypeId);
 			acctgTrans.setPostedAmount(postedAmount);
 			acctgTrans.setGlFiscalTypeId((String) context.get("glFiscalTypeId"));
 			
 			acctgTrans.setCreatedByUserLogin(userLogin.getString("userLoginId"));
 			acctgTrans.setLastModifiedByUserLogin(userLogin.getString("userLoginId"));
 			
 			acctgTrans.setTransactionDate(timestamp);
 			
 			String mes =  String.format("%02d",fecha.getMonth()+1);
 			String annio = String.valueOf(fecha.getYear() + 1900).substring(2);
 			if(acctgTransTypeId.equals("TINGRESOESTIMADO"))
 			{
 				acctgTransId = acctgTransId + " I" + annio + "-" + mes; 
 			}
 			else if(acctgTransTypeId.equals("TPRESUPAPROBADO"))
 			{
 				acctgTransId = acctgTransId + " E" + annio + "-" + mes; 
 			}
 			//acctgTrans.setDescription(description);
 			acctgTrans.setAcctgTransId(acctgTransId);
 			// create
 			// presupuestal.setNextSubSeqId(AcctgTransEntry.Fields.acctgTransEntrySeqId.name());
 			ledgerRepository.createOrUpdate(acctgTrans);
 
 			Map results = ServiceUtil.returnSuccess();
 			results.put("acctgTransId", acctgTrans.getAcctgTransId());
 			return results;
 		} catch (GeneralException e) {
 			return UtilMessage.createAndLogServiceError(e, MODULE);
 		}
 	}
 	
 	
 	
 	/**
 	 * Create a Quick <code>AcctgTrans</code> record. IsPosted is forced to "N".
 	 * Creates an Quick AcctgTrans and two offsetting AcctgTransEntry records.
 	 * 
 	 * @param dctx
 	 *            a <code>DispatchContext</code> value
 	 * @param context
 	 *            a <code>Map</code> value
 	 * @return a <code>Map</code> value
 	 */
 	@SuppressWarnings("unchecked")
 	public static Map createAcctgTransPresupuestoEgreso(DispatchContext dctx,
 			Map context) {
 		Debug.log("Si entro al servicio createAcctgTransPresupuestoEgreso");
 
 		LocalDispatcher dispatcher = dctx.getDispatcher();
 		GenericValue userLogin = (GenericValue) context.get("userLogin");
 
 		try {
 			DomainsLoader dl = new DomainsLoader(
 					new Infrastructure(dispatcher), new User(userLogin));
 			OrganizationRepositoryInterface organizationRepository = dl
 					.loadDomainsDirectory().getOrganizationDomain()
 					.getOrganizationRepository();
 
 			String organizationPartyId = (String) context
 					.get("organizationPartyId");
 
 			//String glFiscalTypeId = (String) context.get("glFiscalTypeId");
 			String acctgTransTypeId = (String) context.get("acctgTransTypeId");
 			String subfuncion = (String) context.get("subfuncion");
 			String actividad = (String) context.get("actividad");
 			String tipoGasto = (String) context.get("tipoGasto");
 			String partidaEspecifica = (String) context.get("partidaEspecifica");
 			String area = (String) context.get("area");
 			String EntidadFederativa = (String) context
 					.get("EntidadFederativa");
 			String Region = (String) context.get("Region");
 			String Municipio = (String) context.get("Municipio");
 			String Localidad = (String) context.get("Localidad");
 			String fechaTransaccion = (String) context.get("fechaTransaccion");
 			String fechaContable = (String) context.get("fechaContable");
 			String clave = UtilBudget.getClavePresupuestal(context, dispatcher); 
 			//String monto = (String) context.get("amount");
 			String referencia = (String) context.get("referencia");
 			String descripcion = (String) context.get("description");
 			String unidadEjecutora = (String) context.get("unidadEjecutora");
 			Double amount = (Double) context.get("amount");
 			Debug.log("organizacion" + organizationPartyId);
 			//Debug.log("glFiscalTypeId" + glFiscalTypeId);
 			Debug.log("acctgTransTypeId" + acctgTransTypeId);
 			Debug.log("area" + subfuncion);
 			Debug.log("actividad" + actividad);
 			Debug.log("tipoGasto" + tipoGasto);
 			Debug.log("partidaEspecifica" + partidaEspecifica);
 			Debug.log("area" + area);
 			Debug.log("EntidadFederativa" + EntidadFederativa);
 			Debug.log("Region" + Region);
 			Debug.log("Municipio" + Municipio);
 			Debug.log("Localidad" + Localidad);
 			Debug.log("fechaTransaccion" + fechaTransaccion);
 			Debug.log("fechaContable" + fechaContable);
 			Debug.log("clave" + clave);
 			//Debug.log("monto" + monto);
 			Debug.log("referencia" + referencia);
 			Debug.log("descripcion" + descripcion);
 			Debug.log("amount" + amount);
 			
     		if(amount.compareTo(ZERO) <= 0){
 				Debug.logError("El monto debe ser MAYOR A CERO",MODULE);
 				throw new ServiceException(String.format("El monto debe ser MAYOR A CERO"));
     			
     		}
 
 			Organization organization = organizationRepository
 					.getOrganizationById(organizationPartyId);
 
 			Date fechaTrasac = UtilBudget.getDateTransaction(fechaTransaccion);
 			Date fechaConta = UtilBudget.getDateTransaction(fechaContable);
 			// create the accounting transaction
 			
 			String glFiscalTypeId = getAcctgTransTypeId("TPRESUPAPROBADO", dispatcher);
 			Debug.log("Tipo glFiscalTypeId" +  glFiscalTypeId);
 			
 			Map createAcctgTransCtx = dctx.getModelService("createAcctgTransBugetManual")
 					.makeValid(context, ModelService.IN_PARAM);
 			if (UtilValidate.isEmpty(createAcctgTransCtx
 					.get("fechaTransaccion"))) {				
 				
 				
 				createAcctgTransCtx.put("transactionDate", fechaTransaccion);
 				createAcctgTransCtx.put("description", clave + "-" +  String.format("%02d",fechaConta.getMonth()+1));
 				createAcctgTransCtx.put("acctgTransTypeId", "TPRESUPAPROBADO");
 				createAcctgTransCtx.put("glFiscalTypeId" ,glFiscalTypeId);
 				createAcctgTransCtx.put("partyId" ,organizationPartyId);
 				//createAcctgTransCtx.put("createdByUserLogin" ,"admin");
 				createAcctgTransCtx.put("postedAmount", amount);
 			}
			
			String clasif = getClasifNull(dispatcher,
					organizationPartyId, context, "EGRESO");
			
			if(clasif.equals("Nok"))
				throw new ServiceException(String.format("Deben de llenarse todas las clasificaciones"));
 
 			Map results = dispatcher.runSync("createAcctgTransBugetManual",
 					createAcctgTransCtx);
 
 			if (!UtilCommon.isSuccess(results)) {
 				Debug.log("results" + results.toString());
 				return UtilMessage.createAndLogServiceError(results, MODULE);
 
 			}
 			String acctgTransId = (String) results.get("acctgTransId");
 			Debug.log("acctgTransId" + acctgTransId);
 
 			UtilCommon.isSuccess(createAcctgPresupuestal(context, acctgTransId,
 					dctx));
 
 			// create both debit and credit entries
 			String currencyUomId = (String) context.get("currencyUomId");
 			if (UtilValidate.isEmpty(currencyUomId)) {
 				PartyAcctgPreference partyAcctgPref = organization
 						.getPartyAcctgPreference();
 				if (partyAcctgPref != null) {
 					currencyUomId = partyAcctgPref.getBaseCurrencyUomId();
 				} else {
 					Debug.logWarning(
 							"No accounting preference found for organization: "
 									+ organizationPartyId, MODULE);
 				}
 			}
 
 			EntityCondition condicion = EntityCondition.makeCondition(
 					"acctgTransTypeId", acctgTransTypeId);
 			List<GenericValue> cuentas = dispatcher
 					.getDelegator()
 					.findByCondition("MiniGuiaContable", condicion,
 							UtilMisc.toList("cuentaCargo", "cuentaAbono"), null);
 
 			String debitGlAccountId = "";
 			String creditGlAccountId = "";
 
 			for (GenericValue genericValue : cuentas) {
 				Debug.log("cuentaCargo"
 						+ genericValue.get("cuentaCargo").toString());
 				Debug.log("cuentaAbono"
 						+ genericValue.get("cuentaAbono").toString());
 				debitGlAccountId = genericValue.get("cuentaCargo").toString();
 				creditGlAccountId = genericValue.get("cuentaAbono").toString();
 			}
 
 			String rubro = (String) context.get("idRubro");
 			Debug.log("idRubro" + rubro);
 			// debit entry, using createAcctgTransEntryManual which validate the
 			// accounting tags, the tags for are prefixed by "debitTagEnumId"
 			Map createAcctgTransEntryCtx = dctx.getModelService(
 					"createAcctgTransEntryManual").makeValid(context,
 					ModelService.IN_PARAM);
 
 			Map debitCtx = new HashMap(createAcctgTransEntryCtx);
 			UtilAccountingTags.addTagParameters(context, debitCtx,
 					"debitTagEnumId", UtilAccountingTags.ENTITY_TAG_PREFIX);
 			debitCtx.put("acctgTransId", acctgTransId);
 			debitCtx.put("glAccountId", debitGlAccountId);
 			debitCtx.put("debitCreditFlag", "D");
 			debitCtx.put("acctgTransEntryTypeId", "_NA_");
 			debitCtx.put("currencyUomId", currencyUomId);
 			debitCtx.put("acctgTagEnumId1", subfuncion);
 			debitCtx.put("acctgTagEnumId2", tipoGasto);
 			debitCtx.put("acctgTagEnumId3", (String) context.get("subFuenteEsp"));
 			debitCtx.put("acctgTagEnumId4", area);
 			debitCtx.put("partyId", organizationPartyId);
 			debitCtx.put("organizationPartyId", organizationPartyId);
 
 			results = dispatcher.runSync("createAcctgTransEntryManual",
 					debitCtx);
 
 			// credit entry, the tags for are prefixed by "creditTagEnumId"
 			Map creditCtx = new HashMap(createAcctgTransEntryCtx);
 			UtilAccountingTags.addTagParameters(context, creditCtx,
 					"creditTagEnumId", UtilAccountingTags.ENTITY_TAG_PREFIX);
 			creditCtx.put("acctgTransId", acctgTransId);
 			creditCtx.put("glAccountId", creditGlAccountId);
 			creditCtx.put("debitCreditFlag", "C");
 			creditCtx.put("acctgTransEntryTypeId", "_NA_");
 			creditCtx.put("currencyUomId", currencyUomId);
 			creditCtx.put("acctgTagEnumId1", subfuncion);
 			creditCtx.put("acctgTagEnumId1", tipoGasto);
 			creditCtx.put("acctgTagEnumId3", (String) context.get("subFuenteEsp"));
 			creditCtx.put("acctgTagEnumId4", area);
 			creditCtx.put("partyId", organizationPartyId);
 			creditCtx.put("organizationPartyId", organizationPartyId);
 			results = dispatcher.runSync("createAcctgTransEntryManual",
 					creditCtx);
 			
 			//AcctgTransEntry.Fields.acctgTagEnumId1
 
 			results = ServiceUtil.returnSuccess();
 			results.put("acctgTransId", acctgTransId);
 
 			postedTransaccion(acctgTransId, fechaConta, dctx, userLogin);
 			glOrganizationHistory(debitGlAccountId, creditGlAccountId, dctx,
 					organizationPartyId, userLogin, amount, fechaConta);
 			return results;
 
 		} catch (GeneralException e) {
 			return UtilMessage.createAndLogServiceError(e, MODULE);
 		}
 	}
 
 	private static String getAcctgTransTypeId(String tipo, LocalDispatcher dispatcher) {
 		String glFiscalTypeIdPres = "";
 		try {
 			
 			
 			EntityCondition condicion = EntityCondition.makeCondition(
 					"acctgTransTypeId", tipo);
 			List<GenericValue> partys = dispatcher.getDelegator()
 					.findByCondition("MiniGuiaContable", condicion,
 							UtilMisc.toList("acctgTransTypeId", "glFiscalTypeIdPres"), null);
 
 			for (GenericValue genericValue : partys) {
 				Debug.log("acctgTransTypeId" + genericValue.get("acctgTransTypeId").toString());
 				Debug.log("glFiscalTypeIdPres" + genericValue.get("glFiscalTypeIdPres").toString());
 				glFiscalTypeIdPres = genericValue.get("glFiscalTypeIdPres").toString();
 			}
 
 			
 		} catch (Exception e) {
 			Debug.log("Error al obtener glFiscalTypeIdPres tipo [" + tipo + "]" + e);
 		}
 		return glFiscalTypeIdPres;
 	}
 
 	/*
 	 * Verificar que las clasificaciones vienen completas o hacen falta
 	 */
 	
 	public static String getClasifNull(LocalDispatcher dispatcher,
 			String Organizacion, Map context, String Tipo) {
 
 		String aviso = null;
 		Calendar c = new GregorianCalendar();
 		String anio = Integer.toString(c.get(Calendar.YEAR));
 		Delegator delegator = dispatcher.getDelegator();
 
 		try {
 
 			EntityCondition condicion = EntityCondition.makeCondition(
 					EntityOperator.AND, EntityCondition.makeCondition(
 							"organizationPartyId", EntityOperator.EQUALS,
 							Organizacion),
 					EntityCondition.makeCondition("acctgTagUsageTypeId",
 							EntityOperator.EQUALS, Tipo),
 					EntityCondition.makeCondition("ciclo",
 							EntityOperator.EQUALS, anio));
 
 			List<GenericValue> resultado = delegator.findByCondition(
 					"EstructuraClave", condicion,
 					UtilMisc.toList(getListColumnas()), null);
 
 			int tam = 0;
 
 			// Se verifica cuantas clasificaciones tiene el Ingreso o Egreso
 			// dentro de su clave
 			if (!resultado.isEmpty()) {
 				for (GenericValue genericValue : resultado) {
 					for (int j = 1; j < 16; j++) {
 						try {
 							if ((genericValue.get("clasificacion"
 									+ String.valueOf(j)) != null))
 								tam++;
 							else if (!(genericValue.get(
 									"clasificacion" + String.valueOf(j))
 									.toString().isEmpty()))
 								tam++;
 
 						} catch (NullPointerException e) {
 							continue;
 						}
 					}
 
 				}
 			}
 
 			int tam2 = 0;
 
 			// Se valida que se llenen todas las clasificaciones
 			if (tam != 0) {
 				for (int j = 1; j <= tam; j++) {
 					Debug.log("Clasificacion"
 							+ context.get("clasificacion" + String.valueOf(j)));
 					if (context.get("clasificacion" + String.valueOf(j)) != null)
 						tam2++;
 				}
 
 			}
 
 			if (tam == tam2)
 				aviso = "ok";
 			else
 				aviso = "Nok";
 
 		} catch (Exception e) {
 
 			Debug.log("Error en clasificaciones obligatorias " + e, MODULE);
 		}
 		return aviso;
 	}
 	
 	/**
 	 * @return lista de columnas de la entidad EstructuraClave
 	 */
 	public static List<String> getListColumnas() {
 		List<String> lista = new ArrayList<String>();
 		lista.add("idSecuencia");
 		for (int j = 1; j < 16; j++) {
 			lista.add("clasificacion"+ String.valueOf(j));
 		}
 		return lista;
 	}
 }
