 package com.kit.lightserver.services.db.forms.conhecimentos;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import com.kit.lightserver.domain.types.ConhecimentoSTY;
 import com.kit.lightserver.services.db.QueryIntegerParameter;
 import com.kit.lightserver.services.db.QueryParameter;
 import com.kit.lightserver.services.db.QueryStringParameter;
 import com.kit.lightserver.services.db.UpdateQueryInterface;
 import com.kit.lightserver.services.db.common.QueryUtil;
 
 public final class UpdateConhecimentosFlagsQuery implements UpdateQueryInterface {
 
     private final String flagName;
 
     private final String rowIdsOrClause;
 
     private final List<QueryParameter> queryParameters = new LinkedList<QueryParameter>();
 
     public UpdateConhecimentosFlagsQuery(final String flagName, final String ktClientId, final List<ConhecimentoSTY> conhecimentosList) {
 
         if( conhecimentosList == null || conhecimentosList.size() == 0 ) {
             throw new RuntimeException("conhecimentosList can not be empty.");
         }
 
         this.flagName = flagName;
 
         this.rowIdsOrClause = QueryUtil.buildLongOrClause("KTRowId", conhecimentosList.size());
 
         final QueryStringParameter ktClientIdParam = new QueryStringParameter(ktClientId);
         queryParameters.add(ktClientIdParam);
 
         for (final ConhecimentoSTY conhecimentoSTY : conhecimentosList) {
             final int currentKtRowId = conhecimentoSTY.getKtRowId();
             final QueryIntegerParameter ktRowIdParam = new QueryIntegerParameter(currentKtRowId);
             queryParameters.add(ktRowIdParam);
         }
 
 
     }// constructor
 
     @Override
     public String getPreparedUpdateQueryString() {
 
         final String tableName = TableConhecimentosConstants.TABLE_NAME_CONHECIMENTOS;
         final String flagColumn = "KTFlag" + flagName;
         final String flagColumnDbUpdateTime = "KTFlag" + flagName + "UpdateDBTime";
 
         final String queryStr =
                "UPDATE " + tableName + " SET "+ flagColumn + "=1, "+ flagColumnDbUpdateTime + "=GETDATE(), KTFormVersion=KTFormVersion+1 WHERE KTClientId=? AND " + rowIdsOrClause;
 
         return queryStr;
     }
 
     @Override
     public List<QueryParameter> getUpdateQueryParameters() {
         return queryParameters;
     }
 
 }// class
