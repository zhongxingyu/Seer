 package yucatan.communication.presentation;
 
 public class TemplateTokenStatusItem {
 
 	/**
 	 * Next explicitly expected character to switch the status to the {@link #successStatus}. If the next character is a different one switch to {@link #failStatus}.
 	 */
 	public char nextExpectedChar;
 
 	/**
 	 * Flag to indicate that the next char has to be an explicitly defined character. This flag is needed since chars {@link #nextExpectedChar} can not be null.
 	 */
 	public boolean nextExpectsExplicitChar = false;
 
 	/**
 	 * This indicates which status comes next if the pattern match. -1 means there is no change.
 	 */
 	public byte successStatus = TemplateProcessor.PLACHOLDER_NO_STATUS_CHANGE;
 
 	/**
 	 * This indicates which status comes next if the pattern fail. -1 means there is no change.
 	 */
 	public byte failStatus = TemplateProcessor.PLACHOLDER_NO_STATUS_CHANGE;
 
 	/**
 	 * This indicates which status comes next. -1 means there is no change. The next status will be applied with the next character execept this status defines terminators see
 	 * {@link #terminatedBy}.
 	 */
	public byte[] nextStatus;
 
 	/**
 	 * Current status is regulary terminated by one of these characters.
 	 */
 	public char[] terminatedBy;
 
 	/**
 	 * Set a new start position at the current position. (Only the first time when the status switched to the current status.)
 	 */
 	public boolean startNewToken = false;
 
 	/**
 	 * Inidcates that a token ends here. A token of type {@link #createTokenType} will be created.
 	 */
 	public boolean stopAndCreateToken = false;
 
 	/**
 	 * Inidcates that a token ends here. The token will be ignored.
 	 */
 	public boolean stopAndDropToken = false;
 
 	/**
 	 * The type of token to be created see static memebers of {@link TemplateToken}. default = TemplateToken.TOKENTYPE_TEXT
 	 */
 	public Byte createTokenType = TemplateToken.TOKENTYPE_TEXT;
 
 	public TemplateTokenStatusItem() {
 
 	}
 }
