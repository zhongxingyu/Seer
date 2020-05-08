 /* Copyright 2009 Sean Hodges <seanhodges@bluebottle.com>
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package tesla.app.command.provider.app;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import tesla.app.command.Command;
 import tesla.app.command.helper.AmarokPlaylistHelper;
 import tesla.app.command.helper.DBusHelper;
 import tesla.app.command.helper.DCopHelper;
 import tesla.app.command.helper.MprisPlaylistHelper;
 import tesla.app.command.provider.IConfigProvider;
 
 public class AmarokConfig implements IConfigProvider {
 	
 	public String getCommand(String key) {
 		final String dbusDest = "org.kde.amarok";
 		final String dcopDest = "amarok";
 		
 		List<String> args = new ArrayList<String>();
 		String out = null;
 		if (key.equals(Command.PLAY) || key.equals(Command.PAUSE)) {
 			String dcopCommand = new DCopHelper().compileMethodCall(dcopDest, "player", "playPause");
 			String dbusCommand = new DBusHelper().compileMethodCall(dbusDest, "/Player", 
 				"org.freedesktop.MediaPlayer.Pause");
 			out = compileCompositeCommand(dcopCommand, dbusCommand);
 		}
 		else if (key.equals(Command.PREV)) {
 			String dcopCommand = new DCopHelper().compileMethodCall(dcopDest, "player", "prev");
 			String dbusCommand = new DBusHelper().compileMethodCall(dbusDest, "/Player", 
 				"org.freedesktop.MediaPlayer.Prev");
 			out = compileCompositeCommand(dcopCommand, dbusCommand);
 		}
 		else if (key.equals(Command.NEXT)) {
 			String dcopCommand = new DCopHelper().compileMethodCall(dcopDest, "player", "next");
 			String dbusCommand = new DBusHelper().compileMethodCall(dbusDest, "/Player", 
 				"org.freedesktop.MediaPlayer.Next");
 			out = compileCompositeCommand(dcopCommand, dbusCommand);
 		}
 		else if (key.equals(Command.VOL_CHANGE)) {
 			args.add("%i");
 			String dcopCommand = new DCopHelper().compileMethodCall(dcopDest, "player", "setVolume", args);
 			args.clear();
 			args.add(new DBusHelper().evaluateArg("%i"));
 			String dbusCommand = new DBusHelper().compileMethodCall(dbusDest, "/Player", 
 				"org.freedesktop.MediaPlayer.VolumeSet", args);
 			out = compileCompositeCommand(dcopCommand, dbusCommand);
 		}
 		else if (key.equals(Command.VOL_MUTE)) {
 			args.add("%i");
 			String dcopCommand = new DCopHelper().compileMethodCall(dcopDest, "player", "setVolume", args);
 			args.clear();
 			args.add(new DBusHelper().evaluateArg("0"));
 			String dbusCommand = new DBusHelper().compileMethodCall(dbusDest, "/Player", 
 				"org.freedesktop.MediaPlayer.VolumeSet", args);
 			out = compileCompositeCommand(dcopCommand, dbusCommand);
 		}
 		else if (key.equals(Command.VOL_CURRENT)) {
 			String dcopCommand = new DCopHelper().compileMethodCall(dcopDest, "player", "getVolume");
 			String dbusCommand = new DBusHelper().compileMethodCall(dbusDest, "/Player", 
 				"org.freedesktop.MediaPlayer.VolumeGet");
 			out = compileCompositeCommand(dcopCommand, dbusCommand);
 		}
 		else if (key.equals(Command.GET_MEDIA_INFO)) {
 			String dcopCommand = buildMediaInfoDCopMethodCallSet(dcopDest);
 			String dbusCommand = new DBusHelper().compileMethodCall(dbusDest, "/Player", 
 				"org.freedesktop.MediaPlayer.GetMetadata");
 			out = compileCompositeCommand(dcopCommand, dbusCommand);
 		}
 		else if (key.equals(Command.IS_PLAYING)) {
 			String dcopCommand = new DCopHelper().compileMethodCall(dcopDest, "player", "isPlaying");
 			String dbusCommand = new DBusHelper().compileMethodCall(dbusDest, "/Player", 
 				"org.freedesktop.MediaPlayer.GetStatus", false);
 			dbusCommand = "if [[ \"$(" + dbusCommand + " | sed -n '3p')\" == \"      int32 0\" ]]; then echo \"PLAYING\"; fi";
 			out = compileCompositeCommand(dcopCommand, dbusCommand);
 		}
 		else if (key.equals(Command.GET_MEDIA_POSITION)) {
 			String dcopCommand = new DCopHelper().compileMethodCall(dcopDest, "player", "trackCurrentTime");
 			String dbusCommand = new DBusHelper().compileMethodCall(dbusDest, "/Player", 
 				"org.freedesktop.MediaPlayer.PositionGet");
 			out = compileCompositeCommand(dcopCommand, dbusCommand);
 		}
 		else if (key.equals(Command.GET_MEDIA_LENGTH)) {
 			String dcopCommand = new DCopHelper().compileMethodCall(dcopDest, "player", "trackTotalTime");
 			String dbusCommand = new DBusHelper().compileMethodCall(dbusDest, "/Player", 
 				"org.freedesktop.MediaPlayer.GetMetadata", false) + " | grep mtime -A 1 | grep variant | sed -e \"s/[^0-9]*//\" | cut -d ' ' -f 2";
 			out = compileCompositeCommand(dcopCommand, dbusCommand);
 		}
 		else if (key.equals(Command.SET_MEDIA_POSITION)) {
 			args.add("%i");
 			String dcopCommand = new DCopHelper().compileMethodCall(dcopDest, "player", "seek", args);
 			args.clear();
 			args.add(new DBusHelper().evaluateArg("%i"));
 			String dbusCommand = new DBusHelper().compileMethodCall(dbusDest, "/Player", 
 				"org.freedesktop.MediaPlayer.PositionSet", args);
 			out = compileCompositeCommand(dcopCommand, dbusCommand);
 		}
 		else if (key.equals(Command.GET_PLAYLIST)) {
 			String dcopCommand = new DCopHelper().compileMethodCall(dcopDest, "playlist", "saveCurrentPlaylist", false);
 			dcopCommand = new AmarokPlaylistHelper().compileQuery(dcopCommand);
 			String getPlaylistLength = new DBusHelper().compileMethodCall(dbusDest, "/TrackList", 
 				"org.freedesktop.MediaPlayer.GetLength", false) + " | grep int32 | sed -e 's/   //' | cut -d ' ' -f 2";
 			String getEntryMetadata = new DBusHelper().compileMethodCall(dbusDest, "/TrackList", 
 				"org.freedesktop.MediaPlayer.GetMetadata", false);
 			String dbusCommand = new MprisPlaylistHelper().compileQuery(getPlaylistLength, getEntryMetadata);
 			out = compileCompositeCommand(dcopCommand, dbusCommand);
 		}
 		else if (key.equals(Command.GET_PLAYLIST_SELECTION)) {
 			String dcopCommand = new DCopHelper().compileMethodCall(dcopDest, "playlist", "getActiveIndex");
 			String dbusCommand = new DBusHelper().compileMethodCall(dbusDest, "/TrackList", "org.freedesktop.MediaPlayer.GetCurrentTrack");
 			out = compileCompositeCommand(dcopCommand, dbusCommand);
 		}
 		else if (key.equals(Command.SET_PLAYLIST_SELECTION)) {
 			args.add("%i");
 			String dcopCommand = new DCopHelper().compileMethodCall(dcopDest, "playlist", "playByIndex", args);
 			String dbusCommand = new MprisPlaylistHelper().compileRebuildPlaylistSetCommand(dbusDest, "/TrackList");
 			out = compileCompositeCommand(dcopCommand, dbusCommand);
 		}
 		return out;
 	}
 
 	public Map<String, String> getSettings(String key) {
 		Map<String, String> settings = new HashMap<String, String>();
 		if (key.equals(Command.VOL_CURRENT)) {
 			settings.put("MIN", "0.0");
 			settings.put("MAX", "100.0");
 		}
 		else if (key.equals(Command.GET_MEDIA_INFO)) {
 			settings.put("ENABLED", "true");
 		}
 		else if (key.equals(Command.IS_PLAYING)) {
 			settings.put("ENABLED", "true");
 		}
 		else if (key.equals(Command.GET_MEDIA_POSITION)) {
 			settings.put("ENABLED", "true");
 		}
 		else if (key.equals(Command.GET_PLAYLIST)) {
 			settings.put("ENABLED", "true");
 		}
 		return settings;
 	}
 
 	private String compileCompositeCommand(String dcopCommand, String dbusCommand) {
 		StringBuilder builder = new StringBuilder();
 		
 		// Use DCOP if Amarok 1.x is present
 		builder.append("if [[ \"$(dcop --all-users amarok player version 2>/dev/null)\" != \"\" ]]; then ");
 		builder.append(dcopCommand);
 		
 		// Use DBUS otherwise
 		builder.append("; else ");
 		builder.append(dbusCommand);
 		
 		builder.append("; fi");
 		return builder.toString();
 	}
 
 	private String buildMediaInfoDCopMethodCallSet(String dcopDest) {
 		// DCOP has no concept of a hashmap, so we build one here
 		StringBuilder builder = new StringBuilder();
 		builder.append("echo \"[dcop]\";");
 		builder.append("echo -n \"tracknumber:\";");
 		builder.append(new DCopHelper().compileMethodCall(dcopDest, "player", "track", false));
 		builder.append(";echo -n \"title:\";");
 		builder.append(new DCopHelper().compileMethodCall(dcopDest, "player", "title", false));
 		builder.append(";echo -n \"artist:\";");
 		builder.append(new DCopHelper().compileMethodCall(dcopDest, "player", "artist", false));
 		builder.append(";echo -n \"album:\";");
 		builder.append(new DCopHelper().compileMethodCall(dcopDest, "player", "album", false));
 		return builder.toString();
 	}
 
 	public String getLaunchAppCommand() {
		return "pidof amarokapp 1>/dev/null || pidof amarok 1>/dev/null || DISPLAY=:0 amarok &>/dev/null & sleep 5 && echo success";
 	}
 }
