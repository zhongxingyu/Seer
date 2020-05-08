 package org.coode.dlquery;
 
 import java.awt.Frame;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.*;
 import javax.swing.JComponent;
 import javax.swing.SwingUtilities;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import org.protege.editor.core.ui.list.MList;
 import org.protege.editor.core.ui.list.MListButton;
 import org.protege.editor.owl.OWLEditorKit;
 import org.protege.editor.owl.ui.OWLClassExpressionComparator;
 import org.protege.editor.owl.ui.explanation.ExplanationManager;
 import org.protege.editor.owl.ui.framelist.ExplainButton;
 import org.protege.editor.owl.ui.renderer.LinkedObjectComponent;
 import org.protege.editor.owl.ui.renderer.LinkedObjectComponentMediator;
 import org.protege.editor.owl.ui.view.Copyable;
 import org.semanticweb.owlapi.model.OWLAxiom;
 import org.semanticweb.owlapi.model.OWLClass;
 import org.semanticweb.owlapi.model.OWLClassExpression;
 import org.semanticweb.owlapi.model.OWLDataFactory;
 import org.semanticweb.owlapi.model.OWLIndividual;
 import org.semanticweb.owlapi.model.OWLNamedIndividual;
 import org.semanticweb.owlapi.model.OWLObject;
 import org.semanticweb.owlapi.reasoner.Node;
 import org.semanticweb.owlapi.reasoner.OWLReasoner;
 
 import static org.coode.dlquery.ResultsSection.*;
 
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
 
    private final OWLEditorKit owlEditorKit;
 
    private final Set<ResultsSection> visibleResultsSections = EnumSet.of(SUB_CLASSES);
 
    private final LinkedObjectComponentMediator mediator;
 
    private final List<ChangeListener> copyListeners = new ArrayList<>();
 
     public ResultsList(OWLEditorKit owlEditorKit) {
         this.owlEditorKit = owlEditorKit;
         setCellRenderer(new DLQueryListCellRenderer(owlEditorKit));
         mediator = new LinkedObjectComponentMediator(owlEditorKit, this);
         getSelectionModel().addListSelectionListener(new ListSelectionListener() {
             public void valueChanged(ListSelectionEvent event) {
                if (!event.getValueIsAdjusting()) {
                    ChangeEvent ev = new ChangeEvent(ResultsList.this);
                    for (ChangeListener l : new ArrayList<>(copyListeners)) {
                        l.stateChanged(ev);
                    }
                 }
             }
         });
     }
 
     public boolean isResultsSectionVisible(ResultsSection section) {
         return visibleResultsSections.contains(section);
     }
 
     public void setResultsSectionVisible(ResultsSection section, boolean b) {
         if (b) {
             visibleResultsSections.add(section);
         } else {
             visibleResultsSections.remove(section);
         }
     }
 
     @Deprecated
     public boolean isShowAncestorClasses() {
         return visibleResultsSections.contains(SUPER_CLASSES);
     }
 
     @Deprecated
     public void setShowAncestorClasses(boolean showAncestorClasses) {
         setResultsSectionVisible(SUPER_CLASSES, showAncestorClasses);
     }
 
     @Deprecated
     public boolean isShowDescendantClasses() {
         return isResultsSectionVisible(SUB_CLASSES);
     }
 
     @Deprecated
     public void setShowDescendantClasses(boolean showDescendantClasses) {
         setResultsSectionVisible(SUB_CLASSES, showDescendantClasses);
     }
 
     @Deprecated
     public boolean isShowInstances() {
         return isResultsSectionVisible(INSTANCES);
     }
 
     @Deprecated
     public void setShowInstances(boolean showInstances) {
         setResultsSectionVisible(INSTANCES, showInstances);
     }
 
     @Deprecated
     public boolean isShowSubClasses() {
         return isResultsSectionVisible(DIRECT_SUB_CLASSES);
     }
 
     @Deprecated
     public void setShowSubClasses(boolean showSubClasses) {
         setResultsSectionVisible(DIRECT_SUB_CLASSES, showSubClasses);
     }
 
     @Deprecated
     public boolean isShowSuperClasses() {
         return isResultsSectionVisible(DIRECT_SUPER_CLASSES);
     }
 
     @Deprecated
     public void setShowSuperClasses(boolean showSuperClasses) {
         setResultsSectionVisible(DIRECT_SUPER_CLASSES, showSuperClasses);
     }
 
     @Deprecated
     public boolean isShowEquivalentClasses() {
         return isResultsSectionVisible(EQUIVALENT_CLASSES);
     }
 
     @Deprecated
     public void setShowEquivalentClasses(boolean showEquivalentClasses) {
         setResultsSectionVisible(EQUIVALENT_CLASSES, showEquivalentClasses);
     }
 
     private List<OWLClass> toSortedList(Set<OWLClass> clses) {
         OWLClassExpressionComparator descriptionComparator = new OWLClassExpressionComparator(owlEditorKit.getModelManager());
         List<OWLClass> list = new ArrayList<OWLClass>(clses);
         Collections.sort(list, descriptionComparator);
         return list;
     }
 
     public void setOWLClassExpression(OWLClassExpression description) {
         List<Object> data = new ArrayList<Object>();
         OWLDataFactory factory = owlEditorKit.getOWLModelManager().getOWLDataFactory();
         OWLReasoner reasoner = owlEditorKit.getModelManager().getReasoner();
         if (isResultsSectionVisible(EQUIVALENT_CLASSES)) {
             final List<OWLClass> results = toSortedList(reasoner.getEquivalentClasses(description).getEntities());
             data.add(new DLQueryResultsSection(EQUIVALENT_CLASSES.getDisplayName() + " (" + results.size() + ")"));
             for (OWLClass cls : results) {
                 data.add(new DLQueryResultsSectionItem(cls, factory.getOWLEquivalentClassesAxiom(description, cls)));
             }
         }
         if (isResultsSectionVisible(SUPER_CLASSES)) {
             final List<OWLClass> results = toSortedList(reasoner.getSuperClasses(description, false).getFlattened());
             data.add(new DLQueryResultsSection(SUPER_CLASSES.getDisplayName() + " (" + results.size() + ")"));
             for (OWLClass superClass : results) {
                 data.add(new DLQueryResultsSectionItem(superClass, factory.getOWLSubClassOfAxiom(description, superClass)));
             }
         }
         if (isResultsSectionVisible(DIRECT_SUPER_CLASSES)) {
             final List<OWLClass> results = toSortedList(reasoner.getSuperClasses(description, true).getFlattened());
             data.add(new DLQueryResultsSection(DIRECT_SUPER_CLASSES.getDisplayName() + " (" + results.size() + ")"));
             for (OWLClass superClass : results) {
                 data.add(new DLQueryResultsSectionItem(superClass, factory.getOWLSubClassOfAxiom(description, superClass)));
             }
         }
         if (isResultsSectionVisible(DIRECT_SUB_CLASSES)) {
             final Set<OWLClass> resultSet = new HashSet<OWLClass>();
             for (Node<OWLClass> clsSet : reasoner.getSubClasses(description, true)) {
                 resultSet.addAll(clsSet.getEntities());
             }
             final List<OWLClass> results = toSortedList(resultSet);
             data.add(new DLQueryResultsSection(DIRECT_SUB_CLASSES.getDisplayName() + " (" + results.size() + ")"));
             for (OWLClass subClass : results) {
                 data.add(new DLQueryResultsSectionItem(subClass, factory.getOWLSubClassOfAxiom(subClass, description)));
             }
         }
         if (isResultsSectionVisible(SUB_CLASSES)) {
             final Set<OWLClass> resultSet = new HashSet<OWLClass>();
             for (Node<OWLClass> clsSet : reasoner.getSubClasses(description, false)) {
                 resultSet.addAll(clsSet.getEntities());
             }
             final List<OWLClass> results = toSortedList(resultSet);
             data.add(new DLQueryResultsSection(SUB_CLASSES.getDisplayName() + " (" + results.size() + ")"));
             for (OWLClass cls : results) {
                 data.add(new DLQueryResultsSectionItem(cls, factory.getOWLSubClassOfAxiom(cls, description)));
             }
         }
         if (isResultsSectionVisible(INSTANCES)) {
             final Set<OWLNamedIndividual> results = reasoner.getInstances(description, false).getFlattened();
             data.add(new DLQueryResultsSection(INSTANCES.getDisplayName() + " (" + results.size() + ")"));
             for (OWLIndividual ind : results) {
                 data.add(new DLQueryResultsSectionItem(ind, factory.getOWLClassAssertionAxiom(description, ind)));
             }
         }
         setListData(data.toArray());
     }
 
     protected List<MListButton> getButtons(Object value) {
         if (value instanceof DLQueryResultsSectionItem) {
             final OWLAxiom axiom = ((DLQueryResultsSectionItem) value).getAxiom();
             List<MListButton> buttons = new ArrayList<MListButton>();
             buttons.add(new ExplainButton(new ActionListener() {
                 public void actionPerformed(ActionEvent e) {
                     ExplanationManager em = owlEditorKit.getOWLModelManager().getExplanationManager();
                     em.handleExplain((Frame) SwingUtilities.getAncestorOfClass(Frame.class, ResultsList.this), axiom);
                 }
             }));
             return buttons;
         } else {
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
         for (Object sel : getSelectedValues()) {
             if (sel instanceof DLQueryResultsSectionItem) {
                 copyObjects.add(((DLQueryResultsSectionItem) sel).getOWLObject());
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
