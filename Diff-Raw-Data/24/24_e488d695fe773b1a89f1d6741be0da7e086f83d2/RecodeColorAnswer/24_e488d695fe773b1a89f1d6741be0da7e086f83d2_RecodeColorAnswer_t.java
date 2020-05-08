 package it.bisi.report.jasper.scriptlet;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.json.simple.JSONObject;
 import org.json.simple.JSONValue;
 
 import net.sf.jasperreports.engine.JRDefaultScriptlet;
 import net.sf.jasperreports.engine.JRScriptletException;
 
 public class RecodeColorAnswer extends JRDefaultScriptlet {
 
 	/**
 	 * Main purpose recode answer number (field) to 1,2,3 (negative,neutral, positive)
 	* for generating barchart and color info.
	* if no valid recoded answer is given, valueRecoded is set to original answer
 	* TODO check if it is better to set valueRecoded to null
	 * @return recodedAnswer
 	 */
 	
	public String recodeColorAnswer() throws JRScriptletException
 	{
 		String questionIdXform=(String) this.getFieldValue("report_question_id");
 		String[] temp=questionIdXform.split("/");
 		String questionId=temp[temp.length-1];
 		Double answer =  Double.parseDouble((String) this.getFieldValue("report_question_value")); 
 		
 		//get recode scheme
 		
		@SuppressWarnings("unchecked")
 		HashMap<String, Map<String, Map<String, Object>>> recodeScheme=(HashMap<String, Map<String, Map<String, Object>>>) this.getParameterValue("RECODE_COLOR_MAP");
 		//get recode scheme for this scale Type
 		//get json map: question id: type of question (questionId:number)
 		String questionInfoScale=(String) this.getParameterValue("SCALE_QUESTION_INFO");
 		JSONObject questionInfoScaleMap=(JSONObject) JSONValue.parse(questionInfoScale);
 		String scaleType=(String) questionInfoScaleMap.get(questionId);
 		Map<String, Map<String, Object>> scaleInfo=recodeScheme.get(scaleType);
 		
 		//iterate through scale type rows and find recoded Answer
 		//we test on >= and <= however the upper limit is excluded except for the last categorie, this is because if the value is te upper margin, it is also the lower margin of the next categorie
 		Double recodedAnswer = null;
 		for (Entry<String, Map<String, Object>> entry : scaleInfo.entrySet()) {
 			if (answer >= (Double) entry.getValue().get("lowest")  && answer <= (Double) entry.getValue().get("highest") ){
 				recodedAnswer= (Double) entry.getValue().get("targetValue");
 			}
 		}
 		if (recodedAnswer==null){
 			recodedAnswer=answer;
 		}
 		//set recodedVariable
		return recodedAnswer.toString();
 	}
}
