 package element_actions;
 
 public enum ElementActions {
	S_OCR("Analiza OCR"),
	S_BREAK_V("Sparge vertical"),
	S_BREAK_H("Sparge orizontal"),
	S_TEXT("Editeaza text"),
	S_PAGE("Este numar pagina"),
	S_DELETE("Sterge element"),
	S_FRONT("Bring to Front"),
	S_BACK("Send to Back"),
	S_RESIZE("Redimensioneaza");
 
 	/**
 	 * @param text
 	 */
 	private ElementActions(final String text) {
 		this.text = text;
 	}
 
 	private final String text;
 
 	@Override
 	public String toString() {
 		return text;
 	}
 }
