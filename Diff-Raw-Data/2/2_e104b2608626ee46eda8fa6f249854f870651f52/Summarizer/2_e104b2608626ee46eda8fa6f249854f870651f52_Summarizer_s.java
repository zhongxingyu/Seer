 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 import com.csvreader.CsvWriter;
 
 
 public abstract class Summarizer {
 
 	public int numFile;
 	public int numTotalLine;
 	public int numCommentLine;
 	public int numBlankLine;
 	public int numFunction;
 	public int numFunctionDecl;
 	public int numBlock;
 	public int numDeclstmt;
 	public int numDecl;
 	public int numExprstmt;
 	public int numExpr;
 	public int numIf;
 	public int numElse;
 	public int numWhile;
 	public int numFor;
 	public int numContinue;
 	public int numBreak;
 	public int numDo;
 	public int numSwitch;
 	public int numCase;
 	public int numReturn;
 	public int numCall;
 	public int numParamList;
 	public int numParam;
 	public int numArguList;
 	public int numArgu;
 	public int numAssignment;
 	public int numLocalFunctionCall = 0;
 	public int numLibFunctionCall = 0;
 	public int numLocalGetterSetterCall = 0;
 	public int numLibGetterSetterCall = 0;
 	public int numZeroOpAssign = 0;
 	public int numZeroOpCallAssign = 0;
 	public int numConstAssign = 0;
 	public int numExecuteLine = 0;
 	public int numLoop = 0;
 	public int numDeclInDeclStmt = 0;
 	public int numDeclInFor = 0;
 	public int numParamDecl = 0;
 	public int numDeclStmtWithInit = 0;
 
 	CsvWriter totalWriter;
 	String fdir;
 	File folder;
 	File[] listOfFiles;
 	FileInputStream fstream; 
 	DataInputStream in;
 	BufferedReader br;
 	int numProject;
 	String resulttype;
 
 	public void writeStatistics(){
 		folder = new File(fdir);
 		listOfFiles = folder.listFiles();
 		numProject = listOfFiles.length;
 		String fileName;
 		String str;
 		int index;
 		String name;
 		int number;
 		int proNum = 0;
 
 		createFile();
 		if(resulttype.equals("line")){
 			writeSameColumnName();
 			writeDiffColumnName();
 		}
 		else{
 			writeSameStatColumnName();
 			writeDiffStatColumnName();
 		}
 		try {
 			for (int i = 0; i < numProject; i++) {
 				fileName = listOfFiles[i].getName();
 				if (listOfFiles[i].isFile()&&fileName.endsWith(".txt")) {
 					fstream = new FileInputStream(fdir+fileName);
 					in = new DataInputStream(fstream);
 					br = new BufferedReader(new InputStreamReader(in));
 					str = br.readLine();
 					while (!str.equals("-------------------------")) {
 						index = str.indexOf(":");
 						if(index >= 0){
 							name = str.substring(0, index);
 							number =Integer.valueOf(str.substring(index+2, str.length())); 
 							getSameNumber(name, number);
 							getDiffNumber(name, number);
 						}
 						str = br.readLine();
 					}
 					numExecuteLine = numTotalLine - numCommentLine - numBlankLine;
 					numLoop = numFor + numWhile;
 					if(resulttype.equals("line")){
 						writeSameNumber(proNum, fileName);
 						writeDiffNumber(proNum, fileName);
 					}
 					else{
 						writeSameStatNumber(proNum, fileName);
 						writeDiffStatNumber(proNum, fileName);
 					}
 					proNum++;
 				}
 			}
 			if(in!=null){
 				in.close();
 			}
 			totalWriter.close();
 		}catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	private void writeSameStatNumber(int proNum, String fileName) {
 		try{
 			totalWriter.write(String.valueOf(proNum));
 			totalWriter.write(String.valueOf(fileName.substring(0, fileName.indexOf(".txt"))));
 			totalWriter.write(String.valueOf(numCall));
 			totalWriter.write(String.valueOf(numIf));
 			totalWriter.write(String.valueOf(numAssignment));
 			totalWriter.write(String.valueOf(numFunction));
 			totalWriter.write(String.valueOf(numReturn));
 			totalWriter.write(String.valueOf(numDeclstmt));
			totalWriter.write(String.valueOf("numDeclStmtWithInit"));
 			totalWriter.write(String.valueOf(numContinue));
 			totalWriter.write(String.valueOf(numBreak));
 			totalWriter.write(String.valueOf(numFunctionDecl));
 			totalWriter.write(String.valueOf(numFor));
 			totalWriter.write(String.valueOf(numElse));
 			totalWriter.write(String.valueOf(numWhile));
 			totalWriter.write(String.valueOf(numDo));
 			totalWriter.write(String.valueOf(numSwitch));
 			totalWriter.write(String.valueOf(numCase));
 		}catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	private void writeSameStatColumnName() {
 		try{
 			totalWriter.write("Number");
 			totalWriter.write("Project");
 			totalWriter.write("FunctionCall");
 			totalWriter.write("If");
 			totalWriter.write("Assignment");
 			totalWriter.write("Function");
 			totalWriter.write("Return");
 			totalWriter.write("DeclStmt");
 			totalWriter.write("DeclStmtWithInit");
 			totalWriter.write("Continue");
 			totalWriter.write("Break");
 			totalWriter.write("FunctionDeclaration");
 			totalWriter.write("For");
 			totalWriter.write("Else");
 			totalWriter.write("While");
 			totalWriter.write("Do");
 			totalWriter.write("Switch");
 			totalWriter.write("Case");
 		}catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	public void writeSameColumnName(){
 		try{
 			//			totalStatistics.writeComment("Total Statistics with Line Percent for java project"+"\n");
 			totalWriter.write("Number");
 			totalWriter.write("Project");
 			totalWriter.write("File");
 			totalWriter.write("TotalLines");
 			totalWriter.write("ExecuteLines");
 			totalWriter.write("FunctionCall");
 			totalWriter.write("FunctionCall_percent");
 			totalWriter.write("If");
 			totalWriter.write("If_percent");
 			totalWriter.write("Assignment");
 			totalWriter.write("Assignment_percent");
 			totalWriter.write("Function");
 			totalWriter.write("Function_percent");
 			totalWriter.write("FunctionDeclaration");
 			totalWriter.write("FunctionDeclaration_percent");
 			totalWriter.write("Loop");
 			totalWriter.write("Loop_percent");
 			totalWriter.write("DeclarationStatement");
 			totalWriter.write("DeclarationStatement_percent");
 			totalWriter.write("Declaration");
 			totalWriter.write("Declaration_percent");
 			totalWriter.write("ExpressionStatement");
 			totalWriter.write("ExpressionStatement_percent");
 			totalWriter.write("Expression");
 			totalWriter.write("Expression_percent");
 			totalWriter.write("ParameterList");
 			totalWriter.write("ParameterList_percent");
 			totalWriter.write("Parameter");
 			totalWriter.write("Parameter_percent");
 			totalWriter.write("ArgumentList");
 			totalWriter.write("ArgumentList_percent");
 			totalWriter.write("Argument");
 			totalWriter.write("Argument_percent");
 			totalWriter.write("Block");
 			totalWriter.write("Block_percent");
 			totalWriter.write("Continue");
 			totalWriter.write("Continue_percent");
 			totalWriter.write("Break");
 			totalWriter.write("Break_percent");
 			totalWriter.write("Return");
 			totalWriter.write("Return_percent");
 			totalWriter.write("For");
 			totalWriter.write("For_percent");
 			totalWriter.write("Else");
 			totalWriter.write("Else_percent");
 			totalWriter.write("While");
 			totalWriter.write("While_percent");
 			totalWriter.write("Do");
 			totalWriter.write("Do_percent");
 			totalWriter.write("Switch");
 			totalWriter.write("Switch_percent");
 			totalWriter.write("Case");
 			totalWriter.write("Case_percent");
 			totalWriter.write("LocalFunctionCall");
 			totalWriter.write("LibFunctionCall");
 			totalWriter.write("GetterSetterCall");
 			totalWriter.write("ZeroOperatorAssign");
 			totalWriter.write("ZeroOpCallAssign");
 			totalWriter.write("ConstAssign");
 			totalWriter.write("DeclInDeclStmt");
 			totalWriter.write("DeclInFor");
 			totalWriter.write("ParamDecl");
 		}catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void getSameNumber(String name, int number) {
 
 		if(name.equals("File")){
 			numFile = number;
 		}
 		if(name.equals("Total line")){
 			numTotalLine = number;
 		}
 		if(name.equals("Comment line")){
 			numCommentLine = number;
 		}
 		if(name.equals("Blank line")){
 			numBlankLine = number;
 		}
 		if(name.equals("Call")){
 			numCall = number;
 		}
 		if(name.equals("If")){
 			numIf = number;
 		}
 		if(name.equals("Assignment")){
 			numAssignment = number;
 		}
 		if(name.equals("Function")){
 			numFunction = number;
 		}
 		if(name.equals("Function declaration")){
 			numFunctionDecl = number;
 		}
 		if(name.equals("Switch")){
 			numSwitch = number;
 		}
 		if(name.equals("Declaration statement")){
 			numDeclstmt = number;
 		}
 		if(name.equals("Declaration")){
 			numDecl = number;
 		}
 		if(name.equals("Expression statement")){
 			numExprstmt = number;
 		}
 		if(name.equals("Expression")){
 			numExpr = number;
 		}
 		if(name.equals("Parameter list")){
 			numParamList = number;
 		}
 		if(name.equals("Parameter")){
 			numParam = number;
 		}
 		if(name.equals("Argument list")){
 			numArguList = number;
 		}
 		if(name.equals("Argument")){
 			numArgu = number;
 		}
 		if(name.equals("Block")){
 			numBlock = number;
 		}
 		if(name.equals("Continue")){
 			numContinue = number;
 		}
 		if(name.equals("Break")){
 			numBreak = number;
 		}
 		if(name.equals("Return")){
 			numReturn = number;
 		}
 		if(name.equals("For")){
 			numFor = number;
 		}
 		if(name.equals("Else")){
 			numElse = number;
 		}
 		if(name.equals("While")){
 			numWhile = number;
 		}
 		if(name.equals("Case")){
 			numCase = number;
 		}
 		if(name.equals("Do")){
 			numDo = number;
 		}
 		if(name.equals("Local function call")){
 			numLocalFunctionCall = number;
 		}
 		if(name.equals("Library function call")){
 			numLibFunctionCall = number;
 		}
 		if(name.equals("Local getter setter call")){
 			numLocalGetterSetterCall = number;
 		}
 		if(name.equals("Library getter setter call")){
 			numLibGetterSetterCall = number;
 		}
 		if(name.equals("Zero operator assignment")){
 			numZeroOpAssign = number;
 		}
 		if(name.equals("Zero operator call assignment")){
 			numZeroOpCallAssign = number;
 		}
 		if(name.equals("Const assignment")){
 			numConstAssign = number;
 		}
 		if(name.equals("Declaration in declaration statement")){
 			numDeclInDeclStmt = number;
 		}
 		if(name.equals("Declaration in for")){
 			numDeclInFor = number;
 		}
 		if(name.equals("Parameter declaration")){
 			numParamDecl = number;
 		}
 		if(name.equals("Declaration statement with initialization")){
 			numDeclStmtWithInit = number;
 		}
 	}
 
 
 
 	private void writeSameNumber(int proNum, String filename) {
 		double percent;
 		try{
 			totalWriter.write(String.valueOf(proNum));
 			totalWriter.write(filename.substring(0, filename.indexOf(".txt")));
 			totalWriter.write(String.valueOf(numFile));
 			totalWriter.write(String.valueOf(numTotalLine));
 			totalWriter.write(String.valueOf(numExecuteLine));
 			totalWriter.write(String.valueOf(numCall));
 			percent = (double)numCall/numExecuteLine;
 			totalWriter.write(String.valueOf((double)Math.round(percent*10000)/10000));
 			totalWriter.write(String.valueOf(numIf));
 			percent = (double)numIf/numExecuteLine;
 			totalWriter.write(String.valueOf((double)Math.round(percent*10000)/10000));
 			totalWriter.write(String.valueOf(numAssignment));
 			percent = (double)numAssignment/numExecuteLine;
 			totalWriter.write(String.valueOf((double)Math.round(percent*10000)/10000));
 			totalWriter.write(String.valueOf(numFunction));
 			percent = (double)numFunction/numExecuteLine;
 			totalWriter.write(String.valueOf((double)Math.round(percent*10000)/10000));
 			totalWriter.write(String.valueOf(numFunctionDecl));
 			percent = (double)numFunctionDecl/numExecuteLine;
 			totalWriter.write(String.valueOf((double)Math.round(percent*10000)/10000));
 			totalWriter.write(String.valueOf(numLoop));
 			percent = (double)numLoop/numExecuteLine;
 			totalWriter.write(String.valueOf((double)Math.round(percent*10000)/10000));
 			totalWriter.write(String.valueOf(numDeclstmt));
 			percent = (double)numDeclstmt/numExecuteLine;
 			totalWriter.write(String.valueOf((double)Math.round(percent*10000)/10000));
 			totalWriter.write(String.valueOf(numDecl));
 			percent = (double)numDecl/numExecuteLine;
 			totalWriter.write(String.valueOf((double)Math.round(percent*10000)/10000));
 			totalWriter.write(String.valueOf(numExprstmt));
 			percent = (double)numExprstmt/numExecuteLine;
 			totalWriter.write(String.valueOf((double)Math.round(percent*10000)/10000));
 			totalWriter.write(String.valueOf(numExpr));
 			percent = (double)numExpr/numExecuteLine;
 			totalWriter.write(String.valueOf((double)Math.round(percent*10000)/10000));
 			totalWriter.write(String.valueOf(numParamList));
 			percent = (double)numParamList/numExecuteLine;
 			totalWriter.write(String.valueOf((double)Math.round(percent*10000)/10000));
 			totalWriter.write(String.valueOf(numParam));
 			percent = (double)numParam/numExecuteLine;
 			totalWriter.write(String.valueOf((double)Math.round(percent*10000)/10000));
 			totalWriter.write(String.valueOf(numArguList));
 			percent = (double)numArguList/numExecuteLine;
 			totalWriter.write(String.valueOf((double)Math.round(percent*10000)/10000));
 			totalWriter.write(String.valueOf(numArgu));
 			percent = (double)numArgu/numExecuteLine;
 			totalWriter.write(String.valueOf((double)Math.round(percent*10000)/10000));
 			totalWriter.write(String.valueOf(numBlock));
 			percent = (double)numBlock/numExecuteLine;
 			totalWriter.write(String.valueOf((double)Math.round(percent*10000)/10000));
 			totalWriter.write(String.valueOf(numContinue));
 			percent = (double)numContinue/numExecuteLine;
 			totalWriter.write(String.valueOf((double)Math.round(percent*10000)/10000));
 			totalWriter.write(String.valueOf(numBreak));
 			percent = (double)numBreak/numExecuteLine;
 			totalWriter.write(String.valueOf((double)Math.round(percent*10000)/10000));
 			totalWriter.write(String.valueOf(numReturn));
 			percent = (double)numReturn/numExecuteLine;
 			totalWriter.write(String.valueOf((double)Math.round(percent*10000)/10000));
 			totalWriter.write(String.valueOf(numFor));
 			percent = (double)numFor/numExecuteLine;
 			totalWriter.write(String.valueOf((double)Math.round(percent*10000)/10000));
 			totalWriter.write(String.valueOf(numElse));
 			percent = (double)numElse/numExecuteLine;
 			totalWriter.write(String.valueOf((double)Math.round(percent*10000)/10000));
 			totalWriter.write(String.valueOf(numWhile));
 			percent = (double)numWhile/numExecuteLine;
 			totalWriter.write(String.valueOf((double)Math.round(percent*10000)/10000));
 			totalWriter.write(String.valueOf(numDo));
 			percent = (double)numDo/numExecuteLine;
 			totalWriter.write(String.valueOf((double)Math.round(percent*10000)/10000));
 			totalWriter.write(String.valueOf(numSwitch));
 			percent = (double)numSwitch/numExecuteLine;
 			totalWriter.write(String.valueOf((double)Math.round(percent*10000)/10000));
 			totalWriter.write(String.valueOf(numCase));
 			percent = (double)numCase/numExecuteLine;
 			totalWriter.write(String.valueOf((double)Math.round(percent*10000)/10000));
 			totalWriter.write(String.valueOf(numLocalFunctionCall));
 			totalWriter.write(String.valueOf(numLibFunctionCall));
 			totalWriter.write(String.valueOf(numLocalGetterSetterCall+
 					numLibGetterSetterCall));
 			totalWriter.write(String.valueOf(numZeroOpAssign));
 			totalWriter.write(String.valueOf(numZeroOpCallAssign));
 			totalWriter.write(String.valueOf(numConstAssign));
 			totalWriter.write(String.valueOf(numDeclInDeclStmt));
 			totalWriter.write(String.valueOf(numDeclInFor));
 			totalWriter.write(String.valueOf(numParamDecl));
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public abstract void createFile();
 	public abstract void writeDiffColumnName();
 	public abstract void writeDiffNumber(int i, String s);
 	public abstract void getDiffNumber(String name, int number);
 	public abstract void writeDiffStatColumnName();
 	public abstract void writeDiffStatNumber(int proNum, String fileName);
 }
