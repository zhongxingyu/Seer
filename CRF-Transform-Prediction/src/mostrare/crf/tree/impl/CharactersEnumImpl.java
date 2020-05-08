package mostrare.crf.tree.impl;

import java.util.HashMap;
import java.util.Map;

import mostrare.crf.tree.CharacterEnum;

public class CharactersEnumImpl implements CharacterEnum {

	private String[]				characters;

	// entry of the map: annotation (string value) | annotation (index)
	private Map<String, Integer>	charactersInv;

	public CharactersEnumImpl (String[] allcharacters)
	{
		// store annotations
		characters = allcharacters;
		charactersInv = new HashMap<String, Integer>();
		for (int labelIndex = 0; labelIndex < characters.length; labelIndex += 1)
			charactersInv.put(characters[labelIndex], labelIndex);
	} 
	
	@Override
	public String[] getCharacterStringArray()
	{
		return characters;
	}

	@Override
	public String getCharacterText(int index)
	{
		return characters[index];
	}

	@Override
	public int getCharacterIndex(String character)
	{ 
		if(charactersInv.get(character)!=null)
		    return charactersInv.get(character);
		else return -1;
	}

	@Override
	public int getCharactersNumber()
	{
		return characters.length;
	}

	@Override
	public Map<String, Integer> getCharacterIntegerMap()
	{
		return charactersInv;
	}
}
