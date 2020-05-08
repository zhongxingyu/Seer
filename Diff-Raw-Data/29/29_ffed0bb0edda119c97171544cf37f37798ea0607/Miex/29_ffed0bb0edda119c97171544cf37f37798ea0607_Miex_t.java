 /*
  *	MIEX - Metadata and Information Extractor from small XML documents.
  *	Copyright (C) 2007 - Nacho Barrientos Arias
  *
  *	This program is free software; you can redistribute it and/or modify
  *	it under the terms of the GNU General Public License as published by
  *	the Free Software Foundation; either version 2 of the License.
  * 
  *	This program is distributed in the hope that it will be useful,
  *	but WITHOUT ANY WARRANTY; without even the implied warranty of
  *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *	GNU General Public License for more details.
  *
  *	You should have received a copy of the GNU General Public License along
  *	with this program; if not, write to the Free Software Foundation, Inc.,
  *	51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
  */
 
 package es.uniovi.aic.miex.run;
 
 /* JDK */
 import java.io.*;
 import java.lang.System;
 
 import java.util.Iterator;
 import java.util.List;
 import java.util.ArrayList;
 
 /* Stanford */
 import edu.stanford.nlp.process.DocumentPreprocessor;
 import edu.stanford.nlp.ling.HasWord;
 import edu.stanford.nlp.trees.TypedDependency;
 
 /* Miex */
 import es.uniovi.aic.miex.input.XMLValidator;
 import es.uniovi.aic.miex.input.FieldsParser;
 
 import es.uniovi.aic.miex.config.*;
 
 import es.uniovi.aic.miex.datastr.*;
 import es.uniovi.aic.miex.semantic.*;
 
 import es.uniovi.aic.miex.filter.GlobalFilter;
 
 import es.uniovi.aic.miex.sql.SQLHandler;
 
 public class Miex 
 {
     public static void main(String[] args)
 		{
 
 				/* STEP 1: Parsing CMDLine arguments */
 
 				ConfigCMDLine theCMDConfig = new ConfigCMDLine(args);
 
 				System.out.println("Cmdline syntax is OK, continuing...\n");
 
 				/* STEP 2: Reading configuration file */
 
 				ConfigFile theFileConfig = new ConfigFile(theCMDConfig.getConfigFileURI());
 
 				System.out.println("Config file is OK, continuing...\n");
 
 				/* STEP 3: Creating database structure (if required) */
 
 				SQLHandler sql = new SQLHandler(theFileConfig);
 
 				if(theFileConfig.getBooleanSetting("CreateDB"))
 				{
 					stopUntilUserPressesEnter("WARNING: Existing database will be completely deleted. [Y/N]: ");
 					System.out.println("Creating DB...\n");
 					sql.executeSQLFile(theFileConfig.getStringSetting("SQLSkeleton"));
 				}
 
 				/* STEP 4: Validating input XML files (if required) */
 
 				if(theFileConfig.getBooleanSetting("Validate"))
 				{
 					if(!validateXMLInputFiles(theCMDConfig.getFiles(), theFileConfig))
 					{
 						System.err.println("ERROR: Internal error validating XML input files");
 						System.exit(-1);
 					}
 
 					System.out.println("Input file's XML syntax is sane (matches XML Schema), parsing files...\n");
 				}
 				else
 					System.out.println("Warning: continuing without validate XML input...\n");
 
 				/* STEP 5: Getting metadata from input files */
 
 				processFiles(theCMDConfig.getFiles(), theFileConfig, sql);
 				
     }
 
 	/*
    * Begin of a set of auxiliar methods called from main
 	 */
 
 	private static boolean validateXMLInputFiles(String[] files, ConfigFile config)
 	{
 		XMLValidator validator = new XMLValidator(config.getStringSetting("XMLschemaURI"));
 
 		for(int j=0; j < files.length; j++)
 		{
 			File theFile = new File(files[j]);
 
 			if(!theFile.canRead())
 			{
 				System.err.println("Input file " + files[j] + " is not readable or not exists, terminating.");
 				return false;
 			}
 
 			if(!(validator.validate(theFile)))
 			{
 				System.err.println("Input file's XML " + files[j] + " does not match this Schema.");
 				return false;
 			 }
 		}
 
 		return true;
 	}
 
 	/* Before this function is called validation using Schema has been completed 
 		 then we only need to process the files with the SP */
 
 	private static void processFiles(String[] files, ConfigFile config, SQLHandler sql)
 	{
		Extractor ex = new Extractor(config.getStringSetting("GrammarURI"));
 
 		GlobalFilter filter = new GlobalFilter();
 
 		FieldsParser unMarshaller = new FieldsParser();
 
 		int collectionID;
 		
 		// Iterating through all collections
 		for(int j=0; j < files.length; j++)
 		{
 			File theFile = new File(files[j]);
 
 			// Registering collection into the database
 			collectionID = sql.getNewCollectionID((theFile.getName().split("\\."))[0]);
 
 			if(collectionID < 0)
 			{
 				System.out.println("Collection already parsed");
 				continue;
 			}
 
 			System.out.println("Parsing file " + files[j] + "\n");
 
 			// Dumping XML to memory
 			MyCollection collection = unMarshaller.run(theFile);
 
 			// Checkin' if the extractor is ready (loadin' needed only once)	
 			if(!ex.isLoaded()) ex.load();
 
 			int docID = 1;
 
 			// Iterating through all documents
 			for(Iterator it = collection.getDocsIterator(); it.hasNext(); )
 				// it.next() is a MyDoc
 			{
 				MyDoc doc = (MyDoc)it.next();
 
 				// Registering document into the database
 				sql.addDocument(docID, collectionID, doc.getTitle(), doc.isTrain());
 
 				// And its categories
 				sql.addCategories(docID, collectionID, doc.getCategories());
 
 				// Splitting title in sentences
 				List<List<? extends HasWord>> titleSentences = chopSentences(doc.getTitle());
 
 				// Splitting body in sentences
 				List<List<? extends HasWord>> bodySentences = chopSentences(doc.getBody());
 
 				// Dumping to category files (if required)
 				if(config.getBooleanSetting("Dump"))
 				{
 					// Creating data files
 					System.out.println("\tCreating category files for document titled: " + doc.getTitle());
 					injectDataToFiles(doc.getCategories().toArray(), bodySentences, config.getStringSetting("DumpDir"));
 				}
 
 				// Really processing document title					
 				System.out.println("\n\tProcessing TITLE of the document titled: " + doc.getTitle().trim());		
 				processSentences(titleSentences, config, ex, filter, sql, docID, collectionID, true);
 																																							// ^ (isFromTitle)
 
 				// Really processing document body
 				System.out.println("\n\tProcessing BODY of the document titled: " + doc.getTitle().trim());
 				processSentences(bodySentences, config, ex, filter, sql, docID, collectionID, false);
 																																							// ^ (isFromTitle)
 	
 				// Next document
 				docID++;
 
 			}
 
 		}
 
 	}
 
 	// Picks up a chunk of text and splits it in sentences.
 	private static List<List<? extends HasWord>> chopSentences(String text)
 	{
 		DocumentPreprocessor documentPreprocessor = new DocumentPreprocessor();
 
 		List<List<? extends HasWord>> sentences = null;
 
 		StringReader theReader = new StringReader(text);
 
 		sentences = documentPreprocessor.getSentencesFromText(theReader, null, null, -1);
 
 		return sentences;	
 	}
 
 	// For each $category injects document's sentences to $category.txt
 	private static void injectDataToFiles(ArrayList<String> categories, 
 																				List<List<? extends HasWord>> sentences,
 																				String destination)
 	{
 			String newline = System.getProperty( "line.separator" );
 	
 			for(int i=0; i < categories.size(); i++)
 			{
 				try
 				{
 					String catName = categories.get(i).toString().replace(" ","");
 
 					System.out.println("\tCreating file " + destination + catName + "...");
 
 					FileWriter fw = new FileWriter(destination + catName + ".txt",true);
 
 					System.out.println("\tInjecting the sentences...");
 
 					for (List sentence : sentences)
 					{
 						fw.write(sentence.toString());
 						fw.write(newline);
 					}
 
 					fw.close();
 
 					System.out.println("\tDone");
 
 				}
 				catch(IOException e)
 				{
           System.err.println(e.toString());
           System.exit(-1);
 				}
 	
 			}
 	}
 
 	private static void 
 	processSentences (List<List<? extends HasWord>> sentences, ConfigFile config, Extractor ex, GlobalFilter filter, 
 										SQLHandler sql, int docNumber, int colNumber, boolean isFromTitle)
 	{
 			int sentencesNum = 1;
 		
 			for (List sentence : sentences)
 			{
 				try
 				{
 					//System.out.println("\t\tProcessing sentence #" + sentencesNum + " " + sentence.toString().trim());
 					System.out.println("\t\tProcessing sentence #" + sentencesNum);
 
 					/* ************************ */
 					/* Dependencies among words */
 					/* ************************ */
 
 					System.out.print("\t\t\tDependencies... [");
 	
 					// Stanford, let me know the dependencies! :)
 					ArrayList<TypedDependency> deps = ex.getDependencies(sentence);
 
 					// Removing useless stuff
 					deps = filter.cleanDependencies(deps);
 					System.out.print(" F ");
 
 					// Injecting the results into the database
 					sql.addDependencies(docNumber,colNumber,deps,isFromTitle,false);
 																																// ^ (normalized)
 					System.out.print(" S ");
 
           if(config.getBooleanSetting("Normalize"))
           {
             // Getting normalized pairs
             deps = filter.normalizeDependencies(deps);
             System.out.print(" N ");
 
             // Injecting the normalized results into the database
             sql.addDependencies(docNumber,colNumber,deps,isFromTitle,true);
                                                                   // ^ (normalized)
             System.out.print(" S ");
           }
 
 					System.out.print("]\n");
 
 					/* ********************** */
 					/* Grammatical categories */
 					/* ********************** */
 
 					System.out.print("\t\t\tProperties...  [");
 
 					// Stanford, let me know the properties! :)
 					ArrayList<ExtendedTaggedWord> props = ex.getProperties(sentence);
 
 					// Removing useless stuff
 					props = filter.cleanProperties(props);
 					System.out.print(" F ");
 
 				 	// Injecting the results into the database	
 					sql.addWordsAndTags(docNumber,colNumber,props,isFromTitle,false);
 																																	// ^ (normalized)
 					System.out.print(" S ");
 
 					if(config.getBooleanSetting("Normalize"))
 					{
 						// Getting normalized pairs
 						props = filter.normalizeProperties(props);
 						System.out.print(" N ");
 
 						// Injecting the normalized results into the database
 						sql.addWordsAndTags(docNumber,colNumber,props,isFromTitle,true);
 																																	// ^ (normalized)
 						System.out.print(" S ");
 					}
 
 					System.out.print("]\n");
 
 					// Next sentence
 					sentencesNum++;
 				}
 				catch(Exception e)
 				{
 					e.printStackTrace();
 					System.err.println(e.toString());
 					System.exit(-1);
 				}
 			}		
 
 	}
 
 	private static void stopUntilUserPressesEnter(String msg)	
 	{
 		BufferedReader userIn = new BufferedReader(new InputStreamReader(System.in));
 
 		try
 		{
 			System.out.print(msg);
 
 			String line = userIn.readLine();
 
 			boolean out = false;
 
 			while(!out)
 			{
 				if(line.equals("Y") || line.equals("y"))
 				{
 					System.out.print("\n");
 					out = true;
 				}
 				else
 					if(line.equals("N") || line.equals("n"))
 						System.exit(0);
 					else
 					{
 						System.out.print("\n" + msg);
 						line = userIn.readLine();
 					}
 			}
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 
 		return;
 	}
 
 }
