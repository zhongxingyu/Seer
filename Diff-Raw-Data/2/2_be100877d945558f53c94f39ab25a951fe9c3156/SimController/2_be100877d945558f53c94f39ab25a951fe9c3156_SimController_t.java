 /*
  * Copyright (c) 2009 David McIntosh (david.mcintosh@yahoo.com)
  *  
  * This program is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the Free
  * Software Foundation; either version 2 of the License, or (at your option)
  * any later version.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
  * more details.
  *
  * You should have received a copy of the GNU General Public License along
  * with this program; if not, write to the Free Software Foundation, Inc., 51
  * Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
  */
 package nrider.debug;
 
 import gnu.io.PortInUseException;
 import nrider.event.EventPublisher;
 import nrider.event.IEvent;
 import nrider.io.*;
 
 import java.io.IOException;
 import java.util.Random;
 import java.util.Timer;
 import java.util.TimerTask;
 
 /**
  *
  */
 public class SimController implements IWorkoutController, IPerformanceDataSource, IControlDataSource
 {
 	private String _identifier;
 	private double _load;
 	private TrainerMode _trainerMode;
 	private boolean _active;
 	private EventPublisher<IPerformanceDataListener> _performancePublisher = EventPublisher.directPublisher();
 	private Timer _timer = new Timer();
 
 	public SimController( String identifier )
 	{
 		_identifier = identifier;
 		_timer.scheduleAtFixedRate( new DataOutputTask(), 0, 1000 );
 	}
 
 	public String getType()
 	{
 		return "Simulator";
 	}
 
 	public String getIdentifier()
 	{
 		return _identifier;
 	}
 
 	public void setLoad( double load )
 	{
 		_load = load;
 	}
 
 	public double getLoad()
 	{
 		return _load;
 	}
 
 	public void setMode( TrainerMode mode )
 	{
 		_trainerMode = mode;
 	}
 
 	public TrainerMode getMode()
 	{
 		return _trainerMode;
 	}
 
 	public void disconnect() throws IOException
 	{
 		_active = false;
 	}
 
 	public void connect() throws PortInUseException
 	{
 		_active = true;
 	}
 
 	public void close() throws IOException
 	{
 		_active = false;
 	}
 
 	public void addPerformanceDataListener( IPerformanceDataListener listener )
 	{
 		_performancePublisher.addListener( listener );
 	}
 
 	public void addControlDataListener( IControlDataListener listener )
 	{
 
 	}
 
 	public void publishPerformanceData( final PerformanceData data )
 	{
 		_performancePublisher.publishEvent(
 				new IEvent<IPerformanceDataListener>()
 				{
 					public void trigger( IPerformanceDataListener target )
 					{
 						target.handlePerformanceData( getIdentifier(), data );
 					}
 				}
 		);
 	}
 
 	class DataOutputTask extends TimerTask
 	{
 		private double _currentPower;
 		private double _currentSpeed = 21/2.237;
 
 
 		@Override
 		public void run()
 		{
 			if( _active )
 			{
 				if( _currentPower != _load )
 				{
 					_currentPower += ( _load - _currentPower ) / 2;
 				}
 
 				publishPerformanceData( new PerformanceData( PerformanceData.Type.POWER, (float) _currentPower ) );
				publishPerformanceData( new PerformanceData( PerformanceData.Type.SPEED, (float) ( _currentSpeed + ( ( new Random( ).nextFloat() * 2 - 1 ) / 2.237 ) ) ) );
 			}
 		}
 	}
 }
