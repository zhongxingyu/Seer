 /**
  * 
  */
 package cz.cuni.mff.peckam.java.origamist.gui.editor;
 
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ComponentAdapter;
 import java.awt.event.ComponentEvent;
 import java.awt.event.ComponentListener;
 import java.awt.event.ContainerEvent;
 import java.awt.event.ContainerListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.File;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.security.Permission;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.ResourceBundle;
 
 import javax.swing.BorderFactory;
 import javax.swing.ButtonGroup;
 import javax.swing.ImageIcon;
 import javax.swing.InputVerifier;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComponent;
 import javax.swing.JDialog;
 import javax.swing.JFileChooser;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.JScrollPane;
 import javax.swing.JSpinner;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.KeyStroke;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.border.TitledBorder;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.DocumentEvent;
 import javax.swing.filechooser.FileNameExtensionFilter;
 import javax.swing.origamist.JFileInput;
 import javax.swing.origamist.JImage;
 import javax.swing.origamist.JLocalizedButton;
 import javax.swing.origamist.JLocalizedLabel;
 
 import com.jgoodies.forms.layout.CellConstraints;
 import com.jgoodies.forms.layout.FormLayout;
 import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;
 
 import cz.cuni.mff.peckam.java.origamist.configuration.Configuration;
 import cz.cuni.mff.peckam.java.origamist.gui.common.DefaultLangStringListTextArea;
 import cz.cuni.mff.peckam.java.origamist.gui.common.JDiagramPaperInput;
 import cz.cuni.mff.peckam.java.origamist.gui.common.JLangStringListTextField;
 import cz.cuni.mff.peckam.java.origamist.gui.common.JModelPaperInput;
 import cz.cuni.mff.peckam.java.origamist.model.ObjectFactory;
 import cz.cuni.mff.peckam.java.origamist.model.Origami;
 import cz.cuni.mff.peckam.java.origamist.model.UnitDimension;
 import cz.cuni.mff.peckam.java.origamist.model.jaxb.Unit;
 import cz.cuni.mff.peckam.java.origamist.services.ServiceLocator;
 import cz.cuni.mff.peckam.java.origamist.services.TooltipFactory;
 import cz.cuni.mff.peckam.java.origamist.services.interfaces.ConfigurationManager;
 import cz.cuni.mff.peckam.java.origamist.utils.PermissionConverter;
 import cz.cuni.mff.peckam.java.origamist.utils.UniversalDocumentListener;
 
 /**
  * A frame for editing metadata of an tempOrigami. It can also be used to fill metadata for a just-created tempOrigami.
  * 
  * @author Martin Pecka
  */
 public class OrigamiPropertiesFrame extends JDialog
 {
     /** */
     private static final long                      serialVersionUID = -8222843108896744313L;
 
     /**
      * If <code>null</code>, indicates that the dialog should display texts for creating a model metadata, otherwise it
      * displays texts for editing an existing model's metadata.
      */
     protected Origami                              origami;
 
     /**
      * The origami we will store the current settings in. These values will be copied to <code>origami</code> when this
      * dialog is closed with the {@link OKAction}.
      */
     protected Origami                              tempOrigami;
 
     /**
      * If <code>true</code>, show texts for creating a model, otherwise display texts for editing an existing model's
      * metadata.
      */
     protected boolean                              isCreating;
 
     /** Input for model names. */
     protected JLangStringListTextField<JTextField> name;
 
     /** Input for model introduction year. */
     protected JSpinner                             year;
 
     /** Input for short description. */
     protected JLangStringListTextField<JTextField> shortDesc;
 
     /** Input for long description. */
     protected JLangStringListTextField<JTextArea>  description;
 
     /** Input for author's name. */
     protected JTextField                           authorName;
 
     /** Input for author's homepage. */
     protected JTextField                           authorHomepage;
 
     /** Input for license name. */
     protected JTextField                           licenseName;
     /** Input for license homepage. */
     protected JTextField                           licenseHomepage;
     /** Input for license content. */
     protected JTextArea                            licenseContent;
     /** RadioButton that signalizes that license homepage will be filled and content not. */
     protected JRadioButton                         licenseChooseHomepage;
     /** RadioButton that signalizes that license homepage will not be filled and content will be. */
     protected JRadioButton                         licenseChooseContent;
     /** Checkbox that signalizes the Do nothing license permission. */
     protected JCheckBox                            licensePermissionDoNothing;
     /** Checkbox that signalizes the Edit license permission. */
     protected JCheckBox                            licensePermissionEdit;
     /** Checkbox that signalizes the Export license permission. */
     protected JCheckBox                            licensePermissionExport;
     /** Checkbox that signalizes the Distribute license permission. */
     protected JCheckBox                            licensePermissionDistribute;
     /** Input for the original's URL. */
     protected JTextField                           original;
     /** RadioButton that signalizes that the thumbnail should be generated from the model. */
     protected JRadioButton                         thumbnailLoadFromModel;
     /** RadioButton that signalizes that the thumbnail should be generated from a file. */
     protected JRadioButton                         thumbnailLoadFromFile;
     /** Input for selecting the thumbnail. */
     protected JFileInput                           thumbnailFileInput;
     /** Component that shows a preview of the thumbnail. */
     protected JImage                               thumbnailPreview;
     /** Input for configuring diagram paper. */
     protected JDiagramPaperInput                   diagramPaper;
     /** Input for configuring model paper. */
     protected JModelPaperInput                     modelPaper;
 
     /** Dialog button. */
     protected JButton                              okButton, cancelButton;
 
     /** Label. */
     protected JLabel                               dialogTitle, nameLabel, yearLabel, shortDescLabel, authorNameLabel,
             authorHomepageLabel, licenseNameLabel, licenseHomepageLabel, licenseContentLabel, originalLabel,
             thumbnailFileInputLabel, thumbnailPreviewLabel;
 
     /**
      * @param tempOrigami If <code>null</code>, indicates that the dialog should display texts for creating a model
      *            metadata, otherwise it displays texts for editing an existing model's metadata.
      */
     public OrigamiPropertiesFrame(Origami origami)
     {
         setOrigami(origami);
 
         final ComponentListener resizeListener = new ComponentAdapter() {
             @Override
             public void componentResized(ComponentEvent e)
             {
                 maybePack();
             }
         };
 
         getContentPane().addContainerListener(new ContainerListener() {
             @Override
             public void componentRemoved(ContainerEvent e)
             {
                 e.getChild().removeComponentListener(resizeListener);
             }
 
             @Override
             public void componentAdded(ContainerEvent e)
             {
                 e.getChild().addComponentListener(resizeListener);
             }
         });
 
         createComponents();
         buildLayout();
 
         if (isCreating)
             fillDefaults();
 
         setModalityType(ModalityType.APPLICATION_MODAL);
 
         String titleKey = (isCreating ? "OrigamiPropertiesFrame.title.create" : "OrigamiPropertiesFrame.title.edit");
         ServiceLocator.get(ConfigurationManager.class).get()
                 .addAndRunResourceBundleListener(new Configuration.LocaleListener("application", titleKey) {
                     @Override
                     protected void updateText(String text)
                     {
                         setTitle(text);
                     }
                 });
     }
 
     /**
      * Create all GUI components.
      */
     protected void createComponents()
     {
         Configuration conf = ServiceLocator.get(ConfigurationManager.class).get();
 
         String titleKey = (isCreating ? "OrigamiPropertiesFrame.title.create" : "OrigamiPropertiesFrame.title.edit");
         dialogTitle = new JLocalizedLabel("application", titleKey);
         dialogTitle.setFont(dialogTitle.getFont().deriveFont(Font.BOLD, 20f));
 
         nameLabel = new JLocalizedLabel("application", "OrigamiPropertiesFrame.nameLabel");
         yearLabel = new JLocalizedLabel("application", "OrigamiPropertiesFrame.yearLabel");
         shortDescLabel = new JLocalizedLabel("application", "OrigamiPropertiesFrame.shortDescLabel");
         authorNameLabel = new JLocalizedLabel("application", "OrigamiPropertiesFrame.authorNameLabel");
         authorHomepageLabel = new JLocalizedLabel("application", "OrigamiPropertiesFrame.authorHomepageLabel");
         licenseNameLabel = new JLocalizedLabel("application", "OrigamiPropertiesFrame.licenseNameLabel");
         licenseHomepageLabel = new JLocalizedLabel("application", "OrigamiPropertiesFrame.licenseHomepageLabel");
         licenseContentLabel = new JLocalizedLabel("application", "OrigamiPropertiesFrame.licenseContentLabel");
         originalLabel = new JLocalizedLabel("application", "OrigamiPropertiesFrame.originalLabel");
         thumbnailFileInputLabel = new JLocalizedLabel("application", "OrigamiPropertiesFrame.thumbnailFileInputLabel");
         thumbnailPreviewLabel = new JLocalizedLabel("application", "OrigamiPropertiesFrame.thumbnailPreviewLabel");
 
         name = new JLangStringListTextField<JTextField>(tempOrigami.getName(), new JTextField(20));
 
        // in creating mode, the 0 will be replaced in fillDefaults()
        year = new JSpinner(new SpinnerNumberModel(tempOrigami.getYear() != null ? tempOrigami.getYear().getYear() : 0,
                Integer.MIN_VALUE, new GregorianCalendar().get(Calendar.YEAR), 1));
         year.addChangeListener(new ChangeListener() {
             @Override
             public void stateChanged(ChangeEvent e)
             {
                 tempOrigami.setYear(new XMLGregorianCalendarImpl(new GregorianCalendar(Integer.parseInt(year.getValue()
                         .toString()), 1, 1)));
             }
         });
 
         shortDesc = new JLangStringListTextField<JTextField>(tempOrigami.getShortdesc(), new JTextField(20));
 
         description = new DefaultLangStringListTextArea(tempOrigami.getDescription());
         final TitledBorder descriptionBorder = BorderFactory.createTitledBorder(description.getBorder(), "");
         description.setBorder(descriptionBorder);
         conf.addAndRunResourceBundleListener(new Configuration.LocaleListener("application",
                 "OrigamiPropertiesFrame.descriptionLabel") {
             @Override
             protected void updateText(String text)
             {
                 descriptionBorder.setTitle(text);
             }
         });
 
         authorName = new JTextField(tempOrigami.getAuthor().getName(), 20);
         authorName.getDocument().addDocumentListener(new UniversalDocumentListener() {
             @Override
             protected void update(DocumentEvent e)
             {
                 tempOrigami.getAuthor().setName(authorName.getText());
             }
         });
 
         authorHomepage = new JTextField(tempOrigami.getAuthor().getHomepage() != null ? tempOrigami.getAuthor()
                 .getHomepage().toString() : "", 20);
         authorHomepage.setInputVerifier(new URIInputVerifier());
         authorHomepage.getDocument().addDocumentListener(new UniversalDocumentListener() {
             @Override
             protected void update(DocumentEvent e)
             {
                 URI value = null;
                 try {
                     value = new URL(authorHomepage.getText()).toURI();
                 } catch (MalformedURLException e1) {} catch (URISyntaxException e1) {}
                 tempOrigami.getAuthor().setHomepage(value);
             }
         });
 
         licenseName = new JTextField(tempOrigami.getLicense().getName(), 20);
         licenseName.getDocument().addDocumentListener(new UniversalDocumentListener() {
             @Override
             protected void update(DocumentEvent e)
             {
                 tempOrigami.getLicense().setName(licenseName.getText());
             }
         });
 
         licenseHomepage = new JTextField(tempOrigami.getLicense().getHomepage() != null ? tempOrigami.getLicense()
                 .getHomepage().toString() : "", 20);
         licenseHomepage.setInputVerifier(new URIInputVerifier());
         licenseHomepage.getDocument().addDocumentListener(new UniversalDocumentListener() {
             @Override
             protected void update(DocumentEvent e)
             {
                 URI value = null;
                 try {
                     value = new URL(licenseHomepage.getText()).toURI();
                 } catch (MalformedURLException e1) {} catch (URISyntaxException e1) {}
                 tempOrigami.getLicense().setHomepage(value);
             }
         });
 
         licenseContent = new JTextArea(tempOrigami.getLicense().getContent(), 5, 20);
         licenseContent.setLineWrap(true);
         licenseContent.setWrapStyleWord(true);
         licenseContent.getDocument().addDocumentListener(new UniversalDocumentListener() {
             @Override
             protected void update(DocumentEvent e)
             {
                 tempOrigami.getLicense().setContent(licenseContent.getText());
             }
         });
 
         ButtonGroup licenseChooseGroup = new ButtonGroup();
 
         licenseChooseHomepage = new JRadioButton();
         conf.addAndRunResourceBundleListener(new Configuration.LocaleListener("application",
                 "OrigamiPropertiesFrame.licenseChooseHomepageLabel") {
             @Override
             protected void updateText(String text)
             {
                 licenseChooseHomepage.setText(text);
             }
         });
         licenseChooseHomepage.addItemListener(new ItemListener() {
             @Override
             public void itemStateChanged(ItemEvent e)
             {
                 if (e.getStateChange() == ItemEvent.SELECTED) {
                     licenseHomepage.setVisible(true);
                     licenseContent.setVisible(false);
                     tempOrigami.getLicense().setContent(null);
                 }
             }
         });
         licenseChooseGroup.add(licenseChooseHomepage);
 
         licenseChooseContent = new JRadioButton();
         conf.addAndRunResourceBundleListener(new Configuration.LocaleListener("application",
                 "OrigamiPropertiesFrame.licenseChooseContentLabel") {
             @Override
             protected void updateText(String text)
             {
                 licenseChooseContent.setText(text);
             }
         });
         licenseChooseContent.addItemListener(new ItemListener() {
             @Override
             public void itemStateChanged(ItemEvent e)
             {
                 if (e.getStateChange() == ItemEvent.SELECTED) {
                     licenseHomepage.setVisible(false);
                     licenseContent.setVisible(true);
                     tempOrigami.getLicense().setHomepage(null);
                 }
             }
         });
         licenseChooseGroup.add(licenseChooseContent);
 
         if (tempOrigami.getLicense().getHomepage() != null)
             licenseChooseHomepage.setSelected(true);
         else
             licenseChooseContent.setSelected(true);
 
         List<String> selectedPermissions = new LinkedList<String>();
         for (Permission p : tempOrigami.getLicense().getPermission())
             selectedPermissions.add(p.getName());
 
         licensePermissionDoNothing = new JCheckBox();
         licensePermissionDoNothing.setSelected(selectedPermissions.contains("doNothing"));
         conf.addAndRunResourceBundleListener(new Configuration.LocaleListener("application", "permission.doNothing") {
             @Override
             protected void updateText(String text)
             {
                 licensePermissionDoNothing.setText(text);
             }
         });
         licensePermissionDoNothing.addItemListener(new ItemListener() {
             @Override
             public void itemStateChanged(ItemEvent e)
             {
                 if (e.getStateChange() == ItemEvent.SELECTED) {
                     if (licensePermissionEdit.isSelected() == licensePermissionDoNothing.isSelected())
                         licensePermissionEdit.setSelected(!licensePermissionDoNothing.isSelected());
                     if (licensePermissionExport.isSelected() == licensePermissionDoNothing.isSelected())
                         licensePermissionExport.setSelected(!licensePermissionDoNothing.isSelected());
                     if (licensePermissionDistribute.isSelected() == licensePermissionDoNothing.isSelected())
                         licensePermissionDistribute.setSelected(!licensePermissionDoNothing.isSelected());
                 }
 
                 Permission perm = PermissionConverter.parse("doNothing");
                 List<Permission> perms = tempOrigami.getLicense().getPermission();
                 if (licensePermissionDoNothing.isSelected()) {
                     if (!perms.contains(perm))
                         perms.add(perm);
                 } else {
                     perms.remove(perm);
                 }
             }
         });
 
         licensePermissionEdit = new JCheckBox();
         licensePermissionEdit.setSelected(selectedPermissions.contains("edit"));
         conf.addAndRunResourceBundleListener(new Configuration.LocaleListener("application", "permission.edit") {
             @Override
             protected void updateText(String text)
             {
                 licensePermissionEdit.setText(text);
             }
         });
         licensePermissionEdit.addItemListener(new ItemListener() {
             @Override
             public void itemStateChanged(ItemEvent e)
             {
                 if (e.getStateChange() == ItemEvent.SELECTED) {
                     if (licensePermissionEdit.isSelected() == licensePermissionDoNothing.isSelected())
                         licensePermissionDoNothing.setSelected(!licensePermissionEdit.isSelected());
                 }
 
                 Permission perm = PermissionConverter.parse("edit");
                 List<Permission> perms = tempOrigami.getLicense().getPermission();
                 if (licensePermissionEdit.isSelected()) {
                     if (!perms.contains(perm))
                         perms.add(perm);
                 } else {
                     perms.remove(perm);
                 }
             }
         });
 
         licensePermissionExport = new JCheckBox();
         licensePermissionExport.setSelected(selectedPermissions.contains("export"));
         conf.addAndRunResourceBundleListener(new Configuration.LocaleListener("application", "permission.export") {
             @Override
             protected void updateText(String text)
             {
                 licensePermissionExport.setText(text);
             }
         });
         licensePermissionExport.addItemListener(new ItemListener() {
             @Override
             public void itemStateChanged(ItemEvent e)
             {
                 if (e.getStateChange() == ItemEvent.SELECTED) {
                     if (licensePermissionExport.isSelected() == licensePermissionDoNothing.isSelected())
                         licensePermissionDoNothing.setSelected(!licensePermissionExport.isSelected());
                 }
 
                 Permission perm = PermissionConverter.parse("export");
                 List<Permission> perms = tempOrigami.getLicense().getPermission();
                 if (licensePermissionExport.isSelected()) {
                     if (!perms.contains(perm))
                         perms.add(perm);
                 } else {
                     perms.remove(perm);
                 }
             }
         });
 
         licensePermissionDistribute = new JCheckBox();
         licensePermissionDistribute.setSelected(selectedPermissions.contains("distribute"));
         conf.addAndRunResourceBundleListener(new Configuration.LocaleListener("application", "permission.distribute") {
             @Override
             protected void updateText(String text)
             {
                 licensePermissionDistribute.setText(text);
             }
         });
         licensePermissionDistribute.addItemListener(new ItemListener() {
             @Override
             public void itemStateChanged(ItemEvent e)
             {
                 if (e.getStateChange() == ItemEvent.SELECTED) {
                     if (licensePermissionDistribute.isSelected() == licensePermissionDoNothing.isSelected())
                         licensePermissionDoNothing.setSelected(!licensePermissionDistribute.isSelected());
                 }
 
                 Permission perm = PermissionConverter.parse("distribute");
                 List<Permission> perms = tempOrigami.getLicense().getPermission();
                 if (licensePermissionDistribute.isSelected()) {
                     if (!perms.contains(perm))
                         perms.add(perm);
                 } else {
                     perms.remove(perm);
                 }
             }
         });
 
         original = new JTextField(tempOrigami.getOriginal() != null ? tempOrigami.getOriginal().toString() : "", 20);
         original.setInputVerifier(new URIInputVerifier());
         original.getDocument().addDocumentListener(new UniversalDocumentListener() {
             @Override
             protected void update(DocumentEvent e)
             {
                 URI value = null;
                 try {
                     value = new URL(original.getText()).toURI();
                 } catch (MalformedURLException e1) {} catch (URISyntaxException e1) {}
                 tempOrigami.setOriginal(value);
             }
         });
 
         ButtonGroup thumbnailChooseGroup = new ButtonGroup();
 
         thumbnailLoadFromModel = new JRadioButton();
         conf.addAndRunResourceBundleListener(new Configuration.LocaleListener("application",
                 "OrigamiPropertiesFrame.thumbnailLoadFromModelLabel") {
             @Override
             protected void updateText(String text)
             {
                 thumbnailLoadFromModel.setText(text);
             }
         });
         thumbnailLoadFromModel.addItemListener(new ItemListener() {
             @Override
             public void itemStateChanged(ItemEvent e)
             {
                 if (e.getStateChange() == ItemEvent.SELECTED) {
                     thumbnailFileInput.setEnabled(false);
                     // thumbnailPreview.setVisible(false); // TODO maybe some better behavior
                     // tempOrigami.getThumbnail().getImage().setImageIcon(null);
                 }
             }
         });
         thumbnailChooseGroup.add(thumbnailLoadFromModel);
 
         thumbnailLoadFromFile = new JRadioButton();
         conf.addAndRunResourceBundleListener(new Configuration.LocaleListener("application",
                 "OrigamiPropertiesFrame.thumbnailLoadFromFileLabel") {
             @Override
             protected void updateText(String text)
             {
                 thumbnailLoadFromFile.setText(text);
             }
         });
         thumbnailLoadFromFile.addItemListener(new ItemListener() {
             @Override
             public void itemStateChanged(ItemEvent e)
             {
                 if (e.getStateChange() == ItemEvent.SELECTED) {
                     thumbnailFileInput.setEnabled(true);
                     thumbnailPreview.setVisible(true);
                 }
             }
         });
         thumbnailChooseGroup.add(thumbnailLoadFromFile);
 
         thumbnailFileInput = new JFileInput(ServiceLocator.get(ConfigurationManager.class).get().getLastOpenPath()
                 .toString(), 20);
         thumbnailFileInput.getFileChooser().setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
         conf.addAndRunResourceBundleListener(new Configuration.LocaleListener("application", "fileFilter.images") {
             @Override
             protected void updateText(String text)
             {
                 thumbnailFileInput.getFileChooser().setFileFilter(
                         new FileNameExtensionFilter(text, "BMP", "JPG", "PNG", "GIF"));
             }
         });
         thumbnailFileInput.getFileChooser().setDialogType(JFileChooser.OPEN_DIALOG);
         conf.addAndRunResourceBundleListener(new Configuration.LocaleListener("application",
                 "OrigamiPropertiesFrame.thumbnailFileDialog.approve.text") {
             @Override
             protected void updateText(String text)
             {
                 String mnemonic = bundle.getString("OrigamiPropertiesFrame.thumbnailFileDialog.approve.mnemonic");
                 String tooltip = bundle.getString("OrigamiPropertiesFrame.thumbnailFileDialog.approve.tooltip");
 
                 thumbnailFileInput.getFileChooser().setApproveButtonText(text);
                 thumbnailFileInput.getFileChooser().setApproveButtonMnemonic(
                         KeyStroke.getKeyStroke(mnemonic).getKeyCode());
                 thumbnailFileInput.getFileChooser().setApproveButtonToolTipText(
                         ServiceLocator.get(TooltipFactory.class).getDecorated(tooltip, text, "open-file-32.png",
                                 KeyStroke.getKeyStroke("alt " + mnemonic)));
             }
         });
         thumbnailFileInput.getTextField().getDocument().addDocumentListener(new UniversalDocumentListener() {
             @Override
             protected void update(DocumentEvent e)
             {
                 File selected = thumbnailFileInput.getFileChooser().getSelectedFile();
                 ServiceLocator.get(ConfigurationManager.class).get().setLastOpenPath(selected);
 
                 tempOrigami.getThumbnail().getImage()
                         .setType(selected.toString().substring(selected.toString().lastIndexOf('.') + 1));
                 tempOrigami.getThumbnail().getImage().setImageIcon(new ImageIcon(selected.toString()));
 
                 if (tempOrigami.getThumbnail().getImage().getImageIcon() != null)
                     thumbnailPreview.setImage(tempOrigami.getThumbnail().getImage().getImageIcon().getImage());
                 else
                     thumbnailPreview.setImage(null);
             }
         });
 
         thumbnailPreview = new JImage(tempOrigami.getThumbnail().getImage().getValue() != null ? tempOrigami
                 .getThumbnail().getImage().getImageIcon().getImage() : null);
 
         thumbnailLoadFromModel.setSelected(true);
 
         diagramPaper = new JDiagramPaperInput(tempOrigami.getPaper());
         final TitledBorder diagramPaperBorder = BorderFactory.createTitledBorder(diagramPaper.getBorder(), "");
         diagramPaper.setBorder(diagramPaperBorder);
         conf.addAndRunResourceBundleListener(new Configuration.LocaleListener("application",
                 "OrigamiPropertiesFrame.diagramPaperLabel") {
             @Override
             protected void updateText(String text)
             {
                 diagramPaperBorder.setTitle(text);
             }
         });
 
         modelPaper = new JModelPaperInput(tempOrigami.getModel().getPaper());
         if (!isCreating)
             // TODO maybe allow changes not affecting aspect ratio
             modelPaper.lockSize();
         final TitledBorder modelPaperBorder = BorderFactory.createTitledBorder(modelPaper.getBorder(), "");
         modelPaper.setBorder(modelPaperBorder);
         conf.addAndRunResourceBundleListener(new Configuration.LocaleListener("application",
                 "OrigamiPropertiesFrame.modelPaperLabel") {
             @Override
             protected void updateText(String text)
             {
                 modelPaperBorder.setTitle(text);
             }
         });
 
         String okKey = "OrigamiPropertiesFrame.okButton." + (isCreating ? "create" : "edit");
         okButton = new JLocalizedButton("application", okKey);
         okButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 if (verifyForm()) {
                     if (isCreating)
                         saveDefaults();
                     setVisible(false);
                 }
             }
         });
 
         cancelButton = new JLocalizedButton("application", "buttons.cancel");
         cancelButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 tempOrigami = null;
                 setVisible(false);
             }
         });
 
         addWindowListener(new WindowAdapter() {
             @Override
             public void windowClosing(WindowEvent e)
             {
                 tempOrigami = null;
             }
         });
     }
 
     /**
      * Add all components to a layout.
      */
     protected void buildLayout()
     {
         Configuration conf = ServiceLocator.get(ConfigurationManager.class).get();
 
         CellConstraints cc = new CellConstraints();
 
         JPanel basicPanel = new JPanel(new FormLayout("right:pref,$lcgap,pref:grow",
                 "pref,$lgap,pref,$lgap,pref,$lgap,pref,$lgap,pref,$lgap,pref"));
         final TitledBorder basicPanelBorder = BorderFactory.createTitledBorder("");
         basicPanel.setBorder(basicPanelBorder);
         conf.addAndRunResourceBundleListener(new Configuration.LocaleListener("application",
                 "OrigamiPropertiesFrame.basicPanelTitle") {
             @Override
             protected void updateText(String text)
             {
                 basicPanelBorder.setTitle(text);
             }
         });
 
         basicPanel.add(nameLabel, cc.xy(1, 1));
         basicPanel.add(name, cc.xy(3, 1));
         basicPanel.add(yearLabel, cc.xy(1, 3));
         basicPanel.add(year, cc.xy(3, 3));
         basicPanel.add(authorNameLabel, cc.xy(1, 5));
         basicPanel.add(authorName, cc.xy(3, 5));
         basicPanel.add(authorHomepageLabel, cc.xy(1, 7));
         basicPanel.add(authorHomepage, cc.xy(3, 7));
         basicPanel.add(shortDescLabel, cc.xy(1, 9));
         basicPanel.add(shortDesc, cc.xy(3, 9));
         basicPanel.add(originalLabel, cc.xy(1, 11));
         basicPanel.add(original, cc.xy(3, 11));
 
         JPanel thumbnailPanel = new JPanel(new FormLayout("center:min(pref;50dlu),$ugap,pref,$ugap,pref",
                 "pref,$lgap,pref"));
         final TitledBorder thumbnailPanelBorder = BorderFactory.createTitledBorder("");
         thumbnailPanel.setBorder(thumbnailPanelBorder);
         conf.addAndRunResourceBundleListener(new Configuration.LocaleListener("application",
                 "OrigamiPropertiesFrame.thumbnailPanelTitle") {
             @Override
             protected void updateText(String text)
             {
                 thumbnailPanelBorder.setTitle(text);
             }
         });
 
         thumbnailPanel.add(thumbnailPreviewLabel, cc.xy(1, 1));
         thumbnailPanel.add(thumbnailPreview, cc.xy(1, 3));
         thumbnailPanel.add(thumbnailLoadFromModel, cc.xy(3, 1));
         thumbnailPanel.add(thumbnailLoadFromFile, cc.xy(5, 1));
         thumbnailPanel.add(thumbnailFileInputLabel, cc.xy(3, 3));
         thumbnailPanel.add(thumbnailFileInput, cc.xy(5, 3));
 
         final JScrollPane licenseContentScrollPane = new JScrollPane(licenseContent,
                 JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
 
         JPanel licenseNamePanel = new JPanel(new FormLayout("pref,$lcgap,pref:grow", "pref"));
         licenseNamePanel.add(licenseNameLabel, cc.xy(1, 1));
         licenseNamePanel.add(licenseName, cc.xy(3, 1));
 
         licenseChooseContent.addItemListener(new ItemListener() {
             @Override
             public void itemStateChanged(ItemEvent e)
             {
                 if (e.getStateChange() == ItemEvent.SELECTED) {
                     licenseHomepageLabel.setVisible(false);
                     licenseContentLabel.setVisible(true);
                     licenseContentScrollPane.setVisible(true);
                 }
             }
         });
         licenseChooseHomepage.addItemListener(new ItemListener() {
             @Override
             public void itemStateChanged(ItemEvent e)
             {
                 if (e.getStateChange() == ItemEvent.SELECTED) {
                     licenseContentLabel.setVisible(false);
                     licenseHomepageLabel.setVisible(true);
                     licenseContentScrollPane.setVisible(false);
                 }
             }
         });
         licenseHomepageLabel.setVisible(licenseHomepage.isVisible());
         licenseContentLabel.setVisible(licenseContent.isVisible());
         licenseContentScrollPane.setVisible(licenseContent.isVisible());
 
         JPanel licenseContentPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
         licenseContentPanel.add(licenseChooseContent);
         licenseContentPanel.add(licenseChooseHomepage);
 
         JPanel licenseHomepagePanel = new JPanel(new FormLayout("pref,$lcgap,pref:grow", "pref"));
         licenseHomepagePanel.add(licenseHomepageLabel, cc.xy(1, 1));
         licenseHomepagePanel.add(licenseHomepage, cc.xy(3, 1));
 
         JPanel licensePermissionPanel = new JPanel(new GridLayout(0, 1));
         licensePermissionPanel.add(licensePermissionDoNothing);
         licensePermissionPanel.add(licensePermissionEdit);
         licensePermissionPanel.add(licensePermissionExport);
         licensePermissionPanel.add(licensePermissionDistribute);
 
         JPanel licensePanel = new JPanel(new FormLayout("pref:grow",
                 "pref,$lgap,pref,$lgap,pref,$rgap,pref,$lgap,pref,$lgap,pref"));
         final TitledBorder licensePanelBorder = BorderFactory.createTitledBorder("");
         licensePanel.setBorder(licensePanelBorder);
         conf.addAndRunResourceBundleListener(new Configuration.LocaleListener("application",
                 "OrigamiPropertiesFrame.licensePanelTitle") {
             @Override
             protected void updateText(String text)
             {
                 licensePanelBorder.setTitle(text);
             }
         });
 
         licensePanel.add(licenseNamePanel, cc.xy(1, 1));
         licensePanel.add(licenseContentPanel, cc.xy(1, 3));
         licensePanel.add(licenseContentLabel, cc.xy(1, 5));
         licensePanel.add(licenseContentScrollPane, cc.xy(1, 7));
         licensePanel.add(licenseHomepagePanel, cc.xy(1, 9));
         licensePanel.add(licensePermissionPanel, cc.xy(1, 11));
 
         JPanel leftPanel = new JPanel(new FormLayout("pref", "pref,$lgap,pref,$lgap,pref"));
         leftPanel.add(basicPanel, cc.xy(1, 1));
         leftPanel.add(thumbnailPanel, cc.xy(1, 3));
         leftPanel.add(licensePanel, cc.xy(1, 5));
 
         JPanel rightPanel = new JPanel(new FormLayout("pref", "pref,$lgap,pref,$lgap,pref"));
         rightPanel.add(description, cc.xy(1, 1));
         rightPanel.add(diagramPaper, cc.xy(1, 3));
         rightPanel.add(modelPaper, cc.xy(1, 5));
 
         JPanel dialogButtonsPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
         dialogButtonsPanel.add(cancelButton);
         dialogButtonsPanel.add(okButton);
 
         setLayout(new FormLayout("$dmargin,pref,$ugap,pref,$dmargin",
                 "$dmargin,pref,$lgap,top:pref,$lgap,bottom:pref:grow,$dmargin"));
 
         add(dialogTitle, cc.xyw(2, 2, 3));
         add(leftPanel, cc.xy(2, 4));
         add(rightPanel, cc.xy(4, 4));
         add(dialogButtonsPanel, cc.xyw(2, 6, 3));
 
         pack();
     }
 
     /**
      * @return the origami that was set to this frame (or a new one if <code>null</code> was specified) with the
      *         metadata copied from this dialog's inputs' values. <code>null</code> can be returned only if the user
      *         cancelled the dialog for creating a new origami.
      */
     public Origami getOrigami()
     {
         if (tempOrigami != null) {
             tempOrigami.setPaper(diagramPaper.getPaper());
             tempOrigami.getModel().setPaper(modelPaper.getPaper());
         }
 
         if (origami != null && tempOrigami != null) {
             origami.getMetadataFrom(tempOrigami);
             return origami;
         } else if (origami != null) {
             return origami;
         }
         return tempOrigami;
     }
 
     /**
      * @param origami The origami that should be edited by the dialog.
      */
     protected void setOrigami(Origami origami)
     {
         this.origami = origami;
 
         tempOrigami = (Origami) new ObjectFactory().createOrigami();
         tempOrigami.initStructure(false);
 
         if (origami != null) {
             tempOrigami.getMetadataFrom(origami);
         }
 
         isCreating = (origami == null);
     }
 
     /**
      * Where applicable, fills a default value. Empty default values are not filled.
      */
     protected void fillDefaults()
     {
         Configuration conf = ServiceLocator.get(ConfigurationManager.class).get();
 
         year.setValue(new GregorianCalendar().get(Calendar.YEAR));
 
         String authorName = conf.getDefaultAuthorName();
         this.authorName.setText(authorName);
 
         String authorHomepage = conf.getDefaultAuthorHomepage();
         this.authorHomepage.setText(authorHomepage);

        this.diagramPaper.selectFirstNonCustom();
        this.modelPaper.selectFirstNonCustom();

        pack();
     }
 
     /**
      * Save all values that can be further used as default values.
      */
     protected void saveDefaults()
     {
         Configuration conf = ServiceLocator.get(ConfigurationManager.class).get();
 
         conf.setDefaultAuthorName(this.authorName.getText());
         conf.setDefaultAuthorHomepage(this.authorHomepage.getText());
     }
 
     /**
      * Pack the window if it is smaller than it needs.
      */
     protected void maybePack()
     {
         Dimension layoutSize = getLayout().preferredLayoutSize(this);
         Dimension currentSize = getSize();
 
         if (layoutSize.width > currentSize.width || layoutSize.height > currentSize.height)
             pack();
     }
 
     /**
      * @return Whether all the fields are filled correctly and required fields are filled.
      */
     protected boolean verifyForm()
     {
         ResourceBundle messages = ResourceBundle.getBundle("application", ServiceLocator
                 .get(ConfigurationManager.class).get().getLocale());
         List<String> errors = new LinkedList<String>();
 
         if (name.getStrings().size() == 0)
             errors.add(messages.getString("OrigamiPropertiesFrame.errors.nameEmpty"));
         if (shortDesc.getStrings().size() == 0)
             errors.add(messages.getString("OrigamiPropertiesFrame.errors.shortDescEmpty"));
         if (authorName.getText().length() == 0)
             errors.add(messages.getString("OrigamiPropertiesFrame.errors.authorNameEmpty"));
         if (licenseName.getText().length() == 0)
             errors.add(messages.getString("OrigamiPropertiesFrame.errors.licenseNameEmpty"));
         if (licenseChooseHomepage.isSelected() && licenseHomepage.getText().length() == 0)
             errors.add(messages.getString("OrigamiPropertiesFrame.errors.licenseHomepageEmpty"));
         if (licenseChooseContent.isSelected() && licenseContent.getText().length() == 0)
             errors.add(messages.getString("OrigamiPropertiesFrame.errors.licenseContentEmpty"));
         if (thumbnailLoadFromFile.isSelected() && !new File(thumbnailFileInput.getTextField().getText()).exists())
             errors.add(messages.getString("OrigamiPropertiesFrame.errors.thumbnailFileNotExists"));
 
         UnitDimension dSize = diagramPaper.getPaper().getSize();
         if (dSize.getWidth() == 0d || dSize.getHeight() == 0d
                 || (dSize.getUnit() == Unit.REL && dSize.getReferenceLength() == 0d))
             errors.add(messages.getString("OrigamiPropertiesFrame.errors.diagramPaperSizeZero"));
 
         UnitDimension mSize = modelPaper.getPaper().getSize();
         if (mSize.getWidth() == 0d || mSize.getHeight() == 0d
                 || (mSize.getUnit() == Unit.REL && mSize.getReferenceLength() == 0d))
             errors.add(messages.getString("OrigamiPropertiesFrame.errors.modelPaperSizeZero"));
 
         if (!errors.isEmpty()) {
             StringBuilder errMessage = new StringBuilder();
             for (String err : errors) {
                 errMessage.append(err).append("\n");
             }
             JOptionPane.showMessageDialog(this, errMessage.toString(),
                     messages.getString("OrigamiPropertiesFrame.errors.title"), JOptionPane.ERROR_MESSAGE);
             return false;
         } else {
             return true;
         }
     }
 
     /**
      * Input verifier that verifies the content is a URI.
      * 
      * @author Martin Pecka
      */
     protected class URIInputVerifier extends InputVerifier
     {
         @Override
         public boolean verify(JComponent input)
         {
             if (!(input instanceof JTextField))
                 return false;
             JTextField field = (JTextField) input;
             if (field.getText().length() == 0)
                 return true;
             try {
                 new URL(field.getText()).toURI();
                 return true;
             } catch (MalformedURLException e) {
                 return false;
             } catch (URISyntaxException e) {
                 return false;
             }
         }
     }
 
 }
