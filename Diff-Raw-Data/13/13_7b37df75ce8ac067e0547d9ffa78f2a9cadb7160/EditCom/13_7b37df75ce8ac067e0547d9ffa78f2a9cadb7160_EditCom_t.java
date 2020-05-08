 package local.texteditor;
 
 import local.texteditor.MovesProtos.Move;
 
 
 public class EditCom
 {
   User.Operation operation;
   public int offset;
   public String mes;
   
   public EditCom(User.Operation operationT, String mesT, int offsetT)
   {
     operation = operationT;
     mes = mesT;
     offset = offsetT;  
   }
   
   //create a move in the protocol buffer format
   //to be sent to the Collabrify Client
   public Move generateMoveMes(int undo)
   {
 		Move move;
 		if (undo != 1 && this.operation == User.Operation.ADD || 
 			(undo == 1 && this.operation == User.Operation.DELETE)) 
 		//either an add, or an undo on a delete
 		{
 			move = Move.newBuilder()
 					.setUserId(User.Id) 
 					.setMoveType(1)
 					.setData(this.mes)
 					.setCursorChange(this.offset)
 					.setUndo(undo)
 					.build();
 		}
 		else if(undo != 1 && this.operation == User.Operation.DELETE || 
 				(undo == 1 && this.operation == User.Operation.ADD))
 		  //either a delete, or an undo on an add
 		{
 			move = Move.newBuilder()
 					.setUserId(User.Id)
 					.setMoveType(2)
					.setData(this.mes)
 					.setCursorChange(this.offset)
 					.setUndo(undo)
 					.build();
 		}
 		else //a cursor move
 		{
 			if (undo == 1)
 			{
 				move = Move.newBuilder()
 						.setUserId(User.Id)
 						.setMoveType(3)
 						.setCursorChange(-this.offset)
 						.setUndo(undo)
 						.build();
 			}
 			else
 			{
 				move = Move.newBuilder()
 				.setUserId(User.Id) //need a user id
 				.setMoveType(3)
 				.setCursorChange(this.offset)
 				.setUndo(undo)
 				.build();
 			}
 		}
 		return move;  
   }
  }
