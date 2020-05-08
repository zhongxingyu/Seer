 package com.myrontuttle.fin.trade.web.panels;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
 import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
 import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
 import org.apache.wicket.extensions.markup.html.repeater.data.table.DefaultDataTable;
 import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
 import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
 import org.apache.wicket.markup.html.form.Button;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.link.Link;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.markup.repeater.Item;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.Model;
 
 import com.myrontuttle.fin.trade.adapt.Group;
 import com.myrontuttle.fin.trade.web.data.DBAccess;
 import com.myrontuttle.fin.trade.web.data.SortableGroupDataProvider;
 import com.myrontuttle.fin.trade.web.pages.GroupPage;
 import com.myrontuttle.fin.trade.web.service.EvolveAccess;
 
 public class GroupTablePanel extends Panel {
 	
 	private static final long serialVersionUID = 1L;
 
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	public GroupTablePanel(String id) {
 		super(id);
 
 		List<IColumn<Group, String>> columns = new ArrayList<IColumn<Group, String>>();
 		
 		columns.add(new PropertyColumn<Group, String>(new Model<String>("ID"), "groupId", "groupId"));
		columns.add(new PropertyColumn(new Model<String>("Alert Address"), "alertAddress"));
 		columns.add(new PropertyColumn(new Model<String>("Frequency"), "frequency"));
 		columns.add(new PropertyColumn(new Model<String>("Active"), "active"));
 		columns.add(new PropertyColumn(new Model<String>("Size"), "size"));
 		columns.add(new PropertyColumn(new Model<String>("Elites"), "eliteCount"));
 		columns.add(new PropertyColumn(new Model<String>("Created On"), "startTime"));
 		columns.add(new PropertyColumn(new Model<String>("Generation"), "generation"));
 		columns.add(new PropertyColumn(new Model<String>("Last Updated"), "updatedTime"));
 		columns.add(new PropertyColumn(new Model<String>("Variability"), "variability"));
 
 		columns.add(new AbstractColumn<Group, String>(new Model<String>("Details")) {
 			public void populateItem(Item<ICellPopulator<Group>> cellItem, String componentId,
 				IModel<Group> model) {
 				cellItem.add(new DetailPanel(componentId, model));
 			}
 		});
 
 		columns.add(new AbstractColumn<Group, String>(new Model<String>("Evolve")) {
 			public void populateItem(Item<ICellPopulator<Group>> cellItem, String componentId,
 				IModel<Group> model) {
 				cellItem.add(new EvolveGroupPanel(componentId, model));
 			}
 		});
 
 		columns.add(new AbstractColumn<Group, String>(new Model<String>("Best Trader")) {
 			public void populateItem(Item<ICellPopulator<Group>> cellItem, String componentId,
 				IModel<Group> model) {
 				cellItem.add(new BestTraderPanel(componentId, model));
 			}
 		});
 
 		columns.add(new AbstractColumn<Group, String>(new Model<String>("Delete Group")) {
 			public void populateItem(Item<ICellPopulator<Group>> cellItem, String componentId,
 				IModel<Group> model) {
 				cellItem.add(new DeleteGroupPanel(componentId, model));
 			}
 		});
 		
 		DataTable dataTable = new DefaultDataTable<Group, String>("groups", columns,
 				new SortableGroupDataProvider(), 5);
 
 		add(dataTable);
 	}
 
 	class DetailPanel extends Panel {
 		/**
 		 * @param id component id
 		 * @param model model for contact
 		 */
 		public DetailPanel(String id, IModel<Group> model) {
 			super(id, model);
 			add(new Link("details") {
 				@Override
 				public void onClick() {
 					String groupId = ((Group)getParent().getDefaultModelObject()).getGroupId();
 					GroupPage gp = new GroupPage(groupId);
 					setResponsePage(gp);
 				}
 			});
 		}
 	}
 
 	class DeleteGroupPanel extends Panel {
 		/**
 		 * @param id component id
 		 * @param model model for contact
 		 */
 		public DeleteGroupPanel(String id, IModel<Group> model) {
 			super(id, model);
 
 			final Form<Group> form = new Form<Group>("deleteGroupForm", model);
 			form.add(new Button("delete") {
 				public void onSubmit() {
 					Group group = ((Group)getParent().getDefaultModelObject());
 					EvolveAccess.getEvolveService().deleteGroupExpression(group.getGroupId());
 					DBAccess.getDAO().removeGroup(group.getGroupId());
 				}
 			});
 			add(form);
 		}
 	}
 }
