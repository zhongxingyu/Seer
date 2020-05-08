 package ch.cern.atlas.apvs.client;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import ch.cern.atlas.apvs.domain.Dosimeter;
 
 import com.google.gwt.cell.client.NumberCell;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.i18n.client.NumberFormat;
 import com.google.gwt.safehtml.shared.SafeHtmlUtils;
 import com.google.gwt.user.cellview.client.CellTable;
 import com.google.gwt.user.cellview.client.Column;
 import com.google.gwt.user.cellview.client.ColumnSortEvent;
 import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.user.client.ui.SimplePanel;
 import com.google.gwt.view.client.ListDataProvider;
 
 public class DosimeterView extends SimplePanel {
 
 	@SuppressWarnings("unused")
 	private static List<Dosimeter> DOSIMETERS = Arrays.asList(new Dosimeter(22,
 			3.0, 0.2), new Dosimeter(19, 2.5, 0.9),
 			new Dosimeter(98, 1.9, 0.1), new Dosimeter(67, 6.5, 0.2));
 	
 	private ListDataProvider<Dosimeter> dataProvider = new ListDataProvider<Dosimeter>();	
 	private CellTable<Dosimeter> table = new CellTable<Dosimeter>();
 	private ListHandler<Dosimeter> columnSortHandler;
 	private DosimeterServiceAsync dosimeterService = GWT.create(DosimeterService.class);
 	private Map<Integer, Dosimeter> dosimeters = new HashMap<Integer, Dosimeter>();
 
 	public DosimeterView() {
 		add(table);
 
 		Column<Dosimeter, Number> serialNo = new Column<Dosimeter, Number>(
 				new NumberCell(NumberFormat.getFormat("0"))) {
 			@Override
 			public Number getValue(Dosimeter object) {
 				return object.getSerialNo();
 			}
 		};
 		serialNo.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
 		serialNo.setSortable(true);
 		table.addColumn(serialNo, "Serial No");
 
 		Column<Dosimeter, Number> dose = new Column<Dosimeter, Number>(
				new NumberCell(NumberFormat.getFormat("0.0"))) {
 			@Override
 			public Number getValue(Dosimeter object) {
 				return object.getDose();
 			}
 		};
 		dose.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
 		dose.setSortable(true);
 		table.addColumn(dose, SafeHtmlUtils.fromSafeConstant("Dose [&micro;Sv]"));
 
 		Column<Dosimeter, Number> rate = new Column<Dosimeter, Number>(
				new NumberCell(NumberFormat.getFormat("0.00"))) {
 			@Override
 			public Number getValue(Dosimeter object) {
 				return object.getRate();
 			}
 		};
 		rate.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
 		rate.setSortable(true);
 		table.addColumn(rate, SafeHtmlUtils.fromSafeConstant("Rate [&micro;Sv/h]"));
 
 		dataProvider.addDataDisplay(table);
 		dataProvider.setList(new ArrayList<Dosimeter>());
 		
 		columnSortHandler = new ListHandler<Dosimeter>(
 				dataProvider.getList());
 		columnSortHandler.setComparator(serialNo, new Comparator<Dosimeter>() {
 			public int compare(Dosimeter o1, Dosimeter o2) {
 				return o1 != null ? o1.compareTo(o2) : -1;
 			}
 		});
 		columnSortHandler.setComparator(dose, new Comparator<Dosimeter>() {
 			public int compare(Dosimeter o1, Dosimeter o2) {
 				if (o1 == o2) {
 					return 0;
 				}
 
 				if (o1 != null) {
 					return (o2 != null) ? o1.getDose() < o2.getDose() ? -1 : o1
 							.getDose() == o2.getDose() ? 0 : 1 : 1;
 				}
 				return -1;
 			}
 		});
 		columnSortHandler.setComparator(rate, new Comparator<Dosimeter>() {
 			public int compare(Dosimeter o1, Dosimeter o2) {
 				if (o1 == o2) {
 					return 0;
 				}
 
 				if (o1 != null) {
 					return (o2 != null) ? o1.getRate() < o2.getRate() ? -1 : o1
 							.getRate() == o2.getRate() ? 0 : 1 : 1;
 				}
 				return -1;
 			}
 		});
 		table.addColumnSortHandler(columnSortHandler);
 		table.getColumnSortList().push(serialNo);
 		
 		getDosimeterMap();		
 	}
 	
 	private void getDosimeterMap() {
 		dosimeterService.getDosimeterMap(dosimeters.hashCode(), new AsyncCallback<Map<Integer,Dosimeter>>() {
 			
 			@Override
 			public void onSuccess(Map<Integer, Dosimeter> result) {
 				if (result == null) {
 					System.err.println("FIXME onSuccess null in dosimeterView");
 					return;
 				}
 				dosimeters  = result;
 				
 				List<Dosimeter> list = dataProvider.getList();
 				list.clear();
 				
 				for (Iterator<Dosimeter> i = result.values().iterator(); i.hasNext(); ) {
 					list.add(i.next());
 				}
 
 				// Resort the table
 				ColumnSortEvent.fire(table, table.getColumnSortList());			
 				table.redraw();
 				
 				getDosimeterMap();
 			}
 			
 			@Override
 			public void onFailure(Throwable caught) {
 				GWT.log("Could not retrieve dosimeter map");
 				
 				getDosimeterMap();
 			}
 		});
 
 	}
 	
 }
