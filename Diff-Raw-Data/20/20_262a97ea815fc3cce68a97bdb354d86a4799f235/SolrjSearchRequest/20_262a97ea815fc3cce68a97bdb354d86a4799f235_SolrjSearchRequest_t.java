 /*
  * See the NOTICE file distributed with this work for additional
  * information regarding copyright ownership.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package org.xwiki.platform.search.internal;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import javax.inject.Inject;
 import javax.inject.Named;
 
import org.apache.commons.lang.StringUtils;
 import org.apache.ecs.xhtml.param;
 import org.apache.solr.common.params.ModifiableSolrParams;
 import org.apache.solr.common.util.NamedList;
 import org.apache.solr.common.util.SimpleOrderedMap;
 import org.apache.solr.core.CoreContainer;
 import org.apache.solr.core.SolrCore;
 import org.apache.solr.core.SolrInfoMBean;
 import org.apache.solr.handler.admin.LukeRequestHandler;
 import org.apache.solr.request.LocalSolrQueryRequest;
 import org.apache.solr.response.SolrQueryResponse;
 import org.slf4j.Logger;
 import org.xwiki.component.annotation.Component;
 import org.xwiki.component.annotation.InstantiationStrategy;
 import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
 import org.xwiki.context.Execution;
 import org.xwiki.context.ExecutionContextManager;
 import org.xwiki.model.reference.EntityReference;
 import org.xwiki.platform.search.DocumentField;
 import org.xwiki.platform.search.SearchEngine;
 import org.xwiki.platform.search.SearchRequest;
 
 import com.xpn.xwiki.XWikiContext;
 import com.xpn.xwiki.util.XWikiStubContextProvider;
 
 /**
  * @version $Id$
  */
 @Component
 @Named(SolrjSearchRequest.HINT)
 @InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
 public class SolrjSearchRequest implements SearchRequest
 {
 
     public static final String HINT = "solrjrequest";
 
     @Inject
     private ExecutionContextManager executionContextManager;
 
     @Inject
     private Execution execution;
 
     @Inject
     @Named(SolrjSearchEngine.HINT)
     private SearchEngine searchEngine;
 
     @Inject
     private Logger logger;
 
     @Inject
     protected XWikiStubContextProvider contextProvider;
 
     protected Map<String, String> searchParametersMap;
 
     protected Map<String, String> filterParametersMap;
 
     protected String queryString;
 
     protected List<String> languages;
 
     protected EntityReference entityReference;
     
     protected  List<String> fields;
 
     /**
      * {@inheritDoc}
      * 
      * @see org.xwiki.platform.search.SearchRequest#processRequestQuery(java.lang.String)
      */
     @Override
     public String processRequestQuery(String query)
     {
         fields= getFields();
         String language = getXWikiContext().getLanguage();
         String[] params = query.trim().split(" ");
         Map<String, String> paramMap = new HashMap<String, String>();
         for (String param : params) {
             if (param.contains(":")) {
                 String[] pa = param.split(":");
                 if (fields.contains(pa[0] + "_" + language)) {
                     pa[0] = pa[0] + "_" + language;
 
                 }
                 paramMap.put(pa[0], pa[1]);
             } else {
                 paramMap.put(param, null);
             }
         }
 
         StringBuilder builder = new StringBuilder();
         
         for (Entry<String, String> entry : paramMap.entrySet()) {
             builder.append(entry.getKey());
             if (entry.getValue() != null) {
                 builder.append(":" + entry.getValue());
             }
             builder.append(" ");
         }
 
        String queryString=builder.toString();
        
        if(!StringUtils.isEmpty(queryString) && !queryString.contains(DocumentField.LANGUAGE)){
            queryString="+("+queryString+") +"+DocumentField.LANGUAGE+":"+language;
        }
        
        return queryString;
 
     }
     
     public String processQueryFrequency(String qfString){
         String[] qfArray=qfString.split(" ");
         Map<String,String> qfMap=new HashMap<String, String>();
         fields= getFields();
         String language = getXWikiContext().getLanguage();
         for (String qfItem : qfArray) {
             if (qfItem.contains("^")) {
                 String[] qf = qfItem.split("\\^");
                 if (fields.contains(qf[0] + "_" + language)) {
                     qfMap.put(qf[0] + "_" + language, qf[1]);
                 }else if(fields.contains(qf[0])){
                     qfMap.put(qf[0], qf[1]);
                 }
    
             }
         }
         
         StringBuilder builder=new StringBuilder();
         for (Entry<String, String> entry : qfMap.entrySet()) {
             builder.append(entry.getKey());
             if (entry.getValue() != null) {
                 builder.append("^" + entry.getValue());
             }
             builder.append(" ");
         }
         
         return builder.toString().trim();
     }
 
     private List<String> getFields()
     {
         CoreContainer container = (CoreContainer) searchEngine.getCoreContainer();
         SolrCore core = null;
         for (SolrCore c : container.getCores()) {
             core = c;
         }
 
         Map<String, SolrInfoMBean> reg = core.getInfoRegistry();
         LukeRequestHandler handler = (LukeRequestHandler) reg.get("/admin/luke");
         LocalSolrQueryRequest req = new LocalSolrQueryRequest(core, new ModifiableSolrParams());
         SolrQueryResponse response = new SolrQueryResponse();
         handler.handleRequest(req, response);
         NamedList list = response.getValues();
 
         ArrayList<String> fieldsList = new ArrayList<String>();
         for (Object obj : list.getAll("fields")) {
             SimpleOrderedMap map = (SimpleOrderedMap) obj;
             Iterator<Map.Entry<String, Object>> entries = map.iterator();
             while (entries.hasNext()) {
                 Map.Entry<String, Object> entry = entries.next();
                 fieldsList.add(entry.getKey());
             }
         }
 
         return fieldsList;
 
     }
 
     /**
      * gets the XWikiContext
      * 
      * @return the XWikiContext
      */
     protected XWikiContext getXWikiContext()
     {
         XWikiContext context = (XWikiContext) execution.getContext().getProperty(XWikiContext.EXECUTIONCONTEXT_KEY);
         if (context == null) {
             context = this.contextProvider.createStubContext();
             logger.info(context.toString());
             this.execution.getContext().setProperty(XWikiContext.EXECUTIONCONTEXT_KEY, context);
         }
         return context;
     }
 
     /**
      * @return the queryString
      */
     @Override
     public String getQueryString()
     {
         return queryString;
     }
 
     /**
      * @param queryString the queryString to set
      */
     @Override
     public void setQueryString(String queryString)
     {
         this.queryString = queryString;
     }
 
     /**
      * @return the searchParametersMap
      */
     public Map<String, String> getSearchParametersMap()
     {
         if (searchParametersMap == null) {
             searchParametersMap = new HashMap<String, String>();
         }
         return searchParametersMap;
     }
 
     /**
      * @param searchParametersMap the searchParametersMap to set
      */
     public void setSearchParametersMap(Map<String, String> searchParametersMap)
     {
         this.searchParametersMap = searchParametersMap;
     }
 
     /**
      * @return the filterParametersMap
      */
     public Map<String, String> getFilterParametersMap()
     {
         if (filterParametersMap == null) {
             filterParametersMap = new HashMap<String, String>();
         }
         return filterParametersMap;
     }
 
     /**
      * @param filterParametersMap the filterParametersMap to set
      */
     public void setFilterParametersMap(Map<String, String> filterParametersMap)
     {
         this.filterParametersMap = filterParametersMap;
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see org.xwiki.platform.search.SearchRequest#setLanguages(java.util.List)
      */
     @Override
     public void setLanguages(List<String> languages)
     {
         this.languages = languages;
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see org.xwiki.platform.search.SearchRequest#setEntityReference(org.xwiki.model.reference.EntityReference)
      */
     @Override
     public void setEntityReference(EntityReference entityReference)
     {
         this.entityReference = entityReference;
     }
 
     /**
      * @return the languages
      */
     public List<String> getLanguages()
     {
         return languages;
     }
 
     /**
      * @return the entityReference
      */
     public EntityReference getEntityReference()
     {
         return entityReference;
     }
 
 }
