 package org.eclipse.stem.analysis.automaticexperiment.executable;
 
 import org.eclipse.emf.common.notify.Adapter;
 import org.eclipse.stem.analysis.automaticexperiment.ErrorAnalysisAlgorithm;
 import org.eclipse.stem.analysis.automaticexperiment.ErrorAnalysisAlgorithmFactory;
 import org.eclipse.stem.jobs.adapters.executable.emf.ExecutableAdapter;
 import org.eclipse.stem.jobs.adapters.executable.emf.ExecutableAdapterFactory;
 import org.eclipse.stem.jobs.execution.IExecutable;
 
 import automaticexperiment.AutomaticExperiment;
 import automaticexperiment.util.AutomaticexperimentAdapterFactory;
 
 /**
  * This class is an {@link ExecutableAdapterFactory} that creates adapters that
  * adapt {@link AutomaticExperiment}s to {@link IExecutable}s.
  */
 public class AutomaticExperimentExecutableAdapterFactory 
 	extends	AutomaticexperimentAdapterFactory 
 	implements ExecutableAdapterFactory 
 {
 
 	/**
 	 * Default Constructor
 	 */
 	public AutomaticExperimentExecutableAdapterFactory() {
 		super();
 		ExecutableAdapterFactory.INSTANCE.addAdapterFactory(this);
 	}
 	
 	/**
 	 * @see automaticexperiment.util.AutomaticexperimentAdapterFactory#createAutomaticExperimentAdapter()
 	 */
 	@Override
 	public Adapter createAutomaticExperimentAdapter() {
 		return new AutomaticExperimentExecutableAdapter();
 	}
 
 	/**
 	 * @see automaticexperiment.util.AutomaticexperimentAdapterFactory#isFactoryForType(java.lang.Object)
 	 */
 	@Override
 	public boolean isFactoryForType(final Object type) {
 		return type == IExecutable.class || super.isFactoryForType(type);
 	}
 	
 	/**
 	 * This class adapts an {@link AutomaticExperiment} to an {@link IExecutable}.
 	 */
 	protected static class AutomaticExperimentExecutableAdapter extends
 			ExecutableAdapter implements Adapter {
 
 		/**
 		 * @see org.eclipse.stem.jobs.adapters.executable.emf.ExecutableAdapter#run()
 		 */
 		@Override
 		public void run() {
 			final AutomaticExperiment automaticExperiment = (AutomaticExperiment)getTarget();
 			String algorithmName = automaticExperiment.getErrorAnalysisAlgorithm();
			ErrorAnalysisAlgorithm algorithm = 
 				ErrorAnalysisAlgorithmFactory.INSTANCE.createErrorAnalysisAlgorithm(algorithmName);
 			algorithm.init(automaticExperiment);
			algorithm.execute();
 		}
 	}
 }
