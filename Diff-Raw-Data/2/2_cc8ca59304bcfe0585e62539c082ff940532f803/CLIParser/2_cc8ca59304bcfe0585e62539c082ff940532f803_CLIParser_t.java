 package server;
 
 class CLIParser {
 
 	public String getConfigFilePath() {
 		return this.configFilePath;
 	}
 
 	public boolean loadTextGraph() {
		return !this.readFromDumpedGraph;
 	}
 
 	public boolean readFromDumpedGraph() {
 		return this.dumpgraph;
 	}
 
     public boolean dumpgraph() {
         return dumpgraph;
     }
 
     private String configFilePath = null;
 	private boolean readFromDumpedGraph = false;
     private boolean dumpgraph = false;
 
 	public CLIParser(String[] args) {
 		for (int i = 0; i < args.length; i++) {
 			if (args[i].equals("-c") && ((i + 1) < args.length) && !(args[i+1].startsWith("-"))) {
 				configFilePath = args[i + 1];
 			} else if (args[i].equals("-f") && ((i + 1) < args.length) &&
                        args[i + 1].equals("dump")) {
                 readFromDumpedGraph = true;
             } else if (args[i].equals("dumpgraph")) {
 				dumpgraph = true;
 			}
 		}
 	}
 }
