 package org.roettig.MLToolbox.model;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 import java.util.Vector;
 
 import org.roettig.MLToolbox.base.Prediction;
 import org.roettig.MLToolbox.base.instance.Instance;
 import org.roettig.MLToolbox.base.instance.InstanceContainer;
 import org.roettig.MLToolbox.base.label.Label;
 import org.roettig.MLToolbox.base.parameter.Parameter;
 import org.roettig.MLToolbox.kernels.KernelFunction;
 import org.roettig.MLToolbox.validation.FMeasure;
 
 
 public class kNN<T extends Instance> extends Model<T> implements ClassificationModel
 {
 	
	public static String K = CSVCModel.class.getCanonicalName()+"_k";
 	
 	private Parameter<Integer> k = new Parameter<Integer>(K,new Integer[]{1});
 	
 	private KernelFunction<T> k_fun;
 	
 	public kNN(KernelFunction<T> k_fun)
 	{
 		this.k_fun = k_fun;
 		this.registerParameter(K, k);
 		qm = new FMeasure();
 	}
 	
 	public void addK(Integer...Ks)
 	{
 		k = new Parameter<Integer>(K,Ks);
 		this.registerParameter(K, k);
 	}
 
 	@Override
 	public void train(InstanceContainer<T> trainingdata)
 	{
 		this.trainingdata = trainingdata;
 	}
 
 	@Override
 	public List<Prediction> predict(InstanceContainer<T> testdata)
 	{
 		List<Prediction> preds = new ArrayList<Prediction>();
 		for(T t: testdata)
 		{
 			preds.add( predict(t) );
 		}
 		return preds;
 	}
 	
 	private Prediction predict(T test)
 	{
 		TreeMap<Double, Vector<T> > NNs = new TreeMap<Double, Vector<T> >();
  		
 		for(T iTrain: trainingdata)
 		{
 			if(test==iTrain)
 				continue;
 			
 			double d = k_fun.getDistance(test, iTrain);
 			insertNN(NNs,iTrain,d);
 		}
 
 		// collect all k NNs
 		List<T> neighbors = new Vector<T>();
 		Double keys[] = NNs.keySet().toArray(new Double[0]);
 
 		int K = k.getCurrentValue();
 		
 		boolean finished = false;
 		
 		for(int i=0;i<K;i++)
 		{
 			for(T nn: NNs.get(keys[i]))
 			{
 				neighbors.add(nn);
 				if(neighbors.size()==K)
 				{
 					finished = true;
 					break;
 				}
 			}
 			if(finished)
 				break;
 		}
 
 		Label yp = vote(neighbors);
 		
 		return new Prediction(test, test.getLabel(), yp);
 	}
 	
 	private final Label vote(List<T> neighbors)
 	{
 		HashMap<Label,Integer> votes = new HashMap<Label,Integer>();
 		
 		for(Instance i: neighbors)
 		{
 			if(!votes.containsKey(i.getLabel()))
 				votes.put(i.getLabel(),1);
 			else
 				votes.put(i.getLabel(), votes.get(i.getLabel())+1 );
 		}
 		
 		Integer maxVotes = Collections.max(votes.values());
 		
 		Label   maxLab   = null;
 		for(Label lab: votes.keySet())
 		{
 			if(votes.get(lab)==maxVotes)
 				maxLab = lab;			
 		}
 		return maxLab; 
 	}
 	
 	private final void insertNN(Map<Double, Vector<T> > NNs, T i, double sim)
 	{
 		if(NNs.containsKey(sim))
 		{
 			NNs.get(sim).add(i);
 		}
 		else
 		{
 			Vector<T> v = new Vector<T>();
 			v.add(i);
 			NNs.put(sim, v );
 		}
 	}
 }
