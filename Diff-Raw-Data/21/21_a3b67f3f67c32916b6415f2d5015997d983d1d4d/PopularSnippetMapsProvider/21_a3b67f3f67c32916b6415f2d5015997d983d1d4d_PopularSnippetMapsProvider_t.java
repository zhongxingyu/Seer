 package ch.unibe.scg.cc.mappers;
 
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.util.Map.Entry;
 import java.util.NavigableMap;
 
 import javax.inject.Named;
 import javax.inject.Provider;
 
 import org.apache.hadoop.hbase.client.HTable;
 import org.apache.hadoop.hbase.client.Result;
 import org.apache.hadoop.hbase.client.ResultScanner;
 import org.apache.hadoop.hbase.client.Scan;
 import org.apache.hadoop.hbase.util.Bytes;
 
 import ch.unibe.scg.cc.Protos.SnippetLocation;
import ch.unibe.scg.cc.WrappedRuntimeException;
 
 import com.google.common.collect.ImmutableMultimap;
 import com.google.inject.Inject;
 import com.google.protobuf.ByteString;
 
 public class PopularSnippetMapsProvider implements Provider<PopularSnippetMaps> {
 	@Inject(optional = true)
 	@Named("popularSnippets")
 	HTable popularSnippets;
 
 	// Must be static to force sharing across all JVMs.
 	private static PopularSnippetMaps popularSnippetMaps;
 
 	@Override
 	public PopularSnippetMaps get() {
		synchronized (this.getClass()) {
 			if (popularSnippetMaps == null) {
 				Scan scan = new Scan();
 				scan.setCaching(1000);
 				// TODO play with caching. (100 is the default value)
 				scan.setCacheBlocks(false);
 				scan.addFamily(GuiceResource.FAMILY);
 				ResultScanner rs;
 				try {
 					rs = popularSnippets.getScanner(scan);
 				} catch (IOException e) {
 					throw new WrappedRuntimeException("Problem with popularSnippets occured: ", e);
 				}
				ImmutableMultimap.Builder<ByteBuffer, SnippetLocation> function2PopularSnippets = ImmutableMultimap
						.builder();
				ImmutableMultimap.Builder<ByteBuffer, SnippetLocation> snippet2PopularSnippets = ImmutableMultimap
						.builder();
 				for (Result r : rs) {
 					byte[] function = r.getRow();
 					NavigableMap<byte[], byte[]> fm = r.getFamilyMap(GuiceResource.FAMILY);
 					for (Entry<byte[], byte[]> cell : fm.entrySet()) {
 						byte[] snippet = cell.getKey();
						// To save space we didn't store a protobuffer object.
						// We create the object now with the information we
						// have.
 						SnippetLocation snippetLocation = SnippetLocation.newBuilder()
 								.setFunction(ByteString.copyFrom(function)).setSnippet(ByteString.copyFrom(snippet))
 								.setPosition(Bytes.toInt(Bytes.head(cell.getValue(), 4)))
 								.setLength(Bytes.toInt(Bytes.tail(cell.getValue(), 4))).build();
 						function2PopularSnippets.put(ByteBuffer.wrap(function), snippetLocation);
 						snippet2PopularSnippets.put(ByteBuffer.wrap(snippet), snippetLocation);
 					}
 				}
 				popularSnippetMaps = new PopularSnippetMaps(function2PopularSnippets.build(),
 						snippet2PopularSnippets.build());
 			}
 
 			return popularSnippetMaps;
 		}
 	}
 }
