 package infodoc.basic.activity;
 
 import infodoc.basic.BasicConstants;
 import infodoc.core.container.InfodocContainerFactory;
 import infodoc.core.dto.Case;
 import infodoc.core.dto.Activity;
 import infodoc.core.dto.User;
 import infodoc.core.ui.common.InfodocTheme;
 
 import java.util.List;
 
 import com.vaadin.terminal.Resource;
 import com.vaadin.terminal.ThemeResource;
 
 public class Unassign extends Update {
 
 	private static final long serialVersionUID = 1L;
 	
 	public Unassign() {
 		super();
 	}
 
 	public Unassign(Activity activity, User user) {
 		super(activity, user);
 	}
 
 	@Override
 	public List<Case> getCases() {
 		return InfodocContainerFactory.getCaseContainer().findAssignedToOtherUserByUserIdAndNextActivityId(getUser().getId(), getActivity().getId());
 	}
 
 	@Override
 	public String getHelp() {
 		return BasicConstants.uiHelpUnassignActivity;
 	}
 
 	@Override
 	public Resource getIcon() {
		return new ThemeResource(InfodocTheme.iconActivityUnassign);
 	}
 
 }
