 package ui.UmlClass;
 
 import static org.jhotdraw.draw.AttributeKeys.FILL_COLOR;
 import static org.jhotdraw.draw.AttributeKeys.FONT_BOLD;
 import static org.jhotdraw.draw.AttributeKeys.STROKE_COLOR;
 import static org.jhotdraw.draw.AttributeKeys.STROKE_DASHES;
 
 import java.awt.Font;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.geom.Point2D;
 import java.awt.geom.Rectangle2D;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.swing.Action;
 import org.jhotdraw.draw.AttributeKey;
 import org.jhotdraw.draw.AttributeKeys;
 import org.jhotdraw.draw.Figure;
 import org.jhotdraw.draw.GraphicalCompositeFigure;
 import org.jhotdraw.draw.LineConnectionFigure;
 import org.jhotdraw.draw.ListFigure;
 import org.jhotdraw.draw.RectangleFigure;
 import org.jhotdraw.draw.TextFigure;
 import javax.swing.AbstractAction;
 import org.jhotdraw.draw.event.FigureAdapter;
 import org.jhotdraw.draw.event.FigureEvent;
 import org.jhotdraw.draw.layouter.VerticalLayouter;
 import org.jhotdraw.geom.Insets2D;
 import org.jhotdraw.samples.pert.figures.SeparatorLineFigure;
 import org.jhotdraw.util.ResourceBundleUtil;
 import org.jhotdraw.xml.DOMInput;
 import org.jhotdraw.xml.DOMOutput;
 
 import domain.UmlClass.AccessModifier;
 import domain.UmlClass.AssociationType;
 import domain.UmlClass.UmlAssociationModel;
 import domain.UmlClass.UmlAttributeModel;
 import domain.UmlClass.UmlClassModel;
 import domain.UmlClass.UmlMethodModel;
 
 @SuppressWarnings("serial")
 public class UmlClassFigure extends GraphicalCompositeFigure {
 	private int uniqAttrId = 1;
 	private int uniqMethodId = 1;
 	
     private static class ClassNameAdapter extends FigureAdapter {
         private UmlClassFigure target;
 
         public ClassNameAdapter(UmlClassFigure target) {
             this.target = target;
         }
         
         @Override
         public void figureChanged(FigureEvent e) {
         	// VERIFY AND MAP USER INPUT TO MODEL
         	target.willChange();
         	TextFigure nameFigure = target.getNameFigure();        	        	
         	String text = nameFigure.getText();
         	
         	AccessModifier modelAccessModifier;
         	if (text.startsWith("+")) {
         		modelAccessModifier = AccessModifier.Public;
         	}
         	else if (text.startsWith("-")) {
         		modelAccessModifier = AccessModifier.Private;
         	}
         	else if (text.startsWith("#")) {
         		modelAccessModifier = AccessModifier.Protected;
         	}
         	else modelAccessModifier = AccessModifier.Public;
         	target.getModel().setAccessModifier(modelAccessModifier);
         	
         	Pattern p = Pattern.compile("([\\w]+\\.)*[\\w]+");
         	Matcher m = p.matcher(text);
         	m.find();      	
         	
         	if (m.group().length() > 0) target.getModel().setName(m.group());
         	else target.getModel().setName("newClass");
         	target.drawClass();
         	target.changed();
         }
     }
 
     private static class AttributeAdapter extends FigureAdapter {
         private UmlClassFigure target;
 
         public AttributeAdapter(UmlClassFigure target) {
             this.target = target;
         }//TODO: split out these huge blocks
         
         @Override
         public void figureChanged(FigureEvent e) {        	
         	// VERIFY AND MAP USER INPUT TO MODELS
         	// new strat: reinitialize the model's attr list; examine all text figures in  
         	// the compartment, map them into attrModels, then re-add them to the classModel
         	// if user entered bad data, we assign default values w/ uniqAttrId
         	target.willChange();
         	target.getAttributesCompartment().willChange();
         	List<Figure> attrFigures = target.getAttributesCompartment().getChildren();
         	Pattern p = Pattern.compile("([\\w]+)");	// regex for words
         	
         	// reinit class model's attrList
         	target.getModel().setAttributes(new ArrayList<UmlAttributeModel>());
         	for(Figure attrFigure : attrFigures) {
         		String attrText = ((TextFigure)attrFigure).getText();
         		
             	// assess <accessmod>
             	AccessModifier attrAccessModifier;
             	if (attrText.startsWith("+")) {
             		attrAccessModifier = AccessModifier.Public;
             	}
             	else if (attrText.startsWith("-")) {
             		attrAccessModifier = AccessModifier.Private;
             	}
             	else if (attrText.startsWith("#")) {
             		attrAccessModifier = AccessModifier.Protected;
             	}
             	else attrAccessModifier = AccessModifier.Private;
         		
         		// assess <name> : <type>
         		String attrType;
         		String attrName;
             	if (attrText.indexOf(':') == -1) {
             		attrType = "Object";
             		
             		Matcher m = p.matcher(attrText.substring(0));
             		m.find();
             		attrName = m.group();
             	}
             	else {
             		//TODO: use fully qualified java identifier pattern
                 	Matcher m = p.matcher(attrText.substring(attrText.indexOf(':')));
                 	m.find();
                 	attrType = m.group();
             		
                 	m = p.matcher(attrText.substring(0, attrText.indexOf(':')));
                 	m.find();
                 	attrName = m.group();
             	}
             	
             	UmlAttributeModel remappedAttr = new UmlAttributeModel(attrAccessModifier, attrName, attrType);
             	try {
             		target.getModel().addAttribute(remappedAttr);
             	} catch (Exception mapException) {
             		// something failed, supply defaults for user revision
             		remappedAttr = new UmlAttributeModel
             				(AccessModifier.Private, "newAttribute" + target.uniqAttrId, "Object");
             	}
             	attrFigure.willChange();
             	((TextFigure)attrFigure).setText
             		(attrAccessModifier.getSymbol() + " " + remappedAttr.getName() + " : " + remappedAttr.getType());
             	attrFigure.changed();
         	}
         	target.getAttributesCompartment().changed();
         	target.changed();
     	}
     }
     
     private static class MethodAdapter extends FigureAdapter {
         private UmlClassFigure target;
 
         public MethodAdapter(UmlClassFigure target) {
             this.target = target;
         }
 
         @Override
         public void figureChanged(FigureEvent evt) {
         	// VERIFY AND MAP USER INPUT TO MODELS
         	// new strat: reinitialize the model's method list; examine all text figures in  
         	// the compartment, map them into methodModels, then re-add them to the classModel
         	// if user entered bad data, we assign default values w/ uniqMethodId
         	target.willChange();
         	target.getMethodsCompartment().willChange();
         	List<Figure> methodFigures = target.getMethodsCompartment().getChildren();
         	Pattern p = Pattern.compile("([\\w]+)");	// regex for words
         	
         	// reinit class model's methodList
         	target.getModel().setMethods(new ArrayList<UmlMethodModel>());
         	for(Figure methodFigure : methodFigures) {
         		String methodText = ((TextFigure)methodFigure).getText();
         		
             	// assess <accessmod>
             	AccessModifier methodAccessModifier;
             	if (methodText.startsWith("+")) {
             		methodAccessModifier = AccessModifier.Public;
             	}
             	else if (methodText.startsWith("-")) {
             		methodAccessModifier = AccessModifier.Private;
             	}
             	else if (methodText.startsWith("#")) {
             		methodAccessModifier = AccessModifier.Protected;
             	}
             	else methodAccessModifier = AccessModifier.Private;
         		
         	// assess <name>(<param>:<paramType>) : <type>
         	String methodType;
         	String methodName;
         	int paramCount = 0;
         	ArrayList<UmlAttributeModel> params = new ArrayList<UmlAttributeModel>();
         		
         	//if user input does not have a valid set of open and close parentheses or has no colons,
         	//assign default values
         	if (methodText.indexOf('(') == -1 || methodText.indexOf(')') == -1 || 
         			methodText.indexOf(':') == -1 || methodText.indexOf(')') < methodText.indexOf('(')) {
         		methodType = "Object";
         		
         		Matcher m = p.matcher(methodText.substring(0));
         		m.find();
         		methodName = m.group();
         	}
         	else {
         		String nameSubstring = methodText.substring(0, methodText.indexOf('('));
         		String paramSubstring = methodText.substring(methodText.indexOf('(') + 1,
         						methodText.indexOf(')'));
 			String afterParen = methodText.substring(methodText.indexOf(')'));
 			if (afterParen.indexOf(':') == -1){
 				methodType = "Object";	
 			}
 			else {
        			String typeSubstring = afterParen.substring(methodText.indexOf(':' + 1));
         			
         			Matcher m = p.matcher(typeSubstring);
         			m.find();
         			methodType = m.group();
 			}
 
         		Matcher m = p.matcher(nameSubstring);
         		m.find();
         		methodName = m.group();
         			
         		int commaIndex = paramSubstring.indexOf(',');
         		String paramName;
         		String paramType;
        			
         		//case for only 1 param; the rest of this could probably be more elegant but
         		//this should do for our purposes.
        		if (commaIndex == -1){
         			paramCount = 1;
         			if (paramSubstring.indexOf(':') == -1){
         				
         				m = p.matcher(paramSubstring);
         				m.find();
         				paramName = m.group();
         				
         				paramType = "Object";
         			}
         			else {
         				m = p.matcher(paramSubstring.substring(0, paramSubstring.indexOf(':')));
         				m.find();
         				paramName = m.group();
         				
         				m = p.matcher(paramSubstring.substring(paramSubstring.indexOf(':')));
         				m.find();
         				paramType = m.group();
         			}
     				params.add(new UmlAttributeModel(AccessModifier.Private, paramName, paramType));
         		}
        		//case for multiple params
         		else {
         			int preLength = paramSubstring.length();
         			String removedCommas = paramSubstring.replaceAll(",","");
         			paramCount = preLength - removedCommas.length() + 1;
         			String curString;
         			int curBegIndex = 0;
         			
         			for (int i = 1; i < paramCount; i++){
         				curString = paramSubstring.substring(curBegIndex, paramSubstring.indexOf(',', curBegIndex));
         				if (curString.indexOf(':') == -1) {
            					m = p.matcher(curString);
            					m.find();
            					paramName = m.group();
             					
            					paramType = "Object";
         				}
         				else {
            					m = p.matcher(paramSubstring.substring(0, curString.indexOf(':')));
            					m.find();
            					paramName = m.group();
            					
            					m = p.matcher(paramSubstring.substring(curString.indexOf(':')));
            					m.find();
            					paramType = m.group();
         				}
         				params.add(new UmlAttributeModel(AccessModifier.Private, paramName, paramType));
         				curBegIndex = paramSubstring.indexOf(',', curBegIndex);
         			}
         		}        			
         	}
         		
             	
            	UmlMethodModel remappedMethod = new UmlMethodModel(methodAccessModifier, methodType, methodName, params);
             	try {
             		target.getModel().addMethod(remappedMethod);
             	} catch (Exception mapException) {
             		// something failed, supply defaults for user revision
             		remappedMethod = new UmlMethodModel
             				(AccessModifier.Public, "Object", "newMethod" + target.uniqMethodId, new ArrayList<UmlAttributeModel>());
             		target.getModel().addMethod(remappedMethod);
             		++target.uniqMethodId;
             	}
             	String paramString = "";
             	for (UmlAttributeModel parameter : params){
             		paramString = parameter.getName() + " : " + parameter.getType() + ", ";
             	}
             	methodFigure.willChange();
             	((TextFigure)methodFigure).setText
             		(methodAccessModifier.getSymbol() + " " + remappedMethod.getName() + "(" + paramString +
             									")" + " : " + remappedMethod.getReturnType());
             	methodFigure.changed();
         	}
         	target.getMethodsCompartment().changed();
         	target.changed();
         }
     }
     
     private HashSet<AssociationFigure> associations; // TODO: consider removing
 	private UmlClassModel model;
 	
     public UmlClassModel getModel() {
         return model;
     }
     protected void setModel(UmlClassModel newModel) {
     	model = newModel;
     }
     
     public UmlClassFigure() {
         super(new RectangleFigure());
         setModel(new UmlClassModel());
         
         setLayouter(new VerticalLayouter());
         RectangleFigure nameCompartmentPF = new RectangleFigure();
         nameCompartmentPF.set(STROKE_COLOR, null);
         nameCompartmentPF.set(FILL_COLOR, null);
         RectangleFigure attributesCompartmentPF = new RectangleFigure();
         attributesCompartmentPF.set(STROKE_COLOR, null);
         attributesCompartmentPF.set(FILL_COLOR, null);
         RectangleFigure methodsCompartmentPF = new RectangleFigure();
         methodsCompartmentPF.set(STROKE_COLOR, null);
         methodsCompartmentPF.set(FILL_COLOR, null);
         
         ListFigure nameCompartment = new ListFigure(nameCompartmentPF); 			// child 0
         SeparatorLineFigure separator1 = new SeparatorLineFigure();					// child 1
         ListFigure attributesCompartment = new ListFigure(attributesCompartmentPF);	// child 2
         SeparatorLineFigure separator2 = new SeparatorLineFigure();					// child 3
         ListFigure methodsCompartment = new ListFigure(methodsCompartmentPF);		// child 4
 
         add(nameCompartment);
         add(separator1);
         add(attributesCompartment);
         add(separator2);
         add(methodsCompartment);
 
         Insets2D.Double insets = new Insets2D.Double(4, 8, 4, 8);
         nameCompartment.set(LAYOUT_INSETS, insets);
         attributesCompartment.set(LAYOUT_INSETS, insets);
         methodsCompartment.set(LAYOUT_INSETS, insets);
 
         TextFigure nameFigure = new TextFigure("+ newClass");
         nameCompartment.add(nameFigure);
         //nameFigure.addFigureListener(new ClassNameAdapter(this));
         
         String attrName = "newAttribute";
         String attrType = "Object";
         AccessModifier attrAccessMod = AccessModifier.Private;
         TextFigure attrFigure = new TextFigure(attrAccessMod.getSymbol() + " " + attrName + " : " + attrType);
         getModel().addAttribute(new UmlAttributeModel(attrAccessMod, attrName, attrType));        
         attributesCompartment.add(attrFigure);
         //attrFigure.addFigureListener(new AttributeAdapter(this));
         
         AccessModifier methodAccessMod = AccessModifier.Public;
         String methodName = "newMethod";
         String methodType = "Object";
         List<UmlMethodModel> methodList = new ArrayList<UmlMethodModel>();
         List<UmlAttributeModel> methodParams = new ArrayList<UmlAttributeModel>();
         methodList.add(new UmlMethodModel(methodAccessMod, methodType, methodName, methodParams));
         getModel().setMethods(methodList);
         methodsCompartment.add(new TextFigure(methodAccessMod.getSymbol() + " " + methodName + "(" + ")" + " : " + methodType));
         
         //ResourceBundleUtil labels = ResourceBundleUtil.getBundle("ui.UmlClass.Labels");
     }
     
     protected TextFigure getNameFigure() {
     	ListFigure nameCompartment = (ListFigure) this.getChild(0);
     	TextFigure nameFigure = (TextFigure) nameCompartment.getChild(0);
     	return nameFigure;
     }
     protected ListFigure getAttributesCompartment() {
     	ListFigure attributesCompartment = (ListFigure) this.getChild(2);
     	return attributesCompartment;
     }
     
     protected ListFigure getMethodsCompartment() {
     	ListFigure methodsCompartment = (ListFigure) this.getChild(4);
     	return methodsCompartment;
     }
 
 	/**
 	 * @precondition	nameFigureCompartment has been initialized
 	 * @postcondition	nameFigure has been updated with info from model
 	 */
     private void drawClass() {
     	String className = getModel().getName();
     	char classAccessMod = getModel().getAccessModifier().getSymbol();
     	getNameFigure().setText(classAccessMod + " " + className);
     }
 
 	/**
 	 * @precondition 	attributesCompartment has been initialized
 	 * @postcondition	new text figure has been added with info from model
 	 * @param 			attrModel	model info to display
 	 */
     private void drawAttribute(UmlAttributeModel attrModel) {
     	TextFigure attrFigure = new TextFigure(attrModel.getAccessModifier().getSymbol() + " " + attrModel.getName() + " : " + attrModel.getType());
     	getAttributesCompartment().add(attrFigure);
     	attrFigure.addFigureListener(new AttributeAdapter(this));
     }
     
     /**
 	 * @precondition 				methodsCompartment has been initialized
 	 * @postcondition				new text figure has been added with info from model
 	 * @param 			methodModel	model info to display
 	 */
     private void drawMethod(UmlMethodModel methodModel) {
     	String text = methodModel.getAccessModifier().getSymbol() + " " + methodModel.getName() + "(";
     	for (int i = 0; i < methodModel.getParameters().size(); ++i) {
     		if ( i == methodModel.getParameters().size() - 1) text += methodModel.getParameters().get(i).getType() + " " + methodModel.getParameters().get(i).getName();
     		else text += methodModel.getParameters().get(i).getType() + " " + methodModel.getParameters().get(i).getName() + ", ";
     	}
     	text += ") : " + methodModel.getReturnType();
     	TextFigure methodFigure = new TextFigure(text);
     	getMethodsCompartment().add(methodFigure);
     }
     
     @Override
     public UmlClassFigure clone() {
     	UmlClassFigure that = (UmlClassFigure) super.clone();
     	that.setModel(new UmlClassModel());
         that.associations = new HashSet<AssociationFigure>();
         
         List<Figure> attrFigureList = that.getAttributesCompartment().getChildren();
         for(int i = 0; i < attrFigureList.size(); ++i) {
         	((TextFigure)attrFigureList.get(i)).addFigureListener(new AttributeAdapter(that));
         }
         
         List<Figure> methodFigureList = that.getMethodsCompartment().getChildren();
         for(int i = 0; i < methodFigureList.size(); ++i) {
         	((TextFigure)methodFigureList.get(i)).addFigureListener(new MethodAdapter(that));
         }
         
         that.getNameFigure().addFigureListener(new ClassNameAdapter(that));
         
         return that;
     }
     
     @Override
     public void write(DOMOutput out) throws IOException {
         Rectangle2D.Double r = getBounds();
         out.addAttribute("x", r.x);
         out.addAttribute("y", r.y);
         writeAttributes(out);
         out.openElement("classModel");
         	out.openElement("classAccessModifier");
         		out.addText("" + getModel().getAccessModifier().getSymbol());
         		//out.writeObject(getModel().getAccessModifier());
         	out.closeElement();
         
 	        out.openElement("className");
 	        	out.addText(getModel().getName());
 	        out.closeElement();
 	        
 	        out.openElement("classAssociations");
 		        for(UmlAssociationModel assoc : getModel().getAssociations()) {
 		        	out.openElement("assoc");
 			        	out.openElement("assocType");
 			        		out.writeObject(assoc.getType());
 			        	out.closeElement();
 			        	
 			        	out.openElement("assocTarget");
 			        		out.writeObject(assoc.getTarget());
 			        	out.closeElement();
 		        	out.closeElement();
 		        }
 	        out.closeElement();
         
 	        out.openElement("classAttributes");
 		        for(UmlAttributeModel attr : getModel().getAttributes()) {
 		        	out.openElement("attr");
 			        	out.openElement("attrAccessModifier");
 			        		out.addText(""+attr.getAccessModifier().getSymbol());
 			        		//out.writeObject(attr.getAccessModifier());
 			        	out.closeElement();
 			        	
 			        	out.openElement("attrName");
 			        		out.addText(attr.getName());
 			        	out.closeElement();
 			        	
 			        	out.openElement("attrType");
 			        		out.addText(attr.getType());
 			        	out.closeElement();
 		        	out.closeElement();
 		        }
 	        out.closeElement();
         
 	        out.openElement("methods");
 		        for(UmlMethodModel method : getModel().getMethods()) {
 		        	out.openElement("method");
 			        	out.openElement("methodAccessModifier");
 			        		out.addText(""+method.getAccessModifier().getSymbol());
 			        	//out.writeObject(method.getAccessModifier());
 			        	out.closeElement();
 			        	
 			        	out.openElement("methodType");
 			        		out.addText(method.getReturnType());
 			        	out.closeElement();
 			        	
 			        	out.openElement("methodName");
 			        		out.addText(method.getName());
 			        	out.closeElement();
 			        	
 			        	out.openElement("params");
 				        	for(UmlAttributeModel param : method.getParameters()) {
 				        		out.openElement("param");
 					        		out.openElement("paramType");
 					        			out.addText(param.getType());
 					        		out.closeElement();
 					        		
 					        		out.openElement("paramName");
 					        			out.addText(param.getName());
 					        		out.closeElement();
 				        		out.closeElement();
 				        	}
 			        	out.closeElement();
 		        	out.closeElement();
 		        }
 	        out.closeElement();
 	        
         out.closeElement();
     }
 
     @Override
     public void read(DOMInput in) throws IOException {
     	// remove default text figures
     	this.willChange();
     	getAttributesCompartment().removeAllChildren();
     	getMethodsCompartment().removeAllChildren();
     	
         double x = in.getAttribute("x", 0d);
         double y = in.getAttribute("y", 0d);
         double w = in.getAttribute("w", 0d);
         double h = in.getAttribute("h", 0d);
         setBounds(new Point2D.Double(x, y), new Point2D.Double(x + w, y + h));
         readAttributes(in);
         in.openElement("classModel");
 	        in.openElement("classAccessModifier");
 	        	String classAccessMod = in.getText();	// workaround for enum issue
 	        	if (classAccessMod.equals("+")) getModel().setAccessModifier(AccessModifier.Public);
 	        	else if (classAccessMod.equals("-")) getModel().setAccessModifier(AccessModifier.Private);
 	        	else getModel().setAccessModifier(AccessModifier.Protected);
 	        	//getModel().setAccessModifier((AccessModifier)in.readObject());
 	        in.closeElement();
 	        in.openElement("className");
 	        	getModel().setName(in.getText());
 	        in.closeElement();
 	        drawClass();
 	        
 	        in.openElement("classAssociations");
 	        	List<UmlAssociationModel> assocList = new ArrayList<UmlAssociationModel>();
 	        	for (int i = 0; i < in.getElementCount("assoc"); ++i) {
 	        		in.openElement("assoc");
 			        	in.openElement("assocType");
 			        		AssociationType assocType = (AssociationType) in.readObject();
 			        	in.closeElement();
 			        	in.openElement("assocTarget");
 			        		UmlClassModel assocTarget = (UmlClassModel) in.readObject();
 			        	in.closeElement();
 		        	in.closeElement();
 		        	getModel().addAssociation(assocTarget, assocType);
 	        	}
 	        	getModel().setAssociations(assocList);
 	        in.closeElement();
 	        
 	        in.openElement("classAttributes");
 		        for(int i = 0; i < in.getElementCount("attr"); ++i) {
 		        	in.openElement("attr");
 			        	in.openElement("attrAccessModifier");
 			        		AccessModifier attrAccessMod;
 				        	String attrAccessModText = in.getText();	// workaround for enum issue
 				        	if (attrAccessModText.equals("+")) attrAccessMod = AccessModifier.Public;
 				        	else if (attrAccessModText.equals("-")) attrAccessMod = AccessModifier.Private;
 				        	else attrAccessMod = AccessModifier.Protected;
 			        		//AccessModifier attrAccessMod = (AccessModifier) in.readObject();
 			        	in.closeElement();
 			        	
 			        	in.openElement("attrName");
 			        		String attrName = in.getText();
 			        	in.closeElement();
 			        	
 			        	in.openElement("attrType");
 			        		String attrType = in.getText();
 			        	in.closeElement();
 		        	in.closeElement();
 		        	UmlAttributeModel attrModel = new UmlAttributeModel(attrAccessMod, attrName, attrType);
 		        	getModel().addAttribute(attrModel);
 		        	drawAttribute(attrModel);
 		        }
 	        in.closeElement();
 	        
 	        in.openElement("methods");
 	        List<UmlMethodModel> methods = new ArrayList<UmlMethodModel>();
 	        for(int i = 0; i < in.getElementCount("method"); ++i) {
 	        	in.openElement("method");
 		        	in.openElement("methodAccessModifier");
 		        		AccessModifier methodAccessMod;
 			        	String methodAccessModText = in.getText();	// workaround for enum issue
 			        	if (methodAccessModText.equals("+")) methodAccessMod = AccessModifier.Public;
 			        	else if (methodAccessModText.equals("-")) methodAccessMod = AccessModifier.Private;
 			        	else methodAccessMod = AccessModifier.Protected;
 		        		//AccessModifier methodAccessMod = (AccessModifier) in.readObject();
 		        	in.closeElement();
 		        	
 		        	in.openElement("methodType");
 		        		String methodType = in.getText();
 		        	in.closeElement();
 		        	
 		        	in.openElement("methodName");
 		        		String methodName = in.getText();
 		        	in.closeElement();
 		        	
 		        	in.openElement("params");
 		        	List<UmlAttributeModel> methodParams = new ArrayList<UmlAttributeModel>();
 			        	for (int j = 0; j < in.getElementCount("param"); ++j) {		        		
 			        		in.openElement("paramType");
 			        			String paramType = in.getText();
 			        		in.closeElement();
 			        		
 			        		in.openElement("paramName");
 			        			String paramName = in.getText();
 			        		in.closeElement();
 			        		methodParams.add(new UmlAttributeModel(null, paramName, paramType));
 			        	}
 		        	in.closeElement();
 	        	in.closeElement();
 	        	UmlMethodModel methodModel = new UmlMethodModel(methodAccessMod, methodType, methodName, methodParams);
 	        	methods.add(methodModel);
 	        	drawMethod(methodModel);
 	        }
 	        in.closeElement();
 	        getModel().setMethods(methods);
         in.closeElement();
         this.changed();
     }
         
     @Override
     public Collection<Action> getActions(Point2D.Double p) {
     	Collection<Action> actions = new ArrayList<Action>();
     	actions.add(new AbstractAction("Add Attribute") {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				willChange();
 		        // add a new attribute text figure
 				UmlAttributeModel attr = new UmlAttributeModel(AccessModifier.Private, "newAttribute" + uniqAttrId, "Object");
 				getModel().getAttributes().add(attr);
 				drawAttribute(attr);
 		        ++uniqAttrId;
 		        changed();
 			}
     	});
     	actions.add(new AbstractAction("Add Method") {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				willChange();
 				// add a new method text figure
 				UmlMethodModel method = new UmlMethodModel(AccessModifier.Public, "Object", "newMethod" + uniqMethodId, new ArrayList<UmlAttributeModel>());
 				drawMethod(method);
 	        	++uniqMethodId;
 	        	changed();
 			}
     	});
     	
     	actions.add(new AbstractAction("Generate Code Skeleton File") {
     		//public final static String id = "edit.addAttribute";
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				//willChange();
 				
 				UmlClassFigure figure = new UmlClassFigure();
 				String output = figure.getModel().toString();
 				
 				String fileName = (model.getName() + ".java");
 
 				try {
 
 					PrintWriter outStream = new PrintWriter(fileName);
 
 					outStream.write(output);
 					outStream.close();
 				
 				} catch (FileNotFoundException e1) {
 					e1.printStackTrace();
 				}
 
 			}
     	});
 
     	
     	return actions;        
     }
 }
