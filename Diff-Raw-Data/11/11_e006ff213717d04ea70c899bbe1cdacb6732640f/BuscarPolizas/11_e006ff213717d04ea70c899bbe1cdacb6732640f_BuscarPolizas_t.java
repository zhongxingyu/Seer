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
 
 package com.opensourcestrategies.financials.transactions;
 
 import java.math.BigDecimal;
 import java.text.ParseException;
 import java.util.Date;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.TimeZone;
 
 import javolution.util.FastList;
 import javolution.util.FastMap;
 
 import org.ofbiz.base.util.Debug;
 import org.ofbiz.base.util.GeneralException;
 import org.ofbiz.base.util.UtilDateTime;
 import org.ofbiz.base.util.UtilMisc;
 import org.ofbiz.base.util.UtilValidate;
 import org.ofbiz.entity.Delegator;
 import org.ofbiz.entity.GenericValue;
 import org.ofbiz.entity.condition.EntityCondition;
 import org.ofbiz.entity.condition.EntityOperator;
 import org.ofbiz.entity.util.EntityFindOptions;
 import org.ofbiz.entity.util.EntityUtil;
 import org.ofbiz.party.party.PartyHelper;
 import org.opentaps.base.entities.AcctgTransAndOrg;
 import org.opentaps.base.entities.AcctgTransType;
 import org.opentaps.base.entities.GlFiscalType;
 import org.opentaps.base.entities.AcctgPolizas;
 import org.opentaps.base.entities.AcctgPolizasDetalleListado;
 import org.opentaps.base.entities.PartyContactWithPurpose;
 import org.opentaps.base.entities.TipoDocumento;
 import org.opentaps.base.entities.TipoPoliza;
 import org.opentaps.common.builder.EntityListBuilder;
 import org.opentaps.common.builder.PageBuilder;
 import org.opentaps.common.util.UtilCommon;
 import org.opentaps.domain.DomainsDirectory;
 import org.opentaps.domain.ledger.LedgerRepositoryInterface;
 import org.opentaps.domain.organization.Organization;
 import org.opentaps.domain.organization.OrganizationRepositoryInterface;
 import org.opentaps.foundation.action.ActionContext;
 
 
 /**
 * BuscarPolizas - Informaci�n de P�lizas Contables
  */
 public class BuscarPolizas {
 
     private static final String MODULE = TransactionActions.class.getName();
 
     /**
      * Action for the findPolizas / listPolizas transactions screen.
      * @param context the screen context
      * @throws GeneralException if an error occurs
      * @throws ParseException if an error occurs
      */
     public static void enlistarPolizasContables(Map<String, Object> context) throws GeneralException, ParseException {
 
     	final ActionContext ac = new ActionContext(context);
         final Locale locale = ac.getLocale();
         final TimeZone timeZone = ac.getTimeZone();
         String organizationPartyId = UtilCommon.getOrganizationPartyId(ac.getRequest());
         String dateFormat = UtilDateTime.getDateFormat(locale);
 
          //String acctgTransId = ac.getParameter("findAcctgTransId");
         //String acctgTransTypeId = ac.getParameter("acctgTransTypeId");
          String postedDate = ac.getParameter("postedDate");
          String agrupador = ac.getParameter("agrupador");          
          String tipoPoliza = ac.getParameter("tipoPoliza");
 
          DomainsDirectory dd = DomainsDirectory.getDomainsDirectory(ac);
          final LedgerRepositoryInterface ledgerRepository = dd.getLedgerDomain().getLedgerRepository();
 
          // TODO: Put a currencyUomId on AcctgTrans and modify postAcctgTrans to set that in addition to postedAmount,
          // instead of using the organization's base currency
          OrganizationRepositoryInterface organizationRepository = dd.getOrganizationDomain().getOrganizationRepository();
          Organization organization = organizationRepository.getOrganizationById(organizationPartyId);
          if (organization != null) {
              ac.put("orgCurrencyUomId", organization.getPartyAcctgPreference().getBaseCurrencyUomId());
          }
 
          // get the list of transactionTypes for the parametrized form ftl
          List<AcctgTransType> transactionTypes = ledgerRepository.findAll(AcctgTransType.class);
          List<Map<String, Object>> transactionTypesList = new FastList<Map<String, Object>>();
          for (AcctgTransType s : transactionTypes) {
              Map<String, Object> map = s.toMap();
              transactionTypesList.add(map);
          }
          ac.put("transactionTypes", transactionTypesList);
          
       // get the list of transactionTypes for the parametrized form ftl         
          List<TipoPoliza> listaTipoPoliza = ledgerRepository.findAll(TipoPoliza.class);         
          List<Map<String, Object>> listaTipoPolizaList = new FastList<Map<String, Object>>();
          for (TipoPoliza s : listaTipoPoliza) {
              Map<String, Object> map = s.toMap();
              listaTipoPolizaList.add(map);
          }
          ac.put("listaTipoPoliza", listaTipoPolizaList);
          
          if ("Y".equals(ac.getParameter("performFind"))) {
         	
              List<EntityCondition> searchConditions = new FastList<EntityCondition>();             
              /*if (UtilValidate.isNotEmpty(acctgTransId)) {
                 searchConditions.add(EntityCondition.makeCondition(AcctgPolizas.Fields.acctgTransId.name(), EntityOperator.EQUALS, acctgTransId));
              }*/
              if (UtilValidate.isNotEmpty(agrupador)) {
                  searchConditions.add(EntityCondition.makeCondition(AcctgPolizas.Fields.agrupador.name(), EntityOperator.EQUALS, agrupador));
              }
              if (UtilValidate.isNotEmpty(postedDate)) {
                  searchConditions.add(EntityCondition.makeCondition(AcctgPolizas.Fields.postedDate.name(), EntityOperator.GREATER_THAN_EQUAL_TO, UtilDateTime.getDayStart(UtilDateTime.stringToTimeStamp(postedDate, dateFormat, timeZone, locale), timeZone, locale)));
              }
             /*if (UtilValidate.isNotEmpty(acctgTransTypeId)) {
             	 searchConditions.add(EntityCondition.makeCondition(AcctgPolizas.Fields.acctgTransTypeId.name(), EntityOperator.EQUALS, acctgTransTypeId));
             }*/
              if (UtilValidate.isNotEmpty(organizationPartyId)) {
             	 searchConditions.add(EntityCondition.makeCondition(AcctgPolizas.Fields.organizationPartyId.name(), EntityOperator.EQUALS, organizationPartyId));
              }
              if (UtilValidate.isNotEmpty("D")) {
             	 searchConditions.add(EntityCondition.makeCondition(AcctgPolizas.Fields.debitCreditFlag.name(), EntityOperator.EQUALS, "D"));
              }
              if (UtilValidate.isNotEmpty(tipoPoliza)) {
             	 searchConditions.add(EntityCondition.makeCondition(AcctgPolizas.Fields.tipoPoliza.name(), EntityOperator.EQUALS, tipoPoliza));
              }
              
             
              // fields to select
              List<String> fieldsToSelect = UtilMisc.toList("agrupador", "description", "postedDate", "amount");
              List<String> orderBy = UtilMisc.toList("amount");
 
              Debug.logInfo("search conditions : " + EntityCondition.makeCondition(searchConditions, EntityOperator.AND).toString(), MODULE);
              EntityListBuilder acctgTransListBuilder = new EntityListBuilder(ledgerRepository, AcctgPolizas.class, EntityCondition.makeCondition(searchConditions, EntityOperator.AND), fieldsToSelect, orderBy);             
              PageBuilder<AcctgPolizas> pageBuilder = new PageBuilder<AcctgPolizas>() {
                  public List<Map<String, Object>> build(List<AcctgPolizas> page) throws Exception {
                      Delegator delegator = ac.getDelegator();
                      List<Map<String, Object>> newPage = FastList.newInstance();
                      for (AcctgPolizas acctgTrans : page) {
                          Map<String, Object> newRow = FastMap.newInstance();
                          newRow.putAll(acctgTrans.toMap());
 //                         if (UtilValidate.isNotEmpty(acctgTrans.getPartyId())) {
 //                             newRow.put("partyNameAndId", PartyHelper.getPartyName(delegator, acctgTrans.getPartyId(), false) + " (" + acctgTrans.getPartyId() + ")");
 //                         }
 //                         AcctgTransType acctgTransType = ledgerRepository.findOneCache(AcctgTransType.class, ledgerRepository.map(AcctgTransType.Fields.acctgTransTypeId, acctgTrans.getAcctgTransTypeId()));
 //                         newRow.put("acctgTransTypeDescription", acctgTransType.get(AcctgTransType.Fields.description.name(), locale));
                          newPage.add(newRow);
                      }
                      return newPage;
                  }
              };
              acctgTransListBuilder.setPageBuilder(pageBuilder);
              ac.put("acctgTransListBuilder", acctgTransListBuilder);
          }
     }
     
     /**
      * Action for the findPolizas / listPolizas transactions screen.
      * @param context the screen context
      * @throws GeneralException if an error occurs
      * @throws ParseException if an error occurs
      */
     public static void enlistarPolizasContablesDetalle(Map<String, Object> context) throws GeneralException, ParseException {
 
     	Debug.logInfo("Omar - enlistarPolizasContablesDetalle", MODULE);
     	final ActionContext ac = new ActionContext(context);
         final Locale locale = ac.getLocale();
         final TimeZone timeZone = ac.getTimeZone();        
 
          //String acctgTransId = ac.getParameter("findAcctgTransId");        
         
         String agrupador = ac.getParameter("agrupador"); 
         String Secuencia = ac.getParameter("secuencia");
          String AccountName = ac.getParameter("accountName");                  
          String DebitCreditFlag = ac.getParameter("debitCreditFlag");
          String amount = ac.getParameter("amount");
          
          Debug.logInfo("Omar - Secuencia: " + Secuencia, MODULE);
          Debug.logInfo("Omar - AccountName: " + AccountName, MODULE);
          Debug.logInfo("Omar - agrupador: " + agrupador, MODULE);
          Debug.logInfo("Omar - DebitCreditFlag: " + DebitCreditFlag, MODULE);
          Debug.logInfo("Omar - amount: " + amount, MODULE);
 
          DomainsDirectory dd = DomainsDirectory.getDomainsDirectory(ac);
          final LedgerRepositoryInterface ledgerRepository = dd.getLedgerDomain().getLedgerRepository();
 
          
          
          Debug.logInfo("Omar - Genera condiciones", MODULE);
              List<EntityCondition> searchConditions = new FastList<EntityCondition>();             
              /*if (UtilValidate.isNotEmpty(acctgTransId)) {
                 searchConditions.add(EntityCondition.makeCondition(AcctgPolizasDetalleListado.Fields.acctgTransId.name(), EntityOperator.EQUALS, acctgTransId));
              }*/
              if (UtilValidate.isNotEmpty(agrupador)) {
                  searchConditions.add(EntityCondition.makeCondition(AcctgPolizasDetalleListado.Fields.agrupador.name(), EntityOperator.EQUALS, agrupador));
              }                          
              if (UtilValidate.isNotEmpty(AccountName)) {
             	 searchConditions.add(EntityCondition.makeCondition(AcctgPolizasDetalleListado.Fields.accountName.name(), EntityOperator.EQUALS, AccountName));
              }             
              if (UtilValidate.isNotEmpty(DebitCreditFlag)) {
             	 searchConditions.add(EntityCondition.makeCondition(AcctgPolizasDetalleListado.Fields.debitCreditFlag.name(), EntityOperator.EQUALS, DebitCreditFlag));
              }
              if (UtilValidate.isNotEmpty(amount)) {
             	 searchConditions.add(EntityCondition.makeCondition(AcctgPolizasDetalleListado.Fields.amount.name(), EntityOperator.EQUALS, amount));
              }
              
             
              // fields to select
              List<String> fieldsToSelect = UtilMisc.toList("accountName", "debitCreditFlag", "amount");
              List<String> orderBy = UtilMisc.toList("amount");
              Debug.logInfo("Omar - Va a ingresar la lista para agrupar", MODULE);
 
              Debug.logInfo("search conditions : " + EntityCondition.makeCondition(searchConditions, EntityOperator.AND).toString(), MODULE);
              EntityListBuilder acctgTransListBuilderDetalle = new EntityListBuilder(ledgerRepository, AcctgPolizasDetalleListado.class, EntityCondition.makeCondition(searchConditions, EntityOperator.AND), fieldsToSelect, orderBy);             
              PageBuilder<AcctgPolizasDetalleListado> pageBuilder = new PageBuilder<AcctgPolizasDetalleListado>() {
                  public List<Map<String, Object>> build(List<AcctgPolizasDetalleListado> page) throws Exception {
                      Delegator delegator = ac.getDelegator();
                      List<Map<String, Object>> newPage = FastList.newInstance();
                      for (AcctgPolizasDetalleListado acctgTrans : page) {
                          Map<String, Object> newRow = FastMap.newInstance();
                          newRow.putAll(acctgTrans.toMap());
 //                         if (UtilValidate.isNotEmpty(acctgTrans.getPartyId())) {
 //                             newRow.put("partyNameAndId", PartyHelper.getPartyName(delegator, acctgTrans.getPartyId(), false) + " (" + acctgTrans.getPartyId() + ")");
 //                         }
 //                         AcctgTransType acctgTransType = ledgerRepository.findOneCache(AcctgTransType.class, ledgerRepository.map(AcctgTransType.Fields.acctgTransTypeId, acctgTrans.getAcctgTransTypeId()));
 //                         newRow.put("acctgTransTypeDescription", acctgTransType.get(AcctgTransType.Fields.description.name(), locale));
                          newPage.add(newRow);
                      }
                      return newPage;
                  }
              };
              Debug.logInfo("Omar - Sale de lista para agrupar", MODULE);
              acctgTransListBuilderDetalle.setPageBuilder(pageBuilder);
              ac.put("acctgTransListBuilder", acctgTransListBuilderDetalle);
          
     }
 }
