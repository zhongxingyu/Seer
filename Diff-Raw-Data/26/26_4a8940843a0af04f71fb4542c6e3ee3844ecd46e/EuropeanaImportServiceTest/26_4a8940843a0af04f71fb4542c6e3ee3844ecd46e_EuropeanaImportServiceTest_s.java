 package nl.waisda.services;
 
 import java.io.UnsupportedEncodingException;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpVersion;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpUriRequest;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.message.BasicHttpResponse;
 import org.apache.http.message.BasicStatusLine;
 import org.junit.Assert;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.Mockito;
 import org.mockito.invocation.InvocationOnMock;
 import org.mockito.stubbing.Answer;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.test.context.transaction.TransactionConfiguration;
 import org.springframework.transaction.annotation.Transactional;
 
 import nl.waisda.repositories.VideoRepository;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Danny
  * Date: 8-3-13
  * Time: 15:04
  * To change this template use File | Settings | File Templates.
  */
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = {"classpath:/nl/waisda/services/europeanaImportService-test.xml"})
 @Transactional
 @TransactionConfiguration(defaultRollback = true)
 public class EuropeanaImportServiceTest {
 //http://preview.europeana.eu/api/v2/search.json?wskey=xxxxxxxx&query=provider_aggregation_edm_dataProvider%3A%22Open+Beelden%22&start=1&rows=12&profile=standard
     @Autowired
     private EuropeanaImportServiceIF service;
 
     @Autowired
     private VideoRepository videoRepository;
 
     @Autowired
     private LocalContainerEntityManagerFactoryBean entityManagerFactory;
 
     @Autowired
     private HttpClient  httpClientMock;
 
     private HttpResponse mockDetailHttpResponse() throws UnsupportedEncodingException {
         BasicHttpResponse response = new BasicHttpResponse(new BasicStatusLine(new HttpVersion(100, 1), 200, null));
         response.setEntity(new StringEntity("{\"apikey\":\"XxxsEZoWj\",\"action\":\"record.json\",\"success\":true,\"statsDuration\":50,\"requestNumber\":3752," +
                 "\"object\":{\"type\":\"VIDEO\",\"year\":[\"1919\"]," +
                 "\"title\":[\"Opening Staten Generaal 20-9-1921\"]," +
                 "\"about\":\"/2021601/6D84A6DB3976320008D516D00F5399132A5E8EE9\"," +
                 "\"proxies\":[{\"about\":\"/proxy/provider/2021601/6D84A6DB3976320008D516D00F5399132A5E8EE9\"," +
                 "\"dcCreator\":{\"def\":[\"Nederlands Instituut voor Beeld en Geluid (beheerder)\",\"Netherlands Institute for Sound and Vison (curator)\"]}," +
                 "\"dcDate\":{\"def\":[\"1919-01-01\"]},\"dcDescription\":{\"def\":[]},\"dcLanguage\":{\"def\":[\"nl\"]}," +
                 "\"dcPublisher\":{\"def\":[\"Nederlands Instituut voor Beeld en Geluid\",\"http://www.openbeelden.nl/users/beeldengeluid\"]}," +
                 "\"dcSource\":{\"def\":[\"ONTHULLINGSTA-HRE00007FDA\"]}," +
                 "\"dcSubject\":{\"def\":[\"standbeelden\",\"volkscultuur\",\"steden\",\"zwemmen\",\"trams\",\"onthullingen\",\"wedstrijden\",\"optochten\",\"menigte\",\"hotels\",\"musici\",\"roeiboten\",\"kransleggingen\"]}," +
                 "\"dcTitle\":{\"def\":[\"Opening Staten Generaal 20-9-1921\"]}," +
                 "\"dcType\":{\"def\":[\"Moving Image\"]}," +
                 "\"dctermsAlternative\":{\"def\":[\"\",\"\"]}," +
                 "\"dctermsExtent\":{\"def\":[\"PT10M57S\"]}," +
                 "\"proxyIn\":[\"/aggregation/provider/2021601/6D84A6DB3976320008D516D00F5399132A5E8EE9\"]," +
                 "\"proxyFor\":\"/item/2021601/6D84A6DB3976320008D516D00F5399132A5E8EE9\"," +
                 "\"edmType\":\"VIDEO\",\"europeanaProxy\":false}," +
                 "{\"about\":\"/proxy/europeana/2021601/6D84A6DB3976320008D516D00F5399132A5E8EE9\"," +
                 "\"edmHasMet\":{\"def\":[\"http://semium.org/time/19xx_1_third\",\"http://semium.org/time/1919\"]}," +
                 "\"proxyIn\":[\"/aggregation/europeana/2021601/6D84A6DB3976320008D516D00F5399132A5E8EE9\"]," +
                 "\"proxyFor\":\"/item/2021601/6D84A6DB3976320008D516D00F5399132A5E8EE9\"," +
                 "\"year\":{\"def\":[\"1919\"]}," +
                 "\"europeanaProxy\":true}]," +
                 "\"aggregations\":[{\"about\":\"/aggregation/provider/2021601/6D84A6DB3976320008D516D00F5399132A5E8EE9\"," +
                 "\"edmDataProvider\":{\"def\":[\"Open Beelden\"]}," +
                 "\"edmIsShownBy\":\"http://www.openbeelden.nl/files/03/8613.3970.ONTHULLINGSTA-HRE00007FDA.mp4\"," +
                 "\"edmIsShownAt\":\"http://preview.europeana.eu/api/728/redirect.json?shownAt=http%3A%2F%2Fwww.openbeelden.nl%2Fmedia%2F3969%3Fbt%3Deuropeanaapi&provider=Nationale+Aggregator&id=http://www.europeana.eu/resolve/record/2021601/6D84A6DB3976320008D516D00F5399132A5E8EE9&profile=full\"," +
                 "\"edmObject\":\"http://www.openbeelden.nl/images/66690/Vlissingen_%285_28%29.png\"," +
                 "\"edmProvider\":{\"def\":[\"Nationale Aggregator\"]}," +
                 "\"edmRights\":{\"def\":[\"http://creativecommons.org/licenses/by-sa/3.0/nl/\"]}," +
                 "\"webResources\":[{\"about\":\"http://www.openbeelden.nl/media/3969\"}," +
                "{\"about\":\"http://www.openbeelden.nl/files/03/8613.3970.ONTHULLINGSTA-HRE00007FDA.mp4\"}," +
                 "{\"about\":\"http://www.openbeelden.nl/images/66690/Vlissingen_%285_28%29.png\"}]}]," +
                 "\"timespans\":[{\"about\":\"http://semium.org/time/19xx_1_third\"," +
                 "\"prefLabel\":{\"en\":[\"early 20th century\"],\"ru\":[\"начало 20-го века\"]}}," +
                 "{\"about\":\"http://semium.org/time/1919\"," +
                 "\"prefLabel\":{\"def\":[\"1919\"]}," +
                 "\"isPartOf\":{\"en\":[\"http://semium.org/time/19xx_1_third\"]}}]," +
                 "\"europeanaCompleteness\":10," +
                 "\"providedCHOs\":[{\"about\":\"/item/2021601/6D84A6DB3976320008D516D00F5399132A5E8EE9\"}],\"europeanaAggregation\":{\"about\":\"/aggregation/europeana/2021601/6D84A6DB3976320008D516D00F5399132A5E8EE9\"," +
                 "\"edmLandingPage\":\"http://www.europeana.eu/portal/record/2021601/6D84A6DB3976320008D516D00F5399132A5E8EE9\"," +
                 "\"edmCountry\":{\"def\":[\"netherlands\"]}," +
                 "\"edmLanguage\":{\"def\":[\"nl\"]}}," +
                 "\"europeanaCollectionName\":[\"2021601_Ag_NL_DNA_Open_Beelden\"]}}"));
 
         return response;
     }
 
     private HttpResponse mockOverviewHttpResponse() throws UnsupportedEncodingException {
         BasicHttpResponse response = new BasicHttpResponse(new BasicStatusLine(new HttpVersion(100, 1), 200, null));
         response.setEntity(new StringEntity("{\"apikey\":\"test\",\"action\":\"search.json\",\"success\":true,\"requestNumber\":3713,\"itemsCount\":1,\"totalResults\":1," +
                 "\"items\":[{\n" +
                 "  \"id\":\"/08614/6569D2A45FD1CD9ADFB0E5BA54A3BD79C9DAE0BC\",\n" +
                 "  \"provider\":[\"EFG - The European Film Gateway\"],\n" +
                "  \"edmPreview\":[\"http://europeanastatic.eu/api/image?uri=http%3A%2F%2Fwww.openbeelden.nl%2Fimages%2F64678%2FNieuws_uit_de_West_suikercultuur_in_Suriname_%25280_44%2529.png&size=LARGE&type=VIDEO\"],\n" +
                 "  \"europeanaCompleteness\":0,\n" +
                 "  \"year\":[\"1921\"],\n" +
                 "  \"rights\":[\"http://www.europeana.eu/rights/rr-f/\"],\n" +
                 "  \"title\":[\"Opening Staten Generaal 20-9-1921\",\n" +
                 "  \"Opening States General 20-9-1921\"],\n" +
                 "  \"type\":\"VIDEO\",\n" +
                 "  \"link\":\"http://detail\",\n" +
                 "  \"guid\":\"http://preview.europeana.eu/portal/record/08614/6569D2A45FD1CD9ADFB0E5BA54A3BD79C9DAE0BC.html?utm_source=api&utm_medium=api&utm_campaign=XxxsEZoWj\",\n" +
                 "  \"dataProvider\":[\"eye Film Instituut Nederland\",\"eye Film Instituut Nederland\"]\n" +
                 "}]}\n"));
 
         return response;
     }
 
     @Test
     @Transactional
     public void testImportAcceptMP4() {
         HttpUriRequest overview = new HttpGet("http://preview.europeana.eu/api/v2/search.json?wskey=XxxsEZoWj&query=dontcare&start=1&rows=12&profile=minimal");
         HttpUriRequest details = new HttpGet("http://detail");
         try {
             // setup http response
             Mockito.reset(httpClientMock);
             Mockito.when(httpClientMock.execute(Mockito.any(HttpUriRequest.class))).thenAnswer(new Answer<HttpResponse>() {
                 @Override
                 public HttpResponse answer(InvocationOnMock invocation) throws Throwable {
                     if (invocation != null && invocation.getArguments() != null && invocation.getArguments().length > 0) {
                         String url = ((HttpUriRequest) invocation.getArguments()[0]).getURI().toString();
                         if (url.equals("http://preview.europeana.eu/api/v2/search.json?wskey=XxxsEZoWj&query=dontcare&start=1&rows=12&profile=minimal")) {
                             return mockOverviewHttpResponse();
                         } else if (url.equals("http://detail")) {
                             return mockDetailHttpResponse();
                         }
                     }
                     return null;
                 }
             });
             // call the service
             service.importEuropeanaData("dontcare");
 
         } catch(Exception e) {
             Assert.fail("Not supposed to fail");
         }
 
 
     }
 
 }
