 package controller.ui;
 
 import java.net.URL;
 import java.util.ResourceBundle;
 
 import javafx.collections.FXCollections;
 import javafx.collections.ObservableList;
 import javafx.event.EventHandler;
 import javafx.fxml.FXML;
 import javafx.fxml.Initializable;
 import javafx.scene.control.Label;
 import javafx.scene.control.ListView;
 import javafx.scene.input.MouseEvent;
 import modello_di_dominio.Commessa;
 import modello_di_dominio.Distinta;
 import modello_di_dominio.Ordine;
 import modello_di_dominio.RigaDistinta;
 import servizi.GestoreOrdine;
 import servizi.GestoreServizi;
 import servizi.Log;
 import servizi.impl.GestoreServiziPrototipo;
 
 public class VisualizzaDistintaController implements Initializable {
     @FXML private ListView<String> listPezziDistinta;
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
 
     final ObservableList<String> listaPezzi = FXCollections.observableArrayList();
     
     public void printSelectedItem(ListView listView) {
         ObservableList<Integer> list = listView.getSelectionModel().getSelectedIndices();
         System.out.println("The selectedIndices property contains: " + list.size() + " element(s):");
         for(int i=0; i<list.size(); i++) { System.out.println(i + ")" + list.get(i)); }
     }
     
 	@Override
 	public void initialize(URL arg0, ResourceBundle arg1) {
 		
 		GestoreServizi gsp = GestoreServiziPrototipo.getGestoreServizi();
 		GestoreOrdine gestoreOrdine = (GestoreOrdine) gsp.getServizio("GestoreOrdineDAO");
		Log log = (Log) gsp.getServizio("LogStdout");
 		
 		Ordine ordine = gestoreOrdine.getOrdine(1);
 		
 		log.i(ordine.getID()+"");
 		
 		//TODO: modifica a getCommessaID(id)
 		Commessa[] commesse = ordine.commesse.toArray();
 		
 		for (int i=0; i < commesse.length; i++) {
 			System.out.println(commesse[i].getID()+" "+commesse[i].getDistinta());
 		}
 		
 		Distinta distinta = commesse[0].getDistinta();
 		
 		final RigaDistinta[] righeDistinta = distinta.righeDistinta.toArray();
 		
 		for (int i=0; righeDistinta.length>i; i++) {
 			listaPezzi.add(righeDistinta[i].getIndicazione());
 		}
 		
 		listPezziDistinta.setItems(listaPezzi);
 		
 	    lbl_modulo.setText("PROSSIMA ITERAZIONE");
 	    lbl_revisione.setText("REV: "+distinta.getRevisione());
 	    lbl_data.setText(distinta.getDataInizio().toGMTString());
 	    lbl_cliente.setText("PROSSIMA ITERAZIONE");
 	    lbl_destinazione.setText(ordine.getDestinazione().getVia());
 	    lbl_elemstrutturale.setText(distinta.getElementoStrutturale());
 	    lbl_cartellino.setText("PROSSIMA ITERAZIONE");
 		
 	    
 	    
 	    listPezziDistinta.setOnMouseClicked(new EventHandler<MouseEvent>() {
 
 	        @Override
 	        public void handle(MouseEvent event) {
 	        	if (listPezziDistinta.getSelectionModel().getSelectedItem()!=null) {
 		            System.out.println("clicked on " + listPezziDistinta.getSelectionModel().getSelectedItem());
 		            
 		            int selected = listPezziDistinta.getSelectionModel().getSelectedIndices().get(0);
 		            
 		            //Aggiorno campi
 		            lbl_codice_pezzo.setText(righeDistinta[selected].getPezzo().getDescrizionePezzo().getNome());
 		            lbl_fornitore.setText(righeDistinta[selected].getPezzo().getDescrizionePezzo().getFornitore());
 		            lbl_n_pezzi.setText(righeDistinta[selected].getPezzo().getQuantita()+"");
 		            lbl_diametro.setText(righeDistinta[selected].getPezzo().getDescrizionePezzo().getDiametro()+"");
 		            lbl_misura_taglio.setText("PROSSIMA ITERAZIONE");
 		            lbl_peso.setText("PROSSIMA ITERAZIONE");
 		            
 		            printSelectedItem(listPezziDistinta);
 	        	}
 	        }
 	    });
 		
 		
 		
 		
 	}
     
     
     
 
 }
