 package org.apache.mahout.clustering.elkan;
 
 import static org.apache.mahout.clustering.iterator.ClusterIterator.PRIOR_PATH_KEY;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.FileStatus;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.fs.PathFilter;
 import org.apache.hadoop.io.IntWritable;
 import org.apache.hadoop.io.LongWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.io.WritableComparable;
 import org.apache.hadoop.mapred.JobConf;
 import org.apache.hadoop.mapred.lib.MultipleOutputFormat;
 import org.apache.hadoop.mapreduce.Job;
 import org.apache.hadoop.mapreduce.Mapper;
 import org.apache.hadoop.mapreduce.Reducer;
 import org.apache.hadoop.mapreduce.Mapper.Context;
 import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
 import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
 import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
 import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;
 import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
 import org.apache.mahout.clustering.Cluster;
 import org.apache.mahout.clustering.classify.ClusterClassifier;
import org.apache.mahout.clustering.elkan.ElkanClusterDistanceStepJob.ElkanClusterDistanceMapper;
 import org.apache.mahout.clustering.iterator.CIReducer;
 import org.apache.mahout.clustering.iterator.ClusterIterator;
 import org.apache.mahout.clustering.iterator.ClusterWritable;
 import org.apache.mahout.clustering.iterator.ClusteringPolicy;
 import org.apache.mahout.clustering.spectral.common.VectorCache;
 import org.apache.mahout.common.distance.DistanceMeasure;
 import org.apache.mahout.common.iterator.sequencefile.ElkanVectorFilter;
 import org.apache.mahout.common.iterator.sequencefile.PathFilters;
 import org.apache.mahout.common.iterator.sequencefile.SequenceFileValueIterator;
 import org.apache.mahout.math.DenseVector;
 import org.apache.mahout.math.Vector;
 import org.apache.mahout.math.VectorWritable;
 import org.apache.mahout.math.Vector.Element;
 import org.apache.mahout.math.map.OpenHashMap;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.io.Closeables;
 
 public class ElkanIterator {
 	public static final String NEW_PRIOR_PATH_KEY = "org.apache.mahout.clustering.prior.path_new";
 	public static final String CENTROID_DISTANCES_PATH_KEY = "org.apache.mahout.clustering.centroid.distances.path_new";
 	public static final String ITERATION_KEY = "org.apache.mahout.clustering.iteration";
 	private static final Logger log = LoggerFactory
 			.getLogger(ElkanIterator.class);
 
 	/**
 	 * Iterate over data using a prior-trained ElkanClassifier, for a number
 	 * of iterations using a mapreduce implementation
 	 * 
 	 * @param conf
 	 *            the Configuration
 	 * @param inPath
 	 *            a Path to input VectorWritables
 	 * @param priorPath
 	 *            a Path to the prior classifier
 	 * @param outPath
 	 *            a Path of output directory
 	 * @param numIterations
 	 *            the int number of iterations to perform
 	 * @param measure 
 	 * @param namedVectors 
 	 */
 	public void iterateMR(Configuration conf, Path inPath, Path priorPath,
 			Path outPath, int numIterations, DistanceMeasure measure, int numClusters, boolean namedVectors) throws IOException,
 			InterruptedException, ClassNotFoundException {
 		conf.set(ElkanClassifier.NUM_CLUSTERS,numClusters+"");
 		ClusteringPolicy policy = ClusterClassifier.readPolicy(priorPath);
 		Path clustersOut = null;
 		int iteration = 1;
 		while (iteration <= numIterations) {
 			conf.set(PRIOR_PATH_KEY, priorPath.toString());
 			
 			String distancesPath=ElkanClassifier.CLUSTER_DISTANCE_PATH+iteration;			
 			Path cacheOutput=new Path(outPath,distancesPath);
 			conf.set(ElkanClassifier.CLUSTER_DISTANCE_KEY, cacheOutput.toString());
 			
 			writeClusterDistances(conf, priorPath, cacheOutput, measure,
 					numClusters, iteration);
 			
 			Job job = new Job(conf, "Elkan Kmeans job - Iteration "+iteration);
 			
 			if (iteration == 1)
 				job.setMapperClass(ElkanVectorOneTimeMapper.class);
 			else
 				job.setMapperClass(ElkanVectorUpdateMapper.class);
 			job.setMapOutputKeyClass(IntWritable.class);
 			job.setMapOutputValueClass(ClusterWritable.class);
 
 			job.setReducerClass(ElkanReducer.class);
 			job.setOutputKeyClass(IntWritable.class);
 			job.setOutputValueClass(ClusterWritable.class);
 
 			job.setInputFormatClass(SequenceFileInputFormat.class);
 			job.setOutputFormatClass(SequenceFileOutputFormat.class);		
 			
 			clustersOut = new Path(outPath, Cluster.CLUSTERS_DIR + iteration);
 			priorPath = clustersOut;
 			FileOutputFormat.setOutputPath(job, clustersOut);
 			if (namedVectors)
 				MultipleOutputs.addNamedOutput(job, ElkanVectorFilter.ELKAN_VECTOR_PREFIX,
 					SequenceFileOutputFormat.class, Text.class,
 					ElkanVectorWritable.class);
 			else
 				MultipleOutputs.addNamedOutput(job, ElkanVectorFilter.ELKAN_VECTOR_PREFIX,
 						SequenceFileOutputFormat.class, LongWritable.class,
 						ElkanVectorWritable.class);
 			
 			FileInputFormat.addInputPath(job, inPath);
 			if (iteration>1)
 				FileInputFormat.setInputPathFilter(job, ElkanVectorFilter.class);			
 			
 			job.setJarByClass(ElkanIterator.class);			
 			
 			if (!job.waitForCompletion(true)) {
 				throw new InterruptedException("Cluster Iteration " + iteration
 						+ " failed processing " + priorPath);
 			}
 			
 			ClusterClassifier.writePolicy(policy, clustersOut);
 			inPath=clustersOut;
 			conf.set(NEW_PRIOR_PATH_KEY, clustersOut.toString());
 			FileSystem fs = FileSystem.get(outPath.toUri(), conf);
 			iteration++;
 			if (isConverged(clustersOut, conf, fs)) {
 				log.info("Elkan Clustering converged");
 				break;
 			}
 		}
 
 	}
 
 	private void writeClusterDistances(Configuration conf, Path priorPath,
 			Path cachePath, DistanceMeasure measure, int numClusters,
 			int iteration) throws IOException {
 		ElkanClassifier classifier = new ElkanClassifier();
 		classifier.readFromSeqFiles(conf, priorPath);
 		
 		ElkanClusterDistancesCache vectorCache=new ElkanClusterDistancesCache(numClusters,conf,cachePath);
 		int clusterIndex=0;
 		Vector halfClusterDistances=new DenseVector(classifier.getModels().size());
 		for (Cluster model : classifier.getModels()) {
 			Vector distances = new DenseVector(classifier.getModels().size());
 			int colIndex = 0;
 			for (Cluster oth_model : classifier.getModels()) {
 				distances.setQuick(colIndex, measure.distance(oth_model.getCenter(),model.getCenter()));
 				colIndex++;
 			}
 			double sCenter = distances.minValue() / 2;
 			halfClusterDistances.setQuick(clusterIndex, sCenter);
 			vectorCache.save(new IntWritable(clusterIndex), distances);				
 			clusterIndex++;
 		}
 		vectorCache.saveHalfDistancesVector(halfClusterDistances);
 		if (iteration>1)
 		{
 			String newPriorClustersPath = conf
 					.get(ElkanIterator.NEW_PRIOR_PATH_KEY);
 
 			List<Cluster> newModels = classifier.readNewModelsFromSeqFiles(conf,
 					new Path(newPriorClustersPath));
 			List<Cluster> oldModels = classifier.getModels();
 			
 			Cluster[] newModelsArray=newModels.toArray(new Cluster[newModels.size()]);
 			newModels.clear();		
 			
 			Cluster[] oldModelsArray=oldModels.toArray(new Cluster[oldModels.size()]);
 			oldModels.clear();
 			
 			Vector oldClusterDistances = new DenseVector(newModelsArray.length);
 			for (int i = 0; i < newModels.size(); i++) {
 				Vector oldCenter = oldModelsArray[i].getCenter();
 				Vector newCenter = newModelsArray[i].getCenter();
 				oldClusterDistances.setQuick(i,
 						measure.distance(oldCenter, newCenter));
 			}
 			vectorCache.saveOldClusterDistances(oldClusterDistances);
 		}
 	}
 
 	/**
 	 * Return if all of the Clusters in the parts in the filePath have converged
 	 * or not
 	 * 
 	 * @param filePath
 	 *            the file path to the single file containing the clusters
 	 * @return true if all Clusters are converged
 	 * @throws IOException
 	 *             if there was an IO error
 	 */
 	private boolean isConverged(Path filePath, Configuration conf, FileSystem fs)
 			throws IOException {
 		
 		for (FileStatus part : fs
 				.listStatus(filePath, PathFilters.partFilter())) {
 			SequenceFileValueIterator<ClusterWritable> iterator = new SequenceFileValueIterator<ClusterWritable>(
 					part.getPath(), true, conf);
 			while (iterator.hasNext()) {
 				ClusterWritable value = iterator.next();
 				if (!value.getValue().isConverged()) {					
 					Closeables.closeQuietly(iterator);
 					return false;					
 				}
 			}
 		}
 		
 		return true;
 	}
 
 }
