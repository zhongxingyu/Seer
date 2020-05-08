 package net.micwin.elysium.view.stats;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import net.micwin.elysium.dao.DaoManager;
 import net.micwin.elysium.entities.characters.Avatar;
 import net.micwin.elysium.entities.characters.User.Role;
 import net.micwin.elysium.view.BasePage;
 import net.micwin.elysium.view.ElysiumApplication;
 import net.micwin.elysium.view.ElysiumWicketModel;
 
 import org.apache.wicket.Component;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.link.Link;
 import org.apache.wicket.markup.repeater.Item;
 import org.apache.wicket.markup.repeater.RefreshingView;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.Model;
 
 public class StatisticsPage extends BasePage {
 
 	public StatisticsPage() {
 		super(false);
 	}
 
 	@Override
 	protected void onInitialize() {
 		super.onInitialize();
 		addToContentBody(createUserCountLabel());
 		addToContentBody(createStartupTimeLabel());
 		addToContentBody(new Label("gateCount", Model.of(DaoManager.I.getGatesDao().countEntries())));
 
 		addToContentBody(createRankingTable());
 	}
 
 	private Component createStartupTimeLabel() {
 		ElysiumApplication app = (ElysiumApplication) getElysiumSession().getApplication();
 		return new Label("startupTime", Model.of(app.getStartupTime().toString()));
 	}
 
 	private Component createUserCountLabel() {
 		return new Label("userCount", Model.of(DaoManager.I.getUserDao().countEntries()));
 	}
 
 	private Component createRankingTable() {
 
 		List<Avatar> avatars = new LinkedList<Avatar>();
 		DaoManager.I.getAvatarDao().loadAll(avatars);
 
 		filterAdmins(avatars);
 		Collections.sort(avatars, new Comparator<Avatar>() {
 
 			@Override
 			public int compare(Avatar o1, Avatar o2) {
 				if (o1.getPoints() > o2.getPoints())
 					return -1;
 				if (o1.getPoints() < o2.getPoints()) {
 					return 1;
 				}
 				return 0;
 			}
 		});
 
 		// only show the first 10
		if (avatars.size() > 10 && getAvatar().getUser().getRole() != Role.ADMIN)
 			avatars = avatars.subList(0, 9);
 
 		final List<IModel<Avatar>> modelList = new LinkedList<IModel<Avatar>>();
 
 		for (Avatar avatar : avatars) {
 			modelList.add(new ElysiumWicketModel<Avatar>(avatar));
 		}
 
 		RefreshingView<Avatar> rankingTable = new RefreshingView<Avatar>("rankingTable") {
 
 			@Override
 			protected Iterator<IModel<Avatar>> getItemModels() {
 				return modelList.iterator();
 			}
 
 			@Override
 			protected void populateItem(Item<Avatar> item) {
 				Avatar avatar = item.getModelObject();
 				item.add(new Label("name", avatar.getName()));
 				item.add(new Label("points", Model.of(avatar.getPoints())));
 				item.add(new Label("groupsCount", Model.of(avatar.getNanites().size())));
 
 				item.add(new Label("birthDate", Model.of(avatar.getCreationDate().toString())));
 				Date lastLoginDate = avatar.getUser().getLastLoginDate();
 				item.add(new Label("lastLogin", Model.of(lastLoginDate != null ? lastLoginDate.toString()
 								: "<unbekannt>")));
 				item.add(getLeverageLink(avatar, 51));
 				item.add(getLeverageLink(avatar, 101));
 				item.add(getResurectLink(avatar));
 
 			}
 		};
 
 		return rankingTable;
 	}
 
 	protected Component getLeverageLink(Avatar avatar, final int targetLevel) {
 		String id = "leverage" + targetLevel + "Link";
 
 		if (!isAdmin() || avatar.getLevel() > targetLevel) {
 			return createDummyLink(id, false, false);
 		}
 
 		final ElysiumWicketModel<Avatar> model = ElysiumWicketModel.of(avatar);
 		Link leverageLink = new Link(id) {
 
 			@Override
 			public void onClick() {
 				getAvatarBPO().leverage(model.getObject(), targetLevel);
 				setResponsePage(StatisticsPage.class);
 
 			}
 		};
 		return leverageLink;
 	}
 
 	protected Component getResurectLink(Avatar avatar) {
 		if (!isAdmin() || getAvatarBPO().isAlive(avatar)) {
 
 			return createDummyLink("resurrectLink", false, false);
 		}
 
 		final ElysiumWicketModel<Avatar> model = ElysiumWicketModel.of(avatar);
 
 		Link resurrectLink = new Link("resurrectLink") {
 
 			@Override
 			public void onClick() {
 
 				getAvatarBPO().resurrect(model.getObject());
 				setResponsePage(StatisticsPage.class);
 			}
 		};
 
 		return resurrectLink;
 	}
 
 	private void filterAdmins(List<Avatar> avatars) {
 
 		List<Avatar> admins = new LinkedList<Avatar>();
 
 		for (Avatar avatar : avatars) {
 			if (avatar.getUser().getRole() == Role.ADMIN) {
 				admins.add(avatar);
 			}
 		}
 
 		avatars.removeAll(admins);
 	}
 }
