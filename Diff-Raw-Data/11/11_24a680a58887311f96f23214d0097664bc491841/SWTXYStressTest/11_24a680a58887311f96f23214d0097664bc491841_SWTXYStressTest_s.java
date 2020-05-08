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
 import java.util.List;
 
 import org.dawb.common.ui.plot.AbstractPlottingSystem;
import org.dawb.common.ui.plot.PlotType;
 import org.dawb.workbench.plotting.system.LightWeightPlottingSystem;
 import org.dawb.workbench.ui.editors.AsciiEditor;
 import org.dawb.workbench.ui.editors.PlotDataEditor;
 import org.eclipse.core.filesystem.EFS;
 import org.eclipse.core.filesystem.IFileStore;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.ide.FileStoreEditorInput;
 import org.junit.Test;
 import org.osgi.framework.Bundle;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.LongDataset;
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
