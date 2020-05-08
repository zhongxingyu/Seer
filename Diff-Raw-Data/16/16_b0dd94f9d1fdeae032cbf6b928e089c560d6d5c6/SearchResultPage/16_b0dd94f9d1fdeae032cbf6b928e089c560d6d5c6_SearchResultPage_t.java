 /*
  * Copyright (C) 2012 Martin Virag <martin.virag@eea.sk> This file is part of Open Data Node. Open Data Node is free
  * software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or (at your option) any later version. Open Data Node
  * is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details. You should
  * have received a copy of the GNU General Public License along with Open Data Node. If not, see
  * <http://www.gnu.org/licenses/>.
  */
 
 package sk.opendatanode.ui.search;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.solr.client.solrj.response.QueryResponse;
 import org.apache.solr.common.SolrDocument;
import org.apache.wicket.Page;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.Link;
 import org.apache.wicket.markup.html.list.ListItem;
 import org.apache.wicket.markup.html.list.PageableListView;
 import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.protocol.http.RequestUtils;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;
 import org.apache.wicket.request.mapper.parameter.PageParameters;
 
 import sk.opendatanode.solr.SolrServerRep;
 import sk.opendatanode.solr.SolrType;
import sk.opendatanode.ui.results.ResultDocumentPage;
 import sk.opendatanode.ui.search.facet.FacetPanel;
import sk.opendatanode.utils.http.ContentNegotiablePage;
 
 public class SearchResultPage extends Panel {
 
     private static final long serialVersionUID = 1965617550606209510L;
     private List<SolrDocument> resultList = new ArrayList<SolrDocument>();
 
     public SearchResultPage(String id, PageParameters parameters, QueryResponse response) {
         super(id);
 
         resultList.addAll(response.getResults());
         
         add(new FacetPanel("facet", parameters, response.getFacetQuery()));
         add(new Label("searchHeader", "VÝSLEDKY VYHĽADÁVANIA"));
 
         MyPageableListView listView = new MyPageableListView("resultList", resultList, SolrServerRep.RESULTS_PER_PAGE);
         add(listView);
 
         add(new CustomPagingNavigator("paginator", listView));
     }
 
     private class MyPageableListView extends PageableListView<SolrDocument> {
 
         private static final long serialVersionUID = 578459835138276328L;
 
         public MyPageableListView(String id, List<SolrDocument> resultList, int rowsPerPage) {
             super(id, resultList, rowsPerPage);
         }
 
         @Override
         protected void populateItem(ListItem<SolrDocument> item) {
             final SolrDocument solrResultItem = item.getModelObject();
             int index = item.getIndex() + 1;
 
             item.add(new Label("itemNumber", index + ". "));
            
            item.add(new ExternalLink("itemUrl", "item/" + solrResultItem.get("id"),
             getLabel(solrResultItem)));          
 //            item.add(new ExternalLink("itemUrl", "http://www.opendata.sk/item/" + solrResultItem.get("id"),
 //                    getLabel(solrResultItem)));
         }
 
         private String getLabel(SolrDocument solrResultItem) {
             String type = (String) solrResultItem.get("type");
             // TODO document type as link on the document
             // TODO 1-2 lines from content and highlighting searched query
             if (SolrType.ORGANIZATION.getTypeString().equals(type)) {
                 return (String) solrResultItem.get("name");
             } else if (SolrType.PROCUREMENT.getTypeString().equals(type)) {
                 return "from: " + solrResultItem.get("customer_ico") + "(ICO), to:"
                         + solrResultItem.get("supplier_ico") + "(ICO)";
             } else if (SolrType.POLITICAL_PARTY_DONATION.getTypeString().equals(type)) {
                 return (String) solrResultItem.get("name") != null ? (String) solrResultItem.get("name")
                         : solrResultItem.get("donor_name") + " " + solrResultItem.get("donor_surname");
             }
             return null;
         }
 
     }
 }
