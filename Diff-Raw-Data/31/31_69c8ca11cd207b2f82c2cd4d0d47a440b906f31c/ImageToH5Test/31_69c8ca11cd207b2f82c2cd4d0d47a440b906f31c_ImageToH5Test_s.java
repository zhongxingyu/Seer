 /*-
  * Copyright © 2011 Diamond Light Source Ltd.
  *
  * This file is part of GDA.
  *
  * GDA is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License version 3 as published by the Free
  * Software Foundation.
  *
  * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along
  * with GDA. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.dawnsci.conversion;
 
 import java.io.File;
 import java.util.Arrays;
 
 import org.dawb.common.services.conversion.IConversionContext;
 import org.dawb.common.services.conversion.IConversionContext.ConversionScheme;
 import org.dawb.common.util.io.FileUtils;
 import org.junit.Test;
 import static org.junit.Assert.fail;
 
 import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
 import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
 
 public class ImageToH5Test {
 
 	@Test
 	public void testImageSimple() throws Exception {
 		
 		System.out.println("starting testImageSimple");
 		doTest("testImageSimple", "/entry/data", new int[]{10,2048,2048});
 	}
 	@Test
 	public void testImageLongPath() throws Exception {
 		
 		System.out.println("starting testImageLongPath");
 		doTest("testImageLongPath", "/entry1/a/long/path/data", new int[]{20,2048,2048});
 	}
 
 	/**
 	 * Test should be done @ 1000x 2k but this clobbers the test decks.
 	 * We reduce to 100 x 2k for the test decks to run properly.
 	 * @throws Exception
 	 */
 	@Test
 	public void testLargeData() throws Exception {
 		
 		System.out.println("starting testLargeData");
 		doTest("testLargeData", "/entry1/data", new int[]{100,2048,2048});
 	}
 
     private void doTest(String testname, String dPath, int[] shape) throws Exception {
 
 		final File image = new File("testfiles/dir/ref-testscale_1_001.img");
 		
 		final File dir   = new File(System.getProperty("java.io.tmpdir"), "ImageToH5Test_"+testname);
 		try {
 			// Copy the file a few times.
 			for (int i = 0; i < shape[0]; i++) {
 				final File nf = new File(dir, "copy_"+i+".img");
 				nf.deleteOnExit();
 				FileUtils.copyNio(image, nf);
 			}
 			
 			ConversionServiceImpl service = new ConversionServiceImpl();
 			
 	        final IConversionContext context = service.open(dir.getAbsolutePath()+"/copy_.*img");
 	        final File output = new File(dir.getParentFile(), "imageStackTestOutput_"+testname+".h5");
 	        output.deleteOnExit();
 	        context.setOutputPath(output.getAbsolutePath());
 	        context.setDatasetName(dPath); // With this conversion dataset is the OUTPUT
 	        context.setConversionScheme(ConversionScheme.H5_FROM_IMAGEDIR);
 			
 			service.process(context);
 			
 			if (!output.exists()) {
 				fail("Image stack was not written to "+output.getName());
 			}
 
 			final ILazyDataset set = LoaderFactory.getData(output.getAbsolutePath(), null).getLazyDataset(0);
 			if (!Arrays.equals(set.getShape(), shape)) {
 				fail("Dataset written with shape "+Arrays.toString(set.getShape())+", but expected shape was "+Arrays.toString(shape));
 			}
 
 		} finally {
 			FileUtils.recursiveDelete(dir);
 		}
 		
 	}
 }
