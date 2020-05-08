 package org.netvogue.server.neo4japi.service;
 
 import org.netvogue.server.neo4japi.common.ResultStatus;
 import org.netvogue.server.neo4japi.domain.Style;
 import org.netvogue.server.neo4japi.domain.Stylesheet;
 import org.netvogue.server.neo4japi.repository.StylesheetRepository;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.neo4j.support.Neo4jTemplate;
 
 public class StylesheetServiceImpl implements StylesheetService {
 
 	@Autowired Neo4jTemplate		neo4jTemplate;
 	@Autowired StylesheetRepository stylesheetRepo;
 	
 	public ResultStatus SaveStylesheet(Stylesheet newStylesheet, String error) {
 		try {
 			//New Categories node will be created an relationship will also be added for this.
 			//Saving it through Template instead of boutiquerepo so that categories node can also be saved
 			neo4jTemplate.save(newStylesheet);
 			System.out.println("Saved Stylesheet Successfully with styles:" + newStylesheet.getStyles().size());
 			return ResultStatus.SUCCESS;
 		} catch(Exception e) {
 			System.out.println("There was an error for" + newStylesheet.getStylesheetname() + " - " + e.toString());
 			error = e.toString();
 			return ResultStatus.FAILURE;
 		}
 	}
 	
 	public ResultStatus SaveStyle(Style newStyle, String error) {
 		try {
 			//New Categories node will be created an relationship will also be added for this.
 			//Saving it through Template instead of boutiquerepo so that categories node can also be saved
 			neo4jTemplate.save(newStyle);
 			System.out.println("Saved Style Successfully");
 			return ResultStatus.SUCCESS;
 		} catch(Exception e) {
 			System.out.println("There was an error for" + newStyle.getStylename());
 			error = e.toString();
 			return ResultStatus.FAILURE;
 		}
 	}
 	public Stylesheet getStylesheet(String stylesheetId) {
 		//galleryRepo.findByPropertyValue(arg0, galleryId);
 		return stylesheetRepo.getStylesheet(stylesheetId);
 	}
 	
 	public ResultStatus editStylesheet(String stylesheetId, String name, String error) {
 		try {
 			stylesheetRepo.editStylesheet(stylesheetId, name);
 			return ResultStatus.SUCCESS;
 		} catch(Exception e) {
 			System.out.println("There was an error while editing style sheet" + stylesheetId + " - " + e.toString());
 			error = e.toString();
 			return ResultStatus.FAILURE;
 		}
 	}
 	
 	public ResultStatus deleteStylesheet(String stylesheetId, String error)  {
 		try {
 			stylesheetRepo.deleteStylesheet(stylesheetId);
 			System.out.println("deleted style sheet:" + stylesheetId);
 			return ResultStatus.SUCCESS;
 		} catch(Exception e) {
 			System.out.println("There was an error while deleting stylesheetId:" + stylesheetId + " - " + e.toString());
 			error = e.toString();
 			return ResultStatus.FAILURE;
 		}
 	}
 	
 	public Style getStyle(String styleId, String error) {
 		if(!styleId.isEmpty()) {
 			return stylesheetRepo.getStyle(styleId);
 		} else {
 			error = "empty styleid";
 		}
 		return null;
 	}
 	public Iterable<Style> getStyles(String stylesheetId) {
 		if(!stylesheetId.isEmpty()) {
 			return stylesheetRepo.getStyles(stylesheetId);
 		}
 		return null;
 	}
	public Iterable<Style> getStylesbyCategory(String username, String category) {
 		if(!category.isEmpty()) {
			return stylesheetRepo.getStylesByCategory(username, category);
 		}
 		return null;
  	}
 	public ResultStatus deleteStyle(String styleId, String error)  {
 		if(null == styleId || styleId.isEmpty()){
 			error = "styleId is empty";
 			return ResultStatus.FAILURE;
 		}
 		try {
 			stylesheetRepo.deleteStyle(styleId);
 			return ResultStatus.SUCCESS;
 		} catch(Exception e) {
 			System.out.println("There was an error while deleting Style" + styleId + " - " + e.toString());
 			error = e.toString();
 			return ResultStatus.FAILURE;
 		}
 	}
 }
