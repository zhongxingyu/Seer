 package org.coode.dlquery;
 
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.swing.JComponent;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import org.protege.editor.core.ui.list.MList;
 import org.protege.editor.core.ui.list.MListButton;
 import org.protege.editor.owl.OWLEditorKit;
 import org.protege.editor.owl.ui.OWLClassExpressionComparator;
 import org.protege.editor.owl.ui.framelist.ExplainButton;
 import org.protege.editor.owl.ui.renderer.LinkedObjectComponent;
 import org.protege.editor.owl.ui.renderer.LinkedObjectComponentMediator;
 import org.protege.editor.owl.ui.view.Copyable;
 import org.semanticweb.owlapi.model.OWLClass;
 import org.semanticweb.owlapi.model.OWLClassExpression;
 import org.semanticweb.owlapi.model.OWLIndividual;
 import org.semanticweb.owlapi.model.OWLNamedIndividual;
 import org.semanticweb.owlapi.model.OWLObject;
 import org.semanticweb.owlapi.reasoner.Node;
 import org.semanticweb.owlapi.reasoner.OWLReasoner;
 
 
 /**
  * Author: Matthew Horridge<br>
  * The University Of Manchester<br>
  * Bio-Health Informatics Group<br>
  * Date: 27-Feb-2007<br><br>
  */
 public class ResultsList extends MList implements LinkedObjectComponent, Copyable {
 
     /**
      * 
      */
     private static final long serialVersionUID = 8184853513690586368L;
 
     private OWLEditorKit owlEditorKit;
 
     private boolean showSuperClasses;
 
     private boolean showAncestorClasses;
 
     private boolean showDescendantClasses;
 
     private boolean showSubClasses;
 
     private boolean showInstances;
 
     private boolean showEquivalentClasses;
 
     private LinkedObjectComponentMediator mediator;
 
     private List<ChangeListener> copyListeners = new ArrayList<ChangeListener>();
 
 
     public ResultsList(OWLEditorKit owlEditorKit) {
         this.owlEditorKit = owlEditorKit;
         setCellRenderer(new DLQueryListCellRenderer(owlEditorKit));
         explainButton.add(new ExplainButton(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
 
             }
         }));
         mediator = new LinkedObjectComponentMediator(owlEditorKit, this);
 
         getSelectionModel().addListSelectionListener(new ListSelectionListener(){
             public void valueChanged(ListSelectionEvent event) {
                 ChangeEvent ev = new ChangeEvent(ResultsList.this);
                 for (ChangeListener l : copyListeners){
                     l.stateChanged(ev);
                 }
             }
         });
     }
 
 
     public boolean isShowAncestorClasses() {
         return showAncestorClasses;
     }
 
 
     public void setShowAncestorClasses(boolean showAncestorClasses) {
         this.showAncestorClasses = showAncestorClasses;
     }
 
 
     public boolean isShowDescendantClasses() {
         return showDescendantClasses;
     }
 
 
     public void setShowDescendantClasses(boolean showDescendantClasses) {
         this.showDescendantClasses = showDescendantClasses;
     }
 
 
     public boolean isShowInstances() {
         return showInstances;
     }
 
 
     public void setShowInstances(boolean showInstances) {
         this.showInstances = showInstances;
     }
 
 
     public boolean isShowSubClasses() {
         return showSubClasses;
     }
 
 
     public void setShowSubClasses(boolean showSubClasses) {
         this.showSubClasses = showSubClasses;
     }
 
 
     public boolean isShowSuperClasses() {
         return showSuperClasses;
     }
 
 
     public void setShowSuperClasses(boolean showSuperClasses) {
         this.showSuperClasses = showSuperClasses;
     }
 
 
     public boolean isShowEquivalentClasses() {
         return showEquivalentClasses;
     }
 
 
     public void setShowEquivalentClasses(boolean showEquivalentClasses) {
         this.showEquivalentClasses = showEquivalentClasses;
     }
 
 
     private List<OWLClass> toSortedList(Set<OWLClass> clses) {
         OWLClassExpressionComparator descriptionComparator = new OWLClassExpressionComparator(owlEditorKit.getModelManager());
         List<OWLClass> list = new ArrayList<OWLClass>(clses);
         Collections.sort(list, descriptionComparator);
         return list;
     }
 
 
     public void setOWLClassExpression(OWLClassExpression description) {
         List<Object> data = new ArrayList<Object>();
         OWLReasoner reasoner = owlEditorKit.getModelManager().getReasoner();
         if (showEquivalentClasses) {
             final List<OWLClass> results = toSortedList(reasoner.getEquivalentClasses(description).getEntities());
             data.add(new DLQueryResultsSection("Equivalent classes (" + results.size() + ")"));
             for (OWLClass cls : results) {
                 data.add(new DLQueryResultsSectionItem(cls));
             }
         }
         if (showAncestorClasses) {
             final List<OWLClass> results = toSortedList(reasoner.getSuperClasses(description, false).getFlattened());
             data.add(new DLQueryResultsSection("Ancestor classes (" + results.size() + ")"));
             for (OWLClass superClass : results) {
                 data.add(new DLQueryResultsSectionItem(superClass));
             }
         }
         if (showSuperClasses) {
             final List<OWLClass> results = toSortedList(reasoner.getSuperClasses(description, true).getFlattened());
             data.add(new DLQueryResultsSection("Super classes (" + results.size() + ")"));
             for (OWLClass superClass : results) {
                 data.add(new DLQueryResultsSectionItem(superClass));
             }
         }
         if (showSubClasses) {
             // flatten and filter out owl:Nothing
             OWLClass owlNothing = owlEditorKit.getOWLModelManager().getOWLDataFactory().getOWLNothing();
             final Set<OWLClass> resultSet = new HashSet<OWLClass>();
             for (Node<OWLClass> clsSet : reasoner.getSubClasses(description, true)){
                 if (!clsSet.contains(owlNothing)){
                     resultSet.addAll(clsSet.getEntities());
                 }
             }
             final List<OWLClass> results = toSortedList(resultSet);
             data.add(new DLQueryResultsSection("Sub classes (" + results.size() + ")"));
             for (OWLClass subClass : results) {
                 data.add(new DLQueryResultsSectionItem(subClass));
             }
         }
         if (showDescendantClasses) {
             // flatten and filter out owl:Nothing
             OWLClass owlNothing = owlEditorKit.getOWLModelManager().getOWLDataFactory().getOWLNothing();
             final Set<OWLClass> resultSet = new HashSet<OWLClass>();
             for (Node<OWLClass> clsSet : reasoner.getSubClasses(description, false)){
                 if (!clsSet.contains(owlNothing)){
                     resultSet.addAll(clsSet.getEntities());
                 }
             }
             final List<OWLClass> results = toSortedList(resultSet);
             data.add(new DLQueryResultsSection("Descendant classes (" + results.size() + ")"));
             for (OWLClass cls : results) {
                 data.add(new DLQueryResultsSectionItem(cls));
             }
         }
 
         if (showInstances) {
            final Set<OWLNamedIndividual> results = reasoner.getInstances(description, false).getFlattened();
             data.add(new DLQueryResultsSection("Instances (" + results.size() + ")"));
             for (OWLIndividual ind : results) {
                 data.add(new DLQueryResultsSectionItem(ind));
             }
         }
         setListData(data.toArray());
     }
 
 
     private List<MListButton> explainButton = new ArrayList<MListButton>();
 
 
     protected List<MListButton> getButtons(Object value) {
         if (value instanceof DLQueryResultsSectionItem) {
             return explainButton;
         }
         else {
             return Collections.emptyList();
         }
     }
 
 
     public JComponent getComponent() {
         return this;
     }
 
 
     public OWLObject getLinkedObject() {
         return mediator.getLinkedObject();
     }
 
 
     public Point getMouseCellLocation() {
         Rectangle r = getMouseCellRect();
         if (r == null) {
             return null;
         }
         Point mousePos = getMousePosition();
         if (mousePos == null) {
             return null;
         }
         return new Point(mousePos.x - r.x, mousePos.y - r.y);
     }
 
 
     public Rectangle getMouseCellRect() {
         Point mousePos = getMousePosition();
         if (mousePos == null) {
             return null;
         }
         int sel = locationToIndex(mousePos);
         if (sel == -1) {
             return null;
         }
         return getCellBounds(sel, sel);
     }
 
 
     public void setLinkedObject(OWLObject object) {
         mediator.setLinkedObject(object);
     }
 
 
     public boolean canCopy() {
         return getSelectedIndices().length > 0;
     }
 
 
     public List<OWLObject> getObjectsToCopy() {
         List<OWLObject> copyObjects = new ArrayList<OWLObject>();
         for (Object sel : getSelectedValues()){
             if (sel instanceof DLQueryResultsSectionItem){
                 copyObjects.add(((DLQueryResultsSectionItem)sel).getOWLObject());
             }
         }
         return copyObjects;
     }
 
 
     public void addChangeListener(ChangeListener changeListener) {
         copyListeners.add(changeListener);
     }
 
 
     public void removeChangeListener(ChangeListener changeListener) {
         copyListeners.remove(changeListener);
     }
 }
