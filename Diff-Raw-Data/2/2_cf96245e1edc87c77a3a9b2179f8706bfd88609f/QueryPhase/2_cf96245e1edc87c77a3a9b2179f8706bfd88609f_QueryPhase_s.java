 package com.dottydingo.hyperion.service.pipeline.phase;
 
 import com.dottydingo.hyperion.exception.BadRequestException;
 import com.dottydingo.hyperion.service.configuration.HyperionEndpointConfiguration;
 import com.dottydingo.hyperion.service.endpoint.EntityResponse;
 import com.dottydingo.hyperion.service.persistence.PersistenceContext;
 import com.dottydingo.hyperion.service.persistence.QueryResult;
 import com.dottydingo.hyperion.service.context.HyperionContext;
 import com.dottydingo.service.endpoint.context.EndpointRequest;
 import com.dottydingo.service.endpoint.context.EndpointResponse;
 
 /**
  */
 public class QueryPhase extends BasePersistencePhase<HyperionContext>
 {
     private HyperionEndpointConfiguration configuration;
 
     public void setConfiguration(HyperionEndpointConfiguration configuration)
     {
         this.configuration = configuration;
     }
 
     @Override
     protected void executePhase(HyperionContext phaseContext) throws Exception
     {
         EndpointRequest request = phaseContext.getEndpointRequest();
         EndpointResponse response = phaseContext.getEndpointResponse();
 
         Integer start = getIntegerParameter("start",request);
         Integer limit = getIntegerParameter("limit",request);
 
         String query = request.getFirstParameter("query");
         String sort = request.getFirstParameter("sort");
 
         if(start != null && start < 1)
             throw new BadRequestException("The start parameter must be greater than zero.");
 
         if(limit != null && limit < 1)
             throw new BadRequestException("The limit parameter must be greater than zero.");
 
         if(limit == null)
             limit = configuration.getDefaultLimit();
 
         if(limit > configuration.getMaxLimit())
             throw new BadRequestException(String.format("The limit parameter can not be greater than %d.",configuration.getMaxLimit()));
 
         PersistenceContext persistenceContext = buildPersistenceContext(phaseContext);
         QueryResult queryResult = phaseContext.getEntityPlugin().getPersistenceOperations().query(query, start, limit, sort, persistenceContext);
 
         EntityResponse entityResponse = new EntityResponse();
         entityResponse.setEntries(queryResult.getItems());
         entityResponse.setResponseCount(queryResult.getResponseCount());
        entityResponse.setStart(start);
         entityResponse.setTotalCount(queryResult.getTotalCount());
 
         phaseContext.setResult(entityResponse);
 
         response.setResponseCode(200);
 
 
     }
 }
