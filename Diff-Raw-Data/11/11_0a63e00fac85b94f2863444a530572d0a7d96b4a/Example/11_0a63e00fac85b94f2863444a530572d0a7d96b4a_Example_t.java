 package ca.gc.agr.itis.ITISProxy.example;
 
 import ca.gc.agr.itis.ITISProxy.Proxy;
 import ca.gc.agr.itis.ITISProxy.MockUtil;
 import ca.gc.agr.itis.ITISProxy.Util;
 import ca.gc.agr.itis.ITISModel.*;
 import java.util.List;
 import java.util.ArrayList;
 
 
 public final class Example{
 
 	static final String[] taxaTsns = {MockUtil.fungiTsn, MockUtil.ascomycotaTsn, MockUtil.pezizomycotinaTsn, 
 	                             MockUtil.lecanoromycetesTsn, MockUtil.lecanoralesTsn, MockUtil.teloschistineaeTsn, 
 	                             MockUtil.teloschistaceaeTsn, MockUtil.caloplacaTsn, MockUtil.caloplacaAlbovariegataTsn};
 
 	public static void main(String [ ] args){
 		printKingdoms();
 
 		for(String tsn: taxaTsns){
 			printTaxon(tsn);
 		}
 	}
 	
 
 
 	static final void printKingdoms(){
 		start("Proxy.getKingdoms");
 		try{
 			List<ItisRecord>kingdoms = Proxy.getKingdoms();
 			for(ItisRecord kir: kingdoms){
 				Util.printRecord(kir);
 			}
 		}
 		catch(Exception e){
 			e.printStackTrace();
 		}
 		end("Proxy.getKingdoms");
 	}
 
     
 	static final void printTaxon(String tsn){
 		try{
 			start("Proxy: " + tsn);
 			ItisRecord ir = Proxy.getByTSN(tsn);
 			if(ir != null){
 				Util.printRecord(ir);
 				end("Proxy: " + tsn);
 			}
 
 		}
 		catch(Exception e){
 			e.printStackTrace();
 		}
 	}
     
 	static final String br = "-----------------------------------------------------------------";
 	static void end(String s){
 		System.out.println("END " + s + br);
 		//System.out.println(br);
 	}
 
 	static void start(String s){
 		System.out.println("\n\nSTART " + s + br);
 	}
 
 
 
 }
