 /*
  * Copyright 2011 Diamond Light Source Ltd.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package uk.ac.diamond.scisoft.analysis;
 
 import gda.analysis.io.ScanFileHolderException;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.util.TreeMap;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractCompoundDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
 import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.IntegerDataset;
 import uk.ac.diamond.scisoft.analysis.hdf5.HDF5File;
 import uk.ac.diamond.scisoft.analysis.io.DataHolder;
 import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
 import uk.ac.diamond.scisoft.analysis.io.RawBinarySaver;
 import uk.ac.diamond.scisoft.analysis.plotserver.AxisMapBean;
 import uk.ac.diamond.scisoft.analysis.plotserver.DataBean;
 import uk.ac.diamond.scisoft.analysis.plotserver.DataBeanException;
 import uk.ac.diamond.scisoft.analysis.plotserver.DataSetWithAxisInformation;
 import uk.ac.diamond.scisoft.analysis.plotserver.GuiBean;
 import uk.ac.diamond.scisoft.analysis.plotserver.GuiParameters;
 import uk.ac.diamond.scisoft.analysis.plotserver.GuiPlotMode;
 
 /**
  * Normal implementation of {@link ISDAPlotter} used by delegator class {@link SDAPlotter} 
  */
 public class SDAPlotterImpl implements ISDAPlotter {
 	private static final Logger logger = LoggerFactory.getLogger(SDAPlotterImpl.class);
 
 	private static ISDAPlotter instance;
 	/**
 	 * Get the default or "normal" instance of the SDAPlotter to use.
 	 * @return instance of SDAPlotter
 	 */
 	public synchronized static ISDAPlotter getDefaultInstance() {
 		if (instance == null) {
 			instance = new SDAPlotterImpl();
 		}
 		return instance;
 	}
 
 	/**
 	 * All locations within SDAPlotter that need a PlotService should get it with this method. This method will collect
 	 * a PlotService from the provider.
 	 * <p>
 	 * Note this method exists to funnel all accesses to the PlotService via here, its purpose is to allow subclasses of
 	 * SDAPlotterImpl to provide their own PlotService and keep all dependencies on a static method calls to one place.
 	 * 
 	 * @return PlotService
 	 */
 	protected PlotService getPlotService() {
 		return PlotServiceProvider.getPlotService();
 	}
 
 	/** 
 	 * Not strictly private to enable other implementation of ISDAPlotter to
 	 * extend this one, such as versions used in test.
 	 */
 	protected SDAPlotterImpl() {
 	}
 
 	private boolean isDataND(IDataset data, int dim) {
 		return data.getRank() == dim;
 	}
 
 	private IDataset[] validateXAxis(final IDataset xAxis, final IDataset... yAxes) {
 		if (xAxis == null) {
 			if (yAxes == null || yAxes.length == 0) {
 				logger.error("No datasets specified");
 				throw new IllegalArgumentException("No datasets specified");
 			}
 			return new IDataset[] { AbstractDataset.arange(yAxes[0].getSize(), AbstractDataset.INT32) };
 		}
 
 		return new IDataset[] { xAxis };
 	}
 
 	private IDataset[] validateXAxes(final IDataset[] xAxes, final IDataset... yAxes) {
 		if (xAxes == null) {
 			return validateXAxis(null, yAxes);
 		}
 
 		return xAxes;
 	}
 
 	@Override
 	public void plot(String plotName, final IDataset yAxis) throws Exception {
 		plot(plotName, (String) null, yAxis);
 	}
 
 	@Override
 	public void plot(String plotName, final String title, final IDataset yAxis) throws Exception {
 		plot(plotName, title, null, yAxis);
 	}
 
 	@Override
 	public void plot(String plotName, final IDataset xAxis, final IDataset yAxis) throws Exception {
 		plot(plotName, (String) null, xAxis, yAxis);
 	}
 
 	@Override
 	public void plot(String plotName, final String title, final IDataset xAxis, final IDataset yAxis) throws Exception {
 		lplot(plotName, title, validateXAxis(xAxis, yAxis), null, new IDataset[] { yAxis }, false);
 	}
 
 	@Override
 	public void plot(String plotName, final IDataset xAxis, final IDataset xAxis2, final IDataset yAxis) throws Exception {
 		lplot(plotName, null, validateXAxis(xAxis, yAxis), xAxis2, new IDataset[] { yAxis }, false);
 	}
 
 	@Override
 	public void plot(String plotName, final IDataset xAxis, final IDataset[] yAxes) throws Exception {
 		plot(plotName, (String) null, xAxis, yAxes);
 	}
 
 	@Override
 	public void plot(String plotName, String title, final IDataset xAxis, IDataset[] yAxes) throws Exception {
 		plot(plotName, title, validateXAxis(xAxis, yAxes), yAxes);
 	}
 
 	@Override
 	public void plot(String plotName, IDataset[] xAxes, final IDataset[] yAxes) throws Exception {
 		plot(plotName, null, xAxes, yAxes);
 	}
 
 	@Override
 	public void plot(String plotName, final String title, IDataset[] xAxes, IDataset[] yAxes) throws Exception {
 		lplot(plotName, title, validateXAxes(xAxes, yAxes), null, yAxes, false);
 	}
 
 	@Override
 	public void updatePlot(String plotName, final IDataset yAxis) throws Exception {
 		updatePlot(plotName, null, yAxis);
 	}
 
 	@Override
 	public void updatePlot(String plotName, final IDataset xAxis, final IDataset yAxis) throws Exception {
 		updatePlot(plotName, xAxis, null, yAxis);
 	}
 
 	@Override
 	public void updatePlot(String plotName, final IDataset xAxis, final IDataset xAxis2, final IDataset yAxis) throws Exception {
 		lplot(plotName, null, validateXAxis(xAxis, yAxis), xAxis2, new IDataset[] { yAxis }, true);
 	}
 
 	@Override
 	public void updatePlot(String plotName, final IDataset xAxis, IDataset[] yAxes) throws Exception {
 		updatePlot(plotName, validateXAxis(xAxis, yAxes), yAxes);
 	}
 
 	@Override
 	public void updatePlot(String plotName, IDataset[] xAxes, IDataset[] yAxes) throws Exception {
 		lplot(plotName, null, validateXAxes(xAxes, yAxes), null, yAxes, true);
 	}
 
 	/**
 	 * Plot line(s) in named view
 	 * @param plotName
 	 * @param title (can be null)
 	 * @param xAxes
 	 * @param xAxis2
 	 * @param yAxes
 	 * @param updateMode if true, keep zoom settings
 	 * @throws Exception
 	 */
 	private void lplot(final String plotName, final String title, IDataset[] xAxes, final IDataset xAxis2, IDataset[] yAxes, final boolean updateMode) throws Exception {
 		for (IDataset x : xAxes) {
 			if (!isDataND(x, 1)) {
 				logger.error("Input x dataset has incorrect rank: it has {} dimensions when it should be 1",
 						x.getRank());
 				throw new Exception("Input x dataset has incorrect rank: it should be 1");
 			}
 		}
 		for (IDataset y : yAxes) {
 			if (!isDataND(y, 1)) {
 				logger.error("Input y dataset has incorrect rank: it has {} dimensions when it should be 1",
 						y.getRank());
 				throw new Exception("Input y dataset has incorrect rank: it should be 1");
 			}
 		}
 
 		logger.info("Plot sent to {}", plotName);
 
 		// Create the beans to transfer the data
 		DataBean dataBean = new DataBean(GuiPlotMode.ONED);
 		if (xAxes.length == 1) {
 			dataBean.addAxis(AxisMapBean.XAXIS, xAxes[0]);
 			for (int i = 0; i < yAxes.length; i++) {
 				try {
 					dataBean.addData(DataSetWithAxisInformation.createAxisDataSet(yAxes[i]));
 				} catch (DataBeanException e) {
 					logger.error("Problem adding data to bean as axis key does not exist");
 					e.printStackTrace();
 				}
 			}
 		} else {
 			if (xAxes.length != yAxes.length)
 				throw new IllegalArgumentException("# xAxis does not match # yAxis");
 			for (int i = 0; i < xAxes.length; i++) {
				String axisStr = (i == 0) ? AxisMapBean.XAXIS : AxisMapBean.XAXIS + i;
 				dataBean.addAxis(axisStr, xAxes[i]);
 				// now add it to the plot data
 				try {
 					dataBean.addData(DataSetWithAxisInformation.createAxisDataSet(yAxes[i], axisStr));
 				} catch (DataBeanException e) {
 					logger.error("Problem adding data to bean as axis key does not exist");
 					e.printStackTrace();
 				}
 			}
 		}
 
 		if (xAxis2 != null)
 			dataBean.addAxis(AxisMapBean.XAXIS2, xAxis2);
 
 		if (updateMode)
 			dataBean.putGuiParameter(GuiParameters.PLOTOPERATION, "UPDATE");
 
 		if (title != null)
 			dataBean.putGuiParameter(GuiParameters.TITLE, title);
 
 		sendBeansToServer(plotName, dataBean, null);
 	}
 
 	/**
 	 * Allows the plotting of an image to the defined view
 	 * 
 	 * @param plotName
 	 * @param imageFileName
 	 * @throws Exception
 	 */
 	@Override
 	public void imagePlot(String plotName, String imageFileName) throws Exception {
 
 		DataHolder dh = null;
 
 		try {
 			// This is the bit that can take a while.
 			dh = LoaderFactory.getData(imageFileName, null);
 
 		} catch (Exception ne) {
 			logger.error("Cannot load file " + imageFileName, ne);
 			throw ne;
 		}
 
 		try {
 			IDataset dataSet = dh.getDataset(0);
 			dataSet.setName(imageFileName);
 			imagePlot(plotName, dataSet);
 		} catch (Exception e) {
 			logger.error("Cannot plot non-image file from  " + imageFileName, e);
 			throw e;
 		}
 	}
 
 	/**
 	 * Allows the plotting of an image to the defined view
 	 * 
 	 * @param plotName
 	 * @param image
 	 * @throws Exception
 	 */
 	@Override
 	public void imagePlot(String plotName, IDataset image) throws Exception {
 		imagePlot(plotName, null, null, image);
 	}
 
 	/**
 	 * Allows the plotting of an image to the defined view
 	 * 
 	 * @param plotName
 	 * @param xAxis
 	 *            can be null
 	 * @param yAxis
 	 *            can be null
 	 * @param image
 	 * @throws Exception
 	 */
 
 	@Override
 	public void imagePlot(String plotName, IDataset xAxis, IDataset yAxis, IDataset image) throws Exception {
 		if (!isDataND(image, 2)) {
 			logger.error("Input dataset has incorrect rank: it has {} dimensions when it should be 2", image.getRank());
 			throw new Exception("Input dataset has incorrect rank: it should be 2");
 		}
 		if (xAxis != null && !isDataND(xAxis, 1)) {
 			String msg = String.format("X axis dataset has incorrect rank: it has %d dimensions when it should be 1",
 					xAxis.getRank());
 			logger.error(msg);
 			throw new Exception(msg);
 		}
 
 		if (yAxis != null && !isDataND(yAxis, 1)) {
 			String msg = String.format("Y axis dataset has incorrect rank: it has %d dimensions when it should be 1",
 					yAxis.getRank());
 			logger.error(msg);
 			throw new Exception(msg);
 		}
 
 		DataBean dataBean = new DataBean(GuiPlotMode.TWOD);
 		DataSetWithAxisInformation axisData = new DataSetWithAxisInformation();
 		AxisMapBean amb = new AxisMapBean(AxisMapBean.DIRECT);
 		axisData.setAxisMap(amb);
 		axisData.setData(image);
 		try {
 			dataBean.addData(axisData);
 		} catch (DataBeanException e) {
 			e.printStackTrace();
 		}
 
 		if (xAxis != null) {
 			dataBean.addAxis(AxisMapBean.XAXIS, xAxis);
 		}
 		if (yAxis != null) {
 			dataBean.addAxis(AxisMapBean.YAXIS, yAxis);
 		}
 		
 		sendBeansToServer(plotName, dataBean, null);
 	}
 
 	/**
 	 * Allows the plotting of images to the defined view
 	 * 
 	 * @param plotName
 	 * @param images
 	 * @throws Exception
 	 */
 	@Override
 	public void imagesPlot(String plotName, IDataset[] images) throws Exception {
 		imagesPlot(plotName, null, null, images);
 	}
 
 	/**
 	 * Allows the plotting of images to the defined view
 	 * 
 	 * @param plotName
 	 * @param xAxis
 	 *            can be null
 	 * @param yAxis
 	 *            can be null
 	 * @param images
 	 * @throws Exception
 	 */
 	@Override
 	public void imagesPlot(String plotName, IDataset xAxis, IDataset yAxis, IDataset[] images) throws Exception {
 		if (!isDataND(images[0], 2)) {
 			logger.error("Input dataset has incorrect rank: it has {} dimensions when it should be 2",
 					images[0].getRank());
 			throw new Exception("Input dataset has incorrect rank: it should be 2");
 		}
 		if (xAxis != null && !isDataND(xAxis, 1)) {
 			String msg = String.format("X axis dataset has incorrect rank: it has %d dimensions when it should be 1",
 					xAxis.getRank());
 			logger.error(msg);
 			throw new Exception(msg);
 		}
 
 		if (yAxis != null && !isDataND(yAxis, 1)) {
 			String msg = String.format("Y axis dataset has incorrect rank: it has %d dimensions when it should be 1",
 					yAxis.getRank());
 			logger.error(msg);
 			throw new Exception(msg);
 		}
 		DataBean dataBean = new DataBean(GuiPlotMode.MULTI2D);
 		for (int i = 0; i < images.length; i++) {
 			DataSetWithAxisInformation axisData = new DataSetWithAxisInformation();
 			AxisMapBean amb = new AxisMapBean(AxisMapBean.DIRECT);
 			axisData.setAxisMap(amb);
 			axisData.setData(images[i]);
 			try {
 				dataBean.addData(axisData);
 			} catch (DataBeanException e) {
 				e.printStackTrace();
 			}
 		}
 		if (xAxis != null) {
 			dataBean.addAxis(AxisMapBean.XAXIS, xAxis);
 		}
 		if (yAxis != null) {
 			dataBean.addAxis(AxisMapBean.YAXIS, yAxis);
 		}
 
 		sendBeansToServer(plotName, dataBean, null);
 	}
 
 	/**
 	 * Allows plotting of points of given size on a 2D grid
 	 * 
 	 * @param plotName
 	 * @param xCoords
 	 * @param yCoords
 	 * @param size
 	 * @throws Exception
 	 */
 	@Override
 	public void scatter2DPlot(String plotName, IDataset xCoords, IDataset yCoords, int size) throws Exception {
 		if (xCoords != null && !isDataND(xCoords, 1) || xCoords == null) {
 			String msg = String.format("X axis dataset is wrong format or null");
 			logger.error(msg);
 			throw new Exception(msg);
 		}
 
 		IntegerDataset sizes = new IntegerDataset(xCoords.getSize());
 		sizes.fill(size);
 
 		scatter2DPlot(plotName, xCoords, yCoords, sizes);
 	}
 
 	/**
 	 * Allows plotting of multiple sets of points of given sizes on a 2D grid
 	 * 
 	 * @param plotName
 	 * @param coordPairs
 	 * @param sizes
 	 * @throws Exception
 	 */
 	@Override
 	public void scatter2DPlot(String plotName, AbstractCompoundDataset[] coordPairs, int[] sizes)
 			throws Exception {
 		IntegerDataset[] pSizes = new IntegerDataset[sizes.length];
 		for (int i = 0; i < sizes.length; i++) {
 			pSizes[i] = new IntegerDataset(coordPairs[i].getShape()[0]);
 			pSizes[i].fill(sizes[i]);
 		}
 
 		scatter2DPlot(plotName, coordPairs, pSizes);
 	}
 
 	/**
 	 * Allows plotting of multiple sets of points of given sizes on a 2D grid
 	 * 
 	 * @param plotName
 	 * @param coordPairs
 	 * @param sizes
 	 * @throws Exception
 	 */
 	@Override
 	public void scatter2DPlot(String plotName, AbstractCompoundDataset[] coordPairs, IDataset[] sizes)
 			throws Exception {
 		if (coordPairs.length != sizes.length) {
 			String msg = String.format("# of coordPairs does not match # of sizes (%d != %d)", coordPairs.length,
 					sizes.length);
 			logger.error(msg);
 			throw new Exception(msg);
 		}
 		DataBean dataBean = new DataBean(GuiPlotMode.SCATTER2D);
 		dataBean.putGuiParameter(GuiParameters.PLOTOPERATION, "NADA");
 		GuiBean guiBean = new GuiBean();
 		guiBean.put(GuiParameters.PLOTMODE, GuiPlotMode.SCATTER2D);
 		for (int i = 0; i < sizes.length; i++) {
 			AbstractCompoundDataset coordData = coordPairs[i];
 			if (coordData.getElementsPerItem() != 2) {
 				String msg = String.format("# of elements of coordPair does not equal two it is %d",
 						coordData.getElementsPerItem());
 				logger.error(msg);
 				throw new Exception(msg);
 			}
 			if (coordData.getShape().length != 1) {
 				String msg = String.format("shape of coordpair does not match (expected one but got %d)",
 						coordData.getShape().length);
 				logger.error(msg);
 				throw new Exception(msg);
 			}
 			AbstractDataset xCoord = coordData.getElements(0);
 			AbstractDataset yCoord = coordData.getElements(1);
 			DataSetWithAxisInformation axisData = new DataSetWithAxisInformation();
 			AxisMapBean amb = new AxisMapBean(AxisMapBean.DIRECT);
 			axisData.setAxisMap(amb);
 			axisData.setData(sizes[i]);
 			dataBean.addData(axisData);
 			dataBean.addAxis(AxisMapBean.XAXIS + Integer.toString(i), xCoord);
 			dataBean.addAxis(AxisMapBean.YAXIS + Integer.toString(i), yCoord);
 		}
 		sendBeansToServer(plotName, dataBean, guiBean);
 	}
 
 	/**
 	 * Allows plotting of points of given sizes on a 2D grid
 	 * 
 	 * @param plotName
 	 * @param xCoords
 	 * @param yCoords
 	 * @param sizes
 	 * @throws Exception
 	 */
 	@Override
 	public void scatter2DPlot(String plotName, IDataset xCoords, IDataset yCoords, IDataset sizes)
 			throws Exception {
 		if (xCoords != null && !isDataND(xCoords, 1)) {
 			String msg = String.format("X coords dataset has incorrect rank: it has %d dimensions when it should be 1",
 					xCoords.getRank());
 			logger.error(msg);
 			throw new Exception(msg);
 		}
 
 		if (yCoords != null && !isDataND(yCoords, 1)) {
 			String msg = String.format("Y coords dataset has incorrect rank: it has %d dimensions when it should be 1",
 					yCoords.getRank());
 			logger.error(msg);
 			throw new Exception(msg);
 		}
 
 		DataBean dataBean = new DataBean(GuiPlotMode.SCATTER2D);
 		dataBean.putGuiParameter(GuiParameters.PLOTOPERATION, "NADA");
 		DataSetWithAxisInformation axisData = new DataSetWithAxisInformation();
 		AxisMapBean amb = new AxisMapBean(AxisMapBean.DIRECT);
 		axisData.setAxisMap(amb);
 		axisData.setData(sizes);
 		try {
 			dataBean.addData(axisData);
 		} catch (DataBeanException e) {
 			e.printStackTrace();
 		}
 
 		if (xCoords != null) {
 			dataBean.addAxis(AxisMapBean.XAXIS + "0", xCoords);
 		}
 		if (yCoords != null) {
 			dataBean.addAxis(AxisMapBean.YAXIS + "0", yCoords);
 		}
 
 		sendBeansToServer(plotName, dataBean, null);
 	}
 
 	// temporary(?) fix for plotting over too quickly when some additional points are omitted
 	private static final int REST_BETWEEN_PLOTOVERS = 6;
 
 	@Override
 	public void scatter2DPlotOver(String plotName, IDataset xCoords, IDataset yCoords, IDataset sizes)
 			throws Exception {
 		if (xCoords != null && !isDataND(xCoords, 1) || xCoords == null) {
 			String msg = String.format("X coords dataset has incorrect rank or is null");
 			logger.error(msg);
 			throw new Exception(msg);
 		}
 
 		if (yCoords != null && !isDataND(yCoords, 1) || yCoords == null) {
 			String msg = String.format("Y coords dataset has incorrect rank or is null");
 			logger.error(msg);
 			throw new Exception(msg);
 		}
 
 		Thread.sleep(REST_BETWEEN_PLOTOVERS);
 
 		DataBean dataBean = new DataBean(GuiPlotMode.SCATTER2D);
 		DataSetWithAxisInformation axisData = new DataSetWithAxisInformation();
 		AxisMapBean amb = new AxisMapBean(AxisMapBean.DIRECT);
 		axisData.setAxisMap(amb);
 		dataBean.putGuiParameter(GuiParameters.PLOTOPERATION, "UPDATE");
 		axisData.setData(sizes);
 		try {
 			dataBean.addData(axisData);
 		} catch (DataBeanException e) {
 			e.printStackTrace();
 		}
 
 		dataBean.addAxis(AxisMapBean.XAXIS + "0", xCoords);
 		dataBean.addAxis(AxisMapBean.YAXIS + "0", yCoords);
 
 		sendBeansToServer(plotName, dataBean, null);
 	}
 
 	@Override
 	public void scatter2DPlotOver(String plotName, IDataset xCoords, IDataset yCoords, int size)
 			throws Exception {
 		if (xCoords != null && !isDataND(xCoords, 1) || xCoords == null) {
 			String msg = String.format("X coords dataset has incorrect rank or is null");
 			logger.error(msg);
 			throw new Exception(msg);
 		}
 
 		IntegerDataset sizes = new IntegerDataset(xCoords.getSize());
 		sizes.fill(size);
 
 		scatter2DPlotOver(plotName, xCoords, yCoords, sizes);
 	}
 
 	/**
 	 * Allows plotting of points of given size on a 2D grid
 	 * 
 	 * @param plotName
 	 * @param xCoords
 	 * @param yCoords
 	 * @param zCoords
 	 * @param size
 	 * @throws Exception
 	 */
 	@Override
 	public void scatter3DPlot(String plotName, IDataset xCoords, IDataset yCoords, IDataset zCoords, int size)
 			throws Exception {
 		if (xCoords != null && !isDataND(xCoords, 1) || xCoords == null) {
 			String msg = String.format("X axis dataset is wrong format or null");
 			logger.error(msg);
 			throw new Exception(msg);
 		}
 
 		IntegerDataset sizes = new IntegerDataset(xCoords.getSize());
 		sizes.fill(size);
 
 		scatter3DPlot(plotName, xCoords, yCoords, zCoords, sizes);
 	}
 
 	/**
 	 * Allows plotting of points of given sizes on a 3D volume
 	 * 
 	 * @param plotName
 	 * @param xCoords
 	 * @param yCoords
 	 * @param sizes
 	 * @throws Exception
 	 */
 	@Override
 	public void scatter3DPlot(String plotName, IDataset xCoords, IDataset yCoords, IDataset zCoords,
 			IDataset sizes) throws Exception {
 		if (xCoords != null && !isDataND(xCoords, 1)) {
 			String msg = String.format("X coords dataset has incorrect rank: it has %d dimensions when it should be 1",
 					xCoords.getRank());
 			logger.error(msg);
 			throw new Exception(msg);
 		}
 
 		if (yCoords != null && !isDataND(yCoords, 1)) {
 			String msg = String.format("Y coords dataset has incorrect rank: it has %d dimensions when it should be 1",
 					yCoords.getRank());
 			logger.error(msg);
 			throw new Exception(msg);
 		}
 
 		if (zCoords != null && !isDataND(zCoords, 1)) {
 			String msg = String.format("Z coords dataset has incorrect rank: it has %d dimensions when it should be 1",
 					zCoords.getRank());
 			logger.error(msg);
 			throw new Exception(msg);
 		}
 
 		DataBean dataBean = new DataBean(GuiPlotMode.SCATTER3D);
 		dataBean.putGuiParameter(GuiParameters.PLOTOPERATION, "NADA");
 		DataSetWithAxisInformation axisData = new DataSetWithAxisInformation();
 		AxisMapBean amb = new AxisMapBean(AxisMapBean.DIRECT);
 		axisData.setAxisMap(amb);
 		axisData.setData(sizes);
 		try {
 			dataBean.addData(axisData);
 		} catch (DataBeanException e) {
 			e.printStackTrace();
 		}
 
 		if (xCoords != null) {
 			dataBean.addAxis(AxisMapBean.XAXIS, xCoords);
 		}
 		if (yCoords != null) {
 			dataBean.addAxis(AxisMapBean.YAXIS, yCoords);
 		}
 		if (zCoords != null) {
 			dataBean.addAxis(AxisMapBean.ZAXIS, zCoords);
 		}
 
 		sendBeansToServer(plotName, dataBean, null);
 	}
 
 	@Override
 	public void scatter3DPlotOver(String plotName, IDataset xCoords, IDataset yCoords, IDataset zCoords, int size)
 			throws Exception {
 		if (xCoords != null && !isDataND(xCoords, 1) || xCoords == null) {
 			String msg = String.format("X axis dataset is wrong format or null");
 			logger.error(msg);
 			throw new Exception(msg);
 		}
 
 		IntegerDataset sizes = new IntegerDataset(xCoords.getSize());
 		sizes.fill(size);
 
 		scatter3DPlotOver(plotName, xCoords, yCoords, zCoords, sizes);
 	}
 
 	@Override
 	public void scatter3DPlotOver(String plotName, IDataset xCoords, IDataset yCoords, IDataset zCoords,
 			IDataset sizes) throws Exception {
 		if (xCoords != null && !isDataND(xCoords, 1)) {
 			String msg = String.format("X coords dataset has incorrect rank: it has %d dimensions when it should be 1",
 					xCoords.getRank());
 			logger.error(msg);
 			throw new Exception(msg);
 		}
 
 		if (yCoords != null && !isDataND(yCoords, 1)) {
 			String msg = String.format("Y coords dataset has incorrect rank: it has %d dimensions when it should be 1",
 					yCoords.getRank());
 			logger.error(msg);
 			throw new Exception(msg);
 		}
 
 		if (zCoords != null && !isDataND(zCoords, 1)) {
 			String msg = String.format("Z coords dataset has incorrect rank: it has %d dimensions when it should be 1",
 					zCoords.getRank());
 			logger.error(msg);
 			throw new Exception(msg);
 		}
 
 		Thread.sleep(REST_BETWEEN_PLOTOVERS);
 
 		DataBean dataBean = new DataBean(GuiPlotMode.SCATTER3D);
 		DataSetWithAxisInformation axisData = new DataSetWithAxisInformation();
 		AxisMapBean amb = new AxisMapBean(AxisMapBean.DIRECT);
 		axisData.setAxisMap(amb);
 		dataBean.putGuiParameter(GuiParameters.PLOTOPERATION, "UPDATE");
 		axisData.setData(sizes);
 		try {
 			dataBean.addData(axisData);
 		} catch (DataBeanException e) {
 			e.printStackTrace();
 		}
 
 		if (xCoords != null) {
 			dataBean.addAxis(AxisMapBean.XAXIS, xCoords);
 		}
 		if (yCoords != null) {
 			dataBean.addAxis(AxisMapBean.YAXIS, yCoords);
 		}
 		if (zCoords != null) {
 			dataBean.addAxis(AxisMapBean.ZAXIS, zCoords);
 		}
 
 		sendBeansToServer(plotName, dataBean, null);
 	}
 
 	/**
 	 * Allows the plotting of a 2D dataset as a surface to the defined view
 	 * 
 	 * @param plotName
 	 * @param data
 	 * @throws Exception
 	 */
 	@Override
 	public void surfacePlot(String plotName, IDataset data) throws Exception {
 		surfacePlot(plotName, null, null, data);
 	}
 
 	/**
 	 * Allows the plotting of a 2D dataset as a surface to the defined view
 	 * 
 	 * @param plotName
 	 * @param xAxis
 	 *            can be null
 	 * @param data
 	 * @throws Exception
 	 */
 	@Override
 	public void surfacePlot(String plotName, IDataset xAxis, IDataset data) throws Exception {
 		surfacePlot(plotName, xAxis, null, data);
 	}
 
 	/**
 	 * Allows the plotting of a 2D dataset as a surface to the defined view
 	 * 
 	 * @param plotName
 	 * @param xAxis
 	 *            can be null
 	 * @param yAxis
 	 *            can be null
 	 * @param data
 	 * @throws Exception
 	 */
 	@Override
 	public void surfacePlot(String plotName, IDataset xAxis, IDataset yAxis, IDataset data) throws Exception {
 		if (!isDataND(data, 2)) {
 			logger.error("Input dataset has incorrect rank: it has {} dimensions when it should be 2", data.getRank());
 			throw new Exception("Input dataset has incorrect rank: it should be 2");
 		}
 
 		if (xAxis != null && !isDataND(xAxis, 1)) {
 			String msg = String.format("X axis dataset has incorrect rank: it has %d dimensions when it should be 1",
 					xAxis.getRank());
 			logger.error(msg);
 			throw new Exception(msg);
 		}
 
 		if (yAxis != null && !isDataND(yAxis, 1)) {
 			String msg = String.format("Y axis dataset has incorrect rank: it has %d dimensions when it should be 1",
 					yAxis.getRank());
 			logger.error(msg);
 			throw new Exception(msg);
 		}
 
 		AxisMapBean amb = new AxisMapBean(AxisMapBean.DIRECT);
 
 		DataSetWithAxisInformation axisData = new DataSetWithAxisInformation();
 		axisData.setAxisMap(amb);
 		axisData.setData(data);
 
 		DataBean dataBean = new DataBean(GuiPlotMode.SURF2D);
 
 		try {
 			dataBean.addData(axisData);
 		} catch (DataBeanException e) {
 			e.printStackTrace();
 		}
 
 		if (xAxis != null) {
 			dataBean.addAxis(AxisMapBean.XAXIS, xAxis);
 		}
 		if (yAxis != null) {
 			dataBean.addAxis(AxisMapBean.YAXIS, yAxis);
 		}
 
 		sendBeansToServer(plotName, dataBean, null);
 	}
 
 	@Override
 	public void stackPlot(String plotName, final IDataset xAxis, IDataset[] yAxes) throws Exception {
 		stackPlot(plotName, xAxis, yAxes, null);
 	}
 
 	@Override
 	public void stackPlot(String plotName, IDataset xAxis, IDataset[] yAxes, final IDataset zAxis) throws Exception {
 		stackPlot(plotName, validateXAxis(xAxis, yAxes), yAxes, zAxis);
 	}
 
 	@Override
 	public void stackPlot(String plotName, IDataset[] xAxes, IDataset[] yAxes) throws Exception {
 		stackPlot(plotName, xAxes, yAxes, null);
 	}
 
 	@Override
 	public void stackPlot(String plotName, IDataset[] xAxes, IDataset[] yAxes, final IDataset zAxis) throws Exception {
 		lstackPlot(plotName, validateXAxes(xAxes, yAxes), yAxes, zAxis, false);
 	}
 
 	@Override
 	public void updateStackPlot(String plotName, IDataset[] xAxes, IDataset[] yAxes, final IDataset zAxis) throws Exception {
 		lstackPlot(plotName, validateXAxes(xAxes, yAxes), yAxes, zAxis, true);
 	}
 
 	/**
 	 * Plot a stack in 3D of 1D line plots to named view
 	 * @param plotName
 	 * @param xAxes
 	 * @param yAxes
 	 * @param zAxis
 	 * @param updateMode if true, keep zoom settings
 	 * @throws Exception
 	 */
 	private void lstackPlot(String plotName, IDataset[] xAxes, IDataset[] yAxes, IDataset zAxis, boolean updateMode) throws Exception {
 		for (IDataset x : xAxes) {
 			if (!isDataND(x, 1)) {
 				logger.error("Input x dataset has incorrect rank: it has {} dimensions when it should be 1",
 						x.getRank());
 				throw new Exception("Input x dataset has incorrect rank: it should be 1");
 			}
 		}
 
 		for (IDataset y : yAxes) {
 			if (!isDataND(y, 1)) {
 				logger.error("Input y dataset has incorrect rank: it has {} dimensions when it should be 1",
 						y.getRank());
 				throw new Exception("Input y dataset has incorrect rank: it should be 1");
 			}
 		}
 
 		DataBean dataBean = new DataBean(GuiPlotMode.ONED_THREED);
 		if (xAxes.length == 1) {
 			dataBean.addAxis(AxisMapBean.XAXIS, xAxes[0]);
 			for (int i = 0; i < yAxes.length; i++) {
 				// now add it to the plot data
 				try {
 					dataBean.addData(DataSetWithAxisInformation.createAxisDataSet(yAxes[i]));
 				} catch (DataBeanException e) {
 					e.printStackTrace();
 				}
 			}
 
 		} else {
 			if (xAxes.length != yAxes.length)
 				throw new Exception("# xAxis does not match # yAxis");
 			for (int i = 0; i < xAxes.length; i++) {
 				String axisStr = AxisMapBean.XAXIS + i;
 				dataBean.addAxis(axisStr, xAxes[i]);
 				// now add it to the plot data
 				try {
 					dataBean.addData(DataSetWithAxisInformation.createAxisDataSet(yAxes[i], axisStr));
 				} catch (DataBeanException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 
 		if (zAxis != null) {
 			if (!isDataND(zAxis, 1)) {
 				logger.error("Input z dataset has incorrect rank: it has {} dimensions when it should be 1",
 						zAxis.getRank());
 				throw new Exception("Input z dataset has incorrect rank: it should be 1");
 			}
 			dataBean.addAxis(AxisMapBean.ZAXIS, zAxis);
 		}
 
 		if (updateMode)
 			dataBean.putGuiParameter(GuiParameters.PLOTOPERATION, "UPDATE");
 
 		GuiBean guibean = new GuiBean();
 		guibean.put(GuiParameters.PLOTMODE, GuiPlotMode.ONED_THREED);
 		
 		sendBeansToServer(plotName, dataBean, guibean);
 	}
 
 	/**
 	 * Scan a directory and populate an image explorer view with any supported image formats
 	 * 
 	 * @param viewName
 	 *            of image explorer
 	 * @param pathname
 	 * @return number of files found
 	 * @throws Exception
 	 */
 	@Override
 	public int scanForImages(String viewName, String pathname) throws Exception {
 		return scanForImages(viewName, pathname, IMAGEORDERNONE, LISTOFSUFFIX, -1, true);
 	}
 
 	/**
 	 * Scan a directory and populate an image explorer view with any supported image formats
 	 * 
 	 * @param viewName
 	 *            of image explorer
 	 * @param pathname
 	 *            name of the path/directory which images should be loaded from
 	 * @param maxFiles
 	 *            maximum number of files that should be loaded
 	 * @param nthFile
 	 *            only load every nth file
 	 * @return number of files loaded
 	 * @throws Exception
 	 */
 
 	@Override
 	public int scanForImages(String viewName, String pathname, int maxFiles, int nthFile) throws Exception {
 		return scanForImages(viewName, pathname, IMAGEORDERNONE, LISTOFSUFFIX, -1, true, maxFiles, nthFile);
 	}
 
 	/**
 	 * Scan a directory and populate an image explorer view with any supported image formats in specified order
 	 * 
 	 * @param viewName
 	 * @param pathname
 	 * @param order
 	 * @return number of files loaded
 	 * @throws Exception
 	 */
 	@Override
 	public int scanForImages(String viewName, String pathname, int order) throws Exception {
 		return scanForImages(viewName, pathname, order, LISTOFSUFFIX, -1, true);
 	}
 
 	/**
 	 * Scan a directory and populate an image explorer view with images of given suffices
 	 * 
 	 * @param viewName
 	 *            of image explorer
 	 * @param pathname
 	 * @param order
 	 * @param suffices
 	 * @param gridColumns
 	 *            use -1 to indicate automatic configuration to square array
 	 * @param rowMajor
 	 *            if true, display images in row-major order
 	 * @return number of files loaded
 	 * @throws Exception
 	 */
 	@Override
 	public int scanForImages(String viewName, String pathname, int order, String[] suffices, int gridColumns,
 			boolean rowMajor) throws Exception {
 		return scanForImages(viewName, pathname, order, suffices, gridColumns, rowMajor, Integer.MAX_VALUE, 1);
 	}
 
 	/**
 	 * Scan a directory and populate an image explorer view with images of given suffices
 	 * 
 	 * @param viewName
 	 *            of image explorer
 	 * @param pathname
 	 * @param order
 	 * @param regex
 	 * @param suffices
 	 * @param gridColumns
 	 *            use -1 to indicate automatic configuration to square array
 	 * @param rowMajor
 	 *            if true, display images in row-major order
 	 * @return number of files loaded
 	 * @throws Exception
 	 */
 	@Override
 	public int scanForImages(String viewName, String pathname, int order, String regex, String[] suffices,
 			int gridColumns, boolean rowMajor) throws Exception {
 		return scanForImages(viewName, pathname, order, regex, suffices, gridColumns, rowMajor, Integer.MAX_VALUE, 1);
 	}
 
 	/**
 	 * Scan a directory and populate an image explorer view with images of given suffices
 	 * 
 	 * @param viewName
 	 * @param pathname
 	 * @param order
 	 * @param suffices
 	 * @param gridColumns
 	 * @param rowMajor
 	 * @param maxFiles
 	 * @param jumpBetween
 	 * @return number of files loaded
 	 * @throws Exception
 	 */
 	@Override
 	public int scanForImages(String viewName, String pathname, int order, String[] suffices, int gridColumns,
 			boolean rowMajor, int maxFiles, int jumpBetween) throws Exception {
 		return scanForImages(viewName, pathname, order, null, suffices, gridColumns, rowMajor, maxFiles, jumpBetween);
 	}
 
 	/**
 	 * Scan a directory and populate an image explorer view with images of given suffices
 	 * 
 	 * @param viewName
 	 * @param pathname
 	 * @param order
 	 * @param nameregex
 	 * @param suffices
 	 * @param gridColumns
 	 * @param rowMajor
 	 * @param maxFiles
 	 * @param jumpBetween
 	 * @return number of files loaded
 	 * @throws Exception
 	 */
 	@Override
 	public int scanForImages(String viewName, String pathname, int order, String nameregex, String[] suffices,
 			int gridColumns, boolean rowMajor, int maxFiles, int jumpBetween) throws Exception {
 		File file = new File(pathname);
 		PlotService plotServer = getPlotService();
 		int filesPushed = 0;
 		if (plotServer != null) {
 			int numFiles = 0;
 			if (file.isDirectory()) {
 				// GuiBean guiBean = getGuiStateForPlotMode(viewName, GuiPlotMode.MULTI2D);
 				GuiBean guiBean = new GuiBean();
 				guiBean.put(GuiParameters.PLOTMODE, GuiPlotMode.IMGEXPL);
 				File[] files = file.listFiles();
 				if (suffices == null)
 					suffices = LISTOFSUFFIX;
 				List<String> imageFiles = filterImages(files, nameregex, suffices);
 				int nImages = imageFiles.size();
 
 				int gridRows = (int) (gridColumns > 0 ? Math.ceil(nImages / (double) gridColumns) : Math.ceil(Math
 						.sqrt(nImages)));
 				if (gridRows == 0)
 					gridRows = 1;
 				int gridCols = gridColumns > 0 ? gridColumns : gridRows;
 				guiBean.put(GuiParameters.IMAGEGRIDSIZE, new Integer[] { gridRows, gridCols });
 				plotServer.updateGui(viewName, guiBean);
 				guiBean.remove(GuiParameters.IMAGEGRIDSIZE);
 
 				if (nImages == 0)
 					return filesPushed;
 
 				switch (order) {
 				case IMAGEORDERALPHANUMERICAL:
 					Collections.sort(imageFiles); // could make faster by removing dirname first
 					break;
 				case IMAGEORDERCHRONOLOGICAL:
 					chronoSort(imageFiles);
 					break;
 				case IMAGEORDERNONE:
 					break;
 				}
 				Iterator<String> iter = imageFiles.iterator();
 				if (rowMajor) {
 					while (iter.hasNext() && numFiles < maxFiles) {
 						String filename = iter.next();
 						if (numFiles % jumpBetween == 0) {
 							guiBean.put(GuiParameters.FILENAME, filename);
 							plotServer.updateGui(viewName, guiBean);
 							filesPushed++;
 						}
 						numFiles++;
 					}
 				} else {
 					int x = 0;
 					int y = 0;
 					while (iter.hasNext() && numFiles < maxFiles) {
 						String filename = iter.next();
 						if (numFiles % jumpBetween == 0) {
 							guiBean.put(GuiParameters.IMAGEGRIDXPOS, Integer.valueOf(x));
 							guiBean.put(GuiParameters.IMAGEGRIDYPOS, Integer.valueOf(y));
 							guiBean.put(GuiParameters.FILENAME, filename);
 							plotServer.updateGui(viewName, guiBean);
 							filesPushed++;
 							y++;
 						}
 						numFiles++;
 						if (y == gridRows) {
 							y = 0;
 							x++;
 						}
 					}
 				}
 			} else {
 				logger.warn("Given path was not a directory");
 			}
 		}
 		return filesPushed;
 	}
 
 	private List<String> filterImages(File[] files, String regex, String[] suffices) {
 		List<String> listOfImages = new ArrayList<String>();
 		if (suffices == null && regex == null) {
 			for (int i = 0; i < files.length; i++) {
 				listOfImages.add(files[i].getAbsolutePath());
 			}
 		} else {
 			StringBuilder fullregex = new StringBuilder(regex == null ? ".*" : regex);
 			if (suffices != null && suffices.length > 0) {
 				if (suffices.length == 1) {
 					fullregex.append(suffices[0]);
 				} else {
 					fullregex.append('(');
 					for (String s : suffices) {
 						fullregex.append(s);
 						fullregex.append('|');
 					}
 					final int end = fullregex.length();
 					fullregex.replace(end - 1, end, ")");
 				}
 
 				Pattern p = Pattern.compile(fullregex.toString());
 
 				for (File f : files) {
 					Matcher m = p.matcher(f.getName().toLowerCase());
 					if (m.matches()) {
 						listOfImages.add(f.getAbsolutePath());
 						continue;
 					}
 				}
 			}
 
 		}
 		return listOfImages;
 	}
 
 	private void chronoSort(List<String> imageFiles) {
 		TreeMap<Long, String> imap = new TreeMap<Long, String>();
 
 		for (String s : imageFiles) {
 			imap.put(new File(s).lastModified(), s);
 		}
 		imageFiles.clear();
 		imageFiles.addAll(imap.values());
 	}
 
 	/**
 	 * Send volume data contained in given filename to remote renderer. Note the raw data needs
 	 * written in little endian byte order
 	 * @param viewName
 	 * @param rawvolume
 	 *            raw format filename
 	 * @param headerSize
 	 *            number of bytes to ignore in file
 	 * @param voxelType
 	 *            0,1,2,3 for byte, short, int and float (integer values are interpreted as unsigned values)
 	 * @param xdim
 	 *            number of voxels in x-dimension
 	 * @param ydim
 	 *            number of voxels in y-dimension
 	 * @param zdim
 	 *            number of voxels in z-dimension
 	 * @throws Exception
 	 */
 	@Override
 	public void volumePlot(String viewName, String rawvolume, int headerSize, int voxelType, int xdim, int ydim,
 			int zdim) throws Exception {
 		PlotService plotServer = getPlotService();
 		if (plotServer != null) {
 			GuiBean guiBean = new GuiBean();
 			guiBean.put(GuiParameters.PLOTMODE, GuiPlotMode.VOLUME);
 			guiBean.put(GuiParameters.FILENAME, rawvolume);
 			guiBean.put(GuiParameters.VOLUMEHEADERSIZE, Integer.valueOf(headerSize));
 			guiBean.put(GuiParameters.VOLUMEVOXELTYPE, Integer.valueOf(voxelType));
 			guiBean.put(GuiParameters.VOLUMEXDIM, Integer.valueOf(xdim));
 			guiBean.put(GuiParameters.VOLUMEYDIM, Integer.valueOf(ydim));
 			guiBean.put(GuiParameters.VOLUMEZDIM, Integer.valueOf(zdim));
 			plotServer.updateGui(viewName, guiBean);
 		}
 	}
 
 	/**
 	 * Send volume data to remote renderer. Only single-element datasets of byte, short, int and float are directly
 	 * supported. Other types are internally converted; first elements of compound datasets are extracted.
 	 * 
 	 * @param viewName
 	 * @param volume
 	 * @throws Exception
 	 */
 	@Override
 	public void volumePlot(String viewName, IDataset volume) throws Exception {
 		if (!isDataND(volume, 3)) {
 			logger.error("Input dataset has incorrect rank: it has {} dimensions when it should be 3", volume.getRank());
 			throw new Exception("Input dataset has incorrect rank: it should be 3");
 		}
 		DataHolder tHolder = new DataHolder();
 		try {
 			AbstractDataset v = DatasetUtils.convertToAbstractDataset(volume);
 			if (v instanceof AbstractCompoundDataset) {
 				AbstractCompoundDataset cd = (AbstractCompoundDataset) v;
 				v = cd.getElements(0);
 			}
 			switch (v.getDtype()) {
 			case AbstractDataset.BOOL:
 				v = v.cast(AbstractDataset.FLOAT32);
 				break;
 			case AbstractDataset.FLOAT64:
 				v = v.cast(AbstractDataset.FLOAT32);
 				break;
 			case AbstractDataset.INT32:
 				v = v.cast(AbstractDataset.FLOAT32);
 				break;
 			case AbstractDataset.INT64:
 				v = v.cast(AbstractDataset.FLOAT32);
 				break;
 			}
 			//FIXME This is a horrible hack to get round some bugs in GigaCube.  Should be fixed in the GigaCube code as soon as possible
 			int[] shape = new int[] {v.getShape()[2], v.getShape()[1], v.getShape()[0]};
 			v.setShape(shape);
 			
 			String filename = saveTempFile(tHolder, v);
 			volumePlot(viewName, filename);
 		} catch (ScanFileHolderException e) {
 			throw new Exception("Failed to save to temporary file");
 		}
 	}
 
 	/**
 	 * Send volume data contained in given filename to remote renderer
 	 * 
 	 * @param viewName
 	 * @param dsrvolume
 	 *            Diamond Scisoft raw format filename
 	 * @throws Exception
 	 */
 	@Override
 	public void volumePlot(String viewName, String dsrvolume) throws Exception {
 		PlotService plotServer = getPlotService();
 		if (plotServer != null) {
 			GuiBean guiBean = new GuiBean();
 			guiBean.put(GuiParameters.PLOTMODE, GuiPlotMode.VOLUME);
 			guiBean.put(GuiParameters.FILENAME, dsrvolume);
 			plotServer.updateGui(viewName, guiBean);
 		}
 	}
 
 	/**
 	 * Clear/empty a current plot view
 	 * 
 	 * @throws Exception
 	 */
 	@Override
 	public void clearPlot(String viewName) throws Exception {
 		PlotService plotServer = getPlotService();
 		if (plotServer != null) {
 			GuiBean guiBean = new GuiBean();
 			guiBean.put(GuiParameters.PLOTMODE, GuiPlotMode.EMPTY);
 			plotServer.updateGui(viewName, guiBean);
 		}
 	}
 
 	/**
 	 * Set up a new image grid for an image explorer view with the specified # rows and columns
 	 * 
 	 * @param viewName
 	 * @param gridRows
 	 *            number of start rows
 	 * @param gridColumns
 	 *            number of start columns
 	 * @throws Exception
 	 */
 	@Override
 	public void setupNewImageGrid(String viewName, int gridRows, int gridColumns) throws Exception {
 		GuiBean guiBean = new GuiBean();
 		guiBean.put(GuiParameters.PLOTMODE, GuiPlotMode.IMGEXPL);
 		guiBean.put(GuiParameters.IMAGEGRIDSIZE, new Integer[] { gridRows, gridColumns });
 		setGuiBean(viewName, guiBean);
 	}
 
 	/**
 	 * Set up a new image grid for an image explorer view with the specified # images
 	 * 
 	 * @param viewName
 	 * @param images
 	 *            number of images
 	 * @throws Exception
 	 */
 	@Override
 	public void setupNewImageGrid(String viewName, int images) throws Exception {
 		int gridRows = (int) Math.ceil(Math.sqrt(images));
 		if (gridRows == 0)
 			gridRows = 1;
 		int gridCols = gridRows;
 		setupNewImageGrid(viewName, gridRows, gridCols);
 	}
 
 	/**
 	 * Plot images to the grid of an image explorer view
 	 * 
 	 * @param viewName
 	 * @param datasets
 	 * @throws Exception
 	 */
 	@Override
 	public void plotImageToGrid(String viewName, IDataset[] datasets) throws Exception {
 		plotImageToGrid(viewName, datasets, false);
 	}
 
 	/**
 	 * Plot images to the grid of an image explorer view
 	 * 
 	 * @param viewName
 	 * @param datasets
 	 * @param store
 	 *            if true, create copies of images as temporary files
 	 * @throws Exception
 	 */
 	@Override
 	public void plotImageToGrid(String viewName, IDataset[] datasets, boolean store) throws Exception {
 		// GuiBean guiBean = getGuiStateForPlotMode(viewName, GuiPlotMode.MULTI2D);
 		GuiBean guiBean = new GuiBean();
 		guiBean.put(GuiParameters.PLOTMODE, GuiPlotMode.IMGEXPL);
 		DataHolder tHolder = new DataHolder();
 
 		for (int i = 0; i < datasets.length; i++) {
 			if (store) {
 				try {
 					String filename = saveTempFile(tHolder, datasets[i]);
 					guiBean.put(GuiParameters.FILENAME, filename);
 				} catch (ScanFileHolderException e) {
 					throw new Exception("Failed to save to temporary file");
 				}
 			} else {
 				guiBean.put(GuiParameters.FILENAME, datasets[i].getName());
 			}
 			setGuiBean(viewName, guiBean);
 		}
 	}
 
 	/**
 	 * Plot images to the grid of an image explorer view
 	 * 
 	 * @param viewName
 	 * @param filename
 	 * @param gridX
 	 *            X position in the grid
 	 * @param gridY
 	 *            Y position in the grid
 	 * @throws Exception
 	 */
 
 	@Override
 	public void plotImageToGrid(String viewName, String filename, int gridX, int gridY) throws Exception {
 		// GuiBean guiBean = getGuiStateForPlotMode(viewName, GuiPlotMode.MULTI2D);
 		GuiBean guiBean = new GuiBean();
 		guiBean.put(GuiParameters.PLOTMODE, GuiPlotMode.IMGEXPL);
 
 		guiBean.put(GuiParameters.FILENAME, filename);
 		guiBean.put(GuiParameters.IMAGEGRIDXPOS, Integer.valueOf(gridX));
 		guiBean.put(GuiParameters.IMAGEGRIDYPOS, Integer.valueOf(gridY));
 		setGuiBean(viewName, guiBean);
 	}
 
 	/**
 	 * Plot images to the grid of an image explorer view
 	 * 
 	 * @param viewName
 	 * @param filename
 	 * @throws Exception
 	 */
 
 	@Override
 	public void plotImageToGrid(String viewName, String filename) throws Exception {
 		// GuiBean guiBean = getGuiStateForPlotMode(viewName, GuiPlotMode.MULTI2D);
 		GuiBean guiBean = new GuiBean();
 		guiBean.put(GuiParameters.PLOTMODE, GuiPlotMode.IMGEXPL);
 
 		guiBean.put(GuiParameters.FILENAME, filename);
 		setGuiBean(viewName, guiBean);
 	}
 
 	/**
 	 * Plot an image to the grid of an image explorer view
 	 * 
 	 * @param viewName
 	 * @param dataset
 	 * @throws Exception
 	 */
 	@Override
 	public void plotImageToGrid(String viewName, IDataset dataset) throws Exception {
 		plotImageToGrid(viewName, dataset, false);
 	}
 
 	/**
 	 * Plot an image to the grid of an image explorer view
 	 * 
 	 * @param viewName
 	 * @param dataset
 	 * @param store
 	 *            if true, create a copy of image as a temporary file
 	 * @throws Exception
 	 */
 	@Override
 	public void plotImageToGrid(String viewName, IDataset dataset, boolean store) throws Exception {
 		// GuiBean guiBean = getGuiStateForPlotMode(viewName, GuiPlotMode.MULTI2D);
 
 		GuiBean guiBean = new GuiBean();
 		guiBean.put(GuiParameters.PLOTMODE, GuiPlotMode.IMGEXPL);
 
 		if (store) {
 			DataHolder tHolder = new DataHolder();
 
 			try {
 				String filename = saveTempFile(tHolder, dataset);
 				guiBean.put(GuiParameters.FILENAME, filename);
 			} catch (ScanFileHolderException e) {
 				throw new Exception("Failed to save to temporary file");
 			}
 		} else {
 			guiBean.put(GuiParameters.FILENAME, dataset.getName());
 		}
 		setGuiBean(viewName, guiBean);
 	}
 
 	/**
 	 * Plot an image to the grid of an image explorer view in specified position
 	 * 
 	 * @param viewName
 	 * @param dataset
 	 * @param gridX
 	 * @param gridY
 	 * @throws Exception
 	 */
 	@Override
 	public void plotImageToGrid(String viewName, IDataset dataset, int gridX, int gridY) throws Exception {
 		plotImageToGrid(viewName, dataset, gridX, gridY, false);
 	}
 
 	/**
 	 * Plot an image to the grid of an image explorer view in specified position
 	 * 
 	 * @param viewName
 	 * @param dataset
 	 * @param gridX
 	 * @param gridY
 	 * @param store
 	 *            if true, create a copy of image as a temporary file
 	 * @throws Exception
 	 */
 	@Override
 	public void plotImageToGrid(String viewName, IDataset dataset, int gridX, int gridY, boolean store)
 			throws Exception {
 		// GuiBean guiBean = getGuiStateForPlotMode(viewName, GuiPlotMode.MULTI2D);
 
 		GuiBean guiBean = new GuiBean();
 		guiBean.put(GuiParameters.PLOTMODE, GuiPlotMode.IMGEXPL);
 
 		guiBean.put(GuiParameters.IMAGEGRIDXPOS, Integer.valueOf(gridX));
 		guiBean.put(GuiParameters.IMAGEGRIDYPOS, Integer.valueOf(gridY));
 		if (store) {
 			DataHolder tHolder = new DataHolder();
 
 			try {
 				String filename = saveTempFile(tHolder, dataset);
 				guiBean.put(GuiParameters.FILENAME, filename);
 			} catch (ScanFileHolderException e) {
 				throw new Exception("Failed to save to temporary file");
 			}
 		} else {
 			guiBean.put(GuiParameters.FILENAME, dataset.getName());
 		}
 		setGuiBean(viewName, guiBean);
 	}
 
 	protected String saveTempFile(DataHolder tHolder, IDataset dataset) throws ScanFileHolderException {
 
 		try {
 			java.io.File tmpFile = File.createTempFile("image-explorer-store-", ".raw");
 			String dirName = "/dls/tmp/rkn21281";
 			java.io.File directory = new java.io.File(dirName);
 			if (!directory.exists())
 				directory.mkdir();
 			String rawFilename = directory.getAbsolutePath() + System.getProperty("file.separator") + tmpFile.getName();
 			tHolder.setDataset("Data", dataset);
 			new RawBinarySaver(rawFilename).saveFile(tHolder);
 			return rawFilename;
 		} catch (IOException e) {
 			throw new ScanFileHolderException("Couldn't save file", e);
 		}
 	}
 
 	/**
 	 * @param viewer
 	 * @param tree
 	 * @throws Exception
 	 */
 	@Override
 	public void viewNexusTree(String viewer, HDF5File tree) throws Exception {
 		logger.info("Tree sent to {}", viewer);
 
 		DataBean db = new DataBean(null);
 		db.addHDF5Tree(tree);
 		sendBeansToServer(viewer, db, null);
 	}
 
 	/**
 	 * @param viewer
 	 * @param tree
 	 * @throws Exception
 	 */
 	@Override
 	public void viewHDF5Tree(String viewer, HDF5File tree) throws Exception {
 		logger.info("Tree sent to {}", viewer);
 
 		DataBean db = new DataBean(null);
 		db.addHDF5Tree(tree);
 		sendBeansToServer(viewer, db, null);
 	}
 
 	/**
 	 * Simple method which forwards the data to the plot server
 	 * 
 	 * @param plotName
 	 *            The name of the plot
 	 * @param dataBean
 	 *            The data to be passed to the plot server (can be null)
 	 * @param guiBean
 	 *            The gui data which is included for the Plot legend
 	 * @throws Exception
 	 */
 	private void sendBeansToServer(String plotName, DataBean dataBean, GuiBean guiBean) throws Exception {
 
 		PlotService plotServer = getPlotService();
 		if (plotServer != null) {
 			if(guiBean != null) {
 				guiBean.remove(GuiParameters.PLOTID); // remove any previous ID now it is being pushed to all the clients
 
 				plotServer.updateGui(plotName, guiBean);
 			}
 				
 			if (dataBean != null)
 				plotServer.setData(plotName, dataBean);
 		}
 	}
 
 	@Override
 	public void setGuiBean(String plotName, GuiBean bean) throws Exception {
 		sendBeansToServer(plotName, null, bean);
 	}
 
 	@Override
 	public GuiBean getGuiBean(String plotName) throws Exception {
 
 		PlotService plotService = getPlotService();
 
 		return plotService.getGuiState(plotName);
 	}
 
 
 	@Override
 	public void setDataBean(String plotName, DataBean bean) throws Exception {
 		sendBeansToServer(plotName, bean, null);
 	}
 
 	@Override
 	public DataBean getDataBean(String plotName) throws Exception {
 
 		PlotService plotService = getPlotService();
 
 		return plotService.getData(plotName);
 	}
 
 	/**
 	 * @return GUI bean for named view of given plot mode or create a new one
 	 */
 	@Override
 	public GuiBean getGuiStateForPlotMode(String plotName, GuiPlotMode plotMode) {
 		GuiBean bean;
 		try {
 			bean = getGuiBean(plotName);
 			if (bean != null) {
 				if (bean.containsKey(GuiParameters.PLOTMODE)) {
 					if (!bean.get(GuiParameters.PLOTMODE).equals(plotMode)) {
 						bean = null;
 					} else {
 						bean.remove(GuiParameters.PLOTID);
 					}
 				}
 			}
 		} catch (Exception e) {
 			bean = null;
 		}
 
 		if (bean == null) {
 			bean = new GuiBean();
 			bean.put(GuiParameters.PLOTMODE, plotMode);
 		}
 		return bean;
 	}
 
 	@Override
 	public String[] getGuiNames() throws Exception {
 		return getPlotService().getGuiNames();
 	}
 }
