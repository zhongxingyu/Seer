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
  * The class <code>CRLFAlgorithm</code> is a algorithm which predict the end of
  * reading when the end of buffer has a carriage return and a linefeed
  * characters (CRLF).
  * 
  * @author bhuisgen
  */
 public class CRLFAlgorithm extends AlgorithmBase
 {
 	/**
 	 * Constructs a new <code>CRLFAlgorithm</code> object.
 	 */
 	public CRLFAlgorithm ()
 	{
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
		setState (false);
 
 		if (byteBuffer.position () == 0)
 		{
 			logger.log (Level.WARNING, "no data to parse, aborting");
 
 			return false;
 		}
 
 		byteBuffer.flip ();
 
 		int limit = byteBuffer.limit ();
 
 		if ((byteBuffer.get (limit - 1) == Constants.CRLFALGORITHM_LF)
 				&& (limit - 1 > byteBuffer.position ())
 				&& (byteBuffer.get (limit - 2) == Constants.CRLFALGORITHM_CR))
 		{
 			setState (true);
 
 			logger.log (Level.FINE, "algorithm succeeded, data available");
 
 			return true;
 		}
 
 		logger.log (Level.FINE, "algorithm failed, data not available");
 
 		return false;
 	}
 
 	private static Logger logger = Logger
 			.getLogger ("fr.hbis.ircs.lib.nio.algorithm.CRLFAlgorithm");
 }
