 package de.haw.cip.examples.ea_idsi1;
 
 /**
 * <p>Überschrift: Compiler & Interpreter Praktikum</p>
 * <p>Beschreibung: Compiler für Subset of Pascal</p>
  * <p>Copyright: Copyright (c) 2002</p>
  * <p>Organisation: </p>
  * @author unbekannt
  * @version 1.0
  */
 
 public abstract class State {
 	// Schnittstellen-Klasse fuer die Zustaende
 
 	// In allen Zustaenden zu implementieren:
 	protected abstract void acceptDigit(Automaton a, char ch);
 	protected abstract void acceptLetter(Automaton a, char ch);
 	protected abstract void acceptSign(Automaton a, char ch);
 	protected abstract void acceptOther(Automaton a, char ch);
 
 	// Transformiert die Aufrufe der Zustaende 
 	// in Aufrufe des Automaten, spez. Up-Casting newState
 
 	protected void changeState(Automaton a, State newState) {
 		a.changeState(newState);
 	}
 
 	protected void reset(Automaton a, char ch) {
 		a.reset(ch);
 	}
 
 	protected void resetAndStore(Automaton a, char ch) {
 		a.resetAndStore(ch);
 	}
 
 	protected void restore(Automaton a, char ch) {
 		a.restore(ch);
 	}
 
 	protected void restoreAndStore(Automaton a, char ch) {
 		a.resetAndStore(ch);
 	}
 
 	protected void store(Automaton a, char ch) {
 		a.store(ch);
 	}
 
 }
