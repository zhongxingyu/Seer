 package org.irri.crosspreditor.helper;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 import java.util.Scanner;
 import java.util.Set;
 
 import org.irri.crosspredictor.model.CrossFormInputModel;
 
import sun.awt.geom.Crossings;
 
 import com.vaadin.server.Page;
 import com.vaadin.server.VaadinService;
 import com.vaadin.ui.CheckBox;
 import com.vaadin.ui.ComboBox;
 import com.vaadin.ui.ListSelect;
 import com.vaadin.ui.Notification;
 import com.vaadin.ui.TabSheet;
 
 
 public class CrossFormHelper{
 
     private String BSLASH = "\\";
     private String FSLASH = "/";
     public String BASE_PATH=VaadinService.getCurrent().getBaseDirectory().getAbsolutePath().replace(BSLASH, FSLASH);
 
     public enum GeneType{
 
 	Glu("Glu"),GluPin("GluPin"),GluPinSpn("GluPinSpn");
 
 	private String geneType;
 
 	private GeneType(String s){
 	    geneType=s;
 
 	}
 
 	public String getGeneType(){
 	    return geneType;
 	}
 
     }    
 
 
     public CrossFormHelper(){
 
 
     }
 
 
     public void populateGeneType(ComboBox comboBoxGeneType){
 
 	comboBoxGeneType.addItem(GeneType.Glu);
 	comboBoxGeneType.addItem(GeneType.GluPin);
 	comboBoxGeneType.addItem(GeneType.GluPinSpn);
     }
 
 
     public void populateParentList(File file, ListSelect listParents) {
 
 	Scanner opnScanner = null;
 	int ctr=0;
 	listParents.removeAllItems();
 	try {
 	    opnScanner = new Scanner(file);
 	    while(opnScanner.hasNext()) {
 		    String lineValue="";
 
 		    String newLine = opnScanner.nextLine();
 		    if(ctr>1){      // Read each line and display its value
 			int c = 0;
 			for(String s: newLine.split(",")){
 			    if(c==0){
 				lineValue=s+"-";
 			    }else{
 				lineValue=lineValue+s+",";
 			    }
 			    c++;
 			}
 			listParents.addItem(lineValue);
 		    }
 		    ctr++;
 		}
 	} catch (FileNotFoundException e) {
 	    // TODO Auto-generated catch block
 	    e.printStackTrace();
 	    System.out.println("file not found.");
 	}finally{
 	    opnScanner.close(); 
 	}
     }
     
     public String getRfOfParent(File file){
 	String toreturn = null;
 	Scanner opnScanner = null;
 	 try {
 	    opnScanner = new Scanner(file);
 	    int counter=0;
 	    while(opnScanner.hasNext()) {
 		String row = opnScanner.nextLine();
 		    if(counter==1){
 			
 			String[] tmpPList=row.split(",");
 			String freValue="";
 			for(int i=1;i<tmpPList.length;i++){
 				freValue+=tmpPList[i]+",";
 			}
 			toreturn=freValue;
 			
 			break;
 		    }
 		    counter++;
 	    }
 	    
 	    
 	} catch (FileNotFoundException e) {
 	    // TODO Auto-generated catch block
 	    e.printStackTrace();
 	}finally{
 	    opnScanner.close(); 
 	}
 	return toreturn.substring(0,toreturn.length()-1);
     }
 
 
     public void resetParentList(ListSelect listParents) {
 	listParents.removeAllItems();
     }
 
 
     public static String getTimeStamp() {
 	Calendar now = Calendar.getInstance();
 	return Long.toString(now.getTimeInMillis());
     }
 
 
     public void setSelectedItemToCross(ListSelect listParents, ListSelect selectedParentToCrossList) {
 	try{
 	    Object values = listParents.getValue();
 	    if (values instanceof Set) {
 		// Multiple selected values returned as a Set
 		List<String> list = new ArrayList<String>((Set) values);
 		for(String s: list){
 		    selectedParentToCrossList.addItem(s);
 		}
 	    } else {
 		selectedParentToCrossList.addItem(listParents.getValue());
 	    }
 	}catch(NullPointerException npe){
 	    new Notification("Please specify P1/P2/P3"+listParents.getCaption(),"",Notification.TYPE_WARNING_MESSAGE, true).show(Page.getCurrent());
 	}
 
     }
 
 
     public void setOtherParentCheckboxUnchecked(CheckBox checkBox1, CheckBox checkBox2) {
 	checkBox1.setValue(false);
 	checkBox2.setValue(false);
     }
 
     public String getCrossResultTabName(CrossFormInputModel crossFormInputModel){
 	return crossFormInputModel.getGeneType()+"-"+crossFormInputModel.getCrossType();
     }
 
 
    
 }
