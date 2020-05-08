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
  * * Project: L-Systems* File: Rule.java** Created on: 26 févr. 2013* Author: xinouch******************************************************
  */
 
 package parser;
 
 /**
  * Represent a rewrite rule. A rule contains a preExpr which contains a single of Symbol that is going to be replaced, and a postExpr that
  * contains the substitute symbols: preExpr -> postExpr.
  * <p>
  * Each rule has a probability to happen if it is in conflict with another (for non determinist grammars)
  * </p>
  * </p>
  * 
  * @author xinouch
  */
 public interface Rule
 {
 	/**
 	 * tell if this rule can be applied in the following phrase
 	 * 
 	 * @param phrase
 	 * @return true if it can be applied, false otherwise
 	 */
 	public boolean canBeApplied (ListSymbols phrase);
 
 	/**
 	 * tell if this rule can be applied in the following phrase at the given index.
 	 * 
 	 * @param indexPrev l'index actuel dans la chaine précédente
 	 * @param phraseFromPreviousIteration the phrase as it was on last iteration
 	 * @return true if it can be applied, false otherwise
 	 */
 	public boolean canBeApplied (int indexPrev, ListSymbols phraseFromPreviousIteration);
 
 	/**
 	 * apply this rule on the given phrase.
 	 * 
 	 * @param phrase
 	 * @return true if the rule has really been applied.
 	 */
 	public boolean apply (ListSymbols phrase);
 
 	/**
 	 * Try to apply this rule on the given index of toBeModified. phraseFromPreviousIteration is used for non context-free grammars. For the
 	 * first calls, use call the function like this: applyOnce(index, toBeModified.clone(), toBeModified);
 	 * <p>
 	 * The returned value may be:
 	 * </p>
 	 * <ul>
 	 * <li>0 if nothing has been changed</li>
	 * <li>-1 if there has been one deletion</li>
 	 * <li>1 if a character has been changed</li>
 	 * <li>x if x characters have been inserted</li>
 	 * </ul>
 	 * 
 	 * @param indexPrev index from the previous
 	 * @param phraseFromPreviousIteration the phrase as it was on last iteration
 	 * @param indexNew the actual index in the new list actually in calcul
 	 * @param toBeModified the phrase to work on
 	 * @return the offset of the next symbol not changed.
 	 */
 	public int applyOnce (int indexPrev, ListSymbols phraseFromPreviousIteration, int indexNew, ListSymbols toBeModified);
 
 	/**
 	 * @return the probability for this rule to happen. It is 1 for determinist grammars.
 	 */
 	public double getProba ();
 
 	/**
 	 * @param probability the probability for the rule
 	 */
 	public void setProba (double probability);
 
 	/**
 	 * @return the preExpr
 	 */
 	public Symbol getPreExpr ();
 
 	/**
 	 * @param preExpr the preExpr to set
 	 */
 	public void setPreExpr (Symbol preExpr);
 
 	/**
 	 * @return the postExpr
 	 */
 	public ListSymbols getPostExpr ();
 
 	/**
 	 * @param postExpr the postExpr to set
 	 */
 	public void setPostExpr (ListSymbols postExpr);
 
 	/**
 	 * @return the determinist
 	 */
 	public boolean isDeterminist ();
 
 	/**
 	 * @param isDeterminist the determinist to set
 	 */
 	public void setDeterminist (boolean isDeterminist);
 
 	/**
 	 * @return true if the rule is contextFree
 	 */
 	public boolean isContextFree ();
 
 	public String toString ();
 }
