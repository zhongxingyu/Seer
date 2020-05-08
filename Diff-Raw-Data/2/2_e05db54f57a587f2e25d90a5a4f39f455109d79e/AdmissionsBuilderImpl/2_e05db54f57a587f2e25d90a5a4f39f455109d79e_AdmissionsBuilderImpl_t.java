 package cz.cvut.fit.mi_mpr_dip.admission.builder;
 
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Required;
 
 import cz.cvut.fit.mi_mpr_dip.admission.domain.Admission;
 import cz.cvut.fit.mi_mpr_dip.admission.domain.Admissions;
 
 public class AdmissionsBuilderImpl implements AdmissionsBuilder {
 
 	private Admissions admissions;
 
 	private Integer limit;
 
 	private Integer maxLimit;
 
 	private Integer offset = new Integer(0);
 
 	@Override
 	public void createNew() {
 		admissions = new Admissions();
 	}
 
 	@Override
 	public Admissions get() {
 		return admissions;
 	}
 
 	@Override
 	public void buildLimit(Integer count, Integer page) {
 		assignLimit(count);
 		assignOffset(page);
 	}
 
 	private void assignLimit(Integer limit) {
 		if (limit != null && limit <= maxLimit) {
 			setLimit(limit);
 		}
 	}
 
 	private void assignOffset(Integer page) {
		if (page != null && page > 1) {
 			offset = (page - 1) * limit;
 		}
 	}
 
 	@Override
 	public void buildAdmissions() {
 		admissions.setAdmissions(Admission.findAdmissionEntries(offset, limit));
 	}
 
 	@Override
 	public void buildAdmissions(List<Admission> admissions) {
 		this.admissions.setAdmissions(admissions);
 	}
 
 	@Required
 	public void setLimit(Integer limit) {
 		this.limit = limit;
 	}
 
 	@Required
 	public void setMaxLimit(Integer maxLimit) {
 		this.maxLimit = maxLimit;
 	}
 
 }
