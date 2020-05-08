 import java.util.ArrayList;
 
 
 public class ArgCheck {
 	
 	public static void check(String[] argss){
 		ArrayList<String> args = new ArrayList<String>();
		for(String s : argss){
			args.add(s);
		}
 		if(args.contains("randommu")){
 			Constants._murand = true;
 			if(args.indexOf("randommu") < args.size() - 2){
 				Constants.randMuStart = Double.parseDouble(args.get(1));
 				Constants.randMuEnd = Double.parseDouble(args.get(2));
 			}
 		}
 		else if(args.contains("mu")){
 			Constants.muCheck = true;
 			if(args.indexOf("mu") < args.size() - 1){
 				Constants.muIncUp = Double.parseDouble(args.get(args.indexOf("mu") + 1 ));
 			}
 		}
 		else if(args.contains("constantep")){
 			Constants.ConstantEp = true;
 			if(args.indexOf("constantep") < args.size() - 1){
 				Constants.muIncUp = Double.parseDouble(args.get(args.indexOf("constantep") + 1 ));
 			}
 		}
 		else if(args.contains("repulse")){
 			Constants.Repulsive = true;
 			if(args.indexOf("repulse") < args.size() - 1){
 				Constants.repuslivePer = Integer.parseInt(args.get(args.indexOf("repulsive") + 1 ));
 			}
 		}
 		else if(args.contains("nodes")){
 			Constants.resetVals(Integer.parseInt(args.get(args.indexOf("nodes") + 1)),
 					Integer.parseInt(args.get(args.indexOf("nodes") + 2)));
 		}
 	}
 }
