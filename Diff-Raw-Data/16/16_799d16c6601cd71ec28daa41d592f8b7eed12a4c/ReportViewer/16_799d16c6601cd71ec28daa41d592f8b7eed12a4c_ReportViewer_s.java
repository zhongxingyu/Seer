 package net.bioclipse.brunn.ui.editors.plateEditor;
 
 import org.w3c.dom.*;
 import org.xml.sax.InputSource;
 
 import net.bioclipse.brunn.pojos.PlateFunction;
 import net.bioclipse.brunn.results.PlateResults;
 import net.bioclipse.ui.BioclipseCache;
 
 import java.io.BufferedWriter;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.PrintStream;
 import java.io.PrintWriter;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Scanner;
 
 import org.eclipse.birt.report.viewer.utilities.WebViewer;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.browser.Browser;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorSite;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.part.EditorPart;
 
 import com.sun.org.apache.xerces.internal.parsers.DOMParser;
 
 public class ReportViewer extends EditorPart implements OutlierChangedListener{
 	
 	private Replicates replicates;
 	private String[] neededData;
 	private Map<String, String[]> content = new HashMap<String, String[]>();
 	private int contentLength = 0;
 	private Map<String, String[]> functions = new HashMap<String, String[]>();
 	private Map<String, Double> EC50 = new HashMap<String, Double>(); 
 	
 	public ReportViewer (PlateMultiPageEditor plateMultiPageEditor, Replicates replicates) {
 		super();
 		this.replicates = replicates;
 		plateMultiPageEditor.addListener(this);
 		neededData = new String[]{"Compound Names","SI%","EC50","Concentration","Unit","Column Index","Plate Name"};
 	}
 	
 	private void autoCompleteContent() {
 		for(String header : neededData) {
 			if(content.get(header) == null) {
 				content.put(header, new String[contentLength]);
 			}
 		}
 	}
 	
 	private void fillContent() {
 		String[][] matrix = replicates.getReplicatesContent();
 		contentLength = matrix.length-1;
 		String[] headers = matrix[0];
 		for(int i=0; i<headers.length; i++) {
 			content.put(headers[i], null);
 		}
 		for(int i=1; i<matrix.length; i++) {
 			for(int j=0; j<headers.length; j++) {
 				content.put(headers[j],addToStringArray(content.get(headers[j]), matrix[i][j]));	
 			}
 		}
 		splitConcAndUnit();
 		storeEC50();
 	}
 	
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
 		for(String plateFunctionName : plateFunctionNames) {
 			functions.put("Function Value",addToStringArray(functions.get("Function Value"),String.valueOf(plateResults.getValue(plateFunctionName))));	
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
 	
 	private void storeEC50() {
 		if(content.containsKey("SI%") && content.containsKey("Concentration")) {
 			String[] names = content.get("Compound Names");
 			double[] conc = null;
 			double[] si = null;
 			String current = names[0];
 			for(int i=0;i<names.length; i++) {
 				if(names[i].equals(current)) {
 					conc = addToDoubleArray(conc, Double.parseDouble(content.get("Concentration")[i]));
 					si = addToDoubleArray(si, Double.parseDouble(content.get("SI%")[i]));
 				}
 				else {
 					EC50.put(current, calculateEC50(conc, si));
 					current = names[i];
 					conc = addToDoubleArray(null, Double.parseDouble(content.get("Concentration")[i]));
 					si = addToDoubleArray(null, Double.parseDouble(content.get("SI%")[i]));
 				}
 			}
 			EC50.put(current, calculateEC50(conc, si));
 			addEC50ToContent();
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
 	
 	private double calculateEC50(double[] conc, double[] si) {
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
 	
 	private void addEC50ToContent() {
 		content.put("EC50", null);
 		String[] names = content.get("Compound Names");
 		for(String name : names) {
 			if(!EC50.isEmpty()) {
 				String ec50 = EC50.get(name).toString();
 				String[] values = content.get("EC50");
 				String[] newValues = addToStringArray(values, ec50);
 				content.put("EC50", newValues);	
 			}
 		}
 	}
 
 	private void printEC50() {
 		Iterator<String> substanceIter = EC50.keySet().iterator();
 		while(substanceIter.hasNext()) {
 			String substance = substanceIter.next();
 			System.out.println(substance+" "+EC50.get(substance));
 		}
 	}
 
 	private void splitConcAndUnit() {
 		String[] concAndUnit = content.get("Concentration");
 		String[] conc = null;
 		String[] unit = null;
 		for(int i=0; i<concAndUnit.length; i++) {
 			String[] splitted = concAndUnit[i].split(" ");
 			conc = addToStringArray(conc, splitted[0]);
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
 	
 	private void addReportColumnIndex(Map<String,String[]> map, String groupOnHeader) {
 		String[] names = map.get(groupOnHeader);
 		Arrays.sort(names);
 		String index = "1";
 		String[] indexes = null;
 		int columnLength = names.length/2;
 		for(int i=0; i<names.length; i++) {
 			if(i>=columnLength) {
 				index = "2";
 			}
 			indexes = addToStringArray(indexes, index);
 		}
 		map.put("Column Index",indexes);
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
 
 	@Override
 	public void createPartControl(Composite parent) {
 		// TODO Auto-generated method stub
 		fillContent();
 		fillFunctions();
 		autoCompleteContent();
 		addReportColumnIndex(content, "Compound Names");
 		addReportColumnIndex(functions, "Function");
 		addPlateName();
 		printMapToFile(content, "values.csv", "Compound Names");
 		printMapToFile(functions, "functions.csv", "Function");
 		printEC50();
		//Browser browser = new Browser(parent, SWT.NONE);
		//WebViewer.display("/home/jonas/brunntrunk/plugins/net.bioclipse.brunn.ui/src/net/bioclipse/brunn/ui/editors/plateEditor/plateReport.rptdesign", WebViewer.HTML, browser, "frameset");
 		//WebViewer.display("/home/jonas/brunnbranchesbirtExample/myJava/myReport.rptdesign", WebViewer.HTML, browser, "frameset");
 	}
 	
 	@Override
 	public void setFocus() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void onOutLierChange() {
 		// TODO Auto-generated method stub
 		fillContent();
 		fillFunctions();
 		printMapToFile(content, "values.csv", "Compound Names");
 		printMapToFile(functions, "functions.csv", "Function");
 	}
 
 }
