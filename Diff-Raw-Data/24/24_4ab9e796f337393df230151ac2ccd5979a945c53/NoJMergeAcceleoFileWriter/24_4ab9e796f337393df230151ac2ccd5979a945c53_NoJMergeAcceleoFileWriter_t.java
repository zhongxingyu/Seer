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
 package net.atos.optimus.m2t.java.core.internal;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.StringWriter;
 import java.io.Writer;
 
 import org.eclipse.acceleo.engine.generation.writers.AbstractAcceleoWriter;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
 import org.eclipse.emf.common.EMFPlugin;
 
 /**
  * Wraps a buffered FileWriter for generation with Acceleo. Note that this can
  * also be used to create a Writer that will previously merge the content of an
  * existing file with the new content through JMerge.
  * 
  * This class is based on the AcceleoFileWriter, except that the JMerge
  * invocation was removed
  * 
  * @author Maxence Vanbésien (mvaawl@gmail.com)
  * @since 1.0
  */
 public final class NoJMergeAcceleoFileWriter extends AbstractAcceleoWriter {
 
 	/** Keeps a reference to the target file's absolute path. */
 	private final String targetPath;
 	private String selectedCharset;
 
 	/**
 	 * Constructs a buffered file writer around the given file. The file will be
 	 * created with the default System encoding.
 	 * 
 	 * @param target
 	 *            File in which this writer will append text.
 	 * @param appendMode
 	 *            Tells us wether the former content of the file should be
 	 *            deleted.
 	 * @throws IOException
 	 *             Thrown if the target file doesn't exist and cannot be
 	 *             created.
 	 */
 	public NoJMergeAcceleoFileWriter(File target, boolean appendMode) throws IOException {
 		delegate = new BufferedWriter(new FileWriter(target, appendMode));
 		targetPath = target.getAbsolutePath();
 	}
 
 	/**
 	 * Constructs a buffered file writer around the given file and tells which
 	 * encoding should be used to generate the file.
 	 * 
 	 * @param target
 	 *            File in which this writer will append text.
 	 * @param appendMode
 	 *            Tells us wether the former content of the file should be
 	 *            deleted.
 	 * @param charset
 	 *            Encoding that should be used to create the target file.
 	 * @throws IOException
 	 *             Thrown if the target file doesn't exist and cannot be
 	 *             created.
 	 */
 	public NoJMergeAcceleoFileWriter(File target, boolean appendMode, String charset) throws IOException {
 		final OutputStream fileOutputStream = new FileOutputStream(target, appendMode);
 		final OutputStreamWriter fileWriter = new OutputStreamWriter(fileOutputStream, charset);
 		delegate = new BufferedWriter(fileWriter);
 		targetPath = target.getAbsolutePath();
 	}
 
 	/**
 	 * Constructs a writer that will use JMerge to merge the content of the file
 	 * existing at path <em>filePath</em> with its new content. Note that the
 	 * file will be written with the default System encoding if using this.
 	 * 
 	 * @param filePath
 	 *            Path of the file this writer will contain the content of.
 	 */
 	public NoJMergeAcceleoFileWriter(String filePath) {
 		delegate = new StringWriter(DEFAULT_BUFFER_SIZE);
 		targetPath = filePath;
 	}
 
 	/**
 	 * Constructs a writer that will use JMerge to merge the content of the file
 	 * existing at path <em>filePath</em> with its new content.
 	 * 
 	 * @param filePath
 	 *            Path of the file this writer will contain the content of.
 	 * @param charset
 	 *            Encoding that's to be used to create the file with the merged
 	 *            content.
 	 */
 	public NoJMergeAcceleoFileWriter(String filePath, String charset) {
 		this(filePath);
 		selectedCharset = charset;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.acceleo.engine.generation.writers.AbstractAcceleoWriter#close()
 	 */
 	@Override
 	public void close() throws IOException {
 		if (!EMFPlugin.IS_ECLIPSE_RUNNING || this.delegate instanceof BufferedWriter) {
 			delegate.close();
 		} else {
 			Writer writer = null;
 			OutputStream fileOutputStream = null;
 			OutputStreamWriter fileWriter = null;
 			try {
 				if (selectedCharset == null) {
 					writer = new BufferedWriter(new FileWriter(new File(targetPath)));
 				} else {
 					fileOutputStream = new FileOutputStream(new File(targetPath));
 					fileWriter = new OutputStreamWriter(fileOutputStream, selectedCharset);
 					writer = new BufferedWriter(fileWriter);
 				}
 				writer.append(toString());
 				writer.flush();
 			} catch (Exception e) {
				Activator
						.getDefault()
						.getLog()
						.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
								"An exception occurred while writing to writer", e));
 			} finally {
 				if (writer != null) {
 					writer.close();
 				}
				if (fileWriter != null) {
					fileWriter.close();
				}
				if (fileOutputStream != null) {
					fileOutputStream.close();
				}
 			}
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.acceleo.engine.generation.writers.AbstractAcceleoWriter#getTargetPath()
 	 */
 	@Override
 	public String getTargetPath() {
 		return targetPath;
 	}
 }
