 /*
  * Copyright (c) 2012 Diamond Light Source Ltd.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  */
 package org.dawnsci.conversion.converters;
 
 import java.io.File;
 import java.text.SimpleDateFormat;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.Map;
 
 import org.dawb.common.services.ServiceManager;
 import org.dawb.common.services.conversion.IConversionContext;
 import org.dawb.common.services.conversion.IProcessingConversionInfo;
 import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
 import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
 import org.eclipse.dawnsci.analysis.api.dataset.Slice;
 import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
 import org.eclipse.dawnsci.analysis.api.processing.IOperationContext;
 import org.eclipse.dawnsci.analysis.api.processing.IOperationService;
 import org.eclipse.dawnsci.analysis.api.slice.SliceFromSeriesMetadata;
 import org.eclipse.dawnsci.analysis.api.slice.Slicer;
 import org.eclipse.dawnsci.analysis.api.slice.SourceInformation;
 
 import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
 import uk.ac.diamond.scisoft.analysis.metadata.AxesMetadataImpl;
 import uk.ac.diamond.scisoft.analysis.metadata.OriginMetadataImpl;
 
 public class ProcessConversion extends AbstractConversion {
 
 	IOperationService service;
 	private final static String PROCESSED = "_processed";
 	private final static String EXT= ".nxs";
 	
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
 					if (lazyDataset != null && lazyDataset.getRank() != lz.getRank()) {
 						lazyDataset = lazyDataset.getSlice();
 						int[] shape = new int[lz.getRank()];
 						Arrays.fill(shape, 1);
 						shape[key-1]= lazyDataset.getShape()[0];
 						lazyDataset.setShape(shape);
 						if (lazyDataset.getName() == null || lazyDataset.getName().isEmpty()) {
 							lazyDataset.setName(axesName);
 						}
 					}
 					
 					axMeta.setAxis(key-1, lazyDataset);
 				}
 				
 				lz.setMetadata(axMeta);
 			} catch (Exception e) {
 				//no axes metadata
 				e.printStackTrace();
 			}
 		}
 		
 		Slice[] init = Slicer.getSliceArrayFromSliceDimensions(sliceDimensions,lz.getShape());
 		int[] dataDims = Slicer.getDataDimensions(lz.getShape(), sliceDimensions);
 		
 		OriginMetadataImpl om = new OriginMetadataImpl(lz, init, dataDims, context.getSelectedConversionFile().getAbsolutePath(), context.getDatasetNames().get(0));
 		lz.setMetadata(om);
 		IOperationContext cc = service.createContext();
 		cc.setData(lz);
 		cc.setSlicing(sliceDimensions);
 		
 		String name = getFileNameNoExtension(context.getSelectedConversionFile());
 		String outputFolder = context.getOutputPath();
 		Date date = new Date() ;
 		SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMdd_HHmmss") ;
 		String timeStamp = "_" +dateFormat.format(date);
 		String full = outputFolder + File.separator + name + PROCESSED+ timeStamp + EXT;
 		File fh = new File(outputFolder);
 		fh.mkdir();
 		
 		SourceInformation si = new SourceInformation(context.getSelectedConversionFile().getAbsolutePath(), context.getDatasetNames().get(0), lz);
 		lz.setMetadata(new SliceFromSeriesMetadata(si));
 		//TODO output path
 		
 		// Run
 		cc.setMonitor(context.getMonitor());
 		cc.setVisitor(info.getExecutionVisitor(full));
 		cc.setSeries(info.getOperationSeries());
 		service.execute(cc);
 	}
 	
 	protected ILazyDataset getLazyDataset(final File                 path, 
             final String               dsPath,
             final IConversionContext   context) throws Exception {
 		ILazyDataset lazyDataset = super.getLazyDataset(path, dsPath, context);
 		
 		if (lazyDataset != null) return lazyDataset;
 		
 
 		final IDataHolder   dh = LoaderFactory.getData(path.getAbsolutePath());
 		context.setSelectedH5Path(dsPath);
 		if (context.getMonitor()!=null) {
 			context.getMonitor().subTask("Process '"+path.getAbsolutePath());
 		}
 		return dh.getLazyDataset(dsPath);
 	}
 	
 	@Override
 	protected void convert(IDataset slice) throws Exception {
 		// does nothing, conversion is in the iterate method
 	}
 
 
 }
