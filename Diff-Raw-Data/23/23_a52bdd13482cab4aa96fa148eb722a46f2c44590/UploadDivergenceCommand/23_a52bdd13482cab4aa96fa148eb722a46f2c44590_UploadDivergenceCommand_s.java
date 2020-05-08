 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.Scanner;
 import java.util.StringTokenizer;
 
 /**
  * TODO Put here a description of what this class does.
  * 
  * @author schepedw. Created Mar 14, 2013.
  */
 public class UploadDivergenceCommand extends UploadCommand {
 	private File fileLocation;
 	private String options;
 	private String name;
 
 	public UploadDivergenceCommand(String fileLocation, String options,
 			String name) {
 		this.fileLocation = new File(fileLocation);
 		this.options = options;
 		this.name = name;
 		if (this.name == "") {
 			this.name = getDate();
 		}
 	}
 
 	@Override
 	public String execute() {
 		try {
 			String result = upload(this.fileLocation);
			System.out.println(result);
 			return result;
 		} catch (Exception exception) {
 			exception.printStackTrace();
 		}
 		return "Upload Failed!";
 	}
 
 	@Override
 	public ArrayList<String[]> parseFile() throws FileNotFoundException {
 		FileInputStream fileIn = new FileInputStream(this.fileLocation);
 		Scanner reader = new Scanner(fileIn);
 		ArrayList<String[]> rows = new ArrayList<String[]>();
 		while (reader.hasNextLine()) {
 			String[] columns = parseLine(reader.nextLine());
 			rows.add(columns);
 		}
 		return rows;
 	}
 
 	private String[] parseLine(String line) {
 		StringTokenizer tokenizer = new StringTokenizer(line);
 		String chrom = tokenizer.nextToken();
 		String position = tokenizer.nextToken();
 		String divValue = tokenizer.nextToken();
 		return new String[] { chrom, position, divValue };
 	}
 
 	@Override
 	public String getSQLStatement(String[] row) {
 
 		String chromosome = row[0];
 		int position = Integer.valueOf(row[1]);
 		int divValue = Integer.valueOf(row[2]);
 		String sql = String
 				.format("INSERT into `Divergence` (`DivName`, `Chromosome`, `Position`, `DivValue`) VALUES ('%s','%s','%d','%d');",
 						this.name, chromosome, position, divValue);
 
 		return sql;
 	}
 
 	@Override
 	public void pipeOutput() {
 		// TODO Auto-generated method stub.
 
 	}
 
 }
