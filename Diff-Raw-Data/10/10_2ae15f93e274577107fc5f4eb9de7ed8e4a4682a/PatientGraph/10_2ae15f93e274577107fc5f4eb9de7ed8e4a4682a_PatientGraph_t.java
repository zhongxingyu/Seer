 package au.edu.uwa.csp.respreport;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 
 import org.achartengine.ChartFactory;
 import org.achartengine.GraphicalView;
 import org.achartengine.chart.PointStyle;
 import org.achartengine.model.TimeSeries;
 import org.achartengine.model.XYMultipleSeriesDataset;
 import org.achartengine.renderer.XYMultipleSeriesRenderer;
 import org.achartengine.renderer.XYSeriesRenderer;
 
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.Color;
 import android.graphics.Paint.Align;
 import android.util.Log;
 
 public class PatientGraph {
 
 	Activity parentActivity;
 
 	public PatientGraph(Activity act) {
 		// TODO Auto-generated constructor stub
 		this.parentActivity = act;
 	}
 
 	/*
 	 * Get the view for patient = patientId
 	 * Return GraphicalView containing the view of the graph.
 	 */
 	public GraphicalView getView(Context context, int patientId) {
 
 		String dateFormat = "dd/MM/yy hh:mm";
 
 		//Getting the respiratory data
 		XYMultipleSeriesDataset dataset = getDataset(context, patientId,
 				dateFormat);
 		//Getting the customization setting of the graph.
 		XYMultipleSeriesRenderer mRenderer = getRenderer();
 
 
 		return ChartFactory.getTimeChartView(context, dataset, mRenderer,
 				"dd/MM/yy hh:mm");
 
 	}
 
 
 	private void checkParameter(XYMultipleSeriesDataset dataset,
 			XYMultipleSeriesRenderer renderer) {
 		if (dataset == null
 				|| renderer == null
 				|| dataset.getSeriesCount() != renderer
 						.getSeriesRendererCount()) {
 			throw new IllegalArgumentException(
 					"Dataset and renderer should be not null and should have the same number of series");
 		}
 	}
 
 	/*
 	 * Get the dataset that is empty. Set the default value to 0
 	 * Return the set of data.
 	 */
 	private XYMultipleSeriesDataset getDatasetEmpty(Context context,
 			int patientId, String dateFormat) {
 		TimeSeries series = new TimeSeries("Rate Graph");
 
 		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
 		//set the default value to 0
 		series.add(new Date(), 0);
 
 		dataset.addSeries(series);
 
 		return dataset;
 
 	}
 
 	/*
 	 * Get the dataset with the specified patientId.
 	 */
 	private XYMultipleSeriesDataset getDataset(Context context, int patientId,
 			String dateFormat) {
 		
 		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
 		TimeSeries series = new TimeSeries("Rate Graph");
 		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
 		
 		//Get data from web services
 		List<Patient> lPatient = FetchParseXML.FetchPatientFromWebService(
 				parentActivity, AppFunctions.getUsername(),
 				AppFunctions.getPassword());
 		
 		Log.d("fetchPatients", "Pgraph");
 
 		Patient patient = null;
 		for (Patient p : lPatient) {
 			if (p.getReturnedID() == patientId) {
 				patient = p;
 				break;
 			}
 		}
 
		if (patient != null){
 		Log.d("Pgraph","found patient "+ patient.getUserName());
 		//Get respiratory data from web services
 		List<Respiratory> resList = FetchParseXML.FetchRespiratoryFromWebService(
 				parentActivity, AppFunctions.getUsername(),
 				AppFunctions.getPassword(), patient.getReturnedID());
 		
 		Log.d("fetchResp", "Pgraph");
 		
 		//if there is no dataset, call default empty dataset.
 		if (resList.size() == 0) {
 			Log.d("No dataset", "No Rate recorded before");
 			dataset = getDatasetEmpty(context, patientId, dateFormat);
 		} else {
 			// Iterate Respiratory List
 			for (int i = 0; i < resList.size(); i++) {
 				Date newDate = new Date();
 
 				String sDate = resList.get(i).getDateMeasured();
 				int iRate = resList.get(i).getRespiratoryRate();
 
 				try {
 					newDate = sdf.parse(sDate);
 				} catch (ParseException e) {
 					System.out.println("Parsing Date Error!");
 					e.printStackTrace();
 				}
 				//add date and value to the series
 				series.add(newDate, iRate);
 			}
 
 			dataset.addSeries(series);
 
 		}
		}
 		return dataset;
 	}
 
 	/*
 	 * Line customization function
 	 */
 	private XYMultipleSeriesRenderer getRenderer() {
 		XYSeriesRenderer renderer = new XYSeriesRenderer();
 
 		renderer.setColor(Color.rgb(30, 144, 255));
 		renderer.setPointStyle(PointStyle.SQUARE);
 		renderer.setFillPoints(true);
 		renderer.setLineWidth((float) 1.5);
 
 		XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
 		mRenderer.addSeriesRenderer(renderer);
 
 		// Customize mRenderer
 		setChartSettings(mRenderer);
 
 		return mRenderer;
 	}
 
 	/*
 	 * Graph customization function
 	 */
 	private void setChartSettings(XYMultipleSeriesRenderer mRenderer) {
 		mRenderer.setBackgroundColor(Color.BLACK);
 		mRenderer.setApplyBackgroundColor(true);
 		mRenderer.setChartTitle("Respiration Rate");
 		mRenderer.setChartTitleTextSize((float) 30);
 		mRenderer.setXTitle("Day");
 		mRenderer.setYTitle("Rate");
 		mRenderer.setLabelsColor(Color.rgb(30, 144, 255));
 		mRenderer.setLabelsTextSize((float) 14);
 		mRenderer.setLegendTextSize((float) 20);
 		mRenderer.setLegendHeight(50);
 		mRenderer.setMargins(new int[] { 100, 50, 50, 50 });
 		mRenderer.setXLabelsAlign(Align.CENTER);
 		mRenderer.setYLabelsAlign(Align.CENTER);
 		mRenderer.setPanEnabled(true, false);
 		mRenderer.setAxisTitleTextSize((float) 18);
 
 		mRenderer.setShowGridX(true);
 		mRenderer.setShowGridY(false);
 		mRenderer.setGridColor(Color.DKGRAY);
 
 	}
 }
