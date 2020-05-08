 /**
  * 
  */
 package org.mdkt.maildist.client;
 
 import java.util.ArrayList;
 
 import org.mdkt.library.client.AbstractAppController;
 import org.mdkt.library.client.LoginServiceAsync;
 import org.mdkt.library.client.rpc.DefaultAsyncCallBack;
 import org.mdkt.maildist.client.dto.DistList;
 import org.mdkt.maildist.client.dto.DistListMember;
 import org.mdkt.maildist.client.event.AddAliasEvent;
 import org.mdkt.maildist.client.event.AddDistListEvent;
 import org.mdkt.maildist.client.event.AddDistListMemberEvent;
 import org.mdkt.maildist.client.event.AliasDeletedEvent;
 import org.mdkt.maildist.client.event.DistListDeletedEvent;
 import org.mdkt.maildist.client.event.DistListMemberDeletedEvent;
 import org.mdkt.maildist.client.event.SaveAliasCancelledEvent;
 import org.mdkt.maildist.client.event.SaveAliasEvent;
 import org.mdkt.maildist.client.event.SaveDistListCancelledEvent;
 import org.mdkt.maildist.client.event.SaveDistListEvent;
 import org.mdkt.maildist.client.event.SaveDistListMemberCancelledEvent;
 import org.mdkt.maildist.client.event.SaveDistListMemberEvent;
 import org.mdkt.maildist.client.event.ShowDistListMemberEvent;
 import org.mdkt.maildist.client.presenter.AddAliasPresenter;
 import org.mdkt.maildist.client.presenter.AddDistListMemberPresenter;
 import org.mdkt.maildist.client.presenter.AddDistListPresenter;
 import org.mdkt.maildist.client.presenter.AliasPresenter;
 import org.mdkt.maildist.client.presenter.DistListMembersPresenter;
 import org.mdkt.maildist.client.presenter.DistListPresenter;
 import org.mdkt.maildist.client.rpc.DistListService;
 import org.mdkt.maildist.client.rpc.DistListServiceAsync;
 import org.mdkt.maildist.client.view.AddAliasViewImpl;
 import org.mdkt.maildist.client.view.AddDistListMemberView;
 import org.mdkt.maildist.client.view.AddDistListMemberViewImpl;
 import org.mdkt.maildist.client.view.AddDistListViewImpl;
 import org.mdkt.maildist.client.view.AliasViewImpl;
 import org.mdkt.maildist.client.view.DistListMembersView;
 import org.mdkt.maildist.client.view.DistListMembersViewImpl;
 import org.mdkt.maildist.client.view.DistListViewImpl;
 import org.mdkt.maildist.client.view.MailDistHome;
 
 import com.google.gwt.event.shared.EventBus;
 import com.google.gwt.user.client.History;
 import com.google.gwt.user.client.ui.HasWidgets;
 
 /**
  * @author trung
  *
  */
 public class MailDistAppController extends AbstractAppController {
 		
 	public static final String MD_HOME = "Distribution List";
 	public static final String MD_LIST_ADD = "Distribution List/add";
 	public static final String MD_LIST_MEMBERS = "Distribution List/members";
 	public static final String MD_ALIAS = "Alias";
 	public static final String MD_ALIAS_ADD = "Alias/add";
 	
 	private final MailDistHome home;
 
 	private final DistListServiceAsync distListService = DistListService.Util.getInstance();
 		
 	public MailDistAppController(HasWidgets container, EventBus eventBus,
 			LoginServiceAsync rpcLoginService) {
 		super(container, eventBus, rpcLoginService);
 		this.home = new MailDistHome();
 		this.container.add(home);
 	}
 
 	@Override
 	protected void bind() {
 		eventBus.addHandler(AddDistListEvent.getType(), new AddDistListEvent.Handler() {
 			
 			@Override
 			public void onAddDistList(AddDistListEvent event) {
 				doAddDistList();
 			}
 		});
 		eventBus.addHandler(SaveDistListCancelledEvent.getType(), new SaveDistListCancelledEvent.Handler() {
 			
 			@Override
 			public void onCancelled(SaveDistListCancelledEvent event) {
 				History.newItem(MD_HOME);
 			}
 		});
 		eventBus.addHandler(SaveDistListEvent.getType(), new SaveDistListEvent.Handler() {
 			@Override
 			public void onSaveDistList(SaveDistListEvent event) {
 				doSaveDistList(event.getDistList());
 			}
 
 		});
 		eventBus.addHandler(DistListDeletedEvent.getType(), new DistListDeletedEvent.Handler() {
 			
 			@Override
 			public void onDistListDeleted(DistListDeletedEvent event) {
 				History.fireCurrentHistoryState();
 			}
 		});
 		eventBus.addHandler(ShowDistListMemberEvent.getType(),new ShowDistListMemberEvent.Handler() {
 			
 			@Override
 			public void onEditDistList(ShowDistListMemberEvent event) {
 				doShowDistListMembers(event.getDistListId());
 			}
 		});
 		eventBus.addHandler(DistListMemberDeletedEvent.getType(), new DistListMemberDeletedEvent.Handler() {
 			
 			@Override
 			public void onDistListMemberDeleted(DistListMemberDeletedEvent event) {
 				History.fireCurrentHistoryState();
 			}
 		});
 		eventBus.addHandler(AddDistListMemberEvent.getType(), new AddDistListMemberEvent.Handler() {
 			
 			@Override
 			public void onAddDistListMember(AddDistListMemberEvent event) {
 				doAddDistListMemberEvent(event.getDistListId());
 			}
 		});
 		eventBus.addHandler(SaveDistListMemberEvent.getType(), new SaveDistListMemberEvent.Handler() {
 			
 			@Override
 			public void onSaveDistListMember(SaveDistListMemberEvent event) {
 				doSaveDistListMember(event.getDistListMembers(), event.getDistListId());
 			}
 		});
 		eventBus.addHandler(SaveDistListMemberCancelledEvent.getType(), new SaveDistListMemberCancelledEvent.Handler() {
 			
 			@Override
 			public void onCancelled(SaveDistListMemberCancelledEvent event) {
 				History.fireCurrentHistoryState();
 			}
 		});
 		eventBus.addHandler(AddAliasEvent.getType(), new AddAliasEvent.Handler() {
 			
 			@Override
 			public void onAddAlias(AddAliasEvent event) {
 				doAddAlias();
 			}
 		});
 		eventBus.addHandler(SaveAliasCancelledEvent.getType(), new SaveAliasCancelledEvent.Handler() {
 			
 			@Override
 			public void onCancelled(SaveAliasCancelledEvent event) {
 				History.newItem(MD_ALIAS);
 			}
 		});
 		eventBus.addHandler(SaveAliasEvent.getType(), new SaveAliasEvent.Handler() {
 			@Override
 			public void onSaveAlias(SaveAliasEvent event) {
 				doSaveAlias(event.getAlias());
 			}
 
 		});
 		eventBus.addHandler(AliasDeletedEvent.getType(), new AliasDeletedEvent.Handler() {
 			
 			@Override
 			public void onAliasDeleted(AliasDeletedEvent event) {
 				History.fireCurrentHistoryState();
 			}
 		});
 		
 	}
 	
 	private void doSaveDistListMember(
 			ArrayList<DistListMember> distListMembers, final String distListId) {
 		distListService.updateDistListMember(distListMembers, new DefaultAsyncCallBack<Void>("add distribution list members") {
 			@Override
 			public void _onSuccess(Void result) {
 				History.fireCurrentHistoryState();
 			}
 		});
 	}
 
 	private void doAddDistListMemberEvent(String distListId) {
 		AddDistListMemberView view = new AddDistListMemberViewImpl();
		HasWidgets tabContainer = home.selectTab(MD_HOME);
		new AddDistListMemberPresenter(distListService, eventBus, view, distListId).go(tabContainer);
 	}
 	
 	private void doShowDistListMembers(String distListId) {
 		doShowDistListMembers(distListId, true);
 	}
 
 	private void doShowDistListMembers(String distListId, boolean issueEvent) {
 		History.newItem(MD_LIST_MEMBERS + "/" + distListId, issueEvent);
 	}
 
 	private void doSaveDistList(String distListName) {
 		DistList distList = new DistList();
 		distList.setDistListId(distListName);
 		distListService.createDistList(distList, new DefaultAsyncCallBack<Void>("save distribution list") {
 			public void _onSuccess(Void result) {
 				History.newItem(MD_HOME);
 			}
 		});
 	}
 
 	private void doSaveAlias(String email) {
 		distListService.addEmailAlias(email, new DefaultAsyncCallBack<Void>("save email alias") {
 			public void _onSuccess(Void result) {
 				History.newItem(MD_ALIAS);
 			}
 		});
 	}
 	/**
 	 * navigate to list/add
 	 */
 	private void doAddDistList() {
 		History.newItem(MD_LIST_ADD);
 	}
 	
 	private void doAddAlias() {
 		History.newItem(MD_ALIAS_ADD);
 	}
 	
 	@Override
 	protected void onHistoryChange(String token) {
 		if (MD_HOME.equals(token)) {
 			HasWidgets tabContainer = home.selectTab(MD_HOME);
 			new DistListPresenter(distListService, eventBus, new DistListViewImpl()).go(tabContainer);
 		} else if (MD_LIST_ADD.equals(token)) {
 			HasWidgets tabContainer = home.selectTab(MD_HOME);
 			new AddDistListPresenter(distListService, eventBus, new AddDistListViewImpl()).go(tabContainer);
 		} else if (token.startsWith(MD_LIST_MEMBERS)) {
 			// extract distListId from the token
 			String distListId = token.substring(token.lastIndexOf("/") + 1);
 			DistListMembersView view = new DistListMembersViewImpl();
 			HasWidgets tabContainer = home.selectTab(MD_HOME);
 			new DistListMembersPresenter(distListService, eventBus, view, distListId).go(tabContainer);		
 		} else 	if (MD_ALIAS.equals(token)) {
 			HasWidgets tabContainer = home.selectTab(MD_ALIAS);
 			new AliasPresenter(distListService, eventBus, new AliasViewImpl()).go(tabContainer);
 		} else if (MD_ALIAS_ADD.equals(token)) {
 			HasWidgets tabContainer = home.selectTab(MD_ALIAS);
 			new AddAliasPresenter(distListService, eventBus, new AddAliasViewImpl()).go(tabContainer);
 		}
 	}
 }
