 /*
  *  Copyright 2012 Axel Winkler, Daniel Dunér
  * 
  *  This file is part of Daxplore Presenter.
  *
  *  Daxplore Presenter is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU Lesser General Public License as published by
  *  the Free Software Foundation, either version 2.1 of the License, or
  *  (at your option) any later version.
  *
  *  Daxplore Presenter is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU Lesser General Public License for more details.
  *
  *  You should have received a copy of the GNU Lesser General Public License
  *  along with Daxplore Presenter.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.daxplore.presenter.chart.display;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import org.daxplore.presenter.chart.QueryInterface;
 import org.daxplore.presenter.chart.QueryInterface.QueryCallback;
 import org.daxplore.presenter.chart.data.QueryResult;
 import org.daxplore.presenter.chart.data.QueryResultCount;
 import org.daxplore.presenter.chart.resources.ChartConfig;
 import org.daxplore.presenter.chart.resources.ChartTexts;
 import org.daxplore.presenter.shared.QueryDefinition.QueryFlag;
 
 import com.google.gwt.event.dom.client.MouseMoveEvent;
 import com.google.gwt.event.dom.client.MouseMoveHandler;
 import com.google.gwt.event.dom.client.MouseOutEvent;
 import com.google.gwt.event.dom.client.MouseOutHandler;
 import com.googlecode.gchart.client.GChart;
 
 /**
  * A basic bar chart, displaying frequencies in different groups. </p>
  * 
  * <p>On the left side of the chart a Y-axis is displayed. The Y axis starts at
  * 0% and shows a tick for each 10% and is high enough to cover all the bars in
  * the chart. The bars are divided into groups of equal size. The sums of the
  * bars in each group is always 100%, with the percentages split among the bars.
  * The bars in each group are colored in a specific sequence, which is matched
  * by the text in the legend to the right. It is possible to mouse over each
  * bar, to get a more detailed description of the bar. Under each group is a
  * tick-text, describing what the group is made up of and the size of the
  * group.</p>
  * 
  * <p>To create a new chart, you have to supply a Query object, created
  * elsewhere. The Query object contains all the data needed to display the
  * chart. The loading of the chart is made in two steps. In the constructor, the
  * Query data can be used to create the correct number of groups, columns and a
  * legend. When the query data has been loaded from the server, the method
  * addData is called. In the addData method, the height and position of the
  * columns are set. When the columns are set, the ready function is called,
  * which updates the chart and adds it to the callback ChartPanel.</p>
  * 
  * @see BarChartCompare
  * @see QueryInterface
  * @see QueryResult
  */
 public class BarChart extends GChartChart {
 
 	/**
 	 * The distance between the bars in a group.
 	 * 
 	 * Meassured in GChart's internal distance system.
 	 */
 	private final static double internalGroupSpacing = 0.15;
 
 	/**
 	 * The space between different groups, using the internal GChart distance
 	 * system.
 	 */
 	protected final static double groupSpacing = 1.5;
 
 	/**
 	 * Keeps track of how wide each group is going to be.
 	 * 
 	 * <p>The value is computed in the constructor. Meassured in GChart's internal
 	 * distance system.</p>
 	 */
 	protected double groupWidth;
 
 	/**
 	 * Keeps track of the distance between the midpoint of each group.
 	 * 
 	 * <p>The value is computed in the constructor. Meassured in GChart's internal
 	 * distance system.</p>
 	 */
 	protected double groupDistance;
 
 	/**
 	 * The height of the highest bar.
 	 * 
 	 * <p>Is used when drawing the Y-axis. The default value is 0.1 to begin with,
 	 * so that the Y-axis shows at least 0%-10%.</p>
 	 */
 	protected double maxValue = 0.1;
 
 	/**
 	 * Keeps track of what position the next groups should be drawn at.
 	 * 
 	 * <p>Uses the internal GChart distance system.</p>
 	 */
 	protected double currentPosition;
 
 	/**
 	 * Keeps track of which group to draw next.
 	 */
 	protected int currentGroup;
 
 	/**
 	 * Keeps track of how many options the question has.
 	 * 
 	 * <p>This corresponds to the number of columns in each group.</p>
 	 */
 	protected int questionOptionCount;
 
 	/**
 	 * Keeps track of the number of groups to be drawn.
 	 * 
 	 * <p>Includes both the selected options and the total item.</p>
 	 */
 	protected int groupCount;
 
 	/**
 	 * Keeps track of the number of bars to be drawn.
 	 * 
 	 * <p>Includes both the selected options and the total item.</p>
 	 */
 	protected int barCount;
 
 	/**
 	 * The index of the curve used to draw ticks between groups.
 	 */
 	protected int betweenGroupsTickCurveIndex;
 
 	/**
 	 * The curve index of the padding bar.
 	 */
 	protected int paddingBarIndex;
 
 	/**
 	 * A list of the groups to be drawn.
 	 * 
 	 * <p>In addition to these, there is also the total-item.</p>
 	 */
 	protected List<Integer> usedPerspectiveOptions;
 
 	/**
 	 * A list of all the primary bars.
 	 * 
 	 * <p>In the basic version of this class, there are no secondary bars.</p>
 	 */
 	protected LinkedList<BarChartBarPrimary> barListPrimary;
 
 	/**
 	 * If a bar is hovered with the mouse, it is stored here.
 	 * 
 	 * <p>Keeping track of it makes it possible to restore any changes made to it,
 	 * while it was being hovered.</p>
 	 */
 	private ChartBar hoveredBar;
 
 	/**
 	 * The query that this chart displays.
 	 */
 	protected final QueryInterface query;
 	protected ChartConfig chartConfig;
 
 	protected int xTickMaxCharacterCount = 13;
 
 	/**
 	 * Create a new bar chart.
 	 * 
 	 * <p>A query is given, that contains all the data needed to draw the barchart.
 	 * When the query has loaded all the needed data the method addData will be
 	 * called, which finalizes the construction of the chart.</p>
 	 * 
 	 * @param query
 	 *            The query that this chart will display.
 	 */
 	protected BarChart(ChartTexts chartTexts, ChartConfig chartConfig, final QueryInterface query, boolean printerMode) {
 		super(chartTexts, chartConfig, query);
 		this.query = query;
 		this.chartConfig = chartConfig;
 
 		externalLegend = new ExternalLegend(chartTexts, query, false, chartConfig.externalLegendItemLimit(), printerMode);
 
 		usedPerspectiveOptions = queryDefinition.getUsedPerspectiveOptions();
 		groupCount = usedPerspectiveOptions.size() + (queryDefinition.hasFlag(QueryFlag.TOTAL) ? 1 : 0);
 		if (groupCount <= 0) {
 			throw new Error("Group count = " + groupCount);
 		}
 		questionOptionCount = queryDefinition.getQuestionOptionCount();
 		barCount = groupCount * questionOptionCount;
 
 		betweenGroupsTickCurveIndex = barCount;
 		paddingBarIndex = betweenGroupsTickCurveIndex + groupCount;
 
 		groupWidth = questionOptionCount * 1 + (questionOptionCount - 1) * internalGroupSpacing;
 		groupDistance = groupWidth + groupSpacing;
 
 		createCurves(queryDefinition.getQuestionOptionTexts(), printerMode);
 		setupMouseHandlers();
 		setupAxes();
 
 		if (this.getClass().equals(BarChart.class)) {
 			if (query.hasResult()) {
 				addData();
 			} else {
 				query.requestResult(new QueryCallback() {
 					@Override
 					public void callback() {
 						addData();
 					}
 				});
 			}
 		}
 	}
 
 	/**
 	 * Set up the curves, the legend and the axes of the chart.
 	 * 
 	 * @param questionOptionTexts
 	 *            The texts of the question's options.
 	 */
 	protected void createCurves(List<String> questionOptionTexts, boolean printerMode) {
 		barListPrimary = new LinkedList<BarChartBarPrimary>();
 
 		for (int groupIndex = 0; groupIndex < groupCount; groupIndex++) {
 			for (int questionIndex = 0; questionIndex < questionOptionCount; questionIndex++) {
 				addCurve();
 				Curve curve = getCurve();
 				AnnotationLocation hoverLocation = AnnotationLocation.SOUTH;
 				if(groupCount>1 && groupIndex==0){
 					hoverLocation = AnnotationLocation.SOUTHEAST;
 				} else if(groupCount>1 && groupIndex==groupCount-1) {
 					hoverLocation = AnnotationLocation.SOUTHWEST;
 				}
 				addPrimaryBar(new BarChartBarPrimary(chartTexts, curve, getColorSet(questionIndex), printerMode, hoverLocation));
 			}
 		}
 		for (int i = 0; i < groupCount; i++) {
 			addBetweenGroupsTickCurve();
 		}
 		addPaddingCurve();
 	}
 
 	/**
 	 * Adds extra curve to use as end-of-chart padding.
 	 */
 	protected void addBetweenGroupsTickCurve() {
 		addCurve();
 		Curve curve = getCurve();
 		Symbol symbol = curve.getSymbol();
 		symbol.setSymbolType(SymbolType.LINE);
 		symbol.setHoverAnnotationEnabled(false);
 		symbol.setHoverSelectionEnabled(false);
 		symbol.setModelWidth(0);
 		symbol.setFillThickness(1);
 		symbol.setBorderColor("black");
 		symbol.setImageURL("/img/daxplore-chart-tick.gif");
 	}
 
 	/**
 	 * Adds extra curve to use as end-of-chart padding.
 	 */
 	protected void addPaddingCurve() {
 		addCurve();
 		Symbol symbol = getCurve().getSymbol();
 		symbol.setBorderStyle("none");
 		symbol.setBorderWidth(0);
 		symbol.setSymbolType(SymbolType.VBAR_SOUTHWEST);
 		symbol.setHoverAnnotationEnabled(false);
 		symbol.setModelWidth(0);
 	}
 
 	/**
 	 * Set up the appearance of the axes.
 	 * 
 	 * <p>When the data is loaded, call setYAxis to give it the Y-axis the correct
 	 * height.</p>
 	 * 
 	 * @see setYAxis
 	 */
 	private void setupAxes() {
 		getXAxis().setTickLabelFontSize(12);
 		getXAxis().setTickLabelThickness(35);
 		getXAxis().setTickLength(6); // small tick-like gap...
 		getXAxis().setTickThickness(0); // but with invisible ticks
 		getXAxis().setAxisMin(0); // keeps first bar on chart
 
 		getYAxis().setAxisMin(0);
 		getYAxis().setTickLabelFormat("#%");
 	}
 
 	/**
 	 * Specify the height and ticks of the Y-Axis.
 	 * 
 	 * <p>The Y-Axis is adjusted, so that all bars fit and no unnessecary space
 	 * exists above the bars.</p>
 	 * 
 	 * @param maxValue
 	 *            The height of the heighest bar.
 	 */
 	protected void setYAxis(double maxValue) {
 		maxValue = (double) Math.round(maxValue * 10 + 0.49) / 10;
 		getYAxis().setAxisMax(maxValue);
 		getYAxis().setTickCount((int) (Math.round(maxValue * 10) + 1));
 	}
 
 	/**
 	 * When a new bar is created, add it here.
 	 * 
 	 * <p>It is important to add the curves in left-to-right order. By keeping
 	 * track of the index of the bar, we can find it when we want to add data to
 	 * the curve of a specific index. It also makes it possible to find the bar
 	 * that belongs to a curve that is being hovered.</p>
 	 * 
 	 * @param bar
 	 *            The bar to be stored in the chart
 	 */
 	protected void addPrimaryBar(BarChartBarPrimary bar) {
 		barListPrimary.add(bar);
 	}
 
 	/**
 	 * Get the primary bar at a specific position in the chart.
 	 * 
 	 * The bars are ordered from left to right. If there are secondary bars,
 	 * each index will have one primary bar and one secondary bar.</p>
 	 * 
 	 * @param index
 	 *            The position of the bar in the chart.
 	 * @return The primary bar at the given position.
 	 */
 	protected ChartBar getBarPrimary(int index) {
 		return barListPrimary.get(index);
 	}
 
 	/**
 	 * Get the bar that belongs to a specific GChart curve.
 	 * 
 	 * @param curve
 	 *            The curve that the bar belongs to.
 	 * @return The bar that belongs to the given curve.
 	 */
 	protected ChartBar getBar(Curve curve) {
 		for (ChartBar column : barListPrimary) {
 			if (column.getCurve() == curve) {
 				return column;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Add mouse handlers, to keep track what bars are hovered and unhovered.
 	 */
 	protected void setupMouseHandlers() {
 		addMouseMoveHandler(new MouseMoveHandler() {
 			@Override
 			public void onMouseMove(MouseMoveEvent event) {
 				GChart theGChart = (GChart) event.getSource();
 				Curve curve = theGChart.getTouchedCurve();
 				boolean updateNeeded = false;
 				if (hoveredBar != null) {
 					hoveredBar.unhover();
 					hoveredBar = null;
 					updateNeeded = true;
 				}
 				if (curve != null) {
 					hoveredBar = getBar(curve);
 					if (hoveredBar != null) {
 						hoveredBar.hover();
 						updateNeeded = true;
 					}
 				}
 				if (updateNeeded) {
 					theGChart.update();
 				}
 			}
 		});
 
 		addMouseOutHandler(new MouseOutHandler() {
 			@Override
 			public void onMouseOut(MouseOutEvent event) {
 				GChart theGChart = (GChart) event.getSource();
 				boolean updateNeeded = false;
 				if (hoveredBar != null) {
 					hoveredBar.unhover();
 					hoveredBar = null;
 					updateNeeded = true;
 				}
 				if (updateNeeded) {
 					theGChart.update();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Add the data, to finish the construction of the chart.
 	 * 
 	 * <p>When the query is ready, this method is called. It will add the actual
 	 * data from the query. Once the data is added, the chart is ready to be
 	 * displayed. This method triggers the drawing of the chart by calling the
 	 * setReady() method.</p>
 	 * 
 	 * @param queryDefinition
 	 */
 	protected void addData() {
 		QueryResultCount queryResult = (QueryResultCount) query.getResult(false, false);
 		List<String> perspectiveOptionTexts = queryDefinition.getPerspectiveOptionTexts();
 		currentPosition = 1 + groupSpacing / 2 + internalGroupSpacing;
 		currentGroup = 0;
 		for (int perspectiveOption : usedPerspectiveOptions) {
 			if (currentGroup > 0) {
 				drawBetweenGroupsTick();
 			}
			if (queryResult.hasData(perspectiveOption)
					&& queryResult.getPopulation(perspectiveOption)!=0) { //TODO temporary hack, handle cut-off properly producer 
 				drawBarGroup(perspectiveOptionTexts.get(perspectiveOption), queryResult.getPopulation(perspectiveOption), queryResult.getCountDataPercentages(perspectiveOption));
 			} else {
 				drawMissingBarGroup(perspectiveOptionTexts.get(perspectiveOption));
 			}
 			currentPosition += groupDistance;
 			currentGroup++;
 		}
 		if (queryDefinition.hasFlag(QueryFlag.TOTAL)) {
 			if (currentGroup > 0) {
 				drawBetweenGroupsTick();
 			}
 			String totalText = chartTexts.compareWithAll();
			if (queryResult.hasTotalDataItemData() && queryResult.getTotalPopulation()!=0) { //TODO temporary hack, handle cut-off properly producer
 				drawBarGroup(totalText, queryResult.getTotalPopulation(), queryResult.getTotalCountDataPercentages());
 			} else {
 				drawMissingBarGroup(totalText);
 			}
 			currentPosition += groupDistance;
 			currentGroup++;
 		}
 		currentPosition -= (1 + groupSpacing / 2);
 
 		drawPaddingBar();
 		setYAxis(maxValue);
 
 		setReady();
 	}
 
 	/**
 	 * Draw a group of bars.
 	 * 
 	 * <p>The group will be drawn at the position given by currentPosition. Each
 	 * bar in the group has a local index, beginning at 0. The height of the bar
 	 * at a specific index, is given by the height of the value at the
 	 * corresponding index in the percentageData array.</p>
 	 * 
 	 * @param groupName
 	 *            The name of the group, to be displayed under the x-axis tick.
 	 * @param groupSize
 	 *            The number of people in the group, to be displayed under the
 	 *            x-axis tick.
 	 * @param percentageData
 	 *            An array containing the bar data.
 	 */
 	private void drawBarGroup(String groupName, int groupSize, double[] percentageData) {
 		List<String> questionOptionTexts = queryDefinition.getQuestionOptionTexts();
 		for (int dataIndex = 0; dataIndex < questionOptionCount; dataIndex++) {
 			BarChartBarPrimary bar = (BarChartBarPrimary) getBarPrimary(currentGroup * questionOptionCount + dataIndex);
 			if (dataIndex < percentageData.length) {
 				bar.setDataPoint(currentPosition + dataIndex * (1 + internalGroupSpacing), percentageData[dataIndex]);
 				bar.setHoverTextStandard(percentageData[dataIndex], questionOptionTexts.get(dataIndex));
 
 				if (percentageData[dataIndex] > maxValue) {
 					maxValue = percentageData[dataIndex];
 				}
 			} else {
 				bar.setDataPoint(currentPosition + dataIndex * (1 + internalGroupSpacing), 0);
 			}
 		}
 		xTickMaxCharacterCount = Math.max(xTickMaxCharacterCount, groupName.length());
 		String tickText = chartTexts.standardTick(groupName, groupSize);
 		getXAxis().addTick(currentPosition + groupWidth / 2 - 1, tickText);
 	}
 
 	/**
 	 * Draws a group for which there is no data.
 	 * 
 	 * <p>When there is no data to display for this group, call this method. A tick
 	 * and explanatory text is added where the group should have been.</p>
 	 * 
 	 * @param groupName
 	 */
 	private void drawMissingBarGroup(String groupName) {
 		String tickText = chartTexts.missingTick(groupName, chartConfig.respondentCountCutoff());
 		getXAxis().addTick(currentPosition + groupWidth / 2 - 1, tickText.replaceFirst("%s", groupName));
 	}
 
 	/**
 	 * Draw a padding column at the end of the chart.
 	 * 
 	 * <p>By adding the padding column, the last bar of the last group won't be
 	 * drawn all the way out to the right. The X-axis will go on for a bit
 	 * beyond the last column, making the chart look a bit better.</p>
 	 */
 	protected void drawPaddingBar() {
 		getCurve(paddingBarIndex).addPoint(currentPosition, 0);
 	}
 
 	protected void drawBetweenGroupsTick() {
 		getCurve(betweenGroupsTickCurveIndex + currentGroup).addPoint(currentPosition - groupSpacing / 2 - 1.0, -0.01);
 		getCurve(betweenGroupsTickCurveIndex + currentGroup).addPoint(currentPosition - groupSpacing / 2 - 1.0, 0.01);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public int getMinWidth() {
 		double groupWidth = Math.max(xTickMaxCharacterCount * 7.5, questionOptionCount * 20);
 		return (int) (80 + groupWidth * groupCount);
 	}
 }
