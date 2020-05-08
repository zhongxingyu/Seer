 package net.kamhon.ieagle.struts2.components;
 
 import java.util.Random;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.struts2.components.ClosingUIBean;
 import org.apache.struts2.components.Form;
 import org.apache.struts2.views.annotations.StrutsTagAttribute;
 
 import com.opensymphony.xwork2.util.ValueStack;
 
 public abstract class AbstractContainer extends ClosingUIBean implements StrutsContainer {
     final private static transient Random RANDOM = new Random();
 
     public static final String THEME = "extra";
 
     protected String parentTheme;
     protected String assignJsVar;
 
     /**
      * this used when id is not set, plugin will auto set it for you.
      * 
      * @return
      */
     public abstract String getIdPrefix();
 
     public AbstractContainer(ValueStack stack, HttpServletRequest request, HttpServletResponse response) {
         super(stack, request, response);
     }
 
     @Override
     protected void evaluateExtraParams() {
         super.evaluateExtraParams();
 
         Form form = (Form) findAncestor(Form.class);
         if (parentTheme != null) {
             addParameter("parentTheme", findString(parentTheme));
         } else if (form != null) {
             if (form != null)
                 addParameter("parentTheme", form.getTheme());
         } else {
             addParameter("parentTheme", "simple");
         }
 
         if ((this.id == null || this.id.length() == 0)) {
             this.id = generateRandomId(getIdPrefix());
             addParameter("id", this.id);
         }
 
        addParameter("idConverted", convertedId());
 
         if (StringUtils.isNotBlank(assignJsVar)) {
             addParameter("assignJsVar", assignJsVar);
         }
 
         setDynamicAttributes(dynamicAttributes);
     }
 
    private String convertedId() {
         if ((this.id == null || this.id.length() == 0)) {
             this.id = generateRandomId(getIdPrefix());
         }
 
        String converted = id.replace('.', '_');
        System.out.println("converted = " + converted + ", id = " + id);

        return id.replace('.', '_');
     }
 
     protected String generateRandomId(String prefix) {
         // resolves Math.abs(Integer.MIN_VALUE) issue reported by FindBugs
         // http://findbugs.sourceforge.net/bugDescriptions.html#RV_ABSOLUTE_VALUE_OF_RANDOM_INT
         int nextInt = RANDOM.nextInt();
         nextInt = nextInt == Integer.MIN_VALUE ? Integer.MAX_VALUE : Math.abs(nextInt);
         return prefix + "_" + String.valueOf(nextInt);
     }
 
     // ---------------- GETTER & SETTER (START) -------------
     @Override
     public void setTheme(String theme) {
         if (StringUtils.isBlank(theme)) {
             theme = THEME;
         }
 
         this.theme = theme;
     }
 
     @StrutsTagAttribute(description = "The parent theme. Default: value of parent form tag or simple if no parent form tag is available")
     public void setParentTheme(String parentTheme) {
         this.parentTheme = parentTheme;
     }
 
     @StrutsTagAttribute(description = "Assign it to a JavaScript Object")
     public void setAssignJsVar(String assignJsVar) {
         this.assignJsVar = assignJsVar;
     }
 
     public String getAssignJsVar() {
         return assignJsVar;
     }
     // ---------------- GETTER & SETTER (END) -------------
 }
