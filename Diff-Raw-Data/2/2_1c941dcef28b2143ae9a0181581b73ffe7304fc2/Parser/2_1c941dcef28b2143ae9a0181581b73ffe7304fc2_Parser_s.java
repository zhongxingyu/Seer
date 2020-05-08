 package bin.jku.se.tetris.coco;
 
 public class Parser {
 	public static final int _EOF = 0;
 	public static final int _integer = 1;
 	public static final int _float = 2;
 	public static final int _word = 3;
 	public static final int maxT = 13;
 
 	static final boolean T = true;
 	static final boolean x = false;
 	static final int minErrDist = 2;
 
 	public Token t;    // last recognized token
 	public Token la;   // lookahead token
 	int errDist = minErrDist;
 	
 	public Scanner scanner;
 	public Errors errors;
 
 	
 
 	public Parser(Scanner scanner) {
 		this.scanner = scanner;
 		errors = new Errors();
 	}
 
 	void SynErr (int n) {
 		if (errDist >= minErrDist) errors.SynErr(la.line, la.col, n);
 		errDist = 0;
 	}
 
 	public void SemErr (String msg) {
 		if (errDist >= minErrDist) errors.SemErr(t.line, t.col, msg);
 		errDist = 0;
 	}
 	
 	void Get () {
 		for (;;) {
 			t = la;
 			la = scanner.Scan();
 			if (la.kind <= maxT) {
 				++errDist;
 				break;
 			}
 
 			la = t;
 		}
 	}
 	
 	void Expect (int n) {
 		if (la.kind==n) Get(); else { SynErr(n); }
 	}
 	
 	boolean StartOf (int s) {
 		return set[s][la.kind];
 	}
 	
 	void ExpectWeak (int n, int follow) {
 		if (la.kind == n) Get();
 		else {
 			SynErr(n);
 			while (!StartOf(follow)) Get();
 		}
 	}
 	
 	boolean WeakSeparator (int n, int syFol, int repFol) {
 		int kind = la.kind;
 		if (kind == n) { Get(); return true; }
 		else if (StartOf(repFol)) return false;
 		else {
 			SynErr(n);
 			while (!(set[syFol][kind] || set[repFol][kind] || set[0][kind])) {
 				Get();
 				kind = la.kind;
 			}
 			return StartOf(syFol);
 		}
 	}
 	
 	void TetrisStatistics() {
 		int gameCount = 0, sumScore = 0, maxScore = 0, pScore = 0, fileErrors = 0; 
 		boolean dateValid = false, timeValid = false; 	
 		String bestPlayer = "", pName = "Max Mustermann"; 	
 		
 		Expect(4);
 		dateValid = Date();
 		timeValid = Time();
 		while (la.kind == 5) {
 			String entry; 
 			entry = Entry();
 			gameCount++;
 			 		// Java does not support multiple return values -> use concatenated string as workaround
 			if (entry != null) { 		
 				int delPos = entry.indexOf("#");
 				int score = Integer.parseInt(entry.substring(delPos + 1));
 				String player = entry.substring(0, delPos);
 				
 				// Sum Score
 				sumScore += score;
 				
 				// Best Player
 				if (score > maxScore) {
 					maxScore = score;
 					bestPlayer = player;
 				}
 				
 				// Highest Score of specified player
 				if (player.equals(pName)) {
 					if (score > pScore) {
 						pScore = score;
 					}
 				}
 			} else {
 				fileErrors++;
 			}
 			
 		}
 		if (!dateValid || !timeValid) {
		System.out.println("-- aborted because the header is invalid");
 		} else if (fileErrors == 0) {
 			System.out.println("Tetris Statistics");
 			System.out.println();
 			System.out.println("Games Played:\t" + gameCount); 
 			System.out.println("Average Score:\t" + (float)sumScore / gameCount);
 			System.out.println("Best Player:\t" + bestPlayer);
 			System.out.println("---");
 			System.out.println("Highest Score of '" + pName + "': " + pScore);
 			System.out.println();
 		} else {
 			System.out.println("-- aborted because " + fileErrors + " entr" + (fileErrors == 1 ? "y is" : "ies are") + " invalid");
 		}
 		
 	}
 
 	boolean  Date() {
 		boolean  valid;
 		int day, month, year = 0; 
 		day = Day();
 		Expect(6);
 		month = Month();
 		Expect(6);
 		year = Year();
 		valid = day >= 1 && day <= 31;
 		valid = valid && month >= 1 && month <= 12;
 		valid = valid && year >= 1970;		
 		
 		return valid;
 	}
 
 	boolean  Time() {
 		boolean  valid;
 		int hour, minute, second = 0; 
 		hour = Hour();
 		Expect(8);
 		minute = Minute();
 		Expect(8);
 		second = Second();
 		valid = hour >= 0 && hour < 24;
 		valid = valid && minute >= 0 && minute < 60;
 		valid = valid && second >= 0 && second < 60;		
 		
 		return valid;
 	}
 
 	String  Entry() {
 		String  entry;
 		String name; int score = 0; boolean validDate=false; boolean validTime = false; 
 		name = Name();
 		Mail();
 		validDate = Date();
 		validTime = Time();
 		score = Score();
 		if (la.kind == 5) {
 			Comment();
 		}
 		Platform();
 		if (validDate && validTime) {
 		entry = name + "#" + score; 
 		} else {
 			entry = null;
 		}
 		
 		return entry;
 	}
 
 	String  Name() {
 		String  name;
 		Expect(5);
 		Expect(3);
 		name = t.val; 
 		while (la.kind == 3) {
 			Get();
 			name += " " + t.val; 
 		}
 		Expect(5);
 		return name;
 	}
 
 	void Mail() {
 		Expect(3);
 		while (la.kind == 6) {
 			Get();
 			Expect(3);
 		}
 		Expect(7);
 		Expect(3);
 		Expect(6);
 		Expect(3);
 	}
 
 	int  Score() {
 		int  score;
 		Expect(1);
 		score = Integer.parseInt(t.val); 
 		return score;
 	}
 
 	void Comment() {
 		Expect(5);
 		while (la.kind == 3) {
 			Get();
 		}
 		Expect(5);
 	}
 
 	void Platform() {
 		if (la.kind == 9) {
 			Get();
 		} else if (la.kind == 10) {
 			Get();
 		} else if (la.kind == 11) {
 			Get();
 		} else if (la.kind == 12) {
 			Get();
 		} else SynErr(14);
 	}
 
 	int  Day() {
 		int  day;
 		Expect(1);
 		day = Integer.parseInt(t.val); 
 		return day;
 	}
 
 	int  Month() {
 		int  month;
 		Expect(1);
 		month = Integer.parseInt(t.val); 
 		return month;
 	}
 
 	int  Year() {
 		int  year;
 		Expect(1);
 		year = Integer.parseInt(t.val); 
 		return year;
 	}
 
 	int  Hour() {
 		int  hour;
 		Expect(1);
 		hour = Integer.parseInt(t.val); 
 		return hour;
 	}
 
 	int  Minute() {
 		int  minute;
 		Expect(1);
 		minute = Integer.parseInt(t.val); 
 		return minute;
 	}
 
 	int  Second() {
 		int  second;
 		Expect(1);
 		second = Integer.parseInt(t.val); 
 		return second;
 	}
 
 
 
 	public void Parse() {
 		la = new Token();
 		la.val = "";		
 		Get();
 		TetrisStatistics();
 		Expect(0);
 
 	}
 
 	private static final boolean[][] set = {
 		{T,x,x,x, x,x,x,x, x,x,x,x, x,x,x}
 
 	};
 } // end Parser
 
 
 class Errors {
 	public int count = 0;                                    // number of errors detected
 	public java.io.PrintStream errorStream = System.out;     // error messages go to this stream
 	public String errMsgFormat = "-- line {0} col {1}: {2}"; // 0=line, 1=column, 2=text
 	
 	protected void printMsg(int line, int column, String msg) {
 		StringBuffer b = new StringBuffer(errMsgFormat);
 		int pos = b.indexOf("{0}");
 		if (pos >= 0) { b.delete(pos, pos+3); b.insert(pos, line); }
 		pos = b.indexOf("{1}");
 		if (pos >= 0) { b.delete(pos, pos+3); b.insert(pos, column); }
 		pos = b.indexOf("{2}");
 		if (pos >= 0) b.replace(pos, pos+3, msg);
 		errorStream.println(b.toString());
 	}
 	
 	public void SynErr (int line, int col, int n) {
 		String s;
 		switch (n) {
 			case 0: s = "EOF expected"; break;
 			case 1: s = "integer expected"; break;
 			case 2: s = "float expected"; break;
 			case 3: s = "word expected"; break;
 			case 4: s = "\"TetrisStatistics\" expected"; break;
 			case 5: s = "\"#\" expected"; break;
 			case 6: s = "\".\" expected"; break;
 			case 7: s = "\"@\" expected"; break;
 			case 8: s = "\":\" expected"; break;
 			case 9: s = "\"PC\" expected"; break;
 			case 10: s = "\"Android\" expected"; break;
 			case 11: s = "\"iPhone\" expected"; break;
 			case 12: s = "\"Playstation\" expected"; break;
 			case 13: s = "??? expected"; break;
 			case 14: s = "invalid Platform"; break;
 			default: s = "error " + n; break;
 		}
 		printMsg(line, col, s);
 		count++;
 	}
 
 	public void SemErr (int line, int col, String s) {	
 		printMsg(line, col, s);
 		count++;
 	}
 	
 	public void SemErr (String s) {
 		errorStream.println(s);
 		count++;
 	}
 	
 	public void Warning (int line, int col, String s) {	
 		printMsg(line, col, s);
 	}
 	
 	public void Warning (String s) {
 		errorStream.println(s);
 	}
 } // Errors
 
 
 class FatalError extends RuntimeException {
 	public static final long serialVersionUID = 1L;
 	public FatalError(String s) { super(s); }
 }
