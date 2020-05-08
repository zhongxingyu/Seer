 package no.hig.qrcontact;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import no.hig.qrcontact.QRContactAlerts.QRContactAlertType;
 import no.hig.qrcontact.db.ContactDBHendler;
 import no.hig.qrcontact.db.MyQRContactDBHendler;
 import no.hig.qrcontact.entities.Contact;
 import android.annotation.SuppressLint;
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class QRContactListActivity extends ListActivity {
 
 	private List<Contact> contactList;
 	private List<Contact> contactListTemp;
 	private QRContactListAdapter listAdapter;
 
 	private ContactDBHendler contactDb;
 
 	private QRContactAlerts alerts;
 
 	private QRContactListAsync listAsync;
 
 	private EditText search;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.qrcontact_list);
 
 		contactDb = new ContactDBHendler(getBaseContext());
 
 		contactList = new ArrayList<Contact>();
 		contactListTemp = new ArrayList<Contact>();
 
 		listAsync = new QRContactListAsync();
 
 		listAsync.execute();
 
 		alerts = new QRContactAlerts(this);
 
 		search = (EditText) findViewById(R.id.search_input);
 		search.addTextChangedListener(new TextWatcher() {
 
 			@Override
 			public void onTextChanged(CharSequence s, int start, int before,
 					int count) {
 
 				if (s.toString().trim().length() > 0) {
 					contactList.clear();
 					for (int i = 0; i < contactListTemp.size(); i++) {
 						if (contactListTemp
 								.get(i)
 								.getName()
 								.toLowerCase()
 								.contains(
 										s.toString()
 												.toLowerCase())) {
 							contactList.add(contactListTemp.get(i));
 						}
 					}
 				} else {
 					contactList.clear();
 					contactList.addAll(contactListTemp);
 				}
 
 			}
 
 			@Override
 			public void beforeTextChanged(CharSequence s, int start, int count,
 					int after) {
 				// TODO Auto-generated method stub
 
 			}
 
 			@Override
 			public void afterTextChanged(Editable s) {
 				listAdapter.notifyDataSetChanged();
 			}
 		});
 
 	}
 
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 
 		Contact c = contactList.get(position);
		Log.d("ContactID", "ID: " + c.getId());
 		ShowListContactAlert(c);
 
 	};
 
 	private class QRContactListAdapter extends ArrayAdapter<Contact> {
 
 		public QRContactListAdapter(Context context, int resource) {
 			super(context, resource);
 			// TODO Auto-generated constructor stub
 		}
 
 		@Override
 		public int getCount() {
 			// TODO Auto-generated method stub
 			return contactList.size();
 		}
 
 		@Override
 		public Contact getItem(int position) {
 			// TODO Auto-generated method stub
 			return contactList.get(position);
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 
 			View view = convertView;
 
 			ListViewHolder holder = null;
 
 			if (view == null) {
 				LayoutInflater vi = LayoutInflater.from(getContext());
 				view = vi.inflate(R.layout.qrcontact_list_holder, null);
 
 				holder = new ListViewHolder();
 
 				holder.name = (TextView) view
 						.findViewById(R.id.name_surname_holder);
 				holder.number = (TextView) view
 						.findViewById(R.id.number_holder);
 
 				view.setTag(holder);
 			} else {
 				holder = (ListViewHolder) view.getTag();
 			}
 
 			Contact data = getItem(position);
 			if (data != null) {
 				holder.name.setText(data.getName() + " " + data.getSurname());
 				holder.number.setText(data.getNumber());
 			}
 
 			return view;
 		}
 
 		class ListViewHolder {
 			TextView name;
 			TextView number;
 		}
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// TODO Auto-generated method stub
 		getMenuInflater().inflate(R.menu.activity_qrcard, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// TODO Auto-generated method stub
 		switch (item.getItemId()) {
 		case R.id.reader_camera:
 			startActivityForResult(new Intent(getApplicationContext(),
 					QRContactReaderActivity.class), 456);
 			return true;
 		case R.id.my_qrcontact:
 			startActivity(new Intent(getApplicationContext(),
 					MyQRContactActivity.class));
 			return true;
 		case R.id.share_my_qrcontact:
 			Intent i = new Intent(getApplicationContext(),
 					QRImageGeneratorActivity.class);
 			Contact c = (new MyQRContactDBHendler(getApplicationContext()))
 					.getContact(1);
 			if (c != null && c.getName().trim().length() != 0
 					&& c.getSurname().trim().length() != 0
 					&& c.getNumber().trim().length() != 0) {
 				i.putExtra("value", Contact.objectToString(c));
 				startActivity(i);
 			} else {
 				Toast.makeText(QRContactListActivity.this, R.string.no_data,
 						Toast.LENGTH_SHORT).show();
 			}
 		default:
 			return false;
 		}
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 
 		if (resultCode == RESULT_OK && requestCode == 456) {
 			String result = data.getStringExtra("result");
 			Contact c = (Contact) Contact.stringToObject(result);
 			if (c != null && c.getName() != null && c.getSurname() != null
 					&& c.getNumber() != null) {
 				if (contactDb.addContact(c)) {
 					alerts.ShowAddedAlert(QRContactAlertType.SUCCES);
 					contactList.add(c);
 					listAdapter.notifyDataSetChanged();
 				} else {
 					alerts.ShowAddedAlert(QRContactAlertType.EXISTS);
 				}
 			} else {
 				alerts.ShowAddedAlert(QRContactAlertType.ERROR);
 			}
 
 		} else if (resultCode == RESULT_OK && requestCode == 92564) {
 			listAsync.doInBackground();
 			listAdapter.notifyDataSetChanged();
 		}
 	}
 
 	private class QRContactListAsync extends
 			AsyncTask<Contact, Contact, Contact> {
 
 		ProgressDialog progress;
 
 		@Override
 		protected void onPostExecute(Contact result) {
 			// TODO Auto-generated method stub
 			progress.dismiss();
 
 			listAdapter = new QRContactListAdapter(getBaseContext(),
 					R.layout.qrcontact_list);
 
 			setListAdapter(listAdapter);
 
 		}
 
 		@Override
 		protected void onPreExecute() {
 			// TODO Auto-generated method stub
 			progress = ProgressDialog.show(QRContactListActivity.this,
 					getString(R.string.loading), getString(R.string.loading_contacts));
 		}
 
 		@Override
 		protected Contact doInBackground(Contact... params) {
 			contactListTemp.clear();
 			contactList.clear();
 			contactList = contactDb.getAllContacts();
 			Collections.sort(contactList, new Contact.Compare());
 			contactListTemp.addAll(contactList);
 			return null;
 		}
 
 	}
 
 	@SuppressLint("NewApi")
 	public void ShowListContactAlert(final Contact c) {
 
 		AlertDialog.Builder alert = new AlertDialog.Builder(this);
 		alert.setTitle(R.string.contact_option_title);
 		alert.setIcon(android.R.drawable.ic_menu_view);
 
 		final LayoutInflater li = LayoutInflater.from(this);
 		View v = li.inflate(R.layout.qrcontact_list_click, null);
 
 		alert.setView(v);
 
 		final AlertDialog dialog = alert.create();
 
 		dialog.show();
 
 		Button call = (Button) v.findViewById(R.id.dialog_call);
 		Button share = (Button) v.findViewById(R.id.dialog_share);
 		Button edit = (Button) v.findViewById(R.id.dialog_edit);
 		Button delete = (Button) v.findViewById(R.id.dialog_delete);
 		Button email = (Button) v.findViewById(R.id.dialog_email);
 		Button sms = (Button) v.findViewById(R.id.dialog_sms);
 
 		call.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				Intent callIntent = new Intent(Intent.ACTION_CALL);
 				callIntent.setData(Uri.parse("tel:" + c.getNumber()));
 				startActivity(callIntent);
 
 				dialog.dismiss();
 
 			}
 		});
 
 		sms.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				Intent smsIntent = new Intent(Intent.ACTION_SENDTO);
 				// smsIntent.putExtra("sms_body", "");
 				smsIntent.setData(Uri.parse("smsto:" + c.getNumber()));
 				startActivity(smsIntent);
 				dialog.dismiss();
 			}
 		});
 
 		if (c.getEmail() == null
 				|| c.getEmail().trim().length() == 0
 				|| !android.util.Patterns.EMAIL_ADDRESS.matcher(c.getEmail())
 						.matches()) {
 			email.setVisibility(View.GONE);
 		} else {
 			email.setOnClickListener(new View.OnClickListener() {
 
 				@Override
 				public void onClick(View v) {
 					Intent emailIntent = new Intent(Intent.ACTION_SEND);
 					emailIntent.setType("plain/text");
 					emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
 							new String[] { c.getEmail() });
 					emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
 							"Subject");
 					emailIntent.putExtra(android.content.Intent.EXTRA_TEXT,
 							"Text");
 
 					/* Send it off to the Activity-Chooser */
 					startActivity(Intent.createChooser(emailIntent,
 							"Send mail..."));
 					dialog.dismiss();
 
 				}
 			});
 		}
 		share.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				Intent i = new Intent(li.getContext(),
 						QRImageGeneratorActivity.class);
 				i.putExtra("value", Contact.objectToString(c));
 				startActivity(i);
 
 				dialog.dismiss();
 
 			}
 		});
 
 		edit.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				Intent i = new Intent(li.getContext(),
 						QRContactEditActivity.class);
 				i.putExtra("id", c.getId());
 				startActivityForResult(i, 92564);
 				dialog.dismiss();
 			}
 		});
 
 		delete.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				deleteConfirmation(c);
 				dialog.dismiss();
 			}
 		});
 
 	}
 
 	public void deleteConfirmation(final Contact c) {
 		AlertDialog.Builder alert = new AlertDialog.Builder(this);
 		alert.setTitle(R.string.contact_delete_title);
 		alert.setIcon(android.R.drawable.ic_dialog_info);
 		alert.setMessage(R.string.contact_delete_message);
 
 		alert.setPositiveButton(R.string.yes, new OnClickListener() {
 
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				contactDb.deleteContact(c);
 				contactList.remove(c);
 				listAdapter.notifyDataSetChanged();
 
 			}
 		});
 
 		alert.setNegativeButton(R.string.no, null);
 
 		AlertDialog dialog = alert.create();
 		dialog.show();
 	}
 
 }
