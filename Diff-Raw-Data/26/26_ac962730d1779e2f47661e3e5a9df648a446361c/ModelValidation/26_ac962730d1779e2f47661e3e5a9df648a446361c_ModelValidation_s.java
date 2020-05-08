 package org.roettig.MLToolbox.validation;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Random;
 import java.util.Vector;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.roettig.MLToolbox.base.Prediction;
 import org.roettig.MLToolbox.base.impl.DefaultInstanceContainer;
 import org.roettig.MLToolbox.base.instance.Instance;
 import org.roettig.MLToolbox.base.instance.InstanceContainer;
 import org.roettig.MLToolbox.base.instance.PrimalInstance;
 import org.roettig.MLToolbox.base.label.FactorLabel;
 import org.roettig.MLToolbox.base.label.Label;
 import org.roettig.MLToolbox.base.parameter.Parameter;
 import org.roettig.MLToolbox.model.ClassificationModel;
 import org.roettig.MLToolbox.model.Model;
 import org.roettig.MLToolbox.model.SelectedModel;
 import org.roettig.MLToolbox.util.MLHelper;
 import org.roettig.MLToolbox.util.SerialClone;
 
 public class ModelValidation
 {
 	private static Logger logger = Logger.getLogger(ModelValidation.class.getCanonicalName());
 
 	/**
 	 * turns off log-messages during validation operations.
 	 */
 	public static void turnLoggingOff()
 	{
 		logger.setLevel(Level.OFF);
 	}
 
 	/**
 	 * turns on log-messages during validation operations.
 	 */
 	public static void turnLoggingOn()
 	{
 		logger.setLevel(Level.INFO);
 	}	
 
 	/**
 	 * 
 	 * conducts a simple nested cross-validation with <i>nOF</i> outer folds
 	 * and <i>nIF</i> inner folds on the instances given by <i>samples</i>.
 	 * The model to operate on is given by <i>m</i>.
 	 * 
 	 * @param nOF
 	 * @param nIF
 	 * @param samples
 	 * @param m
 	 * 
 	 * @return SelectedModel
 	 *  
 	 * @throws Exception
 	 */
 	public static <T extends Instance> SelectedModel<T> SimpleNestedCV( int nOF, int nIF, InstanceContainer<T> samples, Model<T> m) throws Exception
 	{
 		return SimpleNestedCV( nOF, nIF, samples, m, 0);
 	}
 
 	/**
 	 * 
 	 * conducts a simple nested cross-validation with <i>nOF</i> outer folds
 	 * and <i>nIF</i> inner folds on the instances given by <i>samples</i>.
 	 * The model to operate on is given by <i>m</i>. The seed will be forwarded
 	 * to the source of randomness (i.e. Random).
 	 * 
 	 * @param nOF
 	 * @param nIF
 	 * @param samples
 	 * @param m
 	 * @param seed
 	 * 
 	 * @return SelectedModel
 	 *  
 	 * @throws Exception
 	 */
 	public static <T extends Instance> SelectedModel<T> SimpleNestedCV( int nOF, int nIF, InstanceContainer<T> samples, Model<T> m, long seed) throws Exception
 	{
 		if(seed!=0)
 		{
 			Random rng = new Random(seed);
 			samples.shuffle(rng);
 		}
 
 		
 		List<Prediction> Yp = new ArrayList<Prediction>();
 
 
 		for(int oF=1;oF<=nOF;oF++)
 		{
 
 			InstanceContainer<T> train = new DefaultInstanceContainer<T>();
 			InstanceContainer<T> test  = new DefaultInstanceContainer<T>();
 
 			if(m instanceof ClassificationModel)
 			{
 				getStratifiedFold(oF,nOF,samples,train,test);
 				logger.info("outer CV fold "+oF+"/"+nOF+" (stratified)   #train="+train.size()+" #test="+test.size());
 			}
 			else
 			{
 				// regression models need no stratification
 				getFold(oF,nOF,samples,train,test);
 				logger.info("outer CV fold "+oF+"/"+nOF+" (unstratified)  #train="+train.size()+" #test="+test.size());
 			}
 
 			//if(m.usesUnlabeledData())
 			//	//m.setUnlabeledData(test);
 			//	m.addUnlabeledData(test);
 
 			logger.info("Doing model selection");
 			SelectedModel<T> bestModel = ModelSelection(nIF, train, m);
 			
 			logger.info("## Training set ##");
 			for(Instance i: train)
 			{
 				Label y  = i.getLabel();
 				logger.info(String.format(" yt:"+y+" sample-id: %d", i.getProperty("id")));
 			}
 			logger.info("");
 			
 			List<Prediction> Ypl = bestModel.model.predict( test );
 			
 			logger.info("## Test set ##");
 			for(Prediction p: Ypl)
 			{
 				Yp.add(p);
 				logger.info(" yt:"+p.getTrueLabel()+" yp:"+p.getPredictedLabel()+" sample-id:"+p.getInst().getProperty("id"));
 			}
 			
 			logger.info(" local overall quality="+m.getQuality(Ypl));
 			
 			
 			//if(m.usesUnlabeledData())
 			//	m.removeUnlabeledData(test);
 		}
 
 		double generalizationquality = m.getQuality(Yp);
 		logger.info("Overall generalization quality="+generalizationquality);
 
 		SelectedModel<T> ret = null;
 		
 		logger.info("Selecting final model on full data");
 		ret = ModelSelection(nOF, samples, m);
 		String params = "Selected parameters: ";
 		for(Parameter<?> p : m.getParameters())
 		{
 			params+= p.getName()+":"+p.getCurrentValue()+", ";   
 		}
 		logger.info(params);
 		// save previously determined generalization statistics
 		ret.predictions = Yp; ret.qual = generalizationquality;
 		
 		return ret;
 	}
 	
 	//
 	
 	public static <T extends Instance> double randomizedExternalCV(double frac, int nFolds, int nrep, int seed, InstanceContainer<T> samples, Model<T> m) throws Exception
 	{
 		Random rng = new Random(seed);
 		
 		List<Prediction> preds = new ArrayList<Prediction>();
 		
 		for(int r=0;r<nrep;r++)
 		{
 			DefaultInstanceContainer<T>  train = new DefaultInstanceContainer<T>();
 			DefaultInstanceContainer<T>  test  = new DefaultInstanceContainer<T>();
 			
 			logger.info("rECV #"+(r+1)+"\n");
 			
 			if(m instanceof ClassificationModel)
 			{
 				ModelValidation.getStratifiedRandomSplit(frac, samples, train, test, rng);	
 			}
 			else
 			{
 				ModelValidation.getRandomSplit(frac, samples, train, test, rng);
 			}
 			
 			SelectedModel<T> selmod = ModelValidation.ModelSelection(nFolds, train, m);
 			
 			List<Prediction> preds_ = selmod.model.predict(test);
 			preds.addAll(preds_);			
 		}
 		
 		return m.getQuality(preds);
 	}
 	
 	public static <T extends Instance> double CV(int nFolds, InstanceContainer<T> samples, Model<T> m) throws Exception
 	{
 		List<Prediction> preds = new ArrayList<Prediction>();
 		double qual = CV(nFolds, samples, m, preds);
 		return qual;
 	}
 	
 	/**
 	 * simple cross-validation.
 	 * 
 	 * @param nFolds number of folds
 	 * @param samples input data
 	 * @param m model to use
 	 * @param preds predictions from CV
 	 * @return
 	 * @throws Exception
 	 */
 	public static <T extends Instance> double CV(int nFolds, InstanceContainer<T> samples, Model<T> m, List<Prediction> preds) throws Exception
 	{
 		for(int f=1;f<=nFolds;f++)
 		{
 			
 			DefaultInstanceContainer<T> train = new DefaultInstanceContainer<T>();
 			DefaultInstanceContainer<T> test  = new DefaultInstanceContainer<T>();
 			
 			if(m instanceof ClassificationModel)
 			{
 				getStratifiedFold(f,nFolds,samples,train,test);
 				logger.info("CV fold "+f+"/"+nFolds+" (stratified)  #train="+train.size()+" #test="+test.size());
 			}
 			else
 			{
 				getFold(f,nFolds,samples,train,test);
 				logger.info("CV fold "+f+"/"+nFolds+" (unstratified)  #train="+train.size()+" #test="+test.size());
 			}                  
 			
 			m.train(train);
 			List<Prediction> fold_preds = m.predict(test);
 			preds.addAll(fold_preds);			
 		}
 		
 		double qual = m.getQuality(preds); 
 		logger.info(String.format("overall CV qual=%.3f",qual));
 		return qual;
 	}
 	
 
 	public static <T extends Instance> double paraCV(int nFolds,final InstanceContainer<T> samples, Model<T> m, List<Prediction> preds) throws Exception
 	{
 		ExecutorService executor = Executors.newFixedThreadPool(2);
 
 		//Thread[] threads = new Thread[nFolds];
 		for(int f=1;f<=nFolds;f++)
 		{
 			final int        nFolds_ = nFolds;
 			final int        f_      = f;
 			final Model<T>   m_ = SerialClone.clone(m);
 			///List<Prediction>
 			Runnable r = new Runnable() 
 			{
 				  public void run() 
 				  {
 					    InstanceContainer<T> train = new DefaultInstanceContainer<T>();
 						InstanceContainer<T> test  = new DefaultInstanceContainer<T>();
 						
 						if(m_ instanceof ClassificationModel)
 						{
 							getStratifiedFold(f_,nFolds_,samples,train,test);
 							logger.info("CV fold "+f_+"/"+nFolds_+" (stratified)  #train="+train.size()+" #test="+test.size());
 						}
 						else
 						{
 							getFold(f_,nFolds_,samples,train,test);
 							logger.info("CV fold "+f_+"/"+nFolds_+" (unstratified)  #train="+train.size()+" #test="+test.size());
 						}                  
 
 						m_.train(train);
 						List<Prediction> fold_preds = m_.predict(test);
 						//preds.addAll(fold_preds);
 				  }
 			};	
 
 			executor.execute(r);
 			/*
 			Thread thread = new Thread(r);
 
 			threads[f-1] =thread;
 			// Start the thread
 			thread.start();
 			*/
 		}
 		executor.shutdown();
 		// Wait until all threads are finish
 		while (!executor.isTerminated()) 
 		{
 			//System.out.println("waiting for threads");
 		}
 		System.out.println("Finished all threads");
 
 		/*
 		System.out.println("waiting for join");
 		for(int i=0;i<nFolds;i++)
 			threads[i].join();
 		System.out.println("threads joined");
 		*/
 		/*
 		for(int f=1;f<=nFolds;f++)
 		{
 
 			InstanceContainer<T> train = new DefaultInstanceContainer<T>();
 			InstanceContainer<T> test  = new DefaultInstanceContainer<T>();
 			
 			if(m instanceof ClassificationModel)
 			{
 				getStratifiedFold(f,nFolds,samples,train,test);
 				logger.info("CV fold "+f+"/"+nFolds+" (stratified)  #train="+train.size()+" #test="+test.size());
 			}
 			else
 			{
 				getFold(f,nFolds,samples,train,test);
 				logger.info("CV fold "+f+"/"+nFolds+" (unstratified)  #train="+train.size()+" #test="+test.size());
 			}                  
 
 			m.train(train);
 			List<Prediction> fold_preds = m.predict(test);
 			preds.addAll(fold_preds);
 			
 		}
 		double qual = m.getQuality(preds); 
 		logger.info(String.format("overall CV qual=%.3f",qual));
 		return qual;
 		*/
 		return 0.0;
 	}
 	
 	/**
 	 * gives the <i>f</i>-th stratified fold 
 	 * 
 	 * @param f fold index
 	 * @param F total number of folds
 	 * @param samples 
 	 * @param train training fold data
 	 * @param test  test fold data
 	 *
 	 * @throws Exception
 	 */
 	public static <T extends Instance> void getStratifiedFold(int f, int F, InstanceContainer<T> samples, InstanceContainer<T> train, InstanceContainer<T> test)
 	{
 		f--;
 
 		Map< Label, List<T> > pooledSamples = new HashMap< Label, List<T> >();
 
 		// pool samples according to label equivalence
 		for(T s: samples)
 		{
 			Label lab = s.getLabel();
 
 			// pass thru unlabeled data
 			if(lab.getDoubleValue()==0.0)
 			{
 				train.add(s);
 				continue;
 			}
 
 
 			if(pooledSamples.containsKey( lab ))
 			{
 				pooledSamples.get( lab ).add(s);
 			}
 			else
 			{
 				List<T> labsamples = new Vector<T>();
 				labsamples.add(s);
 				pooledSamples.put( lab, labsamples);
 			}
 		}
 
 		for(Label lab: pooledSamples.keySet())
 		{
 			int i = 0;
 			for(T s: pooledSamples.get(lab))
 			{
 				if(i%F==f)
 					test.add( s );
 				else
 					train.add( s );
 				i++;
 			}               
 		}
 	}
 
 	/**
 	 * gives the <i>f</i>-th non-stratified fold 
 	 * 
 	 * @param f
 	 * @param F 
 	 * @param samples
 	 * @param train
 	 * @param test 
 	 *
 	 * @throws Exception
 	 */
 	public static <T extends Instance> void getFold(int f, int F, InstanceContainer<T> samples, InstanceContainer<T> train, InstanceContainer<T> test)
 	{
 		f--;
 		int i = 0;
 		for(T s: samples)
 		{
 			if(i%F==f)
 			{
 				test.add( s );
 			}
 			else
 			{
 				train.add( s );
 			}
 			i++;
 		}               
 
 	}
 	
 	/**
 	 * gives a stratified split into sets of size frac*N and (1-frac)*N 
 	 * 
 	 * @param frac
 	 * @param samples
 	 * @param train
 	 * @param test 
 	 *
 	 * @throws Exception
 	 */
 	public static <T extends Instance> void getStratifiedSplit(double frac, InstanceContainer<T> samples, InstanceContainer<T> train, InstanceContainer<T> test)
 	{
 		Map< Label, Vector<T> > pooledSamples = new HashMap< Label, Vector<T> >();
 
 		// pool samples according to label equivalence
 		for(T s: samples)
 		{
 			FactorLabel lab = (FactorLabel) s.getLabel();
 
 			// pass thru unlabeled data
 			if(lab.isUnlabeled())
 			{
 				train.add(s);
 				continue;
 			}
 
 			if(pooledSamples.containsKey( lab ))
 			{
 				pooledSamples.get( lab ).addElement(s);
 			}
 			else
 			{
 				Vector<T> labsamples = new Vector<T>();
 				labsamples.addElement(s);
 				pooledSamples.put( lab, labsamples);
 			}
 		}
 
 		for(Label lab: pooledSamples.keySet())
 		{
 			int i = 0;
 			int K = (int) (frac*pooledSamples.get(lab).size());
 
 			for(T s: pooledSamples.get(lab))
 			{
 				if(i<K)  
 					train.add( s );
 				else
 					test.add( s );
 				i++;
 			}               
 		}
 	}
 
 	/**
 	 * gives a stratified split into sets of size frac*N and (1-frac)*N 
 	 * 
 	 * @param frac fraction to split
 	 * @param samples
 	 * @param train
 	 * @param test 
 	 * @param rng source of randomness
 	 *  
 	 * @throws Exception
 	 */
 	public static <T extends Instance> void getStratifiedRandomSplit(double frac, InstanceContainer<T> samples, InstanceContainer<T> train, InstanceContainer<T> test, Random rng)
 	{
 		Map< Label, List<T> > pooledSamples = new HashMap< Label, List<T> >();
 
 		// pool samples according to label equivalence
 		for(T s: samples)
 		{
 			FactorLabel lab = (FactorLabel) s.getLabel();
 
 			// pass thru unlabeled data
 			if(lab.isUnlabeled())
 			{
 				train.add(s);
 				continue;
 			}
 
 			if(pooledSamples.containsKey( lab ))
 			{
 				pooledSamples.get( lab ).add(s);
 			}
 			else
 			{
 				List<T> labsamples = new Vector<T>();
 				labsamples.add(s);
 				pooledSamples.put( lab, labsamples);
 			}
 		}
 
 		for(Label lab: pooledSamples.keySet())
 		{
 			int i = 0;
 			int K = (int) (frac*pooledSamples.get(lab).size());
 
 			Collections.shuffle(pooledSamples.get(lab),rng);
 
 			for(T s: pooledSamples.get(lab))
 			{
 				if(i<K)  
 					train.add( s );
 				else
 					test.add( s );
 				i++;
 			}               
 		}
 	}
 
 	/**
 	 * gives a split into sets of size frac*N and (1-frac)*N 
 	 * 
 	 * @param frac
 	 * @param samples
 	 * @param train
 	 * @param test 
 	 *
 	 * @throws Exception
 	 */
 	public static <T extends Instance> void getSplit(double frac, InstanceContainer<T> samples, InstanceContainer<T> train, InstanceContainer<T> test)
 	{
 		int i = 0;
 		int K = (int) (frac*samples.size());
 
 		for(T s: samples)
 		{
 			if(i<K)  
 				train.add( s );
 			else
 				test.add( s );
 			i++;
 		}
 	}
 	
 	/**
 	 * gives a randomized split into sets of size frac*N and (1-frac)*N 
 	 * 
 	 * @param frac
 	 * @param samples
 	 * @param train
 	 * @param test 
 	 *
 	 * @throws Exception
 	 */
 	public static <T extends Instance> void getRandomSplit(double frac, InstanceContainer<T> samples, InstanceContainer<T> train, InstanceContainer<T> test, Random rng)
 	{
 		List<Integer> randperm = MLHelper.randPerm(samples.size(), rng);
 		
 		int K = (int) (frac*samples.size());
 		
 		for(int i=0;i<samples.size();i++)
 		{
 			if(i<K)  
 				train.add( samples.get(randperm.get(i)) );
 			else
 				test.add( samples.get(randperm.get(i)) );
 		}
 	}
 	
 	/**
 	 * gives a Vector of int arrays holding the parameter indices of each grid point.
 	 * The parameter <i>size</i> holds the number of possible values for each hyperparameter. 
 	 * 
 	 * @param size 
 	 *
 	 * @return Vector&lt;int[]&gt;
 	 *
 	 * @throws Exception
 	 */
 	public static Vector< Integer[]> getGridPoints( int size[] )
 	{
 		Vector< Integer[] > points = new Vector< Integer[]>();
 
 		int nParams = size.length;
 
 		int[] regs = new int[nParams];
 		int[] freq = new int[nParams];
 
 
 		int N = 1;
 
 		for(int i=0;i<size.length;i++)
 		{
 			N*=size[i];
 		}
 
 		// calc periodicities
 		freq[0] = 1;
 		for(int i=0;i<nParams-1;i++)
 			freq[i+1]=freq[i]*size[i];
 
 		// each registers counts 1 element further ( modulo #elems in bin)
 		// with periodicity (according to global counter cyc) stored in
 		// array freq
 
 		for(int cyc=1;cyc<=N;cyc++)
 		{
 			Integer[] point = new Integer[nParams];
 			for(int i=0;i<nParams;i++)
 			{
 				//System.out.print(regs[i]+" ");
 				point[i] = regs[i];
 			}
 			//System.out.println();
 			points.add( point );
 
 			for(int i=0;i<nParams;i++)
 			{
 				if(cyc%freq[i]==0)
 				{
 					regs[i]+=1;
 					regs[i] = regs[i]%size[i];
 				}
 			}
 		}
 		return points;
 	}
 	
 	/**
 	 * does model selection using CV with <i>F</i> folds to select
 	 * the best model trying all combinations of hyperparameters.
 	 * 
 	 * @param F
 	 * @param samples
 	 * @param m
 	 *
 	 * @throws Exception
 	 */
 	public static <T extends Instance> SelectedModel<T> ModelSelection(int F, InstanceContainer<T> samples, Model<T> m) throws Exception
 	{
 		List<Parameter<?>> params_ = m.getParameters(); 
 		int nParams       = params_.size(); 
 
 		int[] size        = new int[nParams];
 
 		for(int p=0;p<nParams;p++)
 		{
 			List<Integer> pTry = new ArrayList<Integer>();
 			
 			for(int i=0;i<params_.get(p).getSize();i++)
 				pTry.add(i);
 			
 			size[p]            = pTry.size();
 		}
 
 		Vector<Integer[]>  gridpoints = getGridPoints(size);
 
 
 		double maxQual = -1000;
 
 		int idx = 0; int bestIdx = 0;
 
 		for(Integer[] points : gridpoints)
 		{
 			logger.info("Testing grid point #"+idx);
 
 			// set model parameters to current grid values
 			for(int p=0;p<nParams;p++)
 			{
 				params_.get(p).setCurrentValue(points[p]);
 				logger.info(" setting model hyperparameter "+params_.get(p).getName()+"["+params_.get(p).hashCode()+"] to "+params_.get(p).getCurrentValue());
 			}
 
 			double qual = ModelValidation.CV(F,samples,m);
 			if(qual>maxQual)
 			{
 				maxQual  = qual;
 				bestIdx = idx;
 			}
 			idx++;
 		}
 
 		for(int p=0;p<nParams;p++)
 		{
 			params_.get(p).setCurrentValue(gridpoints.get(bestIdx)[p]);
 			logger.info(" setting final model hyperparameter "+params_.get(p).getName()+"["+params_.get(p).hashCode()+"] to "+params_.get(p).getCurrentValue());
 		}
 		logger.info("with quality "+maxQual);         
 		logger.info("doing training on full training set using optimal hyperparameters");
 		m.train(samples);
 
 		return new SelectedModel<T>(m,maxQual);
 	}
 
 	/**
 	 * divides the input into two subsets (discriminator,!discriminator) and labels the first one with label pos and the second one with label neg.
 	 * 
 	 * @param samples
 	 * @param discriminator : the discriminator label
 	 * @param pos
 	 * @param neg
 	 * @return
 	 */
 	public static <T extends Instance> InstanceContainer<T> binaryDivision(InstanceContainer<T> samples, Label discriminator, Label pos, Label neg)
 	{
 		InstanceContainer<T> ret = new DefaultInstanceContainer<T>();
 		for(Instance i: samples)
 		{
 			if(i.getLabel().equals(discriminator))
 				ret.add( (T) i.reassign(pos) );
 			else
 				ret.add( (T) i.reassign(neg) );
 
 		}
 		return ret;
 	}
 }
