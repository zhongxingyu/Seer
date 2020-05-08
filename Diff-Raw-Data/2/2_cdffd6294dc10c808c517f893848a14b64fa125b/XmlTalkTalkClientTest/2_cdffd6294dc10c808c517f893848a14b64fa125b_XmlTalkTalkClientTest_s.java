 package org.atlasapi.remotesite.talktalk;
 
 import static org.hamcrest.Matchers.is;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertThat;
 import static org.mockito.Matchers.any;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.times;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 
 import java.io.Reader;
 import java.net.URL;
 import java.util.Map;
 
 import javax.xml.bind.Unmarshaller;
 
 import org.atlasapi.remotesite.talktalk.TalkTalkClient.TalkTalkTvStructureCallback;
 import org.atlasapi.remotesite.talktalk.TalkTalkClient.TalkTalkVodListCallback;
 import org.atlasapi.remotesite.talktalk.vod.bindings.ItemDetailType;
 import org.atlasapi.remotesite.talktalk.vod.bindings.TVDataInterfaceResponse;
 import org.atlasapi.remotesite.talktalk.vod.bindings.VodListType;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 
 import com.google.common.collect.Maps;
 import com.google.common.io.Resources;
 import com.google.common.net.HostSpecifier;
 import com.metabroadcast.common.http.HttpException;
 import com.metabroadcast.common.http.SimpleHttpClient;
 
 @RunWith(MockitoJUnitRunner.class)
 public class XmlTalkTalkClientTest {
     
     private HostSpecifier host;
     private SimpleHttpClient client;
     private TalkTalkTvDataInterfaceResponseParser parser;
 
     private TalkTalkClient ttClient;
 
     @Mock private TalkTalkTvStructureCallback<Integer> structureProcessor;
     @Mock private TalkTalkVodListCallback<Integer> vodEntityProcessor;
 
     @Before
     public void setUp() {
         host = HostSpecifier.fromValid("localhost.com");
         parser = mock(TalkTalkTvDataInterfaceResponseParser.class);
 
         Map<String, URL> resourceMap = Maps.newHashMap();
         resourceMap.put("http://localhost.com/TVDataInterface/VOD/List/2?groupType=IMAGE&groupIdentifier=COMPAPP2&page=0&itemsPerPage=5",
                 Resources.getResource(getClass(), "vod-picks.xml"));
         resourceMap.put("http://localhost.com/TVDataInterface/TVStructure/Structure/1",
                 Resources.getResource(getClass(), "structure.xml"));
         resourceMap.put("http://localhost.com/TVDataInterface/VOD/List/2?groupType=BRAND&groupIdentifier=ABCCASTL&page=0&itemsPerPage=5",
                 Resources.getResource(getClass(), "vod-list.xml"));
         resourceMap.put("http://localhost.com/TVDataInterface/VOD/List/2?groupType=BRAND&groupIdentifier=ABCCASTL&page=0&itemsPerPage=3",
                 Resources.getResource(getClass(), "vod-list.xml"));
         resourceMap.put("http://localhost.com/TVDataInterface/VOD/List/2?groupType=BRAND&groupIdentifier=ABCCASTL&page=1&itemsPerPage=3",
                 Resources.getResource(getClass(), "vod-list.xml"));
        resourceMap.put("http://localhost.com/TVDataInterface/Detail/Item/2?groupType=EPISODE&groupIdentifier=397212",
                 Resources.getResource(getClass(), "detail.xml"));
         client = new StubSimpleHttpClient(resourceMap);
         
         ttClient = new XmlTalkTalkClient(client, host, parser);
     }
     
     @Test
     public void testProcessTvStructure() throws HttpException, Exception {
 
         final TVDataInterfaceResponse response = new TVDataInterfaceResponse();
         when(parser.parse(any(Reader.class), any(Unmarshaller.Listener.class)))
             .thenReturn(response);
         when(structureProcessor.getResult())
             .thenReturn(42);
         
         Integer result = ttClient.processTvStructure(structureProcessor);
 
         verify(structureProcessor).getResult();
         verify(parser).parse(any(Reader.class), any(Unmarshaller.Listener.class));
         
         assertThat(result, is(42));
     }
 
     @Test
     public void testProcessVodList() throws HttpException, Exception {
         
         TVDataInterfaceResponse response = new TVDataInterfaceResponse();
         VodListType vodList = new VodListType();
         vodList.setTotalEntityCount(4);
         response.setVodList(vodList);
 
         when(parser.parse(any(Reader.class), any(Unmarshaller.Listener.class)))
             .thenReturn(response);
         when(vodEntityProcessor.getResult())
             .thenReturn(42);
         
         ttClient = new XmlTalkTalkClient(client, host, parser, 5);
         Integer result = ttClient.processVodList(GroupType.BRAND, "ABCCASTL", vodEntityProcessor);
         
         verify(vodEntityProcessor).getResult();
         verify(parser).parse(any(Reader.class), any(Unmarshaller.Listener.class));
 
         assertThat(result, is(42));
     }
 
     @Test
     public void testProcessVodListWithMultipleRequests() throws HttpException, Exception {
         
         TVDataInterfaceResponse response = new TVDataInterfaceResponse();
         VodListType vodList = new VodListType();
         vodList.setTotalEntityCount(4);
         response.setVodList(vodList);
         
         when(parser.parse(any(Reader.class), any(Unmarshaller.Listener.class)))
             .thenReturn(response);
         when(parser.parse(any(Reader.class), any(Unmarshaller.Listener.class)))
             .thenReturn(response);
         when(vodEntityProcessor.getResult())
             .thenReturn(42);
         
 
         ttClient = new XmlTalkTalkClient(client, host, parser, 3);
         
         Integer result = ttClient.processVodList(GroupType.BRAND, "ABCCASTL", vodEntityProcessor);
         
         verify(vodEntityProcessor).getResult();
         verify(parser, times(2)).parse(any(Reader.class), any(Unmarshaller.Listener.class));
         
         assertNotNull(result);
     }
 
     @Test
     public void testGetItemDetail() throws HttpException, Exception {
         
         TVDataInterfaceResponse response = new TVDataInterfaceResponse();
         ItemDetailType detail = new ItemDetailType();
         detail.setTitle("title");
         response.setItemDetail(detail);
         
         when(parser.parse(any(Reader.class), any(Unmarshaller.Listener.class)))
             .thenReturn(response);
         
         ItemDetailType fetchedDetail = ttClient.getItemDetail(GroupType.EPISODE, "397212");
         
         assertThat(fetchedDetail.getTitle(), is("title"));
     }
 
 }
