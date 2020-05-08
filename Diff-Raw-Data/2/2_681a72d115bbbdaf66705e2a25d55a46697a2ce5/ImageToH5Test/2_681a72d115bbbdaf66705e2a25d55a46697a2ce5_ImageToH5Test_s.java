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
 
 import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
 import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
 
 public class ImageToH5Test {
 
 	@Test
 	public void testImageToH5Simple() throws Exception {
 
 		final File image = new File("testfiles/dir/ref-testscale_1_001.img");
 		// Copy the file a few times.
 		for (int i = 0; i < 100; i++) {
 			final File nf = new File(image.getParentFile(), "copy_"+i+".img");
 			FileUtils.copyNio(image, nf);
 			nf.deleteOnExit();
 		}
 		
 		ConversionServiceImpl service = new ConversionServiceImpl();
 		
         final IConversionContext context = service.open("testfiles/dir/copy_.*img");
         final File output = new File("testfiles/imageStackTestOutput.h5");
         output.deleteOnExit();
         context.setOutputPath(output.getAbsolutePath());
         context.setDatasetName("/entry1/data"); // With this conversion dataset is the OUTPUT
         context.setConversionScheme(ConversionScheme.H5_FROM_IMAGEDIR);
 		
 		service.process(context);
 		
 		if (!output.exists()) throw new Exception("Image stack was not written to "+output.getName());
 		
 		
 		final ILazyDataset set = LoaderFactory.getData(output.getAbsolutePath(), null).getLazyDataset(0);
		if (!Arrays.equals(set.getShape(), new int[]{100,1024,1024})) {
 			throw new Exception("Did not write dataset of expected shape!");
 		}
 		
 	}
 }
