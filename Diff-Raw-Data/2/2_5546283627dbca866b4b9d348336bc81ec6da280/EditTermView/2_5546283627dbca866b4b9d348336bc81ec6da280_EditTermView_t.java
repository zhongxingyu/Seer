 package arithmea.client.view;
 
 import java.util.HashMap;
 import java.util.Map;
 import arithmea.client.presenter.EditTermPresenter;
 import arithmea.client.widgets.ExtendedTextBox;
 import arithmea.client.widgets.LetterStarWidget;
 import arithmea.client.widgets.tree.HebrewTreeWidget;
 import arithmea.client.widgets.tree.LatinTreeWidget;
 import arithmea.shared.gematria.GematriaMethod;
 import arithmea.shared.gematria.HebrewMethod;
 import arithmea.shared.gematria.LatinMethod;
 import arithmea.shared.qabalah.SephirothData;
 import com.google.gwt.event.dom.client.HasClickHandlers;
 import com.google.gwt.user.client.ui.Anchor;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.DecoratorPanel;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.user.client.ui.HasValue;
 import com.google.gwt.user.client.ui.HasVerticalAlignment;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.Panel;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 /**
  * View to edit and add new terms.
  */
 public class EditTermView extends Composite implements EditTermPresenter.Display {
     private static final String METHOD = "Method";
     private static final String VALUE = "Value";
     private static final String MATCHES = "Matches";
     private final HorizontalPanel treePanel = new HorizontalPanel();
     private final HebrewTreeWidget hebrewTree = new HebrewTreeWidget(SephirothData.WIDTH, SephirothData.HEIGHT);
     private final LatinTreeWidget latinTree = new LatinTreeWidget(SephirothData.WIDTH, SephirothData.HEIGHT);
     private final LetterStarWidget letterStar = new LetterStarWidget(300, 300);
 
     private final ExtendedTextBox inputTextBox;
     private final Panel busyPanel = new HorizontalPanel();
     private final Image busyImage = new Image("/images/busy.gif");
     private final Label latinLabel;
     private final Label hebrewLabel;
     private final Map<GematriaMethod, Label> methodLabels = new HashMap<GematriaMethod, Label>();
     private final Map<GematriaMethod, Anchor> anchors = new HashMap<GematriaMethod, Anchor>();
     private final Map<GematriaMethod, FlowPanel> matchPanels = new HashMap<GematriaMethod, FlowPanel>();
     private final Map<GematriaMethod, TextBox> valueBoxes = new HashMap<GematriaMethod, TextBox>();
     private final FlexTable detailsTable;
     private final Button saveButton;
     private final Button cancelButton;
 
     /**
      * Default constructor
      * @param eventBus
      * @param word
      */
     public EditTermView(final String word) {
         final DecoratorPanel contentDetailsDecorator = new DecoratorPanel();
         contentDetailsDecorator.setWidth("800px");
         initWidget(contentDetailsDecorator);
 
         final VerticalPanel contentDetailsPanel = new VerticalPanel();
 
         final FlexTable menuTable = new FlexTable();
         menuTable.setWidth("778px");
         final HorizontalPanel hPanel = new HorizontalPanel();
         hPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
         hPanel.setWidth("778px");
 
         final FlowPanel buttonFlow = new FlowPanel();
         cancelButton = new Button("Cancel Input");
         buttonFlow.add(cancelButton);
         saveButton = new Button("Save Word");
         buttonFlow.add(saveButton);
 
         hPanel.add(buttonFlow);
         inputTextBox = new ExtendedTextBox();
        inputTextBox.setWidth("570px");
         inputTextBox.setMaxLength(30);
         hPanel.add(inputTextBox);
         hPanel.add(busyPanel);
 
         menuTable.getCellFormatter().addStyleName(0, 0, "menu-table");
         menuTable.setWidget(0, 0, hPanel);
         contentDetailsPanel.add(menuTable);
 
         detailsTable = new FlexTable();
         detailsTable.setWidth("790px");
         detailsTable.getColumnFormatter().setWidth(0, "89px");
         detailsTable.getColumnFormatter().setWidth(1, "55px");
         detailsTable.getColumnFormatter().setWidth(2, "641px");
         
         latinLabel = new Label();
         latinLabel.setWidth("280px");
         latinLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
         latinLabel.setStyleName("latin-label");
         hebrewLabel = new Label();
         hebrewLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
         hebrewLabel.setStyleName("hebrew-label");
         for (final LatinMethod method : LatinMethod.values()) {
             prepareRow(method);
         }
         for (final HebrewMethod method : HebrewMethod.values()) {
             prepareRow(method);
         }
         initDetailsTable();
         contentDetailsPanel.add(detailsTable);
         treePanel.add(hebrewTree);
         treePanel.add(latinTree);
         treePanel.add(letterStar);
         contentDetailsPanel.add(treePanel);
         
         if (word != null && !word.equals("")) {
             inputTextBox.setText(word);
         }
 
         contentDetailsPanel.setHeight("671px");
         contentDetailsDecorator.add(contentDetailsPanel);
     }
 
     /**
      * Prepares a row for the provided GematriaMethod.
      * @param method
      */
     private void prepareRow(final GematriaMethod method) {
         final Anchor anchor = new Anchor();
         final FlowPanel matches = new FlowPanel();
         final TextBox valueBox = new TextBox();
         methodLabels.put(method, new Label(method.name()));
         anchors.put(method, anchor);
         matchPanels.put(method, matches);
         valueBoxes.put(method, valueBox);
     }
 
     /**
      * Initializes the details table.
      */
     private void initDetailsTable() {
         int row = 0;
         addLabel(row++, latinLabel);
         addHeading(detailsTable, row++);
         for (LatinMethod gm : LatinMethod.values()) {
             addRow(detailsTable, row++, gm);
         }
         addLabel(row++, hebrewLabel);
         addHeading(detailsTable, row++);
         for (HebrewMethod method : HebrewMethod.values()) {
             addRow(detailsTable, row++, method);
         }
     }
 
     /**
      * Adds the big Label to the table.
      */
     private void addLabel(int row, final Label label) {
         detailsTable.setWidget(row, 0, label);
         detailsTable.getFlexCellFormatter().setColSpan(row, 0, 3);
         detailsTable.getCellFormatter().addStyleName(row, 1, "text-table");
     }
     
     /**
      * Adds a row to the table.
      * @param table
      * @param row
      * @param description
      * @param anchor
      */
     private void addRow(final FlexTable table, final int row, final GematriaMethod method) {
         table.setWidget(row, 0, methodLabels.get(method));
         table.setWidget(row, 1, anchors.get(method));
         table.setWidget(row, 2, matchPanels.get(method));
         table.getCellFormatter().setAlignment(row, 1,
                 HasHorizontalAlignment.ALIGN_RIGHT,
                 HasVerticalAlignment.ALIGN_MIDDLE);
         addCellFormats(table, row, false);
     }
 
     /**
      * Adds headers to the table.
      * @param table
      * @param row
      */
     private void addHeading(final FlexTable table, final int row) {
         table.setText(row, 0, METHOD);
         table.setText(row, 1, VALUE);
         table.setText(row, 2, MATCHES);
         addCellFormats(table, row, true);
     }
     
     /**
      * Formats all cells in the provided row.
      * @param table
      * @param row
      */
     private void addCellFormats(final FlexTable table, final int row, final boolean isHeader) {
         for (int col = 0; col < table.getCellCount(row); col++) {
             if (isHeader) {
                 table.getCellFormatter().addStyleName(row, col, "edit-border-cell-header");                
             } else {
                 table.getCellFormatter().addStyleName(row, col, "edit-border-cell");
             }
         }
     }
     
     @Override
     public final HasValue<String> getInputText() {
         return inputTextBox;
     }
 
     @Override
     public final TextBox getInputTextBox() {
         return inputTextBox;
     }
     
     @Override
     public Panel getBusyPanel() {
         return busyPanel;
     }
 
     @Override
     public Image getBusyImage() {
         return busyImage;
     }
 
     @Override
     public final FlowPanel getMatchPanel(final GematriaMethod method) {
         return matchPanels.get(method);
     }
 
     @Override
     public final Label getHebrewLabel() {
         return hebrewLabel;
     }
 
     @Override
     public final Label getLatinLabel() {
         return latinLabel;
     }
 
     @Override
     public final LetterStarWidget getLetterStar() {
         return letterStar;
     }
 
     @Override
     public final HasClickHandlers getSaveButton() {
         return saveButton;
     }
 
     @Override
     public final HasClickHandlers getCancelButton() {
         return cancelButton;
     }
 
     @Override
     public final Widget asWidget() {
         return this;
     }
 
     @Override
     public final HasValue<String> get(final GematriaMethod gm) {
         return valueBoxes.get(gm);
     }
 
     @Override
     public Label getMethodLabel(GematriaMethod gm) {
         return methodLabels.get(gm);
     }
 
     @Override
     public Anchor getAnchor(GematriaMethod gm) {
         return anchors.get(gm);
     }
 
     @Override
     public final Map<GematriaMethod, TextBox> getValueBoxes() {
         return valueBoxes;
     }
 
     @Override
     public final HebrewTreeWidget getHebrewTree() {
         return hebrewTree;
     }
 
     @Override
     public final LatinTreeWidget getLatinTree() {
         return latinTree;
     }
 
 }
