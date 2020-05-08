 package interfaccia;
 
 import java.util.Arrays;
 import java.util.List;
 import java.util.SortedMap;
 import java.util.TreeMap;
 
 import javax.persistence.PersistenceException;
 
 import modelloTreni.Stazione;
 import modelloTreni.StazioneTratta;
 import modelloTreni.TrainException;
 import modelloTreni.TrainManager;
 import modelloTreni.Tratta;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.DisposeEvent;
 import org.eclipse.swt.events.DisposeListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.layout.RowData;
 import org.eclipse.swt.layout.RowLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.MessageBox;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.TabFolder;
 import org.eclipse.swt.widgets.TabItem;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.TableItem;
 import org.eclipse.swt.widgets.Text;
 
 public class TratteTab {
 	private TabItem tabTratte;
 	private Composite tabTratteGroup, tratteButtonGroup,
 			stazioniTrattaButtonGroup;
 	private Table tabellaTratte, tabellaStazioniTratta;
 	private Button aggiungiTratta, rimuoviTratta, aggiungiStazioneTratta,
 			rimuoviStazioneTratta;
 	MainShell mainWindow = MainShell.getMainShell();
 	TrainManager manager = TrainManager.getInstance();
 
 	public TratteTab(TabFolder parent) {
 		tabTratte = new TabItem(parent, SWT.NONE);
 		tabTratteGroup = new Composite(parent, SWT.NONE);
 
 		createTabellaTratte();
 		createTabellaStazioniTratta();
 		createTratteButtonGroup();
 		createStazioniTrattaButtonGroup();
 		createTabellaTratteListener();
 		createTabellaStazioniTrattaListener();
 		createAggiungiTrattaListener();
 		createRimuoviTrattaListener();
 		createAggiungiStazioneTrattaListener();
 		createRimuoviStazioneTrattaListener();
 
 		tabTratteGroup.setLayout(new GridLayout(2, true));
 		tabTratte.setControl(tabTratteGroup);
 	}
 
 	private void createRimuoviStazioneTrattaListener() {
 		rimuoviStazioneTratta.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event) {
 				TableItem selezionetratta = tabellaTratte.getSelection()[0];
 				TableItem[] selezionestazione = tabellaStazioniTratta
 						.getSelection();
				int idtratta = Integer.parseInt(selezionetratta.getText(0));
 				String nomestazione = selezionestazione[0].getText(0);
 				try {
 					manager.removeStazioneFromTratta(idtratta, nomestazione);
 					tabellaStazioniTratta.removeAll();
 					loadTabellaStazioniTratta(selezionetratta);
 				} catch (PersistenceException e) {
 					// AlertShell alert = XXX
 					// new AlertShell(
 					// "E' impossibile eliminare questa stazione perche' e' fermata di una corsa. Eliminare prima tale corsa.");
 					MessageBox alert = new MessageBox(MainShell.getMainShell()
 							.getShell(), SWT.ICON_ERROR | SWT.OK);
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
 
 				final TableItem trattaSelezionata = tabellaTratte
 						.getSelection()[0];
 				Tratta tratta = manager.getTratta(trattaSelezionata.getText(1));
 
 				DialogAggiungiStazioneTratta d = new DialogAggiungiStazioneTratta(
 						tratta);
 				d.showDialog();
 				d.getDialog().addDisposeListener(new DisposeListener() {
 
 					public void widgetDisposed(DisposeEvent e) {
 						loadTabellaStazioniTratta(trattaSelezionata);
 						orderTabellaStazioniTratta();
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
 					manager.removeTratta(selezione[0].getText());
 				} catch (Exception e) {
 					// AlertShell alert = XXX
 //					new AlertShell(
 //							"La tratta e' utilizzata in almeno una corsa.");
 					MessageBox alert = new MessageBox(MainShell.getMainShell().getShell(),SWT.ICON_ERROR | SWT.OK);
 					alert.setMessage("La tratta e' utilizzata in almeno una corsa.");
 					alert.open();
 				}
 				tabellaTratte.removeAll();
 				tabellaStazioniTratta.removeAll();
 				loadTabellaTratte();
 				rimuoviTratta.setEnabled(false);
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
 				// loadTabellaTratte();
 			}
 		});
 	}
 
 	private void createTabellaStazioniTrattaListener() {
 		tabellaStazioniTratta.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event) {
 				TableItem[] selezione = tabellaStazioniTratta.getSelection();
 				if (selezione.length != 0) {
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
 				rimuoviTratta.setEnabled(true);
 				aggiungiStazioneTratta.setEnabled(true);
 				tabellaStazioniTratta.removeAll();
 				TableItem selezione = tabellaTratte.getSelection()[0];
 				TableItem[] selezione2 = tabellaStazioniTratta.getSelection();
 				if (selezione2.length == 0) {
 					rimuoviStazioneTratta.setEnabled(false);
 				}
 				loadTabellaStazioniTratta(selezione);
 				orderTabellaStazioniTratta();
 			}
 		});
 	}
 
 	private void createStazioniTrattaButtonGroup() {
 		stazioniTrattaButtonGroup = new Composite(tabTratteGroup, SWT.NONE);
 		stazioniTrattaButtonGroup.setLayoutData(new GridData(SWT.FILL,
 				SWT.FILL, false, false));
 		stazioniTrattaButtonGroup.setLayout(new GridLayout(2, true));
 
 		aggiungiStazioneTratta = new Button(stazioniTrattaButtonGroup, SWT.PUSH);
 		aggiungiStazioneTratta.setText("+");
 		aggiungiStazioneTratta.setEnabled(false);
 		rimuoviStazioneTratta = new Button(stazioniTrattaButtonGroup, SWT.PUSH);
 		rimuoviStazioneTratta.setText("-");
 		rimuoviStazioneTratta.setEnabled(false);
 	}
 
 	private void createTratteButtonGroup() {
 		tratteButtonGroup = new Composite(tabTratteGroup, SWT.NONE);
 		tratteButtonGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false,
 				false));
 		tratteButtonGroup.setLayout(new GridLayout(2, true));
 
 		aggiungiTratta = new Button(tratteButtonGroup, SWT.PUSH);
 		aggiungiTratta.setText("+");
 		rimuoviTratta = new Button(tratteButtonGroup, SWT.PUSH);
 		rimuoviTratta.setText("-");
 		rimuoviTratta.setEnabled(false);
 	}
 
 	private void createTabellaStazioniTratta() {
 		tabellaStazioniTratta = new Table(tabTratteGroup, SWT.SINGLE);
 		tabellaStazioniTratta.setLinesVisible(true);
 		tabellaStazioniTratta.setHeaderVisible(true);
 
 		TableColumn columnstationone = new TableColumn(tabellaStazioniTratta,
 				SWT.LEFT);
 		columnstationone.setText("Stazioni");
 		TableColumn columnstationtwo = new TableColumn(tabellaStazioniTratta,
 				SWT.LEFT);
 		columnstationtwo.setText("Distanza");
 		tabellaStazioniTratta.getColumn(0).setWidth(290);
 		tabellaStazioniTratta.getColumn(1).setWidth(75);
 
 		tabellaStazioniTratta.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
 				true, true));
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
 
 		tabellaTratte
 				.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 
 		loadTabellaTratte();
 	}
 
 	private void loadTabellaTratte() {
 		tabellaTratte.removeAll();
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
 
 	private void orderTabellaStazioniTratta() {
 		// FIXME non so quale ordinamento non funge, ma crea altri elementi
 		// invece di ordinare i nuovi
 		TableItem[] items = tabellaStazioniTratta.getItems();
 		int[] distanze = new int[items.length];
 		for (int i = 0; i < items.length; i++) {
 			distanze[i] = Integer.parseInt(items[i].getText(1));
 		}
 		Arrays.sort(distanze);
 		for (int i = 0; i < items.length; i++) {
 			for (int j = 0; j < items.length; j++) {
 				if (distanze[i] == Integer.parseInt(items[j].getText(1))) {
 					TableItem newitem = new TableItem(tabellaStazioniTratta,
 							SWT.NONE);
 					newitem.setText(0, items[j].getText(0));
 					newitem.setText(1, items[j].getText(1));
 					break;
 				}
 			}
 		}
 		tabellaStazioniTratta.remove(0,
 				tabellaStazioniTratta.indexOf(items[(items.length) - 1]));
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
