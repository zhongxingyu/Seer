 /*
  * Copyright 2013-2014 Odysseus Software GmbH
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.musicmount.builder;
 
 import java.io.File;
 
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.rules.TemporaryFolder;
 import org.musicmount.builder.impl.AssetLocator;
 import org.musicmount.builder.impl.AssetStore;
 import org.musicmount.builder.impl.LibraryParser;
 import org.musicmount.builder.impl.LocalStrings;
 import org.musicmount.builder.impl.ResourceLocator;
 import org.musicmount.builder.impl.ResponseFormatter;
 import org.musicmount.builder.impl.SimpleAssetLocator;
 import org.musicmount.builder.impl.SimpleAssetParser;
 import org.musicmount.builder.impl.SimpleResourceLocator;
 import org.musicmount.builder.model.Library;
 import org.musicmount.io.ResourceProvider;
 import org.musicmount.io.file.FileResourceProvider;
 
 /*
  * TODO test doesn't assert anything
  */
 public class MusicMountBuilderTest {
 	@Rule
 	public TemporaryFolder outputFolder = new TemporaryFolder();
 
 	@Test
 	public void testMain() throws Exception {
		String input = new File(getClass().getResource("/sample-library").toURI()).getAbsolutePath();
 		String output = outputFolder.getRoot().getAbsolutePath();
		MusicMountBuilder.main(new String[]{ "--pretty", "--full", input, output });
		MusicMountBuilder.main(new String[]{ "--pretty", input, output }); // use asset store
 	}
 
 	@Test
 	public void testGenerateResponseFiles() throws Exception {
 		ResourceProvider resourceProvider = new FileResourceProvider();
 		File inputFolder = new File(getClass().getResource("/sample-library").toURI());
 		AssetStore assetStore = new AssetStore(MusicMountBuilder.API_VERSION);
 		assetStore.update(resourceProvider.newResource(inputFolder.toPath()), new SimpleAssetParser());
 		Library library = new LibraryParser().parse(assetStore.assets());
 		ResourceLocator resourceLocator = new SimpleResourceLocator(resourceProvider.newResource(outputFolder.getRoot().toPath()), false, false);
 		ResponseFormatter<?> formatter = new ResponseFormatter.JSON(MusicMountBuilder.API_VERSION, new LocalStrings(), false, false, false, true);
 		AssetLocator assetLocator = new SimpleAssetLocator(resourceProvider.newResource(inputFolder.toPath()), "music", null);
 		MusicMountBuilder.generateResponseFiles(library, formatter, resourceLocator, assetLocator);
 	}
 }
