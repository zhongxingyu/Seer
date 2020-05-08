 /*
  * JBILLING CONFIDENTIAL
  * _____________________
  *
  * [2003] - [2012] Enterprise jBilling Software Ltd.
  * All Rights Reserved.
  *
  * NOTICE:  All information contained herein is, and remains
  * the property of Enterprise jBilling Software.
  * The intellectual and technical concepts contained
  * herein are proprietary to Enterprise jBilling Software
  * and are protected by trade secret or copyright law.
  * Dissemination of this information or reproduction of this material
  * is strictly forbidden.
  */
 
 package com.sapienter.jbilling.server.pricing.db;
 
import java.util.List;

import org.hibernate.Query;

 import com.sapienter.jbilling.server.util.db.AbstractDAS;
 
 /**
  * PriceModelDAS
  *
  * @author Brian Cowdery
  * @since 09/02/11
  */
 public class PriceModelDAS extends AbstractDAS<PriceModelDTO> {
 
	@SuppressWarnings("unchecked")
	public List<PriceModelDTO> findRateCardPriceModels(Integer rateCardId) {

		String hql = "select distinct pm from PriceModelDTO pm " +
				"where pm.attributes['rate_card_id'] = :rateCardId";
		Query query = getSession().createQuery(hql);
		query.setString("rateCardId", rateCardId.toString());
		return query.list();
	}

 }
