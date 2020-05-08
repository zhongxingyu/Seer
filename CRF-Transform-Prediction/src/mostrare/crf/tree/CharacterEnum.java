package mostrare.crf.tree;

import java.util.Map;

public interface CharacterEnum {

	
	public abstract String[] getCharacterStringArray();

	/**
	 * Returns the string relative to the annotation associated with the provided <code>index</code>.
	 * 
	 * @param index
	 *            the index of an annotation
	 * @return the string version of the annotation
	 */
	public abstract String getCharacterText(int index);

	/**
	 * Returns the index relative to the annotation associated with the provided
	 * <code>stringValue</code>.
	 * 
	 * @param annotation
	 *            string value of the annotation
	 * @return the index relative to the annotation.
	 */
	public abstract int getCharacterIndex(String annotation);

	/**
	 * Returns the number of annotation labels.
	 * 
	 * @return the number of annotation labels.
	 */
	public abstract int getCharactersNumber();
	
	public abstract Map<String, Integer> getCharacterIntegerMap();

}
