 package interfaccia;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import modelloTreni.Posto;
 import modelloTreni.Prenotazione;
 import modelloTreni.TrainManager;
 import modelloUtenti.TipoUtente;
 import modelloUtenti.UserManager;
 import modelloUtenti.Utente;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.TabFolder;
 import org.eclipse.swt.widgets.TabItem;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.TableItem;
 
 public class PrenotazioniTab {
 	private TabItem tabPrenotazioni;
 	private Composite tabPrenotazioniGroup, prenotazioniButtonGroup;
 	private Table tabellaPrenotazioni;
 	private Button rimuoviPrenotazione;
 	MainShell mainWindow = MainShell.getMainShell();
 	TrainManager manager = TrainManager.getInstance();
 	UserManager umanager = UserManager.getInstance();
 	
 	public PrenotazioniTab(TabFolder parent){
 		tabPrenotazioni = new TabItem(parent, SWT.NONE);
 		tabPrenotazioniGroup = new Composite(parent, SWT.NONE);
 		
 		createTabellaPrenotazioni();
 		createPrenotazioniButtonGroup();
 		createTabellaPrenotazioniListener();
 		createRimuoviPrenotazioniListener();
 		
 		tabPrenotazioniGroup.setLayout(new GridLayout(1, false));
 		tabPrenotazioni.setControl(tabPrenotazioniGroup);
 	}
 	
 	private void createTabellaPrenotazioni(){
 		tabellaPrenotazioni = new Table(tabPrenotazioniGroup, SWT.SINGLE);
 		tabellaPrenotazioni.setLinesVisible(true);
 		tabellaPrenotazioni.setHeaderVisible(true);
 		
 		TableColumn column = new TableColumn(tabellaPrenotazioni, SWT.LEFT);
 		column.setText("Data");
 		TableColumn column2 = new TableColumn(tabellaPrenotazioni, SWT.LEFT);
 		column2.setText("Numero Posti");
 		TableColumn column3 = new TableColumn(tabellaPrenotazioni, SWT.LEFT);
 		column3.setText("Classe");
 		TableColumn column4 = new TableColumn(tabellaPrenotazioni, SWT.LEFT);
 		column4.setText("Treno");
 		TableColumn column5 = new TableColumn(tabellaPrenotazioni, SWT.LEFT);
 		column5.setText("Tipo");
 		TableColumn column6 = new TableColumn(tabellaPrenotazioni, SWT.LEFT);
 		column6.setText("Tratta");
 		
 		tabellaPrenotazioni.getColumn(0).setWidth(150);
 		tabellaPrenotazioni.getColumn(1).setWidth(100);
 		tabellaPrenotazioni.getColumn(2).setWidth(150);
 		tabellaPrenotazioni.getColumn(3).setWidth(100);
 		tabellaPrenotazioni.getColumn(4).setWidth(200);
 		tabellaPrenotazioni.getColumn(5).setWidth(200);
 		
 		if(umanager.getLoggedUser().getTipo().equals(TipoUtente.ADMIN)){
 			TableColumn column7 = new TableColumn(tabellaPrenotazioni, SWT.LEFT);
 			column7.setText("Utente");
 			tabellaPrenotazioni.getColumn(6).setWidth(200);
 			TableColumn column8 = new TableColumn(tabellaPrenotazioni, SWT.LEFT);
 			column8.setText("Id Prenotazione");
 			tabellaPrenotazioni.getColumn(7).setWidth(200);
 		}
 
 		tabellaPrenotazioni.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
 				true));	
 		
 		if(umanager.getLoggedUser().getTipo().equals(TipoUtente.CLIENT)){
 			loadTableClient();
 		} else {
 			loadTableAdmin();
 		}
 		
 	}
 	
 	private void loadTableClient(){
 		Utente ulogged = umanager.getLoggedUser();
 		ArrayList<Prenotazione> prenotazioni = ulogged.getPrenotazioni();
 		for(int i = 0; i<prenotazioni.size(); i++){
 			TableItem item = new TableItem(tabellaPrenotazioni, SWT.NONE);
 			ArrayList<Posto> posti = prenotazioni.get(i).getPostiPrenotati();
 			Integer numeroposti = posti.size();
 			item.setText(0, posti.get(0).getIstanzaTreno().getData().toString());
 			item.setText(1, numeroposti.toString());
 			item.setText(2, posti.get(0).getClasse().toString());
 			item.setText(3, posti.get(0).getIstanzaTreno().getCorsa().getId().toString());
 			item.setText(4, posti.get(0).getIstanzaTreno().getCorsa().getTipo().toString());
 			item.setText(5, posti.get(0).getIstanzaTreno().getCorsa().getTratta().getNome());
 		}
 
 	}
 	
 	private void loadTableAdmin(){
 		List<Prenotazione> prenotazioni = manager.getPrenotazioni();
 		for(int i = 0; i<prenotazioni.size(); i++){
 			TableItem item = new TableItem(tabellaPrenotazioni, SWT.NONE);
 			ArrayList<Posto> posti = prenotazioni.get(i).getPostiPrenotati();
			item.setText(0, posti.get(0).getIstanzaTreno().getData().toString());
 			Integer numeroposti = posti.size();
 			item.setText(1, numeroposti.toString());
 			item.setText(2, posti.get(0).getClasse().toString());
 			item.setText(3, posti.get(0).getIstanzaTreno().getCorsa().getId().toString());
 			item.setText(4, posti.get(0).getIstanzaTreno().getCorsa().getTipo().toString());
 			item.setText(5, posti.get(0).getIstanzaTreno().getCorsa().getTratta().getNome());
 			item.setText(6, prenotazioni.get(i).getUtente().getNome() + " " + 
 					prenotazioni.get(i).getUtente().getCognome());
 			item.setText(7, prenotazioni.get(i).getId().toString());
 		}
 	}
 	
 	public TabItem getTab(){
 		return tabPrenotazioni;
 	}
 	
 	private void createPrenotazioniButtonGroup() {
 
 		prenotazioniButtonGroup = new Composite(tabPrenotazioniGroup, SWT.NONE);
 		prenotazioniButtonGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
 		prenotazioniButtonGroup.setLayout(new GridLayout(2, true));
 
 		rimuoviPrenotazione = new Button(prenotazioniButtonGroup, SWT.PUSH);
 		rimuoviPrenotazione.setText("Elimina Prenotazione");
 		rimuoviPrenotazione.setEnabled(false);
 		
 		if(umanager.getLoggedUser().getTipo().equals(TipoUtente.CLIENT)){
 			rimuoviPrenotazione.setVisible(false);
 		} else {
 			rimuoviPrenotazione.setVisible(true);
 			}
 	}
 	
 	private void createTabellaPrenotazioniListener(){
 		tabellaPrenotazioni.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event){
 				rimuoviPrenotazione.setEnabled(true);
 			}
 		});
 	}
 	
 	private void createRimuoviPrenotazioniListener(){
 		rimuoviPrenotazione.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event){
 				TableItem[] selezione = tabellaPrenotazioni.getSelection();
 				manager.removePrenotazione(Integer.parseInt(selezione[0].getText(7)));
 				tabellaPrenotazioni.removeAll();
 				loadTableAdmin();
 				rimuoviPrenotazione.setEnabled(false);
 			}
 		});
 	}
 	
 	public void refresh(){
 		tabellaPrenotazioni.removeAll();
 		if(umanager.getLoggedUser().getTipo().equals(TipoUtente.CLIENT)){
 			loadTableClient();
 			rimuoviPrenotazione.setVisible(false);
 		} else {
 			loadTableAdmin();
 			rimuoviPrenotazione.setVisible(true);
 		}
 	}
 
 }
