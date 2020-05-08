 package net.ostis.confman.ui.mail;
 
 import javax.annotation.PostConstruct;
 import javax.inject.Inject;
 
 import net.ostis.confman.services.common.model.Participant;
 import net.ostis.confman.services.common.model.Person;
 import net.ostis.confman.ui.common.Localizable;
 import net.ostis.confman.ui.common.component.table.DynamicalTable;
 import net.ostis.confman.ui.common.component.util.LocalizationUtil;
 
 import org.eclipse.e4.ui.model.application.ui.basic.MPart;
 import org.eclipse.e4.ui.workbench.modeling.EPartService;
 import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
 import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
 import org.eclipse.e4.ui.workbench.modeling.ISelectionListener;
 import org.eclipse.jface.viewers.ArrayContentProvider;
 import org.eclipse.jface.viewers.ColumnLabelProvider;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Text;
 
 public class TemplatesEditorPart {
 
     private static final int LAYOUT_COL_COUNT = 1;
 
     private enum Captions implements Localizable {
         NAME("participantTableAuthorName"),
        EMAIL("participantTableEmail"),
         SECTION("participantTableSection"),
         PREVIOUS_STEP("previousStep"),
         NEXT_STEP("nextStep");
 
         private String rk;
 
         private Captions(final String rk) {
 
             this.rk = rk;
         }
 
         @Override
         public String getResourceKey() {
 
             return this.rk;
         }
     }
 
     protected final static String PARTICIPANTS_CHOOSE_PART_ID = "net.ostis.confman.ui.part.email.participantsPart";
 
     @Inject
     private EPartService          partService;
 
     @Inject
     private ESelectionService     selectionService;
 
     private Text                  textArea;
 
     private DynamicalTable        table;
 
     private EmailedParticipants   participants;
 
     public TemplatesEditorPart() {
 
         super();
         this.participants = new EmailedParticipants();
     }
 
     @PostConstruct
     public void createComposite(final Composite parent) {
 
         this.selectionService.addSelectionListener(new ISelectionListener() {
 
             @Override
             public void selectionChanged(final MPart part,
                     final Object selection) {
 
                 if (!(selection instanceof EmailedParticipants)) {
                     return;
                 }
                 TemplatesEditorPart.this.participants = (EmailedParticipants) selection;
                 addTableEventSupport();
             }
         });
         buildLayout(parent);
     }
 
     private void onNewSelection(final EmailedParticipants participants) {
 
         this.textArea.setText(participants.getTemplate().getName());
     }
 
     private void buildLayout(final Composite parent) {
 
         parent.setLayout(new FillLayout());
         this.table = new DynamicalTable(parent, Boolean.TRUE, SWT.SINGLE);
         createColumns();
         final Composite composite = createTextWrapper(parent);
         createTextArea(composite);
         createNextStepButton(composite);
         createPreviousStepButton(composite);
     }
 
     private Composite createTextWrapper(final Composite parent) {
 
         final Composite composite = new Composite(parent, SWT.NONE);
         composite.setLayout(new GridLayout(LAYOUT_COL_COUNT, Boolean.FALSE));
         final GridData gridData = new GridData(SWT.FILL, SWT.FILL,
                 Boolean.TRUE, Boolean.TRUE);
         composite.setLayoutData(gridData);
         return composite;
     }
 
     private void createTextArea(final Composite parent) {
 
         final Composite composite = new Composite(parent, SWT.NONE);
         composite.setLayout(new FillLayout());
         final GridData gridData = new GridData(SWT.FILL, SWT.FILL,
                 Boolean.TRUE, Boolean.TRUE);
         this.textArea = new Text(composite, SWT.MULTI | SWT.BORDER
                 | SWT.V_SCROLL | SWT.H_SCROLL);
         composite.setLayoutData(gridData);
     }
 
     private void createColumns() {
 
         final LocalizationUtil localizationUtil = LocalizationUtil
                 .getInstance();
         final int COLUMN_WIDTH = 200;
         this.table.createColumn(localizationUtil.translate(Captions.NAME),
                 COLUMN_WIDTH, new ColumnLabelProvider() {
 
                     @Override
                     public String getText(final Object element) {
 
                         final Participant participant = (Participant) element;
                         final Person person = participant.getPerson();
                         return person.getFirstName() + ' '
                                 + person.getSurname();
                     }
                 });
        this.table.createColumn(localizationUtil.translate(Captions.EMAIL),
                COLUMN_WIDTH, new ColumnLabelProvider() {
 
                     @Override
                     public String getText(final Object element) {
 
                         final Participant participant = (Participant) element;
                        final String email = participant.getPerson()
                                .getContacts().geteMail();
                        return email == null ? "" : email;
                     }
                 });
     }
 
     private void addTableEventSupport() {
 
         this.table.setContentProvider(ArrayContentProvider.getInstance());
         this.table.setInput(this.participants.getParticipants());
     }
 
     private void createNextStepButton(final Composite parent) {
 
         final LocalizationUtil util = LocalizationUtil.getInstance();
         final Button nextButton = new Button(parent, SWT.NONE);
         nextButton.setText(util.translate(Captions.NEXT_STEP));
         final GridData gridData = new GridData(SWT.RIGHT, SWT.BOTTOM,
                 Boolean.FALSE, Boolean.FALSE);
         nextButton.setLayoutData(gridData);
         nextButton.addSelectionListener(new SelectionListener() {
 
             @Override
             public void widgetSelected(final SelectionEvent e) {
 
                 onSelection();
 
             }
 
             @Override
             public void widgetDefaultSelected(final SelectionEvent e) {
 
                 onSelection();
 
             }
 
             private void onSelection() {
 
                 final IStructuredSelection selection = (IStructuredSelection) TemplatesEditorPart.this.table
                         .getViewer().getSelection();
                 final Object selectedElement = selection.getFirstElement();
                 if (selectedElement instanceof EmailedParticipants) {
                     final EmailedParticipants emailedParticipants = (EmailedParticipants) selectedElement;
                     onNewSelection(emailedParticipants);
 
                 } else {
 
                     // TODO vadim-mihalovski: add warning dialog: empty
                     // selection
                 }
             }
         });
     }
 
     private void createPreviousStepButton(final Composite composite) {
 
         final LocalizationUtil util = LocalizationUtil.getInstance();
         final Button previousButton = new Button(composite, SWT.NONE);
         previousButton.setText(util.translate(Captions.PREVIOUS_STEP));
         final GridData gridData = new GridData(SWT.RIGHT, SWT.BOTTOM,
                 Boolean.FALSE, Boolean.FALSE);
         previousButton.setLayoutData(gridData);
         previousButton.addSelectionListener(new SelectionListener() {
 
             @Override
             public void widgetSelected(final SelectionEvent e) {
 
                 previousStep();
 
             }
 
             @Override
             public void widgetDefaultSelected(final SelectionEvent e) {
 
                 previousStep();
 
             }
 
             private void previousStep() {
 
                 final MPart part = TemplatesEditorPart.this.partService
                         .findPart(PARTICIPANTS_CHOOSE_PART_ID);
                 TemplatesEditorPart.this.partService.showPart(part,
                         PartState.VISIBLE);
             }
         });
     }
 
 }
