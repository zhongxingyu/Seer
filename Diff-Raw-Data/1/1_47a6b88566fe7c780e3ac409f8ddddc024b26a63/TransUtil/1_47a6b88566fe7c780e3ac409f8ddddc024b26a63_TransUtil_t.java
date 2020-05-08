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
 
 package org.caltoopia.codegen.transformer;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.caltoopia.codegen.UtilIR;
 import org.caltoopia.codegen.transformer.IrTransformer.IrPassTypes;
 import org.caltoopia.codegen.transformer.analysis.IrTypeStructureAnnotation.TypeMember;
 import org.caltoopia.codegen.transformer.analysis.IrVariableAnnotation.VarAccess;
 import org.caltoopia.codegen.transformer.analysis.IrVariableAnnotation.VarAssign;
 import org.caltoopia.codegen.transformer.analysis.IrVariableAnnotation.VarType;
 import org.caltoopia.codegen.transformer.analysis.IrVariablePlacementAnnotation.VarPlacement;
 import org.caltoopia.ir.AbstractActor;
 import org.caltoopia.ir.Annotation;
 import org.caltoopia.ir.AnnotationArgument;
 import org.caltoopia.ir.Expression;
 import org.caltoopia.ir.FunctionCall;
 import org.caltoopia.ir.IrFactory;
 import org.caltoopia.ir.ListExpression;
 import org.caltoopia.ir.LiteralExpression;
 import org.caltoopia.ir.Namespace;
 import org.caltoopia.ir.Node;
 import org.caltoopia.ir.Scope;
 import org.caltoopia.ir.Type;
 import org.caltoopia.ir.TypeConstructorCall;
 import org.caltoopia.ir.TypeList;
 import org.caltoopia.ir.TypeRecord;
 import org.caltoopia.ir.Variable;
 import org.caltoopia.ir.VariableExpression;
 import org.eclipse.emf.ecore.EObject;
 
 public class TransUtil {
 	static public Annotation createAnnotation(EObject obj, String name) {
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
 
     static public void setNamespaceAnnotation(Node node, Scope scope) {
         if(scope instanceof Namespace) {
             Annotation a = getAnnotation(node,"NAMESPACE");
             if(a==null) {
                 a = IrFactory.eINSTANCE.createAnnotation();
                 a.setName("NAMESPACE");
             }
             for(String s:((Namespace) scope).getName()) {
                 AnnotationArgument aa = IrFactory.eINSTANCE.createAnnotationArgument();
                 aa.setId("ns");
                 aa.setValue(s);
                 a.getArguments().add(aa);
             }
             node.getAnnotations().add(a);
         } else {
             //Just to be able to track when we miss a namespace annotation
             Annotation a = getAnnotation(node,"NAMESPACE");
             if(a==null) {
                 a = IrFactory.eINSTANCE.createAnnotation();
                 a.setName("NAMESPACE");
                 AnnotationArgument aa = IrFactory.eINSTANCE.createAnnotationArgument();
                 aa.setId("ns");
                 aa.setValue("MISSING_NAMESPACE");
                 a.getArguments().add(aa);
                 node.getAnnotations().add(a);
             }
         }
     }
 
     static public void copyNamespaceAnnotation(Node dst, Node src) {
         Annotation aSrc = getAnnotation(src,"NAMESPACE");
         if(aSrc==null) {
             return;
         }
         Annotation aDst = getAnnotation(dst,"NAMESPACE");
         if(aDst==null) {
             aDst = IrFactory.eINSTANCE.createAnnotation();
             aDst.setName("NAMESPACE");
         } else {
             //Don't overwrite
             return;
         }
         for(AnnotationArgument aaSrc: aSrc.getArguments()) {
             AnnotationArgument aaDst = IrFactory.eINSTANCE.createAnnotationArgument();
             aaDst.setId("ns");
             aaDst.setValue(aaSrc.getValue());
             aDst.getArguments().add(aaDst);
         }
         dst.getAnnotations().add(aDst);
     }
 
     static public String getNamespaceAnnotation(Node node) {
         Annotation a = getAnnotation(node,"NAMESPACE");
         if(a==null) {
             return "";
         } else {
             String ns = "";
             for(AnnotationArgument aa: a.getArguments()) {
                 ns += "__" + aa.getValue();
             }
             return ns;
         }
     }
 
     static public List<String> getNamespaceAnnotationList(Node node) {
         Annotation a = getAnnotation(node,"NAMESPACE");
         List<String> ret = new ArrayList<String>();
         if(a==null) {
             ret.add("");
             return ret;
         } else {
             for(AnnotationArgument aa: a.getArguments()) {
                 ret.add(aa.getValue());
             }
             return ret;
         }
     }
 
     static public Annotation getAnnotation(EObject obj, String name) {
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
 		return null;
 	}
 
     static public Boolean rmAnnotation(EObject obj, String name, String key) {
         List<Annotation> annotations = null;
         //Most obj is a node
         if(obj instanceof Node) {
             annotations = ((Node)obj).getAnnotations();
         } else {
             return false;
         }
         int rm = -1;
         for(Annotation a : annotations) {
             if(a.getName().equals(name)) {
                 List<AnnotationArgument> aas = a.getArguments();
                 for(int i = 0; i<aas.size();i++) {
                     if(aas.get(i).getId().equals(key)) {
                         rm = i;
                         break;
                     }
                 }
                 if(rm>=0) {
                     a.getArguments().remove(rm);
                     return true;
                 }
             }
         }
         return false;
     }
 
     static public String getAnnotationArg(EObject obj, String name, String key) {
 		Annotation a = getAnnotation(obj, name);
 		if(a!=null) {
 			for(AnnotationArgument aa : a.getArguments()) {
 				if(aa.getId().equals(key)) {
 					return aa.getValue();
 				}
 			}
 		}
 		if(key.equals("VarAccess"))
 			return VarAccess.unknown.name();
 		else if(key.equals("VarType"))
 			return VarType.unknown.name();
 		else
 			return "unknown";
 	}
 
 	public static void setAnnotation(EObject obj, String name, String key, String value) {
 		setAnnotation(createAnnotation(obj, name),key,value);
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
 
 	static public void AnnotatePass(Node node, IrPassTypes t, String result) {
 		setAnnotation(node,"AnnotationPasses",t.name(),result);
 	}
 
 	static public String getPath(AbstractActor actor) {
 		for(Annotation ann : actor.getAnnotations()) {
 			if(ann.getName().equals("Project")) {
 				for(AnnotationArgument aa : ann.getArguments()) {
 					if(aa.getId().equals("name")) {
 						return aa.getValue();
 					}
 				}
 			}
 		}
 		return null;
 	}
 	
 	public static String varAnn(String ann) {
 		return IrTransformer.VARIABLE_ANNOTATION + "_" + ann;
 	}
 
 	public static String typeAnn(String ann) {
 		return IrTransformer.TYPE_ANNOTATION + "_" + ann;
 	}
 	
 	static public Map<String,String> getAnnotationsMap(EObject obj) {
 		//Most obj is a node
 		if(obj instanceof Node) {
 			Annotation annotation = getAnnotation(obj,IrTransformer.VARIABLE_ANNOTATION);
 			Map<String,String> annotations = new HashMap<String,String>();
 			if(annotation != null) {
 				for(AnnotationArgument aa:annotation.getArguments()) {
 					annotations.put(varAnn(aa.getId()), aa.getValue());
 				}
 			}
 			annotation = getAnnotation(obj,IrTransformer.TYPE_ANNOTATION);
 			if(annotation != null) {
 				for(AnnotationArgument aa:annotation.getArguments()) {
 					annotations.put(typeAnn(aa.getId()), aa.getValue());
 				}
 			}
 			if(annotations.isEmpty())
 				return null;
 			else
 				return annotations;
 		}
 		return null;
 	}
 
 	static public void copyAnnotations(EObject dst, EObject src) {
 		//Copy all variable and type annotations on a node
 		if(dst instanceof Node && src instanceof Node) {
 			Annotation annotation = getAnnotation(src,IrTransformer.VARIABLE_ANNOTATION);
 			if(annotation != null) {
 				Annotation adest = IrFactory.eINSTANCE.createAnnotation();
 				adest.setName(IrTransformer.VARIABLE_ANNOTATION);
 				for(AnnotationArgument aa:annotation.getArguments()) {
 					AnnotationArgument aadest = IrFactory.eINSTANCE.createAnnotationArgument();
 					aadest.setId(aa.getId());
 					aadest.setValue(aa.getValue());
 					adest.getArguments().add(aadest);
 				}
 			}
 			annotation = getAnnotation(src,IrTransformer.TYPE_ANNOTATION);
 			if(annotation != null) {
 				Annotation adest = IrFactory.eINSTANCE.createAnnotation();
 				adest.setName(IrTransformer.TYPE_ANNOTATION);
 				for(AnnotationArgument aa:annotation.getArguments()) {
 					AnnotationArgument aadest = IrFactory.eINSTANCE.createAnnotationArgument();
 					aadest.setId(aa.getId());
 					aadest.setValue(aa.getValue());
 					adest.getArguments().add(aadest);
 				}
 			}
 		}
 	}
 
 	static public class AnnotationsFilter {
 	    public String annotation;
 	    public Set<String> args;
 	    public AnnotationsFilter(String a, String[] aa) {
 	        this.annotation = a;
 	        this.args = new HashSet<String>(Arrays.asList(aa));
 	    }
 	}
 	
    //Copy all variable and type annotations on a node that match the filter, overwrite exist with no duplication
    static public void copySelectedAnnotations(EObject dst, EObject src, AnnotationsFilter[] annotationsFilter) {
         if(dst instanceof Node && src instanceof Node) {
             for(AnnotationsFilter n: annotationsFilter) {
                 Annotation annotation = getAnnotation(src,n.annotation);
                 if(annotation != null) {
                     Annotation adest = getAnnotation(dst, n.annotation);
                     if(adest == null) {
                         adest = IrFactory.eINSTANCE.createAnnotation();
                         adest.setName(n.annotation);
                        ((Node)dst).getAnnotations().add(adest);
                     }
                     Map<String,AnnotationArgument> existingIds = new HashMap<String,AnnotationArgument>();
                     for(AnnotationArgument aa:adest.getArguments()) {
                         existingIds.put(aa.getId(),aa);
                     }
                     for(AnnotationArgument aa:annotation.getArguments()) {
                         if(n.args.contains(aa.getId())) {
                             AnnotationArgument aadest = null;
                             if(existingIds.containsKey(aa.getId())) {
                                 aadest = existingIds.get(aa.getId());
                                 aadest.setValue(aa.getValue());
                             } else {
                                 aadest = IrFactory.eINSTANCE.createAnnotationArgument();
                                 aadest.setId(aa.getId());
                                 aadest.setValue(aa.getValue());
                                 adest.getArguments().add(aadest);
                             }
                         }
                     }
                 }
             }
         }
     }
 
    static public void copySelectedAnnotations(EObject dst, EObject src, AnnotationsFilter annotationsFilter) {
        AnnotationsFilter[] af ={annotationsFilter};
        copySelectedAnnotations(dst,src,af);
    }
    
    static public class HowLiteral {
        public boolean list = false; //Has list expression
        public boolean typeConstruct = false; //Has typeconstructor
        public boolean literalTypeConstruct = false; //Has typeconstructor with only literal parameters
        public boolean builtin = false; //is literal of builtin type at top level
        public boolean total = true; //complete expression is literal (assuming type construct calls with only literal params are literals)
        public boolean containsListType = false; //Has a variable expression or function call that returns a list (i.e. more than one element)
    }
    
    static private HowLiteral literalList(ListExpression list, HowLiteral in) {
        if(list==null) return in;
        if(list.getGenerators().isEmpty() && !list.getExpressions().isEmpty()) {
            for(Expression e : list.getExpressions()) {
                if(e instanceof ListExpression) {
                    in = literalList((ListExpression)e,in);
                } else if(e instanceof TypeConstructorCall) {
                    in.typeConstruct = true;
                    in.literalTypeConstruct = true;
                    in = literalTypeConstruct((TypeConstructorCall) e,in);
                } else if(!(e instanceof LiteralExpression)) {
                    in.total = false;
                } else if(e instanceof VariableExpression) {
                    if(((VariableExpression)e).getType() instanceof ListExpression){
                        in.containsListType = true;
                    }
                } else if(e instanceof FunctionCall) {
                    if(((FunctionCall)e).getType() instanceof ListExpression){
                        in.containsListType = true;
                    }
                }
            }
        } else {
            in.total = false;
        }
        return in;
    }
 
    static private HowLiteral literalTypeConstruct(TypeConstructorCall call, HowLiteral in) {
        if(call==null) return in;
        if(!call.getParameters().isEmpty()) {
            for(Expression e : call.getParameters()) {
                if(e instanceof ListExpression) {
                    HowLiteral h = new HowLiteral();
                    h = literalList((ListExpression)e,h);
                    if(!h.total) {
                        in.total=false;
                        in.literalTypeConstruct = false;
                    }
                    if(h.list) {
                        in.list = true;
                    }
                } else if(e instanceof TypeConstructorCall) {
                    in = literalTypeConstruct((TypeConstructorCall) e,in);
                } else if(!(e instanceof LiteralExpression)) {
                    in.total = false;
                    in.literalTypeConstruct = false;
                }
            }
        } else {
            in.total = false;
        }
        return in;
    }
 
    static public HowLiteral isLiteralExpression(Expression expr) {
        HowLiteral h = new HowLiteral();
        if(expr instanceof LiteralExpression) {
            h.builtin = true;
            h.total = true;
            return h;
        } else if(expr instanceof ListExpression) {
            h.list = true;
            return literalList((ListExpression) expr,h);
        } else if(expr instanceof TypeConstructorCall) {
            h.typeConstruct = true;
            h.literalTypeConstruct = true;
            return literalTypeConstruct((TypeConstructorCall) expr,h);
        } else if(expr instanceof VariableExpression) {
            h.containsListType = ((VariableExpression)expr).getType() instanceof ListExpression;
        } else if(expr instanceof FunctionCall) {
            h.containsListType = ((FunctionCall)expr).getType() instanceof ListExpression;
        }
        h.total = false;
        return h;
    }
    
    static public boolean allFixedLength(Type type) {
        boolean ret = true;
        Type t=type;
        while(t instanceof TypeList) {
            if(((TypeList)t).getSize()==null) {
                ret = false;
            }
            t = ((TypeList)t).getType();
        }
        if(ret && UtilIR.isRecord(t)) {
            t = UtilIR.getType(t);
            for(Variable v: ((TypeRecord)t).getMembers()) {
                if(!allFixedLength(v.getType())) {
                    ret = false;
                    break;
                }
            }
        }
        return ret;
    }
 
 }
