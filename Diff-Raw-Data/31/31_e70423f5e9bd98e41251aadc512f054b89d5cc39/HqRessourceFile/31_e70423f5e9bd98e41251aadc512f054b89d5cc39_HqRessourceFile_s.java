 package de.d2dev.heroquest.engine.files;
 
 import java.io.IOException;
 
 import nu.xom.ParsingException;
 
 import de.d2dev.fourseasons.VersionNumber;
 import de.d2dev.fourseasons.files.AbstractContainerFile;
 import de.d2dev.fourseasons.files.MagicStringException;
 
 import de.schlichtherle.truezip.file.TFile;
 
 public class HqRessourceFile extends AbstractContainerFile {
 
 	public static final String EXTENSION = "hqres";
 	public static final String MAGIC_STRING = "HEROQUEST_RESSOURCES";
 	
 	public static final VersionNumber VERSION = new VersionNumber(1, 0); 
 	
 	private static final String TEXTURES = "textures";
 	private static final String SOUNDS = "sounds";
 	
 	public static HqRessourceFile createHqRessourceFile(String path)  throws IOException, MagicStringException, ParsingException {
 		TFile file = AbstractContainerFile.createEmptyContainer( path, MAGIC_STRING, VERSION );
 		
 		// create texture folder
 		TFile textures = new TFile( file + "/" + TEXTURES );
 		textures.mkdir();
 		
 		// create sounds folder
 		TFile sounds = new TFile( file + "/" + SOUNDS );
 		sounds.mkdir();
 		
 		return new HqRessourceFile( path );
 	}
 	
 	public TFile textures;
 	public TFile sounds;
 	
 	public HqRessourceFile(String path) throws MagicStringException,
 			IOException, ParsingException {
 		super(path);
 		
 		// initialize resource folders
 		this.textures = new TFile( this.file.getAbsolutePath() + "/" + TEXTURES );
 		this.sounds = new TFile( this.file.getAbsolutePath() + "/" + SOUNDS );
 	}
 
 	@Override
 	public String getMagicString() {
 		return MAGIC_STRING;
 	}
 }
