 package nl.dirkgroenen.jokeren;
 
 import java.io.Serializable;
 import java.lang.reflect.Field;
 
 import android.app.Activity;
 
 public class PlayingCard extends Activity implements Serializable,Comparable<PlayingCard>{
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	
 	//suits
     protected static final char HEARTS = 'h';
     protected static final char SPADES = 's';
     protected static final char CLUBS = 'c';
     protected static final char DIAMOND = 'd';
     //tens
     protected static final char JACK = 'j';
     protected static final char QUEEN = 'q';
     protected static final char KING = 'k';
     protected static final char TEN = 't';
     //others
     protected static final char JOKER = 'o';
     protected static final char ACE = 'a';
     protected static final char ACE_AS_FOURTEEN = 'f';
     protected static final char RED_SUITS = 'r';
     protected static final char BLACK_SUITS = 'b';
 
     private boolean selected;
 	private char suit, value;
 	
 	public PlayingCard(char suit, char value) {
 		super();
 		this.suit = suit;
 		this.value = value;
 		this.selected = false;
 	}
 	
 	public char getValue(){
 		return value;
 	}
 	
 	public Integer getIntegerValue(){
         Integer retVal = Character.getNumericValue(this.value);
         switch (value) {
         case TEN:
                 retVal = 10;
                 break;
         case JACK:
                 retVal = 11;
                 break;
         case QUEEN:
                 retVal = 12;
                 break;
         case KING:
                 retVal = 13;
                 break;
         case JOKER:
                 retVal = null;
                 break;
         case ACE_AS_FOURTEEN:
                 retVal = 14;
                 break;
         case ACE:
         	retVal = 1;
         	break;
         default:
                 break;
         }
 
         return retVal;
 	}
 	
 	public char getSuit(){
 		return suit;
 	}
 	
 	public boolean getSelected(){
 		return selected;
 	}
 	public void setSelected(boolean selected){
 		this.selected = selected;
 	}
 	
 	public String getPngName(){
 		return new String(new char[]{suit,value});
 	}
 	
 	public int getImageResourceId(){
 		Field f;
 		int id = -1;
 		String pngName = getPngName();
 		try {
 			f = R.drawable.class.getDeclaredField(pngName);
 			id = f.getInt(null);
 		} catch (NoSuchFieldException e) {
 			e.printStackTrace();
 		} catch (IllegalArgumentException e) {
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			e.printStackTrace();
 		}
 		return id;
 	}
 	
 	public int getPoints(){
 		int retVal = Character.getNumericValue(value);
 		switch(value){
 		case KING:
 		case QUEEN:
 		case JACK:
 		case ACE_AS_FOURTEEN:
 			retVal = 10;
 			break;
 		case ACE:
 			retVal = 1; 
 			break;
 		case JOKER:
 			retVal = 25;
 			break;
 		default:
 			break;
 		}
 		return retVal; 
 	}
 	
 	public Integer getPos(){
 		int retVal = Character.getNumericValue(value);
 		switch(value){
 		case KING:
 			retVal = 13;
 			break;
 		case QUEEN:
 			retVal = 12;
 			break;
 		case JACK:
 			retVal = 11;
 			break;
 		case ACE_AS_FOURTEEN:
 			retVal = 14;
 			break;
 		case ACE:
 			retVal = 1; 
 			break;
 		case JOKER:
 			retVal = (Integer) null;
 			break;
 		default:
 			break;
 		}
 		return retVal; 
 	}
 
 	@Override
 	public int compareTo(PlayingCard other) {
		return (this.getIntegerValue()==null? 0: this.getIntegerValue()) - (other==null? -1 : (other.getIntegerValue() == null ? 0 : other.getIntegerValue()) );
 	}
 }
