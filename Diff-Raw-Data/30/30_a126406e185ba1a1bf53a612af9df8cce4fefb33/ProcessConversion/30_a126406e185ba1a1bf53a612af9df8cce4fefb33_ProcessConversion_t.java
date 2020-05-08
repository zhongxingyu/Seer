 package org.dawnsci.conversion.converters;
 
 import java.io.File;
 import java.util.Map;
 
 import org.dawb.common.services.ServiceManager;
 import org.dawb.common.services.conversion.IConversionContext;
 import org.dawb.common.services.conversion.IProcessingConversionInfo;
 
 import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
 import uk.ac.diamond.scisoft.analysis.io.IDataHolder;
 import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
 import uk.ac.diamond.scisoft.analysis.metadata.AxesMetadataImpl;
 import uk.ac.diamond.scisoft.analysis.processing.IOperationService;
 import uk.ac.diamond.scisoft.analysis.processing.IRichDataset;
 
 public class ProcessConversion extends AbstractConversion {
 
 	IOperationService service;
 	private final static String PROCESSED = "_processed.nxs";
 	
 	public ProcessConversion(IConversionContext context) {
 		super(context);
 		
 	}
 
 	protected void iterate(final ILazyDataset         lz, 
             final String               nameFrag,
             final IConversionContext   context) throws Exception {
 		
 		if (service == null) service = (IOperationService)ServiceManager.getService(IOperationService.class);
 		
 		Object userObject = context.getUserObject();
 		
 		if (userObject == null || !(userObject instanceof IProcessingConversionInfo)) throw new IllegalArgumentException("User object not valid for conversion");
 		
 		IProcessingConversionInfo info = (IProcessingConversionInfo) userObject;
 		final Map<Integer, String> sliceDimensions = context.getSliceDimensions();
 		
 		Map<Integer, String> axesNames = context.getAxesNames();
 		
 		if (axesNames != null) {
 			
 			AxesMetadataImpl axMeta = null;
 			
 			try {
 				axMeta = new AxesMetadataImpl(lz.getRank());
 				for (Integer key : axesNames.keySet()) {
 					String axesName = axesNames.get(key);
 					IDataHolder dataHolder = LoaderFactory.getData(context.getSelectedConversionFile().getAbsolutePath());
 					ILazyDataset lazyDataset = dataHolder.getLazyDataset(axesName);
 					axMeta.setAxis(key, new ILazyDataset[] {lazyDataset});
 				}
 				
 				lz.setMetadata(axMeta);
 			} catch (Exception e) {
 				//no axes metadata
 			}
 		}
 		
 		IRichDataset rich = new IRichDataset() {
 			
 			@Override
 			public Map<Integer, String> getSlicing() {
 				return sliceDimensions;
 			}
 			
 			@Override
 			public ILazyDataset getData() {
 				return lz;
 			}
 		};
 		
 		String name = getFileNameNoExtension(context.getSelectedConversionFile());
 		String outputFolder = context.getOutputPath();
 		String full = outputFolder + File.separator + name + PROCESSED;
 		
 		//TODO output path
 		service.executeSeries(rich, context.getMonitor(), info.getExecutionVisitor(full), info.getOperationSeries());
 	}
 	
	protected ILazyDataset getLazyDataset(final File                 path, 
            final String               dsPath,
            final IConversionContext   context) throws Exception {
		ILazyDataset lazyDataset = super.getLazyDataset(path, dsPath, context);
		
		if (lazyDataset != null) return lazyDataset;
		

		final IDataHolder   dh = LoaderFactory.getData(path.getAbsolutePath());
		context.setSelectedH5Path(dsPath);
		if (context.getMonitor()!=null) {
			context.getMonitor().subTask("Process '"+path.getAbsolutePath() +"''"+dsPath+"'");
		}
		return dh.getLazyDataset(dsPath);
	}
	
 	@Override
 	protected void convert(IDataset slice) throws Exception {
 		// does nothing, conversion is in the iterate method
 	}
 
 
 }
