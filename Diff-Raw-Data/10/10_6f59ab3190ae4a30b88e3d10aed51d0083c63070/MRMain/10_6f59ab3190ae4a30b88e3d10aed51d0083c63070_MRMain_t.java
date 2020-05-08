 package ch.unibe.scg.cc.mappers;
 
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.logging.Logger;
 
 import org.apache.hadoop.conf.Configured;
 import org.apache.hadoop.hbase.client.Result;
 import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
 import org.apache.hadoop.hbase.mapreduce.TableMapper;
 import org.apache.hadoop.hbase.mapreduce.TableReducer;
 import org.apache.hadoop.mapreduce.Mapper;
 import org.apache.hadoop.mapreduce.Reducer;
 import org.apache.hadoop.util.Tool;
 import org.apache.hadoop.util.ToolRunner;
 
 import ch.unibe.scg.cc.WrappedRuntimeException;
 import ch.unibe.scg.cc.modules.CCModule;
 import ch.unibe.scg.cc.modules.CounterModule;
 import ch.unibe.scg.cc.modules.HBaseModule;
 import ch.unibe.scg.cc.modules.JavaModule;
 
 import com.google.inject.AbstractModule;
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 
 /**
  * Executes MR-jobs. All Mappers/Reducers use this class as the Main-Class in
  * the manifest.
  * <p>
  * {@code
  * <antcall target="jar">
  * 	<param name="mainClass" value="ch.unibe.scg.cc.mappers.MRMain" />
  * 	</antcall>
  * }
  * <p>
  * Finally, the command which gets called on the server looks somewhat like
  * this:
  * <p>
  * {@code
  * hadoop jar /tmp/cc.jar ch.unibe.scg.cc.mappers.MakeSnippet2Function
  * }
  * <p>
  * Provides generic Mappers and Reducers that will create the real
  * Mapper/Reducer by using dependency injection. Which Mapper/Reducer gets
  * injected is defined in the configuration with the attribute
  * "GuiceMapperAnnotation"/"GuiceReducerAnnotation" respectively.
  */
 public class MRMain extends Configured implements Tool {
 	static Logger logger = Logger.getLogger(MRMain.class.getName());
 
 	public static void main(String[] args) throws Throwable {
		if (args.length == 0) {
			System.err.println("You must specify the job as an argument to MRMain.");
			System.exit(-1);
		}
 		logger.finer(Arrays.toString(args));
 		ToolRunner.run(new MRMain(), args);
 	}
 
 	@Override
 	public int run(String[] args) {
 		logger.finer(Arrays.toString(args));
 		assert args.length == 1;
 		Class<?> c = classForNameOrPanic(args[0]);
 		AbstractModule confModule = new AbstractModule() {
 			@Override
 			protected void configure() {
 				// bind(Configuration.class).annotatedWith(Names.named("commandLine")).toInstance(getConf());
 			}
 		};
 		Injector injector = Guice.createInjector(confModule, new CCModule(), new JavaModule(), new HBaseModule());
 		Object instance = injector.getInstance(c);
 		((Runnable) instance).run();
 		return 0;
 	}
 
 	/**
 	 * We can only pass the Mapper as a class, but we want to configure our
 	 * mapper using Guice. We'd prefer to set the mapper as an object (already
 	 * Guice configured), but Hadoop won't let us. So, this class bridges
 	 * between Guice and Hadoop. In setup, we Guice configure the real reducer,
 	 * and this class acts as a proxy to the guice-configured reducer.
	 *
 	 * <p>
 	 * All methods except
 	 * {@link #run(org.apache.hadoop.mapreduce.Mapper.Context)} are called on
 	 * the guice-configured reducer.
 	 */
 	public static class MRMainMapper<KEYIN, VALUEIN, KEYOUT, VALUEOUT> extends Mapper<KEYIN, VALUEIN, KEYOUT, VALUEOUT> {
 		GuiceMapper<KEYIN, VALUEIN, KEYOUT, VALUEOUT> guiceMapper;
 
 		@Override
 		protected void setup(final Context context) throws IOException, InterruptedException {
 			Class<?> clazz = classForNameOrPanic(context.getConfiguration().get(
 					GuiceResource.GUICE_MAPPER_ANNOTATION_STRING));
 			Injector injector = Guice.createInjector(new CCModule(), new JavaModule(), new HBaseModule(),
 					new CounterModule(context));
 
 			guiceMapper = (GuiceMapper<KEYIN, VALUEIN, KEYOUT, VALUEOUT>) injector.getInstance(clazz);
 			guiceMapper.setup(context);
 		}
 
 		@Override
 		protected void map(KEYIN key, VALUEIN value, Context context) throws IOException, InterruptedException {
 			guiceMapper.map(key, value, context);
 		}
 
 		@Override
 		protected void cleanup(Context context) throws IOException, InterruptedException {
 			guiceMapper.cleanup(context);
 		}
 
 		/**
 		 * We need to call {@link #run(Mapper.Context)} on the super class here
 		 * in order to keep control over the {@link #setup(Mapper.Context)},
 		 * {@link #map(Object, Object, Mapper.Context)} and
 		 * {@link #cleanup(Mapper.Context)} methods.
 		 */
 		@Override
 		public void run(Context context) throws IOException, InterruptedException {
 			super.run(context);
 		}
 	}
 
 	/** see {@link MRMainMapper} */
 	public static class MRMainTableMapper<KEYOUT, VALUEOUT> extends TableMapper<KEYOUT, VALUEOUT> {
 		GuiceTableMapper<KEYOUT, VALUEOUT> guiceMapper;
 
 		@Override
 		protected void setup(final Context context) throws IOException, InterruptedException {
 			Class<?> clazz = classForNameOrPanic(context.getConfiguration().get(
 					GuiceResource.GUICE_MAPPER_ANNOTATION_STRING));
 			Injector injector = Guice.createInjector(new CCModule(), new JavaModule(), new HBaseModule(),
 					new CounterModule(context));
 
 			guiceMapper = (GuiceTableMapper<KEYOUT, VALUEOUT>) injector.getInstance(clazz);
 			guiceMapper.setup(context);
 		}
 
 		@Override
 		protected void map(ImmutableBytesWritable key, Result value, Context context) throws IOException,
 				InterruptedException {
 			guiceMapper.map(key, value, context);
 		}
 
 		@Override
 		protected void cleanup(Context context) throws IOException, InterruptedException {
 			guiceMapper.cleanup(context);
 		}
 
 		@Override
 		public void run(Context context) throws IOException, InterruptedException {
 			super.run(context);
 		}
 	}
 
 	/** @see MRMainMapper */
 	public static class MRMainReducer<KEYIN, VALUEIN, KEYOUT, VALUEOUT> extends
 			Reducer<KEYIN, VALUEIN, KEYOUT, VALUEOUT> {
 		GuiceReducer<KEYIN, VALUEIN, KEYOUT, VALUEOUT> reducer;
 
 		@Override
 		protected void setup(Context context) throws IOException, InterruptedException {
 			Class<?> clazz = classForNameOrPanic(context.getConfiguration().get(
 					GuiceResource.GUICE_REDUCER_ANNOTATION_STRING));
 			Injector injector = Guice.createInjector(new CCModule(), new JavaModule(), new HBaseModule(),
 					new CounterModule(context));
 			reducer = (GuiceReducer<KEYIN, VALUEIN, KEYOUT, VALUEOUT>) injector.getInstance(clazz);
 			reducer.setup(context);
 		}
 
 		@Override
 		protected void cleanup(Context context) throws IOException, InterruptedException {
 			reducer.cleanup(context);
 		}
 
 		@Override
 		public void run(Context context) throws IOException, InterruptedException {
 			super.run(context);
 		}
 
 		@Override
 		protected void reduce(KEYIN key, Iterable<VALUEIN> values, Context context) throws IOException,
 				InterruptedException {
 			reducer.reduce(key, values, context);
 		}
 	}
 
 	/** @see MRMainMapper */
 	public static class MRMainTableReducer extends
 			TableReducer<ImmutableBytesWritable, ImmutableBytesWritable, ImmutableBytesWritable> {
 		GuiceTableReducer<ImmutableBytesWritable, ImmutableBytesWritable, ImmutableBytesWritable> reducer;
 
 		@Override
 		protected void setup(Context context) throws IOException, InterruptedException {
 			Class<?> clazz = classForNameOrPanic(context.getConfiguration().get(
 					GuiceResource.GUICE_REDUCER_ANNOTATION_STRING));
 			Injector injector = Guice.createInjector(new CCModule(), new JavaModule(), new HBaseModule(),
 					new CounterModule(context));
 			reducer = (GuiceTableReducer<ImmutableBytesWritable, ImmutableBytesWritable, ImmutableBytesWritable>) injector
 					.getInstance(clazz);
 			reducer.setup(context);
 		}
 
 		@Override
 		protected void cleanup(Context context) throws IOException, InterruptedException {
 			reducer.cleanup(context);
 		}
 
 		@Override
 		public void run(Context context) throws IOException, InterruptedException {
 			super.run(context);
 		}
 
 		@Override
 		protected void reduce(ImmutableBytesWritable key, Iterable<ImmutableBytesWritable> values, Context context)
 				throws IOException, InterruptedException {
 			reducer.reduce(key, values, context);
 		}
 	}
 
 	/**
 	 * Throws a RuntimeException if the class is not found.
	 *
 	 * @param qualifiedClassName
 	 *            the fully qualified class name
 	 * @return returns the class Object
 	 */
 	public static Class<?> classForNameOrPanic(String qualifiedClassName) {
 		try {
 			return Class.forName(qualifiedClassName);
 		} catch (ClassNotFoundException e) {
 			throw new WrappedRuntimeException("Class " + qualifiedClassName + " not found!", e);
 		}
 	}
 }
