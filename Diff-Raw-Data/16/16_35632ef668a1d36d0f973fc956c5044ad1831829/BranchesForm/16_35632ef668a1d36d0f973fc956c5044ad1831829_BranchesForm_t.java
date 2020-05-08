 package org.vamdc.portal.session.queryBuilder.forms;
 
import java.util.EnumSet;

import org.vamdc.dictionary.Requestable;

 public class BranchesForm extends AbstractForm implements Form{
 
 	private static final long serialVersionUID = 8345255113788420868L;
 	
 	@Override
 	public String getTitle() { return "Request branches"; }
 	@Override
 	public Integer getOrder() { return Order.Branches; }
 	@Override
 	public String getView() { return "/xhtml/query/forms/branchesForm.xhtml"; }
 	//Tree representation of requestable keywords (XSAMS branches) is called from WAR package, org.vamdc.portal.session.queryBuilder.branches
 	//Here we only attach the appropriate face.
 	
	@Override
	public void clear() {
		queryData.setRequest(EnumSet.noneOf(Requestable.class));
	}
 }
