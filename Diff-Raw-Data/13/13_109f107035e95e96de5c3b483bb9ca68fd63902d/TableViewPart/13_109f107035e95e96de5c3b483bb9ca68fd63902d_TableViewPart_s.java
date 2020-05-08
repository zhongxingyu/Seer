 package net.ostis.confman.ui.participant;
 
 import javax.annotation.PostConstruct;
 import javax.inject.Inject;
 
 import net.ostis.confman.services.ParticipantService;
 import net.ostis.confman.services.ServiceLocator;
 import net.ostis.confman.services.common.model.Conference;
 import net.ostis.confman.services.common.model.Participant;
 import net.ostis.confman.services.common.model.ParticipantRole;
 import net.ostis.confman.services.common.model.Person;
 import net.ostis.confman.ui.common.Localizable;
 import net.ostis.confman.ui.common.component.table.DynamicalTable;
 import net.ostis.confman.ui.common.component.util.LocalizationUtil;
 import net.ostis.confman.ui.conference.ConferenceTopics;
 
 import org.eclipse.e4.core.di.annotations.Optional;
 import org.eclipse.e4.ui.di.UIEventTopic;
 import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
 import org.eclipse.jface.viewers.ArrayContentProvider;
 import org.eclipse.jface.viewers.ColumnLabelProvider;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 
 public class TableViewPart {
 
     private enum TableColumns implements Localizable {
         NAME("participantTableAuthorName"),
         CONFERENCE("participantTableConference"),
         PARTICIPATION_FORM("participantTableParticipationRole");
 
         private String rk;
 
         private TableColumns(final String rk) {
 
             this.rk = rk;
         }
 
         @Override
         public String getResourceKey() {
 
             return this.rk;
         }
     }
 
     @Inject
     private ESelectionService  selectionService;
 
     private ParticipantService participantService;
 
     private DynamicalTable     table;
 
     public TableViewPart() {
 
         super();
         this.participantService = (ParticipantService) ServiceLocator
                 .getInstance().getService(ParticipantService.class);
     }
 
     @PostConstruct
     public void createComposite(final Composite parent) {
 
         parent.setLayout(new GridLayout(1, true));
         this.table = new DynamicalTable(parent, Boolean.TRUE);
         createColumns();
         addTableEventSupport();
     }
 
     private void createColumns() {
 
         final LocalizationUtil localizationUtil = LocalizationUtil
                 .getInstance();
         final int COLUMN_WIDTH = 150;
         this.table.createColumn(localizationUtil.translate(TableColumns.NAME),
                 COLUMN_WIDTH, new ColumnLabelProvider() {
 
                     @Override
                     public String getText(final Object element) {
 
                         final Participant participant = (Participant) element;
                         final Person person = participant.getPerson();
                         return person.getFirstName() + ' '
                                 + person.getSurname();
                     }
                 });
         this.table.createColumn(
                 localizationUtil.translate(TableColumns.CONFERENCE),
                 COLUMN_WIDTH, new ColumnLabelProvider() {
 
                     @Override
                     public String getText(final Object element) {
 
                         final Participant participant = (Participant) element;
                         final Conference conference = participant
                                 .getConference();
                         return conference == null ? "" : conference.getTitle();
                     }
                 });
         this.table.createColumn(
                 localizationUtil.translate(TableColumns.PARTICIPATION_FORM),
                 COLUMN_WIDTH, new ColumnLabelProvider() {
 
                     @Override
                     public String getText(final Object element) {
 
                         final Participant participant = (Participant) element;
                         final ParticipantRole role = participant.getRole();
                         return role == null ? "" : role.getParticipationForm();
                     }
                 });
     }
 
     private void addTableEventSupport() {
 
         this.table.setContentProvider(ArrayContentProvider.getInstance());
         this.table.setInput(this.participantService.getParticipants());
         this.table.addSelectionChangedListener(new ISelectionChangedListener() {
 
             @Override
             public void selectionChanged(final SelectionChangedEvent event) {
 
                 final IStructuredSelection selection = (IStructuredSelection) TableViewPart.this.table
                         .getViewer().getSelection();
                 final Object selectedElement = selection.getFirstElement();
                 if (selectedElement instanceof Participant) {
                     TableViewPart.this.selectionService
                             .setSelection(selectedElement);
                 }
             }
         });
     }
 
     @Inject
     @Optional
     private void onConfDataUpdate(
             @UIEventTopic(ConferenceTopics.TABLE_UPDATE) final String s) {
 
        this.table.getViewer().refresh();
     }
 }
