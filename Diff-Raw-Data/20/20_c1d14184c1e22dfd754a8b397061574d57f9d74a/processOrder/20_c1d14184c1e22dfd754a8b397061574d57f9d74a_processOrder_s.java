 package face;
 
 import java.sql.Date;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.StringTokenizer;
 
 import org.eclipse.jface.fieldassist.AutoCompleteField;
 import org.eclipse.jface.fieldassist.ComboContentAdapter;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.widgets.Dialog;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.DateTime;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.h2.jaqu.Db;
 
 import brain.Car;
 import brain.Clients;
 import brain.DBWrapper;
 import brain.Information;
 import brain.Order;
 import brain.Services;
 import brain.Staff;
 
 public class processOrder extends Dialog {
 
 	protected Object result;
 	protected Shell shell;
 	
 	Map<Integer,String> mapSaveAllAvailableServices;
 	Map<Integer,String> mapSaveAllStaff;
 	List<Integer> SetServices;
 	List<Integer> AlreadyServices;
 	String[] ServicesArray;
 	String[] StaffArray;
 	
 	int yLabel_2=72;
 	int yButton_2 = 96;
 	int yButton_1 = 127;
 	List<Label> labelText;
 	List<Label> labelServ;
 	List<Text> textServ;
 	List<String> ls1;
 	
 
 	/**
 	 * Create the dialog.
 	 * @param parent
 	 * @param style
 	 */
 	public processOrder(Shell parent, int style) {
 		super(parent, style);
 		setText("SWT Dialog");
 	}
 
 	/**
 	 * Open the dialog.
 	 * @return the result
 	 */
 	public Object open(String text2, String text3,String desc,String date) {
 		createContents(text2,text3,desc,date);
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
	private void createContents(final String car,final String client1,String desc,String date) {
 		shell = new Shell(getParent(), getStyle());
 		shell.setSize(362, 187);
 		shell.setText("Назначить исполнителя");
 		
 		DBWrapper dbw = new DBWrapper();
 		Db db = dbw.openConnection();
 		Clients c = new Clients();
 		Car cr = new Car();
 		Order o = new Order();
 		StringTokenizer st = new StringTokenizer(client1, " \t\n\r,."); 
 		String Surname = st.nextToken();
 		String Name = st.nextToken();
 		String MiddleName = st.nextToken();
 		int clientKod = db.from(c).where(c.Surname).is(Surname).and(c.Name).is(Name).and(c.Ochestvo).is(MiddleName).selectFirst(c.Kodclient);
 		int carKod = db.from(cr).where(cr.Kodclient).is(clientKod).and(cr.Mark).is(car).selectFirst(cr.Kodavto);
 		final int kodOrder = db.from(o).where(o.Kodavto).is(carKod).and(o.Kodclient).is(clientKod).and(o.Breakage).is(desc).and(o.Dateorder).is(Date.valueOf(date)).selectFirst(o.Kodzakaza);
 		Information i2 = new Information();
 		AlreadyServices=db.from(i2).where(i2.Kodzakaza).is(kodOrder).select(i2.Kodserv);
 		
 		SetServices = new ArrayList<Integer>();
 		Rectangle client=shell.getBounds();
 		Rectangle par=shell.getParent().getBounds();
 		client.x=(par.x+par.width/2)-client.width/2;
 		client.y=(par.y+par.height/2)-client.height/2;
 		shell.setBounds(client);
 		
 		Label label = new Label(shell, SWT.NONE);
 		label.setBounds(10, 10, 91, 15);
 		label.setText("Дата окончания:");
 		
 		final DateTime dateTime = new DateTime(shell, SWT.BORDER);
 		dateTime.setBounds(107, 8, 218, 24);
 		
 		Label label_1 = new Label(shell, SWT.NONE);
 		label_1.setBounds(10, 43, 91, 15);
 		label_1.setText("Исполнитель:");
 		
 		final Combo combo = new Combo(shell, SWT.NONE);
 		combo.setBounds(107, 38, 218, 23);
 		
 		final Button button = new Button(shell, SWT.NONE);
 		button.setBounds(11, 127, 75, 25);
 		button.setText("ОК");
 		
 		final Button button_1 = new Button(shell, SWT.NONE);
 		button_1.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent arg0) {
 				shell.dispose();
 			}
 		});
 		button_1.setText("Отмена");
 		button_1.setBounds(275, 127, 75, 25);
 		
 		labelText = new ArrayList<Label>();
 		labelText.add(new Label(shell, SWT.NONE));
 		labelText.get(labelText.size()-1).setText("Услуга:");
 		labelText.get(labelText.size()-1).setBounds(10, yLabel_2, 91, 15);
 		
 		final Combo combo_1 = new Combo(shell, SWT.NONE);
 		combo_1.setBounds(107, 67, 218, 23);
 
 		mapSaveAllAvailableServices = Services.getAllServices();
 		ls1 = new ArrayList<String>();
 		final Set s1=mapSaveAllAvailableServices.entrySet();
         Iterator it1=s1.iterator();
         while(it1.hasNext())
         {
         	Map.Entry m =(Map.Entry)it1.next();
         	if(!AlreadyServices.isEmpty())
         	{
         		if(!AlreadyServices.contains(m.getKey()))
         		{
         			String temp = (String)m.getValue();
         			combo_1.add(temp);
         			ls1.add(temp);
         		}
         	} else
         	{
         		String temp = (String)m.getValue();
     			combo_1.add(temp);
     			ls1.add(temp);
         	}
         }
         
         ServicesArray = new String[ls1.size()];
 		ls1.toArray(ServicesArray);
 		new AutoCompleteField(combo_1, new ComboContentAdapter(), ServicesArray);
 		
 		mapSaveAllStaff = Staff.getAllAvailableStaff();
 		List<String> ls2 = new ArrayList<String>();
 		final Set s2=mapSaveAllStaff.entrySet();
         Iterator it2=s2.iterator();
         while(it2.hasNext())
         {
         	Map.Entry m =(Map.Entry)it2.next();
         	String temp = (String)m.getValue();
         	combo.add(temp);
         	ls2.add(temp);
         }
         
         StaffArray = new String[ls2.size()];
 		ls2.toArray(StaffArray);
 		new AutoCompleteField(combo, new ComboContentAdapter(), StaffArray);
 		
 		final Button button_3 = new Button(shell, SWT.NONE);
 		final Button button_2 = new Button(shell, SWT.NONE);
 		button_2.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent arg0) {
 				
 				int y = labelText.get(labelText.size()-1).getBounds().y+29;
 				labelText.add(new Label(shell, SWT.NONE));
 				labelText.get(labelText.size()-1).setBounds(10, y, 91, 15);
 				labelText.get(labelText.size()-1).setText("Услуга:");
 				
 				button.setBounds(11, button.getBounds().y+29, 75, 25);
 				button_1.setBounds(275, button_1.getBounds().y+29, 75, 25);
 				button_2.setBounds(126,button_2.getBounds().y+29, 103, 25);
 				button_3.setBounds(97,button_3.getBounds().y+29, 167, 25);
 				combo_1.setBounds(107, combo_1.getBounds().y+29, 218, 23);
 				
 				Point p = shell.getSize();
 				p.y=p.y+29;
 				shell.setSize(p);
 				Rectangle client=shell.getBounds();
 				Rectangle par=shell.getParent().getBounds();
 				client.x=(par.x+par.width/2)-client.width/2;
 				client.y=(par.y+par.height/2)-client.height/2;
 				shell.setBounds(client);
 				
 				Iterator it;
 				it=mapSaveAllAvailableServices.entrySet().iterator();
 				
 				while(it.hasNext())
 				{
 					Map.Entry m =(Map.Entry)it.next();
 					if(m.getValue().toString().equals(combo_1.getText()))
 					{
 						SetServices.add((Integer)m.getKey());
 					}
 				}
 				
 				textServ = new ArrayList<Text>();
 				textServ.add(new Text(shell, SWT.BORDER));
 				Rectangle pc = combo_1.getBounds();
 				pc.y=pc.y-29;
 				textServ.get(textServ.size()-1).setText(combo_1.getText());
 				textServ.get(textServ.size()-1).setBounds(pc);
 				textServ.get(textServ.size()-1).setEditable(false);
 				
 				combo_1.removeAll();
 				ls1.clear();
 				final Set s1=mapSaveAllAvailableServices.entrySet();
 		        Iterator it1=s1.iterator();
 		        while(it1.hasNext())
 		        {
 		        	Map.Entry m =(Map.Entry)it1.next();
 			        if(!SetServices.contains(m.getKey()) && !AlreadyServices.contains(m.getKey()))
 			        {
 			        	String temp = (String)m.getValue();
 			        	combo_1.add(temp);
 			        	ls1.add(temp);
 		        	}
 		        }
 		        
 		        ServicesArray = new String[ls1.size()];
 				ls1.toArray(ServicesArray);
 				new AutoCompleteField(combo_1, new ComboContentAdapter(), ServicesArray);
 			}
 		});
 		button_2.setBounds(126, 96, 103, 25);
 		button_2.setText("Добавить услугу");
 		
 		
 		button_3.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent arg0) {
 				ProcessOrderServ ds = new ProcessOrderServ(shell,SWT.DIALOG_TRIM);
				ds.open(car,client1);
 				shell.dispose();
 			}
 		});
 		button_3.setText("Обработать текущие услуги");
 		button_3.setBounds(97, 127, 167, 25);
 		
 		button.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent arg0) {
 				int kodStaff = 0;
 				Iterator it;
 				it=mapSaveAllStaff.entrySet().iterator();
 				while(it.hasNext())
 				{
 					Map.Entry m =(Map.Entry)it.next();
 					if(m.getValue().toString().equals(combo.getText()))
 					{
 						kodStaff=(Integer) m.getKey();
 					}
 				}
 				if(!combo_1.getText().isEmpty())
 				{
 					int kodServ=0;
 					it=mapSaveAllAvailableServices.entrySet().iterator();
 					while(it.hasNext())
 					{
 						Map.Entry m =(Map.Entry)it.next();
 						if(m.getValue().toString().equals(combo_1.getText()))
 						{
 							kodServ=(Integer) m.getKey();
 						}
 					}
 					Information.addInfo(kodOrder, kodStaff, 
 							new Date(dateTime.getYear()-1900,dateTime.getMonth(),dateTime.getDay()),kodServ);
 				}
 				if(!SetServices.isEmpty())
 				{
 					Iterator it1 = SetServices.iterator();
 					while(it1.hasNext())
 					{
 						Information.addInfo(kodOrder, kodStaff, 
 								new Date(dateTime.getYear()-1900,dateTime.getMonth(),dateTime.getDay()),(Integer) it1.next());
 					}
 				}
 				shell.dispose();
 
 			}
 		});
 		
 		
 	}
 }
