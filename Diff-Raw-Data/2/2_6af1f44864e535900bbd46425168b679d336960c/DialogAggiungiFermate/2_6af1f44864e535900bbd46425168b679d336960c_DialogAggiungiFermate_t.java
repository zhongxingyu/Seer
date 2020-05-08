 package interfaccia;
 
 import java.sql.Time;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import modelloTreni.Corsa;
 import modelloTreni.Fermata;
 import modelloTreni.StazioneTratta;
 import modelloTreni.TrainException;
 import modelloTreni.TrainManager;
 import modelloTreni.Tratta;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.layout.RowLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.DateTime;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.MessageBox;
 import org.eclipse.swt.widgets.Shell;
 
 public class DialogAggiungiFermate {
 	private MainShell mainWindow = MainShell.getMainShell();
 	private TrainManager manager = TrainManager.getInstance();
 	private Corsa corsa;
 
 	private Shell shell;
 	private Label fermata;
 	private Combo fermateCombo;
 	
 	private DateTime timeSpinner;
 	private Composite timeGroup;
 	private Label timeLabel;
 	private Button ok;
 
 	public DialogAggiungiFermate(Corsa corsa) {
 		this.corsa = corsa;
 
 		shell = new Shell(mainWindow.getShell(), 65616);
 		fermata = new Label(shell, SWT.NONE);
 		fermata.setText("Fermata");
 
 		fermateCombo = new Combo(shell, SWT.DROP_DOWN);
 		initFermateCombo();
 		createTimeGroup();
 
 		ok = new Button(shell, SWT.PUSH);
 		ok.setText("Ok");
 		ok.setEnabled(false);
 
 		createFermateComboListener();
 		createOkButtonListener();
 
 		shell.setLayout(new RowLayout(SWT.VERTICAL));
 		shell.pack();
 		shell.open();
 	}
 
 	public Shell getDialog() {
 		return shell;
 	}
 
 	public void showDialog() {
 		shell.open();
 	}
 	
 	private void createTimeGroup(){
 		timeGroup = new Composite(shell, SWT.NONE);
 		timeGroup.setLayout(new FormLayout());
 		
 		timeLabel = new Label(timeGroup, SWT.NONE);
 	    timeLabel.setText("Orario: ");
 		
 		timeSpinner = new DateTime(timeGroup, SWT.TIME | SWT.SHORT);
		timeSpinner.setSeconds(0);
 	    FormData timeSpinnerLayoutData = new FormData();
 		timeSpinnerLayoutData.left = new FormAttachment(timeLabel, 15);
 		timeSpinner.setLayoutData(timeSpinnerLayoutData);
 	}
 
 	private void initFermateCombo() {
 		ArrayList<Fermata> fermate = corsa.getFermate();
 		ArrayList<String> nomiFermate = new ArrayList<String>();
 		for (int i = 0; i < fermate.size(); i++) {
 			nomiFermate.add(fermate.get(i).getStazioneTratta().getStazione()
 					.getNome());
 		}
 
 		Tratta t = corsa.getTratta();
 		List<StazioneTratta> stazioniTratta = t.getStazioniTratta();
 
 		ArrayList<String> fermaterimanenti = new ArrayList<String>();
 
 		for (StazioneTratta st : stazioniTratta) {
 			if (!nomiFermate.contains(st.getStazione().getNome())) {
 				fermaterimanenti.add(st.getStazione().getNome());
 			}
 		}
 
 		String[] array;
 		if (fermaterimanenti.size() != 0) {
 			array = new String[fermaterimanenti.size()];
 			array = fermaterimanenti.toArray(array);
 		} else {
 			array = new String[1];
 			array[0] = "Non ci sono fermate disponibili";
 		}
 
 		fermateCombo.setItems(array);
 	}
 
 	private void createFermateComboListener() {
 		fermateCombo.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event) {
 				if (fermateCombo.getText() != null) {
 					ok.setEnabled(true);
 				} else {
 					ok.setEnabled(false);
 				}
 			}
 		});
 	}
 	
 	private void createOkButtonListener() {
 		ok.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event) {
 				try {
 					
 					Calendar calendar = new GregorianCalendar();
 					calendar.set(Calendar.HOUR_OF_DAY, timeSpinner.getHours());
 					calendar.set(Calendar.MINUTE,timeSpinner.getMinutes());
 					calendar.set(Calendar.SECOND,timeSpinner.getSeconds());
 					
 					Time time = new Time(calendar.getTimeInMillis());
 					
 					ArrayList<StazioneTratta> stazionitratta = corsa
 							.getTratta().getStazioniTratta();
 					int idStazione = 0;
 					for (int i = 0; i < stazionitratta.size(); i++) {
 						if (stazionitratta.get(i).getStazione().getNome()
 								.equals(fermateCombo.getText())) {
 							idStazione = stazionitratta.get(i).getId();
 							break;
 						}
 					}
 					try {
 						manager.addFermataToCorsa(corsa.getId(), idStazione,
 								time);
 						shell.close();
 					} catch (TrainException e) {
 						MessageBox alert = new MessageBox(mainWindow.getShell(),SWT.ICON_ERROR | SWT.OK);
 						alert.setMessage(e.getMessage());
 						alert.open();
 					}
 				} catch (IllegalArgumentException e) {
 					MessageBox alert = new MessageBox(mainWindow.getShell(),SWT.ICON_ERROR | SWT.OK);
 					alert.setMessage("Inserisci un valore valido");
 					alert.open();
 					shell.close();
 				} catch (NullPointerException e) {
 					MessageBox alert = new MessageBox(mainWindow.getShell(),SWT.ICON_ERROR | SWT.OK);
 					alert.setMessage("Inserisci un valore valido");
 					alert.open();
 					shell.close();
 				}
 
 			}
 		});
 	}
 
 }
