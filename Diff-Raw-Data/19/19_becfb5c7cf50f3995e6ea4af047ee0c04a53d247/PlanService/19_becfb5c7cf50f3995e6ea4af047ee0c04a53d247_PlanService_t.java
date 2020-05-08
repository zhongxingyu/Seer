 package no.fll.plan;
 
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.orm.hibernate3.HibernateTemplate;
 
 import no.fll.web.JsonReaderResponse;
 
 public class PlanService {
 
 	@Autowired
 	private PlanFactory planFactory;
 	@Autowired
 	private HibernateTemplate hibernateTemplate;
 	
 	public JsonReaderResponse<Plan> getPlan() {
 		return new JsonReaderResponse(hibernateTemplate.loadAll(Plan.class));
 	}
 	
 	public JsonReaderResponse<Plan> createPlan(List<Plan> plan) {
 		hibernateTemplate.saveOrUpdateAll(plan);
 		return new JsonReaderResponse(plan);
 	}
 
 	public JsonReaderResponse<Plan> updatePlan(List<Plan> oldPlan, List<Plan> newPlan) {
 		return createPlan(newPlan);
 	}
 
 	public JsonReaderResponse<Plan> deletePlan(List<Plan> plan) {
 		hibernateTemplate.deleteAll(plan);
 		return new JsonReaderResponse(plan);
 	}
 
	public JsonReaderResponse<Plan> generatePlan(String startTime, String endTime, int pitTime) {
 		List<Plan> plan = planFactory.generatePlan(startTime, endTime, pitTime);
		if (plan != null)
			return new JsonReaderResponse(plan);
		return new JsonReaderResponse();
 	}
 
 }
