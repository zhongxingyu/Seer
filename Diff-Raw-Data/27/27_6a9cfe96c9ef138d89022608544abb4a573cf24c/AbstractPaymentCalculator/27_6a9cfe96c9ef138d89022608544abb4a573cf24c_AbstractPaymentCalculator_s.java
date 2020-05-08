 package com.df.order.price;
 
 import java.math.BigDecimal;
 import java.util.List;
 
 import org.springframework.util.Assert;
 
 import com.df.idm.entity.User;
 import com.df.masterdata.entity.Item;
 import com.df.masterdata.entity.Promotion;
 import com.df.order.entity.Order;
 import com.df.order.entity.OrderLine;
 import com.df.order.promotion.PromotionRepository;
 
 public abstract class AbstractPaymentCalculator implements PaymentCalculator {
 
 	private PromotionRepository promotionRepository;
 
 	public AbstractPaymentCalculator(PromotionRepository promotionRepository) {
 		this.promotionRepository = promotionRepository;
 	}
 
 	@Override
 	public final PaymentContext createContext() {
 		return new PaymentContextImpl();
 	}
 
 	public PromotionRepository getPromotionRepository() {
 		return promotionRepository;
 	}
 
 	protected abstract Item getItemByItemCode(String storeCode, String itemCode);
 
 	protected abstract BigDecimal calcuateWithPromotion(Item item, Promotion promotion);
 
 	protected abstract BigDecimal calcuateWithPromotion(Order order, Promotion promotion);
 
 	private void checkPaymentContextSupport(PaymentContext context) {
 		if (!(context instanceof PaymentContextImpl)) {
 			throw new IllegalArgumentException("paymentContext must be type of " + PaymentContextImpl.class);
 		}
 	}
 
 	@Override
 	public BigDecimal calculateItemPayment(PaymentContext paymentContext) {
 		checkPaymentContextSupport(paymentContext);
 		Item item = ((PaymentContextImpl) paymentContext).getItem();
 		User user = ((PaymentContextImpl) paymentContext).getUser();
 		Assert.notNull(item);
 		List<Promotion> promotions = promotionRepository.getItemBestPromotions(user, item);
 		if (promotions != null && promotions.size() > 0) {
 			BigDecimal total = null;
 			Promotion appliedPromotion = null;
 			for (int i = 0; i < promotions.size(); i++) {
 				Promotion p = promotions.get(i);
 				if (i == 0) {
 					total = calcuateWithPromotion(item, p);
 					appliedPromotion = p;
 				} else {
 					BigDecimal otherTotal = calcuateWithPromotion(item, p);
 					if (total.compareTo(otherTotal) > 0) {
 						total = otherTotal;
 						appliedPromotion = p;
 					}
 				}
 			}
 			((PaymentContextImpl) paymentContext).setItemAppliedPromotion(appliedPromotion);
 			return total;
 		} else {
 			return new BigDecimal(item.getPrice());
 		}
 	}
 
 	@Override
 	public void calculateOrderPayment(PaymentContext paymentContext) {
 		checkPaymentContextSupport(paymentContext);
 		PaymentContextImpl context = ((PaymentContextImpl) paymentContext);
 		Order order = context.getOrder();
 		Assert.notNull(order);
 		List<OrderLine> lines = order.getLines();
 		BigDecimal total = new BigDecimal(0);
 		if (lines != null && lines.size() > 0) {
 			for (OrderLine line : lines) {
 				String itemCode = line.getItemCode();
 				Item item = this.getItemByItemCode(order.getStoreCode(), itemCode);
 				paymentContext.setItem(item);
 				BigDecimal price = this.calculateItemPayment(paymentContext);
 				Promotion promotion = paymentContext.getItemAppliedPromotion();
 				if (promotion != null) {
 					line.setPromotionName(promotion.getName());
 					line.setPromotionPrice(price.floatValue());
 				}
 				line.calcuatePayment();
				total.add(line.getTotalPaymentAfterDiscount());
 			}
 			context.setItem(null);
 			context.setItemAppliedPromotion(null);
 			List<Promotion> promotions = promotionRepository.getOrderBestPromotions(context.getUser(), order);
 			if (promotions != null && promotions.size() > 0) {
 				Promotion appliedPromotion = null;
 				for (int i = 0; i < promotions.size(); i++) {
 					Promotion p = promotions.get(i);
 					BigDecimal pTotal = calcuateWithPromotion(order, p);
 					if (total.compareTo(pTotal) > 0) {
 						total = pTotal;
 						appliedPromotion = p;
 					}
 				}
 				context.setOrderAppliedPromotion(appliedPromotion);
 			}
 			order.setTotalPaymentAfterDiscount(total);
 		}
 	}
 }
