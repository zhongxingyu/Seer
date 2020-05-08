 /*
  *  NIO Framework
  *  
  *  Copyright 2009 Boris HUISGEN <bhuisgen@hbis.fr>
  * 
  *  This program is free software; you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation; either version 2 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU Library General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program; if not, write to the Free Software
  *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
  */
 
 package fr.hbis.ircs.lib.nio.algorithm;
 
 import java.nio.ByteBuffer;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * The class <code>NoParsingAlgorithm</code> is a algorithm which don't predict
  * the end of reading of the byte buffer and transfer directly the job to the
  * <code>ProcessTask</code>.
  * 
  * @author bhuisgen
  */
 public class NoParsingAlgorithm extends AlgorithmBase
 {
 	/**
 	 * Constructs a new <code>NoParsingAlgorithm</code>.
 	 */
 	public NoParsingAlgorithm ()
 	{
 		logger.log (Level.FINE, "new algorithm created");
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see fr.hbis.ircs.lib.nio.Algorithm#parse(java.nio.ByteBuffer)
 	 */
 	public boolean parse (ByteBuffer byteBuffer)
 	{
 		setCurrentLimit (byteBuffer.limit ());
 		setCurrentPosition (byteBuffer.position ());
 
 		byteBuffer.flip ();

 		setState (true);
 
 		logger.log (Level.FINE, "algorithm succeeded, data available");
 
 		return true;
 	}
 
 	private static Logger logger = Logger
 			.getLogger ("fr.hbis.ircs.lib.nio.algorithm.NoParsingAlgorithm");
 }
