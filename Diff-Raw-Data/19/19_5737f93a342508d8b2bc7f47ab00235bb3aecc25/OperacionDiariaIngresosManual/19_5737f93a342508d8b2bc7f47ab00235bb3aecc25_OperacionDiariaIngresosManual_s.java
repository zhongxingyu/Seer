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
 
 
 public class OperacionDiariaIngresosManual {
 	
 	private static final String MODULE = OperacionDiariaIngresosManual.class.getName();
 	
     /**
      * Metodo que se utiliza para registrar una operacion diaria de ingresos
      * @param dctx
      * @param context
      * @return
      */
     @SuppressWarnings({ "unchecked", "rawtypes" })
 	public static Map createOperacionDiariaIngresos(DispatchContext dctx, Map context) {
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
         
         Debug.logWarning("ENTRO AL SERVICIO PARA CREAR OPERACION DIARIA INGRESOS", MODULE);
         
         try {
         	
 	        String userLog = userLogin.getString("userLoginId");
 	        String tipoDoc = (String) context.get("Tipo_Documento");
	        fecTrans = (Timestamp) UtilDateTime.stringToTimeStamp((String)context.get("Fecha_Transaccion"), dateFormat, timeZone, locale);
			fecContable = (Timestamp) UtilDateTime.stringToTimeStamp((String)context.get("Fecha_Contable"), dateFormat, timeZone, locale);
 	        String refDoc = (String) context.get("Referencia_Documento");
 	        String sec = (String) context.get("Secuencia");
 	        String cvePrespues = (String) context.get("Cve_Presupuestal");
 	        String idProdAbono = (String) context.get("Id_Producto_Abono");
 	        String idProdCargo = (String) context.get("Id_Producto_Cargo");
 	        String n5 = (String) context.get("N5");
 	        String entFed = (String) context.get("EntidadFederativa");
 	        String region = (String) context.get("Region");
 	        String muni = (String) context.get("Municipio");
 	        String local = (String) context.get("Localidad");
 	        String suFuenteEsp = (String) context.get("Sub_Fuente_Especifica");
 	        String uniEjec = (String) context.get("Unidad_Ejecutora");
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
 	        Debug.logWarning("n5 "+n5, MODULE);
 	        Debug.logWarning("entFed "+entFed, MODULE);
 	        Debug.logWarning("region "+region, MODULE);
 	        Debug.logWarning("muni "+muni, MODULE);
 	        Debug.logWarning("local "+local, MODULE);
 	        Debug.logWarning("suFuente "+suFuenteEsp, MODULE);
 	        Debug.logWarning("uniEjec "+uniEjec, MODULE);
 	        Debug.logWarning("idPago "+idPago, MODULE);
 	        Debug.logWarning("monto "+monto, MODULE);
 	        
 	        //Buscamos el tipo documento seleccionado en pantalla para obtener el acctgTransTypeId
 			GenericValue tipoDocumento = delegator.findByPrimaryKeyCache("TipoDocumento",UtilMisc.<String, Object>toMap("idTipoDoc", tipoDoc));
 			String acctgTransTypeId = tipoDocumento.getString("acctgTransTypeId");
 			Debug.logWarning("tipoDocumento  Encontrado "+tipoDocumento, MODULE);
 			String docu = tipoDocumento.getString("descripcion");
 			String descripcion = docu == null?" ":docu+" - "+refDoc == null ?" ":refDoc;
 			
 	        String ciclo = "";
 	        if(fecContable != null)
 		        ciclo = String.valueOf(UtilDateTime.getYear(fecContable, timeZone, locale)).substring(2);
 	        
 	        //Obtener los tipos fiscales que se encuentran en la miniguia
 	        List<String> tiposFiscales = UtilOperacionDiariaServices.obtenTiposFiscalDoc(dctx, dispatcher, tipoDoc);
 	        
 	        for (String tipoFis : tiposFiscales) {
 		        
 		        //Obtiene el mapa que se utiliza para guardar las cuentas correspondientes y para validaciones
 		        Map<String,String> mapCuentas = UtilOperacionDiariaServices.regresaMapa(dctx, dispatcher, context, 
 		        										monto, fecContable, acctgTransTypeId, n5, 
 		        										tipoFis, idProdAbono, idProdCargo, idPago, "CRI");
 		        
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
 		        acctgtransPres.set("nivel5", n5);
 		        Debug.logWarning("nivel5  enviado"+n5, MODULE);
 		        String concep = UtilOperacionDiariaServices.obtenPadreProductCate(dctx, dispatcher, n5);
 		        Debug.logWarning("concep  enviado"+concep, MODULE);
 		        acctgtransPres.set("conceptoRub", concep);
 		        String clas = UtilOperacionDiariaServices.obtenPadreProductCate(dctx, dispatcher, concep);
 		        Debug.logWarning("clas  enviado"+clas, MODULE);
 		        acctgtransPres.set("clase", clas);
 		        String tip = UtilOperacionDiariaServices.obtenPadreProductCate(dctx, dispatcher, clas);
 		        Debug.logWarning("tip  enviado"+tip, MODULE);
 		        acctgtransPres.set("tipo", tip);
 		        String rubr = UtilOperacionDiariaServices.obtenPadreProductCate(dctx, dispatcher, tip);
 		        Debug.logWarning("rubr  enviado"+rubr, MODULE);
 		        acctgtransPres.set("rubro", rubr);
 		        acctgtransPres.set("subFuenteEspecifica", suFuenteEsp);
 		        String subfuente = UtilOperacionDiariaServices.obtenPadreEnumeration(dctx, dispatcher, suFuenteEsp);
 		        Debug.logWarning("subfuente  enviado"+subfuente, MODULE);
 		        acctgtransPres.set("subFuente",subfuente);
 		        String fuente = UtilOperacionDiariaServices.obtenPadreEnumeration(dctx, dispatcher, subfuente);
 		        Debug.logWarning("fuente  enviado"+fuente, MODULE);
 		        acctgtransPres.set("fuente",fuente);	        
 		        acctgtransPres.set("entidadFederativa", entFed);
 		        acctgtransPres.set("region", region);
 		        acctgtransPres.set("municipio", muni);
 		        acctgtransPres.set("localidad", local);
 		        acctgtransPres.set("idTipoDoc", tipoDoc);
 		        acctgtransPres.set("secuencia", sec);
 		        acctgtransPres.set("idProductoD", idProdCargo);
 		        acctgtransPres.set("idProductoH", idProdAbono);
 		        acctgtransPres.set("idPago",idPago);
 		        acctgtransPres.set("agrupador", refDoc);
 		        acctgtransPres.create();
 	
 		        Map<String,String> mapaAcctgEnums = FastMap.newInstance();
 		        mapaAcctgEnums.put("acctgTagEnumId3",suFuenteEsp);
 	
 		        //Se realiza el registro de trans entries
 		        UtilOperacionDiariaServices.registraEntries(dctx, dispatcher, context,
 		        						organizationPartyId, acctgTransId, monto,
 		        						fecContable, acctgTransTypeId, mapaAcctgEnums, mapCuentas);
 	        
 			}
 	        
		} catch (ParseException e) {
			return UtilMessage.createAndLogServiceError(e, MODULE);
 		} catch (GenericServiceException e) {
 			return UtilMessage.createAndLogServiceError(e, MODULE);
 		} catch (GenericEntityException e) {
 			return UtilMessage.createAndLogServiceError(e, MODULE);
 		} catch (ServiceException e) {
 			return UtilMessage.createAndLogServiceError(e, MODULE);
 		}
     	
         Map results = ServiceUtil.returnSuccess();
         results.put("listTransId", listTransId);
         return results;
     	
     }
 	
 }
 
 
