 /*******************************************************************************
  * Copyright (c) 2012 Emanuele Tamponi.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v3.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/gpl.html
  * 
  * Contributors:
  *     Emanuele Tamponi - initial API and implementation
  ******************************************************************************/
 package game.plugins.metrics;
 
 import game.core.Dataset;
 import game.core.Dataset.SampleIterator;
 import game.core.Experiment;
 import game.core.Sample;
 import game.core.experiments.FullExperiment;
 import game.core.metrics.FullMetric;
 import game.plugins.datatemplates.LabelTemplate;
 import game.utils.Utils;
 
 import java.util.List;
 
 import org.apache.commons.math3.linear.Array2DRowRealMatrix;
 import org.apache.commons.math3.linear.RealMatrix;
 
 public class ConfusionMatrix extends FullMetric {
 	
 	public RealMatrix matrix;
 	public List<String> labels;
 	
 	public ConfusionMatrix() {
 		setPrivateOptions("matrix", "labels");
 	}
 	
 	@Override
 	public boolean isCompatible(Experiment exp) {
 		return super.isCompatible(exp) &&
 				Utils.checkTemplateClass(exp.template.outputTemplate, LabelTemplate.class);
 	}
 
 	@Override
 	public boolean isReady() {
 		return matrix != null;
 	}
 
 	@Override
 	public void evaluate(FullExperiment experiment) {
 		labels = experiment.template.outputTemplate.getOption("labels");
 		
 		matrix = new Array2DRowRealMatrix(labels.size(), labels.size());
 		
 		for(Dataset dataset: experiment.testedDatasets) {
 			SampleIterator it = dataset.sampleIterator(true);
 			while(it.hasNext()) {
 				Sample sample = it.next();
 				int observed = labels.indexOf(sample.getOutput());
 				int predicted = labels.indexOf(sample.getPrediction());
 				matrix.setEntry(observed, predicted, matrix.getEntry(observed, predicted)+1);
 			}
 		}
 	}
 
 	@Override
 	public String prettyPrint() {
 		StringBuilder ret = new StringBuilder();
 		
		ret.append(String.format("%20s |", ""));
 		for(String label: labels)
			ret.append(String.format("%20s |", label+"(P)"));
 		ret.append("\n");
 		for(int i = 0; i < labels.size(); i++) {
 			String observed = labels.get(i);
			ret.append(String.format("%20s |", observed+"(O)"));
 			for(int j = 0; j < labels.size(); j++)
				ret.append(String.format("%20.0f |", matrix.getEntry(i, j)));
 			ret.append("\n");
 		}
 		
 		return ret.toString();
 	}
 
 }
