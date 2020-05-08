 package businessLogic_layer;
 
 import dao_interfaces.DALException;
 
 public interface IFunctionality {
 	String generatePassword();
 	void askForOprName();
 	boolean testId(String i) throws DALException;
 	boolean testPbId(int i) throws DALException;
 	boolean testRaavareBatchId(int i) throws DALException;
 	boolean testPassword(int i, String s) throws DALException;
	boolean askForNewPassword(int i, String s) throws DALException;
 	boolean checkIfIdentical(String s, String k);
 	double calculateWeight(double tare, double brutto);
 }
