 package Overview;
 
 import Character.Character;
 import Utilities.Pair;
 
 public class BoardPosition implements Comparable<BoardPosition>
 {
 	/**
 	 * Matriz e suas posicoes
 	 * 
 	 * 0 1 2
 	 * 3 4 5
 	 */
 	
 	private int position; //posicao na matriz, usado para deixar o set em ordem
 	private int mod;  		//largura do tabuleiro, usado pra encontrar o pos a partir de X e Y
 	//private int x, y;		//posicao real do personagem em uma matriz
 	private Pair<Integer, Integer> XY;
 	//personagem que ocupa a posicao x, y
 	private Character occupied;
 	
 	/**
 	 * 
 	 * @param pCharacter Character que ocupara a posicao
 	 * @param larguraBoard	Largura do tabuleiro (para calculo exato da posicao do personagem na matriz
 	 * @param pos Posicao na matriz
 	 */
 	public BoardPosition(Character pCharacter, int larguraBoard, int pos)
 	{	
		XY = new Pair<Integer, Integer>(pos/larguraBoard, pos%larguraBoard);
 		position = pos;
 		mod = larguraBoard;
 		
 		//Personagem que ocupa essa posicao
 		occupied = pCharacter;
 	}	
 
 	/**
 	 * 
 	 * @param pos posicao na matriz do tabuleiro
 	 */
 	public void setPos(int pos)
 	{
 		XY.setFirst(pos%mod);
 		XY.setSecond(pos/mod);
 		position = pos;
 		
 	}
 	
 	public int getPos()
 	{
 		return position;
 	}
 	
 	/**
 	 * 
 	 * @return pair de <Integer, Integer> com posicao X e Y
 	 */
 	public Pair<Integer, Integer> getXY()
 	{		
 		return XY;
 	}
 
 	/**
 	 * 
 	 * @return Caracter que ocupa a posicao deste boardposition
 	 */
 	public Character getOccup()
 	{
 		return occupied;
 	}
 	
 	@Override
 	public int compareTo(BoardPosition o) {
 		final int BEFORE = -1;
 		final int AFTER = 1;
 		final int EQUAL = 0;
 		
 		if(this.position > o.position)
 		{
 			return AFTER;
 		}
 		if(this.position < o.position)
 		{
 			return BEFORE;
 		}
 		else return EQUAL;	 
 	}
 	
 }
