 package gui.batches;
 
 import common.BarcodePdf;
 import common.command.AddItemCommand;
 import common.command.CommandManager;
 import gui.common.*;
 import gui.inventory.*;
 import gui.item.ItemData;
 import gui.product.*;
 import model.common.ModelFacade;
 import model.item.Item;
 import model.item.ItemVault;
 import model.product.Product;
 import model.product.ProductVault;
 import model.productcontainer.StorageUnit;
 import model.productcontainer.StorageUnitVault;
 import org.joda.time.DateTime;
 
 import java.awt.*;
 import java.io.File;
 import java.io.IOException;
 import java.util.*;
 
 /**
  * Controller class for the add item batch view.
  */
 public class AddItemBatchController extends Controller implements
         IAddItemBatchController {
 
     private HashMap<ProductData,ArrayList<ItemData>> _products = new HashMap<ProductData, ArrayList<ItemData>>();
     private boolean _scanner = true;
     private Timer _timer;
     private ProductContainerData _target;
     private ProductVault _productVault;
     private CommandManager _commandManager;
     
     /**
      * Constructor.
      * 
      * @param view Reference to the add item batch view.
      * @param _target Reference to the storage unit to which items are being added.
      */
     public AddItemBatchController(IView view, ProductContainerData target) {
         super(view);
         getView().setUseScanner(_scanner);
         getView().setCount("1");
         _timer = new Timer();
         _target = target;
         new ModelFacade();
         _productVault = ProductVault.getInstance();
         _commandManager = new CommandManager();
         construct();
     }
 
     /**
      * Returns a reference to the view for this controller.
      */
     @Override
     protected IAddItemBatchView getView() {
         return (IAddItemBatchView) super.getView();
     }
 
     /**
      * Loads data into the controller's view.
      * 
      *  {@pre None}
      *  
      *  {@post The controller has loaded data into its view}
      */
     @Override
     protected void loadValues() {
         ProductData selected = getView().getSelectedProduct();
         getView().setProducts(getStoredProductDatas());
         getView().setItems(getStoredItemDatas(selected));
     }
 
     /**
      * Sets the enable/disable state of all components in the controller's view.
      * A component should be enabled only if the user is currently
      * allowed to interact with that component.
      * 
      * {@pre None}
      * 
      * {@post The enable/disable state of all components in the controller's view
      * have been set appropriately.}
      */
     @Override
     protected void enableComponents() {
         int count = 0;
         try {
             count = Integer.parseInt(getView().getCount());
         } catch(Exception e){
             getView().enableItemAction(false);
             return;
         }
         getView().enableItemAction(
                 !getView().getBarcode().isEmpty()
                 && count > 0
                 && !_scanner
         );
     }
 
     /**
      * This method is called when the "Entry Date" field in the
      * add item batch view is changed by the user.
      */
     @Override
     public void entryDateChanged() {
     }
 
     /**
      * This method is called when the "Count" field in the
      * add item batch view is changed by the user.
      */
     @Override
     public void countChanged() {
         enableComponents();
     }
 
     /**
      * This method is called when the "Product Barcode" field in the
      * add item batch view is changed by the user.
      */
     @Override
     public void barcodeChanged() {
         enableComponents();
         try {
             _timer.cancel();
             _timer = new Timer();
         }
         catch(IllegalStateException e){ }
         if(_scanner)
             _timer.schedule(new ScannerTimer(), SCANNER_SECONDS);
     }
 
     /**
      * This method is called when the "Use Barcode Scanner" setting in the
      * add item batch view is changed by the user.
      */
     @Override
     public void useScannerChanged() {
         _scanner = getView().getUseScanner();
         getView().setBarcode("");
     }
 
     /**
      * This method is called when the selected product changes
      * in the add item batch view.
      */
     @Override
     public void selectedProductChanged() {
         getView().setItems(getStoredItemDatas(getView().getSelectedProduct()));
     }
 
     /**
      * This method is called when the user clicks the "Add Item" button
      * in the add item batch view.
      */
     @Override
     public void addItem() {
         int count = getCountFromView();
         if (count == -1) return;
         boolean createdProduct = false;
 
         // Make sure the product exists to be added to.
         String barcode = getView().getBarcode();
         if (!_productVault.hasProductWithBarcode(barcode)){
             getView().displayAddProductView(_target);
             createdProduct = true;
         }
         // If cancel was pressed, there is no product added, reset the view state.
         if (!_productVault.hasProductWithBarcode(barcode)){
             resetViewFields();
             return;
         }
 
         Product product = _productVault.find("Barcode = %o",  barcode);
         StorageUnit sUnit = StorageUnitVault.getInstance().get((Integer) _target.getTag());
         Collection<Item> newItems = new ArrayList<Item>();
 
         for(int i = 0; i < count; i++){
             Item item = new Item();
             item.setEntryDate(new DateTime(getView().getEntryDate()));
             item.setProduct(product);
             newItems.add(item);
         }
 
         // Make a new command:
         product = (createdProduct) ? product : null;
         AddItemCommand command = new AddItemCommand(newItems, product, sUnit, this);
         _commandManager.executeCommand(command);
 
         getView().setBarcode("");
         loadValues();
         getView().giveBarcodeFocus();
     }
 
     private int getCountFromView() {
         try {
             return Integer.parseInt(getView().getCount());
         } catch (Exception e) {
             getView().displayErrorMessage("Invalid Count!");
             return -1;
         }
     }
 
     private void resetViewFields() {
         loadValues();
         getView().setBarcode("");
         getView().setUseScanner(true);
         _scanner = true;
     }
     
     private ProductData findStoredProductData(String barcode){
         for(ProductData p: _products.keySet()){
             if(p.getBarcode().contentEquals(barcode))
                 return p;
         }
         return null;
     }
 
     /**
      * This method is called when the user clicks the "Redo" button
      * in the add item batch view.
      */
     @Override
     public void redo() {
     	_commandManager.redo();
     }
 
     /**
      * This method is called when the user clicks the "Undo" button
      * in the add item batch view.
      */
     @Override
     public void undo() {
     	_commandManager.undo();
     }
 
     /**
      * This method is called when the user clicks the "Done" button
      * in the add item batch view.
      */
     @Override
     public void done() {
         getView().close();
         if(!_products.isEmpty()){
             try{
                 BarcodePdf pdf = new BarcodePdf("items.pdf");
                 for(ArrayList<ItemData> i : _products.values()){
                     for(ItemData data : i){
                         int id = (Integer) data.getTag();
                         pdf.addItem(ItemVault.getInstance().get(id));
                     }
                 }
                 pdf.finish();
             } catch (Exception e){
                 getView().displayErrorMessage(e.getMessage()    );
             }
             if (Desktop.isDesktopSupported()) {
                 try {
                     File myFile = new File("items.pdf");
                     Desktop.getDesktop().open(myFile);
                 } catch (IOException ex) {
                     // no application registered for PDFs
                 }
             }
         }
     }
 
     private ProductData[] getStoredProductDatas(){
         if(_products.isEmpty())
             return new ProductData[0];
         return _products.keySet().toArray(new ProductData[_products.keySet().size()]);
     }
 
     private ItemData[] getStoredItemDatas(ProductData pd){
         if(_products.containsKey(pd))
             return _products.get(pd).toArray(new ItemData[_products.get(pd).size()]);
         return new ItemData[0];
     }
 
     private class ScannerTimer extends TimerTask {
 
         @Override
         public void run() {
             if(!getView().getBarcode().isEmpty())
                 addItem();
             _timer.cancel();
         }
     }
 
     @Override
     public void addItemToView(Item item) {
         ItemData itemData = GuiModelConverter.wrapItem(item);
         Product product = item.getProduct();
         ProductData productData = findStoredProductData(product.getBarcode());
         if(productData == null){
             productData = GuiModelConverter.wrapProduct(product);
             _products.put(productData, new ArrayList<ItemData>());
         }
 
         _products.get(productData).add(itemData);
         productData.setCount(String.valueOf(_products.get(productData).size()));
         loadValues();
     }
 
     @Override
     public void addProductToView(Product _product) {
         // TODO Auto-generated method stub
         
     }
 
     @Override
     public void removeItemFromView(Item item) {
         Product product = item.getProduct();
         ProductData productData = findStoredProductData(product.getBarcode());
         ArrayList<ItemData> itemsToDeleteArrayList = new ArrayList<ItemData>();
         if(productData != null){
             for(ItemData i : _products.get(productData)){
                 if(i.getTag().toString().equals(Integer.toString(item.getId()))){
                 	itemsToDeleteArrayList.add(i);
                 }
             }
             for (ItemData itemData : itemsToDeleteArrayList){
             	_products.get(productData).remove(itemData);
                 productData.setCount(String.valueOf(_products.get(productData).size()));
             }
 
         }
         loadValues();
     }
 
     @Override
     public void removeProductFromView(Product _product) {
         ProductData productData = findStoredProductData(_product.getBarcode());
         if (productData != null)
         	_products.remove(productData);
         loadValues();
     }
 }
 
