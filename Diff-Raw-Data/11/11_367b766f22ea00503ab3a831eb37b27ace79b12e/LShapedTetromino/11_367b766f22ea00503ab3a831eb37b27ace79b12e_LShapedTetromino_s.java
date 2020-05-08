 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 
 public class LShapedTetromino extends Tetromino {
 	
 	public LShapedTetromino(){
 		blockPositions = new ArrayList<Integer>();
 		Integer[] positions = {300, 301, 400, 500};
 		for(int i = 0; i < 4; i++){
 			blockPositions.add(positions[i]);
 		}
 		currentRotation = 0;
 	}
 	
 	@Override
 	public void rotate(Map<Integer, Boolean> grid) {
 		Integer center;
 		List<Integer> newPositions = new ArrayList<Integer>();
 		if(currentRotation == 0){
 			center = blockPositions.get(2);
 			newPositions.add(center - 100 - 1);
 			newPositions.add(center - 1);
 			newPositions.add(center);
 			newPositions.add(center + 1);
 			currentRotation = 1;
 		}else if(currentRotation == 1){
 			center = blockPositions.get(2);
 			newPositions.add(center - 100);
 			newPositions.add(center);
 			newPositions.add(center + 100 - 1);
 			newPositions.add(center + 100);
 			currentRotation = 2;
 		}else if(currentRotation == 2){
 			center = blockPositions.get(1);
 			newPositions.add(center - 1);
 			newPositions.add(center);
 			newPositions.add(center + 1);
 			newPositions.add(center + 100 + 1);
 			currentRotation = 3;
 		}else{
 			center = blockPositions.get(1);
 			newPositions.add(center - 100);
			newPositions.add(center - 100 - 1);	
 			newPositions.add(center);
 			newPositions.add(center + 100);
 			currentRotation = 0;
 		}
 		if(checkLegalMove(newPositions, grid)){
 			blockPositions = newPositions;
 		}else{
 			newPositions = wallKick(newPositions, grid);
 			if(checkLegalMove(newPositions, grid)){
 				blockPositions = newPositions;
 			}
 		}
 	}
 }
