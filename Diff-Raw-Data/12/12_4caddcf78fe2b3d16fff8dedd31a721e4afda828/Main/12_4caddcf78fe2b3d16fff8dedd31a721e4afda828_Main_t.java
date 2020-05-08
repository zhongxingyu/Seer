 package hk.hku.cs.c7802.driver;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.List;
 
 import hk.hku.cs.c7802.base.conv.DayBase;
 import hk.hku.cs.c7802.base.time.TimeDiff;
 import hk.hku.cs.c7802.base.time.TimePoint;
 import hk.hku.cs.c7802.base.time.TimePointFormatException;
 import hk.hku.cs.c7802.curve.CurveConfig;
 import hk.hku.cs.c7802.curve.SimpleCurve;
 import hk.hku.cs.c7802.curve.YieldCurve;
 import hk.hku.cs.c7802.curve.util.LinearInterpolator;
 import hk.hku.cs.c7802.option.CallPutOption;
 import hk.hku.cs.c7802.option.Option;
 import hk.hku.cs.c7802.rate.CompoundRate;
 
 public class Main {
 	
 	public static void usage() {
 		String cli = String.format("java %s", Main.class.getName());
 		System.out.println(
 			"Usage: \n" +
 			"# Generate Swap Curve: \n" +
 			cli + " -y [-s curveSpec.csv] [-i curveDataInput]\n" +
 			"# Use Black Scholes or Binomial Tree or Monte-Carlo \n" +
 			cli + " -bs|-bt|-mc  \n" +
 			"\t -a|-e American/Europeen(default) \n" +
 			"\t -S StockPrice \n" +
 			"\t -E ExpiryDate \n" +
 			"\t -r Risk-Free-Continuous-Compounding-Rate \n" +
 			"\t -s \\sigma # Once given, we will use it to value the option\n" +
 			"\t -v Value-of-the-option        # once given we will use it to value the sigma\n" +
 			"\t option-class-name arg1 arg2 ...\n" +
 			" # List all available options\n" +
 			cli + " -l" +
 			" # Show available data format\n" +
 			cli + " -df"
 		);
 	}
 	
 	public static void listAllOptions() {
 		System.out.println(
 			"\t call Strike\n" +
 			"\t put Strike\n" +
 			"\t A a b c # if S is within [a, b], payout = |S-c|; else 0\n" +
 			"\t B a b c # R=Smax-Smin, then payout = 0.5R, if R>=a, else R if a>R>=b, else 0"
 		);	
 	}
 	private static Option parseOption(String[] args, int i, String optionType) {
 		if(args[i].equals("call")) {
 			if(args.length - i == 2) {
				// FIXME put the real Call here when the framework is fixed
 				// return new Call(double(args[i+1]), European/American);				
 				return null;
 			}
 		}
 		else if(args[i].equals("put")) {
 			if(args.length - i == 2) {
				// FIXME put the reall Put here when the framework is fixed				
 				// return new Put(double(args[i+1]), European/American);
 				return null;
 			}
 		}
 		else if(args[i].equals("A")) {
 			if(args.length - i == 4) {
				// FIXME put the real Option A here when the framework is fixed				
 				// return new OptionA(double(args[i+1]), double(args[i+2]), double(args[i+3]), European/American);
 				return null;
 			}
 		}
 		else if(args[i].equals("B")) {
 			if(args.length - i == 4) {
				// FIXME put the real OptionB here when the framework is fixed				
 				// return new OptionB(double(args[i+1]), double(args[i+2]), double(args[i+3]), European/American);
 				return null;
 			}
 		}		
 		return null;
 	}
 	
 	public static String[] getTypeRecord(List<String[]> curveSpec, String ID) {
 		for(String[] r: curveSpec) {
 			if(r.length > 2 && r[2] != null && r[2].equals(ID)) {
 				return r;
 			}
 		}
 		return null;
 	}
 	
 	public static void yieldCurve(String curveSpecFilename, String curveDataInputFilename) {
 		System.err.println("Error: not yet implemented");
 		
 		List<String[]> curveData;
 		List<String[]> curveSpec;
 		try {
 			String[] curveSpecHead = new String[3];
 			String[] curveDataHead = new String[2];
 			curveSpec = new CSVParser(curveSpecFilename, curveSpecHead).toList();
 			curveData = new CSVParser(curveDataInputFilename, curveDataHead).toList();
 			if(!(curveSpecHead[0].equals("InstrumentType") &&
 				curveSpecHead[1].equals("subType") &&
 				curveSpecHead[2].equals("ID") &&
 				curveDataHead[0].equals("ID") &&
 				curveDataHead[1].equals("Rate"))) {
 				System.err.println("Warning: the scheme of the curveSpec/curveData csv is not standard");
 			}
 			
 			
 		} catch (FileNotFoundException e) {	
 			e.printStackTrace();
 			return;
 		} catch (IOException e) {
 			e.printStackTrace();
 			return;
 		}
 		
 		CurveConfig config = new CurveConfig();
 		config.setCurveRateType(new CompoundRate(DayBase.ACT365, 4));
 		config.setInterpolator(new LinearInterpolator());
 		SimpleCurve s = new SimpleCurve(TimePoint.now(), config);
 		// s.addDataPoint(new TimeDiff(50), 0.92);
 		// s.addDataPoint(new TimeDiff(1), 0.999);
 		for(String[] record: curveData) {
 			int id = Integer.parseInt(record[0]);	// ID
 			double rate = Double.parseDouble(record[1]);
 			String[] typeRecord = getTypeRecord(curveSpec, record[0]);
 			if(typeRecord != null) {
 				System.out.println(String.format("%s %s %f\n", typeRecord[0], typeRecord[1], rate));
				// FIXME Put the record into the yield curve.
 			}
 			else {
 				System.err.println("Error at record, invalid ID");
			}			
 		}
 		
 		
 		YieldCurve curve = s;		
 		double df = curve.disFactorAfter(new TimeDiff(20));
 		System.out.println(df);		
 	}
 	
 	private static void handleYieldCurve(String args[]) {
 		String action = args[0];
 		String curveSpec = null;
 		String curveData = null;
 		for(int i = 1; i < args.length; i++) {
 			if(args[i].equals("-s")) {
 				curveSpec = args[i+1];
 				i++;
 			}
 			else if(args[i].equals("-i")) {
 				curveData = args[i+1];
 				i++;
 			}
 			else {
 				usage();
 				break;
 			}
 		}
 		if(curveSpec != null && curveData != null) {
 			yieldCurve(curveSpec, curveData);					
 		}
 		else {
 			usage();
 		}
 	}
 
 	private static void handleOptionEvaluation(String args[]) {
 		String action = args[0];
 		String optionType = "European";
 		Double stockPrice = null;
 		TimePoint expiryDate = null;
 		Double riskFreeRate = null;
 		Double sigma = null;
 		Double optionValue = null;
 		Option option = null;
 		for(int i = 1; i < args.length; i++) {
 			try {
 				if(args[i].equals("-a")) {
 					optionType = "American";
 				}
 				else if(args[i].equals("-S")) {
 					i++;
 					stockPrice = Double.parseDouble(args[i]); 
 				}
 				else if(args[i].equals("-E")) {
 					i++;
 					expiryDate = TimePoint.parse(args[i]);
 				}
 				else if(args[i].equals("-r")) {
 					i++;
 					riskFreeRate = Double.parseDouble(args[i]);
 				}
 				else if(args[i].equals("-s")) {
 					i++;
 					sigma = Double.parseDouble(args[i]);
 				}
 				else if(args[i].equals("-v")) {
 					i++;
 					optionValue = Double.parseDouble(args[i]);
 				}
 				else {
 					option = parseOption(args, i, optionType);
 					break;
 				}
 			}
 			catch(NumberFormatException e) {
 				System.err.println(String.format("Wrong number format: '%s' '%s'", args[i-1], args[i]));
 			} catch (TimePointFormatException e) {
 				System.err.println(String.format("Wrong date format: '%s' '%s'", args[i-1], args[i]));
 			}
 		}
 		if (stockPrice == null || expiryDate == null || riskFreeRate == null
 				|| option == null || (sigma == null && optionValue == null)) {
 			System.err.println("Wrong format!");
 			if(stockPrice == null) System.err.println("Please specify: -S StockPrice!");
 			if(expiryDate == null) System.err.println("Please specify: -E ExpiryDate!");
 			if(riskFreeRate == null) System.err.println("Please specify: -r RiskFreeRate!");
 			if(sigma == null && optionValue ==null) 
 				System.err.println("Please specify: -s sigma or -v optionValue!");
 			if(option == null) System.err.println("Please specify: option-name arg1 ...!");
 		}
 	}
 	
 	public static void main(String args[]) {
 		if(args.length > 0) {
 			if(args[0].equals("-l")) {
 				listAllOptions();
 			}
 			else if(args[0].equals("-df")) {
 				showDateFormat();
 			}
 			else if(args[0].equals("-y")) {
 				handleYieldCurve(args);
 			}
 			else if(args[0].equals("-bs") || args[0].equals("-bt") 
 					|| args[0].equals("-mc") ) {
 				handleOptionEvaluation(args);
 			}
 			else {
 				usage();
 			}
 		}
 		else {
 			usage();
 		}
 	}
 
 	private static void showDateFormat() {
 		// FIXME this is not correct
 		System.err.println("2011-03-21 17:18:42 GMT+08:00");
 	}
 
 }
