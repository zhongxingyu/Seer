 /*
 * Copyright (c) 2008-2011 The Negev Project
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
 
 import negev.giyusit.db.LookupTableModel;
 import negev.giyusit.util.CitiesCompleter;
 import negev.giyusit.util.DBColumnCompleter;
 import negev.giyusit.util.DirtyStateWatcher;
 import negev.giyusit.util.MessageDialog;
 import negev.giyusit.util.row.BasicRow;
 import negev.giyusit.util.row.Row;
 import negev.giyusit.widgets.DialogField;
 
 public class CandidateDialog extends QWidget {
 	
 	// Dialog Widgets
 	private QLabel id;
 	private QLineEdit nationalId;
 	private QLineEdit firstName;
 	private QLineEdit lastName;
 	private QLineEdit gender;
 	private QLineEdit address;
 	private QLineEdit city;
 	private QLineEdit zipCode;
 	private QLineEdit homePhone;
 	private QLineEdit cellPhone;
 	private QLineEdit email;
 	private QCheckBox wrongDetailsInd;
 		
 	private QLabel status;
 	private QLineEdit school;
 	private QLineEdit origin;
 	private QComboBox owner;
 	private QComboBox recruiter;
 	private QCheckBox signedDahashInd;
 	private QCheckBox canceledDahashInd;
 	private QTextEdit notes;
 		
 	// Buttons
 	private QPushButton candidateStatusesButton;
 	private QPushButton candidateEventsButton;
 	
 	private QToolButton saveButton;
 	private QToolButton resetButton;
 	private QPushButton closeButton;
 	
 	// Actions
 	private QAction saveAct;
 	private QAction resetAct;
 	
 	// Properties
 	private int candidateId = -1;
	private boolean dbModified = false;
 	private QDateTime firstEdit;
 	
 	private LookupTableModel staffModel;
 	private DirtyStateWatcher watcher;
 		
 	public CandidateDialog(QWidget parent) {
 		super(parent);
 		
 		watcher = new DirtyStateWatcher(this);
 		watcher.dirtyChanged.connect(this, "dirtyChanged(Boolean)");
 		
 		initActions();
 		initUI();
 	}
 	
 	public boolean isDbModified() {
 		return dbModified;
 	}
 	
 	public void showCandidate(int id) {
 		this.candidateId = id;
        this.dbModified = false;
 		
 		initComboModelsAndCompleters();
 		
 		updateWindowTitle();
 		loadCandidateData();
 	}
 	
 	@Override
 	protected void closeEvent(QCloseEvent e) {
 		if (watcher.isDirty()) {
 			MessageDialog.UserResponse result = MessageDialog.warnDirty(this, firstEdit);
 			
 			switch (result) {
 				case Save:
 					saveCandidateData();
 					e.accept();
 					break;
 				
 				case Discard:
 					e.accept();
 					break;
 				
 				default:
 					e.ignore();
 			}
 		}
 		else
 			e.accept();
 	}
 	
 	@Override
 	protected void keyPressEvent(QKeyEvent e) {
 		super.keyPressEvent(e);
 		
 		// Close the dialog when the escape key is pressed
 		if (e.key() == Qt.Key.Key_Escape.value())
 			close();
 	}
 	
 	private void initActions() {
 		saveAct = new QAction(tr("&Save"), this);
 		saveAct.setEnabled(false);
 		saveAct.setIcon(new QIcon("classpath:/icons/save.png"));
 		saveAct.setShortcut(new QKeySequence(QKeySequence.StandardKey.Save));
 		saveAct.triggered.connect(this, "saveCandidateData()");
 		
 		resetAct = new QAction(tr("&Reset"), this);
 		resetAct.setEnabled(false);
 		resetAct.setIcon(new QIcon("classpath:/icons/revert.png"));
 		resetAct.setShortcut(new QKeySequence("Ctrl+R"));
 		resetAct.triggered.connect(this, "loadCandidateData()");
 	}
 		
 	private void initUI() {
 		setWindowFlags(Qt.WindowType.Dialog);
 		setWindowModality(Qt.WindowModality.ApplicationModal);
 		
 		//
 		// Widgets
 		//
 		id = new QLabel();
 			
 		nationalId = new QLineEdit();
 		watcher.watchWidget(nationalId);
 			
 		firstName = new QLineEdit();
 		watcher.watchWidget(firstName);
 			
 		lastName = new QLineEdit();
 		watcher.watchWidget(lastName);
 			
 		gender = new QLineEdit();
 		gender.setMaximumWidth(15);
 		watcher.watchWidget(gender);
 			
 		address = new QLineEdit();
 		watcher.watchWidget(address);
 			
 		city = new QLineEdit();
 		watcher.watchWidget(city);
 			
 		zipCode = new QLineEdit();
 		watcher.watchWidget(zipCode);
 			
 		homePhone = new QLineEdit();
 		watcher.watchWidget(homePhone);
 			
 		cellPhone = new QLineEdit();
 		watcher.watchWidget(cellPhone);
 			
 		email = new QLineEdit();
 		email.setLayoutDirection(Qt.LayoutDirection.LeftToRight);
 		watcher.watchWidget(email);
 		
 		wrongDetailsInd = new QCheckBox(tr("Wrong Details"));
 		watcher.watchWidget(wrongDetailsInd);
 			
 		status = new QLabel();
 		watcher.watchWidget(status);
 		
 		school = new QLineEdit();
 		watcher.watchWidget(school);
 		
 		origin = new QLineEdit();
 		watcher.watchWidget(origin);
 			
 		owner = new QComboBox();
 		watcher.watchWidget(owner);
 			
 		recruiter = new QComboBox();
 		watcher.watchWidget(recruiter);
 			
 		signedDahashInd = new QCheckBox(tr("Signed Dahash"));
 		watcher.watchWidget(signedDahashInd);
 			
 		canceledDahashInd = new QCheckBox(tr("Canceled Dahash"));
 		watcher.watchWidget(canceledDahashInd);
 			
 		notes = new QTextEdit();
 		notes.setMaximumHeight(60);
 		watcher.watchWidget(notes);
 		
 		candidateStatusesButton = new QPushButton(tr("Candidate Statuses"));
 		candidateStatusesButton.clicked.connect(this, "candidateStatuses()");
 		
 		candidateEventsButton = new QPushButton(tr("Candidate Events"));
 		candidateEventsButton.clicked.connect(this, "candidateEvents()");
 			
 		saveButton = new QToolButton();
 		saveButton.setDefaultAction(saveAct);
 		saveButton.setToolButtonStyle(Qt.ToolButtonStyle.ToolButtonTextBesideIcon);
 			
 		resetButton = new QToolButton();
 		resetButton.setDefaultAction(resetAct);
 		resetButton.setToolButtonStyle(Qt.ToolButtonStyle.ToolButtonTextBesideIcon);
 			
 		closeButton = new QPushButton(tr("Close"));
 		closeButton.setIcon(new QIcon("classpath:/icons/close.png"));
 		closeButton.clicked.connect(this, "close()");
 		
 			
 		//
 		// Layout
 		//
 		QHBoxLayout personalInfoTopLayout = new QHBoxLayout();
 		personalInfoTopLayout.addWidget(new DialogField(tr("ID: "), id));
 		personalInfoTopLayout.addWidget(new DialogField(tr("National ID: "), nationalId));
 		personalInfoTopLayout.addWidget(new DialogField(tr("First Name: "), firstName));
 		personalInfoTopLayout.addWidget(new DialogField(tr("Last Name: "), lastName));
 		personalInfoTopLayout.addWidget(new DialogField(tr("Gender: "), gender));
 		
 		QGroupBox addressBox = new QGroupBox(tr("Address"));
 		
 		QGridLayout addressLayout = new QGridLayout(addressBox);
 		addressLayout.addWidget(new DialogField(tr("Address: "), address),	1, 1, 1, 2);
 		addressLayout.addWidget(new DialogField(tr("City: "), city),		2, 1);
 		addressLayout.addWidget(new DialogField(tr("Zip Code: "), zipCode),	2, 2);
 		
 		QGroupBox contactBox = new QGroupBox(tr("Contact Details"));
 		
 		QFormLayout contactLayout = new QFormLayout(contactBox);
 		contactLayout.addRow(tr("Home Phone: "),	homePhone);
 		contactLayout.addRow(tr("Cell Phone: "),	cellPhone);
 		contactLayout.addRow(tr("E-Mail: "), 		email);
 		
 		QHBoxLayout helperLayout1 = new QHBoxLayout();
 		helperLayout1.addWidget(addressBox);
 		helperLayout1.addWidget(contactBox);
 		
 		QHBoxLayout personalInfoBottomLayout = new QHBoxLayout();
 		personalInfoBottomLayout.addWidget(wrongDetailsInd);
 		personalInfoBottomLayout.addStretch(1);
 		
 		QGroupBox personalInfoBox = new QGroupBox(tr("Personal Info"));
 		
 		QVBoxLayout personalInfoLayout = new QVBoxLayout(personalInfoBox);
 		personalInfoLayout.addLayout(personalInfoTopLayout);
 		personalInfoLayout.addLayout(helperLayout1);
 		personalInfoLayout.addLayout(personalInfoBottomLayout);
 		
 		//--------------------------------------------------------------
 		
 		QGroupBox giyusDataBox = new QGroupBox(tr("Giyus Data"));
 		
 		QGridLayout giyusDataLayout = new QGridLayout(giyusDataBox);
 		giyusDataLayout.addWidget(new DialogField(tr("School: "), school), 1, 1);
 		giyusDataLayout.addWidget(new DialogField(tr("Owner: "), owner),   2, 1);
 		giyusDataLayout.addWidget(new DialogField(tr("Origin: "), origin), 1, 2);
 		giyusDataLayout.addWidget(new DialogField(tr("Recruiter: "), recruiter), 2, 2);
 		
 		QGroupBox giyusIndBox = new QGroupBox(tr("Indicators"));
 		
 		QVBoxLayout giyusIndLayout = new QVBoxLayout(giyusIndBox);
 		giyusIndLayout.addWidget(signedDahashInd);
 		giyusIndLayout.addWidget(canceledDahashInd);
 		giyusIndLayout.addStretch(1);
 		
 		giyusDataLayout.addWidget(giyusIndBox, 1, 3, 2, 1);
 		
 		QHBoxLayout giyusBottomLayout = new QHBoxLayout();
 		giyusBottomLayout.setMargin(0);
 		giyusBottomLayout.addWidget(new DialogField(tr("Status: "), status));
 		giyusBottomLayout.addStretch(1);
 		giyusBottomLayout.addWidget(candidateStatusesButton);
 		giyusBottomLayout.addWidget(candidateEventsButton);
 		
 		giyusDataLayout.addLayout(giyusBottomLayout, 3, 1, 1, 3);
 		
 		//--------------------------------------------------------------
 		
 		QGroupBox notesBox = new QGroupBox(tr("Notes"));
 		
 		QVBoxLayout notesLayout = new QVBoxLayout(notesBox);
 		notesLayout.addWidget(notes);
 		
 		//--------------------------------------------------------------
 		
 		QHBoxLayout buttonLayout = new QHBoxLayout();
 		buttonLayout.addWidget(saveButton);
 		buttonLayout.addWidget(resetButton);
 		buttonLayout.addStretch(1);
 		buttonLayout.addWidget(closeButton);
 		
 		QVBoxLayout layout = new QVBoxLayout(this);
 		layout.addWidget(personalInfoBox);
 		layout.addWidget(giyusDataBox);
 		layout.addWidget(notesBox);
 		layout.addLayout(buttonLayout);
 	}
 	
 	private void initComboModelsAndCompleters() {
 		// Lookup models
 		if (staffModel == null) {
 			staffModel = new LookupTableModel("Staff", "ID", "Name", "RealInd = 'true'");
 			
 			owner.setModel(staffModel);
 			owner.setModelColumn(LookupTableModel.VALUE_COLUMN);
 			
 			recruiter.setModel(staffModel);
 			recruiter.setModelColumn(LookupTableModel.VALUE_COLUMN);
 		}
 		else
 			staffModel.refresh();
 		
 		// Completers
 		city.setCompleter(new CitiesCompleter());
 		school.setCompleter(new DBColumnCompleter("Candidates", "School"));
 		origin.setCompleter(new DBColumnCompleter("Candidates", "Origin"));
 	}
 	
 	private void loadCandidateData() {
 		CandidateHelper helper = new CandidateHelper();
 		
 		try {
 			Row candidate = helper.fetchById(candidateId);
 			
 			// Text fields
 			id.setText(candidate.getString("ID"));
 			nationalId.setText(candidate.getString("NationalID"));
 			firstName.setText(candidate.getString("FirstName"));
 			lastName.setText(candidate.getString("LastName"));
 			gender.setText(candidate.getString("Gender"));
 			address.setText(candidate.getString("Address"));
 			city.setText(candidate.getString("City"));
 			zipCode.setText(candidate.getString("ZipCode"));
 			homePhone.setText(candidate.getString("HomePhone"));
 			cellPhone.setText(candidate.getString("CellPhone"));
 			email.setText(candidate.getString("EMail"));
 			school.setText(candidate.getString("School"));
 			origin.setText(candidate.getString("Origin"));
 			
 			notes.setPlainText(candidate.getString("Notes"));
 			
 			// Check boxes
 			wrongDetailsInd.setChecked(candidate.getBoolean("WrongDetailsInd"));
 			signedDahashInd.setChecked(candidate.getBoolean("SignedDahashInd"));
 			canceledDahashInd.setChecked(candidate.getBoolean("CanceledDahashInd"));
 			
 			// Combo boxes
 			owner.setCurrentIndex(
 					staffModel.keyToRow(candidate.getString("OwnerID")));
 			
 			recruiter.setCurrentIndex(
 					staffModel.keyToRow(candidate.getString("RecruiterID")));
 			
 			// Status
 			status.setText(helper.candidateStatusName(candidateId));
 			
 			//
 			watcher.setDirty(false);
 		}
 		catch (Exception e) {
 			MessageDialog.showException(this, e);
 		}
 		finally {
 			helper.close();
 		}
 	}
 	
 	private void saveCandidateData() {
 		// Check to see if the user didn't try to delete the fist name
 		if (firstName.text().trim().isEmpty()) {
 			MessageDialog.showUserError(this, tr("First name can't be empty"));
 			firstName.setFocus();
 			return;
 		}
 		
 		CandidateHelper helper = new CandidateHelper();
 		
 		try {
 			Row candidate = new BasicRow();
 			
 			// Text fields
 			candidate.put("NationalID", nationalId.text());
 			candidate.put("FirstName", firstName.text());
 			candidate.put("LastName", lastName.text());
 			candidate.put("Gender", gender.text());
 			candidate.put("Address", address.text());
 			candidate.put("City", city.text());
 			candidate.put("ZipCode", zipCode.text());
 			candidate.put("HomePhone", homePhone.text());
 			candidate.put("CellPhone", cellPhone.text());
 			candidate.put("EMail", email.text());
 			candidate.put("School", school.text());
 			candidate.put("Origin", origin.text());
 			
 			candidate.put("Notes", notes.toPlainText());
 			
 			// Check boxes
 			candidate.put("WrongDetailsInd", wrongDetailsInd.isChecked());
 			candidate.put("SignedDahashInd", signedDahashInd.isChecked());
 			candidate.put("CanceledDahashInd", canceledDahashInd.isChecked());
 			
 			// Combo boxes
 			candidate.put("OwnerID", staffModel.rowToKey(owner.currentIndex()));
 			candidate.put("RecruiterID", staffModel.rowToKey(recruiter.currentIndex()));
 			
 			// Update DB
 			helper.updateRecord(candidateId, candidate);
 			
 			dbModified = true;
 			watcher.setDirty(false);
 		}
 		catch (Exception e) {
 			MessageDialog.showException(this, e);
 		}
 		finally {
 			helper.close();
 		}
 	}
 	
 	private void updateWindowTitle() {
 		setWindowTitle(firstName.text() + " " + lastName.text() + "[*]");
 	}
 	
 	private void dirtyChanged(Boolean dirty) {
 		updateWindowTitle();
 		setWindowModified(dirty);
 		
 		saveAct.setEnabled(dirty);
 		resetAct.setEnabled(dirty);
 		
 		if (dirty && firstEdit == null)
 			firstEdit = QDateTime.currentDateTime();
 		else if (!dirty)
 			firstEdit = null;
 	}
 	
 	private void updateCandidateStatus() {
 		CandidateHelper helper = new CandidateHelper();
 		
 		try {
 			status.setText(helper.candidateStatusName(candidateId));
 		}
 		catch (Exception e) {
 			MessageDialog.showException(this, e);
 		}
 		finally {
 			helper.close();
 		}
 	}
 	
 	private void candidateStatuses() {
 		CandidateStatusesDialog dlg = new CandidateStatusesDialog(this, candidateId);
 		
 		dlg.exec();
 		
 		if (dlg.isDbModified()) {
 			dbModified = true;
 			updateCandidateStatus();
 		}
 	}
 	
 	private void candidateEvents() {
 		CandidateEventsDialog dlg = new CandidateEventsDialog(this, candidateId);
 		
 		dlg.resize((int) (dlg.width() * 1.1), dlg.height());
 		dlg.exec();
 	}
 }
