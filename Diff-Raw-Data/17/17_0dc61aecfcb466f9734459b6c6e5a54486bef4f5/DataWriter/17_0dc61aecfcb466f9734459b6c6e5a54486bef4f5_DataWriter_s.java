 package org.cnio.appform.util.dump;
 
 import java.io.BufferedWriter;
 import java.math.BigInteger;
 import java.util.regex.Pattern;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 import java.util.LinkedHashMap;
 import java.util.Hashtable;
 
 import org.hibernate.ScrollableResults;
 
 public class DataWriter {
 	
   private final String CSV_SEP = "|";
 //  private Regex regexNum = new Regex ("(\\d+)")
 //	  private val regexLab = new Regex ("^[a-zA-Z]+[\\w|\\p{Punct}]+$")
 //  private val regexLab = new Regex ("(\\D+)")
 
   private String regexNum = "(\\d+)";
   private String regexDec = "(-?\\d+[\\.\\d+])";
   private String regexLab = "(\\D+)";
   
   private Hashtable mapNames;
   
 // the total number of fields to output in the buld download
   private int numFields = 0;
   
 /**
  * This is used to keep a track of processed rows for a entire resultset
  * It is reset when class is created and when a resulset is fully processed
  * (the very last patiend is written out a file).
  * It is used to set when that very last patient has to be written out. The condition
  * is that accumRows + rs.size() >= fullResultsetSize
  */
   private int accumRows = 0;
   
   public DataWriter (Hashtable map) {
   	mapNames = map;
   }
   
   
 /**
  * Writes out the header for the csv file. The fields are not determined and
  * as a guard to stop writing out is set as the change of patient code (or the end of
  * rowset).
  * @param rows the whole rowset.
  */
   public String writeHeader (List<Object[]> rows) {
     // This is ONLY for the header
      StringBuilder out = new StringBuilder ();
      Object[] aRow = rows.get(0);
      String patRef = (String)aRow[0];
 
 // System.out.println ("Writing file header as:");
 out.append("subject"+CSV_SEP+"group"+CSV_SEP+"interview"+CSV_SEP+"section"+CSV_SEP);
      Object[] innRow = aRow;
     for (Object[] row: rows) {
 // System.out.println ("codques: "+innRow[4]);
       innRow = row;
       if (((String)innRow[0]).equalsIgnoreCase(patRef))
         out.append(innRow[4]+CSV_SEP);
     }
     out.setCharAt(out.length()-1, '\n');
 
     return out.toString();
   }
 
   
   
 /**
  * Build up a custom csv header file from a map. Each of the TreeMap keys have
  * the form of itemOrder.codQ-ansNum-ordNum. itemOrder is there to keep the 
  * order of the questionnaires, but it is removed when yielding the file header
  * @param rows, actually it is a map with values = ""
  * @return a string which will be the header of the csv file
  */
   public String writeHeader (LinkedHashMap<String,String> rows) {
     // This is ONLY for the header
     StringBuilder out = new StringBuilder ();
     numFields = rows.size();
     
     out.append("subject"+CSV_SEP+"group"+CSV_SEP+"interview"+CSV_SEP+"section"+CSV_SEP);
 		for (Map.Entry<String, String> entry : rows.entrySet()) {
 			String val = entry.getValue();
 //		    out.append(entry.getKey()+CSV_SEP);
 			out.append(val+CSV_SEP);
 		} 
    out.setCharAt(out.length()-1, '\n');
 
    return out.toString();
   }
   
   
   
 
 /**
  * Gets the varname which is going to be in the file header.
  * The variable names are the values of the hashtable, whereas the keya are like
  * a qCode, XXXX-N-O, where N=1. The qCode param here is a normal question code,
  * but it can have N > 1 when repeatable questions. Then, in order to accomodate
  * the new variable names with repeatable questions, the N part of the variable
  * name has to be the same that is in the qCode parameter
  * COMPARISON IS CASE-INSENSITIVE (SO K14A-1-1==K14a-1-1)
  * 
  * @param qCode, the question code like XXXX-N-O, where XXXX is the true question
  * code as defined in the form builder
  * @param vars, the hashtable for the variable names, where the keys are like
  * XXXX-1-O, XXXX the question code as explained above
  * @return the variable name to be placed in the file header
  */
   private String getRightVarName (String qCode, Hashtable<String,String> vars) {
 // first: convert qCode like I3a_1-2-2 into I3a_1-1-2
   	String parts[] = qCode.split("-");
   	String numAnswer = parts[1];
   	qCode = parts[0]+"-1-"+parts[2];
   	
   	String varname = vars.get(qCode);
   	
 // this is to prevent exceptions. As the variable names file is read working around
 // malformed lines, it can be a question code without variable name (there are no
 // such a pair in the hashtable). then, when looking up the hashtable, the returned
 // value gets null
   	if (varname == null) {
   		System.err.println("No variable name for '"+qCode+"'");
   		return null;
   	}
   	parts = varname.split("-");
 //  	varname = parts[0]+"-"+numAnswer+"-"+parts[2];
   	if (Integer.decode(numAnswer) == 1)
   		varname = parts[0];
   	else
   		varname = parts[0]+"-"+numAnswer;
   	
   	return varname;
   }
   
   
   
 /**
  * Build up a custom csv header file based on a treemap (which keeps the fields in
  * the appropriate order) and a hash to map the codes in the treemap into the
  * corresponding variable names and loaded from a file
  * @param rows, this one represents the ordered set of question codes
  * @param mapVars, a hashtable with the mapping between the question codes and
  * the variable names.
  * @return the header of the csv file where the field labels are modified variable
  * names
  */
  public String writeMappedHdr (LinkedHashMap<String,String> rows, Hashtable<String,String> mapVars) {
   	StringBuilder out = new StringBuilder ();
     
 //  	System.out.println ("Writing file header as:");
   	out.append("subject"+CSV_SEP+"group"+CSV_SEP+"interview"+CSV_SEP+"section"+CSV_SEP);
   	
   	for (Map.Entry<String, String> entry : rows.entrySet()) {
   		String val = entry.getValue();
   		String varName = getRightVarName (val, mapVars);
   		val = (varName == null? val: varName);
   		if (val == null)
   			return null;
   		
   		out.append(val+CSV_SEP);
   	}
   	out.setCharAt(out.length()-1, '\n');
   	
   	return out.toString();
   }
   
 
   
 
   public void writeRows (BufferedWriter file, List<Object[]> rows) throws java.io.IOException {
 
      StringBuilder out = new StringBuilder ();
      String patRef = "";
 System.out.println ("Writing out rows ("+rows.size()+" rows)");
 			
 
     for (Object[] aRow: rows) {
     	Object[] innRow = aRow;
     	String grpName = "", intrvName = "", secName = "";
     	
       if (((String)innRow[0]).equalsIgnoreCase(patRef) == false) {
   //finish the previous subject and start the next one (if so)
         if (out.length() != 0) {
 //            out.replace(out.size-3, out.size, "\n")
 //          out.setCharAt(out.length()-1, '\n');
 //            print (out.toString)
 //        	out.deleteCharAt(out.length()-1);
         	out.setCharAt(out.length()-1, '\n');
           file.write(out.toString());
           file.flush();
           out.delete(0, out.length());
         }
         
         patRef = (String)innRow[0];
         grpName = (String)innRow[1];
         intrvName = (String)innRow[2];
         secName = (String)innRow[3];
 
         out.append("\""+patRef+"\""+CSV_SEP);
         out.append("\""+grpName+"\""+CSV_SEP);
         out.append("\""+intrvName+"\""+CSV_SEP);
         out.append("\""+secName+"\""+CSV_SEP);
       } // eo new patient detected
       String ansVal = (String)innRow[5];
       
 //      if (Pattern.matches(regexLab, ansVal))
       	ansVal = "\""+ansVal+"\"";
       
       out.append(ansVal+CSV_SEP);
     }
 
     System.out.println ();
     file.write(out.toString());
     file.flush();
   }
   
   
   
   
  /**
   * Build the final resultset based on a java.sql.ResultSet object with all rows
   * retrieved. Cursors are internally used to run along the resultset
   * @param patients
   * @param codes
   * @param rs the resulset as a java.sql.ResultSet object
   * @param file
   * @throws java.io.IOException
   * @throws java.sql.SQLException
   */ 
   public void buildResultSet (List<Object[]> patients, LinkedHashMap<String,String> codes, 
   									java.sql.ResultSet rs, BufferedWriter file) 
   									throws java.io.IOException, java.sql.SQLException {
   	
   	StringBuilder out = new StringBuilder ();
   	String patRef = "", grpRef = "";
   	int countPats = 0;
 System.out.println ("Writing out data");
   	
 //As the treemap is going to be used and it is the same than used when
 //yielding the header, it is better clear the values
 		for (Map.Entry<String, String> entry : codes.entrySet())
 			codes.put(entry.getKey(), "");
 	
 		String grpName = "", intrvName = "", secName = "";
 		Object[] patient = patients.get(countPats);
 		patRef = (String)patient[0];
 		grpRef = (String)patient[1];
 	  	
 	  	
 	 	java.sql.ResultSetMetaData rmd = rs.getMetaData();
 	  int columnCount = rmd.getColumnCount(), countRows = 0;
   	
 // loop over the resultset  	
   	while (rs.next()) {
   		countRows++;
 
   		Object[] innRow = new Object[columnCount];
   		for (int cols=0; cols < columnCount; cols++) 
   			innRow[cols] = rs.getObject(cols+1);
   		
       while (((String)innRow[0]).equalsIgnoreCase(patRef) == false) {
 //finish the previous subject and start the next one (if so)
         if (out.length() > 0) { // spit everything
         	
         	for (Map.Entry<String, String> entry : codes.entrySet()) {
         	  out.append(entry.getValue()+CSV_SEP);
         	  codes.put(entry.getKey(), "");
         	}
         }	
       	else {
   				out.append("\""+patRef+"\""+CSV_SEP);
 // System.out.println("\""+grpRef+"\""+CSV_SEP);
   	      out.append("\""+grpRef+"\""+CSV_SEP);
   	      out.append("\""+intrvName+"\""+CSV_SEP);
   	      out.append("\""+secName+"\""+CSV_SEP);
   	      
   	      for (int i=0; i<numFields; i++)
   	      	out.append ("|");
   			}
       	out.setCharAt(out.length()-1, '\n');
 
 				if (file != null) {
 					System.out.print("#");
 					file.append(out.toString());
           file.flush();
 				}
 				countPats++;
         out.delete(0, out.length());
         
         if (countPats < patients.size()) {
         	patient = patients.get(countPats);
         	patRef = (String)patient[0];
 //          grpName = (String)patient[1];
         	grpRef = (String)patient[1];
 // System.out.println("*** group change: "+grpRef);        	
 //          intrvName = (String)innRow[2];
 //          secName = (String)innRow[3];
         }
         else {
         	System.out.println("\n\nTotal subjects collected: " + countPats);
         	return;
         }
       } // eo while (...) => new patient detected
 
    
 // System.out.println ("***=> out.length? "+out.length());
 // new patient with list of results
   		if (out.length() == 0) {
   			intrvName = (String)innRow[2];
         secName = (String)innRow[3];
 
         out.append("\""+patRef+"\""+CSV_SEP);
         out.append("\""+grpRef+"\""+CSV_SEP);
 // System.out.println ("***=> appending group: "+grpRef);        
         out.append("\""+intrvName+"\""+CSV_SEP);
         out.append("\""+secName+"\""+CSV_SEP);
   		}
       
 // get the value of the answer and its keyField to put it in the codes hashtable
       String ansVal = (String)innRow[5], codq = (String)innRow[4];
       Integer num = (Integer)innRow[9], ord = (Integer)innRow[8]; 
       Integer itOrd = (Integer)innRow[7];
 //      String keyField = itOrd+"."+codq+"-"+num+"-"+ord;
       String keyField = itOrd+"."+num+"."+ord;
       String newkeyField = num+"."+itOrd+"."+codq+"."+ord;
       
       
 //      if (Pattern.matches(regexLab, ansVal))
       	ansVal = "\""+ansVal+"\"";
       
       if (codes.containsKey(keyField))
       	codes.put(keyField, ansVal);
       else
       	System.err.println("Interview '"+intrvName+"', sec. '"+secName+"': No key for question: " + keyField + "("+codq+")");
       
 //      out.append(ansVal+CSV_SEP);
     } // EO while (rs.next()), loop over resultset
   	
   	
 System.out.println ("\n ** Rows processed from java.sql.ResultSet: "+countRows);  	
 // here i have to write the very last subject retrieved
   	if (out.length() > 0) { // spit everything
     	
     	for (Map.Entry<String, String> entry : codes.entrySet()) {
     	  out.append(entry.getValue()+CSV_SEP);
     	  codes.put(entry.getKey(), "");
     	}
 
     	out.setCharAt(out.length()-1, '\n');
 //System.out.println(out.toString());
 			if (file != null) {
 				file.append(out.toString());
         file.flush();
 			}
 			countPats++;
     }
   	System.out.println ("\n\nTotal subjects collected: "+countPats);
   } // EO buildResultSet
   
   
   
   
   
   
   
 /**
  * This method writes out the results in the file in the correct place, which is,
  * matching with the file header (it means, matching answers with questions). 
  * It takes and additional list of subjects with any performance for the interview
 <<<<<<< HEAD
 <<<<<<< master
 =======
 =======
 >>>>>>> develop
  * but NO questions at all and put them in the output file with the entire row blank
  * @param patients, the list of patients with any performance for the requested
  * interview
  * @param codes, the ordered set for question codes
  * @param rs, the resultset, a list of patient,group, interview, section and 
  * answer parameters, all of them encapsulated in an Object[]
  * @param file, the output file
  * @throws java.io.IOException if there is any problem with file
  */  
   public void buildResult (List<Object[]> patients, LinkedHashMap<String,String> codes, 
   							List<Object[]> rs, BufferedWriter file) throws java.io.IOException {
 
 		StringBuilder out = new StringBuilder();
 		String patRef = "", grpRef = "";
 		int countPats = 0;
 		
 System.out.println("Writing out result:");
 		
 // As the treemap is going to be used and it is the same than used when
 // yielding the header, it is better clear the values
 		for (Map.Entry<String, String> entry : codes.entrySet())
 			codes.put(entry.getKey(), "");
 
 		String grpName = "", intrvName = "", secName = "";
 		Object[] patient = patients.get(countPats);
 		patRef = (String)patient[0];
 		grpRef = (String)patient[1];
 		int countRows = 0;
 		
 // loop over the resultset
 		for (Object[] row : rs) {
 			countRows++;
 			Object[] innRow = row;
   			
 		while (((String)innRow[0]).equalsIgnoreCase(patRef) == false) {
 			
 // writeout the current patient
 			if (out.length() > 0) {
 				for (Map.Entry<String, String> entry : codes.entrySet()) {
       	  out.append(entry.getValue()+CSV_SEP);
       	  codes.put(entry.getKey(), "");
       	}
 			}
 			else {
 				out.append("\""+patRef+"\""+CSV_SEP);
 	      out.append("\""+grpRef+"\""+CSV_SEP);
 	      out.append("\""+intrvName+"\""+CSV_SEP);
 	      out.append("\""+secName+"\""+CSV_SEP);
 	      
 	      for (int i=0; i<numFields; i++)
 	      	out.append ("|");
 			}
 			
 			out.setCharAt(out.length()-1, '\n');
 			if (file != null) {
 				System.out.print("#");
 				file.append(out.toString());
         file.flush();
 			}
 			countPats++;
       out.delete(0, out.length());
 				
 			// read the new patRef
       if (countPats < patients.size()) {
 				patient = patients.get(countPats);
 				patRef = (String)patient[0];
 				grpRef = (String)patient[1];
       }
       else {
       	System.out.println("\n\nTotal subjects collected: " + countPats);
       	return;
       }
 		} // EO new patient detected: current patcode != patref code
 		
 // new patient with list of results
 		if (out.length() == 0) {
 			intrvName = (String)innRow[2];
       secName = (String)innRow[3];
 
       out.append("\""+patRef+"\""+CSV_SEP);
       out.append("\""+grpRef+"\""+CSV_SEP);
       out.append("\""+intrvName+"\""+CSV_SEP);
       out.append("\""+secName+"\""+CSV_SEP);
 		}
 		
 // get the answer value and put it in the codes hashtable to keep the answers 
 // in an ordered fashion
 		String ansVal = (String) innRow[5], codq = (String) innRow[4];
 		Integer num = (Integer) innRow[9], ord = (Integer) innRow[8];
 		Integer itOrd = (Integer) innRow[7];
 		// String keyField = itOrd+"."+codq+"-"+num+"-"+ord;
 		String keyField = itOrd + "." + num + "." + ord;
 		String newkeyField = num + "." + itOrd + "." + codq + "." + ord;
 
 		
 		ansVal = "\"" + ansVal + "\"";
 		if (codes.containsKey(keyField))
 			codes.put(keyField, ansVal);
 		else
 			System.err.println("Interview '"+intrvName+"', sec. '"+secName+"': No key for question: " + keyField + "("+codq+")");
 
 	} // EO for row, ResultSet loop
 
 // here I have to write the very last subject retrieved
 	if (out.length() > 0) { // spit everything
 
 		for (Map.Entry<String, String> entry : codes.entrySet()) {
 			out.append(entry.getValue() + CSV_SEP);
 			codes.put(entry.getKey(), "");
 		}
 
 		out.setCharAt(out.length() - 1, '\n');
 		//System.out.println(out.toString());
 		if (file != null) {
 			file.append(out.toString());
 			file.flush();
 		}
 		countPats++;
 	}
 	System.out.println("\n\nTotal subjects collected: " + countPats);
 } // EO buildResultSet  
     
     
   
   
   
   
   
 /**
  * @deprecated
  * This method writes out the results in the file in the correct place, which is,
  * matching with the file header (it means, matching answers with questions). 
  * It takes and additional list of subjects with any performance for the interview
 <<<<<<< HEAD
 >>>>>>> local
 =======
 >>>>>>> develop
  * but NO questions at all and put them in the output file with the entire row blank.
  * The proccess is patients-driven, in such a way that a patient is taken from patients
  * list and, from its code, the answers are retrieved from the resultset and therefore
  * building up the row with all questions which will be written out to file
  * 
  * @param patients, the list of patients with any performance for the requested
  * interview
  * @param codes, the ordered set for question codes
  * @param rs, the resultset, a list of patient,group, interview, section and 
  * answer parameters, all of them encapsulated in an Object[]
  * @param file, the output file
  * @return the processed rows, that is, the diff between the current resultset
  * portion size and the no processed rows for the last incomplete patient
  * @throws java.io.IOException if there is any problem with file
  */  
   public int buildResult (List<Object[]> patients, LinkedHashMap<String,String> codes, 
   							List<Object[]> rs, int fullRSSize, BufferedWriter file) throws java.io.IOException {
 
 		StringBuilder out = new StringBuilder();
 		String patRef = "", grpRef = "";
 		int countPats = 0, rowsForLastPat = 0;
 		
 System.out.println("Writing out result");
 		// As the treemap is going to be used and it is the same than used when
 		// yielding the header, it is better clear the values
 		for (Map.Entry<String, String> entry : codes.entrySet())
 			codes.put(entry.getKey(), "");
 
 		String grpName = "", intrvName = "", secName = "";
 		Object[] patient;
 		
 		if (patients != null && patients.size() > 0) {
 			patient = patients.get(countPats);
 			patRef = (String)patient[0];
 			grpRef = (String)patient[1];
 		}
 			
 // loop over the resultset
 System.out.print ("Resultset progress: ");
 		for (Object[] row : rs) {
 // System.out.print(".");
 			Object[] innRow = row;
 			this.accumRows++;
 
 // patRef loop
 // in order to properly dump the patients, the very last patient has to be
 // discarded when getting bunchs of rows
 // a counter has to be hold and it is the return value for this method
 // the counter will be the number of rows processed and it will be the offset
 // for the next iteration
 			while (((String)innRow[0]).equalsIgnoreCase(patRef) == false) {
 				// writeout the current patient
 				if (out.length() > 0) {
 					for (Map.Entry<String, String> entry : codes.entrySet()) {
         	  out.append(entry.getValue()+CSV_SEP);
         	  codes.put(entry.getKey(), "");
         	}
 				}
 				else {
 					out.append("\""+patRef+"\""+CSV_SEP);
 		      out.append("\""+grpRef+"\""+CSV_SEP);
 		      out.append("\""+intrvName+"\""+CSV_SEP);
 		      out.append("\""+secName+"\""+CSV_SEP);
 		      
 		      for (int i=0; i<numFields; i++)
 		      	out.append ("|");
 				}
 				out.setCharAt(out.length()-1, '\n');
 				if (file != null) {
 					System.out.print("#");
 					file.append(out.toString());
           file.flush();
 				}
 				countPats++;
 				rowsForLastPat = 0; // the current row
         out.delete(0, out.length());
 					
 				// read the new patRef
         if (countPats < patients.size()) {
 					patient = patients.get(countPats);
 					patRef = (String)patient[0];
 					grpRef = (String)patient[1];
         }
         else {
         	System.out.println("\n\nTotal subjects collected: " + countPats);
         	return rs.size();
         }
 			} // EO while (patRef == false)
 			
 // new patient with list of results
 			if (out.length() == 0) {
 				intrvName = (String)innRow[2];
 	      secName = (String)innRow[3];
 	
 	      out.append("\""+patRef+"\""+CSV_SEP);
 	      out.append("\""+grpRef+"\""+CSV_SEP);
 	      out.append("\""+intrvName+"\""+CSV_SEP);
 	      out.append("\""+secName+"\""+CSV_SEP);
 			}
 			
 // get the answer value and put it in the codes hashtable to keep the answers 
 // in an ordered fashion
 			String ansVal = (String) innRow[5], codq = (String) innRow[4];
 			Integer num = (Integer) innRow[9], ord = (Integer) innRow[8];
 			Integer itOrd = (Integer) innRow[7];
 			// String keyField = itOrd+"."+codq+"-"+num+"-"+ord;
 			String keyField = itOrd + "." + num + "." + ord;
 			String newkeyField = num + "." + itOrd + "." + codq + "." + ord;
 
 			ansVal = "\"" + ansVal + "\"";
 			if (codes.containsKey(keyField))
 				codes.put(keyField, ansVal);
 			else
 				System.err.println("Interview '"+intrvName+"', sec. '"+secName+"': No key for question: " + keyField + "("+codq+")");
 
 			rowsForLastPat++;
 		} // EO for row, ResultSet loop
 
 // here i have to write the very last subject retrieved only if 
 // rs.length() < MAX_ROWS, which means t
 		if (out.length() > 0 && this.accumRows == fullRSSize) { // spit everything
 
 			for (Map.Entry<String, String> entry : codes.entrySet()) {
 				out.append(entry.getValue() + CSV_SEP);
 				codes.put(entry.getKey(), "");
 			}
 
 			out.setCharAt(out.length() - 1, '\n');
 			//System.out.println(out.toString());
 			if (file != null) {
 				file.append(out.toString());
 				file.flush();
 			}
 			countPats++;
 		}
 		System.out.println ();
 		System.out.println("\n\nTotal subjects collected: " + countPats);
 		
 		return rs.size() - rowsForLastPat; // processed rows
 	} // EO buildResultSet
 
 
   
   
   
  /**
   * This method writes out into a file rows like:
   * prj	|intrv |name	|codq	|thevalue	|count |answer_number |answer_order	|idquestion
   * 
   * The output into a file will be involving only prj, intrv, name, codq, thevalue,
   * the number of those values, number and order
   * @param rs, the resultset got from the query
   * @param bw, the output file
   */ 
   public void buildQuestionTotals (List<Object[]> rs, BufferedWriter bw) 
   												throws java.io.IOException {
   	String header = 
   		"Project|Questionnaire|Group|CodQuestion|Value|Quantity|Order|Number\n";
   	bw.append(header);
   	StringBuilder sbOut = new StringBuilder ();
   	
   	for (Object[] row: rs) {
   		sbOut.append(row[0]+"|"+row[1]+"|"+row[2]+"|"+row[3]+"|"+row[4]+"|"+row[5]);
   		sbOut.append("|"+row[6]+"|"+row[7]+"\n");
   		
   		bw.append(sbOut.toString());
   		bw.flush();
   		sbOut.delete(0, sbOut.length());
   	}
   	
   }
   
   
   
   
   
 /**
  * Get the total patients (cases, controls, samples) for every interview regarding
  * to the answers in the answer table and their performances. So, the number of
  * subjects for the performance aspect can be different than the number of subjects 
  * with at least one answer as one subject can have a performance but no answers
  * (if no question was answered).
  * 
  * Actually, builds up an output file from rows like:
  * name	| idgroup |	intrvname |	project_code | idinterview | subjtype |	source | count
  * Hospital General Universitario de Elche | 501 | Aliquots_SP_New |	157 |	4202 |	"" | answers |	5
  * Hospital de la Santa Creu I San Pau | 1 | Calidad Entrevista | 157 | 1350 | 1 | perf |42
  * @param rs, the resultset
  * @param bw, the output file
  * @throws java.io.IOException
  */  
   public void buildAllTotals (List<Object[]> rs, BufferedWriter bw) 
   													throws java.io.IOException {
   	String header = "Group|Questionnaire|Project|Type|Source|Count\n";
   	bw.append(header);
   	Object[] last = {"","","","","","","-1","",""};
   	int tempTotals = 0; // to accumulate totals
   	StringBuilder sb = new StringBuilder ();
   	String prj, grp, intrv, casecontrol, source;
   	boolean first = true;
   	
  // row is groupName, idGroup, intrvName, prjName, prjCode, idintrv, subject type, count 
  // 	for (Object[] row: rs) {
   	for (Iterator<Object[]> rsIt = rs.iterator(); rsIt.hasNext();) {
   		Object[] row = rsIt.next();
   		prj = (String)row[3];
   		grp = (String)row[0];
   		intrv = (String)row[2];
   		source = (String)row[8];
   		
   		casecontrol = ((String)row[6]).equalsIgnoreCase("1")? "Caso": 
   			((String)row[6]).equalsIgnoreCase("2")? "Control": "Sample?";
   		
   		int count = ((BigInteger)row[7]).intValue();
   		
  // if current attributes are equals to last ones, increment totals
   		if (prj.equalsIgnoreCase((String)last[3]) && 
   				grp.equalsIgnoreCase((String)last[0]) && 
   				intrv.equalsIgnoreCase((String)last[2]) &&
   				source.equalsIgnoreCase((String)last[8])	)
   			tempTotals += count;
   		
   		else if (!((String)last[6]).equalsIgnoreCase("-1")) { // print totals: 
   			sb.append((String)last[0]+"|");
   			sb.append((String)last[2]+"|");
   			sb.append((String)last[3]+"|");
   			
   			sb.append("Total"+"|");
   			sb.append((String)last[8]+"|");
   			sb.append(tempTotals+"\n");
   			tempTotals = count;
   		}
   		
 // in any case, print the current row  		
   		sb.append(grp+"|"+intrv+"|"+prj+"|"+casecontrol+"|"+source+"|"+count+"\n");
   		if (first) {
   			tempTotals = count;
   			first = false;
   		}
   		
 // this snippet checks printing of the very last totals in the case the last
 // retrieved two rows have the same prj, grp and intrv attributes
   		if (rsIt.hasNext() == false) { // FINISH!!!!
   			if (prj.equalsIgnoreCase((String)last[3]) && 
     				grp.equalsIgnoreCase((String)last[0]) && 
     				intrv.equalsIgnoreCase((String)last[2]) &&
     				source.equalsIgnoreCase((String)last[8])) {
   				sb.append((String)last[0]+"|");
     			sb.append((String)last[2]+"|");
     			sb.append((String)last[3]+"|");
     			
     			sb.append("Total"+"|");
     			sb.append((String)last[8]+"|");
     			sb.append(tempTotals+"\n");
   			}
   		} // EO finish
   		bw.append(sb.toString());
   		bw.flush();
   		sb.delete(0, sb.length());
   		
   		last = row;
   	} // EO for rs
   	
   }
 
   
   
   
   
 /**
  * Build up an output file from rows like:
  * name	| idgroup |	intrvname |	project_code | idinterview | subjtype |	count
  * Hospital General Universitario de Elche | 501 | Aliquots_SP_New |	157 |	4202 |	"" |	5
  * Hospital de la Santa Creu I San Pau | 1 | Calidad Entrevista | 157 | 1350 | 1 | 42
  * 
  * To process this one (cases controls and totals, lets see the layout) the whole
  * current row has to be taken into consideration in order to get the total number
  * of subjets for an interview and hospital (and project)
  * @param rs
  * @param bw
  */
   public void buildTotals (List<Object[]> rs, BufferedWriter bw) 
   												throws java.io.IOException {
   	
   	String header = "Group|Questionnaire|Project|Type|Count\n";
   	bw.append(header);
   	Object[] last = {"","","","","","","-1",""};
   	int tempTotals = 0; // to accumulate totals
   	StringBuilder sb = new StringBuilder ();
   	String prj, grp, intrv, casecontrol;
   	boolean first = true;
   	
  // row is groupName, idGroup, intrvName, prjName, prjCode, idintrv, subject type, count 
  // 	for (Object[] row: rs) {
   	for (Iterator<Object[]> rsIt = rs.iterator(); rsIt.hasNext();) {
   		Object[] row = rsIt.next();
   		prj = (String)row[3];
   		grp = (String)row[0];
   		intrv = (String)row[2];
   		
   		casecontrol = ((String)row[6]).equalsIgnoreCase("1")? "Caso": 
   			((String)row[6]).equalsIgnoreCase("2")? "Control": "Sample?";
   		
   		int count = ((BigInteger)row[7]).intValue();
   		
  // if current attributes are equals to last ones, increment totals
   		if (prj.equalsIgnoreCase((String)last[3]) && 
   				grp.equalsIgnoreCase((String)last[0]) && 
   				intrv.equalsIgnoreCase((String)last[2]))
   			tempTotals += count;
   		
   		else if (!((String)last[6]).equalsIgnoreCase("-1")) { // print totals: 
   			sb.append((String)last[0]+"|");
   			sb.append((String)last[2]+"|");
   			sb.append((String)last[3]+"|");
   			
   			sb.append("Total"+"|");
   			sb.append(tempTotals+"\n");
   			tempTotals = count;
   		}
   		
 // in any case, print the current row  		
   		sb.append(grp+"|"+intrv+"|"+prj+"|"+casecontrol+"|"+count+"\n");
   		if (first) {
   			tempTotals = count;
   			first = false;
   		}
   		
 // this snippet checks printing of the very last totals in the case the last
 // retrieved two rows have the same prj, grp and intrv attributes
   		if (rsIt.hasNext() == false) { // FINISH!!!!
   			if (prj.equalsIgnoreCase((String)last[3]) && 
     				grp.equalsIgnoreCase((String)last[0]) && 
     				intrv.equalsIgnoreCase((String)last[2])) {
   				sb.append((String)last[0]+"|");
     			sb.append((String)last[2]+"|");
     			sb.append((String)last[3]+"|");
     			
     			sb.append("Total"+"|");
     			sb.append(tempTotals+"\n");
   			}
   		} // EO finish
 
   		bw.append(sb.toString());
   		bw.flush();
   		sb.delete(0, sb.length());
   		
   		last = row;
   	} // EO for rs
   	
   } // EO buildTotals method
 }
