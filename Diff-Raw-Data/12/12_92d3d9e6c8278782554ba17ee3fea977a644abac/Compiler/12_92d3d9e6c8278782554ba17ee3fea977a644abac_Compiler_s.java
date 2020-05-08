 /**
  * 
  */
 package hu.cubussapiens.modembed.modularasm.compiler.internal;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 import hexfile.AddressType;
 import hexfile.Entry;
 import hexfile.HexFile;
 import hexfile.HexfileFactory;
 import hu.cubussapiens.modembed.hexfile.persistence.HexFileResource;
 import hu.cubussapiens.modembed.modularasm.compiler.CompilerException;
 import hu.cubussapiens.modembed.modularasm.compiler.IArchitectureResolver;
 import hu.cubussapiens.modembed.modularasm.compiler.ICompiler;
 import hu.cubussapiens.modembed.modularasm.compiler.IModuleConfigurationHandler;
 import hu.cubussapiens.modembed.modularasm.compiler.IModuleResolver;
 import hu.cubussapiens.modembed.modularasm.compiler.IPostBuildProcess;
 import hu.cubussapiens.modembed.modularasm.modularASM.Module;
 
 import memory.MemoryModel;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.SubProgressMonitor;
 
 /**
  * @author balage
  *
  */
 public class Compiler implements ICompiler {
 
 	public Module main;
 	public IArchitectureResolver archresolver;
 	public IModuleResolver modresolver;
 	public MemoryModel memmodel;
 	public IModuleConfigurationHandler confighandler;
 	
 	private final List<IPostBuildProcess> postBuilds = new ArrayList<IPostBuildProcess>();
 	
 	/* (non-Javadoc)
 	 * @see hu.cubussapiens.modembed.modularasm.builder.ICompiler#setMain(hu.cubussapiens.modembed.modularasm.modularASM.Module)
 	 */
 	@Override
 	public void setMain(Module main) {
 		this.main = main;
 	}
 
 	/* (non-Javadoc)
 	 * @see hu.cubussapiens.modembed.modularasm.builder.ICompiler#setArchResolver(hu.cubussapiens.modembed.modularasm.builder.IArchitectureResolver)
 	 */
 	@Override
 	public void setArchResolver(IArchitectureResolver resolver) {
 		this.archresolver = resolver;
 	}
 
 	/* (non-Javadoc)
 	 * @see hu.cubussapiens.modembed.modularasm.builder.ICompiler#setModuleResolver(hu.cubussapiens.modembed.modularasm.builder.IModuleResolver)
 	 */
 	@Override
 	public void setModuleResolver(IModuleResolver resolver) {
 		this.modresolver = resolver;
 	}
 	
 	
 	/* (non-Javadoc)
 	 * @see hu.cubussapiens.modembed.modularasm.builder.ICompiler#compile(org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	@Override
 	public HexFile compile(IProgressMonitor monitor) throws CompilerException{
 		
 		monitor.beginTask("Compiling..", 100+(postBuilds.size()*10));
 		
 		/*
 		 * Instantiate main module, which will trigger the preparation
 		 * of the full software architecture
 		 */
 		CompilationManager cmanager = new CompilationManager(this);
 		ModuleInstance mainInstance = cmanager.instantiate(main);
		if (mainInstance != null){
			cmanager.symbolManager.setMainModuleInstance(mainInstance);
		}
 		monitor.worked(1);
 		
 		/*
 		 * Preparing instances
 		 */
 		IProgressMonitor sm = new SubProgressMonitor(monitor, 20);
 		sm.beginTask("Preparing modules..", cmanager.instances.size());
 		for(ModuleInstance mi : cmanager.instances){
 			mi.prepare();
 			sm.worked(1);
 		}
 		sm.done();
 		
 		/*
 		 * Preparing function instances
 		 */
 		sm = new SubProgressMonitor(monitor, 29);
 		sm.beginTask("Compiling functions..", cmanager.symbolManager.functions.size());
 		for(FunctionInstance fi : cmanager.symbolManager.functions){
 			fi.prepare();
 			sm.worked(1);
 		}
 		sm.done();
 		
 		/*
 		 * Allocating RAM
 		 */
 		sm = new SubProgressMonitor(monitor, 10);
 		sm.beginTask("Allocating RAM..", cmanager.symbolManager.variables.size());
 		MemoryAllocator allocator = new MemoryAllocator(memmodel.getRam().getSegments());
 		for(VariableInstance vi : cmanager.symbolManager.variables){
 			vi.address = (int) allocator.allocate(1);
 			sm.worked(1);
 		}
 		sm.done();
 		
 		/*
 		 * Allocating progmem
 		 */
 		sm = new SubProgressMonitor(monitor, 10);
 		sm.beginTask("Allocating Program memory..", cmanager.symbolManager.functions.size());
 		allocator = new MemoryAllocator(Collections.singletonList(memmodel.getProg()));
 		for(FunctionInstance fi : cmanager.symbolManager.functions){
 			fi.address = (int) allocator.allocate(fi.getLength());
 			sm.worked(1);
 		}
 		sm.done();
 		
 		/*
 		 * Building HexFile
 		 */
 		sm = new SubProgressMonitor(monitor, 30);
 		sm.beginTask("Constructing hexfile..", cmanager.symbolManager.functions.size());
 		HexFile hf = HexfileFactory.eINSTANCE.createHexFile();
 		Entry program = HexfileFactory.eINSTANCE.createEntry();
 		hf.getEntries().add(program);
 		hf.setAddressType(AddressType.EXTENDED_LINEAR);
 		program.setAddress((int) memmodel.getProg().getStartAddr());
 		long[] data = new long[0];
 		for(FunctionInstance fi : cmanager.symbolManager.functions){
 			long[] f = fi.calculateWords();
 			long[] olddata = data;
 			data = new long[olddata.length+f.length];
 			System.arraycopy(olddata, 0, data, 0, olddata.length);
 			System.arraycopy(f, 0, data, olddata.length, f.length);
 			sm.worked(1);
 		}
 		sm.done();
 		
 		/*
 		 * long array to byte array
 		 */
 		int wordbytes = cmanager.insManager.getWordSize()/8; //num of bytes in a word
 		byte[] bytes = new byte[data.length*wordbytes];
 		for(int i=0;i<data.length;i++){
 			long d = data[i];
 			for(int j=0;j<wordbytes;j++){
 				int b = (int)(d&0xFF);
 				d = d>>8;
 				//Reversed byte order (for now, it seems that 
 				// forward byte order should be used)
 				//int index = i*wordbytes + (wordbytes-1-j);
 				int index = i*wordbytes + j;
 				bytes[index] = HexFileResource.intToByte(b);
 			}
 		}
 		program.setData(bytes);
 		
 		for(IPostBuildProcess bp : postBuilds){
 			bp.execute(hf, new SubProgressMonitor(monitor, 10));
 		}
 		
 		lastSymbolManager = cmanager.symbolManager;
 		
 		monitor.done();
 		return hf;
 	}
 	
 	@Override
 	public void setMemoryModel(MemoryModel memmodel) {
 		this.memmodel = memmodel;
 	}
 
 	private SymbolManager lastSymbolManager = null;
 	
 	@Override
 	public Map<String, Long> getSymbolMapping() {
 		Map<String, Long> result = new TreeMap<String, Long>();
 		if (lastSymbolManager != null){
 			for(VariableInstance vi : lastSymbolManager.variables){
 				result.put(vi.getRootReference(), Long.valueOf(vi.address));
 			}
 			for(FunctionInstance fi : lastSymbolManager.functions){
 				result.put(fi.getRootReference(), Long.valueOf(fi.address));
 			}
 		}
 		return result;
 	}
 
 	@Override
 	public void addPostBuildProcess(IPostBuildProcess process) {
 		postBuilds.add(process);
 	}
 
 	@Override
 	public void setConfigurationHandler(IModuleConfigurationHandler handler) {
 		confighandler = handler;
 	}
 
 }
