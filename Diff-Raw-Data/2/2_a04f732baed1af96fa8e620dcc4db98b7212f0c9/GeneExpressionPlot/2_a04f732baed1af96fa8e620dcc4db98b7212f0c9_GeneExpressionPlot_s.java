 package gov.nih.nci.rembrandt.web.graphing.data;
 
 import gov.nih.nci.caintegrator.dto.critieria.InstitutionCriteria;
 import gov.nih.nci.caintegrator.enumeration.GeneExpressionDataSetType;
 import gov.nih.nci.caintegrator.ui.graphing.chart.plot.BoxAndWhiskerCoinPlotRenderer;
 import gov.nih.nci.caintegrator.ui.graphing.chart.plot.FaroutOutlierBoxAndWhiskerCalculator;
 import gov.nih.nci.rembrandt.util.RembrandtConstants;
 import gov.nih.nci.rembrandt.web.helper.InsitutionAccessHelper;
 import gov.nih.nci.rembrandt.web.legend.LegendCreator;
 
 import java.awt.Color;
 import java.io.PrintWriter;
 import java.math.BigDecimal;
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 import java.util.HashMap;
 
 import javax.servlet.http.HttpSession;
 
 import org.jfree.chart.ChartFactory;
 import org.jfree.chart.ChartRenderingInfo;
 import org.jfree.chart.ChartUtilities;
 import org.jfree.chart.JFreeChart;
 import org.jfree.chart.LegendItemCollection;
 import org.jfree.chart.axis.CategoryAxis;
 import org.jfree.chart.axis.NumberAxis;
 import org.jfree.chart.entity.StandardEntityCollection;
 import org.jfree.chart.imagemap.StandardURLTagFragmentGenerator;
 import org.jfree.chart.labels.CategoryToolTipGenerator;
 import org.jfree.chart.plot.CategoryPlot;
 import org.jfree.chart.plot.PlotOrientation;
 import org.jfree.chart.renderer.category.BarRenderer;
 import org.jfree.chart.renderer.category.BoxAndWhiskerRenderer;
 import org.jfree.chart.renderer.category.StatisticalBarRenderer;
 import org.jfree.chart.servlet.ServletUtilities;
 import org.jfree.data.category.CategoryDataset;
 import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;
 import org.jfree.data.statistics.DefaultStatisticalCategoryDataset;
 
 
 
 /**
 * caIntegrator License
 * 
 * Copyright 2001-2005 Science Applications International Corporation ("SAIC"). 
 * The software subject to this notice and license includes both human readable source code form and machine readable, 
 * binary, object code form ("the caIntegrator Software"). The caIntegrator Software was developed in conjunction with 
 * the National Cancer Institute ("NCI") by NCI employees and employees of SAIC. 
 * To the extent government employees are authors, any rights in such works shall be subject to Title 17 of the United States
 * Code, section 105. 
 * This caIntegrator Software License (the "License") is between NCI and You. "You (or "Your") shall mean a person or an 
 * entity, and all other entities that control, are controlled by, or are under common control with the entity. "Control" 
 * for purposes of this definition means (i) the direct or indirect power to cause the direction or management of such entity,
 *  whether by contract or otherwise, or (ii) ownership of fifty percent (50%) or more of the outstanding shares, or (iii) 
 * beneficial ownership of such entity. 
 * This License is granted provided that You agree to the conditions described below. NCI grants You a non-exclusive, 
 * worldwide, perpetual, fully-paid-up, no-charge, irrevocable, transferable and royalty-free right and license in its rights 
 * in the caIntegrator Software to (i) use, install, access, operate, execute, copy, modify, translate, market, publicly 
 * display, publicly perform, and prepare derivative works of the caIntegrator Software; (ii) distribute and have distributed 
 * to and by third parties the caIntegrator Software and any modifications and derivative works thereof; 
 * and (iii) sublicense the foregoing rights set out in (i) and (ii) to third parties, including the right to license such 
 * rights to further third parties. For sake of clarity, and not by way of limitation, NCI shall have no right of accounting
 * or right of payment from You or Your sublicensees for the rights granted under this License. This License is granted at no
 * charge to You. 
 * 1. Your redistributions of the source code for the Software must retain the above copyright notice, this list of conditions
 *    and the disclaimer and limitation of liability of Article 6, below. Your redistributions in object code form must reproduce 
 *    the above copyright notice, this list of conditions and the disclaimer of Article 6 in the documentation and/or other materials
 *    provided with the distribution, if any. 
 * 2. Your end-user documentation included with the redistribution, if any, must include the following acknowledgment: "This 
 *    product includes software developed by SAIC and the National Cancer Institute." If You do not include such end-user 
 *    documentation, You shall include this acknowledgment in the Software itself, wherever such third-party acknowledgments 
 *    normally appear.
 * 3. You may not use the names "The National Cancer Institute", "NCI" "Science Applications International Corporation" and 
 *    "SAIC" to endorse or promote products derived from this Software. This License does not authorize You to use any 
 *    trademarks, service marks, trade names, logos or product names of either NCI or SAIC, except as required to comply with
 *    the terms of this License. 
 * 4. For sake of clarity, and not by way of limitation, You may incorporate this Software into Your proprietary programs and 
 *    into any third party proprietary programs. However, if You incorporate the Software into third party proprietary 
 *    programs, You agree that You are solely responsible for obtaining any permission from such third parties required to 
 *    incorporate the Software into such third party proprietary programs and for informing Your sublicensees, including 
 *    without limitation Your end-users, of their obligation to secure any required permissions from such third parties 
 *    before incorporating the Software into such third party proprietary software programs. In the event that You fail 
 *    to obtain such permissions, You agree to indemnify NCI for any claims against NCI by such third parties, except to 
 *    the extent prohibited by law, resulting from Your failure to obtain such permissions. 
 * 5. For sake of clarity, and not by way of limitation, You may add Your own copyright statement to Your modifications and 
 *    to the derivative works, and You may provide additional or different license terms and conditions in Your sublicenses 
 *    of modifications of the Software, or any derivative works of the Software as a whole, provided Your use, reproduction, 
 *    and distribution of the Work otherwise complies with the conditions stated in this License.
 * 6. THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED WARRANTIES, (INCLUDING, BUT NOT LIMITED TO, 
 *    THE IMPLIED WARRANTIES OF MERCHANTABILITY, NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE) ARE DISCLAIMED. 
 *    IN NO EVENT SHALL THE NATIONAL CANCER INSTITUTE, SAIC, OR THEIR AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT, 
 *    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE 
 *    GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF 
 *    LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
 *    OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 */
 
 public class GeneExpressionPlot {
 	
 	public enum PlotSize { SMALL, MEDIUM, LARGE }
 	
 	public static HashMap generateBarChart(String gene, HttpSession session,
 			PrintWriter pw, GeneExpressionDataSetType geType) {
 		return GeneExpressionPlot.generateBarChart(gene, null, session, pw, geType);
 	}
 	
 	public static HashMap generateBarChart(String gene, String reporter, HttpSession session,
 			PrintWriter pw, GeneExpressionDataSetType geType) {
 		String log2Filename = null;
 		String rawFilename = null;
 		String medianFilename = null;
 		String bwFilename = "";
 		String legendHtml = null;
 		HashMap charts = new HashMap();
 		PlotSize ps = PlotSize.MEDIUM;
 		
 		final String geneName = gene;
 		final String alg = geType.equals(GeneExpressionDataSetType.GeneExpressionDataSet) ? RembrandtConstants.REPORTER_SELECTION_AFFY : RembrandtConstants.REPORTER_SELECTION_UNI;
 		try {
 			InstitutionCriteria institutionCriteria = InsitutionAccessHelper.getInsititutionCriteria(session);
 
 			final GenePlotDataSet gpds = new GenePlotDataSet(gene, reporter, institutionCriteria, geType, session.getId());
 			//final GenePlotDataSet gpds = new GenePlotDataSet(gene, institutionCriteria,GeneExpressionDataSetType.GeneExpressionDataSet );
 
 			//LOG2 Dataset
 			DefaultStatisticalCategoryDataset dataset = (DefaultStatisticalCategoryDataset) gpds.getLog2Dataset();
 			
 			//RAW Dataset
 			CategoryDataset meanDataset = (CategoryDataset) gpds.getRawDataset();
 			
 			//B&W dataset
 			DefaultBoxAndWhiskerCategoryDataset bwdataset = (DefaultBoxAndWhiskerCategoryDataset) gpds.getBwdataset();
 						
 			//Median dataset
 			CategoryDataset medianDataset = (CategoryDataset) gpds.getMedianDataset();
 			
 			charts.put("diseaseSampleCountMap",gpds.getDiseaseSampleCountMap());
 			
 			//IMAGE Size Control
 			if(bwdataset!=null && bwdataset.getRowCount()>5)	{
 				ps = PlotSize.LARGE;
 			}
 			else	{
 				ps = PlotSize.MEDIUM;
 			}
 			//SMALL/MEDIUM == 650 x 400
 			//LARGE == 1000 x 400
 			//put as external Props?
 			int imgW = 650;
 			if(ps == PlotSize.LARGE)	{
 				imgW = new BigDecimal(bwdataset.getRowCount()).multiply(new BigDecimal(75)).intValue() > 1000 ? new BigDecimal(bwdataset.getRowCount()).multiply(new BigDecimal(75)).intValue() : 1000;
 			}
 			
 			JFreeChart bwChart = null;
 
 				//B&W plot
 				CategoryAxis xAxis = new CategoryAxis("Disease Type");
 		        NumberAxis yAxis = new NumberAxis("Log2 Expression Intensity");
 		        yAxis.setAutoRangeIncludesZero(true);
 		        BoxAndWhiskerCoinPlotRenderer bwRenderer = null;
 		       // BoxAndWhiskerRenderer bwRenderer = new BoxAndWhiskerRenderer();
 		        if(reporter != null)	{
 		        	//single reporter, show the coins
 		        	bwRenderer = new BoxAndWhiskerCoinPlotRenderer(gpds.getCoinHash());
 		        	bwRenderer.setDisplayCoinCloud(true);
 		        	bwRenderer.setDisplayMean(false);
 		        	bwRenderer.setDisplayAllOutliers(true);
 		        	bwRenderer.setToolTipGenerator(new CategoryToolTipGenerator() {
 		        		public String generateToolTip(CategoryDataset dataset,int series, int item) {
 							String tt="";
 							NumberFormat formatter = new DecimalFormat(".####");
 							String key = "";
 						    //String s = formatter.format(-1234.567);  // -001235
 						    if(dataset instanceof DefaultBoxAndWhiskerCategoryDataset){
 							    DefaultBoxAndWhiskerCategoryDataset ds = (DefaultBoxAndWhiskerCategoryDataset)dataset;
 							    try	{
 									String med = formatter.format(ds.getMedianValue(series, item));
 									tt += "Median: " + med + "<br/>";
 									tt += "Mean: " + formatter.format(ds.getMeanValue(series, item))+"<br/>";
 									tt += "Q1: " + formatter.format(ds.getQ1Value(series, item))+"<br/>";
 									tt += "Q3: " + formatter.format(ds.getQ3Value(series, item))+"<br/>";
 									tt += "Max: " + formatter.format(
 											FaroutOutlierBoxAndWhiskerCalculator.getMaxFaroutOutlier(ds,series, item))+"<br/>";
 									tt += "Min: " + formatter.format(
 											FaroutOutlierBoxAndWhiskerCalculator.getMinFaroutOutlier(ds,series, item))+"<br/>";
 									//tt += "<br/><br/>Please click on the box and whisker to view a plot for this reporter.<br/>";
 									//tt += "X: " + ds.getValue(series, item).toString()+"<br/>";
 									//tt += "<br/><a href=\\\'#\\\' id=\\\'"+ds.getRowKeys().get(series)+"\\\' onclick=\\\'alert(this.id);return false;\\\'>"+ds.getRowKeys().get(series)+" plot</a><br/><br/>";
 									key = ds.getRowKeys().get(series).toString();
 							    }
 							    catch(Exception e) {}
 						    }
 						    
 							return  tt;						}
 		
 					});
 		        }
 		        else	{
 		        	//groups, dont show coins
 		        	bwRenderer = new BoxAndWhiskerCoinPlotRenderer();
 		        	bwRenderer.setDisplayAllOutliers(true);
 		        	bwRenderer.setToolTipGenerator(new CategoryToolTipGenerator() {
 		        		public String generateToolTip(CategoryDataset dataset,int series, int item) {
 							String tt="";
 							NumberFormat formatter = new DecimalFormat(".####");
 							String key = "";
 						    //String s = formatter.format(-1234.567);  // -001235
 						    if(dataset instanceof DefaultBoxAndWhiskerCategoryDataset){
 							    DefaultBoxAndWhiskerCategoryDataset ds = (DefaultBoxAndWhiskerCategoryDataset)dataset;
 							    try	{
 									String med = formatter.format(ds.getMedianValue(series, item));
 									tt += "Median: " + med + "<br/>";
 									tt += "Mean: " + formatter.format(ds.getMeanValue(series, item))+"<br/>";
 									tt += "Q1: " + formatter.format(ds.getQ1Value(series, item))+"<br/>";
 									tt += "Q3: " + formatter.format(ds.getQ3Value(series, item))+"<br/>";
 									tt += "Max: " + formatter.format(
 											FaroutOutlierBoxAndWhiskerCalculator.getMaxFaroutOutlier(ds,series, item))+"<br/>";
 									tt += "Min: " + formatter.format(
 											FaroutOutlierBoxAndWhiskerCalculator.getMinFaroutOutlier(ds,series, item))+"<br/>";
 									tt += "<br/><br/>Please click on the box and whisker to view a plot for this reporter.<br/>";
 									//tt += "X: " + ds.getValue(series, item).toString()+"<br/>";
 									//tt += "<br/><a href=\\\'#\\\' id=\\\'"+ds.getRowKeys().get(series)+"\\\' onclick=\\\'alert(this.id);return false;\\\'>"+ds.getRowKeys().get(series)+" plot</a><br/><br/>";
 									key = ds.getRowKeys().get(series).toString();
 							    }
 							    catch(Exception e) {}
 						    }
 							return "onclick=\"popCoin('"+geneName+"','"+key+"', '"+alg+"');\" | " + tt;
 
 								
 						}
 		
 					});
 		        }
 		        bwRenderer.setFillBox(false);
 		        
 		        
 		        
 		        CategoryPlot bwPlot = new CategoryPlot(bwdataset, xAxis, yAxis, bwRenderer);
 		        bwChart = new JFreeChart(bwPlot);
 		 
 			    //    JFreeChart bwChart = new JFreeChart(
 			    //    	null /*"Gene Expression Plot (" + gene.toUpperCase() + ")"*/,
 			    //        new Font("SansSerif", Font.BOLD, 14),
 			    //        bwPlot,
 			    //        true
 			    //    );
 		        
 		        bwChart.setBackgroundPaint(java.awt.Color.white);
 		        //bwChart.getTitle().setHorizontalAlignment(TextTitle.DEFAULT_HORIZONTAL_ALIGNMENT.LEFT);
 		   
 		        bwChart.removeLegend();
 				//END BW plot
 
 			
 			// create the chart...for LOG2 dataset
 			JFreeChart log2Chart = ChartFactory.createBarChart(
 					null /*"Gene Expression Plot (" + gene.toUpperCase() + ")"*/, // chart
 																			// title
 					"Groups", // domain axis label
 					"Log2 Expression Intensity", // range axis label
 					dataset, // data
 					PlotOrientation.VERTICAL, // orientation
 					true, // include legend
 					true, // tooltips?
 					false // URLs?
 					);
 
 			//create the chart .... for RAW dataset
 			JFreeChart meanChart = ChartFactory.createBarChart(
 					null /*"Gene Expression Plot (" + gene.toUpperCase() + ")"*/, // chart
 																			// title
 					"Groups", // domain axis label
 					"Mean Expression Intensity", // range axis label
 					meanDataset, // data
 					PlotOrientation.VERTICAL, // orientation
 					true, // include legend
 					true, // tooltips?
 					false // URLs?
 					);
 			
 //			create the chart .... for Median dataset
 			JFreeChart medianChart = ChartFactory.createBarChart(
 					null /*"Gene Expression Plot (" + gene.toUpperCase() + ")"*/, // chart
 																			// title
 					"Groups", // domain axis label
					"Medain Expression Intensity", // range axis label
 					medianDataset, // data
 					PlotOrientation.VERTICAL, // orientation
 					true, // include legend
 					true, // tooltips?
 					false // URLs?
 					);
 
 			log2Chart.setBackgroundPaint(java.awt.Color.white);
 			// lets start some customization to retro fit w/jcharts lookand feel
 			CategoryPlot log2Plot = log2Chart.getCategoryPlot();
 			CategoryAxis log2Axis = log2Plot.getDomainAxis();
 			log2Axis.setLowerMargin(0.02); // two percent
 			log2Axis.setCategoryMargin(0.20); // 20 percent
 			log2Axis.setUpperMargin(0.02); // two percent
 
 			// same for our fake chart - just to get the tooltips
 			meanChart.setBackgroundPaint(java.awt.Color.white);
 			CategoryPlot meanPlot = meanChart.getCategoryPlot();
 			CategoryAxis meanAxis = meanPlot.getDomainAxis();
 			meanAxis.setLowerMargin(0.02); // two percent
 			meanAxis.setCategoryMargin(0.20); // 20 percent
 			meanAxis.setUpperMargin(0.02); // two percent
 
 			//	median plot
 			medianChart.setBackgroundPaint(java.awt.Color.white);
 			CategoryPlot medianPlot = medianChart.getCategoryPlot();
 			CategoryAxis medianAxis = medianPlot.getDomainAxis();
 			medianAxis.setLowerMargin(0.02); // two percent
 			medianAxis.setCategoryMargin(0.20); // 20 percent
 			medianAxis.setUpperMargin(0.02); // two percent
 			
 			// customise the renderer...
 			StatisticalBarRenderer log2Renderer = new StatisticalBarRenderer();
 
 			// BarRenderer renderer = (BarRenderer) plot.getRenderer();
 			log2Renderer.setItemMargin(0.01); // one percent
 			log2Renderer.setDrawBarOutline(true);
 			log2Renderer.setOutlinePaint(Color.BLACK);
 			log2Renderer.setToolTipGenerator(new CategoryToolTipGenerator() {
 
 				public String generateToolTip(CategoryDataset dataset,
 						int series, int item) {
 					HashMap pv = gpds.getPValuesHashMap();
 					HashMap std_d = gpds.getStdDevMap();
 					
 					String currentPV = (String) pv.get(dataset
 							.getRowKey(series)
 							+ "::" + dataset.getColumnKey(item));
 					String stdDev = (String) std_d.get(dataset
 							.getRowKey(series)
 							+ "::" + dataset.getColumnKey(item));
 					
 					return "Probeset : " + dataset.getRowKey(series)
 							+ "<br/>Intensity : "
 							+ new DecimalFormat("0.0000").format(dataset.getValue(series, item)) + "<br/>"+RembrandtConstants.PVALUE+" : "
 							+ currentPV + "<br/>Std. Dev.: " + stdDev + "<br/>";
 				}
 
 			});
 			log2Plot.setRenderer(log2Renderer);
 			// customize the  renderer
 			BarRenderer meanRenderer = (BarRenderer) meanPlot.getRenderer();
 			meanRenderer.setItemMargin(0.01); // one percent
 			meanRenderer.setDrawBarOutline(true);
 			meanRenderer.setOutlinePaint(Color.BLACK);
 			meanRenderer.setToolTipGenerator(new CategoryToolTipGenerator() {
 
 				public String generateToolTip(CategoryDataset dataset,
 						int series, int item) {
 					HashMap pv = gpds.getPValuesHashMap();
 					HashMap std_d = gpds.getStdDevMap();
 					String currentPV = (String) pv.get(dataset
 							.getRowKey(series)
 							+ "::" + dataset.getColumnKey(item));
 					
 					String stdDev = (String) std_d.get(dataset
 							.getRowKey(series)
 							+ "::" + dataset.getColumnKey(item));
 					
 					return "Probeset : " + dataset.getRowKey(series) +
 							"<br/>Intensity : "+ new DecimalFormat("0.0000").format(dataset.getValue(series, item)) + 
 							"<br/>"+RembrandtConstants.PVALUE +": " + currentPV +
 							"<br/>";
 							//"<br/>Std. Dev.: " + stdDev + "<br/>";
 				}
 
 			});
 
 			meanPlot.setRenderer(meanRenderer);
 			// customize the  renderer
 			BarRenderer medianRenderer = (BarRenderer) medianPlot.getRenderer();
 			medianRenderer.setItemMargin(0.01); // one percent
 			medianRenderer.setDrawBarOutline(true);
 			medianRenderer.setOutlinePaint(Color.BLACK);
 			medianRenderer.setToolTipGenerator(new CategoryToolTipGenerator() {
 
 				public String generateToolTip(CategoryDataset dataset,
 						int series, int item) {
 					HashMap pv = gpds.getPValuesHashMap();
 					HashMap std_d = gpds.getStdDevMap();
 					String currentPV = (String) pv.get(dataset
 							.getRowKey(series)
 							+ "::" + dataset.getColumnKey(item));
 					
 					String stdDev = (String) std_d.get(dataset
 							.getRowKey(series)
 							+ "::" + dataset.getColumnKey(item));
 					
 					return "Probeset : " + dataset.getRowKey(series) +
 							"<br/>Intensity : "+ new DecimalFormat("0.0000").format(dataset.getValue(series, item)) + 
 							"<br/>"+RembrandtConstants.PVALUE +": " + currentPV +
 							"<br/>";
 							//"<br/>Std. Dev.: " + stdDev + "<br/>";
 				}
 
 			});
 
 			// LegendTitle lg = chart.getLegend();
 
 			medianPlot.setRenderer(medianRenderer);
 			// lets generate a custom legend - assumes theres only one source?
 			LegendItemCollection lic = log2Chart.getLegend().getSources()[0].getLegendItems();
 			legendHtml = LegendCreator.buildLegend(lic, "Probesets");
 
 			log2Chart.removeLegend();
 			meanChart.removeLegend();
 			medianChart.removeLegend();
 			
 			//bwChart.removeLegend(); // <-- do this above
 
 			// Write the chart image to the temporary directory
 			ChartRenderingInfo info = new ChartRenderingInfo(new StandardEntityCollection());
 			
 			// BW
 			if(bwChart != null){
 				int bwwidth = new BigDecimal(1.5).multiply(new BigDecimal(imgW)).intValue();
 				bwFilename = ServletUtilities.saveChartAsPNG(bwChart, bwwidth, 400, info, session);
 				CustomOverlibToolTipTagFragmentGenerator ttip = new CustomOverlibToolTipTagFragmentGenerator();
 				ttip.setExtra(" href='javascript:void(0);' "); //must have href for area tags to have cursor:pointer
 				ChartUtilities.writeImageMap(pw, bwFilename, info,
 						ttip,
 						new StandardURLTagFragmentGenerator());
 				info.clear(); // lose the first one
 				info = new ChartRenderingInfo(new StandardEntityCollection());
 			}
 			//END  BW
 			log2Filename = ServletUtilities.saveChartAsPNG(log2Chart, imgW, 400, info, session);
 			ChartUtilities.writeImageMap(pw, log2Filename, info,
 					new CustomOverlibToolTipTagFragmentGenerator(),
 					new StandardURLTagFragmentGenerator());
 			// clear the first one and overwrite info with our second one - no
 			// error bars
 			info.clear(); // lose the first one
 			info = new ChartRenderingInfo(new StandardEntityCollection());
 			rawFilename = ServletUtilities.saveChartAsPNG(meanChart, imgW, 400, info, session);
 			// Write the image map to the PrintWriter
 			// can use a different writeImageMap to pass tooltip and URL custom
 			ChartUtilities.writeImageMap(pw, rawFilename, info,
 					new CustomOverlibToolTipTagFragmentGenerator(),
 					new StandardURLTagFragmentGenerator());
 			
 			info.clear(); // lose the first one
 			info = new ChartRenderingInfo(new StandardEntityCollection());
 			medianFilename = ServletUtilities.saveChartAsPNG(medianChart, imgW, 400, info, session);
 			
 			// Write the image map to the PrintWriter
 			// can use a different writeImageMap to pass tooltip and URL custom
 
 			ChartUtilities.writeImageMap(pw, medianFilename, info,
 					new CustomOverlibToolTipTagFragmentGenerator(),
 					new StandardURLTagFragmentGenerator());
 			
 			// ChartUtilities.writeImageMap(pw, filename, info, true);
 
 			pw.flush();
 
 		} catch (Exception e) {
 			System.out.println("Exception - " + e.toString());
 			e.printStackTrace(System.out);
 			log2Filename = "public_error_500x300.png";
 		}
 		// return filename;
 		charts.put("errorBars", log2Filename);
 		charts.put("noErrorBars", rawFilename);
 		charts.put("medianBars",medianFilename);
 		charts.put("bwFilename", bwFilename);
 		charts.put("legend", legendHtml);
 		charts.put("size", ps.toString());
 
 
 		return charts;
 	}
 
 }
