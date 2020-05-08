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
 
 package com.orangeleap.tangerine.controller.gift.commitment.recurringGift;
 
 import com.orangeleap.tangerine.domain.paymentInfo.RecurringGift;
 import com.orangeleap.tangerine.util.OLLogger;
 import com.orangeleap.tangerine.util.StringConstants;
 import com.orangeleap.tangerine.controller.TangerineForm;
 import org.apache.commons.logging.Log;
 import org.springframework.validation.BindException;
 import org.springframework.web.servlet.ModelAndView;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 public class NewRecurringGiftViewController extends NewRecurringGiftFormController {
 
     /**
      * Logger for this class and subclasses
      */
     protected final Log logger = OLLogger.getLog(getClass());
 
     @Override
     protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object command, BindException formErrors) throws Exception {
 	    TangerineForm form = (TangerineForm) command;
         RecurringGift recurringGift = (RecurringGift) form.getDomainObject();
	    try {
             recurringGift = recurringGiftService.editRecurringGift(recurringGift);
	    }
	    catch (BindException domainErrors) {
		    bindDomainErrorsToForm(formErrors, domainErrors);
	    }
         return new ModelAndView(getSuccessView() + "?" + getParamId() + "=" + recurringGift.getId() + "&" + StringConstants.CONSTITUENT_ID + "=" + super.getConstituentId(request));
     }
 }
