 /**
  * 
  */
 package org.cotrix.web.importwizard.server;
 
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Random;
 
 import javax.servlet.http.HttpSession;
 
 import org.cotrix.web.importwizard.client.ImportService;
 import org.cotrix.web.importwizard.shared.AssetDetails;
 import org.cotrix.web.importwizard.shared.AssetInfo;
 import org.cotrix.web.importwizard.shared.AttributeMapping;
 import org.cotrix.web.importwizard.shared.CsvParserConfiguration;
 import org.cotrix.web.importwizard.shared.CodeListPreviewData;
 import org.cotrix.web.importwizard.shared.CodeListType;
 import org.cotrix.web.importwizard.shared.Field;
 import org.cotrix.web.importwizard.shared.ImportMetadata;
 import org.cotrix.web.importwizard.shared.ImportProgress;
 import org.cotrix.web.importwizard.shared.ImportServiceException;
 import org.cotrix.web.importwizard.shared.Property;
 import org.cotrix.web.importwizard.shared.RepositoryDetails;
 import org.cotrix.web.importwizard.shared.FileUploadProgress;
 import org.cotrix.web.importwizard.shared.CsvParserConfiguration.NewLine;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 import com.google.gwt.view.client.Range;
 
 
 /**
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
 @SuppressWarnings("serial")
 public class ImportServiceImpl extends RemoteServiceServlet implements ImportService {
 	
 	/*@Inject
 	org.cotrix.io.ImportService service;*/
 
 	protected Logger logger = LoggerFactory.getLogger(ImportServiceImpl.class);
 
 	/** 
 	 * {@inheritDoc}
 	 * @throws ImportServiceException 
 	 */
 	@Override
 	public ArrayList<AssetInfo> getAssets(Range range) throws ImportServiceException {
 
 		try {
 
 			ArrayList<AssetInfo> assets = new ArrayList<AssetInfo>();
 
 			/*int discovered = service.discoverRemoteCodelists();
 			logger.trace("discovered "+discovered+" remote codelist");
 
 			for (Asset asset:service.remoteCodelists()){
 				System.out.println("converting "+asset.name());
 				AssetInfo assetInfo = Assets.convert(asset);
 				assets.add(assetInfo);
 			}*/
 
 			assets.add(new AssetInfo("urn:sdmx:org.sdmx.infomodel.codelist.Codelist=FAO:CL_DIVISION(0.1)", "CL_DIVISION", "sdmx/codelist", "D4Science Development Registry"));
 			assets.add(new AssetInfo("321", "Gears", "SDMX", "D4Science Development Registry"));
 			assets.add(new AssetInfo("333", "Species Year 2013", "CSV", "D4Science Development Registry"));
 			assets.add(new AssetInfo("324", "Country", "SDMX", "D4Science Development Registry"));
 
 			System.out.println("returning "+assets.size()+" elements");
 
 			return assets;
 		} catch(Exception e)
 		{
 			e.printStackTrace();
 
 			logger.error("Error retrieving assets", e);
 			throw new ImportServiceException(e.getMessage());
 		}
 	}
 
 
 
 
 	/** 
 	 * {@inheritDoc}
 	 * @throws ImportServiceException 
 	 */
 	@Override
 	public AssetDetails getAssetDetails(String assetId) throws ImportServiceException {
 
 		/*Asset asset = getAsset(assetId);
 		if (asset == null) throw new ImportServiceException("Asset with id "+assetId+" not found");
 		AssetDetails details = Assets.convertToDetails(asset);
 		System.out.println(details);
 		return details;*/
 
 		List<Property> properties = new ArrayList<Property>();
 		properties.add(new Property("scope", "/gcube/devsec", "null"));
 		RepositoryDetails repository = new RepositoryDetails("D4Science Development Registry", "sdmx/codelist",  "sdmx/codelist", properties);
 
 		properties = new ArrayList<Property>();
 		properties.add(new Property("uri", 
 				"http://node8.d.d4science.research-infrastructures.eu:8080/FusionRegistry/ws/rest/codelist/FAO/CL_DIVISION/0.1", 
 				"asset's URI"));
 		properties.add(new Property("status", "partial", "asset's status"));
 		AssetDetails assetDetails = new AssetDetails("urn:sdmx:org.sdmx.infomodel.codelist.Codelist=FAO:CL_DIVISION(0.1)", 
 				"CL_DIVISION", "sdmx/codelist", properties, repository);
 
 		return assetDetails;
 	}
 	
 	Random random = new Random();
 
 	@Override
 	public FileUploadProgress getUploadProgress() throws ImportServiceException {
 		
 		HttpSession httpSession = this.getThreadLocalRequest().getSession();
 		WizardImportSession importSession = WizardImportSession.getImportSession(httpSession);
 		FileUploadProgress uploadProgress = importSession.getUploadProgress();
 		if (uploadProgress == null) {
 			logger.error("Unexpected upload progress null.");
 			throw new ImportServiceException("Upload progress not available");
 		}
 		return uploadProgress;
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public CodeListPreviewData getPreviewData() throws ImportServiceException {
 		//TODO implement it
 		List<String> header = Arrays.asList("ISSCAAP","TAXOCODE","3A_CODE","Scientific_name","English_name","French_name","Spanish_name","Author","Family","Order","Stats_data");
 		List<List<String>> data = Arrays.<List<String>>asList(Arrays.asList("25","1020100501","LAF","Eudontomyzon mariae","Ukrainian brook lamprey","Lamproie ukrainienne","","(Berg 1931)","Petromyzontidae","PETROMYZONTIFORMES","0"),
 				Arrays.asList("25","1020100502","ICJ","Eudontomyzon danfordi","Carpathian lamprey","Lamproie carpathique","","Regan 1911","Petromyzontidae","PETROMYZONTIFORMES","0"),
 				Arrays.asList("25","1020100503","IKG","Eudontomyzon graecus","","","","Renaud & Economidis 2010","Petromyzontidae","PETROMYZONTIFORMES","0"),
 				Arrays.asList("25","1020100504","IKJ","Eudontomyzon hellenicus","Greek brook Lamprey","Lamproie de ruisseau grecque","","Vladykov, Renaud, Kott & Economidis 1982","Petromyzontidae","PETROMYZONTIFORMES","0"),
 				Arrays.asList("25","1020100505","IKL","Eudontomyzon morii","Korean lamprey","Lamproie coréene","","(Berg 1931)","Petromyzontidae","PETROMYZONTIFORMES","0"),
 				Arrays.asList("25","1020100901","LAW","Caspiomyzon wagneri","Caspian lamprey","Lamproie caspienne","","(Kessler 1870)","Petromyzontidae","PETROMYZONTIFORMES","0"),
 				Arrays.asList("25","1020101101","LSZ","Lethenteron camtschaticum","Arctic lamprey","Lamproie arctique","","(Tilesius 1811)","Petromyzontidae","PETROMYZONTIFORMES","0"),
 				Arrays.asList("25","1020101102","IDQ","Lethenteron kessleri","Siberian lamprey","Lamproie de Sibérie","","(Anikin 1905)","Petromyzontidae","PETROMYZONTIFORMES","0"),
 				Arrays.asList("25","1020101103","IDT","Lethenteron ninae","Western Transcaucasian lamprey","Lamproie de la Transcaucasie","","Naseka, Tuniyev & Renaud 2009","Petromyzontidae","PETROMYZONTIFORMES","0"));
 		CodeListPreviewData previewData = new CodeListPreviewData(header, header.size(), data);
 		return previewData;
 	}
 
 	@Override
 	public CodeListType getCodeListType() throws ImportServiceException {
 		HttpSession httpSession = this.getThreadLocalRequest().getSession();
 		WizardImportSession importSession = WizardImportSession.getImportSession(httpSession);
 		
 		return importSession.getCodeListType();
 	}
 
 	@Override
 	public ImportMetadata getMetadata() throws ImportServiceException {
 		ImportMetadata metadata = new ImportMetadata();
 		metadata.setName("Asfis sp Feb 2012");
 		//metadata.setRowCount(12000);
 		return metadata;
 	}
 
 	@Override
 	public CsvParserConfiguration getCsvParserConfiguration() throws ImportServiceException {
 		CsvParserConfiguration configuration = new CsvParserConfiguration();
 		configuration.setComment('#');
 		configuration.setCharset("UTF-8");
 		configuration.setFieldSeparator(',');
 		configuration.setHasHeader(true);
 		configuration.setLineSeparator(NewLine.LF);
 		configuration.setQuote('"');
 		configuration.setAvailablesCharset(getEncodings());
 		return configuration;
 	}
 	
 	protected List<String> getEncodings()
 	{
 		List<String> charsets = new ArrayList<String>();
 		for (Charset charset:Charset.availableCharsets().values()) {
 			charsets.add(charset.displayName());
 		}
 		return charsets;
 	}
 
 	@Override
 	public void updateCsvParserConfiguration(CsvParserConfiguration configuration) throws ImportServiceException {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
	public void updateMetadata(ImportMetadata metadata)
			throws ImportServiceException {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public List<AttributeMapping> getMapping() throws ImportServiceException {
 		List<AttributeMapping> mapping = new ArrayList<AttributeMapping>();
 		
 		String[] headers = new String[]{"ISSCAAP","TAXOCODE","3A_CODE","Scientific_name","English_name","French_name","Spanish_name","Author","Family","Order","Stats_data"};
 
 		for (String header:headers) {
 			Field field = new Field();
 			field.setId(header);
 			field.setLabel(header);
 			
 			AttributeMapping attributeMapping = new AttributeMapping();
 			attributeMapping.setField(field);
 			mapping.add(attributeMapping);
 		}
 
 		return mapping;
 	}
 
 	@Override
 	public void startImport(ImportMetadata metadata, List<AttributeMapping> columns) throws ImportServiceException {
 		importProgress = 0;
 	}
 	
 	int importProgress = 0;
 
 	@Override
 	public ImportProgress getImportProgress() throws ImportServiceException {
 		importProgress += random.nextInt(30);
 		ImportProgress progress = new ImportProgress();
 		progress.setStatus(importProgress>100?ImportProgress.Status.DONE:ImportProgress.Status.ONGOING);
 		progress.setReport("All fine p: "+importProgress);
 		return progress;
 	}
 
 
 	/*protected Asset getAsset(String id)
 	{
 		for (Asset asset:service.remoteCodelists()) if (asset.id().equals(id)) return asset;
 		return null;
 	}*/
 
 
 
 }
