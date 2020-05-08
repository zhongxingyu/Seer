 package net.ostis.confman.ui.conference.parts;
 
 import java.util.Date;
 import java.util.EnumMap;
 import java.util.Map;
 
 import javax.annotation.PostConstruct;
 import javax.inject.Inject;
 
 import net.ostis.confman.services.common.model.Conference;
 import net.ostis.confman.ui.common.Localizable;
 import net.ostis.confman.ui.common.component.DateDataConverter;
 import net.ostis.confman.ui.common.component.EditableComponent;
 import net.ostis.confman.ui.common.component.StringDataConverter;
 import net.ostis.confman.ui.common.component.TextField;
 import net.ostis.confman.ui.common.component.ValueBinder;
 import net.ostis.confman.ui.common.component.util.LocalizationUtil;
 import net.ostis.confman.ui.conference.ConferenceTopics;
 
 import org.eclipse.e4.core.services.events.IEventBroker;
 import org.eclipse.e4.ui.model.application.ui.basic.MPart;
 import org.eclipse.e4.ui.workbench.modeling.EPartService;
 import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
 import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
 import org.eclipse.e4.ui.workbench.modeling.ISelectionListener;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 
 public class ConferenceEditorPart {
 
     public static final String PART_ID          = "net.ostis.confman.ui.part.conference.editor";
 
     private static final int   LAYOUT_COL_COUNT = 1;
 
     private enum ConferenceFields implements Localizable {
         TITLE("conferenceTitle"),
         START_DATE("conferenceStartDate"),
        END_DATE("conferenceEndDate");
 
         private String rk;
 
         private ConferenceFields(final String rk) {
 
             this.rk = rk;
         }
 
         @Override
         public String getResourceKey() {
 
             return this.rk;
         }
     }
 
     private enum Buttons implements Localizable {
         SAVE("save");
 
         private String rk;
 
         private Buttons(final String rk) {
 
             this.rk = rk;
         }
 
         @Override
         public String getResourceKey() {
 
             return this.rk;
         }
 
     }
 
     private final Map<ConferenceFields, EditableComponent<TextField>> editFields;
 
     @Inject
     private ESelectionService                                         selectionService;
 
     @Inject
     private IEventBroker                                              eventBroker;
 
     @Inject
     private EPartService                                              partService;
 
     public ConferenceEditorPart() {
 
         super();
         this.editFields = new EnumMap<>(ConferenceFields.class);
     }
 
     @PostConstruct
     public void createComposite(final Composite parent) {
 
         this.selectionService.addSelectionListener(new ISelectionListener() {
 
             @Override
             public void selectionChanged(final MPart part,
                     final Object selection) {
 
                 if (selection instanceof Conference) {
                     final Conference conf = (Conference) selection;
                     onConferenceEvent(conf);
                 }
             }
         });
         buildLayout(parent);
     }
 
     protected void onConferenceEvent(final Conference conf) {
 
         applyValueBindings(conf);
         for (final ConferenceFields field : this.editFields.keySet()) {
             this.editFields.get(field).activate();
         }
         showConferencePart();
     }
 
     private void showConferencePart() {
 
         final MPart confPart = this.partService.findPart(PART_ID);
         this.partService.showPart(confPart, PartState.VISIBLE);
     }
 
     private void applyValueBindings(final Conference conf) {
 
         this.editFields.get(ConferenceFields.TITLE).setValueBinder(
                 new ValueBinder() {
 
                     @Override
                     public void setValue(final Object value) {
 
                         conf.setTitle((String) value);
                     }
 
                     @Override
                     public Object getValue() {
 
                         return conf.getTitle();
                     }
                 });
         this.editFields.get(ConferenceFields.START_DATE).setValueBinder(
                 new ValueBinder() {
 
                     @Override
                     public void setValue(final Object value) {
 
                         conf.setStartDate((Date) value);
                     }
 
                     @Override
                     public Object getValue() {
 
                         return conf.getStartDate();
                     }
                 });
         this.editFields.get(ConferenceFields.END_DATE).setValueBinder(
                 new ValueBinder() {
 
                     @Override
                     public void setValue(final Object value) {
 
                         conf.setEndDate((Date) value);
                     }
 
                     @Override
                     public Object getValue() {
 
                         return conf.getEndDate();
                     }
                 });
     }
 
     private void buildLayout(final Composite parent) {
 
         final LocalizationUtil util = LocalizationUtil.getInstance();
         parent.setLayout(new GridLayout(LAYOUT_COL_COUNT, true));
         this.editFields.put(ConferenceFields.TITLE,
                 new TextField(parent, util.translate(ConferenceFields.TITLE))
                         .setDataConverter(new StringDataConverter()));
         final DateDataConverter dateConverter = new DateDataConverter();
         this.editFields.put(ConferenceFields.START_DATE, new TextField(parent,
                 util.translate(ConferenceFields.START_DATE))
                 .setDataConverter(dateConverter));
         this.editFields
                 .put(ConferenceFields.END_DATE,
                         new TextField(parent, util
                                 .translate(ConferenceFields.END_DATE))
                                 .setDataConverter(dateConverter));
         final Button button = new Button(parent, SWT.PUSH);
         button.setText(util.translate(Buttons.SAVE));
         button.addSelectionListener(new SelectionListener() {
 
             @Override
             public void widgetSelected(final SelectionEvent e) {
 
                 onUpdate();
             }
 
             @Override
             public void widgetDefaultSelected(final SelectionEvent e) {
 
                 onUpdate();
             }
         });
     }
 
     private void onUpdate() {
 
         for (final ConferenceFields field : this.editFields.keySet()) {
             this.editFields.get(field).apply();
 
         }
         // TODO: add getter (?) for ValueBinder in TextField and create\
         // Conference obj with updated fiels.
         this.eventBroker.post(ConferenceTopics.CONF_UPDATE, null);
     }
 }
