 /*
  * Copyright 2010 Jasha Joachimsthal
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.onehippo.forge.weblogdemo.components;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.lang.StringEscapeUtils;
 import org.apache.commons.lang.StringUtils;
 import org.hippoecm.hst.content.beans.query.HstQuery;
 import org.hippoecm.hst.content.beans.query.HstQueryResult;
 import org.hippoecm.hst.content.beans.query.exceptions.QueryException;
 import org.hippoecm.hst.content.beans.query.filter.Filter;
 import org.hippoecm.hst.content.beans.standard.HippoBean;
 import org.hippoecm.hst.content.beans.standard.HippoBeanIterator;
 import org.hippoecm.hst.core.component.HstComponentException;
 import org.hippoecm.hst.core.component.HstRequest;
 import org.hippoecm.hst.core.component.HstResponse;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import org.onehippo.forge.weblogdemo.beans.BaseDocument;
 
 /**
  * Simple search component. Excludes construction
  * @author Jasha Joachimsthal
  *
  */
 public class Search extends BaseSiteComponent {
 
     private static final String SEARCHFOR_PARAM = "searchfor";
     private static final String PAGEPARAM = "page";
     public static final Logger log = LoggerFactory.getLogger(Search.class);
     public static final int PAGESIZE = 10;
 
     @Override
     public void doBeforeRender(HstRequest request, HstResponse response) throws HstComponentException {
         super.doBeforeRender(request, response);
         List<BaseDocument> documents = new ArrayList<BaseDocument>();
 
         String pageStr = request.getParameter(PAGEPARAM);
         String query = getPublicRequestParameter(request, SEARCHFOR_PARAM);
         if (StringUtils.isBlank(query)) {
             query = request.getParameter(SEARCHFOR_PARAM);
         }
         int page = 0;
         if (StringUtils.isNotBlank(pageStr)) {
             try {
                 page = Integer.parseInt(pageStr);
             } catch (NumberFormatException e) {
                 // empty ignore
             }
         }
         
         
         request.setAttribute(PAGEPARAM, page);
         try {
             List<HippoBean> excludes = new ArrayList<HippoBean>();
             HippoBean construction = this.getSiteContentBaseBean(request).getBean("construction");
             if (construction != null) {
                 excludes.add(construction);
             }
             
             HstQuery hstQuery = getQueryManager().createQuery(getSiteContentBaseBean(request));
             hstQuery.excludeScopes(excludes);
             if (StringUtils.isNotBlank(query)) {
                 Filter filter = hstQuery.createFilter();
                 filter.addContains(".", query);
                 hstQuery.setFilter(filter);
                 request.setAttribute(SEARCHFOR_PARAM, StringEscapeUtils.escapeHtml(query));
             }
             HstQueryResult result = hstQuery.execute();
             HippoBeanIterator beans = result.getHippoBeans();
             if (beans == null) {
                 return;
             }
 
             long beansSize = beans.getSize();
            long pages = beansSize % PAGESIZE > 0L ? beansSize / PAGESIZE + 1L : beansSize / PAGESIZE;
 
             request.setAttribute("nrhits", beansSize > 0 ? beansSize : 0);
             request.setAttribute("pages", pages);
             int results = 0;
             if (beansSize > page * PAGESIZE) {
                 beans.skip(page * PAGESIZE);
             }
             while (beans.hasNext() && results < PAGESIZE) {
                 HippoBean bean = beans.next();
                 if (bean != null && bean instanceof BaseDocument) {
                     documents.add((BaseDocument) bean);
                     results++;
                 }
             }
         } catch (QueryException e) {
             log.warn("Error in search", e);
         }
         request.setAttribute("documents", documents);
     }
 
 }
