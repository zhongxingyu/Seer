 package ru.terra.terramarket.gui.swt.sell;
 
 import java.util.Date;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.DateTime;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.TableItem;
 import org.eclipse.swt.widgets.ToolBar;
 import org.eclipse.swt.widgets.ToolItem;
 
 import ru.terra.terramarket.cache.SellsCache;
 import ru.terra.terramarket.dto.sell.SellDTO;
 import ru.terra.terramarket.dto.sell.SellItemDTO;
 
 public class SellsWindow extends Shell {
 	private Table table;
 
 	/**
 	 * Create the shell.
 	 * 
 	 * @param display
 	 */
 	public SellsWindow(Display display) {
 		super(display, SWT.SHELL_TRIM);
 		setLayout(new GridLayout(1, false));
 
 		ToolBar toolBar = new ToolBar(this, SWT.FLAT | SWT.RIGHT);
 		toolBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
 
 		ToolItem tltmNewItem = new ToolItem(toolBar, SWT.NONE);
 		tltmNewItem.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				new DoSellDialog(getShell(), SWT.DIALOG_TRIM).open();
 			}
 		});
 		tltmNewItem.setText("Новая продажа...");
 
 		ToolItem tltmNewItem_1 = new ToolItem(toolBar, SWT.NONE);
 		tltmNewItem_1.setText("Отчёт по продажам...");
 
 		Composite composite = new Composite(this, SWT.NONE);
 		composite.setLayout(new GridLayout(2, false));
 		composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
 
 		Button button = new Button(composite, SWT.CHECK);
 		button.setText("Продажи за день");
 
 		DateTime dateTime = new DateTime(composite, SWT.BORDER);
 
 		table = new Table(this, SWT.BORDER | SWT.FULL_SELECTION);
 		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
 		table.setHeaderVisible(true);
 		table.setLinesVisible(true);
 
 		TableColumn tblclmnNewColumn_1 = new TableColumn(table, SWT.NONE);
 		tblclmnNewColumn_1.setWidth(100);
 		tblclmnNewColumn_1.setText("№");
 
 		TableColumn tblclmnNewColumn_2 = new TableColumn(table, SWT.NONE);
 		tblclmnNewColumn_2.setWidth(121);
 		tblclmnNewColumn_2.setText("Дата");
 
 		TableColumn tblclmnNewColumn_3 = new TableColumn(table, SWT.NONE);
 		tblclmnNewColumn_3.setWidth(100);
 		tblclmnNewColumn_3.setText("Сумма");
 
 		TableColumn tblclmnNewColumn = new TableColumn(table, SWT.NONE);
 		tblclmnNewColumn.setText("Продавец");
 		tblclmnNewColumn.setWidth(100);
 		createContents();
 		load();
 	}
 
 	/**
 	 * Create contents of the shell.
 	 */
 	protected void createContents() {
 		setText("Продажи");
 		setSize(798, 445);
 
 	}
 
 	@Override
 	protected void checkSubclass() {
 		// Disable the check that prevents subclassing of SWT components
 	}
 
 	private void load() {
 		for (SellDTO dto : SellsCache.getInstance().getValues()) {
 			TableItem ti = new TableItem(table, SWT.NONE);
 			Integer sum = 0;
 			for (SellItemDTO si : dto.sellItems) {
 				sum += (si.prod.priceOut * si.count);
 			}
 			ti.setText(new String[] { dto.id.toString(), new Date(dto.sellDate).toString(), sum.toString(), dto.user.name });
 		}
 	}
 }
