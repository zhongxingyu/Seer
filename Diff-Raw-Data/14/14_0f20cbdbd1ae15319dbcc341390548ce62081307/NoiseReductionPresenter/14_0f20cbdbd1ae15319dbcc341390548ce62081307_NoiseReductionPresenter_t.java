 package org.esa.beam.chris.ui;
 
 import org.esa.beam.dataio.chris.ChrisConstants;
 import org.esa.beam.dataio.chris.ChrisProductReaderPlugIn;
 import org.esa.beam.framework.dataio.ProductIO;
 import org.esa.beam.framework.datamodel.GeoPos;
 import org.esa.beam.framework.datamodel.MetadataElement;
 import org.esa.beam.framework.datamodel.Product;
 import org.esa.beam.framework.ui.ModalDialog;
 import org.esa.beam.util.SystemUtils;
 import org.esa.beam.util.io.BeamFileChooser;
 import org.esa.beam.util.io.BeamFileFilter;
 import org.esa.beam.visat.VisatApp;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.DefaultListSelectionModel;
 import javax.swing.JOptionPane;
 import javax.swing.ListSelectionModel;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.table.DefaultTableModel;
 import java.awt.Component;
 import java.awt.Window;
 import java.awt.event.ActionEvent;
 import java.io.File;
 import java.io.IOException;
 import java.util.Vector;
 
 /**
  * Created by Marco Peters.
  *
  * @author Marco Peters
  * @version $Revision:$ $Date:$
  */
 class NoiseReductionPresenter {
 
     private DefaultTableModel productsTableModel;
     private ListSelectionModel productsTableSelectionModel;
 
     private DefaultTableModel metadataTableModel;
 
     private Action addProductAction;
     private Action removeProductAction;
     private Action settingsAction;
 
     private Window window;
 
     private AdvancedSettingsPresenter settingsPresenter;
 
     public NoiseReductionPresenter(Product[] products, AdvancedSettingsPresenter settingsPresenter) {
         Object[][] productsData = new Object[products.length][2];
         if (products.length > 0) {
             for (int i = 0; i < productsData.length; i++) {
                 productsData[i][0] = Boolean.TRUE;
                 productsData[i][1] = products[i];
             }
         }
 
         productsTableModel = new DefaultTableModel(productsData, new String[]{"Output", "Product Name"});
         productsTableSelectionModel = new DefaultListSelectionModel();
         productsTableSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         productsTableSelectionModel.addListSelectionListener(new ListSelectionListener() {
             public void valueChanged(ListSelectionEvent e) {
                 updateMetadata();
             }
         });
 
         String[][] metadata = new String[5][2];
         metadata[0][0] = ChrisConstants.ATTR_NAME_CHRIS_MODE;
         metadata[1][0] = ChrisConstants.ATTR_NAME_TARGET_NAME;
         metadata[2][0] = "Target Coordinates";
         metadata[3][0] = ChrisConstants.ATTR_NAME_FLY_BY_ZENITH_ANGLE;
         metadata[4][0] = ChrisConstants.ATTR_NAME_MINIMUM_ZENITH_ANGLE;
         metadataTableModel = new DefaultTableModel(metadata, new String[]{"Name", "Value"});
 
         addProductAction = new AddProductAction(this);
         removeProductAction = new RemoveProductAction(this);
         settingsAction = new AdvancedSettingsAction(this);
 
         this.settingsPresenter = settingsPresenter;
         if (products.length > 0) {
             productsTableSelectionModel.setSelectionInterval(0, 0);
         }
 
     }
 
 
     public DefaultTableModel getProductsTableModel() {
         return productsTableModel;
     }
 
     public ListSelectionModel getProductsTableSelectionModel() {
         return productsTableSelectionModel;
     }
 
     public DefaultTableModel getMetadataTableModel() {
         return metadataTableModel;
     }
 
     public Action getAddProductAction() {
         return addProductAction;
     }
 
     public Action getRemoveProductAction() {
         return removeProductAction;
     }
 
     public Action getSettingsAction() {
         return settingsAction;
     }
 
     public AdvancedSettingsPresenter getSettingsPresenter() {
         return settingsPresenter;
     }
 
     public void setSettingsPresenter(AdvancedSettingsPresenter settingsPresenter) {
         this.settingsPresenter = settingsPresenter;
     }
 
     public void setWindow(Window window) {
         this.window = window;
     }
 
     public Window getWindow() {
         return window;
     }
 
     public Product[] getProducts() {
         Vector productVector = (Vector) getProductsTableModel().getDataVector();
         Product[] products = new Product[productVector.size()];
         for (int i = 0; i < productVector.size(); i++) {
             products[i] = (Product) ((Vector) productVector.elementAt(i)).get(1);
         }
         return products;
     }
 
     void setProductAsOutput(Product product, boolean output) {
         for (int i = 0; i < getProductsTableModel().getRowCount(); i++) {
             Product current = (Product) getProductsTableModel().getValueAt(i, 1);
             if (current.equals(product)) {
                 getProductsTableModel().setValueAt(output, i, 0);
                 return;
             }
         }
     }
 
     boolean isProductAsOutputSet(Product product) {
         for (int i = 0; i < getProductsTableModel().getRowCount(); i++) {
             Product current = (Product) getProductsTableModel().getValueAt(i, 1);
             if (current.equals(product)) {
                 Object isOutput = getProductsTableModel().getValueAt(i, 0);
                 if (isOutput != null) {
                     return (Boolean) isOutput;
                 }
             }
         }
         return false;
     }
 
 
     void addProduct(Product product) throws NRPValidationException {
         Product[] products = getProducts();
         if (products.length >= 5) {
             throw new NRPValidationException("Aqusition set already contains five products.");
         }
         if (products.length != 0 && !shouldConsiderProduct(products[0], product)) {
             throw new NRPValidationException("Product does not belong to the aqusition set.");
         }
         if (containsProduct(products, product)) {
             throw new NRPValidationException("Product is already defined in the aqusition set.");
         }
 
         if (!NoiseReductionAction.CHRIS_TYPES.contains(product.getProductType())) {
             StringBuilder sb = new StringBuilder(50);
             for (java.util.Iterator it = NoiseReductionAction.CHRIS_TYPES.iterator(); it.hasNext();) {
                 String type = (String) it.next();
                 sb.append("\"").append(type).append("\"");
                 if (it.hasNext()) {
                     sb.append(", ");
                 }
             }
             throw new NRPValidationException("Product type '" + product.getProductType() + "'is not valid .\n" +
                                              "Must be one of " + sb + "\n");
         }
         DefaultTableModel tableModel = getProductsTableModel();
         tableModel.addRow(new Object[]{Boolean.TRUE, product});
         updateSelection(tableModel.getRowCount() - 1);
     }
 
     void removeSelectedProduct() {
         getProductsTableModel().removeRow(getProductsTableSelectionModel().getLeadSelectionIndex());
         int newSelectionIndex = getProductsTableSelectionModel().getLeadSelectionIndex() - 1;
         updateSelection(newSelectionIndex);
     }
 
     private void updateSelection(int newSelectionIndex) {
         if (newSelectionIndex < getProductsTableModel().getRowCount() - 1) {
             newSelectionIndex++;
         }
         if (newSelectionIndex == -1) {
             getProductsTableSelectionModel().clearSelection();
         } else {
             getProductsTableSelectionModel().setSelectionInterval(newSelectionIndex, newSelectionIndex);
         }
     }
 
     private static boolean containsProduct(Product[] products, Product product) {
         for (Product aProduct : products) {
             if (aProduct.getName().equals(product.getName())) {
                 return true;
             }
         }
         return false;
     }
 
     static boolean shouldConsiderProduct(Product referenceProduct, Product product) {
         return product.getProductType().equals(referenceProduct.getProductType()) &&
                belongsToSameAquisitionSet(referenceProduct.getFileLocation(), product.getFileLocation());
     }
 
     static boolean belongsToSameAquisitionSet(File refernceProductFile, File currentProductFile) {
         if (refernceProductFile != null && currentProductFile != null) {
             String[] expectedParts = expectedParts = refernceProductFile.getName().split("_", 5);
             String[] actualParts = actualParts = currentProductFile.getName().split("_", 5);
            if(expectedParts.length == 5 && expectedParts.length == actualParts.length) {
             return expectedParts[0].equals(actualParts[0])
                    && expectedParts[1].equals(actualParts[1])
                    && expectedParts[2].equals(actualParts[2])
                    // actualParts[3] should be different
                    && expectedParts[4].equals(actualParts[4]);
            }
         }
        return false;
     }
 
 
     private static class AddProductAction extends AbstractAction {
 
         private static String LAST_OPEN_DIR_KEY = "chris.ui.file.lastOpenDir";
         private static String CHRIS_IMPORT_DIR_KEY = "user." + ChrisConstants.FORMAT_NAME.toLowerCase() + ".import.dir";
 
         private NoiseReductionPresenter presenter;
 
         public AddProductAction(NoiseReductionPresenter presenter) {
             super("Add...");
             this.presenter = presenter;
 
         }
 
         public void actionPerformed(ActionEvent e) {
             if (presenter.getProducts().length == 5) {
                 JOptionPane.showMessageDialog((Component) e.getSource(), "You cannot select more than five products.");
                 return;
             }
             BeamFileFilter fileFilter = new BeamFileFilter(ChrisConstants.FORMAT_NAME,
                                                            ChrisConstants.DEFAULT_FILE_EXTENSION,
                                                            new ChrisProductReaderPlugIn().getDescription(null));
             BeamFileChooser fileChooser = new BeamFileChooser();
             String lastDir = SystemUtils.getUserHomeDir().getPath();
             String chrisImportDir = VisatApp.getApp().getPreferences().getPropertyString(CHRIS_IMPORT_DIR_KEY,
                                                                                          SystemUtils.getUserHomeDir().getPath());
             lastDir = VisatApp.getApp().getPreferences().getPropertyString(LAST_OPEN_DIR_KEY, chrisImportDir);
             fileChooser.setMultiSelectionEnabled(true);
             fileChooser.setFileFilter(fileFilter);
             fileChooser.setCurrentDirectory(new File(lastDir));
 
             if (BeamFileChooser.APPROVE_OPTION == fileChooser.showOpenDialog(presenter.getWindow())) {
                 File[] selectedFiles = fileChooser.getSelectedFiles();
                 for (File file : selectedFiles) {
                     Product product = null;
                     try {
                         product = ProductIO.readProduct(file, null);
                     } catch (IOException e1) {
                         JOptionPane.showMessageDialog((Component) e.getSource(), e1.getMessage());
                     }
                     if (product != null) {
                         try {
                             presenter.addProduct(product);
                         } catch (NRPValidationException e1) {
                             JOptionPane.showMessageDialog((Component) e.getSource(),
                                                           "Cannot add product.\n" + e1.getMessage());
                         }
                     }
                 }
                 VisatApp.getApp().getPreferences().setPropertyString(LAST_OPEN_DIR_KEY,
                                                                      fileChooser.getCurrentDirectory().getPath());
             }
 
         }
     }
 
     private class RemoveProductAction extends AbstractAction {
 
         private NoiseReductionPresenter presenter;
 
         public RemoveProductAction(NoiseReductionPresenter presenter) {
             super("Remove");
             this.presenter = presenter;
         }
 
         public void actionPerformed(ActionEvent e) {
             presenter.removeSelectedProduct();
         }
     }
 
     private class AdvancedSettingsAction extends AbstractAction {
 
         private NoiseReductionPresenter presenter;
 
         public AdvancedSettingsAction(NoiseReductionPresenter presenter) {
             super("Advanced Settings...");
             this.presenter = presenter;
         }
 
         public void actionPerformed(ActionEvent e) {
             ModalDialog dialog = new ModalDialog(presenter.getWindow(),
                                                  "Optional Settings",
                                                  ModalDialog.ID_OK_CANCEL_HELP,
                                                  "chrisNoiseReductionAdvancedSettings");
             AdvancedSettingsPresenter settingsPresenter = presenter.getSettingsPresenter();
             AdvancedSettingsPresenter workingCopy = settingsPresenter.createCopy();
             dialog.setContent(new AdvancedSettingsPanel(workingCopy));
             if (dialog.show() == ModalDialog.ID_OK) {
                 presenter.setSettingsPresenter(workingCopy);
             }
         }
     }
 
 
     private void updateMetadata() {
         final String na = "Not available";
         Product selectedProduct = null;
         if (productsTableSelectionModel.getMaxSelectionIndex() == -1) {
             return;
         }
         selectedProduct = (Product) productsTableModel.getValueAt(productsTableSelectionModel.getMaxSelectionIndex(),
                                                                   1);
 
         MetadataElement root = selectedProduct.getMetadataRoot();
         if (root == null) {
             return;
         }
         MetadataElement mph = root.getElement(ChrisConstants.MPH_NAME);
         if (mph == null) {
             return;
         }
 
         metadataTableModel.setValueAt(mph.getAttributeString(ChrisConstants.ATTR_NAME_CHRIS_MODE, na), 0, 1);
         metadataTableModel.setValueAt(mph.getAttributeString(ChrisConstants.ATTR_NAME_TARGET_NAME, na), 1, 1);
         metadataTableModel.setValueAt(getTargetCoordinatesString(mph, na), 2, 1);
         metadataTableModel.setValueAt(mph.getAttributeString(ChrisConstants.ATTR_NAME_FLY_BY_ZENITH_ANGLE, na),
                                       3, 1);
         metadataTableModel.setValueAt(mph.getAttributeString(ChrisConstants.ATTR_NAME_MINIMUM_ZENITH_ANGLE, na), 4, 1);
 
 
         return;
     }
 
     private static String getTargetCoordinatesString(MetadataElement element, String defaultValue) {
         String str = defaultValue;
 
         String lat = element.getAttributeString(ChrisConstants.ATTR_NAME_TARGET_LAT, null);
         String lon = element.getAttributeString(ChrisConstants.ATTR_NAME_TARGET_LON, null);
 
         if (lat != null && lon != null) {
             try {
                 GeoPos geoPos = new GeoPos(Float.parseFloat(lat), Float.parseFloat(lon));
                 str = geoPos.getLatString() + ", " + geoPos.getLonString();
             } catch (NumberFormatException e) {
                 // ignore
             }
         }
 
         return str;
     }
 
 
 }
