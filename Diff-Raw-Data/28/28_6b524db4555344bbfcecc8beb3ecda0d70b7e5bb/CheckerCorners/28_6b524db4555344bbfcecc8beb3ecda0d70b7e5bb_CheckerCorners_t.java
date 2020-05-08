 package net.mydebug.corners.drive.figures;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import net.mydebug.corners.drive.ChessGame;
 
 public class CheckerCorners extends Checker {
 
 	private List<Position>   turns;
 	ArrayList<Position>      possiblePositions    = new ArrayList<Position>();
 	ArrayList<MoveLine>      possibleMovesLines   = new ArrayList<MoveLine>();
     ArrayList<Position>      alreadyChecked       = new ArrayList<Position>();
     MoveLine                 tmpMoveLineFullField;
 
 
 	public CheckerCorners(  ) {
 
     }
 
 	public CheckerCorners(int color , int x , int y , ChessGame board ) {
 		super(color , x , y , board );
 	}
 	
 	// Генерируем turns - куда может ходить шашка (позиции)
	private List<Position> generateTurnsByPosition( Position position ) {
        List<Position>   turns = new ArrayList<Position>();
 		turns.add( new Position( position.x , position.y + 1 ) );
 		turns.add( new Position( position.x , position.y - 1 ) );
 		turns.add( new Position( position.x - 1 , position.y ) );
 		turns.add( new Position( position.x + 1 , position.y ) );
        return turns;
 	}
 	
 	//рекурсивно проверяем возможные ходы фигуры по заданному направлению
 	public void checkTurnNextLevel( Position position  , MoveDirection direction  ) {		
 //		изменяем проверочную позицию по направлению, получаем поле через занятую клетку
 		int tmpX = position.x;
 		int tmpY = position.y;
 		if( direction.direction == DIRECTION_X ) {
 			position.x = position.x + direction.value ;
 		} 
 		if( direction.direction == DIRECTION_Y ) {
 			position.y = position.y + direction.value ;	
 		}
 		int empty = ChessGame.checkFieldIsEmpty( position );
 
 		// Проверка предотвращающая закольцованность ходов. 
 		// Если ход еще не проверяли - добавляем в список
         for (Position anAlreadyChecked : alreadyChecked) {
             if (position.x == anAlreadyChecked.x && position.y == anAlreadyChecked.y) {
                 return;
             }
         }
 		alreadyChecked.add( position );
 		// Если поле свободно - добавляем его в возможные ходы и проверяем 3 направления (на возможность шагнуть еще через какую-то фигуру
 		if( empty == 1 ) {
 			
 			possiblePositions.add( position );
			if( tmpMoveLineFullField != null )
 				possibleMovesLines.add( tmpMoveLineFullField );
             tmpMoveLineFullField = null;
 			possibleMovesLines.add( new MoveLine( tmpX , tmpY , position.x , position.y ) );
 			List<Position> directPos = new ArrayList<Position>();
 			List<MoveDirection> directions = new ArrayList<MoveDirection>();
 			
 			// Определяем какие 3 клетки нам еще стоит проверить (соседние клетки, но не ту, откуда пришли)
 			
 			if( direction.direction == DIRECTION_Y ) {
 				if( direction.value == 1 ) {
 					directPos.add(new Position( position.x , position.y  + 1));
 					directions.add(new MoveDirection( DIRECTION_Y , 1 ) );
 				}
 				else {
 					directPos.add(new Position( position.x , position.y - 1));	
 					directions.add(new MoveDirection( DIRECTION_Y , -1 ) );
 				}
 				directPos.add(new Position( position.x + 1 , position.y ));
 				directions.add(new MoveDirection( DIRECTION_X , 1 ) );
 				directPos.add(new Position( position.x - 1, position.y  ));
 				directions.add(new MoveDirection( DIRECTION_X , -1 ) );
 			} 
 			if( direction.direction == DIRECTION_X) {
 				if( direction.value == 1 ) {
 					directPos.add(new Position( position.x + 1, position.y  ));
 					directions.add(new MoveDirection( DIRECTION_X , 1 ) );
 				}
 				else {
 					directPos.add(new Position( position.x - 1, position.y ));
 					directions.add(new MoveDirection( DIRECTION_X , -1 ) );
 				}
 				directPos.add(new Position( position.x , position.y  + 1));
 				directions.add(new MoveDirection( DIRECTION_Y , 1 ) );
 				directPos.add(new Position( position.x , position.y - 1 ));
 				directions.add(new MoveDirection( DIRECTION_Y , -1 ) );
 			}
 			// запускаем проверку для этих 3 клеток
 			for( int i = 0 ; i < directions.size() ; i++ ) {
 				if( ChessGame.checkFieldIsEmpty( directPos.get(i) ) == 0 ) {
 					// tmpMoveLineFullField - темповая переменная, в которая траэкторию прохода пропускаемого поля
 					tmpMoveLineFullField = new MoveLine( position.x, position.y, directPos.get(i).x, directPos.get(i).y );
 					checkTurnNextLevel( directPos.get(i)  , directions.get(i)  );
 				}	
 			}
 		} 
 	}
 
 	
 
 	@Override
 	public List<Position> getAviableMoves() {
        // переменные для результата
 		possiblePositions  = new ArrayList<Position>();
 		possibleMovesLines = new ArrayList<MoveLine>();
        // массив клеток, в которые мы уже проверяли (защита от зацикливания)
 		alreadyChecked     = new ArrayList<Position>();
        // генерируем возможные ходы для этой фигуры - x+1;y , x;y+1, x-1;y , x;y-1.
        List<Position>   turns = generateTurnsByPosition( getPosition() );
 		alreadyChecked.add( new Position( this.x, this.y ) );
         for (Position turn : turns) {
             int isEmpty = ChessGame.checkFieldIsEmpty(turn);
            //если в поле пусто добавляем возможность хода на него
             if (isEmpty == 1) {
                 possiblePositions.add(turn);
                 possibleMovesLines.add(new MoveLine(this.x, this.y, turn.x, turn.y));
                 // проверяем можем ли мы перешагнуть через занятую клетку
             } else if (isEmpty == 0) {
                 int direction;
                 int value;
                 //определяем направление, по которому надо проверить возможные ходы
                 // tmpMoveLineFullField - темповая переменная, в которая траэкторию прохода пропускаемого поля
                 if (turn.x == this.x) {
                     value = -(this.y - turn.y);
                     direction = DIRECTION_Y;
                     tmpMoveLineFullField = new MoveLine(this.x, this.y, turn.x, turn.y);
                 } else {
                     value = -(this.x - turn.x);
                     direction = DIRECTION_X;
                     tmpMoveLineFullField = new MoveLine(this.x, this.y, turn.x, turn.y);
                 }
                 // рекурсивно проверяем можем ли мы перешагнуть через занятую клетку
                 checkTurnNextLevel(turn, new MoveDirection(direction, value));
             }
         }
 		return possiblePositions;
 	}
 
 	@Override
 	public ArrayList<MoveLine> getAviableDirectionsLines() {
 		
 		
 		return possibleMovesLines;
 	}
 
 	
 
 
 }
 
