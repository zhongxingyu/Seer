 package org.uncertweb.ps.processes;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
import java.util.regex.Pattern;
 
 import org.apache.log4j.Logger;
 import org.uncertweb.et.emulator.Emulator;
 import org.uncertweb.ps.data.DataDescription;
 import org.uncertweb.ps.data.Metadata;
 import org.uncertweb.ps.data.ProcessInputs;
 import org.uncertweb.ps.data.ProcessOutputs;
 import org.uncertweb.ps.data.SingleOutput;
 import org.uncertweb.ps.process.AbstractProcess;
 import org.uncertweb.ps.process.ProcessException;
 import org.uncertweb.ps.process.ProcessRepository;
 
 public class UploadEmulator extends AbstractProcess {
 	
 	private final Logger logger = Logger.getLogger(UploadEmulator.class);
 
 	@Override
 	public DataDescription getInputDataDescription(String arg0) {
 		if (arg0.equals("Identifier")) {
 			return new DataDescription(String.class);
 		}
 		else {
 			return new DataDescription(Emulator.class);
 		}
 	}
 
 	@Override
 	public List<String> getInputIdentifiers() {
 		return Arrays.asList(new String[] { "Identifier", "Emulator" });
 	}
 
 	@Override
 	public List<Metadata> getInputMetadata(String arg0) {
 		List<Metadata> metadata = new ArrayList<Metadata>();
 		if (arg0.equals("Identifier")) {
 			metadata.add(new Metadata("description", "The desired service identifier for the emulator."));
 		}
 		else if (arg0.equals("Expression")) {
 			metadata.add(new Metadata("description", "The emulator to upload."));
 		}
 		return metadata;
 	}
 
 	@Override
 	public DataDescription getOutputDataDescription(String arg0) {
 		return new DataDescription(String.class);
 	}
 
 	@Override
 	public List<String> getOutputIdentifiers() {
 		return Arrays.asList(new String[] { "Status" });
 	}
 
 	@Override
 	public List<Metadata> getOutputMetadata(String arg0) {
 		List<Metadata> metadata = new ArrayList<Metadata>();
 		if (arg0.equals("Status")) {
 			metadata.add(new Metadata("description", "The upload status."));
 		}
 		return metadata;
 	}
 
 	@Override
 	public ProcessOutputs run(ProcessInputs arg0) throws ProcessException {
 		// get inputs
 		String identifier = arg0.get("Identifier").getAsSingleInput().getObjectAs(String.class);
 		Emulator emulator = arg0.get("Emulator").getAsSingleInput().getObjectAs(Emulator.class);
 		
 		// TODO: make sure the identifier is valid for deployment
		Pattern pattern = Pattern.compile("[a-zA-Z][a-zA-Z0-9]*");
		if (!pattern.matcher(identifier).matches()) {
			throw new ProcessException("Invalid identifier: it should start with a letter and only contain alphanumeric characters.");
		}
		
 		// no emulators that have more than one output
 		
 		// add		
 		try {
 			logger.info("Adding '" + identifier + "' emulator to repository.");
 			ProcessRepository.getInstance().addEmulatorProcess(identifier, emulator);
 		}
 		catch (Exception e) {
 			throw new ProcessException(e.getMessage());
 		}
 		
 		// return
 		ProcessOutputs outputs = new ProcessOutputs();
 		outputs.add(new SingleOutput("Status", "'" + identifier + "' emulator uploaded successfully."));
 		return outputs;
 	}
 
 	@Override
 	public List<Metadata> getMetadata() {
 		List<Metadata> metadata = new ArrayList<Metadata>();
 		metadata.add(new Metadata("description", "Upload an emulator to be exposed as a process on this service."));
 		return metadata;
 	}
 
 }
