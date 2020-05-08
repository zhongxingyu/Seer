 package org.sergut.diceroller;
 
 import java.util.HashMap;
 
 import javax.swing.JFrame;
 
 import org.sergut.diceroller.adt.AdditionNode;
 import org.sergut.diceroller.adt.ConstantNode;
 import org.sergut.diceroller.adt.ContainerNode;
 import org.sergut.diceroller.adt.DieNode;
 import org.sergut.diceroller.adt.DieType;
 import org.sergut.diceroller.adt.Node;
 
 public class DiceRoller {
 
     /**
      * A cache of already parsed expressions into dice trees. 
      * 
      * As the method {@link #rollDice(String)} is called several million times, having
      * a cache saves around 95% of computation time (informal calculation) for a litle
      * price in memory. Not bad. ;-)
      */
     private HashMap<String,ContainerNode> diceBagMap = new HashMap<String,ContainerNode>();  
     
     /**
      * Takes a string with roll20 syntax, parses it, 
      * rolls it, and returns the result.
      * 
      * For now, we can only parse a normal addition of regular die, exploding die, and constants.
      * 
      * @param diceDescription a string like "2d10", "1d6!+1d8!", or "2d8!+1"
      * 
      * @return the result of the roll
      */
     /*
      * This method (with the adt package) is the main reusable part of this class/project.
      */
     public int rollDice(String diceDescription) {
 	ContainerNode diceTree = diceBagMap.get(diceDescription);
 	if (diceTree == null)
 	    diceTree = createNewDiceTree(diceDescription);
 	return diceTree.getResult();
     }
     
     private ContainerNode createNewDiceTree(String diceDescription) {
 	// parse addition
 	ContainerNode additionNode = new AdditionNode();
 	String[] tokens = diceDescription.split("\\+");
 	for (String s : tokens) {
 	    // parse dice and constants
 	    additionNode.addChild(parseAdditionOperands(s));
 	}
 	additionNode.clean();
 	addToCache(diceDescription, additionNode);
 	return additionNode;
     }
 
     private void addToCache(String diceDescription, ContainerNode additionNode) {
 	diceBagMap.put(diceDescription, additionNode);
     }
 
     /**
      * 
      * @param dice something like "5", "3d6", "2d6!" or "d20" 
      * @return
      */
     private static Node parseAdditionOperands(String diceExpression) {
 	String dice = regulariseDiceExpression(diceExpression);
 	if (!diceExpression.contains("d")) {
 	    return new ConstantNode(Integer.parseInt(diceExpression));
 	}
 	DieType dieType = DieType.REGULAR;
 	if (dice.endsWith("!")) {
 	    dice = dice.substring(0, dice.length()-1);
 	    dieType = DieType.EXPLODING;
 	}
 	if (dice.endsWith("d")) 
 	    throw new IllegalArgumentException("Wrong dice expression (no side count): " + diceExpression);
 	String[] tokens = dice.split("d");
 	if (tokens.length != 2)
 	    throw new IllegalArgumentException("Wrong dice expression: " + diceExpression);
 	int diceCount = Integer.parseInt(tokens[0]);
 	int sideCount = Integer.parseInt(tokens[1]);
 	return new DieNode(diceCount, sideCount, dieType);
     }
 
     /**
      * Gets something like "D20" and returns "1d20".
      */
     private static String regulariseDiceExpression(String s) {
 	String result = new String(s);
 	result = result.replace('D', 'd');
 	if (result.charAt(0) == 'd') 
 	    result = "1" + result;
 	return result;
     }
     
     public static void main(String args[]) {
 	JFrame mainFrame = new SavageWorldsDiceRollerFrame();
 	mainFrame.setVisible(true);
     }
 }
