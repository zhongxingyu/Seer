 /**
  * Copyright (c) 2013 HAN University of Applied Sciences
  * Arjan Oortgiese
  * Boyd Hofman
  * Joëll Portier
  * Michiel Westerbeek
  * Tim Waalewijn
  * 
  * Permission is hereby granted, free of charge, to any person
  * obtaining a copy of this software and associated documentation
  * files (the "Software"), to deal in the Software without
  * restriction, including without limitation the rights to use,
  * copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the
  * Software is furnished to do so, subject to the following
  * conditions:
  * 
  * The above copyright notice and this permission notice shall be
  * included in all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
  * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
  * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
  * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
  * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
  * OTHER DEALINGS IN THE SOFTWARE.
  */
 package nl.han.ica.ap.purify.language.java.callgraph.listeners;
 
 import nl.han.ica.ap.purify.language.java.JavaBaseListener;
 import nl.han.ica.ap.purify.language.java.JavaParser;
 import nl.han.ica.ap.purify.language.java.callgraph.CallGraph;
 
 /**
  * This listener checks for method calls and maps them to the given CallGraph.
  * 
  * @author Tim
  */
 public class EdgeListener extends JavaBaseListener {
 	private boolean methodfound, callfound;
 	
 	/** Name of the current class. */
 	private String classID;
 	
 	/** Name of the current method. */
 	private String methodID;
 	
 	/** The class of the call. */
 	private String currentCallClass;
 	
 	/** The current call. */
 	private String currentCall;
 	
 	/** Stores the previous call when in a callchain */
 	private String prevCall;
 	
 	/** Stores the previous call class when in a callchain */
 	private String prevCallClass;
 	
 	/** The CallGraph that is used. */
 	private CallGraph graph;
 	
 	public EdgeListener(CallGraph graph) {
 		this.graph = graph;
 	}
 	
 	/** 
 	 * First tree node the listener enters.
 	 * Initialize variables here to make parsing multiple files possible. 
 	 */
 	@Override
 	public void enterCompilationUnit(JavaParser.CompilationUnitContext ctx) {
 		methodfound = false;
 		callfound = false;
 	}
 	
 	/**
 	 * Called when entering class declaration.
 	 * Sets the current class name to classID.
 	 * If type != null this class extends another class.
 	 */
 	@Override
 	public void enterNormalClassDeclaration(JavaParser.NormalClassDeclarationContext ctx) {
 		classID = ctx.Identifier().getText();
 		if(ctx.type() == null) {	
 			//TODO: add 'extends' functionality here.
 		}
 	}
 	
 	/**
 	 * Detects if current ClassBodyDeclaration node has a method sub-tree.
 	 */
 	@Override
 	public void enterClassBodyDeclaration(JavaParser.ClassBodyDeclarationContext ctx) {
 		if(ctx.memberDecl().memberDeclaration() != null) {
 			if(ctx.memberDecl().memberDeclaration().fieldDeclaration() == null) {
 				methodfound = true;
 			}
 		} else {
 			if(ctx.memberDecl().classDeclaration() == null) {
 				methodfound = true;
 			}
 		}
 	}
 	
 	/**
 	 * If a method was found sets methodID to the current method name.
 	 */
 	@Override
 	public void enterMemberDecl(JavaParser.MemberDeclContext ctx) {
 		if(methodfound) {
 			if(ctx.memberDeclaration() == null) {
 				methodID = ctx.Identifier().getText();
 			} else if(ctx.memberDeclaration().methodDeclaration() != null) {
 				methodID = ctx.memberDeclaration().methodDeclaration().Identifier().getText();
 			}
 		}
 	}
 	
 	/**
 	 * If exiting a method sets methodfound to false.
 	 */
 	@Override
 	public void exitMemberDecl(JavaParser.MemberDeclContext ctx) {
 		if(methodfound) {
 			methodfound = false;
 		}
 	}
 	
 	/**
 	 * Called when entering parameter block.
 	 * Appends "( " to the current methodID.
 	 */
 	@Override
 	public void enterFormalParameters(JavaParser.FormalParametersContext ctx) {
 		if(methodfound) {
 			methodID = methodID + "( ";
 		}
 	}
 	
 	/**
 	 * Called when exiting parameter block.
 	 * Appends ")" to close current methodID and maps any variables found.
 	 */
 	@Override
 	public void exitFormalParameters(JavaParser.FormalParametersContext ctx) {
 		if(methodfound) {
 			methodID = methodID + ")";
 		}
 	}
 	
 	/**
 	 * Called when entering parameter declarations.
 	 * If a method was found add any variable types found to the stack and also append it to the methodID.
 	 * exmpl: testmethod(String s, int i) -> testmethod( String -> testmethod( String int
 	 */
 	@Override
 	public void enterFormalParameterDecls(JavaParser.FormalParameterDeclsContext ctx) {
 		if(methodfound) {
 			methodID = methodID + ctx.type().getText() + " ";
 		}
 	}
 	
 	/**
 	 * Called when entering a call.
 	 * Add beginning of parameter here in case there is no ExpressionList.
 	 */
 	@Override
 	public void enterExpressionMethodExpressionList(JavaParser.ExpressionMethodExpressionListContext ctx) {
 		currentCall = "(";
 	}
 	
 	/**
 	 * Called when exiting a call.
 	 * Formats the currentCall and gets the proper callClass.
 	 * Adds current call to the CallGraph.
 	 * Sets prevCall and prevCallClass in case of a callchain.
 	 */
 	public void exitExpressionMethodExpressionList(JavaParser.ExpressionMethodExpressionListContext ctx) {
 		String[] ss = ctx.getText().split("\\.");
 		currentCall = currentCall + " )";
 		if(ss.length == 1) {
 			currentCallClass = classID;
 			currentCall = ss[0].replaceAll("\\(.*\\)", "") + currentCall;
 		} else {
 			currentCallClass = getVariableType(ss[ss.length-2]);
			if(currentCallClass == null) {
 				currentCallClass = getCurrentCallClassType(prevCallClass, prevCall);
 				currentCall = currentCall.replace(prevCall, "(");
 			}
 			currentCall = ss[ss.length-1].replaceAll("\\(.*\\)", "") + currentCall;
 		}
 		graph.addEdge(classID, methodID, currentCallClass, currentCall);
 		prevCall = currentCall;
 		prevCallClass = currentCallClass;
 	}
 	
 	/**
 	 * Called when entering an expression list (parameter list etc.).
 	 * Sets callfound to true.
 	 */
 	@Override
 	public void enterExpressionList(JavaParser.ExpressionListContext ctx) {
 		callfound = true;
 	}
 	
 	/**
 	 * Called when exiting an expression list (parameter list etc.).
 	 * Sets callfound to false.
 	 */
 	@Override
 	public void exitExpressionList(JavaParser.ExpressionListContext ctx) {
 		callfound = false;
 	}
 	
 	/**
 	 * Called when entering a creator (new Object()).
 	 * Sets currentCallClass and currentCall to the type of the object.
 	 * Appends a ( to currentCall to indicate the start of parameters.
 	 * exmpl: new Demo() -> Demo Demo(
 	 */
 	@Override
 	public void enterCreator(JavaParser.CreatorContext ctx) {
 		if(ctx.createdName().classOrInterfaceType() != null) {
 			currentCallClass = ctx.createdName().classOrInterfaceType().getText();
 			currentCall = ctx.createdName().classOrInterfaceType().getText() + "(";
 		}
 	}
 	
 	/**
 	 * Called when exiting a creator (new Object()).
 	 * Appends " )" to currentCall to close the call.
 	 * If object was not created in a method use 'this' as method else use the methodID.
 	 * Tells the current CallGraph to create a new Edge.
 	 */
 	@Override
 	public void exitCreator(JavaParser.CreatorContext ctx) {
 		if(ctx.createdName().classOrInterfaceType() != null) {
 			currentCall = currentCall + " )";
 			
 			String source;			
 			if (!methodfound) {
 				source = "this";
 			} else {
 				source = methodID;
 			}
 			
 			graph.addEdge(classID, source, currentCallClass, currentCall);
 		}
 	}
 	
 	/**
 	 * Called when entering the name of a variable.
 	 * If this primary is in a call, get the type of this variable from the CallGraph.
 	 * Appends the type to currentCall. 
 	 */
 	@Override
 	public void enterPrimaryIdentifier(JavaParser.PrimaryIdentifierContext ctx) {
 		if(callfound) {
 			currentCall = currentCall + " " + getVariableType(ctx.getText());
 		}
 	}
 	
 	/**
 	 * Called when entering a string literal. ("stringliteral")
 	 * Appends " String" to the currentCall.
 	 */
 	@Override
 	public void enterLiteralString(JavaParser.LiteralStringContext ctx) {
 		if(callfound) {
 			currentCall = currentCall + " String";
 		}
 	}
 	
 	/**
 	 * Called when entering a integer literal. (25)
 	 * Appends " int" to the currentCall.
 	 */
 	@Override
 	public void enterLiteralInteger(JavaParser.LiteralIntegerContext ctx) {
 		if(callfound) {
 			currentCall = currentCall + " int";
 		}
 	}
 	
 	/**
 	 * Called when entering a character literal. ('A')
 	 * Appends " char" to the currentCall.
 	 */
 	@Override
 	public void enterLiteralCharacter(JavaParser.LiteralCharacterContext ctx) {
 		if(callfound) {
 			currentCall = currentCall + " char";
 		}
 	}
 
 	/**
 	 * Called when entering a boolean literal. (true/false)
 	 * Appends " boolean" to the currentCall.
 	 */
 	@Override
 	public void enterLiteralBoolean(JavaParser.LiteralBooleanContext ctx) {
 		if(callfound) {
 			currentCall = currentCall + " boolean";
 		}
 	}
 	
 	/**
 	 * Gets the type of the specified variable name from the CallGraph.
 	 * 
 	 * @param variableID Name of the variable to get the type of.
 	 * @return Type of the variable in a String.
 	 */
 	private String getVariableType(String variableID) {
 		String type = graph.getVariableType(classID, methodID, variableID);
 		if(type != null) {
 			return type;
 		}
 		return null;	
 	}
 	
 	/**
 	 * Gets the type of the specified variable name from the CallGraph.
 	 * 
 	 * @param variableID Name of the variable to get the type of.
 	 * @return Type of the variable in a String.
 	 */
 	private String getCurrentCallClassType(String classID, String methodID) {
 		String type = graph.getMethodReturnType(classID, methodID);
 		if(type != null) {
 			return type;
 		}
 		return null;	
 	}
 }
