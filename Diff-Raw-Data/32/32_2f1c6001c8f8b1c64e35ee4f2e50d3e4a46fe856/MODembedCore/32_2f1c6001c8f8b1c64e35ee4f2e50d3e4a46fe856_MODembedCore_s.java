 package hu.cubussapiens.modembed;
 
import java.net.URL;
import java.util.Collection;

 import hu.cubussapiens.modembed.internal.ArchitectureRegistry;
 import hu.cubussapiens.modembed.internal.InstructionsetRegistry;
 
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
 
public class MODembedCore implements BundleActivator {
 
	private static BundleContext context;
 
 	private ArchitectureRegistry areg;
 	
 	private InstructionsetRegistry ireg;
 	
	static BundleContext getContext() {
		return context;
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
		MODembedCore.context = bundleContext;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
 	 */
 	public void stop(BundleContext bundleContext) throws Exception {
		MODembedCore.context = null;
 	}
 
 }
