 package cl.votainteligente.inspector.client.views;
 
 import cl.votainteligente.inspector.client.i18n.ApplicationMessages;
 import cl.votainteligente.inspector.client.presenters.ParlamentarianPresenter;
 import cl.votainteligente.inspector.client.resources.DisplayCellTableResource;
 import cl.votainteligente.inspector.client.uihandlers.ParlamentarianUiHandlers;
 import cl.votainteligente.inspector.client.widgets.ShareThis;
 import cl.votainteligente.inspector.model.ParlamentarianComment;
 import cl.votainteligente.inspector.model.Society;
 import cl.votainteligente.inspector.model.Stock;
 import cl.votainteligente.inspector.shared.NotificationEventType;
 
 import com.gwtplatform.mvp.client.ViewWithUiHandlers;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.user.cellview.client.CellTable;
 import com.google.gwt.user.client.ui.*;
 
 import org.adapters.highcharts.codegen.sections.options.OptionPath;
 import org.adapters.highcharts.codegen.sections.options.types.RawStringType;
 import org.adapters.highcharts.codegen.types.SeriesType;
 import org.adapters.highcharts.gwt.widgets.HighChart;
 
 import java.util.Map;
 
 import javax.inject.Inject;
 
 public class ParlamentarianView extends ViewWithUiHandlers<ParlamentarianUiHandlers> implements ParlamentarianPresenter.MyView {
 	private static ParlamentarianViewUiBinder uiBinder = GWT.create(ParlamentarianViewUiBinder.class);
 	interface ParlamentarianViewUiBinder extends UiBinder<Widget, ParlamentarianView> {}
 	private final Widget widget;
 
 	@Inject
 	private ApplicationMessages applicationMessages;
 
 	@UiField Label parlamentarianName;
 	@UiField Label parlamentarianDescription;
 	@UiField Label parlamentarianBirthDate;
 	@UiField Label parlamentarianPermanentCommissions;
 	@UiField Label parlamentarianSpecialCommissions;
 	@UiField Label parlamentarianParty;
 	@UiField Image parlamentarianImage;
 	@UiField Anchor interestDeclarationLink;
 	@UiField Anchor patrimonyDeclarationLink;
 	@UiField Anchor reportConflictLink;
 	@UiField FlowPanel consistencyIndexChartPanel;
 	@UiField FlowPanel perAreaChartPanel;
 	@UiField HTMLPanel societyPanel;
 	@UiField HTMLPanel stockPanel;
 	@UiField Anchor parliamentarianUrlToVotainteligente;
 	@UiField HTMLPanel parlamentarianCommentPanel;
 	@UiField HTMLPanel sharePanel;
 	CellTable<Society> societyTable;
 	CellTable<Stock> stockTable;
 	CellTable<ParlamentarianComment> commentTable;
 	private ShareThis share;
 
 	public ParlamentarianView() {
 		widget = uiBinder.createAndBindUi(this);
 		DisplayCellTableResource displayResource = GWT.create(DisplayCellTableResource.class);
 		societyTable = new CellTable<Society>(15, displayResource);
 		societyPanel.add(societyTable);
 		stockTable = new CellTable<Stock>(15, displayResource);
 		stockPanel.add(stockTable);
 		commentTable = new CellTable<ParlamentarianComment>(15, displayResource);
 		parlamentarianCommentPanel.add(commentTable);
 	}
 
 	@Override
 	public Widget asWidget() {
 		return widget;
 	}
 
 	@Override
 	public void clearParlamentarianData() {
 		parlamentarianImage.setUrl("images/parlamentarian/large/avatar.png");
 		parlamentarianName.setText("");
 		parlamentarianDescription.setText("");
 		parlamentarianBirthDate.setText("");
 		parlamentarianPermanentCommissions.setText("");
 		parlamentarianSpecialCommissions.setText("");
 		parlamentarianParty.setText("");
 		interestDeclarationLink.setHref("");
 		patrimonyDeclarationLink.setHref("");
 		reportConflictLink.setHref("");
 		consistencyIndexChartPanel.clear();
 		perAreaChartPanel.clear();
 	}
 
 	@Override
 	public void setParlamentarianName(String parlamentarianName) {
 		this.parlamentarianName.setText(parlamentarianName);
 	}
 
 	@Override
 	public void setParlamentarianDescription(String parlamentarianDescription) {
 		this.parlamentarianDescription.setText(parlamentarianDescription);
 	}
 
 	@Override
 	public void setParlamentarianBirthDate(String parlamentarianBirthDate) {
 		this.parlamentarianBirthDate.setText(parlamentarianBirthDate);
 	}
 
 	@Override
 	public void setParlamentarianPermanentCommissions(String parlamentarianPermanentCommissions) {
 		this.parlamentarianPermanentCommissions.setText(parlamentarianPermanentCommissions);
 	}
 
 	@Override
 	public void setParlamentarianSpecialCommissions(String parlamentarianSpecialCommissions) {
 		this.parlamentarianSpecialCommissions.setText(parlamentarianSpecialCommissions);
 	}
 
 	@Override
 	public void setParlamentarianParty(String parlamentarianParty) {
 		this.parlamentarianParty.setText(parlamentarianParty);
 	}
 
 	@Override
 	public void setParlamentarianImage(String url) {
 		parlamentarianImage.setUrl(url);
 	}
 
 	@Override
 	public void setInterestDeclarationLink(String interestDeclarationLink) {
 		this.interestDeclarationLink.setHref(interestDeclarationLink);
 	}
 
 	@Override
 	public void setPatrimonyDeclarationLink(String patrimonyDeclarationLink) {
 		this.patrimonyDeclarationLink.setHref(patrimonyDeclarationLink);
 	}
 
 	@Override
 	public void setReportConflictLink(String href) {
 		reportConflictLink.setHref(href);
 	}
 
 	@Override
 	public CellTable<Society> getSocietyTable() {
 		return societyTable;
 	}
 
 	@Override
 	public CellTable<Stock> getStockTable() {
 		return stockTable;
 	}
 
 	@Override
 	public CellTable<ParlamentarianComment> getParlamentarianCommentTable() {
 		return commentTable;
 	}
 
 	@Override
 	public void setConsistencyChartData(Map<String, Double> chartData) {
 		try {
 			HighChart declarationChart = new HighChart();
 			declarationChart.setAutoResize(true);
 			declarationChart.setOption(new OptionPath("/title/text"), applicationMessages.getSocietyConsistencyIndex());
 			declarationChart.setOption(new OptionPath("/subtitle/text"), applicationMessages.getSocietyReportedVsUnreported());
 			declarationChart.setOption(new OptionPath("/chart/animation"), true);
 			declarationChart.setOption(new OptionPath("/chart/margin"), new Integer[] {30, 25, 0, 25});
 			declarationChart.setOption(new OptionPath("/chart/plotShadow"), false);
 			declarationChart.setOption(new OptionPath("/chart/backgroundColor"), "transparent");
 			declarationChart.setOption(new OptionPath("/credits/enabled"), false);
 			declarationChart.setOption(new OptionPath("/plotOptions/pie/animation"), true);
 			declarationChart.setOption(new OptionPath("/plotOptions/pie/allowPointSelect"), true);
 			declarationChart.setOption(new OptionPath("/plotOptions/pie/dataLabels/enabled"), true);
 			declarationChart.setOption(new OptionPath("/plotOptions/pie/dataLabels/distance"), -40);
 			declarationChart.setOption(new OptionPath("/plotOptions/pie/dataLabels/color"), "white");
 			declarationChart.setOption(new OptionPath("/plotOptions/pie/dataLabels/style/font"), "10px Trebuchet MS, Verdana, sans-serif");
 			declarationChart.setOption(new OptionPath("/plotOptions/pie/dataLabels/formatter"),
 					new RawStringType(
 						"function(e) {" +
 						"	return '<b>'+this.point.name+'</b><br/>'+this.percentage.toFixed(1)+'%'" +
 						"	}"
 					)
 			);
 			declarationChart.setOption(new OptionPath("/tooltip/enabled"), false);
 
 			SeriesType series = new SeriesType("Consistencia");
 			series.setType("pie");
 
 			for (String key : chartData.keySet()) {
 				series.addEntry(new SeriesType.SeriesDataEntry(key, chartData.get(key)));
 			}
 
 			declarationChart.addSeries(series);
 			declarationChart.setSize(270, 280);
 			consistencyIndexChartPanel.add(declarationChart);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void setPerAreaChartData(Map<String, Double> categoryChartData) {
 		try {
 			HighChart perAreaChart = new HighChart();
 			perAreaChart.setAutoResize(true);
 			perAreaChart.setOption(new OptionPath("/title/text"), applicationMessages.getParlamentarianDistributionPerArea());
 			perAreaChart.setOption(new OptionPath("/subtitle/text"), applicationMessages.getParlamentarianAccordingToAreasOfSocietiesOrStocks());
 			perAreaChart.setOption(new OptionPath("/chart/animation"), true);
 			perAreaChart.setOption(new OptionPath("/chart/margin"), new Integer[] {30, 55, 0, 55});
 			perAreaChart.setOption(new OptionPath("/chart/plotShadow"), false);
 			perAreaChart.setOption(new OptionPath("/chart/backgroundColor"), "transparent");
 			perAreaChart.setOption(new OptionPath("/credits/enabled"), false);
 			perAreaChart.setOption(new OptionPath("/plotOptions/pie/animation"), true);
 			perAreaChart.setOption(new OptionPath("/plotOptions/pie/allowPointSelect"), true);
 			perAreaChart.setOption(new OptionPath("/plotOptions/pie/dataLabels/enabled"), false);
 			perAreaChart.setOption(new OptionPath("/tooltip/enabled"), true);
 			perAreaChart.setOption(new OptionPath("/tooltip/formatter"),
 					new RawStringType(
 						"function(e) {" +
 						"	return '<b>'+this.point.name+':</b><br/>'+this.percentage.toFixed(1)+'%'" +
 						"	}"
 					)
 			);
 
 			SeriesType series = new SeriesType("Por area");
 			series.setType("pie");
 
 			for (String key : categoryChartData.keySet()) {
 				series.addEntry(new SeriesType.SeriesDataEntry(key, categoryChartData.get(key)));
 			}
 
 			perAreaChart.addSeries(series);
 			perAreaChart.setSize(330, 280);
 			perAreaChartPanel.add(perAreaChart);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void setparliamentarianUrlToVotainteligente(String hrefToVotainteligente, String messageToVotainteligente) {
 		parliamentarianUrlToVotainteligente.setText(messageToVotainteligente);
 		parliamentarianUrlToVotainteligente.setHref(hrefToVotainteligente);
 	}
 
 	@Override
 	public void setShare(String href, String parliamentarianName) {
		sharePanel.clear();
 		share = new ShareThis();
 		share.setHref(href);
 		// TODO: define social network messages
 		share.setTitle(applicationMessages.getGeneralAppName());
 		share.setMessage(applicationMessages.getParlamentarianLookAtInterestsOf() + " " + parliamentarianName);
 		share.setup();
 		sharePanel.add(share);
 	}
 
 	@UiHandler("interestDeclarationLink")
 	public void onInterestDeclarationLinkClick(ClickEvent event) {
 		if (getUiHandlers().getInterestDeclaration() == false) {
 			event.preventDefault();
 			getUiHandlers().showNotification(applicationMessages.getParlamentarianNoInterestDeclarationFile(), NotificationEventType.NOTICE);
 		}
 	}
 
 	@UiHandler("patrimonyDeclarationLink")
 	public void onPatrimonyDeclarationLinkClick(ClickEvent event) {
 		if (getUiHandlers().getPatrimonyDeclaration() == false) {
 			event.preventDefault();
 			getUiHandlers().showNotification(applicationMessages.getParlamentarianNoPatrimonyDeclarationFile(), NotificationEventType.NOTICE);
 		}
 	}
 }
