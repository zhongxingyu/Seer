 /**
  * 
  */
 package pl.dmcs.whatsupdoc.client.providers;
 
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 
 import pl.dmcs.whatsupdoc.client.ContentManager;
 import pl.dmcs.whatsupdoc.client.model.PatientCard;
 import pl.dmcs.whatsupdoc.client.services.UserService;
 import pl.dmcs.whatsupdoc.client.services.UserServiceAsync;
 import pl.dmcs.whatsupdoc.shared.Alergy;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.Label;
 
 
 public class PatientCardProvider extends BodyProvider {
 
 	private String PESEL, key;
 	private LinkedHashMap<String, String> infos = new LinkedHashMap<String,String>();
 	private FlowPanel row, column;
 	private Label information, text;
 	private Button button;
 	private ArrayList<String> CSS;
 	private StringBuilder stringAlergy;
 	private AsyncCallback<PatientCard> patientCardCallback = new AsyncCallback<PatientCard>() {
 		
 		@Override
 		public void onSuccess(PatientCard patientCard) {
 			infos.put("Imię", patientCard.getName());
 			infos.put("Nazwisko", patientCard.getSurname());
 			infos.put("Data Urodzin", "25-08-1978");
 			infos.put("Pesel", patientCard.getPESEL());
 			infos.put("Numer ubezpieczenia", "333-23-54355");
 			infos.put("Płeć", "Płeć");
 			infos.put("Miasto", patientCard.getAddress().getCity());
 			infos.put("Kod Pocztowy", "90-441");
 			infos.put("Ulica", patientCard.getAddress().getStreet());
 			infos.put("Telefon", patientCard.getPhone());
 			infos.put("Email", patientCard.getMail());
 			infos.put("Numer domu/mieszkania", patientCard.getAddress().getHouseNumber());
 			key = patientCard.getKeyString();
 			
 			if(patientCard.getAlergies()!=null)
 				for(Alergy alergy: patientCard.getAlergies()){
 					stringAlergy.append(alergy.toString());
 				}
 			else
 				stringAlergy.append("Brak alergii");
 			
 			infos.put("Alergie", stringAlergy.toString());
 			addPatientCardWidgets();
 		}
 		
 		@Override
 		public void onFailure(Throwable caught) {			
 			Label err = new Label("err");
 			mainPanel.add(err);
 		}
 	};
 	
 	
 	/**
 	 * @param cm CoontentManager of this class
 	 * @param Pesel Patient PESEL number.
 	 */
 	
 	public PatientCardProvider(ContentManager cm, String Pesel) {
 		super(cm);
 		this.PESEL=Pesel;
		stringAlergy = new StringBuilder();
 		CSS = new ArrayList<String>();
 		CSS.add("patientCardRow");
 		CSS.add("patientCardColumn");
 		CSS.add("name");
 		CSS.add("confirmButton");
 		CSS.add("value");
 		final UserServiceAsync userService = GWT.create(UserService.class);
 		userService.getPatientCard(PESEL, patientCardCallback);
 		
 	}
 	
 	/**
 	 * Method add widget's to panel.
 	 */
 	
 	private void addPatientCardWidgets(){
 		int a=3;
 		row = new FlowPanel();
 		row.setStyleName(CSS.get(0));
 		
 		for (String key : infos.keySet()) {
 			if(a==0){
 				a=3;
 				mainPanel.add(row);
 				row = new FlowPanel();
 				row.setStyleName(CSS.get(0));
 			}
 			column = new FlowPanel();
 			column.setStyleName(CSS.get(1));
 				text = new Label();
 				text.setStyleName(CSS.get(2));
 				text.setText(key);
 				information = new Label();
 				information.setStyleName(CSS.get(3));
 				information.setText(infos.get(key));
 			column.add(text);
 			column.add(information);
 			row.add(column);
 			a--;
 		}
 		
 			column = new  FlowPanel();
 			column.setStyleName(CSS.get(1));
 				button = new Button();
 				button.setStyleName(CSS.get(3));
 				button.setText("Dodaj Alergie");
 				
 				button.addClickHandler(new ClickHandler() {
 
 					@Override
 					public void onClick(ClickEvent event) {
 					}
 				});
 				
 			column.add(button);
 		row.add(column);
 		
 		mainPanel.add(row);
 		
 		row = new FlowPanel();
 		row.setStyleName(CSS.get(0));
 			column = new  FlowPanel();
 			column.setStyleName(CSS.get(1));
 				button = new Button();
 				button.setStyleName(CSS.get(3));
 				button.setText("Historia Rozpozna�");
 				
 				button.addClickHandler(new ClickHandler() {
 
 					@Override
 					public void onClick(ClickEvent event) {
 						BodyProvider b = new PatientRecognitionsProvider(getCm(), key);
 						getCm().setBody(b);
 						getCm().drawContent();
 					}
 				});
 				
 			column.add(button);
 		row.add(column);
 			
 		row.add(column);
 			column = new  FlowPanel();
 			column.setStyleName(CSS.get(1));
 				button = new Button();
 				button.setStyleName(CSS.get(3));
 				button.setText("Dodaj Rozpoznanie");
 				
 				button.addClickHandler(new ClickHandler() {
 
 					@Override
 					public void onClick(ClickEvent event) {
 					}
 				});
 				
 			column.add(button);
 		row.add(column);
 		
 		mainPanel.add(row);
 	}
 }
