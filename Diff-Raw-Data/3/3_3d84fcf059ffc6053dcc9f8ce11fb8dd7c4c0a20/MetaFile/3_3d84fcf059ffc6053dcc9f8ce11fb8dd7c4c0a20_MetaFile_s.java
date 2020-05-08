 package net.hetimatan.net.torrent.util.metafile;
 
 import java.io.File;
 import java.io.IOException;
 import java.security.DigestException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.LinkedList;
 
 import net.hetimatan.io.file.MarkableFileReader;
 import net.hetimatan.io.filen.RACashFile;
 import net.hetimatan.net.torrent.util.bencode.BenDiction;
 import net.hetimatan.net.torrent.util.bencode.BenList;
 import net.hetimatan.net.torrent.util.bencode.BenObject;
 import net.hetimatan.net.torrent.util.bencode.BenString;
 import net.hetimatan.util.io.ByteArrayBuilder;
 import net.hetimatan.util.url.PercentEncoder;
 
 
 public class MetaFile {
 
 	// Single file and Multiple file
 	public static final String TYPE_ANNOUNCE = "announce";
 	public static final String TYPE_INFO = "info";
 	public static final String TYPE_NAME = "name";
 	public static final String TYPE_PIECE_LENGTH = "piece length";
 	public static final String TYPE_PIECES = "pieces";
 	public static final String TYPE_CREATION_DATE = "creation date";
 
 	// Multiple file
 	public static final String TYPE_FILES = "files";
 	public static final String TYPE_PATH = "path";
 	public static final String TYPE_LENGTH = "length";
 
 	//
 	public static final int DEFAULT_PIECE_LENGTH_CLASSIC = 1024 * 256;
 	public static final int DEFAULT_PIECE_LENGTH = 16 * 1024;
 
 	public static final int SHA1_LENGTH = 20;//60;
 	public static final int CASH_LENGTH_FOR_SHA1CALC = 1024 * 256;
 
 	private BenDiction mDiction = null;
 	private String mAnnounce = null;
 	private String mName = null;
 	private BenString mPieces = null;
 	private long mPieceLength = 0;
 	private String[] mFiles = null;
 	private Long[] mFileLengts = null;
 
 	public MetaFile(BenDiction diction, String _announce, String _name,
 			BenString pieces, long _pieceLength, String[] _files, Long[] _fileLengths) {
 		mDiction = diction;
 		mAnnounce = _announce;
 		mName = _name;
 		mPieces = pieces;
 		mPieceLength = _pieceLength;
 		mFiles = new String[_files.length];
 		mFileLengts = new Long[_fileLengths.length];
 		System.arraycopy(_files, 0, mFiles, 0, mFiles.length);
 		System.arraycopy(_fileLengths, 0, mFileLengts, 0, mFileLengts.length);
 	}
 
 	public String getAnnounce() {
 		return mAnnounce;}
 
 	public String getName() {
 		return mName;}
 
 	public BenString getPieces() {
 		return mPieces;}
 
 	public long getPieceLength() {
 		return mPieceLength;}
 
 	public BenDiction getDiction() {
 		return mDiction;}
 
 	public BenDiction getInfo() throws IOException {
 		return (BenDiction) mDiction
 				.getBenValue(TYPE_INFO, BenObject.TYPE_DICT);}
 
 	public BenString getInfoSha1AsBenString() throws IOException {
 		BenDiction info = getInfo();
 		RACashFile output = new RACashFile(1024, 2);
 		info.encode(output.getLastOutput());
 		output.seek(0);
 		MarkableFileReader reader = new MarkableFileReader(output, 512);
 		try {
 			BenString infoSha1 = createInfoSHA1(reader);
 			return infoSha1;
 		} finally {
 			reader.close();
 		}
 	}
 
 	public String getInfoSha1AsPercentString() throws IOException {
 		BenString infoSha1 = getInfoSha1AsBenString();
 		PercentEncoder encoder = new PercentEncoder();
 		return encoder.encode(infoSha1.toByte());
 	}
 
 	public String[] getFiles() {
 		return mFiles;
 	}
 
 	public Long[] getFileLengths() {
 		return mFileLengts;
 	}
 
 	public int numOfPiece() {
 		return getPieces().byteLength()/SHA1_LENGTH;
 	}
 
 	/**
 	 * write metainfo(torrentfile) content. you should close args(VirtualFile).
 	 * @param output
 	 * @throws IOException
 	 */
 	// TODOb kiyo seek output,\.seek(length)
 	public void save(RACashFile output) throws IOException {
 		mDiction.encode(output.getLastOutput());
 		output.syncWrite();
 	}
 
 
 
 	public static BenList _filePath(File dir, File path) {
 		BenList list = new BenList();
 		while(true) {
 			if(path == null || dir.equals(path)) {
 				break;
 			}
 //			System.out.println("FF="+path.getName());
 			list.push(new BenString(path.getName()));
 			path = path.getParentFile();
 		}		
 		return list;
 	}
 
 	public static LinkedList<File> findFile(File targetDir) {
 		LinkedList<File> ret = new LinkedList<File>();
 		LinkedList<File> tmp = new LinkedList<File>();
 		tmp.push(targetDir);
 		while (tmp.size() > 0) {
 			File currentDir = tmp.pop();
 			for (File f : currentDir.listFiles()) {
 				if (f.isDirectory()) {
 					tmp.push(f);
 				} else {
 					ret.push(f);
 				}
 			}
 		}
 
 		return ret;
 	}
 
 	// use create peerid
 	public static BenString createInfoSHA1(MarkableFileReader reader) throws IOException {
 		byte[] temp = new byte[CASH_LENGTH_FOR_SHA1CALC];
 		byte[] buffer = new byte[SHA1_LENGTH];
 		int bufferBegin = 0;
 		int digestLen = SHA1_LENGTH;
 		int offset = 0;
 		try {
 			MessageDigest md = MessageDigest.getInstance("SHA");
 			do {
 				int len = reader.read(temp);
 				if (len < 0) {
 					break;
 				}
				md.update(temp, offset, len);
 				offset += len;
 			} while (true);
 			digestLen = md.digest(buffer, bufferBegin, digestLen);
 			return new BenString(buffer, 0, digestLen, "utf8");
 		} catch (NoSuchAlgorithmException e) {
 			throw new IOException();
 		} catch (DigestException e) {
 			throw new IOException();
 		}
 	}
 
 	public static BenString createPieces(MarkableFileReader reader) throws IOException {
 		byte[] temp = new byte[DEFAULT_PIECE_LENGTH];
 		long filelength = reader.length();
 		int piecesLength = (int) (filelength / DEFAULT_PIECE_LENGTH);
 		piecesLength += ((filelength % DEFAULT_PIECE_LENGTH) == 0 ? 0 : 1);
 		byte[] buffer = new byte[piecesLength * SHA1_LENGTH];
 		int bufferBegin = 0;
 		try {
 			do {
 				int len = reader.read(temp);
 				if (len < 0) {
 					break;
 				}
 				MessageDigest md = MessageDigest.getInstance("SHA");
 				md.update(temp, 0, len);
 				int digestLen = SHA1_LENGTH;
 				if (digestLen > buffer.length - bufferBegin) {
 					digestLen = buffer.length - bufferBegin;
 				}
 
 				//  Length must be at least 20 byte
 				digestLen = md.digest(buffer, bufferBegin, digestLen);
 				if (digestLen > 0) {
 					bufferBegin += digestLen;
 				}
 			} while (true);
 			return new BenString(buffer, 0, bufferBegin, "utf8");
 		} catch (NoSuchAlgorithmException e) {
 			e.printStackTrace();
 			throw new IOException();
 		} catch (DigestException e) {
 			e.printStackTrace();
 			throw new IOException();
 		}
 	}
 
 	//
 	// benlist "files"
 	// bendiction
 	// beninteger "length"
 	// benlist "path"
 	// benstring
 	// bendiction
 	// beninteger "length"
 	// benlist "path"
 	// benstring
 	// ...
 	// ...
 	public static void extractFileList(String dir, LinkedList<String> buffer, LinkedList<Long> lengths, BenList list) {
 		int size = list.size();
 		ByteArrayBuilder _tmpFilePath = new ByteArrayBuilder();
 		for (int i = 0; i < size; i++) {
 			BenObject value = list.getBenValue(i);
 			if (value == null) {
 				continue;
 			}
 			if (value.getType() == BenObject.TYPE_DICT) {
 				// path
 				BenObject path = value.getBenValue(TYPE_PATH);
 				if (path != null && path.getType() == BenObject.TYPE_LIST) {
 					BenObject tmp = path.getBenValue(0);
 					if (tmp == null) {
 						continue;
 					}
 					{
 						_tmpFilePath.clear();
 						_tmpFilePath.append(dir.getBytes());
 						for (int j = 0; j < path.size(); j++) {
 							_tmpFilePath.append("/".getBytes());
 							_tmpFilePath.append(path.getBenValue(j).toByte());
 						}
 						buffer.add(new String(_tmpFilePath.getBuffer(), 0,
 								_tmpFilePath.length()));
 					}
 				}
 
 				// length
 				BenObject length = value.getBenValue(TYPE_LENGTH);
 				if (length != null && length.getType() == BenObject.TYPE_INTE) {
 					lengths.add((long) length.toInteger());
 				} else {
 					lengths.add((long) 0);
 				}
 			}
 		}
 	}
 
 }
