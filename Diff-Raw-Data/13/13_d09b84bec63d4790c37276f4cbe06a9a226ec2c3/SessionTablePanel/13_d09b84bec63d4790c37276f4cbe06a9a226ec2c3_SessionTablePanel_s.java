 package net.alpha01.jwtest.panels.session;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import net.alpha01.jwtest.JWTestSession;
 import net.alpha01.jwtest.beans.Session;
 import net.alpha01.jwtest.component.DataTableAlternatedRows;
 import net.alpha01.jwtest.component.DatePropertyColumn;
 import net.alpha01.jwtest.dao.SessionMapper;
 import net.alpha01.jwtest.dao.SqlConnection;
 import net.alpha01.jwtest.dao.SqlSessionMapper;
 import net.alpha01.jwtest.pages.session.SessionsPage;
 import net.alpha01.jwtest.panels.PanelAjaxLink;
 
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
 import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
 import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
 import org.apache.wicket.extensions.markup.html.repeater.data.table.HeadersToolbar;
 import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
 import org.apache.wicket.extensions.markup.html.repeater.data.table.NavigationToolbar;
 import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
 import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.markup.repeater.Item;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.model.StringResourceModel;
 
 public class SessionTablePanel extends Panel {
 	private static final long serialVersionUID = 1L;
 
 	class SessionDataProvider extends SortableDataProvider<Session> {
 		private static final long serialVersionUID = 1L;
 		private int idPlan;
 		private List<Session> sessions;
 
 		public SessionDataProvider(int idPlan) {
 			this.idPlan = idPlan;
 		}
 
 		@Override
 		public Iterator<? extends Session> iterator(int index, int length) {
 			return sessions.subList(index, index + length).iterator();
 		}
 
 		@Override
 		public IModel<Session> model(Session step) {
 			IModel<Session> result = new Model<Session>(step);
 			result.detach();
 			return result;
 		}
 
 		@Override
 		public int size() {
 			SqlSessionMapper<SessionMapper> sesMapper = SqlConnection.getSessionMapper(SessionMapper.class);
 			sessions = sesMapper.getMapper().getAllByPlan(idPlan);
 			sesMapper.close();
 			return sessions.size();
 		}
 
 	}
 	
 	public SessionTablePanel(String id,int idPlan,int rows) {
 		super(id);
 		SessionDataProvider dataProvider = new SessionDataProvider(idPlan);
 		List<IColumn<Session>> columns = new ArrayList<IColumn<Session>>();
 		columns.add(new AbstractColumn<Session>(new Model<String>("ID")) {
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void populateItem(Item<ICellPopulator<Session>> item, String contentId, final IModel<Session> model) {
				item.add(new PanelAjaxLink(contentId,model.getObject().getId().toString()) {
					private static final long serialVersionUID = 1L;
					@Override
					protected void onAjaxClick(AjaxRequestTarget target) {
						((JWTestSession)getWebPage().getSession()).setCurrentSession(model.getObject());
						setResponsePage(SessionsPage.class);
					}
				});
 			}
 		});
 		columns.add(new PropertyColumn<Session>(new StringResourceModel("version", this, null), "version"));
 		columns.add(new DatePropertyColumn<Session>(new StringResourceModel("start_date", this, null), "start_date","dd-MM-yyyy HH:mm"));
 		columns.add(new DatePropertyColumn<Session>(new StringResourceModel("end_date", this, null), "end_date","dd-MM-yyyy HH:mm"));
 		DataTable<Session> sessionDataTable = new DataTableAlternatedRows<Session>("sessionDataTable", columns, dataProvider, rows);
 		sessionDataTable.addTopToolbar(new HeadersToolbar(sessionDataTable, dataProvider));
 		sessionDataTable.addBottomToolbar(new NavigationToolbar(sessionDataTable));
 		add(sessionDataTable);
 	}
 
 }
