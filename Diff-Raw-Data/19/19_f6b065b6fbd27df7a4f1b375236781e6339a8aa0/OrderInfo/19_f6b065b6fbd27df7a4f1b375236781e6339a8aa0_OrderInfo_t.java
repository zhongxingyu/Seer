 package face;
 
import java.sql.Date;
 import java.util.List;
 import java.util.StringTokenizer;
 
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.widgets.Dialog;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.custom.ScrolledComposite;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.h2.jaqu.Db;
 
 import brain.Car;
 import brain.Clients;
 import brain.DBWrapper;
 import brain.Information;
 import brain.Order;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 
 public class OrderInfo extends Dialog {
 
 	protected Object result;
 	protected Shell shell;
 	private Table table;
 	private Text text;
 
 	/**
 	 * Create the dialog.
 	 * @param parent
 	 * @param style
 	 */
 	public OrderInfo(Shell parent, int style) {
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
 		shell.setSize(553, 237);
 		shell.setText("Информация о заказе");
 		
 		Rectangle client=shell.getBounds();
 		Rectangle par=shell.getParent().getBounds();
 		client.x=(par.x+par.width/2)-client.width/2;
 		client.y=(par.y+par.height/2)-client.height/2;
 		shell.setBounds(client);
 		
 		ScrolledComposite scrolledComposite = new ScrolledComposite(shell, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
 		scrolledComposite.setBounds(0, 0, 548, 181);
 		scrolledComposite.setExpandHorizontal(true);
 		scrolledComposite.setExpandVertical(true);
 		
 		table = new Table(scrolledComposite, SWT.BORDER | SWT.FULL_SELECTION);
 		table.setHeaderVisible(true);
 		table.setLinesVisible(true);
 		
 		TableColumn tblclmnNewColumn = new TableColumn(table, SWT.NONE);
 		tblclmnNewColumn.setWidth(117);
 		tblclmnNewColumn.setText("Услуга");
 		
 		TableColumn tblclmnNewColumn_1 = new TableColumn(table, SWT.NONE);
 		tblclmnNewColumn_1.setWidth(202);
 		tblclmnNewColumn_1.setText("Исполнитель");
 		
 		TableColumn tblclmnNewColumn_2 = new TableColumn(table, SWT.NONE);
 		tblclmnNewColumn_2.setWidth(113);
 		tblclmnNewColumn_2.setText("Дата выполнения");
 		scrolledComposite.setContent(table);
 		scrolledComposite.setMinSize(table.computeSize(SWT.DEFAULT, SWT.DEFAULT));
 		
 		TableColumn tblclmnNewColumn_3 = new TableColumn(table, SWT.NONE);
 		tblclmnNewColumn_3.setWidth(100);
 		tblclmnNewColumn_3.setText("Статус");
 		
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
 		
 		Information.fillingTable(table, kodOrder);
 		
 		
 		
 		Label label = new Label(shell, SWT.NONE);
 		label.setBounds(10, 187, 123, 15);
 		label.setText("Общая сумма заказа:");
 		
 		text = new Text(shell, SWT.BORDER);
 		text.setEditable(false);
 		text.setBounds(132, 185, 76, 21);
 		text.setText(Integer.toString(Information.getSumOfServices(kodOrder)));
 	}
 }
