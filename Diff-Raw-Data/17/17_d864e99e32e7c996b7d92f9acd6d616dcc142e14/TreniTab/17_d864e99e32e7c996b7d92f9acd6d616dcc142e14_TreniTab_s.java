 package interfaccia;
 
 import interfaccia.AlertShell;
 import interfaccia.MainShell;
 
 import java.util.Arrays;
 import java.util.List;
 import java.util.SortedMap;
 import java.util.TreeMap;
 
 import javax.persistence.PersistenceException;
 
 import modelloTreni.ClassePosto;
 import modelloTreni.Tipologia;
 import modelloTreni.TrainManager;
 import modelloTreni.Treno;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.layout.RowData;
 import org.eclipse.swt.layout.RowLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Spinner;
 import org.eclipse.swt.widgets.TabFolder;
 import org.eclipse.swt.widgets.TabItem;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.TableItem;
 import org.eclipse.swt.widgets.Text;
 
 public class TreniTab {
 	private TabItem train;
 	private Composite group, buttonGroup, buttonGroup2;
 	private Table table;
 	private Label id, posti;
 	private Button plus, minus, edit, save;
 	private Text textid, textfirst, textsecond;
 	private Combo type;
 	MainShell mainWindow = MainShell.getMainShell();
 	TrainManager manager = TrainManager.getInstance();
 
 	public TreniTab(TabFolder parent) {
 		train = new TabItem(parent, SWT.NONE);
 
 		group = new Group(parent, SWT.NONE);
 		group.setLayout(new GridLayout(2, false));
 
 		createTrainTable();
 		createTrainDataArea();
 		createButtonArea();
 
 		createPlusButtonListener();
 		createMinusButtonListener();
 		createEditButtonListener();
 		createSaveButtonListener();
 		createTrainTableListener();
 
 		train.setControl(group);
 
 	}
 
 	private void createButtonArea() {
 		buttonGroup2 = new Composite(group, SWT.NONE);
 		buttonGroup2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false,
 				false));
 		buttonGroup2.setLayout(new GridLayout(2, true));
 
 		plus = new Button(buttonGroup2, SWT.PUSH);
 		plus.setText("+");
 
 		minus = new Button(buttonGroup2, SWT.PUSH);
 		minus.setText("-");
 		minus.setEnabled(false);
 
 	}
 
 	private void createTrainDataArea() {
 		// Inizializzazione dell'area
 		buttonGroup = new Composite(group, SWT.NONE);
 		buttonGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 		buttonGroup.setLayout(new FormLayout());
 
 		// "Costanti"
 		FormAttachment leftMargin = new FormAttachment(10);
 		FormAttachment topMargin = new FormAttachment(10);
 
 		// Inizializzazione dei vari widget dell'area
 		id = new Label(buttonGroup, SWT.NONE);
 		id.setText("Id: ");
 		FormData idLayoutData = new FormData();
 		idLayoutData.left = leftMargin;
 		idLayoutData.top = topMargin;
 		id.setLayoutData(idLayoutData);
 
 		textid = new Text(buttonGroup, SWT.NONE);
 		FormData textIdLayoutData = new FormData();
 		textIdLayoutData.left = new FormAttachment(id, 15);
 		textIdLayoutData.top = topMargin;
 		textid.setLayoutData(textIdLayoutData);
 		textid.setEnabled(false);
 
 		posti = new Label(buttonGroup, SWT.NONE);
 		posti.setText("Posti: ");
 		FormData postiLayoutData = new FormData();
 		postiLayoutData.left = leftMargin;
 		postiLayoutData.top = new FormAttachment(id, 10);
 		posti.setLayoutData(postiLayoutData);
 
 		textfirst = new Text(buttonGroup, SWT.NONE);
 		textfirst.setText("I classe");
 		textfirst.setToolTipText("Numero posti I classe");
 		FormData textfirstLayoutData = new FormData();
 		textfirstLayoutData.left = new FormAttachment(posti, 15);
 		textfirstLayoutData.top = postiLayoutData.top;
 		textfirst.setLayoutData(textfirstLayoutData);
 		textfirst.setEnabled(false);
 
 		textsecond = new Text(buttonGroup, SWT.NONE);
 		textsecond.setText("II classe");
 		textsecond.setToolTipText("Numero posti II classe");
 		FormData textsecondLayoutData = new FormData();
 		textsecondLayoutData.left = new FormAttachment(posti, 15);
 		textsecondLayoutData.top = new FormAttachment(textfirst, 10);
 		textsecond.setLayoutData(textsecondLayoutData);
 		textsecond.setEnabled(false);
 
 		type = new Combo(buttonGroup, SWT.DROP_DOWN);
 		type.setText("Tipologia");
 		type.setItems(new String[] { Tipologia.REGIONALE.toString(),
 				Tipologia.INTERCITY.toString(), Tipologia.EUROSTAR.toString() });
 		FormData typeLayoutData = new FormData();
 		typeLayoutData.left = leftMargin;
 		typeLayoutData.top = new FormAttachment(textsecond, 10);
 		type.setLayoutData(typeLayoutData);
 		type.setEnabled(false);
 
 		edit = new Button(buttonGroup, SWT.PUSH);
 		edit.setSize(50, 30);
 		edit.setText("Modifica");
 		FormData editLayoutData = new FormData();
 		editLayoutData.top = new FormAttachment(type, 50);
 		editLayoutData.left = leftMargin;
 		edit.setLayoutData(editLayoutData);
 		edit.setEnabled(false);
 
 		save = new Button(buttonGroup, SWT.PUSH);
 		save.setSize(50, 30);
 		save.setText("Salva");
 		FormData saveLayoutData = new FormData();
 		saveLayoutData.top = editLayoutData.top;
 		saveLayoutData.left = new FormAttachment(edit, 20);
 		save.setLayoutData(saveLayoutData);
 		save.setVisible(false);
 	}
 
 	private void createPlusButtonListener() {
 		plus.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event) {
 				final Shell popup = new Shell(mainWindow.getShell(), 65616);
 				popup.setText("Nuovo Treno");
 
 				// FIXME: cancellare
 				// RowLayout popuplayout = new RowLayout(SWT.VERTICAL);
 				// popuplayout.pack = false;
 
 				FormAttachment leftMargin = new FormAttachment(5);
 				FormAttachment topMargin = new FormAttachment(5);
 
 				Label postiPrimaClasse = new Label(popup, SWT.NONE);
 				postiPrimaClasse.setText("Numero posti I classe: ");
 				FormData postiPrimaClasseLayoutData = new FormData();
 				postiPrimaClasseLayoutData.left = leftMargin;
 				postiPrimaClasseLayoutData.top = topMargin;
 				postiPrimaClasse.setLayoutData(postiPrimaClasseLayoutData);
 
 				final Spinner newfirst = new Spinner(popup, SWT.WRAP);
 				newfirst.setMinimum(0);
 				newfirst.setMaximum(100);
 				newfirst.setSelection(0);
 				FormData newfirstLayoutData = new FormData();
 				newfirstLayoutData.top = postiPrimaClasseLayoutData.top;
 				newfirstLayoutData.left = new FormAttachment(postiPrimaClasse,
 						10);
 				newfirst.setLayoutData(newfirstLayoutData);
 
 				Label postiSecondaClasse = new Label(popup, SWT.NONE);
 				postiSecondaClasse.setText("Numero posti II classe: ");
 				FormData postiSecondaClasseLayoutData = new FormData();
 				postiSecondaClasseLayoutData.left = leftMargin;
 				postiSecondaClasseLayoutData.top = new FormAttachment(
 						postiPrimaClasse, 10);
 				postiSecondaClasse.setLayoutData(postiSecondaClasseLayoutData);
 
 				final Spinner newsecond = new Spinner(popup, SWT.WRAP);
 				newsecond.setMinimum(0);
 				newsecond.setMaximum(100);
 				newsecond.setSelection(0);
 				FormData newsecondLayoutData = new FormData();
 				newsecondLayoutData.top = postiSecondaClasseLayoutData.top;
 				newsecondLayoutData.left = new FormAttachment(
 						postiSecondaClasse, 10);
 				newsecond.setLayoutData(newsecondLayoutData);
 
 				// FIXME: cancellare
 				// final Text newfirst = new Text(popup, SWT.SINGLE);
 				// newfirst.setText("Posti I classe");
 				// newfirst.setLayoutData(new RowData(200, 20));
 				//
 				// final Text newsecond = new Text(popup, SWT.SINGLE);
 				// newsecond.setText("Posti II classe");
 
 				Label tipo = new Label(popup, SWT.NONE);
 				tipo.setText("Tipologia");
 				FormData tipoLayoutData = new FormData();
 				tipoLayoutData.left = leftMargin;
 				tipoLayoutData.top = new FormAttachment(newsecond, 10);
 				tipo.setLayoutData(tipoLayoutData);
 
 				final Combo newtype = new Combo(popup, SWT.DROP_DOWN);
 				newtype.setItems(new String[] { Tipologia.REGIONALE.toString(),
 						Tipologia.INTERCITY.toString(),
 						Tipologia.EUROSTAR.toString() });
 				FormData newtypeLayoutData = new FormData();
 				newtypeLayoutData.left = new FormAttachment(tipo, 10);
 				newtypeLayoutData.top = tipoLayoutData.top;
 				newtype.setLayoutData(newtypeLayoutData);
 
 				final Button ok = new Button(popup, SWT.PUSH);
 				ok.setSize(50, 30);
 				ok.setText("Ok");
 				ok.setEnabled(false);
 				FormData okLayoutData = new FormData();
 				okLayoutData.left = leftMargin;
 				okLayoutData.top = new FormAttachment(newtype, 10);
 				ok.setLayoutData(okLayoutData);
 
 				newtype.addSelectionListener(new SelectionAdapter() {
 					public void widgetSelected(SelectionEvent event) {
 						if (newtype.getText() != null) {
 							ok.setEnabled(true);
 						} else {
 							ok.setEnabled(false);
 						}
 					}
 				});
 
 				ok.addSelectionListener(new SelectionAdapter() {
 					@SuppressWarnings("unused")
 					public void widgetSelected(SelectionEvent event) {
 						SortedMap<ClassePosto, Integer> numPostiPerClasse = new TreeMap<ClassePosto, Integer>();
 
 						numPostiPerClasse.put(ClassePosto.SECONDACLASSE,
 								newsecond.getSelection());
 						numPostiPerClasse.put(ClassePosto.PRIMACLASSE,
 								newfirst.getSelection());
 
 						Tipologia tipologia;
 						if (newtype.getText().equals(
 								Tipologia.EUROSTAR.toString())) {
 							tipologia = Tipologia.EUROSTAR;
 						} else if (newtype.getText().equals(
 								Tipologia.REGIONALE.toString())) {
 							tipologia = Tipologia.REGIONALE;
 						} else {
 							tipologia = Tipologia.INTERCITY;
 						}
 						try {
 							Treno treno = manager.createTreno(
 									numPostiPerClasse, tipologia);
 						} catch (NumberFormatException e) {
 							AlertShell alert = new AlertShell(
 									"Inserire un numero di posti valido");
 						}
 						table.removeAll();
 						loadTable();
 						orderTable();
 						minus.setEnabled(false);
 						edit.setEnabled(false);
 						popup.close();
 					}
 				});
 
 				FormLayout popupLayout = new FormLayout();
 				popupLayout.marginRight = 5;
 				popupLayout.marginLeft = 5;
 				popupLayout.marginBottom = 5;
 				popupLayout.marginTop = 5;
 				popupLayout.spacing = 10;
 				popup.setLayout(popupLayout);
 				popup.pack();
 				popup.open();
 
 			}
 		});
 	}
 
 	private void createMinusButtonListener() {
 		minus.addSelectionListener(new SelectionAdapter() {
 			@SuppressWarnings("unused")
 			public void widgetSelected(SelectionEvent event) {
 				try {
 					TableItem[] selezione = table.getSelection();
 					manager.removeTreno(Integer.parseInt(selezione[0]
 							.getText(1)));
 					table.removeAll();
 					loadTable();
 					orderTable();
 					minus.setEnabled(false);
 					edit.setEnabled(false);
 				} catch (PersistenceException e) {
 					AlertShell alert = new AlertShell(
 							"E' impossibile eliminare questo treno, in quanto � utlizzato in una Istanza."
 									+ "\nEliminare prima tale Istanza.");
 				}
 			}
 		});
 	}
 
 	private void createEditButtonListener() {
 		edit.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event) {
 				if (edit.getText().equals("Modifica")) {
 					type.setEnabled(true);
 					save.setVisible(true);
 					edit.setText("Annulla");
 				}
 
 				else if (edit.getText().equals("Annulla")) {
 					TableItem[] selezione = table.getSelection();
 					loadFields(selezione[0]);
 					type.setEnabled(false);
 					save.setVisible(false);
 					edit.setText("Modifica");
 				}
 
 			}
 		});
 	}
 
 	private void createSaveButtonListener() {
 		save.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event) {
 				Tipologia tipologia;
 				SortedMap<ClassePosto, Integer> posti = new TreeMap<ClassePosto, Integer>();
 				if (type.getText().equals(Tipologia.EUROSTAR.toString())) {
 					tipologia = Tipologia.EUROSTAR;
 					posti.put(ClassePosto.PRIMACLASSE,
 							Integer.parseInt(textfirst.getText()));
 				} else if (type.getText()
 						.equals(Tipologia.REGIONALE.toString())) {
 					tipologia = Tipologia.REGIONALE;
 					posti.put(ClassePosto.PRIMACLASSE, 0);
 				} else {
 					tipologia = Tipologia.INTERCITY;
 					posti.put(ClassePosto.PRIMACLASSE,
 							Integer.parseInt(textfirst.getText()));
 				}
 				posti.put(ClassePosto.SECONDACLASSE,
 						Integer.parseInt(textsecond.getText()));
 
 				manager.updateTreno(Integer.parseInt(textid.getText()),
 						tipologia);
 				type.setEnabled(false);
 				save.setVisible(false);
 				edit.setText("Modifica");
 				TableItem[] selezione = table.getSelection();
 				int index = table.indexOf(selezione[0]);
 				table.removeAll();
 				loadTable();
 				orderTable();
 				TableItem modificato = table.getItem(index);
 				table.select(index);
 				loadFields(modificato);
 
 			}
 		});
 
 	}
 
 	private void createTrainTable() {
 		table = new Table(group, SWT.SINGLE);
 		table.setLinesVisible(true);
 		table.setHeaderVisible(true);
 		TableColumn column0 = new TableColumn(table, SWT.NONE);
 		column0.setText("Treno");
 		table.getColumn(0).setWidth(400);
 		TableColumn column1 = new TableColumn(table, SWT.LEFT);
 		column1.setText("Id");
 		table.getColumn(1).setWidth(30);
 		table.getColumn(1).setResizable(false);
 
 		GridData tableGridData = new GridData(SWT.FILL, SWT.FILL, true, true);
 		// tableGridData.heightHint = 350;
 		// tableGridData.widthHint = 350;
 		// tableGridData.minimumHeight = 300;
 		// tableGridData.minimumWidth = 200;
 		table.setLayoutData(tableGridData);
 
 		loadTable();
 		orderTable();
 	}
 
 	private void createTrainTableListener() {
 		table.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event) {
 				// TODO: Modifica i valori nella detail quando viene selezionata
 				// una nuova riga e abilita il tasto minus
 				minus.setEnabled(true);
 				edit.setEnabled(true);
 				textfirst.setEnabled(true);
 				textsecond.setEnabled(true);
 				type.setEnabled(true);
 				TableItem[] selezione = table.getSelection();
 				loadFields(selezione[0]);
 				textfirst.setEnabled(false);
 				textsecond.setEnabled(false);
 				type.setEnabled(false);
 
 			}
 		});
 	}
 
 	@SuppressWarnings("unchecked")
 	private void loadTable() {
 		List<Treno> treni = (List<Treno>) manager
 				.doQuery("select treno from Treno treno");
 
 		for (int i = 0; i < treni.size(); i++) {
 			TableItem item = new TableItem(table, SWT.NONE);
 			item.setText(0, treni.get(i).getTipo().toString());
 			item.setText(1, treni.get(i).getId().toString());
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	private void loadFields(TableItem selezione) {
 		textid.setText(selezione.getText(1));
 		type.setText(selezione.getText(0));
 		int trenoid = Integer.parseInt(selezione.getText(1));
 		List<Treno> treno = (List<Treno>) manager
 				.doQuery("select treno from Treno treno where id = '" + trenoid
 						+ "'");
 		textfirst.setText(String.valueOf(treno.get(0).getNumPosti(
 				ClassePosto.PRIMACLASSE)));
 		textsecond.setText(String.valueOf(treno.get(0).getNumPosti(
 				ClassePosto.SECONDACLASSE)));
 
 	}
 
 	private void orderTable() {
 		TableItem[] items = table.getItems();
 		int[] array = new int[items.length];
 		for (int i = 0; i < items.length; i++) {
			if (items[i].getText(0).equals("EUROSTAR")) {
 				array[i] = 1;
			} else if (items[i].getText(0).equals("INTERCITY")) {
 				array[i] = 2;
			} else if (items[i].getText(0).equals("REGIONALE")) {
 				array[i] = 3;
 			}
 		}
 		Arrays.sort(array);
 		for (int i = 0; i < items.length; i++) {
 			for (int j = 0; j < items.length; j++) {
				if (array[i] == 1 && items[j].getText(0).equals("EUROSTAR")) {
 					TableItem item = new TableItem(table, SWT.NONE);
 					item.setText(0, items[j].getText(0));
 					item.setText(1, items[j].getText(1));
 					items[j].setText(0, "NULL");
 				} else if (array[i] == 2
						&& items[j].getText(0).equals("INTERCITY")) {
 					TableItem item = new TableItem(table, SWT.NONE);
 					item.setText(0, items[j].getText(0));
 					item.setText(1, items[j].getText(1));
 					items[j].setText(0, "NULL");
 
 				} else if (array[i] == 3
						&& items[j].getText(0).equals("REGIONALE")) {
 					TableItem item = new TableItem(table, SWT.NONE);
 					item.setText(0, items[j].getText(0));
 					item.setText(1, items[j].getText(1));
 					items[j].setText(0, "NULL");
 				}
 			}
 		}
 		table.remove(0, table.indexOf(items[(items.length) - 1]));
 	}
 
 	public TabItem getTab() {
 		return train;
 	}
 
 }
