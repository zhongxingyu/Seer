 /**
  * Optimus, framework for Model Transformation
  *
  * Copyright (C) 2013 Worldline or third-party contributors as
  * indicated by the @author tags or express copyright attribution
  * statements applied by the authors.
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
  */
 package net.atos.optimus.m2t.java.core.internal.postprocessors;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 import java.io.StringWriter;
 
 import net.atos.optimus.m2t.java.core.FileHandler;
 import net.atos.optimus.m2t.java.core.IPostProcessor;
 import net.atos.optimus.m2t.merger.java.core.JavaCodeMerger;
 
 import org.eclipse.jdt.core.JavaModelException;
 
 /**
  * Post process that invokes the java code merger
  * 
  * @author Maxence Vanbésien (mvaawl@gmail.com)
  * @since 1.0
  * 
  */
 public class MergerPostProcessor implements IPostProcessor {
 
 	private JavaCodeMerger delegate;
 
 	/**
 	 * Creates a new Merger Processor for generation which name is provided as
 	 * param
 	 * 
 	 * @param generatorName
 	 */
 	public MergerPostProcessor(JavaCodeMerger javaCodeMerger) {
 		this.delegate = javaCodeMerger;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
	 * net.atos.optimus.m2t.java.core.IPostProcessor#beforeWrite(net.atos.optimus
	 * .m2t.java.core.FileHandler)
 	 */
 	public void beforeWrite(FileHandler fileHandler) {
 
 		File file = new File(fileHandler.getFilePath());
 		if (!file.exists())
 			return;
 
 		try {
 			String fileContent = getFileContent(fileHandler.getFilePath());
 			String merged = delegate.merge(fileContent, fileHandler.getWriter().toString());
 			StringWriter mergedWriter = new StringWriter();
 			mergedWriter.write(merged);
 			fileHandler.setWriter(mergedWriter);
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (JavaModelException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 
 	private static final String getFileContent(String path) throws FileNotFoundException, IOException {
 		RandomAccessFile raf = null;
 
 		try {
 			raf = new RandomAccessFile(path, "r");
 			byte[] contentAsCharArray = new byte[(int) raf.length()];
 			raf.readFully(contentAsCharArray);
 			return new String(contentAsCharArray);
 		} finally {
 			if (raf != null) {
 				try {
 					raf.close();
 				} catch (IOException ioe) {
 				}
 			}
 		}
 	}
 
 }
