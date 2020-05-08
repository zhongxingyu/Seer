 /*******************************************************************************
  * Copyright (c) 2012 Emanuele.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v3.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/gpl.html
  * 
  * Contributors:
  *     Emanuele - initial API and implementation
  ******************************************************************************/
 package game.plugins.metrics;
 
 import game.core.Dataset;
 import game.core.Dataset.SampleIterator;
 import game.core.Result;
 import game.core.Sample;
 import game.core.metrics.FullMetric;
 import game.plugins.datatemplates.LabelTemplate;
 
 import java.util.LinkedList;
 import java.util.List;
 
 public class AccuracyPrecisionRecall extends FullMetric {
 	
	private List<Double> singleTP;
	private List<Double> singleT;
	private List<Double> singleP;
 	
 	@Override
 	public boolean isCompatible(Result result) {
 		return super.isCompatible(result) &&
 				result.experiment.template.outputTemplate instanceof LabelTemplate;
 	}
 
 	@Override
 	protected void prepare() {
		singleTP = new LinkedList<>();
		singleT = new LinkedList<>();
		singleP = new LinkedList<>();
		
 		List<String> labels = getResult().experiment.template.outputTemplate.getOption("labels");
 		for(int k = 0; k < labels.size(); k++) {
 			singleTP.add(0.0);
 			singleP.add(0.0);
 			singleT.add(0.0);
 		}
 		
 		for(Dataset dataset: getResult().testedDatasets.getList(Dataset.class)) {			
 			SampleIterator it = dataset.sampleIterator(true);
 			while(it.hasNext()) {
 				Sample sample = it.next();
 				String trueLabel = (String) sample.getOutput();
 				String predLabel = (String) sample.getPrediction();
 				int k = labels.indexOf(predLabel);
 				singleP.set(k, singleP.get(k)+1);
 				if (predLabel.equals(trueLabel))
 					singleTP.set(k, singleTP.get(k)+1);
 				k = labels.indexOf(trueLabel);
 				singleT.set(k, singleT.get(k)+1);
 			}
 		}
 		
 	}
 
 	@Override
 	public String prettyPrint() {
 		StringBuilder ret = new StringBuilder();
 		
 		List<String> labels = getResult().experiment.template.outputTemplate.getOption("labels");
 		
 		ret.append(String.format("%20s%15s%15s%15s%15s%15s\n", "", "Observed", "Classified", "Correct", "Precision", "Recall"));
 		double totalT = 0;
 		double totalTP = 0;
 		for(int i = 0; i < labels.size(); i++) {
 			ret.append(String.format("%20s%15.0f%15.0f%15.0f%15.2f%15.2f\n", labels.get(i),
 																	   		 singleT.get(i),
 																	   		 singleP.get(i),
 																	   		 singleTP.get(i),
 																	   		 singleTP.get(i)/singleP.get(i)*100,
 																	   		 singleTP.get(i)/singleT.get(i)*100));
 			totalT += singleT.get(i);
 			totalTP += singleTP.get(i);
 		}
 		ret.append(String.format("\n%20s%15.0f%15.0f%15.0f%15s%15.2f\n", "Overall",
 																	 totalT, totalT, totalTP,
 																	 "Accuracy", 100*totalTP/totalT));
 		
 		return ret.toString();
 	}
 
 }
