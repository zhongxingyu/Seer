 package ch.hslu.appe.fs1303.gui.dialogs;
 
 import java.util.Comparator;
 
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.swt.widgets.Shell;
 
 import ch.hslu.appe.fs1301.business.shared.dto.DTOProdukt;
 import ch.hslu.appe.fs1303.gui.labelprovider.ProductLabelProvider;
 
 public class ProductSearchDialog extends QuickSearchDialog<DTOProdukt> {
 
 	public static final String ID = "ch.hslu.appe.fs1303.gui.dialogs.ProductSearchDialog";
 	
 	public ProductSearchDialog(Shell shell, boolean multi,
 			iQuickSearchCallback<DTOProdukt> callback) {
 		super(shell, multi, callback);
 	}
 
 	@Override
 	public LabelProvider getLabelProvider() {		
 		return new ProductLabelProvider();
 	}
 
 	@Override
 	public String getId() {
 		return ID;
 	}
 
 	@SuppressWarnings("rawtypes")
 	@Override
 	protected Comparator getItemsComparator() {
 		return new Comparator<DTOProdukt>() {
 			@Override
 			public int compare(DTOProdukt o1, DTOProdukt o2) {
 				return o1.getBezeichnung().compareTo(o2.getBezeichnung());
 			}
 		};
 	}
 }
