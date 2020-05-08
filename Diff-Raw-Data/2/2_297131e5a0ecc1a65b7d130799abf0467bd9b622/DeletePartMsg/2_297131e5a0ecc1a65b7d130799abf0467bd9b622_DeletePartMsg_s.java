 import java.io.*;
 
 /** networking message indicating to delete a part */
 public class DeletePartMsg implements Serializable {
 	/** part number of part to delete */
 	public int number;
	/** constructor to set up NewPartMsg with the number of part that should be deleted */
 	public DeletePartMsg(int delNumber) {
 		number = delNumber;
 	}
 }
