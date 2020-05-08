 package org.jpoetker.objstore.atmos.parser;
 
 import static org.hamcrest.CoreMatchers.*;
 import static org.junit.Assert.*;
 import static org.mockito.Mockito.*;
 
 import java.io.ByteArrayInputStream;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.StatusLine;
 import org.apache.http.message.BasicHeader;
 import org.jpoetker.objstore.Identifier;
 import org.jpoetker.objstore.ObjectInfo;
 import org.jpoetker.objstore.QueryResults;
 import org.jpoetker.objstore.atmos.AtmosResponse;
 import org.junit.Before;
 import org.junit.Test;
 
 
 public class TestQueryResponseProcessorStreamImpl {
 	private static final String objectResponseXML = "<?xml version='1.0' encoding='UTF-8'?>\n" + 
 	"<ListObjectsResponse xmlns='http://www.emc.com/cos/'>\n" + 
 	"	<Object>\n" + 
 	"		<ObjectID>4ee696e4a11f549804f0b909b09e0d04fa41c52938b1</ObjectID>\n" + 
 	"	</Object>\n" + 
 	"	<Object>\n" + 
 	"		<ObjectID>4ee696e4a11f549804f0b909b0abf204faa6a72a6873</ObjectID>\n" + 
 	"	</Object>\n" + 
 	"	<Object>\n" + 
 	"		<ObjectID>4ee696e4a11f549804f0b909b0abf204faabb905c1e9</ObjectID>\n" + 
 	"	</Object>\n" + 
 	"	<Object>\n" + 
 	"		<ObjectID>4ee696e4a21f549604f0b75393f7e004faab97c1a0d0</ObjectID>\n" + 
 	"	</Object>\n" + 
 	"	<Object>\n" + 
 	"		<ObjectID>4ee696e4a21f549604f0b75393f7e004faabb134fe0a</ObjectID>\n" + 
 	"	</Object>\n" + 
 	"	<Object>\n" + 
 	"		<ObjectID>4ee696e4a41f549804f0b909b6d8e304fa97db17e4d9</ObjectID>\n" + 
 	"	</Object>\n" + 
 	"</ListObjectsResponse>";
private static final String objectResonpseWithMetadataXML = "<?xml version='1.0' encoding='UTF-8'?>\n" + 
 	"<ListObjectsResponse xmlns='http://www.emc.com/cos/'>\n" + 
 	"	<Object>\n" + 
 	"		<ObjectID>4ee696e4a11f549804f0b909b09e0d04fa41c52938b1</ObjectID>\n" + 
 	"		<SystemMetadataList>\n" + 
 	"			<Metadata>\n" + 
 	"				<Name>atime</Name>\n" + 
 	"				<Value>2012-05-04T18:13:38Z</Value>\n" + 
 	"			</Metadata>\n" + 
 	"			<Metadata>\n" + 
 	"				<Name>mtime</Name>\n" + 
 	"				<Value>2012-05-04T18:13:38Z</Value>\n" + 
 	"			</Metadata>\n" + 
 	"			<Metadata>\n" + 
 	"				<Name>ctime</Name>\n" + 
 	"				<Value>2012-05-04T18:13:38Z</Value>\n" + 
 	"			</Metadata>\n" + 
 	"			<Metadata>\n" + 
 	"				<Name>itime</Name>\n" + 
 	"				<Value>2012-05-04T18:13:38Z</Value>\n" + 
 	"			</Metadata>\n" + 
 	"			<Metadata>\n" + 
 	"				<Name>type</Name>\n" + 
 	"				<Value>regular</Value>\n" + 
 	"			</Metadata>\n" + 
 	"			<Metadata>\n" + 
 	"				<Name>uid</Name>\n" + 
 	"				<Value>A8100507428c2141f068</Value>\n" + 
 	"			</Metadata>\n" + 
 	"			<Metadata>\n" + 
 	"				<Name>gid</Name>\n" + 
 	"				<Value>apache</Value>\n" + 
 	"			</Metadata>\n" + 
 	"			<Metadata>\n" + 
 	"				<Name>objectid</Name>\n" + 
 	"				<Value>4ee696e4a11f549804f0b909b09e0d04fa41c52938b1</Value>\n" + 
 	"			</Metadata>\n" + 
 	"			<Metadata>\n" + 
 	"				<Name>size</Name>\n" + 
 	"				<Value>169488</Value>\n" + 
 	"			</Metadata>\n" + 
 	"			<Metadata>\n" + 
 	"				<Name>nlink</Name>\n" + 
 	"				<Value>0</Value>\n" + 
 	"			</Metadata>\n" + 
 	"			<Metadata>\n" + 
 	"				<Name>policyname</Name>\n" + 
 	"				<Value>default</Value>\n" + 
 	"			</Metadata>\n" + 
 	"		</SystemMetadataList>\n" + 
 	"		<UserMetadataList>\n" + 
 	"			<Metadata>\n" + 
 	"				<Name>Encryption-Algorithm</Name>\n" + 
 	"				<Value>AES</Value>\n" + 
 	"				<Listable>false</Listable>\n" + 
 	"			</Metadata>\n" + 
 	"			<Metadata>\n" + 
 	"				<Name>File-Name</Name>\n" + 
 	"				<Value>/Users/poetker_j/Downloads/balldsi.doc</Value>\n" + 
 	"				<Listable>false</Listable>\n" + 
 	"			</Metadata>\n" + 
 	"			<Metadata>\n" + 
 	"				<Name>Test-Data</Name>\n" + 
 	"				<Value>Test-Data</Value>\n" + 
 	"				<Listable>true</Listable>\n" + 
 	"			</Metadata>\n" + 
 	"		</UserMetadataList>\n" + 
 	"	</Object>\n" + 
 	"	<Object>\n" + 
 	"		<ObjectID>4ee696e4a11f549804f0b909b0abf204faa6a72a6873</ObjectID>\n" + 
 	"		<SystemMetadataList>\n" + 
 	"			<Metadata>\n" + 
 	"				<Name>atime</Name>\n" + 
 	"				<Value>2012-05-09T13:00:35Z</Value>\n" + 
 	"			</Metadata>\n" + 
 	"			<Metadata>\n" + 
 	"				<Name>mtime</Name>\n" + 
 	"				<Value>2012-05-09T13:00:34Z</Value>\n" + 
 	"			</Metadata>\n" + 
 	"			<Metadata>\n" + 
 	"				<Name>ctime</Name>\n" + 
 	"				<Value>2012-05-09T13:00:35Z</Value>\n" + 
 	"			</Metadata>\n" + 
 	"			<Metadata>\n" + 
 	"				<Name>itime</Name>\n" + 
 	"				<Value>2012-05-09T13:00:34Z</Value>\n" + 
 	"			</Metadata>\n" + 
 	"			<Metadata>\n" + 
 	"				<Name>type</Name>\n" + 
 	"				<Value>regular</Value>\n" + 
 	"			</Metadata>\n" + 
 	"			<Metadata>\n" + 
 	"				<Name>uid</Name>\n" + 
 	"				<Value>A8100507428c2141f068</Value>\n" + 
 	"			</Metadata>\n" + 
 	"			<Metadata>\n" + 
 	"				<Name>gid</Name>\n" + 
 	"				<Value>apache</Value>\n" + 
 	"			</Metadata>\n" + 
 	"			<Metadata>\n" + 
 	"				<Name>objectid</Name>\n" + 
 	"				<Value>4ee696e4a11f549804f0b909b0abf204faa6a72a6873</Value>\n" + 
 	"			</Metadata>\n" + 
 	"			<Metadata>\n" + 
 	"				<Name>size</Name>\n" + 
 	"				<Value>1162</Value>\n" + 
 	"			</Metadata>\n" + 
 	"			<Metadata>\n" + 
 	"				<Name>nlink</Name>\n" + 
 	"				<Value>0</Value>\n" + 
 	"			</Metadata>\n" + 
 	"			<Metadata>\n" + 
 	"				<Name>policyname</Name>\n" + 
 	"				<Value>default</Value>\n" + 
 	"			</Metadata>\n" + 
 	"		</SystemMetadataList>\n" + 
 	"		<UserMetadataList>\n" + 
 	"			<Metadata>\n" + 
 	"				<Name>Test-Data</Name>\n" + 
 	"				<Value>Test</Value>\n" + 
 	"				<Listable>true</Listable>\n" + 
 	"			</Metadata>\n" + 
 	"			<Metadata>\n" + 
 	"				<Name>test-name</Name>\n" + 
 	"				<Value>test-value</Value>\n" + 
 	"				<Listable>false</Listable>\n" + 
 	"			</Metadata>\n" + 
 	"		</UserMetadataList>\n" + 
 	"	</Object>\n" + 
 	"</ListObjectsResponse>";
 
 	private QueryResponseParser processor;
 	private HttpResponse mockHttpResponse;
 
 	@Before
 	public void setUp() throws Exception {
 		processor = new QueryResponseParserStreamImpl();
 
 		StatusLine mock200 = mock(StatusLine.class);
 		when(mock200.getStatusCode()).thenReturn(new Integer(200));
 		when(mock200.getReasonPhrase()).thenReturn("OK");
 
 		mockHttpResponse = mock(HttpResponse.class);
 		when(mockHttpResponse.getStatusLine()).thenReturn(mock200);
 	}
 
 	@Test
 	public void testParseObjectIdentifiers() throws Exception {
 		HttpEntity mockEntity = mock(HttpEntity.class);
 		when(mockEntity.getContent()).thenReturn(new ByteArrayInputStream(objectResponseXML.getBytes("UTF-8")));
 
 		when(mockHttpResponse.getEntity()).thenReturn(mockEntity);
 		when(mockHttpResponse.getFirstHeader("x-emc-token")).thenReturn(new BasicHeader("x-emc-token", "tokenvalue"));
 
 		QueryResults<Identifier> ids = processor.parseObjectIdentifiers(new AtmosResponse(mockHttpResponse));
 
 		verify(mockEntity).getContent();
 		verify(mockHttpResponse).getFirstHeader("x-emc-token");
 
 		assertThat(ids.getContinuationToken(), is("tokenvalue"));
 		assertThat(ids.getResults().size(), is(6));
 		assertThat(ids.getResults().iterator().next().toString(), is("4ee696e4a11f549804f0b909b09e0d04fa41c52938b1"));
 	}
 
 	@Test
 	public void testParseObjectInfo() throws Exception {
 		HttpEntity mockEntity = mock(HttpEntity.class);
 		when(mockEntity.getContent()).thenReturn(new ByteArrayInputStream(objectResonpseWithMetadataXML.getBytes("UTF-8")));
 
 		when(mockHttpResponse.getEntity()).thenReturn(mockEntity);
 		when(mockHttpResponse.getFirstHeader("x-emc-token")).thenReturn(new BasicHeader("x-emc-token", "tokenvalue"));
 
 		QueryResults<ObjectInfo> objects = processor.parseObjectInfo(new AtmosResponse(mockHttpResponse));
 
 		verify(mockEntity).getContent();
 		verify(mockHttpResponse).getFirstHeader("x-emc-token");
 
 		assertThat(objects.getContinuationToken(), is("tokenvalue"));
 		assertThat(objects.getResults().size(), is(2));
 
 		ObjectInfo first = objects.getResults().iterator().next();
 		assertThat(first.getId().toString(), is("4ee696e4a11f549804f0b909b09e0d04fa41c52938b1"));
 		assertThat(first.getSystemMetadata().size(), is(11));
 		assertThat(first.getUserMetadata().size(), is(3));
 
 	}
 }
