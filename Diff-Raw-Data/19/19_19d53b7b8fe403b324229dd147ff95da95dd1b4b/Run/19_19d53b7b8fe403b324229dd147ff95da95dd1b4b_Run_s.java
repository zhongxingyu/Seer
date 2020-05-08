 /*
  * Copyright (c) 2010-2012 The University of Manchester, UK.
  *
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *
  * * Redistributions of source code must retain the above copyright notice,
  *   this list of conditions and the following disclaimer.
  *
  * * Redistributions in binary form must reproduce the above copyright notice,
  *   this list of conditions and the following disclaimer in the documentation
  *   and/or other materials provided with the distribution.
  *
  * * Neither the names of The University of Manchester nor the names of its
  *   contributors may be used to endorse or promote products derived from this
  *   software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  */
 
 package uk.org.taverna.server.client;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import javax.xml.bind.DatatypeConverter;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang.math.LongRange;
 
 import uk.org.taverna.server.client.connection.MimeType;
 import uk.org.taverna.server.client.connection.UserCredentials;
 import uk.org.taverna.server.client.util.IOUtils;
 import uk.org.taverna.server.client.util.URIUtils;
 import uk.org.taverna.server.client.xml.Resources.Label;
 import uk.org.taverna.server.client.xml.RunResources;
 import uk.org.taverna.server.client.xml.XMLReader;
 import uk.org.taverna.server.client.xml.XMLWriter;
 
 /**
  * The Run class represents a workflow run on a Taverna Server instance. It is
  * created by supplying a Server instance on which to create it and a workflow
  * to be run.
  * 
  * @author Robert Haines
  */
 public final class Run {
 
 	/*
 	 * Internal names to use for storing baclava input and output in files on
 	 * the server.
 	 */
 	private static final String BACLAVA_IN_FILE = "in.baclava";
 	private static final String BACLAVA_OUT_FILE = "out.baclava";
 
 	private final URI uri;
 	private final Server server;
 	private final String id;
 	private byte[] workflow;
 	private boolean baclavaIn;
 	private boolean baclavaOut;
 
 	private boolean deleted;
 
 	private RunResources resources;
 
 	private final UserCredentials credentials;
 
 	// Ports
 	private Map<String, InputPort> inputPorts = null;
 	private Map<String, OutputPort> outputPorts = null;
 
 	/*
 	 * Create a Run instance. This will already have been created on the remote
 	 * server.
 	 */
 	private Run(URI uri, Server server, byte[] workflow,
 			UserCredentials credentials) {
 		this.uri = uri;
 		this.server = server;
 		this.id = URIUtils.extractFinalPathComponent(uri);
 		this.workflow = workflow;
 		this.baclavaIn = false;
 		this.baclavaOut = false;
 
 		this.credentials = credentials;
 		resources = null;
 
 		this.deleted = false;
 	}
 
 	/*
 	 * Internal constructor for other classes within the package to create
 	 * "lightweight" runs. Used when listing runs, etc.
 	 */
 	Run(URI uri, Server server, UserCredentials credentials) {
 		this(uri, server, null, credentials);
 	}
 
 	/**
 	 * 
 	 * @param server
 	 * @param workflow
 	 * @param credentials
 	 * @return
 	 */
 	public static Run create(Server server, byte[] workflow,
 			UserCredentials credentials) {
 		URI uri = server.initializeRun(workflow, credentials);
 
 		return new Run(uri, server, workflow, credentials);
 	}
 
 	/**
 	 * 
 	 * @param server
 	 * @param workflow
 	 * @param credentials
 	 * @return
 	 * @throws IOException
 	 */
 	public static Run create(Server server, File workflow,
 			UserCredentials credentials) throws IOException {
 		return create(server, FileUtils.readFileToByteArray(workflow),
 				credentials);
 	}
 
 	/**
 	 * 
 	 * @return
 	 */
 	public URI getURI() {
 		return uri;
 	}
 
 	/**
 	 * 
 	 * @return
 	 */
 	public Server getServer() {
 		return server;
 	}
 
 	/**
 	 * 
 	 * @return
 	 */
 	public Map<String, InputPort> getInputPorts() {
 		if (inputPorts == null) {
 			inputPorts = getInputPortInfo();
 		}
 
 		return inputPorts;
 	}
 
 	/**
 	 * 
 	 * @param name
 	 * @return
 	 */
 	public InputPort getInputPort(String name) {
 		return getInputPorts().get(name);
 	}
 
 	/**
 	 * 
 	 * @return
 	 */
 	public Map<String, OutputPort> getOutputPorts() {
 		if (outputPorts == null) {
 			outputPorts = getOutputPortInfo();
 		}
 
 		return outputPorts;
 	}
 
 	/**
 	 * 
 	 * @param name
 	 * @return
 	 */
 	public OutputPort getOutputPort(String name) {
 		return getOutputPorts().get(name);
 	}
 
 	/**
 	 * Upload data to a file in this Run instance's workspace on the server.
 	 * 
 	 * @param data
 	 *            The data to upload.
 	 * @param remoteName
 	 *            The name of the file to save the data in on the server.
 	 * @param remoteDirectory
 	 *            The directory within the workspace in which to save the data.
 	 *            This directory must already exist.
 	 * @return the URI of the uploaded data on the remote server.
 	 */
 	public URI uploadData(byte[] data, String remoteName,
 			String remoteDirectory) {
 
 		return uploadData(new ByteArrayInputStream(data), remoteName,
 				remoteDirectory);
 	}
 
 	/**
 	 * Upload data to a file in this Run instance's workspace on the server.
 	 * 
 	 * @param data
 	 *            The data to upload.
 	 * @param remoteName
 	 *            The name of the file to save the data in on the server.
 	 * @return the URI of the uploaded data on the remote server.
 	 */
 	public URI uploadData(byte[] data, String remoteName) {
 
 		return uploadData(data, remoteName, null);
 	}
 
 	/**
 	 * Upload data to a file in this Run instance's workspace on the server.
 	 * 
 	 * @param stream
 	 *            The stream with the data to be uploaded.
 	 * @param remoteName
 	 *            The name of the file to save the data in on the server.
 	 * @param remoteDirectory
 	 *            The directory within the workspace in which to save the data.
 	 *            This directory must already exist.
 	 * @return the URI of the uploaded data on the remote server.
 	 */
 	public URI uploadData(InputStream stream, String remoteName,
 			String remoteDirectory) {
 		URI uploadLocation = getLink(Label.WDIR);
 		if (remoteDirectory != null) {
 			uploadLocation = URIUtils.appendToPath(uploadLocation,
 					remoteDirectory);
 		}
 
 		return server.uploadData(uploadLocation, stream, remoteName,
 				credentials);
 	}
 
 	/**
 	 * Upload data to a file in this Run instance's workspace on the server.
 	 * 
 	 * @param stream
 	 *            The stream with the data to be uploaded.
 	 * @param remoteName
 	 *            The name of the file to save the data in on the server.
 	 * @return the URI of the uploaded data on the remote server.
 	 */
 	public URI uploadData(InputStream stream, String remoteName) {
 
 		return uploadData(stream, remoteName, null);
 	}
 
 	/**
 	 * Upload a file to this Run instance's workspace on the server.
 	 * 
 	 * @param file
 	 *            The file to upload.
 	 * @param remoteDirectory
 	 *            The directory within the workspace to upload the file to.
 	 * @param rename
 	 *            The name to use for the file when saving it in the workspace.
 	 * @return the name of the file as used on the server.
 	 * @throws FileNotFoundException
 	 *             if the file does not exist or cannot be read.
 	 */
 	public String uploadFile(File file, String remoteDirectory, String rename)
 			throws FileNotFoundException {
 
 		URI uploadLocation = getLink(Label.WDIR);
 		if (remoteDirectory != null) {
 			uploadLocation = URIUtils.appendToPath(uploadLocation,
 					remoteDirectory);
 		}
 
 		return server.uploadFile(uploadLocation, file, rename, credentials);
 	}
 
 	/**
 	 * Upload a file to this Run instance's workspace on the server.
 	 * 
 	 * @param file
 	 *            The file to upload.
 	 * @param remoteDirectory
 	 *            The directory within the workspace to upload the file to.
 	 * @return the name of the file as used on the server.
 	 * @throws IOException
 	 */
 	public String uploadFile(File file, String remoteDirectory)
 			throws IOException {
 		return uploadFile(file, remoteDirectory, null);
 	}
 
 	/**
 	 * Upload a file to this Run instance's workspace on the server.
 	 * 
 	 * @param file
 	 *            The file to upload.
 	 * @return the name of the file as used on the server.
 	 * @throws IOException
 	 */
 	public String uploadFile(File file) throws IOException {
 		return uploadFile(file, null, null);
 	}
 
 	/**
 	 * Upload baclava data to specify all input port value.
 	 * 
 	 * @param data
 	 *            The data to upload.
 	 */
 	public void setBaclavaInput(byte[] data) {
 		RunStatus rs = getStatus();
 		if (rs == RunStatus.INITIALIZED) {
 			uploadData(data, BACLAVA_IN_FILE);
 			server.setData(getLink(Label.BACLAVA), BACLAVA_IN_FILE, credentials);
 
 			baclavaIn = true;
 		} else {
 			throw new RunStateException(rs, RunStatus.INITIALIZED);
 		}
 	}
 
 	/**
 	 * Upload a baclava file to specify all input port values.
 	 * 
 	 * @param file
 	 *            The file to upload.
 	 * @throws IOException
 	 */
 	public void setBaclavaInput(File file) throws IOException {
 		RunStatus rs = getStatus();
 		if (rs == RunStatus.INITIALIZED) {
 			uploadFile(file, null, BACLAVA_IN_FILE);
 			server.setData(getLink(Label.BACLAVA), BACLAVA_IN_FILE, credentials);
 
 			baclavaIn = true;
 		} else {
 			throw new RunStateException(rs, RunStatus.INITIALIZED);
 		}
 	}
 
 	/**
 	 * Is this run using baclava to set all its input ports?
 	 * 
 	 * @return true if yes, false if not.
 	 */
 	public boolean isBaclavaInput() {
 		// if baclavaIn is true then we know that is correct, else we check.
 		if (baclavaIn) {
 			return true;
 		} else {
 			String test = server.getDataString(getLink(Label.BACLAVA),
 					credentials);
 
 			// if we get back the baclava input file name we are using it.
 			if (test.equals(BACLAVA_IN_FILE)) {
 				baclavaIn = true;
 			}
 
 			return baclavaIn;
 		}
 	}
 
 	/**
 	 * Is this run using baclava to return all its output port data?
 	 * 
 	 * @return true if yes, false if not.
 	 */
 	public boolean isBaclavaOutput() {
 		// if baclavaOut is true then we know that is correct, else we check.
 		if (baclavaOut) {
 			return true;
 		} else {
 			String test = server.getDataString(getLink(Label.OUTPUT),
 					credentials);
 
 			// if we get back the baclava output file name we are using it.
 			if (test.equals(BACLAVA_OUT_FILE)) {
 				baclavaOut = true;
 			}
 
 			return baclavaOut;
 		}
 	}
 
 	/**
 	 * Set the server to return outputs for this Run in baclava format. This
 	 * must be set before the Run is started.
 	 */
 	public void requestBaclavaOutput() {
 		// don't try and request it again!
 		if (baclavaOut) {
 			return;
 		}
 
 		RunStatus rs = getStatus();
 		if (rs == RunStatus.INITIALIZED) {
 			server.setData(getLink(Label.OUTPUT), BACLAVA_OUT_FILE, credentials);
 
 			baclavaOut = true;
 		} else {
 			throw new RunStateException(rs, RunStatus.INITIALIZED);
 		}
 	}
 
 	/**
 	 * Get the outputs of this Run as a baclava formatted document. The Run must
 	 * have been set to output in baclava format before it is started.
 	 * 
 	 * @return The baclava formatted document contents as a byte array.
 	 * @see #requestBaclavaOutput()
 	 * @see #getBaclavaOutputStream()
 	 * @see #writeBaclavaOutputToFile(File)
 	 */
 	public byte[] getBaclavaOutput() {
 		RunStatus rs = getStatus();
 		if (rs == RunStatus.FINISHED) {
 			URI baclavaLink = URIUtils.appendToPath(getLink(Label.WDIR),
 					BACLAVA_OUT_FILE);
 			if (!baclavaOut) {
 				throw new AttributeNotFoundException(baclavaLink);
 			}
 
 			return server.getData(baclavaLink, MimeType.BYTES, credentials);
 		} else {
 			throw new RunStateException(rs, RunStatus.FINISHED);
 		}
 	}
 
 	/**
 	 * Get an input stream that can be used to stream the baclava output data of
 	 * this run. The Run must have been set to output in baclava format before
 	 * it is started.
 	 * 
 	 * <b>Note:</b> You are responsible for closing the stream once you have
 	 * finished with it. Not doing so may prevent further use of the underlying
 	 * network connection.
 	 * 
 	 * @return The stream to read the baclava data from.
 	 * @see #getBaclavaOutput()
 	 * @see #writeBaclavaOutputToFile(File)
 	 * @see #requestBaclavaOutput()
 	 */
 	public InputStream getBaclavaOutputStream() {
 		RunStatus rs = getStatus();
 		if (rs == RunStatus.FINISHED) {
 			URI baclavaLink = URIUtils.appendToPath(getLink(Label.WDIR),
 					BACLAVA_OUT_FILE);
 			if (!baclavaOut) {
 				throw new AttributeNotFoundException(baclavaLink);
 			}
 
 			return server.getDataStream(baclavaLink, MimeType.BYTES, null,
 					credentials);
 		} else {
 			throw new RunStateException(rs, RunStatus.FINISHED);
 		}
 	}
 
 	/**
 	 * Writes the baclava output data of this run directly to a file. The data
 	 * is not loaded into memory, it is streamed directly to the file. The file
 	 * is created if it does not already exist and will overwrite existing data
 	 * if it does.
 	 * 
 	 * The Run must have been set to output in baclava format before it is
 	 * started.
 	 * 
 	 * @param file
 	 *            the file to write to.
 	 * @throws FileNotFoundException
 	 *             if the file exists but is a directory rather than a regular
 	 *             file, does not exist but cannot be created, or cannot be
 	 *             opened for any other reason.
 	 * @throws IOException
 	 *             if there is any I/O error.
 	 * @see #getBaclavaOutput()
 	 * @see #getBaclavaOutputStream()
 	 * @see #requestBaclavaOutput()
 	 */
 	public void writeBaclavaOutputToFile(File file) throws IOException {
 		IOUtils.writeStreamToFile(getBaclavaOutputStream(), file);
 	}
 
 	/**
 	 * Get the id of this run.
 	 * 
 	 * @return the id of this run.
 	 */
 	public String getIdentifier() {
 		return id;
 	}
 
 	/**
 	 * Get the status of this Run.
 	 * 
 	 * @return the status of this Run.
 	 */
 	public RunStatus getStatus() {
 		if (deleted) {
 			return RunStatus.DELETED;
 		} else {
 			return RunStatus.state(server.getDataString(getLink(Label.STATUS),
 					credentials));
 		}
 	}
 
 	/**
 	 * Is this Run initialized?
 	 * 
 	 * @return true if the Run is initialized, false otherwise.
 	 */
 	public boolean isInitialized() {
 		return getStatus() == RunStatus.INITIALIZED;
 	}
 
 	/**
 	 * Is this Run running?
 	 * 
 	 * @return true if the Run is running, false otherwise.
 	 */
 	public boolean isRunning() {
 		return getStatus() == RunStatus.RUNNING;
 	}
 
 	/**
 	 * Is this Run finished?
 	 * 
 	 * @return true if the Run is finished, false otherwise.
 	 */
 	public boolean isFinished() {
 		return getStatus() == RunStatus.FINISHED;
 	}
 
 	public boolean isDeleted() {
 		return deleted;
 	}
 
 	/**
 	 * Start this Run running on the server. The Run must not be already
 	 * running, or finished.
 	 * 
 	 * @throws IOException
 	 */
 	public void start() throws IOException {
 		RunStatus rs = getStatus();
 		if (rs != RunStatus.INITIALIZED) {
 			throw new RunStateException(rs, RunStatus.INITIALIZED);
 		}
 
 		// set all the inputs
 		if (!isBaclavaInput()) {
 			setAllInputs();
 		}
 
 		server.setData(getLink(Label.STATUS), RunStatus.RUNNING.status(),
 				credentials);
 	}
 
 	/**
 	 * Get the workflow of this Run as a String.
 	 * 
 	 * @return the workflow of this Run as a String.
 	 */
 	public byte[] getWorkflow() {
 		if (workflow == null) {
 			workflow = server.getData(getLink(Label.WORKFLOW), MimeType.XML,
 					credentials);
 		}
 
 		return workflow;
 	}
 
 	/**
 	 * Get the expiry time of this Run as a Date object.
 	 * 
 	 * @return the expiry time of this Run as a Date object.
 	 */
 	public Date getExpiry() {
 		return getTime(Label.EXPIRY);
 	}
 
 	/**
 	 * Set the expiry time of this Run.
 	 * 
 	 * @param time
 	 *            the new expiry time of this Run.
 	 */
 	public void setExpiry(Date time) {
 		Calendar cal = Calendar.getInstance();
 		cal.setTime(time);
 		String expiry = DatatypeConverter.printDateTime(cal);
 		server.setData(getLink(Label.EXPIRY), expiry, credentials);
 	}
 
 	/**
 	 * Delete this Run.
 	 */
 	public boolean delete() {
 		try {
 			server.delete(uri, credentials);
 		} catch (AttributeNotFoundException e) {
 			// Ignore this. Delete is idempotent so deleting a run that has
 			// already been deleted or is for some other reason not there should
 			// happen silently.
 		} finally {
 			deleted = true;
 		}
 
 		return true;
 	}
 
 	/**
 	 * Get the return code of the underlying Taverna Server process. A zero
 	 * value indicates success.
 	 * 
 	 * @return the return code of the underlying Taverna Server process.
 	 */
 	public int getExitCode() {
 		return new Integer(server.getDataString(getLink(Label.EXITCODE),
 				credentials));
 	}
 
 	/**
 	 * Get the console output of the underlying Taverna Server process.
 	 * 
 	 * @return the console output of the underlying Taverna Server process.
 	 */
 	public String getConsoleOutput() {
 		return server.getDataString(getLink(Label.STDOUT), credentials);
 	}
 
 	/**
 	 * Get the console errors of the underlying Taverna Server process.
 	 * 
 	 * @return the console errors of the underlying Taverna Server process.
 	 */
 	public String getConsoleError() {
 		return server.getDataString(getLink(Label.STDERR), credentials);
 	}
 
 	/**
 	 * Get the time that this Run was created as a Date object.
 	 * 
 	 * @return the time that this Run was created.
 	 */
 	public Date getCreateTime() {
 		return getTime(Label.CREATE_TIME);
 	}
 
 	/**
 	 * Get the time that this Run was started as a Date object.
 	 * 
 	 * @return the time that this Run was started.
 	 */
 	public Date getStartTime() {
 		return getTime(Label.START_TIME);
 	}
 
 	/**
 	 * Get the time that this Run finished as a Date object.
 	 * 
 	 * @return the time that this Run finished.
 	 */
 	public Date getFinishTime() {
 		return getTime(Label.FINISH_TIME);
 	}
 
 	private Date getTime(Label time) {
 		String dateTime = server.getDataString(getLink(time), credentials);
 		Calendar cal = DatatypeConverter.parseDateTime(dateTime);
 
 		return cal.getTime();
 	}
 
 	/**
 	 * Get an input stream that can be used to stream all the output data of
 	 * this run in zip format.
 	 * 
 	 * <b>Note:</b> You are responsible for closing the stream once you have
 	 * finished with it. Not doing so may prevent further use of the underlying
 	 * network connection.
 	 * 
 	 * @return The stream to read the zip data from.
 	 * @see #writeOutputToZipFile(File)
 	 */
 	public InputStream getOutputZipStream() {
 		RunStatus rs = getStatus();
 		if (rs == RunStatus.FINISHED) {
 			URI uri = URIUtils.appendToPath(getLink(Label.WDIR), "out");
 
 			return server.getDataStream(uri, MimeType.ZIP, null, credentials);
 		} else {
 			throw new RunStateException(rs, RunStatus.FINISHED);
 		}
 	}
 
 	/**
 	 * Writes all the output data of this run directly to a file in zip format.
 	 * The data is not loaded into memory, it is streamed directly to the file.
 	 * The file is created if it does not already exist and will overwrite
 	 * existing data if it does.
 	 * 
 	 * @param file
 	 *            the file to write to.
 	 * @throws FileNotFoundException
 	 *             if the file exists but is a directory rather than a regular
 	 *             file, does not exist but cannot be created, or cannot be
 	 *             opened for any other reason.
 	 * @throws IOException
 	 *             if there is any I/O error.
 	 * @see #getOutputZipStream()
 	 */
 	public void writeOutputToZipFile(File file) throws IOException {
 		IOUtils.writeStreamToFile(getOutputZipStream(), file);
 	}
 
 	/**
 	 * Create a directory in the workspace of this Run. At present you can only
 	 * create a directory one level deep.
 	 * 
 	 * @param dir
 	 *            the name of the directory to create.
 	 * @return the {@link URI} of the created directory.
 	 * @throws IllegalArgumentException
 	 *             if an attempt to create a directory more than one level deep
 	 *             is made.
 	 */
 	public URI mkdir(String dir) {
 		if (dir.contains("/")) {
 			throw new IllegalArgumentException(
 					"Directories can only be created one level deep.");
 		}
 
 		return server.mkdir(getLink(Label.WDIR), dir, credentials);
 	}
 
 	/*
 	 * Set all the inputs on the server. The inputs must have been set prior to
 	 * this call using the InputPort API or a runtime exception is thrown.
 	 */
 	private void setAllInputs() throws IOException {
 		List<String> missingPorts = new ArrayList<String>();
 
 		for (InputPort port : getInputPorts().values()) {
 			if (!port.isSet()) {
 				missingPorts.add(port.getName());
			}

			if (port.isFile()) {
				// If we're using a local file upload it first then set the
				// port to use a remote file.
				if (!port.isRemoteFile()) {
					String file = uploadFile(port.getFile());
					port.setRemoteFile(file);
 				}
 
 				setInputPort(port);
			} else {
				setInputPort(port);
 			}
 		}
 
 		if (!missingPorts.isEmpty()) {
 			throw new RunInputsNotSetException(id, missingPorts);
 		}
 	}
 
 	private void setInputPort(InputPort port) {
 		URI path = URIUtils.appendToPath(getLink(Label.INPUT),
 				"/input/" + port.getName());
 		byte[] value;
 
 		if (port.isFile()) {
 			value = XMLWriter.inputFile(port.getFile());
 		} else {
 			value = XMLWriter.inputValue(port.getValue());
 		}
 
 		server.setData(path, value, MimeType.XML, credentials);
 	}
 
 	private RunResources getRunResources() {
 		if (resources == null) {
 			resources = server.getXMLReader()
 					.readRunResources(uri, credentials);
 		}
 
 		return resources;
 	}
 
 	private Map<String, InputPort> getInputPortInfo() {
 		XMLReader reader = server.getXMLReader();
 
 		return reader.readInputPortDescription(this,
 				getLink(Label.EXPECTED_INPUTS), credentials);
 	}
 
 	private Map<String, OutputPort> getOutputPortInfo() {
 		XMLReader reader = server.getXMLReader();
 
 		return reader.readOutputPortDescription(this, getLink(Label.OUTPUT),
 				credentials);
 	}
 
 	byte[] getOutputData(URI uri, LongRange range) {
 		return server.getData(uri, MimeType.BYTES, range, credentials);
 	}
 
 	InputStream getOutputDataStream(URI uri, LongRange range) {
 		return server.getDataStream(uri, MimeType.BYTES, range, credentials);
 	}
 
 	private URI getLink(Label key) {
 		return getRunResources().get(key);
 	}
 }
