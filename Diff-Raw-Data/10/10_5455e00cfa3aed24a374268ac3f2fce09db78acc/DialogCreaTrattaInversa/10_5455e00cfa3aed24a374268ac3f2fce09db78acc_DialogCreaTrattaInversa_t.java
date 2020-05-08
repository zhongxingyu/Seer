 package interfaccia;
 
 import java.util.SortedMap;
 import java.util.TreeMap;
 
 import modelloTreni.Stazione;
 import modelloTreni.TrainException;
 import modelloTreni.TrainManager;
 import modelloTreni.Tratta;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.RowData;
 import org.eclipse.swt.layout.RowLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.MessageBox;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 
 public class DialogCreaTrattaInversa {
 	
 	private MainShell mainWindow = MainShell.getMainShell();
 	private TrainManager manager = TrainManager.getInstance();
 	
 	private Shell popup;
 	private RowLayout popupLayout;
 	private Text nameText;
 	private Button okButton;
 	private int tratta;
 	
 	public DialogCreaTrattaInversa(int idTratta) {
 		super();
 		this.tratta=idTratta;
 		
 		popup = new Shell(mainWindow.getShell(), 65616);
 		popup.setText("Nuova Tratta");
 	
 		popupLayout = new RowLayout(SWT.VERTICAL);
 		popupLayout.pack = false;
 	
 		nameText = new Text(popup, SWT.SINGLE);
 		nameText.setText("Nome Tratta");
 		nameText.setLayoutData(new RowData(200, 20));
 
 		okButton = new Button(popup, SWT.PUSH);
 		okButton.setText("Ok");
 	
 		createOkButtonListener();
 
 		popup.setLayout(popupLayout);
 		popup.pack();
 	
 	}
 	
 	public void open(){
 		popup.open();
 	}
 
 	public Shell getDialog(){
 		return popup;
 	}
 
 
 	private void createOkButtonListener(){
 		
 		okButton.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event) {
 
 					SortedMap<Integer, Stazione> map;
 					map = new TreeMap<Integer, Stazione>();
 					Tratta trattaSelezionata = manager.getTratta(tratta);
					if(trattaSelezionata.getStazioniTratta().size()<2){
						MessageBox alert = new MessageBox(MainShell.getMainShell().getShell(),SWT.ICON_ERROR | SWT.OK);
						alert.setMessage("La tratta selezionata deve avere almeno 2 stazioni!");
						alert.open();
						return;
					}
 					int distanzaMassima = trattaSelezionata.getStazioniTratta().get(0).getDistanza();
 					for(int i=1; i< trattaSelezionata.getStazioniTratta().size(); i++){
 						if(distanzaMassima < trattaSelezionata.getStazioniTratta().get(i).getDistanza()){
 							distanzaMassima = trattaSelezionata.getStazioniTratta().get(i).getDistanza();
 						}
 					}
 					
 					for(int i=trattaSelezionata.getStazioniTratta().size() -1; i > -1; i--){
 						int distanza = distanzaMassima - trattaSelezionata.getStazioniTratta().get(i).getDistanza();
 						map.put(distanza, trattaSelezionata.getStazioniTratta().get(i).getStazione());
 					}
 					try{
 						manager.createTratta(map, nameText.getText());
 						popup.close();
 					} catch (TrainException e) {
 						MessageBox alert = new MessageBox(MainShell.getMainShell().getShell(),SWT.ICON_ERROR | SWT.OK);
 						alert.setMessage(e.getMessage());
 						alert.open();
 					}
 			}
 		});
 		
 		return;
 		
 	}
}
