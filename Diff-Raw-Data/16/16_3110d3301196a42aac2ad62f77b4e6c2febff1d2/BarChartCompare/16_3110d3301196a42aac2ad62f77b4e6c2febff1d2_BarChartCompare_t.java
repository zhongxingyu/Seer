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
 
 import org.daxplore.presenter.chart.ChartTools;
 import org.daxplore.presenter.chart.QueryInterface;
 import org.daxplore.presenter.chart.QueryInterface.QueryCallback;
 import org.daxplore.presenter.chart.data.QueryResult;
 import org.daxplore.presenter.chart.data.QueryResultCountCompare;
 import org.daxplore.presenter.chart.resources.ChartConfig;
 import org.daxplore.presenter.chart.resources.ChartTexts;
 import org.daxplore.presenter.shared.QueryDefinition.QueryFlag;
 
 /**
  * A bar chart that compares two data sets, displaying frequencies in different
  * groups.
  * 
  * <p>The comparative version of the bar chart works like the standard bar
  * chart, except for a few details. For a description of the shared
  * functionality, see {@link BarChart}.</p>
  * 
  * <p>In the comparative bar chart, each group displays to data sets. One of the
  * sets are called primary and one secondary. The primary data set is shown in
  * front in clear visible way, overlapping the secondary bars which are put in
  * the background. Each bar in the primary data set overlaps its corresponding
  * bar in the secondary data set, allowing for a direct comparison between the
  * two data sets.</p>
  * 
  * @see BarChart
  * @see QueryInterface
  * @see QueryResult
  */
 public class BarChartCompare extends BarChart {
 
 	/**
 	 * The distance between the bar-pairs in a group.
 	 * 
 	 * <p>Meassured in GChart's internal distance system.
 	 */
 	private final static double internalGroupSpacing = 0.2;
 
 	/**
 	 * How far the secondary bar is shifted to the right, relative to it's
 	 * primary bar.
 	 * 
 	 * <p>Meassured in GChart's internal distance system.
 	 */
 	private final static double secondaryBarShift = 0.5;
 
 	/**
 	 * A list of all the secondary bars.
 	 */
 	private LinkedList<BarChartBarSecondary> barListSecondary;
 
 	/**
 	 * Create a new bar chart that compare data sets.
 	 * 
 	 * <p>A query is given, that contains all the data needed to draw the
 	 * barchart. When the query has loaded all the needed data the method
 	 * addData will be called, which finalizes the construction of the
 	 * chart.</p>
 	 * 
 	 * @param query
 	 *            The query that this chart will display.
 	 */
 	protected BarChartCompare(ChartTexts chartTexts, ChartConfig chartConfig, final QueryInterface query, boolean printerMode) {
 		super(chartTexts, chartConfig, query, printerMode);
 
 		xTickMaxCharacterCount = 19;
 
 		// Twice the number of bars, compared to the basic bar chart.
 		barCount *= 2;
 		betweenGroupsTickCurveIndex = barCount;
 		paddingBarIndex = betweenGroupsTickCurveIndex + groupCount;
 
 		groupWidth = questionOptionCount * (1 + secondaryBarShift) + (questionOptionCount - 1) * internalGroupSpacing;
 		groupDistance = groupWidth + groupSpacing;
 
 		addStyleDependentName("compare");
 		if (ChartTools.ieVersion() > 0) {
 			addStyleDependentName("compare-IE");
 		}
 
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
 
 	@Override
 	protected void createCurves(List<String> questionOptionTexts, boolean printerMode) {
 		/*
 		 * Lists are created here to make sure they exist when the
 		 * super-constructor calls the overridden version of
 		 * createCurvesAndLegend.
 		 */
 		barListPrimary = new LinkedList<BarChartBarPrimary>();
 		barListSecondary = new LinkedList<BarChartBarSecondary>();
 
 		for (int groupIndex = 0; groupIndex < groupCount; groupIndex++) {
 			for (int questionIndex = 0; questionIndex < questionOptionCount; questionIndex++) {
 				addCurve();
 				Curve curveSecondary = getCurve();
 				addCurve();
 				Curve curvePrimary = getCurve();
 				AnnotationLocation hoverLocation = AnnotationLocation.SOUTH;
 				if(groupCount>1 && groupIndex==0){
 					hoverLocation = AnnotationLocation.SOUTHEAST;
 				} else if(groupCount>1 && groupIndex==groupCount-1) {
 					hoverLocation = AnnotationLocation.SOUTHWEST;
 				}
 				addPrimaryBar(new BarChartBarPrimary(chartTexts, curvePrimary, getColorSet(questionIndex), printerMode, hoverLocation));
 				addSecondaryBar(new BarChartBarSecondary(chartTexts, curveSecondary, getColorSet(questionIndex), printerMode, hoverLocation));
 			}
 		}
 		for (int i = 0; i < groupCount; i++) {
 			addBetweenGroupsTickCurve();
 		}
 		addPaddingCurve();
 	}
 
 	/**
 	 * When a new secondary bar is created, add it here.
 	 * 
 	 * <p>It is important to add the curves in left-to-right order. By keeping
 	 * track of the index of the bar, we can find it when we want to add data to
 	 * the curve of a specific index. It also makes it possible to find the bar
 	 * that belongs to a curve that is being hovered.</p>
 	 * 
 	 * @param bar
 	 *            The bar to be stored in the chart
 	 */
 	protected void addSecondaryBar(BarChartBarSecondary bar) {
 		barListSecondary.add(bar);
 	}
 
 	/**
 	 * Get the secondary bar at a specific position in the chart.
 	 * 
 	 * <p>The bars are ordered from left to right. Each index will have one
 	 * primary bar and one secondary bar.</p>
 	 * 
 	 * @param index
 	 *            The position of the bar in the chart.
 	 * @return The primary bar at the given position.
 	 * 
 	 * @see getBarSecondary
 	 */
 	protected ChartBar getBarSecondary(int index) {
 		return barListSecondary.get(index);
 	}
 
 	@Override
 	protected ChartBar getBar(Curve curve) {
 		for (ChartBar column : barListPrimary) {
 			if (column.getCurve() == curve) {
 				return column;
 			}
 		}
 		for (ChartBar column : barListSecondary) {
 			if (column.getCurve() == curve) {
 				return column;
 			}
 		}
 		return null;
 	}
 
 	@Override
 	protected void addData() {
 		QueryResultCountCompare queryResult = (QueryResultCountCompare) query.getResult(true, false);
 		List<String> perspectiveOptionTexts = queryDefinition.getPerspectiveOptionTexts();
 		currentPosition = 1 + groupSpacing / 2;
 		currentGroup = 0;
 		for (int perspectiveOption : usedPerspectiveOptions) {
 			if (currentGroup > 0) {
 				drawBetweenGroupsTick();
 			}
			if (queryResult.hasData(perspectiveOption) && queryResult.getPopulation(perspectiveOption)!=0) { //TODO temporary hack, handle cut-off properly producer
 				drawBarGroup(perspectiveOptionTexts.get(perspectiveOption),
 						queryResult.getPopulation(perspectiveOption), queryResult.getCountDataPercentages(perspectiveOption),
 						queryResult.getPopulationSecondary(perspectiveOption), queryResult.getCountDataPercentagesSecondary(perspectiveOption));
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
			if (queryResult.hasTotalDataItemData()&& queryResult.getTotalPopulation()!=0) { //TODO temporary hack, handle cut-off properly producer
 				drawBarGroup(totalText,
 						queryResult.getTotalPopulation(), queryResult.getTotalCountDataPercentages(),
 						queryResult.getTotalPopulationSecondary(), queryResult.getTotalCountDataPercentagesSecondary());
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
 	 * at a specific index is given by the height of the value at the
 	 * corresponding index in the percentageData array.</p>
 	 * 
 	 * @param groupName
 	 *            The name of the group, to be displayed under the x-axis tick.
 	 * @param groupSizePrimary
 	 *            The number of people in the primary group, to be displayed
 	 *            under the x-axis tick.
 	 * @param percentageDataPrimary
 	 *            An array containing the primary bar data.
 	 * @param groupSizeSecondary
 	 *            The number of people in the secondary group, to be displayed
 	 *            under the x-axis tick.
 	 * @param percentageDataSecondary
 	 *            An array containing the secondary bar data.
 	 */
 	private void drawBarGroup(String groupName, int groupSizePrimary, double[] percentageDataPrimary, int groupSizeSecondary, double[] percentageDataSecondary) {
 		List<String> questionOptionTexts = queryDefinition.getQuestionOptionTexts();
 		for (int dataIndex = 0; dataIndex < questionOptionCount; dataIndex++) {
 			BarChartBarPrimary primaryBar = (BarChartBarPrimary) getBarPrimary(currentGroup * questionOptionCount + dataIndex);
 			Curve curve = primaryBar.getCurve();
 			if (dataIndex < percentageDataPrimary.length) {
 				curve.addPoint(currentPosition + dataIndex * (1 + secondaryBarShift + internalGroupSpacing), percentageDataPrimary[dataIndex]);
 
 				primaryBar.setHoverTextComparative(percentageDataPrimary[dataIndex], questionOptionTexts.get(dataIndex));
 
 				if (percentageDataPrimary[dataIndex] > maxValue) {
 					maxValue = percentageDataPrimary[dataIndex];
 				}
 			}
 
 			BarChartBarSecondary secondaryBar = (BarChartBarSecondary) getBarSecondary(currentGroup * questionOptionCount + dataIndex);
 			curve = secondaryBar.getCurve();
 			if (groupSizeSecondary > 0 && dataIndex < percentageDataSecondary.length) {
 				curve.addPoint(currentPosition + dataIndex * (1 + secondaryBarShift + internalGroupSpacing) + secondaryBarShift, percentageDataSecondary[dataIndex]);
 				secondaryBar.setHoverTextComparative(percentageDataSecondary[dataIndex], questionOptionTexts.get(dataIndex));
 
 				if (percentageDataSecondary[dataIndex] > maxValue) {
 					maxValue = percentageDataSecondary[dataIndex];
 				}
 			}
 		}
 
 		// TODO calculate offset from widgets?
 		xTickMaxCharacterCount = Math.max(xTickMaxCharacterCount, groupName.length());
 
 		String tickText;
 		if (groupSizeSecondary > 0) {
 			tickText = chartTexts.compareTick(groupName, groupSizePrimary, groupSizeSecondary);
 		} else {
 			tickText = chartTexts.compareMissingSecondaryTick(groupName, groupSizePrimary, chartConfig.respondentCountCutoff());
 			xTickMaxCharacterCount = Math.max(xTickMaxCharacterCount, 23);
 		}
 		getXAxis().addTick(currentPosition + groupWidth / 2 - 1, tickText);
 	}
 
 	/**
 	 * Draws a group for which there is no data.
 	 * 
 	 * <p>When a group is too small there is no data to display. Call this
 	 * method to add a tick and explanatory text, where the group should have
 	 * been.</p>
 	 * 
 	 * @param groupName
 	 */
 	private void drawMissingBarGroup(String groupName) {
 		String tickText = chartTexts.compareMissingTick(groupName, chartConfig.respondentCountCutoff());
 		getXAxis().addTick(currentPosition + groupWidth / 2 - 1, tickText);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void setChartSizeSmart(int width, int height) {
 		setChartSize(width, height);
 		setChartSize(Math.max(2 * width - getXChartSizeDecorated() - 5, 0), Math.max(2 * height - getYChartSizeDecorated() - 15, 0));
 		update();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public int getMinWidth() {
 		double groupWidth = Math.max(xTickMaxCharacterCount * 7.5, questionOptionCount * 30);
 		return (int) (80 + groupWidth * groupCount);
 	}
 }
