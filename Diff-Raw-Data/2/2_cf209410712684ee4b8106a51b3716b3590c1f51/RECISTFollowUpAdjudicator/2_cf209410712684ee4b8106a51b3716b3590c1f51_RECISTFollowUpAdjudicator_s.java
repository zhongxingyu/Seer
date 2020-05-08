 /**
  * Copyright (c) 2008 Washington University in Saint Louis. All Rights Reserved.
  */
 
 import java.io.File;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.StringTokenizer;
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 import org.nema.dicom.wg23.ArrayOfObjectDescriptor;
 import org.nema.dicom.wg23.ArrayOfObjectLocator;
 import org.nema.dicom.wg23.ArrayOfQueryResult;
 import org.nema.dicom.wg23.ArrayOfString;
 import org.nema.dicom.wg23.ArrayOfUUID;
 import org.nema.dicom.wg23.AvailableData;
 import org.nema.dicom.wg23.ModelSetDescriptor;
 import org.nema.dicom.wg23.ObjectDescriptor;
 import org.nema.dicom.wg23.ObjectLocator;
 import org.nema.dicom.wg23.Patient;
 import org.nema.dicom.wg23.QueryResult;
 import org.nema.dicom.wg23.Rectangle;
 import org.nema.dicom.wg23.Series;
 import org.nema.dicom.wg23.State;
 import org.nema.dicom.wg23.Study;
 import org.nema.dicom.wg23.Uid;
 import org.nema.dicom.wg23.Uuid;
 
 import com.pixelmed.dicom.AttributeList;
 import com.pixelmed.dicom.StructuredReportBrowser;
 
 import edu.wustl.xipApplication.application.ApplicationTerminator;
 import edu.wustl.xipApplication.applicationGUI.ExceptionDialog;
 import edu.wustl.xipApplication.application.WG23Application;
 import edu.wustl.xipApplication.samples.ApplicationFrameTempl;
 import edu.wustl.xipApplication.recist.RECISTFactory;
 import edu.wustl.xipApplication.recist.RECISTManager;
 import edu.wustl.xipApplication.recist.Tumor;
 import edu.wustl.xipApplication.wg23.ApplicationImpl;
 import edu.wustl.xipApplication.wg23.OutputAvailableEvent;
 import edu.wustl.xipApplication.wg23.OutputAvailableListener;
 import edu.wustl.xipApplication.wg23.WG23DataModel;
 import edu.wustl.xipApplication.wg23.WG23DataModelImpl;
 import edu.wustl.xipApplication.wg23.WG23Listener;
 
 
 public class RECISTFollowUpAdjudicator extends WG23Application implements WG23Listener, OutputAvailableListener{		
 	ApplicationFrameTempl frame = new ApplicationFrameTempl();			
 	RECISTFollowUpAdjudicatorPanel appPanel = new RECISTFollowUpAdjudicatorPanel();
 	
 	String outDir;
 	State appCurrentState;		
 	RECISTManager recistMgr;	
 	
 	public RECISTFollowUpAdjudicator(URL hostURL, URL appURL) {
 		super(hostURL, appURL);				
 		appPanel.setVisible(false);
 		appPanel.getAIMPanel().addOutputAvailableListener(this);
 		frame.getDisplayPanel().add(appPanel);
 		frame.setVisible(true);	
 		/*Set application dimensions */
 		Rectangle rect = getClientToHost().getAvailableScreen(null);			
 		frame.setBounds(rect.getRefPointX(), rect.getRefPointY(), rect.getWidth(), rect.getHeight());
 		recistMgr = RECISTFactory.getInstance();
 		/*Notify Host application was launched*/							
 		ApplicationImpl appImpl = new ApplicationImpl();
 		appImpl.addWG23Listener(this);
 		setAndDeployApplicationService(appImpl);		
 		getClientToHost().notifyStateChanged(State.IDLE);		
 	}
 		
 	public static void main(String[] args) {
 		try {
 			/*args = new String[4];
 			args[0] = "--hostURL";
 			args[1] = "http://localhost:8090/HostService";
 			args[2] = "--applicationURL";
 			args[3] = "http://localhost:8060/ApplicationService";*/			
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 			System.out.println("Number of parameters: " + args.length);
 			for (int i = 0; i < args.length; i++){
 				System.out.println(i + ". " + args[i]);
 			}
 			URL hostURL = null;
 			URL applicationURL = null;
 			for (int i = 0; i < args.length; i++){
 				if (args[i].equalsIgnoreCase("--hostURL")){
 					hostURL = new URL(args[i + 1]);
 				}else if(args[i].equalsIgnoreCase("--applicationURL")){
 					applicationURL = new URL(args[i + 1]);
 				}					
 			}									
 			new RECISTFollowUpAdjudicator(hostURL, applicationURL);										
 		} catch (ClassNotFoundException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} catch (InstantiationException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} catch (IllegalAccessException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} catch (UnsupportedLookAndFeelException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} catch (MalformedURLException e) {			
 			e.printStackTrace();
 		} catch (NullPointerException e){
 			new ExceptionDialog("List of parameters is not valid!", 
 					"Ensure: --hostURL url1 --applicationURL url2",
 					"Launch Application Dialog");
 			System.exit(0);
 		}
 	}
 	
 	public String getSceneGraphInput(List<ObjectLocator> objLocs){
 		String input = new String();
 		int size = objLocs.size();
 		for (int i = 0; i < size; i++){
 			if(i == 0){
 				String filePath;				
 				filePath = new File(objLocs.get(i).getUri()).getPath();
 				// input = input + "\"" + nols.get(i).getURI() + "\"" + ", ";					
 				filePath = filePath.substring(6 , filePath.length());
 				input = "[" + "\"" + filePath + "\"" + ", ";								
 			} else if(i < size -1){
 				String filePath = new File(objLocs.get(i).getUri()).getPath();
 				//input = input + "\"" + nols.get(i).getURI() + "\"" + ", ";
 				filePath = filePath.substring(6 , filePath.length());
 				input = input + "\"" + filePath + "\"" + ", ";
 			}else if(i == size -1){
 				String filePath = new File(objLocs.get(i).getUri()).getPath();
 				//input = input + "\"" + nols.get(i).getURI() + "\"" + ", ";
 				filePath = filePath.substring(6 , filePath.length());
 				input = input + "\"" + filePath + "\"" + "]";
 			}				
 		}
 		return input;
 	}		
 	
 	public boolean bringToFront() {
 		frame.setAlwaysOnTop(true);
 		frame.setAlwaysOnTop(false);
 		return true;
 	}	
 			
 	List<String> sopInstanceUIDPrev;
 	List<String> sopInstanceUIDCurr;
 	public void notifyDataAvailable(AvailableData availableData, boolean lastData) {										
 		List<Uuid> uuidsGroup1 = new ArrayList<Uuid>();
 		List<Uuid> uuidsGroup2 = new ArrayList<Uuid>();		
 		List<Uuid> uuidsGSPS = new ArrayList<Uuid>();		
 		List<Uuid> uuidsSR = new ArrayList<Uuid>();		
 		//Extract UUIDs for all dicom objects from both groups
 		List<Patient> patients = availableData.getPatients().getPatient();		
 		for(int i = 0; i < patients.size(); i++){
 			Patient patient = patients.get(i);		
 			List<Study> studies = patient.getStudies().getStudy();
 			for(int j = 0; j < studies.size(); j++){
 				Study study = studies.get(j);				
 				List<Series> listOfSeries = study.getSeries().getSeries();
 				for(int k = 0; k < listOfSeries.size(); k++){
 					Series series = listOfSeries.get(k);					
 					ArrayOfObjectDescriptor descriptors = series.getObjectDescriptors();
 					List<ObjectDescriptor> listDescriptors = descriptors.getObjectDescriptor();
 					for(int m = 0;  m < listDescriptors.size(); m++){
 						ObjectDescriptor desc = listDescriptors.get(m);
 						String sopClassUID = desc.getClassUID().getUid();
 						String modality = desc.getModality().getModality();
 						if (sopClassUID.equalsIgnoreCase("1.2.840.10008.5.1.4.1.1.11.1")){
 							uuidsGSPS.add(desc.getUuid());
 						}else if (sopClassUID.equalsIgnoreCase("1.2.840.10008.5.1.4.1.1.88.59")){ //KOS
 							uuidsSR.add(desc.getUuid());
 						}else if(modality.equalsIgnoreCase("SR")){							
 							uuidsSR.add(desc.getUuid());
 						}else if(j == 0){							
 							uuidsGroup1.add(desc.getUuid());
 						}else if(j == 1){
 							uuidsGroup2.add(desc.getUuid());
 						}	
 					}
 				}
 			}
 		}
 		
 		//1. Determin DICOM dataset prev and current - use xpath to get series date
 		arrangeDicomDataSet(uuidsGroup1, uuidsGroup2);		
 		//2. Get ObjectLocators for all aim objects (aim objects need to be parsed by the application in order to determin
 		//what data set prev or curr they belong to
 		List<ObjectDescriptor> aimObjects = availableData.getObjectDescriptors().getObjectDescriptor();		
 		ArrayOfUUID arrayUUIDsAim = new ArrayOfUUID();
 		List<Uuid> listUUIDsAim = arrayUUIDsAim.getUuid();
 		for(int i = 0; i < aimObjects.size(); i++){
 			listUUIDsAim.add(aimObjects.get(i).getUuid());
 		}	
 		ArrayOfObjectLocator arrayOfObjectLocator = getClientToHost().getDataAsFile(arrayUUIDsAim, false);
 		List<ObjectLocator> objectLocatorsAim = arrayOfObjectLocator.getObjectLocator();
 		
 		//3. Get SOPInstanceUIDs for dicom dataset prev
 		//sopInstanceUIDPrev = new ArrayList<String>();
 		//sopInstanceUIDCurr = new ArrayList<String>();		
 		//4. Get SOPInstanceUID for dicom dataset prev and curr		
 		sopInstanceUIDPrev = getSOPInstanceUIDs(getDicomUUIDsPrev());				
 		sopInstanceUIDCurr = getSOPInstanceUIDs(getDicomUUIDsCurr());		
 		recistMgr.setSOPInstanceUIDPrev(sopInstanceUIDPrev);
 		recistMgr.setSOPInstanceUIDCurr(sopInstanceUIDCurr);
 		//5. Parse aim objects, determine their refSOPInstanceUID and add to aim prev or curr group
 		try {
 			arrangeAimDataSet(objectLocatorsAim);
 		} catch (URISyntaxException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}						
 		
 		//Get previous dataset		
 		ArrayOfUUID arrayUUIDsPrev = new ArrayOfUUID();
 		List<Uuid> listUUIDsPrev = arrayUUIDsPrev.getUuid();
 		for(int i = 0; i < getDicomUUIDsPrev().size(); i++){
 			listUUIDsPrev.add(getDicomUUIDsPrev().get(i));
 		}
 		ArrayOfObjectLocator objLocsPrev = getClientToHost().getDataAsFile(arrayUUIDsPrev, true);
 		List<ObjectLocator> listObjLocsPrev = objLocsPrev.getObjectLocator();									
 		//Get current dataset if it is present
 		List<ObjectLocator> listObjLocsCurr = new ArrayList<ObjectLocator>();
 		if(getDicomUUIDsCurr().size() != 0){
 			ArrayOfUUID arrayUUIDsCurr = new ArrayOfUUID();
 			List<Uuid> listUUIDsCurr = arrayUUIDsCurr.getUuid();
 			for(int i = 0; i < getDicomUUIDsCurr().size(); i++){
 				listUUIDsCurr.add(getDicomUUIDsCurr().get(i));
 			}
 			ArrayOfObjectLocator objLocsCurr = getClientToHost().getDataAsFile(arrayUUIDsCurr, true);
 			listObjLocsCurr = objLocsCurr.getObjectLocator();
 		}					
 		//update sene graph
 		if(appPanel.getIvCanvas().set("LoadDicom1.name", getSceneGraphInput(listObjLocsPrev))){
 		appPanel.getIvCanvas().set("DicomExaminer1.viewAll", "");
 		//appPanel.getIvCanvas().processQueue();
 		}
 		if(appPanel.getIvCanvas().set("LoadDicom2.name", getSceneGraphInput(listObjLocsCurr))){
 			appPanel.getIvCanvas().set("DicomExaminer2.viewAll", "");
 			//appPanel.getIvCanvas().processQueue();
 		}
 		//appPanel.getIvCanvas().set("Lut1.bitsUsed", "16");
 		appPanel.getIvCanvas().set("DicomExaminer1.imageIndex", "1");
 		appPanel.getIvCanvas().set("DicomExaminer2.imageIndex", "1");
 		
 		appPanel.getIvCanvas().set("OverlayManager2.create", "1");
 		appPanel.getIvCanvas().set("OverlayManager2.menuEnabled", "1");
 		appPanel.getIvCanvas().set("SelectMode1.index", "2");
 		appPanel.getIvCanvas().set("SelectMode2.index", "2");
 		appPanel.getIvCanvas().set("LockModeToggle.toggle", "");
 		appPanel.getIvCanvas().set("LockModeToggle.on", "");
 				
 		int numTumors = recistMgr.getNumberOfTumors();				
 		if(numTumors == 0){
 			Tumor tumor = null;
 			appPanel.getAIMPanel().addTumorTab(tumor);
 		}
 		for(int i = 0; i< numTumors; i++){
 			Tumor tumor = recistMgr.getTumors().get(i);						
 			appPanel.getAIMPanel().addTumorTab(tumor);						
 		}				
 		outDir = getClientToHost().getOutputDir();
 		recistMgr.setOutputDir(outDir);
 		appPanel.setVisible(true);
 		appPanel.repaint();	
 		
 		// Now for the SRs
 		List<ObjectLocator> listObjLocsSR = new ArrayList<ObjectLocator>();
 		if(uuidsSR.size() != 0){
 			ArrayOfUUID arraySR = new ArrayOfUUID();
 			List<Uuid> listUUIDsSR = arraySR.getUuid();
 			for(int i = 0; i < uuidsSR.size(); i++){
 				listUUIDsSR.add(uuidsSR.get(i));
 			}
 			ArrayOfObjectLocator objLocsCurr = getClientToHost().getDataAsFile(arraySR, true);
 			listObjLocsSR = objLocsCurr.getObjectLocator();
 		}					

 		
 	}	
 	
 	public boolean setState(State newState) {
 		if(State.valueOf(newState.toString()).equals(State.CANCELED)){
 			getClientToHost().notifyStateChanged(State.CANCELED);
 			getClientToHost().notifyStateChanged(State.IDLE);
 		}else if(State.valueOf(newState.toString()).equals(State.EXIT)){
 			getClientToHost().notifyStateChanged(State.EXIT);						
 			//terminating endpoint and existing system is accomplished through ApplicationTerminator
 			//and ApplicationScheduler. ApplicationSechduler is present to alow termination delay if needed (posible future use)
 			ApplicationTerminator terminator = new ApplicationTerminator(getEndPoint());
 			Thread t = new Thread(terminator);
 			t.start();	
 		}else{
 			getClientToHost().notifyStateChanged(newState);
 		}
 		return true;
 	}
 		
 	List<Uuid> dicomUUIDsPrev;
 	List<Uuid> dicomUUIDsCurr;
 	List<Uuid> getDicomUUIDsPrev(){
 		return dicomUUIDsPrev;
 	}
 	List<Uuid> getDicomUUIDsCurr(){
 		return dicomUUIDsCurr;
 	}
 	
 	void arrangeDicomDataSet(List<Uuid> uuidsGroup1, List<Uuid> uuidsGroup2){		
 		dicomUUIDsPrev = new ArrayList<Uuid>();
 		dicomUUIDsCurr = new ArrayList<Uuid>();
 		//Check if there is only one dataset
 		if(uuidsGroup1.size() != 0 && uuidsGroup2.size() == 0){
 			dicomUUIDsCurr = uuidsGroup1;
 		}else if(uuidsGroup1.size() == 0 && uuidsGroup2.size() != 0 ){
 			dicomUUIDsCurr = uuidsGroup2;
 		}else{
 			//Get native models UUIDs for one dicom object from group1 and one from group2		
 			ArrayOfUUID arrayUUIDs = new ArrayOfUUID();
 			List<Uuid> listUUIDs = arrayUUIDs.getUuid();
 			listUUIDs.add(uuidsGroup1.get(0));
 			listUUIDs.add(uuidsGroup2.get(0));
 			Uid uid = new Uid();		
 			uid.setUid("1");
 			Uid transferSyntaxUID = new Uid();
 			transferSyntaxUID.setUid("");
 			ModelSetDescriptor msd = getClientToHost().getAsModels(arrayUUIDs, uid, transferSyntaxUID);
 			ArrayOfUUID models = msd.getModels();					
 			ArrayOfString arrayOfString = new ArrayOfString();
 			List<String> xpaths = arrayOfString.getString();
 			xpaths.add("/DicomDataSet/DicomAttribute[@keyword=\"SeriesDate\"]/Value[@number=\"1\"]/text()");									
 			ArrayOfQueryResult results = getClientToHost().queryModel(models, arrayOfString, false);				
 			String dateGroup1 = results.getQueryResult().get(0).getResults().getString().get(0);
 			dateGroup1 = getParsedResult(dateGroup1);
 			String dateGroup2 = results.getQueryResult().get(1).getResults().getString().get(0);
 			dateGroup2 = getParsedResult(dateGroup2);							
 			//System.out.println("Date group 1 " + dateGroup1);
 			//System.out.println("Date group 2 " + dateGroup2);					
 			DateFormat formatter = new SimpleDateFormat("MM/dd/yy");		
 			Date date1 = null;
 			Date date2 = null;
 			try{					
 		        String strYearG1 = dateGroup1.trim().substring(0, 4);
 				String strMonthG1 = dateGroup1.trim().substring(4, 6);
 				String strDayG1 = dateGroup1.trim().substring(6, 8);
 		
 				String strYearG2 = dateGroup2.trim().substring(0, 4);
 				String strMonthG2 = dateGroup2.trim().substring(4, 6);
 				String strDayG2 = dateGroup2.trim().substring(6, 8);
 				date1 = (Date)formatter.parse(strMonthG1 + "/" + strDayG1 + "/" + strYearG1);
 				date2 = (Date)formatter.parse(strMonthG2 + "/" + strDayG2 + "/" + strYearG2);	
 			} catch(NullPointerException e){
 				e.printStackTrace();
 			} catch (ParseException e) {			
 				e.printStackTrace();
 			} 						
 			if(date1.before(date2)){			
 				dicomUUIDsPrev = uuidsGroup1;
 				dicomUUIDsCurr = uuidsGroup2;		
 			} else{
 				dicomUUIDsPrev = uuidsGroup2;
 				dicomUUIDsCurr = uuidsGroup1;
 			}
 		}				
 	}
 		
 	void arrangeAimDataSet(List<ObjectLocator> aimObjects) throws URISyntaxException{		
 		recistMgr.parseAIMObjects(aimObjects);		
 	}
 	
 		
 	List<String> getSOPInstanceUIDs(List<Uuid> uuidsGroup){
 		ArrayOfString arrayOfString = new ArrayOfString();
 		List<String> xpath = arrayOfString.getString();
 		xpath.add("/DicomDataSet/DicomAttribute[@keyword=\"SOPInstanceUID\"]/Value[@number=\"1\"]/text()");		
 		//PixelData/BulkData/path should not be used to get location of a dicom file, since header of dicom file and bulk data could be in separate places
 		//xpath.add("/DicomDataSet/DicomAttribute[@keyword=\"PixelData\"]/BulkData/@path");
 		ArrayOfUUID arrayUUIDs = new ArrayOfUUID();
 		List<Uuid> listUUIDs = arrayUUIDs.getUuid();		
 		for(int i = 0; i < uuidsGroup.size(); i++){
 			listUUIDs.add(uuidsGroup.get(i));			
 		}				
 		Uid uid = new Uid();		
 		uid.setUid("1");
 		Uid transferSyntaxUID = new Uid();
 		transferSyntaxUID.setUid("");		
 		ModelSetDescriptor msd = getClientToHost().getAsModels(arrayUUIDs, uid, transferSyntaxUID);
 		ArrayOfUUID models = msd.getModels();		
 		ArrayOfQueryResult results = getClientToHost().queryModel(models, arrayOfString, false);
 		List<QueryResult> list = results.getQueryResult();		
 		List<String> listSOPInstanceUIDs = new ArrayList<String>();
 		for(int i = 0; i < list.size(); i++){			
 			if(list.get(i).getXpath().equalsIgnoreCase("/DicomDataSet/DicomAttribute[@keyword=\"SOPInstanceUID\"]/Value[@number=\"1\"]/text()")){								
 				String sopInstanceUID = getParsedResult(list.get(i).getResults().getString().get(0));										
 				listSOPInstanceUIDs.add(sopInstanceUID);
 			}			
 		}		
 		return listSOPInstanceUIDs;
 	}
 
 	@SuppressWarnings("unchecked")
 	public void outputAvailable(OutputAvailableEvent e) {
 		List<File> output = (List<File>)e.getSource();
 		WG23DataModel wg23DM = new WG23DataModelImpl(output);		
 		recistMgr.setOutputData(wg23DM);
 		AvailableData availableData = wg23DM.getAvailableData();
 		getClientToHost().notifyDataAvailable(availableData, true);		
 	}
 	
 	public String getParsedResult(String result){        	 
 		String strTokens = "<>";
 	    StringTokenizer st = new StringTokenizer(result, strTokens, false);
 	    //ignore first token
 	    st.nextToken();
 	    String tok = st.nextToken();
 	    return tok;	    
 	}
 	
 	public void displaySR(List<ObjectLocator> listObjLocsSR){
 		
 		System.out.println("Found " + listObjLocsSR.size() + "KONs and SRs");
 
 		try {
 			if (listObjLocsSR.size() > 0){
 				for (int i = 0; i < listObjLocsSR.size(); i++) {
 					AttributeList list = new AttributeList();
 					String filePath;
 					URI filePathUri = new URI(listObjLocsSR.get(i).getUri());
 					filePath = filePathUri.getSchemeSpecificPart();
 					//filePath = filePath.substring(6 , filePath.length());
 					System.out.println(filePath);
 					list.read(filePath,null,true,true);
 					StructuredReportBrowser tree = new StructuredReportBrowser(list);
 					//DisplayStructuredReportBrowser tree = new DisplayStructuredReportBrowser(list,fileMap,512,512);
 					tree.setVisible(true);
 				}
 				
 			}
 		} catch (Exception e) {
 			e.printStackTrace();			
 		}
 	}
 }
