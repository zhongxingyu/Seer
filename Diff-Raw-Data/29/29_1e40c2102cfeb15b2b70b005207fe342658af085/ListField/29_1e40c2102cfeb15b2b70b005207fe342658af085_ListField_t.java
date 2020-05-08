 package org.zend.php.zendserver.deployment.ui.editors;
 
 import java.util.List;
 
import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
 import org.zend.php.zendserver.deployment.core.descriptor.IModelContainer;
 import org.zend.php.zendserver.deployment.core.internal.descriptor.Feature;
 
 public class ListField extends TextField {
 	
 	public ListField(IModelContainer target, Feature key, String label) {
 		super(target, key, label);
	}
	
	@Override
	protected void createTextControl(Composite parent, FormToolkit toolkit) {
		text = toolkit.createText(parent, "", SWT.MULTI|SWT.V_SCROLL|SWT.H_SCROLL|SWT.WRAP);
		GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gd.horizontalSpan = labelTxt != null ? 2 : 3;
		gd.heightHint = 100;
		text.setLayoutData(gd);
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
					List<Object> list = ((IModelContainer)target).getChildren(key);
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
			List<Object> value = target != null ? ((IModelContainer)target).getChildren(key) : null;
 			StringBuilder sb = new StringBuilder();
 			if (value != null) {
 				for (Object o : value) {
					sb.append(o).append("\n");
 				}
 			}
 			text.setText(sb.toString());
 		} finally {
 			isRefresh = false;
 		}
 	}
 }
