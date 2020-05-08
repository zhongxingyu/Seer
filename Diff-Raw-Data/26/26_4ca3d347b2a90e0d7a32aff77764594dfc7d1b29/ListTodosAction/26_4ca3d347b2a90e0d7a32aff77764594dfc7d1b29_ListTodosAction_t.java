 package ch.helvetia.jax2011.web;
 
 import java.io.Serializable;
 import java.util.Date;
 import java.util.List;
 
 import javax.enterprise.context.Conversation;
 import javax.faces.context.FacesContext;
 import javax.inject.Inject;
 
 import org.primefaces.event.DateSelectEvent;
 import org.primefaces.model.tagcloud.DefaultTagCloudItem;
 import org.primefaces.model.tagcloud.DefaultTagCloudModel;
 import org.primefaces.model.tagcloud.TagCloudModel;
 
 import ch.helvetia.jax2011.boundary.ListTodosTask;
 import ch.helvetia.jax2011.common.stereotypes.Action;
 import ch.helvetia.jax2011.entity.Tag;
 import ch.helvetia.jax2011.entity.Todo;
import ch.helvetia.jax2011.error.TodoException;
 
 /**
  * Action to create a new todo-item.
  */
 @Action
 public class ListTodosAction implements Serializable {
 
 	@Inject
 	private ListTodosTask task;
 
 	@Inject
 	private Conversation conversation;
 
 	private Date filterDate = new Date();
 
 	private Long filterTagId = null;
 
 	private TagCloudModel tagCloudModel = new DefaultTagCloudModel();
 
 	// TODO: introduce seam3 view-action
 	public void init() {
 		if (!FacesContext.getCurrentInstance().isPostback()
 				&& conversation.isTransient()) {
 			task.init();
 		}
		throw new TodoException();
 	}
 
 	public void refresh() {
 		if (!FacesContext.getCurrentInstance().isPostback()) {
 			task.findAllTodos(filterDate, filterTagId);
 			initTagCloudModel();
 		}
 	}
 
 	public void dateChanged(DateSelectEvent e) {
 		filterDate = e.getDate();
 		task.findAllTodos(filterDate, filterTagId);
 		initTagCloudModel();
 	}
 
 	private void initTagCloudModel() {
 		TagCloudModel result = new DefaultTagCloudModel();
 		for (Object[] tagCount : task.countTags(filterDate)) {
 			Tag loopTag = (Tag) tagCount[0];
 			int loopCount = ((Long) tagCount[1]).intValue();
 			result.addTag(new DefaultTagCloudItem(loopTag.getName(),
 					"/home.xhtml?filterTagId=" + loopTag.getId() + "&cid="
 							+ conversation.getId(), loopCount));
 		}
 		tagCloudModel = result;
 	}
 
 	public String editTodo(Todo todo) {
 		task.finish();
 		return "/editTodo.xhtml?faces-redirect=true&id=" + todo.getId();
 	}
 
 	//
 	// getter & setter
 	//
 
 	public Date getFilterDate() {
 		return filterDate;
 	}
 
 	public void setFilterDate(Date filterDate) {
 		this.filterDate = filterDate;
 	}
 
 	public Long getFilterTagId() {
 		return filterTagId;
 	}
 
 	public void setFilterTagId(Long filterTagId) {
 		this.filterTagId = filterTagId;
 	}
 
 	public List<Todo> getTodos() {
 		return task.getTodos();
 	}
 
 	public ListTodosTask getTask() {
 		return task;
 	}
 
 	public TagCloudModel getTagCloudModel() {
 		return tagCloudModel;
 	}
 
 }
