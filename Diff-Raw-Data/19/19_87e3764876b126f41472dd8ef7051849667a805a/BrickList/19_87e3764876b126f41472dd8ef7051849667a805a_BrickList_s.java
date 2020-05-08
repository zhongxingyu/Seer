 package essentials.objects;
 
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.xml.bind.JAXB;
 import javax.xml.bind.annotation.XmlAccessType;
 import javax.xml.bind.annotation.XmlAccessorType;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlTransient;
 
 import essentials.enums.LetterEnum;
 import essentials.enums.OrientationEnum;
 
 /**
  * Class for generating a list of bricks.
  * Also contains parameters row, column and orientation
  * to use class to set bricks on game map.
  * @author hannes
  *
  */
 @XmlAccessorType(XmlAccessType.FIELD)
 @XmlRootElement(name = "bricklist")
 public class BrickList{
 
 	@XmlElement
 	protected List<Brick> bricks;
 	@XmlElement
 	private int row;
 	@XmlElement
 	private int col;
 	@XmlElement
 	private OrientationEnum orientation;
 	
 	
 	/**
 	 * Constructor
 	 */
 	public BrickList() {
 		this(0, 0, OrientationEnum.HORIZONTAL);
 	}
 	
 	/**
 	 * Constructor
 	 * @param aRow
 	 * @param aColumn
 	 * @param aOrientation
 	 */
 	public BrickList(int aRow, int aColumn, OrientationEnum aOrientation){
 		row = aRow;
 		col = aColumn;
 		orientation = aOrientation;
 		bricks = new ArrayList<Brick>();
 	}
 	
 	/**
 	 * Constructor
 	 * @param aBricks
 	 * @param aRow
 	 * @param aColumn
 	 * @param aOrientation
 	 */
 	public BrickList(List<Brick> aBricks, int aRow, int aColumn, OrientationEnum aOrientation){
 		this(aRow, aColumn, aOrientation);
 		bricks = aBricks;
 	}
 	
 	/**
 	 * Constructor
 	 * Clones another bricklist
 	 * @param aBrickList
 	 */
 	public BrickList(BrickList aBrickList){
 		this(
 				aBrickList.getBricks(),
 				aBrickList.getRow(),
 				aBrickList.getColumn(),
 				aBrickList.getOrientation()
 				);		
 	}
 	
 	/**
 	 * Get one brick a specific position
 	 * @param i
 	 * @return brick at position i (starting at position 0)
 	 */
 	public Brick get(int i){
 		return bricks.get(i);
 	}
 	
 	/**
 	 * @return size of bricklist
 	 */
 	public int size(){
 		return bricks.size();
 	}
 	
 	/**
 	 * @return row
 	 */
 	@XmlTransient
 	public int getRow(){
 		return row;
 	}
 	
 	/**
 	 * Set row
 	 * @param aRow
 	 */
 	public void setRow(int aRow){
 		row = aRow;
 	}
 	
 	/**
 	 * @return column
 	 */
 	public int getColumn(){
 		return col;
 	}
 	
 	/**
 	 * Set column
 	 * @param aColumn
 	 */
 	public void setColumn(int aColumn){
 		col = aColumn;
 	}
 	
 	/**
 	 * @return orientation
 	 */
 	@XmlTransient
 	public OrientationEnum getOrientation(){
 		return orientation;
 	}
 	
 	/**
 	 * Set orientation (horizontal or vertical)
 	 * @param aOrientation
 	 */
 	public void setOrientation(OrientationEnum aOrientation){
 		orientation = aOrientation;
 	}
 	
 	/**
 	 * Set position (row and column)
 	 * @param aRow
 	 * @param aColumn
 	 */
 	public void setPosition(int aRow, int aColumn)
 	{
 		setRow(aRow);
 		setColumn(aColumn);
 	}
 	
 	/**
 	 * Sets position (row, column and orientation)
 	 * @param aRow
 	 * @param aColumn
 	 * @param aOrientation
 	 */
 	public void setPosition(int aRow, int aColumn, OrientationEnum aOrientation)
 	{
 		setRow(aRow);
 		setColumn(aColumn);
 		setOrientation(aOrientation);
 	}
 	
 	/**
 	 * @return list of bricks
 	 */
 	@XmlTransient
 	public List<Brick> getBricks(){
 		return bricks;
 	}
 	
 	/**
 	 * Sets a list of bricks
 	 * @param aBricks
 	 */
 	public void setBricks(List<Brick> aBricks){
 		bricks = aBricks;
 	}
 	
 	
 	/**
 	 * Add a brick to bricklist
 	 * @param aBrick
 	 */
 	public void add(Brick aBrick){
 		bricks.add(aBrick);
 	}
 	
 	/**
 	 * Adds a list of bricks to bricklist
 	 * @param aBricks
 	 */
 	public void add(List<Brick> aBricks){
 		for(Brick b : aBricks){
 			add(b);
 		}
 	}
 	
 	/**
 	 * Adds a bricklist's bricks to this list
 	 * @param aBrickList
 	 */
 	public void add(BrickList aBrickList){
 		add( aBrickList.getBricks() );
 	}
 	
 	/**
 	 * Remove a brick with one specific letter
 	 * @param aLetter
 	 */
 	public void remove(String aLetter){
 		for(Brick b : bricks){
 			if( b.getLetter().toString().equals(aLetter) ){
 				bricks.remove(b);
 				return;
 			}
 		}
 	}
 	
 	/**
 	 * Remove a brick with one specific letter
 	 * from LetterEnum
 	 * @param aLetter
 	 */
 	public void remove(LetterEnum aLetter){
 		remove(aLetter.toString());
 	}
 	
 	/**
 	 * Remove a brick from list
 	 * ATTENTION: Only the letter is important,
 	 * not the weight of the brick!
 	 * @param aBrick
 	 */
 	public void remove(Brick aBrick){
		if( aBrick.getWeight() > 0 ){
 			remove( aBrick.getLetter() );
 		}
 	}
 	
 	/**
 	 * Removes a set of bricks from list
 	 * @param aBrickList
 	 */
 	public void remove(BrickList aBrickList){
 		for( Brick b : aBrickList.getBricks() ){
 			remove(b);
 		}
 	}
 	
 	public Brick get(String aLetter){
 		Brick rBrick = new Brick();
 		return rBrick;
 	}
 	
 	/**
 	 * Checks if a brick with a specific letter is inside list
 	 * @param aLetter
 	 * @return true if brick found, otherwise false
 	 */
 	public boolean contains(String aLetter){
 		for(Brick b : bricks){
 			if( b.getLetter().toString().equals(aLetter) ){
 				return true;
 			}
 		}		
 		return false;
 	}
 	
 	/**
 	 * Checks if a brick with a specific letter and weight is inside list
 	 * @param letter
 	 * @param weight
 	 * @return true if a brick with both (letter and weight) is in pool,
 	 * otherwise false
 	 */
 	public boolean contains(String letter, int weight){
 		for( Brick b : bricks ){
 			int bWeight = b.getWeight();
 			String bLetter = b.getLetter().toString();
 			if( (bLetter.equals(letter) && bWeight == weight)
 					|| weight < 0 ){
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	/**
 	 * Checks if a brick with a specific letter from LetterEnum
 	 * is inside list.
 	 * ATTENTION: Compares only the letters,
 	 * not the weight of the brick!
 	 * @param aLetter
 	 * @return true if brick found, otherwise false
 	 */
 	public boolean contains(LetterEnum aLetter){
 		return contains(aLetter.toString());
 	}
 	
 	/**
 	 * Checks if a brick with a specific letter from LetterEnum
 	 * is inside list.
 	 * @param aLetter
 	 * @param weight
 	 * @return true if a brick with both (letter and weight) is in pool,
 	 * otherwise false
 	 */
 	public boolean contains(LetterEnum aLetter, int weight){
 		return contains(aLetter.toString(), weight);
 	}
 	
 	/**
 	 * Checks if a brick is inside list.
 	 * @param aBrick
 	 * @return true if a brick with both (letter and weight) is in pool,
 	 * otherwise false
 	 */
 	public boolean contains(Brick aBrick) {
 		return contains(aBrick.getLetter(), aBrick.getWeight());
 	}
 	
 	/**
 	 * Checks if a set of bricks is in list
 	 * Checks letter AND weight!
 	 * @param aBrickList
 	 * @return true if all bricks are in list, otherwise false
 	 */
 	public boolean contains(BrickList aBrickList){
 		for( Brick b : aBrickList.getBricks() ){
 			if( !contains(b) ){
 				return false;
 			}
 		}		
 		return true;
 	}
 	
 	/**
 	 * Checks if a set of set of bricks (list of bricklists) is
 	 * in list. Checks letter AND weight
 	 * @param aBrickLists
 	 * @return true if all bricks are in list, otherwise false
 	 */
 	public boolean contains(List<BrickList> aBrickLists){
 		for( BrickList bL : aBrickLists ){
 			if( !contains(bL) ){
 				return false;
 			}
 		}
 		return true;
 	}
 	
 	
 	/**
 	 * Converts object to string
 	 */
 	public String toString(){
 		StringWriter dataWriter = new StringWriter();
 		JAXB.marshal(this, dataWriter);
 		return dataWriter.toString();
 	}
 	
 	/**
 	 * Converts bricklist to a word
 	 * @return
 	 */
 	public String toWord(){
 		String word = "";
 		for(Brick b : bricks){
 			word = word.concat( b.getLetter().toString() );
 		}
 		return word;
 	}
 	
 	/**
 	 * Calculates score from bricks in bricklist
 	 * @return score
 	 */
 	public int getScore(){
 		int score = 0;
 		for(Brick b : bricks){
 			score += b.getWeight();
 		}
 		return score;
 	}
 	
 	/**
 	 * Converts a word into a list of bricks
 	 * @param word
 	 * @return List of bricks with no weight
 	 */
 	public static List<Brick> toBricks(String word){
 		List<Brick> bricks = new ArrayList<Brick>();
 		for( char c : word.toCharArray() ){
 			LetterEnum letter = LetterEnum.valueOf( String.valueOf(c) );
 			bricks.add( new Brick(letter, 0) );
 		}		
 		return bricks;
 	}
 	
 	/**
 	 * Calculates score for a set of bricklists
 	 * @param aBrickLists List of bricklists
 	 * @return score
 	 */
 	public static int getScore(List<BrickList> aBrickLists){
 		int score = 0;
 		for( BrickList bL : aBrickLists ){
 			if(bL.getScore() > 0){
 				score += bL.getScore();
 			}
 		}
 		return score;
 	}
 	
 }
