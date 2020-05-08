/*
 * Copyright (C) 2006-2007 MOSTRARE INRIA Project
 * 
 * This file is part of XCRF, an implementation of CRFs for trees (http://treecrf.gforge.inria.fr)
 * 
 * XCRF is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * XCRF is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with XCRF; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package mostrare.tree.io.impl;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import mostrare.crf.tree.CharacteristicBinaryOperator;
import mostrare.crf.tree.CharacteristicConstant;
import mostrare.crf.tree.CharacteristicConstructor;
import mostrare.crf.tree.CharacteristicLogExp;
import mostrare.crf.tree.CharacteristicMethod;
import mostrare.crf.tree.CharacteristicStat;
import mostrare.crf.tree.CharacteristicTypeAccess;
import mostrare.crf.tree.CharacteristicVar;
import mostrare.crf.tree.VirtualTransforms;
import mostrare.crf.tree.impl.CRFWithConstraintNode;
import mostrare.tree.Node;
import mostrare.tree.impl.NodeAST;
import mostrare.tree.impl.TreeAST;
import mostrare.tree.io.TreeReader;

/**
 * @author missi
 */
public class DefaultTreeReader implements TreeReader {
	private static TreeReader instance = null;

	static {
		instance = new DefaultTreeReader();
	}

	private DefaultTreeReader() {
	}

	public static TreeReader getInstance() {
		return instance;
	}

