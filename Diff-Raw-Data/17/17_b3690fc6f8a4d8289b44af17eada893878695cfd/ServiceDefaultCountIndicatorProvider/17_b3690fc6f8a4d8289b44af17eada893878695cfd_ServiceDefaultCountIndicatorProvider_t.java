 /**
  * EasySOA Proxy
  * Copyright 2011-2013 Open Wide
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  * 
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  * 
  * Contact : easysoa-dev@googlegroups.com
  */
 
 package org.easysoa.registry.indicators.rest;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import org.easysoa.registry.DocumentService;
 import org.easysoa.registry.integration.EndpointStateServiceImpl;
 import org.easysoa.registry.rest.integration.EndpointStateService;
 import org.easysoa.registry.rest.integration.SlaOrOlaIndicator;
 import org.easysoa.registry.types.Endpoint;
 import org.easysoa.registry.utils.NXQLQueryHelper;
 import org.nuxeo.ecm.core.api.CoreSession;
 import org.nuxeo.ecm.core.api.DocumentModel;
 import org.nuxeo.ecm.core.api.DocumentModelList;
 import org.nuxeo.runtime.api.Framework;
 
 /**
  *
  * @author jguillemotte
  */
 public class ServiceDefaultCountIndicatorProvider implements IndicatorProvider {
 
     private String category;
     
    public ServiceDefaultCountIndicatorProvider(String category) {
         if(category == null){
             this.category = "";
         } else {
             this.category = category;
         }
     }    
     
     @Override
     public List<String> getRequiredIndicators() {
         return null;
     }
 
     @Override
     public Map<String, IndicatorValue> computeIndicators(CoreSession session, String subProjectId, Map<String, IndicatorValue> computedIndicators, String visibility) throws Exception {
 
        // Get each SLA or OLA and check the ind:serviceLevelViolation attribute
        // If violation => set +1
        
         DocumentService documentService = Framework.getService(DocumentService.class);        
         EndpointStateService endpointStateService = new EndpointStateServiceImpl();
         
         // Init the result map
         Map<String, IndicatorValue> indicators = new HashMap<String, IndicatorValue>();
         
         // Get endpoints
         String subProjectPathCriteria = NXQLQueryHelper.buildSubprojectPathCriteria(session, subProjectId, visibility);
         StringBuilder query = new StringBuilder();
         query.append(DocumentService.NXQL_SELECT_FROM);
         query.append(Endpoint.DOCTYPE);
         if(!"".equals(subProjectPathCriteria)){
             query.append(DocumentService.NXQL_WHERE);
             query.append(subProjectPathCriteria);
         }
         DocumentModelList endpointsList = documentService.query(session, query.toString(), true, false);
         
         // For each endpoint, get the SLA/OLA indicators : check the violation and increase or not the counter
         int countValue = 0;
         int percentValue = -1;
         int totalNumberOfIndicators = 0;
         
         for(DocumentModel endpoint : endpointsList){
            // TODO : take only the latest indicator ?
             List<SlaOrOlaIndicator> slaOlaIndicators = endpointStateService.getSlaOrOlaIndicators(endpoint.getId(), "", null, null, 10, 0).getSlaOrOlaIndicatorList();
             for(SlaOrOlaIndicator indicator : slaOlaIndicators){
                 totalNumberOfIndicators++;
                 if(indicator.isServiceLevelViolation()){
                     countValue++;
                 }
             }
         }
 
         // Compute the percentage
         if(totalNumberOfIndicators > 0){
             percentValue = countValue * 100 / totalNumberOfIndicators;
         }
         
         // Set the indicator
         IndicatorValue iValue = new IndicatorValue("Nombre de services en défaut", category, countValue, percentValue);
         indicators.put("Nombre de services en défaut", iValue);
         
         return indicators;
     }
 
 }
