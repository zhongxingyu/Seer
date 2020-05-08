 package fr.cg95.cvq.generator.plugins.fo;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.lang.StringUtils;
 
 import fr.cg95.cvq.generator.common.Autofill;
 import fr.cg95.cvq.generator.common.Condition;
 import fr.cg95.cvq.generator.common.Step;
 import fr.cg95.cvq.generator.common.Autofill.AutofillType;
 
 /**
  * @author rdj@zenexity.fr
  */
 public class ElementFo {
     
     public enum ElementTypeClass { SIMPLE, COMPLEX, COLLECTION; }
     
     private String label;
     private String name;
     private String javaFieldName;
     private String modelNamespace;
     
     private String type;
     private boolean mandatory = true;
     private String jsRegexp;
     
     private Integer minLength = 0;
     private Integer maxLength = 0;
     
     private String i18nPrefixCode;
     private String htmlClass;
     private String widget;
     private String[] enumValues;
 
     private ElementTypeClass typeClass;
  
     private boolean display;
 
     private String elementToDisplay;
     private String after;
     private String modifier;
     private int rows;
 
     private Step step;
     private List<Condition> conditions;
     private Autofill autofill;
     
     private List<ElementFo> elements;
     
     public ElementFo(String name, String requestAcronym) {
         this.name = name;
         this.javaFieldName = StringUtils.uncapitalize(name);
         this.i18nPrefixCode = requestAcronym + ".property." + this.javaFieldName;
         display = false;
     }
     
     public String getLabel() {
         return label;
     }
 
     public void setLabel(String label) {
         this.label = label;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
         this.javaFieldName = StringUtils.uncapitalize(name);
     }
     
     public String getJavaFieldName() {
         return javaFieldName;
     }
 
     public String getModelNamespace() {
         return modelNamespace;
     }
 
     public void setModelNamespace(String modelNamespace) {
         this.modelNamespace = modelNamespace;
     }
 
     public String getType() {
         return type;
     }
 
     public void setType(String type) {
         this.type = type;
     }
     
     public String getEnumValuesAsString() {
         if (enumValues == null )
             return null;
         String s = "[";
         for (int i = 0; i < enumValues.length; i++) {
             s += "'" + enumValues[i] + "'";
             if (i < enumValues.length - 1)
                 s += ",";
         }
         s += "]";
         return s;
     }
 
     public void setEnumValues(String[] enumValues) {
         this.enumValues = enumValues;
     }
 
     public String getQualifiedType() {
         return modelNamespace + "." + type;
     }
 
     public String getI18nPrefixCode() {
         return i18nPrefixCode;
     }
 
     public String getJsRegexp() {
         String s = "";
         if (jsRegexp != null)
             s += "regex=\"" + jsRegexp + "\""; 
         return s;
     }
 
     public void setJsRegexp(String jsRegexp) {
         this.jsRegexp = jsRegexp;
     }
 
     public void setMinLength(int minLength) {
         this.minLength = minLength;
     }
 
     public void setMaxLength(int maxLength) {
         this.maxLength = maxLength;
     }
     
     public String getLengthLimits() {
         String limits = "";
         if (maxLength != 0)
             limits += "maxLength=\"" + maxLength + "\""; 
         if (minLength != 0)
             limits += " minLength=\"" + minLength + "\""; 
         return limits;
     }
 
     // TODO - refactor 'htmlClass' and 'conditionClass' members in respectively 'labelClass' 'formFieldClass'
     // TODO - add jsRegExp validation feature
     public String getHtmlClass() {
         if (htmlClass == null)
             setHtmlClass();
         return htmlClass;
     }
     
     private void setHtmlClass() {
         this.htmlClass = getConditionsClass() + " " + getAutofillClass() + " ";
         if (jsRegexp != null)
             this.htmlClass += "validate-regex";
         else if (widget == null)
             return;
         else if (widget.equals("select"))
             this.htmlClass += "validate-not-first";
        else if (widget.equals("radio") || widget.equals("boolean"))
             this.htmlClass += "validate-one-required";
         else 
             this.htmlClass += "validate-" + widget;
     }
 
     public String getWidget() {
         return widget;
     }
 
     public void setWidget(String type) {
         if (widget != null) {
             // TODO - how to process element without xmlschema 'type' attribute
             if (type == null)
                 return;
             if (widget.equals("textarea") && !(type.equals("string") || type.equals("token")))
                 throw new RuntimeException("setWidget() - " +
                 		"<textarea /> can be only used for types {string, token}. [element: " + this.name + "]");
             return;
         }
         this.widget = StringUtils.uncapitalize(StringUtils.removeEnd(type, "Type"));
     }
     
     public String getTypeClass() {
         return typeClass.toString();
     }
 
     public void setTypeClass(ElementTypeClass typeClass) {
         this.typeClass = typeClass;
     }
     
     public boolean isMandatory() {
         return mandatory;
     }
 
     public void setMandatory(boolean mandatory) {
         this.mandatory = mandatory;
         if (conditions != null)
             for (Condition c : this.conditions)
                 if(c.isRequired())
                     this.mandatory = true;
     }
     
     public boolean isDisplay() {
         return display;
     }
 
     public void setDisplay(boolean display) {
         this.display = display;
     }
 
     public String getAfter() {
         return after;
     }
 
     public void setAfter(String after) {
         this.after = after;
     }
 
     // TODO - finish to implement <fo element="" /> attribute feature
     public String getElementToDisplay() {
         return elementToDisplay;
     }
 
     public void setElementToDisplay(String elementToDisplay) {
         this.elementToDisplay = elementToDisplay;
     }
 
     public String getModifier() {
         return modifier;
     }
 
     public void setModifier(String modifier) {
         this.modifier = modifier;
     }
 
     public String getRows() {
         String s = "";
         if (rows > 0)
             s += "rows=\"" + rows + "\""; 
         return s;
     }
 
     public void setRows(String rows) {
         if (rows == null)
             return;
         try {
             this.rows = Integer.valueOf(rows).intValue();
         } catch (NumberFormatException nfe) {
             throw new RuntimeException("setRows() - rows {"+ rows +"} is not an integer in element : " + name);
         }
     }
 
     public Step getStep() {
         return step;
     }
 
     public void setStep(Step step) {
         this.step = step;
     }
 
     // FIXME - manage condition and mandatory state of the element. separation of concerns ??
     // TODO - refactor 'htmlClass' and 'conditionClass' members in respectively 'labelClass' 'formFieldClass'
     public String getConditionsClass() {
         StringBuffer sb = new StringBuffer();
         sb.append(mandatory ? "required " : "");
         if (conditions != null) {
             for (Condition c : conditions)
                 sb.append("condition-" + c.getName() + "-" + c.getType() + " ");
         }
         return sb.toString().trim();
     }
     
     public String getListenerConditionsClass() {
         StringBuffer sb = new StringBuffer();
         sb.append(mandatory ? "required " : "");
         if (conditions != null) {
             for (Condition c : conditions)
                 if ( ! Condition.ConditionType.valueOf(c.getType().toUpperCase())
                         .equals(Condition.ConditionType.TRIGGER))
                     sb.append("condition-" + c.getName() + "-" + c.getType() + " ");
         }
         return sb.toString().trim();
     }
     
     public void setConditions(List<Condition> conditions) {
         this.conditions = conditions;
     }
 
     public List<Condition> getConditions() {
         return conditions;
     }
 
     public void addElement (ElementFo element) {
         if (elements == null)
             elements = new ArrayList<ElementFo>();
         elements.add(element);
     }
     
     public List<ElementFo> getElements() {
         return elements;
     }
 
     public Autofill getAutofill() {
         return autofill;
     }
 
     public void setAutofill(Autofill autofill) {
         this.autofill = autofill;
     }
 
     public String getAutofillClass() {
         StringBuffer sb = new StringBuffer();
         if (autofill != null) {
             sb.append("autofill-" + autofill.getName() + "-" + autofill.getType().name().toLowerCase());
             if (autofill.getType().equals(AutofillType.LISTENER)) {
                 sb.append("-" + autofill.getField());
             }
         }
         return sb.toString();
     }
 }
