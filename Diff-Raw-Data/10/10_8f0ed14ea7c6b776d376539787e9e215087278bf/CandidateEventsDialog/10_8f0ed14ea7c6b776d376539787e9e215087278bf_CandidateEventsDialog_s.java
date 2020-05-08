 /*
  * Copyright (c) 2008-2009 The Negev Project
  *
  * Redistribution and use in source and binary forms, with or without 
  * modification, are permitted provided that the following conditions are met:
  *
  * - Redistributions of source code must retain the above copyright notice, 
  *   this list of conditions and the following disclaimer.
  *
  * - Redistributions in binary form must reproduce the above copyright notice, 
  *   this list of conditions and the following disclaimer in the documentation 
  *   and/or other materials provided with the distribution.
  *
  * - Neither the name of The Negev Project nor the names of its contributors 
  *   may be used to endorse or promote products derived from this software 
  *   without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE 
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
  * POSSIBILITY OF SUCH DAMAGE.
  */
 package negev.giyusit.candidates;
 
 import com.trolltech.qt.core.*;
 import com.trolltech.qt.gui.*;
 
 import java.text.MessageFormat;
 import java.util.List;
 
 import negev.giyusit.DataTable;
 import negev.giyusit.db.LookupTableModel;
 import negev.giyusit.events.EventAttendanceItemDialog;
 import negev.giyusit.events.EventHelper;
 import negev.giyusit.util.DBValuesTranslator;
 import negev.giyusit.util.MessageDialog;
 import negev.giyusit.util.Row;
 import negev.giyusit.util.RowSetModel;
 import negev.giyusit.widgets.DialogField;
 
 public class CandidateEventsDialog extends QDialog {
 
 	// Widgets
 	private DataTable dataTable;
 	
 	// Buttons
 	private QPushButton addToEventButton;
 	private QPushButton removeFromEventButton;
 	
 	// Properties
 	private RowSetModel model;
 	
 	private int candidateId;
 
 	public CandidateEventsDialog(QWidget parent, int candidateId) {
 		super(parent);
 		
 		this.candidateId = candidateId;
 		
 		// Models
 		model = new RowSetModel(new String[] 
 			{"ID", "Name", "Type", "StartDate", "EndDate", "AttType", "Notes"});
 		
 		DBValuesTranslator.translateModelHeaders(model);
 		
 		initUI();
 		updateTitle();
 		loadData();
 	}
 	
 	private void initUI() {
 		// Widgets
 		dataTable = new DataTable();
 		dataTable.setFilterEnabled(false);
 		dataTable.setModel(model);
 		
 		dataTable.selectionModel().selectionChanged.connect(
 				this, "selectionChanged()");
 		
 		dataTable.internalView().activated.connect(
 				this, "updateAttendance(QModelIndex)");
 		
 		// Buttons
 		addToEventButton = new QPushButton(tr("Add to Event..."));
 		addToEventButton.setIcon(new QIcon("classpath:/icons/add.png"));
 		addToEventButton.clicked.connect(this, "addToEvent()");
 		
 		removeFromEventButton = new QPushButton(tr("Remove from Event"));
 		removeFromEventButton.setEnabled(false);
 		removeFromEventButton.setIcon(new QIcon("classpath:/icons/remove.png"));
 		removeFromEventButton.clicked.connect(this, "removeFromEvent()");
 		
 		QPushButton closeButton = new QPushButton(tr("Close"));
 		closeButton.setIcon(new QIcon("classpath:/icons/close.png"));
 		closeButton.clicked.connect(this, "close()");
 		
 		// A button the steals the default action
 		QPushButton defaultStealer = new QPushButton(this);
 		defaultStealer.setVisible(false);
 		defaultStealer.setAutoDefault(true);
 		defaultStealer.setDefault(true);
 		
 		// Layout
 		QHBoxLayout buttonLayout = new QHBoxLayout();
 		buttonLayout.addWidget(addToEventButton);
 		buttonLayout.addWidget(removeFromEventButton);
 		buttonLayout.addStretch(1);
 		buttonLayout.addWidget(closeButton);
 		
 		QVBoxLayout layout = new QVBoxLayout(this);
 		layout.addWidget(dataTable, 1);
 		layout.addLayout(buttonLayout);
 	}
 	
 	private void loadData() {
 		CandidateHelper helper = new CandidateHelper();
 		
 		try {
 			model.setData(helper.getCandidateEvents(candidateId));
 			dataTable.setModel(model);
 		}
 		catch (Exception e) {
 			MessageDialog.showException(this, e);
 		}
 		finally {
 			helper.close();
 		}
 	}
 	
 	private void updateTitle() {
 		CandidateHelper helper = new CandidateHelper();
 		
 		try {
 			String title = tr("Events for candidate {0}");
 			
 			setWindowTitle(MessageFormat.format(title, 
 									helper.candidateFullName(candidateId)));
 		}
 		catch (Exception e) {
 			MessageDialog.showException(this, e);
 		}
 		finally {
 			helper.close();
 		}
 	}
 	
 	private void selectionChanged() {
 		removeFromEventButton.setEnabled(
 				dataTable.selectionModel().hasSelection());
 	}
 	
 	private void addToEvent() {
 		// Show event picker dialog
 		EventPickerDialog dlg = new EventPickerDialog(this);
 		
 		if (dlg.exec() == QDialog.DialogCode.Rejected.value())
 			return;
 		
 		// Add candidate to the selected event
 		EventHelper helper = new EventHelper();
 		
 		try {
 			int selectedEvent = dlg.getSelectedEventID();
 			
 			helper.addEventAttendance(selectedEvent, candidateId);
 			loadData();
 		}
 		catch (Exception e) {
 			MessageDialog.showException(this, e);
 		}
 		finally {
 			helper.close();
 		}
 	}
 	
 	private void removeFromEvent() {
 		// Get the selected event
 		List<QModelIndex> selection = dataTable.selectionModel().selectedRows();
 		
 		if (selection.isEmpty() || selection.get(0) == null)
 			return;
 		
 		QModelIndex index = selection.get(0);
 		int eventId = Integer.parseInt(index.data().toString());
 		
 		// Remove this attendance
 		EventHelper helper = new EventHelper();
 		
 		try {
 			helper.deleteEventAttendance(eventId, candidateId);
 			
 			dataTable.selectionModel().clearSelection();
 			loadData();
 		}
 		catch (Exception e) {
 			MessageDialog.showException(this, e);
 		}
 		finally {
 			helper.close();
 		}
 	}
 	
 	private void updateAttendance(QModelIndex index) {
 		if (index == null)
 			return;
 		
 		// Extract event ID
 		int eventId = Integer.parseInt(index.model().data(index.row(), 0).toString());
 		
 		// Show dialog
 		EventAttendanceItemDialog dlg = new EventAttendanceItemDialog(this);
 		EventHelper helper = new EventHelper();
 		
 		try {
 			dlg.fromRow(helper.getAttendanceRow(eventId, candidateId));
 				
 			if (dlg.exec() == QDialog.DialogCode.Rejected.value())
 				return;
 			
 			// Update
 			helper.updateAttendanceRow(eventId, candidateId, dlg.toRow());
 			loadData();
 		}
 		catch (Exception e) {
 			MessageDialog.showException(this, e);
 		}
 		finally {
 			helper.close();
 		}
 	}
 }
 
 class EventPickerDialog extends QDialog {
 	
 	// Widgets
 	private QComboBox events;
 	private QLabel type;
 	private QLabel startDate;
 	
 	// Buttons
 	private QPushButton okButton;
 	
 	// Fields
 	private LookupTableModel eventsModel;
 	private LookupTableModel eventTypesModel;
 	private int selectedEventId = -1;
 	
 	public EventPickerDialog(QWidget parent) {
 		super(parent);
 		
 		initUI();
 		
 		// Setup combo
 		eventsModel = new LookupTableModel("Events", "ID", "Name");
 		
 		events.setModel(eventsModel);
 		events.setModelColumn(LookupTableModel.VALUE_COLUMN);
 		events.setCurrentIndex(-1);
 		events.currentIndexChanged.connect(this, "currentEventChanged(int)");
 		
 		//
 		eventTypesModel = new LookupTableModel("EventTypes");
 	}
 	
 	public int getSelectedEventID() {
 		return selectedEventId;
 	}
 	
 	private void initUI() {
 		setWindowTitle(tr("Add to Event"));
 		
 		//
 		// Widgets
 		//
 		events = new QComboBox();
 		
 		type = new QLabel();
 		startDate = new QLabel();
 		
 		okButton = new QPushButton(tr("OK"));
 		okButton.clicked.connect(this, "accept()");
 		
 		QPushButton cancelButton = new QPushButton(tr("Cancel"));
 		cancelButton.clicked.connect(this, "reject()");
 				
 		//
 		// Layout
 		//
 		QHBoxLayout buttonLayout = new QHBoxLayout();
 		buttonLayout.addStretch(1);
 		buttonLayout.addWidget(okButton);
 		buttonLayout.addWidget(cancelButton);
 				
 		QGridLayout layout = new QGridLayout(this);
 		layout.addWidget(new DialogField(tr("<b>Event:</b> "), events), 1, 1, 1, 2);
 		layout.addWidget(new DialogField(tr("<b>Type: </b>"), type), 2, 1);
 		layout.addWidget(new DialogField(tr("<b>Start Date: </b>"), startDate), 2, 2);
 		layout.setRowMinimumHeight(3, 5);
 		layout.addLayout(buttonLayout, 4, 1, 1, 2);
 	}
 	
 	private void currentEventChanged(int index) {
 		if (index == -1) {
 			okButton.setEnabled(false);
 			return;
 		}
 		
 		okButton.setEnabled(true);
 		
 		// Show event details
 		EventHelper helper = new EventHelper();
 		
 		try {
 			int id = Integer.parseInt(eventsModel.rowToKey(index));
 			selectedEventId = id;
 			
 			System.out.println(id);
 			
 			Row eventRow = helper.fetchById(id);
 			
 			// Type
 			String typeId = eventRow.getString("TypeID");
 			type.setText(eventTypesModel.data(
 					eventTypesModel.keyToRow(typeId), 
 					LookupTableModel.VALUE_COLUMN).toString());
 			
 			// Date
 			QDate date = eventRow.getDate("StartDate");
 			startDate.setText(date.toString("dd/MM/yyyy"));
 		}
 		catch (Exception e) {
 			MessageDialog.showException(this, e);
 		}
 		finally {
 			helper.close();
 		}
 	}
 }
