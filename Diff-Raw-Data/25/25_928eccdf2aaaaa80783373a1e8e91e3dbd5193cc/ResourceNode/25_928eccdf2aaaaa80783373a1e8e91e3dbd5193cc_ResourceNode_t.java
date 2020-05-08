 package tk.ludva.restfulchecker;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.springframework.web.util.HtmlUtils;
 
 import tk.ludva.restfulchecker.model.ViolationMessagesHolder;
 
 /**
  * Class for holding resources in Tree structure.
  * 
  * @author Lu2
  * 
  */
 public class ResourceNode
 {
 	/**
 	 * Informations about resource's HTTP GET..
 	 */
 	private RemoteResource currentResource;
 	
 	/**
 	 * Informations about resource's HTTP OPTIONS.
 	 */
 	private RemoteResource currentResourceOptions;
 	
 	/**
 	 * List of resource's children.
 	 */
 	private List<ResourceNode> descendants;
 	
 	/**
 	 * Map for storing REST error messages for this resource.
 	 */
 	private Map<String, ViolationMessagesHolder> violationMessages;
 	
 	/**
 	 * Map for storing REST warning messages for this resource.
 	 */
 	private Map<String, ViolationMessagesHolder> nonViolationMessages;
 
 	public ResourceNode()
 	{
 	}
 
 	/**
 	 * Creates ResourceNode and sets specified resource to it.
 	 * @param currentResource the resource to be used as currentResource.
 	 */
 	public ResourceNode(RemoteResource currentResource)
 	{
 		this.currentResource = currentResource;
 	}
 
 	/**
 	 * Gets this resource.
 	 * @return RemoteResource.
 	 */
 	public RemoteResource getCurrentResource()
 	{
 		return currentResource;
 	}
 
 	/**
 	 * Sets this resource.
 	 * @param currentResource to be set.
 	 */
 	public void setCurrentResource(RemoteResource currentResource)
 	{
 		this.currentResource = currentResource;
 	}
 
 	/**
 	 * Gets this resource's OPTIONS.
 	 * @return RemoteResource for OPTIONS.
 	 */
 	public RemoteResource getCurrentResourceOptions()
 	{
 		return currentResourceOptions;
 	}
 
 	/**
 	 * Sets this resource's OPTIONS.
 	 * @param currentResourceOptions to be set.
 	 */
 	public void setCurrentResourceOptions(RemoteResource currentResourceOptions)
 	{
 		this.currentResourceOptions = currentResourceOptions;
 	}
 
 	/**
 	 * Gets list of resource's children.
 	 * @return List of resource's children.
 	 */
 	public List<ResourceNode> getDescendants()
 	{
 		if (descendants == null)
 			descendants = new ArrayList<ResourceNode>();
 		return descendants;
 	}
 
 	/**
 	 * Sets list of resource's children.
 	 * @param descendants List of children to be set.
 	 */
 	public void setDescendants(List<ResourceNode> descendants)
 	{
 		this.descendants = descendants;
 	}
 
 	/**
 	 * Gets REST error messages for this resource.
 	 * @return Map with error messages.
 	 */
 	public Map<String, ViolationMessagesHolder> getViolationMessages()
 	{
 		if (violationMessages == null)
 			violationMessages = new HashMap<String, ViolationMessagesHolder>();
 		return violationMessages;
 	}
 
 	/**
 	 * Sets REST error messages for this resource.
 	 * @param violationMessages Map with error messages.
 	 */
 	public void setViolationMessages(Map<String, ViolationMessagesHolder> violationMessages)
 	{
 		this.violationMessages = violationMessages;
 	}
 
 	/**
 	 * Adds a error message into map of error messages.
 	 * @param key of error message.
 	 * @param message value of error message.
 	 */
 	public void addViolationMessage(String key, String message)
 	{
 		if (getViolationMessages().containsKey(key))
 		{
 			getViolationMessages().get(key).addMessage(message);
 		}
 		else
 		{
 			getViolationMessages().put(key, new ViolationMessagesHolder(key, message));
 		}
 	}
 
 	/**
 	 * Gets REST warning messages for this resource.
 	 * @return Map with warning messages.
 	 */
 	public Map<String, ViolationMessagesHolder> getNonViolationMessages()
 	{
 		if (nonViolationMessages == null)
 			nonViolationMessages = new HashMap<String, ViolationMessagesHolder>();
 		return nonViolationMessages;
 	}
 
 	/**
 	 * Sets REST warning messages for this resource.
 	 * @param nonViolationMessages Map with warning messages.
 	 */
 	public void setNonViolationMessages(Map<String, ViolationMessagesHolder> nonViolationMessages)
 	{
 		this.nonViolationMessages = nonViolationMessages;
 	}
 
 	/**
 	 * Adds a warning message into map of warning messages.
 	 * @param key of warning message.
 	 * @param message value of warning message.
 	 */
 	public void addNonViolationMessage(String key, String message)
 	{
 		if (getNonViolationMessages().containsKey(key))
 		{
 			getNonViolationMessages().get(key).addMessage(message);
 		}
 		else
 		{
 			getNonViolationMessages().put(key, new ViolationMessagesHolder(key, message));
 		}
 	}
 
 	/**
	 * Returns HTML representation of response for this resource.
 	 * @return HTML representation in String.
 	 */
 	public String toStringResponse()
 	{
 		StringBuilder htmlOutput = new StringBuilder();
 		// htmlOutput.append("<div>");
 		// htmlOutput.append("<span class=\"violationMessage\">"+violationMessages.values().toString()+"</span>");
 		// htmlOutput.append("<span class=\"nonViolationMessage\">"+nonViolationMessages.values().toString()+"</span>");
 		// htmlOutput.append("</div>");
 		htmlOutput.append("<div class=\"requestResponse\" id=\"" + currentResource.getUrl() + "\">");
 		String resp = null;
 		for (Header responseHeader : currentResource.getResponseHeaders())
 		{
 			if (responseHeader.getHeaderKey() == null)
 			{
 				resp = responseHeader.getHeaderValue();
 				break;
 			}
 		}
 		if (resp == null)
 		{
 			resp = currentResource.getResponseCode() + " " + currentResource.getResponseMessage();
 		}
 		htmlOutput.append("<h3>HTTP Response for " + currentResource.getUrl() + "</h3>");
 		htmlOutput.append("<p>" + resp + "</p>");
 		htmlOutput.append("<table class=\"responseHeaders\">");
 		for (Header responseHeader : currentResource.getResponseHeaders())
 		{
 			if (responseHeader.getHeaderKey() != null)
 			{
 				htmlOutput.append("<tr>");
 				htmlOutput.append("<td>" + HtmlUtils.htmlEscape(responseHeader.getHeaderKey()) + "</td>");
 				htmlOutput.append("<td>" + HtmlUtils.htmlEscape(responseHeader.getHeaderValue()) + "</td>");
 				htmlOutput.append("</tr>");
 			}
 		}
 		htmlOutput.append("</table>");
 		htmlOutput.append("<p><textarea>" + HtmlUtils.htmlEscape(currentResource.getResponseBody()) + "</textarea></p>");
 		htmlOutput.append("</div>");
 		if (currentResourceOptions == null)
 		{
 			return htmlOutput.toString();
 		}
 		htmlOutput.append("<div class=\"requestResponse\" id=\"o" + currentResourceOptions.getUrl() + "\">");
 		htmlOutput.append("<p>" + currentResourceOptions.getResponseCode() + " " + currentResourceOptions.getResponseMessage()
 				+ "</p>");
 		htmlOutput.append("<table class=\"responseHeaders\">");
 		for (Header responseHeader : currentResourceOptions.getResponseHeaders())
 		{
 			htmlOutput.append("<tr>");
 			htmlOutput.append("<td>" + HtmlUtils.htmlEscape(responseHeader.getHeaderKey()) + "</td>");
 			htmlOutput.append("<td>" + HtmlUtils.htmlEscape(responseHeader.getHeaderValue()) + "</td>");
 			htmlOutput.append("</tr>");
 		}
 		htmlOutput.append("</table>");
 		htmlOutput.append("<p><textarea>" + HtmlUtils.htmlEscape(currentResourceOptions.getResponseBody()) + "</textarea></p>");
 		htmlOutput.append("</div>\n");
 		return htmlOutput.toString();
 	}
 
 	/**
 	 * Returns HTML resource URI with clickable elements representation.
 	 * @return representation as String.
 	 */
 	public String toStringDetail()
 	{
 		return "<a href=\"#\" onclick=\"toggleVisibility(document.getElementById(\'" + currentResource.getUrl()
 				+ "\')); return false\" >#</a>" + "<a href=\"#\" onclick=\"toggleVisibility(document.getElementById(\'o"
 				+ currentResource.getUrl() + "\')); return false\" >o</a>";
 	}
 
 	/**
	 * TODO: delete this?
 	 */
 	@Override
 	public String toString()
 	{
 		if (descendants == null)
 			return "<li>" + toStringDetail() + " " + currentResource.getUrl() + " " + toStringResponse() + " " + "<ul>"
 					+ descendants + "</ul></li>";
 		if (descendants.size() >= 10)
 		{
 			return "<li>" + toStringDetail() + " " + currentResource.getUrl() + " " + toStringResponse() + " " + "<ul>"
 					+ descendants.subList(0, 10) + "<li>and " + (descendants.size() - 10)
 					+ " more, TODO dead ones.</li></ul></li>";
 		}
 		return "<li>" + toStringDetail() + " " + currentResource.getUrl() + " " + toStringResponse() + " " + "<ul>"
 				+ descendants + "</ul></li>";
 	}
 
 	/**
 	 * Sends HTTP OPTIONS request for this resource.
 	 */
 	public void sendOptions()
 	{
 		try
 		{
 			currentResourceOptions = (RemoteResource) currentResource.clone();
 		}
 		catch (CloneNotSupportedException e)
 		{
 			currentResourceOptions = null;
 		}
 		currentResourceOptions.setMethod("OPTIONS");
 		currentResourceOptions.sendRequest();
 	}
 
 	/**
 	 * Sends HTTP request for this resource.
 	 */
 	public void sendRequest()
 	{
 		currentResource.sendRequest();
 	}
 
 	/**
 	 * Returns XML representation for this resource.
 	 * @return XML representation in String.
 	 */
 	public String toStringResponseXML()
 	{
 		StringBuilder htmlOutput = new StringBuilder();
 		htmlOutput.append("<HTTP_response>");
 		String resp = null;
 		for (Header responseHeader : currentResource.getResponseHeaders())
 		{
 			if (responseHeader.getHeaderKey() == null)
 			{
 				resp = responseHeader.getHeaderValue();
 				break;
 			}
 		}
 		if (resp == null)
 		{
 			resp = currentResource.getResponseCode() + " " + currentResource.getResponseMessage();
 		}
 		htmlOutput.append("<message>" + resp + "</message>");
 		htmlOutput.append("<headers>");
 		for (Header responseHeader : currentResource.getResponseHeaders())
 		{
 			if (responseHeader.getHeaderKey() != null)
 			{
 				htmlOutput.append("<header>");
 				htmlOutput.append("<key>" + responseHeader.getHeaderKey() + "</key>");
 				htmlOutput.append("<value>" + responseHeader.getHeaderValue() + "</value>");
 				htmlOutput.append("</header>");
 			}
 		}
 		htmlOutput.append("</headers>");
 		htmlOutput.append("</HTTP_response>");
 		if (currentResourceOptions == null)
 		{
 			return htmlOutput.toString();
 		}
 
 		htmlOutput.append("<HTTP_OPTIONS_response>");
 		resp = null;
 		for (Header responseHeader : currentResourceOptions.getResponseHeaders())
 		{
 			if (responseHeader.getHeaderKey() == null)
 			{
 				resp = responseHeader.getHeaderValue();
 				break;
 			}
 		}
 		if (resp == null)
 		{
 			resp = currentResourceOptions.getResponseCode() + " " + currentResourceOptions.getResponseMessage();
 		}
 		htmlOutput.append("<message>" + resp + "</message>");
 		htmlOutput.append("<headers>");
 		for (Header responseHeader : currentResourceOptions.getResponseHeaders())
 		{
 			if (responseHeader.getHeaderKey() != null)
 			{
 				htmlOutput.append("<header>");
 				htmlOutput.append("<key>" + responseHeader.getHeaderKey() + "</key>");
 				htmlOutput.append("<value>" + responseHeader.getHeaderValue() + "</value>");
 				htmlOutput.append("</header>");
 			}
 		}
 		htmlOutput.append("</headers>");
 		htmlOutput.append("</HTTP_OPTIONS_response>");
 
 		return htmlOutput.toString();
 	}
 
 }
