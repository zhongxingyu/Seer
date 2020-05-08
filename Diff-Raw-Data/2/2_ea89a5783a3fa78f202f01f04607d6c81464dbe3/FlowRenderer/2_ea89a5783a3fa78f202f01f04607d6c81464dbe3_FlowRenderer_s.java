 package org.amplafi.flow;
 
 import java.io.Writer;
 
 import org.amplafi.flow.web.FlowRequest;
 
 /**
  * Implementations render the flow output for at the conclusion of processing a flow request.
  * @author patmoore
  *
  */
 public interface FlowRenderer {
 	
 	/**
 	 * @return json, html, xml etc..
 	 */
 	public String getRenderResultType();
 
 	/**
 	 * TODO
 	 * 
 	 * @param flowState
 	 * @param writer
 	 */
 	public void render(FlowState flowState, Writer writer);
 	
 	/**
 	 * TODO
 	 * 
 	 * @param flowState
 	 * @param message
 	 * @param exception
 	 * @param writer
 	 */
	public void renderError(FlowState flowState, String message, Exception exception, Writer writer);
 
 	/**
 	 * TODO
 	 * 
 	 */
 	public void describe(FlowRequest flowRequest);
 
 }
