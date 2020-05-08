 package uk.ac.diamond.scisoft.icatexplorer.rcp.datasets;
 
 import org.eclipse.jface.viewers.ILabelProvider;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
 import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.navigator.IDescriptionProvider;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 
import uk.ac.diamond.scisoft.icatexplorer.rcp.datasets.DatasetTreeData;

 public class DatasetLabelProvider extends LabelProvider implements
 ILabelProvider, IDescriptionProvider, IStyledLabelProvider {
 
 
 	public Image getImage(Object element) {
 		if (element instanceof DatasetTreeData){
 			String iconName = "prop_ps.gif";
 			return AbstractUIPlugin.imageDescriptorFromPlugin(
 					"uk.ac.diamond.scisoft.icatexplorer.rcp",
 					"icons/" + iconName).createImage();
 		}
 		return null;
 	}
 
 	public String getText(Object element) {
 
 		if (element instanceof DatasetTreeData) {
 			return "" + getStyledText(element); 
 		}  
 		return null;
 	}
 
 	public String getDescription(Object anElement) {
 		if (anElement instanceof DatasetTreeData) {
 			DatasetTreeData data = (DatasetTreeData) anElement;
 			return "Dataset: " + data.getIcatDataset().getName(); 
 		}
 		return null;
 	}
 
 
 	@Override
 	public StyledString getStyledText(Object obj) {
 		StyledString styledString = new StyledString();
 
 		if (obj instanceof DatasetTreeData) {
 			styledString.append(((DatasetTreeData) obj).getIcatDataset().getName());
 //			styledString.append("   ");
 //			styledString.append(
 //					((DatasetTreeData) obj).getIcatDataset().getDatasetStatus()
 //					+ "   ",
 //					StyledString.DECORATIONS_STYLER);
 
 		}
 		return styledString;
 	}
 
 }
