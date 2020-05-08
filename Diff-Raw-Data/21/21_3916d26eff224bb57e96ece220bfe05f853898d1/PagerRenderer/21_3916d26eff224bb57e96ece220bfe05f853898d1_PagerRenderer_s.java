 /**********************************************************************************
  * $URL$
  * $Id$
  ***********************************************************************************
  *
  * Copyright (c) 2007, 2008 The Sakai Foundation.
  *
  * Licensed under the Educational Community License, Version 1.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.opensource.org/licenses/ecl1.php
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  **********************************************************************************/
 
 package org.sakaiproject.assignment2.tool.producers.renderers;
 
 import org.sakaiproject.assignment2.tool.params.PagerViewParams;
 import org.sakaiproject.assignment2.tool.params.AssignmentListSortViewParams;
 import org.sakaiproject.assignment2.tool.producers.ListProducer;
 
 import uk.org.ponder.messageutil.MessageLocator;
 import uk.org.ponder.rsf.components.UIBoundList;
 import uk.org.ponder.rsf.components.UIContainer;
 import uk.org.ponder.rsf.components.UIForm;
 import uk.org.ponder.rsf.components.UIInput;
 import uk.org.ponder.rsf.components.UIJointContainer;
 import uk.org.ponder.rsf.components.UIMessage;
 import uk.org.ponder.rsf.components.UISelect;
 import uk.org.ponder.rsf.components.decorators.DecoratorList;
 import uk.org.ponder.rsf.components.decorators.UIFreeAttributeDecorator;
 import uk.org.ponder.rsf.viewstate.ViewParameters;
 import java.util.HashMap;
 import java.util.Map;
 
 public class PagerRenderer {
 
 	private MessageLocator messageLocator;
 	
 	public static final int DEFAULT_START_COUNT = 50;
 	
 	public Integer currentStart = 0;
 	public Integer currentCount = DEFAULT_START_COUNT;		//actually set in the pager view parameters :-)
 	public Integer totalCount = 16;
 	
 	@SuppressWarnings("unchecked")
 	public void makePager(UIContainer tofill, String divID, String currentViewID, ViewParameters viewparams, Integer totalCount) {
     	PagerViewParams pagerparams = (PagerViewParams) viewparams;
     	
     	//set vars
     	this.currentCount = pagerparams.current_count;
     	this.currentStart = pagerparams.current_start;
     	this.totalCount = totalCount;
     	
         UIJointContainer joint = new UIJointContainer(tofill, divID, "pagerDivContainer:", ""+1);
         
         //Currently Viewing
         UIMessage.make(joint, "pager_viewing", "assignment2.pager.viewing", 
         		new Object[] {this.getViewingStart(), this.getViewingEnd(), this.getViewingTotal()} );
         UIMessage.make(joint, "pager_viewing_format", "assignment2.pager.viewing");
         
         //Form
         UIForm form = UIForm.make(joint, "pager_form");
                 
 		//Paging Buttons
 		////////////////////
 		String href_params = "";
 		//If we are on the Assignment_list-sortview page... add in the view params to keep the sorting accurate
 		if (ListProducer.VIEW_ID.equals(currentViewID)){
 			AssignmentListSortViewParams sortparams = (AssignmentListSortViewParams) viewparams;
 			href_params = "sort_dir=" + sortparams.sort_dir + "&sort_by=" + sortparams.sort_by + "&";
 		}		
 
 		//Select Box
         UISelect select_box = UISelect.make(form, "pager_select_box");
         UIInput selection = new UIInput();
         selection.setValue(this.getCurrentSelect());
         select_box.selection = selection;
         UIBoundList comboValues = new UIBoundList();
         comboValues.setValue(new String[] {"5","10","20","50","100","200"});
         select_box.optionlist = comboValues;
 		UIBoundList comboNames = new UIBoundList();
 		comboNames.setValue(new String[] {"Show 5 items", "Show 10 items", "Show 20 items", "Show 50 items", "Show 100 items", "Show 200 items"});
 		select_box.optionnames = comboNames;
 		Map attrmap = new HashMap(); 
		attrmap.put("onchange", "changePage(pStart);");
 		select_box.decorators = new DecoratorList(new UIFreeAttributeDecorator(attrmap)); 
 		
 		
 		//first page
 		UIInput first_page = UIInput.make(form, "pager_first_page", null, messageLocator.getMessage("assignment2.pager.pager_first_page"));
 		attrmap = new HashMap();
		attrmap.put("onclick", "changePage('first')");
 		first_page.decorators = new DecoratorList(new UIFreeAttributeDecorator(attrmap));
 				
 		//previous page
 		UIInput prev_page = UIInput.make(form, "pager_prev_page", null, messageLocator.getMessage("assignment2.pager.pager_prev_page"));
 		attrmap = new HashMap();
		attrmap.put("onclick", "changePage('prev')");
 		prev_page.decorators = new DecoratorList(new UIFreeAttributeDecorator(attrmap));
 
 		//next page
 		UIInput next_page = UIInput.make(form, "pager_next_page", null, messageLocator.getMessage("assignment2.pager.pager_next_page"));
 		attrmap = new HashMap();
		attrmap.put("onclick", "changePage('next')");
 		next_page.decorators = new DecoratorList(new UIFreeAttributeDecorator(attrmap));
 		
 		//last button
 		UIInput last_page = UIInput.make(form, "pager_last_page", null, messageLocator.getMessage("assignment2.pager.pager_last_page"));
 		attrmap = new HashMap();
		attrmap.put("onclick", "changePage('last')");
 		last_page.decorators = new DecoratorList(new UIFreeAttributeDecorator(attrmap));
 
     }
     
     public void setMessageLocator(MessageLocator messageLocator) {
         this.messageLocator = messageLocator;
     }
     
     //Private Methods
 	private String getViewingStart(){
 		return Integer.toString(currentStart + 1);
 	}
 	
 	private String getViewingEnd(){
 		return Integer.toString((totalCount < (currentStart + currentCount)) ? totalCount : (currentStart + currentCount));
 	}
 	
 	private String getViewingTotal(){
 		return totalCount.toString();
 	}
 	
 	private String getCurrentSelect(){
 		return currentCount.toString();
 	}
 	
 	//Form Submit Methods
 	private void changePageSize(){
 		//do nothing
 	}
 }
