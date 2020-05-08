 package com.tp.action;
 
 import java.util.Date;
 import java.util.List;
 
 import org.apache.commons.lang3.StringUtils;
 import org.apache.shiro.authz.annotation.RequiresPermissions;
 import org.apache.struts2.convention.annotation.Namespace;
 import org.apache.struts2.convention.annotation.Result;
 import org.apache.struts2.convention.annotation.Results;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import com.google.common.collect.Lists;
 import com.tp.entity.UserFeedback;
 import com.tp.orm.Page;
 import com.tp.orm.PageRequest.Sort;
 import com.tp.orm.PropertyFilter;
 import com.tp.service.FeedbackService;
 import com.tp.utils.Struts2Utils;
 
 @Namespace("/report")
 @Results({ @Result(name = CRUDActionSupport.RELOAD, location = "feedback.action", type = "redirect") })
 public class FeedbackAction extends CRUDActionSupport<UserFeedback> {
 
 	private static final long serialVersionUID = 1L;
 	private Long id;
 	private UserFeedback entity;
 	private Page<UserFeedback> page = new Page<UserFeedback>();
 	private List<Integer> sliders = Lists.newArrayList();
 	private FeedbackService feedbackService;
 
     @RequiresPermissions("feedback:view")
 	public String execute() throws Exception {
 		return list();
 	}
 
     @RequiresPermissions("feedback:view")
 	public String list() throws Exception {
 		List<PropertyFilter> filters = PropertyFilter.buildFromHttpRequest(Struts2Utils.getRequest());
 		String defaultFilter = Struts2Utils.getParameter("filter_EQS_status");
 		if (defaultFilter == null) {
 			PropertyFilter filter = new PropertyFilter("EQS_status", "0");
 			filters.add(filter);
 		}
 		if (!page.isOrderBySetted()) {
 			page.setOrderBy("createTime");
 			page.setOrderDir(Sort.DESC);
 		}
 		page = feedbackService.searchFeedback(page, filters);
 		sliders = page.getSlider(10);
 		return SUCCESS;
 	}
 
//    @RequiresPermissions("feedback:edit")
 	public String save() throws Exception {
 		feedbackService.save(entity);
 		if (id == null)
 			return null;
 		return RELOAD;
 	}
 
 	public String input() throws Exception {
 
 		return INPUT;
 	}
 
 	public Long getId() {
 		return id;
 	}
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	public List<Integer> getSliders() {
 		return sliders;
 	}
 
 	public Page<UserFeedback> getPage() {
 		return page;
 	}
 
 	@Autowired
 	public void setFeedbackService(FeedbackService feedbackService) {
 		this.feedbackService = feedbackService;
 	}
 
 	@Override
 	public UserFeedback getModel() {
 
 		return entity;
 	}
 
 	@Override
 	public String delete() throws Exception {
 		feedbackService.delete(id);
 		return RELOAD;
 	}
 
 	@Override
 	protected void prepareModel() throws Exception {
 		if (id == null) {
 			entity = new UserFeedback();
 			String content = Struts2Utils.getParameter("opinition");
 			String contacts = Struts2Utils.getParameter("contact");
 			String params = Struts2Utils.getParameter("params");
 
 			entity.setContact(StringUtils.substring(contacts, 0, 50));
 			entity.setContent(StringUtils.substring(content, 0, 140));
 			entity.setParams(StringUtils.substring(params, 0, 255));
 			entity.setCreateTime(new Date());
 			entity.setStatus("0");
 		} else {
 			entity = feedbackService.get(id);
 			entity.setModifyTime(new Date());
 		}
 	}
 }
