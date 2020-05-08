 package org.icefaces.ace.component.list;
 
 import org.icefaces.ace.meta.annotation.*;
 import org.icefaces.ace.meta.baseMeta.UIDataMeta;
 import org.icefaces.render.MandatoryResourceComponent;
 
 import javax.el.MethodExpression;
 import javax.faces.application.ResourceDependencies;
 import javax.faces.application.ResourceDependency;
 import java.util.Set;
 
 @Component(
     tagName = "list",
     componentClass = "org.icefaces.ace.component.list.ACEList",
     generatedClass = "org.icefaces.ace.component.list.ListBase",
     extendsClass = "org.icefaces.impl.component.UISeriesBase",
     componentType = "org.icefaces.ace.component.List",
     rendererType  = "org.icefaces.ace.component.ListRenderer",
     rendererClass = "org.icefaces.ace.component.list.ListRenderer",
     componentFamily = "org.icefaces.ace.List",
     tlddoc = "Renders an Array or List of objects as a styled HTML UL element. Supports dual & multi" +
             "list configurations, reordering, item drag/drop, selection & block style layout.  The component " +
             "strives to be exceptionally easy to style and adjust layout.The List can optionally " +
            "take SelectItem objects to use their String value representations as list item contents; or " +
             "the ace:list tag defines child components that are iteratively rendered for each object." +
             "The components are associated with the iterative object via bindings of the 'var' property."
 )
 @ResourceDependencies({
     @ResourceDependency(library="icefaces.ace", name="util/combined.css"),
     @ResourceDependency(library = "icefaces.ace", name = "util/ace-jquery.js"),
     @ResourceDependency(library = "icefaces.ace", name = "util/ace-components.js")
 })
 @ClientBehaviorHolder(events = {
         @ClientEvent(name="select", defaultRender="@this", defaultExecute="@this",
                 javadoc="Fired when an item is clicked & selected in the List.",
                 tlddoc ="Fired when an item is clicked & selected in the List."),
         @ClientEvent(name="deselect", defaultRender="@this", defaultExecute="@this",
                 javadoc="Fired when an item is clicked & deselected in the List.",
                 tlddoc ="Fired when an item is clicked & deselected in the List."),
         @ClientEvent(name="move", defaultRender="@this", defaultExecute="@this",
                 javadoc="Fired when an item is moved within the List.",
                 tlddoc ="Fired when an item is moved within the List."),
         @ClientEvent(name="migrate", defaultRender="@all", defaultExecute="@all",
                 javadoc="Fired when an item is migrated to this List.",
                 tlddoc ="Fired when an item is migrated to this List.")
     },
     defaultEvent = "select"
 )
 public class ListMeta extends UIDataMeta {
     // Properties
     @Property(tlddoc="Style class to apply to the iterative LI element.")
     private String itemClass;
 
     @Property(tlddoc="Style class to apply to the container UL element.")
     private String bodyClass;
 
     @Property(tlddoc="Style class to apply to the container DIV element.")
     private String styleClass;
 
     @Property(tlddoc="Style class to apply to the header DIV element.")
     private String headerClass;
 
     @Property(tlddoc="Style class to apply to the footer DIV element.")
     private String footerClass;
 
     @Property(tlddoc="Style class to apply to the optional dragging placeholder LI element.")
     private String placeholderClass;
 
 
     @Property(tlddoc = "Defines a CSS height value to set in the UL element style. eg. '200px', '10%', etc.")
     private String height;
 
 
     @Property(tlddoc = "Style rules to apply to the container DIV element")
     private String style;
 
     @Property(tlddoc="Style rules to apply to the iterative LI element.")
     private String itemStyle;
 
     @Property(tlddoc="Style rules to apply to the container UL element.")
     private String bodyStyle;
 
     @Property(tlddoc="Style rules to apply to the header DIV element.")
     private String headerStyle;
 
     @Property(tlddoc="Style rules to apply to the footer DIV element.")
     private String footerStyle;
 
 
 
     @Property(tlddoc = "Enable adding a style to the whitespace that is cleared for a" +
             " list item being dragged / dropped.", defaultValue = "true",
             defaultValueType = DefaultValueType.EXPRESSION)
     private Boolean placeholder;
 
     @Property(tlddoc = "Enable the dragging of list items in this list.",
             defaultValue = "true", defaultValueType = DefaultValueType.EXPRESSION)
     private Boolean dragging;
 
     @Property(tlddoc = "Enables inter-list dragging and dropping; an identifier" +
             " used to link this region and others for bi-directional dropping.")
     private String dropGroup;
 
 
 
     @Property(tlddoc = "Enables item selection via clicking when defined as \"multiple\" or \"single\". " +
             "The default value of the property is null; this and any value other than \"multiple\" or " +
             "\"single\" disables selection for the list. Note that each click begins a " +
             "new selection. If \"multiple\" is set, CTRL (or Command) + Click adds to (or removes from) " +
             "the selection, and Shift + Click will add to the selection all unselected items between the previous selection " +
             "(or deselection) and the clicked item (inclusive of the clicked item itself).")
     private String selectionMode;
 
     @Property(tlddoc = "Enable single item migration to the next list in the first matching " +
             "ListControl via item double clicks; with reverse migation via shift double clicks. " +
             "If used in a dual list configuration the reverse migration is implied for the second " +
             "list, and holding the shift key isn't required.",
             defaultValue = "true", defaultValueType = DefaultValueType.EXPRESSION)
     private Boolean doubleClickMigration;
 
     @Property(tlddoc = "Defines the set of Objects from the source List that are selected.")
     private Set<Object> selections;
 
     @Property(expression = Expression.METHOD_EXPRESSION,
             methodExpressionArgument = "org.icefaces.ace.event.ListSelectEvent",
             tlddoc = "Define a method listener that is passed a ListSelectEvent wrapping the array" +
                     "of items newly selected in a single request.")
     private MethodExpression selectionListener;
 
 
 
     @Property(tlddoc = "Enable a set of buttons to control reordering of items within this list." +
             "Requires selection of determine items to adjust via buttons.",
             defaultValue = "false", defaultValueType = DefaultValueType.EXPRESSION)
     private Boolean controlsEnabled;
 
     @Property(tlddoc = "Defines the order in which the reordering buttons render.",
             defaultValue = "top up dwn btm",
             defaultValueType = DefaultValueType.STRING_LITERAL)
     private String controlsFormat;
 
     @Property(tlddoc = "", defaultValue = "Style class to apply to the DIV surrounding the reordering controls.",
             defaultValueType = DefaultValueType.STRING_LITERAL)
     private String controlsContainerClass;
 
     @Property(tlddoc = "Style class to apply to the div surrounding the individual reordering icons.",
             defaultValue = "ui-state-default ui-corner-all",
             defaultValueType = DefaultValueType.STRING_LITERAL)
     private String controlsItemClass;
 
     @Property(tlddoc="Style class to apply to the spacer container around each button element.")
     private String controlsSpacerClass;
 
     @Property(tlddoc = "Style class to apply to the span defining the top button icon.",
             defaultValue = "ui-icon ui-icon-arrowstop-1-n",
             defaultValueType = DefaultValueType.STRING_LITERAL)
     private String topButtonClass;
 
     @Property(tlddoc = "Style class to apply to the span defining the up button icon.",
             defaultValue = "ui-icon ui-icon-arrow-1-n",
             defaultValueType = DefaultValueType.STRING_LITERAL)
     private String upButtonClass;
 
     @Property(tlddoc = "Style class to apply to the span defining the down button icon.",
             defaultValue = "ui-icon ui-icon-arrow-1-s",
             defaultValueType = DefaultValueType.STRING_LITERAL)
     private String downButtonClass;
 
     @Property(tlddoc = "Style class to apply to the span defining the bottom button icon.",
             defaultValue = "ui-icon ui-icon-arrowstop-1-s",
             defaultValueType = DefaultValueType.STRING_LITERAL)
     private String bottomButtonClass;
 
 }
