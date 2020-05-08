 package n3phele.service.model;
 
 import java.util.Date;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlType;
 
 import n3phele.service.core.NotFoundException;
 import n3phele.service.model.core.Entity;
 import n3phele.service.model.core.Helpers;
 import n3phele.service.rest.impl.NarrativeResource;
 
 @XmlRootElement(name="CloudProcessSummary")
 @XmlType(name="CloudProcess", propOrder={"state", "narrative", "costPerHour", "epoch", "start", "complete"})
 public class CloudProcessSummary extends Entity {
 	private ActionState state = ActionState.NEWBORN;
 	private Narrative[] narrative;
 	private double costPerHour;
 	private Date epoch;
 	private Date start;
 	private Date complete;
 	
 	public CloudProcessSummary() {}
 	
 	public CloudProcessSummary(CloudProcess full) {
 		this.name = full.getName();
 		this.uri = Helpers.URItoString(full.getUri());
 		this.isPublic = full.getPublic();
 		this.owner = full.getOwner().toString();
 		this.state = full.getState();
 		this.costPerHour = full.getCostPerHour();
		this.start = full.getEpoch();
 		this.start = full.getStart();
 		this.complete = full.getComplete();
 		
 		try {
 			this.narrative = new Narrative[] {NarrativeResource.dao.getLastNarrative(full.getUri()) };
 		} catch (NotFoundException e) {
 			this.narrative = new Narrative[0];
 		}
 	}
 
 	/**
 	 * @return the state
 	 */
 	public ActionState getState() {
 		return state;
 	}
 
 	/**
 	 * @param state the state to set
 	 */
 	public void setState(ActionState state) {
 		this.state = state;
 	}
 
 	/**
 	 * @return the narrative
 	 */
 	public Narrative[] getNarrative() {
 		return narrative;
 	}
 
 	/**
 	 * @param narrative the narrative to set
 	 */
 	public void setNarrative(Narrative[] narrative) {
 		this.narrative = narrative;
 	}
 
 	public double getCostPerHour() {
 		return this.costPerHour;
 	}
 
 	public void setCostPerHour(double costPerHour) {
 		this.costPerHour = costPerHour;
 	}
 
 	public Date getStart() {
 		return this.start;
 	}
 
 	public void setStart(Date start) {
 		this.start = start;
 	}
 
 	public Date getComplete() {
 		return this.complete;
 	}
 
 	public void setComplete(Date complete) {
 		this.complete = complete;
 	}
 
 	public Date getEpoch() {
 		return this.epoch;
 	}
 
 	public void setEpoch(Date epoch) {
 		this.epoch = epoch;
 	}
 	
 }
