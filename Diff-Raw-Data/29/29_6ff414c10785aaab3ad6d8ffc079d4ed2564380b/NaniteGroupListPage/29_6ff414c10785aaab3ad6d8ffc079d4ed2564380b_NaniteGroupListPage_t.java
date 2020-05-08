 package net.micwin.elysium.view.collective;
 
 /*
  (c) 2012 micwin.net
 
  This file is part of open-space.
 
  open-space is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
 
  open-space is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Affero Public License for more details.
 
  You should have received a copy of the GNU Affero Public License
  along with open-space.  If not, see http://www.gnu.org/licenses.
 
  Diese Datei ist Teil von open-space.
 
  open-space ist Freie Software: Sie können es unter den Bedingungen
  der GNU Affero Public License, wie von der Free Software Foundation,
  Version 3 der Lizenz oder (nach Ihrer Option) jeder späteren
  veröffentlichten Version, weiterverbreiten und/oder modifizieren.
 
  open-space wird in der Hoffnung, dass es nützlich sein wird, aber
  OHNE JEDE GEWÄHRLEISTUNG, bereitgestellt; sogar ohne die implizite
  Gewährleistung der MARKTFÄHIGKEIT oder EIGNUNG FÜR EINEN BESTIMMTEN ZWECK.
  Siehe die GNU Affero Public License für weitere Details.
 
  Sie sollten eine Kopie der GNU Affero Public License zusammen mit diesem
  Programm erhalten haben. Wenn nicht, siehe http://www.gnu.org/licenses. 
 
  */
 
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import net.micwin.elysium.dao.DaoManager;
 import net.micwin.elysium.entities.NaniteGroup;
 import net.micwin.elysium.entities.NaniteGroup.State;
 import net.micwin.elysium.entities.characters.Avatar;
 import net.micwin.elysium.entities.characters.User;
 import net.micwin.elysium.entities.galaxy.Environment;
 import net.micwin.elysium.entities.galaxy.Position;
 import net.micwin.elysium.entities.gates.Gate;
 import net.micwin.elysium.view.BasePage;
 import net.micwin.elysium.view.ElysiumWicketModel;
 import net.micwin.elysium.view.jumpGates.UsePlanetaryGatePage;
 
 import org.apache.wicket.Component;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.link.Link;
 import org.apache.wicket.markup.repeater.Item;
 import org.apache.wicket.markup.repeater.RefreshingView;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 
 public class NaniteGroupListPage extends BasePage {
 
 	@SpringBean
 	DaoManager daoManager;
 
 	public NaniteGroupListPage() {
 		super(true);
 		ensureStoryShown();
 		ensureAvatarPresent(true);
 	}
 
 	@Override
 	protected void onInitialize() {
 		super.onInitialize();
 
 		if (getAvatar().getNanites().size() == 1) {
 
 			getElysiumSession().setNamedEntity(NaniteGroupShowPage.NE_NANITE_GROUP,
 							getAvatar().getNanites().iterator().next());
 			setResponsePage(NaniteGroupShowPage.class);
 		}
 
 		addToContentBody(getGroupsTable());
 	}
 
 	@Override
 	protected void onBeforeRender() {
 		super.onBeforeRender();
 	}
 
 	private Component getGroupsTable() {
 
 		Iterator<NaniteGroup> nanites = getAvatar().getNanites().iterator();
 		final List<IModel<NaniteGroup>> models = new ArrayList<IModel<NaniteGroup>>();
 		while (nanites.hasNext()) {
 			models.add(new ElysiumWicketModel<NaniteGroup>(nanites.next()));
 		}
 
 		Component groupsTable = new RefreshingView<NaniteGroup>("groupsTable") {
 			protected Iterator<IModel<NaniteGroup>> getItemModels() {
 				return models.iterator();
 			}
 
 			protected void populateItem(Item<NaniteGroup> item) {
 				final IModel naniteGroupModel = item.getModel();
 				final NaniteGroup nanitesGroup = (NaniteGroup) naniteGroupModel.getObject();
 
 				item.add(new Label("groupId", Model.of(nanitesGroup.getId())));
 
 				Position position = nanitesGroup.getPosition();
 				Gate gate = getGateBPO().getGateAt(position.getEnvironment());
 				String gateCode = gate.getGateAdress();
 
 				item.add(new Label("gateAdress", gateCode));
 
 				Label label = new Label("label", position.getEnvironment().toString());
 				Link<NaniteGroup> link = new Link<NaniteGroup>("groupPosition") {
 
 					@Override
 					public void onClick() {
 						getElysiumSession().setNamedEntity("naniteGroup", (NaniteGroup) naniteGroupModel.getObject());
 						setResponsePage(NaniteGroupShowPage.class);
 					}
 				};
 				link.add(label);
 				item.add(link);
 				Model<String> countModel = new Model<String>(NumberFormat.getIntegerInstance().format(
 								nanitesGroup.getNaniteCount()));
 				item.add(new Label("groupCount", countModel));
 				String stateString = nanitesGroup.getState().toString();
 				item.add(new Label("groupState", new Model(stateString)));
 				item.add(getDoubleCountLink(naniteGroupModel, countModel));
 				item.add(getGateLink(naniteGroupModel));
 
 				item.add(getSplitLink(naniteGroupModel));
 				item.add(getKillLink(naniteGroupModel));
 				item.add(getEntrenchLink(naniteGroupModel));
 
 			}
 
 			private Component getEntrenchLink(final IModel<NaniteGroup> naniteGroupModel) {
 				Link<NaniteGroup> entrenchLink = new Link<NaniteGroup>("entrench", naniteGroupModel) {
 
 					/**
 					 * 
 					 */
 					private static final long serialVersionUID = -7730841008038281114L;
 
 					@Override
 					public void onClick() {
 
 						getNanitesBPO().entrench(getModelObject());
 					}
 
 				};
 
 				entrenchLink.setVisible(getNanitesBPO().canEntrench(naniteGroupModel.getObject()));
 				return entrenchLink;
 			}
 
 			protected Link getSplitLink(final IModel<NaniteGroup> naniteGroupModel) {
 				Link link = new Link("split") {
 
 					/**
 					 * 
 					 */
 					private static final long serialVersionUID = -213962075156991317L;
 
 					@Override
 					public void onClick() {
 						getNanitesBPO().split(naniteGroupModel.getObject());
 						setResponsePage(NaniteGroupListPage.class);
 					}
 				};
 
 				link.setVisible(getNanitesBPO().canSplit(naniteGroupModel.getObject()));
 				return link;
 			}
 
 			protected Link getKillLink(final IModel<NaniteGroup> naniteGroupModel) {
 				Link link = new Link("kill") {
 
 					/**
 					 * 
 					 */
 					private static final long serialVersionUID = -213962075156991317L;
 
 					@Override
 					public void onClick() {
 						getNanitesBPO().kill(naniteGroupModel.getObject());
 						setResponsePage(NaniteGroupListPage.class);
 					}
 				};
 
 				link.setVisible(naniteGroupModel.getObject().getController().getNanites().size() > 1);
 				return link;
 			}
 
 			private Component getGateLink(final IModel<NaniteGroup> naniteGroupModel) {
 
 				final NaniteGroup naniteGroup = naniteGroupModel.getObject();
 				Position position = naniteGroup.getPosition();
 				Environment environment = position.getEnvironment();
 				Gate gate = getGateBPO().getGateAt(environment);
 
 				Link<Gate> gateLink = new Link<Gate>("jumpGate") {
 					public void onClick() {
 						getElysiumSession().setNamedEntity("naniteGroup", naniteGroupModel.getObject());
 						setResponsePage(UsePlanetaryGatePage.class);
 					};
 				};
				gateLink.setVisible(gate != null && getNanitesBPO().canJumpGate(naniteGroup));
	
 				return gateLink;
 			}
 
 			private Component getDoubleCountLink(final IModel<NaniteGroup> nanitesGroupModel,
 							final IModel<String> labelModel) {
 				Link link = new Link("doubleCount") {
 
 					@Override
 					public void onClick() {
 						getNanitesBPO().doubleCount(nanitesGroupModel.getObject());
 						setResponsePage(NaniteGroupListPage.class);
 					}
 				};
 				Avatar controller = nanitesGroupModel.getObject().getController();
 
 				link.setVisible(getNanitesBPO().canRaiseNanitesCount(nanitesGroupModel.getObject()));
 				return link;
 			}
 		};
 
 		return groupsTable;
 	}
 
 	/**
 	 * 
 	 * @param user
 	 * @return
 	 */
 	public static boolean userCanShow(User user) {
 		return user != null;
 	}
 }
