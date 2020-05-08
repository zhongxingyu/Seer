 /*
  * Copyright (c) 2012 European Synchrotron Radiation Facility,
  *                    Diamond Light Source Ltd.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  */ 
 package org.dawb.workbench.ui.editors.test;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import org.dawb.common.ui.plot.AbstractPlottingSystem;
 import org.dawb.common.ui.plot.trace.IImageTrace;
 import org.dawb.common.ui.plot.trace.ILineTrace;
 import org.dawb.common.ui.plot.trace.ITrace;
 import org.dawb.workbench.plotting.system.LightWeightPlottingSystem;
 import org.dawb.workbench.ui.editors.AsciiEditor;
 import org.dawb.workbench.ui.editors.ImageEditor;
 import org.dawb.workbench.ui.editors.PlotDataEditor;
 import org.eclipse.core.filesystem.EFS;
 import org.eclipse.core.filesystem.IFileStore;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.ide.FileStoreEditorInput;
 import org.junit.Test;
 import org.osgi.framework.Bundle;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.IntegerDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.LongDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.Random;
 import fable.framework.toolbox.EclipseUtils;
 
 /**
  * 
  * Testing scalability of plotting.
  * 
  * @author gerring
  *
  */
 public class SWTXYStressTest {
 	
 	@Test
 	public void testIfMemoryLeak1D() throws Throwable {
 		
 		final Bundle bun  = Platform.getBundle("org.dawb.workbench.ui.test");
 
 		String path = (bun.getLocation()+"src/org/dawb/workbench/ui/editors/test/ascii.dat");
 		path = path.substring("reference:file:".length());
 		if (path.startsWith("/C:")) path = path.substring(1);
 		
 		final IWorkbenchPage page      = EclipseUtils.getPage();		
 		final IFileStore  externalFile = EFS.getLocalFileSystem().fromLocalFile(new File(path));
 		final IEditorPart part         = page.openEditor(new FileStoreEditorInput(externalFile), AsciiEditor.ID);
 		
 		final AsciiEditor editor       = (AsciiEditor)part;
 		final PlotDataEditor plotter   = (PlotDataEditor)editor.getActiveEditor();
 		final AbstractPlottingSystem sys = plotter.getPlottingSystem();
 		
     	AbstractDataset data = Random.rand(new int[]{2048});
 		final ILineTrace trace = sys.createLineTrace("Test line plot");
 		trace.setData(IntegerDataset.arange(2048, AbstractDataset.INT32), data);
 		sys.addTrace(trace);
 
         for (int i = 0; i < 1000000; i++) {
 			
         	data = Random.rand(new int[]{2048});
         	
         	Display.getDefault().syncExec(new Runnable() {
         		public void run() {
         			AbstractDataset data = Random.rand(new int[]{2048});
        			trace.setData(IntegerDataset.arange(2048, AbstractDataset.INT32), data);
         			sys.repaint();
         			
         		}
         	});
         	
     		EclipseUtils.delay(20);
       	
 		}
 	
 		
 		EclipseUtils.delay(1000);
 		
 	}
 
 	@Test
 	public void testIfMemoryLeak2D() throws Throwable {
 		
 		final Bundle bun  = Platform.getBundle("org.dawb.workbench.ui.test");
 
 		String path = (bun.getLocation()+"src/org/dawb/workbench/ui/editors/test/billeA.edf");
 		path = path.substring("reference:file:".length());
 		if (path.startsWith("/C:")) path = path.substring(1);
 
 		final IWorkbenchPage page      = EclipseUtils.getPage();		
 		final IFileStore  externalFile = EFS.getLocalFileSystem().fromLocalFile(new File(path));
 		final IEditorPart part         = page.openEditor(new FileStoreEditorInput(externalFile), ImageEditor.ID);
 		page.setPartState(EclipseUtils.getPage().getActivePartReference(), IWorkbenchPage.STATE_MAXIMIZED);
 
 		EclipseUtils.delay(2000);
 		
 		final ImageEditor editor       = (ImageEditor)part;
 		final AbstractPlottingSystem sys = editor.getPlotImageEditor().getPlottingSystem();
 
 		final Collection<ITrace>   traces= sys.getTraces(IImageTrace.class);
 		final IImageTrace          imt = (IImageTrace)traces.iterator().next();
 
 		// TODO Longer term tests may do while(true) here...
         for (int i = 0; i < 1000000; i++) {
 			
         	final DoubleDataset data = new DoubleDataset(new int[]{2048, 2048});
         	for (int j = 0; j < 2048*2048; j++) {
         		data.getData()[j] =  Math.random();
 			}
         	
         	Display.getDefault().syncExec(new Runnable() {
         		public void run() {
                 	imt.setData(data, null, false);
         		}
         	});
         	
     		EclipseUtils.delay(5);
       	
 		}
 	
 		
 		EclipseUtils.delay(1000);
 		
 	}
 
 	
 	@Test
 	public void testRandomNumbers() throws Throwable {
 		
 		createTest(createTestArraysRandom(10, 1000), 1000);
 	}
 	
 	@Test
 	public void testCoherantNumbers1() throws Throwable {
 		
 		createTest(createTestArraysCoherant(10, 1000), 1000);
 	}
 	
 	@Test
 	public void testCoherantNumbers2() throws Throwable {
 		
 		createTest(createTestArraysCoherant(100, 10000), 2000);
 	}
 
 	private void createTest(final List<AbstractDataset> ys, long expectedTime) throws Throwable {
 		
 		final Bundle bun  = Platform.getBundle("org.dawb.workbench.ui.test");
 
 		String path = (bun.getLocation()+"src/org/dawb/workbench/ui/editors/test/ascii.dat");
 		path = path.substring("reference:file:".length());
 		if (path.startsWith("/C:")) path = path.substring(1);
 		
 		final IWorkbenchPage page      = EclipseUtils.getPage();		
 		final IFileStore  externalFile = EFS.getLocalFileSystem().fromLocalFile(new File(path));
 		final IEditorPart part         = page.openEditor(new FileStoreEditorInput(externalFile), AsciiEditor.ID);
 		
 		final AsciiEditor editor       = (AsciiEditor)part;
 		final PlotDataEditor plotter   = (PlotDataEditor)editor.getActiveEditor();
 		final AbstractPlottingSystem sys = plotter.getPlottingSystem();
 		
 		if (!(sys instanceof LightWeightPlottingSystem)) throw new Exception("This test is designed for "+LightWeightPlottingSystem.class.getName());
 		page.setPartState(EclipseUtils.getPage().getActivePartReference(), IWorkbenchPage.STATE_MAXIMIZED);
 				
 		final long start = System.currentTimeMillis();
 		
 		sys.createPlot1D(AbstractDataset.arange(0, ys.get(0).getSize(), 1, AbstractDataset.INT32), ys, null);
 		EclipseUtils.delay(10);
 		
 		final long end  = System.currentTimeMillis();
 		final long time = end-start-10;
 		
 		System.out.println("It took "+time+"ms to plot the sets.");
 		if (time>expectedTime) throw new Exception("It took too long to plot the data sets! It took "+time+"ms to plot the sets, it should be "+expectedTime+" or less.");
 			
  		EclipseUtils.delay(2000);
 		
 		EclipseUtils.getPage().closeEditor(editor, false);
 		System.out.println("Closed: "+path);
 	}
 
 	
 	private List<AbstractDataset> createTestArraysRandom(final int numberPlots, final int size) {
 		
 		final List<AbstractDataset> ys = new ArrayList<AbstractDataset>(numberPlots);
 		for (int i = 0; i < numberPlots; i++) {
 			final long[] buffer = new long[size];
 			for (int j = 0; j < size; j++) buffer[j] = Math.round(Math.random()*10000);
 			final LongDataset ls = new LongDataset(buffer,size);
 			ys.add(ls);
 		}
 		
 		return ys;
 	}
 	
 	private List<AbstractDataset> createTestArraysCoherant(final int numberPlots, final int size) {
 		
 		final List<AbstractDataset> ys = new ArrayList<AbstractDataset>(numberPlots);
 		for (int i = 0; i < numberPlots; i++) {
 			
 			double rand = Math.random();
 			
 			final long[] buffer = new long[size];
 			for (int j = 0; j < size; j++) buffer[j] = (long)Math.pow(j+rand, 2d)*i;
 			final LongDataset ls = new LongDataset(buffer,size);
 			ys.add(ls);
 		}
 		
 		return ys;
 	}
 	
 }
