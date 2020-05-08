 package caskman.polygonsim;
 
 import java.awt.Color;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.regex.PatternSyntaxException;
 
 public class Parameters {
 	
 	final static int PARAMETER_COUNT = 4;
 	public static boolean WINDOWED;
 	public static Color DOT_COLOR;
 	public static Color LINE_COLOR;
 	public static Color BACKGROUND_COLOR;
 	private static String homeDir = System.getProperty("user.home") + File.separator;
 	private static String gameDir = homeDir + "PolygonSim" + File.separator;
 	private static String parameterDir = gameDir + "params" + File.separator;
 	private static String parameterFilePath = parameterDir + "params.txt";
 	public static final String[] dotColorChoices = {};
 
 
 	public static void load() {
 		File parameterFile = new File(parameterFilePath);
 		if (!parameterFile.exists())
 			loadDefaults();
 		else loadParameterFile(parameterFile);
 	}
 	
 	private static void loadParameterFile(File file) {
 		try {
 			BufferedReader in = new BufferedReader(new FileReader(file));
 			int count;
 			
 			for (count = 0; in.ready(); count++) {
 				String line = in.readLine();
 				if (line.compareTo("") == 0 || !loadParameter(line)) {
 					in.close();
 					loadDefaults();
 					return;
 				}
 			}
 			
 			if (count != PARAMETER_COUNT)
 				loadDefaults();
 			in.close();
 				
 			
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private static void loadDefaults() {
 		set2Defaults();
 		save();
 	}
 	
 	public static void save() {
 		FileWriter out = null;
 		try {
 			createParameterFilePath();
 			out = new FileWriter(new File(parameterFilePath));
 			ParameterCode[] codes = ParameterCode.values();
 			for (int i = 0; i < PARAMETER_COUNT; i++) {
 				saveParameter(out,codes[i]);
 				if (i != PARAMETER_COUNT-1)
 					out.write("\n");
 			}
 			out.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private static void createParameterFilePath() {
 		File f;
 		f = new File(gameDir);
 		if (!f.exists())
 			f.mkdir();
 		f = new File(parameterDir);
 		if (!f.exists())
 			f.mkdir();
 	}
 	
 	private static void saveParameter(FileWriter out,ParameterCode code) throws Exception {
 		out.write(code.toString() + " ");
 		switch (code) {
 		case WINDOWED:
 			out.write(WINDOWED+"");
 			break;
 		case DOT_COLOR:
 			out.write(color2String(DOT_COLOR));
 			break;
 		case LINE_COLOR:
 			out.write(color2String(LINE_COLOR));
 			break;
 		case BACKGROUND_COLOR:
 			out.write(color2String(BACKGROUND_COLOR));
 			break;
 			default: throw new Exception();
 		}
 	}
 	
 	private static void set2Defaults() {
 		WINDOWED = false;
 		DOT_COLOR = Color.RED;
 		LINE_COLOR = Color.GREEN;
 		BACKGROUND_COLOR = Color.YELLOW;
 	}
 	
 	private static boolean loadParameter(String line) {
 		String[] parts;
 		try {
 			parts = line.split(" ");
 		} catch (PatternSyntaxException e) {
 			return false;
 		}
 		if (parts.length != 2)
 			return false;
 		
 		ParameterCode code = getParameterCode(parts[0]);
 		if (code == ParameterCode.NO_CODE)
 			return false;
 		try {
 			switch (code) {
 			case WINDOWED:
 				WINDOWED = string2Boolean(parts[1]);
 				break;
 			case DOT_COLOR:
 				DOT_COLOR = string2Color(parts[1]);
 				break;
 			case LINE_COLOR:
 				LINE_COLOR = string2Color(parts[1]);
 				break;
 			case BACKGROUND_COLOR:
 				BACKGROUND_COLOR = string2Color(parts[1]);
 				break;
 			}
 		} catch (Exception e) {
 			return false;
 		}
 		return true;
 	}
 	
 	private static ColorCode string2ColorCode(String s) throws Exception {
 		ColorCode[] values = ColorCode.values();
 		for (ColorCode c : values) {
 			if (s.compareTo(c.toString()) == 0)
 				return c;
 		}
 		throw new Exception();
 	}
 	
 	private static ColorCode color2ColorCode(Color c) throws Exception {
 		if (c == Color.RED)
 			return ColorCode.Red;
 		else if (c == Color.BLUE)
 			return ColorCode.Blue;
 		else if (c == Color.GREEN)
 			return ColorCode.Green;
		else if ((c.getRGB()&0xffffff) == 0xff4800)
 			return ColorCode.Orange;
 		else if (c == Color.YELLOW)
 			return ColorCode.Yellow;
 		else if (c == Color.CYAN)
 			return ColorCode.Cyan;
 		else if (c == Color.GRAY)
 			return ColorCode.Gray;
 		else if (c == Color.MAGENTA)
 			return ColorCode.Magenta;
 		else if (c == Color.PINK)
 			return ColorCode.Pink;
 		else if (c == Color.WHITE)
 			return ColorCode.White;
 		throw new Exception();
 	}
 	
 	private static String color2String(Color c) throws Exception {
 		return color2ColorCode(c).toString();
 	}
 	
 	public static Color string2Color(String s) throws Exception {
 		ColorCode code = string2ColorCode(s);
 		switch (code) {
 		case Red:
 			return Color.RED;
 		case Blue:
 			return Color.BLUE;
 		case Green:
 			return Color.GREEN;
 		case Orange:
			return new Color(0xff4800);
 		case Yellow:
 			return Color.YELLOW;
 		case Cyan:
 			return Color.CYAN;
 		case Gray:
 			return Color.GRAY;
 		case Magenta:
 			return Color.MAGENTA;
 		case Pink:
 			return Color.PINK;
 		case White:
 			return Color.WHITE;
 		}
 		throw new Exception();
 	}
 	
 	private static boolean string2Boolean(String s) throws Exception {
 		if (s.compareTo(Boolean.toString(true)) == 0)
 			return true;
 		else if (s.compareTo(Boolean.toString(false)) == 0)
 			return false;
 		throw new Exception();
 	}
 	
 	private static ParameterCode getParameterCode(String s) {
 		ParameterCode[] codes = ParameterCode.values();
 		for (ParameterCode c : codes) {
 			if (s.compareTo(c.toString()) == 0)
 				return c;
 		}
 		return ParameterCode.NO_CODE;
 	}
 	
 	public static String[] getDotColorChoices() {
 		return getColorCodeChoices();
 	}
 	
 	private static String[] getColorCodeChoices() {
 		ColorCode[] codes = ColorCode.values();
 		String[] choices = new String[codes.length];
 		for (int i = 0; i < codes.length; i++) {
 			choices[i]= codes[i].toString();
 		}
 		return choices;
 
 	}
 	
 	public static String[] getLineColorChoices() {
 		return getColorCodeChoices();
 	}
 	
 	public static String[] getBackgroundColorChoices() {
 		return getColorCodeChoices();
 	}
 	
 	private enum ParameterCode {
 		WINDOWED,DOT_COLOR,LINE_COLOR,BACKGROUND_COLOR,NO_CODE
 	}
 	
 	private enum ColorCode {
 		Red,Blue,Green,Orange,Yellow,Cyan,Gray,Magenta,Pink,White
 	}
 	
 }
