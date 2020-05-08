 package org.apache.mahout.clustering.elkan;
 
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.IntWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapreduce.Mapper;
 import org.apache.hadoop.mapreduce.Mapper.Context;
 import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;
 import org.apache.mahout.clustering.Cluster;
 import org.apache.mahout.clustering.classify.ClusterClassifier;
 import org.apache.mahout.clustering.iterator.ClusterIterator;
 import org.apache.mahout.clustering.iterator.ClusterWritable;
 import org.apache.mahout.clustering.iterator.ClusteringPolicy;
 import org.apache.mahout.clustering.kmeans.KMeansConfigKeys;
 import org.apache.mahout.common.ClassUtils;
 import org.apache.mahout.common.distance.DistanceMeasure;
 import org.apache.mahout.math.DenseVector;
 import org.apache.mahout.math.MatrixSlice;
 import org.apache.mahout.math.Vector;
 import org.apache.mahout.math.VectorWritable;
 import org.apache.mahout.math.hadoop.DistributedRowMatrix;
 import org.apache.mahout.math.map.OpenHashMap;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public abstract class ElkanMapper<KEYIN, VALUEIN> extends
 		Mapper<KEYIN, VALUEIN, IntWritable, ClusterWritable> {
 
 	protected ElkanClassifier classifier;
 	protected ClusteringPolicy policy;
 	protected Vector halfClusterDistances;
 	protected DistanceMeasure measure;
 	protected MultipleOutputs<Text, ElkanVectorWritable> m_multiOutputs;
 	protected ElkanClusterDistancesCache vectorCache;
 	private static final Logger log = LoggerFactory
 			.getLogger(ElkanMapper.class);
 
 	@Override
 	protected void setup(Context context) throws IOException,
 			InterruptedException {
 		Configuration conf = context.getConfiguration();
 		String measureClass = conf.get(KMeansConfigKeys.DISTANCE_MEASURE_KEY);
 		measure = ClassUtils.instantiateAs(measureClass, DistanceMeasure.class);
 		
 		String clusterDistancePath=conf.get(ElkanClassifier.CLUSTER_DISTANCE_KEY);
		log.info("Current distances key at Mapper "+clusterDistancePath);
 		
 		String numClustersValue=conf.get(ElkanClassifier.NUM_CLUSTERS);
		log.info("num clusters key at Mapper "+numClustersValue);
 		int numClusters=Integer.parseInt(numClustersValue);
 		
 		Path distancesPath=new Path(clusterDistancePath);
 		vectorCache=new ElkanClusterDistancesCache(numClusters,conf,distancesPath);
 		vectorCache.preload();
 		
 		String priorClustersPath = conf.get(ClusterIterator.PRIOR_PATH_KEY);
 		classifier = new ElkanClassifier();
 		classifier.readFromSeqFiles(conf, new Path(priorClustersPath));
 		policy = classifier.getPolicy();
 		policy.update(classifier);
 		halfClusterDistances=vectorCache.getHalfClusterDistances();		
 		m_multiOutputs = new MultipleOutputs(context);
 	}
 
 	@Override
 	protected void cleanup(org.apache.hadoop.mapreduce.Mapper.Context context)
 			throws IOException, InterruptedException {
 		List<Cluster> clusters = classifier.getModels();
 		ClusterWritable cw = new ClusterWritable();
 		for (int index = 0; index < clusters.size(); index++) {
 			cw.setValue(clusters.get(index));
 			context.write(new IntWritable(index), cw);
 		}
 		m_multiOutputs.close();
 		halfClusterDistances=null;
 		vectorCache.cleanup();
 		vectorCache=null;
 		super.cleanup(context);
 	}
 
 }
