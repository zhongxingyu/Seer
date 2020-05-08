 package org.icefaces.ace.component.listcontrol;
 
 import org.icefaces.ace.meta.annotation.Component;
 import org.icefaces.ace.meta.annotation.DefaultValueType;
 import org.icefaces.ace.meta.annotation.Field;
 import org.icefaces.ace.meta.annotation.Property;
 import org.icefaces.ace.meta.baseMeta.UIComponentBaseMeta;
 import org.icefaces.render.MandatoryResourceComponent;
 
 import javax.faces.application.ResourceDependencies;
 import javax.faces.application.ResourceDependency;
 
 @Component(
     tagName = "listControl",
     componentClass = "org.icefaces.ace.component.listcontrol.ListControl",
     generatedClass = "org.icefaces.ace.component.listcontrol.ListControlBase",
     extendsClass = "javax.faces.component.UIComponentBase",
     componentType = "org.icefaces.ace.component.ListControl",
     rendererType  = "org.icefaces.ace.component.ListControlRenderer",
     componentFamily = "org.icefaces.ace.ListControl",
     tlddoc = "Renders a set of controls for moving items among ace:list components. Requires a " +
             " selector that defines the set of ace:list components to move items between. Defaults " +
             "to all lists. " +
             "Optionally if this component has two nested ace:list children, they will be rendered " +
             "within a styled container, and connected via this control without configuration."
 )
 @ResourceDependencies({
     @ResourceDependency(library="icefaces.ace", name="util/combined.css"),
     @ResourceDependency(library = "icefaces.ace", name = "util/ace-jquery.js"),
     @ResourceDependency(library = "icefaces.ace", name = "util/ace-components.js")
 })
 public class ListControlMeta extends UIComponentBaseMeta {
     @Property(tlddoc="Style class to apply to the container DIV element.",
             defaultValue = "ui-widget-content ui-corner-all",
             defaultValueType = DefaultValueType.STRING_LITERAL)
     private String styleClass;
 
     @Property(tlddoc="Style class to apply to the left button icon element.",
             defaultValue = "ui-icon ui-icon-arrow-1-w",
             defaultValueType = DefaultValueType.STRING_LITERAL )
     private String leftClass;
 
     @Property(tlddoc="Style class to apply to the all-left button icon element.",
             defaultValue = "ui-icon ui-icon-arrowstop-1-w",
             defaultValueType = DefaultValueType.STRING_LITERAL)
     private String allLeftClass;
 
     @Property(tlddoc="Style class to apply to the right button icon element.",
             defaultValue = "ui-icon ui-icon-arrow-1-e",
             defaultValueType = DefaultValueType.STRING_LITERAL)
     private String rightClass;
 
     @Property(tlddoc="Style class to apply to the all-right button icon element.",
             defaultValue = "ui-icon ui-icon-arrowstop-1-e",
             defaultValueType = DefaultValueType.STRING_LITERAL)
     private String allRightClass;
 
     @Property(tlddoc="Style class to apply to the spacer container around each button element.")
     private String spacerClass;
 
     @Property(tlddoc="Style class to apply to the container around each button icon element.",
             defaultValue = "ui-state-default ui-corner-all",
             defaultValueType = DefaultValueType.STRING_LITERAL)
     private String controlClass;
 
     @Property(tlddoc="Style class to apply to the header DIV element.",
             defaultValue = "ui-state-default",
             defaultValueType = DefaultValueType.STRING_LITERAL)
     private String headerClass;
 
     @Property(tlddoc="Style class to apply to the footer DIV element.",
             defaultValue = "ui-widget-content",
             defaultValueType = DefaultValueType.STRING_LITERAL)
     private String footerClass;
 
     @Property(tlddoc="Style rules to apply to the header DIV element.")
     private String headerStyle;
 
     @Property(tlddoc="Style rules to apply to the footer DIV element.")
     private String footerStyle;
 
     @Property(tlddoc="Style rules to apply to the container DIV element.")
     private String style;
 
     @Property(tlddoc="JQuery/CSS selector defining the group of lists this " +
             "control navigates and creates mutually exclusive selection between. " +
             "Default selects all lists. When in dual list mode, this property " +
             "has no effect.",
             defaultValue = ".if-list",
             defaultValueType = DefaultValueType.STRING_LITERAL)
     private String selector;
 
     @Property(tlddoc = "Defines the order that the movement controls appear in.",
             defaultValue = "alll lft rgt allr",
             defaultValueType = DefaultValueType.STRING_LITERAL)
     private String format;
 
     @Property(tlddoc = "When dual list mode is used, this property will determine " +
             "if we render the migration control in the \"MIDDLE\", on the \"TOP\", \"BOTTOM\" or \"BOTH\" ends of the " +
             "nested lists. \"ALL\" renders controls in every position",
             defaultValue = "DualListPosition.TOP",
             defaultValueType = DefaultValueType.EXPRESSION
     )
     private DualListPosition position;
 
    // Object to prevent circular dependency during meta generation
     @Field
    Object renderContext;
 }
