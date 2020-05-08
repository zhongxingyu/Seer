 /*
  * Copyright (c) 2010, 2011 The University of Manchester, UK.
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
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.UUID;
 
 import net.sf.practicalxml.ParseUtil;
 import net.sf.practicalxml.XmlUtil;
 
 import org.apache.commons.io.FileUtils;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 /**
  * The Run class represents a workflow run on a Taverna Server instance. It is
  * created by supplying a Server instance on which to create it and a workflow
  * to be run.
  * 
  * @author Robert Haines
  */
 public final class Run {
 
 	private final Server server;
 	private final UUID uuid;
 	private String workflow;
 	private String baclavaOut;
 
 	private final Map<String, String> links;
 
 	private final XmlUtils xmlUtils;
 
 	/**
 	 * Create a new Run instance on the specified server with the supplied
 	 * workflow.
 	 * 
 	 * @param server
 	 *            The server to create the Run on.
 	 * @param workflow
 	 *            The workflow associated with the Run.
 	 */
 	public Run(Server server, String workflow) {
 		this.server = server;
 		this.uuid = server.initializeRun(workflow);
 		this.workflow = workflow;
 		this.baclavaOut = null;
 
 		xmlUtils = XmlUtils.getInstance();
 
 		links = getRunDescription();
 	}
 
 	/**
 	 * Create a new Run instance on the specified server with the supplied
 	 * workflow file.
 	 * 
 	 * @param server
 	 *            The server to create the Run on.
 	 * @param workflow
 	 *            The file containing the workflow to be associated with the
 	 *            Run.
 	 */
 	public Run(Server server, File workflow) throws IOException {
 		this(server, FileUtils.readFileToString(workflow));
 	}
 
 	/**
 	 * Create a new Run instance to represent a run that is already on the
 	 * specified server. This constructor is provided for internal use when
 	 * lists of runs are being built up in a Server instance.
 	 * 
 	 * @param server
 	 *            The server the Run is already on.
 	 * @param uuid
 	 *            The UUID of the Run.
 	 */
 	Run(Server server, UUID uuid) {
 		this.server = server;
 		this.uuid = uuid;
 		this.workflow = null;
 		this.baclavaOut = null;
 
 		xmlUtils = XmlUtils.getInstance();
 
 		links = getRunDescription();
 	}
 
 	/**
 	 * Set a workflow input value.
 	 * 
 	 * @param input
 	 *            The input port to set.
 	 * @param value
 	 *            The value to set the port to.
 	 */
 	public void setInput(String input, String value) {
 		RunStatus rs = getStatus();
 		if (rs == RunStatus.INITIALIZED) {
 			server.setRunInput(this, input, value);
 		} else {
 			throw new RunStateException(rs, RunStatus.INITIALIZED);
 		}
 	}
 
 	/**
 	 * Use an already uploaded file as input to a workflow input port. The file
 	 * to be used must already have been uploaded to the server with
 	 * {@link #uploadFile(File)} or {@link #uploadFile(File, String, String)}
 	 * before it can be used by this method and the filename returned by either
 	 * of those methods is what should be passed into this one.
 	 * {@link #uploadInputFile(String, File, String, String)} can be used to do
 	 * these two steps in one call.
 	 * 
 	 * @param input
 	 *            The input port to set.
 	 * @param filename
 	 *            The filename of the file (on the server) to use as input.
 	 * @see #uploadInputFile(String, File, String, String)
 	 */
 	public void setInputFile(String input, String filename) {
 		RunStatus rs = getStatus();
 		if (rs == RunStatus.INITIALIZED) {
 			server.setRunInputFile(this, input, filename);
 		} else {
 			throw new RunStateException(rs, RunStatus.INITIALIZED);
 		}
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
 		String uploadLocation = links.get("wdir");
 		uploadLocation += remoteDirectory != null ? "/" + remoteDirectory : "";
 		return server.uploadRunFile(this, file, uploadLocation, rename);
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
 	 * Upload a file to the Run's workspace on the server and then use it as
 	 * input to an input port.
 	 * 
 	 * @param input
 	 *            The input port to set.
 	 * @param file
 	 *            The file to upload and use as input.
 	 * @param remoteDirectory
 	 *            The directory within the workspace to upload the file to.
 	 * @param rename
 	 *            The name to use for the file when saving it in the workspace.
 	 * @throws IOException
 	 */
 	public void uploadInputFile(String input, File file,
 			String remoteDirectory, String rename) throws IOException {
 		RunStatus rs = getStatus();
 		if (rs == RunStatus.INITIALIZED) {
 			String filename = uploadFile(file, remoteDirectory, rename);
 			setInputFile(input, filename);
 		} else {
 			throw new RunStateException(rs, RunStatus.INITIALIZED);
 		}
 	}
 
 	/**
 	 * Upload a file to the Run's workspace on the server and then use it as
 	 * input to an input port.
 	 * 
 	 * @param input
 	 *            The input port to set.
 	 * @param file
 	 *            The file to upload and use as input.
 	 * @throws IOException
 	 */
 	public void uploadInputFile(String input, File file) throws IOException {
 		uploadInputFile(input, file, null, null);
 	}
 
 	/**
 	 * Upload a baclava file to specify all input port values.
 	 * 
 	 * @param file
 	 *            The file to upload.
 	 * @throws IOException
 	 */
 	public void uploadBaclavaFile(File file) throws IOException {
 		RunStatus rs = getStatus();
 		if (rs == RunStatus.INITIALIZED) {
 			String filename = uploadFile(file);
 			server.setRunAttribute(this, links.get("baclava"), filename);
 		} else {
 			throw new RunStateException(rs, RunStatus.INITIALIZED);
 		}
 	}
 
 	/**
 	 * Set the server to return outputs for this Run in baclava format. This
 	 * must be set before the Run is started.
 	 * 
 	 * @param name
 	 *            the name of the baclava file to use
 	 */
 	public void setBaclavaOutput(String name) {
 		RunStatus rs = getStatus();
 		if (rs == RunStatus.INITIALIZED) {
 			this.baclavaOut = name;
 			server.setRunAttribute(this, links.get("output"), baclavaOut);
 		} else {
 			throw new RunStateException(rs, RunStatus.INITIALIZED);
 		}
 	}
 
 	/**
 	 * Set the server to return outputs for this Run in baclava format. This
 	 * must be set before the Run is started.
 	 */
 	public void setBaclavaOutput() {
 		setBaclavaOutput("out.xml");
 	}
 
 	/**
 	 * Get the outputs of this Run as a baclava formatted document. The Run must
 	 * have been set to output in baclava format before it is started.
 	 * 
 	 * @return The baclava formatted document contents as a String.
 	 * @see #setBaclavaOutput()
 	 * @see #setBaclavaOutput(String)
 	 */
 	public String getBaclavaOutput() {
 		RunStatus rs = getStatus();
 		if (rs == RunStatus.FINISHED) {
 			String baclavaLink = links.get("wdir") + "/" + baclavaOut;
 			if (baclavaOut == null) {
 				throw new AttributeNotFoundException(baclavaLink);
 			}
 
 			return server.getRunAttribute(this, baclavaLink,
 					"application/octet-stream");
 		} else {
 			throw new RunStateException(rs, RunStatus.FINISHED);
 		}
 	}
 
 	/**
 	 * Get the UUID of this run.
 	 * 
 	 * @return the UUID of this run.
 	 */
 	public UUID getUUID() {
 		return uuid;
 	}
 
 	/**
 	 * Get the status of this Run.
 	 * 
 	 * @return the status of this Run.
 	 */
 	public RunStatus getStatus() {
 		return RunStatus
 				.state(server.getRunAttribute(this, links.get("status")));
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
 	 */
 	public void start() {
 		RunStatus rs = getStatus();
 		if (rs == RunStatus.INITIALIZED) {
 			server.setRunAttribute(this, links.get("status"),
 					RunStatus.RUNNING.status());
 		} else {
 			throw new RunStateException(rs, RunStatus.INITIALIZED);
 		}
 	}
 
 	/**
 	 * Get the workflow of this Run as a String.
 	 * 
 	 * @return the workflow of this Run as a String.
 	 */
 	public String getWorkflow() {
 		if (workflow == null) {
 			workflow = server.getRunAttribute(this, links.get("workflow"));
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
 		server.deleteRun(this);
 	}
 
 	String getInputsPath() {
 		return links.get("inputs");
 	}
 
 	/**
 	 * Get a list of the output ports that have been written to so far. Note
 	 * that this list will be in flux until the workflow has finished running so
 	 * if you wish to simply gather results at the end of a run you should test
 	 * its status first.
 	 * 
 	 * @return the list of the output ports that have been written to so far.
 	 * @see #isFinished()
 	 */
 	public List<String> getOutputPorts() {
 		List<String> lists = new ArrayList<String>();
 		List<String> items = new ArrayList<String>();
 
 		ls_ports("out", lists, items);
 
 		// return concatenated lists
 		items.addAll(lists);
 		return items;
 	}
 
 	/**
 	 * Get the return code of the underlying Taverna Server process. A zero
 	 * value indicates success.
 	 * 
 	 * @return the return code of the underlying Taverna Server process.
 	 */
 	public int getExitCode() {
 		return new Integer(server.getRunAttribute(this, links.get("exitcode")));
 	}
 
 	/**
 	 * Get the console output of the underlying Taverna Server process.
 	 * 
 	 * @return the console output of the underlying Taverna Server process.
 	 */
 	public String getConsoleOutput() {
 		return server.getRunAttribute(this, links.get("stdout"));
 	}
 
 	/**
 	 * Get the console errors of the underlying Taverna Server process.
 	 * 
 	 * @return the console errors of the underlying Taverna Server process.
 	 */
 	public String getConsoleError() {
 		return server.getRunAttribute(this, links.get("stderr"));
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
 				links.get(time)));
 
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
 			server.makeRunDir(this, links.get("wdir") + "/" + path, leaf);
 		} else {
 			server.makeRunDir(this, links.get("wdir"), dir);
 		}
 	}
 
 	private Map<String, String> getRunDescription() {
 		HashMap<String, String> links = new HashMap<String, String>();
 
 		// parse out the simple stuff
 		String description = server.getRunDescription(this);
 		Document doc = ParseUtil.parse(description);
 
 		links.put("expiry",
 				xmlUtils.evalXPath(doc, "//nsr:expiry", "xlink:href"));
 		links.put("workflow",
 				xmlUtils.evalXPath(doc, "//nsr:creationWorkflow", "xlink:href"));
 		links.put("status",
 				xmlUtils.evalXPath(doc, "//nsr:status", "xlink:href"));
 		links.put("createtime",
 				xmlUtils.evalXPath(doc, "//nsr:createTime", "xlink:href"));
 		links.put("starttime",
 				xmlUtils.evalXPath(doc, "//nsr:startTime", "xlink:href"));
 		links.put("finishtime",
 				xmlUtils.evalXPath(doc, "//nsr:finishTime", "xlink:href"));
 		links.put("wdir",
 				xmlUtils.evalXPath(doc, "//nsr:workingDirectory", "xlink:href"));
 		links.put("inputs",
 				xmlUtils.evalXPath(doc, "//nsr:inputs", "xlink:href"));
 		links.put("output",
 				xmlUtils.evalXPath(doc, "//nsr:output", "xlink:href"));
 		links.put("securectx",
 				xmlUtils.evalXPath(doc, "//nsr:securityContext", "xlink:href"));
 		links.put("listeners",
 				xmlUtils.evalXPath(doc, "//nsr:listeners", "xlink:href"));
 
 		// get the inputs
 		String inputs = server.getRunAttribute(this, links.get("inputs"));
 		doc = ParseUtil.parse(inputs);
 		links.put("baclava",
 				xmlUtils.evalXPath(doc, "//nsr:baclava", "xlink:href"));
 
 		// set io properties
 		links.put("io", links.get("listeners") + "/io");
 		links.put("stdout", links.get("io") + "/properties/stdout");
 		links.put("stderr", links.get("io") + "/properties/stderr");
 		links.put("exitcode", links.get("io") + "/properties/exitcode");
 
 		return links;
 	}
 
 	private void ls_ports(String dir, List<String> lists, List<String> values,
 			boolean top) {
 
 		if (dir.endsWith("/")) {
 			dir = dir.substring(0, dir.length() - 1);
 		}
 
 		String dirList = server.getRunAttribute(this, links.get("wdir") + "/"
 				+ dir, "application/xml");
 		Document doc = ParseUtil.parse(dirList);
 
 		if (lists != null) {
 			List<Element> dirs = xmlUtils.evalXPath(doc, "//nss:dir");
 
 			// need to make space in the list for random access
 			if (dirs.size() > lists.size()) {
 				for (int i = 0; i < dirs.size(); i++) {
 					lists.add("");
 				}
 			}
 
 			for (Element e : dirs) {
 				String[] paths = e.getTextContent().split("/");
 				String name = paths[paths.length - 1];
 				if (top) {
 					lists.set(0, name);
 				} else {
					int index = (Integer.parseInt(xmlUtils.getServerAttribute(
							e, "name")) - 1);
 					lists.set(index, name);
 				}
 			}
 		}
 
 		if (values != null) {
 			List<Element> files = xmlUtils.evalXPath(doc, "//nss:file");
 
 			// need to make space in the list for random access
 			if (files.size() > values.size()) {
 				for (int i = 0; i < files.size(); i++) {
 					values.add("");
 				}
 			}
 
 			for (Element e : files) {
 				String[] paths = e.getTextContent().split("/");
 				String name = paths[paths.length - 1];
 				if (top) {
 					values.set(0, name);
 				} else {
					int index = (Integer.parseInt(xmlUtils.getServerAttribute(
							e, "name")) - 1);
 					values.set(index, name);
 				}
 			}
 		}
 	}
 
 	private void ls_ports(String dir, List<String> lists, List<String> values) {
 		ls_ports(dir, lists, values, true);
 	}
 
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	private RunOutput<?> getOutput(String port, boolean refs, boolean top) {
 
 		// if we are at the top level we need to return a singleton value here.
 		if (top) {
 			ArrayList<String> values = new ArrayList<String>();
 			ls_ports("out", null, values);
 
 			if (values.contains(port)) {
 				if (refs) {
 					try {
 						return new RunOutputRefs(new URI(links.get("wdir")
 								+ "/out/" + port));
 					} catch (URISyntaxException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				} else {
 					return new RunOutputData(server.getRunData(this,
 							links.get("wdir") + "/out/" + port,
 							"application/octet-stream"));
 				}
 			}
 		}
 
 		// this port isn't a singleton so drill into it and build up
 		// lists (of lists, etc) of items
 		ArrayList<String> lists = new ArrayList<String>();
 		ArrayList<String> items = new ArrayList<String>();
 
 		RunOutput result = refs ? new RunOutputRefs() : new RunOutputData();
 
 		ls_ports("out/" + port, lists, items, false);
 
 		// for each list, recurse into it and add the items to the result
 		for (String list : lists) {
 			result.addNode(getOutput(port + "/" + list, refs, false));
 		}
 
 		// each item, add it to the output list
 		for (String item : items) {
 			if (refs) {
 				try {
 					result.addNode(new RunOutputRefs(new URI(links.get("wdir")
 							+ "/out/" + port + "/" + item)));
 				} catch (URISyntaxException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			} else {
 				result.addNode(new RunOutputData(server.getRunData(this,
 						links.get("wdir") + "/out/" + port + "/" + item,
 						"application/octet-stream")));
 			}
 		}
 
 		return result;
 	}
 
 	/**
 	 * Get the contents of the specified output port. Note that the contents of
 	 * an output port will be in flux until the workflow has finished running so
 	 * if you wish to simply gather results at the end of a run you should test
 	 * its status first.
 	 * 
 	 * @param port
 	 *            the output port to get.
 	 * @param refs
 	 *            true to return references to the data, false to return the
 	 *            actual data.
 	 * @return a data structure containing the data in the output port.
 	 */
 	public RunOutput<?> getOutput(String port, boolean refs) {
 		return getOutput(port, refs, true);
 	}
 
 	/**
 	 * Get the contents of the specified output port. Note that the contents of
 	 * an output port will be in flux until the workflow has finished running so
 	 * if you wish to simply gather results at the end of a run you should test
 	 * its status first.
 	 * 
 	 * @param port
 	 *            the output port to get.
 	 * @return a data structure containing the data in the output port.
 	 */
 	public RunOutput<?> getOutput(String port) {
 		return getOutput(port, false, true);
 	}
 
 	/**
 	 * Get references to the contents of the specified output port. Note that
 	 * the contents of an output port will be in flux until the workflow has
 	 * finished running so if you wish to simply gather results at the end of a
 	 * run you should test its status first.
 	 * 
 	 * @param port
 	 *            the output port to get.
 	 * @return a data structure containing the data in the output port.
 	 */
 	public RunOutput<?> getOutputRefs(String port) {
 		return getOutput(port, true, true);
 	}
 }
