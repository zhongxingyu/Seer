 package fr.cg95.cvq.generator.plugins.bo;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.lang.StringUtils;
 
 import fr.cg95.cvq.generator.common.Condition;
 import fr.cg95.cvq.generator.common.Step;
 
 /**
  * @author rdj@zenexity.fr
  */
 public class ElementBo {
     
     public enum ElementTypeClass { SIMPLE, COMPLEX, COLLECTION; }
     
     private String label;
     private String name;
     private String javaFieldName;
     private String modelNamespace;
     
     private String type;
     private boolean mandatory = true;
     private String jsRegexp;
     
     private String i18nPrefixCode;
     private String htmlClass;
     private String widget;
     private int rows;
     
     private ElementTypeClass typeClass;
  
     private boolean display;
 
     private int column;
     private String after;
 
     private Step step;
     private List<Condition> conditions;
     
     private List<ElementBo> elements;
     
     public ElementBo(String name, String requestAcronym) {
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
     
     public String getQualifiedType() {
         return modelNamespace + "." + type;
     }
 
     public String getI18nPrefixCode() {
         return i18nPrefixCode;
     }
 
     public String getJsRegexp() {
         return jsRegexp;
     }
 
     public void setJsRegexp(String jsRegexp) {
         this.jsRegexp = jsRegexp;
     }
 
     public String getHtmlClass() {
         return htmlClass;
     }
     
     private void setHtmlClass() {
         this.htmlClass = StringUtils.join(new String[] {
             "action-editField"
             , " " , "validate-", (jsRegexp != null ? "regex" : widget)
             ,(mandatory ? " required-true" : "")
             , " " , "i18n-" +i18nPrefixCode
             ,(widget != null && widget.equals("capdematEnum") ? " " + "javatype-" + getQualifiedType() : "" )
             ,(widget != null && widget.equals("textarea") ? " rows-" + String.valueOf(rows) : "" )
         });
     }
 
     public String getWidget() {
         return widget;
     }
 
     public void setWidget(String type) {
         if (widget != null) {
             if (widget.equals("textarea") && !(type.equals("string") || type.equals("token")))
                 throw new RuntimeException("setWidget() - <textarea /> can be only used for types {string, token}");
             return;
         }
         this.widget = StringUtils.uncapitalize(StringUtils.removeEnd(type, "Type"));
        setHtmlClass();
     }
     
     public int getRows() {
         return rows;
     }
 
     public void setRows(String rows) {
         if (rows == null)
             return;
         try {
             this.rows= Integer.valueOf(rows).intValue();
         } catch (NumberFormatException nfe) {
             throw new RuntimeException("setRows() - rows {"+ rows +"} is not an integer in " + name + "element.");
         }
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
 
     public int getColumn() {
         return column;
     }
 
     public void setColumn(String column) {
         if (column == null)
             return;
         try {
             this.column = Integer.valueOf(column).intValue();
         } catch (NumberFormatException nfe) {
             throw new RuntimeException("setColumn() - Column {"+ column +"} is not an integer");
         }
         if (this.column < 1 || this.column > 2)
             throw new RuntimeException("setColumn() - Column {"+ column +"} is not in [1,2]");
     }
 
     public String getAfter() {
         return after;
     }
 
     public void setAfter(String after) {
         if (after == null)
             return;
         this.after = after;
     }
 
     public Step getStep() {
         return step;
     }
 
     public void setStep(Step step) {
         this.step = step;
     }
 
     // FIXME - manage condition and mandatory state of the element. separation of concerns ??
     public String getConditionsClass() {
         StringBuffer sb = new StringBuffer();
         sb.append(mandatory ? "required " : "");
         if (conditions != null) {
             for (Condition c : conditions)
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
 
     public void addElement (ElementBo element) {
         if (elements == null)
             elements = new ArrayList<ElementBo>();
         elements.add(element);
     }
     
     public List<ElementBo> getElements() {
         return elements;
     }
 }
