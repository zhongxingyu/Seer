 package org.coode.dlquery;
 
 import org.apache.log4j.Logger;
 import org.protege.editor.core.ui.util.ComponentFactory;
import org.protege.editor.core.ui.util.InputVerificationStatusChangedListener;
 import org.protege.editor.owl.model.description.OWLDescriptionParser;
 import org.protege.editor.owl.model.event.EventType;
 import org.protege.editor.owl.model.event.OWLModelManagerChangeEvent;
 import org.protege.editor.owl.model.event.OWLModelManagerListener;
 import org.protege.editor.owl.ui.clsdescriptioneditor.ExpressionEditor;
 import org.protege.editor.owl.ui.clsdescriptioneditor.OWLDescriptionChecker;
 import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
 import org.semanticweb.owl.model.OWLDescription;
 import org.semanticweb.owl.model.OWLException;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 /*
  * Copyright (C) 2007, University of Manchester
  *
  * Modifications to the initial code base are copyright of their
  * respective authors, or their employers as appropriate.  Authorship
  * of the modifications may be determined from the ChangeLog placed at
  * the end of this file.
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
 
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
 
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
  */
 
 
 /**
  * Author: Matthew Horridge<br>
  * The University Of Manchester<br>
  * Medical Informatics Group<br>
  * Date: 22-Aug-2006<br><br>
  * <p/>
  * matthew.horridge@cs.man.ac.uk<br>
  * www.cs.man.ac.uk/~horridgm<br><br>
  */
 public class OWLDescriptionEditorViewComponent extends AbstractOWLViewComponent {
     Logger log = Logger.getLogger(OWLDescriptionEditorViewComponent.class);
 
     private ExpressionEditor owlDescriptionEditor;
 
     private ResultsList resultsList;
 
     private JCheckBox showSuperClassesCheckBox;
 
     private JCheckBox showAncestorClassesCheckBox;
 
     private JCheckBox showEquivalentClassesCheckBox;
 
     private JCheckBox showSubClassesCheckBox;
 
     private JCheckBox showDescendantClassesCheckBox;
 
     private JCheckBox showIndividualsCheckBox;
 
     private OWLModelManagerListener listener;
 
     private JButton executeButton;
 
 
     protected void initialiseOWLView() throws Exception {
         setLayout(new BorderLayout(10, 10));
         JPanel editorPanel = new JPanel(new BorderLayout());
         owlDescriptionEditor = new ExpressionEditor<OWLDescription>(getOWLEditorKit(),
                                                                     new OWLDescriptionChecker(getOWLEditorKit()));
         owlDescriptionEditor.addStatusChangedListener(new InputVerificationStatusChangedListener(){
             public void verifiedStatusChanged(boolean newState) {
                 executeButton.setEnabled(newState);
             }
         });
         owlDescriptionEditor.setPreferredSize(new Dimension(100, 50));
         editorPanel.add(ComponentFactory.createScrollPane(owlDescriptionEditor), BorderLayout.CENTER);
         JPanel buttonHolder = new JPanel(new FlowLayout(FlowLayout.LEFT));
         executeButton = new JButton(new AbstractAction("Execute") {
             public void actionPerformed(ActionEvent e) {
                 doQuery();
             }
         });
         buttonHolder.add(executeButton, BorderLayout.NORTH);
         editorPanel.add(buttonHolder, BorderLayout.SOUTH);
         editorPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(
                 Color.LIGHT_GRAY), "Query (class expression)"), BorderFactory.createEmptyBorder(3, 3, 3, 3)));
         add(editorPanel, BorderLayout.NORTH);
 
         JPanel resultsPanel = new JPanel(new BorderLayout(10, 10));
         resultsPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(
                 Color.LIGHT_GRAY), "Query results"), BorderFactory.createEmptyBorder(3, 3, 3, 3)));
         add(resultsPanel);
         resultsList = new ResultsList(getOWLEditorKit());
         resultsPanel.add(ComponentFactory.createScrollPane(resultsList));
         Box optionsBox = new Box(BoxLayout.Y_AXIS);
         resultsPanel.add(optionsBox, BorderLayout.EAST);
         showSuperClassesCheckBox = new JCheckBox(new AbstractAction("Super classes") {
             public void actionPerformed(ActionEvent e) {
                 resultsList.setShowSuperClasses(showSuperClassesCheckBox.isSelected());
                 doQuery();
             }
         });
         optionsBox.add(showSuperClassesCheckBox);
         optionsBox.add(Box.createVerticalStrut(3));
 
         showAncestorClassesCheckBox = new JCheckBox(new AbstractAction("Ancestor classes") {
             public void actionPerformed(ActionEvent e) {
                 resultsList.setShowAncestorClasses(showAncestorClassesCheckBox.isSelected());
                 doQuery();
             }
         });
         showAncestorClassesCheckBox.setSelected(false);
         optionsBox.add(showAncestorClassesCheckBox);
         optionsBox.add(Box.createVerticalStrut(3));
 
         showEquivalentClassesCheckBox = new JCheckBox(new AbstractAction("Equivalent classes") {
             public void actionPerformed(ActionEvent e) {
                 resultsList.setShowEquivalentClasses(showEquivalentClassesCheckBox.isSelected());
                 doQuery();
             }
         });
         optionsBox.add(showEquivalentClassesCheckBox);
         optionsBox.add(Box.createVerticalStrut(3));
 
         showSubClassesCheckBox = new JCheckBox(new AbstractAction("Subclasses") {
             public void actionPerformed(ActionEvent e) {
                 resultsList.setShowSubClasses(showSubClassesCheckBox.isSelected());
                 doQuery();
             }
         });
         optionsBox.add(showSubClassesCheckBox);
         optionsBox.add(Box.createVerticalStrut(3));
 
         showDescendantClassesCheckBox = new JCheckBox(new AbstractAction("Descendant classes") {
             public void actionPerformed(ActionEvent e) {
                 resultsList.setShowDescendantClasses(showDescendantClassesCheckBox.isSelected());
                 doQuery();
             }
         });
         showDescendantClassesCheckBox.setSelected(false);
         optionsBox.add(showDescendantClassesCheckBox);
         optionsBox.add(Box.createVerticalStrut(3));
 
         showIndividualsCheckBox = new JCheckBox(new AbstractAction("Individuals") {
             public void actionPerformed(ActionEvent e) {
                 resultsList.setShowInstances(showIndividualsCheckBox.isSelected());
                 doQuery();
             }
         });
         optionsBox.add(showIndividualsCheckBox);
         updateGUI();
         listener = new OWLModelManagerListener() {
 
             public void handleChange(OWLModelManagerChangeEvent event) {
                 if (event.isType(EventType.ONTOLOGY_CLASSIFIED)) {
                     doQuery();
                 }
             }
         };
         getOWLModelManager().addListener(listener);
     }
 
 
     protected void disposeOWLView() {
         getOWLModelManager().removeListener(listener);
     }
 
 
     private void updateGUI() {
 
         showSuperClassesCheckBox.setSelected(resultsList.isShowSuperClasses());
         showAncestorClassesCheckBox.setSelected(resultsList.isShowAncestorClasses());
         showEquivalentClassesCheckBox.setSelected(resultsList.isShowEquivalentClasses());
         showSubClassesCheckBox.setSelected(resultsList.isShowSubClasses());
         showDescendantClassesCheckBox.setSelected(resultsList.isShowDescendantClasses());
         showIndividualsCheckBox.setSelected(resultsList.isShowInstances());
     }
 
 
     private void doQuery() {
         try {
             if (!getOWLModelManager().getReasoner().isClassified()) {
                 JOptionPane.showMessageDialog(this,
                                               "The reasoner is not syncronised.  This may produce misleading results.",
                                               "Reasoner out of sync",
                                               JOptionPane.WARNING_MESSAGE);
             }
 
             String exp = owlDescriptionEditor.getText();
 
             OWLDescriptionParser parser = getOWLModelManager().getOWLDescriptionParser();
             if (parser.isWellFormed(exp)) {
                 OWLDescription desc = parser.createOWLDescription(exp);
                 resultsList.setOWLDescription(desc);
             }
         }
         catch (OWLException e) {
             if (log.isDebugEnabled()) {
                 log.debug("Exception caught trying to do the query", e);
             }
         }
     }
 }
