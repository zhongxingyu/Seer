 package com.xpandit.fusionplugin;
 
 import java.util.ArrayList;
 import java.util.Map;
 
 import org.pentaho.commons.connection.IPentahoResultSet;
 import org.pentaho.commons.connection.IPentahoMetaData;
 
 import com.fusioncharts.Series;
 import com.fusioncharts.Category;
 import com.fusioncharts.ChartFactoryChart;
 import com.fusioncharts.ChartType;
 import com.fusioncharts.ColorRange;
 
 import com.xpandit.fusionplugin.exception.InvalidDataResultSetException;
 import com.xpandit.fusionplugin.exception.InvalidParameterException;
 
 /**
  * 
  * This class is responsible for manage all the widgets logic
  * 
  * @author dduque
  *
  */
 public class FusionComponentChart extends  FusionComponent {
 
 
 
 	/***************************************************************************
 	 * Constructor for a FusionComponent object.
 	 *
 	 * @param  graphId
 	 *         The graph name.
 	 *         
 	 * @param  graphType
 	 *         The graph type ie:(pie graph, column chart)        
 	 *         
 	 * @param  length
 	 *         The length of the categories.
 	 *         
 	 *         
 	 ***************************************************************************/
 	public FusionComponentChart(ChartType graphType, int length) {
 		super("chart", graphType, length);
 	}
 
 	/**
 	 * 
 	 * @param isFreeVersion renders the chart to be used with free version or not
 	 * @return
 	 * @throws Exception
 	 */
 	public String execute() throws Exception {
 
 		ChartFactoryChart chart	= new ChartFactoryChart(isFreeVersion());   
 		//attach graph to chart factory
 		chart.insertGraph(graph);
 		return chart.buildDOMFusionChart(graph.getGraphId()); 
 	}
 	
 	/**
      * 
      * Set data to chart
      * 
      * @param resultSets Pentaho ResultSet with multi result sets from a query multi queries
      * @throws InvalidDataResultSetException when reult set is invalid 
      */
 	@Override
     public void setData(Map<String, ArrayList<IPentahoResultSet>> resultSets) throws InvalidDataResultSetException {
 	    if(resultSets==null)
             throw new InvalidDataResultSetException(InvalidDataResultSetException.ERROR_001 , "Result Set is null");
 	    
 	    //if is the bubble charts
         if(graph.getGraphType()==ChartType.BUBBLE)
         {
             this.data = resultSets.get("results").get(0); 
 
             // get Data Set Metadata
             IPentahoMetaData metadata = this.data.getMetaData();
             //verify meta data
             int metadataSize= metadata.getColumnCount();
             if(metadataSize<3)
                 throw new InvalidDataResultSetException(InvalidDataResultSetException.ERROR_001 , "less than 3");
  
             //get link values
             //the chart link template
             String chartLink=graph.getChartProperties().get("chartLink");
             //seriesParam to replace in template link
             String seriesParam=graph.getChartProperties().get("seriesParam");
             //categoriesParam to replace in template link
             String categoriesParam=graph.getChartProperties().get("categoriesParam");
 
 
             double maxXvalue=0; 
             double minXvalue=0;
             
             double maxYvalue=0; 
             double minYvalue=0;
             
             //get data 
             int rowCount=this.data.getRowCount(); 
             for (int i = 0; i < rowCount; i++) {
                 try
                 {   
                     Series series = graph.createSeries(this.data.getValueAt(i,0).toString());
                     setSeriesProperties(series,i);
                     
                     Double xValue=Double.parseDouble(this.data.getValueAt(i,1).toString());
                     series.setXValue(0,xValue);
                     
                     //calculate the max and min values to XAxis
                     maxXvalue=xValue>maxXvalue?xValue:maxXvalue;
                     minXvalue=xValue<minXvalue?xValue:minXvalue;
                     
                     Double yValue=Double.parseDouble(this.data.getValueAt(i,2).toString());
                     series.setYValue(0, yValue);                
                     
                     //calculate the max and min values to YAxis
                     maxYvalue=yValue>maxYvalue?yValue:maxYvalue;
                     minYvalue=yValue<minYvalue?yValue:minYvalue;
                     
                     if(this.data.getColumnCount()>3)
                         series.setZValue(0, Double.parseDouble((this.data.getValueAt(i,3).toString())));
 
                     //build a chart link
                     if(chartLink!=null)
                     {
 
                         String serieChartLink=chartLink;
 
                         //set seriesValue
                         if(seriesParam!=null)
                             serieChartLink=chartLink.replace("{"+seriesParam+"}", series.getValue(i).toString());
                         //set categoriesValue
                         if(categoriesParam!=null)
                             serieChartLink=serieChartLink.replace("{"+categoriesParam+"}", graph.getCategory(i).toString());
                         series.setEvent(i, serieChartLink);
                     }
 
                 }
                 catch(Exception e)
                 {
                     log.error("Problem in result set. Null values found at index:"+i, e);
                 }
             }
             
             //set max YAxis with more 10% of current yMax Value
             int maxYvalueAux=(int)(maxYvalue*1.30);
             //fusion charts tweak 
             //the automatic scale at y axis don'w work correctly when the value is like-> 100999999
             // this transform the value to 100999000
             if(maxYvalueAux>1000)
             {
                 maxYvalueAux/=1000;
                 maxYvalueAux*=1000;
             }
             graph.setChartProperties("yAxisMaxValue",String.valueOf(maxYvalueAux));
              
  
             // set the categories for bubble chart
             int index=0;
             int width=graph.getWidth();
             
             //each vline should have 90px between each vline 
             int numDivLinesXAxis=width/90;
             
             // the max value of x Axis is 10% more than real max value
             int maxValueX=(int) (maxXvalue*1.10);
             
             //calculates the number of vertical lines 
             int stepsValue=maxValueX/numDivLinesXAxis;
             
             //build the categories
             for(int i=(int) minXvalue;i<=maxValueX;i+=stepsValue)
             {
                 Category cat=new Category();
                 //calculates the K,M for xAxis
                 //the fusion charts don't do this
                 int indexDivision=0;
                 int auxI=i;
                while(true)
                {
                    if(auxI<1000)
                        break;
                    auxI/=1000;
                    ++indexDivision;
                }
                 // set then correct value at the label
                 cat.setLable(auxI+numberDivision[indexDivision]);
                 //set the X value
                 cat.setxValue((double) i);
                 //set the category
                 graph.setCategory(index,cat);
                 ++index; 
             }
             
         }
         else
         {
             this.data = resultSets.get("results").get(0);
 
             // get Data Set Metadata
             IPentahoMetaData metadata = this.data.getMetaData();
             //verify meta data
             int metadataSize= metadata.getColumnCount();
             if(metadataSize<2)
                 throw new InvalidDataResultSetException(InvalidDataResultSetException.ERROR_001 , "less than 2");
 
             //get link values
             //the chart link template
             String chartLink=graph.getChartProperties().get("chartLink");
             //seriesParam to replace in template link
             String seriesParam=graph.getChartProperties().get("seriesParam");
             //categoriesParam to replace in template link
             String categoriesParam=graph.getChartProperties().get("categoriesParam");
 
 
 
             for(int seriesCount=1;seriesCount<metadataSize;++seriesCount)
             {
                 // get measure column name to set series title
                 String seriesTitle=metadata.getColumnHeaders()[0][seriesCount].toString();
 
                 //TODO:Improve Code
                 //this code remove the MDX notation and return the member name
                 //[measures].[day] returns -> day
                 String[] seriesTitleArr=seriesTitle.split("/.")[0].split("\\]\\.");
                 seriesTitle=seriesTitleArr[seriesTitleArr.length-1].replace("]","").replace("[","");
 
                 Series series = graph.createSeries(seriesTitle);
                 setSeriesProperties(series,seriesCount-1);
                 //get data 
                 int rowCount=this.data.getRowCount();
                 for (int i = 0; i < rowCount; i++) {
                     try
                     { 
                         //set category label
                         Category categ=new Category();
                         categ.setLable(this.data.getValueAt(i,0).toString());
 
                         //set category in chart
                         graph.setCategory(i,categ);
                         series.setValue(i,Double.parseDouble((this.data.getValueAt(i,seriesCount).toString())));    
 
                         //build a chart link
                         if(chartLink!=null)
                         {
 
                             String serieChartLink=chartLink;
 
                             //set seriesValue
                             if(seriesParam!=null) 
                                 serieChartLink=chartLink.replace("{"+seriesParam+"}", series.getValue(i).toString());
                             //set categoriesValue
                             if(categoriesParam!=null)
                                 serieChartLink=serieChartLink.replace("{"+categoriesParam+"}", graph.getCategory(i).getLable());
                             series.setEvent(i, serieChartLink);
                         }
 
                     }
                     catch(Exception e)
                     {
                         log.error("Problem in result set. Null values found at index:"+i, e);
                     }
                 }
 
                 if(graph.getGraphType()==ChartType.BUBBLE)
                 {
                     return;
                 }
             }
         }
     }
 }
