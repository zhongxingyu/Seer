 package com.kdcloud.server.entity;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.restlet.data.Form;
 import org.restlet.data.Parameter;
 import org.restlet.representation.ObjectRepresentation;
 import org.restlet.representation.Representation;
 
 import com.kdcloud.weka.core.Attribute;
 import com.kdcloud.weka.core.Instances;
 
 public class ServerAction implements Serializable {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	
 	String uri;
 	
 	String outputLabel;
 	
 	ServerMethod method;
 	
 	ArrayList<Attribute> dataSpec;
 	
 	Set<ServerParameter> uriParams;
 	Set<ServerParameter> postParams;
 	ArrayList<Parameter> postForm;
 	
 	boolean repeat;
 	
 	long sleepTime;
 	
 	public ServerAction() {
 		// TODO Auto-generated constructor stub
 	}
 
 
 	public ServerAction(String uri, String outputLabel, ServerMethod method,
 			boolean repeat, long sleepTime) {
 		super();
 		this.uri = uri;
 		this.outputLabel = outputLabel;
 		this.method = method;
 		this.repeat = repeat;
 		this.sleepTime = sleepTime;
 		this.uriParams = ServerParameter.getParamsFromUri(uri);
 		this.postParams = new HashSet<ServerParameter>();
 		this.postForm = new ArrayList<Parameter>();
 	}
 
 
 	ServerAction(ServerAction serverAction, String newUri, ArrayList<Parameter> newPostForm) {
 		this.outputLabel = serverAction.outputLabel;
 		this.method = serverAction.method;
 		this.repeat = serverAction.repeat;
 		this.sleepTime = serverAction.sleepTime;
 		this.dataSpec = serverAction.dataSpec;
 		this.uri = newUri;
 		this.uriParams = ServerParameter.getParamsFromUri(uri);
 		this.postParams = serverAction.postParams;
 		this.postForm = newPostForm;
 	}
 
 
 	public ServerMethod getMethod() {
 		return method;
 	}
 
 	public void setMethod(ServerMethod method) {
 		this.method = method;
 	}
 
 
 	public String getUri() {
 		return uri;
 	}
 
 	public String getOutputLabel() {
 		return outputLabel;
 	}
 
 	public void setOutputLabel(String outputLabel) {
 		this.outputLabel = outputLabel;
 	}
 
 
 	public boolean isRepeat() {
 		return repeat;
 	}
 
 	public void setRepeat(boolean repeat) {
 		this.repeat = repeat;
 	}
 
 	public long getSleepTime() {
 		return sleepTime;
 	}
 
 	public void setSleepTime(long sleepTime) {
 		this.sleepTime = sleepTime;
 	}
 	
 	public ArrayList<Attribute> getDataSpec() {
 		return dataSpec;
 	}
 
 	public void setDataSpec(ArrayList<Attribute> dataSpec) {
 		this.dataSpec = dataSpec;
 	}
 
 
 	public List<ServerParameter> getParams() {
 		ArrayList<ServerParameter> params = 
 				new ArrayList<ServerParameter>(uriParams.size() + postParams.size());
		params.addAll(postParams);
 		params.addAll(postParams);
 		return params;
 	}
 	
 	public boolean hasParameters() {
 		return uriParams.isEmpty() && postParams.isEmpty();
 	}
 	
 	public boolean addParameter(ServerParameter param) {
 		return postParams.add(param);
 	}
 
 	public ServerAction setParameter(ServerParameter param, String value) {
 		String newUri = uri;
 		ArrayList<Parameter> newPostForm = postForm;
 		if (uriParams.contains(param)) {
 			newUri = uri.replaceAll(param.getPattern(), value);
 		} else if (postParams.contains(param)) {
 			newPostForm = new ArrayList<Parameter>(postForm);
 			Parameter postParam = new Parameter(param.getName(), value);
 			newPostForm.add(postParam);
 		}
 		return new ServerAction(this, newUri, newPostForm);
 	}
 	
 	public Representation getPostRepresentation() {
 		return new Form(postForm).getWebRepresentation();
 	}
 	
 	public Representation getPutRepresentation(Instances instances) {
 		return new ObjectRepresentation<Serializable>(instances);
 	}
 	
 	@Override
 	public String toString() {
 		String postString = " ( ";
 		for (ServerParameter p : postParams) {
 			postString = postString + p.getName() + " ";
 		}
 		postString = postString + ")";
 		return method.toString() + ": " + uri + postString;
 	}
 	
 }
 
