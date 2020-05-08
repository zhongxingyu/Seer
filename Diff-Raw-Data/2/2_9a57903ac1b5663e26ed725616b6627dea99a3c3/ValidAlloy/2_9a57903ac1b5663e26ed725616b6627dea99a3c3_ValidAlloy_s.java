 import java.io.File;
 
 import java.io.IOException;
 
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.util.ArrayList;
 import java.util.HashMap;
 import edu.mit.csail.sdg.alloy4.A4Reporter;
 import edu.mit.csail.sdg.alloy4.Err;
 import edu.mit.csail.sdg.alloy4.ErrorWarning;
 import edu.mit.csail.sdg.alloy4compiler.ast.Command;
 import edu.mit.csail.sdg.alloy4compiler.ast.ExprVar;
 import edu.mit.csail.sdg.alloy4compiler.ast.Module;
 import edu.mit.csail.sdg.alloy4compiler.parser.CompUtil;
 import edu.mit.csail.sdg.alloy4compiler.translator.A4Options;
 import edu.mit.csail.sdg.alloy4compiler.translator.A4Solution;
 import edu.mit.csail.sdg.alloy4compiler.translator.TranslateAlloyToKodkod;
 
 import org.antlr.runtime.*;
 import org.apache.commons.io.FileUtils;
 import org.pmw.tinylog.Logger;
 
 
 public class ValidAlloy {
 	/**
 	 * Main method that buils a number of git filesystems, from a number of alloy instances.
 	 * It has two inputs, the file with the alloy model and the number of git filesystem to create
 	 * @param  		git alloy model(.als)
 	 * @param		Number of filesystem to create
 	 * @return 
 	 * @return      N git filesystems
 	 * @throws IOException 
 	 * @throws Err 
 	 * @see         main
 	 */
 	public static void executeValidAlloy(String input_text) throws IOException, Err{
 		 A4Reporter rep = new A4Reporter() {
 	            // For example, here we choose to display each "warning" by printing it to System.out
 	            @Override public void warning(ErrorWarning msg) {
 	                System.out.print("Relevance Warning:\n"+(msg.toString().trim())+"\n\n");
 	                System.out.flush();
 	            }
 	        };
 	        
 
 	        ArrayList<HashMap<String,String>> vars = null ; 
  			ArrayList<ArrayList<String>> arg = null;
  			ArrayList<ArrayList<String>> opts = null ;
  			ArrayList<String> preds = null ;
  			ArrayList<String> scopes = null ;
  			ArrayList<String> cmds = null ;
  			int n_cmds = 0 ;
  			int n_runs = 0 ;
 
     		
     		FileUtils.deleteDirectory(new File("output"));
     		System.out.println("===========         Parsing + Typechecking          =========== ");
     		
     		CfgLexer lex = new CfgLexer(new ANTLRFileStream(input_text, "UTF8"));
             	CommonTokenStream tokens = new CommonTokenStream(lex);
 
             	CfgParser g = new CfgParser(tokens);
                   	
             	try {
             		Logger.info("Parsing Config File \n");
             		
             		CfgParser.cfg_return cfg_obj = g.cfg();
             	
             		vars = cfg_obj.vars;
             		arg = cfg_obj.args;
             		opts = cfg_obj.opts;
             		preds = cfg_obj.preds;
             		scopes = cfg_obj.scopes;
             		cmds = cfg_obj.cmds;
             		n_cmds = cfg_obj.n_comands;
             		n_runs = cfg_obj.n_runs;
             		
             		System.out.println("Config File         : "+ input_text);
             		System.out.println("Number of runs      : "+n_runs);
 	    			System.out.println("Number of commands  : " +n_cmds);
     			
           
     			
     			
             	} catch (RecognitionException e) {
                 	e.printStackTrace();
             	}
     	
      
         int test_iterations = n_runs;
 
         
         for(int j = 0 ; j<n_cmds;j++){
         
         Utils.delTemporaryModel("src/git_dynamic.als");	
         	
         Module world = CompUtil.parseEverything_fromFile(rep, null, Utils.addPred("src/git_dynamic.als", preds.get(j), scopes.get(j)));
        
         Logger.info("Parsing Alloy File for "+preds.get(j));
         
         A4Solution sol=null ;
         
         // Choose some default options for how you want to execute the commands
         A4Options options = new A4Options();
         options.solver = A4Options.SatSolver.SAT4J;
         Command cmd1 = world.getAllCommands().get(0);
         sol = TranslateAlloyToKodkod.execute_command(rep, world.getAllReachableSigs(), cmd1, options);
         
         
         System.out.println("=========== Getting  solutions from git_dynamic.als =========== ");
     	System.out.println("Variables           : " +vars.get(j));
       	System.out.println("Predicate           : " +preds.get(j));
       	System.out.println("Arguments           : " +arg.get(j));
 		System.out.println("Scope               : " +scopes.get(j));
 		System.out.println("Commands            : "+ cmds.get(j));
 		System.out.println("Options             : " +opts.get(j));
 		System.out.println("===========             Running Instances           =========== ");
 		
 		boolean flagerr;	
 		boolean flagcontinue = true;
         for(int i =0; i< test_iterations && flagcontinue ;i++){
         	flagerr = false;
         	
         	System.out.print("\rInstance            : " + i);
       
 
         	Logger.info("********* Beginning of Instance : "+i+" *********\n\n\n\n\n ");
   
 	        String newpath = null;
 	        Path p  = null;
 	        Iterable<ExprVar> skolems = null;
 	        ExprVar preState = null;
 	        ExprVar posState = null;
 	        ArrayList<ExprVar> pathSkol = new ArrayList<ExprVar>();
         	
 	
         	if (sol.satisfiable())
         	{
         		HashMap<String,ExprVar> mapAtom =Utils.atom2ObjectMapE(sol.getAllAtoms());
         		newpath = "output/"+preds.get(j)+"/"+Integer.toString(i);
         		p = Paths.get(newpath);
         		skolems = sol.getAllSkolems();
         		Files.createDirectories(p);
         		
         		String preS = "$" + preds.get(j) +"_s";
         		String posS = "$" + preds.get(j) +"_s'";
         		
         //		System.out.println("PreS: " +preS);
         //		System.out.println("PosS: " +posS);
         		
         		preState= Utils.getEFromIterable(skolems, preS);
         		posState = Utils.getEFromIterable(skolems, posS);
 
        // 		System.out.println("See: " + skolems);
         		
         		
         		for(int t = 0;t<arg.get(j).size();t++){
         			
         			String skol = "$" + preds.get(j) +"_" + arg.get(j).get(t);
       //  		System.out.println("Skool " + skol);
         			pathSkol.add(Utils.getEFromIterable(skolems, skol));
         		
         		}
      //   		System.out.println("Pre it :" +preState);
      //   		System.out.println("Pos it :" +posState);
         		
         		FileSystemBuilder.buildFileSystem(sol,i,preds.get(j),world,preState,posState);
         		
         		String cmdpath = "output/"+preds.get(j)+"/"+Integer.toString(i);
         		
         		Logger.info("Instance "+i+" preState\n________________________________________________________________");
         		BuildGitObjects.buildObjects(sol, world, preds.get(j)+"/"+Integer.toString(i)+"/pre",preState,mapAtom);
         		
         		Logger.info("Instance "+i+" posState\n________________________________________________________________");
         		BuildGitObjects.buildObjects(sol, world, preds.get(j)+"/"+Integer.toString(i)+"/pos",posState,mapAtom);
         		
         		try {
					BuildGitObjects.runCmd(sol,world,cmdpath+"/pre",pathSkol.get(j),mapAtom,cmds.get(j),opts.get(j),vars.get(j));
 				} catch (Exception e) {
 					Path p_e = Paths.get(cmdpath+"/git_errors.txt");
 					Files.createFile(p_e);
 					Files.write(p_e, e.getMessage().getBytes("ISO-8859-1"));
 					Files.move(Paths.get(cmdpath), Paths.get(cmdpath+"_err"));
 					cmdpath = cmdpath+"_err";
 					flagerr = true;
 					sol.writeXML(cmdpath+"/instance"+i+".xml");
 				}
         		
         		if(!flagerr)
         		{
         			Utils.diffIndex(preds.get(j)+"/"+Integer.toString(i));
         			if(Utils.diffPosPre(preds.get(j)+"/"+Integer.toString(i)))
         			{
         				FileUtils.deleteDirectory(new File(cmdpath));
         			} else sol.writeXML("output/"+preds.get(j)+"/"+i+"/instance"+i+".xml");
         		}
         		Logger.info("********* End of Instance       : "+i+" ********* ");		
         	
         		sol=sol.next();
         	}else
         		flagcontinue = false;
         	
         	
         } 
         System.out.println("\n===========             Command terminated          =========== ");
         }
         Utils.delTemporaryModel("src/git_dynamic.als");
        
      
 	}
 
 	
 	public static void main(String[] args)  throws Err, IOException{
 	
 		System.out.println("===========                ValidAlloy               =========== ");
 		
 
 	    	if(args.length == 1){
 	    		
 	    		
 	    		
 	    	executeValidAlloy(args[0]);
 	    	FileUtils.moveFileToDirectory(new File("log.txt"), new File("output"),false);
 	    	
 	      } else System.out.println("Must provide config file to vallidAlloy");
 	    	System.out.println("===========           ValidAlloy terminated         =========== ");
 	}
 }	
 	
 	
