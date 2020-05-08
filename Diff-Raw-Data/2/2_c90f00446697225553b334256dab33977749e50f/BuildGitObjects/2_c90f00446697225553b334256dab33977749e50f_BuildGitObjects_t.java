 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import org.pmw.tinylog.Logger;
 
 import edu.mit.csail.sdg.alloy4.Err;
 import edu.mit.csail.sdg.alloy4compiler.ast.Decl;
 import edu.mit.csail.sdg.alloy4compiler.ast.Expr;
 import edu.mit.csail.sdg.alloy4compiler.ast.ExprVar;
 import edu.mit.csail.sdg.alloy4compiler.ast.Module;
 import edu.mit.csail.sdg.alloy4compiler.ast.Sig;
 import edu.mit.csail.sdg.alloy4compiler.parser.CompUtil;
 import edu.mit.csail.sdg.alloy4compiler.translator.A4Solution;
 import edu.mit.csail.sdg.alloy4compiler.translator.A4Tuple;
 import edu.mit.csail.sdg.alloy4compiler.translator.A4TupleSet;
 
 public class BuildGitObjects {
 
 	private static File path;
 	
 	public static String exec(List<String> command, File path, String message, String input) throws GitException {
 		String result = null;
 		try {
 			ProcessBuilder pb = new ProcessBuilder(command);
 			Logger.trace(message + " on " + path.getPath() + "\n   with " + command);
 			pb.directory(path);
 			Map<String, String> env = pb.environment();
 			env.put("GIT_AUTHOR_DATE", "Wed Feb 16 14:00 2037 +0100");
 			env.put("GIT_COMMITTER_DATE", "Wed Feb 16 14:00 2037 +0100");
 
 			Process pr = pb.start();
 			if (input != null) {
 				BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(pr.getOutputStream()));
 				bw.flush();
 				bw.write(input);
 				bw.close();
 			}
 			pr.waitFor();
 			BufferedReader br = new BufferedReader(new InputStreamReader(pr.getInputStream()));
 			BufferedReader be = new BufferedReader(new InputStreamReader(pr.getErrorStream()));
 			if (br.ready()) {
 				String line = br.readLine();
 				StringBuilder lines = new StringBuilder();
 				while (line != null) {
 					lines.append(line);
 					line = br.readLine();
 					if (line != null) lines.append('\n');
 				}
 				result = lines.toString();				
 			}
 			br.close();
 			if (be.ready()) {
 				String line = be.readLine();
 				StringBuilder lines = new StringBuilder();
 				while (line != null) {
 					lines.append(line);
 					line = be.readLine();
 					if (line != null) lines.append('\n');
 				}
 				be.close();
 				Logger.error(lines.toString());
 				throw new GitException(lines.toString());
 			}
 		} catch (IOException exc) {
 			exc.printStackTrace();
 		} catch (InterruptedException exc) {
 			exc.printStackTrace();
 		}
 		return result;
 	}
 	
 	public static String exec(List<String> command, String message, String input) throws GitException {
 		return exec(command,path,message,input);
 	}
 
 	public static String exec(List<String> command, String message) throws GitException {
 		return exec(command,path,message,null);
 	}
 
 	public static String exec(List<String> command, File path, String message) throws GitException {
 		return exec(command,path,message,null);
 	}
 
 	public static void gitInit() {
 		ArrayList<String> cmds = new ArrayList<String>();
 	 	cmds.add(Utils.GIT_CMD);
 	 	cmds.add("init");
 		try {
 			exec(cmds,"Git init");
 		} catch (GitException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	public static String buildGitHashObject(String blob) {
 		ArrayList<String> cmds = new ArrayList<String>();
 	 	cmds.add(Utils.GIT_CMD);
 	 	cmds.add("hash-object");
 	 	cmds.add("-w");
 	 	cmds.add("--stdin");
 	 	String hash = null;
 		try {
 			hash = exec(cmds, "Creating blob", blob);
 		 	Logger.trace("Blob hash : " + hash);
 		} catch (GitException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return hash;
 	}
 
 	
 	public static String buildTreeEntry(String type, String hashcode,String ref) {
 		String mode;
 		if (type.equals("this/Blob")) {
 			mode = "100644";
 			type = "blob";
 		} else {
 			mode = "040000";
 			type = "tree";
 		}
 		String entry = mode + " " + type + " " + hashcode + "\t" + ref + "\n";
 		return entry;
 	}
 	
 	public static String buildGitTree(ArrayList<String> entrys) {	
 		StringBuilder tree = new StringBuilder();
 		for (String line : entrys) tree.append(line);
 		
 		ArrayList<String> cmds = new ArrayList<String>();
 	 	cmds.add(Utils.GIT_CMD);
 	 	cmds.add("mktree");
 	 	String hash = null;
 		try {
 			hash = exec(cmds,"Building tree",tree.toString());
 		 	Logger.trace("Tree hash : " + hash);
 		} catch (GitException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return hash;		
 	}
 	
 	public static String buildCommitTree(String tree_hashcode, String message, ArrayList<String> commits) {
 		ArrayList<String> cmds = new ArrayList<String>();	
 		if (commits.isEmpty()) {
 			cmds.add(Utils.GIT_CMD);
 			cmds.add("commit-tree");
 			cmds.add(tree_hashcode);
 		} else {
 			cmds.add(Utils.GIT_CMD);
 			cmds.add("commit-tree");
 			cmds.add(tree_hashcode);				 
 			for(String com : commits){
 				cmds.add("-p");
 				cmds.add(com);
 			}
 		}
 		String hash = null;
 		try {
 			hash = exec(cmds, "Building commit with tree "+tree_hashcode, message);
 			Logger.trace("Commit hash : "+ hash);
 		} catch (GitException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return hash;
 	}
 
 	public static String setHead(String path_name) {
 		ArrayList<String> cmds = new ArrayList<String>();
 	 	cmds.add(Utils.GIT_CMD);
 	 	cmds.add("symbolic-ref");
 	 	cmds.add("HEAD");
 	 	cmds.add(path_name);
 	 	try {
 			return exec(cmds,"Setting HEAD to "+path_name);
 		} catch (GitException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	public static String buildGitRef(String commit_hashcode, String path_name) {	
 		ArrayList<String> cmds = new ArrayList<String>();
 		cmds.add(Utils.GIT_CMD);
 		cmds.add("update-ref");
 		cmds.add(path_name);
 		cmds.add(commit_hashcode);
 		try {
 			exec(cmds,"Setting head " + path_name + " to " + commit_hashcode);
 		} catch (GitException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return path_name;
 	}
 
 	public static String buildGitIndexEntry(String object_hash, String file_name) {	
 		ArrayList<String> cmds = new ArrayList<String>();
 		cmds.add(Utils.GIT_CMD);
 		cmds.add("update-index");
 		cmds.add("--add");
 		cmds.add("--cacheinfo");
 		cmds.add("100644");
 		cmds.add(object_hash);
 		cmds.add(file_name);
 		try {
 			exec(cmds,"Building index entry with " + file_name + " pointing to " + object_hash);
 		} catch (GitException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return file_name;	
 	}
 	
 	public static void buildIndex(A4Solution sol, Module world, HashMap<String,String> mapObjsHash,HashMap<String,ExprVar>mapAtom, Expr state) throws Err {
 		Expr files = CompUtil.parseOneExpression_fromString(world, "index").join(state);
 		Expr parent =  CompUtil.parseOneExpression_fromString(world,"Node <: parent");
 		Expr content = CompUtil.parseOneExpression_fromString(world, "File <: content");
 		
 		A4TupleSet ts =  (A4TupleSet) sol.eval(files);
 		String path;
 		for (A4Tuple t : ts) {
 			String a = t.atom(0);
 			path = buildPath(sol,world,parent,mapAtom.get(a), mapAtom);
 			String blobStr = null;
 			for (A4Tuple blob: (A4TupleSet)sol.eval(mapAtom.get(a).join(content))){
 				blobStr = blob.atom(0);
 			}
 			Logger.trace("Res map   :" +mapObjsHash.get(blobStr));
 			Logger.trace("Res path  :" +path);
 			Logger.trace("Index res : "+ buildGitIndexEntry(mapObjsHash.get(blobStr),path));
 		}
 	}
 	
 	/**
 	 * 
 	 * @param sol
 	 * @param parent: Expr representing relation of type Path -> Path
 	 * @param name: Expr representing relation of type Path -> Path
 	 * @param current: Expr representing relation of type Path
 	 * @param mapAtom
 	 * @return
 	 * @throws Err
 	 */
 	private static String buildPath(A4Solution sol, Module world, Expr parent, ExprVar current, HashMap<String, ExprVar> mapAtom) throws Err {
 		Expr name = CompUtil.parseOneExpression_fromString(world, "Node <: name");
 
 		A4TupleSet ts = (A4TupleSet) sol.eval(current.join(name));
 		String it = ts.iterator().next().atom(0).replace("$", "_");
 	
 		A4TupleSet tsp = (A4TupleSet) sol.eval(current.join(parent));
 		if(tsp.size() == 0)
 			return null;
 		else {
 			String res = buildPath(sol,world,parent,mapAtom.get(tsp.iterator().next().atom(0)),mapAtom);
 			if (res == null)
 				return it; 
 			else 
 				return res + "/" + it;
 		}
 	}
 
 	public static String buildBranch(A4Solution sol, ExprVar branch) throws Err {
 		A4TupleSet ts = (A4TupleSet) sol.eval(branch);
 		A4Tuple t = ts.iterator().next();
 		return t.atom(0).replace("$","_");
 	}	
 	
 	public static String buildType(Module world, HashMap<String,String> vars,String cmd,A4Solution sol,HashMap<String,ExprVar> mapAtom,Expr parent,ExprVar path) throws Err, GitException {		
 		String type = null;
 		if (vars.get(cmd).equals("path")) {
 			type = buildPath(sol,world, parent, path,mapAtom);
                         if (type == null) type = "."; // hack - buildPath must be changed
 		} else if (vars.get(cmd).equals("branch")) {
 			type = buildBranch(sol,path);
 		} else {			
 			type = buildPath(sol,world,parent, path,mapAtom);
                         if (type == null) type = "."; // idem
 		}
 		return type;
 	}
 	
 	public static void runCmd(A4Solution sol, Module world, String p, ExprVar path, HashMap<String, ExprVar> mapAtom, String cmd, ArrayList<String> options, HashMap<String,String> vars) throws GitException, Err { 
 				
 		Expr parent =  CompUtil.parseOneExpression_fromString(world," Node <: parent");
 		ArrayList<String> n_cmds = new ArrayList<String>();
 		
 		n_cmds.add(Utils.GIT_CMD);
 		n_cmds.add(cmd);
 				
 		for (String n_cmd : options) {
 			if(n_cmd.matches("#[a-zA-Z0-9]*")){
 				n_cmds.add(buildType(world,vars,n_cmd,sol,mapAtom,parent,path));
 			}else n_cmds.add(n_cmd);
 		};	
 	    
 		try {	
 			String result = exec(n_cmds,new File(p),"Running "+n_cmds);
 			Logger.trace("Output: " + result);
 		} catch (GitException e) {
 			throw new GitException("Result from "+ n_cmds+" on path "+p+":\n\n"+e.getMessage() );
 		}	
 	}
 
 	
 	
 	public static void buildRefs(A4Solution sol,Module world, ExprVar iState, HashMap<String,ExprVar>mapAtom, HashMap<String,String> mapObjHash) throws Err {
 		Expr refs = CompUtil.parseOneExpression_fromString(world, "ref").join(iState);
 		
 		
 		A4TupleSet ts = (A4TupleSet) sol.eval(refs);
 		for(A4Tuple t : ts) {
 			buildGitRef(mapObjHash.get(t.atom(0)),"refs/heads/"+t.atom(0).replace("$", "_"));
 		}
 	}
 	
 	public static void treeBuilder(A4Solution sol,Module world,HashMap<String,ExprVar>mapAtom,HashMap<String,String> mapObjsHash, ExprVar iState) throws Err {
 		Expr domain = CompUtil.parseOneExpression_fromString(world, "stored").join(iState);
 		Expr content = CompUtil.parseOneExpression_fromString(world, "Tree <: content");
 		Expr Tree = CompUtil.parseOneExpression_fromString(world, "Tree").domain(domain);
 	//	Expr parent =  CompUtil.parseOneExpression_fromString(world," Path <: parent");
 	//	Expr name =  CompUtil.parseOneExpression_fromString(world," Path <: iden");
 		
 		LinkedList<ExprVar> aux = new LinkedList<ExprVar>();
 		aux.add(ExprVar.make(null, "t",Tree.type()));
 		Expr previousTrees = CompUtil.parseOneExpression_fromString(world, "none :> Tree");
 		Decl tDecl = new Decl(null,null,null,aux,Tree);
 		Expr treeExpr = Sig.UNIV.join(tDecl.get().join(content)).range(Tree).in(previousTrees); 
 		treeExpr = treeExpr.comprehensionOver(tDecl);
 				
 		A4TupleSet trees = (A4TupleSet) sol.eval(treeExpr);
 		while (trees.size()>0) { 
 			buildTrees(sol,trees,content,mapAtom,mapObjsHash);
 			for (A4Tuple t : trees) {
 				previousTrees = previousTrees.plus(mapAtom.get(t.atom(0)));
 				tDecl = new Decl(null,null,null,aux,Tree.minus(previousTrees)); 
 				treeExpr = Sig.UNIV.join(tDecl.get().join(content)).range(Tree).in(previousTrees); 
 				treeExpr = treeExpr.comprehensionOver(tDecl);
 			}
 			
 			trees = (A4TupleSet) sol.eval(treeExpr);
 		}
 	}
 	
 	
 	public static void commitBuilder(A4Solution sol,Module world,HashMap<String,ExprVar> mapAtom, HashMap<String,String> mapObjsHash,ExprVar iState) throws Err {
 		Sig Commit = Utils.getEFromIterable(world.getAllSigs(), "this/Commit");
 		Expr object = CompUtil.parseOneExpression_fromString(world,"stored");
 		Expr domain = object.join(iState);
 		Expr previousCommits = CompUtil.parseOneExpression_fromString(world, "none :> Commit");
 		Expr previous = CompUtil.parseOneExpression_fromString(world, "previous").range(domain);
 		Expr c = Commit.decl.get();
 		// forall c:Commit :: c in object.State and c.previous in 'previousCommits' and c not in 'previousComits'
 		Expr currentCommits = c.in(domain).and(c.join(previous).in(previousCommits).and(c.in(previousCommits).not()));
 		currentCommits = currentCommits.comprehensionOver(Commit.decl);
 		
 		A4TupleSet commits = (A4TupleSet) sol.eval(currentCommits);
 		while (commits.size() >0) {
 			//Logger.trace(commits.size());
 			buildCommits(sol,commits,previous,CompUtil.parseOneExpression_fromString(world, "tree").range(domain),mapAtom,mapObjsHash);
 			for (A4Tuple tc : commits) {
 				previousCommits = previousCommits.plus(mapAtom.get(tc.atom(0)));
 				currentCommits = c.in(domain).and(c.join(previous).in(previousCommits).and(c.in(previousCommits).not()));
 				currentCommits = currentCommits.comprehensionOver(Commit.decl);
 			}
 			commits =  (A4TupleSet) sol.eval(currentCommits);
 		}
 	}
 	
 	public static void placeHEAD(A4Solution sol,Module world,Expr iState) throws Err {
		Expr HEAD = CompUtil.parseOneExpression_fromString(world, "HEAD");
 		A4TupleSet res = (A4TupleSet) sol.eval(HEAD.join(iState));
         if (res.size() > 0) {
         	A4Tuple tup = res.iterator().next();
         	Logger.trace(setHead("refs/heads/" + tup.atom(0).replace("$", "_")));
         } else {
         	Logger.trace(setHead("refs/heads/master"));
         }		
 	}
 	
 	
 	public static void buildCommits(A4Solution sol, A4TupleSet commits,Expr previous,Expr tree,HashMap<String,ExprVar> mapAtom,HashMap<String,String> mapObjsHash) throws Err {
 		ArrayList<String> entries;
 		ExprVar commit;
 		A4TupleSet prevCommits;
 		A4Tuple commitTree;
 		String treeHash;
 		
 		for (A4Tuple t : commits) {
 			entries  = new ArrayList<String>();
 			commit = mapAtom.get(t.atom(0));
 			//Logger.trace(t.atom(0));
 			commitTree = ((A4TupleSet)sol.eval(commit.join(tree))).iterator().next();
 			//Logger.trace(commitTree.atom(0));
 			prevCommits = (A4TupleSet) sol.eval(commit.join(previous));
 			
 			for (A4Tuple prev: prevCommits){
 				// TODO: Not sure this is safe 
 				// Don't add duplicate commit 
 				String h = mapObjsHash.get(prev.atom(0));
 				if (!entries.contains(h)) entries.add(h);
 			}
 			treeHash = mapObjsHash.get(commitTree.atom(0));
 			//Logger.trace(treeHash);
 			
 			mapObjsHash.put(t.atom(0),buildCommitTree(treeHash,"message\n",entries));
 		}
 	}
 
 	public static void buildTrees(A4Solution sol, 
 			//Expr parent, Expr name,
 			A4TupleSet trees, Expr content, HashMap<String, ExprVar> mapAtoms,
 			HashMap<String, String> mapObjsHash) throws Err {
 		ArrayList<String> entries;
 		ExprVar tree;
 		A4TupleSet lines;
 		for (A4Tuple t : trees) {
 			entries = new ArrayList<String>();
 			tree = mapAtoms.get(t.atom(0));
 			lines = (A4TupleSet) sol.eval(tree.join(content));
 			for (A4Tuple line : lines) {
 				// path =
 				// buildPath(sol,parent,name,mapAtoms.get(line.atom(0)),mapAtoms);
 				entries.add(buildTreeEntry(line.sig(1).toString(),
 						mapObjsHash.get(line.atom(1)),
 						line.atom(0).replace("$", "_")));
 			}
 			mapObjsHash.put(t.atom(0), buildGitTree(entries));
 		}
 	}
 
 	public static void buildObjects(A4Solution sol, Module world, String index,
 			ExprVar iState, HashMap<String, ExprVar> mapAtom) throws Err {
 
 		HashMap<String, String> mapObjsHash = new HashMap<String, String>();
 		path = new File("output/" + index);
 
 		Expr domain = CompUtil.parseOneExpression_fromString(world, "stored")
 				.join(iState);
 		Expr blobs = CompUtil.parseOneExpression_fromString(world, "Blob")
 				.domain(domain);
 
 		A4TupleSet ts = (A4TupleSet) sol.eval(blobs);
 
 		gitInit();
 
 		for (A4Tuple t : ts)
 			mapObjsHash.put(t.atom(0), buildGitHashObject(t.atom(0)));
 
 		treeBuilder(sol, world, mapAtom, mapObjsHash, iState);
 
 		commitBuilder(sol, world, mapAtom, mapObjsHash, iState);
 
 		buildIndex(sol, world, mapObjsHash, mapAtom, iState);
 
 		buildRefs(sol, world, iState, mapAtom, mapObjsHash);
 
 		placeHEAD(sol, world, iState);
 	}
 
 }
