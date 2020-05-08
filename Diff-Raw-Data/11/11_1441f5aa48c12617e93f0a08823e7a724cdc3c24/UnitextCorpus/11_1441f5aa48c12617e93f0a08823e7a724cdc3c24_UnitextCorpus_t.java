 /*
  * Copyright (C) 2010 Minwoo Jeong (minwoo.j@gmail.com).
  * This file is part of the "bitextOpenIE" distribution.
  * http://github.com/minwoo/bitextOpenIE/
  * This software is provided under the terms of LGPL.
  */
 
 package openie.text;
 
 import java.io.*;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 
 public class UnitextCorpus {
 
 	protected ArrayList<Sequence> data;
 	protected Parameter param;
 	
 	protected int numOfElement;
 	
 	public UnitextCorpus () {
 		data = new ArrayList<Sequence>();
 		param = new Parameter();
 		numOfElement = 0;
 	}
 
 	public UnitextCorpus (Parameter param) {
 		data = new ArrayList<Sequence>();
 		this.param = param;
 		numOfElement = 0;
 	}
 	
 	public boolean readFile (String filename, boolean isUpdate) throws IOException {
 		FileReader fr = new FileReader(filename);
 		BufferedReader br = new BufferedReader(fr);
 		String line = null;
 		String prev_label = "";
 		
 		// initialization for OpenIE; pre-defined labels - ENT, NP
 		Alphabet labelDict = param.getLabelAlphabet();
 		int entLabelId = labelDict.lookup("ENT", true);
 		int npLabelId = labelDict.lookup("NP", true);
 		assert (entLabelId == 0);
 		assert (npLabelId == 1);
 		
 		Sequence oneSentence = new Sequence();
 		while ((line = br.readLine()) != null) {
			if (line.startsWith("#"))
				continue;
 			String[] tokens = line.trim().split(" ", -1);
 			if (tokens.length < 2) { // smth strange; len(blank line) = 1
 				if (oneSentence.size() > 0)
 					append(oneSentence);
 				oneSentence = new Sequence();
 				prev_label = "";
 				continue;
 			}
 			oneSentence.addElement(pack(tokens, isUpdate));
 			// todo: refactoring the following code for making edge (transition) feature index
 //			if (prev_label != "" && isUpdate) 
 //				param.indexingEdge(tokens[0], prev_label, 1.0);
 //			prev_label = tokens[0];
 		}
 		if (oneSentence.size() > 0)
 			append(oneSentence);
 		
 		br.close(); fr.close();
 		param.makeEdgeIndex(isUpdate);
 		
 		return true;
 	}
 	
 	public void append (Sequence instance) {
 		data.add(instance);
 		numOfElement += instance.size();
 	}
 	
 	static public String elimiter = ":";
 	
 	private SparseVector pack (String[] tokens, boolean isUpdate) {
 		SparseVector ret = new SparseVector();
 		String[] inputs = new String[tokens.length - 1];
 		double[] values = new double[tokens.length - 1];
 		for (int i = 1; i < tokens.length; i++) {
 			String[] insideTokens = {tokens[i]};
 			//String[] insideTokens = tokens[i].split(elimiter, -1);
 			
 			if (insideTokens.length > 1) {
 				inputs[i-1] = insideTokens[0];
 				values[i-1] = Double.parseDouble(insideTokens[1]);
 			}
 			else {
 				inputs[i-1] = tokens[i];
 				values[i-1] = 1;
 			}
 		}
 		
 		int[] ids = param.indexing(tokens[0], inputs, values, isUpdate);
 		ret.label = ids[0];
 		for (int i = 1; i < ids.length; i++) {
 			if (ids[i] >= 0)
 				ret.addElement(ids[i], values[i-1]);
 		}
 		
 		return ret;
 	}
 		
 	public void shuffle (java.util.Random r) {
 		Collections.shuffle(this.data, r);
 	}
 	
 	public int size () {
 		return data.size();
 	}
 	
 	public int sizeElement () {
 		return numOfElement;
 	}
 	
 	public Iterator<Sequence> iterator () {
 		return data.iterator();
 	}
 }
