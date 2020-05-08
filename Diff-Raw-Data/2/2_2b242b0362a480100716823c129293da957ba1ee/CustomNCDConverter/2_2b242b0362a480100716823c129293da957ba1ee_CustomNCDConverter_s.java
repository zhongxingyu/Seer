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
 import java.io.FileOutputStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import javax.measure.unit.NonSI;
 import javax.measure.unit.SI;
 import javax.measure.unit.UnitFormat;
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBElement;
 import javax.xml.bind.Marshaller;
 
 import org.apache.commons.lang.ArrayUtils;
 import org.cansas.cansas1d.FloatUnitType;
 import org.cansas.cansas1d.IdataType;
 import org.cansas.cansas1d.ObjectFactory;
 import org.cansas.cansas1d.SAScollimationType;
 import org.cansas.cansas1d.SASdataType;
 import org.cansas.cansas1d.SASdetectorType;
 import org.cansas.cansas1d.SASentryType;
 import org.cansas.cansas1d.SASentryType.Run;
 import org.cansas.cansas1d.SASinstrumentType;
 import org.cansas.cansas1d.SASrootType;
 import org.cansas.cansas1d.SASsampleType;
 import org.cansas.cansas1d.SASsourceType;
 import org.cansas.cansas1d.SAStransmissionSpectrumType;
 import org.dawnsci.conversion.converters.util.LocalServiceManager;
 import org.eclipse.dawnsci.analysis.api.conversion.IConversionContext;
 import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
 import org.eclipse.dawnsci.analysis.api.dataset.IErrorDataset;
 import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
 import org.eclipse.dawnsci.analysis.api.dataset.Slice;
 import org.eclipse.dawnsci.analysis.api.io.ScanFileHolderException;
 import org.eclipse.dawnsci.analysis.api.metadata.AxesMetadata;
 import org.eclipse.dawnsci.analysis.api.monitor.IMonitor;
 import org.eclipse.dawnsci.analysis.api.tree.Node;
 import org.eclipse.dawnsci.analysis.api.tree.Tree;
 import org.eclipse.dawnsci.analysis.dataset.impl.AggregateDataset;
 import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
 import org.eclipse.dawnsci.analysis.dataset.impl.DatasetUtils;
 import org.eclipse.dawnsci.analysis.dataset.impl.PositionIterator;
 import org.eclipse.dawnsci.hdf5.HierarchicalDataFactory;
 import org.eclipse.dawnsci.hdf5.IHierarchicalDataFile;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.io.ASCIIDataWithHeadingSaver;
 import uk.ac.diamond.scisoft.analysis.io.DataHolder;
 import uk.ac.diamond.scisoft.analysis.io.HDF5Loader;
 import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
 
 public class CustomNCDConverter extends AbstractConversion  {
 
 	private static final String ANGSTROM = "Angstrom";
 	private static final String DEGREES = "degrees";
 	private static final String INVERSE_ANGSTROM = "1/A";
 	private static final String INVERSE_NM = "1/nm";
 	
 	private static final Logger logger = LoggerFactory.getLogger(CustomNCDConverter.class);
 	private static final String DEFAULT_AXIS_NAME = "x";
 	private static final String DEFAULT_COLUMN_NAME = "Column";
 	private static final String DEFAULT_ERRORS_COLUMN_NAME = "Error";
 	private static final String DEFAULT_TITLE_NODE = "/entry1/title";
 	private static final String DEFAULT_SCAN_COMMAND_NODE = "/entry1/scan_command";
 	private static final String CANSAS_JAXB_CONTEXT = "org.cansas.cansas1d";
 	
 	private static final String ASCII_EXT = ".dat";
 	private static final String TOPAZ_EXT = ".xy";
 	
 	public static enum SAS_FORMAT { ASCII, ATSAS, CANSAS, TOPAZ };
 
 	public CustomNCDConverter(IConversionContext context) {
 		super(context);
 		final File dir = new File(context.getOutputPath());
 		dir.mkdirs();
 	}
 
 	@Override
 	protected void convert(IDataset slice) {
 		//we do our convert elsewhere
 	}
 	
 	@Override
 	protected void iterate(final ILazyDataset         lz, 
             final String               nameFrag,
             final IConversionContext   context) throws Exception {
 		
 		Object obj = context.getUserObject();
 		SAS_FORMAT exportFormat;
 		if (obj instanceof SAS_FORMAT) {
 			exportFormat = (SAS_FORMAT) obj;
 		} else {
 			exportFormat = SAS_FORMAT.ASCII;
 		}
 		
 		OutputBean bean = createBean(exportFormat, lz);
 
 		if (exportFormat.equals(SAS_FORMAT.CANSAS)) {
 			exportCanSAS(lz, nameFrag, context, bean);
 			return;
 		}
 		
 		String selFilePath = bean.filepath;
 		String titleNodeString = bean.title;
 		String commandNodeString = bean.command;
 		Dataset axis = bean.axis;
 		
 		//Set up position iterator (final 2 dimensions saved in a single file
 		int[] stop = lz.getShape();
 		boolean hasErrors = hasErrors(lz);
 		int iterDim;
 		int[] cutAxes;
 		if (stop.length == 1 || exportFormat.equals(SAS_FORMAT.ATSAS) || exportFormat.equals(SAS_FORMAT.TOPAZ)) {
 			iterDim = lz.getRank() - 1;
 			cutAxes = new int[] {lz.getRank() - 1};
 		} else {
 			iterDim = lz.getRank() - 2;
 			cutAxes = new int[] {lz.getRank() - 2, lz.getRank() - 1};
 		}
 
 		PositionIterator iterator = new PositionIterator(stop, cutAxes);
 
 		for (int i = 0 ; i < iterDim ; i++) {
 			stop[i] = 0;
 		}
 
 		int[] step = new int[stop.length];
 		for (int i = 0 ; i < step.length; i++) {
 			step[i] = 1;
 		}
 
 		//Make file header and column names
 		final String separator = System.getProperty("line.separator");
 		StringBuilder sb = new StringBuilder();
 		sb.append("# Diamond Light Source Ltd.");
 		sb.append(separator);
 		sb.append("# Non Crystalline Diffraction Results Export File");
 		if (selFilePath != null && !selFilePath.isEmpty()) {
 			sb.append(separator);
 			sb.append("# Data extracted from file: " + selFilePath);
 		}
 		sb.append(separator);
 		sb.append("# Dataset name: " + nameFrag);
 
 		if (titleNodeString != null && !titleNodeString.isEmpty()) {
 			sb.append(separator);
 			sb.append("# Title: " + titleNodeString);
 		}
 
 		if (commandNodeString != null && !commandNodeString.isEmpty()) {
 			sb.append(separator);
 			sb.append("# Scan command: " + commandNodeString);
 		}
 
 		List<String> headings = new ArrayList<String>();
 		String stringFormat = "%-12s";
 
 		if (axis != null) {
 			String axisUnit = bean.axisUnits;
 			String axisName = String.format(stringFormat, String.format("%s(%s)", axis.getName(), axisUnit));
 			headings.add(" ".concat(axisName));
 			if (axis.hasErrors()) {
 				headings.add(String.format(stringFormat, String.format("%s(%s)", axis.getName().concat("_errors"), axisUnit)));
 			}
 		}
 
 		if (stop.length == 1 || exportFormat.equals(SAS_FORMAT.ATSAS)) {
 			headings.add(String.format(stringFormat,DEFAULT_COLUMN_NAME));
 			if (hasErrors) {
 				headings.add(String.format(stringFormat,DEFAULT_ERRORS_COLUMN_NAME));
 			}
 		} else {
 			for (int i = 0; i< stop[iterDim]; i++) {
 				headings.add(String.format(stringFormat,DEFAULT_COLUMN_NAME + "_" + i));
 			}
 			if (hasErrors) {
 				for (int i = 0; i< stop[iterDim]; i++) {
 					headings.add(String.format(stringFormat,DEFAULT_ERRORS_COLUMN_NAME + "_" + i));
 				}
 			}
 		}
 
 		//Iterate over lazy dataset and save
 		while (iterator.hasNext()) {
 
 			int[] start = iterator.getPos();
 
 			for (int j = 0 ; j < iterDim ; j++) {
 				stop[j] = start[j]+1;
 			}
 
 			Slice[] slices = Slice.convertToSlice(start, stop, step);
 			Dataset data = DatasetUtils.convertToDataset(lz.getSlice(slices));
 			data = data.squeeze();
 
 			Dataset errors = null;
 			if (hasErrors) {
 				errors = DatasetUtils.cast(data.getError(), data.getDtype());
 			}
 
 			String nameSuffix = "";
 			String ext = ASCII_EXT;
 			if (exportFormat.equals(SAS_FORMAT.TOPAZ)) ext = TOPAZ_EXT;
 
 
 			if (!(Arrays.equals(lz.getShape(), data.getShape()))) {
 				nameSuffix = nameStringFromSliceArray(iterDim, slices);
 			}
 			data.setName(nameFrag + nameStringFromSliceArray(iterDim, slices));
 
 
 			//Check data suitable then concatenate axis with data
 			if (data.getRank() == 1) {
 				data.setShape(1,data.getShape()[0]);
 				if (hasErrors) {
 					errors.setShape(1,errors.getShape()[0]);
 				}
 			}
 
 			String header = sb.toString();
 
 			if (exportFormat.equals(SAS_FORMAT.TOPAZ)){
 				//Kill headers and headings
 				header = null;
 				headings = null;
 			}
 
 			String monitorLabel;
 			String fullName;
 			if (context.getSelectedConversionFile() != null) {
 				String pathToFolder = context.getOutputPath();
 				String fileName = buildFileName(context.getSelectedConversionFile().getAbsolutePath(),nameFrag);
 				monitorLabel = fileName;
 				fullName = pathToFolder + File.separator + fileName + nameSuffix +ext;
 			}
 			else {
 				//exportASCII without using filename
 				String pathToFolder = context.getOutputPath();
 				String fileName = buildFileNameGeneric("dataset", nameFrag);
 				fullName = pathToFolder + File.separator + fileName + nameSuffix +ext;
 				monitorLabel = lz.getName();
 			}
 			
 			Dataset[] fixed = fixDtypes(axis, data, errors);
 			axis = fixed[0];
 			data = fixed[1];
 			errors = fixed[2];
 			
 			exportASCII(axis, data, errors, fullName, header, headings);
 
 			if (context.getMonitor() != null) {
 				IMonitor mon = context.getMonitor();
 				if (mon.isCancelled()) {
 					return;
 				}
 				context.getMonitor().subTask(monitorLabel + nameSuffix);
 			}
 		}
 
 		if (context.getMonitor() != null) {
 			IMonitor mon = context.getMonitor();
 			mon.worked(1);
 		}
 	}
 	
 	private boolean hasErrors(ILazyDataset lz) {
 		if (lz instanceof AggregateDataset) {
 			return lz.getSlice(new Slice(0)).getError() != null ? true : false;
 		}
 		return lz.getError() != null ? true : false;
 	}
 	
 	private Dataset[] fixDtypes(Dataset axis, Dataset data, Dataset errors) {
 		int dataDtype = data.getDtype();
 		int axisDtype = 0;
 		if (axis != null) {
 			axisDtype = axis.getDtype();
 		}
 		int errorsDtype = 0;
 		if (errors != null) {
 			errorsDtype = errors.getDtype();
 		}
 		int largestDtype = Math.max(Math.max(dataDtype, axisDtype), errorsDtype);
 		if (data.getDtype() < largestDtype) {
 			data = improveLessPreciseData(data, largestDtype);
 		}
 		if (axis != null && axis.getDtype() < largestDtype) {
 			axis = improveLessPreciseData(axis, largestDtype);
 		}
 		if (errors != null && errors.getDtype() < largestDtype) {
 			errors = improveLessPreciseData(errors, largestDtype);
 		}
 		return new Dataset[]{axis, data, errors};
 	}
 	
 	private Dataset improveLessPreciseData(Dataset lessPreciseData, int dType) {
 		return lessPreciseData.cast(dType);
 	}
 
 	private void exportASCII(IErrorDataset axis, Dataset data, IDataset errors, String fullName, String header, List<String> headings) throws ScanFileHolderException {
 		String dataName = data.getName();
 		IDataset[] columns = new IDataset[] {DatasetUtils.transpose(data, null)};
 		if (axis != null) {
 			if (axis.hasErrors()) {
 				Dataset axisErrors = DatasetUtils.cast(axis.getError(), data.getDtype());
 				columns = (IDataset[]) ArrayUtils.addAll(new IDataset[]{axis, axisErrors}, columns);
 				
 			} else {
 				columns = (IDataset[]) ArrayUtils.addAll(new IDataset[]{axis}, columns);
 			}
 			
 		}
 		if (errors != null) {
 			columns = (IDataset[]) ArrayUtils.addAll(columns, new IDataset[]{DatasetUtils.transpose(errors, null)});
 		}
 		data = DatasetUtils.concatenate(columns, 1);
 		data.setName(dataName);
 		
 		DataHolder dh = new  DataHolder();
 		dh.addDataset(data.getName(), data);
 		
 		ASCIIDataWithHeadingSaver saver = new ASCIIDataWithHeadingSaver(fullName);
 		saver.setCellFormat("%-12.8g");
 		saver.setHeader(header);
 		saver.setHeadings(headings);
 		saver.saveFile(dh);
 	}
 	
 	private void exportCanSAS(final ILazyDataset         lz, 
             final String               nameFrag,
             final IConversionContext   context,
             final OutputBean           outputBean) throws Exception {
 		
 		String titleNodeString = outputBean.title;
 		String selFilePath = outputBean.filepath;
 		
 		//get the x axis if required
 		Dataset axis = outputBean.axis;
 		Dataset axisErrors = outputBean.axis.getError();
 		String axisUnits = outputBean.axisUnits;
 	
 		//Set up position iterator (final 2 dimensions saved in a single file
 		int[] stop = lz.getShape();
 		boolean hasErrors = (lz.getError() != null ? true : false);
 		int iterDim = lz.getRank() - 1;
 		int[] cutAxes = new int[] {lz.getRank() - 1};
 
 		PositionIterator iterator = new PositionIterator(stop, cutAxes);
 
 		for (int i = 0 ; i < iterDim ; i++) {
 			stop[i] = 0;
 		}
 
 		int[] step = new int[stop.length];
 		for (int i = 0 ; i < step.length; i++) {
 			step[i] = 1;
 		}
 
 		ObjectFactory of       = new ObjectFactory();
 		SASrootType   sasRoot  = of.createSASrootType();
 		SASsampleType sasSample  = of.createSASsampleType();
 
 		SASsourceType sasSource = of.createSASsourceType();
 		sasSource.setRadiation("x-ray");
 		SASdetectorType sasDetector = of.createSASdetectorType();
 		sasDetector.setName(nameFrag);
 		SAScollimationType sasCollimation = of.createSAScollimationType();
 
 		SASinstrumentType sasInstrument  = of.createSASinstrumentType();
 		sasInstrument.setName("Diamond Light Source Ltd.");
 		sasInstrument.setSASsource(sasSource);
 		sasInstrument.getSASdetector().add(sasDetector);
 		sasInstrument.getSAScollimation().add(sasCollimation);
 		SAStransmissionSpectrumType sasTransmission  = of.createSAStransmissionSpectrumType();
 
 		if (titleNodeString != null && !titleNodeString.isEmpty()) {
 			sasSample.setID(titleNodeString);
 		} else {
 			sasSample.setID("N/A");
 		}
 		
 		String pathToFolder = context.getOutputPath();
 		String fileName = buildFileName(context.getSelectedConversionFile().getAbsolutePath(),nameFrag);
 		String fullName = pathToFolder + File.separator + fileName + ".xml";
 
 		//Iterate over lazy dataset and save
 		while (iterator.hasNext()) {
 
 			SASentryType  sasEntry = of.createSASentryType();
 
 			int[] start = iterator.getPos();
 
 			for (int j = 0 ; j < iterDim ; j++) {
 				stop[j] = start[j]+1;
 			}
 
 			Slice[] slices = Slice.convertToSlice(start, stop, step);
 			Dataset data = DatasetUtils.convertToDataset(lz.getSlice(slices).squeeze());
 
 			Dataset errors = null;
 			if (hasErrors) {
 				errors = DatasetUtils.cast(data.getError(), data.getDtype());
 				errors.squeeze();
 			}
 
 			Run run = new Run();
 			String runName = "Frame"+ nameStringFromSliceArray(iterDim, slices);
 			run.setValue(runName);
 			sasEntry.getRun().add(run);
 
 			SASdataType sasData  = of.createSASdataType();
 
 			PositionIterator iter = new PositionIterator(data.getShape(), new int[] {});
 			while (iter.hasNext()) {
 				int[] idx = iter.getPos();
 				float val;
 
 				IdataType iData = of.createIdataType();
 				FloatUnitType I = of.createFloatUnitType();
 				val = data.getFloat(idx);
 				I.setValue(val);
 				I.setUnit("a.u.");
 				iData.setI(I);
 				if (axis != null) {
 					FloatUnitType Q = of.createFloatUnitType();
 					val = axis.getFloat(idx);
 					Q.setValue(val);
 					Q.setUnit(axisUnits);
 					iData.setQ(Q);
 				}
 				if (errors != null) {
 					FloatUnitType devI = of.createFloatUnitType();
 					val = errors.getFloat(idx);
 					devI.setValue(val);
 					devI.setUnit("a.u.");
 					iData.setIdev(devI);
 				}
 				if (axisErrors != null) {
 					FloatUnitType devQ = of.createFloatUnitType();
 					val = axisErrors.getFloat(idx);
 					devQ.setValue(val);
 					devQ.setUnit(axisUnits);
 					iData.setQdev(devQ);
 				}
 				sasData.getIdata().add(iData);
 			}
 
 			sasEntry.setTitle(data.getName());
 			sasEntry.getSASdata().add(sasData);
 			sasEntry.setSASsample(sasSample);
 			sasEntry.getSAStransmissionSpectrum().add(sasTransmission);
 			sasEntry.setSASinstrument(sasInstrument);
 			if (selFilePath != null && !selFilePath.isEmpty()) {
 				sasEntry.getSASnote().add(selFilePath);
 			}
 
 			sasRoot.getSASentry().add(sasEntry);
 		}
 		JAXBElement<SASrootType> jabxSASroot = of.createSASroot(sasRoot);
 
 		JAXBContext jc = JAXBContext.newInstance(CANSAS_JAXB_CONTEXT);
 		Marshaller m = jc.createMarshaller();
		m.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, "http://www.cansas.org/formats/1.1/cansas1d.xsd");		
 		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
 		m.marshal(jabxSASroot, new FileOutputStream(fullName));
 
 		if (context.getMonitor() != null) {
 			IMonitor mon = context.getMonitor();
 			mon.worked(1);
 		}
 	}
 	
 	private String getAxisDatasetName(String axisDatasetName) {
 		
 		if (!(axisDatasetName.contains("/"))) {
 			return DEFAULT_AXIS_NAME;
 		} else {
 			int pos = axisDatasetName.lastIndexOf("/");
 			return axisDatasetName.substring(pos+1, axisDatasetName.length());
 		}
 	}
 
 	private String nameStringFromSliceArray(int iterDim, Slice[] slices) {
 
 		StringBuilder t = new StringBuilder();
 		t.append('_');
 		for (int idx = 0; idx < iterDim; idx++) {
 			Slice s = slices[idx];
 			t.append(s != null ? s.toString() : "");
 			t.append('_');
 		}
 		t.deleteCharAt(t.length()-1);
 		return t.toString();
 	}
 	
 	private Dataset getAxis(String datasetName, File path) {
 		
 		Dataset data = null;
 		try {
 			data = DatasetUtils.convertToDataset(LocalServiceManager.getLoaderService().getDataset(path.getAbsolutePath(), datasetName, null));
 			//expand so the concatenation works later
 			data.setShape(data.getShape()[0],1);
 			data.setName(getAxisDatasetName(datasetName));
 		} catch (Exception e) {
 			logger.warn("Couldn't get dataset: " + datasetName);
 		}
 		return data;
 	}
 	
 	private String getAxisUnit(String datasetName, File path) {
 		try {
 			Tree tree = new HDF5Loader(path.getAbsolutePath()).loadTree();
 			Node node = tree.findNodeLink(datasetName).getDestination();
 			String units = null;
 			if (node.containsAttribute("units")) {
 				units = node.getAttribute("units").getFirstElement();
 			} else if (node.containsAttribute("unit")) {
 				units = node.getAttribute("unit").getFirstElement();
 			}
 			if (units != null) {
 				UnitFormat unitFormat = UnitFormat.getUCUMInstance();
 				String angstrom = unitFormat.format(NonSI.ANGSTROM.inverse());
 				String nanometer = unitFormat.format(SI.NANO(SI.METER)
 						.inverse());
 				String angle = unitFormat.format(NonSI.DEGREE_ANGLE);
 				String dspace = unitFormat.format(NonSI.ANGSTROM);
 				if (units.equals(nanometer)) {
 					return INVERSE_NM;
 				} else if (units.equals(angstrom)) {
 					return INVERSE_ANGSTROM;
 				} else if (units.equals(angle)) {
 					return DEGREES;
 				} else if (units.equals(dspace)) {
 					return ANGSTROM;
 				}
 			}
 		} catch (ScanFileHolderException e) {
 			logger.warn("Unit information for axis dataset {} not found", datasetName);
 		}
 		return "a.u.";
 	}
 	
 	private String buildFileName(String pathToOriginal, String datasetName) {
 		
 		String name = new File(pathToOriginal).getName();
 		return buildFileNameGeneric(name, datasetName);
 	}
 	
 	private String buildFileNameGeneric(String name, String datasetName) {
 		int index = name.lastIndexOf('.');
 		if (index != -1) {
 			name = name.substring(0, index);
 		}
 		
 		if (datasetName.contains("processing")) {
 			String trimmed = datasetName.replaceAll("(.*_processing/)", "");
 			trimmed = trimmed.replaceAll("/", "_");
 			name = name + "_" + trimmed;
 		}
 		return name;
 	}
 	
 	private String getTitleNodeString(IHierarchicalDataFile hdf5Reader) throws Exception {
 		ncsa.hdf.object.Dataset titleData = (ncsa.hdf.object.Dataset) hdf5Reader.getData(DEFAULT_TITLE_NODE);
 		String[] str = null;
 		if (titleData != null) {
 			str = (String[]) titleData.getData();
 			String title = str[0];
 			return title;
 		}
 		return "";
 	}
 	
 	private String getCommandNodeString(IHierarchicalDataFile hdf5Reader) throws Exception {
 		ncsa.hdf.object.Dataset scanCommandData = (ncsa.hdf.object.Dataset) hdf5Reader.getData(DEFAULT_SCAN_COMMAND_NODE);
 		String[] str = null;
 		if (scanCommandData != null) {
 			str = (String[])scanCommandData.getData();
 			String scanCommand = str[0];
 			return scanCommand;
 		}
 		return "";
 	}
 	
 	public OutputBean createBean(SAS_FORMAT exportFormat, ILazyDataset lz) throws Exception {
 		OutputBean outputBean = new OutputBean();
 		if (context.getSelectedConversionFile() != null) {
 			IHierarchicalDataFile hdf5Reader = null;
 			try {
 				outputBean.filepath = context.getSelectedConversionFile().getAbsolutePath();
 				hdf5Reader = HierarchicalDataFactory.getReader(outputBean.filepath);
 				outputBean.title = getTitleNodeString(hdf5Reader);
 				outputBean.command = getCommandNodeString(hdf5Reader);
 			} catch (Exception e) {
 				logger.error("Exception while getting title and command information", e);
 			}
 			finally {
 				if (hdf5Reader != null) {
 					hdf5Reader.close();
 				}
 			}
 		}
 		
 		if (context.getAxisDatasetName() != null) {
 			outputBean.axis = getAxis(context.getAxisDatasetName(), context.getSelectedConversionFile());
 			// ATSAS ASCII format doesn't support axis errors
 			if (outputBean.axis != null && outputBean.axis.hasErrors() && exportFormat.equals(SAS_FORMAT.ATSAS)) {
 				outputBean.axis.setError(null);
 			}
 			outputBean.axisUnits = getAxisUnit(context.getAxisDatasetName(), context.getSelectedConversionFile());
 		}
 		else {
 			ILazyDataset set;
 			if (lz instanceof AggregateDataset) {
 				set = lz.getSlice(new Slice(0, 1));
 			}
 			else {
 				set = lz;
 			}
 			List<AxesMetadata> axes = set.getMetadata(AxesMetadata.class);
 			for (AxesMetadata axis : axes) {
 				for (ILazyDataset a: axis.getAxes()) {
 					if (a != null) {
 						a = ((Dataset) a).transpose();
 						if (a != null) {
 							if (a.getName().equals("q")) {
 								outputBean.axis = (Dataset) a;
 								outputBean.axisUnits = INVERSE_ANGSTROM;
 							}
 							else if (a.getName().equals("d-spacing")) {
 								outputBean.axis = (Dataset) a;
 								outputBean.axisUnits = INVERSE_ANGSTROM;
 							}
 							else if (a.getName().equals("angle")) {
 								outputBean.axis = (Dataset) a;
 								outputBean.axisUnits = DEGREES;
 							}
 							else if (a.getName().equals("2theta")) {
 								outputBean.axis = (Dataset) a;
 							}
 							else if (a.getName().equals("pixel")) {
 								outputBean.axis = (Dataset) a;
 							}
 						}
 						break;
 					}
 				}
 			}
 			
 		}
 		return outputBean;
 	}
 	
 	private class OutputBean {
 		String filepath;
 		String title;
 		String command;
 		Dataset axis;
 		String axisUnits;
 	}
 }
