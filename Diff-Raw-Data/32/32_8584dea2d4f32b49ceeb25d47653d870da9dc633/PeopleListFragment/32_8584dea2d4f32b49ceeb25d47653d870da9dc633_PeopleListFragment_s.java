 /*
 TimeKeeper - Software to monitor how much time each person speaks
 Copyright (C) 2012, 2013 Vincent Hiribarren
 
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 package net.alea.timekeeper;
 
 import java.util.List;
 
 import net.alea.timekeeper.model.Chrono;
 import net.alea.timekeeper.model.TimedElement;
 import android.annotation.SuppressLint;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.DialogFragment;
 import android.app.Fragment;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.Log;
 import android.view.ActionMode;
 import android.view.DragEvent;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.DragShadowBuilder;
 import android.view.ViewGroup;
 import android.widget.AbsListView;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class PeopleListFragment extends Fragment {
 	
 	private final static String LOG_TAG = PeopleListFragment.class.getSimpleName();
 	private final static int GUI_REFRESH_RATE = 500; // in ms
 	
 	private static class DragState {
 		public enum Border {top, bottom, undefined};
 		public boolean ongoingDrag = false;
 		public int targetPosition = 0;
 		public int draggedPosition = 0;
 		public Border border = Border.undefined;
 	}
 	
 	private boolean _multiChrono = false;
 	private DragState _dragState = new DragState();
 	private List<TimedElement> _timedElements;
 	private ListView _timedElementListView;
 	private Button _chronoStopButton;
 	private Button _chronoStartButton;
 
 	private final Handler _handler = new Handler();
 	
 	
 	private final Runnable _updateViewTask = new Runnable() {
 		public void run() {
 			refreshUI();
 			_handler.postDelayed(this, GUI_REFRESH_RATE);
 		}
 	};	
 	
 	private void refreshUI() {
 		if (_timedElementListView != null) {
 			_timedElementListView.invalidateViews();
 		}		
 	}
 	
 	private void startUIRefresh() {
 		_handler.removeCallbacks(_updateViewTask);
 		_handler.postDelayed(_updateViewTask, GUI_REFRESH_RATE);
 	}
 		
 	private void stopUIRefresh() {
 		_handler.removeCallbacks(_updateViewTask);
 	}
 
 	
 	@Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         return inflater.inflate(R.layout.fragment_people_list, container, false);
     }
     
     
     @Override
 	public void onViewCreated(View view, Bundle savedInstanceState) {
     	_chronoStartButton = (Button)view.findViewById(R.id.chronoStartButton);
     	_chronoStartButton.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				startAllChronos();
 				refreshUI();
 			}
 		});
     	_chronoStopButton = (Button)view.findViewById(R.id.chronoStopButton);
     	_chronoStopButton.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				stopAllChronos();
 				refreshUI();
 			}
 		});
     	_timedElementListView = (ListView) view.findViewById(R.id.peopleListView);
     	_timedElementListView.setOnDragListener(new TimedElementListViewDragListener());
     	_timedElementListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 				TimedElement timedElement = (TimedElement)_timedElementListView.getAdapter().getItem(position);
 				Chrono chrono = timedElement.getChrono();
 				if (chrono.isRunning()) {
 					if (!_multiChrono) stopAllChronos(); // Not sure if keeping a set of running chronos worth it (list should have less than 15 elements)
 					else chrono.stop();
 				}
 				else {
 					if (!_multiChrono) stopAllChronos(); // Not sure if keeping a set of running chronos worth it (list should have less than 15 elements)
 					chrono.start();
 				}
 				refreshUI();
 			}
 		});
     	_timedElementListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
     	_timedElementListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {			
 			private MenuItem editMenuItem;
     		@Override
 			public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
 				return false;
 			}
 			@Override
 			public void onDestroyActionMode(ActionMode mode) {
 				refreshUI();
 			}	
 			@Override
 			public boolean onCreateActionMode(ActionMode mode, Menu menu) {
 		        MenuInflater inflater = mode.getMenuInflater();
 		        inflater.inflate(R.menu.people_list_context, menu);
 		        editMenuItem = menu.findItem(R.id.menu_content_edit);
 				return true;
 			}
 			@Override
 			public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
 				switch (item.getItemId()) {
 				case R.id.menu_content_remove:
 					ContentRemoveDialogFragment removeDialog = new ContentRemoveDialogFragment(mode);
 					removeDialog.show(getFragmentManager(), "removePartialDialog");
 					return true;
 				case R.id.menu_reset:
 		        	ChronoResetDialogFragment resetDialog = new ChronoResetDialogFragment(mode);
 		        	resetDialog.show(getFragmentManager(), "resetPartialDialog");
 					return true;
 				case R.id.menu_content_edit:
 				default:
 					return false;
 				}
 			}
 			@Override
 			public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
 				mode.setTitle(""+_timedElementListView.getCheckedItemCount()+" selected");
 				if (_timedElementListView.getCheckedItemCount() == 1) {
 					editMenuItem.setVisible(true);
 					editMenuItem.setEnabled(true);
 				}
 				else {
 					editMenuItem.setVisible(false);
 					editMenuItem.setEnabled(false);					
 				}
 				refreshUI();
 			}
 		});
     	// At the end as it depends on the reference of _chronoStartButton
     	setMultiChrono(false); 
 	}
 
   
 	@Override
 	public void onResume() {
 		super.onResume();
 		startUIRefresh();
 	}
 	
 
 	@Override
 	public void onPause() {
 		super.onPause();
 		stopUIRefresh();
 	}
 	
 	
 	private void startAllChronos() {
 		int count = _timedElementListView.getCount();
 		for (int i=0; i < count; i++) {
 			TimedElement timedElement = (TimedElement)_timedElementListView.getItemAtPosition(i);
 			if (!timedElement.getChrono().isRunning()) {
 				timedElement.getChrono().start();
 			}
 		}			
 	}
 
 
 	private void stopAllChronos() {
 		int count = _timedElementListView.getCount();
 		for (int i=0; i < count; i++) {
 			TimedElement timedElement = (TimedElement)_timedElementListView.getItemAtPosition(i);
 			if (timedElement.getChrono().isRunning()) {
 				timedElement.getChrono().stop();
 			}
 		}		
 	}
 	
 	
 	public void setTimedElementList(List<TimedElement> timedElements) {
 		_timedElements = timedElements;
 		_timedElementListView.setAdapter(new TimedElementViewAdapter(getActivity(), timedElements));
 		_timedElementListView.post(new Runnable() {
 			@Override
 			public void run() {
 				if (_timedElementListView.getCount() > _timedElementListView.getLastVisiblePosition() - _timedElementListView.getFirstVisiblePosition()) {
 
 					Log.i("app", "Create object for " +  _timedElementListView.getLastVisiblePosition());
 					_timedElementListView.getAdapter().getView(_timedElementListView.getLastVisiblePosition()+1, null, null);
 					_timedElementListView.getAdapter().getView(_timedElementListView.getLastVisiblePosition()+2, null, null);
 
 				}	
 			}
 		});
 
    }
 	
 	
 	public void setMultiChrono(boolean multi) {
 		_multiChrono = multi;
 		if (_multiChrono) {
 			_chronoStartButton.setVisibility(View.VISIBLE);
 		}
 		else {
 			_chronoStartButton.setVisibility(View.GONE);			
 		}
 	}
 	
 	
 	private class TimedElementViewAdapter extends ArrayAdapter<TimedElement> {
 		public TimedElementViewAdapter(Context context, List<TimedElement> objects) {
 			super(context, 0, objects);
 		}
 		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
 			final TimedElement timedElement = this.getItem(position);
 			View timedElementView = convertView;
 			if (timedElementView == null) {
 				LayoutInflater inflater =  (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				final View newTimedElementView = inflater.inflate(R.layout.timed_element_view, parent, false);
				final ImageView dragdropImage = (ImageView)newTimedElementView.findViewById(R.id.dragdropImage);
				dragdropImage.setOnTouchListener(new View.OnTouchListener() {
					@Override
					public boolean onTouch(View view, MotionEvent event) {
						_dragState.draggedPosition = position;
						DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(newTimedElementView);
						newTimedElementView.startDrag(null, shadowBuilder, _timedElementListView.getItemAtPosition(position), 0);
						return true;
					}
				});
				timedElementView = newTimedElementView;
 			}
 			// Associates view and position in ListAdapter, needed for drag and drop
 			timedElementView.setId(position);
 			// Get views to fill
 			final TextView nameTextView = (TextView)timedElementView.findViewById(R.id.nameLabel);		
 			final TextView chronoTextView = (TextView)timedElementView.findViewById(R.id.chronoLabel);
 			// If element is currently dragged
 			if (_dragState.ongoingDrag && _dragState.draggedPosition == position) {
 				timedElementView.setBackgroundColor(getResources().getColor(android.R.color.background_dark));
 				nameTextView.setText("");
 				chronoTextView.setText("");
 				return timedElementView;
 			}		
 			// Build username
 			nameTextView.setText(timedElement.getName());
 			// Build chrono text
 			int[] time = timedElement.getChrono().getElapsedTimeHourMinuteSecond();
 			chronoTextView.setText(String.format("%02d:%02d:%02d", time[0], time[1], time[2]));
 			// If selection mode
			final ImageView dragdropImage = (ImageView)timedElementView.findViewById(R.id.dragdropImage);
 			if (_timedElementListView.getCheckedItemCount() == 0) {
 				dragdropImage.setEnabled(true);				
 			}
 			else {
 				dragdropImage.setEnabled(false);				
 			}
 			// If ongoing drag and drop
 			if (_dragState.ongoingDrag && position == _dragState.targetPosition) {
 				timedElementView.setBackgroundColor(Color.YELLOW);
 				return timedElementView;
 			}			
 			// Build background
 			if(_timedElementListView.isItemChecked(position)) {
 				if(timedElement.getChrono().isRunning()) {
 					timedElementView.setBackgroundResource(R.drawable.item_selected_running);					
 				}
 				else {
 					timedElementView.setBackgroundResource(R.drawable.item_selected_normal);				
 				}
 			}
 			else {
 				if (timedElement.getChrono().isRunning()) {
 					timedElementView.setBackgroundColor(getResources().getColor(R.color.chrono_running));
 				}
 				else {
 					timedElementView.setBackgroundColor(getResources().getColor(android.R.color.background_light));
 				}
 			}			
 			return timedElementView;
 		}
 	}
 	
     
 	private class TimedElementListViewDragListener implements View.OnDragListener {
 		private void updateDragState(DragEvent event, int targetPosition, View targetView) {
         	_dragState.targetPosition = targetPosition;
         	if (event.getY() < 	targetView.getHeight()/2 + targetView.getY()) {
         		Log.d(LOG_TAG, "Drag&Drop, top of position "+targetPosition);  
         		_dragState.border = DragState.Border.top;
         	}
         	else {
         		Log.d(LOG_TAG, "Drag&Drop, bottom of position "+targetPosition); 
         		_dragState.border = DragState.Border.bottom;
         	}		
 		}
 		@Override
 		public boolean onDrag(View v, DragEvent event) {
 	        final int action = event.getAction();
         	final int targetPosition = _timedElementListView.pointToPosition((int)event.getX(), (int)event.getY());
         	final View targetView = _timedElementListView.findViewById(targetPosition);
 	        switch(action) {
 		        case DragEvent.ACTION_DRAG_STARTED:
 		        	if (event.getLocalState() instanceof TimedElement) { return true; }
 		        	else { return false; }
 		        case DragEvent.ACTION_DRAG_ENTERED:
 		        	_dragState.ongoingDrag = true;
 		        	refreshUI();
 	                return true;
 		        case DragEvent.ACTION_DRAG_LOCATION: {
 		        	if (targetView == null) {
 		        		// Not a problem, we will act on next drag location event
 		        		return true;
 		        	}  
 		        	updateDragState(event, targetPosition, targetView);
 		        	if (targetPosition > _timedElementListView.getLastVisiblePosition()-2) {
 		        		_timedElementListView.smoothScrollToPosition(targetPosition+2);
 		        	}
 		        	else if (targetPosition < _timedElementListView.getFirstVisiblePosition()+2) {
 		        		_timedElementListView.smoothScrollToPosition(targetPosition-2);
 		        	}
 		        	refreshUI();
 		        	return true;
 		        }
 		        case DragEvent.ACTION_DROP: {
 		        	if (targetView != null) {
 			        	updateDragState(event, targetPosition, targetView);        		
 		        	}
 		        	else {
 		        		// FIXME targetView should not be null, I had this error once, to check if happens again
 		        		Log.w(LOG_TAG, "Error, targetView is null, fallback code");
 		        		_dragState.targetPosition = targetPosition;
 		        		_dragState.border = DragState.Border.undefined;	        		
 		        	}
 		        	Log.d(LOG_TAG, "Drag&Drop, drop on top of "+targetPosition+" with view "+targetView); 
 		        	if (_dragState.targetPosition != _dragState.draggedPosition) {
 		        		final TimedElement draggedElement = _timedElements.remove(_dragState.draggedPosition);		        		
 		        		int finalPosition =  _dragState.targetPosition < _dragState.draggedPosition ?
 		        				_dragState.targetPosition :
 		        				_dragState.targetPosition - 1;
 		        		if (_dragState.border.equals(DragState.Border.bottom)) finalPosition++;
 		        		if (finalPosition < 0) finalPosition = 0;
 		        		if (finalPosition > _timedElements.size()-1) finalPosition = _timedElements.size() -1;
 		        		_timedElements.add(finalPosition, draggedElement);
 		        	}
 		        	refreshUI();
 		        	return true;
 		        }
 		        case DragEvent.ACTION_DRAG_EXITED:
 		        case DragEvent.ACTION_DRAG_ENDED:
 		        	_dragState.ongoingDrag = false;
 		        	return true;
 		        default:
 		        	break;
 	        }
 			return false;
 		}		
 	}
 	
 	
 	@SuppressLint("ValidFragment")
 	private class ChronoResetDialogFragment extends DialogFragment {
 		private ActionMode _actionMode;
 		public ChronoResetDialogFragment(ActionMode actionMode) {
 			_actionMode = actionMode;
 		}
 	    @Override
 	    public Dialog onCreateDialog(Bundle savedInstanceState) {
         	AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
         	builder.setMessage(R.string.dlg_reset_message_partial);
             builder.setTitle(R.string.dlg_reset_title);
             builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int id) {
                 	for(int i=_timedElements.size()-1; i>=0; i--) {
                 		if (_timedElementListView.isItemChecked(i)) {
                 			_timedElements.get(i).getChrono().reset();
                 		}
                 	}
                    _actionMode.finish();
                 }
             });
             builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int id) {
                     // Nothing to do
                 }
             });
             return builder.create();
 	    }
 	}
 	
 	
 	@SuppressLint("ValidFragment")
 	private class ContentRemoveDialogFragment extends DialogFragment {
 		private ActionMode _actionMode;
 		public ContentRemoveDialogFragment(ActionMode actionMode) {
 			_actionMode = actionMode;
 		}
 	    @Override
 	    public Dialog onCreateDialog(Bundle savedInstanceState) {
         	AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
         	builder.setMessage(R.string.dlg_content_remove_message_partial);
             builder.setTitle(R.string.dlg_content_remove_title);
             builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int id) {
                 	// Reverse order as we delete on place
                 	for(int i=_timedElements.size()-1; i>=0; i--) {
                 		if (_timedElementListView.isItemChecked(i)) {
                 			_timedElements.remove(i);
                 		}
                 	}
                    _actionMode.finish();
                 }
             });
             builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int id) {
                     // Nothing to do
                 }
             });
             return builder.create();
 	    }
 	}
 	
 }
