 package no.niths.application.rest.lists;
 
 import java.util.List;
 
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 
 import no.niths.application.rest.misc.RestResource;
import no.niths.common.constants.DomainConstantNames;
import no.niths.domain.APIEvent;
 
@XmlRootElement(name = "resource")
 public class RestResourceList extends ListAdapter<RestResource> {
 
     private static final long serialVersionUID = 2794068992188408350L;
 
     @SuppressWarnings("unused")
     @XmlElement(name = "resource")
     private List<RestResource> data;
 
     @Override
     public void setData(List<RestResource> data) {
         this.data = data;
     }
 }
