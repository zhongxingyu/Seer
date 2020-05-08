 package gov.nih.nci.caintegrator.analysis.server;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Vector;
 
 import gov.nih.nci.caintegrator.analysis.messaging.*;
 import gov.nih.nci.caintegrator.exceptions.AnalysisServerException;
 
 import org.rosuda.JRclient.REXP;
 
 public class PrincipalComponentAnalysisTaskR extends AnalysisTaskR {
 
 	private PrincipalComponentAnalysisResult result = null;
 
 	public PrincipalComponentAnalysisTaskR(
 			PrincipalComponentAnalysisRequest request) {
 		this(request, false);
 	}
 
 	public PrincipalComponentAnalysisTaskR(
 			PrincipalComponentAnalysisRequest request, boolean debugRcommands) {
 		super(request, debugRcommands);
 	}
 
 	public void run() {
 		PrincipalComponentAnalysisRequest pcaRequest = (PrincipalComponentAnalysisRequest) getRequest();
 		result = new PrincipalComponentAnalysisResult(getRequest()
 				.getSessionId(), getRequest().getTaskId());
 		
 		System.out.println(this.getExecutingThreadName() + " processing principal component analysis request=" + pcaRequest);
 		
 		double[] pca1, pca2, pca3;
 
 		doRvoidEval("pcaInputMatrix <- dataMatrix");
 
 		if (pcaRequest.getSampleGroup() != null) {
 			// sample group should never be null when passed from middle tier
 			String rCmd = getRgroupCmd("sampleIds", pcaRequest.getSampleGroup());
 			doRvoidEval(rCmd);
 			rCmd = "pcaInputMatrix <- getSubmatrix.onegrp(pcaInputMatrix, sampleIds)";
 			doRvoidEval(rCmd);
 		}
 
 		if (pcaRequest.getReporterGroup() != null) {
 			String rCmd = getRgroupCmd("reporterIds", pcaRequest
 					.getReporterGroup());
 			doRvoidEval(rCmd);
 			rCmd = "pcaInputMatrix <- getSubmatrix.rep(pcaInputMatrix, reporterIds)";
 			doRvoidEval(rCmd);
 		}
 
 		if (pcaRequest.doVarianceFiltering()) {
 			double varianceFilterValue = pcaRequest.getVarianceFilterValue();
 			System.out
 					.println("Processing principal component analysis request varianceFilterVal="
 							+ varianceFilterValue);
 			doRvoidEval("pcaResult <- computePCAwithVariance(pcaInputMatrix,"
 					+ varianceFilterValue + " )");
 		} else if (pcaRequest.doFoldChangeFiltering()) {
 			double foldChangeFilterValue = pcaRequest
 					.getFoldChangeFilterValue();
 			System.out
 					.println("Processing principal component analysis request foldChangeFilterVal="
 							+ foldChangeFilterValue);
 			doRvoidEval("pcaResult <- computePCAwithFC(pcaInputMatrix,"
 					+ foldChangeFilterValue + " )");
 		}
 
 		// check to make sure at least 3 components came back
 		int numComponents = doREval("length(pcaResult$x[1,])").asInt();
 		if (numComponents < 3) {
 			AnalysisServerException ex = new AnalysisServerException(
 					"PCA result has less than 3 components.");
 			ex.setFailedRequest(pcaRequest);
 			setException(ex);
 			return;
 		}
 
 		pca1 = doREval("pcaMatrixX <- pcaResult$x[,1]").asDoubleArray();
 		pca2 = doREval("pcaMatrixY <- pcaResult$x[,2]").asDoubleArray();
 		pca3 = doREval("pcaMatrixZ <- pcaResult$x[,3]").asDoubleArray();
 		REXP exp = doREval("pcaLabels <- dimnames(pcaResult$x)");
 		// System.out.println("Got back xVals.len=" + xVals.length + "
 		// yVals.len=" + yVals.length + " zVals.len=" + zVals.length);
 		Vector labels = (Vector) exp.asVector();
 		Vector sampleIds = ((REXP) (labels.get(0))).asVector();
 //		Vector pcaLabels = ((REXP) (labels.get(1))).asVector();
 
 		List<PCAresultEntry> pcaResults = new ArrayList<PCAresultEntry>(
 				sampleIds.size());
 
 		String sampleId = null;
 		int index = 0;
 		for (Iterator i = sampleIds.iterator(); i.hasNext();) {
 			sampleId = ((REXP) i.next()).asString();
 			pcaResults.add(new PCAresultEntry(sampleId, pca1[index],
 					pca2[index], pca3[index]));
 			index++;
 		}
 
 		result.setResultEntries(pcaResults);
 
 		// generate the pca1 vs pca2 image
 		doRvoidEval("maxComp1<-max(abs(pcaResult$x[,1]))");
 		doRvoidEval("maxComp2<-max(abs(pcaResult$x[,2]))");
 		doRvoidEval("maxComp3<-max(abs(pcaResult$x[,3]))");
 		doRvoidEval("xrange<-c(-maxComp1,maxComp1)");
 		doRvoidEval("yrange<-c(-maxComp2,maxComp2)");
 		String plot1Cmd = "plot(pcaResult$x[,1],pcaResult$x[,2],xlim=xrange,ylim=yrange,main=\"Component1 Vs Component2\",xlab=\"PC1\",ylab=\"PC2\",pch=20)";
 		byte[] img1Code = getImageCode(plot1Cmd);
 		result.setImage1Bytes(img1Code);
 
 		// generate the pca1 vs pca3 image
 		doRvoidEval("yrange<-c(-maxComp3,maxComp3)");
 		String plot2Cmd = "plot(pcaResult$x[,1],pcaResult$x[,3],xlim=xrange,ylim=yrange,main=\"Component1 Vs Component3\",xlab=\"PC1\",ylab=\"PC3\",pch=20)";
 		byte[] img2Code = getImageCode(plot2Cmd);
 		result.setImage2Bytes(img2Code);
 
 		// generate the pca2 vs pca3 image
 		doRvoidEval("xrange<-c(-maxComp2,maxComp2)");
 		doRvoidEval("yrange<-c(-maxComp3,maxComp3)");
 		String plot3Cmd = "plot(pcaResult$x[,2],pcaResult$x[,3],xlim=xrange,ylim=yrange,main=\"Component2 Vs Component3\",xlab=\"PC2\",ylab=\"PC3\",pch=20)";
 		byte[] img3Code = getImageCode(plot3Cmd);
 		result.setImage3Bytes(img3Code);
 
 	}
 
 	@Override
 	public AnalysisResult getResult() {
 		return result;
 	}
 
 	/**
 	 * Clean up some R memory and release and remove the 
 	 * reference to the R connection so that this task can be 
 	 * garbage collected.
 	 */
 	public void cleanUp() {
		doRvoidEval("remove(hcInputMatrix)");
		doRvoidEval("remove(mycluster)");
 		setRconnection(null);
 	}
 
 }
