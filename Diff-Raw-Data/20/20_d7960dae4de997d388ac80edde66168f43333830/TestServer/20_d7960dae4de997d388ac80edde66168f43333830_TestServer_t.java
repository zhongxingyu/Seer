 /*
  * Copyright 2011 Proofpoint, Inc.
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
 package com.proofpoint.event.monitor;
 
 import com.google.common.base.Charsets;
 import com.google.common.base.Function;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.Iterables;
 import com.google.inject.Binder;
 import com.google.inject.Injector;
 import com.google.inject.Key;
 import com.google.inject.Module;
 import com.google.inject.Scopes;
 import com.google.inject.TypeLiteral;
 import com.google.inject.util.Modules;
 import com.proofpoint.bootstrap.Bootstrap;
 import com.proofpoint.bootstrap.LifeCycleManager;
 import com.proofpoint.discovery.client.DiscoveryModule;
 import com.proofpoint.http.client.ApacheHttpClient;
 import com.proofpoint.http.client.FullJsonResponseHandler.JsonResponse;
 import com.proofpoint.http.client.HttpClient;
 import com.proofpoint.http.client.HttpClientConfig;
 import com.proofpoint.http.client.Request;
 import com.proofpoint.http.server.testing.TestingHttpServer;
 import com.proofpoint.http.server.testing.TestingHttpServerModule;
 import com.proofpoint.jaxrs.JaxrsModule;
 import com.proofpoint.jmx.JmxHttpModule;
 import com.proofpoint.jmx.JmxModule;
 import com.proofpoint.json.JsonCodec;
 import com.proofpoint.json.JsonModule;
 import com.proofpoint.node.testing.TestingNodeModule;
 import org.joda.time.DateTime;
 import org.testng.Assert;
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import javax.annotation.Nullable;
 import javax.management.MBeanServer;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response.Status;
 import java.net.URI;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import static com.google.common.collect.Iterables.concat;
 import static com.google.common.collect.Lists.newArrayList;
 import static com.google.common.collect.Maps.newHashMap;
 import static com.proofpoint.http.client.FullJsonResponseHandler.createFullJsonResponseHandler;
 import static com.proofpoint.http.client.Request.Builder.prepareDelete;
 import static com.proofpoint.http.client.Request.Builder.prepareGet;
 import static com.proofpoint.http.client.Request.Builder.preparePost;
 import static com.proofpoint.http.client.StaticBodyGenerator.createStaticBodyGenerator;
 import static com.proofpoint.http.client.StatusResponseHandler.createStatusResponseHandler;
 import static com.proofpoint.testing.Assertions.assertEqualsIgnoreOrder;
 import static java.util.Collections.nCopies;
 import static org.mockito.Mockito.mock;
 import static org.testng.Assert.assertEquals;
 
 public class TestServer
 {
     private static final JsonCodec<Map<String, Integer>> STATS_CODEC = JsonCodec.mapJsonCodec(String.class, Integer.class);
     private static final JsonCodec<Map<String, Object>> MONITOR_CODEC = JsonCodec.mapJsonCodec(String.class, Object.class);
     private static final JsonCodec<List<Map<String, Object>>> MONITOR_LIST_CODEC = JsonCodec.listJsonCodec(JsonCodec.mapJsonCodec(String.class, Object.class));
 
     private HttpClient client;
     private TestingHttpServer server;
    private Monitor scorerHttpMonitor;
    private Monitor prsMessageMonitor;
     private LifeCycleManager lifeCycleManager;
 
     @BeforeMethod
     public void setup()
             throws Exception
     {
         ImmutableMap<String, String> config = ImmutableMap.of("monitor.file", "src/test/resources/monitor.json");
 
         final MBeanServer mockMBeanServer = mock(MBeanServer.class);
 
         Bootstrap app = new Bootstrap(
                 new TestingNodeModule(),
                 new TestingHttpServerModule(),
                 new JsonModule(),
                 new JaxrsModule(),
                 new JmxHttpModule(),
                 Modules.override(new JmxModule()).with(
                         new Module()
                         {
                             @Override
                             public void configure(Binder binder)
                             {
                                 binder.bind(MBeanServer.class).toInstance(mockMBeanServer);
                             }
                         }),
                 new DiscoveryModule(),
                 new MainModule(),
                 new Module()
                 {
                     @Override
                     public void configure(Binder binder)
                     {
                         binder.bind(Alerter.class).to(InMemoryAlerter.class).in(Scopes.SINGLETON);
                     }
                 }
         ).setRequiredConfigurationProperties(config);
 
         Injector injector = app
                 .doNotInitializeLogging()
                 .initialize();
 
         lifeCycleManager = injector.getInstance(LifeCycleManager.class);
         server = injector.getInstance(TestingHttpServer.class);
 
         Map<String, Monitor> monitors = newHashMap();
         for (Monitor monitor : newArrayList(injector.getInstance(Key.get(new TypeLiteral<Set<Monitor>>() { })))) {
             monitors.put(monitor.getName(), monitor);
         }
 
         Assert.assertEquals(monitors.size(), 6);
 
         scorerHttpMonitor = monitors.get("ScorerHttpMonitor");
         Assert.assertNotNull(scorerHttpMonitor);
 
         prsMessageMonitor = monitors.get("PrsMessageMonitor");
         Assert.assertNotNull(prsMessageMonitor);
 
         client = new ApacheHttpClient(new HttpClientConfig());
     }
 
     @AfterMethod
     public void teardown()
             throws Exception
     {
         if (lifeCycleManager != null) {
             lifeCycleManager.stop();
         }
     }
 
     @Test
     public void testPostEvents()
             throws Exception
     {
         List<Event> events = newArrayList(concat(
                 nCopies(3, new Event("HttpRequest", "id", "host", new DateTime(), ImmutableMap.of("requestUri", "/v1/scorer/foo", "responseCode", 204))),
                 nCopies(5, new Event("not-HttpRequest", "id", "host", new DateTime(), ImmutableMap.<String, Object>of())),
                 nCopies(7, new Event("HttpRequest", "id", "host", new DateTime(), ImmutableMap.of("requestUri", "/other/path"))),
                 nCopies(11, new Event("PrsMessage", "id", "host", new DateTime(), ImmutableMap.<String, Object>of())),
                 nCopies(13, new Event("not-PrsMessage", "id", "host", new DateTime(), ImmutableMap.<String, Object>of())),
                 nCopies(17, new Event("HttpRequest", "id", "host", new DateTime(), ImmutableMap.of("requestUri", "/v1/scorer/foo", "responseCode", 400)))
         ));
         final String json = JsonCodec.listJsonCodec(Event.class).toJson(events);
 
         Request request = preparePost()
                 .setUri(urlFor("/v1/event"))
                 .setHeader("Content-Type", MediaType.APPLICATION_JSON)
                 .setBodyGenerator(createStaticBodyGenerator(json, Charsets.UTF_8))
                 .build();
         int statusCode = client.execute(request, createStatusResponseHandler()).getStatusCode();
 
         assertEquals(statusCode, Status.NO_CONTENT.getStatusCode());
         Assert.assertEquals(scorerHttpMonitor.getEvents().getCount(), 3);
         Assert.assertEquals(prsMessageMonitor.getEvents().getCount(), 11);
     }
 
     @Test
     public void testListMonitors()
             throws Exception
     {
         Request request = prepareGet()
                 .setUri(urlFor("/v1/monitor"))
                 .build();
         JsonResponse<List<Map<String, Object>>> response = client.execute(request, createFullJsonResponseHandler(MONITOR_LIST_CODEC));
 
         assertEquals(response.getStatusCode(), Status.OK.getStatusCode());
         List<Map<String,Object>> actual = response.getValue();
         Iterable<String> entryNames = Iterables.transform(actual, new Function<Map<String, Object>, String>()
         {
             @Override
             public String apply(@Nullable Map<String, Object> monitor)
             {
                 return (String) monitor.get("name");
             }
         });
 
         assertEqualsIgnoreOrder(entryNames, ImmutableList.of(
                 "ScorerHttpMonitor",
                 "ScorerHttpErrorMonitor",
                 "PrsMessageMonitor",
                 "Min",
                 "Max",
                 "Between"));
     }
 
     @Test
     public void testGetMonitorDetail()
             throws Exception
     {
         Request request = prepareGet()
                 .setUri(urlFor("/v1/monitor/ScorerHttpMonitor"))
                 .build();
         JsonResponse<Map<String, Object>> response = client.execute(request, createFullJsonResponseHandler(MONITOR_CODEC));
 
         assertEquals(response.getStatusCode(), Status.OK.getStatusCode());
         Map<String, Object> actual = response.getValue();
 
         assertEquals(actual, ImmutableMap.<String, Object>builder()
                 .put("name", "ScorerHttpMonitor")
                 .put("ok", true)
                 .put("minimumOneMinuteRate", 100.0)
                 .put("oneMinuteRate", 0.0)
                 .put("fiveMinuteRate", 0.0)
                 .put("fifteenMinuteRate", 0.0)
                 .put("self", urlFor("/v1/monitor/ScorerHttpMonitor").toString())
                 .build());
     }
 
     @Test
     public void testGetMonitorState()
             throws Exception
     {
         Request request = prepareGet()
                 .setUri(urlFor("/v1/monitor/ScorerHttpMonitor"))
                 .build();
         JsonResponse<Map<String, Object>> response = client.execute(request, createFullJsonResponseHandler(MONITOR_CODEC));
 
         assertEquals(response.getStatusCode(), Status.OK.getStatusCode());
 
         Map<String, Object> before = response.getValue();
         assertEquals(before.get("ok"), true);
 
         failHttpScorerMonitor();
 
         request = prepareGet()
                 .setUri(urlFor("/v1/monitor/ScorerHttpMonitor"))
                 .build();
         response = client.execute(request, createFullJsonResponseHandler(MONITOR_CODEC));
 
         assertEquals(response.getStatusCode(), Status.OK.getStatusCode());
 
         Map<String, Object> after = response.getValue();
         assertEquals(after.get("ok"), false);
     }
 
     @Test
     public void testStats()
             throws Exception
     {
         List<Event> events = nCopies(3, new Event("HttpRequest", "id", "host", new DateTime(), ImmutableMap.of("requestUri", "/v1/scorer/foo", "responseCode", 204)));
 
         final String json = JsonCodec.listJsonCodec(Event.class).toJson(events);
 
         Request request = preparePost()
                 .setUri(urlFor("/v1/event"))
                 .setHeader("Content-Type", MediaType.APPLICATION_JSON)
                 .setBodyGenerator(createStaticBodyGenerator(json, Charsets.UTF_8))
                 .build();
 
         client.execute(request, createStatusResponseHandler());
 
         request = prepareGet()
                 .setUri(urlFor("/v1/event/stats"))
                 .build();
         JsonResponse<Map<String, Integer>> response = client.execute(request, createFullJsonResponseHandler(STATS_CODEC));
 
         assertEquals(response.getStatusCode(), Status.OK.getStatusCode());
         assertEquals(response.getValue(), ImmutableMap.of("HttpRequest", 3));
 
         request = prepareDelete()
                 .setUri(urlFor("/v1/event/stats"))
                 .build();
 
         assertEquals(client.execute(request, createStatusResponseHandler()).getStatusCode(), Status.NO_CONTENT.getStatusCode());
 
         request = prepareGet()
                 .setUri(urlFor("/v1/event/stats"))
                 .build();
         response = client.execute(request, createFullJsonResponseHandler(STATS_CODEC));
 
         assertEquals(response.getStatusCode(), Status.OK.getStatusCode());
         assertEquals(response.getValue(), ImmutableMap.of());
     }
 
     private void failHttpScorerMonitor()
     {
         for (int i = 0; i < 100; ++i) {
             scorerHttpMonitor.getEvents().tick();
         }
         scorerHttpMonitor.checkState();
     }
 
     private URI urlFor(String path)
     {
         return server.getBaseUrl().resolve(path);
     }
 }
