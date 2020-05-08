 package org.andrew.nlp.parser;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Arrays;
 
 import opennlp.tools.tokenize.Tokenizer;
 import opennlp.tools.tokenize.TokenizerME;
 import opennlp.tools.tokenize.TokenizerModel;
 import opennlp.tools.util.InvalidFormatException;
 
public class TokenizerParserAdapter
 {
 
 	TokenizerModel model;
 
 	public TokenizerParserAdapter(String filename)
 	{
 		InputStream modelIn = null;
 		try
 		{
 			modelIn = new FileInputStream(filename);
 			model = new TokenizerModel(modelIn);
 		}
 		catch (FileNotFoundException e)
 		{
 			throw new ModelNotLoadedException();
 		}
 		catch (InvalidFormatException e)
 		{
 			throw new ModelNotLoadedException();
 		}
 		catch (IOException e)
 		{
 			throw new ModelNotLoadedException();
 		}
 		finally
 		{
 			if (modelIn != null)
 			{
 				try
 				{
 					modelIn.close();
 				}
 				catch (IOException e)
 				{
 					throw new ModelNotLoadedException();
 				}
 			}
 		}
 	}
 
 	public String[] parse(String text)
 	{
 		if (model != null)
 		{
 			Tokenizer tokenizer = new TokenizerME(model);
 			String tokens[] = tokenizer.tokenize(text);
 			// TODO: Replace with logger
 			System.out.println(Arrays.asList(tokens));
 			return tokens;
 		}
 		// TODO: Replace with logger
 		throw new ModelNotLoadedException();
 
 	}
 
 }
