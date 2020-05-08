 package hu.cubussapiens.modembed;
 
 import hu.cubussapiens.modembed.internal.ArchitectureRegistry;
 import hu.cubussapiens.modembed.internal.InstructionsetRegistry;
 
import java.net.URL;
import java.util.Collection;
 
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;
 
public class MODembedCore extends Plugin {
 
 	private ArchitectureRegistry areg;
 	
 	private InstructionsetRegistry ireg;
 	
	private static MODembedCore plugin = null;
 
	public static MODembedCore getDefault(){
		return plugin;
	}
	
 	private ArchitectureRegistry getAReg(){
 		if (areg == null){
 			areg = new ArchitectureRegistry();
 		}
 		return areg;
 	}
 	
 	private InstructionsetRegistry getIReg(){
 		if (ireg == null){
 			ireg = new InstructionsetRegistry();
 		}
 		return ireg;
 	}
 	
 	public Collection<String> getArchitectures(){
 		return getAReg().isets.keySet();
 	}
 	
 	public String getArchName(String archID){
 		return getAReg().names.get(archID);
 	}
 	
 	public URL getInstructionSetModel(String archID){
 		String iset = getAReg().isets.get(archID);
 		if (iset != null){
 			return getIReg().models.get(iset);
 		}
 		return null;
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
 	 */
 	public void start(BundleContext bundleContext) throws Exception {
		super.start(bundleContext);
		plugin = this;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
 	 */
 	public void stop(BundleContext bundleContext) throws Exception {
		super.stop(bundleContext);
 	}
 
 }
