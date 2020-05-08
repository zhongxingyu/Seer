 package org.dawnsci.persistence.internal;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import ncsa.hdf.object.h5.H5ScalarDS;
 
 import org.dawb.common.services.ServiceManager;
 import org.eclipse.dawnsci.hdf5.IHierarchicalDataFile;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.fasterxml.jackson.core.JsonProcessingException;
 import com.fasterxml.jackson.databind.ObjectMapper;
 
 import uk.ac.diamond.scisoft.analysis.processing.IOperation;
 import uk.ac.diamond.scisoft.analysis.processing.IOperationService;
 import uk.ac.diamond.scisoft.analysis.processing.OperationData;
 import uk.ac.diamond.scisoft.analysis.processing.model.IOperationModel;
 
 public class PersistJsonOperationHelper {
 
 	ObjectMapper mapper;
 	IOperationService service;
 	
 	private final static Logger logger = LoggerFactory.getLogger(PersistJsonOperationHelper.class);
 	
 	public IOperation<? extends IOperationModel, ? extends OperationData>[] readOperations(IHierarchicalDataFile file) throws Exception{
 		
 		List<IOperation> opList = new ArrayList<IOperation>();
 
 		String group = file.group(PersistenceConstants.PROCESS_ENTRY);
 		List<String> memberList = file.memberList(group);
 
 		int i = 0;
		String[] name = new String[]{PersistenceConstants.PROCESS_ENTRY+ Integer.toString(i)};
 
 		while (memberList.contains(name[0])){
 			Object data = file.getData(name[0] +"/data");
 			Object id = file.getData(name[0] +"/id");
 			String json = ((String[])((H5ScalarDS)data).getData())[0];
 			String sid = ((String[])((H5ScalarDS)id).getData())[0];
 
 			if (service == null) service = (IOperationService)ServiceManager.getService(IOperationService.class);
 
 			IOperation op = service.create(sid);
 			Class<? extends IOperationModel> modelClass = service.getModelClass(sid);
 			if (mapper == null) mapper = new ObjectMapper();
 			IOperationModel readValue = mapper.readValue(json, modelClass);
 			op.setModel(readValue);
 			opList.add(op);
 
 			name[0] = "/entry/process/"+ Integer.toString(++i);
 
 		}
 
 		return opList.isEmpty() ? null : opList.toArray(new IOperation[opList.size()]);
 	}
 	
 	public void writeOperations(IHierarchicalDataFile file, IOperation<? extends IOperationModel, ? extends OperationData>[] operations) throws Exception {
 			
 		String process = file.group(PersistenceConstants.PROCESS_ENTRY);
 		file.setNexusAttribute(process, "NXprocess");
 
 		for (int i = 0; i < operations.length; i++) {
 			writeOperationToProcess(file, process, i, operations[i]);
 		}
 		
 	}
 	
 	private void writeOperationToProcess(IHierarchicalDataFile file,String group, int i, IOperation<? extends IOperationModel, ? extends OperationData> op) throws Exception {
 		
 		String opId = op.getId();
 		String name = op.getName();
 		String modelJson = getModelJson(op.getModel());
 		
 		String note = file.group(Integer.toString(i), group);
 		file.setNexusAttribute(note, "NXnote");
 		file.createStringDataset("name", name, note);
 		file.createStringDataset("id", opId, note);
 		file.createStringDataset("data", modelJson, note);
 		
 	}
 	
 	public String getModelJson(IOperationModel model) throws Exception {
 		if (mapper == null ) mapper = new ObjectMapper();
 		return mapper.writeValueAsString(model);
 	}
 }
