 /*
  * Copyright 2013 Takao Nakaguchi.
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
 package org.jsonman;
 
 import java.text.ParseException;
 import java.util.Iterator;
 
 import org.jsonman.finder.Reference;
 import org.jsonman.finder.ReferenceParser;
 import org.jsonman.node.ArrayNode;
 import org.jsonman.node.MapNode;
 
 public class NodeUpdater {
 	public NodeUpdater(Node target){
 		this.target = target;
 	}
 
 	public Node getTarget() {
 		return target;
 	}
 
 	public void update(String referencePath, Node node) throws ParseException{
 		update(ReferenceParser.parse(referencePath), node);
 	}
 
 	public void update(Iterable<Reference> path, Node node){
 		final Iterator<Reference> it = path.iterator();
 		Node t = target;
 		while(true){
 			Reference r = it.next();
 			if(t.isArray() && r.isArray()){
 				ArrayNode an = t.cast();
 				Integer index = r.getId();
 				t = an.getChild(index);
 				if(t != null){
 					if(!it.hasNext()){
 						if(t.isMap() && node.isMap()){
 							((MapNode)t).mergeValue((MapNode)node);
 						} else{
 							t.setValue(node.getValue());
 						}
 						break;
 					}
 					continue;
 				} else{
 					an.setChild(index, createNode(it, node));
 					break;
 				}
 			} else if(t.isMap() && r.isMap()){
 				MapNode mn = t.cast();
 				String name = r.getId();
 				t = mn.getChild(name);
 				if(t != null){
 					if(!it.hasNext()){
 						if(t.isMap() && node.isMap()){
 							((MapNode)t).mergeValue((MapNode)node);
 						} else{
							mn.setChild(name, node);
 						}
 						break;
 					}
 					continue;
 				} else{
 					mn.setChild(name, createNode(it, node));
 					break;
 				}
 			} else{
 				throw new RuntimeException(String.format(
 						"type of node(%s) and reference(%s) not match.",
 						t.getClass().getName(),
 						r.getClass().getName()
 						));
 			}
 		}
 	}
 	static Node createNode(Iterator<Reference> it, Node leaf){
 		Reference ref = it.next();
 		Node child = null;
 		if(it.hasNext()){
 			child = createNode(it, leaf);
 		} else{
 			child = leaf;
 		}
 		Node n = null;
 		if(ref.isMap()){
 			MapNode mn = new MapNode();
 			mn.appendChild((String)ref.getId(), child);
 			n = mn;
 		} else if(ref.isArray()){
 			ArrayNode an = new ArrayNode();
 			an.setChild((Integer)ref.getId(), child);
 			n = an;
 		} else{
 			return null;
 		}
 		return n;
 	}
 
 	private Node target;
 }
