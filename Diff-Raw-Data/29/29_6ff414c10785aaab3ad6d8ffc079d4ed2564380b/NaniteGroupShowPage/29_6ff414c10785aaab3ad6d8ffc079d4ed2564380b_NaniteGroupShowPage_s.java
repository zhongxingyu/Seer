 package net.micwin.elysium.view.collective;
 
 import java.text.DecimalFormat;
 import java.text.DecimalFormatSymbols;
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import net.micwin.elysium.bpo.BaseBPO;
 import net.micwin.elysium.entities.NaniteGroup;
 import net.micwin.elysium.entities.NaniteGroup.State;
 import net.micwin.elysium.entities.appliances.Utilization;
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
 import org.apache.wicket.model.ResourceModel;
 
 public class NaniteGroupShowPage extends BasePage {
 
 	static final String NE_NANITE_GROUP = "naniteGroup";
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1446425437846982751L;
 
 	public NaniteGroupShowPage() {
 		super(true);
 	}
 
 	@Override
 	protected void onInitialize() {
 		super.onInitialize();
 		ensureAvatarPresent(true);
 		ensureLoggedIn();
 		ensureStoryShown();
 
 		NaniteGroup group = getElysiumSession().getNamedEntity(NE_NANITE_GROUP);
 		final ElysiumWicketModel<NaniteGroup> groupModel = new ElysiumWicketModel<NaniteGroup>(group);
 		addToContentBody(new Label("groupId", "" + group.getId()));
 		addToContentBody(new Label("groupPosition", "" + group.getPosition().getEnvironment()));
 
 		String gateCode = getGateBPO().getGateAt(group.getPosition().getEnvironment()).getGateAdress();
 
 		addToContentBody(new Label("groupGate", "" + gateCode));
 		addToContentBody(new Label("groupCount", NumberFormat.getIntegerInstance().format(group.getNaniteCount())));
 		addToContentBody(new Label("signatureStrength", ""
 						+ new DecimalFormat("0.0####", DecimalFormatSymbols.getInstance()).format(getScannerBPO()
 										.computeSignatureStrength(group))));
 
 		addToContentBody(new Label("groupState", "" + group.getState()));
 		addToContentBody(getDoubleCountLink(groupModel));
 
		addToContentBody(new Link<Gate>("jumpGate") {
			public void onClick() {
				getElysiumSession().setNamedEntity(NE_NANITE_GROUP, groupModel.getEntity());
				setResponsePage(UsePlanetaryGatePage.class);
			};
		});
 
 		addToContentBody(composeQuickJumpItems(group));
 		addToContentBody(getOtherNanitesTable(group));
 		addToContentBody(getSplitLink(groupModel));
 		addToContentBody(composeTitleText(group));
 
 	}
 
 	private Component composeTitleText(NaniteGroup group) {
 		String key = "naniteGroup";
 		return new Label("titleText", new ResourceModel(key));
 	}
 
 	protected Link getDoubleCountLink(final ElysiumWicketModel<NaniteGroup> groupModel) {
 		Link link = new Link("doubleCount") {
 
 			/**
 			 * 
 			 */
 			private static final long serialVersionUID = -213962075156991317L;
 
 			@Override
 			public void onClick() {
 				getNanitesBPO().doubleCount(groupModel.getEntity());
 				setResponsePage(NaniteGroupShowPage.class);
 			}
 		};
 
 		link.setVisible(getNanitesBPO().canRaiseNanitesCount(groupModel.getEntity()));
 
 		return link;
 	}
 
 	protected Link getSplitLink(final ElysiumWicketModel<NaniteGroup> groupModel) {
 		Link link = new Link("split") {
 
 			/**
 			 * 
 			 */
 			private static final long serialVersionUID = -213962075156991317L;
 
 			@Override
 			public void onClick() {
 				getNanitesBPO().split(groupModel.getEntity());
 				setResponsePage(NaniteGroupShowPage.class);
 			}
 		};
 
 		link.setVisible(getNanitesBPO().canSplit(groupModel.getObject()));
 		return link;
 	}
 
 	private Collection<Component> composeQuickJumpItems(NaniteGroup group) {
 
 		Collection<Component> result = new LinkedList<Component>();
 
 		final ElysiumWicketModel<NaniteGroup> groupModel = new ElysiumWicketModel<NaniteGroup>(group);
 
 		final String homeGateAdress = getAvatar().getHomeGateAdress();
 
 		result.add(getJumpHomeLink(groupModel, homeGateAdress));
 
 		result.add(getJumpArenaLink(groupModel));
 
 		result.add(getJumpElysiumLink(groupModel));
 
 		return result;
 	}
 
 	protected Link<String> getJumpHomeLink(final ElysiumWicketModel<NaniteGroup> groupModel, final String homeGateAdress) {
 
 		String currentGateAdress = getGateBPO().getGateAt(groupModel.getEntity().getPosition().getEnvironment())
 						.getGateAdress();
 
 		Link<String> link = new Link<String>("jumpHome") {
 
 			@Override
 			public void onClick() {
 				getNanitesBPO().gateTravel(groupModel.getEntity(), homeGateAdress);
 				setResponsePage(NaniteGroupShowPage.class);
 			}
 		};
 
 		link.setEnabled(!homeGateAdress.equals(currentGateAdress));
 		link.setVisible(getNanitesBPO().canJumpGate(groupModel.getObject()));
 
 		return link;
 	}
 
 	protected Link<String> getJumpArenaLink(final ElysiumWicketModel<NaniteGroup> groupModel) {
 		Link<String> link = new Link<String>("jumpArena") {
 
 			@Override
 			public void onClick() {
 				getNanitesBPO().gateTravel(groupModel.getEntity(), "arena");
 				setResponsePage(NaniteGroupShowPage.class);
 
 			}
 		};
 
 		String currentGateAdress = getGateBPO().getGateAt(groupModel.getEntity()).getGateAdress();
 
 		link.setEnabled(!"arena".equals(currentGateAdress));
 		link.setVisible(getNanitesBPO().canJumpGate(groupModel.getObject()));
 
 		return link;
 	}
 
 	protected Link<String> getJumpElysiumLink(final ElysiumWicketModel<NaniteGroup> groupModel) {
 		Link<String> link = new Link<String>("jumpElysium") {
 
 			@Override
 			public void onClick() {
 				getNanitesBPO().gateTravel(groupModel.getEntity(), "elysium");
 				setResponsePage(NaniteGroupShowPage.class);
 			}
 		};
 
 		String currentGateAdress = getGateBPO().getGateAt(groupModel.getEntity()).getGateAdress();
 		link.setEnabled(!"elysium".equals(currentGateAdress));
 		link.setVisible(getNanitesBPO().canJumpGate(groupModel.getObject()));
 
 		return link;
 	}
 
 	private Component getOtherNanitesTable(NaniteGroup scanningGroup) {
 
 		Iterator<NaniteGroup> nanites = getScannerBPO().scanForOtherNaniteGroups(scanningGroup).iterator();
 		final ElysiumWicketModel<NaniteGroup> scanningGroupModel = new ElysiumWicketModel<NaniteGroup>(scanningGroup);
 
 		final List<IModel> models = new ArrayList<IModel>();
 		while (nanites.hasNext()) {
 			models.add(new ElysiumWicketModel<NaniteGroup>(nanites.next()));
 		}
 
 		Component otherNanitesTable = new RefreshingView<NaniteGroup>("otherNanitesTable") {
 			protected Iterator getItemModels() {
 
 				return models.iterator();
 			}
 
 			protected void populateItem(Item item) {
 				final ElysiumWicketModel<NaniteGroup> naniteGroupModel = (ElysiumWicketModel<NaniteGroup>) item
 								.getModel();
 				NaniteGroup otherGroup = (NaniteGroup) naniteGroupModel.getObject();
 
 				Position position = otherGroup.getPosition();
 				Gate gate = getGateBPO().getGateAt(position.getEnvironment());
 				String gateCode = gate.getGateAdress();
 
 				item.add(new Label("owner", new Model(otherGroup.getController().getName())));
 
 				NaniteGroup scanningGroup = scanningGroupModel.getObject();
 				boolean canScanDetails = getScannerBPO().canScanDetails(scanningGroup, otherGroup);
 				String leveltext = canScanDetails ? NumberFormat.getIntegerInstance().format(
 								otherGroup.getController().getLevel()) : "???";
 				item.add(new Label("level", new Model(leveltext)));
 
 				item.add(new Label("signature", new Model(NumberFormat.getNumberInstance().format(
 								getScannerBPO().computeSignatureStrength(otherGroup)))));
 
 				String countText = canScanDetails ? NumberFormat.getNumberInstance()
 								.format(otherGroup.getNaniteCount()) : "???";
 
 				item.add(new Label("count", new Model(countText)));
 
 				item.add(getAttackCommandLink(scanningGroupModel, naniteGroupModel));
 
 			}
 
 			private Component getAttackCommandLink(final ElysiumWicketModel<NaniteGroup> attackerModel,
 							final ElysiumWicketModel<NaniteGroup> defenderModel) {
 				Link link = new Link("attack") {
 
 					@Override
 					public void onClick() {
 
 						NaniteGroup attacker = attackerModel.getEntity();
 						NaniteGroup defender = defenderModel.getEntity();
 						if (getNanitesBPO().canAttack(attacker, defender)) {
 							getNanitesBPO().attack(attacker, defender);
 							if (attacker.getNaniteCount() > 0) {
 								getPage().setResponsePage(NaniteGroupShowPage.class);
 							} else {
 								getElysiumSession().setNamedEntity(NE_NANITE_GROUP, null);
 								getPage().setResponsePage(NaniteGroupListPage.class);
 							}
 						} else {
 							error("cannot attack");
 						}
 					}
 				};
 
 				link.setVisible(getNanitesBPO().canAttack(attackerModel.getEntity(), defenderModel.getEntity()));
 				return link;
 			}
 		};
 
 		return otherNanitesTable;
 	}
 
 }
