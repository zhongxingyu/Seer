 package com.radicaldynamic.turboform.utilities;
 
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 
 import android.util.Base64;
 import android.util.Log;
 
 import com.mycila.xmltool.CallBack;
 import com.mycila.xmltool.XMLDoc;
 import com.mycila.xmltool.XMLTag;
 import com.radicaldynamic.turboform.application.Collect;
 import com.radicaldynamic.turboform.xform.Bind;
 import com.radicaldynamic.turboform.xform.Control;
 import com.radicaldynamic.turboform.xform.Translation;
 import com.radicaldynamic.turboform.xform.TranslationText;
 
 public class FormUtils
 {
     private static final String t = "FormUtils: ";
     
     private XMLTag mForm;                           // The "form" as it was loaded by xmltool
     private String mInstanceRoot;                   // The name of the instance root element 
     private String mDefaultPrefix;                  // The name of the default Xform prefix (needed for navigation)    
     private ArrayList<String> mControlList = new ArrayList<String>();    
     
     // State of controls and other form elements    
     private ArrayList<Control> mControlState = new ArrayList<Control>();
     private ArrayList<Translation> mTranslationState = new ArrayList<Translation>();
     private ArrayList<Bind> mBindState = new ArrayList<Bind>(); 
     
     {
         // List of valid controls that we can handle
         Collections.addAll(mControlList, "group", "input", "item", "repeat", "select", "select1", "trigger", "upload");
     }
     
     public FormUtils(String title) {
         //XMLTag mForm = XMLDoc.from(, false);
         System.out.println(mForm.toString());
     }
     
     public FormUtils(InputStream is)
     {
         mForm = (XMLDoc.from(is, false));
         mDefaultPrefix = mForm.getPefix("http://www.w3.org/2002/xforms");
         mInstanceRoot = mForm.gotoTag("h:head/%1$s:model/%1$s:instance", mDefaultPrefix).gotoChild().getCurrentTagName();
         
         Log.d(Collect.LOGTAG, t + "default prefix for form: " + mDefaultPrefix);
         Log.d(Collect.LOGTAG, t + "instance root element name: " + mInstanceRoot);
     }
     
     public ArrayList<Control> getControlState()
     {
         return mControlState;
     }
     
     public ArrayList<Translation> getTranslationState()
     {
         return mTranslationState;
     }
 
     public XMLTag getForm()
     {
         return mForm;
     }
     
     /*
      * Returns a Base64 encoded form
      */
     public String getEncodedForm()
     {
         return Base64.encodeToString(mForm.gotoRoot().toBytes(), Base64.DEFAULT);       
     }
     
     /*
      * Trigger method for doing all of the actual work
      */
     public void parseForm()
     {
         if (mForm.gotoRoot().gotoTag("h:head/%1$s:model", mDefaultPrefix).hasTag("%1$s:itext", mDefaultPrefix)) {
             Log.d(Collect.LOGTAG, t + "parsing itext form translations...");
             parseFormTranslations(mForm.gotoRoot().gotoTag("h:head/%1$s:model/%1$s:itext", mDefaultPrefix));
         } else
             Log.d(Collect.LOGTAG, t + "no form translations to parse");        
         
         Log.d(Collect.LOGTAG, t + "parsing form binds...");
         parseFormBinds(mForm.gotoRoot().gotoTag("h:head/%1$s:model", mDefaultPrefix));
 
         Log.d(Collect.LOGTAG, t + "parsing form controls...");
         parseFormControls(mForm.gotoRoot().gotoTag("h:body"));
         
         Log.d(Collect.LOGTAG, t + "parsing form instance...");
         parseFormInstance(mForm.gotoRoot().gotoTag("h:head/%1$s:model/%1$s:instance", mDefaultPrefix).gotoChild(), "/" + mInstanceRoot);
         Log.d(Collect.LOGTAG, t + "FINISHED parsing form instance");
     }
     
     /*
      * Recursively iterate over the form controls, creating objects to represent these controls
      */
     private void parseFormControls(XMLTag tag)
     {       
         Log.v(Collect.LOGTAG, t + "visiting <" + tag.getCurrentTagName() + ">");
         
         if (mControlList.contains(tag.getCurrentTagName())) {
             if (tag.getCurrentTagLocation().split("/").length == 2) {                
                 mControlState.add(new Control(tag, null, mInstanceRoot, mBindState));
             } else {
                 attachChildToParentControl(tag, null);
             }
         } else if (tag.getCurrentTagName().equals("label")) {
             applyProperty(tag);
         } else if (tag.getCurrentTagName().equals("hint")) {
             applyProperty(tag);
         } else if (tag.getCurrentTagName().equals("value")) {
             applyProperty(tag);
         }
 
         // If control element has children then listControls recursively from the standpoint of every child
         if (tag.getChildCount() > 0) {           
             tag.forEachChild(new CallBack() {
                 @Override
                 public void execute(XMLTag arg0)
                 {
                     parseFormControls(arg0);
                 }
             });
         }
     }
 
     /*
      * For controls that are nested within other controls (e.g., not top-level group or repeat controls) 
      * determine which control is the parent and attach the new control to it as a child.
      * 
      * This uses the same test for determining a parent as recursivelyApplyProperty().
      */
     private void attachChildToParentControl(XMLTag child, Control incomingParent)
     {
         Iterator<Control> it = null;
         
         if (incomingParent == null)
             it = mControlState.iterator();
         else
             it = incomingParent.children.iterator();
         
         while (it.hasNext()) {
             Control parent = it.next();
             
             if (child.getCurrentTagLocation().split("/").length - parent.getLocation().split("/").length == 1 &&
                     parent.getLocation().equals(child.getCurrentTagLocation().substring(0, parent.getLocation().length())))
                 parent.children.add(new Control(child, parent, mInstanceRoot, mBindState));                
             
             if (!parent.children.isEmpty())
                 attachChildToParentControl(child, parent);
         }
     }
     
     /*
      * Iterate recursively through the list of controls stopping only when the correct control object 
      * has been found (the control object to which this property applies and should be set for)
      */
     private boolean applyProperty(XMLTag tag)
     {
         Iterator<Control> it = mControlState.iterator();
         
         while (it.hasNext()) {
             Control c = it.next();
             if (recursivelyApplyProperty(c, tag)) {
                 return true;
             }
         }
         
         return false;
     }
     
     /*
      * Recursive method for applyProperty().  Determines the proper object to apply the property to by 
      * comparing the property location and the control object location.  E.g.,
      * 
      * If the control exists at *[2]/*[2]/*[3] then the property located at  *[2]/*[2]/*[3]/*[1] or 
      * any other direct child of *[2]/*[2]/*[3] should be assigned to same. 
      */
     private boolean recursivelyApplyProperty(Control targetControl, XMLTag tag)
     {
         if (tag.getCurrentTagLocation().split("/").length - targetControl.getLocation().split("/").length == 1 &&
                 targetControl.getLocation().equals(tag.getCurrentTagLocation().substring(0, targetControl.getLocation().length()))) {           
             String label = tag.getInnerText();
             
             /*
              * Obtain a single translation to represent this control on the form builder screen
              * 
              * FIXME: We should select the most appropriate language (not necessarily English)
              *        before falling back to English.  The most appropriate language can be determined
              *        by checking the locale of the device. 
              */ 
             if (tag.hasAttribute("ref")) {
                 String ref = tag.getAttribute("ref");
                 String [] items = ref.split("'");           // jr:itext('GroupLabel')
                 String id = items[1];                       // The string between the single quotes
                 
                 label = getTranslation("English", id);
                 label = label + " [i18n]";
             }
             
             // Set the label
             if (tag.getCurrentTagName().contains("label")) {
                 if (tag.hasAttribute("ref")) 
                     targetControl.setLabel(tag.getAttribute("ref"));
                     
                 targetControl.setLabel(label);                
             }
             
             // Set the hint
             if (tag.getCurrentTagName().contains("hint")) {
                 if (tag.hasAttribute("ref")) 
                     targetControl.setHint(tag.getAttribute("ref"));
                 
                 targetControl.setHint(label);                
             }
             
             if (tag.getCurrentTagName().contains("value")) {
                 targetControl.setItemValue(tag.getInnerText());
             }
             
             return true;
         }
         
         Iterator<Control> children = targetControl.children.iterator();
         
         while (children.hasNext()) {
             Control child = children.next();
             
             if (recursivelyApplyProperty(child, tag))
                 return true;
         }
         
         // This should never happen
         return false;
     }
     
     /*
      * Recursively parse the form translations, adding objects to mTranslationState to represent them
      */
     private void parseFormTranslations(XMLTag tag)
     {
         if (tag.getCurrentTagName().equals("translation")) {
             Log.v(Collect.LOGTAG, t + "adding translations for " + tag.getAttribute("lang"));
             mTranslationState.add(new Translation(tag.getAttribute("lang")));
         } else if (tag.getCurrentTagName().equals("text")) {
             Log.v(Collect.LOGTAG, t + "adding translation ID " + tag.getAttribute("id"));
             mTranslationState.get(mTranslationState.size() -1).texts.add(new TranslationText(tag.getAttribute("id")));
         } else if (tag.getCurrentTagName().equals("value")) {
             Log.v(Collect.LOGTAG, t + "adding translation: " + tag.getInnerText());
             mTranslationState.get(mTranslationState.size() - 1)
                 .texts.get(mTranslationState.get(mTranslationState.size() - 1).texts.size() - 1)
                 .setValue(tag.getInnerText());
         }
  
         if (tag.getChildCount() > 0) {           
             tag.forEachChild(new CallBack() {
                 @Override
                 public void execute(XMLTag arg0)
                 {
                     parseFormTranslations(arg0);
                 }
             });
         }
     }
     
     /*
      * Retrieve a translation for a specific ID from a specific language.
      */
     private String getTranslation(String language, String id)
     {
         Iterator<Translation> translations = mTranslationState.iterator();
         
         while (translations.hasNext()) {
             Translation translation = translations.next();
             
             if (translation.getLang().equals(language)) {
                 Iterator<TranslationText> texts = translation.texts.iterator();
                 
                 while (texts.hasNext()) {
                     TranslationText text = texts.next();
                     
                     if (text.getId().equals(id)) {
                         text.setUsed(true);
                         return text.getValue();
                     }
                 }
             }
         }
         
         return "[Translation Not Available]";
     }
     
     /*
      * Go through the list of binds and build objects to represent them.
      * Binds are associated with control objects when the control is instantiated.
      */
     private void parseFormBinds(XMLTag tag)
     {
         tag.forEachChild(new CallBack() {
             @Override
             public void execute(XMLTag arg0)
             {
                 if (arg0.getCurrentTagName().equals("bind"))
                     mBindState.add(new Bind(arg0, mInstanceRoot));                    
             }
         });
     }
     
     /*
      * Recursively parse and use the information supplied in the form instance
      * to supplement the control objects in mControlState 
      */
     private void parseFormInstance(XMLTag tag, final String instancePath)
     {
        // Search controls for an object having a ref that matches this instancePath
        if (!applyInstanceToControl(null, tag, instancePath)) {
            // FIXME: this does not guarantee that the control will be added to the correct parent
            mControlState.add(new Control(tag, mBindState, instancePath));
         }
         
         tag.forEachChild(new CallBack() {
             @Override
             public void execute(XMLTag arg0)
             {
                 parseFormInstance(arg0, instancePath + "/" + arg0.getCurrentTagName());
             }
         });
     }
     
     private boolean applyInstanceToControl(Control control, final XMLTag instanceTag, final String instancePath)
     {
         Iterator<Control> it;
         
         if (control == null) {
             it = mControlState.iterator();
         } else { 
             if (control.getRef() != null && control.getRef().equals(instancePath)) {
                 Log.v(Collect.LOGTAG, t + "instance matched with control object via " + instancePath);
                 control.setDefaultValue(instanceTag.getInnerText());
                 return true;
             }
             
             it = control.children.iterator();
         }
         
         while (it.hasNext()) {
             Control c = it.next();
             
             if (applyInstanceToControl(c, instanceTag, instancePath)) {
                 return true;
             }
         }
         
         return false;
     }
 }
