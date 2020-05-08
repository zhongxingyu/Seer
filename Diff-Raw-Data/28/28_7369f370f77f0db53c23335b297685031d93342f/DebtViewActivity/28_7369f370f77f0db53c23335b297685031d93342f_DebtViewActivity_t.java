 package android.debtlistandroid;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import console.Main;
 
 import session.Session;
 
 import logic.Debt;
 import logic.DebtStatus;
 import android.os.Bundle;
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Color;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class DebtViewActivity extends ListActivity {
 
 	private DebtAdapter adapter;
 	private Debt selectedDebt = null;
 	// Only for testing purposes
 	private static boolean exampleDebtsAdded = false;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 //		setContentView(R.layout.activity_debt_view);
 		
 		// Set up list view
 		if(!exampleDebtsAdded) {
 			// Add some example debts
 	//		Session.session.getUser().addConfirmedDebt(new Debt(44, 12.0, "pokes", Session.session.getUser(), new User("Arne"), "Test", Session.session.getUser(), DebtStatus.CONFIRMED));
 	//		Session.session.getUser().addConfirmedDebt(new Debt(55, 3, "Banana", new User("Arne"), Session.session.getUser(), "Test", Session.session.getUser(), DebtStatus.CONFIRMED));
 	//		Session.session.getUser().addConfirmedDebt(new Debt(66, 20, "kr", new User("Arne"), Session.session.getUser(), "Test", Session.session.getUser(), DebtStatus.CONFIRMED));
 //			Session.session.getUser().addPendingDebt(new Debt(77, 3, "kr", new User("Arne"), Session.session.getUser(), "TestPending", Session.session.getUser(), DebtStatus.REQUESTED));
 //			Session.session.getUser().addPendingDebt(new Debt(88, 3, "kr", Session.session.getUser(), new User("Arne"), "TestPending", Session.session.getUser(), DebtStatus.REQUESTED));
 			exampleDebtsAdded = true;
 		}
 		List<List<Debt>> debts = new ArrayList<List<Debt>>();
 		debts.add(new ArrayList<Debt>());
		// Add confirmed debts that are not completed
//		debts.add(Session.session.getUser().getConfirmedDebts());
		List<Debt> confirmedDebts = new ArrayList<Debt>();
		for(Debt d : Session.session.getUser().getConfirmedDebts()) {
			// Don't display completed debts
			if(d.getStatus() != DebtStatus.COMPLETED) {
				confirmedDebts.add(d);
			}
		}
		debts.add(confirmedDebts);
 		debts.add(new ArrayList<Debt>());
 		// Split pending debts requested by the user, and pending debts requested by an other user
 		for (Debt d : Session.session.getUser().getPendingDebts()) {
 			if(d.getRequestedBy().equals(Session.session.getUser())) {
 				debts.get(2).add(d);
 			} else {
 				debts.get(0).add(d);
 			}
 		}
 		List<String> separators = new ArrayList<String>();
 		separators.add(getResources().getString(R.string.debt_view_incoming_requests_separator));
 		separators.add(getResources().getString(R.string.debt_view_confirmed_separator));
 		separators.add(getResources().getString(R.string.debt_view_outgoing_requests_separator));
 		adapter = new DebtAdapter(this, debts, separators, constructNullList(debts.get(0).size() + debts.get(1).size() + debts.get(2).size()));
 		setListAdapter(adapter);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_debt_view, menu);
 		return true;
 	}
 
 	public boolean onOptionsItemSelected(MenuItem item) {
 		if(item.getItemId() == R.id.menu_create_debt) {
 			// Check if the user has any confirmed friends
 			if(Session.session.getUser().getNumberOfFriends() == 0) {
 				// If none, display message telling the user to add a friend before adding a debt
 				new AlertDialog.Builder(this).setMessage(R.string.debt_view_dialog_no_friends).show();
 			} else {
 				startActivity(new Intent(this, CreateDebtActivity.class));
 			}
 		} else if(item.getItemId() == R.id.menu_friends) {
 			startActivity(new Intent(this, FriendViewActivity.class));
 		} else if(item.getItemId() == R.id.menu_settings) {
 			startActivity(new Intent(this, SettingsActivity.class));
 		} else {
 			System.out.println("WTF?!");
 		}
 		return true;
 	}
 	
 	public void complete_debt(View v) {
 		Main.processCommand("complete debt " + selectedDebt.getId());
 		recreate();
 	}
 	
 	public void accept_debt(View v) {
 		Main.processCommand("accept debt " + selectedDebt.getId());
 		recreate();
 	}
 	
 	public void decline_debt(View v) {
 		Main.processCommand("decline debt " + selectedDebt.getId());
 		recreate();
 	}
 	
 	/**
 	 * Constructs and returns a list filled with nulls.
 	 * @param size	The size of the list (number of nulls to put in it)
 	 * @return		The list containing the nulls
 	 */
 	public static List<Debt> constructNullList(int size) {
 		List<Debt> list = new ArrayList<Debt>();
 		for (int i = 0; i < size; i++) {
 			list.add(null);
 		}
 		return list;
 	}
 	
 	class DebtAdapter extends ArrayAdapter<Debt> {
 
 		private List<List<Debt>> debtLists;
 		private Context context;
 		private List<String> separatorTexts;
 		private View lastExpandedView = null;
 		private int numberOfElements;
 		
 		public DebtAdapter(Context context, List<List<Debt>> debtLists, List<String> separatorTexts, List<Debt> nullList) {
 			super(context, R.layout.activity_debt_view, (nullList.size() == 0 ? constructNullList(1) : nullList));
 			this.context = context;
 			this.separatorTexts = separatorTexts;
 			this.debtLists = debtLists;
 			this.numberOfElements = nullList.size();
 		}
 		
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 			final View view = inflater.inflate(R.layout.activity_debt_view, null);
 			if(numberOfElements == 0) {
 				// Display message about no debts
 				((TextView) view.findViewById(R.id.toptext)).setText(getResources().getString(R.string.debt_view_no_debts1));
 				TextView bot = (TextView) view.findViewById(R.id.bottomtext);
 				bot.setText(getResources().getString(R.string.debt_view_no_debts2));
 				collapse(view);
 //				bot.setLayoutParams(new LinearLayout.LayoutParams(android.widget.LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
 				return view;
 			}
 			TextView sep = (TextView) view.findViewById(R.id.separator);
 			// Find the corresponding debt
 			Debt d = null;
 			int count = 0; // The number of debts 'before' the current list
 			int listIndex = -1;
 			System.out.println("Checking position: " + position);
 			for (int i = 0; i < debtLists.size(); i++) {
 				List<Debt> ds = debtLists.get(i);
 				System.out.println("Checking list " + i + " " + ds);
 				System.out.println("Count: " + count);
 				if(position >= ds.size() + count) {
 					count += ds.size();
 				} else {
 					d = ds.get(position - count);
 					listIndex = i;
 					System.out.println("List index: " + listIndex);
 					break;
 				}
 			}
 			if (position == count) {
 				// Add separator if on top of list
 				sep.setVisibility(View.VISIBLE);
 				sep.setText(separatorTexts.get(listIndex));
 				sep.setBackgroundColor(Color.BLACK);
 				sep.setTextColor(Color.WHITE);
 			} else {
 				// Else remove the space
 				sep.setHeight(0);
 			}
 			// Fix debt text
 			TextView topTextView = (TextView) view.findViewById(R.id.toptext);
 			TextView bottomTextView = (TextView) view.findViewById(R.id.bottomtext);
 			topTextView.setText(d.getAmount() + " " + d.getWhat());
 			bottomTextView.setText((d.getFrom().equals(Session.session.getUser()) ? d.getTo() : d.getFrom()).getUsername());
 			TextView comment = (TextView) view.findViewById(R.id.commenttext);
 			comment.setText(d.getComment());
 			// Collapse this view
 			collapse(view);
 			// Set the background color
 			view.setBackgroundColor((d.getFrom().equals(Session.session.getUser()) ? Color.rgb(204, 0, 0) : Color.rgb(0, 204, 102)));
 			// Expand on click
 //			final boolean isConfirmed = listIndex == 0;
 			final Debt debt = d;
 			view.setOnClickListener(new View.OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					if(lastExpandedView == view) {
 						collapse(lastExpandedView);
 						lastExpandedView = null;
 						return;
 					}
 					if(lastExpandedView != null) {
 						collapse(lastExpandedView);
 					}
 //					expand(v, isConfirmed);
 					expand(v, debt);
 					lastExpandedView = v;
 					selectedDebt = debt;
 				}
 			});
 			return view;
 		}
 		
 		public void expand(View view, Debt d) {
 			((TextView) view.findViewById(R.id.commenttext)).setVisibility(View.VISIBLE);
 			if(d.isConfirmed()) {
 				// Check that this user already haven't marked this debt as completed
 				if((d.getFrom().equals(Session.session.getUser()) && d.getStatus() != DebtStatus.COMPLETED_BY_FROM) ||
 						(d.getTo().equals(Session.session.getUser()) && d.getStatus() != DebtStatus.COMPLETED_BY_TO)) {
 					((Button) view.findViewById(R.id.button_complete)).setVisibility(View.VISIBLE);
 				}
 			} else {
 				// Check that this is not the user that requested the debt
 				if(!d.getRequestedBy().equals(Session.session.getUser())) {
 					((Button) view.findViewById(R.id.button_accept)).setVisibility(View.VISIBLE);
 					((Button) view.findViewById(R.id.button_decline)).setVisibility(View.VISIBLE);
 				}
 			}
 		}
 		
 		public void collapse(View view) {
 			((TextView) view.findViewById(R.id.commenttext)).setVisibility(View.GONE);
 			((Button) view.findViewById(R.id.button_accept)).setVisibility(View.GONE);
 			((Button) view.findViewById(R.id.button_complete)).setVisibility(View.GONE);
 			((Button) view.findViewById(R.id.button_decline)).setVisibility(View.GONE);
 		}
 		
 	}
 }
