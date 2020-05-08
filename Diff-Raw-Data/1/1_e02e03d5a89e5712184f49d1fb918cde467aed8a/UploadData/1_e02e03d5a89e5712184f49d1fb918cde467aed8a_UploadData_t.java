 package org.strasa.web.uploadstudy.view.model;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintWriter;
 import java.security.Timestamp;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.io.input.ReaderInputStream;
 import org.strasa.middleware.filesystem.manager.UserFileManager;
 import org.strasa.middleware.manager.GermplasmManagerImpl;
 import org.strasa.middleware.manager.ProgramManagerImpl;
 import org.strasa.middleware.manager.ProjectManagerImpl;
 import org.strasa.middleware.manager.StudyDerivedDataManagerImpl;
 import org.strasa.middleware.manager.StudyGermplasmManagerImpl;
 import org.strasa.middleware.manager.StudyRawDataManagerImpl;
 import org.strasa.middleware.manager.StudyTypeManagerImpl;
 import org.strasa.middleware.manager.StudyVariableManagerImpl;
 import org.strasa.middleware.model.Program;
 import org.strasa.middleware.model.Project;
 import org.strasa.middleware.model.Study;
 import org.strasa.middleware.model.StudyGermplasm;
 import org.strasa.middleware.model.StudyRawDataByDataColumn;
 import org.strasa.middleware.model.StudyType;
 import org.strasa.web.common.api.Encryptions;
 import org.strasa.web.common.api.ProcessTabViewModel;
 import org.strasa.web.uploadstudy.view.pojos.UploadCSVDataVariableModel;
 import org.strasa.web.utilities.FileUtilities;
 import org.zkoss.bind.BindContext;
 import org.zkoss.bind.BindUtils;
 import org.zkoss.bind.annotation.BindingParam;
 import org.zkoss.bind.annotation.Command;
 import org.zkoss.bind.annotation.ContextParam;
 import org.zkoss.bind.annotation.ContextType;
 import org.zkoss.bind.annotation.GlobalCommand;
 import org.zkoss.bind.annotation.Init;
 import org.zkoss.bind.annotation.NotifyChange;
 import org.zkoss.zk.ui.Component;
 import org.zkoss.zk.ui.Executions;
 import org.zkoss.zk.ui.event.UploadEvent;
 import org.zkoss.zul.Messagebox;
 import org.zkoss.zul.Window;
 
 import au.com.bytecode.opencsv.CSVReader;
 
 public class UploadData extends ProcessTabViewModel {
 
 	private List<String> columnList = new ArrayList<String>();
 	public String dataFileName;
 	private List<String[]> dataList = new ArrayList<String[]>();
 	private ArrayList<String> programList = new ArrayList<String>();
 	private ArrayList<String> projectList = new ArrayList<String>();
 	private ArrayList<String> studyTypeList = new ArrayList<String>();
 
 	private ArrayList<String> dataTypeList = new ArrayList<String>();
 	private ArrayList<GenotypeFileModel> genotypeFileList = new ArrayList<UploadData.GenotypeFileModel>();
 	private String txtProgram = new String();
 	private String txtProject = new String();
 
 	private String txtStudyName = new String();
 	private String txtStudyType = new String();
 	private int startYear = Calendar.getInstance().get(Calendar.YEAR);
 	private int endYear = Calendar.getInstance().get(Calendar.YEAR);
 	private int pageSize = 10;
 	private int activePage = 0;
 	private File tempFile;
 	private String uploadTo = "database";
 	private String studyType = "rawdata";
 
 	public ArrayList<GenotypeFileModel> getGenotypeFileList() {
 		return genotypeFileList;
 	}
 
 	public void setGenotypeFileList(
 			ArrayList<GenotypeFileModel> genotypeFileList) {
 		this.genotypeFileList = genotypeFileList;
 	}
 
 	public int getTotalSize() {
 		return dataList.size();
 	}
 
 	public Study getStudy() {
 		return study;
 	}
 
 	public int getStartYear() {
 		return startYear;
 	}
 
 	public void setStartYear(int startYear) {
 		this.startYear = startYear;
 	}
 
 	public int getEndYear() {
 		return endYear;
 	}
 
 	public void setEndYear(int endYear) {
 		this.endYear = endYear;
 	}
 
 	public void setStudy(Study study) {
 		this.study = study;
 	}
 
 	private String txtYear = "";
 
 	public int getPageSize() {
 		return pageSize;
 	}
 
 	@NotifyChange("*")
 	public void setPageSize(int pageSize) {
 		this.pageSize = pageSize;
 	}
 
 	@NotifyChange("*")
 	public int getActivePage() {
 
 		return activePage;
 	}
 
 	@NotifyChange("*")
 	public void setActivePage(int activePage) {
 		System.out.println("pageSize");
 		this.activePage = activePage;
 	}
 
 	public boolean isVariableDataVisible = false;
 
 	private Study study;
 
 	public List<UploadCSVDataVariableModel> varData = new ArrayList<UploadCSVDataVariableModel>();
 	private int userId = 1;
 
 	public ArrayList<String> getProgramList() {
 		return programList;
 	}
 
 	public void setProgramList(ArrayList<String> programList) {
 		this.programList = programList;
 	}
 
 	public String getUploadTo() {
 		return uploadTo;
 	}
 
 	public void setUploadTo(String uploadTo) {
 		this.uploadTo = uploadTo;
 	}
 
 	public String getStudyType() {
 		return studyType;
 	}
 
 	public void setStudyType(String studyType) {
 		this.studyType = studyType;
 	}
 
 	public ArrayList<String> getProjectList() {
 		return projectList;
 	}
 
 	public void setProjectList(ArrayList<String> projectList) {
 		this.projectList = projectList;
 	}
 
 	public ArrayList<String> getDataTypeList() {
 		return dataTypeList;
 	}
 
 	public void setDataTypeList(ArrayList<String> dataTypeList) {
 		this.dataTypeList = dataTypeList;
 	}
 
 	public ArrayList<String> getStudyTypeList() {
 
 		studyTypeList.clear();
 
 		for (StudyType studyType : new StudyTypeManagerImpl().getAllStudyType()) {
 			studyTypeList.add(studyType.getStudytype());
 		}
 		;
 		return studyTypeList;
 	}
 
 	public void setStudyTypeList(ArrayList<String> studyTypeList) {
 		this.studyTypeList = studyTypeList;
 	}
 
 	public String getTxtProgram() {
 		return txtProgram;
 	}
 
 	public void setTxtProgram(String txtProgram) {
 		this.txtProgram = txtProgram;
 	}
 
 	public String getTxtProject() {
 		return txtProject;
 	}
 
 	public void setTxtProject(String txtProject) {
 		this.txtProject = txtProject;
 	}
 
 	public String getTxtStudyName() {
 		return txtStudyName;
 	}
 
 	public void setTxtStudyName(String txtStudyName) {
 		this.txtStudyName = txtStudyName;
 	}
 
 	public String getTxtStudyType() {
 		return txtStudyType;
 	}
 
 	public void setTxtStudyType(String txtStudyType) {
 		this.txtStudyType = txtStudyType;
 	}
 
 	public String getTxtYear() {
 		return txtYear;
 	}
 
 	public void setTxtYear(String txtYear) {
 		this.txtYear = txtYear;
 	}
 
 	public List<String> getColumnList() {
 		return columnList;
 	}
 
 	public void setColumnList(List<String> columnList) {
 		this.columnList = columnList;
 	}
 
 	public List<String[]> getDataList() {
 		if (true)
 			return dataList;
 		ArrayList<String[]> pageData = new ArrayList<String[]>();
 		for (int i = activePage * pageSize; i < activePage * pageSize
 				+ pageSize; i++) {
 			pageData.add(dataList.get(i));
 			System.out.println(Arrays.toString(dataList.get(i)));
 		}
 
 		return pageData;
 	}
 
 	public ArrayList<ArrayList<String>> getCsvData() {
 		ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
 		if (dataList.isEmpty())
 			return result;
 		for (int i = activePage * pageSize; i < activePage * pageSize
 				+ pageSize
 				&& i < dataList.size(); i++) {
 			ArrayList<String> row = new ArrayList<String>();
 			row.addAll(Arrays.asList(dataList.get(i)));
 			result.add(row);
 			row.add(0, "  ");
 			System.out.println(Arrays.toString(dataList.get(i)) + "ROW: "
 					+ row.get(0));
 		}
 		return result;
 	}
 
 	public void setDataList(List<String[]> dataList) {
 		this.dataList = dataList;
 	}
 
 	public String getDataFileName() {
 		return dataFileName;
 	}
 
 	public boolean isVariableDataVisible() {
 		return isVariableDataVisible;
 	}
 
 	public void setVariableDataVisible(boolean isVariableDataVisible) {
 		this.isVariableDataVisible = isVariableDataVisible;
 	}
 
 	public List<UploadCSVDataVariableModel> getVarData() {
 		return varData;
 	}
 
 	@Command
 	@NotifyChange("variableData")
 	public void changeVar(
 			@ContextParam(ContextType.BIND_CONTEXT) BindContext ctx,
 			@ContextParam(ContextType.VIEW) Component view,
 			@BindingParam("oldVar") String oldVar) {
 
 		Map<String, Object> params = new HashMap<String, Object>();
 		System.out.println(oldVar);
 		params.put("oldVar", oldVar);
 		params.put("parent", view);
 
 		Window popup = (Window) Executions.createComponents(
 				DataColumnChanged.ZUL_PATH, view, params);
 
 		popup.doModal();
 	}
 
 	@NotifyChange("*")
 	@Command("uploadCSV")
 	public void uploadCSV(
 			@ContextParam(ContextType.BIND_CONTEXT) BindContext ctx,
 			@ContextParam(ContextType.VIEW) Component view) {
 
 		UploadEvent event = (UploadEvent) ctx.getTriggerEvent();
 
 		// System.out.println(event.getMedia().getStringData());
 
 		String name = event.getMedia().getName();
 		if (tempFile == null)
 			try {
 				tempFile = File.createTempFile(name, ".tmp");
 			} catch (IOException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 
 		if (!name.endsWith(".csv")) {
 			Messagebox.show("Error: File must be a text-based csv format",
 					"Upload Error", Messagebox.OK, Messagebox.ERROR);
 			return;
 		}
 
 		InputStream in = event.getMedia().isBinary() ? event.getMedia()
 				.getStreamData() : new ReaderInputStream(event.getMedia()
 				.getReaderData());
 		FileUtilities.uploadFile(tempFile.getAbsolutePath(), in);
 		BindUtils.postNotifyChange(null, null, this, "*");
 
 		ArrayList<String> invalidHeader = new ArrayList<String>();
 		boolean isHeaderValid = true;
 		try {
 			StudyVariableManagerImpl studyVarMan = new StudyVariableManagerImpl();
 			CSVReader reader = new CSVReader(new FileReader(
 					tempFile.getAbsolutePath()));
 			String[] header = reader.readNext();
 			for (String column : header) {
 				if (!studyVarMan.hasVariable(column)) {
 					invalidHeader.add(column);
 					isHeaderValid = false;
 				}
 			}
 			System.out.println(invalidHeader.size());
 
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		isVariableDataVisible = true;
 		dataFileName = name;
 		if (!isHeaderValid)
 			openCSVHeaderValidator(tempFile.getAbsolutePath(), false);
 		else
 			refreshCsv();
 
 	}
 
 	public void uploadFile(String path, String name, String data) {
 
 		String filePath = path + name;
 
 		try {
 			PrintWriter out = new PrintWriter(filePath);
 			out.println(data);
 			out.flush();
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	@Command("addProgram")
 	public void addProgram() {
 		Map<String, Object> params = new HashMap<String, Object>();
 
 		params.put("oldVar", null);
 		params.put("parent", getMainView());
 
 		Window popup = (Window) Executions.createComponents(
 				AddProgram.ZUL_PATH, getMainView(), params);
 
 		popup.doModal();
 	}
 
 	@Command("addProject")
 	public void addProject() {
 		Map<String, Object> params = new HashMap<String, Object>();
 
 		params.put("oldVar", null);
 		params.put("parent", getMainView());
 
 		Window popup = (Window) Executions.createComponents(
 				AddProject.ZUL_PATH, getMainView(), params);
 
 		popup.doModal();
 	}
 
 	@GlobalCommand
 	public void testGlobalCom(@BindingParam("newVal") double newVal) {
 		System.out.println("globalCom: " + newVal);
 	}
 
 	@Init
 	public void init(@ContextParam(ContextType.VIEW) Component view) {
 		setMainView(view);
 
 		refreshProgramList(null);
 		refreshProjectList(null);
 		System.out.println("LOADED");
 	}
 
 	public void openCSVHeaderValidator(String CSVPath, boolean showAll) {
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("CSVPath", CSVPath);
 		params.put("parent", getMainView());
 		params.put("showAll", showAll);
 		Window popup = (Window) Executions.createComponents(
 				DataColumnValidation.ZUL_PATH, getMainView(), params);
 
 		popup.doModal();
 	}
 
 	@NotifyChange("*")
 	@Command("refreshVarList")
 	public void refreshList(@BindingParam("newValue") String newValue,
 			@BindingParam("oldVar") String oldVar) {
 		for (int i = 0; i < varData.size(); i++) {
 
 			if (varData.get(i).getCurrentVariable().equals(oldVar)) {
 				System.out.println("   ss");
 				varData.get(i).setNewVariable(newValue);
 			}
 
 		}
 
 	}
 
 	@NotifyChange("*")
 	@Command("removeUpload")
 	public void removeUpload() {
 		isVariableDataVisible = false;
 		dataFileName = "";
 		varData.clear();
 	}
 
 	@NotifyChange("*")
 	@Command("refreshCsv")
 	public void refreshCsv() {
 		activePage = 0;
 		CSVReader reader;
 		try {
 			reader = new CSVReader(new FileReader(tempFile.getAbsolutePath()));
 			List<String[]> rawData = reader.readAll();
 			columnList.clear();
 			dataList.clear();
 			columnList = new ArrayList<String>(Arrays.asList(rawData.get(0)));
 			rawData.remove(0);
 			dataList = new ArrayList<String[]>(rawData);
 			System.out.println(Arrays.toString(dataList.get(0)));
 
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 
 	@NotifyChange("*")
 	@Command("refreshProgramList")
 	public void refreshProgramList(@BindingParam("selected") String selected) {
 
 		ProgramManagerImpl programMan = new ProgramManagerImpl();
 		programList.clear();
 		for (Program data : programMan.getProgramByUserId(1)) {
 			programList.add(data.getName());
 
 		}
 		System.out.print(selected);
 		txtProgram = selected;
 
 	}
 
 	@NotifyChange("*")
 	@Command("refreshProjectList")
 	public void refreshProjectList(@BindingParam("selected") String selected) {
 
 		ProjectManagerImpl programMan = new ProjectManagerImpl();
 		projectList.clear();
 		for (Project data : programMan.getProjectByUserId(1)) {
 			projectList.add(data.getName());
 
 		}
 		System.out.print(selected);
 		txtProject = selected;
 
 	}
 
 	@Override
 	public boolean validateTab() {
 		Runtimer timer = new Runtimer();
 		timer.start();
 		boolean isRawData =  studyType.equalsIgnoreCase("rawdata");
 		System.out.println("StudyType: " + studyType + " + " + isRawData);
 		if (txtProgram == null || txtProject == null || txtStudyName == null
 				|| txtStudyType == null) {
 			Messagebox.show("Error: All fields are required", "Upload Error",
 					Messagebox.OK, Messagebox.ERROR);
 
 			// TODO: must have message DIalog
 			return false;
 		}
 
 		if (txtProgram.isEmpty() || txtProject.isEmpty()
 				|| txtStudyName.isEmpty() || txtStudyType.isEmpty()
 				) {
 			Messagebox.show("Error: All fields are required", "Upload Error",
 					Messagebox.OK, Messagebox.ERROR);
 
 			// TODO: must have message DIalog
 			return false;
 		}
 		if (tempFile == null || !isVariableDataVisible) {
 			Messagebox.show("Error: You must upload a data first",
 					"Upload Error", Messagebox.OK, Messagebox.ERROR);
 
 			return false;
 		}
 		if(startYear < Calendar.getInstance().get(Calendar.YEAR)){
 			Messagebox.show("Error: Invalid start year. Year must be greater or equal than the present year(" + Calendar.getInstance().get(Calendar.YEAR) +  " )",
 					"Upload Error", Messagebox.OK, Messagebox.ERROR);
 
 			return false;
 		}
 		if(endYear < Calendar.getInstance().get(Calendar.YEAR)){
 			Messagebox.show("Error: Invalid end year. Year must be greater or equal than the present year(" + Calendar.getInstance().get(Calendar.YEAR) +  " )",
 					"Upload Error", Messagebox.OK, Messagebox.ERROR);
 
 			return false;
 		}
 
 		UserFileManager fileMan = new UserFileManager();
 		StudyRawDataManagerImpl studyRawData = new StudyRawDataManagerImpl(isRawData);
 		if (study == null) {
 			study = new Study();
 		}
 		study.setName(txtStudyName);
 		study.setStudytypeid(new StudyTypeManagerImpl().getStudyTypeByName(
 				txtStudyType).getId());
 		study.setProgramid(new ProgramManagerImpl().getProgramByName(
 				txtProgram, userId).getId());
 		study.setProjectid(new ProjectManagerImpl().getProjectByName(
 				txtProject, userId).getId());
 		study.setStartyear(String.valueOf(startYear));
 		study.setEndyear(String.valueOf(String.valueOf(endYear)));
 
 		if (uploadTo.equals("database")) {
 
 				
 
 				studyRawData.addStudyRawData(study,columnList.toArray(new String[columnList.size()]),dataList);
 //					studyRawData.addStudyRawDataByRawCsvList(study,
 //							new CSVReader(new FileReader(tempFile)).readAll());
 //					GermplasmManagerImpl germplasmManager = new GermplasmManagerImpl();
 //					StudyGermplasmManagerImpl studyGermplasmManager = new StudyGermplasmManagerImpl();
 //
 //					StudyRawDataManagerImpl studyRawDataManagerImpl = new StudyRawDataManagerImpl(isRawData);
 //					ArrayList<StudyRawDataByDataColumn> list = (ArrayList<StudyRawDataByDataColumn>) studyRawDataManagerImpl
 //							.getStudyRawDataColumn(study.getId(), "GName");
 //					for (StudyRawDataByDataColumn s : list) {
 //						// System.out.println(s.getStudyid()+
 //						// " "+s.getDatacolumn()+
 //						// " "+ s.getDatavalue());
 //
 //						if (!germplasmManager.isGermplasmExisting(s
 //								.getDatavalue())) {
 //							StudyGermplasm studyGermplasmData = new StudyGermplasm();
 //							studyGermplasmData.setGermplasmname(s
 //									.getDatavalue());
 //							studyGermplasmData.setStudyid(study.getId());
 //							studyGermplasmManager
 //									.addStudyGermplasm(studyGermplasmData);
 //						}
 
 
 				 
 				
 		
 		}
 		else{
 			fileMan.createNewFileFromUpload(1, study.getId(), dataFileName, tempFile, (isRaw) ? "rd":"dd");
 	 
 		}
 		for(GenotypeFileModel genoFile : genotypeFileList){
 			fileMan.createNewFileFromUpload(1, study.getId(), genoFile.name, genoFile.tempFile,"gd");
 		}
 		this.setStudyID(study.getId());
 		this.isRaw = isRawData;
 		System.out.println("Timer ends in: " + timer.end());
 		
 		return true;
 
 	}
 
 	@NotifyChange("genotypeFileList")
 	@Command("uploadGenotypeData")
 	public void uploadGenotypeData(
 			@ContextParam(ContextType.BIND_CONTEXT) BindContext ctx,
 			@ContextParam(ContextType.VIEW) Component view) {
 
 		UploadEvent event = (UploadEvent) ctx.getTriggerEvent();
 
 		// System.out.println(event.getMedia().getStringData());
 
 		String name = event.getMedia().getName();
 		if (!name.endsWith(".txt")) {
 			Messagebox.show("Error: File must be a text-based  format",
 					"Upload Error", Messagebox.OK, Messagebox.ERROR);
 			return;
 		}
 
 		try {
 			String filename = name
 					+ Encryptions.encryptStringToNumber(name,
 							new Date().getTime());
 			File tempGenoFile = File.createTempFile(filename, ".tmp");
 			InputStream in = event.getMedia().isBinary() ? event.getMedia()
 					.getStreamData() : new ReaderInputStream(event.getMedia()
 					.getReaderData());
 			FileUtilities.uploadFile(tempGenoFile.getAbsolutePath(), in);
 
 			GenotypeFileModel newGenotypeFile = new GenotypeFileModel(name,
 					tempGenoFile);
 			genotypeFileList.add(newGenotypeFile);
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 
 	}
 
 	@Command
 	public void modifyDataHeader() {
 		openCSVHeaderValidator(tempFile.getAbsolutePath(), true);
 	}
 
 	@NotifyChange("genotypeFileList")
 	@Command("removeGenotypeFile")
 	public void removeGenotypeFile(@BindingParam("index") int index) {
 		System.out.println("Deleted file index: " + index);
 		genotypeFileList.get(index).tempFile.delete();
 		genotypeFileList.remove(index);
 	}
 
 	
 	public class Runtimer {
 		long startTime = System.nanoTime();
 		
 		public long start(){
 			startTime = System.nanoTime();
 			return startTime;
 		}
 		public double end() {
 			long endTime = System.nanoTime();
 			return (endTime - startTime) / 1000000000.0;
 		}
 		
 		
 	}
 	public class GenotypeFileModel {
 
 		private String name;
 		private File tempFile;
 
 		public String getName() {
 			return name;
 		}
 
 		public void setName(String name) {
 			this.name = name;
 		}
 
 		public File getFilepath() {
 			return tempFile;
 		}
 
 		public void setFilepath(File filepath) {
 			this.tempFile = filepath;
 		}
 
 		public GenotypeFileModel(String name, File path) {
 			this.name = name;
 			this.tempFile = path;
 		}
 
 	}
 }
