 package model.report;
 
 import java.util.Arrays;
 
 import model.Item;
 import model.Product;
 import model.ProductContainer;
 import model.ProductContainerManager;
 import model.ProductGroup;
 import model.ProductManager;
 import model.StorageUnit;
 import model.report.builder.ReportBuilder;
 import model.visitor.InventoryVisitor;
 
 @SuppressWarnings("serial")
 public class NMonthSupplyReport extends Report implements InventoryVisitor {
 	private ProductManager productManager;
 	private ProductContainerManager productContainerManager;
 	private ReportBuilder builder;
 	private int months;
 
 	/**
 	 * Set up an empty NMonthSupplyReport.
 	 * 
 	 * @param productManager
 	 *            Manager to use for finding Product n-month supplies
 	 * @param productContainerManager
 	 *            Manager to use for finding ProductGroup n-month supplies
 	 * 
 	 * @pre true
 	 * @post true
 	 */
 	public NMonthSupplyReport(ProductManager productManager,
 			ProductContainerManager productContainerManager) {
 		this.productManager = productManager;
 		this.productContainerManager = productContainerManager;
 		months = -1;
 	}
 
 	/**
 	 * Construct a completed NMonthSupplyReport, where months is the desired number of months
 	 * of supply to have on hand.
 	 * 
 	 * @param builder
 	 *            ReportBuilder to use
 	 * @param months
 	 *            Desired number of months of supply to have on hand
 	 * 
 	 * @pre true
 	 * @post (new Date()).getTime() - getLastRunTime().getTime() < 1000
 	 */
 	public void construct(ReportBuilder builder, int months) {
 		this.builder = builder;
 		this.months = months;
 
 		updateLastRunTime();
 		builder.addDocumentTitle(Integer.toString(months) + "-Month Supply Report");
 
 		builder.addSectionTitle("Products");
 		builder.startTable(Arrays.asList("Description", "Barcode", Integer.toString(months)
 				+ "-Month Supply", "Current Supply"));
 
 		for (Product product : productManager.getProducts()) {
 			if (product.getCurrentSupply() < product.getNMonthSupply(months)) {
 				builder.addTableRow(Arrays.asList(product.getDescription(),
 						product.getBarcode(),
 						Integer.toString(product.getNMonthSupply(months)) + " count",
 						Integer.toString(product.getCurrentSupply()) + " count"));
 			}
 		}
 
 		builder.addSectionTitle("Product Groups");
 		builder.startTable(Arrays.asList("Product Group", "Storage Unit",
 				Integer.toString(months) + "-Month Supply", "Current Supply"));
 
 		for (StorageUnit storageUnit : productContainerManager.getStorageUnits()) {
 			storageUnit.accept(this);
 		}
 
 		this.builder = null;
 		this.months = -1;
 	}
 
 	@Override
 	public void visit(Item item) {
 	}
 
 	@Override
 	public void visit(Product product) {
 	}
 
 	@Override
 	public void visit(ProductContainer productContainer) {
 		if (builder == null) {
 			throw new NullPointerException(
 					"visit(ProductContainer) called outside a construct operation");
 		}
 		if (months == -1) {
 			throw new IllegalStateException(
 					"visit(ProductContainer) called outside a construct operation");
 		}
 
 		if (productContainer instanceof ProductGroup) {
 			ProductGroup productGroup = (ProductGroup) productContainer;
 
 			try {
 				if (productGroup.getCurrentSupply().compareTo(
						productGroup.getThreeMonthSupply()) < 0) {
 					builder.addTableRow(Arrays.asList(productGroup.getName(), productGroup
							.getRoot().getName(), productGroup.getThreeMonthSupply()
							.multiplyBy(months / 3.0f).toString(), productGroup
							.getCurrentSupply().toString()));
 				}
 			} catch (IllegalArgumentException e) {
 				// Skip this node
 			}
 		}
 	}
 }
