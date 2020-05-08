 /*
  jBilling - The Enterprise Open Source Billing System
  Copyright (C) 2003-2011 Enterprise jBilling Software Ltd. and Emiliano Conde
 
  This file is part of jbilling.
 
  jbilling is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
 
  jbilling is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Affero General Public License for more details.
 
  You should have received a copy of the GNU Affero General Public License
  along with jbilling.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.sapienter.jbilling.server.pricing.strategy;
 
 import com.sapienter.jbilling.common.Constants;
 import com.sapienter.jbilling.server.item.PricingField;
 import com.sapienter.jbilling.server.item.tasks.PricingResult;
 import com.sapienter.jbilling.server.order.Usage;
 import com.sapienter.jbilling.server.order.db.OrderDTO;
 import com.sapienter.jbilling.server.pricing.db.AttributeDefinition;
 import com.sapienter.jbilling.server.pricing.db.ChainPosition;
 import com.sapienter.jbilling.server.pricing.db.PriceModelDTO;
 import com.sapienter.jbilling.server.pricing.util.AttributeUtils;
 
 import org.apache.log4j.Logger;
 
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 import java.util.HashMap;
 
 /**
 * Tiered pricing strategy.
 *
 * Price set on bands of usage.
 *
 * @author Vikas Bodani
 * @since 15-03-2011
 */
 public class TieredPricingStrategy extends AbstractPricingStrategy {
 	
 	private static final Logger LOG = Logger.getLogger(TieredPricingStrategy.class);
 
    public TieredPricingStrategy() {
        setAttributeDefinitions(
                //new AttributeDefinition("", DECIMAL, true)
        );
 
        setChainPositions(
                ChainPosition.START
        );
 
        setRequiresUsage(true);
    }
 
    /**
     * Sets the price per minute the price defined against the usage band.
     * 
     * This method applies the purchased quantity to the usage and uses the aggregate 
     * to determine the right band in which the total usage falls. It uses the quantity amount equal to or lower than the 
     * band max value and multiplies with the price of the determined 
     * band to set the price for current usage. If part of the usage falls in a higher band also,
     * that part will be set at the higher band price. A weighted average is then used to 
     * determine the price
     *
     * <code>
     *      total_qty = (purchased_qty + current usage)
     *      totalCost += price (total_qty >= max band value) * max band quantity 
     *      qty1 =  Max (priceBand) 
     *      qty2 = (total_qty - Max (priceBand))
     * </code>
     *
     * @param pricingOrder target order for this pricing request (not used by this strategy)
     * @param result pricing result to apply pricing to
     * @param fields pricing fields (not used by this strategy)
     * @param planPrice the plan price to apply
     * @param quantity quantity of item being priced
     * @param usage total item usage for this billing period
     */
    public void applyTo(OrderDTO pricingOrder, PricingResult result, List<PricingField> fields,
                        PriceModelDTO planPrice, BigDecimal quantity, Usage usage) {
 
        if (usage == null || usage.getQuantity() == null)
            throw new IllegalArgumentException("Usage quantity cannot be null for TieredPricingStrategy.");
 
        //This may need to be done on 'Tiered Based on Time'
        BigDecimal total = quantity.add(usage.getQuantity());
        LOG.debug("Total: " + quantity + " + " + usage.getQuantity() + " = " + total);
        BigDecimal toRateQty= BigDecimal.ZERO;
        BigDecimal totalCost= BigDecimal.ZERO;
        /**
         * 1. if no attributes, setPrice to BigDecimal.ZERO
         * 2. if attributes, check price band for total usage
         *    getAttributes(), order by maxQty ascending
         *    
         */
        LOG.debug("planPrice.getAttributes().size(): " + planPrice.getAttributes().size());
        
        if (null == planPrice.getAttributes() || planPrice.getAttributes().size() == 0 ) {
     	   //no bands specified, default behaviour of no price
     	   result.setPrice(BigDecimal.ZERO);
     	   LOG.debug("Setting price to BigDecimal.ZERO");
        } else {
     	   
     	   Map<String, String> map = planPrice.getAttributes();
            Map<BigDecimal, BigDecimal> priceMap= new HashMap<BigDecimal, BigDecimal>();
            for (Map.Entry<String, String> entry : map.entrySet()) {
               BigDecimal tier = AttributeUtils.parseDecimal(entry.getKey());
               BigDecimal price = AttributeUtils.parseDecimal(entry.getValue());
               if (tier != null && price != null) {
                   priceMap.put(tier, price);
               }
            }     
            ArrayList<BigDecimal> maxValues= new ArrayList<BigDecimal>(priceMap.keySet());
            Collections.sort(maxValues);
            boolean priceSet= false;
            LOG.debug("Run bandMax.compareTo(" + total + ")");
            
            BigDecimal availableQty= total;
            //iterating bands with ascending order
            for (int iter=0 ; iter < maxValues.size(); iter++ ) {
         	   
         	   //BigDecimal b= (BigDecimal) maxValues.get(iter);
         	   BigDecimal currentTierQty= null;
         	   BigDecimal tierCumulativQty= (BigDecimal) maxValues.get(iter);
         	   currentTierQty= (iter == 0) ? tierCumulativQty : tierCumulativQty.subtract(((BigDecimal) maxValues.get(iter - 1 ))); 
 
         	   if (availableQty.compareTo(BigDecimal.ZERO) > 0) {
         		   //quantity to rate is either equal to total if lower than band max value, 
         		   //else it is equal to the band max value
         		   BigDecimal useQty=null;
         		   if (availableQty.compareTo(currentTierQty) < 0) {
         			   useQty= availableQty;
         			   availableQty= BigDecimal.ZERO;
         		   } else {
         			   useQty= currentTierQty;
         			   availableQty= availableQty.subtract(useQty); 
         		   }
         		   totalCost= totalCost.add(((BigDecimal)priceMap.get(tierCumulativQty)).multiply(useQty));
         		   toRateQty= toRateQty.add(useQty);
         		   LOG.debug("Use Qty: " + useQty + " Price: " + priceMap.get(tierCumulativQty));
         		   priceSet= true;
         	   }
         	   
         	   //if total usage falls under or equivalent to the max value of this band, use it
         	   /*if (!priceSet && b.compareTo(total) >= 0) {
         		   result.setPrice(priceMap.get(b));
         		   LOG.debug("Setting price to: " + priceMap.get(b));
         		   priceSet= true;
         		   break;
         	   }*/
            }
            //on any remaining quantity, if quantity was greater than max tier
            //therefore, the last tier quantity acts as 'under max or more'
           if (maxValues.size() > 0 && availableQty.compareTo(BigDecimal.ZERO) > 0) {
         	   BigDecimal extraQty=  total.subtract(toRateQty);
         	   totalCost= totalCost.add(((BigDecimal)priceMap.get(maxValues.get((maxValues.size()-1)))).multiply(extraQty));
         	   toRateQty= toRateQty.add(availableQty);
     		   LOG.debug("Extra Qty: " + extraQty + ", Available Qty: " + availableQty);
            }
            LOG.debug("totalCost: " + totalCost + " total: " + total + " toRateQty: " + toRateQty + ", and is equal to total " + (toRateQty.compareTo(total)== 0) );
            LOG.debug("priceSet: " + priceSet);
            
            if ( priceSet) {
         	   LOG.debug("result.setPrice=" + totalCost.divide(total, Constants.BIGDECIMAL_SCALE, Constants.BIGDECIMAL_ROUND));
         	   result.setPrice(totalCost.divide(total, Constants.BIGDECIMAL_SCALE, Constants.BIGDECIMAL_ROUND));
            } else {
         	   result.setPrice(BigDecimal.ZERO);
         	   LOG.debug("Setting price to BigDecimal.ZERO");
 /*               if (total.compareTo(maxValues.get((maxValues.size()-1))) > 0 ) {
             	   result.setPrice(priceMap.get( maxValues.get((maxValues.size()-1)) ));
             	   LOG.debug("Setting price to: " + result.getPrice());
                }*/
            }
        }
    }
 }
