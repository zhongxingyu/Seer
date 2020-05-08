 package edu.nrao.dss.client;
 
 import java.util.Date;
 
 import com.extjs.gxt.ui.client.data.BaseListLoadResult;
 import com.extjs.gxt.ui.client.data.BaseModelData;
 import com.extjs.gxt.ui.client.data.BasePagingLoadResult;
 import com.extjs.gxt.ui.client.widget.ContentPanel;
 import com.extjs.gxt.ui.client.widget.layout.FitData;
 import com.extjs.gxt.ui.client.widget.layout.FitLayout;
 import com.google.gwt.http.client.RequestBuilder;
 
 public class Reservations extends ContentPanel {
 
 	public ReservationsGrid res;
 	
 	public Reservations (Date start, int days) {
 		super();
 		initLayout(start, days);
 	}
 	
 	private void initLayout(Date start, int days){
		setHeading("Reservations");
 		setBorders(true);
 		
 		// put the reservation grid inside
 		FitLayout fl = new FitLayout();
 		setLayout(fl);
 	    res = new ReservationsGrid(start, days);
 	    add(res, new FitData(10));
 	}
 	
 	public void update(String start, String days) {
 		// get the period explorer to load these
 		String url = "/reservations?start=" + start + "&days=" + days;
 		RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);
 		DynamicHttpProxy<BaseListLoadResult<BaseModelData>> proxy = res.getProxy();
 		proxy.setBuilder(builder);
 		res.load();
 	}
 }
