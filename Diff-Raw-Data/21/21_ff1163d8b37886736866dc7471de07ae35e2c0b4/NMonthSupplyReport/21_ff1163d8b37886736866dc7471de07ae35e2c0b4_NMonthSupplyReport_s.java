 package model.report;
 
 import java.util.Arrays;
 
 import model.Item;
 import model.Product;
 import model.ProductContainer;
 import model.ProductContainerManager;
 import model.ProductManager;
 import model.report.builder.ReportBuilder;
 import model.visitor.InventoryVisitor;
 
 @SuppressWarnings("serial")
 public class NMonthSupplyReport extends Report implements InventoryVisitor {
 	private ProductManager productManager;
 	private ProductContainerManager productContainerManager;
 
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
 		updateLastRunTime();
 		builder.addDocumentTitle(Integer.toString(months) + "-Month Supply Report");
 
 		builder.addSectionTitle("Products");
 		builder.startTable(Arrays.asList("Description", "Barcode", Integer.toString(months)
 				+ "-Month Supply", "Current Supply"));
 
 		for (Product product : productManager.getProducts()) {
 			builder.addTableRow(Arrays.asList(product.getDescription(), product.getBarcode(),
					Integer.toString(product.getThreeMonthSupply()), product
 							.getCurrentSupply().toString()));
 		}
 
 		builder.addSectionTitle("Product Groups");
 		builder.startTable(Arrays.asList("Product Group", "Storage Unit",
 				Integer.toString(months) + "-Month Supply", "Current Supply"));
 	}
 
 	@Override
 	public void visit(Item item) {
 	}
 
 	@Override
 	public void visit(Product product) {
 	}
 
 	@Override
 	public void visit(ProductContainer productContainer) {
 	}
 }
