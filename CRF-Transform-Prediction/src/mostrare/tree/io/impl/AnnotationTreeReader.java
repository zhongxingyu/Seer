package mostrare.tree.io.impl;

import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

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
import mostrare.crf.tree.impl.CRFWithConstraintNode;
import mostrare.tree.Node;
import mostrare.tree.impl.NodeAST;
import mostrare.tree.impl.TreeAST;
import mostrare.tree.io.TreeToAnnotationReader;

public class AnnotationTreeReader implements TreeToAnnotationReader {

	private static TreeToAnnotationReader instance = null;

	static {
		instance = new AnnotationTreeReader();
	}

	private AnnotationTreeReader() {

	}

	public static TreeToAnnotationReader getInstance() {
		return instance;
	}

	@Override
	public TreeAST readTree(File file, CRFWithConstraintNode crf) {
		JSONParser parser = new JSONParser();
		ArrayList<TreeAST> trainingtrees = new ArrayList<TreeAST>();

		try {
			long countOfLines = Files.lines(Paths.get(new File(file.getAbsolutePath()).getPath())).count();

			if (countOfLines > 1) {

				Object obj = parser.parse(new FileReader(file.getAbsolutePath()));
				JSONObject jsonObject = (JSONObject) obj;
				getTree(file, crf, trainingtrees, jsonObject);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (trainingtrees.size() > 0)
			return trainingtrees.get(0);
		else
			return null;
	}

	public TreeAST readTree(JSONObject jsonObject, CRFWithConstraintNode crf) {
		ArrayList<TreeAST> trainingtrees = new ArrayList<TreeAST>();
		getTree(null, crf, trainingtrees, jsonObject);
		if (trainingtrees.size() > 0) {

			return trainingtrees.get(0);
		} else
			return null;
	}

	public void getTree(File file, CRFWithConstraintNode crf, ArrayList<TreeAST> trainingtrees, JSONObject jsonObject) {

					JSONArray astList = (JSONArray) (jsonObject.get("faulty_ast"));
					JSONObject context = (JSONObject) (jsonObject.get("context"));
					JSONObject contextinfo = (JSONObject) context.get("cntx");

					if (astList.size() > 0) {

						JSONObject faulty_ast = (JSONObject) (astList.get(0));
						String nodetype = faulty_ast.get("type").toString();
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
						virtualroot.setParentNode(null);
						virtualroot.setLogicalExpressionIdentity("");
						virtualroot.setExpressionIdentity("");
						virtualroot.setBinaryOperatorIdentity("");
						virtualroot.setTypeofReturn("");
						virtualroot.setNameofNode("DEFAULT");

						TreeAST tree = new TreeAST(virtualroot);
						tree.setFileNmae((file != null) ? file.getName() : "nofilename");
						virtualroot.setTree(tree);
						tree.addNode(virtualroot);

						NodeAST root = new NodeAST(tree.getNodesNumber(), nodetype, crf);

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
							NodeAST lastChild = (NodeAST) virtualroot.orderedChildrenNodes.getQuick(oldNodesNumber - 1);
							lastChild.setNextSibling(root);
							root.setPreviousSibling(lastChild);
						} else
							virtualroot.firstChild = root;

						getNodeCharacterInfo(root, contextinfo, namefornode, crf);

						readEachJSONNode(faulty_ast, contextinfo, tree, root, crf);
						
						adjustFeatureV4(tree, crf);

						trainingtrees.add(tree);

					}
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

	public void readEachJSONNode(JSONObject faulty_node, JSONObject contextinfo, TreeAST ast, NodeAST parentNode,
			CRFWithConstraintNode crf) {

		JSONArray childrenList = (JSONArray) (faulty_node.get("children"));
		ArrayList<NodeAST> parentnodelist = new ArrayList<NodeAST>();
		parentnodelist.clear();

		if (childrenList.size() > 0) {
			for (int index = 0; index < childrenList.size(); index++) {

				JSONObject jsonObject = (JSONObject) (childrenList.get(index));
				String nodetype = jsonObject.get("type").toString();

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
				currentnode.setLogicalExpressionIdentity(logicalExpIdentity);
				currentnode.setExpressionIdentity(expIdentity);
				currentnode.setBinaryOperatorIdentity(binaryoperatorIdentity);
				currentnode.setNameofNode(namefornode);
				currentnode.setTypeofReturn(typeofreturn);
				
				currentnode.jsonnode = jsonObject;

				ast.addNode(currentnode);
				parentnodelist.add(currentnode);

				int oldNodesNumber = parentNode.orderedNodesNumber;
				parentNode.orderedChildrenNodes.add(currentnode);
				currentnode.setParentNode(parentNode);
				parentNode.setOrderedNodesNumber(oldNodesNumber + 1);
				currentnode.setPosition(oldNodesNumber);
				currentnode.setTree(ast);

				if (currentnode.getPosition() != 0) {
					NodeAST lastChild = (NodeAST) parentNode.orderedChildrenNodes.getQuick(oldNodesNumber - 1);
					lastChild.setNextSibling(currentnode);
					currentnode.setPreviousSibling(lastChild);
				} else
					parentNode.firstChild = currentnode;

				getNodeCharacterInfo(currentnode, contextinfo, namefornode, crf);
			}

			for (int index = 0; index < childrenList.size(); index++) {
				NodeAST parentnode = parentnodelist.get(index);
				JSONObject innernode = ((JSONObject) (childrenList.get(index)));
				readEachJSONNode(innernode, contextinfo, ast, parentnode, crf);
			}
		}
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

			if(!statementtype.isEmpty())
			    node.charactervaluemap.put(crf.getCharacterIndex("S3_IS_" + statementtype), true);
			if(!statementparenttype.isEmpty())
			    node.charactervaluemap.put(crf.getCharacterIndex("S14_IS_" + statementparenttype), true);
			if(!statementtypebefore1.isEmpty())
			    node.charactervaluemap.put(crf.getCharacterIndex("S13_Before_HAS_" + statementtypebefore1), true);
			if(!statementtypebefore2.isEmpty())
			    node.charactervaluemap.put(crf.getCharacterIndex("S13_Before_HAS_" + statementtypebefore2), true);
            if(!statementtypebefore3.isEmpty())
			    node.charactervaluemap.put(crf.getCharacterIndex("S13_Before_HAS_" + statementtypebefore3), true);
            if(!statementtypeafter1.isEmpty())
			    node.charactervaluemap.put(crf.getCharacterIndex("S13_After_HAS_" + statementtypeafter1), true);
            if(!statementtypeafter2.isEmpty())
			    node.charactervaluemap.put(crf.getCharacterIndex("S13_After_HAS_" + statementtypeafter2), true);
            if(!statementtypeafter3.isEmpty())
			    node.charactervaluemap.put(crf.getCharacterIndex("S13_After_HAS_" + statementtypeafter3), true);
            
            if(!statementtype.isEmpty() && !statementparenttype.isEmpty()) 
			    node.charactervaluemap.put(crf.getCharacterIndex("S3_IS_" + statementtype + 
			    		"_S14_IS_" + statementparenttype), true);
            
            if(!statementtype.isEmpty() && !statementtypebefore1.isEmpty()) 
			    node.charactervaluemap.put(crf.getCharacterIndex("S3_IS_" + statementtype + 
			    		"_S13_Before_HAS_" + statementtypebefore1), true);

            if(!statementtype.isEmpty() && !statementtypebefore2.isEmpty()) 
			    node.charactervaluemap.put(crf.getCharacterIndex("S3_IS_" + statementtype + 
			    		"_S13_Before_HAS_" + statementtypebefore2), true);
            
            if(!statementtype.isEmpty() && !statementtypebefore3.isEmpty()) 
			    node.charactervaluemap.put(crf.getCharacterIndex("S3_IS_" + statementtype + 
			    		"_S13_Before_HAS_" + statementtypebefore3), true);
            
            if(!statementtype.isEmpty() && !statementtypeafter1.isEmpty()) 
			    node.charactervaluemap.put(crf.getCharacterIndex("S3_IS_" + statementtype + 
			    		"_S13_After_HAS_" + statementtypeafter1), true);

            if(!statementtype.isEmpty() && !statementtypeafter2.isEmpty()) 
			    node.charactervaluemap.put(crf.getCharacterIndex("S3_IS_" + statementtype + 
			    		"_S13_After_HAS_" + statementtypeafter2), true);
            
            if(!statementtype.isEmpty() && !statementtypeafter3.isEmpty()) 
			    node.charactervaluemap.put(crf.getCharacterIndex("S3_IS_" + statementtype + 
			    		"_S13_After_HAS_" + statementtypeafter3), true);
            
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
}
