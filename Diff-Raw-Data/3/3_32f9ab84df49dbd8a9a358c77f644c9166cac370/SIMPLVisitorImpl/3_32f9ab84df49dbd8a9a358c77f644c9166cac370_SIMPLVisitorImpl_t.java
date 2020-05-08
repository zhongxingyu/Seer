 /**
  * 
  */
 package com.simPL.visitor;
 
 import com.simPL.compiler.ASTAddMinus;
 import com.simPL.compiler.ASTAndOr;
 import com.simPL.compiler.ASTApplication;
 import com.simPL.compiler.ASTAssignment;
 import com.simPL.compiler.ASTBool;
 import com.simPL.compiler.ASTBracket;
 import com.simPL.compiler.ASTCompare;
 import com.simPL.compiler.ASTCond;
 import com.simPL.compiler.ASTExpression;
 import com.simPL.compiler.ASTFunction;
 import com.simPL.compiler.ASTInt;
 import com.simPL.compiler.ASTLet;
 import com.simPL.compiler.ASTList;
 import com.simPL.compiler.ASTMulDiv;
 import com.simPL.compiler.ASTNil;
 import com.simPL.compiler.ASTPair;
 import com.simPL.compiler.ASTSTART;
 import com.simPL.compiler.ASTUnaryExp;
 import com.simPL.compiler.ASTUnit;
 import com.simPL.compiler.ASTVar;
 import com.simPL.compiler.ASTWhile;
 import com.simPL.compiler.SIMPL;
 import com.simPL.compiler.SIMPLVisitor;
 import com.simPL.compiler.SimpleNode;
 import com.simPL.compiler.SIMPLConstants;
 import com.simPL.compiler.Token;
 
 import java.util.ArrayList;
 import java.util.List;
 /**
  *
  */
 public class SIMPLVisitorImpl implements SIMPLVisitor, SIMPLConstants {
 	
 	public SimPLEnv env = new SimPLEnv();
 	public SimPLEnv envbak = null;
 	/* (non-Javadoc)
 	 * @see com.simPL.compiler.SIMPLVisitor#visit(com.simPL.compiler.SimpleNode, java.lang.Object)
 	 */
 	@Override
 	public Object visit(SimpleNode node, Object data) {
 		// TODO Auto-generated method stub
 		node.childrenAccept(this, data);
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.simPL.compiler.SIMPLVisitor#visit(com.simPL.compiler.ASTSTART, java.lang.Object)
 	 */
 	@Override
 	public Object visit(ASTSTART node, Object data) {
 		// TODO Auto-generated method stub
 		//return node.childrenAccept(this, data);
 		env.EnterBlock();
 		SimPLSymbol result = (SimPLSymbol) node.jjtGetChild(0).jjtAccept(this, data);
 		env.LeaveBlock();
 		if(result.type == ValueType.VAR){
 			if(env.GlobalExist((String)result.value)){
 				return env.GlobalGetSymbol((String)result.value);
 			}else
 				return new SimPLSymbol(ValueType.EXCEPTION,"not var "+result.value +" exists"); 
 		}
 		
 		return result;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.simPL.compiler.SIMPLVisitor#visit(com.simPL.compiler.ASTAssignment, java.lang.Object)
 	 */
 	@Override
 	public Object visit(ASTAssignment node, Object data) {
 		// TODO Auto-generated method stub
 		//node.childrenAccept(this, data);
 		int num = node.jjtGetNumChildren();
 		//System.out.println("in Assignment:"+num);
 		SimPLSymbol left = (SimPLSymbol)node.jjtGetChild(0).jjtAccept(this, data);
 		
 		if(num > 1){
 /*			if(node.jjtGetFirstToken().image == "head" || node.jjtGetFirstToken().image == "tail" || node.jjtGetFirstToken().image == "fst" || node.jjtGetFirstToken().image == "snd"){
 				return new SimPLSymbol(ValueType.EXCEPTION,"cannot operate on a unit");
 			}*/
 			String leftName = "";
 			if(left.type != ValueType.VAR){
 				return new SimPLSymbol(ValueType.EXCEPTION,":= left param should be a variable");
 			}
 			leftName = left.value.toString();
 			if(!env.GlobalExist(leftName)){
 				return new SimPLSymbol(ValueType.EXCEPTION,"var "+leftName+" does not exist in assignment");
 			}
 			ValueType leftType = env.GlobalGetSymbol(leftName).type;
 			
 			SimPLSymbol right = (SimPLSymbol)node.jjtGetChild(1).jjtAccept(this, data);
 			String rightName = "";
 			if(right.type == ValueType.VAR)
 			{
 				rightName = right.value.toString();
 				if(!env.GlobalExist((String)right.value)){
 					return new SimPLSymbol(ValueType.EXCEPTION, "var "+rightName+" is not defined");
 				}
 				right = env.GlobalGetSymbol((String)right.value);
 			}
 			if(right.type == ValueType.FREE && left.type == ValueType.FREE){
 				return new SimPLSymbol(ValueType.UNIT);
 			}
 			if(right.type == ValueType.FREE && left.type != ValueType.FREE){
 				env.GlobalSetSymbol(rightName, left);
 				return new SimPLSymbol(ValueType.UNIT);
 			}
 			env.GlobalSetSymbol(leftName, right);
 			return new SimPLSymbol(ValueType.UNIT);
 		}
 		
 		/*List<Token> list= new ArrayList<Token>();
 		Token cur = node.jjtGetFirstToken();
 		while(cur != ((SimpleNode)node.jjtGetChild(0)).jjtGetFirstToken()){
 			list.add(0,cur);
 			cur = cur.next;
 		}*/
 		
 		SimPLSymbol n = left;
 		/*for(int i = 0; i < list.size();i++){
 			//SimPLSymbol n = new SimPLSymbol(left.type,left.value);
 			cur = list.get(i);
 			if(n.type == ValueType.VAR){
 				if(env.GlobalExist((String)n.value)){
 					n = env.GlobalGetSymbol((String)n.value);
 				}else
 					return new SimPLSymbol(ValueType.EXCEPTION,"not var "+n.value +" exists"); 
 			}
 			try{
 				if(cur.image == "head"){
 					if(n.type != ValueType.LIST){
 						return new SimPLSymbol(ValueType.EXCEPTION,"head argument is not a list");
 					}else{
 						if(n.value == null)//empty list
 							return new SimPLSymbol(ValueType.EXCEPTION,"head on nil");
 						else {
 							n = ((ArrayList<SimPLSymbol>)(n.value)).get(0);
 							cur = cur.next;
 							continue;
 						}
 					}
 				}else if(cur.image == "tail"){
 					if(n.type != ValueType.LIST){
 						return new SimPLSymbol(ValueType.EXCEPTION,"tail argument is not a list");
 					}else{
 						if(n.value == null)//empty list
 							return new SimPLSymbol(ValueType.EXCEPTION,"tail on nil");
 						else{
 							SimPLSymbol result = n;
 							if(((ArrayList<SimPLSymbol>)result.value).size()==1){
 								n.value=null;
 								continue;
 							}else if(((ArrayList<SimPLSymbol>)result.value).size()==0){
 								return new SimPLSymbol(ValueType.EXCEPTION,"tail on nil");
 							}
 							
 							((ArrayList<SimPLSymbol>)result.value).remove(0);
 							n = result;
 							cur = cur.next;
 							
 							continue;
 						}
 					}
 				}else if(cur.image == "fst"){
 					if(n.type != ValueType.PAIR){
 						return new SimPLSymbol(ValueType.EXCEPTION,"fst argument is not a pair");
 					}else{
 						
 							MyPair p  = (MyPair)(n.value);
 							n = (SimPLSymbol)p.first;
 							cur = cur.next;
 							continue;
 					}
 				}else if(cur.image == "snd"){
 					if(n.type != ValueType.PAIR){
 						return new SimPLSymbol(ValueType.EXCEPTION,"snd argument is not a pair");
 					}else{
 						
 							MyPair p  = (MyPair)(n.value);
 							n = (SimPLSymbol)p.second;
 							cur = cur.next;
 							continue;
 					}
 				}else {
 					return new SimPLSymbol(ValueType.EXCEPTION,"error in fst,head,tail,snd");
 					
 				}
 			}catch (Exception e){
 				return new SimPLSymbol(ValueType.EXCEPTION,"error in fst,head,tail,snd");
 			}
 			//cur = cur.next;
 		}*/
 		
 		return n;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.simPL.compiler.SIMPLVisitor#visit(com.simPL.compiler.ASTListInsert, java.lang.Object)
 	 */
 	@Override
 	public Object visit(ASTList node, Object data) {
 		// TODO Auto-generated method stub
 		int num = node.jjtGetNumChildren();
 		//System.out.println("in ADDMINUS:"+num);
 		
 		Object first = node.jjtGetChild(0).jjtAccept(this, data);
 		if(num > 1){
 			try {
 				String leftName="";
 				SimPLSymbol left = (SimPLSymbol)first;
 				if(left.type == ValueType.VAR){
 					leftName=left.value.toString();
 					if(env.GlobalExist((String)left.value)){
 						left = env.GlobalGetSymbol((String)left.value);
 					}else{
 						return new SimPLSymbol(ValueType.EXCEPTION,"no such symbol "+left.value.toString());
 					}
 				}
 				SimPLSymbol right = null;
 				for(int i = 1; i < num; i++){
 					right = (SimPLSymbol)node.jjtGetChild(i).jjtAccept(this, data);
 					if(right.type == ValueType.VAR){
 						if(env.GlobalExist((String)right.value)){
 							right = env.GlobalGetSymbol((String)right.value);
 						}else{
 							return new SimPLSymbol(ValueType.EXCEPTION,"no such symbol "+right.value.toString());
 						}
 					}
 					if(right.type == ValueType.LIST){
 						if(right.value==null){
 							//left is the first element
 							List<SimPLSymbol> list = new ArrayList<SimPLSymbol>();
 							list.add(left);
 							right.value = list;
 						}else{
 							try{
 								List<SimPLSymbol> list = (ArrayList<SimPLSymbol>)right.value;
 								ValueType listType = list.get(0).type;
 								SimPLSymbol listSample = list.get(0);
 								for(int j = 0; j < list.size(); j++){
 									ValueType curType = list.get(j).type;
 									if(curType != ValueType.FREE){
 										if(listType==ValueType.FREE){
 											listSample = list.get(j);
 											listType=curType;
 											break;
 										}else if(listType != curType){
 											return new SimPLSymbol(ValueType.EXCEPTION, "list value not the same type");
 										}
 									}
 								}
 								for(int j = 0; j < list.size(); j++){
 									ValueType curType = list.get(j).type;
 									if(curType == ValueType.FREE){
 										//runtime check only
 										System.out.println("warning, free value in list");
 									}
 								}
 								if(left.type == ValueType.FREE && listType != ValueType.FREE)
 								{
 									//left = new SimPLSymbol(listType);
 									left = listSample;
 									env.GlobalSetSymbol(leftName, left);
 								}
 								if(SameListLevel(left,listSample)){
 									//list.add(left);
 									list.add(0,left);
 									//right.value=list;
 								}else{
 									return new SimPLSymbol(ValueType.EXCEPTION,"list level not matched");
 								}
 							}catch (Exception e){
 								return new SimPLSymbol(ValueType.EXCEPTION,"exception in list");
 							}
 						}
 						
 					}else{
 						return new SimPLSymbol(ValueType.EXCEPTION,"right of list op is not a list");
 					}
 					left = right;
 				}
 				return right; 
 			}
 			catch (Exception e){
 				return new SimPLSymbol(ValueType.EXCEPTION,"Err in AddMinus");
 			}
 		}
 		
 		return first;
 	}
 
 	private boolean SameListLevel(SimPLSymbol left, SimPLSymbol right){
 		if(left.type == ValueType.VAR){
 			left = env.GlobalGetSymbol((String)left.value);
 		}
 		if(right.type == ValueType.VAR){
 			right = env.GlobalGetSymbol((String)right.value);
 		}
 		
 		if(left.type == ValueType.FREE || right.type == ValueType.FREE)
 		{
 			return true;
 		}
 		
 		if(right.type != left.type)
 			return false;
 		if(left.type != ValueType.LIST)
 		{
 			//function judgement
 			return true;
 		}
 		if((ArrayList<SimPLSymbol>)left.value == null)
 			return true;
 		if((ArrayList<SimPLSymbol>)right.value == null)
 			return true;
 		return SameListLevel(((ArrayList<SimPLSymbol>)left.value).get(0),((ArrayList<SimPLSymbol>)right.value).get(0));
 	}
 	/* (non-Javadoc)
 	 * @see com.simPL.compiler.SIMPLVisitor#visit(com.simPL.compiler.ASTAndOr, java.lang.Object)
 	 */
 	@Override
 	public Object visit(ASTAndOr node, Object data) {
 		// TODO Auto-generated method stub
 		int num = node.jjtGetNumChildren();
 		//System.out.println("in AndOr:"+num);
 		
 		Object first = node.jjtGetChild(0).jjtAccept(this, data);
 		try {
 			if(num > 1){
 				boolean sum = true;
 				Object cur = first;
 				boolean sign = true;
 				for(int i = 0; i < num; i++){
 					SimPLSymbol curS = (SimPLSymbol)cur;
 					String varName = "";
 					if(curS.type == ValueType.VAR)
 					{
 						varName = (String)curS.value;
 						if(!env.GlobalExist(varName)){
 							return new SimPLSymbol(ValueType.EXCEPTION,"no such symbol "+varName);
 						}else {
 							curS =env.GlobalGetSymbol(varName);
 						}
 						//sum += (int)cur.value;
 					}
 					if(curS.type == ValueType.FREE){
 						curS = new SimPLSymbol(ValueType.BOOLEAN,"true");
 						env.GlobalSetSymbol(varName, curS);
 					}
 					//if(curS.type == ValueType.UNDEF){
 					//	return new SimPLSymbol(ValueType.UNDEF);
 					//}
 					if(curS.type == ValueType.BOOLEAN){
 						if(sign)
 							sum = sum && (curS.value.toString()=="true");
 						else
 							sum = sum || (curS.value.toString()=="true");
 					} else {
 						return new SimPLSymbol(ValueType.EXCEPTION,"wrong type in andor, bool needed");
 					}
 					if( i == num -1 )
 						break;
 					SimpleNode last = (SimpleNode)(node.jjtGetChild(i));
 					//System.out.println(last.jjtGetLastToken().next.image);
 					if(last.jjtGetLastToken().next.image == "and")
 						sign = true;
 					else if(last.jjtGetLastToken().next.image == "or")
 						sign = false;
 					cur = node.jjtGetChild(i+1).jjtAccept(this, data);
 				}
 				SimPLSymbol result = new SimPLSymbol(ValueType.BOOLEAN);
 				result.value = sum?"true":"false";
 				return result; 
 			}
 		}
 		catch (Exception e){
 			return new SimPLSymbol(ValueType.EXCEPTION,"Err in AndOr");
 		}
 		return first;
 	}
 
 	
 	private boolean SameList(SimPLSymbol left, SimPLSymbol right){
 		if((left.type == ValueType.LIST && right.type != ValueType.LIST)
 				||(left.type != ValueType.LIST && right.type == ValueType.LIST))
 			return false;
 		
 		if(right.type != left.type)
 			return false;
 		
 		if(left.type != ValueType.LIST)
 		{
 			return Equal(left,right);
 		}
 		if(left.value == null || right.value == null){
 			return left.value == null && right.value == null;
 		}
 		ArrayList<SimPLSymbol> leftlist = ((ArrayList<SimPLSymbol>)left.value);
 		ArrayList<SimPLSymbol> rightlist = ((ArrayList<SimPLSymbol>)right.value);
 		if(leftlist.size()!=rightlist.size())
 			return false;
 		for(int i = 0; i != leftlist.size(); i++){
 			if(!Equal(leftlist.get(i),rightlist.get(i)))
 					return false;
 		}
 		return true;
 	}
 	private boolean Equal(SimPLSymbol left, SimPLSymbol right){
 		if(left.type == ValueType.VAR)
 		{
 			if(!env.GlobalExist(left.value.toString())){
 				return false;
 			}else {
 				left =env.GlobalGetSymbol(left.value.toString());
 			}
 		}
 		if(left.type == ValueType.FREE){
 			return false;
 		}
 		if(right.type == ValueType.VAR)
 		{
 			if(!env.GlobalExist(right.value.toString())){
 				return false;
 			}else {
 				right =env.GlobalGetSymbol(right.value.toString());
 			}
 		}
 		if(right.type == ValueType.FREE){
 			return false;
 		}
 		if(left.type != right.type)
 			return false;
 		if(left.type == ValueType.INTEGER)
 			return Integer.parseInt(left.value.toString())== Integer.parseInt(right.value.toString());
 		if(left.type == ValueType.BOOLEAN)
 			return left.value.toString() == right.value.toString();
 		if(left.type == ValueType.LIST){
 			return SameList(left,right);
 		}
 		if(left.type == ValueType.UNIT)
 			return true;
 		if(left.type == ValueType.PAIR)
 			return Equal(((MyPair)left.value).first,((MyPair)right.value).first) 
 					&& Equal(((MyPair)left.value).second,((MyPair)right.value).second);
 		if(left.type == ValueType.FUN){
 			MyFunc leftFunc = (MyFunc)left.value;
 			MyFunc rightFunc = (MyFunc)right.value;
 			if(leftFunc.level != rightFunc.level)
 				return false;
 			if(leftFunc.level == 0)
 				return leftFunc.returnType == rightFunc.returnType && leftFunc.paramType == rightFunc.paramType;
 			else{
 				return Equal(leftFunc.body,rightFunc.body);
 			}
 		}
 		if(left.type == ValueType.UNDEF)
 			return false;
 		return true;
 	}
 	
 	
 	/* (non-Javadoc)
 	 * @see com.simPL.compiler.SIMPLVisitor#visit(com.simPL.compiler.ASTCompare, java.lang.Object)
 	 */
 	@Override
 	public Object visit(ASTCompare node, Object data) {
 		// TODO Auto-generated method stub
 		int num = node.jjtGetNumChildren();
 		//System.out.println("in Compare:"+num);
 		
 		Object first = node.jjtGetChild(0).jjtAccept(this, data);
 		try {
 			if(num > 1){
 				String op = ((SimpleNode)(node.jjtGetChild(0))).jjtGetLastToken().next.image;
 				SimPLSymbol left = (SimPLSymbol)first;
 				String leftName = "";
 				if(left.type == ValueType.VAR)
 				 {
 					leftName = left.value.toString();
 						String var = (String)left.value;
 						if(!env.GlobalExist(var)){
 							return new SimPLSymbol(ValueType.EXCEPTION,"no such symbol "+var);
 						}else {
 							left =env.GlobalGetSymbol(var);
 						}
 				}
 				if(left.type == ValueType.UNDEF)
 					return new SimPLSymbol(ValueType.UNDEF);
 				
 				
 				SimPLSymbol right = (SimPLSymbol)node.jjtGetChild(1).jjtAccept(this, data);
 				String rightName="";
 				if(right.type == ValueType.VAR)
 				 {
 					rightName = right.value.toString();
 						String var = (String)right.value;
 						if(!env.GlobalExist(var)){
 							return new SimPLSymbol(ValueType.EXCEPTION,"no such symbol "+var);
 						}else {
 							right =env.GlobalGetSymbol(var);
 						}
 				}
 				if(right.type == ValueType.UNDEF)
 					return new SimPLSymbol(ValueType.UNDEF);
 				if(op != "=") {
 					if(left.type == ValueType.FREE){
 						left = new SimPLSymbol(ValueType.INTEGER);
 						env.GlobalSetSymbol(leftName, left);
 					}
 					if(right.type == ValueType.FREE){
 						right = new SimPLSymbol(ValueType.INTEGER);
 						env.GlobalSetSymbol(rightName, right);
 					}
 					if(left.type != ValueType.INTEGER)
 					{
 						return new SimPLSymbol(ValueType.EXCEPTION,"left in compare need int");
 					}
 					if(right.type != ValueType.INTEGER){
 						return new SimPLSymbol(ValueType.EXCEPTION,"right in compare need int");
 					}
 					int lv = Integer.parseInt(left.value.toString());
 					int rv = Integer.parseInt(right.value.toString());
 					SimPLSymbol result = new SimPLSymbol(ValueType.BOOLEAN);
 					if(op==">")
 						result.value = lv > rv?"true":"false";
 					else if(op=="<")
 						result.value = lv < rv?"true":"false";
 					return result;
 				}else{
 					if(left.type == ValueType.FREE && right.type == ValueType.FREE){
 						return new SimPLSymbol(ValueType.BOOLEAN);
 					}
 					if(left.type == ValueType.FREE){
 						left = new SimPLSymbol(right.type,right.value);
 						env.GlobalSetSymbol(leftName, left);
 					}else if (right.type == ValueType.FREE){
 						right = new SimPLSymbol(left.type,left.value);
 						env.GlobalSetSymbol(rightName,right);
 					}
 					if(left.type != right.type)
 						return new SimPLSymbol(ValueType.EXCEPTION, "type in eq not match");
 					SimPLSymbol result = new SimPLSymbol(ValueType.BOOLEAN);
 					result.value = Equal(left,right);
 					return result;
 				}
 			}
 		}
 		catch (Exception e){
 			return new SimPLSymbol(ValueType.EXCEPTION,"Err in Compare");
 		}
 		return first;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.simPL.compiler.SIMPLVisitor#visit(com.simPL.compiler.ASTAddMinus, java.lang.Object)
 	 */
 	@Override
 	public Object visit(ASTAddMinus node, Object data) {
 		// TODO Auto-generated method stub
 		int num = node.jjtGetNumChildren();
 		//System.out.println("in ADDMINUS:"+num);
 		
 		Object first = node.jjtGetChild(0).jjtAccept(this, data);
 		try {
 			if(num > 1){
 				int sum = 0;
 				Object cur = first;
 				int sign = 1;
 				for(int i = 0; i < num; i++){
 					String varName="";
 					SimPLSymbol curS = (SimPLSymbol)cur;
 					 if(curS.type == ValueType.VAR)
 					 {
 						 varName = (String)curS.value;
 							String var = varName;
 							if(!env.GlobalExist(var)){
 								return new SimPLSymbol(ValueType.EXCEPTION,"no such symbol "+var);
 							}else {
 								curS =env.GlobalGetSymbol(var);
 							}
 							//sum += (int)cur.value;
 					}
 					if(curS.type==ValueType.FREE){
 						curS = new SimPLSymbol(ValueType.INTEGER,"0");
 						env.GlobalSetSymbol(varName, curS);
 					}
 					if(curS.type == ValueType.UNDEF)
 						return new SimPLSymbol(ValueType.UNDEF);
 					if(curS.type == ValueType.INTEGER)
 						sum += sign * Integer.parseInt((String)curS.value);
 					else {
 						return new SimPLSymbol(ValueType.EXCEPTION,"wrong type in addminus, integer needed");
 					}
 					if( i == num -1 )
 						break;
 					SimpleNode last = (SimpleNode)(node.jjtGetChild(i));
 					//System.out.println(last.jjtGetLastToken().next.image);
 					if(last.jjtGetLastToken().next.image == "+")
 						sign = 1;
 					else
 						sign = -1;
 					cur = node.jjtGetChild(i+1).jjtAccept(this, data);
 				}
 				SimPLSymbol result = new SimPLSymbol(ValueType.INTEGER);
 				result.value = Integer.toString(sum);
 				return result; 
 			}
 		}
 		catch (Exception e){
 			return new SimPLSymbol(ValueType.EXCEPTION,"Err in AddMinus");
 		}
 		return first;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.simPL.compiler.SIMPLVisitor#visit(com.simPL.compiler.ASTMulDiv, java.lang.Object)
 	 */
 	@Override
 	public Object visit(ASTMulDiv node, Object data) {
 		// TODO Auto-generated method stub
 		int num = node.jjtGetNumChildren();
 		//System.out.println("in MulDiv:"+num);
 		
 		Object first = node.jjtGetChild(0).jjtAccept(this, data);
 		try {
 			if(num > 1){
 				int sum = 1;
 				Object cur = first;
 				boolean sign = true;
 				for(int i = 0; i < num; i++){
 					
 					SimPLSymbol curS = (SimPLSymbol)cur;
 					String var = "";
 					if(curS.type == ValueType.VAR)
 					{
 						var = (String)curS.value;
 						if(!env.GlobalExist(var)){
 							return new SimPLSymbol(ValueType.EXCEPTION,"no such symbol "+var);
 						}else {
 							curS =env.GlobalGetSymbol(var);
 						}
 						//sum += (int)cur.value;
 					}
 					if(curS.type==ValueType.FREE){
 						curS = new SimPLSymbol(ValueType.INTEGER,"1");
 						env.GlobalSetSymbol(var, curS);
 					}
 					if(curS.type == ValueType.INTEGER){
 						if(sign)
 							sum = sum * (Integer.parseInt(curS.value.toString()));
 						else {
 							int division = (Integer.parseInt(curS.value.toString()));
 							if(division == 0)
 								return new SimPLSymbol(ValueType.UNDEF);
 							sum = sum / division;
 						}
 					} else {
 						return new SimPLSymbol(ValueType.EXCEPTION,"wrong type in muldiv, integer needed");
 					}
 					if( i == num -1 )
 						break;
 					SimpleNode last = (SimpleNode)(node.jjtGetChild(i));
 					//System.out.println(last.jjtGetLastToken().next.image);
 					if(last.jjtGetLastToken().next.image == "*")
 						sign = true;
 					else if(last.jjtGetLastToken().next.image == "/")
 						sign = false;
 					cur = node.jjtGetChild(i+1).jjtAccept(this, data);
 				}
 				SimPLSymbol result = new SimPLSymbol(ValueType.INTEGER);
 				result.value = Integer.toString(sum);
 				return result; 
 			}
 		}
 		catch (Exception e){
 			return new SimPLSymbol(ValueType.EXCEPTION,"Err in MultiDiv");
 		}
 		return first;
 	}
 
 	
 
 	/* (non-Javadoc)
 	 * @see com.simPL.compiler.SIMPLVisitor#visit(com.simPL.compiler.ASTUnaryExp, java.lang.Object)
 	 */
 	@Override
 	public Object visit(ASTUnaryExp node, Object data) {
 		// TODO Auto-generated method stub
 		SimPLSymbol value = (SimPLSymbol)node.jjtGetChild(0).jjtAccept(this, data);
 		String op = ((SimpleNode)node).jjtGetFirstToken().image;
 		if(op == "not"){
 			String var="";
 			if(value.type == ValueType.VAR)
 			{
 				var = (String)value.value;
 				if(!env.GlobalExist(var)){
 					return new SimPLSymbol(ValueType.EXCEPTION, "var "+value.value+" is not defined");
 				}else
 					value = env.GlobalGetSymbol(value.value.toString());
 			}
 			if(value.type==ValueType.FREE){
 				value = new SimPLSymbol(ValueType.BOOLEAN,"true");
 				env.GlobalSetSymbol(var, value);
 			}
 			if(value.type != ValueType.BOOLEAN)
 				return new SimPLSymbol(ValueType.EXCEPTION, "not op should be followed by a boolean"); 
 			value.value = value.value.toString()=="true"?"false":"true";
 			
 			return value;
 		}else if(op == "~"){
 			String var="";
 			if(value.type == ValueType.VAR)
 			{
 				var = (String)value.value;
 				if(!env.GlobalExist((String)value.value)){
 					return new SimPLSymbol(ValueType.EXCEPTION, "var "+value.value+" is not defined");
 				}else
 					value = env.GlobalGetSymbol(value.value.toString());
 			}
 			if(value.type==ValueType.FREE){
 				value = new SimPLSymbol(ValueType.INTEGER,"0");
 				env.GlobalSetSymbol(var, value);
 			}
 			if(value.type == ValueType.UNDEF)
 				return new SimPLSymbol(ValueType.UNDEF);
 			if(value.type != ValueType.INTEGER)
 				return new SimPLSymbol(ValueType.EXCEPTION, "~ op should be followed by a int"); 
 			value.value = Integer.toString(-1*Integer.parseInt(value.value.toString()));
 			return value;
 		}else if(op == "fst" || op == "snd"){
 			String var="";
 			if(value.type == ValueType.VAR)
 			{
 				var = (String)value.value;
 				if(!env.GlobalExist(var)){
 					return new SimPLSymbol(ValueType.EXCEPTION, "var "+value.value+" is not defined");
 				}else
 					value = env.GlobalGetSymbol(value.value.toString());
 			}
 			if(value.type==ValueType.FREE){
 				MyPair mine = new MyPair(new SimPLSymbol(ValueType.FREE),new SimPLSymbol(ValueType.FREE));
 				value = new SimPLSymbol(ValueType.PAIR,mine);
 				env.GlobalSetSymbol(var, value);
 			}
 			if(value.type != ValueType.PAIR)
 				return new SimPLSymbol(ValueType.EXCEPTION, "fst/snd should be followed by a pair"); 
 			return op=="fst"?((MyPair)value.value).first:((MyPair)value.value).second;
 		}else if(op == "head" || op == "tail"){
 			String var="";
 			if(value.type == ValueType.VAR)
 			{
 				var = (String)value.value;
 				if(!env.GlobalExist(var)){
 					return new SimPLSymbol(ValueType.EXCEPTION, "var "+value.value+" is not defined");
 				}else
 					value = env.GlobalGetSymbol(value.value.toString());
 			}
 			if(value.type==ValueType.FREE){
 				value =  new SimPLSymbol(ValueType.LIST,null);
 				env.GlobalSetSymbol(var, value);
 			}
 			if(value.type != ValueType.LIST)
 				return new SimPLSymbol(ValueType.EXCEPTION, "head/tail should be followed by a list");
 			List<SimPLSymbol> list = (List<SimPLSymbol>)value.value;
 			if(list == null) {
 				return value;
 			//	return new SimPLSymbol(ValueType.EXCEPTION, "head/tail on a nil");
 			}
 			if(op == "head"){
 				return list.get(0);
 			}else{
 				list.remove(0);
				if(list.size()==0)
					value.value = null;
 				return value;
 			}
 		}
 		return new SimPLSymbol(ValueType.EXCEPTION,"not such unary op "+op);
 	}
 
 	/* (non-Javadoc)
 	 * @see com.simPL.compiler.SIMPLVisitor#visit(com.simPL.compiler.ASTLet, java.lang.Object)
 	 */
 	@Override
 	public Object visit(ASTLet node, Object data) {
 		// TODO Auto-generated method stub
 		int num = node.jjtGetNumChildren();
 		//System.out.println("in Let:"+num);
 		
 		SimPLSymbol var = (SimPLSymbol)node.jjtGetChild(0).jjtAccept(this, data);
 		
 		
 		
 //		if(env.LocalExist((String)var.value)){
 //			//should never in
 //			return new SimPLSymbol(ValueType.EXCEPTION, "var "+var.value+" already exists in let");
 //		}
 		env.EnterBlock();
 		env.LocalSetSymbol((String)var.value, new SimPLSymbol(ValueType.FREE));
 		SimPLSymbol value = (SimPLSymbol)node.jjtGetChild(1).jjtAccept(this, data);
 		if(value.type == ValueType.VAR)
 		{
 			if(!env.GlobalExist((String)value.value)){
 				return new SimPLSymbol(ValueType.EXCEPTION, "var "+value.value+" is not defined");
 			}
 			env.LocalSetSymbol((String)var.value, env.GlobalGetSymbol((String)value.value));
 		}
 		else {
 			env.LocalSetSymbol((String)var.value, value);
 		}
 		SimPLSymbol body = (SimPLSymbol)node.jjtGetChild(2).jjtAccept(this, data);
 		if(body.type == ValueType.VAR)
 		{
 			if(!env.GlobalExist((String)body.value)){
 				return new SimPLSymbol(ValueType.EXCEPTION, "var "+body.value+" is not defined");
 			}
 			body = env.GlobalGetSymbol((String)body.value);
 		}
 		env.LeaveBlock();
 		return body;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.simPL.compiler.SIMPLVisitor#visit(com.simPL.compiler.ASTCond, java.lang.Object)
 	 */
 	@Override
 	public Object visit(ASTCond node, Object data) {
 		// TODO Auto-generated method stub
 		SimPLSymbol cond = (SimPLSymbol)node.jjtGetChild(0).jjtAccept(this, data);
 		String var = "";
 		if(cond.type == ValueType.EXCEPTION){
 			return cond;
 		}
 		/*SimPLEnv envbak = env.Duplicate();
 		SimPLSymbol thenValue = (SimPLSymbol)node.jjtGetChild(1).jjtAccept(this, data);
 		var ="";
 		if(thenValue.type == ValueType.VAR){
 			if(env.GlobalExist((String)thenValue.value)){
 				var = thenValue.value.toString();
 				thenValue = env.GlobalGetSymbol((String)thenValue.value);
 			}else
 				return new SimPLSymbol(ValueType.EXCEPTION,"var "+(String)thenValue.value+" not exist in assignment");
 		}
 		env = envbak;
 		SimPLSymbol elseValue = (SimPLSymbol)node.jjtGetChild(2).jjtAccept(this, data);
 		String var2= "";
 		if(elseValue.type == ValueType.VAR){
 			if(env.GlobalExist((String)elseValue.value)){
 				var2 = elseValue.value.toString();
 				elseValue = env.GlobalGetSymbol((String)elseValue.value);
 			}else
 				return new SimPLSymbol(ValueType.EXCEPTION,"var "+(String)elseValue.value+" not exist in assignment");
 		}
 		env = envbak;
 		if(thenValue.type != elseValue.type){
 			if(thenValue.type != ValueType.FREE && elseValue.type != ValueType.FREE){
 				return new SimPLSymbol(ValueType.EXCEPTION,"else statement and then statement should return the same type");
 			}else if(thenValue.type == ValueType.FREE){
 				env.GlobalSetSymbol(var, elseValue);
 			}else
 				env.GlobalSetSymbol(var2, thenValue);
 		}*/
 		
 		if(cond.type == ValueType.VAR)
 		{
 			var = cond.value.toString();
 			if(!env.GlobalExist((String)cond.value)){
 				return new SimPLSymbol(ValueType.EXCEPTION, "var "+cond.value+" is not defined");
 			}
 			cond = env.GlobalGetSymbol((String)cond.value);
 		}
 		if(cond.type == ValueType.FREE)
 		{
 			cond = new SimPLSymbol(ValueType.BOOLEAN,"true");
 			env.GlobalSetSymbol(var, cond);
 		}
 		if(cond.type != ValueType.BOOLEAN)
 		{
 			return new SimPLSymbol(ValueType.EXCEPTION,"if condition should be boolean");
 		}
 		
 		if(cond.value.toString() == "true"){
 			return node.jjtGetChild(1).jjtAccept(this, data);
 		}else{
 			return node.jjtGetChild(2).jjtAccept(this, data);
 		}
 		
 	}
 
 	/* (non-Javadoc)
 	 * @see com.simPL.compiler.SIMPLVisitor#visit(com.simPL.compiler.ASTWhile, java.lang.Object)
 	 */
 	@Override
 	public Object visit(ASTWhile node, Object data) {
 		// TODO Auto-generated method stub
 		
 				
 		while(true){
 			SimPLSymbol cond = (SimPLSymbol)node.jjtGetChild(0).jjtAccept(this, data);
 			
 			String var = "";
 			if(cond.type == ValueType.VAR)
 			{
 				var = cond.value.toString();
 				if(!env.GlobalExist((String)cond.value)){
 					return new SimPLSymbol(ValueType.EXCEPTION, "var "+cond.value+" is not defined");
 				}
 				cond = env.GlobalGetSymbol((String)cond.value);
 			}
 			if(cond.type == ValueType.FREE)
 			{
 				cond = new SimPLSymbol(ValueType.BOOLEAN,"true");
 				env.GlobalSetSymbol(var, cond);
 			}
 			
 			if(cond.type != ValueType.BOOLEAN)
 			{
 				return new SimPLSymbol(ValueType.EXCEPTION,"while condition should be boolean");
 			}
 			
 			if(cond.value.toString() == "true"){
 				SimPLSymbol result = (SimPLSymbol)node.jjtGetChild(1).jjtAccept(this, data);
 				if(result.type == ValueType.EXCEPTION)
 					return result;
 			}else {
 				break;
 			}
 		}
 		return new SimPLSymbol(ValueType.UNIT);
 	}
 
 	/* (non-Javadoc)
 	 * @see com.simPL.compiler.SIMPLVisitor#visit(com.simPL.compiler.ASTBracket, java.lang.Object)
 	 */
 	@Override
 	public Object visit(ASTBracket node, Object data) {
 		// TODO Auto-generated method stub
 		return node.jjtGetChild(0).jjtAccept(this, data);
 	}
 	/* (non-Javadoc)
 	 * @see com.simPL.compiler.SIMPLVisitor#visit(com.simPL.compiler.ASTPair, java.lang.Object)
 	 */
 	@Override
 	public Object visit(ASTPair node, Object data) {
 		// TODO Auto-generated method stub
 		int num = node.jjtGetNumChildren();
 		
 		SimPLSymbol first = (SimPLSymbol)node.jjtGetChild(0).jjtAccept(this, data);
 		if(first.type == ValueType.VAR)
 		{
 			if(!env.GlobalExist((String)first.value)){
 				return new SimPLSymbol(ValueType.EXCEPTION, "var "+first.value+" is not defined");
 			}
 			first = env.GlobalGetSymbol((String)first.value);
 		}
 		SimPLSymbol second = (SimPLSymbol)node.jjtGetChild(1).jjtAccept(this, data);
 		if(second.type == ValueType.VAR)
 		{
 			if(!env.GlobalExist((String)second.value)){
 				return new SimPLSymbol(ValueType.EXCEPTION, "var "+second.value+" is not defined");
 			}
 			second = env.GlobalGetSymbol((String)second.value);
 		}
 		SimPLSymbol result = new SimPLSymbol(ValueType.PAIR);
 		result.value = new MyPair(first,second);
 		return result;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.simPL.compiler.SIMPLVisitor#visit(com.simPL.compiler.ASTApplication, java.lang.Object)
 	 */
 	@Override
 	public Object visit(ASTApplication node, Object data) {
 		// TODO Auto-generated method stub
 		int num = node.jjtGetNumChildren();
 		
 		//System.out.println("in Application:"+num);
 		
 		SimPLSymbol param = (SimPLSymbol)node.jjtGetChild(1).jjtAccept(this, data);
 		String paramName = "";
 		if(param.type == ValueType.VAR){
 			paramName = param.value.toString();
 			if(!env.GlobalExist((String)param.value)){
 				return new SimPLSymbol(ValueType.EXCEPTION, "var "+param.value+" is not defined");
 			}else
 				param = env.GlobalGetSymbol(param.value.toString());
 		}
 		int depth = env.GetDepth();
 		SimPLSymbol func = (SimPLSymbol)node.jjtGetChild(0).jjtAccept(this, data);
 		if(depth != env.GetDepth())
 		{
 			System.out.println("nests should be "+depth);
 		}
 		if(func.type == ValueType.EXCEPTION)
 			return new SimPLSymbol(ValueType.EXCEPTION,"error in first application");
 		if(func.type == ValueType.VAR){
 			if(!env.GlobalExist((String)func.value)){
 				return new SimPLSymbol(ValueType.EXCEPTION, "var "+func.value+" is not defined");
 			}else
 				func = env.GlobalGetSymbol(func.value.toString());
 		}
 		if(func.type == ValueType.FREE){
 			return new SimPLSymbol(ValueType.FREE);
 		}
 		if (func.type == ValueType.FUN) {
 			MyFunc f = (MyFunc) (func.value);
 			if(param.type == ValueType.FREE){
 				param = new SimPLSymbol(f.paramType);
 				env.GlobalSetSymbol(paramName, param);
 			}
 			if(f.level == 0) {
 				SimPLSymbol var = f.param;
 				env.EnterBlock();
 				env.LocalSetSymbol(var.value.toString(), param);
 				SimPLSymbol exp = (SimPLSymbol)f.node.jjtAccept(this, data);
 				
 				if(exp.type == ValueType.VAR){
 					exp = env.GlobalGetSymbol(exp.value.toString());
 				}
 				env.LeaveBlock();
 				int nests = env.PopStackToDepth(depth);
 				if(nests>0)
 					System.out.println("nests is "+nests);
 				return exp;
 			}else {
 				
 				SimPLSymbol var = f.param;
 				//env.EnterBlock();
 				env.LocalSetSymbol(var.value.toString(), param);
 				return f.body;
 			}
 			
 		}else {
 			return new SimPLSymbol(ValueType.EXCEPTION,"application need fun type");
 		}
 		
 	}
 
 	/* (non-Javadoc)
 	 * @see com.simPL.compiler.SIMPLVisitor#visit(com.simPL.compiler.ASTFunction, java.lang.Object)
 	 */
 	@Override
 	public Object visit(ASTFunction node, Object data) {
 		// TODO Auto-generated method stub
 		//node.childrenAccept(this, data);
 		SimPLSymbol result = new SimPLSymbol(ValueType.FUN);
 		ValueType paramType = ValueType.FREE;
 		ValueType returnType = ValueType.FREE;
 		SimPLSymbol param = (SimPLSymbol)node.jjtGetChild(0).jjtAccept(this, data);
 		env.EnterBlock();
 		env.LocalSetSymbol(param.value.toString(), new SimPLSymbol(ValueType.FREE));
 		boolean backedup = false;
 		if(envbak == null) {
 			backedup = true;
 			envbak = env.Duplicate();
 		}
 		SimPLSymbol body = (SimPLSymbol)node.jjtGetChild(1).jjtAccept(this, data);
 		paramType = env.GlobalGetSymbol(param.value.toString()).type;
 		
 		if(body.type == ValueType.VAR)
 		{
 			if(!env.GlobalExist((String)body.value)){
 				return new SimPLSymbol(ValueType.EXCEPTION, "var "+body.value.toString()+" is not defined");
 			}else
 				body = env.GlobalGetSymbol(body.value.toString());
 		}
 		returnType = body.type;
 		if(backedup){
 			env = envbak;
 			envbak = null;
 		}
 		
 		
 		int level = 0;
 		if(body.type == ValueType.FUN)
 			level = ((MyFunc)(body.value)).level+1;
 		
 		result.value = new MyFunc(param,level,body,(SimpleNode)node.jjtGetChild(1),paramType,returnType);
 		env.LeaveBlock();
 		return result;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.simPL.compiler.SIMPLVisitor#visit(com.simPL.compiler.ASTVar, java.lang.Object)
 	 */
 	@Override
 	public Object visit(ASTVar node, Object data) {
 		// TODO Auto-generated method stub
 		node.childrenAccept(this, data);
 		SimPLSymbol result = new SimPLSymbol(ValueType.VAR);
 		result.value = node.jjtGetFirstToken().image;
 		return result;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.simPL.compiler.SIMPLVisitor#visit(com.simPL.compiler.ASTInt, java.lang.Object)
 	 */
 	@Override
 	public Object visit(ASTInt node, Object data) {
 		// TODO Auto-generated method stub
 		node.childrenAccept(this, data);
 		SimPLSymbol result = new SimPLSymbol(ValueType.INTEGER);
 		result.value = node.jjtGetFirstToken().image;
 		return result;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.simPL.compiler.SIMPLVisitor#visit(com.simPL.compiler.ASTBool, java.lang.Object)
 	 */
 	@Override
 	public Object visit(ASTBool node, Object data) {
 		// TODO Auto-generated method stub
 		node.childrenAccept(this, data);
 		SimPLSymbol result = new SimPLSymbol(ValueType.BOOLEAN);
 		result.value = node.jjtGetFirstToken().image;
 		//System.out.println("In Bool Node, return:"+result.value.toString());
 		return result;
 	}
 
 	@Override
 	public Object visit(ASTExpression node, Object data) {
 		// TODO Auto-generated method stub
 		//node.childrenAccept(this, data);
 		int num = node.jjtGetNumChildren();
 		Object result = null;
 		for(int i = 0; i < num;i++){
 			result = node.jjtGetChild(i).jjtAccept(this, data);
 			if(i!=num-1)
 				if(((SimPLSymbol)result).type!=ValueType.UNIT){
 					return new SimPLSymbol(ValueType.EXCEPTION, "in-final sequence expression should be unit");
 				}
 		}
 		return result;
 	}
 
 	@Override
 	public Object visit(ASTNil node, Object data) {
 		// TODO Auto-generated method stub
 		node.childrenAccept(this, data);
 		SimPLSymbol result = new SimPLSymbol(ValueType.LIST);
 		result.value = null;
 		//System.out.println("In Bool Node, return:"+result.value.toString());
 		return result;
 	}
 
 	@Override
 	public Object visit(ASTUnit node, Object data) {
 		// TODO Auto-generated method stub
 		
 		return new SimPLSymbol(ValueType.UNIT);
 		
 	}
 
 }
