 package net.sf.jmoney.stocks.navigator;
 
 import net.sf.jmoney.model2.ExtendableObject;
 import net.sf.jmoney.model2.PropertySet;
 import net.sf.jmoney.stocks.model.Stock;
 import net.sf.jmoney.stocks.resources.Messages;
 import net.sf.jmoney.stocks.views.StocksNode;
 
 import org.eclipse.jface.viewers.ILabelProvider;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.ui.navigator.IDescriptionProvider;
 
 public class StocksLabelProvider extends LabelProvider implements ILabelProvider, IDescriptionProvider {
 
 	@Override	
 	public Image getImage(Object element) {
 		if (element instanceof ExtendableObject) {
 			ExtendableObject extendableObject = (ExtendableObject)element;
			return PropertySet.getPropertySet(extendableObject.getClass()).getIconImage();
 		}
 		return null;
 	}
 
 	@Override
 	public String getText(Object element) {
 		if (element instanceof StocksNode) {
 			return Messages.NavigationTreeNode_stocks;
 		} else if (element instanceof ExtendableObject) {
 			return ((ExtendableObject)element).toString();
 		}
 		
 		// We should not get here.
 		return "Unnamed Node"; //$NON-NLS-1$
 	}
 
 	public String getDescription(Object element) {
 		if (element instanceof Stock) {
 			return NLS.bind(Messages.NavigationTree_stockDescription,((Stock)element).getName());
 		}
 		return "";
 	}
 
 }
