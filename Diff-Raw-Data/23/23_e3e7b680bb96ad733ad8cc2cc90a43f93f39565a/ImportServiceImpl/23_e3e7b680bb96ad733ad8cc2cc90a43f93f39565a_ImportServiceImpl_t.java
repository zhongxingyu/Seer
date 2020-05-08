 /**
  * 
  */
 package org.cotrix.web.importwizard.server;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.inject.Inject;
 
 import org.cotrix.web.importwizard.client.ImportService;
 import org.cotrix.web.importwizard.shared.AssetDetails;
 import org.cotrix.web.importwizard.shared.AssetInfo;
 import org.cotrix.web.importwizard.shared.ImportServiceException;
 import org.cotrix.web.importwizard.shared.Property;
 import org.cotrix.web.importwizard.shared.RepositoryDetails;
 import org.cotrix.web.share.shared.CotrixImportModel;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.virtualrepository.Asset;
 
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
 
 	@Override
 	public boolean sendToServer(CotrixImportModel model) throws IllegalArgumentException {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public void testBackendConnection() throws IllegalArgumentException {
 		// TODO Auto-generated method stub
 
 	}
 
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
 
	/*protected Asset getAsset(String id)
 	{
 		for (Asset asset:service.remoteCodelists()) if (asset.id().equals(id)) return asset;
 		return null;
	}*/
 
 
 
 }
