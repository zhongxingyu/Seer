 package org.netvogue.server.neo4japi.service;
 
 import org.netvogue.server.neo4japi.common.ResultStatus;
 import org.netvogue.server.neo4japi.domain.Style;
 import org.netvogue.server.neo4japi.domain.Stylesheet;
 
 public interface StylesheetService {
 	public ResultStatus SaveStylesheet(Stylesheet newStylesheet, String error);
 	public ResultStatus SaveStyle(Style newStyle, String error);
 	
 	public Stylesheet getStylesheet(String id);
 	public ResultStatus editStylesheet(String id, String name, String error);
 	public ResultStatus deleteStylesheet(String id, String error);
 	
 	public Style getStyle(String styleid, String error);
	public Iterable<Style> getStylesbyCategory(String username, String category);
 	public Iterable<Style> getStyles(String StylesheetId);
 	/*public Iterable<PrintCampaignPhoto> searchPhotoByName(PrintCampaign printCampaign, String name);
 	public Iterable<PrintCampaignPhoto> searchPhotoByName(String printcampaignId, String name);*/
 	public ResultStatus deleteStyle(String styleId, String error);
 	
 }
