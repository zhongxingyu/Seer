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
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URI;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 
 import net.sf.practicalxml.ParseUtil;
 import net.sf.practicalxml.XmlUtil;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang.math.IntRange;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import uk.org.taverna.server.client.connection.URIUtils;
 import uk.org.taverna.server.client.connection.UserCredentials;
 
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
 
 	private final Server server;
 	private final String id;
 	private byte[] workflow;
 	private boolean baclavaIn;
 	private boolean baclavaOut;
 
 	private final Map<String, URI> links;
 
 	private final XmlUtils xmlUtils;
 
 	private final UserCredentials credentials;
 
 	// Ports
 	private Map<String, InputPort> inputPorts = null;
 	private Map<String, OutputPort> outputPorts = null;
 
 	/**
 	 * Create a new Run instance on the specified server with the supplied
 	 * workflow.
 	 * 
 	 * @param server
 	 *            The server to create the Run on.
 	 * @param workflow
 	 *            The workflow associated with the Run.
 	 * @param id
 	 * 
 	 * @param credentials
 	 */
 	private Run(Server server, byte[] workflow, String id,
 			UserCredentials credentials) {
 		this.server = server;
 		this.id = id;
 		this.workflow = workflow;
 		this.baclavaIn = false;
 		this.baclavaOut = false;
 
 		xmlUtils = XmlUtils.getInstance();
 
 		this.credentials = credentials;
 		links = getRunDescription(this.credentials);
 	}
 
 	/**
 	 * Create a new Run instance to represent a run that is already on the
 	 * specified server. This constructor is provided for internal use when
 	 * lists of runs are being built up in a Server instance.
 	 * 
 	 * @param server
 	 *            The server the Run is already on.
 	 * @param id
 	 *            The id of the Run.
 	 * @param credentials
 	 */
 	Run(Server server, String id, UserCredentials credentials) {
 		this.server = server;
 		this.id = id;
 		this.workflow = null;
 		this.baclavaOut = false;
 
 		xmlUtils = XmlUtils.getInstance();
 
 		this.credentials = credentials;
 		links = getRunDescription(this.credentials);
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
 		String id = server.initializeRun(workflow, credentials);
 
 		return new Run(server, workflow, id, credentials);
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
 	 */
 	public void uploadData(byte[] data, String remoteName,
 			String remoteDirectory) {
 		URI uploadLocation = links.get("wdir");
 		if (remoteDirectory != null) {
 			uploadLocation = URIUtils
 					.addToPath(uploadLocation, remoteDirectory);
 		}
 
 		server.uploadData(uploadLocation, data, remoteName, credentials);
 	}
 
 	/**
 	 * Upload data to a file in this Run instance's workspace on the server.
 	 * 
 	 * @param data
 	 *            The data to upload.
 	 * @param remoteName
 	 *            The name of the file to save the data in on the server.
 	 */
 	public void uploadData(byte[] data, String remoteName) {
 		uploadData(data, remoteName, null);
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
 	 * @throws IOException
 	 */
 	public String uploadFile(File file, String remoteDirectory, String rename)
 			throws IOException {
 		URI uploadLocation = links.get("wdir");
 		if (remoteDirectory != null) {
 			uploadLocation = URIUtils
 					.addToPath(uploadLocation, remoteDirectory);
 		}
 
 		return server.uploadFile(this, file, uploadLocation, rename,
 				credentials);
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
 			server.setRunAttribute(this, links.get("baclava"), BACLAVA_IN_FILE,
 					"text/plain", credentials);
 
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
 		byte[] data = FileUtils.readFileToByteArray(file);
 		setBaclavaInput(data);
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
 			String test = server.getRunAttribute(this, links.get("baclava"),
 					"text/plain", credentials);
 
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
			String test = server.getRunAttribute(this, links.get("output"),
					"text/plain", credentials);

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
 			server.setRunAttribute(this, links.get("output"), BACLAVA_OUT_FILE,
 					"text/plain", credentials);
 
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
 	 * @see #setBaclavaOutput()
 	 * @see #setBaclavaOutput(String)
 	 */
 	public byte[] getBaclavaOutput() {
 		RunStatus rs = getStatus();
 		if (rs == RunStatus.FINISHED) {
 			URI baclavaLink = URIUtils.addToPath(links.get("wdir"),
 					BACLAVA_OUT_FILE);
 			if (!baclavaOut) {
 				throw new AttributeNotFoundException(baclavaLink);
 			}
 
 			return server.getRunData(this, baclavaLink,
 					"application/octet-stream", credentials);
 		} else {
 			throw new RunStateException(rs, RunStatus.FINISHED);
 		}
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
 		return RunStatus.state(server.getRunAttribute(this,
 				links.get("status"), "text/plain", credentials));
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
 
 		server.setRunAttribute(this, links.get("status"),
 				RunStatus.RUNNING.status(), "text/plain", credentials);
 	}
 
 	/**
 	 * Get the workflow of this Run as a String.
 	 * 
 	 * @return the workflow of this Run as a String.
 	 */
 	public byte[] getWorkflow() {
 		if (workflow == null) {
 			workflow = server.getRunData(this, links.get("workflow"),
 					"application/xml", credentials);
 		}
 
 		return workflow;
 	}
 
 	/**
 	 * Get the expiry time of this Run as a Date object.
 	 * 
 	 * @return the expiry time of this Run as a Date object.
 	 */
 	public Date getExpiry() {
 		return getTime("expiry");
 	}
 
 	/**
 	 * Set the expiry time of this Run.
 	 * 
 	 * @param time
 	 *            the newexpiry time of this Run.
 	 */
 	public void setExpiry(Date time) {
 		System.out.println(XmlUtil.formatXsdDatetime(time));
 	}
 
 	/**
 	 * Delete this Run.
 	 */
 	public void delete() {
 		server.deleteRun(this, credentials);
 	}
 
 	/**
 	 * Get the return code of the underlying Taverna Server process. A zero
 	 * value indicates success.
 	 * 
 	 * @return the return code of the underlying Taverna Server process.
 	 */
 	public int getExitCode() {
 		return new Integer(server.getRunAttribute(this, links.get("exitcode"),
 				"text/plain", credentials));
 	}
 
 	/**
 	 * Get the console output of the underlying Taverna Server process.
 	 * 
 	 * @return the console output of the underlying Taverna Server process.
 	 */
 	public String getConsoleOutput() {
 		return server.getRunAttribute(this, links.get("stdout"), "text/plain",
 				credentials);
 	}
 
 	/**
 	 * Get the console errors of the underlying Taverna Server process.
 	 * 
 	 * @return the console errors of the underlying Taverna Server process.
 	 */
 	public String getConsoleError() {
 		return server.getRunAttribute(this, links.get("stderr"), "text/plain",
 				credentials);
 	}
 
 	/**
 	 * Get the time that this Run was created as a Date object.
 	 * 
 	 * @return the time that this Run was created.
 	 */
 	public Date getCreateTime() {
 		return getTime("createtime");
 	}
 
 	/**
 	 * Get the time that this Run was started as a Date object.
 	 * 
 	 * @return the time that this Run was started.
 	 */
 	public Date getStartTime() {
 		return getTime("starttime");
 	}
 
 	/**
 	 * Get the time that this Run finished as a Date object.
 	 * 
 	 * @return the time that this Run finished.
 	 */
 	public Date getFinishTime() {
 		return getTime("finishtime");
 	}
 
 	private Date getTime(String time) {
 		return XmlUtil.parseXsdDatetime(server.getRunAttribute(this,
 				links.get(time), "text/plain", credentials));
 
 	}
 
 	/**
 	 * Create a directory in the workspace of this Run.
 	 * 
 	 * @param dir
 	 *            the name of the directory to create.
 	 * @throws IOException
 	 */
 	public void mkdir(String dir) throws IOException {
 		if (dir.contains("/")) {
 			int lastSlash = dir.lastIndexOf("/");
 			String leaf = dir.substring(lastSlash + 1, dir.length());
 			String path = dir.substring(0, lastSlash);
 			server.makeRunDir(this,
 					URIUtils.addToPath(links.get("wdir"), path), leaf,
 					credentials);
 		} else {
 			server.makeRunDir(this, links.get("wdir"), dir, credentials);
 		}
 	}
 
 	/*
 	 * Set all the inputs on the server. The inputs must have been set prior to
 	 * this call using the InputPort API.
 	 */
 	private void setAllInputs() throws IOException {
 		for (InputPort port : getInputPorts().values()) {
 			if (!port.isSet()) {
 				continue;
 			}
 
 			if (port.isFile()) {
 				// If we're using a local file upload it first then set the
 				// port to use a remote file.
 				if (!port.isRemoteFile()) {
 					File file = new File(uploadFile(port.getFile()));
 					port.setRemoteFile(file);
 				}
 
 				setInputPort(port);
 			} else {
 				setInputPort(port);
 			}
 		}
 	}
 
 	private void setInputPort(InputPort port) {
 		URI path = URIUtils.addToPath(links.get("inputs"),
 				"/input/" + port.getName());
 		String value;
 
 		if (port.isFile()) {
 			String payload = xmlUtils.escapeXML(port.getFile().getPath());
 			value = xmlUtils.buildXMLFragment("inputfile", payload);
 		} else {
 			String payload = xmlUtils.escapeXML(port.getValue());
 			value = xmlUtils.buildXMLFragment("inputvalue", payload);
 		}
 
 		server.setRunAttribute(this, path, value, "application/xml",
 				credentials);
 	}
 
 	private Map<String, URI> getRunDescription(UserCredentials credentials) {
 		HashMap<String, URI> links = new HashMap<String, URI>();
 
 		// parse out the simple stuff
 		String description = server.getRunDescription(this, credentials);
 		Document doc = ParseUtil.parse(description);
 
 		links.put("expiry",
 				xmlUtils.evalXPathHref(doc, "//nsr:expiry"));
 		links.put("workflow",
 				xmlUtils.evalXPathHref(doc, "//nsr:creationWorkflow"));
 		links.put("status",
 				xmlUtils.evalXPathHref(doc, "//nsr:status"));
 		links.put("createtime",
 				xmlUtils.evalXPathHref(doc, "//nsr:createTime"));
 		links.put("starttime",
 				xmlUtils.evalXPathHref(doc, "//nsr:startTime"));
 		links.put("finishtime",
 				xmlUtils.evalXPathHref(doc, "//nsr:finishTime"));
 		links.put("wdir",
 				xmlUtils.evalXPathHref(doc, "//nsr:workingDirectory"));
 		links.put("inputs",
 				xmlUtils.evalXPathHref(doc, "//nsr:inputs"));
 		links.put("output",
 				xmlUtils.evalXPathHref(doc, "//nsr:output"));
 		links.put("securectx",
 				xmlUtils.evalXPathHref(doc, "//nsr:securityContext"));
 		links.put("listeners",
 				xmlUtils.evalXPathHref(doc, "//nsr:listeners"));
 
 		// get the inputs
 		String inputs = server.getRunAttribute(this, links.get("inputs"),
 				"application/xml", credentials);
 		doc = ParseUtil.parse(inputs);
 		links.put("baclava",
 				xmlUtils.evalXPathHref(doc, "//nsr:baclava"));
 		links.put("inputexp",
 				xmlUtils.evalXPathHref(doc, "//nsr:expected"));
 
 		// set io properties
 		links.put("io", URIUtils.addToPath(links.get("listeners"), "/io"));
 		links.put("stdout",
 				URIUtils.addToPath(links.get("io"), "/properties/stdout"));
 		links.put("stderr",
 				URIUtils.addToPath(links.get("io"), "/properties/stderr"));
 		links.put("exitcode",
 				URIUtils.addToPath(links.get("io"), "/properties/exitcode"));
 
 		return links;
 	}
 
 	private Map<String, InputPort> getInputPortInfo() {
 		Map<String, InputPort> ports = new HashMap<String, InputPort>();
 
 		String portDesc = server.getRunAttribute(this, links.get("inputexp"),
 				"application/xml", credentials);
 		Document doc = ParseUtil.parse(portDesc);
 
 		for (Element e : xmlUtils.evalXPath(doc, "//port:input")) {
 			InputPort port = new InputPort(this, e);
 			ports.put(port.getName(), port);
 		}
 
 		return ports;
 	}
 
 	private Map<String, OutputPort> getOutputPortInfo() {
 		Map<String, OutputPort> ports = new HashMap<String, OutputPort>();
 
 		String portDesc = server.getRunAttribute(this, links.get("output"),
 				"application/xml", credentials);
 		Document doc = ParseUtil.parse(portDesc);
 
 		for (Element e : xmlUtils.evalXPath(doc, "//port:output")) {
 			OutputPort port = new OutputPort(this, e);
 			ports.put(port.getName(), port);
 		}
 
 		return ports;
 	}
 
 	byte[] getOutputData(URI uri, IntRange range) {
 		return server.getRunData(this, uri,
 				"application/octet-stream", range, credentials);
 	}
 }
