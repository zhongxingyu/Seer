 package ch.unibe.scg.cc.mappers;
 
 import java.io.IOException;
 import java.util.Map.Entry;
 
 import javax.inject.Inject;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.hbase.HTableDescriptor;
 import org.apache.hadoop.hbase.client.HBaseAdmin;
 import org.apache.hadoop.hbase.client.HTable;
 import org.apache.hadoop.hbase.client.Scan;
 import org.apache.hadoop.hbase.mapreduce.MultithreadedTableMapper;
 import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
 import org.apache.hadoop.hbase.mapreduce.TableMapper;
 import org.apache.hadoop.hbase.mapreduce.TableReducer;
 import org.apache.hadoop.io.Writable;
 import org.apache.hadoop.io.WritableComparable;
 import org.apache.hadoop.mapreduce.Job;
 import org.apache.hadoop.mapreduce.MRJobConfig;
 import org.apache.hadoop.mapreduce.Mapper;
 import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.map.MultithreadedMapper;
 
 import ch.unibe.scg.cc.activerecord.ConfigurationProvider;
 import ch.unibe.scg.cc.mappers.MRMain.MRMainMapper;
 import ch.unibe.scg.cc.mappers.MRMain.MRMainReducer;
 import ch.unibe.scg.cc.mappers.MRMain.MRMainTableMapper;
 import ch.unibe.scg.cc.mappers.MRMain.MRMainTableReducer;
 
 import com.google.common.base.Optional;
 
 public class MRWrapper {
 	@Inject
 	private ConfigurationProvider configurationProvider;
 
 	/** truncates a table (1. disable, 2. delete, 3. recreate) */
 	public void truncate(HTable hTable) throws IOException {
 		HTableDescriptor tableDescription = hTable.getTableDescriptor();
 		String tableName = tableDescription.getNameAsString();
 		HBaseAdmin admin = new HBaseAdmin(configurationProvider.get());
 		try {
 			admin.disableTable(tableName);
 			admin.deleteTable(tableName);
 			admin.createTable(tableDescription);
 		} finally {
 			admin.close();
 		}
 	}
 
 	/**
 	 * @param config
 	 *            the config provided will be merged with the configuration of
 	 *            the configurationProvider, while the provided config has
 	 *            higher priority
 	 * @param mapperTableName
 	 *            if mapper should get HBase rows as input, set the existing
 	 *            table name
 	 * @param reducerTableName
 	 *            if reducer should write its output to a HBase table, set its
 	 *            name
 	 * @param reducerClassName
 	 *            only provide this name if there is actually a reduce step to
 	 *            be done
 	 */
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	public boolean launchMapReduceJob(String jobName, Configuration config, Optional<String> mapperTableName,
 			Optional<String> reducerTableName, Optional<Scan> tableScanner, String mapperClassName,
 			Optional<String> reducerClassName, Class<? extends WritableComparable> outputKey,
 			Class<? extends Writable> outputValue) throws IOException, ClassNotFoundException, InterruptedException {
 		Configuration merged = merge(configurationProvider.get(), config);
 		appendHighlyRecommendedPropertiesToConfiguration(merged);
 		Job job = Job.getInstance(merged, jobName);
 		job.setJarByClass(MRMain.class);
 		TableMapReduceUtil.addDependencyJars(job);
 
		Class<?> mapperClass = (mapperTableName.isPresent()) ? MultithreadedTableMapper.class
				: MultithreadedMapper.class;
 		Class<?> reducerClass = (reducerTableName.isPresent()) ? MRMainTableReducer.class : MRMainReducer.class;
 
 		// mapper configuration
 		if (isSubclassOf(mapperClass, TableMapper.class)) {
 			if (!mapperTableName.isPresent()) {
 				throw new IllegalArgumentException("mapperTableName argument is not set!");
 			}
 			if (!tableScanner.isPresent()) {
 				throw new IllegalArgumentException("tableScanner argument is not set!");
 			}
 			TableMapReduceUtil.initTableMapperJob(mapperTableName.get(), tableScanner.get(),
 					(Class<? extends TableMapper>) mapperClass, outputKey, outputValue, job);
 			MultithreadedTableMapper.setMapperClass(job, (Class) MRMainTableMapper.class);
 		} else {
 			job.setMapperClass((Class<? extends Mapper>) mapperClass);
			MultithreadedMapper.setMapperClass(job, (Class) MRMainMapper.class);
 		}
 		job.setMapOutputKeyClass(outputKey);
 		job.setMapOutputValueClass(outputValue);
 
 		// reducer configuration
 		if (reducerClassName.isPresent()) {
 			if (isSubclassOf(reducerClass, TableReducer.class)) {
 				if (!reducerTableName.isPresent()) {
 					throw new IllegalArgumentException(
 							"tableNameReducer argument is set, but reducerClass is not a subclass of the TableReducer class!");
 				}
 				TableMapReduceUtil.initTableReducerJob(reducerTableName.get(),
 						(Class<? extends TableReducer>) reducerClass, job);
 			} else {
 				job.setReducerClass((Class<? extends Reducer>) reducerClass);
 
 			}
 		}
 		if (reducerTableName.isPresent() && !reducerClassName.isPresent()) {
 			throw new IllegalArgumentException("If you set the table name, you'll have to give a reducer, too!");
 		}
 
 		// guice configuration
 		job.getConfiguration().set(GuiceResource.GUICE_MAPPER_ANNOTATION_STRING, mapperClassName);
 		if (reducerClassName.isPresent()) {
 			job.getConfiguration().set(GuiceResource.GUICE_REDUCER_ANNOTATION_STRING, reducerClassName.get());
 		}
 
 		return job.waitForCompletion(true);
 	}
 
 	private void appendHighlyRecommendedPropertiesToConfiguration(Configuration config) {
 		String mapJavaOpts = "-XX:+UseG1GC";
 		String reduceJavaOpts = "-XX:+UseG1GC";
 
 		appendProperty(config, MRJobConfig.MAP_JAVA_OPTS, mapJavaOpts);
 		appendProperty(config, MRJobConfig.REDUCE_JAVA_OPTS, reduceJavaOpts);
 	}
 
 	private void appendProperty(Configuration config, String propertyName, String propertyValue) {
 		String existingPropertyValue = config.get(propertyName);
 		if (existingPropertyValue == null) {
 			existingPropertyValue = "";
 		}
 		config.set(propertyName, (existingPropertyValue + " " + propertyValue).trim());
 	}
 
 	static boolean isSubclassOf(Class<?> subClass, Class<?> superClass) {
 		try {
 			subClass.asSubclass(superClass);
 		} catch (ClassCastException e) {
 			return false;
 		}
 		return true;
 	}
 
 	private Configuration merge(Configuration lowPriority, Configuration highPriority) {
 		Configuration merged = new Configuration(lowPriority);
 		for (Entry<String, String> entry : highPriority) {
 			merged.set(entry.getKey(), entry.getValue());
 		}
 		return merged;
 	}
 }
