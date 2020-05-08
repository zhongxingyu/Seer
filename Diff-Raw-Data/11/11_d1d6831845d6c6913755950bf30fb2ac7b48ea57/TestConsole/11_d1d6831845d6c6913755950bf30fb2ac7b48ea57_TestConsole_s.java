 package mula.console;
 
 import java.io.Console;
 import java.io.StringReader;
 import java.util.List;
 import java.util.Map;
 
 import mula.parser.MulaParser;
 import mula.parser.ParseException;
 import mula.parser.TokenMgrError;
 import mula.structure.*;
 
 public class TestConsole {
 
 	/**
 	 * @param args
 	 * @throws ParseException 
 	 */
 	public static void main(String[] args) throws ParseException {
         Console console = System.console();
     	console.printf("Mula Preview on Java, (version 0.1)\n\n");
         while(true)
         {
         	try {
         		console.printf("mula-preview >>> ");
         		String text = System.console().readLine();
         		MulaParser parser = new MulaParser(new StringReader(text));
         		MulaSubstance[] substances = parser.article();
         		if(!(substances.length == 1 && substances[0] instanceof MulaList && ((MulaList)substances[0]).get(0).equals(MulaAtom.get("match")) && 
         				((MulaList)substances[0]).get(1) instanceof MulaList && ((MulaList)substances[0]).get(2) instanceof MulaList)) {
         			throw new Exception("Unexpected list, using \"(match [scheme] [source])\" to test MulaScheme.");
         		}
         		
        		MulaList scheme = (MulaList)substances[1];
        		MulaList source = (MulaList)substances[2];
         		
         		MulaScheme mulaScheme = new MulaScheme(scheme);
         		Map<MulaAtom, List<MulaSubstance>> variables = mulaScheme.match(source);
         		
         		if(variables == null) {
         			throw new Exception("Source doesn't match scheme.");
         		}
         		
         		console.printf("Variables: \n");
         		for(MulaAtom name: variables.keySet()) {
         			for(MulaSubstance substance: variables.get(name)) {
         				console.printf(name.toString() + " -> " + substance.toString());
         			}
         		}
         	} catch(Exception e) {
        		console.printf(e.toString());
         	}
         }
 	}
 
 }
