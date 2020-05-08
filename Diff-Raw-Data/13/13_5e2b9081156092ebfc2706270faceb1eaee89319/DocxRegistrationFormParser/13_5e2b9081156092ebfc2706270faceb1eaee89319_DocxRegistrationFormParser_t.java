 package net.ostis.confman.model.registrationform.wordparser;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import net.ostis.confman.model.registrationform.AuthorInformation;
 import net.ostis.confman.model.registrationform.RegistrationForm;
 import net.ostis.confman.model.registrationform.RegistrationFormConstant;
 
 import org.apache.poi.xwpf.usermodel.XWPFDocument;
 import org.apache.poi.xwpf.usermodel.XWPFTable;
 import org.apache.poi.xwpf.usermodel.XWPFTableCell;
 import org.apache.poi.xwpf.usermodel.XWPFTableRow;
 
 public class DocxRegistrationFormParser {
 	
 	private RegistrationForm registrationForm;	 
 	private List<String> allInformation;
 	
 	public DocxRegistrationFormParser() {
 		super();
 		registrationForm = new RegistrationForm();
 		allInformation = new ArrayList<String>();
 	}
 	
 	public RegistrationForm parse(FileInputStream inputStream) {
             try {
 				XWPFDocument document = new XWPFDocument(inputStream);
 				List<XWPFTable> tables = document.getTables();
 				readTables(tables);
 			} 
             catch (IOException e) {
 				e.printStackTrace();
 			}	
             return registrationForm;
 	}
 
 	private void readTables(List<XWPFTable> tables) {
 		for(XWPFTable table : tables){
 			for(XWPFTableRow row : table.getRows()){
 				for(XWPFTableCell cell : row.getTableCells()){
 					if(row.getTableCells().indexOf(cell)==1 && table.getRows().indexOf(row)!=0){
 						allInformation.add(cell.getText());
 					} 
 				}
 			}
 		}
 		parseAllInform(allInformation);
 	}
 
 	private void parseAllInform(List<String> information) {	
 		int counter = 0;
 		AuthorInformation authorInformation = new AuthorInformation();
 		for(String info : information){
 			infoAllocation(counter, info, authorInformation);
 			counter++;
 		}
 	}
 
 	private void infoAllocation(int counter, String info, AuthorInformation authorInformation) {
 		int currrentAuthorNumber = 0;
 		if(counter<5) {
 			completeArticlePart(counter, info);
 		}
 		else {
 			if(currrentAuthorNumber == getAuthorNumber(counter)){
 				int index = spotIndex(counter, getAuthorNumber(counter));
 				completeAuthorsPart(index, info, authorInformation);
 			}
 			else{
 				addAuthorInformation(counter, info, authorInformation);
 				currrentAuthorNumber = getAuthorNumber(counter);
 			}
 		}
 	}
 
 	private void addAuthorInformation(int counter, String info,
 			AuthorInformation authorInformation) {
 		registrationForm.getAuthorsInformation().add(authorInformation);
 		authorInformation = new AuthorInformation();
 		int index = spotIndex(counter, getAuthorNumber(counter));
 		completeAuthorsPart(index, info, authorInformation);
 	}
 
 	private int spotIndex(int counter, int authorNumber) {
		return counter-(authorNumber*RegistrationFormConstant.NUMBER_AUTORS_ITEMS);
 	}
 
 	private void completeAuthorsPart(int index, String info, AuthorInformation authorInformation) {
 		switch (index) {		
 		case RegistrationFormConstant.SECOND_NAME:{
 			authorInformation.getPersonalInformation().setSecondName(info);
 			break;
 		}			
 		case RegistrationFormConstant.FIRST_NAME:{
 			authorInformation.getPersonalInformation().setFirstName(info);
 			break;
 		}	
 		case RegistrationFormConstant.THIRD_NAME:{
 			authorInformation.getPersonalInformation().setThirdName(info);
 			break;
 		}
 		case RegistrationFormConstant.ACADEMIC_DEGREE:{
 			authorInformation.getPersonalInformation().setAcademicDegree(info);
 			break;
 		}	
 		case RegistrationFormConstant.ACADEMIC_TITLE:{
 			authorInformation.getPersonalInformation().setAcademicTitle(info);
 			break;
 		}		
 		case RegistrationFormConstant.COUNTRY:{
 			authorInformation.getContactInformation().setCountry(info);
 			break;
 		}			
 		case RegistrationFormConstant.CITY:{
 			authorInformation.getContactInformation().setCity(info);
 			break;
 		}	
 		case RegistrationFormConstant.EMAIL:{
 			authorInformation.getContactInformation().seteMail(info);
 			break;
 		}
 		case RegistrationFormConstant.CONTACT_PHONE_NUMBER:{
 			authorInformation.getContactInformation().setContactPhoneNumber(info);
 			break;
 		}	
 		case RegistrationFormConstant.AFFLIATION:{
 			authorInformation.getWorkPlaceInformation().setAffliation(info);
 			break;
 		}	
 		case RegistrationFormConstant.POSITION:{
 			authorInformation.getWorkPlaceInformation().setPosition(info);
 			break;
 		}	
 		default:
 			break;
 		}	
 		
 	}
 
 	private int getAuthorNumber(int counter) {
		return (counter-RegistrationFormConstant.NUMBER_ARTICLE_ITEMS)/RegistrationFormConstant.NUMBER_AUTORS_ITEMS;
 	}
 
 	private void completeArticlePart(int counter, String info) {
 		switch (counter) {		
 		case RegistrationFormConstant.TITLE_ENTRY:{
 			registrationForm.getArticleInformation().setTitleEntry(info);
 			break;
 		}			
 		case RegistrationFormConstant.CO_AUTHOR:{
 			parseCoAuthors(info);
 			break;
 		}	
 		case RegistrationFormConstant.PARTICIPATION_FORM:{
 			registrationForm.getArticleInformation().setParticipationForm(info);
 			break;
 		}
 		case RegistrationFormConstant.SPEAKER:{
 			registrationForm.getArticleInformation().setSpeaker(info);
 			break;
 		}	
 		case RegistrationFormConstant.SHOW_LAUNCHING:{
 			registrationForm.getArticleInformation().setShowLaunching(info);
 			break;
 		}		
 		default:
 			break;
 		}			
 	}
 
 	private void parseCoAuthors(String info) {
 		for(int index = 0; index<info.length(); index++){
 			if(",".equals(info.substring(index, index))){
 				
 			}			
 		}		
 	}
 }
