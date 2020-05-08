 package controllers;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import net.sf.saxon.functions.Collection;
 import dataparser.Dataparser;
 
 public class SchoolController {
 	
 	public int getTotalNumberOfAuthors(){
 		return Dataparser.getTotalNumberOfDistinctAuthors();
 	}
 	
 	public int getTotalNumberOfPublications(){
 		return Dataparser.getTotalNumberOfPublications();
 	}
 	public int getNumberOfDistinctAuthorsInYear(String year){
 		return Dataparser.getNumberOfDistinctAuthorsInYear(year);
 	}
 	public int getNumberOfDistinctAuthorsInPublicationType(String type){
 		return Dataparser.getNumberOfDistinctAuthorsByType(type);
 	}
 	public Object[][] getYearBreakdownContent(){
 		List<String> years = Dataparser.getDistinctPublicationYears();
 		Object[][] tableData = new Object[years.size()][3];
 		int row = 0;
 		for(String year : years){
 			tableData[row][0] = year;
 			tableData[row][1] = Dataparser.getNumberOfPublicationsInYear(year);
 			tableData[row][2] = Dataparser.getNumberOfDistinctAuthorsInYear(year);
 			row++;
 		}
 		return tableData;
 	}
 	
 	public Object[][] getTypeBreakdownContent(){
 		List<String> types = Dataparser.getDistinctPublicationTypes();
 		Object[][] tableData = new Object[types.size()][3];
 		int row = 0;
 		for(String type : types){
 			tableData[row][0] = type;
 			tableData[row][1] = Dataparser.getNumberOfPublicationsByType(type);
 			tableData[row][2] = Dataparser.getNumberOfDistinctAuthorsByType(type);
 			row++;
 		}
 		return tableData;
 	}
 
 	public Object[][] getPublicationsFilteredByYear(String selectedYear) {
 		List<String> types = Dataparser.getDistinctPublicationTypes();
 		Object[][] tableData = new Object[types.size()][3];
 		int row = 0;
 		for(String type : types){
 			tableData[row][0] = type;
 			tableData[row][1] = Dataparser.getNumberOfPublicationsByTypeFilteredByYear(type,selectedYear);
 			tableData[row][2] = Dataparser.getNumberOfDistinctAuthorsByTypeFilteredByYear(type,selectedYear);
 			row++;
 		}
 		return tableData;
 	}
 
 	public Object[][] getPublicationsFilteredByType(String selectedType) {
 		List<String> years = Dataparser.getDistinctPublicationYears();
 		Object[][] tableData = new Object[years.size()][3];
 		int row = 0;
 		for(String year : years){
 			tableData[row][0] = year;
 			tableData[row][1] = Dataparser.getNumberOfPublicationsInYearFilteredByType(year,selectedType);
 			tableData[row][2] = Dataparser.getNumberOfDistinctAuthorsInYearFilteredByType(year,selectedType);
 			row++;
 		}
 		return tableData;
 	}
 	
 		public Object[] getAuthorListContent()
 		{
 			List<String> authorList = Dataparser.getListOfAuthors();
 			Object[] authorArray= (authorList.toArray());
 			Arrays.sort(authorArray);
 			return authorArray;
 		}
 		
 		public Object[] getAuthorListContent(String filter){
 			List<String> authorList = Dataparser.getListOfAuthors();
 			ArrayList<String> filteredList = new ArrayList<String>();
 			for (String author : authorList){
				if (author.toUpperCase().contains(filter.toUpperCase())){
 					filteredList.add(author);
 				}
 			}
 			Object[] filteredAuthorArray= (filteredList.toArray());
 			Arrays.sort(filteredAuthorArray);
 			return filteredAuthorArray;
 		}
 		
 
 }
 
