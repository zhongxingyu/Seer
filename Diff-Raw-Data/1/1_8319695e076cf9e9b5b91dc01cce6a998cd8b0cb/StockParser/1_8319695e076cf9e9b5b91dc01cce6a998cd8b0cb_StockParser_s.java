 package de.hotware.hotsound.audio.playlist;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 import de.hotware.hotsound.audio.player.BaseSong;
 import de.hotware.hotsound.audio.player.ISong;
 
 public enum StockParser implements IPlaylistParser {
 	M3U("m3u") {
 
 		@Override
 		public List<ISong> parse(URL pURL) throws IOException {
 			try(BufferedReader buf = new BufferedReader(new InputStreamReader(pURL
 					.openStream()))) {
 				List<ISong> ret = new ArrayList<ISong>();
 				String line;
 				while((line = buf.readLine()) != null) {
 					//ignore ALL the unnecessary whitespace
 					line.trim();
 					if(!line.startsWith("#")) {
 						if(!line.startsWith("http")) {
 							File file = new File(line);
 							if(file.exists()) {
 								//file path was absolute
 								ret.add(new BaseSong(file.toURI().toURL()));
 							} else if(pURL.getProtocol().startsWith("http")) {
 								//file path was relative
 								File parentFile = new File(pURL.getFile())
 										.getParentFile();
 								if(!parentFile.isDirectory()) {
 									throw new AssertionError("parent file of url is no directory!");
 								}
 								file = new File(parentFile, line);
 								if(file.exists()) {
 									//file path was relative and file exists
 									ret.add(new BaseSong(file.toURI().toURL()));
 								}
 							}
 						} else {
 							ret.add(new BaseSong(new URL(line)));
 						}
 					}
 				}
 				return ret;
 			}
 		}
 
 	};
 
 	protected String[] mKeys;
 
 	private StockParser(String... pKeys) {
 		if(pKeys == null) {
 			throw new IllegalArgumentException("pKeys may not be null");
 		}
 		this.mKeys = pKeys;
 	}
 
 	@Override
 	public String[] getKeys() {
 		return this.mKeys;
 	}
 
 }
