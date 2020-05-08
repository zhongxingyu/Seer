 package com.diamond.matrix;
 
 import java.io.Serializable;
 import java.text.NumberFormat;
 import java.text.ParseException;
 import java.util.Vector;
 
 import android.content.Context;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnLongClickListener;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.EditText;
 import android.widget.GridView;
 import android.widget.PopupMenu;
 import android.widget.PopupMenu.OnMenuItemClickListener;
 import android.view.View.OnFocusChangeListener;
 
 public class MatrixAdaptor extends BaseAdapter {
 	Vector<Double> theMatrix;
 	int columns;
 	NumberFormat nf;
 	LayoutInflater infl;
 	GridView myGrid;
 	static final String LOG_TAG = "MatrixAdapter (no Keanu)";
 	
 	public MatrixAdaptor() {
 		nf = NumberFormat.getNumberInstance();
 		
 		columns = 3;
 		theMatrix = new Vector<Double>();
 		addRow();
 	}
 
 	@SuppressWarnings("unchecked")
 	public MatrixAdaptor(Serializable serializable, int int1) {
		columns = int1;
		nf = NumberFormat.getNumberInstance();
 		try {
 			theMatrix = (Vector<Double>)serializable;
 		} catch (ClassCastException e) {
			theMatrix = new Vector<Double>();
 			Log.e(LOG_TAG, "Failed to extract serializable");
			addRow();
 		}
 	}
 
 	public int addRow() {
 		for (int i=0; i < columns; ++i) {
 			theMatrix.add(0.0);
 		}
 		notifyDataSetChanged();
 		return theMatrix.size()/columns;
 	}
 	
 	public int addRow(int beforeRow) {
 		int numRows = theMatrix.size()/columns;
 		if (beforeRow < 0 || beforeRow > numRows) {
 			return -1;
 		}
 		beforeRow *= columns;
 		for (int i=0; i < columns; ++i) {
 			theMatrix.add(beforeRow, 0.0);
 		}
 		notifyDataSetChanged();
 		return numRows + 1;
 	}
 	
 	public int addColumn() {
 		int newSize = theMatrix.size()/columns * (columns + 1);
 		for (int i=columns; i <= newSize; i += columns) {
 			theMatrix.add(i, 0.0);
 			++i;
 		}
 		++columns;
 		if (myGrid != null) {
 			myGrid.setNumColumns(columns);
 		}
 		notifyDataSetChanged();
 		return columns;
 	}
 	
 	public int addColumn(int beforeColumn) {
 		if (beforeColumn < 0 || beforeColumn > columns) {
 			return -1;
 		}
 		int newSize = theMatrix.size()/columns * (columns + 1);
 		for (int i=beforeColumn; i <= newSize; i += columns) {
 			theMatrix.add(i, 0.0);
 			++i;
 		}
 		++columns;
 		if (myGrid != null) {
 			myGrid.setNumColumns(columns);
 		}
 		notifyDataSetChanged();
 		return columns;
 	}
 	
 	public int delRow(int rownum) {
 		if (rownum < 0) {
 			return -1;
 		}
 		int location = rownum * columns;
 		if (location >= theMatrix.size()) {
 			return -1;
 		}
 		for (int i=0; i < columns; ++i) {
 			theMatrix.removeElementAt(location);
 		}
 		notifyDataSetChanged();
 		return 0;
 	}
 	
 	public int delColumn(int colnum) {
 		if (colnum < 0 || colnum >= columns) {
 			return -1;
 		}
 		--columns;
 		for (int i=colnum; i <= theMatrix.size(); i += columns) {
 			theMatrix.removeElementAt(i);
 		}
 		if (myGrid != null) {
 			myGrid.setNumColumns(columns);
 		}
 		notifyDataSetChanged();
 		return 0;
 	}
 	
 	@Override
 	public int getCount() {
 		return theMatrix == null ? 0 : theMatrix.size();
 	}
 
 	@Override
 	public Object getItem(int arg0) {
 		return theMatrix.get(arg0);
 	}
 
 	@Override
 	public long getItemId(int arg0) {
 		return arg0;
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		if (myGrid == null) {
 			myGrid = (GridView) parent;
 		}
 		EditText ret = (EditText)convertView;
 		final MatrixActivity _context = (MatrixActivity)parent.getContext();
 		final int _pos = position;
 		
 		if (ret == null) {
 			if (infl == null) {
 				infl = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 			}
 			ret = (EditText) infl.inflate(R.layout.grid_text_view, null);
 		} else {
 			ret.removeTextChangedListener((TextWatcher)ret.getTag());
 		}
 		final EditText _ret = ret;
 		ret.setOnLongClickListener(new OnLongClickListener() {
 			
 			@Override
 			public boolean onLongClick(View v) {
 				PopupMenu pm = new PopupMenu(_context, _ret);
 				pm.getMenuInflater().inflate(R.menu.popup, pm.getMenu());
 				pm.setOnMenuItemClickListener(new OnMenuItemClickListener() {
 					
 					@Override
 					public boolean onMenuItemClick(MenuItem item) {
 						_context.handleContextMenuClick(item, _pos);
 						return true;
 					}
 				});
 				pm.show();
 				return true;
 			}
 		});
 		
 		ret.setText(nf.format(theMatrix.get(position)));
 		
 		TextWatcher tw = new TextWatcher() {
 			
 			@Override
 			public void onTextChanged(CharSequence s, int start, int before, int count) {
 				Number val = null;
 				try {
 					val = (Number) nf.parse(s.toString());
 					theMatrix.set(_pos, val.doubleValue());
 				} catch (ParseException e) {
 					e.printStackTrace();
 				}
 			}
 			
 			@Override
 			public void beforeTextChanged(CharSequence s, int start, int count,
 					int after) {
 				
 			}
 			
 			@Override
 			public void afterTextChanged(Editable s) {
 				
 			}
 		};
 		ret.addTextChangedListener(tw);
 		ret.setTag(tw);
 		
 		ret.setOnFocusChangeListener(new OnFocusChangeListener() {
 			
 			@Override
 			public void onFocusChange(View v, boolean hasFocus) {
 				if (!hasFocus) {
 					// if we lost the focus, reformat the number
 					_ret.setText(nf.format(theMatrix.get(_pos)));
 				} else {
 					if (theMatrix.get(_pos) == 0) {
 						_ret.setText("");
 					} else {
 						_ret.selectAll();
 					}
 				}
 			}
 		});
 		return ret;
 	}
 
 	public Vector<Double> getDoubleArray() {
 		return theMatrix;
 	}
 	
 	public void replaceMatrix(Vector<Double> newMatrix, int newColumns) {
 		theMatrix = newMatrix;
 		columns = newColumns;
 		notifyDataSetChanged();
 	}
 	
 	public void updateMatrix() {
 		notifyDataSetChanged();
 	}
 
 }
