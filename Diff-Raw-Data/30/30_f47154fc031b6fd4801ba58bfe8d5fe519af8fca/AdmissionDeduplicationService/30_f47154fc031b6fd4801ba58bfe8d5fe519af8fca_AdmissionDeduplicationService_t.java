 package cz.cvut.fit.mi_mpr_dip.admission.service.deduplication;
 
 import java.util.Set;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import cz.cvut.fit.mi_mpr_dip.admission.domain.Admission;
 import cz.cvut.fit.mi_mpr_dip.admission.domain.personal.Person;
 import cz.cvut.fit.mi_mpr_dip.admission.service.deduplication.person.PersonDeduplicationTemplate;
 
 @Service
 public class AdmissionDeduplicationService implements DeduplicationService<Admission> {
 
 	@Autowired
 	private Set<AdmissionDeduplicationTemplate> admissionDeduplicationTemplates;
 
 	@Autowired
 	private Set<PersonDeduplicationTemplate> personDeduplicationTemplates;
 
 	@Transactional
	@Override
 	public void deduplicateAndStore(Admission admission) {
 		deduplicate(admission);
 		deduplicate(admission.getPerson());
 
 		admission.persist();
 	}
 
 	private void deduplicate(Admission admission) {
 		for (AdmissionDeduplicationTemplate deduplicationTemplate : admissionDeduplicationTemplates) {
 			deduplicationTemplate.deduplicate(admission);
 		}
 	}
 
 	private void deduplicate(Person person) {
 		for (PersonDeduplicationTemplate deduplicationTemplate : personDeduplicationTemplates) {
 			deduplicationTemplate.deduplicate(person);
 		}
 	}
 }
