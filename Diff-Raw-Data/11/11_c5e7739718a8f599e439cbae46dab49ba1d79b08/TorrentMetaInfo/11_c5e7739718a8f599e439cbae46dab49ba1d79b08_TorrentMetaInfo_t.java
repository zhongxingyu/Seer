 package tracker.torrents;
 
 import tracker.Config;
 import tracker.bencode.Decoder;
 import tracker.bencode.Encoder;
 import tracker.util.FileUtils;
 import tracker.util.Utils;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.UnsupportedEncodingException;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 public class TorrentMetaInfo {
 	protected Map torrentMap;
 
 	protected byte[] infoHash = null;
 	protected Long totalSize = null;
 	protected List<FileMetaInfo> files = null;
 
 	public TorrentMetaInfo(File torrentMetaFile) throws Exception {
		BufferedInputStream bufferedInputStream = null;
		try {
			bufferedInputStream = new BufferedInputStream(new FileInputStream(torrentMetaFile));
			this.torrentMap = Decoder.get().decode(bufferedInputStream);
		} finally {
			if (bufferedInputStream != null) {
				bufferedInputStream.close();
			}
		}
 		this.parseData();
 	}
 
 	public TorrentMetaInfo(byte[] rawData) throws Exception {
 		this.torrentMap = Decoder.get().decode(rawData);
 		this.parseData();
 	}
 
 	public TorrentMetaInfo(Map torrentMap) throws Exception {
 		this.torrentMap = torrentMap;
 		this.parseData();
 	}
 
 	@SuppressWarnings("unchecked")
 	protected void parseData() throws Exception {
 		if (!torrentMap.containsKey("info")) {
 			throw new Exception("Info section is not defined.");
 		}
 
 		this.infoHash = Utils.hashSha1(Encoder.get().encode(torrentMap.get("info")).toByteArray());
 
 		Map<String, Object> infoSection = (Map<String, Object>)torrentMap.get("info");
 
 		this.files = new ArrayList<FileMetaInfo>();
 		if(infoSection.containsKey("files")) {
 			this.totalSize = 0l;
 			List<Map<String, Object>> files = (List<Map<String, Object>>)infoSection.get("files");
 			for (Map<String, Object> file : files) {
 				String path = "";
 				List<Object> breadcrumbs = (ArrayList<Object>)file.get("path");
 				for (Object breadcrumb : breadcrumbs) {
 					path += "/" + new String((byte[])breadcrumb, Charset.forName(Config.getString("tracker.charset")));
 				}
 
 				FileMetaInfo fileMetaInfo = new FileMetaInfo(path, (Long)file.get("length"));
 				this.files.add(fileMetaInfo);
 
 				this.totalSize += fileMetaInfo.getSize();
 			}
 		} else if (infoSection.containsKey("length")) {
 			String fileName = new String((byte[])infoSection.get("name"), Charset.forName(Config.getString("tracker.charset")));
 
 			FileMetaInfo fileMetaInfo = new FileMetaInfo(fileName, (Long)infoSection.get("length"));
 			this.files.add(fileMetaInfo);
 
 			this.totalSize = fileMetaInfo.getSize();
 		}
 	}
 
 	public Map getTorrentMap() {
 		return this.torrentMap;
 	}
 
 	public byte[] getInfoHash() {
 		return this.infoHash;
 	}
 
 	public String getInfoHashHexString() throws UnsupportedEncodingException {
 		return Utils.getHexString(this.getInfoHash());
 	}
 
 	public Long getSize() {
 		return this.totalSize;
 	}
 
 	public List<FileMetaInfo> getFiles() {
 		return this.files;
 	}
 
 	public class FileMetaInfo {
 		protected String path;
 		protected Long size;
 
 		public FileMetaInfo(String path, Long size) {
 			this.path = path;
 			this.size = size;
 		}
 
 		public String getPath() {
 			return this.path;
 		}
 
 		public Long getSize() {
 			return this.size;
 		}
 
 		public String getFormattedSize() {
 			return FileUtils.getSizeString(this.getSize());
 		}
 	}
 }
