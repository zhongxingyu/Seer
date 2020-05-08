 package edu.sru.andgate.bitbot.interpreter;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.PipedInputStream;
 import java.io.PrintStream;
 
 import android.util.Log;
 import android.widget.TextView;
 import edu.sru.andgate.bitbot.Bot;
 import edu.sru.andgate.bitbot.parser.Node;
 import edu.sru.andgate.bitbot.parser.SimpleNode;
 import edu.sru.andgate.bitbot.parser.bc1;
 
 public class BotInterpreter
 {
 	private RunThread rThread = null;
 	
 	/**
 	 * This is the root node of an AST generated from the source
 	 * of the botcode program to be executed.
 	 */
 	private SimpleNode root;
 	
 	/**
 	 * This is essentially the "Program Counter".  
 	 * It traverses the AST executing instructions.
 	 */
 	private RunVisitor rv;
 	
 	/**
 	 * This is the object we manipulate to cause changes in accordance with
 	 * the interpreted instructions.
 	 */
 	private Bot bot;
 	
 	/**
 	 * A stream of output from the bot interpreter
 	 */
 	private PipedInputStream botOutput;
 	
 	/**
 	 * A log of the bot interpreter's output.
 	 */
 	private StringBuffer botLog = new StringBuffer();;
 	
 	/**
 	 * A textview destined to receive the output of the bot interpreter
 	 */
 	private TextView outTV;
 	
 	
 	public void abort()
 	{
 		rv.abort();
 	}
 	
 	public void pause()
 	{
 		rv.pause();
 	}
 	
 	/**
 	 * Reads text from botOutput into botLog and sends that to the TextView
 	 * set by setOutputTextView().
 	 */
 	public void flush()
 	{
 		try
 		{
 			while (botOutput.available() > 1)
 				botLog.append( (char) botOutput.read() );
 		}
 		catch (IOException e)
 		{
 			Log.e("BitBot Interpreter", "Error reading InputStream.");
 			Log.e("BitBot Interpreter", e.getStackTrace().toString());
 		}
 		
 		flushLogToOutputTextView();
 	}
 	
 	/**
 	 * Sends the botLog to the output TextView set by setOutputTextView() 
 	 */
 	private void flushLogToOutputTextView()
 	{
 		if (outTV != null)
 			outTV.post(new Runnable() {
 				@Override
 				public void run()
 				{
 					outTV.setText(botLog.toString());
 				}
 			});
 	}
 	
 	/**
 	 * Returns a String that contains all output from the interpreter.
 	 * @return botLog or "" if botLog is null
 	 */
 	public String getBotLog()
 	{
 		if (botLog != null)
 			return botLog.toString();
 		else
 			return "";
 	}
 	
 	
 	// TODO: Dumb, Hack
 	public void setPrintStream(PrintStream p)
 	{
 		rv.setPrintStream(p);
 	}
 	
 	/**
 	 * Sets a TextView to receive the output from this interpreter.  If there is
 	 * anything in the botLog, it gets sent to the textView.
 	 * @param tv
 	 */
 	public void setOutputTextView(TextView tv)
 	{
 		outTV = tv;
 		
 		// When we connect a TextView, send the contents of the botLog to it. 
 		flushLogToOutputTextView();
 	}
 	
 	
 	/**
 	 * Create a BotInterpreter given code as a String.  This constructor creates a
 	 * ByteArrayInputStream out of the <code>code</code> and passes it to
 	 * BotInterpreter(Bot, ByteArrayInputStream).
 	 * @param code the source code to execute.
 	 */
 	public BotInterpreter(Bot b, String code)
 	{
 		this( b, new ByteArrayInputStream(code.getBytes()) );
 	}
 	
 	/**
 	 * Create a BotInterpreter given code as a ByteArrayInputStream.
 	 * <p>
 	 * This compiles the code and sets the programs root.  If there is a compile error,
 	 * it will be in the botLog.
 	 * @param isCode the source code to execute.
 	 */
 	public BotInterpreter(Bot b, ByteArrayInputStream isCode)
 	{
 		// Assign the bot
 		this.bot = b;
 		
 		try
 		{
 			// Create the parser.
 			bc1 parser = new bc1(isCode);
 			
 			// Start the parser
 			root = parser.Start();
 		}
 		catch (Exception e)
 		{
 			System.out.println("Syntax Error.");
 			System.out.println(e.getMessage());
 			
 			botLog.append( "Syntax Error.\n" + e.getMessage() );
 			flushLogToOutputTextView();
 		}
 		
 		if (root != null)
 			Dump(root);
 		else
 			Log.e("BitBot Interpreter", "Error: The code did not compile correctly.  Root is null.");
 	}
 	
 	/**
 	 * Create a BotInterpreter given an AST to execute.
 	 * @param root The root node of the AST this Interpreter should execute.
 	 */
 	public BotInterpreter(Bot b, SimpleNode root)
 	{
 		this.bot = b;
 		this.root = root;			// Store the root of the AST to execute.
 		
 		if (root == null)
 			Log.e("BitBot Interpreter", "Error: Passed a null root node.");		
 	}
 	
 	/**
 	 * Creates a textual representation of the AST starting at node <code>s</code>.
 	 * @param s the node to dump
 	 */
 	private void Dump(Node s)
 	{
 		Dump(s, "");
 	}
 	
 	/**
 	 * Creates a textual representation of the AST starting at node <code>s</code> with
 	 * <code>prefix</code> in front of it.
 	 * <p>
 	 * This code is recursively called to put <code>"- "</code> on the line a number of
 	 * times equal to the depth of the node.
 	 * @param s the node to dump
 	 * @param prefix the string to prefix to all output at this depth
 	 */
 	private void Dump(Node s, String prefix)
 	{
 		
 		System.out.println(prefix + s.toString() + ": " + ((SimpleNode)s).jjtGetValue());
 		
 		for(int i=0; i < s.jjtGetNumChildren(); i++)
 			Dump(s.jjtGetChild(i), prefix + "- ");
 	}
 	
 	/**
 	 * Create a new thread to execute the AST, and begin executing (0 instructions).
 	 */
 	public void start()
 	{
 		if (root == null)
 			Log.e("BitBot Interpreter", "Error: Cannot start().  root is null.");
 		else
 		{
 			if (rThread == null)
 			{
 				rThread = new RunThread(this);
 				rThread.start();
 			}
 			else
 				Log.e("BitBot Interpreter", "ERROR in BotInterpreter.start(), rThread not null.");
 		}
 	}
 	
 	/**
 	 * Execute a specific number of instructions.
 	 * @param numberOfInstructionsToExecute Number of instructions to execute.
 	 */
 	public void resume(int numberOfInstructionsToExecute)
 	{
 		if (rv != null)
 			rv.resume(numberOfInstructionsToExecute);
 		else
 			Log.e("BitBot Interpreter", "RunVisitor was null in BotInterpreter.resume()");
 	}
 	
 	/**
 	 * Interface between a BotInterpreter and a Bot.  Subroutines of the form bot_*() get
 	 * handled by this function.
 	 * @param instr The subroutine name.
 	 * @param params Array of parameters
 	 * @return true if evaluated and executed correctly.
 	 */
 	public boolean executeBotInstruction(String instr, String[] params)
 	{
 //		System.out.println("Executing bot instruction " + instr);
 //		
 //		// Get parameters if there are any
 //		if (params != null)
 //			for (int i=0; i<params.length; i++)
 //			{
 //				System.out.println("param[" + i + "] = " + params[i]);
 //			}
 		
 		if (instr.equalsIgnoreCase("bot_move"))
 		{
 			float degrees = Float.parseFloat(params[0]);
 			float stepSize = Float.parseFloat(params[1]);
 			
			bot.Move(degrees, stepSize);
 			return true;
 		}
 		else if (instr.equalsIgnoreCase("bot_turn"))
 		{
 			return true;
 		}
 		else if (instr.equalsIgnoreCase("bot_fire"))
 		{
 			return true;
 		}
 		else
 			Log.w("BitBot Interpreter", "Instruction not valid" + instr);
 		
 		return false;
 	}
 	
 	
 	
 	
 	/**
 	 * A thread that starts executing on the root node.
 	 * @author Josh
 	 *
 	 */
 	private class RunThread extends Thread
 	{
 		/**
 		 * Check to make sure we only instantiate this thread once per BotInterpreter.
 		 */
 		private RunThread(BotInterpreter bi)
 		{
 			if (rv == null)
 			{
 				rv = new RunVisitor(bi);
 				
 				try
 				{
 					// Set up a Stream between this and the bot.
 					botOutput = new PipedInputStream(rv.getStdOut());
 				}
 				catch (IOException e)
 				{
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 			else
 				Log.e("BitBot Interpreter", "Error: two RunThreads running in BotInterpreter.");
 			
 			// Set name so we can identify thread types
 			setName("=RunThread= " + getName());
 //			setPriority(NORM_PRIORITY);
 //			setPriority(NORM_PRIORITY - 1);
 //			setPriority(9);
 //			setPriority(MAX_PRIORITY);
 		}
 		
 		/**
 		 * Cause the RunVisitor to visit the first node in the AST and traverse it until it
 		 *  comes to an instruction to execute.  It then suspends and waits to resume().
 		 */
 		public void run()
 		{
 			Log.d("BitBot Interpreter", "Thread '" + this.getName() + "' has started.");
 			
 			// Send the visitor to the first node.
 			try
 			{
 				root.jjtAccept(rv, null);
 			}
 			catch (Error e)
 			{
 				Log.w("BitBot Interpreter", "Thread has aborted prematurely.");
 			}
 			
 			Log.d("BitBot Interpreter", "Thread '" + this.getName() + "' has finished.");
 		}
 	}
 	
 	
 	/**
 	 * Determine if this BotInterpreter's thread is waiting or not.
 	 * @return true if this thread is waiting.
 	 */
 	public synchronized boolean isWaiting()
 	{
 		return (rv == null) || rv.isWaiting();
 	}
 
 	
 }
 
 
 
 
