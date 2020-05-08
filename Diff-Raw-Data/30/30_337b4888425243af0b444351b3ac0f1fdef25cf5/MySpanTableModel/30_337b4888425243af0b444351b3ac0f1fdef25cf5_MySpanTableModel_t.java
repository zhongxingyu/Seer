 package gui;
 
 import gui.spantable.CellAttribute;
 import gui.spantable.CellSpan;
 import gui.spantable.DefaultCellAttribute;
 import gui.spantable.Span;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 
 import javax.swing.JLabel;
 import javax.swing.JProgressBar;
 import javax.swing.table.AbstractTableModel;
 
 import data.Pair;
 import data.Service;
 import data.Service.Status;
 import data.UserEntry;
 
 public class MySpanTableModel extends AbstractTableModel {
 
 	private static final long	serialVersionUID	= 1L;
 
 	private List<List<Object>>	data;
 	private List<String>		columns;
 	private List<Service>		services;
 	private ArrayList<Span>		spans;
 	protected CellAttribute		cellAtt;
 
 	private final Lock			mutex				= new ReentrantLock(true);
 
 	public MySpanTableModel(ArrayList<Service> services, ArrayList<String> columns) {
 		this.services = (List<Service>) Collections.synchronizedList((ArrayList<Service>) services
 				.clone());
 
 		this.columns = (List<String>) Collections.synchronizedList(new ArrayList<String>(columns));
 
 		data = Collections.synchronizedList(new ArrayList<List<Object>>());
 		cellAtt = new DefaultCellAttribute(0, getColumnCount());
 
 		System.out.println("From constructor ...");
 		for (Service service : services) {
 			addService(service, false);
 		}
 		System.out.println("Exit from constructor...");
 	}
 
 	public void addSpan(Span span) {
 		mutex.lock();
 		if (spans == null) {
 			spans = new ArrayList<Span>();
 		}
 
 		if (!spans.contains(span)) {
 			((CellSpan) cellAtt).combine(span);
 			spans.add(span);
 			fireTableStructureChanged();
 		}
 		mutex.unlock();
 	}
 
 	public void removeSpan(Span obj) {
 		mutex.lock();
 		Boolean found = false;
 		for (Span span : spans) {
 			if (span.equals(obj)) {
 				found = true;
 				((CellSpan) cellAtt).split(span);
 				break;
 			}
 		}
 
 		if (found) {
 			spans.remove(obj);
 			fireTableStructureChanged();
 		}
 		mutex.unlock();
 	}
 
 	public void clearSpans() {
 		mutex.lock();
 		for (Span span : spans) {
 			((CellSpan) cellAtt).split(span);
 		}
 
 		spans.clear();
 		fireTableStructureChanged();
 		mutex.unlock();
 	}
 
 	public void removeService(Integer index) {
 		System.out.println(Thread.currentThread().getName() + " is trying to acquaire lock ...");
 		mutex.lock();
 		System.out.println(Thread.currentThread().getName() + " acquaired lock ...");
 
 		/* Clear all the previous data */
 		data.clear();
 		cellAtt = new DefaultCellAttribute(0, getColumnCount());
 		clearSpans();
 
 		services.remove(index.intValue());
 
 		for (Service serv : services) {
 			addService(serv, false);
 		}
 
 		mutex.unlock();
 		System.out.println(Thread.currentThread().getName() + " released lock ...");
 	}
 
 	public void removeService(Service service) {
 		mutex.lock();
 		removeService(services.indexOf(service));
 		mutex.unlock();
 	}
 
 	private void addService(Service service, Boolean add) {
 		mutex.lock();
 		ArrayList<UserEntry> users;
 		ArrayList<ArrayList<Object>> serviceData;
 
 		users = service.getUsers();
 		System.out.println(service);
 		serviceData = service.getAsTable();
 
 		data.addAll(serviceData);
 		cellAtt.addRows(serviceData.size());
 
 		if (add) {
 			services.add(service);
 		}
 
 		if (service.getStatus() == Status.ACTIVE && users != null) {
 			/* Service name span */
 			addSpan(new Span(data.size() - users.size(), 0, users.size(), 1));
 			/* Status span */
 			addSpan(new Span(data.size() - users.size(), 1, users.size(), 1));
 		} else {
 			addSpan(new Span(data.size() - 1, 2, 1, 4));
 		}
 
 		fireTableDataChanged();
 		fireTableStructureChanged();
 
 		mutex.unlock();
 	}
 
 	public void addService(Service service) {
 		mutex.lock();
 		System.out.println("Before add : " + services.size());
 		addService(service, true);
 		System.out.println("After add : " + services.size());
 		mutex.unlock();
 	}
 
 	public void changeService(Service service) {
 		mutex.lock();
 
		if (service.getUsers() != null) {
			Collections.sort(service.getUsers());
		}

 		removeService(service);
 		addService(service);
 
 		fireTableDataChanged();
 		fireTableStructureChanged();
 
 		mutex.unlock();
 	}
 
 	public Pair<Service, Integer> getServiceFromRow(Integer row) {
 
 		mutex.lock();
 
 		int counter = 0;
 		int userCounter;
 
 		if (row > getRowCount() || row < 0) {
 			mutex.unlock();
 			return null;
 		}
 
 		for (Service service : services) {
 			if (row >= counter && row < counter + getNeededRows(service)) {
 				userCounter = 0;
 
 				/* Get selected user */
 				if (service.getUsers() != null) {
 					for (UserEntry user : service.getUsers()) {
 						if (row == userCounter + counter) {
 							mutex.unlock();
 							return new Pair<Service, Integer>(service, userCounter);
 						}
 						userCounter++;
 					}
 				}
 
 				mutex.unlock();
 				return new Pair<Service, Integer>(service, -1);
 			}
 
 			counter += getNeededRows(service);
 		}
 
 		mutex.unlock();
 		return null;
 	}
 
 	private Integer getNeededRows(Service service) {
 		switch (service.getStatus()) {
 		case INACTIVE:
 			return 1;
 		case ACTIVE:
 			if (service.getUsers() == null) {
 				return 1;
 			} else {
 				return service.getUsers().size();
 			}
 		case TRANSFER_IN_PROGRESS:
 		case TRANSFER_STARTED:
 		case TRANSFER_COMPLETE:
 		case TRANSFER_FAILED:
 			return 1;
 		default:
 			System.err.println("[MySpanTableModel, geNeededRows] Unexpected Status :|");
 			break;
 		}
 
 		return 0;
 	}
 
 	@Override
 	public int getColumnCount() {
 
 		mutex.lock();
 		int count = columns.size();
 		mutex.unlock();
 
 		return count;
 	}
 
 	@Override
 	public int getRowCount() {
 
 		mutex.lock();
 		int count = data.size();
 		mutex.unlock();
 
 		return count;
 	}
 
 	@Override
 	public Object getValueAt(int rowIndex, int columnIndex) {
 
 		mutex.lock();
 		Object obj = data.get(rowIndex).get(columnIndex);
 		mutex.unlock();
 
 		return obj;
 	}
 
 	@Override
 	public String getColumnName(int column) {
 
 		mutex.lock();
 		String columnName = columns.get(column);
 		mutex.unlock();
 		return columnName;
 	}
 
 	public CellAttribute getCellAttribute() {
 		mutex.lock();
 		CellAttribute attr = cellAtt;
 		mutex.unlock();
 		return attr;
 	}
 
 	public void setCellAttribute(CellAttribute newCellAtt) {
 		mutex.lock();
 		int numColumns = getColumnCount();
 		int numRows = getRowCount();
 
 		if ((newCellAtt.getSize().width != numColumns) || (newCellAtt.getSize().height != numRows)) {
 			newCellAtt.setSize(new Dimension(numRows, numColumns));
 		}
 
 		cellAtt = newCellAtt;
 		fireTableDataChanged();
 		mutex.unlock();
 	}
 
 	@Override
 	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
 		mutex.lock();
 		data.get(rowIndex).set(columnIndex, aValue);
 		mutex.unlock();
 	}
 
 	@Override
 	public boolean isCellEditable(int rowIndex, int columnIndex) {
 		return false;
 	}
 }
