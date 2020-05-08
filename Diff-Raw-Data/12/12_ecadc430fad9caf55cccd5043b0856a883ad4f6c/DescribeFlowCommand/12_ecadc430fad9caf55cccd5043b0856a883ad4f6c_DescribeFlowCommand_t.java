 package org.amplafi.flow.ui.command;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.amplafi.flow.utils.AdminTool;
 
 /**
  * @author bfv
  * Command to describe flows or APIs. Constructor Options should be API name and optionally flow name.
  */
 public class DescribeFlowCommand extends AShellCommand {
 
 	static final Pattern ONE_AND_MAYBE_TWO_WORDS = Pattern.compile("^([^\\s]+)\\s*([^\\s]*)$");
	static final Pattern NOTHING = Pattern.compile("^\\s*$");
 	
 	protected DescribeFlowCommand(String setOptions) {
 		super(setOptions);
 	}
 
 	@Override
 	public int execute(AdminTool adminTool) {
 		String st = this.getOptions();
 		Matcher m = ONE_AND_MAYBE_TWO_WORDS.matcher(st);
		Matcher m2 = NOTHING.matcher(st);
		if(m.matches() && !m2.matches()){
 			String api = m.group(1);
 			String flow = m.group(2);
 			boolean success = adminTool.describeFlow(api,flow);
 			if(!success){
 				System.out.println("Invalid API. Make sure you typed it correctly, available APIs: 'public', 'api', 'su'");
 			}
 
 		}else{
 			System.out.println("Invalid Options. Usage: describe <api> [<flow>]");
 		}
 		return 0;
 	}
 
 }
