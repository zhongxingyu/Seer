 package com.orange.groupbuy.web.client.component;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.dom.client.AnchorElement;
 import com.google.gwt.dom.client.DivElement;
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.LIElement;
 import com.google.gwt.dom.client.SpanElement;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiTemplate;
 import com.google.gwt.user.client.ui.Widget;
 import com.orange.groupbuy.web.client.model.SearchResult;
 import com.orange.groupbuy.web.client.util.DateUtil;
 
 public class GroupBuyWidget extends Widget {
 
 	private static final String GROUP_BUY_MSG = "咕噜团购推荐：";
 
 	// private static final String TARGET_BLANK = "_blank";
 
 	private static GroupBuyWidgetUiBinder uiBinder = GWT
 			.create(GroupBuyWidgetUiBinder.class);
 
 	@UiTemplate("GroupBuyWidget_Yipit.ui.xml")
 	interface GroupBuyWidgetUiBinder extends
 			UiBinder<DivElement, GroupBuyWidget> {
 	}
 
 	@UiField
 	AnchorElement siteNameLabel;
 
 	@UiField
 	AnchorElement link;
 
 	@UiField
 	AnchorElement imageAnchor;
 
 	@UiField
 	ImageElement image;
 
 	@UiField
 	Element originalPriceLabel;
 
 	@UiField
 	SpanElement rebateLabel;
 
 	@UiField
 	Element boughtNumLabel;
 
 	@UiField
	LIElement valueLabel;
 
 	@UiField
 	AnchorElement detailsButton;
 
 	@UiField
 	SpanElement startDateLabel;
 
 	@UiField
 	SpanElement endDateLabel;
 
 	@UiField
 	AnchorElement shareToSina;
 
 	@UiField
 	AnchorElement shareToKaixin;
 
 	@UiField
 	AnchorElement shareToTencent;
 
 	public GroupBuyWidget() {
 		setElement(uiBinder.createAndBindUi(this));
 	}
 
 	public void updateModel(SearchResult result) {
 		if (result != null) {
 			siteNameLabel.setHref(result.getSiteUrl());
 			siteNameLabel.setInnerText(result.getSiteName());
 			//
 			link.setTitle(result.getDesctiption());
 			link.setHref(result.getProductUrl());
 			link.setInnerHTML(result.getDesctiption());
 			//
 			imageAnchor.setHref(result.getProductUrl());
 			imageAnchor.setTitle(result.getDesctiption());
 			image.setSrc(result.getImageUrl());
 
 			originalPriceLabel.setInnerText(formatMoney(result.getValue()));
 			rebateLabel.setInnerText(formatRebate(result.getRebate()));
 			//
 			boughtNumLabel.setInnerText(formatNumber(result.getBought()));
			// valueLabel.setInnerHTML(formatMoney(result.getPrice())
			// + valueLabel.getInnerHTML());
 
 			detailsButton.setHref(result.getProductUrl());
 			detailsButton.setTitle(result.getDesctiption());
 
 			startDateLabel.setInnerText(DateUtil.formatReadableDate(result
 					.getStartDate()));
 			endDateLabel.setInnerText(DateUtil.formatReadableDate(result
 					.getEndDate()));
 			//
 			shareToSina.setHref(getSinaUrl(result));
 			shareToKaixin.setHref(getKaixinUrl(result));
 			shareToTencent.setHref(getTencentUrl(result));

			valueLabel.setInnerHTML(formatMoney(result.getPrice())
					+ valueLabel.getInnerHTML());
 		}
 	}
 
 	private String getTencentUrl(SearchResult result) {
 		// TODO:
 		String url = "http://v.t.qq.com/share/share.php?url="
 				+ result.getProductUrl() + "&site=" + result.getSiteUrl()
 				+ "&pic=" + result.getImageUrl() + "&title=" + GROUP_BUY_MSG
 				+ result.getDesctiption() + "&appkey=";
 		return url;
 	}
 
 	private String getSinaUrl(SearchResult result) {
 		String sinaUrl = "http://v.t.sina.com.cn/share/share.php?url="
 				+ result.getProductUrl() + "&title=" + GROUP_BUY_MSG
 				+ result.getDesctiption() + "&content=utf-8";
 		return sinaUrl;
 	}
 
 	private String getKaixinUrl(SearchResult result) {
 		String sinaUrl = "http://www.kaixin001.com/repaste/share.php?rurl="
 				+ result.getProductUrl() + "&rtitle=咕噜团购推荐" + "&rcontent="
 				+ result.getDesctiption() + " " + result.getProductUrl();
 		return sinaUrl;
 	}
 
 	private String formatNumber(Integer result) {
 		return String.valueOf(result);
 	}
 
 	private String formatRebate(Double result) {
 		return String.valueOf(result) + "折";
 	}
 
 	private String formatMoney(Double result) {
 		return "￥" + String.valueOf(result);
 	}
 }
