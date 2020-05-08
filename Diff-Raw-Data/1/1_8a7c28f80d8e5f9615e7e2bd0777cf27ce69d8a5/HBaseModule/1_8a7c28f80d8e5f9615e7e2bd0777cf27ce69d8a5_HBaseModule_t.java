 package ch.unibe.scg.cc.mappers;
 
 import javax.inject.Singleton;
 
 import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Scan;
 
 import ch.unibe.scg.cc.Protos.Occurrence;
 import ch.unibe.scg.cc.mappers.HTableWriteBuffer.BufferFactory;
 import ch.unibe.scg.cc.mappers.OccurrenceLoaderProvider.File2FunctionFactory;
 import ch.unibe.scg.cc.mappers.OccurrenceLoaderProvider.OccurrenceFactory;
 import ch.unibe.scg.cc.mappers.OccurrenceLoaderProvider.Project2VersionFactory;
 import ch.unibe.scg.cc.mappers.OccurrenceLoaderProvider.Version2FileFactory;
 
 import com.google.common.base.Optional;
 import com.google.common.cache.LoadingCache;
 import com.google.inject.AbstractModule;
 import com.google.inject.PrivateModule;
 import com.google.inject.TypeLiteral;
 import com.google.inject.assistedinject.FactoryModuleBuilder;
 import com.google.inject.name.Named;
 import com.google.inject.name.Names;
 
 public final class HBaseModule extends AbstractModule {
 	@Override
 	protected void configure() {
 		installHTable("project2version", Optional.<Class<? extends OccurrenceFactory>>of(Project2VersionFactory.class));
 		installHTable("version2file", Optional.<Class<? extends OccurrenceFactory>>of(Version2FileFactory.class));
 		installHTable("file2function", Optional.<Class<? extends OccurrenceFactory>>of(File2FunctionFactory.class));
 		installHTable("function2snippet", Optional.<Class<? extends OccurrenceFactory>>absent());
 		installHTable("strings", Optional.<Class<? extends OccurrenceFactory>>absent());
 
 		installHTable("snippet2function", Optional.<Class<? extends OccurrenceFactory>>absent());
 		installHTable("function2roughclones", Optional.<Class<? extends OccurrenceFactory>>absent());
 		installHTable("popularSnippets", Optional.<Class<? extends OccurrenceFactory>>absent());
 		installHTable("function2fineclones", Optional.<Class<? extends OccurrenceFactory>>absent());
 
 		installHTable("duplicateSnippetsPerFunction", Optional.<Class<? extends OccurrenceFactory>>absent());
 
 		install(new FactoryModuleBuilder().implement(HTableWriteBuffer.class, HTableWriteBuffer.class).build(
 				BufferFactory.class));
 
 		bind(new TypeLiteral<LoadingCache<byte[], String>>() {})
 			.annotatedWith(CloneLoaderProvider.CloneLoader.class)
 			.toProvider(CloneLoaderProvider.class)
 			.in(Singleton.class);
 
 		bind(Scan.class).toProvider(ScanProvider.class);
 	}
 
 	private void installHTable(final String tableName, final Optional<Class<? extends OccurrenceFactory>> factory) {
 		this.install(new HTableModule(Names.named(tableName), factory));
 	}
 
 	/**
 	 * This is a bit of a contraption to make table injection easy.
 	 * It would have been much easier to leave it at a HTableFactory
 	 * and a HTableWriteBufferFactory.
 	 *
 	 * <p>The advantage of the contraption is that -- from the client perspective,
 	 * it's actually quite nice. You can just do
 	 *
 	 * {@code @Inject @Named("popularSnippets") HTable popularSnippets; }
 	 *
 	 * and that'll work.
 	 *
 	 * <p>Without the contraption, clients needing a popularSnippetsTable would have to choose
 	 * between calling factory methods in their constructors, or all getting very
 	 * similar providers that call just one method. (That's probably what we should have done.)
 	 *
 	 * @author nes
 	 */
 	static class HTableModule extends PrivateModule {
 		final Named named;
 		final Optional<Class<? extends OccurrenceFactory>> factory;
 
 		HTableModule(Named named, Optional<Class<? extends OccurrenceFactory>> factory) {
 			this.named = named;
 			this.factory = factory;
 		}
 
 		@Override
 		protected void configure() {
 			bind(String.class).annotatedWith(Names.named("tableName")).toInstance(named.value());
 
 			bind(HTable.class).annotatedWith(named).to(HTable.class);
 			bind(HTable.class).toProvider(HTableProvider.class).in(Singleton.class);
 
 			bind(HTableWriteBuffer.class)
 					.annotatedWith(named)
 					.toProvider(HTableWriteBuffer.HTableWriteBufferProvider.class);
 
 			if (factory.isPresent()) {
 				TypeLiteral<LoadingCache<byte[], Iterable<Occurrence>>> loadingCache
 						= new TypeLiteral<LoadingCache<byte[], Iterable<Occurrence>>>() {};
 				bind(loadingCache).annotatedWith(named).toProvider(OccurrenceLoaderProvider.class);
 				bind(OccurrenceFactory.class).to(factory.get());
 				expose(loadingCache).annotatedWith(named);
 			}
 
 			expose(HTable.class).annotatedWith(named);
 			expose(HTableWriteBuffer.class).annotatedWith(named);
 
 		}
 	}
 }
