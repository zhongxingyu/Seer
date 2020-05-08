 /*
  * Copyright (c) 2000-2004 Netspective Communications LLC. All rights reserved.
  *
  * Netspective Communications LLC ("Netspective") permits redistribution, modification and use of this file in source
  * and binary form ("The Software") under the Netspective Source License ("NSL" or "The License"). The following
  * conditions are provided as a summary of the NSL but the NSL remains the canonical license and must be accepted
  * before using The Software. Any use of The Software indicates agreement with the NSL.
  *
  * 1. Each copy or derived work of The Software must preserve the copyright notice and this notice unmodified.
  *
  * 2. Redistribution of The Software is allowed in object code form only (as Java .class files or a .jar file
  *    containing the .class files) and only as part of an application that uses The Software as part of its primary
  *    functionality. No distribution of the package is allowed as part of a software development kit, other library,
  *    or development tool without written consent of Netspective. Any modified form of The Software is bound by these
  *    same restrictions.
  *
  * 3. Redistributions of The Software in any form must include an unmodified copy of The License, normally in a plain
  *    ASCII text file unless otherwise agreed to, in writing, by Netspective.
  *
  * 4. The names "Netspective", "Axiom", "Commons", "Junxion", and "Sparx" are trademarks of Netspective and may not be
  *    used to endorse or appear in products derived from The Software without written consent of Netspective.
  *
  * THE SOFTWARE IS PROVIDED "AS IS" WITHOUT A WARRANTY OF ANY KIND. ALL EXPRESS OR IMPLIED REPRESENTATIONS AND
  * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT,
  * ARE HEREBY DISCLAIMED.
  *
  * NETSPECTIVE AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE OR ANY THIRD PARTY AS A
  * RESULT OF USING OR DISTRIBUTING THE SOFTWARE. IN NO EVENT WILL NETSPECTIVE OR ITS LICENSORS BE LIABLE FOR ANY LOST
  * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
  * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THE SOFTWARE, EVEN
  * IF IT HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
  */
 package com.netspective.sparx.form.field;
 
 import java.io.IOException;
 import java.io.Writer;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.net.URLDecoder;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.servlet.http.Cookie;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import com.netspective.commons.io.InputSourceLocator;
 import com.netspective.commons.text.TextUtils;
 import com.netspective.commons.value.GenericValue;
 import com.netspective.commons.value.ValueSource;
 import com.netspective.commons.value.source.StaticValueSource;
 import com.netspective.commons.xdm.XmlDataModelSchema;
 import com.netspective.commons.xml.template.Template;
 import com.netspective.commons.xml.template.TemplateCatalog;
 import com.netspective.commons.xml.template.TemplateConsumer;
 import com.netspective.commons.xml.template.TemplateConsumerDefn;
 import com.netspective.sparx.form.ClientDataEncryption;
 import com.netspective.sparx.form.Dialog;
 import com.netspective.sparx.form.DialogContext;
 import com.netspective.sparx.form.DialogContextBeanMemberInfo;
 import com.netspective.sparx.form.DialogFlags;
 import com.netspective.sparx.form.DialogPerspectives;
 import com.netspective.sparx.form.DialogValidationContext;
 import com.netspective.sparx.form.field.conditional.DialogFieldConditionalApplyFlag;
 import com.netspective.sparx.form.field.conditional.DialogFieldConditionalClear;
 import com.netspective.sparx.form.field.conditional.DialogFieldConditionalData;
 import com.netspective.sparx.form.field.conditional.DialogFieldConditionalDisplay;
 import com.netspective.sparx.panel.HtmlHelpPanel;
 import com.netspective.sparx.value.source.DialogFieldValueSource;
 
 /**
  * A <code>DialogField</code> object represents a data field of a form/dialog. It contains functionalities
  * such as data validation rules, dynamic data binding, HTML rendering, and conditional logics.
  * It provides the default behavior and functionality for all types of dialog fields.
  * All dialog classes representing specialized  fields such as text fields, numerical fields, and phone fields subclass
  * the <code>DialogField</code> class to create specialized behavior.
  */
 public class DialogField implements TemplateConsumer, XmlDataModelSchema.InputSourceLocatorListener
 {
     public static final XmlDataModelSchema.Options XML_DATA_MODEL_SCHEMA_OPTIONS = new XmlDataModelSchema.Options().setIgnorePcData(true);
     private static final Log log = LogFactory.getLog(DialogField.class);
     public static final String ATTRNAME_TYPE = "type";
     public static final String[] ATTRNAMES_SET_BEFORE_CONSUMING = new String[]{"name"};
     private static FieldTypeTemplateConsumerDefn fieldTypeConsumer = new FieldTypeTemplateConsumerDefn();
 
     static
     {
         TemplateCatalog.registerConsumerDefnForClass(fieldTypeConsumer, DialogField.class, true, true);
     }
 
     protected static class FieldTypeTemplateConsumerDefn extends TemplateConsumerDefn
     {
         public FieldTypeTemplateConsumerDefn()
         {
             super(null, ATTRNAME_TYPE, ATTRNAMES_SET_BEFORE_CONSUMING);
         }
 
         public String getNameSpaceId()
         {
             return DialogField.class.getName();
         }
     }
 
     // This is the default flags to be carried to child fields. User-defined fields can use the set() method to replace this
     // with the flags of their own for now.
     public static final int[] CHILD_CARRY_FLAGS =
             new int[]{
                 DialogFieldFlags.REQUIRED, DialogFieldFlags.UNAVAILABLE, DialogFieldFlags.READ_ONLY,
                 DialogFieldFlags.PERSIST, DialogFieldFlags.CREATE_ADJACENT_AREA,
                 DialogFieldFlags.SHOW_CAPTION_AS_CHILD
             };
 
 
     // TODO: Create an abstract State class which will be extended by all field state classes to force them to construct their own state flag objects
     /**
      * Class representing the current state of the dialog field. The state of a dialog field is dynamic and reflects the
      * changes in values and flags. It is usually initialized with the XML configured value and flags of the field.
      */
     public class State
     {
         private DialogFieldValue value = constructValueInstance();
         private String adjacentAreaValue;
         private DialogFieldFlags stateFlags;
         private boolean loadedPersistentValue;
         private DialogContext dialogContext;
         private DialogField field;
 
         public class BasicStateValue extends GenericValue implements DialogFieldValue
         {
             private boolean isValid = true;
             private String invalidText;
 
             public BasicStateValue()
             {
             }
 
             public BasicStateValue(List value)
             {
                 super(value);
             }
 
             public BasicStateValue(Object value)
             {
                 super(value);
             }
 
             public BasicStateValue(String[] value)
             {
                 super(value);
             }
 
             public boolean isValid()
             {
                 return isValid;
             }
 
             public String getInvalidText()
             {
                 return invalidText;
             }
 
             public void setInvalidText(String text)
             {
                 isValid = false;
                 invalidText = text;
             }
 
             public DialogField getField()
             {
                 return DialogField.this;
             }
 
             public State getState()
             {
                 return State.this;
             }
 
 
         }
 
         /**
          * Creates the dialog field state object with the associated context and default flag objects
          */
         public State(DialogContext dc, DialogField field)
         {
             this.dialogContext = dc;
             this.field = field;
             this.stateFlags = field.createFlags();
             this.stateFlags.setState(this);
             initialize(dc);
         }
 
         /**
          * Initializes the state object by copying field flag values and setting additional flags based
          * on dialog perspectives. Also sets the state value based on cookies if the 'persist' flag is set.
          */
         private void initialize(DialogContext dc)
         {
             stateFlags.copy(getFlags());
 
             if(stateFlags.flagIsSet(DialogFieldFlags.PERSIST) && dc.getDialogState().isInLoadPersistentFieldDataMode())
             {
                 Cookie[] cookies = dc.getHttpRequest().getCookies();
                 if(cookies != null)
                 {
                     for(int i = 0; i < cookies.length; i++)
                     {
                         Cookie cookie = cookies[i];
                         if(cookie.getName().equals(getCookieName()))
                         {
                             value.setTextValue(URLDecoder.decode(cookie.getValue()));
                             setLoadedPersistentValue(true);
                         }
                     }
                 }
             }
 
             switch((int) dc.getDialogState().getPerspectives().getFlags())
             {
                 case DialogPerspectives.ADD:
                     // when in "add" mode, auto generated primary keys should not be on the form
                     if(stateFlags.flagIsSet(DialogFieldFlags.PRIMARY_KEY_GENERATED))
                         stateFlags.setFlag(DialogFieldFlags.UNAVAILABLE);
                     break;
 
                 case DialogPerspectives.EDIT:
                     // when in "edit" mode, the primary key should be read-only
                     if(stateFlags.flagIsSet(DialogFieldFlags.PRIMARY_KEY | DialogFieldFlags.PRIMARY_KEY_GENERATED))
                         stateFlags.setFlag(DialogFieldFlags.READ_ONLY);
                     break;
 
                 case DialogPerspectives.CONFIRM:
                 case DialogPerspectives.DELETE:
                 case DialogPerspectives.PRINT:
                     // when any of these modes, all the fields should be read-only
                     stateFlags.setFlag(DialogFieldFlags.READ_ONLY);
                     break;
             }
         }
 
         /**
          * Constructs a DialogFieldVale object
          */
         public DialogFieldValue constructValueInstance()
         {
             return new BasicStateValue();
         }
 
         /**
          * Gets the dialog context
          */
         public DialogContext getDialogContext()
         {
             return dialogContext;
         }
 
         /**
          * Checks to see if the field value is required
          */
         public boolean hasRequiredValue()
         {
             return value.hasValue();
         }
 
         /**
          * Gets the dialog field value
          */
         public DialogFieldValue getValue()
         {
             return value;
         }
 
         /**
          * Gets the field's adjacent area's value
          */
         public String getAdjacentAreaValue()
         {
             return adjacentAreaValue;
         }
 
         /**
          * Sets the adjacent area's value
          */
         public void setAdjacentAreaValue(String adjacentAreaValue)
         {
             this.adjacentAreaValue = adjacentAreaValue;
         }
 
         public boolean isLoadedPersistentValue()
         {
             return loadedPersistentValue;
         }
 
         public void setLoadedPersistentValue(boolean loadedPersistentValue)
         {
             this.loadedPersistentValue = loadedPersistentValue;
         }
 
         /**
          * Gets the state flags associated with the field
          */
         public DialogFieldFlags getStateFlags()
         {
             return stateFlags;
         }
 
         /**
          * Returns the dialog field object to which this state belongs to.
          */
         public final DialogField getField()
         {
             return field;
         }
 
         /**
          * Sets a cookie when the 'persist' flag is set and the field has a value in it
          */
         public void persistValue()
         {
             if(getStateFlags().flagIsSet(DialogFieldFlags.PERSIST) && value.hasValue())
             {
                 Cookie cookie = new Cookie(getCookieName(), URLEncoder.encode(value.getTextValue()));
                 cookie.setMaxAge(60 * 60 * 24 * 365); // 1 year
                 dialogContext.getHttpResponse().addCookie(cookie);
             }
         }
 
         /**
          * Return the object that will be used to store the validation error messages in the ValidationContext
          */
         public Object getValidationContextScope()
         {
             return value;
         }
 
         /**
          * Imports the field's related information from an XML element
          */
         public void importFromXml(Element fieldStateElem)
         {
             String fieldName = fieldStateElem.getAttribute("name");
             if(fieldName == null)
                 return;
             if(!fieldName.equals(getQualifiedName()))
                 throw new RuntimeException("Attempting to assign field state for '" + fieldName + "' into '" + getQualifiedName() + "'.");
             getStateFlags().setValue(fieldStateElem.getAttribute("flags"));
             String adjAreaValue = fieldStateElem.getAttribute("adjacent-area-value");
             if(!adjAreaValue.equals("-NULL-"))
                 setAdjacentAreaValue(adjAreaValue);
             value.importFromXml(fieldStateElem);
         }
 
         /**
          * Exports the field's related information as an XML element
          */
         public void exportToXml(Element parent)
         {
             Document doc = parent.getOwnerDocument();
             Element fieldStateElem = doc.createElement("field-state");
             String fieldName = getQualifiedName();
             if(fieldName != null)
             {
                 fieldStateElem.setAttribute("name", getQualifiedName());
                 String flagsText = getStateFlags().getFlagsText();
                 fieldStateElem.setAttribute("flags", flagsText);
                 fieldStateElem.setAttribute("adjacent-area-value", adjacentAreaValue != null
                                                                    ? adjacentAreaValue : "-NULL-");
                 value.exportToXml(fieldStateElem);
                 parent.appendChild(fieldStateElem);
             }
         }
 
         public void appendAsUrlParam(StringBuffer sb)
         {
             String fieldName = getQualifiedName();
             if(fieldName != null)
                 value.appendAsUrlParamValue(getHtmlFormControlId(), sb);
         }
     }
 
     /**
      * Translates the passed in name into lower case values
      */
     public static final String translateFieldNameForMapKey(final String name)
     {
         return name.toLowerCase();
     }
 
     public static final int DISPLAY_FORMAT = 0;
     public static final int SUBMIT_FORMAT = 1;
 
     public static final String CUSTOM_CAPTION = new String();
     public static final String GENERATE_CAPTION = "*";
 
     static public int fieldCounter = 0;
     private boolean multi = false;
 
     private InputSourceLocator inputSourceLocator;
     private List fieldTypes = new ArrayList();
     private Dialog owner;
     private DialogField parent;
     private String htmlFormControlId;
     private String name;
     private String qualifiedName;
     private ValueSource caption = ValueSource.NULL_VALUE_SOURCE;
     private ValueSource errorCaption = ValueSource.NULL_VALUE_SOURCE;
     private ValueSource defaultValue = ValueSource.NULL_VALUE_SOURCE;
     private ValueSource hint = ValueSource.NULL_VALUE_SOURCE;
     private HtmlHelpPanel helpPanel;
     private String cookieName;
     private DialogFields children;
     private DialogFieldConditionalActions conditionalActions = new DialogFieldConditionalActions();
     private DialogFieldConditionalActions dependentConditions = new DialogFieldConditionalActions();
     private List clientJavascripts = new ArrayList();
     private DialogFieldFlags flags = createFlags();
     private DialogFieldPopup popup;
 
     private List popupList = new ArrayList();   // array to keep track of popup actions assigned to the field
     private DialogFieldScanEntry scanEntry;
     private DialogFieldAutoBlur autoBlur;
     private DialogFieldSubmitOnBlur submitOnBlur;
     private DialogFieldValidations validationRules = constructValidationRules();
     private String requiredFieldMissingMessage = "{0} is required.";
     private String accessKey;
     private int[] childCarryFlags = CHILD_CARRY_FLAGS;
     private ClientDataEncryption encryption;
 
     public DialogField()
     {
         setFlags(new DialogFieldFlags());
     }
 
     public InputSourceLocator getInputSourceLocator()
     {
         return inputSourceLocator;
     }
 
     public void setInputSourceLocator(InputSourceLocator inputSourceLocator)
     {
         this.inputSourceLocator = inputSourceLocator;
     }
 
     public int[] getChildCarryFlags()
     {
         return childCarryFlags;
     }
 
     public void setChildCarryFlags(int[] childCarryFlags)
     {
         this.childCarryFlags = childCarryFlags;
     }
 
     public TemplateConsumerDefn getTemplateConsumerDefn()
     {
         return fieldTypeConsumer;
     }
 
     public void registerTemplateConsumption(Template template)
     {
         fieldTypes.add(template.getTemplateName());
     }
 
     public List getFieldTypes()
     {
         return fieldTypes;
     }
 
     /**
      * Gets the owner dialog of this field
      *
      * @return owner dialog
      */
     public Dialog getOwner()
     {
         return owner;
     }
 
     /**
      * Sets the owner dialog
      *
      * @param owner Dialog object
      */
     public void setOwner(Dialog owner)
     {
         this.owner = owner;
     }
 
     /**
      * Creates a new field state object for this field
      *
      * @param dc The dialog context which is the state of the dialog
      */
     public State constructStateInstance(DialogContext dc)
     {
         return new State(dc, this);
     }
 
     /**
      * Gets the class of the field's state
      */
     public Class getStateClass()
     {
         return State.class;
     }
 
     /**
      * Gets the class of the field's state value
      */
     public Class getStateValueClass()
     {
         return State.BasicStateValue.class;
     }
 
     /**
      * Creates a new validation  class for the field
      */
     public DialogFieldValidations constructValidationRules()
     {
         return new DialogFieldValidations(this);
     }
 
     /**
      * Gets the validation object for this field
      */
     public DialogFieldValidations getValidationRules()
     {
         return validationRules;
     }
 
     /**
      * Gets the validation object for this field. This is used by XDM.
      */
     public DialogFieldValidations createValidation()
     {
         return validationRules;
     }
 
     /**
      * Empty method.  Thiis is used by  XDM  to know that validation rules are allowed for fields
      */
     public void addValidation(DialogFieldValidations rules)
     {
         // do nothing but keep method because XDM needs to know rules are allowed
     }
 
     /**
      * Create flags for the field object
      */
     public DialogFieldFlags createFlags()
     {
         return new DialogFieldFlags();
     }
 
     /**
      * Create flags for the field state object
      */
     public DialogFieldFlags createFlags(State state)
     {
         return new DialogFieldFlags(state);
     }
 
     public DialogFieldFlags getFlags()
     {
         return flags;
     }
 
     /**
      * Sets all the flags for this dialog field.
      *
      * @param flags flags associated with this dialog field
      */
     public void setFlags(DialogFieldFlags flags)
     {
         this.flags.copy(flags);
         this.flags.setField(this);
     }
 
     /**
      * Checks to see if the flag should be carried to children fields
      */
     public boolean carryFlag(long flag)
     {
         boolean carryFlag = false;
 
         int[] childCarryFlags = getChildCarryFlags();
         for(int i = 0; i < childCarryFlags.length; i++)
         {
             if(flag == childCarryFlags[i])
             {
                 carryFlag = true;
                 break;
             }
         }
         return carryFlag;
     }
 
 
     /**
      * Checks to see if the field requires multi-part endcoding
      *
      * @return boolean
      */
     public boolean requiresMultiPartEncoding()
     {
         // if any child requires multi part encoding, then return true (this will take of things recursively)
         if(children != null)
             if(children.requiresMultiPartEncoding())
                 return true;
 
         // no child requires it and we don't require it by default, either
         return false;
     }
 
     /**
      * Get a list of conditional actions
      *
      * @return List a list of conditional actions
      */
     public DialogFieldConditionalActions getConditionalActions()
     {
         return conditionalActions;
     }
 
     /**
      * Add a conditional action for this field
      */
     public void addConditional(DialogFieldConditionalAction action)
     {
         conditionalActions.addAction(action);
         flags.setFlag(DialogFieldFlags.HAS_CONDITIONAL_DATA); // in case JavaScript needs it
     }
 
     /**
      * Creates a new default conditional action for this field
      *
      * @return a <code>DialogFieldConditionalAction</code> object
      */
     public DialogFieldConditionalAction createConditional()
     {
         return new DialogFieldConditionalAction(this);
     }
 
     /**
      * Creates a new custom conditional action class for this field
      *
      * @param cls Custom class
      */
     public DialogFieldConditionalAction createConditional(Class cls) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException
     {
         if(DialogFieldConditionalAction.class.isAssignableFrom(cls))
         {
             Constructor c = cls.getConstructor(new Class[]{DialogField.class});
             return (DialogFieldConditionalAction) c.newInstance(new Object[]{this});
         }
         else
             throw new RuntimeException("Don't know what to do with with class: " + cls);
     }
 
     /**
      * Gets the associated popup field
      *
      * @deprecated Multiple popups are allowed now and this method needs to be removed.
      */
     public DialogFieldPopup getPopup()
     {
         return popup;
     }
 
     /**
      * Gets all the popups associated with the dialog field
      *
      * @return an array of popups
      */
     public DialogFieldPopup[] getPopups()
     {
         DialogFieldPopup[] popups = new DialogFieldPopup[popupList.size()];
         for(int i = 0; i < popupList.size(); i++)
         {
             popups[i] = (DialogFieldPopup) popupList.get(i);
         }
         return popups;
     }
 
     /**
      * Sets the associated popup field
      */
     public void addPopup(DialogFieldPopup popup)
     {
         if(popup.getFill() == null)
             popup.setFill(getQualifiedName());
         this.popup = popup;
         this.popupList.add(popup);
     }
 
     public DialogFieldPopup createPopup()
     {
         return new DialogFieldPopup();
     }
 
     /**
      * Gets all the javascripts defined for this field
      *
      * @return ArrayList
      */
     public List getClientJavascripts()
     {
         return this.clientJavascripts;
     }
 
     /**
      * Adds a custom javascript handler for this field
      */
     public void addClientJs(DialogFieldClientJavascript clientJs)
     {
         clientJavascripts.add(clientJs);
     }
 
     public void addAutoBlur(DialogFieldAutoBlur autoBlur)
     {
         flags.setFlag(DialogFieldFlags.AUTO_BLUR);
         this.autoBlur = autoBlur;
     }
 
     public void addSubmitOnBlur(DialogFieldSubmitOnBlur submitOnBlur)
     {
         flags.setFlag(DialogFieldFlags.SUBMIT_ONBLUR);
         this.submitOnBlur = submitOnBlur;
     }
 
     public void addScanEntry(DialogFieldScanEntry scanEntry)
     {
         flags.setFlag(DialogFieldFlags.SCANNABLE);
         this.scanEntry = scanEntry;
     }
 
     /**
      * Invalidates the field
      *
      * @param dc      dialog context
      * @param message Validation error message
      */
     public void invalidate(DialogContext dc, String message)
     {
         State fieldState = dc.getFieldStates().getState(parent != null ? parent : this);
         dc.getValidationContext().addValidationError(fieldState.getValidationContextScope(), message, null);
     }
 
     /**
      * Get the keyboard shortcut key for the field
      */
     public String getAccessKey()
     {
         return accessKey;
     }
 
     /**
      * Set the keyboard shortcut key for the field
      */
     public void setAccessKey(String accessKey)
     {
         this.accessKey = accessKey;
     }
 
     /**
      * Gets the parent dialog field
      *
      * @return DialogField
      */
     public DialogField getParent()
     {
         return parent;
     }
 
     /**
      * Sets the parent dialog field
      *
      * @param newParent the parent field
      */
     public void setParent(DialogField newParent)
     {
         parent = newParent;
         setOwner(parent.getOwner());
     }
 
     /**
      * Returns true if the field is supposed to return multiple values
      *
      * @return boolean Does this field return multiple values (e.g. multilist select field)
      */
     public boolean isMulti()
     {
         return multi;
     }
 
     /**
      * Sets the value of the multi attribute which determines whether a field returns multiple values or not (e.g.
      * a multilist select field)
      *
      * @param multi boolean true/false - Whether or not this field returns multiple values
      */
     public void setMulti(boolean multi)
     {
         this.multi = multi;
     }
 
     public String getHtmlFormControlId()
     {
         return htmlFormControlId;
     }
 
     /**
      * Gets the simple name of the dialog
      *
      * @return String
      */
     public String getName()
     {
         return name;
     }
 
     public String getNameForMapKey()
     {
         return name != null ? name.toLowerCase() : null;
     }
 
     /**
      * Gets the qualified name of the dialog
      *
      * @return String
      */
     public String getQualifiedName()
     {
         return qualifiedName;
     }
 
     /**
      * Sets the simple name of this dialog field.
      *
      * @param newName new simple name
      */
     public void setName(String newName)
     {
         name = newName;
         if(name != null)
         {
             setHtmlFormControlId(Dialog.PARAMNAME_CONTROLPREFIX + TextUtils.getInstance().xmlTextToJavaIdentifier(name, false));
             setQualifiedName(name);
         }
     }
 
     /**
      * Sets the qualified name of this dialog field.
      *
      * @param newName new qualified name
      */
     public void setQualifiedName(String newName)
     {
         qualifiedName = newName;
         if(qualifiedName != null)
             setHtmlFormControlId(Dialog.PARAMNAME_CONTROLPREFIX + TextUtils.getInstance().xmlTextToJavaIdentifier(qualifiedName, false));
     }
 
     /**
      * Sets the HTML form control identifier for this dialog field.  An identifier
      * is a field whose values may only contain uppercase letters, numbers, and an underscore.
      *
      * @param htmlFormControlId name of the HTML control for this dialog field
      */
     public void setHtmlFormControlId(String htmlFormControlId)
     {
         this.htmlFormControlId = htmlFormControlId;
     }
 
     /**
      * Gets the cookie name associated with the dialog
      *
      * @return String cookie name
      */
     public String getCookieName()
     {
         return cookieName == null
                ? (Dialog.PARAMNAME_CONTROLPREFIX + getOwner().getQualifiedName() + "." + getQualifiedName())
                : cookieName;
     }
 
     /**
      * Sets the cookie name associated with this dialog field.
      *
      * @param name cookie name for this dialog field
      */
     public void setCookieName(String name)
     {
         cookieName = name;
     }
 
     /**
      * Gets the caption of the dialog as a single value source
      *
      * @return ValueSource
      */
     public ValueSource getCaption()
     {
         return caption;
     }
 
     /**
      * Sets the caption of the dialog field from a value source.
      *
      * @param value value source object from which the field caption is extracted
      */
     public void setCaption(ValueSource value)
     {
         caption = value;
         validationRules.updateCaptions();
     }
 
     public ValueSource getErrorCaption()
     {
         return errorCaption != ValueSource.NULL_VALUE_SOURCE ?
                errorCaption : getCaption();
     }
 
     /**
      * Sets the caption for the error message to be displayed when a validation rule fails for this field.
      *
      * @param errorCaption value source object containing the error message caption
      */
     public void setErrorCaption(ValueSource errorCaption)
     {
         this.errorCaption = errorCaption;
         validationRules.updateCaptions();
     }
 
     /**
      * Gets the hint string associated with the dialog field
      *
      * @return String
      */
     public ValueSource getHint()
     {
         return hint;
     }
 
     /**
      * Sets the hint string associated with this dialog field from a value source.
      *
      * @param value value source object containing the hint string for this field
      */
     public void setHint(ValueSource value)
     {
         hint = value;
     }
 
     public HtmlHelpPanel getHelpPanel()
     {
         return helpPanel;
     }
 
     public HtmlHelpPanel createHelp()
     {
         return new HtmlHelpPanel(this);
     }
 
     public void addHelp(HtmlHelpPanel helpPanel)
     {
         this.helpPanel = helpPanel;
     }
 
     public boolean isHelpAvailable()
     {
         return helpPanel != null;
     }
 
     /**
      * Gets the default value of the field as a value source
      *
      * @return ValueSource    value source containing the field's value
      */
     public ValueSource getDefault()
     {
         return defaultValue;
     }
 
     /**
      * Sets the default value for this field from a value source.
      *
      * @param value value source containing the default value for field
      */
     public void setDefault(ValueSource value)
     {
         defaultValue = value;
     }
 
     /**
      * Gets a list of children fields
      *
      * @return List list of children fields
      */
     public DialogFields getChildren()
     {
         return children;
     }
 
     public DialogField createField()
     {
         return new DialogField();
     }
 
     public DialogField createComposite()
     {
         return createField();
     }
 
     /**
      * Adds a child field to current field. If any of the flags that belong to CHILD_CARRY_FLAGS list is <em>set</em> in the parent
      * then those flags are also set in the child field. This means that the parent will not clear any flags in the child.
      *
      * @param field child field
      */
     public void addField(DialogField field)
     {
         for(int i = 0; i < CHILD_CARRY_FLAGS.length; i++)
         {
             int flag = CHILD_CARRY_FLAGS[i];
             if(flags.flagIsSet(flag))
                 field.flags.setFlag(flag);
         }
 
         if(children == null) children = new DialogFields(this);
         field.setParent(this);
         children.add(field);
 
         field.setParent(this);
         if(qualifiedName != null)
             field.setQualifiedName(qualifiedName + "." + field.getName());
     }
 
     /**
      * Adds a composite field as a child
      */
     public void addComposite(DialogField field)
     {
         addField(field);
     }
 
     public ClientDataEncryption createClientEncryption()
     {
         return new ClientDataEncryption(this);
     }
 
     public void addClientEncryption(ClientDataEncryption dataEncryptionType)
     {
         this.encryption = dataEncryptionType;
     }
 
     public ClientDataEncryption getClientEncryption()
     {
         return encryption;
     }
 
     /**
      * Finalize the dialog field's contents: loops through each conditional action of the field to
      * assign partner fields and loops through each child field to finalize their contents.
      */
     public void finalizeContents()
     {
         for(int i = 0; i < conditionalActions.size(); i++)
         {
             DialogFieldConditionalAction action = conditionalActions.getAction(i);
             DialogField partnerField = owner.getFields().getByQualifiedName(action.getPartnerFieldName());
             if(partnerField != null)
                 action.setPartnerField(partnerField);
             else if(action.isPartnerRequired())
                log.error("Unknown partner '"+ action.getPartnerFieldName() +"' supplied for dialog field '" + action.getSourceField().getQualifiedName() + "' conditional action.");
         }
 
         if(children != null)
             children.finalizeContents();
 
         if(flags.flagIsSet(DialogFieldFlags.DOUBLE_ENTRY))
             this.setupDoubleEntry();
 
         if(requiresMultiPartEncoding())
             getOwner().getDialogFlags().setFlag(DialogFlags.ENCTYPE_MULTIPART_FORMDATA);
     }
 
     /**
      * Sets up client side javascript objects to create a double entry "effect" for the field
      */
     public void setupDoubleEntry()
     {
         this.setHint(new StaticValueSource("Double Entry"));
         DialogFieldClientJavascript doubleEntryJS = new DialogFieldClientJavascript();
         doubleEntryJS.setType(new DialogFieldClientJavascript.ScriptType(DialogFieldClientJavascript.ScriptType.EXTENDS));
         doubleEntryJS.setEvent(new DialogFieldClientJavascript.ControlEvent(DialogFieldClientJavascript.ControlEvent.LOSE_FOCUS));
         doubleEntryJS.setJsExpr(new StaticValueSource("doubleEntry(field, control)"));
         this.addClientJs(doubleEntryJS);
 
         DialogFieldClientJavascript deOnChangeJS = new DialogFieldClientJavascript();
         deOnChangeJS.setType(new DialogFieldClientJavascript.ScriptType(DialogFieldClientJavascript.ScriptType.EXTENDS));
         deOnChangeJS.setEvent(new DialogFieldClientJavascript.ControlEvent(DialogFieldClientJavascript.ControlEvent.VALUE_CHANGED));
         deOnChangeJS.setJsExpr(new StaticValueSource("field.successfulEntry = false"));
         this.addClientJs(deOnChangeJS);
     }
 
     /**
      * Gets the dependent conditions of this field
      */
     public DialogFieldConditionalActions getDependentConditions()
     {
         return dependentConditions;
     }
 
     /**
      * Indicates whether or not the field is a required field. It checks the  <code>Flags.REQUIRED</code>
      * flag of the field and its' children.
      *
      * @param dc dialog context
      */
     public boolean isRequired(DialogContext dc)
     {
         if(dc.getFieldStates().getState(this).getStateFlags().flagIsSet(DialogFieldFlags.REQUIRED))
             return true;
 /*
 		if (children != null)
 		{
             for(int i = 0; i < children.size(); i++)
             {
 				if (children.get(i).isRequired(dc))
 					return true;
 			}
 		}
         */
         return false;
     }
 
     /**
      * Returns a required field missin message
      */
     public String getRequiredFieldMissingMessage()
     {
         return requiredFieldMissingMessage;
     }
 
     /**
      * Sets the display error message for use when a field is required and no value is entered for it
      */
     public void setRequiredFieldMissingMessage(String requiredFieldMissingMessage)
     {
         this.requiredFieldMissingMessage = requiredFieldMissingMessage;
     }
 
     /**
      * Checks whether or not the field is available in the form. The check is done by seeing if the available flag,
      * <code>Flags.UNAVAILABLE</code> is set or not and by making sure each partner field of its' conditionals have a
      * value or not.
      *
      * @param dc dialog context
      *
      * @return boolean True if the field is visible
      */
     public boolean isAvailable(DialogContext dc)
     {
         for(int i = 0; i < conditionalActions.size(); i++)
         {
             DialogFieldConditionalAction action = conditionalActions.getAction(i);
             if(action instanceof DialogFieldConditionalData)
             {
 // if the partner field doesn't have data yet, hide this field
                 if(isRequired(dc))
                 {
                     if(!dc.getFieldStates().getState(action.getPartnerField()).hasRequiredValue())
                         return false;
                 }
             }
         }
 
         DialogField.State state = dc.getFieldStates().getState(this);
         DialogFieldFlags stateFlags = state.getStateFlags();
 
         if(stateFlags.flagIsSet(DialogFieldFlags.UNAVAILABLE))
             return false;
 
         if(children == null && stateFlags.flagIsSet(DialogFieldFlags.READ_ONLY) &&
            (stateFlags.flagIsSet(DialogFieldFlags.READONLY_UNAVAILABLE_UNLESS_HAS_DATA) ||
             dc.getDialog().getDialogFlags().flagIsSet(DialogFlags.READONLY_FIELDS_UNAVAILABLE_UNLESS_HAVE_DATA)))
         {
             Object value = state.getValue().getValue();
             return value == null
                    ? false : (value instanceof String ? (((String) value).length() == 0 ? false : true) : true);
         }
         else
             return true;
     }
 
     /**
      * Checks to see if the field state's  has a read only flag set
      */
     public boolean isReadOnly(DialogContext dc)
     {
         DialogField.State state = dc.getFieldStates().getState(this);
         return state.getStateFlags().flagIsSet(DialogFieldFlags.READ_ONLY);
     }
 
     /**
      * Checks to see if the field state's has a browser read-only flag set. The browser read-only is same as read-only but
      * the display style is different
      */
     public boolean isBrowserReadOnly(DialogContext dc)
     {
         DialogField.State state = dc.getFieldStates().getState(this);
         return state.getStateFlags().flagIsSet(DialogFieldFlags.BROWSER_READONLY);
     }
 
     /**
      * Checks to see if the field state's  hidden flag is set
      */
     public boolean isInputHiddenFlagSet(DialogContext dc)
     {
         DialogField.State state = dc.getFieldStates().getState(this);
         return state.getStateFlags().flagIsSet(DialogFieldFlags.INPUT_HIDDEN);
     }
 
     /**
      * Checks to see if the field should be hidden based on all flags
      */
     public boolean isInputHidden(DialogContext dc)
     {
         DialogField.State state = dc.getFieldStates().getState(this);
         DialogFieldFlags stateFlags = state.getStateFlags();
 
         if(stateFlags.flagIsSet(DialogFieldFlags.INPUT_HIDDEN))
             return true;
 
         if(children == null && stateFlags.flagIsSet(DialogFieldFlags.READ_ONLY) &&
            (stateFlags.flagIsSet(DialogFieldFlags.READONLY_HIDDEN_UNLESS_HAS_DATA) ||
             dc.getDialog().getDialogFlags().flagIsSet(DialogFlags.READONLY_FIELDS_HIDDEN_UNLESS_HAVE_DATA)))
         {
             return !state.hasRequiredValue();
         }
         else
             return flags.flagIsSet(DialogFieldFlags.INPUT_HIDDEN);
     }
 
     /**
      * Checks to see if the field value should be persistent across dialog states
      */
     public boolean persistValue()
     {
         return flags.flagIsSet(DialogFieldFlags.PERSIST);
     }
 
     /**
      * Checks to see if captions of child fields should be shown
      */
     public boolean showCaptionAsChild()
     {
         return flags.flagIsSet(DialogFieldFlags.SHOW_CAPTION_AS_CHILD);
     }
 
     public String getHiddenControlHtml(DialogContext dc)
     {
         DialogField.State state = dc.getFieldStates().getState(this);
         String value = state.getValue() != null ? state.getValue().getTextValue() : null;
         return "<input type='hidden' id=\"" + getHtmlFormControlId() + "\" name='" +
                getHtmlFormControlId() + "' value=\"" + (value != null
                                                         ? TextUtils.getInstance().escapeHTML(value) : "") + "\">";
     }
 
     /**
      * Renders the input part of the field
      */
     public void renderControlHtml(Writer writer, DialogContext dc) throws IOException
     {
         if(isInputHidden(dc))
         {
             getHiddenControlHtml(dc);
             return;
         }
 
         if(children == null)
             return;
 
         dc.getSkin().renderCompositeControlsHtml(writer, dc, this);
     }
 
     /**
      * Checks to see if the field needs validation. Also checks the child fields to verify if validation is needed or not.
      *
      * @param dc dialog context
      *
      * @return True if the field needs validation
      */
     public boolean needsValidation(DialogContext dc)
     {
         if(flags.flagIsSet(DialogFieldFlags.HAS_CONDITIONAL_DATA) || validationRules.size() > 0)
             return true;
 
         if(children == null)
             return isRequired(dc);
 
         int validateFieldsCount = 0;
         for(int i = 0; i < children.size(); i++)
         {
             DialogField field = children.get(i);
             if(field.isAvailable(dc) && field.needsValidation(dc))
                 validateFieldsCount++;
         }
 
         return validateFieldsCount > 0 ? true : false;
     }
 
     /**
      * Performs the validation of the field
      */
     public void validate(DialogValidationContext dvc)
     {
         DialogContext dc = dvc.getDialogContext();
         State fieldState = dc.getFieldStates().getState(this);
 
         for(int i = 0; i < conditionalActions.size(); i++)
         {
             DialogFieldConditionalAction action = conditionalActions.getAction(i);
             if(action instanceof DialogFieldConditionalData)
             {
 // if the partner field doesn't have data, then this field is "invalid"
                 if(isRequired(dc))
                 {
                     if(!dc.getFieldStates().getState(action.getPartnerField()).hasRequiredValue())
                     {
                         dvc.addValidationError(fieldState.getValidationContextScope(), getRequiredFieldMissingMessage(), new Object[]{
                             action.getPartnerField().getErrorCaption().getTextValue(dc)
                         });
                         return;
                     }
                 }
             }
         }
 
         if(isRequired(dvc.getDialogContext()))
         {
             if(!fieldState.hasRequiredValue())
             {
                 dvc.addValidationError(fieldState.getValidationContextScope(), getRequiredFieldMissingMessage(), new Object[]{
                     getErrorCaption().getTextValue(dc)
                 });
                 return;
             }
         }
 
         if(validationRules.size() > 0)
             validationRules.validateValue(dvc, fieldState.getValue());
 
         if(children != null)
         {
             for(int i = 0; i < children.size(); i++)
             {
                 DialogField field = children.get(i);
                 if(field.isAvailable(dc)) field.validate(dvc);
             }
         }
     }
 
     /**
      * Format the dialog value after it has been validated and is ready for submission
      *
      * @param value dialog field value
      *
      * @return String
      */
     public String formatSubmitValue(String value)
     {
         return value;
     }
 
     /**
      * Format the dialog value for every dialog stage except before submission
      *
      * @param value dialog field value
      *
      * @return String
      */
     public String formatDisplayValue(String value)
     {
         return value;
     }
 
     /**
      * Performs the state change of the field in accordance with the dialog state change
      */
     public void makeStateChanges(DialogContext dc, int stage)
     {
         if(stage == DialogContext.STATECALCSTAGE_BEFORE_VALIDATION)
         {
             for(int i = 0; i < conditionalActions.size(); i++)
             {
                 DialogFieldConditionalAction action = conditionalActions.getAction(i);
                 if(action instanceof DialogFieldConditionalApplyFlag)
                     ((DialogFieldConditionalApplyFlag) action).applyFlags(dc);
             }
         }
 
         if(children != null)
         {
             for(int i = 0; i < children.size(); i++)
             {
                 DialogField field = children.get(i);
                 field.makeStateChanges(dc, stage);
             }
         }
     }
 
     /**
      * Fills the field with value
      */
     public void populateValue(DialogContext dc, int formatType)
     {
         if(htmlFormControlId == null) return;
 
         DialogField.State state = dc.getFieldStates().getState(this);
         DialogFieldValue dfValue = state.getValue();
         String textValue = dfValue.getTextValue();
 
         if(state.isLoadedPersistentValue())
         {
             // if we loaded a value from a cookie but there is an override param, the param takes precedence
             String overrideParam = dc.getRequest().getParameter(htmlFormControlId);
             if(overrideParam != null)
                 textValue = overrideParam;
         }
         else if(textValue == null)
             textValue = dc.getRequest().getParameter(htmlFormControlId);
 
         if(dc.getDialogState().getRunSequence() == 1)
         {
             if((textValue != null && textValue.length() == 0 && defaultValue != null) ||
                (textValue == null && defaultValue != null))
                 textValue = defaultValue.getTextValueOrBlank(dc);
         }
 
         if(formatType == DialogField.DISPLAY_FORMAT)
             dfValue.setTextValue(formatDisplayValue(textValue));
         else if(formatType == DialogField.SUBMIT_FORMAT)
             dfValue.setTextValue(formatSubmitValue(textValue));
 
         if(children == null) return;
 
         for(int i = 0; i < children.size(); i++)
         {
             DialogField field = children.get(i);
             if(field.isAvailable(dc)) field.populateValue(dc, formatType);
         }
     }
 
     /**
      * Produces JavaScript code to handle Client-side events for the dialog field
      */
     public String getJavaScriptDefn(DialogContext dc)
     {
         String fieldClassName = this.getClass().getName();
         String fieldQualfName = this.getQualifiedName();
         String fieldCaption = caption != null ? caption.getTextValueOrBlank(dc) : "";
         String js =
                 "field = new DialogField(\"" + fieldClassName + "\", \"" + this.getHtmlFormControlId() + "\", \"" + this.getName() + "\", \"" + fieldQualfName + "\", \"" + fieldCaption + "\", " + dc.getFieldStates().getState(this).getStateFlags().getFlags() + ");\n" +
                 "dialog.registerField(field);\n";
 // if the field has a parent field, set the parent name
         if(getParent() != null)
             js = js + "field.parentName = '" + getParent().getQualifiedName() + "';\n";
         String customStr = this.getEventJavaScriptFunctions(dc);
         customStr += this.getCustomJavaScriptDefn(dc);
         if(customStr != null)
             js += customStr;
 
         if(dependentConditions.size() > 0)
         {
             StringBuffer dcJs = new StringBuffer();
             for(int i = 0; i < dependentConditions.size(); i++)
             {
                 DialogFieldConditionalAction o = dependentConditions.getAction(i);
                 if(o instanceof DialogFieldConditionalClear)
                 {
                     DialogFieldConditionalClear action = (DialogFieldConditionalClear) o;
                     if(action.getPartnerField().isAvailable(dc))
                         dcJs.append("field.dependentConditions[field.dependentConditions.length] = new DialogFieldConditionalClear(\"" + action.getSourceField().getQualifiedName()
                                     + "\", \"" + action.getPartnerField().getQualifiedName() + "\", \"" + action.getExpression() + "\");\n");
                     else
                         log.warn("Javascript dependent condition was not added because the partner field with name '" + action.getPartnerFieldName() + "' was not available.");
 
                 }
                 else if(o instanceof DialogFieldConditionalDisplay)
                 {
                     DialogFieldConditionalDisplay action = (DialogFieldConditionalDisplay) o;
                     if(action.getPartnerField().isAvailable(dc))
                         dcJs.append("field.dependentConditions[field.dependentConditions.length] = new DialogFieldConditionalDisplay(\"" + action.getSourceField().getQualifiedName()
                                     + "\", \"" + action.getPartnerField().getQualifiedName() + "\", \"" + action.getExpression() + "\");\n");
                     else
                         log.warn("Javascript dependent condition was not added because the partner field with name '" + action.getPartnerFieldName() + "' was not available.");
                 }
                 else if(o instanceof DialogFieldConditionalApplyFlag)
                 {
                     DialogFieldConditionalApplyFlag condition = (DialogFieldConditionalApplyFlag) o;
                     DialogFieldFlags flags = condition.getFlags();
                     if(flags.flagIsSet(DialogFieldFlags.INPUT_HIDDEN) || flags.flagIsSet(DialogFieldFlags.REQUIRED) || flags.flagIsSet(DialogFieldFlags.BROWSER_READONLY))
                     {
                         // the INPUT HIDDEN flag needs to be handled on both the client side and the server side if the
                         // condition is based on client-side actions
                         ValueSource hasValue = condition.getHasValue();
                         if(hasValue != null && hasValue instanceof DialogFieldValueSource)
                         {
                             // based on another field
                             if(condition.getPartnerField().isAvailable(dc))
                             {
                                 dcJs.append("field.dependentConditions[field.dependentConditions.length] = new DialogFieldConditionalFlag(\"" +
                                             condition.getSourceField().getQualifiedName() + "\", \"" +
                                             condition.getPartnerField().getQualifiedName() + "\", \"" +
                                             condition.getExpression(dc) + "\", " + flags.getFlags() + ", " + !condition.isClear() + ");\n");
                             }
                             else
                                 log.warn("Javascript dependent condition was not added because " +
                                          "the field with name '" + condition.getPartnerFieldName() + "' set in has-value attribute was not available.");
                         }
 
                     }
                 }
 
             }
             js = js + dcJs.toString();
         }
 
         if(children != null)
         {
             StringBuffer childJs = new StringBuffer();
             for(int i = 0; i < children.size(); i++)
             {
                 DialogField child = children.get(i);
                 childJs.append(child.getJavaScriptDefn(dc));
             }
             js = js + childJs.toString();
         }
 
         return js;
     }
 
     /**
      * Retrieves user defined java script strings for this field and creates JS functions
      * out of them
      */
     public String getEventJavaScriptFunctions(DialogContext dc)
     {
         String ret = "";
 
         List jsList = this.getClientJavascripts();
         if(jsList != null && !jsList.isEmpty())
         {
             String eventName = "";
 
             StringBuffer jsBuffer = new StringBuffer();
             Iterator i = jsList.iterator();
             while(i.hasNext())
             {
                 DialogFieldClientJavascript jsObject = (DialogFieldClientJavascript) i.next();
                 String script = (jsObject.getJsExpr() != null ? jsObject.getJsExpr().getTextValue(dc) : null);
                 eventName = TextUtils.getInstance().xmlTextToJavaIdentifier(jsObject.getEvent().getValue(), false);
                 // append function signature
                 if(script != null)
                 {
                     jsBuffer.append("field.customHandlers." + eventName + " = new Function(\"field\", \"control\", \"" +
                                     jsObject.getJsExpr().getTextValue(dc) + "\");\n");
                     jsBuffer.append("field.customHandlers." + eventName + "Type = '" + jsObject.getType().getValue() + "';\n");
                 }
             }
             ret = ret + jsBuffer.toString();
         }
         return ret;
     }
 
     /**
      * Gets the custome javascript definitions for this field
      *
      * @param dc dialog context
      *
      * @return String containing the custom javascript
      */
     public String getCustomJavaScriptDefn(DialogContext dc)
     {
         StringBuffer sb = new StringBuffer();
 
         if(this.isBrowserReadOnly(dc))
             sb.append("field.readonly = 'yes';\n");
         else
             sb.append("field.readonly = 'no';\n");
 
         if(flags.flagIsSet(DialogFieldFlags.IDENTIFIER))
             sb.append("field.identifier = 'yes';\n");
         else
             sb.append("field.identifier = 'no';\n");
 
         if(flags.flagIsSet(DialogFieldFlags.DOUBLE_ENTRY))
         {
             sb.append("field.doubleEntry = 'yes';\n");
             sb.append("field.firstEntryValue = '';\n");
             sb.append("field.successfulEntry = true;\n");
         }
 
         if(flags.flagIsSet(DialogFieldFlags.SCANNABLE))
         {
             sb.append("field.scannable = 'yes';\n");
             sb.append("field.scanStartCode = '" + scanEntry.getStartCode() + "';\n");
             sb.append("field.scanStopCode = '" + scanEntry.getStopCode() + "';\n");
             sb.append("field.scanCodeIgnoreCase = '" + scanEntry.getIgnoreCase() + "';\n");
             sb.append("field.isScanned = false;\n");
             sb.append("field.scanPartnerField = '" + scanEntry.getPartnerField() + "';\n");
             if(scanEntry.getCustomScript() != null && scanEntry.getCustomScript().length() > 0)
                 sb.append("field.scanFieldCustomScript = new Function(\"field\", \"control\", \"inputString\", \"" + scanEntry.getCustomScript() + "\");\n");
             else
                 sb.append("field.scanFieldCustomScript = '';\n");
         }
 
         if(flags.flagIsSet(DialogFieldFlags.AUTO_BLUR))
         {
             sb.append("field.autoBlur = 'yes';\n");
             sb.append("field.autoBlurLength = " + autoBlur.getLength() + ";\n");
             sb.append("field.autoBlurExcRegExp = '" + autoBlur.getExcludeExpr() + "';\n");
             sb.append("field.numCharsEntered = 0;\n");
         }
 
         if(flags.flagIsSet(DialogFieldFlags.SUBMIT_ONBLUR))
         {
             sb.append("field.submitOnBlur = true;\n");
             if(submitOnBlur.getPartner() != null)
                 sb.append("field.submitOnBlurPartnerField = '" + submitOnBlur.getPartner() + "';\n");
             if(submitOnBlur.getCustomScript() != null && submitOnBlur.getCustomScript().length() > 0)
                 sb.append("field.submitOnBlurCustomScript = new Function(\"field\", \"control\", \"" + submitOnBlur.getCustomScript() + "\");\n");
             else
                 sb.append("field.submitOnBlurCustomScript = '';\n");
         }
 
         if(encryption != null)
             encryption.addCustomJavaScriptDefn(dc, sb);
 
         return sb.toString();
     }
 
     /**
      * Produces Java code when a custom DialogContext is created
      */
     public DialogContextBeanMemberInfo createDialogContextMemberInfo()
     {
         DialogContextBeanMemberInfo mi = new DialogContextBeanMemberInfo(this.getQualifiedName());
 
         String memberName = mi.getMemberName();
         String fieldName = mi.getFieldName();
 
         if(memberName == null || fieldName == null)
             return mi;
 
         String fieldClassName = this.getClass().getName().replace('$', '.');
         String stateClassName = getStateClass().getName().replace('$', '.');
         String stateValueClassName = getStateValueClass().getName().replace('$', '.');
 
         if(stateClassName != DialogField.State.class.getName())
             mi.addJavaCode("\tpublic " + stateClassName + " get" + mi.getMemberName() + "State() { return (" + stateClassName + ") fieldStates.getState(\"" + mi.getFieldName() + "\"); }\n");
         else
             mi.addJavaCode("\tpublic DialogField.State get" + mi.getMemberName() + "State() { return fieldStates(\"" + mi.getFieldName() + "\"); }\n");
 
         mi.addJavaCode("\tpublic " + stateValueClassName + " get" + memberName + "() { return (" + stateValueClassName + ") get" + mi.getMemberName() + "State().getValue(); }\n");
         mi.addJavaCode("\tpublic DialogFieldFlags get" + memberName + "StateFlags() { return get" + mi.getMemberName() + "State().getStateFlags(); }\n");
         mi.addJavaCode("\tpublic String get" + memberName + "PrivateRequestParam() { return dialogContext.getRequest().getParameter(\"" + Dialog.PARAMNAME_CONTROLPREFIX + fieldName + "\"); }\n");
         mi.addJavaCode("\tpublic String get" + memberName + "PublicRequestParam() { return dialogContext.getRequest().getParameter(\"" + fieldName + "\"); }\n");
         mi.addJavaCode("\tpublic " + fieldClassName + " get" + memberName + "Field() { return (" + fieldClassName + ") get" + mi.getMemberName() + "State().getField(); }\n");
 
         return mi;
     }
 
     /**
      * Produces Java code when a custom DialogContext is created
      * The default method produces nothing; all the subclasses must define what they need.
      */
     public DialogContextBeanMemberInfo getDialogContextBeanMemberInfo()
     {
         return createDialogContextMemberInfo();
     }
 }
