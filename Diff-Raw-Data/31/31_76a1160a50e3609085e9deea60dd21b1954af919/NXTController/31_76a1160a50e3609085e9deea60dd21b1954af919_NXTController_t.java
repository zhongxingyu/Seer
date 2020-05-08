 package net.contentcube.robot.nxt;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.LinkedList;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import net.contentcube.robot.nxt.commands.MovementCommand;
 import net.contentcube.robot.nxt.commands.PingCommand;
 import android.bluetooth.BluetoothSocket;
 import android.util.Log;
 
 public class NXTController
 {
 	public static final int TIME_BETWEEN_PING = 7500; // miliseconds.
 	private static NXTController mInstance;
 	
 	// used to ensure the NXT device wont turn off.
 	private Timer mKeepAliveTimer;
 	private TimerTask mPingTask = new TimerTask()
 	{
 		@Override
 		public void run()
 		{
 			if (mOutputStream == null) return;
 			
 			PingCommand ping = new PingCommand();
			ping.run(mOutputStream);
 		}
 	};
 	
 	private OutputStream mOutputStream;
 	private LinkedList<NXTCommand> mCommandQueue;
 	private boolean mRunning = false;
 	private MovementCommand.OnCompleteListener mCompleteListener = new MovementCommand.OnCompleteListener() {
 
 		@Override
 		public void onComplete()
 		{
 			mRunning = false;
 			runQueue();
 		}
 		
 	};
 	
 	public static NXTController getInstance()
 	{
 		if (mInstance == null)
 		{
 			mInstance = new NXTController();
 		}
 		
 		return mInstance;
 	}
 	
 	public NXTController()
 	{
 		mCommandQueue = new LinkedList<NXTCommand>();
 		mKeepAliveTimer = new Timer();
 		mKeepAliveTimer.scheduleAtFixedRate(mPingTask, TIME_BETWEEN_PING, TIME_BETWEEN_PING);
 	}
 	
 	public void setBluetoothSocket(BluetoothSocket socket)
 	{
 		try
 		{
 			mOutputStream = socket.getOutputStream();
 		}
 		catch (IOException e)
 		{
 			e.printStackTrace();
 		}
 	}
 	
 	public void forward(int duration)
 	{
 		MovementCommand command = new MovementCommand(NXTCommand.Command.FORWARD);
 		command.setDuration(duration);
 		
 		queue(command);
 	}
 	
 	public void turnRight(int duration)
 	{
 		MovementCommand command = new MovementCommand(NXTCommand.Command.TURN_RIGHT);
 		command.setDuration(duration);
 		
 		queue(command);
 	}
 	
 	public void turnLeft(int duration)
 	{
 		MovementCommand command = new MovementCommand(NXTCommand.Command.TURN_LEFT);
 		command.setDuration(duration);
 		
 		queue(command);
 	}
 	
 	public void backward(int duration)
 	{
 		MovementCommand command = new MovementCommand(NXTCommand.Command.BACKWARD);
 		command.setDuration(duration);
 		
 		queue(command);
 	}
 	
 	public void brake()
 	{
 		MovementCommand command = new MovementCommand(NXTCommand.Command.BRAKE);
 		queue(command);
 	}
 	
 	public void queue(NXTCommand command)
 	{
 		mCommandQueue.add(command);
 		runQueue();
 	}
 	
 	private void runQueue()
 	{
 		if (mRunning) return;
 		mRunning = true;
 		
 		NXTCommand command = mCommandQueue.poll();
 		
 		if (command != null)
 		{
 			command.setOnCompleteListener(mCompleteListener);
 			command.run(mOutputStream);
 			Log.e("NXT", "command-sent");
 		}
 		else
 		{
 			mRunning = false;
 		}
 	}
 }
