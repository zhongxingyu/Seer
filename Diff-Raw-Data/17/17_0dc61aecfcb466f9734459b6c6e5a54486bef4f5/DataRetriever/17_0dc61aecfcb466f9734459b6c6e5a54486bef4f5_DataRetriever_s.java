 package org.cnio.appform.util.dump;
 
 import java.util.*;
 import java.util.regex.Pattern;
 import java.io.BufferedWriter;
 import java.io.FileOutputStream;
 import java.io.OutputStreamWriter;
 import java.math.BigInteger;
 import java.nio.charset.Charset;
 import java.nio.charset.spi.CharsetProvider;
 
 import org.cnio.appform.entity.AppGroup;
 import org.cnio.appform.entity.Interview;
 import org.cnio.appform.entity.Project;
 import org.cnio.appform.util.AppUserCtrl;
 import org.cnio.appform.util.HibernateUtil;
 import org.cnio.appform.util.IntrvController;
 import org.hibernate.HibernateException;
 import org.hibernate.SQLQuery;
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 
 
 
 /**
  * 
  * This class performs the logic to get a dump as they want. To do that, the 
  * next steps are followed:
  * a- get the repeatable items for the interview and section requested
  * b- get the maximum number of answers for those repeatable items
  * c- get the rest of questions ("normal" questions) for the interview and section
  * d- build up the header of the file based on b and c. keep the header file
  * into a list as reference
  * e- get answers along with order, number and codquestion 
  *
  */
 public class DataRetriever {
 
 	private String filePath = "";
 	
 	private DataWriter dw;
 	
 	private Hashtable mapVarNames;
 	
 	public final static int MAX_ROWS = 15000;
 	
 	public DataRetriever (String path, Hashtable map) {
 		filePath = path;
 		mapVarNames = map;
 		
 		dw = new DataWriter (mapVarNames);
 	}
 
 	
 	
 /**
  * Execute an sql query on hibernate and return the result
  * @param sql, the string representing the query and ready to be runned
  */
   private List<Object[]> execQuery (String qryStr, int offset, int maxRows) {
      Session myHibSes = HibernateUtil.getSessionFactory().openSession();
      Transaction tx = null;
      List<Object[]> rows = null;
 
     try {
       tx = myHibSes.beginTransaction();
        SQLQuery sqlQry = myHibSes.createSQLQuery(qryStr);
        
        if (offset > 0)
       	 sqlQry.setFirstResult(offset);
        
        if (maxRows > 0)
       	 sqlQry.setMaxResults(maxRows);
       
       rows = sqlQry.list();
       tx.commit();
     }
     catch (HibernateException ex) {
       if (tx != null)
         tx.rollback();
 
       System.out.println(ex.getMessage());
       ex.printStackTrace ();
     }
     finally {
       myHibSes.close();
     }
 
     return rows;
   }
   
   
   
   
  /**
   * Gets the ids of the repeteable items in the questionnaire intrvId.
   * @param prjCode, the project code
   * @param intrvId, the database id of the interview
   * @param secOrder, the order of the section inside the interview
   * @return a string of comma-separated item ids
   */
   public String getRepeatableItems (String prjCode, 
       															Integer intrvId, Integer secOrder) {
   	String theIds = "";
   	
   	String qry = 
   		"select it.iditem, it.content "+
   		"from interview i, section s, item it, project prj "+
   		"where prj.project_code = '"+prjCode+"' "+
   		"and prj.idprj = i.codprj " +
   		"and s.codinterview = i.idinterview "+
   		"and i.idinterview in ("+intrvId+") "+
   		"and it.idsection = s.idsection "  +
   		"and it.\"repeatable\" = 1";
   	
   	List<Object[]> ids = execQuery (qry, -1, -1);
   	for (Object[] id: ids) {
   		Integer myId = (Integer)id[0];
   		theIds += myId.toString()+",";
   	}
   	
   	if (theIds.length() > 0)
   		theIds = theIds.substring(0, theIds.length()-1);
   	return theIds;
   }
   
   
   
   
  /**
   * En la lista hay que hacer como en el treemap: meter pares key-field
   * Pero en el orden program�tico de primero el padre (si es pregunta), despu�s
   * todos los hijos, de acuerdo con su item order; 
   * despu�s el siguiente padre y todos los hijos de �l de acuerdo con su itemorder;
   * todos repetidos maxAns (el n�mero m�ximo de respuestas del que m�s tiene)
   *
   * Build a ordered map with the entries itemorder.n.o=varname-n-o, where:<br/>
   * <ul>
   * <li>n is the number of answer for a repeatable questions</li>
   * <li>o is the order of the answer item for that question, when the question
   * has several answer values (i.e. litres/time unit)</li>
   * </ul>
   * @param res, the resultset with all repeatable questions
   * @return the ordered map
   */
   private LinkedHashMap<String,String> buildRepMap (List<Object[]> res) {
   	int maxAns = -1;
   	Integer oldParent = -1;
   	List<String> items = new ArrayList<String> ();
   	StringBuilder field = new StringBuilder (), key = new StringBuilder(),
   		auxField, auxKey;
   	LinkedHashMap<String,String> hdrFields = new LinkedHashMap<String,String>();
   	
   	
     for (Object[] row : res) {
       Integer order = (Integer) row[3], idq = (Integer) row[1];
       int num = ((BigInteger) row[5]).intValue(); // num of answers
       Integer itOrder = (Integer) row[4]; // item order in section
       String codq = (String) row[2]; // cod question
       Integer parent = (Integer) row[6]; // parent element
 
       // placeholder to replace with the correct number of answer
       field.append(codq + "-*-" + order);
       key.append(itOrder + ".*." + order);
 
       if (parent == null) { // parent is null 'coz this is the parent
         items.add(key.toString());
         items.add(field.toString());
       }
       else if (parent.compareTo(oldParent) != 0) {
         // reset everything but the elements retrieved has to be added to the
         // main
         // LinkedHashMap list
         for (int i = 0; i < maxAns; i++) {
 
           for (int it = 0; it < items.size(); it += 2) {
             auxKey = new StringBuilder(items.get(it));
             auxField = new StringBuilder(items.get(it + 1));
 
             int idx = auxKey.indexOf("*");
             auxKey.replace(idx, idx + 1, (new Integer(i + 1)).toString());
             idx = auxField.indexOf("*");
             auxField.replace(idx, idx + 1, (new Integer(i + 1).toString()));
 
             hdrFields.put(auxKey.toString(), auxField.toString());
           }
         }
         oldParent = parent;
         maxAns = num;
         items.clear();
         items.add(key.toString());
         items.add(field.toString());
       }
       else { // if (parent.compareTo(oldParent) == 0){ // parent is the same
 
         // i keep both the key and the field in the list just to be able to
         // retrieve them
         // later. Avoiding duplicates
         if (items.contains(key.toString()) == false) {
           items.add(key.toString()); // this is the key which will be in the
                                      // whole list
           items.add(field.toString()); // this is the field which is gonna be
                                        // set in the main list
         }
         maxAns = (maxAns >= num) ? maxAns : num;
       }
 
       field.delete(0, field.length());
       key.delete(0, key.length());
 
     } // end looping over rows: for (row...)
   	
 // this is to store the elements kept in the items list after finishing the loop
     for (int i = 0; i < maxAns; i++) {
 
       for (int it = 0; it < items.size(); it += 2) {
         auxKey = new StringBuilder(items.get(it));
         auxField = new StringBuilder(items.get(it + 1));
 
         int idx = auxKey.indexOf("*");
         auxKey.replace(idx, idx + 1, (new Integer(i + 1)).toString());
         idx = auxField.indexOf("*");
         auxField.replace(idx, idx + 1, (new Integer(i + 1).toString()));
 
         hdrFields.put(auxKey.toString(), auxField.toString());
       }
     }
 
   	return hdrFields;
   }
   
   
   
 /**
  * This method builds up a "ordered" map with the maximum number of repeatable items
  * found for all patients with the interview intrvId and for the section secOrder
  * For instance, if the section of a interview has two repeatable group of questions,
  * and the maximum number of answers for that repeatable group is 4, then 4 groups
  * of questions will appear in the result.
  * It is clear not all patients will have 4 answers for those questions, but we
  * have to take into account the maximum number.
  * @param prjCode the code of the project
  * @param grpId the database id of the group
  * @param intrvId the database id of the interview
  * @param secOrder the order of the section
  * @return a tree map with header values for repeatable items
  */  
   @SuppressWarnings ("unused")
   public LinkedHashMap<String,String> getRepeatHeader (String prjCode, Integer grpId,
 	                                  Integer intrvId, Integer sortOrder, Integer secOrder) {
   	String repids = getRepeatableItems (prjCode, intrvId, secOrder);
   	String grpParam = (grpId == null? "1=1 ": "g.idgroup = "+grpId);
   	
 /*  	Comparator <String> comparator = 
   		(sortOrder != null && sortOrder == 1)? new InvKeyComparator (): new KeyComparator ();
 */ 
   	LinkedHashMap<String,String> hdrFields = new LinkedHashMap<String,String> ();
   	List<String> qCodes = new ArrayList<String>();
 
 // if not repeatable items, avoid the rest of the method as I know the result
 // of the query is going to be an empty set
   	if (repids.length() == 0)
   		return hdrFields;
   	
   	String qry = 
   		"select distinct on (5,6,4,3) p.codpatient as codpat, " +
   		"q.idquestion as idq, q.codquestion as codq, "+
 			"pga.answer_order as aorder, it.item_order as itorder, count (pga.idp_a_q) as numans "+
 			",it.ite_iditem as parent " +
 			"from item it, question q, pat_gives_answer2ques pga, patient p"+
 			",interview i, section s, performance pf, appgroup g " +
 			" where (it.iditem in ("+repids+") or it.ite_iditem in ("+repids+")) "+
    		"and it.iditem = q.idquestion "+
    		"and it.ite_iditem is not null " +
    		"and pga.codquestion = q.idquestion "+
    		"and p.idpat = pga.codpat "+
    		"and i.idinterview = "+intrvId +
    		" and pf.codinterview = i.idinterview "+
    		"and s.section_order = "+secOrder+
    		" and s.codinterview = i.idinterview "+
    		"and it.idsection = s.idsection "+
    		"and "+ grpParam +
    		" and pf.codgroup = g.idgroup "+
    		"and pf.codpat = p.idpat "+
    		"group by 1, 2, 3, 4, 5, 7 order by 5 asc, 6 desc, 4 asc, 3";
 // System.out.println ("repeatHdr query:\n"+qry);
 
 // got rows like (codpat, idq, codq, aorder, numans, parent)
   	List<Object[]> res = execQuery (qry, -1, -1);
   	hdrFields = buildRepMap (res);
   	
 /*
 System.out.println ("hdrFields with LinkedHashMap");
 for (Map.Entry<String, String> entry : hdrFields.entrySet()) {
 	String akey = entry.getKey().toString();
 System.out.print(akey+"|");
 }
 System.out.println();
 */
   	return hdrFields;
   }
   
   
   
   
 /**
  * Build the arch-ordered list of questions by merging the listmap of items
  * with single answer and the listmap of repeatable questions. To do that, as
  * the order is absolutely custom, it is not possible to use a predefined
  * structure like a TreeMap and the merging has to be done manually
  * @param singlesMap
  * @param repMap
  * @return
  */  
   private LinkedHashMap<String, String> buildFullMap(
                     Set<Map.Entry<String, String>> singleSet,
                     Set<Map.Entry<String, String>> repSet) {
 
     Iterator<Map.Entry<String, String>> repIt = repSet.iterator(), 
                                       singleIt = singleSet.iterator();
     
     Map.Entry<String, String> singleEntry = null, repEntry = null;
     String singleKey, repKey;
     String singleParts[] = null, repParts[] = null;
     LinkedHashMap<String, String> fullMap = new LinkedHashMap<String, String>();
 
 // now, aMap and fullMap has to be merged with item order to get the full list
 // every time an item is put into the fullMap list, it is removed from the source list
 // (either repeatable or single list) and his assciated boolean variable
 // (singleRemoved or repRemoved for singles and repeatables) is set to true
 // indicating that item was the last one in being removed from the list and 
 // another has to be taken off the list. In such a way, all items are got in
 // the specific order
     boolean singleRemoved = true, repRemoved = true;
     while (true) {
       singleKey = null;
       repKey = null;
       singleEntry = singleRemoved == false ? singleEntry : null;
       repEntry = repRemoved == false ? repEntry : null;
 
       // end condition, loop is over
       if (singleIt.hasNext() == false && repIt.hasNext() == false &&
       		singleEntry == null && repEntry == null)
         break;
 
       if (singleRemoved) {
         singleEntry = singleIt.hasNext() ? singleIt.next() : null;
       }
       singleKey = singleEntry == null ? null : singleEntry.getKey();
       singleParts = singleKey == null ? null : singleKey.split("\\.");
 
       if (repRemoved) {
         repEntry = repIt.hasNext() ? repIt.next() : null;
       }
       repKey = repEntry == null ? null : repEntry.getKey();
       repParts = repKey == null ? null : repKey.split("\\.");
 
       int keyComparison = singleKey == null ? 1 : (repKey == null ? -1
           : Integer.decode(singleParts[0]).compareTo(
               Integer.decode(repParts[0])));
 
       if (keyComparison < 0) {
         fullMap.put(singleKey, singleEntry.getValue());
         singleIt.remove();
         singleRemoved = true;
         repRemoved = false;
       }
       else {
         fullMap.put(repKey, repEntry.getValue());
         repIt.remove();
         repRemoved = true;
         singleRemoved = false;
       }
 
     } // while
 
     return fullMap;
   }
   
   
   
  /**
   * Similar to method getRepeatableHeader (...) but this method gets only the 
   * questions which does not have repeatable answers
   * @param prjCode the code of the project
   * @param grpId the database id of the group
   * @param intrvId the database id of the interview
   * @param secOrder the order of the section
   * @return a tree map with header values for the entire section
   */
   public LinkedHashMap<String, String> getHeader (String prjCode, Integer grpId,
       								Integer intrvId, Integer sortOrder, Integer secOrder) {
   	
   	String repids = getRepeatableItems (prjCode, intrvId, secOrder);
   	String repIdsConstraint = repids.length() == 0? "and it.ite_iditem is NULL ": 
   														"and (it.ite_iditem not in ("+repids+") or it.ite_iditem is NULL)";
  						
   	String sql = "select q.codquestion as codq, it.item_order as itorder, qa.answer_order "+
   				"from interview i, section s, item it, question q, question_ansitem qa "+
   				"where i.idinterview = "+intrvId+" "+
   				"and s.section_order = "+secOrder+" "+
   				"and s.codinterview = i.idinterview "+
   				"and it.idsection = s.idsection "+
   				"and it.iditem = q.idquestion "+
 //  				"and it.\"repeatable\" = 0 "+
   				repIdsConstraint +
   				"and q.idquestion = qa.codquestion "+
   				"order by 2";
 // System.out.println("getHdr:\n"+sql);
 
   	TreeMap<String, String> singlesMap = new TreeMap<String,String> (new ItOrderComparator()); ;
 /*  	fullMap = (sortOrder != null && sortOrder == 1)?
   				new TreeMap<String,String> (new InvKeyComparator()):
     				new TreeMap<String,String> (new KeyComparator());
 */  				
 
   	Map<String,String> repMap = getRepeatHeader (prjCode, grpId, intrvId, sortOrder, secOrder);
   	
 // with the repeatable questions already on the treemap
 // now is time for the "simple" questions, which key is done as
 // item_order.num_answer.order_answer
   	List<Object[]> res = execQuery (sql, -1, -1);
   	for (Object[] row: res) {
   		String field = (String)row[0]+"-1-"+row[2]; //codq-num-order, num=1
   		singlesMap.put((Integer)row[1]+".1."+row[2], field);
   	}
 
   	Set<Map.Entry<String, String>> singleSet = singlesMap.entrySet(), 
 										repSet = repMap.entrySet();
   	
 		LinkedHashMap<String,String> fullMap = buildFullMap (singleSet, repSet);
   	return fullMap;
   }
   
   
   
 /**
  * It gets the total rows retrieved for the full resultset, not constrained by
  * offset and maxRows parameters  
  * @param prjCode, the project code
  * @param intrvId, the database interview id
  * @param grpId, the database group id, if so
  * @param secOrder, the section within the interview
  * 
  * @return the number of rows composing the entire full resultset
  */
   public int getFullResultsetSize (String prjCode, Integer intrvId, Integer grpId,
 														Integer secOrder) {
   	int count = 0;
   	String secParam = (secOrder == null)?"s.section_order ": secOrder.toString();
   	String grpParam = (grpId == null? "1=1 ": "g.idgroup = "+grpId);
   	
     String sqlqry = "select count(*) as resultset_size " +
 	 		"from (" +
 	 		"select p.codpatient, g.name as grpname, "+
 	    "i.name as intrvname, s.name as secname, "+
 	    "q.codquestion as codq, a.thevalue, s.section_order, "+
 	    "it.item_order, pga.answer_order, pga.answer_number, it.\"repeatable\" as itrep "+
 	    "from patient p, pat_gives_answer2ques pga, appgroup g,	performance pf, "+
 	      "question q, answer a, interview i, item it, section s, project pj "+
 	    "where "+ grpParam +
 	    " and i.idinterview = "+intrvId +
 	    " and pj.project_code = '"+prjCode+"' " +
 	    "and pj.idprj = i.codprj "+
 	    "and pf.codinterview = i.idinterview "+
 	    "and pf.codgroup = g.idgroup "+
 	    "and s.codinterview = i.idinterview "+
 	    "and pf.codpat = p.idpat "+
 	    "and pga.codpat = p.idpat "+
 	    "and pga.codquestion = q.idquestion "+
 	    "and pga.codanswer = a.idanswer "+
 	    "and q.idquestion = it.iditem "+
 	    "and it.idsection = s.idsection " +
 	    "and s.section_order = " + secParam +
 	    ") a;";
 
     
     Session myHibSes = HibernateUtil.getSessionFactory().openSession();
     Transaction tx = null;
     List<Object[]> rows = null;
 
     try {
       tx = myHibSes.beginTransaction();
       SQLQuery sqlQry = myHibSes.createSQLQuery(sqlqry);
       
       List <BigInteger> uniqueCount = sqlQry.list();
       BigInteger qryCount = uniqueCount.get(0);
       count = qryCount.intValue();
       
 //      count = ((BigInteger)uniqueCount.get(0)[0]).intValue();
       tx.commit();
     }
     catch (HibernateException ex) {
       if (tx != null)
         tx.rollback();
 
       System.out.println(ex.getMessage());
       ex.printStackTrace ();
     }
     finally {
       myHibSes.close();
     }
   	return count;
   }
   
   
   
   
 	/**
 	 * Get the result set with the form 
 	 * (0codpatient, 1grpname, 2intrvname, 3secname, 4codq, 
 	 * 	5val, 6secorder, 7itemorder, 8ansorder, 9ansnumber, 10rep)
 	 * 
 	 * @param prjCode, the project code
 	 * @param intrvId, the database id (PK) for the interview
 	 * @param grpId, the database id for the group
 	 * @param secOrder, the number of section regarding to the sort order
 	 * @param initRow, the first row to return
 	 * @param maxRows, the maximun number of rows to return. Set to the
 	 * DataRetriever.MAX_ROWS value
 	 * 
 	 * @return a list with MAX_ROWS-offset + 1 rows returned as maximum 
 	 */
   private List<Object[]> getResultSet (String prjCode, Integer intrvId, Integer grpId,
           												Integer secOrder, int initRow, int maxRows) {
   	String secParam = (secOrder == null)?"s.section_order ": secOrder.toString();
   	String grpParam = (grpId == null? "1=1 ": "g.idgroup = "+grpId);
   	
      String sqlqry = "select p.codpatient, g.name as grpname, "+
         "i.name as intrvname, s.name as secname, "+
       "q.codquestion as codq, a.thevalue, s.section_order, "+
       "it.item_order, pga.answer_order, pga.answer_number, it.\"repeatable\" as itrep "+
       "from patient p, pat_gives_answer2ques pga, appgroup g,	performance pf, "+
         "question q, answer a, interview i, item it, section s, project pj "+
       "where "+ grpParam +
       " and i.idinterview = "+intrvId +
       " and pj.project_code = '"+prjCode+"' " +
       "and pj.idprj = i.codprj "+
       "and pf.codinterview = i.idinterview "+
       "and pf.codgroup = g.idgroup "+
       "and s.codinterview = i.idinterview "+
       "and pf.codpat = p.idpat "+
       "and pga.codpat = p.idpat "+
       "and pga.codquestion = q.idquestion "+
       "and pga.codanswer = a.idanswer "+
       "and q.idquestion = it.iditem "+
       "and it.idsection = s.idsection " +
       "and s.section_order = " + secParam +
       " order by 1, 7, 10, 8, 5, 9";
      
 System.out.println ("\nResultSet query:\n"+sqlqry);
      List<Object[]> rows = execQuery (sqlqry, initRow, maxRows);
      return rows;
 //     dw.writeRows(csvFile, rows);
   }
   
   
   
 /**
  * 
  * @param prjCode
  * @param intrvId
  * @param grpId
  * @param secOrder
  * @return an iterator over the resulset
  */
   private Iterator<Object[]> getResultSetIterator (String prjCode, Integer intrvId, Integer grpId,
           																	Integer secOrder) {
   	
   	Iterator<Object[]> sqlIt = null;
   	String secParam = (secOrder == null)?"s.section_order ": secOrder.toString();
   	String grpParam = (grpId == null? "1=1 ": "g.idgroup = "+grpId);
   	
     String sqlqry = "select p.codpatient, g.name as grpname, "+
         "i.name as intrvname, s.name as secname, "+
       "q.codquestion as codq, a.thevalue, s.section_order, "+
       "it.item_order, pga.answer_order, pga.answer_number, it.\"repeatable\" as itrep "+
       "from patient p, pat_gives_answer2ques pga, appgroup g,	performance pf, "+
         "question q, answer a, interview i, item it, section s, project pj "+
       "where "+ grpParam +
       " and i.idinterview = "+intrvId +
       " and pj.project_code = '"+prjCode+"' " +
       "and pj.idprj = i.codprj "+
       "and pf.codinterview = i.idinterview "+
       "and pf.codgroup = g.idgroup "+
       "and s.codinterview = i.idinterview "+
       "and pf.codpat = p.idpat "+
       "and pga.codpat = p.idpat "+
       "and pga.codquestion = q.idquestion "+
       "and pga.codanswer = a.idanswer "+
       "and q.idquestion = it.iditem "+
       "and it.idsection = s.idsection " +
       "and s.section_order = " + secParam +
       " order by 1, 7, 10, 8, 5, 9";
      
     Session myHibSes = HibernateUtil.getSessionFactory().openSession();
     Transaction tx = null;
     List<Object[]> rows = null;
 
     try {
       tx = myHibSes.beginTransaction();
        SQLQuery sqlQry = myHibSes.createSQLQuery(sqlqry);
        
       sqlIt = sqlQry.iterate();
       tx.commit();
     }
     catch (HibernateException ex) {
       if (tx != null)
         tx.rollback();
 
       System.out.println(ex.getMessage());
       ex.printStackTrace ();
     }
     finally {
       myHibSes.close();
     }
   	
   	
   	return sqlIt;
   }
   
   
   
   
 /**
  * Gets all patients which have a performance BUT no questions for that performance 
  * was replied as is in the database (there is no entry patient-question for that
  * performance-answer for that question in pat_gives_ans2question table).
  * <b>Note:</b> As on 14.10.2011, the prjCode param is not used in order to be
  * able to detect patients with interviews IN A DIFFERENT PROJECT than the 
  * right project (primarily for sample questionnaires).
  * @param prjCode, the project code (not the id in database, the project code)
  * @param intrvId, the id of the interview (as is in database PK)
  * @param grpId, the id of the group (idem as interview)
  * @return a (ordered) list of patients code strings
  */
   private List<Object[]> getPats4Intrv (String prjCode, Integer intrvId, Integer grpId) {
   	String grpParam = (grpId == null? "1=1 ": "g.idgroup = "+grpId);
   	String sqlQry = "select p.codpatient as codpat, g.name as grpName" +
   			" from patient p, performance pf, appgroup g " +
   			"where " + grpParam +
   			" and pf.codgroup = g.idgroup " +
 //  			"and p.codprj = '"+prjCode+"' " + 
   			"and p.idpat = pf.codpat " +
   	 		"and pf.codinterview =  " + intrvId +
   			" order by 1";
   	
   	List<Object[]> rows = execQuery (sqlQry, -1, -1);
 
 System.out.println (rows.size() + " patiens for \npatients4Intrv query: "+sqlQry);
  
 
   	return rows;
   }
 
   
   
   
 /**
  * Retrieves the number of answers for a set of questions of a questionnaire
  * grouping by the possible values. The method takes into account the order of
  * each answer for the question and also the number of answers for that (those)
  * question(s)
  * @param filename
  * @param intrvName
  * @param qCodes
  * @throws java.io.FileNotFoundException
  * @throws java.io.IOException
  */  
   public boolean getQuestionTotals (String filename, String prjName, 
   																	String intrvName, String[] qCodes)
   									throws java.io.FileNotFoundException, java.io.IOException {
 // build a string with the form (q.codquestion = 'A1' or q.codquestion = 'A3'...)
   	String questions = "(";
   	if (qCodes.length > 0) {
 			for (int i=0; i<qCodes.length; i++)
 				questions += "q.codquestion = '" + qCodes[i]+"' or ";
 			
 			questions = questions.substring(0, questions.length()-4) + ") ";
 		}
   	
 // get the interview id  	
     	Session theSess = HibernateUtil.getSessionFactory().openSession();
   	List<Project> prjs = HibernateUtil.getProjectByName(theSess, prjName);
   	Interview intrv = null;
   	if (prjs != null && prjs.size() > 0) {
   		Project prj = prjs.get(0);
   		IntrvController intrvCtrl = new IntrvController (theSess);
   		
   		List<Interview> intrvs = intrvCtrl.getIntr4Proj(prj, intrvName);
   		if (intrvs != null && intrvs.size() > 0)
   			intrv = intrvs.get(0);
   	}
   	
   	if (questions.length() < 5) {
   		System.err.println ("There are no enough question codes to query");
   		return false;
   	}
   	
   	if (intrv == null) {
   		System.err.println ("The questionnaire "+intrvName+" was not found for" +
   				"the project "+prjName);
   		return false;
   	}
   	
   	String qTotalsQry = "select pj.name as prj, i.name as intrv, g.name, " +
   			"q.codquestion as codq, a.thevalue, count(p.codpatient), " +
   			"pga.answer_number, pga.answer_order, q.idquestion " +
   			"from patient p, pat_gives_answer2ques pga, question q, answer a, " +
   			"interview i,	performance pf, appgroup g, project pj " +
 //    			"where (q.codquestion = 'A2') -- or q.codquestion = 'A3') " +
   			"where " + questions +
   			" and i.idinterview = " + intrv.getId() +
    			" and i.codprj = pj.idprj " +
   			"and p.idpat = pga.codpat and pga.codquestion = q.idquestion " +
   			"and pga.codanswer = a.idanswer and pf.codinterview = i.idinterview " +
   			"and pf.codgroup = g.idgroup and pf.codpat = p.idpat " +
   			"group by 1, 2, 3, 4, 5, 7, 8, 9 " +
   			"order by g.name, codq, pga.answer_number, pga.answer_order, a.thevalue;";
   	
   	List<Object[]> rs = execQuery (qTotalsQry, -1, -1);
   	BufferedWriter fileOut = new BufferedWriter (
         new OutputStreamWriter (new FileOutputStream (filename), Charset.forName("UTF-8")));
   	
   	dw.buildQuestionTotals(rs, fileOut);
   	fileOut.close();
   	
   	return true;
   }
   
     
   
   
   
  /**
   * This method is an interface method to execute an save to file out data about
   * the number of patiens which have a performance done and the number of
   * answers for any question in the performances. This is addressed to see
   * how many subjects with performances but no answer are held in the database. 
   * This is a more detailed dump than provided by getTotals method
   * @param fileName, the output filename
   * @throws java.io.FileNotFoundException
   * @throws java.io.IOException
   */ 
   public void getAllTotals (String fileName) 
   						throws java.io.FileNotFoundException, java.io.IOException {
   	
   	String allTotalQry = "select g.name as grpname, g.idgroup as gid, i.name as intrvname, " +
   			"pj.name as pjname, pj.project_code as prjcode, i.idinterview as idintrv, " +
   			"p.cod_type_subject as pattype, count (p.idpat) as countpat, 'perf' as qryType " +
   			"from interview i, performance pf, appgroup g, patient p, project pj " +
   			"where pf.codinterview = i.idinterview " +
   			"and pf.codgroup = g.idgroup and pf.codpat = p.idpat " +
   			"and pj.idprj = i.codprj " +
   			"group by 1, 2, 3, 4, 5, 6, 7 " +
   			"union " +
   			"select g.name as grpname, g.idgroup as gid, i.name as intrvname, " +
   			"pj.name as pjname,	pj.project_code as prjcode, i.idinterview as idintrv, " +
   			"pt.cod_type_subject as subjtype, count (pf.codpat) as countpat, " +
   			"'answers' as qryType " +
   			"from performance pf, interview i, appgroup g , patient pt, project pj " +
   			"where pf.codpat in (" +
   			"select p.idpat " +
   			"from patient p, pat_gives_answer2ques pga, question q, answer a " +
   			"where pga.codanswer = a.idanswer and pga.codquestion = q.idquestion " +
   			"and pga.codpat = p.idpat) " +
   			"and pf.codinterview = i.idinterview and pf.codgroup = g.idgroup " +
   			"and pf.codpat = pt.idpat and pj.idprj = i.codprj " +
   			"group by 1, 2, 3, 4, 5, 6, 7 " +
   			"order by 3, 2, 5, 9 desc, 7;";
   	
 // System.out.println (allTotalQry);
   	
   	List<Object[]> rs = execQuery(allTotalQry, -1, -1);
   	BufferedWriter fileOut = new BufferedWriter (
         new OutputStreamWriter (new FileOutputStream (fileName), Charset.forName("UTF-8")));
   	
   	dw.buildAllTotals(rs, fileOut);
   	fileOut.close();
   }
   
   
   
   
   
   
   
 /**
  * This method is an interface method to execute and save to file data about 
  * total number of subjects regarding to interviews and groups. Specifically, it
  * gives the number of patients with at least one answer for any question for
  * all interviews.
  * @param fileName, the output file name to save out the data
  * @throws java.io.FileNotFoundException
  * @throws java.io.IOException
  */  
   public void getTotals (String fileName) 
   								throws java.io.FileNotFoundException, java.io.IOException {
   
   	String totalQry = "select g.name as gname, g.idgroup as gid, i.name as intrvname, " +
   			"pj.name as pjname, " +
   			"pj.project_code as pjcode, i.idinterview, pt.cod_type_subject as subjtype, " +
   			"count (pf.codpat) as countpat " +
   			"from performance pf, interview i, appgroup g , patient pt, project pj " +
   			"where pf.codpat in (" +
   			"	select p.idpat " +
   			"from patient p, pat_gives_answer2ques pga, question q, answer a " +
   			"	where pga.codanswer = a.idanswer and pga.codquestion = q.idquestion " +
   			"and pga.codpat = p.idpat " +
   			") " +
   			"and pf.codinterview = i.idinterview and pf.codgroup = g.idgroup " +
   			"and pf.codpat = pt.idpat and pj.idprj = i.codprj " +
   			"group by 1, 2, 3, 4, 5, 6, 7 " +
   			"order by 3, 2, 5;";
 // System.out.println (totalQry);
   	
   	List<Object[]> rs = execQuery(totalQry, -1, -1);
   	BufferedWriter fileOut = new BufferedWriter (
         new OutputStreamWriter (new FileOutputStream (fileName), Charset.forName("UTF-8")));
   	
   	dw.buildTotals(rs, fileOut);
   	fileOut.close();
   }
   
   
   
   
   /**
 	 * Write out a file with the interview data from database parametrized by project,
 	 * questionnaire, group and sections.
 	 * @param prjCode, the project code
 	 * @param intrvId, the interview id (can be redundant...)
 	 * @param grpId, the group the interviews are going to belong to
 	 * @param the section defined by its order in the questionnaire
 	 * @param fileName, the name of the file output
 	 */
 	  public void getDump (String prjCode, Integer intrvId, Integer grpId, 
 	  						Integer orderSec, Integer sortOrder, String fileName) 
 	  										throws java.io.FileNotFoundException, java.io.IOException {
 
 	  	BufferedWriter fileOut = new BufferedWriter (
 	          new OutputStreamWriter (new FileOutputStream (fileName), Charset.forName("UTF-8")));
 	  	LinkedHashMap<String,String> listMapHdr = getHeader (prjCode, grpId, intrvId, sortOrder, orderSec);
 	  	
 //write file header
 	  	String fileHeader = (mapVarNames == null)? dw.writeHeader(listMapHdr):
 	  																					dw.writeMappedHdr (listMapHdr, mapVarNames);
 // ver si lo que saca con var names y con codes es lo mismo
 // primero s�lo con la cabecera y 
 // luego probar con varias secciones y pa�ses
 			if (fileHeader == null) {
 				System.out.println("An error was found when getting variable names from file");
 				System.out.println("Try again without supplying a variable names file");
 			}
 			else {
 				System.out.println ("Writing file header as:");
 				System.out.println(fileHeader);
 				
 		  	fileOut.append (fileHeader);
 		  	fileOut.flush();
 		  	
 		  	List<Object[]> patients = getPats4Intrv(prjCode, intrvId, grpId);
 		  	int resultsetSize = 
 	  					this.getFullResultsetSize(prjCode, intrvId, grpId, orderSec);
 		  	int maxRows = DataRetriever.MAX_ROWS, offset = 0, rowsProcessed;
 		  	boolean moreResults = true;
 		  	List<Object[]> resultSet;
 		  	
 ////////////////////////////////////////////////////
 //		  	resultSet = getResultSet (prjCode, intrvId, grpId, orderSec, -1, -1);
 //		  	dw.buildResult (patients, listMapHdr, resultSet, fileOut);
 
 		  	SqlDataRetriever sqldr = new SqlDataRetriever();
 		  	java.sql.ResultSet rs = sqldr.getResultSet(prjCode, intrvId, grpId, orderSec);
 		  	try {
 		  		if (patients.size() > 0)
 		  			dw.buildResultSet(patients, listMapHdr, rs, fileOut);
 		  	}
 		  	catch (Exception ex) {
 		  		ex.printStackTrace();
 		  	}		  	
 /*
 		  	while (moreResults) {
 		  		resultSet = getResultSet (prjCode, intrvId, grpId, orderSec, offset, maxRows);
 		  		rowsProcessed = dw.buildResult(patients, listMapHdr, resultSet, resultsetSize, fileOut);
 		  		
 System.out.println ("num of results: "+resultSet.size());
 //					offset += maxRows+1;
 					offset += rowsProcessed;
 		  		moreResults = resultSet.size() > offset;
 		  		
 		  		resultSet.clear();
 		  	}		  	
 //		  	List<Object[]> resultSet = getResultSet (prjCode, intrvId, grpId, orderSec);
 		  	
 //		  	dw.buildResultSet (listMapHdr, resultSet, fileOut);
 //		  	dw.buildResult(patients, listMapHdr, resultSet, fileOut);
  */
 			}
 	    fileOut.close(); 
 	  }
   
 	  
 	  
 	  
 	  
   
   /**
    * Interface method to get a dump from project, interview, group names, not ids
    * @param prjName, the name of the project
    * @param intrvName, the name of the interview
    * @param grpName, the name of the group
    * @param orderSec, the section
    * @param fileName, the fileName
    */
 	  public void getDump (String prjName, String intrvName, String grpName, 
 	  										Integer orderSec, Integer sortOrder, String fileName) 
 	  										throws java.io.FileNotFoundException, java.io.IOException {
 	  	String prjCode = "";
 	  	Integer intrvId, grpId;
 	  	
 	  	Project prj = null;
 	  	Interview intrv = null;
 	  	AppGroup grp = null;
 	  	
 	  	Session hibSes = HibernateUtil.getSessionFactory().openSession();
 	  	List<Project> prjs = HibernateUtil.getProjectByName(hibSes, prjName);
 	  	IntrvController intrvCtrl = new IntrvController (hibSes);
 	  	AppUserCtrl usrCtrl = new AppUserCtrl (hibSes);
 	  	
 	  	if (prjs != null && prjs.size() > 0) {
 	  		prj = prjs.get(0);
 //	  		IntrvController intrvCtrl = new IntrvController (hibSes);
 	  		
 	  		List<Interview> intrvs = intrvCtrl.getIntr4Proj(prj, intrvName);
 	  		if (intrvs != null && intrvs.size() > 0)
 	  			intrv = intrvs.get(0);
 	  	}
 	  	
 	  	if (grpName != null && grpName.length() > 0) {
 // System.out.println ("DateRetriever: session.open? "+hibSes.isOpen());
 //		  	AppUserCtrl usrCtrl = new AppUserCtrl (hibSes);
 		  	grp = usrCtrl.getGroupFromName(grpName);
 		  	if (hibSes.isOpen())
 		  		hibSes.close();
 		  	
 		  	if (grp == null) {
 		  		System.err.println ("An group named '"+grpName+"' was not found");
 		  		return;
 		  	}
 	  	}
 	  	
 	  	if (prj == null) {
 	  		System.err.println ("A project named '"+prjName+"' was not found");
 	  		return;
 	  	}
 	  	
 	  	if (intrv == null) {
 	  		System.err.println ("An interview named '"+intrvName+"' for project " +
 	  				"'"+prjName+"' was not found");
 	  		return;
 	  	}
 	  	
 	  	prjCode = prj.getProjectCode();
 	  	intrvId = intrv.getId();
 	  	grpId = grp != null? grp.getId(): null;
 	  	
 	  	getDump (prjCode, intrvId, grpId, orderSec, sortOrder, fileName);
 	  	
 	  	if (hibSes.isOpen())
 	  		hibSes.close();
 	  	
 	  }
 	  
 	  
 	/*  
 	  public void getDump (String prjCode, Integer grpId,
 		                                  Integer intrvId, Integer secOrder) 
 	  											throws java.io.IOException {
 	  	TreeMap <String,String> list = getHeader (prjCode, grpId, intrvId, secOrder);
 	  	System.out.println(dw.writeHeader(list));
 	  		
 	  	List<Object[]> resultSet = getResultSet ("157", 50, 304, 8);
 	  	dw.buildResultSet(list, resultSet, null);
 	  	
 	  }
 	*/ 
 	  
 	  
 
 }
 
