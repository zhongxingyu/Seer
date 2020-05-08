 /** 
  * Copyright 2010 Douglas Linder.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package twisted.client.impl;
 
 import java.util.ArrayList;
 
import twisted.client.ComponentLog;

 import com.google.gwt.dom.client.Document;
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.dom.client.Node;
 import com.google.gwt.dom.client.NodeList;
 
 /** 
  * Custom query implementation. 
  * <p>
  * TODO: Once GWT supports getElementsByClassName, use that.<br/>
  * see http://code.google.com/p/google-web-toolkit/issues/detail?id=3441
  */
 public class ComponentQuery {
 	
 	/** Nodes for this list. */
 	private ArrayList<Element> nodes = null;
 	
 	/** Node list stack. */
 	private static ArrayList<NodeList<Node>> children = new ArrayList<NodeList<Node>>();
 	
 	protected ComponentQuery(ArrayList<Element> nodes) {
 		this.nodes = nodes;
 	}
 	
 	/** Returns a list of elements matching the given classname from the document body. */
 	public static ComponentQuery query(String classname) {
 		ComponentQuery rtn = query(classname, Document.get().getBody());
 		return(rtn);
 	}
 	
 	/** Returns a list of elements matching the given classname which are children of root. */
 	public static ComponentQuery query(String classname, Element root) {
 		ComponentQuery rtn = null;
 		if (root != null) {
 			ArrayList<Element> rtnSet = new ArrayList<Element>();
 			rtn =  new ComponentQuery(rtnSet);
 			children.add(root.getChildNodes());
 			while(children.size() > 0) {
 				NodeList<Node> set = children.get(0);
 				children.remove(set);
 				for (int i = 0; i < set.getLength(); ++i) {
 					Node n = set.getItem(i);
 					if (n.getNodeType() == Node.ELEMENT_NODE) {
 						if (n.hasChildNodes()) 
							children.add(n.getChildNodes());
						if (matchesClassTarget(n, classname)) 
							rtnSet.add(Element.as(n));
 					}
 				}
 			}
 		}
 		return(rtn);
 	}
 	
 	/** Looks for a matching classname... */
 	private static boolean matchesClassTarget(Node n, String classname) {
 		boolean rtn = true;
 		int offset = 0;
 		String rawClassname = Element.as(n).getClassName();
 		while(rtn) {
 			offset = rawClassname.indexOf(classname, offset);
 			if (offset == -1) 
 				rtn = false;
 			else {
 				// Allowable match conditions are:
 				// 1) Space before and after instance.
 				// 2) Space before and end of string after instance.
 				// 3) Start before and space after instance.
 				// 4) Start before and end after instance. 
 				int length = rawClassname.length();
 				if (length == classname.length()) 
 					break; // Case 4
 				else {
 					char pre = offset == 0 ? '\0' : rawClassname.charAt(offset - 1);
 					char post = (offset + classname.length()) == length  ? '\0' : rawClassname.charAt(offset + classname.length());
 					if ((pre == ' ') && (post == ' '))
 						break; // Case 1
 					else if ((pre == '\0') && (post == ' '))
 						break; // Case 3
 					else if ((pre == ' ') && (post == '\0'))
 						break; // Case 2
 				}
				++offset; // Start at the next character.
 			}
 		}
 		return(rtn);
 	}
 	
 	/** Returns the count of elements currently held. */
 	public int getLength() {
 		if (nodes == null)
 			return(0);
 		else
 			return(nodes.size());
 	}
 	
 	/** Returns an item from those held. */
 	public Element getItem(int index) {
 		Element rtn = null;
 		if (nodes != null)
 			rtn = nodes.get(index);
 		return(rtn);
 	}
 }
