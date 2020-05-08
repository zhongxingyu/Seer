 package me.kevinwells.darxen.data;
 
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.TimeZone;
 
import android.util.SparseArray;

 public class Description implements Serializable {
 	
 	private static final long serialVersionUID = 5398520238014480987L;
 	
 	public float lat;
 	public float lon;
 	
 	public OpMode opmode;
 	
 	public Date scanTime;
 	public Date genTime;
 	
 	public SymbologyBlock symbologyBlock;
 	
 	public Description() {
 		
 	}
 
 	public static Description parse(DataFileStream stream) throws ParseException, IOException {
 		Description res = new Description();
 		
 		if (stream.readShort() != -1)
 			throw new ParseMagicException("Description Block Header");
 		
 		res.lat = (float)stream.readInt() / 1000.0f;
 		res.lon = (float)stream.readInt() / 1000.0f;
 		stream.readShort();
 		stream.readShort();
 		short opmode = stream.readShort();
 		switch (opmode) {
 		case 0:
 			res.opmode = OpMode.MAINTENANCE;
 			break;
 		case 1:
 			res.opmode = OpMode.CLEAN_AIR;
 			break;
 		case 2:
 			res.opmode = OpMode.PRECIPITATION;
 			break;
 		default:
 			throw new ParseException("Invalid Operation Mode");	
 		}
 		stream.readShort();
 		stream.readShort();
 		stream.readShort();
 		{
 			short date;
 			int time;
 			date = stream.readShort();
 			time = stream.readInt();
 			res.scanTime = convertDate(date, time);
 	
 			date = stream.readShort();
 			time = stream.readInt();
 			res.genTime = convertDate(date, time);
 		}
 		
 		//prod specific codes
 		stream.readShort();
 		stream.readShort();
 		
 		stream.readShort();
 		
 		stream.readShort();
 		
 		for (int i = 0; i < 16; i++) {
 			stream.readByte();
 			stream.readByte();
 		}
 		
 		for (int i = 0; i < 7; i++) {
 			stream.readShort();
 		}
 		if (stream.readByte() > 1)
 			throw new ParseMagicException("Description Block blank");
 		stream.readByte();
 		
 		int symbOffset = stream.readInt();
 		stream.readInt();
 		stream.readInt();
 		
 		if (symbOffset > 0) {
 			res.symbologyBlock = SymbologyBlock.parse(stream);
 		}
 		
 		return res;
 	}
 	
 	private static Date convertDate(short date, int time) {
 		Calendar cal = Calendar.getInstance();
 		cal.clear();
 		cal.set(Calendar.YEAR, 1);
 		cal.set(Calendar.DATE, 1);
 		cal.setTimeZone(TimeZone.getTimeZone("GMT"));
 		cal.add(Calendar.DAY_OF_YEAR, date + 719161 + 2);
 		cal.add(Calendar.SECOND, time);
 		return cal.getTime();
 	}
 	
 	public enum OpMode {
 		MAINTENANCE,
 		CLEAN_AIR,
 		PRECIPITATION;
 		
		private static SparseArray<OpMode> types;
 		
 		static {
			types = new SparseArray<OpMode>();
 			for (OpMode type : OpMode.values()) {
 				types.put(type.ordinal(), type);
 			}
 		}
 		
 		public static OpMode fromOrdinal(int ordinal) {
 			return types.get(ordinal);
 		}
 	}
 }
