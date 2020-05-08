 // ______________________________________________________________________________
 //
 //           Project: jolokia-munin-plugin
 //            Module: jolokia-munin-plugin
 //             Class: MuninProviderImplTest
 //              File: MuninProviderImplTest.java
 //        changed by: christian
 //       change date: 24.02.13 18:56
 //       description:
 // ______________________________________________________________________________
 //
 //         Copyright: (c) Christian Fritz, all rights reserved
 // ______________________________________________________________________________
 package de.chrfritz.jolokiamunin.munin.impl;
 
 import de.chrfritz.jolokiamunin.config.Category;
 import de.chrfritz.jolokiamunin.config.Field;
 import de.chrfritz.jolokiamunin.config.Graph;
 import de.chrfritz.jolokiamunin.jolokia.Fetcher;
import de.chrfritz.jolokiamunin.jolokia.FetcherFactory;
 import de.chrfritz.jolokiamunin.jolokia.Request;
 import de.chrfritz.jolokiamunin.munin.MuninProvider;
 import org.junit.After;
 import org.junit.Before;
import org.junit.Ignore;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 
import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.*;
 import static org.mockito.Mockito.when;
 
 @RunWith(MockitoJUnitRunner.class)
 public class MuninProviderImplTest {
 
     @Mock
     private Fetcher fetcher;
 
    @Mock
    private FetcherFactory factory;

     private MuninProvider provider;
 
     @Before
     public void setUp() throws Exception {
        when(factory.getInstance((URL) any())).thenReturn(fetcher);
        when(factory.getInstance(anyString())).thenReturn(fetcher);

        provider = new MuninProviderImpl(factory);
     }
 
     @After
     public void tearDown() throws Exception {
         provider = null;
     }
 
     @Test
    @Ignore
     public void testGetConfig() throws Exception {
 
     }
 
     @Test
    @Ignore
     public void testGetConfig1() throws Exception {
 
     }
 
     @Test
     public void testGetValues() throws Exception {
 
         Map<Request, Number> fetcherResult = new HashMap<>();
         when(fetcher.fetchValues(anyList())).thenReturn(fetcherResult);
         Category category = new Category();
         category.setName("jetty");
         category.setGraphs(new ArrayList<Graph>());
         addHeapMemUsage(fetcherResult, category);
         addNonHeapMemUsage(fetcherResult, category);
 
         String actual = provider.getValues(category);
 
         String expected = "multigraph jettyHeapMemoryUsage\n" +
                 "HeapMemoryUsage_max.value 1000\n" +
                 "HeapMemoryUsage_used.value 1000\n" +
                 "HeapMemoryUsage_committed.value 1000\n" +
                 "HeapMemoryUsage_init.value 1000\n" +
                 "multigraph jettyNonHeapMemoryUsage\n" +
                 "NonHeapMemoryUsage_max.value 1000\n" +
                 "NonHeapMemoryUsage_used.value 1000\n" +
                 "NonHeapMemoryUsage_committed.value 1000\n" +
                 "NonHeapMemoryUsage_init.value 1000\n";
 
         assertEquals(expected, actual);
     }
 
     @Test
     public void testGetValuesList() throws Exception {
 
         Map<Request, Number> fetcherResult = new HashMap<>();
         when(fetcher.fetchValues(anyList())).thenReturn(fetcherResult);
         List<Category> categories = new ArrayList<>();
 
         Category category = new Category();
         category.setName("jetty");
         category.setGraphs(new ArrayList<Graph>());
         addHeapMemUsage(fetcherResult, category);
         categories.add(category);
 
         category = new Category();
         category.setName("tomcat");
         category.setGraphs(new ArrayList<Graph>());
         addNonHeapMemUsage(fetcherResult, category);
         categories.add(category);
 
         String actual = provider.getValues(categories);
 
         String expected = "multigraph jettyHeapMemoryUsage\n" +
                 "HeapMemoryUsage_max.value 1000\n" +
                 "HeapMemoryUsage_used.value 1000\n" +
                 "HeapMemoryUsage_committed.value 1000\n" +
                 "HeapMemoryUsage_init.value 1000\n" +
                 "multigraph tomcatNonHeapMemoryUsage\n" +
                 "NonHeapMemoryUsage_max.value 1000\n" +
                 "NonHeapMemoryUsage_used.value 1000\n" +
                 "NonHeapMemoryUsage_committed.value 1000\n" +
                 "NonHeapMemoryUsage_init.value 1000\n";
 
         assertEquals(expected, actual);
     }
 
     private void addHeapMemUsage(Map<Request, Number> fetcherResult, Category category) {
         Graph graph = createGraph("HeapMemoryUsage", "java.lang:type=Memory", "HeapMemoryUsage");
         addField(graph, "max", "max");
         addField(graph, "used", "used");
         addField(graph, "committed", "committed");
         addField(graph, "init", "init");
         category.getGraphs().add(graph);
 
         fetcherResult.put(new Request("java.lang:type=Memory", "HeapMemoryUsage", "max"), 1000);
         fetcherResult.put(new Request("java.lang:type=Memory", "HeapMemoryUsage", "used"), 1000);
         fetcherResult.put(new Request("java.lang:type=Memory", "HeapMemoryUsage", "committed"), 1000);
         fetcherResult.put(new Request("java.lang:type=Memory", "HeapMemoryUsage", "init"), 1000);
     }
 
     private void addNonHeapMemUsage(Map<Request, Number> fetcherResult, Category category) {
         Graph graph;
         graph = createGraph("NonHeapMemoryUsage", "java.lang:type=Memory", "NonHeapMemoryUsage");
         addField(graph, "max", "max");
         addField(graph, "used", "used");
         addField(graph, "committed", "committed");
         addField(graph, "init", "init");
         category.getGraphs().add(graph);
 
         fetcherResult.put(new Request("java.lang:type=Memory", "NonHeapMemoryUsage", "max"), 1000);
         fetcherResult.put(new Request("java.lang:type=Memory", "NonHeapMemoryUsage", "used"), 1000);
         fetcherResult.put(new Request("java.lang:type=Memory", "NonHeapMemoryUsage", "committed"), 1000);
         fetcherResult.put(new Request("java.lang:type=Memory", "NonHeapMemoryUsage", "init"), 1000);
     }
 
     private Graph createGraph(String name, String mbean) {
         return createGraph(name, mbean);
     }
 
     private Graph createGraph(String name, String mbean, String attribute) {
         Graph graph = new Graph();
         graph.setName(name);
         graph.setMbean(mbean);
         graph.setAttribute(attribute);
         graph.setFields(new ArrayList<Field>());
         return graph;
     }
 
     private void addField(Graph graph, String name, String path) {
         addField(graph, name, path, null, null);
     }
 
     private void addField(Graph graph, String name, String path, String attribute) {
         addField(graph, name, path, attribute, null);
     }
 
     private void addField(Graph graph, String name, String path, String attribute, String mbean) {
         Field field = new Field();
         field.setName(name);
         field.setPath(path);
         field.setAttribute(attribute);
         field.setMbean(mbean);
         graph.getFields().add(field);
     }
 
 }
