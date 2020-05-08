 /*
  * PatientView
  *
  * Copyright (c) Worth Solutions Limited 2004-2013
  *
  * This file is part of PatientView.
  *
  * PatientView is free software: you can redistribute it and/or modify it under the terms of the
  * GNU General Public License as published by the Free Software Foundation, either version 3 of the License,
  * or (at your option) any later version.
  * PatientView is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
  * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  * You should have received a copy of the GNU General Public License along with PatientView in a file
  * titled COPYING. If not, see <http://www.gnu.org/licenses/>.
  *
  * @package PatientView
  * @link http://www.patientview.org
  * @author PatientView <info@patientview.org>
  * @copyright Copyright (c) 2004-2013, Worth Solutions Limited
  * @license http://www.gnu.org/licenses/gpl-3.0.html The GNU General Public License V3.0
  */
 
 package org.patientview.patientview.controller;
 
 import org.apache.commons.lang.StringUtils;
 import org.patientview.model.Unit;
 import org.patientview.service.PatientManager;
 import org.patientview.utils.LegacySpringUtils;
 import org.springframework.beans.support.MutableSortDefinition;
 import org.springframework.beans.support.PagedListHolder;
 import org.springframework.beans.support.SortDefinition;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import javax.servlet.http.HttpServletRequest;
 import java.util.List;
 @Controller
 public class UnitPatientsControlller extends BaseController {
 
     @RequestMapping(value = Routes.UNIT_PATIENTS_LIST_URL)
     public String getPatients(@RequestParam(value = "unitcode", required = false) String unitcode,
                               @RequestParam(value = "page", required = false) String page,
                               @RequestParam(value = "nhsno", required = false) String nhsno,
                               @RequestParam(value = "name", required = false) String name,
                               @RequestParam(value = "showgps", required = false) boolean showgps,
                               @RequestParam(value = "property", required = false) String property,
                               HttpServletRequest request) {
 
         unitcode = (unitcode == null) ? "" : unitcode;
 
        PagedListHolder pagedListHolder = (PagedListHolder) request.getSession().getAttribute("patients");
 
         //TODO So we get every patient in the database and then start paging
         //TODO There is probably not a single use case why you would want this many patients
         //TODO Pagination needs to restrict query results
 
         if (StringUtils.isEmpty(page) || pagedListHolder == null) {

            // Validation
            if (StringUtils.isEmpty(unitcode) && StringUtils.isEmpty(nhsno) && StringUtils.isEmpty(name)) {
                return "redirect:/renal/control/unitPatientsUnitSelect.do?validation=failed";
            }

             nhsno = (nhsno == null) ? "" : nhsno;
             name = (name == null) ? "" : name;
             List patients = null;
             PatientManager patientManager = LegacySpringUtils.getPatientManager();
             if (StringUtils.isEmpty(unitcode)) {
                 patients = patientManager.getAllUnitPatientsWithTreatment(nhsno, name, showgps);
             } else {
                 patients = patientManager.getUnitPatientsWithTreatment(unitcode, nhsno, name, showgps);
             }
             pagedListHolder = new PagedListHolder(patients);
             request.getSession().setAttribute("patients", pagedListHolder);
         } else {
             if ("first".equals(page)) {
                 pagedListHolder.setPage(0);
             } else if ("prev".equals(page)) {
                 pagedListHolder.previousPage();
             } else if ("next".equals(page)) {
                 pagedListHolder.nextPage();
             } else if ("last".equals(page)) {
                 pagedListHolder.setPage(pagedListHolder.getPageCount() - 1);
             } else if ("sort".equals(page)) {
                 MutableSortDefinition newSort = new MutableSortDefinition(property, true, false);
                 SortDefinition sort =  pagedListHolder.getSort();
                 if (StringUtils.equals(sort.getProperty(), property)) {
                     newSort.setAscending(!sort.isAscending());
                 }
                 pagedListHolder.setSort(newSort);
                 pagedListHolder.resort();
             }
         }
 
         if (!"".equals(unitcode)) {
             Unit unit = LegacySpringUtils.getUnitManager().get(unitcode);
             request.setAttribute("unit", unit);
         }
 
         return forwardTo(request, Routes.UNIT_PATIENTS_LIST_PAGE);
     }
 
 }
