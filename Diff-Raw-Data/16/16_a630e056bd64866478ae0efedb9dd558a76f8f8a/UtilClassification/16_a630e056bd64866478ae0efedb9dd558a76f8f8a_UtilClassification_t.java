 /* 
  */
 
 package org.opentaps.common.util;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 
 import javolution.util.FastList;
 import javolution.util.FastMap;
 
 import org.ofbiz.base.util.Debug;
 import org.ofbiz.base.util.UtilDateTime;
 import org.ofbiz.base.util.UtilHttp;
 import org.ofbiz.base.util.UtilMisc;
 import org.ofbiz.base.util.UtilValidate;
 import org.ofbiz.base.util.collections.ResourceBundleMapWrapper;
 import org.ofbiz.entity.Delegator;
 import org.ofbiz.entity.GenericEntityException;
 import org.ofbiz.entity.GenericValue;
 import org.ofbiz.entity.condition.EntityCondition;
 import org.ofbiz.entity.condition.EntityOperator;
 import org.ofbiz.service.DispatchContext;
 import org.ofbiz.service.LocalDispatcher;
 import org.ofbiz.service.ServiceUtil;
 import org.opentaps.base.entities.AcctgPolizas;
 import org.opentaps.base.entities.AcctgPolizasDetalleListado;
 import org.opentaps.base.entities.AcctgTagEnumType;
 import org.opentaps.base.entities.Enumeration;
 import org.opentaps.base.entities.EnumerationType;
 
 import org.opentaps.base.entities.Party;
 import org.opentaps.base.entities.PartyGroup;
 import org.opentaps.base.entities.Geo;
 import org.opentaps.common.builder.EntityListBuilder;
 import org.opentaps.common.builder.PageBuilder;
 import org.opentaps.common.domain.organization.OrganizationRepository;
 import org.opentaps.domain.organization.AccountingTagConfigurationForOrganizationAndUsage;
 import org.opentaps.foundation.entity.Entity;
 import org.opentaps.foundation.entity.EntityInterface;
 import org.opentaps.foundation.entity.hibernate.Session;
 import org.opentaps.foundation.repository.RepositoryException;
 
 /**
  * UtilAccountingTags - Utilities for the accounting tag system.
  */
 public final class UtilClassification {
 
     @SuppressWarnings("unused")
     private static final String MODULE = UtilClassification.class.getName();
 
     /** Number of tags defined in <code>AcctgTagEnumType</code>. */
     public static final int TAG_COUNT = 10;
     
     
     
 
     private UtilClassification() { }
 
     public static String buscaHojaNivelPresupuestal(String tipo, Session session)
 			throws RepositoryException {
 		return session
 				.createQuery(
 						"select NIVEL_ID from NIVEL_PRESUPUESTAL"
 								+ "where NIVEL_ID not in (select NIVEL_PADRE_ID from NIVEL_PRESUPUESTAL"
 								+ "where CLASIFICACION_ID = "
 								+ tipo
 								+ " and NIVEL_PADRE_ID is not null) and CLASIFICACION_ID = "
 								+ tipo).list().get(0).toString();
 	}
     
     public static List<GenericValue> getListaNiveles(String tabla, String niveles, Delegator delegator)
 	{	String entidad = tabla;
 		String valorBusqueda = "";
 		String valorId = "";
 		String valorDescripcion = "";
 		List<GenericValue> listGenericaNivelesResult = null;						
		
		Debug.log("Omar - Nivel: " + niveles);
		Debug.log("Omar - Tabla: " + tabla);
		
 		if(niveles.equals("2013") || niveles.equals("2014") || niveles.equals("2015") || niveles.equals("2016") || niveles.equals("2017") || niveles.equals("2018") || niveles.equals("2019") || niveles.equals("2020") || niveles.equals("CICLO"))
 		{	valorBusqueda = "clasificacionId";
 			valorId = "nivelId";			
 			entidad = "NivelesCiclo";
 			try 
 			{	EntityCondition condicionCiclo = EntityCondition.makeCondition(EntityOperator.AND,
					EntityCondition.makeCondition(valorBusqueda, EntityOperator.EQUALS, "CICLO"));
 			
 				listGenericaNivelesResult = delegator
 						.findByCondition(
 								entidad,
 								condicionCiclo,
								UtilMisc.toList(valorId, valorId), null);
				Debug.log("Omar - ListGenericaNivelesResult: " + listGenericaNivelesResult);
 				return listGenericaNivelesResult;				
 			}catch (GenericEntityException e) {			
 				e.printStackTrace();
 			}
 			
 		}
 		
 		if(tabla.equals("Enumeration"))
 		{	valorBusqueda = "nivelId";
 			valorId="enumId";
 			valorDescripcion="enumCode";
 		}	
 		else if(tabla.equals("Geo"))
 		{	valorBusqueda = "geoTypeId";
 			valorId="geoId";
 			valorDescripcion="geoName";
 		}
 		else if(tabla.equals("WorkEffort"))
 		{	valorBusqueda = "nivelId";
 			valorId="workEffortName";
 			valorDescripcion="description";		
 		}
 		else if(tabla.equals("Party"))
 		{	valorBusqueda = "Nivel_id";
 			valorId="externalId";
 			valorDescripcion="groupName";
 		}
 		else if(tabla.equals("ProductCategory"))
 		{	valorBusqueda = "productCategoryTypeId";
 			valorId="categoryName";
 			valorDescripcion="description";
 		}
 		EntityCondition condicion = EntityCondition.makeCondition(EntityOperator.AND,
             EntityCondition.makeCondition(valorBusqueda, EntityOperator.EQUALS, niveles));
 			
			
 			
 						
 			try 
 			{	if(tabla.equals("Party"))
 				{	condicion = EntityCondition.makeCondition(EntityOperator.AND,
 			            EntityCondition.makeCondition(valorBusqueda, EntityOperator.EQUALS, niveles),
 			            EntityCondition.makeCondition(Party.Fields.partyId, EntityOperator.EQUALS, PartyGroup.Fields.partyId));
 					entidad = "NivelesParty";
 		        }				
 				
 				listGenericaNivelesResult = delegator
 						.findByCondition(
 								entidad,
 								condicion,
 								UtilMisc.toList(valorId,
 										valorDescripcion), null);				
 				
 				if(listGenericaNivelesResult.isEmpty())
 				{	Debug.log("Omar - LA LISTA ESTA VACIA!!!");			
 				}
 				else
 				{	Debug.log("Omar - ListGenericaNivelesResult: " + listGenericaNivelesResult);					
 				}
 			} catch (GenericEntityException e) {				
 				e.printStackTrace();
 			}
 		return listGenericaNivelesResult;	
 	}
     
 }
