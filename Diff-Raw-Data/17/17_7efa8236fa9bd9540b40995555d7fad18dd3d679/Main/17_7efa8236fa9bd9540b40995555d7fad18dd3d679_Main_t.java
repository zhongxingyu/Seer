 package face;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Monitor;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.TabFolder;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.TabItem;
 import org.eclipse.swt.widgets.TableItem;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.swt.custom.ControlEditor;
 import org.eclipse.swt.custom.TableCursor;
 import org.eclipse.swt.events.FocusAdapter;
 import org.eclipse.swt.events.FocusEvent;
 import org.eclipse.swt.events.KeyAdapter;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.MouseAdapter;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.layout.GridData;
 
 import brain.Car;
 import brain.Clients;
 import brain.Order;
 import brain.Services;
 import brain.Staff;
 
 import org.eclipse.swt.layout.RowLayout;
 
 public class Main {
 
 	protected Shell shlAutoservice;
 	private Table table;
 	TableItem rowForDelInClients;
 	TableItem rowForDelInCar;
 	TableItem rowForDelInOrder;
 	TableItem rowForChangeInStaff;
 	TableItem rowForChangeInServices;
 	private Table table_1;
 	private Table table_2;
 	private Table table_3;
 	private Table table_4;
 
 	/**
 	 * Launch the application.
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		try {
 			Main window = new Main();
 			window.open();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Open the window.
 	 */
 	public void open() {
 		Display display = Display.getDefault();
 		createContents(display);
 		presentForm pf = new presentForm(shlAutoservice, 0);
 		pf.open();
 		shlAutoservice.open();
 		shlAutoservice.layout();
 		while (!shlAutoservice.isDisposed()) {
 			if (!display.readAndDispatch()) {
 				display.sleep();
 			}
 		}
 	}
 
 	/**
 	 * Create contents of the window.
 	 */
 	protected void createContents(Display display) {
 		shlAutoservice = new Shell();
 		shlAutoservice.setSize(887, 389);
 		shlAutoservice.setText("AutoService");
 		shlAutoservice.setLayout(new FillLayout(SWT.HORIZONTAL));
 		
 		Monitor[] list = display.getMonitors();
 		org.eclipse.swt.graphics.Rectangle client = shlAutoservice.getBounds();
 		org.eclipse.swt.graphics.Rectangle screen = list[0].getBounds();
 		client.x = screen.width/2 -client.width/2;
 		client.y = screen.height/2 - client.height/2;
 		shlAutoservice.setBounds(client);
 		
 		TabFolder tabFolder = new TabFolder(shlAutoservice, SWT.NONE);
 		
 		TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
 		tabItem.setText("Клиенты");
 		
 		Composite composite = new Composite(tabFolder, SWT.NONE);
 		tabItem.setControl(composite);
 		composite.setLayout(new GridLayout(1, false));
 		
 		table = new Table(composite, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
 		GridData gd_table = new GridData(SWT.CENTER, SWT.FILL, true, true, 1, 1);
 		gd_table.heightHint = 287;
 		gd_table.widthHint = 589;
 		table.setLayoutData(gd_table);
 		table.setHeaderVisible(true);
 		table.setLinesVisible(true);
 		
 		TableColumn tblclmnNewColumn = new TableColumn(table, SWT.CENTER);
 		tblclmnNewColumn.setWidth(78);
 		tblclmnNewColumn.setText("Фамилия");
 		
 		TableColumn tableColumn = new TableColumn(table, SWT.CENTER);
 		tableColumn.setWidth(77);
 		tableColumn.setText("Имя");
 
 		TableColumn tableColumn_1 = new TableColumn(table, SWT.CENTER);
 		tableColumn_1.setWidth(90);
 		tableColumn_1.setText("Отчество");
 
 		TableColumn tableColumn_2 = new TableColumn(table, SWT.CENTER);
 		tableColumn_2.setWidth(132);
 		tableColumn_2.setText("Мобильный \r\nтелефон");
 		
 		TableColumn tableColumn_3 = new TableColumn(table, SWT.CENTER);
 		tableColumn_3.setWidth(100);
 		tableColumn_3.setText("Паспорт");
 		
 		TableColumn tableColumn_4 = new TableColumn(table, SWT.CENTER);
 		tableColumn_4.setWidth(100);
 		tableColumn_4.setText("Права");
 		
 		final Clients c = new Clients();
 		c.fillingTable(table);
 		
 		
 		TabItem tabItem_1 = new TabItem(tabFolder, SWT.NONE);
 		tabItem_1.setText("Машины");
 		
 		// create a TableCursor to navigate around the table
 		final TableCursor cursor = new TableCursor(table, SWT.NONE);
 		// create an editor to edit the cell when the user hits "ENTER" 
 		// while over a cell in the table
 		final ControlEditor editor = new ControlEditor(cursor);
 		
 		Composite composite_2 = new Composite(composite, SWT.NONE);
 		composite_2.setLayout(null);
 		GridData gd_composite_2 = new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1);
 		gd_composite_2.widthHint = 157;
 		gd_composite_2.heightHint = 24;
 		composite_2.setLayoutData(gd_composite_2);
 		
 		Button btnNewButton = new Button(composite_2, SWT.NONE);
 		btnNewButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent arg0) {
 				AddClients ac = new AddClients(shlAutoservice,SWT.DIALOG_TRIM);
 				ac.open();
 				table.removeAll();
 				c.fillingTable(table);
 			}
 		});
 		btnNewButton.setBounds(0, 0, 75, 25);
 		btnNewButton.setText("Добавить");
 		
 		
 		
 		cursor.addSelectionListener(new SelectionAdapter() {
 			// when the TableEditor is over a cell, select the corresponding row in 
 			// the table
 			public void widgetSelected(SelectionEvent e) {
 				rowForDelInClients = cursor.getRow();
 				
 				table.setSelection(new TableItem[] { cursor.getRow()});
 			}
 			// when the user hits "ENTER" in the TableCursor, pop up a text editor so that 
 			// they can change the text of the cell
 			public void widgetDefaultSelected(SelectionEvent e) {
 				final Text text = new Text(cursor, SWT.NONE);
 				TableItem row = cursor.getRow();
 				int column = cursor.getColumn();
 				text.setText(row.getText(column));
 				text.addKeyListener(new KeyAdapter() {
 					public void keyPressed(KeyEvent e) {
 						// close the text editor and copy the data over 
 						// when the user hits "ENTER"
 						if (e.character == SWT.CR) {
 							TableItem row = cursor.getRow();
 							String buff = row.getText(4);
 							int column = cursor.getColumn();
 							row.setText(column, text.getText());
 							c.setTableItem(buff, column, text.getText());
 							text.dispose();
 						}
 						// close the text editor when the user hits "ESC"
 						if (e.character == SWT.ESC) {
 							text.dispose();
 						}
 					}
 				});
 				// close the text editor when the user tabs away
 				text.addFocusListener(new FocusAdapter() {
 					public void focusLost(FocusEvent e) {
 						text.dispose();
 					}
 				});
 				editor.setEditor(text);
 				text.setFocus();
 			}
 		});
 		
 		Button button = new Button(composite_2, SWT.NONE);
 		button.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent arg0) {
 				if(rowForDelInClients!=null)
 				{
 					c.deleteTableItem(rowForDelInClients.getText(4));
 					table.removeAll();
 					c.fillingTable(table);
 				}
 			}
 		});
 		button.setBounds(81, 0, 75, 25);
 		button.setText("Удалить");
 		editor.grabHorizontal = true;
 		editor.grabVertical = true;
 		
 		// When the user double clicks in the TableCursor, pop up a text editor so that 
 		// they can change the text of the cell.
 		cursor.addMouseListener(new MouseAdapter() {
 			public void mouseDown(MouseEvent e) {
 				final Text text = new Text(cursor, SWT.NONE);
 				TableItem row = cursor.getRow();
 				int column = cursor.getColumn();
 				text.setText(row.getText(column));
 				text.addKeyListener(new KeyAdapter() {
 					public void keyPressed(KeyEvent e) {
 						// close the text editor and copy the data over 
 						// when the user hits "ENTER"
 						if (e.character == SWT.CR) {
 							TableItem row = cursor.getRow();
 							String buff = row.getText(4);
 							int column = cursor.getColumn();
 							row.setText(column, text.getText());
 							c.setTableItem(buff, column, text.getText());
 							text.dispose();
 						}
 						// close the text editor when the user hits "ESC"
 						if (e.character == SWT.ESC) {
 							text.dispose();
 						}
 					}
 				});
 				// close the text editor when the user clicks away
 				text.addFocusListener(new FocusAdapter() {
 					public void focusLost(FocusEvent e) {
 						text.dispose();
 					}
 				});
 				editor.setEditor(text);
 				text.setFocus();
 			}
 		});
 		
 //		// Hide the TableCursor when the user hits the "CTRL" or "SHIFT" key.
 //		// This allows the user to select multiple items in the table.
 //		cursor.addKeyListener(new KeyAdapter() {
 //			public void keyPressed(KeyEvent e) {
 //				if (e.keyCode == SWT.CTRL
 //					|| e.keyCode == SWT.SHIFT
 //					|| (e.stateMask & SWT.CONTROL) != 0
 //					|| (e.stateMask & SWT.SHIFT) != 0) {
 //					cursor.setVisible(false);
 //				}
 //			}
 //		});
 //		
 //		table.addKeyListener(new KeyAdapter() {
 //			public void keyReleased(KeyEvent e) {
 //				if (e.keyCode == SWT.CONTROL && (e.stateMask & SWT.SHIFT) != 0)
 //					return;
 //				if (e.keyCode == SWT.SHIFT && (e.stateMask & SWT.CONTROL) != 0)
 //					return;
 //				if (e.keyCode != SWT.CONTROL
 //					&& (e.stateMask & SWT.CONTROL) != 0)
 //					return;
 //				if (e.keyCode != SWT.SHIFT && (e.stateMask & SWT.SHIFT) != 0)
 //					return;
 //
 //				TableItem[] selection = table.getSelection();
 //				TableItem row = (selection.length == 0) ? table.getItem(table.getTopIndex()) : selection[0];
 //				table.showItem(row);
 //				cursor.setSelection(row, 0);
 //				cursor.setVisible(true);
 //				cursor.setFocus();
 //			}
 //		});
 		
 		Composite composite_1 = new Composite(tabFolder, SWT.NONE);
 		tabItem_1.setControl(composite_1);
 		composite_1.setLayout(new GridLayout(1, false));
 		
 		table_1 = new Table(composite_1, SWT.BORDER | SWT.FULL_SELECTION);
 		GridData gd_table_1 = new GridData(SWT.CENTER, SWT.FILL, true, true, 1, 1);
 		gd_table_1.widthHint = 843;
 		table_1.setLayoutData(gd_table_1);
 		table_1.setHeaderVisible(true);
 		table_1.setLinesVisible(true);
 		
 		TableColumn tableColumn_5 = new TableColumn(table_1, SWT.NONE);
 		tableColumn_5.setWidth(153);
 		tableColumn_5.setText("Владелец");
 		
 		TableColumn tblclmnNewColumn_1 = new TableColumn(table_1, SWT.NONE);
 		tblclmnNewColumn_1.setWidth(88);
 		tblclmnNewColumn_1.setText("Марка");
 		
 		TableColumn tblclmnNewColumn_2 = new TableColumn(table_1, SWT.NONE);
 		tblclmnNewColumn_2.setWidth(72);
 		tblclmnNewColumn_2.setText("Кузов");
 		
 		TableColumn tblclmnNewColumn_3 = new TableColumn(table_1, SWT.NONE);
 		tblclmnNewColumn_3.setWidth(124);
 		tblclmnNewColumn_3.setText("VIN");
 		
 		TableColumn tblclmnNewColumn_4 = new TableColumn(table_1, SWT.NONE);
 		tblclmnNewColumn_4.setWidth(100);
 		tblclmnNewColumn_4.setText("Цвет");
 		
 		TableColumn tblclmnNewColumn_5 = new TableColumn(table_1, SWT.NONE);
 		tblclmnNewColumn_5.setWidth(100);
 		tblclmnNewColumn_5.setText("Год");
 		
 		TableColumn tblclmnNewColumn_6 = new TableColumn(table_1, SWT.NONE);
 		tblclmnNewColumn_6.setWidth(100);
 		tblclmnNewColumn_6.setText("Стоимость");
 		
 		TableColumn tableColumn_6 = new TableColumn(table_1, SWT.NONE);
 		tableColumn_6.setWidth(100);
 		tableColumn_6.setText("Гарантия");
 		
 		Car.fillingTable(table_1);
 		
 				final TableCursor cursor1 = new TableCursor(table_1, SWT.NONE);
 				final ControlEditor editor1 = new ControlEditor(cursor1);
 				
 				Composite composite_3 = new Composite(composite_1, SWT.NONE);
 				GridData gd_composite_3 = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
 				gd_composite_3.widthHint = 158;
 				gd_composite_3.heightHint = 24;
 				composite_3.setLayoutData(gd_composite_3);
 				
 				Button btnNewButton_1 = new Button(composite_3, SWT.NONE);
 				btnNewButton_1.addSelectionListener(new SelectionAdapter() {
 					@Override
 					public void widgetSelected(SelectionEvent arg0) {
 						AddCar ac = new AddCar(shlAutoservice,SWT.DIALOG_TRIM);
 						ac.open();
 						table_1.removeAll();
 						Car.fillingTable(table_1);
 					}
 				});
 				btnNewButton_1.setBounds(0, 0, 75, 25);
 				btnNewButton_1.setText("Добавить");
 				
 				Button button_1 = new Button(composite_3, SWT.NONE);
 				button_1.addSelectionListener(new SelectionAdapter() {
 					@Override
 					public void widgetSelected(SelectionEvent arg0) {
 						if(rowForDelInCar!=null)
 						{
 							Car.deleteTableItem(rowForDelInCar.getText(3));
 							table_1.removeAll();
 							Car.fillingTable(table_1);
 						}
 					}
 				});
 				button_1.setBounds(81, 0, 75, 25);
 				button_1.setText("Удалить");
 				
 				TabItem tabItem_2 = new TabItem(tabFolder, SWT.NONE);
 				tabItem_2.setText("Заказы");
 				
 				Composite composite_4 = new Composite(tabFolder, SWT.NONE);
 				tabItem_2.setControl(composite_4);
 				composite_4.setLayout(new GridLayout(1, false));
 				
 				
 				
 				table_2 = new Table(composite_4, SWT.BORDER | SWT.FULL_SELECTION);
 				GridData gd_table_2 = new GridData(SWT.CENTER, SWT.FILL, true, true, 1, 1);
 				gd_table_2.heightHint = 93;
 				table_2.setLayoutData(gd_table_2);
 				table_2.setHeaderVisible(true);
 				table_2.setLinesVisible(true);
 				
 				TableColumn tableColumn_7 = new TableColumn(table_2, SWT.NONE);
 				tableColumn_7.setWidth(100);
 				tableColumn_7.setText("Машина");
 				
 				TableColumn tableColumn_8 = new TableColumn(table_2, SWT.NONE);
 				tableColumn_8.setWidth(126);
 				tableColumn_8.setText("Владелец");
 				
 				TableColumn tableColumn_9 = new TableColumn(table_2, SWT.NONE);
 				tableColumn_9.setWidth(149);
 				tableColumn_9.setText("Описание поломки");
 				
 				TableColumn tableColumn_10 = new TableColumn(table_2, SWT.NONE);
 				tableColumn_10.setWidth(100);
 				tableColumn_10.setText("Дата заказа");
 				
 				TableColumn tableColumn_12 = new TableColumn(table_2, SWT.NONE);
 				tableColumn_12.setWidth(116);
 				tableColumn_12.setText("Наличие гарантии");
 				
 				TableColumn tableColumn_13 = new TableColumn(table_2, SWT.NONE);
 				tableColumn_13.setWidth(100);
 				tableColumn_13.setText("Статус");
 				
 				Order.fillingTable(table_2);
 				
 				Composite composite_5 = new Composite(composite_4, SWT.NONE);
 				GridData gd_composite_5 = new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1);
 				gd_composite_5.widthHint = 679;
 				gd_composite_5.heightHint = 25;
 				composite_5.setLayoutData(gd_composite_5);
 				
 				Button btnNewButton_2 = new Button(composite_5, SWT.NONE);
 				btnNewButton_2.addSelectionListener(new SelectionAdapter() {
 					@Override
 					public void widgetSelected(SelectionEvent arg0) {
 						AddOrder ac = new AddOrder(shlAutoservice,SWT.DIALOG_TRIM);
 						ac.open();
 						table_2.removeAll();
 						Order.fillingTable(table_2);
 					}
 				});
 				btnNewButton_2.setBounds(0, 0, 75, 25);
 				btnNewButton_2.setText("Добавить");
 				
 				Button button_2 = new Button(composite_5, SWT.NONE);
 				button_2.addSelectionListener(new SelectionAdapter() {
 					@Override
 					public void widgetSelected(SelectionEvent arg0) {
 						if(rowForDelInOrder!=null)
 						{
 							Order.deleteTableItem(rowForDelInOrder.getText(0), rowForDelInOrder.getText(1));
 							table_2.removeAll();
 							Order.fillingTable(table_2);
 						}
 					}
 				});
 				button_2.setBounds(81, 0, 75, 25);
 				button_2.setText("Удалить");
 				
 				Button button_3 = new Button(composite_5, SWT.NONE);
 				button_3.addSelectionListener(new SelectionAdapter() {
 					@Override
 					public void widgetSelected(SelectionEvent arg0) {
 						if(rowForDelInOrder!=null)
 						{
 							CarInfo ci = new CarInfo(shlAutoservice,SWT.DIALOG_TRIM);
 							ci.open(rowForDelInOrder.getText(0), rowForDelInOrder.getText(1));
 						}	
 					}
 				});
 				button_3.setBounds(162, 0, 152, 25);
 				button_3.setText("Характеристики машины");
 				
 				Button button_4 = new Button(composite_5, SWT.NONE);
 				button_4.addSelectionListener(new SelectionAdapter() {
 					@Override
 					public void widgetSelected(SelectionEvent arg0) {
 						processOrder p = new processOrder(shlAutoservice,SWT.DIALOG_TRIM);
 						if(rowForDelInOrder!=null)
 						{
							p.open(rowForDelInOrder.getText(0), rowForDelInOrder.getText(1),rowForDelInOrder.getText(2),rowForDelInOrder.getText(3));
 							table_2.removeAll();
 							Order.fillingTable(table_2);
 							rowForDelInOrder=null;
 						}
 					}
 				});
 				button_4.setBounds(320, 0, 108, 25);
 				button_4.setText("Обработать заказ");
 				
 				Button button_5 = new Button(composite_5, SWT.NONE);
 				button_5.addSelectionListener(new SelectionAdapter() {
 					@Override
 					public void widgetSelected(SelectionEvent arg0) {
 						setStatus p = new setStatus(shlAutoservice,SWT.DIALOG_TRIM);
 						if(rowForDelInOrder!=null)
 						{
 							p.open(rowForDelInOrder.getText(0), rowForDelInOrder.getText(1),"order");
 							table_2.removeAll();
 							Order.fillingTable(table_2);
 							rowForDelInOrder=null;
 						}
 					}
 				});
 				button_5.setBounds(573, 0, 100, 25);
 				button_5.setText("Изменить статус");
 				
 				Button button_6 = new Button(composite_5, SWT.NONE);
 				button_6.addSelectionListener(new SelectionAdapter() {
 					@Override
 					public void widgetSelected(SelectionEvent arg0) {
 						OrderInfo oi = new OrderInfo(shlAutoservice,SWT.DIALOG_TRIM);
 						if(rowForDelInOrder!=null)
 						{
 							oi.open(rowForDelInOrder.getText(0), rowForDelInOrder.getText(1));
 						}
 						
 					}
 				});
 				button_6.setBounds(434, 0, 133, 25);
 				button_6.setText("Информация о заказе");
 				
 				final TableCursor cursor2 = new TableCursor(table_2, SWT.NONE);
 				final ControlEditor editor2 = new ControlEditor(cursor1);
 				
 				TabItem tabItem_3 = new TabItem(tabFolder, SWT.NONE);
 				tabItem_3.setText("Персонал");
 				
 				Composite composite_6 = new Composite(tabFolder, SWT.NONE);
 				tabItem_3.setControl(composite_6);
 				composite_6.setLayout(new GridLayout(1, false));
 				
 				cursor2.addSelectionListener(new SelectionAdapter() {
 					public void widgetSelected(SelectionEvent e) {
 						rowForDelInOrder = cursor2.getRow();
 						//table_2.setSelection(new TableItem[] { cursor1.getRow()});
 					}
 				});
 				
 				cursor1.addSelectionListener(new SelectionAdapter() {
 					public void widgetSelected(SelectionEvent e) {
 						rowForDelInCar = cursor1.getRow();
 						table_1.setSelection(new TableItem[] { cursor1.getRow()});
 					}
 					
 					
 					public void widgetDefaultSelected(SelectionEvent e) {
 						final Text text = new Text(cursor1, SWT.NONE);
 						TableItem row = cursor1.getRow();
 						int column = cursor1.getColumn();
 						if(column!=0)
 						{
 							text.setText(row.getText(column));
 							text.addKeyListener(new KeyAdapter() {
 								public void keyPressed(KeyEvent e) {
 									if (e.character == SWT.CR) {
 										TableItem row = cursor1.getRow();
 										String buff = row.getText(3);
 										int column = cursor1.getColumn();
 										row.setText(column, text.getText());
 										Car.setTableItem(buff, column, text.getText());
 										text.dispose();
 									}
 									if (e.character == SWT.ESC) {
 										text.dispose();
 									}
 								}
 							});
 							text.addFocusListener(new FocusAdapter() {
 								public void focusLost(FocusEvent e) {
 									text.dispose();
 							}
 						});
 						editor1.setEditor(text);
 						text.setFocus();
 						}
 					}
 				});
 				
 				editor1.grabHorizontal = true;
 				editor1.grabVertical = true;
 				
 				cursor1.addMouseListener(new MouseAdapter() {
 					public void mouseDown(MouseEvent e) {
 						final Text text = new Text(cursor1, SWT.NONE);
 						TableItem row = cursor1.getRow();
 						
 						int column = cursor1.getColumn();
 						if(column!=0)
 						{
 							text.setText(row.getText(column));
 							text.addKeyListener(new KeyAdapter() {
 								public void keyPressed(KeyEvent e) {
 									if (e.character == SWT.CR) {
 										TableItem row = cursor1.getRow();
 										String buff = row.getText(3);
 										int column = cursor1.getColumn();
 										row.setText(column, text.getText());
 										Car.setTableItem(buff, column, text.getText());
 										text.dispose();
 									}
 									if (e.character == SWT.ESC) {
 										text.dispose();
 									}
 								}
 							});
 							text.addFocusListener(new FocusAdapter() {
 								public void focusLost(FocusEvent e) {
 									text.dispose();
 								}
 							});
 							editor1.setEditor(text);
 							text.setFocus();
 						}
 					}
 				});
 				
 				table_3 = new Table(composite_6, SWT.BORDER | SWT.FULL_SELECTION);
 				GridData gd_table_3 = new GridData(SWT.CENTER, SWT.FILL, true, true, 1, 1);
 				gd_table_3.widthHint = 619;
 				table_3.setLayoutData(gd_table_3);
 				table_3.setHeaderVisible(true);
 				table_3.setLinesVisible(true);
 				
 				TableColumn tableColumn_11 = new TableColumn(table_3, SWT.NONE);
 				tableColumn_11.setWidth(100);
 				tableColumn_11.setText("Фамилия");
 				
 				TableColumn tableColumn_14 = new TableColumn(table_3, SWT.NONE);
 				tableColumn_14.setWidth(100);
 				tableColumn_14.setText("Имя");
 				
 				TableColumn tableColumn_15 = new TableColumn(table_3, SWT.NONE);
 				tableColumn_15.setWidth(100);
 				tableColumn_15.setText("Отчество");
 				
 				TableColumn tableColumn_16 = new TableColumn(table_3, SWT.NONE);
 				tableColumn_16.setWidth(125);
 				tableColumn_16.setText("Должность");
 				
 				TableColumn tableColumn_17 = new TableColumn(table_3, SWT.NONE);
 				tableColumn_17.setWidth(100);
 				tableColumn_17.setText("Телефон");
 				
 				TableColumn tableColumn_18 = new TableColumn(table_3, SWT.NONE);
 				tableColumn_18.setWidth(100);
 				tableColumn_18.setText("Статус");
 				
 				Composite composite_7 = new Composite(composite_6, SWT.NONE);
 				GridData gd_composite_7 = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
 				gd_composite_7.widthHint = 297;
 				composite_7.setLayoutData(gd_composite_7);
 				
 				Button btnNewButton_3 = new Button(composite_7, SWT.NONE);
 				btnNewButton_3.addSelectionListener(new SelectionAdapter() {
 					@Override
 					public void widgetSelected(SelectionEvent arg0) {
 						AddStaff a = new AddStaff(shlAutoservice,SWT.DIALOG_TRIM);
 						a.open();
 						table_3.removeAll();
 						Staff.fillingTable(table_3);	
 						rowForChangeInStaff=null;
 					}
 				});
 				btnNewButton_3.setBounds(0, 0, 75, 25);
 				btnNewButton_3.setText("Добавить");
 				
 				Button button_7 = new Button(composite_7, SWT.NONE);
 				button_7.addSelectionListener(new SelectionAdapter() {
 					@Override
 					public void widgetSelected(SelectionEvent arg0) {
 						if(rowForChangeInStaff!=null)
 						{
 							setStatus s = new setStatus(shlAutoservice,SWT.DIALOG_TRIM);
 							s.open(null, rowForChangeInStaff.getText(4), "staff");
 							table_3.removeAll();
 							Staff.fillingTable(table_3);
 							rowForChangeInStaff=null;
 						}
 						
 					}
 				});
 				button_7.setBounds(81, 0, 103, 25);
 				button_7.setText("Изменить статус");
 				
 				Button button_8 = new Button(composite_7, SWT.NONE);
 				button_8.addSelectionListener(new SelectionAdapter() {
 					@Override
 					public void widgetSelected(SelectionEvent arg0) {
 						if(rowForChangeInStaff!=null)
 						{
 							StaffOrderHistory soh = new StaffOrderHistory(shlAutoservice,SWT.DIALOG_TRIM);
 							soh.open(rowForChangeInStaff.getText(4));
 						}
 					}
 				});
 				button_8.setBounds(190, 0, 103, 25);
 				button_8.setText("История заказов");
 				
 				TabItem tabItem_4 = new TabItem(tabFolder, SWT.NONE);
 				tabItem_4.setText("Услуги");
 				
 				Composite composite_8 = new Composite(tabFolder, SWT.NONE);
 				tabItem_4.setControl(composite_8);
 				composite_8.setLayout(new GridLayout(1, false));
 				
 				Staff.fillingTable(table_3);
 				
 				final TableCursor cursor3 = new TableCursor(table_3, SWT.NONE);
 				final ControlEditor editor3 = new ControlEditor(cursor3);
 				
 				cursor3.addSelectionListener(new SelectionAdapter() {
 					public void widgetSelected(SelectionEvent e) {
 						rowForChangeInStaff = cursor3.getRow();
 						
 						table_3.setSelection(new TableItem[] { cursor3.getRow()});
 					}
 					public void widgetDefaultSelected(SelectionEvent e) {
 						final Text text = new Text(cursor3, SWT.NONE);
 						TableItem row = cursor3.getRow();
 						int column = cursor3.getColumn();
 						text.setText(row.getText(column));
 						text.addKeyListener(new KeyAdapter() {
 							public void keyPressed(KeyEvent e) {
 								if (e.character == SWT.CR) {
 									TableItem row = cursor3.getRow();
 									String buff = row.getText(4);
 									int column = cursor3.getColumn();
 									row.setText(column, text.getText());
 									Staff.setTableItem(buff, column, text.getText());
 									text.dispose();
 								}
 								if (e.character == SWT.ESC) {
 									text.dispose();
 								}
 							}
 						});
 						text.addFocusListener(new FocusAdapter() {
 							public void focusLost(FocusEvent e) {
 								text.dispose();
 							}
 						});
 						editor3.setEditor(text);
 						text.setFocus();
 					}
 				});
 				
 				editor3.grabHorizontal = true;
 				editor3.grabVertical = true;
 				
 				cursor3.addMouseListener(new MouseAdapter() {
 					public void mouseDown(MouseEvent e) {
 						final Text text = new Text(cursor3, SWT.NONE);
 						TableItem row = cursor3.getRow();
 						int column = cursor3.getColumn();
 						text.setText(row.getText(column));
 						text.addKeyListener(new KeyAdapter() {
 							public void keyPressed(KeyEvent e) {
 								if (e.character == SWT.CR) {
 									TableItem row = cursor3.getRow();
 									String buff = row.getText(4);
 									int column = cursor3.getColumn();
 									row.setText(column, text.getText());
 									Staff.setTableItem(buff, column, text.getText());
 									text.dispose();
 								}
 								if (e.character == SWT.ESC) {
 									text.dispose();
 								}
 							}
 						});
 						text.addFocusListener(new FocusAdapter() {
 							public void focusLost(FocusEvent e) {
 								text.dispose();
 							}
 						});
 						editor3.setEditor(text);
 						text.setFocus();
 					}
 				});
 				
 				table_4 = new Table(composite_8, SWT.BORDER | SWT.FULL_SELECTION);
 				table_4.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, true, 1, 1));
 				table_4.setHeaderVisible(true);
 				table_4.setLinesVisible(true);
 				
 				TableColumn tableColumn_19 = new TableColumn(table_4, SWT.NONE);
 				tableColumn_19.setWidth(100);
 				tableColumn_19.setText("Название");
 				
 				TableColumn tblclmnNewColumn_7 = new TableColumn(table_4, SWT.NONE);
 				tblclmnNewColumn_7.setWidth(100);
 				tblclmnNewColumn_7.setText("Цена");
 				
 				TableColumn tblclmnNewColumn_8 = new TableColumn(table_4, SWT.NONE);
 				tblclmnNewColumn_8.setWidth(100);
 				tblclmnNewColumn_8.setText("Статус");
 				
 				Composite composite_9 = new Composite(composite_8, SWT.NONE);
 				GridData gd_composite_9 = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
 				gd_composite_9.widthHint = 241;
 				composite_9.setLayoutData(gd_composite_9);
 				
 				Button btnNewButton_4 = new Button(composite_9, SWT.NONE);
 				btnNewButton_4.addSelectionListener(new SelectionAdapter() {
 					@Override
 					public void widgetSelected(SelectionEvent arg0) {
 						AddService as = new AddService(shlAutoservice,SWT.DIALOG_TRIM);
 						as.open();
 						table_4.removeAll();
 						Services.fillingTable(table_4);
 					}
 				});
 				btnNewButton_4.setBounds(0, 0, 75, 25);
 				btnNewButton_4.setText("Добавить");
 				
 				Button btnNewButton_5 = new Button(composite_9, SWT.NONE);
 				btnNewButton_5.addSelectionListener(new SelectionAdapter() {
 					@Override
 					public void widgetSelected(SelectionEvent arg0) {
 						if(rowForChangeInServices!=null)
 						{
 							Services.setActive(rowForChangeInServices.getText(0));
 							table_4.removeAll();
 							Services.fillingTable(table_4);
 							rowForChangeInServices=null;
 						}
 					}
 				});
 				btnNewButton_5.setBounds(81, 0, 75, 25);
 				btnNewButton_5.setText("Активна");
 				
 				Button btnNewButton_6 = new Button(composite_9, SWT.NONE);
 				btnNewButton_6.addSelectionListener(new SelectionAdapter() {
 					@Override
 					public void widgetSelected(SelectionEvent arg0) {
 						Services.setPassive(rowForChangeInServices.getText(0));
 						table_4.removeAll();
 						Services.fillingTable(table_4);
 						rowForChangeInServices=null;
 					}
 				});
 				btnNewButton_6.setBounds(162, 0, 75, 25);
 				btnNewButton_6.setText("Неактивна");
 				
 				Services.fillingTable(table_4);
 				
 				final TableCursor cursor4 = new TableCursor(table_4, SWT.NONE);
 				final ControlEditor editor4 = new ControlEditor(cursor4);
 				
 				cursor4.addSelectionListener(new SelectionAdapter() {
 					public void widgetSelected(SelectionEvent e) {
 						rowForChangeInServices = cursor4.getRow();
 						
 						table_4.setSelection(new TableItem[] { cursor4.getRow()});
 					}
 					public void widgetDefaultSelected(SelectionEvent e) {
 						final Text text = new Text(cursor4, SWT.NONE);
 						TableItem row = cursor4.getRow();
 						int column = cursor4.getColumn();
 						if(column!=2)
 						{
 						text.setText(row.getText(column));
 						text.addKeyListener(new KeyAdapter() {
 							public void keyPressed(KeyEvent e) {
 								if (e.character == SWT.CR) {
 									TableItem row = cursor4.getRow();
 									String buff = row.getText(0);
 									int column = cursor4.getColumn();
 									row.setText(column, text.getText());
 									Services.setTableItem(buff, column, text.getText());
 									text.dispose();
 								}
 								if (e.character == SWT.ESC) {
 									text.dispose();
 								}
 							}
 						});
 						text.addFocusListener(new FocusAdapter() {
 							public void focusLost(FocusEvent e) {
 								text.dispose();
 							}
 						});
 						editor4.setEditor(text);
 						text.setFocus();
 					}}
 				});
 				
 				editor4.grabHorizontal = true;
 				editor4.grabVertical = true;
 				
 				cursor4.addMouseListener(new MouseAdapter() {
 					public void mouseDown(MouseEvent e) {
 						final Text text = new Text(cursor4, SWT.NONE);
 						TableItem row = cursor4.getRow();
 						int column = cursor4.getColumn();
 						if(column!=2)
 						{
 						text.setText(row.getText(column));
 						text.addKeyListener(new KeyAdapter() {
 							public void keyPressed(KeyEvent e) {
 								if (e.character == SWT.CR) {
 									TableItem row = cursor4.getRow();
 									String buff = row.getText(0);
 									int column = cursor4.getColumn();
 									row.setText(column, text.getText());
 									Services.setTableItem(buff, column, text.getText());
 									text.dispose();
 								}
 								if (e.character == SWT.ESC) {
 									text.dispose();
 								}
 							}
 						});
 						text.addFocusListener(new FocusAdapter() {
 							public void focusLost(FocusEvent e) {
 								text.dispose();
 							}
 						});
 						editor4.setEditor(text);
 						text.setFocus();
 					}
 					}
 				});
 				
 				
 
 	}
 }
