 
 package controller.comparer.xmi.request;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 
 import org.json.simple.JSONObject;
 
 import controller.comparer.xmi.Utility;
 import controller.comparer.xmi.XmiAttributeElement;
 import controller.comparer.xmi.XmiClassDiagramComparer;
 import controller.comparer.xmi.XmiClassElement;
 import controller.comparer.xmi.XmiOperationElement;
 import controller.merge.xmi.xclass.XmiMergedClass;
 
 /**
  * Refresh()
  * Read the elements in three lists
  * @author Prashant Shukla
  *
  */
 
 public class Consolidate implements Request {
 
 	@Override
 	@SuppressWarnings("unchecked")
 	public JSONObject request(JSONObject jsonObj,
 			XmiClassDiagramComparer comparer) {
 
 		final String JSON_REQ 				= "Request";
 		final String JSON_REQ_CONSOLIDATE	= "Consolidate";
 		
 		final String JSON_REQ_CONSOLIDATE_CLASS_1 = "Class1";
 		final String JSON_REQ_CONSOLIDATE_CLASS_2 = "Class2";
 		final String JSON_REQ_CONSOLIDATE_CLASS_NAME = "Class";
 		final String JSON_REQ_CONSOLIDATE_CLASS_ATTR = "Attributes";
 		final String JSON_REQ_CONSOLIDATE_CLASS_OPER = "Operations";
 		final String JSON_REQ_CONSOLIDATE_NEW_CLASS = "Name";
 		JSONObject response = new JSONObject();
 		try {
 			String requestVal = (String) jsonObj.get(JSON_REQ);
 
 			// Verify we received the correct request message
 			if (requestVal.equals(JSON_REQ_CONSOLIDATE)){
 				
 				XmiMergedClass mergedclass = new XmiMergedClass();
 				// Extracting the values from class1
 				HashMap class1_info = (HashMap)jsonObj.get(JSON_REQ_CONSOLIDATE_CLASS_1);
 				
 				if(class1_info != null ) {
 				
 						String class1Name = (String)class1_info.get(JSON_REQ_CONSOLIDATE_CLASS_NAME);
 						XmiClassElement classElement1 = Utility.getClassByName(comparer.getUniqueClass1(), class1Name);
 						
 						//Get the list of attributes
 						ArrayList attrList1 = (ArrayList)class1_info.get(JSON_REQ_CONSOLIDATE_CLASS_ATTR);
 					
 						mergedclass.setClass1(classElement1);
 						mergedclass.setNewName(class1Name);
 						
 						if (attrList1 != null ) {
 							ArrayList <XmiAttributeElement> attrlist = new ArrayList<XmiAttributeElement> (); 
 							for (int i =0; i < attrList1.size(); i++) {
 								Object Attrval = attrList1.get(i);
 								
 								XmiAttributeElement  elem = Utility.getAttributebyName(classElement1,Attrval.toString());
								attrList1.add(elem);
 							}
 							mergedclass.setAttributes(attrlist);
 						}
 						// Get the list of operation
 						ArrayList operlist1 = (ArrayList)class1_info.get(JSON_REQ_CONSOLIDATE_CLASS_OPER);	
 						
 						if (operlist1 != null) {
 							// Get the list of attributes
 							ArrayList<XmiOperationElement> operList = new ArrayList<XmiOperationElement> ();
 							for (int i =0; i < operlist1.size(); i++) {
 								Object operval  = operlist1.get(i);
 								XmiOperationElement elem = Utility.getOperationbyName(classElement1,operval.toString());
 								operList.add(elem);
 							}
 							mergedclass.setOperations(operList);
 						}
 						comparer.getUniqueClass1().remove(classElement1);
 				}
 				// Extracting the values from Class2 
 				HashMap class2_info = (HashMap)jsonObj.get(JSON_REQ_CONSOLIDATE_CLASS_2);
 				
 				if(class2_info != null) {
 						String class2Name = (String)class2_info.get(JSON_REQ_CONSOLIDATE_CLASS_NAME);			
 						XmiClassElement classElement2 = Utility.getClassByName(comparer.getUniqueClass2(), class2Name);
 						
 						mergedclass.setClass2(classElement2);
 						mergedclass.setNewName(class2Name);
 						
 						// Get the list of attributes
 						ArrayList jSonAttrlist2 = (ArrayList)class2_info.get(JSON_REQ_CONSOLIDATE_CLASS_ATTR);
 						if (jSonAttrlist2 != null) {
 							ArrayList <XmiAttributeElement> attrlist2 = new ArrayList<XmiAttributeElement> (); 
 							for (int i =0; i < jSonAttrlist2.size(); i++) {
 								Object Attrval = jSonAttrlist2.get(i);
 								XmiAttributeElement  elem = Utility.getAttributebyName(classElement2,Attrval.toString());
 								attrlist2.add(elem);
 							}
 							mergedclass.setAttributes2(attrlist2);
 						}
 						// Get the list of operation
 						ArrayList jSonOperlist2 = (ArrayList)class2_info.get(JSON_REQ_CONSOLIDATE_CLASS_OPER);
 						if (jSonOperlist2 != null) {
 							ArrayList<XmiOperationElement> operList2 = new ArrayList<XmiOperationElement> ();				
 							// Get the list of attributes
 							for (int i =0; i < jSonOperlist2.size(); i++) {
								Object operval = jSonOperlist2.get(i);					
 								// Get the element information
 								XmiOperationElement elem = Utility.getOperationbyName(classElement2,operval.toString());
 								// add the element to the list 
 								operList2.add(elem);
 			
 							}
 							// add the operation list to the mergedClass.
 							mergedclass.setOperations2(operList2);
 						}
 						comparer.getUniqueClass2().remove(classElement2);
 				}
 				
 				String newName = (String) jsonObj.get(JSON_REQ_CONSOLIDATE_NEW_CLASS);
 				if(newName != null) {
 					mergedclass.setNewName(newName);
 				}
 				comparer.getSameClass().add(mergedclass);
 				//comparer.setUniqueClass1(classElement2);
 				
 				// Do a Refresh to set the Response
 				JSONObject refreshReq = new JSONObject();
 				refreshReq.put("Request","Refresh");
 				response = comparer.action(refreshReq);
 				
 			}
 		} catch (Exception ex) {
 			// Set up failed response
 			ex.printStackTrace();
 			response.put("Response", "Fail");
 		}
 		return response;
 
 	}	
 }
