 package dto;
 
 import models.JCustomer;
 import models.JMission;
 import org.bson.types.ObjectId;
 import org.codehaus.jackson.map.annotate.JsonSerialize;
 import utils.serializer.ObjectIdSerializer;
 
 /**
  * @author f.patin
  */
 public abstract class AbstractMissionDTO {
 
 	@JsonSerialize(using = ObjectIdSerializer.class)
 	public ObjectId id;
 	public String code;
 	public String label;
 	public String customerName;
 
 	@SuppressWarnings({"unused"})
 	public AbstractMissionDTO() {
 	}
 
 	public AbstractMissionDTO(final JMission mission) {
 		this.id = mission.id;
 		this.code = mission.code;
 		this.label = mission.label;
		if(mission.customerId != null) {
			this.customerName = JCustomer.byId(mission.customerId).name;
		}
 	}
 
 }
