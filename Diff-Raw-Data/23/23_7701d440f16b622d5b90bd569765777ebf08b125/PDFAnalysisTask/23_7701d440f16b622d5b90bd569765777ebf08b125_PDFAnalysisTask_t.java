 /**
  * 
  */
 package fr.esiea.sd.greenrobot.website;
 
 import java.util.List;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutionException;
 
 import org.apache.pdfbox.pdmodel.PDDocument;
 
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Multimap;
 import com.google.common.util.concurrent.ListenableFuture;
 import com.google.common.util.concurrent.ListeningExecutorService;
 
 import fr.esiea.sd.greenrobot.pdf_analysis.extraction.KeywordsExtractor;
 import fr.esiea.sd.greenrobot.pdf_analysis.graph.Keyword;
 import fr.esiea.sd.greenrobot.pdf_analysis.graph.KeywordFactory;
 import fr.esiea.sd.greenrobot.pdf_analysis.graph.KeywordsGraphBuilder;
 
 /**
  * @author nic0w
  *
  */
 public class PDFAnalysisTask {
 
 	private class InternalAnalyzerThread implements Callable<KeywordsGraphBuilder> {
 
 		private final PDDocument pdfDoc;
 
 		private KeywordsExtractor keywordExtractor;
 		
 		InternalAnalyzerThread(PDDocument pdfDocument) {
 			this.pdfDoc = pdfDocument;
 		}
 
 		@Override
 		public KeywordsGraphBuilder call() throws Exception {
 			
 			this.keywordExtractor = new KeywordsExtractor(this.pdfDoc);
 			
 			//We retrieve the keywords and the position of all their occurrences in document.
 			Multimap<String, Integer> keywordsOccurences = this.keywordExtractor.call();
 			
 			//We transform the list of keyword & occurrences to a List of Keyword objects.
 			List<Keyword> keywords = Lists.newArrayList(
 					Iterables.transform(
 							keywordsOccurences.asMap().entrySet(), 
 							KeywordFactory.asFunction()
 						)	
 					);
 			
 			//Now we can return the KeywordsGraphBuilder !
 			return new KeywordsGraphBuilder(keywords);
 		}
 
 		public int getProgression() {
 			return this.keywordExtractor.getExtractionProgress();
 		}
 
 	}
 
 
 	private final ListeningExecutorService executor;
 	private final boolean startOnDocumentReception;
 
 	private PDDocument document;
 	private Throwable failure;
 	private ListenableFuture<KeywordsGraphBuilder> futureGraphBuilder;
 	private InternalAnalyzerThread internalThread;
 	private boolean analysisRunning;
 
 	public PDFAnalysisTask(ListeningExecutorService executor, boolean autostart) {
 		this.executor = executor;
 
 		this.failure  		    = null;
 		this.document 		    = null;
 		this.futureGraphBuilder = null;
 		this.internalThread     = null;
 		this.analysisRunning    = false;
 
 		this.startOnDocumentReception = autostart;
 	}
 
 	public boolean isRetrievalDone() {
 		return this.document != null;
 	}
 
 	public boolean didRetrievalFail() {
 		return this.failure != null;
 	}
 
 	public boolean isAutoStarting() {
 		return this.startOnDocumentReception;
 	}
 	
 	public boolean isAnalysisDone() {
 		if(this.futureGraphBuilder == null)
 			return false;
 		
 		return this.futureGraphBuilder.isDone();
 	}
 	
 	public void setFailure(Throwable t) {
 		this.failure = t;
 		
 		System.err.println(t);
 	}
 
 	public void setDocument(PDDocument doc) {
 
 		if(this.document == null) {
 			this.document = doc;
 
 			if(this.startOnDocumentReception)
 				startAnalysis();
 		}
 	}
 
 	public int getAnalysisProgression() {
 		return this.internalThread.getProgression();
 	}
 	
 	public KeywordsGraphBuilder getGraphBuilder() throws InterruptedException, ExecutionException {
 		return this.futureGraphBuilder.get();
 	}
 	
 	private void startAnalysis() {
 		this.futureGraphBuilder = this.executor.submit(
 				this.internalThread = new InternalAnalyzerThread(this.document)
 				);
 	}
 
 	public boolean isAnalyzing() {
 		return this.futureGraphBuilder != null && !isAnalysisDone();
 	}
 
	public Throwable getFailure() {
		return this.failure;
	}

 }
 
