 /** 
  * Copyright (c) 2011, Clinton Health Access Initiative.
  *
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *     * Redistributions of source code must retain the above copyright
  *       notice, this list of conditions and the following disclaimer.
  *     * Redistributions in binary form must reproduce the above copyright
  *       notice, this list of conditions and the following disclaimer in the
  *       documentation and/or other materials provided with the distribution.
  *     * Neither the name of the <organization> nor the
  *       names of its contributors may be used to endorse or promote products
  *       derived from this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
  * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package org.chai.kevin.dsr;
 
 /**
  * @author Jean Kahigiso M.
  *
  */
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.chai.kevin.Organisation;
 import org.chai.kevin.OrganisationService;
 import org.chai.kevin.ValueService;
 import org.chai.kevin.value.ExpressionValue;
 import org.hisp.dhis.organisationunit.OrganisationUnitGroup;
 import org.hisp.dhis.period.Period;
 import org.springframework.transaction.annotation.Transactional;
 
 public class DsrService {
 	// private Log log = LogFactory.getLog(DsrService.class);
 	private OrganisationService organisationService;
 	private ValueService valueService;
 
 	@Transactional(readOnly = true)
 	public DsrTable getDsr(Organisation organisation, DsrObjective objective,
 			Period period) {
 		List<Organisation> organisations = null;
 		Map<Organisation, Map<DsrTarget, Dsr>> dsrMap = null;
 		List<DsrTarget> targets = null;
 		List<OrganisationUnitGroup> facilityTypes = null;
 		Set<OrganisationUnitGroup> facilityTypeSet = null;
 		if (objective == null || organisation == null) {
 			return new DsrTable(organisation, organisations, period, objective,
 					targets, facilityTypes, dsrMap);
 		} else {
 			organisations = organisationService.getChildrenOfLevel(
 					organisation, organisationService.getFacilityLevel());
 			targets = objective.getTargets();
 			Collections.sort(targets, new DsrTargetSorter());
 			dsrMap = new HashMap<Organisation, Map<DsrTarget, Dsr>>();
 			facilityTypeSet = new LinkedHashSet<OrganisationUnitGroup>();
 			for (Organisation child : organisations) {
 				organisationService.loadGroup(child);
 				if (child.getOrganisationUnitGroup() != null) {
 					facilityTypeSet.add(child.getOrganisationUnitGroup());
 				}
 				Map<DsrTarget, Dsr> orgDsr = new HashMap<DsrTarget, Dsr>();
 				for (DsrTarget target : targets) {
 					ExpressionValue expressionValue = valueService.getValue(
 							target.getExpression(),
 							child.getOrganisationUnit(), period);
 					String value = null;
 					if (expressionValue != null)
 						if (expressionValue.getNumberValue() != null) {
 							value = getFormat(target,
 									expressionValue.getValue());
 						} else {
 							value = expressionValue.getValue();
 						}
 					orgDsr.put(target, new Dsr(child, period, target, value));
 				}
 				dsrMap.put(child, orgDsr);
 			}
 			facilityTypes = new ArrayList<OrganisationUnitGroup>(
 					facilityTypeSet);
 		}
 		return new DsrTable(organisation, organisations, period, objective,
 				targets, facilityTypes, dsrMap);
 	}
 
	public String getFormat(DsrTarget target, String value) {
		if (target.getFormat() != null) {
			DecimalFormat frmt = new DecimalFormat(target.getFormat());
			return frmt.format(Double.parseDouble(value)).toString();
		}
		return value;
 	}
 
 	public void setOrganisationService(OrganisationService organisationService) {
 		this.organisationService = organisationService;
 	}
 
 	public void setValueService(ValueService valueService) {
 		this.valueService = valueService;
 	}
 }
