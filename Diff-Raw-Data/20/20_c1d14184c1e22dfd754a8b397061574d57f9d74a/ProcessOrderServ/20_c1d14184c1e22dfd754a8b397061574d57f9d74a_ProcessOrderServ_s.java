 package face;
 
 import java.sql.Date;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.StringTokenizer;
 
 import org.eclipse.jface.fieldassist.AutoCompleteField;
 import org.eclipse.jface.fieldassist.ComboContentAdapter;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.widgets.Dialog;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.swt.widgets.DateTime;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.h2.jaqu.Db;
 
 import brain.Car;
 import brain.Clients;
 import brain.DBWrapper;
 import brain.Information;
 import brain.Order;
 import brain.Staff;
 
 public class ProcessOrderServ extends Dialog {
 
 	protected Object result;
 	protected Shell shell;
 	private Text text;
 	private Text text_1;
 	Map<Integer,String> mapSaveAllServ;
 	String[] ServArray;
 	private Text text_2;
 
 	/**
 	 * Create the dialog.
 	 * @param parent
 	 * @param style
 	 */
 	public ProcessOrderServ(Shell parent, int style) {
 		super(parent, style);
 		setText("SWT Dialog");
 	}
 
 	/**
 	 * Open the dialog.
 	 * @return the result
 	 */
	public Object open(String text2, String text3) {
		createContents(text2,text3);
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
	private void createContents(final String car,final String client1) {
 		shell = new Shell(getParent(), getStyle());
 		shell.setSize(342, 204);
 		shell.setText("Обработать текущие услуги");
 		shell.getParent().getShell().setVisible(false);
 		Rectangle client=shell.getBounds();
 		Rectangle par=shell.getParent().getBounds();
 		client.x=(par.x+par.width/2)-client.width/2;
 		client.y=(par.y+par.height/2)-client.height/2;
 		shell.setBounds(client);
 		
 		Label label = new Label(shell, SWT.NONE);
 		label.setBounds(10, 10, 44, 15);
 		label.setText("Услуга:");
 		
 		final Combo combo = new Combo(shell, SWT.NONE);
 		combo.setBounds(122, 10, 198, 23);
 		
 		text = new Text(shell, SWT.BORDER);
 		text.setEditable(false);
 		text.setBounds(122, 37, 198, 21);
 		
 		Label label_1 = new Label(shell, SWT.NONE);
 		label_1.setBounds(10, 37, 77, 15);
 		label_1.setText("Исполнитель:");
 		
 		Label label_2 = new Label(shell, SWT.NONE);
 		label_2.setText("Дата выполнения:");
 		label_2.setBounds(10, 64, 100, 15);
 		
 		text_1 = new Text(shell, SWT.BORDER);
 		text_1.setEditable(false);
 		text_1.setBounds(122, 64, 198, 21);
 		
 		Button button = new Button(shell, SWT.NONE);
 		button.setBounds(23, 141, 75, 25);
 		button.setText("Удалить");
 		
 		Button button_1 = new Button(shell, SWT.NONE);
 		button_1.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent arg0) {
 				shell.getParent().getShell().dispose();
 			}
 		});
 		button_1.setBounds(244, 141, 75, 25);
 		button_1.setText("Отмена");
 		
 		DBWrapper dbw = new DBWrapper();
 		final Db db = dbw.openConnection();
 		Clients c = new Clients();
 		Car cr = new Car();
 		Order o = new Order();
 		StringTokenizer st = new StringTokenizer(client1, " \t\n\r,."); 
 		String Surname = st.nextToken();
 		String Name = st.nextToken();
 		String MiddleName = st.nextToken();
 		int clientKod = db.from(c).where(c.Surname).is(Surname).and(c.Name).is(Name).and(c.Ochestvo).is(MiddleName).selectFirst(c.Kodclient);
 		int carKod = db.from(cr).where(cr.Kodclient).is(clientKod).and(cr.Mark).is(car).selectFirst(cr.Kodavto);
		final int kodOrder = db.from(o).where(o.Kodavto).is(carKod).and(o.Kodclient).is(clientKod).selectFirst(o.Kodzakaza);
 		
 		mapSaveAllServ = Information.getAllServicesOnThisOrder(kodOrder);
 		List<String> ls2 = new ArrayList<String>();
 		final Set s2=mapSaveAllServ.entrySet();
         Iterator it2=s2.iterator();
         while(it2.hasNext())
         {
         	Map.Entry m =(Map.Entry)it2.next();
         	String temp = (String)m.getValue();
         	combo.add(temp);
         	ls2.add(temp);
         }
         
         ServArray = new String[ls2.size()];
 		ls2.toArray(ServArray);
 		new AutoCompleteField(combo, new ComboContentAdapter(), ServArray);
 		
 		Label label_3 = new Label(shell, SWT.NONE);
 		label_3.setText("Статус:");
 		label_3.setBounds(10, 91, 100, 15);
 		
 		text_2 = new Text(shell, SWT.BORDER);
 		text_2.setEditable(false);
 		text_2.setBounds(122, 91, 198, 21);
 		
 		Button button_2 = new Button(shell, SWT.NONE);
 		button_2.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent arg0) {
 				Iterator it;
 				it=mapSaveAllServ.entrySet().iterator();
 				while(it.hasNext())
 				{
 					Map.Entry m =(Map.Entry)it.next();
 					if(m.getValue().toString().equals(combo.getText()))
 					{
 						Information i = new Information();
 						Information ii =db.from(i).where(i.Kodinfo).is((Integer)m.getKey()).selectFirst();
 						boolean b = ii.Done;
 						if(b==true)
 						{
 							db.from(i).set(i.Done).to(false).where(i.Kodinfo).is((Integer)m.getKey()).update();
 							text_2.setText("Не выполнена");
 							
 						} else
 						{
 							db.from(i).set(i.Done).to(true).where(i.Kodinfo).is((Integer)m.getKey()).update();
 							text_2.setText("Выполнена");
 						}
 					}
 				}
 			}
 		});
 		button_2.setBounds(121, 141, 100, 25);
 		button_2.setText("Изменить статус");
 		
 		combo.addSelectionListener(new SelectionAdapter() {
 			 public void widgetSelected(SelectionEvent e) {
 				Iterator it;
 				it=mapSaveAllServ.entrySet().iterator();
 				while(it.hasNext())
 				{
 					Map.Entry m =(Map.Entry)it.next();
 					if(m.getValue().toString().equals(combo.getText()))
 					{
 						int kodInfo=(Integer) m.getKey();
 						Information ii = new Information();
 						Information i = db.from(ii).where(ii.Kodinfo).is(kodInfo).selectFirst();
 						Staff s = new Staff();
 						text.setText(db.from(s).where(s.Kodstaff).is(i.Kodstaff).selectFirst(s.Surname)+" "+
 								db.from(s).where(s.Kodstaff).is(i.Kodstaff).selectFirst(s.Name)+" "+
 								db.from(s).where(s.Kodstaff).is(i.Kodstaff).selectFirst(s.Ochestvo));
 						
 						text_1.setText(i.FinishDate.toString());
 						if(i.Done==true)
 						{
 							text_2.setText("Выполнена");
 						} else text_2.setText("Не выполнена");
 					}
 				}
 			 }
 		});
 		
 		button.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent arg0) {
 				Iterator it;
 				it=mapSaveAllServ.entrySet().iterator();
 				while(it.hasNext())
 				{
 					Map.Entry m =(Map.Entry)it.next();
 					if(m.getValue().toString().equals(combo.getText()))
 					{
 						Information i = new Information();
 						db.from(i).where(i.Kodinfo).is((Integer)m.getKey()).delete();
 					}
 				}
 				
 				combo.removeAll();
 				text.setText("");
 				text_1.setText("");
 				text_2.setText("");
 				mapSaveAllServ = Information.getAllServicesOnThisOrder(kodOrder);
 				List<String> ls2 = new ArrayList<String>();
 				final Set s2=mapSaveAllServ.entrySet();
 		        Iterator it2=s2.iterator();
 		        while(it2.hasNext())
 		        {
 		        	Map.Entry m =(Map.Entry)it2.next();
 		        	String temp = (String)m.getValue();
 		        	combo.add(temp);
 		        	ls2.add(temp);
 		        }
 		        
 		        ServArray = new String[ls2.size()];
 				ls2.toArray(ServArray);
 				new AutoCompleteField(combo, new ComboContentAdapter(), ServArray);
 				
 			}
 		});
 
 	}
 }
