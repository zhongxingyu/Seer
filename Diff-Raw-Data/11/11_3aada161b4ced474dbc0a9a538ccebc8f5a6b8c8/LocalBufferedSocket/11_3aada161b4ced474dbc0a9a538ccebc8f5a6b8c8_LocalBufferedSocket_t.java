 package uk.co.harcourtprogramming.mewler;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 
 public class LocalBufferedSocket
 {
 
 	private final BlockingQueue<Integer> q = new LinkedBlockingQueue<Integer>();
 
 	public class LineReader extends InputStream {
 
 		@Override
 		public synchronized int read() throws IOException
 		{
 			try
 			{
 				return q.take();
 			}
 			catch (InterruptedException ex)
 			{
 			}
 			return 0;
 		}
 
 		@Override
 		public int available() throws IOException
 		{
 			return q.size();
 		}
 
 		/**
 		 * Returns text up to a new line or the end of the buffered input,
 		 * up to a maximum of 8192 bytes.
 		 */
 		public synchronized String readLine() throws IOException
 		{
 			final int newLineChar = (int)('\n' & 0xFF);
 			final int carRetnChar = (int)('\r' & 0xFF);
 
 			final byte[] buffer = new byte[8192];
 			int position = 0;
 
 			while (q.size() > 0 && position < 8192)
 			{
 				// Next byte value.
 				byte b = q.poll().byteValue();
 
 				// If we reach the end of the line, remove extra new line if it exists
 				if (b == newLineChar || b == carRetnChar)
 				{
 					if (q.isEmpty()) break;
 
 					byte secondLineChar = q.peek().byteValue();
 					if (secondLineChar != b && (b == newLineChar || b == carRetnChar))
 						q.poll();
 
 					break;
 				}
 
 				if (b != -1)
 				{
 					buffer[position] = b;
 					++position;
 				}
 			}
 
 			return new String(buffer, 0, position);
 		}
 	}
 
 	public class LineWriter extends OutputStream
 	{
 		@Override
 		public synchronized void write(int b) throws IOException
 		{
 			try
 			{
 				q.put(b & 0xFF);
 				return;
 			}
 			catch (InterruptedException ex)
 			{
 			}
 		}
 
 		public synchronized void newLine() throws IOException
 		{
 			write('\n');
 			flush();
 		}
 
 		public synchronized void writeLine(String s) throws IOException
 		{
 			for (byte b : s.getBytes())
 				write(b);
 
 			newLine();
 		}
 
 		@Override
 		public void flush() throws IOException
 		{
			// Don't ask me why, but the end of stream marker can apparently
			// denote a break in the stream. I can't find any documentation to
			// back this up, but...it works, dammit. Even with other classes
			// in java.io (BufferedReader, I'm looking at you...)
 			q.add(-1);
 			super.flush();
 		}
 
 
 	}
 
 	public final LineReader in = new LineReader();
 
 	public final LineWriter out = new LineWriter();
 
 	int peek()
 	{
 		return q.peek();
 	}
 
 	int size()
 	{
 		return q.size();
 	}
 
 	@Override
 	public String toString()
 	{
 		return super.toString() + ":" + q.size();
 	}
 }
