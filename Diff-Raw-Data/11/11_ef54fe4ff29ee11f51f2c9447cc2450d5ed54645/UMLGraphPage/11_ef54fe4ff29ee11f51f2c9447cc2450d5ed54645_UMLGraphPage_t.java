 /*
  * (C) Copyright 2005 Antti Hakala
  * 
  *  Permission to use, copy, and distribute this software and its documentation 
  *  for any purpose and without fee is hereby granted, provided that the above 
  *  copyright notice appear in all copies and that both that copyright notice and 
  *  this permission notice appear in supporting documentation.
  *  
  *  THIS SOFTWARE IS PROVIDED ``AS IS'' AND WITHOUT ANY EXPRESS OR IMPLIED WARRANTIES,
  *  INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTIES OF MERCHANTIBILITY AND
  *  FITNESS FOR A PARTICULAR PURPOSE. 
  */
 
 package org.lightuml.ui.preferences;
 
 import org.eclipse.jface.preference.FileFieldEditor;
 import org.eclipse.jface.preference.RadioGroupFieldEditor;
 
 /**
  * <p>
  * Preference subpage for UMLGraph doclet.
  * </p>
  * 
  * @author Antti Hakala
  */
 public class UMLGraphPage extends LightUMLPreferencePage {
     /**
      * <p>
      * Constructor.
      * </p>
      */
     public UMLGraphPage() {
         setDescription("Preferences for UMLGraph:");
     }
 
     /**
      * <p>
      * Creates the field editors.
      * </p>
      */
     public void createFieldEditors() {
         addField(new FileFieldEditor(P_UMLGRAPH_JAR_PATH, "UmlGraph.jar path:",
                 true, getFieldEditorParent()));
         addField(new FileFieldEditor(P_PIC_MACROS_PATH, "sequence.pic path:",
                 true, getFieldEditorParent()));
         addField(new RadioGroupFieldEditor(P_UMLGRAPH_DOCLET_NAME,
                "UMLGraph version. LightUML supports three versions of UMLGraph.", 
                 1, VERSION_DOCLET_NAME, getFieldEditorParent()));
     }
     
     // TODO: update preferences shown in class diagrams page.
     // if UMLGraph-2.10 is selected, options for newer versions
     // should be removed. Now they are removed only when preferences
     // window is opened again.
 }
