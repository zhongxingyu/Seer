 package net.geekgrandad.plugin;
 
 import java.io.IOException;
 
 import net.geekgrandad.interfaces.MediaControl;
 import net.geekgrandad.interfaces.Provider;
 import net.geekgrandad.interfaces.Reporter;
 
 import com.dt.iTunesController.ITLibraryPlaylist;
 import com.dt.iTunesController.ITPlayerState;
 import com.dt.iTunesController.ITPlaylist;
 import com.dt.iTunesController.ITPlaylistCollection;
 import com.dt.iTunesController.ITPlaylistSearchField;
 import com.dt.iTunesController.ITSource;
 import com.dt.iTunesController.ITSourceCollection;
 import com.dt.iTunesController.ITTrack;
 import com.dt.iTunesController.ITTrackCollection;
 import com.dt.iTunesController.ITUserPlaylist;
 import com.dt.iTunesController.iTunes;
 
 public class ITunesControl implements MediaControl {
 	private iTunes itc = new iTunes();
 	private Reporter reporter;
 	
 	public static void main(String[] args) {
 		iTunes itc = new iTunes();
 		ITTrack itt = itc.getCurrentTrack();
 		System.out.println("Currently playing:");
 		System.out.println("Name:    " + itt.getName());
 		System.out.println("By:      " + itt.getArtist());
 		System.out.println("Album:   " + itt.getAlbum());
 	}
 
 	@Override
 	public void setProvider(Provider provider) {
 		this.reporter = provider.getReporter();		
 	}
 
 	@Override
 	public void start(int id, String playlist, boolean repeat)
 			throws IOException {
 		ITSource source = itc.getLibrarySource();
 		ITPlaylistCollection lists = source.getPlaylists();
 		ITPlaylist list = lists.ItemByName(playlist);
 		list.playFirstTrack();
 	}
 
 	@Override
 	public void open(int id, String file, boolean repeat) throws IOException {;
 		
 		ITSource source = itc.getLibrarySource();
 		ITPlaylistCollection lists = source.getPlaylists();
 		ITPlaylist temp = lists.ItemByName("temp");
 		if (temp != null) temp.delete();
 		
 		ITLibraryPlaylist all = itc.getLibraryPlaylist();
 		
 		reporter.print("Library contains " + all.getTracks().getCount() + " tracks");
 		ITTrackCollection tracks = all.search(file, ITPlaylistSearchField.ITPlaylistSearchFieldAll);
 		reporter.print("Search returned " + ((int) tracks.getCount()) + " tracks");
 		ITUserPlaylist pl = (ITUserPlaylist) itc.createPlaylist("temp");
 		for(int i=1; i <= tracks.getCount(); i++) {
 			ITTrack track = tracks.getItem(i);
 			pl.addTrack(track);
 		}
 		pl.playFirstTrack();
 	}
 
 	@Override
 	public void type(int id, String s) throws IOException {
 		reporter.error("ITunes: type not supported");
 		
 	}
 
 	@Override
 	public void select(int id, String service) throws IOException {
 		reporter.error("ITunes: select not supported");	
 	}
 
 	@Override
 	public void pause(int id) throws IOException {
 		itc.pause();	
 	}
 
 	@Override
 	public void stop(int id) throws IOException {
 		itc.playPause();	
 	}
 
 	@Override
 	public void play(int id) throws IOException {
 		itc.play();		
 	}
 
 	@Override
 	public void record(int id) throws IOException {
 		reporter.error("ITunes: record not supported");	
 	}
 
 	@Override
 	public void ff(int id) throws IOException {
 		itc.fastForward();	
 	}
 
 	@Override
 	public void fb(int id) throws IOException {
 		reporter.error("ITunes: fb not supported");	
 	}
 
 	@Override
 	public void skip(int id) throws IOException {
 		itc.nextTrack();		
 	}
 
 	@Override
 	public void skipb(int id) throws IOException {
 		itc.previousTrack();	
 	}
 
 	@Override
 	public void slow(int id) throws IOException {
 		reporter.error("iTunes: slow not supported");	
 	}
 
 	@Override
 	public void delete(int id) throws IOException {
 		reporter.error("iTunes: delete not supported");	
 	}
 
 	@Override
 	public void up(int id) throws IOException {
 		reporter.error("iTunes: up not supported");
 	}
 
 	@Override
 	public void down(int id) throws IOException {
 		reporter.error("iTunes: down not supported");		
 	}
 
 	@Override
 	public void left(int id) throws IOException {
 		reporter.error("iTunes: left not supported");	
 	}
 
 	@Override
 	public void right(int id) throws IOException {
 		reporter.error("iTunes: right not supported");
 	}
 
 	@Override
 	public void ok(int id) throws IOException {
 		reporter.error("iTunes: ok not supported");		
 	}
 
 	@Override
 	public void back(int id) throws IOException {
 		reporter.error("iTunes: back not supported");	
 	}
 
 	@Override
 	public void lastChannel(int id) throws IOException {
 		reporter.error("iTunes: lastChannel not supported");	
 	}
 
 	@Override
 	public void option(int id, String option) throws IOException {
 		reporter.error("ITunes: option not supported");
 	}
 
 	@Override
 	public void volumeUp(int id) throws IOException {
 		int vol = itc.getSoundVolume();
 		itc.setSoundVolume(vol+1);	
 	}
 
 	@Override
 	public void volumeDown(int id) throws IOException {
 		int vol = itc.getSoundVolume();
 		itc.setSoundVolume(vol-1);		
 	}
 
 	@Override
 	public void mute(int id) throws IOException {
 		itc.setMute(true);
 	}
 
 	@Override
 	public int getVolume(int id) throws IOException {
 		return itc.getSoundVolume();
 	}
 
 	@Override
 	public void setVolume(int id, int volume) throws IOException {
 		itc.setSoundVolume(volume);		
 	}
 
 	@Override
 	public void setChannel(int id, int channel) throws IOException {
 		reporter.error("iTunes: channels not supported");		
 	}
 
 	@Override
 	public String getTrack(int id) throws IOException {
 		ITTrack itt = itc.getCurrentTrack();
 		return itt.getName();
 	}
 
 	@Override
 	public int getChannel(int id) throws IOException {
 		reporter.error("iTunes: channels not supported");
 		return 0;
 	}
 
 	@Override
 	public void channelUp(int id) throws IOException {
 		reporter.error("iTunes: channels not supported");
 		
 	}
 
 	@Override
 	public void channelDown(int id) throws IOException {
 		reporter.error("iTunes: channels not supported");	
 	}
 
 	@Override
 	public void thumbsUp(int id) throws IOException {
 		reporter.error("iTunes: ratings not supported");
 	}
 
 	@Override
 	public void thumbsDown(int id) throws IOException {
 		reporter.error("iTunes: ratings not supported");		
 	}
 
 	@Override
 	public void digit(int id, int n) throws IOException {
 		reporter.error("iTunes: digit not supported");	
 	}
 
 	@Override
 	public void color(int id, int n) throws IOException {
 		reporter.error("iTunes: color not supported");	
 	}
 
 	@Override
 	public void turnOn(int id) throws IOException {
 		reporter.error("iTunes: turnOn not supported");		
 	}
 
 	@Override
 	public void turnOff(int id) throws IOException {
 		itc.quit();	
 	}
 
 	@Override
 	public void pin(int id) throws IOException {
 		reporter.error("iTunes: pin not supported");
 	}
 
 	@Override
 	public void setSource(int id, int source) throws IOException {
 		reporter.error("iTunes: setSource not supported");		
 	}
 
 	@Override
 	public String getArtist(int id) throws IOException {
 		ITTrack itt = itc.getCurrentTrack();
 		return itt.getArtist();
 	}
 
 	@Override
 	public String getAlbum(int id) throws IOException {
 		ITTrack itt = itc.getCurrentTrack();
 		return itt.getAlbum();
 	}
 
 	@Override
 	public String getPlaylist(int id) throws IOException {
 		ITPlaylist p = itc.getCurrentPlaylist();	
 		return p.getName();
 	}
 
 	@Override
 	public void say(int id, String text) throws IOException {
 		reporter.error("ITunes: say not supported");	
 	}
 
 	@Override
 	public void pageUp(int id) throws IOException {
 		reporter.error("ITunes: pageUp not supported");		
 	}
 
 	@Override
 	public void pageDown(int id) throws IOException {
 		reporter.error("ITunes: pageDown not supported");	
 	}
 
 	@Override
 	public boolean isPlaying(int id) throws IOException {
 		return (itc.getPlayerState()  == ITPlayerState.ITPlayerStatePlaying);
 	}
 
 	@Override
 	public void reboot(int id) throws IOException {
 		reporter.error("ITunes: reboot not supported");		
 	}
 
 	@Override
 	public void setPlayer(int id, int playerId) throws IOException {
 		reporter.error("ITunes: pageDown not supported");
 	}
 
 	@Override
 	public void setRepeat(int id, boolean repeat) throws IOException {
 		reporter.error("ITunes: setRepeat not supported");	
 	}
 
 	@Override
 	public void setShuffle(int id, boolean shuffle) throws IOException {
 		reporter.error("iTunes: setShuffle not supported");	
 	}
 }
