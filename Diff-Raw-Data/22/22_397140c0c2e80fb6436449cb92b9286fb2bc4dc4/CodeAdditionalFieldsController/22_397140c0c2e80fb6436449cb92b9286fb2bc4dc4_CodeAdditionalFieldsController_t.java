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
 
 package com.orangeleap.tangerine.controller.code;
 
 import com.orangeleap.tangerine.domain.customization.PicklistItem;
 import com.orangeleap.tangerine.util.OLLogger;
import com.orangeleap.tangerine.util.StringConstants;
 import org.apache.commons.logging.Log;
 import org.springframework.util.StringUtils;
 import org.springframework.web.servlet.ModelAndView;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
import java.util.*;
 
 public class CodeAdditionalFieldsController extends CodeHelperController {
 
     /**
      * Logger for this class and subclasses
      */
     protected final Log logger = OLLogger.getLog(getClass());
 
     private String additionalFieldsView;
 
     public void setAdditionalFieldsView(String additionalFieldsView) {
         this.additionalFieldsView = additionalFieldsView;
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
         ModelAndView mv = super.handleRequestInternal(request, response);
         mv.setViewName(additionalFieldsView);
         if (isResultsOnlyView(request) != null) {
             mv.setViewName(isResultsOnlyView(request));
         }
         getSelectedCodes((List<PicklistItem>) mv.getModel().get(CODES), request, mv);
         mv.addObject("additionalCodes", getAdditionalCodes(request));
         return mv;
     }
 
     @SuppressWarnings("unchecked")
     public void getSelectedCodes(List<PicklistItem> allCodes, HttpServletRequest request, ModelAndView mv) {
         List<PicklistItem> selectedCodes = new ArrayList<PicklistItem>();
         List<PicklistItem> availableCodes = new ArrayList<PicklistItem>();
         if (allCodes != null) {
             String selectedCodesStr = request.getParameter("selectedCodes");
             if (StringUtils.hasText(selectedCodesStr)) {
                String[] chosenCodesStringsArray = StringUtils.delimitedListToStringArray(selectedCodesStr, StringConstants.CUSTOM_FIELD_SEPARATOR);
                Set<String> chosenCodesStrings = new HashSet<String>(Arrays.asList(chosenCodesStringsArray));
                 if (chosenCodesStrings != null) {
                     for (PicklistItem aCode : allCodes) {
                        if (chosenCodesStrings.contains(aCode.getDefaultDisplayValue())) {
                             selectedCodes.add(aCode);
                        }
                        else {
                             availableCodes.add(aCode);
                         }
                     }
                 }
             } else {
                 availableCodes = allCodes;
             }
         }
         mv.addObject("selectedCodes", selectedCodes);
         mv.addObject("availableCodes", availableCodes);
     }
 
     private List<String> getAdditionalCodes(HttpServletRequest request) {
         List<String> additionalCodes = new ArrayList<String>();
         String additionalCodesStr = request.getParameter("additionalCodes");
         if (StringUtils.hasText(additionalCodesStr)) {
            String[] s = StringUtils.delimitedListToStringArray(additionalCodesStr, StringConstants.CUSTOM_FIELD_SEPARATOR);
             for (String str : s) {
                 additionalCodes.add(str + "\n");
             }
         }
         return additionalCodes;
     }
 }
