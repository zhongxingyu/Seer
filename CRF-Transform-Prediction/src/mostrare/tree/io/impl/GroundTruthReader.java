package mostrare.tree.io.impl;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import mostrare.crf.tree.VirtualTransforms;
import mostrare.crf.tree.impl.CRFWithConstraintNode;
import mostrare.tree.Node;
import mostrare.tree.impl.NodeAST;
import mostrare.tree.impl.TreeAST;
import mostrare.tree.io.TreeToAnnotationReader;

public class GroundTruthReader implements TreeToAnnotationReader {
	private static TreeToAnnotationReader instance = null;

	static {
		instance = new GroundTruthReader();
	}

	private GroundTruthReader() {

	}

	public static TreeToAnnotationReader getInstance() {
		return instance;
	}

	@Override
	public TreeAST readTree(File file, CRFWithConstraintNode crf) {
		ArrayList<TreeAST> trainingtrees = new ArrayList<TreeAST>();
		JSONParser parser = new JSONParser();

		try {
				Object obj = parser.parse(new FileReader(file.getAbsolutePath()));
				JSONObject jsonObject = (JSONObject) obj;


							JSONArray astList = (JSONArray) (jsonObject.get("faulty_ast"));

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
								virtualroot.setParentNode(null);
								virtualroot.setLogicalExpressionIdentity("");
								virtualroot.setExpressionIdentity("");
								virtualroot.setBinaryOperatorIdentity("");
								virtualroot.setNameofNode("DEFAULT");
								virtualroot.setTypeofReturn("");

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

								readEachJSONNode(faulty_ast, tree, root, crf);

								for (int i = 0; i < tree.getNodesNumber(); i++) {
									Node toStudy = tree.getNode(i);
									if (i != 0) {
										if (toStudy.getNodeLabel().equals("addassignment")
												|| toStudy.getNodeLabel().equals("wrapsTryCatch")
												|| toStudy.getNodeLabel().equals("wrapsLoop"))
											toStudy.setNodeLabel("EMPTY");
									}
								}

								if (!checkWhetherEmptyTree(tree))
									trainingtrees.add(tree);
							}
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (trainingtrees.size() > 0)
			return trainingtrees.get(0);
		else
			return null;
	}

