 package pl.edu.zut.mad.tools.noise.meter;
 
 import org.achartengine.ChartFactory;
 import org.achartengine.GraphicalView;
 import org.achartengine.model.TimeSeries;
 import org.achartengine.model.XYMultipleSeriesDataset;
 import org.achartengine.renderer.XYMultipleSeriesRenderer;
 import org.achartengine.renderer.XYSeriesRenderer;
 
 import pl.edu.zut.mad.tools.R;
 import android.content.Context;
 import android.graphics.Color;
 
 public class NoiseLinearGraph {
 
 	private GraphicalView view;
 
 	private TimeSeries dataset = new TimeSeries("");
 	private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
 	private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
 
 	public NoiseLinearGraph(Context ctx) {
 
 		dataset.setTitle(ctx.getString(R.string.noise_bar_title));
 		mDataset.addSeries(dataset);
 
 		mRenderer.setXTitle("");
		mRenderer.setYTitle("dB");
 		mRenderer.setAxesColor(Color.WHITE);
 		mRenderer.setLabelsColor(Color.WHITE);
 		mRenderer.setYAxisMax(120.0f);
 		mRenderer.setYAxisMin(0.0f);
		mRenderer.setApplyBackgroundColor(true);
		mRenderer.setMarginsColor(Color.argb(0x00, 0x01, 0x01, 0x01));
		mRenderer.setBackgroundColor(Color.TRANSPARENT);
 
 		// Customize bar 1
 		XYSeriesRenderer renderer = new XYSeriesRenderer();
 		renderer.setDisplayChartValues(false);
 		renderer.setColor(Color.WHITE);
 		renderer.setLineWidth(3.0f);

 		mRenderer.addSeriesRenderer(renderer);
 
 	}
 
 	public GraphicalView getView(Context ctx) {
 
 		view = ChartFactory.getLineChartView(ctx, mDataset, mRenderer);
 		return view;
 
 	}
 
 	public void addNewPoints(GraphPoint p) {
 		dataset.add(p.getX(), p.getY());
 	}
 
 }
