 package com.chinarewards.qqgbvpn.main.logic.qqadidas.impl;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 
 import com.chinarewards.qqgbvpn.main.logic.qqadidas.QQAdRespScreenDisplayGen;
 import com.chinarewards.qqgbvpn.main.qqadidas.vo.GiftScreenDisplayGenModel;
 import com.chinarewards.qqgbvpn.main.qqadidas.vo.PrivilegeScreenDisplayGenModel;
 import com.chinarewards.qqgbvpn.main.qqadidas.vo.ScreenDisplay;
 
 public class QQAdRespScreenDisplayGenImpl implements QQAdRespScreenDisplayGen {
 
	public static final String dateFormatStr = "yy-MM-dd HH:mm";
 
 	@Override
 	public ScreenDisplay genGiftRespScreenDisplay(
 			GiftScreenDisplayGenModel genModel) {
 
 		StringBuffer content = new StringBuffer();
 		DateFormat dateFormat = new SimpleDateFormat(dateFormatStr);
 
 		if (QQAdConstant.GIFT_FAIL_INVALID_MEMBER == genModel.getReturnCode()) {
			content.append(genModel.getMemberKey()).append("无效");
 		} else if (QQAdConstant.GIFT_FAIL_OBTAINED_ALREADY == genModel
 				.getReturnCode()) {
 			content.append(genModel.getMemberKey()).append("在")
 					.append(dateFormat.format(genModel.getLastObtainedTime()))
					.append("已经领取了礼品");
 		}
 
 		return new ScreenDisplay(content.toString());
 	}
 
 	@Override
 	public ScreenDisplay genPrivilegeRespScreenDisplay(
 			PrivilegeScreenDisplayGenModel genModel) {
 		StringBuffer content = new StringBuffer();
 		if (QQAdConstant.PRIVILEGE_FAIL_CONSUME_NOT_ENOUGH == genModel
 				.getReturnCode()) {
 			content.append(genModel.getMemberKey()).append("消费")
					.append(genModel.getConsumeAmt()).append("元,未能获得优惠");
 		} else if (QQAdConstant.PRIVILEGE_FAIL_INVALD_MEMBER == genModel
 				.getReturnCode()) {
			content.append(genModel.getMemberKey()).append("无效");
 		} else if (QQAdConstant.PRIVILEGE_FAIL_OBTAINED_ALL_ALREADY == genModel
 				.getReturnCode()) {
			content.append(genModel.getMemberKey()).append("优惠已全部使用");
 		}
 
 		return new ScreenDisplay(content.toString());
 	}
 
 }
