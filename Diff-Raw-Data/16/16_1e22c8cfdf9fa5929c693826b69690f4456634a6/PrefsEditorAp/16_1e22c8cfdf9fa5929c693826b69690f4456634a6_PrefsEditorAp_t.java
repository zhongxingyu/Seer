 /**
  * 
  */
 package ecologylab.standalone;
 
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 
 
 //import cf.services.messages.CFServicesTranslations;
 import ecologylab.appframework.ApplicationEnvironment;
 import ecologylab.appframework.types.prefs.MetaPrefSet;
 import ecologylab.appframework.types.prefs.PrefSet;
 import ecologylab.appframework.types.prefs.PrefTranslations;
 import ecologylab.appframework.types.prefs.gui.PrefsEditor;
 import ecologylab.net.ParsedURL;
 import ecologylab.services.messages.DefaultServicesTranslations;
 import ecologylab.xml.ElementState;
 import ecologylab.xml.TranslationSpace;
 import ecologylab.xml.XmlTranslationException;
 
 /**
  * Standalone app to open a prefs editing dialog.
  * 
  * @author Cae
  *
  */
 public class CFPrefsEdit extends ApplicationEnvironment 
 {
 	
 	public CFPrefsEdit(String[] args) throws XmlTranslationException 
 	{
 		super("ecologyLabFundamental", PrefTranslations.get(), args);
 		
         // TODO method for this to get prefs dir
 		//ParsedURL metaPrefsPURL = this.preferencesDir().getRelative("metaprefs.xml", "");
         //ParsedURL prefsPURL = this.preferencesDir().getRelative("prefs.xml", "");
		PrefsEditor mgr = (PrefsEditor) this.createPrefsEditor();
 
 /*		try 
 		{
 //			println("Loading meta-preferences from: " + metaPrefsPURL);
 //			TranslationSpace prefTranslations = PrefTranslations.get();
 //			MetaPrefSet metaPrefSet	= MetaPrefSet.load(metaPrefsPURL, prefTranslations);
 //            println("Loading preferences from: " + prefsPURL);
 //            PrefSet prefSet = PrefSet.load(prefsPURL, prefTranslations);
 
 //            PrefsEditor mgr = new PrefsEditor(metaPrefSet(), prefSet(), prefsPURL, true);
             // could also call: JFrame jFrame = mgr.fetchJFrame();
 		}
 		catch (XmlTranslationException e)
 		{
 			error(metaPrefsPURL, "Caught exception while reading meta-preferences:");
 			e.printStackTrace();
 		}
  */		
 	}
 	/**
 	 * @param args
 	 * @throws XmlTranslationException 
 	 */
 	public static void main(String[] args) throws XmlTranslationException 
 	{
 		new CFPrefsEdit(args);
 	}
 
 }
