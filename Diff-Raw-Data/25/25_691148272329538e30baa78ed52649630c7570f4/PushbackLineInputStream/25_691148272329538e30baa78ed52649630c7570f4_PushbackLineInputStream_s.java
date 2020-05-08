 /*
    Copyright 2011 James Cowgill
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 */
 package uk.org.cowgill.james.jircd.util;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PushbackInputStream;
 
 public class PushbackLineInputStream extends PushbackInputStream
 {
 	private int lineNo;
 	private int charNo;
 
 	/**
 	 * Creates a new PushbackLineInputStream reading from the specified stream
 	 *
 	 * @param in stream which bytes will be read from
 	 */
 	public PushbackLineInputStream(InputStream in)
 	{
		super(in);
 	}
 
 	/**
 	 * Creates a new PushbackLineInputStream reading from the specified stream with the specified pushback buffer size
 	 *
 	 * @param in stream which bytes will be read from
 	 * @param size size of pushback buffer
 	 *
 	 * @throws IllegalArgumentException if size is <= 0
 	 */
 	public PushbackLineInputStream(InputStream in, int size)
 	{
 		super(in, size);
 	}
 
 	@Override
 	public int read() throws IOException
 	{
 		//Read character + determine if it's a newline
 		int c = super.read();
 		switch(c)
 		{
 			case 13:
				//Newline unless followed by a 10
 				c = super.read();
				if(c == 10)
 				{
					//Don't increase character number
 					super.unread(c);
					return c;
 				}
 
 			case 10:
 				//Newline
 				lineNo++;
 				charNo = 1;
 				return c;
 				
 			case -1:
 				//EOF
 				return c;
 		}
 
 		//Normal character
 		charNo++;
 		return c;
 	}
 
 	@Override
 	public long skip(long n) throws IOException
 	{
 		long skipped = 0;
 
 		while(skipped < n)
 		{
 			if(this.read() == -1)
 			{
 				break;
 			}
 
 			skipped++;
 		}
 
 		return skipped;
 	}
 
 	@Override
 	public void unread(int b) throws IOException
 	{
 		//Only pushback if not EOF
 		if(b != -1)
 		{
 			super.unread(b);
 	
 			if(charNo == 1)
 			{
 				//Go up 1 row. Using 0 as character since we don't know!
 				lineNo--;
 				charNo = 0;
 			}
 			else
 			{
 				charNo--;
 			}
 		}
 	}
 
 	/**
 	 * Returns the line number the stream is currently at
 	 */
 	public int getLineNo()
 	{
 		return lineNo;
 	}
 
 	/**
 	 * Returns the character number the stream is currently at
 	 */
 	public int getCharNo()
 	{
 		return charNo;
 	}
 	
 	/**
 	 * This method is not implemented and throws UnsupportedOperationException
 	 */
 	@Override
 	public int read(byte[] b, int off, int len)
 	{
 		//Error - not allowed for this stream
 		throw new UnsupportedOperationException();
 	}
 	
 	/**
 	 * This method is not implemented and throws UnsupportedOperationException
 	 */
 	@Override
 	public void unread(byte[] b)
 	{
 		//Error - not allowed for this stream
 		throw new UnsupportedOperationException();
 	}
 	
 	/**
 	 * This method is not implemented and throws UnsupportedOperationException
 	 */
 	@Override
 	public void unread(byte[] b, int off, int len)
 	{
 		//Error - not allowed for this stream
 		throw new UnsupportedOperationException();
 	}
 	
 }
