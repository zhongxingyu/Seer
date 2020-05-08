 package gui.product;
 
 import gui.common.*;
 import gui.inventory.ProductContainerData;
 import model.common.Barcode;
 import model.common.Size;
 import model.product.Product;
 import model.productidentifier.ProductIdentifier;
 import org.joda.time.DateTime;
 
 /**
  * Controller class for the add item view.
  */
 public class AddProductController extends Controller implements
 		IAddProductController {
 
     private ProductContainerData target;
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param view Reference to the add product view
 	 * @param barcode Barcode for the product being added
 	 */
 	public AddProductController(IView view, String barcode, ProductContainerData target) {
 		super(view);
         this.target = target;
 		construct();
         toggleAll(false);
         getView().setBarcode(barcode);
         getView().setDescription("Fetching Description... Please Wait...");
         String desc = ProductIdentifier.identify(barcode);
         toggleAll(true);
         getView().setDescription("");
         if(desc.length()>0){
             getView().setDescription(desc);
         }
         getView().setSizeValue("1");
         getView().enableSizeValue(false);
         getView().setShelfLife("0");
         getView().setSupply("0");
         enableComponents();
     }
 
     private void toggleAll(boolean toggle){
         getView().enableBarcode(toggle);
         getView().enableDescription(toggle);
         getView().enableOK(toggle);
         getView().enableShelfLife(toggle);
         getView().enableSizeUnit(toggle);
         getView().enableSizeValue(toggle);
         getView().enableSupply(toggle);
     }
 
 	//
 	// Controller overrides
 	//
 	
 	/**
 	 * Returns a reference to the view for this controller.
 	 * 
 	 * {@pre None}
 	 * 
 	 * {@post Returns a reference to the view for this controller.}
 	 */
 	@Override
 	protected IAddProductView getView() {
 		return (IAddProductView)super.getView();
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
         getView().enableSizeValue(true);
         Product p = new Product();
         if(getView().getSizeUnit().name() == "Count"){
             getView().enableSizeValue(false);
             getView().setSizeValue("1");
         }
         try{
             p.setSize(new Size(Float.parseFloat(getView().getSizeValue()),getView().getSizeUnit().toString()));
             p.setShelfLife(Integer.parseInt(getView().getShelfLife()));
             p.set3MonthSupply(Integer.parseInt(getView().getSupply()));
             p.setBarcode(getView().getBarcode());
             p.setDescription(getView().getDescription());
             p.setContainerId((Integer) target.getTag());
             p.setCreationDate(DateTime.now());
             p.setStorageUnitId((Integer) target.getTag());
         } catch (Exception e){
             getView().enableOK(false);
             return;
         }
         getView().enableOK(p.validate().getStatus());
 
 
 
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
 
 	}
 
 	//
 	// IAddProductController overrides
 	//
 	
 	/**
 	 * This method is called when any of the fields in the
 	 * add product view is changed by the user.
 	 */
 	@Override
 	public void valuesChanged() {
         enableComponents();
 	}
 	
 	/**
 	 * This method is called when the user clicks the "OK"
 	 * button in the add product view.
 	 */
 	@Override
 	public void addProduct() {
         Product product = new Product();
         product.set3MonthSupply(Integer.parseInt(getView().getSupply()));
         product.setBarcode(getView().getBarcode());
         product.setContainerId((Integer) target.getTag());
         product.setCreationDate(DateTime.now());
         product.setDescription(getView().getDescription());
         product.setShelfLife(Integer.parseInt(getView().getShelfLife()));
         product.setSize(new Size(Float.parseFloat(getView().getSizeValue()), getView().getSizeUnit().toString()));
         product.setStorageUnitId((Integer) target.getTag());
         product.validate();
         product.save();
 	}
 
 }
 
