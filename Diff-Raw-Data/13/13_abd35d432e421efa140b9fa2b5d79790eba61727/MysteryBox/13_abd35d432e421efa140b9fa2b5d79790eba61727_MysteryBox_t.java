 package org.listerkeler.api.randoms;
 
import org.listerkeler.api.util.Timer;
import org.listerkeler.scripts.ChlenixChopper.ChlenixChopper;
 import org.vinsert.bot.script.ScriptContext;
 import org.vinsert.bot.script.api.Item;
 import org.vinsert.bot.script.api.Widget;
 
 public class MysteryBox extends RandomEvent {
 	
 	private static final int[] MYSTERY_BOX = { 3063 };
 	private static boolean randomSolved = false;
 	
 	public MysteryBox(ScriptContext context) {
 		super(context, "MysteryBox");
 	}
 	
 	public void solve() {
         log("Started MysteryBox random event!");
         while (!randomSolved) {
 			Item box = context.inventory.getItem(MYSTERY_BOX);
 			if (box != null) {
 				if (isValid(190)) {		
 					String answer = getAnswer();
 					for (int i = 10; i < 13; i++) {
 						Widget found = context.widgets.get(190, i);
 						if (found.getText().toLowerCase().contains(answer)) {
 							found.click();
 							randomSolved = true;
 							ChlenixChopper.finishedRandom();
 						}
 						for (Timer t = new Timer(2000L); (t.getElapsed() < 2000L) && (isValid(190)); sleep(100));
 					}
 				} else { 
 					int itemSlot = context.inventory.indexOf(context.inventory.getItem(MYSTERY_BOX));
 				  
 					log("Clicking box (" + itemSlot + ") in inventory");
 				  
 					context.inventory.interact(itemSlot, "Open");
 					for (Timer t = new Timer(2000L); (t.getElapsed() < 2000L) && (!isValid(190)); sleep(100));
 				}
 			}
 		}
 	}
 
     private void sleep(int time) {
         try {
             Thread.sleep(time);
         } catch (InterruptedException e) {
 
         }
     }
 
     private String getAnswer() {
 		String answer = "";
 		String question = getQuestion();
 		String sides = getSides();
 		String[] temp = sides.split("-");
 		for (String i : temp) {
 		    if (i.contains(question)) {
 		    	answer = i.replace(question, "");
 		    }
 		}
 		log("The box answer is " + answer);
 		return answer;
 	}
 
     private String getQuestion() {
 		String question = "";
 		if (isValid(190)) {
 			question = context.widgets.get(190, 6).getText();
 			if (question.contains("shape has")) {
 				question = question.substring(question.indexOf("number ") + "number ".length(), question.indexOf("?"));
 			}
 			if (question.contains("number is")) {
 				question = question.substring(question.indexOf("the ") + "the ".length(), question.indexOf("?")).toLowerCase();
 			}
 		}
 		log("The box question is " + question);
 		return question;
 	}
 
     private String getSides() {
 		String result = "";
 		String[] shape = { "", "", "" };
 		String[] number = { "", "", "" };
 
 		int[] circle = { 7005, 7020, 7035 };
 		int[] pentagon = { 7006, 7021, 7036 };
 		int[] square = { 7007, 7022, 7037 };
 		int[] star = { 7008, 7023, 7038 };
 		int[] triangle = { 7009, 7024, 7039 };
 
 		int[] n0 = { 7010, 7025, 7040 };
 		int[] n1 = { 7011, 7026, 7041 };
 		int[] n2 = { 7012, 7027, 7042 };
 		int[] n3 = { 7013, 7028, 7043 };
 		int[] n4 = { 7014, 7029, 7044 };
 		int[] n5 = { 7015, 7030, 7045 };
 		int[] n6 = { 7016, 7031, 7046 };
 		int[] n7 = { 7017, 7032, 7047 };
 		int[] n8 = { 7018, 7033, 7048 };
 		int[] n9 = { 7019, 7034, 7049 };
 
 		if (isValid(190)) {
 			for (int i = 0; i < 3; i++) {
                 // TODO: Add .getModelId() is here
                 final int widgetShape = context.widgets.get(190, i).getId();
                 if (contains(circle, widgetShape)) shape[i] = "circle";
 				if (contains(pentagon, widgetShape)) shape[i] = "pentagon";
 				if (contains(star, widgetShape)) shape[i] = "star";
 				if (contains(square, widgetShape)) shape[i] = "square";
 				if (contains(triangle, widgetShape)) shape[i] = "triangle";
 			}
 
 			for (int i = 3; i < 6; i++) {
                 // TODO: Add .getModelId() is here
                 final int widgetNumber = context.widgets.get(190, i).getId();
                 if (contains(n0, widgetNumber)) number[(i - 3)] = "0";
 				if (contains(n1, widgetNumber)) number[(i - 3)] = "1";
 				if (contains(n2, widgetNumber)) number[(i - 3)] = "2";
 				if (contains(n3, widgetNumber)) number[(i - 3)] = "3";
 				if (contains(n4, widgetNumber)) number[(i - 3)] = "4";
 				if (contains(n5, widgetNumber)) number[(i - 3)] = "5";
 				if (contains(n6, widgetNumber)) number[(i - 3)] = "6";
 				if (contains(n7, widgetNumber)) number[(i - 3)] = "7";
 				if (contains(n8, widgetNumber)) number[(i - 3)] = "8";
 				if (contains(n9, widgetNumber)) number[(i - 3)] = "9";
 			}
 
 			for (int i = 0; i < 3; i++) {
 				result = result + shape[i] + number[i] + "-";
 			}
 		}
 		log("The box sides are " + result);
 		return result;
 	}
 }
