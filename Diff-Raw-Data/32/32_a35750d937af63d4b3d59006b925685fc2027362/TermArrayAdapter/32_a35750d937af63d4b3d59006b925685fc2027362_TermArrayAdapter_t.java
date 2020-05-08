 package com.selagroup.schedu.adapters;
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.List;
 
 import android.app.AlertDialog;
 import android.app.DatePickerDialog;
 import android.app.DatePickerDialog.OnDateSetListener;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.DatePicker;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.selagroup.schedu.R;
 import com.selagroup.schedu.managers.TermManager;
 import com.selagroup.schedu.model.Term;
 
 public class TermArrayAdapter extends ArrayAdapter<Term> {
 
 	public interface TermEditListener {
 		public void onTermEdit(Term iTerm);
		public void onTermDelete(Term iTerm);
 	}
 
 	private TermEditListener mListener;
 	private TermManager mTermManager;
 	
 	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM d, yyyy");
 
 	private Context mContext;
 	private List<Term> mTerms;
 	private boolean mEditEnabled = false;
 	private boolean mAddEnabled = false;
 	private int mEditIndex = -1;
 
 	private static AlertDialog.Builder mDeleteAlert;
 	private AlertDialog mDeleteAlertDialog;
 
 	private static class ViewHolder {
 		private TextView term_adapter_tv_select;
 		private Button term_btn_delete;
 		private Button term_btn_start;
 		private Button term_btn_end;
 	}
 
 	public TermArrayAdapter(Context iContext, int textViewResourceId, List<Term> iTerms,
 			TermEditListener iListener, TermManager iTermManager) {
 		super(iContext, textViewResourceId, iTerms);
 		mContext = iContext;
 		mTerms = iTerms;
 		mListener = iListener;
 		mTermManager = iTermManager;
 		mDeleteAlert = new AlertDialog.Builder(mContext);
 	}
 
 	public void setEditEnabled(boolean iEditEnabled) {
 		mEditEnabled = iEditEnabled;
 	}
 	
 	public void setAddEnabled(boolean iAddEnabled) {
 		mAddEnabled = iAddEnabled;
 	}
 
 	public void setEditIndex(int iEditIndex) {
 		mEditIndex = iEditIndex;
 	}
 
 	@Override
 	public View getView(int position, View row, ViewGroup parent) {
 		ViewHolder tmpHolder = null;
 
 		// Only get the items from the layout once
 		if (row == null) {
 			LayoutInflater li = LayoutInflater.from(mContext);
 			row = li.inflate(R.layout.adapter_term_select, null);
 			tmpHolder = new ViewHolder();
 			tmpHolder.term_adapter_tv_select = (TextView) row.findViewById(R.id.term_adapter_tv_select);
 			tmpHolder.term_btn_delete = (Button) row.findViewById(R.id.term_btn_delete);
 			tmpHolder.term_btn_start = (Button) row.findViewById(R.id.term_btn_start);
 			tmpHolder.term_btn_end = (Button) row.findViewById(R.id.term_btn_end);
 			row.setTag(tmpHolder);
 		} else {
 			tmpHolder = (ViewHolder) row.getTag();
 		}
 		final ViewHolder holder = tmpHolder;
 
 		// Get the item
 		final Term term = mTerms.get(position);
 
 		// If there's a term, set up the widgets
 		if (term != null) {
 			boolean editThisRow = mEditIndex == position;
 			holder.term_adapter_tv_select.setText(term.toString());
 			holder.term_adapter_tv_select.setVisibility(!editThisRow || !mEditEnabled ? View.VISIBLE : View.GONE);
 			holder.term_btn_delete.setVisibility(mEditEnabled && !mAddEnabled ? View.VISIBLE : View.GONE);
 			holder.term_btn_start.setVisibility(mEditEnabled && editThisRow ? View.VISIBLE : View.GONE);
 			holder.term_btn_end.setVisibility(mEditEnabled && editThisRow ? View.VISIBLE : View.GONE);
 
 			Calendar startDate = term.getStartDate();
 			Calendar endDate = term.getEndDate();
 			if (editThisRow && startDate != null) {
 				holder.term_btn_start.setText(DATE_FORMAT.format(startDate.getTime()));
 			}
 			else {
 				holder.term_btn_start.setText("Select Start");
 			}
 			if (editThisRow && endDate != null) {
 				holder.term_btn_end.setText(DATE_FORMAT.format(endDate.getTime()));
 			}
 			else {
 				holder.term_btn_end.setText("Select End");
 			}
 
 			// If edit enabled, show editing options
 			if (mEditEnabled) {
 				holder.term_btn_delete.setOnClickListener(new OnClickListener() {
 					public void onClick(View view) {
 						mDeleteAlert.setTitle("Warning!");
 						mDeleteAlert.setMessage("You will lose all your data associated with this term! Are you sure you want to delete this term?");
 						mDeleteAlert.setPositiveButton("Delete", new AlertDialog.OnClickListener() {
 							public void onClick(DialogInterface dialog, int which) {
								mListener.onTermDelete(term);
 							}
 						});
 						mDeleteAlert.setNegativeButton("Cancel", new AlertDialog.OnClickListener() {
 							public void onClick(DialogInterface dialog, int which) {
 								mDeleteAlertDialog.dismiss();
 							}
 						});
 						mDeleteAlertDialog = mDeleteAlert.create();
 						mDeleteAlertDialog.show();
 					}
 				});
 				// THIS row is being edited
 				if (editThisRow) {
 					holder.term_btn_start.setOnClickListener(new OnClickListener() {
 						public void onClick(View v) {
 							setStartDate(term.getStartDate(), term, holder.term_btn_start);
 						}
 					});
 					holder.term_btn_end.setOnClickListener(new OnClickListener() {
 						public void onClick(View v) {
 							setEndDate(term.getEndDate(), term, holder.term_btn_end);
 						}
 					});
 				}
 			}
 		}
 		// No terms, show a message
 		else {
 			holder.term_adapter_tv_select.setVisibility(View.VISIBLE);
 			holder.term_adapter_tv_select.setText("No terms. Create a term first.");
 			holder.term_btn_delete.setVisibility(View.GONE);
 			holder.term_btn_start.setVisibility(View.GONE);
 			holder.term_btn_end.setVisibility(View.GONE);
 		}
 
 		return row;
 	}
 
 	private void setStartDate(Calendar initDate, final Term iTerm, final Button iStartButton) {
 		if (initDate == null) {
 			initDate = Calendar.getInstance();
 		}
 		// Set up start date dialog
 		DatePickerDialog startDateDialog = new DatePickerDialog(mContext, new OnDateSetListener() {
 			public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
 				Calendar newDate = Calendar.getInstance();
 				newDate.set(year, monthOfYear, dayOfMonth);
 				if (newDate.after(iTerm.getEndDate())) {
 					Toast.makeText(mContext, "Term cannot start after it ends.", Toast.LENGTH_SHORT).show();
 				}
 				else {
 					iTerm.setStartDate(newDate);
 					notifyDataSetChanged();
 					mListener.onTermEdit(iTerm);
 				}
 			}
 		}, initDate.get(Calendar.YEAR), initDate.get(Calendar.MONTH), initDate.get(Calendar.DAY_OF_MONTH));
 		startDateDialog.setTitle("Set Term Start Date");
 		startDateDialog.show();
 	}
 
 	private void setEndDate(Calendar initDate, final Term iTerm, final Button iEndButton) {
 		if (initDate == null) {
 			initDate = Calendar.getInstance();
 		}
 		// Set up end date dialog
 		DatePickerDialog endDateDialog = new DatePickerDialog(mContext, new OnDateSetListener() {
 			public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
 				Calendar newDate = Calendar.getInstance();
 				newDate.set(year, monthOfYear, dayOfMonth);
 				if (newDate.before(iTerm.getStartDate())) {
 					Toast.makeText(mContext, "Term cannot start after it ends.", Toast.LENGTH_SHORT).show();
 				}
 				else {
 					iTerm.setEndDate(newDate);
 					notifyDataSetChanged();
 					mListener.onTermEdit(iTerm);
 				}
 			}
 		}, initDate.get(Calendar.YEAR), initDate.get(Calendar.MONTH), initDate.get(Calendar.DAY_OF_MONTH));
 		endDateDialog.setTitle("Set Term End Date");
 		endDateDialog.show();
 	}
 }
