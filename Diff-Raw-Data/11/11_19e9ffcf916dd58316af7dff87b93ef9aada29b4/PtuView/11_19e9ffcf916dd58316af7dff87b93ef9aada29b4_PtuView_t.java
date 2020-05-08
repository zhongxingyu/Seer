 package ch.cern.atlas.apvs.client;
 
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.SortedMap;
 import java.util.TreeMap;
 
 import ch.cern.atlas.apvs.domain.Measurement;
 import ch.cern.atlas.apvs.domain.Ptu;
 
 import com.google.gwt.cell.client.Cell.Context;
 import com.google.gwt.cell.client.TextCell;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.i18n.client.NumberFormat;
 import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
 import com.google.gwt.safehtml.shared.SafeHtmlUtils;
 import com.google.gwt.user.cellview.client.CellTable;
 import com.google.gwt.user.cellview.client.ColumnSortEvent;
 import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
 import com.google.gwt.user.cellview.client.TextColumn;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.user.client.ui.SimplePanel;
 import com.google.gwt.view.client.ListDataProvider;
 
 public class PtuView extends SimplePanel {
 
 	private ListDataProvider<String> dataProvider = new ListDataProvider<String>();
 	private CellTable<String> table = new CellTable<String>();
 	private ListHandler<String> columnSortHandler;
 	private PtuServiceAsync ptuService = GWT.create(PtuService.class);
 
 	Measurement<Double> last = new Measurement<Double>();
 	SortedMap<Integer, Ptu> ptus = new TreeMap<Integer, Ptu>();
 	Map<String, String> units = new HashMap<String, String>();
 	Map<Integer, TextColumn<String>> columns = new HashMap<Integer, TextColumn<String>>();
 
 	public PtuView() {
 		add(table);
 
 		// name column
 		TextColumn<String> name = new TextColumn<String>() {
 			@Override
 			public String getValue(String object) {
 				return object;
 			}
 
 			@Override
 			public void render(Context context, String object,
 					SafeHtmlBuilder sb) {
 				((TextCell) getCell()).render(context,
 						SafeHtmlUtils.fromSafeConstant(getValue(object)), sb);
 			}
 		};
 		name.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
 		name.setSortable(true);
 		table.addColumn(name, "Name");
 
 		// unit column
 		TextColumn<String> unit = new TextColumn<String>() {
 			@Override
 			public String getValue(String object) {
 				return units.get(object);
 			}
 
 			@Override
 			public void render(Context context, String object,
 					SafeHtmlBuilder sb) {
 				((TextCell) getCell()).render(context,
 						SafeHtmlUtils.fromSafeConstant(getValue(object)), sb);
 			}
 		};
 		unit.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
 		table.addColumn(unit, "Unit");
 
 		dataProvider.addDataDisplay(table);
 		dataProvider.setList(new ArrayList<String>());
 
 		columnSortHandler = new ListHandler<String>(dataProvider.getList());
 		columnSortHandler.setComparator(name, new Comparator<String>() {
 			public int compare(String o1, String o2) {
 				return o1 != null ? o1.compareTo(o2) : -1;
 			}
 		});
 		table.addColumnSortHandler(columnSortHandler);
 		table.getColumnSortList().push(name);
 		
 		ptuService.getCurrentMeasurements(new AsyncCallback<List<Measurement<Double>>>() {
 			
 			@Override
 			public void onSuccess(List<Measurement<Double>> result) {
 				for (Iterator<Measurement<Double>> i = result.iterator(); i.hasNext(); ) {
 					handleMeasurement(i.next());
 				}
 
				System.err.println(result.size());
				
 				redraw();
 				getLastMeasurement();
 			}
 			
 			@Override
 			public void onFailure(Throwable caught) {
 				GWT.log("Could not retrieve current Measurements" + caught);
 				getLastMeasurement();
 			}
 		});
 
 	}
 
 	private void getLastMeasurement() {
 		ptuService.getLastMeasurement(last.hashCode(),
 				new AsyncCallback<Measurement<Double>>() {
 
 					@Override
 					public void onSuccess(Measurement<Double> result) {
 						if (result == null) {
 							System.err
 									.println("FIXME onSuccess null in ptuView");
 							return;
 						}
 						
 						handleMeasurement(result);
 
 						last = result;
 
 						redraw();
 						getLastMeasurement();
 					}
 
 
 					@Override
 					public void onFailure(Throwable caught) {
 						GWT.log("Could not retrieve last Measurement" + caught);
 						getLastMeasurement();
 					}
 
 				});
 	}
 
 	private void handleMeasurement(Measurement<Double> result) {
 		Integer ptuId = result.getPtuId();
 		Ptu ptu = ptus.get(ptuId);
 		if (ptu == null) {
 			ptu = new Ptu(ptuId);
 			ptus.put(ptuId, ptu);
 			insertColumn(ptuId);
 		}
 
 		String name = result.getName();
 		if (ptu.getMeasurement(name) == null) {
 			units.put(name, result.getUnit());
 			ptu.add(result);
 		} else {
 			ptu.setMeasurement(name, result);
 		}
 
 		if (!dataProvider.getList().contains(name)) {
 			dataProvider.getList().add(name);
 		}
 	}
 	
 	private void insertColumn(final Integer ptuId) {
 		int columnIndex = new ArrayList<Integer>(ptus.keySet()).indexOf(ptuId) + 2;
 		TextColumn<String> column = new TextColumn<String>() {
 			@Override
 			public String getValue(String object) {
 				Measurement<Double> m = ptus.get(ptuId).getMeasurement(object);
 				if (m == null) {
 					return "";
 				}
 				return NumberFormat.getFormat("0.00").format(m.getValue());
 			}
 
 			@Override
 			public void render(Context context, String object,
 					SafeHtmlBuilder sb) {
 				Measurement<Double> m = ptus.get(ptuId).getMeasurement(object);
 				String s = getValue(object);
 				if ((m != null) && m.equals(last)) {
 					s = "<b>" + s + "</b>";
 				}
 				((TextCell) getCell()).render(context,
 						SafeHtmlUtils.fromSafeConstant(s), sb);
 			}
 
 		};
 		column.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
 		table.insertColumn(columnIndex, column, ptuId.toString());
 	}
 
 	private void redraw() {
 		ColumnSortEvent.fire(table, table.getColumnSortList());
 		table.redraw();
 	}
 }
