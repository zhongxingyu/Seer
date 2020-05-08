 /*
 *	http://www.jrecruiter.org
 *
 *	Disclaimer of Warranty.
 *
 *	Unless required by applicable law or agreed to in writing, Licensor provides
 *	the Work (and each Contributor provides its Contributions) on an "AS IS" BASIS,
 *	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied,
 *	including, without limitation, any warranties or conditions of TITLE,
 *	NON-INFRINGEMENT, MERCHANTABILITY, or FITNESS FOR A PARTICULAR PURPOSE. You are
 *	solely responsible for determining the appropriateness of using or
 *	redistributing the Work and assume any risks associated with Your exercise of
 *	permissions under this License.
 *
 */
 package org.jrecruiter.web.actions.admin;
 
 import java.util.EnumSet;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
import org.jrecruiter.common.ApiKeysHolder;
 import org.jrecruiter.common.Constants.OfferedBy;
 import org.jrecruiter.model.Industry;
 import org.jrecruiter.model.Region;
 import org.jrecruiter.web.JobForm;
 import org.jrecruiter.web.actions.BaseAction;
 
 import com.opensymphony.xwork2.ModelDriven;
 import com.opensymphony.xwork2.Preparable;
 
 /**
  * Edit a job. Save the changes or delete the job posting altogether.
  *
  * @author Gunnar Hillert
  * @version $Id$
  *
  */
 public abstract class JobBaseAction extends BaseAction implements Preparable, ModelDriven<JobForm> {
 
     /** serialVersionUID. */
     private static final long serialVersionUID = 5109535527147122330L;
 

    private ApiKeysHolder apiKeysHolder;

     protected JobForm model = new JobForm();
 
     private Set<OfferedBy>offeredBySet;
     private List<Region> regions;
     private List<Industry>industries;
     private Map<Boolean, String>yesNoList;
 
     protected Long id;
 
     /** Prepare the select boxes of the form. */
     public void prepare() throws Exception {
 
         this.offeredBySet = EnumSet.allOf(OfferedBy.class);
         this.offeredBySet.remove(OfferedBy.UNDEFINED);
 
         this.regions = jobService.getRegions();
         this.industries = jobService.getIndustries();
         this.yesNoList = new HashMap<Boolean, String>();
         this.yesNoList.put(Boolean.FALSE, "False");
         this.yesNoList.put(Boolean.TRUE, "True");
 
     }
 
     //~~~~~Getters and Setters~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     public JobForm getModel() {
         return model;
     }
 
     public void setModel(JobForm job) {
         this.model = job;
     }
 
     public Set<OfferedBy> getOfferedBySet() {
         return offeredBySet;
     }
 
     public void setOfferedBySet(Set<OfferedBy> offeredBySet) {
         this.offeredBySet = offeredBySet;
     }
 
     public List<Region> getRegions() {
         return regions;
     }
 
     public void setRegions(List<Region> regions) {
         this.regions = regions;
     }
 
     public List<Industry> getIndustries() {
         return industries;
     }
 
     public void setIndustries(List<Industry> industries) {
         this.industries = industries;
     }
 
     public Map<Boolean, String> getYesNoList() {
         return yesNoList;
     }
 
     public void setYesNoList(Map<Boolean, String> yesNoList) {
         this.yesNoList = yesNoList;
     }
 
    /**
     * @return the apiKeysHolder
     */
    public ApiKeysHolder getApiKeysHolder() {
        return apiKeysHolder;
    }

    /**
     * @param apiKeysHolder the apiKeysHolder to set
     */
    public void setApiKeysHolder(ApiKeysHolder apiKeysHolder) {
        this.apiKeysHolder = apiKeysHolder;
    }

 }
