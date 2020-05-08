 package net.ostis.confman.ui.common.component.conftree;
 
 import net.ostis.confman.services.ConferenceService;
 import net.ostis.confman.services.ServiceLocator;
 import net.ostis.confman.services.common.model.Conference;
 import net.ostis.confman.services.common.model.Report;
 import net.ostis.confman.services.common.model.Section;
 import net.ostis.confman.ui.common.Localizable;
 import net.ostis.confman.ui.common.component.util.LocalizationUtil;
 import net.ostis.confman.ui.conference.parts.PartId;
 import net.ostis.confman.ui.reports.SelectReportDialog;
 
 import org.eclipse.e4.ui.model.application.ui.basic.MPart;
 import org.eclipse.e4.ui.workbench.modeling.EPartService;
 import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
 import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IMenuListener;
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.util.LocalSelectionTransfer;
 import org.eclipse.jface.viewers.DoubleClickEvent;
 import org.eclipse.jface.viewers.IDoubleClickListener;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.jface.viewers.ViewerDropAdapter;
 import org.eclipse.jface.window.Window;
 import org.eclipse.swt.dnd.DragSourceEvent;
 import org.eclipse.swt.dnd.DragSourceListener;
 import org.eclipse.swt.dnd.DropTargetEvent;
 import org.eclipse.swt.dnd.TransferData;
 import org.eclipse.swt.widgets.Composite;
 
 public class ConfTreeListenerProvider {
 
     public ConfTreeListenerProvider() {
 
         super();
     }
 
     public ISelectionChangedListener getSelectionChangedListener(
             final TreeViewer treeViewer,
             final ESelectionService selectionService,
             final EPartService partService) {
 
         return new TreeSelectionChangedListener(treeViewer, selectionService,
                 partService);
     }
 
     public IDoubleClickListener getDoubleClickListener() {
 
         return new TreeDoubleClickListener();
     }
 
     public IMenuListener getIMenuListener(final TreeViewer treeViewer) {
 
         return new TreeMenuListener(treeViewer);
     }
 
     public DragSourceListener getDragSourceListener(final TreeViewer treeViewer) {
 
         return new TreeDragListener(treeViewer);
     }
 
     public ViewerDropAdapter getViewerDropAdapter(final TreeViewer treeViewer) {
 
         return new TreeDropListener(treeViewer);
     }
 
     private static class TreeSelectionChangedListener implements
             ISelectionChangedListener {
 
         private TreeViewer        treeViewer;
 
         private ESelectionService selectionService;
 
         private EPartService      partService;
 
         public TreeSelectionChangedListener(final TreeViewer treeViewer,
                 final ESelectionService selectionService,
                 final EPartService partService) {
 
             super();
             this.treeViewer = treeViewer;
             this.selectionService = selectionService;
             this.partService = partService;
         }
 
         @Override
         public void selectionChanged(final SelectionChangedEvent event) {
 
             final IStructuredSelection selection = (IStructuredSelection) this.treeViewer
                     .getSelection();
             final Object selectedElement = selection.getFirstElement();
             switchParts(selectedElement);
             this.selectionService.setSelection(selectedElement);
         }
 
         private void switchParts(final Object selectedElement) {
 
             if (selectedElement instanceof Conference) {
                 showPart(PartId.CONFERENCE_PART);
             } else if (selectedElement instanceof Section) {
                 showPart(PartId.SECTION_PART);
             } else if (selectedElement instanceof Report) {
                 showPart(PartId.REPORT_PART);
             }
         }
 
         private void showPart(final String partId) {
 
             final MPart part = this.partService.findPart(partId);
             this.partService.showPart(part, PartState.VISIBLE);
         }
     }
 
     private static class TreeDoubleClickListener implements
             IDoubleClickListener {
 
         public TreeDoubleClickListener() {
 
             super();
         }
 
         @Override
         public void doubleClick(final DoubleClickEvent event) {
 
             final TreeViewer viewer = (TreeViewer) event.getViewer();
             final IStructuredSelection thisSelection = (IStructuredSelection) event
                     .getSelection();
             final Object selectedNode = thisSelection.getFirstElement();
             viewer.setExpandedState(selectedNode,
                     !viewer.getExpandedState(selectedNode));
         }
     }
 
     private static class TreeMenuListener implements IMenuListener {
 
         private enum ConferenceFields implements Localizable {
             ADD_SECTION("ctxAddSection");
 
             private String rk;
 
             private ConferenceFields(final String rk) {
 
                 this.rk = rk;
             }
 
             @Override
             public String getResourceKey() {
 
                 return this.rk;
             }
         }
 
         private enum SectionFields implements Localizable {
             ADD_REPORT("ctxAddReport"),
             DELETE("ctxDeleteSection");
 
             private String rk;
 
             private SectionFields(final String rk) {
 
                 this.rk = rk;
             }
 
             @Override
             public String getResourceKey() {
 
                 return this.rk;
             }
         }
 
         private enum ReportFields implements Localizable {
             DELETE("ctxDeleteReport");
 
             private String rk;
 
             private ReportFields(final String rk) {
 
                 this.rk = rk;
             }
 
             @Override
             public String getResourceKey() {
 
                 return this.rk;
             }
         }
 
         private TreeViewer        treeViewer;
         
         private Composite parent;
 
         private ConferenceService conferenceService;
 
         public TreeMenuListener(final TreeViewer treeViewer) {
 
             super();
             this.treeViewer = treeViewer;
             parent = treeViewer.getControl().getParent();
             this.conferenceService = (ConferenceService) ServiceLocator
                     .getInstance().getService(ConferenceService.class);
         }
 
         @Override
         public void menuAboutToShow(final IMenuManager manager) {
 
             final IStructuredSelection selection = (IStructuredSelection) this.treeViewer
                     .getSelection();
             if (!selection.isEmpty()) {
                 final Object selectedElement = selection.getFirstElement();
                 if (selectedElement instanceof Conference) {
                     addConferenceActions(manager, (Conference) selectedElement);
                 } else if (selectedElement instanceof Section) {
                     addSectionActions(manager, (Section) selectedElement);
                 } else if (selectedElement instanceof Report) {
                     addReportActions(manager, (Report) selectedElement);
                 }
             }
         }
 
         private void addConferenceActions(final IMenuManager manager,
                 final Conference selectedElement) {
 
             final String actionText = getLocalizedValue(ConferenceFields.ADD_SECTION);
             final Action addSectionAction = new Action(actionText) {
 
                 @Override
                 public void run() {
 
                    // TODO kfs: implement add section method.
                 }
             };
             manager.add(addSectionAction);
         }
 
         private void addSectionActions(final IMenuManager manager,
                 final Section selectedElement) {
 
             final String addReportActionText = getLocalizedValue(SectionFields.ADD_REPORT);
             final Action addReportAction = new Action(addReportActionText) {
 
                 @Override
                 public void run() {
 
                     showReportDialog(parent, selectedElement);
                 }
             };
             final String deleteSectionActionText = getLocalizedValue(SectionFields.DELETE);
             final Action deleteSectionAction = new Action(
                     deleteSectionActionText) {
 
                 @Override
                 public void run() {
 
                     TreeMenuListener.this.conferenceService
                             .deleteSection(selectedElement);
                 }
             };
             manager.add(addReportAction);
             manager.add(deleteSectionAction);
         }
         
         private void showReportDialog(final Composite parent, Section section) {
 
             final SelectReportDialog dialog = new SelectReportDialog(
                     parent.getShell());
             dialog.create();
             if (dialog.open() == Window.OK) {
                 final Report selectedReport = dialog.getSelectedReport();
                 if (selectedReport != null) {
                     this.conferenceService.addReport(section, selectedReport);
                 }
             }
         }
 
         private void addReportActions(final IMenuManager manager,
                 final Report selectedElement) {
 
             final String deleteReportActionText = getLocalizedValue(ReportFields.DELETE);
             final Action deleteReportAction = new Action(deleteReportActionText) {
 
                 @Override
                 public void run() {
 
                     TreeMenuListener.this.conferenceService
                             .deleteReport(selectedElement);
                 }
             };
             manager.add(deleteReportAction);
         }
 
         private String getLocalizedValue(final Localizable toTranslate) {
 
             final LocalizationUtil localizationUtil = LocalizationUtil
                     .getInstance();
             return localizationUtil.translate(toTranslate);
         }
     }
 
     private static class TreeDragListener implements DragSourceListener {
 
         private final TreeViewer treeViewer;
 
         public TreeDragListener(final TreeViewer treeViewer) {
 
             this.treeViewer = treeViewer;
         }
 
         @Override
         public void dragSetData(final DragSourceEvent event) {
 
             final IStructuredSelection selection = (IStructuredSelection) this.treeViewer
                     .getSelection();
             if (!(selection.getFirstElement() instanceof Report)) {
                 return;
             }
             final LocalSelectionTransfer localSelectionTransfer = LocalSelectionTransfer
                     .getTransfer();
             localSelectionTransfer.setSelection(selection);
         }
 
         @Override
         public void dragStart(final DragSourceEvent event) {
 
             // do nothing
         }
 
         @Override
         public void dragFinished(final DragSourceEvent event) {
 
             // do nothing
         }
     }
 
     public static class TreeDropListener extends ViewerDropAdapter {
 
         public TreeDropListener(final TreeViewer treeViewer) {
 
             super(treeViewer);
         }
 
         @Override
         public void drop(final DropTargetEvent event) {
 
             if (event.item == null) {
                 return;
             }
             final Object dropTarget = event.item.getData();
             final Report report = getReport();
             processEvent(dropTarget, report);
         }
 
         private Report getReport() {
 
             final LocalSelectionTransfer localSelectionTransfer = LocalSelectionTransfer
                     .getTransfer();
             final IStructuredSelection selection = (IStructuredSelection) localSelectionTransfer
                     .getSelection();
             final Report report = (Report) selection.getFirstElement();
             return report;
         }
 
         private void processEvent(final Object dropTarget, final Report report) {
 
             final ConferenceService conferenceService = (ConferenceService) ServiceLocator
                     .getInstance().getService(ConferenceService.class);
             if (dropTarget instanceof Report) {
                 final Report targetReport = (Report) dropTarget;
                 if (targetReport.getSection() != report.getSection()) {
                     conferenceService.moveReport(report, report.getSection(),
                             targetReport.getSection());
                 }
             }
             if (dropTarget instanceof Section) {
                 conferenceService.moveReport(report, report.getSection(),
                         (Section) dropTarget);
             }
         }
 
         @Override
         public boolean performDrop(final Object data) {
 
             // do nothing
             return true;
         }
 
         @Override
         public boolean validateDrop(final Object target, final int operation,
                 final TransferData transferType) {
 
             // do nothing
             return true;
         }
     }
 }
