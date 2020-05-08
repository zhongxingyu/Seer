 package wicket.contrib.data.model.bind;
 
import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import wicket.Component;
 import wicket.IFeedback;
 import wicket.contrib.data.model.bind.IDataSource.EntityField;
 import wicket.markup.html.list.ListItem;
 import wicket.markup.html.list.ListView;
 import wicket.markup.html.panel.Panel;
 import wicket.model.IModel;
 import wicket.model.PropertyModel;
 import wicket.util.string.Strings;
 
 /**
  * A panel that browses and edits a list of Hibernate objects. Each row
  * maintains an "edit" status that can be toggled by the user. By default, all
  * string fields are turned into text boxes on edit, booleans are turned into
  * check boxes, everything else is a label, and all columns are sortable. The
  * column names are also taken directly from the property names of the class and
  * show up in the order they are defined in the class. This can all be changed,
  * however, by supplying you own list of {@link IColumn}s.
  * 
  * TODO Move some of the data needed at construction into user overridable 
  * methods ncluding stuff not incorperated yet, like an IFeedback, perPage, 
  * a custom list of objects, and a custom buttons panel.
  * 
  * @author Phil Kulak
  */
 public class GridPanel extends Panel
 {
 	private List columns;
 
 	private IDataSource dataSource;
 	
 	private int perPage;
 
 	/**
 	 * @param id
 	 *            the id of this panel
 	 */
 	public GridPanel(String id, IDataSource dataSource)
 	{
 		this(id, dataSource, 15, null, null);
 	}
 
 	/**
 	 * @param id
 	 *            the id of this panel
 	 * @param columns
 	 *            a custom list of columns
 	 */
 	public GridPanel(String id, IDataSource dataSource, int perPage, 
 			List columns, IFeedback feedback)
 	{
 		super(id);
 		
 		if (columns == null)
 		{
 			this.columns = makeColumns(dataSource.getFields());
 		}
 		else
 		{
 			this.columns = columns;
 		}
 		
 		this.dataSource = dataSource;
 		this.perPage = perPage;
 		add(new LocalGridView(feedback));
 	}
 
 	private class LocalGridView extends GridView
 	{
 
 		public LocalGridView(IFeedback feedback)
 		{
 			super("form", dataSource, feedback, perPage);
 			add(new Orderings(getListView()));
 		}
 
 		protected void populateItem(ListItem item)
 		{
 			item.add(new InlineDeleteLink("delete"));
 			item.add(new InlineEditLink("edit"));
 			item.add(new InlineSubmitButton("save"));
 			item.add(new Columns(item.getModel()));
 		}
 	}
 
 	private class Columns extends ListView
 	{
 		IModel model;
 
 		public Columns(IModel model)
 		{
 			super("columns", columns);
 			this.model = model;
 			setOptimizeItemRemoval(true);
 		}
 
 		protected void populateItem(ListItem item)
 		{
 			IColumn col = (IColumn) item.getModelObject();
 			Component component = col.getComponent("column",
 				new PropertyModel(model, col.getModelPath()));
 
 			component.setRenderBodyOnly(true);
 			item.add(component);
 		}
 	}
 
 	private class Orderings extends ListView
 	{
 		ListView mainList;
 
 		public Orderings(ListView mainList)
 		{
 			super("orderings", columns);
 			this.mainList = mainList;
 			setOptimizeItemRemoval(true);
 		}
 
 		protected void populateItem(ListItem item)
 		{
 			IColumn col = (IColumn) item.getModelObject();
 			String field = "e." + col.getOrderByPath();
 
 			if (col.allowOrderBy())
 			{
 				item.add(new OrderByPanel("orderBy", field, col.getDisplayName(),
 						mainList));
 			}
 			else
 			{
 				item.add(new LabelPanel("orderBy", col.getDisplayName()));
 			}
 		}
 	}
 
 	private String makeColHeading(String s)
 	{
 		return Strings.capitalize(s);
 	}
 
 	private List makeColumns(List fields)
 	{
		List cols = new ArrayList(fields.size());
 		for (Iterator i = fields.iterator(); i.hasNext();)
 		{
 			EntityField field = (EntityField) i.next();
			Class clazz = field.getClazz();
 			String name = field.getName();
 
 			if (clazz.isAssignableFrom(String.class))
 			{
				cols.add(new TextFieldColumn(makeColHeading(name), name));
 			}
 			else if (clazz.isAssignableFrom(Boolean.class))
 			{
				cols.add(new CheckBoxColumn(makeColHeading(name), name));
 			}
 			else
 			{
				cols.add(new LabelColumn(makeColHeading(name), name));
 			}
 		}
 
		return cols;
 	}
 }
