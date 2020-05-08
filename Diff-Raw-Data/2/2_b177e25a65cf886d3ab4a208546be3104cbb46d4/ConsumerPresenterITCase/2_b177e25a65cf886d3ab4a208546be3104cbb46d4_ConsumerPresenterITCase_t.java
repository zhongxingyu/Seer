 package org.ebayopensource.turmeric.monitoring.test.integration;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 import java.util.Date;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 import org.ebayopensource.turmeric.monitoring.client.Util;
 import org.ebayopensource.turmeric.monitoring.client.model.ConsumerMetric;
 import org.ebayopensource.turmeric.monitoring.client.model.Filterable;
 import org.ebayopensource.turmeric.monitoring.client.presenter.ConsumerPresenter;
 import org.ebayopensource.turmeric.monitoring.client.presenter.ServicePresenter;
 import org.ebayopensource.turmeric.monitoring.client.view.ConsumerView;
 import org.junit.Test;
 
 import com.google.gwt.dom.client.Node;
 import com.google.gwt.dom.client.NodeList;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.Tree;
 import com.google.gwt.user.client.ui.TreeItem;
 import com.google.gwt.user.client.ui.Widget;
 import com.octo.gwt.test.utils.events.Browser;
 
 public class ConsumerPresenterITCase extends ConsoleIntegrationTestBase {
 
     @Test
     public void testServicePresenterInitialization() {
         assertNotNull(consumerPresenter);
         ConsumerView view = (ConsumerView) consumerPresenter.getView();
         assertNotNull(view);
     }
 
     @Test
     public void testServiceSelectionFromServiceList() {
         ConsumerView view = (ConsumerView) consumerPresenter.getView();
         Date now = new Date();
         Filterable filter = view.getFilter();
         filter.setDate1(now);
         filter.setDate2(now);
         filter.setHour1(10);// 10 am
         filter.setDuration(2);// 2hrs
         filter.setSelectedMetricNames(Util.convertFromEnumToCamelCase(ConsumerPresenter.ANY_CONSUMER_METRICS));
         Browser.click((Widget) filter.getApplyButton());
         Tree serviceTree = (Tree) view.getSelector();
         assertNotNull(serviceTree);
         assertNotNull(serviceTree.getItem(0));// first level of the services
                                               // tree
         assertNotNull(serviceTree.getItem(0).getChild(0));// first service in
                                                           // the list
         TreeItem serviceToSelect = serviceTree.getItem(0).asTreeItem().getChild(0);
         String html = serviceToSelect.asTreeItem().getHTML();
         assertNotNull(html);
         Map<String, Set<String>> consumerData = service.getConsumerData();
         String firstServiceName = consumerData.keySet().iterator().next();
 
         assertTrue(html.contains(firstServiceName));
         // now, I select the first service in the tree
         // now, I select the first service in the tree
         selectServiceForTab(ConsumerPresenter.CONSUMER_ID, firstServiceName);
         // and now I get the data table in the view
         FlexTable table = view.getTable(ConsumerMetric.CallVolume);
         assertNotNull(table);
         Widget cellContent = table.getWidget(1, 0);
         assertNotNull(cellContent);
         // //I need to get the first operation of firstServiceName
         Iterator<String> operationIterator = consumerData.get(firstServiceName).iterator();
         NodeList<Node> childNodes = cellContent.getElement().getChildNodes();
         int childNodesLength = childNodes.getLength();
         for (int i = 0; i < childNodesLength; i++) {
             assertEquals(operationIterator.next(), childNodes.getItem(i).getNodeValue());
         }
 
     }
 
     @Test
     public void testDateSelectionInFilter() {
         assertNotNull(consumerPresenter);
         ConsumerView view = (ConsumerView) consumerPresenter.getView();
         assertNotNull(view);
         Tree serviceTree = (Tree) view.getSelector();
         assertNotNull(serviceTree);
         assertNotNull(serviceTree.getItem(0));// first level of the services
                                               // tree
         assertNotNull(serviceTree.getItem(0).getChild(0));// first service in
                                                           // the list
         TreeItem serviceToSelect = serviceTree.getItem(0).asTreeItem().getChild(0);
         String html = serviceToSelect.asTreeItem().getHTML();
         assertNotNull(html);
         Map<String, Set<String>> consumerData = service.getConsumerData();
         String firstServiceName = consumerData.keySet().iterator().next();
 
         assertTrue(html.contains(firstServiceName));
         // now, I select the first service in the tree
        selectServiceForTab(ConsumerPresenter.CONSUMER_ID, firstServiceName);
 
         view.getFilter().setDate1(new Date());
         Browser.click((Widget) view.getFilter().getApplyButton());
 
         // and now I get the data table in the view
         FlexTable table = view.getTable(ConsumerMetric.CallVolume);
         assertNotNull(table);
         Widget cellContent = table.getWidget(1, 0);
         assertNotNull(cellContent);
         // //I need to get the first operation of firstServiceName
         Iterator<String> operationIterator = consumerData.get(firstServiceName).iterator();
         NodeList<Node> childNodes = cellContent.getElement().getChildNodes();
         int childNodesLength = childNodes.getLength();
         for (int i = 0; i < childNodesLength; i++) {
             assertEquals(operationIterator.next(), childNodes.getItem(i).getNodeValue());
         }
     }
 
 }
