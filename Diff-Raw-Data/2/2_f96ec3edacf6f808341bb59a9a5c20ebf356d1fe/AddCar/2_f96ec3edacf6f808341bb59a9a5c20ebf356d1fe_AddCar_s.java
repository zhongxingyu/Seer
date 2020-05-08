 package face;
 
 import java.sql.Date;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.jface.bindings.keys.KeyStroke;
 import org.eclipse.jface.fieldassist.AutoCompleteField;
 import org.eclipse.jface.fieldassist.ComboContentAdapter;
 import org.eclipse.jface.fieldassist.ContentProposalAdapter;
 import org.eclipse.jface.fieldassist.SimpleContentProposalProvider;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Dialog;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Spinner;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.swt.widgets.DateTime;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.events.*;
 import org.eclipse.core.*;
 
 import brain.Car;
 import brain.Clients;
 import brain.Warranty;
 
 public class AddCar extends Dialog {
 
 	protected Object result;
 	protected Shell shell;
 	protected Shell parent;
 	private Text text;
 	private Text text_1;
 	private Text text_2;
 	private Spinner text_3;
 	
 	
 
 	/**
 	 * Create the dialog.
 	 * @param parent
 	 * @param style
 	 */
 	public AddCar(Shell parent, int style) {
 		super(parent, style);
 		this.parent=parent;
 		setText("SWT Dialog");
 	}
 
 	/**
 	 * Open the dialog.
 	 * @return the result
 	 */
 	public Object open() {
 		createContents();
 		shell.open();
 		shell.layout();
 		Display display = getParent().getDisplay();
 		while (!shell.isDisposed()) {
 			if (!display.readAndDispatch()) {
 				display.sleep();
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Create contents of the dialog.
 	 */
 	
 	
 	private void createContents() {
 		shell = new Shell(getParent(), getStyle());
 		shell.setSize(283, 346);
 		shell.setText("Добавить машину");
 		
 		Rectangle client=shell.getBounds();
 		Rectangle par=parent.getBounds();
 		client.x=(par.x+par.width/2)-client.width/2;
 		client.y=(par.y+par.height/2)-client.height/2;
 		shell.setBounds(client);
 		
		String[] listCar = {"BMW","Audi","Запор"};
 		String[] warr = {"1 год","2 года","3 года"};
 		
 		final Combo combo = new Combo(shell, SWT.NONE);
 		
 		Label label = new Label(shell, SWT.NONE);
 		label.setBounds(10, 10, 55, 15);
 		label.setText("Владелец:");
 		
 
 		Label label_1 = new Label(shell, SWT.NONE);
 		label_1.setText("Марка:");
 		label_1.setBounds(10, 44, 39, 15);
 		
 		final Combo combo_1 = new Combo(shell, SWT.NONE);
 		combo_1.setBounds(71, 42, 196, 23);
 		combo_1.setItems(listCar);
 		new AutoCompleteField(combo_1, new ComboContentAdapter(), listCar);
 		
 		Label label_2 = new Label(shell, SWT.NONE);
 		label_2.setText("Кузов:");
 		label_2.setBounds(10, 78, 39, 15);
 		
 		text = new Text(shell, SWT.BORDER);
 		text.setBounds(71, 76, 196, 21);
 		
 		Label lblVin = new Label(shell, SWT.NONE);
 		lblVin.setText("VIN:");
 		lblVin.setBounds(10, 112, 39, 15);
 		
 		Label label_3 = new Label(shell, SWT.NONE);
 		label_3.setText("Цвет:");
 		label_3.setBounds(10, 146, 39, 15);
 
 		Label label_6 = new Label(shell, SWT.NONE);
 		label_6.setText("Цена:");
 		label_6.setBounds(10, 180, 39, 15);
 		
 		text_1 = new Text(shell, SWT.BORDER);
 		text_1.setBounds(71, 110, 196, 21);
 		
 		text_2 = new Text(shell, SWT.BORDER);
 		text_2.setBounds(71, 144, 196, 21);
 		
 		text_3 = new Spinner(shell, SWT.BORDER);
 		text_3.setBounds(71, 178, 196, 21);
 		text_3.setMaximum(10000000);
 		
 		Label label_4 = new Label(shell, SWT.NONE);
 		label_4.setText("Год выпуска:");
 		label_4.setBounds(10, 216, 71, 15);
 		
 		Label label_5 = new Label(shell, SWT.NONE);
 		label_5.setText("Гарантия:");
 		label_5.setBounds(10, 250, 55, 15);
 		
 	
 		
 		final DateTime dateTime = new DateTime(shell, SWT.BORDER);
 		dateTime.setBounds(87, 214, 76, 24);
 		
 		final Combo combo_2 = new Combo(shell, SWT.NONE);
 		combo_2.setBounds(71, 248, 196, 23);
 		combo_2.setItems(warr);
 		new AutoCompleteField(combo_2, new ComboContentAdapter(), warr);
 		
 		Button button = new Button(shell, SWT.NONE);
 		button.setBounds(44, 282, 75, 25);
 		button.setText("ОК");
 		
 		Button button_1 = new Button(shell, SWT.NONE);
 		button_1.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent arg0) {
 				shell.dispose();
 			}
 		});
 		button_1.setBounds(163, 282, 75, 25);
 		button_1.setText("Отмена");
 		
 		
 		
 		Map<Integer,String> mapSaveClientToId = Clients.getClientsList();
 		List<String> ls = new ArrayList<String>();
 		final Set s=mapSaveClientToId.entrySet();
         Iterator it=s.iterator();
         while(it.hasNext())
         {
         	Map.Entry m =(Map.Entry)it.next();
         	String temp = (String)m.getValue();
         	combo.add(temp);
         	ls.add(temp);
         }
         
         String[] ClientsArray = new String[ls.size()];
 		ls.toArray(ClientsArray);
 		
 		new AutoCompleteField(combo, new ComboContentAdapter(), ClientsArray);
 		combo.setBounds(71, 8, 196, 23);
 		
 		button.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent arg0) {
 				Iterator it=s.iterator();
 				int kodclient = 0;
 				while(it.hasNext())
 				{
 					Map.Entry m =(Map.Entry)it.next();
 					if(m.getValue().toString().equals(combo.getText()))
 					{
 						kodclient=(Integer) m.getKey();
 					}
 				}
 				Car.addCar(kodclient,combo_1.getText(),text.getText(),text_1.getText(),text_2.getText(),
 						new Date(dateTime.getYear()-1900,dateTime.getMonth(),dateTime.getDay()),
 						Integer.parseInt(text_3.getText()),Warranty.addWarranty(combo_2.getText()));
 				shell.dispose();
 				
 			}
 		});
 
 	}
 }
