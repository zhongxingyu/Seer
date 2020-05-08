 package ch.unibe.scg.cc;
 
 import static com.google.common.io.BaseEncoding.base16;
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertThat;
 
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 
 import org.junit.Test;
 
 import ch.unibe.scg.cc.Annotations.PopularSnippets;
 import ch.unibe.scg.cc.Annotations.PopularSnippetsThreshold;
 import ch.unibe.scg.cc.Protos.Clone;
 import ch.unibe.scg.cc.Protos.CloneType;
 import ch.unibe.scg.cc.Protos.GitRepo;
 import ch.unibe.scg.cc.Protos.Snippet;
 import ch.unibe.scg.cc.javaFrontend.JavaModule;
 import ch.unibe.scg.cells.Cell;
 import ch.unibe.scg.cells.CellSource;
 import ch.unibe.scg.cells.Codec;
 import ch.unibe.scg.cells.InMemoryPipeline;
 import ch.unibe.scg.cells.InMemoryShuffler;
 import ch.unibe.scg.cells.InMemoryStorage;
 import ch.unibe.scg.cells.Source;
 
 import com.google.common.collect.Iterables;
 import com.google.inject.AbstractModule;
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 import com.google.inject.Key;
 import com.google.inject.TypeLiteral;
 import com.google.inject.util.Modules;
 
 @SuppressWarnings("javadoc")
 public final class Function2RoughClonerTest {
 	@Test
 	public void testMap() throws IOException, InterruptedException {
 		Injector i = Guice.createInjector(
 				Modules.override(new CCModule(new InMemoryStorage()), new JavaModule()).with(new TestModule()));
 		Codec<GitRepo> repoCodec = i.getInstance(GitRepoCodec.class);
 		CollectionCellSource<GitRepo> src = new CollectionCellSource<>(Arrays.<Iterable<Cell<GitRepo>>> asList(Arrays
 				.asList(repoCodec.encode(GitPopulatorTest.parseZippedGit("paperExample.zip")))));
 
 		try (InMemoryShuffler<Clone> sink = i.getInstance(Key.get(new TypeLiteral<InMemoryShuffler<Clone>>() {}))) {
 			InMemoryPipeline.make(src, sink)
 			.influx(repoCodec)
 			.map(i.getInstance(GitPopulator.class))
 			.shuffle(i.getInstance(Snippet2FunctionsCodec.class))
 			.mapAndEfflux(
 					i.getInstance(Function2RoughCloner.class),
 					i.getInstance(Function2RoughClonesCodec.class));
 
 			// See paper: Table III
 			assertThat(Iterables.size(sink), is(2));
 
 			try(Source<Snippet> popularPartitions =
 					i.getInstance(Key.get(new TypeLiteral<Source<Snippet>>() {}, PopularSnippets.class))) {
 
 				Iterable<Snippet> aaa0 = null;
 				for (Iterable<Snippet> row : popularPartitions) {
 					if (base16().encode(Iterables.get(row, 0).getFunction().toByteArray()).startsWith("AAA0")) {
 						aaa0 = row;
 						break;
 					}
 				}
				assert aaa0 != null; // Null analysis insists.
 				assertNotNull(aaa0);
 
 				Set<String> snippetHashes = new HashSet<>();
 				for (Snippet s : aaa0) {
 					if (s.getCloneType() == CloneType.GAPPED) {
 						snippetHashes.add(base16().encode(s.getHash().toByteArray()));
 					}
 				}
 
 				assertThat(snippetHashes.toString(),
 						new HashSet<>(GitPopulatorTest.d618SnippetHashes()).containsAll(snippetHashes), is(true));
 			}
 		}
 		// TODO continue paper example
 	}
 
 	/** Bridge between Collections and CellSource */
 	static class CollectionCellSource<T> implements CellSource<T> {
 		final private static long serialVersionUID = 1L;
 
 		final private Iterable<Iterable<Cell<T>>> collection;
 
 		CollectionCellSource(Iterable<Iterable<Cell<T>>> collection) {
 			this.collection = collection;
 		}
 
 		@Override
 		public Iterator<Iterable<Cell<T>>> iterator() {
 			return collection.iterator();
 		}
 
 		@Override
 		public void close() throws IOException {
 			// Nothing to do.
 		}
 	}
 
 	static class TestModule extends AbstractModule {
 		@Override
 		protected void configure() {
 			bindConstant().annotatedWith(PopularSnippetsThreshold.class).to(3);
 		}
 	}
 }
