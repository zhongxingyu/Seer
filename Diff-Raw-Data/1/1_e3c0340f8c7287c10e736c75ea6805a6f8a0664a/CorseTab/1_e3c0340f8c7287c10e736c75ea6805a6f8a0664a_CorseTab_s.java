 package interfaccia;
 
 import java.sql.Time;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.SortedMap;
 import java.util.TreeMap;
 
 import javax.persistence.PersistenceException;
 
 import modelloTreni.Corsa;
 import modelloTreni.Fermata;
 import modelloTreni.StazioneTratta;
 import modelloTreni.Tipologia;
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
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.MessageBox;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.TabFolder;
 import org.eclipse.swt.widgets.TabItem;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.TableItem;
 
 public class CorseTab {
 	private TabItem tabCorse;
 	private Composite tabCorseGroup, corsaButtonGroup, fermataButtonGroup, filtriGroup;
 	private Combo filtroTratta;
 	private Map<Tipologia, Button> filtriTipologie;
 	private Table tabellaCorse, tabellaFermate;
 	private Button aggiungiCorsa, rimuoviCorsa, aggiungiFermata, rimuoviFermata;
 	private boolean modificato = false;
 
 	MainShell mainWindow = MainShell.getMainShell();
 	TrainManager manager = TrainManager.getInstance();
 
 	public CorseTab(TabFolder parent) {
 		tabCorse = new TabItem(parent, SWT.NONE);
 
 		tabCorseGroup = new Composite(parent, SWT.NONE);
 		tabCorseGroup.setLayout(new GridLayout(3, false));
 
 		createTabellaCorse();
 		createTabellaFermate();
 		createFiltriGroup();
 		createCorsaButtonGroup();
 		createFermataButtonGroup();
 
 		createTabellaCorseListener();
 		createTabellaFermateListener();
 		createAggiungiCorsaListener();
 		createRimuoviCorsaListener();
 		createAggiungiFermataListener();
 		createRimuoviFermataListener();
 		createFiltroTrattaListener();
 		createFiltriTipologieListener();
 
 		loadTabellaCorse(null, null);
 		SwtUtil.orderTable(tabellaCorse, 0);
 
 		tabCorse.setControl(tabCorseGroup);
 	}
 
 	private void createFiltriGroup() {
 		filtriGroup = new Composite(tabCorseGroup, SWT.NONE);
 		filtriGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
 		filtriGroup.setLayout(new RowLayout(SWT.VERTICAL)); // XXX va bene il
 															// rowLayout?
 		filtriTipologie = new TreeMap<Tipologia, Button>();
 
 		Label filtriLabel = new Label(filtriGroup, SWT.NONE);
 		filtriLabel.setText("Filtri");
 
 		filtroTratta = new Combo(filtriGroup, SWT.DROP_DOWN);
 		initFiltroTratta();
 
 		for (Tipologia tipo : Tipologia.values()) {
 			Button filtroButton = new Button(filtriGroup, SWT.CHECK);
 			filtriTipologie.put(tipo, filtroButton);
 			filtroButton.setText(tipo.toString());
 			filtroButton.setSelection(true);
 		}
 
 	}
 
 	private void initFiltroTratta() {
 		filtroTratta.removeAll();
 		filtroTratta.add("");
 		List<Tratta> tratte = manager.getTratte();
 		for (Tratta t : tratte) {
 			filtroTratta.add(t.getNome());
 		}
 	}
 
 	private void createFiltroTrattaListener() {
 		filtroTratta.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				super.widgetSelected(e);
 
 				filtraTabellaCorse();
 
 			}
 		});
 	}
 
 	private void createFiltriTipologieListener() {
 		for (Button b : filtriTipologie.values()) {
 			b.addSelectionListener(new SelectionAdapter() {
 				@Override
 				public void widgetSelected(SelectionEvent e) {
 					super.widgetSelected(e);
 					filtraTabellaCorse();
 				}
 			});
 		}
 	}
 
 	private void resetFiltri() {
 		filtroTratta.select(0);// XXX magic number!
 		for (Button b : filtriTipologie.values()) {
 			b.setSelection(true);
 		}
 	}
 
 	private void createRimuoviFermataListener() {
 		rimuoviFermata.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event) {
 				TableItem[] selezioneCorsa = tabellaCorse.getSelection();
 				Integer idCorsa = Integer.parseInt(selezioneCorsa[0].getText(0));
 				TableItem[] fermata = tabellaFermate.getSelection();
 
 				Corsa c = manager.getCorsa(idCorsa);
 				List<Fermata> fermate = c.getFermate();
 
 				int idstazioneTratta = 0;
 				// FIXME ??
 				// for(int i = 0; i<stazioneTratta.size(); i++){
 				// if(stazioneTratta.get(i).getStazione().getNome().equals(fermata[0].getText(0))){
 				// idstazioneTratta = stazioneTratta.get(i).getId();
 				// break;
 				// }
 				// }
 
 				boolean exit = false;
 				Iterator<Fermata> i = fermate.iterator();
 				while (i.hasNext() && exit == false) {
 
 					Fermata f = i.next();
 
 					if (f.getStazioneTratta().getStazione().getNome().equals(fermata[0].getText(0))) {
 						idstazioneTratta = f.getStazioneTratta().getId();
 						exit = true;
 					}
 				}
 				try {
 					manager.removeFermataFromCorsa(idCorsa, idstazioneTratta);
 					loadTabellaFermate(selezioneCorsa[0]);
 					rimuoviFermata.setEnabled(false);
 				} catch (TrainException e) {
 					MessageBox alert = new MessageBox(MainShell.getMainShell().getShell(), SWT.ICON_ERROR | SWT.OK);
 					alert.setMessage(e.getMessage());
 					alert.open();
 				}
 			}
 		});
 
 	}
 
 	private void createAggiungiFermataListener() {
 		aggiungiFermata.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event) {
 
 				final TableItem corsaSelezionata = tabellaCorse.getSelection()[0];
 				Corsa corsa = manager.getCorsa(Integer.parseInt(corsaSelezionata.getText(0)));
 
 				DialogAggiungiFermate d = new DialogAggiungiFermate(corsa);
 				d.showDialog();
 				d.getDialog().addDisposeListener(new DisposeListener() {
 
 					public void widgetDisposed(DisposeEvent e) {
 						loadTabellaFermate(corsaSelezionata);
 					}
 
 				});
 			}
 		});
 
 	}
 
 	private void createRimuoviCorsaListener() {
 		rimuoviCorsa.addSelectionListener(new SelectionAdapter() {
 			// @SuppressWarnings("unused")
 			public void widgetSelected(SelectionEvent event) {
 				TableItem[] selezione = tabellaCorse.getSelection();
 				try {
 					manager.removeCorsa(Integer.parseInt(selezione[0].getText(0)));
 					resetFiltri();
 					loadTabellaCorse(null, null);
 					tabellaFermate.removeAll();
 					SwtUtil.orderTable(tabellaCorse, 0);
 					modificato = true;
 					rimuoviCorsa.setEnabled(false);
 					aggiungiFermata.setEnabled(false);
 				} catch (PersistenceException e) {
 					// AlertShell alert =
 					// new AlertShell(
 					// "E' impossibile eliminare la corsa selezionata in quanto e' utilizzata in una istanza."
 					// + "\ncon posti prenotati.");
 					MessageBox alert = new MessageBox(MainShell.getMainShell().getShell(), SWT.ICON_ERROR | SWT.OK);
 					alert.setMessage("E' impossibile eliminare la corsa selezionata in quanto e' utilizzata in una istanza."
 							+ "\ncon posti prenotati.");
 					alert.open();
 				}
 			}
 		});
 	}
 
 	private void createAggiungiCorsaListener() {
 		aggiungiCorsa.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event) {
 				final Shell popup = new Shell(mainWindow.getShell(), 65616);
 				popup.setText("Nuova Corsa");
 				RowLayout popuplayout = new RowLayout(SWT.VERTICAL);
 				popuplayout.pack = false;
 				Label label = new Label(popup, SWT.NONE);
 				label.setText("Tratta");
 				label.setLayoutData(new RowData(200, 20));
 				final Combo newtrack = new Combo(popup, SWT.DROP_DOWN);
 				String[] tratte = initTrackCombo();
 				newtrack.setItems(tratte);
 				Label label2 = new Label(popup, SWT.NONE);
 				label2.setText("Tipologia");
 				final Combo newtype = new Combo(popup, SWT.DROP_DOWN);
 				newtype.setItems(new String[] { Tipologia.REGIONALE.toString(), Tipologia.INTERCITY.toString(),
 						Tipologia.EUROSTAR.toString() });
 				final Button ok = new Button(popup, SWT.PUSH);
 				ok.setText("Ok");
 				ok.setEnabled(false);
 
 				newtrack.addSelectionListener(new SelectionAdapter() {
 					public void widgetSelected(SelectionEvent event) {
 						if (newtrack.getText() != null && newtype.getText() != null) {
 							ok.setEnabled(true);
 						} else {
 							ok.setEnabled(false);
 						}
 
 						if (newtrack.getText().equals("Non ci sono fermate disponibili")) {
 							ok.setEnabled(false);
 						}
 					}
 				});
 
 				newtype.addSelectionListener(new SelectionAdapter() {
 					public void widgetSelected(SelectionEvent event) {
 						if (newtrack.getText() != null && newtype.getText() != null) {
 							ok.setEnabled(true);
 						} else {
 							ok.setEnabled(false);
 						}
 					}
 				});
 
 				ok.addSelectionListener(new SelectionAdapter() {
 					// @SuppressWarnings("unchecked")
 					public void widgetSelected(SelectionEvent event) {
 						SortedMap<Time, StazioneTratta> map = new TreeMap<Time, StazioneTratta>();
 						// List<StazioneTratta> stazionitratta =
 						// (List<StazioneTratta>)
 						// manager.doQuery("select stazionetratta from StazioneTratta stazionetratta where tratta_id="
 						// +
 						// "(select id from Tratta where nome = '"+newtrack.getText()+"')");
 
 						// List<Tratta> tratta = (List<Tratta>)
 						// manager.doQuery("select tratta from Tratta tratta where nome ='"+newtrack.getText()+"'");
 						Tratta tratta = manager.getTratta(newtrack.getText());
 						Tipologia tipologia;
 						if (newtype.getText().equals(Tipologia.EUROSTAR.toString())) {
 							tipologia = Tipologia.EUROSTAR;
 						} else if (newtype.getText().equals(Tipologia.REGIONALE.toString())) {
 							tipologia = Tipologia.REGIONALE;
 						} else {
 							tipologia = Tipologia.INTERCITY;
 						}
 						// manager.createCorsa(tratta.get(0), tipologia, map);
 						try {
 							manager.createCorsa(tratta, tipologia, map);
 							resetFiltri();
 							loadTabellaCorse(null, null);
 							SwtUtil.orderTable(tabellaCorse, 0);
 							popup.close();
 						} catch (TrainException e) {
 							MessageBox alert = new MessageBox(MainShell.getMainShell().getShell(), SWT.ICON_ERROR
 									| SWT.OK);
 							alert.setMessage(e.getMessage());
 							alert.open();
 						}
 					}
 				});
 
 				popup.setLayout(popuplayout);
 				popup.pack();
 				popup.open();
 
 			}
 		});
 	}
 
 	private void createTabellaCorseListener() {
 		tabellaCorse.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event) {
 				rimuoviCorsa.setEnabled(true);
 				aggiungiFermata.setEnabled(true);
 				loadTabellaFermate(tabellaCorse.getSelection()[0]);
 			}
 		});
 	}
 
 	private void createTabellaFermateListener() {
 		tabellaFermate.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event) {
 				rimuoviFermata.setEnabled(true);
 			}
 		});
 	}
 
 	private void createFermataButtonGroup() {
 		fermataButtonGroup = new Composite(tabCorseGroup, SWT.NONE);
 		fermataButtonGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
 		fermataButtonGroup.setLayout(new GridLayout(2, true));
 
 		aggiungiFermata = new Button(fermataButtonGroup, SWT.PUSH);
 		aggiungiFermata.setText("+");
 		aggiungiFermata.setEnabled(false);
 
 		rimuoviFermata = new Button(fermataButtonGroup, SWT.PUSH);
 		rimuoviFermata.setText("-");
 		rimuoviFermata.setEnabled(false);
 	}
 
 	private void createCorsaButtonGroup() {
 		corsaButtonGroup = new Composite(tabCorseGroup, SWT.NONE);
 		corsaButtonGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
 		corsaButtonGroup.setLayout(new GridLayout(2, true));
 
 		aggiungiCorsa = new Button(corsaButtonGroup, SWT.PUSH);
 		aggiungiCorsa.setText("+");
 
 		rimuoviCorsa = new Button(corsaButtonGroup, SWT.PUSH);
 		rimuoviCorsa.setText("-");
 		rimuoviCorsa.setEnabled(false);
 	}
 
 	private void createTabellaFermate() {
 		tabellaFermate = new Table(tabCorseGroup, SWT.SINGLE);
 		tabellaFermate.setLinesVisible(true);
 		tabellaFermate.setHeaderVisible(true);
 
 		TableColumn columnstop = new TableColumn(tabellaFermate, SWT.LEFT);
 		columnstop.setText("Fermata");
 		tabellaFermate.getColumn(0).setWidth(120);
 		TableColumn columndate = new TableColumn(tabellaFermate, SWT.LEFT);
 		columndate.setText("Tempo");
 		tabellaFermate.getColumn(1).setWidth(70);
 
 		GridData gridtablestop = new GridData(SWT.FILL, SWT.FILL, true, true);
 		tabellaFermate.setLayoutData(gridtablestop);
 	}
 
 	private void createTabellaCorse() {
 		tabellaCorse = new Table(tabCorseGroup, SWT.SINGLE);
 		tabellaCorse.setLinesVisible(true);
 		tabellaCorse.setHeaderVisible(true);
 
 		TableColumn columnid = new TableColumn(tabellaCorse, SWT.LEFT);
 		columnid.setText("Id Corsa");
 		tabellaCorse.getColumn(0).setWidth(50);
 		TableColumn columntrack = new TableColumn(tabellaCorse, SWT.LEFT);
 		columntrack.setText("Tratta");
 		tabellaCorse.getColumn(1).setWidth(200);
 		TableColumn columnType = new TableColumn(tabellaCorse, SWT.LEFT);
 		columnType.setText("Tipologia");
 		columnType.setWidth(100);
 
 		GridData gridtablerun = new GridData(SWT.FILL, SWT.FILL, true, true);
 		tabellaCorse.setLayoutData(gridtablerun);
 	}
 
 	private void filtraTabellaCorse() {
 
 		Tratta tratta;
 		ArrayList<Tipologia> tipologie = new ArrayList<Tipologia>();
 		String filtroTrattaText = filtroTratta.getText();
 		if (filtroTrattaText.isEmpty()) {
 			tratta = null;
 		} else {
 			tratta = manager.getTratta(filtroTrattaText);// XXX va bene così o
 															// devo trovare il
 															// modo per cercare
 															// tramite id?
 		}
 		for (Tipologia t : filtriTipologie.keySet()) {
 			if (filtriTipologie.get(t).getSelection() == true) {
 				tipologie.add(t);
 			}
 		}
 		loadTabellaCorse(tratta, tipologie);
 	}
 
 	private void loadTabellaCorse(Tratta tratta, ArrayList<Tipologia> tipologie) {
 		tabellaFermate.removeAll();
 		aggiungiFermata.setEnabled(false);
 		rimuoviFermata.setEnabled(false);
 		rimuoviCorsa.setEnabled(false);
 
 		tabellaCorse.removeAll();
 		List<Corsa> corse = manager.getCorse();
 		for (Corsa c : corse) {
 			if (checkCorsa(c, tratta, tipologie)) {
 				TableItem item = new TableItem(tabellaCorse, SWT.NONE);
 				item.setText(0, c.getId().toString());
 				item.setText(1, c.getTratta().getNome());
 				item.setText(2, c.getTipo().toString());
 			}
 		}
 	}
 
 	// private void filterTabellaCorseByTratta(Tratta tratta){
 	// TableItem[] items = tabellaCorse.getItems();
 	// if(items.length > 0){
 	// for(int i = 0; i<items.length; i++){
 	// TableItem item = tabellaCorse.getItem(i);
 	// Tratta trattaItem = manager.getTratta(item.getText(0));
 	// if(trattaItem != tratta){
 	// tabellaCorse.re
 	// }
 	// }
 	// }
 	// }
 
 	private boolean checkCorsa(Corsa corsa, Tratta tratta, ArrayList<Tipologia> tipologie) {
 		boolean trattaOk = false;
 		boolean tipologieOk = false;
 
 		if (tratta == null || corsa.getTratta().getId() == tratta.getId()) {
 			trattaOk = true;
 		}
 
 		if (tipologie == null || tipologie.isEmpty()) {
 			tipologieOk = true;
 		} else {
 			for (Tipologia t : tipologie) {
 				if (corsa.getTipo().equals(t)) {
 					tipologieOk = true;
 					break;
 				}
 			}
 		}
 		return trattaOk && tipologieOk;
 	}
 
 	private void loadTabellaFermate(TableItem selezione) {
 
 		tabellaFermate.removeAll();
 		Corsa c = manager.getCorsa(Integer.parseInt(selezione.getText(0)));
 		List<Fermata> fermate = c.getFermate();
 		for (Fermata f : fermate) {
 			TableItem item = new TableItem(tabellaFermate, SWT.NONE);
 			item.setText(0, f.getStazioneTratta().getStazione().getNome());
 			item.setText(1, f.getTime().toString());
 		}
 
 	}
 
 	private String[] initTrackCombo() {
 
 		List<Tratta> tratte = manager.getTratte();
 		String[] array = new String[tratte.size()];
 		int i = 0;
 		for (Tratta t : tratte) {
 			array[i] = t.getNome();
 			i++;
 		}
 		return array;
 	}
 
 	// private void orderTable() {
 	// TableItem[] items = tabellaCorse.getItems();
 	// int[] ids = new int[items.length];
 	// for (int i = 0; i < items.length; i++) {
 	// ids[i] = Integer.parseInt(items[i].getText(0));
 	// }
 	// Arrays.sort(ids);
 	// for (int i = 0; i < items.length; i++) {
 	// for (int j = 0; j < items.length; j++) {
 	// if (ids[i] == Integer.parseInt(items[j].getText(0))) {
 	// TableItem newitem = new TableItem(tabellaCorse, SWT.NONE);
 	// newitem.setText(0, items[j].getText(0));
 	// newitem.setText(1, items[j].getText(1));
 	// break;
 	// }
 	// }
 	// }
 	// tabellaCorse.remove(0, tabellaCorse.indexOf(items[(items.length) - 1]));
 	// }
 
 	public boolean isModified() {
 		return modificato;
 	}
 
 	public TabItem getTab() {
 		return tabCorse;
 	}
 
 }
