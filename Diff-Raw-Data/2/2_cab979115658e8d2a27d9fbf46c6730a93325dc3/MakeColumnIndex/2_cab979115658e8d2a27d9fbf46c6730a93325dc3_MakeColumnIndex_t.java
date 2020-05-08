 package ch.unibe.scg.cc.mappers;
 
 import java.io.IOException;
 import java.util.Map.Entry;
 import java.util.NavigableMap;
 
 import javax.inject.Inject;
 import javax.inject.Provider;
 import javax.inject.Singleton;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.hbase.client.HTable;
 import org.apache.hadoop.hbase.client.Put;
 import org.apache.hadoop.hbase.client.Result;
 import org.apache.hadoop.hbase.client.Scan;
 import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
 import org.apache.hadoop.mapreduce.MRJobConfig;
 
 import ch.unibe.scg.cc.WrappedRuntimeException;
 import ch.unibe.scg.cc.activerecord.PutFactory;
 
 import com.google.common.base.Optional;
 import com.google.inject.AbstractModule;
 import com.google.inject.Module;
 import com.google.inject.name.Named;
 import com.google.inject.name.Names;
 
 class MakeColumnIndex {
 	private static final Named TABLE_NAME_ANNOTATION = Names.named("tableName");
 
 	/** inverts mapping: sets the column key as row key and vice versa */
 	static class MakeColumnIndexMapper extends GuiceTableMapper<ImmutableBytesWritable, ImmutableBytesWritable> {
 		private static final byte[] EMPTY_BYTE_ARRAY = new byte[] {};
 		final PutFactory putFactory;
 
 		@Inject
 		MakeColumnIndexMapper(HTableWriteBuffer table, PutFactory putFactory) {
 			super(table);
 			this.putFactory = putFactory;
 		}
 
 		@Override
 		public void map(ImmutableBytesWritable key, Result value, Context context) throws IOException,
 				InterruptedException {
 			NavigableMap<byte[], byte[]> familyMap = value.getFamilyMap(Constants.FAMILY);
 			for (Entry<byte[], byte[]> column : familyMap.entrySet()) {
 				context.write(new ImmutableBytesWritable(column.getKey()), key);
 			}
 		}
 	}
 
 	static class MakeColumnIndexReducer extends
 			GuiceTableReducer<ImmutableBytesWritable, ImmutableBytesWritable, ImmutableBytesWritable> {
 		private static final byte[] EMPTY_BYTE_ARRAY = new byte[] {};
 		final PutFactory putFactory;
 
 		@Inject
 		public MakeColumnIndexReducer(HTableWriteBuffer table, PutFactory putFactory) {
 			super(table);
 			this.putFactory = putFactory;
 		}
 
 		@Override
 		public void reduce(ImmutableBytesWritable key, Iterable<ImmutableBytesWritable> values, Context context)
 				throws IOException, InterruptedException {
 			Put put = putFactory.create(key.get());
 			for (ImmutableBytesWritable value : values) {
 				put.add(Constants.INDEX_FAMILY, value.get(), 0l, EMPTY_BYTE_ARRAY);
 			}
 			context.write(key, put);
 		}
 	}
 
 	static class Project2Version implements Runnable {
 		public static final String TABLE_NAME = "project2version";
 		final Launcher launcher;
 
 		@Inject
 		Project2Version(Launcher abstractIndexer) {
 			this.launcher = abstractIndexer;
 		}
 
 		static class Project2VersionModule extends AbstractModule {
 			@Override
 			public void configure() {
 				bind(String.class).annotatedWith(TABLE_NAME_ANNOTATION).toInstance(TABLE_NAME);
 				install(new IndexingModule());
 			}
 		}
 
 		@Override
 		public void run() {
 			launcher.launch(TABLE_NAME, Project2VersionModule.class);
 		}
 	}
 
 	static class Version2File implements Runnable {
 		public static final String TABLE_NAME = "version2file";
 		final Launcher launcher;
 
 		@Inject
 		Version2File(Launcher abstractIndexer) {
 			this.launcher = abstractIndexer;
 		}
 
 		static class Version2FileModule extends AbstractModule {
 			@Override
 			public void configure() {
 				bind(String.class).annotatedWith(TABLE_NAME_ANNOTATION).toInstance(TABLE_NAME);
 				install(new IndexingModule());
 			}
 		}
 
 		@Override
 		public void run() {
 			launcher.launch(TABLE_NAME, Version2FileModule.class);
 		}
 	}
 
 	static class File2Function implements Runnable {
 		public static final String TABLE_NAME = "file2function";
 		final Launcher launcher;
 
 		@Inject
 		File2Function(Launcher abstractIndexer) {
 			this.launcher = abstractIndexer;
 		}
 
 		static class File2FunctionModule extends AbstractModule {
 			@Override
 			public void configure() {
 				bind(String.class).annotatedWith(TABLE_NAME_ANNOTATION).toInstance(TABLE_NAME);
 				install(new IndexingModule());
 			}
 		}
 
 		@Override
 		public void run() {
 			launcher.launch(TABLE_NAME, File2FunctionModule.class);
 		}
 	}
 
 	static class Function2Snippet implements Runnable {
 		public static final String TABLE_NAME = "function2snippet";
 		final Launcher launcher;
 
 		@Inject
 		Function2Snippet(Launcher abstractIndexer) {
 			this.launcher = abstractIndexer;
 		}
 
 		static class Function2SnippetModule extends AbstractModule {
 			@Override
 			public void configure() {
 				bind(String.class).annotatedWith(TABLE_NAME_ANNOTATION).toInstance(TABLE_NAME);
 				install(new IndexingModule());
 			}
 		}
 
 		@Override
 		public void run() {
 			launcher.launch(TABLE_NAME, Function2SnippetModule.class);
 		}
 	}
 
	static class Launcher {
 		final MRWrapper mrWrapper;
 		final Provider<Scan> scanProvider;
 
 		@Inject
 		Launcher(MRWrapper mrWrapper, Provider<Scan> scanProvider) {
 			this.mrWrapper = mrWrapper;
 			this.scanProvider = scanProvider;
 		}
 
 		void launch(String tableName, Class<? extends Module> module) {
 			launchMapReduceJob(mrWrapper, scanProvider, module, tableName);
 		}
 	}
 
 	static class IndexingModule extends AbstractModule {
 		@Override
 		public void configure() {
 			bind(HTable.class).toProvider(HTableProvider.class).in(Singleton.class);
 			bind(HTableWriteBuffer.class).toProvider(HTableWriteBuffer.HTableWriteBufferProvider.class);
 		}
 	}
 
 	static void launchMapReduceJob(MRWrapper mrWrapper, Provider<Scan> scanProvider,
 			Class<? extends Module> customModuleClass, String tableName) {
 		try {
 			Configuration config = new Configuration();
 			config.set(MRJobConfig.NUM_REDUCES, "30");
 			config.set(MRJobConfig.REDUCE_MERGE_INMEM_THRESHOLD, "0");
 			config.set(MRJobConfig.REDUCE_MEMTOMEM_ENABLED, "true");
 			config.set(MRJobConfig.IO_SORT_MB, "256");
 			config.set(MRJobConfig.IO_SORT_FACTOR, "100");
 			config.set(MRJobConfig.JOB_UBERTASK_ENABLE, "true");
 			config.set(MRJobConfig.TASK_TIMEOUT, "86400000");
 			config.setInt(MRJobConfig.MAP_MEMORY_MB, 1536);
 			config.set(MRJobConfig.MAP_JAVA_OPTS, "-Xmx1024M");
 			config.setInt(MRJobConfig.REDUCE_MEMORY_MB, 3072);
 			config.set(MRJobConfig.REDUCE_JAVA_OPTS, "-Xmx2560M");
 			config.set(Constants.GUICE_CUSTOM_MODULE_ANNOTATION_STRING, customModuleClass.getName());
 
 			Scan scan = scanProvider.get();
 			scan.addFamily(Constants.FAMILY);
 
 			mrWrapper.launchMapReduceJob(MakeColumnIndex.class.getName() + "Job", config, Optional.of(tableName),
 					Optional.of(tableName), Optional.of(scan), MakeColumnIndexMapper.class.getName(),
 					Optional.of(MakeColumnIndexReducer.class.getName()), ImmutableBytesWritable.class,
 					ImmutableBytesWritable.class);
 		} catch (IOException | ClassNotFoundException e) {
 			throw new WrappedRuntimeException(e);
 		} catch (InterruptedException e) {
 			Thread.currentThread().interrupt();
 			return; // Exit.
 		}
 	}
 }
