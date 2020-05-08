 package de.oose.taskboard.client.widget;
 
 import com.google.gwt.cell.client.AbstractCell;
 import com.google.gwt.core.client.GWT;
import com.google.gwt.safecss.shared.SafeStyles;
import com.google.gwt.safecss.shared.SafeStylesUtils;
 import com.google.gwt.safehtml.client.SafeHtmlTemplates;
 import com.google.gwt.safehtml.shared.SafeHtml;
 import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
 import com.google.gwt.safehtml.shared.SafeHtmlUtils;
 import com.google.gwt.user.cellview.client.CellList;
 import com.google.gwt.user.client.ui.Label;
 
 import de.oose.taskboard.shared.bo.TaskBO;
 import de.oose.taskboard.shared.enums.TaskVisibility;
 
 public class TaskCellList extends CellList<TaskBO> {
 
 	public TaskCellList() {
 		super(new TaskCell());
 		Label emptyLabel = new Label("nothing to do");
 		emptyLabel.setStylePrimaryName("nothingToDoLabel");
 		setEmptyListWidget(emptyLabel);
 	}
 
 	static class TaskCell extends AbstractCell<TaskBO> {
 
 		interface Templates extends SafeHtmlTemplates {
 			@SafeHtmlTemplates.Template("<div style='{0}'><div class='tasktitle'><b>{1}</b></div><div class='taskdesc'>{2}</div></div>")
			SafeHtml cell(SafeStyles style, SafeHtml title, SafeHtml description);
 		}
 
 		private static Templates templates = GWT.create(Templates.class);
 
 		@Override
 		public void render(com.google.gwt.cell.client.Cell.Context context,
 				TaskBO value, SafeHtmlBuilder sb) {
 			if (value == null)
 				return;
 			SafeHtml title = SafeHtmlUtils.fromString(value.getTitle());
 			SafeHtml desc = SafeHtmlUtils.fromString(value.getDescription());
			final String style = computeStyle(value);
			SafeHtml html = templates.cell(SafeStylesUtils.fromTrustedString(style), title, desc);
 			sb.append(html);
 
 		}
 
		private String computeStyle(final TaskBO value) {
			if (value.getVisibility().equals(TaskVisibility.PRIVATE)) {
				return "background-color: #D3D3D3;";
			} else
				return "";
		}
 	}
	
	
 }
