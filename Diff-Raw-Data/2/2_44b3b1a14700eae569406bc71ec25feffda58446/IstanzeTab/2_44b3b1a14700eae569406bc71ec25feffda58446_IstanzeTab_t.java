 package interfaccia;
 
 
 import java.util.List;
 
 import javax.persistence.PersistenceException;
 
 import modelloTreni.IstanzaTreno;
 
 import modelloTreni.TrainManager;
 
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.DisposeEvent;
 import org.eclipse.swt.events.DisposeListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.MessageBox;
 import org.eclipse.swt.widgets.TabFolder;
 import org.eclipse.swt.widgets.TabItem;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.TableItem;
 
 
 public class IstanzeTab {
 	private TabItem tabIstanze;
 	private Composite tabIstanzeGroup, istanzeButtonGroup;
 	private Table tabellaIstanze;
 	private Button aggiungiIstanza, rimuoviIstanza, modificaIstanza;
 	
 	MainShell mainWindow = MainShell.getMainShell();
 	TrainManager manager = TrainManager.getInstance();
 
 	public IstanzeTab(TabFolder parent) {
 		tabIstanze = new TabItem(parent, SWT.NONE);
 		tabIstanzeGroup = new Composite(parent, SWT.NONE);
 
 		createTabellaIstanze();
 		createIstanzaButtonGroup();
 		createTabellaIstanzeListener();
 		createAggiungiIstanzaListener();
 		createRimuoviIstanzaListener();
 		createModificaIstanzaListener();
 
 		tabIstanzeGroup.setLayout(new GridLayout(1, false));
 		tabIstanze.setControl(tabIstanzeGroup);
 	}
 
 	
 
 	private void createModificaIstanzaListener() {
 		modificaIstanza.addSelectionListener(new SelectionAdapter() {
 
 			public void widgetSelected(SelectionEvent event) {
 				TableItem[] selezione = tabellaIstanze.getSelection();
 				DialogModificaIstanze dialog = new DialogModificaIstanze(selezione[0]);
 				dialog.showDialog();
 				dialog.getDialog().addDisposeListener(new DisposeListener() {
 					
 					public void widgetDisposed(DisposeEvent e) {
 						loadTabellaIstanze();
 						SwtUtil.orderTable(tabellaIstanze, 0);
 						tabellaIstanze.select(0);
 					}
 				});
 			}	
 		});
 	}
 
 	private void createRimuoviIstanzaListener() {
 		rimuoviIstanza.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event) {
 				try {
 					TableItem[] selezione = tabellaIstanze.getSelection();
 					int idIstanza = Integer.parseInt(selezione[0].getText(0));
 					manager.removeIstanza(idIstanza);
					modificaIstanza.setEnabled(false);
 					loadTabellaIstanze();
 					SwtUtil.orderTable(tabellaIstanze, 0);
 				} catch (PersistenceException e) {
 					MessageBox alert = new MessageBox(mainWindow.getShell(),
 							SWT.ICON_ERROR | SWT.OK);
 					alert.setMessage("E' impossibile eliminare questa Istanza, in quanto contiene dei posti già prenotati.");
 					alert.open();
 				}
 				rimuoviIstanza.setEnabled(false);
 			}
 		});
 	}
 
 	private void createAggiungiIstanzaListener() {
 		aggiungiIstanza.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent event) {
 				DialogAggiungiIstanze d = new DialogAggiungiIstanze();
 				d.showDialog();
 				d.getDialog().addDisposeListener(new DisposeListener() {
 
 					public void widgetDisposed(DisposeEvent e) {
 
 						loadTabellaIstanze();
 						SwtUtil.orderTable(tabellaIstanze, 0);
 					}
 				});
 			}
 		});
 
 	}
 
 	private void createTabellaIstanzeListener() {
 		tabellaIstanze.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event) {
 				modificaIstanza.setEnabled(true);
 				rimuoviIstanza.setEnabled(true);			}
 		});
 	}
 
 	private void createIstanzaButtonGroup() {
 		istanzeButtonGroup = new Composite(tabIstanzeGroup, SWT.NONE);
 		istanzeButtonGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
 				false, false));
 		istanzeButtonGroup.setLayout(new GridLayout(3, true));
 
 		aggiungiIstanza = new Button(istanzeButtonGroup, SWT.PUSH);
 		aggiungiIstanza.setText("Aggiungi Istanza");
 
 		rimuoviIstanza = new Button(istanzeButtonGroup, SWT.PUSH);
 		rimuoviIstanza.setText("Elimina Istanza");
 		rimuoviIstanza.setEnabled(false);
 		
 		modificaIstanza = new Button(istanzeButtonGroup, SWT.PUSH);
 		modificaIstanza.setText("Modifica Istanza");
 		modificaIstanza.setEnabled(false);
 	}
 
 	
 
 	private void createTabellaIstanze() {
 		tabellaIstanze = new Table(tabIstanzeGroup, SWT.SINGLE);
 		tabellaIstanze.setLinesVisible(true);
 		tabellaIstanze.setHeaderVisible(true);
 		TableColumn columnIdIstanza = new TableColumn(tabellaIstanze, SWT.LEFT);
 		columnIdIstanza.setText("Id Istanza");
 		TableColumn columnPartenza = new TableColumn(tabellaIstanze, SWT.LEFT);
 		columnPartenza.setText("Partenza");
 		TableColumn columnOraPartenza = new TableColumn(tabellaIstanze, SWT.LEFT);
 		columnOraPartenza.setText("Ora Partenza");
 		TableColumn columnArrivo = new TableColumn(tabellaIstanze, SWT.LEFT);
 		columnArrivo.setText("Arrivo");
 		TableColumn columnOraArrivo = new TableColumn(tabellaIstanze, SWT.LEFT);
 		columnOraArrivo.setText("Ora Arrivo");
 		TableColumn columnIdTreno = new TableColumn(tabellaIstanze, SWT.LEFT);
 		columnIdTreno.setText("Id Treno");
 		TableColumn columnTipo = new TableColumn(tabellaIstanze, SWT.LEFT);
 		columnTipo.setText("Tipo");
 		TableColumn columnData = new TableColumn(tabellaIstanze, SWT.LEFT);
 		columnData.setText("Data");
 		tabellaIstanze.setLayout(new FillLayout());
 		tabellaIstanze.getColumn(0).setWidth(60);
 		tabellaIstanze.getColumn(1).setWidth(100);
 		tabellaIstanze.getColumn(2).setWidth(100);
 		tabellaIstanze.getColumn(3).setWidth(100);
 		tabellaIstanze.getColumn(4).setWidth(100);
 		tabellaIstanze.getColumn(5).setWidth(50);
 		tabellaIstanze.getColumn(6).setWidth(100);
 		tabellaIstanze.getColumn(7).setWidth(100);
 
 		tabellaIstanze.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
 				true));
 
 		loadTabellaIstanze();
 		SwtUtil.orderTable(tabellaIstanze, 0);
 	}
 
 	private void loadTabellaIstanze() {
 		tabellaIstanze.removeAll();
 
 		List<IstanzaTreno> istanze = manager.getIstanzeTreno();
 
 		for (int i = 0; i < istanze.size(); i++) {
 			TableItem item = new TableItem(tabellaIstanze, SWT.NONE);
 			item.setText(0, istanze.get(i).getId().toString());
 			item.setText(1, istanze.get(i).getCorsa().getFermataPartenza()
 					.getStazioneTratta().getStazione().getNome());
 			item.setText(2, istanze.get(i).getCorsa().getFermataPartenza().getTime().toString());
 			item.setText(3, istanze.get(i).getCorsa().getFermataArrivo()
 					.getStazioneTratta().getStazione().getNome());
 			item.setText(4, istanze.get(i).getCorsa().getFermataArrivo().getTime().toString());
 			item.setText(5, istanze.get(i).getTreno().getId().toString());
 			item.setText(6, istanze.get(i).getTreno().getTipo().toString());
 			item.setText(7, istanze.get(i).getData().toString());
 
 		}
 		SwtUtil.orderTable(tabellaIstanze, 0);
 	}
 	
 
 	public void refresh() {
 		tabellaIstanze.removeAll();
 		loadTabellaIstanze();
 		SwtUtil.orderTable(tabellaIstanze, 0);
 	}
 
 	public TabItem getTab() {
 		return tabIstanze;
 	}
 
 }
