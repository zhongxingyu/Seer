 package com.example.airbrushrecorder.fragments;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.AdapterView.OnItemSelectedListener;
 
 import com.example.airbrushrecorder.Flight;
 import com.example.airbrushrecorder.LoginHelper;
 import com.example.airbrushrecorder.MainActivity;
 import com.example.airbrushrecorder.R;
 import com.example.airbrushrecorder.WebInterface;
 import com.example.airbrushrecorder.data.FlightsDataSource;
 import com.example.airbrushrecorder.dialog.DialogDeleteFlight;
 import com.example.airbrushrecorder.fragments.FragmentRecorder.OnToggleRecordingListener;
 
 public class FragmentFlightBrowser extends Fragment
 {
 	private static String TAG = "FlightBrowser";
 	private Spinner _spinner = null;
 	
 	private ArrayList<Flight> _flights = null;
 	
 	private OnFlightBrowserListener m_listener;
 	
 	public interface OnFlightBrowserListener //well, thats a stupid name...
 	{
 		public void submitSelectedFlight(View view);
 		public void deleteSelectedFlight(View view);
 	}
 	
 	private final OnItemSelectedListener m_itemSelectedListener = new OnItemSelectedListener()
 	{
 		@Override
 		public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id)
 		{
 			displayMetaData();
 	    }
 		
 		 @Override
 	    public void onNothingSelected(AdapterView<?> parentView)
 		{
 			 
 	    }
 	};
 	
 	@Override
 	public void onAttach(Activity activity)
 	{
 		super.onAttach(activity);
 		
 		if(activity instanceof OnToggleRecordingListener)
 		{
 			m_listener = (OnFlightBrowserListener) activity;
 		}
 		else
 		{
 			throw new ClassCastException(activity.toString() + "must implement OnFlightBrowserListener");
 		}
 	}
 	
 	@Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
 	{
 		super.onCreateView(inflater, container, savedInstanceState);
 		
 		return inflater.inflate(R.layout.fragment_flight_browser, container, false);
 	}
 	
 	@Override
 	public void onStart()
 	{
 		super.onStart();
 		
 		listFiles();
 	}
 	
 	
 	private void listFiles()
 	{
 		//Log.d(TAG, "list files");
 		
 		try
 		{
 			FlightsDataSource dataSource = new FlightsDataSource(this.getActivity());
 			dataSource.open();
 			_flights = dataSource.getFlights();
 			//dataSource.printAllWaypoints();
 			dataSource.close();
 			
 			List<String> list = new ArrayList<String>();
 			for(int i = 0; i < _flights.size(); i++)
 			{
 				list.add(_flights.get(i).getDate().toString());
 			}
 			
 			_spinner = (Spinner) getView().findViewById(R.id.dropdown_file);
 			
 			ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_spinner_item, list);
 			dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 			_spinner.setAdapter(dataAdapter);
 			_spinner.setOnItemSelectedListener(m_itemSelectedListener);
 			
 			displayMetaData();
 		}
 		catch(Exception e)
 		{
 			Log.e(TAG, e.toString());
 		}
 	}
 
 	public void deleteSelectedFlight(View view)
 	{
 		try
 		{
 			DialogDeleteFlight dialog = new DialogDeleteFlight();
 			dialog.show(this.getActivity().getSupportFragmentManager(), TAG);
 		}
 		catch(Exception e)
 		{
 			Log.d(TAG, e.toString());
 		}
 	}
 	
 	public void deleteSelectedFlight()
 	{
 		try
 		{
 			Flight flight = getSelectedFlight();
 			if(flight != null)
 			{
 				FlightsDataSource dataSource = new FlightsDataSource(this.getActivity());
 				dataSource.open();
 				dataSource.deleteFlight(flight.getId(), true);
 				dataSource.close();
 				
 				listFiles();
 			}
 		}
 		catch(Exception e)
 		{
 			Log.d(TAG, e.toString());
 		}
 	}
 	
 	public void submitSelectedFlight(View view)
 	{
 		try
 		{
 			Flight flight = getSelectedFlight();
 			if(flight != null)
 			{
 				FlightsDataSource dataSource = new FlightsDataSource(this.getActivity());
 				dataSource.open();
 				String sessionData = dataSource.getCookie();
 				dataSource.close();
 				
 				LoginHelper loginHelper = new LoginHelper();
 				if(sessionData.length() <= 0 || loginHelper.ipChanged(this.getActivity()))
 				{
 					loginHelper.login(this.getActivity());
 					
 					dataSource.open();
 					sessionData = dataSource.getCookie();
 					dataSource.close();
 				}
 				
 				if(sessionData.length() > 0)
 				{
 					submitSelectedFlight(flight, sessionData);
 				}
 			}
 		}
 		catch(Exception e)
 		{
 			Log.d(TAG, e.toString());
 		}
 	}
 	
 	private void submitSelectedFlight(Flight flight, String sessionData)
 	{
		WebInterface wf = new WebInterface(getActivity());
 		wf.postFlight(flight, sessionData);
 	}
 	
 	private Flight getSelectedFlight()
 	{
 		if(_spinner != null && _flights != null)
 		{
 			Object selectedItem = _spinner.getSelectedItem();
 			if(selectedItem == null)
 				return null;
 			
 			String fileName = selectedItem.toString();
 			
 			for(int i = 0; i < _flights.size(); i++)
 			{
 				Flight flight = _flights.get(i);
 				String date = flight.getDate().toString();
 				
 				if(fileName.equals(date))
 				{
 					return flight;
 				}
 			}
 		}
 		
 		return null;
 	}
 	
 	private void displayMetaData()
 	{
 		if(_spinner != null)
 		{
 			Flight flight = getSelectedFlight();
 			
 			if(flight == null)
 			{
 				TextView textView = (TextView)getView().findViewById(R.id.textview_flight_data);
 				textView.setText("No flight selected");
 				return;
 			}
 			
 			try
 			{
 				String date = "Date: " + flight.getDate().toString();
 				String departure = "Departure: " + flight.getDeparture();
 				String destination = "Destination: " + flight.getDestination();
 				
 				TextView textView = (TextView)getView().findViewById(R.id.textview_flight_data);
 				textView.setText(date + "\n" + departure + "\n" + destination + "\nWaypoints: " + flight.getWaypointCount());
 			}
 			catch(Exception e)
 			{
 				Log.e(TAG, e.getMessage());
 			}
 		}
 	}
 	
 	public void onDialogPositiveClick(DialogFragment dialog)
 	{
 		deleteSelectedFlight();
 	}
 }
