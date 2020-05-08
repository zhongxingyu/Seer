 /*******************************************************************************
  * Copyright (c) 2007, 2008 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.communication.core.attachment;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 
 import org.eclipse.core.runtime.Assert;
 
 /**
  * This class contains data constructed from a File object or a URL. If this
  * object is then passed as parameter in your webservice call, the contained
  * data is passed as SOAP attachment. This is helpful for large and binary data
  * transport
  * 
  * @author Christian Campo
  */
 public class Attachment {
 
 	protected enum Type {
 		FILE, URL, INPUTSTREAM
 	}
 
 	private static final int READ_BLOCK_SIZE = 10000;
 
 	// make all fields that are not in AttachmentSerialized transient, so
 	// hessian does not try to transfer them
 	// type how the attachment was constructed, is either FILE, URL or
 	// INPUTSTREAM
 	private transient Type type;
 	// contains the file object from which the Attachment was constructed
 	private transient File file;
 	// contains the url object from which the Attachment was constructed
 	//	private transient URL url;
 	// contains the datasource with the data
 	private transient IDataSource dataSource;
 
 	// byte array for data when using a binary protocol i.e. hessian
 	// this array is not explicitly set but rather "magically" filled by hessian
 	// upon deserialization
 	private ByteArrayDataSource internalDataSource = null;
 
 	// private constructor used by hessian
 	private Attachment() {
 		super();
 	}
 
 	/**
 	 * @param file
 	 *            create attachment from file content
 	 * @pre file!=null
 	 * @pre file.exist==true
 	 * @throws java.io.FileNotFoundException
 	 */
 	public Attachment(File file) throws FileNotFoundException, IOException {
 		this();
 		type = Type.FILE;
 		this.file = file;
 		this.dataSource = new FileDataSource(file);
 		this.dataSource.checkValid();
 	}
 
 	/**
 	 * @param url
 	 *            create attachment from url content
 	 */
 	public Attachment(URL url) throws IOException {
 		this();
 		type = Type.URL;
 		//		this.url = url;
 		this.dataSource = new HttpURLDataSource(url);
 		this.dataSource.checkValid();
 	}
 
 	/**
 	 * @param dataHandler
 	 */
 	public Attachment(InputStream inputStream) throws IOException {
 		this();
 		type = Type.INPUTSTREAM;
 		this.dataSource = new InputStreamDataSource(inputStream);
 		this.dataSource.checkValid();
 	}
 
 	protected Attachment(ByteArrayDataSource dataSource) {
 		this();
 		type = Type.INPUTSTREAM;
 		this.dataSource = dataSource;
 	}
 
 	/**
 	 * @return InputStream
 	 * @throws IOException
 	 */
 	public InputStream readAsStream() throws IOException {
 		return dataSource.getInputStream();
 	}
 
 	/**
 	 * Gets the attachment as file. File is created with this request. Caller
 	 * must delete file after use.
 	 * 
 	 * @param fullFilePath
 	 *            (full file path from where the attachemnt should be opened,
 	 *            includes the filename)
 	 * @return read the content of the attachment as file object (requires temp
 	 *         storage)
 	 * @throws IOException
 	 */
 	public File readAsFile(String fullFilePath) throws IOException {
 		InputStream input = readAsStream();
 		if (input == null) {
 			throw new IOException("no inputstream to save as file"); //$NON-NLS-1$
 		}
 		FileOutputStream output = new FileOutputStream(fullFilePath);
 		try {
 			byte[] b = new byte[READ_BLOCK_SIZE];
 			int length = 0;
 			while ((length = input.read(b)) == READ_BLOCK_SIZE) {
 				output.write(b, 0, length);
 			}
 			if (length > 0) {
 				output.write(b, 0, length);
 			}
 			File returnFile = new File(fullFilePath);
 			return returnFile;
 		} finally {
 			output.close();
 		}
 	}
 
 	/**
 	 * @return return the construction type of the attachment
 	 */
 	protected Type getType() {
 		return type;
 	}
 
 	/**
 	 * @return File object that was used when creating the attachment
 	 * @pre getType()==Type.FILE;
 	 */
 	protected File getInternalFile() {
 		Assert.isTrue(getType() == Type.FILE, "invalid type when getting File object"); //$NON-NLS-1$
 		return file;
 	}
 
 	/**
 	 * this method is called by hessian before an object is serialized. It
 	 * stores that blob in a different unrelated object and returns it as the
 	 * new attachment object. We need to make sure that the private fields of
 	 * AttachmentSerialized are also available in Attachment as they are
 	 * magically filled when deserializing.
 	 * 
 	 * @return Object
 	 */
 	public Object writeReplace() {
 		try {
 			if (type == Type.INPUTSTREAM) {
 				try {
 					InputStream input = dataSource.getInputStream();
					if (input != null) {
 						input.reset();
 					}
 				} catch (IOException e) {
					// TODO: null the dataSource if the inputstream does not work
					// anyway ??
 				}
 			}
 		} catch (Throwable e) {
 			return new AttachmentSerialized(dataSource);
 		}
		return new AttachmentSerialized(dataSource); // to feed the compiler,
		// since it will never
		// get here
 	}
 
 	/**
 	 * this method is called by hessian after an object was fully deserialized
 	 * we use it to create the dataHandler Object from the internal byte array
 	 * 
 	 * @return Object
 	 */
 	public Object readResolve() {
 		dataSource = internalDataSource;
 		type = Type.INPUTSTREAM;
 		internalDataSource = null;
 		return this;
 	}
 }
