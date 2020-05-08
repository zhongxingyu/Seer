 package com.kokotchy.betaSeriesAPI.api.factories;
 
 import org.dom4j.Node;
 
 import com.kokotchy.betaSeriesAPI.UtilsXml;
 import com.kokotchy.betaSeriesAPI.model.Error;
 
 /**
  * Error factory
  * 
  * @author kokotchy
  */
 public class ErrorFactory {
 	/**
 	 * Create an error from a node
 	 * 
 	 * @param node
 	 *            Node
 	 * @return Error
 	 */
 	public static Error createError(Node node) {
 		Error error = new Error(UtilsXml.readInt(node, "#code"));
 		error.setText(node.getText());
 		return error;
 	}
 
 }
