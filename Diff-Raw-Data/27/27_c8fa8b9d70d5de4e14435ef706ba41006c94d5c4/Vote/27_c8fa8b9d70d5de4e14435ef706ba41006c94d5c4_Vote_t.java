 package com.rau.evoting.beans;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.util.ArrayList;
 
 import javax.faces.context.FacesContext;
 
 import net.sourceforge.barbecue.BarcodeFactory;
 import net.sourceforge.barbecue.BarcodeImageHandler;
 
 import org.primefaces.model.DefaultStreamedContent;
 import org.primefaces.model.StreamedContent;
 
 import com.rau.evoting.data.SqlDataProvider;
 import com.rau.evoting.models.Answer;
 import com.rau.evoting.utils.Util;
 
 public class Vote {
 
 	private ArrayList<Answer> answers1;
 	private ArrayList<Answer> answers2;
 	private int selectedAnswer1;
 	private int selectedAnswer2;
 
	//private StreamedContent barcode;
	//private ArrayList<StreamedContent> barcodes1;
	//private ArrayList<StreamedContent> barcodes2;
 
 	public Vote() {
		/*try {
 			File barcodeFile = new File("dynamicbarcode");
 			BarcodeImageHandler.saveJPEG(
 					BarcodeFactory.createCode128("PRIMEFACES"), barcodeFile);
 			// setBarcode(new DefaultStreamedContent(new
 			// FileInputStream(barcodeFile), "image/jpeg"));
 			barcode = new DefaultStreamedContent(new FileInputStream(
 					barcodeFile), "image/jpeg");
 		} catch (Exception e) {
 			e.printStackTrace();
		}*/
 	}
 
 	public ArrayList<Answer> getAnswers1() {
 		return answers1;
 	}
 
 	public void setAnswers1(ArrayList<Answer> answers) {
 		this.answers1 = answers;
 	}
 
 	public ArrayList<Answer> getAnswers2() {
 		return answers2;
 	}
 
 	public void setAnswers2(ArrayList<Answer> answers2) {
 		this.answers2 = answers2;
 	}
 
 	public int getSelectedAnswer1() {
 		return selectedAnswer1;
 	}
 
 	public void setSelectedAnswer1(int selectedAnswer1) {
 		this.selectedAnswer1 = selectedAnswer1;
 	}
 
 	public int getSelectedAnswer2() {
 		return selectedAnswer2;
 	}
 
 	public void setSelectedAnswer2(int selectedAnswer2) {
 		this.selectedAnswer2 = selectedAnswer2;
 	}
 
/*	public StreamedContent getBarcode() {
 		return barcode;
 	}
 
 	public void setBarcode(StreamedContent barcode) {
 		this.barcode = barcode;
 	}
 
 	
 	
 	public ArrayList<StreamedContent> getBarcodes1() {
 		return barcodes1;
 	}
 
 	public void setBarcodes1(ArrayList<StreamedContent> barcodes1) {
 		this.barcodes1 = barcodes1;
 	}
 
 	public ArrayList<StreamedContent> getBarcodes2() {
 		return barcodes2;
 	}
 
 	public void setBarcodes2(ArrayList<StreamedContent> barcodes2) {
 		this.barcodes2 = barcodes2;
 	}
*/
 	public String fromElection() {
 		int elId = Integer.valueOf(FacesContext.getCurrentInstance()
 				.getExternalContext().getRequestParameterMap().get("elId"));
 		answers1 = SqlDataProvider.getInstance().getElectionAnswers(elId);
 		answers2 = SqlDataProvider.getInstance().getElectionAnswers(elId);
		//genBarCodes();
 		return "Vote";
 	}
 
/*	private void genBarCodes() {
 		for (Answer ans : answers1) {
 			try {
 				File barcodeFile = new File("dynamicbarcode");
 				BarcodeImageHandler.saveJPEG(
 						BarcodeFactory.createCode128(ans.getAnswer()),
 						barcodeFile);
 				barcodes1.add(new DefaultStreamedContent(new FileInputStream(
 						barcodeFile), "image/jpeg"));
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 
 		for (Answer ans : answers2) {
 			try {
 				File barcodeFile = new File("dynamicbarcode");
 				BarcodeImageHandler.saveJPEG(
 						BarcodeFactory.createCode128(ans.getAnswer()),
 						barcodeFile);
 				barcodes2.add(new DefaultStreamedContent(new FileInputStream(
 						barcodeFile), "image/jpeg"));
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
*/
 	public String shuffle() {
 		Util.shuffle(answers1);
 		Util.shuffle(answers2);
 		return null;
 	}
 
 	public String encode() {
 		// showShuffle = false;
 		// codeValue = "decode";
 		return "encode";
 	}
 
 }
