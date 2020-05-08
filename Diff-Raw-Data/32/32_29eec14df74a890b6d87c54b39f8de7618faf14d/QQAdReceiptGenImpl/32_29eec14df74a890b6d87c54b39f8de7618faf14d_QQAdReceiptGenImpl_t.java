 package com.chinarewards.qqgbvpn.main.logic.qqadidas.impl;
 
 import java.text.DateFormat;
import java.text.DecimalFormat;
 import java.text.SimpleDateFormat;
 
 import com.chinarewards.qqgbvpn.main.logic.qqadidas.QQAdReceiptGen;
 import com.chinarewards.qqgbvpn.main.qqadidas.vo.GiftReceiptGenModel;
 import com.chinarewards.qqgbvpn.main.qqadidas.vo.PrivilegeReceiptGenModel;
 import com.chinarewards.qqgbvpn.main.qqadidas.vo.Receipt;
 import com.chinarewards.qqgbvpn.main.util.BigDecimalUtil;
 
 public class QQAdReceiptGenImpl implements QQAdReceiptGen {
 
 	public static final String dateFormatStr = "yyyy-MM-dd HH:mm:ss";
	public static final String moneyFormatStr = "###,###.##";
 
 	@Override
 	public Receipt generateGiftReceipt(GiftReceiptGenModel genModel) {
 		Receipt receipt = null;
 		if (QQAdConstant.GIFT_OK == genModel.getReturnCode()) {
 			String title = "Adidas Neo 五月新品";
 			StringBuffer content = new StringBuffer();
 			content.append(genModel.getMemberKey()).append("可以领取免费礼品一份.");
 			receipt = new Receipt(title, content.toString());
 		}
 
 		return receipt;
 	}
 
 	@Override
 	public Receipt generatePrivilegeReceipt(PrivilegeReceiptGenModel genModel) {
 		Receipt receipt = null;
		DecimalFormat decimalFormat = new DecimalFormat(moneyFormatStr);
 		if (QQAdConstant.PRIVILEGE_OK == genModel.getReturnCode()) {
 			String title = "Adidas Neo 五月新品";
 			StringBuffer content = new StringBuffer();
 			if (genModel.isExistLastTimeConsume()) {
 				DateFormat dateFormat = new SimpleDateFormat(dateFormatStr);
 
 				content.append(genModel.getMemberKey())
 						.append("在")
 						.append(dateFormat.format(genModel.getLastConsumeDate()))
 						.append("获得")
 						.append((int) genModel.getLastRebateAmt())
 						.append("元折扣优惠.本次消费")
						.append(decimalFormat.format(genModel.getConsumeAmt()))
 						.append("元,享受")
 						.append((int) genModel.getRebateAmt())
 						.append("元折扣优惠.折扣后实际支付金额为")
						.append(decimalFormat.format(BigDecimalUtil.sub(
								genModel.getConsumeAmt(),
								genModel.getRebateAmt()))).append("元.");
 			} else if (genModel.getRebateAmt() == QQAdConstant.REBATE_HALF_AMOUNT) {
 				content.append(genModel.getMemberKey())
 						.append("本次消费")
						.append(decimalFormat.format(genModel.getConsumeAmt()))
 						.append("元.")
 						.append("享受")
 						.append((int) genModel.getRebateAmt())
 						.append("元折扣优惠,折扣后实际支付金额为")
						.append(decimalFormat.format(BigDecimalUtil.sub(
								genModel.getConsumeAmt(),
								genModel.getRebateAmt())))
 						.append("元.")
 						.append("还有一次消费")
 						.append((int) QQAdConstant.CONSUME_AMOUNT_TO_REBATE_HALF_PRIVILEGE)
 						.append("-")
 						.append((int) QQAdConstant.CONSUME_AMOUNT_TO_REBATE_FULL_PRIVILEGE)
 						.append("元,折扣")
 						.append((int) (QQAdConstant.REBATE_FULL_AMOUNT - QQAdConstant.REBATE_HALF_AMOUNT))
 						.append("元的机会.");
 			} else {
 				content.append(genModel.getMemberKey())
 						.append("本次消费")
						.append(decimalFormat.format(genModel.getConsumeAmt()))
 						.append("元.享受")
 						.append((int) genModel.getRebateAmt())
 						.append("元折扣优惠,折扣后实际支付金额为")
						.append(decimalFormat.format(BigDecimalUtil.sub(
								genModel.getConsumeAmt(),
								genModel.getRebateAmt()))).append("元.");
 			}
 			receipt = new Receipt(title, content.toString());
 		}
 
 		return receipt;
 	}
 }
