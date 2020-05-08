 package de.htw.berater;
 
 import java.util.List;
 
 import de.htw.berater.controller.Controller;
 import de.htw.berater.db.ResultData;
 import de.htw.berater.db.SQLConstraint;
 import de.htw.berater.ui.BeraterUI;
 
 public class StaticFactory {
 	public static BeraterUI getNewBeraterUI() {
 		return new BeraterUI() {
 			
 			
 			@Override
 			public void onNewQuestion(String newQuestion) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			
 			@Override
 			public void onFalseQuestion(String string) {
 				// TODO Auto-generated method stub
 				
 			}
 
 			@Override
 			public void show(List<ResultData> resultData) {
 				// TODO Auto-generated method stub
 				
 			}
 
 			@Override
 			public void show() {
 				// TODO Auto-generated method stub
 				
 			}
 
 			@Override
 			public void onNewData(List<ResultData> resultData) {
 				// TODO Auto-generated method stub
 				
 			}
 
 
 			@Override
 			public void setController(Controller controller) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 		};
 	}
 
 
 	public static Berater getNewBerater(String rdfPath, String namespace) {
 		return new Berater() {
 			
 			@Override
 			public void reset() {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			@Override
 			public List<SQLConstraint> getCurrentSQLConstraintsList() {
 				// TODO Auto-generated method stub
 				return null;
 			}
 			
 			@Override
			public boolean expectsKeywordAnswer() {
 				// TODO Auto-generated method stub
 				return false;
 			}
 			
 			@Override
 			public boolean expectsYesNoAnswer() {
 				// TODO Auto-generated method stub
 				return false;
 			}
 			
 			@Override
 			public String evaluateAndAskNewQuestion(boolean yes) {
 				// TODO Auto-generated method stub
 				return null;
 			}
 			
 			@Override
 			public String evaluateAndAskNewQuestion(String string) {
 				// TODO Auto-generated method stub
 				return null;
 			}
 
 			@Override
 			public String askFirstQuestionZweck() {
 				// TODO Auto-generated method stub
 				return null;
 			}
 
 			@Override
 			public String askFirstQuestionGeneral() {
 				// TODO Auto-generated method stub
 				return null;
 			}
 		};
 	}
 }
