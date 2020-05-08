 package valificationtool.model;
 
 import java.util.ArrayList;
 
 
 public class FixProgram {
 
 	public final static String UPPER_UNBOUND_ERROR = "UpperUnboundError";
 	private int error_line;
 	private int line;
 	private String programPath;
 	private String projectName;
 	private String type;
 	private String fixBefore;
 	private String fixAfter;
 
 	FixProgram(int error_line, int line, String type, String programPath, String projectName, String fixBefore, String fixAfter){
 		this.setError_line(error_line);
 		this.line = line;
 		this.setType(type);
 		this.programPath = programPath;
 		this.projectName = projectName;
 		this.fixBefore = fixBefore;
 		this.fixAfter = fixAfter;
 	}
 
 	public int getLine() {
 		return line;
 	}
 
 
 	public void setLine(int line) {
 		this.line = line;
 	}
 
 
 	public String getProgramPath() {
 		return programPath;
 	}
 
 
 	public void setProgramPath(String programPath) {
 		this.programPath = programPath;
 	}
 
 
 	public String getProjectName() {
 		return projectName;
 	}
 
 
 	public void setProjectName(String projectName) {
 		this.projectName = projectName;
 	}
 
 
 	public String getType() {
 		return type;
 	}
 
 	public void setType(String type) {
 		this.type = type;
 	}
 
 	public String getFixBefore() {
 		return fixBefore;
 	}
 
 
 	public void setFixBefore(String fixBefore) {
 		this.fixBefore = fixBefore;
 	}
 
 
 	public String getFixAfter() {
 		return fixAfter;
 	}
 
 
 	public void setFixAfter(String fixAfter) {
 		this.fixAfter = fixAfter;
 	}
 
 	public String getFixProblemStatement(){
 		String result = "";
 		if(getType().equals(UPPER_UNBOUND_ERROR)){
 			result = "<p>Add the branch condition on line " + getLine() + " as the following.<br><br>";
 			result = result + "<pre>" + getFixBefore() + "</pre><br>";
 			result = result + "->" + "<br>";
 			result = result + "<pre>" + getFixAfter() + "</pre><br></p>";
 		}
 
 		return result;
 	}
 
 	/**
 	 * 実行経路をもとに配列範囲外参照の修復方法を提示する。
 	 */
 	public static ArrayList<FixProgram> getFixProgramUpperUnbound(ArrayList<ProgramChart> p, ErrorInformation e,
 			ArrayList<MacroInformationCollection> macro_information_collections){
 		ArrayList<FixProgram> result = new ArrayList<FixProgram>();
 		ProgramChart branch_p = null;
 		String variable_name;
 		ArrayList<VariableInfo> before_variable_info;
 		int arraySize = 0;
 		MacroInformationCollection macro_info_collection;
 		ArrayList<MacroInformation> macro_info;
 		String error_expression;
 		/*実行経路からbranchを探す*/
 		for(int i = p.size() - 1; i > 0; i-- ){
 			if(p.get(i).getType().equals("branch")){
 				branch_p = p.get(i);
 				break;
 			}
 		}
 		/*配列の数を求める*/
 		variable_name = e.getVariable();
 		before_variable_info = p.get(p.size() - 1).before_variable_info;
 		for(int i = before_variable_info.size() - 1;
 				i > 0; i--){
 			if(before_variable_info.get(i).getName().equals(variable_name)){
 				arraySize = before_variable_info.get(i).getMax_size();
 				break;
 			}
 		}
 
 		/*範囲外をさせた式を抽出させる*/
 		error_expression = e.getExpression();
 
 		/*TODO:配列の数に一致する変数を取得する*/
 
 		/*該当ファイルのマクロ一覧を抽出する*/
 		macro_info_collection = MacroInformationCollection.find(macro_information_collections, p.get(p.size()-1).getFile());
 
 		/*マクロ一覧から配列の値に該当するマクロを抽出する*/
 		macro_info = macro_info_collection.findMacroByValue(arraySize + "");
 
 		/*branchの文字列から以下の式を追加する
 		 *
 		 * program
 		 * */
 		if(branch_p != null){
 			for(int i = 0; i < macro_info.size(); i++){
 				FixProgram f = new FixProgram(p.get(p.size() - 1).getLine(), branch_p.getLine(), UPPER_UNBOUND_ERROR, branch_p.getFile(),
 						branch_p.getProject_name(), branch_p.getProgram(),
 						error_expression + " < " + macro_info.get(i).getName() + " && " + branch_p.getProgram());
 				result.add(f);
 			}
 		}
 		/*エラーの発生箇所がブランチでなければ、該当コードに条件分岐を追加する*/
		if(!p.get(p.size() - 1).getType().equals("branch")){
 
 			for(int i = 0; i < macro_info.size(); i++){
				FixProgram f = new FixProgram(p.get(p.size() - 1).getLine(), p.get(p.size() - 1).getLine(), UPPER_UNBOUND_ERROR, branch_p.getFile(),
						branch_p.getProject_name(), p.get(p.size() - 1).getProgram(),
						"if(" + error_expression + " < " + macro_info.get(i).getName() + " ){\n" + p.get(p.size() - 1).getProgram() + "\n}\n");
 				result.add(f);
 			}
 		}

 		return result;
 	}
 
 	public int getError_line() {
 		return error_line;
 	}
 
 	public void setError_line(int error_line) {
 		this.error_line = error_line;
 	}
 }
