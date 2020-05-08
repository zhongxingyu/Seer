 /**
  * Copyright (c) 2012, Institute of Telematics, Institute of Information Systems (Dennis Pfisterer, Sven Groppe, Andreas Haller, Thomas Kiencke, Sebastian Walther, Mario David), University of Luebeck
  *
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
  * following conditions are met:
  *
  * 	- Redistributions of source code must retain the above copyright notice, this list of conditions and the following
  * 	  disclaimer.
  * 	- Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
  * 	  following disclaimer in the documentation and/or other materials provided with the distribution.
  * 	- Neither the name of the University of Luebeck nor the names of its contributors may be used to endorse or promote
  * 	  products derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
  * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
  * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
  * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
  * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package de.uzl.decentsparqle.luposdate.operators.formatter;
 
 import de.uzl.decentsparqle.luposdate.index.P2PIndexCollection;
 import de.uzl.decentsparqle.luposdate.index.P2PIndexScan;
 import de.uzl.decentsparqle.luposdate.operators.P2PApplication;
 import lupos.engine.operators.BasicOperator;
 import lupos.engine.operators.OperatorIDTuple;
 import lupos.engine.operators.index.BasicIndexScan;
 import lupos.engine.operators.index.Dataset;
 import lupos.engine.operators.index.Root;
 import lupos.engine.operators.singleinput.Filter;
 import lupos.engine.operators.singleinput.Result;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map.Entry;
 
 import static com.google.common.base.Throwables.propagate;
 import static com.google.common.collect.Lists.newLinkedList;
 import static com.google.common.collect.Maps.newHashMap;
 
 // TODO: Auto-generated Javadoc
 
 /**
  * The Class SubGraphContainerFormatter.
  */
 public class SubGraphContainerFormatter implements OperatorFormatter {
 
 	private int id_counter;
 
 	private BasicOperator root;
 
 	private Dataset dataset;
 
 	private P2PApplication p2pApplication;
 
 	public SubGraphContainerFormatter() {
 	}
 
 	public SubGraphContainerFormatter(Dataset dataset,
 									  P2PApplication p2pApplication) {
 		this.dataset = dataset;
 		this.p2pApplication = p2pApplication;
 	}
 
 	public JSONObject serialize(BasicOperator operator, int node_id) throws JSONException {
 
 		Collection<JSONObject> nodesJSON = newLinkedList();
 		Collection<JSONObject> edgesJSON = newLinkedList();
 
 		id_counter = 0;
 
 		serializeNode(new OperatorIDTuple(operator, 0), nodesJSON, edgesJSON, id_counter);
 		JSONObject serializedSubGraph = new JSONObject();
 
 		try {
 			serializedSubGraph.put("nodes", nodesJSON);
 			serializedSubGraph.put("edges", edgesJSON);
 		} catch (JSONException e) {
 			throw propagate(e);
 		}
 
 		return serializedSubGraph;
 	}
 
 	private void serializeNode(OperatorIDTuple node,
 							   Collection<JSONObject> nodesJSON,
 							   Collection<JSONObject> edgesJSON,
 							   int parent_id) {
 		id_counter++;
 
 		int edge_id = node.getId();
 
 		BasicOperator op = node.getOperator();
 
 		if (parent_id > 0) {
 			JSONObject edge = new JSONObject();
 			try {
 				edge.put("from", parent_id);
 				edge.put("to", id_counter);
 				edge.put("edge_id", edge_id);
 				edgesJSON.add(edge);
 			} catch (JSONException e) {
 				e.printStackTrace();
 			}
 		}
 
 		OperatorFormatter serializer;
 		if (op instanceof BasicIndexScan) {
 			serializer = new P2PIndexScanFormatter();
 		} else if (op instanceof Root) {
 			serializer = new P2PIndexCollectionFormatter();
 		} else if (op instanceof Result) {
 			serializer = new ResultFormatter();
 		} else if (op instanceof Filter) {
 			serializer = new FilterFormatter();
 		} else {
 			throw new RuntimeException("Something is wrong here. Forgot case?");
 		}
 
 		try {
 			nodesJSON.add(serializer.serialize(op, id_counter));
 		} catch (NullPointerException e) {
 			throw new IllegalArgumentException("Dieser Operator ist bisher nicht serialisierbar", e);
 		} catch (JSONException e) {
 			throw propagate(e);
 		}
 
 		for (OperatorIDTuple successor : op.getSucceedingOperators()) {
 			serializeNode(successor, nodesJSON, edgesJSON, id_counter);
 		}
 	}
 
 	public BasicOperator deserialize(JSONObject serialiezedOperator) throws JSONException {
 		root = null;
 
 		HashMap<Integer, BasicOperator> nodes = deserializeNodes(serialiezedOperator);
 
 		JSONArray edgesJson = (JSONArray) serialiezedOperator.get("edges");
 		deserializeEdges(edgesJson, nodes);
 
 		return root;
 	}
 
 	private void deserializeEdges(JSONArray edgesJson, HashMap<Integer, BasicOperator> nodes) throws JSONException {
 
 		HashMap<BasicOperator, List<OperatorIDTuple>> succeedingOperators = newHashMap();
 		HashMap<BasicOperator, List<BasicOperator>> precedingOperators = newHashMap();
 
 		for (int i = 0; i < edgesJson.length(); i++) {
 
 			JSONObject edgeJson = edgesJson.getJSONObject(i);
 
 			BasicOperator from = nodes.get(edgeJson.getInt("from"));
 			BasicOperator to = nodes.get(edgeJson.getInt("to"));
 
 			if (succeedingOperators.get(from) == null) {
 				succeedingOperators.put(from, new LinkedList<OperatorIDTuple>());
 			}
 
 			if (precedingOperators.get(to) == null) {
 				precedingOperators.put(to, new LinkedList<BasicOperator>());
 			}
 
 			succeedingOperators.get(from).add(new OperatorIDTuple(to, edgeJson.getInt("edge_id")));
 			precedingOperators.get(to).add(from);
 		}
 
 		for (Entry<BasicOperator, List<OperatorIDTuple>> from : succeedingOperators.entrySet()) {
 			from.getKey().setSucceedingOperators(from.getValue());
 		}
 
 		for (Entry<BasicOperator, List<BasicOperator>> to : precedingOperators.entrySet()) {
 			to.getKey().setPrecedingOperators(to.getValue());
 		}
 	}
 
 	private HashMap<Integer, BasicOperator> deserializeNodes(JSONObject rootJson) throws JSONException {
 
 		HashMap<Integer, BasicOperator> nodes = newHashMap();
 		JSONArray nodesJson = (JSONArray) rootJson.get("nodes");
 
 		HashMap<String, OperatorFormatter> formatters = createFormatters();
 
 		for (int i = 0; i < nodesJson.length(); i++) {
 
 			JSONObject nodeJson = nodesJson.getJSONObject(i);
 
 			// get corresponding formatter from map
 			OperatorFormatter formatter = formatters.get(nodeJson.getString("type"));
 
 			// add deserialized node to list
 			BasicOperator node = formatter.deserialize(nodeJson);
 			nodes.put(nodeJson.getInt("node_id"), node);
 
 			if (node instanceof P2PIndexCollection) {
 				P2PIndexScanFormatter p2pIndexScanFormatter = (P2PIndexScanFormatter) formatters.get(
 						P2PIndexScan.class.getName()
 				);
 				p2pIndexScanFormatter.setRoot((P2PIndexCollection) node);
 			}
 
 			try {
 				if (nodeJson.getBoolean("root")) {
 					root = node;
 				}
 			} catch (JSONException e) {
				throw propagate(e);
 			}
 		}
 
 		return nodes;
 	}
 
 	private HashMap<String, OperatorFormatter> createFormatters() {
 		final HashMap<String, OperatorFormatter> formatters = newHashMap();
 		formatters.put(P2PIndexCollection.class.getName(), new P2PIndexCollectionFormatter(dataset));
 		formatters.put(P2PIndexScan.class.getName(), new P2PIndexScanFormatter());
 		formatters.put(Filter.class.getName(), new FilterFormatter());
 		formatters.put(Result.class.getName(), new ResultFormatter(p2pApplication));
 		return formatters;
 	}
 
 }
