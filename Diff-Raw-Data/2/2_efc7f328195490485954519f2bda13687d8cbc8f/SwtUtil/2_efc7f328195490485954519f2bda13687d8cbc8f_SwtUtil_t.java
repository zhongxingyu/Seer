 package interfaccia;
 
 import java.util.Arrays;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableItem;
 
 public class SwtUtil {
 	private SwtUtil(){
 		
 	}
 	
 	public static void orderTableByInt(Table tabella, int indiceCampo) {
 
 		TableItem[] items = tabella.getItems();
 
 		if (items.length > 0) {
 			int[] keys = new int[items.length];
 			for (int i = 0; i < items.length; i++) {
 				keys[i] = Integer.parseInt(items[i].getText(indiceCampo));
 			}
 			Arrays.sort(keys);
 			for (int i = 0; i < keys.length; i++) {
 				for (int j = 0; j < items.length; j++) {
 
 					if (keys[i] == Integer.parseInt(items[j].getText(indiceCampo))) {
 
 						TableItem newitem = new TableItem(tabella, SWT.NONE);
 						for (int k = 0; k < tabella.getColumnCount(); k++) {
 							newitem.setText(k, items[j].getText(k));
 
 						}
 						break;
 					}
 				}
 			}
 
 			tabella.remove(0, tabella.indexOf(items[(items.length) - 1]));
 		}
 	}
 	
 	public static void orderTableByString(Table tabella, int indiceCampo) {
 
 		TableItem[] items = tabella.getItems();
 
 		if (items.length > 0) {
 			String[] keys = new String[items.length];
 			for (int i = 0; i < items.length; i++) {
 				keys[i] = items[i].getText(indiceCampo);
 			}
 			Arrays.sort(keys);
 			for (int i = 0; i < keys.length; i++) {
 				for (int j = 0; j < items.length; j++) {
 
					if (keys[i].equals(items[j].getText(indiceCampo))) {
 
 						TableItem newitem = new TableItem(tabella, SWT.NONE);
 						for (int k = 0; k < tabella.getColumnCount(); k++) {
 							newitem.setText(k, items[j].getText(k));
 
 						}
 						break;
 					}
 				}
 			}
 
 			tabella.remove(0, tabella.indexOf(items[(items.length) - 1]));
 		}
 	}
 }
