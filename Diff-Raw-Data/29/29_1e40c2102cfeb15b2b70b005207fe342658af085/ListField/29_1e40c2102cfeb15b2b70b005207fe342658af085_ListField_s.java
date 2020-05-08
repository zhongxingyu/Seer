 package org.zend.php.zendserver.deployment.ui.editors;
 
 import java.util.List;
 
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.widgets.Text;
 import org.zend.php.zendserver.deployment.core.descriptor.IModelContainer;
 import org.zend.php.zendserver.deployment.core.internal.descriptor.Feature;
 
 public class ListField extends TextField {

	private IModelContainer target;
 	
 	public ListField(IModelContainer target, Feature key, String label) {
 		super(target, key, label);
		this.target = target;
 	}
 
 	protected void createActions() {
 		text.addModifyListener(new ModifyListener() {
 			public void modifyText(ModifyEvent e) {
 				if (isRefresh) {
 					return;
 				}
 				
 				String text = ((Text)e.widget).getText();
 				String[] items = text.split("\n");
 				if (target != null) {
					List<Object> list = target.getChildren(key);
 					for (int i = 0; i < Math.min(items.length, list.size()); i++) {
 						list.set(i, items[i]);
 					}
 					for (int i = list.size(); i < items.length; i++) {
 						list.add(items[i]);
 					}
 					for (int i = items.length; i < list.size(); i++) {
 						list.remove(items.length);
 					}
 				}
 			}
 		});
 	}
 	
 	public void refresh() {
 		isRefresh = true;
 		try {
			List<Object> value = target != null ? target.getChildren(key) : null;
 			StringBuilder sb = new StringBuilder();
 			if (value != null) {
 				for (Object o : value) {
					sb.append(o);
 				}
 			}
 			text.setText(sb.toString());
 		} finally {
 			isRefresh = false;
 		}
 	}
 }
