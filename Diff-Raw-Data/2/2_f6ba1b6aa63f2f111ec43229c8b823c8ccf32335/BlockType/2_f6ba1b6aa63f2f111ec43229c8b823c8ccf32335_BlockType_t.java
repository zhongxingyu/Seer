 package jp.ac.osaka_u.ist.sdl.ectec.data;
 
 /**
  * An enum that represents types of blocks
  * 
  * @author k-hotta
  * 
  */
 public enum BlockType {
 
 	CLASS("class"),
 
 	METHOD("method"),
 
	CATCH("catch"),
 
 	DO("do"),
 
 	ELSE("else"),
 
 	ENHANCED_FOR("for"),
 
 	FINALLY("finally"),
 
 	FOR("for"),
 
 	IF("if"),
 
 	SWITCH("switch"),
 
 	SYNCHRONIZED("synchronized"),
 
 	TRY("try"),
 
 	WHILE("while");
 
 	/**
 	 * the head string
 	 */
 	private final String head;
 
 	private BlockType(final String head) {
 		this.head = head;
 	}
 
 	/**
 	 * get the head string
 	 * 
 	 * @return
 	 */
 	public final String getHead() {
 		return head;
 	}
 
 }
