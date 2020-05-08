 package net.bioclipse.brunn.ui.editors.masterPlateEditor;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.net.URL;
 
 import net.bioclipse.ui.BioclipseCache;
 
 import org.eclipse.birt.report.viewer.utilities.WebViewer;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.browser.Browser;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorSite;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.part.EditorPart;
 
 public class MasterPlateReport extends EditorPart{
 	
 	private MasterPlateEditor masterPlateEditor;
 	private Browser browser;
 	private String[][] masterPlateName = new String[2][1];
 	private String[][] substances;
 	private String[][] masterPlateLayout;
 	private String[][] combinedMasterPlateAndMarkerLayout;
 	private int size;
 	
 	public MasterPlateReport(MasterPlateEditor masterPlateEditor) {
 		super();
 		this.masterPlateEditor = masterPlateEditor;
 		readData();
 	}
 	
 	private void readData() {
 		masterPlateName[0][0] = "MasterPlateName";
 		masterPlateName[1][0] = masterPlateEditor.getPartName();
 		substances = masterPlateEditor.getSubstanceNames();
 		masterPlateLayout = masterPlateEditor.getMasterPlateLayout();
 		combinedMasterPlateAndMarkerLayout = new String[masterPlateLayout.length][masterPlateLayout[0].length*2];
 		size = (masterPlateLayout.length-1)*(masterPlateLayout[0].length-1);
 	}
 	
 	private void printDataSetToFile(String[][] Array, String filename) {
 		try {
 			File folder = BioclipseCache.getCacheDir();
 			PrintWriter printWriter = new PrintWriter(new FileWriter(folder+File.separator+filename));
 			if(filename.equals("substances.csv")) {
 				printWriter.write("marker,sample\n");
 			}
 			for(int i=0; i<Array.length; i++) {
 				for(int j=0; j<Array[0].length; j++) {
 					printWriter.write(Array[i][j]+(j<Array[0].length-1?",":"\n"));
 				}
 			}
 			printWriter.close();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (CoreException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	//Combines the layouts into one dataset to be used in the same table in the report
 	public void combineMasterPlateAndMarkerLayout() {
 		//This will correct the header row of the dataset so columns are correctly ordered.
 		for(int i=0; i<masterPlateLayout[0].length; i++) {
 			if(masterPlateLayout[0][i].length() == 0) {
 				masterPlateLayout[0][i] = "00";
 			}
 			if(masterPlateLayout[0][i].length() == 1) {
 				masterPlateLayout[0][i] = "0"+masterPlateLayout[0][i];
 			}
 		}
 		//This will fill the header row of dataset with correct numbers.
 		int column = 0;
 		for(int i=0; i<masterPlateLayout[0].length*2; i++) {
 			if(i<masterPlateLayout[0].length) {
 				column = Integer.parseInt(masterPlateLayout[0][i]);
 				combinedMasterPlateAndMarkerLayout[0][i] = masterPlateLayout[0][i];
 			}
 			else {
 				column++;
 				combinedMasterPlateAndMarkerLayout[0][i] = ""+column;
 			}
 		}
 		//This will fill the rest of the dataset.
 		for(int i=1; i<masterPlateLayout.length; i++) {
 			for(int j=0; j<masterPlateLayout[0].length; j++) {
 				String ij = masterPlateLayout[i][j];
 				if(ij.startsWith("M")) {
 					String[] ij2 = ij.split(" ",2);
 					combinedMasterPlateAndMarkerLayout[i][masterPlateLayout[0].length+j] = ij2[0];
					ij2[1] = ij2[1].replace(',', '.'); //can not have 100,00 in csv file
 					combinedMasterPlateAndMarkerLayout[i][j] = ij2[1];
 				}
 				else if(ij.startsWith("B") || ij.startsWith("C") || ij.startsWith("S")) {
 					combinedMasterPlateAndMarkerLayout[i][j] = masterPlateLayout[i][j];
 					combinedMasterPlateAndMarkerLayout[i][masterPlateLayout[0].length+j] = "";
 				}
 				else {
 					combinedMasterPlateAndMarkerLayout[i][j] = masterPlateLayout[i][j];
 					combinedMasterPlateAndMarkerLayout[i][masterPlateLayout[0].length+j] = masterPlateLayout[i][j];
 				}
 			}
 		}
 	}
 
 	@Override
 	public void doSave(IProgressMonitor monitor) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void doSaveAs() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void init(IEditorSite site, IEditorInput input)
 			throws PartInitException {
 		// TODO Auto-generated method stub
 		setSite(site);
 		setInput(input);
 	}
 
 	@Override
 	public boolean isDirty() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean isSaveAsAllowed() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 	
 	//returns the full path of the report file
 	public String getReportFile() {
 		URL url = null;
         try {
         	if(size == 96) {
                 url = FileLocator.toFileURL( MasterPlateReport.class.getResource( "masterPlateReport96.rptdesign" ) );	
         	}
         	if(size == 384) {
                 url = FileLocator.toFileURL( MasterPlateReport.class.getResource( "masterPlateReport384.rptdesign" ) );
         	}
         } catch ( IOException e ) {
             throw new RuntimeException(e);
         }
         try {
         	if(size == 96) {
     			changeFileLocation("masterPlateReport96.rptdesign","/home/jonas/runtime-bioclipse.product/tmp",BioclipseCache.getCacheDir().getAbsolutePath());	
         	}
         	if(size == 384) {
     			changeFileLocation("masterPlateReport384.rptdesign","/home/jonas/runtime-bioclipse.product/tmp",BioclipseCache.getCacheDir().getAbsolutePath());
         	}
 		} catch (CoreException e) {
 			e.printStackTrace();
 		}
 		return url.getFile();
 	}
 	
 	//changes report file location to full path required by BIRT
 	//could be done in BIRT, but does not work on windows then
 	public void changeFileLocation(String fileName, String from, String to) {
 		URL url = null;
         try {
             url = FileLocator.toFileURL(MasterPlateReport.class.getResource(fileName));
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
 		File file = new File(url.getFile());
 		FileWriter out;
 		try {
 			String fileAsString = "";
 			BufferedReader reader = new BufferedReader(new FileReader(file));
 			String fileLine;
 			while ((fileLine = reader.readLine()) != null){
 				if( fileLine.contains(from) ) {
 					fileLine = fileLine.substring(0, fileLine.indexOf(from) ) + to + "</property>";
 				}
 				fileAsString += fileLine+"\n";
 			}
 			reader.close();
 			
 			out = new FileWriter(file);
 			out.write(fileAsString);
 			out.close();
 
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void createPartControl(Composite parent) {
 		/*combineMasterPlateAndMarkerLayout();
 		printDataSetToFile(masterPlateName,"masterPlateName.csv");
 		printDataSetToFile(substances,"substances.csv");
 		printDataSetToFile(combinedMasterPlateAndMarkerLayout,"masterPlateLayout.csv");
        */
 		browser = new Browser(parent, SWT.NONE);
 		//WebViewer.display(getReportFile(), WebViewer.HTML, browser, "frameset");
 	}
 	
 	//reads the data and reloads the report when report tab is activated
 	public void onPageChange() {
 		readData();
 		combineMasterPlateAndMarkerLayout();
 		printDataSetToFile(masterPlateName,"masterPlateName.csv");
 		printDataSetToFile(substances,"substances.csv");
 		printDataSetToFile(combinedMasterPlateAndMarkerLayout,"masterPlateLayout.csv");
 		
 		//Browser browser = new Browser(parent, SWT.NONE);
 		WebViewer.cancel(browser);
 		WebViewer.display(getReportFile(), WebViewer.HTML, browser, "frameset");
 	}
 
 	@Override
 	public void setFocus() {
 		// TODO Auto-generated method stub
 	}
 }
