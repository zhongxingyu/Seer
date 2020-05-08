 package org.opentaps.dataimport.domain;
 
 import java.sql.Timestamp;
 import java.text.ParseException;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.TimeZone;
 
 import javolution.util.FastList;
 import javolution.util.FastMap;
 
 import org.ofbiz.base.util.Debug;
 import org.ofbiz.base.util.UtilDateTime;
 import org.ofbiz.base.util.UtilMisc;
 import org.ofbiz.entity.Delegator;
 import org.ofbiz.entity.GenericEntityException;
 import org.ofbiz.entity.GenericValue;
 import org.ofbiz.service.DispatchContext;
 import org.ofbiz.service.GenericServiceException;
 import org.ofbiz.service.LocalDispatcher;
 import org.ofbiz.service.ServiceUtil;
 import org.opentaps.common.util.UtilMessage;
 import org.opentaps.dataimport.UtilOperacionDiariaServices;
 import org.opentaps.foundation.action.ActionContext;
 import org.opentaps.foundation.service.ServiceException;
 
 public class OperacionDiariaEgresosManual {
 	
 	private static final String MODULE = OperacionDiariaEgresosManual.class.getName();
 	
 	/**
 	 * Metodo que crea la operacion diaria de Egresos 
 	 * @param dctx
 	 * @param context
 	 * @return
 	 */
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	public static Map createOperacionDiariaEgresos(DispatchContext dctx, Map context){
         LocalDispatcher dispatcher = dctx.getDispatcher();
         Delegator delegator = dctx.getDelegator();
         GenericValue userLogin = (GenericValue) context.get("userLogin");
         String organizationPartyId = (String) context.get("organizationPartyId");
         
         final ActionContext ac = new ActionContext(context);
         final Locale locale = ac.getLocale();
         final TimeZone timeZone = ac.getTimeZone();
         String dateFormat = UtilDateTime.getDateFormat(locale);
         
         Timestamp fecContable = null;
         Timestamp fecTrans = null; 
         
         GenericValue acctgtrans = null;
         GenericValue acctgtransPres = null;
         
         String acctgTransId = null;
         
         //Aqui se guardan las transacciones registradas
         List<String> listTransId = FastList.newInstance();
 		
         Debug.logWarning("ENTRO AL SERVICIO PARA CREAR OPERACION DIARIA EGRESOS", MODULE);
         
         try{
         	
 	        String userLog = userLogin.getString("userLoginId");
 	        String tipoDoc = (String) context.get("Tipo_Documento");
	        fecTrans = (Timestamp) context.get("Fecha_Transaccion");
			fecContable = (Timestamp) context.get("Fecha_Contable");
 	        String refDoc = (String) context.get("Referencia_Documento");
 	        String sec = (String) context.get("Secuencia");
 	        String cvePrespues = (String) context.get("Cve_Presupuestal");
 	        String idProdAbono = (String) context.get("Id_Producto_Abono");
 	        String idProdCargo = (String) context.get("Id_Producto_Cargo");
 	        String entFed = (String) context.get("EntidadFederativa");
 	        String region = (String) context.get("Region");
 	        String muni = (String) context.get("Municipio");
 	        String local = (String) context.get("Localidad");
 	        String suFuenteEsp = (String) context.get("Sub_Fuente_Especifica");
 	        String uniEjec = (String) context.get("Unidad_Ejecutora");
 	        String subFun = (String) context.get("Subfuncion");
 	        String tipoGasto = (String) context.get("Tipo_Gasto");
 	        String partEspec = (String) context.get("Partida_Especifica");
 	        String actividad = (String) context.get("Actividad");
 	        String area = (String) context.get("Area");	        
 	        String idPago = (String) context.get("Id_RecaudadoH");
 	        java.math.BigDecimal monto = java.math.BigDecimal.valueOf(Long.valueOf((String)context.get("Monto")));
 	        
 	        Debug.logWarning("userLog "+userLog, MODULE);
 	        Debug.logWarning("tipoDoc "+tipoDoc, MODULE);
 	        Debug.logWarning("fecTrans "+fecTrans, MODULE);
 	        Debug.logWarning("fecContable "+fecContable, MODULE);
 	        Debug.logWarning("refDoc "+refDoc, MODULE);
 	        Debug.logWarning("sec "+sec, MODULE);
 	        Debug.logWarning("cvePrespues "+cvePrespues, MODULE);
 	        Debug.logWarning("idProdAbono "+idProdAbono, MODULE);
 	        Debug.logWarning("idProdCargo "+idProdCargo, MODULE);
 	        Debug.logWarning("entFed "+entFed, MODULE);
 	        Debug.logWarning("region "+region, MODULE);
 	        Debug.logWarning("muni "+muni, MODULE);
 	        Debug.logWarning("local "+local, MODULE);
 	        Debug.logWarning("suFuente "+suFuenteEsp, MODULE);
 	        Debug.logWarning("uniEjec "+uniEjec, MODULE);
 	        Debug.logWarning("subFun "+subFun, MODULE);
 	        Debug.logWarning("tipoGasto "+tipoGasto, MODULE);
 	        Debug.logWarning("partEspec "+partEspec, MODULE);
 	        Debug.logWarning("actividad "+actividad, MODULE);
 	        Debug.logWarning("area "+area, MODULE);
 	        Debug.logWarning("idPago "+idPago, MODULE);
 	        Debug.logWarning("monto "+monto, MODULE);	        
 	        
 	        //Buscamos el tipo documento seleccionado en pantalla para obtener el acctgTransTypeId
 			GenericValue tipoDocumento = delegator.findByPrimaryKeyCache("TipoDocumento",UtilMisc.<String, Object>toMap("idTipoDoc", tipoDoc));
 			String acctgTransTypeId = tipoDocumento.getString("acctgTransTypeId");
 			Debug.logWarning("tipoDocumento  Encontrado "+tipoDocumento, MODULE);
 			String docu = tipoDocumento.getString("descripcion");
 			String descripcion = (docu == null?" ":docu)+" - "+(refDoc == null ?" ":refDoc);
 			
 	        String ciclo = "";
 	        if(fecContable != null)
 		        ciclo = String.valueOf(UtilDateTime.getYear(fecContable, timeZone, locale)).substring(2);
 
 	        //Obtener los tipos fiscales que se encuentran en la miniguia
 	        List<String> tiposFiscales = UtilOperacionDiariaServices.obtenTiposFiscalDoc(dctx, dispatcher, tipoDoc);
 	        
 	        for (String tipoFis : tiposFiscales) {	        
 	        
 		        //Obtiene el mapa que se utiliza para guardar las cuentas correspondientes y para validaciones
 		        Map<String,String> mapCuentas = UtilOperacionDiariaServices.regresaMapa(dctx, dispatcher, context, 
 		        										monto, fecContable, acctgTransTypeId, partEspec, 
 		        										tipoFis, idProdAbono, idProdCargo, idPago, "COG");
 		        
 		        String tipoAsiento = UtilOperacionDiariaServices.obtenTipoAsiento(mapCuentas);
 		        
 		        //Generamos la transaccion
 		        acctgTransId = (refDoc == null ? "" :refDoc)+"-"+(sec == null ? "" :sec)+"-"+(tipoAsiento == null ? "" :tipoAsiento);
 		        
 		        listTransId.add(acctgTransId);
 		        
 		        acctgtrans = GenericValue.create(delegator.getModelEntity("AcctgTrans"));
 	//	        acctgtrans.setNextSeqId();
 		        acctgtrans.set("acctgTransId", acctgTransId);
 		        acctgtrans.set("acctgTransTypeId", acctgTransTypeId);
 		        acctgtrans.set("description", descripcion);
 		        acctgtrans.set("transactionDate", fecTrans);
 		        acctgtrans.set("isPosted", "Y");
 		        acctgtrans.set("postedDate", fecContable);
 		        acctgtrans.set("glFiscalTypeId", tipoFis);
 		        acctgtrans.set("workEffortId", actividad);
 		        acctgtrans.set("partyId", uniEjec);
 		        acctgtrans.set("createdByUserLogin", userLog);
 		        acctgtrans.set("postedAmount", monto);
 		        acctgtrans.create();
 		        
 	//	        acctgTransId = acctgtrans.getString("acctgTransId");
 		        //Se registra en AcctTransPresupuestal
 		        acctgtransPres = GenericValue.create(delegator.getModelEntity("AcctgTransPresupuestal"));
 		        acctgtransPres.set("acctgTransId", acctgTransId);
 		        acctgtransPres.set("ciclo", ciclo);
 		        acctgtransPres.set("unidadOrganizacional", organizationPartyId);
 		        acctgtransPres.set("unidadEjecutora", uniEjec);
 		        String unidadOr = UtilOperacionDiariaServices.obtenPadrePartyId(dctx, dispatcher, uniEjec);
 		        acctgtransPres.set("unidadOrganizacional", unidadOr);
 		        String unidadRes = UtilOperacionDiariaServices.obtenPadrePartyId(dctx, dispatcher, unidadOr);
 		        acctgtransPres.set("unidadResponsable", unidadRes);
 		        acctgtransPres.set("clavePres", cvePrespues);
 		        acctgtransPres.set("subFuenteEspecifica", suFuenteEsp);
 		        String subfuente = UtilOperacionDiariaServices.obtenPadreEnumeration(dctx, dispatcher, suFuenteEsp);
 		        acctgtransPres.set("subFuente",subfuente);
 		        String fuente = UtilOperacionDiariaServices.obtenPadreEnumeration(dctx, dispatcher, subfuente);
 		        acctgtransPres.set("fuente",fuente);	        
 		        acctgtransPres.set("entidadFederativa", entFed);
 		        acctgtransPres.set("region", region);
 		        acctgtransPres.set("municipio", muni);
 		        acctgtransPres.set("localidad", local);
 		        acctgtransPres.set("subFuncion", subFun);
 		        String funcion = UtilOperacionDiariaServices.obtenPadreEnumeration(dctx, dispatcher, subFun);
 		        acctgtransPres.set("funcion", funcion);
 		        String finalidad = UtilOperacionDiariaServices.obtenPadreEnumeration(dctx, dispatcher, funcion);
 		        acctgtransPres.set("finalidad", finalidad);
 		        acctgtransPres.set("tipoGasto", tipoGasto);
 		        acctgtransPres.set("partidaEspecifica", partEspec);
 		        String partGene = UtilOperacionDiariaServices.obtenPadreProductCate(dctx, dispatcher, partEspec);
 		        acctgtransPres.set("partidaGenerica", partGene);
 		        String concepto = UtilOperacionDiariaServices.obtenPadreProductCate(dctx, dispatcher, partGene);
 		        acctgtransPres.set("concepto", concepto);
 		        String capitulo = UtilOperacionDiariaServices.obtenPadreProductCate(dctx, dispatcher, concepto);
 		        acctgtransPres.set("capitulo", capitulo);
 		        acctgtransPres.set("actividad", actividad);
 		        Debug.logWarning("actividad  enviado"+actividad, MODULE);
 		        String subProg = UtilOperacionDiariaServices.obtenPadreWorkEffort(dctx, dispatcher, actividad);
 		        acctgtransPres.set("subProgramaPresupuestario", subProg);
 		        Debug.logWarning("subProg  enviado"+subProg, MODULE);
 		        String progPresu = UtilOperacionDiariaServices.obtenPadreWorkEffort(dctx, dispatcher, subProg);
 		        acctgtransPres.set("programaPresupuestario", progPresu);	      
 		        String progPlan = UtilOperacionDiariaServices.obtenPadreWorkEffort(dctx, dispatcher, progPresu);
 		        Debug.logWarning("progPresu  enviado"+progPresu, MODULE);
 		        acctgtransPres.set("programaPlan", progPlan);
 		        Debug.logWarning("progPlan  enviado"+progPlan, MODULE);
 		        acctgtransPres.set("area", area);
 		        String subSector = UtilOperacionDiariaServices.obtenPadreEnumeration(dctx, dispatcher, area);
 		        acctgtransPres.set("subSector", subSector); 
 		        String sector = UtilOperacionDiariaServices.obtenPadreEnumeration(dctx, dispatcher, subSector);
 		        acctgtransPres.set("sector", sector);	        
 		        acctgtransPres.set("idTipoDoc", tipoDoc);
 		        acctgtransPres.set("secuencia", sec);
 		        acctgtransPres.set("idProductoD", idProdCargo);
 		        acctgtransPres.set("idProductoH", idProdAbono);
 		        acctgtransPres.set("idPago",idPago);
 		        acctgtransPres.set("agrupador", refDoc);
 		        acctgtransPres.create();
 		        
 		        Map<String,String> mapaAcctgEnums = FastMap.newInstance();
 		        mapaAcctgEnums.put("acctgTagEnumId1",subFun);
 		        mapaAcctgEnums.put("acctgTagEnumId2",tipoGasto);
 		        mapaAcctgEnums.put("acctgTagEnumId3",suFuenteEsp);
 		        mapaAcctgEnums.put("acctgTagEnumId4",area);
 		        
 		        //Se realiza el registro de trans entries
 		        UtilOperacionDiariaServices.registraEntries(dctx, dispatcher, context,
 						organizationPartyId, acctgTransId, monto,
 						fecContable, acctgTransTypeId, mapaAcctgEnums, mapCuentas);
 			
 	        }
         
         } catch (GenericEntityException e) {
         	return UtilMessage.createAndLogServiceError(e, MODULE);
 		} catch (GenericServiceException e) {
 			return UtilMessage.createAndLogServiceError(e, MODULE);
 		} catch (ServiceException e) {
 			return UtilMessage.createAndLogServiceError(e, MODULE);
 		}
 		
         Map results = ServiceUtil.returnSuccess();
         results.put("listTransId", listTransId);
         return results;
 	}
 
 
 }
