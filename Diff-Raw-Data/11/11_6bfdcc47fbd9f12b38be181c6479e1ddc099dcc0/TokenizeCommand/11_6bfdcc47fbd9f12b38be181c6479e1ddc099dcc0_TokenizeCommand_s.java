 package org.amplafi.flow.ui.command;
 
 import java.util.Iterator;
 import java.util.List;
 
 import org.amplafi.flow.utils.AdminTool;
 import org.amplafi.flow.utils.ParameterTokenizer;
 
 public class TokenizeCommand extends AShellCommand {
 
 	protected TokenizeCommand(boolean setHelp, String setOptions) {
 		super(setHelp, "tokenize", setOptions);
 		// TODO Auto-generated constructor stub
 	}
 
 	@Override
 	protected String helpString() {
 		return "For testing purposes of the tokenizer";
 	}
 
 	@Override
 	protected int executeCommand(AdminTool adminTool) {
 		String chain = getOptions();
		//adminTool.
 		/*List<String> stList = ParameterTokenizer.tokenize(chain);
 		int l = stList.size();
 		System.out.println(l + " tokens detected");
 		Iterator<String> it = stList.iterator();
 		for(int i =0; i< l; i++){
 			System.out.println("Token " + i + ": " + it.next());
 		}*/
 		return 0;
 	}
 
 }
