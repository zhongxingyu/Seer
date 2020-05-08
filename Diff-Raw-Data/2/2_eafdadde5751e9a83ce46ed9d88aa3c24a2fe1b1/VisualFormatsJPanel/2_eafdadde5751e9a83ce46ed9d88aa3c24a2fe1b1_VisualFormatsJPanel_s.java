 package cz.muni.fi.fresneleditor.gui.mod.format2;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import javax.swing.BorderFactory;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.ButtonGroup;
 import javax.swing.ComboBoxModel;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.GroupLayout;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.LayoutStyle;
 import javax.swing.ListModel;
 import javax.swing.ListSelectionModel;
 import javax.swing.SwingUtilities;
 import javax.swing.border.LineBorder;
 import javax.swing.border.TitledBorder;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import org.jdesktop.application.Application;
 import org.openrdf.model.URI;
 import org.openrdf.model.Value;
 import org.openrdf.query.QueryLanguage;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.jgoodies.forms.layout.CellConstraints;
 import com.jgoodies.forms.layout.FormLayout;
 
 import cz.muni.fi.fresneleditor.common.AppEventsManager;
 import cz.muni.fi.fresneleditor.common.ContextHolder;
 import cz.muni.fi.fresneleditor.common.FresnelEditorConstants;
 import cz.muni.fi.fresneleditor.common.ITabComponent;
 import cz.muni.fi.fresneleditor.common.data.AdditionalContentGuiWrapper;
 import cz.muni.fi.fresneleditor.common.data.AdditionalContentPositionType;
 import cz.muni.fi.fresneleditor.common.data.AdditionalContentType;
 import cz.muni.fi.fresneleditor.common.data.StyleGuiWrapper;
 import cz.muni.fi.fresneleditor.common.data.StyleType;
 import cz.muni.fi.fresneleditor.common.guisupport.IContextMenu;
 import cz.muni.fi.fresneleditor.common.guisupport.IEditable;
 import cz.muni.fi.fresneleditor.common.guisupport.MessageDialog;
 import cz.muni.fi.fresneleditor.common.guisupport.dialogs.ElementDetailDialog;
 import cz.muni.fi.fresneleditor.common.guisupport.dialogs.PreviewDialog;
 import cz.muni.fi.fresneleditor.common.utils.FresnelUtils;
 import cz.muni.fi.fresneleditor.common.utils.GuiUtils;
 import cz.muni.fi.fresneleditor.gui.mod.format2.components.ContentSelectorJPanel;
 import cz.muni.fi.fresneleditor.gui.mod.format2.components.FormatDomainTableModel;
 import cz.muni.fi.fresneleditor.gui.mod.format2.data.DomainSelectorGuiWrapper;
 import cz.muni.fi.fresneleditor.gui.mod.format2.data.FormatModel;
 import cz.muni.fi.fresneleditor.gui.mod.format2.data.ValueDisplayFormat;
 import cz.muni.fi.fresneleditor.gui.mod.format2.data.enums.LabelType;
 import cz.muni.fi.fresneleditor.gui.mod.format2.dialogs.DomainSelectorDialog;
 import cz.muni.fi.fresneleditor.gui.mod.format2.dialogs.FormatPreviewDialog;
 import cz.muni.fi.fresneleditor.gui.mod.format2.treemodel.FormatItemNode;
 import cz.muni.fi.fresneleditor.gui.mod.format2.utils.FormatModelManager;
 import cz.muni.fi.fresneleditor.gui.mod.format2.utils.TextFileLoader;
 import cz.muni.fi.fresneleditor.model.FresnelRepositoryDao;
 import cz.muni.fi.fresneleditor.model.SparqlUtils;
 import fr.inria.jfresnel.Format;
 import fr.inria.jfresnel.formats.FormatPurposeType;
 import fr.inria.jfresnel.formats.FormatValueType;
 import fr.inria.jfresnel.sesame.SesameFormat;
 import javax.swing.GroupLayout.Alignment;
 import javax.swing.LayoutStyle.ComponentPlacement;
 
 /**
  * This code was edited or generated using CloudGarden's Jigloo SWT/Swing GUI
  * Builder, which is free for non-commercial use. If Jigloo is being used
  * commercially (ie, by a corporation, company or business for any purpose
  * whatever) then you should purchase a license for each developer using Jigloo.
  * Please visit www.cloudgarden.com for details. Use of Jigloo implies
  * acceptance of these licensing terms. A COMMERCIAL LICENSE HAS NOT BEEN
  * PURCHASED FOR THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED LEGALLY FOR
  * ANY CORPORATE OR COMMERCIAL PURPOSE.
  */
 public class VisualFormatsJPanel extends javax.swing.JPanel implements
 		ITabComponent<URI>, IEditable {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	private static final Logger LOG = LoggerFactory
 			.getLogger(VisualFormatsJPanel.class);
 
 	private static int NO_ROW_SELECTED = -1;
 
 	private FormatModel initialFormatModel = null;
 	private URI formatUri = null;
 	private String formatDescription = "";
 
 	private boolean createNewFormat = false;
 
 	private JLabel formatNameLabel = null;
 	private JTextField formatNameText = null;
 	private JButton detailsBtn = null;
 	private JScrollPane domainTableSrollPane = null;
 	private JTable domainTable = null;
 	private FormatItemNode formatItemNode = null;
 	private FormatJScrollPane representingScrollPane = null;
 	private ButtonGroup labelButtonGroup;
 	private JLabel valueLabel;
 	private JPanel formatURIJPanel;
 	private JLabel labelNote;
 	private JButton newSelectorBtn;
 	private JScrollPane scrollPaneStyleSheetOverview;
 	private JLabel txtCSSFilename;
 	private JLabel lblCSSFilename;
 	private JTextArea txtStyleSheetOverview;
 	private JPanel styleSheetOverview;
 	private JList listAssociatedGroups;
 	private JPanel associatedGroupsPanel;
 	private JPanel purposePanel;
 	private ButtonGroup buttonGroupPurpose;
 	private JRadioButton radioPurposePrint;
 	private JRadioButton radioPurposeProjection;
 	private JRadioButton radioPurposeScreen;
 	private JRadioButton radioPurposeDefault;
 	private ContentSelectorJPanel valueNoValueContentSelector;
 	private JButton btnPreview;
 	private JComboBox valueDisplayFormat;
 	private JButton closeBtn;
 	private JButton deleteBtn;
 	private JButton saveBtn;
 	private JButton deleteSelectedBtn;
 	private JButton editSelectedBtn;
 	private JPanel formatDomainPanel;
 	private JPanel valueJPanel;
 	private JTextField labelLiteralTextField;
 	private JRadioButton labelLiteral;
 	private JRadioButton labelNone;
 	private JLabel labelLabel;
 	private JPanel labelJPanel;
 	private JRadioButton labelDefault;
 	private ContentSelectorJPanel valueCSSClassNameSelector;
 	private ContentSelectorJPanel labelCSSClassNameSelector;
 	private ContentSelectorJPanel lastValueContentSelector;
 	private ContentSelectorJPanel afterValueContentSelector;
 	private JPanel resourceBox;
 	private ContentSelectorJPanel beforePropertyContentSelector;
 	private ContentSelectorJPanel beforeValueContentSelector;
 	private ContentSelectorJPanel firstValueContentSelector;
 	private JPanel valueBox;
 	private ContentSelectorJPanel noValueContentSelector;
 	private ContentSelectorJPanel afterLabelContentSelector;
 	private ContentSelectorJPanel propertyCSSClassNameSelector;
 	private JPanel rightContainer;
 	private JPanel labelBox;
 	private ContentSelectorJPanel beforeLabelContentSelector;
 	private JPanel leftContainer;
 	private JPanel propertyInside;
 	private JPanel propertyBox;
 	private ContentSelectorJPanel resouceCSSClassNameSelector;
 	private ContentSelectorJPanel afterPropertyContentSelector;
 	private ContentSelectorJPanel afterResourceContentSelector;
 	private ContentSelectorJPanel beforeResourceContentSelector;
 	private JPanel abstractBoxModel;
 
 	private class FormatJScrollPane extends JScrollPane implements IContextMenu {
 
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = 1L;
 
 		public FormatJScrollPane(VisualFormatsJPanel formatsJPanel) {
 			super(formatsJPanel);
 		}
 
 		@Override
 		public List<JMenuItem> getMenu() {
 			return formatItemNode != null ? formatItemNode.getMenu()
 					: Collections.<JMenuItem> emptyList();
 		}
 	}
 
 	@Override
 	public URI getItem() {
 		return formatUri;
 	}
 
 	@Override
 	public String getLabel() {
 		return createNewFormat ? "New format" : FresnelUtils
 				.getLocalName(initialFormatModel.getUri());
 	}
 
 	@Override
 	public JScrollPane getScrollPane() {
 		if (representingScrollPane == null) {
 			representingScrollPane = new FormatJScrollPane(this);
 		}
 
 		return representingScrollPane;
 	}
 
 	@Override
 	public void doDelete() {
 		new MessageDialog(GuiUtils.getOwnerFrame(this), "Confirmation",
 				"Do you really want to delete the format '"
 						+ initialFormatModel.getModelUri() + "'?",
 				new ActionListener() {
 					@Override
 					public void actionPerformed(ActionEvent e) {
 						FresnelRepositoryDao fresnelDao = ContextHolder
 								.getInstance().getFresnelRepositoryDao();
 						fresnelDao.deleteFresnelResource(initialFormatModel);
 						AppEventsManager.getInstance()
 								.fireRepositoryDataChanged(
 										this,
 										ContextHolder.getInstance()
 												.getFresnelRepositoryName());
 					}
 				}).setVisible(true);
 	}
 
 	@Override
 	public void doSave() {
 		if (validateForm()) {
 			// Insert new statements
 			FormatModel formatModel = saveFormatModel();
 			FresnelRepositoryDao fresnelDao = ContextHolder.getInstance()
 					.getFresnelRepositoryDao();
 			if (createNewFormat) {
 				fresnelDao.updateFresnelResource(null, formatModel);
 			} else {
 				fresnelDao.updateFresnelResource(initialFormatModel,
 						formatModel);
 			}
 
 			// If changes were successfully commited then switch to new initial
 			// model
 			initialFormatModel = formatModel;
 			createNewFormat = false;
 
 			AppEventsManager.getInstance().fireRepositoryDataChanged(this,
 					ContextHolder.getInstance().getFresnelRepositoryName());
 		}
 	}
 
 	/**
 	 * Auto-generated main method to display this JPanel inside a new JFrame.
 	 */
 
 	public VisualFormatsJPanel(URI formatUri, FormatItemNode formatItemNode) {
 		super();
 
 		this.formatUri = formatUri;
 		this.formatItemNode = formatItemNode;
 		this.createNewFormat = (formatUri == null);
 
 		FormatModelManager modelManager = new FormatModelManager();
 
 		if (createNewFormat) {
 			initialFormatModel = modelManager.buildNewModel();
 			formatUri = null;
 		} else {
 			FresnelRepositoryDao fresnelDao = ContextHolder.getInstance()
 					.getFresnelRepositoryDao();
 			Format format = fresnelDao.getFormat(formatUri.toString());
 			this.formatUri = formatUri;
 
 			initialFormatModel = modelManager.buildModel(format);
 		}
 
 		initGUI();
 		loadFormatModel(initialFormatModel);
 	}
 
 	private void loadFormatModel(final FormatModel formatModel) {
 		final FormatDomainTableModel formatDomainTableModel = (FormatDomainTableModel) domainTable
 				.getModel();
 
 		SwingUtilities.invokeLater(new Runnable() {
 			@Override
 			public void run() {
 				formatNameText.setText(formatModel.getUri());
 				formatDescription = formatModel.getComment();
 
 				// Domain selectors
 				List<DomainSelectorGuiWrapper> dsModelList = new ArrayList<DomainSelectorGuiWrapper>();
 				for (DomainSelectorGuiWrapper ds : formatModel
 						.getDomainSelectors()) {
 					dsModelList.add(ds.clone());
 				}
 				formatDomainTableModel.addAll(dsModelList);
 
 				listAssociatedGroups.setModel(new DefaultComboBoxModel(
 						formatModel.getAssociatedGroupURIs().toArray()));
 
 				// Styles
 				resouceCSSClassNameSelector.setStyleType(StyleType.RESOURCE);
 				propertyCSSClassNameSelector.setStyleType(StyleType.PROPERTY);
 				labelCSSClassNameSelector.setStyleType(StyleType.LABEL);
 				valueCSSClassNameSelector.setStyleType(StyleType.VALUE);
 
 				for (StyleGuiWrapper style : formatModel.getStyles()) {
 					if (style.getType() == StyleType.RESOURCE) {
 						resouceCSSClassNameSelector.setStyleGuiWrapper(style
 								.clone());
 					} else if (style.getType() == StyleType.PROPERTY) {
 						propertyCSSClassNameSelector.setStyleGuiWrapper(style
 								.clone());
 					} else if (style.getType() == StyleType.LABEL) {
 						labelCSSClassNameSelector.setStyleGuiWrapper(style
 								.clone());
 					} else if (style.getType() == StyleType.VALUE) {
 						valueCSSClassNameSelector.setStyleGuiWrapper(style
 								.clone());
 					}
 				}
 
 				// Additional contents
 				for (AdditionalContentGuiWrapper additionalContent : formatModel
 						.getAdditionalContents()) {
 					if (additionalContent.getType() == AdditionalContentType.RESOURCE) {
 						beforeResourceContentSelector.setAdditionalContentGuiWrapper(
 								additionalContent.clone(),
 								AdditionalContentPositionType.CONTENT_BEFORE);
 						afterResourceContentSelector.setAdditionalContentGuiWrapper(
 								additionalContent.clone(),
 								AdditionalContentPositionType.CONTENT_AFTER);
 					} else if (additionalContent.getType() == AdditionalContentType.PROPERTY) {
 						beforePropertyContentSelector.setAdditionalContentGuiWrapper(
 								additionalContent.clone(),
 								AdditionalContentPositionType.CONTENT_BEFORE);
 						afterPropertyContentSelector.setAdditionalContentGuiWrapper(
 								additionalContent.clone(),
 								AdditionalContentPositionType.CONTENT_AFTER);
 					} else if (additionalContent.getType() == AdditionalContentType.LABEL) {
 						beforeLabelContentSelector.setAdditionalContentGuiWrapper(
 								additionalContent.clone(),
 								AdditionalContentPositionType.CONTENT_BEFORE);
 						afterLabelContentSelector.setAdditionalContentGuiWrapper(
 								additionalContent.clone(),
 								AdditionalContentPositionType.CONTENT_AFTER);
 						noValueContentSelector.setAdditionalContentGuiWrapper(
 								additionalContent.clone(),
 								AdditionalContentPositionType.CONTENT_NO_VALUE);
 					} else if (additionalContent.getType() == AdditionalContentType.VALUE) {
 						beforeValueContentSelector.setAdditionalContentGuiWrapper(
 								additionalContent.clone(),
 								AdditionalContentPositionType.CONTENT_BEFORE);
 						afterValueContentSelector.setAdditionalContentGuiWrapper(
 								additionalContent.clone(),
 								AdditionalContentPositionType.CONTENT_AFTER);
 						firstValueContentSelector.setAdditionalContentGuiWrapper(
 								additionalContent.clone(),
 								AdditionalContentPositionType.CONTENT_FIRST);
 						lastValueContentSelector.setAdditionalContentGuiWrapper(
 								additionalContent.clone(),
 								AdditionalContentPositionType.CONTENT_LAST);
 						valueNoValueContentSelector.setAdditionalContentGuiWrapper(
 								additionalContent.clone(),
 								AdditionalContentPositionType.CONTENT_NO_VALUE);
 					}
 				}
 
 				// Set label settings
 				if (formatModel.getLabelType() == LabelType.NOT_SPECIFIED) {
 					labelDefault.setSelected(true);
 				} else if (formatModel.getLabelType() == LabelType.NONE) {
 					labelNone.setSelected(true);
 				} else if (formatModel.getLabelType() == LabelType.SHOW) {
 					labelLiteral.setSelected(true);
 					labelLiteralTextField.setEnabled(true);
 					labelLiteralTextField.setText(formatModel
 							.getLiteralLabelValue());
 				}
 
 				// Set purpose settings
 				if (formatModel.getPurposeType() == FormatPurposeType.NOT_SPECIFIED) {
 					radioPurposeDefault.setSelected(true);
 				} else if (formatModel.getPurposeType() == FormatPurposeType.SCREEN) {
 					radioPurposeScreen.setSelected(true);
 				} else if (formatModel.getPurposeType() == FormatPurposeType.PROJECTION) {
 					radioPurposeProjection.setSelected(true);
 				} else if (formatModel.getPurposeType() == FormatPurposeType.PRINT) {
 					radioPurposePrint.setSelected(true);
 				}
 
 				// Set value settings
 				if(formatModel.getValueType() != null){
 					valueDisplayFormat.setSelectedItem(new ValueDisplayFormat(
							formatModel.getValueType(), null));
 				}
 			}
 		});
 
 	}
 
 	private FormatModel saveFormatModel() {
 		FormatModel format = new FormatModel();
 
 		FormatDomainTableModel formatDomainTableModel = (FormatDomainTableModel) domainTable
 				.getModel();
 		format.setUri(formatNameText.getText());
 		format.setComment(formatDescription);
 		// Save domain selectors
 		format.setDomainSelectors(formatDomainTableModel.getAll());
 		List<StyleGuiWrapper> styleWrappers = new ArrayList<StyleGuiWrapper>();
 		if (resouceCSSClassNameSelector.getStyleGuiWrapper() != null) {
 			styleWrappers.add(resouceCSSClassNameSelector.getStyleGuiWrapper());
 		}
 		if (propertyCSSClassNameSelector.getStyleGuiWrapper() != null) {
 			styleWrappers
 					.add(propertyCSSClassNameSelector.getStyleGuiWrapper());
 		}
 		if (labelCSSClassNameSelector.getStyleGuiWrapper() != null) {
 			styleWrappers.add(labelCSSClassNameSelector.getStyleGuiWrapper());
 		}
 		if (valueCSSClassNameSelector.getStyleGuiWrapper() != null) {
 			styleWrappers.add(valueCSSClassNameSelector.getStyleGuiWrapper());
 		}
 		format.setStyles(styleWrappers);
 		// Save additional content
 		// Note: we have to recreate all wrapper structures
 		List<AdditionalContentGuiWrapper> additionalContentWrappers = new ArrayList<AdditionalContentGuiWrapper>();
 
 		AdditionalContentGuiWrapper resourceCGW = new AdditionalContentGuiWrapper(
 				AdditionalContentType.RESOURCE);
 		resourceCGW.setContentBefore(beforeResourceContentSelector
 				.getAdditionalContentGuiWrapper().getContentBefore());
 		resourceCGW.setContentAfter(afterResourceContentSelector
 				.getAdditionalContentGuiWrapper().getContentAfter());
 		additionalContentWrappers.add(resourceCGW);
 
 		AdditionalContentGuiWrapper propertyCGW = new AdditionalContentGuiWrapper(
 				AdditionalContentType.PROPERTY);
 		propertyCGW.setContentBefore(beforePropertyContentSelector
 				.getAdditionalContentGuiWrapper().getContentBefore());
 		propertyCGW.setContentAfter(afterPropertyContentSelector
 				.getAdditionalContentGuiWrapper().getContentAfter());
 		additionalContentWrappers.add(propertyCGW);
 
 		AdditionalContentGuiWrapper labelCGW = new AdditionalContentGuiWrapper(
 				AdditionalContentType.LABEL);
 		labelCGW.setContentBefore(beforeLabelContentSelector
 				.getAdditionalContentGuiWrapper().getContentBefore());
 		labelCGW.setContentAfter(afterLabelContentSelector
 				.getAdditionalContentGuiWrapper().getContentAfter());
 		labelCGW.setContentNoValue(noValueContentSelector
 				.getAdditionalContentGuiWrapper().getContentNoValue());
 		additionalContentWrappers.add(labelCGW);
 
 		AdditionalContentGuiWrapper valueCGW = new AdditionalContentGuiWrapper(
 				AdditionalContentType.VALUE);
 		valueCGW.setContentBefore(beforeValueContentSelector
 				.getAdditionalContentGuiWrapper().getContentBefore());
 		valueCGW.setContentAfter(afterValueContentSelector
 				.getAdditionalContentGuiWrapper().getContentAfter());
 		valueCGW.setContentFirst(firstValueContentSelector
 				.getAdditionalContentGuiWrapper().getContentFirst());
 		valueCGW.setContentLast(lastValueContentSelector
 				.getAdditionalContentGuiWrapper().getContentLast());
 		valueCGW.setContentNoValue(valueNoValueContentSelector
 				.getAdditionalContentGuiWrapper().getContentNoValue());
 		additionalContentWrappers.add(valueCGW);
 
 		format.setAdditionalContents(additionalContentWrappers);
 
 		// Save label settings
 		if (labelDefault.isSelected()) {
 			format.setLabelType(LabelType.NOT_SPECIFIED);
 		} else if (labelNone.isSelected()) {
 			format.setLabelType(LabelType.NONE);
 		} else if (labelLiteral.isSelected()) {
 			format.setLiteralLabelValue(labelLiteralTextField.getText());
 			format.setLabelType(LabelType.SHOW);
 		} else {
 			LOG.warn("No label type radio button selected - using default!");
 			format.setLabelType(LabelType.NOT_SPECIFIED);
 		}
 
 		// Save purpose settings
 		if (radioPurposeDefault.isSelected()) {
 			format.setPurposeType(FormatPurposeType.NOT_SPECIFIED);
 		} else if (radioPurposeScreen.isSelected()) {
 			format.setPurposeType(FormatPurposeType.SCREEN);
 		} else if (radioPurposeProjection.isSelected()) {
 			format.setPurposeType(FormatPurposeType.PROJECTION);
 		} else if (radioPurposePrint.isSelected()) {
 			format.setPurposeType(FormatPurposeType.PRINT);
 		} else {
 			LOG.warn("No purpose type radio button selected - using default!");
 			format.setPurposeType(FormatPurposeType.NOT_SPECIFIED);
 		}
 
 		// Save values settings
 		ValueDisplayFormat vdf = (ValueDisplayFormat) valueDisplayFormat
 				.getSelectedItem();
 		format.setValueType(vdf.getValueType());
 
 		return format;
 	}
 
 	private void initGUI() {
 		try {
 			GroupLayout thisLayout = new GroupLayout((JComponent) this);
 			this.setLayout(thisLayout);
 			thisLayout
 					.setVerticalGroup(thisLayout
 							.createSequentialGroup()
 							.addContainerGap()
 							.addComponent(getFormatURIJPanel(),
 									GroupLayout.PREFERRED_SIZE, 26,
 									GroupLayout.PREFERRED_SIZE)
 							.addGroup(
 									thisLayout
 											.createParallelGroup()
 											.addComponent(
 													getFormatDomainPanel(),
 													GroupLayout.Alignment.LEADING,
 													GroupLayout.PREFERRED_SIZE,
 													148,
 													GroupLayout.PREFERRED_SIZE)
 											.addComponent(
 													getJPanel1(),
 													GroupLayout.Alignment.LEADING,
 													GroupLayout.PREFERRED_SIZE,
 													148,
 													GroupLayout.PREFERRED_SIZE))
 							.addPreferredGap(
 									LayoutStyle.ComponentPlacement.UNRELATED)
 							.addGroup(
 									thisLayout
 											.createParallelGroup()
 											.addGroup(
 													GroupLayout.Alignment.LEADING,
 													thisLayout
 															.createSequentialGroup()
 															.addComponent(
 																	getAssociatedGroupsPanel(),
 																	GroupLayout.PREFERRED_SIZE,
 																	179,
 																	GroupLayout.PREFERRED_SIZE)
 															.addPreferredGap(
 																	LayoutStyle.ComponentPlacement.RELATED)
 															.addComponent(
 																	getStyleSheetOverview(),
 																	GroupLayout.PREFERRED_SIZE,
 																	281,
 																	GroupLayout.PREFERRED_SIZE)
 															.addGap(0,
 																	0,
 																	Short.MAX_VALUE))
 											.addComponent(
 													getAbstractBoxModel(),
 													GroupLayout.Alignment.LEADING,
 													0, 466, Short.MAX_VALUE))
 							.addPreferredGap(
 									LayoutStyle.ComponentPlacement.RELATED)
 							.addGroup(
 									thisLayout
 											.createParallelGroup(
 													GroupLayout.Alignment.BASELINE)
 											.addComponent(
 													getSaveBtn(),
 													GroupLayout.Alignment.BASELINE,
 													GroupLayout.PREFERRED_SIZE,
 													GroupLayout.PREFERRED_SIZE,
 													GroupLayout.PREFERRED_SIZE)
 											.addComponent(
 													getDeleteBtn(),
 													GroupLayout.Alignment.BASELINE,
 													GroupLayout.PREFERRED_SIZE,
 													GroupLayout.PREFERRED_SIZE,
 													GroupLayout.PREFERRED_SIZE)
 											.addComponent(
 													getCloseBtn(),
 													GroupLayout.Alignment.BASELINE,
 													GroupLayout.PREFERRED_SIZE,
 													GroupLayout.PREFERRED_SIZE,
 													GroupLayout.PREFERRED_SIZE)
 											.addComponent(
 													getBtnPreview(),
 													GroupLayout.Alignment.BASELINE,
 													GroupLayout.PREFERRED_SIZE,
 													GroupLayout.PREFERRED_SIZE,
 													GroupLayout.PREFERRED_SIZE))
 							.addContainerGap());
 			thisLayout
 					.setHorizontalGroup(thisLayout
 							.createSequentialGroup()
 							.addContainerGap()
 							.addGroup(
 									thisLayout
 											.createParallelGroup()
 											.addGroup(
 													GroupLayout.Alignment.LEADING,
 													thisLayout
 															.createSequentialGroup()
 															.addComponent(
 																	getSaveBtn(),
 																	GroupLayout.PREFERRED_SIZE,
 																	83,
 																	GroupLayout.PREFERRED_SIZE)
 															.addPreferredGap(
 																	LayoutStyle.ComponentPlacement.RELATED)
 															.addComponent(
 																	getDeleteBtn(),
 																	GroupLayout.PREFERRED_SIZE,
 																	83,
 																	GroupLayout.PREFERRED_SIZE)
 															.addPreferredGap(
 																	LayoutStyle.ComponentPlacement.RELATED)
 															.addComponent(
 																	getCloseBtn(),
 																	GroupLayout.PREFERRED_SIZE,
 																	83,
 																	GroupLayout.PREFERRED_SIZE)
 															.addPreferredGap(
 																	LayoutStyle.ComponentPlacement.RELATED)
 															.addComponent(
 																	getBtnPreview(),
 																	GroupLayout.PREFERRED_SIZE,
 																	80,
 																	GroupLayout.PREFERRED_SIZE)
 															.addGap(279))
 											.addComponent(
 													getAbstractBoxModel(),
 													GroupLayout.Alignment.LEADING,
 													GroupLayout.PREFERRED_SIZE,
 													623,
 													GroupLayout.PREFERRED_SIZE)
 											.addComponent(
 													getFormatDomainPanel(),
 													GroupLayout.Alignment.LEADING,
 													GroupLayout.PREFERRED_SIZE,
 													623,
 													GroupLayout.PREFERRED_SIZE)
 											.addComponent(
 													getFormatURIJPanel(),
 													GroupLayout.Alignment.LEADING,
 													GroupLayout.PREFERRED_SIZE,
 													623,
 													GroupLayout.PREFERRED_SIZE))
 							.addPreferredGap(
 									LayoutStyle.ComponentPlacement.RELATED)
 							.addGroup(
 									thisLayout
 											.createParallelGroup()
 											.addComponent(
 													getAssociatedGroupsPanel(),
 													GroupLayout.Alignment.LEADING,
 													0, 340, Short.MAX_VALUE)
 											.addComponent(
 													getStyleSheetOverview(),
 													GroupLayout.Alignment.LEADING,
 													0, 340, Short.MAX_VALUE)
 											.addComponent(
 													getJPanel1(),
 													GroupLayout.Alignment.LEADING,
 													0, 340, Short.MAX_VALUE))
 							.addContainerGap());
 			this.setPreferredSize(new java.awt.Dimension(993, 704));
 			Application.getInstance().getContext().getResourceMap(getClass())
 					.injectComponents(this);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	private JPanel getAbstractBoxModel() {
 		if (abstractBoxModel == null) {
 			abstractBoxModel = new JPanel();
 			BoxLayout abstractBoxModelLayout = new BoxLayout(abstractBoxModel,
 					javax.swing.BoxLayout.Y_AXIS);
 			abstractBoxModel.setLayout(abstractBoxModelLayout);
 			abstractBoxModel.setBorder(BorderFactory
 					.createTitledBorder("Fresnel Abstract Box Model"));
 			abstractBoxModel.setBounds(0, 0, 600, 488);
 			abstractBoxModel.add(getBeforeResourceContentSelector());
 			abstractBoxModel.add(Box.createVerticalStrut(3));
 			abstractBoxModel.add(getResourceBox());
 			abstractBoxModel.add(Box.createVerticalStrut(3));
 			abstractBoxModel.add(getAfterResourceContentSelector());
 		}
 		return abstractBoxModel;
 	}
 
 	private JPanel getResourceBox() {
 		if (resourceBox == null) {
 			resourceBox = new JPanel();
 			BoxLayout resourceBoxLayout = new BoxLayout(resourceBox,
 					javax.swing.BoxLayout.Y_AXIS);
 			resourceBox.setLayout(resourceBoxLayout);
 			resourceBox.setBorder(BorderFactory.createTitledBorder(
 					new LineBorder(new java.awt.Color(0, 0, 0), 1, false),
 					"Resource Box", TitledBorder.LEADING,
 					TitledBorder.DEFAULT_POSITION));
 			resourceBox.setName("resourceBox");
 			resourceBox.setBounds(5, 47, 613, 417);
 			resourceBox.setPreferredSize(new java.awt.Dimension(613, 432));
 			resourceBox.add(getResouceCSSClassNameSelector());
 			resourceBox.add(Box.createVerticalStrut(3));
 			resourceBox.add(getBeforePropertyContentSelector());
 			resourceBox.add(Box.createVerticalStrut(3));
 			resourceBox.add(getPropertyBox());
 			resourceBox.add(Box.createVerticalStrut(3));
 			resourceBox.add(getAfterPropertyContentSelector());
 		}
 		return resourceBox;
 	}
 
 	private ContentSelectorJPanel getBeforeResourceContentSelector() {
 		if (beforeResourceContentSelector == null) {
 			beforeResourceContentSelector = new ContentSelectorJPanel();
 			beforeResourceContentSelector
 					.setName("beforeResourceContentSelector");
 			beforeResourceContentSelector.setBounds(5, 21, 613, 20);
 		}
 		return beforeResourceContentSelector;
 	}
 
 	private ContentSelectorJPanel getAfterResourceContentSelector() {
 		if (afterResourceContentSelector == null) {
 			afterResourceContentSelector = new ContentSelectorJPanel();
 			afterResourceContentSelector
 					.setName("afterResourceContentSelector");
 			afterResourceContentSelector.setBounds(5, 464, 613, 20);
 		}
 		return afterResourceContentSelector;
 	}
 
 	private ContentSelectorJPanel getBeforePropertyContentSelector() {
 		if (beforePropertyContentSelector == null) {
 			beforePropertyContentSelector = new ContentSelectorJPanel();
 			beforePropertyContentSelector
 					.setName("beforePropertyContentSelector");
 			beforePropertyContentSelector.setBounds(5, 41, 580, 20);
 		}
 		return beforePropertyContentSelector;
 	}
 
 	private ContentSelectorJPanel getAfterPropertyContentSelector() {
 		if (afterPropertyContentSelector == null) {
 			afterPropertyContentSelector = new ContentSelectorJPanel();
 			afterPropertyContentSelector
 					.setName("afterPropertyContentSelector");
 			afterPropertyContentSelector.setBounds(5, 284, 580, 20);
 		}
 		return afterPropertyContentSelector;
 	}
 
 	private ContentSelectorJPanel getResouceCSSClassNameSelector() {
 		if (resouceCSSClassNameSelector == null) {
 			resouceCSSClassNameSelector = new ContentSelectorJPanel();
 			resouceCSSClassNameSelector.setName("resouceCSSClassNameSelector");
 			resouceCSSClassNameSelector.setBounds(5, 21, 580, 20);
 		}
 		return resouceCSSClassNameSelector;
 	}
 
 	private JPanel getPropertyBox() {
 		if (propertyBox == null) {
 			propertyBox = new JPanel();
 			BoxLayout propertyBoxLayout = new BoxLayout(propertyBox,
 					javax.swing.BoxLayout.Y_AXIS);
 			propertyBox.setBorder(BorderFactory.createTitledBorder(
 					new LineBorder(new java.awt.Color(0, 0, 0), 1, false),
 					"Property Box", TitledBorder.LEADING,
 					TitledBorder.DEFAULT_POSITION));
 			propertyBox.setLayout(propertyBoxLayout);
 			propertyBox.setName("propertyBox");
 			propertyBox.setBounds(5, 61, 580, 223);
 			propertyBox.setPreferredSize(new java.awt.Dimension(603, 328));
 			propertyBox.add(getPropertyCSSClassNameSelector());
 			propertyBox.add(getPropertyInside());
 		}
 		return propertyBox;
 	}
 
 	private JPanel getPropertyInside() {
 		if (propertyInside == null) {
 			propertyInside = new JPanel();
 			propertyInside.setName("propertyInside");
 			propertyInside.setBounds(5, 21, 570, 197);
 			propertyInside.setPreferredSize(new java.awt.Dimension(593, 309));
 			propertyInside.add(getLeftContainer());
 
 			propertyInside.add(getRightContainer());
 		}
 		return propertyInside;
 	}
 
 	private JPanel getLeftContainer() {
 		if (leftContainer == null) {
 			leftContainer = new JPanel();
 			BoxLayout leftContainerLayout = new BoxLayout(leftContainer,
 					javax.swing.BoxLayout.Y_AXIS);
 			leftContainer.setLayout(leftContainerLayout);
 			leftContainer.setName("leftContainer");
 			leftContainer.setBounds(0, 0, 287, 197);
 			leftContainer.setPreferredSize(new java.awt.Dimension(281, 187));
 			leftContainer.add(getBeforeLabelContentSelector());
 			leftContainer.add(Box.createVerticalStrut(3));
 			leftContainer.add(getLabelBox());
 			leftContainer.add(Box.createVerticalStrut(3));
 			leftContainer.add(getAfterLabelContentSelector());
 		}
 		return leftContainer;
 	}
 
 	private ContentSelectorJPanel getBeforeLabelContentSelector() {
 		if (beforeLabelContentSelector == null) {
 			beforeLabelContentSelector = new ContentSelectorJPanel();
 			beforeLabelContentSelector.setName("beforeLabelContentSelector");
 		}
 		return beforeLabelContentSelector;
 	}
 
 	private JPanel getLabelBox() {
 		if (labelBox == null) {
 			labelBox = new JPanel();
 			BoxLayout labelBoxLayout = new BoxLayout(labelBox,
 					javax.swing.BoxLayout.Y_AXIS);
 			labelBox.setLayout(labelBoxLayout);
 			labelBox.setBorder(BorderFactory.createTitledBorder(new LineBorder(
 					new java.awt.Color(0, 0, 0), 1, false), "Label Box",
 					TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION));
 			labelBox.setName("labelBox");
 			labelBox.setPreferredSize(new java.awt.Dimension(10, 127));
 			labelBox.add(getNoValueContentSelector());
 			labelBox.add(Box.createVerticalStrut(3));
 			labelBox.add(getLabelCSSClassNameSelector());
 			labelBox.add(getLabelJPanel());
 		}
 		return labelBox;
 	}
 
 	private JPanel getRightContainer() {
 		if (rightContainer == null) {
 			rightContainer = new JPanel();
 			BoxLayout rightContainerLayout = new BoxLayout(rightContainer,
 					javax.swing.BoxLayout.Y_AXIS);
 			rightContainer.setLayout(rightContainerLayout);
 			rightContainer.setBounds(293, 0, 277, 197);
 			rightContainer.setPreferredSize(new java.awt.Dimension(276, 234));
 			rightContainer.setName("rightContainer");
 			rightContainer.add(getFirstValueContentSelector());
 			rightContainer.add(Box.createVerticalStrut(3));
 			rightContainer.add(getBeforeValueContentSelector());
 			rightContainer.add(Box.createVerticalStrut(3));
 			rightContainer.add(getValueBox());
 			rightContainer.add(Box.createVerticalStrut(3));
 			rightContainer.add(getAfterValueContentSelector());
 			rightContainer.add(Box.createVerticalStrut(3));
 			rightContainer.add(getLastValueContentSelector());
 		}
 		return rightContainer;
 	}
 
 	private ContentSelectorJPanel getPropertyCSSClassNameSelector() {
 		if (propertyCSSClassNameSelector == null) {
 			propertyCSSClassNameSelector = new ContentSelectorJPanel();
 			propertyCSSClassNameSelector
 					.setName("propertyCSSClassNameSelector");
 		}
 		return propertyCSSClassNameSelector;
 	}
 
 	private ContentSelectorJPanel getAfterLabelContentSelector() {
 		if (afterLabelContentSelector == null) {
 			afterLabelContentSelector = new ContentSelectorJPanel();
 			afterLabelContentSelector.setName("afterLabelContentSelector");
 		}
 		return afterLabelContentSelector;
 	}
 
 	private ContentSelectorJPanel getNoValueContentSelector() {
 		if (noValueContentSelector == null) {
 			noValueContentSelector = new ContentSelectorJPanel();
 			noValueContentSelector.setName("noValueContentSelector");
 		}
 		return noValueContentSelector;
 	}
 
 	private JPanel getValueBox() {
 		if (valueBox == null) {
 			valueBox = new JPanel();
 			BoxLayout valueBoxLayout = new BoxLayout(valueBox,
 					javax.swing.BoxLayout.Y_AXIS);
 			valueBox.setLayout(valueBoxLayout);
 			valueBox.setBorder(BorderFactory.createTitledBorder(new LineBorder(
 					new java.awt.Color(0, 0, 0), 1, false), "Value Box",
 					TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION));
 			valueBox.setName("valueBox");
 			valueBox.setPreferredSize(new java.awt.Dimension(276, 143));
 			valueBox.add(getValueNoValueContentSelector());
 			valueBox.add(getValueCSSClassNameSelector());
 			valueBox.add(getValueJPanel());
 			valueBox.add(Box.createVerticalGlue());
 		}
 		return valueBox;
 	}
 
 	private ContentSelectorJPanel getFirstValueContentSelector() {
 		if (firstValueContentSelector == null) {
 			firstValueContentSelector = new ContentSelectorJPanel();
 			firstValueContentSelector.setName("firstValueContentSelector");
 		}
 		return firstValueContentSelector;
 	}
 
 	private ContentSelectorJPanel getBeforeValueContentSelector() {
 		if (beforeValueContentSelector == null) {
 			beforeValueContentSelector = new ContentSelectorJPanel();
 			beforeValueContentSelector.setName("beforeValueContentSelector");
 		}
 		return beforeValueContentSelector;
 	}
 
 	private ContentSelectorJPanel getAfterValueContentSelector() {
 		if (afterValueContentSelector == null) {
 			afterValueContentSelector = new ContentSelectorJPanel();
 			afterValueContentSelector.setName("afterValueContentSelector");
 		}
 		return afterValueContentSelector;
 	}
 
 	private ContentSelectorJPanel getLastValueContentSelector() {
 		if (lastValueContentSelector == null) {
 			lastValueContentSelector = new ContentSelectorJPanel();
 			lastValueContentSelector.setName("lastValueContentSelector");
 		}
 		return lastValueContentSelector;
 	}
 
 	private ContentSelectorJPanel getLabelCSSClassNameSelector() {
 		if (labelCSSClassNameSelector == null) {
 			labelCSSClassNameSelector = new ContentSelectorJPanel();
 			labelCSSClassNameSelector.setName("labelCSSClassNameSelector");
 		}
 		return labelCSSClassNameSelector;
 	}
 
 	private ContentSelectorJPanel getValueCSSClassNameSelector() {
 		if (valueCSSClassNameSelector == null) {
 			valueCSSClassNameSelector = new ContentSelectorJPanel();
 			valueCSSClassNameSelector.setName("valueCSSClassNameSelector");
 		}
 		return valueCSSClassNameSelector;
 	}
 
 	private ButtonGroup getLabelButtonGroup() {
 		if (labelButtonGroup == null) {
 			labelButtonGroup = new ButtonGroup();
 		}
 		return labelButtonGroup;
 	}
 
 	private JRadioButton getLabelDefault() {
 		if (labelDefault == null) {
 			labelDefault = new JRadioButton();
 			labelDefault.setName("labelDefault");
 		}
 		return labelDefault;
 	}
 
 	private JPanel getLabelJPanel() {
 		if (labelJPanel == null) {
 			labelJPanel = new JPanel();
 			FormLayout labelJPanelLayout = new FormLayout(
 					"max(p;5dlu), max(p;5dlu), max(p;5dlu), max(p;5dlu)",
 					"max(p;5dlu), max(p;5dlu), max(p;5dlu), max(p;5dlu)");
 			labelJPanel.setLayout(labelJPanelLayout);
 			labelJPanel.setName("labelJPanel");
 			labelJPanel.add(getLabelLabel(), new CellConstraints(
 					"1, 1, 1, 1, default, default"));
 			labelJPanel.add(getLabelDefault(), new CellConstraints(
 					"3, 1, 1, 1, default, default"));
 			labelJPanel.add(getLabelNone(), new CellConstraints(
 					"3, 2, 1, 1, default, default"));
 			labelJPanel.add(getLabelLiteral(), new CellConstraints(
 					"3, 3, 1, 1, default, default"));
 			labelJPanel.add(getLabelLiteralTextField(), new CellConstraints(
 					"4, 3, 1, 1, default, default"));
 			labelJPanel.add(getLabelNote(), new CellConstraints(
 					"4, 1, 1, 1, default, default"));
 
 			getLabelButtonGroup().add(getLabelDefault());
 			getLabelButtonGroup().add(getLabelNone());
 			getLabelButtonGroup().add(getLabelLiteral());
 		}
 		return labelJPanel;
 	}
 
 	private JLabel getLabelLabel() {
 		if (labelLabel == null) {
 			labelLabel = new JLabel();
 			labelLabel.setName("labelLabel");
 		}
 		return labelLabel;
 	}
 
 	private JRadioButton getLabelNone() {
 		if (labelNone == null) {
 			labelNone = new JRadioButton();
 			labelNone.setName("labelNone");
 		}
 		return labelNone;
 	}
 
 	private JRadioButton getLabelLiteral() {
 		if (labelLiteral == null) {
 			labelLiteral = new JRadioButton();
 			labelLiteral.setName("labelLiteral");
 		}
 		return labelLiteral;
 	}
 
 	private JTextField getLabelLiteralTextField() {
 		if (labelLiteralTextField == null) {
 			labelLiteralTextField = new JTextField();
 			labelLiteralTextField.setPreferredSize(new java.awt.Dimension(175,
 					23));
 		}
 		return labelLiteralTextField;
 	}
 
 	private JPanel getValueJPanel() {
 		if (valueJPanel == null) {
 			valueJPanel = new JPanel();
 			FormLayout valueJPanelLayout = new FormLayout(
 					"max(p;5dlu), max(p;5dlu), max(p;5dlu), max(p;5dlu)",
 					"max(p;5dlu), max(p;5dlu), max(p;5dlu), max(p;5dlu)");
 			valueJPanel.setLayout(valueJPanelLayout);
 			valueJPanel.setPreferredSize(new java.awt.Dimension(266, 114));
 			valueJPanel.setName("valueJPanel");
 			valueJPanel.add(getValueLabel(), new CellConstraints(
 					"1, 1, 1, 1, default, default"));
 			valueJPanel.add(getValueDisplayFormat(), new CellConstraints(
 					"3, 1, 1, 1, default, default"));
 		}
 		return valueJPanel;
 	}
 
 	private JLabel getValueLabel() {
 		if (valueLabel == null) {
 			valueLabel = new JLabel();
 			valueLabel.setName("valueLabel");
 		}
 		return valueLabel;
 	}
 
 	private JLabel getLabelNote() {
 		if (labelNote == null) {
 			labelNote = new JLabel();
 			labelNote.setName("labelNote");
 		}
 		return labelNote;
 	}
 
 	private JPanel getFormatURIJPanel() {
 		if (formatURIJPanel == null) {
 			formatURIJPanel = new JPanel();
 			GroupLayout gl_formatURIJPanel = new GroupLayout(
 					(JComponent) formatURIJPanel);
 			formatURIJPanel.setLayout(gl_formatURIJPanel);
 			formatNameLabel = new JLabel();
 			formatNameLabel.setName("formatNameLabel");
 			formatNameText = new JTextField();
 			detailsBtn = new JButton();
 			detailsBtn.setName("detailsBtn");
 			detailsBtn.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent evt) {
 					detailsBtnActionPerformed(evt);
 				}
 			});
 			gl_formatURIJPanel.setHorizontalGroup(gl_formatURIJPanel
 					.createSequentialGroup()
 					.addComponent(formatNameLabel, GroupLayout.PREFERRED_SIZE,
 							106, GroupLayout.PREFERRED_SIZE)
 					.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
 					.addComponent(formatNameText, GroupLayout.PREFERRED_SIZE,
 							384, GroupLayout.PREFERRED_SIZE)
 					.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
 					.addComponent(detailsBtn, GroupLayout.PREFERRED_SIZE, 109,
 							GroupLayout.PREFERRED_SIZE)
 					.addGap(0, 6, Short.MAX_VALUE));
 			gl_formatURIJPanel.setVerticalGroup(gl_formatURIJPanel
 					.createSequentialGroup()
 					.addGroup(
 							gl_formatURIJPanel
 									.createParallelGroup(
 											GroupLayout.Alignment.BASELINE)
 									.addComponent(formatNameLabel,
 											GroupLayout.Alignment.BASELINE,
 											GroupLayout.PREFERRED_SIZE, 18,
 											GroupLayout.PREFERRED_SIZE)
 									.addComponent(formatNameText,
 											GroupLayout.Alignment.BASELINE,
 											GroupLayout.PREFERRED_SIZE,
 											GroupLayout.PREFERRED_SIZE,
 											GroupLayout.PREFERRED_SIZE)
 									.addComponent(detailsBtn,
 											GroupLayout.Alignment.BASELINE,
 											GroupLayout.PREFERRED_SIZE, 23,
 											GroupLayout.PREFERRED_SIZE))
 					.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED));
 		}
 		return formatURIJPanel;
 	}
 
 	private JPanel getFormatDomainPanel() {
 		if (formatDomainPanel == null) {
 			formatDomainPanel = new JPanel();
 			GroupLayout gl_formatDomainPanel = new GroupLayout(
 					(JComponent) formatDomainPanel);
 			formatDomainPanel.setLayout(gl_formatDomainPanel);
 			formatDomainPanel.setBorder(BorderFactory
 					.createTitledBorder("Format Domains"));
 			{
 				domainTableSrollPane = new JScrollPane();
 				{
 					domainTable = new JTable();
 					domainTable.setModel(new FormatDomainTableModel());
 					// domainTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
 					domainTable.getColumnModel().getColumn(0)
 							.setPreferredWidth(100);
 					domainTable.getColumnModel().getColumn(1)
 							.setPreferredWidth(100);
 					domainTable.getColumnModel().getColumn(2)
 							.setPreferredWidth(260);
 					domainTable.setName("domainTable");
 					domainTable
 							.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 					domainTableSrollPane.setViewportView(domainTable);
 				}
 			}
 			gl_formatDomainPanel.setHorizontalGroup(gl_formatDomainPanel
 					.createSequentialGroup()
 					.addContainerGap()
 					.addComponent(domainTableSrollPane,
 							GroupLayout.PREFERRED_SIZE, 464,
 							GroupLayout.PREFERRED_SIZE)
 					.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
 					.addGroup(
 							gl_formatDomainPanel
 									.createParallelGroup()
 									.addComponent(getNewSelectorBtn(),
 											GroupLayout.Alignment.LEADING, 0,
 											120, Short.MAX_VALUE)
 									.addComponent(getEditSelectedBtn(),
 											GroupLayout.Alignment.LEADING, 0,
 											120, Short.MAX_VALUE)
 									.addComponent(getDeleteSelectedBtn(),
 											GroupLayout.Alignment.LEADING, 0,
 											120, Short.MAX_VALUE))
 					.addContainerGap());
 			gl_formatDomainPanel
 					.setVerticalGroup(gl_formatDomainPanel
 							.createSequentialGroup()
 							.addGroup(
 									gl_formatDomainPanel
 											.createParallelGroup()
 											.addGroup(
 													GroupLayout.Alignment.LEADING,
 													gl_formatDomainPanel
 															.createSequentialGroup()
 															.addComponent(
 																	getNewSelectorBtn(),
 																	GroupLayout.PREFERRED_SIZE,
 																	GroupLayout.PREFERRED_SIZE,
 																	GroupLayout.PREFERRED_SIZE)
 															.addPreferredGap(
 																	LayoutStyle.ComponentPlacement.RELATED)
 															.addComponent(
 																	getEditSelectedBtn(),
 																	GroupLayout.PREFERRED_SIZE,
 																	GroupLayout.PREFERRED_SIZE,
 																	GroupLayout.PREFERRED_SIZE)
 															.addPreferredGap(
 																	LayoutStyle.ComponentPlacement.RELATED)
 															.addComponent(
 																	getDeleteSelectedBtn(),
 																	GroupLayout.PREFERRED_SIZE,
 																	23,
 																	GroupLayout.PREFERRED_SIZE)
 															.addGap(0,
 																	31,
 																	Short.MAX_VALUE))
 											.addComponent(
 													domainTableSrollPane,
 													GroupLayout.Alignment.LEADING,
 													0, 110, Short.MAX_VALUE))
 							.addContainerGap());
 		}
 		return formatDomainPanel;
 	}
 
 	private JButton getNewSelectorBtn() {
 		if (newSelectorBtn == null) {
 			newSelectorBtn = new JButton();
 			newSelectorBtn.setName("newSelectorBtn");
 			newSelectorBtn.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent evt) {
 					newSelectorBtnActionPerformed(evt);
 				}
 			});
 		}
 		return newSelectorBtn;
 	}
 
 	private JButton getEditSelectedBtn() {
 		if (editSelectedBtn == null) {
 			editSelectedBtn = new JButton();
 			editSelectedBtn.setName("editSelectedBtn");
 			editSelectedBtn.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent evt) {
 					editSelectedBtnActionPerformed(evt);
 				}
 			});
 		}
 		return editSelectedBtn;
 	}
 
 	private JButton getDeleteSelectedBtn() {
 		if (deleteSelectedBtn == null) {
 			deleteSelectedBtn = new JButton();
 			deleteSelectedBtn.setName("deleteSelectedBtn");
 			deleteSelectedBtn.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent evt) {
 					deleteSelectedBtnActionPerformed(evt);
 				}
 			});
 		}
 		return deleteSelectedBtn;
 	}
 
 	public void setFormatDescription(String description) {
 		this.formatDescription = description;
 	}
 
 	private void detailsBtnActionPerformed(ActionEvent evt) {
 		final ElementDetailDialog dialog = new ElementDetailDialog(
 				GuiUtils.getOwnerFrame(this), true, this.formatDescription);
 		final VisualFormatsJPanel thisPanel = this;
 		dialog.addWindowListener(new WindowAdapter() {
 			@Override
 			public void windowClosed(WindowEvent ev) {
 				thisPanel.setFormatDescription(dialog.getDescription());
 			}
 		});
 		GuiUtils.centerOnScreen(dialog);
 		dialog.setVisible(true);
 	}
 
 	private void newSelectorBtnActionPerformed(ActionEvent evt) {
 		// Get table model
 		final FormatDomainTableModel tableModel = (FormatDomainTableModel) domainTable
 				.getModel();
 		// Create new domain selector and pass it to domain selector dialog
 		final DomainSelectorGuiWrapper ds = new DomainSelectorGuiWrapper();
 		ds.setUpdated(false);
 		LOG.info("Creating new domain selector: " + ds.toString());
 		final DomainSelectorDialog domainSelectorDialog = new DomainSelectorDialog(
 				GuiUtils.getOwnerFrame(this), true, ds);
 		domainSelectorDialog.addWindowListener(new WindowAdapter() {
 			@Override
 			public void windowClosed(WindowEvent ev) {
 				// Take selector returned from domain selector dialog and add it
 				// to table model
 				LOG.info("Merging changes of domain selector: " + ds.toString());
 				SwingUtilities.invokeLater(new Runnable() {
 					@Override
 					public void run() {
 						if (ds.isUpdated()) {
 							tableModel.addRow(ds);
 						}
 					}
 				});
 			}
 		});
 		// Display domain selector dialog
 		SwingUtilities.invokeLater(new Runnable() {
 			@Override
 			public void run() {
 				GuiUtils.centerOnScreen(domainSelectorDialog);
 				domainSelectorDialog.setVisible(true);
 			}
 		});
 	}
 
 	private void editSelectedBtnActionPerformed(ActionEvent evt) {
 		// Get selected domain selector from table model
 		final FormatDomainTableModel tableModel = (FormatDomainTableModel) domainTable
 				.getModel();
 		final int selectedRowIndex = domainTable.getSelectedRow();
 
 		if (selectedRowIndex == NO_ROW_SELECTED) {
 			return;
 		}
 
 		final DomainSelectorGuiWrapper ds = tableModel.getRow(domainTable
 				.getSelectedRow());
 		// Pass domain selector to domain selector dialog
 		LOG.info("Editing domain selector: " + ds.toString());
 		final DomainSelectorDialog domainSelectorDialog = new DomainSelectorDialog(
 				GuiUtils.getOwnerFrame(this), true, ds);
 		domainSelectorDialog.addWindowListener(new WindowAdapter() {
 			@Override
 			public void windowClosed(WindowEvent ev) {
 				// Take updated selector from domain selector dialog and update
 				// table model
 				LOG.info("Merging changes of domain selector: " + ds.toString());
 				SwingUtilities.invokeLater(new Runnable() {
 					@Override
 					public void run() {
 						tableModel.updateRow(selectedRowIndex, ds);
 					}
 				});
 			}
 		});
 		// Display domain selector dialog
 		SwingUtilities.invokeLater(new Runnable() {
 			@Override
 			public void run() {
 				GuiUtils.centerOnScreen(domainSelectorDialog);
 				domainSelectorDialog.setVisible(true);
 			}
 		});
 	}
 
 	private void deleteSelectedBtnActionPerformed(ActionEvent evt) {
 		final int selectedRowIndex = domainTable.getSelectedRow();
 
 		if (selectedRowIndex == NO_ROW_SELECTED) {
 			return;
 		}
 
 		SwingUtilities.invokeLater(new Runnable() {
 			@Override
 			public void run() {
 				LOG.info("Deleting selected domain selector.");
 				FormatDomainTableModel tableModel = (FormatDomainTableModel) domainTable
 						.getModel();
 				tableModel.deleteRow(selectedRowIndex);
 			}
 		});
 	}
 
 	private JButton getSaveBtn() {
 		if (saveBtn == null) {
 			saveBtn = new JButton();
 			saveBtn.setName("saveBtn");
 			saveBtn.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent evt) {
 					saveBtnActionPerformed(evt);
 				}
 			});
 		}
 		return saveBtn;
 	}
 
 	private JButton getDeleteBtn() {
 		if (deleteBtn == null) {
 			deleteBtn = new JButton();
 			deleteBtn.setName("deleteBtn");
 			deleteBtn.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent evt) {
 					deleteBtnActionPerformed(evt);
 				}
 			});
 		}
 		return deleteBtn;
 	}
 
 	private JButton getCloseBtn() {
 		if (closeBtn == null) {
 			closeBtn = new JButton();
 			closeBtn.setName("closeBtn");
 			closeBtn.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent evt) {
 					closeBtnActionPerformed(evt);
 				}
 			});
 		}
 		return closeBtn;
 	}
 
 	private void saveBtnActionPerformed(ActionEvent evt) {
 		doSave();
 	}
 
 	private void deleteBtnActionPerformed(ActionEvent evt) {
 		doDelete();
 	}
 
 	private void closeBtnActionPerformed(ActionEvent evt) {
 		formatItemNode.closeTab();
 	}
 
 	private boolean validateForm() {
 		String formatName = formatNameText.getText();
 
 		String validateMessage = FresnelUtils.validateResourceUri(formatName,
 				ContextHolder.getInstance().getFresnelRepositoryDao());
 		if (validateMessage != null) {
 			new MessageDialog(GuiUtils.getOwnerFrame(this),
 					"Invalid Fresnel Format URI", "The Format URI '"
 							+ formatName + "' is not valid:<br>"
 							+ validateMessage).setVisible(true);
 		}
 
 		return validateMessage == null;
 	}
 
 	private JComboBox getValueDisplayFormat() {
 		if (valueDisplayFormat == null) {
 			ValueDisplayFormat[] formats = {
 					new ValueDisplayFormat(FormatValueType.NOT_SPECIFIED,
 							"Default (not specified)"),
 					new ValueDisplayFormat(FormatValueType.EXTERNAL_LINK),
 					new ValueDisplayFormat(FormatValueType.IMAGE),
 					new ValueDisplayFormat(FormatValueType.URI),
 					new ValueDisplayFormat(FormatValueType.NONE),
 					new ValueDisplayFormat(FormatValueType.VIDEO,
 							"fresnel:video (extended)"),
 					new ValueDisplayFormat(FormatValueType.AUDIO,
 							"fresnel:audio (extended)"),
 					new ValueDisplayFormat(FormatValueType.ANIMATION,
 							"fresnel:animation (extended)"),
 					new ValueDisplayFormat(FormatValueType.HTML,
 							"fresnel:html (extended)"),
 					new ValueDisplayFormat(FormatValueType.TEXT,
 							"fresnel:text (extended)"),
 					new ValueDisplayFormat(FormatValueType.TEXTSTREAM,
 							"fresnel:textstream (extended)") };
 			ComboBoxModel valueDisplayFormatModel = new DefaultComboBoxModel(
 					formats);
 			valueDisplayFormat = new JComboBox();
 			valueDisplayFormat.setModel(valueDisplayFormatModel);
 			valueDisplayFormat
 					.setPreferredSize(new java.awt.Dimension(223, 23));
 		}
 		return valueDisplayFormat;
 	}
 
 	private JButton getBtnPreview() {
 		if (btnPreview == null) {
 			btnPreview = new JButton();
 			btnPreview.setName("btnPreview");
 			btnPreview.setSize(83, 23);
 			btnPreview.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent evt) {
 					btnPreviewActionPerformed(evt);
 				}
 			});
 		}
 		return btnPreview;
 	}
 
 	private void btnPreviewActionPerformed(ActionEvent evt) {
 		// Export currently edited format to be displayed
 		FormatModelManager modelManager = new FormatModelManager();
 		SesameFormat format = modelManager
 				.convertModel2JFresnel(saveFormatModel());
 
 		// Show preview dialog for setting preview parameters
 		PreviewDialog previewDialog = new FormatPreviewDialog(
 				GuiUtils.getOwnerFrame(this), true,
 				PreviewDialog.PREVIEW_FORMAT, null, format);
 		GuiUtils.centerOnScreen(previewDialog);
 		previewDialog.setVisible(true);
 	}
 
 	private ContentSelectorJPanel getValueNoValueContentSelector() {
 		if (valueNoValueContentSelector == null) {
 			valueNoValueContentSelector = new ContentSelectorJPanel();
 		}
 		return valueNoValueContentSelector;
 	}
 
 	private JRadioButton getRadioPurposeDefault() {
 		if (radioPurposeDefault == null) {
 			radioPurposeDefault = new JRadioButton();
 			radioPurposeDefault.setName("radioPurposeDefault");
 			getButtonGroupPurpose().add(radioPurposeDefault);
 		}
 		return radioPurposeDefault;
 	}
 
 	private JRadioButton getRadioPurposeScreen() {
 		if (radioPurposeScreen == null) {
 			radioPurposeScreen = new JRadioButton();
 			radioPurposeScreen.setName("radioPurposeScreen");
 			getButtonGroupPurpose().add(radioPurposeScreen);
 		}
 		return radioPurposeScreen;
 	}
 
 	private JRadioButton getRadioPurposeProjection() {
 		if (radioPurposeProjection == null) {
 			radioPurposeProjection = new JRadioButton();
 			radioPurposeProjection.setName("radioPurposeProjection");
 			getButtonGroupPurpose().add(radioPurposeProjection);
 		}
 		return radioPurposeProjection;
 	}
 
 	private JRadioButton getRadioPurposePrint() {
 		if (radioPurposePrint == null) {
 			radioPurposePrint = new JRadioButton();
 			radioPurposePrint.setName("radioPurposePrint");
 			getButtonGroupPurpose().add(radioPurposePrint);
 		}
 		return radioPurposePrint;
 	}
 
 	private ButtonGroup getButtonGroupPurpose() {
 		if (buttonGroupPurpose == null) {
 			buttonGroupPurpose = new ButtonGroup();
 		}
 		return buttonGroupPurpose;
 	}
 
 	private JPanel getJPanel1() {
 		if (purposePanel == null) {
 			purposePanel = new JPanel();
 			GroupLayout gl_purposePanel = new GroupLayout(
 					(JComponent) purposePanel);
 			gl_purposePanel.setHorizontalGroup(
 				gl_purposePanel.createParallelGroup(Alignment.LEADING)
 					.addGroup(gl_purposePanel.createSequentialGroup()
 						.addComponent(getRadioPurposePrint(), GroupLayout.PREFERRED_SIZE, 93, GroupLayout.PREFERRED_SIZE)
 						.addGap(0, 244, Short.MAX_VALUE))
 					.addGroup(gl_purposePanel.createSequentialGroup()
 						.addComponent(getRadioPurposeProjection())
 						.addGap(0, 244, Short.MAX_VALUE))
 					.addGroup(gl_purposePanel.createSequentialGroup()
 						.addComponent(getRadioPurposeScreen(), GroupLayout.PREFERRED_SIZE, 93, GroupLayout.PREFERRED_SIZE)
 						.addGap(0, 244, Short.MAX_VALUE))
 					.addComponent(getRadioPurposeDefault(), 0, 337, Short.MAX_VALUE)
 			);
 			gl_purposePanel.setVerticalGroup(
 				gl_purposePanel.createParallelGroup(Alignment.LEADING)
 					.addGroup(gl_purposePanel.createSequentialGroup()
 						.addComponent(getRadioPurposeDefault(), GroupLayout.PREFERRED_SIZE, 20, GroupLayout.PREFERRED_SIZE)
 						.addPreferredGap(ComponentPlacement.RELATED)
 						.addComponent(getRadioPurposeScreen(), GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
 						.addPreferredGap(ComponentPlacement.RELATED)
 						.addComponent(getRadioPurposeProjection(), GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
 						.addPreferredGap(ComponentPlacement.RELATED)
 						.addComponent(getRadioPurposePrint(), GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
 						.addContainerGap(13, Short.MAX_VALUE))
 			);
 			purposePanel.setLayout(gl_purposePanel);
 			purposePanel.setBorder(BorderFactory
 					.createTitledBorder("Purpose (extended)"));
 		}
 		return purposePanel;
 	}
 
 	private JPanel getAssociatedGroupsPanel() {
 		if (associatedGroupsPanel == null) {
 			associatedGroupsPanel = new JPanel();
 			GroupLayout gl_associatedGroupsPanel = new GroupLayout(
 					(JComponent) associatedGroupsPanel);
 			associatedGroupsPanel.setLayout(gl_associatedGroupsPanel);
 			associatedGroupsPanel.setBorder(BorderFactory
 					.createTitledBorder("Associated groups"));
 			gl_associatedGroupsPanel
 					.setHorizontalGroup(gl_associatedGroupsPanel
 							.createSequentialGroup()
 							.addContainerGap()
 							.addComponent(getListAssociatedGroups(), 0, 118,
 									Short.MAX_VALUE).addContainerGap());
 			gl_associatedGroupsPanel
 					.setVerticalGroup(gl_associatedGroupsPanel
 							.createSequentialGroup()
 							.addComponent(getListAssociatedGroups(), 0, 141,
 									Short.MAX_VALUE).addContainerGap());
 		}
 		return associatedGroupsPanel;
 	}
 
 	private JList getListAssociatedGroups() {
 		if (listAssociatedGroups == null) {
 			ListModel listAssociatedGroupsModel = new DefaultComboBoxModel(
 					new String[] {});
 			listAssociatedGroups = new JList();
 			listAssociatedGroups.setModel(listAssociatedGroupsModel);
 			listAssociatedGroups.setBorder(new LineBorder(new java.awt.Color(0,
 					0, 0), 1, false));
 			listAssociatedGroups
 					.addListSelectionListener(new ListSelectionListener() {
 						public void valueChanged(ListSelectionEvent evt) {
 							listAssociatedGroupsValueChanged(evt);
 						}
 					});
 		}
 		return listAssociatedGroups;
 	}
 
 	private JPanel getStyleSheetOverview() {
 		if (styleSheetOverview == null) {
 			styleSheetOverview = new JPanel();
 			GroupLayout gl_styleSheetOverview = new GroupLayout(
 					(JComponent) styleSheetOverview);
 			gl_styleSheetOverview.setHorizontalGroup(
 				gl_styleSheetOverview.createParallelGroup(Alignment.LEADING)
 					.addGroup(gl_styleSheetOverview.createSequentialGroup()
 						.addContainerGap()
 						.addGroup(gl_styleSheetOverview.createParallelGroup(Alignment.LEADING)
 							.addGroup(gl_styleSheetOverview.createSequentialGroup()
 								.addComponent(getLblCSSFilename())
 								.addPreferredGap(ComponentPlacement.UNRELATED)
 								.addComponent(getTxtCSSFilename(), GroupLayout.DEFAULT_SIZE, 228, Short.MAX_VALUE))
 							.addComponent(getJScrollPane1(), 0, 325, Short.MAX_VALUE))
 						.addContainerGap())
 			);
 			gl_styleSheetOverview.setVerticalGroup(
 				gl_styleSheetOverview.createParallelGroup(Alignment.LEADING)
 					.addGroup(gl_styleSheetOverview.createSequentialGroup()
 						.addGroup(gl_styleSheetOverview.createParallelGroup(Alignment.LEADING)
 							.addComponent(getLblCSSFilename(), GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
 							.addComponent(getTxtCSSFilename(), GroupLayout.PREFERRED_SIZE, 16, GroupLayout.PREFERRED_SIZE))
 						.addPreferredGap(ComponentPlacement.RELATED)
 						.addComponent(getJScrollPane1(), 0, 225, Short.MAX_VALUE)
 						.addContainerGap())
 			);
 			styleSheetOverview.setLayout(gl_styleSheetOverview);
 			styleSheetOverview.setBorder(BorderFactory
 					.createTitledBorder("CSS Style Sheet Overview"));
 		}
 		return styleSheetOverview;
 	}
 
 	private JTextArea getTxtStyleSheetOverview() {
 		if (txtStyleSheetOverview == null) {
 			txtStyleSheetOverview = new JTextArea();
 			txtStyleSheetOverview
 					.setFont(new java.awt.Font("Monospaced", 0, 12));
 		}
 		return txtStyleSheetOverview;
 	}
 
 	private void listAssociatedGroupsValueChanged(ListSelectionEvent evt) {
 		String groupURI = listAssociatedGroups.getSelectedValue().toString();
 
 		FresnelRepositoryDao fresnelDao = ContextHolder.getInstance()
 				.getFresnelRepositoryDao();
 		String prefixes = SparqlUtils.getSparqlQueryPrefixes(fresnelDao
 				.getRepository());
 		List<Value> result = fresnelDao.execTupleQuery(prefixes
 				+ "SELECT ?styleSheet WHERE { <" + groupURI
 				+ "> a fresnel:Group . " + "<" + groupURI
 				+ "> fresnel:stylesheetLink ?styleSheet .}",
 				QueryLanguage.SPARQL, "styleSheet");
 		String stylesheetURL;
 		if (result.size() == 0) {
 			stylesheetURL = FresnelEditorConstants.DEFAULT_CSS_STYLESHEET_URL;
 		} else {
 			stylesheetURL = result.get(0).toString();
 		}
 
 		String stylesheetText = "";
 		TextFileLoader fileLoader;
 		try {
 			fileLoader = new TextFileLoader(stylesheetURL);
 			stylesheetText = fileLoader.deserializeString();
 			txtCSSFilename.setText(fileLoader.getName());
 		} catch (IOException e) {
 			try {
 				fileLoader = new TextFileLoader(
 						FresnelEditorConstants.DEFAULT_CSS_STYLESHEET_URL);
 				stylesheetText = fileLoader.deserializeString();
 				txtCSSFilename.setText(fileLoader.getName());
 			} catch (IOException e1) {
 				// TODO Auto-generated catch block
 				txtCSSFilename.setText("Error while loading stylesheet!");
 				e1.printStackTrace();
 			}
 
 		}
 
 		txtStyleSheetOverview.setText(stylesheetText);
 	}
 
 	private JLabel getLblCSSFilename() {
 		if (lblCSSFilename == null) {
 			lblCSSFilename = new JLabel();
 			lblCSSFilename.setName("lblCSSFilename");
 		}
 		return lblCSSFilename;
 	}
 
 	private JLabel getTxtCSSFilename() {
 		if (txtCSSFilename == null) {
 			txtCSSFilename = new JLabel();
 		}
 		return txtCSSFilename;
 	}
 
 	private JScrollPane getJScrollPane1() {
 		if (scrollPaneStyleSheetOverview == null) {
 			scrollPaneStyleSheetOverview = new JScrollPane();
 			scrollPaneStyleSheetOverview
 					.setViewportView(getTxtStyleSheetOverview());
 		}
 		return scrollPaneStyleSheetOverview;
 	}
 
 }
