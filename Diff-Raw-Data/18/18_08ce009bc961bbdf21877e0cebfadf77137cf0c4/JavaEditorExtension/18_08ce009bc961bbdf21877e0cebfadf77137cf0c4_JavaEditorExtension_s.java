 package jnome.editor;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import jnome.core.modifier.Default;
 
 import org.eclipse.swt.graphics.Image;
 
 import chameleon.core.element.Element;
 import chameleon.core.method.Method;
 import chameleon.core.modifier.Modifier;
 import chameleon.core.namespace.Namespace;
 import chameleon.core.namespacepart.NamespacePart;
 import chameleon.core.variable.FormalParameter;
import chameleon.core.variable.RegularMemberVariable;
 import chameleon.editor.connector.EclipseEditorExtension;
 import chameleon.exception.ModelException;
import chameleon.oo.type.Type;
 import chameleon.output.Syntax;
 import chameleon.support.modifier.Abstract;
 import chameleon.support.modifier.Constructor;
 import chameleon.support.modifier.Final;
 import chameleon.support.modifier.Interface;
 import chameleon.support.modifier.Native;
 import chameleon.support.modifier.Private;
 import chameleon.support.modifier.Protected;
 import chameleon.support.modifier.Public;
 import chameleon.support.modifier.Static;
 import chameleon.tool.Connector;
 
 /**
  * @author Marko van Dooren
  * @author Koen Vanderkimpen
  */
 public class JavaEditorExtension extends EclipseEditorExtension {
 	
 //    public JavaEditorExtension(ChameleonProjectNature nature) {
 //		 super(nature);
 //	  }
 
 		public String getLabel(Element element) {
   		String result;
             if (element instanceof Method) {
                 Method<? , ? , ? ,?> method = (Method<?,?,?,? >)element;
                 result = method.name();
                 List<FormalParameter> params = method.formalParameters();
                 result += "(";
                 if (params.size()>0) {
                     for (int i = 0;i<params.size();i++) {
                         FormalParameter p = params.get(i);
                         try {
 													result += element.language().connector(Syntax.class).toCode(p.getTypeReference());
 												} catch (ModelException e) {
 													// TODO Auto-generated catch block
 													e.printStackTrace();
 												}
                         if (i<params.size()-1) {
                         	result += ",";
                         }
                     }
                 }
                 result += ")";
            } else if (element instanceof RegularMemberVariable) {
                result = ((RegularMemberVariable)element).getName();
            } else if (element instanceof Type) {
                result = ((Type)element).getName();
            } else if (element instanceof NamespacePart) {
             	Namespace namespace = ((NamespacePart)element).namespace();
             	if(namespace != null) {
             		result = namespace.getFullyQualifiedName();
             	} else {
             		result = "Error in namespace declaration.";
             	}
             } else {
             	result = "";
             }
         return result;
     }
 
     public Connector clone() {
         return new JavaEditorExtension();
     }
 
       @Override
       public List<Modifier> getFilterModifiers() {
               List<Modifier> result = new ArrayList<Modifier>();
               result.add(new Private());
               result.add(new Protected());
               result.add(new Public());
               result.add(new Default());
               result.add(new Static());
               result.add(new Final());
               result.add(new Abstract());
               result.add(new Constructor());
               result.add(new Interface());
               result.add(new Native());
               return result;
       }
 
 		@Override
 		public String getMethodTemplatePattern(Method method) {
       String methodName = method.name();
       String patternString = methodName+"(";
       List<FormalParameter> parameters = method.formalParameters();
       for(FormalParameter param : parameters){
               patternString += "${" + param.getName() + "}, ";
       }
       if(parameters.size()>0){
               // remove last comma added:
               patternString = patternString.substring(0, patternString.length()-2);
       }
       patternString += ")";
       return patternString;
 		}
 
 		@Override
 		public JavaDeclarationCategorizer declarationCategorizer()  {
 			return new JavaDeclarationCategorizer();
 		}
 
 		@Override
 		public Image getIcon(Element modelObject) {
 			return null;
 		}
 
 }
