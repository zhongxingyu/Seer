 /*
  File: EqnAttrTracker.java
 
  Copyright (c) 2010, The Cytoscape Consortium (www.cytoscape.org)
 
  This library is free software; you can redistribute it and/or modify it
  under the terms of the GNU Lesser General Public License as published
  by the Free Software Foundation; either version 2.1 of the License, or
  any later version.
 
  This library is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
  MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
  documentation provided hereunder is on an "as is" basis, and the
  Institute for Systems Biology and the Whitehead Institute
  have no obligations to provide maintenance, support,
  updates, enhancements or modifications.  In no event shall the
  Institute for Systems Biology and the Whitehead Institute
  be liable to any party for direct, indirect, special,
  incidental or consequential damages, including lost profits, arising
  out of the use of this software and its documentation, even if the
  Institute for Systems Biology and the Whitehead Institute
  have been advised of the possibility of such damage.  See
  the GNU Lesser General Public License for more details.
 
  You should have received a copy of the GNU Lesser General Public License
  along with this library; if not, write to the Free Software Foundation,
  Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
 package cytoscape.data.readers;
 
 
 import cytoscape.Cytoscape;
 import cytoscape.data.CyAttributes;
 import cytoscape.logger.CyLogger;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.cytoscape.equations.EqnCompiler;
 import org.cytoscape.equations.Equation;
 
 
 public class EqnAttrTracker {
 	private enum AttrClass {
 		NODES(Cytoscape.getNodeAttributes()),
 		EDGES(Cytoscape.getEdgeAttributes()),
 		NETWORKS(Cytoscape.getNetworkAttributes());
 
 		private CyAttributes attribs;
 
 		AttrClass(final CyAttributes attribs) { this.attribs = attribs; }
 
 		CyAttributes getAttribs() { return attribs; }
 	};
 	
 
 	private static class AttrInfo {
 		private String id;
 		private String attrName;
 		private String equation;
 		private Class returnType;
 
 		AttrInfo(final String id, final String attrName, final String equation, final Class returnType) {
 			this.id = id;
 			this.attrName = attrName;
 			this.equation = equation;
 			this.returnType = returnType;
 		}
 
 		String getID() { return id; }
 		String getAttrName() { return attrName; }
 		String getEquation() { return equation; }
 		Class getReturnType() { return returnType; }
 	}
 
 	private CyLogger logger;
 	private List<AttrInfo> nodeAttrs, edgeAttrs, networkAttrs;
 
 	public EqnAttrTracker() {
 		logger = CyLogger.getLogger(EqnAttrTracker.class);
 		nodeAttrs = new ArrayList<AttrInfo>();
 		edgeAttrs = new ArrayList<AttrInfo>();
 		networkAttrs = new ArrayList<AttrInfo>();
 	}
 
 	void reset() {
 		nodeAttrs.clear();
 		edgeAttrs.clear();
 		networkAttrs.clear();
 	}
 
 	void recordEquation(final CyAttributes attribs, final String id, final String attrName,
 			    final String equation, final Class returnType)
 	{
 		if (attribs == AttrClass.NODES.getAttribs())
 			nodeAttrs.add(new AttrInfo(id, attrName, equation, returnType));
 		else if (attribs == AttrClass.EDGES.getAttribs())
 			edgeAttrs.add(new AttrInfo(id, attrName, equation, returnType));
 		else if (attribs == AttrClass.NETWORKS.getAttribs())
 			networkAttrs.add(new AttrInfo(id, attrName, equation, returnType));
 		else
 			throw new IllegalArgumentException("unkown CyAttributes!");
 
 		if (returnType == Double.class)
 			registerFloat(attribs, id, attrName);
 		else if (returnType == Long.class)
 			registerInteger(attribs, id, attrName);
 		else if (returnType == String.class)
 			registerString(attribs, id, attrName);
 		else if (returnType == Boolean.class)
 			registerBoolean(attribs, id, attrName);
 		else if (returnType == List.class)
 			registerList(attribs, id, attrName);
 		else
 			throw new IllegalArgumentException("unknown retuyrn type: " + returnType + "!");
 	}
 
 	void addAllEquations() {
 		addEquations(AttrClass.NODES.getAttribs(), nodeAttrs);
 		addEquations(AttrClass.EDGES.getAttribs(), edgeAttrs);
 		addEquations(AttrClass.NETWORKS.getAttribs(), networkAttrs);
 	}
 
 	private void addEquations(final CyAttributes attribs, final List<AttrInfo> attrInfos) {
 		final EqnCompiler compiler = new EqnCompiler();
 		final String[] allAttribNames = attribs.getAttributeNames();
 		final Class[] allTypes = new Class[allAttribNames.length];
 		final Map<String, Class> attribNameToTypeMap = new HashMap<String, Class>();
 		int index = 0;
 		for (final String attribName : allAttribNames) {
 			final byte type = attribs.getType(attribName);
 			final Class eqnType = mapCytoscapeAttribTypeToEqnType(type);
 			if (eqnType != null)
 				attribNameToTypeMap.put(attribName, eqnType);
 		}
 		
 		for (final AttrInfo attrInfo : attrInfos) {
 			attribNameToTypeMap.remove(attrInfo.getAttrName());
 
 			if (compiler.compile(attrInfo.getEquation(), attribNameToTypeMap))
 				attribs.setAttribute(attrInfo.getID(), attrInfo.getAttrName(), compiler.getEquation());
 			else {
 				final String errorMessage = compiler.getLastErrorMsg();
 				logger.warn("bad equation on import: " + errorMessage);
 				final Equation errorEquation = Equation.getErrorEquation(attrInfo.getEquation(),
 											 attrInfo.getReturnType(), errorMessage);
 				attribs.setAttribute(attrInfo.getID(), attrInfo.getAttrName(), errorEquation);
 			}
 
 			attribNameToTypeMap.put(attrInfo.getAttrName(), attrInfo.getReturnType());
 		}
 	}
 
 	private static Class mapCytoscapeAttribTypeToEqnType(final byte attribType) {
 		switch (attribType) {
 		case CyAttributes.TYPE_BOOLEAN:
 			return Boolean.class;
 		case CyAttributes.TYPE_INTEGER:
 			return Long.class;
 		case CyAttributes.TYPE_FLOATING:
 			return Double.class;
 		case CyAttributes.TYPE_STRING:
 			return String.class;
 		case CyAttributes.TYPE_SIMPLE_LIST:
 			return List.class;
 		default:
			throw new IllegalStateException("can't map Cytoscape type " + attribType + " to equation return type!");
 		}
 	}
 
 	private static void registerFloat(final CyAttributes attrs, final String id, final String attrName) {
 		attrs.setAttribute(id, attrName, 0.0);
 		attrs.deleteAttribute(id, attrName);
 	}
 
 	private static void registerString(final CyAttributes attrs, final String id, final String attrName) {
 		attrs.setAttribute(id, attrName, "");
 		attrs.deleteAttribute(id, attrName);
 	}
 
 	private static void registerInteger(final CyAttributes attrs, final String id, final String attrName) {
 		attrs.setAttribute(id, attrName, 0);
 		attrs.deleteAttribute(id, attrName);
 	}
 
 	private static void registerBoolean(final CyAttributes attrs, final String id, final String attrName) {
 		attrs.setAttribute(id, attrName, true);
 		attrs.deleteAttribute(id, attrName);
 	}
 
 	private static void registerList(final CyAttributes attrs, final String id, final String attrName) {
 		attrs.setListAttribute(id, attrName, new ArrayList());
 		attrs.deleteAttribute(id, attrName);
 	}
 }
