 package fr.istic.synthlab;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import fr.istic.synthlab.abstraction.module.IModule;
 import fr.istic.synthlab.abstraction.module.out.IModuleOUT;
 import fr.istic.synthlab.abstraction.synthesizer.ISynthesizer;
 import fr.istic.synthlab.command.ICommand;
 import fr.istic.synthlab.controller.synthesizer.ICSynthesizer;
 import fr.istic.synthlab.factory.impl.PACFactory;
 import fr.istic.synthlab.util.ReadXMLFile;
 import fr.istic.synthlab.util.WriteXMLFile;
 
 //github.com/m2gl-synthlab/synthlab.git/
 
 /**
  * Application
  */
 public class SynthApp implements ISynthApp {
 
 	private List<ICSynthesizer> synth;
 	private ICSynthesizer currentSynth;
 	private ISynthFrame frame;
 	private ICommand displayCmd;
 	private ICommand undisplayCmd;
 	private String[] currentFile = { null, null };
 	private int untitledIndex = 1;
 
 	public SynthApp(ISynthFrame frame){
 		this.frame = frame;
 		synth = new ArrayList<ICSynthesizer>();
 		currentFile[0]=null;
 		currentFile[1]=null;
 		newSynthInstance();
 	}
 	
 	@Override
 	public void newSynth() {
 		newSynthInstance();
 		displayNewSynth();
		startSynth();
 	}
 
 	@Override
 	public void startSynth() {
 		currentSynth.start();
 	}
 
 	private void newSynthInstance() {
 		if(synth.size()>0)
 			currentSynth.stop();
 		this.synth.add((ICSynthesizer) (PACFactory.getFactory()).newSynthesizer());
 		currentSynth = synth.get(synth.size()-1);
 		currentSynth.setPath("untitled"+untitledIndex);
 		currentSynth.setFrame(frame);
 		untitledIndex++;
 	}
 
 	@Override
 	public void quitSynth() {
 		this.currentSynth = null;
 		undisplayCmd.execute();
 		System.exit(0);
 	}
 
 	@Override
 	public void saveToXML(String fileDir, String filename) {
 		List<IModule> modules = currentSynth.getModules();
 
 		WriteXMLFile writeToXML = new WriteXMLFile(new File(fileDir + filename));
 		writeToXML.saveModules(modules);
 
 		currentFile[0] = fileDir;
 		currentFile[1] = filename;
 
 		frame.removeFromMenu(currentSynth);
 		
 		currentSynth.setPath(fileDir + filename);
 		frame.addToMenu(currentSynth);
 		
 	}
 
 	@Override
 	public void loadFromXML(String dir, String file) {
 		currentSynth.stop();
 		newSynthInstance();
//		frame.setSynthesizer(currentSynth);
 		displayCmd.execute();
 		
 		ReadXMLFile readXML = new ReadXMLFile(currentSynth, new File(dir+file));
 		readXML.loadSynthesizer();
 
 		currentFile[0] = dir;
 		currentFile[1] = file;
 
		currentSynth.start();
 		currentSynth.setPath(dir+file);
 		frame.addToMenu(currentSynth);
 	}
 
 	@Override
 	public void setSynthesizer(String synthS) {
 		currentSynth.stop();
 		for(ISynthesizer synth : this.synth){
 			if(((ICSynthesizer)synth).getPath().equals(synthS)){
 				currentSynth = (ICSynthesizer) synth;
 				return;
 			}
 		}
 	}
 
 	@Override
 	public ICSynthesizer getSynthesizer() {
 		if(currentSynth==null)
 			newSynthInstance();
 		return currentSynth;
 	}
 
 	@Override
 	public void setDisplaySynthCommand(ICommand displaySynthCommand) {
 		this.displayCmd = displaySynthCommand;
 	}
 
 	@Override
 	public void setUndisplaySynthCommand(ICommand undisplaySynthCommand) {
 		this.undisplayCmd = undisplaySynthCommand;
 	}
 
 	@Override
 	public String[] getCurrentFile() {
 		return currentFile;
 	}
 
 	public void displayNewSynth() {
 		displayCmd.execute();
 
 		// Add an OUT module
 		IModuleOUT out = (PACFactory.getFactory()).newOUT(currentSynth);
 		currentSynth.add(out);
		
 	}
 
 	public void setFrame(SynthFrame frame2) {
 		this.frame = frame2;
 	}
 }
