 /**
  * 
  */
 package app;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import org.apache.log4j.Logger;
 
 import com.hp.hpl.jena.rdf.model.Selector;
 
 import exception.InvalidClueException;
 
 import framework.Clue;
 import framework.Solution;
 
 import lombok.AccessLevel;
 import lombok.Getter;
 import lombok.Setter;
 
 /**
  * @author Ben Griffiths
  * Represents a clue
  */
 public class ClueImpl implements Clue {
 	private static Logger log = Logger.getLogger(ClueImpl.class);
 	private final String APOSTROPHE_S_SEQUENCE = "'s"; // if present in a clue, requires further special transformation
 	private final String FILL_IN_THE_BLANK_MARKER = "_";
 	
 	@Getter(AccessLevel.PUBLIC) @Setter(AccessLevel.PRIVATE) private ArrayList<String> clueFragments;
 	
 	@Getter(AccessLevel.PUBLIC) @Setter(AccessLevel.PUBLIC) private String sourceClue;
 	@Getter(AccessLevel.PUBLIC) @Setter(AccessLevel.PUBLIC) private ArrayList<String> clueVariations;
 	@Getter(AccessLevel.PUBLIC) @Setter(AccessLevel.PUBLIC) private ArrayList<Selector> selectorVariations;
 	/* solutionStructure of e.g. {2, 3} means the answer consists of a 2-letter word followed by a 3-letter word */
 	@Getter(AccessLevel.PUBLIC) @Setter(AccessLevel.PUBLIC) private int[] SolutionStructure;
 	@Getter(AccessLevel.PUBLIC) @Setter(AccessLevel.PRIVATE) private boolean fillInTheBlank; // true if the clue is a 'Fill in the blank' style clue
 
 	/**
 	 * Constructor - constructs a clue from a String representing the clue text and an int[] representing the solution structure
 	 * @param clueText - the text of the clue
 	 * @param solutionStructure - an array of integers representing the structure of the solution, with each element of the array
 	 * representing a word, and the value of the element representing the number of letters in that array
 	 * @throws InvalidClueException - if either the clue text or the structure of the solution is invalid
 	 */
 	public ClueImpl(String clueText, int[] solutionStructure) throws InvalidClueException {
 		if(clueText == null || clueText.length() == 0)
 			throw new InvalidClueException("Empty clue");
 		if(solutionStructure == null || solutionStructure.length == 0)
 			throw new InvalidClueException("Invalid specification of solution structure");
 		for(int i = 0; i < solutionStructure.length; i++) {
 			if(solutionStructure[i] < 1)
 				throw new InvalidClueException("Invalid specification of solution structure");
 		}
 		this.setSolutionStructure(solutionStructure);
 		this.setClueFragments(new ArrayList<String>());
 		
 		
 		if(clueText.contains(FILL_IN_THE_BLANK_MARKER))
 			this.setFillInTheBlank(true);
 		
 		if(this.isFillInTheBlank() && imbalancedFITBMarkers(clueText))
 			throw new InvalidClueException("The numbed of underscores in the clue text doesn't " +
 											"match the number of words in the solution");
 		
 		this.setSourceClue(clueText);
 		this.parseClueText(clueText);
 		
 		
 		/* Logging */
 		log.debug("Clue text = " + this.getSourceClue());
 		log.debug("Clue fragments generated:");
 		for(String f : this.getClueFragments())
 			log.debug(f);
 	}
 	
 	
 	
 	/**
 	 *            - removes segments surrounded by double-inverted commas from the clue text of non-FITB-type clues, adds those 
 	 * segments, without the quotes, to the list of clueFragments, and returns the modified clue text.
 	 * The text of FITB-type clues is handled differently, with instances of " 's " removed, and then any remaining single-inverted
 	 * commas being removed, before returning the clue text with any double-inverted commas left in place. Assumes that quotation marks
 	 * come in pairs and are not nested
 	 * This function must be called before addClueFragments is called.
 	 * @param clueText
 	 * @return
 	 */
 	private void parseClueText(String clueText) {
 		String textToBeFragmented = clueText;
 		final String QUOTE = "\"";
 		if(this.isFillInTheBlank()) {
 			textToBeFragmented = clueText.replace(this.APOSTROPHE_S_SEQUENCE, ""); // remove instances of " 's "
 			textToBeFragmented = textToBeFragmented.replace("'", ""); // remove any remaining inverted commas
 		}
 		else { /* Remove any sequences surrounded by quotes and add them in their entirety as clue fragments */
 			while(textToBeFragmented.contains(QUOTE)) {
 				int indexOfStartQuote = textToBeFragmented.indexOf(QUOTE); // find the first double-inverted comma
 				int indexOfEndQuote = textToBeFragmented.indexOf(QUOTE, indexOfStartQuote + 1); // find the next one
 				String quotedSequence = textToBeFragmented.substring(indexOfStartQuote + 1, indexOfEndQuote);
 				String sequenceBeforeQuotation = textToBeFragmented.substring(0, indexOfStartQuote);
				String sequenceAfterQuotation = textToBeFragmented.substring(indexOfEndQuote + 1, textToBeFragmented.length());

 				this.getClueFragments().add(quotedSequence);
 				
 				textToBeFragmented = sequenceBeforeQuotation + sequenceAfterQuotation;
				System.err.println("textToBeFragmented after stripping quoted sequence: " + textToBeFragmented); // DEBUGGING *******
 			}
 		}
 		this.addClueFragments(textToBeFragmented);
 	}
 
 	/**
 	 * matchesStructure - see framework.Clue
 	 */
 	@Override
 	public boolean matchesStructure(Solution solution) {
 		return (Arrays.equals(solution.getSolutionStructure(), this.getSolutionStructure())); // requires comparison of deep equality
 	}
 	
 	/**
 	 * getSolutionStructureAsString - see framework.Clue
 	 */
 	@Override
 	public String getSolutionStructureAsString() {
 		if(this.getSolutionStructure() == null || this.getSolutionStructure().length < 1)
 			return "";
 		String structure = "[" + this.getSolutionStructure()[0];
 		for(int i = 1; i < this.getSolutionStructure().length; i++)
 			structure += ", " + this.getSolutionStructure()[i];
 		structure += "]";
 		return structure;
 	}
 	
 	private void addClueFragments(String clueText) {
 		if(this.isFillInTheBlank())
     		this.addFITBClueFragments(clueText);
 		else this.addStandardClueFragments(clueText);
 	}
 	
     private void addStandardClueFragments(String clueText) {
     	// firstly, remove any FITB markers, since we are treating this text as not being part of a FITB sequence
     	clueText = clueText.replace(this.FILL_IN_THE_BLANK_MARKER, "");
 		String[] wordsInClueText = clueText.split(" ");
 		
 		System.out.println("Elements of wordsInClueText after splitting around space:"); // DEBUGGING **************************
 		for(int i = 0; i < wordsInClueText.length; i++) // DEBUGGING **************************
 			System.out.println(i + ": " + wordsInClueText[i]); // DEBUGGING **************************
 		
 		
 		for(int i = 0; i < wordsInClueText.length; i++) {
 			String thisWord = wordsInClueText[i];
 
 			if(wordsInClueText[i].isEmpty())
 				continue;
 			thisWord = this.toProperCase(wordsInClueText[i]);
 			
 			if(!this.getClueFragments().contains(thisWord)) {
 				/* if the word ends with a comma, remove the comma before adding the word as a clue fragment */
 				if(thisWord.length() > 1 && (thisWord.substring(thisWord.length() - 1, thisWord.length()).equals(",")))
 						thisWord = thisWord.substring(0, thisWord.length() - 1);
 				/* if the word contains unbalanced parentheses, remove all parentheses */
 				if(this.imbalancedParentheses(thisWord)) {
 					thisWord = thisWord.replace("(", "");
 					thisWord = thisWord.replace(")", "");
 				}
 				this.getClueFragments().add(thisWord);
 				
 				/*
 				this.getClueFragments().add(thisWord);
 				// if the word ends with a comma or closing bracket, add the word without the comma/bracket as a fragment too
 				if(thisWord.length() > 1 && (thisWord.substring(thisWord.length() - 1, thisWord.length()).equals(",")
 						|| thisWord.substring(thisWord.length() - 1, thisWord.length()).equals(")")))
 					this.getClueFragments().add(thisWord.substring(0, thisWord.length() - 1));
 				// if the word begins with a (, add the word without the ( as a fragment too
 				if(thisWord.length() > 1 && thisWord.substring(0, 1).equals("("))
 					this.getClueFragments().add(thisWord.substring(1, thisWord.length()));
 				*/
 			}
 			for(int j = i + 1; j < wordsInClueText.length; j++) {
 				String wordToAppend = wordsInClueText[j];
 				if(!wordToAppend.isEmpty())
 					thisWord = thisWord + " " + this.toProperCase(wordToAppend);
 				if(!this.getClueFragments().contains(thisWord)) {
 					/* if the word ends with a comma, remove the comma before adding the word as a clue fragment */
 					if(thisWord.length() > 1 && (thisWord.substring(thisWord.length() - 1, thisWord.length()).equals(",")))
 							thisWord = thisWord.substring(0, thisWord.length() - 1);
 					/* if the word contains unbalanced parentheses, remove all parentheses */
 					if(this.imbalancedParentheses(thisWord)) {
 						thisWord = thisWord.replace("(", "");
 						thisWord = thisWord.replace(")", "");
 					}
 					this.getClueFragments().add(thisWord);
 					
 					
 					/*
 					this.getClueFragments().add(thisWord);
 					// if the word ends with a comma or closing bracket, add the word without the comma/bracket as a fragment too
 					if(thisWord.length() > 1 && (thisWord.substring(thisWord.length() - 1, thisWord.length()).equals(",")
 							|| thisWord.substring(thisWord.length() - 1, thisWord.length()).equals(")")))
 						this.getClueFragments().add(thisWord.substring(0, thisWord.length() - 1));
 					// if the word begins with a (, add the word without the ( as a fragment too
 					if(thisWord.length() > 1 && thisWord.substring(0, 1).equals("("))
 						this.getClueFragments().add(thisWord.substring(1, thisWord.length()));
 					*/
 				}
 			}
 		}
 
 		if(clueText.contains(this.APOSTROPHE_S_SEQUENCE)) {
 			String transformedClueText = clueText.replace(this.APOSTROPHE_S_SEQUENCE, "");
 			this.addStandardClueFragments(transformedClueText);
 		}
 	}
     
     private void addFITBClueFragments(String clueText) {
     	final String QUOTE = "\"";
     	ArrayList<String> FITBfragments = new ArrayList<String>();
     	ArrayList<String> otherFragments = new ArrayList<String>();
     	
     	if(!clueText.contains(QUOTE)) { // if no quotations are present, we complete the parsing of the clue as per non-FITB clues
     		this.addStandardClueFragments(clueText);
     		return;
     	}
     	
     	while(clueText.contains(QUOTE)) {
     		/* Clue text contains at least one double quote */
     		/* Find the first double quote */
     		int indexOfStartQuote = clueText.indexOf(QUOTE);
     		
     		if(indexOfStartQuote >= (clueText.length() - 1)) {
     			clueText = clueText.replace(QUOTE, ""); // if no matching end-quote exists, simply strip the quote out
     		}
     		else {
     			/* Find a matching end-quote */
         		int indexOfEndQuote = clueText.indexOf(QUOTE, indexOfStartQuote + 1);
         		
         		if(indexOfEndQuote == -1)
         			clueText = clueText.replace(QUOTE, ""); // if no matching end-quote exists, simply strip the quote out
         		else {
         			String fragmentBeforeQuotedSequence = clueText.substring(0, indexOfStartQuote);
         			String fragmentAfterQuotedSequence = clueText.substring(indexOfEndQuote + 1, clueText.length());
         			otherFragments.add(fragmentBeforeQuotedSequence);
         			otherFragments.add(fragmentAfterQuotedSequence);
         			
         			String FITBfragment = clueText.substring(indexOfStartQuote + 1, indexOfEndQuote);
         			FITBfragments.add(FITBfragment);
         			
         			clueText = clueText.replaceFirst(QUOTE, ""); // remove the processed start quote tag
         			clueText = clueText.replaceFirst(QUOTE, ""); // remove the processed end quote tag
         		}
     		}
     	}
     	
     	/* Parse the portions of the clue outside of quoted sections as normal */
     	for(String portionOfClue : otherFragments)
     		this.addStandardClueFragments(portionOfClue);
     	
     	/* Parse the quoted sections of the clue */
     	for(String fragment : FITBfragments) {
     		/* Split the clue around any FITB-markers that are present, and treat each fragment as a single unit rather than
     		 * a collection of words
     		 */
     		String[] fragmentsOfFITBsection = fragment.split(FILL_IN_THE_BLANK_MARKER);
     		
     		for(int i = 0; i < fragmentsOfFITBsection.length; i++) {
 				String thisFragment = fragmentsOfFITBsection[i];
 				if(!this.getClueFragments().contains(thisFragment)) {
 					this.getClueFragments().add(thisFragment);
 					/* if the fragment ends with a comma or closing bracket, add the fragment without the comma/bracket too */
 					if(thisFragment.length() > 1 && (thisFragment.substring(thisFragment.length() - 1, thisFragment.length()).equals(",")
 							|| thisFragment.substring(thisFragment.length() - 1, thisFragment.length()).equals(")")))
 						this.getClueFragments().add(thisFragment.substring(0, thisFragment.length() - 1));
 					/* if the fragment begins with a (, add the fragment without the ( too */
 					if(thisFragment.length() > 1 && thisFragment.substring(0, 1).equals("("))
 						this.getClueFragments().add(thisFragment.substring(1, thisFragment.length()));
 				}
     		}
     	}
     }
     
     /**
      * 
      * @param text
      * @return true if the number of opening brackets in the text argument is not equal to the number of closing brackets
      */
     private boolean imbalancedParentheses(String text) {
     	System.out.println("imbalancedParentheses called on: " + text); // DEBUGGING ********************************
     	final String OPEN_BRACKET = "(";
     	final String CLOSE_BRACKET = ")";
     	int openBracketCount = 0, closeBracketCount = 0;
     	for(int i = 0; i < text.length(); i++) {
     		String thisCharacter = text.substring(i, i + 1);
     		if(thisCharacter.equals(OPEN_BRACKET))
     			openBracketCount++;
     		else {
     			if(thisCharacter.equals(CLOSE_BRACKET))
     				closeBracketCount++;
     		}		
     	}
     	return (openBracketCount != closeBracketCount);
     }
 
 	private String toProperCase(String thisWord) {
 		log.debug("calling substring(0,1) on the String \"" + thisWord + "\"");
 		String thisWordInProperCase = thisWord.substring(0, 1).toUpperCase();
 		if(thisWord.length() > 1) {
 			int index = 1; // start at the second letter of the word
 			while(index < thisWord.length()) {
 				String nextCharacter = thisWord.substring(index, index + 1);
 				thisWordInProperCase += nextCharacter;
 				if((nextCharacter.equals(" ")) && (index < (thisWord.length() - 1))) {
 					 index++; // the next character needs to be capitalised
 					 nextCharacter = thisWord.substring(index, index + 1);
 					 thisWordInProperCase += nextCharacter.toUpperCase();
 				}
 				index++;
 			}
 		}
 		return thisWordInProperCase;
 	}
 	
 	/**
 	 * 
 	 * @param clueText
 	 * @return
 	 */
 	private boolean imbalancedFITBMarkers(String clueText) {
 		int countOfFITBmarkers = 0;
 		for(int i = 0; i < clueText.length(); i++) {
 			if(clueText.substring(i, i + 1).equals(this.FILL_IN_THE_BLANK_MARKER))
 				countOfFITBmarkers++;
 		}
 		return (countOfFITBmarkers != this.getSolutionStructure().length);
 	}
 }
