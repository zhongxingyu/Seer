 /* 
  * Copyright (c) Ericsson AB, 2013
  * All rights reserved.
  *
  * License terms:
  *
  * Redistribution and use in source and binary forms, 
  * with or without modification, are permitted provided 
  * that the following conditions are met:
  *     * Redistributions of source code must retain the above 
  *       copyright notice, this list of conditions and the 
  *       following disclaimer.
  *     * Redistributions in binary form must reproduce the 
  *       above copyright notice, this list of conditions and 
  *       the following disclaimer in the documentation and/or 
  *       other materials provided with the distribution.
  *     * Neither the name of the copyright holder nor the names 
  *       of its contributors may be used to endorse or promote 
  *       products derived from this software without specific 
  *       prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND 
  * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, 
  * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
  * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR 
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT 
  * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR 
  * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS 
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package org.caltoopia.codegen.analysis;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.caltoopia.ast2ir.Instantiator;
 import org.caltoopia.ast2ir.Util;
 import org.caltoopia.cli.ActorDirectory;
 import org.caltoopia.cli.CompilationSession;
 import org.caltoopia.cli.DirectoryException;
 import org.caltoopia.codegen.CPrinter;
 import org.caltoopia.codegen.IrXmlPrinter;
 import org.caltoopia.codegen.analysis.IrVariableAnnotation.VarAccess;
 import org.caltoopia.codegen.analysis.IrVariableAnnotation.VarType;
 import org.caltoopia.ir.AbstractActor;
 import org.caltoopia.ir.Actor;
 import org.caltoopia.ir.ActorInstance;
 import org.caltoopia.ir.Annotation;
 import org.caltoopia.ir.AnnotationArgument;
 import org.caltoopia.ir.ExternalActor;
 import org.caltoopia.ir.IrFactory;
 import org.caltoopia.ir.Network;
 import org.caltoopia.ir.Node;
 import org.caltoopia.ir.TypeActor;
 import org.caltoopia.ir.TypeDeclarationImport;
 import org.caltoopia.ir.TypeUser;
 import org.caltoopia.ir.util.IrReplaceSwitch;
 import org.caltoopia.types.TypeAnnotater;
 import org.caltoopia.types.TypeMatchDeclaration;
 import org.eclipse.emf.ecore.EObject;
 
 public class IrAnnotations {
 	//The different types of annotation passes that can be executed
 	public enum IrAnnotationTypes {
 		Init,
 		Variable,
 		Type
 	}
 	
 	/*
 	 * Constructor that runs the list of annotation passes on the top network
 	 * The Annotations passes gets a node typically a network or actor.
 	 * When a annotator need access to other nodes in the tree it needs 
 	 * to retrieve them from the $Transformed part of ActorDirectory
 	 */
 	//FIXME can store/retrieve elaborate network: public IrAnnotations(TypeActor nodeType, CompilationSession session, List<IrAnnotationTypes> passes) {
 	public IrAnnotations(TypeActor nodeType, CompilationSession session, List<IrAnnotationTypes> passes) {
 	//public IrAnnotations(Node node, CompilationSession session, List<IrAnnotationTypes> passes) {
 		//Apply the list of annotation passes in order
 		Node node=null;
 		for(IrAnnotationTypes p : passes) {
 			switch (p) {
 			case Variable:
 				System.out.println("[IrAnnotations] ********************************************************************");
 				System.out.println("[IrAnnotations] ********************************************************************");
 				System.out.println("[IrAnnotations] ************                     Variable                  *********");
 				System.out.println("[IrAnnotations] ********************************************************************");
 				System.out.println("[IrAnnotations] ********************************************************************");
 				node=getNode(nodeType,p);
 				new IrVariableAnnotation(node, session, true);
 				break;
 			case Type:
 				System.out.println("[IrAnnotations] ********************************************************************");
 				System.out.println("[IrAnnotations] ********************************************************************");
 				System.out.println("[IrAnnotations] ************                       Type                    *********");
 				System.out.println("[IrAnnotations] ********************************************************************");
 				System.out.println("[IrAnnotations] ********************************************************************");
 				node=getNode(nodeType,p);
 				new IrTypeAnnotation(node, session, true);
 				break;
 			}
 		}
 	}
 	
 	/*
 	 * Utility functions
 	 */
 	private Node getNode(TypeActor nodeType, IrAnnotationTypes p) {
 		Node node=null;
 		try {
 			System.out.println("[IrAnnotations] Get top network from storage.");
 			node=ActorDirectory.findTransformedActor(nodeType);
 		} catch (DirectoryException e) {
 			System.err.println("[IrAnnotate] Internal error could not get actor of type " + nodeType.toString() + " at pass " + p.toString());
 		}
 		return node;
 	}
 
 	/*
 	 * Function used to store the initial elaborated network 
 	 * and the instanciate actors into
 	 * the $Transformed part of the ActorDirectory.
 	 * Must be called before any passes.
 	 * FIXME does not work for the top network. Hence, back to in-memory tree.
 	 */
 	public static void IrTransformNetworkInit(Network network) {
 		System.out.println("[IrAnnotations] ********************************************************************");
 		System.out.println("[IrAnnotations] ********************************************************************");
 		System.out.println("[IrAnnotations] ************                       Init                    *********");
 		System.out.println("[IrAnnotations] ********************************************************************");
 		System.out.println("[IrAnnotations] ********************************************************************");
 		String path = null;
 		for(Annotation ann : network.getAnnotations()) {
 			if(ann.getName().equals("Project")) {
 				for(AnnotationArgument aa : ann.getArguments()) {
 					if(aa.getId().equals("name")) {
 						path = aa.getValue();
 						break;
 					}
 				}
 				if(path!=null)
 					break;
 			}
 		}
 		if(path==null) {
 			path="";
 		}
 	    //DEBUG
 		new IrReplaceSwitch() {
 			private Map<String,String> found = new HashMap<String,String>();
 			@Override	
 			public TypeUser caseTypeUser(TypeUser type) {
 				found.put(((TypeUser) type).getDeclaration().getId(),(((TypeUser) type).getDeclaration() instanceof TypeDeclarationImport?"I_":"R_") +((TypeUser) type).getDeclaration().getName());
 				return type;
 			}
 
 			@Override
 			public AbstractActor caseNetwork(Network network) {
 				super.caseNetwork(network);
 				System.out.println("[IrAnnotations] ----- Found type declarations usage direcly after elaboration -----");
 				for(String f:found.keySet()) {
 					System.out.println("[IrAnnotations] Found type declaration " + f + " " + found.get(f));
 				}
 				return network;
 			}
 		}.doSwitch(network);
 		//End DEBUG
 
 		new TypeAnnotater().doSwitch(network);
 		new TypeMatchDeclaration().doSwitch(network);
 		
 	    //DEBUG
 		new IrReplaceSwitch() {
 			private Map<String,String> found = new HashMap<String,String>();
 			@Override	
 			public TypeUser caseTypeUser(TypeUser type) {
 				found.put(((TypeUser) type).getDeclaration().getId(),(((TypeUser) type).getDeclaration() instanceof TypeDeclarationImport?"I_":"R_") +((TypeUser) type).getDeclaration().getName());
 				return type;
 			}
 
 			@Override
 			public AbstractActor caseNetwork(Network network) {
 				super.caseNetwork(network);
 				System.out.println("[IrAnnotations] ----- Found type declarations usage direcly after network type annotation -----");
 				for(String f:found.keySet()) {
 					System.out.println("[IrAnnotations] Found type declaration " + f + " " + found.get(f));
 				}
 				return network;
 			}
 		}.doSwitch(network);
 		//End DEBUG
 	    /*//DEBUG
 		new IrReplaceSwitch() {
 			@Override	
 			public TypeUser caseTypeUser(TypeUser t) {
 				if (t.getDeclaration() instanceof TypeDeclarationImport) {
 					System.out.println("[IrAnnotations] DEBUG Is STILL2 network imported TypeDeclaration " + t.getDeclaration().getName());
 				}
 				return t;
 			}
 
 			@Override	
 			public TypeDeclarationImport caseTypeDeclarationImport(TypeDeclarationImport t) {
 				System.out.println("[IrAnnotations] DEBUG Is STILL2 network imported TypeDeclaration " + t.getName());
 				return t;
 			}
 		}.doSwitch(network);
 		//End DEBUG
 		 */
 		IrAnnotations.AnnotatePass(network, IrAnnotationTypes.Init, "0");
 		System.out.println("[IrTransformNetworkInit] Write network  " + network.getType().getName());
 		ActorDirectory.addTransformedActor(network, null, path);
 
 	    //DEBUG
 		new IrReplaceSwitch() {
 			private Map<String,String> found = new HashMap<String,String>();
 			@Override	
 			public TypeUser caseTypeUser(TypeUser type) {
 				found.put(((TypeUser) type).getDeclaration().getId(),(((TypeUser) type).getDeclaration() instanceof TypeDeclarationImport?"I_":"R_") +((TypeUser) type).getDeclaration().getName());
 				return type;
 			}
 
 			@Override
 			public AbstractActor caseNetwork(Network network) {
 				super.caseNetwork(network);
 				System.out.println("[IrAnnotations] ----- Found type declarations usage direcly after printing -----");
 				for(String f:found.keySet()) {
 					System.out.println("[IrAnnotations] Found type declaration " + f + " " + found.get(f));
 				}
 				return network;
 			}
 		}.doSwitch(network);
 		//End DEBUG
 		/*//DEBUG
 		new IrReplaceSwitch() {
 			@Override	
 			public TypeUser caseTypeUser(TypeUser t) {
 				if (t.getDeclaration() instanceof TypeDeclarationImport) {
 					System.out.println("[IrAnnotations] DEBUG Is STILL3 network imported TypeDeclaration " + t.getDeclaration().getName());
 				}
 				return t;
 			}
 
 			@Override	
 			public TypeDeclarationImport caseTypeDeclarationImport(TypeDeclarationImport t) {
 				System.out.println("[IrAnnotations] DEBUG Is STILL3 network imported TypeDeclaration " + t.getName());
 				return t;
 			}
 		}.doSwitch(network);
 		//End DEBUG
 		 */
 		for(ActorInstance a : network.getActors()) {
 			AbstractActor actor=null;
 			try {
 				System.out.println("[IrTransformNetworkInit] Read in actor class " + ((TypeActor) a.getType()).getName());
 				actor = (AbstractActor) ActorDirectory.findActor((TypeActor) a.getType());
 			} catch (DirectoryException x) {
 				System.err.println("[IrAnnotate] Internal error could not get actor of type " + a.getType().toString() + " at init.");
 			}
 			if(actor!=null && !(actor instanceof ExternalActor)) {
 				path = null;
 				for(Annotation ann : actor.getAnnotations()) {
 					if(ann.getName().equals("Project")) {
 						for(AnnotationArgument aa : ann.getArguments()) {
 							if(aa.getId().equals("name")) {
 								path = aa.getValue();
 								break;
 							}
 						}
 						if(path!=null)
 							break;
 					}
 				}
 				if(path==null) {
 					path="";
 				}
 				AbstractActor actorInstantiated = Instantiator.instantiate(a, network);
 				IrAnnotations.AnnotatePass(actorInstantiated, IrAnnotationTypes.Init, "0");
 				String nsName = Util.marshallQualifiedName(((TypeActor) a.getType()).getNamespace());
 				System.out.println("[IrTransformNetworkInit] Write actor instance named " + 
 									nsName + "__" + a.getName() + 
 									" of class " + ((TypeActor) a.getType()).getName());
 				//FIXME need to print into file named by the instance
 				ActorDirectory.addTransformedActor(actorInstantiated, a, path);
 			}
 		}
 	}
 	
 	/*
 	 * Utility functions for handling IR annotations
 	 */
 	static public final String VARIABLE_ANNOTATION = "Variable";
 	static public final String TYPE_ANNOTATION = "Type";
 	static public Annotation getAnalysAnnotations(EObject obj, String name) {
 		List<Annotation> annotations = null;
 		//Most obj is a node
 		if(obj instanceof Node) {
 			annotations = ((Node)obj).getAnnotations();
 		} else {
 			return null;
 		}
 		for(Annotation a : annotations) {
 			if(a.getName().equals(name)) {
 				return a;
 			}
 		}
 		//No analyze annotation yet but we should have one
 		Annotation a = IrFactory.eINSTANCE.createAnnotation();
 		a.setName(name);
 		annotations.add(a);
 		return a;
 	}
 	static public String getAnnotationArg(EObject obj, String name, String key) {
 		Annotation a = getAnalysAnnotations(obj, name);
 		for(AnnotationArgument aa : a.getArguments()) {
 			if(aa.getId().equals(key)) {
 				return aa.getValue();
 			}
 		}
 		if(key.equals("VarAccess"))
 			return VarAccess.unknown.name();
 		else if(key.equals("VarType"))
 			return VarType.unknown.name();
 		else
 			return "unknown";
 	}
 
 	public static void setAnnotation(Annotation a, String key, String value) {
 		for(AnnotationArgument aa : a.getArguments()) {
 			if(aa.getId().equals(key)) {
 				aa.setValue(value);
 				return;
 			}
 		}
 		AnnotationArgument aa = IrFactory.eINSTANCE.createAnnotationArgument();
 		aa.setId(key);
 		aa.setValue(value);
 		a.getArguments().add(aa);
 	}
 
 	static public void AnnotatePass(Node node, IrAnnotationTypes t, String result) {
		boolean done=false;
		for(Annotation ann : node.getAnnotations()) {
			if(ann.getName().equals("AnnotationPasses")) {
				for(AnnotationArgument aa : ann.getArguments()) {
					if(aa.getId().equals(t.name())) {
						aa.setValue(result);
						done=true;
						break;
					}
				}
				if(!done) {
					AnnotationArgument aa=IrFactory.eINSTANCE.createAnnotationArgument();
					aa.setId(t.name());
					aa.setValue(result);
					ann.getArguments().add(aa);
					done=true;
					break;
				}
			}
		}
 	}
 }
