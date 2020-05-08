 /**
  * 
  */
 package edu.illinois.ncsa.versus.web.server;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.tupeloproject.kernel.OperatorException;
 import org.tupeloproject.rdf.Resource;
 
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 
 import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
 import edu.illinois.ncsa.versus.web.client.ExecutionService;
 import edu.illinois.ncsa.versus.web.shared.Job;
 import edu.illinois.ncsa.versus.web.shared.PairwiseComparison;
 import edu.illinois.ncsa.versus.web.shared.Submission;
 import edu.uiuc.ncsa.cet.bean.tupelo.DatasetBeanUtil;
 
 /**
  * @author lmarini
  *
  */
 @SuppressWarnings("serial")
 public class ExecutionServiceImpl extends RemoteServiceServlet implements
 		ExecutionService {
 	
 	private static ExecutionEngine executionEngine = new ExecutionEngine(TupeloStore.getInstance().getBeanSession());
 	
     /** Commons logging **/
     private static Log log = LogFactory.getLog(ExecutionServiceImpl.class);
 	
 	@Override
 	public Job submit(Submission set) {
 		
 		// create comparison
 		Set<PairwiseComparison> comparisons = new HashSet<PairwiseComparison>();
 		DatasetBeanUtil dbu = new DatasetBeanUtil(TupeloStore.getInstance().getBeanSession());
 		
 		List<String> datasetsURI = new ArrayList<String>(set.getDatasetsURI());
 		for (int i=0; i<datasetsURI.size(); i++) {
 			for (int j=i+1; j<datasetsURI.size(); j++) {
 				PairwiseComparison pairwiseComparison = new PairwiseComparison();
 				try {
 					pairwiseComparison.setFirstDataset(dbu.get(Resource.uriRef(datasetsURI.get(i))));
 					pairwiseComparison.setSecondDataset(dbu.get(Resource.uriRef(datasetsURI.get(j))));
					pairwiseComparison.setAdapterId(set.getAdapter().getId());
 					pairwiseComparison.setMeasureId(set.getMeasure().getId());
 					pairwiseComparison.setExtractorId(set.getExtraction().getId());
 				} catch (OperatorException e) {
 					log.error("Error setting up comparison",e);
 				}
 				comparisons.add(pairwiseComparison);
 			}
 		}
 		
 		// submit job for execution
 		Job job = new Job();
 		job.setStarted(new Date());
 		job.setComparison(comparisons);
 		executionEngine.submit(job);
 		return job;
 	}
 
 	@Override
 	public Job getStatus(String jobId) {
 		return executionEngine.getJob(jobId);
 	}
 }
