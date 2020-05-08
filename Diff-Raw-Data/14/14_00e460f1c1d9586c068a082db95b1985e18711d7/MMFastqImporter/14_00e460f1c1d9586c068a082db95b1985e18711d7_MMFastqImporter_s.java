 /*
  * Acacia - GS-FLX & Titanium read error-correction and de-replication software.
  * Copyright (C) <2011>  <Lauren Bragg and Glenn Stone - CSIRO CMIS & University of Queensland>
  * 
  * 	This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *  
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *  
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package pyromaniac.IO;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.nio.CharBuffer;
 import java.nio.MappedByteBuffer;
 import java.nio.channels.FileChannel;
 import java.nio.charset.CharacterCodingException;
 import java.nio.charset.Charset;
 import java.nio.charset.CharsetDecoder;
 import java.util.ArrayList;
 import java.util.LinkedList;
 
 import pyromaniac.DataStructures.MutableInteger;
 import pyromaniac.DataStructures.Pyrotag;
 import pyromaniac.DataStructures.Sequence;
 import pyromaniac.IO.MMFastaImporter.SeqFormattingException;
 
 // TODO: Auto-generated Javadoc
 /**
  * The Class MMFastqImporter.
  */
 public class MMFastqImporter implements TagImporter
 {
 	
 	/** The logger. */
 	private AcaciaLogger logger;
 	
 	/** The fastq file. */
 	private String fastqFile;
 	
 	/** The record starts. */
 	private int[] recordStarts; //populate these first
 	
 	/** The decoder. */
 	private CharsetDecoder decoder = Charset.forName(System.getProperty("file.encoding")).newDecoder();
 	
 	/** The Constant BEGINNING_FASTQ_SEQ. */
 	public static final char  BEGINNING_FASTQ_SEQ = '@';
 	
 	/** The Constant BEGINNING_FASTQ_QUAL. */
 	public static final char  BEGINNING_FASTQ_QUAL = '+';
 	
 	/** The Constant ACCEPTIBLE_IUPAC_CHARS. */
 	public static final String ACCEPTIBLE_IUPAC_CHARS = "ATGCNURYWSMKBHDV";
 	
 	/** The record buffer. */
 	private MappedByteBuffer recordBuffer;
 	
 	/**
 	 * Instantiates a new mM fastq importer.
 	 *
 	 * @param fastqFile the fastq file
 	 * @param logger the logger
 	 */
 	public MMFastqImporter(String fastqFile, AcaciaLogger logger)
 	{
 		this.fastqFile = fastqFile;
 		this.logger = logger;
 		this.recordBuffer = null;
 		this.init();
 	}
 	
 	/* (non-Javadoc)
 	 * @see pyromaniac.IO.TagImporter#getNumberOfSequences()
 	 */
 	public int getNumberOfSequences()
 	{
 		return this.recordStarts.length;
 	}
 	
 	/**
 	 * Inits the.
 	 */
 	public void init()
 	{
 		//essentially all I want to do is look for delimiters in the file.
 		try
 		{
 			_initFile();
 		}
 		catch (CharacterCodingException cce) 
 		{  
 			throw new RuntimeException(cce);  
 		
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 	}
 		
 	/**
 	 * _init file.
 	 *
 	 * @throws Exception the exception
 	 */
 	private void _initFile() throws Exception
 	{
 		FileInputStream tempStream = new FileInputStream(new File(this.fastqFile)); 
 		FileChannel fcSeq = tempStream.getChannel();
 		
 		recordBuffer = fcSeq.map(FileChannel.MapMode.READ_ONLY, 0, fcSeq.size());
 		LinkedList <Integer> seqStartsLL = new LinkedList <Integer>();
 		
 		int maxBuffer = 2048;
 		int bufferSize = (recordBuffer.capacity() > maxBuffer)? maxBuffer: recordBuffer.capacity();
 		
 		recordBuffer.limit(bufferSize);
 		recordBuffer.position(0);
 
 		int state = -1;
 		char last = '\n';
 		
 		while(recordBuffer.position() != recordBuffer.capacity())
 		{					
 			int prevPos = recordBuffer.position();
 			CharBuffer result = decoder.decode(recordBuffer);	
 			recordBuffer.position(prevPos);
 			
 			for(int i = 0; i < result.capacity(); i++)
 			{
 				char curr = result.charAt(i);
 				int posInFile = prevPos + i ;
 
 				//I see a fastq header, I am either at beginning of file, or last saw the quality line...
				if(curr == BEGINNING_FASTQ_SEQ && (state == -1 || state == 2) & (last == '\n' || last == '\r'))
 				{
 					seqStartsLL.add(posInFile);
 					state = 1;
 				}
 				else if (curr == BEGINNING_FASTQ_QUAL && (state == 1))
 				{
 					state = 2;
 				}
				
 				last = curr;
 			}
 			
 			int newPos = recordBuffer.limit();
 			
 			if(recordBuffer.limit() + bufferSize > recordBuffer.capacity())
 				recordBuffer.limit(recordBuffer.capacity());
 			else
 				recordBuffer.limit(recordBuffer.limit() + bufferSize);
 			recordBuffer.position(newPos);
 		}
 		
 		recordBuffer.rewind();
 		this.recordStarts = new int [seqStartsLL.size()];
 		
 		int pos = 0;
 		for(int element : seqStartsLL)
 		{
 			this.recordStarts[pos] = element;
 			pos++;
 		}
 	}
 	
 	/**
 	 * Checks if is printable char.
 	 *
 	 * @param c the c
 	 * @return true, if is printable char
 	 */
 	public boolean isPrintableChar( char c ) 
 	{
 	    Character.UnicodeBlock block = Character.UnicodeBlock.of( c );
 	    return (!Character.isISOControl(c)) &&
 	            block != null &&
 	            block != Character.UnicodeBlock.SPECIALS;
 	}
 	
 	//pyrotag at index.
 	/* (non-Javadoc)
 	 * @see pyromaniac.IO.TagImporter#getPyrotagAtIndex(int)
 	 */
 	public Pyrotag getPyrotagAtIndex(int index)
 	{
 		if(index >= this.recordStarts.length)
 			return null;
 		
 		char [] relRecordBlock = getBlock(this.recordStarts, index, this.recordBuffer);
 		
 		//construct the pyrotag in this block.
 		
 		Pyrotag p = processRecordBlock(relRecordBlock);
 		p.setInternalID(index);
 		return p;
 	}
 	
 	/**
 	 * Gets the block.
 	 *
 	 * @param starts the starts
 	 * @param index the index
 	 * @param buff the buff
 	 * @return the block
 	 */
 	public char [] getBlock(int [] starts, int index, MappedByteBuffer buff)
 	{
 		if(index  >= starts.length)
 		{
 			return null;
 		
 		}
 		
 		long blockStart = starts[index];
 		long blockEnd = blockStart;
 		if(index == starts.length - 1)
 			blockEnd = buff.capacity(); 
 		else
 			blockEnd = starts[index + 1];
 
 		try
 		{
 			buff.limit((int)blockEnd);
 			buff.position((int)blockStart);
 			CharBuffer resBuffer = decoder.decode(buff);
 			buff.rewind();
 			return resBuffer.array();
 		}
 		catch(Exception e)
 		{
 			System.out.println("Error: " + e.getMessage());
 			System.out.println("Tried to get block starting at " + blockStart + " for " + (blockEnd - blockStart + 1) + " chars");
 			System.out.println("The maximum block size is " + buff.limit());
 		}
 		return null;
 	}
 	
 	/**
 	 * Process record block.
 	 *
 	 * @param pyrotagBlock the pyrotag block
 	 * @return the pyrotag
 	 */
 	public Pyrotag processRecordBlock(char [] pyrotagBlock)
 	{	
 		try
 		{
 			MutableInteger index = new MutableInteger(0);
 			
 			String identifier = _readIdentifierLine(pyrotagBlock,index); //read identifier and read sequence need to be fixed.
 			String [] idComp = _parseIdentifierLine(identifier);
 			
 			ArrayList <Character> nucleotides = this._readSequence(pyrotagBlock, index);
 			
 			_readIdentifierLine(pyrotagBlock,index); //throw this result away for now, if I wanted to, could do a check to compare
 			//headers
 	
 			ArrayList <Integer> qualities = this._readQualities(pyrotagBlock, index);
 			
 			Sequence <Character> pyrotagSeq = new Sequence<Character> (nucleotides, idComp[0], idComp[1]);
 			Sequence <Integer> pyrotagQual = new Sequence <Integer>(qualities, idComp[0], idComp[1]);
 			Pyrotag p = new Pyrotag(idComp[0],idComp[1], pyrotagSeq, pyrotagQual);
 			
 			return p;
 		}
 		catch(SeqFormattingException sfe)
 		{
 			System.out.println(sfe.getMessage());
 			System.exit(1);
 		}
 		return null;
 	}
 	
 	
 	
 	/**
 	 * _parse identifier line.
 	 *
 	 * @param identifierLine the identifier line
 	 * @return the string[]
 	 */
 	public String [] _parseIdentifierLine(String identifierLine)
 	{
 		int posAngle = identifierLine.indexOf(BEGINNING_FASTQ_SEQ);
 		int posWhite = identifierLine.indexOf(" ");
 		
 		String [] IDAndDescription = new String [2];
 		if(posWhite > 0)
 		{
 			IDAndDescription[0] = identifierLine.substring(posAngle + 1, posWhite);
 			IDAndDescription[1] = identifierLine.substring(posWhite + 1, identifierLine.length());
 		}
 		else
 		{
 			IDAndDescription[0] = identifierLine.substring(posAngle + 1, identifierLine.length());
 			IDAndDescription[1] = "";
 		}
 		return IDAndDescription;
 	}
 	
 	/**
 	 * _read sequence.
 	 *
 	 * @param pyrotagBlock the pyrotag block
 	 * @param pos the pos
 	 * @return the array list
 	 * @throws SeqFormattingException the seq formatting exception
 	 */
 	public ArrayList <Character> _readSequence(char [] pyrotagBlock, MutableInteger pos) throws SeqFormattingException
 	{
 			ArrayList <Character> characters = new ArrayList <Character>();			
 			char curr;
 			int index = pos.value();
 			
 			
 			//push through newlines
 			while(index < pyrotagBlock.length)
 			{
 				curr = pyrotagBlock[index];
 				
 				if(! (curr == '\n' || curr == '\r'))
 					break;
 				
 				index++;
 			}
 			
 			
 			//should be in nucleotides
 			while(index < pyrotagBlock.length)
 			{
 				curr = pyrotagBlock[index];
 				curr = Character.toUpperCase(curr);
 
 				if(curr == '\n' || curr == '\r')
 					break;
 				
 				if(Character.isLetter((char)curr))
 				{
 					if(ACCEPTIBLE_IUPAC_CHARS.indexOf(curr) == -1)
 					{
 						throw new SeqFormattingException("Non-IUPAC character (" + curr + ") in sequence", this.fastqFile);
 					}
 					else
 					{
 						characters.add(curr);
 					}
 				}
 				index++;
 			}
 			pos.update(index);
 			return characters;
 	}
 	
 	//FASTQ qualities need to be mapped back to integers.
 	/**
 	 * _read qualities.
 	 *
 	 * @param pyrotagBlock the pyrotag block
 	 * @param pos the pos
 	 * @return the array list
 	 * @throws SeqFormattingException the seq formatting exception
 	 */
 	public ArrayList <Integer> _readQualities(char [] pyrotagBlock, MutableInteger pos) throws SeqFormattingException
 	{
 		ArrayList <Integer> qualities = new ArrayList <Integer>();
 		char curr;
 		
 		int index = pos.value();
 		
 		//push through newlines
 		while(index < pyrotagBlock.length)
 		{
 			curr = pyrotagBlock[index];
 			
 			if(! (curr == '\n' || curr == '\r'))
 				break;
 			
 			index++;
 		}
 	
 		//qualities should be here...
 		while(index < pyrotagBlock.length)
 		{
 			curr = pyrotagBlock[index];	
 			
 			if(curr == '\n' || curr == '\r')
 				break;
 			
 			int conv = curr - 33;
 			qualities.add(conv);
 			index++;
 		}
 		
 		return qualities;
 	}
 	
 	
 	/**
 	 * _read identifier line.
 	 *
 	 * @param pyrotagBlock the pyrotag block
 	 * @param pos the pos
 	 * @return the string
 	 */
 	public String _readIdentifierLine(char [] pyrotagBlock, MutableInteger pos)
 	{
 		StringBuffer buff = new StringBuffer();
 		int currPos = pos.value();
 		
 		char curr = pyrotagBlock[currPos];
 		while(curr == '\n' || curr == '\r')
 		{
 			currPos++;
 			curr = pyrotagBlock[currPos];
 		}
 		
 		
 		while(!(curr == '\n' || curr == '\r'))
 		{
 			buff.append(curr);
 			currPos++;
 			curr = pyrotagBlock[currPos];
 		}
 		
 		pos.update(currPos);
 		return buff.toString();
 	}
 	
 	/**
 	 * The Class SeqFormattingException.
 	 */
 	public class SeqFormattingException extends Exception
 	{
 		
 		/** The Constant serialVersionUID. */
 		private static final long serialVersionUID = 1L;
 		
 		/**
 		 * Instantiates a new seq formatting exception.
 		 *
 		 * @param message the message
 		 * @param filename the filename
 		 */
 		public SeqFormattingException(String message, String filename)
 		{
 			super("File: " + filename  + " FormattingException: " + message);
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see pyromaniac.IO.TagImporter#closeFiles()
 	 */
 	public void closeFiles() 
 	{
 		// TODO Auto-generated method stub
 		
 	}
 }
