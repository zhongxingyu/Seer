 package no.niths.application.rest.lists;
 
 import java.util.List;
 
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 
 import no.niths.common.AppConstants;
import no.niths.domain.APIEvent;
 import no.niths.domain.Developer;
 
 @XmlRootElement(name = AppConstants.DEVELOPERS)
 public class DeveloperList extends ListAdapter<Developer>{
 
	/**
	 * 
	 */
 	private static final long serialVersionUID = 7176343871162986392L;
 	@XmlElement(name = "developer")
     private List<Developer> data;
 
     @Override
     public void setData(List<Developer> data) {
      this.data = data;
     }
 }
