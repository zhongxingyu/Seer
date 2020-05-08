 package org.esa.beam.chris.ui;
 
 import junit.framework.TestCase;
 import org.esa.beam.dataio.chris.ChrisConstants;
 import org.esa.beam.framework.datamodel.*;
 
 import javax.swing.*;
 import javax.swing.table.DefaultTableModel;
 import java.io.File;
 
 /**
  * Created by IntelliJ IDEA.
  *
  * @author Ralf Quast
  * @version $Revision$ $Date$
  */
 public class NoiseReductionPresenterTest extends TestCase {
 
     private String[] meatadatNames;
     private Product first;
     private Product second;
     private Product third;
     private Product[] expectedProducts;
 
     @Override
     protected void setUp() throws Exception {
         meatadatNames = new String[]{
                 ChrisConstants.ATTR_NAME_CHRIS_MODE,
                 ChrisConstants.ATTR_NAME_TARGET_NAME,
                 "Target Coordinates",
                 ChrisConstants.ATTR_NAME_FLY_BY_ZENITH_ANGLE,
                 ChrisConstants.ATTR_NAME_MINIMUM_ZENITH_ANGLE,
         };
 
         first = createChrisDummyProduct("first", "DummyMode1", "DummyTarget1");
         second = createChrisDummyProduct("second", "DummyMode2", "DummyTarget2");
         third = createChrisDummyProduct("third", "DummyMode3", "DummyTarget3");
 
         expectedProducts = new Product[]{first, second, third};
     }
 
     public void testConstuctor() {
         NoiseReductionPresenter nrp = new NoiseReductionPresenter(expectedProducts, new AdvancedSettingsPresenter());
 
         Product[] actualProducts = nrp.getDestripingFactorsSourceProducts();
         assertNotNull(actualProducts);
         assertEquals(3, actualProducts.length);
 
         for (int i = 0; i < actualProducts.length; i++) {
             assertSame(expectedProducts[i], actualProducts[i]);
         }
 
         int selectionIndex = nrp.getProductTableSelectionModel().getMaxSelectionIndex();
         assertEquals(0, selectionIndex);
         assertSame(first, nrp.getDestripingFactorsSourceProducts()[selectionIndex]);
 
         checkMetadata(nrp.getMetadataTableModel(), "DummyMode1", "DummyTarget1");
     }
 
     public void testConstructorWithoutProducts() {
         NoiseReductionPresenter nrp = new NoiseReductionPresenter(new Product[0], new AdvancedSettingsPresenter());
 
         DefaultTableModel metadata = nrp.getMetadataTableModel();
         assertEquals(5, metadata.getRowCount());
         for (int i = 0; i < metadata.getRowCount(); i++) {
             assertEquals(nrp.getMetadataTableModel().getValueAt(i, 1), null);
         }
     }
 
     public void testAddRemove() throws NoiseReductionValidationException {
         NoiseReductionPresenter nrp = new NoiseReductionPresenter(expectedProducts, new AdvancedSettingsPresenter());
 
         Product fourth = createChrisDummyProduct("fourth", "chris", "DummyTarget4");
         nrp.addProduct(fourth);
         assertEquals(4, nrp.getDestripingFactorsSourceProducts().length);
         assertSame(fourth, nrp.getDestripingFactorsSourceProducts()[3]);
         ListSelectionModel selectionModel = nrp.getProductTableSelectionModel();
         assertEquals(3, selectionModel.getMaxSelectionIndex());
 
         nrp.getProductTableSelectionModel().setSelectionInterval(2, 2);
 
         nrp.removeSelectedProduct();
         assertEquals(3, nrp.getDestripingFactorsSourceProducts().length);
         assertSame(first, nrp.getDestripingFactorsSourceProducts()[0]);
         assertSame(second, nrp.getDestripingFactorsSourceProducts()[1]);
         assertSame(fourth, nrp.getDestripingFactorsSourceProducts()[2]);
         assertEquals(2, nrp.getProductTableSelectionModel().getMaxSelectionIndex());
 
         nrp.removeSelectedProduct();
         assertEquals(2, nrp.getDestripingFactorsSourceProducts().length);
         assertSame(first, nrp.getDestripingFactorsSourceProducts()[0]);
         assertSame(second, nrp.getDestripingFactorsSourceProducts()[1]);
         assertEquals(1, nrp.getProductTableSelectionModel().getMaxSelectionIndex());
 
         nrp.removeSelectedProduct();
         assertEquals(0, nrp.getProductTableSelectionModel().getMaxSelectionIndex());
         assertSame(first, nrp.getDestripingFactorsSourceProducts()[0]);
 
         nrp.removeSelectedProduct();
         assertEquals(0, nrp.getDestripingFactorsSourceProducts().length);
         assertEquals(-1, nrp.getProductTableSelectionModel().getMaxSelectionIndex());
 
         nrp.addProduct(fourth);
         assertEquals(0, nrp.getProductTableSelectionModel().getMaxSelectionIndex());
     }
 
     public void testSelectionChange() {
         NoiseReductionPresenter nrp = new NoiseReductionPresenter(expectedProducts, new AdvancedSettingsPresenter());
         checkMetadata(nrp.getMetadataTableModel(), "DummyMode1", "DummyTarget1");
 
         nrp.getProductTableSelectionModel().setSelectionInterval(2, 2);
         checkMetadata(nrp.getMetadataTableModel(), "DummyMode3", "DummyTarget3");
 
         nrp.getProductTableSelectionModel().setSelectionInterval(1, 1);
         checkMetadata(nrp.getMetadataTableModel(), "DummyMode2", "DummyTarget2");
     }
 
     public void testProductAsOutput() {
         NoiseReductionPresenter nrp = new NoiseReductionPresenter(expectedProducts, new AdvancedSettingsPresenter());
 
         assertTrue(nrp.isChecked(first));
         assertTrue(nrp.isChecked(second));
         assertTrue(nrp.isChecked(third));
 
         nrp.setCheckedState(second, false);
 
         assertTrue(nrp.isChecked(first));
         assertFalse(nrp.isChecked(second));
         assertTrue(nrp.isChecked(third));
     }
 
     private static Product createChrisDummyProduct(String name, String mode, String targetName) {
        Product product = new Product(name, "CHRIS_M2", 1, 1);
         product.setFileLocation(new File("CHRIS_BR_123456_9876_21.hdf"));
         MetadataElement mphElement = new MetadataElement("MPH");
         mphElement.addAttribute(new MetadataAttribute(ChrisConstants.ATTR_NAME_CHRIS_MODE,
                                                       ProductData.createInstance(mode),
                                                       true));
         mphElement.addAttribute(new MetadataAttribute(ChrisConstants.ATTR_NAME_TARGET_NAME,
                                                       ProductData.createInstance(targetName),
                                                       true));
         mphElement.addAttribute(new MetadataAttribute(ChrisConstants.ATTR_NAME_TARGET_LAT,
                                                       ProductData.createInstance("45.32"),
                                                       true));
         mphElement.addAttribute(new MetadataAttribute(ChrisConstants.ATTR_NAME_TARGET_LON,
                                                       ProductData.createInstance("10.8"),
                                                       true));
         product.getMetadataRoot().addElement(mphElement);
         // leave ATTR_NAME_NOMINAL_FLY_BY_ZENITH_ANGLE and ATTR_NAME_MINIMUM_ZENITH_ANGLE as "Not available"
         return product;
     }
 
     private void checkMetadata(DefaultTableModel metaData, String mode, String target) {
         assertNotNull(metaData);
         assertEquals(5, metaData.getRowCount());
 
         for (int i = 0; i < meatadatNames.length; i++) {
             assertEquals(meatadatNames[i], metaData.getValueAt(i, 0));
         }
 
         assertEquals(2, metaData.getColumnCount());
         assertEquals(mode, metaData.getValueAt(0, 1));
         assertEquals(target, metaData.getValueAt(1, 1));
 
         GeoPos expectedGeoPos = new GeoPos(45.32f, 10.8f);
         String expectedGeoPosString = expectedGeoPos.getLatString() + ", " + expectedGeoPos.getLonString();
         assertEquals(expectedGeoPosString, metaData.getValueAt(2, 1));
 
         assertEquals("Not available", metaData.getValueAt(3, 1));
         assertEquals("Not available", metaData.getValueAt(4, 1));
     }
 
 }