	public boolean checkWhetherEmptyTree(TreeAST inputtree) {

		boolean emptytree = true;
		for (int i = 0; i < inputtree.getNodesNumber(); i++) {
			Node toStudy = inputtree.getNode(i);
			if (!toStudy.getNodeLabel().equals("EMPTY")) {
				emptytree = false;
				break;
			}
		}
		return emptytree;
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

	public void readEachJSONNode(JSONObject faulty_node, TreeAST ast, NodeAST parentNode, CRFWithConstraintNode crf) {

		JSONArray childrenList = (JSONArray) (faulty_node.get("children"));
		ArrayList<NodeAST> parentnodelist = new ArrayList<NodeAST>();
		parentnodelist.clear();

		if (childrenList.size() > 0) {
			for (int index = 0; index < childrenList.size(); index++) {

				JSONObject jsonObject = (JSONObject) (childrenList.get(index));
				String nodetype = jsonObject.get("type").toString();
				JSONArray actionListinner = (JSONArray) jsonObject.get("susp");
				String namefornode = jsonObject.get("label").toString();
				
				String logicalExpIdentity = "";
				try {
					logicalExpIdentity = jsonObject.get("index_of_logical_exper").toString();
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

			}

			for (int index = 0; index < childrenList.size(); index++) {
				NodeAST parentnode = parentnodelist.get(index);
				JSONObject innernode = ((JSONObject) (childrenList.get(index)));
				readEachJSONNode(innernode, ast, parentnode, crf);
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
		if (ArrayUtils.contains(inputtransform, "susp_unwrapMethod")
				&& ArrayUtils.contains(inputtransform, "susp_wrapsTryCatch")) {
			inputtransform = ArrayUtils.removeElement(inputtransform, "susp_unwrapMethod");
		}

		if (ArrayUtils.contains(inputtransform, "susp_unwrapMethod")
				&& ArrayUtils.contains(inputtransform, "susp_wrapsIfElse_elsenotnull")) {
			inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrapsIfElse_elsenotnull");
		}

		if (ArrayUtils.contains(inputtransform, "susp_unwrapMethod")
				&& ArrayUtils.contains(inputtransform, "susp_wrapsLoop")) {
			inputtransform = ArrayUtils.removeElement(inputtransform, "susp_unwrapMethod");
			inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrapsLoop");
		}

		if (ArrayUtils.contains(inputtransform, "susp_unwrapMethod")
				&& ArrayUtils.contains(inputtransform, "susp_wrongMethodRef_Removed_Invocation_Added_FieldRead")) {
			inputtransform = ArrayUtils.removeElement(inputtransform,
					"susp_wrongMethodRef_Removed_Invocation_Added_FieldRead");
		}

		// pot-process null-check related transforms
		if (ArrayUtils.contains(inputtransform, "susp_missNullCheckP")
				&& ArrayUtils.contains(inputtransform, "susp_wrapsIf_elsenull")) {
			inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrapsIf_elsenull");
			inputtransform = ArrayUtils.removeElement(inputtransform, "susp_missNullCheckP");
			inputtransform = add(inputtransform, "wrapsIf_NULL");
		}

		if (ArrayUtils.contains(inputtransform, "susp_missNullCheckN")
				&& ArrayUtils.contains(inputtransform, "susp_wrapsIf_elsenull")) {
			inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrapsIf_elsenull");
			inputtransform = ArrayUtils.removeElement(inputtransform, "susp_missNullCheckN");
			inputtransform = add(inputtransform, "wrapsIf_NULL");
		}

		if (ArrayUtils.contains(inputtransform, "susp_missNullCheckP")
				&& ArrayUtils.contains(inputtransform, "susp_wrapsIfElse_elsenotnull")) {
			inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrapsIfElse_elsenotnull");
			inputtransform = ArrayUtils.removeElement(inputtransform, "susp_missNullCheckP");
			inputtransform = add(inputtransform, "wrapsIfElse_NULL");
		}

		if (ArrayUtils.contains(inputtransform, "susp_missNullCheckN")
				&& ArrayUtils.contains(inputtransform, "susp_wrapsIfElse_elsenotnull")) {
			inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrapsIfElse_elsenotnull");
			inputtransform = ArrayUtils.removeElement(inputtransform, "susp_missNullCheckN");
			inputtransform = add(inputtransform, "wrapsIfElse_NULL");
		}

		// post-process a few remaining unnormbal susp_missNullCheckP or
		// susp_missNullCheckN
		if (inputtransform.length == 1 && ArrayUtils.contains(inputtransform, "susp_missNullCheckP"))
			inputtransform = ArrayUtils.removeElement(inputtransform, "susp_missNullCheckP");

		if (inputtransform.length == 1 && ArrayUtils.contains(inputtransform, "susp_missNullCheckN"))
			inputtransform = ArrayUtils.removeElement(inputtransform, "susp_missNullCheckN");

		if (ArrayUtils.contains(inputtransform, "susp_missNullCheckN")
				&& ArrayUtils.contains(inputtransform, "wrapsIf_NULL")) {
			inputtransform = ArrayUtils.removeElement(inputtransform, "susp_missNullCheckN");
		}

		// post0process a few remaining unnorml wrap if and unwrap if related transforms
		if (ArrayUtils.contains(inputtransform, "susp_unwrapIfElse")
				&& ArrayUtils.contains(inputtransform, "susp_wrapsIfElse_elsenotnull")) {
			inputtransform = ArrayUtils.removeElement(inputtransform, "susp_unwrapIfElse");
			inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrapsIfElse_elsenotnull");
		}

		if (ArrayUtils.contains(inputtransform, "susp_unwrapIfElse")
				&& ArrayUtils.contains(inputtransform, "wrapsIfElse_NULL")) {
			inputtransform = ArrayUtils.removeElement(inputtransform, "susp_unwrapIfElse");
			inputtransform = ArrayUtils.removeElement(inputtransform, "wrapsIfElse_NULL");
		}

		if (ArrayUtils.contains(inputtransform, "susp_unwrapIfElse_elsenotnull")
				&& ArrayUtils.contains(inputtransform, "susp_unwrapIfElse_elsenull")) {
			inputtransform = ArrayUtils.removeElement(inputtransform, "susp_unwrapIfElse_elsenotnull");
			inputtransform = ArrayUtils.removeElement(inputtransform, "susp_unwrapIfElse_elsenull");
		}

		if (ArrayUtils.contains(inputtransform, "susp_unwrapIfElse_elsenull")
				&& ArrayUtils.contains(inputtransform, "susp_wrapsIfElse_elsenotnull")) {
			inputtransform = ArrayUtils.removeElement(inputtransform, "susp_unwrapIfElse_elsenull");
			inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrapsIfElse_elsenotnull");
		}

		// post-process a few unnormal susp_wrapsTryCatch and wrapsIf
		if (ArrayUtils.contains(inputtransform, "susp_wrapsTryCatch")
				&& ArrayUtils.contains(inputtransform, "wrapsIf_NULL")) {
			inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrapsTryCatch");
		}

		if (ArrayUtils.contains(inputtransform, "susp_wrapsTryCatch")
				&& ArrayUtils.contains(inputtransform, "susp_wrapsIfElse_elsenotnull")) {
			inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrapsTryCatch");
		}

		if (ArrayUtils.contains(inputtransform, "susp_wrapsTryCatch")
				&& ArrayUtils.contains(inputtransform, "susp_unwrapIfElse_elsenotnull")) {
			inputtransform = ArrayUtils.removeElement(inputtransform, "susp_unwrapIfElse_elsenotnull");
		}

		if (ArrayUtils.contains(inputtransform, "susp_wrapsTryCatch")
				&& ArrayUtils.contains(inputtransform, "susp_unwrapIfElse_elsenull")) {
			inputtransform = ArrayUtils.removeElement(inputtransform, "susp_unwrapIfElse_elsenull");
		}

		// post-process a few remaining unnormal susp_wrapsTryCatch and susp_wrapsLoop
		if (ArrayUtils.contains(inputtransform, "susp_wrapsTryCatch")
				&& ArrayUtils.contains(inputtransform, "susp_wrapsLoop")) {
			inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrapsLoop");
		}

		// post-process a few remaining unnormal susp_wrapsIfElse_elsenotnull and
		// susp_addassignment

		if (ArrayUtils.contains(inputtransform, "susp_wrapsIfElse_elsenotnull")
				&& ArrayUtils.contains(inputtransform, "susp_addassignment")) {
			inputtransform = ArrayUtils.removeElement(inputtransform, "susp_addassignment");
			inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrapsIfElse_elsenotnull");
		}

		if (ArrayUtils.contains(inputtransform, "susp_wrapsIf_elsenull")
				&& ArrayUtils.contains(inputtransform, "susp_addassignment")) {
			inputtransform = ArrayUtils.removeElement(inputtransform, "susp_addassignment");
			inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrapsIf_elsenull");
		}

		if (ArrayUtils.contains(inputtransform, "susp_wrapsMethod_VarRead_Invocation")
				&& ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_VariableRead_Added_ConstructorCall")) {
			inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrapsMethod_VarRead_Invocation");
		}

		if (ArrayUtils.contains(inputtransform, "susp_wrapsMethod_VarRead_Constructor")
				&& ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_VariableRead_Added_FieldRead")) {
			inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrapsMethod_VarRead_Constructor");
		}

		if (ArrayUtils.contains(inputtransform, "susp_wrapsMethod_VarRead_Invocation")
				&& ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_VariableRead_Added_FieldRead")) {
			inputtransform = ArrayUtils.removeElement(inputtransform,
					"susp_wrongVarRef_Removed_VariableRead_Added_FieldRead");
			inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrapsMethod_VarRead_Invocation");
		}

		if (ArrayUtils.contains(inputtransform, "susp_unwrapIfElse_elsenotnull")
				&& ArrayUtils.contains(inputtransform, "susp_wrapsLoop")) {
			inputtransform = ArrayUtils.removeElement(inputtransform, "susp_unwrapIfElse_elsenotnull");
		}

		if (ArrayUtils.contains(inputtransform, "susp_wrapsIf_elsenull")
				&& ArrayUtils.contains(inputtransform, "susp_wrapsLoop")) {
			inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrapsIf_elsenull");
		}

		if (ArrayUtils.contains(inputtransform, "susp_wrapsIfElse_elsenotnull")
				&& ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_VariableRead_Added_Invocation")) {
			inputtransform = ArrayUtils.removeElement(inputtransform,
					"susp_wrongVarRef_Removed_VariableRead_Added_Invocation");
			inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrapsIfElse_elsenotnull");
		}

		if (ArrayUtils.contains(inputtransform, "susp_wrapsIfElse_elsenotnull") && ArrayUtils.contains(inputtransform,
				"susp_wrongMethodRef_Removed_Invocation_Added_ConstructorCall")) {
			inputtransform = ArrayUtils.removeElement(inputtransform,
					"susp_wrongMethodRef_Removed_Invocation_Added_ConstructorCall");
			inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrapsIfElse_elsenotnull");
		}

		if (ArrayUtils.contains(inputtransform, "susp_wrapsIfElse_elsenotnull") && ArrayUtils.contains(inputtransform,
				"susp_wrongMethodRef_Removed_ConstructorCall_Added_Invocation")) {
			inputtransform = ArrayUtils.removeElement(inputtransform,
					"susp_wrongMethodRef_Removed_ConstructorCall_Added_Invocation");
			inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrapsIfElse_elsenotnull");
		}

		// post-process superaccess
		if (ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_SuperAccess_Added_VariableRead")) {
			inputtransform = ArrayUtils.removeElement(inputtransform,
					"susp_wrongVarRef_Removed_SuperAccess_Added_VariableRead");
		}

		if (ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_SuperAccess_Added_FieldRead")) {
			inputtransform = ArrayUtils.removeElement(inputtransform,
					"susp_wrongVarRef_Removed_SuperAccess_Added_FieldRead");
		}

		if (ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_SuperAccess_Added_TypeAccess")) {
			inputtransform = ArrayUtils.removeElement(inputtransform,
					"susp_wrongVarRef_Removed_SuperAccess_Added_TypeAccess");
		}

		if (ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_SuperAccess_Added_FieldRead")) {
			inputtransform = ArrayUtils.removeElement(inputtransform,
					"susp_wrongVarRef_Removed_SuperAccess_Added_FieldRead");
		}

		if (ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_VariableRead_Added_FieldWrite")) {
			inputtransform = ArrayUtils.removeElement(inputtransform,
					"susp_wrongVarRef_Removed_VariableRead_Added_FieldWrite");
		}

		if (ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_FieldRead_Added_FieldWrite")) {
			inputtransform = ArrayUtils.removeElement(inputtransform,
					"susp_wrongVarRef_Removed_FieldRead_Added_FieldWrite");
		}

		if (ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_TypeAccess_Added_TypeAccess")) {
			inputtransform = ArrayUtils.removeElement(inputtransform,
					"susp_wrongVarRef_Removed_TypeAccess_Added_TypeAccess");
		}

		if (ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_FieldRead_Added_VariableWrite")) {
			inputtransform = ArrayUtils.removeElement(inputtransform,
					"susp_wrongVarRef_Removed_FieldRead_Added_VariableWrite");
		}

		if (ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_FieldRead_Added_VariableWrite")) {
			inputtransform = ArrayUtils.removeElement(inputtransform,
					"susp_wrongVarRef_Removed_FieldRead_Added_VariableWrite");
		}

		if (ArrayUtils.contains(inputtransform, "susp_wrongMethodRef_Removed_NewClass_Added_ConstructorCall")) {
			inputtransform = ArrayUtils.removeElement(inputtransform,
					"susp_wrongMethodRef_Removed_NewClass_Added_ConstructorCall");
		}

		if (ArrayUtils.contains(inputtransform, "susp_wrongMethodRef_Removed_NewClass_Added_FieldRead")) {
			inputtransform = ArrayUtils.removeElement(inputtransform,
					"susp_wrongMethodRef_Removed_NewClass_Added_FieldRead");
		}

		if (ArrayUtils.contains(inputtransform, "susp_wrongMethodRef_Removed_NewClass_Added_Invocation")) {
			inputtransform = ArrayUtils.removeElement(inputtransform,
					"susp_wrongMethodRef_Removed_NewClass_Added_Invocation");
		}

		if (ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_VariableWrite_Added_VariableRead")) {
			inputtransform = ArrayUtils.removeElement(inputtransform,
					"susp_wrongVarRef_Removed_VariableWrite_Added_VariableRead");
		}

		// uniform different sub-patterns to the super pattern
		// start var replace by var
		if (ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Update_VariableRead")) {
			inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongVarRef_Update_VariableRead");
			inputtransform = add(inputtransform, "VAR_RW_VAR");
		}

		if (ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Update_FieldRead")) {
			inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongVarRef_Update_FieldRead");
			inputtransform = add(inputtransform, "VAR_RW_VAR");
		}

		if (ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Update_TypeAccess")) {
			inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongVarRef_Update_TypeAccess");
			inputtransform = add(inputtransform, "VAR_RW_VAR");
		}

		if (ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_FieldRead_Added_VariableRead")) {
			inputtransform = ArrayUtils.removeElement(inputtransform,
					"susp_wrongVarRef_Removed_FieldRead_Added_VariableRead");
			inputtransform = add(inputtransform, "VAR_RW_VAR");
		}

		if (ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_VariableRead_Added_FieldRead")) {
			inputtransform = ArrayUtils.removeElement(inputtransform,
					"susp_wrongVarRef_Removed_VariableRead_Added_FieldRead");
			inputtransform = add(inputtransform, "VAR_RW_VAR");
		}

		if (ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Update_FieldWrite")) {
			inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongVarRef_Update_FieldWrite");
			inputtransform = add(inputtransform, "VAR_RW_VAR");
		}

		if (ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Update_VariableWrite")) {
			inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongVarRef_Update_VariableWrite");
			inputtransform = add(inputtransform, "VAR_RW_VAR");
		}

		if (ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_VariableRead_Added_Literal")) {
			inputtransform = ArrayUtils.removeElement(inputtransform,
					"susp_wrongVarRef_Removed_VariableRead_Added_Literal");
			inputtransform = add(inputtransform, "VAR_RW_VAR");
		}

		if (ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_FieldRead_Added_Literal")) {
			inputtransform = ArrayUtils.removeElement(inputtransform,
					"susp_wrongVarRef_Removed_FieldRead_Added_Literal");
			inputtransform = add(inputtransform, "VAR_RW_VAR");
		}

		if (ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_VariableRead_Added_VariableRead")) {
			inputtransform = ArrayUtils.removeElement(inputtransform,
					"susp_wrongVarRef_Removed_VariableRead_Added_VariableRead");
			inputtransform = add(inputtransform, "VAR_RW_VAR");
		}

		if (ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_VariableWrite_Added_FieldWrite")) {
			inputtransform = ArrayUtils.removeElement(inputtransform,
					"susp_wrongVarRef_Removed_VariableWrite_Added_FieldWrite");
			inputtransform = add(inputtransform, "VAR_RW_VAR");
		}

		if (ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_TypeAccess_Added_VariableRead")) {
			inputtransform = ArrayUtils.removeElement(inputtransform,
					"susp_wrongVarRef_Removed_TypeAccess_Added_VariableRead");
			inputtransform = add(inputtransform, "VAR_RW_VAR");
		}

		if (ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_FieldRead_Added_TypeAccess")) {
			inputtransform = ArrayUtils.removeElement(inputtransform,
					"susp_wrongVarRef_Removed_FieldRead_Added_TypeAccess");
			inputtransform = add(inputtransform, "VAR_RW_VAR");
		}

		if (ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_TypeAccess_Added_FieldRead")) {
			inputtransform = ArrayUtils.removeElement(inputtransform,
					"susp_wrongVarRef_Removed_TypeAccess_Added_FieldRead");
			inputtransform = add(inputtransform, "VAR_RW_VAR");
		}

		if (ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_VariableRead_Added_TypeAccess")) {
			inputtransform = ArrayUtils.removeElement(inputtransform,
					"susp_wrongVarRef_Removed_VariableRead_Added_TypeAccess");
			inputtransform = add(inputtransform, "VAR_RW_VAR");
		}

		if (ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_FieldWrite_Added_VariableWrite")) {
			inputtransform = ArrayUtils.removeElement(inputtransform,
					"susp_wrongVarRef_Removed_FieldWrite_Added_VariableWrite");
			inputtransform = add(inputtransform, "VAR_RW_VAR");
		}

		if (ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_FieldRead_Added_FieldRead")) {
			inputtransform = ArrayUtils.removeElement(inputtransform,
					"susp_wrongVarRef_Removed_FieldRead_Added_FieldRead");
			inputtransform = add(inputtransform, "VAR_RW_VAR");
		}

		if (ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_TypeAccess_Added_TypeAccess")) {
			inputtransform = ArrayUtils.removeElement(inputtransform,
					"susp_wrongVarRef_Removed_TypeAccess_Added_TypeAccess");
			inputtransform = add(inputtransform, "VAR_RW_VAR");
		}

		if (ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_SuperAccess_Added_FieldRead")) {
			inputtransform = ArrayUtils.removeElement(inputtransform,
					"susp_wrongVarRef_Removed_SuperAccess_Added_FieldRead");
			inputtransform = add(inputtransform, "VAR_RW_VAR");
		}

		if (ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_VariableWrite_Added_VariableRead")) {
			inputtransform = ArrayUtils.removeElement(inputtransform,
					"susp_wrongVarRef_Removed_VariableWrite_Added_VariableRead");
			inputtransform = add(inputtransform, "VAR_RW_VAR");
		}

		if (ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_VariableRead_Added_VariableWrite")) {
			inputtransform = ArrayUtils.removeElement(inputtransform,
					"susp_wrongVarRef_Removed_VariableRead_Added_VariableWrite");
			inputtransform = add(inputtransform, "VAR_RW_VAR");
		}

		if (ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_VariableRead_Added_FieldWrite")) {
			inputtransform = ArrayUtils.removeElement(inputtransform,
					"susp_wrongVarRef_Removed_VariableRead_Added_FieldWrite");
			inputtransform = add(inputtransform, "VAR_RW_VAR");
		}

		if (ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_TypeAccess_Added_Literal")) {
			inputtransform = ArrayUtils.removeElement(inputtransform,
					"susp_wrongVarRef_Removed_TypeAccess_Added_Literal");
			inputtransform = add(inputtransform, "VAR_RW_VAR");
		}
		// end var replacement by var

		// start var replacment by method
		if (ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_FieldRead_Added_Invocation")) {
			inputtransform = ArrayUtils.removeElement(inputtransform,
					"susp_wrongVarRef_Removed_FieldRead_Added_Invocation");
			inputtransform = add(inputtransform, "VAR_RW_Method");
		}

		if (ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_VariableRead_Added_Invocation")) {
			inputtransform = ArrayUtils.removeElement(inputtransform,
					"susp_wrongVarRef_Removed_VariableRead_Added_Invocation");
			inputtransform = add(inputtransform, "VAR_RW_Method");
		}

		if (ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_VariableRead_Added_ConstructorCall")) {
			inputtransform = ArrayUtils.removeElement(inputtransform,
					"susp_wrongVarRef_Removed_VariableRead_Added_ConstructorCall");
			inputtransform = add(inputtransform, "VAR_RW_Method");
		}

		if (ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_TypeAccess_Added_Invocation")) {
			inputtransform = ArrayUtils.removeElement(inputtransform,
					"susp_wrongVarRef_Removed_TypeAccess_Added_Invocation");
			inputtransform = add(inputtransform, "VAR_RW_Method");
		}

		if (ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_FieldRead_Added_ConstructorCall")) {
			inputtransform = ArrayUtils.removeElement(inputtransform,
					"susp_wrongVarRef_Removed_FieldRead_Added_ConstructorCall");
			inputtransform = add(inputtransform, "VAR_RW_Method");
		}

		if (ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_FieldWrite_Added_Invocation")) {
			inputtransform = ArrayUtils.removeElement(inputtransform,
					"susp_wrongVarRef_Removed_FieldWrite_Added_Invocation");
			inputtransform = add(inputtransform, "VAR_RW_Method");
		}

		if (ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_VariableWrite_Added_Invocation")) {
			inputtransform = ArrayUtils.removeElement(inputtransform,
					"susp_wrongVarRef_Removed_VariableWrite_Added_Invocation");
			inputtransform = add(inputtransform, "VAR_RW_Method");
		}
		// end var replacement by method

		// start method replace by method
		if (ArrayUtils.contains(inputtransform, "susp_wrongMethodRef_SameNamedifferentArgument")) {
			inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongMethodRef_SameNamedifferentArgument");
			inputtransform = add(inputtransform, "Method_RW_Method");
		}

		if (ArrayUtils.contains(inputtransform, "susp_wrongMethodRef_differentMethodName")) {
			inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongMethodRef_differentMethodName");
			inputtransform = add(inputtransform, "Method_RW_Method");
		}

		if (ArrayUtils.contains(inputtransform, "susp_wrongMethodRef_Removed_ConstructorCall_Added_Invocation")) {
			inputtransform = ArrayUtils.removeElement(inputtransform,
					"susp_wrongMethodRef_Removed_ConstructorCall_Added_Invocation");
			inputtransform = add(inputtransform, "Method_RW_Method");
		}

		if (ArrayUtils.contains(inputtransform, "susp_wrongMethodRef_Removed_Invocation_Added_Invocation")) {
			inputtransform = ArrayUtils.removeElement(inputtransform,
					"susp_wrongMethodRef_Removed_Invocation_Added_Invocation");
			inputtransform = add(inputtransform, "Method_RW_Method");
		}

		if (ArrayUtils.contains(inputtransform, "susp_wrongMethodRef_Removed_Invocation_Added_ConstructorCall")) {
			inputtransform = ArrayUtils.removeElement(inputtransform,
					"susp_wrongMethodRef_Removed_Invocation_Added_ConstructorCall");
			inputtransform = add(inputtransform, "Method_RW_Method");
		}

		if (ArrayUtils.contains(inputtransform, "susp_wrongMethodRef_Removed_ConstructorCall_Added_ConstructorCall")) {
			inputtransform = ArrayUtils.removeElement(inputtransform,
					"susp_wrongMethodRef_Removed_ConstructorCall_Added_ConstructorCall");
			inputtransform = add(inputtransform, "Method_RW_Method");
		}

		if (ArrayUtils.contains(inputtransform, "susp_wrongMethodRef_Removed_NewClass_Added_Invocation")) {
			inputtransform = ArrayUtils.removeElement(inputtransform,
					"susp_wrongMethodRef_Removed_NewClass_Added_Invocation");
			inputtransform = add(inputtransform, "Method_RW_Method");
		}
		// end method replace by method

		// start method replace by var
		if (ArrayUtils.contains(inputtransform, "susp_wrongMethodRef_Removed_Invocation_Added_VariableRead")) {
			inputtransform = ArrayUtils.removeElement(inputtransform,
					"susp_wrongMethodRef_Removed_Invocation_Added_VariableRead");
			inputtransform = add(inputtransform, "Method_RW_Var");
		}

		if (ArrayUtils.contains(inputtransform, "susp_wrongMethodRef_Removed_Invocation_Added_FieldRead")) {
			inputtransform = ArrayUtils.removeElement(inputtransform,
					"susp_wrongMethodRef_Removed_Invocation_Added_FieldRead");
			inputtransform = add(inputtransform, "Method_RW_Var");
		}

		if (ArrayUtils.contains(inputtransform, "susp_wrongMethodRef_Removed_ConstructorCall_Added_VariableRead")) {
			inputtransform = ArrayUtils.removeElement(inputtransform,
					"susp_wrongMethodRef_Removed_ConstructorCall_Added_VariableRead");
			inputtransform = add(inputtransform, "Method_RW_Var");
		}

		if (ArrayUtils.contains(inputtransform, "susp_wrongMethodRef_Removed_Invocation_Added_Literal")) {
			inputtransform = ArrayUtils.removeElement(inputtransform,
					"susp_wrongMethodRef_Removed_Invocation_Added_Literal");
			inputtransform = add(inputtransform, "Method_RW_Var");
		}

		if (ArrayUtils.contains(inputtransform, "susp_wrongMethodRef_Removed_ConstructorCall_Added_FieldRead")) {
			inputtransform = ArrayUtils.removeElement(inputtransform,
					"susp_wrongMethodRef_Removed_ConstructorCall_Added_FieldRead");
			inputtransform = add(inputtransform, "Method_RW_Var");
		}

		if (ArrayUtils.contains(inputtransform, "susp_wrongMethodRef_Removed_ConstructorCall_Added_Literal")) {
			inputtransform = ArrayUtils.removeElement(inputtransform,
					"susp_wrongMethodRef_Removed_ConstructorCall_Added_Literal");
			inputtransform = add(inputtransform, "Method_RW_Var");
		}

		if (ArrayUtils.contains(inputtransform, "susp_wrongMethodRef_Removed_NewClass_Added_FieldRead")) {
			inputtransform = ArrayUtils.removeElement(inputtransform,
					"susp_wrongMethodRef_Removed_NewClass_Added_FieldRead");
			inputtransform = add(inputtransform, "Method_RW_Var");
		}

		if (ArrayUtils.contains(inputtransform, "susp_wrongMethodRef_Removed_ConstructorCall_Added_TypeAccess")) {
			inputtransform = ArrayUtils.removeElement(inputtransform,
					"susp_wrongMethodRef_Removed_ConstructorCall_Added_TypeAccess");
			inputtransform = add(inputtransform, "Method_RW_Var");
		}

		if (ArrayUtils.contains(inputtransform, "susp_wrongMethodRef_Removed_Invocation_Added_TypeAccess")) {
			inputtransform = ArrayUtils.removeElement(inputtransform,
					"susp_wrongMethodRef_Removed_Invocation_Added_TypeAccess");
			inputtransform = add(inputtransform, "Method_RW_Var");
		}

		if (ArrayUtils.contains(inputtransform, "susp_wrongMethodRef_Removed_ConstructorCall_Added_VariableWrite")) {
			inputtransform = ArrayUtils.removeElement(inputtransform,
					"susp_wrongMethodRef_Removed_ConstructorCall_Added_VariableWrite");
			inputtransform = add(inputtransform, "Method_RW_Var");
		}
		// end method replace by var

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
}
