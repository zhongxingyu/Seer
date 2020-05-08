 package spaceshooters.save;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 import java.nio.ByteBuffer;
 
 import spaceshooters.exceptions.InvalidSaveDataSignatureException;
 import spaceshooters.exceptions.InvalidSaveDataVersionException;
 import spaceshooters.level.LevelType;
 import spaceshooters.main.Spaceshooters;
 import spaceshooters.util.MathHelper;
 
 public abstract class SaveData {
 	
 	public static final String SIGNATURE = "S2SF";
 	private static final int MAX_BYTES = 25 * 1024; // 25 KB
 	
 	private static SaveData instance;
 	private static boolean inited = false;
 	
 	protected static RandomAccessFile file;
 	protected static ByteBuffer data;
 	
 	public static SaveData init() {
 		if (!inited) {
 			try {
 				data = ByteBuffer.allocate(MAX_BYTES);
 				new File(Spaceshooters.getPath() + "saves/").mkdirs();
 				file = new RandomAccessFile(Spaceshooters.getPath() + "saves/save.ssf", "rw");
 				
 				if (file.length() <= 0) {
 					createEmptySaveFile();
 				}
 				
 				file = new RandomAccessFile(Spaceshooters.getPath() + "saves/save.ssf", "rw");
 				file.read(data.array());
 				
 				byte[] signBytes = new byte[4];
 				data.get(signBytes);
 				String signature = new String(signBytes);
 				
 				if (!SIGNATURE.equals(signature)) {
 					throw new InvalidSaveDataSignatureException(signature);
 				}
 				
 				instance = readVersion();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			
 			inited = true;
 		}
 		
 		return instance;
 	}
 	
 	public abstract int readHighscore(LevelType gamemode);
 	
 	public abstract void writeHighscore(LevelType gamemode, int score) throws IOException;
 	
 	public void save() {
 		try {
 			file.seek(0);
 			file.write(data.array());
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public static final SaveData reload() {
 		try {
 			instance = null;
 			
 			data = ByteBuffer.allocate(MAX_BYTES);
			file.readFully(data.array(), 0, data.array().length);
 			
 			byte[] signBytes = new byte[4];
 			data.get(signBytes);
 			String signature = new String(signBytes);
 			
 			if (!SIGNATURE.equals(signature)) {
 				throw new InvalidSaveDataSignatureException(signature);
 			}
 			
 			instance = readVersion();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		return instance;
 	}
 	
 	private static final SaveData readVersion() {
 		int version = MathHelper.readAsUnsignedByte(data.get(4));
 		switch (version) {
 		case SaveDataActual.VERSION:
 			return new SaveDataActual();
 		default:
 			throw new InvalidSaveDataVersionException(version);
 		}
 	}
 	
 	private static final void createEmptySaveFile() throws IOException {
 		file.seek(0);
 		file.writeBytes(SIGNATURE);
 		file.writeByte(SaveDataActual.VERSION);
 		file.writeInt(0);
 		file.writeInt(0);
 	}
 	
 	public static final SaveData getSaveData() {
 		return instance;
 	}
 }
