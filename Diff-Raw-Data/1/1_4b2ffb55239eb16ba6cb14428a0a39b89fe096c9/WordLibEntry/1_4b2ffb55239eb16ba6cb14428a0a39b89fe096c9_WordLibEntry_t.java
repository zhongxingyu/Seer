 package net.axstudio.axparty.guessword;
 
 import java.util.Vector;
 
 class WordLibEntry
 {
 
 	int mNumChars;
 
 	String mKeys = "";
 	String[] mWords;
 
 	// final Vector<WordLibElement> mElements = new Vector<WordLibElement>();
 	WordLibEntry()
 	{
 
 	}
 
 	String getKey(int pos)
 	{
 		if (pos < 0 || pos >= mKeys.length())
 			return null;
 		else
 			return mKeys.substring(pos, pos + 1);
 	}
 
 	String getWord(int keyPos, int wordPos)
 	{
 		if (keyPos < 0 || keyPos >= mWords.length)
 			return null;
		wordPos *= mNumChars;
 		if (wordPos < 0 || wordPos >= mWords[keyPos].length())
 			return null;
 		return mWords[keyPos].substring(wordPos, wordPos + mNumChars);
 	}
 
 	int getNumWords(int keyPos)
 	{
 		if (keyPos < 0 || keyPos >= mWords.length)
 			return 0;
 		return (int) Math.floor(mWords[keyPos].length() / mNumChars);
 	}
 
 	String[] getWords(int keyPos)
 	{
 
 		if (keyPos < 0 || keyPos >= mWords.length)
 			return null;
 		String[] words = new String[getNumWords(keyPos)];
 		for (int i = 0; i < words.length; ++i)
 		{
 			words[i] = getWord(keyPos, i);
 		}
 		return words;
 	}
 
 	String getWordsString(int keyPos)
 	{
 
 		if (keyPos < 0 || keyPos >= mWords.length)
 			return null;
 		return mWords[keyPos];
 	}
 
 	public String[] genWord()
 	{
 		if (mKeys.length() == 0)
 			return null;
 		final int keyIndex = (int) Math.floor(Math.random() * mKeys.length());
 
 		if (null == mWords[keyIndex])
 			return null;
 
 		final int numWords = getNumWords(keyIndex);
 
 		if (numWords <= 1)
 			return null;
 
 		for (int x = 0; x < 100; ++x)
 		{
 			int i = (int) Math.floor(Math.random() * numWords);
 			int j = (int) Math.floor(Math.random() * numWords);
 			if (i != j)
 			{
 				return new String[] { getWord(keyIndex, i),
 						getWord(keyIndex, j), getKey(keyIndex) + mNumChars };
 
 			}
 		}
 		return null;
 
 	}
 
 }
