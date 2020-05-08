 package com.codekata.quoteboxes;
 
 import java.io.IOException;
 import java.nio.charset.Charset;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.util.ArrayList;
 import java.util.List;
 
 public class PuzzleOutput {
 
 	private static final String OUTPUT_DIR = "./output/";
 	
 	private Quote m_quote;
 	private List<PuzzleColumn> m_columns;
 	private int m_numRows = 0;
 	
 	public PuzzleOutput(Quote quote, List<PuzzleColumn> columns) {
 		m_quote = quote;
 		m_columns = columns;
 		m_numRows = m_columns.get(0).getSize();
 	}
 	
 	public void outputFile() {
 		Path outputFile = Paths.get(OUTPUT_DIR, m_quote.getPerson().replace(" ", "") + ".html");
 		Charset charset = Charset.forName("utf-8");
 		List<String> output = new ArrayList<>();
 		
 		addFileHeader(output);
 		addPuzzle(output);		
 		addFileFooter(output);
 		
 		try {
 			Files.write(outputFile, output, charset);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public String toString() {
 		StringBuffer output = new StringBuffer();
 		output.append("== " + m_quote.getPerson() + " ==\n");
 		
 		// Print the letter columns
 		for (int rowNum = 0; rowNum < m_numRows; rowNum++) {
 			StringBuffer rowOutput = new StringBuffer();
 			
 			for (PuzzleColumn column : m_columns) {
 				rowOutput.append(column.getLetterAt(rowNum));
 			}
 			
 			output.append(rowOutput.toString() + "\n");
 		}
 		
 		output.append("A: " + m_quote.getQuote() + "\n");
 		
 		return output.toString();
 	}
 	
 	private void addFileHeader(List<String> output) {
 		output.add("<html>");
 		output.add("<head>");
 		output.add("<style>");
 		
 		output.add("#puzzle_cells");
 		output.add("{");
 		output.add("font-family:\"Trebuchet MS\", Arial, Helvetica, sans-serif;");
		output.add("margin-right: 20px;");
 		output.add("border-collapse:collapse;");
		output.add("float: left;");
 		output.add("}");
 		output.add("#puzzle_cells td, #puzzle_cells th"); 
 		output.add("{");
 		output.add("font-size:1.2em;");
 		output.add("border:1px solid #000000;");
 		output.add("}");
 		output.add("#puzzle_cells td.inactive");
 		output.add("{");
 		output.add("background-color:#000000;");
 		output.add("}");
 		output.add("#puzzle_cells td.shaded");
 		output.add("{");
 		output.add("background-color:#888888;");
 		output.add("}");
 		
 		output.add("</style>");
 		output.add("</head>");
 		output.add("<body>");
 	}
 	
 	private void addPuzzle(List<String> output) {
 		List<String> letters = new ArrayList<>();
 		List<String> cells = new ArrayList<>();
 		
		output.add("<div>" + m_quote.getPerson() + "</div><div><table id=\"puzzle_cells\" cellspacing=\"0\" cellpadding=\"0\">");
 		
 		// Print the letter columns
 		for (int rowNum = 0; rowNum < m_numRows; rowNum++) {
 			
 			// Add the letters
 			letters.add("<tr>");
 			for (PuzzleColumn column : m_columns) {
 				String letter = column.getLetterAt(rowNum);
				addPuzzleSquare(letters, " ".equals(letter) ? "shaded" : "white", " ".equals(letter) ? "&nbsp;" : letter);
 			}
 			letters.add("</tr>");
 			
 			// Add the cells
 			cells.add("<tr>");
 			for (PuzzleColumn column : m_columns) {
				addPuzzleSquare(cells, column.isLetterAt(rowNum) ? "white" : "inactive", "&nbsp;");
 			}
 			cells.add("</tr>");
 		}
 		
 		output.addAll(letters);
 		output.addAll(cells);
 		
 		output.add("</table></div>");
	}
	
	private void addPuzzleSquare(List<String> squares, String imageName, String contents) {
		squares.add("<td><div style=\"position: relative\">");
		squares.add("<img src=\"../resources/images/" + imageName + ".png\" style=\"width:2em; height:2em; border: 0; padding: 0\" />");
		squares.add("<span style=\"position: absolute; top: 50%; left: 50%; margin-top: -0.6em; margin-left: -0.3em\">" + contents + "</span>");
		squares.add("</div></td>");
 	}
 	
 	private void addFileFooter(List<String> output) {
 		output.add("</body>");
 		output.add("</html>");
 	}
 }
