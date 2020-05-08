 /**
  * 
  */
 package nz.ac.vuw.ecs.rprofs.client;
 
 import nz.ac.vuw.ecs.rprofs.client.data.Report;
 import nz.ac.vuw.ecs.rprofs.client.data.Report.ClassEntry;
 import nz.ac.vuw.ecs.rprofs.client.data.Report.EntryVisitor;
 import nz.ac.vuw.ecs.rprofs.client.data.Report.InstanceEntry;
 import nz.ac.vuw.ecs.rprofs.client.data.Report.PackageEntry;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.HasClickHandlers;
 import com.google.gwt.event.shared.HandlerRegistration;
 import com.google.gwt.resources.client.CssResource;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiFactory;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.InlineHTML;
 import com.google.gwt.user.client.ui.Panel;
 import com.google.gwt.user.client.ui.Widget;
 
 /**
  * @author Stephen Nelson (stephen@sfnelson.org)
  *
  */
 public class Entry extends Composite implements HasClickHandlers, HasEntries, EntryVisitor<Void> {
 
 	interface EntryUiBinder extends UiBinder<Widget, Entry> {}
 
 	private EntryUiBinder uiBinder = GWT.create(EntryUiBinder.class);
 
 	interface Style extends CssResource {
 		String string();
 		String object();
 		String count();
 		String flag();
 		String open();
 		String number();
 	}
 
 	@UiField Style style;
 
 	@UiField EntryPanel entries;
 	@UiField Panel children;
 
 	private final Report report;
 	private final Report.Entry target;
 	private final HTML[] fields;
 
 	public Entry(Report report, Report.Entry entry) {
 		this.report = report;
 		this.target = entry;
 		this.fields = new HTML[report.headings.size()];
 
 		initWidget(uiBinder.createAndBindUi(this));
 		initFields();
 
 		entry.visit(this);
 	}
 
 	public Entry(Report report) {
 		this.report = report;
 		this.target = null;
 		this.fields = new HTML[report.headings.size()];
 
 		initWidget(uiBinder.createAndBindUi(this));
 		initFields();
 
 		for (int i = 0; i < report.headings.size(); i++) {
 			initField(report.types.get(i), report.headings.get(i), fields[i]);
 		}
 	}
 	
 	private void initFields() {
 		for (int i = 0; i < fields.length; i++) {
			HTML field = new InlineHTML();
 			switch (report.types.get(i)) {
 			case OBJECT: field.setStyleName(style.object()); break;
 			case COUNT: field.setStyleName(style.count()); break;
 			case NAME: field.setStyleName(style.string()); break;
 			case FLAG: field.setStyleName(style.flag()); break;
 			}			
 			entries.add(field);
 			fields[i] = field;
 		}
 	}
 
 	@Override
 	public Void visitClassEntry(ClassEntry entry) {
 		String name = entry.cls.getClassName();
 		visitEntry("", name, entry.instances, entry);
 		return null;
 	}
 
 	@Override
 	public Void visitInstanceEntry(InstanceEntry entry) {
 		visitEntry("", "", entry.id, entry);
 		return null;
 	}
 
 	@Override
 	public Void visitPackageEntry(PackageEntry entry) {
 		String pkg = entry.pkg;
 		if (pkg.equals("")) {
 			pkg = "<em>default</em>";
 		}
 		visitEntry(pkg, entry.classes, entry.instances, entry);
 		return null;
 	}
 
 	private void visitEntry(Object pkg, Object cls, Object id, Report.Entry entry) {
 		int index = 0;
 
 		if (report.headings.get(index).equals("Package")) {
 			initField(report.types.get(index), pkg, fields[index]);
 			index++;
 		}
 		if (report.headings.get(index).equals("Class")) {
 			initField(report.types.get(index), cls, fields[index]);
 			index++;
 		}
 		if (report.headings.get(index).equals("Instance")) {
 			initField(report.types.get(index), id, fields[index]);
 			index++;
 		}
 
 		for (Object value: entry.values) {
 			initField(report.types.get(index), value, fields[index]);
 			index++;
 		}
 	}
 
 	private void initField(Report.Type type, Object value, HTML field) {
 		setHTML(type, value, field);
 		setTitle(type, value, field);
 	}
 
 	public Report.Entry getTarget() {
 		return target;
 	}
 
 	public HandlerRegistration addClickHandler(ClickHandler handler) {
 		return entries.addClickHandler(handler);
 	}
 
 	@Override
 	public void add(Entry child) {
 		children.add(child);
 	}
 
 	private boolean open = false;
 
 	public boolean isOpen() {
 		return open;
 	}
 
 	public void open() {
 		open = true;
 		addStyleName(style.open());
 	}
 
 	public void close() {
 		open = false;
 		removeStyleName(style.open());
 	}
 
 	public void setPopulated(boolean value) {
 		populated = value;
 	}
 
 	public boolean isPopulated() {
 		return populated;
 	}
 
 	private boolean populated = false;
 
 	protected void setHTML(Report.Type type, Object value, HTML field) {
 		String html;
 		switch (type) {
 		case OBJECT:
 			if (value instanceof Long) {
 				long arg = (Long) value;
 				if (arg == 0) {
 					html = "<strong>null</strong>";
 				}
 				html = String.valueOf(arg>> 32) + "." + String.valueOf(arg & 0xffffffffl);
 				break;
 			}
 			html = String.valueOf(value);
 			break;
 		case COUNT:
 			if (value instanceof Integer) {
 				if (((Integer) value) == 0) {
 					html = "";
 					break;
 				}
 			}
 			html = String.valueOf(value);
 			break;
 		case NAME:
 			if (value instanceof Integer) {
 				field.addStyleName(style.number());
 			}
 			html = String.valueOf(value);
 			break;
 		default:
 			html = String.valueOf(value);
 			break;
 		}
 		field.setHTML(html);
 	}
 
 	protected void setTitle(Report.Type type, Object value, HTML field) {
 		String title;
 		switch (type) {
 		case OBJECT:
 			if (value instanceof Long) {
 				long arg = (Long) value;
 				if (arg == 0) {
 					title = "null";
 				}
 				title = String.valueOf(arg>> 32) + "." + String.valueOf(arg & 0xffffffffl);
 			}
 		default:
 			title = String.valueOf(value);
 		}
 		field.setTitle(title);
 	}
 
 	@UiFactory
 	EntryPanel createEntryPanel() {
 		return new EntryPanel(this);
 	}
 
 	static class EntryPanel extends FlowPanel implements HasClickHandlers {
 		private Entry parent;
 		public EntryPanel(Entry entry) {
 			this.parent = entry;
 		}
 		public Entry parent() {
 			return parent;
 		}
 		@Override
 		public HandlerRegistration addClickHandler(ClickHandler handler) {
 			return addDomHandler(handler, ClickEvent.getType());
 		}
 
 	}
 }
