 package net.bioclipse.brunn.ui.editors.plateEditor;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.math.BigDecimal;
 import java.math.MathContext;
 import java.net.URL;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.Map;
 
 import net.bioclipse.brunn.pojos.PlateFunction;
 import net.bioclipse.brunn.results.PlateResults;
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
 
 public class PlateReport extends EditorPart {
 	
 	private Browser browser;
 	private Replicates replicates;
 	private String[] neededData;
 	private Map<String, String[]> content = new HashMap<String, String[]>();
 	private int contentLength = 0;
 	private Map<String, String[]> functions = new HashMap<String, String[]>();
 	private Map<String, Double> IC50 = new HashMap<String, Double>();
 	private int numberOfColumns;
 	
 	public PlateReport (Replicates replicates) {
 		super();
 		this.replicates = replicates;
 		neededData = new String[]{"Compound Names","SI%","IC50","Concentration","Unit","Column Index","Plate Name"};
 	}
 	
 	//adds default values for uninitialized variables in dataset
 	private void autoCompleteContent() {
 		for(String header : neededData) {
 			if(content.get(header) == null) {
 				for(int i=0; i<contentLength; i++) {
 					content.put(header,addToStringArray(content.get(header),"-1.0"));
 				}
 			}
 		}
 	}
 	
 	//reads dataset from replicate table
 	private void fillContent() {
 		String[][] matrix = replicates.getReplicatesContent();
 		contentLength = matrix.length-1;
 		String[] headers = matrix[0];
 		for(int i=0; i<headers.length; i++) {
 			if(headers[i].equals("si%") || headers.equals("si") || headers.equals("SI") || headers.equals("Si")) {
 				headers[i] = "SI%";
 			}
 			content.put(headers[i], null);
 		}
 		for(int i=1; i<matrix.length; i++) {
 			for(int j=0; j<headers.length; j++) {
 				content.put(headers[j],addToStringArray(content.get(headers[j]), matrix[i][j]));	
 			}
 		}
 		splitConcAndUnit();
 		storeIC50();
 	}
 	
 	//reads in all defined functions for the plate
 	private void fillFunctions() {
 		PlateResults plateResults = new PlateResults(replicates.getPlate(),null);
 		Iterator<PlateFunction> plateFunctionIterator = replicates.getPlate().getPlateFunctions().iterator();
 		functions.put("Function", null);
 		functions.put("Function Value", null);
 		while(plateFunctionIterator.hasNext()) {
 			PlateFunction plateFunction = (PlateFunction) plateFunctionIterator.next();
 			functions.put("Function",addToStringArray(functions.get("Function"),plateFunction.getName()));
 		}
 		String[] plateFunctionNames = functions.get("Function");
 		Arrays.sort(plateFunctionNames);
 		MathContext mc = new MathContext(3);
 		for(String plateFunctionName : plateFunctionNames) {
 			double value = plateResults.getValue(plateFunctionName);
 			String result;
 			if ( Double.isInfinite(value) || Double.isNaN(value) ) {
 				result = String.valueOf(Double.MAX_VALUE);
 			}
 			else {
 				BigDecimal bd = new BigDecimal(value);
 				result = String.valueOf(bd.round(mc));
 			}
 			
 			functions.put("Function Value",addToStringArray(functions.get("Function Value"), result));	
 		}
 	}
 	
 	private String[] addToStringArray(String[] array, String string) {
 		if(array != null) {
 			String[] newArray = new String[array.length+1];
 			for(int i=0; i<array.length; i++) {
 				newArray[i] = array[i];
 			}
 			newArray[array.length] = string;
 			return newArray;
 		}
 		else {
 			return new String[] {string};
 		}
 	}
 	
 	private void storeIC50() {
 		if(content.containsKey("SI%") && content.containsKey("Concentration")) {
 			String[] names = content.get("Compound Names");
 			double[] conc = null;
 			double[] si = null;
 			String current = names[0];
 			MathContext mc = new MathContext(3);
 			for(int i=0;i<names.length; i++) {
 				if(names[i].equals(current)) {
 					conc = addToDoubleArray(conc, Double.parseDouble(content.get("Concentration")[i]));
 					si = addToDoubleArray(si, Double.parseDouble(content.get("SI%")[i]));
 				}
 				else {
 					BigDecimal bd = new BigDecimal(calculateIC50(conc, si));
 					IC50.put(current, bd.round(mc).doubleValue());
 					current = names[i];
 					conc = addToDoubleArray(null, Double.parseDouble(content.get("Concentration")[i]));
 					si = addToDoubleArray(null, Double.parseDouble(content.get("SI%")[i]));
 				}
 			}
 			BigDecimal bd = new BigDecimal(calculateIC50(conc, si));
 			IC50.put(current, bd.round(mc).doubleValue());
 			addIC50ToContent();
 		}
 	}
 
 	private double[] addToDoubleArray(double[] array, double d) {
 		if(array != null) {
 			double[] newArray = new double[array.length+1];
 			for(int i=0; i<array.length; i++) {
 				newArray[i] = array[i];
 			}
 			newArray[array.length] = d;
 			return newArray;
 		}
 		else {
 			return new double[] {d};
 		}
 	}
 	
 	private double calculateIC50(double[] conc, double[] si) {
 		double x1=0,x2=0,y1=0,y2=0;
 		for(int i=0; i<conc.length; i++) {
 			if(si[i]<50) {
 				x2 = conc[i];
 				y2 = si[i];
 				break;
 			}
 			x1 = conc[i];
 			y1 = si[i];
 		}
 		return (x1>0 && x2>0)?(x1-x2)/(y1-y2)*(50-y1)+x1:-1.;
 	}
 	
 	private void addIC50ToContent() {
 		content.put("IC50", null);
 		String[] names = content.get("Compound Names");
 		for(String name : names) {
 			if(!IC50.isEmpty()) {
 				String ic50 = IC50.get(name).toString();
 				String[] values = content.get("IC50");
 				String[] newValues = addToStringArray(values, ic50);
 				content.put("IC50", newValues);	
 			}
 		}
 	}
 	
 	//just for printing to promt
 	private void printIC50() {
 		Iterator<String> substanceIter = IC50.keySet().iterator();
 		while(substanceIter.hasNext()) {
 			String substance = substanceIter.next();
 			System.out.println(substance+" "+IC50.get(substance));
 		}
 	}
 
 	private void splitConcAndUnit() {
 		String[] concAndUnit = content.get("Concentration");
 		String[] conc = null;
 		String[] unit = null;
 		MathContext mc = new MathContext(3);
 		for(int i=0; i<concAndUnit.length; i++) {
 			String[] splitted = concAndUnit[i].split(" ");
 			BigDecimal bd = new BigDecimal(splitted[0]);
 			conc = addToStringArray(conc, bd.round(mc).toString());
 			unit = addToStringArray(unit, splitted[1]);
 		}
 		content.put("Concentration", conc);
 		content.put("Unit", unit);
 	}
 	
 	private void addPlateName() {
 		String[] rows = content.get("Compound Names");
 		String plateName = replicates.getPlate().getName();
 		String[] names = null;
 		for(String row : rows) {
 			names = addToStringArray(names, plateName);
 		}
 		content.put("Plate Name",names);
 	}
 	
 	//divides substances into columns, and adds trailing newline in function list if needed
 	private void addReportColumnIndex(Map<String,String[]> map, String groupOnHeader) {
 		//retrieves and sorts the names, they should be in order in the report
 		String[] names = map.get(groupOnHeader);
 		Arrays.sort(names);
 		//removes duplicates for correct column assignment
 		LinkedHashSet<String> noDuplicates = new LinkedHashSet<String>(Arrays.asList(names));
 		String[] noDupNames = noDuplicates.toArray(new String[noDuplicates.size()]);
 		//determines number of columns for report based on number of compounds
 		numberOfColumns = noDupNames.length<6?1:noDupNames.length<13?2:3;
 		//calculates correct column numbers
 		int index = numberOfColumns;
 		Integer[] indexes = new Integer[noDupNames.length];
 		for(int i=0; i<noDupNames.length; i++) {
 		    index = nextIndex(index,numberOfColumns);
 			indexes[i] = index;
 		}
 		Arrays.sort(indexes);
 		//connects correct column number for items
 		Map<String,Integer> nameIndex = new HashMap<String,Integer>();
 		for(int i=0; i<indexes.length; i++) {
 			nameIndex.put(noDupNames[i], indexes[i]);
 		}
 		//assigns correct column number to items
 		String[] nameIndexes = new String[names.length];
 		for(int i=0; i<names.length; i++) {
 			nameIndexes[i] = String.valueOf(nameIndex.get(names[i]));
 		}
 		//adds trailing spaces in case of non equal column lengths
 		if(map.containsKey("Function") && indexes.length%numberOfColumns!=0) {
 			int toAdd = numberOfColumns - indexes.length%numberOfColumns;
 			for(int i=0; i<toAdd; i++) {
 				nameIndexes = addToStringArray(nameIndexes,String.valueOf(numberOfColumns));
 				map.put("Function", addToStringArray(map.get("Function"),""));
 				map.put("Function Value", addToStringArray(map.get("Function Value"),""));
 			}
 		}
 		//stores the result in dataset
 		map.put("Column Index", nameIndexes);
 	}
 	
 	private int nextIndex(int current, int numberOf) {
 		return (current%numberOf) + 1;
 	}
 	
 	private void printMapToFile(Map<String,String[]> map, String filename, String headerInMap) {
 		try {
 			File folder = BioclipseCache.getCacheDir();
 			PrintWriter printWriter = new PrintWriter(new FileWriter(folder+File.separator+filename));
 			Object[] headers = map.keySet().toArray();
 			for(int i=0; i<headers.length; i++) {
 				printWriter.write(headers[i]+(i<headers.length-1?",":"\n"));
 			}
 			for(int i=0; i<map.get(headerInMap).length; i++) {
 				for(int j=0; j<headers.length; j++) {
 					printWriter.write(map.get(headers[j])[i]+(j<headers.length-1?",":"\n"));
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
 		String filename = numberOfColumns==1?"plateReport1.rptdesign":numberOfColumns==2?"plateReport2.rptdesign":"plateReport3.rptdesign";
         try {
         	url = FileLocator.toFileURL( PlateReport.class.getResource( filename ) );
         } catch ( IOException e ) {
             throw new RuntimeException(e);
         }
         try {
 			changeFileLocation(filename,"C:\\Program\\Eclipse\\runtime-bioclipse.product\\tmp",BioclipseCache.getCacheDir().getAbsolutePath());
 		} catch (CoreException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return url.getFile();
 	}
 
 	//changes report file location to full path required by BIRT
 	//could be done in BIRT, but does not work on windows then
 	public void changeFileLocation(String fileName, String from, String to) {
 		URL url = null;
         try {
             url = FileLocator.toFileURL(PlateReport.class.getResource(fileName));
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
 		// TODO Auto-generated method stub
 		
 		browser = new Browser(parent, SWT.NONE);
		WebViewer.startup(browser);
 		//WebViewer.display(getReportFile(), WebViewer.HTML, browser, "frameset");
 	}
 	
 	@Override
 	public void setFocus() {
 			// TODO Auto-generated method stub
 		
 	}
 	
 	//reads the data and reloads the report when report tab is activated
 	public void onPageChange() {
 		fillContent();
 		fillFunctions();
 		autoCompleteContent();
 		addReportColumnIndex(functions, "Function");
 		addReportColumnIndex(content, "Compound Names");
 		addPlateName();
 		printMapToFile(content, "values.csv", "Compound Names");
 		printMapToFile(functions, "functions.csv", "Function");
 
 		WebViewer.cancel(browser);
 		WebViewer.display(getReportFile(), WebViewer.HTML, browser, "frameset");
 	}
 }
