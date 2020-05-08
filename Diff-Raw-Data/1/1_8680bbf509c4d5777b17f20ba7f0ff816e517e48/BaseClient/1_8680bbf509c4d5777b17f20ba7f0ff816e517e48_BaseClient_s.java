 /**
  * Copyright (C) 2012 Vincenzo Pirrone
  * This program is free software; you can redistribute it and/or modify it under
  * the terms of the GNU General Public License as published by the Free Software
  * Foundation; either version 2 of the License, or (at your option) any later
  * version.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along with
  * this program; if not, write to the Free Software Foundation, Inc., 51
  * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
  */
 package com.kdcloud.lib.client;
 
 
 
 import java.io.IOException;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Queue;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpression;
 import javax.xml.xpath.XPathExpressionException;
 import javax.xml.xpath.XPathFactory;
 
 import org.restlet.data.ChallengeScheme;
 import org.restlet.data.MediaType;
 import org.restlet.data.Status;
 import org.restlet.ext.xml.DomRepresentation;
 import org.restlet.representation.Representation;
 import org.restlet.resource.ClientResource;
 import org.restlet.resource.ResourceException;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import weka.core.Instances;
 
 import com.kdcloud.lib.domain.DataSpecification;
 import com.kdcloud.lib.domain.Modality;
 import com.kdcloud.lib.domain.ServerAction;
 import com.kdcloud.lib.domain.ServerParameter;
 import com.kdcloud.lib.domain.ServerParameter.ReferenceType;
 import com.kdcloud.lib.rest.api.ModalitiesResource;
 import com.kdcloud.lib.rest.ext.InstancesRepresentation;
 
 public abstract class BaseClient implements Runnable {
 
 	// the server url
 	String baseUri;
 
 	// the executing modality
 	Modality modality;
 
 	// used for restlet requests
 	ClientResource resource;
 
 	// stores each (xml) output of any server request
 	Document executionLog;
 
 	// utility factories
 	DocumentBuilder documentBuilder;
 	XPath xpath;
 
 	// queue of actions to execute
 	Queue<ServerAction> queue;
 
 	// used sto stop execution
 	private boolean canRun;
 
 	// whether or not repeating repeatable actions
 	private boolean repeatAllowed;
 
 	// the current executing action
 	private ServerAction currentAction;
 	
 	//the modality output
 	private Instances output;
 
 	public abstract void log(String message, Throwable thrown);
 
 	public abstract void log(String message);
 
 	/**
 	 * this method is called when a request (PUT) requires data to send
 	 * 
 	 * @return the instances to send
 	 */
 	public abstract Instances getData();
 
 	/**
 	 * returns a selected string between the ones in choices
 	 * 
 	 * @param parameterName
 	 *            a string specifing the semantic meaning of the choices
 	 * @param choices
 	 *            the possibles choice alternatives
 	 * @return the choosen alternative
 	 */
 	public abstract String handleChoice(String parameterName, String[] choices);
 
 	/**
 	 * this method is called when an xml report is available
 	 * 
 	 * @param view
 	 *            the xml report to handle
 	 */
 	public abstract void report(Document view);
 
 	/**
 	 * checks if a single action can be repeated
 	 * 
 	 * @return
 	 */
 	public synchronized boolean isRepeatAllowed() {
 		return repeatAllowed;
 	}
 
 	public synchronized void setRepeatAllowed(boolean repeatAllowed) {
 		this.repeatAllowed = repeatAllowed;
 	}
 
 	public BaseClient(String url) throws ParserConfigurationException {
 		this(url, null);
 	}
 
 	public BaseClient(String url, Modality modality)
 			throws ParserConfigurationException {
 		super();
 		this.baseUri = url;
 		this.modality = modality;
 
 		// initialize xml stuff
 		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 		this.documentBuilder = dbf.newDocumentBuilder();
 		
 		XPathFactory xPathfactory = XPathFactory.newInstance();
 		this.xpath = xPathfactory.newXPath();
 
 		// creates the client resource
 		this.resource = new ClientResource(url);
 
 		// initialize control variables
 		this.canRun = true;
 		this.repeatAllowed = true;
 	}
 
 	public synchronized void stopModalityExecution() {
 		canRun = false;
 	}
 
 	public synchronized void startModalityExecution() {
 		canRun = true;
 	}
 
 	public synchronized boolean canRun() {
 		return canRun;
 	}
 
 	public synchronized Modality getModality() {
 		return modality;
 	}
 
 	/**
 	 * changes client modality, stops the execution of the previous if any
 	 * 
 	 * @param modality
 	 */
 	public synchronized void setModality(Modality modality) {
 		this.canRun = false;
 		this.modality = modality;
 	}
 
 	public void setAccessToken(String token) {
 		log("setting access token");
 		setAuthentication("oauth", token);
 	}
 	
 	public void setAuthentication(String userId, String password) {
 		resource.setChallengeResponse(ChallengeScheme.HTTP_BASIC, userId, password);
 	}
 
 	public static List<Modality> getModalities(String url) {
 		return getModalities(url, null);
 	}
 
 	public static List<Modality> getModalities(String url, String accessToken) {
 		ClientResource cr = new ClientResource(url + ModalitiesResource.URI);
 		if (accessToken != null)
 			cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, "login",
 					accessToken);
 		return cr.wrap(ModalitiesResource.class).listModalities().asList();
 	}
 	
 	public List<Modality> getModalities() {
 		setResourceReference(ModalitiesResource.URI);
 		return resource.wrap(ModalitiesResource.class).listModalities().asList();
 	}
 
 	@Override
 	public void run() {
 		try {
 			executeModality();
 		} catch (Exception e) {
 			log(e.getMessage(), e);
 		}
 	}
 
 	/**
 	 * executed the predefined modality
 	 * 
 	 * @throws IOException
 	 * @throws InterruptedException
 	 */
 	public void executeModality() throws IOException, InterruptedException {
 		startModalityExecution();
 		
 		this.executionLog = this.documentBuilder.newDocument();
 		Element rootElement = this.executionLog.createElementNS("dummy",
 				"execution");
 		this.executionLog.appendChild(rootElement);
 		
 		log("executing " + modality.getName());
 		queue = new LinkedList<ServerAction>();
 		queue.add(new ServerAction(modality.getInitAction()));
 		queue.add(new ServerAction(modality.getAction()));
 		while (canRun() && !queue.isEmpty()) {
 			currentAction = queue.poll();
 			currentAction.waitTriggers();
 			if (repeatAllowed && currentAction.isRepeat())
 				queue.add(new ServerAction(currentAction));
 			while (currentAction.hasParameters())
 				setActionParameter();
 			try {
 				executeAction();
 			} catch (ResourceException e) {
 				handleResourceException(resource.getStatus(), e);
 			}
 		}
 	}
 
 	/**
 	 * this method is called when a server request does not terminate correctly
 	 * 
 	 * @param status
 	 *            the request status
 	 * @param e
 	 *            the exception thrown by the restlet ClientResource
 	 */
 	public abstract void handleResourceException(Status status,
 			ResourceException e);
 
 	protected void setActionParameter() throws IOException {
 		ServerParameter parameter = currentAction.getParams().get(0);
 		try {
 			log("executing xpath expression: " + parameter.getReferenceExpression());
 			XPathExpression expr = xpath.compile(parameter.getReferenceExpression());
 			NodeList result = (NodeList) expr.evaluate(executionLog,
 					XPathConstants.NODESET);
 			log("expression result length: " + result.getLength());
 			String[] choices = new String[result.getLength()];
 			for (int i = 0; i < choices.length; i++) {
 				choices[i] = result.item(i).getTextContent();
 			}
 			handleParameter(parameter, choices);
 		} catch (XPathExpressionException e) {
 			throw new IOException(e.getMessage());
 		}
 	}
 
 	private void handleParameter(ServerParameter parameter, String[] values) throws IOException {
 		if (parameter.getReferenceType() == ReferenceType.CHOICE) {
 			if (values.length == 0)
 				throw new IOException("missing parameter");
 			String value = handleChoice(parameter.getName(), values);
 			log("setting parameter " + parameter.getName() + ":" + value);
 			currentAction.setParameter(parameter, value);
 		} else {
 			Queue<ServerAction> newQueue = new LinkedList<ServerAction>();
 			for (int i = 0; i < values.length; i++) {
 				ServerAction copy = new ServerAction(currentAction);
 				log("setting parameter " + parameter.getName() + ":" + values[i]);
 				copy.setParameter(parameter, values[i]);
 				newQueue.add(copy);
 			}
 			newQueue.addAll(queue);
 			queue = newQueue;
 			currentAction = queue.poll();
 		}
 	}
 
 	
 
 	protected void setResourceReference(String uri) {
 		String reference = baseUri + uri;
 		log("fetching " + reference);
 		resource.setReference(reference);
 	}
 
 	/**
 	 * retries the execution of the last server action
 	 * 
 	 * @throws IOException if the requests fails
 	 */
 	public void retryRequest() throws IOException {
 		try {
 			executeAction();
 		} catch (ResourceException e) {
 			throw new IOException(e.getMessage());
 		}
 	}
 
 	/**
 	 * executes the current action
 	 * 
 	 * @throws IOException if the client fails to read the response
 	 * @throws ResourceException if there has been an error during the request
 	 */
 	public void executeAction() throws IOException, ResourceException {
 		setResourceReference(currentAction.getUri());
 		beforeRequest();
 		Representation entity = null;
 		switch (currentAction.getMethod()) {
 		case GET:
 			log("executing GET");
 			entity = resource.get(MediaType.APPLICATION_ALL_XML);
 			break;
 		case PUT:
 			log("executing PUT");
 			Instances data = getData();
 			if (!modality.getInputSpecification().matchingSpecification(data))
 				throw new IOException("input does not match specification");
 			Representation putRep = currentAction.getPutRepresentation(data);
 			entity = resource.put(putRep);
 			break;
 		case DELETE:
 			log("executing DELETE");
 			entity = resource.delete();
 			break;
 		case POST:
 			log("executing POST");
 			Representation postRep = currentAction.getPostRepresentation();
 			entity = resource.post(postRep);
 		default:
 			break;
 		}
 		if (!entity.isEmpty()) {
 			handleEntity(entity);
 		} else {
 			log("received entity is empty, doing nothing");
 		}
 	}
 
 	/**
 	 * this method is called before each server request
 	 */
 	public void beforeRequest() {
 	}
 
 	/**
 	 * handles each response
 	 * 
 	 * @param entity the response entity
 	 * @throws IOException if the client fails to read the entity
 	 */
 	protected void handleEntity(Representation entity) throws IOException {
 		log("handling entity");
 		MediaType t = entity.getMediaType();
 		if (t.equals(MediaType.APPLICATION_XML) || t.equals(MediaType.TEXT_XML)) {
 			Document lastOutput = new DomRepresentation(entity).getDocument();
 //			DOMImplementation domImpl = lastOutput.getImplementation();
 //			DOMImplementationLS domImplLS = (DOMImplementationLS)domImpl.getFeature("LS", "3.0");
 //			LSSerializer serializer = domImplLS.createLSSerializer();
 //			System.out.println(serializer.writeToString(lastOutput));
 			log("storing last output");
 			Node child = executionLog
 					.adoptNode(lastOutput.getDocumentElement());
 			executionLog.getDocumentElement().appendChild(child);
 		} else {
 			output = new InstancesRepresentation(entity).getInstances();
 			DataSpecification outSpec = modality.getOutputSpecification();
 			if (outSpec != null && !outSpec.matchingSpecification(output))
 				throw new IOException("output does not match the specifications");
 			String view = modality.getOutputSpecification().getView();
 			if (view != null)
 				report(view);
 		}
 	}
 
 	private void report(String viewResource) throws IOException {
 		log("generating report");
 		setResourceReference(viewResource);
 //		Document view = resource.wrap(ViewResource.class).getView();
 		Representation rep = resource.get();
 		DomRepresentation domRep = new DomRepresentation(rep);
 		Document view = domRep.getDocument();
 		XmlReport.mergeWithData(view, output);
 		report(view);
 	}
 	
 }
