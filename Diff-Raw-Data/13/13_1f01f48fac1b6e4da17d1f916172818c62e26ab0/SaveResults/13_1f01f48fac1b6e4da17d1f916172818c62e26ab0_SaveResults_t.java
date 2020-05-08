 package counter.saveresults;
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.util.Iterator;
 import java.util.List;
 
 import counter.filestatistics.*;
 
 
 
 public abstract class SaveResults {
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
 	public int numArgument;
 	public int numAssignment;
 	public int numLocalFunctionCall = 0;
 	public int numLibFunctionCall = 0;
 	public int numZeroOpAssign = 0;
 	public int numZeroOpCallAssign = 0;
 	public int numConstAssign = 0;
 	public int numParamDecl = 0;
 	public int numDeclInFor = 0;
 	public int numDeclInStmt = 0;
 	public int numDeclStmtWithInit = 0;
 	public int numAssignInDeclStmt = 0;
 	//for C and C++, numLocalFunctionCall2 is used to get the number of local
 	//function calls that have a caller. For Java, numLocalFunctionCall2 is used
 	//to get the number of local method calls.
 	public int numLocalFunctionCall2 = 0;
 	public int numLocalGetterSetterCall = 0;
 	public int numLibGetterSetterCall = 0;
 
 
 	public void writeResult(String fileName,
 			List<FileStatistics> fileList, List<String> functionList,
 			List<String> functionCallList, List<String> classList,
 			List<String> callerList, int numLocalCall1, Boolean saveFunction, Boolean saveOperator, Boolean savefilestat) {
 		FileStatistics fileStatistics;
 		String fileInfo;
 		try{
 			int index = fileName.indexOf(".xml");
 			String outFileName = fileName.substring(0, index);
 			//create file
 			FileWriter fw = new FileWriter(outFileName + ".txt");
 			BufferedWriter writer = new BufferedWriter(fw);
 			StringBuilder fileBuilder = new StringBuilder();
 			Iterator<FileStatistics> it = fileList.iterator();
 			if(savefilestat){
 				while(it.hasNext()){
 					numFile++;
 					fileStatistics = it.next();
 					getTotalStatistics(fileStatistics);
 					fileInfo = getFileStatisticsInfo(fileStatistics);
 					fileBuilder.append(fileInfo);
 					fileBuilder.append("\n");
 				}
 			}
 			else{
 				while(it.hasNext()){
 					numFile++;
 					fileStatistics = it.next();
 					getTotalStatistics(fileStatistics);
 				}
 			}
 			numLocalFunctionCall2 = getLocalFunctionCallNumber(functionList, functionCallList, classList, callerList);
 			numLocalFunctionCall = numLocalCall1 + numLocalFunctionCall2;
 			numLibFunctionCall = functionCallList.size() - numLocalFunctionCall;
 			String totalInfo = getTotalStatisticsInfo();
 			writer.write(totalInfo);
 			writer.write(fileBuilder.toString());
 			writer.close();
 			if(saveFunction){
				writeFunction(outFileName, functionList, functionCallList);
 			}
 			if(saveOperator){
				writeOperator(outFileName, fileList);
 			}
 		}catch (Exception e){//Catch exception if any
 			System.err.println("Error: " + e.getMessage());
 		}		
 	}
 
 	public void writeFunction(String fileName, List<String> functionList,
 			List<String> functionCallList) {
 		try{
 			//create file
			FileWriter fw_function = new FileWriter(fileName +"_functions.txt");
			FileWriter fw_functioncall = new FileWriter(fileName +"_functioncalls.txt");
 			BufferedWriter writer_function = new BufferedWriter(fw_function);
 			BufferedWriter writer_functioncall = new BufferedWriter(fw_functioncall);
 			Iterator<String> it_function = functionList.iterator();
 			StringBuilder functionBuilder = new StringBuilder();
 			while(it_function.hasNext()){
 				functionBuilder.append(it_function.next());
 				functionBuilder.append("\n");
 			}
 			writer_function.write(functionBuilder.toString());
 			writer_function.close();
 			Iterator<String> it_functioncall = functionCallList.iterator();
 			StringBuilder functioncallBuilder = new StringBuilder();
 			while(it_functioncall.hasNext()){
 				functioncallBuilder.append(it_functioncall.next());
 				functioncallBuilder.append("\n");
 			}
 			writer_functioncall.write(functioncallBuilder.toString());
 			writer_functioncall.close();
 		}catch (Exception e){//Catch exception if any
 			System.err.println("Error: " + e.getMessage());
 		}		
 	}
 
 	public void writeOperator(String fileName, 
 			List<FileStatistics> fileList) {
 		FileStatistics fileStatistics;
 		try{
 			int index = fileName.indexOf(".");
 			String outFileName = fileName.substring(0, index);
 			//create file
 			FileWriter fw_operator = new FileWriter(outFileName +"_operator.txt");
 			BufferedWriter writer_operator = new BufferedWriter(fw_operator);
 			String operatorInfo = "";
 			Iterator<FileStatistics> it = fileList.iterator();
 			while(it.hasNext()){
 				numFile++;
 				fileStatistics = it.next();
 				operatorInfo = getOperatorInfo(fileStatistics.numOperatorList);
 				writer_operator.write(fileStatistics.fileName);
 				writer_operator.write("\n");
 				writer_operator.write(operatorInfo);
 			}
 			writer_operator.close();
 		}catch (Exception e){//Catch exception if any
 			System.err.println("Error: " + e.getMessage());
 		}		
 	}
 
 
 	private String getOperatorInfo(List<Integer> numOperatorList) {
 		StringBuilder opBuilder = new StringBuilder();
 		Iterator <Integer> opIt = numOperatorList.iterator();
 		int n = 0;
 		while(opIt.hasNext()){
 			n = opIt.next();
 			opBuilder.append(n);
 			opBuilder.append("\n");
 		}
 		return opBuilder.toString();
 	}
 
 	public String getFileStatisticsInfo(FileStatistics fileStatistics){
 		String samepart = getSameFileStatisticsInfo(fileStatistics);
 		String diffpart = getDiffFileStatisticsInfo(fileStatistics);
 		return samepart+diffpart;
 	}
 
 	private String getSameFileStatisticsInfo(FileStatistics fileStatistics) {
 		FileStatistics fs = fileStatistics;
 		StringBuilder sBuilder = new StringBuilder();
 		sBuilder.append("*****  File Statistics  *****");
 		sBuilder.append("\n");
 		sBuilder.append("\n");
 		sBuilder.append("File name: [[[ " + fs.fileName + " ]]]");
 		sBuilder.append("\n");
 		sBuilder.append("\n");
 		sBuilder.append("Total line: " + fs.numTotalLine);
 		sBuilder.append("\n");
 		sBuilder.append("Comment line: " + fs.numCommentLine);
 		sBuilder.append("\n");
 		sBuilder.append("Blank line: " + fs.numBlankLine);
 		sBuilder.append("\n");
 		sBuilder.append("Function declaration: " + fs.numFunctionDecl);
 		sBuilder.append("\n");
 		sBuilder.append("Function: " + fs.numFunction);
 		sBuilder.append("\n");
 		sBuilder.append("Declaration statement: " + fs.numDeclstmt);
 		sBuilder.append("\n");
 		sBuilder.append("Declaration:" + fs.numDecl);
 		sBuilder.append("\n");
 		sBuilder.append("Blocks: " + fs.numBlock);
 		sBuilder.append("\n");
 		sBuilder.append("Expressions: " + fs.numExpr);
 		sBuilder.append("\n");
 		sBuilder.append("Expression statements: " + fs.numExprstmt);
 		sBuilder.append("\n");
 		sBuilder.append("Call: " + fs.numCall);
 		sBuilder.append("\n");
 		sBuilder.append("Continue: " + fs.numContinue);
 		sBuilder.append("\n");
 		sBuilder.append("Break: " + fs.numBreak);
 		sBuilder.append("\n");
 		sBuilder.append("Return: " +fs.numReturn);
 		sBuilder.append("\n");
 		sBuilder.append("For: " + fs.numFor);
 		sBuilder.append("\n");
 		sBuilder.append("If: " + fs.numIf);
 		sBuilder.append("\n");
 		sBuilder.append("Else: " + fs.numElse);
 		sBuilder.append("\n");
 		sBuilder.append("While: " + fs.numWhile);
 		sBuilder.append("\n");
 		sBuilder.append("Do: " + fs.numDo);
 		sBuilder.append("\n");
 		sBuilder.append("Switch: " + fs.numSwitch);
 		sBuilder.append("\n");
 		sBuilder.append("Case: " + fs.numCase);
 		sBuilder.append("\n");
 		sBuilder.append("Parameter list: " + fs.numParamList);
 		sBuilder.append("\n");
 		sBuilder.append("Param: " + fs.numParam);
 		sBuilder.append("\n");
 		sBuilder.append("Argument list: " + fs.numArguList);
 		sBuilder.append("\n");
 		sBuilder.append("Argument: " + fs.numArgu);
 		sBuilder.append("\n");
 		sBuilder.append("Assignment: " + fs.numAssignment);
 		sBuilder.append("\n");
 		sBuilder.append("Zero operator assignment: " + fs.numZeroOpAssign);
 		sBuilder.append("\n");
 		sBuilder.append("Zero operator call assignment: " + fs.numZeroOpCallAssign);
 		sBuilder.append("\n");
 		sBuilder.append("Const assignment: " + fs.numConstAssign);
 		sBuilder.append("\n");
 		sBuilder.append("Parameter declaration: " + fs.numParamDecl);
 		sBuilder.append("\n");
 		sBuilder.append("Declaration in for: " + fs.numDeclInFor);
 		sBuilder.append("\n");
 		sBuilder.append("Declaration in declaration statement: " + fs.numDeclInStmt);
 		sBuilder.append("\n");
 		sBuilder.append("Declaration statement with initialization: " + fs.numDeclStmtWithInit);
 		sBuilder.append("\n");
 		sBuilder.append("Assignment in declaration statement" + fs.numAssignInDeclStmt);
 		sBuilder.append("\n");
 		return sBuilder.toString();
 
 	}
 
 	public void getTotalStatistics(FileStatistics fs){
 		getSameTotalStatistics(fs);
 		getDiffTotalStatistics(fs);
 
 	}
 
 	public void getSameTotalStatistics(FileStatistics fs){
 		numTotalLine += fs.numTotalLine;
 		numCommentLine += fs.numCommentLine;
 		numBlankLine += fs.numBlankLine;
 		numFunctionDecl += fs.numFunctionDecl;
 		numFunction += fs.numFunction;
 		numDeclstmt += fs.numDeclstmt;
 		numDecl += fs.numDecl;
 		numBlock += fs.numBlock;
 		numExpr += fs.numExpr;
 		numExprstmt += fs.numExprstmt;
 		numCall += fs.numCall;
 		numContinue += fs.numContinue;
 		numBreak += fs.numBreak;
 		numReturn += fs.numReturn;
 		numFor += fs.numFor;
 		numIf += fs.numIf;
 		numElse += fs.numElse;
 		numWhile += fs.numWhile;
 		numDo += fs.numDo;
 		numSwitch += fs.numSwitch;
 		numCase += fs.numCase;
 		numParamList += fs.numParamList;
 		numParam += fs.numParam;
 		numArguList += fs.numArguList;
 		numArgument += fs.numArgu;
 		numAssignment += fs.numAssignment;
 		numZeroOpAssign += fs.numZeroOpAssign;
 		numZeroOpCallAssign += fs.numZeroOpCallAssign;
 		numConstAssign += fs.numConstAssign;
 		numParamDecl += fs.numParamDecl;
 		numDeclInFor += fs.numDeclInFor;
 		numDeclInStmt += fs.numDeclInStmt;
 		numDeclStmtWithInit += fs.numDeclStmtWithInit;
 		numAssignInDeclStmt += fs.numAssignInDeclStmt;
 	}
 
 	public String getTotalStatisticsInfo(){
 		String samepart = getSameTotalStatisticsInfo();
 		String diffpart = getDiffTotalStatisticsInfo();
 		return samepart+diffpart;
 	}
 
 	private String getSameTotalStatisticsInfo() {
 		StringBuilder total = new StringBuilder();
 		total.append("\n");
 		total.append("*** Total Statistics Information ***");
 		total.append("\n");
 		total.append("\n");
 		total.append("File: " + numFile);
 		total.append("\n");
 		total.append("Total line: " + numTotalLine);
 		total.append("\n");
 		total.append("Comment line: " + numCommentLine);
 		total.append("\n");
 		total.append("Blank line: " + numBlankLine);
 		total.append("\n");
 		total.append("Function declaration: " + numFunctionDecl);
 		total.append("\n");
 		total.append("Function: " + numFunction);
 		total.append("\n");
 		total.append("Call: " + numCall);
 		total.append("\n");
 		total.append("Expression statement: " + numExprstmt);
 		total.append("\n");
 		total.append("Expression: " + numExpr);
 		total.append("\n");
 		total.append("Block: " + numBlock);
 		total.append("\n");
 		total.append("Declaration: " + numDecl);
 		total.append("\n");
 		total.append("Declaration statement: " + numDeclstmt);
 		total.append("\n");
 		total.append("Continue: " + numContinue);
 		total.append("\n");
 		total.append("Break: " + numBreak);
 		total.append("\n");
 		total.append("Return: " + numReturn);
 		total.append("\n");
 		total.append("For: " + numFor);
 		total.append("\n");
 		total.append("If: " + numIf);
 		total.append("\n");
 		total.append("Else: " + numElse);
 		total.append("\n");
 		total.append("While: " + numWhile);
 		total.append("\n");
 		total.append("Switch: " + numSwitch);
 		total.append("\n");
 		total.append("Case: " + numCase);
 		total.append("\n");
 		total.append("Parameter list: " + numParamList);
 		total.append("\n");
 		total.append("Argument list: " + numArguList);
 		total.append("\n");
 		total.append("Parameter: " + numParam);
 		total.append("\n");
 		total.append("Argument: " + numArgument);
 		total.append("\n");
 		total.append("Assignment: " + numAssignment);
 		total.append("\n");
 		total.append("Do: " + numDo);
 		total.append("\n");
 		total.append("Local function call: " + numLocalFunctionCall);
 		total.append("\n");
 		total.append("Library function call: " + numLibFunctionCall);
 		total.append("\n");
 		total.append("Local gettersetter call: " + numLocalGetterSetterCall);
 		total.append("\n");
 		total.append("Library gettersetter call: " + numLibGetterSetterCall);
 		total.append("\n");
 		total.append("Zero operator assignment: " + numZeroOpAssign);
 		total.append("\n");
 		total.append("Zero operator call assignment: " + numZeroOpCallAssign);
 		total.append("\n");
 		total.append("Const assignment: " + numConstAssign);
 		total.append("\n");
 		total.append("Parameter declaration: " + numParamDecl);
 		total.append("\n");
 		total.append("Declaration in for: " + numDeclInFor);
 		total.append("\n");
 		total.append("Declaration in declaration statement: " + numDeclInStmt);
 		total.append("\n");
 		total.append("Declaration statement with initialization: " + numDeclStmtWithInit);
 		total.append("\n");
 		total.append("Assignment in declaration statement: " + numAssignInDeclStmt);
 		total.append("\n");
 		return total.toString();
 	}
 
 	public abstract String getDiffTotalStatisticsInfo();
 	public abstract String getDiffFileStatisticsInfo(FileStatistics fileStatistics);
 	public abstract void getDiffTotalStatistics(FileStatistics fs);
 	public abstract int getLocalFunctionCallNumber(List<String> functionList,
 	List<String> functionCallList, List<String> classList, List<String> callerList);
 }
