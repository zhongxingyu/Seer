 package ecologylab.appframework.types;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import ecologylab.serialization.ElementState;
 import ecologylab.serialization.annotations.simpl_collection;
 import ecologylab.serialization.annotations.simpl_inherit;
 import ecologylab.serialization.annotations.simpl_nowrap;
 
 /**
  * Handles the loading and parsing of the asset version XML file
  *  
  * @author robinson
  * @author andruid
  */
 @simpl_inherit public class AssetsState extends ElementState
 {
 	HashMap<String, AssetState>	assetsMap	= new HashMap<String, AssetState>();
 	
 	@simpl_collection("asset")
 	@simpl_nowrap
 	ArrayList<AssetState> assetStates;
 	
 	public ArrayList<AssetState> getAssetStates() {
 		if (assetStates != null)
 			return assetStates;
 		return assetStates = new ArrayList<AssetState>();
 	}
 
	/**
	 * Perform custom processing on the newly created child node,
	 * just before it is added to this.
	 * <p/>
	 * This is part of depth-first traversal during translateFromXML().
	 * <p/>
	 * This, the default implementation, does nothing.
	 * Sub-classes may wish to override.
	 * 
	 * @param child
	 */
	protected void createChildHook(ElementState child)
 	{
		AssetState asset	= (AssetState) child;
		register(asset);
 	}
 
 	/**
 	 * @param asset
 	 */
 	private void register(AssetState asset)
 	{
 		assetsMap.put(asset.getId(), asset);
 	}
 	
 	public AssetState lookup(String id)
 	{
 		return assetsMap.get(id);
 	}
 	public AssetState lookupAndUpdate(String id)
 	{
 		AssetState asset = lookup(id);
 		if (asset == null)
 		{
 			asset = update(id);
 		}
 		return asset;
 	}
 
 	/**
 	 * @return
 	 */
 	public AssetState update(String id)
 	{
 		AssetState asset	= new AssetState(id);
 		getAssetStates().add(asset);
 		register(asset);
 		return asset;
 	}
 
 	public boolean contains(String id)
 	{
 		HashMap<String, AssetState> hashMap = assetsMap;
 		return hashMap.containsKey(id);
 	}
 }
