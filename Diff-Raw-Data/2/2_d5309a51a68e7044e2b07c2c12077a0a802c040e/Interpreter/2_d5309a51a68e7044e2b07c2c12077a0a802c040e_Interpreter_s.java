 package com.dustyneuron.bitprivacy;
 
 
 
 import java.util.Enumeration;
 import java.util.logging.Level;
 import java.util.logging.LogManager;
 import java.util.logging.Logger;
 
 import com.dustyneuron.bitprivacy.bitcoin.WalletMgr;
 import com.dustyneuron.bitprivacy.exchanger.MixClient;
 import com.dustyneuron.bitprivacy.exchanger.MixServer;
 
 
 import asg.cliche.Command;
 import asg.cliche.Shell;
 import asg.cliche.ShellDependent;
 import asg.cliche.ShellFactory;
 
 public class Interpreter implements ShellDependent {
 	
 	WalletMgr walletMgr;
 	MixServer mixServer;
 	MixClient mixClient;
 	
 
 	@Command
 	public void wallet() throws Exception {
 		ShellFactory.createSubshell("wallet", theShell, "Wallet Shell", walletMgr).commandLoop();
 	}
 	
 	@Command
 	public void server() throws Exception {
 		ShellFactory.createSubshell("server", theShell, "Mix Server Shell", mixServer).commandLoop();
 	}
 
 	@Command
 	public void client() throws Exception {
 		ShellFactory.createSubshell("client", theShell, "Mix Client Shell", mixClient).commandLoop();
 	}
 	
 	public Interpreter(String file) throws Exception {
         walletMgr = new WalletMgr(file);
         mixServer = new MixServer(walletMgr.getWallet(), walletMgr);
        mixClient = new MixClient(walletMgr.getWallet());
         
 		ShellFactory.createConsoleShell("bitprivacy", "bitprivacy Shell", this).commandLoop();
 	}
 	
 	public static void main(String[] args) throws Exception {
         LogManager logManager = LogManager.getLogManager();
         Enumeration<String> loggerNames = logManager.getLoggerNames();
         while (loggerNames.hasMoreElements()) {
                 Logger logger = logManager.getLogger(loggerNames.nextElement());
             logger.setLevel(Level.WARNING);
         }
         
         String walletFile = "wallet";
         if (args.length > 0) {
         	walletFile = args[0];
         }
 
         //System.out.println("hello world");    
         //SimpleMix.createSchema();
         new Interpreter(walletFile);
 	}
 	
     private Shell theShell;
 
     public void cliSetShell(Shell theShell) {
         this.theShell = theShell;
     }
 }
