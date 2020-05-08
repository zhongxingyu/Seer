 package infodoc.basic.activity;
 
 import infodoc.basic.BasicConstants;
 import infodoc.core.container.InfodocContainerFactory;
 import infodoc.core.dto.Case;
 import infodoc.core.dto.Activity;
 import infodoc.core.dto.User;
 import infodoc.core.ui.activity.ActivityListExecutorTemplate;
 import infodoc.core.ui.cases.CaseForm;
 import infodoc.core.ui.common.InfodocTheme;
 
 import java.util.HashSet;
 
 import com.vaadin.terminal.Resource;
 import com.vaadin.terminal.ThemeResource;
 
 public class Update extends ActivityListExecutorTemplate {
 
 	private static final long serialVersionUID = 1L;
 	
 	public Update() {
 		super();
 	}
 
 	public Update(Activity activity, User user) {
 		super(activity, user);
 	}
 	
 	@Override
 	public void execute(CaseForm form) {
 		form.validate();
 		HashSet<User> users = new HashSet<User>();
 		users.add(getUser());
 		Case caseDto = InfodocContainerFactory.getCaseContainer().getEntity(form.getCase().getId());
 		InfodocContainerFactory.getCaseContainer().updateInstance(caseDto, form.getPropertyValues(), getNewActivityInstance(caseDto, form.getComments(), users, null));
 		update();
 	}
 
 	@Override
 	public String getHelp() {
 		return BasicConstants.uiHelpUpdateActivity;
 	}
 
 	@Override
 	public Resource getIcon() {
		return new ThemeResource(InfodocTheme.iconActivityUpdate);
 	}
 
 }
