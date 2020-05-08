 package alignment;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 
 public class AlignmentCalculator
 {
 	final static String ENTRY_SEPARATOR = ",";
 	final static String SCORE_DIRECTION_SEPARATOR = ";";
 	final static String NEWLINE = System.getProperty("line.separator");
 	public final static String NEEDLEMAN_WUNCH = "Needleman-Wunch";
 	public final static String SMITH_WATERMAN = "Smith-Waterman";
 	
 	private String x;
 	private String y;
 	private AlignmentElement[][] a;
 	private AlignmentElement[][] n;
 	private AlignmentElement[][] w;
 	/**
 	 * Used for local alignment printing
 	 */
 	private AlignmentElement highest;
 	private String xalig, align, yalig;
 	private boolean local;
 	private AlignmentScoringSystem scoring;
 	
 	public AlignmentCalculator(String s1, String s2, AlignmentScoringSystem scoring_, boolean local_)
 	{
 		x = s1;
 		y = s2;
 		a = new AlignmentElement[y.length() + 1][x.length() + 1];
 		n = new AlignmentElement[y.length() + 1][x.length() + 1];
 		w = new AlignmentElement[y.length() + 1][x.length() + 1];
 		for (int j = 0; j <= y.length(); j++)
 		{
 			for (int i = 0; i <= x.length(); i++)
 			{
 				a[j][i] = new AlignmentElement(j, i, null);
 				n[j][i] = new AlignmentElement(j, i, null);
 				w[j][i] = new AlignmentElement(j, i, null);
 			}
 		}
 		highest = a[0][0];
 		local = local_;
 		scoring = scoring_;
 	}
 	
 	public void fillScoreArray()
 	{
 		int col, row;
 		double northwest;
 		double west;
 		double north;
 		double best;
 		PointerDirection dir;
 		a[0][0].score = 0.0;
		if (x.length() >= 2)
 		{
			a[0][1].score = localScore(scoring.gapStart + scoring.gapContinue);
			a[0][1].direction = PointerDirection.WEST;
			n[0][1].score = Double.NEGATIVE_INFINITY;
		}
		if (y.length() >= 2)
		{
			a[1][0].score = localScore(scoring.gapStart + scoring.gapContinue);
			a[1][0].direction = PointerDirection.NORTH;
			w[1][0].score = Double.NEGATIVE_INFINITY;
		}
		for (col = 2; col <= x.length(); col++)
		{
			a[0][col].score = localScore(scoring.gapContinue * col + scoring.gapStart);
 			a[0][col].direction = PointerDirection.WEST;
 			n[0][col].score = Double.NEGATIVE_INFINITY;
 		}
		for (row = 2; row <= y.length(); row++)
 		{
			a[row][0].score = localScore(scoring.gapContinue * row + scoring.gapStart);
 			a[row][0].direction = PointerDirection.NORTH;
 			w[row][0].score = Double.NEGATIVE_INFINITY;
 		}
 		for (row = 1; row <= y.length(); row++)
 		{
 			for (col = 1; col <= x.length(); col++)
 			{
 				// N
 				double from_a = localScore(a[row - 1][col].score + scoring.gapStart + scoring.gapContinue);
 				double from_n = localScore(n[row - 1][col].score + scoring.gapContinue);
 				north = n[row][col].score = Math.max(from_a, from_n);
 				// W
 				from_a = localScore(a[row][col - 1].score + scoring.gapStart + scoring.gapContinue);
 				double from_w = localScore(w[row][col - 1].score + scoring.gapContinue);
 				west = w[row][col].score = Math.max(from_a, from_w);
 				// A
 				if (x.charAt(col - 1) == y.charAt(row - 1))
 				{
 					northwest = localScore(a[row - 1][col - 1].score + scoring.match);
 				}
 				else
 				{
 					northwest = localScore(a[row - 1][col - 1].score + scoring.mismatch);
 				}
 				best = northwest;
 				dir = PointerDirection.NORTHWEST;
 				if (north > best)
 				{
 					dir = PointerDirection.NORTH;
 					best = north;
 				}
 				if (west > best)
 				{
 					dir = PointerDirection.WEST;
 					best = west;
 				}
 				a[row][col].score = best;
 				a[row][col].direction = dir;
 				
 				if (a[row][col].score >= highest.score)
 				{
 					highest = a[row][col];
 				}
 			}
 		}
 	}
 	
 	public void print3(Double score)
 	{
 		String string = String.format("%03.0f, ", score);
 		if (string.length() <= 8)
 		{
 			System.out.print(string);
 		}
 		else
 		{
 			System.out.print("***");
 		}
 	}
 	
 	public void printArray()
 	{
 		System.out.print("         ");
 		for (int col = 0; col < x.length(); col++)
 		{
 			System.out.printf("%5s", x.charAt(col));
 		}
 		System.out.println();
 		System.out.print("     ");
 		for (int col = 0; col < a[0].length; col++)
 		{
 			System.out.printf("%4.0f,", a[0][col].score);
 		}
 		System.out.println();
 		for (int row = 1; row < a.length; row++)
 		{
 			System.out.printf("%5s", y.charAt(row - 1));
 			for (int col = 0; col < a[row - 1].length; col++)
 			{
 				System.out.printf("%4.0f,", a[row][col].score);
 			}
 			System.out.println();
 		}
 	}
 	
 	public void setAlignment()
 	{
 		int row = local ? highest.row : y.length();
 		int col = local ? highest.col : x.length();
 		StringBuilder xb = new StringBuilder();
 		xb.append(x.substring(col).toLowerCase());
 		StringBuilder ab = new StringBuilder();
 		StringBuilder yb = new StringBuilder();
 		yb.append(y.substring(row).toLowerCase());
 		char gapChar;
 		char matchChar;
 		char mismatchChar;
 		boolean passedZero = false;
 		while ((col > 0) || (row > 0))
 		{
 			PointerDirection dir = a[row][col].direction;
 			if (a[row][col].score <= 0.0 && local)
 			{
 				passedZero = true;
 			}
 			a[row][col].isPartOfAlignment = isPartOfAlignment(passedZero, row, col);
 			gapChar = passedZero ? ' ' : '-';
 			matchChar = passedZero ? ' ' : '|';
 			mismatchChar = passedZero ? ' ' : 'X';
 			switch (dir)
 			{
 				case NORTH:
 					xb.insert(0, gapChar);
 					ab.insert(0, gapChar);
 					yb.insert(0, localChar(y.charAt(row - 1), passedZero, row, col));
 					row--;
 					break;
 				case WEST:
 					xb.insert(0, localChar(x.charAt(col - 1), passedZero, row, col));
 					ab.insert(0, gapChar);
 					yb.insert(0, gapChar);
 					col--;
 					break;
 				case NORTHWEST:
 					char xc = localChar(x.charAt(col - 1), passedZero, row, col);
 					char yc = localChar(y.charAt(row - 1), passedZero, row, col);
 					xb.insert(0, xc);
 					ab.insert(0, (xc == yc) ? matchChar : mismatchChar);
 					yb.insert(0, yc);
 					col--;
 					row--;
 					break;
 			}
 		}
 		if (!local)
 		{
 			a[0][0].isPartOfAlignment = true;
 		}
 		xalig = xb.toString();
 		align = ab.toString();
 		yalig = yb.toString();
 	}
 	
 	public void printAlignment()
 	{
 		System.out.println(xalig);
 		System.out.println(align);
 		System.out.println(yalig);
 		System.out.println();
 	}
 	
 	public String getAlignment()
 	{
 		StringBuilder sb = new StringBuilder();
 		String newline = System.getProperty("line.separator");
 		
 		sb.append(xalig);
 		sb.append(newline);
 		sb.append(align);
 		sb.append(newline);
 		sb.append(yalig);
 		
 		return sb.toString();
 	}
 	
 	/**
 	 * @param column
 	 *            Column index
 	 * @param row
 	 *            Row index
 	 * @return
 	 */
 	public double getValue(int row, int column)
 	{
 		return a[row][column].score;
 	}
 	
 	public boolean isPartOfAlignment(int row, int column)
 	{
 		return a[row][column].isPartOfAlignment;
 	}
 	
 	private double localScore(double i)
 	{
 		return (local && i < 0) ? 0 : i;
 	}
 	
 	private char localChar(char c, boolean passedZero, int row, int col)
 	{
 		return isPartOfAlignment(passedZero, row, col) ? c : Character.toLowerCase(c);
 	}
 	
 	private boolean isPartOfAlignment(boolean passedZero, int row, int col)
 	{
 		return !(local && (row > highest.row || col > highest.col || passedZero));
 	}
 	
 	public void exportToFile(File file) throws IOException
 	{
 		BufferedWriter w = new BufferedWriter(new FileWriter(file));
 		w.write("# Data format:");
 		w.write(NEWLINE);
 		w.write("#   entry = score");
 		w.write(SCORE_DIRECTION_SEPARATOR);
 		w.write("direction");
 		w.write(NEWLINE);
 		w.write("#   entry_list = entry_list[");
 		w.write(ENTRY_SEPARATOR);
 		w.write("entry]");
 		w.write(NEWLINE);
 		w.write("# Horizontal string: ");
 		w.write(x);
 		w.write(NEWLINE);
 		w.write("# Vertical string:   ");
 		w.write(y);
 		w.write(NEWLINE);
 		w.write("# Scoring:");
 		w.write(NEWLINE);
 		w.write("#   Match:        ");
 		w.write(Integer.toString(scoring.match));
 		w.write(NEWLINE);
 		w.write("#   Mismatch:     ");
 		w.write(Integer.toString(scoring.mismatch));
 		w.write(NEWLINE);
 		w.write("#   Gap start:    ");
 		w.write(Integer.toString(scoring.gapStart));
 		w.write(NEWLINE);
 		w.write("#   Gap continue: ");
 		w.write(Integer.toString(scoring.gapContinue));
 		w.write(NEWLINE);
 		w.write("# ");
 		w.write(local ? SMITH_WATERMAN : NEEDLEMAN_WUNCH);
 		w.write(" alignment:");
 		w.write(NEWLINE);
 		w.write("# ");
 		w.write(xalig);
 		w.write(NEWLINE);
 		w.write("# ");
 		w.write(align);
 		w.write(NEWLINE);
 		w.write("# ");
 		w.write(yalig);
 		w.write(NEWLINE);
 		for (int row = 0; row <= y.length(); row++)
 		{
 			for (int col = 0; col <= x.length(); col++)
 			{
 				w.write(String.format("%.0f", a[row][col].score));
 				w.write(SCORE_DIRECTION_SEPARATOR);
 				String dir = a[row][col].direction == null ? "?" : a[row][col].direction.getShortDescription();
 				w.write(dir);
 				w.write(ENTRY_SEPARATOR);
 			}
 			w.write(NEWLINE);
 		}
 		w.close();
 	}
 	
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args)
 	{
 		AlignmentScoringSystem scoring = new AlignmentScoringSystem(0, -1, 2, -1);
 		AlignmentCalculator ac = new AlignmentCalculator(args[0], args[1], scoring, false);
 		ac.local = true;
 		ac.fillScoreArray();
 		ac.printArray();
 		System.out.println();
 		ac.setAlignment();
 		ac.printAlignment();
 	}
 }
