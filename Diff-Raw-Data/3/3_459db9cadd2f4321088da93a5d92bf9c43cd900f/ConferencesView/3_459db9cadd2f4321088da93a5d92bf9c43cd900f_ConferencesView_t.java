 package net.ostis.confman.ui.conference;
 
 import javax.annotation.PostConstruct;
 import javax.inject.Inject;
 
import net.ostis.confman.model.convert.ConverterFromStorageProvider;
 import net.ostis.confman.services.ConferenceService;
 import net.ostis.confman.services.ServiceLocator;
import net.ostis.confman.services.common.model.FullModel;
 import net.ostis.confman.ui.common.component.conftree.ConfTreeContentProvider;
 import net.ostis.confman.ui.common.component.conftree.ConfTreeLabelProvider;
 import net.ostis.confman.ui.common.component.conftree.ConfTreeListenerProvider;
 
 import org.eclipse.e4.core.di.annotations.Optional;
 import org.eclipse.e4.ui.di.Focus;
 import org.eclipse.e4.ui.di.UIEventTopic;
 import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.dnd.DND;
 import org.eclipse.swt.dnd.TextTransfer;
 import org.eclipse.swt.dnd.Transfer;
 import org.eclipse.swt.widgets.Composite;
 
 public class ConferencesView {
 
     private static final int  CONF_LEVEL_EXPAND = 2;
 
     private ConferenceService confService;
 
     @Inject
     private ESelectionService selectionService;
 
     private TreeViewer        treeViewer;
 
     public ConferencesView() {
 
         super();
         this.confService = (ConferenceService) ServiceLocator.getInstance()
                 .getService(ConferenceService.class);
     }
 
     @PostConstruct
     public void createComposite(final Composite parent) {
 
         initTreeViewer(parent);
         addEventSupport();
     }
 
     private void initTreeViewer(final Composite parent) {
 
         this.treeViewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL
                 | SWT.V_SCROLL);
         this.treeViewer.setContentProvider(new ConfTreeContentProvider());
         this.treeViewer.setLabelProvider(new ConfTreeLabelProvider());
         this.treeViewer.setAutoExpandLevel(CONF_LEVEL_EXPAND);
         this.treeViewer.setInput(this.confService.getConferences());
     }
 
     private void addEventSupport() {
 
         final ConfTreeListenerProvider listenerProvider = new ConfTreeListenerProvider();
         addDragAndDropSupport(listenerProvider);
         this.treeViewer.addSelectionChangedListener(listenerProvider
                 .getSelectionChangedListener());
         this.treeViewer.addDoubleClickListener(listenerProvider
                 .getDoubleClickListener());
         initContextMenu(listenerProvider);
     }
 
     private void addDragAndDropSupport(
             final ConfTreeListenerProvider listenerProvider) {
 
         final int operations = DND.DROP_COPY | DND.DROP_MOVE;
         final Transfer[] transferTypes = new Transfer[] { TextTransfer
                 .getInstance() };
         this.treeViewer.addDragSupport(operations, transferTypes,
                 listenerProvider.getDragSourceListener(this.treeViewer));
         this.treeViewer.addDropSupport(operations, transferTypes,
                 listenerProvider.getViewerDropAdapter(this.treeViewer));
     }
 
     private void initContextMenu(final ConfTreeListenerProvider listenerProvider) {
 
         final MenuManager manager = new MenuManager();
         manager.setRemoveAllWhenShown(true);
         manager.addMenuListener(listenerProvider
                 .getIMenuListener(this.treeViewer));
         this.treeViewer.getControl().setMenu(
                 manager.createContextMenu(this.treeViewer.getControl()));
     }
 
     @Focus
     public void setFocus() {
 
         this.treeViewer.getControl().setFocus();
     }
 
     private void fillConferenceList() {
 
         // for (final ConferenceDto conf : this.conferences) {
         // this.confUiList.add(conf.getTitle());
         // /}
     }
 
     @Inject
     @Optional
     private void onConfDataUpdate(
             @UIEventTopic(ConferenceTopics.CONF_SAVE) final String s) {
 
         // this.confUiList.removeAll();
         fillConferenceList();
     }
 
 }
