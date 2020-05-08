 package servlet;
 
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.logging.ConsoleHandler;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import shared.Command;
 import shared.Communication;
 
 public class Servlet extends HttpServlet
 {
 	private static final long serialVersionUID = 6938369357587229915L;
 
 	private static Logger LOGGER;
 
 	private Simulation simulation;
 
 	private int simulationSpeed = 50;
 
 	public Servlet()
 	{
 		super();
 
 		LOGGER = Logger.getLogger(Servlet.class.getName());
 		LOGGER.setLevel(Level.ALL);
 		LOGGER.addHandler(new ConsoleHandler());
 
 		simulation = new Simulation();
 
 		LOGGER.fine(System.currentTimeMillis() + " Created simulation");
 
 		final Thread t = new Thread(new Runnable()
 		{
 			public void run()
 			{
 				LOGGER.fine(System.currentTimeMillis() + " Entered run");
 				while(!Thread.currentThread().isInterrupted())
 				{
 					try
 					{
 						Thread.sleep(simulationSpeed);
 					}
 					catch(InterruptedException e)
 					{
 						e.printStackTrace();
 						LOGGER.warning("Simulation sleep thread interrupted " + e.getMessage());
 						Thread.currentThread().interrupt();
 					}
 					LOGGER.fine("Began a step-through of the simulation");
 					simulation.run();
 					LOGGER.fine("Completed a step-through of the simulation");
 				}
 			}
 		});
 		t.start();
 
 		LOGGER.fine("Created servlet and started simulation");
 	}
 
 	public void closeInput(ObjectInputStream ob)
 	{
 		try
 		{
 			ob.close();
 		}
 		catch(IOException e)
 		{
 			e.printStackTrace();
 			LOGGER.warning("Unable to close input stream " + e.getMessage());
 		}
 	}
 
 	public void closeOutput(ObjectOutputStream outputToApplet)
 	{
 		try
 		{
 			outputToApplet.flush();
 			outputToApplet.close();
 		}
 		catch(IOException e)
 		{
 			e.printStackTrace();
 			LOGGER.warning("Unable to close output stream " + e.getMessage());
 		}
 	}
 
 	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException
 	{
 		response.setContentType("application/x-java-serialized-object");
 
 		ObjectInputStream inputFromApplet = null;
 		try
 		{
 			inputFromApplet = new ObjectInputStream(request.getInputStream());
 		}
 		catch(IOException e)
 		{
 			e.printStackTrace();
 			LOGGER.warning("Unable get input stream " + e.getMessage());
 		}
 
 		Command c = null;
 		try
 		{
 			c = (Command) inputFromApplet.readObject();
 		}
 		catch(ClassNotFoundException | IOException e)
 		{
 			e.printStackTrace();
 			LOGGER.warning("Problem with reading in the command " + e.getMessage());
 		}
 
 		LOGGER.fine("Received " + c + " command");
 
 		ObjectOutputStream outputToApplet = null;
 		try
 		{
 			outputToApplet = new ObjectOutputStream(response.getOutputStream());
 		}
 		catch(IOException e)
 		{
 			e.printStackTrace();
 			LOGGER.warning("Unable make output stream " + e.getMessage());
 		}
 
 		LOGGER.log(Level.INFO, "created output streams");
 
 		try
 		{
 		switch(c)
 		{
 			case LOG:
 			{
 				LOGGER.log(Level.INFO, "logging message from applet");
 
 				Level level = null;
 				try
 				{
 					level = (Level) inputFromApplet.readObject();
 				}
 				catch(ClassNotFoundException | IOException e)
 				{
 					e.printStackTrace();
 					LOGGER.warning("Problem with reading in the logging level from the applet " + e.getMessage());
 
 					// signify failure
 					outputToApplet.writeObject(new Integer("-1"));
 					outputToApplet.writeObject(Communication.MISSING_ARGUMENT_ERROR.toString() + ": Attempt to read logging level failed");
 
 					break;
 				}
 
 				String message = null;
 				try
 				{
 					message = (String) inputFromApplet.readObject();
 				}
 				catch(ClassNotFoundException | IOException e)
 				{
 					e.printStackTrace();
 					LOGGER.warning("Problem with reading in the message from the applet " + e.getMessage());
 
 					// signify failure
 					outputToApplet.writeObject(new Integer("-1"));
 					outputToApplet.writeObject(Communication.MISSING_ARGUMENT_ERROR.toString() + ": Attempt to read logging message failed");
 
 					break;
 				}
 
 				LOGGER.log(level, message);
 
 				// return true to signify success
 				outputToApplet.writeObject(new Integer("1"));
 				outputToApplet.writeObject(true);

				break;
 			}
 			case ADD_CAR:
 			{
 				LOGGER.log(Level.INFO, "adding cars");
 
 				simulation.addCar();
 
 				// return true to signify success
 				outputToApplet.writeObject(new Integer("1"));
 				outputToApplet.writeObject(true);
 
 				break;
 			}
 			case GET_CARS:
 			{
 				LOGGER.log(Level.INFO, "getting cars");
 
 				outputToApplet.writeObject(new Integer("1"));
 				outputToApplet.writeObject(simulation.getCars());
 
 				break;
 			}
 			default: // as in default response
 			{
 				LOGGER.log(Level.INFO, "command not found: " + c);
 
 				// signify failure
 				outputToApplet.writeObject(new Integer("-1"));
 				outputToApplet.writeObject(Communication.COMMAND_UNKNOWN_ERROR);
 
 				break;
 			}
 		}
 		}
 		catch(IOException e)
 		{
 			e.printStackTrace();
 			LOGGER.warning("Unable make output stream " + e.getMessage());
 		}
 		finally
 		{
 			closeInput(inputFromApplet);
 			closeOutput(outputToApplet);
 		}
 	}
 }
