 package org.jbs.happysad;
 
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Iterator;
 
 import org.achartengine.ChartFactory;
 import org.achartengine.renderer.DefaultRenderer;
 
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Color;
 
 /**
  * Budget demo pie chart.
  */
 public class ChartYear extends AbstractChart {
   /**
    * Returns the chart name.
    * 
    * @return the chart name
    */
   public String getName() {
     return "Happy Pie-Chart";
   }
 
   /**
    * Returns the chart description.
    * 
    * @return the chart description
    */
   public String getDesc() {
     return "This chart shows the percentages of your happiness in a pie chart format.";
   }
 
   /**
    * Executes the chart demo.
    * 
    * @param context the context
    * @return the built intent
    */
   public Intent execute(Context context) {
 	  
 	HappyData datahelper = new HappyData(context);
	ArrayList<HappyBottle> plottables = datahelper.getMyHistory();
 	
     double[] values = percentages(plottables);
     	//new double[] { 12, 14, 11, 10, 19 };
     int[] colors = new int[] { Color.YELLOW, Color.CYAN };
     
     DefaultRenderer renderer = buildCategoryRenderer(colors);
     renderer.setZoomButtonsVisible(true);
     renderer.setZoomEnabled(true);
     //renderer.setChartTitleTextSize(20);
     
     renderer.setChartTitle("PIE CHART!");
     
     renderer.setChartTitleTextSize(50);
 	renderer.setLabelsTextSize(20);
 	renderer.setLegendTextSize(40);
 	renderer.setMargins(new int[] {20, 30});
     
     return ChartFactory.getPieChartIntent(context, buildCategoryDataset("Happy Pie", values),
         renderer, "Chart");
   }
   
   public double [] percentages(ArrayList<HappyBottle> plottables){
 		
 	  	Iterator<HappyBottle> itr = plottables.iterator(); 
 		double happy = 0;
 		double sad = 0;
 		double [] values = new double[2];
 	
 		while(itr.hasNext()) {     
 		   	
 			HappyBottle element = itr.next();
 			
 			int x = new Timestamp (element.getTime()).getYear() + 1900;
 			
 			int year = Calendar.getInstance().get(Calendar.YEAR);
 		     
 		   	if (x == year){
 		   		
 		   		if (element.getEmo() == 1){
 			    	happy += 1; 
 			     } else {
 			    	sad += 1; 
 			     }		
 		   	}	
 		} 
 		
 		double happyprctg = (happy * 100) / (happy + sad);
 		double sadprctg = 100 - happyprctg;
 		
 		int happytransf = (int) (happyprctg * 100);
 		int sadtrans = (int) (sadprctg * 100);
 		
 		happyprctg = (double) happytransf / 100;
 		sadprctg = (double) sadtrans / 100;
 		
 		values[0] = happyprctg;
 		values[1] = sadprctg;
 		
 		return values;
 	}
   
   /*
   public ArrayList<Integer> getYears(ArrayList<HappyBottle> plottables){
 		Iterator<HappyBottle> itr = plottables.iterator(); 
 		ArrayList<Integer> year = new ArrayList<Integer>();
 		while(itr.hasNext()) {     
 		   	HappyBottle element = itr.next();
 		   	
 		   	String date = new Timestamp (element.getTime()).toString();
 		   	
 		   	String a = date.substring(0, 4);
 		   	
 		   	int x = Integer.parseInt(a);
 		   	
 		   	int w = new Timestamp (element.getTime()).getYear();
 		   	
 		   	year.add(w);
 		     
 		} 
 		
 		return year;
 	}
 	*/
   
 }	
