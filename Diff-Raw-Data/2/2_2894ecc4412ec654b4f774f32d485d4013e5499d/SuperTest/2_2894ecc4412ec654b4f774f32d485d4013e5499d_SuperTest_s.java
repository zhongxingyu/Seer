 package com.cs3240.parsergenerator.Domain;
 
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Scanner;
 
 import org.junit.Test;
 
 import com.cs3240.parsergenerator.GrammarFileParser;
 import com.cs3240.parsergenerator.LexicalClass;
 import com.cs3240.parsergenerator.exceptions.InvalidSyntaxException;
 import com.cs3240.parsergenerator.utils.Driver;
 import com.cs3240.parsergenerator.utils.ParseTableGenerator;
 
 public class SuperTest {
 
     DateFormat df = new SimpleDateFormat("yyyy-MM-dd-Hmmss");
 
    @Test(expected=InvalidSyntaxException.class)
 	public void testEverything2() throws IOException, InvalidSyntaxException {
 		Grammar grammar = GrammarFileParser.parse("TinyGrammar.txt");
 		grammar.removeLeftRecursion();
 		System.out.println(grammar);
 		ParseTable pt = ParseTableGenerator.generateTable(grammar);
 		Driver.outputTableToFile(pt, "tiny-output-" + df.format(new Date()) + ".txt");
 		
 		Scanner scan = new Scanner(new BufferedReader(new FileReader("sample.txt")));   
     	StringBuffer output = new StringBuffer();
     	
     	while (scan.hasNextLine()) {
     		String nextLine = scan.nextLine();
     		Scanner lineScan = new Scanner(nextLine);
     		while (lineScan.hasNext()) {
     			String token = lineScan.next();
     			if (token.contains(";")) {
     				String nonSemi = token.substring(0, token.length()-1);
     				String semi = token.substring(token.length()-1);
     				output.append(LexicalClass.parseToken(nonSemi).toString() + " ");
     				output.append(LexicalClass.parseToken(semi).toString() + " ");
     			} else {
     				output.append(LexicalClass.parseToken(token).toString() + " ");
     			}
 			}
     	}
     	System.out.println(output.toString());
     	assertTrue(Driver.parse(pt, output.toString()));
 	}
   
     public void testEverything1() throws IOException, InvalidSyntaxException {
 
         Grammar grammar = GrammarFileParser.parse(new File("GrammarSample.txt"));
         grammar.removeLeftRecursion();
         ParseTable pt = ParseTableGenerator.generateTable(grammar);
         Driver.outputTableToFile(pt, "output-" + df.format(new Date()) + ".txt");
 
         assertTrue(Driver.parse(pt, "LEFTPAR NUMBER ID NUMBER RIGHTPAR"));
         assertFalse(Driver.parse(pt, "NUMBER ID NUMBER RIGHTPAR"));
         assertFalse(Driver.parse(pt, "LEFTPAR NUMBER ID NUMBER"));
         assertFalse(Driver.parse(pt, "LEFTPAR NUMBER NUMBER LEFTPAR NUMBER NUMBER NUMBER RIGHTPAR NUMBER RIGHTPAR LEFTPAR NUMBER RIGHTPAR"));
     }
 
 }
