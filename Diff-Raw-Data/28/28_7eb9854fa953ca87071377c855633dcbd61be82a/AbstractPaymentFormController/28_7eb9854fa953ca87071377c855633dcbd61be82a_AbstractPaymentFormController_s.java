 /*
  * Copyright (c) 2009. Orange Leap Inc. Active Constituent
  * Relationship Management Platform.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.orangeleap.tangerine.controller.gift;
 
 import com.orangeleap.tangerine.controller.NewTangerineConstituentAttributesFormController;
 import com.orangeleap.tangerine.controller.TangerineForm;
 import com.orangeleap.tangerine.domain.paymentInfo.AbstractPaymentInfoEntity;
 import com.orangeleap.tangerine.util.StringConstants;
 import org.apache.commons.lang.math.NumberUtils;
 import org.springframework.beans.MutablePropertyValues;
 import org.springframework.web.bind.ServletRequestDataBinder;
 
 import javax.servlet.http.HttpServletRequest;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.TreeMap;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * User: alexlo
  * Date: Jul 20, 2009
  * Time: 10:57:28 AM
  */
 public abstract class AbstractPaymentFormController extends NewTangerineConstituentAttributesFormController {
 
 	@Override
 	@SuppressWarnings("unchecked")
 	protected void convertFormToDomain(HttpServletRequest request, TangerineForm form, Map<String, Object> paramMap) throws Exception {
 		Map<String, Object> distributionLinesMap = new TreeMap<String, Object>();
 
 		Iterator<Map.Entry<String, Object>> paramEntry = paramMap.entrySet().iterator();
 		while (paramEntry.hasNext()) {
 			Map.Entry<String, Object> entry = paramEntry.next();
 
			if (entry.getKey().indexOf(StringConstants.DISTRIBUTION_LINES) > -1) {
 				distributionLinesMap.put(entry.getKey(), request.getParameter(entry.getKey()));
 				paramEntry.remove();
 			}
 		}
 		/* Handle everything else that is not a distribution line */
 		super.convertFormToDomain(request, form, paramMap);
 
 		if (form.getDomainObject() instanceof AbstractPaymentInfoEntity) {
 			int newIndex = -1;
 			int oldIndex = -1;
 			MutablePropertyValues propertyValues = new MutablePropertyValues();
 
 			ServletRequestDataBinder binder = new ServletRequestDataBinder(form.getDomainObject());
 			initBinder(request, binder);
 
 			/*
 			 * Distro line indexes will be in ascending order but could skip numbers (i.e, 1, 3, 9, 15).
 			 * We need to 'normalize' the indexes to 0, 1, 2, 3 in the example above
 			 */
 			for (Map.Entry<String, Object> distroLineEntry : distributionLinesMap.entrySet()) {
 				int distroLineIndex = findDistroLineIndex(distroLineEntry.getKey());
 
 				/* Create the distribution line only if an amount is specified */
 				boolean hasAmount = false;
 
 				String distroLineAmtKey = new StringBuilder(StringConstants.DISTRIBUTION_LINES).append(TangerineForm.TANG_START_BRACKET).
 						append(distroLineIndex).append(TangerineForm.TANG_END_BRACKET).append(TangerineForm.TANG_DOT).append("amount").toString();
 				String amountString = request.getParameter(distroLineAmtKey);
 
 				if (NumberUtils.isNumber(amountString)) {
 					hasAmount = true;
 				}
 				
 				if (distroLineIndex != oldIndex) {
 					oldIndex = distroLineIndex;
 					if (hasAmount) {
 						newIndex++;
 					}
 				}
 				if (hasAmount) {
 					String newDistroLineKey = distroLineEntry.getKey().replaceFirst(TangerineForm.TANG_START_BRACKET + "(\\d+)" + TangerineForm.TANG_END_BRACKET,
 							TangerineForm.TANG_START_BRACKET + newIndex + TangerineForm.TANG_END_BRACKET);
 
 					/* Add the re-indexed property back to the form in case an error occurs and we need to re-display */
 					form.addField(newDistroLineKey, distroLineEntry.getValue());
 
 					propertyValues.addPropertyValue(TangerineForm.unescapeFieldName(newDistroLineKey), distroLineEntry.getValue());
 				}
 			}
 
 			/* Now create the DistributionLine objects to bind to */
 			int arraySize = ++newIndex;
 			if (arraySize > 0) {
 				AbstractPaymentInfoEntity entity = (AbstractPaymentInfoEntity) form.getDomainObject();
 				entity.clearDistributionLines();
 				entity.addDistributionLines(arraySize, getConstituent(request));
 			}
 
 			/* Now, bind to the distribution lines */
 			binder.bind(propertyValues);
 		}
 	}
 
 	protected int findDistroLineIndex(String fieldName) {
		Matcher matcher = Pattern.compile("^_{0,1}" + StringConstants.DISTRIBUTION_LINES + TangerineForm.TANG_START_BRACKET + "(\\d+)" + TangerineForm.TANG_END_BRACKET + ".+$").matcher(fieldName);
 		int start = 0;
 		String s = null;
 		if (matcher != null) {
 		    while (matcher.find(start)) {
 		        s = matcher.group(1);
 		        start = matcher.end();
 		    }
 		}
 		return Integer.parseInt(s);
 	}
 }
