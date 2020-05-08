 package uk.co.unclealex.music.command;
 import java.io.File;
 import java.io.IOException;
import java.util.Arrays;
 import java.util.List;
 import java.util.TreeSet;
 
 import uk.co.flamingpenguin.jewel.JewelException;
 import uk.co.flamingpenguin.jewel.cli.CommandLineInterface;
 import uk.co.flamingpenguin.jewel.cli.Option;
 import uk.co.flamingpenguin.jewel.cli.Unparsed;
 import uk.co.unclealex.executable.CommandLine;
 import uk.co.unclealex.music.encoding.RenamingService;
 
 public class RenameCommand extends AbstractRenamingCommand<RenameCommandLine> {
 
	public static void main(String[] args) {
		new RenameCommand().execute(Arrays.asList(args));
	}
	
 	@Override
 	protected void validateCommandLine(RenameCommandLine commandLine) throws JewelException {
 		if (!(commandLine.isArtist()|| commandLine.isAlbum() || commandLine.isTrackNumber() || commandLine.isTitle() || commandLine.isCompilation() || commandLine.isNotCompilation())) {
 			throw new JewelException("You must supply at least one property to change.");
 		}
 		if (commandLine.isCompilation() && commandLine.isNotCompilation()) {
 			throw new JewelException("You cannot specify tracks to be both a compilation and not a compilation.");
 		}
 		if ((commandLine.isTrackNumber() || commandLine.isTitle()) && commandLine.getFlacFiles().size() != 1) {
 			throw new JewelException("If a track number or title is specified you may only change one flac file.");
 		}
 	}
 	
 	@Override
 	public void run(RenamingService renamingService, RenameCommandLine commandLine) throws IOException {
 		Integer trackNumber = commandLine.isTrackNumber()?commandLine.getTrackNumber():null;
		Boolean compilationResult = commandLine.isCompilation()?Boolean.TRUE:(commandLine.isNotCompilation()?Boolean.FALSE:null);
		String artist = commandLine.isArtist()?commandLine.getArtist():null;
		String album = commandLine.isAlbum()?commandLine.getAlbum():null;
		String title = commandLine.isTitle()?commandLine.getTitle():null;
		TreeSet<File> flacFiles = new TreeSet<File>(commandLine.getFlacFiles());
		renamingService.rename(flacFiles, artist, album, compilationResult, trackNumber, title);
 	}
 }
 
 @CommandLineInterface(application="flac-rename")
 interface RenameCommandLine extends CommandLine {
 
	@Option(shortName="a", description = "The new artist name.")
 	public String getArtist();
 	public boolean isArtist();
 
	@Option(shortName="l", description = "The new album name.")
 	public String getAlbum();
 	public boolean isAlbum();
 	
	@Option(shortName="n", description = "The new track number.")
 	public int getTrackNumber();
 	public boolean isTrackNumber();
 	
	@Option(shortName="t", description = "The new track title.")
 	public String getTitle();
 	public boolean isTitle();
 	
 	@Option(shortName="c", description = "Indicate that theses files are part of a compilation.")
 	public boolean isCompilation();
 
	@Option(shortName="d", description = "Indicate that theses files are not part of a compilation.")
 	public boolean isNotCompilation();
 
 	@Unparsed(name="The files to rename.")
 	public List<File> getFlacFiles();
 
 }
