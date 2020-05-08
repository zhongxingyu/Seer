 import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.TimeZone;
 import java.util.Vector;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.RowSorter;
 import javax.swing.RowSorter.SortKey;
 import javax.swing.SortOrder;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.table.TableModel;
 import java.math.BigDecimal;
 
 public class SQLReadSingleLogic {
 
 	public static void main(String[] args) throws IOException {
 
 		Sql reader = new Sql();
		final String emisora = "BIMBO";
 		final String serie = "";
 		final Calendar day = new GregorianCalendar(2010,10,16);
 		ArrayList<Orden> ordenes = reader.readIniciales(emisora, serie, day);
 		reader.read(ordenes, emisora, serie, day);
 
 		FileWriter fw = new FileWriter("ordenes.txt");
 		final BufferedWriter bw = new BufferedWriter(fw);
 
 		FileWriter fw2 = new FileWriter("ordenes2.txt");
 		final BufferedWriter bw2 = new BufferedWriter(fw2);
 
 		FileWriter fwc = new FileWriter("compra.txt");
 		final BufferedWriter bwc = new BufferedWriter(fwc);
 
 		FileWriter fwv = new FileWriter("venta.txt");
 		final BufferedWriter bwv = new BufferedWriter(fwv);
 
 		FileWriter fwd = new FileWriter("distancia.txt");
 		final BufferedWriter bwd = new BufferedWriter(fwd);
 
 		JFrame frame = new JFrame();
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 		final DefaultTableModel modelVenta = new DefaultTableModel() {
 			@Override
 			public Class getColumnClass(int column) {
 				if (column == 1) {
 					return BigDecimal.class;
 				}
 				return Object.class;
 			}
 		};
 		final DefaultTableModel modelCompra = new DefaultTableModel() {
 			@Override
 			public Class getColumnClass(int column) {
 				if (column == 1) {
 					return BigDecimal.class;
 				}
 				return Object.class;
 			}
 		};
 		final DefaultTableModel modelExec = new DefaultTableModel();
 		final DefaultTableModel modelExec2 = new DefaultTableModel();
 		final DefaultTableModel modelOrdenes = new DefaultTableModel();
 
 		final JTable tableVenta = new JTable(modelVenta);
 		tableVenta.setAutoCreateRowSorter(true);
 		final JTable tableCompra = new JTable(modelCompra);
 		tableCompra.setAutoCreateRowSorter(true);
 		final JTable tableExec = new JTable(modelExec);
 		final JTable tableExec2 = new JTable(modelExec2);
 		final JTable tableOrdenes = new JTable(modelOrdenes);
 
 		JScrollPane scrollPaneVenta = new JScrollPane(tableVenta);
 		JScrollPane scrollPaneCompra = new JScrollPane(tableCompra);
 		JScrollPane scrollPaneExec = new JScrollPane(tableExec);
 		JScrollPane scrollPaneExec2 = new JScrollPane(tableExec2);
 		JScrollPane scrollPaneOrdenes = new JScrollPane(tableOrdenes);
 		scrollPaneVenta.setBounds(0, 0, 400, 250);
 		scrollPaneCompra.setBounds(0, 250, 400, 250);
 		scrollPaneExec.setBounds(400, 0, 400, 250);
 		scrollPaneExec2.setBounds(400, 250, 400, 250);
 		scrollPaneOrdenes.setBounds(0, 500, 800, 200);
 		frame.setLayout(null);
 		frame.add(scrollPaneVenta);
 		frame.add(scrollPaneCompra);
 		frame.add(scrollPaneExec);
 		frame.add(scrollPaneExec2);
 		frame.add(scrollPaneOrdenes);
 		frame.setSize(806, 792);
 		frame.setVisible(true);
 		frame.setResizable(false);
 		ArrayList<SortKey> sortKeysv = new ArrayList<RowSorter.SortKey>();
 		sortKeysv.add(new RowSorter.SortKey(1, SortOrder.DESCENDING));
 		sortKeysv.add(new RowSorter.SortKey(3, SortOrder.DESCENDING));
 		modelVenta.addColumn("");
 		modelVenta.addColumn("Precio");
 		modelVenta.addColumn("Volumen");
 		modelVenta.addColumn("Folio");
 		tableVenta.getRowSorter().setSortKeys(sortKeysv);
 		ArrayList<SortKey> sortKeysc = new ArrayList<RowSorter.SortKey>();
 		sortKeysc.add(new RowSorter.SortKey(1, SortOrder.DESCENDING));
 		sortKeysc.add(new RowSorter.SortKey(3, SortOrder.ASCENDING));
 		modelCompra.addColumn("");
 		modelCompra.addColumn("Precio");
 		modelCompra.addColumn("Volumen");
 		modelCompra.addColumn("Folio");
 		tableCompra.getRowSorter().setSortKeys(sortKeysc);
 		modelExec.addColumn("Hora");
 		modelExec.addColumn("Precio");
 		modelExec.addColumn("Volumen");
 		modelExec2.addColumn("Hora");
 		modelExec2.addColumn("Precio");
 		modelExec2.addColumn("Volumen");
 		modelOrdenes.addColumn("Tipo");
 		modelOrdenes.addColumn("Precio");
 		modelOrdenes.addColumn("Volumen");
 		modelOrdenes.addColumn("Hora");
 		modelOrdenes.addColumn("Folio");
 		modelOrdenes.addColumn("Folio Ant.");
 		modelOrdenes.addColumn("Casa de Bolsa");
 		Object[] columns = {"Tipo","Precio","Volumen","Hora","Folio","Folio Ant.","Casa de Bolsa","Orden"};
 		Vector<Object> columnNames = convertToVector(columns);
 		Vector<Object> data =  new Vector<Object>(6000);
 		for (int i = 0; i < ordenes.size(); i++) {
 			Vector<Object> row = convertToVector(ordenes.get(i).toRow());
 			data.add(row);
 		}
 		modelOrdenes.setDataVector(data, columnNames);
 		DateRenderer2 render = new DateRenderer2();
 		tableOrdenes.getColumnModel().getColumn(3).setCellRenderer(render);
 		tableExec.getColumnModel().getColumn(0).setCellRenderer(render);
 		tableExec2.getColumnModel().getColumn(0).setCellRenderer(render);
 		JButton buttonNext = new JButton("Siguiente");
 		buttonNext.setBounds(30, 716, 100, 30);
 		frame.add(buttonNext);
 		buttonNext.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				//Execute when button is pressed
 				if(modelOrdenes.getRowCount() > 0)
 					action(day, modelCompra, modelVenta, modelOrdenes, modelExec, modelExec2, tableCompra, tableVenta, tableExec, tableExec2, bw, bw2, bwc, bwv, bwd);
 			}
 		});
 
 		JButton buttonDeleteCompra = new JButton("Cancelar Compra");
 		buttonDeleteCompra.setBounds(150, 716, 200, 30);
 		frame.add(buttonDeleteCompra);
 		buttonDeleteCompra.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e)
 			{
 				//Execute when button is pressed
 				int d = tableCompra.getSelectedRow();
 				try {
 					modelCompra.removeRow(tableCompra.convertRowIndexToModel(d));
 				}
 				catch(Exception ex)
 				{
 					System.out.println("Cannot delete row: " + ex);			
 				}
 			}
 		});
 
 		JButton buttonDeleteVenta = new JButton("Cancelar Venta");
 		buttonDeleteVenta.setBounds(370, 716, 200, 30);
 		frame.add(buttonDeleteVenta);
 		buttonDeleteVenta.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e)
 			{
 				//Execute when button is pressed
 				int d = tableVenta.getSelectedRow();
 				try {
 					modelVenta.removeRow(tableVenta.convertRowIndexToModel(d));
 				}
 				catch(Exception ex)
 				{
 					System.out.println("Cannot delete row: " + ex);			
 				}
 			}
 		});
 		JButton buttonPlay = new JButton("Play");
 		buttonPlay.setBounds(600, 716, 100, 30);
 		frame.add(buttonPlay);
 		buttonPlay.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e)
 			{
 				//Execute when button is pressed
 				int i = 0;
 				while(tableOrdenes.getRowCount() > 0 && i < 100) {
 					//while(tableOrdenes.getRowCount() > 0) {
 					Date cal = (Date) modelOrdenes.getValueAt(0, 3);
 					action(day, modelCompra, modelVenta, modelOrdenes, modelExec, modelExec2, tableCompra, tableVenta, tableExec, tableExec2, bw, bw2, bwc, bwv, bwd);
 					//modelCompra.getValueAt(0, 1)
 
 					i++;
 				}
 				if(tableOrdenes.getRowCount() <= 0)
 					try {
 						bw.close();
 						bw2.close();
 						bwc.close();
 						bwv.close();
 						bwd.close();
 					} catch (IOException e1) {
 						// TODO Auto-generated catch block
 						e1.printStackTrace();
 					}
 			}
 		});
 	}
 	protected static <T> Vector<T> convertToVector(Object[] data) 
 	{
 		if (data == null)
 			return null;
 		Vector<T> vector = new Vector<T>(data.length);
 		for (int i = 0; i < data.length; i++) 
 			vector.add((T) data[i]);
 		return vector;          
 	}
 	public static <T> Vector<T> convertToVector(Object[][] data) 
 	{
 		if (data == null)
 			return null;
 		Vector<T> vector = new Vector<T>(data.length);
 		for (int i = 0; i < data.length; i++)
 			vector.add((T) convertToVector(data[i]));
 		return vector;
 	}
 	public static Vector<NumFolio> createList(Vector v) {
 		Vector<NumFolio> res = new Vector<NumFolio>(v.size());
 		for (int i = 0; i < v.size(); i++) {
 			Vector test = (Vector) v.get(i);
 			res.add(new NumFolio(i,(Integer) test.get(3)));
 		}
 		return res;
 	}
 	public static void action(Calendar day, DefaultTableModel modelCompra, DefaultTableModel modelVenta, DefaultTableModel modelOrdenes, DefaultTableModel modelExec, DefaultTableModel modelExec2, JTable tableCompra, JTable tableVenta, JTable tableExec, JTable tableExec2, BufferedWriter bw, BufferedWriter bw2, BufferedWriter bwc, BufferedWriter bwv, BufferedWriter bwd) {
 		Date time;
 		TimeZone tz = TimeZone.getTimeZone("America/Mexico_City");
 		Calendar cal = Calendar.getInstance(tz);
 		time = (Date) modelOrdenes.getValueAt(0, 3);
 		cal.setTime(time);
 		switch((Orden.Mov) modelOrdenes.getValueAt(0, 0)) {
 		case CO:
 			if(cal.get(Calendar.DAY_OF_MONTH) == day.get(Calendar.DAY_OF_MONTH) && cal.get(Calendar.MONTH) == day.get(Calendar.MONTH)) {
 				if(cal.get(Calendar.HOUR_OF_DAY) > 8 || (cal.get(Calendar.HOUR_OF_DAY) >= 8 && cal.get(Calendar.MINUTE) >= 30)) {
 					if(tableVenta.getRowCount()>0) {
 						BigDecimal askpx = (BigDecimal) tableVenta.getValueAt(modelVenta.getRowCount()-1, 1);
 						BigDecimal price = (BigDecimal) modelOrdenes.getValueAt(0, 1);
 						System.out.println("Compra");
 						System.out.println(askpx.subtract(price));
 						try {
 							Long ts = time.getTime();
 							bwd.write("Compra," + ts.toString() + "," + askpx.subtract(price).toString());
 							bwd.newLine();
 						} catch (IOException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 					}
 				}
 			}
 			Object[] tempc = new Object[4];
 			tempc[0] = new String("COMPRA");
 			tempc[1] = modelOrdenes.getValueAt(0, 1);
 			tempc[2] = modelOrdenes.getValueAt(0, 2);
 			tempc[3] = modelOrdenes.getValueAt(0, 4);
 			modelCompra.addRow(tempc);
 			if(cal.get(Calendar.DAY_OF_MONTH) == day.get(Calendar.DAY_OF_MONTH) && cal.get(Calendar.MONTH) == day.get(Calendar.MONTH))
 				if(cal.get(Calendar.HOUR_OF_DAY) > 8 || (cal.get(Calendar.HOUR_OF_DAY) >= 8 && cal.get(Calendar.MINUTE) >= 30))
 					check(modelCompra, modelVenta, modelOrdenes, modelExec, modelExec2, tableCompra, tableVenta, tableExec, tableExec2, bw2);
 			break;
 		case VE:
 			if(cal.get(Calendar.DAY_OF_MONTH) == day.get(Calendar.DAY_OF_MONTH) && cal.get(Calendar.MONTH) == day.get(Calendar.MONTH)) {
 				if(cal.get(Calendar.HOUR_OF_DAY) > 8 || (cal.get(Calendar.HOUR_OF_DAY) >= 8 && cal.get(Calendar.MINUTE) >= 30)) {
 					if(tableCompra.getRowCount()>0) {
 						BigDecimal bidpx = (BigDecimal) tableCompra.getValueAt(0, 1);
 						BigDecimal price = (BigDecimal) modelOrdenes.getValueAt(0, 1);
 						System.out.println("Venta");
 						System.out.println(price.subtract(bidpx));
 						try {
 							Long ts = time.getTime();
 							bwd.write("Venta," + ts.toString() + "," + price.subtract(bidpx).toString());
 							bwd.newLine();
 						} catch (IOException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 					}	
 				}
 			}
 			Object[] tempv = new Object[4];
 			tempv[0] = new String("VENTA");
 			tempv[1] = modelOrdenes.getValueAt(0, 1);
 			tempv[2] = modelOrdenes.getValueAt(0, 2);
 			tempv[3] = modelOrdenes.getValueAt(0, 4);
 			modelVenta.addRow(tempv);
 			if(cal.get(Calendar.DAY_OF_MONTH) == day.get(Calendar.DAY_OF_MONTH) && cal.get(Calendar.MONTH) == day.get(Calendar.MONTH))
 				if(cal.get(Calendar.HOUR_OF_DAY) > 8 || (cal.get(Calendar.HOUR_OF_DAY) >= 8 && cal.get(Calendar.MINUTE) >= 30))
 					check(modelCompra, modelVenta, modelOrdenes, modelExec, modelExec2, tableCompra, tableVenta, tableExec, tableExec2, bw2);
 			break;
 		case AH:
 			Object[] tempe = new Object[3];
 			tempe[0] = modelOrdenes.getValueAt(0, 3);
 			tempe[1] = modelOrdenes.getValueAt(0, 1);
 			tempe[2] = modelOrdenes.getValueAt(0, 2);
 			modelExec.addRow(tempe);
 			try {
 				bw.write(tempe[0].toString() + ',' + tempe[1].toString() + ',' + tempe[2].toString());
 				bw.newLine();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 			tableExec.scrollRectToVisible(new Rectangle(tableExec.getCellRect(tableExec.getRowCount()-1, 0, true)));
 			tableExec2.scrollRectToVisible(new Rectangle(tableExec2.getCellRect(tableExec2.getRowCount()-1, 0, true)));
 			break;
 		case CA:
 			Vector datac = modelCompra.getDataVector();
 			Vector datav = modelVenta.getDataVector();
 			Vector<NumFolio> listc = createList(datac);
 			Collections.sort(listc);
 			Vector<NumFolio> listv = createList(datav);
 			Collections.sort(listv);
 			Comparator<NumFolio> c = new Comparator<NumFolio>() {
 				public int compare(NumFolio n1, NumFolio n2) {
 					return n1.compareTo(n2);
 				}
 			};
 			if (listc.size() > 0) {
 				int index = Collections.binarySearch(listc, new NumFolio(0, (Integer) modelOrdenes.getValueAt(0, 4)), c);
 				if (index >= 0) {
 					modelCompra.removeRow(listc.get(index).num);
 				}
 			}
 			if (listv.size() > 0) {
 				int index = Collections.binarySearch(listv, new NumFolio(0, (Integer) modelOrdenes.getValueAt(0, 4)), c);
 				if (index >= 0)
 					modelVenta.removeRow(listv.get(index).num);
 			}
 			break;
 		case MO:
 			Vector datacm = modelCompra.getDataVector();
 			Vector datavm = modelVenta.getDataVector();
 			Vector<NumFolio> listcm = createList(datacm);
 			Collections.sort(listcm);
 			Vector<NumFolio> listvm = createList(datavm);
 			Collections.sort(listvm);
 			Comparator<NumFolio> cm = new Comparator<NumFolio>() {
 				public int compare(NumFolio n1, NumFolio n2) {
 					return n1.compareTo(n2);
 				}
 			};
 			if (listcm.size() > 0) { //Modificacion compra
 				int index = Collections.binarySearch(listcm, new NumFolio(0, (Integer) modelOrdenes.getValueAt(0, 5)), cm);
 				if (index >= 0) {
 					if(cal.get(Calendar.DAY_OF_MONTH) == day.get(Calendar.DAY_OF_MONTH) && cal.get(Calendar.MONTH) == day.get(Calendar.MONTH)) {
 						if(cal.get(Calendar.HOUR_OF_DAY) > 8 || (cal.get(Calendar.HOUR_OF_DAY) >= 8 && cal.get(Calendar.MINUTE) >= 30)) {
 							if(tableVenta.getRowCount()>0) {
 								BigDecimal askpx = (BigDecimal) tableVenta.getValueAt(modelVenta.getRowCount()-1, 1);
 								BigDecimal cancpx = (BigDecimal) modelCompra.getValueAt(listcm.get(index).num, 1);
 								BigDecimal price = (BigDecimal) modelOrdenes.getValueAt(0, 1);
 								System.out.println("CompraMod");
 								System.out.println(askpx.subtract(cancpx));
 								try {
 									Long ts = time.getTime();
 									bwd.write("CancCompraMod,");
 									bwd.write(ts.toString());
 									bwd.write(",");
 									bwd.write(askpx.subtract(cancpx).toString());
 									bwd.newLine();
 									bwd.write("CompraMod,");
 									bwd.write(ts.toString());
 									bwd.write(",");
 									bwd.write(askpx.subtract(price).toString());
 									bwd.newLine();
 								} catch (IOException e) {
 									// TODO Auto-generated catch block
 									e.printStackTrace();
 								}
 							}
 						}
 					}
 					modelCompra.setValueAt(modelOrdenes.getValueAt(0, 2), listcm.get(index).num, 2);
 					modelCompra.setValueAt(modelOrdenes.getValueAt(0, 4), listcm.get(index).num, 3);
 					modelCompra.setValueAt(modelOrdenes.getValueAt(0, 1), listcm.get(index).num, 1);
 					modelCompra.fireTableDataChanged();
 				}
 			}
 			if (listvm.size() > 0) { //Modificacion venta
 				int index = Collections.binarySearch(listvm, new NumFolio(0, (Integer) modelOrdenes.getValueAt(0, 5)), cm);
 				if (index >= 0) {
 					if(cal.get(Calendar.DAY_OF_MONTH) == day.get(Calendar.DAY_OF_MONTH) && cal.get(Calendar.MONTH) == day.get(Calendar.MONTH)) {
 						if(cal.get(Calendar.HOUR_OF_DAY) > 8 || (cal.get(Calendar.HOUR_OF_DAY) >= 8 && cal.get(Calendar.MINUTE) >= 30)) {
 							if(tableCompra.getRowCount()>0) {
 								BigDecimal bidpx = (BigDecimal) tableCompra.getValueAt(0, 1);
 								BigDecimal cancpx = (BigDecimal) modelVenta.getValueAt(listvm.get(index).num, 1);
 								BigDecimal price = (BigDecimal) modelOrdenes.getValueAt(0, 1);
 								System.out.println("VentaMod");
 								System.out.println(cancpx.subtract(bidpx));
 								try {
 									Long ts = time.getTime();
 									bwd.write("CancVentaMod,");
 									bwd.write(ts.toString());
 									bwd.write(",");
 									bwd.write(cancpx.subtract(bidpx).toString());
 									bwd.newLine();
 									bwd.write("VentaMod,");
 									bwd.write(ts.toString());
 									bwd.write(",");
 									bwd.write(price.subtract(bidpx).toString());
 									bwd.newLine();
 								} catch (IOException e) {
 									// TODO Auto-generated catch block
 									e.printStackTrace();
 								}
 							}
 						}
 					}
 					modelVenta.setValueAt(modelOrdenes.getValueAt(0, 2), listvm.get(index).num, 2);
 					modelVenta.setValueAt(modelOrdenes.getValueAt(0, 4), listvm.get(index).num, 3);
 					modelVenta.setValueAt(modelOrdenes.getValueAt(0, 1), listvm.get(index).num, 1);
 					modelVenta.fireTableDataChanged();
 				}
 			}
 			if(cal.get(Calendar.DAY_OF_MONTH) == day.get(Calendar.DAY_OF_MONTH) && cal.get(Calendar.MONTH) == day.get(Calendar.MONTH))
 				if(cal.get(Calendar.HOUR_OF_DAY) > 8 || (cal.get(Calendar.HOUR_OF_DAY) >= 8 && cal.get(Calendar.MINUTE) >= 30))
 					check(modelCompra, modelVenta, modelOrdenes, modelExec, modelExec2, tableCompra, tableVenta, tableExec, tableExec2, bw2);
 			break;
 		default:
 			break;
 		}
 		if(cal.get(Calendar.DAY_OF_MONTH) == day.get(Calendar.DAY_OF_MONTH) && cal.get(Calendar.MONTH) == day.get(Calendar.MONTH))
 			if(cal.get(Calendar.HOUR_OF_DAY) > 8 || (cal.get(Calendar.HOUR_OF_DAY) >= 8 && cal.get(Calendar.MINUTE) >= 30)){
 
 				try {
 					Long timestamp = cal.getTimeInMillis();
 					bwc.write(timestamp.toString() + ',');
 					for (int j = 0; j < modelCompra.getRowCount(); j++) {
 						bwc.write(tableCompra.getValueAt(j, 1).toString() + ',' + tableCompra.getValueAt(j, 2).toString() + ',');
 					}
 					bwc.newLine();
 					bwv.write(timestamp.toString() + ',');
 					for (int j = modelVenta.getRowCount()-1; j >= 0; j--) {
 						bwv.write(tableVenta.getValueAt(j, 1).toString() + ',' + tableVenta.getValueAt(j, 2).toString() + ',');
 					}
 					bwv.newLine();
 				} catch (IOException e1) {
 					// TODO Auto-generated catch block
 					e1.printStackTrace();
 				}
 			}
 		modelOrdenes.removeRow(0);
 		tableVenta.scrollRectToVisible(new Rectangle(tableVenta.getCellRect(tableVenta.getRowCount()-1, 0, true)));
 	}
 	public static void check(DefaultTableModel modelCompra, DefaultTableModel modelVenta, DefaultTableModel modelOrdenes, DefaultTableModel modelExec, DefaultTableModel modelExec2, JTable tableCompra, JTable tableVenta, JTable tableExec, JTable tableExec2, BufferedWriter bw2) {
 		if (modelVenta.getRowCount() > 0 && modelCompra.getRowCount() > 0) {
 			BigDecimal precioMejorVenta = (BigDecimal) modelVenta.getValueAt(tableVenta.convertRowIndexToModel(tableVenta.getRowCount()-1), 1);
 			BigDecimal precioMejorCompra = (BigDecimal) modelCompra.getValueAt(tableCompra.convertRowIndexToModel(0), 1);
 			Integer volumenMejorVenta = (Integer) modelVenta.getValueAt(tableVenta.convertRowIndexToModel(tableVenta.getRowCount()-1), 2);
 			Integer volumenMejorCompra = (Integer) modelCompra.getValueAt(tableCompra.convertRowIndexToModel(0), 2);
 			while(precioMejorVenta.compareTo(precioMejorCompra) <= 0 && modelVenta.getRowCount() > 0 && modelCompra.getRowCount() > 0) {
 				int volumen;
 				if(volumenMejorCompra.compareTo(volumenMejorVenta) < 0) {
 					modelVenta.setValueAt(volumenMejorVenta - volumenMejorCompra, tableVenta.convertRowIndexToModel(tableVenta.getRowCount()-1), 2);
 					modelCompra.removeRow(tableCompra.convertRowIndexToModel(0));
 					volumen = volumenMejorCompra;
 				}
 				else if (volumenMejorCompra.compareTo(volumenMejorVenta) > 0) {
 					modelVenta.removeRow(tableVenta.convertRowIndexToModel(tableVenta.getRowCount()-1));
 					modelCompra.setValueAt(volumenMejorCompra - volumenMejorVenta, tableCompra.convertRowIndexToModel(0), 2);
 					volumen = volumenMejorVenta;
 				}
 				else {
 					modelVenta.removeRow(tableVenta.convertRowIndexToModel(tableVenta.getRowCount()-1));
 					modelCompra.removeRow(tableCompra.convertRowIndexToModel(0));
 					volumen = volumenMejorCompra;
 				}
 				BigDecimal precio;
 				if(precioMejorCompra.compareTo(precioMejorVenta) <= 0)
 					precio = precioMejorCompra;
 				else
 					precio = precioMejorVenta;
 				Object[] tempe = new Object[3];
 				tempe[0] = modelOrdenes.getValueAt(0, 3);
 				tempe[1] = precio;
 				tempe[2] = volumen;
 				modelExec2.addRow(tempe);
 				try {
 					bw2.write(tempe[0].toString() + ',' + tempe[1].toString() + ',' + tempe[2].toString());
 					bw2.newLine();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				tableExec.scrollRectToVisible(new Rectangle(tableExec.getCellRect(tableExec.getRowCount()-1, 0, true)));
 				tableExec2.scrollRectToVisible(new Rectangle(tableExec2.getCellRect(tableExec2.getRowCount()-1, 0, true)));
 				if (modelVenta.getRowCount() > 0 && modelCompra.getRowCount() > 0) {
 					precioMejorVenta = (BigDecimal) modelVenta.getValueAt(tableVenta.convertRowIndexToModel(tableVenta.getRowCount()-1), 1);
 					precioMejorCompra = (BigDecimal) modelCompra.getValueAt(tableCompra.convertRowIndexToModel(0), 1);
 					volumenMejorVenta = (Integer) modelVenta.getValueAt(tableVenta.convertRowIndexToModel(tableVenta.getRowCount()-1), 2);
 					volumenMejorCompra = (Integer) modelCompra.getValueAt(tableCompra.convertRowIndexToModel(0), 2);
 				}
 			}
 		}
 	}
 }
