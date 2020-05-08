 package ee.itcollege.i377.iseseisev3;
 
 import java.util.Date;
 
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

 import org.springframework.format.annotation.DateTimeFormat;
 
 public class Incident {
 	@DateTimeFormat(pattern="dd.MM.yyyy")
 	private Date start;
 	@DateTimeFormat(pattern="dd.MM.yyyy")
 	private Date end;
	@NotNull
	@Size(min=2)
 	private String location;
	@NotNull
	@Size(min=2)
 	private String description;
	@NotNull
	@Min(value=1)
 	private Integer involvedGuardCount;
	@NotNull
	@Size(min=2)
 	private String Status;
 	public Date getStart() {
 		return start;
 	}
 	public void setStart(Date start) {
 		this.start = start;
 	}
 	public Date getEnd() {
 		return end;
 	}
 	public void setEnd(Date end) {
 		this.end = end;
 	}
 	public String getLocation() {
 		return location;
 	}
 	public void setLocation(String location) {
 		this.location = location;
 	}
 	public String getDescription() {
 		return description;
 	}
 	public void setDescription(String description) {
 		this.description = description;
 	}
 	public Integer getInvolvedGuardCount() {
 		return involvedGuardCount;
 	}
 	public void setInvolvedGuardCount(Integer involvedGuardCount) {
 		this.involvedGuardCount = involvedGuardCount;
 	}
 	public String getStatus() {
 		return Status;
 	}
 	public void setStatus(String status) {
 		Status = status;
 	}
 
 }
