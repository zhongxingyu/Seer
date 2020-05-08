 package fr.istic.synthlab;
 
 import java.awt.Image;
 import java.io.File;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 
 import com.alee.laf.WebLookAndFeel;
 
 import fr.istic.synthlab.command.app.DisplayCommand;
 import fr.istic.synthlab.command.app.UndisplayCommand;
 import fr.istic.synthlab.command.menu.AboutCommand;
 import fr.istic.synthlab.command.menu.AddModuleAudioScopeCommand;
 import fr.istic.synthlab.command.menu.AddModuleEGCommand;
 import fr.istic.synthlab.command.menu.AddModuleMIXCommand;
 import fr.istic.synthlab.command.menu.AddModuleOUTCommand;
 import fr.istic.synthlab.command.menu.AddModuleREPCommand;
 import fr.istic.synthlab.command.menu.AddModuleVCACommand;
 import fr.istic.synthlab.command.menu.AddModuleVCFHPCommand;
 import fr.istic.synthlab.command.menu.AddModuleVCFLPCommand;
 import fr.istic.synthlab.command.menu.AddModuleVCOCommand;
 import fr.istic.synthlab.command.menu.DocumentationCommand;
 import fr.istic.synthlab.command.menu.NewSynthCommand;
 import fr.istic.synthlab.command.menu.OpenSynthCommand;
 import fr.istic.synthlab.command.menu.QuitSynthCommand;
 import fr.istic.synthlab.command.menu.SaveAsSynthCommand;
 import fr.istic.synthlab.command.menu.SaveSynthCommand;
 import fr.istic.synthlab.command.toolbar.ToolbarCurrentWireColorCommand;
 import fr.istic.synthlab.command.toolbar.ToolbarPauseCommand;
 import fr.istic.synthlab.command.toolbar.ToolbarPlayCommand;
 import fr.istic.synthlab.factory.impl.AFactory;
 import fr.istic.synthlab.factory.impl.CFactory;
 import fr.istic.synthlab.factory.impl.PACFactory;
 import fr.istic.synthlab.factory.impl.PFactory;
 
 public class Synthlab {
 	public static void main(String[] args) {
 		// Set the look and feel
 		WebLookAndFeel.install();
 
 		// Set the icon application
 		try {
 			Image imageIcon = ImageIO.read(new File("res/logo.png"));
 			if (System.getProperty("os.name").contains("Mac")) {
 				com.apple.eawt.Application.getApplication().setDockIconImage(imageIcon);
 			}
 		} catch (IOException e2) {
 			e2.printStackTrace();
 		}
 
 		// Initialize factories
 		PACFactory.setAFactory(AFactory.getInstance());
 		PACFactory.setCFactory(CFactory.getInstance());
 		PACFactory.setPFactory(PFactory.getInstance());

 		
 		// Create the main frame
 		SynthFrame frame = null;
 	
 		// Create the application
 		SynthApp app = new SynthApp(frame);
 		frame = new SynthFrame(app);
 		try {
 			Image imageIcon = ImageIO.read(new File("res/logo.png"));
 			frame.setIconImage(imageIcon);
 		} catch (IOException e2) {
 			e2.printStackTrace();
 		}
 		
 		app.setFrame(frame);
 
 		// Configure the application
 		app.setDisplaySynthCommand(new DisplayCommand(frame));
 		app.setUndisplaySynthCommand(new UndisplayCommand(frame));
 
 		// Create a default synthesizer
 		app.displayNewSynth();
 
 		// Configure the frame
 		frame.setNewSynthCommand(new NewSynthCommand(app, frame));
 		frame.setOpenSynthCommand(new OpenSynthCommand(app, frame));
 		frame.setSaveSynthCommand(new SaveSynthCommand(app, frame));
 		frame.setSaveAsSynthCommand(new SaveAsSynthCommand(app, frame));
 		frame.setQuitSynthCommand(new QuitSynthCommand(app));
 		frame.setDocSynthCommand(new DocumentationCommand());
 		frame.setAboutSynthCommand(new AboutCommand());
 		frame.setToolbarPlayCommand(new ToolbarPlayCommand(app));
 		frame.setToolbarPauseCommand(new ToolbarPauseCommand(app));
 		frame.setCurrentWireColorCommand(new ToolbarCurrentWireColorCommand(app, frame));
 
 		frame.setAddModuleOUTCommand(new AddModuleOUTCommand(app));
 		frame.setAddModuleVCOCommand(new AddModuleVCOCommand(app));
 		frame.setAddModuleVCACommand(new AddModuleVCACommand(app));
 		frame.setAddModuleVCFLPCommand(new AddModuleVCFLPCommand(app));
 		frame.setAddModuleVCFHPCommand(new AddModuleVCFHPCommand(app));
 		frame.setAddModuleEGCommand(new AddModuleEGCommand(app));
 		frame.setAddModuleAudioScopeCommand(new AddModuleAudioScopeCommand(app));
 		frame.setAddModuleREPCommand(new AddModuleREPCommand(app));
 		frame.setAddModuleMIXCommand(new AddModuleMIXCommand(app));
 
 		// Start the application
 		app.startSynth();
 
 		frame.addToMenu(app.getSynthesizer());
 		app.getSynthesizer().setFrame(frame);
 
 		// Chargement du fichier ouvert
 		String filename = null;
 		if (args.length > 0) {
 			filename = args[0];
 			app.loadFromXML("", filename);
 		}
 
 	}
 }
