 /* ******************************************************
  * * Copyright (C) 2013 xinouch** This file is part of L-Systems** L-Systems is free software: you can redistribute it and/or modify* it
  * under the terms of the GNU General Public License as published by* the Free Software Foundation, either version 3 of the License, or* (at
  * your option) any later version.** This program is distributed in the hope that it will be useful,* but WITHOUT ANY WARRANTY; without even
  * the implied warranty of* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the* GNU General Public License for more details.** You
  * should have received a copy of the GNU General Public License* along with this program. If not, see <http://www.gnu.org/licenses/>.
  * *****************************************************
  */
 /* ******************************************************
  * *
  * * Project: L-Systems* File: Generator.java** Created on: 26 févr. 2013* Author: xinouch*
  * *****************************************************
  */
 
 package parser;
 
 import java.security.SecureRandom;
 import java.util.ArrayList;
 
 
 /**
  * This class is used to generate phrases from a given grammar.
  * 
  * @author xinouch
  */
 public class Generator
 {
 	/** The grammar to use in generation */
 	private Grammar grammar;
 	/** The generated phrase */
 	private ListSymbols generated;
 	/** The last generated phrase (previous iteration) */
 	private ListSymbols lastGenerated;
 	/** The number of iteration realized */
 	private int actualIt = 0;
 	/** The number of iteration to realize */
 	private int totalIt = 3;
 	/** true if it has been correctly finished, false if the user stop it by calling stopGenerating() */
 	private boolean correctlyFinished = false;
 
 	/**
 	 * constructor
 	 * 
 	 * @param g the grammar to work with to generate words
 	 */
 	public Generator (Grammar g)
 	{
 		grammar = g;
 		generated = g.getAxiom().clone();
 		lastGenerated = generated.clone();
 	}
 
 	/**
 	 * generate a word from the grammar. The number of iteration to create the word can be precised with setTotalIt(), the default is 3.
 	 * @param listener the object that will receive the messages of advancement
 	 * @throws BadSymbolException 
 	 */
 	public void generate (GeneratorPseudoListener listener) throws BadSymbolException
 	{
 		correctlyFinished = true;
 		listener.begin();
 		switch (grammar.getType())
 		{
 			case Grammar.TYPE_DOL:
 				generateDOL(listener);
 				break;
 			case Grammar.TYPE_SOL:
 				generateSOL(listener);
 				break;
 			case Grammar.TYPE_DIL:
 				generateDIL(listener);
 				break;
 			case Grammar.TYPE_SIL:
 				generateSIL(listener);
 				break;
 		}
 		listener.finished();
 	}
 
 	/**
 	 * @return the grammar
 	 */
 	public Grammar getGrammar ()
 	{
 		return grammar;
 	}
 
 	/**
 	 * @param grammar the grammar to set
 	 */
 	public void setGrammar (Grammar grammar)
 	{
 		this.grammar = grammar;
 	}
 
 	/**
 	 * @return the generated
 	 */
 	public ListSymbols getGenerated ()
 	{
 		return generated;
 	}
 
 	/**
 	 * @return the lastGenerated
 	 */
 	public ListSymbols getLastGenerated ()
 	{
 		return lastGenerated;
 	}
 
 	/**
 	 * @return the actualIt
 	 */
 	public int getActualIteration ()
 	{
 		return actualIt;
 	}
 
 	/**
 	 * @return the totalIt
 	 */
 	public int getTotalIteration ()
 	{
 		return totalIt;
 	}
 
 	/**
 	 * Chage the number total of iterations to do in the generation of the word. Default is 3.
 	 * 
 	 * @param totalIt the totalIt to set
 	 */
 	public void setTotalIteration (int totalIt)
 	{
 		this.totalIt = totalIt;
 	}
 
 	/**
 	 * @return the correctlyFinished
 	 */
 	public boolean isCorrectlyFinished ()
 	{
 		return correctlyFinished;
 	}
 	
 	/**
 	 * stop the generation, isCorreclyFinished will return false
 	 */
 	public void stopGenerating ()
 	{
 		correctlyFinished = false;
 	}
 
 	/**
 	 * generate the words when the grammar is DOL
 	 * @param listener 
 	 */
 	private void generateDOL (GeneratorPseudoListener listener)
 	{
 		ArrayList<Rule> rules = grammar.getRules();
 		final int sizeRules = rules.size();
 		int sizeGenerated, i, j, offset, indexOld;
 		boolean isModified;
 
 		for (actualIt = 0; actualIt < totalIt && correctlyFinished; actualIt++)
 		{
 			lastGenerated = generated.clone();
 			sizeGenerated = generated.size();
 			i = 0;
 			indexOld = 0;
 			while (i < sizeGenerated && correctlyFinished) // loop for the generated phrase
 			{
 				j = 0;
 				isModified = false;
 				while ((!isModified) && (j < sizeRules) && correctlyFinished) // loop to apply each rules on each symbols of the phrase
 				{
 					offset = rules.get(j).applyOnce(indexOld, lastGenerated, i, generated);
 					if (offset != 0)
 					{
						i += offset;
 						if (i < 0)
 							i = 0;
 						sizeGenerated = generated.size();
 						isModified = true;
 					}
 					j++;
 				}
 				if (!isModified)
 					i++;
 				indexOld++;
 			}
 			listener.setStep(actualIt + 1, totalIt);
 		}
 	}
 
 	/**
 	 * generate the words when the grammar is SOL
 	 * @param listener 
 	 */
 	private void generateSOL (GeneratorPseudoListener listener)
 	{
 		ArrayList<Rule> rules = grammar.getRules();
 		final int sizeRules = rules.size();
 		int sizeGenerated, i, j, offset, indexOld;
 
 		for (actualIt = 0; actualIt < totalIt && correctlyFinished; actualIt++)
 		{
 			lastGenerated = generated.clone();
 			sizeGenerated = generated.size();
 			i = 0;
 			indexOld = 0;
 			while (i < sizeGenerated && correctlyFinished) // loop for the generated phrase
 			{
 				ArrayList<Rule> rulesApplicable = new ArrayList<Rule>();
 				offset = 0;
 				// We look at all rules that can be applied
 				for (j = 0; j < sizeRules && correctlyFinished; j++)
 				{
 					if (rules.get(j).canBeApplied(indexOld, lastGenerated))
 						rulesApplicable.add(rules.get(j));
 				}
 				// we pick one
 				Rule thisone = chooseRandomRule(rulesApplicable);
 				if (thisone != null)
 					offset = thisone.applyOnce(indexOld, lastGenerated, i, generated);
 				if (offset != 0) // one rule has been applied
 				{
 					i += offset < 0 ? 0 : offset; // si offset < 0 (deletion), on recule pas
 					sizeGenerated = generated.size();
 				}
 				else
 					// no rule applied, we take the following symbol
 					i++;
 				indexOld++;
 			}
 			listener.setStep(actualIt + 1, totalIt);
 		}
 	}
 
 	/**
 	 * generate the words when the grammar is DXL
 	 * @param listener 
 	 */
 	private void generateDIL (GeneratorPseudoListener listener)
 	{
 		ArrayList<OLRule> rulesOL = new ArrayList<OLRule>();
 		ArrayList<ILRule> rulesIL = new ArrayList<ILRule>();
 		final int size = grammar.getRules().size();
 		int sizeGenerated, i, j, offset, indexOld;
 		boolean isModified;
 
 		// Initialisation des tableaux
 		for (i = 0; i < size; i++)
 		{
 			if (grammar.getRules().get(i).isContextFree())
 				rulesOL.add((OLRule) grammar.getRules().get(i));
 			else
 				rulesIL.add((ILRule) grammar.getRules().get(i));
 		}
 		final int sizeIL = rulesIL.size(), sizeOL = rulesOL.size();
 
 		for (actualIt = 0; actualIt < totalIt && correctlyFinished; actualIt++)
 		{
 			lastGenerated = generated.clone();
 			sizeGenerated = generated.size();
 			i = 0;
 			indexOld = 0;
 			while (i < sizeGenerated && correctlyFinished) // loop for the generated phrase
 			{
 				j = 0;
 				isModified = false;
 				// we start with IL rules
 				while ((!isModified) && (j < sizeIL) && correctlyFinished) // loop to apply each rules on each symbols of the phrase
 				{
 					offset = rulesIL.get(j).applyOnce(indexOld, lastGenerated, i, generated);
 					if (offset != 0)
 					{
 						i += offset;
 						sizeGenerated = generated.size();
 						isModified = true;
 					}
 					j++;
 				}
 				j = 0;
 				// If none have been applied, we try with OL rules
 				while ((!isModified) && (j < sizeOL) && correctlyFinished) // loop to apply each rules on each symbols of the phrase
 				{
 					offset = rulesOL.get(j).applyOnce(indexOld, lastGenerated, i, generated);
 					if (offset != 0)
 					{
 						i += offset;
 						sizeGenerated = generated.size();
 						isModified = true;
 					}
 					j++;
 				}
 
 				if (!isModified)
 					i++;
 				indexOld++;
 			}
 			listener.setStep(actualIt + 1, totalIt);
 		}
 	}
 
 	/**
 	 * generate the words when the grammar is SXL
 	 * @param listener 
 	 */
 	private void generateSIL (GeneratorPseudoListener listener)
 	{
 		ArrayList<OLRule> rulesOL = new ArrayList<OLRule>();
 		ArrayList<ILRule> rulesIL = new ArrayList<ILRule>();
 		final int size = grammar.getRules().size();
 		int sizeGenerated, i, j, offset, indexOld;
 
 		// Initialisation des tableaux
 		for (i = 0; i < size; i++)
 		{
 			if (grammar.getRules().get(i).isContextFree())
 				rulesOL.add((OLRule) grammar.getRules().get(i));
 			else
 				rulesIL.add((ILRule) grammar.getRules().get(i));
 		}
 		final int sizeIL = rulesIL.size(), sizeOL = rulesOL.size();
 
 		for (actualIt = 0; actualIt < totalIt && correctlyFinished; actualIt++)
 		{
 			lastGenerated = generated.clone();
 			sizeGenerated = generated.size();
 			i = 0;
 			indexOld = 0;
 			while (i < sizeGenerated && correctlyFinished) // loop for the generated phrase
 			{
 				ArrayList<Rule> rulesApplicable = new ArrayList<Rule>();
 				offset = 0;
 				// We look at all rules that can be applied
 				// we start with IL rules
 				for (j = 0; j < sizeIL && correctlyFinished; j++)
 				{
 					if (rulesIL.get(j).canBeApplied(indexOld, lastGenerated))
 						rulesApplicable.add(rulesIL.get(j));
 				}
 				// If none have been chosen, we try with OL
 				if (rulesApplicable.isEmpty())
 				{
 					j = 0;
 					for (j = 0; j < sizeOL && correctlyFinished; j++)
 					{
 						if (rulesOL.get(j).canBeApplied(indexOld, lastGenerated))
 							rulesApplicable.add(rulesOL.get(j));
 					}
 				}
 				// we pick one
 				Rule thisone = chooseRandomRule(rulesApplicable);
 				if (thisone != null)
 					offset = thisone.applyOnce(indexOld, lastGenerated, i, generated);
 				if (offset != 0) // one rule has been applied
 				{
 					i += offset < 0 ? 0 : offset; // si offset < 0 (deletion), on recule pas
 					sizeGenerated = generated.size();
 				}
 				else
 					// no rule applied, we take the following symbol
 					i++;
 				indexOld++;
 			}
 			listener.setStep(actualIt + 1, totalIt);
 		}
 	}
 
 	/**
 	 * Return a rule choosen from the given set. The chose is made randomly, with the probability of each rules
 	 * 
 	 * @param rules the set of rules
 	 * @return the chosen rule
 	 */
 	private Rule chooseRandomRule (ArrayList<Rule> rules)
 	{
 		SecureRandom rand = new SecureRandom();
 		final int size = rules.size();
 		int i;
 		double sum = 0;
 		double probas[] = new double[size], proba;
 
 		if (rules.size() == 0)
 			return null;
 
 		for (i = 0; i < size; i++)
 			sum += rules.get(i).getProba();
 		for (i = 0; i < size; i++)
 			probas[i] = rules.get(i).getProba() / sum; // sum(probas[i]) = 1 && 0 <= probas[i] <= 1
 
 		proba = rand.nextDouble(); // 0 <= proba <= 1
 
 		i = 0;
 		sum = probas[i];
 		while ((proba >= sum) && (i < size))
 		{
 			sum += probas[i];
 			i++;
 		}
 
 		return rules.get(i == size ? i - 1 : i);
 	}
 }
