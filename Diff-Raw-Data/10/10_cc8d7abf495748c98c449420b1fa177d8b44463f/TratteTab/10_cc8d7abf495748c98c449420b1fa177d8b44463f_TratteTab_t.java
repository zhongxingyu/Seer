 package interfaccia;
 
 import java.util.List;
 
 import javax.persistence.PersistenceException;
 
 import modelloTreni.StazioneTratta;
 import modelloTreni.TrainManager;
 import modelloTreni.Tratta;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.DisposeEvent;
 import org.eclipse.swt.events.DisposeListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
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
 
 public class TratteTab {
 	private TabItem tabTratte;
 	private Composite tabTratteGroup, tratteButtonGroup, stazioniTrattaButtonGroup;
 	private Table tabellaTratte, tabellaStazioniTratta;
 	private Button aggiungiTratta, rimuoviTratta, aggiungiStazioneTratta, rimuoviStazioneTratta, creaTrattaInversa;
 	MainShell mainWindow = MainShell.getMainShell();
 	TrainManager manager = TrainManager.getInstance();
 
 	public TratteTab(TabFolder parent) {
 		tabTratte = new TabItem(parent, SWT.NONE);
 		tabTratteGroup = new Composite(parent, SWT.NONE);
 		tabTratteGroup.setLayout(new GridLayout(2, true));
 
 		createTabellaTratte();
 		createTabellaStazioniTratta();
 		createTratteButtonGroup();
 		createStazioniTrattaButtonGroup();
 
 		createTabellaTratteListener();
 		createTabellaStazioniTrattaListener();
 		createAggiungiTrattaListener();
 		createRimuoviTrattaListener();
 		createTrattaInversaListener();
 		createAggiungiStazioneTrattaListener();
 		createRimuoviStazioneTrattaListener();
 
 		loadTabellaTratte();
 
 		tabTratte.setControl(tabTratteGroup);
 	}
 
 	private void createRimuoviStazioneTrattaListener() {
 		rimuoviStazioneTratta.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event) {
 				TableItem selezionetratta = tabellaTratte.getSelection()[0];
 				TableItem[] selezionestazione = tabellaStazioniTratta.getSelection();
 				int idtratta = Integer.parseInt(selezionetratta.getText(0));
 				String nomestazione = selezionestazione[0].getText(0);
 				try {
 					manager.removeStazioneFromTratta(idtratta, nomestazione);
 					tabellaStazioniTratta.removeAll();
 					loadTabellaStazioniTratta(selezionetratta);
 				} catch (PersistenceException e) {
 					MessageBox alert = new MessageBox(MainShell.getMainShell().getShell(), SWT.ICON_ERROR | SWT.OK);
 					alert.setMessage("E' impossibile eliminare questa stazione perche' e' fermata di una corsa. Eliminare prima tale corsa.");
 					alert.open();
 					return;
 				}
 			}
 		});
 	}
 
 	private void createAggiungiStazioneTrattaListener() {
 		aggiungiStazioneTratta.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event) {
 
 				final TableItem trattaSelezionata = tabellaTratte.getSelection()[0];
 				Tratta tratta = manager.getTratta(trattaSelezionata.getText(1));
 
 				DialogAggiungiStazioneTratta d = new DialogAggiungiStazioneTratta(tratta);
 				d.showDialog();
 				d.getDialog().addDisposeListener(new DisposeListener() {
 
 					public void widgetDisposed(DisposeEvent e) {
 						loadTabellaStazioniTratta(trattaSelezionata);
 						SwtUtil.orderTableByInt(tabellaStazioniTratta, 1);
 					}
 
 				});
 			}
 		});
 	}
 
 	private void createRimuoviTrattaListener() {
 		rimuoviTratta.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event) {
 				TableItem[] selezione = tabellaTratte.getSelection();
 				try {
 					manager.removeTratta(Integer.parseInt(selezione[0].getText()));
 					aggiungiStazioneTratta.setEnabled(false);
 				} catch (Exception e) {
 					MessageBox alert = new MessageBox(MainShell.getMainShell().getShell(), SWT.ICON_ERROR | SWT.OK);
 					alert.setMessage("La tratta che stai cercando di eliminare e' utilizzata in almeno una corsa. Rimuovere prima tale corsa.");
 					alert.open();
 				}
 				tabellaTratte.removeAll();
 				tabellaStazioniTratta.removeAll();
 				loadTabellaTratte();
 				rimuoviTratta.setEnabled(false);
 				creaTrattaInversa.setEnabled(false);
 			}
 		});
 	}
 
 	private void createAggiungiTrattaListener() {
 		aggiungiTratta.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event) {
 				DialogAggiungiTratta d = new DialogAggiungiTratta();
 				d.showDialog();
 				d.getDialog().addDisposeListener(new DisposeListener() {
 
 					public void widgetDisposed(DisposeEvent e) {
 						loadTabellaTratte();
 					}
 				});
 			}
 		});
 	}
 
 	private void createTrattaInversaListener() {
 		creaTrattaInversa.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event) {
 				DialogCreaTrattaInversa d = new DialogCreaTrattaInversa(
 						Integer.parseInt(tabellaTratte.getSelection()[0].getText()));
 				d.open();
 				d.getDialog().addDisposeListener(new DisposeListener() {
 					@Override
 					public void widgetDisposed(DisposeEvent e) {
 						loadTabellaTratte();
 					}
 				});
 			}
 		});
 	}
 
 	private void createTabellaStazioniTrattaListener() {
 		tabellaStazioniTratta.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event) {
 				if (tabellaStazioniTratta.getSelection().length > 0) {
 					rimuoviStazioneTratta.setEnabled(true);
 				} else {
 					rimuoviStazioneTratta.setEnabled(false);
 				}
 			}
 		});
 	}
 
 	private void createTabellaTratteListener() {
 		tabellaTratte.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event) {
 				TableItem[] itemSelezionati = tabellaTratte.getSelection();
 				if (itemSelezionati.length > 0) {
 					rimuoviTratta.setEnabled(true);
 					creaTrattaInversa.setEnabled(true);
 					aggiungiStazioneTratta.setEnabled(true);
 					loadTabellaStazioniTratta(itemSelezionati[0]);
 					SwtUtil.orderTableByInt(tabellaStazioniTratta, 1);
 				}
 				else{
 					rimuoviTratta.setEnabled(false);
 					creaTrattaInversa.setEnabled(false);
 					aggiungiStazioneTratta.setEnabled(false);
 					tabellaStazioniTratta.removeAll();
 				}
 
 				rimuoviStazioneTratta.setEnabled(false);
 			}
 		});
 	}
 
 	private void createStazioniTrattaButtonGroup() {
 		stazioniTrattaButtonGroup = new Composite(tabTratteGroup, SWT.NONE);
 		stazioniTrattaButtonGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
 		stazioniTrattaButtonGroup.setLayout(new GridLayout(2, true));
 
 		aggiungiStazioneTratta = new Button(stazioniTrattaButtonGroup, SWT.PUSH);
 		aggiungiStazioneTratta.setText("Aggiungi Stazione alla Tratta");
 		aggiungiStazioneTratta.setEnabled(false);
 		rimuoviStazioneTratta = new Button(stazioniTrattaButtonGroup, SWT.PUSH);
 		rimuoviStazioneTratta.setText("Elimina Stazione dalla Tratta");
 		rimuoviStazioneTratta.setEnabled(false);
 	}
 
 	private void createTratteButtonGroup() {
 		tratteButtonGroup = new Composite(tabTratteGroup, SWT.NONE);
 		tratteButtonGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
 		tratteButtonGroup.setLayout(new GridLayout(2, true));
 
 		aggiungiTratta = new Button(tratteButtonGroup, SWT.PUSH);
 		aggiungiTratta.setText("Aggiungi Tratta");
 		creaTrattaInversa = new Button(tratteButtonGroup, SWT.PUSH);
 		creaTrattaInversa.setText("Aggiungi Tratta Inversa");
 		creaTrattaInversa.setEnabled(false);
 		rimuoviTratta = new Button(tratteButtonGroup, SWT.PUSH);
 		rimuoviTratta.setText("Elimina Tratta");
 		rimuoviTratta.setEnabled(false);
 	}
 
 	private void createTabellaStazioniTratta() {
 		tabellaStazioniTratta = new Table(tabTratteGroup, SWT.SINGLE);
 		tabellaStazioniTratta.setLinesVisible(true);
 		tabellaStazioniTratta.setHeaderVisible(true);
 
 		TableColumn columnstationone = new TableColumn(tabellaStazioniTratta, SWT.LEFT);
 		columnstationone.setText("Stazioni");
 		TableColumn columnstationtwo = new TableColumn(tabellaStazioniTratta, SWT.LEFT);
 		columnstationtwo.setText("Distanza");
 		tabellaStazioniTratta.getColumn(0).setWidth(290);
 		tabellaStazioniTratta.getColumn(1).setWidth(75);
 
 		tabellaStazioniTratta.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 	}
 
 	private void createTabellaTratte() {
 		tabellaTratte = new Table(tabTratteGroup, SWT.SINGLE);
 		tabellaTratte.setLinesVisible(true);
 		tabellaTratte.setHeaderVisible(true);
 
 		TableColumn colonnaId = new TableColumn(tabellaTratte, SWT.LEFT);
 		colonnaId.setText("Id");
 		colonnaId.setWidth(70);
 		TableColumn colonnaTratta = new TableColumn(tabellaTratte, SWT.LEFT);
 		colonnaTratta.setText("Tratta");
 		colonnaTratta.setWidth(360);
 
 		tabellaTratte.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 
 	}
 
 	private void loadTabellaTratte() {
 		tabellaTratte.removeAll();
		tabellaStazioniTratta.removeAll();
 		rimuoviTratta.setEnabled(false);
 		rimuoviStazioneTratta.setEnabled(false);
 		aggiungiStazioneTratta.setEnabled(false);
		creaTrattaInversa.setEnabled(false);
 
 		List<Tratta> tratte = manager.getTratte();
 		for (Tratta t : tratte) {
 			TableItem item = new TableItem(tabellaTratte, SWT.NONE);
 			item.setText(1, t.getNome());
 			item.setText(0, Integer.toString(t.getId()));
 		}
 	}
 
 	private void loadTabellaStazioniTratta(TableItem selezione) {
 
 		tabellaStazioniTratta.removeAll();
 		Tratta t = manager.getTratta(selezione.getText(1));
 		for (StazioneTratta st : t.getStazioniTratta()) {
 
 			TableItem item = new TableItem(tabellaStazioniTratta, SWT.NONE);
 			item.setText(0, st.getStazione().getNome());
 			item.setText(1, Integer.toString(st.getDistanza()));
 		}
 
 	}
 
 	public void refresh() {
 		tabellaStazioniTratta.removeAll();
 		tabellaTratte.removeAll();
 		loadTabellaTratte();
 	}
 
 	public TabItem getTab() {
 		return tabTratte;
 	}
 
 }
