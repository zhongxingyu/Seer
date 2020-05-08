 package gui.product;
 
 import gui.common.Controller;
 import gui.common.IView;
 import gui.common.SizeUnits;
 import model.Product;
 import model.ProductManager;
 import model.ProductQuantity;
 import model.Unit;
 
 /**
  * Controller class for the add item view.
  */
 public class AddProductController extends Controller implements IAddProductController {
 
 	private String barcode;
 	/**
 	 * Constructor.
 	 * 
 	 * @param view
 	 *            Reference to the add product view
 	 * @param barcode
 	 *            Barcode for the product being added
 	 */
 	public AddProductController(IView view, String barcode) {
 		super(view);
 		this.barcode = barcode;
 
 	}
 
 	//
 	// Controller overrides
 	//
 
 	/**
 	 * This method is called when the user clicks the "OK" button in the add product view.
 	 */
 	@Override
 	public void addProduct() {
 		ProductManager productManager = getProductManager();
 		String barcode = getView().getBarcode();
 		String description = getView().getDescription();
 		int shelfLife = 0;
 		try {
 			shelfLife = Integer.parseInt(getView().getShelfLife());
 		} catch (NumberFormatException e) {
 		}
 		int threeMonthSupply = 0;
 		try {
 			threeMonthSupply = Integer.parseInt(getView().getSupply());
 		} catch (NumberFormatException e) {
 		}
 		float quantity = 1;
 		try {
 			quantity = (float) Double.parseDouble(getView().getSizeValue());
 		} catch (NumberFormatException e) {
 		}
 
 		Unit unit = Unit.convertFromSizeUnits(getView().getSizeUnit());
 		ProductQuantity pq = new ProductQuantity(quantity, unit);
 
 		Product product = new Product(barcode, description, shelfLife, threeMonthSupply, pq,
 				productManager);
 	}
 
 	/**
 	 * This method is called when any of the fields in the add product view is changed by the
 	 * user.
 	 */
 	@Override
 	public void valuesChanged() {
 		if (getView().getSizeUnit().equals(SizeUnits.Count))
 			getView().setSizeValue("1");
 		enableComponents();
 	}
 
 	private boolean isAllDataValid() {
 		int shelfLife = 0;
 		try {
 			shelfLife = Integer.parseInt(getView().getShelfLife());
 			if (!Product.isValidShelfLife(shelfLife))
 				return false;
 		} catch (NumberFormatException e) {
 			return false;
 		}
 		double sizeValue = 1;
 		try {
 			sizeValue = Double.parseDouble(getView().getSizeValue());
 			Unit unit = Unit.convertFromSizeUnits(getView().getSizeUnit());
 			if (!ProductQuantity.isValidProductQuantity((float) sizeValue, unit))
 				return false;
 			ProductQuantity pq = new ProductQuantity((float) sizeValue, unit);
 			if (!Product.isValidProductQuantity(pq))
 				return false;
 		} catch (NumberFormatException e) {
 			return false;
 		}
 		int tms = 0;
 		try {
 			tms = Integer.parseInt(getView().getSupply());
 			if (!Product.isValidThreeMonthSupply(tms))
 				return false;
 		} catch (NumberFormatException e) {
 			return false;
 		}
 		return (!getView().getBarcode().equals("") && !getView().getDescription().equals(""));
 	}
 
 	/**
 	 * Sets the enable/disable state of all components in the controller's view. A component
 	 * should be enabled only if the user is currently allowed to interact with that component.
 	 * 
 	 * {@pre None}
 	 * 
 	 * {@post The enable/disable state of all components in the controller's view have been set
 	 * appropriately.}
 	 */
 	@Override
 	protected void enableComponents() {
 		getView().enableBarcode(false);
 		getView().enableDescription(true);
 		getView().enableOK(isAllDataValid());
 		getView().enableShelfLife(true);
 		getView().enableSizeUnit(true);
 		getView().enableSizeValue(!getView().getSizeUnit().equals(SizeUnits.Count));
 		getView().enableSupply(true);
 	}
 
 	//
 	// IAddProductController overrides
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
 		return (IAddProductView) super.getView();
 	}
 
 	/**
 	 * Loads data into the controller's view.
 	 * 
 	 * {@pre None}
 	 * 
 	 * {@post The controller has loaded data into its view}
 	 */
 	@Override
 	protected void loadValues() {
		getView().setBarcode(barcode);
		getView().setSizeUnit(SizeUnits.Count);
		getView().setSizeValue("1");
		getView().setSupply("0");
		getView().setShelfLife("0");
		construct();
 	}
 }
