 package ch.unibe.scg.cc;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 
 import java.nio.ByteBuffer;
 
 import org.junit.Test;
 
 import ch.unibe.scg.cc.Protos.SnippetLocation;
 import ch.unibe.scg.cc.Protos.SnippetMatch;
import ch.unibe.scg.cc.mappers.PopularSnippetMapsProvider;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableListMultimap;
 import com.google.protobuf.ByteString;
 
 /** Test {@link CloneExpander} */
 public final class CloneExpanderTest {
 	/** Test {@link CloneExpander#expandClones} */
 	@Test
 	public void testExpandClones() {
 		// Construct something like h1fun2@3 h2fun2@5 h3fun3@1 h4fun3@7
 		SnippetLocation thisFunction = SnippetLocation.newBuilder().setFunction(ByteString.copyFromUtf8("fun1")).build();
 		ImmutableList<SnippetMatch> toExpand = ImmutableList.of(
 				SnippetMatch.newBuilder()
 					.setThisSnippetLocation(SnippetLocation.newBuilder(thisFunction)
 							.setSnippet(ByteString.copyFromUtf8("h1"))
 							.setPosition(2))
 					.setThatSnippetLocation(SnippetLocation.newBuilder()
 							.setFunction(ByteString.copyFromUtf8("fun2"))
 							.setSnippet(ByteString.copyFromUtf8("h1"))
 							.setPosition(2))
 					.build(),
 
 					SnippetMatch.newBuilder()
 					.setThisSnippetLocation(SnippetLocation.newBuilder(thisFunction)
 							.setSnippet(ByteString.copyFromUtf8("h2"))
 							.setPosition(5))
 					.setThatSnippetLocation(SnippetLocation.newBuilder()
 							.setFunction(ByteString.copyFromUtf8("fun2"))
 							.setSnippet(ByteString.copyFromUtf8("h2"))
 							.setPosition(5))
 					.build(),
 
 					SnippetMatch.newBuilder()
 					.setThisSnippetLocation(SnippetLocation.newBuilder(thisFunction)
 							.setSnippet(ByteString.copyFromUtf8("h3"))
 							.setPosition(1))
 					.setThatSnippetLocation(SnippetLocation.newBuilder()
 							.setFunction(ByteString.copyFromUtf8("fun3"))
 							.setSnippet(ByteString.copyFromUtf8("h3"))
 							.setPosition(1))
 					.build(),
 
 					SnippetMatch.newBuilder()
 					.setThisSnippetLocation(SnippetLocation.newBuilder(thisFunction)
 							.setSnippet(ByteString.copyFromUtf8("h4"))
 							.setPosition(7))
 					.setThatSnippetLocation(SnippetLocation.newBuilder()
 							.setFunction(ByteString.copyFromUtf8("fun3"))
 							.setSnippet(ByteString.copyFromUtf8("h1"))
 							.setPosition(7))
 					.build());
 
 		// Construct maps for one popular snippet h7 that occurs in two places: fun2@6, fun1@2
 		final SnippetLocation fun2Loc = SnippetLocation.newBuilder()
 				.setSnippet(ByteString.copyFromUtf8("h7"))
 				.setPosition(6)
 				.setFunction(ByteString.copyFromUtf8("fun2")).build();
 		final SnippetLocation fun1Loc = SnippetLocation.newBuilder()
 				.setSnippet(ByteString.copyFromUtf8("h7"))
 				.setPosition(1)
 				.setFunction(ByteString.copyFromUtf8("fun1")).build();
 		final ImmutableListMultimap<ByteBuffer, SnippetLocation> snippet2Popular = ImmutableListMultimap.of(
 				fun1Loc.getSnippet().asReadOnlyByteBuffer(), fun1Loc,
 				fun2Loc.getSnippet().asReadOnlyByteBuffer(), fun2Loc);
 		final ImmutableListMultimap<ByteBuffer, SnippetLocation> function2Popular = ImmutableListMultimap.of(
 				fun1Loc.getFunction().asReadOnlyByteBuffer(), fun1Loc,
 				fun2Loc.getFunction().asReadOnlyByteBuffer(), fun2Loc);
 
		CloneExpander expander = new CloneExpander(new PopularSnippetMapsProvider() {
 			@Override
 			public PopularSnippetMaps get() {
 				return new PopularSnippetMaps(function2Popular, snippet2Popular);
 			}
 		}, new SnippetMatchComparator());
 		assertThat(expander.expandClones(toExpand).toString(), is(
 				"[this_function: \"fun1\"\nthat_function: \"fun2\"\n" +
 				"this_from_position: 1\nthat_from_position: 2\nthis_length: 9\nthat_length: 8\n, " +
 				"this_function: \"fun1\"\nthat_function: \"fun3\"\n" +
 				"this_from_position: 1\nthat_from_position: 1\nthis_length: 11\nthat_length: 11\n]"));
 		}
 }
