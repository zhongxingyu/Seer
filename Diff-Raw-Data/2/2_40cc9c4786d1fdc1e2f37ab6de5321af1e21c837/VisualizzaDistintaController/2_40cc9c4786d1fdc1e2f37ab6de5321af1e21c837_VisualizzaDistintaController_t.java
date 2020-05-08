 package controller.ui;
 
 import java.net.URL;
 import java.time.LocalDate;
 import java.time.format.DateTimeFormatter;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.ResourceBundle;
 import java.util.Set;
 
 import javafx.collections.FXCollections;
 import javafx.collections.ObservableList;
 import javafx.event.EventHandler;
 import javafx.fxml.FXML;
 import javafx.fxml.FXMLLoader;
 import javafx.fxml.Initializable;
 import javafx.scene.Node;
 import javafx.scene.Parent;
 import javafx.scene.Scene;
 import javafx.scene.control.Button;
 import javafx.scene.control.DatePicker;
 import javafx.scene.control.Label;
 import javafx.scene.control.Labeled;
 import javafx.scene.control.ListCell;
 import javafx.scene.control.ListView;
 import javafx.scene.control.TextField;
 import javafx.scene.control.TitledPane;
 import javafx.scene.input.MouseEvent;
 import javafx.scene.layout.Pane;
 import javafx.stage.Modality;
 import javafx.stage.Stage;
 import javafx.util.Callback;
 import javafx.util.StringConverter;
 import modello_di_dominio.Commessa;
 import modello_di_dominio.Distinta;
 import modello_di_dominio.LavorazionePezzo;
 import modello_di_dominio.Ordine;
 import modello_di_dominio.Pezzo;
 import modello_di_dominio.RigaDistinta;
 import servizi.GestoreCommessa;
 import servizi.GestoreDistinta;
 import servizi.GestoreOrdine;
 import servizi.GestoreRigaDistinta;
 import servizi.GestoreServizi;
 import servizi.Log;
 import servizi.Sessione;
 import servizi.impl.GestoreServiziPrototipo;
 
 public class VisualizzaDistintaController implements Initializable {
 	
     @FXML private ListView<RigaDistinta> listPezziDistinta;
     @FXML private Label lbl_modulo;
     @FXML private Label lbl_revisione;
     @FXML private Label lbl_data;
     @FXML private Label lbl_cliente;
     @FXML private Label lbl_destinazione;
     @FXML private Label lbl_elemstrutturale;
     @FXML private Label lbl_cartellino;
     
     @FXML private Label lbl_n_pezzi;
     @FXML private Label lbl_diametro;
     @FXML private Label lbl_peso;
     @FXML private Label lbl_misura_taglio;
     @FXML private Label lbl_codice_pezzo;
     @FXML private Label lbl_fornitore;
     
     @FXML private Button modificaDistButton;
     @FXML private Button salvaDistButton;
     @FXML private Button aggiungiPezzoButton;
     @FXML private Button modificaPezzoButton;
     @FXML private Button rimuoviPezzoButton;
     
     @FXML private TitledPane righe_distinta;
     @FXML private TitledPane informazioni_distinta;
     
     @FXML private DatePicker datePicker = new DatePicker();
 	
     
     private Map<String,Label> distintaLabels;
     private Map<String,TextField> distintaTextFields;
     private Map<String,Object> rigaDistintaNodes;
     private Map<String,Object> rigaDistintaTextFields;
     
     private GestoreDistinta gestoreDistinta;
     private GestoreRigaDistinta gestoreRigaDistinta;
     private GestoreOrdine gestoreOrdine;
     private GestoreCommessa gestoreCommessa;
     private Log log;
     private Sessione session;
     
     Ordine ordine;
     Commessa[] commesse;
     Distinta distinta;
     RigaDistinta[] righeDistinta;
     
     private Boolean modificandoDistinta = false;
     private Boolean modificandoRigaDistinta = false;
 
     final ObservableList<RigaDistinta> listaPezzi = FXCollections.observableArrayList();
     
     public void printSelectedItem(ListView listView) {
         ObservableList<Integer> list = listView.getSelectionModel().getSelectedIndices();
         System.out.println("The selectedIndices property contains: " + list.size() + " element(s):");
         for(int i=0; i<list.size(); i++) { System.out.println(i + ")" + list.get(i)); }
     }
     
 	@Override
 	public void initialize(URL arg0, ResourceBundle arg1) {
 		
 		salvaDistButton.setDisable(true);
 		modificaPezzoButton.setDisable(true);
 		rimuoviPezzoButton.setDisable(true);
 		
 		//Caricamento servizi
 		GestoreServizi gsp = GestoreServiziPrototipo.getGestoreServizi();
 		gestoreOrdine = (GestoreOrdine) gsp.getServizio("GestoreOrdineDAO");
 		gestoreCommessa = (GestoreCommessa) gsp.getServizio("GestoreCommessaDAO");
 		gestoreDistinta = (GestoreDistinta) gsp.getServizio("GestoreDistintaDAO");
 		gestoreRigaDistinta = (GestoreRigaDistinta) gsp.getServizio("GestoreRigaDistintaDAO");
 		log = (Log) gsp.getServizio("LogStdout");
 		session = (Sessione) gsp.getServizio("SessionePrototipo");
 		
 		//Prendo il primo ordine per ora
 		ordine = gestoreOrdine.getOrdine(1);
 		
 		log.i(String.valueOf(ordine.getID()));
 		
 		
 		//TODO: modifica a getCommessaID(id)
 		commesse = ordine.commesse.toArray();
 		
 		for (int i=0; i < commesse.length; i++) {
 			log.i(commesse[i].getID()+" "+commesse[i].getDistinta());
 		}
 		
 		distinta = commesse[0].getDistinta();
 		
 		righeDistinta = distinta.righeDistinta.toArray();
 		
 		listaPezzi.addAll(righeDistinta);
 		
 		//for (int i=0; righeDistinta.length>i; i++) {
 		//	listaPezzi.addAll(righeDistinta);
 		//}
 		
 		listPezziDistinta.setItems(listaPezzi);
 		
 	    lbl_modulo.setText("PROSSIMA ITERAZIONE");
 	    lbl_revisione.setText(distinta.getRevisione()+"");
 	    lbl_data.setText(distinta.getDataInizio().toString());
 	    lbl_cliente.setText("PROSSIMA ITERAZIONE");
 	    lbl_destinazione.setText(ordine.getDestinazione().getVia());
 	    lbl_elemstrutturale.setText(distinta.getElementoStrutturale());
 	    lbl_cartellino.setText("PROSSIMA ITERAZIONE");
 	    
 	    //distinta labels
 	    
 	    distintaLabels = new HashMap<String,Label>();
 	    //distintaLabels.put("modulo",lbl_modulo);
 	    distintaLabels.put("revisione",lbl_revisione);
 	    //distintaLabels.put("data",lbl_data);
 	    //distintaLabels.put("cliente",lbl_cliente);
 	    distintaLabels.put("destinazione",lbl_destinazione);
 	    distintaLabels.put("elemstrutturale",lbl_elemstrutturale);
 	    //distintaLabels.put("cartellino",lbl_cartellino);
 	    
 	    //rigaDistinta nodes
 	    
 	    rigaDistintaNodes = new HashMap<String,Object>();
 	    rigaDistintaNodes.put("n_pezzi", lbl_n_pezzi);
 	    rigaDistintaNodes.put("diametro", lbl_diametro);
 	    rigaDistintaNodes.put("peso", lbl_peso);
 	    rigaDistintaNodes.put("misura_taglio", lbl_misura_taglio);
 	    rigaDistintaNodes.put("fornitore", lbl_fornitore);
 		
 	    //CellFactory
 	    listPezziDistinta.setCellFactory(new Callback<ListView<RigaDistinta>, 
 	            ListCell<RigaDistinta>>() {
 	                @Override 
 	                public ListCell<RigaDistinta> call(ListView<RigaDistinta> list) {
 	                    return new RigaDistCell();
 	                }
 	            }
 	        );
 	    
 	    //Listener
 	    listPezziDistinta.setOnMouseClicked(new EventHandler<MouseEvent>() {
 
 	        @Override
 	        public void handle(MouseEvent event) {
 	        	
 	        	
 	        	
 	        	if (listPezziDistinta.getSelectionModel().getSelectedItem()!=null) {
 		            System.out.println("clicked on " + listPezziDistinta.getSelectionModel().getSelectedItem());
 		            
 		            RigaDistinta riga = listPezziDistinta.getSelectionModel().getSelectedItem();
 		            
 		            //Aggiorno campi
 		            lbl_codice_pezzo.setText(riga.getPezzo().getDescrizionePezzo().getNome());
 		            lbl_fornitore.setText(riga.getPezzo().getDescrizionePezzo().getFornitore());
 		            lbl_n_pezzi.setText(riga.getPezzo().getQuantita()+"");
 		            lbl_diametro.setText(riga.getPezzo().getDescrizionePezzo().getDiametro()+"");
 		            lbl_misura_taglio.setText("PROSSIMA ITERAZIONE");
 		            lbl_peso.setText("PROSSIMA ITERAZIONE");
 		            
 		            modificaPezzoButton.setDisable(false);
 		            rimuoviPezzoButton.setDisable(false);
 		            
 		            printSelectedItem(listPezziDistinta);
 	        	}
 	        	
 	        }
 	    });
 		
 		
 		
 		
 	}
 /**
  * modificaDatiDistinta
  */
 	@FXML
 	protected void modificaDatiDistinta(){
 		
 		log.i("Modifica dei dati della distinta");
 		
 		//Check
 		if(modificandoDistinta != false){
 			return;
 		}
 		
 		Set<Map.Entry<String, Label>> insieme = distintaLabels.entrySet(); 
 		Iterator<Map.Entry<String, Label>> iterator = insieme.iterator();
 		
 		distintaTextFields = new HashMap<String, TextField>();
 		
 		while(iterator.hasNext() != false){
 			
 			Map.Entry<String, Label> entry = iterator.next();
 			
 			Parent p = entry.getValue().getParent();
 			TextField tf = new TextField();
 			tf.setText(entry.getValue().getText());
 			Pane tps = (Pane) p;
 			tps.getChildren().remove(entry.getValue());
 			tps.getChildren().add(tf);
 			
 			distintaTextFields.put(entry.getKey(), tf);
 		}
 
 		String current_date = lbl_data.getText();
 		Parent datePickParent = lbl_data.getParent();
 		
 		final String pattern = "yyyy-MM-dd";
 
 		datePicker.setPromptText(pattern.toLowerCase());
 
 		datePicker.setConverter(new StringConverter<LocalDate>() {
 		     DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(pattern);
 
 		     @Override 
 		     public String toString(LocalDate date) {
 		         if (date != null) {
 		             return dateFormatter.format(date);
 		         } else {
 		             return "";
 		         }
 		     }
 
 		     @Override 
 		     public LocalDate fromString(String string) {
 		         if (string != null && !string.isEmpty()) {
 		             return LocalDate.parse(string, dateFormatter);
 		         } else {
 		             return null;
 		         }
 		     }
 		 });
 		String[] split = current_date.split("-");
 		LocalDate ld = LocalDate.of(Integer.parseInt(split[0]),Integer.parseInt(split[1]),Integer.parseInt(split[2]));
 			//	new LocalDate(getNumber(split[0]),getNumber(split[1]),getNumber(split[2]))
 		datePicker.setValue(ld);
 		
 		Pane tps = (Pane) datePickParent;
 		tps.getChildren().remove(lbl_data);
 		tps.getChildren().add(datePicker);
 		
 		//Flag
 		modificandoDistinta = true;
 		//Disabilito le modifiche
 		modificaDistButton.setDisable(true);
 		//Abilito il salvataggio
 		salvaDistButton.setDisable(false);
 	}
 /**
  * 
  */
 	@FXML
 	protected void modificaRigaDistinta(){
 		
 		log.i("Modifica della riga distinta");
 		
 		if(modificandoRigaDistinta != true){
 		
 			Set<Map.Entry<String, Object>> insieme = rigaDistintaNodes.entrySet();
 			Iterator<Map.Entry<String, Object>> iterator = insieme.iterator();
 			
 			rigaDistintaTextFields = new HashMap<String,Object>();
 			
 			while(iterator.hasNext()){
 				
 				Map.Entry<String, Object> entry = iterator.next();
 				
 				Parent p = ((Node) entry.getValue()).getParent();
 				TextField tf = new TextField();
 				tf.setText(((Label) entry.getValue()).getText());
 				Pane tps = (Pane) p;
 				System.out.println(entry.getKey());
 				tps.getChildren().remove(entry.getValue());
 				tps.getChildren().add(tf);
 				
 				rigaDistintaTextFields.put(entry.getKey(), tf);
 
 			}
 			
 			aggiungiPezzoButton.setDisable(true);
 			modificaPezzoButton.setText("Salva");
 			rimuoviPezzoButton.setDisable(true);
 			
 			modificandoRigaDistinta = true;
 			
 		}else{
 			
 			Set<Map.Entry<String, Object>> insieme = rigaDistintaTextFields.entrySet();
 			Iterator<Map.Entry<String, Object>> iterator = insieme.iterator();
 			
 			while(iterator.hasNext()){
 				
 				Map.Entry<String, Object> entry = iterator.next();
 				Parent p = ((Node) entry.getValue()).getParent();
 				
 				Pane tps = (Pane) p;
 				tps.getChildren().remove(entry.getValue());
 				
 				((Label) rigaDistintaNodes.get(entry.getKey())).setText(((TextField) entry.getValue()).getText());
 				
				tps.getChildren().add((Node) rigaDistintaNodes.get(entry.getKey()));
 				
 			}
 			
 			aggiungiPezzoButton.setDisable(false);
 			modificaPezzoButton.setText("Modifica");
 			rimuoviPezzoButton.setDisable(false);
 			modificandoRigaDistinta = false;
 		}
 		
 	}
 /**
  * 
  */
 	@FXML 
 	protected void salvaDatiDistinta(){
 		
 		log.i("Salvataggio dati distinta");
 		
 		listPezziDistinta.getSelectionModel().getSelectedItem();
 				
 		if(modificandoDistinta != true){
 			return;
 		}
 		
 		Set<Map.Entry<String, TextField>> insieme = distintaTextFields.entrySet(); 
 		Iterator<Map.Entry<String, TextField>> iterator = insieme.iterator();
 		
 		while(iterator.hasNext()){
 			
 			Map.Entry<String, TextField> entry = iterator.next();
 			Parent p = entry.getValue().getParent();
 			
 			Pane tps = (Pane) p;
 			tps.getChildren().remove(entry.getValue());
 			distintaLabels.get(entry.getKey()).setText(entry.getValue().getText());
 			
 			tps.getChildren().add(distintaLabels.get(entry.getKey()));
 			
 		}
 		/*
 	    @FXML private Label lbl_modulo;
 	    @FXML private Label lbl_revisione;
 	    @FXML private Label lbl_data;
 	    @FXML private Label lbl_cliente;
 	    @FXML private Label lbl_destinazione;
 	    @FXML private Label lbl_elemstrutturale;
 	    @FXML private Label lbl_cartellino;
 		
 		distinta.setDataInizio(lbl_data.getText());
 		distinta.setElementoStrutturale(lbl_elemstrutturale.getText());
 		distinta.setModello(value);
 		distinta.setRevisione(lbl_revisione.getText());
 		*/
 		
 		Parent datePickParent = datePicker.getParent();
 		LocalDate ld = datePicker.getValue();
 		
 		Pane tps = (Pane) datePickParent;
 		tps.getChildren().remove(datePicker);
 		tps.getChildren().add(lbl_data);
 		lbl_data.setText(ld.toString());
 		
 		Calendar cal = Calendar.getInstance();
 		cal.set(ld.getYear(), ld.getMonthValue()-1, ld.getDayOfMonth()); //year is as expected, month is zero based, date is as expected
 		Date dt = cal.getTime();
 		
 		gestoreDistinta.modificaDistintaByID(distinta.getID(), dt, distinta.getCommessa(), getNumber(lbl_revisione.getText()), distinta.getModello(), lbl_elemstrutturale.getText());
 		gestoreOrdine.modificaDestinazione(ordine, lbl_destinazione.getText());
 		//gestoreCommessa.
 		//gestoreOrdine.
 		//distinta.setElementoStrutturale(lbl_elemstrutturale.getText());
 		
 		modificandoDistinta = false;
 		modificaDistButton.setDisable(false);
 		salvaDistButton.setDisable(true);
 		
 		
 	}
 	
 	//TODO: classe di utilit
 	public int getNumber(final String str){
 	    final String onlyNumbers = str.replaceAll("[^0-9]", "");
 	    int a = (onlyNumbers.length() > 0 ? onlyNumbers.charAt(0) - '0' : -1);
 	    System.out.println(a);
 	    return a;
 	}
 	
 /**
  *  aggiungiPezzo
  */
 	@FXML 
 	protected void aggiungiPezzo(){
 		
 		log.i("Aggiungi pezzo");
 		
 		
 		Stage popupStage = new Stage();
 		popupStage.initModality(Modality.APPLICATION_MODAL);
 		
 		//popupStage.setScene(new Scene(new Group(new Text(10,10, "my second window"))));
 		
 		
 		Parent root = null;
 		try {
 			root = FXMLLoader.load(getClass().getResource("../../ui/fxml/aggiungi_pezzo.fxml"));
 		} catch (Exception e) {
 			// TODO: handle exception
 			e.printStackTrace();
 		}
 		
 		Scene scene = new Scene(root);
 		popupStage.setScene(scene);
 		
 		//blocking
 		popupStage.showAndWait();
 		
 		Pezzo p = (Pezzo) session.get("pezzo_aggiunto");
 		RigaDistinta riga = new RigaDistinta();
 		listaPezzi.add(riga);
 		gestoreRigaDistinta.creaRigaDistinta(p, distinta, new LavorazionePezzo(), "test_string");
 		
 	}
     
 	@FXML
 	protected void rimuoviPezzo(){
 		
 		log.i("Rimuovi pezzo");
 		
 		RigaDistinta riga = listPezziDistinta.getSelectionModel().getSelectedItem();
 		listaPezzi.remove(riga);
 		gestoreRigaDistinta.cancellaRigaDistinta(riga);
 	}
 	
 	static class RigaDistCell extends ListCell<RigaDistinta> {
 		@Override
 		public void updateItem(RigaDistinta item, boolean empty) {
             super.updateItem(item, empty);
             if(item != null){
             	Label l = new Label(item.getIndicazione());
             	setGraphic(l);
             }
         }
     }
 	
 	
 }
