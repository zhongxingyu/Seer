 package org.bladerunnerjs.plugin.plugins.bundlers.thirdparty;
 
 
 import java.io.File;
 import java.io.IOException;
 import java.io.Reader;
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.bladerunnerjs.model.AssetLocation;
 import org.bladerunnerjs.model.BundlableNode;
 import org.bladerunnerjs.model.JsLib;
 import org.bladerunnerjs.model.NonBladerunnerJsLibManifest;
 import org.bladerunnerjs.model.SourceModule;
 import org.bladerunnerjs.model.SourceModulePatch;
 import org.bladerunnerjs.model.exception.ConfigException;
 import org.bladerunnerjs.model.exception.ModelOperationException;
 import org.bladerunnerjs.plugin.plugins.bundlers.nodejs.NodeJsSourceModule;
 import org.bladerunnerjs.utility.RelativePathUtility;
 import org.bladerunnerjs.utility.UnicodeReader;
 
 import com.Ostermiller.util.ConcatReader;
 
 
 public class ThirdpartySourceModule implements SourceModule
 {
 
 	private AssetLocation assetLocation;
 	private File dir;
 	private NonBladerunnerJsLibManifest manifest;
 	private String assetPath;
 	private SourceModulePatch patch;
 	private String defaultInputEncoding;
 	
 	@Override
 	public void initialize(AssetLocation assetLocation, File dir, String assetName)
 	{
 		try {
 			this.assetLocation = assetLocation;
 			this.dir = dir;
 			assetPath = RelativePathUtility.get(assetLocation.getAssetContainer().getApp().dir(), dir);
 			defaultInputEncoding = assetLocation.root().bladerunnerConf().getDefaultInputEncoding();
 		}
 		catch (ConfigException e) {
 			throw new RuntimeException(e);
 		}
 	}
 	
 	@Override
 	public Reader getReader() throws IOException
 	{
 		List<Reader> fileReaders = new ArrayList<>();
 		
 		try {
 			String defineBlockHeader = String.format(NodeJsSourceModule.NODEJS_DEFINE_BLOCK_HEADER, getRequirePath());
 			String defineBlockExports = "module.exports = " + manifest.getExports();
 			String defineBlockFooter = NodeJsSourceModule.NODEJS_DEFINE_BLOCK_FOOTER;
 			
 			// If package.json exists then the code being loaded expects module and exports variables to be present
 			// the define block supplies those. So wrap the code in the define block.
			Boolean packageJsonExists = !assetLocation.getAssetContainer().file("package.json").isFile();
 			
 			if(packageJsonExists)
 			{
 				fileReaders.add( new StringReader( defineBlockHeader ) );
 			}
 			
 			for(File file : manifest.getJsFiles()) {
 				fileReaders.add(new UnicodeReader(file, defaultInputEncoding));
 				fileReaders.add(new StringReader("\n\n"));
 			}
 			
 			if (packageJsonExists)
 			{
     			fileReaders.add( new StringReader( defineBlockExports ) );
     			fileReaders.add( new StringReader( defineBlockFooter ) );
 			}
 			else
 			{
     			fileReaders.add( new StringReader( defineBlockHeader ) );
     			fileReaders.add( new StringReader( defineBlockExports ) );
     			fileReaders.add( new StringReader( defineBlockFooter ) );
 			}
 			
 			fileReaders.add(patch.getReader());
 		}
 		catch (ConfigException | IOException e)
 		{
 			throw new RuntimeException(e);
 		}
 		
 		
 		return new ConcatReader(fileReaders.toArray(new Reader[]{}));
 	}
 	
 	@Override
 	public AssetLocation getAssetLocation()
 	{
 		return assetLocation;
 	}
 	
 	@Override
 	public File dir()
 	{
 		return dir;
 	}
 	
 	@Override
 	public String getAssetName() {
 		return "";
 	}
 	
 	@Override
 	public String getAssetPath() {
 		return assetPath;
 	}
 	
 	public void initManifest(NonBladerunnerJsLibManifest manifest)
 	{
 		if (this.manifest == null)
 		{
 			this.manifest= manifest;
 		}
 	}
 
 	@Override
 	public List<SourceModule> getDependentSourceModules(BundlableNode bundlableNode) throws ModelOperationException
 	{
 		Set<SourceModule> dependentLibs = new HashSet<SourceModule>();
 		
 		try 
 		{
 			for (String dependentLibName : manifest.getDepends())
 			{
 				JsLib dependentLib = assetLocation.getAssetContainer().getApp().nonBladeRunnerLib(dependentLibName);
 				if (!dependentLib.dirExists())
 				{
 					throw new ConfigException(String.format("Library '%s' depends on '%s', which doesn't exist.", getAssetName(), dependentLibName)) ;
 				}
 				dependentLibs.addAll(dependentLib.sourceModules());
 			}
 		}
 		catch (ConfigException ex)
 		{
 			new ModelOperationException( ex );
 		}
 		
 		return new ArrayList<SourceModule>( dependentLibs );
 	}
 
 	@Override
 	public List<String> getAliasNames() throws ModelOperationException
 	{
 		return new ArrayList<String>();
 	}
 
 	@Override
 	public String getRequirePath()
 	{
 		return dir.getName();
 	}
 	
 	@Override
 	public String getClassname() {
 		return null;
 	}
 	
 	@Override
 	public boolean isEncapsulatedModule() {
 		return false;
 	}
 	
 	@Override
 	public List<SourceModule> getOrderDependentSourceModules(BundlableNode bundlableNode) throws ModelOperationException
 	{
 		return getDependentSourceModules(bundlableNode);
 	}
 
 	@Override
 	public void addPatch(SourceModulePatch patch)
 	{
 		this.patch = patch;
 	}
 	
 }
