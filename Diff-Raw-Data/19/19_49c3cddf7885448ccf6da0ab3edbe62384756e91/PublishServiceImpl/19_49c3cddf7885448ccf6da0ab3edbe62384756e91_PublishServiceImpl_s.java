 package org.cotrix.web.publish.server;
 
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import javax.inject.Inject;
 
 import org.cotrix.domain.Codelist;
 import org.cotrix.repository.CodelistRepository;
 import org.cotrix.web.publish.client.PublishService;
 import org.cotrix.web.share.shared.CotrixImportModel;
 import org.cotrix.web.share.shared.Metadata;
 import org.cotrix.web.share.shared.UICodelist;
 
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 
 /**
  * The server side implementation of the RPC service.
  */
 @SuppressWarnings("serial")
 public class PublishServiceImpl extends RemoteServiceServlet implements
 		PublishService {
 
 	@Inject CodelistRepository repository;
 	
 	public ArrayList<UICodelist> getAllCodelists()throws IllegalArgumentException {
 		ArrayList<UICodelist> list = new ArrayList<UICodelist>();
 		Iterator<Codelist> it  = repository.queryFor(allLists()).iterator();
 		while (it.hasNext()) {
 			Codelist codelist = (Codelist) it.next();
 			UICodelist c = new UICodelist();
 			c.setName(codelist.name().toString());
 			c.setId(codelist.id());
 			list.add(c);
 		}
 	
 		// Just for testing perpose.
 		/*Codelist c = new Codelist();
 		c.setName("Test code list");
 		c.setId(1);
 		list.add(c);*/
 		return list;
 	}
 	public CotrixImportModel getCodeListModel(String codelistId) {
 		CotrixImportModel model = null;
 		return model;
 	}
 	
 	private CotrixImportModel getGearType() {
 		Metadata meta = new Metadata();
 		meta.setName("Gear Type");
 		meta.setOwner("FAO");
 		meta.setVersion("1.9");
 		meta.setDescription("");
 		
 		CotrixImportModel model = new CotrixImportModel();
 		model.setMetadata(meta);
 		return model;
 	}
 
 	private CotrixImportModel getVesselType(){
 		Metadata meta = new Metadata();
 		meta.setName("Vessel Type");
 		meta.setOwner("FAO");
 		meta.setVersion("1.9");
 		meta.setDescription("");
 		
 		
 		CotrixImportModel model = new CotrixImportModel();
 		model.setMetadata(meta);
 		return model;
 	}
 	private CotrixImportModel getFAOMajorWaterArea(){
 		Metadata meta = new Metadata();
 		meta.setName("FAO Major Water Area");
 		meta.setOwner("FAO");
 		meta.setVersion("0.9");
 		meta.setDescription("");
 		
 		
 		
 		CotrixImportModel model = new CotrixImportModel();
 		model.setMetadata(meta);
 		return model;
 	}
 	private CotrixImportModel getLanguage(){
 		Metadata meta = new Metadata();
 		meta.setName("Language");
 		meta.setOwner("FAO");
 		meta.setVersion("0.9");
 		meta.setDescription("ISO 639-1 defines abbreviations for languages. In HTML and XHTML they can be used in the lang and xml:lang attributes");
 		
 		CotrixImportModel model = new CotrixImportModel();
 		model.setMetadata(meta);
 		return model;
 	}
 	private CotrixImportModel getContinent(){
 		Metadata meta = new Metadata();
 		meta.setName("Continent");
 		meta.setOwner("FAO");
 		meta.setVersion("0.9");
 		meta.setDescription("This data was compiled by hand from the above and may contain errors. One small modification is that the various insular areas of the United State listed above are recorded here as the single United States Minor Outlying Islands.");
 		
 		
 		CotrixImportModel model = new CotrixImportModel();
 		model.setMetadata(meta);
 		return model;
 	}
 	private CotrixImportModel getCountry(){
 		Metadata meta = new Metadata();
 		meta.setName("Country");
 		meta.setOwner("FAO");
 		meta.setVersion("0.9");
 		meta.setDescription("CountryCode.org is your complete guide to call anywhere in the world. The calling chart below will help you find the dialing codes you need to make long distance phone calls to friends, family, and business partners around the globe. Simply find and click the country you wish to call. You'll find instructions on how to call that country using its country code, as well as other helpful information like area codes, ISO country codes, and the kinds of electrical outlets and phone jacks found in that part of the world. Making a phone call has never been easier with CountryCode.org.");
 		
 		CotrixImportModel model = new CotrixImportModel();
 		model.setMetadata(meta);
 		return model;
 	}
 	private CotrixImportModel getASFIS(){
 		Metadata meta = new Metadata();
 		meta.setName("ASFIS");
 		meta.setOwner("FAO");
 		meta.setVersion("0.9");
 		meta.setDescription("ASFIS list of species includes 12 000 species items selected according to their interest or relation to fisheries and aquaculture. For each species item stored in a record, codes (ISSCAAP group, taxonomic and 3-alpha) and taxonomic information (scientific name, author(s), family, and higher taxonomic classification) are provided. An English name is available for most of the records, and about one third of them have also a French and Spanish name. Information is also provided about the availability of fishery production statistics on the species item in the FAO databases.");
 		
 		CotrixImportModel model = new CotrixImportModel();
 		model.setMetadata(meta);
 		return model;
 	}
 
 	public ArrayList<String[]> getDataRange(int id, int start, int end) {
 		ArrayList<String[]> data = new ArrayList<String[]>();
 		switch (id) {
 		case 0:
 			data = Util.readFile(this.getThreadLocalRequest().getSession().getServletContext().getRealPath("files/Country.txt"),";");
 			break;
 		case 1:
 			data = Util.readFile(this.getThreadLocalRequest().getSession().getServletContext().getRealPath("files/Continent.txt"),",");
 			break;
 		case 2:
 			ArrayList<String[]> line = Util.readFile(this.getThreadLocalRequest().getSession().getServletContext().getRealPath("files/ASFIS_sp_Feb_2012.txt"),"\t");
 			data = new ArrayList<String[]>(line.subList(1, line.size()));
 			break;
 		case 3:
 			data = Util.readFile(this.getThreadLocalRequest().getSession().getServletContext().getRealPath("files/Language.txt"),";");
 			break;
 		case 4:
 			break;
 		case 5:
 			break;
 		case 6:
 			break;
 
 		default:
 			break;
 		}
 		
 		if(data.size() < end){
 			end = data.size();
 		}
 		return new ArrayList<String[]>(data.subList(start, end));
 	}
 }
