 package com.ml.task;
 
 import java.io.File;
import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.io.FileUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.data.mongodb.core.query.Criteria;
 import org.springframework.data.mongodb.core.query.Query;
 
 import com.ml.db.MongoDB;
 import com.ml.model.Category;
 import com.ml.model.Cluster;
 import com.ml.model.News;
 import com.ml.util.CompressUtils;
 import com.ml.util.Constants;
 import com.ml.util.SSHUtil;
 import com.ml.util.ShortUrlUtil;
 
 
 public class HadoopClusterTask implements Runnable {
 	private static final Logger logger = LoggerFactory.getLogger(HadoopClusterTask.class);
 
 	private MongoDB mongodb;
 
 	public HadoopClusterTask(MongoDB mongodb) {
 		this.mongodb = mongodb;
 	}
 	
 	@Override
 	public void run() {
 		Query query = new Query();
 		query.addCriteria(Criteria.where("clusterId").is(null));
 		List<News> newsList = mongodb.find(query, News.class, Constants.newsCollectionName);
 		logger.info("uncluster size: " + newsList.size());
 		//uncluster size > 1000, then do cluster
 		if(newsList.size() > Constants.minClusterNum)
 			clusterAll();
 		logger.info("End of all clustering");
 		System.out.println();
 
 	}
 	
 	public void clusterAll() {
 		List<Category> categoryList = mongodb.findAll(Category.class, Constants.categoryCollectionName);
 		for(Category category: categoryList) {
 			logger.info("begin to cluster: " + category.getId());
 			clusterByCategoryId(category.getId());
 			logger.info("End of cluster: " + category.getId());
 		}
 	}
 	
 	public void clusterByCategoryId(String categoryId) {
 		//1, get category news
 		Query query = new Query();
 		query.addCriteria(Criteria.where("categoryId").is(categoryId));
 		List<News> newsList = mongodb.find(query, News.class, Constants.newsCollectionName);
 		
 		if(newsList.size() == 0)
 			return ;
 		
 		//2, cluster news
 		String resultFile = doCluster(newsList);
 		//System.out.println("result file: " + resultFile);
 		
 		//3, parse result
 		Map<Integer, List<String>> resultMap = resolveResult(resultFile);
 		//System.out.println("result map: " + resultMap.toString());
 
 		//4, first drop cluster collection
 		mongodb.delete(query, Constants.clusterCollectionName);
 		
 		//5, key: cluster id, value: cluster items, news url
 		long stamp = System.currentTimeMillis();
 		for(Integer key: resultMap.keySet()) {
 			String clusterId = stamp + "_" + key;
 			List<String> clusterNewsUrlList = resultMap.get(key);
 			
 			//set news' cluster id
 			for(String url: clusterNewsUrlList) {
 				News news = this.getNewsByShortUrl(newsList, url);
 				news.setClusterId(clusterId);
 			}
 			
 			//save cluster
 			Cluster cluster = new Cluster(clusterId, categoryId, clusterNewsUrlList.size());
 			mongodb.save(cluster, Constants.clusterCollectionName);
 			
 			//System.out.println(key + ": " + clusterNewsUrlList.size());
 		}
 		
 		//6, save updated news
 		mongodb.delete(query, Constants.newsCollectionName);
 		mongodb.insert(newsList, Constants.newsCollectionName);
 	}
 	
 	private News getNewsByShortUrl(List<News> newsList, String url) {
     	for(News news: newsList) {
     		String shortUrl = ShortUrlUtil.shortText(news.getUrl())[0] + Constants.fileExt;
     		if(url.equals(shortUrl)) {
     			return news;
     		}
     	}
     	return null;
     }
    
 	
 	private String doCluster(List<News> newsList) {
 		String destClusterResult = null;
 		try {
 			long start = System.currentTimeMillis();
 			
 			long taskId = System.currentTimeMillis();
 			logger.info("taskId：" + taskId + ", news size: " + newsList.size());
 			
 			String newFileName = Constants.newFileName + taskId;
 			String newFileSeq = Constants.newFileSeq + taskId;
 			String newFileVectors = Constants.newFileVectors + taskId;
 			String clusterResult = "cdump.txt" + taskId;
 	
 			int numOfClusters = (newsList.size() / Constants.clusterKvalue) == 0 ? 1: (newsList.size() / Constants.clusterKvalue);
 			String centroid = "kmeans-centroids" + taskId;
 			String clusters = "kmeans-clusters" + taskId;
 			
 			String destCompressFilePath = this.compressContent(newsList, taskId, newFileName);
 			
 			SSHUtil ssh = new SSHUtil();
 			ssh.sshLogin(Constants.sshIP, Constants.sshUser, Constants.sshPass);
 			
 			// 1) upload zip file
 			ssh.scpUploadFile(Constants.defaultUploadDir, destCompressFilePath, destCompressFilePath);
 			
 			// 2) generate scripts
 			String scriptPath = this.generateClusterScripts(taskId, destCompressFilePath,
 					newFileName, newFileSeq, newFileVectors, clusterResult,
 					numOfClusters, centroid, clusters);
 			ssh.scpUploadFile(Constants.defaultUploadDir, scriptPath, scriptPath);
 			
 			// 3) exec script and get the result
 			destClusterResult = Constants.tmpFileDir + clusterResult;
 			ssh.sshExec("chmod a+x " + Constants.defaultUploadDir + scriptPath + "; nohup sh " + Constants.defaultUploadDir + scriptPath);
 			ssh.scpDownloadFile(Constants.defaultUploadDir, clusterResult, destClusterResult);
 			
 			// 4) do clean work
 			String cleanScript = this.generateCleanScripts(destCompressFilePath, scriptPath, 
 					newFileName, newFileSeq, newFileVectors, clusterResult,
 					centroid, clusters);
 			
 			ssh.sshExec(cleanScript);
 			ssh.sshExit();
 			
 			long end = System.currentTimeMillis();
 			long time = (end - start)/1000;
 			logger.info("taskId：" + taskId + ", 耗时：" + time);
 			
 			FileUtils.forceDelete(new File(newFileName)); 
 			FileUtils.forceDelete(new File(destCompressFilePath)); 
			FileUtils.forceDelete(new File(scriptPath)); 
 			
			return destClusterResult;
			
		} catch (FileNotFoundException e) {
			//ignore it, for if code run in jar mode, file could not found
			return destClusterResult;
 		} catch(Exception e) {
 			e.printStackTrace();
 		}
		return null;
 	}
 	
 	private Map<Integer, List<String>> resolveResult(String resultFile) {
 		//Key: 25: Value: 1.0: /63M7F3.txt = []
 		try {
 			List<String> lines = FileUtils.readLines(new File(resultFile), Constants.defaultFileEncoding);
 			Map<Integer, List<String>> resultMap = new HashMap<Integer, List<String>>(lines.size());
 			for(String line: lines) {
 				String[] fields = line.split(": ");
 				if(fields.length < 5)
 					continue;
 				int key = Integer.parseInt(fields[1]);
 				String value = fields[4].substring(1, fields[4].indexOf(" = "));
 				
 				List<String> valueList = resultMap.get(key);
 				if(valueList == null) {
 					valueList = new ArrayList<String>();
 				}
 				valueList.add(value);
 				resultMap.put(key, valueList);
 			}
 			FileUtils.forceDelete(new File(resultFile)); 
 			return resultMap;
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	private String compressContent(List<News> newsList, long taskId, String newFileName) throws Exception {
 		//1, mkdir
 		File dir = new File(newFileName);
 		if( dir.exists() ) {
 			FileUtils.forceDelete(dir); 
 		}
 		dir.mkdir();
 		
 		//2, write file
 		for(News news: newsList) {
 			String filePath = dir.getPath() + File.separator + ShortUrlUtil.shortText(news.getUrl())[0] + Constants.fileExt;
 			FileUtils.writeStringToFile(new File(filePath), news.getContent(), Constants.defaultFileEncoding);
 		}
 		
 		//3, compress
 		String destCompressFilePath = newFileName + Constants.zipFileExt;
 		CompressUtils.compress(newFileName, destCompressFilePath);
 		
 		return destCompressFilePath;
 	}
 	
 	private String generateClusterScripts(long taskId, String destCompressFilePath, 
 			String newFileName, String newFileSeq, String newFileVectors, String clusterResult,
 			int numOfClusters, String centroid, String clusters) throws IOException {
 		// 2) generate scripts
 		String newFileTFIDFVectors = newFileVectors + "/" + Constants.newFileTFIDFVectors;
 		//String clustersFinal = clusters + "/clusters-*-final";
 		String clusteredPoints = clusters + "/clusteredPoints";
 		
 		StringBuffer scriptSB = new StringBuffer();
 		scriptSB.append("unzip " + Constants.defaultUploadDir + destCompressFilePath + " -d " + Constants.defaultUploadDir + Constants.scriptSeparator);
 		scriptSB.append("hadoop fs -put " + Constants.defaultUploadDir + newFileName + " ." + Constants.scriptSeparator);
 		scriptSB.append("mahout seqdirectory -ow -i " + newFileName + " -o " + newFileSeq + Constants.scriptSeparator);
 		scriptSB.append("mvector -lnorm -nv -ow -wt " + Constants.vectorWeight + " -a " + Constants.vectorTokenAnalyzer
 				+ " -i " + newFileSeq + " -o " + newFileVectors
 				+ " -dp " + Constants.corpusFileDictionaryFile + " -dfp " + Constants.corpusFileFrequencyFile
 				+ " -tfvp " + Constants.corpusFileTFVectors + Constants.scriptSeparator);
 		scriptSB.append("mahout kmeans -cl -ow -i " + newFileTFIDFVectors + " -c " + centroid + " -o " + clusters + 
 				" -k " + numOfClusters + " -x " + Constants.maxIter + " -dm " + Constants.distanceMesasure + Constants.scriptSeparator);
 		//scriptSB.append("mahout clusterdump -i " + clustersFinal + " -p " + clusteredPoints + " -o " + Constants.defaultUploadDir + clusterResult + Constants.scriptSeparator);
 		scriptSB.append("mahout seqdumper -i " + clusteredPoints + " -o " + Constants.defaultUploadDir + clusterResult + Constants.scriptSeparator);
 		//System.out.println(scriptSB.toString());
 
 		// 3) upload script
 		String scriptPath = Constants.scriptPath + taskId;
 		FileUtils.writeStringToFile(new File(scriptPath), scriptSB.toString(), Constants.defaultFileEncoding);
 		
 		return scriptPath;
 	}
 	
 	private String generateCleanScripts(String destCompressFilePath, String scriptPath,
 			String newFileName, String newFileSeq, String newFileVectors, String clusterResult,
 			String centroid, String clusters) {
 		
 		StringBuffer cleanSB = new StringBuffer();
 		cleanSB.append("rm -rf " + Constants.defaultUploadDir + newFileName + Constants.scriptSeparator);
 		cleanSB.append("rm -rf " + Constants.defaultUploadDir + destCompressFilePath + Constants.scriptSeparator);
 		cleanSB.append("hadoop fs -rmr " + newFileName + Constants.scriptSeparator);
 		cleanSB.append("hadoop fs -rmr " + newFileSeq + Constants.scriptSeparator);
 		cleanSB.append("hadoop fs -rmr " + newFileVectors + Constants.scriptSeparator);
 		cleanSB.append("hadoop fs -rmr " + centroid + Constants.scriptSeparator);
 		cleanSB.append("hadoop fs -rmr " + clusters + Constants.scriptSeparator);
 		cleanSB.append("rm -rf " + Constants.defaultUploadDir + scriptPath + Constants.scriptSeparator);
 		cleanSB.append("rm -rf " + Constants.defaultUploadDir + clusterResult + Constants.scriptSeparator);
 		//System.out.println(cleanSB.toString());
 		return cleanSB.toString();
 	}
 
 	
 }
