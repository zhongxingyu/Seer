 /*
  *  (c) 2012 University of Bolton
  *  
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package uk.ac.bolton.spaws.model.impl;
 
 import java.util.Date;
 
 import com.navnorth.learningregistry.LRActivity;
 
 import uk.ac.bolton.spaws.model.IActor;
 import uk.ac.bolton.spaws.model.IParadata;
 import uk.ac.bolton.spaws.model.ISubmission;
 import uk.ac.bolton.spaws.model.ISubmitter;
 
 public class Submission implements ISubmission {
 
 	private ISubmitter submitter;
 	private String resourceURL;
 	private IParadata action;
 	private IActor actor;
 	private Date updated;
 	
 	public Submission(){
 	}
 	
 	public Submission(Actor actor, IParadata action, String resourceUrl){
 		setActor(actor);
 		setAction(action);
 		setResourceURL(resourceUrl);
		setSubmitter(new Submitter());
 	}
 	
 	public Submission(ISubmitter submitter, Actor actor, IParadata action, String resourceUrl){
 		setActor(actor);
 		setAction(action);
 		setResourceURL(resourceUrl);
 		setSubmitter(submitter);
 	}
 	
 	public Submission(ISubmitter submitter, Actor actor, IParadata action, String resourceUrl, Date updated){
 		setActor(actor);
 		setAction(action);
 		setResourceURL(resourceUrl);
 		setSubmitter(submitter);
 		setUpdated(updated);
 	}
 
 	/* (non-Javadoc)
 	 * @see uk.ac.bolton.spaws.model.impl.ISubmission#getActivity()
 	 */
 	public LRActivity getActivity(){
 		LRActivity activity = new LRActivity(getResourceURL(), getSubmitter().getSubmitter(), getSubmitter().getSubmitterType(), getSubmitter().getSubmissionTOS(), getSubmitter().getSubmissionAttribution(), getSubmitter().getSigner());
 		activity.addActor("actor", getActor().getName(), getActor().getUrl(), null);
 		activity.addVerb(getAction().getVerb(), null, null, null, null);
 		if (this.action.getContextUrl() != null){
 			activity.addContextToVerb("Widget", this.action.getContextUrl(), "Detail page");
 		}
 		activity.addContent(getAction().getContent());
 		getAction().addMeasure(activity);
 		activity.addObject(null, null, getResourceURL());
 		return activity;
 	}
 	
 	/* (non-Javadoc)
 	 * @see uk.ac.bolton.spaws.model.impl.ISubmission#getAction()
 	 */
 	public IParadata getAction() {
 		return action;
 	}
 	/* (non-Javadoc)
 	 * @see uk.ac.bolton.spaws.model.impl.ISubmission#setAction(uk.ac.bolton.spaws.model.IParadata)
 	 */
 	public void setAction(IParadata action) {
 		this.action = action;
 	}
 	/* (non-Javadoc)
 	 * @see uk.ac.bolton.spaws.model.impl.ISubmission#getActor()
 	 */
 	public IActor getActor() {
 		return actor;
 	}
 	/* (non-Javadoc)
 	 * @see uk.ac.bolton.spaws.model.impl.ISubmission#setActor(uk.ac.bolton.spaws.model.IActor)
 	 */
 	public void setActor(IActor actor) {
 		this.actor = actor;
 	}
 	/* (non-Javadoc)
 	 * @see uk.ac.bolton.spaws.model.impl.ISubmission#getResourceURL()
 	 */
 	public String getResourceURL() {
 		return resourceURL;
 	}
 	/* (non-Javadoc)
 	 * @see uk.ac.bolton.spaws.model.impl.ISubmission#setResourceURL(java.lang.String)
 	 */
 	public void setResourceURL(String resourceURL) {
 		this.resourceURL = resourceURL;
 	}
 
 	public void setSubmitter(ISubmitter submitter) {
 		this.submitter = submitter;
 	}
 
 	public ISubmitter getSubmitter() {
 		return submitter;
 	}
 	
 	/* (non-Javadoc)
 	 * @see uk.ac.bolton.spaws.model.ISubmission#getUpdated()
 	 */
 	public Date getUpdated() {
 		return updated;
 	}
 
 	/* (non-Javadoc)
 	 * @see uk.ac.bolton.spaws.model.ISubmission#setUpdated(java.util.Date)
 	 */
 	public void setUpdated(Date submitted) {
 		this.updated = submitted;
 	}
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals(Object other) {
 		
 		//
 		// We're overriding equals for submissions in that if its the same actor using the same verb for the same
 		// resource then its a duplicate.
 		//
 		if ( this.getActor().getName().equals( ((ISubmission)other).getActor().getName()) ){
 			if ( this.getAction().getVerb().equals( ((ISubmission)other).getAction().getVerb())){
 				if ( this.getResourceURL().equals( ((ISubmission)other).getResourceURL())){
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 }
