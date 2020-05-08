 package Escada.tpc.tpcc.database.virtualdatabase;
 
 import Escada.tpc.tpcc.database.*;
 import Escada.tpc.tpcc.*;
 import Escada.tpc.tpcc.util.*;
 import Escada.tpc.common.util.*;
 import Escada.tpc.common.database.*;
 
 import java.util.*;
 
 /**
  * This class represents a virtual database based on the statistics distributions defined by the TPC-C.
  * In other words, instead of connecting to a real database, the emulation program accesses this
  * class in order to retrieve and update data. 
  * The dbVirtualDatabase was built to circumvent the scalability problems, such as resources constraints 
  * (e.g., memory, storage, cpu and network bandwidth), faced when a real database is usedi.
  * It is important to notice that it does not mischaracterize the TPC-C's definitions.
  *
  * This class can be divided into two main sections: <p>
  * (i) the first one defines a persitent mechanism which simulates the database'relations; <p>
  * (ii) the second one defines a set of methods, similar to stored procedures, which are used to access the relations.
  **/
 public class dbVirtualDatabase {
     private Random rand = new Random();
     private static persistentObjects ptrClass = null;
 
     /**
     * This class defines our persistent mechanism which simulates the database's relations.
     * For a detailed explanation about the structures internally created see 
     * <a href=figs/persistenObjects-Figure.ps>persistenteObjects</a>.
     *
     * TODO: This class could use bit comparison to speed up the search process.
     * We are planning for the next realease to do that.  
     *
     * TODO: Change a set of paramters that a hardcode and must be defined in TPCCConst class.
     **/
     private class persistentObjects {
         private Hashtable orders = new Hashtable();
         private Hashtable neworder = new Hashtable();
         private Hashtable customerLastName = new Hashtable();
         private Vector rangeCustomerLastName = new Vector();
 	private int nWareHouse = 0;
 
         /** It populates the database receceiving as parameter the total number of cliens.
         *   This population is solely based on the TPC specification. 
 	*/
         private persistentObjects(int totcli) {
             Hashtable ptrOrdersHash = null;
             Hashtable ptrNewOrderHash = null;
             Hashtable ptrCustomerLastNameHash[] = null;
             String lastname = null;
             String occur = null;
 
             int wid = ((totcli - 1) / TPCCConst.numMinClients) + 1;
 	    nWareHouse = wid;
 
             System.out.println("Creating the database strcutures before continuing the execution.");
             int contw = 1;
             while (contw <= wid) {
                 ptrOrdersHash = new Hashtable();
                 ptrNewOrderHash = new Hashtable();
                 ptrCustomerLastNameHash = new Hashtable[TPCCConst.rngDistrict];
 
                 orders.put(Integer.toString(contw),ptrOrdersHash);
                 neworder.put(Integer.toString(contw),ptrNewOrderHash);
 
                 customerLastName.put(Integer.toString(contw),ptrCustomerLastNameHash);
 
                 int contd = 1;
                 while (contd <= TPCCConst.rngDistrict) {
                     int nValue = 0;
                     ptrOrdersHash.put(Integer.toString(contd),
                                       Integer.toString(3000)); //TODO TPCCConst
                     ptrNewOrderHash.put(Integer.toString(contd),
                                         Integer.toString(2100));
                     ptrCustomerLastNameHash[contd - 1] = new Hashtable();
 
                     int contc = 0;
                     while (contc <= TPCCConst.numENDCustomer)
                     {
                         lastname =
                             TPCCRandGen.digSyl(RandGen.NURand(rand,TPCCConst.LastNameA,TPCCConst.numINILastName,TPCCConst.numENDLastName));
                         occur = (String) ptrCustomerLastNameHash[contd -
                                 1].get(lastname);
                         if (occur != null)
                         {
                             nValue = Integer.parseInt(occur) + 1;
                             ptrCustomerLastNameHash[contd -
                                                     1].put(lastname,Integer.toString(nValue));
                         }
                         else
                         {
                             ptrCustomerLastNameHash[contd -
                                                     1].put(lastname,Integer.toString(1));
                         }
 
                         nValue = rangeCustomerLastName.indexOf(lastname);
                         if (nValue == -1)
                         {
                             rangeCustomerLastName.add(lastname);
                         }
                         contc++;
                     }
                     contd++;
                 }
                 contw++;
             }
         }
     }
 
 
     /**
      * It instantiates the virtual database and populates it according to the number of clients. 
      * 
      *
      * @param totcli the total number of clients which will be accessing the database.
      *
 * @throws InvalidDatabaseException The following pre-condition must evaluate to <code>true</code>: totcli % 10 = 0.
      * If the expression holds the method can proceed otherwise an expection <code>InvalidDatabaseException</code>
      * is thrown. See <a href="Escada.tpc.common.database#InvalidDatabaseException">InvalidDatabaseException</a>
 *
      **/
     public dbVirtualDatabase(int totcli) throws InvalidDatabaseException{
         if (ptrClass == null) {
	    //TODO: See this block, if it is necessary.
	    //if ((totcli % 10) != 0) throw new InvalidDatabaseException("Wrong number of clients, \"totcli % 10 != 0\".");
             ptrClass = new persistentObjects(totcli);
         }
     }
 
     /**
 * It returns the current neworder for a given warehouse and district. Note that the return is just the <code>neworder</code> element.
 * As a pattern, we assume that the warehouse <code>wid</code> and the district <code>did</code> are not returned as part of the information when supplied as parameters.
 * 
 * @param wid the warehouse number specified as <code>String</code>
 * @param did the district number specified as <code>String</code>
 *
 * @return the current neworder specified as <code>String</code>. 
 *
 * @throws InvalidDatabaseParameterException If an input error is found this exception is raised which means that
 * the warehouse or the district parameter is invalid. See <a href="Escada.tpc.common.database#InvalidDatabaseParameterException">InvalidDatabaseParameterException</a>.
 **/
     public String getCurrentNewOrder(String wid, String did) throws InvalidDatabaseParameterException {
         String getCurrentNewOrder = null;
         Hashtable ptrNewOrderHash = null;
 
         if ((Integer.parseInt(wid) <= ptrClass.nWareHouse) && (Integer.parseInt(wid) != 0) && (Integer.parseInt(did) <= TPCCConst.rngDistrict) && (Integer.parseInt(did) != 0)) {
         	ptrNewOrderHash = (Hashtable) ptrClass.neworder.get(wid);
         	getCurrentNewOrder =  (String) ptrNewOrderHash.get(did);
 	}
 	else throw new InvalidDatabaseParameterException();
         
         return (getCurrentNewOrder);
     }
 
     /**
 * It returns a set of current neworders for a given warehouse and its districts. Note that the return combines the warehouse <code>wid</code>, district <code>id</code> and the <code>neworder</code> element.
 * As a pattern, we assume that the warehouse <code>wid</code> and the district <code>did</code> are returned as part of the information when one of them is not supplied as parameter.
 * 
 * @param wid the warehouse number specified as <code>String</code>
 *
 * @return the set of current neworders, for a given warehouse and its districts, specified as a <code>HashSet</code>. 
 *
 * @throws InvalidDatabaseParameterException If an input error is found this exception is raised which means that
 * the warehouse parameter is invalid. See <a href="Escada.tpc.common.database#InvalidDatabaseParameterException">InvalidDatabaseParameterException</a>.
 **/
     public HashSet getCurrentNewOrder(String wid) throws InvalidDatabaseParameterException {
         int cont = 1;
         HashSet dbtrace = null;
         Hashtable ptrNewOrderHash = null;
 
        if ((Integer.parseInt(wid) <= ptrClass.nWareHouse) && (Integer.parseInt(wid) != 0)) {
         	ptrNewOrderHash = (Hashtable) ptrClass.neworder.get(wid);
 	        dbtrace = new HashSet();
 
 	        while (cont <= TPCCConst.rngDistrict) {
         	    dbtrace.add(wid + Integer.toString(cont) +
                 	        (String) ptrNewOrderHash.get(Integer.toString(cont)));
 	            cont++;
         	}
 	}
 	else throw new InvalidDatabaseParameterException();
         return (dbtrace);
     }
 
 
     /**
 * It returns the current order for a given warehouse and district. Note that the return is just the <code>order</code> element.
 * As a pattern, we assume that the warehouse <code>wid</code> and the district <code>did</code> are not returned as part of the information when supplied as parameters.
 * 
 * @param wid the warehouse number specified as <code>String</code>
 * @param did the district number specified as <code>String</code>
 *
 * @return the current order specified as <code>String</code>. 
 *
 * @throws InvalidDatabaseParameterException If an input error is found this exception is raised which means that
 * the warehouse or the district parameter is invalid. See <a href="Escada.tpc.common.database#InvalidDatabaseParameterException">InvalidDatabaseParameterException</a>.
 **/
     public String getCurrentOrders(String wid, String did) throws InvalidDatabaseParameterException {
 	String getCurrentOrders = null;
         Hashtable ptrOrdersHash = null;
 
 	if ((Integer.parseInt(wid) <= ptrClass.nWareHouse) && (Integer.parseInt(wid) != 0) && (Integer.parseInt(did) <= TPCCConst.rngDistrict) && (Integer.parseInt(did) != 0)) {
         	 ptrOrdersHash = (Hashtable) ptrClass.orders.get(wid);
 		 getCurrentOrders = (String) ptrOrdersHash.get(did);
 	}
 	else throw new InvalidDatabaseParameterException();
 
 	return (getCurrentOrders);
     }
 
     /**
 * It returns a set of current neworders for a given warehouse and its districts for a customer randomly generated for each district. Note that the return combines the warehouse <code>wid</code>, district <code>id</code> and the <code>neworder</code> element.
 * As a pattern, we assume that the warehouse <code>wid</code> and the district <code>did</code> are returned as part of the information when one of them is not supplied as parameter.<p>
 * The following statement regards anyone that has access to the code. If it is not the case, please ignore it. <p>
 * It may seem a little bit strange for one looking at the code, while seeing the neworder element returned as the customer id. For us, this is acceptable because
 * our interesing is to retrieve a unique neworder associated to the client.
 * 
 * @param wid the warehouse number specified as <code>String</code>
 *
 * @return the set of current neworders, for a given warehouse and its districts for a customer randomly generated for each district, specified as a <code>HashSet</code>. 
 *
 * @throws InvalidDatabaseParameterException If an input error is found this exception is raised which means that
 * the warehouse parameter is invalid. See <a href="Escada.tpc.common.database#InvalidDatabaseParameterException">InvalidDatabaseParameterException</a>.
 **/
     public HashSet getCustomerNewOrder(String wid) throws InvalidDatabaseParameterException {
         HashSet dbtrace = null;
         int cont = 1;
         int cid = 0;
 
 	if ((Integer.parseInt(wid) <= ptrClass.nWareHouse) && (Integer.parseInt(wid) != 0)) {
         	dbtrace = new HashSet();
 	        while (cont <= TPCCConst.rngDistrict) {
         	    cid = RandGen.NURand(rand,
                 	                 TPCCConst.CustomerA, TPCCConst.numINICustomer,
                         	         TPCCConst.numENDCustomer);
 
 	            dbtrace.add(wid + Integer.toString(cont) + Integer.toString(cid));
         	    cont++;
 	        }
 	}
 	else throw new InvalidDatabaseParameterException();
         return (dbtrace);
     }
 
     /**
 * It returns a set of current neworder lines for a given warehouse and its districts. Note that the return combines the warehouse <code>wid</code>, district <code>id</code> and the <code>neworder</code> element.
 * As a pattern, we assume that the warehouse <code>wid</code> and the district <code>did</code> are returned as part of the information when one of them is not supplied as parameter.<p>
 *  
 * @param wid the warehouse number specified as <code>String</code>
 *
 * @return the set of current neworder lines, for a given warehouse and its districts, specified as a <code>HashSet</code>. 
 *
 * @throws InvalidDatabaseParameterException If an input error is found this exception is raised which means that
 * the warehouse parameter is invalid. See <a href="Escada.tpc.common.database#InvalidDatabaseParameterException">InvalidDatabaseParameterException</a>.
 **/
     public HashSet getOrderLineNewOrder(String wid) throws InvalidDatabaseParameterException {
         HashSet dbtrace = null;
         int cont = 1;
         int qtd = RandGen.nextInt(rand, TPCCConst.qtdINIItem,
                                   TPCCConst.qtdENDItem + 1);
 
 	if ((Integer.parseInt(wid) <= ptrClass.nWareHouse) && (Integer.parseInt(wid) != 0)) {
 	        dbtrace = new HashSet();
 	        while (cont <= TPCCConst.rngDistrict) {
         	    int qtdcont = 1;
 	            while (qtdcont <= qtd) {
         	        dbtrace.add(wid + Integer.toString(cont) +
                 	            getCurrentNewOrder(wid, Integer.toString(cont)) +
                         	    Integer.toString(qtdcont));
 	                qtdcont++;
         	    }
 	            cont++;
         	}
 	}
 	else throw new InvalidDatabaseParameterException();
         return (dbtrace);
     }
 
 
     /**
 * It returns the current order for a given warehouse, district and customer. Note that the return combines the warehouse <code>wid</code>, district <code>id</code> and the <code>order</code> element.
 * This method does not follow our pattern which implies that the order element must not include the warehouse and district ids.<p>
 * The following statement regards anyone that has access to the code. If it is not the case, please ignore it. <p>
 * It may seem a little bit strange for one looking at the code, while seeing the order element returned as the customer id. For us, this is acceptable because
 * our interesing is to retrieve a unique order associated to the client.
 * 
 * @param wid the warehouse number specified as <code>String</code>
 *
 * @return the current order, for a given warehouse, district and customer, specified as a <code>HashSet</code>. 
 *
 * @throws InvalidDatabaseParameterException If an input error is found this exception is raised which means that
 * the warehouse parameter is invalid. See <a href="Escada.tpc.common.database#InvalidDatabaseParameterException">InvalidDatabaseParameterException</a>.
 **/
     public HashSet getCustomerOrders(String wid, String did, String cid) throws InvalidDatabaseParameterException {
         HashSet dbtrace = null;
 	if ((Integer.parseInt(wid) <= ptrClass.nWareHouse) && (Integer.parseInt(wid) != 0) && (Integer.parseInt(did) <= TPCCConst.rngDistrict) && (Integer.parseInt(did) != 0)) {
 	        dbtrace = new HashSet();
 	        dbtrace.add(wid + did + cid);
 	}
 	else throw new InvalidDatabaseParameterException();
         return (dbtrace);
     }
 
 /**
  It inserts an order for a given warehouse and district. It may seem a little bit strange since the customer is not provided in order
 * to register the entries. For us, this is acceptable because our interesing is to compute that the district has a neworder. Moreover,
 * to retrive the current order for a give client, we simply retrieve the current order for the district and append the client identfication
 * as described in <a href="#getCustomerNewOrder">getCustomerNewOrder</a> and <a href="#getCustomerOrders">getCustomerOrders</a>.
 * 
 * @param wid the warehouse number specified as <code>String</code>
 * @param did the district number specified as <code>String</code>
 *
 * @throws InvalidDatabaseParameterException If an input error is found this exception is raised which means that
 * the warehouse or the district parameter is invalid. See <a href="Escada.tpc.common.database#InvalidDatabaseParameterException">InvalidDatabaseParameterException</a>.
 **/
     public void insertNewOrder(String wid, String did) throws InvalidDatabaseParameterException {
         Hashtable ptrNewOrderHash = null;
 
 	if ((Integer.parseInt(wid) <= ptrClass.nWareHouse) && (Integer.parseInt(wid) != 0) && (Integer.parseInt(did) <= TPCCConst.rngDistrict) && (Integer.parseInt(did) != 0)) {
 
 	        ptrNewOrderHash = (Hashtable) ptrClass.neworder.get(wid);
 
         	int n = Integer.parseInt( (String) ptrNewOrderHash.get(did));
 	        n++;
         	ptrNewOrderHash.put(did, Integer.toString(n));
 	} 
 	else throw new InvalidDatabaseParameterException();
     }
 
 /**
  It inserts an order for a given warehouse and its districts. It may seem a little bit strange since the customer is not provided in order
 * to register the entries. For us, this is acceptable because our interesing is to compute that the district has a neworder. Moreover,
 * to retrive the current order for a give client, we simply retrieve the current order for the district and append the client identfication
 * as described in <a href="#getCustomerNewOrder">getCustomerNewOrder</a> and <a href="#getCustomerOrders">getCustomerOrders</a>.
 * 
 * @param wid the warehouse number specified as <code>String</code>
 *
 * @throws InvalidDatabaseParameterException If an input error is found this exception is raised which means that
 * the warehouse parameter is invalid. See <a href="Escada.tpc.common.database#InvalidDatabaseParameterException">InvalidDatabaseParameterException</a>.
 **/
     public void insertNewOrder(String wid) throws InvalidDatabaseParameterException {
        	int n = 0;
        	int cont = 1;
         Hashtable ptrNewOrderHash = null;
 
 	if ((Integer.parseInt(wid) <= ptrClass.nWareHouse) && (Integer.parseInt(wid) != 0)) {
 
         	ptrNewOrderHash = (Hashtable) ptrClass.neworder.get(wid);
 
         	while (cont <= TPCCConst.rngDistrict) {
 	            n = Integer.parseInt( (String) ptrNewOrderHash.get(Integer.toString(cont)));  
 		    n++;
 	            ptrNewOrderHash.put(Integer.toString(cont), Integer.toString(n));
         	    cont++;
 	        }
 	}
 	else throw new InvalidDatabaseParameterException();
     }
 
 /**
  It inserts an order for a given warehouse and district. It may seem a little bit strange since the customer is not provided in order
 * to register the entries. For us, this is acceptable because our interesing is to compute that the district has a neworder. Moreover,
 * to retrive the current order for a give client, we simply retrieve the current order for the district and append the client identfication
 * as described in <a href="#getCustomerNewOrder">getCustomerNewOrder</a> and <a href="#getCustomerOrders">getCustomerOrders</a>.
 * 
 * @param wid the warehouse number specified as <code>String</code>
 * @param did the district number specified as <code>String</code>
 *
 * @throws InvalidDatabaseParameterException If an input error is found this exception is raised which means that
 * the warehouse or the district parameter is invalid. See <a href="Escada.tpc.common.database#InvalidDatabaseParameterException">InvalidDatabaseParameterException</a>.
 **/
     public void insertOrders(String wid, String did) throws InvalidDatabaseParameterException {
         Hashtable ptrOrdersHash = null;
 
         if ((Integer.parseInt(wid) <= ptrClass.nWareHouse) && (Integer.parseInt(wid) != 0) && (Integer.parseInt(did) <= TPCCConst.rngDistrict) && (Integer.parseInt(did) != 0)) {
         	ptrOrdersHash = (Hashtable) ptrClass.orders.get(wid);
 
 	        int n = Integer.parseInt((String) ptrOrdersHash.get(did));   
 		n++;
 	        ptrOrdersHash.put(did, Integer.toString(n));
 	}
 	else throw new InvalidDatabaseParameterException();
     }
 
 /**
 * It returns the latest orders for a given warehouse and its districts. 
 * 
 * @param wid the warehouse number specified as <code>String</code>
 *
 * @return the latest orders, for a given warehouse, specified as a <code>HashSet</code>.
 *
 * @throws InvalidDatabaseParameterException If an input error is found this exception is raised which means that
 * the warehouse parameter is invalid. See <a href="Escada.tpc.common.database#InvalidDatabaseParameterException">InvalidDatabaseParameterException</a>.
 **/
     public HashSet getNewOrder(String wid) throws InvalidDatabaseParameterException {
         int contdis = 1;
         HashSet dbtrace = null;
         Hashtable ptrOrdersHash = null;
         Hashtable ptrNewOrderHash = null;
 
 	if ((Integer.parseInt(wid) <= ptrClass.nWareHouse) && (Integer.parseInt(wid) != 0)) {
 	        dbtrace = new HashSet();
 	        ptrOrdersHash = (Hashtable) ptrClass.orders.get(wid);
 	        ptrNewOrderHash = (Hashtable) ptrClass.neworder.get(wid);
         	while (contdis <= TPCCConst.rngDistrict) {
 	            int contini = Integer.parseInt((String)ptrNewOrderHash.get(Integer.toString(contdis)));
         	    int contend = Integer.parseInt((String)ptrOrdersHash.get(Integer.toString(contdis)));
 
 	            while (contini <= contend)
         	    {
                 	dbtrace.add(wid + contdis + Integer.toString(contini));
 	                contini++;
         	    }
 	            contdis++;
 		}
         }
 	else throw new InvalidDatabaseParameterException();
         return (dbtrace);
     }
 
 /**
 * It returns the latest orders for a given warehouse and district.  Note that the return combines the warehouse <code>wid</code>, district <code>id</code> and the <code>order</code> element.
 * This method does not follow our pattern which implies that the order element must not include the warehouse and district ids.<p>
 * 
 * @param wid the warehouse number specified as <code>String</code>
 * @param did the district number specified as <code>String</code>
 *
 * @return the latest orders, for a given warehouse and district, specified as a <code>HashSet</code>.
 *
 * @throws InvalidDatabaseParameterException If an input error is found this exception is raised which means that
 * the warehouse parameter is invalid. See <a href="Escada.tpc.common.database#InvalidDatabaseParameterException">InvalidDatabaseParameterException</a>.
 **/
     public HashSet getOrderLineStockLevel(String wid, String did) throws InvalidDatabaseParameterException {
         int oldorders = 0; 
         int curorders = 0;
         HashSet dbtrace = null;
 
 	 if ((Integer.parseInt(wid) <= ptrClass.nWareHouse) && (Integer.parseInt(wid) != 0) && (Integer.parseInt(did) <= TPCCConst.rngDistrict) && (Integer.parseInt(did) != 0)) {
 		oldorders = Integer.parseInt(getCurrentOrders(wid, did)) - 20; //TODO: TPCCConst
 		curorders = Integer.parseInt(getCurrentOrders(wid, did));
 		dbtrace = new HashSet();
 
          	while (oldorders < curorders) {
 			int qtd = RandGen.nextInt(rand, TPCCConst.qtdINIItem,TPCCConst.qtdENDItem + 1);
 			int contqtd = 1;
 		        while (contqtd <= qtd) {
 		                dbtrace.add(wid + did + oldorders + contqtd);
                 		contqtd++;
 		        }
 			oldorders++;
         	}
 	}
 	else throw new InvalidDatabaseParameterException();
         return (dbtrace);
     }
 
 /**
 * It returns the items in stock that are bellow an specified threshold for a given warehouse and district.  Note that the return combines the warehouse <code>wid</code>, district <code>id</code> and the <code>item</code> element.
 * This method does not follow our pattern which implies that the order element must not include the warehouse and district ids.<p>
 * 
 * @param wid the warehouse number specified as <code>String</code>
 * @param did the district number specified as <code>String</code>
 *
 * @return the returns the items in stock that are bellow an specified threshold, for a given warehouse and district, specified as a <code>HashSet</code>.
 *
 * @throws InvalidDatabaseParameterException If an input error is found this exception is raised which means that
 * the warehouse parameter is invalid. See <a href="Escada.tpc.common.database#InvalidDatabaseParameterException">InvalidDatabaseParameterException</a>.
 **/
     public HashSet getStockLevel(String wid, String did) throws InvalidDatabaseParameterException {
         int oldorders = 0;
         int curorders = 0;
         HashSet dbtrace = null;
 
 	if ((Integer.parseInt(wid) <= ptrClass.nWareHouse) && (Integer.parseInt(wid) != 0) && (Integer.parseInt(did) <= TPCCConst.rngDistrict) && (Integer.parseInt(did) != 0)) {
         	oldorders = Integer.parseInt(getCurrentOrders(wid, did)) - 20; //TODO: TPCCConst
 	        curorders = Integer.parseInt(getCurrentOrders(wid, did));
 	        dbtrace = new HashSet();
 
 	        while (oldorders < curorders) {
         	    int qtd = RandGen.nextInt(rand, TPCCConst.qtdINIItem,
                                       TPCCConst.qtdENDItem + 1);
 	            int contqtd = 1;
 
         	    while (contqtd <= qtd) {
                 	int iid = RandGen.NURand(rand, TPCCConst.iidA, TPCCConst.numINIItem,
                         	                 TPCCConst.numENDItem);
 	                dbtrace.add(wid + iid);
         	        contqtd++;
 	            }
         	    oldorders++;
 		}
         }
 	else throw new InvalidDatabaseParameterException();
         return (dbtrace);
     }
 
 /**
 * It returns a set of customer for given warehouse and district that matches the pattern specified by the parameter lastName. Note that the return combines the warehouse <code>wid</code>, district <code>id</code> and the <code>lastName</code> element.
 * This method does not follow our pattern which implies that the order element must not include the warehouse and district ids.<p>
 * 
 * @param wid the warehouse number specified as <code>String</code>
 * @param did the district number specified as <code>String</code>
 * @param lastName the last name  specified as <code>String</code>
 *
 * @return the latest orders, for a given warehouse, specified as a <code>HashSet</code>.
 *
 * @throws InvalidDatabaseParameterException If an input error is found this exception is raised which means that
 * the warehouse parameter is invalid. See <a href="Escada.tpc.common.database#InvalidDatabaseParameterException">InvalidDatabaseParameterException</a>.
 **/
     public HashSet getCustomerLastName(String wid, String did, String lastname) throws InvalidDatabaseParameterException  {
        	Hashtable ptrCustomerLastNameHash [] = null;
         HashSet dbtrace = null;
        	String occur = null;
 
 	if ((Integer.parseInt(wid) <= ptrClass.nWareHouse) && (Integer.parseInt(wid) != 0) && (Integer.parseInt(did) <= TPCCConst.rngDistrict) && (Integer.parseInt(did) != 0)) {
         	ptrCustomerLastNameHash = (Hashtable []) ptrClass.customerLastName.get(wid);
 	        dbtrace = new HashSet();
         	occur = (String) ptrCustomerLastNameHash[Integer.parseInt(did) - 1].get(lastname);
 
 	        if (occur != null) {
         	    int contini = 1;
 	            int contend = Integer.parseInt(occur);
 	            int range = ptrClass.rangeCustomerLastName.indexOf(lastname) * 1000; //TODO: This operation could use bit comparison to speed up the search process.
         	    int nValue = 0;
 	            while (contini <= contend)
         	    {
 			nValue = contini + range;
 	                dbtrace.add(wid + did + nValue);
         	        contini++;
 	            }
 	        }
 	} 
 	else throw new InvalidDatabaseParameterException();
 
         return (dbtrace);
     }
 }
 
 // arch-tag: d254f0e6-c3d8-41a2-8857-5bdc06efd581
