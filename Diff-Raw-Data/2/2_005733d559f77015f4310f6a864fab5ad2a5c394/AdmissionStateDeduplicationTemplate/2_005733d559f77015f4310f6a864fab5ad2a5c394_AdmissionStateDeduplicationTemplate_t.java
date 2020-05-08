 package cz.cvut.fit.mi_mpr_dip.admission.service.deduplication;
 
 import java.util.List;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.springframework.stereotype.Service;
 
 import cz.cvut.fit.mi_mpr_dip.admission.domain.Admission;
 import cz.cvut.fit.mi_mpr_dip.admission.domain.AdmissionState;
 
 @Service
 public class AdmissionStateDeduplicationTemplate implements AdmissionDeduplicationTemplate {
 
 	@Override
 	public void deduplicate(Admission admission) {
 		AdmissionState admissionState = admission.getAdmissionState();
 		if (admissionState != null) {
 			List<AdmissionState> admissionStates = AdmissionState.findAdmissionStatesByCodeEquals(
 					admissionState.getCode()).getResultList();
 			if (CollectionUtils.isNotEmpty(admissionStates)) {
				admission.setAdmissionState(admissionStates.get(0));
 			}
 		}
 	}
 
 }