	@Override
	public ArrayList<TreeAST> readTree(File file, CRFWithConstraintNode crf) {
		ArrayList<TreeAST> trainingtrees = new ArrayList<TreeAST>();
		JSONParser parser = new JSONParser();

		try {
	//		long countOfLines = Files.lines(Paths.get(new File(file.getAbsolutePath()).getPath())).count(); 

				FileReader readeroffile = new FileReader(file.getAbsolutePath());
				Object obj = parser.parse(readeroffile);
				JSONObject jsonObject = (JSONObject) obj;

							JSONArray astList = (JSONArray) (jsonObject.get("faulty_ast"));

							JSONObject context = (JSONObject) (jsonObject.get("context"));

							JSONObject contextinfo = (JSONObject) context.get("cntx");
							

							if (astList.size() > 0) {

								JSONObject faulty_ast = (JSONObject) (astList.get(0));
								String nodetype = faulty_ast.get("type").toString();
								JSONArray actionList = (JSONArray) faulty_ast.get("susp");
								String namefornode = faulty_ast.get("label").toString();
								
								String logicalExpIdentity = "";
								try {
									logicalExpIdentity = faulty_ast.get("index_of_logical_exper").toString();
								} catch (Exception e) {
									logicalExpIdentity = "";
								}
								
								String typeofreturn = "";
								try {
									typeofreturn = jsonObject.get("return_type").toString();
								} catch (Exception e) {
									typeofreturn = "";
								}
								
								String expIdentity = "";
								try {
									expIdentity = faulty_ast.get("index_of_exper").toString();
								} catch (Exception e) {
									expIdentity = "";
								}
								
								String binaryoperatorIdentity = "";
								try {
									binaryoperatorIdentity = faulty_ast.get("index_of_binary_operator").toString();
								} catch (Exception e) {
									binaryoperatorIdentity = "";
								}

								NodeAST virtualroot = new NodeAST(0, "VIRTUALROOT", crf);
								getNodeCharacterInfo(virtualroot, contextinfo, namefornode, crf);
							//	virtualroot.setIsBooleanType(false);
								virtualroot.setParentNode(null);
								virtualroot.setLogicalExpressionIdentity("");
								virtualroot.setExpressionIdentity("");
								virtualroot.setBinaryOperatorIdentity("");
								virtualroot.setTypeofReturn("");
								virtualroot.setNameofNode("DEFAULT");

								String virtualtransform = "EMPTY";
								ArrayList<String> alltransforms = new ArrayList<String>();
								ArrayList<String> originalname = new ArrayList<String>();

								String[] roottransformname = getUniqueLabel(actionList);

								for (int rootindex = 0; rootindex < roottransformname.length; rootindex++) {
									
									String modifyname = modifyTransformName(roottransformname[rootindex]);
									
									if(modifyname.equals("wrapsLoop")) {
										originalname.add(roottransformname[rootindex]);
										continue;
									}
									
									for (VirtualTransforms cha : VirtualTransforms.values()) {
										if (modifyname.equals(cha.toString())) {
											alltransforms.add(modifyname);
											originalname.add(roottransformname[rootindex]);
											break;
										}
									}
								}

								if (alltransforms.size() > 0) {
									virtualtransform = alltransforms.get(0);
								}
								virtualroot.setNodeLabel(virtualtransform);
								virtualroot.setAnnotation(crf.getAnnotationIndex(virtualtransform));

								TreeAST tree = new TreeAST(virtualroot);
								tree.setFileNmae(file.getName());
								virtualroot.setTree(tree);
								tree.addNode(virtualroot);

								for (int removeindex = 0; removeindex < originalname.size(); removeindex++) {
									roottransformname = ArrayUtils.removeElement(roottransformname,
											originalname.get(removeindex));
								}

								NodeAST root = new NodeAST(tree.getNodesNumber(), nodetype, crf);

								String nodelabelroot = roottransformname.length > 0
										? modifyTransformName(roottransformname[0])
										: "EMPTY";
								root.setNodeLabel(nodelabelroot);
								root.setAnnotation(crf.getAnnotationIndex(nodelabelroot));
								// virtualroot.addNode(root);
								root.setLogicalExpressionIdentity(logicalExpIdentity);
								root.setExpressionIdentity(expIdentity);
								root.setBinaryOperatorIdentity(binaryoperatorIdentity);
								root.setNameofNode(namefornode);
								root.setTypeofReturn(typeofreturn);
								int oldNodesNumber = virtualroot.orderedNodesNumber;
								virtualroot.orderedChildrenNodes.add(root);
								root.setParentNode(virtualroot);
								virtualroot.setOrderedNodesNumber(oldNodesNumber + 1);
								root.setPosition(oldNodesNumber);
								root.setTree(tree);
								tree.addNode(root);

								if (root.getPosition() != 0) {
									// adds relations to the previous node
									NodeAST lastChild = (NodeAST) virtualroot.orderedChildrenNodes
											.getQuick(oldNodesNumber - 1);
									lastChild.setNextSibling(root);
									root.setPreviousSibling(lastChild);
								} else
									virtualroot.firstChild = root;

								getNodeCharacterInfo(root, contextinfo, namefornode, crf);
							//	root.setIsBooleanType(deterWhetherRootLogical(root, returntypefornode));

								readEachJSONNode(faulty_ast, contextinfo, tree, root, crf);

								for (int i = 0; i < tree.getNodesNumber(); i++) {
									Node toStudy = tree.getNode(i);
									if (i != 0) {
										if (toStudy.getNodeLabel().equals("addassignment")
												|| toStudy.getNodeLabel().equals("wrapsTryCatch")
												|| toStudy.getNodeLabel().equals("wrapsLoop"))
											toStudy.setNodeLabel("EMPTY");
									}
								}

								adjustFeatureV4(tree, crf);
								
								tree.setNumberofTransform(checkTransformNumber(tree));
								
								if (tree.getNumberofTransform()!=0 )
									trainingtrees.add(tree);

							}
						
				readeroffile.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return trainingtrees;
	}
	
	public void adjustFeatureV4(TreeAST ast, CRFWithConstraintNode crf) {
		
		for (int i = 0; i < ast.getNodesNumber(); i++) {
			
			NodeAST toStudy = (NodeAST) ast.getNode(i);
			
			boolean whethernotfirstusedasparemeter = false;
			
			if (toStudy.getNodeType().equals("VariableRead") || toStudy.getNodeType().equals("VariableWrite")
					|| toStudy.getNodeType().equals("FieldRead") || toStudy.getNodeType().equals("FieldWrite")) {
				if(toStudy.getParentNode().getNodeType().equals("Invocation") 
						|| toStudy.getParentNode().getNodeType().equals("ConstructorCall")) {
					Node previous =toStudy;
					
					while(previous.getPreviousSibling()!=null) {
						previous= previous.getPreviousSibling();
						
						if(previous.getNodeType().equals(toStudy.getNodeType()) 
								&& previous.getNameofNode().equals(toStudy.getNameofNode())) {
							whethernotfirstusedasparemeter = true;
							break;
						}
					}
				}
				
				if(whethernotfirstusedasparemeter)
					toStudy.charactervaluemap.put(crf.getCharacterIndex("V4B_USED_MULTIPLE_AS_PARAMETER"), true);
			}
		}
	}
	
	public int checkTransformNumber(TreeAST inputtree) {
		
		int number=0;
		
		for (int i = 0; i < inputtree.getNodesNumber(); i++) {
			Node toStudy = inputtree.getNode(i);
			if (!toStudy.getNodeLabel().equals("EMPTY")) {
				number+=1;
			}
		}

		return number;
	}

	public String modifyTransformName(String inputname) {

		if (inputname.equals("susp_wrapsIf_elsenull"))
			inputname = "wrapsIf_Others";
		if (inputname.equals("susp_wrapsIfElse_elsenotnull"))
			inputname = "wrapsIfElse_Others";

		if (inputname.indexOf("susp_") != -1) {
			String[] splittedarray = inputname.split("_");
			inputname = splittedarray[1];
		}

		return inputname;
	}

	public void readEachJSONNode(JSONObject faulty_node, JSONObject contextinfo, TreeAST ast, NodeAST parentNode,
			CRFWithConstraintNode crf) {

		JSONArray childrenList = (JSONArray) (faulty_node.get("children"));
		ArrayList<NodeAST> parentnodelist = new ArrayList<NodeAST>();
		parentnodelist.clear();

		if (childrenList.size() > 0) {
			for (int index = 0; index < childrenList.size(); index++) {

				JSONObject jsonObject = (JSONObject) (childrenList.get(index));
				String nodetype = jsonObject.get("type").toString();
				JSONArray actionListinner = (JSONArray) jsonObject.get("susp");

				String namefornode = jsonObject.get("label").toString();
				
				String typeofreturn = "";
				try {
					typeofreturn = jsonObject.get("return_type").toString();
				} catch (Exception e) {
					typeofreturn = "";
				}
				
				String logicalExpIdentity = "";
				try {
					logicalExpIdentity = jsonObject.get("index_of_logical_exper").toString();
				} catch (Exception e) {
					logicalExpIdentity = "";
				}
				
				String expIdentity = "";
				try {
					expIdentity = jsonObject.get("index_of_exper").toString();
				} catch (Exception e) {
					expIdentity = "";
				}
				
				String binaryoperatorIdentity = "";
				try {
					binaryoperatorIdentity = jsonObject.get("index_of_binary_operator").toString();
				} catch (Exception e) {
					binaryoperatorIdentity = "";
				}

				NodeAST currentnode = new NodeAST(ast.getNodesNumber(), nodetype, crf);
				String nodelabelcurrent = modifyTransformName(getUniqueLabel(actionListinner)[0]);
				currentnode.setNodeLabel(nodelabelcurrent);
				currentnode.setAnnotation(crf.getAnnotationIndex(nodelabelcurrent));
				currentnode.setLogicalExpressionIdentity(logicalExpIdentity);
				currentnode.setExpressionIdentity(expIdentity);
				currentnode.setBinaryOperatorIdentity(binaryoperatorIdentity);
				currentnode.setNameofNode(namefornode);
				currentnode.setTypeofReturn(typeofreturn);
				
				currentnode.jsonnode = jsonObject;

				// parentNode.addNode(currentnode);

				ast.addNode(currentnode);
				parentnodelist.add(currentnode);

				int oldNodesNumber = parentNode.orderedNodesNumber;
				parentNode.orderedChildrenNodes.add(currentnode);
				currentnode.setParentNode(parentNode);
				parentNode.setOrderedNodesNumber(oldNodesNumber + 1);
				currentnode.setPosition(oldNodesNumber);
				currentnode.setTree(ast);

				if (currentnode.getPosition() != 0) {
					// adds relations to the previous node
					NodeAST lastChild = (NodeAST) parentNode.orderedChildrenNodes.getQuick(oldNodesNumber - 1);
					lastChild.setNextSibling(currentnode);
					currentnode.setPreviousSibling(lastChild);
				} else
					parentNode.firstChild = currentnode;

				getNodeCharacterInfo(currentnode, contextinfo, namefornode, crf);
				// currentnode.setIsBooleanType(deterWhetherRootLogical(currentnode, returntypefornode));
			}

			for (int index = 0; index < childrenList.size(); index++) {
				NodeAST parentnode = parentnodelist.get(index);
				JSONObject innernode = ((JSONObject) (childrenList.get(index)));
				readEachJSONNode(innernode, contextinfo, ast, parentnode, crf);
			}
		}
	}

	public String[] getUniqueLabel(JSONArray actionList) {

		if (actionList != null) {

			String trimmed = actionList.toString().replaceAll("\\]", "").replaceAll("\\[", "").replaceAll("\"", "");
			String[] splitted = trimmed.split(",");
			splitted = Arrays.stream(splitted).distinct().toArray(String[]::new);

			splitted = postProcessingTransform(splitted);

			if (splitted.length > 0)
				return splitted;
			else
				return new String[] { "EMPTY" };
		} else
			return new String[] { "EMPTY" };
	}

	public String[] postProcessingTransform(String[] inputtransform) {
		   
		   // some special processing for "susp_unwrapMethod"
		   if(ArrayUtils.contains(inputtransform, "susp_unwrapMethod") 
				   && ArrayUtils.contains(inputtransform, "susp_wrapsTryCatch")) {
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_unwrapMethod");
		   }
		   
		   if(ArrayUtils.contains(inputtransform, "susp_unwrapMethod") 
				   && ArrayUtils.contains(inputtransform, "susp_wrapsIfElse_elsenotnull")) {
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrapsIfElse_elsenotnull");
		   }
		   
		   if(ArrayUtils.contains(inputtransform, "susp_unwrapMethod") 
				   && ArrayUtils.contains(inputtransform, "susp_wrapsLoop")) {
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_unwrapMethod");
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrapsLoop");   
		   }
		   
		   if(ArrayUtils.contains(inputtransform, "susp_unwrapMethod") 
				   && ArrayUtils.contains(inputtransform, "susp_wrongMethodRef_Removed_Invocation_Added_FieldRead")) {
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongMethodRef_Removed_Invocation_Added_FieldRead");
		   }
		   
		   // pot-process null-check related transforms
		   if(ArrayUtils.contains(inputtransform, "susp_missNullCheckP") 
				   && ArrayUtils.contains(inputtransform, "susp_wrapsIf_elsenull")) {
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrapsIf_elsenull");
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_missNullCheckP");
			   inputtransform = add(inputtransform, "wrapsIf_NULL");
		   }
		   
		   if(ArrayUtils.contains(inputtransform, "susp_missNullCheckN") 
				   && ArrayUtils.contains(inputtransform, "susp_wrapsIf_elsenull")) {  
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrapsIf_elsenull");
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_missNullCheckN");
			   inputtransform = add(inputtransform, "wrapsIf_NULL");
		   }

		   if(ArrayUtils.contains(inputtransform, "susp_missNullCheckP") 
				   && ArrayUtils.contains(inputtransform, "susp_wrapsIfElse_elsenotnull")) {
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrapsIfElse_elsenotnull");
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_missNullCheckP");
			   inputtransform = add(inputtransform, "wrapsIfElse_NULL");
		   }
		   
		   if(ArrayUtils.contains(inputtransform, "susp_missNullCheckN") 
				   && ArrayUtils.contains(inputtransform, "susp_wrapsIfElse_elsenotnull")) {
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrapsIfElse_elsenotnull");
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_missNullCheckN");
			   inputtransform = add(inputtransform, "wrapsIfElse_NULL");
		   }
		  
	       // post-process a few remaining unnormbal susp_missNullCheckP or susp_missNullCheckN
		   if(inputtransform.length==1 && ArrayUtils.contains(inputtransform, "susp_missNullCheckP"))
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_missNullCheckP");
		   
		   if(inputtransform.length==1 && ArrayUtils.contains(inputtransform, "susp_missNullCheckN"))
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_missNullCheckN");
		   
		   if(ArrayUtils.contains(inputtransform, "susp_missNullCheckN") 
				   && ArrayUtils.contains(inputtransform, "wrapsIf_NULL")) {
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_missNullCheckN");
		   }
		   
		   // post0process a few remaining unnorml wrap if and unwrap if related transforms
		   if(ArrayUtils.contains(inputtransform, "susp_unwrapIfElse") &&
	                  ArrayUtils.contains(inputtransform, "susp_wrapsIfElse_elsenotnull")) {
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_unwrapIfElse");
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrapsIfElse_elsenotnull");
		   }
		   
		   if(ArrayUtils.contains(inputtransform, "susp_unwrapIfElse") &&
	               ArrayUtils.contains(inputtransform, "wrapsIfElse_NULL")) {
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_unwrapIfElse");
			   inputtransform = ArrayUtils.removeElement(inputtransform, "wrapsIfElse_NULL");
		   }
		   
		   if(ArrayUtils.contains(inputtransform, "susp_unwrapIfElse_elsenotnull") &&
	               ArrayUtils.contains(inputtransform, "susp_unwrapIfElse_elsenull")) {
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_unwrapIfElse_elsenotnull");
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_unwrapIfElse_elsenull");
		   }
		   
		   if(ArrayUtils.contains(inputtransform, "susp_unwrapIfElse_elsenull") &&
	               ArrayUtils.contains(inputtransform, "susp_wrapsIfElse_elsenotnull")) {
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_unwrapIfElse_elsenull");
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrapsIfElse_elsenotnull");
		   }
		   
		   // post-process a few unnormal susp_wrapsTryCatch and wrapsIf
		   if(ArrayUtils.contains(inputtransform, "susp_wrapsTryCatch") &&
	               ArrayUtils.contains(inputtransform, "wrapsIf_NULL")) {
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrapsTryCatch");
		   }
		   
		   if(ArrayUtils.contains(inputtransform, "susp_wrapsTryCatch") &&
	               ArrayUtils.contains(inputtransform, "susp_wrapsIfElse_elsenotnull")) {
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrapsTryCatch");
		   }
		   
		   if(ArrayUtils.contains(inputtransform, "susp_wrapsTryCatch") &&
	               ArrayUtils.contains(inputtransform, "susp_unwrapIfElse_elsenotnull")) {
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_unwrapIfElse_elsenotnull");
		   }
		   
		   if(ArrayUtils.contains(inputtransform, "susp_wrapsTryCatch") &&
	               ArrayUtils.contains(inputtransform, "susp_unwrapIfElse_elsenull")) {
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_unwrapIfElse_elsenull");
		   }
		   
		   // post-process a few remaining unnormal susp_wrapsTryCatch and susp_wrapsLoop 
		   if(ArrayUtils.contains(inputtransform, "susp_wrapsTryCatch") &&
	               ArrayUtils.contains(inputtransform, "susp_wrapsLoop")) {
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrapsLoop");
		   }
		   
		   // post-process a few remaining unnormal susp_wrapsIfElse_elsenotnull and susp_addassignment 
		   
		   if(ArrayUtils.contains(inputtransform, "susp_wrapsIfElse_elsenotnull") &&
	               ArrayUtils.contains(inputtransform, "susp_addassignment")) {
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_addassignment");
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrapsIfElse_elsenotnull");
		   }
		   
		   if(ArrayUtils.contains(inputtransform, "susp_wrapsIf_elsenull") &&
	               ArrayUtils.contains(inputtransform, "susp_addassignment")) {
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_addassignment");
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrapsIf_elsenull");
		   }
		   
		   if(ArrayUtils.contains(inputtransform, "susp_wrapsMethod_VarRead_Invocation") &&
	               ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_VariableRead_Added_ConstructorCall")) {
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrapsMethod_VarRead_Invocation");
		   }
		   
		   if(ArrayUtils.contains(inputtransform, "susp_wrapsMethod_VarRead_Constructor") &&
	               ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_VariableRead_Added_FieldRead")) {
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrapsMethod_VarRead_Constructor");
		   }
		   
		   if(ArrayUtils.contains(inputtransform, "susp_wrapsMethod_VarRead_Invocation") &&
	               ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_VariableRead_Added_FieldRead")) {
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongVarRef_Removed_VariableRead_Added_FieldRead");
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrapsMethod_VarRead_Invocation");
		   }
		   
		   if(ArrayUtils.contains(inputtransform, "susp_unwrapIfElse_elsenotnull") &&
	               ArrayUtils.contains(inputtransform, "susp_wrapsLoop")) {
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_unwrapIfElse_elsenotnull");
		   }
		   
		   if(ArrayUtils.contains(inputtransform, "susp_wrapsIf_elsenull") &&
	               ArrayUtils.contains(inputtransform, "susp_wrapsLoop")) {
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrapsIf_elsenull");
		   }
		   
		   if(ArrayUtils.contains(inputtransform, "susp_wrapsIfElse_elsenotnull") &&
	               ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_VariableRead_Added_Invocation")) {
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongVarRef_Removed_VariableRead_Added_Invocation");
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrapsIfElse_elsenotnull");
		   }
		   
		   if(ArrayUtils.contains(inputtransform, "susp_wrapsIfElse_elsenotnull") &&
	               ArrayUtils.contains(inputtransform, "susp_wrongMethodRef_Removed_Invocation_Added_ConstructorCall")) {
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongMethodRef_Removed_Invocation_Added_ConstructorCall");
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrapsIfElse_elsenotnull");
		   }
		   
		   if(ArrayUtils.contains(inputtransform, "susp_wrapsIfElse_elsenotnull") &&
	               ArrayUtils.contains(inputtransform, "susp_wrongMethodRef_Removed_ConstructorCall_Added_Invocation")) {
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongMethodRef_Removed_ConstructorCall_Added_Invocation");
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrapsIfElse_elsenotnull");
		   }
		   
		   // post-process superaccess
		   if(ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_SuperAccess_Added_VariableRead")) {
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongVarRef_Removed_SuperAccess_Added_VariableRead");
		   }
		   
		   if(ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_SuperAccess_Added_FieldRead")) {
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongVarRef_Removed_SuperAccess_Added_FieldRead");
		   }
		   
		   if(ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_SuperAccess_Added_TypeAccess")) {
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongVarRef_Removed_SuperAccess_Added_TypeAccess");
		   }
		   
		   if(ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_SuperAccess_Added_FieldRead")) {
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongVarRef_Removed_SuperAccess_Added_FieldRead");
		   }
		   
		   if(ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_VariableRead_Added_FieldWrite")) {
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongVarRef_Removed_VariableRead_Added_FieldWrite");
		   }
		   
		   if(ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_FieldRead_Added_FieldWrite")) {
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongVarRef_Removed_FieldRead_Added_FieldWrite");
		   }
		   
		   if(ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_TypeAccess_Added_TypeAccess")) {
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongVarRef_Removed_TypeAccess_Added_TypeAccess");
		   }
		   
		   if(ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_FieldRead_Added_VariableWrite")) {
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongVarRef_Removed_FieldRead_Added_VariableWrite");
		   }
		   
		   if(ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_FieldRead_Added_VariableWrite")) {
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongVarRef_Removed_FieldRead_Added_VariableWrite");
		   }
		   
		   if(ArrayUtils.contains(inputtransform, "susp_wrongMethodRef_Removed_NewClass_Added_ConstructorCall")) {
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongMethodRef_Removed_NewClass_Added_ConstructorCall");
		   }
		   
		   if(ArrayUtils.contains(inputtransform, "susp_wrongMethodRef_Removed_NewClass_Added_FieldRead")) {
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongMethodRef_Removed_NewClass_Added_FieldRead");
		   }
		   
		   if(ArrayUtils.contains(inputtransform, "susp_wrongMethodRef_Removed_NewClass_Added_Invocation")) {
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongMethodRef_Removed_NewClass_Added_Invocation");
		   }
		   
		   if(ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_VariableWrite_Added_VariableRead")) {
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongVarRef_Removed_VariableWrite_Added_VariableRead");
		   }
		   
		   //uniform different sub-patterns to the super pattern
		   //start var replace by var
		   if(ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Update_VariableRead")) {
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongVarRef_Update_VariableRead");
			   inputtransform = add(inputtransform, "VAR_RW_VAR");
		   }
		   
		   if(ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Update_FieldRead")) {
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongVarRef_Update_FieldRead");
			   inputtransform = add(inputtransform, "VAR_RW_VAR");
		   }
		   
		   if(ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Update_TypeAccess")) {
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongVarRef_Update_TypeAccess");
			   inputtransform = add(inputtransform, "VAR_RW_VAR");
		   }
		   
		   if(ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_FieldRead_Added_VariableRead")) {
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongVarRef_Removed_FieldRead_Added_VariableRead");
			   inputtransform = add(inputtransform, "VAR_RW_VAR");
		   }
		   
		   if(ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_VariableRead_Added_FieldRead")) {
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongVarRef_Removed_VariableRead_Added_FieldRead");
			   inputtransform = add(inputtransform, "VAR_RW_VAR");
		   }
		   
		   if(ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Update_FieldWrite")) {
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongVarRef_Update_FieldWrite");
			   inputtransform = add(inputtransform, "VAR_RW_VAR");
		   }
		   
		   if(ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Update_VariableWrite")) {
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongVarRef_Update_VariableWrite");
			   inputtransform = add(inputtransform, "VAR_RW_VAR");
		   }
		   
		   if(ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_VariableRead_Added_Literal")) {
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongVarRef_Removed_VariableRead_Added_Literal");
			   inputtransform = add(inputtransform, "VAR_RW_VAR");
		   }
		   
		   if(ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_FieldRead_Added_Literal")) {
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongVarRef_Removed_FieldRead_Added_Literal");
			   inputtransform = add(inputtransform, "VAR_RW_VAR");
		   }
		   
		   if(ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_VariableRead_Added_VariableRead")) {
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongVarRef_Removed_VariableRead_Added_VariableRead");
			   inputtransform = add(inputtransform, "VAR_RW_VAR");
		   }
		   
		   if(ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_VariableWrite_Added_FieldWrite")) {
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongVarRef_Removed_VariableWrite_Added_FieldWrite");
			   inputtransform = add(inputtransform, "VAR_RW_VAR");
		   }
		   
		   if(ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_TypeAccess_Added_VariableRead")) {
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongVarRef_Removed_TypeAccess_Added_VariableRead");
			   inputtransform = add(inputtransform, "VAR_RW_VAR");
		   }
		   
		   if(ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_FieldRead_Added_TypeAccess")) {
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongVarRef_Removed_FieldRead_Added_TypeAccess");
			   inputtransform = add(inputtransform, "VAR_RW_VAR");
		   }
		   
		   if(ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_TypeAccess_Added_FieldRead")) {
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongVarRef_Removed_TypeAccess_Added_FieldRead");
			   inputtransform = add(inputtransform, "VAR_RW_VAR");
		   }
		   
		   if(ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_VariableRead_Added_TypeAccess")) {
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongVarRef_Removed_VariableRead_Added_TypeAccess");
			   inputtransform = add(inputtransform, "VAR_RW_VAR");
		   }
		   
		   if(ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_FieldWrite_Added_VariableWrite")) {
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongVarRef_Removed_FieldWrite_Added_VariableWrite");
			   inputtransform = add(inputtransform, "VAR_RW_VAR");
		   }
		   
		   if(ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_FieldRead_Added_FieldRead")) {
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongVarRef_Removed_FieldRead_Added_FieldRead");
			   inputtransform = add(inputtransform, "VAR_RW_VAR");
		   }
		   
		   if(ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_TypeAccess_Added_TypeAccess")) {
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongVarRef_Removed_TypeAccess_Added_TypeAccess");
			   inputtransform = add(inputtransform, "VAR_RW_VAR");
		   }
		   
		   if(ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_SuperAccess_Added_FieldRead")) {
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongVarRef_Removed_SuperAccess_Added_FieldRead");
			   inputtransform = add(inputtransform, "VAR_RW_VAR");
		   }
		   
		   if(ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_VariableWrite_Added_VariableRead")) {
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongVarRef_Removed_VariableWrite_Added_VariableRead");
			 //  inputtransform = add(inputtransform, "VAR_RW_VAR");
		   }
		   
		   if(ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_VariableRead_Added_VariableWrite")) {
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongVarRef_Removed_VariableRead_Added_VariableWrite");
			  // inputtransform = add(inputtransform, "VAR_RW_VAR");
		   }
		   
		   if(ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_VariableRead_Added_FieldWrite")) {
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongVarRef_Removed_VariableRead_Added_FieldWrite");
			 //  inputtransform = add(inputtransform, "VAR_RW_VAR");
		   }
		   
		   if(ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_TypeAccess_Added_Literal")) {
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongVarRef_Removed_TypeAccess_Added_Literal");
			   inputtransform = add(inputtransform, "VAR_RW_VAR");
		   }
		   // end var replacement by var
		   
		   // start var replacment by method
		   if(ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_FieldRead_Added_Invocation")) {
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongVarRef_Removed_FieldRead_Added_Invocation");
			   inputtransform = add(inputtransform, "VAR_RW_Method");
		   }
		   
		   if(ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_VariableRead_Added_Invocation")) {
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongVarRef_Removed_VariableRead_Added_Invocation");
			   inputtransform = add(inputtransform, "VAR_RW_Method");
		   }
		   
		   if(ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_VariableRead_Added_ConstructorCall")) {
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongVarRef_Removed_VariableRead_Added_ConstructorCall");
			   inputtransform = add(inputtransform, "VAR_RW_Method");
		   }
		   
		   if(ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_TypeAccess_Added_Invocation")) {
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongVarRef_Removed_TypeAccess_Added_Invocation");
			   inputtransform = add(inputtransform, "VAR_RW_Method");
		   }
		   
		   if(ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_TypeAccess_Added_ConstructorCall")) {
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongVarRef_Removed_TypeAccess_Added_ConstructorCall");
			   inputtransform = add(inputtransform, "VAR_RW_Method");
		   }
		   
		   if(ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_FieldRead_Added_ConstructorCall")) {
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongVarRef_Removed_FieldRead_Added_ConstructorCall");
			   inputtransform = add(inputtransform, "VAR_RW_Method");
		   }
		   
		   if(ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_FieldWrite_Added_Invocation")) {
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongVarRef_Removed_FieldWrite_Added_Invocation");
			 //  inputtransform = add(inputtransform, "VAR_RW_Method");
		   }
		   
		   if(ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_VariableWrite_Added_Invocation")) {
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongVarRef_Removed_VariableWrite_Added_Invocation");
			 //  inputtransform = add(inputtransform, "VAR_RW_Method");
		   }
		   // end var replacement by method
		   
		   // start method replace by method 
		   if(ArrayUtils.contains(inputtransform, "susp_wrongMethodRef_SameNamedifferentArgument")) {
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongMethodRef_SameNamedifferentArgument");
			   inputtransform = add(inputtransform, "Method_RW_Method");
		   }
		   
		   if(ArrayUtils.contains(inputtransform, "susp_wrongMethodRef_differentMethodName")) {
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongMethodRef_differentMethodName");
			   inputtransform = add(inputtransform, "Method_RW_Method");
		   }
		   
		   if(ArrayUtils.contains(inputtransform, "susp_wrongMethodRef_Removed_ConstructorCall_Added_Invocation")) {
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongMethodRef_Removed_ConstructorCall_Added_Invocation");
			   inputtransform = add(inputtransform, "Method_RW_Method");
		   }
		   
		   if(ArrayUtils.contains(inputtransform, "susp_wrongMethodRef_Removed_Invocation_Added_Invocation")) {
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongMethodRef_Removed_Invocation_Added_Invocation");
			   inputtransform = add(inputtransform, "Method_RW_Method");
		   }
		   
		   if(ArrayUtils.contains(inputtransform, "susp_wrongMethodRef_Removed_Invocation_Added_ConstructorCall")) {
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongMethodRef_Removed_Invocation_Added_ConstructorCall");
			   inputtransform = add(inputtransform, "Method_RW_Method");
		   }
		   
		   if(ArrayUtils.contains(inputtransform, "susp_wrongMethodRef_Removed_ConstructorCall_Added_ConstructorCall")) {
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongMethodRef_Removed_ConstructorCall_Added_ConstructorCall");
			   inputtransform = add(inputtransform, "Method_RW_Method");
		   }
		   
		   if(ArrayUtils.contains(inputtransform, "susp_wrongMethodRef_Removed_NewClass_Added_Invocation")) {
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongMethodRef_Removed_NewClass_Added_Invocation");
			 //  inputtransform = add(inputtransform, "Method_RW_Method");
		   }   
		   // end method replace by method 

		   //start method replace by var
		   if(ArrayUtils.contains(inputtransform, "susp_wrongMethodRef_Removed_Invocation_Added_VariableRead")) {
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongMethodRef_Removed_Invocation_Added_VariableRead");
			   inputtransform = add(inputtransform, "Method_RW_Var");
		   }
		   
		   if(ArrayUtils.contains(inputtransform, "susp_wrongMethodRef_Removed_Invocation_Added_FieldRead")) {
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongMethodRef_Removed_Invocation_Added_FieldRead");
			   inputtransform = add(inputtransform, "Method_RW_Var");
		   }
		   
		   if(ArrayUtils.contains(inputtransform, "susp_wrongMethodRef_Removed_ConstructorCall_Added_VariableRead")) {
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongMethodRef_Removed_ConstructorCall_Added_VariableRead");
			   inputtransform = add(inputtransform, "Method_RW_Var");
		   }
		   
		   if(ArrayUtils.contains(inputtransform, "susp_wrongMethodRef_Removed_Invocation_Added_Literal")) {
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongMethodRef_Removed_Invocation_Added_Literal");
			   inputtransform = add(inputtransform, "Method_RW_Var");
		   }
		   
		   if(ArrayUtils.contains(inputtransform, "susp_wrongMethodRef_Removed_ConstructorCall_Added_FieldRead")) {
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongMethodRef_Removed_ConstructorCall_Added_FieldRead");
			   inputtransform = add(inputtransform, "Method_RW_Var");
		   }
		   
		   if(ArrayUtils.contains(inputtransform, "susp_wrongMethodRef_Removed_ConstructorCall_Added_Literal")) {
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongMethodRef_Removed_ConstructorCall_Added_Literal");
			   inputtransform = add(inputtransform, "Method_RW_Var");
		   }
		   
		   if(ArrayUtils.contains(inputtransform, "susp_wrongMethodRef_Removed_NewClass_Added_FieldRead")) {
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongMethodRef_Removed_NewClass_Added_FieldRead");
			 //  inputtransform = add(inputtransform, "Method_RW_Var");
		   }
		   
		   if(ArrayUtils.contains(inputtransform, "susp_wrongMethodRef_Removed_ConstructorCall_Added_TypeAccess")) {
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongMethodRef_Removed_ConstructorCall_Added_TypeAccess");
			   inputtransform = add(inputtransform, "Method_RW_Var");
		   }
		   
		   if(ArrayUtils.contains(inputtransform, "susp_wrongMethodRef_Removed_Invocation_Added_TypeAccess")) {
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongMethodRef_Removed_Invocation_Added_TypeAccess");
			   inputtransform = add(inputtransform, "Method_RW_Var");
		   }
		   
		   if(ArrayUtils.contains(inputtransform, "susp_wrongMethodRef_Removed_ConstructorCall_Added_VariableWrite")) {
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongMethodRef_Removed_ConstructorCall_Added_VariableWrite");
			 //  inputtransform = add(inputtransform, "Method_RW_Var");
		   }
		   //end method replace by var
		   
		   
		   // additional postprocessing to remove some unnormal transforms associated with nodes
		   
		   if(ArrayUtils.contains(inputtransform, "susp_missNullCheckP") 
				   && inputtransform.length>=2) {
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_missNullCheckP");
		   }
		   
		   if(ArrayUtils.contains(inputtransform, "susp_missNullCheckN") 
				   && inputtransform.length>=2) {
			   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_missNullCheckN");
		   }
		   
		   
		   List<String> transformtoremove = new ArrayList<String>();

		   for (int index=0; index<inputtransform.length; index++) {
			   
			   String tostudy=inputtransform[index];
			   
			   if(tostudy.indexOf("susp_wrongVarRef")!=-1)
				   transformtoremove.add(tostudy);
			   
			   if(tostudy.indexOf("susp_wrongMethodRef")!=-1)
				   transformtoremove.add(tostudy);
		   }
		   
		   for(int index=0; index<transformtoremove.size(); index++) {
			   if(ArrayUtils.contains(inputtransform, transformtoremove.get(index))) {
				   inputtransform = ArrayUtils.removeElement(inputtransform, transformtoremove.get(index));
			   }
		   }
		   
		   return inputtransform;  
	}

	public String[] add(String[] originalArray, String newItem) {
		int currentSize = originalArray.length;
		int newSize = currentSize + 1;
		String[] tempArray = new String[newSize];
		for (int i = 0; i < currentSize; i++) {
			tempArray[i] = originalArray[i];
		}
		tempArray[newSize - 1] = newItem;
		return tempArray;
	}

	public void getNodeCharacterInfo(NodeAST node, JSONObject context, String name, CRFWithConstraintNode crf) {

		if (node.getNodeType().equals("VIRTUALROOT"))
			getCharacterVirtualRoot(node, context, crf);
		else if (node.getNodeType().equals("VariableRead") || node.getNodeType().equals("VariableWrite")
				|| node.getNodeType().equals("FieldRead") || node.getNodeType().equals("FieldWrite"))
			getCharacterVarable(node, context, name, crf);
		else if (node.getNodeType().equals("Invocation"))
			getCharacterInvocation(node, context, name, crf);
		else if (node.getNodeType().equals("Literal"))
			getCharacterConstant(node, context, name, crf);
		else if (node.getNodeType().equals("TypeAccess"))
			getCharacterTypeAccess(node, context, name, crf);
		else if (node.getNodeType().equals("ConstructorCall"))
			getCharacterConstructorCall(node, context, name, crf);
		else if (node.getNodeType().equals("BinaryOperator"))
			getCharacterBinaryOperator(node, context, node.getBinaryOperatorIdentity(), crf);
		else {
		}
		
		if(!node.getLogicalExpressionIdentity().isEmpty()) {
			getCharacterLogicExper(node, context, crf, node.getLogicalExpressionIdentity());
		}
	}
	
	public void getCharacterBinaryOperator (NodeAST node, JSONObject context, String operatoridentity, CRFWithConstraintNode crf) {

		for (CharacteristicBinaryOperator cha : CharacteristicBinaryOperator.values()) {
			String charactername = cha.toString();
			try {
				JSONObject binoperatorfeatureall = (JSONObject) context.get("FEATURES_BINARYOPERATOR");
				JSONObject binoperatorfeaturespecific = (JSONObject) binoperatorfeatureall.get(operatoridentity);
				Boolean charactervalue = Boolean.valueOf(binoperatorfeaturespecific.get(charactername).toString());
				node.charactervaluemap.put(crf.getCharacterIndex(charactername), charactervalue);
			} catch (Exception e) {
				node.charactervaluemap.put(crf.getCharacterIndex(charactername), false);
			}
		}
	}
	
	public void getCharacterLogicExper (NodeAST node, JSONObject context, CRFWithConstraintNode crf, 
			String logicalexperidentity) {

		for (CharacteristicLogExp cha : CharacteristicLogExp.values()) {
			String charactername = cha.toString();
			try {
				JSONObject logicalexperfeatureall = (JSONObject) context.get("FEATURES_LOGICAL_EXPRESSION");
				JSONObject logicalexperfeaturespecific = (JSONObject) logicalexperfeatureall.get(logicalexperidentity);
				Boolean charactervalue = Boolean.valueOf(logicalexperfeaturespecific.get(charactername).toString());
				node.charactervaluemap.put(crf.getCharacterIndex(charactername), charactervalue);
			} catch (Exception e) {
				node.charactervaluemap.put(crf.getCharacterIndex(charactername), false);
			}
		}
	}

	public void getCharacterConstructorCall(NodeAST node, JSONObject context, String callname,
			CRFWithConstraintNode crf) {

		for (CharacteristicConstructor cha : CharacteristicConstructor.values()) {
			String charactername = cha.toString();
			try {
				JSONObject constructorfeatureall = (JSONObject) context.get("FEATURES_CONSTRUCTOR");
				JSONObject constructorfeaturespecific = (JSONObject) constructorfeatureall.get(callname);
				Boolean charactervalue = Boolean.valueOf(constructorfeaturespecific.get(charactername).toString());
				node.charactervaluemap.put(crf.getCharacterIndex(charactername), charactervalue);
			} catch (Exception e) {
				node.charactervaluemap.put(crf.getCharacterIndex(charactername), false);
			}
		}
	}

	public void getCharacterTypeAccess(NodeAST node, JSONObject context, String typeaccessname,
			CRFWithConstraintNode crf) {

		for (CharacteristicTypeAccess cha : CharacteristicTypeAccess.values()) {
			String charactername = cha.toString();
			try {
				JSONObject typeaccessfeatureall = (JSONObject) context.get("FEATURES_TYPEACCESS");
				JSONObject typeaccessfeaturespecific = (JSONObject) typeaccessfeatureall.get(typeaccessname);
				Boolean charactervalue = Boolean.valueOf(typeaccessfeaturespecific.get(charactername).toString());
				node.charactervaluemap.put(crf.getCharacterIndex(charactername), charactervalue);
			} catch (Exception e) {
				node.charactervaluemap.put(crf.getCharacterIndex(charactername), false);
			}
		}
	}

	public void getCharacterConstant(NodeAST node, JSONObject context, String constantname, CRFWithConstraintNode crf) {

		for (CharacteristicConstant cha : CharacteristicConstant.values()) {
			String charactername = cha.toString();
			try {
				JSONObject constantfeatureall = (JSONObject) context.get("CONSTANT");
				JSONObject constantspecific = (JSONObject) constantfeatureall.get(constantname);
				Boolean charactervalue = Boolean.valueOf(constantspecific.get(charactername).toString());
				node.charactervaluemap.put(crf.getCharacterIndex(charactername), charactervalue);
			} catch (Exception e) {
				node.charactervaluemap.put(crf.getCharacterIndex(charactername), false);
			}
		}
	}

	public void getCharacterVirtualRoot(NodeAST node, JSONObject context, CRFWithConstraintNode crf) {

		for (CharacteristicStat cha : CharacteristicStat.values()) {
			String charactername = cha.toString();
			try {
				 Boolean charactervalue = Boolean.valueOf(context.get(charactername).toString());
				 node.charactervaluemap.put(crf.getCharacterIndex(charactername), charactervalue);
			} catch (Exception e) {
				 node.charactervaluemap.put(crf.getCharacterIndex(charactername), false);
			}
		}
		
		String statementtype = "";
		String statementparenttype = "";
		String statementtypebefore1 = "";
		String statementtypebefore2 = "";
        String statementtypebefore3 = "";
        String statementtypeafter1 = "";
        String statementtypeafter2 = "";
        String statementtypeafter3 = "";
		
		try {	
			statementtype = context.get("S3_TYPE_OF_FAULTY_STATEMENT").toString();
			statementparenttype = context.get("S14_TYPE_OF_FAULTY_STATEMENT_PARENT").toString();
			statementtypebefore1 = context.get("S13_TYPE_OF_FAULTY_STATEMENT_BEFORE_1").toString();
			statementtypebefore2 = context.get("S13_TYPE_OF_FAULTY_STATEMENT_BEFORE_2").toString();
			statementtypebefore3 = context.get("S13_TYPE_OF_FAULTY_STATEMENT_BEFORE_3").toString();
			statementtypeafter1 = context.get("S13_TYPE_OF_FAULTY_STATEMENT_AFTER_1").toString();
			statementtypeafter2 = context.get("S13_TYPE_OF_FAULTY_STATEMENT_AFTER_2").toString();
			statementtypeafter3 = context.get("S13_TYPE_OF_FAULTY_STATEMENT_AFTER_3").toString();

			if(!statementtype.isEmpty() && crf.getCharacterIndex("S3_IS_" + statementtype)!=-1)
			    node.charactervaluemap.put(crf.getCharacterIndex("S3_IS_" + statementtype), true);
			
			if(!statementparenttype.isEmpty() && crf.getCharacterIndex("S14_IS_" + statementparenttype)!=-1)
			    node.charactervaluemap.put(crf.getCharacterIndex("S14_IS_" + statementparenttype), true);
			
			if(!statementtypebefore1.isEmpty() && crf.getCharacterIndex("S13_Before_HAS_" + statementtypebefore1)!=-1)
			    node.charactervaluemap.put(crf.getCharacterIndex("S13_Before_HAS_" + statementtypebefore1), true);
			
			if(!statementtypebefore2.isEmpty() && crf.getCharacterIndex("S13_Before_HAS_" + statementtypebefore2)!=-1)
			    node.charactervaluemap.put(crf.getCharacterIndex("S13_Before_HAS_" + statementtypebefore2), true);
			
            if(!statementtypebefore3.isEmpty() && crf.getCharacterIndex("S13_Before_HAS_" + statementtypebefore3)!=-1)
			    node.charactervaluemap.put(crf.getCharacterIndex("S13_Before_HAS_" + statementtypebefore3), true);
            
            if(!statementtypeafter1.isEmpty() && crf.getCharacterIndex("S13_After_HAS_" + statementtypeafter1)!=-1)
			    node.charactervaluemap.put(crf.getCharacterIndex("S13_After_HAS_" + statementtypeafter1), true);
            
            if(!statementtypeafter2.isEmpty() && crf.getCharacterIndex("S13_After_HAS_" + statementtypeafter2)!=-1)
			    node.charactervaluemap.put(crf.getCharacterIndex("S13_After_HAS_" + statementtypeafter2), true);
            
            if(!statementtypeafter3.isEmpty() && crf.getCharacterIndex("S13_After_HAS_" + statementtypeafter3)!=-1)
			    node.charactervaluemap.put(crf.getCharacterIndex("S13_After_HAS_" + statementtypeafter3), true);
            
            if(!statementtype.isEmpty() && !statementparenttype.isEmpty())  {
            	if(crf.getCharacterIndex("S3_IS_" + statementtype + 
			    		"_S14_IS_" + statementparenttype)!=-1) {
			        node.charactervaluemap.put(crf.getCharacterIndex("S3_IS_" + statementtype + 
			    		"_S14_IS_" + statementparenttype), true);
            	}
            }
            
            if(!statementtype.isEmpty() && !statementtypebefore1.isEmpty()) {
            	if(crf.getCharacterIndex("S3_IS_" + statementtype + 
			    		"_S13_Before_HAS_" + statementtypebefore1)!=-1) {
			        node.charactervaluemap.put(crf.getCharacterIndex("S3_IS_" + statementtype + 
			    		"_S13_Before_HAS_" + statementtypebefore1), true);
            	}
            }

            if(!statementtype.isEmpty() && !statementtypebefore2.isEmpty()) {
            	if(crf.getCharacterIndex("S3_IS_" + statementtype + 
			    		"_S13_Before_HAS_" + statementtypebefore2)!=-1) {
			        node.charactervaluemap.put(crf.getCharacterIndex("S3_IS_" + statementtype + 
			    		"_S13_Before_HAS_" + statementtypebefore2), true);
            	}
            }
            
            if(!statementtype.isEmpty() && !statementtypebefore3.isEmpty()) {
            	if(crf.getCharacterIndex("S3_IS_" + statementtype + 
			    		"_S13_Before_HAS_" + statementtypebefore3)!=-1) {
			        node.charactervaluemap.put(crf.getCharacterIndex("S3_IS_" + statementtype + 
			    		"_S13_Before_HAS_" + statementtypebefore3), true);
            	}
            }
            
            if(!statementtype.isEmpty() && !statementtypeafter1.isEmpty()) {
            	if(crf.getCharacterIndex("S3_IS_" + statementtype + 
			    		"_S13_After_HAS_" + statementtypeafter1)!=-1) {
			        node.charactervaluemap.put(crf.getCharacterIndex("S3_IS_" + statementtype + 
			    		"_S13_After_HAS_" + statementtypeafter1), true);
            	}
            }

            if(!statementtype.isEmpty() && !statementtypeafter2.isEmpty()) {
            	if(crf.getCharacterIndex("S3_IS_" + statementtype + 
			    		"_S13_After_HAS_" + statementtypeafter2)!=-1) {
			       node.charactervaluemap.put(crf.getCharacterIndex("S3_IS_" + statementtype + 
			    		"_S13_After_HAS_" + statementtypeafter2), true);
            	}
            }
            
            if(!statementtype.isEmpty() && !statementtypeafter3.isEmpty()) {
            	if(crf.getCharacterIndex("S3_IS_" + statementtype + 
			    		"_S13_After_HAS_" + statementtypeafter3)!=-1) {
			       node.charactervaluemap.put(crf.getCharacterIndex("S3_IS_" + statementtype + 
			    		"_S13_After_HAS_" + statementtypeafter3), true);
            	}
            }
            
		} catch (Exception e) {
			// 
		}
	}

	public void getCharacterVarable(NodeAST node, JSONObject context, String variablename, CRFWithConstraintNode crf) {

		for (CharacteristicVar cha : CharacteristicVar.values()) {
			String charactername = cha.toString();
			try {
				if(!charactername.equals("V4B_USED_MULTIPLE_AS_PARAMETER")) {
				   JSONObject varfeatureall = (JSONObject) context.get("FEATURES_VARS");
				   JSONObject varfeaturespecific = (JSONObject) varfeatureall.get(variablename);
				   Boolean charactervalue = Boolean.valueOf(varfeaturespecific.get(charactername).toString());
				   node.charactervaluemap.put(crf.getCharacterIndex(charactername), charactervalue);
				}
			} catch (Exception e) {
				node.charactervaluemap.put(crf.getCharacterIndex(charactername), false);
			}
		}
	}

	public void getCharacterInvocation(NodeAST node, JSONObject context, String methodname, CRFWithConstraintNode crf) {

		for (CharacteristicMethod cha : CharacteristicMethod.values()) {
			String charactername = cha.toString();
			try {
				JSONObject methodfeatureall = (JSONObject) context.get("FEATURES_METHODS");
				JSONObject methodfeaturespecific = (JSONObject) methodfeatureall.get(methodname);
				Boolean charactervalue = Boolean.valueOf(methodfeaturespecific.get(charactername).toString());
				node.charactervaluemap.put(crf.getCharacterIndex(charactername), charactervalue);
			} catch (Exception e) {
				node.charactervaluemap.put(crf.getCharacterIndex(charactername), false);
			}
		}
	}

//	public boolean deterWhetherRootLogical(NodeAST node, String noderetuntype) {
//		if (!noderetuntype.toLowerCase().equals("boolean") && !noderetuntype.toLowerCase().equals("java.lang.boolean"))
//			return false;
//		else {
//			NodeAST parent = node;
//			while (parent.getParentNode() != null) {
//				if (parent.getParentNode().getIsBooleanType())
//					return false;
//				parent = (NodeAST) parent.getParentNode();
//			}
//			return true;
//		}
//	}
}
