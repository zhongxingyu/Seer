 package ch.unibe.scg.cc.mappers;
 
 import java.io.IOException;
 
 import org.apache.hadoop.hbase.client.Result;
 import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
 import org.apache.hadoop.hbase.mapreduce.TableMapper;
 import org.apache.hadoop.hbase.mapreduce.TableReducer;
 import org.apache.hadoop.mapreduce.Mapper;
 
 import ch.unibe.scg.cc.modules.CCModule;
 import ch.unibe.scg.cc.modules.HBaseModule;
 import ch.unibe.scg.cc.modules.JavaModule;
 import ch.unibe.scg.cc.util.WrappedRuntimeException;
 
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 import com.google.inject.Key;
 import com.google.inject.name.Names;
 
 public class MRMain {
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	public static void main(String[] args) throws Throwable {
 		assert args.length == 1;
 		Class c = Class.forName(args[0]);
 		Injector injector = Guice.createInjector(new CCModule(), new JavaModule(), new HBaseModule());
 		Object instance = injector.getInstance(c);
 		try {
 			((Runnable) instance).run();
 		} catch(WrappedRuntimeException e) {
 			throw e.getCause();
 		}
 	}
 	
 	public static class MRMainMapper<KEYIN, VALUEIN, KEYOUT, VALUEOUT> extends Mapper<KEYIN, VALUEIN, KEYOUT, VALUEOUT> {
 		GuiceMapper<KEYIN, VALUEIN, KEYOUT, VALUEOUT> guiceMapper;
 		
 		@SuppressWarnings("unchecked")
 		@Override
 		public void setup(Context context) {
 			String clazz = context.getConfiguration().get("GuiceMapperAnnotation");
			Injector injector = Guice.createInjector(new CCModule(), new JavaModule(), new HBaseModule());
 			guiceMapper = injector.getInstance(Key.get(GuiceMapper.class, Names.named(clazz)));
 		}
 		
 		@Override
 		public void map(KEYIN key, VALUEIN value, Context context) throws IOException, InterruptedException {
 			guiceMapper.map(key, value, context);
 		}
 	}
 	
 	public static class MRMainTableMapper<KEYOUT, VALUEOUT> extends TableMapper<KEYOUT, VALUEOUT> {
 		GuiceTableMapper<KEYOUT, VALUEOUT> guiceMapper;
 		
 		@SuppressWarnings("unchecked")
 		@Override
 		public void setup(Context context) {
 			String clazz = context.getConfiguration().get("GuiceMapperAnnotation");
			Injector injector = Guice.createInjector(new CCModule(), new JavaModule(), new HBaseModule());
 			guiceMapper = injector.getInstance(Key.get(GuiceTableMapper.class, Names.named(clazz)));
 		}
 		
 		@Override
 		public void map(ImmutableBytesWritable key, Result value, Context context) throws IOException, InterruptedException {
 			guiceMapper.map(key, value, context);
 		}
 	}
 	
 	public static class MRMainTableReducer extends TableReducer<ImmutableBytesWritable, ImmutableBytesWritable, ImmutableBytesWritable> {
 		GuiceTableReducer<ImmutableBytesWritable, ImmutableBytesWritable, ImmutableBytesWritable> reducer;
 		
 		@SuppressWarnings("unchecked")
 		@Override
 		protected void setup(Context context) {
 			String clazz = context.getConfiguration().get("GuiceReducerAnnotation");
			Injector injector = Guice.createInjector(new CCModule(), new JavaModule(), new HBaseModule());
 			reducer = injector.getInstance(Key.get(GuiceTableReducer.class, Names.named(clazz)));
 		}
 		
 		@Override
 		public void reduce(ImmutableBytesWritable key, Iterable<ImmutableBytesWritable> values, Context context) throws IOException, InterruptedException {
 			reducer.reduce(key, values, context);
 		}
 	}
 }
