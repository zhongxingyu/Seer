 package org.wm.timebox.app.screen.timeline;
 
 import javafx.collections.ObservableList;
 import javafx.scene.control.TableColumn;
 import javafx.scene.control.TableView;
 import javafx.scene.control.cell.PropertyValueFactory;
 
 import org.wm.timebox.app.model.TimelineItemVO;
 import org.wm.timebox.app.screen.mvc.DataEntryContext;
 import org.wm.timebox.app.screen.mvc.View;
 
 public class TimelineView extends View<DataEntryContext<ObservableList<TimelineItemVO>>> {
 
 	private TableView<TimelineItemVO> cTimelineTable = new TableView<TimelineItemVO>();
 
 	public TimelineView() {
 		super();
 	}
 
 	private void initControls() {
 		cTimelineTable.setItems(getController().getModel().getModel());
 
		TableColumn<TimelineItemVO, String> tempColumn = new TableColumn<TimelineItemVO, String>("Start");
 		tempColumn.setCellValueFactory(new PropertyValueFactory<TimelineItemVO, String>("start"));
 		cTimelineTable.getColumns().add(tempColumn);
 
		tempColumn = new TableColumn<TimelineItemVO, String>("End");
 		tempColumn.setCellValueFactory(new PropertyValueFactory<TimelineItemVO, String>("end"));
 		cTimelineTable.getColumns().add(tempColumn);
 
 		setCenter(cTimelineTable);
 	}
 
 	@Override
 	public void init() {
 		super.init();
 		initControls();
 	}
 }
