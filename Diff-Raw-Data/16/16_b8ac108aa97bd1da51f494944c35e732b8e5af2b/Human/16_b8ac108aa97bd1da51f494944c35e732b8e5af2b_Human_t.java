 package onshogi;
 
 import java.awt.Point;
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.util.Scanner;
 
 /**
  * 人からの入力をAdministratorに仲介するクラス.
  * Administratorから手の入力を促される．nextMoveで答える．
  * 人からの入力を構文解析し，Moveとして変換してAdministratorに返す．
  * 構文解析ではStateの合法判定メソッド(isLegalMove)を用いて，
  * 違法手の場合は再び入力を促す．
  * @author oshun119
  * @author nisshy
  */
 public class Human extends Player {
 
 	public Human(boolean isBlack) {
 		super(isBlack);
 	}
 
 	@Override
 	public Move nextMove(State currentState) {
 		Move move = null;
 		
 		while(true){
 			String teban = isBlack ? "先手":"後手";
 			System.out.println(teban + "の手番です 手を入力して下さい >");
 			
 			BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
 			String te = null;
 			try {
 				te = input.readLine();
 			} catch(Exception e) {
 				System.out.println("入力が正しくありません: "+e);
 			}
 			
 			/*
 			try{
 				Scanner scan = new Scanner(System.in);
 				te = scan.next();
 			} catch (java.util.InputMismatchException e) {
 				System.out.println("入力が正しくありません: "+e);
 				continue;
 			}*/
 			
 			
 			if(te.length() != 4 && te.length() != 5) {
 				System.out.println("入力が正しくありません: 4~5文字ではない");
 				continue;
 			}
 			
 			char piece[] = te.toCharArray();
 			
 			/* 手 abcdとあったとき,
 			 * abが
 			 * 01 歩
 			 * 02 香車
 			 * 03 桂馬
 			 * 04 銀
 			 * 05 金
 			 * 06 飛車
 			 * 07 角
 			 */
 			//打つ処理
 			if(piece[0] == '0') {
 				Piece koma;
 				switch(piece[1]){
 					case '1':
 						koma = new Fu(this.isBlack);
 						break;
 					case '2':
 						koma = new Kyosha(this.isBlack);
 						break;
 					case '3':
 						koma = new Keima(this.isBlack);
 						break;
 					case '4':
 						koma = new Gin(this.isBlack);
 						break;
 					case '5':
 						koma = new Kin(this.isBlack);
 						break;
 					case '6':
 						koma = new Hisha(this.isBlack);
 						break;
 					case '7':
 						koma = new Kaku(this.isBlack);
 						break;
 					default:
 						System.out.println("入力が正しくありません: 0,8,9の打ち駒は存在しない");
 						continue;
 				}
 				
 				move = new Move(new Point(piece[2]-'0', piece[3]-'0'), koma);
 			} 
 			//打たない
 			else {
 				boolean flag = false;
 				if(piece.length == 5){
					if(piece[4] == '*') {
 						flag = true;
 					}else{
 						System.out.println("入力が正しくありません: 5文字目は*のみです");
 						continue;
 					}
 				}
				move = new Move(new Point(piece[0]-'0', piece[1]-'0'),new Point(piece[2]-'0', piece[3]-'0'), flag);
 			}
 		
 			if(!currentState.isLegalMove(move, this.isBlack)){
 				System.out.println("不正な手です: もう一度入力して下さい");
 				continue;
 			}
 			return move;
 		}
 		
 	}
 
 	@Override
 	public String toString() {
 		return "YOU";
 	}
 }
