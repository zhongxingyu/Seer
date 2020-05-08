 package org.andrew.nlp.parser;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Arrays;
 
 import opennlp.tools.sentdetect.SentenceDetectorME;
 import opennlp.tools.sentdetect.SentenceModel;
 import opennlp.tools.util.InvalidFormatException;
 
public class SentenceParserAdapter
 {
 
 	SentenceModel model;
 
 	public SentenceParserAdapter(String filename)
 	{
 		InputStream modelIn = null;
 		try
 		{
 			modelIn = new FileInputStream(filename);
 			model = new SentenceModel(modelIn);
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
 			SentenceDetectorME sentenceDetector = new SentenceDetectorME(model);
 			String[] sentences = sentenceDetector.sentDetect(text);
 			// TODO: Replace with logger
 			System.out.println(Arrays.asList(sentences));
 			return sentences;
 		}
 		// TODO: Replace with logger
 		throw new ModelNotLoadedException();
 	}
 
 }
