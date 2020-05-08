package oop.JavaGrinder.cc;
 
 import oop.ccBlock;
 import xtc.tree.GNode;
 import xtc.tree.Node;
 import xtc.tree.Visitor;
 
 public class ccStatement extends Visitor{
 	String line="";
 	private ccBlock block;
 	
 	public ccStatement(GNode n, ccBlock parent) {
 		block = parent;
 		dispatch(n);
 	}
 	
 	
 	
 	public ccStatement(GNode n) {
 		dispatch(n);
 	}
 	
 	
 	
 	public void visitExpression(GNode n){
 		visit(n);
 	}
 	
 	public void visitType(GNode n){
 		//for arrays
 		if((n.get(1) instanceof Node)&&n.getNode(1).hasName("Dimensions")){
 			line+="__rt::Array<";
 			dispatch(n.getNode(0));
 			line+=">";
 			System.out.println(line);
 		}
 		else{
 			visit(n);
 		}
 	}
 	public void visitReturnStatement(GNode n){
 		line += "return ";
 		visit(n);
 		line+=";";
 	}
 	public void visitCallExpression(GNode n){
 		if(n.getString(2).contains("print")){
 			line+="std::cout << ";
 			dispatch(n.getGeneric(3));
 			if(n.getString(2).equals("println")){
 				line+=" << std::endl";
 			}
 		}
 		else{
 			String objectType = block.currentClass;
 			String __this = "__this";
 			ccMethod methodInQuestion;
 			if(null != n.getNode(0) && null != n.getNode(0).getString(0)){
 				if(block.variables.containsKey(n.getNode(0).getString(0))){
 					__this = n.getNode(0).getString(0);
 					objectType = block.variables.get(__this);
 //					System.out.println(objectType);
 				}
 			} 
 			for(int i=0; i< block.classList.size(); i++){
 				if(block.classList.get(i).getName().startsWith(objectType)){ 
 //					System.out.println("  " + findArgumentTypes(n.getNode(3)).length);
 					System.out.println("  " + block.classList.get(i).getMethodName(n.getString(2), findArgumentTypes(n.getNode(3))));
 					methodInQuestion = block.classList.get(i).getMethod(n.getString(2), findArgumentTypes(n.getNode(3)));
 					System.out.println("    " + methodInQuestion.isStatic);
 					if(methodInQuestion.isStatic){
 						line+= __this + "." + methodInQuestion.getName() + "(" + __this;
 						for(int j = 0; j < n.getNode(3).size(); j++){
 							line+= ", ";
 							visit(n.getNode(3).getNode(j));
 						}
 						line+= ")";
 					}
 //					else if(methodInQuestion.access.contentEquals("private")){
 //						line+= __this + "." + methodInQuestion.getName() + "(" + __this;
 //						for(int j = 0; j < n.getNode(3).size(); j++){
 //							line+= ", ";
 //							visit(n.getNode(3).getNode(j));
 //						}
 //						line+= ")";
 //					}
 					else {
 						line+= __this + "->__vptr->" + methodInQuestion.getName() + "(" + __this;
 						for(int j = 0; j < n.getNode(3).size(); j++){
 							line+= ", ";
							if(n.getNode(3).getNode(j).hasName("StringLiteral")){
								line+="new __String("+n.getNode(3).getNode(j).getString(0)+")";
							}
							else{
								visit(n.getNode(3).getNode(j));
							}
 						}
 						line+= ")";
 					}
 				}
 			}
 		}
 	}
 	
 	public void visitUnaryExpression(GNode n){
 		line+=n.getString(0);
 		visit(n);
 	}
 	
 	public void visitMultiplicativeExpression(GNode n){
 		visit(n);
 	}
 	public void visitSubscriptExpression(GNode n){
 		dispatch(n.getNode(0));
 		line+="[";
 		dispatch(n.getNode(1));
 		line+="]";
 		
 	}
 	public void visitIntegerLiteral(GNode n){
 		line+=(String) n.get(0);
 	}
 	public void visitFloatingPointLiteral(GNode n){
 		line+=(String) n.get(0);
 	}
 	public void visitStringLiteral(GNode n){
 		line+=(String) n.get(0);
 	}
 	public void visitBooleanLiteral(GNode n){
 		line+=(String) n.get(0);		
 	}
 	
 	public void visitBlock(GNode n){
 		ccBlock blockStatement = new ccBlock(n);
 		while (!blockStatement.blockLines.isEmpty())
 			line += blockStatement.blockLines.remove()+"\n";
 	}
 	
 	public void visitConditionalStatement(GNode n){
 		line += "if("+new ccStatement((GNode)n.get(0)).publish()+")\n";
 		dispatch(n.getNode(1));
 		line += "\n else ";
 		dispatch(n.getNode(2));
 		System.out.println(line);
 	}
 	public void visitForStatement(GNode n){
 		line = "for(";
 		dispatch(n.getGeneric(0));
 		line+=")\n";
 		dispatch(n.getGeneric(1));
 	}
 	public void visitBasicForControl(GNode n){
 		dispatch(n.getNode(0));
 		dispatch(n.getNode(1));
 		dispatch(n.getNode(2));
 		dispatch(n.getNode(3));
 		line+=";";
 		dispatch(n.getNode(4));
 	}
 	public void visitBreakStatement(GNode n){
 		line = "break;";
 	}
 	public void visitWhileStatement(GNode n){
 		line = "while("+new ccStatement((GNode)n.get(0)).publish()+")";
 //		System.out.println(line);
 	}
 	public void visitDeclarator(GNode n){
 		line+=n.getString(0);
 		if(n.getNode(1)==null)
 			line+="= ";
 		else
 			line+=n.getString(1);
 		dispatch(n.getGeneric(2));
 		line+=";";
 	}
 	public void visitRelationalExpression(GNode n){
 		visit(n);
 	}
 	public void visitBitwiseOrExpression(GNode n){
 		dispatch(n.getNode(0));
 		line+="|";
 		dispatch(n.getNode(1));
 	}
 	public void visitBitwiseAndExpression(GNode n){
 		dispatch(n.getNode(0));
 		line+=" & ";
 		dispatch(n.getNode(1));
 	}
 	public void visitExpressionStatement(GNode n){
 		visit(n);
 		line+=";";
 	}
 	public void visitArguments(GNode n){
 		line += "(";
 		int i = 0;
 		for (Object o : n){
 			if(i > 0){line += ", ";}
 			if (o instanceof Node){
 				dispatch((Node)o);
 			}
 			i++;
 		}
 		line += ")";
 	}
 	
 	public void visitSelectionExpression(GNode n){
 		visit(n);
 	}
 	public void visitThisExpression(GNode n){
 		line+="this->";
 	}
 	public void visitNewArrayExpression(GNode n){
 		line+="new __rt::Array<";
 		dispatch(n.getNode(0));
 		line+=">[";
 		dispatch(n.getNode(1));
 		line+="]";
 	}
 	public void visitPrimaryIdentifier(GNode n){
 		if(block!=null&&!block.getLocalVariables().contains(n.get(0))){
 			line+= "__this::" + ccHelper.convertType(n.getString(0));
 		}	
 
 		else{
 			line+= ccHelper.convertType(n.getString(0));
 		}
 	}
 	
 	public void visitNewClassExpression(GNode n){
 		line+= "new __";
 		visit(n);
 	}
 	
 	public  String publish() {
 		return line;
 	}
	
 	public String[] findArgumentTypes(Node n){
 		String[] argTypes = new String[n.size()];
 		for(int i=0; i< n.size(); i++){
 //			System.out.println("  visiting: " + n.getNode(i).getName());
 			if(n.getNode(i).getName().matches("IntegerLiteral")){
 				argTypes[i] = "int32_t";
 			}
 			else if(n.getNode(i).getName().matches("FloatingPointLiteral")){
 				argTypes[i] = "double";
 			}
 			else if(n.getNode(i).getName().matches("StringLiteral")){
 				argTypes[i] = "String";
 			}
 			else if(n.getNode(i).getName().matches("BooleanLiteral")){
 				argTypes[i] = "bool";
 			}
 			else if(n.getNode(i).getName().matches("ThisExpression")){
 				argTypes[i] = block.currentClass;
 			}
 			else if(n.getNode(i).getName().matches("PrimaryIdentifier")){
 //				System.out.println("  !!!:" + n.getNode(i).getString(0));
 //				System.out.println("  !!!:" + block.variables.get(n.getNode(i).getString(0)));
 				argTypes[i] = block.variables.get(n.getNode(i).getString(0));
 			}
 		}
 		return argTypes;
 	}
 	
 	public void visit(Node n) {
 		for (Object o : n){
 			if (o instanceof Node){
 				dispatch((Node)o);
 			}
 			else if(o instanceof String){
 				if(((String) o).matches("=")){
 					line+=" = ";
 				}
 				else{
 					line+=ccHelper.convertType(o.toString());
 				}
 			}
 		}
 	}
 }
